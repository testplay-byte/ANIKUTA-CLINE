package app.confused.anikuta.core.backup.di

import app.confused.anikuta.core.backup.AutoBackupScheduler
import app.confused.anikuta.core.backup.BackupManager
import app.confused.anikuta.core.backup.BackupPreferences
import app.confused.anikuta.core.backup.BackupProvider
import app.confused.anikuta.core.backup.BackupStorage
import app.confused.anikuta.core.backup.provider.AnimeDetailsBackupProvider
import app.confused.anikuta.core.backup.provider.CategoryBackupProvider
import app.confused.anikuta.core.backup.provider.CoverDownloader
import app.confused.anikuta.core.backup.provider.CoverImageProvider
import app.confused.anikuta.core.backup.provider.EpisodeBackupProvider
import app.confused.anikuta.core.backup.provider.EpisodeMetadataBackupProvider
import app.confused.anikuta.core.backup.provider.LibraryBackupProvider
import app.confused.anikuta.core.backup.provider.PreferencesBackupProvider
import app.confused.anikuta.core.backup.provider.SourceLinkBackupProvider
import app.confused.anikuta.core.backup.provider.TrackerBackupProviderAdapter
import app.confused.anikuta.core.backup.provider.WatchProgressBackupProvider
import app.confused.anikuta.core.database.AnikutaDatabase
import app.confused.anikuta.core.episodemetadata.repository.EpisodeMetadataCache
import app.confused.anikuta.core.player.WatchProgressStore
import app.confused.anikuta.core.preferences.PreferenceStore
import app.confused.anikuta.core.tracker.TrackerBackupProvider
import app.confused.anikuta.data.extension.cache.ExtensionLinkStore
import app.confused.anikuta.data.extension.cache.SourceLinkStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for the backup engine (`:core:backup`).
 *
 * Registers:
 * - All 10 [BackupProvider] implementations (each backed by its data source).
 * - [CoverDownloader] — HTTP cover image downloader.
 * - [BackupManager] — the orchestrator (gets providers via `getAll<BackupProvider>()`).
 * - [BackupPreferences] — auto-backup config + SAF folder URI.
 * - [BackupStorage] — SAF folder/file management.
 * - [AutoBackupScheduler] — WorkManager periodic work scheduler.
 *
 * Must be added to `modules(...)` in `App.kt`'s `startKoin`.
 *
 * **Adding a new provider:**
 * 1. Create the provider class.
 * 2. Register it here with `single<BackupProvider> { ... }` (Koin collects all
 *    `BackupProvider` bindings into a list for [BackupManager]).
 */
val backupModule: Module = module {
    // ── Backup providers (each registered as BackupProvider so Koin collects them) ──
    single<BackupProvider> { LibraryBackupProvider(get<AnikutaDatabase>()) }
    single<BackupProvider> { AnimeDetailsBackupProvider(get<AnikutaDatabase>()) }
    single<BackupProvider> { EpisodeBackupProvider(get<AnikutaDatabase>()) }
    single<BackupProvider> { CategoryBackupProvider(get<AnikutaDatabase>()) }
    single<BackupProvider> { EpisodeMetadataBackupProvider(get<EpisodeMetadataCache>()) }
    single<BackupProvider> { WatchProgressBackupProvider(get<WatchProgressStore>()) }
    single<BackupProvider> { SourceLinkBackupProvider(get<SourceLinkStore>(), get<ExtensionLinkStore>()) }
    single<BackupProvider> { TrackerBackupProviderAdapter(get<TrackerBackupProvider>()) }
    single<BackupProvider> { PreferencesBackupProvider(get<PreferenceStore>()) }
    single<BackupProvider> { CoverImageProvider(get<AnikutaDatabase>()) }

    // ── Cover downloader ──
    single { CoverDownloader() }

    // ── Orchestrator (receives all providers via Koin getAll) ──
    single {
        BackupManager(
            providers = getAll<BackupProvider>(),
            coverDownloader = get<CoverDownloader>(),
        )
    }

    // ── Preferences + storage ──
    single { BackupPreferences(get<PreferenceStore>()) }
    single { BackupStorage(androidContext(), get<BackupPreferences>()) }
    single { AutoBackupScheduler(androidContext()) }
}
