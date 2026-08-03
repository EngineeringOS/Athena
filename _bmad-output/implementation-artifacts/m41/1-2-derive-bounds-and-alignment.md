---
baseline_commit: 8fd6e34cfa2e182f1f0ae2bbe755c1bf9d2e739c
---

# Story 1.2: Derive Bounds And Alignment

Status: done

## Story

As an engineer,
I want grouping geometry derived from actual Occurrence rectangles,
so that Regions and Constructs truthfully contain what they represent.

## Acceptance Criteria

1. Given a Region with placed member rectangles at different positions and sizes, geometry
   compilation publishes exactly one typed Region fact with stable Region identity, owning Sheet,
   exact Projection membership, and exact Source Trace. Its bound is the exact member-rectangle
   union expanded by 24 drawing units on every side, is positive, and is fully inside its owning
   Drawing Area. (FR-3.1, FR-3.5, FR-3.6, FR-9.1)
2. Given a Construct with offset members of different sizes, geometry compilation publishes exactly
   one typed Construct fact with owning Sheet, stable Construct identity, authored member order,
   Source Trace, and an envelope equal to the exact member-rectangle union expanded by 24 units on
   every side. Every member is fully contained; a maximum-member width/height shortcut cannot pass
   the behavioral test. (FR-3.2, FR-3.3)
3. Geometry compilation publishes one typed alignment fact for every nonempty effective Region and
   every authored Construct. Its typed source is exactly that Region or Construct; its identity is
   derived from owning Sheet plus typed source identity; Region members follow canonical placement
   order and Construct members preserve authored order. Every reference resolves to an existing
   same-Sheet Occurrence. Repeated compilation and permutation of unordered Projection collections
   produce equal canonical Region, Construct, and alignment facts. (FR-3.4, FR-9.1, NFR-1)
4. Empty membership, a duplicate member within one Region or Construct, an unknown member,
   ambiguous member identity, cross-Sheet membership, duplicate grouping identity, missing placed
   geometry, or a padded result outside the owning Drawing Area fails the geometry stage. All
   deterministic issues are sorted by subject then problem and include subject, problem,
   correction, and Source Trace; no partial Region, Construct, or alignment facts reach
   Presentation. Distinct Constructs may overlap in membership and remain valid. (FR-3.5, FR-3.6,
   FR-10.1, FR-10.2, FR-10.3, FR-10.4)

## Tasks / Subtasks

- [x] Task 1: Prove exact grouping geometry with RED tests (AC: 1, 2, 3)
  - [x] Add `SpatialGeometryCompilerTest.kt` with hand-computable, mixed-position,
        mixed-size Occurrence rectangles. Assert literal Region bound and Construct envelope values
        from the edge-coordinate union plus 24 units on every side.
  - [x] Make the Construct fixture reject the failed maximum-width/maximum-height pseudo-envelope;
        assert exact ordered membership, containment, owning Sheet, stable typed identity, and
        literal Source Trace Projection/source identities.
  - [x] Add repeated and unordered-input permutation coverage comparing complete typed results.
        Confirm the focused test fails before production implementation and record the RED output.
- [x] Task 2: Add typed grouping and alignment contracts (AC: 1, 2, 3)
  - [x] Extend `SpatialGeometryModels.kt` with strongly typed Region identity/geometry, Construct
        identity/geometry, and alignment identity/source facts. Every fact carries `sheetId` and
        `SpatialSourceTrace`; members use `SpatialOccurrenceId`, never labels or overloaded strings.
  - [x] Preserve Construct authored member order. Defensively copy public list inputs as Story 1.1
        does for placement reasons and Source Trace.
  - [x] Reuse `ProjectionConstructId` as Construct identity and add only the coordinate-free,
        resolvable source reference required by `SpatialSourceTrace`; update every constructor
        without adding a second identity, coordinates, or authoring syntax.
- [x] Task 3: Derive exact bounds, envelopes, and alignment facts (AC: 1, 2, 3)
  - [x] Add a cohesive `SpatialGeometryCompiler` stage that consumes `ProjectionDocument` plus
        Story 1.1 `SpatialOccurrenceGeometry`; it must never recompute placement or use legacy
        `SpatialBounds` width/height as grouping authority.
  - [x] Compute each padded union with edge coordinates using overflow-safe integer arithmetic:
        `x=minX-24`, `y=minY-24`, `right=maxRight+24`, `bottom=maxBottom+24`. Never clamp or shrink
        required padding to force a pass.
  - [x] Publish authored Regions in Sheet/authored order and Constructs in Sheet/authored order,
        with stable identity fallback only where order is otherwise undefined. Publish the
        compiler-owned Unassigned Region when Story 1.1 produced that placement group.
  - [x] Derive exactly one typed alignment per effective Region and authored Construct from the
        same membership constraints. Region alignment member order follows canonical placement;
        Construct alignment member order stays authored. Do not parse human reason strings, expose
        solver fields, or retain the blanket `row-0` alignment.
- [x] Task 4: Fail closed on membership and containment defects (AC: 1-4)
  - [x] Add separate RED/GREEN cases for empty Region, empty Construct, duplicate member within one
        Construct, unknown/ambiguous/cross-Sheet Construct member, duplicate Construct identity,
        missing member geometry, and out-of-area padded geometry. Reuse existing Region validation
        where it already proves the same invariant instead of duplicating it.
  - [x] Keep membership overlap across distinct Constructs valid. Earliest authored Construct may
        govern Story 1.1 placement order, but every authored Construct still receives its own exact
        geometry fact.
  - [x] Return all deterministic geometry diagnostics in canonical subject/problem order and empty
        fact lists on any issue. Add one multi-defect fixture proving complete issue aggregation,
        literal canonical order, and no partial facts. Add an active compilation-path test proving
        no partial grouping facts reach Presentation.
- [x] Task 5: Integrate and verify without duplicate geometry authority (AC: 1-4)
  - [x] Introduce `ProjectionSpatialCompiler` as the sole Projection-to-Spatial orchestrator and
        invoke geometry after placement. Migrate active callers and delete
        `ProjectionToSpatialTransformation` and `SpatialPlacementCompiler`; do not add a bridge,
        fallback, maximum-member compatibility path, or second orchestration entry.
  - [x] Migrate `SpatialDocument` away from legacy flat `placements`, `bounds`, and untyped
        `SpatialAlignment` authority when typed Occurrence/Region/Construct/alignment facts enter the
        active path. Adapt current Route/quality stage inputs without implementing later-story
        routing or metric behavior, and do not retain duplicate geometry models.
  - [x] Keep Story 1.1 placement facts byte-equivalent under its existing focused suite. Keep grids,
        Anchors, Routes, Lanes, metrics, Presentation styling, and rendering in their owning later
        stories/milestones.
  - [x] Run focused geometry tests, full compiler tests, Spatial model tests, repository tests,
        source-set hygiene audit, encoding audit, and `git diff --check` sequentially. Complete this
        story's Debug Log, Completion Notes, File List, and Change Log before review.

### Review Findings

- [x] [Review][Patch] Preserve structured Source Trace through active transformation failures
      [`ProjectionSpatialCompiler.kt`:56]
- [x] [Review][Patch] Validate compiler-owned Unassigned Region geometry before publication
      [`SpatialGroupingValidator.kt`:30]
- [x] [Review][Patch] Return overflow/out-of-area diagnostics instead of throwing
      [`SpatialGeometryCompiler.kt`:148]
- [x] [Review][Patch] Add successful multi-Sheet canonical permutation proof
      [`SpatialGeometryCompilerTest.kt`:117]
- [x] [Review][Patch] Assert exact correction and Source Trace for every multi-defect diagnostic
      [`SpatialGeometryCompilerTest.kt`:175]
- [x] [Review][Patch] Delete retired `SpatialQualityMetricsReporter` authority
      [`SpatialQualityMetricsReporter.kt`:11]
- [x] [Review][Patch] Reserve the compiler-owned Unassigned Region identity against authored use
      [`SpatialGroupingValidator.kt`:104]
- [x] [Review][Patch] Reject duplicate placed Occurrence geometry identities deterministically
      [`SpatialGroupingValidator.kt`:25]
- [x] [Review][Patch] Diagnose blank Region/Construct identity and Construct kind before fact
      construction [`SpatialGroupingValidator.kt`:104]
- [x] [Review][Patch] Prove exact correction and Source Trace for every remaining defect fixture
      [`SpatialGeometryCompilerTest.kt`:339]
- [x] [Review][Defer] Validate duplicate typed Region, Construct, and alignment identities in
      `SpatialReality` [Story 3.2] - deferred, pre-existing
- [x] [Review][Defer] Preserve Sheet boundaries through Presentation [Story 3.1] - deferred,
      pre-existing
- [x] [Review][Defer] Surface authored Spatial/Presentation failures in compilation diagnostics
      [Story 3.2] - deferred, pre-existing
- [x] [Review][Defer] Resolve repeated occurrence labels per Sheet when compiling connections
      [Story 3.1] - deferred, pre-existing
- [x] [Review][Defer] Preserve actual route port identity in Presentation endpoints [Story 2.1] -
      deferred, pre-existing
- [x] [Review][Defer] Reject missing or unresolved route endpoints without fallback/drop
      [Story 2.1] - deferred, pre-existing
- [x] [Review][Defer] Remove zero-length route segments [Story 2.1] - deferred, pre-existing
- [x] [Review][Defer] Validate route occurrence/anchor consistency [Story 3.2] - deferred,
      pre-existing
- [x] [Review][Defer] Correct route crossing and body-intersection measurements [Story 4.1] -
      deferred, pre-existing
- [x] [Review][Defer] Keep fixed Sheet composition inside Presentation canvas [Story 3.1] -
      deferred, pre-existing
- [x] [Review][Defer] Guard Presentation canvas extent overflow [Story 3.2] - deferred,
      pre-existing
- [x] [Review][Defer] Reject non-finite Spatial route points [Story 3.2] - deferred, pre-existing
- [x] [Review][Defer] Scope duplicate/nested Construct validation per Sheet [Story 3.2] - deferred,
      pre-existing
- [x] [Review][Defer] Prevent duplicate authored Presentation publication [Story 3.1] - deferred,
      pre-existing

## Dev Notes

### Architecture Guardrails

- Authority chain remains Athena source -> Engineering -> Projection -> Spatial -> Presentation ->
  Theia. Grouping geometry is Spatial-owned and compiler-derived.
- Apply AD-20, AD-21, AD-26, AD-27, AD-28, and AD-30. `SpatialGeometryCompiler` is a stage behind the
  sole `ProjectionSpatialCompiler`, not a second public orchestrator.
- Public geometry uses integer drawing units. Stable identities combine owning Sheet with the typed
  Projection Region/Construct identity. Construct IDs never occupy `SpatialOccurrenceId`.
- Every Region/Construct/alignment fact carries its owning Sheet and `SpatialSourceTrace`. Public
  collection inputs are immutable copies.
- Exact authored Region trace order is Sheet ID, Region ID, then member Projection IDs, with Region
  source geometry followed by member source geometry. Construct trace uses Sheet ID, Construct ID,
  then authored member Projection IDs, with Construct source geometry followed by member source
  geometry. Alignment trace equals its typed source grouping trace. Synthetic Unassigned Region
  trace uses Sheet source geometry followed by canonical member source geometry.
- No compatibility shim, dual grouping authority, raw map, solver/electrical field, milestone type,
  or proof/demo/sample class may enter `src/main`.

### Exact Geometry Policy

For member rectangles `r`:

```text
minX = min(r.x)
minY = min(r.y)
maxRight = max(r.x + r.width)
maxBottom = max(r.y + r.height)

paddedX = minX - 24
paddedY = minY - 24
paddedWidth = (maxRight - minX) + 48
paddedHeight = (maxBottom - minY) + 48
```

- Use `ProjectionSpatialLayout.GROUPING_PADDING`; do not copy a second literal policy constant.
- Perform intermediate edge math safely before constructing `SpatialRect`.
- Validate the full padded result against the owning Drawing Area. A clipped envelope is false.
- Region membership is exact. Construct membership is exact and authored-order preserving.
- An empty grouping is invalid. Distinct Constructs sharing members are valid.

### Current Code Intelligence

- `SpatialGeometryModels.kt` currently owns typed `SpatialRect`, `SpatialOccurrenceGeometry`,
  `SpatialSourceTrace`, and `SpatialDiagnostic`; add the closely related grouping facts there unless
  file size/responsibility requires a `SpatialGroupingModels.kt` split.
- `ProjectionSpatialLayout.place` is the Story 1.1 placement authority. Consume its occurrences
  unchanged. Do not recompute rectangles.
- `ProjectionPlacementPlanner` already supplies canonical Region groups and earliest-authored
  Construct ordering. Preserve that rule; overlapping Constructs are not a validation error.
- `ProjectionSpatialValidator` already handles duplicate/ambiguous/unknown/cross-Sheet Region
  membership, Region identities, ownership, and placement capacity. It is already over 300 lines;
  extract a cohesive grouping validator instead of growing a mixed-responsibility dump.
- `ProjectionSheetConstruct` has ID/kind/name/member names but no source geometry reference.
  `AuthoredProjectionViewCompiler` must populate one deterministic coordinate-free reference while
  preserving `ProjectionConstructId` as identity.
- `ProjectionToSpatialTransformation` emits one false `row-0` alignment and
  `SpatialPlacementCompiler` maps typed rectangles into legacy Double/string geometry. Replace both
  with `ProjectionSpatialCompiler`; delete them after caller migration.
- `SpatialDocument.kt` exposes legacy flat `placements`, `bounds`, and `SpatialAlignment`. Migrate
  that active geometry contract; never publish legacy and typed geometry as competing authorities.

### Previous Story Intelligence

- Story 1.1 is `done`; full repository `test`, compiler, layout-engine, spatial-model, hygiene,
  encoding, and diff checks passed.
- Story 1.1 established one shared internal `RuleBasedLayoutEngine`, typed integer rectangles,
  Sheet-qualified Occurrence IDs, immutable human-first reasons/traces, and canonical diagnostics.
- Review required exact literal rolling-shutter facts and permutations of every unordered
  collection. Repeat that evidence quality here; self-comparisons and minimum-only assertions do not
  count.
- `AuthoredProjectionSpatialBridge` was deleted. Do not recreate its maximum-member pseudo-envelope
  or any caller repair path.
- Story 1.3 owns grid semantics: vertical rows `A/B/C...`, horizontal columns `1/2/3...`, cell
  references such as `A1` and `B3`, row labels beyond `Z`, and grid dimension validation.

### Expected Files

- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompiler.kt`
- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- Create: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompilerTest.kt`
- Update or split by responsibility:
  `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt`
- Update: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- Update: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- Update or extract only as needed:
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt`
- Update: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- Update active Route/quality compilers and callers only enough to consume the new canonical
  Occurrence geometry contract; preserve later-story behavior.
- Delete after caller migration:
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt`,
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt`
- Preserve: `ProjectionSpatialLayout.kt`, `ProjectionPlacementPlanner.kt`, and Story 1.1 placement
  tests except for integration changes required by the new typed result.

### Testing Requirements

- Follow RED-GREEN-REFACTOR per task. Record the first failing assertion/compilation for each new
  behavior before production edits.
- Use literal expected rectangles and typed facts. Do not compare implementation output to itself.
- Include multi-Sheet ownership and permutation fixtures. Construct member overlap across different
  Constructs must remain green.
- Assert literal Source Trace identity lists, not merely nonempty trace. Verify every diagnostic has
  exact subject, problem, correction, and expected source identities.
- Run Gradle commands strictly sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "*SpatialGeometryCompilerTest*"
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### Scope Boundaries

- Story 1.3: Grid Reference facts and `A1`/`B3` semantics.
- Epic 2: port Anchors, Routes, and Lanes; Story 2.2 owns Lane facts.
- Story 3.1: complete typed per-Sheet `SpatialDocument` assembly.
- Story 3.2: complete document-wide validation; Story 1.2 still fails its own invalid inputs now.
- Epic 4: quality metrics and baseline.
- M42: labels, styling, visibility, terminal labels, grid chrome.
- M43: SVG/Theia/PDF/Canvas/Excel rendering and export surfaces.
- M44: readability optimization. M45: professional routing and multi-Sheet continuation.

### Git Intelligence

- Current baseline/HEAD: `8fd6e34cfa2e182f1f0ae2bbe755c1bf9d2e739c`.
- Recent M41 commits contain only recovery design/planning; implementation remains in a large dirty
  worktree. Many Story 1.1 files are untracked, so plain `git diff` cannot prove their contents.
- Read current disk state before every edit. Never revert or overwrite unrelated user changes.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md` Epic 1, Story 1.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md` Sections 5, 6 FR-3, 8 FR-10, 11-13]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md` Confirmed Decisions 3, 7-10]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md` AD-20, AD-21, AD-26-AD-30]
- [Source: `docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md` Spatial Contract, Placement Policy, Story Recovery]
- [Source: `_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md` Findings 7-10]
- [Source: `_bmad-output/implementation-artifacts/m41/1-1-derive-deterministic-explainable-placement.md`]
- [Source: `_bmad-output/implementation-artifacts/m41/deferred-work.md` Story 1.3 grid deferrals]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: focused geometry test compilation failed because `RealityTransformationDiagnostic` lacked
  structured `subject`, `problem`, `correction`, and `sourceTrace`; review test also exposed an
  invalid member-extension reference.
- GREEN: structured transformation diagnostics, Unassigned Region validation, overflow-safe padded
  union diagnostics, multi-Sheet permutation, and exact diagnostic contract tests all passed.
- REFACTOR: compiler-owned Unassigned groups validate exact `ProjectionNode` identities rather than
  re-resolving human labels, preserving distinct same-label Occurrences.
- Verification: focused `SpatialGeometryCompilerTest` (15 tests), full compiler suite, Spatial model
  suite, repository `test`, source-set hygiene audit, encoding audit, and `git diff --check` passed
  sequentially on 2026-08-03.
- Fresh adversarial review: Blind Hunter and Edge Case Hunter completed; the broad Acceptance Auditor
  timed out, then completed a focused retry. Four additional in-scope findings were resolved and
  verified; no Story 1.2 production defect remains open.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added typed Region, Construct, and alignment geometry derived from exact placed Occurrence
  rectangles with 24-unit padding, stable Sheet-qualified identities, immutable membership, and
  exact Source Trace.
- Replaced legacy Projection-to-Spatial orchestration and flat Spatial geometry authority with
  `ProjectionSpatialCompiler` and typed `SpatialDocument` facts; migrated active route, quality,
  Presentation, and test callers.
- Added canonical aggregation for grouping membership, containment, missing geometry, duplicate
  identity, cross-Sheet, multi-Sheet permutation, and overflow failures with no partial facts.
- Resolved all ten in-scope adversarial review findings; later routing, assembly, validation, and
  quality findings remain explicitly assigned in `deferred-work.md`.
- Reserved compiler-owned Unassigned identity, rejected duplicate placed geometry facts, diagnosed
  blank grouping definitions without exceptions, and proved every defect diagnostic's exact
  correction and Source Trace.

### File List

- `_bmad-output/implementation-artifacts/m41/1-2-derive-bounds-and-alignment.md`
- `_bmad-output/implementation-artifacts/m41/deferred-work.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionPlacementPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialGroupingValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM40ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41GeometryQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayoutTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGroupingModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialGeometryModelsTest.kt`
- Deleted: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt`
- Deleted: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt`
- Deleted: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityMetricsReporter.kt`

### Change Log

- 2026-08-03: Created through BMad create-story from milestone-local M41 artifacts.
- 2026-08-03: Implemented exact typed grouping geometry and alignment through the active Spatial
  pipeline; migrated callers and removed legacy geometry authority.
- 2026-08-03: Addressed adversarial code review findings - 6 in-scope items resolved; verification
  gates passed and story moved to review.
- 2026-08-03: Addressed fresh adversarial review findings - 4 in-scope items resolved, 1 document
  validation item deferred to Story 3.2, all verification gates passed, and story marked done.
