# ai-tooling — Consumer Onboarding Checklist

## Overview

`ai-tooling` is the shared AI tooling repository for the development team. It ships Claude Code rules, skills, agent guides, hooks, and docs that are common across all Dev consumer repos (your-repo, my-app, and future additions). The installer (`scripts/install.sh`) creates symlinks (or copies on Windows) from the consumer repo into ai-tooling, so shared files stay in one place and updates propagate with a single `git pull` + re-run.

Run this checklist in three situations:

- Fresh checkout of a consumer repo on a new developer machine
- Setting up a brand-new consumer repo that has never been wired to ai-tooling
- After ai-tooling has been updated and you need to pull the latest shared assets into an existing consumer

---

## Prerequisites

Before running the install, confirm the following are available on your machine:

- `git` ≥ 2.20 — symlink-capable `git clone` requires at least this version
- `jq` ≥ 1.6 — used by the installer to read and write the lock file

---

## Project-mode install checklist

These steps wire ai-tooling into a single consumer repository. Repeat for each consumer repo on a new machine.

1. Pull the latest shared tooling:

    ```bash
    git -C ~/Code/ai-tooling pull origin master
    ```

2. Run the installer from the ai-tooling root, pointing it at the consumer repo:

    ```bash
    # Linux / macOS
    ~/Code/ai-tooling/scripts/install.sh --project ~/Code/<consumer>
    pwsh
    # Windows (PowerShell) - a UAC prompt appears if Developer Mode is off
    ~\Code\ai-tooling\scripts\install.ps1 -Project ~\Code\<consumer>
    bash
    # Any platform: --copy uses file copies instead of symlinks (re-run after pulling updates)
    ~/Code/ai-tooling/scripts/install.sh --project ~/Code/<consumer> --copy
    ```

3. Verify the lock file was written:

    ```bash
    cat <consumer-root>/.claude/.ai-tooling.lock
    # Expected: {"version":1,"shared_repo_commit":"<40-char SHA>"}
    ```

4. Verify that symlinks resolve (skip this step when `--copy` was used):

    ```bash
    ls -L <consumer-root>/.claude/rules/repository-operations.md
    ```

5. Register hooks in `<consumer-root>/.claude/settings.json` — **the installer does not touch `settings.json`**. The installer prints the exact JSON block to add at the end of its output; paste it into the `hooks` array. If you missed the output, check the installer source for the current hook list or look at an existing consumer's `settings.json` as a reference.

6. Create per-consumer constants files for parameterised skills — see §"Per-consumer constants" below.


8. Install Claude Code plugins — the two-step sequence (add marketplace, then install) is required:

    ```bash
    #  — team notifications and PR review pings
    claude plugin marketplace add anthropics/claude-plugins-official

    # Caveman — cuts output tokens ~65% with no accuracy loss (recommended)
    claude plugin marketplace add JuliusBrussee/caveman
    claude plugin install caveman@caveman
    ```

    Follow the browser OAuth flow for  when prompted. See [`docs/ai/overview.md`](overview.md) and [`docs/ai/caveman.md`](caveman.md) for full configuration details.

---

## Per-consumer constants

Some skills contain team-wide logic but require consumer-specific values to function (for example, the  project key, default sprint, or backlog board ID). These skills read their values from a constants file at:

```text
<consumer-root>/.claude/constants/<skill-name>.md
```

To populate constants for a skill:

1. Locate the template installed alongside the skill:

    ```bash
    cat <consumer-root>/.claude/skills/<skill-name>/constants.md.template
    ```

2. Copy it to the constants directory and fill in the values:

    ```bash
    mkdir -p <consumer-root>/.claude/constants
    cp <consumer-root>/.claude/skills/<skill-name>/constants.md.template \
       <consumer-root>/.claude/constants/<skill-name>.md
    # Edit the copy — replace all placeholder values with real ones
    ```


Skills that currently require constants: `refine-backlog`, `create-agent-spec-ticket`.

**Important:** do NOT place `constants.md` inside `.claude/skills/<skill>/`. That path resolves through the shared-repo symlink and would affect every consumer on the machine that has ai-tooling installed. The `.claude/constants/` directory is consumer-local and unaffected by the symlink.

---

## Gitignore

The installer prints a gitignore block at the end of its output. Add that block to `<consumer-root>/.gitignore`. It covers:

- Installer-managed symlink targets (rules, skills, hooks, agent guides, docs)
- Runtime artefacts: `.claude/state/`, `.claude/.ai-tooling-manifest.json`
- Per-user files: `.claude/settings.local.json`

If you missed the output, mirror the `# ai-tooling` section from `~/Code/your-repo/.gitignore` — it is kept up to date as the canonical reference.

---

## User-home install

In addition to the per-repo project-mode install, run the user-home install once per developer machine:

```bash
~/Code/ai-tooling/scripts/install.sh --user-home
```

This installs shared skills (including `setup-ai-env`) into `~/.claude/` so they are available globally in every Claude Code session, regardless of which repo you have open. It is a prerequisite for `/setup-ai-env` to work on a machine that has never had it installed before.

---

## Verifying the install

Run these commands from the consumer repo root to confirm everything landed correctly:

```bash
# Lock written
cat .claude/.ai-tooling.lock

# Rules installed
ls -L .claude/rules/

# Skills installed
ls -L .claude/skills/

# Hooks installed
ls -L .claude/hooks/

# Agent guides installed
ls agents/repository-operations.md

# Docs installed
ls docs/ai/overview.md

# GitHub skills (GitHub VCS only)
ls -L .github/skills/argo-wf-reference/
```

All paths should resolve without "No such file or directory". Symlinks that fail to resolve indicate the ai-tooling sibling directory moved or was deleted — fix the path and re-run the installer.

---

## Updating the install

After ai-tooling has been updated upstream (e.g. a new skill was added or a rule was changed), refresh the consumer repo with:

```bash
git -C ~/Code/ai-tooling pull origin master
~/Code/ai-tooling/scripts/install.sh --project <consumer-path> --vcs <vcs>
```

The installer is idempotent — it updates stale symlinks, writes the new lock file SHA, and prints a summary of what changed. No manual cleanup is required.
