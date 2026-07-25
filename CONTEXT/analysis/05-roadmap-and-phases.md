# ANIKUTA — Roadmap & Phased Plan

> Analysis of `PLANNING/PHASED_PLAN.md` and phase execution plans.

---

## Overall Roadmap

The project follows a phased approach. Current status: **Phase 7+ (Implementation) IN PROGRESS**.

---

## Phase Breakdown

### Phase 0a — Repository Setup (COMPLETE)
- Repo structured: `ANIYOMI_REFRENCE/` (reference + 68-doc analysis), `OLD_ANIKUTA/` (prior attempt + screen analysis), `ANIKUTA_PROJECT/` (live code)
- Rules established (`RULES/ai-agent-rules.md` — 14 sections)

### Phase 0b — Design & Planning (COMPLETE)
- Aniyomi reference fully documented (`ANIYOMI_REFRENCE/DOCUMENTATION/`)
- Vision clarified → 30+ ADRs in `DOCS/04`
- Design language docs complete (`DESIGN_LANGUAGE/` — 12 principles, 9 components, themes, 10 per-screen specs)
- Old ANIKUTA key screens analyzed (`OLD_ANIKUTA/ANALYSIS/` — 4 files)
- `ARCHITECTURE.md` finalized — the single source of truth

### Phase 1 — Scaffolding (COMPLETE)
- Gradle project scaffolded under `ANIKUTA_PROJECT/ANIKUTA/`
- Multi-module setup (core/*/feature/*/data/*/app)
- Compose-first, Koin DI
- Convention plugins in `buildSrc/` (`anikuta.library` + `anikuta.library.compose`)

### Phase 2 — Browse & AniList (COMPLETE)
- Browse + AniList API + extension system (Aniyomi-compat via Injekt)
- See `PLANNING/02-phase-2-execution-plan.md`

### Phase 3 — Search & Details (COMPLETE)
- Search (dual-source AniList + extensions, manual link flow)
- Anime details (3-stage load: AniList → source match → episodes + metadata)
- See `PLANNING/03-phase-3-execution-plan.md`

### Phase 4 — Watch & Player (COMPLETE)
- Watch screen + MPV player (YouTube-style, gestures, PiP, episode switching)
- See `PLANNING/04-phase-4-execution-plan.md`

### Phase 5 — Library (COMPLETE)
- Library (grid/list, categories, selection mode)
- See `PLANNING/05-phase-5-execution-plan.md`

### Phase 6 — Episode Metadata (COMPLETE)
- Episode metadata enrichment (Jikan/MAL + Anikage.cc + AniList Streaming)
- `:core:episode-metadata` module
- Per-field fetch toggles

### Phase 7 — Episode Settings (COMPLETE)
- Episode settings subsystem (`:feature:episode-settings` module)
- 4 full-page screens (Hub → Display / Layout / Metadata) with sticky live previews
- Episode row rebuilt to match OLD ANIKUTA design
- `EpisodeDisplayPreferences` wired to `EpisodeRow` via `koinInject` + reactive `Preference.changes`
- Critical bug fixed (settings now affect the list, not just the preview)

---

## Remaining Phases (NOT STARTED)

### Trackers
- AniList/MAL tracking sync beyond display
- Two-way sync (watch status, progress, scores)

### Manga Reader
- Anime-first; manga comes later
- Architecture-ready, hidden behind UI

### Downloads / Offline Playback
- See `BACKUP-AND-RESTORE-AND-DOWNLOADING-PLANING/DOWNLOADS-PLAN.md`
- Offline playback of downloaded episodes

### Notifications
- Dual-mode episode notifications
- See design language principle #8

### Backups
- See `BACKUP-AND-RESTORE-AND-DOWNLOADING-PLANING/BACKUP-AND-RESTORE-PLAN.md`
- Backup/restore of library, history, settings

### Release Build
- Play Store / signed APK build flavor
- Release signing configuration

---

## Build & CI (`DOCS/06-build-and-ci.md`)

- **CI**: GitHub Actions
- **Rule**: CI-only builds (ADR-003) — no local APK builds
- **Feature branches**: Do NOT auto-build on push (only `main` does)
- **Triggering CI on feature branches**: Use `workflow_dispatch`
- **Output**: Debug APKs shipped via CI

---

## Exit Criteria

Each phase has exit criteria defined in `DOCS/05-roadmap.md`. Check there for the current phase's specific completion requirements.