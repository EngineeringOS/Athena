---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.1: Bootstrap Athena LSP As The Sole Semantic Entry Point For The IDE

Status: done

## Story

As a language-tooling engineer,
I want Athena LSP to be wired into the Theia product as the only semantic entry point,
so that the IDE reaches kernel and runtime meaning through one governed protocol boundary.

## Acceptance Criteria

1. Given the Theia product modules exist, when the first LSP integration is introduced, then Athena provides an `ide/lsp` module that embeds the existing JVM runtime, compiler, validation, and plugin-hosting stack, and neither the Theia frontend nor backend calls `kernel/*` directly for semantic state.
2. Given the Theia product and the Athena LSP server are both running, when the workbench opens an Athena-authored source file, then the frontend communicates with semantic services through standard or Athena-namespaced LSP methods, and the IDE semantic path is visibly `frontend -> LSP -> runtime/compiler`.

## Tasks / Subtasks

- [x] Convert `ide/lsp` from a repository-session-only JVM host into a real stdio Athena LSP server. (AC: 1, 2)
  - [x] Keep the existing runtime-backed repository activation logic inside the JVM boundary.
  - [x] Add minimal `initialize` and `textDocument/didOpen` handling for the first LSP proof.
  - [x] Preserve the earlier `--repository-root` compatibility mode so prior story proofs do not regress.
- [x] Replace backend bootstrap handshaking with LSP `initialize`. (AC: 1, 2)
  - [x] Spawn the JVM host in stdio LSP mode.
  - [x] Initialize it from the backend using repository-root workspace metadata.
  - [x] Expose generic LSP transport endpoints from the Theia backend without introducing direct kernel imports.
- [x] Route the first `.athena` editor-open path through Athena LSP. (AC: 2)
  - [x] Register `.athena` as an Athena language in the frontend.
  - [x] Send `textDocument/didOpen` through the backend bridge when an Athena-authored file becomes current.
  - [x] Surface the semantic path in the workbench state and output channel.
- [x] Update M4 docs and artifacts. (AC: 1, 2)
  - [x] Update `ide/README*`, `ide/lsp/README*`, and package READMEs to describe the LSP boundary correctly.
  - [x] Update `docs/usages/athena-workspace-summary.md`.
  - [x] Record the story outcome in `_bmad-output/implementation-artifacts/m4/`.
- [x] Verify the implementation sequentially on Windows. (AC: 1, 2)
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`.
  - [x] Run `Set-Location ide; yarn install`.
  - [x] Run `Set-Location ide; yarn build`.
  - [x] Run a direct stdio LSP proof: `initialize` plus `textDocument/didOpen` against `examples/m4/open-repository-proof`.
  - [x] Run `Set-Location ide; yarn verify:desktop`.
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

## Dev Notes

### Implementation Notes

- The architectural shift in this story is small but important:
  - `ide/lsp` is now a real stdio LSP server, not only a one-shot repository-session helper
  - Theia backend now speaks LSP `initialize` to the JVM host instead of relying on a custom JSON line protocol
  - Theia frontend now forwards `.athena` editor-open semantics through `textDocument/didOpen`
- The current proof is intentionally narrow:
  - repository activation at `initialize`
  - authored-source open through `textDocument/didOpen`
  - visible semantic path in the home surface and output channel
- Diagnostics, navigation, completion, hover, and richer semantic inspection remain the next stories. This story only establishes the governed protocol boundary and proves it works.

### Files Changed

- `gradle/libs.versions.toml`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspServerCli.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/Main.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServerTest.kt`
- `ide/theia-backend/package.json`
- `ide/theia-backend/src/node/athena-backend-contribution.ts`
- `ide/theia-backend/src/node/athena-repository-session-manager.ts`
- `ide/theia-frontend/package.json`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`
- `ide/theia-frontend/src/browser/athena-repository-session-service.ts`
- `ide/README.md`
- `ide/README.zh-CN.md`
- `ide/lsp/README.md`
- `ide/lsp/README.zh-CN.md`
- `ide/theia-backend/README.md`
- `ide/theia-frontend/README.md`
- `docs/usages/athena-workspace-summary.md`

### Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`
- `Set-Location ide; yarn install`
- `Set-Location ide; yarn build`
- direct stdio LSP proof result:
  - `{"repositoryRoot":"D:\\Aaron\\workspace\\projects\\2026\\eos\\Athena\\examples\\m4\\open-repository-proof","sourcePath":"D:\\Aaron\\workspace\\projects\\2026\\eos\\Athena\\examples\\m4\\open-repository-proof\\src\\factory-line.athena","projectName":"factory-line","semanticPath":"frontend -> LSP -> runtime/compiler"}`
- `Set-Location ide; yarn verify:desktop`
  - result: `Athena desktop smoke start passed. ready=true javaHome=D:\Program Files\Java\openjdk-25.0.2`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`

### Remaining Review Note

- The semantic boundary is now real, but still intentionally thin. Story `2.2` should start from this exact transport path and add diagnostics instead of creating a side channel.
