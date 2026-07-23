---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 7.5
epic: 7
title: Final Graph View Product Polish And Purge
---

# Story 7.5: Final Graph View Product Polish And Purge

## Status

Review

## Story

As a customer-demo owner,
I want Graph View to read as one coherent industrial product surface,
so that the M32 customer demo is not undermined by chaotic controls, stale labels, or hidden
fallbacks.

## Required Context

- Stories 7.1-7.4 must be in `review` before this story starts.
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- M32 product smoke: `ide/theia-product/scripts/verify-athena-m32-sample-project.js`
- M32 sample project: `examples/m32/sample-project`
- Visual direction: `draft/layouts/003-presentation-language.md`

## Acceptance Criteria

1. Given Stories 7.1 through 7.4 are complete, when the M32 IDE sample opens, then the Graph View
   toolbar has stable grouped controls, no confusing architecture labels, no visible normal wrapper
   chrome, transparent hitboxes/backgrounds, and no duplicated labels.
2. Given Electron E2E runs, when it captures structured proof and screenshot, then it verifies view
   taxonomy, stable sheet navigation, usable create panel, package-backed representation authority,
   tight viewBox, route anchors, and no fallback rendering.
3. Given final cleanup runs, when source, generated Theia bundles, docs, examples, proof scripts,
   screenshots, and sprint artifacts are reviewed, then stale experiments are removed or ledgered,
   `.tools` is not staged, and AC-to-evidence is recorded before review.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Confirm Stories 7.1-7.4 are in `review` with AC-to-evidence before starting. (AC: 1)
- [x] Run a CodeGraph/source audit of Graph View toolbar, sheet selector, create panel, package
  proof, representation proof, route proof, and stale fallback strings. (AC: 1..3)
- [x] Add or update final smoke assertions only for integrated product proof, not new behavior.
  (AC: 2)
- [x] Run M32 Electron E2E and capture screenshot proof. (AC: 1,2)
- [x] Rebuild Theia product bundles so manual IDE validation cannot use stale generated output.
  (AC: 2,3)
- [x] Update cleanup ledger, story evidence, and sprint status; do not mark stories `done` without
  the review workflow. (AC: 3)
- [x] Verify no `.tools` path is staged or included. (AC: 3)
- [x] Run focused frontend/product checks, full regression if previous stories touched kernel or
  runtime, and encoding audit. (AC: 2..4)

## Dev Notes

- This story is a purge/integration gate. Do not introduce new Graph View concepts here.
- If an issue remains, ledger it with owner, reason, target milestone, and verification path. Do
  not hide it behind a passing screenshot.
- Keep screenshot evidence secondary to structured proof.
- Story status stops at `review` unless a separate code-review workflow moves it.

## Testing Requirements

- Required:
  - `yarn start:smoke:m32` in `ide/theia-product`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git status --short -- .tools`
- Run `.\gradlew.bat --no-daemon --console=plain check` sequentially if kernel/runtime/package
  code changed in Stories 7.1-7.4 or final proof touches JVM modules.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-22: Story 7.5 scope corrected by user: M32 product polish focuses the customer-demo Graph View on the `documentation` professional sheet. Other projection view ids remain compatibility surfaces and should not dominate the main demo toolbar.
- 2026-07-22: CodeGraph/source audit covered Graph View toolbar rendering, projection view buttons, sheet selector persistence, create panel proof, package-backed representation proof, route proof, and stale `cabinet` naming. The final user decision is to hide unfinished projection modes from the M32 customer toolbar and ledger them as compatibility.

### Completion Notes List

- Story started after Stories 7.1-7.4 were in `review`; Story 7.4 verification included rebuilt M32 Electron smoke, full Gradle `check`, and encoding audit.
- Graph View toolbar now exposes only the `documentation` projection button when the documentation view is available; `cabinet`, `wiring`, and `schematic` remain protocol/programmatic compatibility and are counted by `data-athena-compatibility-projection-view-count`.
- Removed stale cabinet-first internal naming from the projection information popover (`buildProjectionInfoRows`, `renderProjectionInfoPopover`).
- M32 product smoke now asserts `projectionViewProof.visibleViewIds = ["documentation"]`, `visibleViewButtonCount = 1`, active documentation button, hidden compatibility count, package-backed representation evidence, route anchors, tight viewBox, transparent normal chrome, create panel geometry, and Outline nested port proof.
- `.tools` status was empty; no `.tools` path is included.
- AC-to-evidence:
  - AC1: frontend taxonomy/density tests plus rebuilt smoke prove stable grouped controls, product taxonomy, hidden compatibility projection buttons, transparent sheet/chrome, and no duplicate/fallback proof.
  - AC2: `yarn start:smoke:m32` captured structured graph proof and screenshot with documentation-only toolbar, sheet navigation, create panel geometry, package-backed representation, route anchors, and viewBox guard.
  - AC3: cleanup ledger entry `M32-CL-007-7.5` records retained projection compatibility; `.tools` check was empty.
  - AC4: final polish/purge gate passed through frontend tests, product build, E2E smoke, full Gradle check, and encoding audit.
- Verification evidence: frontend `yarn test` passed 198/198, product `yarn build` passed, product `yarn start:smoke:m32` passed, root `gradlew check` passed, encoding audit passed, and `git status --short -- .tools` produced no output.

### File List

- _bmad-output/implementation-artifacts/m32/7-5-final-graph-view-product-polish-and-purge.md
- _bmad-output/implementation-artifacts/m32/cleanup-ledger.md
- _bmad-output/implementation-artifacts/m32/screenshots/m32-graph-workbench-smoke.png
- _bmad-output/implementation-artifacts/m32/sprint-status.yaml
- ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs
- ide/theia-frontend/scripts/athena-m21-canvas-contract.test.mjs
- ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs
- ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs
- ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m32-sample-project.js

### Change Log

- 2026-07-22: Focused the M32 customer Graph View on the documentation sheet only, hid unfinished projection modes as ledgered compatibility, tightened product smoke proof, purged cabinet-first internal naming, and verified rebuilt IDE E2E.

## Mandatory Final Polish/Purge Gate

- Review all Epic 7 source, tests, generated bundles, docs, examples, screenshots, sprint status,
  and cleanup ledger.
- Remove stale artifacts or ledger retained compatibility.
- Record AC-to-evidence before moving the story to `review`.
