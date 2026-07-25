package app.confused.anikuta.core.backup.provider

import android.util.Log
import app.confused.anikuta.core.backup.BackupCategory
import app.confused.anikuta.core.backup.BackupEntry
import app.confused.anikuta.core.backup.BackupProvider
import app.confused.anikuta.core.backup.model.EpisodeMetadataBackup
import app.confused.anikuta.core.backup.model.EpisodeMetadataItem
import app.confused.anikuta.core.episodemetadata.model.EpisodeMetadata
import app.confused.anikuta.core.episodemetadata.repository.EpisodeMetadataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private const val TAG = "AnikutaBackup"

/**
 * Backs up enriched episode metadata (titles, descriptions, thumbnails, air
 * dates from Jikan/MAL/AniList sources).
 *
 * Export reads the [EpisodeMetadataCache] for all anime. Import overwrites the
 * cache for each anime (no merge — the backup is treated as authoritative for
 * the included anime).
 *
 * This is optional (default off) because metadata can be re-fetched from
 * sources. It's useful for offline restore or to avoid re-fetching on a new
 * device.
 */
class EpisodeMetadataBackupProvider(
    private val metadataCache: EpisodeMetadataCache,
) : BackupProvider {

    override val id: String = BackupCategory.EPISODE_METADATA.id

    override suspend fun export(): BackupEntry = withContext(Dispatchers.IO) {
        try {
            val rawCache = metadataCache.getAll()
            val byAnime = mutableMapOf<String, EpisodeMetadataBackup>()
            val json = Json { ignoreUnknownKeys = true }
            val metadataSerializer = MapSerializer(
                Int.serializer(),
                EpisodeMetadata.serializer(),
            )
            rawCache.forEach { (animeIdStr, jsonStr) ->
                try {
                    val metadataMap = json.decodeFromString(metadataSerializer, jsonStr)
                    val items = metadataMap.map { (epNum, meta) ->
                        epNum.toString() to EpisodeMetadataItem(
                            episodeNumber = epNum,
                            title = meta.title,
                            description = meta.description,
                            thumbnailUrl = meta.thumbnailUrl,
                            airDate = meta.airDate,
                            filler = meta.filler,
                            lastFetched = meta.lastFetched,
                        )
                    }.toMap()
                    byAnime[animeIdStr] = EpisodeMetadataBackup(byAnime = items)
                } catch (e: Exception) {
                    Log.w(TAG, "EpisodeMetadata export: failed to parse cache for animeId=$animeIdStr — ${e.message}")
                }
            }
            Log.i(TAG, "EpisodeMetadata export: ${byAnime.size} anime with metadata")
            BackupEntry.EpisodeMetadata(byAnime = byAnime)
        } catch (e: Exception) {
            Log.e(TAG, "EpisodeMetadata export failed", e)
            BackupEntry.EpisodeMetadata()
        }
    }

    override suspend fun import(entry: BackupEntry): Boolean = withContext(Dispatchers.IO) {
        require(entry is BackupEntry.EpisodeMetadata) { "Expected EpisodeMetadata entry, got ${entry.providerId}" }
        if (entry.byAnime.isEmpty()) return@withContext false
        var imported = 0
        entry.byAnime.forEach { (animeIdStr, metadataBackup) ->
            try {
                val animeId = animeIdStr.toIntOrNull() ?: return@forEach
                if (metadataBackup.byAnime.isEmpty()) return@forEach
                // Convert backup model → domain model and save
                val metadataMap = mutableMapOf<Int, EpisodeMetadata>()
                metadataBackup.byAnime.forEach { (epNumStr, item) ->
                    val epNum = epNumStr.toIntOrNull() ?: return@forEach
                    metadataMap[epNum] = EpisodeMetadata(
                        animeId = animeId,
                        episodeNumber = epNum,
                        title = item.title,
                        description = item.description,
                        thumbnailUrl = item.thumbnailUrl,
                        airDate = item.airDate,
                        filler = item.filler,
                        lastFetched = item.lastFetched,
                    )
                }
                if (metadataMap.isNotEmpty()) {
                    metadataCache.save(animeId, metadataMap)
                    imported++
                }
            } catch (e: Exception) {
                Log.w(TAG, "EpisodeMetadata import: failed for animeId=$animeIdStr — ${e.message}")
            }
        }
        Log.i(TAG, "EpisodeMetadata import: $imported anime restored")
        imported > 0
    }
}
