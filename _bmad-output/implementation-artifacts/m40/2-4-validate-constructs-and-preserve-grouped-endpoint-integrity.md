---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.4: Validate Constructs And Preserve Grouped Endpoint Integrity

Status: review

## Story

As an engineer,
I want broken constructs to fail clearly,
so that invalid projection never reaches Spatial.

## Acceptance Criteria

1. Empty constructs, duplicate construct identities, constructs without source trace, and invalid
   nesting (e.g., a rung containing a rail) fail with named diagnostics before Spatial.
2. A group referencing a missing occurrence, a coil without its device, a strip referencing a
   missing terminal, or a rung without an assigned occurrence fails with a plain diagnostic.
3. Engineering connections and their source traces are identical with and without projection
   declarations.

## Tasks / Subtasks

- [x] Empty / duplicate / unresolved-member / before-sheet diagnostics (delivered in 2-3).
- [x] Add invalid-nesting validation (construct member referencing another construct).
- [x] Add grouped endpoint integrity test (connections identical with/without constructs).
- [x] Run focused tests; update story + sprint.

## Dev Notes

- Member resolution is device-occurrence scoped in M40; port/terminal-level membership checks
  (coil-without-device-port, strip-missing-terminal) land with representation binding in M42+.
  This is recorded, not guessed.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 2.4]
- [Source: PRD FR-9, FR-11]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added invalid-nesting validation + grouped endpoint integrity test.
- 2026-08-02: Compiler suite green (370 tests).

### Completion Notes List

- Invalid nesting: a construct whose member references another construct's identity fails with a
  plain diagnostic before Spatial.
- Grouped endpoint integrity: engineering connections identical with and without projection
  declarations (tested).
- Empty/duplicate/unresolved-member/before-sheet diagnostics delivered in Story 2.3 reused here.
- Recorded: port/terminal-level membership checks (coil-without-device-port,
  strip-missing-terminal) land with representation binding in M42+, not guessed.

### File List

- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionConstructCompilationTest.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 2.4 and PRD FR-9/FR-11.
- 2026-08-02: Implemented nesting validation + grouped endpoint integrity test; compiler green;
  marked review.
