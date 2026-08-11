---
story: 3.3
epic: 3
title: Navigate Placed And Unplaced Connectivity
status: ready-for-dev
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 3.3: Navigate Placed And Unplaced Connectivity

Status: review

## Story

As an engineer,
I want a Connection Navigator independent from the drawing,
so that I can inspect and locate all connectivity, including unplaced facts.

## Acceptance Criteria

1. Given accepted Connection IR, when LSP publishes `ConnectionReadModel`, then it lists every Engineering
   Connection and Engineering Net in deterministic order with stable identity, placed/unplaced state,
   endpoints, endpoint roles, kind, potential/signal, resolved specifications, validation, and source trace.
2. Given one semantic item with zero, one, or many Scene projections, when the engineer selects it in the
   Navigator, then source selection resolves through the existing semantic selection service and every matching
   placed projection can be located/selected by stable semantic identity; an unplaced item remains inspectable
   without creating placeholder graphics.
3. Given source change, failed compilation, repository change, or out-of-order response, when the read model
   refreshes, then LSP reports attempted and accepted Input Revision explicitly, frontend retains only a `READY`
   model keyed by accepted Input Revision, discards stale cache, and never fabricates connection state.
4. Given normal engineering use, when Connection Navigator is opened in the Theia left area, then Connections
   and Nets are scannable semantic groups, compact facts remain readable, and selection does not change canvas
   style target, annotation visibility, source authority, or engineering state.

## Tasks / Subtasks

- [x] Publish a revision-bound Connection read model through LSP (AC: 1, 3)
  - [x] Add one cohesive `ConnectionReadModelService` and payload contracts mapping accepted
    `CompilerCompilationSuccess.connectionIr`; do not parse source, traverse graphics, or duplicate Connection IR.
  - [x] Publish deterministic Connection/Net items with endpoint roles, kind, potential/signal, canonical resolved
    specifications, accepted validation facts, placed state from route identities in the same accepted compiler
    result, and source traces suitable for semantic selection.
  - [x] Model `READY`, `STALE`, and `UNAVAILABLE` explicitly with attempted/accepted Input Revision and diagnostics;
    cache accepted domain results only by accepted Input Revision.
  - [x] Register `athena/connectionReadModel` on `AthenaLanguageServer` and test ready, stale/unavailable, ordering,
    placed/unplaced, and revision behavior through the LSP boundary.
- [x] Add Connection Navigator to Theia (AC: 1-4)
  - [x] Add typed frontend payload contracts and `requestConnectionReadModel()` to the existing LSP bridge.
  - [x] Add one Connection Navigator widget with compact Connections/Nets groups and item detail for endpoints,
    roles, kind, potential/signal, specifications, validation, and placed/unplaced state.
  - [x] Register widget factory, command, Athena View menu entry, and left-area workbench extension using existing
    Athena module/contribution patterns; do not replace or mutate filesystem authority in this story.
  - [x] Refresh on repository/editor changes, reject out-of-order responses, clear non-`READY` and repository-old
    cache, and expose no placeholder graphics for unplaced items.
- [x] Unify Navigator, source, and canvas selection (AC: 2, 4)
  - [x] Route Navigator item selection through `AthenaSemanticSelectionService` using published source trace.
  - [x] Extend Presentation selection synchronization so one semantic connection/net identity highlights all
    matching `SceneConnection` projections through paint-only interaction overlays.
  - [x] Preserve style target, annotation visibility, source, Scene, and Engineering Reality during navigation.
- [x] Add regression and hygiene proof (AC: 1-4)
  - [x] Add LSP tests for complete item facts, deterministic order, unplaced visibility, source trace, and
    revision-keyed stale/unavailable behavior.
  - [x] Add frontend source/contract tests for widget registration, `READY`-only caching, semantic trace selection,
    all-projection canvas selection, and absence of placeholder/source mutation paths.
  - [x] Run sequential `:ide:lsp:test`, frontend `yarn test`, full Gradle `test`, contract check, encoding audit,
    and source-set hygiene audit.

## Dev Notes

### Authority Chain

```text
Athena source -> accepted Connection IR -> revision-bound ConnectionReadModel -> Theia Navigator
                                                       |
                                                       +-> stable source/Scene trace selection
```

`ConnectionReadModel` is disposable read state. It never becomes Engineering Reality, Connection IR, source,
or Scene authority. Placed state is a join between accepted Connection IR identity and
`SpatialSheet.routes.connectionId` from the same compiler result; geometry cannot create connectivity.

### Read Model Contract

- One item kind discriminator: `CONNECTION` or `NET`.
- Required facts: semantic id, display name, Connection Kind, endpoints with stable Port id/authored path/role,
  potential or signal where present, canonical resolved specification properties, validation status/diagnostics,
  placed boolean, matching projection trace ids, and one source trace.
- Publication states are closed: `READY`, `STALE`, `UNAVAILABLE`.
- Every response carries attempted Input Revision. `READY` carries the same accepted Input Revision used by its
  items. `STALE` may describe the prior accepted revision but frontend must discard it rather than present old
  connectivity as current. `UNAVAILABLE` carries no items.
- Equal accepted inputs produce equal item order and payload facts.

### Current Code And Required Change

- `CompilerCompilationSuccess.connectionIr` already owns accepted renderer-neutral connectivity. Consume it; do
  not add another parser, relationship traversal, or graphic inference path.
- `AthenaConnectionPublicationService` already models accepted/stale/unavailable Connection IR but not Input
  Revision. Compose or deepen it in the LSP service; do not add a compatibility publication path.
- `CompilerCompilationSuccess.spatialDocuments` already carries route identities from the same accepted compile.
  Use those identities to classify placement. Do not require package assets or a Presentation Scene to inspect
  unplaced Connection Reality.
- `AthenaLanguageServer` exposes custom read-only requests with `@JsonRequest`; follow the existing
  `athena/repositoryGraphSession` and `athena/diagramScene` boundary.
- `AthenaLspEditorBridgeService.sendLanguageRequest` is the one frontend transport. Add one typed method only.
- Existing Athena widgets are `ReactWidget`s registered in `athena-frontend-module.ts` and exposed by
  `AthenaProductContribution`/`ATHENA_WORKBENCH_EXTENSIONS`; use that established product pattern.
- `AthenaSemanticSelectionService.selectSceneTrace` already resolves source trace and opens the source editor.
  Extend selection consumers; do not create a second selection service.
- `KonvaDiagramAdapter` now owns semantic connection hit/overlay paint. Add an external semantic selection method
  that selects every matching connection id; no source, IR, style, or label mutation.

### Architecture Guardrails

- Governed by M46 AD-5 through AD-8, AD-13, AD-15, AD-17, and AD-18.
- No generic relationship-to-route fallback, `SceneRoute`, compatibility alias, placeholder occurrence, or
  milestone-named production class.
- Do not expose topology operators, route segments, source paths, AST links, trace ids, or internal Port ids as
  canvas text. Navigator may inspect engineering facts in its own semantic surface.
- No custom filesystem writes, source assembly, renderer topology inference, or edit operations in Story 3.3.
- Keep Kotlin files cohesive by role. Prefer one models/protocol file plus one service rather than one DTO per file
  or a mixed 400-line dump.

### Technical Stack

- Kotlin 2.3.0, Java 25, Gradle 9.2.1, LSP4J 1.0.0.
- TypeScript 5.7.3, Theia 1.73.1, React 18.3.1, Konva 10.3.0.
- No new dependency is required. No web research changes a pinned local stack contract.

### Expected Files / Areas

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/ConnectionReadModelModels.kt` (new)
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/ConnectionReadModelService.kt` (new)
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/ConnectionReadModelServiceTest.kt` (new)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaConnectionReadModelRequestTest.kt` (new if
  service and LSP boundary proofs remain clearer when separate)
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-connection-navigator-widget.tsx` (new)
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- focused frontend script tests and `src/browser/style/index.css`

### Previous Story Intelligence

- Story 3.2 replaced flattened route paint with exact segment paint, shared identity deduplication, published
  marker/annotation paint, transparent 12px connection hit targets, and semantic `connectionId`/`traceId`
  selection.
- Connection selection already flows through `AthenaSemanticSelectionService` without changing style target or
  annotation visibility. Story 3.3 must reuse that path in the opposite direction.
- `SceneConnection.connectionId` is semantic identity while one identity may have multiple Scene projections.
  Frontend external selection matches all current Scene projections, but Scene never determines whether a
  Connection/Net exists in the read model.
- Frontend contract generation, all 56 frontend tests, full Gradle tests, encoding audit, and source-set hygiene
  passed at Story 3.2 closure.

### Git Intelligence

- Baseline remains `9ca7c80239d38d9e2a4880886e71e27e2923c41b`; current milestone work is intentionally uncommitted and
  coexists with a large user-owned refactor. Never revert unrelated files.
- Recent repository commits predate M46; current on-disk M46 story artifacts and source are authoritative.

### Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
Set-Location ide/theia-frontend
yarn contracts:check
yarn test
Set-Location ../..
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 3 / Story 3.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR-13]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-15]
- [Source: `_bmad-output/planning-artifacts/m46/implementation-readiness-report.md`, FR-13 coverage]
- [Source: `_bmad-output/implementation-artifacts/m46/3-2-paint-and-select-professional-connections-in-konva.md`]
- [Source: `AGENTS.md`, Human-First Language and Engineering Document Visual Golden Rules]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Corrected Story fixture to include required `WIRE.crossSection`; compiler rejection was valid.
- Preserved strict edit CAS revision while adding read-only attempted revision hashing for stale lock/source states.
- Fixed live-editor refresh to consume LSP-owned tracked primary-source compilation instead of rereading accepted disk text.
- Added same-compilation route identity proof for placed state and explicit first-failure `UNAVAILABLE` proof.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Published deterministic revision-bound Connection/Net read models with complete facts, placement, projection traces,
  validation, provenance, and closed READY/STALE/UNAVAILABLE states.
- Added compact Theia Connection Navigator with READY-only revision cache, repository/sequence stale-response rejection,
  live editor refresh, unplaced inspection, and no placeholder graphics.
- Unified Navigator/source/canvas selection through `AthenaSemanticSelectionService`; Konva highlights every matching
  connection projection through paint-only overlays without changing style, annotations, source, Scene, or engineering state.
- Verification passed: `:ide:lsp:test`, frontend 63/63, `contracts:check`, full Gradle `test`, encoding audit, and
  source-set hygiene audit.

### File List

- `_bmad-output/implementation-artifacts/m46/3-3-navigate-placed-and-unplaced-connectivity.md`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/ConnectionReadModelModels.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/ConnectionReadModelService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceRevisionService.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaConnectionReadModelRequestTest.kt`
- `ide/theia-frontend/scripts/athena-connection-navigator.test.mjs`
- `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`
- `ide/theia-frontend/src/browser/athena-connection-navigator-widget.tsx`
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
- `ide/theia-frontend/src/browser/athena-semantic-selection-service.ts`
- `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/style/index.css`

### Change Log

- 2026-08-11: Created Story 3.3 from complete M46 sprint, Epic 3 requirements, PRD FR-13,
  Architecture AD-15, Story 3.2 intelligence, current LSP/Theia selection patterns, and git context;
  status `ready-for-dev`.
- 2026-08-11: Implemented and verified revision-bound Connection Navigator, live-buffer publication, placed/unplaced
  inspection, semantic source navigation, and all-projection canvas selection; status `review`.
