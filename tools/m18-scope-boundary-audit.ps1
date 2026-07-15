$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$m18Artifacts = Join-Path $repoRoot '_bmad-output\implementation-artifacts\m18'
$m18Examples = Join-Path $repoRoot 'examples\m18'
$closeoutDoc = Join-Path $m18Artifacts 'm18-closeout-boundaries.md'

if (-not (Test-Path -LiteralPath $m18Artifacts)) {
    throw "Missing M18 implementation artifacts: $m18Artifacts"
}
if (-not (Test-Path -LiteralPath $m18Examples)) {
    throw "Missing M18 examples: $m18Examples"
}
if (-not (Test-Path -LiteralPath $closeoutDoc)) {
    throw "Missing M18 closeout boundary document: $closeoutDoc"
}

$scanExtensions = @('.md', '.yaml', '.yml', '.athena', '.txt', '.ps1')
$scanFiles = Get-ChildItem -LiteralPath $m18Artifacts, $m18Examples -Recurse -File |
    Where-Object { $scanExtensions -contains $_.Extension.ToLowerInvariant() }

$absolutePathMatches = foreach ($file in $scanFiles) {
    Select-String -LiteralPath $file.FullName -Pattern '\b[A-Za-z]:\\|file://|\\\\(?:\?\\)?' -AllMatches |
        ForEach-Object { "$($file.FullName):$($_.LineNumber): $($_.Line.Trim())" }
}

if ($absolutePathMatches) {
    $message = "M18 artifacts must use relative paths only:`n" + ($absolutePathMatches -join "`n")
    throw $message
}

$closeoutText = Get-Content -Raw -LiteralPath $closeoutDoc
$requiredPhrases = @(
    'remote registry',
    'package marketplace',
    'package publish flows',
    'full export/visibility',
    'broad authored-language redesign',
    'multi-root',
    'frontend-owned semantic resolution',
    'project semantic graph foundation',
    'apps/desktop-viewer',
    'Kotlin Compose'
)

$missingPhrases = $requiredPhrases | Where-Object {
    $closeoutText.IndexOf($_, [StringComparison]::OrdinalIgnoreCase) -lt 0
}

if ($missingPhrases) {
    throw "M18 closeout boundary document is missing required phrases: $($missingPhrases -join ', ')"
}

$forbiddenFixturePathTokens = @(
    'registry',
    'marketplace',
    'publish',
    'multi-root',
    'desktop-viewer',
    'compose',
    'frontend-resolver',
    'frontend-owned'
)

$fixtureRootPrefix = $m18Examples.TrimEnd('\', '/') + [System.IO.Path]::DirectorySeparatorChar
$badFixturePaths = foreach ($item in Get-ChildItem -LiteralPath $m18Examples -Recurse) {
    $relative = $item.FullName
    if ($relative.StartsWith($fixtureRootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        $relative = $relative.Substring($fixtureRootPrefix.Length)
    }
    $relative = $relative.Replace('\', '/')
    foreach ($token in $forbiddenFixturePathTokens) {
        if ($relative.IndexOf($token, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
            $relative
            break
        }
    }
}

if ($badFixturePaths) {
    $uniquePaths = $badFixturePaths | Sort-Object -Unique
    throw "M18 proof fixture paths imply deferred scope: $($uniquePaths -join ', ')"
}

Write-Host 'M18 scope boundary audit passed.'
