#!/usr/bin/env python3
"""
Register ai-tooling hooks into the user-global Claude settings.json.

Called by install.sh and install.ps1. Idempotent: re-runs leave the file
unchanged if every required hook is already registered. Existing entries
(other hooks, other top-level keys, $schema) are preserved.

Usage:
    register-hooks.py <settings_path> [--unregister]
"""
from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path

# Hook script filenames (relative to ~/.claude/hooks/)
_PRE_TOOL_USE_SCRIPTS = [
    "convention-enforcer.py",
    "-enforcer.py",
]
_USER_PROMPT_SUBMIT_SCRIPT = "convention-loader.py"
_REMINDERS_SCRIPT = "check-reminders.sh"
_ALL_SCRIPTS = [*_PRE_TOOL_USE_SCRIPTS, _USER_PROMPT_SUBMIT_SCRIPT, _REMINDERS_SCRIPT]


def _python_exe() -> str:
    return "python" if sys.platform == "win32" else "python3"


def _make_cmd(settings_path: str, script: str) -> str:
    """Build an absolute-path command for a hook script.

    Absolute paths are required because global settings.json hooks are invoked
    with the project directory as CWD, not ~/.claude — so relative paths break.
    Forward slashes are used throughout; they work on both Windows and Unix.
    """
    hooks_dir = Path(settings_path).resolve().parent / "hooks"
    return f'{_python_exe()} "{hooks_dir.as_posix()}/{script}"'


def _make_bash_cmd(settings_path: str, script: str, *extra_args: str) -> str:
    """Same as _make_cmd but invokes via bash (for non-Python hook scripts)."""
    hooks_dir = Path(settings_path).resolve().parent / "hooks"
    cmd = f'bash "{hooks_dir.as_posix()}/{script}"'
    if extra_args:
        cmd += " " + " ".join(extra_args)
    return cmd


def _is_managed(cmd: str) -> bool:
    """Match managed hook commands by script filename (handles both old relative
    paths and new absolute paths, so unregister works after an upgrade)."""
    return any(s in cmd for s in _ALL_SCRIPTS)


def load(path: str) -> dict:
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    return {}


def save(path: str, data: dict) -> None:
    os.makedirs(os.path.dirname(path) or ".", exist_ok=True)
    tmp = path + ".tmp"
    with open(tmp, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    os.replace(tmp, path)


def register(data: dict, settings_path: str) -> tuple[dict, list[str]]:
    """Return (updated settings, list of added commands).

    Works on a deep copy so the caller's ``data`` is never mutated in place.
    """
    result: dict = json.loads(json.dumps(data))
    added: list[str] = []
    hooks = result.setdefault("hooks", {})
    pre = hooks.setdefault("PreToolUse", [])
    ups = hooks.setdefault("UserPromptSubmit", [])
    starts = hooks.setdefault("SessionStart", [])

    bash_entry = next((e for e in pre if e.get("matcher") == "Bash"), None)
    if bash_entry is None:
        bash_entry = {"matcher": "Bash", "hooks": []}
        pre.append(bash_entry)
    bash_hooks = bash_entry.setdefault("hooks", [])

    for script in _PRE_TOOL_USE_SCRIPTS:
        cmd = _make_cmd(settings_path, script)
        if not any(script in h.get("command", "") for h in bash_hooks):
            bash_hooks.append({"type": "command", "command": cmd})
            added.append(cmd)

    loader_present = any(
        any(_USER_PROMPT_SUBMIT_SCRIPT in h.get("command", "") for h in entry.get("hooks", []))
        for entry in ups
    )
    if not loader_present:
        cmd = _make_cmd(settings_path, _USER_PROMPT_SUBMIT_SCRIPT)
        ups.append({"hooks": [{"type": "command", "command": cmd}]})
        added.append(cmd)

    reminders_prompt_present = any(
        any(_REMINDERS_SCRIPT in h.get("command", "") for h in entry.get("hooks", []))
        for entry in ups
    )
    if not reminders_prompt_present:
        cmd = _make_bash_cmd(settings_path, _REMINDERS_SCRIPT)
        ups.append({"hooks": [{"type": "command", "command": cmd}]})
        added.append(cmd)

    reminders_start_present = any(
        any(_REMINDERS_SCRIPT in h.get("command", "") for h in entry.get("hooks", []))
        for entry in starts
    )
    if not reminders_start_present:
        cmd = _make_bash_cmd(settings_path, _REMINDERS_SCRIPT, "--force")
        starts.append({"hooks": [{"type": "command", "command": cmd}]})
        added.append(cmd)

    return result, added


def _partition_managed(inner: list) -> tuple[list, list]:
    """Split a hook list into (kept, removed-commands) by managed-script match."""
    kept: list = []
    removed: list[str] = []
    for h in inner:
        cmd = h.get("command") if isinstance(h, dict) else None
        if cmd and _is_managed(cmd):
            removed.append(cmd)
        else:
            kept.append(h)
    return kept, removed


def unregister(data: dict) -> list[str]:
    removed: list[str] = []
    hooks = data.get("hooks")
    if not isinstance(hooks, dict):
        return removed

    for key in ("PreToolUse", "UserPromptSubmit", "SessionStart"):
        entries = hooks.get(key)
        if not isinstance(entries, list):
            continue
        for entry in entries:
            inner = entry.get("hooks") if isinstance(entry, dict) else None
            if not isinstance(inner, list):
                continue
            entry["hooks"], gone = _partition_managed(inner)
            removed.extend(gone)
        hooks[key] = [e for e in entries if e.get("hooks")]
        if not hooks[key]:
            del hooks[key]

    if not hooks:
        del data["hooks"]
    return removed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("path")
    parser.add_argument("--unregister", action="store_true")
    args = parser.parse_args()

    try:
        data = load(args.path)
    except json.JSONDecodeError as e:
        print(f"ERROR: {args.path} is not valid JSON: {e}", file=sys.stderr)
        return 1

    if args.unregister:
        changed = unregister(data)
        verb = "Removed"
    else:
        data, changed = register(data, args.path)
        verb = "Registered"

    if changed:
        save(args.path, data)
        print(f"{verb} {len(changed)} hook(s) in {args.path}:")
        for cmd in changed:
            print(f"  - {cmd}")
    elif args.unregister:
        print(f"No managed hooks found in {args.path} - nothing to remove")
    else:
        print(f"All hooks already registered in {args.path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())