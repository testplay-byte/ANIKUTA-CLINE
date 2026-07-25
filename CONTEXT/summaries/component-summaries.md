# ANIKUTA — Component Summaries

> Concise summaries of each component/module in the ANIKUTA project.

---

## App Layer

### `:app`
- **Purpose**: Application entry point, DI setup, navigation host
- **Key files**: `App.kt` (Application class), `MainActivity.kt` (Single Activity + state-machine nav)
- **DI**: Koin modules (Database, Extension, Repository, Search)
- **Navigation**: Hand-rolled state-machine (NOT Voyager/Compose Nav) — state flags drive `when` block
- **Error handling**: Crash handler + ErrorActivity

---

## Feature Layer

### `:feature:browse`
- **Purpose**: Browse/discovery screen
- **Data source**: AniList-driven discovery
- **Status**: ✅ Complete

### `:feature:search`
- **Purpose**: Search screen
- **Data sources**: Dual-source (AniList + extensions)
- **Key flow**: Manual link flow (link AniList entry to extension source)
- **Status**: ✅ Complete

### `:feature:anime-details`
- **Purpose**: Anime details screen
- **Load pattern**: 3-stage load (AniList → source match → episodes + metadata)
- **Key files**: `EpisodesSection.kt`, `EpisodeDisplayPreferences.kt`
- **Status**: ✅ Complete

### `:feature:watch`
- **Purpose**: Watch screen with video player
- **Player**: MPV (via `aniyomi-mpv-lib`)
- **Features**: YouTube-style, gestures, PiP, episode switching
- **Status**: ✅ Complete

### `:feature:library`
- **Purpose**: Library screen
- **Views**: Grid/list, categories, selection mode
- **Status**: ✅ Complete

### `:feature:episode-settings`
- **Purpose**: Episode settings subsystem
- **Screens**: 4 full-page (Hub → Display / Layout / Metadata) with sticky live previews
- **Episode row design**: Black 70% pill badge, `outlineVariant` date/audio pills, plain-text title
- **Wiring**: `EpisodeDisplayPreferences` → `EpisodeRow` via `koinInject` + reactive `Preference.changes`
- **Status**: ✅ Complete

---

## Core Layer

### `:core:common`
- **Purpose**: Domain models, shared utilities
- **Contents**: Base entities, enums

### `:core:database`
- **Purpose**: SQLDelight database setup
- **Contents**: Schema, queries

### `:core:anilist-api`
- **Purpose**: AniList GraphQL API client
- **Role**: Co-primary data source (ADR-010) — discovery, metadata, personalization

### `:core:source-api`
- **Purpose**: Extension source contract
- **Compatibility**: Aniyomi-compatible interface

### `:core:episode-metadata`
- **Purpose**: Episode metadata enrichment
- **Sources**: Jikan/MAL + Anikage.cc + AniList Streaming
- **Features**: Per-field fetch toggles

### `:core:extensions`
- **Purpose**: Extension loading system
- **Mechanism**: Injekt-based, runtime loading of Keiyoushi/Aniyomi-compatible extensions

### `:core:preferences`
- **Purpose**: Preference storage
- **Contents**: `EpisodeDisplayPreferences` and similar

### `:core:ui`
- **Purpose**: Shared Compose UI components, theme
- **Contents**: Theme, reusable composables

### `:core:util`
- **Purpose**: Utility functions

---

## Data Layer

### `:data:*`
- **Purpose**: Repository implementations backing `:core` interfaces
- **Rule**: Repository interfaces in `:core`; implementations in `:data`

---

## Build System

### `buildSrc/`
- **Purpose**: Convention plugins
- **Plugins**: `anikuta.library` (standard), `anikuta.library.compose` (Compose-enabled)
- **Location**: `buildSrc/src/main/kotlin/`