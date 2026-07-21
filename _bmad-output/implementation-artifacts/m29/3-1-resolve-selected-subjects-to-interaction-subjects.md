# Story 3.1: Resolve Selected Subjects To Interaction Subjects

## Status

Done

## Objective

Resolve source/graph/problem/inspector selections to governed Interaction subjects.

## Required Context

- Architecture AD-3 and AD-7.
- M27/M28 graph selection behavior.

## Scope

- Adapt selection resolution to request Interaction subjects from runtime/LSP payloads.
- Preserve frontend metadata only as adapter metadata.
- Return unresolved diagnostics for elements without governed semantic payloads.

## Acceptance Criteria

- Given a component, port, connection, route, reference marker, or diagnostic is selected, when
  resolution runs, then it returns an Interaction subject keyed by canonical identity.
- Given a frontend element lacks governed semantic payload, when resolution runs, then
  `interaction.subject.unresolved` is returned.
- Given selection is tested, then no DOM text, SVG geometry, or CSS class is used as authority.

## Verification

- Frontend/runtime structured tests for subject resolution.
- Regression check for M27 graph density and M28 semantic selection.

## Dev Agent Record

### Completion Notes

- Added governed selection payload and subject resolver.
- Resolver accepts only explicit `InteractionSubjectKey` payloads and returns unresolved diagnostics
  for DOM/SVG/text-only selections.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionSubjectResolver.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/InteractionSubjectResolutionTest.kt`

### Change Log

- 2026-07-21: Implemented Story 3.1 subject resolution.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
