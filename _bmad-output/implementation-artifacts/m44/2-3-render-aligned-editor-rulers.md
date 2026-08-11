---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 2.3: Render Aligned Editor Rulers

Status: done

## Story

As a design reviewer,
I want row and column coordinates rendered as aligned editor chrome,
so that the engineering sheet is clean, readable, and never distorted by canvas scaling.

## Acceptance Criteria

1. Given a READY Canonical Scene, when the Engineering Document renders, then a plain HTML horizontal
   `display: flex` ruler shows `1..columns`, a vertical flex ruler shows `A..rows`, and one corner cell
   joins them without gaps, overlap, uneven borders, or canvas-scale distortion.
2. Given desktop and narrow editor panels, when the editor viewport resizes, then the ruler stays pinned
   to the viewport's top and left edges, spans its complete width and height, uses a fixed small chrome
   thickness, and the style toolbar occupies its own row instead of covering labels.
3. Given Canonical Scene publication, when Konva paints it, then Konva paints only the drawing area and
   engineering content; it does not paint `COORDINATE_LABEL`, ruler-divider `FRAME_SEGMENT`, or an
   internal grid, and semantic, spatial, selection, drag, and style behavior remain unchanged.
4. Given the same scene row/column facts, when compilation and frontend tests run, then ruler labels are
   deterministic, rows beyond `Z` use spreadsheet-style labels, no ruler identity is inferred in the
   frontend, and all affected product surfaces rebuild and pass live screenshot proof.

## Tasks / Subtasks

- [x] Add failing frontend ruler layout tests (AC: 1, 2, 4)
  - [x] Prove the widget derives horizontal and vertical label arrays only from `scene.snapGrid.columns`
        and `scene.snapGrid.rows`.
  - [x] Prove the DOM has one corner cell, two flex rulers, bordered cells, and a separate toolbar row.
  - [x] Prove CSS uses a fixed small ruler size, no viewport padding/centering, and exact full-editor
        ruler/drawing alignment at desktop and narrow sizes.
- [x] Remove ruler paint from the Konva path (AC: 3)
  - [x] Add a failing adapter contract test rejecting coordinate-label and frame-segment paint branches.
  - [x] Fit Konva to `scene.page.drawingBounds`; preserve canonical coordinates and interaction behavior.
  - [x] Keep the drawing area white and internal grid hidden.
- [x] Retire compiler-owned ruler decorations (AC: 3, 4)
  - [x] Add a failing compiler test proving scene decorations no longer contain coordinate labels or
        ruler divider/frame segments.
  - [x] Keep one traceable page background decoration for default style targeting and scene provenance.
- [x] Implement plain HTML/CSS ruler chrome (AC: 1, 2, 4)
  - [x] Render numbered column cells, spreadsheet-letter row cells, and the empty corner cell.
  - [x] Use simple flex borders; do not add SVG, Canvas, Konva, half-pixel patches, or a second renderer.
  - [x] Pin the ruler to the viewport edges and give the canvas every remaining pixel.
- [x] Rebuild and prove the product (AC: all)
  - [x] Run focused compiler and frontend tests, sequential full Gradle test, frontend build, encoding audit,
        and source-set hygiene audit.
  - [x] Capture desktop and narrow Electron screenshots under M44 implementation artifacts.

## Dev Notes

### Architecture Guardrails

- Rulers are fixed editor-viewport chrome for future drag/crosshair coordinates, not Engineering,
  Projection, Spatial, or Presentation scene elements.
- The accepted scene supplies only `snapGrid.rows`, `snapGrid.columns`, `page.drawingBounds`, and existing
  canonical business paint facts. The frontend does not infer engineering meaning.
- Konva remains the one business-content renderer and stays disposable behind its adapter.
- No compatibility branch. Remove the wrong Konva ruler path instead of preserving it.

### Current Code Reality

- `AthenaDiagramSceneCompiler.decorations()` currently emits `COORDINATE_LABEL` and `FRAME_SEGMENT`
  decorations for the outer rulers. This is the defect source.
- `KonvaDiagramAdapter.draw()` paints those decorations and `applyFit()` fits the full page bounds, so
  browser layout, canvas scale, stroke placement, and floating controls cannot align reliably.
- `AthenaPresentationWidget.render()` currently renders only the canvas host plus an absolutely positioned
  style bar. The style bar covers horizontal ruler labels on narrower editor panels.
- `index.css` already gives the canvas shell the full center editor panel. Preserve that full-size behavior.

### Previous Story Intelligence

- Story 2.2 established disposable style preview and server-owned solidification. Do not change style
  authority, command payloads, source mutation, Source Revision, or publication behavior.
- Current product proof showed the style toolbar covering columns and scene-scaled ruler gaps. Rebuild the
  frontend bundle before judging screenshots.
- Preserve the large intentional pre-M42 deletion/refactor worktree. Do not restore compatibility files.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md` (`FR-6`)
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md` (`AD-1`, `AD-9`)
- Epics: `_bmad-output/planning-artifacts/m44/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m44/2-2-preview-discard-and-solidify-style-edits.md`
- Defect evidence: `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-accepted.png`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Story contexted through BMad create-story from M44 PRD, architecture, design, epics, sprint status,
  Story 2.2 intelligence, CodeGraph source analysis, current screenshot evidence, and git state.
- Red phase started with compiler and frontend contract tests for DOM-owned rulers and drawing-area fit.
- Frontend RED required the M44 proof harness to unmaximize, set, and verify distinct desktop and narrow
  Electron bounds; the original narrow capture remained maximized at the desktop size.
- First live proof exposed a size-dependent canvas digest baseline. Baseline capture moved after verified
  desktop sizing so preview/discard comparison uses identical canvas dimensions.
- Full Gradle regression exposed the retired M43 scene-owned ruler SVG golden. Its deterministic test and
  generator now consume the active M44 example and emit the ruler-free shared SVG contract.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Moved ruler ownership entirely to 20px HTML editor chrome: `1..columns` on top, spreadsheet row labels
  on the left, one corner cell, toolbar in its own row, and canvas filling the remaining editor panel.
- Removed coordinate labels and ruler frame segments from compiler output and Konva paint while retaining
  one traceable page background and all canonical interaction coordinates.
- Verified frontend 39/39, focused compiler and SVG renderer tests, sequential full Gradle test, full Theia
  build, Electron product proof, encoding audit, and source-set hygiene audit.
- Captured verified 1440x960 desktop and 760x720 narrow Electron evidence; PNG dimensions are 2880x1920
  and 1520x1440 respectively at the active device pixel ratio.

### File List

- `_bmad-output/implementation-artifacts/m44/2-3-render-aligned-editor-rulers.md`
- `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`
- `_bmad-output/planning-artifacts/m44/epics.md`
- `_bmad-output/implementation-artifacts/m44/operation-transcripts/2-2-style-authoring-product-proof.json`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-rulers-desktop.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-rulers-narrow.png`
- `contracts/presentation/v1/render/rolling-shutter.svg`
- `ide/theia-frontend/scripts/athena-editor-rulers.test.mjs`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/scripts/athena-product-layout.test.mjs`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/editor-rulers.ts`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-product/scripts/athena-m44-style-proof-main.js`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
- `kernel/svg-renderer/build.gradle.kts`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/svg/ActiveSceneSvgGoldenGenerator.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/svg/AthenaSvgRendererTest.kt`

### Change Log

- 2026-08-07: Story created via BMad create-story flow for aligned HTML editor rulers.
- 2026-08-07: Replaced scene-owned rulers with fixed HTML editor chrome, verified real desktop/narrow
  layouts, retired the stale M43 SVG ruler oracle, and completed all Story 2.3 regression evidence.
