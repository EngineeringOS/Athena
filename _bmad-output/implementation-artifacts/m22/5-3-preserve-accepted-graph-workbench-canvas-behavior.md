---
baseline_commit: 0b43cbe
---

# Story 5.3: Preserve Accepted Graph Workbench Canvas Behavior

Status: done

## Story

As a reviewer,
I want M22 optimization and round-trip work to preserve the accepted graph workbench surface,
so that layout intelligence does not regress M20/M21 UI behavior.

## Acceptance Criteria

1. Given the M22 graph workbench is open, when optimization, ELK-assisted layout, or round-trip preview behavior is active, then the stage grid remains the coordinate surface.
2. Given sheet and component bodies are rendered, then they do not hide the grid.
3. Given `Cabinet Main` information is needed, then it remains in the top information popover only.
4. Given canvas controls are shown, then floating controls remain transparent overlays.

## Tasks / Subtasks

- [x] Add canvas behavior regression coverage (AC: 1, 2, 3, 4)
  - [x] Add failing frontend/static test for M22 canvas invariants.
  - [x] Cover grid, transparent sheet/component bodies, top-only `Cabinet Main`, and transparent control overlays.
- [x] Preserve graph workbench surface behavior (AC: 1, 2, 3, 4)
  - [x] Confirm `Cabinet Main` is not rendered as canvas title-block text or bottom dock content.
  - [x] Confirm preview controls do not introduce opaque canvas panels over the grid.
- [x] Run validation (AC: 1, 2, 3, 4)
  - [x] Run affected frontend test.
  - [x] Run M22 baseline proof test.
  - [x] Run frontend build.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- M20/M21 established the accepted canvas behavior after user review: grid is a coordinate surface, overlays are transparent, and `Cabinet Main` information lives behind the top info icon.
- M22 adds layout preview and optimization behavior that must not regress that visual contract.

### Guardrails

- Do not put `Cabinet Main` back into the main canvas or bottom dock.
- Do not make toolbar parent containers opaque or bordered.
- Keep component/sheet fills transparent enough for the grid to remain visible.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m22-baseline-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 5, Story 5.3]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [Source: `ide/theia-frontend/src/browser/style/index.css`]
- [Source: `ide/theia-product/scripts/verify-athena-m22-sample-project.js`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m22-baseline-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added M22 canvas behavior regression coverage for the grid coordinate surface, transparent sheet and electrical component bodies, transparent floating controls, and top-only `Cabinet Main` information.
- Confirmed `Cabinet Main` is absent from the bottom dock, sheet chrome, and layout mutation preview.
- Documented the preserved canvas behavior in the M22 usage guide.

### File List

- `_bmad-output/implementation-artifacts/m22/5-3-preserve-accepted-graph-workbench-canvas-behavior.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs`

## Change Log

- 2026-07-18: Created M22 Story 5.3 with canvas behavior preservation requirements.
- 2026-07-18: Added M22 canvas behavior guard and verified baseline proof, frontend build, and encoding audit.
