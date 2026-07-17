---
baseline_commit: 0be07ce79c1cbcab9d4e678966fd283fda63bd5d
---

# Story 3.1: Stabilize viewport choreography

Status: review

## Story

As an engineer,
I want open, fit, zoom, and pan behavior to feel calm and predictable,
so that the sheet does not jump around while I inspect it.

## Acceptance Criteria

1. Given an opened schematic sheet, when I fit, zoom, or pan, the viewport behavior remains understandable and stable.
2. Selected content stays findable after viewport changes.
3. Repeated use on the same governed input does not create visible layout drift.
4. The view remains inside the existing Athena/Theia frontend boundary.
5. No desktop viewer, shell replacement, or new layout-stack decision is introduced.

## Tasks / Subtasks

- [x] Stabilize viewport commands (AC: 1, 2, 3)
  - [x] Keep open, fit, zoom, and pan behavior governed by the existing sheet surface.
  - [x] Preserve selection visibility after motion.
- [x] Keep the frontend boundary intact (AC: 4, 5)
  - [x] Do not add a second shell or alternate viewer path.
  - [x] Keep viewport behavior projection-driven.
- [x] Add interaction coverage (AC: 1, 2, 3)
  - [x] Prove stable behavior with local tests or scripted checks.

## Dev Notes

### Current State

- M19 already proved source, Problems, and sheet coherence through canonical ids.
- M20 should improve the feel of the viewport without changing canonical identity behavior.
- The current product boundary stays in Theia; no desktop-viewer work is in scope.

### Architectural Guardrails

- Follow M20 AD-5, AD-6, and AD-9.
- Viewport choreography is presentation only.
- Selection and reveal still round-trip through canonical ids.

### Project Structure Notes

- Likely update targets:
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-semantic-selection-service.ts`
  - `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
  - `ide/theia-frontend/scripts/*m20*.test.mjs`
- Prefer small frontend changes over layout rewrites.

### Testing Requirements

- Use the existing Theia frontend test stack.
- Prefer deterministic interaction tests over manual browser checks.
- Keep verification sequential on Windows.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 3, Story 3.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-5]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-5, AD-6]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- RED: `node --test ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs` failed because `keepAthenaGraphViewportFocusedOnSelection` did not exist.
- GREEN: `node --test ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs` passed after adding the viewport focus helper.
- Verification: `yarn test` from `ide/theia-frontend` passed all 63 frontend scripted checks.

### Completion Notes List

- Added a projection-driven viewport focus helper that centers offscreen node and connection selections without changing zoom.
- Repeated selection focus checks are idempotent, so the same governed input does not create visible drift.
- The Theia graph workbench now preserves manual viewport state across same-diagram refreshes and only auto-fits when the diagram/view/sheet identity changes.
- No desktop viewer, alternate shell, or new layout stack was introduced.

### File List

- `_bmad-output/implementation-artifacts/m20/3-1-stabilize-viewport-choreography.md`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

## Change Log

- 2026-07-17: Stabilized viewport refresh and selection focus behavior inside the existing Athena/Theia graph workbench.
