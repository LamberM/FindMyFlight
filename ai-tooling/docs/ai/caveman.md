# Caveman — Token Compression Plugin

[Caveman](https://github.com/JuliusBrussee/caveman) is a Claude Code plugin that cuts output tokens ~65% by making Claude respond tersely — same technical accuracy, fewer words. Auto-activates on every session start; no per-session trigger needed.

## Installation

```bash
claude plugin marketplace add JuliusBrussee/caveman
claude plugin install caveman@caveman
```

Restart Claude Code after installing.

## Configuration

### Default intensity level

The `ai-tooling` installer (`scripts/install.sh`) automatically adds `CAVEMAN_DEFAULT_MODE=full` to your shell RC file (`~/.zshrc`, `~/.bash_profile`, `~/.bashrc`, or `~/.config/fish/config.fish` — detected from `$SHELL`). This is the recommended approach: the env var takes highest priority and survives plugin reinstalls.

To override manually or set a different level, export the variable in your RC file:

```bash
# bash / zsh
export CAVEMAN_DEFAULT_MODE=full   # lite | full | ultra | wenyan-full

# fish
set -x CAVEMAN_DEFAULT_MODE full
```

Alternatively, create a user config file:

```bash
mkdir -p ~/.config/caveman
echo '{"defaultMode":"full"}' > ~/.config/caveman/config.json
```

Valid values: `lite`, `full` (default), `ultra`, `wenyan-full`.

| Level | Effect |
|---|---|
| `lite` | Drops filler/hedging, keeps articles and full sentences |
| `full` | Drops articles, fragments OK, short synonyms — classic caveman |
| `ultra` | Telegraphic; abbreviates prose words, arrows for causality |
| `wenyan-full` | Classical Chinese register; maximum compression |

Switch level mid-session with `/caveman lite`, `/caveman full`, `/caveman ultra`.

Resolution order: `CAVEMAN_DEFAULT_MODE` env var → repo-local `.caveman/config.json` → `~/.config/caveman/config.json` → `full`.

### Statusline badge

Add to `~/.claude/settings.json` to show `[CAVEMAN]` in the Claude Code status bar:

```json
"statusLine": {
  "type": "command",
  "command": "bash \"$(ls ~/.claude/plugins/cache/caveman/caveman/*/src/hooks/caveman-statusline.sh | head -1)\""
}
```

After `/caveman-stats` runs at least once, the badge shows lifetime savings: `[CAVEMAN] ⛏ 12.4k`.

## Available skills

| Skill | What it does |
|---|---|
| `/caveman [lite\|full\|ultra]` | Switch intensity level for current session |
| `/caveman-stats` | Real token usage + lifetime savings + USD cost |
| `/caveman-compress <file>` | Rewrite a memory file (e.g. CLAUDE.md) into caveman-speak (~46% input token reduction per session) |
| `/caveman-commit` | Conventional Commit messages, ≤50 char subject |
| `/caveman-review` | One-line PR comments |

## Disabling

Say `"normal mode"` or `"stop caveman"` to turn off for the current session.

To disable permanently, set `defaultMode` to `"off"` in `~/.config/caveman/config.json`.

## Updating

```bash
claude plugin update caveman@caveman
```

## Troubleshooting

| Symptom | Fix |
|---|---|
| Plugin not found | Run `claude plugin marketplace add JuliusBrussee/caveman` first |
| Caveman not activating on session start | Confirm plugin is enabled: `claude plugin list` — `caveman@caveman` should show `enabled` |
| Statusline not showing | Check `statusLine` block in `~/.claude/settings.json`; verify Node.js is installed (`node --version`) |
