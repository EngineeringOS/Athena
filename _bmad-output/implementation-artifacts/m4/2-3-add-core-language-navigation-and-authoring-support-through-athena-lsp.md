---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.3: Add Core Language Navigation And Authoring Support Through Athena LSP

Status: done

## Story

As an engineer,
I want core language tooling such as completion and navigation in the workbench,
so that Athena source authoring behaves like a serious programmable environment.

## Acceptance Criteria

1. Given Athena LSP is wired into the workbench, when I use the minimum approved M4 language features on Athena-authored source, then the product supports a serious baseline such as completion, document symbols, go-to-definition, or references, and those features are available inside the Theia workbench rather than only through CLI tooling.
2. Given the first navigation and authoring features are implemented, when the feature boundary is reviewed, then the implementation remains small enough for M4, and richer language behavior can be added later without changing the semantic authority boundary.

## Tasks / Subtasks

- [x] Add Athena-owned server document tracking for authoring requests. (AC: 1, 2)
  - [x] Keep the latest in-memory authored text and compiler result per open document inside `ide/lsp`.
  - [x] Reuse the same tracked document path for diagnostics and language requests.
- [x] Implement the minimum serious LSP authoring/navigation request set. (AC: 1, 2)
  - [x] Add `textDocument/completion`.
  - [x] Add `textDocument/documentSymbol`.
  - [x] Add `textDocument/definition`.
  - [x] Add `textDocument/references`.
- [x] Keep the implementation downstream of Athena-owned state. (AC: 1, 2)
  - [x] Derive completion and navigation data from server-owned AST/compiler state, not frontend-owned parsing.
  - [x] Keep the Theia frontend as a transport consumer only.
- [x] Wire the first Monaco providers in the Theia frontend. (AC: 1)
  - [x] Bridge completion requests to Athena LSP.
  - [x] Bridge document symbols, definition, and references to Athena LSP.
  - [x] Keep the bridge additive on top of the current diagnostics path.
- [x] Update docs and implementation artifacts. (AC: 1, 2)
  - [x] Update `ide/README*`, `ide/lsp/README*`, and frontend/backend package READMEs.
  - [x] Update `docs/usages/athena-workspace-summary.md`.
  - [x] Record the story outcome in `_bmad-output/implementation-artifacts/m4/`.
- [x] Verify sequentially on Windows. (AC: 1, 2)
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`.
  - [x] Run `Set-Location ide; yarn build`.
  - [x] Run a direct stdio LSP proof for completion, document symbols, definition, and references.
  - [x] Run `Set-Location ide; yarn verify:desktop`.
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

## Dev Notes

### Implementation Notes

- The M4 baseline intentionally stays small:
  - server-owned tracked document state inside `ide/lsp`
  - one same-document navigation index for completion, symbols, definition, and references
  - Monaco providers that proxy to Athena LSP through the existing backend request bridge
- No frontend-owned parser or semantic engine was added.
- The current navigation proof is same-document only. Cross-file navigation, hover, rename, formatting, and richer incremental reuse remain later work.

### Files Changed

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`
- `ide/README.md`
- `ide/README.zh-CN.md`
- `ide/lsp/README.md`
- `ide/lsp/README.zh-CN.md`
- `ide/theia-frontend/README.md`
- `ide/theia-frontend/README.zh-CN.md`
- `ide/theia-backend/README.md`
- `ide/theia-backend/README.zh-CN.md`
- `docs/usages/athena-workspace-summary.md`

### Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`
- `Set-Location ide; yarn build`
- direct stdio LSP authoring proof:
  - completion returns `connect` for partial authored source
  - document symbols return `FactoryLine`, `PLC1`, `PLC1.out`, and `connect PLC1.out -> PLC1.out`
  - definition resolves `PLC1.out` usage to its declaration
  - references return declaration plus both connection occurrences for `PLC1.out`
- `Set-Location ide; yarn verify:desktop`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`

### Remaining Review Note

- Story `2.4` should now harden repeated-edit continuity and stale-result cleanup on top of this authoring surface rather than replacing it.
