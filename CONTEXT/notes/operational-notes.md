# ANIKUTA — Operational Notes

> Operational notes, warnings, and useful information for working on the ANIKUTA project.

---

## Environment Notes

### Shell (Windows PowerShell)
- The default shell is **PowerShell**, not cmd
- Use `;` for command chaining (NOT `&&`)
- Use `curl.exe` for HTTP requests (PowerShell aliases `curl` to `Invoke-WebRequest`)
- Git is available at v2.54.0

### NTFY.SH Notifications
- **Topic**: `TASKISDONE`
- **Endpoint**: `https://ntfy.sh/TASKISDONE`
- **Format**: 8 emojis (same color) on line 1, blank line, then message
- **Colors**: 🟩 green=success, 🟥 red=error, 🟧 orange=processing, 🟦 blue=stopped/needs input
- **Free tier limit**: Daily message quota may be reached (HTTP 429). Quota resets daily.
- **Command** (PowerShell):
  ```powershell
  curl.exe -H "Title: <title>" -H "Tags: <emoji>" -d "<message>" https://ntfy.sh/TASKISDONE
  ```

---

## Build & CI Notes

### CI-Only Builds (ADR-003)
- **NEVER** build APKs locally — no `./gradlew assemble*`
- All builds go through GitHub Actions
- Feature branches do NOT auto-build on push (only `main` does)
- To trigger CI on a feature branch: use `workflow_dispatch`

### Gradle
- Kotlin DSL
- Convention plugins in `buildSrc/` (`anikuta.library`, `anikuta.library.compose`)
- Multi-module: `:app`, `:core:*`, `:feature:*`, `:data:*`

---

## Critical Warnings

### Read-Only References
- `ANIYOMI_REFRENCE/` — **READ-ONLY**. Do not modify, reorganize, or build inside.
- `OLD_ANIKUTA/` — **READ-ONLY**. Use as design reference only.
- Copy ideas into `ANIKUTA_PROJECT/ANIKUTA/` and adapt there.

### Navigation Pattern
- The app uses a **hand-rolled state-machine** in `MainActivity.kt`
- NOT Voyager, NOT Compose Nav
- New screens must follow this pattern (state flags → `when` block)

### Design Language
- Follow `DESIGN_LANGUAGE/` docs — **don't improvise UI**
- Every screen/component must trace back to a design doc
- NOT stock Material 3, NOT Aniyomi's UI

### Data Flow
- **Never skip layers**: UI → ViewModel → Repository → Data Source
- ViewModels never call APIs/databases directly
- Feature modules never import from other feature modules (use `:core`)

---

## Dependencies

### Key Dependencies
| Dependency | Purpose | Notes |
|------------|---------|-------|
| Kotlin | Language | Primary language |
| Jetpack Compose | UI framework | Compose-first |
| Koin | DI | App-level DI |
| Injekt | DI | Aniyomi-compat extension loading |
| SQLDelight | Database | Local storage |
| `aniyomi-mpv-lib` | Video playback | MPV player for watch screen |
| AniList GraphQL API | Data source | Co-primary (ADR-010) |

### Extension Compatibility
- Keiyoushi/Aniyomi-compatible extensions
- Loaded at runtime via Injekt
- Implement `:core:source-api` contract

---

## File Location Cheat Sheet

| You want to... | Go here |
|----------------|---------|
| Understand the vision | `DOCS/04-design-decisions.md` (ADRs 009–030) |
| Understand the design language | `DESIGN_LANGUAGE/` |
| Read the Aniyomi reference analysis | `ANIYOMI_REFRENCE/DOCUMENTATION/` |
| Read the old ANIKUTA screen analysis | `OLD_ANIKUTA/ANALYSIS/` |
| Find planning specs | `PLANNING/` |
| Find the rules | `RULES/` |
| Write/edit app code | `ANIKUTA_PROJECT/ANIKUTA/` (live multi-module project) |
| Episode settings screens | `ANIKUTA_PROJECT/ANIKUTA/feature/episode-settings/` |
| Episode row + display prefs | `ANIKUTA_PROJECT/ANIKUTA/feature/anime-details/.../EpisodesSection.kt` + `EpisodeDisplayPreferences.kt` |
| Episode metadata (sources/repo/prefs) | `ANIKUTA_PROJECT/ANIKUTA/core/episode-metadata/` |
| Convention plugins (build) | `ANIKUTA_PROJECT/ANIKUTA/buildSrc/src/main/kotlin/` |
| Leave a note for the next agent | `RULES/sessions/` |
| Send a notification | ntfy.sh, topic `TASKISDONE` |

---

## Mandatory Read Order (for new agents)

1. `AGENT_CONTEXT/START_HERE.md`
2. `ARCHITECTURE.md` — the single source of truth
3. `RULES/ai-agent-rules.md` — the 14-section ruleset
4. `RULES/project-conventions.md` — ANIKUTA-specific rules
5. `RULES/notifications.md` — ntfy.sh notification format
6. `DOCS/04-design-decisions.md` — all decisions (ADRs 001–030+)
7. `DOCS/05-roadmap.md` — current phase
8. `DESIGN_LANGUAGE/` — UI/UX spec
9. Newest session note in `RULES/sessions/`

---

## Git Repository Information

- **Source repo**: `https://github.com/testplay-byte/ANI_KUTA_NEW` (remote: `origin`)
- **Target repo**: `https://github.com/testplay-byte/ANIKUTA-CLINE` (remote: `target` — to be added)
- **Default branch**: `main`
- **Commit style**: One concern per commit, descriptive messages