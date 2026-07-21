# Story 1.3: Add Lifecycle, Provenance, And Diagnostic Contract Tests

## Status

Done

## Objective

Lock the lifecycle, provenance, and diagnostic contracts with tests.

## Required Context

- `INTERACTION-CONTRACT.md` lifecycle, provenance, and minimum diagnostic codes.

## Scope

- Add legal lifecycle transition tests.
- Add illegal transition diagnostics.
- Add provenance serialization/comparison tests.
- Add diagnostic envelope tests for the minimum stable code set.

## Acceptance Criteria

- Given lifecycle tests run, when legal transitions are exercised, then the runtime-owned state
  machine accepts them.
- Given an illegal transition is attempted, when validation runs, then
  `interaction.command.invalid-state` is produced.
- Given diagnostic serialization runs, when each minimum code is emitted, then severity, subject,
  command id, range, and retryability fields remain stable and transport-safe.

## Verification

- Sequential Gradle tests for interaction model/runtime contract.

## Dev Agent Record

### Completion Notes

- Added `InteractionLifecycle` as the runtime-owned lifecycle transition guard.
- Covered legal command path, terminal stale/cancelled/rejected/blocked transitions, and illegal
  transition diagnostics.
- Added stable diagnostic code coverage through interaction model tests.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Retrospective Notes

- Sprint tracking must be treated as an artifact under test, not passive metadata. Story 1.3 was
  recorded twice in `sprint-status.yaml` with conflicting states (`done` and `ready-for-dev`), which
  can make downstream story discovery resume already-completed work.
- Prevention: after marking any story done, verify `development_status` has exactly one key for the
  story and that the status matches the story file before moving to the next story.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionLifecycle.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/InteractionLifecycleContractTest.kt`

### Change Log

- 2026-07-21: Implemented Story 1.3 lifecycle and diagnostic contract tests.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
