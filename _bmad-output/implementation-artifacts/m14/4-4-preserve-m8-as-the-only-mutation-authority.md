---
baseline_commit: 72b498f
---

# Story 4.4: Preserve M8 As The Only Mutation Authority

Status: done

## Story

As an architecture owner,  
I want M14 to preserve one mutation authority across all authoring surfaces,  
so that component knowledge does not accidentally create a second write path.

## FR Traceability

- FR-10: Athena preserves M8 semantic mutation as the only write authority

## Acceptance Criteria

1. Given M14 introduces read-only component knowledge resolution, when its product and runtime boundaries are reviewed, then authored changes still converge through the M8 semantic command and mutation path before resolution runs again.
2. Given future graph, form, template, AI, API, or DSL authoring is considered, when product direction is inspected, then M14 explicitly preserves the one-authority write rule rather than opening a new path.

## Tasks / Subtasks

- [x] Add a focused runtime regression that proves dirty source preview does not become canonical component knowledge authority. (AC: 1)
  - [x] Compile a dirty source variant that changes resolved component knowledge.
  - [x] Verify preview compilation can differ from canonical component knowledge.
  - [x] Verify runtime-inspected component knowledge stays anchored to the active canonical project state.
  - [x] Verify command history remains empty when no M8 mutation authority is invoked.
- [x] Keep the milestone record explicit about read-only M14 ownership. (AC: 1, 2)
  - [x] Record that M14 adds read-only resolution and downstream evidence only.
  - [x] Do not introduce a second write path.
- [x] Finish sequential runtime verification on Windows with Java 25. (AC: 1)

## Story Completion Status

- Status: done
- Completion note: the regression test in `AthenaSourceMutationRuntimeServiceTest` proves preview-only dirty source compilation cannot replace canonical component knowledge or append command history, and it was verified with `java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSourceMutationRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 Story `4.4` mutation authority preservation regression
- Sequential Java 25 runtime verification passed
