#!/bin/bash
# Prints pending reminders from reminders.md.
# With --force: always prints. Without: prints only if 30+ min since last display.

REMINDERS_FILE="/root/.claude/reminders.md"
LAST_SHOWN_FILE="/root/.claude/.reminder_last_shown"
FORCE="${1:-}"
TODAY=$(date +%F)

[ -f "$REMINDERS_FILE" ] || exit 0

due=$(awk -v today="$TODAY" '/^- \[ \] [0-9]{4}-[0-9]{2}-[0-9]{2} \|/ { d=$4; if (d <= today) print }' "$REMINDERS_FILE")
[ -n "$due" ] || exit 0

should_show=false
if [ "$FORCE" = "--force" ]; then
    should_show=true
elif [ ! -f "$LAST_SHOWN_FILE" ]; then
    should_show=true
else
    last_shown=$(cat "$LAST_SHOWN_FILE")
    now=$(date +%s)
    elapsed=$(( now - last_shown ))
    [ "$elapsed" -ge 1800 ] && should_show=true
fi

if [ "$should_show" = "true" ]; then
    echo "PENDING REMINDERS — you MUST display these to the user verbatim in your next response before doing anything else:"
    echo "$due" | sed 's/^- \[ \] /  • /'
    echo "(Only mark a reminder done when the user explicitly says the task is complete.)"
    date +%s > "$LAST_SHOWN_FILE"
fi