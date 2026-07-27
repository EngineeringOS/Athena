---
story_id: 7.2
story_key: 7-2-purge-toy-rendering-and-workbench-debt
epic: 7
status: review
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-24'
---

# Story 7.2: Purge Toy Rendering And Workbench Debt

## Status

Review

## Story

As a maintainer, I want stale renderer and Workbench paths removed or ledgered.

## Acceptance Criteria

- Cleanup removes or ledgers generic fallback boxes, stale Graph View modes, dead authoring panels,
  duplicate-label paths, and hard-coded viewBox paths.
- Ledger entries include owner, reason, target milestone, and verification.
- No story can move done with open unowned debt introduced by that story.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add a RED cleanup ledger contract test for owned debt metadata.
- [x] Classify transient M33 cleanup entries as done or open.
- [x] Move unresolved architecture debt to explicit M34/Post-M33 owner points.
- [x] Preserve Cabinet-only product focus and hidden compatibility view ledger notes.
- [x] Run focused cleanup, product smoke contract, visual checklist, rebuilt IDE, Electron smoke, and encoding audit.
- [x] Complete AC-to-evidence mapping, three-layer review, and polish/purge notes.

## Dev Notes

- Bind to M33 AD-7, AD-8, AD-9, AD-10.
- Do not hide defects in compatibility code without ledger ownership.

## Testing

- Repository scans for known stale patterns.
- Product smoke confirms cleanup did not break demo.

## Evidence Plan

- Cleanup ledger plus smoke output.

## Polish And Purge

This story is the milestone-wide purge gate.

## Dev Agent Record

### Implementation Plan

- Add a documentation contract test that fails when cleanup debt is transient or unowned.
- Update the M33 cleanup ledger to close proven Cabinet demo debts and keep unresolved architecture debt explicit.
- Verify the Cabinet product smoke still passes after rebuild.

### Debug Log

- RED: `node --test scripts\athena-m33-cleanup-ledger-contract.test.mjs` failed because ledger rows still used `in-progress` and open rows targeted completed M33 stories.
- GREEN: updated `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md` so rows are either `done` or `open`, and open rows target M34/Post-M33 owner points.

### Completion Notes

- Closed proven active Cabinet cleanup items: generic fallback boxes, hard-coded oversized active viewBox, duplicate/off-screen demo elements, Workbench sheet pixel fallback, and composition-owned reference marker visibility.
- Kept real architecture debt open with owner, reason, target, and verification: legacy SVG path authority, M30 primitive overlap, legacy box renderer compatibility, reference navigation compatibility, package XML hardening, Graphic Primitive IR convergence, and Cabinet visual polish.
- Cabinet remains the only M33 product surface; Documentation/Schematic/Wiring are ledgered as hidden compatibility, not product UX.

### Three-Layer Adversarial Review

- Blind Hunter: no open ledger row is unowned, transient, or targeted at a completed M33 story.
- Edge Case Hunter: hidden compatibility views are not deleted silently; they are documented as non-product compatibility.
- Acceptance Auditor: cleanup ledger contract, product smoke contract, visual checklist test, rebuilt IDE, Electron smoke, and encoding audit cover the story ACs.

### AC-To-Evidence

- AC1: `cleanup-ledger.md` closes or ledgers fallback boxes, hidden Graph View modes, Create Device state, duplicate/off-screen elements, and viewBox debt.
- AC2: `athena-m33-cleanup-ledger-contract.test.mjs` asserts owner, reason, target, status, and verification for every row.
- AC3: the same test rejects transient/unowned open debt and requires future owner points for open rows.
- AC4: this Dev Agent Record records AC evidence plus polish/purge notes before review.

### Verification

- `node --test scripts\athena-m33-cleanup-ledger-contract.test.mjs scripts\athena-m33-product-smoke-contract.test.mjs scripts\athena-m33-visual-review-checklist.test.mjs` passed: 9/9.
- `yarn build` passed from `ide`.
- `yarn start:smoke:m33` passed from `ide/theia-product`.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

## File List

- `_bmad-output/implementation-artifacts/m33/7-2-purge-toy-rendering-and-workbench-debt.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m33-cleanup-ledger-contract.test.mjs`

## Change Log

- 2026-07-24: Added cleanup ledger contract test, reclassified M33 cleanup debt, and verified Cabinet smoke after rebuild.
