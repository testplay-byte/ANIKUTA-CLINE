package app.confused.anikuta.core.backup

import app.confused.anikuta.core.backup.model.AnimeBackup
import app.confused.anikuta.core.backup.model.AnimeCategoryBackup
import app.confused.anikuta.core.backup.model.CategoryBackup
import app.confused.anikuta.core.backup.model.EpisodeBackup
import app.confused.anikuta.core.backup.model.EpisodeMetadataBackup
import app.confused.anikuta.core.backup.model.PreferenceBackup
import app.confused.anikuta.core.backup.model.SourceLinkBackup
import app.confused.anikuta.core.backup.model.TrackerBackupModel
import app.confused.anikuta.core.backup.model.WatchProgressBackup
import kotlinx.serialization.Serializable

/**
 * A sealed class representing one provider's backup payload.
 *
 * Each [BackupProvider] exports one [BackupEntry] subclass and imports the same
 * type. The [providerId] is a compile-time constant per subclass (marked
 * `@Transient` so kotlinx-serialization doesn't try to serialize it — the
 * polymorphic class discriminator handles type identity in the JSON).
 *
 * Adding a new data type:
 *  1. Add a new subclass here (with `@Serializable`, `@Transient providerId`).
 *  2. Create the data model in `model/`.
 *  3. Create a [BackupProvider] implementation in `provider/`.
 *  4. Register the provider in [BackupModule].
 *  5. Add a [BackupCategory] entry.
 *
 * This sealed class enables exhaustive `when` matching and compile-time
 * safety when new types are added.
 */
@Serializable
sealed class BackupEntry {

    /** Stable identifier matching [BackupProvider.id] and [BackupCategory.id]. */
    @Transient
    abstract val providerId: String

    /** Library anime (favorites from the `animes` table). */
    @Serializable
    data class Library(
        val animes: List<AnimeBackup> = emptyList(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.LIBRARY.id
    }

    /** Full anime details (description, genres, scores — the non-favorite columns). */
    @Serializable
    data class AnimeDetails(
        val animes: List<AnimeBackup> = emptyList(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.ANIME_DETAILS.id
    }

    /** Episodes per anime (keyed by anime DB id). */
    @Serializable
    data class Episodes(
        val byAnime: Map<String, List<EpisodeBackup>> = emptyMap(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.EPISODES.id
    }

    /** Enriched episode metadata per anime (keyed by AniList ID). */
    @Serializable
    data class EpisodeMetadata(
        val byAnime: Map<String, EpisodeMetadataBackup> = emptyMap(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.EPISODE_METADATA.id
    }

    /** Watch progress (playback positions). */
    @Serializable
    data class WatchProgress(
        val progress: WatchProgressBackup = WatchProgressBackup(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.WATCH_PROGRESS.id
    }

    /** AniList↔extension source links + extension↔AniList links. */
    @Serializable
    data class SourceLinks(
        val links: SourceLinkBackup = SourceLinkBackup(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.SOURCE_LINKS.id
    }

    /** Tracker tokens + bindings. */
    @Serializable
    data class Tracker(
        val data: TrackerBackupModel = TrackerBackupModel(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.TRACKER.id
    }

    /** Categories + anime–category junction links. */
    @Serializable
    data class Categories(
        val categories: List<CategoryBackup> = emptyList(),
        val links: List<AnimeCategoryBackup> = emptyList(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.CATEGORIES.id
    }

    /** All app preferences. */
    @Serializable
    data class Preferences(
        val prefs: PreferenceBackup = PreferenceBackup(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.PREFERENCES.id
    }

    /**
     * Cover image references. The actual image bytes are stored as files in the
     * zip container (`covers/<anilistId>.jpg`), not in the JSON. This entry just
     * records which anilistIds have bundled covers + their original URLs.
     */
    @Serializable
    data class CoverImages(
        val covers: Map<String, String> = emptyMap(),
    ) : BackupEntry() {
        @Transient override val providerId: String = BackupCategory.COVER_IMAGES.id
    }
}
