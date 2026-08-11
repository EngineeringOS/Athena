---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 3.1: Classify Edit Operations And Journal Transactions

Status: done

## Story

As a maintainer,
I want every canvas operation classified by authority and journaled after acceptance,
so that presentation, representation, and engineering edits cannot leak authority into each other.

## Acceptance Criteria

1. Given Move, Align, Distribute, Snap, Set Style, Change Symbol, Reconnect, Bind Part, Undo, or Redo,
   when its operation contract is constructed or decoded, then it belongs to one closed operation kind
   with one server-fixed authority class: presentation for Move/Align/Distribute/Snap/Set Style,
   representation for Change Symbol, engineering for Reconnect/Bind Part, and journal-derived authority
   for Undo/Redo; unknown kinds, fields, or authority claims fail closed.
2. Given an operation reaches the server, when its envelope is validated, then it carries a stable
   operation id, stable target identities, Source Trace context, one structured Source Revision, and a
   sorted unique repository-relative writable file set; the server independently derives authority and
   allowed files and rejects spoofed authority, absolute paths, traversal, duplicates, or undeclared files
   before handler execution.
3. Given Source Revision is calculated, when any governed input changes, then its structured tuple changes:
   scene input revision, normalized source-root identity, engineering-source digest, Sheet digest, Style
   Companion digest or explicit absent marker, `athena.lock` digest, admitted package/item digests, and
   compiler/schema/profile versions; any tuple mismatch rejects as stale without mutation.
4. Given a supported operation passes envelope and revision checks, when the server accepts it, then one
   transaction derives writable files, stages the complete forward patch set, records exact inverse patches,
   compiles and validates the staged workspace, atomically publishes every file, publishes one Canonical
   Scene with one correlation id, and only then appends exactly one Operation Journal entry.
5. Given one accepted transaction, when its journal entry is inspected, then it contains journal id and
   sequence, operation id/type, authority class, target identity, Source Trace, previous and resulting
   Source Revisions, exact writable files, forward and inverse patch sets, staged compile result, and
   publication correlation id.
6. Given a rejected envelope, stale revision, staged failure, rollback, unsupported future handler, or
   duplicate conflict, when processing ends, then source and accepted scene remain unchanged and no journal
   entry is appended; replaying the exact same accepted operation id returns the same accepted result without
   a second entry, while reusing that id with different content rejects as conflict.
7. Given Story 2.2 `SetStyle` solidification, when it succeeds through the new transaction engine, then it
   remains the first supported journaled Presentation operation and preserves the existing preview/discard,
   same-basename Style-only write, deterministic recompile, trace/identity, and product-proof behavior.
   Other classified operations may reject as unsupported until their owning Stories 3.2, 3.3, and 3.5.
8. Given the refactor is complete, when architecture and generated-contract tests run, then retired
   `DiagramCommand` types, schema, mapper, endpoint, `ConnectPorts`, fake accepted Move/Connect responses,
   singular `WorkspaceEdit`, and in-memory result map masquerading as history are absent; no compatibility
   alias, deprecated endpoint, or dual schema remains.

## Tasks / Subtasks

- [x] Replace command contracts with closed Edit Operation and journal models (AC: 1, 2, 5, 8)
  - [x] Add failing interaction-model tests covering every operation kind, fixed authority mapping, stable
        identities/trace, path normalization, unknown fields, spoofed authority, and invalid writable sets.
  - [x] Replace `DiagramCommandContracts.kt` and `athena-diagram-command.schema.json` with cohesive
        `EditOperationContracts.kt`, `OperationJournalContracts.kt`, and one closed edit-operation schema.
  - [x] Model multi-file forward/inverse source patches; do not retain singular `WorkspaceEdit`.
  - [x] Keep Undo/Redo as journal intents whose effective authority comes from the referenced accepted entry.
- [x] Establish complete structured Source Revision (AC: 2, 3, 6)
  - [x] Add failing tests proving every PRD tuple component changes the revision and any mismatch fails stale.
  - [x] Centralize revision calculation in one LSP/kernel service; remove partial revision assumptions from
        style-specific and frontend payloads.
  - [x] Sort package/item digest records deterministically and use explicit absent markers.
- [x] Build accepted-only Operation Journal and transaction engine (AC: 4, 5, 6, 7)
  - [x] Add failing tests for accepted-only append, monotonic sequence, complete entry, exact inverse patch,
        idempotent replay, conflicting id reuse, unsupported handler, failed staging, rollback, and no append.
  - [x] Split orchestration by responsibility: operation service, source transaction engine, revision service,
        and session journal; avoid another mixed-responsibility service dump.
  - [x] Derive authority and writable files on server before staging; compare full revision before any write.
  - [x] Stage all files, compile/validate staged content, atomically publish or restore all files, publish one
        scene/correlation id, then append one journal entry.
- [x] Migrate Set Style to the new transaction path (AC: 4, 6, 7, 8)
  - [x] Preserve existing deterministic Style Companion editor and staged scene validation.
  - [x] Make accepted Set Style return resulting structured revision and one complete journal entry.
  - [x] Prove stale, ambiguous, invalid, missing-target, compile, and IO failures mutate nothing and append
        nothing.
- [x] Replace LSP and frontend transport without compatibility (AC: 1, 2, 6, 8)
  - [x] Replace wire mapper and `athena/applyDiagramCommand` with one edit-operation mapper and endpoint.
  - [x] Regenerate frontend schema/types/validators; remove old generated command schema and names.
  - [x] Update bridge, widget, and M44 style proof to submit intent only through the new endpoint.
  - [x] Add architecture tests proving old endpoint, mapper, schema, and public type names are absent.
- [x] Preserve prior product behavior and verify sequentially (AC: all)
  - [x] Run focused interaction-model, LSP, compiler, language, and frontend contract/tests one at a time.
  - [x] Rebuild full Theia frontend/product and rerun live style plus ruler proof.
  - [x] Run sequential full Gradle `test`, encoding audit, and source-set hygiene audit.
  - [x] Record Story 3.1 operation/journal evidence under M44 implementation artifacts.

## Dev Notes

### Authority And Scope

- This story creates transaction foundations for all Epic 3 operations. It classifies full operation breadth
  but executes only current `SetStyle`. Story 3.2 implements Move/Align/Distribute/Snap, Story 3.3 implements
  Change Symbol/Reconnect/Bind Part, and Story 3.5 implements Undo/Redo replay and durable transcripts.
- Operation Journal is accepted source-transaction history. It is not Semantic SCM history, raw mouse-event
  history, Konva state, a cache of command results, or a second engineering authority.
- Frontend submits typed intent only. Server derives authority, writable files, source patches, validation,
  and acceptance. Canonical Scene and Konva state remain disposable outputs.
- No backward compatibility. Delete retired command endpoint/schema/types/tests instead of aliasing them.

### Required Models

Use compact cohesive contracts rather than one type per file:

```text
EditOperationEnvelope
  operationId
  kind
  target identities
  sourceTrace
  sourceRevision
  requested writable files
  body

SourceRevision
  sceneInputRevision
  sourceRootIdentity
  engineeringSourceDigest
  sheetDigest
  styleDigest | absent
  lockDigest | absent
  packageItemDigests[]
  compilerVersion
  sceneSchemaVersion
  profileVersion

SourcePatchSet
  files[] { relativePath, beforeDigest/absence, afterBytes/absence }

OperationJournalEntry
  journalEntryId / sequence
  operation identity, kind, authority, targets, trace
  previous/resulting revision
  writable files
  forward/inverse patch sets
  staged compile result
  publication correlation id
```

Patch representation must support create, update, and delete so exact inversion and future multi-file
operations do not require a new contract. Paths are normalized repository-relative POSIX paths; server
rejects absolute paths, `..`, duplicates, and paths outside operation ownership.

### Current Code Reality And Required Retirement

- `kernel/interaction-model/.../DiagramCommandContracts.kt` currently exposes only Move, Connect Ports,
  and Set Style, a client-visible authority field only on Set Style, partial `SourcePrecondition`, singular
  `WorkspaceEdit`, and `DiagramCommandResult`. Replace this file and its schema entirely.
- `ide/lsp/.../DiagramAuthoringService.kt` mixes connection options, validation, patch generation, file IO,
  idempotency cache, and operation execution. Split transaction/revision/journal responsibilities. Preserve
  only cohesive reusable Sheet Style editing behavior.
- Current `results: LinkedHashMap` caches every result and is not an Operation Journal. Exact accepted replay
  may use an idempotency index, but rejected operations never become journal entries.
- Current Move and Connect paths can return `ACCEPTED` with an unapplied edit. Remove this false acceptance.
  `ConnectPorts` is not M44 `ReconnectPort`; delete it and let Reconnect remain classified/unsupported until
  Story 3.3.
- Replace `DiagramCommandWireMapper.kt`, `athena-diagram-command.schema.json`,
  `athena/applyDiagramCommand`, frontend `requestDiagramCommand`, generated old command types, and tests
  preserving those names. No shim or deprecated endpoint.
- `AthenaLspSessionHostReady.currentInputRevision()` already includes source, Sheet, optional Style, lock,
  and presentation contract records, but returns one opaque digest and does not expose/compare the complete
  structured tuple or admitted package/item digest records. Centralize and complete it.
- Story 2.2 live proof already establishes exact Style-only mutation and preview/discard/solidify behavior.
  Migrate that behavior; do not rewrite style syntax, renderer, ruler chrome, or preview model.

### Transaction Ordering

Required accepted path:

```text
validate envelope
compare structured Source Revision
derive authority and writable files
stage complete patch set
compile and validate staged workspace
atomically publish all files
publish one Canonical Scene and correlation id
append exactly one Operation Journal entry
```

Rollback or rejection before final append leaves journal unchanged. Journal append failure must not leave
an unjournaled accepted source mutation; transaction boundary must account for it explicitly.

### Project Structure

Expected product files:

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/EditOperationContracts.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/OperationJournalContracts.kt`
- `kernel/interaction-model/src/main/resources/schema/athena-edit-operation.schema.json`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationWireMapper.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceRevisionService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngine.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SessionOperationJournal.kt`

Group small related value/data types per Kotlin organization rule. Keep orchestration flows cohesive; split
files crossing roughly 200-300 lines with distinct roles.

### Testing And Regression Guardrails

- Interaction tests own closed kind/authority/schema/path/journal invariants.
- LSP tests own wire decoding, full revision CAS, server-derived writable files, staging, rollback,
  idempotency, journal append ordering, and Set Style transaction behavior.
- Generated frontend contract tests must prove only new operation names exist.
- Product proof must preserve Story 2.2 style round trip and Story 2.3 1440x960 / 760x720 ruler geometry.
- No new library is required. Keep pinned Kotlin 2.4.0, LSP4J 0.23.1, TypeScript 5.9.2, Node >=22,
  Yarn 1.22.22, Theia 1.73.1, Konva 10.3.0, and React 18.3.1.
- Gradle commands must run strictly sequentially.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md` (`FR-9`, `FR-10`, `FR-11`, `FR-12`)
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/addendum.md` (`Edit and History Boundary`)
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md` (`AD-6`, `AD-7`, `AD-8`, `AD-11`)
- Design: `_bmad-output/planning-artifacts/m44/design.md` (`Edit Operations and Source Round-Trip`)
- Epics: `_bmad-output/planning-artifacts/m44/epics.md` (`Epic 3`, `Story 3.1`)
- Prior implementation: `_bmad-output/implementation-artifacts/m44/2-2-preview-discard-and-solidify-style-edits.md`
- Ruler regression: `_bmad-output/implementation-artifacts/m44/2-3-render-aligned-editor-rulers.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Story contexted through BMad create-story from full M44 PRD/addendum/reviews, architecture, design,
  epics, sprint status, Story 2.2/2.3 intelligence, CodeGraph current-code analysis, subagent artifact/code
  review, and git state.
- Red-green-refactor completed for closed operation classification, structured Source Revision, accepted-only
  journal behavior, transaction ordering/rollback, Set Style migration, explicit wire mapping, and retired
  command-surface absence.
- Live screenshot review exposed two renderer unit errors: stage scaling multiplied route stroke width and
  `ScenePort.hitRadius` was painted as visible geometry. Failing frontend contract added first; Konva now
  keeps route strokes in screen space and separates tiny markers from generous invisible hit geometry.
- First post-rebuild product run repeatedly reopened the same widget while polling, preventing a stable canvas
  digest. Proof automation now opens workspace/source once, then polls accepted paint without reactivation.
- Final sequential verification passed frontend 40/40, full Theia build, live M44 style/ruler proof, full
  Gradle `test` (118 tasks), encoding audit, source-set hygiene audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Replaced the retired Diagram Command model and endpoint with one closed Edit Operation contract whose
  authority and writable files are server-derived and whose unknown/spoofed inputs fail closed.
- Added full structured Source Revision comparison, deterministic package/item records, exact multi-file
  forward/inverse patches, atomic staged publication, accepted-only journal append, idempotent accepted
  replay, and conflicting operation-id rejection.
- Migrated Set Style as the first supported journaled Presentation operation; all later operation kinds are
  classified but reject `UNSUPPORTED` until their owning stories.
- Removed old command schema/mapper/endpoint, `ConnectPorts`, fake accepted operations, singular
  `WorkspaceEdit`, and result-cache history behavior without compatibility aliases.
- Added permanent engineering-document visual Golden Rule using
  `draft/screenshort/equipement_d'un_volet_roulant.png`; rebuilt screenshots prove thin screen-space routes
  and tiny port markers while preserving invisible interaction targets.

### File List

- `AGENTS.md`
- `_bmad-output/implementation-artifacts/m44/3-1-classify-edit-operations-and-journal-transactions.md`
- `_bmad-output/implementation-artifacts/m44/operation-transcripts/2-2-style-authoring-product-proof.json`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-accepted.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-discarded.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-preview.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-rulers-desktop.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-rulers-narrow.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-solidified.png`
- `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`
- `contracts/presentation/v1/operations/rejection-vectors.json`
- `contracts/presentation/v1/operations/set-style-operation.json`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationWireMapper.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SessionOperationJournal.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SetStyleOperationHandler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceRevisionService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngine.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocolArchitectureTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/EditOperationWireMapperTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngineTest.kt`
- `ide/theia-frontend/scripts/athena-diagram-contracts.test.mjs`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/generated/schema-hash.ts`
- `ide/theia-frontend/src/browser/diagram/generated/schema/athena-edit-operation.schema.json`
- `ide/theia-frontend/src/browser/diagram/generated/types.ts`
- `ide/theia-frontend/src/browser/diagram/generated/validators.ts`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-product/scripts/athena-m44-style-proof-main.js`
- `ide/theia-product/scripts/verify-athena-m44-style-proof.js`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/EditOperationContracts.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/OperationJournalContracts.kt`
- `kernel/interaction-model/src/main/resources/schema/athena-edit-operation.schema.json`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/EditOperationContractTest.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/OperationJournalContractTest.kt`
- Deleted: retired `DiagramCommand` contracts/schema/tests, `DiagramAuthoringService`, command wire mapper,
  generated command schema, old command endpoint, and command-contract proof vectors.

### Change Log

- 2026-08-07: Story created via BMad create-story flow for classified Edit Operations and accepted-only
  Operation Journal transactions.
- 2026-08-07: Replaced Diagram Commands with validated Edit Operation transactions and accepted-only journal,
  migrated Set Style, retired compatibility surfaces, restored professional thin line/port rendering, and
  completed full product/regression evidence; story ready for review.
