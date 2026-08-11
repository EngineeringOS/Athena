---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 3.2: Apply Move And Lock Commands

Status: done

## Story

As an engineer, I can drag an occurrence to a snapped Sheet Anchor and lock it, so authored layout
survives recompilation and undo.

## Acceptance Criteria

1. Drag preview uses the shared grid snap contract and emits revision-checked `MoveOccurrence` only
   on drop. Pointer movement remains transient adapter state; no source text changes during preview.
2. Runtime validates the command and returns one versioned single-file `WorkspaceEdit` against the
   colocated `*.sheet.athena`; frontend never parses Athena or edits text directly.
3. Stale scene/input revision, stale source precondition, locked occurrence, invalid Sheet Anchor,
   out-of-bounds anchor, and blocking overlap reject with deterministic diagnostics. Rejection leaves
   source bytes and accepted `READY` scene unchanged.
4. Accepted move/lock edit applies through existing editor undo/redo as one undo unit; normal source
   synchronization recompiles a correlated `READY` publication with stable occurrence identity,
   persisted anchor/lock, and deterministic scene digest/order.
5. Duplicate command ID with identical content returns original result; reuse with different content
   returns `CONFLICT`. No command fallback, rebase, or compatibility reader exists.
6. Focused Kotlin/frontend tests, frontend build, root Gradle tests, encoding audit, and source-set
   hygiene audit pass sequentially.

## Tasks / Subtasks

- [x] Create runtime-owned `DiagramAuthoringService` move/lock path over existing compiler and Sheet Companion parser.
  - [x] Validate command envelope, scene/input revision, source preconditions, idempotency, target occurrence, lock state, anchor grid, bounds, and overlap.
  - [x] Produce one structured `WorkspaceEdit` for the companion file; validate proposed companion source before returning acceptance.
  - [x] Keep rejection fail-closed and preserve accepted publication.
- [x] Wire `AthenaLanguageServer.applyDiagramCommand` to runtime service; connect remains unavailable until Story 3-3.
- [x] Add typed frontend command bridge and adapter drop/lock callbacks; adapter restores transient drag state until accepted source edit.
- [x] Add command contract and adapter interaction coverage through generated validators and frontend tests.
- [x] Run all required sequential verification and complete story records.

## Dev Notes

### Authority

- `AthenaDiagramScene` and accepted `AssetBundle` remain sole paint input.
- Semantic source owns occurrence/Port identity. Colocated `*.sheet.athena` owns authored cell/micro
  anchor and lock intent. Exact pixels, routes, SVG geometry, and Konva state remain derived.
- Runtime/LSP owns validation and source mutation. Frontend sends typed command and applies returned
  LSP `WorkspaceEdit`; it must not perform string surgery or infer semantic relationships.

### Command Contract

Use `kernel/interaction-model/.../DiagramCommandContracts.kt`, generated frontend types, and
`M43-CONTRACT-PACK.md` section 6. Every command carries UUID `commandId`, `sceneId`, expected
`InputRevision`, non-empty `SourcePrecondition` list, and `MoveOccurrence(sheetId, occurrenceId,
SheetAnchor(address, microX, microY), lockAction)`.

Accepted result contains prior revision, one companion-file `WorkspaceEdit`, and publication
correlation ID. Rejected result contains reason plus ordered human diagnostics. Never rebase stale
commands or mutate source on rejection.

### Existing Code To Reuse

- `AthenaDiagramSceneCompiler`, `SheetCompanionProjectionPlacementMapper`,
  `GridAlignedSpatialLayoutCompiler`, `AthenaSheetCompanionParser`, and `lowerSheetAnchor` are current
  authorities. Extend/refactor aligned code; do not duplicate grid math.
- `KonvaDiagramAdapter` is only interaction owner. Add drag handlers and callbacks without adding a
  second canvas/DOM layer.
- `AthenaLspEditorBridgeService` already requests `athena/diagramScene`; add typed command request and
  use Theia editor APIs for one undoable WorkspaceEdit.

### Required Diagnostics

Diagnostics name exact sheet/occurrence/field, problem, correction, and stable code. Required rejection
codes include stale revision, precondition mismatch, locked occurrence, invalid anchor, bounds,
overlap, duplicate conflict, and unavailable companion.

### References

- `_bmad-output/planning-artifacts/m43/epics.md` Story 3-2
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md` FR-3, FR-5, FR-6, FR-9, FR-12
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md` AD-9, AD-11, AD-12, AD-14
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md` section 6
- `_bmad-output/implementation-artifacts/m43/3-1-render-scene-through-konva-adapter.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

Focused LSP tests, frontend `yarn test`, root Gradle `test`, encoding audit, and source-set hygiene audit pass.

### Completion Notes List

- Runtime validates scene ID, input revision, source digest/path, Sheet grid anchor, lock, and overlap before one companion `WorkspaceEdit`.
- Duplicate command IDs are idempotent for identical content and reject conflicting reuse.
- Konva drag preview emits typed move callback only on drop and restores node position until source recompile.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramAuthoringService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`

### Change Log

- 2026-08-06: Created through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-06: Implemented runtime move/lock transaction, typed bridge, and Konva drop/lock callbacks;
  final command and regression gates passed; status `done`.
