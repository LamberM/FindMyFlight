# Repository Operations — GitHub (`gh` CLI)

Conventions for branch naming, commit format, PR content, `gh` CLI commands, and pre-push safety checks.

## Branch Naming

Use a prefix indicating the type of change, followed by the ticket number and a short kebab-case description:

```text
feature/TICKET-XXXXX-descriptive-name
fix/TICKET-XXXXX-descriptive-name
```

Use `fix/` for bug fixes and `feature/` for new functionality.

Examples:

```text
feature/TICKET-23181-xrf-domains-management
fix/TICKET-25537-duplicate-error
```

## Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) format with an **inline** ticket reference:

```text
<type>: <message>, ref: TICKET-XXXXX
```

Use `NOISSUE` when there is no associated ticket.

### Commit Types

| Type       | When to use                                                  |
|------------|--------------------------------------------------------------|
| `feat`     | New functionality                                            |
| `fix`      | Bug fix                                                      |
| `docs`     | Documentation-only changes                                   |
| `style`    | Formatting, whitespace (no logic changes)                    |
| `refactor` | Code restructuring (no new feature, no bug fix)              |
| `perf`     | Performance improvement                                      |
| `test`     | Adding or adjusting tests                                    |
| `build`    | Build system or dependency changes                           |
| `ci`       | CI configuration changes                                     |
| `chore`    | Maintenance tasks (no source or test changes)                |
| `revert`   | Reverting a previous commit                                  |

### Fixup Commits

When making fixes or adjustments to an **already opened** PR (addressing review comments, follow-up tweaks, build fixes), create **fixup commits** rather than regular commits:

```bash
git commit --fixup <target-commit-sha>
```

When squashing before merge:

```bash
git rebase --interactive --autosquash HEAD~<N>
```

## Pull Request Conventions & `gh` CLI Usage

### PR Title

Always include the ticket reference as a prefix:

```text
<TICKET-XXXXX> <concise summary of the change>
```

### Creating a Pull Request (`gh pr create`)

Write the body to a temp file first to avoid shell quoting issues:

```bash
cat > /tmp/pr-body.txt <<'EOF'
Executive summary in 2-4 sentences explaining what changed and why.

- Bullet points highlighting key changes.
- Focus on intent and impact, not file paths.

---
Assisted-by: Claude Code:claude-sonnet-4-6
EOF

gh pr create \
  --title "TICKET-XXXXX <summary>" \
  --body "$(cat /tmp/pr-body.txt)"
```

### Checking & Editing Pull Requests

```bash
# Check PR state
gh pr view <branch-or-number> --json state,title,url

# List open PRs
gh pr list --state open

# Edit PR body
gh pr edit <number> --body "$(cat /tmp/pr-body.txt)"
```

### Branch Reuse Check

Before pushing or opening a PR, confirm the branch has not already been merged:

```bash
gh pr list --head <branch> --state merged --json number | jq '.[].number'
```

Non-empty output means the branch was already merged — stop and create a new branch off `main`.

## Pre-Push Safety Checks

1. **Confirm ticket reference:** Ensure `TICKET-XXXXX` prefix in PR title.
2. **Fetch remote state:** Run `git fetch origin` and rebase if behind.
3. **Branch reuse check:** Verify the branch has no previously merged PR.
4. **Set upstream:** Always push with `--set-upstream origin <branch>` on first push.
5. **Never force-push on default branch:** Never `--force` or `commit --amend` on `main` / `master`.
