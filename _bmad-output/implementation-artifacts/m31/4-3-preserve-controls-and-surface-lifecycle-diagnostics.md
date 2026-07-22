---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 4.3
epic: 4
title: Preserve Controls And Surface Lifecycle Diagnostics
---

# Story 4.3: Preserve Controls And Surface Lifecycle Diagnostics

## Status

Done

## Story

As a controls engineer,
I want authoring controls and failures to remain understandable through view changes,
so that I never lose the document or mistake failure for success.

## Acceptance Criteria

1. With the M31 project open, switching Cabinet, Wire, or other available projection modes and switching sheets preserves the visible sheet selector, exposes exactly two policy-owned sheets, preserves focus/reveal state where meaningful, and never infers a third sheet from `.athena` source files.
2. Blocked, stale, cancelled, compile-stopped, committed, reprojected, and projection-failed transaction outcomes render as distinct lifecycle states with structured recovery actions. The UI must not collapse them into generic `Projection unavailable`, and controls must remain usable after recovery.
3. After the accepted create/connect workflow completes and the IDE is reopened, the same semantic and projection proof is available, transient transaction state is not treated as canonical truth, and Outline shows nested ports for the created entity.
4. M27-M31 visual and authority invariants remain intact: Cabinet default, exactly two governed sheets, transparent normal chrome, content-derived viewBox, no duplicate off-sheet occurrences, no center fallback routes, no generic fallback boxes, no frontend source serializer, no legacy `ConnectPortsIntent`, and no downstream projection artifact mutation.
5. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete, including controls/state races, lifecycle UI, smoke hooks, Outline nested-port paths, generated artifacts, `.tools`, encoding, and cleanup-ledger review.

## Tasks/Subtasks

- [x] Add failing frontend tests for projection mode and sheet control preservation. (AC: 1,4)
  - [x] Assert Graphical View keeps the sheet selector visible while switching Cabinet, Wire, and any additional available projection modes.
  - [x] Assert exactly two governed M31 policy sheets remain selectable after projection mode switches, sheet switches, create preview changes, relationship preview changes, and recompile/reopen payload refreshes.
  - [x] Assert sheet count comes from document projection policy payloads, not `.athena` source file count, DOM tabs, widget ids, route labels, or fallback source-file inference.
  - [x] Assert focus/reveal state is preserved where the canonical subject remains present and fails closed with structured diagnostics where it is absent.
- [x] Add failing lifecycle diagnostic and recovery tests. (AC: 2,4)
  - [x] Cover blocked, stale, cancelled, compile-stopped, committed, reprojected, and projection-failed payloads with distinct labels, diagnostic authority, lifecycle stage, and recovery action.
  - [x] Assert `STOP_DOWNSTREAM` remains a named compile-stop lifecycle/diagnostic and is not rendered as generic projection unavailability.
  - [x] Assert committed-plus-projection-failed displays both committed mutation evidence and projection failure recovery, without rolling back source or inventing visual success.
  - [x] Assert controls remain enabled or recoverable after reject/cancel/stale/block/projection-failed flows according to returned lifecycle eligibility.
- [x] Add failing reopen and Outline nested-port proof tests. (AC: 3,4)
  - [x] Assert persisted reopen proof uses compiled semantic, sheet, occurrence, route, and relationship identities rather than transient transaction ids.
  - [x] Assert transient preview/decision state is cleared across reload and cannot become canonical authoring truth.
  - [x] Assert Outline exposes nested ports for created entities such as `OperatorHMI1.status` under the device tree after compile/reopen.
  - [x] Assert Outline nested-port ranges come from parser/LSP semantic facts and not from visible graph labels or component-panel catalog guesses.
- [x] Implement minimal control-state and lifecycle UI fixes in existing adapters. (AC: 1,2,3,4)
  - [x] Reuse `AthenaGraphWorkbenchWidget` sheet/mode state, `AthenaGraphWorkbenchModel` projection facts, semantic selection service, and authoring preview/decision payloads; do not create a second controls registry or projection mode model.
  - [x] Preserve the Cabinet default and current governed sheet selection while switching modes; reset only when the selected sheet id is absent from the new governed policy payload.
  - [x] Render lifecycle diagnostics from typed authoring payloads with authority, lifecycle stage, stable diagnostic code, subject ids, and recovery action.
  - [x] Keep sheet controls independent from preview panels so creation/relationship lifecycle recovery cannot hide or disable the document selector.
  - [x] Ensure reopen initialization clears transient preview state and derives graph/Outline proof from compiled semantic and projection payloads.
- [x] Implement or repair Outline nested-port exposure if current LSP/tree-sitter/outline path omits nested ports. (AC: 3,4)
  - [x] Inspect the existing Outline provider/parser model before editing; determine whether the gap is parser, semantic model, LSP document-symbol mapping, or Theia Outline consumption.
  - [x] Add nested `port` children under their containing `device` document symbol for nested-port syntax.
  - [x] Preserve existing top-level document symbols and grouped `connect` handling.
  - [x] Do not restore legacy top-level `port Device.name` source generation or treat legacy ports as the preferred syntax.
- [x] Preserve M27-M31 projection, representation, interaction, and mutation invariants. (AC: 1,2,3,4)
  - [x] Do not mutate Presentation IR, Representation Occurrence, Projection Occurrence, document sheet output, layout facts, route facts, or rendered geometry.
  - [x] Do not reintroduce generic fallback boxes, center fallback routes, fixed `viewBox="0 0 1680 1188"`, duplicate `_reference` normal occurrences, repeated labels, or visible normal wrappers/hitboxes.
  - [x] Do not reintroduce `ConnectPortsIntent`, connect-ports transport names, frontend `.athena` serializers, DOM/SVG/label identity inference, or component-specific authoring authority.
- [x] Run focused and regression verification sequentially on Windows. (AC: 1,2,3,4)
  - [x] Run RED/GREEN focused frontend tests for sheet/mode preservation, lifecycle diagnostics, recovery, reveal/focus, reopen state, and no hidden source-file sheet inference.
  - [x] Run LSP/parser/document-symbol tests if Outline nested-port code changes; run Gradle commands strictly sequentially.
  - [x] Run `yarn test` in `ide/theia-frontend`.
  - [x] Run `git diff --check`, stale/fallback/source-authority scans, `.tools` status check, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- [x] Complete mandatory polish/purge review and update story evidence. (AC: 5)
  - [x] Review touched and adjacent Graphical View controls, lifecycle rendering, semantic selection, LSP Outline/parser, tests, sample hooks, docs, generated files, and cleanup ledger.
  - [x] Remove stale control assumptions, hidden fallback code, obsolete tests/docs, generated residue, or ledger each retained item with owner, reason, target milestone, and verification.
  - [x] Record AC-to-evidence mapping, final verification commands, File List, Completion Notes, and Change Log.

### Review Findings

- [x] [Review][Patch] Validate current sheet selector before displaying or caching it. [`ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`]
- [x] [Review][Patch] Treat `reprojected` as the normal successful accept lifecycle and `projection-failed` as committed-with-recovery. [`ide/theia-frontend/src/browser/athena-authoring-protocol.ts`]
- [x] [Review][Patch] Preserve projection-failed-after-commit diagnostics after source edit application. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [x] [Review][Patch] Mark malformed authoring preview payloads as blocked instead of pending-review. [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`]
- [x] [Review][Patch] Allow invalid relationship previews without source edits to reject/cancel by preview session identity. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [x] [Review][Patch] Prevent diagram refresh from stale-cancelling previews while authoring decisions are in flight. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [x] [Review][Patch] Add create-preview request-token invalidation and pre-commit revision guard check. [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [x] [Review][Patch] Update focused and regression tests for lifecycle, selector, reject/cancel, and revision-guard behavior. [`ide/theia-frontend/scripts`]
- [x] [Review][Defer] Model field mapping is a broader create-entity template/property propagation concern; not introduced by Story 4.3 and should be handled by a future semantic template/property story. [`ide/theia-frontend/src/browser/athena-authoring-protocol.ts`]
- [x] [Review][Defer] Relationship preview request view/source context enrichment is a broader relationship planning enhancement; current source-edit revision evidence remains the acceptance authority. [`ide/theia-frontend/src/browser/athena-authoring-protocol.ts`]
- [x] [Review][Defer] Rendering stage chrome during generic empty projection remains existing Graphical View behavior; Story 4.3 prevents authoring lifecycle diagnostics from collapsing into that path, but a future product-state UX story should separate document controls from empty projection rendering.

## Dev Notes

### Architecture Guardrails

- This story implements M31 Epic 4 Story 4.3 and contributes to FR-24, FR-31, FR-32, FR-33, FR-34, FR-35, FR-36, FR-38, FR-41, FR-42, FR-51, NFR-6, NFR-7, NFR-8, NFR-9, NFR-10, and NFR-12.
- Graphical View remains an adapter. It may preserve visible controls, request refresh/reveal, render lifecycle diagnostics, and display typed proof. It may not decide sheet count, infer projection mode authority from DOM/widget state, serialize source, or repair semantic/projection payloads.
- Sheet count, role, order, and identity derive from Document Projection Policy only. M31 exposes exactly control and field/device sheets; source file count must never create a third sheet.
- Lifecycle outcomes are platform facts. Do not collapse blocked, stale, cancelled, compile-stopped/`STOP_DOWNSTREAM`, committed, reprojected, or projection-failed into one generic renderer error.
- A committed mutation followed by projection failure must remain honest: source mutation succeeded, downstream projection failed, diagnostics identify authority/stage, and recovery is explicit.
- Transient preview/decision/transaction state is runtime evidence only. It must be cleared on reopen and must not become canonical engineering truth.
- Outline nested ports are part of the semantic authoring proof. For nested source like `device OperatorHMI1 { port status { ... } }`, Outline must expose the nested `status` port under `OperatorHMI1` after compile/reopen.
- Reveal uses canonical subject identity and governed source/projection facts. It must not parse visible graph text, route labels, DOM ids, SVG ids, CSS classes, `_reference` suffixes, or coordinates.
- Normal industrial drawing density remains quiet and transparent. Do not fix control visibility by adding persistent cards on the engineering sheet or visible normal hitbox/background chrome.
- Every story ends with mandatory polish/purge. Do not mark done without fresh AC evidence and cleanup review.

### Existing Code To Inspect And Extend

- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`: sheet/mode controls, create/relationship preview lifecycle, cross-reference navigation, semantic reveal, source/editor context change handling, and lifecycle rendering. Extend as adapter only.
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`: typed projection, sheet, reference, occurrence, route, port, and proof payloads. Use this for governed sheet and occurrence identity.
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`: lifecycle, preview, decision, source-edit, and diagnostic payload helpers from Stories 4.1/4.2. Reuse existing matching and guard helpers.
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts` and `athena-semantic-selection-service.ts`: canonical selection/reveal aliases and cross-surface reveal. Use for focus/reveal preservation.
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`: editor revision/source edit boundary and reload interaction.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/*`: Outline/document symbol and authoring transport if nested ports or lifecycle payload fields are missing.
- `grammar/antlr` and `grammar/tree-sitter` paths only if the Outline issue proves parser-based; do not touch grammar speculatively.
- `examples/m29` and `examples/m31` sample projects only if product smoke fixtures need stable nested-port proof. Do not add QET runtime references.

### Previous Story Intelligence

- Story 3.1 established exactly two M31 policy-owned sheets and preserved Cabinet as the default projection. 4.3 must prevent regressions when mode/sheet switches happen after authoring controls are added.
- Story 3.3 established typed cross-sheet reference navigation and required sheet switching to fail closed when the target cannot be selected.
- Story 4.1 added create-entity lifecycle/source guards and explicit backend cancel for hidden stale previews; those patterns must remain intact when controls are preserved through mode switches.
- Story 4.2 added relationship preview race guards, strict source-edit evidence equality including offsets, backend cancellation for stale relationship previews, and relationship-only reveal after accept. 4.3 must not weaken those gates.
- User-reported product issue before this story: Theia/Electron Outline did not show nested `port` nodes under `device OperatorHMI1` even though the `.athena` source contained nested port syntax. Treat this as an acceptance-critical proof gap, not a cosmetic tree issue.
- Sprint action items still open: add no-side-effect assertions to rejected authoring/projection stories and purge stale generated artifacts after deleting frontend source files. This story should close or materially advance them if touched.

### Testing Requirements

- Follow RED-GREEN-REFACTOR. Record observed RED failures before production edits.
- Use structured tests as acceptance authority; screenshots are secondary only.
- Frontend focused tests must cover sheet selector persistence, exactly two sheet options, mode switching, lifecycle diagnostic rendering, recovery actions, reveal/focus preservation, transient preview clearing, and no source-file sheet inference.
- If Outline is repaired in LSP/parser code, add focused Gradle tests for document symbols/nested ports and run the relevant Gradle suite sequentially before frontend regression.
- If only Theia files change, run `yarn test` in `ide/theia-frontend`. If LSP/runtime/compiler/parser code changes, run relevant Gradle suites sequentially; never run Gradle commands concurrently.
- Final polish must run `git diff --check`, scans for `ConnectPortsIntent`, connect-ports transport names, frontend `.athena` serializers, DOM/SVG/label inference, `_reference`, generic fallback, center fallback, hard-coded `viewBox="0 0 1680 1188"`, visible normal wrapper regressions, `.tools` status, and encoding audit.

### Scope Boundaries

- This story is control preservation, lifecycle diagnostic surfacing, reopen coherence, and Outline nested-port proof for the already delivered create/connect workflow.
- No new authoring intent family, no update/remove accepted UX, no full undo/redo, no AI agent workflow, no symbol/library expansion, no print/PDF, no new `.athena` syntax, and no QET importer.
- Do not create a second capability registry, lifecycle model, source planner, document projection policy, Outline authority, reveal registry, or frontend projection state authority.
- Do not directly mutate document sheet membership, layout facts, route facts, Presentation IR, Representation Occurrence, Projection Occurrence, or rendered geometry.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 4, Story 4.3.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md` - FR-24, FR-31..FR-36, FR-38, FR-41, FR-42, FR-51; NFR-6..NFR-12.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md` - Failure Taxonomy, Two-Sheet Product Proof, No Hidden Projection Editing, Revision-Bound Preview.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-3, AD-5, AD-10, AD-11, AD-12, AD-13, AD-15, AD-18.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Lifecycle, Diagnostic Envelope, Frontend Adapter Contract, Product Proof Contract.
- `_bmad-output/implementation-artifacts/m31/3-1-add-the-two-sheet-customer-projection-policy.md` - two-sheet policy and Cabinet default evidence.
- `_bmad-output/implementation-artifacts/m31/3-3-add-cross-sheet-reference-and-reopen-stability.md` - cross-sheet reveal and reopen identity lessons.
- `_bmad-output/implementation-artifacts/m31/4-1-add-graphical-entity-creation-transaction-ux.md` - create preview lifecycle and stale cancellation patterns.
- `_bmad-output/implementation-artifacts/m31/4-2-add-graphical-relationship-and-reveal-workflow.md` - relationship preview/reveal lifecycle and race-guard patterns.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created via BMAD create-story workflow after Story 4.2 reached done.
- Loaded M31 sprint status, epics, PRD/addendum, architecture spine, authoring contract, Stories 4.1/4.2, and recent git history.
- RED: `node scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs` failed because `resolveVisibleAthenaGraphSheetViewSelector` preserved a stale three-source-file selector, `AthenaAuthoringDiagnosticPayload` lacked `recoveryAction`, and create/relationship preview diagnostics did not render recovery actions.
- GREEN: `yarn build` then `node scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs` passed after selector filtering, diagnostic transport, and UI rendering fixes.
- RED: code review follow-up `node scripts/athena-authoring-protocol.test.mjs` failed when `projection-failed` committed-with-recovery was expected to pass the committed decision guard, proving the previous helper blocked committed projection-failure recovery.
- GREEN: `yarn build`, `node scripts/athena-authoring-protocol.test.mjs`, `node scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs`, and `node scripts/athena-graph-workbench-model.test.mjs` passed after lifecycle helper, selector, and recovery guard fixes.
- Review fix: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest` passed after malformed preview payloads were corrected to `blocked`.
- Outline proof: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringSupportTest` passed. Existing LSP document-symbol coverage nests compact authored ports under owning devices and Theia's bridge recursively maps `DocumentSymbol.children`; no ANTLR4 or tree-sitter repair was indicated by the evidence.
- Frontend regression: `yarn test` in `ide/theia-frontend` passed: 181/181.
- LSP regression: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- Polish/Purge: `git diff --check` passed with line-ending warnings only; `.tools` status was clean; encoding audit passed; stale/source-authority/fallback scans found only negative-test assertions, historical fixture names, or legitimate reference-marker terms; fixed oversized `viewBox="0 0 1680 1188"` was absent.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added focused M31 controls/lifecycle diagnostics tests covering stale selector rejection, exactly two governed M31 sheet roles, recovery-action transport, recovery-action UI rendering, control separation, and Outline child preservation in the Theia document-symbol bridge.
- `resolveVisibleAthenaGraphSheetViewSelector` now preserves fallback sheet controls only when the cached selector is the governed M31 two-sheet policy, preventing source-file-derived three-sheet selectors from surviving projection mode switches.
- Frontend authoring diagnostics now carry optional `recoveryAction`, expose stable lifecycle diagnostic codes for stop-downstream and projection-failed-after-commit, and include recovery actions in decision diagnostic summaries.
- Graphical View create-entity and semantic-relationship preview diagnostics now render `Recovery: ...` from typed payloads without collapsing lifecycle failures into `Projection unavailable`.
- LSP authoring protocol now transports `AuthoringDiagnostic.recoveryAction` from backend authoring/runtime diagnostics into Theia payloads.
- Code review fixes hardened accepted lifecycle handling (`reprojected`, committed `projection-failed`), selector fail-closed behavior, create-preview async races, pre-commit revision guard checks, invalid relationship reject/cancel recovery, and malformed preview lifecycle labels.
- AC evidence: AC1 covered by `athena-m31-controls-lifecycle-diagnostics.test.mjs` selector test and existing sheet selector regression in `athena-graph-workbench-model.test.mjs`; AC2 covered by recovery-action protocol/UI tests, `reprojected`/`projection-failed` helper tests, invalid preview `blocked` LSP test, and `:ide:lsp:test`; AC3 covered by LSP nested-port document-symbol test and Theia bridge recursive child test; AC4 covered by frontend/LSP regressions and stale authority scans; AC5 covered by final polish/purge commands, review finding closure, and this evidence record.

### File List

- `_bmad-output/implementation-artifacts/m31/4-3-preserve-controls-and-surface-lifecycle-diagnostics.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/theia-frontend/scripts/athena-authoring-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-governed-relationship-preview.test.mjs`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`

### Change Log

- 2026-07-22: Created Story 4.3 context for BMAD dev-story execution.
- 2026-07-22: Implemented M31 sheet-control preservation guard, lifecycle recovery-action transport/UI, Outline nested-port bridge proof, and polish/purge evidence; marked Story 4.3 ready for review.
- 2026-07-22: Resolved BMAD code-review findings for reprojected/projection-failed lifecycle handling, selector fail-closed behavior, async preview races, invalid preview recovery, and pre-commit revision guard checks; marked Story 4.3 done.
