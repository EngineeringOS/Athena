---
baseline_commit: 2c0f82e3ffea77ed3be5de5a9cd6d3aab38cacc5
---

# Story 6.2: Add Structured Interaction Product Smoke

## Status

Done

## Objective

Add product smoke based on structured Interaction proof payloads.

## Required Context

- `INTERACTION-CONTRACT.md` Structured Proof Payload Inventory.
- Existing M27/M28 product smoke patterns.

## Scope

- Open the M29 sample.
- Assert structured proof payloads before UI click assertions.
- Cover registry, action discovery, reveal, relationship preview/accept, entity creation
  preview/accept/reject, stale clearing, and legacy inventory.

## Acceptance Criteria

- Given M29 smoke runs, when sample opens, then `subject-registry`, `action-discovery`, reveal,
  relationship, entity creation, stale clearing, and legacy inventory proof payloads are asserted.
- Given UI click smoke exists, then it is secondary to structured payload assertions.
- Given proof checks run, then no check uses DOM text as semantic authority.

## Verification

- Product smoke command documented in usage notes.
- Sequential Gradle/task execution if Gradle is involved.

## Dev Agent Record

### Completion Notes

- Added M29 product smoke wiring for `examples/m29/sample-project` through `start:m29` and
  `start:smoke:m29`.
- Added product smoke script that opens the M29 sample, validates the full
  `m29.interaction.v1` structured proof payload inventory, and only then runs graph-workbench UI
  proof assertions.
- Added a focused Node test that verifies smoke wiring, proof assertion order, executable proof
  payload validation, and absence of DOM text/SVG geometry semantic authority.
- Published M29 proof usage notes with the LSP install and product smoke commands.
- Final polish/purge sweep found no temporary proof artifacts to remove; retained legacy paths
  remain recorded in the M29 cleanup ledger.

### Verification

- `node --test .\ide\theia-frontend\scripts\athena-m29-product-smoke-wiring.test.mjs`
- `yarn --cwd .\ide start:smoke:m29`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- Sprint duplicate-key audit: `No duplicate development_status keys. count=30`

### File List

- `docs/usages/m29-proof-usage.md`
- `ide/package.json`
- `ide/theia-frontend/scripts/athena-m29-product-smoke-wiring.test.mjs`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m29-sample-project.js`

### Change Log

- 2026-07-21: Added structured M29 Interaction product smoke and usage documentation.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
