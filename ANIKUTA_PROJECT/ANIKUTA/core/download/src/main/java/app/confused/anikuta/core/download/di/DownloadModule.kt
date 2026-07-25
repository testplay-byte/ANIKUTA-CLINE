package app.confused.anikuta.core.download.di

import android.content.Context
import app.confused.anikuta.core.download.DefaultDownloadManager
import app.confused.anikuta.core.download.DownloadManager
import app.confused.anikuta.core.download.DownloadPreferences
import app.confused.anikuta.core.download.DownloadStore
import app.confused.anikuta.core.preferences.PreferenceStore
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

/**
 * Koin DI module for the download engine (`:core:download`).
 *
 * Registers:
 *  - [DownloadPreferences] — backed by the shared [PreferenceStore].
 *  - [DownloadStore] — backed by the SAME [PreferenceStore] (so the queue's
 *    persisted state lives alongside other prefs).
 *  - A download-dedicated [OkHttpClient] (qualifier `"download"`) — long
 *    timeouts for large files, separate from the extension NetworkHelper
 *    client so a stuck download can't starve extension HTTP calls.
 *  - [DownloadManager] → [DefaultDownloadManager] (single binding; swapping to
 *    a future `OneDmDownloadManager` is a one-line change here, gated on the
 *    `DownloadPreferences.method()` pref — ADR-020 future-proofing).
 *
 * Added to `App.kt`'s `startKoin { modules(...) }` list.
 */
val downloadModule: Module = module {
    single { DownloadPreferences(get<PreferenceStore>()) }
    single { DownloadStore(get<PreferenceStore>()) }

    single(named("download")) {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    single<DownloadManager> {
        DefaultDownloadManager(
            context = get<Context>(),
            okHttp = get(named("download")),
            preferences = get(),
            store = get(),
        )
    }
}
