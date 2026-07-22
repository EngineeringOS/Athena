---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 3.1
epic: 3
title: Add The Two-Sheet Customer Projection Policy
---

# Story 3.1: Add The Two-Sheet Customer Projection Policy

## Status

Done

## Story

As a controls engineer,
I want a control sheet and a field/device sheet derived from engineering policy,
so that document structure does not depend on source file count or frontend tabs.

## Acceptance Criteria

1. The M31 customer projection profile exposes exactly two governed sheet roles in deterministic order: control and field/device. Source file count, editor tabs, DOM state, and renderer widgets do not affect sheet count.
2. Unchanged semantic model plus unchanged projection policy recompiles/reopens with stable sheet ids, sheet order, publication metadata, and Projection Occurrence identities.
3. Graphical View still defaults to the accepted Cabinet view when no explicit mode is selected.
4. Switching between projection models/views preserves the governed sheet selector; the sheet list remains available after switching to wiring/cabinet views and back.
5. Projection/session payloads carry enough typed sheet-policy evidence for frontend selection without parsing sheet labels or canvas chrome.
6. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including stale sheet-count/default-view assumptions removed or ledgered.

## Tasks/Subtasks

- [x] Add failing document-projection/projection-model tests for the M31 two-sheet customer policy: exact roles, deterministic order, deterministic identity, and source-file-count independence. (AC: 1,2)
- [x] Add failing compiler/runtime/LSP tests proving two-sheet projection survives recompile/reopen with stable sheet ids, publication facts, and occurrence identity recipes. (AC: 2,5)
- [x] Add failing frontend tests proving Graphical View defaults to Cabinet and the sheet selector stays populated after switching projection views/models. (AC: 3,4,5)
- [x] Implement the M31 customer projection policy by extending existing document projection policy and projection-sheet contracts; do not create a second sheet model or frontend-owned sheet registry. (AC: 1,2,5)
- [x] Route projection session payloads through typed policy/sheet facts so Theia consumes governed sheet identity and never derives sheet count from files, tabs, DOM, labels, or canvas bounds. (AC: 3,4,5)
- [x] Prove the M31 projection policy does not mutate downstream artifacts directly and remains compatible with M27/M30 sheet chrome and content-derived viewBox behavior. (AC: 1,2,5)
- [x] Run focused tests and full document-projection/projection-model, compiler, runtime, LSP, language, representation, and frontend suites sequentially. (AC: 1,2,3,4,5)
- [x] Complete mandatory polish/purge review, update cleanup ledger for any retained assumptions, rerun verification after cleanup, and record AC-to-evidence mapping. (AC: 6)

### Review Findings

- [x] [Review][Patch] Normalize GLSP sheet policy evidence so malformed partial objects cannot become governed sheet authority. [`integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`]
- [x] [Review][Patch] Preserve the selected documentation sheet across a Cabinet view round trip. [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`]
- [x] [Review][Defer] `create-semantic-relationship` remains advertised through projection ownership while the old graph-command mutation bypass rejects direct execution. [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeViews.kt`] - deferred, pre-existing Epic 4 graphical relationship workflow boundary tracked by M31-CL-009.

## Dev Notes

### Architecture Guardrails

- The sheet policy is projection policy, not source syntax. Do not add `.athena`, ANTLR4, or Tree-sitter syntax.
- `.athena` source remains canonical semantic persistence; sheets are derived document projection artifacts.
- Sheet count must come from the M31 projection policy, not from `.athena` file count, open editors, Theia widgets, SVG groups, Presentation IR occurrence count, or renderer viewBox.
- Reuse `DocumentProjectionPolicy`, `DocumentProjectionSheetViewRole`, `ProjectionSheet`, `ProjectionSheetPublication`, and projection-session payload types. Do not introduce a parallel frontend sheet model.
- Keep Cabinet as the default Graphical View mode because the user explicitly accepted that behavior after M29. Story 3.1 must not regress it while adding the two-sheet policy.
- Do not directly mutate Presentation IR, Representation Occurrence, Projection Occurrence, route facts, placement overrides, layout geometry, or SVG. This story only defines/reuses sheet policy and session payload derivation.
- Every rejected or unavailable projection path must prove no downstream side effect, following the Epic 2 retrospective action item.
- Every story must finish with the mandatory polish/purge workspace review.

### Existing Code To Extend

- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionPolicyModel.kt`: current built-in policy exposes three sheet roles. Add or specialize the M31 customer policy here or in a cohesive adjacent file while preserving existing public contracts.
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`: existing governed sheet, publication, composition, title-block, and subject contracts. Reuse these rather than inventing view-specific DTOs.
- Compiler/runtime projection derivation code that currently calls `deriveSheets`, `documentationSheets`, or `ProjectionSheet.fromProjectionState`: adapt to the M31 two-sheet policy and stable identity recipe.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt` and projection payload protocol files: transport typed sheet-policy/session facts only.
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts` and `athena-graph-workbench-widget.tsx`: keep sheet selector driven by governed payload facts and preserve Cabinet default behavior.
- Existing frontend tests around sheet selectors and projection view switching should be extended instead of replaced.

### Testing Requirements

- Follow RED-GREEN-REFACTOR and record observed RED failures before production edits.
- Assert exact two-sheet role ids/order and exact absence of file-count-derived sheet identities.
- Use at least one fixture with multiple `.athena` files and assert it still projects to exactly two customer sheets.
- Reopen/recompile proof must compare sheet ids, order, publication title-block data, and occurrence ids.
- Frontend proof must switch projection views/models and assert the sheet selector remains available without relying on DOM text parsing as authority.
- Run Gradle verification strictly sequentially on Windows.
- Run frontend `yarn test`, CodeGraph authority review, `git diff --check`, encoding audit, and stale-name/sheet-assumption scans after cleanup.

### Previous Story Intelligence

- Epic 2 proved that accepted authoring must mutate canonical source first and then rederive downstream projection state. Do not patch sheet state directly to satisfy UI tests.
- Story 2.4 completed `ConnectPortsIntent` removal; active relationship authoring uses `SemanticRelationshipIntent`. Do not reintroduce legacy transport vocabulary.
- Final Story 2.3/2.4 review found that rejected payloads are insufficient evidence. Rejected projection/source-planning paths must prove no runtime placement or sheet mutation.
- Generated frontend `lib` output can retain stale files after source deletion. The polish gate must distinguish ignored generated output from active source and purge stale generated artifacts when practical.

### Scope Boundaries

- No new source grammar, no source-level `sheet` declarations, and no visual syntax.
- No cross-sheet reference navigation; Story 3.3 owns semantic reference occurrences and reveal behavior.
- No full rerouting/composition rebuild beyond what is needed to keep two-sheet identities stable; Story 3.2 owns representation/composition/routing re-derivation.
- No customer-facing graphical creation UX; Epic 4 owns transaction UX.
- No renderer fallback, hard-coded SVG viewBox, duplicate offscreen occurrences, or direct canvas persistence.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 3, Story 3.1 and FR-19/20/24.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - M31 governed authoring and multi-sheet projection requirements.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - downstream artifacts are derived, Theia is an adapter, and source is canonical persistence for M31.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - no hidden document editing and mutation handoff constraints.
- `_bmad-output/implementation-artifacts/m31/epic-2-retro-2026-07-22.md` - Epic 3 carry-forward action items.
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionPolicyModel.kt` - existing document projection policy contract.
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt` - existing governed sheet/publication contracts.

## Dev Agent Record

### Debug Log

- Created via BMAD create-story workflow after Epic 2 retrospective.
- Resumed BMAD dev-story workflow for Story 3.1; loaded sprint status, story context, project config, CodeGraph projection policy context, and TDD instructions.
- RED: `:kernel:document-projection-model:test` failed before production changes because `BuiltInDocumentProjectionPolicies.athenaM31CustomerProjectionV0()` did not exist.
- GREEN: `:kernel:document-projection-model:test` passed after adding the M31 customer policy with two deterministic sheet roles.
- RED: runtime focused projection tests failed before transport changes because sheet policy evidence was absent from runtime session sheets.
- GREEN: runtime focused projection tests passed after policy evidence was derived from projection sheets and carried through runtime models.
- RED: frontend workbench model tests failed before frontend changes because selector roles were not resolved from `policyEvidence`.
- GREEN: frontend workbench tests passed after Theia consumed typed policy evidence and preserved the document sheet selector while switching views.
- Polish scan migrated the remaining Graph GLSP three-sheet fixture to M31 two-sheet typed policy evidence and ledgered the retained M26 display-title fallback as M31-CL-010.
- Code review patch: malformed GLSP policy evidence now fails closed instead of being copied into governed diagram state.
- Code review patch: runtime now preserves the last valid governed sheet selection when switching to Cabinet and back to Documentation.

### Completion Notes

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `athenaM31CustomerProjectionV0()` without changing the legacy M26 `athenaDocumentProjectionV0()` contract.
- Derived exactly two M31 customer sheets, `documentation/sheet/01-control` and `documentation/sheet/02-field-device`, from projection policy rather than source file count, editor tabs, DOM, renderer widgets, or canvas bounds.
- Added `ProjectionSheetPolicyEvidence` and transported it through compiler projection, runtime projection sessions, LSP payloads, GLSP adapter source types, and Theia workbench model selection.
- Preserved Cabinet as the default Graphical View mode and added frontend coverage that sheet selection survives view/model switching.
- AC-to-evidence mapping:
  - AC1: `DocumentProjectionModelContractTest`, compiler M30 sample test, and frontend source-file-count fixture prove exactly two policy-derived sheets.
  - AC2: compiler/runtime/LSP tests compare stable sheet ids, order, publication facts, and occurrence identity recipes across repeated derivation.
  - AC3: `athena-graph-workbench-model.test.mjs` preserves Cabinet default behavior.
  - AC4: `preserves document sheet selector across projection view changes from sheet facts` covers sheet selector persistence across Cabinet/documentation switching.
  - AC5: runtime, LSP, GLSP, and frontend tests assert typed `policyEvidence` instead of label/canvas parsing for M31 payloads.
  - AC6: stale M31 three-sheet id scan returned empty, `git diff --check` exited 0, encoding audit passed, and M31-CL-010 records retained legacy M26 fallback.
- Final verification run sequentially on Windows after code review fixes:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`
  - `yarn test` in `integrations/graph-glsp`
  - `yarn test` in `integrations/graph-glsp` with 6 passing tests after review fix
  - `yarn test` in `ide/theia-frontend` with 162 passing tests
  - `git diff --check`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- Code review triage: 2 patch findings fixed, 1 Epic 4 relationship-authoring boundary deferred to M31-CL-009, 2 false-positive compatibility findings dismissed after source verification.

### File List

- `_bmad-output/implementation-artifacts/m31/3-1-add-the-two-sheet-customer-projection-policy.md`
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/deferred-work.md`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionPolicyModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM11DepthTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM12RendererBenchmarkTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM30SampleProjectCompilerTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionDepthTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionM11DepthRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.d.ts.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts.map`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

### Change Log

- 2026-07-22: Added M31 two-sheet customer projection policy, typed sheet-policy evidence transport, frontend sheet selector preservation, verification evidence, and cleanup ledger entry M31-CL-010.
- 2026-07-22: Addressed code review findings for malformed GLSP policy evidence and runtime documentation sheet persistence across Cabinet round trips.
