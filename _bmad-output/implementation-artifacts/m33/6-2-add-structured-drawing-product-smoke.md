---
story_id: 6.2
story_key: 6-2-add-structured-drawing-product-smoke
epic: 6
status: review
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 6.2: Add Structured Drawing Product Smoke

## Status

Review

## Story

As a maintainer, I want a structured product smoke so demo correctness is not screenshot-only.

## Acceptance Criteria

- Smoke proves package resolution, symbol anatomy, Graphic Primitive IR, sheet composition, route
  anchors, text placement, bounds, viewBox, and no fallback rendering.
- Smoke fails on visible wrapper borders, duplicate labels, off-screen duplicates, dead Create
  Device, and hidden sheet navigation.
- Smoke reports exact failed authority.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add a RED M33 Cabinet product smoke that requires package, anatomy, Graphic Primitive IR,
  composition, route-anchor, text, bounds, and content-derived viewBox proof.
- [x] Integrate frame, title block, zones, rails, lanes, terminal-strip grouping, label bands, route
  channels, and composition-owned reference marker into the live Cabinet payload.
- [x] Make the active Cabinet Workbench consume derived composition bounds and structure without
  visible wrapper chrome or legacy fixed sheet sizes.
- [x] Reject generic fallback, center-anchor fallback, duplicate labels, off-screen duplicates,
  hidden Cabinet navigation, and dead Create Device with exact failed-authority output.
- [x] Run focused compiler/LSP/GLSP/frontend tests and the rebuilt real product smoke.
- [x] Complete three-layer adversarial review, AC-to-evidence mapping, and deep polish/purge.

## Dev Notes

- Bind to M33 AD-10.
- Do not mark full E2E clean unless this smoke exits 0.

## Testing

- Product smoke script.
- Negative fixture assertions where practical.

## Evidence Plan

- Smoke output and proof JSON paths recorded in story.

## Polish And Purge

Remove obsolete M32 smoke assumptions or version them as compatibility checks.

## Dev Agent Record

### Implementation Plan

- Add a first-class M33 product smoke verifier that fails by `failedAuthority` instead of screenshot-only assertions.
- Render live Cabinet drawing composition facts into the SVG layer before component/route rendering.
- Keep Cabinet as the only product surface while hidden compatibility views remain accounted for.
- Open the source document for LSP diagnostics without reopening the Outline panel.

### Debug Log

- RED: `node --test scripts\athena-m33-product-smoke-contract.test.mjs` failed when zero-diagnostic evidence was split across Electron output chunks.
- GREEN: `hasZeroDiagnosticPublication` now scans joined and whitespace-compacted output, preserving source-specific matching.
- RED: M33 product smoke contract failed because M33 skipped Outline and did not explicitly open the source for LSP diagnostics.
- GREEN: Electron launcher now collects `sourceDiagnosticActivationProof`; verifier requires it and the rebuilt product smoke exits 0.

### Completion Notes

- Product smoke now proves package-backed Cabinet rendering, drawing composition layer, route anchors, no wrapper borders, no fallback representations, no duplicate semantic render occurrences, visible Cabinet-only navigation, Create Device panel reachability, source diagnostic activation, and real screenshot creation.
- Screenshot evidence is now split for Story 6.3 at `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-desktop.png` and `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-narrow.png`.
- Visual review note: screenshot proves real rendering but the Cabinet drawing is still too small for customer-demo quality. That is a Story 6.3/7.2 visual polish item, not a structured-smoke blocker.

### Three-Layer Adversarial Review

- Blind Hunter: no SVG/DOM authority is accepted as proof; smoke consumes drawing composition, representation package ids, descriptor ids, graphic resource ids, anchor maps, and failed-authority checks.
- Edge Case Hunter: guards cover visible wrapper borders, generic fallback, center-anchor fallback, duplicate semantic ids, off-screen duplicates, hidden Cabinet navigation, dead Create Device, source diagnostic activation, and non-orthogonal/body-crossing routes.
- Acceptance Auditor: all ACs have command-backed evidence below; residual visual-density concern is recorded and deferred to visual evidence/purge stories.

### AC-To-Evidence

- AC1: `yarn start:smoke:m33` emits `ATHENA_M33_PROFESSIONAL_DRAWING_PROOF` with package ids, descriptor ids, graphic resource ids, drawing layer kinds, route anchors, bounds, viewBox, and empty fallback ids.
- AC2: `assertProfessionalDrawingProof` rejects wrapper borders, duplicate semantic ids, off-screen duplicate ids, center fallback routes, hidden/missing Cabinet navigation, and dead Create Device states.
- AC3: verifier failures call `fail(failedAuthority, ...)`; focused smoke guards proved the failed-authority path.
- AC4: this Dev Agent Record records AC evidence and polish/purge notes before review.

### Verification

- `node --test scripts\athena-m33-live-cabinet-composition.test.mjs` passed: 3/3.
- `node --test scripts\athena-m33-product-smoke-contract.test.mjs` passed: 3/3.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM33CabinetProjectionSmokeTest` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist` passed.
- `yarn build` from `ide` passed.
- `yarn start:smoke:m33` from `ide/theia-product` passed after rebuild.

## File List

- `ide/theia-frontend/scripts/athena-m33-live-cabinet-composition.test.mjs`
- `ide/theia-frontend/scripts/athena-m33-product-smoke-contract.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m33-sample-project.js`
- `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-desktop.png`
- `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-narrow.png`

## Change Log

- 2026-07-23: Added structured M33 Cabinet product smoke, live drawing layer rendering, failed-authority checks, source diagnostic activation proof, and rebuilt Electron smoke evidence.
