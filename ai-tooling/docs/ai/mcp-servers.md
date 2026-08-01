# MCP Server Setup

Project-level MCP servers can be configured in `.mcp.json` at the root of the repository.

## Configuration Format

```json
{
  "mcpServers": {
    "server-name": {
      "command": "executable-name",
      "args": ["--arg1", "value1"]
    }
  }
}
```

In Claude Code, type `/mcp` to inspect and authenticate active MCP servers.
