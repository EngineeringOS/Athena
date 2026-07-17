---
baseline_commit: 0b43cbe
---

# Story 4.3: Show Mutation Preview Before Applying Layout Source Edits

Status: done

## Story

As an engineer,
I want to inspect a proposed layout source mutation before applying it,
so that round-trip edits remain intentional and reviewable.

## Acceptance Criteria

1. Given a layout adjustment intent has been created, when Athena prepares a source mutation, then the preview names the affected subject and layout intent.
2. Given the preview is shown, when it is inspected, then the preview shows the proposed `.athena` layout hint or block change.
3. Given the preview is rejected, when state is inspected, then rejecting the preview does not modify source or canvas state.

## Tasks / Subtasks

- [x] Add mutation preview contract (AC: 1, 2)
  - [x] Add failing frontend/static test for preview generation.
  - [x] Add preview payload naming subject, intent, and proposed layout block snippet.
  - [x] Keep preview transient and non-persisted.
- [x] Add graph workbench preview affordance (AC: 1, 2, 3)
  - [x] Show pending preview state from captured adjustment intent.
  - [x] Add reject behavior that clears preview only.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run affected frontend tests.
  - [x] Run frontend build.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 4.2 captures transient layout adjustment intents.
- Story 4.3 must not apply source edits; it only prepares and displays a reviewable preview.

### Guardrails

- Do not persist source edits in this story.
- Do not store hidden canvas truth.
- Do not implement route or label source hint persistence.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 4, Story 4.3]
- [Source: `_bmad-output/implementation-artifacts/m22/M22-LAYOUT-HINT-SYNTAX.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs` failed first because preview types, builder, and widget rendering did not exist.
- `node --test ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs` passed after adding the preview builder and widget preview affordance.
- `yarn workspace @engineeringood/athena-theia-frontend build` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `AthenaGraphLayoutMutationPreview` and `buildAthenaGraphLayoutMutationPreview` for reviewable layout block snippets.
- Added a transient graph workbench preview panel with reject behavior that clears preview state only.
- Kept source edit application out of Story 4.3.

### File List

- `_bmad-output/implementation-artifacts/m22/4-3-show-mutation-preview-before-applying-layout-source-edits.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`

## Change Log

- 2026-07-18: Created M22 Story 4.3 with mutation preview requirements.
- 2026-07-18: Added transient layout mutation preview model, UI affordance, reject behavior, styles, and tests.
