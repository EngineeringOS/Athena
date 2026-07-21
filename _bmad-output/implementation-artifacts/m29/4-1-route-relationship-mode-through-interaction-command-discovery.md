# Story 4.1: Route Relationship Mode Through Interaction Command Discovery

## Status

Done

## Objective

Route M28 relationship mutation through Interaction command discovery.

## Required Context

- Existing `SemanticRelationshipIntent` and `ElectricalConnectionRelationship`.
- `INTERACTION-CONTRACT.md` conversion rules.

## Scope

- Discover relationship mutation command from port subjects through Interaction IR.
- Keep the visible command domain-labeled if useful, but map underlying mutation to
  `SemanticRelationshipIntent(ElectricalConnectionRelationship)`.
- Reject non-port subjects for M28 electrical relationship mutation.

## Acceptance Criteria

- Given relationship mode starts from a valid port subject, when actions are discovered, then the
  relationship command comes from Interaction IR.
- Given a non-port subject is selected, when discovery runs, then `interaction.action.unsupported`
  or a more specific structured diagnostic is returned.
- Given command conversion runs, then the target mutation contract is `SemanticRelationshipIntent`.

## Verification

- Unit tests for action discovery and conversion.
- No new direct `ConnectPortsIntent` frontend/runtime call sites.

## Dev Agent Record

### Completion Notes

- Added relationship Interaction command discovery in the Theia relationship model.
- Command targets `ElectricalConnectionRelationship` through Interaction-shaped action intent
  payloads.
- No source edit or legacy `connect-ports` command is produced by this story.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `yarn --cwd .\ide\theia-frontend build`
- `node --test .\ide\theia-frontend\scripts\athena-m28-relationship-authoring-model.test.mjs`

### File List

- `ide/theia-frontend/src/browser/athena-relationship-authoring-model.ts`
- `ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs`

### Change Log

- 2026-07-21: Implemented Story 4.1 relationship Interaction command discovery.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
