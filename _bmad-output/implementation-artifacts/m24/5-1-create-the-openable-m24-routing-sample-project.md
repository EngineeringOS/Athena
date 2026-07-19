---
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
status: review
epic: 5
story: 5.1
title: Create the openable M24 routing sample project
---

# Story 5.1: Create the openable M24 routing sample project

As a product reviewer, I want a real M24 sample project, so that I can test routing fidelity through
the IDE.

## Acceptance Criteria

- `examples/m24/sample-project` exists with real `.athena` files.
- The sample includes PLC-HMI, PLC-terminal-load, 24V power/protection, and terminal-strip route
  cases.
- The sample opens without false syntax diagnostics.
- Graphical View projects the active M24 source.
- The sample is documented for customer-facing proof and does not require reading `.mjs` files.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`

## Notes

The sample is the product proof. Do not create docs that claim behavior the sample cannot show.

## Tasks/Subtasks

- [x] Create the M24 sample project with real `.athena` routing sources.
- [x] Add compiler and IDE/frontend regression coverage proving the sample opens cleanly.
- [x] Document the customer-facing proof path without requiring `.mjs` files.

## Dev Agent Record

### Debug Log

- 2026-07-19: Root-caused LSP false diagnostics to duplicate authored device names across M24 sample files in one package namespace; single-file compiler tests did not catch this, so project-semantic binding coverage was added.
- 2026-07-19: Verified focused LSP diagnostics, compiler sample coverage, and frontend sample/Graph Workbench suite after fixing the package-level sample identity.

### Completion Notes

- Created `examples/m24/sample-project` as a real IDE-openable Athena workspace with three `.athena` routing scenarios.
- Kept the sample customer-facing: README points reviewers to IDE usage and does not require reading `.mjs` files.
- Added compiler, LSP, and frontend regression coverage so the sample cannot silently regress into false diagnostics or stale assertions.

### File List

- examples/m24/sample-project/src/02-terminal-strip-routes.athena
- examples/m24/sample-project/src/01-control-route.athena
- examples/m24/sample-project/src/03-power-protection-load.athena
- examples/m24/sample-project/athena.yaml
- examples/m24/sample-project/athena.lock
- examples/m24/sample-project/README.md
- examples/m24/README.md
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM24SampleProjectCompilerTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- ide/theia-frontend/scripts/athena-m24-sample-project.test.mjs
- ide/package.json
- ide/theia-product/package.json
- _bmad-output/implementation-artifacts/m24/5-1-create-the-openable-m24-routing-sample-project.md
- _bmad-output/implementation-artifacts/m24/sprint-status.yaml

### Change Log

- 2026-07-19: Started story 5.1 and fixed project-level duplicate sample declaration diagnostics.
- 2026-07-19: Completed Story 5.1 sample project, tests, usage docs, and review-state tracking.
