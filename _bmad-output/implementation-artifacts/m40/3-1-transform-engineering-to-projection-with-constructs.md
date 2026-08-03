---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.1: Transform Engineering To Projection With Constructs

Status: review

## Story

As a compiler maintainer,
I want Engineering Reality to lower to Projection Reality through one typed transformation,
so that views never corrupt engineering truth.

## Acceptance Criteria

1. The transformation uses the common typed interface (`RealityTransformation<InputReality,
   OutputReality>`), accepts Engineering Reality, and emits Projection Reality only.
2. Views, sheets, occurrences, regions, constructs, and reading order survive with source trace
   and engineering identity.
3. The same source and domain packages produce the same projection snapshot on every run.

## Tasks / Subtasks

- [x] Add `AuthoredProjectionTransformation : RealityTransformation<EngineeringDocument,
  ProjectionDocument>` wrapping the authored view compiler.
- [x] Wire source-trace and determinism tests.
- [x] Run focused tests; update story + sprint.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 3.1]
- [Source: PRD FR-13, FR-14; ARCHITECTURE-SPINE.md AD-19]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added typed Engineering->Projection transformation for authored views.
- 2026-08-02: Compiler suite green.

### Completion Notes List

- `AuthoredProjectionTransformation` implements `RealityTransformation<EngineeringDocument,
  ProjectionDocument>`: accepts Engineering Reality, emits Projection Reality only, fails with
  plain diagnostics on authored-view errors.
- Determinism + identity trace verified by double-transform test.

### File List

- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionTransformationTest.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 3.1 and PRD FR-13/FR-14.
- 2026-08-02: Implemented typed authored projection transformation; compiler green; review.
