---
story: 2.1
epic: 2
title: Plan Deterministic Orthogonal Connection Routes
status: review
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 2.1: Plan Deterministic Orthogonal Connection Routes

Status: review

## Story

As an engineer,
I want clean deterministic orthogonal routes around real symbols,
so that generated documents are readable without manual repair.

## Acceptance Criteria

1. Given a `ConnectionProjection`, admitted Port anchors, drawing bounds, frame/rulers, occurrence bounds,
   and hard keep-outs, when `ConnectionRoutePlanner` runs, then it returns a `ConnectionRoutePlan` with the
   source/target anchor identities, typed orthogonal segments, quality metrics, and source trace; every segment
   stays in the drawing area and no segment enters a hard obstacle interior.
2. Given multiple valid plans, when the planner chooses one, then candidates are ranked in this fixed
   lexicographic order: semantic/topological validity, hard-obstacle violations, ambiguous overlap count,
   crossing count, bend count, Manhattan length, and stable encoded geometry identity. Equal inputs produce
   byte/equality-identical plans across repeated runs and independent planner instances.
3. Given missing anchors, invalid bounds, impossible keep-outs, diagonal candidates, zero-length segments, or
   unresolved projection identity, when planning runs, then no partial route plan is published and a plain,
   actionable diagnostic names the exact connection and correction.
4. Given current production source, when this story completes, then `SpatialRoute` and
   `SpatialRouteCompiler` are absent from active production paths. No first-valid selection, giant-detour
   fallback, deprecated alias, compatibility adapter, or renderer-owned route computation remains.
5. Given equivalent admitted anchors and obstacles with different viewport zoom or pixel dimensions, when
   planning runs, then the persisted plan is unchanged; viewport state is not an input to route identity or
   geometry.

## Tasks / Subtasks

- [x] Define the replacement spatial contract (AC: 1-5)
  - [x] Add `ConnectionRoutePlanModels.kt` in `kernel/spatial-model` with stable plan identity, endpoint
    anchor references, typed orthogonal segments, quality metrics, source trace, and immutable hard-constraint
    inputs/results. Keep the model renderer-neutral and integer/logical-coordinate based.
  - [x] Add model invariants for non-blank identities, distinct endpoints, in-bounds geometry, orthogonality,
    positive segments, and stable equality/canonical encoding.
  - [x] Add focused contract tests for valid plans and every rejected invariant; ensure no Konva, DOM, viewport,
    paint, or raw mouse field exists.

- [x] Implement deterministic route planning (AC: 1-3, 5)
  - [x] Add `ConnectionRoutePlanner` in `kernel/compiler` consuming only `ConnectionProjection`, admitted
    spatial anchors, drawing/frame/ruler bounds, occurrence bounds, and explicit keep-outs.
  - [x] Generate a finite deterministic orthogonal candidate set from endpoint lanes and legal detours; reject
    candidates intersecting hard obstacle interiors or leaving the drawing area.
  - [x] Rank candidates using the exact fixed lexicographic cost tuple and stable encoded geometry identity;
    never choose first-valid and never emit giant-detour fallback geometry.
  - [x] Return fail-closed diagnostics for unresolved identity/anchor and impossible geometry. Preserve prior
    accepted publication through existing compiler transaction rules instead of publishing partial plans.

- [x] Replace legacy route authority (AC: 4)
  - [x] Migrate spatial/compiler consumers from `SpatialRoute`/`SpatialRouteCompiler` to
    `ConnectionRoutePlan`/`ConnectionRoutePlanner` without aliases or dual writes.
  - [x] Delete retired route contracts and tests that assert old point-list or first-valid behavior. Keep
    connection meaning in `ConnectionProjection`; planner owns geometry only.
  - [x] Confirm source-set hygiene has no milestone/demo/proof route helpers in `src/main`.

- [x] Prove deterministic planning (AC: 1-5)
  - [x] Add unit and compiler tests covering obstacle avoidance, frame/ruler bounds, repeated-run equality,
    cost-tuple tie breaking, viewport invariance, invalid input fail-closed behavior, and all endpoint traces.
  - [x] Run affected Gradle tests strictly sequentially, then encoding and source-set hygiene audits.

## Dev Notes

### Authority Chain

```text
Athena source Engineering Connection/Net
  -> canonical ConnectionDocument
  -> ConnectionProjection
  -> ConnectionRoutePlan (this story)
  -> SceneConnection (later story)
  -> Theia/SVG paint
```

`ConnectionRoutePlan` is derived spatial reality. It may contain logical/physical drawing geometry and
quality evidence, but it cannot change Engineering Connection/Net identity, endpoint roles, Port direction,
kind, specifications, topology membership, or semantic source. The renderer consumes the plan and never
recomputes routes.

### Required Contract Shape

Use stable source-derived plan ids, projection identity, selected anchor ids, `OrthogonalSegment` values,
immutable `RouteQualityMetrics`, and `SpatialSourceTrace`. Persist integer logical geometry only. Frame and
ruler cells are hard drawing bounds, not paint content. Keep-outs are explicit spatial constraints; package
assets supply Port anchor geometry but never connection meaning.

### Determinism Rules

Candidate ordering and cost comparison must be total and platform-independent. Compare exactly:

```text
(semantic/topological validity,
 hard-obstacle violations,
 ambiguous overlap count,
 crossing count,
 bend count,
 Manhattan length,
 stable encoded geometry identity)
```

Sort all input collections by stable identity before candidate generation. Equal source, package, Sheet, and
compiler profile must produce equal plan objects and canonical bytes regardless of viewport zoom or canvas size.

### Fail-Closed Rules

No plan publication for unresolved projection identity, missing/ambiguous anchors, invalid drawing bounds,
out-of-frame segments, obstacle-interior intersections, diagonal/zero-length segments, or no legal candidate.
Diagnostics must state the exact connection/net or endpoint subject, the problem, and the correction in plain
engineering language. Existing accepted publication remains under STALE/UNAVAILABLE transaction semantics.

### Legacy Replacement Rules

`SpatialRoute`, `SpatialRouteCompiler`, generic route ids, aliases, fallback parsers, first-valid routing,
giant-detour compatibility logic, and renderer route calculation are deleted. Do not preserve old M45 tests or
examples. No `.elmt`, HTML, XML, external layout engine, or package SVG becomes runtime authority. ELK is
deferred; this planner is the active deterministic implementation.

### Expected Files / Areas

- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/ConnectionRoutePlanModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/ConnectionRoutePlanContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionRoutePlanner.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionRoutePlannerTest.kt`
- existing spatial/compiler consumers and build metadata that still reference `SpatialRoute`.

### Verification

Run Gradle strictly sequentially on Windows; never overlap Gradle invocations:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- RED: `ConnectionRoutePlanContractTest` failed to compile because the replacement model did not exist.
- GREEN: focused spatial-model and planner tests passed after adding typed plans and deterministic planning.
- REFACTOR: migrated active consumers, runtime/LSP payloads, validators, and tests; deleted all active
  `SpatialRoute`/`SpatialRouteCompiler` names and reran full affected suites.

### Completion Notes List

- Added immutable `ConnectionRoutePlan`, typed `ConnectionRouteSegment`, stable plan identity, complete
  lexicographic quality tuple, source trace, drawing-bound admission, and deterministic geometry identity.
- Added `ConnectionRoutePlanner` with sorted finite candidate generation, occurrence/keep-out avoidance,
  fixed cost ordering, stable tie-breaking, and all-or-nothing diagnostics.
- Replaced active `SpatialRoute`/`SpatialRouteCompiler` production and test usage without aliases or dual paths.
- Sequential verification passed: `:kernel:spatial-model:test`, `:kernel:compiler:test`,
  `:kernel:runtime:test`, `:ide:lsp:test`, encoding audit, and source-set hygiene audit.

### File List

- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/ConnectionRoutePlanModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingValidation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialTraceValidation.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/ConnectionRoutePlanContractTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTestFixtures.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionRoutePlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionRouteTraceSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialAuthorityValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageInventory.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityGeometry.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionRoutePlannerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayoutTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeSpatialFacts.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeSpatialFactsMapper.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSpatialFactsPayloads.kt`
- Deleted: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- Deleted: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteTraceSupport.kt`

### Change Log

- 2026-08-11: Created Story 2.1 context from active M46 PRD, architecture spine, epics, and implementation plan; status `ready-for-dev`.
- 2026-08-11: Replaced legacy spatial routes with deterministic typed connection route plans; status `review`.
