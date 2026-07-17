---
baseline_commit: 0b43cbe
---

# Story 1.3: Add the M22 IDE-visible baseline proof

Status: done

## Story

As a reviewer,
I want the M22 sample to prove baseline behavior in the graph workbench,
so that later optimization work starts from a visible, accepted IDE surface.

## Acceptance Criteria

1. Given the M22 sample project is opened in Theia, when the Graphical View is launched for the baseline `.athena` file, then the graph workbench renders the active source file.
2. Given the baseline graph workbench, when the smoke proof runs, then the stage grid remains visible behind sheet and component bodies.
3. Given accepted M20/M21 canvas behavior, when the smoke proof runs, then `Cabinet Main` information remains in the top information popover only.
4. Given accepted graph controls, when the smoke proof runs, then top and bottom controls remain transparent canvas overlays.
5. Given reviewers inspect the sample documentation, when they look for the baseline proof, then the proof path is documented from sample docs and usage docs.

## Tasks / Subtasks

- [x] Publish the M22 baseline proof note (AC: 1, 2, 3, 4, 5)
  - [x] Add a sample-project baseline proof document.
  - [x] Name the baseline source file and Graphical View proof path.
  - [x] Document the graph workbench DOM checks that define accepted baseline behavior.
- [x] Link the baseline proof from user-facing docs (AC: 5)
  - [x] Update the M22 sample README.
  - [x] Update the M22 usage doc.
- [x] Add validation (AC: 1, 2, 3, 4, 5)
  - [x] Add a Node static check for the baseline proof artifact and smoke assertions.
  - [x] Run the M22 Electron smoke proof.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 1.1 added `yarn start:smoke:m22`, which opens `examples/m22/sample-project` and validates graph workbench DOM behavior.
- Story 1.3 must make the baseline proof explicit for reviewers before later stories change optimization behavior.

### Guardrails

- Do not add new renderer behavior in this story.
- Do not broaden visual acceptance beyond accepted M20/M21 canvas invariants.
- Keep the proof tied to the openable Theia sample project.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-baseline-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m22`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 1, Story 1.3]
- [Source: `ide/theia-product/scripts/verify-athena-m22-sample-project.js`]
- [Source: `examples/m22/sample-project/README.md`]
- [Source: `docs/usages/m22-proof-usage.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-baseline-proof.test.mjs` failed first because `M22-BASELINE-PROOF.md` did not exist, then passed after adding the proof artifact and links.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m22` passed and reported the M22 sample-project workspace path plus graph-workbench DOM proof.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `examples/m22/sample-project/M22-BASELINE-PROOF.md` to document the active-source Graphical View baseline and accepted graph workbench DOM markers.
- Linked the baseline proof from the M22 sample README and M22 usage doc.
- Verified the live M22 Electron smoke proof still opens `examples/m22/sample-project` and validates grid, transparency, popover, and whitespace-close behavior.

### File List

- `_bmad-output/implementation-artifacts/m22/1-3-add-the-m22-ide-visible-baseline-proof.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `examples/m22/sample-project/README.md`
- `examples/m22/sample-project/M22-BASELINE-PROOF.md`
- `ide/theia-frontend/scripts/athena-m22-baseline-proof.test.mjs`
## Change Log

- 2026-07-18: Created M22 Story 1.3 with IDE-visible baseline proof requirements.
- 2026-07-18: Added the M22 baseline proof document, links, static validation, and smoke verification.
