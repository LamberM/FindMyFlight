#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------------------------
# install.sh — Phase 1 implementation of the ai-tooling shared AI
# configuration installer. Manages symlinks (or copies) of rules, hooks,
# skills, agent guides, docs, and .mcp.json into a consumer repo or ~/.claude.
# ---------------------------------------------------------------------------

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SHARED_REPO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# ---------------------------------------------------------------------------
# Usage
# ---------------------------------------------------------------------------
usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Install or remove the ai-tooling shared AI configuration into a consumer
repo or the user-wide ~/.claude/ directory.

Required (exactly one):
  --project <path>        Install into the consumer repo at <path> (absolute path)
  --user-home             Install into ~/.claude/ (user-wide)

Optional:
  --copy                  Copy files instead of creating symlinks
                          (Windows fallback or explicit opt-in)
  --unlink                Remove symlinks / copies installed by a previous run
                          (reads the install manifest; lock file is preserved)
  --dry-run               Print what would be done without modifying the filesystem
                          VCS platform; auto-detected from git remote when omitted
  --help, -h              Print this usage message and exit 0

Examples:
  # Symlink-install into a sibling repo:

  # Copy-install (no sibling constraint):
  $(basename "$0") --project /some/path/repo --copy

  # User-wide install:
  $(basename "$0") --user-home

  # Preview what would happen:

  # Remove a previously installed configuration:
EOF
}

# ---------------------------------------------------------------------------
# Preflight checks — validates minimum tool versions
# Skipped when --dry-run or --help is active.
# ---------------------------------------------------------------------------
preflight_check() {
    local failures=()

    # git >= 2.20
    if command -v git &>/dev/null; then
        local git_ver
        git_ver="$(git --version | grep -oE '[0-9]+\.[0-9]+' | head -1)"
        local git_major git_minor
        git_major="${git_ver%%.*}"
        git_minor="${git_ver##*.}"
        if [ "$git_major" -lt 2 ] || { [ "$git_major" -eq 2 ] && [ "$git_minor" -lt 20 ]; }; then
            failures+=("git >= 2.20 required (found $(git --version | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1))")
        fi
    else
        failures+=("git not found — install git >= 2.20")
    fi

    # python3 >= 3.10 — search common Homebrew paths in addition to PATH
    local _py3 _py_found=false
    for _py3 in python3.14 python3.13 python3.12 python3.11 python3.10 \
        /opt/homebrew/bin/python3.14 /opt/homebrew/bin/python3.13 \
        /opt/homebrew/bin/python3.12 /opt/homebrew/bin/python3.11 \
        /opt/homebrew/bin/python3.10 python3; do
        command -v "$_py3" &>/dev/null || continue
        local py_ver py_major py_minor
        py_ver="$("$_py3" --version 2>&1 | grep -oE '[0-9]+\.[0-9]+' | head -1)"
        py_major="${py_ver%%.*}"
        py_minor="${py_ver##*.}"
        if [ "$py_major" -ge 3 ] && [ "$py_minor" -ge 10 ] 2>/dev/null; then
            _py_found=true
            break
        fi
    done
    if ! $_py_found; then
        failures+=("python3 >= 3.10 required — not found in PATH or /opt/homebrew/bin; install via brew install python@3.13")
    fi

    # jq >= 1.6
    if command -v jq &>/dev/null; then
        local jq_ver
        jq_ver="$(jq --version 2>&1 | grep -oE '[0-9]+\.[0-9]+' | head -1)"
        local jq_major jq_minor
        jq_major="${jq_ver%%.*}"
        jq_minor="${jq_ver##*.}"
        if [ "$jq_major" -lt 1 ] || { [ "$jq_major" -eq 1 ] && [ "$jq_minor" -lt 6 ]; }; then
            failures+=("jq >= 1.6 required (found $(jq --version 2>&1))")
        fi
    else
        failures+=("jq not found — install jq >= 1.6")
    fi

    # ln must exist
    if ! command -v ln &>/dev/null; then
        failures+=("ln not found — symlink support is required")
    fi

    if [ "${#failures[@]}" -gt 0 ]; then
        echo "ERROR: preflight check failed — the following tools are missing or outdated:" >&2
        for msg in "${failures[@]}"; do
            echo "  - $msg" >&2
        done
        exit 1
    fi
}

# ---------------------------------------------------------------------------
# Sibling-directory check
# Only runs in project mode when --copy is NOT set.
# ---------------------------------------------------------------------------
sibling_dir_check() {
    local shared_dir="$SHARED_REPO_DIR"
    local consumer_dir
    consumer_dir="$(cd "$PROJECT_PATH" && pwd)"

    if [ "$(dirname "$shared_dir")" != "$(dirname "$consumer_dir")" ]; then
        echo "ERROR: ai-tooling and the consumer repo must be sibling directories." >&2
        echo "  shared:   $shared_dir" >&2
        echo "  consumer: $consumer_dir" >&2
        echo "Move one of the repos or use --copy mode (copies instead of symlinks)." >&2
        exit 1
    fi
}

# ---------------------------------------------------------------------------
# VCS auto-detection
# Only runs in project mode when --vcs is NOT set explicitly.
# ---------------------------------------------------------------------------
detect_vcs() {
    local remote_url
    if ! remote_url="$(git -C "$PROJECT_PATH" remote get-url origin 2>/dev/null)"; then
        echo "ERROR: Cannot auto-detect VCS — no 'origin' remote found in $PROJECT_PATH." >&2
        exit 1
    fi

    [[ "$remote_url" == *"github.com"* ]] && is_github=true

        echo "  remote: $remote_url" >&2
        exit 1
    elif $is_github; then
        VCS="github"
    else
        echo "  remote: $remote_url" >&2
        exit 1
    fi

    echo "Detected VCS: $VCS (from remote: $remote_url)"
}

# ---------------------------------------------------------------------------
# Asset manifest — the fixed set of Tier 1 Phase 1 assets.
#
# Each entry is a colon-separated record:
#   <asset_type>:<source_rel>:<link_rel_project>:<link_rel_user_home>
#
# asset_type    : rules | hooks | skills | agents | docs | root
# source_rel    : path relative to SHARED_REPO_DIR
# link_rel_project  : path relative to CONSUMER_DIR (project mode)
# link_rel_user_home: path relative to HOME (user-home mode), or "-" to skip
#
# For skills the source and link are directories (trailing slash omitted here).
# ---------------------------------------------------------------------------
# ASSET_MANIFEST format: "type:source_rel:link_rel_project:link_rel_user_home[:vcs_filter]"
# "-" in link_rel means not applicable for that install mode.
ASSET_MANIFEST=(
    # Rules (Phase 1 — Tier 1)
    "rules:rules/agent-behavior.md:.claude/rules/agent-behavior.md:.claude/rules/agent-behavior.md"
    "rules:rules/code-comments.md:.claude/rules/code-comments.md:.claude/rules/code-comments.md"
    "rules:rules/diagrams.md:.claude/rules/diagrams.md:.claude/rules/diagrams.md"
    "rules:rules/skills.md:.claude/rules/skills.md:.claude/rules/skills.md"
    # Rules (Phase 2 — Tier 2)
    "rules:rules/docs.md:.claude/rules/docs.md:.claude/rules/docs.md"
    "rules:rules/git.md:.claude/rules/git.md:.claude/rules/git.md"
    "rules:rules/reminders.md:.claude/rules/reminders.md:.claude/rules/reminders.md"
    # Hooks (Phase 1 — Tier 1)
    "hooks:hooks/-enforcer.py:.claude/hooks/-enforcer.py:.claude/hooks/-enforcer.py"
    "hooks:hooks/convention-loader.py:.claude/hooks/convention-loader.py:.claude/hooks/convention-loader.py"
    # Hooks (Phase 2 — Tier 2)
    "hooks:hooks/convention-enforcer.py:.claude/hooks/convention-enforcer.py:.claude/hooks/convention-enforcer.py"
    "hooks:hooks/check-reminders.sh:.claude/hooks/check-reminders.sh:.claude/hooks/check-reminders.sh"
    # Commands (Phase 2 — Tier 2)
    "commands:commands/recall.md:.claude/commands/recall.md:.claude/commands/recall.md"
    "commands:commands/remember.md:.claude/commands/remember.md:.claude/commands/remember.md"
    # Skills (project mode)
    "skills:skills/memory-maintenance:.claude/skills/memory-maintenance:.claude/skills/memory-maintenance"
    "skills:skills/preflight:.claude/skills/preflight:.claude/skills/preflight"
    "skills:skills/argo-wf-reference:.claude/skills/argo-wf-reference:.claude/skills/argo-wf-reference"
    "skills:skills/argo-debug:.claude/skills/argo-debug:.claude/skills/argo-debug"
    "skills:skills/cue-reference:.claude/skills/cue-reference:.claude/skills/cue-reference"
    # Skills (Phase 3 — Tier 2, parameterized)
    "skills:skills/refine-backlog:.claude/skills/refine-backlog:.claude/skills/refine-backlog"
    "skills:skills/align-epic-priority:.claude/skills/align-epic-priority:.claude/skills/align-epic-priority"
    "skills:skills/create-agent-spec-ticket:.claude/skills/create-agent-spec-ticket:.claude/skills/create-agent-spec-ticket"
    "skills:skills/app-cluster-investigate:.claude/skills/app-cluster-investigate:.claude/skills/app-cluster-investigate"
    # Skills — user-home only (link_rel_project = "-")
    "skills:skills/setup-ai-env:-:.claude/skills/setup-ai-env"
    # Agent guides (Phase 1 — Tier 1)
    # Agent guides (Phase 2 — Tier 2)
    # .md, .md, and repository-operations-*.md are installed in user-home mode
    # so that convention-loader.py / convention-enforcer.py can locate them when running
    # as a user-level hook (without a project-scoped agents/ directory on the search path).
    "agents:agents/repository-operations.md:agents/repository-operations.md:.claude/agents/repository-operations.md"
    "agents:agents/repository-operations.md:agents/repository-operations.md:.claude/agents/repository-operations.md:github"
    # Docs (project mode only; link_rel_user_home = "-")
    "docs:docs/ai/claude-code-setup.md:docs/ai/claude-code-setup.md:-"
    "docs:docs/ai/claude-project-config.md:docs/ai/claude-project-config.md:-"
    "docs:docs/ai/infinite-context-memory.md:docs/ai/infinite-context-memory.md:-"
    "docs:docs/ai/maintenance.md:docs/ai/maintenance.md:-"
    "docs:docs/ai/mcp-servers.md:docs/ai/mcp-servers.md:-"
    "docs:docs/ai/prompt-cost-reduction.md:docs/ai/prompt-cost-reduction.md:-"
    "docs:docs/ai/cli-tools.md:docs/ai/cli-tools.md:-"
    "docs:docs/ai/overview.md:docs/ai/overview.md:-"
    "docs:docs/ai/claude-skills.md:docs/ai/claude-skills.md:-"
    "docs:docs/ai/onboarding-checklist.md:docs/ai/onboarding-checklist.md:-"
    # Root (project mode only; link_rel_user_home = "-")
    "root:.mcp.json:.mcp.json:-"
    "root:.markdownlint.json:.markdownlint.json:-"
)

# ---------------------------------------------------------------------------
# _resolve_consumer_dir — sets CONSUMER_DIR and MANIFEST_FILE / LOCK_FILE
# ---------------------------------------------------------------------------
_resolve_paths() {
    if $USER_HOME_MODE; then
        CONSUMER_DIR="$HOME"
    else
        CONSUMER_DIR="$(cd "$PROJECT_PATH" && pwd)"
    fi
    MANIFEST_FILE="${CONSUMER_DIR}/.claude/.ai-tooling-manifest.json"
    LOCK_FILE="${CONSUMER_DIR}/.claude/.ai-tooling.lock"
}

# ---------------------------------------------------------------------------
# _asset_list — echoes "type source_abs link_abs link_rel source_rel" tuples
# for every asset relevant to the current mode (project/user-home).
# ---------------------------------------------------------------------------
_asset_list() {
    local mode="$1" # "project" or "user-home"
    for entry in "${ASSET_MANIFEST[@]}"; do
        local atype src link_p link_uh vcs_filter
        IFS=':' read -r atype src link_p link_uh vcs_filter <<<"$entry"
        # VCS filter (5th field): skip if set and doesn't match current VCS
        if [[ -n "$vcs_filter" && "$vcs_filter" != "-" ]]; then
            [[ "$vcs_filter" == "$VCS" ]] || continue
        fi
        local link_rel
        if [[ "$mode" == "user-home" ]]; then
            link_rel="$link_uh"
        else
            link_rel="$link_p"
        fi
        # "-" means this asset does not apply in the current mode
        [[ "$link_rel" == "-" ]] && continue
        local source_abs="${SHARED_REPO_DIR}/${src}"
        local link_abs="${CONSUMER_DIR}/${link_rel}"
        echo "$atype|${src}|${source_abs}|${link_abs}|${link_rel}"
    done
}

# ---------------------------------------------------------------------------
# that needs a secondary .github/skills/ symlink when VCS=github.
# Only applicable in project mode.
# ---------------------------------------------------------------------------
    [[ "$VCS" == "github" ]] || return 0
    $USER_HOME_MODE && return 0

    for entry in "${ASSET_MANIFEST[@]}"; do
        IFS=':' read -r atype src link_p _link_uh <<<"$entry"
        [[ "$atype" != "skills" ]] && continue
        [[ "$link_p" == "-" ]] && continue
        local skill_name
        skill_name="$(basename "$src")"
        local source_abs="${SHARED_REPO_DIR}/${src}"
        local link_rel=".github/skills/${skill_name}"
        local link_abs="${CONSUMER_DIR}/${link_rel}"
        echo "${source_abs}|${link_abs}|${link_rel}"
    done
}

# ---------------------------------------------------------------------------
# _install_one — installs a single asset (symlink or copy).
# Prints one of: installed | skipped | conflict
# Args: source_abs link_abs link_rel source_rel
# ---------------------------------------------------------------------------
_install_one() {
    local source_abs="$1"
    local link_abs="$2"
    local link_rel="$3"
    local source_rel="$4"

    # Source must exist; skip silently if it doesn't (asset not yet committed)
    if [[ ! -e "$source_abs" ]]; then
        echo "missing"
        return
    fi

    # Idempotency: if a symlink already points to the correct target, skip.
    # Compare by resolved absolute path to handle both old absolute symlinks
    # and new relative ones correctly.
    if [[ -L "$link_abs" ]]; then
        local link_dir resolved_target
        link_dir="$(dirname "$link_abs")"
        resolved_target="$(cd "$link_dir" 2>/dev/null && realpath "$(readlink "$link_abs")" 2>/dev/null)" || true
        if [[ "$resolved_target" == "$source_abs" ]]; then
            echo "already-linked"
            return
        fi
        # Symlink exists but points elsewhere (or nowhere) — conflict
        if [[ -z "$resolved_target" ]]; then
            echo "conflict:dangling-symlink"
        else
            echo "conflict:symlink-elsewhere:${resolved_target}"
        fi
        return
    fi

    # Real file/dir already on disk — conflict
    if [[ -e "$link_abs" ]]; then
        echo "conflict:real-file"
        return
    fi

    # Create parent directory
    mkdir -p "$(dirname "$link_abs")"

    if $COPY_MODE; then
        cp -r "$source_abs" "$link_abs"
        # chmod 444 on files, leave dirs as-is
        if [[ -f "$link_abs" ]]; then
            chmod 444 "$link_abs"
        fi
        echo "copied"
    else
        # Use a relative symlink so the link works regardless of the absolute
        # path the consumer repo is cloned to (different developer machines).
        # Resolve the physical path of the parent directory (pwd -P) so that
        # symlinks in the path (for example, /tmp to /private/tmp on macOS) are
        # expanded before computing the relative offset. Without this, symlinks
        # created inside a HOME under /tmp would contain the wrong number of
        # leading "../" components and break at resolution time.
        local link_dir rel_source
        link_dir="$(cd "$(dirname "$link_abs")" && pwd -P)"
        rel_source="$(python3 -c "import os,sys; print(os.path.relpath(sys.argv[1],sys.argv[2]))" "$source_abs" "$link_dir")"
        ln -s "$rel_source" "$link_abs"
        echo "installed"
    fi
}

# ---------------------------------------------------------------------------
# _conflict_note — turn a conflict:* reason (with the "conflict:" prefix
# already stripped) into a short, human-readable explanation for the summary
# table's Note column.
# ---------------------------------------------------------------------------
_conflict_note() {
    local reason="$1"
    case "$reason" in
    real-file)
        echo "real file already exists — remove it or diff against the source, then re-run"
        ;;
    symlink-elsewhere:*)
        echo "symlink exists -> ${reason#symlink-elsewhere:} — remove it or re-point it, then re-run"
        ;;
    dangling-symlink)
        echo "broken symlink (target no longer exists, unrelated to this installer) — remove it, then re-run"
        ;;
    esac
}

# ---------------------------------------------------------------------------
# register_global_hooks — idempotently add the ai-tooling hooks to the
# user-global ~/.claude/settings.json. Existing entries are preserved.
# ---------------------------------------------------------------------------
register_global_hooks() {
    local settings="${HOME}/.claude/settings.json"
    mkdir -p "$(dirname "$settings")"
    python3 "${SCRIPT_DIR}/register-hooks.py" "$settings"
}

# ---------------------------------------------------------------------------
# unregister_global_hooks — remove the ai-tooling hooks from the
# user-global settings.json. No-op if the file is absent.
# ---------------------------------------------------------------------------
unregister_global_hooks() {
    local settings="${HOME}/.claude/settings.json"
    if [[ ! -f "$settings" ]]; then
        return 0
    fi
    python3 "${SCRIPT_DIR}/register-hooks.py" "$settings" --unregister
}

# ---------------------------------------------------------------------------
# set_caveman_env — idempotently append CAVEMAN_DEFAULT_MODE=full to the
# active shell's RC file so the caveman plugin auto-activates at full
# compression on every Claude Code session without needing a config.json.
#
# Detects: zsh → ~/.zshrc, bash (macOS) → ~/.bash_profile,
#          bash (Linux) → ~/.bashrc, fish → ~/.config/fish/config.fish.
# Falls back to ~/.profile when the shell is unknown.
# ---------------------------------------------------------------------------
set_caveman_env() {
    local shell_name
    shell_name="$(basename "${SHELL:-}")"

    local rc_file
    case "$shell_name" in
    zsh)  rc_file="${HOME}/.zshrc" ;;
    bash)
        if [[ "$(uname -s)" == "Darwin" ]]; then
            rc_file="${HOME}/.bash_profile"
        else
            rc_file="${HOME}/.bashrc"
        fi
        ;;
    fish) rc_file="${HOME}/.config/fish/config.fish" ;;
    *)    rc_file="${HOME}/.profile" ;;
    esac

    # Idempotency: skip if any form of the variable is already declared.
    if grep -qF "CAVEMAN_DEFAULT_MODE" "$rc_file" 2>/dev/null; then
        echo "caveman: CAVEMAN_DEFAULT_MODE already set in ${rc_file} — skipped"
        return
    fi

    mkdir -p "$(dirname "$rc_file")"

    if [[ "$shell_name" == "fish" ]]; then
        printf '\nset -x CAVEMAN_DEFAULT_MODE full  # added by ai-tooling installer\n' >> "$rc_file"
    else
        printf '\nexport CAVEMAN_DEFAULT_MODE=full  # added by ai-tooling installer\n' >> "$rc_file"
    fi

    echo "caveman: added CAVEMAN_DEFAULT_MODE=full to ${rc_file}"
    echo "         Open a new terminal or run: source ${rc_file}"
}

# ---------------------------------------------------------------------------
# do_install
# ---------------------------------------------------------------------------
do_install() {
    _resolve_paths

    local mode_label
    $USER_HOME_MODE && mode_label="user-home" || mode_label="project"

    local target_label
    $USER_HOME_MODE && target_label="user-home" || target_label="$(basename "$CONSUMER_DIR")"

    local link_mode
    $COPY_MODE && link_mode="copy" || link_mode="symlink"

    if $COPY_MODE; then
        echo "WARN: Symlinks unavailable. Installing copies. These files will NOT automatically reflect" \
            "upstream changes. Run install again after pulling ai-tooling to update." >&2
    fi

    # Collect git metadata
    local shared_branch shared_commit shared_sha
    shared_branch="$(git -C "$SHARED_REPO_DIR" branch --show-current 2>/dev/null || echo "unknown")"
    shared_commit="$(git -C "$SHARED_REPO_DIR" rev-parse HEAD 2>/dev/null || echo "unknown")"
    shared_sha="${shared_commit:0:8}"

    local now
    now="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

    # Tracking arrays for the summary table
    declare -a summary_rows=()
    declare -a manifest_entries=()
    local hooks_installed=false

    # --- Phase 1: Claude-side assets ---
    while IFS='|' read -r atype source_rel source_abs link_abs link_rel; do
        local status
        status="$(_install_one "$source_abs" "$link_abs" "$link_rel" "$source_rel")"
        local display_status="$status"
        local note=""
        case "$status" in
        installed | copied)
            [[ "$atype" == "hooks" ]] && hooks_installed=true
            manifest_entries+=("{\"link\":\"${link_rel}\",\"source\":\"${source_rel}\",\"mode\":\"${link_mode}\"}")
            display_status="$($COPY_MODE && echo 'copied' || echo 'installed')"
            ;;
        already-linked)
            [[ "$atype" == "hooks" ]] && hooks_installed=true
            display_status="already-linked"
            ;;
        conflict:*)
            display_status="CONFLICT"
            note="$(_conflict_note "${status#conflict:}")"
            ;;
        missing)
            display_status="missing-src"
            note="source not yet in shared repo — will be available in a future commit"
            ;;
        esac
        summary_rows+=("$link_rel|$display_status|$link_mode|$note")
    done < <(_asset_list "$mode_label")

    while IFS='|' read -r source_abs link_abs link_rel; do
        local status
        status="$(_install_one "$source_abs" "$link_abs" "$link_rel" "")"
        local display_status="$status"
        local note=""
        case "$status" in
        installed | copied)
            manifest_entries+=("{\"link\":\"${link_rel}\",\"source\":\"skills/$(basename "$source_abs")\",\"mode\":\"${link_mode}\"}")
            display_status="$($COPY_MODE && echo 'copied' || echo 'installed')"
            ;;
        already-linked)
            display_status="already-linked"
            ;;
        conflict:*)
            display_status="CONFLICT"
            note="$(_conflict_note "${status#conflict:}")"
            ;;
        missing)
            display_status="missing-src"
            note="source not yet in shared repo — will be available in a future commit"
            ;;
        esac

    # --- Write manifest ---
    mkdir -p "$(dirname "$MANIFEST_FILE")"

    local entries_json
    if [[ "${#manifest_entries[@]}" -eq 0 ]]; then
        entries_json="[]"
    else
        entries_json="$(printf '%s\n' "${manifest_entries[@]}" | paste -sd ',' - | sed 's/^/[/;s/$/]/')"
    fi

    local new_install
    new_install="$(jq -n \
        --arg mode "$mode_label" \
        --arg target "$target_label" \
        --arg created_at "$now" \
        --argjson entries "$entries_json" \
        '{"mode":$mode,"target":$target,"created_at":$created_at,"entries":$entries}')"

    if [[ -f "$MANIFEST_FILE" ]]; then
        local tmp_manifest
        tmp_manifest="$(mktemp)"
        jq --argjson new_install "$new_install" \
            'if (.installs | map(select(.mode == $new_install.mode and .target == $new_install.target)) | length) > 0
             then .installs |= map(if .mode == $new_install.mode and .target == $new_install.target then $new_install else . end)
             else .installs += [$new_install]
             end' "$MANIFEST_FILE" >"$tmp_manifest"
        mv "$tmp_manifest" "$MANIFEST_FILE"
    else
        jq -n \
            --argjson new_install "$new_install" \
            '{"version":1,"installs":[$new_install]}' >"$MANIFEST_FILE"
    fi

    # --- Write lock file (minimal — the only artifact consumer repos may optionally track) ---
    # Branch, timestamp, and mode live in the manifest only (untracked).
    jq -n \
        --arg commit "$shared_commit" \
        '{"version":1,"shared_repo_commit":$commit}' \
        >"$LOCK_FILE"

    # --- Print summary table ---
    echo ""
    echo "ai-tooling install complete (${mode_label} mode — ${target_label})"
    echo "Shared repo: ${SHARED_REPO_DIR} @ ${shared_sha} (${shared_branch})"
    echo ""
    echo "Asset Deployment Summary"
    printf "%-55s %-14s %s\n" "Asset" "Status" "Mode"
    printf '%0.s-' {1..85}
    echo ""
    for row in "${summary_rows[@]+"${summary_rows[@]}"}"; do
        IFS='|' read -r asset st lm note <<<"$row"
        printf "%-55s %-14s %s\n" "$asset" "$st" "$lm"
        [[ -n "$note" ]] && printf "  -> %s\n" "$note"
    done
    echo ""

    # --- Register hooks in ~/.claude/settings.json (idempotent) ---
    if $hooks_installed; then
        echo ""
        register_global_hooks
    fi

    # --- Set CAVEMAN_DEFAULT_MODE=full in shell RC (idempotent) ---
    echo ""
    set_caveman_env

    # --- Post-install reminder ---
    if ! $USER_HOME_MODE && $hooks_installed; then
        cat <<'REMINDER'

Next steps:
  1. Create .claude/convention-enforcer-config.json with:
  2. Install Claude Code plugins (two-step sequence required for each):
        claude plugin marketplace add anthropics/claude-plugins-official
        claude plugin marketplace add JuliusBrussee/caveman
        claude plugin install caveman@caveman
     and fill in the real field IDs. See agents/.md §Consumer-specific defaults.
REMINDER
    fi
}

# ---------------------------------------------------------------------------
# do_unlink
# ---------------------------------------------------------------------------
do_unlink() {
    _resolve_paths

    if [[ ! -f "$MANIFEST_FILE" ]]; then
        echo "Nothing to unlink (manifest not found: $MANIFEST_FILE)"
        exit 0
    fi

    # Collect all entries from all install records in the manifest
    local total=0 removed=0

    while IFS= read -r link_rel; do
        local link_abs="${CONSUMER_DIR}/${link_rel}"
        ((total++)) || true
        if [[ -L "$link_abs" ]]; then
            unlink "$link_abs"
            echo "Unlinked: $link_rel"
            ((removed++)) || true
        elif [[ -e "$link_abs" ]]; then
            rm -rf "$link_abs"
            echo "Removed:  $link_rel"
            ((removed++)) || true
        else
            echo "Already removed: $link_rel"
        fi
    done < <(jq -r '.installs[].entries[].link' "$MANIFEST_FILE")

    rm -f "$MANIFEST_FILE"
    echo "Manifest removed: $MANIFEST_FILE"

    # Remove the ai-tooling hooks from ~/.claude/settings.json (idempotent).
    echo ""
    unregister_global_hooks

    # The lock file is intentionally preserved: consumer repos may commit it
    # to track which shared-tooling revision is pinned, so removing it on
    # unlink would silently drop a tracked file from disk.
    if [[ -f "$LOCK_FILE" ]]; then
        echo "Lock file preserved (not removed): $LOCK_FILE"
    fi

    echo ""
    echo "Unlinked ${removed} of ${total} entries. Manifest removed. Lock file preserved."
}

# ---------------------------------------------------------------------------
# do_dry_run
# ---------------------------------------------------------------------------
do_dry_run() {
    _resolve_paths

    local mode_label
    $USER_HOME_MODE && mode_label="user-home" || mode_label="project"

    echo ""
    echo "[DRY RUN] ai-tooling install preview (${mode_label} mode)"
    echo "[DRY RUN] Shared repo: ${SHARED_REPO_DIR}"
    echo "[DRY RUN] Consumer:    ${CONSUMER_DIR}"
    echo ""

    local link_mode
    $COPY_MODE && link_mode="copy" || link_mode="symlink"

    local had_output=false

    # Claude-side assets
    while IFS='|' read -r _atype _source_rel source_abs link_abs link_rel; do
        had_output=true
        if [[ ! -e "$source_abs" ]]; then
            printf "[DRY RUN] Would skip:    %-50s (source not in shared repo yet)\n" "$link_rel"
        elif [[ -L "$link_abs" ]]; then
            local link_dir resolved_target
            link_dir="$(dirname "$link_abs")"
            resolved_target="$(cd "$link_dir" 2>/dev/null && realpath "$(readlink "$link_abs")" 2>/dev/null)" || true
            if [[ "$resolved_target" == "$source_abs" ]]; then
                printf "[DRY RUN] Already done: %-50s (symlink already correct)\n" "$link_rel"
            elif [[ -z "$resolved_target" ]]; then
                printf "[DRY RUN] Would skip:    %-50s (symlink exists but target is missing — broken link, not managed by this installer)\n" "$link_rel"
            else
                printf "[DRY RUN] Would skip:    %-50s (symlink exists → %s)\n" "$link_rel" "$resolved_target"
            fi
        elif [[ -e "$link_abs" ]]; then
            printf "[DRY RUN] Would skip:    %-50s (real file already exists — conflict)\n" "$link_rel"
        else
            printf "[DRY RUN] Would install: %-50s → %s → %s\n" "$link_rel" "$link_mode" "$source_abs"
        fi
    done < <(_asset_list "$mode_label")

    while IFS='|' read -r source_abs link_abs link_rel; do
        had_output=true
        if [[ ! -e "$source_abs" ]]; then
            printf "[DRY RUN] Would skip:    %-50s (source not in shared repo yet)\n" "$link_rel"
        elif [[ -L "$link_abs" ]]; then
            local link_dir resolved_target
            link_dir="$(dirname "$link_abs")"
            resolved_target="$(cd "$link_dir" 2>/dev/null && realpath "$(readlink "$link_abs")" 2>/dev/null)" || true
            if [[ "$resolved_target" == "$source_abs" ]]; then
                printf "[DRY RUN] Already done: %-50s (symlink already correct)\n" "$link_rel"
            elif [[ -z "$resolved_target" ]]; then
                printf "[DRY RUN] Would skip:    %-50s (symlink exists but target is missing — broken link, not managed by this installer)\n" "$link_rel"
            else
                printf "[DRY RUN] Would skip:    %-50s (symlink exists → %s)\n" "$link_rel" "$resolved_target"
            fi
        elif [[ -e "$link_abs" ]]; then
            printf "[DRY RUN] Would skip:    %-50s (real file already exists — conflict)\n" "$link_rel"
        else
        fi

    if ! $had_output; then
        echo "[DRY RUN] No assets apply for this mode."
    fi
    echo ""
    echo "[DRY RUN] Would register hooks in ${HOME}/.claude/settings.json:"
    echo "[DRY RUN]   PreToolUse[Bash]: python .claude/hooks/convention-enforcer.py"
    echo "[DRY RUN]   UserPromptSubmit: python .claude/hooks/convention-loader.py"
    echo "[DRY RUN]   UserPromptSubmit: bash .claude/hooks/check-reminders.sh"
    echo "[DRY RUN]   SessionStart: bash .claude/hooks/check-reminders.sh --force"
    echo ""
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
PROJECT_PATH=""
USER_HOME_MODE=false
COPY_MODE=false
UNLINK_MODE=false
DRY_RUN=false
VCS=""
HELP=false

while [[ $# -gt 0 ]]; do
    case "$1" in
    --project)
        if [[ $# -lt 2 || "$2" == --* ]]; then
            echo "ERROR: --project requires a path argument." >&2
            exit 1
        fi
        PROJECT_PATH="$2"
        shift 2
        ;;
    --user-home)
        USER_HOME_MODE=true
        shift
        ;;
    --copy)
        COPY_MODE=true
        shift
        ;;
    --unlink)
        UNLINK_MODE=true
        shift
        ;;
    --dry-run)
        DRY_RUN=true
        shift
        ;;
    --vcs)
        if [[ $# -lt 2 || "$2" == --* ]]; then
            exit 1
        fi
        case "$2" in
        *)
            exit 1
            ;;
        esac
        shift 2
        ;;
    --help | -h)
        HELP=true
        shift
        ;;
    *)
        echo "ERROR: Unknown flag: $1" >&2
        echo "Run '$(basename "$0") --help' for usage." >&2
        exit 1
        ;;
    esac
done

# Handle --help before any validation so it always succeeds
if $HELP; then
    usage
    exit 0
fi

# ---------------------------------------------------------------------------
# Validate: exactly one of --project / --user-home is required
# ---------------------------------------------------------------------------
if $USER_HOME_MODE && [[ -n "$PROJECT_PATH" ]]; then
    echo "ERROR: --project and --user-home are mutually exclusive; specify exactly one." >&2
    exit 1
fi

if ! $USER_HOME_MODE && [[ -z "$PROJECT_PATH" ]]; then
    echo "ERROR: One of --project <path> or --user-home is required." >&2
    echo "Run '$(basename "$0") --help' for usage." >&2
    exit 1
fi

# ---------------------------------------------------------------------------
# Preflight (skipped for --dry-run)
# ---------------------------------------------------------------------------
if ! $DRY_RUN; then
    preflight_check
fi

# ---------------------------------------------------------------------------
# Project-mode only checks
# ---------------------------------------------------------------------------
if [[ -n "$PROJECT_PATH" ]]; then
    # Verify the project path exists and is accessible
    if [[ ! -d "$PROJECT_PATH" ]]; then
        echo "ERROR: --project path does not exist or is not a directory: $PROJECT_PATH" >&2
        exit 1
    fi

    # Sibling-directory check (only when not copying)
    if ! $COPY_MODE; then
        sibling_dir_check
    fi

    # VCS auto-detection (only when --vcs not explicitly set)
    if [[ -z "$VCS" ]]; then
        detect_vcs
    fi
fi

# ---------------------------------------------------------------------------
# Dispatch
# ---------------------------------------------------------------------------
if $UNLINK_MODE; then
    do_unlink
elif $DRY_RUN; then
    do_dry_run
else
    do_install
fi