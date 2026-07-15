---
baseline_commit: d3b56c91fcd03e79692bfe90a1ae17e1583198ac
---

# Story 4.4: Use Existing Canvas Reveal From Package-Aware Navigation

Status: done

## Story

As an engineering workbench user,
I want package-aware navigation to use existing reveal-capable canvas surfaces when applicable,
so that source navigation and graphical inspection stay aligned.

## Acceptance Criteria

1. Given package-aware semantic provenance for an engineering subject, when IDE navigation lands on the subject's authored source range, the existing semantic-selection flow resolves the canonical subject identity from compiler/LSP inspection state.
2. Given that canonical subject identity and an existing Theia graph workbench surface, the existing graph reveal path can determine whether the subject is present or must be revealed through a supported workbench view.
3. Canvas reveal behavior remains downstream of compiler/LSP authority and uses canonical engineering subject ids such as `component:*`, `port:*`, and `connection:*`.
4. The implementation does not add a new canvas system, renderer path, graphical projection authority, frontend-local package resolver, desktop-viewer behavior, or Kotlin Compose UI work.
5. Verification runs sequentially through focused frontend reveal/selection tests, relevant LSP tests if touched, and encoding audit.

## Tasks / Subtasks

- [x] Add failing frontend coverage for package-aware navigation reveal handoff (AC: 1-3)
  - [x] Cover an editor navigation landing range inside a compiler/LSP semantic inspection entry.
  - [x] Assert the resolved selection preserves the canonical semantic id used by graph reveal.
  - [x] Assert an existing graph workbench diagram recognizes that canonical id without aliasing or frontend-local resolution.
- [x] Reuse existing Theia semantic-selection and graph reveal behavior (AC: 1-4)
  - [x] Keep navigation-to-source as an LSP/Monaco operation; do not add a custom navigation command.
  - [x] Keep source-to-selection resolution in `athena-semantic-selection-service` / `athena-semantic-selection-model`.
  - [x] Keep canvas reveal in `athena-graph-workbench-widget` / `AthenaGraphAdapterService.revealSemanticId`.
  - [x] Add only the smallest model/projection adjustment needed for deterministic canonical-id reveal, if the failing test exposes a gap.
- [x] Preserve scope boundaries (AC: 3-4)
  - [x] Do not touch `apps/desktop-viewer`.
  - [x] Do not touch Kotlin Compose UI.
  - [x] Do not create renderer, GLSP, or canvas projection authority.
  - [x] Do not implement frontend package/import/symbol resolution.
- [x] Run scoped verification sequentially (AC: 5)
  - [x] Run focused Theia frontend test for semantic selection / reveal handoff.
  - [x] Run `yarn workspace @engineeringood/athena-theia-frontend test` if frontend code or tests changed.
  - [x] Run LSP tests only if LSP code changed.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Notes

- User correction for this story is authoritative: focus on IDE/Theia frontend logic only. Ignore `apps/desktop-viewer`, Kotlin Compose, and any desktop viewer canvas.
- Existing reveal chain:
  - Theia Monaco definition/references providers live in `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`.
  - Source selection synchronization lives in `ide/theia-frontend/src/browser/athena-semantic-selection-service.ts`.
  - Canonical source-range-to-semantic-id helpers live in `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`.
  - Existing canvas/workbench reveal lives in `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx` and `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`.
- `AthenaSemanticSelectionService` already refreshes selection from the current `.athena` editor by requesting `athena/semanticInspection` through the LSP bridge and resolving the most specific semantic subject containing the editor selection.
- `AthenaGraphWorkbenchWidget.handleSemanticSelectionChanged(...)` already calls `graphAdapterService.revealSemanticId(selection.semanticId, diagram)` when the current diagram does not contain the selected canonical id.
- `AthenaGraphAdapterService.revealSemanticId(...)` already reuses existing supported views and does not create a new projection or renderer.
- If implementation work is required, it should be a narrow deterministic projection/model fix around canonical ids. Do not invent a new package-aware canvas API.

### Previous Story Intelligence

- Story 4.1 added project semantic snapshot tracking and package-aware diagnostics through LSP.
- Story 4.2 added package-aware definition/references from the compiler-owned navigation snapshot.
- Story 4.3 added package-aware document symbol behavior while explicitly keeping workspace symbols and canvas logic out of scope.
- Recent verification pattern: focused test first, then broader module tests, then encoding audit. Gradle commands must run sequentially and only if relevant.

### Project Structure Notes

- Expected implementation/test modules:
  - `ide/theia-frontend/src/browser/`
  - `ide/theia-frontend/scripts/`
- Keep TypeScript model helpers small and pure where possible so `node --test` can cover behavior without booting Theia.

### References

- [Source: `epics.md` - Epic 4, Story 4.4]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-8, SM-4, UX-DR1/UX-DR2]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 8-12]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-6, AD-8, AD-11, AD-14]
- [Source: `4-3-add-snapshot-derived-symbol-behavior.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test scripts/athena-semantic-selection-model.test.mjs` failed before sheet-published canonical subject ids were recognized by the existing graph reveal presence check.
- `node --test scripts/athena-semantic-selection-model.test.mjs` failed during review when inactive sheet metadata was incorrectly treated as active reveal presence.
- `yarn build` passed after adding active-sheet-aware subject metadata handling.
- `node --test scripts/athena-semantic-selection-model.test.mjs` passed with package-aware navigation reveal handoff coverage.
- `yarn test` passed for the Theia frontend package.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - Story 4.4 prepared for existing Theia graph reveal handoff from package-aware navigation.
- Added frontend coverage proving package-aware navigation selections keep canonical engineering subject ids when an existing workbench sheet publishes the subject.
- Updated the existing semantic-selection model so `graphContainsSemanticId` recognizes active-sheet `subjectSemanticIds` as reveal-capable workbench presence.
- Fixed review-discovered inactive-sheet false positives by honoring `activeSheetId` when present.
- Kept the change inside Theia frontend model/test logic; no desktop-viewer, Kotlin Compose, renderer, GLSP, LSP, or frontend semantic-resolution work was added.

### File List

- `_bmad-output/implementation-artifacts/m18/4-4-use-existing-canvas-reveal-from-package-aware-navigation.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`

## Change Log

- 2026-07-15: Created Story 4.4 from Epic 4, PRD FR-8/SM-4, architecture AD-6/AD-8/AD-14, and Story 4.3 implementation intelligence.
- 2026-07-15: Implemented active-sheet canonical subject reveal presence for existing Theia graph workbench surfaces.
