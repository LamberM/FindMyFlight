# Git / Repository Conventions

The VCS platform and CLI for each consumer repo are documented in `agents/repository-operations.md`. Read those files before performing any git or PR operation.

## Branch naming

```text
feature/TICKET-XXXXX-descriptive-name
<bug-fix-prefix>/TICKET-XXXXX-descriptive-name
```

The bug-fix prefix differs by platform — see `agents/repository-operations.md` § *Branch Naming*
for the full table.

Use `NOISSUE` only when there is genuinely no associated ticket.

## Commit messages

[Conventional Commits](https://www.conventionalcommits.org/) format with an inline ticket reference:

```text
<type>: <message>, ref: TICKET-XXXXX
```

| Type | When to use |
|---|---|
| `feat` | New functionality |
| `fix` | Bug fix |
| `docs` | Documentation-only changes |
| `style` | Formatting, whitespace |
| `refactor` | Code restructuring (no feature, no fix) |
| `perf` | Performance improvement |
| `test` | Adding or adjusting tests |
| `build` | Build system or dependency changes |
| `ci` | CI configuration changes |
| `chore` | Maintenance tasks |
| `revert` | Reverting a previous commit |

Renovate-generated commits use the `RENOVATE:` prefix — never use it manually.

### CI pipeline skip headers

Some consumer repos support special headers prepended to the commit (and PR) title that bypass expensive CI pipeline steps. Check `agents/repository-operations.md` § *CI Pipeline Skip Headers* for the full list. When composing a commit for docs-only changes or when the user mentions phrases like "skip sandbox" or "override tests", ask whether to apply one.

## PR title

```text
TICKET-XXXXX <concise summary>
```

The summary is free-form; it does **not** need conventional-commit format. The ticket prefix is **mandatory** unless the work is truly `NOISSUE`.

## PR description

Every PR body **must** contain both of the following — neither alone is acceptable:

1. **Executive summary** — 2–4 sentences explaining *what* changed and *why*.
2. **Bullet points** — key changes highlighted for reviewers; focus on intent, not file paths.

Additional rules:

- Inline code (backticks) for tool/file/flag names; **bold** for emphasis.
- Do **not** repeat the ticket number at the end of the body — it is already in the title.

When AI-assisted, end the body with attribution after a `---` separator (per the
[Linux kernel coding-assistants attribution](https://docs.kernel.org/process/coding-assistants.html#attribution)):

```text
---
Assisted-by: Claude Code:claude-opus-4-7
```

## Branch reuse

**Never reuse a branch whose PR has already been merged.** When the previous PR for a branch is merged, that branch's history is part of `master` — committing on it again and opening a new PR produces a dirty diff that includes all the already-merged changes. Always create a **new branch** (with a new descriptive suffix if needed) for any follow-up work, even if it targets the same ticket.

## Pre-push checks

2. `git fetch origin` and rebase if behind.
3. Verify the PR is not already merged — use the VCS-specific repository-operations file for the command.
4. **Verify the branch has no previously merged PR** — use the command in the VCS-specific repository-operations file for your repo's host.
5. Always set the upstream on the first push: `--set-upstream origin <branch>`.
6. Never `--force`, `--force-with-lease`, or `commit --amend` on `master` / `main`.

## Post-PR

After opening a PR, **ask the user** whether to post a  notification — see `agents/.md` for  notification conventions. Do not post unless the user confirms.
