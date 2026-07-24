# Downloads / Offline Playback — Requirements & Plan

## User Requirements

### Download Methods (two, user-selectable)
1. **Default method (Aniyomi-style)** — standard HTTP download. Simple, reliable, the default.
2. **1DM-style method (future)** — multi-threaded downloading with resume capability, faster speeds, better experience. To be added AFTER the default method works properly.

### Architecture
- Build the default method first with a modular architecture so the 1DM method can be added later without rewriting
- `:core:download` — the download engine (DownloadManager, queue, progress, file I/O)
- `:feature:downloads` (or `:feature:download`) — the UI (download queue, downloaded anime list)

### Download Flow
1. User taps a download button on an episode row (in the details page or watch page)
2. App resolves the video URL (same flow as watching)
3. App downloads the video file to the folder structure (see FOLDER-STRUCTURE-PLAN.md)
4. Download progress shown in a notification + a downloads queue screen
5. When complete, the episode is available for offline playback
6. In the watch page, if a downloaded copy exists, play the local file instead of streaming

### Folder Structure for Downloads
See `FOLDER-STRUCTURE-PLAN.md` in this folder.

### UI Requirements
- Download button on episode rows (the `pref_ep_show_download_button` pref already exists)
- Download progress indicator on the episode row (progress bar or percentage)
- Downloads screen (from More or bottom nav) showing:
  - Queue (active + pending downloads)
  - Downloaded anime (grouped by anime, showing downloaded episodes)
- Delete downloaded episode / anime option

### Future-Proofing
- The `DownloadManager` should be an interface with a `DefaultDownloadManager` implementation
- A future `OneDmDownloadManager` can implement the same interface
- User selects the download method in settings
