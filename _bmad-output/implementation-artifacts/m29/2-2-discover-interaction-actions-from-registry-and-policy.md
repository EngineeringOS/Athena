# Story 2.2: Discover Interaction Actions From Registry And Policy

## Status

Done

## Objective

Discover available interaction actions from semantic registry facts and capability policy.

## Required Context

- Architecture AD-4.
- `INTERACTION-CONTRACT.md` `SemanticCapability` and `SemanticActionIntent`.

## Scope

- Add Interaction Compiler v0 action discovery.
- Support reveal, inspect/selection, preview, and mutation command families where subjects qualify.
- Emit structured diagnostics for unsupported actions.

## Acceptance Criteria

- Given a registered subject, when action discovery runs, then actions reflect subject kind,
  projection context, source context, and policy.
- Given an unsupported action is requested, when discovery runs, then
  `interaction.action.unsupported` is returned.
- Given action discovery runs, then source and projection facts are not mutated.

## Verification

- Unit tests for supported and unsupported subject/action combinations.
- Structured proof payload for `action-discovery`.

## Dev Agent Record

### Completion Notes

- Added `InteractionActionDiscovery` as the first Interaction Compiler v0-style action discovery
  seam.
- Enabled action intent creation from registered capabilities.
- Added structured diagnostics for missing subjects and disabled/unsupported capabilities.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionActionDiscovery.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/InteractionActionDiscoveryTest.kt`

### Change Log

- 2026-07-21: Implemented Story 2.2 action discovery from capability registry.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
