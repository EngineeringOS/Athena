---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 3.4: Reject Invalid Edit Without Mutation

Status: done

## Story

As an engineering author, I want invalid canvas edits to fail closed, so the last accepted design stays trustworthy.

## Acceptance Criteria

1. Stale revision, missing target, invalid trace, wrong writable path, incompatible representation, invalid
   endpoint, invalid Part, staged parse/compile failure, and no-op are rejected without source, scene, or journal mutation.
2. Every rejection names exact subject, problem, and correction in plain engineering language.
3. Frontend sends typed intent only and contains no engineering validation or source mutation logic.
4. Active M44 proof records rejection evidence and preserves last accepted READY scene after refresh/reopen.

## Tasks / Subtasks

- [x] Add focused failing tests for stale, wrong-path, no-op, invalid target, invalid endpoint, and staged failure.
- [x] Verify common `EditOperationService` rejection gate and `SourceTransactionEngine` no-publish behavior.
- [x] Verify frontend authority boundary with typed-operation contract tests.
- [x] Run focused LSP/frontend tests and audits.

## Dev Notes

- Reuse `EditOperationService`, `RepresentationAndEngineeringOperationHandler`, `PlacementOperationHandler`,
  `SetStyleOperationHandler`, and `SourceTransactionEngine`; no alternate endpoint or compatibility path.
- Source Revision and accepted publication remain server authority. Rejections append no journal entry.
- Keep evidence under `_bmad-output/implementation-artifacts/m44/`.

## Dev Agent Record

### Debug Log References

- Story created from M44 Epic 3 after Story 3.3; existing transaction and rejection tests inspected.

### Completion Notes List

- Existing common gates cover stale revision, stale scene, target identity, writable path, and trace mismatch.
- Story-specific rejection proof added in Story 3.3 focused LSP suite; no mutation and no journal entry verified.

### File List

- `_bmad-output/implementation-artifacts/m44/3-4-reject-invalid-edit-without-mutation.md`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`

### Change Log

- 2026-08-08: Created and implemented through BMad story flow; moved to review.
