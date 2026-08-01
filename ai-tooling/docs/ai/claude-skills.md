# Claude skills reference

This page is the reference for shared AI skills installed by `/setup-ai-env`. Each skill adds a scoped instruction file to `~/.claude/skills/` that teaches Claude how to execute specific workflows.

!!! note "Prerequisite"
    The skills below require local CLI binaries (`gh`, `icm`, `rtk`).
    Install and authenticate them first: [CLI Tools Setup](cli-tools.md).

## Installed skills

The table below maps each skill to its purpose and the keywords that trigger it.

| Skill | What it does | Triggers on |
|---|---|---|
| `github` | GitHub CLI (`gh`) for pull requests, issues, repositories, releases, and Actions workflows | "gh", "github cli", "pull request", "pr create", "issue", "github actions", "workflow run", "gh api", "release" |
