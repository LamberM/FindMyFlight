# Maintenance

The AI tooling stack ships frequent updates. Run the steps below periodically (or whenever something stops working) to keep every tool on a supported version.

!!! tip "Automated path"


## Claude Code

Or let `/setup-ai-env` handle this — see [Setup AI Env](../../.claude/skills/setup-ai-env/SKILL.md).

```bash
claude update
```

This updates the Claude Code CLI in place. Follow up with `claude --version` to confirm.

## ICM (Infinite Context Memory)

Or let `/setup-ai-env` handle this — see [Setup AI Env](../../.claude/skills/setup-ai-env/SKILL.md).

Check for a new release:

```bash
icm --version
```

Compare against the latest tag at <https://github.com/rtk-ai/icm/releases/latest>. If newer, download the platform installer and re-run it (Windows: `.msi`; macOS: `.pkg`; Linux: `.deb` / `.rpm`). The SQLite database under `~/.icm/` is preserved across upgrades.

## RTK (Rust Token Killer)

Or let `/setup-ai-env` handle this — see [Setup AI Env](../../.claude/skills/setup-ai-env/SKILL.md).

```bash
rtk --version
```

Compare against <https://github.com/rtk-ai/rtk/releases/latest>. RTK is a single binary in `~/.claude/skills/bin/`; replace the file in place to upgrade.


Or let `/setup-ai-env` handle this — see [Setup AI Env](../../.claude/skills/setup-ai-env/SKILL.md).


```bash
git -C ~/workspaces/ pull --ff-only
```

After pulling, reinstall the skills you use — see [Claude Skills Setup](claude-skills.md) for the install commands.

# then re-run the corresponding `auth login` from cli-tools.md if needed
```

## Quick health check

After a maintenance round, re-run `/setup-ai-env` to verify everything is green. The skill re-detects the current state and prints a PASS/FAIL table for every managed tool. If any item shows `FAIL`, scroll up to the matching section above for the manual fix procedure.
