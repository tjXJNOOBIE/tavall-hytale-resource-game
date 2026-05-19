param(
    [string]$SshAlias = "novus-remote",
    [string]$SshConfigPath = "C:\Users\TJ\.ssh\config",
    [int]$TunnelPort = 25569,
    [int]$RemoteProxyPort = 25565,
    [string]$RemoteProxyHost = "127.0.0.1",
    [switch]$NoTunnel,
    [string]$MinecraftBotDir = "",
    [string]$ScenarioScriptPath = "",
    [string]$Username = "ResourceProxyBot",
    [string]$MinecraftVersion = "1.21.4",
    [string]$OutputDir = "",
    [string]$LogDir = "",
    [string]$ServerLogPath = "/srv/mc-kingdom-servers/mc-kingdom-server-1/logs/latest.log",
    [switch]$SkipServerLogCheck
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($MinecraftBotDir)) {
    $MinecraftBotDir = [System.IO.Path]::GetFullPath((Join-Path $repoRoot "..\..\tavall-java-game-tools\minecraft-bot"))
}
if ([string]::IsNullOrWhiteSpace($ScenarioScriptPath)) {
    $ScenarioScriptPath = Join-Path $repoRoot "scripts\minecraft-velocity-control-plane-flow.mjs"
}
if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $repoRoot "bot-logs"
}
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $OutputDir = Join-Path $LogDir ("local-minecraft-velocity-control-plane-{0}" -f $timestamp)
}

if (-not (Test-Path -LiteralPath $MinecraftBotDir)) {
    throw "Minecraft bot workspace not found at $MinecraftBotDir"
}
if (-not (Test-Path -LiteralPath $ScenarioScriptPath)) {
    throw "Scenario script not found at $ScenarioScriptPath"
}

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$baseName = "local-minecraft-velocity-control-plane-$timestamp"
$runLogPath = Join-Path $LogDir "$baseName-run.txt"
$summaryPath = Join-Path $LogDir "$baseName-summary.txt"

function Write-LogLine {
    param([string]$Message)
    Add-Content -Path $runLogPath -Value $Message -Encoding utf8
    Write-Host $Message
}

function Invoke-LoggedCommand {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$WorkingDirectory = $null
    )

    $stdoutPath = [System.IO.Path]::GetTempFileName()
    $stderrPath = [System.IO.Path]::GetTempFileName()
    $stdoutLines = [System.Collections.Generic.List[string]]::new()
    $stderrLines = [System.Collections.Generic.List[string]]::new()
    try {
        $startParams = @{
            FilePath = $FilePath
            ArgumentList = $Arguments
            Wait = $true
            NoNewWindow = $true
            PassThru = $true
            RedirectStandardOutput = $stdoutPath
            RedirectStandardError = $stderrPath
        }
        if (-not [string]::IsNullOrWhiteSpace($WorkingDirectory)) {
            $startParams.WorkingDirectory = $WorkingDirectory
        }

        $process = Start-Process @startParams

        foreach ($path in @($stdoutPath, $stderrPath)) {
            if (-not (Test-Path $path)) {
                continue
            }
            Get-Content -Path $path | ForEach-Object {
                Add-Content -Path $runLogPath -Value $_ -Encoding utf8
                if ($path -eq $stdoutPath) {
                    $stdoutLines.Add($_)
                } else {
                    $stderrLines.Add($_)
                }
            }
        }

        return [pscustomobject]@{
            ExitCode = [int]$process.ExitCode
            StdoutLines = $stdoutLines.ToArray()
            StderrLines = $stderrLines.ToArray()
        }
    } finally {
        foreach ($path in @($stdoutPath, $stderrPath)) {
            if (Test-Path $path) {
                Remove-Item -Path $path -Force
            }
        }
    }
}

$startedAt = (Get-Date).ToString("o")
$tunnelProcess = $null
$connectHost = $RemoteProxyHost
try {
    Write-LogLine ("[{0}] Starting local Minecraft bot harness" -f $startedAt)
    Write-LogLine ("[{0}] SshAlias={1}" -f (Get-Date).ToString("o"), $SshAlias)
    Write-LogLine ("[{0}] TunnelPort={1}" -f (Get-Date).ToString("o"), $TunnelPort)
    Write-LogLine ("[{0}] RemoteProxy={1}:{2}" -f (Get-Date).ToString("o"), $RemoteProxyHost, $RemoteProxyPort)
    Write-LogLine ("[{0}] NoTunnel={1}" -f (Get-Date).ToString("o"), [bool]$NoTunnel)
    Write-LogLine ("[{0}] MinecraftBotDir={1}" -f (Get-Date).ToString("o"), $MinecraftBotDir)
    Write-LogLine ("[{0}] ScenarioScriptPath={1}" -f (Get-Date).ToString("o"), $ScenarioScriptPath)

    if (-not (Test-Path -LiteralPath (Join-Path $MinecraftBotDir "node_modules"))) {
        throw "Minecraft bot dependencies were not found at $MinecraftBotDir\nInstall them in tavall-java-game-tools/minecraft-bot first."
    }

    if ($NoTunnel) {
        if ([string]::IsNullOrWhiteSpace($RemoteProxyHost) -or $RemoteProxyHost -eq "127.0.0.1") {
            $resolvedHost = & ssh.exe -F $SshConfigPath -G $SshAlias 2>$null |
                Where-Object { $_ -match '^hostname\s+' } |
                ForEach-Object { ($_ -replace '^hostname\s+', '').Trim() } |
                Select-Object -First 1
            if ([string]::IsNullOrWhiteSpace($resolvedHost)) {
                throw "Could not resolve remote proxy host for alias $SshAlias."
            }
            $connectHost = $resolvedHost
        }
        Write-LogLine ("[{0}] Connecting directly to remote proxy without tunnel: {1}:{2}" -f (Get-Date).ToString("o"), $connectHost, $RemoteProxyPort)
    } else {
        $forwardArgs = @(
            "-F", $SshConfigPath,
            "-N",
            "-L", "$TunnelPort`:$RemoteProxyHost`:$RemoteProxyPort",
            $SshAlias
        )
        Write-LogLine ("[{0}] Opening SSH tunnel for local client probing." -f (Get-Date).ToString("o"))
        $tunnelProcess = Start-Process -FilePath "ssh.exe" -ArgumentList $forwardArgs -PassThru

        $deadline = (Get-Date).AddSeconds(30)
        do {
            $connection = Test-NetConnection -ComputerName 127.0.0.1 -Port $TunnelPort -WarningAction SilentlyContinue
            if ($connection.TcpTestSucceeded) {
                break
            }
            if ($tunnelProcess.HasExited) {
                throw "SSH tunnel exited before opening local port $TunnelPort."
            }
            Start-Sleep -Milliseconds 500
        } while ((Get-Date) -lt $deadline)

        $connection = Test-NetConnection -ComputerName 127.0.0.1 -Port $TunnelPort -WarningAction SilentlyContinue
        if (-not $connection.TcpTestSucceeded) {
            throw "Timed out waiting for SSH tunnel to open local port $TunnelPort."
        }
        $connectHost = "127.0.0.1"
    }

    $scenarioArgs = @(
        $ScenarioScriptPath,
        $connectHost,
        ($(if ($NoTunnel) { $RemoteProxyPort } else { $TunnelPort })).ToString(),
        $Username,
        $MinecraftVersion,
        $OutputDir
    )

    $localResult = Invoke-LoggedCommand -FilePath "node.exe" -Arguments $scenarioArgs -WorkingDirectory $MinecraftBotDir
    $exitCode = $localResult.ExitCode

    if (-not $SkipServerLogCheck) {
        Write-LogLine ("[{0}] Checking remote server log for resource-pack status." -f (Get-Date).ToString("o"))
        $serverLogProbe = @"
set -euo pipefail
grep -nE 'Resource pack status from|Sending forced resource pack|Skipping local resource pack HTTP server|FAILED_DOWNLOAD|SUCCESSFULLY_LOADED|DECLINED|INVALID_URL|ACCEPTED' '$ServerLogPath' | tail -n 40
"@
        try {
            $serverResult = Invoke-LoggedCommand -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $serverLogProbe)
            if ($serverResult.StdoutLines.Count -gt 0) {
                Write-Host ($serverResult.StdoutLines -join "`n")
            }
        } catch {
            Write-LogLine ("[{0}] Server log probe failed: {1}" -f (Get-Date).ToString("o"), $_.Exception.Message)
        }
    }

    $summary = [ordered]@{
        startedAt = $startedAt
        completedAt = (Get-Date).ToString("o")
        sshAlias = $SshAlias
        tunnelPort = $TunnelPort
        noTunnel = [bool]$NoTunnel
        remoteProxyHost = $RemoteProxyHost
        connectHost = $connectHost
        remoteProxyPort = $RemoteProxyPort
        minecraftBotDir = $MinecraftBotDir
        scenarioScriptPath = $ScenarioScriptPath
        username = $Username
        minecraftVersion = $MinecraftVersion
        outputDir = $OutputDir
        exitCode = $exitCode
        success = ($exitCode -eq 0)
        logPath = $runLogPath
    }

    Set-Content -Path $summaryPath -Value ($summary.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" }) -Encoding utf8
    Write-LogLine ("[{0}] SummaryFile={1}" -f (Get-Date).ToString("o"), $summaryPath)
    Write-LogLine ("[{0}] ExitCode={1}" -f (Get-Date).ToString("o"), $exitCode)

    if ($exitCode -ne 0) {
        exit $exitCode
    }
} finally {
    if ($tunnelProcess -and -not $tunnelProcess.HasExited) {
        try {
            Stop-Process -Id $tunnelProcess.Id -ErrorAction SilentlyContinue
        } catch {
        }
    }
}
