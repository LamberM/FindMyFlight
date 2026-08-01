# AI-Assisted Development Overview

This is the starting point for AI-assisted development on development team repositories. One command —
`/setup-ai-env` — installs and authenticates the full toolchain. A first run takes 15–25
minutes; re-runs converge in 1–2 minutes.

---

## Step 0 — Bootstrap Claude Code

`/setup-ai-env` is a Claude Code skill, so Claude Code must be installed before you can run
it. Install it first using the instructions for your platform.

**Windows:**

```powershell
winget install --id Anthropic.Claude -e
```

Or download directly from [https://claude.ai/download](https://claude.ai/download).

**macOS:**

```bash
brew install --cask claude
```

Or download directly from [https://claude.ai/download](https://claude.ai/download).

**Linux:**

Follow the download instructions at [https://claude.ai/download](https://claude.ai/download).

**VS Code extension (all platforms):**

Install the [Claude Code extension](https://marketplace.visualstudio.com/items?itemName=Anthropic.claude-code)
from the VS Code Marketplace if you use VS Code.

**Verify the install:**

```bash
claude --version
```

This should print a version string. If the command is not found, restart your terminal and
try again.

---

## Step 1 — Run /setup-ai-env

Open your development team repository in Claude Code, then type:

```text
/setup-ai-env
```

The skill detects what is installed, checks version freshness, installs or updates missing
tools, authenticates each one, and prints a final summary. It is safe to re-run at any time.
Full details are in the [setup-ai-env skill](../../.claude/skills/setup-ai-env/SKILL.md).

!!! tip "Prerequisites first"
    Before running the skill, make sure Git, Python, and SSH are in place — see
    [CLI Tools Setup](cli-tools.md) for installation details.

---

## Step 2 — Read these in order

After setup completes, work through these pages. They build on each other.

1. [CLI Tools Setup](cli-tools.md) — deep dive on every CLI the skill installed
2. your repo's `AGENTS.md` — project conventions (mandatory reading)
4. [Infinite Context Memory](infinite-context-memory.md) — ICM usage and hygiene (mandatory)
5. [Prompt Cost Reduction](prompt-cost-reduction.md) — RTK token savings
6. [Maintenance](maintenance.md) — keep the toolchain fresh

After setup, `/setup-ai-env` prints a self-test cheatsheet. Re-run it at any time to check
your environment.

---

## Why this stack

The table below maps the concrete problems this toolchain solves to the tools that solve them.

| Problem | Tool | What you gain |
|---|---|---|
| Each Claude session has no memory of past decisions | [ICM](infinite-context-memory.md) | Decisions, preferences, and resolved errors persist across sessions via local SQLite + embeddings |
| Claude reads full command output and burns tokens fast | [RTK](prompt-cost-reduction.md) | 60–90% token savings on routine dev commands — transparent hook, zero prompt changes |
| Skill / binary versions drift across the team | [`/setup-ai-env`](../../.claude/skills/setup-ai-env/SKILL.md) and [Maintenance](maintenance.md) | Idempotent install + periodic refresh keeps the toolchain reproducible |

---

## Knowledge sources

Providing the AI with the right context is as important as the tooling. Use the sources below

### Agent instruction files

Windsurf, etc.) and shape how the assistant responds within the repository.

| File | Purpose |
|---|---|
| `AGENTS.md` | Top-level agent instructions for the repo (build commands, constraints, module layout links). Recognised by Codex, Claude Code, and others. |
| `CLAUDE.md` | Claude Code root entry point. Delegates to `AGENTS.md` so the rules stay in one place. Module-scoped `CLAUDE.md` files point at path-scoped rules for their subtree. |
| `.github/prompts/` | Reusable prompt templates. Mirrored as Claude slash commands under `.claude/commands/` (thin pointer files — canonical content stays in `.github/prompts/`). |
| `.claude/commands/` | Claude Code slash commands. |
| `.claude/agents/` | Claude Code subagents. |

Each development team repository ships an `AGENTS.md` at the root — review and extend it as the project evolves.


The internal knowledgebase []()
contains curated domain knowledge, runbooks, and best practices that can be fed into AI
agents.

## Company resources

| Resource | Description |
|---|---|

---

## Internet resources

| Resource | Description |
|---|---|
| [awesome-ai-agents](https://github.com/e2b-dev/awesome-ai-agents) | Curated list of AI agent frameworks and tools |
| [A Complete Guide to AGENTS.md](https://www.aihero.dev/a-complete-guide-to-agents-md) | In-depth guide to writing effective agent instruction files |
| [AGENTS.md – Builder.io](https://www.builder.io/blog/agents-md) | Practical advice for configuring AI agents in codebases |
| [Windsurf University](https://windsurf.com/university) | Learning resources for the Windsurf AI editor |

---
