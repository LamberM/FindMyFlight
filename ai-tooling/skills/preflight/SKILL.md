---
name: preflight
description: >
  Run this skill as step 0 of any PR, , , or commit workflow before
  calling any external tool. Verifies shell context, required CLIs (,
  , gh, icm, rtk, ), git identity, and Python encoding. Prints a
  compact PASS/FAIL/MISSING table and a one-line READY or BLOCKED verdict.
  If BLOCKED, stops the workflow and surfaces the missing piece so the user
  can fix it before proceeding.
  Triggers: "preflight", "/preflight", "environment check", "before pr",
  "before ", "before ", "check environment", "check tools",
  "are tools ready", "run preflight".
compatibility:
  required_tools: [gh, icm, rtk, python]
---

# Preflight — environment check

Run as step 0 of any PR, , , or commit workflow to confirm the
shell and required CLIs are available and authenticated before touching any
external system.

## Checks

Run all checks in parallel (one Bash call each). All checks are blocking (BLOCKED on failure).

| # | What | Command | On failure |
|---|------|---------|-----------|
| 1 | Shell (expect bash) | `echo "$SHELL"` | BLOCKED |
| 4 | `gh` auth | `gh auth status 2>&1` | BLOCKED |
| 5 | `icm` present | `icm --version 2>&1 \|\| icm health 2>&1` | BLOCKED |
| 6 | `rtk` present | `rtk --version 2>&1` | BLOCKED |
| 7 | Git identity | `git config --global user.email && git config --global user.name` | BLOCKED |
| 8 | Python encoding | `python -c "import sys; print(sys.stdout.encoding)"` | BLOCKED |
| 11 | `DT_ENVIRONMENT` set | `echo "${DT_ENVIRONMENT:-UNSET}"` | BLOCKED |

` doctor` is the single source of  health: it checks the config
endpoint is reachable, and the OS keyring is available. BLOCKED if `` is
unreachable.

## Output format

Print a compact table, then a one-line verdict.

```text
preflight results
─────────────────────────────────────────────
 1  shell         PASS  /usr/bin/bash
 4  gh            PASS  Logged in to github.com
 5  icm           PASS  icm 1.2.3
 6  rtk           PASS  rtk 0.9.1
 7  git identity  PASS  developer@example.com / Tomasz Gajger
 8  python enc    PASS  utf-8
─────────────────────────────────────────────
READY — proceed to Step 1 (read conventions).
```

On any FAIL or MISSING:

```text
─────────────────────────────────────────────
Fix the above before proceeding.
```

Stop if BLOCKED — do not continue the workflow until the user resolves the
issue. On READY, return control to the calling workflow.
