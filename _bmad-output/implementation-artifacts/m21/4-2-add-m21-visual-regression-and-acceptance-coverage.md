---
baseline_commit: ef77e67098a0140fe33db16f9978ae337a0324a1
---

# Story 4.2: Add M21 visual regression and acceptance coverage

Status: done

## Story

As a reviewer,
I want automated coverage for the customer-facing M21 layout proof,
so that visual and interaction regressions are caught before manual review.

## Acceptance Criteria

1. Given the M21 sample project and graph workbench, when the M21 regression suite runs, then it covers layout intent/fact stability, schematic route facts, label readability, M20 canvas invariants, and source/outline/sheet coherence.
2. Given the graph workbench proof path, when it runs, then screenshot, Playwright-style, DOM, or equivalent graph workbench evidence is produced or checked.
3. Given repeated runs on the same governed input, when stability checks run, then deterministic layout, route, and label facts remain covered.
4. Given M21 scope, when coverage scripts are inspected, then they do not become customer instructions, frontend semantic authority, desktop-viewer scope, AI layout, or final stack selection.

## Tasks / Subtasks

- [x] Add M21 acceptance coverage guard (AC: 1, 2, 3, 4)
  - [x] Add a test that verifies the M21 proof suite includes kernel layout, routing, label, sample-project, same-tab navigation, and graph-workbench DOM proof coverage.
  - [x] Verify the Theia smoke proof reports graph-workbench DOM evidence for grid, sheet, transparent controls, info popover, and whitespace close behavior.
  - [x] Keep `.mjs` files as supporting checks, not the customer-facing proof itself.
- [x] Validate deterministic coverage remains executable (AC: 1, 3)
  - [x] Ensure `:kernel:layout-engine:test` and `:kernel:routing-model:test` are part of the documented M21 verification path or covered by the acceptance guard.
  - [x] Ensure routing and label tests cover repeated-run stability.
- [x] Validate and update story status (AC: 1, 2, 3, 4)
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- M21 has focused kernel coverage for layout regions, route facts, and label facts.
- M21 has Node checks for sample-project shape, source identity vocabulary, same-tab editor navigation, and graph-workbench visual proof.
- The Theia smoke proof emits `ATHENA_GRAPH_WORKBENCH_PROOF={...}` with runtime DOM evidence.
- Story 4.2 should make this coverage explicit and hard to accidentally drop.

### Architectural Guardrails

- Follow M21 AD-9 and AD-10.
- Proof scripts are supporting checks only. Customer proof remains the openable Theia IDE sample.
- Do not add desktop-viewer scope or frontend-owned semantic resolution.

### Implementation Guidance

Likely update targets:

- `ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`
- `docs/usages/m21-proof-usage.md`
- this story file and `sprint-status.yaml`

### Testing Requirements

Run checks sequentially on Windows as listed in the tasks.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Verification:
  - `node --test ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
  - `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
  - `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added `athena-m21-acceptance-coverage.test.mjs` to verify the M21 proof suite covers kernel layout, routing, label, sample-project, navigation, graph-workbench DOM proof, and smoke evidence.
- Updated M21 usage documentation to include the kernel test tasks, acceptance coverage guard, and same-tab navigation proof.
- Review found no additional code changes required.

### File List

- `_bmad-output/implementation-artifacts/m21/4-2-add-m21-visual-regression-and-acceptance-coverage.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `docs/usages/m21-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`

## Change Log

- 2026-07-17: Created M21 Story 4.2 for visual regression and acceptance coverage.
- 2026-07-17: Added M21 acceptance coverage guard and marked Story 4.2 done.

## Senior Developer Review (AI)

### Review Outcome

Approved.

### Findings Addressed

- No code changes required after review.

### Verification

- `node --test ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` passed from `ide/`.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
