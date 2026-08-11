---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 3.3: Connect Ports Through Validated Commands

Status: done

## Story

As an engineer, I can connect compatible Ports through an explicit compiler-advertised option, so
engineering relationships are authored without renderer inference.

## Acceptance Criteria

1. Revision-bound `athena/diagramConnectOptions` exposes relationship definition, participant roles,
   direction, and one writable project source target. No option means command unavailable.
2. `ConnectPorts` validates scene/revision, source preconditions, option identity, endpoint existence,
   direction (`out` to `in` or bidirectional), and semantic compatibility before one project-file
   `WorkspaceEdit`.
3. Invalid direction, incompatible flow, stale revision, stale source digest, ambiguous/missing option,
   duplicate command conflict, and failed proposed compilation reject with deterministic diagnostics;
   source and accepted READY scene remain unchanged.
4. Accepted relationship persists through normal source synchronization and atomic scene recompile with
   stable route/trace identity. Frontend never infers relationship kind, role, direction, or file.
5. Focused tests, frontend build, root Gradle tests, encoding audit, and source-set hygiene pass sequentially.

## Tasks / Subtasks

- [x] Implement revision-bound compiler/runtime connection-option discovery.
- [x] Extend `DiagramAuthoringService` for validated `ConnectPorts` and one project-file WorkspaceEdit.
- [x] Wire LSP options request and command dispatch; keep generated contracts authoritative.
- [x] Add typed frontend options/command bridge.
- [x] Preserve fail-closed invalid-direction, stale, duplicate, option, source, and proposed-compilation paths.
- [x] Run required sequential verification and complete story records.

## Dev Notes

- Engineering source owns relationship meaning. Port direction/flow comes from compiled `EngineeringDocument`.
- Use existing `AthenaCompiler`, relation grammar, and generated command schemas. Do not add renderer-side
  semantic inference or a second relationship model.
- Accepted edit targets owning project `.athena`; M43 commands remain single-file and undoable.
- Human diagnostics name exact Ports, relationship, problem, and correction.

## References

- `_bmad-output/planning-artifacts/m43/epics.md` Story 3-3
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md` FR-12, NFR-2, NFR-4
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md` section 6
- `_bmad-output/implementation-artifacts/m43/3-2-apply-move-and-lock-commands.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

Frontend tests, focused LSP tests, root Gradle tests, encoding audit, and source-set hygiene audit pass.

### Completion Notes List

- Runtime advertises revision-bound options only from compiled Port direction/flow facts.
- `ConnectPorts` writes one project-source relation edit only after CAS, direction, option, duplicate, and proposed-compilation validation.
- Exact invalid direction diagnostic uses `relationship.direction.illegal`; no source mutation occurs on rejection.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramAuthoringService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`

### Change Log

- 2026-08-06: Created through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-06: Implemented compiler-advertised connection options and validated project-source
  relationship commands; final command and regression gates passed; status `done`.
