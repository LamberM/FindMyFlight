# AI tool catalog

This page is the canonical reference for tools `/setup-ai-env` installs — purpose, auth, and first commands.

## PATH setup

All portable binaries share one directory: `~/.claude/skills/bin/`. Add it to your `PATH` once.

**Windows (PowerShell):**

```powershell
$binDir = "$ENV:USERPROFILE\.claude\skills\bin"
New-Item -Path $binDir -ItemType Directory -Force
$current = [Environment]::GetEnvironmentVariable('PATH', 'User')
if ($current -notlike "*$binDir*") {
    [Environment]::SetEnvironmentVariable('PATH', "$current;$binDir", 'User')
}
```

**Linux / macOS (Bash):**

```bash
mkdir -p ~/.claude/skills/bin
# Add to your shell profile (~/.bashrc, ~/.zshrc, etc.):
export PATH="$HOME/.claude/skills/bin:$PATH"
```

Open a new terminal session after setting the path.

## Claude Code

**Purpose:** Anthropic's AI coding assistant — the primary agent runtime.

**Installed by:** `/setup-ai-env` (automated). Manual fallback: [maintenance.md](maintenance.md).

**Authenticate:** Authentication is part of Claude Code startup. Run `claude` and follow the browser OAuth flow on first launch.

**Try this:**

```bash
claude --version
```

**Deep dive:** [Claude Code setup](claude-code-setup.md) — install, VS Code extension, spinner config.

## gh

**Purpose:** GitHub CLI for PRs, issues, releases, and API queries.

**Installed by:** `/setup-ai-env` (automated). Manual fallback: [maintenance.md](maintenance.md).

**Authenticate:**

```bash
gh auth login
```

Follow the interactive browser flow. Select **GitHub.com** as the target and **HTTPS** as the preferred protocol.

**Try this:**

```bash
gh pr list
```

**Deep dive:** [GitHub CLI documentation](https://cli.github.com/manual/) — full command reference.

## rtk

**Purpose:** Token-saving proxy that rewrites common dev commands transparently — 60–90% reduction in LLM token consumption per session.

**Installed by:** `/setup-ai-env` (automated). Manual fallback: [maintenance.md](maintenance.md).

No authentication required.

**Try this:**

```bash
rtk gain
```

**Deep dive:** [Prompt cost reduction](prompt-cost-reduction.md) — hook setup, savings verification, `rtk discover`.

## icm

**Purpose:** Persistent cross-session memory backed by local SQLite. Stores architecture decisions, error resolutions, and user preferences that survive context compaction and session restarts.

**Installed by:** `/setup-ai-env` (automated). Manual fallback: [maintenance.md](maintenance.md).

No authentication required.

**Try this:**

```bash
icm health
```

**Deep dive:** [Infinite Context Memory](infinite-context-memory.md) — initialization, hooks, topic naming, importance levels, memory hygiene.
