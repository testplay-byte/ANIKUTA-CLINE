# ANIKUTA — References & Rules Analysis

> Analysis of reference materials (`ANIYOMI_REFRENCE/`, `OLD_ANIKUTA/`) and the ruleset (`RULES/`).

---

## Aniyomi Reference (`ANIYOMI_REFRENCE/`)

### Status: READ-ONLY — Do NOT modify, reorganize, or build inside this folder.

### Contents
- `ANIYOMI/` — Full source tree of Aniyomi (from `github.com/aniyomiorg/aniyomi`, branch `main`), no `.git` history
- `DOCUMENTATION/` — 64 documents (~21,400 lines) covering every module, subsystem, data model, user flow, UI screen, and key file

### Documentation Structure (8 sections)
| Section | Coverage |
|---------|---------|
| `00-overview/` | 10,000-foot view — project overview, dual nature (manga + anime) |
| `01-architecture/` | Architecture, module map |
| `02-data-models/` | Data models (Chapter↔Episode, MangaSource↔AnimeSource) |
| `03-subsystems/` | Subsystems (extensions, downloads, tracking, backup, etc.) |
| `04-ui-screens/` | UI screens |
| `05-key-files/` | Key file analysis |
| `06-user-flows/` | User flows |
| `07-*/` | Additional docs |

### Key Characteristic: Dual Nature
Aniyomi is a fork of Tachiyomi (manga-only) extended with a full anime side. Almost every subsystem has a manga variant and an anime variant:
- `Chapter` ↔ `Episode`
- `MangaSource` ↔ `AnimeSource`

### Rule
Copy ideas into `ANIKUTA_PROJECT/ANIKUTA/` and adapt there. The snapshot is NOT kept in sync with upstream.

---

## Old ANIKUTA (`OLD_ANIKUTA/`)

### Status: READ-ONLY reference.

### Contents
- `ANIKUTA_OLD/` — Prior attempt source code
- `ANALYSIS/` — 4 analysis files of key screens

### Purpose
The old ANIKUTA project's owner-flagged screens are the **primary design reference** for the new design language. Key screens analyzed:
- Screen analysis files (4 files in `ANALYSIS/`)

### Rule
Read-only. Use as design reference only.

---

## Reference Module Map (`DOCS/03-reference-module-map.md`)

Maps Aniyomi modules to ANIKUTA's planned modules. Shows how the reference architecture informs the new module structure.

---

## Feature Specs (`PLANNING/01-feature-specs/`)

Detailed feature specifications for planned features.

---

## Module Architecture Plans (`PLANNING/04-module-architecture/`)

Draft module architecture plans used during Phase 0b (Design & Planning).

---

## Prompts (`PROMPTS/`)

| File | Purpose |
|------|---------|
| `library-page-prompt1-setup.md` | Library page setup prompt |
| `library-page-prompt2-implementation.md` | Library page implementation prompt |
| `search-page-prompt1-setup.md` | Search page setup prompt |
| `search-page-prompt2-implementation.md` | Search page implementation prompt |

These are prompts used to guide feature development in two phases: setup → implementation.

---

## The 14 AI Agent Rules (`RULES/ai-agent-rules.md`)

The foundational, non-negotiable ruleset:

1. **No Blind Guesses** — Never silently assume, guess, or fabricate. If unclear, list 2-3 explicit assumptions with trade-offs, recommend one, and ask for confirmation. Show reasoning first.
2. **Architecture Document (MUST READ FIRST)** — `ARCHITECTURE.md` is the single source of truth. Read before writing any code. Keep updated after structural changes. Flag conflicts between user requests and architecture doc.
3. **Data Flow Rules (CRITICAL)** — Data flows strictly: `UI → ViewModel → Repository → Data Source`. Never skip a layer. UI = display logic only. ViewModels never call APIs/databases directly. Repository interfaces in `:core`; implementations in `:data`.
4. **Modularity** — Feature modules never import from other feature modules. Cross-feature goes through `:core`.
5. **Dependencies** — Manage carefully. Check `ARCHITECTURE.md` for approved dependencies.
6. **Design Language** — Follow `DESIGN_LANGUAGE/`. Don't improvise UI.
7. **Task Management** — Track progress. Tick phase tasks.
8. **Logging** — Use appropriate logging. Send ntfy.sh notifications on task completion (ADR-008).
9. **Code Quality** — Follow Kotlin best practices, Compose conventions.
10. **Communication** — Ask if unclear. Show reasoning. List options with trade-offs.
11. **Errors** — Handle gracefully. Don't crash. Use crash handler.
12. **Git** — One concern per commit. Descriptive commit messages.
13. **Module Boundaries** — Respect module boundaries (see Rule 4).
14. **Documentation** — Document as you go. New decision → ADR. New module → README. Phase task → tick.

---

## Project Conventions (`RULES/project-conventions.md`)

ANIKUTA-specific rules:
- **Reference boundaries**: `ANIYOMI_REFRENCE/` and `OLD_ANIKUTA/` are read-only
- **CI-only builds**: No local APK builds (ADR-003)
- **ntfy notifications**: Send on every task completion (ADR-008)
- **Session handoff**: Write session notes in `RULES/sessions/`

---

## Notification Format (`RULES/notifications.md`)

**Topic**: `TASKISDONE`
**Endpoint**: `https://ntfy.sh/TASKISDONE`

### Format
8 emojis (same color) on line 1, blank line, then the message:

| Color | Meaning |
|-------|---------|
| 🟩 (green) | Success |
| 🟥 (red) | Error |
| 🟧 (orange) | Processing (starting a long task) |
| 🟦 (blue) | Stopped / needs input |

### Example
```bash
# Success
curl -s -H "Title: <short title>" -d "🟩🟩🟩🟩🟩🟩🟩🟩

<message>" https://ntfy.sh/TASKISDONE
```

---

## Session Handoff (`RULES/session-handoff-template.md`)

Template for writing session notes in `RULES/sessions/`. File naming: `YYYY-MM-DD-HHMM`. The newest session note indicates what the last agent was doing.