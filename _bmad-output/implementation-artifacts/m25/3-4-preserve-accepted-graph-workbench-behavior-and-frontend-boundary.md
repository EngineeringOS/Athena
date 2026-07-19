---
status: ready-for-dev
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

- [ ] Add boundary check for forbidden frontend modules.
- [ ] Run existing M20-M24 Graph Workbench regression coverage.
- [ ] Add M25-specific frontend smoke assertions.
- [ ] Document accepted workbench behavior in completion notes.

## Dev Notes

- Governed by AD-9.
- User explicitly rejected desktop-viewer/KMP/Compose scope.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
