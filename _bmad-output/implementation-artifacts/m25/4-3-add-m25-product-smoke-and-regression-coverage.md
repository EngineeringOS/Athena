---
baseline_commit: 616271a8721cf9fd6538bf8823eaf57a0392074a
status: done
epic: 4
story: 4.3
title: Add M25 product smoke and regression coverage
---

# Story 4.3: Add M25 product smoke and regression coverage

## Story

As a developer,
I want product-path tests for M25,
So that Theia proof failures are caught before user review.

## Acceptance Criteria

- Product smoke verifies rendered representation facts, terminal facts, label facts, route
  attachments, zero fallback symbols, and active-source behavior.
- M24 route quality regression checks still pass.
- Verification commands are documented.
- Gradle verification is not run concurrently.

## Tasks/Subtasks

- [x] Add M25 product smoke test.
- [x] Add regression checks for M24 route quality and M25 zero fallback.
- [x] Add script/package wiring consistent with prior milestone pattern.
- [x] Document commands and evidence.

## Dev Notes

- Governed by AD-2, AD-7, AD-9.
- Follow repo Windows Gradle rule: sequential Gradle only.

## Dev Agent Record

### Debug Log

- 2026-07-19: Added M25 smoke wiring and reproduced product smoke failure with `representationCount=0`, `presentationTerminalCount=0`, and `presentationLabelCount=0`.
- 2026-07-19: Root cause found in Theia model path: `buildWorkbenchNodeFromPresentation` dropped M25 representation facts when `presentation.occurrences` drove the sheet.
- 2026-07-19: Added a regression that failed before the fix: Presentation IR occurrence nodes must keep M25 representation, terminal, and label facts.
- 2026-07-19: Corrected the M25 smoke assertion to match governed transport role serialization: `device_tag`.

### Completion Notes

- Added product-path M25 smoke script and package wiring for `yarn --cwd ide start:smoke:m25`.
- Product smoke verifies rendered representation facts, presentation terminal facts, presentation label facts, route attachments, zero fallback symbols, and expected sample semantic ids.
- Preserved M24 product route smoke as a regression proof.
- Fixed Theia occurrence-driven sheet model so representation facts survive the Presentation IR path used by the real sheet.
- Verification evidence:
  - `yarn --cwd ide/theia-frontend test --test-name-pattern "Presentation IR occurrences"`: 114/114 pass.
  - `yarn --cwd ide build`: pass.
  - `yarn --cwd ide start:smoke:m25`: pass with 4 representations, 4 terminals, 4 labels, zero fallbacks, and 3 governed routes.
  - `yarn --cwd ide start:smoke:m24`: pass with governed route quality proof.
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`: pass.

### File List

- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m25-sample-project.js`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m25-product-smoke-wiring.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`

## Change Log

- 2026-07-19: Added M25 product smoke coverage and fixed Theia Presentation IR occurrence representation propagation.

## Status

done
