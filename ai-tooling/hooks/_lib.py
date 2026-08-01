#!/usr/bin/env python3
"""
Shared utilities for Dev convention hooks.

Imported by convention-enforcer.py and convention-loader.py.
Never executed directly.
"""

import json
import os
import re
import shutil
from datetime import datetime
from pathlib import Path


def _detect_vcs_cli() -> list:
    """Return VCS CLIs found on PATH. Fallback to ['gh'] when none found."""
    found = [cli for cli in ("gh", "") if shutil.which(cli)]
    return found if found else ["gh"]


_CONFIG_DEFAULTS: dict = {
    # Generic ticket prefix — consumer repos override via convention-enforcer-config.json.
    "branch_prefix_pattern": r"(?:[A-Z]+-\d+|NOISSUE)",
    "vcs_cli": _detect_vcs_cli(),
}


def should_defer(script_path: Path, cwd: "Path | None") -> bool:
    """Return True if a *different* project-local copy of script_path exists.

    Uses os.path.realpath on both sides so a project symlink pointing at the
    same physical file is treated as equal — preventing the hook from silently
    skipping when the project copy is just a symlink installed by install.sh.
    """
    if not cwd:
        return False
    project_copy = cwd / ".claude" / "hooks" / script_path.name
    if not project_copy.exists():
        return False
    return os.path.realpath(project_copy) != os.path.realpath(script_path)


def load_config(cwd: "Path | None") -> dict:
    """Load .claude/convention-enforcer-config.json from the consumer repo.
    Falls back to defaults on missing file or malformed JSON."""
    if not cwd:
        return _CONFIG_DEFAULTS
    cfg_path = cwd / ".claude" / "convention-enforcer-config.json"
    if not cfg_path.is_file():
        return _CONFIG_DEFAULTS
    try:
        with cfg_path.open(encoding="utf-8") as fh:
            user_cfg = json.load(fh)
    except (json.JSONDecodeError, OSError):
        return _CONFIG_DEFAULTS
    return {**_CONFIG_DEFAULTS, **user_cfg}


def _try_read_file(path: str) -> "str | None":
    path = path.strip().strip("'\"")
    m = re.match(r"^/([a-zA-Z])(/.*)", path)
    if m:
        path = f"{m.group(1).upper()}:{m.group(2)}"
    try:
        with open(path, encoding="utf-8") as fh:
            return fh.read()
    except OSError:
        return None


def extract_flag(command: str, flag: str) -> "str | None":
    """Extract the value for --flag from command string.
    Handles double-quoted, single-quoted, bare values, and $(cat file) substitutions."""
    # $(cat file) command substitution takes precedence.
    sub = re.search(rf'{re.escape(flag)}\s+"?\$\(cat\s+([^\)]+)\)"?', command, re.DOTALL)
    if sub:
        return _try_read_file(sub.group(1))
    for pat in [
        rf'{re.escape(flag)}\s+"((?:[^"\\]|\\.)*)"',
        rf"{re.escape(flag)}\s+'((?:[^'\\]|\\.)*)'",
        rf"{re.escape(flag)}\s+(\S+)",
    ]:
        m = re.search(pat, command, re.DOTALL)
        if m:
            return m.group(1)
    return None


def log_violations(violation_log: str, command: str, violations: list) -> None:
    timestamp = datetime.now().isoformat(timespec="seconds")
    try:
        with open(violation_log, "a", encoding="utf-8") as fh:
            fh.write(f"\n[{timestamp}]\n")
            fh.write(f"command: {command[:300]}\n")
            for v in violations:
                fh.write(f"  VIOLATION: {v}\n")
    except OSError:
        pass