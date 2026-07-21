# Story 1.2: Introduce Semantic Action Intent Primitive

## Status

Done

## Objective

Introduce `SemanticActionIntent` as the producer-neutral primitive below UI gestures.

## Required Context

- `INTERACTION-CONTRACT.md`, especially Conversion Rules.
- Existing `kernel/authoring-model` contracts: `AuthoringIntent`, `SemanticRelationshipIntent`,
  `CreateComponentIntent`, and legacy `ConnectPortsIntent`.

## Scope

- Implement `SemanticActionIntent` in the interaction model.
- Define conversion/mapping helpers or documented seams from action intent to interaction command.
- Do not replace existing authoring mutation contracts.

## Acceptance Criteria

- Given a reveal, preview, or mutate-capable action is created, when it becomes executable, then it
  carries action family, subject, target subjects, parameters, and provenance.
- Given a mutation-capable action is accepted, when mapping is requested, then relationship mutation
  maps toward `SemanticRelationshipIntent` and component insertion maps toward `CreateComponentIntent`.
- Given AI/API producers are represented in tests, when creating action intents, then no hover,
  click, DOM node, SVG node, or Theia widget fields are required.

## Verification

- Unit tests in interaction/authoring boundary.
- No new direct `ConnectPortsIntent` usage outside compatibility tests/adapters.

## Dev Agent Record

### Completion Notes

- Added authoring-model dependency on `:kernel:interaction-model` for the mapping seam.
- Added conversion from `SemanticActionIntent` to `SemanticRelationshipIntent`.
- Added conversion from `SemanticActionIntent` to `CreateComponentIntent`.
- Kept existing authoring contracts as the mutation boundary; no replacement authoring model was
  introduced.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test`

### File List

- `kernel/authoring-model/build.gradle.kts`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/InteractionAuthoringMapping.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/InteractionAuthoringMappingTest.kt`

### Change Log

- 2026-07-21: Implemented Story 1.2 Semantic Action Intent to authoring intent mapping.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
