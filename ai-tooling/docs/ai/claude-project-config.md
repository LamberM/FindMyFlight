# Claude Project Configuration

This page describes the project-level Claude Code configuration layout — the equivalent of the
GitHub setup under `.github/` and `.vscode/`. It applies to **both** the Claude Code CLI
and the Claude Code VS Code extension; both read the same files.

For one-time install steps (CLI binary, VS Code extension, spinner verbs), see [Claude Code Setup](claude-code-setup.md).

## File Layout

```text
<your-repo>/
├── CLAUDE.md                       # Root entry point — delegates to AGENTS.md
├── AGENTS.md                       # Canonical agent instructions 
├── .claude/
│   ├── settings.json               # Project-scoped Claude Code settings
│   ├── commands/                   # Slash commands
│   ├── agents/                     # Subagents (canonical location)
│   └── skills/                     # Step-by-step skill bundles (canonical location)
└── <module>/CLAUDE.md              # Module-scoped — points at module-specific instructions
```

## CLAUDE.md and AGENTS.md

The root `CLAUDE.md` delegates to `AGENTS.md` — all repo guidelines, build commands, and hard
constraints live in `AGENTS.md`. Edits go there, not in `CLAUDE.md` or
`.github/ai-instructions.md` (which is also kept in sync with `AGENTS.md`).

Module-scoped `CLAUDE.md` files act as Claude's equivalent of AI assistant's `applyTo:` glob in
`.github/instructions/*.instructions.md`. They contain a one-line pointer at the matching
instruction file so the canonical content stays single-source.

## Slash Commands (`.claude/commands/`)

Each `.md` file under `.claude/commands/` becomes a `/<filename>` command in Claude Code. Commands
are thin pointers — canonical prompt content lives in `.github/prompts/*.prompt.md` (shared with
GitHub). Edit there, not in `.claude/commands/`.

## Skills (`.claude/skills/`)

Skills are step-by-step workflow bundles with a `SKILL.md` file plus optional `references/`.
Claude auto-loads a skill when the user's request matches the trigger phrases in its `description:` front-matter.

Browse `.claude/skills/` for the current set of skills.

!!! note
    Skills live under `.claude/skills/` so Claude Code auto-loads them without extra configuration. The `SKILL.md` format is unchanged.

## MCP Servers (`.mcp.json`)

`.mcp.json` at the repository root configures project-level MCP servers for Claude Code.

On first use Claude Code prompts for approval before connecting to a project-level MCP server. To
auto-approve every server in `.mcp.json`, add `"enableAllProjectMcpServers": true` to your user
settings (`~/.claude/settings.json`) — do **not** set this in the project-level
`.claude/settings.json`.

For VS Code setup, see [MCP Server Setup](mcp-servers.md).

!!! warning "Windows shell constraints"
    On Windows, Claude Code's `Bash` tool runs in **Git Bash** regardless of the user's default shell. Mixing PowerShell syntax into a `Bash` tool call fails silently or produces wrong output. The conventions in Dev repos:

    - **Use bash inside `Bash` tool calls.** `$VARIABLE` not `$env:VARIABLE`, `rm` not `Remove-Item`, forward slashes (`C:/Users/...`) for paths, `$(...)` for command substitution.
    - **Don't `&&`-chain commands** when the user expects PowerShell — write them as separate calls or wrap in a script.
    - **Don't use heredocs** (`cat <<'EOF'`). Use the `Write` tool to create a file, then reference its path.
    - **Avoid `>` redirection** in long commands. Use `Write` to put the payload in a file and read it back.
    - **Force UTF-8 explicitly** when sending non-ASCII payloads from PowerShell — Windows defaults to `cp1252` and will corrupt diacritics.

## Settings (`.claude/settings.json`)

The project-level settings file is intentionally minimal — it ships only the JSON Schema reference.
Personal preferences (model, spinner verbs, permission allowlists, hooks) belong in your
user-level `~/.claude/settings.json`, not in the repository.

See the [Claude Code Settings reference](https://code.claude.com/docs/en/settings) for the full
list of available keys.

## Subagents (`.claude/agents/`)

Subagent files have YAML front-matter (`name`, `description`, `tools`, `model`) and are auto-invoked
by Claude when the user's request matches the `description`. Each file contains the full agent definition.

Browse `.claude/agents/` for the current set.

### Subagent Efficiency Rules

Subagents protect the main context window from large outputs. They earn their keep when the call would otherwise dump >100 lines into Claude's context. Reach for one whenever the task involves:

- A command whose output is unbounded or hard to predict.
- Read-heavy exploration (`grep -r`, `find`, `git log`).

When delegating, follow these rules:

1. **Single-purpose** — one query per agent. Don't chain three slow commands; spawn three agents.
2. **Always prefix `timeout`** for anything that may block:
   - `timeout 60 az ...`
3. **Cap output** — pipe through `| head -50` or `| tail -50` so the agent returns a digest, not a transcript.
4. **No `run_in_background: true`** for agents expected to return in seconds — failures stay silent until notification.
5. **Dry-run first for writes** — when the agent will mutate state (`apply`, `create`, `edit`, `transition`), have it print the planned action and exit, then call a second agent to commit only after human confirmation. Mutating state behind a single agent is the most common cause of "Claude did the wrong thing in production."

## Resumable Long-Running Tasks

Multi-session work (a refactor that spans several days, a backlog audit, a multi-step ticket
investigation) loses momentum when each new session starts from zero. The development team pattern:

1. **Create a tracking file** as soon as a task crosses a rough complexity threshold (>5 tool calls or work that won't finish in one session). Place it under `.archive/` (gitignored) or in the ticket folder under `/tmp/` if it should never reach the repo.
2. **Lay out the file with a known structure** so the next session can pick up:

    ```markdown
    # TICKET-XXXXX — short title

    ## Status
    in progress / blocked on X / ready for review

    ## Information collected
    - <bullet list of facts gathered>

    ## Plan
    1. <step>
    2. <step>

    ## Checklist
    - [x] step 1 — done YYYY-MM-DD
    - [ ] step 2
    ```

3. **Resume by instructing Claude to read the file** at session start: "Read `.archive/TICKET-XXXXX.md` and continue from the first unchecked checklist item." This costs one tool call and restores the entire mental model.
4. **Move to `.archive/` on completion** (or delete) so the working tree stays clean.

The `refine-backlog` skill is the canonical example — it persists state across batches via a JSON file and resumes from the last unprocessed ticket.

## Git Hooks

A `pre-push` hook can be installed for each consumer repo as a plain Git hook. It is
editor-agnostic. Install it once with:

```bash
ln -sf ../../.github/hooks/pre-push .git/hooks/pre-push
```

This is **not** the same as a Claude Code event hook (`PreToolUse`, `Stop`, etc.) — those live
under `~/.claude/settings.json` and are out of scope for the project-level configuration.

---

## Extending and Authoring

The sections below are for engineers who want to write or improve instruction files, skills, or subagents. Familiarity with the sections above is assumed.

## Writing Effective Instructions

Instruction files (`AGENTS.md`, `CLAUDE.md`, `.github/instructions/*.instructions.md`) are how a
repo teaches Claude what "good" looks like. A few rules keep them useful:

- **Layer the hierarchy.** Global preferences (`~/.claude/CLAUDE.md`) → project rules (`AGENTS.md` / `.claude/rules/`) → module rules (`<module>/CLAUDE.md`) → path-scoped rules (`.github/instructions/*.instructions.md`). Each layer overrides the layer above it. Never duplicate a rule across layers — link instead.
- **Hard rules vs soft guidance.** Use imperative language ("must", "never", "always") for non-negotiable rules; use descriptive language ("prefer", "consider") for guidance. Mixing them dilutes the imperative ones.
- **Single source of truth.** Canonical content lives in `AGENTS.md` or `.github/instructions/`. `CLAUDE.md` files are thin pointers. When you update a rule, search the repo to make sure no stale copy survives.

For the rationale behind this style — and why structure matters more than length — see the GitHub blog post [How to write a great AGENTS.md](https://github.blog/ai-and-ml/github-ai/how-to-write-a-great-agents-md-lessons-from-over-2500-repositories/) referenced from [`overview.md`](overview.md#internet-resources).

### Skill Design Principles

A good skill is not a `--help` page in markdown. It is a focused, opinionated workflow that captures the *judgement* a human would apply when running a tool against a specific project.

- **One job per skill.** If a `SKILL.md` covers two unrelated workflows, split it. Discovery is keyed on the `description:` triggers — overloaded skills fire on too many prompts and surface the wrong workflow.
- **Grow incrementally.** Ship the smallest workflow that solves a real recurring task. Add steps when the team finds the gap; don't speculate.
- **Justify why the skill beats raw `--help`.** A skill is worth its maintenance cost when the answer to "why not just read `--help`?" is non-trivial — there's a sequence to follow, fields to set in a specific order, or a confirmation gate you don't want skipped.

For creating new skills (front-matter, safety guardrails, evals), use the [``]() skill. The hard rule for vendoring or creating a skill in a consumer repo lives in `.claude/rules/skills.md`.

