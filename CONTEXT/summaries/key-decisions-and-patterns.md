# ANIKUTA — Key Decisions & Patterns

> Concise summary of important architecture decisions (ADRs) and recurring patterns.

---

## Key Architecture Decisions (ADRs)

30+ ADRs recorded in `DOCS/04-design-decisions.md`. Most critical:

| ADR | Decision | Impact |
|-----|---------|--------|
| ADR-003 | CI-only builds (no local APK builds) | All builds via GitHub Actions; no `./gradlew assemble*` locally |
| ADR-008 | ntfy.sh notifications on every task completion | Topic `TASKISDONE`; 8-emoji format (green=success, red=error, orange=processing, blue=stopped) |
| ADR-010 | AniList as co-primary data source | Not just a tracker — used for discovery, metadata, personalization |
| ADR-015 | Custom M3-inspired design language | NOT stock Material 3; NOT Aniyomi's UI; based on OLD ANIKUTA screens |
| ADR-029 | Aniyomi-compatible extension system | Extensions are external APKs implementing `:source-api` contract; Injekt-based loading |

---

## Recurring Patterns

### 1. Layered Data Flow (Critical)
```
UI → ViewModel → Repository → Data Source
```
- **Never skip a layer**
- UI = display logic + event forwarding only
- ViewModels never call APIs/databases directly
- Repository interfaces in `:core`; implementations in `:data`

### 2. Module Boundary Rule
- Feature modules **never** import from other feature modules
- Cross-feature communication goes through `:core`

### 3. Hand-Rolled State-Machine Navigation
- Used in `MainActivity.kt`
- NOT Voyager, NOT Compose Nav
- State flags (`detailAnimeId`, `showSettings`, `episodeSettingsPage`) drive a `when` block
- New screens must follow this pattern

### 4. Compose-First UI
- All UI in Jetpack Compose
- Reusable components in `:core:ui`
- Design language enforced via `DESIGN_LANGUAGE/` docs

### 5. Koin + Injekt DI
- Koin for app DI
- Injekt for Aniyomi-compatible extension loading
- Koin modules in `:app/di/`

### 6. Convention Plugins
- `buildSrc/` contains `anikuta.library` and `anikuta.library.compose`
- All modules apply these conventions

### 7. Reactive Preferences
- `Preference.changes` for reactive updates
- Example: `EpisodeDisplayPreferences` wired to `EpisodeRow` via `koinInject` + `Preference.changes`

### 8. 3-Stage Data Loading
- Used in anime details: AniList → source match → episodes + metadata
- Progressive enhancement pattern

### 9. Dual-Source Pattern
- Search uses dual sources (AniList + extensions)
- User picks preferred metadata source with automatic fallback

### 10. Read-Only References
- `ANIYOMI_REFRENCE/` and `OLD_ANIKUTA/` are read-only
- Copy ideas into `ANIKUTA_PROJECT/ANIKUTA/` and adapt

---

## Important Conventions

| Convention | Detail |
|-----------|--------|
| Application ID | `app.confused.anikuta` |
| Package convention | `app.confused.anikuta.*` |
| Version | 0.1.0 (versionCode 1) |
| Commit style | One concern per commit, descriptive messages |
| Documentation | New decision → ADR; New module → README; Phase task → tick |
| Session handoff | Write notes in `RULES/sessions/` (format: `YYYY-MM-DD-HHMM`) |

---

## Anti-Patterns to Avoid

1. **Don't build APKs locally** — CI-only (ADR-003)
2. **Don't modify references** — `ANIYOMI_REFRENCE/`, `OLD_ANIKUTA/` are read-only
3. **Don't improvise UI** — Follow `DESIGN_LANGUAGE/`
4. **Don't skip layers** — Always go UI → ViewModel → Repository → Data Source
5. **Don't cross feature boundaries** — Use `:core` for cross-feature communication
6. **Don't use Voyager/Compose Nav** — Use hand-rolled state-machine in `MainActivity.kt`
7. **Don't blind guess** — Ask if unsure, show reasoning first (Rule §1)