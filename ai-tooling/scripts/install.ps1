# ai-tooling installer (PowerShell / Windows native)
# Linux/macOS/Git Bash: use install.sh instead

[CmdletBinding()]
param(
    [string]$Project,
    [switch]$UserHome,
    [switch]$Copy,
    [switch]$Unlink,
    [switch]$DryRun,
    [string]$Vcs
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# ---------------------------------------------------------------------------
# Validate mutually-exclusive mode flags
# ---------------------------------------------------------------------------

if ($Project -and $UserHome) {
    Write-Error "Specify exactly one of -Project or -UserHome, not both."
    exit 1
}

if (-not $Project -and -not $UserHome) {
    Write-Error "One of -Project <path> or -UserHome is required."
    exit 1
}


$SharedRepoDir = Split-Path $PSScriptRoot -Parent

# ---------------------------------------------------------------------------
# Preflight checks  (skipped for -DryRun)
# ---------------------------------------------------------------------------

function Test-ToolVersion {
    <#
    .SYNOPSIS
        Returns $true if the named tool is on PATH and reports a version >= the
        required minimum.  Writes a descriptive failure message to $failures.
    #>
    param(
        [string]$Tool,
        [string]$MinVersion,
        [scriptblock]$VersionCmd,  # must return a single version string, e.g. "2.43.0"
        [ref]$Failures
    )

    $cmd = Get-Command $Tool -ErrorAction SilentlyContinue
    if (-not $cmd) {
        $Failures.Value.Add("$Tool not found (required >= $MinVersion)")
        return
    }

    try {
        $raw = & $VersionCmd 2>&1 | Out-String
        # Extract first token that looks like a semver / version number
        if ($raw -match '(\d+\.\d+(?:\.\d+)*)') {
            $found = [version]($Matches[1])
            $min   = [version]$MinVersion
            if ($found -lt $min) {
                $Failures.Value.Add("$Tool $found is below minimum $MinVersion")
            }
        } else {
            $Failures.Value.Add("${Tool}: could not parse version from output: $raw")
        }
    } catch {
        $Failures.Value.Add("${Tool}: version check failed: $_")
    }
}

function Invoke-Preflight {
    $failures = [System.Collections.Generic.List[string]]::new()

    # git >= 2.20
    Test-ToolVersion -Tool 'git' -MinVersion '2.20' `
        -VersionCmd { git --version } `
        -Failures ([ref]$failures)

    # python >= 3.10
    Test-ToolVersion -Tool 'python' -MinVersion '3.10' `
        -VersionCmd { python --version } `
        -Failures ([ref]$failures)

    # jq >= 1.6
    Test-ToolVersion -Tool 'jq' -MinVersion '1.6' `
        -VersionCmd { jq --version } `
        -Failures ([ref]$failures)

    if ($failures.Count -gt 0) {
        Write-Error "Preflight failed:`n$($failures -join "`n")"
        exit 1
    }

    Write-Host "Preflight passed."
}

# ---------------------------------------------------------------------------
# Symlink helper - the only place that calls New-Item -ItemType SymbolicLink.
# Requires Developer Mode (or admin) on Windows.
# ---------------------------------------------------------------------------

function make-link ($target, $link) {
    New-Item -Path $link -ItemType SymbolicLink -Value $target -Force | Out-Null
}

# ---------------------------------------------------------------------------
# Symlink capability check - returns $true if the current token can create
# symlinks (Developer Mode on, or elevated).
# ---------------------------------------------------------------------------

function Test-SymlinkCapability {
    $testTarget = Join-Path $env:TEMP "ai-tooling-symlink-test-target-$([guid]::NewGuid())"
    $testLink   = Join-Path $env:TEMP "ai-tooling-symlink-test-link-$([guid]::NewGuid())"
    try {
        New-Item -Path $testTarget -ItemType File -Force | Out-Null
        New-Item -Path $testLink -ItemType SymbolicLink -Value $testTarget -ErrorAction Stop | Out-Null
        return $true
    } catch {
        return $false
    } finally {
        Remove-Item -LiteralPath $testLink   -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $testTarget -Force -ErrorAction SilentlyContinue
    }
}

# ---------------------------------------------------------------------------
# Batch symlink creation - creates all symlinks directly (Developer Mode / already
# elevated) or via a single UAC-elevated child process.  Returns one result object
# per asset with Success=$true/$false.
# ---------------------------------------------------------------------------

function Invoke-BatchSymlinkCreation {
    param([System.Collections.Generic.List[pscustomobject]]$Assets)

    if ($Assets.Count -eq 0) { return }

    if (Test-SymlinkCapability) {
        foreach ($asset in $Assets) {
            # Swallow per-asset errors so a single failure doesn't abort the whole batch;
            # the verification loop below records Success=$false for any link that wasn't created.
            try {
                make-link $asset.SourceAbs $asset.LinkAbs
            } catch {
                Write-Warning "Symlink failed for $($asset.LinkRel): $_"
            }
        }
    } else {
        $tempScript = Join-Path $env:TEMP ("ai-symlinks-{0}.ps1" -f [guid]::NewGuid())
        try {
            $symlinkCmds = $Assets | ForEach-Object {
                # Escape single quotes so paths like C:\Users\O'Brien\... stay valid PS syntax.
                $linkAbs = $_.LinkAbs  -replace "'", "''"
                $srcAbs  = $_.SourceAbs -replace "'", "''"
                "New-Item -Path '$linkAbs' -ItemType SymbolicLink -Value '$srcAbs' -Force | Out-Null"
            }
            Set-Content -Path $tempScript -Value $symlinkCmds -Encoding utf8

            Write-Host ""
            Write-Host "Symlink creation requires elevation - a UAC prompt will appear." -ForegroundColor Yellow
            Start-Process powershell `
                -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $tempScript) `
                -Verb RunAs `
                -Wait
        } finally {
            Remove-Item -LiteralPath $tempScript -Force -ErrorAction SilentlyContinue
        }
    }

    foreach ($asset in $Assets) {
        $item = Get-Item -LiteralPath $asset.LinkAbs -Force -ErrorAction SilentlyContinue
        [pscustomobject]@{
            Asset   = $asset
            Success = [bool]($item -and $item.LinkType -eq 'SymbolicLink')
        }
    }
}

# ---------------------------------------------------------------------------
# Sibling-directory check
# ---------------------------------------------------------------------------

function Invoke-SiblingCheck {
    # Skip when copying or installing to user home
    if ($Copy -or $UserHome) { return }

    $SharedDir   = Split-Path $SharedRepoDir -Parent
    $ConsumerDir = Split-Path (Resolve-Path $Project) -Parent

    if ($SharedDir -ne $ConsumerDir) {
        Write-Error "ai-tooling and the consumer repo must be sibling directories."
        Write-Error "  shared:   $SharedRepoDir"
        Write-Error "  consumer: $(Resolve-Path $Project)"
        Write-Error "Move one of the repos or use -Copy mode."
        exit 1
    }
}

# ---------------------------------------------------------------------------
# VCS auto-detection
# ---------------------------------------------------------------------------

function Resolve-Vcs {
    # Only when -Vcs was not explicitly set and we are in project mode
    if ($Vcs -or $UserHome) { return }

    $remoteUrl = & git -C $Project remote get-url origin 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Could not determine VCS remote: $remoteUrl"
        exit 1
    }

    $isGithub    = $remoteUrl -match 'github\.com'

        exit 1
        Write-Error "Remote URL does not match a known VCS host: $remoteUrl"
        exit 1
    }

    Write-Host "Auto-detected VCS: $script:Vcs"
}

# ---------------------------------------------------------------------------
# Hook registration in user-global ~/.claude/settings.json. Delegates to the
# Python helper so bash and PS share the same merge logic.
# ---------------------------------------------------------------------------

function Register-GlobalHooks {
    $settings = Join-Path $HOME '.claude\settings.json'
    New-Item -ItemType Directory -Path (Split-Path $settings -Parent) -Force | Out-Null
    & python (Join-Path $PSScriptRoot 'register-hooks.py') $settings
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to register hooks in $settings"
        exit 1
    }
}

function Unregister-GlobalHooks {
    $settings = Join-Path $HOME '.claude\settings.json'
    if (-not (Test-Path -LiteralPath $settings)) { return }
    & python (Join-Path $PSScriptRoot 'register-hooks.py') $settings --unregister
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to unregister hooks in $settings"
        exit 1
    }
}

# ---------------------------------------------------------------------------
# Asset manifest - mirrors ASSET_MANIFEST in install.sh.
# A 'Project' or 'UserHome' value of '-' means the asset does not apply in
# that mode. VcsFilter, when set, restricts the asset to that VCS host.
# ---------------------------------------------------------------------------

$AssetManifest = @(
    # Rules (Phase 1)
    @{ Type='rules';  Source='rules/agent-behavior.md';            Project='.claude/rules/agent-behavior.md';            UserHome='.claude/rules/agent-behavior.md' }
    @{ Type='rules';  Source='rules/code-comments.md';             Project='.claude/rules/code-comments.md';             UserHome='.claude/rules/code-comments.md' }
    @{ Type='rules';  Source='rules/.md';                Project='.claude/rules/.md';                UserHome='.claude/rules/.md' }
    @{ Type='rules';  Source='rules/diagrams.md';                  Project='.claude/rules/diagrams.md';                  UserHome='.claude/rules/diagrams.md' }
    @{ Type='rules';  Source='rules/skills.md';                    Project='.claude/rules/skills.md';                    UserHome='.claude/rules/skills.md' }
    # Rules (Phase 2)
    @{ Type='rules';  Source='rules/docs.md';                      Project='.claude/rules/docs.md';                      UserHome='.claude/rules/docs.md' }
    @{ Type='rules';  Source='rules/git.md';                       Project='.claude/rules/git.md';                       UserHome='.claude/rules/git.md' }
    @{ Type='rules';  Source='rules/.md';                      Project='.claude/rules/.md';                      UserHome='.claude/rules/.md' }
    @{ Type='rules';  Source='rules/reminders.md';                 Project='.claude/rules/reminders.md';                 UserHome='.claude/rules/reminders.md' }
    # Hooks (Phase 1)
    @{ Type='hooks';  Source='hooks/-enforcer.py';         Project='.claude/hooks/-enforcer.py';         UserHome='.claude/hooks/-enforcer.py' }
    @{ Type='hooks';  Source='hooks/convention-loader.py';         Project='.claude/hooks/convention-loader.py';         UserHome='.claude/hooks/convention-loader.py' }
    # Hooks (Phase 2)
    @{ Type='hooks';  Source='hooks/convention-enforcer.py';       Project='.claude/hooks/convention-enforcer.py';       UserHome='.claude/hooks/convention-enforcer.py' }
    @{ Type='hooks';  Source='hooks/check-reminders.sh';           Project='.claude/hooks/check-reminders.sh';           UserHome='.claude/hooks/check-reminders.sh' }
    # Commands (Phase 2)
    @{ Type='commands'; Source='commands/recall.md';               Project='.claude/commands/recall.md';                 UserHome='.claude/commands/recall.md' }
    @{ Type='commands'; Source='commands/remember.md';             Project='.claude/commands/remember.md';               UserHome='.claude/commands/remember.md' }
    # Skills
    @{ Type='skills'; Source='skills/memory-maintenance';          Project='.claude/skills/memory-maintenance';          UserHome='.claude/skills/memory-maintenance' }
    @{ Type='skills'; Source='skills/preflight';                   Project='.claude/skills/preflight';                   UserHome='.claude/skills/preflight' }
    @{ Type='skills'; Source='skills/argo-wf-reference';           Project='.claude/skills/argo-wf-reference';           UserHome='.claude/skills/argo-wf-reference' }
    @{ Type='skills'; Source='skills/argo-debug';                  Project='.claude/skills/argo-debug';                  UserHome='.claude/skills/argo-debug' }
    @{ Type='skills'; Source='skills/cue-reference';               Project='.claude/skills/cue-reference';               UserHome='.claude/skills/cue-reference' }
    @{ Type='skills'; Source='skills/refine-backlog';              Project='.claude/skills/refine-backlog';              UserHome='.claude/skills/refine-backlog' }
    @{ Type='skills'; Source='skills/align-epic-priority';         Project='.claude/skills/align-epic-priority';         UserHome='.claude/skills/align-epic-priority' }
    @{ Type='skills'; Source='skills/create-agent-spec-ticket';    Project='.claude/skills/create-agent-spec-ticket';    UserHome='.claude/skills/create-agent-spec-ticket' }
    @{ Type='skills'; Source='skills/route--ticket';           Project='.claude/skills/route--ticket';           UserHome='.claude/skills/route--ticket' }
    @{ Type='skills'; Source='skills/-ip-rights-summary';      Project='.claude/skills/-ip-rights-summary';      UserHome='.claude/skills/-ip-rights-summary' }
    # Skills - user-home only
    @{ Type='skills'; Source='skills/setup-ai-env';                Project='-';                                          UserHome='.claude/skills/setup-ai-env' }
    # Agent guides (project mode only — project-scoped consumer files)
    @{ Type='agents'; Source='agents/.md';               Project='agents/.md';                       UserHome='-' }
    # Agent guides — both project + user-home (needed by hooks running at user level)
    @{ Type='agents'; Source='agents/.md';                     Project='agents/.md';                             UserHome='.claude/agents/.md' }
    @{ Type='agents'; Source='agents/--pitfalls.md';   Project='agents/--pitfalls.md';           UserHome='.claude/agents/--pitfalls.md' }
    @{ Type='agents'; Source='agents/repository-operations.md';    Project='agents/repository-operations.md';    UserHome='.claude/agents/repository-operations.md' }
    @{ Type='agents'; Source='agents/repository-operations.md';    Project='agents/repository-operations.md';    UserHome='.claude/agents/repository-operations.md'; VcsFilter='github' }
    # Docs (project mode only)
    @{ Type='docs';   Source='docs/ai/claude-code-setup.md';       Project='docs/ai/claude-code-setup.md';       UserHome='-' }
    @{ Type='docs';   Source='docs/ai/claude-project-config.md';   Project='docs/ai/claude-project-config.md';   UserHome='-' }
    @{ Type='docs';   Source='docs/ai/infinite-context-memory.md'; Project='docs/ai/infinite-context-memory.md'; UserHome='-' }
    @{ Type='docs';   Source='docs/ai/.md';                 Project='docs/ai/.md';                 UserHome='-' }
    @{ Type='docs';   Source='docs/ai/maintenance.md';             Project='docs/ai/maintenance.md';             UserHome='-' }
    @{ Type='docs';   Source='docs/ai/mcp-servers.md';             Project='docs/ai/mcp-servers.md';             UserHome='-' }
    @{ Type='docs';   Source='docs/ai/prompt-cost-reduction.md';   Project='docs/ai/prompt-cost-reduction.md';   UserHome='-' }
    @{ Type='docs';   Source='docs/ai/cli-tools.md';               Project='docs/ai/cli-tools.md';               UserHome='-' }
    @{ Type='docs';   Source='docs/ai/overview.md';                Project='docs/ai/overview.md';                UserHome='-' }
    @{ Type='docs';   Source='docs/ai/claude-skills.md';           Project='docs/ai/claude-skills.md';           UserHome='-' }
    @{ Type='docs';   Source='docs/ai/-integration.md';       Project='docs/ai/-integration.md';       UserHome='-' }
    @{ Type='docs';   Source='docs/ai/onboarding-checklist.md';    Project='docs/ai/onboarding-checklist.md';    UserHome='-' }
    # Root (project mode only)
    @{ Type='root';   Source='.mcp.json';                          Project='.mcp.json';                          UserHome='-' }
    @{ Type='root';   Source='.markdownlint.json';                 Project='.markdownlint.json';                 UserHome='-' }
)

# ---------------------------------------------------------------------------
# Resolve consumer dir + manifest/lock paths into script-scoped variables.
# ---------------------------------------------------------------------------

function Resolve-Paths {
    if ($UserHome) {
        $script:ConsumerDir = $HOME
    } else {
        $script:ConsumerDir = (Resolve-Path $Project).Path
    }
    $script:ManifestFile = Join-Path $script:ConsumerDir '.claude\.ai-tooling-manifest.json'
    $script:LockFile     = Join-Path $script:ConsumerDir '.claude\.ai-tooling.lock'
}

# ---------------------------------------------------------------------------
# Filtered asset list for the current mode and VCS. Returns hashtables with
# absolute paths pre-computed.
# ---------------------------------------------------------------------------

function Get-AssetList {
    $mode = if ($UserHome) { 'user-home' } else { 'project' }
    foreach ($asset in $AssetManifest) {
        $linkRel = if ($mode -eq 'user-home') { $asset.UserHome } else { $asset.Project }
        if ($linkRel -eq '-') { continue }
        if ($asset.ContainsKey('VcsFilter') -and $asset.VcsFilter -ne $Vcs) { continue }
        [pscustomobject]@{
            Type      = $asset.Type
            SourceRel = $asset.Source
            LinkRel   = $linkRel
            SourceAbs = Join-Path $SharedRepoDir $asset.Source
            LinkAbs   = Join-Path $script:ConsumerDir $linkRel
        }
    }
}

# ---------------------------------------------------------------------------
# Install a single asset. Returns one of:
#   installed | already-linked | conflict | missing | needs-symlink
# ---------------------------------------------------------------------------

function Install-Asset {
    param($Asset)

    if (-not (Test-Path -LiteralPath $Asset.SourceAbs)) {
        return 'missing'
    }

    $existing = Get-Item -LiteralPath $Asset.LinkAbs -Force -ErrorAction SilentlyContinue
    if ($existing) {
        $isLink = $existing.Attributes.HasFlag([System.IO.FileAttributes]::ReparsePoint)
        if ($isLink) {
            $target = $existing.Target
            if ($target) {
                $resolved = if ([System.IO.Path]::IsPathRooted($target)) {
                    $target
                } else {
                    Join-Path (Split-Path $Asset.LinkAbs -Parent) $target
                }
                if ((Resolve-Path -LiteralPath $resolved -ErrorAction SilentlyContinue).Path -eq (Resolve-Path -LiteralPath $Asset.SourceAbs).Path) {
                    return 'already-linked'
                }
            }
            return 'conflict'
        }
        return 'conflict'
    }

    $parent = Split-Path $Asset.LinkAbs -Parent
    if (-not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Path $parent -Force | Out-Null
    }

    if ($Copy) {
        Copy-Item -LiteralPath $Asset.SourceAbs -Destination $Asset.LinkAbs -Recurse -Force
        return 'installed'
    }
    return 'needs-symlink'
}

# ---------------------------------------------------------------------------
# do_install
# ---------------------------------------------------------------------------

function Invoke-Install {
    Resolve-Paths

    $modeLabel = if ($UserHome) { 'user-home' } else { 'project' }
    $targetLabel = if ($UserHome) { 'user-home' } else { Split-Path $script:ConsumerDir -Leaf }

    $sharedBranch = (& git -C $SharedRepoDir branch --show-current 2>$null)
    if (-not $sharedBranch) { $sharedBranch = 'unknown' }
    $sharedCommit = (& git -C $SharedRepoDir rev-parse HEAD 2>$null)
    if (-not $sharedCommit) { $sharedCommit = 'unknown' }
    $sharedSha = if ($sharedCommit.Length -ge 8) { $sharedCommit.Substring(0, 8) } else { $sharedCommit }

    $summary    = [System.Collections.Generic.List[object]]::new()
    $conflicts  = [System.Collections.Generic.List[string]]::new()
    $missing    = [System.Collections.Generic.List[string]]::new()
    $entries    = [System.Collections.Generic.List[object]]::new()
    $hooksDone  = $false

    $pendingSymlinks = [System.Collections.Generic.List[pscustomobject]]::new()

    foreach ($asset in Get-AssetList) {
        $status = Install-Asset -Asset $asset
        if ($status -eq 'needs-symlink') {
            $pendingSymlinks.Add($asset)
            continue
        }
        if (($status -eq 'installed' -or $status -eq 'already-linked') -and $asset.Type -eq 'hooks') {
            $hooksDone = $true
        }
        switch ($status) {
            'installed' {
                $entries.Add([ordered]@{ link = $asset.LinkRel; source = $asset.SourceRel; mode = 'symlink' })
            }
            'conflict'  { $conflicts.Add($asset.LinkRel) }
            'missing'   { $missing.Add($asset.LinkRel) }
        }
        $summary.Add([pscustomobject]@{ Asset = $asset.LinkRel; Status = $status; Mode = 'symlink' })
    }

    foreach ($result in Invoke-BatchSymlinkCreation -Assets $pendingSymlinks) {
        if ($result.Success) {
            if ($result.Asset.Type -eq 'hooks') { $hooksDone = $true }
            $entries.Add([ordered]@{ link = $result.Asset.LinkRel; source = $result.Asset.SourceRel; mode = 'symlink' })
            $summary.Add([pscustomobject]@{ Asset = $result.Asset.LinkRel; Status = 'installed'; Mode = 'symlink' })
        } else {
            $summary.Add([pscustomobject]@{ Asset = $result.Asset.LinkRel; Status = 'failed'; Mode = 'symlink' })
        }
    }

    # --- Write manifest ---
    New-Item -ItemType Directory -Path (Split-Path $script:ManifestFile -Parent) -Force | Out-Null

    $newInstall = [ordered]@{
        mode       = $modeLabel
        target     = $targetLabel
        created_at = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
        entries    = @($entries)
    }

    if (Test-Path -LiteralPath $script:ManifestFile) {
        $manifest = Get-Content -LiteralPath $script:ManifestFile -Raw | ConvertFrom-Json
        $installs = @($manifest.installs) + $newInstall
        $manifest = [ordered]@{ version = 1; installs = $installs }
    } else {
        $manifest = [ordered]@{ version = 1; installs = @($newInstall) }
    }
    $manifest | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $script:ManifestFile -Encoding UTF8

    @{ version = 1; shared_repo_commit = $sharedCommit } |
        ConvertTo-Json | Set-Content -LiteralPath $script:LockFile -Encoding UTF8

    # --- Print summary ---
    Write-Host ""
    Write-Host "ai-tooling install complete ($modeLabel mode - $targetLabel)"
    Write-Host "Shared repo: $SharedRepoDir @ $sharedSha ($sharedBranch)"
    Write-Host ""
    $summary | Format-Table -AutoSize Asset, Status, Mode | Out-Host

    if ($conflicts.Count -gt 0) {
        Write-Host "Skipped (conflict - real file already exists on disk, not managed by this installer):"
        $conflicts | ForEach-Object { Write-Host "  $_" }
        Write-Host ""
    }
    if ($missing.Count -gt 0) {
        Write-Host "Skipped (source not yet in shared repo - will be available in a future commit):"
        $missing | ForEach-Object { Write-Host "  $_" }
        Write-Host ""
    }

    if ($hooksDone) {
        Write-Host ""
        Register-GlobalHooks
    }

    if (-not $UserHome -and $hooksDone) {
        Write-Host @"

Next steps:
  1. Create .claude/convention-enforcer-config.json with consumer-specific values.
"@
    }
}

# ---------------------------------------------------------------------------
# do_unlink
# ---------------------------------------------------------------------------

function Invoke-Unlink {
    Resolve-Paths

    if (-not (Test-Path -LiteralPath $script:ManifestFile)) {
        Write-Host "Nothing to unlink (manifest not found: $script:ManifestFile)"
        exit 0
    }

    $manifest = Get-Content -LiteralPath $script:ManifestFile -Raw | ConvertFrom-Json

    $total = 0; $removed = 0
    foreach ($install in $manifest.installs) {
        foreach ($entry in $install.entries) {
            $total++
            $linkAbs = Join-Path $script:ConsumerDir $entry.link
            $existing = Get-Item -LiteralPath $linkAbs -Force -ErrorAction SilentlyContinue
            if (-not $existing) {
                Write-Host "Already removed: $($entry.link)"
                continue
            }
            if ($existing.Attributes.HasFlag([System.IO.FileAttributes]::ReparsePoint)) {
                Remove-Item -LiteralPath $linkAbs -Force
                Write-Host "Unlinked: $($entry.link)"
            } else {
                Remove-Item -LiteralPath $linkAbs -Recurse -Force
                Write-Host "Removed:  $($entry.link)"
            }
            $removed++
        }
    }

    Remove-Item -LiteralPath $script:ManifestFile -Force
    Write-Host "Manifest removed: $script:ManifestFile"

    Write-Host ""
    Unregister-GlobalHooks

    # Lock file is intentionally preserved (consumer repos may track it).
    if (Test-Path -LiteralPath $script:LockFile) {
        Write-Host "Lock file preserved (not removed): $script:LockFile"
    }
    Write-Host ""
    Write-Host "Unlinked $removed of $total entries. Manifest removed. Lock file preserved."
}

# ---------------------------------------------------------------------------
# do_dry_run
# ---------------------------------------------------------------------------

function Invoke-DryRun {
    Resolve-Paths

    $modeLabel = if ($UserHome) { 'user-home' } else { 'project' }
    Write-Host ""
    Write-Host "[DRY RUN] ai-tooling install preview ($modeLabel mode)"
    Write-Host "[DRY RUN] Shared repo: $SharedRepoDir"
    Write-Host "[DRY RUN] Consumer:    $script:ConsumerDir"
    Write-Host ""

    $had = $false
    foreach ($asset in Get-AssetList) {
        $had = $true
        if (-not (Test-Path -LiteralPath $asset.SourceAbs)) {
            "[DRY RUN] Would skip:    {0,-50} (source not in shared repo yet)" -f $asset.LinkRel | Write-Host
            continue
        }
        $existing = Get-Item -LiteralPath $asset.LinkAbs -Force -ErrorAction SilentlyContinue
        if ($existing -and $existing.Attributes.HasFlag([System.IO.FileAttributes]::ReparsePoint)) {
            "[DRY RUN] Already done: {0,-50} (symlink exists, target {1})" -f $asset.LinkRel, $existing.Target | Write-Host
        } elseif ($existing) {
            "[DRY RUN] Would skip:    {0,-50} (real file already exists - conflict)" -f $asset.LinkRel | Write-Host
        } else {
            "[DRY RUN] Would install: {0,-50} -> {1}" -f $asset.LinkRel, $asset.SourceAbs | Write-Host
        }
    }
    if (-not $had) {
        Write-Host "[DRY RUN] No assets apply for this mode."
    }
    Write-Host ""
    $globalSettings = Join-Path $HOME '.claude\settings.json'
    Write-Host "[DRY RUN] Would register hooks in $($globalSettings):"
    Write-Host "[DRY RUN]   PreToolUse[Bash]: python .claude/hooks/convention-enforcer.py"
    Write-Host "[DRY RUN]   UserPromptSubmit: python .claude/hooks/convention-loader.py"
    Write-Host "[DRY RUN]   UserPromptSubmit: bash .claude/hooks/check-reminders.sh"
    Write-Host "[DRY RUN]   SessionStart: bash .claude/hooks/check-reminders.sh --force"
    Write-Host ""
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

if (-not $DryRun) {
    Invoke-Preflight
}

Invoke-SiblingCheck
Resolve-Vcs

if ($Unlink) {
    Invoke-Unlink
} elseif ($DryRun) {
    Invoke-DryRun
} else {
    Invoke-Install
}