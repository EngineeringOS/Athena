---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 7.3
epic: 7
title: Make Create Entity Control Understandable And Usable
---

# Story 7.3: Make Create Entity Control Understandable And Usable

## Status

Review

## Story

As a controls engineer,
I want the add/create control to open a clear, frontmost creation panel,
so that governed semantic entity creation is usable instead of hidden behind architecture wording
or overlapping graph chrome.

## Required Context

- Story 7.1 taxonomy contract: `_bmad-output/implementation-artifacts/m32/7-1-define-graph-view-taxonomy-and-toolbar-contract.md`
- Story 7.2 sheet navigation behavior: `_bmad-output/implementation-artifacts/m32/7-2-stabilize-sheet-navigation-across-graph-view-modes.md`
- M31 authoring architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m31/ARCHITECTURE-SPINE.md`
- M32 architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`

## Acceptance Criteria

1. Given the Graph View add button is visible, when a user hovers or inspects the control, then the
   label explains the action in product language, not only `Create governed entity`.
2. Given the add button is clicked, when the create panel opens, then it is frontmost, within the
   viewport, not covered by canvas/sheet chrome, keyboard reachable, closeable, and visually stable
   at desktop and compact widths.
3. Given no Athena source editor is active, when the create panel opens, then browsing concept
   templates remains possible, while preview/accept clearly require a governed source context.
4. Given Electron smoke runs, when the M32 sample opens, then structured proof verifies panel
   geometry, frontmost hit target, controls, close behavior, and screenshot evidence.
5. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Use CodeGraph on create-entity widget state, authoring protocol, and smoke proof before
  editing. (AC: 1..4)
- [x] Add RED frontend/source tests for visible label/aria/title product language. (AC: 1)
- [x] Add RED smoke assertions for panel geometry: within viewport, frontmost at center, not
  canvas-covered, closeable, and controls reachable. (AC: 2,4)
- [x] Implement the smallest UI/CSS/state fix so opening the panel is deterministic and does not
  require current editor focus for browsing. (AC: 2,3)
- [x] Preserve backend-governed preview/accept authority; do not let Theia construct source edits.
  (AC: 3)
- [x] Run desktop and compact-width proof or explicitly add a stable viewport-size smoke path.
  (AC: 2,4)
- [x] Update docs/ledger for retained limitations. (AC: 5)
- [x] Run focused frontend tests, product build, M32 smoke, screenshot proof, and encoding audit if
  docs changed. (AC: 1..5)

## Dev Notes

- This story fixes usability of the create panel only. It must not change package rendering
  authority or sheet navigation behavior except where needed to avoid overlap with Story 7.2
  controls.
- Existing DOM-existence tests are insufficient. E2E must prove visible, frontmost, usable panel
  geometry.
- Preview and accept remain governed backend authoring transactions. Theia may collect form input
  and show preview but cannot serialize authoritative source edits itself.

## Testing Requirements

- TDD required. The first RED should fail because DOM existence is not enough.
- Focused commands:
  - `yarn build` and relevant `node --test scripts\*.test.mjs` in `ide/theia-frontend`
  - `yarn build` and `yarn start:smoke:m32` in `ide/theia-product`
- Screenshot evidence is secondary to structured geometry proof.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- CodeGraph/context inspection covered create-entity widget state, CSS stacking, authoring protocol boundaries, and M32 smoke proof collection before edits.
- RED proof: `yarn start:smoke:m32` failed with `panelHeight=22` after adding geometry assertions, proving DOM existence was insufficient.
- GREEN proof: `node --test scripts\athena-m32-create-entity-panel.test.mjs scripts\athena-m32-graph-view-taxonomy.test.mjs scripts\athena-ide-density-contract.test.mjs` passed 8/8.
- Build proof: `yarn build` in `ide/theia-frontend` passed; `yarn build` in `ide/theia-product` passed.
- E2E proof: `yarn start:smoke:m32` passed after rebuild with create panel `panelWidth=560`, `panelHeight=220`, `withinViewport=true`, `frontmostAtCenter=true`, `reachableControlCount=5`, and screenshot capture at `_bmad-output/implementation-artifacts/m32/screenshots/m32-graph-workbench-smoke.png`.
- One intermediate post-build smoke run timed out after Graph Workbench viewport startup without graph proof; an immediate rerun passed with the structured geometry proof above.

### Completion Notes List

- Made the create panel viewport-bounded and frontmost using fixed positioning, viewport width/height constraints, min height, shadow, and a compact-width media rule.
- Added structured smoke geometry proof for panel bounds, center hit target, viewport containment, and reachable controls.
- Kept preview/accept authority backend-governed; Theia still only collects draft inputs and requests previews/decisions.
- Updated source-context guidance wording to product language.
- AC-to-evidence: AC1 covered by product label tests; AC2 covered by CSS source test and E2E geometry proof; AC3 covered by existing no-editor-focus browsing test and unchanged backend preview/accept path; AC4 covered by smoke proof and screenshot; AC5 covered by cleanup ledger, tests, builds, smoke, and encoding audit.

### File List

- _bmad-output/implementation-artifacts/m32/7-3-make-create-entity-control-understandable-and-usable.md
- _bmad-output/implementation-artifacts/m32/cleanup-ledger.md
- _bmad-output/implementation-artifacts/m32/screenshots/m32-graph-workbench-smoke.png
- _bmad-output/implementation-artifacts/m32/sprint-status.yaml
- ide/theia-frontend/scripts/athena-m32-create-entity-panel.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m32-sample-project.js

### Change Log

- 2026-07-22: Fixed create-panel collapsed/overlapped geometry, added frontmost/viewport E2E proof, and preserved backend-governed source preview authority.

## Mandatory Final Polish/Purge Gate

- Review CSS stacking, toolbar grouping, create-panel state, smoke proof, screenshots, and stale
  panel tests.
- Remove or ledger any known limitation.
- Record AC-to-evidence before moving the story to `review`.
