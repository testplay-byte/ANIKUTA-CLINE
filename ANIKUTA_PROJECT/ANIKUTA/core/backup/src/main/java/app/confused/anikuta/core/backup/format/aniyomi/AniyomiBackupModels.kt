package app.confused.anikuta.core.backup.format.aniyomi

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Minimal copy of Aniyomi's protobuf backup models — restore-only.
 *
 * These mirror the `@ProtoNumber` annotations from
 * `ANIYOMI_REFRENCE/.../data/backup/models/Backup*.kt` so that
 * `kotlinx-serialization-protobuf` can decode Aniyomi `.tachibk` files.
 *
 * Only the anime-related fields are included (manga fields are omitted —
 * ANIKUTA is anime-first). Fields that reference Aniyomi domain enums
 * (e.g. `AnimeUpdateStrategy`) are typed as `Int` to avoid pulling in
 * Aniyomi's source-api module.
 *
 * **Adding fields:** match the exact `@ProtoNumber` from the Aniyomi source.
 * Never change an existing proto number — that breaks decoding.
 */

/** Root protobuf backup model (Aniyomi `Backup` / `LegacyBackup`). */
@Serializable
data class AniyomiBackup(
    @ProtoNumber(1) val backupManga: List<AniyomiBackupManga> = emptyList(),
    @ProtoNumber(2) val backupCategories: List<AniyomiBackupCategory> = emptyList(),
    @ProtoNumber(101) val backupSources: List<AniyomiBackupSource> = emptyList(),
    @ProtoNumber(104) val backupPreferences: List<AniyomiBackupPreference> = emptyList(),
    @ProtoNumber(105) val backupSourcePreferences: List<AniyomiBackupSourcePreferences> = emptyList(),
    @ProtoNumber(106) val backupMangaExtensionRepo: List<AniyomiBackupExtensionRepo> = emptyList(),
    // Aniyomi-specific (500+)
    @ProtoNumber(500) val isLegacy: Boolean = true,
    @ProtoNumber(501) val backupAnime: List<AniyomiBackupAnime> = emptyList(),
    @ProtoNumber(502) val backupAnimeCategories: List<AniyomiBackupCategory> = emptyList(),
    @ProtoNumber(503) val backupAnimeSources: List<AniyomiBackupAnimeSource> = emptyList(),
    @ProtoNumber(504) val backupExtensions: List<AniyomiBackupExtension> = emptyList(),
    @ProtoNumber(505) val backupAnimeExtensionRepo: List<AniyomiBackupExtensionRepo> = emptyList(),
)

@Serializable
data class AniyomiBackupAnime(
    @ProtoNumber(1) val source: Long = 0,
    @ProtoNumber(2) val url: String = "",
    @ProtoNumber(3) val title: String = "",
    @ProtoNumber(4) val artist: String? = null,
    @ProtoNumber(5) val author: String? = null,
    @ProtoNumber(6) val description: String? = null,
    @ProtoNumber(7) val genre: List<String> = emptyList(),
    @ProtoNumber(8) val status: Int = 0,
    @ProtoNumber(9) val thumbnailUrl: String? = null,
    @ProtoNumber(13) val dateAdded: Long = 0,
    @ProtoNumber(16) val episodes: List<AniyomiBackupEpisode> = emptyList(),
    @ProtoNumber(17) val categories: List<Long> = emptyList(),
    @ProtoNumber(18) val tracking: List<AniyomiBackupAnimeTracking> = emptyList(),
    @ProtoNumber(100) val favorite: Boolean = true,
    @ProtoNumber(101) val episodeFlags: Int = 0,
    @ProtoNumber(103) val viewer_flags: Int = 0,
    @ProtoNumber(104) val history: List<AniyomiBackupAnimeHistory> = emptyList(),
    @ProtoNumber(105) val updateStrategy: Int = 0,
    @ProtoNumber(106) val lastModifiedAt: Long = 0,
    @ProtoNumber(107) val favoriteModifiedAt: Long? = null,
    @ProtoNumber(109) val version: Long = 0,
)

@Serializable
data class AniyomiBackupEpisode(
    @ProtoNumber(1) val url: String = "",
    @ProtoNumber(2) val name: String = "",
    @ProtoNumber(3) val scanlator: String? = null,
    @ProtoNumber(4) val seen: Boolean = false,
    @ProtoNumber(5) val bookmark: Boolean = false,
    @ProtoNumber(6) val lastSecondSeen: Long = 0,
    @ProtoNumber(16) val totalSeconds: Long = 0,
    @ProtoNumber(7) val dateFetch: Long = 0,
    @ProtoNumber(8) val dateUpload: Long = 0,
    @ProtoNumber(9) val episodeNumber: Float = 0F,
    @ProtoNumber(10) val sourceOrder: Long = 0,
    @ProtoNumber(11) val lastModifiedAt: Long = 0,
    @ProtoNumber(12) val version: Long = 0,
    @ProtoNumber(501) val fillermark: Boolean = false,
    @ProtoNumber(502) val summary: String? = null,
    @ProtoNumber(503) val previewUrl: String? = null,
)

@Serializable
data class AniyomiBackupCategory(
    @ProtoNumber(1) val name: String = "",
    @ProtoNumber(2) val order: Long = 0,
    @ProtoNumber(3) val id: Long = 0,
    @ProtoNumber(100) val flags: Long = 0,
)

@Serializable
data class AniyomiBackupAnimeTracking(
    @ProtoNumber(1) val syncId: Int = 0,
    @ProtoNumber(2) val libraryId: Long = 0,
    @ProtoNumber(3) val mediaIdInt: Int = 0,
    @ProtoNumber(4) val trackingUrl: String = "",
    @ProtoNumber(5) val title: String = "",
    @ProtoNumber(6) val lastEpisodeSeen: Float = 0F,
    @ProtoNumber(7) val totalEpisodes: Int = 0,
    @ProtoNumber(8) val score: Float = 0F,
    @ProtoNumber(9) val status: Int = 0,
    @ProtoNumber(10) val startedWatchingDate: Long = 0,
    @ProtoNumber(11) val finishedWatchingDate: Long = 0,
    @ProtoNumber(12) val private: Boolean = false,
    @ProtoNumber(100) val mediaId: Long = 0,
)

@Serializable
data class AniyomiBackupAnimeHistory(
    @ProtoNumber(1) val url: String = "",
    @ProtoNumber(2) val lastRead: Long = 0,
    @ProtoNumber(3) val readDuration: Long = 0,
)

@Serializable
data class AniyomiBackupAnimeSource(
    @ProtoNumber(1) val name: String = "",
    @ProtoNumber(2) val sourceId: Long = 0,
)

@Serializable
data class AniyomiBackupSource(
    @ProtoNumber(1) val name: String = "",
    @ProtoNumber(2) val sourceId: Long = 0,
)

// ── Stubs (parsed but not deeply processed by ANIKUTA restore) ──

@Serializable
data class AniyomiBackupPreference(
    @ProtoNumber(1) val key: String = "",
    @ProtoNumber(2) val value: AniyomiBackupPreferenceValue? = null,
)

@Serializable
data class AniyomiBackupPreferenceValue(
    @ProtoNumber(1) val int: Int? = null,
    @ProtoNumber(2) val long: Long? = null,
    @ProtoNumber(3) val float: Float? = null,
    @ProtoNumber(4) val string: String? = null,
    @ProtoNumber(5) val boolean: Boolean? = null,
    @ProtoNumber(6) val stringSet: List<String> = emptyList(),
)

@Serializable
data class AniyomiBackupSourcePreferences(
    @ProtoNumber(1) val sourceKey: String = "",
    @ProtoNumber(2) val prefs: List<AniyomiBackupPreference> = emptyList(),
)

@Serializable
data class AniyomiBackupManga(
    @ProtoNumber(1) val source: Long = 0,
    @ProtoNumber(2) val url: String = "",
    @ProtoNumber(3) val title: String = "",
)

@Serializable
data class AniyomiBackupExtension(
    @ProtoNumber(1) val name: String = "",
    @ProtoNumber(2) val version: String = "",
)

@Serializable
data class AniyomiBackupExtensionRepo(
    @ProtoNumber(1) val baseUrl: String = "",
    @ProtoNumber(2) val name: String = "",
    @ProtoNumber(3) val shortName: String? = null,
    @ProtoNumber(4) val website: String? = null,
)
