---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 6.2
epic: 6
title: Add Structured Product Smoke
---

# Story 6.2: Add Structured Product Smoke

## Status

Done

## Story

As a maintainer,
I want structured product proof for M30,
so that visual acceptance does not rely on guessing or screenshots alone.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given M30 smoke runs, when it opens the sample in Theia, then representation library loaded, binding counts, anchor usage, composition bounds, route anchors, and chrome rules are verified.
2. Given accepted proof status is inspected, when binding diagnostics are counted, then missing-binding diagnostics are absent.
3. Given Gradle tasks are needed, when verification runs on Windows, then Gradle commands run sequentially.

## Tasks/Subtasks

- [x] Add product smoke script/harness for M30 sample. (AC: 1)
- [x] Add structured payload assertions for library, binding, anchors, composition, routes, and chrome. (AC: 1,2)
- [x] Document and enforce sequential Gradle verification where applicable. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Structured proof is authoritative; screenshot is a guard, not the semantic proof.
- Avoid brittle DOM text authority.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- RED: `node --test ide\theia-frontend\scripts\athena-m30-product-smoke-wiring.test.mjs` failed when the M30 smoke verifier did not pass `ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE`.
- RED: `yarn --cwd ide start:smoke:m30` opened the M30 workspace but failed because the shared Electron smoke runner still hard-coded the M29 Outline source path.
- RED: after source configurability, `yarn --cwd ide start:smoke:m30` failed on the M29-only Outline expected path `OperatorHMI1 > status`.
- RED: after Outline configurability, the smoke reached graph proof and failed because route/body intersection proof counted port-label hitboxes as component body crossings.
- RED: product proof payload originally hard-coded `athena-native-iec-v0`; smoke output showed the live product representation facts currently emit `athena-industrial-control-v0:*`, so proof was corrected to derive loaded library ids from actual representation ids.
- GREEN: `yarn --cwd ide start:smoke:m30` passed and emitted `ATHENA_M30_REPRESENTATION_PROOF` plus `ATHENA_GRAPH_WORKBENCH_PROOF`.
- 2026-07-21: Final closeout found an Outline smoke timing race that returned `paths: []` before the Outline model populated. Added an explicit wait in `AthenaProductContribution.revealOutlineForSource`, rebuilt Theia, and re-ran `yarn --cwd ide start:smoke:m30` successfully.

### Completion Notes

- Added `start:m30` and `start:smoke:m30` product scripts.
- Added M30 structured product smoke verifier for representation library ids, binding counts, anchor usage, composition bounds, route anchors, chrome transparency, active view, sheet selector persistence, and Outline nested-port proof.
- Updated the shared Electron smoke runner so M29 defaults remain intact while M30 can configure source path and expected Outline path.
- Added an explicit Outline-model population wait so the structured proof path does not snapshot an empty Outline tree during product smoke.
- Updated route/body proof collection to count actual component body hitboxes, not port-label hitboxes.
- Enforced sequential Gradle preparation through documented smoke error text and actual sequential `:ide:lsp:installDist` / `yarn --cwd ide build` runs.
- Mandatory polish/purge review found no temporary generated artifacts to remove and no cleanup-ledger entry was required.

## File List

- ide/package.json
- ide/theia-frontend/src/browser/athena-product-contribution.ts
- ide/theia-product/package.json
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m30-sample-project.js
- _bmad-output/implementation-artifacts/m30/6-2-add-structured-product-smoke.md
- ide/theia-frontend/scripts/athena-m30-product-smoke-wiring.test.mjs

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added structured M30 product smoke, configurable Outline proof, body-only route intersection proof, and M30 script wiring.
- 2026-07-21: Hardened the Outline smoke hook against async tree population races.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
