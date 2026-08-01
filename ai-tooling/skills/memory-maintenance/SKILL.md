---
name: memory-maintenance
description: >
  Daily ICM memory garden maintenance. Run a full audit of all memories:
  detect duplicates, enrich weak keywords, consolidate stale entries,
  verify accuracy, and report the garden's health. Triggers: "konserwacja
  pamięci", "memory maintenance", "pielęgnacja pamięci", "audyt pamięci",
  "memory garden".
---

# ICM Memory Maintenance

Perform a systematic audit and improvement of the ICM memory garden.
Work through every step. Report only a concise summary at the end.

!!! warning "Windows / PowerShell: use `icm.exe`, not `icm`"
    In PowerShell, `icm` is a built-in alias for `Invoke-Command`. Every bare `icm` call
    will fail with a parameter-binding error. **Use `icm.exe` for every command in this
    skill when running in a PowerShell terminal.** In a bash/Claude Code context, bare
    `icm` works fine.

## Step 0 — Volume check and bulk prune

Run before any pair-by-pair work. A garden with many low-weight entries won't converge on manual review alone.

```bash
icm decay
icm prune --dry-run --threshold 0.5
```

Read the candidate count at the end of the dry-run output. If it exceeds 50, execute the prune:

```bash
icm prune --threshold 0.5
```

`prune` skips `critical` and `high` importance memories regardless of weight — curated entries survive.

Run `icm stats` before and after to capture the delta for the Step 10 report.

## Step 1 — Inventory

Run `icm topics` first to see the size of each topic:

```bash
icm topics
```

Then do a per-topic scan to enumerate all memories:

```bash
for topic in $(icm topics --no-header 2>/dev/null | awk '{print $1}'); do
  icm recall "${topic}" --limit 200 --no-embeddings -t "${topic}"
done
```

Or in PowerShell:

```powershell
icm.exe topics | Select-Object -Skip 2 | ForEach-Object {
  $t = ($_ -split '\s+')[0]
  icm.exe recall $t --limit 200 --no-embeddings -t $t
}
```

**If any topic has more than 500 entries:** skip pair-by-pair review for that topic. Sample it with `icm recall "<topic>" -t "<topic>" --limit 20` to check quality, then use `icm forget --topic <T>` for topics that are clearly junk, or defer to a scheduled `icm prune` run. The pair-by-pair workflow in Steps 2–5 won't converge on a topic that size in one session.

Note every memory ID, topic, importance, keyword list, and creation date for the topics you will review.

The `icm recall "." --limit 100` shortcut is useful for a quick semantic sample but covers at most the top-100 by cosine similarity. Use the per-topic scan above for a complete picture.

## Step 2 — Detect duplicates

Two memories are duplicates if they describe the same fact or preference.
Overlapping topic coverage is acceptable — true duplicates have near-identical
*content*, not just the same topic.

For each duplicate pair:

1. Keep the richer entry (more keywords, more detail, higher importance).
2. Merge any unique facts from the weaker entry into the keeper.
3. Delete the weaker entry: `icm forget <ID>`

## Step 3 — Keyword enrichment

For each memory, ask: "What terms would I type to need this?"
A well-keyworded memory should cover:

- All proper nouns (names, cluster names, ticket IDs, tool names)
- Common synonyms and abbreviations
- The *question* the memory answers (e.g. "how to tell 2nd gen from 3rd gen")

**Language rule:** All memory *content* (summary body) must be in English. Keywords may
include Polish phrases as search aids for Polish-language queries, but the summary must
be English-only. If you encounter Polish content in a memory during this step, translate
it in-place using `icm update`.

Update memories that have fewer than 8 keywords or are missing obvious terms:

```bash
icm update <ID> -c "<existing content verbatim>" -k "<enriched keyword list>"
```

**Rule:** `-c`/`--content` is a *required* argument — `icm update` cannot be called with
`--keywords` alone, even if you only intend to change keywords. Always supply the full
existing content verbatim. Never truncate.

## Step 4 — Accuracy check

For memories referencing file paths, binary names, or CLI tools:

- Verify the path/binary still exists using `ls` or `which`.
- If stale: update the content with the correct path, or add a note.

For memories referencing  tickets or external state (TICKET-*, TEMPLATE-*):

- Do NOT auto-verify (would require live API calls). Instead, check the
  `accessed` counter. If a memory has been accessed 0 times and is older
  than 30 days, flag it in the report as "candidate for staleness review".

## Step 5 — Importance calibration

Review importance levels against these rules:

- `critical`: user preferences, behavioral instructions — never decays
- `high`: active project context, architecture decisions — slow decay
- `medium`: reference info, background context — normal decay
- `low`: transient facts, FYI notes — fast decay

Adjust any mismatched entries:

```bash
icm update <ID> -c "<existing content verbatim>" -i <new-importance>
```

**Rule:** Same as Step 3 — `--content` is required even when only changing importance.

## Step 6 — Topic hygiene

Run `icm health` and review the output.

If a topic is flagged for consolidation, **do not consolidate blindly**. The
`icm health` flag is a count-based threshold artifact — it fires whenever a
topic has ≥5 entries, regardless of actual redundancy. Before acting:

1. List the entries in the flagged topic.
2. For each candidate pair, ask: do they describe *the same fact or preference*
   with near-identical content? If yes → merge (same as Step 2 duplicate
   handling). If no → leave them separate and note in the report that the flag
   is a threshold artifact, not true redundancy.
3. Only merge entries that pass the content-similarity test from Step 2.
   Never merge just to clear the flag.

If a topic name is unclear or inconsistently named, update affected memories
with a cleaner topic name using `icm update`.

!!! warning "Never run `icm consolidate` / `icm_memory_consolidate`"
    The bulk consolidation commands fire on the same count threshold and can collapse
    semantically distinct memories. Always work pair-by-pair using `icm update` /
    `icm forget` after the content-similarity check above.

## Step 7 — Embed sync

If any memories were updated or created during this session, run:

```bash
icm embed 2>/dev/null || true
```

In PowerShell:

```powershell
icm.exe embed 2>&1 | Out-Null
```

This ensures semantic search (vector embeddings) is up to date.
`embed-all` is not a valid subcommand — the correct command is `embed`.
Silently ignore if the command fails.

## Step 8 — Memoir maintenance

Memoirs hold permanent, structured reference knowledge as concept graphs — a separate layer
from raw memories that requires its own periodic review. Work through all four sub-steps
for every memoir found.

### 8a — Inventory

```bash
icm memoir list
```

For each memoir returned, get per-memoir stats:

```bash
icm memoir show <name>
```

Note: concept count, creation date, label breakdown. This replaces any hardcoded list —
always derive the memoir list dynamically.

### 8d — Promote memories → memoir (distill & manual)

Check whether any raw memory topics have accumulated stable, session-independent knowledge
that belongs in a memoir. Two pathways:

**Assisted** (AI-driven batch promotion):

```bash
icm memoir distill --from-topic <topic> --into <memoir>
```

Review the distill output — it may create duplicate concepts. Inspect afterwards.

**Manual** (single fact promotion):

```bash
icm memoir add-concept --memoir <name> --name "<concept>" --definition "<def>"
icm memoir link --memoir <name> --from "<concept>" --to "<related>" --relation <type>
```

After promotion, decide on the source memory:

- Still useful as a quick-recall entry → `icm update <ID> -i low` (downgrade, don't delete)
- Fully superseded by memoir → `icm forget <ID>`

## Step 9 — Session-start config integrity check

Verify that both AI assistants have the ICM wake-up section in their config files.

In bash:

```bash
grep -l "Session Start" ~/.claude/CLAUDE.md 2>/dev/null
```

In PowerShell:

```powershell
@("$HOME/.claude/CLAUDE.md") | ForEach-Object {
  $present = (Test-Path $_) -and ((Get-Content $_ -Raw) -match "Session Start")
  [PSCustomObject]@{ File = $_; HasSessionStart = $present }
} | Format-Table -AutoSize
```

Expected: both files show `HasSessionStart = True`. If either is missing or the file
does not exist, append the following section **after** the `<!-- icm:end -->` marker
in the respective file.


```markdown
## Session Start -- MANDATORY FIRST ACTION

On the very first turn of each new session, BEFORE responding to the user's first message,
call the `icm-icm_wake_up` tool (max_tokens: 800) to load a compact summary of critical
and high-importance memories from prior sessions. Apply all retrieved preferences
immediately. Do not skip this step even for simple tasks.
```

**For Claude Code** (`~/.claude/CLAUDE.md`):

```markdown
## Session Start -- MANDATORY FIRST ACTION

On the very first turn of each new session, BEFORE responding to the user's first message,
run `icm wake-up --max-tokens 800` and read the output. Apply all retrieved preferences
immediately. Do not skip this step even for simple tasks.
```

Note: Claude Code uses bash (`icm wake-up`), not MCP — `~/.claude/mcp.json` has no servers.

## Step 10 — Report

Print a concise maintenance report in this format:

```text
## ICM Garden Report — YYYY-MM-DD

**Stats:** N memories across M topics | K memoirs with C concepts total

**Memory actions:**
- Merged: [list IDs merged, or "none"]
- Deleted: [list IDs deleted, or "none"]
- Keywords enriched: [count] memories
- Importance adjusted: [count] memories
- Accuracy issues found: [list, or "none"]

**Memoir actions:**
- Concepts refined: [count] (list names)
- Concepts deprecated: [count] (list names + memoir)
- Concepts added: [count] (list names + memoir)
- Links added: [count] (list pairs)
- Memories promoted to memoir: [count]
- Memories downgraded/deleted after promotion: [count]

**Staleness candidates** (0 accesses, >30 days old):
- [ID] — topic: X — created: DATE

**Garden health:** Good / Needs attention / Action required
```

Keep the report short. Do not print full memory contents.

---

*Source: vendored from the Triforce Claude Code setup guide ([rtk-ai/icm](https://github.com/rtk-ai/icm)).*
