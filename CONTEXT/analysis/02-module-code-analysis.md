# ANIKUTA — Module & Code Analysis

> Detailed analysis of the live Android code at `ANIKUTA_PROJECT/ANIKUTA/`.

## Project Identity

- **Application ID**: `app.confused.anikuta`
- **Version**: 0.1.0 (versionCode 1)
- **Package convention**: `app.confused.anikuta.*`
- **Location**: `ANIKUTA_PROJECT/ANIKUTA/`

---

## Complete Module Tree

### `:app` — Application Module
| File | Purpose |
|------|---------|
| `App.kt` | Application class — Koin + Injekt DI setup, crash handler, extension init |
| `MainActivity.kt` | Single Activity, Compose host, **hand-rolled state-machine navigation** (state flags → `when` block) |
| `di/DatabaseModule.kt` | Koin module for database |
| `di/ExtensionModule.kt` | Koin module for extensions |
| `di/RepositoryModule.kt` | Koin module for repositories |
| `di/SearchModule.kt` | Koin module for search |
| `error/` | Crash handler + ErrorActivity |

### `:core:*` — Core Modules (14 modules)

| Module | Purpose | Key Contents |
|--------|---------|---------------|
| `core:common` | Domain models, shared utilities | Base entities, enums |
| `core:database` | SQLDelight database setup | Schema, queries |
| `core:anilist-api` | AniList GraphQL API client | Discovery, metadata, personalization |
| `core:source-api` | Extension source contract | Aniyomi-compatible interface |
| `core:episode-metadata` | Episode metadata enrichment | Jikan/MAL + Anikage.cc + AniList Streaming; per-field fetch toggles |
| `core:extensions` | Extension loading system | Injekt-based, loads Keiyoushi/Aniyomi-compatible extensions at runtime |
| `core:preferences` | Preference storage | `EpisodeDisplayPreferences` and similar |
| `core:ui` | Shared Compose UI components, theme | Theme, reusable composables |
| `core:util` | Utility functions | Helpers |

### `:feature:*` — Feature Modules

| Module | Purpose | Key Implementation Details |
|--------|---------|----------------------------|
| `feature:browse` | Browse/discovery screen | AniList-driven discovery |
| `feature:search` | Search screen | Dual-source (AniList + extensions), manual link flow |
| `feature:anime-details` | Anime details screen | 3-stage load: AniList → source match → episodes + metadata; contains `EpisodesSection.kt` + `EpisodeDisplayPreferences.kt` |
| `feature:watch` | Watch screen | MPV player, YouTube-style, gestures, PiP, episode switching |
| `feature:library` | Library screen | Grid/list views, categories, selection mode |
| `feature:episode-settings` | Episode settings subsystem | 4 full-page screens (Hub → Display / Layout / Metadata) with sticky live previews |

### `:data:*` — Data Layer
Repository implementations backing `:core` interfaces.

### `buildSrc/` — Convention Plugins
| Plugin | Purpose |
|--------|---------|
| `anikuta.library` | Standard library module convention |
| `anikuta.library.compose` | Compose-enabled library module convention |

Location: `buildSrc/src/main/kotlin/`

---

## Key Source Files

### `MainActivity.kt` (Navigation)
- **Pattern**: Hand-rolled state-machine (NOT Voyager, NOT Compose Nav)
- **Mechanism**: State flags (`detailAnimeId`, `showSettings`, `episodeSettingsPage`) drive a `when` block
- **Rule**: New screens must follow this pattern

### `App.kt` (Application)
- Koin + Injekt DI setup
- Crash handler initialization
- Extension system initialization

### Episode Settings Subsystem (`:feature:episode-settings`)
- 4 full-page screens: Hub → Display / Layout / Metadata
- Sticky live previews on all screens
- Episode row rebuilt to match OLD ANIKUTA design:
  - Black 70% pill badge
  - `outlineVariant` date/audio pills
  - Plain-text title
- `EpisodeDisplayPreferences` wired to `EpisodeRow` via `koinInject` + reactive `Preference.changes`
- **Critical bug fixed**: Settings previously only affected the preview, not the list (now resolved)

### Episode Metadata (`:core:episode-metadata`)
- Sources: Jikan/MAL + Anikage.cc + AniList Streaming
- Per-field fetch toggles
- `EpisodeDisplayPreferences` reactive wiring

---

## Build Configuration

- **Gradle**: Kotlin DSL
- **Convention plugins**: `buildSrc/` (`anikuta.library`, `anikuta.library.compose`)
- **CI**: GitHub Actions — feature branches do NOT auto-build on push (only `main` does); use `workflow_dispatch` to trigger CI on feature branches
- **Rule**: Do NOT build APKs locally (ADR-003, CI-only)

---

## Dependencies (Key)

| Dependency | Purpose |
|------------|---------|
| Jetpack Compose | UI framework |
| Koin | Dependency Injection |
| Injekt | Aniyomi-compat DI for extensions |
| SQLDelight | Database |
| `aniyomi-mpv-lib` | MPV video playback |
| AniList GraphQL API | Co-primary data source |

---

## Extension System

- **Compatibility**: Keiyoushi/Aniyomi-compatible extensions
- **Loading**: Runtime loading via Injekt
- **Contract**: `:core:source-api` defines the source interface
- **Rule**: Extensions are external APKs that implement the `:source-api` contract