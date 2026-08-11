---
story: 2.3
epic: 2
title: Place Compact Connection Annotations And Validate Quality
status: review
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 2.3: Place Compact Connection Annotations And Validate Quality

Status: review

## Story

As an engineer,
I want selected connection annotations placed compactly and quality-checked,
so that published sheets remain readable, professional, and free of debug-text noise.

## Acceptance Criteria

1. Given explicit annotation selections and accepted route/topology facts, when annotation planning runs,
   then each selected annotation has a stable identity, resolved engineering value, display role, source trace,
   and collision-free logical anchor inside the Sheet Drawing Area.
2. Given symbol bounds, route segments, topology markers, existing annotations, and Sheet bounds, when
   placement has multiple legal anchors, then candidates are ranked deterministically and repeated runs and
   reversed input order produce equal annotation plans.
3. Given no annotation selection, when Spatial/Scene compilation runs, then no source path, AST link, internal
   id, anchor id, debug label, or automatic Port-name flood is emitted; selection does not turn hidden metadata
   into canvas text.
4. Given route quality violations or impossible annotation placement, when spatial validation runs, then no
   partial Spatial publication occurs and diagnostics name exact subject, problem, and correction in plain
   engineering language.
5. Given crossing, bend, overlap, detour, label collision, and deterministic tie-boundary inputs, when tests
   run, then quality metrics and annotation placement remain stable and prove professional-document constraints.

## Tasks / Subtasks

- [x] Define renderer-neutral annotation contracts (AC: 1-4)
  - [x] Add `ConnectionAnnotation`, stable annotation identity, display role/value, logical anchor, bounds, and
    `SpatialSourceTrace` under `kernel/spatial-model`; keep style/rendering out of the contract.
  - [x] Add immutable annotation collection ownership to `SpatialSheet`/route topology without duplicating
    Connection IR meaning or exposing AST/debug fields.
  - [x] Add contract validation for identity, Sheet ownership, bounds, trace, duplicate annotations, and
    explicit-selection admission.
- [x] Plan compact annotations deterministically (AC: 1-3, 5)
  - [x] Create `kernel/compiler/.../ConnectionAnnotationPlanner.kt` consuming only accepted Connection IR,
    projections, route plans/topology, symbol/occurrence bounds, and explicit annotation selections.
  - [x] Resolve engineering values through existing canonical Connection/Net specifications; never infer
    labels from geometry or paint order.
  - [x] Generate finite in-bounds candidate anchors, reject symbol/route/marker/label collisions, and rank by
    stable collision cost then encoded geometry/identity. No viewport pixels persist.
- [x] Validate quality and fail closed (AC: 4-5)
  - [x] Extend spatial quality/authority validators to include route crossing, bend, overlap, detour,
    annotation collision, out-of-bounds, duplicate, and topology-marker checks.
  - [x] Preserve all-or-nothing publication and existing STALE/UNAVAILABLE behavior on invalid quality or
    impossible label placement.
  - [x] Add deterministic repeated-run and reversed-input tests plus crossing, bend, overlap, detour, and
    impossible-placement diagnostics tests.
  - [x] Run spatial-model, compiler, runtime, and LSP tests sequentially; run full `test`, encoding audit,
    and source-set hygiene audit.

## Dev Notes

### Authority Chain

```text
Athena source annotation selection + Connection/Net meaning
  -> canonical Connection IR values
  -> ConnectionProjection
  -> ConnectionRoutePlan + explicit topology
  -> ConnectionAnnotationPlan
  -> SceneConnection/Scene annotation (later story)
  -> SVG/Konva paint
```

Annotation planning is derived Spatial Reality. It may choose logical placement and collision evidence, but
cannot mutate Engineering Connection/Net identity, endpoint roles, Port direction, kind, specifications,
topology membership, or package geometry. Renderer never computes or exposes annotations itself.

### Required Rules

- FR-12 is explicit-only: default sheet stays clean white. Never emit AST links, source paths, internal ids,
  anchor ids, debug labels, or all Port names by default.
- `ConnectionAnnotation` carries one declared display role and one resolved engineering value. Internal ids
  remain in trace/interaction metadata, not visible text.
- Candidate placement uses integer logical Sheet coordinates and Drawing Area bounds. Viewport zoom, DOM,
  Konva, SVG, mouse state, and pixel dimensions are forbidden inputs or persisted fields.
- Collision tests cover occurrence rectangles, route body segments, junction/crossing/interruption markers,
  and already accepted annotation bounds. Tiny professional marker geometry remains renderer-owned.
- Stable tie-breaking sorts all source facts and candidates by identity before scoring. No first-valid choice,
  random order, or geometry-derived semantic identity.
- Quality checks fail closed. Preserve previous accepted publication under existing STALE/UNAVAILABLE rules;
  do not publish partial annotations or metrics.

### Existing Contracts To Extend

- `ConnectionDocument`, `ConnectionFact`, `NetFact`, and resolved specification precedence remain semantic
  authority (`project < potential-or-signal < Net < Connection`).
- `ConnectionRoutePlan` and `ConnectionRouteTopologyPlan` from Stories 2.1/2.2 remain renderer-neutral
  geometry/topology authority. Do not reintroduce `SpatialRoute` or renderer inference.
- `SpatialQualityCompiler`, `SpatialValidation`, `SpatialTraceValidation`, and
  `ProjectionSpatialCompiler` currently recompute/validate Sheet quality; extend these paths rather than
  adding a second quality system.
- `canonicalSpatialSheet` is the canonical ordering boundary. Add annotation ordering there.

### Expected Files / Areas

- Create: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/ConnectionAnnotationModels.kt`
- Create: `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/ConnectionAnnotationContractTest.kt`
- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionAnnotationPlanner.kt`
- Create/modify: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionAnnotationPlannerTest.kt`
- Modify: `SpatialDocument.kt`, `SpatialValidation.kt`, `SpatialTraceValidation.kt`,
  `SpatialQualityModels.kt`, `SpatialQualityCompiler.kt`, `ProjectionSpatialCompiler.kt`, and focused tests.

### Verification

Run Gradle strictly sequentially on Windows; never overlap Gradle invocations:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 2 / Story 2.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR-12 and NFR-1/NFR-2/NFR-3]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-9, AD-11, AD-13, AD-16, AD-17, AD-18]
- [Source: `_bmad-output/implementation-artifacts/m46/2-1-plan-deterministic-orthogonal-connection-routes.md`]
- [Source: `_bmad-output/implementation-artifacts/m46/2-2-plan-junctions-crossings-shared-trunks-and-interruptions.md`]
- [Source: `AGENTS.md`, Engineering Document Visual Golden Rule and E2E Proof Rule]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- RED: annotation contract and planner tests failed before new contracts/planner existed.
- GREEN: added typed annotation contracts and deterministic planner; focused tests passed after correcting
  fail-closed diagnostic assertion.
- REFACTOR: wired explicit annotation selections into `ProjectionSpatialCompiler`; default selection remains
  empty; extended Spatial ownership, trace, bounds, duplicate, route-collision validation.

### Completion Notes List

- Added explicit-only `ConnectionAnnotationSelection`, stable role/value identity, logical bounds/anchor, and
  immutable `ConnectionAnnotationPlan`.
- Added `ConnectionAnnotationPlanner` with canonical Connection/Net value resolution, finite offset candidates,
  route/occurrence/topology collision rejection, deterministic ordering, and plain diagnostics.
- Spatial Sheet now owns annotations; validators reject foreign/duplicate/out-of-bounds/route-colliding facts
  and trace drift. Compiler publishes no annotations unless explicit selections are supplied.
- Verification passed sequentially: `:kernel:spatial-model:test`, `:kernel:compiler:test`,
  `:kernel:runtime:test`, `:ide:lsp:test`, full `test`, encoding audit, source-set hygiene audit.

### File List

- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/ConnectionAnnotationModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialTraceValidation.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/ConnectionAnnotationContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionAnnotationPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionAnnotationPlannerTest.kt`

### Change Log

- 2026-08-11: Created Story 2.3 context from active M46 PRD, architecture spine, epics, implementation plan,
  Stories 2.1/2.2 intelligence, and current repository rules; status `ready-for-dev`.
- 2026-08-11: Implemented explicit compact annotation planning and fail-closed spatial quality validation;
  completed regression evidence; status `review`.
