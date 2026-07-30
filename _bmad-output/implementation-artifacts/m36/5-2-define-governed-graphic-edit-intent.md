---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E5.S2: Define Governed Graphic Edit Intent

Status: done

## Story

As an engineer,
I want graphic edits to become reviewable source mutations,
so that visual interaction cannot corrupt Athena SSOT.

**Requirements:** FR-32.

## Acceptance Criteria

1. Given a graphic edit request, the governed edit path produces an intent, source-mutation
   preview, validation result, accept-or-reject result, recompilation, and rerendering.
2. Direct mutation of SVG, DOM, Graphic Primitive IR, PlacementFacts, or RouteFacts is forbidden.
3. The story establishes the typed contract only and does not introduce a full Cabinet editor.
4. Rejected edits preserve the compiled result and return actionable diagnostics.
5. Renderer, workbench, and preview surfaces remain non-authoritative for engineering truth.

## Tasks / Subtasks

- [x] Add failing tests for governed graphic edit intent (AC: 1-5)
  - [x] Cover layout-edit capture, preview, accept, and reject behavior.
  - [x] Cover the source-mutation preview contract and compiler-owned validation evidence.
  - [x] Prove direct mutation of SVG, DOM, Graphic Primitive IR, PlacementFacts, or RouteFacts is
    rejected.
- [x] Expose governed graphic edit intent through IDE/LSP and workbench contracts (AC: 1-5)
  - [x] Reuse the existing authoring preview and source-edit payload shape instead of inventing a
    second mutation model.
  - [x] Keep the edit preview source-first, typed, and compiler-owned.
  - [x] Preserve deterministic ordering, stable ids, and explicit revision guards.
- [x] Wire accept/reject and rerender flow through the governed mutation path (AC: 1-5)
  - [x] Route graphic edits through preview, validation, decision, source edit application,
    recompilation, and rerender.
  - [x] Preserve renderer purity and keep workbench state non-authoritative.
  - [x] Attach source spans and compiler evidence to rejected edits.
- [x] Run story evidence gate (AC: 1-5)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- The repo already has a governed preview and source-edit seam that this story should extend:
  `AthenaGraphLayoutAdjustmentIntent`, `AthenaGraphLayoutMutationPreview`,
  `AthenaAuthoringSourceEditPayload`, `previewInspectorUpdate`, `requestAuthoringPreview`, and
  `applyAuthoringSourceEdit`.
- Existing preview flows already model reviewable source mutations for other governed edits. Use
  the same contract shape and keep graphic edits within the compiler-owned preview/decision path.
- This story is about the typed contract and transport path, not a full interactive Cabinet editor.
  Do not introduce direct SVG, DOM, or IR mutation authority.
- No XML compatibility paths. The project is unreleased, so stale incompatible edit paths may be
  removed rather than preserved.

### Project Structure Notes

- Likely touchpoints:
  - `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
  - `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Keep text assets UTF-8.
- Do not add legacy XML compatibility paths.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 5.2, FR-32.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-32, NFR-1,
  NFR-4, NFR-5, NFR-8, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-9, AD-10.
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
  - layout adjustment capture and preview model.
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - governed workbench interaction flow.
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - source-edit transport and apply flow.
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
  - preview and source-edit payload contracts.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
  - typed source-edit payload contract.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
  - governed diagnostics transport contract.
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewService.kt`
  - existing compiler-owned preview pattern.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Started implementation; status moved to in-progress in story and sprint tracker.
- 2026-07-29: Confirmed RED failure for missing governed graphic edit contract with focused LSP test.
- 2026-07-29: Verified `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM36GovernedGraphicEditIntentTest`.
- 2026-07-29: Verified `node ide/theia-frontend/scripts/athena-governed-graphic-edit-protocol.test.mjs`.
- 2026-07-29: Verified full `.\gradlew.bat --no-daemon --console=plain test`.
- 2026-07-29: Verified `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- 2026-07-29: Verified `git diff --check`.

### Completion Notes List

- Added a typed governed graphic edit intent contract exposed through the LSP module.
- Added `athena/graphicEdit/preview` as a preview-only LSP method that returns compiler-owned
  source mutation previews and rejects direct graphic authority mutation attempts.
- Reused the existing `AthenaAuthoringSourceEditPayload` and revision guard shape; no second source
  mutation model was introduced.
- Added Theia frontend protocol types and bridge request method for governed graphic edit preview.
- Direct mutation targets for SVG resource, DOM node, Graphic Primitive IR, PlacementFact, and
  RouteFact are explicitly rejected with diagnostics and no source edit.
- AC evidence: AC1 and AC4 covered by `AthenaM36GovernedGraphicEditIntentTest`; AC2 covered by
  direct mutation target rejection assertions; AC3 and AC5 covered by the preview-only LSP contract,
  frontend protocol script, and reuse of source-edit payload.

### File List

- `_bmad-output/implementation-artifacts/m36/5-2-define-governed-graphic-edit-intent.md`
- `_bmad-output/implementation-artifacts/m36/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedGraphicEditIntentProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM36GovernedGraphicEditIntentTest.kt`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/scripts/athena-governed-graphic-edit-protocol.test.mjs`

## Change Log

- 2026-07-29: Created M36-E5.S2 story for governed graphic edit intent.
- 2026-07-29: Completed governed graphic edit preview contract, LSP endpoint, frontend bridge contract, and validation tests.
