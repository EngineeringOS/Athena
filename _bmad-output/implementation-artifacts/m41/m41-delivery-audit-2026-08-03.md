# M41 Delivery Audit

Date: 2026-08-03

Verdict: **failed closure gate**. M41 has useful authority boundaries and some traceability work,
but it is not finished and does not meet its own PRD. The focused M41 tests pass because they
mostly verify determinism, field presence, constants, or script text. They do not reject the
visibly unusable result captured by the milestone screenshots.

## Critical Findings

1. **Placement never became milestone-grade.** The PRD explicitly distinguishes M40's simple row
   proof from M41 placement, but `SpatialPlacementCompiler` still emits `x = index * 140.0` and
   `y = 0.0` for every node (`SpatialPlacementCompiler.kt:34-43`). Regions only change the reason
   string; constructs and reading order do not affect placement. Both screenshots show the result:
   all subjects and routes collapse into one strip at the top of an otherwise empty sheet.

2. **E2E accepts unusable output.** The verifier accepts any valid PNG larger than 1024 bytes
   (`verify-athena-m41-product-proof.js:162-168`). It performs no canvas-pixel, geometry,
   readability, overlap, or route visibility checks. `paintOnlyRenderer` is hardcoded to four
   `false` values (`verify-athena-m41-product-proof.js:123-128`) and then asserted, so it proves its
   own fixture rather than runtime behavior. `routeProofPresent` is collected but never asserted.

3. **Quality metrics are not the metrics their names claim.** `label-pressure` is `routes.size`,
   and `labelCount` is read from `route-count` (`SpatialQualityCompiler.kt:22-24`,
   `SpatialQualityMetricsReporter.kt:22-24`). `bodyIntersectionCount` counts route vertices inside
   boxes rather than segment/body intersections (`SpatialQualityCompiler.kt:38-46`). Crossing
   calculation compares segments from the same route and counts inclusive endpoint contacts
   (`SpatialQualityCompiler.kt:49-53,91-108`). These facts cannot support FR-11 through FR-13.

4. **Tests certify wrong behavior.** `M41GeometryQualityTest` asserts a value equals itself at line
   125, declares two local constants to prove the M40 baseline at lines 89-95, and treats
   `route-count` as label count at lines 132-150. `DedicatedM41ExampleTest` accepts any nonempty
   Presentation output and `labelCount >= 0` at lines 49-55. Focused M41 tests pass, but these
   assertions cannot detect the screenshot failure or false metrics.

5. **Story decomposition dropped PRD requirements.** Scope and FR-1 require placement for every
   occurrence, region, and construct, while story acceptance only checks that existing placements
   are deterministic and have reasons. Story 3.1 drops source trace, Story 3.2 substitutes lane
   identity for lane-without-placement, and Story 4.2 substitutes constants/per-document output for
   same-viewport, per-sheet baseline results. Passing story tests therefore does not prove the PRD.

6. **Routing can silently lose engineering connections.** `SpatialRouteCompiler.routesFor` falls
   back to the first/last occurrence when endpoint identities are absent, then uses `mapNotNull` to
   drop any connection whose endpoint anchor cannot be found (`SpatialRouteCompiler.kt:91-104`).
   No diagnostic is returned for those per-connection failures. This violates fail-closed routing,
   exact traceability, and the pre-1.0 no-compatibility rule.

## High Findings

7. **Construct geometry is not geometry.** `deriveConstructBounds` emits only the maximum member
   width and height, with no construct placement and no min/max member extents
   (`AuthoredProjectionSpatialBridge.kt:34-53`). A construct spanning several occurrences still
   gets an 80x40-like box. The code calls this an envelope, but it cannot locate or enclose one.

8. **Region and construct placement is absent.** `SpatialDocument.placements` contains occurrence
   placements only. Construct facts are appended later as bounds and grid-map entries; regions get
   no spatial facts. Global acceptance criterion 1 and the PRD scope therefore fail.

9. **Multi-sheet behavior is structurally unsupported.** Grid references always use the first
   sheet carrying a grid and fixed 1200x800 dimensions
   (`AuthoredProjectionSpatialBridge.kt:84-118`). Placements have no sheet identity, metrics are
   per-document, and the single-sheet example masks the defect. FR-11 and FR-12 require facts per
   composed sheet.

10. **Geometry validation is far below FR-10.** `SpatialReality.validate` does not verify one bound
    per placement, route endpoint equality, orthogonality, source/target occurrence consistency,
    required port trace, alignment references, grid references, duplicate IDs, finite coordinates,
    or in-sheet bounds (`SpatialDocument.kt:69-103`). Diagnostics such as `missing lane identity`
    also omit the exact subject and correction required by NFR-7.

11. **Identity/source-trace claims exceed the model.** `SpatialQualityMeasurement` has only
    `kind` and `value`; grid references are a raw `Map<String, String>`; bounds reuse an
    `occurrenceId` field for construct IDs. Story 3.1 tests only nonblank IDs, not source trace or
    stability. FR-9 is not verified.

12. **Baseline comparison was never published.** M41 artifacts contain the inherited numbers 28
    and 0, but no measured M40/M41 result table for the same viewport and zoom. Story 4.2 calls
    asserting constants a baseline cross-reference. PRD FR-13 and global AC 5 fail.

13. **Sprint state proves M41 is not complete.** All 13 stories are `review`, none are `done`.
    Epics E2-E4 remain `backlog`, while their stories are `review`; E1/E5 remain `in-progress`.
    Meanwhile the retrospective is marked `complete`. This violates the repository story process
    and makes closure status internally contradictory.

14. **Checked TDD tasks are factually false.** Stories 2.1, 2.2, 3.1, 4.1, and 4.2 check a task
    requiring a failing red test, then record that the added test passed on its first run. Honest
    verification is fine, but the red task was not completed and must not be checked.

15. **No reviewable milestone delta exists in Git.** `HEAD` remains
    `1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62`, the same baseline recorded by every story. M41
    product files, tests, artifacts, and example are untracked. The worktree has 1,222 entries
    (683 untracked, 308 modified, 231 deleted), so M41 cannot be isolated from M37-M40 or reliably
    reviewed against its claimed baseline. `git diff --check` also does not inspect untracked M41
    files.

## Medium Findings

16. **Presentation output still exposes placeholder milestone residue.** The transformation emits a
    generic rectangle pack and title metadata with `sheetFamily = "M39"`
    (`SpatialToPresentationTransformation.kt:151-173,192-219`). Production comments in the M41
    spatial bridge and metrics reporter still describe M40 stories. This contradicts clean current
    product concepts even though the class names avoid milestone suffixes.

17. **Retrospective overstates closure.** It correctly admits simple-row placement and missing
    rung/rail composition, but still claims Spatial Reality was established and quality was measured
    without publishing valid metric results. It is an interim review note, not a completed milestone
    retrospective.

## Verification Performed

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "*SpatialMilestoneTest*" --tests "*M41GeometryQualityTest*" --tests "*DedicatedM41ExampleTest*"
```

Result: `BUILD SUCCESSFUL` in 23 seconds. This confirms the focused suite is green while the defects
above remain observable in source and screenshots.

## Required Recovery

1. Reopen M41: remove the completed retrospective claim; keep stories out of `done`.
2. Correct the PRD exit gate and metric definitions before rewriting stories.
3. Recreate stories from atomic PRD consequences, with coverage for every subject and every sheet.
4. Replace row placement, construct pseudo-bounds, route fallbacks, and placeholder metrics.
5. Add behavioral E2E assertions and screenshot pixel/layout checks that fail on the current images.
6. Produce a clean, reviewable M41 delta and only then run full sequential verification and closure.
