---
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
status: review
epic: 5
story: 5.3
title: Add M24 Electron smoke and route regression coverage
---

# Story 5.3: Add M24 Electron smoke and route regression coverage

As an Athena maintainer, I want product-path smoke tests for M24, so that route rendering does not
pass only in unit tests.

## Acceptance Criteria

- Product smoke opens `examples/m24/sample-project` in Theia.
- Smoke proves terminal-anchor route rendering exists.
- Smoke proves no center-to-center fallback is used in the accepted proof.
- Failures include actionable route/projection state.
- The installed LSP host is rebuilt or confirmed current before product smoke claims success.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Carry forward the M23 stale installed-LSP lesson.

## Tasks/Subtasks

- [x] Add route-specific DOM proof emitted by the product smoke harness.
- [x] Add an M24 Electron smoke script for `examples/m24/sample-project`.
- [x] Add package scripts and regression checks for the M24 smoke entry point.
- [x] Verify the installed LSP host before claiming product smoke success.

## Dev Agent Record

### Debug Log

- 2026-07-19: Started from existing M21-M23 Electron smoke pattern; extending the shared open-workspace harness with route-specific proof state.
- 2026-07-19: Product smoke passed with route proof: routeCount=1, terminalCount=2, labelCount=1, routesWithTerminalAnchors=1, routesWithOrthogonalBends=1, centerFallbackRouteIds=[].
- 2026-07-19: Verified installed LSP host with `:ide:lsp:installDist`; `yarn --cwd ide build` confirmed the product bundle and dev runtime before smoke.

### Completion Notes

- Added route-specific data attributes to rendered route paths, labels, and terminals so product smoke can inspect governed route facts instead of screenshots.
- Extended the shared Electron workspace proof payload with `routeProof`.
- Added `verify-athena-m24-sample-project.js` and `start:smoke:m24` scripts.
- Verified the M24 sample opens in Electron and reports terminal-anchor routing with no center fallback ids.

### File List

- ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m24-sample-project.js
- ide/theia-frontend/scripts/athena-m24-sample-project.test.mjs
- ide/package.json
- ide/theia-product/package.json
- _bmad-output/implementation-artifacts/m24/5-3-add-m24-electron-smoke-and-route-regression-coverage.md
- _bmad-output/implementation-artifacts/m24/sprint-status.yaml

### Change Log

- 2026-07-19: Started Story 5.3 and added M24 route-specific Electron smoke proof plumbing.
- 2026-07-19: Completed Story 5.3 Electron smoke, route DOM proof, package scripts, and verification.
