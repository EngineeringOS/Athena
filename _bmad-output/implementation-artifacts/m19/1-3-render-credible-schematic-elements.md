---
baseline_commit: 80a790cb5e7b6733055a529e4f6f6de5b03ff857
---

# Story 1.3: Render credible schematic elements

Status: done

## Story

As an engineer,
I want the Theia sheet surface to render a credible schematic with page frame, grid, title block, labels, terminals, conductor paths, and cross-reference markers,
so that the customer-facing IDE view reads like a real engineering sheet instead of a graph sketch.

## Acceptance Criteria

1. Given governed projection data for the active schematic view, the Theia sheet surface renders a visible page frame, grid, title block, labels, terminals, conductor paths, and cross-reference markers.
2. The sheet surface consumes projection-produced sheet and layout facts only; it does not infer page chrome or schematic meaning locally.
3. Repeated runs on the same governed input produce the same visible sheet composition, element placement, and cross-reference presentation.
4. Rendered elements preserve canonical subject and occurrence identity so selection, reveal, and diagnostics can keep targeting the same engineering entities.
5. The sheet surface remains read-only presentation. It does not introduce local editing authority, renderer-owned semantics, or a new protocol/layout-stack decision.
6. The implementation ships with deterministic frontend/model tests that cover the new schematic sheet chrome and rendered element structure from local fixtures.
7. The story remains schematic-first and does not add cabinet preview, ecosystem expansion, or protocol selection work.

## Tasks / Subtasks

- [x] Extend the Theia workbench sheet model with schematic chrome facts (AC: 1, 2, 4)
  - [x] Surface sheet frame, title block, page grid, cross-reference, and active sheet summary data from the governed projection payload.
  - [x] Keep canonical ids and occurrence references attached to rendered subjects.
  - [x] Preserve the existing read-only projection boundary for the frontend model.
- [x] Render the sheet as a credible schematic surface in Theia (AC: 1, 2, 3)
  - [x] Update the workbench widget so the sheet surface visibly reads as a page with frame, title block, and governed element layering.
  - [x] Preserve labels, terminals, conductor paths, and cross-reference markers as presentation of governed facts.
  - [x] Keep the view deterministic for the same governed input state.
- [x] Prove deterministic schematic rendering from local fixtures (AC: 3, 4, 6)
  - [x] Add or update focused model tests for the workbench sheet chrome and element mapping.
  - [x] Add or update render or script-level tests for the visible schematic surface and its deterministic structure.
  - [x] Verify repeated runs keep the same visible sheet result and canonical identity mapping.
- [x] Keep the milestone bounded (AC: 5, 7)
  - [x] Do not add cabinet preview behavior.
  - [x] Do not choose or hard-code the final protocol/layout engine stack here.
  - [x] Do not move semantic authority into frontend-local state.

## Dev Notes

### Current State

- Theia already hosts the main customer-facing IDE surface for Athena.
- `athena-graph-workbench-model.ts` already normalizes `sheets`, `crossReferences`, `electricalAnchors`, `electricalConnectionEndpoints`, and `activeRenderContributions` from the LSP bridge.
- `athena-graph-workbench-widget.tsx` already renders the workbench shell, sheet-like viewport, grid, nodes, edges, terminals, selection panel, and sheet/session metadata.
- `style/index.css` already contains the workbench tokens and layout primitives that make the surface feel like an engineering canvas.
- The current surface is credible but still reads closer to a graph workbench than a professional schematic sheet.

### Architectural Guardrails

- Semantic authority stays upstream in Athena.
- The frontend remains projection-only and read-only.
- Schematic chrome must come from governed sheet and projection facts, not from DOM state or local reconstruction.
- Canonical subject and occurrence ids remain the identity path for selection, reveal, and diagnostics.
- Cabinet preview stays deferred from M19.
- Final protocol/layout-stack selection remains a separate tech-selector discussion.

### Project Structure Notes

- Likely update targets:
  - `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/style/index.css`
  - `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
  - `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`
- Keep the sheet chrome model grouped with the existing workbench model instead of creating one file per tiny DTO.

### Testing Requirements

- Use the repository-pinned stack only: Java 25, Kotlin 2.4.0, Gradle 9.6.1, and the existing Theia frontend toolchain.
- Run verification sequentially on Windows; never overlap `gradlew` invocations.
- Assert deterministic rendering structure, not just a successful mount.
- Keep the proof corpus small and governed.
- If text files change, run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 1, Story 1.3]
- [Source: `_bmad-output/implementation-artifacts/m19/1-1-establish-sheet-identity-and-page-structure.md` - current sheet contract baseline]
- [Source: `_bmad-output/implementation-artifacts/m19/1-2-project-schematic-content-into-sheet-ir.md` - projection-state publication contract baseline]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-2, FR-3, FR-6, FR-7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md` - Sheet IR, Layout pattern, Boundary Notes]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-3, AD-6, AD-9]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [Source: `ide/theia-frontend/src/browser/style/index.css`]
- [Source: `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`]
- [Source: `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`]
- [Source: `draft/layouts/001-disucss.md` - GLSP/Sprotty/ELK discussion]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 planning artifacts and the current Theia sheet-surface implementation baseline.
- Scoped to the IDE-facing schematic sheet surface and its governed chrome, not to cabinet preview or protocol selection.
- Red phase: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-graph-workbench-model.test.mjs scripts/athena-ide-density-contract.test.mjs }` failed because `sheetChrome` and sheet chrome markup/styles were not implemented.
- Green verification: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-graph-workbench-model.test.mjs scripts/athena-ide-density-contract.test.mjs }` passed 13 tests.
- Regression verification: `yarn test` in `ide/theia-frontend` passed 52 tests.
- Encoding verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added a governed `sheetChrome` model derived from projection payload `sheets`, `crossReferences`, and canvas dimensions.
- Rendered the workbench SVG inside a transformed schematic sheet surface with visible frame, grid, title block, and cross-reference markers.
- Preserved the frontend as read-only projection presentation; no cabinet preview behavior, protocol selection, or frontend-owned semantic resolution was added.
- Added deterministic model and source-level frontend tests for sheet chrome, identity mapping, and visible schematic surface structure.

### File List

- `_bmad-output/implementation-artifacts/m19/1-3-render-credible-schematic-elements.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`

### Change Log

- 2026-07-16: Implemented governed schematic sheet chrome and deterministic frontend tests for Story 1.3.
