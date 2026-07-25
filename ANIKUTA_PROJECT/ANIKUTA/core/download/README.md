# app.confused.anikuta.core.download

The download engine — a modular, future-proof system for downloading anime
episodes for offline playback.

**Module path:** `core/download`
**Type:** Android library (no Compose — pure logic + SAF I/O + notifications)
**Status:** Implemented (DEFAULT method). 1DM method is interface-ready (ADR-020).

## What it does

- Downloads episodes (video + ALL subtitles + metadata) to a user-selected
  SAF folder in an AniList-first structure.
- Manages a queue with active/pending/paused/completed/error states and a
  configurable concurrency limit.
- Persists the queue across app restarts.
- Posts Android notifications (progress, completion, error).
- Exposes offline-playback queries (is the episode on disk? give me its URI).

## Architecture

```
DownloadManager (interface)          ← pluggable contract (DEFAULT + future 1DM)
└── DefaultDownloadManager           ← wires everything; implements the interface
    ├── DownloadQueue                ← state machine + Semaphore concurrency
    │   └── HttpDownloader           ← OkHttp streaming download + subtitles
    ├── DownloadStore                ← persists the queue (PreferenceStore JSON)
    ├── DownloadStorageProvider      ← SAF folder structure (AniList-first)
    └── DownloadNotificationManager  ← Android notifications
```

### Key design decisions

1. **Resolved-video input (not source/episode).** `enqueueDownload` takes a
   `DownloadRequest` (already-resolved video URL + headers + subtitle tracks),
   NOT a `(Anime, SEpisode, AnimeSource)`. This respects module boundaries:
   `:core:download` cannot import `:feature:video-resolver` (Rule §14).
   Resolution is orchestrated by `:app`'s `DownloadOrchestrator`.

2. **AniList-first folder structure.** `<root>/ANIKUTA/downloads/anime/
   <Anime Title [anilistId]>/Episode NNN/video.<ext>` + `data/subtitles/` +
   `data/metadata.json` (per FOLDER-STRUCTURE-PLAN.md).

3. **SAF (DocumentFile), not java.io.File.** The user picks the folder; we use
   content:// URIs throughout. MPV plays these via `resolveUrlForMpv`
   (`fd://` / real-path) — offline playback needs no file copying.

4. **Pluggable method.** `DownloadPreferences.method()` selects DEFAULT (now)
   or ONEDM (future). Swapping is a Koin binding change in `DownloadModule`.

5. **Wi-Fi-only aware.** The connectivity check honours the pref; tasks stay
   QUEUED when off-Wi-Fi (if the pref is on).

## Public API

See `DownloadManager.kt` for the full interface. Key entry points:
- `enqueueDownload(DownloadRequest): Long`
- `pauseDownload / resumeDownload / cancelDownload / retryDownload / deleteDownload`
- `isEpisodeDownloaded(anilistId, episodeUrl)`
- `getDownloadedVideoUri(...)` / `getDownloadedSubtitleUris(...)` (for MPV)
- `activeDownloads` / `completedDownloads` / `allDownloads` (Flows)

## Logging

All logs route through `DownloadLogger` (tag `AnikutaDownload`).
`adb logcat -s AnikutaDownload` shows the full download subsystem.

## Dependencies

- `:core:preferences` (PreferenceStore), `:core:source-api` (SEpisode/Track + OkHttp)
- `androidx.documentfile` (SAF), `okhttp`, `kotlinx-serialization-json`,
  `kotlinx-coroutines`, Koin.
