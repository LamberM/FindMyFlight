# Agent self-healing directive

## When this rule applies

Act immediately when **any** of the following is true:

- The user flags a recurring bug or broken pattern (e.g. "this keeps happening", "we fixed this before")
- You identify a rule that consistently produces incorrect output
- You discover a workaround that must be permanent — not just valid for this session
- You are about to write to ICM a fact that should instead be a permanent source-file edit

## Directive

**Edit the relevant source file now. Do not defer.**

When a fix belongs in a permanent file:

1. Identify the file to update in the current repo's structure: `rules/`, `agents/`, or a skill file under `.claude/skills/` or `skills/` (whichever path exists in this repo).
2. Make the edit using the `Edit` or `Write` tool.
3. Continue the current task without interruption.

Never do these instead:

- Store the fix only in ICM
- Add a comment to the user ("I'll remember this") without also editing the source
- Defer with "I'll update the rules file later" or "remind me after we finish"

## Landing the fix in `ai-tooling`

A self-healing edit **must not stay a local working-tree change**. When the file to fix
lives in the `ai-tooling` repo (`rules/`, `agents/`, or a skill under `.claude/skills/`
or `skills/`), land it through a pull request every time the directive fires — never leave
it uncommitted on `main`, and never commit directly to `main`:

1. Make the edit immediately (per the Directive above) — do **not** defer the content.
2. Create a feature branch off `main`: `fix/NOISSUE-<slug>` (or `feature/<slug>`, or use
   `TICKET-XXXXX` in place of `NOISSUE` when a ticket applies).
3. Stage **only** the files changed by this fix — never sweep unrelated pre-existing
   working-tree changes into the same branch. If unrelated local edits are present, flag
   them to the user rather than committing them.
4. Commit via the `/git-commit` skill and open a PR via `/pull-request-summary` — never
   bypass with raw `git commit` / `gh pr create`.
5. Report the PR link to the user.

For fixes in any **other** repo, follow that repo's own contribution conventions
(`agents/repository-operations-*.md`), applying the same "branch + PR, never direct to the
default branch" principle.

## Ambiguous cases

If you are unsure which file should own the fix, ask:

> "Which file should I update to make this permanent?"

Once the user answers, edit that file immediately, then continue.
If the relevant path does not exist in the current repo, ask the user which file
to update before proceeding.
