# Folder Structure Proposal

## Current Aniyomi Structure (problems)
```
/data/data/app.anikuta/
├── auto_backup/          ← good
├── downloads/            ← shows per-extension folders (not AniList-first)
│   ├── extension1/
│   │   ├── anime1/
│   │   └── anime2/
│   └── extension2/
├── local/                ← redundant split
│   └── local anime/      ← redundant
├── mpvconfig/            ← good
```

Problems:
- Downloads organized by extension, not by anime (we're AniList-first)
- `local` and `local anime` are redundant
- Not easy to manage or navigate

## Proposed ANIKUTA Structure
```
/data/data/app.anikuta/
├── auto_backup/                  ← automatic backup files
├── downloads/                    ← all downloaded content
│   ├── anime/                    ← anime downloads (AniList-first)
│   │   ├── Anime Title [anilistId]/
│   │   │   ├── Episode 001/
│   │   │   │   ├── video.mp4        ← the actual episode file
│   │   │   │   └── data/            ← episode-specific data
│   │   │   │       ├── subtitles/   ← subtitle files (.ass, .srt)
│   │   │   │       └── metadata.json← episode metadata cache
│   │   │   ├── Episode 002/
│   │   │   └── ...
│   │   └── Another Anime [12345]/
│   └── manga/                    ← manga downloads (future)
│       └── (same structure, future)
├── local/                        ← combined local source (anime + manga)
│   ├── anime/                    ← local anime files (user's own)
│   └── manga/                    ← local manga files (future)
├── mpvconfig/                    ← MPV player configuration
│   ├── subfont.ttf
│   └── ...
├── backups/                      ← manual backup files (user-created)
└── cache/                        ← image cache, temporary files
```

## Folder Naming Convention

### Anime Folder: `Anime Title [anilistId]`
- Title is always English (from AniList `title.english` or `title.romaji`)
- AniList ID in square brackets at the end
- Example: `Jujutsu Kaisen [101522]`

### Episode Folder: `Episode NNN`
- Zero-padded 3-digit episode number
- Example: `Episode 001`, `Episode 002`, `Episode 012`

### Episode File: `video.mp4` (or original format)
- Simple name, the folder context provides the anime + episode info
- If multiple video versions exist (different quality), use: `video_1080p.mp4`, `video_720p.mp4`

### Episode Data Subfolder: `data/`
- `subtitles/` — external subtitle files (.ass, .srt)
- `metadata.json` — cached episode metadata (title, description, air date, thumbnail URL)

## Benefits
1. **AniList-first** — anime organized by AniList ID, not by extension
2. **Human-readable** — users can browse the folder and understand what's there
3. **Easy backup** — the `downloads/anime/` folder is self-contained
4. **Easy to extend** — manga folder is ready for future
5. **No redundancy** — `local` is a single folder (not split into `local` + `local anime`)
6. **Metadata co-located** — each episode folder has its own metadata + subtitles
