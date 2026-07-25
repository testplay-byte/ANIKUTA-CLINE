package app.confused.anikuta.download

import android.util.Log
import app.confused.anikuta.core.download.DownloadAnimeInfo
import app.confused.anikuta.core.download.DownloadEpisodeInfo
import app.confused.anikuta.core.download.DownloadManager
import app.confused.anikuta.core.download.DownloadRequest
import app.confused.anikuta.core.download.DownloadTrack
import app.confused.anikuta.core.download.TrackKind
import app.confused.anikuta.feature.videoresolver.ResolverResult
import app.confused.anikuta.feature.videoresolver.ResolverServer
import app.confused.anikuta.feature.videoresolver.ResolverVideo
import app.confused.anikuta.feature.videoresolver.ResolverService
import app.confused.anikuta.feature.videoresolver.SubtitleTrack
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.SEpisode

/**
 * Bridges `:feature:video-resolver` and `:core:download`.
 *
 * **Why this lives in `:app` (not a feature/core module):** `:core:download`
 * cannot import `:feature:video-resolver` (Rule §14 — feature isolation), so
 * the resolve→enqueue orchestration happens here, where both are available.
 * `:app` is the composition root and already depends on both.
 *
 * **Flow:**
 *  1. `ResolverService.resolve(source, episode)` (same flow as watching).
 *  2. Auto-pick the best video (highest resolution; fallback first). The
 *     download button is a single tap — no quality picker for the MVP
 *     (documented decision; a future enhancement could surface a picker).
 *  3. Build a [DownloadRequest] (video URL + headers + ALL subtitle tracks) +
 *     `DownloadManager.enqueueDownload`.
 *
 * All network work runs on `Dispatchers.IO` (ResolverService + the manager
 * enforce this internally — Rule §9).
 *
 * @param resolver The shared [ResolverService] (also used by the watch flow).
 * @param manager The download manager (Koin-injected).
 */
class DownloadOrchestrator(
    private val resolver: ResolverService,
    private val manager: DownloadManager,
) {

    /**
     * Resolve + enqueue a download.
     *
     * @param anime The anime identity (anilistId drives the folder structure).
     * @param episode The episode to download.
     * @param source The matched source (for video resolution).
     * @return [EnqueueResult] — Success(taskId), NoSources, AlreadyExists, or Error.
     */
    suspend fun enqueueDownload(
        anime: DownloadAnimeInfo,
        episode: SEpisode,
        source: AnimeSource,
    ): EnqueueResult {
        if (!manager.isFolderReady()) {
            Log.w(TAG, "enqueueDownload: no download folder configured")
            return EnqueueResult.Error("No download folder set. Open Downloads → settings to pick one.")
        }

        Log.i(TAG, "Resolving video for download: ${anime.title} EP ${episode.episode_number}")
        return try {
            when (val result = resolver.resolve(source, episode)) {
                is ResolverResult.Success -> {
                    val video = pickBestVideo(result.servers)
                        ?: return EnqueueResult.NoSources
                    val request = buildRequest(anime, episode, source, video)
                    val taskId = manager.enqueueDownload(request)
                    if (taskId < 0) {
                        EnqueueResult.Error("Failed to enqueue (invalid request).")
                    } else {
                        Log.i(TAG, "Enqueued: ${anime.title} EP ${episode.episode_number} (task $taskId)")
                        EnqueueResult.Success(taskId)
                    }
                }
                is ResolverResult.NoSources -> EnqueueResult.NoSources
                is ResolverResult.Error -> EnqueueResult.Error(result.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download enqueue failed", e)
            EnqueueResult.Error(e.message ?: e.javaClass.simpleName)
        }
    }

    /**
     * Pick the best video from the 3-tier server/audio/quality hierarchy.
     * Strategy: flatten, prefer the one with the highest numeric resolution;
     * fall back to the first. We do NOT surface a picker — the download button
     * is a single tap (documented MVP decision).
     */
    private fun pickBestVideo(servers: List<ResolverServer>): ResolverVideo? {
        val all = servers.flatMap { it.audioVersions }.flatMap { it.videos }
        if (all.isEmpty()) return null
        return all.maxByOrNull { video ->
            // Extract the leading integer from the quality string (e.g. "1080p" → 1080).
            video.quality.trim().takeWhile { it.isDigit() }.toIntOrNull() ?: 0
        } ?: all.first()
    }

    private fun buildRequest(
        anime: DownloadAnimeInfo,
        episode: SEpisode,
        source: AnimeSource,
        video: ResolverVideo,
    ): DownloadRequest {
        val epInfo = DownloadEpisodeInfo(
            episodeUrl = episode.url,
            episodeNumber = episode.episode_number,
            name = episode.name,
            scanlator = episode.scanlator,
        )
        return DownloadRequest(
            anime = anime,
            episode = epInfo,
            videoUrl = video.url,
            videoHeaders = video.videoHeaders,
            subtitleTracks = video.subtitleTracks.map { it.toDownloadTrack(TrackKind.SUBTITLE) },
            audioTracks = video.audioTracks.map { it.toDownloadTrack(TrackKind.AUDIO) },
            sourceId = source.id,
        )
    }

    private fun SubtitleTrack.toDownloadTrack(kind: TrackKind) =
        DownloadTrack(url = url, lang = lang, kind = kind)

    companion object {
        private const val TAG = "AnikutaDownloadOrch"
    }
}

/** Result of [DownloadOrchestrator.enqueueDownload]. */
sealed interface EnqueueResult {
    data class Success(val taskId: Long) : EnqueueResult
    data object NoSources : EnqueueResult
    data class Error(val message: String) : EnqueueResult
}
