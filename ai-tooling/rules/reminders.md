# Reminders

## Storing a reminder

When the user asks to be reminded of something, append a checkbox line to `~/.claude/reminders.md`:

```text
- [ ] YYYY-MM-DD | <description>
```

Use the date the reminder becomes relevant (today's date if unspecified). This is the **only**
storage method for reminders — do not rely on ICM alone or on ad-hoc notes elsewhere; those are
not wired to any hook and will not resurface reliably.

## Surfacing reminders

The `hooks/check-reminders.sh` hook (registered on `SessionStart` with `--force`, and on every
`UserPromptSubmit` without it) scans `reminders.md` for unchecked lines whose date has arrived and
prints them as a `PENDING REMINDERS` block. `UserPromptSubmit` throttles repeats to once per 30
minutes; `SessionStart --force` always prints if any are due.

When a `PENDING REMINDERS` block appears in system context, always echo it to the user in the
response — they cannot see `<system-reminder>`-style tags directly.

## Closing out a reminder

Mark a reminder done by changing `[ ]` to `[x]` — only when the user explicitly confirms the
task is complete. Never mark one done speculatively or because the related work merely started.
