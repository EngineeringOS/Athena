---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 4.1
epic: 4
title: Add Graphical Entity Creation Transaction UX
---

# Story 4.1: Add Graphical Entity Creation Transaction UX

## Status

Done

## Story

As a controls engineer,
I want to create a device from Graphical View and inspect its governed transaction,
so that graphical authoring remains transparent and semantic.

## Acceptance Criteria

1. Graphical View renders create-entity actions discovered from authoring capability evidence for eligible concepts only. Theia must not turn a symbol palette item, visible label, SVG node, DOM id, or CSS class into a semantic identity.
2. The user can provide tag/model values for one eligible concept and request a governed preview. The preview shows transaction id, lifecycle, canonical tag, semantic type/model, nested ports, affected semantic ids, backend source diff, representation/composition evidence, diagnostics, and accept/reject controls.
3. Accept applies only the backend-generated revision-guarded source edit as transport; reject/cancel leaves source and downstream projection unchanged. The UI follows returned lifecycle state and never invents success.
4. Normal engineering canvas density does not regress: no persistent cards on the sheet, no visible normal wrappers/hitboxes, no hard-coded viewBox, no duplicate off-sheet occurrences, no center-route fallback, and no generic fallback boxes.
5. Creation UX remains coherent through current editor/source revision changes: stale preview acceptance is blocked with structured diagnostics and no source mutation.
6. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including stale creation widgets, frontend source planning, visible wrapper regressions, generated artifacts, `.tools`, encoding, and cleanup-ledger review.

## Tasks/Subtasks

- [x] Add failing Theia model/protocol tests for create-entity action discovery and preview payload rendering. (AC: 1,2,5)
  - [x] Extend existing `athena-m31-governed-entity-preview.test.mjs`, `athena-component-panel-model.test.mjs`, or adjacent tests instead of creating a parallel action model.
  - [x] Assert available create actions come from typed capability/preview payloads, not DOM/SVG/label/class/palette inference.
  - [x] Assert preview rendering data includes transaction id, lifecycle, canonical tag, semantic type/model, exact nested ports, affected ids, source edit, representation id, composition target, occurrence ids, eligibility, and diagnostics.
  - [x] Assert stale or blocked preview payloads surface structured lifecycle/diagnostic data and expose no enabled accept action.
- [x] Add failing Graphical View source-preservation tests for the creation UX wiring. (AC: 1,3,4,5)
  - [x] Assert `AthenaGraphWorkbenchWidget` requests backend preview through existing authoring protocol and never computes `.athena` insertion spans or serializes source.
  - [x] Assert accept awaits backend decision/source edit application and reject/cancel clear transient UI without mutating source or projection state.
  - [x] Assert creation controls live in dense tool surfaces/panels, not persistent sheet cards or visible canvas wrappers.
  - [x] Assert no production frontend code infers identity from DOM text, SVG id, CSS class, visible label text, or canvas coordinates.
- [x] Implement minimal Graphical View creation UX. (AC: 1,2,3,4,5)
  - [x] Reuse existing authoring preview/decision transport: `buildCreateEntityPreviewRequest`, `buildAuthoringDecisionRequest`, `AthenaAuthoringPreviewPayload`, and `AthenaLspEditorBridgeService`.
  - [x] Reuse existing component/concept catalog grouping where possible; do not introduce a new semantic concept registry in Theia.
  - [x] Add compact controls for concept selection plus tag/model entry using existing IDE-density visual language and no landing-page/card layout.
  - [x] Render preview/diagnostics from typed payloads; include nested ports and source diff without making the canvas itself a document editor.
  - [x] Apply backend source edits through the editor bridge only after accepted decision returns a valid source edit, then refresh/reveal returned canonical subject where available.
  - [x] Fail closed for missing source edit, stale revision, blocked lifecycle, missing preview evidence, or missing active editor coverage.
- [x] Preserve M27-M31 projection, interaction, and representation invariants. (AC: 3,4,5)
  - [x] Preserve Cabinet default and sheet selector behavior while adding creation controls.
  - [x] Preserve transparent normal hitboxes/wrappers and transient-only hover/selection/preview chrome.
  - [x] Do not mutate Presentation IR, Representation Occurrence, Projection Occurrence, sheet output, layout facts, route facts, or rendered geometry.
  - [x] Do not reintroduce `ConnectPortsIntent`, top-level legacy port source generation, frontend source serializers, generic fallback boxes, center fallback routes, or hard-coded viewBox.
- [x] Run focused and regression verification sequentially on Windows. (AC: 1,2,3,4,5)
  - [x] Run focused RED/GREEN frontend tests for action discovery, preview rendering, accept/reject, stale handling, and source-authority scans.
  - [x] Run `yarn test` in `ide/theia-frontend`.
  - [x] Run relevant Gradle suites sequentially if LSP/runtime/protocol code changes; never run Gradle commands concurrently.
  - [x] Run `git diff --check`, stale/fallback/source-authority scans, `.tools` status check, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- [x] Complete mandatory polish/purge review and update story evidence. (AC: 6)
  - [x] Review touched and adjacent UI, CSS, protocol, runtime/LSP transport, tests, docs, generated files, and cleanup ledger.
  - [x] Remove stale creation widgets, preview helpers, obsolete tests/docs, generated residue, or ledger each retained item with owner/reason/target/verification.
  - [x] Record AC-to-evidence mapping, final verification commands, File List, Completion Notes, and Change Log.

### Review Findings

- [x] [Review][Patch] Clear or cancel live create previews when the editor/source/diagram context changes, and prevent hidden stale previews from remaining acceptable. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`:144]
- [x] [Review][Patch] Require the current Athena editor and preview source-edit revision guard to match before accepting create-entity previews. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`:624]
- [x] [Review][Patch] Follow backend decision lifecycle/status before applying returned source edits or clearing rejected previews. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`:1388]
- [x] [Review][Patch] Normalize malformed/partial preview evidence instead of crashing while rendering typed preview tables. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`:733]
- [x] [Review][Patch] Compare decision source edit identity and revision guard against preview evidence before applying it. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`:1391]
- [x] [Review][Patch] Remove hard-coded `component:` fallback from post-accept reveal and use typed entity evidence generically. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`:1393]
- [x] [Review][Patch] Add behavior-level frontend tests for stale preview, close/cancel, malformed evidence, accept/reject failure, and generic reveal guard paths. [`ide/theia-frontend/scripts/athena-m31-governed-entity-preview.test.mjs`:5]

## Dev Notes

### Architecture Guardrails

- This story implements M31 Epic 4 Story 4.1 and contributes to FR-1, FR-3, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-31, FR-38, FR-45, FR-47, FR-48, FR-49, and FR-50.
- Theia remains an adapter. It may collect values, request preview/decision, render typed payloads, apply returned source edits as transport, and refresh/reveal. It may not own semantic identity, concept eligibility, source edit planning, representation selection, composition, routing, or final geometry.
- Capability discovery precedes transaction creation. Use the existing M29 `SemanticCapabilityRegistry`/M31 authoring capability evidence path; do not create a second frontend registry or palette-owned semantic catalog.
- Every mutable action needs a revision-bound preview before acceptance. Acceptance must carry the exact preview/Revision Guard and must block stale changes without source mutation.
- Engineering Concept Templates and Representation Definitions remain separate. A UI action selects a semantic concept/template; representation is downstream M30 policy/binding evidence.
- Generated source must use compact nested-port `.athena` syntax. Do not emit legacy top-level `port Device.name` declarations.
- Source edits are backend-owned. Theia must not compute insertion positions, serialize `.athena`, parse source text to find a system block, or repair returned edits.
- Normal sheet chrome remains visually quiet and transparent. Use dense Theia controls/panels/popovers; do not place persistent cards on the engineering canvas.
- Missing preview evidence, missing source edit, stale revision, blocked lifecycle, missing active editor coverage, or failed apply must fail closed with structured diagnostics.
- Every story ends with mandatory polish/purge. Do not mark done without fresh evidence and cleanup review.

### Existing Code To Inspect And Extend

- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`: Graphical View UI shell, current preview/decision hooks, bottom dock, sheet controls, reference marker navigation, layout/relationship flows. Extend this as an adapter; avoid new authority.
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`: frontend transport builders including create preview and decision requests.
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`: editor revision/source-edit application boundary. Keep Revision Guard checks here or in existing helper paths.
- `ide/theia-frontend/src/browser/athena-component-panel-model.ts` and `athena-component-panel-widget.tsx`: existing concept/component catalog grouping and preview affordances; reuse instead of inventing a second catalog.
- `ide/theia-frontend/src/browser/athena-component-knowledge-protocol.ts`: current component knowledge payloads if action catalog data needs to be surfaced.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`, `AthenaAuthoringSourceEditProtocol.kt`, `AthenaGovernedAuthoringExecution.kt`, and `AthenaLanguageServer.kt`: backend preview/decision transport already used by Story 2.3. Touch only if the frontend needs a missing typed field.
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedEntityCreationPreviewService.kt` and `GovernedEntityCreationModels.kt`: existing governed entity preview/result/evidence service. Do not duplicate in frontend.
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEngineeringConceptTemplates.kt`: electrical concept templates for the first proof slice.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt`: backend-only source planning. Theia must not replicate it.

### Previous Story Intelligence

- Story 2.3 implemented governed nested-port entity creation preview/acceptance, exact source evidence, M30 representation/composition resolution, stale acceptance protection, and typed LSP/Theia transport.
- Story 2.4 migrated relationship authoring to generic `SemanticRelationshipIntent` and removed legacy `ConnectPortsIntent`; do not restore connect-port-specific contracts while adding creation UX.
- Story 3.1 established exactly two M31 document sheets and preserved Cabinet as default projection.
- Story 3.2 removed duplicate off-sheet `_reference` visual occurrences, generic fallbacks, and fixed/oversized framing. Do not satisfy preview UX by adding duplicate visual nodes.
- Story 3.3 added typed cross-sheet reference links and fail-closed navigation; do not parse labels or SVG/DOM ids to reveal created entities.
- Recent review follow-up in Story 3.3 required frontend navigation to validate projection facts and stop when sheet switching fails. Apply the same fail-closed standard to creation accept/reveal.
- Sprint action items still open for future stories: no-side-effect assertions on rejected authoring/projection stories, durable source planning before runtime side effects, and generated artifact purge after source deletion.

### Testing Requirements

- Follow RED-GREEN-REFACTOR. Record observed RED failures before production edits.
- Use structured tests as acceptance authority; screenshots are secondary only.
- Frontend focused tests must cover action discovery, preview evidence, accept/reject, stale/blocked diagnostics, no frontend source planning, and no DOM/SVG/label identity inference.
- If only Theia files change, run `yarn test` in `ide/theia-frontend`. If LSP/runtime/protocol code changes, run the relevant Gradle suites sequentially before frontend tests.
- Final polish must run `git diff --check`, scans for `ConnectPortsIntent`, frontend `.athena` serializers, DOM/SVG/label inference, `_reference`, generic fallback, center fallback, hard-coded `viewBox="0 0 1680 1188"`, visible normal wrapper regressions, `.tools` status, and encoding audit.

### Scope Boundaries

- This story is Graphical View entity creation UX only. Story 4.2 owns graphical relationship and cross-surface reveal workflow polish.
- No full component library browser, marketplace, macro editor, symbol editor, QET importer, AI agent, bulk creation, update/remove UI, undo/redo, print/PDF, or new source syntax.
- Do not implement accepted entity removal, accepted update, or relationship removal UX here.
- Do not create a second authoring runtime, concept registry, source planner, representation binder, or projection mutation path.
- Do not directly mutate document sheet membership, layout facts, route facts, Presentation IR, Representation Occurrence, Projection Occurrence, or rendered geometry.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 4, Story 4.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - FR-1..FR-9, FR-31, FR-38, FR-45, FR-47..FR-50, NFR-2, NFR-4, NFR-7.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md` - Graphical-First Is Not Geometry-First, Revision-Bound Preview, Core Product Slice.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-3, AD-4, AD-6, AD-10, AD-12, AD-15, AD-16, AD-17, AD-18.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Frontend Adapter Contract and Product Proof Contract.
- `_bmad-output/implementation-artifacts/m31/2-3-create-a-nested-port-entity-through-governed-mutation.md` - backend creation preview/acceptance evidence.
- `_bmad-output/implementation-artifacts/m31/3-3-add-cross-sheet-reference-and-reopen-stability.md` - latest fail-closed frontend navigation review lesson.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created via BMAD create-story workflow after Story 3.3 review follow-ups completed.
- Loaded M31 sprint status, epics, PRD/addendum, architecture spine, authoring contract, Story 2.3 creation evidence, Story 3.3 review lessons, git history, and CodeGraph context for governed entity preview plus Theia authoring hooks.
- RED frontend: `yarn test` in `ide/theia-frontend` failed because `buildCreateEntityPreviewRequest` could not carry Graphical View origin/model properties and `AthenaGraphWorkbenchWidget` had no create-entity transaction UX.
- GREEN frontend: `yarn test` in `ide/theia-frontend` passed after Graphical View gained governed create controls, typed preview rendering, accept/reject transport, and source-authority tests.
- Polish/purge: `git diff --check` passed with line-ending warnings only; source-authority scan found forbidden terms only inside tests that assert absence; `.tools` status clean; encoding audit passed.
- BMAD code review: Blind Hunter and Edge Case Hunter returned patch findings; Acceptance Auditor initially timed out and later returned aligned AC findings before production edits. RED review tests failed for missing lifecycle/source-match helpers and stale/cancel/diff/diagnostic/reveal guards; GREEN `yarn test` passed after fixes.
- Review polish/purge: `git diff --check` passed with line-ending warnings only; touched-production stale scan found only legal `reference-marker` control names; hard-coded `viewBox="0 0 1680 1188"` absent; `.tools` status clean; encoding audit passed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Extended create-entity preview request shape to carry Graphical View origin and model property while preserving existing palette request behavior.
- Added Graphical View create-entity controls as a transient dense overlay using existing component knowledge grouping and backend authoring preview/decision transport.
- Rendered governed entity preview evidence from typed payloads: transaction/intent ids, lifecycle, canonical tag, type/model, nested ports, affected ids, source edit, representation, composition, occurrence ids, diagnostics, and accept/reject actions.
- Acceptance now requires eligible preview evidence and applies only the backend-returned source edit through `AthenaLspEditorBridgeService`; rejected or missing-evidence flows fail closed without source planning in Theia.
- Review follow-ups now clear stale previews on editor/source/diagram changes, cancel hidden previews through the decision protocol, require current editor/source-edit/revision-guard match, require committed lifecycle and zero diagnostics before source apply, render diagnostic authority/lifecycle plus backend source-impact text, and reveal generic created entity ids without a `component:` namespace assumption.
- AC evidence: AC1 covered by `athena-m31-governed-entity-preview.test.mjs` Graphical View adapter test and component grouping reuse; AC2 covered by preview evidence render assertions in the same test and `athena-authoring-protocol.test.mjs` graph-origin/model request; AC3 covered by source-preservation assertions for decision/apply wiring; AC4 covered by dense overlay CSS plus M30/M31 visual/source scans; AC5 covered by blocked/eligible preview gating and existing revision-guard tests in the frontend suite; AC6 covered by final verification and purge scans.
- Final verification passed: `yarn test` in `ide/theia-frontend` (171/171), `git diff --check`, `.tools` status check, touched-production stale/source-authority scan, hard-coded viewBox scan, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### File List

- `_bmad-output/implementation-artifacts/m31/4-1-add-graphical-entity-creation-transaction-ux.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-authoring-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-governed-entity-preview.test.mjs`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`

### Change Log

- 2026-07-22: Created Story 4.1 context for BMAD dev-story execution.
- 2026-07-22: Implemented Graphical View create-entity transaction UX, graph-origin/model preview request support, typed preview rendering, source-authority guards, and mandatory polish/purge evidence.
- 2026-07-22: Completed BMAD code-review follow-ups for stale preview cancellation, lifecycle-gated acceptance, source-edit evidence matching, structured diagnostics, backend source-impact display, generic entity reveal, and review verification.
