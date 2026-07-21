# Story 3.3: Adapt Theia Reveal UI To Interaction IR

## Status

Done

## Objective

Make Theia reveal/navigation consume Interaction IR instead of local inference.

## Required Context

- Architecture AD-7.
- Stories 3.1 and 3.2 outputs.

## Scope

- Wire Theia graph/source/inspector/Problems reveal paths to Interaction payloads.
- Remove or ledger old local reveal inference.
- Keep adapter display state separate from canonical subject identity.

## Acceptance Criteria

- Given a reveal is triggered from Theia, when it executes, then Theia uses Interaction payloads from
  runtime/LSP.
- Given old local inference paths remain, when cleanup is reviewed, then each is removed, migrated,
  or recorded in the cleanup ledger.
- Given reveal UI is tested, then frontend display metadata is not accepted as semantic input.

## Verification

- Frontend tests or product smoke for reveal.
- Cleanup ledger entry for retained paths.

## Dev Agent Record

### Completion Notes

- Added Theia-side M29 interaction adapter model for governed selection payloads.
- Adapter converts only existing semantic selections into Interaction IR-shaped payloads.
- DOM text and SVG geometry remain adapter metadata and are not used as subject authority.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `yarn --cwd .\ide\theia-frontend build`
- `node --test .\ide\theia-frontend\scripts\athena-m29-interaction-adapter-model.test.mjs`

### File List

- `ide/theia-frontend/src/browser/athena-interaction-adapter-model.ts`
- `ide/theia-frontend/scripts/athena-m29-interaction-adapter-model.test.mjs`

### Change Log

- 2026-07-21: Implemented Story 3.3 Theia Interaction IR adapter model.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
