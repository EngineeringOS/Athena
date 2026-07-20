---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 1.3: Professional Density, Scale, And Compact Text

Status: done

## Story

As an engineer,
I want grid density, line weights, symbol scale, terminal labels, route labels, and reference markers
to feel coherent,
so that the sheet is readable at engineering-document density without visual crowding.

## Acceptance Criteria

1. The M27 Theia Graphical View uses a coherent professional density system for sheet grid,
   linework, symbol text, terminal labels, route labels, and reference markers.
2. Default visible route/reference text stays compact, while verbose semantic endpoint strings
   remain available through hover, selection, inspector, or proof payloads.
3. Accepted sheet text does not overlap symbols, routes, title/frame chrome, controls, or other
   labels in structured proof coverage.
4. The implementation stays in the active Theia IDE path and does not touch deprecated
   desktop-viewer, Compose, or KMP frontend modules.
5. Existing M24 route, M25 representation, M26 document projection, and M27 sheet-surface proof
   behavior remains intact.

## Tasks / Subtasks

- [x] Establish professional density tokens and CSS behavior (AC: 1, 3, 4)
  - [x] Tune Graphical View CSS for grid density, electrical line weights, terminal markers,
        terminal numbers, presentation labels, and reference controls as one coherent system.
  - [x] Keep sheet frame/title-block evidence fact-driven without adding visible canvas crowding.
  - [x] Do not use viewport-scaled font sizing; text remains stable and bounded.
- [x] Preserve compact text and verbose-on-demand behavior (AC: 2, 5)
  - [x] Keep verbose semantic route endpoint strings out of default visible labels.
  - [x] Ensure route hover/title and inspector/proof payload still expose full semantic identity.
  - [x] Keep route labels selection-only unless a compact display fact is explicitly visible.
- [x] Add density regression coverage (AC: 1, 2, 3, 5)
  - [x] Add/extend Theia frontend tests that assert professional density tokens and no forbidden
        crowded canvas overlays.
  - [x] Add/extend product smoke proof assertions for compact visible labels and sheet-surface
        density facts.
  - [x] Verify product smoke against the rebuilt Theia bundle.
- [x] Preserve active-editor projection correctness (AC: 4, 5)
  - [x] Keep M27 sample sources independently projectable through the current active-editor
        projection path.
  - [x] Do not introduce new `.athena` syntax.

## Dev Notes

- Story 1.2 added governed sheet-surface facts and product smoke proof attributes. Reuse those
  facts; do not reintroduce visible title-block overlay DOM on the canvas because M21 density tests
  intentionally forbid `athena-graph-workbench__sheet-title-block`.
- Story 1.2 also exposed an important product-path lesson: model/unit tests are not enough. The
  actual `yarn --cwd ide start:smoke:m27` path must be run after rebuilding the Theia product bundle.
- Current density styles live in `ide/theia-frontend/src/browser/style/index.css`, especially the
  rules around `.athena-graph-workbench__sheet-frame`, `__edge`, `__edge-label`,
  `__node-label--electrical-device`, `__presentation-terminal-number`,
  `__presentation-label`, and `__reference-marker-button`.
- Route labels are rendered by
  `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`; full semantic route detail
  is already available in `<title>` and route inspection/proof payloads.
- Presentation labels and terminal numbers are rendered by
  `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx` and
  `ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`.
- Do not touch deprecated desktop-viewer, Compose, or KMP frontend modules.
- Verification must run sequentially on Windows.

### Architecture Guardrails

- M27 AD-8: professional sheet surface is Presentation IR output.
- M27 AD-10: route quality is a fact, not a visual guess.
- M27 AD-11: Theia remains a fact consumer.
- M27 AD-14: no new source syntax by default.
- M27 AD-15: Theia IDE is the only frontend scope.
- M27 AD-16: cleanup gate remains mandatory before milestone closure.

### Previous Story Intelligence

- Story 1.2 corrected the M27 sample after product smoke revealed `semantic validation requested
  STOP_DOWNSTREAM`; do not add cross-file active-editor route endpoints until a governed workspace
  projection compiler path exists.
- Story 1.2 regenerated `integrations/graph-glsp/lib` because the Theia frontend consumes linked
  GLSP package typings from `lib`.
- Product smoke must run after `yarn --cwd ide build` when frontend code changes, otherwise Electron
  may serve a stale product bundle.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m27/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m27/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m27/1-2-professional-sheet-frame-and-metadata-facts.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-20: Started with frontend density/product proof assertions before production changes.
- 2026-07-20: Corrected graph-view sizing regression after live SVG inspection showed `viewBox="0 0 1680 1188"` and off-screen duplicate render elements in the stale path.
- 2026-07-20: Recorded incident lessons in `_bmad-output/implementation-artifacts/m27/M27-GRAPH-VIEW-FAILURE-NOTE.md`.

### Completion Notes List

- Graphical View route labels remain deferred/selection-only by default. Fresh M27 smoke reported `visibleRouteLabelCount: 0`, `deferredRouteLabelCount: 2`, and `visibleVerboseRouteLabelCount: 0` on the active first sheet.
- Live SVG sizing now follows active filtered scene bounds instead of publication frame size. Fresh M27 smoke reported active first-sheet `svgViewBox: "0 36 624 124"` and did not report `0 0 1680 1188`.
- Active first-sheet visual centering is verified by smoke proof with `sheetCenterDeltaX: 0` and `sheetCenterDeltaY: 0`.
- Density proof reports stable professional token values: electrical line width `1.6px`, terminal text `10px`, device text `11px`, route label text `10px`, and invalid text box count `0`.
- Product proof and screenshot path were generated through the rebuilt Theia bundle.

### File List

- `_bmad-output/implementation-artifacts/m27/1-3-professional-density-scale-and-compact-text.md`
- `_bmad-output/implementation-artifacts/m27/M27-GRAPH-VIEW-FAILURE-NOTE.md`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`

## Change Log

- 2026-07-20: Created Story 1.3 for professional density, scale, and compact text.
- 2026-07-20: Completed Story 1.3 after fresh frontend tests, Theia build, M27 smoke, and screenshot inspection.

## Verification

- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - passed.
- `yarn --cwd ide/theia-frontend test` - passed, 133/133 tests.
- `yarn --cwd ide build` - passed.
- `yarn --cwd ide start:smoke:m27` - passed and generated `_bmad-output/implementation-artifacts/m27/proofs/m27-graph-workbench-smoke.png`.
