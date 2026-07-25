package app.confused.anikuta.feature.backup

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.confused.anikuta.core.backup.AutoBackupFrequency
import app.confused.anikuta.core.backup.AutoBackupScheduler
import app.confused.anikuta.core.backup.BackupCategory
import app.confused.anikuta.core.backup.BackupManager
import app.confused.anikuta.core.backup.BackupOptions
import app.confused.anikuta.core.backup.BackupPreferences
import app.confused.anikuta.core.backup.BackupResult
import app.confused.anikuta.core.backup.BackupStorage
import app.confused.anikuta.core.backup.CreateSummary
import app.confused.anikuta.core.backup.RestoreSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "BackupViewModel"

/**
 * UI state for the Backup & Restore screen.
 */
sealed class BackupUiState {
    /** Idle — no operation in progress. */
    object Idle : BackupUiState()

    /** Backup creation in progress. */
    data class Creating(val message: String = "Creating backup…") : BackupUiState()

    /** Backup created successfully. */
    data class Created(val summary: CreateSummary) : BackupUiState()

    /** Reading a selected backup file (detecting format + summary). */
    data class ReadingFile(val fileName: String) : BackupUiState()

    /** Restore summary loaded — waiting for user confirmation. */
    data class RestorePending(val summary: RestoreSummary, val fileUri: Uri) : BackupUiState()

    /** Restore in progress. */
    data class Restoring(val message: String = "Restoring…") : BackupUiState()

    /** Restore completed. */
    data class Restored(val summary: RestoreSummary) : BackupUiState()

    /** Error state with a user-facing message. */
    data class Error(val message: String, val recoverable: Boolean = true) : BackupUiState()
}

/**
 * ViewModel for the Backup & Restore settings screen.
 *
 * Manages:
 * - Manual backup category selection + creation.
 * - Restore: file selection → format detection → summary → confirm → execute.
 * - Auto-backup: enable/disable, frequency, category selection.
 * - Storage: SAF folder selection + usage display.
 *
 * All backup operations are delegated to [BackupManager] (engine in `:core:backup`).
 * UI state is exposed via [state] (a sealed class) so the screen can render
 * loading/success/error states cleanly.
 */
class BackupViewModel(
    private val backupManager: BackupManager,
    private val backupStorage: BackupStorage,
    private val backupPreferences: BackupPreferences,
    private val autoBackupScheduler: AutoBackupScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    // Manual backup category selection
    private val _manualCategories = MutableStateFlow(BackupCategory.defaultSelection.toMutableSet())
    val manualCategories: StateFlow<Set<String>> = _manualCategories.asStateFlow()

    // Auto-backup category selection (separate from manual)
    private val _autoCategories = MutableStateFlow(backupPreferences.autoCategories.get().toMutableSet())
    val autoCategories: StateFlow<Set<String>> = _autoCategories.asStateFlow()

    // Auto-backup enabled + frequency
    private val _autoEnabled = MutableStateFlow(backupPreferences.autoEnabled.get())
    val autoEnabled: StateFlow<Boolean> = _autoEnabled.asStateFlow()

    private val _autoFrequency = MutableStateFlow(AutoBackupFrequency.fromName(backupPreferences.autoFrequency.get()))
    val autoFrequency: StateFlow<AutoBackupFrequency> = _autoFrequency.asStateFlow()

    // Storage
    private val _folderUri = MutableStateFlow(backupPreferences.folderUri.get())
    val folderUri: StateFlow<String> = _folderUri.asStateFlow()

    private val _storageUsage = MutableStateFlow(0L)
    val storageUsage: StateFlow<Long> = _storageUsage.asStateFlow()

    init {
        refreshStorageUsage()
    }

    // ── Manual backup ──

    fun toggleManualCategory(categoryId: String) {
        _manualCategories.update { current ->
            current.toMutableSet().apply {
                if (contains(categoryId)) remove(categoryId) else add(categoryId)
            }
        }
    }

    fun createBackup() {
        if (_manualCategories.value.isEmpty()) {
            _state.value = BackupUiState.Error("Select at least one category to back up.")
            return
        }
        if (!backupStorage.hasFolder()) {
            _state.value = BackupUiState.Error("No backup folder selected. Choose a folder in the Storage section below.", recoverable = true)
            return
        }
        viewModelScope.launch {
            _state.value = BackupUiState.Creating()
            try {
                val fileName = backupStorage.generateBackupName(isAuto = false)
                val output = backupStorage.createManualBackupFile(fileName)
                if (output == null) {
                    _state.value = BackupUiState.Error("Failed to create backup file. Check folder permissions.")
                    return@launch
                }
                output.use { stream ->
                    val options = BackupOptions(categories = _manualCategories.value)
                    when (val result = backupManager.createBackup(options, stream)) {
                        is BackupResult.Success -> {
                            backupPreferences.lastManualBackup.set(System.currentTimeMillis())
                            _state.value = BackupUiState.Created(result.data.copy(
                                filePath = fileName,
                            ))
                            refreshStorageUsage()
                        }
                        is BackupResult.Error -> {
                            _state.value = BackupUiState.Error(result.message, result.recoverable)
                        }
                        is BackupResult.InProgress -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBackup failed", e)
                _state.value = BackupUiState.Error("Backup failed: ${e.message}")
            }
        }
    }

    // ── Restore ──

    fun onSelectBackupFile(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupUiState.ReadingFile(uri.lastPathSegment ?: "backup file")
            try {
                val input = backupStorage.openInput(uri)
                if (input == null) {
                    _state.value = BackupUiState.Error("Cannot open the selected file.")
                    return@launch
                }
                input.use { stream ->
                    when (val result = backupManager.readSummary(stream)) {
                        is BackupResult.Success -> {
                            _state.value = BackupUiState.RestorePending(result.data, uri)
                        }
                        is BackupResult.Error -> {
                            _state.value = BackupUiState.Error(result.message, result.recoverable)
                        }
                        is BackupResult.InProgress -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onSelectBackupFile failed", e)
                _state.value = BackupUiState.Error("Failed to read backup: ${e.message}")
            }
        }
    }

    fun confirmRestore(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupUiState.Restoring()
            try {
                val input = backupStorage.openInput(uri)
                if (input == null) {
                    _state.value = BackupUiState.Error("Cannot open the backup file.")
                    return@launch
                }
                input.use { stream ->
                    when (val result = backupManager.restoreBackup(stream)) {
                        is BackupResult.Success -> {
                            _state.value = BackupUiState.Restored(result.data)
                        }
                        is BackupResult.Error -> {
                            _state.value = BackupUiState.Error(result.message, result.recoverable)
                        }
                        is BackupResult.InProgress -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "confirmRestore failed", e)
                _state.value = BackupUiState.Error("Restore failed: ${e.message}")
            }
        }
    }

    fun dismissState() {
        _state.value = BackupUiState.Idle
    }

    // ── Auto-backup ──

    fun toggleAutoEnabled(enabled: Boolean) {
        _autoEnabled.value = enabled
        backupPreferences.autoEnabled.set(enabled)
        autoBackupScheduler.reschedule(enabled, _autoFrequency.value)
    }

    fun setAutoFrequency(frequency: AutoBackupFrequency) {
        _autoFrequency.value = frequency
        backupPreferences.autoFrequency.set(frequency.name)
        if (_autoEnabled.value) {
            autoBackupScheduler.reschedule(true, frequency)
        }
    }

    fun toggleAutoCategory(categoryId: String) {
        _autoCategories.update { current ->
            current.toMutableSet().apply {
                if (contains(categoryId)) remove(categoryId) else add(categoryId)
            }
        }
        backupPreferences.autoCategories.set(_autoCategories.value)
    }

    // ── Storage ──

    fun setFolder(uri: Uri) {
        if (backupStorage.setFolderUri(uri)) {
            _folderUri.value = uri.toString()
            refreshStorageUsage()
        } else {
            _state.value = BackupUiState.Error("Failed to set backup folder. Try a different location.")
        }
    }

    fun refreshStorageUsage() {
        _storageUsage.value = backupStorage.getStorageUsage()
    }

    /** All backup categories (for the checkbox lists). */
    val categories: List<BackupCategory> = BackupCategory.entries
}
