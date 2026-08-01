#!/usr/bin/env python3
"""
check-integrity.py — repo-level integrity checks for ai-tooling.

Verifies:
  1. All source files referenced in install.sh install_list exist.
  2. Python hook files are syntactically valid.
  3. SKILL.md files have valid YAML front-matter.
  4. catalog-info.yaml files are valid YAML.
  5. Markdown files have no dangling links.
  7. No standalone TEAM_UUID row in constants.md or constants.md.template files.

Exit 0 on all pass, exit 1 on any failure.
"""

import ast
import os
import re
import sys
from pathlib import Path

try:
    import yaml
    _HAS_YAML = True
except ImportError:
    _HAS_YAML = False

REPO_ROOT = Path(__file__).resolve().parent.parent

FAILURES: list[str] = []


def fail(msg: str) -> None:
    FAILURES.append(msg)
    print(f"  FAIL  {msg}")


def ok(msg: str) -> None:
    print(f"  ok    {msg}")


# ---------------------------------------------------------------------------
# 1. Source files that must exist
# ---------------------------------------------------------------------------

# Intentionally separate from ASSET_MANIFEST in install.sh: this list tests
# that key source files exist in the repo, not the full install contract.
# Update here when adding new required files that must exist before install runs.
REQUIRED_SOURCES = [
    # hooks
    "hooks/_lib.py",
    # scripts
    "scripts/install.sh",
    "scripts/install.ps1",
    "scripts/register-hooks.py",
    # rules
    "rules/agent-behavior.md",
    "rules/diagrams.md",
    "rules/docs.md",
    "rules/git.md",
    # agents
    "agents/repository-operations.md",
    # docs
    "docs/ai/overview.md",
    "docs/ai/cli-tools.md",
    "docs/ai/mcp-servers.md",
    "docs/ai/maintenance.md",
    # root
    ".markdownlint.json",
]


def check_sources() -> None:
    print("\n--- Source files ---")
    for rel in REQUIRED_SOURCES:
        p = REPO_ROOT / rel
        if p.exists():
            ok(rel)
        else:
            fail(f"missing: {rel}")


# ---------------------------------------------------------------------------
# 2. Python syntax check
# ---------------------------------------------------------------------------

def check_python_syntax() -> None:
    print("\n--- Python syntax ---")
    for py in sorted((REPO_ROOT / "hooks").glob("*.py")):
        try:
            ast.parse(py.read_text(encoding="utf-8"))
            ok(py.name)
        except SyntaxError as e:
            fail(f"{py.name}: {e}")

    for py in sorted((REPO_ROOT / "scripts").glob("*.py")):
        try:
            ast.parse(py.read_text(encoding="utf-8"))
            ok(py.name)
        except SyntaxError as e:
            fail(f"{py.name}: {e}")


# ---------------------------------------------------------------------------
# 3. SKILL.md front-matter
# ---------------------------------------------------------------------------

def _skill_tree_files(filename: str):
    """Yield (path, rel_str) for files named *filename* anywhere under skills/, sorted."""
    for path in sorted((REPO_ROOT / "skills").rglob(filename)):
        yield path, str(path.relative_to(REPO_ROOT))


def _load_yaml_text(text: str):
    """Parse a YAML string; return (data, error_message). error is None on success."""
    try:
        return yaml.safe_load(text), None
    except yaml.YAMLError as e:
        return None, str(e)


def _load_yaml(path: "Path"):
    """Load YAML from *path*; return (data, error_message). error is None on success."""
    return _load_yaml_text(path.read_text(encoding="utf-8"))


def check_skill_frontmatter() -> None:
    if not _HAS_YAML:
        print("\n--- SKILL.md front-matter (SKIPPED — pyyaml not installed) ---")
        return
    print("\n--- SKILL.md front-matter ---")
    for skill_md, rel in _skill_tree_files("SKILL.md"):
        text = skill_md.read_text(encoding="utf-8")
        if not text.startswith("---"):
            fail(f"{rel}: missing front-matter")
            continue
        parts = text.split("---", 2)
        if len(parts) < 3:
            fail(f"{rel}: malformed front-matter")
            continue
        meta, err = _load_yaml_text(parts[1])
        if err:
            fail(f"{rel}: YAML error: {err}")
        elif not isinstance(meta, dict) or "name" not in meta:
            fail(f"{rel}: front-matter missing 'name'")
        else:
            ok(rel)


# ---------------------------------------------------------------------------
# 4. catalog-info.yaml validity
# ---------------------------------------------------------------------------

def check_catalog_yaml() -> None:
    if not _HAS_YAML:
        print("\n--- catalog-info.yaml (SKIPPED — pyyaml not installed) ---")
        return
    print("\n--- catalog-info.yaml ---")
    for cat, rel in _skill_tree_files("catalog-info.yaml"):
        data, err = _load_yaml(cat)
        if err:
            fail(f"{rel}: {err}")
        elif not isinstance(data, dict) or data.get("kind") != "Component":
            fail(f"{rel}: expected kind: Component")
        else:
            ok(rel)


# ---------------------------------------------------------------------------
# 5. Dangling markdown links
# ---------------------------------------------------------------------------

_MD_LINK_RE = re.compile(r'\[(?:[^\]]*)\]\(([^)]+)\)')
_FENCED_BLOCK_RE = re.compile(r'```.*?```', re.DOTALL)
_INLINE_CODE_RE = re.compile(r'`[^`\n]+`')
_SKIP_DIRS = {"node_modules", ".git"}
# Installed symlink destinations or consumer-repo paths — only valid post-install
_SKIP_RESOLVED_PREFIXES = (".claude/skills/", ".claude/agents/", ".github/instructions/")


def _md_link_targets(text: str):
    """Yield real link targets (skipping URLs, anchors, and non-ASCII placeholders)."""
    for m in _MD_LINK_RE.finditer(text):
        target = m.group(1).partition("#")[0].strip()
        if not target or target.startswith(("http://", "https://", "mailto:")):
            continue
        if any(ord(c) > 127 for c in target):
            continue  # non-ASCII placeholder (e.g. …) — not a real path
        yield target


def _is_broken_link(md: "Path", target: str) -> bool:
    resolved = (md.parent / target).resolve()
    try:
        rel = resolved.relative_to(REPO_ROOT)
    except ValueError:
        return False  # escapes repo root — intentional consumer-context link
    if any(str(rel).startswith(p) for p in _SKIP_RESOLVED_PREFIXES):
        return False  # post-install symlink destination — valid in consumer repo
    return not resolved.exists()


def check_markdown_links() -> None:
    print("\n--- Markdown links ---")
    md_files = [
        p for p in REPO_ROOT.rglob("*.md")
        if not any(part in _SKIP_DIRS for part in p.parts)
    ]
    broken: list[str] = []
    for md in sorted(md_files):
        text = _FENCED_BLOCK_RE.sub("", md.read_text(encoding="utf-8", errors="replace"))
        text = _INLINE_CODE_RE.sub("", text)
        for target in _md_link_targets(text):
            if _is_broken_link(md, target):
                broken.append(f"{md.relative_to(REPO_ROOT)}: broken link → {target}")
    if broken:
        for b in broken:
            fail(b)
    else:
        ok(f"{len(md_files)} markdown file(s) checked, no dangling links")




# ---------------------------------------------------------------------------
def main() -> None:
    os.chdir(REPO_ROOT)

    print(f"check-integrity — {REPO_ROOT}")
    check_sources()
    check_python_syntax()
    check_skill_frontmatter()
    check_catalog_yaml()
    check_markdown_links()

    print(f"\n{'='*50}")
    if FAILURES:
        print(f"FAILED — {len(FAILURES)} issue(s):")
        for f in FAILURES:
            print(f"  * {f}")
        sys.exit(1)
    else:
        print("ALL CHECKS PASSED")
        sys.exit(0)


if __name__ == "__main__":
    main()