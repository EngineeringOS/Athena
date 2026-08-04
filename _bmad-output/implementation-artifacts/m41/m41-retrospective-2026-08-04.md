# M41 Retrospective

Date: 2026-08-04
Status: complete

## Outcome

M41 establishes compiler-owned Spatial Reality for the Golden rolling-shutter fixture. One
Projection session retains typed Sheet, Occurrence, Region, Construct, Anchor, Route, Lane, Grid
Reference, and quality facts through runtime, LSP, GLSP, and Theia product proof. Product proof
validates exact identity and geometry; it does not claim professional drafting parity.

## Evidence Ledger

- Source: file:///d%3A/Aaron/workspace/projects/2026/eos/Athena/examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena
- Source SHA-256: sha256:3af530db2f1390c5b873ed1f5134766293bf390301535adfcb94dfd3cb1a773e
- Active view/Sheet: schematic / schematic/sheet/S1
- Counts: 8 Occurrences, 3 Regions, 7 Constructs, 16 Anchors, 9 Routes, 7 used Lanes, 15 Grid References
- Blocking metrics: overlap 0, containment failure 0, Route/body intersection 0, twist 0
- Quality: crossings 3, peak Routes/Lane 2, Density 8/716800, Occupancy 25600/716800
- Spatial occupied span: desktop 848/1120 = 0.7571428571428571 width and 592/640 = 0.925 height; narrow 848/1120 = 0.7571428571428571 width and 592/640 = 0.925 height
- Pixel buckets: desktop horizontal [552, 598, 736], vertical [782, 322, 782]; narrow horizontal [260, 432, 390], vertical [404, 274, 404]
- Rendered DOM geometry: both viewports preserve 8 exact Component bounds and 9 exact Route point lists
- Baseline source digest: sha256:3af530db2f1390c5b873ed1f5134766293bf390301535adfcb94dfd3cb1a773e
- Baseline generation command: .\gradlew.bat :kernel:compiler:generateM41SpatialQualityBaseline -Pm41BaselineTimestamp=2026-08-04T01:20:00Z
- Desktop screenshot: D:\Aaron\workspace\projects\2026\eos\Athena\_bmad-output\implementation-artifacts\m41\screenshots\m41-rolling-shutter-desktop-1920x1080.png (1920x1080)
- Narrow screenshot: D:\Aaron\workspace\projects\2026\eos\Athena\_bmad-output\implementation-artifacts\m41\screenshots\m41-rolling-shutter-narrow.png (720x900)

## Sequential Verification

- node --test scripts/athena-m41-product-proof-contract.test.mjs: PASS (67 tests, 67 pass, 0 fail)
- node --test scripts/athena-m41-closure-contract.test.mjs: PASS (10 tests, 10 pass, 0 fail)
- yarn test (integrations/graph-glsp): PASS (9 tests, 9 pass, 0 fail)
- yarn test (ide/theia-frontend): PASS (219 tests, 219 pass, 0 fail)
- .\gradlew.bat --no-daemon --console=plain test: PASS (BUILD SUCCESSFUL, 148 actionable tasks)
- .\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist: PASS (BUILD SUCCESSFUL, 75 actionable tasks)
- yarn --cwd ide build: PASS (Theia browser/node/electron builds finished with 0 errors)
- yarn --cwd ide start:smoke:m41: PASS (desktop and narrow Electron smoke passed; proof regenerated)
- powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1: PASS (Source-set hygiene audit passed)
- powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1: PASS (Encoding audit passed)
- git diff --check: PASS (No whitespace errors)

## What Improved

- Placement is distributed across the governed Drawing Area and remains compiler-owned.
- Routes retain exact endpoint Anchors, ordered points, Lane ownership, and Projection identity.
- Presentation coordinates are compared against Spatial coordinates without renderer repair.
- Native pixel buckets reject blank or top-strip-only screenshots.
- Production source-set hygiene removes milestone-named proof authority and stale alternatives.

## Human Screenshot Review

- Desktop and narrow views show full two-dimensional Sheet content across all three horizontal and vertical Drawing Area buckets.
- Occurrence bodies and Routes remain visible at both viewport sizes without renderer coordinate repair.
- Labels remain small and overlap at fit-to-sheet zoom; M42 owns labels/styling and M44 owns readability optimization.
- Grid facts use vertical letters and horizontal numbers such as A1 and B3; painted grid chrome remains M42 scope.

## Honest Boundaries

M41 does not provide label layout, styling/readability optimization, rendering/export evolution, or
professional routing parity. No QElectroTech or EPLAN parity claim is made.

- M42: labels, styling, visibility, terminal labels, grid chrome.
- M43: SVG/Theia/PDF/Canvas rendering and Excel export.
- M44: readability optimization and quality target tuning.
- M45: professional routing, bundles/trunks, and multi-Sheet continuation.

## Process Lessons

- Runtime evidence must be checked before closure prose is written.
- Counts alone are insufficient; stable identities, ordered coordinates, pixel distribution, and
  exact metrics must be checked together.
- Milestone artifacts stay under the active milestone directory; deferred work remains explicit.

## Closure Decision

All M41 stories and Epics are marked done only after this evidence ledger, closure tests, BMad review,
and final audits pass.
