---
story: 2.2
epic: 2
title: Plan Junctions Crossings Shared Trunks And Interruptions
status: review
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 2.2: Plan Junctions Crossings Shared Trunks And Interruptions

Status: review

## Story

As an engineer,
I want route topology to display exact electrical meaning,
so that a reviewer never confuses crossing conductors with connected conductors.

## Acceptance Criteria

1. Given a joined Net branch and accepted Connection IR topology operators, when planning runs, then exactly
   one stable `ConnectionJunction` fact is emitted at each electrically joined topology point; shared trunk
   segments are canonicalized by stable identity and occur once in the route plan document.
2. Given two unrelated connection route segments intersect, when planning runs, then exactly one stable
   `ConnectionCrossing` fact is emitted without any Junction; deterministic bridge/gap ownership names one
   owning route plan explicitly and cannot be inferred or changed by paint order.
3. Given accepted `INTERRUPTION` topology, when planning runs, then one paired
   `ConnectionInterruptionAnchors` fact retains the semantic connection/net identity, ordered continuation
   identities, geometry, and source trace; renderer does not infer continuation from proximity.
4. Given equal Connection IR, projections, anchors, and route geometry, when planning repeats, then junction,
   crossing, shared-segment, interruption, and owner identities/ordering are byte/equality-identical.
5. Given contradictory topology, duplicate markers, unpaired interruption, junction/crossing collision, or a
   marker outside its route geometry, when validation runs, then no partial replacement publishes and exact
   subject/problem/correction diagnostics identify the invalid topology fact.

## Tasks / Subtasks

- [x] Add typed route-topology contracts (AC: 1-5)
  - [x] Add stable junction, crossing, bridge/gap ownership, shared-segment, and paired interruption facts to
    `kernel/spatial-model`, with immutable geometry and source trace only.
  - [x] Extend `ConnectionRoutePlan`/spatial document ownership without duplicating Connection/Net meaning.
  - [x] Add contract and validation tests for identity, pairing, route membership, and deterministic ordering.
- [x] Compile exact topology facts (AC: 1-4)
  - [x] Extend `ConnectionRoutePlanner` to consume accepted Connection IR topology operators alongside
    `ConnectionProjection`; never infer electrical joining from segment intersection.
  - [x] Canonicalize shared trunks once, emit Junction only for joined Net topology, emit Crossing only for
    unrelated route intersection, and select bridge/gap owner by stable route-plan identity.
  - [x] Emit paired interruption anchors only from explicit accepted interruption topology.
- [x] Fail closed and prove renderer independence (AC: 3-5)
  - [x] Add spatial validation for duplicates, marker/route mismatch, joined-vs-crossing contradiction,
    interruption pairing, and out-of-geometry facts.
  - [x] Add deterministic repeated-run, reversed-input, junction, crossing, trunk, and interruption tests.
  - [x] Run spatial-model, compiler, runtime, and LSP tests sequentially; run encoding and hygiene audits.

## Dev Notes

### Authority

```text
Connection IR topology operator
  -> ConnectionProjection selection
  -> ConnectionRoutePlan topology geometry
  -> SceneConnection markers
  -> renderer paint
```

Segment intersection never creates engineering connectivity. A Junction requires accepted BRANCH/MERGE/
PASS_THROUGH topology for the same Net. An unrelated geometric intersection creates a Crossing only. Shared
Net trunk identity canonicalizes coincident paint but cannot change Net membership. Interruption requires an
explicit accepted INTERRUPTION operator and paired stable anchors.

### Required Facts

- `ConnectionJunction`: stable id, net id, route-plan ids, point, trace.
- `ConnectionCrossing`: stable id, two route-plan ids, point, explicit bridge/gap owner, trace.
- `ConnectionSharedSegment`: stable id, net id, member route-plan ids, one canonical segment, trace.
- `ConnectionInterruptionAnchors`: stable id, semantic id, ordered continuation ids, paired points, trace.

No fact contains style, stroke, Konva, DOM, viewport, z-order, or mouse state. Later Scene/paint chooses visual
grammar from explicit topology facts only.

### Previous Story Intelligence

Story 2.1 established `ConnectionRoutePlan`, deterministic orthogonal planning, explicit keep-outs, complete
quality tuple, all-or-nothing diagnostics, and deleted active `SpatialRoute`/`SpatialRouteCompiler` names.
Preserve these contracts. Do not restore point-list aliases, first-valid routing, or renderer recomputation.

### Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Initial focused topology test exposed interruption routes being excluded from shared-segment canonicalization.
- Fixed planner classification: `BRANCH`/`MERGE` produce junctions and shared segments; `INTERRUPTION` produces
  shared segments and paired anchors without junctions; `PASS_THROUGH` does not create shared topology.
- Re-ran focused test, module tests, full Gradle `test`, encoding audit, and source-set hygiene audit successfully.

### Completion Notes List

- Added renderer-neutral topology contracts with stable IDs, immutable geometry, route membership, ordered
  interruption continuations, and source traces.
- Integrated accepted Connection IR topology operators into spatial compilation through
  `ConnectionRouteTopologyPlanner`; renderer remains topology-fact consumer only.
- Deterministic ordering and stable bridge ownership preserved across repeated planning and input order changes.
- Verification passed: focused topology test; `:kernel:spatial-model:test`; `:kernel:compiler:test`;
  `:kernel:runtime:test`; `:ide:lsp:test`; full `test`; encoding audit; source-set hygiene audit.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionRouteTopologyPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionRouteTopologyPlannerTest.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/ConnectionRouteTopologyModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingValidation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialTraceValidation.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/ConnectionRouteTopologyContractTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTestFixtures.kt`

### Change Log

- 2026-08-11: Created Story 2.2 from active M46 PRD, architecture, epics, Story 2.1 intelligence, and git context; status `ready-for-dev`.
- 2026-08-11: Implemented deterministic junction, crossing, shared-segment, and interruption topology planning;
  completed verification; status `review`.
