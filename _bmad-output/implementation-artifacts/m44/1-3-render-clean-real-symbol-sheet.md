---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 1.3: Render Clean Real-Symbol Sheet

Status: done

## Story

As a design reviewer,
I want Theia to show real admitted symbol geometry on a clean sheet,
so that the canvas looks like an engineering document rather than debug rectangles.

## Acceptance Criteria

1. Given the active M44 rolling-shutter example compiles with admitted assets and traceable
   occurrences, when the Theia canvas opens the sheet, then every engineering occurrence renders its
   admitted SVG geometry, labels, ports, routes, and selection trace without visible placeholder
   rectangles.
2. Given the scene contains `SceneAsset` entries and an `AssetBundle`, when the renderer resolves assets,
   then it uses the bundle entry digest and media kind, places geometry at the occurrence bounds, and
   never fetches `reference/elements`, `reference/elements_contrib`, or another filesystem path.
3. Given the sheet contains page decorations, when the canvas is fit to the editor viewport, then the
   white page, outer frame, row letters, and column numbers are visible, the drawing area is clean, and
   internal grid lines, bottom tables, and debug decorations are absent by default.
4. Given a scene occurrence, port, or label is selected, when the user clicks it, then the visible
   selection overlay does not become engineering geometry and the callback exposes the published
   `semanticId`, `occurrenceId`, and `traceId`.
5. Given the publication is `UNAVAILABLE`, the asset bundle is missing, or an asset digest cannot be
   resolved, when the canvas receives the publication, then it shows no guessed symbol or partial scene
   and exposes the publication diagnostic/unavailable state.
6. Given the same ready publication is rendered repeatedly, when the viewport and device scale are held
   constant, then visible geometry, frame/rulers, labels, ports, and route styling remain deterministic
   and no object URL or Konva node leak remains after `clear()`/`dispose()`.

## Tasks / Subtasks

- [x] Add failing frontend tests for real-asset rendering and clean-sheet composition (AC: 1, 2, 3, 5)
  - [x] Build a minimal ready `AthenaScenePublication` with one SVG asset, one occurrence, ports,
        labels, one route, and frame/ruler decorations.
  - [x] Assert SVG asset bytes are loaded from `AssetBundle` by `bundleEntryId` and digest, not from a
        reference directory or a hard-coded path.
  - [x] Assert visible paint contains the SVG image and semantic hit target, but no visible occurrence
        placeholder rectangle.
  - [x] Assert unavailable/missing-bundle or unresolved-asset publication clears the scene without
        partial geometry.
  - [x] Assert frame/rulers render while internal grid and bottom-table/debug decorations do not.
- [x] Implement asset-backed Konva occurrence rendering (AC: 1, 2, 4, 6)
  - [x] Extend the existing `KonvaDiagramAdapter`; do not add a second renderer or scene authority.
  - [x] Resolve `SceneAsset.bundleEntryId` against the publication `AssetBundle`, verify digest and
        media kind, decode SVG bytes, and cache/revoke object URLs per accepted input revision.
  - [x] Render SVG image geometry at occurrence bounds; use transparent semantic hit targets only for
        selection/drag interaction.
  - [x] Preserve published `semanticId`, `occurrenceId`, `traceId`, port ids, labels, and routes.
  - [x] Keep page/frame/ruler paint in the page layer, symbols/routes/labels in content layers, and
        selection overlays in interaction layer.
- [x] Enforce clean sheet framing and deterministic fit (AC: 3, 6)
  - [x] Render only `PAGE_BACKGROUND`, `FRAME_SEGMENT`, and `COORDINATE_LABEL` decorations from the
        scene contract.
  - [x] Keep internal grid lines hidden unless a future interaction projection explicitly publishes
        them; do not synthesize grid paint from snap-grid data.
  - [x] Fit the full page to the available center editor viewport without changing source or scene facts.
  - [x] Ensure repeated publication and resize/clear/dispose cycles do not accumulate Konva nodes,
        object URLs, listeners, or stale image callbacks.
- [x] Preserve frontend authority boundary (AC: all)
  - [x] Frontend paints and previews only; it must not resolve engineering relationships, validate port
        direction/flow, select Parts, or mutate source.
  - [x] Selection and drag callbacks carry published ids only; source mutation remains later typed
        operation work.
- [x] Run sequential validation (AC: all)
  - [x] `yarn workspace @engineeringood/athena-theia-frontend contracts:generate`
  - [x] `yarn workspace @engineeringood/athena-theia-frontend contracts:check`
  - [x] `yarn workspace @engineeringood/athena-theia-frontend build`
  - [x] `yarn workspace @engineeringood/athena-theia-frontend test`
  - [x] `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaDiagramSceneCompilerTest`
  - [x] `.\gradlew.bat --no-daemon --console=plain test`
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`

## Dev Notes

### PRD / Architecture Guardrails

- M44 is the first complete engineering authoring transaction loop, but this story only delivers the
  readable real-symbol projection slice.
- `AD-1`: `AthenaDiagramScene` is the renderer-neutral scene authority. Konva is disposable adapter state.
- `AD-3`: SVG and Symbol metadata provide geometry and compatibility only. Engineering source owns Port
  identity, direction, domain, flow kind, and semantic properties.
- `AD-9`: Konva Canvas2D is the only M44 interactive backend. Do not add GLSP, SVG DOM interaction,
  tldraw, Excalidraw, draw.io, Graphite, WebGL, or WebGPU runtime paths.
- `AD-13`: Keep viewport culling and asset caching, but never meet performance by hiding real geometry,
  labels, ports, or source trace.
- `FR-6`: clean frame/rulers are required; default internal grid is hidden; no bottom-table/debug clutter.

### EPLAN Lesson Applied

- Symbol is a library representation, not Entity or Function identity.
- Function-level occurrence selection must remain traceable to source.
- Route paint is not Relationship truth; this renderer consumes already compiled routes and does not
  infer or edit relationships.
- Library bytes are governed package assets, not copied project SVG files.

### Current Code Reality

- `KonvaDiagramAdapter` already owns one Konva stage with page, content, and interaction layers, asset
  bundle loading, viewport culling, selection metadata, and disposal. Extend those paths.
- Current `drawOccurrence` still paints a visible `Konva.Rect` as the occurrence. Replace visible
  placeholder paint with the admitted SVG image while retaining a transparent hit target.
- Current `loadAssets` creates `Konva.Image` nodes asynchronously. Guard stale input revisions and clear
  old image nodes before repaint; revoke all object URLs on revision change, `clear()`, and `dispose()`.
- Generated TypeScript contracts under
  `ide/theia-frontend/src/browser/diagram/generated/` are compiler-owned output. Never hand-edit them;
  regenerate through `scripts/generate-diagram-contracts.mjs`.
- Frontend tests are plain Node scripts under `ide/theia-frontend/scripts/`; follow existing adapter and
  contract test patterns. No new dependency without explicit approval.

### Asset Contract

- `SceneAsset.assetId` identifies scene asset; `bundleEntryId` identifies bytes in `AssetBundle`.
- `SceneAsset.digest` and `AssetBundle` entry digest must match before image creation.
- SVG media type is `image/svg+xml`; PNG is `image/png`; WOFF2 is font-only and not an occurrence image.
- Object URLs are runtime cache state only. They never enter `AthenaDiagramScene`, source, or operation
  contracts.

### Layout And Visual Contract

- Page background is white.
- Outer row/column ruler cells and frame segments come from scene decorations.
- Drawing area remains visually blank except real symbols, labels, ports, and routes.
- Do not draw full internal grid from `snapGrid`; snap data remains interaction/compiler input.
- Do not add debug counters, dense border boxes, bottom tables, or placeholder rectangles.
- Fit calculation uses full `page.pageBounds` against host center-editor dimensions and remains stable on
  resize.

### Testing Requirements

- Test both successful SVG load and failure/cleanup paths.
- Test a function occurrence with `semanticId` and `traceId`, not only an Entity-level placeholder.
- Test asset revision replacement does not leave old image/object URL or old scene paint.
- Test `clear()` and `dispose()` are idempotent.
- Run Gradle commands sequentially. Never run two Gradle tasks concurrently.
- Production source must not gain `M44`, `Demo`, `Proof`, `Sample`, `V0`, or `V1` class names.

### Previous Story Intelligence

- Story 1.2 established explicit `semanticId`, Function-first Source Trace, `representationRef`, and
  fail-closed Symbol port authority conflict.
- Story 1.2 moved compiler/contract active proof from `examples/m43` to
  `examples/m44/rolling-shutter`. Keep all new evidence under M44 paths.
- Story 1.1 fixed strict `symbol.yaml` / `athena-symbol-v1` admission. Do not add another descriptor
  parser or runtime dependency on reference catalogs.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md`
- PRD review: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/review-final-alignment.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/planning-artifacts/m44/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m44/1-2-compile-traceable-function-symbol-occurrences.md`
- Active example: `examples/m44/rolling-shutter`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Story contexted from M44 Epic 1 Story 1.3 after Story 1.2 review status.
- Existing Konva adapter and generated scene contract inspected before story creation.
- Must preserve renderer-neutral scene and published identity fields; visible occurrence Rect is known
  placeholder behavior to replace.
- RED: `yarn workspace @engineeringood/athena-theia-frontend test` from repo root failed because Yarn
  workspace root is `ide`; reran from `ide`.
- RED: `yarn workspace @engineeringood/athena-theia-frontend test` from `ide` failed on
  `real symbols render from admitted asset bytes, not visible placeholder rectangles` because adapter still
  used visible `Konva.Rect` occurrence placeholder paint.
- GREEN: extended `KonvaDiagramAdapter` with route, asset, content, and interaction layer separation;
  digest-checked `AssetBundle` image loading; transparent occurrence hit targets; object URL/image cache
  cleanup; and no dense placeholder batching for asset-backed occurrences.
- Validation commands passed sequentially:
  - `yarn workspace @engineeringood/athena-theia-frontend contracts:generate`
  - `yarn workspace @engineeringood/athena-theia-frontend contracts:check`
  - `yarn workspace @engineeringood/athena-theia-frontend build`
  - `yarn workspace @engineeringood/athena-theia-frontend test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaDiagramSceneCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Theia Konva adapter now renders asset-backed occurrence geometry from admitted publication bytes instead
  of visible placeholder rectangles.
- Occurrence interaction remains a transparent hit target carrying published `semanticId`, `occurrenceId`,
  and `traceId`.
- Clean sheet paint remains page/frame/ruler decorations plus published symbols, routes, ports, labels,
  and selection overlays. Internal grid remains hidden by default.
- Asset loading verifies `SceneAsset.digest` against `AssetBundle` entry digest and decoded bytes, clears
  failed publications, and revokes runtime object URLs on revision change, clear, and dispose.

### File List

- `_bmad-output/implementation-artifacts/m44/1-3-render-clean-real-symbol-sheet.md`
- `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`

### Change Log

- 2026-08-07: Story created via BMad create-story flow for M44 Story 1.3.
- 2026-08-07: Implemented asset-backed Konva occurrence paint, clean layer separation, digest-checked
  bundle loading, and cleanup validation.
