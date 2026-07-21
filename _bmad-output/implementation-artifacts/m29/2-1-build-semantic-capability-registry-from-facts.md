# Story 2.1: Build Semantic Capability Registry From Facts

## Status

Done

## Objective

Build a semantic capability registry from compiled semantic model and projection facts.

## Required Context

- Architecture AD-3 and AD-4.
- `INTERACTION-CONTRACT.md` `InteractionSubjectKey` and `InteractionOccurrenceKey`.

## Scope

- Index components, ports, connections, routes, sheet occurrences, reference markers, diagnostics,
  and source ranges where available.
- Preserve presentation/standard metadata only as metadata.
- Bind registry cache to active source/projection revision.

## Acceptance Criteria

- Given a compiled sample has supported subject kinds, when registry build runs, then each subject is
  keyed by canonical subject identity and occurrence context.
- Given frontend adapter metadata is supplied, when registry keys are generated, then it never
  changes canonical identity.
- Given source or projection refresh occurs, when registry state is reused, then stale registry state
  is diagnosed or rebuilt.

## Verification

- Unit tests with compiled/projection fixtures.
- Structured proof payload for `subject-registry`.

## Dev Agent Record

### Completion Notes

- Added normalized registry input facts so compiler/projection adapters can feed Interaction subjects
  without making `interaction-model` depend on document/routing/presentation modules.
- Added `SemanticCapabilityRegistry` keyed by canonical subject identity plus source context.
- Preserved presentation, standard, and adapter metadata as metadata only.
- Added stale source revision diagnostics.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/SemanticCapabilityRegistry.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/SemanticCapabilityRegistryTest.kt`

### Change Log

- 2026-07-21: Implemented Story 2.1 Semantic Capability Registry.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
