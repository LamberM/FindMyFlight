# Symlink Strategy — Resolved Spikes

**Date:** 2026-05-15

## Context

Before committing to a symlink-based install strategy for `ai-tooling`, several
behavioural questions needed to be answered: whether Claude Code auto-loads rules from
discovers skills via `.github/skills/` symlinks, whether user-home and project-local installs
of the same skill coexist without collision, and whether MkDocs follows symlinks when building
documentation. All five spikes were resolved prior to Phase 1. The answers inform the
install script design.

## Findings

### B1 — Rule loading

**Question:** Does Claude Code auto-load `.claude/rules/<name>.md` from a consumer repo?

**Result: YES.**

Claude Code scans `.claude/rules/` at session start and loads every `.md` file whose
`paths:` frontmatter matches (or has no `paths:` filter). No additional configuration is
required in `settings.json`. This confirms that symlinks created by the installer at
`.claude/rules/<name>.md` will be discovered and loaded automatically.

### B1b — Symlink following

**Question:** Does Claude Code follow symlinks at `.claude/rules/<name>.md`?

**Result: YES.**

Claude Code reads the target of symlinks placed under `.claude/rules/`. A symlink from
`.claude/rules/git.md` → `../../ai-tooling/rules/git.md` resolves correctly; the
rule content from the shared repo is loaded. No shim files are needed. The install script
creates direct symlinks at the target paths; the never-overwrite policy (skip + log if a
real file already exists) prevents accidental replacement of consumer-local rules.


`.github/skills/<name>/SKILL.md`?


symlinks. Skills installed as symlinks by `install.sh --vcs github` are picked up by the
via `.github/skills/` symlinks is unverified; IDE support is out of scope for v1 and
targeted for v2.

### B1d — User-home + project-local coexistence

**Question:** When the same skill (e.g. `refine-backlog`) exists in both `~/.claude/skills/`
(user-home install) and `.claude/skills/` (project install), do they coexist without
collision?

**Result: YES.**

User-home and project-local skill copies coexist independently. Claude Code loads both;
each is a separate skill entry. No removal or deduplication is required. A developer with
both a project-mode install and a user-home install will see the same skill available in
both scopes — the project-local symlink takes precedence within that project.

### Q5 — MkDocs symlink rendering

**Question:** Does MkDocs follow symlinks when rendering `docs/ai/*.md` files?

**Result: YES (assumed based on MkDocs source; not verified with a live build).**

MkDocs uses Python's `os.walk` with `followlinks=True` when scanning the `docs_dir`. This
is explicit in `mkdocs/utils/__init__.py`. Symlinks to absolute paths carry no circular
reference risk. MkDocs was not installed in the development environment at the time of this
spike, so a live build was not run. The conclusion is based on MkDocs documentation and
source code review.

**Action if this assumption fails:** Use `pymdownx.snippets` includes in stub `.md` files
at the consumer `docs/ai/` path, pointing at the shared file. Add a note to Phase 4 gate
criteria to re-verify before docs are promoted.

### Phase 3 — B1e: Directory-level skill symlinks and `constants.md` placement

**Question:** When a skill directory is installed as a directory-level symlink (e.g.
`.claude/skills/refine-backlog` → `ai-tooling/skills/refine-backlog`), does the
shared SKILL.md load correctly, and where does the skill's `constants.md` file need
to live?

**Result: Shared SKILL.md loads correctly. `constants.md` resolves into ai-tooling.**

Tested during Phase 3 (2026-05-18) by temporarily replacing CC's `.claude/skills/refine-backlog`
directory with a symlink to `ai-tooling/skills/refine-backlog`. Claude Code loaded the
parameterized shared SKILL.md (visible in skill list by the updated description lacking hardcoded
filter ID). The original CC skill (renamed `.cc-live`) appeared as a separate skill under that name.

**Constants.md path resolution:** The SKILL.md bootstrap step instructs Claude Code to run
`git rev-parse --show-toplevel` (returns consumer repo root, e.g. `/path/to/your-repo`),
then read `.claude/skills/refine-backlog/constants.md` relative to that root. Since
`.claude/skills/refine-backlog` is a directory symlink, this path resolves through the symlink
to `ai-tooling/skills/refine-backlog/constants.md`. **`constants.md` must therefore exist
inside `ai-tooling/skills/<skill>/`** when using directory-level skill symlinks.

**Consequence:** `skills/*/constants.md` is added to ai-tooling's `.gitignore`. Each
developer's local clone of ai-tooling may contain one `constants.md` per consumer repo
installed on that machine (gitignored; never committed). Single-machine multi-consumer
setups would overwrite constants.md — Phase 5 should document that
only one project-level install per skill is supported per machine, or switch to
individual-file symlinks (SKILL.md + assets/ + references/ symlinked individually;
constants.md as a real file in the consumer repo's `.claude/skills/<name>/`).

## Decision

All shared assets — rules, skills, agent guides, hook scripts, and documentation pages —
are installed as symlinks; no shim files or snippet includes are needed for v1.
For parameterized skills using directory-level symlinks, `constants.md` lives inside the
shared repo path (gitignored) and is per-developer, not per-consumer. Multi-consumer
setups on a single machine are a Phase 5 concern.

### Phase 5 addendum — installer artifacts are untracked in consumer repos

**Date:** 2026-05-26

Resolved during Phase 5 (CC migration, TICKET-28031): the installer produces two categories
of output, and consumer repos treat them differently.

**Untracked local bootstrap artifacts (must not be committed):**

- Symlinks under `.claude/rules/`, `.claude/skills/`, `agents/`, `docs/ai/`, `.github/skills/`,
  and the hook entry-point — all use relative paths valid only when `ai-tooling` is
  checked out at the expected sibling location. Every developer runs the installer locally
  after clone and after pulling upstream updates.
- `.claude/.ai-tooling-manifest.json` — verbose install record (mode, timestamps, per-entry
  paths). Consumer repos add this to their `.gitignore`.

**Optionally tracked minimal lock:**

`.claude/.ai-tooling.lock` contains only:

```json
{"version":1,"shared_repo_commit":"<40-char sha>"}
```

Consumer repos may commit this file to let reviewers detect shared-tooling revision bumps
via `git diff`. All other fields (branch, timestamp, mode) that existed in earlier installer
versions have been removed from the lock and kept only in the manifest.

**`--copy` fallback:** when a consumer prefers tracked copies over symlinks, `--copy` produces real
files at the link paths. On Windows, `install.ps1` auto-elevates via UAC when Developer Mode is
off, so `--copy` is a preference, not a requirement. Copies are
portable and commitable, unlike relative symlinks.
