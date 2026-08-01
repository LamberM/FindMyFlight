# Claude Code Setup

Claude Code is Anthropic's AI coding assistant available as a CLI tool and as a VS Code extension.
This page covers installation and common configuration options.

## Installation

### CLI

Follow the [Claude Code Quickstart](https://code.claude.com/docs/en/quickstart) for the full
installation guide. The CLI works on Linux, macOS, and Windows (WSL or Git Bash).

### VS Code Extension

Install the [Claude Code extension](https://marketplace.visualstudio.com/items?itemName=anthropic.claude-code)
from the VS Code Marketplace. Search for **Claude Code** by Anthropic, or install it directly from the link above.

## Configuration

### Spinner Verbs

Claude shows rotating words while it thinks (e.g. "Pondering…", "Ruminating…", "Cogitating…").
You can replace these with any words you like.

**CLI** — add to `~/.claude/settings.json`:

```json
"spinnerVerbs": {
  "mode": "replace",
  "verbs": [
    "Thinking"
  ]
}
```

**VS Code extension** — add to your VS Code user settings (`~/Library/Application Support/Code/User/settings.json`
on macOS, `$Env:APPDATA\Code\User\settings.json` on Windows):

```json
"claudeCode.spinnerVerbs": {
  "mode": "replace",
  "verbs": ["Thinking"]
}
```

!!! tip
    Set `"mode": "replace"` to use only your list. Omit the key or use `"add"` to append your words to the defaults.

### Further Reading

See the [Claude Code Settings reference](https://code.claude.com/docs/en/settings) for the full list of available configuration options.
The [VS Code extension documentation](https://code.claude.com/docs/en/vs-code) covers extension-specific features and settings.
