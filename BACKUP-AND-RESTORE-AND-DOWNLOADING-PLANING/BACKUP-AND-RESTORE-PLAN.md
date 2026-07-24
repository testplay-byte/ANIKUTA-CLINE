# Backup & Restore — Requirements & Plan

## User Requirements

### Core Backup System
- Custom backup format (ANIKUTA's own, NOT just Aniyomi's)
- Also compatible with Aniyomi backup format (can restore from Aniyomi backups)
- User selects what data to back up (granular checkboxes)
- Two export formats: ANIKUTA format (recommended) + Aniyomi format (compatibility)

### Data to Back Up (user-selectable)
1. **Library anime** — all anime in the user's library (with cover images optional)
2. **Cover images** — option to back up cover images of all library anime
3. **Details page data** — full anime details (synopsis, genres, scores, etc.)
4. **Episodes list metadata** — thumbnails, titles, release dates, summaries per episode
5. **Watch progress / history** — WatchProgressStore data (position, duration, timestamps)
6. **AniList-extension links** — ExtensionLinkStore data (which AniList anime → which extension source)
7. **Tracker tokens + bindings** — OAuth tokens (AniList + MAL) + animetrack table
8. **Categories** — user's custom categories + anime-category links
9. **Preferences** — all app preferences (display settings, episode settings, etc.)

### Restore Flow
1. User selects a backup file
2. App processes the file, determines the format (ANIKUTA vs Aniyomi)
3. App parses the anime entries
4. App processes each episode individually
5. App shows a summary of what will be restored
6. User confirms → restore executes

### Architecture Requirements
- Multiple modules, properly documented
- Highly customizable (easy to add new data types to backup/restore later)
- Clean separation between backup format, data providers, and UI
- `:core:backup` — the backup/restore engine (format-agnostic)
- `:feature:backup` — the UI (create backup, restore backup, auto-backup settings)

### Backup Providers (already documented by prior agents)
- `HistoryBackupProvider` — from Agent 1 (History page)
- `TrackerBackupProvider` — from Agent 2 (Trackers)
- Need to create: `LibraryBackupProvider`, `AnimeDetailsBackupProvider`, `EpisodeMetadataBackupProvider`, `ExtensionLinkBackupProvider`, `PreferencesBackupProvider`, `CategoryBackupProvider`

### Folder Structure (for backup files)
See `FOLDER-STRUCTURE-PLAN.md` in this folder.
