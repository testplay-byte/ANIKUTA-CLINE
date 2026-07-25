# ANIKUTA — Design Language Analysis

> Analysis of `DESIGN_LANGUAGE/` folder. The design language is a **custom M3-inspired visual + interaction language** (ADR-015).

## Overview

The ANIKUTA design language is explicitly:
- **NOT** stock Material 3 Expressive (owner finds it insufficient)
- **NOT** a copy of Aniyomi's UI (owner finds it ugly, especially the bottom nav)
- The old ANIKUTA project's owner-flagged screens are the **primary design reference**

**Rule**: Every screen and component must trace back to a doc in `DESIGN_LANGUAGE/`. Don't improvise UI.

---

## Folder Structure

| Folder | Contents |
|--------|---------|
| `01-principles/` | Core cross-cutting design principles (12 principles) |
| `02-components/` | Reusable UI components — the vocabulary (9 components) |
| `03-themes/` | Color palettes, typography, theme selection system |
| `04-screens/` | Per-screen design specs (10 per-screen specs) |

---

## 12 Core Design Principles (`01-principles/core-principles.md`)

Each principle has What/Why/Where/Source:

1. **Edge-to-edge top bar** — Top nav extends under status bar; only content gets `statusBarsPadding`, never the background. Global rule, no exceptions.
2. **No drag handle on bottom-up menus** — `dragHandle = null` on all `ModalBottomSheet` usages. Visual noise.
3. **Bottom-up menus** — Prefer bottom-up menus over dialogs for contextual actions.
4. **Floating bottom nav** — Custom floating bottom navigation (not Aniyomi's ugly bottom nav).
5. **M3-inspired but unique** — Material 3 as baseline, but custom expressions.
6. **Per-episode metadata** — Rich metadata display per episode.
7. **YouTube-style watch page** — Watch page modeled on YouTube's UX.
8. **Dual-mode episode notifications** — Two notification modes.
9. **Auto-download** — Background download capability.
10. **Customizable screens/nav** — User-configurable screens and navigation.
11. **AniList as co-primary** — AniList integration for discovery and personalization.
12. **Extension-based** — Aniyomi-compatible extension system.

---

## 9 Reusable Components (`02-components/components.md`)

The component vocabulary includes 9 reusable UI components. Key ones:

- **Episode row** — Black 70% pill badge, `outlineVariant` date/audio pills, plain-text title (matches OLD ANIKUTA design)
- **Bottom-up menus** — `ModalBottomSheet` with `dragHandle = null`
- **Floating bottom nav** — Custom floating navigation bar
- **Top bar** — Edge-to-edge, extends under status bar
- *(+ 5 more components — see `DESIGN_LANGUAGE/02-components/components.md`)*

---

## Themes (`03-themes/`)

- Color palettes (light/dark)
- Typography scale
- Theme selection system (user-configurable)

---

## 10 Per-Screen Specs (`04-screens/`)

Detailed design specs for each screen:

1. Browse screen
2. Search screen
3. Anime details screen
4. Watch screen
5. Library screen
6. Episode settings screens (Hub, Display, Layout, Metadata)
7. *(+ others — see `DESIGN_LANGUAGE/04-screens/`)*

---

## Episode Settings Architecture (`DOCS/episode-settings-architecture.md`)

- **4 full-page screens**: Hub → Display / Layout / Metadata
- **Sticky live previews** on all screens
- **Episode row design**: Black 70% pill badge, `outlineVariant` date/audio pills, plain-text title
- **Wiring**: `EpisodeDisplayPreferences` → `EpisodeRow` via `koinInject` + reactive `Preference.changes`
- **Critical bug fixed**: Settings previously only affected the preview, not the list (now resolved)

---

## Backup, Restore & Download Plans

Located in `BACKUP-AND-RESTORE-AND-DOWNLOADING-PLANING/`:

| File | Purpose |
|------|---------|
| `BACKUP-AND-RESTORE-PLAN.md` | Backup and restore strategy |
| `DOWNLOADS-PLAN.md` | Download/offline playback plan |
| `FOLDER-STRUCTURE-PLAN.md` | Folder structure for downloads/backups |

These are **planned** features (not yet implemented — see roadmap).

---

## Prototype Reference (`PROTOTYPE_REFERENCE/`)

- `ANALYSIS.md` — Analysis of the prototype design
- `Anime_App/` — Prototype app files
- Used as a visual reference for the design language