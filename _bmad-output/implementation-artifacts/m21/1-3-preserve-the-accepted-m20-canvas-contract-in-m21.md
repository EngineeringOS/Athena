---
baseline_commit: 0b8e6fd5659595da0d779309ed745e8740faf685
---

# Story 1.3: Preserve the accepted M20 canvas contract in M21

Status: done

## Story

As an engineer,
I want the M21 sheet to preserve the accepted M20 canvas behavior,
so that layout intelligence does not regress the IDE surface that was already accepted.

## Acceptance Criteria

1. Given the M21 graph workbench surface, when the sheet is opened and inspected, then the stage grid remains the coordinate surface.
2. Given the M21 graph workbench surface, when the sheet is opened and inspected, then sheet and component bodies do not hide the grid.
3. Given the M21 graph workbench surface, when `Cabinet Main` details are inspected, then they remain in the top information popover only.
4. Given the M21 graph workbench surface, when top and bottom controls are inspected, then they remain transparent canvas overlays.
5. Given Story 1.3 scope, when implementation is reviewed, then it does not implement layout intent, layout engine, route facts, label avoidance, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection.

## Tasks / Subtasks

- [x] Lock the accepted canvas contract in test coverage (AC: 1, 2, 3, 4, 5)
  - [x] Add or extend a focused Story 1.3 canvas-contract test script under `ide/theia-frontend/scripts/`.
  - [x] Verify the graph workbench source still exposes the accepted stage, viewport, sheet, canvas, info button, info popover, bottom dock, zoom dock, and sheet frame contracts.
  - [x] Verify the stylesheet keeps the stage grid-backed, the sheet transparent, the overlays transparent, the bottom dock transparent, the zoom dock transparent, and the node/component fills transparent.
  - [x] Verify stale canvas chrome remains absent: sheet title block, sheet grid overlay element, bottom dock heading, bottom-right overlay, floating panel, and any direct canvas `Cabinet Main` panel or bottom info table.
- [x] Keep the accepted popover and whitespace behavior explicit (AC: 3, 4)
  - [x] Verify the info popover remains top-bar driven.
  - [x] Verify whitespace click closes the info popover.
  - [x] Verify the bottom controls remain icon-only and transparent.
- [x] Update truth-preserving usage notes if needed (AC: 1, 3, 4, 5)
  - [x] Add any missing M21 canvas-contract note to `docs/usages/m21-proof-usage.md`.
  - [x] Keep `.mjs` files framed as supporting checks only.
  - [x] Keep M21 non-goals explicit and unchanged.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5)
  - [x] Run the new Story 1.3 contract test.
  - [x] Run the Story 1.2 visual-proof test.
  - [x] Run the Story 1.1 sample-project static test.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Story 1.2 already added a focused visual-proof test that binds the accepted customer-facing graph workbench surface.
- Story 1.1 already proved the sample project opens through `start:m21` and the smoke path.
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx` currently contains the accepted popover-only `Cabinet Main` behavior and whitespace-close handler.
- `ide/theia-frontend/src/browser/style/index.css` currently carries the accepted transparent overlay and grid-backed stage contract.

### Architectural Guardrails

- Follow M21 AD-9 and AD-10: visible IDE proof is a gate, and accepted M20 canvas behavior carries forward.
- This story is a regression guardrail for the accepted surface, not layout-intelligence feature work.
- Do not add layout intent, route facts, label logic, cabinet authoring, or final stack selection here.
- Preserve the Theia graph workbench as a consumer of governed data, not the owner of semantic truth.

### Implementation Guidance

Likely update targets:

- `ide/theia-frontend/scripts/athena-m21-canvas-contract.test.mjs`
- `docs/usages/m21-proof-usage.md`
- this story file and `sprint-status.yaml`

Expected preserved source contracts:

- `renderCabinetMainPopover`
- `renderBottomDock`
- `handleWorkbenchClick`
- `data-athena-info-button='true'`
- `data-athena-info-popover='true'`
- `athena-graph-workbench__stage`
- `athena-graph-workbench__viewport`
- `athena-graph-workbench__sheet`
- `athena-graph-workbench__canvas`
- `athena-graph-workbench__floating-bar`
- `athena-graph-workbench__bottom-dock`
- `athena-graph-workbench__zoom-dock`
- `athena-graph-workbench__info-popover`
- `athena-graph-workbench__sheet-frame`

Forbidden stale patterns:

- `athena-graph-workbench__sheet-title-block`
- `athena-graph-workbench__sheet-grid`
- `athena-graph-workbench__sheet-cross-reference-marker`
- `athena-graph-workbench__bottom-dock-heading`
- `athena-graph-workbench__overlay--bottom-right`
- `athena-graph-workbench__hud-chip`
- `athena-graph-workbench__floating-panel`
- any visible canvas `Cabinet Main` panel
- any bottom info table in the canvas surface

### Testing Requirements

Run checks sequentially on Windows:

- `node --test ide/theia-frontend/scripts/athena-m21-canvas-contract.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m21-canvas-contract.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`
- `yarn workspace @engineeringood/athena-theia-product build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added a focused M21 canvas-contract guard that checks the accepted graph workbench source and stylesheet contracts.
- Preserved the M20-accepted behavior: stage-level grid, transparent sheet/component surfaces, transparent floating controls, top info popover, and whitespace-close behavior.
- Tightened the CSS checks after review so the canvas-contract guard matches exact selector rules instead of accidentally spanning unrelated CSS.
- Kept this story limited to regression protection; no layout intent, layout engine, route facts, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection was implemented.
- Updated usage documentation to frame the `.mjs` checks as supporting verification, not user-authored sample content.

### File List

- `_bmad-output/implementation-artifacts/m21/1-3-preserve-the-accepted-m20-canvas-contract-in-m21.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `docs/usages/m21-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m21-canvas-contract.test.mjs`

## Change Log

- 2026-07-17: Created M21 Story 1.3 as the accepted M20 canvas-contract regression guard.
- 2026-07-17: Implemented and verified the accepted M20 canvas-contract regression guard for M21.
- 2026-07-17: Review passed after CSS guard matching was tightened.

## Senior Developer Review (AI)

### Outcome

Approved after patch.

### Action Items

- [x] [Review][Patch] Tighten canvas-contract CSS assertions to check exact selector rules.
