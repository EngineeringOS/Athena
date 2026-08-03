---
baseline_commit: 8fd6e34cfa2e182f1f0ae2bbe755c1bf9d2e739c
---

# Story 1.1: Derive Deterministic Explainable Placement

Status: done

## Story

As an engineer,
I want every projected Occurrence placed deterministically in a meaningful two-dimensional Sheet,
so that Region grouping is visible and repeatable without authored coordinates.

## Acceptance Criteria

1. Given a canonical M41 Projection Snapshot with three Regions, compiling twice and with unordered
   inputs permuted produces equal canonical Occurrence placement facts. Each fact has one owning
   Sheet, a positive in-area rectangle, stable identity, and a human-first placement reason that
   names the constraints used. (FR-1.1, FR-1.3, FR-1.4, FR-1.5)
2. Golden Fixture placement uses Sheet extent `1200 x 800`, Drawing Area `(40,60,1120,640)`, and
   title-block boundary `y=740`; no Occurrence enters the title-block band.
3. The three authored Regions occupy ordered, distinct X ranges with a 32-unit gutter. Occurrences
   maintain at least 48 units of vertical separation. Golden Fixture occupied Occurrence range is
   at least 55% of Drawing Area width and 45% of Drawing Area height. (FR-2.1, FR-2.3, FR-2.4)
4. An unassigned Occurrence enters one explicit final Region in stable order; no Occurrence is
   discarded. (FR-2.2, FR-2.5)
5. Projection and Athena source remain coordinate-free. The compiler does not expose solver fields,
   authored coordinates, or a renderer repair path. (FR-1.5, NFR-3, NFR-4)

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 2, 3)
  - [x] Add a failing compiler test for three distinct Region X ranges, nonzero Y range, in-area
        rectangles, exact sheet policy, and minimum spacing. Run it and record the red failure.
  - [x] Add a failing permutation test that compares complete canonical placement facts.
- [x] Task 2 (AC: 1, 2, 3, 4)
  - [x] Add `SpatialGeometryModels.kt` types for integer points/rectangles, Occurrence geometry,
        placement reason, and Source Trace without adding milestone-named production types.
  - [x] Add `ProjectionSpatialLayout.place(ProjectionDocument)` and normalized input ordering.
  - [x] Replace row-only `SpatialPlacementCompiler` behavior with Region-column placement and
        explicit unassigned Region handling.
  - [x] Keep placement deterministic for repeated and unordered-input compilation.
- [x] Task 3 (AC: 1, 2, 3, 5)
  - [x] Validate positive size, Drawing Area containment, title-block exclusion, Region gutter,
        Occurrence separation, and occupied-range thresholds in tests.
  - [x] Add plain diagnostic subject/problem/correction/source for invalid projection inputs.
- [x] Task 4 (AC: 1-5)
  - [x] Run focused placement tests, compiler tests, repository test, source-set hygiene audit,
        encoding audit, and `git diff --check` sequentially.
  - [x] Complete Debug Log, Completion Notes, File List, and Change Log in this story before review.

### Review Findings

- [x] [Review][Patch] Use Construct membership, Connection topology, authored/reading order, then
      stable identity for canonical placement order. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:168]
- [x] [Review][Patch] Remove implicit single-Sheet ownership and fail every zero-owner Occurrence.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:42]
- [x] [Review][Patch] Derive typed Spatial Occurrence identity from owning Sheet plus Projection
      identity. [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt:58]
- [x] [Review][Patch] Reject ambiguous duplicate labels before Region membership resolution.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:167]
- [x] [Review][Patch] Add canonical eight-Occurrence rolling-shutter placement acceptance coverage;
      final example filesystem creation remains Story 5.1. [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompilerTest.kt:24]
- [x] [Review][Patch] Remove stale bridge and forbidden label-metric assertions from changed M41
      milestone coverage. [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialMilestoneTest.kt:166]
- [x] [Review][Patch] Handle Sheets with zero placement groups without division by zero.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:112]
- [x] [Review][Patch] Reject Region counts that cannot preserve node width and 32-unit gutters inside
      Drawing Area. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:112]
- [x] [Review][Patch] Reject unknown and cross-Sheet Region membership instead of silently dropping
      authored facts. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:169]
- [x] [Review][Patch] Diagnose duplicate membership within one Region separately from membership in
      multiple Regions. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:66]
- [x] [Review][Patch] Preserve owning Sheet and Region identities in every placement Source Trace.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:148]
- [x] [Review][Patch] Expose geometry-model dependency required by public Spatial Source Trace types.
      [kernel/spatial-model/build.gradle.kts:7]
- [x] [Review][Patch] Reject integer rectangle extent overflow before containment checks.
      [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt:25]
- [x] [Review][Patch] Derive vertical-capacity diagnostic text from active separation policy.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:95]
- [x] [Review][Patch] Derive placement-reason policy text from active geometry constants.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:131]
- [x] [Review][Patch] Defensively copy public placement-reason and Source Trace lists.
      [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt:35]
- [x] [Review][Patch] Match semantic ownership only when a Sheet subject has no Projection node IDs.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:193]
- [x] [Review][Patch] Reject duplicate Projection node IDs before ownership and placement.
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:39]
- [x] [Review][Patch] Reject blank geometry element IDs in Source Trace.
      [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt:54]
- [x] [Review][Patch] Reuse and refactor the existing rule-based layout engine behind a
      domain-neutral adapter instead of adding a second placement engine. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:17]
- [x] [Review][Patch] Reject blank, duplicate, and reserved effective Region identities with
      structured diagnostics before shared-engine placement. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt:14]
- [x] [Review][Patch] Reject duplicate Sheet identities before publishing conflated coordinate
      spaces. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt:14]
- [x] [Review][Patch] Resolve overlapping Construct membership by earliest authored Construct
      instead of silently choosing the last Construct order. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionPlacementPlanner.kt:74]
- [x] [Review][Patch] Reject ambiguous Connection endpoint aliases instead of attaching topology
      order to an arbitrary Occurrence. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionPlacementPlanner.kt:83]
- [x] [Review][Patch] Give an unowned Region member a complete actionable diagnostic instead of an
      empty owning-Sheet phrase. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt:161]
- [x] [Review][Patch] Remove the active Projection repair bridge and migrate callers to canonical
      Projection placement inputs. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialBridge.kt:20]
- [x] [Review][Patch] Hide the concrete rule-based solver from the public Projection layout
      constructor. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:22]
- [x] [Review][Patch] Make placement reasons truthful for the synthetic Unassigned Region and name
      the active vertical-separation constraint. [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt:98]
- [x] [Review][Patch] Assert complete literal eight-Occurrence placement facts and permute every
      unordered Projection collection. [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompilerTest.kt:64]
- [x] [Review][Defer] Correct grid axis generation so vertical rows are `A/B/C` and horizontal
      columns are `1/2/3`. [kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt:195] - deferred to Story 1.3
- [x] [Review][Defer] Define grid behavior beyond 26 lettered rows without emitting punctuation.
      [kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt:197] - deferred to Story 1.3
- [x] [Review][Defer] Reject blank grid identity and non-positive row/column dimensions.
      [kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt:186] - deferred to Story 1.3

## Dev Notes

### Architecture Guardrails

- Authority chain is Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
- Use `ProjectionSpatialCompiler` as the eventual sole orchestrator. This story may introduce the
  layout component behind that boundary but must not add another bridge or compatibility shim.
- Apply AD-20, AD-21, AD-27, AD-28, and AD-30 from the M41 architecture spine.
- Public Spatial geometry uses integer drawing units. Stable identity comes from Projection identity
  and owning Sheet, not list index or delimiter-concatenated display labels.
- Current layout/routing solvers are internal adapters. Do not expose solver or electrical policy
  fields in Spatial models or Athena syntax.
- M41 does not implement labels, rendering/export, readability optimization, or professional routing.

### Current Defect To Replace

CodeGraph and delivery audit show `SpatialPlacementCompiler.compile` currently maps
`projection.nodes.mapIndexed` to `x = index * 140.0`, `y = 0.0`, with fixed 80x40 bounds and a
reason string that only changes Region text. This is the failed behavior and must be rejected by
the first red test. `AuthoredProjectionSpatialBridge` is stale and must not become a new caller.

### Target Placement Policy

1. Canonicalize Sheets by authored order then stable Sheet identity.
2. Canonicalize Regions by authored Region order.
3. Canonicalize Occurrences within a Region by Construct membership, Connection topology,
   authored/reading order, then stable identity.
4. Allocate Region columns left-to-right inside Drawing Area with 32-unit gutter.
5. Allocate Occurrences vertically with at least 48-unit separation and enough edge clearance for
   24-unit future Region/Construct padding.
6. Put unassigned Occurrences in an explicit final Region.
7. Validate every rectangle inside `(40,60,1120,640)` and outside title-block band `y >= 740`.

### Target API Shape

```kotlin
class ProjectionSpatialLayout {
    fun place(projection: ProjectionDocument): SpatialLayoutResult
}

data class SpatialLayoutResult(
    val occurrences: List<SpatialOccurrenceGeometry>,
    val diagnostics: List<SpatialDiagnostic>,
)
```

Adapt names to existing model conventions, but preserve typed facts and failure semantics. Do not
add a second renderer/layout engine.

### Expected Files

- Create/modify: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt`
- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt`
- Rewrite only as caller migration requires:
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt`
- Rewrite test:
  `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompilerTest.kt`
- Do not modify M42-M45 code, Theia rendering, or unrelated milestone artifacts.

### Testing Requirements

- First test run must be red against the current one-row implementation, then green after the
  minimal implementation. Do not check a red task if it passed first run.
- Assert exact policy and relationships, not minimum-only counts or self-comparisons.
- Shuffle unordered input collections and compare complete typed results.
- Run Gradle commands strictly sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "*SpatialPlacementCompilerTest*"
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md` Sections 4, 5, 6, 10, 11]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md` AD-20, AD-21, AD-27, AD-28, AD-30]
- [Source: `_bmad-output/implementation-artifacts/m41/epics.md` Epic 1, Story 1.1]
- [Source: `docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md` Placement Policy]
- [Source: `_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md` Critical Finding 1]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt` current row behavior]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: focused placement run failed 2/6 tests against row-only `(index * 140, 0)` behavior.
- RED: typed geometry, explicit unassigned Region, duplicate membership, vertical capacity, and
  cross-Sheet ownership tests each failed before their production change.
- RED: each code-review regression test failed for its targeted missing behavior before patching.
- RED: shared-engine delegation failed at `SpatialPlacementCompilerTest.kt:650`; shared engine test
  then failed compilation because `RuleBasedLayoutEngine` and its domain-neutral contracts did not exist.
- GREEN: shared engine, Spatial model, focused placement, full compiler, and repository test tasks passed.
- GREEN: source-set hygiene, encoding audit, and `git diff --check` passed after final refactor.
- REVIEW RED: eight focused placement cases failed before structured identity, ownership, reason,
  trace, permutation, and exact rolling-shutter fact patches were applied.
- REVIEW RED: authored Projection node publication and earliest-Construct governance tests failed
  before caller migration and overlap-policy correction.
- REVIEW GREEN: `:kernel:compiler:test` passed 408 tests; `:kernel:spatial-model:test`,
  `:kernel:layout-engine:test`, and full repository `test` all passed sequentially.
- REVIEW GREEN: source-set hygiene audit, encoding audit, and `git diff --check` passed after review
  record completion; line-ending notices from unrelated dirty files were warnings only.

### Completion Notes List

- BMad context engine analysis completed; story recreated after invalidating failed M41 story.
- Replaced row placement with deterministic Sheet/Region composition over exact M41 drawing policy.
- Added typed integer Occurrence rectangles, placement reasons, Source Trace, and actionable
  fail-closed diagnostics for duplicate membership, capacity, and Sheet ownership.
- Unassigned Occurrences enter one stable final Region; unordered node input produces equal facts.
- Removed stale closed-milestone proof requiring a sheetless compatibility path.
- Closed all 20 review findings with regression coverage, including identity, ownership, membership,
  capacity, overflow, immutable trace, and actionable diagnostic boundaries.
- Closed all 9 second-pass patch findings with focused RED/GREEN proof; deferred 3 grid-contract
  findings to Story 1.3, which owns `A1`, `B3` row/column reference semantics.
- Reused one domain-neutral `RuleBasedLayoutEngine` from both schematic and Projection adapters;
  split Projection placement, planning, and validation responsibilities into cohesive files.
- Removed `AuthoredProjectionSpatialBridge`; authored view compilation now publishes real Projection
  nodes and explicit Sheet ownership for direct `ProjectionToSpatialTransformation` use.
- All Story 1.1 acceptance tests and repository validation gates pass; BMad review outcome is done.

### File List

- `_bmad-output/implementation-artifacts/m41/1-1-derive-deterministic-explainable-placement.md`
- `_bmad-output/implementation-artifacts/m41/deferred-work.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/RuleBasedLayoutEngine.kt`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/RuleBasedLayoutEngineTest.kt`
- `kernel/spatial-model/build.gradle.kts`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModelsTest.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionPlacementPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialBridge.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialMilestoneTest.kt` (deleted)

### Change Log

- 2026-08-03: Recreated from replacement M41 PRD/architecture through BMad.
- 2026-08-03: Implemented deterministic typed Region-column placement through BMad dev-story.
- 2026-08-03: Applied all BMad code-review patches and reused shared rule-based layout engine.
- 2026-08-03: Closed 9 second-pass review patches, deferred 3 Story 1.3 grid findings, and marked done.
