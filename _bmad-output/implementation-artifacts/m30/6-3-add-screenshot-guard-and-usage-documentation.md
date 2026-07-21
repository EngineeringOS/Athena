---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 6.3
epic: 6
title: Add Screenshot Guard And Usage Documentation
---

# Story 6.3: Add Screenshot Guard And Usage Documentation

## Status

Done

## Story

As a reviewer,
I want inspectable screenshot evidence and usage docs,
so that a human can verify the M30 demo path and visual target.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given screenshot guard runs, when the M30 demo opens, then it captures the demo sheet for review.
2. Given usage docs are read, when following steps, then a reviewer can open and evaluate the M30 sample.
3. Given docs describe visual references, when claims are inspected, then they state qualitative reference only and no QET/EPLAN parity.

## Tasks/Subtasks

- [x] Add screenshot guard for M30 demo sheet. (AC: 1)
- [x] Write M30 usage doc with exact open/test path. (AC: 2)
- [x] Document visual-reference boundaries. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Do not claim customer-demo credibility unless screenshot and structured proof both pass.
- Docs must remain accurate after implementation; final purge must remove stale claims.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Ran `node --test ide\theia-frontend\scripts\athena-m30-sample-project.test.mjs ide\theia-frontend\scripts\athena-m30-product-smoke-wiring.test.mjs ide\theia-frontend\scripts\athena-m30-representation-rendering.test.mjs ide\theia-frontend\scripts\athena-m30-transparent-chrome.test.mjs ide\theia-frontend\scripts\athena-m30-svg-bounds-regression.test.mjs`; 8 tests passed.
- 2026-07-21: Ran `yarn --cwd ide start:smoke:m30`; product smoke passed and captured `_bmad-output/implementation-artifacts/m30/screenshots/m30-graph-workbench-smoke.png`.
- 2026-07-21: Ran `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`; encoding audit passed.
- 2026-07-21: Final closeout re-ran the current smoke/doc wiring tests and refreshed the screenshot through the passing M30 product smoke path.

### Completion Notes

- Added the M30 screenshot guard to the product smoke wiring test and verifier path.
- Added reviewer usage documentation with exact sample open/test commands and visual-reference boundaries.
- Retained the generated screenshot as the required human-inspectable proof artifact and recorded it in the M30 cleanup ledger.

## File List

- `_bmad-output/implementation-artifacts/m30/6-3-add-screenshot-guard-and-usage-documentation.md`
- `_bmad-output/implementation-artifacts/m30/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m30/m30-demo-usage.md`
- `_bmad-output/implementation-artifacts/m30/screenshots/m30-graph-workbench-smoke.png`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `examples/m30/sample-project/README.md`
- `ide/theia-frontend/scripts/athena-m30-product-smoke-wiring.test.mjs`
- `ide/theia-product/scripts/verify-athena-m30-sample-project.js`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added screenshot guard, usage documentation, visual-reference boundaries, and cleanup-ledger record for retained screenshot proof.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
