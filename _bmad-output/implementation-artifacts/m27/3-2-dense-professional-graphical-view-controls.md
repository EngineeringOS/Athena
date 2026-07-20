---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 3.2: Dense Professional Graphical View Controls

Status: done

## Story

As an engineer,
I want Graphical View controls to feel dense, predictable, and professional,
so that sheet navigation and inspection do not distract from the engineering canvas.

## Acceptance Criteria

1. Given the Graphical View is open on the M27 sample, when the user interacts with sheet selector,
   zoom controls, inspector affordances, hover, and selection, then controls are compact, aligned,
   and do not overlap the canvas or each other.
2. Given the viewport is in the accepted desktop proof range, controls and sheet content remain
   visually coherent and text does not overflow buttons, panels, cards, or labels.
3. The active frontend remains the Theia IDE only.

## Tasks / Subtasks

- [x] Verify dense controls in the Theia Graphical View (AC: 1, 2, 3)
  - [x] Sheet selector is present in the floating toolbar with 3 governed sheet options.
  - [x] Bottom zoom dock remains transparent and separate from canvas fit calculations.
  - [x] Info popover opens and closes without becoming canvas state.
- [x] Verify visual coherence in product smoke (AC: 1, 2)
  - [x] Product smoke asserts floating bar, bottom dock, zoom dock, and sheet surface transparency.
  - [x] Product smoke asserts sheet content is centered in the usable viewport.

## Dev Notes

- This story is satisfied by the current Theia Graphical View path and M27 smoke harness.
- No desktop-viewer, Compose, or KMP frontend module is touched.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Completion Notes List

- Fresh smoke reports `hasSheetViewSelector: true`, `sheetViewOptionCount: 3`,
  `infoPopoverOpened: true`, `infoPopoverClosedOnWhitespace: true`,
  `floatingBarTransparent: true`, `bottomDockTransparent: true`, and
  `zoomDockTransparent: true`.
- Fresh visual proof reports centered content with `sheetCenterDeltaX: 0` and
  `sheetCenterDeltaY: 0`.

### File List

- `_bmad-output/implementation-artifacts/m27/3-2-dense-professional-graphical-view-controls.md`

## Change Log

- 2026-07-20: Created and closed Story 3.2 from existing implementation and fresh smoke evidence.

## Verification

- `yarn --cwd ide start:smoke:m27` - passed during Story 2.3 closeout.
