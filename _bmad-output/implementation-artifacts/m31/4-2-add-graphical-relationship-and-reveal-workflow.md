---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 4.2
epic: 4
title: Add Graphical Relationship And Reveal Workflow
---

# Story 4.2: Add Graphical Relationship And Reveal Workflow

## Status

Done

## Story

As a controls engineer,
I want to connect compatible terminals and navigate the result across surfaces,
so that one canonical relationship remains understandable everywhere.

## Acceptance Criteria

1. With a selected source terminal, Graphical View displays relationship candidates from typed authoring capability/semantic compatibility evidence. Endpoint identity must come from projection facts and canonical subjects, never SVG coordinates, DOM ids, labels, CSS classes, or route geometry.
2. Choosing a compatible target requests a governed `SemanticRelationshipIntent` preview showing endpoint ids, relationship type, compatibility, route preview facts, source impact, Revision Guard, diagnostics, lifecycle, and accept/reject/cancel controls.
3. Accept applies only the backend-generated revision-guarded source edit as transport, then refreshes the anchored route after recompile. Reject/cancel/stale/blocked decisions leave source, semantic model, projection, route facts, and document sheets unchanged.
4. Selecting the new entity, nested port, or relationship can reveal source, Outline nested port, Inspector, Problems, graph occurrence, and sheet target through canonical identity where available. Missing targets fail closed with structured diagnostics.
5. M27-M31 invariants remain intact: Cabinet default, sheet selector, exactly two governed sheets, transparent normal chrome, content-derived viewBox, no duplicate off-sheet occurrences, no center fallback routes, no generic fallback boxes, no legacy `ConnectPortsIntent`, and no frontend source serializer.
6. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including stale frontend compatibility paths, stale previews, DOM/SVG/label inference, direct downstream mutation, generated artifacts, `.tools`, encoding, and cleanup-ledger review.

## Tasks/Subtasks

- [x] Add failing frontend and protocol tests for relationship candidate discovery. (AC: 1,5)
  - [x] Assert Graphical View uses capability-derived relationship candidates and semantic compatibility evidence instead of DOM/SVG/label/class/coordinate inference.
  - [x] Assert incompatible targets surface explicit rejected reasons with diagnostic authority and lifecycle stage.
  - [x] Assert no `ConnectPortsIntent`, connect-ports transport names, guided-connection compatibility adapter, or frontend-owned endpoint eligibility table is restored.
- [x] Add failing preview/decision tests for graphical relationship transactions. (AC: 2,3)
  - [x] Assert preview requests use `buildSemanticRelationshipPreviewRequest` and generic `SemanticRelationshipIntent` payload fields only.
  - [x] Assert preview rendering includes endpoint ids, relationship type, compatibility, route preview facts, source impact, Revision Guard evidence, diagnostics, lifecycle/status, and accept/reject/cancel controls.
  - [x] Assert accept requires current editor/source/revision match, committed lifecycle, zero blocking diagnostics, and source-edit equality with preview evidence before applying.
  - [x] Assert reject/cancel/stale/blocked decisions clear or preserve transient UI correctly without source or projection mutation.
- [x] Implement minimal Graphical View relationship UX using existing M28/M29/M31 contracts. (AC: 1,2,3)
  - [x] Reuse `buildSemanticRelationshipPreviewRequest`, `buildAuthoringDecisionRequest`, `AthenaAuthoringPreviewPayload.relationshipEvidence`, and `AthenaLspEditorBridgeService`.
  - [x] Use projection occurrence/port subject facts and existing semantic selection aliases to choose endpoints; do not parse labels, route labels, DOM/SVG ids, or coordinates.
  - [x] Render a dense transient relationship preview/control surface; do not place persistent cards on the engineering sheet.
  - [x] Apply backend source edits through the editor bridge only after committed accepted decision and matching preview evidence.
  - [x] Cancel hidden/abandoned previews through the decision protocol and clear stale previews on editor/source/diagram context changes.
- [x] Implement cross-surface reveal workflow polish for created relationships and nested ports. (AC: 4,5)
  - [x] Reveal canonical subjects through existing `AthenaSemanticSelectionService`, semantic selection model, typed cross-reference links, and Outline/LSP source ranges where available.
  - [x] Ensure sheet switching stops if target sheet/view cannot be selected, preserving the Story 3.3 fail-closed navigation lesson.
  - [x] Surface structured missing-target diagnostics instead of silently falling back to visible text, first DOM match, or route geometry.
  - [x] Preserve Cabinet default, sheet selector visibility, exactly two sheet options, and current focus where meaningful.
- [x] Preserve M27-M31 projection, interaction, representation, and mutation invariants. (AC: 3,5)
  - [x] Do not mutate Presentation IR, Representation Occurrence, Projection Occurrence, sheet output, layout facts, route facts, or rendered geometry.
  - [x] Do not reintroduce generic fallback boxes, center fallback routes, fixed `viewBox="0 0 1680 1188"`, duplicate `_reference` normal occurrences, or visible normal wrappers/hitboxes.
  - [x] Keep route geometry as downstream evidence, not relationship authority.
- [x] Run focused and regression verification sequentially on Windows. (AC: 1,2,3,4,5)
  - [x] Run RED/GREEN focused frontend tests for candidate discovery, preview evidence, accept/reject/cancel/stale, source authority, route/reveal identity, and no DOM/SVG/label inference.
  - [x] Run `yarn test` in `ide/theia-frontend`.
  - [x] Run relevant Gradle suites sequentially if LSP/runtime/protocol/compiler code changes; never run Gradle commands concurrently.
  - [x] Run `git diff --check`, stale/fallback/source-authority scans, `.tools` status check, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- [x] Complete mandatory polish/purge review and update story evidence. (AC: 6)
  - [x] Review touched and adjacent Graphical View, semantic selection, LSP protocol, runtime/compiler transport, tests, docs, generated files, and cleanup ledger.
  - [x] Remove stale relationship widgets/helpers/tests/docs or ledger every retained item with owner, reason, target milestone, and verification.
  - [x] Record AC-to-evidence mapping, final verification commands, File List, Completion Notes, and Change Log.

### Review Findings

- [x] [Review][Patch] Async relationship preview/decision responses could race stale or newer preview state. [ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx:1685] - fixed with `connectPreviewRequestToken`, `isCurrentConnectPreview`, stale-response ignores, and current-preview checks before accept/reject/cancel side effects.
- [x] [Review][Patch] Relationship source-edit matching did not compare edit and selection offsets. [ide/theia-frontend/src/browser/athena-authoring-protocol.ts:299] - fixed by carrying source-edit offsets through the LSP payload and requiring start/end/selection offset equality before applying.
- [x] [Review][Patch] Relationship reveal fell back to the target endpoint when no relationship identity was returned. [ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx:1852] - fixed by revealing only explicit `connection:` or `relationship:` identities and otherwise failing closed.

## Dev Notes

### Architecture Guardrails

- This story implements M31 Epic 4 Story 4.2 and contributes to FR-13, FR-14, FR-15, FR-16, FR-17, FR-27, FR-32, FR-36, FR-38, FR-41, FR-42, FR-43, FR-44, FR-47, FR-50, and FR-51.
- Theia remains an adapter. It may collect endpoint intent, request preview/decision, render typed relationship evidence, apply returned source edits as transport, and refresh/reveal. It may not decide compatibility, serialize `.athena`, infer endpoints from visuals, route relationships, or mutate downstream projection artifacts.
- Use generic relationship language and contracts. Do not reintroduce `ConnectPortsIntent`, connect-ports request builders, connect-ports-specific transport names, or compatibility conversion layers.
- Route preview facts are evidence only. Accepted relationship truth is canonical semantic relationship source/model output.
- Every mutable action needs a Revision Guard preview before acceptance. Acceptance must fail closed on stale source, missing active editor, lifecycle not committed, diagnostics, or source-edit mismatch.
- Reject and cancel leave source and downstream projections unchanged. Hidden/abandoned preview panels must not leave an acceptable stale transaction.
- Reveal uses canonical subject identity and governed projection/source facts. It must not parse visible text, route labels, DOM ids, SVG ids, CSS classes, `_reference` suffixes, or coordinates.
- Normal sheet chrome remains visually quiet and transparent. Use dense transient controls/panels only.
- Every story ends with mandatory polish/purge. Do not mark done without fresh AC evidence and cleanup review.

### Existing Code To Inspect And Extend

- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`: current relationship mode, create-preview lifecycle guards from Story 4.1, sheet/reference controls, preview/decision hooks, semantic reveal handling. Extend as adapter only.
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`: generic preview/decision builders and Story 4.1 guard helpers. Reuse or extend helper functions instead of duplicating lifecycle/source-edit matching.
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`: typed projection occurrence, route, port, reference, and sheet facts. Use these facts for endpoint identity and route evidence.
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts` and `athena-semantic-selection-service.ts`: canonical reveal, aliases, projection occurrence resolution, and cross-surface selection. Extend here only if reveal needs a typed missing-target result.
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`: request/apply authoring preview/decision and Revision Guard source-edit application boundary.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`, `AthenaSemanticRelationshipSourceEditProtocol.kt`, `AthenaGovernedAuthoringExecution.kt`, and `AthenaLanguageServer.kt`: backend transport for relationship preview/decision if frontend needs missing typed fields.
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewService.kt`: governed relationship preview/result/evidence service. Do not duplicate compatibility or source planning in frontend.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt`: backend-only source planning.

### Previous Story Intelligence

- Story 2.4 completed the generic `SemanticRelationshipIntent` migration and removed legacy `ConnectPortsIntent`; 4.2 must not restore old transport names while adding UX.
- Story 3.2 removed duplicate off-sheet `_reference` visual occurrences, generic fallback boxes, center fallback routes, and fixed/oversized framing. Route preview UI cannot add any of these back.
- Story 3.3 established typed cross-reference navigation and required sheet switching to fail closed when the target cannot be selected.
- Story 4.1 review follow-ups added lifecycle/source-edit guard helpers, stale preview clearing on editor/source/diagram changes, explicit cancel decisions, structured diagnostic rendering, backend source-impact display, and generic entity reveal without `component:` assumptions. Reuse these patterns for relationship preview/acceptance.
- Sprint action items still open for future stories: no-side-effect assertions on rejected authoring/projection stories, durable source planning before runtime side effects in Story 4.2 graph workflows, and generated artifact purge after source deletion.

### Testing Requirements

- Follow RED-GREEN-REFACTOR. Record observed RED failures before production edits.
- Use structured tests as acceptance authority; screenshots are secondary only.
- Frontend focused tests must cover candidate discovery, typed preview evidence, committed lifecycle gating, stale/rejected/cancelled no-side-effect behavior, route/reveal identity, and no DOM/SVG/label/coordinate inference.
- If only Theia files change, run `yarn test` in `ide/theia-frontend`. If LSP/runtime/compiler code changes, run relevant Gradle suites sequentially before frontend tests.
- Final polish must run `git diff --check`, scans for `ConnectPortsIntent`, connect-ports transport names, frontend `.athena` serializers, DOM/SVG/label inference, `_reference`, generic fallback, center fallback, hard-coded `viewBox="0 0 1680 1188"`, visible normal wrapper regressions, `.tools` status, and encoding audit.

### Scope Boundaries

- This story is graphical relationship creation and reveal workflow polish only.
- No accepted relationship removal UX, entity update/removal UX, full undo/redo, AI agent workflow, symbol/library expansion, print/PDF, new `.athena` syntax, or QET importer.
- Do not create a second capability registry, relationship runtime, source planner, route authority, reveal registry, or frontend compatibility model.
- Do not directly mutate document sheet membership, layout facts, route facts, Presentation IR, Representation Occurrence, Projection Occurrence, or rendered geometry.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 4, Story 4.2.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - FR-13..FR-17, FR-27, FR-32, FR-36, FR-38, FR-41..FR-44, FR-47, FR-50, FR-51; NFR-2, NFR-4, NFR-6, NFR-7, NFR-8, NFR-12.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md` - Graphical-First Is Not Geometry-First, Relationship Serialization, Revision-Bound Preview, Failure Taxonomy, No Hidden Projection Editing.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-3, AD-4, AD-8, AD-10, AD-11, AD-12, AD-13, AD-15, AD-16, AD-18.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Frontend Adapter Contract, Relationship Rules, Revision Guard, Mutation Handoff, Product Proof Contract.
- `_bmad-output/implementation-artifacts/m31/2-4-create-semantic-relationships-and-retire-legacy-connect-ports.md` - generic relationship authoring and legacy cleanup evidence.
- `_bmad-output/implementation-artifacts/m31/3-3-add-cross-sheet-reference-and-reopen-stability.md` - latest fail-closed cross-sheet navigation lesson.
- `_bmad-output/implementation-artifacts/m31/4-1-add-graphical-entity-creation-transaction-ux.md` - create-preview lifecycle guard and review follow-up patterns.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created via BMAD create-story workflow after Story 4.1 reached done.
- Loaded M31 sprint status, epics, PRD/addendum, architecture spine, authoring contract, Story 4.1 review lessons, and recent git history.
- RED: `yarn test` in `ide/theia-frontend` failed with 4 expected failures in `athena-m31-governed-relationship-preview.test.mjs`: missing relationship candidate helper, relationship preview renderer, committed/source-evidence accept gate, and cancel/clear stale preview protocol.
- GREEN: `yarn test` in `ide/theia-frontend` passed after Graphical View relationship adapter implementation: 176 tests passed.
- Polish/Purge: `git diff --check` passed with line-ending warnings only; `.tools` status was clean; production scans found no `ConnectPortsIntent`, `buildConnectPortsPreviewRequest`, `connect-ports`, DOM/SVG/text inference, fixed oversized `viewBox`, frontend serializer, backend source planner import, `_reference` duplicate suffix, center fallback, or generic fallback in touched production files; encoding audit passed.
- Code Review: Blind Hunter and Acceptance Auditor failed due model capacity; Edge Case Hunter initially timed out and then returned 3 actionable findings. Main-thread review also found stale-preview backend cancellation. All findings were patched.
- Review RED: `yarn test` in `ide/theia-frontend` failed after adding assertions for stale backend cancellation, current preview gating, source-edit offset equality, and no target fallback reveal.
- Review GREEN: `yarn test` in `ide/theia-frontend` passed after review fixes: 176 tests passed.
- LSP transport verification: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed after adding source-edit offset fields to the LSP payload.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added a focused M31 relationship preview regression test covering generic transport, capability-derived candidate evidence, typed preview rendering, committed lifecycle gating, source-edit evidence equality, cancel/reject/stale clearing, and DOM/SVG/label inference bans.
- Graphical View now renders a dense transient semantic relationship preview panel with source/target endpoint ids, relationship type, compatibility, route preview, Revision Guard, diagnostics, source impact, and accept/reject/cancel controls.
- Relationship accept now fails closed unless the active Athena editor matches the preview source, the backend decision is committed with no diagnostics, and the returned source edit matches `relationshipEvidence.sourceEdit`.
- Reject/cancel relationship preview decisions now inspect the backend decision result before clearing, and stale previews send backend cancellation on editor/source/diagram context changes.
- Relationship accept/reject/cancel ignores stale async responses unless the same preview remains current and the editor still matches the preview source.
- Source-edit equality now includes uri, admitted text, Revision Guard, suggested id membership, edit offsets, and selection offsets.
- Reveal after accept uses the backend suggested semantic id or an explicit relationship identity from affected ids through `AthenaSemanticSelectionService`; it no longer falls back to the target endpoint when no relationship identity exists.
- AC-to-evidence: AC1 covered by `athena-m31-governed-relationship-preview.test.mjs` candidate/ban assertions and `isRelationshipCandidateNode`; AC2 covered by `renderSemanticRelationshipPreview`; AC3 covered by `acceptConnectPreview`, `rejectConnectPreview`, `cancelConnectPreview`, `currentEditorMatchesConnectPreview`, and source-evidence matching tests; AC4 covered by canonical `selectSemanticId` reveal and existing semantic selection tests in `yarn test`; AC5 covered by invariant scans and existing M27-M31 regression tests; AC6 covered by final polish/purge commands and this evidence record.

### File List

- `_bmad-output/implementation-artifacts/m31/4-2-add-graphical-relationship-and-reveal-workflow.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
- `ide/theia-frontend/scripts/athena-authoring-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-governed-relationship-preview.test.mjs`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`

### Change Log

- 2026-07-22: Created Story 4.2 context for BMAD dev-story execution.
- 2026-07-22: Implemented Graphical View governed semantic relationship preview/decision UX with lifecycle/source-evidence gates and polish/purge evidence.
- 2026-07-22: Resolved BMAD review findings for stale async preview races, strict source-edit offset equality, stale backend cancellation, and relationship-only reveal identity; marked Story 4.2 done.
