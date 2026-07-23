---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 7.2
epic: 7
title: Stabilize Sheet Navigation Across Graph View Modes
---

# Story 7.2: Stabilize Sheet Navigation Across Graph View Modes

## Status

Review

## Story

As a controls engineer,
I want sheet navigation to stay predictable while switching Graph View modes,
so that the sheet dropdown does not appear and disappear unpredictably.

## Required Context

- Story 7.1 taxonomy contract: `_bmad-output/implementation-artifacts/m32/7-1-define-graph-view-taxonomy-and-toolbar-contract.md`
- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`

## Acceptance Criteria

1. Given a runtime payload contains multiple governed sheets, when the active projection mode
   changes, then sheet navigation remains visible and uses the current runtime sheet selector or
   the last valid selector only as explicit compatibility fallback.
2. Given a runtime payload contains only one sheet or no governed sheet facts, when Graph View
   renders, then the UI intentionally hides or disables sheet navigation with structured proof, not
   accidental projection state.
3. Given Electron smoke switches available modes, when the M32 sample is opened, then E2E proof
   records sheet selector visibility, option count, selected sheet, and no blink/disappear
   regression.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Use CodeGraph on `resolveVisibleAthenaGraphSheetViewSelector`,
  `renderSheetViewSelector`, and Graph View smoke proof before editing. (AC: 1..3)
- [x] Add RED model tests for current multi-sheet selector stability across mode changes,
  including non-M31 three-sheet/package-style roles. (AC: 1)
- [x] Add RED model/E2E proof for intentional single-sheet/no-sheet behavior. (AC: 2)
- [x] Implement the smallest model/widget change that satisfies selector stability without
  changing view-mode semantics. (AC: 1,2)
- [x] Extend M32 Electron smoke to switch available modes and assert sheet navigation state after
  each switch. (AC: 3)
- [x] Update cleanup ledger for removed M31-only selector assumptions or retained compatibility.
  (AC: 4)
- [x] Run focused frontend tests, Theia product build, M32 Electron smoke, and encoding audit if
  docs changed. (AC: 1..4)
- [x] Complete mandatory polish/purge review and AC-to-evidence mapping. (AC: 4)

## Dev Notes

- Do not rename UI controls in this story except to comply with Story 7.1.
- Do not change package rendering authority in this story; Story 7.4 owns package-backed rendering.
- The bug to prevent is a selector that passes DOM existence tests while disappearing under mode
  changes.
- Keep the distinction clear: View mode changes what projection surface is rendered; Sheet
  navigation moves within a governed document/sheet set.

## Testing Requirements

- TDD required. Current behavior should fail first for a non-M31 multi-sheet selector.
- Focused commands:
  - `yarn build` and relevant `node --test scripts\*.test.mjs` in `ide/theia-frontend`
  - `yarn build` and `yarn start:smoke:m32` in `ide/theia-product`
- Rebuild product bundles before manual IDE validation.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- CodeGraph inspection covered `resolveVisibleAthenaGraphSheetViewSelector`, `renderSheetViewSelector`, and the Electron/M32 smoke proof path before edits.
- RED proof: `node --test scripts\athena-graph-workbench-model.test.mjs` failed on the new non-M31 package selector test because the old M31-only role gate returned `undefined`.
- GREEN proof: `node --test scripts\athena-graph-workbench-model.test.mjs` passed 31/31 after replacing the M31-only gate with a generic multi-sheet selector contract.
- Focused frontend proof: `node --test scripts\athena-graph-workbench-model.test.mjs scripts\athena-m32-graph-view-taxonomy.test.mjs scripts\athena-ide-density-contract.test.mjs scripts\athena-m32-create-entity-panel.test.mjs` passed 38/38.
- Build proof: `yarn build` in `ide/theia-frontend` passed; `yarn build` in `ide/theia-product` passed.
- E2E proof: `yarn start:smoke:m32` passed and recorded `activeViewId=documentation`, `sheetViewOptionCount=2`, selected sheet `documentation/sheet/01-control`, and selector persistence across `cabinet`, `wiring`, and `schematic`.

### Completion Notes List

- Removed the hard-coded M31 two-sheet role gate from visible sheet selector resolution.
- Preserved intentional no-selector behavior for single-sheet and empty selector states.
- Extended M32 Electron smoke to start on the professional sheet view and assert selector visibility, option count, selected sheet, and post-switch persistence.
- Updated smoke label expectations to match Story 7.1 product taxonomy.
- AC-to-evidence: AC1 covered by model tests and smoke persistence states; AC2 covered by single/empty selector test; AC3 covered by `yarn start:smoke:m32`; AC4 covered by cleanup ledger, focused tests, product build, smoke, and encoding audit.

### File List

- _bmad-output/implementation-artifacts/m32/7-2-stabilize-sheet-navigation-across-graph-view-modes.md
- _bmad-output/implementation-artifacts/m32/cleanup-ledger.md
- _bmad-output/implementation-artifacts/m32/sprint-status.yaml
- ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs
- ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m32-sample-project.js

### Change Log

- 2026-07-22: Replaced M31-only sheet selector visibility gate, added non-M31 multi-sheet selector tests, and extended M32 smoke selector persistence proof.

## Mandatory Final Polish/Purge Gate

- Review model/widget/smoke changes, generated Theia output, and stale M31-only assertions.
- Remove or ledger retained compatibility.
- Record AC-to-evidence before moving the story to `review`.
