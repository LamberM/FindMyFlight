# Infinite Context Memory

Infinite Context Memory (ICM) gives Claude Code persistent, cross-session memory backed by a local SQLite database.
This page explains what ICM solves, how it integrates with Claude Code, and how to install it.
For the broader setup, see [AI-Assisted Development Setup](overview.md),
[Claude Code Setup](claude-code-setup.md), and [MCP Server Setup](mcp-servers.md).

## What is ICM?

ICM is a **single Rust binary, zero-dependency memory system** that gives Claude Code persistent, cross-session memory backed by a local SQLite database. It replaces flat-file `MEMORY.md` with structured, semantically searchable memory that survives context compaction and session restarts.

## Why adopt it?

ICM addresses the most common memory and context-management gaps in interactive coding sessions:

| Problem | ICM Solution |
|---------|-------------|
| Claude forgets everything between sessions | Memories persist in a local SQLite database |
| Keyword-only search misses relevant context | Hybrid search: 30% BM25 + 70% cosine similarity via local embeddings |
| Manual memory curation overhead | Auto-extraction hooks run on every tool call at zero LLM cost |
| Low-value memories clutter recall | Importance-weighted decay automatically prunes stale entries |

## How it integrates

One command - `icm init --mode standard` - wires up three layers:

1. **Lifecycle Hooks** (`~/.claude/settings.json`) - `SessionStart`, `UserPromptSubmit`, `PreToolUse`, `PostToolUse`, `PreCompact`
2. **Slash Commands** - `/recall` and `/remember`
3. **CLAUDE.md instructions** - CLI fallback and wake-up pack injection

## Installation

`/setup-ai-env` installs and initializes ICM automatically. Run it from Claude Code if you haven't already — see [overview.md](overview.md) for the setup path.

For manual install on a machine without Claude Code, or to reinstall after a binary update:

| Platform | Command |
|---|---|
| macOS | `brew tap rtk-ai/tap && brew install icm` |
| Linux / WSL | `curl -fsSL https://raw.githubusercontent.com/rtk-ai/icm/main/install.sh \| sh` |
| Windows (native) | Download `icm-x86_64-pc-windows-msvc.zip` from [releases](https://github.com/rtk-ai/icm/releases/latest) → extract `icm.exe` to `%USERPROFILE%\.claude\skills\bin\` |

After installing, initialize:

```bash
icm init --mode standard
```

Verify:

```bash
icm health
```

!!! warning "Use `icm.exe`, not `icm`, in PowerShell"
    PowerShell has a built-in alias `icm` for `Invoke-Command`. Always use `icm.exe` explicitly in PowerShell sessions.

!!! note "`icm doctor` gives false negatives on Windows"
    `icm doctor` reports *No ICM hooks found* even when hooks are correctly installed. Use the `UserPromptSubmit hook success` banner at the top of each Claude Code session as the reliable signal.

### First session

Open a new Claude Code session after install and paste:

```text
Run `icm health` and confirm ICM is operational.
Store this preference: icm store -t "preferences" -i critical -c "All ICM memories must be written in English."
Verify: icm recall "what language should memories be written in"
```

Expected: the preference appears as the top recall result.

---

## Memory Hygiene

ICM rewards regular gardening: rich keywords beat a noisy database, and importance levels keep the wake-up pack focused. The conventions below are the team standard — they are also encoded in the [`memory-maintenance`](../../.claude/skills/memory-maintenance/) skill (run it via `memory maintenance` / `audyt pamięci`).

### Topic naming

| Topic prefix | Use for | Example |
|---|---|---|
| `preferences` | User-level rules and behavioural instructions — applied across all projects | "All ICM memories must be in English." |
| `context-<project>` | Active work, decisions in flight, ticket-specific state | `context-ai-tooling` |
| `decisions-<project>` | Long-lived architecture / design choices | `decisions-ai-tooling` |
| `errors-resolved` | Reproducible fixes for non-obvious errors | "Localstack S3 ETag mismatch — set `force_path_style`." |
| `references` | Pointers to external systems (dashboards, channels, docs) | "Pipeline bugs tracked in Linear project INGEST." |

Keep one fact per memory. Use `icm update` to extend an existing entry rather than creating a near-duplicate.

### Importance levels

| Level | Decay | When to use |
|---|---|---|
| `critical` | Never decays | User preferences, behavioural instructions, project-wide rules |
| `high` | Slow | Active context, architecture decisions, recent error resolutions |
| `medium` | Normal | Reference info, background context |
| `low` | Fast | Transient FYIs, weekly TODOs |

A memory at `critical` is loaded into every wake-up pack — be deliberate. Errors-resolved entries stay at `high` long enough to surface during a re-occurrence, then naturally fade.

!!! warning "Never run `icm consolidate` (CLI) or `icm_memory_consolidate` (MCP)"
    Bulk consolidation is count-driven — it fires whenever a topic has ≥5 entries, regardless of whether the entries are actually redundant. It can collapse semantically distinct memories into a lossy summary that you cannot restore.

    Use the [`memory-maintenance`](../../.claude/skills/memory-maintenance/) skill instead — it audits pair-by-pair, only merging entries that pass a content-similarity check.

## Expected Config Files After Setup

**`~/.claude/settings.json`** hooks:

```json
{
  "hooks": {
    "SessionStart":     [{ "type": "command", "command": "$HOME/.local/bin/icm hook start" }],
    "UserPromptSubmit": [{ "type": "command", "command": "$HOME/.local/bin/icm hook prompt" }],
    "PreToolUse":       [{ "type": "command", "command": "$HOME/.local/bin/icm hook pre" }],
    "PostToolUse":      [{ "type": "command", "command": "$HOME/.local/bin/icm hook post" }],
    "PreCompact":       [{ "type": "command", "command": "$HOME/.local/bin/icm hook compact" }]
  }
}
```
