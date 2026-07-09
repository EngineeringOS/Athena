---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.3: Surface Read-Only Semantic Inspection Beside Source Editing

Status: done

## Story

As an engineer,
I want to inspect semantic information beside the authored source I am editing,
so that Athena keeps engineering meaning visible in the same workflow as text authoring.

## Acceptance Criteria

1. Given an Athena-authored source file is open in the editor, when I open the semantic inspection surface, then Athena shows read-only semantic information or closely related derived inspection data beside source editing, and the inspection data is returned through the Athena protocol boundary from the LSP-embedded runtime.
2. Given semantic inspection is visible in the workbench, when I review how it behaves, then it does not mutate canonical semantic state directly, it does not become a hidden second semantic authority in the frontend, and it does not pretend to be the later graphical editing layer.

## Tasks / Subtasks

- [x] Add one Athena-owned read-only inspection request to the JVM LSP boundary. (AC: 1, 2)
  - [x] Return canonical derived document counts, diagnostics summaries, and semantic lists from tracked Athena-owned document state.
  - [x] Keep the request read-only and downstream of LSP-owned tracked compilation state.
- [x] Add one right-panel Athena semantic inspection widget in the frontend. (AC: 1, 2)
  - [x] Refresh inspection data from the current Athena editor.
  - [x] Keep the panel read-only.
  - [x] Show useful empty and unavailable states when there is no active Athena document or repository session.
- [x] Attach the new panel through Athena-owned product commands. (AC: 1, 2)
  - [x] Add a `Reveal Semantic Inspection` command.
  - [x] Add the command to the Athena `View` submenu.
  - [x] Add a home-surface quick action for the panel.
- [x] Add regression coverage. (AC: 1, 2)
  - [x] Verify that inspection follows the latest tracked document state after open and change.
- [x] Verify sequentially on Windows with Java 25. (AC: 1, 2)
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`.
  - [x] Run `Set-Location ide; yarn workspace @engineeringood/athena-theia-frontend build`.
  - [x] Run `Set-Location ide; yarn verify:desktop`.
  - [x] Run `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

## Dev Notes

### Implementation Notes

- The read-only inspection panel deliberately stays textual and canonical for M4.
- The frontend does not derive semantic meaning on its own; it only requests the latest inspection payload through `athena/semanticInspection` after the same document-sync path used by diagnostics and navigation.
- The current inspection surface exposes:
  - document status
  - diagnostics summaries
  - canonical component list
  - canonical port list
  - canonical connection list
- It does not mutate semantic state and it does not stand in for a later graphical or bidirectional editor.

### Files Changed

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-home-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`

### Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"`
- `Set-Location ide; yarn workspace @engineeringood/athena-theia-frontend build`
- `Set-Location ide; yarn verify:desktop`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`

### Remaining Review Note

- Story `3.4` should keep this inspection widget additive and preserve the same Athena-owned command/view boundary as more workbench capabilities arrive.
