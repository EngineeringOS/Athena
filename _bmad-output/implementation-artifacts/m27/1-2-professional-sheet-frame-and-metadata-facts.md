---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 1.2: Professional Sheet Frame And Metadata Facts

Status: done

## Story

As an engineer,
I want sheet frame, zones, margins, title block fields, and sheet metadata to come from governed
Presentation IR facts,
so that the rendered page looks professional while keeping `.athena` semantic source authoritative.

## Acceptance Criteria

1. M27 Presentation IR exposes governed sheet frame, coordinate-zone, margin, title-block, and sheet
   metadata facts for the M27 sample projection.
2. The Theia Graphical View renders those facts without owning page identity, page state, or hidden
   canvas truth.
3. The same source and projection inputs regenerate stable sheet frame and metadata facts across
   repeated builds.
4. The facts contain paint-ready presentation data only and do not introduce raw canvas coordinates
   as source truth.
5. Existing M24/M25/M26 route, representation, and document projection proof behavior remains
   intact for the M27 sample.

## Tasks / Subtasks

- [x] Model governed sheet-surface presentation facts (AC: 1, 3, 4)
  - [x] Identify whether existing sheet chrome types should move into `presentation-model` or be
        mapped from existing Presentation IR transport.
  - [x] Add explicit frame, zone, margin, title-block, and sheet metadata fields without moving
        document identity into canvas state.
  - [x] Keep coordinates paint-ready and projection-derived only.
- [x] Transport sheet-surface facts into Theia (AC: 1, 2, 5)
  - [x] Update the projection/GLSP/Theia adapter path so Theia consumes sheet-surface facts.
  - [x] Preserve current route, representation, and document projection payloads.
- [x] Render governed sheet frame and metadata in Graphical View (AC: 2, 5)
  - [x] Use existing Theia Graphical View surface only.
  - [x] Keep `AthenaGraphWorkbenchSheetFrame`, grid, title block, and selector behavior fact-driven.
  - [x] Do not touch deprecated desktop-viewer, Compose, or KMP frontend modules.
- [x] Add regression coverage (AC: 1, 2, 3, 4, 5)
  - [x] Verify deterministic sheet frame and metadata facts.
  - [x] Verify Theia proof payload exposes sheet-surface facts.
  - [x] Verify M27 sample smoke continues to pass.

## Dev Notes

- Story 1.1 created the M27 openable sample and proved the current Theia path opens it.
- Current codegraph findings show sheet chrome is already represented in Theia-side model types:
  `AthenaGraphWorkbenchSheetFrame`, `AthenaGraphWorkbenchSheetGrid`, and
  `AthenaGraphWorkbenchSheetTitleBlock` in
  `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`.
- Story 1.2 should reduce Theia-local authority by making those values explicit Presentation IR or
  projection facts before Theia renders them.
- Do not introduce new `.athena` syntax.
- Do not turn source files, document projection views, or canvas DOM state into sheet authority.
- Theia may render and inspect facts only.
- Verification must run sequentially on Windows.

### Likely Code Areas

- `kernel/presentation-model`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/src/athena-glsp-graph-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`

### Architecture Guardrails

- M27 AD-8: professional sheet surface is Presentation IR output.
- M27 AD-11: Theia remains a fact consumer.
- M27 AD-14: no new source syntax by default.
- M27 AD-15: Theia IDE is the only frontend scope.
- M27 AD-16: cleanup gate remains mandatory before milestone closure.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m27/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m27/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m27/1-1-openable-m27-sheet-proof.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-20: Resumed from red test proving Theia used canvas-derived sheet chrome before governed presentation sheet-surface facts.
- 2026-07-20: `yarn --cwd ide/theia-frontend test` initially failed because linked GLSP `lib` typings were stale; rebuilt `integrations/graph-glsp`.
- 2026-07-20: `yarn --cwd ide start:smoke:m27` initially failed with semantic STOP_DOWNSTREAM because the M27 sample contained a cross-file connection endpoint from `01-workspace-semantic-source.athena` to `FieldOutputModuleIOM1.do1`.
- 2026-07-20: Added `AthenaM27SampleProjectCompilerTest` and corrected the sample so active-editor projection sources compile independently before downstream rendering.
- 2026-07-20: Rebuilt the Theia product bundle before product smoke so Electron consumed the updated frontend `lib`.

### Completion Notes List

- Added explicit GLSP sheet publication and presentation sheet-surface contracts, including frame, margins, zones, grid, title-block fields, and metadata.
- Theia Graphical View now prefers governed `presentation.sheetSurface` facts and otherwise derives a paint-ready sheet surface from the active projection sheet publication; canvas dimensions remain fallback only.
- Sheet-surface proof is exposed as nonvisual attributes on the existing frame element, preserving the M21/M27 no-crowding UI contract.
- Corrected the M27 sample to avoid unsupported cross-file active-editor route projection and documented that workspace-level cross-file route projection is deferred.
- Verification passed: `yarn --cwd integrations/graph-glsp test`; `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM27SampleProjectCompilerTest`; `yarn --cwd ide/theia-frontend test`; `yarn --cwd ide build`; `yarn --cwd ide start:smoke:m27`; `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### File List

- `_bmad-output/implementation-artifacts/m27/1-2-professional-sheet-frame-and-metadata-facts.md`
- `_bmad-output/implementation-artifacts/m27/sprint-status.yaml`
- `examples/m27/sample-project/README.md`
- `examples/m27/sample-project/src/01-workspace-semantic-source.athena`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m27-sample-project.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.d.ts.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts.map`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM27SampleProjectCompilerTest.kt`

## Change Log

- 2026-07-20: Created Story 1.2 for governed professional sheet-surface facts.
- 2026-07-20: Implemented governed Theia sheet-surface facts and product smoke proof.
