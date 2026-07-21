# Story 5.1: Discover Insert Semantic Entity Action

## Status

Done

## Objective

Expose one component insertion proof as a semantic entity creation action.

## Required Context

- Architecture AD-8.
- Existing `CreateComponentIntent`.
- `INTERACTION-CONTRACT.md` component insertion conversion rule.

## Scope

- Discover an insert semantic entity action from a valid system, graph, or empty-sheet context.
- Use one known component concept for the proof.
- Treat semantic parent/context as truth, not screen coordinates.

## Acceptance Criteria

- Given a valid context is active, when action discovery runs, then an insert semantic entity action
  is available.
- Given the action is inspected, then it identifies semantic parent/context and component concept.
- Given screen coordinate data exists, then it is adapter metadata only and not creation authority.

## Verification

- Interaction action discovery tests.
- Structured proof payload for entity creation action discovery.

## Dev Agent Record

### Completion Notes

- Extended `SemanticCapability` with platform-owned parameters so discovered actions can carry
  semantic creation context without frontend-specific authority.
- Updated action discovery to propagate capability parameters into `SemanticActionIntent`.
- Added an entity creation action discovery proof for `system:FactoryLine` creating
  `electrical.motor.ac`, with screen coordinates retained only as adapter metadata.
- Final polish/purge sweep found no production coordinate authority leak and no story-local stale
  code or generated artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionModels.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionActionDiscovery.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/InteractionActionDiscoveryTest.kt`

### Change Log

- 2026-07-21: Added semantic entity creation action discovery parameters and proof coverage.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
