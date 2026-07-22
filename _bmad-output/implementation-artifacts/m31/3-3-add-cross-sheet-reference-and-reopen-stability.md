---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 3.3
epic: 3
title: Add Cross-Sheet Reference And Reopen Stability
---

# Story 3.3: Add Cross-Sheet Reference And Reopen Stability

## Status

Done

## Story

As a controls engineer,
I want to follow one semantic relationship across the two document sheets,
so that the projected document remains coherent after save and reopen.

## Acceptance Criteria

1. When one semantic relationship spans control and field/device sheet occurrences, document projection emits one first-class semantic continuation or cross-sheet reference occurrence that links stable source and target occurrence ids. Display notation derives from governed document locations, not from label text, DOM ids, SVG geometry, or frontend state.
2. Activating the cross-sheet reference reveals the target by canonical semantic identity. Graphical View switches to the target sheet and focuses the canonical subject without guessing from visible labels; source, Inspector, and Problems reveal targets remain coherent where available.
3. After closing and reopening the unchanged project, semantic ids, relationship ids, sheet ids, projection occurrence ids, cross-reference ids, route ids, and active sheet selector behavior match the pre-close proof.
4. Cross-reference and reveal failures are structured and fail closed. Missing source occurrence, target occurrence, sheet id, subject id, or projection facts must not create fallback aliases, parse label text, or silently switch to an arbitrary sheet.
5. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including stale identity, label-guessing, DOM/SVG inference, duplicate `_reference` fixture, hard-coded viewBox, direct downstream mutation, generated artifact, and `.tools` review.

## Tasks/Subtasks

- [x] Add failing compiler/projection tests for first-class cross-sheet reference facts. (AC: 1,4)
  - [x] Use the M30/M31 documentation projection sample and assert at least one relationship or subject spans `documentation/sheet/01-control` and `documentation/sheet/02-field-device`.
  - [x] Assert each cross-sheet reference exposes stable source occurrence id, target occurrence id, source sheet id, target sheet id, canonical semantic id, and compact governed notation.
  - [x] Assert display notation is derived from sheet/order/location facts and does not parse node labels, route labels, DOM ids, SVG ids, or `_reference` suffixes.
  - [x] Assert missing source/target occurrence or sheet facts produce structured diagnostics or absent reference facts, not fallback aliases.
- [x] Add failing runtime/LSP tests for cross-reference transport and reveal. (AC: 2,3,4)
  - [x] Extend runtime projection payloads only if the existing `ProjectionCrossReference` mapping cannot carry source/target occurrence and location identity safely.
  - [x] Assert LSP projection payload exposes stable cross-reference/reveal facts after switching to documentation sheets.
  - [x] Assert activating a reference switches to the target sheet and canonical subject through runtime-owned projection/session state.
  - [x] Assert reveal fails closed when the target sheet or occurrence is unavailable.
- [x] Add failing Theia/GLSP model tests for reference marker navigation without frontend inference. (AC: 2,4)
  - [x] Extend `athena-graph-workbench-model` or adjacent reference-marker tests so marker target resolution uses typed reference payloads and governed projection occurrences.
  - [x] Preserve Story 3.1 sheet selector behavior and Story 3.2 content-derived framing while reference navigation changes active sheet focus.
  - [x] Assert no production frontend code infers reference target from label text, DOM text, SVG id, CSS class, or `_reference` suffix.
- [x] Add failing reopen-stability tests. (AC: 3)
  - [x] Capture pre-close semantic, relationship, sheet, occurrence, cross-reference, and route ids from a ready projection.
  - [x] Reopen the same unchanged project and assert the captured ids and sheet selector entries match exactly.
  - [x] Include both active documentation sheet and projection-mode round trips so Cabinet default and sheet selector availability do not regress.
- [x] Implement the minimal cross-sheet reference and reveal changes. (AC: 1,2,3,4)
  - [x] Reuse `ProjectionCrossReference`, `ProjectionSheetSubject`, `ProjectionSheetPolicyEvidence`, runtime projection support, existing reference marker controls, and semantic selection/reveal infrastructure before adding new contracts.
  - [x] If `ProjectionCrossReference` is extended, keep it renderer-neutral and platform-owned; do not add DOM, SVG, CSS, canvas, or visual-coordinate authority.
  - [x] Do not reintroduce duplicate off-sheet visual occurrences, `_reference` production ids, generic fallback boxes, or center fallback routes.
  - [x] If a legacy defensive fixture must remain, update the cleanup ledger with owner, reason, target story/milestone, and verification.
- [x] Run focused and regression verification sequentially on Windows. (AC: 1,2,3,4)
  - [x] Run focused RED/GREEN tests for compiler/projection/runtime/LSP/frontend changes.
  - [x] Run relevant Gradle suites sequentially; never run Gradle commands concurrently.
  - [x] Run `yarn test` in `integrations/graph-glsp` and `ide/theia-frontend` if frontend or GLSP payload/model code changes.
  - [x] Run `git diff --check`, stale/fallback/reference scans, `.tools` status check, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- [x] Complete mandatory polish/purge review and update story evidence. (AC: 5)
  - [x] Review touched and adjacent source, tests, generated output, docs, samples, and cleanup ledger.
  - [x] Remove stale code/docs/tests/generated artifacts or ledger each retained item.
  - [x] Record AC-to-evidence mapping, final verification commands, File List, Completion Notes, and Change Log.
- [x] Review Follow-ups (AI). (AC: 2,4,5)
  - [x] [AI-Review][High] Reject typed cross-reference links when source/target sheets, occurrence ids, or compact notation are not proven by current projection facts.
  - [x] [AI-Review][High] Stop reference marker navigation before semantic selection when governed sheet switching fails.

## Dev Notes

### Architecture Guardrails

- This story implements M31 FR-20, FR-23, FR-24, and FR-36. It must preserve NFR-3 and NFR-6: structured proof is acceptance authority, and frontend behavior cannot own semantic truth.
- Do not add `.athena`, ANTLR4, or Tree-sitter syntax in this story. Cross-sheet reference is downstream projection/interaction evidence derived from existing semantic relationships and document policy.
- Sheet count, sheet role, sheet order, and sheet identity come from `BuiltInDocumentProjectionPolicies.athenaM31CustomerProjectionV0()` and `ProjectionSheetPolicyEvidence`; they must not derive from `.athena` file count, editor tabs, DOM state, or frontend widget lifecycle.
- Projection occurrence identity must derive deterministically from canonical semantic and document inputs. Reopen with unchanged source must reproduce the same ids.
- Cross-reference facts are renderer-neutral downstream facts. Canonical engineering truth remains upstream semantic ids and `SemanticRelationshipIntent`.
- Reveal uses canonical subject identity and governed projection occurrence/sheet facts. It must not parse visible label text, route label text, DOM text, SVG ids, CSS classes, `_reference` suffixes, or canvas coordinates.
- Renderer remains paint-only. It may show reference controls from typed payloads and transient state, but it must not invent reference targets or repair missing projection facts.
- Missing reference/reveal facts fail closed with structured diagnostics or absent actions. No fallback aliases, arbitrary first sheet switches, generic boxes, or center routes.
- Preserve Story 3.1 and 3.2 outcomes: Cabinet remains the default projection, sheet selector survives projection-mode switching, normal hitboxes/wrappers are transparent, viewBox/framing stays content-derived, and no duplicate off-sheet occurrences are produced.
- Every story must finish with the mandatory polish/purge workspace review.

### Existing Code To Inspect And Extend

- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReferences.kt`: current `ProjectionCrossReference` only carries semantic id, kind, sheet ids, and occurrence ids. Extend only if source/target occurrence/location identity cannot be represented safely.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`: `deriveCrossReferences()` currently groups repeated subjects across documentation sheets. It is the likely compiler entry point for first-class cross-sheet reference derivation.
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`: maps `ProjectionCrossReference` to runtime payloads. Keep sorting deterministic and transport-safe.
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt` and `AthenaExecutionContext.switchActiveProjectionView()`: preserve active sheet selection and reopen-stable session behavior.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt` and `AthenaProjectionSessionProtocol.kt`: expose any extended reference/reveal payloads without frontend inference.
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`: model already exposes `referenceMarkers`, `sheetChrome.crossReferenceMarkers`, sheet selector entries, and scene bounds. Extend typed reference resolution here rather than in React DOM handlers.
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`: `renderReferenceMarkerControls()` and `handleReferenceMarkerClick()` are the UI entry points. Keep the widget an adapter to typed model facts.
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs` and `athena-semantic-selection-model.test.mjs`: existing tests cover reference marker navigation, repeated occurrence handling, and no DOM semantic inference. Extend these rather than adding screenshot-only proof.
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`: update only if the transport payload needs additional typed cross-reference fields.

### Previous Story Intelligence

- Story 3.1 established exactly two M31 document sheets: `documentation/sheet/01-control` and `documentation/sheet/02-field-device`, with typed policy evidence and Cabinet as the default projection.
- Story 3.1 fixed malformed sheet-policy evidence to fail closed; apply the same fail-closed rule to malformed cross-reference/reveal evidence.
- Story 3.2 removed compiler-generated duplicate off-sheet `_reference` nodes. Do not restore that visual duplication to satisfy cross-reference ACs.
- Story 3.2 changed documentation canvas derivation to use projected nodes, labels, and route endpoints. Do not regress to node-only or fixed-size framing.
- Story 3.2 ledgered retained test-only `_reference` fixtures as M31-CL-011 for Story 5.3. If Story 3.3 touches those fixtures, either remove them deliberately or update the ledger; do not let them become normal M31 production truth.
- Epic 2 completed generic relationship authoring through `SemanticRelationshipIntent`; do not reintroduce `ConnectPortsIntent`, connect-ports-specific transport, or frontend source serializers.

### Testing Requirements

- Follow RED-GREEN-REFACTOR. Record observed RED failures in the Dev Agent Record before production edits.
- Use structured compiler/runtime/LSP/frontend proof as acceptance authority. Screenshots are secondary only.
- Run Gradle verification strictly sequentially on Windows.
- Focused suites likely include:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `yarn test` in `integrations/graph-glsp` if GLSP payloads change
  - `yarn test` in `ide/theia-frontend` if Theia model/widget code changes
- Also run any affected representation/routing/domain suites if reference derivation touches presentation, routing, or electrical domain facts.
- Final polish must run `git diff --check`, stale scans for `_reference`, label guessing, DOM/SVG inference, hard-coded `viewBox="0 0 1680 1188"`, center fallback, generic fallback, generated-output residue, `.tools` status, and encoding audit.

### Scope Boundaries

- No graphical entity creation UX completion; Story 4.1 owns customer-facing creation controls.
- No full graphical relationship/reveal workflow completion beyond the cross-reference reveal path required here; Story 4.2 owns relationship and reveal workflow polish.
- No new source syntax, visual syntax, QET runtime dependency, CAD geometry database, renderer repair layer, permanent coordinates, or direct Presentation IR/Projection Occurrence mutation.
- No screenshot-only acceptance and no claims based on UI appearance without structured proof.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 3, Story 3.3, FR-20/23/24/36.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - Feature 4, Core Acceptance Scope, SM-4, FR-20/23/24/36.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md` - Core Product Slice, No Hidden Projection Editing, Two-Sheet Product Proof.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-11, AD-12, AD-13.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Product Proof Contract and document projection identity rules.
- `_bmad-output/implementation-artifacts/m31/3-1-add-the-two-sheet-customer-projection-policy.md` - two-sheet policy and selector preservation.
- `_bmad-output/implementation-artifacts/m31/3-2-re-derive-representation-composition-and-routing.md` - duplicate occurrence removal, content-derived framing, and M31-CL-011.
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md` - retained compatibility items.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created via BMAD create-story workflow after Story 3.2 completion.
- Loaded M31 sprint status, epics, PRD/addendum, architecture spine, authoring contract, Story 3.2 lessons, and CodeGraph context for projection cross references, runtime payload mapping, and Theia reference marker controls.
- Started BMAD dev-story workflow for Story 3.3; loaded sprint status, story context, project config, CodeGraph cross-reference/reveal context, TDD instructions, and verification-before-completion instructions.
- RED compiler/projection: `:kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM30SampleProjectCompilerTest` failed on missing `ProjectionCrossReference.links` and `crossReferenceId`.
- GREEN compiler/projection: same focused compiler test passed after adding renderer-neutral typed cross-reference links.
- RED runtime: `:kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest` failed on missing runtime `links` and `crossReferenceId`.
- RED LSP: `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest.projection*typed*m31*` failed on missing LSP typed link fields.
- GREEN runtime/LSP: focused runtime and LSP tests passed after transport mapping preserved `crossReferenceId` and typed links.
- RED frontend: `yarn test` in `ide/theia-frontend` failed because typed `crossReferences.links` did not produce a navigable workbench reference marker.
- GREEN frontend/GLSP: `yarn test` passed in `integrations/graph-glsp` and `ide/theia-frontend` after typed link transport and navigation support.
- Polish/purge: `git diff --check` passed with line-ending warnings only; stale scans found no hard-coded M27 viewBox, center fallback, generic fallback, DOM/label/SVG inference, or production `_reference` occurrence path in touched production files; `.tools` status clean; encoding audit passed.
- Review RED frontend: `yarn test` in `ide/theia-frontend` failed on malformed typed cross-reference links creating a marker and `switchActiveView` still returning `Promise<void>`.
- Review GREEN frontend: `yarn test` in `ide/theia-frontend` passed after typed link marker creation required known sheet ids, occurrence ids, and one governed `->` notation separator, and reference marker clicks stopped before selection when sheet switching failed.
- Review polish/purge: `git diff --check` passed with line-ending warnings only; `.tools` status clean; stale scan on touched Theia files found only legal `cross_reference` marker/control names plus existing test-only `_reference` fixtures; encoding audit passed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added first-class renderer-neutral `ProjectionCrossReferenceId` and `ProjectionCrossReferenceLink` facts.
- Derived cross-sheet links from governed documentation sheets and sheet subject occurrence facts; malformed references without typed source/target occurrence evidence emit no link.
- Transported typed cross-reference links through runtime, LSP, GLSP, and Theia model boundaries.
- Added Theia reference-marker navigation from typed cross-reference links without presentation marker, DOM, SVG, label, or `_reference` inference.
- Resolved review follow-up: Theia typed cross-reference markers now fail closed unless sheet ids, occurrence ids, and compact notation are proven by current projection facts.
- Resolved review follow-up: Reference marker activation now returns early when governed sheet switching fails, preventing selection/reveal against the wrong active sheet.
- Reopen proof now compares semantic ids, relationship ids, sheet ids, occurrence ids, cross-reference links, and route ids.
- AC evidence: AC1 covered by `AthenaM30SampleProjectCompilerTest.m31 documentation projection emits typed cross sheet reference links`; AC2 covered by `AthenaProjectionRequestTest.projection session payload exposes typed m31 customer sheet policy evidence` and `athena-graph-workbench-model.test.mjs` typed-link navigation test; AC3 covered by `AthenaRuntimeProjectionSessionTest.m31 customer documentation projection publishes two stable policy backed sheets after reopen`; AC4 covered by fail-closed typed-link validation and missing-marker navigation tests; AC5 covered by final verification and purge scans below.
- Final verification passed sequentially: `:kernel:projection-model:test`, `:kernel:compiler:test`, `:kernel:runtime:test`, `:ide:lsp:test`, `yarn test` in `integrations/graph-glsp`, `yarn test` in `ide/theia-frontend`, `git diff --check`, `.tools` status, stale scans, and encoding audit.
- Review follow-up verification passed: `yarn test` in `ide/theia-frontend`; `git diff --check`; `.tools` status check; stale scan for hard-coded viewBox, fallback, `_reference`, DOM/SVG/label inference, querySelector, and canvas coordinate authority; `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### File List

- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReferences.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM30SampleProjectCompilerTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.d.ts.map`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-graph-workbench-preservation.test.mjs`
- `_bmad-output/implementation-artifacts/m31/3-3-add-cross-sheet-reference-and-reopen-stability.md`

### Change Log

- 2026-07-22: Created Story 3.3 context for BMAD dev-story execution.
- 2026-07-22: Implemented typed cross-sheet reference links, runtime/LSP/GLSP/Theia transport, reopen stability proof, and mandatory polish/purge evidence.
- 2026-07-22: Addressed code review follow-ups for typed cross-reference fail-closed validation and sheet-switch failure gating.
