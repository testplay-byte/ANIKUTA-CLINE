package app.confused.anikuta.core.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.OutputStream
import kotlin.coroutines.coroutineContext

/**
 * The actual HTTP file downloader (the "DEFAULT" method — ADR-020).
 *
 * Downloads the video via OkHttp streaming (with a [ProgressReporter] so the
 * queue can emit progress ticks), then downloads ALL subtitle tracks alongside
 * (per DOWNLOADS-PLAN: subtitles are always downloaded, no user option), then
 * writes `data/metadata.json`.
 *
 * **Cancellation / pause.** This is a `suspend` function; the [DownloadQueue]
 * runs it in a child Job. Pausing or cancelling the task = cancelling that Job,
 * which throws `CancellationException` here (caught by the queue → moves the
 * task to PAUSED/CANCELLED). We cooperatively check [ensureActive] in the copy
 * loop so a large file cancels promptly.
 *
 * **All I/O on Dispatchers.IO** (Rule §9). The OkHttp client is shared (passed
 * in from the DI module — single instance, connection-pooled).
 *
 * **Errors.** Network/IO failures throw [DownloadException] with a human-readable
 * message; the queue catches it and moves the task to ERROR.
 *
 * **Future 1DM method.** A future `OneDmDownloader` will implement multi-
 * threaded ranged downloads with resume; it will replace this class for the
 * ONEDM method. The [DownloadManager] interface stays the same.
 */
class HttpDownloader(
    private val client: OkHttpClient,
    private val storage: DownloadStorageProvider,
) {

    /**
     * Downloads the video + all subtitles + metadata for [task].
     *
     * @param task The task to download (must have a non-blank videoUrl).
     * @param onProgress Called on every progress tick with (downloadedBytes, totalBytes).
     * @return The updated task with [DownloadTask.videoUri] + [DownloadTask.subtitleUris] set.
     * @throws DownloadException on network/IO failure.
     * @throws kotlinx.coroutines.CancellationException on pause/cancel.
     */
    suspend fun download(
        task: DownloadTask,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): DownloadTask = withContext(Dispatchers.IO) {
        val anime = task.request.anime
        val episode = task.request.episode
        val videoUrl = task.request.videoUrl

        if (videoUrl.isBlank()) {
            throw DownloadException("Video URL is blank — cannot download")
        }

        DownloadLogger.i("Starting download: ${anime.title} EP ${episode.episodeNumber} ($videoUrl)")

        val epDir = storage.ensureEpisodeDir(anime, episode)
            ?: throw DownloadException("Download folder not configured or not writable")

        // ── 1. Download the video (streaming with progress) ──
        val videoUri = downloadVideo(videoUrl, task.request.videoHeaders, epDir, onProgress)
            ?: throw DownloadException("Failed to create/write the video file")

        // ── 2. Download ALL subtitle tracks ──
        val subtitleUris = downloadSubtitles(task.request.subtitleTracks, epDir)

        // ── 3. Write metadata.json ──
        storage.writeMetadata(epDir, EpisodeMetadataCache(
            anilistId = anime.anilistId,
            animeTitle = anime.title,
            episodeNumber = episode.episodeNumber,
            episodeName = episode.name,
            videoUrl = videoUrl,
            downloadedAt = System.currentTimeMillis(),
            sourceId = task.request.sourceId,
        ))

        DownloadLogger.i("Download complete: ${anime.title} EP ${episode.episodeNumber} " +
            "(subs=${subtitleUris.size})")

        task.copy(
            status = DownloadStatus.COMPLETED,
            progress = 100,
            videoUri = videoUri,
            subtitleUris = subtitleUris,
            updatedAt = System.currentTimeMillis(),
        )
    }

    /** Streams the video to disk, reporting progress. Returns the video content URI. */
    private suspend fun downloadVideo(
        url: String,
        headers: String?,
        epDir: androidx.documentfile.provider.DocumentFile,
        onProgress: (Long, Long) -> Unit,
    ): String? {
        val request = Request.Builder().url(url).apply {
            if (!headers.isNullOrBlank()) {
                // Headers are stored as a single "Key: Value\nKey2: Value2" string
                // (matches WatchRequest.videoHeaders / MPV http-header-fields format).
                headers.split('\n').forEach { line ->
                    val sep = line.indexOf(':')
                    if (sep > 0) {
                        addHeader(line.substring(0, sep).trim(), line.substring(sep + 1).trim())
                    }
                }
            }
        }.build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw DownloadException("HTTP ${response.code} for video URL")
                }
                val total = response.body?.contentLength() ?: -1L
                val out: OutputStream = storage.openVideoOutputStream(epDir, url)
                    ?: return null
                val videoFile = epDir.listFiles().firstOrNull {
                    it.name?.startsWith("video.") == true
                }

                out.use { os ->
                    response.body?.byteStream()?.use { input ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var downloaded = 0L
                        while (true) {
                            // Cooperative cancellation — pause/cancel throws here.
                            coroutineContext.ensureActive()
                            val read = input.read(buffer)
                            if (read == -1) break
                            os.write(buffer, 0, read)
                            downloaded += read
                            onProgress(downloaded, total)
                        }
                        os.flush()
                    }
                }
                videoFile?.uri?.toString()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Propagate cancellation (pause/cancel) — the queue handles cleanup.
            DownloadLogger.d("Video download cancelled/paused")
            throw e
        } catch (e: DownloadException) {
            throw e
        } catch (e: Exception) {
            throw DownloadException("Video download failed: ${e.message ?: e.javaClass.simpleName}", e)
        }
    }

    /** Downloads every subtitle track. Best-effort: a failed subtitle is skipped (logged). */
    private suspend fun downloadSubtitles(
        tracks: List<DownloadTrack>,
        epDir: androidx.documentfile.provider.DocumentFile,
    ): List<String> {
        val uris = mutableListOf<String>()
        tracks.forEachIndexed { index, track ->
            coroutineContext.ensureActive()
            try {
                val out = storage.openSubtitleOutputStream(epDir, track, index) ?: return@forEachIndexed
                out.use { os ->
                    client.newCall(Request.Builder().url(track.url).build()).execute().use { resp ->
                        if (!resp.isSuccessful) {
                            DownloadLogger.w("Subtitle $index (${track.lang}) HTTP ${resp.code} — skipped")
                            return@use
                        }
                        resp.body?.byteStream()?.use { it.copyTo(os) }
                    }
                }
                // Capture the URI we just wrote.
                val subDir = epDir.findFile("data")?.findFile("subtitles")
                val safeLang = track.lang.ifBlank { "track" }
                    .replace(Regex("[^A-Za-z0-9 ]"), " ").trim().ifBlank { "track" }
                subDir?.findFile("${safeLang}_$index.${subtitleExt(track.url)}")?.uri?.toString()
                    ?.let { uris.add(it) }
                DownloadLogger.d("Subtitle $index (${track.lang}) downloaded")
            } catch (e: Exception) {
                // A missing subtitle must NOT fail the whole download — skip it.
                DownloadLogger.w("Subtitle $index (${track.lang}) failed — skipped", e)
            }
        }
        return uris
    }

    private fun subtitleExt(url: String): String {
        val noQuery = url.substringBefore('?')
        val dot = noQuery.lastIndexOf('.')
        if (dot < 0) return "srt"
        return when (noQuery.substring(dot + 1).lowercase()) {
            "ass", "srt", "vtt", "ssa", "sub" -> noQuery.substring(dot + 1).lowercase()
            else -> "srt"
        }
    }

    companion object {
        private const val BUFFER_SIZE = 8 * 1024 // 8 KB — balance of throughput vs memory
    }
}

/** Thrown when a download fails (network/IO/HTTP error). Carries a user-facing message. */
class DownloadException(message: String, cause: Throwable? = null) : Exception(message, cause)
