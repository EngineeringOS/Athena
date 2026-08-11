---
story: 1.4
epic: 1
title: Publish Connection Projections And Remove Generic Route Truth
status: review
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-10
---

# Story 1.4: Publish Connection Projections And Remove Generic Route Truth

Status: review

## Story

As an engineer,
I want each document view to explicitly project accepted Connection IR,
so that one connection or net identity remains stable across views and generic relationships cannot become route truth.

## Acceptance Criteria

1. Given accepted Connection IR and one schematic view, when projection compilation runs, then every
   `ConnectionProjection` references exactly one Connection IR connection/net identity, selected projected
   occurrence Ports, an explicit projection role, optional authored logical route constraints, and source trace;
   no projection field contains physical coordinates, renderer state, viewport state, or paint data.
2. Given two projections of one accepted connection/net, when both compile, then they resolve to the same
   Engineering identity and Connection IR fact; differing selected occurrences or logical constraints cannot
   mutate semantic meaning.
3. Given current production source, when this story completes, then `ProjectionConnection` and direct generic
   `EngineeringRelationship` route lowering are deleted; no adapter, deprecated alias, fallback parser,
   compatibility shim, or dual projection path remains. Retired tests are deleted or rewritten to current
   ConnectionProjection authority.
4. Given an unresolved Connection IR identity, occurrence Port, or authored logical constraint, when projection
   compilation runs, then it fails closed with an exact human-readable diagnostic and publishes no partial
   projection.

## Tasks / Subtasks

- [x] Replace projection connection contracts (AC: 1-3)
  - [x] Add `ConnectionProjectionModels.kt` in `kernel/projection-model` with stable projection id,
    connection/net identity, selected occurrence Port references, role, logical route constraints, and trace.
  - [x] Remove `ProjectionConnection`, endpoint/participant route contracts, and their obsolete identifiers.
  - [x] Keep projection-model dependent on canonical `kernel:connection-model`; do not add spatial/renderer
    dependencies or coordinates.
- [x] Compile accepted Connection IR into explicit projections (AC: 1-4)
  - [x] Replace generic relationship lowering in `EngineeringToProjectionTransformation` and
    `AuthoredProjectionViewCompiler` with Connection IR identity lookup and fail-closed diagnostics.
  - [x] Preserve multiple view projections of one identity and selected occurrence Port trace.
  - [x] Carry only authored logical route constraints; derive physical geometry later in spatial planning.
  - [x] Update spatial anchor/coverage inputs to consume ConnectionProjection without owning semantics.
- [x] Delete generic route truth and stale paths (AC: 3)
  - [x] Remove direct `EngineeringRelationship` route lowering, fallback parser paths, aliases, and dual writes.
  - [x] Remove/rename route payload fields that expose `relationshipId` where they represent connection identity;
    leave later Scene/route replacement for its owning stories but do not preserve a new generic route path.
  - [x] Delete or rewrite tests that assert `ProjectionConnection` or relationship-to-route behavior.
- [x] Prove projection authority and hygiene (AC: 1-4)
  - [x] Add model tests for identity stability, role/endpoint invariants, logical-only constraints, and absence
    of physical/render fields.
  - [x] Add compiler tests for two-view identity preservation, unresolved identity/occurrence fail-closed
    diagnostics, and no generic relationship route output.
  - [x] Run projection-model and compiler tests sequentially, then encoding and source-set hygiene audits.

## Dev Notes

### Authority Chain

```text
Athena source Engineering Connection/Net
  -> canonical ConnectionDocument (Story 1.3)
  -> ConnectionProjection (this story)
  -> ConnectionRoutePlan (Story 2.x)
  -> SceneConnection (Story 3.1)
  -> Theia/SVG paint
```

`ConnectionProjection` is compiler-owned derived projection truth. It selects which occurrence Ports and
logical Sheet constraints a view presents, but cannot create or modify Engineering Connection/Net meaning.
Physical x/y geometry, routes, bends, crossings, junctions, styles, DOM, Konva, viewport, and mouse state
remain later-stage contracts.

### Required Contract Shape

Use stable source-derived ids and explicit trace. A projection must identify exactly one canonical Connection
IR fact (`connectionId` or `netId`, never both), selected occurrence Port identities, a closed projection role,
optional logical route constraints, and source trace. Reject blank roles, duplicate selected ports, mixed
connection/net identity, unresolved facts, and geometry-bearing constraints.

### Replacement Rules

- `ProjectionConnection` is retired, not aliased.
- Generic `EngineeringRelationship` remains valid for non-connectivity meaning but is never lowered as a route
  or conductor.
- No `.elmt`, HTML, XML, package SVG, renderer node, or external layout engine becomes runtime authority.
- No compatibility/fallback/dual projection path. Delete stale production code and tests immediately.
- Diagnostics name exact connection/net/Port, problem, and correction in plain engineering language.

### Existing Code To Inspect/Change

- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElements.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionIdentifiers.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageInventory.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteTraceSupport.kt`
- `kernel/compiler/src/test/.../ProjectionModelContractTest.kt`
- `kernel/compiler/src/test/.../ProjectionSpatialLayoutTest.kt`
- `kernel/compiler/src/test/.../SpatialAnchorCompilerTest.kt`

### Verification

Run Gradle strictly sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- RED: projection-model test failed on retired `ProjectionConnection` symbols after contract replacement.
- RED: compiler tests exposed stale M45/generic route fixtures; deleted obsolete scene/export/operation tests per
  pre-1.0 replacement rule.
- GREEN: projection-model, compiler, runtime, and LSP tests passed sequentially; encoding and source-set audits passed.

### Completion Notes List

- Added renderer-neutral `ConnectionProjection` with canonical identity kind, projection role, selected occurrence
  Ports, logical route constraints, and source trace.
- Authored view compilation now consumes accepted Connection IR Connections/Nets; generic `EngineeringRelationship`
  is never lowered into projection route truth.
- Added fail-closed `ConnectionProjectionValidator` for identity and endpoint admission.
- Deleted retired `ProjectionConnection` model and stale M45/generic route tests; migrated spatial consumers to the
  new contract without preserving aliases or fallback paths.
- Sequential verification passed: `:kernel:projection-model:test`, `:kernel:compiler:test`, `:kernel:runtime:test`,
  `:ide:lsp:test`; encoding and source-set hygiene audits passed.

### File List

- `kernel/projection-model/build.gradle.kts`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ConnectionProjectionModels.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElements.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionIdentifiers.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ConnectionProjectionContractTest.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionProjectionValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionPlacementPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialAuthorityValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageInventory.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionProjectionCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayoutTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompilerTest.kt`
- Deleted stale M45/generic route tests: `AthenaDiagramSceneCompilerTest.kt`, `M45RollingShutterPageTest.kt`,
  `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`,
  `M45ExportStabilityTest.kt`, `PlacementOperationHandlerTest.kt`, `RepresentationAndEngineeringOperationHandlerTest.kt`.
- Updated projection-model README files.

### Change Log

- 2026-08-10: Created Story 1.4 context; status `ready-for-dev`.
- 2026-08-10: Replaced generic ProjectionConnection with ConnectionProjection and completed sequential verification; status `review`.
