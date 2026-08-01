# ai-tooling

## What is ai-tooling

`ai-tooling` is a shared, dotfiles-style repository that centralises Claude Code rules,
skills, agent workflow guides, hooks, and documentation for the development team . Without
it, every consumer repo (`your-repo`, `my-app`, and any future additions)
duplicates the same configuration independently, causing drift whenever conventions change. This
repo eliminates that problem: the installer creates symlinks from each consumer repo (or from
`~/.claude/` for user-wide access) into a single source of truth, so updates land in one place
and propagate automatically to every linked consumer on the next `git pull` + re-run of
`scripts/install.sh`.

---

## Requirements

The following tools must be available on `PATH` before running the installer.

| Tool | Minimum version | Notes |
|---|---|---|
| `git` | 2.20 | Required for symlink detection and lock-file SHA recording |
| `jq` | 1.6 | JSON parsing used inside the installer and hook scripts |
| `ln` | any | Bundled on Linux/macOS; included in Git for Windows (Git Bash) |

**Windows note:** `install.ps1` creates symlinks automatically. If Developer Mode is off, a single
UAC elevation prompt appears during install to create the symlinks. Enable Developer Mode
(`Settings > Privacy & Security > For developers`) to avoid the prompt entirely. Use `--copy` only
if symlinks are actively unwanted (see [Windows support](#windows-support)).

---

## Getting started — clone

`ai-tooling` **must** be cloned as a sibling directory of every consumer repo it serves. The
installer resolves consumer paths relative to the filesystem layout, and symlink targets must
remain valid across machines — they are relative paths, so the sibling constraint is not
optional.

Expected layout:

```text
~/Code/
  ai-tooling/              # this repo
  my-app/  # consumer (GitHub / gh)
```

Clone command (SSH):

```bash
git clone git@github.com:ai-tooling.git
```

HTTPS alternative (no SSH key required):

```bash
git clone 
```

---

## Installation

`scripts/install.sh` operates in one of two modes. Exactly one of `--project` or `--user-home` is
required per invocation.

### Project mode

Wires shared assets into a single consumer repo. Run this once per consumer repo, from within
the `ai-tooling` root.

```bash
./scripts/install.sh --project <absolute-path-to-consumer-repo>
pwsh
.\scripts\install.ps1 -Project <path>
```

- `--vcs` is auto-detected from the consumer's `git remote get-url origin` when omitted. Pass
  it explicitly if detection produces a wrong result or if the remote is not yet configured.
- **GitHub consumers** (`--vcs github`): symlinks are created in `.claude/` and additionally in
  `.github/skills/` to support AI assistant CLI skill discovery.

### User-home mode

Installs rules, skills, and hooks into `~/.claude/` so they apply globally to every repo on
the machine.

```bash
./scripts/install.sh --user-home
pwsh
# A UAC prompt appears if Developer Mode is off; enable Developer Mode to suppress it
.\scripts\install.ps1 -UserHome
```

This mode includes `setup-ai-env` (not available in project mode). Agent workflow guides and
docs are **not** installed in user-home mode — they are project-scoped.

### Other flags

| Flag | Effect |
|---|---|
| `--copy` | Copies files instead of creating symlinks (Windows fallback — see [Windows support](#windows-support)) |
| `--unlink` | Removes all symlinks (or copies) previously created by the installer — teardown |
| `--dry-run` | Prints every action that would be taken without making any changes |

### Hook registration

After install, hook scripts are symlinked into `.claude/hooks/`. The installer **never**
modifies `settings.json` — hook registration is the consumer's responsibility. A post-install
reminder is printed listing the exact entries needed.

Required additions to the consumer's `.claude/settings.json`:

```json
{
  "hooks": {
    "PreToolUse": [
      { "matcher": "*", "hooks": [{ "type": "command", "command": "python3 .claude/hooks/-enforcer.py" }] },
      { "matcher": "*", "hooks": [{ "type": "command", "command": "python3 .claude/hooks/convention-enforcer.py" }] }
    ],
    "UserPromptSubmit": [
      { "matcher": "*", "hooks": [{ "type": "command", "command": "python3 .claude/hooks/convention-loader.py" }] }
    ]
  }
}
```

---

## After install — what you still need to provide

The installer creates symlinks; it cannot fill in team-specific values. The following must be
provided manually in each consumer repo after running `scripts/install.sh`.

### Parameterised skill constants

Skills marked "parameterized" in the [repository layout](#repository-layout) require a
`constants.md` file alongside the installed skill directory. `route--ticket` has no
`constants.md.template` and needs no per-consumer setup.

For `refine-backlog` and `create-agent-spec-ticket`, copy the template and fill in the values:

```bash
cp .claude/skills/refine-backlog/constants.md.template .claude/skills/refine-backlog/constants.md
cp .claude/skills/create-agent-spec-ticket/constants.md.template .claude/skills/create-agent-spec-ticket/constants.md
# 1. Update ai-tooling
cd /path/to/ai-tooling
git pull

# 2. Re-run the installer in every consumer repo (idempotent — only new entries are linked)
cd /path/to/ai-tooling
./scripts/install.sh --project /path/to/my-app
```

The installer is idempotent: existing symlinks that already point to the correct target are
left untouched; only missing or stale entries are updated.

---

## v1 scope — Claude Code only

In v1, all shared assets target **Claude Code** exclusively.

AI assistant IDE skill discovery (`.github/skills/` consumed by VS Code agent mode) is not verified
in v1. GitHub consumers do receive `.github/skills/` symlinks, but these are intended for the
targeted for v2.

This scope decision is recorded in `docs/decisions/symlink-strategy.md`.

---

## Windows support

Symlink behaviour depends on whether Developer Mode is enabled.

**Developer Mode enabled** (recommended):

```bash
# Standard install — symlinks behave identically to Linux/macOS
```

**Developer Mode disabled** (fallback):

```bash
# After pulling ai-tooling updates, re-run install to refresh the copies
```

Copies created with `--copy` are read-only to make accidental local edits obvious. After
pulling changes from `ai-tooling`, re-run `scripts/install.sh --copy` to overwrite them.

**Using `scripts/install.ps1` (Windows native PowerShell):** follows the same flag surface as
`install.sh`, with one difference — when Developer Mode is off, `install.ps1` auto-elevates via a
single UAC prompt rather than requiring `--copy`. `--copy` remains available as a preference.

---

## Contributing

Before committing, run both checks locally:

```bash
# Markdown lint
npx markdownlint-cli2 "**/*.md" --ignore node_modules

# Config integrity (dangling refs, manifest sources, hook/agent scope, samples)
python scripts/check-integrity.py .
```

Both run automatically in CI via `.github/workflows/integrity.yaml`. Fix all findings before
pushing. The linter config is in `.markdownlint.json` at the repo root.

Branch protection on `master` requires a pull request with two approvals — one from a
`your-repo` maintainer and one from a `my-app` maintainer. This is
