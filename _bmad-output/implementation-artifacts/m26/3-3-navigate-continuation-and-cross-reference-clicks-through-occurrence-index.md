---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 3.3: Navigate Continuation And Cross-Reference Clicks Through Occurrence Index

Status: done

## Story

As an engineer following a reference,
I want clicking a continuation or cross-reference marker to reveal the related occurrence,
so that I can navigate document views through semantic identity rather than screen geometry.

## Acceptance Criteria

1. Theia resolves visible continuation and cross-reference marker targets through governed
   document occurrence identity.
2. Cross-view marker navigation switches to the target sheet view when needed.
3. Same-view marker navigation selects or reveals the target occurrence without unnecessary view
   switching.
4. Missing target occurrences fail cleanly without scanning rendered lines, DOM nodes, or graph
   labels.
5. Tests cover same-view, cross-view, and missing-target behavior.

## Tasks / Subtasks

- [x] Add reference marker navigation model helpers (AC: 1, 3, 4, 5)
  - [x] Resolve marker target occurrences from marker payloads and supplied sheet metadata.
  - [x] Return a clear missing-target result without DOM/canvas fallback.
- [x] Wire Theia marker selection/click handling (AC: 1, 2, 3, 4)
  - [x] Switch to target sheet view through the existing governed graph adapter command path.
  - [x] Update semantic selection using canonical target subject identity.
- [x] Add frontend coverage (AC: 1, 2, 3, 4, 5)
  - [x] Verify same-view references keep the current view and select the target subject.
  - [x] Verify cross-view references switch to the target sheet view.
  - [x] Verify missing targets produce no canvas/DOM inference.

## Dev Notes

- Active frontend is Theia only.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend modules.
- Use the `referenceMarkers` payload introduced in Story 3.1 and the sheet metadata surfaced in
  Story 3.2.
- Theia must navigate by canonical subject and occurrence identity. It must not parse visible marker
  text, route labels, DOM nodes, or canvas geometry.
- Do not add new `.athena` syntax.
- Verification must run sequentially on Windows.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-9, FR-10
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-10: Diagnostics preserve source and projection provenance
  - AD-11: Theia navigates through the occurrence index
- Previous stories:
  - `_bmad-output/implementation-artifacts/m26/3-1-transport-document-projection-snapshot-to-theia.md`
  - `_bmad-output/implementation-artifacts/m26/3-2-add-lightweight-sheet-view-selector-in-graphical-view.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story started after Story 3.2 sheet-view selector verification.
- `yarn test` in `ide/theia-frontend` failed in red phase because
  `resolveAthenaGraphReferenceMarkerNavigation` did not exist.
- `yarn test` in `ide/theia-frontend` passed after adding marker navigation resolution and Theia
  compact reference marker controls.

### Completion Notes List

- Added downstream workbench reference marker facts sourced from Presentation IR marker payloads.
- Added `resolveAthenaGraphReferenceMarkerNavigation()` for ready and missing-marker outcomes.
- Added compact marker controls that call the existing governed sheet-view switch path and then
  select the target canonical identity.
- Kept marker navigation independent from rendered route labels, DOM scans, and canvas geometry.

### File List

- `_bmad-output/implementation-artifacts/m26/3-3-navigate-continuation-and-cross-reference-clicks-through-occurrence-index.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-graph-workbench-preservation.test.mjs`

## Change Log

- 2026-07-20: Created Story 3.3 from M26 Epic 3.
- 2026-07-20: Implemented governed reference marker navigation through marker payload identity.
- 2026-07-20: Marked Story 3.3 done after frontend verification.
