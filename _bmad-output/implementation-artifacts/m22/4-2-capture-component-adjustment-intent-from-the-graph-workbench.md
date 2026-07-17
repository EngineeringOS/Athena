---
baseline_commit: 0b43cbe
---

# Story 4.2: Capture Component Adjustment Intent from the Graph Workbench

Status: done

## Story

As an engineer,
I want component placement, alignment, and grouping adjustments captured as governed intents,
so that user adjustment starts as semantic layout input rather than hidden canvas state.

## Acceptance Criteria

1. Given an M22 sheet occurrence is selected in the Graphical View, when the user adjusts placement, alignment, or grouping, then Athena creates a layout adjustment intent with canonical subject, occurrence, view, sheet, snapshot, and source identities.
2. Given an adjustment is captured, when state is inspected, then the canvas does not persist hidden layout truth.
3. Given unsupported route or label adjustments are attempted, when capture is evaluated, then unsupported route or label adjustments are rejected or ignored with a clear non-M22 boundary.

## Tasks / Subtasks

- [x] Add graph workbench adjustment intent contract (AC: 1, 2)
  - [x] Add failing frontend unit/static test for adjustment intent capture.
  - [x] Add typed adjustment intent payload with canonical identities.
  - [x] Keep payload transient and source-mutation independent.
- [x] Add unsupported adjustment boundary (AC: 3)
  - [x] Reject or ignore route and label adjustment kinds.
  - [x] Expose a clear non-M22 boundary reason.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run affected frontend tests.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 4.1 selected a layout block syntax.
- Story 4.2 should capture adjustment intent only; mutation preview and source persistence are later stories.

### Guardrails

- Do not persist source edits in this story.
- Do not store hidden canvas truth.
- Do not support route or label adjustment persistence.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-layout-adjustment-intent.test.mjs`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 4, Story 4.2]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-4, AD-5, AD-6]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-layout-adjustment-intent.test.mjs` failed first because adjustment intent types and capture function did not exist.
- `node --test ide/theia-frontend/scripts/athena-m22-layout-adjustment-intent.test.mjs` passed after adding the intent contract and widget capture hook.
- `yarn workspace @engineeringood/athena-theia-frontend build` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added a typed `AthenaGraphLayoutAdjustmentIntent` payload carrying subject, occurrence, view, sheet, snapshot, and source identities.
- Added `captureAthenaGraphLayoutAdjustmentIntent` with a clear non-M22 boundary for route and label adjustment persistence.
- Wired graph workbench drag placement completion to capture the transient intent without hidden browser storage or source mutation.

### File List

- `_bmad-output/implementation-artifacts/m22/4-2-capture-component-adjustment-intent-from-the-graph-workbench.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m22-layout-adjustment-intent.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`

## Change Log

- 2026-07-18: Created M22 Story 4.2 with graph workbench adjustment intent requirements.
- 2026-07-18: Added transient layout adjustment intent capture, unsupported-kind boundary, widget hook, and tests.
