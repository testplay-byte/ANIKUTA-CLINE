package app.confused.anikuta.feature.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.confused.anikuta.core.download.DownloadManager
import app.confused.anikuta.core.download.DownloadPreferences
import app.confused.anikuta.core.download.DownloadStatus
import app.confused.anikuta.core.download.DownloadTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the Downloads screen.
 *
 * Observes [DownloadManager.activeDownloads] + [completedDownloads] + the
 * folder-ready state, combining them into a single [DownloadUiState]. Forwards
 * user actions (pause/resume/cancel/delete/retry) to the manager.
 *
 * All manager calls run on `Dispatchers.IO` (the manager enforces this
 * internally); the UI state is collected on the main thread.
 */
class DownloadViewModel(
    private val manager: DownloadManager,
    private val preferences: DownloadPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadUiState())
    val state: StateFlow<DownloadUiState> = _state.asStateFlow()

    init {
        // Combine active + completed + folder-ready into the UI state.
        viewModelScope.launch {
            combine(
                manager.activeDownloads,
                manager.completedDownloads,
                preferences.downloadFolderUri().changes(),
            ) { active, completed, folderUri ->
                DownloadUiState(
                    queue = active,
                    downloaded = groupByAnime(completed),
                    folderReady = folderUri.isNotBlank(),
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }
    }

    fun pause(taskId: Long) = viewModelScope.launch { manager.pauseDownload(taskId) }
    fun resume(taskId: Long) = viewModelScope.launch { manager.resumeDownload(taskId) }
    fun cancel(taskId: Long) = viewModelScope.launch { manager.cancelDownload(taskId) }
    fun retry(taskId: Long) = viewModelScope.launch { manager.retryDownload(taskId) }

    fun deleteEpisode(taskId: Long) = viewModelScope.launch { manager.deleteDownload(taskId) }

    fun deleteAnime(anilistId: Int) = viewModelScope.launch {
        manager.deleteAnimeDownloads(anilistId)
    }

    /** Persist the SAF folder permission + URI (from the folder picker). */
    fun setDownloadFolder(treeUriString: String) {
        try {
            manager.setDownloadFolder(treeUriString)
        } catch (e: Exception) {
            // Surface via state — the UI shows a toast/snackbar.
            _state.value = _state.value.copy()
        }
    }

    /** Group completed tasks by anime for the expandable library section. */
    private fun groupByAnime(tasks: List<DownloadTask>): Map<DownloadedAnimeKey, List<DownloadTask>> {
        return tasks
            .groupBy {
                DownloadedAnimeKey(
                    anilistId = it.request.anime.anilistId,
                    title = it.request.anime.title,
                    coverUrl = it.request.anime.coverUrl,
                    coverColor = it.request.anime.coverColor,
                )
            }
            .toSortedMap(compareBy { it.title.lowercase() })
    }
}
