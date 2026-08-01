---
name: setup-ai-env
description: >
  Configure the user's local environment to support all AI tools and skills documented in
  docs/ai/. Installs and validates CLI binaries (, gh, , , , rtk,
  , , ), registers the  server,
  and verifies Claude Code is installed. Idempotent and re-runnable: detects missing AND
  outdated tools, then installs or updates them. On macOS/Linux it prefers Homebrew
  (signature-verified bottles, no manual checksum step); on Windows it uses winget where
  available and falls back to a supply-chain-aware manual-download flow with user-confirmed
  checksums for the rest. Detects intentional source builds (`--HEAD`, `git describe` suffix,
  literal `dev`) and skips overwriting them. Shows a confirmation plan before touching any
  file. Prints a final summary table.
  Triggers on: "setup-ai-env", "setup ai environment", "configure ai tools",
  "install ai tools", "set up claude skills", "onboard ai env", "/setup-ai-env",
  "update ai tools", "refresh ai tools", "upgrade ", "/update-ai-env",
  "onboarding", "new hire", "first day", "set up ai", "getting started with ai".
---

# setup-ai-env

Configure a user's local environment end-to-end for AI-assisted development on Cloud Control.
All docs live in `docs/ai/`; this skill automates what those docs describe.

**Cross-platform: macOS, Linux, and Windows.** Detect the OS in Phase 0 before
running any platform-specific command. Auth flows that require interactive input are
shown but never auto-executed — the skill pauses and instructs the user to run them.

---

## Phase 0a — Welcome & orientation

Begin every run with this orientation block, printed before any detection or install.

```text
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Welcome to AI-assisted development setup on Cloud Control
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

This skill configures your environment for AI-assisted development on Cloud Control.
It is safe to re-run at any time — it only acts on what is missing or out of date.

  Fresh install:   15–25 minutes (includes auth flows you complete in your browser)
  Re-run / update: 1–2 minutes if your environment is already converged

What gets installed
  • gh           — GitHub CLI for PRs, issues, releases, and Actions
  •         —  DC CLI (Cloud Control's repo host)
  •      —  CLI with automatic PII redaction ( + )
  • rtk          — Token-saving proxy (60–90% reduction on dev commands)
  • icm          — Persistent cross-session memory (mandatory in this project)

Want to read first?
  docs/ai/overview.md — human-readable setup guide and critical path
  docs/ai/cli-tools.md — full tool catalog with purpose + examples

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

Ask: "Type **CONTINUE** to begin detection, or **STOP** to read the docs first."

Accept only `CONTINUE` (case-insensitive). On `STOP` or anything else, print:
> "Open [`docs/ai/overview.md`](../../docs/ai/overview.md) to start. Re-run `/setup-ai-env` when you're ready."
and stop.

---

## Phase 0 — Detect OS

Run the appropriate command and record the result as `macos`, `linux`, or `windows`.

**macOS / Linux (POSIX shell):**

```bash
uname -s
```

- `Darwin`    → `macos`
- `Linux`     → `linux`
- `MINGW*` or `MSYS*` → `windows` (Git Bash on Windows; `uname -s` returns e.g. `MINGW64_NT-10.0-26200`)

**Windows:**

If `uname` returns a value starting with `MINGW` or `MSYS`, OS is `windows`.
If `uname` is unavailable, or `$env:OS` equals `Windows_NT`, OS is also `windows`.

Store the result — every branching block in Phases 1 and 4 is labelled
`(macOS/Linux)` or `(Windows)`. Execute only the block that matches.

**Windows / Git Bash note:** Claude Code's Bash tool always runs in **Git Bash**, not PowerShell.
For all Phase 1 detection commands and Phase 4 file operations (download, hash, move, extract),
use the **macOS/Linux bash blocks** — they work correctly in Git Bash on Windows. `$HOME`
resolves to the user home directory in Git Bash; do not use `$USERPROFILE` in Bash tool calls.
PowerShell blocks in Phase 4 apply only to system-level operations that require PowerShell
(e.g. setting the User PATH via `[Environment]::SetEnvironmentVariable`).

---

## Phase 1 — Detect current state

Run every check below. Record each result as `OK`, `MISSING`, `AUTH_NEEDED`, or `PARTIAL`.
Do **not** modify anything in this phase.

### 1a — PATH directory

Canonical path:

- macOS/Linux: `$HOME/.claude/skills/bin`
- Windows: `$env:USERPROFILE\.claude\skills\bin`

**macOS/Linux:**

```bash
[ -d "$HOME/.claude/skills/bin" ] && echo "dir: EXISTS" || echo "dir: MISSING"
echo "$PATH" | grep -q ".claude/skills/bin" && echo "path: IN PATH" || echo "path: NOT IN PATH"
```

**Windows (PowerShell):**

```powershell
$binDir = "$env:USERPROFILE\.claude\skills\bin"
Test-Path $binDir
([Environment]::GetEnvironmentVariable('PATH', 'User')) -like "*$binDir*"
```

`OK` if directory exists AND bin dir appears in PATH.

### 1b — CLI binaries

| Binary | Version command | Notes |
|---|---|---|
| `gh` | `gh --version` | macOS/Linux: Homebrew · Windows: winget |
| `rtk` | `rtk --version` | macOS/Linux: Homebrew core · Windows: manual download |
| `icm` | macOS/Linux: `icm --version` · Windows: `icm.exe --version` | macOS/Linux: `rtk-ai/tap` formula · Windows: manual download |

After the version command runs, also record the **install source** — this drives which update
path Phase 4 uses (brew upgrade vs. supply-chain download flow).

**macOS/Linux:**

```bash
for b in  gh    rtk icm claude-code; do
  if brew list --cask "$b" >/dev/null 2>&1; then
    echo "$b: brew-cask"
  elif brew list "$b" >/dev/null 2>&1; then
    echo "$b: brew-formula"
  elif [ -x "$HOME/.claude/skills/bin/$b" ] || [ -x "$HOME/.claude/skills/bin/$b.exe" ]; then
    echo "$b: manual-bin"
  elif command -v "$b" >/dev/null 2>&1; then
    echo "$b: manual-bin ($(command -v "$b"))"
  else
    echo "$b: not-installed"
  fi
done
```

**Windows (PowerShell):** no Homebrew — every installed binary is `manual-bin` unless it was
brought in by `winget` (currently only `gh`). Record `winget` for `gh` if `winget list --id GitHub.cli`
returns a row; record `manual-bin` for everything else. If neither winget nor `~/.claude/skills/bin`
accounts for the binary, fall back to `where.exe "$b" 2>$null` — if it returns a path, record
`manual-bin (<path>)`; otherwise record `not-installed`.

**Source-build detection.** Some users intentionally build a binary from source via
`brew install --HEAD` or local source build. Treat the binary as `SOURCE_BUILD` and **skip any update**
when *either* of the following matches the version string:

- contains `-g<hex>` (the `git describe` suffix, e.g. `v0.11.0-13-g20b8da5`)

For each binary record one of: `brew-cask`, `brew-formula`, `winget`, `manual-bin`, `not-installed`,
`SOURCE_BUILD`.

### 1c — CLI auth status (only for binaries that returned OK in 1b)

| Binary | Auth check |
|---|---|
| `gh` | `gh auth status` |

`rtk` and `icm` have no auth step.

### 1e — MCP servers


**macOS/Linux:**

```bash
```

**Windows (PowerShell):**

```powershell
$cfg = Get-Content "$env:USERPROFILE\.claude.json" -Raw -ErrorAction SilentlyContinue
if ($cfg -match '/v0\.1/servers/-mcp') { "-mcp: OK" } else { "-mcp: MISSING" }
```

### 1f — Claude Code

`claude --version` — exit 0 = `OK`.

### 1g — ICM hooks

**macOS/Linux:**

```bash
grep -i "icm" "$HOME/.claude/settings.json" && echo "hooks: OK" || echo "hooks: MISSING"
```

**Windows (PowerShell):**

```powershell
Get-Content "$env:USERPROFILE\.claude\settings.json" | Select-String "icm"
```

`OK` if `icm` appears in the file.

### 1h — Version freshness (only for binaries that returned OK in 1b)

Pick the freshness source based on Phase 1b install source:

- `brew-cask` / `brew-formula` → `brew outdated --json=v2` (no remote API call, signature-verified).
- `winget` → **detection only** (do NOT run `winget upgrade` during Phase 1 — it performs the install,
  not just a check). Instead, use `winget list --id <id>` and inspect the "Available" column:
  if the "Available" column is populated with a version string, mark `OUTDATED`; otherwise mark `OK`.
  Example: `winget list --id GitHub.cli | Select-String "GitHub.cli"` — if the output row has a fourth
  column, an update is available.
- `manual-bin` → query the GitHub Releases API via `gh api` (table below).
- `SOURCE_BUILD` → mark `SKIP_CHECK`, never offer an update.

| Binary | Installed version command | Latest-version source (manual-bin only) |
|---|---|---|
| `gh` | `gh --version` | Defer to package manager (`brew upgrade` / `apt-get upgrade` / `winget upgrade`) — skip remote lookup |
| `rtk` | `rtk --version` | `gh api repos/rtk-ai/rtk/releases/latest --jq .tag_name` |
| `icm` | macOS/Linux: `icm --version` · Windows: `icm.exe --version` | `gh api repos/rtk-ai/icm/releases/latest --jq .tag_name` |
| `claude` | `claude --version` | No remote check — `claude update` has no dry-run flag and is idempotent. Mark `ALWAYS_RUN` so Phase 4 always runs it when `claude` is installed. |

Record `OK` if installed version matches latest (or check was skipped), `OUTDATED` if it differs.

Strip leading `v` from tags before comparing (e.g. `v1.2.3` → `1.2.3`).
`icm` uses an `icm-v` prefix (e.g. `icm-v0.10.49`) — strip `icm-v` before comparing.

> **Brew lag note.** Homebrew may trail the upstream GitHub release by hours or days
> (for example, `` brew cask reached `0.27.0` while GitHub had already published `0.27.1`).
> When the install source is brew, always defer to brew's view of latest — overwriting a
> brew-managed binary with a manual download breaks brew's tracking.

### 1j — Claude Code freshness

`claude update` has no `--check` or dry-run flag. Skip the remote lookup. If Phase 1f returned
`OK`, emit a single `ALWAYS_RUN` row for `claude update` in Phase 4. The command self-reports
whether an update was applied or the install was already current.

---

## Phase 2 — Build plan table

Include rows where status is **not** `OK` — that is: `MISSING`, `AUTH_NEEDED`, `PARTIAL`,
`OUTDATED`, `CHECK_PENDING`, `SKIP_CHECK`, or `ALWAYS_RUN`. If all are `OK`, print:
"All AI environment checks passed — nothing to do." and stop.

For `OUTDATED` binary rows, the action column reads:
`Update <binary> <currentVer> → <latestVer> (download + checksum confirm)`.

For the `CHECK_PENDING` s row, the action column reads:

For the `ALWAYS_RUN` claude row, the action column reads:
`Run claude update`.

**Plan ordering rules** — apply before numbering rows:

1. PATH setup
2. Binary installs (`MISSING`) and updates (`OUTDATED`) — file-download steps, no auth required
3. Auth steps (`AUTH_NEEDED`) — manual; must complete before any step that invokes the binary
4. Skill installs (`MISSING`) and skill updates (`CHECK_PENDING`) — require binary auth
5. MCP registration, ICM init, Claude update (`ALWAYS_RUN`)

If a binary is both `OUTDATED` and `AUTH_NEEDED`, its update row appears **before** its auth row.
This ensures the user installs the correct binary version before authenticating with it.


Example plan table format:

```text
Setup Plan
──────────────────────────────────────────────────────────────────────────────
 #   Item                          Current Status   Action
──────────────────────────────────────────────────────────────────────────────
 1   PATH: ~/.claude/skills/bin    MISSING          Create dir + add to User PATH
 2   gh binary                     MISSING          Install gh (brew / apt / dnf / winget)
 4   rtk binary                    OUTDATED         Update rtk 0.4.1 → 0.5.0 (download + checksum confirm)
 5   gh auth                       AUTH_NEEDED       Show auth command (manual)
 9   claude                        ALWAYS_RUN       Run claude update
 …
──────────────────────────────────────────────────────────────────────────────
```

---

## Phase 3 — Confirm

Display the plan table, then output a confirmation banner that lists **every category of action**
the plan contains. Tailor the text to what is actually in the plan. For example:

```text
The actions above may:
 • Create or modify your PATH / shell profile
 • Install new binaries to ~/.claude/skills/bin/
 • Update existing binaries (each requires checksum verification — two confirmations per binary)
 • Update ~/.claude config (MCP server registration, ICM hooks)
 • Run: claude update
Items marked "Manual download required" need you to download a file first — the skill
will pause and prompt you before each one.

Type YES to proceed, or NO to cancel.
```

Omit bullet points for action categories that are not present in the plan.

Accept only `YES` (case-insensitive). Cancel on anything else. Do not proceed until `YES`.

---

## Phase 4 — Apply

Execute each row in order. Announce each step. Record `DONE` or `FAILED: <error>`.

### PATH directory

**macOS/Linux:**

```bash
mkdir -p "$HOME/.claude/skills/bin"
```

Then add to the appropriate shell profile. First detect which shell is active:

```bash
echo $SHELL
```

Append to the correct profile file (substitute as needed — `~/.zshrc` for zsh, `~/.bashrc` for bash on Linux, `~/.bash_profile` for bash on macOS):

```bash
PROFILE="$HOME/.zshrc"
grep -qF '.claude/skills/bin' "$PROFILE" || \
  echo 'export PATH="$HOME/.claude/skills/bin:$PATH"' >> "$PROFILE"
echo "Added to $PROFILE. Open a new terminal or run: source $PROFILE"
```

**Windows (PowerShell):**

```powershell
$binDir = "$env:USERPROFILE\.claude\skills\bin"
New-Item -Path $binDir -ItemType Directory -Force
$cur = [Environment]::GetEnvironmentVariable('PATH', 'User')
if ($cur -notlike "*$binDir*") {
    [Environment]::SetEnvironmentVariable('PATH', "$cur;$binDir", 'User')
}
```

### Install binaries — Homebrew-first on macOS/Linux

Before falling back to the manual-download flow, prefer Homebrew on macOS/Linux. Each of these
formulas/casks is supply-chain-verified by Homebrew (bottle signatures), so no separate checksum
confirmation is required. **Windows users skip this section and use the per-binary blocks below.**

```bash
# Required taps (idempotent)
brew tap rtk-ai/tap               # icm
```

| Binary | Install (macOS/Linux) | Update (macOS/Linux) |
|---|---|---|
| `gh` | `brew install gh` | `brew upgrade gh` |
| `rtk` | `brew install rtk` | `brew upgrade rtk` |
| `icm` | `brew install rtk-ai/tap/icm` | `brew upgrade icm` |
| `claude` | `brew install --cask claude-code` | `brew upgrade --cask claude-code` |

`` and `` have **no** Homebrew formula — always use the manual-download flow below
on macOS/Linux as well as Windows.

Before installing, tell the user:
> "Installing the brew-managed AI tooling — ``, `gh`, ``, `rtk`, `icm`, `claude-code`.
> Homebrew handles signature verification and PATH wiring; no separate supply-chain confirmation
> is needed for these."

### Install gh (Windows / Linux without brew)

Before installing, tell the user:
> "Installing **gh** — GitHub CLI. Used for PRs, issues, releases, Actions workflows, and the version-freshness checks this skill runs. Try after install: `gh pr list`"

**Linux without Homebrew:** check which package manager is available and use the first that works:

```bash
command -v apt-get && sudo apt-get install -y gh
command -v dnf    && sudo dnf install -y gh
```

If neither is available, mark `MANUAL` and direct user to <https://cli.github.com/>.

**Windows (PowerShell):**

```powershell
winget install --id GitHub.cli --silent --accept-package-agreements --accept-source-agreements
```

If `winget` is unavailable, mark `MANUAL` and direct user to <https://cli.github.com/>.

### Update Claude Code

Run whenever Phase 1j emitted an `ALWAYS_RUN` row (i.e. `claude` is installed).
`claude update` is idempotent — it reports "Already up to date" when no update is available:

```bash
claude update
```

Capture the exit code. On non-zero, mark `FAILED: <error output>` and continue.

### Update package-managed binaries

When Phase 1h marks any binary with install source `brew-cask` / `brew-formula` / `winget` as
`OUTDATED`, defer to the package manager. No checksum step is required — the package manager
verifies signatures.

**macOS/Linux (brew):** see the install/update table in "Install binaries — Homebrew-first" above.

**Linux without Homebrew:**

```bash
sudo apt-get upgrade -y gh    # Debian/Ubuntu
sudo dnf upgrade -y gh        # RHEL/Fedora
```

**Windows (PowerShell):**

```powershell
winget upgrade --id GitHub.cli --silent --accept-package-agreements --accept-source-agreements
```

### Register Remote  MCP server

Before registering, tell the user:

Pause and ask: "What is your  tenant subdomain? (e.g. `abc12345` from `abc12345.apps..com`)"

Instruct the user to update the `-mcp` URL in their repo-local `.mcp.json` — replace `<tenant>` with the actual subdomain:

```json
{
  "mcpServers": {
    "-mcp": {
      "type": "http",
      "url": "https://<tenant>.apps..com/platform-reserved//v0.1/servers/-mcp/mcp"
    }
  }
}
```

Then: "Run `/mcp` in Claude Code and complete the browser OAuth flow for the `-mcp` server."

If the OAuth flow fails (server does not support MCP OAuth PKCE), fall back to a Platform Token — see [MCP Server Setup](../../docs/ai/mcp-servers.md) for the fallback configuration.

### Initialize ICM hooks

Before initializing, tell the user:
> "Setting up **ICM** (Infinite Context Memory) hooks — wires Claude Code to automatically
> recall past decisions and store new ones on every session. This is mandatory in the Cloud
> Control project (see AGENTS.md). Try after setup: `icm health`"

**macOS/Linux:**

```bash
icm init --mode standard
```

**Windows (PowerShell):**

```powershell
icm.exe init --mode standard
```

On Windows use `icm.exe` (not `icm`) to avoid the `Invoke-Command` alias clash.

`icm init` also injects an ICM block into the project `CLAUDE.md` (when present) and writes hooks
into `~/.claude/settings.json`. **Tell the user to restart Claude Code** so the freshly-wired
`SessionStart`, `UserPromptSubmit`, `PreToolUse`, `PostToolUse`, `PreCompact`, and `SessionEnd`
hooks take effect — the current session keeps running on the pre-init configuration.

### Update existing binary (supply-chain aware)

Execute this procedure only for binaries whose **install source is `manual-bin`** and whose Phase 1h
status is `OUTDATED`. Binaries installed via `brew-cask`, `brew-formula`, or `winget` use the
package-manager flow above. Binaries marked `SOURCE_BUILD` (`-g<hex>` suffix or literal `dev`)
are intentional source builds — **never overwrite them**; instead, tell the user:

> "`<binary>` looks like a source build (`<version-string>`). Skipping update to avoid
> clobbering your local build. If you want to switch to a packaged release, run
> `brew uninstall --HEAD <binary>` (or remove the manual binary) and re-run `/setup-ai-env`."

1. **Fetch release metadata and checksum** using `gh` (handles both public and private repos):

   ```bash
   TAG=$(gh api repos/<owner>/<repo>/releases/latest --jq .tag_name)
   HTML_URL=$(gh api repos/<owner>/<repo>/releases/latest --jq .html_url)

   # Fetch checksum file via gh — works for private repos; curl -L would 401
   CHKDIR=$(mktemp -d)
   gh release download "$TAG" --repo <owner>/<repo> --pattern "*checksums*" \
     --dir "$CHKDIR" 2>/dev/null
   CHKFILE=$(ls "$CHKDIR"/*checksums* 2>/dev/null | head -1)
   SHA256=$([ -n "$CHKFILE" ] && grep "<asset-name>" "$CHKFILE" | awk '{print $1}' || echo "")
   rm -rf "$CHKDIR"
   ```

   Replace `<asset-name>` with the OS/arch-specific filename (see the install table below for patterns).
   If `SHA256` is empty, no checksum was published — proceed to the escalation message in step 2.

2. **Show the user the release details and require the first confirmation:**

   ```text
   Updating <binary>: <currentVer> → <latestVer>
   Asset:         <asset-url>
   SHA256:        <published-sha256>   (source: GitHub release manifest)
   Release notes: <release-html-url>
   Supply-chain note: verify the SHA256 matches what you see at the release URL before typing YES.

   Type YES to download, or NO to skip this binary.
   ```

   If no SHA256 is published for the asset, escalate:

   ```text
   WARNING: no published checksum found for <binary> <latestVer>.
   Proceeding without checksum verification is a supply-chain risk.
   Type OVERRIDE to accept and download anyway, or NO to skip.
   ```

   Accept `YES` (or `OVERRIDE` in the no-checksum case) before downloading.

3. **Download to a temp file using `gh`, then compute the local hash:**

   `gh release download` authenticates automatically — works for both public and private repos.

   ```bash
   TMPFILE=$(mktemp /tmp/<binary>-update-XXXX)
   gh release download "$TAG" --repo <owner>/<repo> --pattern "<asset-name>" \
     --output "$TMPFILE"
   sha256sum "$TMPFILE"          # Linux and Windows/Git Bash
   # shasum -a 256 "$TMPFILE"   # macOS alternative
   ```

4. **Show both hashes side-by-side and require the second confirmation:**

   ```text
   Published SHA256: <published-sha256>
   Local SHA256:     <computed-sha256>

   Do the hashes match? Type YES to install, or NO to discard and skip.
   ```

   If the user types `NO`, delete the temp file and mark the binary `SKIPPED`.

5. **Replace the binary and restore permissions:**

   On macOS/Linux — extract archive (if needed), then:

   ```bash
   mv /tmp/<binary>-update "$HOME/.claude/skills/bin/<binary>"
   chmod +x "$HOME/.claude/skills/bin/<binary>"
   xattr -d com.apple.quarantine "$HOME/.claude/skills/bin/<binary>" 2>/dev/null || true
   ```

   On Windows (Git Bash) — extract `.zip` (binaries are always zipped on Windows), then move:

   ```bash
   unzip -o "$TMPFILE" <binary>.exe -d "$HOME/.claude/skills/bin/"
   rm "$TMPFILE"
   ```

6. **Record** `UPDATED` on success, `FAILED: <error>` on any error.

### Binaries requiring manual download

Skip this section entirely for binaries already installed via Homebrew or winget — only run it for
binaries Phase 1b reported as `not-installed` *and* with no package-manager path (i.e. `` and
`` on any OS, plus everything on Windows except `gh`).

Before starting downloads, tell the user what each binary is for:
> "The following binaries require a manual download (internal or supply-chain-verified releases):
>
> | Binary | Purpose | Try after install |
> |---|---|---|
> | `` |  +  CLI with automatic PII redaction | `  workitem view --key TICKET-1` |
> | `rtk` | Token-saving proxy — rewrites shell commands transparently | `rtk gain` |
> | `icm` | Persistent memory across Claude sessions (mandatory) | `icm recall "setup"` |

For each binary, pause, print download instruction, wait for user to confirm the binary is in `$binDir`, then re-check.

On Windows, binaries end in `.exe`; on macOS and Linux the binary has no extension. Look for the OS and architecture in the release asset name (e.g. `linux_amd64`, `darwin_arm64`, `windows_amd64`).

| Binary | Source | Windows | macOS | Linux |
|---|---|---|---|---|
| `rtk` | <https://github.com/rtk-ai/rtk/releases/latest> | asset contains `windows` → `rtk.exe` | asset contains `darwin` → `rtk` | asset contains `linux` → `rtk` |
| `icm` | <https://github.com/rtk-ai/icm/releases/latest> | `icm-x86_64-pc-windows-msvc.zip` → `icm.exe` | Apple Silicon: `icm-aarch64-apple-darwin.tar.gz` · Intel: `icm-x86_64-apple-darwin.tar.gz` → `icm` | `icm-x86_64-unknown-linux-musl.tar.gz` → `icm` |

macOS / Linux:
```bash
GH_TOKEN=$(gh auth token)
BASE=""
# macOS only — remove quarantine:
xattr -d com.apple.quarantine "$HOME/.claude/skills/bin/" 2>/dev/null || true
```

Windows (PowerShell):
```powershell
$token = gh auth token
Invoke-WebRequest `
  -Headers @{Authorization="Bearer $token"} `
```

No published checksum for this binary — use the OVERRIDE flow and record the computed SHA256 for your records.

**macOS — after extraction of other binaries, make them executable and remove the quarantine attribute:**

```bash
chmod +x "$HOME/.claude/skills/bin/<binary>"
xattr -d com.apple.quarantine "$HOME/.claude/skills/bin/<binary>" 2>/dev/null || true
```

**Linux — after extraction, make the binary executable:**

```bash
chmod +x "$HOME/.claude/skills/bin/<binary>"
```

### Auth steps (show, never auto-execute)

Two tools require a manually generated token — pause and show the retrieval URL before displaying the auth command. The remaining tools use interactive browser-based OAuth flows.

### Interactive OAuth flows (browser opens automatically — no token to retrieve)

| Binary | Auth command |
|---|---|
| `gh` | `gh auth login` |

>
> ```bash
> ```
>
> Tenant URLs sourced from the `SFM Url` row in [`app-cicd-state/README.md`](). If these tenants are ever repointed, update both this note and the " Tenant per Environment" table in `skills/argo-debug/reference/argo-debug.md` together.

After completing all auth steps, re-run `/setup-ai-env` to validate.

### Claude Code plugins

Install the team plugins (warn-only if absent — does not block):


```bash
claude plugin marketplace add anthropics/claude-plugins-official
```

Follow the OAuth browser flow when prompted.

**Caveman** — cuts output tokens ~65% with no accuracy loss (recommended):

```bash
claude plugin marketplace add JuliusBrussee/caveman
claude plugin install caveman@caveman
```

Re-run to verify both are installed:

```bash
claude plugin list
```

See [`docs/ai/caveman.md`](../../docs/ai/caveman.md) for intensity level config and statusline badge setup.

---

## Phase 5 — Validate

Re-run Phase 1 checks for every item that had an action. For updated binaries, also re-run Phase
1h to confirm the installed version now matches latest. Record `PASS` or `FAIL` for each item.

---

## Phase 6 — Report

Print a final summary table. The `Action` column distinguishes fresh installs from updates:

```text
Setup Summary
──────────────────────────────────────────────────────────────────────────────
 Item                          Action              Result
──────────────────────────────────────────────────────────────────────────────
 PATH: ~/.claude/skills/bin    Created             PASS
 gh binary                     Installed           PASS
  binary                  Manual              PASS (user confirmed)
 rtk binary                    Updated 0.4.1→0.5.0 PASS
 gh auth                       Manual              AWAITING USER
 :          Installed           PASS
 s (bulk update)     Updated             PASS
 icm hooks                     Initialized         PASS
 claude                        ALWAYS_RUN          PASS
  binary                  SKIPPED (current)   —
──────────────────────────────────────────────────────────────────────────────
Re-run /setup-ai-env after completing those steps to verify.
──────────────────────────────────────────────────────────────────────────────
```


After printing the table, tell the user: "Proceeding to Phase 7 — environment orientation and next steps."

---

## Phase 7 — Welcome summary

Print this after the Phase 6 report table. Always run Phase 7, even if all items were already PASS.

```text
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Your AI-assisted development environment
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Tool            Purpose                                         Try this now
──────────────  ──────────────────────────────────────────────  ─────────────────────────────
gh              GitHub CLI — PRs, issues, Actions               gh pr list
         +  with PII redaction              workitem view --key TICKET-1
rtk             Token-saving shell proxy (60–90% savings)       rtk gain
icm             Persistent cross-session memory (mandatory)     icm health
s     7 pre-built Claude skill bundles                Ask: "refine a backlog ticket"

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Self-test (run tomorrow to confirm everything still works)
  gh auth status
  icm health
  rtk gain

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Read these next, in order:
  1. docs/ai/overview.md               — the human map of this setup
  2. docs/ai/cli-tools.md              — deep dive on every CLI you just installed
  3. AGENTS.md                         — project conventions (mandatory reading)
  4. docs/ai/claude-skills.md          — what each installed skill does and when it triggers
  5. docs/ai/infinite-context-memory.md — ICM usage and memory hygiene (mandatory)
  6. docs/ai/prompt-cost-reduction.md  — RTK savings and usage
  7. docs/ai/maintenance.md            — keep tooling fresh

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

 channels

  Broader  AI help: , 

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

First task suggestion: pick a TICKET ticket and ask Claude to investigate it.
  Example: "Look at TICKET-12345 and tell me what needs to be done."
  Claude will use  to fetch the ticket and AGENTS.md conventions to advise you.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## Reference: docs/ai/ source pages

| Topic | Page |
|---|---|
| Prerequisites (system tools, SSH, GitHub-Internal access, tokens) | `docs/ai/prerequisites.md` |
| MCP servers | `docs/ai/mcp-servers.md` |
| Claude Code | `docs/ai/claude-code-setup.md` |
| ICM hooks + memory hygiene | `docs/ai/infinite-context-memory.md` |
| Periodic tool maintenance | `docs/ai/maintenance.md` |
