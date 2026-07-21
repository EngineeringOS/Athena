# Story 4.3: Clear Previews And Ledger Legacy Connect-Ports Paths

## Status

Done

## Objective

Clear transient relationship preview state correctly and inventory legacy `connect-ports` paths.

## Required Context

- `INTERACTION-CONTRACT.md` Preview Ownership and Connect-Ports Inventory Rule.
- Existing Theia relationship preview/model code.

## Scope

- Move preview lifecycle ownership to Interaction Runtime.
- Ensure source impact comes from backend runtime/source-edit logic.
- Create M29 cleanup ledger entries for every `connect-ports` or `ConnectPortsIntent` path.

## Acceptance Criteria

- Given preview is active, when cancel, source reload, projection refresh, active source change, or
  accepted mutation occurs, then preview state becomes cleared or stale and cannot persist as truth.
- Given current legacy paths are inventoried, when cleanup completes, then each is classified as
  removed, migrated-to-interaction-ir, compatibility-adapter, or retained-with-owner-target-milestone.
- Given new M29 story code is reviewed, then it does not call legacy `ConnectPortsIntent` directly.

## Verification

- Structured proof payloads for `relationship-preview`, `preview-stale-clearing`, and
  `legacy-connect-ports-inventory`.
- Cleanup ledger updated.

## Dev Agent Record

### Completion Notes

- Changed relationship preview source impact so Theia no longer synthesizes authored `connect`
  statements; preview source impact is marked as backend runtime/source-edit owned.
- Added stale preview invalidation for source reload, projection refresh, active source change, and
  accepted mutation. Cancel still clears the transient preview.
- Added the M29 cleanup ledger with `legacy-connect-ports-inventory` proof payload and classified
  retained legacy `connect-ports` / `ConnectPortsIntent` paths by owner, reason, and target
  milestone.
- Final polish/purge sweep found no story-local generated artifacts or stale direct
  `ConnectPortsIntent` calls introduced by M29 relationship story code.

### Verification

- `yarn --cwd .\ide\theia-frontend build`
- `node --test .\ide\theia-frontend\scripts\athena-m28-relationship-authoring-model.test.mjs`
- `rg "ConnectPortsIntent|connect-ports" kernel/interaction-model kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/InteractionAuthoringMapping.kt ide/theia-frontend/src/browser/athena-interaction-adapter-model.ts ide/theia-frontend/src/browser/athena-relationship-authoring-model.ts ide/theia-frontend/scripts/athena-m29-interaction-adapter-model.test.mjs ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs`
- `rg "statement: \`connect|statement: 'connect|connect \$\{|authoredPortPath" ide/theia-frontend/src/browser/athena-relationship-authoring-model.ts ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs`

### File List

- `ide/theia-frontend/src/browser/athena-relationship-authoring-model.ts`
- `ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs`
- `_bmad-output/implementation-artifacts/m29/cleanup-ledger.md`

### Change Log

- 2026-07-21: Cleared frontend-authored relationship source impact and added legacy connect-ports cleanup ledger.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
