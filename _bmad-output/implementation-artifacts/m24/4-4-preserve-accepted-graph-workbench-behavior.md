---
status: review
epic: 4
story: 4.4
title: Preserve accepted Graph Workbench behavior
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 4.4: Preserve accepted Graph Workbench behavior

As Aaron, I want routing improvements without UI regressions, so that M24 does not reopen the
M20-M23 canvas issues.

## Acceptance Criteria

- Grid remains the coordinate surface.
- Floating controls remain transparent overlays.
- Top information popover behavior remains unchanged.
- Whitespace click closes the information popover.
- Outline navigation keeps the same `.athena` editor tab.
- Active-source Graphical View projection remains correct.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Run the accepted M20-M23 UI regression checks after route rendering changes.

## Tasks/Subtasks

- [x] Add an M24-specific Graph Workbench preservation guard for grid, transparent overlays, and info popover behavior.
- [x] Preserve same-tab editor/navigation and active-source refresh checks from accepted M20-M23 behavior.
- [x] Verify route rendering does not introduce hidden canvas persistence or desktop-viewer scope.
- [x] Run the accepted Theia frontend regression suite after route rendering changes.

## Dev Agent Record

### Debug Log

- Added `athena-m24-graph-workbench-preservation.test.mjs` to guard the accepted graph workbench UI contract during M24 routing work.
- The guard checks the grid-backed stage, transparent sheet/controls, top info popover close behavior, active-source refresh hooks, and no desktop-viewer scope.
- Ran the full Theia frontend test suite, including M20, M21, M22, M23, and M24 behavior preservation checks.

### Completion Notes

- Story 4.4 is regression preservation only.
- No production behavior change was required for this story.
- Route rendering additions preserve the accepted Graph Workbench behavior.

### Verification

- `yarn --cwd ide/theia-frontend test`

## File List

- `ide/theia-frontend/scripts/athena-m24-graph-workbench-preservation.test.mjs`

## Change Log

- 2026-07-19: Added M24 graph workbench preservation regression guard.

## Status

review
