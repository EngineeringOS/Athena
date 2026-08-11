---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 2.1: Compile Canonical AthenaDiagramScene

Status: done

## Story

As a runtime,
I want one compiler-owned `AthenaDiagramScene` derived from validated Spatial Reality,
so that every renderer and IDE surface consumes the same logical geometry, identity, ordering, and source trace.

## Acceptance Criteria

1. A valid `CompilerCompilationSuccess` with one Spatial document lowers to one validated, canonical `AthenaDiagramScene` with page, snap grid, styles, occurrences, Ports, labels, routes, decorations, traces, stable IDs, z-order, input revision, and digest.
2. The scene compiler copies and validates Spatial facts only; it does not reroute, move, infer engineering meaning, or derive geometry from raw Projection/frontend data.
3. Scene identity, element identity, occurrence/subject/Port/relationship identity, canonical ordering, and digest are stable across source declaration reorder and repeated process execution.
4. Clean page decorations include only the page background, outer frame, numeric top coordinates, and alphabetic left coordinates by default; no interior grid, bottom table, banner, or unexplained layout whitespace is emitted.
5. Every published scene reference points to an admitted style, asset, or trace; invalid/empty Spatial input fails closed with no scene publication and human-first diagnostics.
6. Shared contract vectors and Kotlin presentation-model validation pass; focused compiler tests, full Gradle, frontend tests/build, encoding audit, and source-set hygiene audit pass sequentially.

## Tasks / Subtasks

- [x] Add compiler-owned Spatial-to-scene lowering (AC: 1, 2, 5)
  - [x] Add the minimal `kernel:compiler` dependency on `kernel:presentation-model` and implement one cohesive scene compiler/orchestrator.
  - [x] Map `SpatialSheet` page extent, drawing area, grid, occurrence geometry, anchors, routes, labels/ports, and source traces into contract types without renderer types.
  - [x] Produce deterministic stable IDs from canonical semantic/spatial identities; do not use list position, object identity, absolute paths, or process time.
  - [x] Emit source origins with correct trace roles and exactly one primary origin per trace.
  - [x] Return no scene when Spatial is empty or diagnostics exist; preserve upstream diagnostics for publication callers.
- [x] Implement canonical page decorations and scene contract completion (AC: 1, 4, 5)
  - [x] Add clean page background/frame and top numeric/left alphabetic coordinate decorations from the compiled page/grid.
  - [x] Keep interior macro/micro grid lines, title tables, feature banners, and instructional text out of the default scene.
  - [x] Select deterministic style tokens and z-index bands for decorations, routes, occurrences, labels, and Ports.
  - [x] Run `AthenaDiagramSceneContract.canonicalize` and `validate`; never hand-roll a second digest or JSON shape.
- [x] Add contract and product regression proof (AC: 1, 3, 5, 6)
  - [x] Compile the M43 rolling-shutter Spatial result and assert scene topology, clean decorations, stable IDs, traces, and non-zero digest.
  - [x] Compile reordered source and compile twice in one process; assert canonical scene equality and digest equality.
  - [x] Assert invalid/missing Spatial input produces no scene and no partial scene collections.
  - [x] Validate against `contracts/presentation/v1/scene/rolling-shutter.json`, digest vectors, and trace role vectors where applicable.
- [x] Verify and record (AC: 6)
  - [x] Run focused compiler/presentation tests, full Gradle, frontend build/tests, encoding audit, and source-set hygiene audit sequentially.
  - [x] Complete story records, file list, change log, and set status `review` only after all evidence passes.

## Dev Notes

### Authority and Boundaries

- `SpatialDocument` is the only geometry authority. Presentation lowers already-validated facts; it must not call a renderer, inspect Theia, reconstruct raw Projection, or invent routes/placements.
- Public scene is a contract, not a new Reality or database. Use `kernel/presentation-model` types and `AthenaDiagramSceneContract` for validation, canonical ordering, and digest.
- Keep source meaning in `.athena` and authored placement/lock in colocated `.sheet.athena`; scene facts are derived and immutable.
- No compatibility reader, raw `athena/projectionSession`, split SVG/Canvas payload, legacy fallback, or milestone-named production class may be added.

### Required Contract Shape

- `schemaVersion = 1`; logical integer `SceneUnit` coordinates only.
- `ScenePage.pageBounds` and `drawingBounds` come from Spatial sheet extent/drawing area.
- `SceneSnapGrid` uses `rows`, `columns`, `subdivisions = cell`, `drawingOrigin`, and `formulaVersion = athena-grid-1`.
- `SceneOccurrence` carries stable occurrence/subject IDs, bounds, placement anchor, style, z-index, trace, Ports, and labels.
- `SceneRoute` carries relationship/flow identity, exact source/target Port IDs, orthogonal Spatial points, style, z-index, and relationship trace.
- `SceneDecoration` is used only for clean page/background/frame/coordinate labels in this story.
- Scene/element/style/asset/trace IDs must satisfy the existing SHA-256 value-class contracts.
- Canonical ordering and digest must be delegated to `AthenaDiagramSceneContract.canonicalize`; do not compare or serialize unsorted collections.

### Existing Implementation to Reuse

- `ProjectionSpatialCompiler` remains the Projection-to-Spatial orchestrator.
- `CompilerCompilationSuccess.spatialDocuments` is already fail-closed by `AthenaCompilerCompilationSupport`.
- `SpatialSourceTrace`, `SpatialOccurrenceGeometry`, `SpatialAnchorPosition`, `SpatialRoute`, and Spatial grid facts contain the source and geometry evidence needed for lowering.
- `contracts/presentation/v1` is the only cross-language fixture authority. Do not create local duplicate vectors.

### Testing Requirements

- Tests belong in `kernel/compiler/src/test` and/or `kernel/presentation-model/src/test`; no proof helper enters `src/main` with `Demo`, `Sample`, `Proof`, or milestone names.
- Cover valid rolling shutter, source reorder, repeated compilation, missing/empty Spatial, invalid Spatial, ID uniqueness, trace primary roles, decoration cleanliness, contract validation, and digest determinism.
- Frontend is a consumer of generated contracts only; no frontend geometry test may become scene authority.
- Gradle verification commands are strictly sequential on Windows.

### Project Structure Notes

- Prefer one cohesive compiler class plus small strongly-related support models; split only when responsibilities become distinct or files exceed the repository Kotlin organization heuristic.
- If a new compiler module dependency is required, update only the owning module build file and record it in the story; do not add a renderer dependency to kernel.

### References

- [Source: `_bmad-output/planning-artifacts/m43/epics.md`#Epic-2---Canonical-Scene-Compilation-And-Assets]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-5---Presentation-Reality-Is-One-Canonical-Scene]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-8---Scene-Transport-Is-Closed-Logical-And-Renderer-Neutral]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-11---Printable-Canvas-Is-Clean-By-Default]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md`]
- [Source: `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`]
- [Source: `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContract.kt`]
- [Source: `contracts/presentation/v1/scene/rolling-shutter.json`]
- [Source: `contracts/presentation/v1/scene/digest-vectors.json`]
- [Source: `contracts/presentation/v1/trace/expected-origins.json`]
- [Previous story: `_bmad-output/implementation-artifacts/m43/1-3-compile-sheet-intent-into-spatial-reality.md`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `2026-08-06`: Passed `:kernel:presentation-model:test`, `:kernel:compiler:test`, `:ide:lsp:test`, and root `test` sequentially.
- `2026-08-06`: Passed `yarn workspace @engineeringood/athena-theia-frontend test` and root `yarn build` from `ide`.
- `2026-08-06`: Passed `tools/encoding-audit.ps1` and `tools/source-set-hygiene-audit.ps1`.

### Completion Notes List

- Added compiler-owned lowering from validated Projection and Spatial facts to one canonical `AthenaDiagramScene`; invalid upstream, cardinality, and Spatial facts fail closed.
- Published runtime-backed `athena/diagramScene` output with source-plus-companion input revision and traceable `READY` or `UNAVAILABLE` state.
- Kept default scene clean: page, outer frame, numeric top coordinates, alphabetic left coordinates, and no interior grid or title table.
- Proved deterministic rolling-shutter topology, identity, digest, trace roles, source reorder stability, and invalid-input refusal.

### File List

- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageSupportTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`

### Change Log

- 2026-08-06: Created through the M43 BMad story flow from the active Epic 2 contract and architecture.
- 2026-08-06: Compiled and verified canonical Spatial-to-scene publication; final regression gates
  passed; status set to `done`.
