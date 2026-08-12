[CmdletBinding()]
param(
    [string]$Workspace
)

$ErrorActionPreference = 'Stop'

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$ideRoot = Join-Path $repositoryRoot 'ide'
if ([string]::IsNullOrWhiteSpace($Workspace)) {
    $Workspace = Join-Path $repositoryRoot 'examples\m46\rolling-shutter'
}
$workspaceRoot = (Resolve-Path $Workspace).Path
$logDirectory = Join-Path $repositoryRoot '.athena\logs'
$logPath = Join-Path $logDirectory 'clean-start.log'

if (-not (Test-Path (Join-Path $workspaceRoot 'athena.yaml'))) {
    throw "Workspace must contain athena.yaml: $workspaceRoot"
}

# Stop only Athena Electron launchers rooted in this repository. Never kill unrelated Electron processes.
Get-CimInstance Win32_Process -Filter "Name = 'electron.exe'" |
    Where-Object { $_.CommandLine -and $_.CommandLine.Contains($repositoryRoot) -and $_.CommandLine.Contains('athena-electron-main.js') } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

Push-Location $repositoryRoot
try {
    .\gradlew.bat --no-daemon --console=plain :ide:lsp:clean :ide:lsp:installDist
    if ($LASTEXITCODE -ne 0) { throw "LSP rebuild failed with exit code $LASTEXITCODE." }
} finally {
    Pop-Location
}

Push-Location $ideRoot
try {
    & yarn.cmd clean
    if ($LASTEXITCODE -ne 0) { throw "IDE clean failed with exit code $LASTEXITCODE." }
    & yarn.cmd build
    if ($LASTEXITCODE -ne 0) { throw "IDE build failed with exit code $LASTEXITCODE." }
} finally {
    Pop-Location
}

New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
$escapedIdeRoot = $ideRoot.Replace("'", "''")
$escapedWorkspaceRoot = $workspaceRoot.Replace("'", "''")
$escapedLogPath = $logPath.Replace("'", "''")
$launch = "Set-Location -LiteralPath '$escapedIdeRoot'; & yarn.cmd workspace @engineeringood/athena-theia-product start '$escapedWorkspaceRoot' *>&1 | Tee-Object -FilePath '$escapedLogPath'"

Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $launch)
Write-Output "Athena rebuilt and starting with workspace: $workspaceRoot"
Write-Output "Launch log: $logPath"
