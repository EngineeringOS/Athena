---
status: done
story_id: 5.1
epic: 5
title: Create The M31 Customer Sample Project
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
---

# Story 5.1: Create The M31 Customer Sample Project

## Status

Done

## Story

As a customer-demo owner,
I want an exact rolling-shutter engineering model authoring sample,
so that M31 can be evaluated through a realistic two-sheet workflow.

## Acceptance Criteria

1. `examples/m31/sample-project` exists as an openable Athena workspace with `athena.yaml`, `athena.lock`, README, and source under `src/`.
2. The sample source uses admitted nested-port syntax and grouped `connect` syntax where grouping improves readability; it contains no QET runtime reference, no visual primitive syntax, no legacy top-level preferred device-port generation, and no `.athena` viewBox/geometry truth.
3. The initial model supports the M31 customer workflow: one eligible governed entity creation, one compatible relationship, exactly two policy-owned sheets, and at least one semantic cross-sheet reference after compile/projection.
4. Opening the sample in Athena IDE defaults to the accepted Cabinet view and exposes exactly two governed sheet choices, with no fallback representation, duplicate off-sheet occurrence, center fallback route, repeated labels, visible normal chrome, or fixed oversized viewBox.
5. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including sample files, README wording, manifest/lock consistency, stale fixture names, adjacent M29/M30 references, `.tools`, encoding, and cleanup-ledger review.

## Tasks/Subtasks

- [x] Add RED sample-structure and syntax tests before creating or changing sample files. (AC: 1,2,5)
  - [x] Assert `examples/m31/sample-project/athena.yaml`, `athena.lock`, `README.md`, and at least one `src/*.athena` file exist.
  - [x] Assert source contains nested `port` blocks inside `device` blocks and at least one grouped `connect <name> { ... }` block.
  - [x] Assert source does not contain QET runtime references, `.elmt`, SVG/viewBox/visual primitive authority, or preferred legacy generated top-level `port Device.name` declarations.
  - [x] Assert sample naming is M31-specific and does not accidentally point product smoke hooks at M29/M30 samples.
- [x] Create the M31 rolling-shutter customer sample workspace. (AC: 1,2,3)
  - [x] Create `examples/m31/sample-project/athena.yaml` using the existing sample manifest shape from M29/M30.
  - [x] Create `examples/m31/sample-project/athena.lock` through the governed lock/materialization path or by matching existing deterministic sample lock conventions.
  - [x] Create `examples/m31/sample-project/src/01-governed-authoring-customer-source.athena` with compact nested-port devices and grouped connections.
  - [x] Include a realistic rolling-shutter/control slice: power supply, breaker/protection, wall switch, control relay/PLC or controller, field terminal transition, shutter motor, pilot/status indicator, and a creation context that can accept one additional governed device.
- [x] Ensure the source supports M31 authoring proof rather than a static viewer-only diagram. (AC: 2,3)
  - [x] Ensure at least one concept-template-compatible creation target remains available for Graphical View create-entity preview.
  - [x] Ensure at least one compatible relationship can be authored after creation using canonical terminal identities.
  - [x] Keep grouped `connect` names as source provenance only; do not imply new relationship types.
  - [x] Preserve flat canonical relationship equivalence and avoid source tricks that only satisfy renderer appearance.
- [x] Wire or verify sample projection expectations. (AC: 3,4)
  - [x] Compile or run the existing projection/product proof path for the sample.
  - [x] Verify Cabinet remains the default projection for the sample.
  - [x] Verify the document projection policy exposes exactly `control-and-plc-logic` and `field-wiring-and-terminal-transition` sheet roles.
  - [x] Verify cross-sheet reference facts exist for at least one relationship spanning the two sheets.
- [x] Add or update focused product-smoke wiring tests for the M31 sample. (AC: 1,3,4)
  - [x] Assert the sample is openable through the same product smoke opener conventions as M27-M30.
  - [x] Assert structured proof can locate semantic ids, nested ports, grouped relationship provenance, sheet roles, reference markers, representation occurrences, and anchored route facts.
  - [x] Assert no fixed `viewBox="0 0 1680 1188"`, center fallback route, generic fallback box, duplicate off-sheet occurrence, visible normal hitbox/background chrome, or repeated terminal label is introduced.
- [x] Preserve M31 authority boundaries. (AC: 2,3,4)
  - [x] Do not add new `.athena` syntax.
  - [x] Do not add QET runtime dependency or `.athena` source references to QET assets.
  - [x] Do not encode symbol geometry, permanent placement coordinates, route geometry, sheet membership, viewBox, or renderer details as semantic truth.
  - [x] Do not change ANTLR4 or tree-sitter unless a focused syntax test proves the existing admitted grouped/nested syntax is not parsed.
- [x] Run verification sequentially on Windows. (AC: 1,2,3,4)
  - [x] Run RED/GREEN focused sample tests.
  - [x] Run relevant compiler/LSP/projection tests if manifest, lock, parser, compiler, or projection code changes; run Gradle commands strictly sequentially.
  - [x] Run `yarn test` in `ide/theia-frontend` if frontend smoke wiring changes.
  - [x] Run `git diff --check`, sample stale/reference scans, `.tools` status check, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- [x] Complete mandatory polish/purge review and update story evidence. (AC: 5)
  - [x] Review sample source, manifest, lock, README, smoke hooks, tests, adjacent M29/M30 sample references, generated files, and cleanup ledger.
  - [x] Remove stale sample assumptions, obsolete fixture names, generated residue, or ledger each retained item with owner, reason, target milestone, and verification.
  - [x] Record AC-to-evidence mapping, final verification commands, File List, Completion Notes, and Change Log.
- [x] Review Follow-ups (AI)
  - [x] [AI-Review][High] Add runtime proof that the M31 sample supports one governed entity creation preview and one compatible relationship preview.
  - [x] [AI-Review][Medium] Tighten cross-sheet reference and routing-corridor assertions beyond non-empty projection facts.
  - [x] [AI-Review][Deferred to 5.2] Full `start:smoke:m31` and active Cabinet screenshot/default proof belong to Story 5.2 structured product smoke and screenshot guard.

## Dev Notes

### Architecture Guardrails

- This story implements M31 Epic 5 Story 5.1 and contributes to FR-37, FR-39, FR-40, FR-41, FR-42, NFR-1, NFR-6, NFR-7, NFR-10, NFR-12, and NFR-13.
- The sample is a customer proof artifact, not a new source-language milestone. Use existing nested-port and grouped-relationship syntax only.
- `.athena` remains semantic persistence. Do not encode view-layer or representation-library geometry in source.
- QET is not an authority in M31. The sample may be visually inspired by professional control documents, but it must not reference QET `.elmt`, QET XML, or QET runtime paths.
- The two-sheet policy remains platform-owned. The sample source may provide semantic facts that project well; it must not fake sheet count or sheet membership with source file count.
- Representation and routing proof must come from M30/M27-M31 platform layers. Do not add generic fallback boxes or center fallback routes to make the sample appear complete.
- The sample must be openable by product smoke tooling. Avoid paths, names, or README claims that force manual setup outside the existing Athena example conventions.
- Every story ends with mandatory polish/purge. Do not mark done without fresh AC evidence and cleanup review.

### Existing Files And Patterns To Inspect

- `examples/m30/sample-project/athena.yaml`, `athena.lock`, `README.md`, and `src/01-rolling-shutter-control-source.athena`: closest professional visual/sample baseline.
- `examples/m29/sample-project/athena.yaml`, `README.md`, and `src/*.athena`: interaction/authoring sample structure and openable workspace conventions.
- `ide/theia-frontend/scripts/athena-m30-product-smoke-wiring.test.mjs`, `athena-m30-sample-project.test.mjs`, and adjacent M27-M30 smoke tests: product-smoke wiring patterns.
- `ide/theia-frontend/scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs`: current M31 selector/lifecycle guard evidence.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md`: FR-37..FR-42 and M31 core acceptance scope.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md`: AD-11, AD-12, AD-13, AD-15, AD-18.

### Previous Story Intelligence

- Story 3.1 established exactly two M31 policy-owned sheets and Cabinet default; the sample must exercise that, not derive sheet count from source files.
- Story 3.2 preserved representation/composition/routing re-derivation and forbids downstream artifact mutation; the sample must not include semantic shortcuts for view appearance.
- Story 3.3 established typed cross-sheet reference navigation and reopen identity; the sample must include a relationship that naturally spans the two sheet roles.
- Story 4.1/4.2 delivered create/connect graphical workflows; the sample must provide realistic data for those workflows rather than a static viewer-only document.
- Story 4.3 fixed lifecycle/selector/revision races and proved nested Outline ports; this sample must use nested ports prominently enough that the proof remains customer-visible.
- Code-review lessons from 4.3: tests must not only regex the source when behavior can regress; where practical, assert structured proof payloads or compiled facts.

### Testing Requirements

- Follow RED-GREEN-REFACTOR. Record observed RED failures before production edits.
- Prefer structured proof over screenshots. Screenshots are secondary and belong mainly to Story 5.2.
- If source-only sample files change, at minimum run focused sample tests, relevant smoke wiring tests, `git diff --check`, `.tools` status, and encoding audit.
- If parser/compiler/projection behavior is touched, run the relevant Gradle tests sequentially; never run Gradle commands concurrently.
- If frontend smoke scripts change, run `yarn test` in `ide/theia-frontend`.
- Final polish must scan for QET runtime references, `.elmt`, visual primitive syntax in `.athena`, fixed oversized viewBox, generic fallback, center fallback, duplicate off-sheet occurrence, repeated labels, frontend source serializer language, and stale M29/M30 sample path references.

### Scope Boundaries

- In scope: sample workspace, sample source, manifest/lock, README, focused tests, smoke wiring references needed to identify the M31 sample.
- Out of scope: new syntax, QET importer, new representation library format, new visual renderer behavior, full product smoke execution with screenshots, final M31 retrospective, and cleanup ledger closeout for the whole milestone.
- Do not start Story 5.2 work here except for small sample path hooks required to make 5.1 verifiable.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 5, Story 5.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - FR-37..FR-42, M31 Core Acceptance Scope.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-11, AD-12, AD-13, AD-15, AD-18.
- `_bmad-output/implementation-artifacts/m31/4-3-preserve-controls-and-surface-lifecycle-diagnostics.md` - latest lifecycle, selector, and nested Outline review lessons.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created via BMAD create-story workflow after Story 4.3 reached done.
- Loaded M31 sprint status, epics, PRD, architecture spine, current Story 4.3 record, and existing M29/M30 sample structure.
- RED: `node --test scripts\athena-m31-sample-project.test.mjs` failed because `examples/m31/sample-project/src/01-governed-authoring-customer-source.athena` did not exist.
- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM31SampleProjectCompilerTest` failed because the M31 sample source/project did not exist.
- GREEN: focused frontend M31 sample test passed after adding the sample workspace and `start:m31` opener hook.
- GREEN: focused compiler M31 sample test passed after adding the semantic source, deterministic manifest/lock, and projection assertions.
- Regression: `yarn test` in `ide/theia-frontend` passed 182/182 tests.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- Polish/Purge: `git diff --check` passed with existing line-ending warnings only; `.tools` status was clean; encoding audit passed; M31 source stale/forbidden-token scan returned no hits.
- Code Review: Blind Hunter, Edge Case Hunter, and Acceptance Auditor completed. High-confidence follow-ups were resolved with `AthenaM31SampleAuthoringProofTest` and tighter compiler cross-sheet/routing assertions; Story 5.2 smoke-hook/default-view screenshot work remains deferred by scope.
- Final Verification: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM31SampleProjectCompilerTest`, `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaM31SampleAuthoringProofTest`, `yarn test` in `ide/theia-frontend`, `git diff --check`, `.tools` status check, M31 source forbidden-token scan, and encoding audit all passed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `examples/m31/sample-project` as the governed rolling-shutter authoring sample with semantic-only nested ports and grouped relationship provenance.
- Added M31 sample tests covering workspace structure, opener wiring, nested/grouped syntax, forbidden visual/QET source authority, two governed sheet roles, cross-sheet reference facts, anchored endpoints, duplicate occurrence prevention, and content-derived canvas bounds.
- Added runtime authoring proof against the actual M31 sample source for one eligible governed entity creation preview and one compatible governed relationship preview.
- Wired `yarn --cwd ide start:m31` to open `examples/m31/sample-project` without forcing the old documentation active-view flag, preserving the accepted Cabinet default behavior.
- AC-to-evidence mapping: AC1 covered by `athena-m31-sample-project.test.mjs`; AC2 covered by source regex guards and `AthenaM31SampleProjectCompilerTest`; AC3 covered by compiler/projection, runtime authoring preview, and typed cross-sheet reference assertions; AC4 covered by start hook, governed sheet roles, anchor/corridor proof, duplicate occurrence prevention, and canvas-bound assertions; AC5 covered by stale scans, `.tools` check, `git diff --check`, encoding audit, and review follow-up closure.

### File List

- `_bmad-output/implementation-artifacts/m31/5-1-create-the-m31-customer-sample-project.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `examples/m31/sample-project/athena.yaml`
- `examples/m31/sample-project/athena.lock`
- `examples/m31/sample-project/README.md`
- `examples/m31/sample-project/src/01-governed-authoring-customer-source.athena`
- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-frontend/scripts/athena-m31-sample-project.test.mjs`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM31SampleProjectCompilerTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaM31SampleAuthoringProofTest.kt`

### Change Log

- 2026-07-22: Created Story 5.1 context for BMAD dev-story execution.
- 2026-07-22: Implemented the M31 governed rolling-shutter customer sample workspace, opener hook, compiler proof, frontend sample test, and polish/purge evidence.
- 2026-07-22: Resolved AI review findings with runtime authoring proof and stronger cross-sheet/routing assertions; deferred full smoke/screenshot hook to Story 5.2.
