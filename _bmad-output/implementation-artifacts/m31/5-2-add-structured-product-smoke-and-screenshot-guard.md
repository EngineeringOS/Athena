---
status: done
story_id: 5.2
epic: 5
title: Add Structured Product Smoke And Screenshot Guard
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
---

# Story 5.2: Add Structured Product Smoke And Screenshot Guard

## Status

Done

## Story

As a maintainer,
I want authoritative end-to-end proof of the customer workflow,
so that M31 completion does not depend on visual guessing or mocked integration.

## Acceptance Criteria

1. M31 product smoke is wired through `ide` and `ide/theia-product` scripts as `start:smoke:m31`, opens `examples/m31/sample-project`, and uses the existing Electron smoke opener conventions without adding a new product runtime dependency.
2. Structured proof output covers the M31 customer workflow: capability discovery, one single-intent entity creation transaction, nested source edit preview/accept evidence, one compatible relationship transaction, anchored route evidence, sheet/mode switch stability, reveal identity, close/reopen identity, stale diagnostic, and blocked diagnostic.
3. Structured assertions verify semantic ids, nested ports, source edits, Revision Guard, relationship endpoints, representation occurrences, composition facts, terminal-anchored routes, exactly two governed sheet roles, typed cross-sheet reference ids, lifecycle state, no direct downstream mutation, and no visual fallback regressions.
4. Screenshot guard captures a secondary human-review PNG for the M31 Graphical View, proves the active view defaults to Cabinet, and rejects oversized fixed viewBox, overlapping controls, visible normal chrome, duplicate off-sheet occurrences, center fallback routes, fallback representations, and repeated terminal labels.
5. Product-smoke wiring tests and executable proof payload tests exist in `ide/theia-frontend/scripts`, and they must not assert semantic meaning from DOM text, SVG geometry, or screenshots as authority.
6. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including smoke hooks, scripts, screenshots, generated output paths, sample references, stale M29/M30 copies, `.tools`, encoding, and cleanup-ledger review.

## Tasks/Subtasks

- [x] Add RED smoke-wiring and structured-payload tests before adding the M31 smoke script. (AC: 1,2,3,5,6)
  - [x] Assert `ide/package.json` exposes `start:smoke:m31` through the product workspace.
  - [x] Assert `ide/theia-product/package.json` exposes `start:smoke:m31` and references `verify-athena-m31-sample-project.js`.
  - [x] Assert the M31 smoke script exports structured proof builders/assertions with an M31 schema version and required proof-kind inventory.
  - [x] Assert the smoke script resolves `examples/m31/sample-project`, `src/01-governed-authoring-customer-source.athena`, and an M31 screenshot path under `_bmad-output/implementation-artifacts/m31/screenshots/`.
  - [x] Assert smoke tests do not use DOM text, SVG geometry, or screenshot pixels as semantic authority.
- [x] Implement `ide/theia-product/scripts/verify-athena-m31-sample-project.js`. (AC: 1,2,3,4)
  - [x] Follow the M29/M30 smoke script conventions: assert installed LSP host, spawn Electron with `athena-electron-open-workspace-main.js`, collect sentinels, fail loudly with captured output, and use `windowsHide: true`.
  - [x] Open `examples/m31/sample-project` and set smoke environment for screenshot capture, Outline nested-port proof, temporary user data, and deterministic exit on workspace-open proof.
  - [x] Emit `ATHENA_M31_AUTHORING_PROOF=` with structured proof payloads and echo `ATHENA_GRAPH_WORKBENCH_PROOF=` for debugging.
  - [x] Keep Gradle verification outside the smoke script and document sequential verification wording when LSP install is missing.
- [x] Build structured M31 proof payloads. (AC: 2,3,5)
  - [x] Include proof kinds for `capability-discovery`, `entity-transaction`, `nested-source-edit`, `relationship-transaction`, `route-anchors`, `sheet-reference-identity`, `mode-switch-reveal-reopen`, `lifecycle-diagnostics`, and `visual-regression-guards`.
  - [x] Use the sample source URI and stable M31 sample revision label; no M29/M30 sample source URI may appear in M31 proof payloads.
  - [x] Prove transaction vocabulary uses `SemanticAuthoringTransaction`, single-intent cardinality, Revision Guard, and backend-owned source edit wording.
  - [x] Prove nested ports, compatible relationship endpoint ids, representation/composition/routing facts, governed two-sheet roles, typed cross-sheet reference ids, lifecycle diagnostics, and no direct downstream mutation.
- [x] Add graph-workbench proof assertions for M31. (AC: 2,3,4)
  - [x] Assert active view is `cabinet` by default.
  - [x] Assert exactly two governed sheet choices survive mode switches and match `control-and-plc-logic` and `field-wiring-and-terminal-transition`.
  - [x] Assert representation count, terminal count, label count, fallback representation ids, route count, terminal count, routes-with-terminal-anchors, center fallback ids, duplicate occurrence evidence, repeated label evidence, transparent chrome, and viewBox dimensions are valid.
  - [x] Assert Outline proof includes nested port path from the M31 sample.
  - [x] Assert cross-reference/reveal proof exists without DOM-label inference.
- [x] Wire product package scripts and usage references. (AC: 1,4,6)
  - [x] Add `start:smoke:m31` to `ide/package.json`.
  - [x] Add `start:smoke:m31` to `ide/theia-product/package.json`.
  - [x] Update `examples/m31/sample-project/README.md` with the smoke command and screenshot output path.
  - [x] Do not add new product dependencies, product runtime services, `.athena` syntax, or QET runtime references.
- [x] Run verification sequentially on Windows. (AC: 1,2,3,4,5)
  - [x] Run the RED/GREEN focused frontend smoke-wiring tests.
  - [x] Run `yarn test` in `ide/theia-frontend` for frontend script coverage.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist` if product smoke execution requires a fresh installed LSP host; do not run any Gradle command in parallel.
  - [x] Run `yarn --cwd ide start:smoke:m31` if the Electron smoke can complete in the current environment; if it cannot, record the exact blocker and keep structured script tests as the executable guard.
  - [x] Run `git diff --check`, M31 stale/reference scans, `.tools` status check, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- [x] Complete mandatory polish/purge review and update story evidence. (AC: 6)
  - [x] Review smoke script, package hooks, README, screenshot path, generated screenshots, adjacent M29/M30 smoke scripts, sample paths, frontend tests, and cleanup ledger.
  - [x] Remove stale copied M29/M30 wording, old fixture names, generated residue, and any product-smoke claims not backed by executable assertions.
  - [x] Ledger retained/deferred items with owner, reason, target milestone, and verification.
  - [x] Record AC-to-evidence mapping, final commands, File List, Completion Notes, and Change Log.

### Review Findings

- [x] [Review][Patch] Reject duplicate occurrence and repeated terminal-label regressions from currently available graph proof fields. [`ide/theia-product/scripts/verify-athena-m31-sample-project.js`]

## Dev Notes

### Architecture Guardrails

- This story implements M31 Epic 5 Story 5.2 and contributes to FR-38, FR-39, FR-40, FR-41, FR-42, NFR-3, NFR-4, NFR-6, NFR-7, NFR-8, NFR-9, NFR-10, and NFR-12.
- Structured proof is the acceptance authority; screenshots are secondary human-review evidence only.
- Theia and Electron smoke scripts may transport and inspect proof payloads, but they must not infer semantic truth from DOM text, SVG geometry, CSS classes, or screenshot pixels.
- The smoke script must not introduce a new runtime dependency or product service. Follow the existing M29/M30 product smoke pattern.
- M31 must preserve the accepted Cabinet default and exactly two governed sheet roles. Do not force `--active-view documentation` for the M31 opener.
- Product smoke must stay within the existing sample and platform-owned facts. It must not add source-level geometry, sheet membership, viewBox, or renderer truth.
- Every story ends with mandatory polish/purge. Do not mark done without fresh AC evidence and cleanup review.

### Existing Files And Patterns To Inspect

- `ide/theia-product/scripts/verify-athena-m29-sample-project.js`: structured interaction proof inventory and product opener pattern.
- `ide/theia-product/scripts/verify-athena-m30-sample-project.js`: screenshot guard, graph-workbench proof assertions, representation proof payload shape, transparent chrome and viewBox checks.
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`: shared Electron opener and smoke environment support.
- `ide/theia-frontend/scripts/athena-m29-product-smoke-wiring.test.mjs` and `athena-m30-product-smoke-wiring.test.mjs`: package-hook and proof-export test conventions.
- `ide/theia-frontend/scripts/athena-m31-sample-project.test.mjs`: 5.1 openable sample proof.
- `examples/m31/sample-project/README.md` and `src/01-governed-authoring-customer-source.athena`: exact M31 sample paths and names.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM31SampleProjectCompilerTest.kt`: compiler/projection assertions for two sheets, typed cross-sheet links, anchors, corridors, duplicate occurrence prevention, and derived canvas.
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaM31SampleAuthoringProofTest.kt`: runtime proof that the sample supports entity creation and compatible relationship previews.

### Previous Story Intelligence

- Story 5.1 created the M31 sample and intentionally deferred full `start:smoke:m31`, active Cabinet screenshot/default proof, and full product smoke to this story.
- 5.1 code review found that compile-only proof is not enough. This story must expose the create/connect workflow in structured product-smoke proof, not just source regex or projection existence.
- 5.1 accepted `start:m31` without `--active-view documentation`; keep that behavior so Cabinet remains the default proof.
- 5.1 runtime proof uses `ServiceMotorM31` as the eligible creation target and `ControlRelayK31.spareOut -> SpareTerminalXT31.in1` as the compatible relationship preview.
- M30 smoke already verifies PNG signature/size, active Cabinet default, transparent chrome, no fallback representations, no center fallback routes, route/body intersection count, and graph-workbench proof sentinels.
- M29 smoke already verifies structured interaction proof payloads and avoids DOM semantic authority.

### Testing Requirements

- Follow RED-GREEN-REFACTOR. Record observed RED failures before production edits.
- Prefer structured proof over screenshot evidence. Screenshot assertions should prove only that the secondary human-review artifact exists and is non-empty/PNG.
- If frontend scripts or package hooks change, run `yarn test` in `ide/theia-frontend`.
- If product smoke is executed, run Gradle installDist sequentially before it if required; never run Gradle commands concurrently.
- Final polish must scan for stale M29/M30 sample paths in M31 proof payloads, QET runtime references, `.elmt`, visual primitive syntax in `.athena`, fixed oversized viewBox, generic fallback, center fallback, duplicate off-sheet occurrence, repeated labels, frontend source serializer language, `.tools`, and generated screenshot residue.

### Scope Boundaries

- In scope: M31 product smoke script, M31 package smoke hooks, structured proof payload builders/assertions, screenshot guard, M31 README smoke usage, focused frontend tests, smoke execution evidence where possible.
- Out of scope: new product runtime services, new `.athena` syntax, QET importer, changes to representation model semantics, final milestone retrospective, and full release packaging.
- Do not implement Story 5.3 final purge/retrospective here except for local cleanup ledger entries directly exposed by this story.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 5, Story 5.2.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - FR-38..FR-40 and Success Metrics SM-1..SM-6.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-13 structured proof, AD-15 polish/purge, AD-18 downstream immutability.
- `_bmad-output/implementation-artifacts/m31/5-1-create-the-m31-customer-sample-project.md` - sample proof and review deferral.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created via BMAD create-story workflow after Story 5.1 reached done.
- Loaded M31 sprint status, epics, PRD, architecture spine, completed Story 5.1 record, and M29/M30 product smoke scripts.
- RED: `node --test scripts\athena-m31-product-smoke-wiring.test.mjs` failed because `start:smoke:m31` and `verify-athena-m31-sample-project.js` were missing.
- GREEN focused: `node --test scripts\athena-m31-product-smoke-wiring.test.mjs` passed 4/4.
- Frontend regression: `yarn test` in `ide/theia-frontend` passed 186/186 after the final patch.
- LSP install: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist` passed sequentially.
- Product smoke: `yarn --cwd ide start:smoke:m31` passed and emitted `ATHENA_M31_AUTHORING_PROOF=` plus `ATHENA_GRAPH_WORKBENCH_PROOF=`.
- Polish/purge: `git diff --check` passed with existing line-ending warnings only; M31 stale scan returned no matches; `.tools` status was clean; `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Code review: Subagent layers were unavailable because the session agent limit was exhausted, so the main thread performed Blind/Edge/Acceptance checks against the scoped 5.2 diff and story. One patch finding was identified and resolved.
- Review RED: `node --test scripts\athena-m31-product-smoke-wiring.test.mjs` failed because duplicate occurrence and repeated terminal-label regressions were not rejected.
- Review GREEN: `node --test scripts\athena-m31-product-smoke-wiring.test.mjs` passed 5/5 after adding duplicate occurrence and repeated terminal-label guards.
- Review regression: `yarn test` in `ide/theia-frontend` passed 187/187 and `yarn --cwd ide start:smoke:m31` passed after the review patch.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added executable M31 product smoke wiring and payload tests in the frontend script suite.
- Implemented the M31 product smoke script with structured authoring proof payloads, Cabinet-default screenshot proof, documentation-view sheet selector proof, Outline nested-port proof, route/representation/visual guards, and PNG validation.
- Added review-patch guard coverage so M31 graph proof rejects duplicate representation occurrences, duplicate node semantic ids, and repeated presentation terminal labels using currently emitted proof fields.
- Wired `start:smoke:m31` through `ide` and `ide/theia-product`, and documented the smoke command plus screenshot path in the M31 sample README.
- AC-to-evidence mapping: AC1 covered by package-hook tests and product smoke; AC2/AC3 covered by `ATHENA_M31_AUTHORING_PROOF=` payload builders/assertions; AC4 covered by `assertGraphWorkbenchProof`, `assertPngScreenshot`, and the generated PNG; AC5 covered by frontend smoke-wiring tests and forbidden-authority scans; AC6 covered by stale scans, `.tools` check, encoding audit, and this ledger.
- Retained/deferred ledger: The product smoke uses a second documentation-view opener pass only to prove governed sheet selector stability because the accepted default Cabinet view has no sheet selector; owner M31 implementation agent, retained in Story 5.2, verified by `yarn --cwd ide start:smoke:m31`.

### File List

- `_bmad-output/implementation-artifacts/m31/5-2-add-structured-product-smoke-and-screenshot-guard.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m31/screenshots/m31-graph-workbench-smoke.png`
- `examples/m31/sample-project/README.md`
- `ide/package.json`
- `ide/theia-frontend/scripts/athena-m31-product-smoke-wiring.test.mjs`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m31-sample-project.js`

### Change Log

- 2026-07-22: Created Story 5.2 context for BMAD dev-story execution.
- 2026-07-22: Implemented M31 structured product smoke, screenshot guard, package hooks, README usage, verification evidence, and polish/purge ledger.
- 2026-07-22: Addressed code-review patch finding for duplicate occurrence and repeated terminal-label guard coverage.
