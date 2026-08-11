---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 4.1: Prove Rolling-Shutter Editable Document

Status: done

## Story

As a reviewer, I can open the M43 rolling-shutter project and inspect, select, move, trace, and
connect on a clean live page, so milestone value is demonstrated on the real product surface.

## Acceptance Criteria

1. M43-local same-basename project source and Sheet Companion open explicitly in Theia.
2. Live page shows clean frame, numeric top border, alphabetic left border, symbols, labels, Ports,
   and routes without interior grid or bottom table; READY scene identity and source trace remain
   inspectable through product behavior.
3. Product proof selects a visible occurrence and records stable occurrence/trace identity with source
   editor evidence.
4. Existing validated move/lock and Port command contracts are covered by passing affected tests and
   the active M43 proof evidence; invalid direction and stale revision remain fail-closed.
5. Desktop and narrow screenshots plus scene/SVG/PNG proof evidence live under the M43 artifact
   directory only.
6. Sequential frontend build, affected Gradle tests, root regression, encoding, and source-set
   hygiene checks pass.

## Tasks / Subtasks

- [x] Run BMad implementation discovery and mark story in-progress.
- [x] Verify M43 example contract: colocated source/Sheet Companion, `grid: 17 * 16 cell: 4`, A1
      placements, no macro syntax.
- [x] Update Electron product proof to wait for READY canonical scene, use trusted input for selection,
      and record scene revision, digest, counts, canvas pixels, and source editor trace evidence.
- [x] Run proof at desktop and narrow viewports; write screenshots and `m43-product-proof.json` under
      `_bmad-output/implementation-artifacts/m43`.
- [x] Run affected frontend/LSP/compiler tests and required sequential build/audits.
- [x] Complete Dev Agent Record, File List, Change Log, mark story `review`, and update M43 sprint.

## Dev Notes

### Architecture Guardrails

- M43 scene/publication contracts are normative. Proof consumes product-published scene metadata; it
  must not reconstruct Projection or Spatial DTOs.
- Konva is sole live interaction owner. Product proof must exercise the rendered canvas through real
  Electron input; no DOM SVG selector or synthetic adapter state is authoritative.
- READY is the only mutation-enabled publication state. Invalid direction, stale revision, missing
  companion, and malformed source must remain rejected or unavailable without guessed layout.
- `rolling-shutter.athena` and `rolling-shutter.sheet.athena` stay colocated under the M43 example;
  do not reuse older milestone examples or proof paths.

### Existing Implementation To Adopt

- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx` publishes closed scene metadata.
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts` owns paint, hit, pan, zoom,
  selection, and command enablement.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramAuthoringService.kt` owns
  revision-checked move/connect validation and source edits.
- `ide/theia-product/scripts/verify-athena-m43-product-proof.js` owns viewport evidence and artifact
  assertions; `athena-m43-proof-main.js` owns Electron lifecycle and browser evidence.

### Required Evidence

- `m43-rolling-shutter-desktop-1920x1080.png`
- `m43-rolling-shutter-mobile-720x900.png`
- `m43-product-proof.json`
- Passing `RollingShutterLiveDocumentProofTest` plus affected LSP/frontend contract tests.

## References

- `_bmad-output/planning-artifacts/m43/epics.md` Epic 4, Story 4-1
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md` FR-10..FR-12 and UJ-1..UJ-4
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md`
- `_bmad-output/implementation-artifacts/m43/3-4-refresh-publication-states-atomically.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

Product proof passed after rebuild. Earlier selector assumptions failed because Theia Explorer still
rendered `NO FOLDER OPENED` while Repository Graph and canonical scene were ready; proof now uses
normalized repository evidence and shell metadata. Synthetic DOM clicks did not exercise Konva hit
testing reliably; trusted Electron mouse input now selects real occurrence nodes. Final proof also
waits for the Monaco trace surface, scans all Konva canvas layers, ignores transparent pixels, and
uses full center-panel bounds after bottom-dock/startup-layout correction.

### Implementation Plan

1. Make product proof wait on canonical READY scene and normalize repository evidence.
2. Use trusted Electron pointer input to select a real Konva occurrence and verify source trace state.
3. Add LSP proof for accepted projection-ID move, invalid direction, and stale revision rejection.
4. Rebuild product and run desktop/narrow proof plus sequential regression/audits.

### Completion Notes List

- M43 rolling-shutter source and same-basename Sheet Companion open and compile as one READY scene.
- Desktop and narrow product captures show nonblank clean-page rendering with 8 occurrences and 7
  routes; both proof selections resolve to occurrence IDs and stable trace IDs.
- LSP command test verifies accepted move WorkspaceEdit, `relationship.direction.illegal`, and
  `STALE` rejection. Move service now maps projection occurrence IDs to authored Sheet names and
  validates Sheet Companion text without compiling companion text as project source.
- `yarn workspace @engineeringood/athena-theia-frontend contracts:check` and frontend tests pass
  (27/27); frontend/product builds, product proof, `:ide:lsp:test`, root `test`, encoding audit,
  and source-set hygiene audit pass sequentially.

### File List

- `_bmad-output/implementation-artifacts/m43/4-1-prove-rolling-shutter-editable-document.md`
- `_bmad-output/implementation-artifacts/m43/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m43/m43-product-proof.json`
- `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-mobile-720x900.png`
- `ide/theia-product/scripts/athena-m43-proof-main.js`
- `ide/theia-product/scripts/verify-athena-m43-product-proof.js`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/scripts/athena-product-layout.test.mjs`
- `ide/theia-frontend/scripts/athena-presentation-layout.test.mjs`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramAuthoringService.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`

### Change Log

- 2026-08-06: Created through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-06: Implemented trusted Electron selection proof, M43 command boundary regression proof,
  corrected full center-panel/top-anchor proof behavior, and recorded sequential product evidence;
  status `done`.
