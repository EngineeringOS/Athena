# Story 3.1: Publish And Render Presentation Reality

Status: review

## Story

As an engineer,
I want one live framed document surface,
so that I can inspect grid, title block, occurrences, labels, terminals, and routes in Theia.

## Acceptance Criteria

1. LSP publishes immutable, revision-consistent Projection and Spatial facts needed by the document
   surface, including sheet frame, grid, title metadata, occurrence bounds, labels, and routes.
2. Theia renders one Presentation Reality through semantic SVG and dense Canvas layers; both layers
   consume the same payload revision and occurrence identities.
3. The page is visibly nonblank and framed at desktop and mobile viewport sizes; grid and title block
   remain readable without overlap.
4. SVG exposes selectable occurrence identities and Canvas paints only compiler-provided geometry;
   frontend does not infer engineering meaning or routes.
5. Invalid or unavailable runtime payload renders diagnostics/empty state and never guessed geometry.
6. Focused LSP/frontend tests and frontend build pass.

## Tasks / Subtasks

- [x] Task 1: Publish full Spatial presentation payload through LSP (AC: 1, 5).
  - [x] Serialize immutable sheet geometry, grid, title metadata, occurrences, anchors, and routes.
  - [x] Keep Projection/Spatial authority in runtime/compiler; no frontend derivation.
- [x] Task 2: Add hybrid Presentation Reality Theia widget (AC: 2-4).
  - [x] Render semantic SVG occurrences and Canvas grid/routes from one payload.
  - [x] Keep stable dimensions and responsive viewport behavior.
  - [x] Render explicit unavailable/diagnostic state.
- [x] Task 3: Add focused protocol/widget tests and build frontend (AC: 5-6).

## Dev Notes

- Use existing `athena/projectionSession` LSP request and `AthenaLspEditorBridgeService`; existing
  semantic selection service remains source navigation authority.
- Extend current payloads instead of inventing a second transport. Spatial facts are already present
  in runtime `SpatialDocument` and must be serialized, not recomputed in TypeScript.
- Register widget through `athena-frontend-module.ts` and expose command/menu through existing
  `athena-workbench-extensions.ts` patterns.
- SVG semantic nodes need `data-occurrence-id`; Canvas is a paint layer only. Do not use old graph
  widget as document renderer. No PDF/export, Pattern/Macro, AI chat, or compatibility fallback.
- Keep TypeScript normal and source UTF-8. Run frontend `yarn build` after changes.

### References

- [Source: _bmad-output/planning-artifacts/m43/epics.md#Epic 3 - Live Hybrid Theia Document Surface]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#AD-5---one-presentation-reality-feeds-two-paint-mechanisms]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt]
- [Source: kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `:ide:lsp:test` passed.
- `yarn build` passed in `ide/theia-frontend`.

### Completion Notes List

- Extended `athena/projectionSession` payload with publication metadata and compiler-owned Spatial
  geometry, anchors, and routes.
- Added `AthenaPresentationWidget` with one SVG semantic layer and one Canvas dense layer.
- Registered document surface in Theia workbench with unavailable/diagnostic fail-closed state.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
- `ide/theia-frontend/src/browser/style/index.css`

### Change Log

- 2026-08-05: Published Presentation Reality payload and added hybrid Theia document surface.
