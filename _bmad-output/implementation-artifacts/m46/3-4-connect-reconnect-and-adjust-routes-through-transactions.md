---
story: 3.4
epic: 3
title: Connect Reconnect And Adjust Routes Through Transactions
status: review
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 3.4: Connect Reconnect And Adjust Routes Through Transactions

Status: review

## Story

As an engineer,
I want canvas connection edits to update the correct source authority,
so that visual editing and engineering truth remain synchronized.

## Acceptance Criteria

1. Given two compatible package-backed Port anchors, when the engineer connects or reconnects them, then
   frontend sends a typed Engineering Edit Operation containing canonical Endpoint identities, closed intent,
   operation id, stable targets, source trace, and full Source Revision; server validates, stages governed
   engineering source, compiles Connection IR, projection, Route Plan, and Scene, durably commits one source
   transaction, appends one accepted journal entry, then publishes READY state.
2. Given a stale revision or invalid candidate, including missing identity/trace, incompatible Port direction,
   potential/signal, Connection Kind, physical specification, or Net membership, when operation runs, then one
   exact plain-language diagnostic names subject, problem, and correction; engineering source, Sheet, lock,
   journal, accepted Connection IR, Route Plan, and Scene remain byte/digest unchanged.
3. Given a route segment or bend adjustment, when engineer commits it, then a typed Presentation Edit Operation
   persists one closed logical Sheet constraint against stable connection/projection identity; it contains no raw
   viewport or Konva coordinates and cannot reconnect Ports or change Connection/Net identity, membership, kind,
   potential/signal, or physical requirements.
4. Given an accepted Connect, Reconnect, or route edit, when Undo/Redo runs, then its accepted source transaction
   reverses/reapplies atomically through the same CAS, stage, compile, durable commit, journal, and READY publication
   path; pointer move, hover, snap, candidate preview, cancel, and rejected operations create no history entry.

## Tasks / Subtasks

- [x] Replace stale connection edit contracts with M46 authority (AC: 1-4)
  - [x] Add cohesive typed connection operations in
    `kernel/interaction-model/.../ConnectionEditOperations.kt`: Connect, Reconnect, and closed route
    segment/bend adjustment. Use closed Endpoint role/intent/constraint types, not free-form strings.
  - [x] Delete `ReconnectPort(relationshipId, endpointRole: String, portId)` and its generic Relationship path.
    No alias, fallback, deprecated kind, legacy decoder, or compatibility fixture remains.
  - [x] Keep Connect/Reconnect as `ENGINEERING`; keep route adjustment as `PRESENTATION`. Ensure clients cannot
    submit or spoof authority class.
  - [x] Update canonical edit-operation schema, strict Kotlin wire mapper, generated TypeScript contracts and
    validators. Unknown kind/field, raw pixel field, invalid path, or malformed identity must fail closed.
- [x] Add structured engineering Connection mutation (AC: 1, 2, 4)
  - [x] Create `ConnectionSourceEditor.kt` using parsed `ConnectionDeclaration`/Net syntax and source spans. Do not
    string-search source, mutate generic `RelationDeclaration`, assemble source in frontend, or patch compiled IR.
  - [x] Create `ConnectionOperationHandler.kt`; derive actual writable engineering file server-side, verify target
    and primary source trace against current accepted Connection IR/Scene, and admit only package-backed semantic
    Port identities. SVG anchors provide geometry only.
  - [x] Validate role/direction, potential/signal, kind, physical specification, and Net membership with current M46
    compiler/validation contracts before commit. Connect intent must include required facts such as `crossSection`
    where Connection Kind requires them.
  - [x] Recompile full semantic source -> Connection IR -> projection -> Route Plan -> Scene. Prove deterministic
    resulting identity; do not assume reconnect preserves id because current canonical id derives from endpoints.
  - [x] Remove reconnect dispatch/import from `RepresentationAndEngineeringOperationHandler`; keep that handler
    focused on representation, Part, package, and Macro behavior.
- [x] Add logical Sheet route-constraint authoring (AC: 2-4)
  - [x] Replace stringly `LogicalRouteConstraint(name, value)` with smallest closed typed contract needed for
    segment/bend adjustment, addressed by stable connection/projection plus stable logical target.
  - [x] Extend Sheet Companion syntax/parser/editor and projection lowering to persist durable logical coordinates
    or constraints only. Human source must not expose Scene segments, topology operator ids, bridge markers,
    renderer fields, or viewport X/Y.
  - [x] Make `ConnectionRoutePlanner` consume typed logical constraints deterministically. Route edit may invalidate
    its connected topology group and local colliders only; unrelated route identities/paint remain unchanged.
  - [x] Create a route operation handler using the existing transaction engine; derive same-basename
    `.sheet.athena` as the sole writable authority and reject semantic-field changes.
- [x] Wire transient Theia/Konva intent to server transactions (AC: 1-4)
  - [x] Reuse `AthenaLspEditorBridgeService` and current edit request. Add typed Connect/Reconnect and route commit
    methods carrying operation id, target identities, source trace, and full Source Revision.
  - [x] Add restrained valid/invalid endpoint candidate and bend/segment preview to existing Presentation widget
    and `KonvaDiagramAdapter`. Keep accepted Scene visible until server acceptance; cancel/reject removes preview.
  - [x] Convert pointer position into logical Sheet coordinates before operation construction. Konva nodes, hit
    radius, pointer events, and candidate paint never become source or journal authority.
  - [x] Preserve professional one-pixel route grammar, invisible generous Port hit targets, compact labels,
    zoom-invariant visual weight, style target, and annotation visibility.
- [x] Prove atomic rejection, journal replay, and regressions (AC: 1-4)
  - [x] Add interaction-model tests for closed operation types, fixed authority, canonical encoding, strict decode,
    path validation, and removal of legacy reconnect.
  - [x] Add LSP tests for accepted Connect/Reconnect, deterministic identity, full recompile/publication, and exact
    before/after patch/journal/revision evidence.
  - [x] Add rejection matrix tests for stale/no-op, missing connection/endpoint/trace, role-direction mismatch,
    potential/signal mismatch, kind/spec mismatch, invalid Net membership, parse/compile failure, and journal or
    publication failure. Assert no partial source, Sheet, lock, journal, IR, plan, or Scene state.
  - [x] Add route tests proving Sheet-only patch, unchanged engineering source/Connection IR/lock, deterministic
    plan/Scene change, focused invalidation, and raw viewport fields rejected.
  - [x] Add Undo/Redo tests for Connect, Reconnect, and route edit; frontend tests prove one committed gesture emits
    one operation while pointer events/previews emit zero journal operations.
  - [x] Run focused tests, frontend contract generation/check/tests, full sequential Gradle test, encoding audit,
    source-set hygiene audit, and `git diff --check`.

## Dev Notes

### Non-Negotiable Authority Chain

```text
Connect/Reconnect intent -> EngineeringEditOperation -> governed .athena
Route bend/segment intent -> PresentationEditOperation -> same-basename .sheet.athena
                                   |
                                   v
full Source Revision CAS -> stage -> parse/lower/validate -> compile IR/projection/plan/Scene
                         -> durable source commit -> accepted journal -> READY publication
```

- Connection/Net existence, endpoints, roles, kind, potential/signal, membership, and physical requirements are
  Engineering Reality. Route constraints are Presentation Reality. Canvas/Konva remains disposable paint.
- Rejection, stale revision, no-op, compile failure, commit failure, or publication failure must leave every
  accepted authority unchanged. Never force apply.
- One user commit is one journal transaction. Undo/Redo replays accepted patch sets through normal validation and
  compilation. Pointer events never enter history.
- EPLAN lesson retained: Connections Navigator, definition-point, and Smart Connect workflow are useful; graphic
  proximity never creates engineering truth. Port semantics have no x/y. Package SVG supplies anchor geometry.

### Current Code And Required Replacement

- `EditOperationContracts.kt` currently exposes only stale
  `ReconnectPort(relationshipId, endpointRole: String, portId)`. Replace it; M46 uses first-class
  `EngineeringConnection`/`EngineeringNet`, not generic Relationship identity.
- `RepresentationAndEngineeringOperationHandler.reconnect()` parses `RelationDeclaration` and replaces one source
  span. Delete this path and move connection mutation into dedicated `ConnectionOperationHandler`.
- `ConnectionProjection.logicalRouteConstraints` currently defaults empty and is not populated from Sheet source;
  `LogicalRouteConstraint(name, value)` is stringly. Deepen this existing contract rather than creating parallel
  route metadata.
- `SheetCompanionLanguage.kt` and `SheetCompanionEditor.kt` currently own page/frame/snap/title/placements. Extend
  this one grammar/editor. Do not create another companion or JSON sidecar.
- Reuse `EditOperationService`, `SourceRevisionService`, `SourceTransactionEngine`, and
  `SessionOperationJournal`. Existing engine already owns CAS, staging, atomic publish/rollback, and accepted
  patch replay; fix only gaps exposed by tests.
- Reuse Story 3.3 `ConnectionReadModel` and `AthenaSemanticSelectionService` for stable endpoint/connection trace.
  Frontend caches are read state, never transaction authority.

### Transaction And Identity Decisions

- Envelope must carry full existing `SourceRevision`: accepted Scene Input Revision, source-root identity,
  engineering source/Sheet/style/lock/package item digests, compiler/schema/profile versions.
- Server derives writable file from current authority; requested path remains a least-privilege assertion and must
  exactly match derived patch files.
- Current Connection identity is canonical over source unit, kind, and endpoints. Reconnect may replace old
  identity with a deterministic new identity. Journal lineage and source trace provide continuity; tests must not
  claim false identity preservation.
- A route constraint targets stable Connection projection identity plus logical intent, never a transient
  Konva/Scene segment id whose geometry may change after replanning.
- Durable source commit precedes accepted journal and READY publish. Recovery metadata may be pre-staged, but no
  accepted journal/Scene publication may survive failed source commit.

### Architecture Guardrails

- Governed by M46 AD-1 through AD-6, AD-9/10, AD-14 through AD-18; inherited M44 transaction laws and M45 AD-7,
  AD-12/13/14/17/20.
- Delete generic relationship reconnect and stringly route-constraint contracts. No compatibility layer,
  `SceneRoute`, alternate endpoint, source-string search, renderer topology inference, or milestone-named
  production class.
- Keep Kotlin files cohesive by role. Split current 573-line mixed handler by extracting connection behavior;
  do not create one tiny DTO file per type or another large mixed-responsibility file.
- No new dependency. Use pinned repository stack: Java 25, Gradle 9.2.1, Theia 1.73.1, React 18.3.1,
  TypeScript 5.9.x, Konva 10.3.0, and current repository-pinned Kotlin/LSP4J versions.

### Expected Files / Areas

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/ConnectionEditOperations.kt` (new)
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/EditOperationContracts.kt`
- interaction contract/schema tests and generated protocol schema/TypeScript output
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionEditor.kt`
- one structured Connection source editor in `kernel/language`
- `kernel/projection-model/.../ConnectionProjectionModels.kt`
- `kernel/compiler/.../AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/.../ConnectionRoutePlanner.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/ConnectionOperationHandler.kt` (new)
- one cohesive LSP route-operation handler if separation keeps responsibilities clear
- `ide/lsp/.../EditOperationWireMapper.kt`, `EditOperationService.kt`, and focused tests
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- focused frontend contract/interaction tests

### Previous Story Intelligence

- Story 3.3 publishes immutable Connection/Net read state from accepted Connection IR with attempted/accepted
  revision tracking, live dirty-buffer compilation, placed/unplaced state, projection traces, and stable semantic
  source selection. Reuse this data and transport; do not parse source in Theia.
- Frontend accepts only READY read models keyed by accepted revision and rejects repository-old/out-of-order
  responses. Apply same repository/revision discipline to edit responses.
- Every `SceneConnection` projection sharing semantic identity can be selected through paint-only overlay. Edit
  previews must stay equally non-authoritative and must not change label/style state.
- Previous verification passed `:ide:lsp:test`, frontend 63/63, `contracts:check`, full Gradle `test`, encoding and
  source-set hygiene audits.

### Verification

Run Gradle commands strictly sequentially:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
Set-Location ide/theia-frontend
yarn contracts:generate
yarn contracts:check
yarn test
Set-Location ../..
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
git diff --check
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 3 / Story 3.4]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR-14, FR-15, SM-5]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-14 through AD-18]
- [Source: `_bmad-output/planning-artifacts/m46/implementation-plan.md`, Task 11]
- [Source: `_bmad-output/implementation-artifacts/m46/3-3-navigate-placed-and-unplaced-connectivity.md`]
- [Source: `draft/20260803-confuse/connections-details.md`]
- [Source: `AGENTS.md`, Pre-1.0, Human-First Language, E2E Proof, and Engineering Document Visual Golden Rules]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Initial LSP fixture failures exposed a contract bug: Scene Port `portId` carried Spatial anchor identity,
  while edit operations require semantic Port identity. Closed Scene contract now separates `anchorId` and
  `semanticPortId`; Scene Connection also publishes stable `projectionId`.
- Compiler-generated Connection/Projection identities contain `->`; interaction contracts and JSON schemas now
  admit this canonical character instead of rejecting compiler-owned identity.

### Completion Notes List

- Typed `ConnectPorts`, `ReconnectConnectionEndpoint`, and `AdjustConnectionRoute` now use closed contracts and
  fixed authority classes. Legacy generic Relationship reconnect path deleted.
- Structured `ConnectionSourceEditor` mutates parsed Connection declarations through source spans. Server owns
  writable-file derivation, Source Revision CAS, staging, compile, durable commit, journal, and READY publication.
- Sheet route edits persist typed logical `(column, row)` constraints against stable Connection/Projection target;
  no viewport pixels, Konva nodes, or renderer topology enters source authority.
- Theia/Konva now keeps transient previews paint-only, separates semantic Port and geometry anchor IDs, emits one
  typed operation per committed gesture, and refreshes only after server acceptance.
- Verification passed: focused interaction/language/compiler/LSP tests, full `:ide:lsp:test`, frontend contract
  generation/check and 71/71 tests, full sequential Gradle `test`, encoding audit, source-set hygiene audit, and
  `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m46/3-4-connect-reconnect-and-adjust-routes-through-transactions.md`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/ConnectionEditOperations.kt`
- `kernel/interaction-model/src/main/resources/schema/athena-edit-operation.schema.json`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/ConnectionEditOperationsTest.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/ConnectionSourceEditor.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionEditor.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionRoutePlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContract.kt`
- `kernel/presentation-model/src/main/resources/schema/athena-diagram-scene.schema.json`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/ConnectionOperationHandler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/ConnectionOperationHandlerTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/JournalUndoRedoTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/diagram/generated/types.ts`
- `ide/theia-frontend/src/browser/diagram/generated/validators.ts`
- `ide/theia-frontend/scripts/athena-connection-authoring.test.mjs`

### Change Log

- 2026-08-11: Created Story 3.4 from complete M46 sprint, Epic 3, PRD FR-14/FR-15, architecture
  AD-14 through AD-18, Story 3.3 intelligence, current transaction/connection code, and git context; status
  `ready-for-dev`.
- 2026-08-11: Implemented typed Connection/route transactions, semantic-vs-anchor Scene contract, Theia gesture
  commits, rejection proofs, and transaction Undo/Redo evidence; all verification gates passed; status `review`.
