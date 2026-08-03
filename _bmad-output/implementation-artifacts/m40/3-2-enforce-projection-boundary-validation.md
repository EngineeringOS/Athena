---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.2: Enforce Projection Boundary Validation

Status: review

## Story

As a maintainer,
I want projection snapshots validated before downstream consumption,
so that spatial or presentation facts never leak into Projection.

## Acceptance Criteria

1. A projection snapshot containing coordinate, anchor, lane, route, stroke, label, or
   paint-order facts fails validation with a named diagnostic.
2. Diagnostics name the exact subject, problem, and correction in plain engineering language.

## Tasks / Subtasks

- [x] Add `ProjectionBoundaryValidator` rejecting spatial/presentation facts in snapshots.
- [x] Wire into authored projection compilation; add tests.
- [x] Run focused tests; update story + sprint.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 3.2]
- [Source: PRD FR-15; NFR-9; ARCHITECTURE-SPINE.md AD-19]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added ProjectionBoundaryValidator + wiring into authored compilation.
- 2026-08-02: Compiler suite green.

### Completion Notes List

- `ProjectionBoundaryValidator` rejects layout-box zones (spatial leakage) with plain
  diagnostics naming sheet + problem + correction; logical zones accepted.
- Wired into `AuthoredProjectionViewCompiler` so boundary violations fail closed before
  downstream consumption.

### File List

- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionBoundaryValidator.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionTransformationTest.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 3.2 and PRD FR-15/NFR-9.
- 2026-08-02: Implemented boundary validation; compiler green; review.
