---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.3: Open One Existing Engineering Repository Into An Active Repository Session

Status: done

## Story

As an engineer,
I want to open one existing Engineering Repository into the Athena product,
so that I can begin work in a real repository-backed session instead of a demo-only shell.

## Acceptance Criteria

1. Given the Athena Theia product is running, when I choose an existing Engineering Repository from the open flow, then Athena opens that repository root and activates one Repository Session for the product window, and the session authority is created in the LSP-embedded runtime rather than in frontend-only state.
2. Given one Repository Session is already active, when I open another repository in the same window, then the previous session is replaced according to the single-session M4 rule, and multi-root repository behavior is not introduced implicitly.

## Tasks / Subtasks

- [x] Turn `ide/lsp` into the first live JVM session host for the IDE path. (AC: 1, 2)
  - [x] Register a real Gradle module at `:ide:lsp` instead of keeping `ide/lsp` as README-only structure.
  - [x] Add a temporary M4 repository resolver that maps one repository root to exactly one authored `.athena` source, preferring `src/` when it exists.
  - [x] Activate the repository session through `AthenaRuntime` inside the new `ide/lsp` host instead of storing semantic authority in frontend-only state.
- [x] Wire the Theia product to the runtime-backed repository session path. (AC: 1, 2)
  - [x] Add a product-owned `Open Engineering Repository` command in `ide/theia-frontend`.
  - [x] Add frontend session-state synchronization so a workspace reload re-attaches to the JVM host for the current repository root.
  - [x] Add backend session-host orchestration and explicit repository-session endpoints without importing `kernel/*` into Node/TypeScript.
- [x] Publish one existing Engineering Repository proof fixture for M4. (AC: 1)
  - [x] Add `examples/m4/open-repository-proof/` as the first repository-root fixture.
  - [x] Keep the fixture intentionally light and avoid freezing M5 manifest or package-graph rules.
- [x] Update docs and milestone artifacts. (AC: 1, 2)
  - [x] Update the `ide/*` READMEs so ownership and runtime-session behavior stay legible.
  - [x] Update `docs/usages/athena-workspace-summary.md` with the repository-session proof path.
  - [x] Move this story into the M4 implementation-artifacts folder and update sprint tracking.
- [x] Verify the repository-session boundary honestly. (AC: 1, 2)
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`.
  - [x] Run `Set-Location ide; yarn build`.
  - [x] Run `cmd /c "call java25 && echo shutdown | ide\lsp\build\install\athena-lsp-host\bin\athena-lsp-host.bat --repository-root examples\m4\open-repository-proof"` and confirm `session-ready`.
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"` after adding `:ide:lsp` to the root module graph.
  - [ ] Run an interactive manual Theia open-folder proof in the desktop shell.

## Dev Notes

### Implementation Notes

- `ide/lsp` now hosts the first JVM process for the IDE path. It is not the full LSP server yet, but repository-session authority already moved to the correct module boundary.
- The Theia frontend owns only the open command, home-surface state display, and workspace-root synchronization.
- The Theia backend owns child-process orchestration for the JVM session host and exposes narrow repository-session endpoints.
- The temporary M4 repository rule is explicit:
  - one repository root must resolve to exactly one `.athena` source
  - `src/` is preferred when present
  - multi-root behavior stays out of scope

### Files Changed

- `settings.gradle.kts`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/*`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolverTest.kt`
- `ide/theia-backend/src/node/athena-backend-contribution.ts`
- `ide/theia-backend/src/node/athena-backend-module.ts`
- `ide/theia-backend/src/node/athena-repository-session-manager.ts`
- `ide/theia-frontend/package.json`
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-repository-session-service.ts`
- `ide/theia-frontend/src/browser/style/index.css`
- `examples/README.md`
- `examples/m4/*`
- `docs/usages/athena-workspace-summary.md`

### Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`
- `Set-Location ide; yarn build`
- `cmd /c "call java25 && echo shutdown | ide\lsp\build\install\athena-lsp-host\bin\athena-lsp-host.bat --repository-root examples\m4\open-repository-proof"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`

### Remaining Review Note

- The interactive desktop open-folder proof is still pending manual validation from the running Athena shell. The build and JVM host proof are complete, but Story `1.5` still owns the final deterministic desktop bring-up path.
