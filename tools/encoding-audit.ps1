[CmdletBinding()]
param(
    [string]$RepoRoot = "."
)

$ErrorActionPreference = "Stop"

$utf8Strict = [System.Text.UTF8Encoding]::new($false, $true)
$bom = [byte[]](0xEF, 0xBB, 0xBF)
$trackedTextExtensions = @(
    ".md", ".txt", ".yml", ".yaml", ".toml", ".json", ".xml",
    ".kt", ".kts", ".ts", ".tsx", ".js", ".jsx", ".css", ".html"
)
$replacementCharacter = [string]([char]0xFFFD)

function Get-TrackedFiles {
    param([string]$Root)

    $gitFiles = @()
    try {
        $gitFiles = & git -C $Root ls-files
    } catch {
        $gitFiles = @()
    }

    if ($LASTEXITCODE -eq 0 -and $gitFiles.Count -gt 0) {
        return $gitFiles | ForEach-Object { Join-Path $Root $_ }
    }

    return Get-ChildItem -Path $Root -Recurse -File |
        Where-Object {
            $_.FullName -notmatch "\\(build|node_modules|\.git|\.gradle|\.idea|\.codegraph)\\"
        } |
        ForEach-Object FullName
}

$issues = [System.Collections.Generic.List[string]]::new()

foreach ($path in Get-TrackedFiles -Root $RepoRoot) {
    if (-not (Test-Path $path -PathType Leaf)) {
        continue
    }

    $extension = [System.IO.Path]::GetExtension($path)
    if ($trackedTextExtensions -notcontains $extension) {
        continue
    }

    $bytes = [System.IO.File]::ReadAllBytes($path)
    try {
        $text = $utf8Strict.GetString($bytes)
    } catch {
        $issues.Add("INVALID_UTF8 $path")
        continue
    }

    if ($text.Contains($replacementCharacter)) {
        $issues.Add("REPLACEMENT_CHARACTER $path")
    }

    if ($path -like "*.zh-CN.md") {
        $hasBom = $bytes.Length -ge 3 -and
            $bytes[0] -eq $bom[0] -and
            $bytes[1] -eq $bom[1] -and
            $bytes[2] -eq $bom[2]
        if (-not $hasBom) {
            $issues.Add("MISSING_UTF8_BOM $path")
        }
        $hasDuplicateBom = $bytes.Length -ge 6 -and
            $bytes[0] -eq $bom[0] -and
            $bytes[1] -eq $bom[1] -and
            $bytes[2] -eq $bom[2] -and
            $bytes[3] -eq $bom[0] -and
            $bytes[4] -eq $bom[1] -and
            $bytes[5] -eq $bom[2]
        if ($hasDuplicateBom) {
            $issues.Add("DUPLICATE_UTF8_BOM $path")
        }
    }
}

if ($issues.Count -gt 0) {
    $issues | Sort-Object -Unique | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Encoding audit passed."
