param(
    [string]$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot ".."))
)

$ErrorActionPreference = "Stop"

$repositoryRootPath = (Resolve-Path -LiteralPath $RepositoryRoot).Path.TrimEnd("\", "/")
$sourceRoots = @(
    "apps",
    "extensions",
    "ide",
    "integrations",
    "kernel",
    "ui"
)
$forbiddenFileName = [regex]::new(
    "proof|demo|smoke|sample|fixture|test$|(?:^|[-_.])test(?:$|[-_.])|m\d+|v0|v1",
    [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
)
$forbiddenDeclarationName = [regex]::new(
    "(?:Proof|Demo|Smoke|Sample|Fixture)$|^(?:Proof|Demo|Smoke|Sample|Fixture|Test)|M\d+|V0|V1"
)
$productionDeclaration = [regex]::new(
    "\b(?:data\s+class|value\s+class|sealed\s+(?:class|interface)|enum\s+class|class|object|interface|typealias|fun|const\s+val|val|var)\s+([A-Za-z_][A-Za-z0-9_]*)"
)
$typescriptDeclaration = [regex]::new(
    "\b(?:export\s+)?(?:abstract\s+)?(?:class|interface|type|function|const|let|var)\s+([A-Za-z_][A-Za-z0-9_]*)"
)
$kotlinEnumEntry = [regex]::new("^\s*([A-Z][A-Z0-9_]*)\s*,?\s*$")
$violations = [System.Collections.Generic.List[string]]::new()

foreach ($sourceRoot in $sourceRoots) {
    $absoluteSourceRoot = Join-Path $repositoryRootPath $sourceRoot
    if (-not (Test-Path -LiteralPath $absoluteSourceRoot -PathType Container)) {
        continue
    }

    Get-ChildItem -LiteralPath $absoluteSourceRoot -Recurse -File | Where-Object {
        $_.FullName -match "[\\/]src[\\/](?:main|commonMain|jvmMain|desktopMain|browser)[\\/]" -and
        $_.FullName -notmatch "[\\/]node_modules[\\/]" -and
        $_.FullName -notmatch "[\\/]build[\\/]"
    } | ForEach-Object {
        $relativePath = $_.FullName.Substring($repositoryRootPath.Length).TrimStart("\", "/").Replace("\", "/")
        if ($forbiddenFileName.IsMatch($_.BaseName)) {
            $violations.Add("file:$relativePath")
        }

        if ($_.Extension -in @(".kt", ".java")) {
            $lineNumber = 0
            foreach ($line in [System.IO.File]::ReadLines($_.FullName)) {
                $lineNumber++
                foreach ($match in $productionDeclaration.Matches($line)) {
                    $declarationName = $match.Groups[1].Value
                    if ($forbiddenDeclarationName.IsMatch($declarationName)) {
                        $violations.Add("declaration:$relativePath`:$lineNumber`:$declarationName")
                    }
                }
                $enumEntryMatch = $kotlinEnumEntry.Match($line)
                if ($enumEntryMatch.Success) {
                    $entryName = $enumEntryMatch.Groups[1].Value
                    if ($forbiddenDeclarationName.IsMatch($entryName)) {
                        $violations.Add("enum-entry:$relativePath`:$lineNumber`:$entryName")
                    }
                }
            }
        }
        if ($_.Extension -in @(".ts", ".tsx")) {
            $lineNumber = 0
            foreach ($line in [System.IO.File]::ReadLines($_.FullName)) {
                $lineNumber++
                foreach ($match in $typescriptDeclaration.Matches($line)) {
                    $declarationName = $match.Groups[1].Value
                    if ($forbiddenDeclarationName.IsMatch($declarationName)) {
                        $violations.Add("declaration:$relativePath`:$lineNumber`:$declarationName")
                    }
                }
            }
        }
    }
}

$orderedViolations = $violations | Sort-Object -Unique
if ($orderedViolations.Count -gt 0) {
    Write-Error ("Production source-set hygiene violations:`n" + ($orderedViolations -join "`n"))
}

Write-Host "Source-set hygiene audit passed."
