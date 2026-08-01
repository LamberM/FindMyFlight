# Rules index

Rule files are installed to `~/.claude/rules/` (or the project's `.claude/rules/`) by `install.sh`. Each file is automatically loaded by Claude Code when its trigger conditions match.

| Rule file | When it loads | What it governs |
|---|---|---|
| `agent-behavior.md` | Every session | Self-healing directive: fix bugs in source files immediately rather than deferring to ICM or leaving verbal notes |
| `code-comments.md` | Any code-writing task | Prefer self-documenting code and commit messages over inline comments; keep any comment short and specific |
| `diagrams.md` | Any documentation task involving diagrams | Mermaid conventions: `.mmd` source files, styling, accessibility |
| `docs.md` | Any TechDocs / MkDocs documentation edit | Front-matter, formatting, admonitions, link syntax |
| `git.md` | Any PR, branch, or commit operation | Branch naming, commit message format, PR body format, secrets rules |
| `reminders.md` | Every session | Reminders convention: store via `reminders.md` checkbox lines, surfaced by the `check-reminders.sh` hook |

## Adding a new rule

1. Create the rule file under `rules/` in this repo.
2. Add an entry to `$AssetManifest` in `scripts/install.ps1` and the corresponding `do_install` call in `scripts/install.sh`.
3. Update this index.

See `rules/skills.md` for the canonical skill and rule authoring conventions.
