# ANIKUTA — Downloads Branch Analysis (`feature/downloads`)

> Analysis of the `feature/downloads` branch. Adds a Downloads & Offline Playback subsystem with ADR-035.

## Branch Info
- **Branch**: `feature/downloads` (tracks `origin/feature/downloads`)
- **Commits**: 4 commits (1 feature + 1 docs + 2 CI fixes)
- **Files changed**: 39 files (+3,335/-9 lines)
- **Key commit**: `a24934c` — feat: Downloads & Offline Playback (Agent 2)

### Commit History
1. `a24934c` — `feat: Downloads & Offline Playback (Agent 2)` — main implementation
2. `b08bde4` — `docs: ARCHITECTURE.md + ADR-035 for Downloads subsystem`
3. `9121c3a` — `fix: queue.tasks is already StateFlow — drop asStateFlow() (CI fix #1)`
4. `35ff551` — `fix: import Icons.Filled.Download extension property (CI fix #2)`

---

## Module Structure

### `:core:download` — Download Engine (no Compose)

| File | Purpose |
|------|---------|
| `core/download/README.md` | Module overview: architecture diagram, design decisions, public API, logging, dependencies |
| `core/download/build.gradle.kts` | Android library plugin (`anikuta.library` + serialization). Deps: `:core:preferences`, `:core:source-api`, OkHttp, DocumentFile, kotlinx-serialization, coroutines, Koin |
| `core/download/src/main/AndroidManifest.xml` | Manifest for download module |
| `DownloadManager.kt` | Central download orchestrator |
| `DownloadJob.kt` | Represents a single download job |
| `DownloadQueue.kt` | Queue management (StateFlow-based) |
| `DownloadTask.kt` | Individual download task |
| `DownloadState.kt` | State tracking (queued, downloading, completed, error) |
| `DownloadPreferences.kt` | User preferences for downloads |
| `DownloadStorage.kt` | Storage management (DocumentFile-based) |

### `:feature:download` — UI Layer (download queue screen, ViewModel)
| File | Purpose |
|------|---------|
| `DownloadQueueScreen.kt` | Download queue management screen |
| `DownloadViewModel.kt` | ViewModel managing download state |
| Components | UI components for download progress, status |

---

## Downloads Architecture

### Download Queue Management
- **StateFlow-based**: `queue.tasks` is already a `StateFlow` (CI fix #1 removed unnecessary `asStateFlow()`)
- Downloads are queued and executed sequentially or concurrently
- State tracking: queued → downloading → completed/error

### Download Flow
1. User initiates download from anime details
2. `DownloadManager` creates a `DownloadJob`
3. Job added to `DownloadQueue` (StateFlow-based)
4. `DownloadTask` executes the actual download via source API
5. `DownloadStorage` saves to user-selected location (DocumentFile/SAF)
6. State updates flow through StateFlow to UI

### Offline Playback Integration
- Downloaded episodes stored via `DownloadStorage` (DocumentFile-based)
- Watch screen checks for local downloads before streaming
- Integration with MPV player for offline playback

---

## ADR-035: Downloads Subsystem

Added to `ARCHITECTURE.md` on the downloads branch:
- Documents the downloads architecture
- Defines the `:core:download` and `:feature:download` modules
- Specifies StateFlow-based queue management
- Documents storage approach (DocumentFile/SAF)

---

## Integration Points

### `App.kt`
- Download module DI initialization

### `MainActivity.kt`
- Navigation state for download queue screen
- Download UI entry points

### DI Modules
- Koin module for `DownloadManager`, `DownloadQueue`, `DownloadPreferences`

### Dependencies
- `:core:preferences` — download settings
- `:core:source-api` — source contract for fetching episodes
- OkHttp — HTTP client for downloads
- DocumentFile — Storage Access Framework for file storage
- kotlinx-serialization — serializing download metadata
- Coroutines — async download operations
- Koin — dependency injection

---

## CI Fixes Applied
1. **CI fix #1** (`9121c3a`): `queue.tasks` is already `StateFlow` — removed unnecessary `asStateFlow()` call
2. **CI fix #2** (`35ff551`): Import `Icons.Filled.Download` extension property — fixed missing import

---

## Design Decisions
- **StateFlow-based queue**: Reactive queue management (not callback-based)
- **DocumentFile/SAF storage**: User-selected storage location (not hardcoded paths)
- **Separate core/feature modules**: Download engine in `:core:download`, UI in `:feature:download`
- **OkHttp for downloads**: Reliable HTTP client with progress tracking
- **Coroutine-based**: Async download operations with structured concurrency

---

## AI Agent Prompts (on `main` branch)

The `AI-AGENT-PROMPTS/` folder (added to `main`) contains prompts used to guide the downloads feature development:
- `AGENT2-DOWNLOADS-SETUP.md` — Setup prompt for downloads agent
- `AGENT2-DOWNLOADS-IMPLEMENTATION.md` — Implementation prompt for downloads agent

These describe the intended architecture and implementation steps for the downloads subsystem.