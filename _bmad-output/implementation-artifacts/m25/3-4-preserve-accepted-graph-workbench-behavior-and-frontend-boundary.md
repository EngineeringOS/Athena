---
status: ready-for-dev
baseline_commit: b195399ea8ba56f120948427e5f63d55cc8fec5f
epic: 3
story: 3.4
title: Preserve accepted Graph Workbench behavior and frontend boundary
---

# Story 3.4: Preserve accepted Graph Workbench behavior and frontend boundary

## Story

As a product owner,
I want M25 to keep accepted M20-M24 IDE behavior,
So that symbol work does not regress the actual product shell.

## Acceptance Criteria

- Active-source projection, same-tab outline navigation, grid-backed canvas, transparent controls,
  and top-popover behavior remain intact.
- M25 rendering is implemented only in Theia IDE/frontend path.
- No desktop-viewer, Compose, or deprecated KMP frontend module is changed.
- Existing M24 Graph Workbench smoke continues to pass.

## Tasks/Subtasks

- [x] Add boundary check for forbidden frontend modules.
- [x] Run existing M20-M24 Graph Workbench regression coverage.
- [x] Add M25-specific frontend smoke assertions.
- [x] Document accepted workbench behavior in completion notes.

## Dev Notes

- Governed by AD-9.
- User explicitly rejected desktop-viewer/KMP/Compose scope.

## Dev Agent Record

### Debug Log

- 2026-07-19: Added `athena-m25-frontend-boundary.test.mjs`; first run failed because the test matched boundary language in story prose instead of only implementation file lists.
- 2026-07-19: Fixed the boundary guard to inspect only File List sections for forbidden implementation outputs.
- 2026-07-19: Green phase passed with `yarn --cwd ide/theia-frontend test --test-name-pattern "M20|M21|M22|M23|M24|M25|Graphical View projection|same-tab"`; 111 tests passed.

### Completion Notes

- Added a Theia/frontend boundary test covering M25 representation rendering sources and M25 story File Lists.
- Verified accepted Graph Workbench behavior remains covered: active-source projection, same-tab reveal, grid-backed canvas, transparent controls, and top-popover behavior.
- Added M25 frontend smoke coverage for representation markers, terminal markers/numbers, label markers, and fallback-free rendering markers.
- No desktop-viewer, Compose, or deprecated KMP frontend implementation files are part of the M25 Epic 3 output.

### File List

- `ide/theia-frontend/scripts/athena-m25-frontend-boundary.test.mjs`

## Change Log

- 2026-07-19: Added M25 frontend boundary and accepted Graph Workbench behavior guardrails.

## Status

review
