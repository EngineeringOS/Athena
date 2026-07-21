# Story 3.2: Reveal Subjects Across Source, Graph, Inspector, And Problems

## Status

Done

## Objective

Reveal one canonical Interaction subject across source, graph, inspector, and Problems where targets
exist.

## Required Context

- `INTERACTION-CONTRACT.md` reveal request/result shapes.
- Existing LSP/source range and graph occurrence facts.

## Scope

- Implement reveal request/result handling.
- Support partial success with diagnostics.
- Keep reveal stable across sheet/projection mode changes when subject still exists.

## Acceptance Criteria

- Given a subject has source range, graph occurrence, inspector payload, or problem diagnostic, when
  reveal is requested, then the result lists available targets.
- Given a target is missing, when reveal runs, then `interaction.reveal.missing-target` is returned
  without guessed navigation.
- Given sheet or projection mode changes, when the canonical subject still exists, then reveal uses
  refreshed occurrence context.

## Verification

- Structured proof payload for `reveal-source-graph-inspector-problems`.
- LSP/runtime tests for partial reveal results.

## Dev Agent Record

### Completion Notes

- Added `InteractionRevealService` for source, graph, inspector, and Problems reveal target
  derivation.
- Added missing-target diagnostics without guessed navigation.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionRevealService.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/InteractionRevealServiceTest.kt`

### Change Log

- 2026-07-21: Implemented Story 3.2 semantic reveal service.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
