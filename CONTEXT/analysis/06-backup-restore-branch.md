# ANIKUTA — Backup & Restore Branch Analysis (`feature/backup-restore`)

> Analysis of the `feature/backup-restore` branch. Adds a comprehensive Backup & Restore subsystem.

## Branch Info
- **Branch**: `feature/backup-restore` (tracks `origin/feature/backup-restore`)
- **Commits**: 5 commits (1 feature + 4 fixes)
- **Key commit**: `f7b95a4` — feat: comprehensive Backup & Restore

---

## Module Structure

The Backup & Restore subsystem spans two Gradle modules:

### `:core:backup` — Backup/Restore Engine (format-agnostic, modular)
All files under `core/backup/src/main/java/app/confused/anikuta/core/backup/`:

| File | Purpose |
|------|---------|
| `BackupManager.kt` | Central orchestrator — creates and restores backups, delegates to providers + formats |
| `BackupProvider.kt` | Interface — each data source implements `export()` and `import()` |
| `BackupEntry.kt` | Sealed class — 10 subclasses, one per data type (polymorphic serialization) |
| `BackupCategory.kt` | Enum — 10 user-selectable categories with stable IDs, display names, defaults |
| `BackupFormat.kt` | Interface — pluggable file format contract (write, read, readCovers, detect) |
| `BackupFormatType.kt` | Enum — ANIKUTA (`.anikuta`) and ANIYOMI (`.tachibk`) format types |
| `BackupOptions.kt` | Data class — controls which categories to include + output format |
| `BackupResult.kt` | Sealed class — Success/Error/InProgress + progress tracking |
| `AutoBackupScheduler.kt` | Schedules automatic backups via WorkManager |
| `AutoBackupWorker.kt` | Worker that executes scheduled auto-backups |
| `providers/` | Backup provider implementations (one per data type) |
| `formats/` | Format implementations (ANIKUTA JSON, ANIYOMI compat) |

### `:feature:backup` — UI Layer (settings screen, ViewModel, components)
| File | Purpose |
|------|---------|
| `BackupSettingsScreen.kt` | Settings screen for backup configuration |
| `BackupViewModel.kt` | ViewModel managing backup/restore state |
| `RestoreConfirmSheet.kt` | Bottom sheet for restore confirmation (experimental API opt-in) |
| Components | UI components for backup category selection, progress display |

---

## Backup Architecture

### Backup Creation Flow
1. User selects categories via `BackupOptions`
2. `BackupManager` iterates enabled `BackupProvider` implementations
3. Each provider exports its data as a `BackupEntry` subclass
4. `BackupFormat` serializes entries to file (ANIKUTA `.anikuta` or ANIYOMI `.tachibk`)
5. `BackupResult` tracks success/error/progress

### Backup Providers (10 data types)
Each `BackupProvider` implementation handles one data type:
- Anime metadata
- Categories
- History
- Trackers
- Episode metadata
- Extensions
- Source links
- Preferences
- *(+ others)*

### Backup Formats
- **ANIKUTA format** (`.anikuta`): Custom JSON format with polymorphic serialization
- **ANIYOMI format** (`.tachibk`): Compatibility format for Aniyomi backups

### Auto-Backup Scheduling
- Uses **WorkManager** for scheduled background backups
- `AutoBackupScheduler` configures periodic backup intervals
- `AutoBackupWorker` executes the backup in background

### Restore Flow
1. User selects backup file
2. `BackupFormat.detect()` identifies format
3. `BackupFormat.read()` parses entries
4. `RestoreConfirmSheet` shows confirmation (experimental API opt-in)
5. `BackupManager` delegates to providers' `import()` methods
6. Smart cast handling for restore return types

---

## Integration Points

### `App.kt`
- Backup module DI initialization

### `MainActivity.kt`
- Navigation state for backup settings screen
- Backup/restore UI entry points

### DI Modules
- Koin module for backup providers and `BackupManager`

---

## Key Fixes Applied
1. `BackupEntry @Transient` — serialization fix
2. Format return types — corrected
3. Query names — fixed database query names
4. Metadata model — corrected
5. `SourceLinkStore.getAll` — fixed
6. `TrackerBackupProviderImpl` restore return type (Unit not withContext result)
7. `BackupManager.countItems` for EpisodeMetadata + remaining anime_categoryQueries
8. `RestoreConfirmSheet` experimental API opt-in + smart cast note

---

## Design Decisions
- **Format-agnostic**: Pluggable `BackupFormat` interface supports both ANIKUTA and ANIYOMI formats
- **Modular providers**: Each data type has its own `BackupProvider` — easy to add new ones
- **Polymorphic serialization**: `BackupEntry` sealed class with 10 subclasses
- **User-selectable categories**: `BackupCategory` enum with 10 categories, stable IDs, defaults
- **WorkManager for auto-backup**: Reliable background scheduling