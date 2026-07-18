---
story_id: 4.3
story_key: 4-3-reparse-and-reproject-after-approved-layout-edits
epic: 4
epic_title: LSP And Graph Workbench Round-Trip Closure
title: Reparse and reproject after approved layout edits
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 4.3: Reparse And Reproject After Approved Layout Edits

## Story

As an IDE user,
I want accepted layout edits to immediately reparse and reproject,
So that the graph reflects source-owned layout intent.

## Acceptance Criteria

**Given** an approved M23 layout source edit
**When** the source document updates
**Then** LSP diagnostics refresh
**And** Graphical View reprojects from the active source
**And** close/reopen reproduces the same admitted layout relationship

## Developer Context

Story 4.2 made Graph Workbench source text serializer-owned. Story 4.3 must ensure the edit lands in
the admitted system scope so the next parse, diagnostics refresh, and projection request can use the
updated source.

## Architecture Guardrails

- Keep layout blocks system-scoped.
- Source edits must produce parseable `.athena` text on first application.
- Graphical View refresh must continue through the existing active-source projection path.

## Tasks/Subtasks

- [x] Add failing source-edit regression proving applied layout text remains inside the active system block.
- [x] Insert approved layout source edits before the system closing brace rather than after the file.
- [x] Keep accepted edit refresh behavior intact in Graph Workbench.
- [x] Run frontend checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 4.3 from backlog after Story 4.2 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `node --test ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs`; the applied layout source edit landed after the system closing brace.
- 2026-07-18: TypeScript build passed with `yarn --cwd ide/theia-frontend build`.
- 2026-07-18: Targeted frontend checks passed with `node --test ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`.

### Completion Notes

- Source edits now use the active document text to insert M23 layout blocks before the system closing brace.
- Graph Workbench passes active document text into the layout source-edit builder.
- Existing accepted preview refresh behavior remains in place through `scheduleRefresh()`.

### File List

- `ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`

### Change Log

- 2026-07-18: Kept approved layout edits system-scoped so the updated source can reparse and reproject.

## Status

Review. Story 4.3 is marked review in `sprint-status.yaml`.
