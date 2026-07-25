# CONTEXT/ — Persistent Memory for ANIKUTA Project

> This folder is the agent's structured memory area. It contains analysis, summaries, notes, and credentials gathered during the full project analysis.

## Folder Structure

```
CONTEXT/
├── README.md                  ← This file
├── analysis/                  ← Detailed code analysis, architecture diagrams, reverse-engineered docs
│   ├── 01-architecture-overview.md
│   ├── 02-module-code-analysis.md
│   ├── 03-design-language-analysis.md
│   ├── 04-references-and-rules.md
│   ├── 05-roadmap-and-phases.md
│   ├── 06-backup-restore-branch.md
│   └── 07-downloads-branch.md
├── summaries/                 ← Concise summaries of components, key decisions, patterns
│   ├── component-summaries.md
│   └── key-decisions-and-patterns.md
├── notes/                     ← Operational notes, warnings, dependencies
│   └── operational-notes.md
└── credentials/               ← Sensitive information (GITIGNORED — never committed)
    └── github_token.txt
```

## How to Use This Folder

### For a new agent picking up the project:
1. Read `AGENT_CONTEXT/START_HERE.md` first (project onboarding)
2. Then read this `CONTEXT/README.md` for the structured memory
3. Dive into `analysis/` for detailed understanding
4. Check `summaries/` for quick reference
5. Review `notes/operational-notes.md` for warnings and environment info

### File Index

| File | Purpose |
|------|---------|
| `analysis/01-architecture-overview.md` | Overall architecture, tech stack, module breakdown, Mermaid diagram, ADRs, phase status |
| `analysis/02-module-code-analysis.md` | Detailed module tree, key source files, build config, dependencies, extension system |
| `analysis/03-design-language-analysis.md` | Design language principles, components, themes, screen specs, episode settings architecture |
| `analysis/04-references-and-rules.md` | Aniyomi reference, old ANIKUTA, 14 agent rules, conventions, notification format |
| `analysis/05-roadmap-and-phases.md` | Phased plan (Phase 0a through Phase 7+), remaining phases, build & CI |
| `analysis/06-backup-restore-branch.md` | Backup & Restore branch analysis — `:core:backup` + `:feature:backup` modules, providers, formats, auto-backup |
| `analysis/07-downloads-branch.md` | Downloads branch analysis — `:core:download` + `:feature:download` modules, ADR-035, StateFlow queue, offline playback |
| `summaries/component-summaries.md` | Concise summary of each module/component |
| `summaries/key-decisions-and-patterns.md` | Key ADRs, recurring patterns, conventions, anti-patterns |
| `notes/operational-notes.md` | Environment notes, build/CI, warnings, dependencies, file location cheat sheet |
| `credentials/github_token.txt` | GitHub PAT (GITIGNORED — never committed) |

## Security Note

The `credentials/` folder is **gitignored** and must never be committed. See `.gitignore` at the project root.