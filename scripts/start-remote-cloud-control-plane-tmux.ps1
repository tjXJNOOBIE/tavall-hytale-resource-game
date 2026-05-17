param(
    [string]$SshAlias = "novus-remote",
    [string]$SshConfigPath = "C:\Users\TJ\.ssh\config",
    [string]$RemoteCloudControlPlaneDir = "/srv/control-plane",
    [string]$CloudControlPlaneJarPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/cloud-control-plane/target/cloud-control-plane-0.1.1-SNAPSHOT.jar",
    [string]$CloudControlPlaneSessionName = "cloud-control-plane"
)

$ErrorActionPreference = "Stop"

function Invoke-Checked {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$FailureMessage
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw $FailureMessage
    }
}

if (-not (Test-Path $CloudControlPlaneJarPath)) {
    throw "Cloud control plane jar not found at $CloudControlPlaneJarPath"
}

$localStartScriptPath = Join-Path $env:TEMP "cloud-control-plane-start.sh"
$startScriptContent = @'
#!/usr/bin/env bash
set -euo pipefail
cd /srv/control-plane
export TAVALL_CLOUD_CONTROL_PLANE_ROOT="/srv/control-plane"
mkdir -p logs workloads consoles cache kingdoms/kingdom-1
exec java --enable-preview -jar /srv/control-plane/cloud-control-plane.jar >> /srv/control-plane/logs/cloud-control-plane.out.log 2>&1
'@
[System.IO.File]::WriteAllText($localStartScriptPath, ($startScriptContent -replace "`r`n", "`n"), [System.Text.Encoding]::ASCII)

Write-Host "Deploying cloud control plane to $RemoteCloudControlPlaneDir"
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, "mkdir -p '$RemoteCloudControlPlaneDir'") -FailureMessage "Failed to prepare remote cloud control plane directory."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $CloudControlPlaneJarPath, "$SshAlias`:$RemoteCloudControlPlaneDir/cloud-control-plane.jar.new") -FailureMessage "Failed to copy cloud control plane jar."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $localStartScriptPath, "$SshAlias`:$RemoteCloudControlPlaneDir/start.sh.new") -FailureMessage "Failed to copy cloud control plane start script."
Invoke-Checked -FilePath "ssh.exe" -Arguments @(
    "-F",
    $SshConfigPath,
    $SshAlias,
    "cd '$RemoteCloudControlPlaneDir' && mv cloud-control-plane.jar.new cloud-control-plane.jar && mv start.sh.new start.sh && chmod +x start.sh && mkdir -p logs workloads consoles cache kingdoms/kingdom-1 && tmux has-session -t '$CloudControlPlaneSessionName' 2>/dev/null && tmux kill-session -t '$CloudControlPlaneSessionName' || true && tmux new-session -d -s '$CloudControlPlaneSessionName' 'bash /srv/control-plane/start.sh' && for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20; do if tmux has-session -t '$CloudControlPlaneSessionName' 2>/dev/null; then break; fi; sleep 1; done && tmux list-sessions"
) -FailureMessage "Failed to start remote cloud control plane."

Remove-Item -LiteralPath $localStartScriptPath -Force -ErrorAction SilentlyContinue
Write-Host ""
Write-Host "attach commands:"
Write-Host "tmux attach -t $CloudControlPlaneSessionName"
