package app.confused.anikuta.core.download

import app.confused.anikuta.core.preferences.Preference
import app.confused.anikuta.core.preferences.PreferenceStore
import app.confused.anikuta.core.preferences.getEnum

/**
 * Download settings, persisted via [PreferenceStore] (reactive — observe via
 * `Preference.changes()`). Mirrors the `WatchProgressStore` pattern.
 *
 * Settings (per DOWNLOADS-PLAN + the implementation prompt):
 *  - [downloadFolderUri] — the SAF tree URI the user picked (content://...).
 *    Empty until the user selects a folder via the preferences sheet.
 *  - [method] — DEFAULT (OkHttp) now; ONEDM (multi-threaded) future.
 *  - [wifiOnly] — only download on Wi-Fi (default true, per ADR-020 battery note).
 *  - [concurrentDownloads] — max parallel downloads (default 3).
 *  - [autoDownloadNewEpisodes] — global auto-download toggle (future; per-series
 *    override is a follow-up). Off by default.
 *  - [showDownloadButton] — whether episode rows show the download button.
 *
 * The folder URI is the ONLY setting the user MUST set before downloading;
 * the manager checks [hasDownloadFolder] and surfaces a clear error + UI hint
 * when it's unset (explicit error handling — Rule §10).
 */
class DownloadPreferences(
    private val store: PreferenceStore,
) {

    /** The SAF tree URI (`content://...`) of the user-selected ANIKUTA root folder. */
    fun downloadFolderUri(): Preference<String> =
        store.getString(KEY_FOLDER_URI, "")

    /** True once the user has picked a download folder. */
    val hasDownloadFolder: Boolean
        get() = downloadFolderUri().get().isNotBlank()

    /** The download method (DEFAULT now; ONEDM future — interface-ready). */
    fun method(): Preference<DownloadMethod> =
        store.getEnum(KEY_METHOD, DownloadMethod.DEFAULT)

    /** Only download on Wi-Fi (default true). */
    fun wifiOnly(): Preference<Boolean> =
        store.getBoolean(KEY_WIFI_ONLY, true)

    /** Max parallel downloads (default 3; 1..5 clamped at the UI layer). */
    fun concurrentDownloads(): Preference<Int> =
        store.getInt(KEY_CONCURRENT, 3)

    /** Global auto-download toggle (future; off by default). */
    fun autoDownloadNewEpisodes(): Preference<Boolean> =
        store.getBoolean(KEY_AUTO_DOWNLOAD, false)

    /** Show the download button on episode rows (default true). */
    fun showDownloadButton(): Preference<Boolean> =
        store.getBoolean(KEY_SHOW_BUTTON, true)

    companion object {
        private const val KEY_FOLDER_URI = "pref_dl_folder_uri"
        private const val KEY_METHOD = "pref_dl_method"
        private const val KEY_WIFI_ONLY = "pref_dl_wifi_only"
        private const val KEY_CONCURRENT = "pref_dl_concurrent"
        private const val KEY_AUTO_DOWNLOAD = "pref_dl_auto_new"
        private const val KEY_SHOW_BUTTON = "pref_dl_show_button"
    }
}

/** The download method (per DOWNLOADS-PLAN — two methods, user-selectable). */
enum class DownloadMethod {
    /** Standard single-threaded OkHttp download (the working MVP method). */
    DEFAULT,

    /** Future: multi-threaded, resume-capable, 1DM-style. NOT implemented yet. */
    ONEDM,
}
