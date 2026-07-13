---
baseline_commit: 72b498f
---

# Story 2.2: Keep Compatibility Judgement Out Of The Port Contract

Status: done

## Story

As an architecture owner,  
I want Athena to keep rich compatibility logic outside the port contract,  
so that M14 does not collapse into a second rule-engine milestone.

## FR Traceability

- FR-4: keep rich compatibility logic outside the port contract
- NFR-5: semantic ports remain kernel knowledge contracts and not renderer-local affordances
- NFR-7: M14 output remains read-only resolved knowledge consumed by later layers

## Acceptance Criteria

1. Given the connection-model contract is reviewed, when M9 responsibilities are compared with M14 responsibilities, then semantic port contracts define type and role only.
2. Given semantic port knowledge is compared with later reasoning layers, when compatibility or sufficiency is reviewed, then richer compatibility or sufficiency evaluation remains downstream M9 logic.

## Tasks / Subtasks

- [x] Tighten the semantic-port contract comments and docs. (AC: 1, 2)
  - [x] Make the code comments explicit that compatibility and sufficiency judgement do not belong in `connection-model`.
  - [x] Make the module docs explicit that richer judgement belongs in the M9 ladder.
- [x] Add a focused regression test for the boundary. (AC: 1, 2)
  - [x] Prove the semantic-port contract stays declarative.
  - [x] Prove judgement concepts are represented by downstream M9 models such as capability facts and constraint evaluations.
- [x] Update milestone tracking and verify sequentially on Java 25. (AC: 1, 2)
  - [x] Update the M14 implementation-artifact tracking files.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Story Completion Status

- Status: done
- Completion note: tightened `:kernel:connection-model` boundaries, added a regression test against M9 judgement ownership, and verified with `java25; .\gradlew.bat :kernel:connection-model:test --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 Story `2.2` connection-model boundary tightening
- Sequential Java 25 verification
