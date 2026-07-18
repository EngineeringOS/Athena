---
story_id: 5.2
story_key: 5-2-add-m23-end-to-end-smoke-proof
epic: 5
epic_title: Sample Proof, Usage, And Boundary Guardrails
title: Add M23 end-to-end smoke proof
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 5.2: Add M23 End-To-End Smoke Proof

## Story

As a product reviewer,
I want automated smoke coverage for the M23 sample,
So that future parser or IDE regressions are caught before manual testing.

## Acceptance Criteria

**Given** the M23 sample project
**When** smoke tests run
**Then** compiler, LSP, and Graphical View paths accept the sample
**And** test output identifies the exact sample file under test
**And** failures are actionable without inspecting unrelated `.mjs` implementation details

## Developer Context

Story 5.1 created the openable sample. Story 5.2 adds executable evidence that the sample is accepted
by the compiler, LSP diagnostics path, and Theia Graphical View smoke harness.

## Architecture Guardrails

- Keep compiler/LSP checks source-driven.
- Smoke script must open `examples/m23/sample-project`, not a helper-only fixture.
- Do not claim manual IDE proof until the script or IDE launch is actually run.

## Tasks/Subtasks

- [x] Add failing tests for M23 compiler/LSP/smoke proof coverage.
- [x] Add M23 compiler sample-project test.
- [x] Add M23 LSP sample-project diagnostics test.
- [x] Add M23 Theia sample-project smoke script.
- [x] Run compiler/LSP/frontend checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 5.2 from backlog after Story 5.1 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Added compiler, LSP, frontend manifest, and Electron smoke coverage for `examples/m23/sample-project/src/01-layout-hints.athena`.
- 2026-07-18: First Electron smoke failed with `Projection unavailable: extraneous input 'layout'`, proving the product was using a stale installed LSP host rather than the updated source parser.
- 2026-07-18: Ran `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`; reran the same Electron smoke and received `ATHENA_GRAPH_WORKBENCH_PROOF={...}` with every required DOM proof key set to `true`.

### Completion Notes

- Added checked-in M23 smoke proof coverage for the compiler, LSP diagnostics path, frontend sample wiring, and Theia Graphical View DOM proof.
- Improved the Electron workspace opener failure evidence so future projection-empty failures report the rendered Graph Workbench state instead of only timing out on the viewport.

### File List

- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM23SampleProjectCompilerTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs`
- `ide/theia-product/scripts/verify-athena-m23-sample-project.js`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`

### Change Log

- 2026-07-18: Added M23 compiler/LSP/sample smoke proof and actionable Graph Workbench smoke diagnostics.

## Status

Review.
