---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 4.1: Produce Shared Scene Export Evidence

Status: done

## Story

As an integrator,
I want canvas, SVG, and PNG evidence to consume the same Canonical Scene,
so that export does not become a second rendering truth.

## Acceptance Criteria

1. Fixed M44 source, Sheet, Style, lock, package, compiler, schema, profile, and source-root inputs
   produce one Canonical Scene digest and exact repeatable SVG bytes.
2. PNG captures use the existing Theia/Konva product surface and record OS, Electron/Chromium,
   viewport, DPR, fonts, and color profile.
3. Export evidence is stored only under `_bmad-output/implementation-artifacts/m44/exports/` and
   references the same scene digest used by the live canvas.

## Tasks / Subtasks

- [x] Add deterministic SVG export proof from `AthenaSvgRenderer` and canonical scene.
- [x] Add pinned PNG capture metadata and pixel-tolerance comparison.
- [x] Verify repeated scene/SVG/PNG proof and store M44 export evidence.
- [x] Run focused renderer, LSP, frontend, and product export validations.

## Dev Notes

- `AthenaSvgRenderer` is the only SVG authority; do not add a second scene compiler or renderer.
- Konva remains disposable product adapter. PNG is raster evidence, not a byte-determinism claim.
- Keep active example untouched; proof uses a temporary M44 repository copy.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Loaded M44 PRD, architecture spine, Epic 4, existing SVG renderer tests, product proof scripts,
  and M44 artifact conventions through BMad story flow.

### Completion Notes List

- `:kernel:svg-renderer:test` passes.
- Shared-scene export verifier passes; exact SVG repeat and pinned PNG metadata stored under M44
  `exports/`.
- Live product proof supplies the scene digest and Konva canvas captures; active example remains untouched.

### File List

- `_bmad-output/implementation-artifacts/m44/4-1-produce-shared-scene-export-evidence.md`
- `ide/theia-product/scripts/verify-athena-m44-export.js`
- `ide/theia-product/package.json`
- `_bmad-output/implementation-artifacts/m44/exports/m44-shared-scene-export-proof.json`

### Change Log

- 2026-08-08: Created through BMad story flow.
- 2026-08-08: Added deterministic SVG/PNG shared-scene evidence and moved to review.
