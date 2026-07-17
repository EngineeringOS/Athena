---
baseline_commit: 0b43cbe
---

# Story 5.1: Preserve Active-Source Projection in Graphical View

Status: done

## Story

As an engineer,
I want Graphical View to project the active `.athena` file,
so that opening an optimized or round-trip scenario does not show a stale seed file.

## Acceptance Criteria

1. Given the M22 sample project contains multiple `.athena` files, when I open a non-baseline M22 source file and launch Graphical View, then the projection corresponds to the active source file.
2. Given Graphical View becomes the focused widget, when projection is requested, then no stale baseline or seed projection is shown.
3. Given active-source projection is supported, when regression coverage runs, then it proves the active-source selection path.

## Tasks / Subtasks

- [x] Add active-source regression coverage (AC: 1, 2, 3)
  - [x] Add failing frontend/static test for retaining the last active Athena editor when Graphical View takes focus.
  - [x] Confirm the backend active-source projection test remains the semantic authority proof.
- [x] Preserve active Athena editor identity across Graphical View focus (AC: 1, 2)
  - [x] Track the most recent Athena editor in the LSP editor bridge.
  - [x] Use that editor as the projection model fallback when the current widget is not an Athena editor.
- [x] Document the active-source boundary (AC: 1, 3)
  - [x] Update M22 usage docs with the active-source projection expectation.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run affected frontend test.
  - [x] Run affected LSP projection test.
  - [x] Run frontend build.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Backend LSP tests already prove `projectionSession` follows the latest opened source file in a governed repository.
- The Theia frontend can lose `editorManager.currentEditor` when the Graphical View itself becomes active.
- M22 must preserve the user-visible active source, not rely on seed-file fallback.

### Guardrails

- Do not move projection authority into the renderer or graph workbench.
- Do not infer semantic meaning in the frontend.
- Keep the fix in the editor bridge/session request path so all downstream projection consumers benefit.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-active-source-projection.test.mjs`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest.projection*active*source*`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 5, Story 5.1]
- [Source: `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`]
- [Source: `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-active-source-projection.test.mjs`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest.projection session request follows latest opened source file in governed repository"`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Root cause fixed: Graphical View focus could leave `editorManager.currentEditor` without an Athena editor, so projection requests could fall back to the latest backend-opened or seed source.
- The LSP editor bridge now remembers the most recent Athena editor and uses it as the projection model fallback when the graph widget has focus.
- Added a frontend regression guard and reused the existing backend active-source projection test as the semantic authority proof.

### File List

- `_bmad-output/implementation-artifacts/m22/5-1-preserve-active-source-projection-in-graphical-view.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-active-source-projection.test.mjs`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`

## Change Log

- 2026-07-18: Created M22 Story 5.1 with active-source projection requirements.
- 2026-07-18: Fixed Graphical View active-source fallback and verified frontend plus LSP regression coverage.
