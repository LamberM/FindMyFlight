# Prompt Cost Reduction

## Overview

This document covers tooling to reduce prompt and context window costs when working with AI coding agents.

---

## RTK (Rust Token Killer)

RTK is a CLI proxy that reduces LLM token consumption by **60–90%** on common dev commands. Single Rust binary, zero dependencies. It intercepts verbose CLI output (git, cargo, npm, etc.) and compresses it before it hits the context window.

**Real-world savings:** ~89% token reduction across 2,900+ commands measured. Your context quota stretches ~3x.

---

## Installation

### macOS

```bash
brew tap rtk-ai/tap && brew install rtk
```

### WSL / Linux

```bash
curl -fsSL https://raw.githubusercontent.com/rtk-ai/rtk/refs/heads/master/install.sh | sh
```

### Windows (native, no WSL)

**i.** Download `rtk-x86_64-pc-windows-msvc.zip` from the [RTK releases page](https://github.com/rtk-ai/rtk/releases/latest).

**ii.** Extract `rtk.exe` into the shared binaries directory (already on `PATH` if you followed [CLI Tools Setup](cli-tools.md)):

```powershell
Expand-Archive -Path "$ENV:USERPROFILE\Downloads\rtk-x86_64-pc-windows-msvc.zip" `
    -DestinationPath "$ENV:USERPROFILE\.claude\skills\bin"
```

**iii.** Open a new terminal and verify:

```powershell
rtk --version
```

!!! note "Hook works on native Windows via Git Bash"
    Claude Code's `PreToolUse` hook always runs in **Git Bash**, even on native Windows. As long as
    `rtk.exe` is on `PATH` inside Git Bash (e.g. via `~/.claude/skills/bin`), the hook rewrites
    commands transparently — no WSL required.

    Verify with `which rtk` inside a Git Bash session. If the binary is not found, add its parent
    directory to your Git Bash `PATH` in `~/.bashrc`.

---

## Setup for Claude Code

```bash
rtk init -g
```

This installs a `PreToolUse` hook that transparently rewrites Bash commands (e.g. `git status` → `rtk git status`) before execution. The agent never sees the rewrite — it just gets dramatically smaller output. 100% RTK adoption across all conversations and subagents, zero manual effort.

### Verify

```bash
rtk init --show
```

Should confirm the hook is installed and executable.

---

## Useful Commands

| Command | What it does |
|---------|-------------|
| `rtk gain` | Show token savings summary — total tokens saved, commands processed, compression ratio |
| `rtk gain --graph` | Visual graph of savings over time |
| `rtk gain --daily` | Breakdown by day |
| `rtk gain --weekly` | Aggregated by week |
| `rtk gain --all --format json` | Export all stats as JSON |
| `rtk discover` | Scan recent sessions and show missed savings opportunities — commands RTK could have compressed but didn't |

---

## Verifying Savings

Use `rtk gain` to see your **actual** token savings. This is the source of truth because it tracks every command RTK processed and compressed.

---

## Understanding `rtk discover`

`rtk discover` scans your last 30 days of Claude Code conversation logs and reports commands that RTK supports.

!!! warning "Known false-negative reporting (Issue #1055)"
    `rtk discover` reads Claude Code's JSONL conversation history, which records commands **as the agent requested them**
    (for example `git diff`), not what was actually executed after the hook rewrote them (for example `rtk git diff`).
    This means it can report "missed savings" even when the hook is active and working correctly.

    **If you have the hook installed with `rtk init -g`, the savings shown by `rtk discover` are already being captured.**
    Use `rtk gain` to confirm actual savings.

    See [Issue #1055 in `rtk`](https://github.com/rtk-ai/rtk/issues/1055).

Example output:

```text
Command                  Count    RTK Equivalent     Est. Savings
gh api                     423    rtk gh             ~201.1K tokens
git log                    564    rtk git            ~65.6K tokens
find /Users/...            296    rtk find           ~56.9K tokens
ls -la                     319    rtk ls             ~30.2K tokens
```

Despite showing low adoption percentages such as `1.2%`, if the hook is installed these commands **are** being rewritten at
execution time. The low percentage is a reporting artifact.

---

## How It Works

RTK sits between the AI agent and shell output. It applies smart filtering, grouping, truncation, and deduplication:

| Command | Before | After | Savings |
|---------|--------|-------|---------|
| `cargo test` | 155 lines | 3 lines | 98% |
| `git status` | 119 chars | 28 chars | 76% |
| `git log` | full output | compact summaries | ~90% |

---

## Scope

!!! note
    RTK applies to **Bash tool calls only**. Claude Code built-in tools (`Read`, `Grep`, `Glob`) bypass the hook. For maximum savings, prefer shell commands over built-in tools where practical.
