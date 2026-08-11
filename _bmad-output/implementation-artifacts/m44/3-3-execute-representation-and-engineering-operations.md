---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 3.3: Execute Representation And Engineering Operations

Status: done

## Story

As an engineering author,
I want Change Symbol, Reconnect, and Bind Part to use their correct authority paths,
so that non-graphics engineering intent is validated before source changes.

## Acceptance Criteria

1. Given one READY Canonical Scene, a full current Source Revision, stable targets, and valid Source
   Trace, when Change Symbol, Reconnect, or Bind Part is submitted, then the existing
   `EditOperationService` derives authority and exact writable files server-side and delegates all staging,
   validation, publish, rollback, journal, replay, and conflict behavior to `SourceTransactionEngine`.
   No second endpoint, source writer, journal, renderer authority, or compatibility alias exists.
2. Given Change Symbol targets one visible occurrence and a compatible locked representation item, when
   accepted, then only declared representation binding authority changes. Engineering Entity, Function,
   Occurrence, Relationship, and Port identities, semantic source digest, and relationship facts remain
   unchanged; recompilation publishes the new admitted geometry with stable trace.
3. Given Reconnect targets a Relationship endpoint and compatible Port, when accepted, then only
   engineering source changes after server-owned Relationship, Capability, and Flow validation. Direction,
   domain, flow kind, endpoint role, stable relationship identity, and primary Source Trace are validated;
   route geometry is recompiled and never authored by TypeScript.
4. Given Bind Part targets one Entity and an admitted compatible Part, when accepted, then only declared
   engineering Part binding authority changes after server-owned validation. Entity, Function, Occurrence,
   Relationship, and Port identities stay stable; affected validation, package provenance, and scene facts
   refresh from source.
5. Given incompatible Symbol/Part, stale revision or scene, invalid target/trace, wrong writable path,
   invalid port direction/domain/flow/capability, missing/ambiguous binding source, staged parse/compile
   failure, file I/O failure, or no-op, when processing ends, then no source or accepted scene changes and
   no journal entry is appended. Diagnostic states exact subject, problem, correction.
6. Given accepted Change Symbol, valid Reconnect, Bind Part, and rejected invalid Reconnect on the active
   M44 example, when it refreshes and reopens, then the accepted source state and stable identities
   reproduce. Evidence records forward/inverse patch, old/new revision, target traces, writable files,
   journal entry, correlation id, rejection no-op, and desktop/narrow screenshots under M44 only.

## Tasks / Subtasks

- [x] Establish authority-specific source editors and failing tests (AC: 1, 2, 3, 4, 5)
  - [x] Read current representation-binding and engineering-source grammar/model before mutation; use
        structured parser/editor APIs, never source-text search or frontend payload patches.
  - [x] Add focused failing tests for deterministic Change Symbol binding update and no-op/rejection.
  - [x] Add focused failing tests for Reconnect endpoint update and Bind Part update, preserving stable
        IDs and unrelated source ordering.
- [x] Implement common transaction routing for all three typed bodies (AC: 1, 5)
  - [x] Add cohesive authority handlers beside `PlacementOperationHandler` and `SetStyleOperationHandler`;
        keep `EditOperationService` dispatch-only.
  - [x] Derive exact writable paths server-side: representation binding only for Change Symbol; engineering
        source only for Reconnect/Bind Part. Reject user-declared wrong paths.
  - [x] Reuse `SourceTransactionEngine` unchanged where possible for CAS, staged compile, atomic publish,
        accepted-only journal append, replay, and rollback.
- [x] Enforce representation identity preservation (AC: 2, 5)
  - [x] Resolve occurrence only from accepted scene `occurrenceId` and its primary trace.
  - [x] Resolve Symbol compatibility from locked library/package facts; SVG geometry and descriptor remain
        package authority, never engineering truth.
  - [x] Prove Symbol replacement changes binding/asset scene facts while preserving Entity, Function,
        Occurrence, Relationship, and Port identities.
- [x] Enforce engineering validation before source mutation (AC: 3, 4, 5)
  - [x] Route Reconnect and Bind Part through current M42 `EngineeringValidationDocument` /
        compiler validation path; do not duplicate compatibility checks in TypeScript.
  - [x] Prove valid reconnect and part replacement publish one new scene; route geometry is derived.
  - [x] Prove invalid direction, incompatible domain/flow/capability, and invalid Part leave source, scene,
        and journal unchanged with plain diagnostics.
- [x] Add minimal product interaction and proof automation (AC: 1, 6)
  - [x] Add compact Theia controls or selection-driven actions for Change Symbol, Reconnect, and Bind Part;
        frontend sends typed intent, stable ids, revision, and trace only.
  - [x] Keep Konva disposable: drag/link preview cannot mutate scene, route, source, or journal.
  - [x] Extend M44 temporary-copy Electron proof for accepted Symbol, valid Reconnect, Bind Part, invalid
        Reconnect rejection, refresh/reopen, trace stability, and desktop/narrow screenshots.
- [x] Rebuild and verify sequentially (AC: all)
  - [x] Run focused interaction-model, language, validation, compiler, LSP, and frontend tests one command
        at a time, then full Gradle `test`.
  - [x] Rebuild frontend then Theia product before live E2E; inspect screenshots for clean rulers,
        thin-line/port-marker Golden Rule, readable real symbols, and canvas dominance.
  - [x] Run encoding audit, source-set hygiene audit, `git diff --check`, and store all evidence only in
        `_bmad-output/implementation-artifacts/m44/`.

## Dev Notes

### Authority And Scope

- `ChangeSymbol` is `REPRESENTATION`: library tells how a Symbol looks; Engineering Reality tells what
  it means. It writes declared representation binding authority only and must preserve engineering IDs.
- `ReconnectPort` and `BindPart` are `ENGINEERING`: write engineering source only after M42
  Relationship/Capability/Flow validation. They must never share presentation or representation shortcut.
- `EditOperationEnvelope`, structured `SourceRevision`, `SourcePatchSet`, `SourceTransactionEngine`, and
  `SessionOperationJournal` are the only operation path. Source Revision covers source, Sheet, Style,
  lock, package digests, compiler/schema/profile, scene input revision, and root identity.
- No `DiagramCommand`, `athena/applyDiagramCommand`, `ConnectPorts`, raw `WorkspaceEdit`, source text
  payload, direct Konva persistence, compatibility shim, or alternate endpoint may return.

### Current System And Expected Files

- Update `ide/lsp/.../EditOperationService.kt`: dispatch authority handlers only.
- Add cohesive LSP handler/editor tests beside `PlacementOperationHandler.kt`; do not create one tiny Kotlin
  file per DTO.
- Reuse current `SourceTransactionEngine.kt`, wire mapper/protocol, compiler, validation document, and
  package/runtime binding resolver. Update only where a failing test proves a needed contract gap.
- Update `AthenaPresentationWidget` / `KonvaDiagramAdapter` only for typed intent and disposable preview.
  No JavaScript source parser, semantic validation, geometry persistence, or route authoring.
- Extend only M44 product proof scripts and `examples/m44/rolling-shutter` as active proof fixture.
  Reference assets remain research-only; do not make `reference/elements*` a runtime dependency.

### Product and Visual Guardrails

- Preserve plain HTML rulers at top/left, hidden internal grid, white drawing area, thin screen-space
  linework, tiny visible port markers with generous invisible hits, and no bottom debug table.
- Narrow mode keeps toolbar single-row/scrollable and diagnostic bounded; canvas remains dominant.
- Use actual admitted SVG geometry. No placeholder rectangles, second renderer, GLSP/tldraw/Graphite runtime,
  or WebGPU work belongs here.

### Tests And Evidence

- Kotlin owns authority, deterministic patching, source ordering, validation, staged compile, rollback,
  source identity, and journal tests.
- Frontend tests own only typed intent, selection, transient preview, and absence of duplicated validation.
- Product proof uses a temporary copy of the M44 example and proves active example remains unchanged.
- Store transcripts/screenshots only under M44; do not revive M43/M42 demos or legacy test paths.
- Gradle must run strictly sequentially. Required final gates: focused module tests, `.\gradlew.bat
  --no-daemon --console=plain test`, frontend tests/build, live product proof, encoding audit, source-set
  hygiene audit, and `git diff --check`.

### Previous Story Intelligence

- Story 3.2 fixed source seeding to exclude Sheet/Style companions and proved transaction-backed
  Presentation edits. Keep this resolver behavior.
- Product proof must build `@engineeringood/athena-theia-frontend` before
  `@engineeringood/athena-theia-product`; otherwise Electron uses a stale product bundle.
- Live proof must open workspace/source once then poll READY, not repeatedly activate the widget.
- Narrow E2E exposed unbounded diagnostic layout. Preserve single-row toolbar and bounded one-line error.

### References

- `_bmad-output/planning-artifacts/m44/epics.md` (Epic 3, Story 3.3)
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md` (UJ-3, UJ-4, FR-3, FR-9,
  FR-10, FR-11, FR-12, FR-14, Golden Authoring Loop)
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/addendum.md` (Library Port Metadata,
  Edit and History Boundary)
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md`
  (AD-1, AD-3 through AD-8, AD-11, AD-12)
- `_bmad-output/implementation-artifacts/m44/3-2-execute-presentation-operations.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Ultimate context engine analysis completed: M44 PRD/addendum, architecture spine, epic order, full sprint
  status, Story 3.2 product proof learning, current typed operation contracts, and git context loaded.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.

### File List

- 2026-08-08: Implemented representation and engineering operation authority handlers, structured companion editing, lock refresh, and focused LSP proof; moved story to review.
