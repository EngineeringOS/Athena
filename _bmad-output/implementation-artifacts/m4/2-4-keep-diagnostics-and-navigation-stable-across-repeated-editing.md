---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.4: Keep Diagnostics And Navigation Stable Across Repeated Editing

Status: done

## Story

As an engineer,
I want diagnostics and navigation to stay coherent while I repeatedly edit the same Athena source file,
so that the workbench behaves like a serious editor session instead of recomputing from scratch in a fragile way.

## Acceptance Criteria

1. Given an Athena-authored source file is edited repeatedly in one active Repository Session, when I make consecutive edits and request diagnostics or navigation results after each change, then Athena LSP returns results that stay aligned with the latest saved or in-memory document state, and the workbench does not fall back to a disposable frontend-owned semantic model between requests.
2. Given a file has already been analyzed in the current Repository Session, when I request diagnostics, symbols, completion, or navigation again after another edit, then Athena LSP reuses governed session state for the repeated request path, and the repeated request remains continuous from the user's perspective instead of behaving like a brand-new cold session every time.
3. Given repeated edits introduce and then resolve an authored-source problem, when I inspect diagnostics and navigation after each edit, then stale issues do not remain visible after they are resolved.

## Tasks / Subtasks

- [x] Harden Athena-owned tracked document continuity in `ide/lsp`. (AC: 1, 2, 3)
  - [x] Reject stale tracked-document rollbacks by document version.
  - [x] Reuse tracked compiler and navigation state for repeated requests.
  - [x] Publish diagnostics against the actual tracked document version.
- [x] Serialize frontend document synchronization before follow-up language requests. (AC: 1, 2, 3)
  - [x] Queue `didOpen` and `didChange` notifications per URI in `ide/theia-frontend`.
  - [x] Ensure completion, symbols, definition, and references wait for the latest synchronized text snapshot.
  - [x] Keep the frontend as a transport consumer instead of introducing frontend-owned semantics.
- [x] Add repeated-edit regression coverage. (AC: 1, 2, 3)
  - [x] Cover invalid -> valid -> invalid -> valid transitions.
  - [x] Cover stale lower-version replay after a newer valid change.
- [x] Update docs and implementation artifacts. (AC: 1, 2, 3)
  - [x] Update `ide/README*`, `ide/lsp/README*`, and frontend/backend package READMEs.
  - [x] Update `docs/usages/athena-workspace-summary.md`.
  - [x] Record the story outcome in `_bmad-output/implementation-artifacts/m4/`.
- [x] Verify sequentially on Windows with Java 25. (AC: 1, 2, 3)
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`.
  - [x] Run `Set-Location ide; yarn build`.
  - [x] Run a direct stdio repeated-edit proof against the built Athena LSP host.
  - [x] Run `Set-Location ide; yarn verify:desktop`.
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

## Dev Notes

### Implementation Notes

- `ide/lsp` now treats tracked documents as version-aware session state instead of replacing them blindly on every repeated edit.
- Navigation requests reuse precomputed tracked navigation state rather than rebuilding ad hoc per request.
- `ide/theia-frontend` now serializes document notifications per URI and waits for the latest synchronized document snapshot before sending follow-up language requests.
- The repeated-edit proof stays deliberately small for M4:
  - same-document continuity
  - diagnostics continuity
  - definition continuity
  - stale lower-version rollback rejection

### Files Changed

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepeatedEditingStabilityTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
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
- direct stdio LSP repeated-edit proof:
  - invalid `didOpen` publishes `2` diagnostics at version `1`
  - valid `didChange` publishes `0` diagnostics at version `2` and definition resolves once
  - invalid `didChange` publishes diagnostics again at version `3` and definition resolves zero times
  - valid `didChange` publishes `0` diagnostics at version `4`
  - stale replay of version `3` after version `4` does not roll tracked state backward
- `Set-Location ide; yarn verify:desktop`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`

### Closeout Note

- The final Epic 2 review also corrected the backend diagnostics wait contract so stale lower-version replay no longer conflicts with Athena LSP's monotonic tracked-document versioning.
