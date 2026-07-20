---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 3.2: Add Lightweight Sheet-View Selector In Graphical View

Status: done

## Story

As an Athena user,
I want a lightweight sheet-view selector in the existing Graphical View toolbar,
so that I can switch between document views without a new document explorer or crowded canvas.

## Acceptance Criteria

1. The Graphical View toolbar lists available sheet views by compact display order, view title, and
   view role when M26 sheet-view metadata is present.
2. Selecting `Power Distribution`, `Control And PLC Logic`, or `Field Wiring And Terminal
   Transition` updates the canvas to the selected view facts.
3. The selector does not create, infer, or persist document meaning.
4. The selector stays inside the existing Graphical View toolbar and does not obscure the canvas or
   regress M20-M25 controls.
5. Frontend tests verify selector model behavior and view switching against supplied sheet-view
   facts without mutating document projection data.

## Tasks / Subtasks

- [x] Add sheet-view selector model data (AC: 1, 3, 5)
  - [x] Derive compact selectable sheet-view entries from governed diagram sheet metadata.
  - [x] Preserve active sheet identity and avoid mutation of input document projection data.
- [x] Render selector in existing Theia Graphical View toolbar (AC: 1, 3, 4)
  - [x] Add a compact select/control near the existing view controls.
  - [x] Keep existing projection view buttons, info button, and connection controls unchanged.
- [x] Wire selector selection to existing governed view switching path (AC: 2, 3, 5)
  - [x] Reuse the Theia graph adapter service command path instead of adding canvas-owned state.
  - [x] Reset transient interaction state consistently with existing view switches.
- [x] Add frontend coverage (AC: 1, 2, 3, 5)
  - [x] Verify selector entries include order, title, role, active state, and subject count.
  - [x] Verify switching is command-driven and leaves source sheet metadata immutable.

## Dev Notes

- Active frontend is Theia only.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend modules.
- Do not create a new document explorer in M26; selector belongs in the existing Graphical View
  toolbar.
- Sheet-view selector data must be derived from transport/projection facts. Theia must not infer
  sheet-view meaning from source file names, canvas geometry, rendered DOM nodes, or graph labels.
- Do not add new `.athena` syntax.
- Verification must run sequentially on Windows.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-9, FR-10
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-2: `athena-document-projection-v0` owns view organization
  - AD-3: Document Projection IR owns topology, not geometry
  - AD-11: Theia navigates through the occurrence index
- Previous story:
  - `_bmad-output/implementation-artifacts/m26/3-1-transport-document-projection-snapshot-to-theia.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story started after Story 3.1 transport verification.
- `yarn test` in `ide/theia-frontend` failed in red phase because `sheetViewSelector` was not
  yet derived by the workbench model.
- `yarn test` in `ide/theia-frontend` failed in red phase because the Graphical View toolbar did
  not yet expose the M26 sheet-view selector.
- `yarn test` in `integrations/graph-glsp` passed after adding optional sheet role transport type.
- `yarn test` in `ide/theia-frontend` passed after adding selector model, toolbar control, and
  preservation tests.

### Completion Notes List

- Added optional sheet-view role transport metadata for M26 while preserving existing sheet payload
  compatibility.
- Added a derived `sheetViewSelector` model with stable order, title, role, subject count, active
  state, and compact labels.
- Added a compact Theia Graphical View toolbar selector that delegates to the existing governed view
  switch command path.
- Kept selector state downstream and non-authoritative; it does not persist document meaning or scan
  canvas/DOM state.

### File List

- `_bmad-output/implementation-artifacts/m26/3-2-add-lightweight-sheet-view-selector-in-graphical-view.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts.map`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-graph-workbench-preservation.test.mjs`

## Change Log

- 2026-07-20: Created Story 3.2 from M26 Epic 3.
- 2026-07-20: Implemented lightweight governed sheet-view selector in the Theia Graphical View
  toolbar.
- 2026-07-20: Marked Story 3.2 done after frontend and GLSP verification.
