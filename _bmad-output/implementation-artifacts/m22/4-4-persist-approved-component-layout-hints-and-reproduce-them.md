---
baseline_commit: 0b43cbe
---

# Story 4.4: Persist Approved Component Layout Hints and Reproduce Them

Status: done

## Story

As an engineer,
I want approved component layout adjustments persisted into `.athena`,
so that closing and reopening the project reproduces the same governed layout.

## Acceptance Criteria

1. Given a user approves a placement, alignment, or grouping mutation preview, when Athena applies the change through governed mutation authority, then the `.athena` source receives a reviewable layout hint or layout block.
2. Given the source edit is applied, when source, outline, Problems, and sheet identity are inspected, then they remain coherent after the edit.
3. Given the project is reopened or reprojected, when layout is reproduced, then the adjusted layout is derived from source.

## Tasks / Subtasks

- [x] Add layout source edit generation (AC: 1, 3)
  - [x] Add failing frontend/static test for approved preview source edit generation.
  - [x] Build append-only source edit from the layout mutation preview.
  - [x] Preserve suggested semantic identity for reveal after edit.
- [x] Add graph workbench accept behavior (AC: 1, 2)
  - [x] Apply approved source edit through existing editor bridge.
  - [x] Clear transient preview after apply and schedule refresh.
- [x] Document reproduction boundary (AC: 3)
  - [x] Update M22 usage docs with approved layout block persistence path.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run affected frontend tests.
  - [x] Run frontend build.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 4.3 creates a transient preview with a layout block snippet.
- Story 4.4 may apply a source edit, but it must use the existing editor bridge and reviewable source text.

### Guardrails

- Do not persist hidden canvas state.
- Do not persist route or label hints.
- Do not bypass the existing source edit application path.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 4, Story 4.4]
- [Source: `_bmad-output/implementation-artifacts/m22/M22-LAYOUT-HINT-SYNTAX.md`]
- [Source: `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added a reviewable append-only layout source edit helper that persists the approved preview block and preserves the subject semantic id for reveal after apply.
- Wired the graph workbench accept action through the existing LSP editor bridge, then clears the transient preview, refreshes projection, and reselects the adjusted semantic subject.
- Documented the approved layout preview path and confirmed no hidden canvas persistence was introduced.

### File List

- `_bmad-output/implementation-artifacts/m22/4-4-persist-approved-component-layout-hints-and-reproduce-them.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`

## Change Log

- 2026-07-18: Created M22 Story 4.4 with approved layout hint persistence requirements.
- 2026-07-18: Implemented approved layout preview source persistence and verified frontend build plus encoding audit.
