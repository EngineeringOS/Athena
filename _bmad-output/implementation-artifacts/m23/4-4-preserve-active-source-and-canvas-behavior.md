---
story_id: 4.4
story_key: 4-4-preserve-active-source-and-canvas-behavior
epic: 4
epic_title: LSP And Graph Workbench Round-Trip Closure
title: Preserve active-source and canvas behavior
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 4.4: Preserve Active-Source And Canvas Behavior

## Story

As a reviewer,
I want M23 language admission to preserve M20-M22 IDE behavior,
So that parser work does not regress the visible product surface.

## Acceptance Criteria

**Given** the M23 sample project is open in Theia
**When** switching between `.athena` files and opening Graphical View
**Then** the active source file drives the projection
**And** outline navigation keeps the same editor tab
**And** grid, transparent controls, top information popover, and whitespace dismissal behavior remain unchanged

## Developer Context

Stories 4.1-4.3 touched LSP diagnostics and Graph Workbench layout source-edit behavior. This story
guards the accepted M20-M22 visible IDE contract before the M23 sample project is introduced.

## Architecture Guardrails

- Preserve active-source projection.
- Preserve same-tab source navigation.
- Preserve the accepted canvas visual contract: grid background, transparent controls, top info popover, and whitespace dismissal.

## Tasks/Subtasks

- [x] Add M23 regression checks for active-source/source-edit/canvas behavior preservation.
- [x] Verify existing M22 active-source and canvas guardrails still pass.
- [x] Avoid production changes unless a regression is found.
- [x] Run frontend checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 4.4 from backlog after Story 4.3 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Frontend behavior checks passed with `node --test ide/theia-frontend/scripts/athena-m23-ide-behavior-preservation.test.mjs ide/theia-frontend/scripts/athena-m22-active-source-projection.test.mjs ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs`.

### Completion Notes

- Added M23 regression guardrail tying layout source edits to active-source refresh and accepted canvas behavior.
- Verified existing M22 active-source and canvas behavior checks still pass.
- No additional production change was required for this guardrail story.

### File List

- `ide/theia-frontend/scripts/athena-m23-ide-behavior-preservation.test.mjs`

### Change Log

- 2026-07-18: Added M23 IDE behavior preservation guardrail for active-source refresh and accepted canvas behavior.

## Status

Review. Story 4.4 is marked review in `sprint-status.yaml`.
