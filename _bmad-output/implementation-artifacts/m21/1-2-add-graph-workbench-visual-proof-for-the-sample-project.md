---
baseline_commit: 0b8e6fd5659595da0d779309ed745e8740faf685
---

# Story 1.2: Add graph workbench visual proof for the sample project

Status: done

## Story

As a reviewer,
I want executable IDE-visible proof for the M21 graph workbench,
so that the milestone is judged through the actual customer-facing surface.

## Acceptance Criteria

1. Given the M21 sample project, when the visual proof path runs, then it verifies the graph workbench surface rather than only model fixtures.
2. Given the graph workbench surface, when proof checks inspect the canvas contract, then they verify grid visibility, sheet transparency, transparent overlays, information popover behavior, and absence of stale M20-forbidden canvas elements.
3. Given M21 proof execution, when runtime diagnostics or logs are needed, then the proof records or checks them without requiring users to inspect `.mjs` fixtures as the customer proof.
4. Given Story 1.2 scope, when implementation is reviewed, then it does not implement layout intent, layout engine, route facts, label avoidance, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection.
5. Given Story 1.1 launch work, when Story 1.2 checks run, then they reuse `examples/m21/sample-project` and the accepted `start:smoke:m21` path instead of relying on stale workspace history.

## Tasks / Subtasks

- [x] Add executable graph workbench visual-proof guardrails (AC: 1, 2, 4)
  - [x] Add a focused M21 visual proof test script under `ide/theia-frontend/scripts/`.
  - [x] Check the graph workbench widget for the accepted customer-facing surface: stage chrome, viewport, canvas, sheet, info button, popover, bottom dock, zoom controls, and sheet frame.
  - [x] Check stylesheet rules for grid-backed stage, transparent sheet, transparent overlays, transparent zoom dock, and transparent node/component bodies.
  - [x] Check forbidden stale canvas elements remain absent: canvas `Cabinet Main` panel, bottom info table, sheet-local title block, sheet grid overlay element, sheet cross-reference marker chrome, floating panel borders/backgrounds, and bottom dock heading.
- [x] Record or check runtime proof evidence (AC: 1, 3, 5)
  - [x] Extend the M21 Electron smoke output so it reports the proof workspace and confirms graph-workbench proof mode.
  - [x] Keep the smoke path rooted at `examples/m21/sample-project`.
  - [x] Avoid claiming screenshot or Playwright visual regression scope in this story; that belongs to later Story 4.2.
- [x] Keep usage documentation truthful (AC: 1, 3, 4, 5)
  - [x] Update `docs/usages/m21-proof-usage.md` with the Story 1.2 visual-proof command.
  - [x] State that `.mjs` files are supporting automation only; the customer proof remains opening the sample project in Theia.
  - [x] Reconfirm M21 non-goals and the accepted M20 canvas invariants.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5)
  - [x] Run the new visual-proof test.
  - [x] Run the Story 1.1 sample-project static test.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Story 1.1 created `examples/m21/sample-project` and proved it opens with `yarn start:m21`.
- Story 1.1 added `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`, which opens the exact sample project path and verifies Java 25.
- `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs` already guards many accepted M20 UI invariants.
- `ide/theia-frontend/scripts/athena-m20-sheet-proof.test.mjs` is model-focused support. Story 1.2 should not regress into model-only proof.

### Architectural Guardrails

- Follow M21 AD-9 and AD-10: visible IDE proof is a gate, and accepted M20 canvas behavior carries forward.
- The visual proof is allowed to inspect Theia graph workbench source, styles, and smoke logs, but it must not move semantic or layout authority into the frontend.
- This story does not implement `kernel/layout-model`, `kernel/layout-engine`, route facts, label facts, or layout-intelligence algorithms. Those belong to later epics.
- The proof must keep Story 4.2 open for richer screenshot/Playwright-style regression coverage.

### Implementation Guidance

Likely update targets:

- `ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `ide/theia-product/scripts/verify-athena-m21-sample-project.js`
- `docs/usages/m21-proof-usage.md`
- this story file and `sprint-status.yaml`

Existing graph workbench contracts to preserve:

- `athena-graph-workbench__stage`
- `athena-graph-workbench__viewport`
- `athena-graph-workbench__sheet`
- `athena-graph-workbench__canvas`
- `athena-graph-workbench__floating-bar`
- `athena-graph-workbench__info-popover`
- `athena-graph-workbench__bottom-dock`
- `athena-graph-workbench__zoom-dock`
- `data-athena-info-button='true'`
- `data-athena-info-popover='true'`
- `handleWorkbenchClick` closes the information popover on whitespace clicks

Forbidden stale patterns:

- `athena-graph-workbench__overlay--bottom-right`
- `athena-graph-workbench__hud-chip`
- `athena-graph-workbench__bottom-dock-heading`
- `athena-graph-workbench__sheet-title-block`
- `athena-graph-workbench__sheet-grid`
- `athena-graph-workbench__sheet-cross-reference-marker`
- `athena-graph-workbench__floating-panel`
- visible canvas text pattern that renders `Cabinet Main` directly into the main canvas instead of the top information popover

### Testing Requirements

Run checks sequentially on Windows:

- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Previous Story Intelligence

Story 1.1 found and fixed a real launch-proof issue: the first smoke tried to use a frontend
`require` bridge that is unavailable in the current Theia runtime. The correct launch proof is to
let Theia's Electron positional workspace argument open the project and verify the resulting
workspace URL fragment. Do not reintroduce a frontend-owned workspace-open bridge.

Story 1.1 also established that `.athena` sources must use compiler-safe local syntax. Story 1.2
should not change sample source syntax.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs` passed after adding the M21 graph-workbench contract guard.
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` passed and now prints `ATHENA_GRAPH_WORKBENCH_PROOF={...}` with runtime graph-workbench DOM evidence for stage, viewport, sheet, canvas, transparent overlays, sheet frame, stage grid, info popover open, and whitespace-close behavior.
- `yarn workspace @engineeringood/athena-theia-product build` passed after the review hardening.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added a focused M21 graph workbench visual-proof test that binds the customer-facing surface, sheet chrome, bottom dock, info popover, and forbidden stale canvas shapes.
- Extended the M21 smoke path to click the IDE `Graphical View` action and emit graph-workbench proof only after the rendered DOM contract passes.
- Hardened the smoke path with isolated Electron user data and robust Windows hash/path normalization.
- Tightened the CSS contract guards so selector checks cannot pass by spanning unrelated CSS rules.
- Updated M21 usage documentation so the visual-proof command is visible and `.mjs` remains supporting automation only.
- Kept Story 1.2 bounded away from layout-engine, routing, cabinet, or final stack work.

### File List

- `_bmad-output/implementation-artifacts/m21/1-2-add-graph-workbench-visual-proof-for-the-sample-project.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `docs/usages/m21-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `ide/theia-product/scripts/verify-athena-m21-sample-project.js`

## Change Log

- 2026-07-17: Created M21 Story 1.2 with graph workbench visual-proof guardrails.
- 2026-07-17: Implemented the M21 graph workbench visual-proof guardrails, runtime proof marker, and usage update.
- 2026-07-17: Addressed code review by replacing the self-fulfilling proof marker with runtime graph-workbench DOM proof.

## Senior Developer Review (AI)

### Outcome

Approved after patch.

### Action Items

- [x] [Review][Patch] Replace false graph-workbench proof marker with runtime DOM proof from the rendered Theia graph workbench.
- [x] [Review][Patch] Isolate smoke user data so stale workspace state cannot satisfy the M21 launch proof.
- [x] [Review][Patch] Tighten CSS contract tests to match exact selector rules instead of spanning unrelated CSS.
- [x] [Review][Patch] Remove the accidental `start:m20` alias from the M21 story changes.
