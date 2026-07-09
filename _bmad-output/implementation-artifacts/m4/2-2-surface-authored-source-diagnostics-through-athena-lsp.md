---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.2: Surface Authored-Source Diagnostics Through Athena LSP

Status: done

## Story

As an engineer,
I want authored-source diagnostics to appear in the editor and workbench problem surfaces,
so that I can immediately see syntax and semantic issues while editing.

## Acceptance Criteria

1. Given an active Repository Session contains Athena-authored source, when I open or edit a source file in the workbench, then Athena LSP publishes diagnostics derived from Athena-owned parsing, semantic analysis, and validation, and the diagnostics are visible in both the editor and Problems-style workbench surfaces.
2. Given diagnostics are produced through Athena LSP, when I inspect their origin, then they remain traceable to the existing JVM semantic stack, and the Theia client does not invent an independent diagnostic engine.

## Tasks / Subtasks

- [x] Add in-memory diagnostics compilation for Athena-authored editor buffers. (AC: 1, 2)
  - [x] Extend the compiler with an overlay text path that preserves the same JVM parsing, lowering, and validation stack.
  - [x] Preserve syntax-span and semantic provenance so LSP diagnostics can point at authored source ranges.
- [x] Publish diagnostics from the Athena LSP server. (AC: 1, 2)
  - [x] Handle `textDocument/didOpen` and `textDocument/didChange` with full-text content.
  - [x] Publish `textDocument/publishDiagnostics` from JVM-owned parsing and semantic validation.
  - [x] Clear diagnostics on `textDocument/didClose`.
- [x] Relay published diagnostics through the Theia backend without adding semantic logic there. (AC: 1, 2)
  - [x] Capture `publishDiagnostics` notifications in the backend session manager.
  - [x] Expose a frontend-consumable diagnostics endpoint backed only by LSP-published state.
  - [x] Wait for the corresponding diagnostics publication before returning open/change relay responses.
- [x] Project Athena LSP diagnostics into Theia editor and Problems surfaces. (AC: 1)
  - [x] Send `textDocument/didChange` from the active Athena editor.
  - [x] Update Theia `ProblemManager` markers from the published diagnostics payload.
  - [x] Keep the workbench output channel explicit about the `frontend -> LSP -> runtime/compiler` diagnostics path.
- [x] Update M4 docs and artifacts. (AC: 1, 2)
  - [x] Update `ide/README*`, `ide/lsp/README*`, and frontend/backend package READMEs.
  - [x] Update `docs/usages/athena-workspace-summary.md`.
  - [x] Record the story outcome in `_bmad-output/implementation-artifacts/m4/`.
- [x] Verify the implementation sequentially on Windows. (AC: 1, 2)
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`.
  - [x] Run `Set-Location ide; yarn install`.
  - [x] Run `Set-Location ide; yarn build`.
  - [x] Run a direct stdio LSP diagnostics proof showing invalid-open diagnostics and valid-change clearing.
  - [x] Run `Set-Location ide; yarn verify:desktop`.
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

## Dev Notes

### Implementation Notes

- This story keeps diagnostics ownership entirely inside the JVM stack:
  - the compiler now supports overlay text for unsaved editor buffers
  - Athena LSP publishes diagnostics from the same parsing and semantic validation path used elsewhere
  - the backend stores only published LSP diagnostics and relays them
  - the frontend only maps those published diagnostics into Theia markers and Problems
- No independent Theia diagnostic engine was introduced.
- The current implementation is intentionally scoped to open/change diagnostics. Richer navigation, completion, and repeated-edit stability remain later stories in Epic 2.

### Files Changed

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/theia-backend/package.json`
- `ide/theia-backend/src/node/athena-backend-contribution.ts`
- `ide/theia-backend/src/node/athena-repository-session-manager.ts`
- `ide/theia-frontend/package.json`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`
- `ide/README.md`
- `ide/lsp/README.md`
- `ide/theia-backend/README.md`
- `ide/theia-backend/README.zh-CN.md`
- `ide/theia-frontend/README.md`
- `ide/theia-frontend/README.zh-CN.md`
- `docs/usages/athena-workspace-summary.md`

### Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`
- `Set-Location ide; yarn install`
- `Set-Location ide; yarn build`
- direct stdio LSP diagnostics proof result:
  - `[{"version":1,"count":2},{"version":2,"count":0}]`
- `Set-Location ide; yarn verify:desktop`
  - result: `Athena desktop smoke start passed. ready=true javaHome=D:\Program Files\Java\openjdk-25.0.2`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`

### Remaining Review Note

- Diagnostics now follow the right boundary, but repeated-edit behavior is still basic. Story `2.4` should harden repeated editing and diagnostic stability on top of this path instead of replacing it.
