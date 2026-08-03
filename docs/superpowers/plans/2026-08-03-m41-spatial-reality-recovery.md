# M41 Spatial Reality Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development
> (recommended) or superpowers:executing-plans to implement this plan task-by-task. Every numbered
> story MUST additionally use `bmad-create-story` followed by `bmad-dev-story`; neither wrapper may
> replace or abbreviate the BMad workflow.

**Goal:** Reopen M41 and deliver deterministic, coherent, validated Spatial placement, geometry,
routing, metrics, and product proof from Story 1.1 through Story 5.3.

**Architecture:** Replace the duplicate row-layout and placeholder metric pipeline with one
`ProjectionSpatialCompiler`. Adapt the current rule-based layout engine and obstacle-aware routing
engine behind typed, per-sheet Spatial facts; validate complete geometry before immutable
Presentation mapping.

**Tech Stack:** Kotlin 2.4, Gradle 9.6.1, Kotlin test, Node.js 22+, Theia 1.73.1, Electron 39.8.7,
Playwright/Electron smoke infrastructure, BMad planning/story workflows.

---

## Execution Rules

- Work in the existing workspace. A clean worktree is unsafe because M37-M41 foundation is
  uncommitted and absent from `HEAD`.
- Never run two Gradle commands concurrently. Wait for each invocation to finish.
- Never hand-write a story file. Invoke `bmad-create-story` for its exact story key.
- Never implement outside `bmad-dev-story`. Follow story tasks in order and prove red before green.
- Pass the milestone-scoped paths explicitly to every BMad story workflow:
  `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`, the M41 `epics.md`, PRD, and
  architecture spine. Do not let generic artifact discovery select another milestone.
- Run BMad code review after each story reaches `review`. Fix findings through
  `bmad-dev-story` review continuation. Mark `done` only after acceptance passes.
- Stage and commit only files named by the active story. Do not absorb unrelated dirty-worktree
  changes.
- Before every displayed commit command, stage the exact paths from the story File List and run
  `git diff --cached --check`.
- After every text/document change run `tools/encoding-audit.ps1`.
- After production cleanup run `tools/source-set-hygiene-audit.ps1`.
- Story verification sequence: focused test, affected module tests, repository `test`, audits,
  `git diff --check`. Final milestone sequence adds `build`, frontend tests, LSP install, Electron
  E2E, screenshot inspection, and pixel/layout checks.

## Target File Structure

### Spatial Model

- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
  - document/sheet roots and `SpatialReality` metadata only.
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt`
  - points, rectangles, occurrences, regions, constructs, anchors, grid facts, source trace.
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
  - lanes, orthogonal segments, routes, endpoint trace.
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialQualityModels.kt`
  - typed per-sheet quality snapshot.
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt`
  - complete document validator and actionable diagnostic model.

Core target contract:

```kotlin
data class SpatialDocument(val sheets: List<SpatialSheet>)

data class SpatialSheet(
    val sheetId: String,
    val extent: SpatialRect,
    val drawingArea: SpatialRect,
    val grid: SpatialGrid?,
    val occurrences: List<SpatialOccurrenceGeometry>,
    val regions: List<SpatialRegionGeometry>,
    val constructs: List<SpatialConstructGeometry>,
    val anchors: List<SpatialAnchorPosition>,
    val lanes: List<SpatialLane>,
    val routes: List<SpatialRoute>,
    val gridReferences: List<SpatialGridReference>,
    val quality: SpatialQualitySnapshot,
    val sourceTrace: SpatialSourceTrace,
)

data class SpatialRect(val x: Int, val y: Int, val width: Int, val height: Int)
data class SpatialSourceTrace(
    val projectionIds: List<String>,
    val geometryElementIds: List<String>,
)
```

### Compiler

- Create `ProjectionSpatialCompiler.kt`: complete orchestration and final validation.
- Create `ProjectionSpatialLayout.kt`: Projection-to-layout intent adapter and normalized facts.
- Create `SpatialGeometryCompiler.kt`: region/construct union geometry and grid mapping.
- Create `SpatialAnchorCompiler.kt`: stable per-port anchor placement.
- Rewrite `SpatialRouteCompiler.kt`: generic adapter over current routing engine geometry core.
- Rewrite `SpatialQualityCompiler.kt`: exact per-sheet formulas.
- Rewrite `SpatialToPresentationTransformation.kt`: immutable typed mapping.
- Delete after callers move: `AuthoredProjectionSpatialBridge.kt`,
  `ProjectionToSpatialTransformation.kt`, `SpatialPlacementCompiler.kt`,
  `SpatialQualityMetricsReporter.kt`.

### Tests

- Keep tests split by responsibility:
  `SpatialPlacementCompilerTest.kt`, `SpatialGeometryCompilerTest.kt`,
  `SpatialGridReferenceCompilerTest.kt`, `SpatialAnchorCompilerTest.kt`,
  `SpatialRouteCompilerTest.kt`, `SpatialQualityCompilerTest.kt`,
  `SpatialValidationTest.kt`, `SpatialToPresentationTransformationTest.kt`,
  `DedicatedM41ExampleTest.kt`.
- Remove or rewrite shallow assertions in `SpatialMilestoneTest.kt` and
  `M41GeometryQualityTest.kt`; no self-comparisons, local baseline constants, minimum-only counts,
  or script-text proof.

## Task 0: Reopen And Replan M41 With BMad

**Files:**
- Modify: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md`
- Modify: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md`
- Modify via memlog script: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/.memlog.md`
- Modify: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md`
- Replace through BMad: `_bmad-output/implementation-artifacts/m41/epics.md`
- Replace through BMad: `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- Remove stale closure: `_bmad-output/implementation-artifacts/m41/m41-retrospective-2026-08-03.md`
- Remove stale stories before BMad recreation: `_bmad-output/implementation-artifacts/m41/[1-5]-*.md`

- [ ] **Step 1: Run BMad correct-course**

Invoke `bmad-correct-course` with the approved recovery design and delivery audit. Record that M41
is reopened, all stories reset, old proof invalidated, and compatibility with the failed pass is a
non-goal.

- [ ] **Step 2: Update PRD through BMad**

Invoke `bmad-prd` in Update mode. Source inputs:

```text
docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md
_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md
_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/review-rubric.md
```

Required result: atomic FR consequence IDs, subject-by-fact matrix, metric dictionary, observable
exit gates, no label metric, no stale confirmed decisions.

- [ ] **Step 3: Update architecture through BMad**

Invoke `bmad-architecture` Update intent. Bind the typed per-sheet contract, internal solver
adapters, no-fallback routing, full validation, and product proof gates.

- [ ] **Step 4: Regenerate epics and stories through BMad**

Invoke `bmad-create-epics-and-stories`. Preserve story numbering 1.1 through 5.3, but regenerate
all acceptance criteria from atomic PRD consequences. No implementation story may weaken
quantifiers or substitute constants for measurements.

- [ ] **Step 5: Regenerate sprint tracking through BMad**

Invoke `bmad-sprint-planning`. Expected initial state: all five epics `backlog`, all 13 stories
`backlog`, retrospectives `optional`; no milestone retrospective `complete`.

- [ ] **Step 6: Validate replacement planning artifacts through BMad**

Invoke `bmad-prd` in Validate mode against the replacement M41 PRD, then invoke
`bmad-check-implementation-readiness` against the replacement PRD, architecture, and epics. Fix
every critical/high finding through the owning BMad workflow before Story 1.1. Required result:
no broken rubric dimension, no unresolved critical/high finding, and no weakened or untestable
acceptance consequence.

- [ ] **Step 7: Remove invalid proof artifacts**

Delete the 13 failed story files, two failed M41 screenshots, and completed retrospective after the
replacement planning artifacts explicitly mark them invalid. Preserve the delivery audit as
historical evidence. `bmad-create-story` must create each replacement story from a missing path.

- [ ] **Step 8: Verify planning reset**

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Expected: audits pass; sprint has 13 backlog stories and zero review/done stories.

- [ ] **Step 9: Commit planning reset**

```powershell
git add -- _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41 `
  _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41 `
  _bmad-output/implementation-artifacts/m41/epics.md `
  _bmad-output/implementation-artifacts/m41/sprint-status.yaml `
  _bmad-output/implementation-artifacts/m41/m41-retrospective-2026-08-03.md `
  _bmad-output/implementation-artifacts/m41/1-1-derive-deterministic-explainable-placement.md `
  _bmad-output/implementation-artifacts/m41/1-2-derive-bounds-and-alignment.md `
  _bmad-output/implementation-artifacts/m41/1-3-derive-grid-reference-facts.md `
  _bmad-output/implementation-artifacts/m41/2-1-build-anchor-accurate-routes.md `
  _bmad-output/implementation-artifacts/m41/2-2-build-orthogonal-route-facts-and-lanes.md `
  _bmad-output/implementation-artifacts/m41/2-3-trace-routes-and-hold-the-no-optimization-boundary.md `
  _bmad-output/implementation-artifacts/m41/3-1-publish-explicit-geometry-facts.md `
  _bmad-output/implementation-artifacts/m41/3-2-enforce-geometry-validation.md `
  _bmad-output/implementation-artifacts/m41/4-1-measure-quality-milestone-facts.md `
  _bmad-output/implementation-artifacts/m41/4-2-measure-density-and-occupancy-against-the-baseline.md `
  _bmad-output/implementation-artifacts/m41/5-1-build-the-dedicated-m41-example.md `
  _bmad-output/implementation-artifacts/m41/5-2-build-the-m41-e2e-evidence.md `
  _bmad-output/implementation-artifacts/m41/5-3-close-m41-with-an-honest-retrospective.md `
  _bmad-output/implementation-artifacts/m41/screenshots
git diff --cached --check
git commit -m "docs(m41): reopen spatial reality milestone"
```

## Task 1: Story 1.1 - Deterministic Explainable 2D Placement

**Files:**
- BMad create/modify: `_bmad-output/implementation-artifacts/m41/1-1-derive-deterministic-explainable-placement.md`
- Create: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt`
- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt`
- Rewrite: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt`
- Rewrite test: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompilerTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for `1-1-derive-deterministic-explainable-placement`**

Require exact coverage, distinct region columns, non-overlap, in-sheet geometry, reason/source trace,
input permutation determinism, and a failing single-row regression.

Bind exact approved policy in story acceptance tests: sheet extent `1200 x 800`, drawing area
`SpatialRect(40, 60, 1120, 640)`, title-block boundary `y = 740`, three left-to-right region
columns separated by a 32-unit gutter, at least 48 units of vertical occurrence separation, and
24 units of region/construct envelope padding. Unassigned occurrences use an explicit final region
and stable order.

- [ ] **Step 2: Invoke `bmad-dev-story` with the created story path**

First red test must assert three distinct region-column X ranges and meaningful Y range for the M41
fixture. Minimal target API:

```kotlin
class ProjectionSpatialLayout {
    fun place(projection: ProjectionDocument): SpatialLayoutResult
}

data class SpatialLayoutResult(
    val occurrences: List<SpatialOccurrenceGeometry>,
    val diagnostics: List<RealityTransformationDiagnostic>,
)
```

- [ ] **Step 3: Run story verification sequentially**

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "*SpatialPlacementCompilerTest*"
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

- [ ] **Step 4: Run BMad code review, fix through `bmad-dev-story`, mark done, commit**

```powershell
git commit -m "feat(spatial): derive deterministic two-dimensional placement"
```

## Task 2: Story 1.2 - Bounds, Alignment, Regions, And Constructs

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/1-2-derive-bounds-and-alignment.md`
- Modify: `SpatialGeometryModels.kt`
- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompiler.kt`
- Create test: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompilerTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 1.2**

Require one rectangle per occurrence, one padded union bound per region, one padded union envelope
per construct, exact membership, alignment facts, and no overloaded occurrence IDs.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Red fixtures must include members with different positions/sizes so `max(width,height)` cannot pass.
Target helper:

```kotlin
fun paddedUnion(rectangles: List<SpatialRect>, padding: Int): SpatialRect {
    require(rectangles.isNotEmpty())
    val left = rectangles.minOf(SpatialRect::x) - padding
    val top = rectangles.minOf(SpatialRect::y) - padding
    val right = rectangles.maxOf { it.x + it.width } + padding
    val bottom = rectangles.maxOf { it.y + it.height } + padding
    return SpatialRect(left, top, right - left, bottom - top)
}
```

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Focused test: `*SpatialGeometryCompilerTest*`; then compiler tests, repository `test`, audits, diff
check. Commit: `feat(spatial): derive region and construct geometry`.

## Task 3: Story 1.3 - Per-Sheet Grid References

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/1-3-derive-grid-reference-facts.md`
- Modify: `SpatialGeometryModels.kt`, `SpatialGeometryCompiler.kt`
- Create test: `SpatialGridReferenceCompilerTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 1.3**

Require owning-sheet grid, center mapping, occurrence and construct coverage, multi-sheet isolation,
and rejection of missing/invalid grids.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Target calculation:

```kotlin
fun cellFor(center: SpatialPoint, area: SpatialRect, rows: Int, columns: Int): String {
    require(rows in 1..26 && columns > 0)
    require(center.x in area.x until area.x + area.width)
    require(center.y in area.y until area.y + area.height)
    val column = ((center.x - area.x) * columns / area.width).coerceAtMost(columns - 1)
    val row = ((center.y - area.y) * rows / area.height).coerceAtMost(rows - 1)
    return "${('A'.code + row).toChar()}${column + 1}"
}
```

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Focused test: `*SpatialGridReferenceCompilerTest*`. Commit:
`feat(spatial): derive sheet-owned grid references`.

## Task 4: Story 2.1 - Exact Port Anchors And Route Endpoints

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/2-1-build-anchor-accurate-routes.md`
- Create: `SpatialRoutingModels.kt`, `SpatialAnchorCompiler.kt`
- Modify: `ProjectionElements.kt` only if structured endpoint trace is missing
- Create test: `SpatialAnchorCompilerTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 2.1**

Require every referenced port to resolve once, stable anchor distribution, complete port trace, and
route first/last equality without rounding.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Target contract:

```kotlin
data class SpatialAnchorPosition(
    val anchorId: String,
    val occurrenceId: String,
    val portId: String,
    val side: SpatialSide,
    val point: SpatialPoint,
    val sourceTrace: SpatialSourceTrace,
)
```

Red tests: missing port, duplicate port, shuffled port input, more than two ports on one side, and
endpoint equality.

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Focused test: `*SpatialAnchorCompilerTest*`. Commit:
`feat(spatial): derive stable port anchors`.

## Task 5: Story 2.2 - Obstacle-Aware Orthogonal Routes And Lanes

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/2-2-build-orthogonal-route-facts-and-lanes.md`
- Refactor: `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt`
- Rewrite: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- Rewrite tests: routing-model `AthenaRouteEngineTest.kt`, compiler `SpatialRouteCompilerTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 2.2**

Require nonzero orthogonal segments, obstacle avoidance, stable lanes, determinism, and no
professional optimization claim.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Extract/reuse the engine's domain-neutral geometry core. Electrical role and label facts must not
enter Spatial models. Red fixture places a third body directly between endpoints and asserts every
segment avoids its interior.

```kotlin
data class OrthogonalRouteRequest(
    val routeId: String,
    val source: SpatialAnchorPosition,
    val target: SpatialAnchorPosition,
)

data class OrthogonalRouteInput(
    val drawingArea: SpatialRect,
    val obstacles: List<SpatialOccurrenceGeometry>,
    val requests: List<OrthogonalRouteRequest>,
    val gridSize: Int,
)
```

- [ ] **Step 3: Verify sequentially**

Run focused routing-model tests, compiler route tests, both module test tasks, repository `test`,
audits, and diff check.

- [ ] **Step 4: BMad review, fix, done, commit**

Commit: `feat(spatial): route orthogonally around occurrence bodies`.

## Task 6: Story 2.3 - Complete Trace And Fail-Closed Routing

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/2-3-trace-routes-and-hold-the-no-optimization-boundary.md`
- Modify: `SpatialRoutingModels.kt`, `SpatialRouteCompiler.kt`
- Modify test: `SpatialRouteCompilerTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 2.3**

Require exact route/connection count, source/target occurrence-port-anchor trace, no nullable trace,
no first/last fallback, no `mapNotNull`, and actionable failure.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Red tests deliberately remove one endpoint anchor while leaving other anchors present. Expected
result is one diagnostic naming connection, missing endpoint, correction, and source; no partial
route list.

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Commit: `fix(spatial): fail closed on incomplete route trace`.

## Task 7: Story 3.1 - Typed Geometry Identity And Source Trace

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/3-1-publish-explicit-geometry-facts.md`
- Rewrite/split: all spatial model production files listed above
- Modify: `ProjectionSpatialCompiler.kt` and compiler mappings
- Rewrite test: `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 3.1**

Require typed facts for sheets, occurrences, regions, constructs, anchors, routes, lanes, grids,
and quality; every fact has stable identity, sheet ownership, and projection/geometry trace.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Remove raw `Map<String,String>` grid references and construct IDs stored in `occurrenceId`. Add
canonical ordering constructors or compiler normalization so input order cannot affect equality.

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Focused tests: spatial model tests plus `*Spatial*CompilerTest*`. Commit:
`refactor(spatial): publish typed sheet geometry facts`.

## Task 8: Story 3.2 - Complete Spatial Validation

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/3-2-enforce-geometry-validation.md`
- Create: `SpatialValidation.kt`
- Simplify: `SpatialDocument.kt`
- Create test: `SpatialValidationTest.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 3.2**

Create one task and red test group per invariant; never collapse lane/placement into lane identity.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Target diagnostic:

```kotlin
data class SpatialDiagnostic(
    val subject: String,
    val problem: String,
    val correction: String,
    val sourceTrace: SpatialSourceTrace,
)
```

Validator must return all deterministic issues, sorted by subject/problem, and block Presentation
when any exists.

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Commit: `feat(spatial): validate complete geometry before presentation`.

## Task 9: Story 4.1 - Truthful Geometry Quality Metrics

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/4-1-measure-quality-milestone-facts.md`
- Create: `SpatialQualityModels.kt`
- Rewrite: `SpatialQualityCompiler.kt`
- Rewrite test: `SpatialQualityCompilerTest.kt`
- Delete: `SpatialQualityMetricsReporter.kt`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 4.1**

Copy metric formulas from approved design. Remove label pressure/count from M41.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Use hand-computable fixtures for edge touch, positive overlap, a segment crossing a body without a
vertex inside, shared junction versus crossing, and non-orthogonal twist.

```kotlin
data class SpatialQualitySnapshot(
    val occurrenceOverlapCount: Int,
    val constructContainmentFailureCount: Int,
    val routeBodyIntersectionCount: Int,
    val routeCrossingCount: Int,
    val twistCount: Int,
    val usedLaneCount: Int,
    val peakLaneOccupancy: Int,
    val density: Double,
    val occupancy: Double,
)
```

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Commit: `fix(spatial): measure geometry quality from exact facts`.

## Task 10: Story 4.2 - Per-Sheet Density, Occupancy, And Baseline

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/4-2-measure-density-and-occupancy-against-the-baseline.md`
- Modify: `SpatialQualityCompiler.kt`, `SpatialQualityCompilerTest.kt`
- Create artifact: `_bmad-output/implementation-artifacts/m41/m41-spatial-quality-baseline.md`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 4.2**

Require union-area occupancy, per-sheet metrics, exact published M41 fixture values, and a true
M40/M41 comparison where comparable. No constant-only tests.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Red tests use overlapping rectangles to prove occupancy computes union area rather than sum and two
sheets to prove independent denominators.

- [ ] **Step 3: Generate baseline from compiler output and verify it in test**

Artifact must include fixture digest, sheet/drawing bounds, subject counts, metric definitions,
exact values, command, and timestamp. Tests parse structured compiler facts, not prose constants.

- [ ] **Step 4: Verify, BMad review, fix, done, commit**

Commit: `test(m41): publish truthful spatial quality baseline`.

## Task 11: Story 5.1 - Dedicated M41 Four-Reality Example

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/5-1-build-the-dedicated-m41-example.md`
- Modify only if needed: `examples/m41/rolling-shutter/**`
- Rewrite: `DedicatedM41ExampleTest.kt`
- Modify pipeline callers: `AthenaCompilerCompilationSupport.kt`
- Delete old bridge/transformation files after all callers migrate

- [ ] **Step 1: Invoke `bmad-create-story` for Story 5.1**

Require exact source-derived counts: 8 occurrences, 3 regions, 7 constructs, and route count equal
to compiled Projection connections. Require three distinct columns and zero blocking metrics.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Wire `ProjectionSpatialCompiler` into the real compiler pipeline. Remove old bridge and duplicate
transformation callers, update tests to current product path, and reject any M40 example reference.

- [ ] **Step 3: Verify, BMad review, fix, done, commit**

Run dedicated example, compiler module, repository `test`, source hygiene, encoding, diff check.
Commit: `feat(m41): compile dedicated spatial reality example`.

## Task 12: Story 5.2 - Product E2E And Fresh Screenshots

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/5-2-build-the-m41-e2e-evidence.md`
- Rewrite: `ide/theia-product/scripts/verify-athena-m41-product-proof.js`
- Rewrite: `ide/theia-frontend/scripts/athena-m41-product-proof-contract.test.mjs`
- Modify: active Theia projection/presentation payload tests as required
- Create fresh: `_bmad-output/implementation-artifacts/m41/screenshots/*.png`

- [ ] **Step 1: Invoke `bmad-create-story` for Story 5.2**

Require runtime proof assertions, expected geometry counts, exact route coverage, zero blocking
metrics, two-dimensional occupied ranges, canvas pixel distribution, desktop/narrow screenshots,
and coordinate preservation. Ban hardcoded renderer proof booleans.

- [ ] **Step 2: Invoke `bmad-dev-story`**

Verifier must fail on the old top-strip images. Target proof assertions include:

```javascript
requireValue(proof.spatial.occurrenceCount === 8, 'spatial', 'Expected 8 occurrences.', proof);
requireValue(proof.spatial.regionCount === 3, 'spatial', 'Expected 3 regions.', proof);
requireValue(proof.spatial.constructCount === 7, 'spatial', 'Expected 7 constructs.', proof);
requireValue(proof.spatial.routeCount === proof.projection.connectionCount, 'spatial', 'Route coverage mismatch.', proof);
requireValue(proof.spatial.occupiedWidthRatio >= 0.55, 'spatial', 'Drawing collapsed horizontally.', proof);
requireValue(proof.spatial.occupiedHeightRatio >= 0.45, 'spatial', 'Drawing collapsed vertically.', proof);
```

Pixel checks must sample the drawing area, exclude UI chrome, and prove non-background route/body
pixels across at least three horizontal and three vertical buckets.

- [ ] **Step 3: Rebuild and verify strictly sequentially**

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd ide workspace @engineeringood/athena-theia-frontend test
yarn --cwd ide build
yarn --cwd ide start:smoke:m41
```

- [ ] **Step 4: Inspect both screenshots and verify dimensions/pixels**

Use local image inspection for desktop and narrow screenshots. Reject overlap, clipping, top-strip
collapse, unreadable internal IDs, or blank route/body regions.

- [ ] **Step 5: BMad review, fix, done, commit**

Commit: `test(m41): prove spatial reality in product`.

## Task 13: Story 5.3 - Honest Closure

**Files:**
- BMad: `_bmad-output/implementation-artifacts/m41/5-3-close-m41-with-an-honest-retrospective.md`
- Create: `_bmad-output/implementation-artifacts/m41/m41-retrospective-2026-08-03.md`
- Modify through BMad: `sprint-status.yaml`

- [ ] **Step 1: Confirm Stories 1.1-5.2 and Epics 1-4 are `done`**

If any status differs, HALT. Do not create closure story content early.

- [ ] **Step 2: Invoke `bmad-create-story` for Story 5.3**

Require exact verification commands/results, baseline values, screenshot paths, remaining M42
label layout, M43 rendering/export, M44 readability optimization, M45 professional routing work,
and no parity claim.

- [ ] **Step 3: Invoke `bmad-dev-story`**

Create retrospective only from recorded evidence. No keyword-only test; validate referenced files,
statuses, metric values, and screenshot dimensions.

- [ ] **Step 4: Run final milestone verification sequentially**

```powershell
.\gradlew.bat --no-daemon --console=plain test
.\gradlew.bat --no-daemon --console=plain build
yarn --cwd ide workspace @engineeringood/athena-theia-frontend test
yarn --cwd ide build
yarn --cwd ide start:smoke:m41
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

- [ ] **Step 5: BMad review, fix, mark Story 5.3 and Epic 5 done**

Only then set the milestone retrospective status to complete.

- [ ] **Step 6: Commit closure**

```powershell
git commit -m "docs(m41): close recovered spatial reality milestone"
```

## Final Acceptance Checklist

- [ ] PRD rubric has no broken dimension and no critical/high unresolved finding.
- [ ] All 13 stories were created by `bmad-create-story` and developed by `bmad-dev-story`.
- [ ] Story files contain complete tasks, debug log, completion notes, file list, and change log.
- [ ] All stories and epics are `done`; retrospective alone is `complete`.
- [ ] Production `src/main` contains no proof/demo/sample/milestone types or stale alternatives.
- [ ] One route exists for every visible Projection connection; no fallback or silent drop exists.
- [ ] M41 fixture has 8 occurrences, 3 regions, 7 constructs, and three visible region columns.
- [ ] Blocking spatial metrics are zero and published per sheet.
- [ ] Desktop/narrow screenshots are fresh, readable, nonblank, and geometrically distributed.
- [ ] Sequential Gradle `test` and `build`, frontend tests, LSP install, product E2E, hygiene audit,
  encoding audit, and diff check all pass from fresh commands.
