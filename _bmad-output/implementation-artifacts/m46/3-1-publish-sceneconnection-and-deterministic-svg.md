---
story: 3.1
epic: 3
title: Publish SceneConnection And Deterministic SVG
status: done
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 3.1: Publish SceneConnection And Deterministic SVG

Status: done

## Story

As an engineer,
I want canonical Presentation Reality to retain connection topology,
so that every renderer paints the same professional electrical meaning.

## Acceptance Criteria

1. Given accepted `ConnectionRoutePlan`, topology, and annotation facts, when Scene compiles, then one
   `SceneConnection` per route carries semantic identity, typed path segments, endpoint identities,
   Junction/Crossing/Interruption markers, explicit annotations, z-order, style id, and source trace.
2. Given `SceneConnection` input, when SVG renders, then routes, markers, bridges/gaps, and selected labels use
   thin professional proportions, exact topology, deterministic ordering, and screen/print-safe geometry.
3. Given equal accepted inputs, when Scene/SVG compilation repeats, then canonical scene equality and SVG bytes
   and digest are equal.
4. Given current production source, when replacement completes, then `SceneRoute` and legacy route schema fields
   are deleted; no deprecated aliases, fallback parser, or dual presentation path remains.
5. Given invalid spatial topology or annotation diagnostics, when Scene compiles, then no partial scene or SVG
   publishes and plain subject/problem/correction diagnostics survive.

## Tasks / Subtasks

- [x] Replace presentation route contract (AC: 1, 4)
  - [x] Add typed `SceneConnection`/marker/annotation contracts in `kernel/presentation-model`, preserving
    semantic identity, trace, styles, z-order, and canonical scene digest invariants.
  - [x] Remove active `SceneRoute` and legacy route fields from Kotlin contracts, generated TypeScript schema,
    tests, and adapters; no compatibility aliases or dual writes.
  - [x] Add contract tests for marker ownership, typed paths, annotation trace, deterministic canonical ordering,
    and renderer-neutral fields.
- [x] Compile Spatial topology to SceneConnection (AC: 1, 3, 5)
  - [x] Update `AthenaDiagramSceneCompiler` to consume `ConnectionRoutePlan`, `ConnectionRouteTopologyPlan`,
    and explicit `ConnectionAnnotation` facts only; renderer must not infer topology from points.
  - [x] Map stable route/net identity, endpoint anchors, typed segments, junctions, crossings/bridge owner,
    interruption pairs, annotations, style, z-order, and source trace without leaking AST/debug text.
  - [x] Keep all-or-nothing publication and deterministic canonicalization/schema version update exactly once.
- [x] Render deterministic professional SVG (AC: 2, 3, 5)
  - [x] Update `AthenaSvgRenderer` and tests to paint `SceneConnection` only, with constant screen/print-safe
    line/marker/label weights and deterministic element ordering.
  - [x] Ensure shared segments paint once, junctions remain tiny, crossings use explicit bridge/gap owner,
    interruptions use paired anchors, and labels paint only when selected.
  - [x] Prove repeated SVG byte/digest equality and invalid-publication fail-closed behavior.
  - [x] Run presentation, SVG, compiler, runtime, LSP tests sequentially; run full `test`, encoding, and
    source-set hygiene audits.

## Dev Notes

### Authority Chain

```text
Athena source meaning
  -> Connection IR
  -> ConnectionProjection
  -> ConnectionRoutePlan + ConnectionRouteTopologyPlan + ConnectionAnnotationPlan
  -> SceneConnection (this story)
  -> SVG/Konva adapters
```

`SceneConnection` is the only paint contract. It contains derived geometry/topology and trace, never source
grammar, package SVG, DOM/Konva state, viewport pixels, or mouse state. `SceneRoute` is retired, not aliased.

### Golden Visual Rules

- Match `AGENTS.md` Engineering Document Visual Golden Rule: one-pixel square frame, narrow flush rulers,
  blank white drawing area, thin orthogonal linework, tiny markers, compact regular labels, no debug flood.
- Route width, junction/crossing/interruption marker size, bridge size, and text stroke stay constant in
  screen/print space; invisible hit targets are not SVG paint.
- SVG package assets remain inert geometry resources; semantic ports and connection facts remain upstream truth.

### Existing Code To Replace

- `kernel/presentation-model/.../PresentationContracts.kt` currently owns `SceneRoute` and scene route lists.
- `kernel/compiler/.../AthenaDiagramSceneCompiler.kt` currently maps `SpatialSheet.routes` to `SceneRoute`.
- `kernel/svg-renderer/.../AthenaSvgRenderer.kt` currently paints `SceneRoute` point lists.
- `AthenaDiagramSceneContract.kt`, generated frontend contracts, and SVG tests enforce current schema and must
  be updated once, with no legacy fields left.

### Previous Story Intelligence

- Stories 2.1-2.3 established deterministic `ConnectionRoutePlan`, explicit topology facts, and explicit-only
  annotations. Preserve stable ids, source traces, fail-closed diagnostics, integer logical geometry, and
  default no-annotation output.
- Do not restore `SpatialRoute`, first-valid routing, renderer topology inference, or compatibility shims.

### Expected Files / Areas

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContract.kt`
- `kernel/presentation-model/src/test/...` scene contract tests
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/test/...` scene compilation tests
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/svg/AthenaSvgRenderer.kt`
- `kernel/svg-renderer/src/test/.../AthenaSvgRendererTest.kt`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`
- `ide/theia-frontend/src/browser/diagram/generated/types.ts` and generated adapters

### Verification

Run Gradle sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 3 / Story 3.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR-11, FR-12, NFR-1, NFR-3, NFR-5, NFR-6]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-9, AD-11, AD-12, AD-13, AD-18, AD-19]
- [Source: `_bmad-output/implementation-artifacts/m46/2-3-place-compact-connection-annotations-and-validate-quality.md`]
- [Source: `AGENTS.md`, Engineering Document Visual Golden Rule and E2E Proof Rule]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Replaced the scene schema in one break: `schemaVersion` 1 -> 2, `routes` -> `connections`; regenerated
  frontend contracts and removed every active `SceneRoute` reference from the publication path.
- LSP verification initially exposed three stale `route` style-operation fixtures; renamed the authoring role
  to `connection`, updated wire fixtures, and reran the module successfully.
- Preserved route-plan segment order during canonicalization; sorting segments by id would destroy path
  continuity for multi-digit segment indices.

### Completion Notes List

- Published typed `SceneConnection` facts with orthogonal/shared segments, endpoint anchors, explicit topology
  markers, selected annotations, style/z-order, and source traces.
- Compiler maps accepted Spatial topology and annotation facts only. One deterministic owner carries shared
  semantic decorations, preventing duplicate scene element identities.
- SVG paints shared segments once, tiny Junction/Interruption markers, explicit Crossing bridges, selected
  labels, and non-scaling one-pixel linework with deterministic bytes.
- Verified sequentially: presentation-model, svg-renderer, compiler, runtime, LSP, full Gradle `test`, frontend
  `yarn test` (52 tests), encoding audit, and source-set hygiene audit.

### File List

- contracts/presentation/v1/scene/rolling-shutter.json
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SetStyleOperationHandler.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/EditOperationWireMapperTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationPlannerTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngineTest.kt
- ide/theia-frontend/scripts/athena-diagram-contracts.test.mjs
- ide/theia-frontend/scripts/generate-diagram-contracts.mjs
- ide/theia-frontend/src/browser/athena-presentation-widget.tsx
- ide/theia-frontend/src/browser/diagram/generated/schema/athena-diagram-scene.schema.json
- ide/theia-frontend/src/browser/diagram/generated/schema-hash.ts
- ide/theia-frontend/src/browser/diagram/generated/types.ts
- ide/theia-frontend/src/browser/diagram/generated/validators.ts
- ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts
- ide/theia-frontend/src/browser/diagram/scale-benchmark.ts
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt
- kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/EditOperationContracts.kt
- kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/EditOperationContractTest.kt
- kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/OperationJournalContractTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContract.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt
- kernel/presentation-model/src/main/resources/schema/athena-diagram-scene.schema.json
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationAssetCompilerTest.kt
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationContractTest.kt
- kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/svg/AthenaSvgRenderer.kt
- kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/svg/AthenaSvgRendererTest.kt

### Change Log

- 2026-08-11: Created Story 3.1 context from active M46 PRD, architecture spine, epics, implementation plan,
  and completed Epic 2 story intelligence; status `ready-for-dev`.
- 2026-08-11: Replaced `SceneRoute` with canonical `SceneConnection`, published schema v2, deterministic SVG
  topology paint, generated frontend/LSP wire contracts, and complete regression evidence; status `review`.
