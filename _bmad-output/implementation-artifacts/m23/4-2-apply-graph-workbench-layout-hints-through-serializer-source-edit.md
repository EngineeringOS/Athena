---
story_id: 4.2
story_key: 4-2-apply-graph-workbench-layout-hints-through-serializer-source-edit
epic: 4
epic_title: LSP And Graph Workbench Round-Trip Closure
title: Apply Graph Workbench layout hints through serializer/source edit
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 4.2: Apply Graph Workbench Layout Hints Through Serializer/Source Edit

## Story

As an engineer,
I want an approved layout adjustment to become valid `.athena` text,
So that layout round-trip survives close and reopen.

## Acceptance Criteria

**Given** a selected subject in Graph Workbench
**When** a placement, alignment, or grouping adjustment is approved
**Then** Graph Workbench sends layout intent to the serializer/source-edit path
**And** the resulting source text reparses without syntax errors
**And** rejected previews do not mutate source or canvas state

## Developer Context

M22 previewed layout edits by appending frontend-owned layout snippets. M23 must use typed authored
layout intent and a serializer path so the source edit is admitted `.athena` syntax, not hidden
canvas state or ad hoc string construction.

## Architecture Guardrails

- Follow AD-6: Graph Workbench is not syntax authority.
- Preview and persisted source must be the same accepted syntax.
- Do not introduce hidden storage such as localStorage, sessionStorage, IndexedDB, or canvas-owned persistence.

## Tasks/Subtasks

- [x] Add failing frontend tests for typed layout intent serialization and source-edit application.
- [x] Add Graph Workbench authored layout intent serialization for place, align, and group statements.
- [x] Ensure source edits use serialized intent text and normalize semantic IDs to authored names.
- [x] Run frontend checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 4.2 from backlog after Story 4.1 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `node --test ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs`; tests failed because no exported serializer existed and source edits trusted `layoutBlockSnippet`.
- 2026-07-18: TypeScript build passed with `yarn --cwd ide/theia-frontend build`.
- 2026-07-18: Targeted frontend checks passed with `node --test ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`.

### Completion Notes

- Added a typed Graph Workbench authored layout intent model and serializer for place, align, and group hints.
- Source edits now regenerate layout text from serialized authored intent instead of trusting preview snippet text.
- Semantic IDs such as `component:HMI1` are normalized to authored names such as `HMI1` before source emission.

### File List

- `ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`
- `ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`

### Change Log

- 2026-07-18: Added Graph Workbench authored layout intent serialization and source-edit emission for admitted M23 syntax.

## Status

Review. Story 4.2 is marked review in `sprint-status.yaml`.
