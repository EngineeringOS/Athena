---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 3.1: Transport Document Projection Snapshot To Theia

Status: done

## Story

As an Athena IDE integrator,
I want Theia to receive document projection snapshots and occurrence indexes from the compiler
runtime,
so that sheet-view navigation uses governed projection data rather than canvas scans.

## Acceptance Criteria

1. The LSP presentation payload includes compact document reference marker facts needed by M26.
2. The marker payload preserves canonical subject identity, source/target occurrence identity,
   selected sheet view, relation type, and compact display notation.
3. The transport remains compatible with existing M24/M25 single-sheet consumers.
4. Theia/GLSP source types admit the marker payload without desktop-viewer or Compose changes.
5. Tests cover payload shape and backward compatibility.

## Tasks / Subtasks

- [x] Extend LSP presentation payloads (AC: 1, 2, 3)
  - [x] Add reference marker payload contracts.
  - [x] Map `PresentationDocument.referenceMarkers` through `toPayload()`.
- [x] Extend Theia/GLSP source types (AC: 4)
  - [x] Add optional marker payload fields to the translation-only presentation source type.
  - [x] Preserve optional compatibility for existing diagrams without markers.
- [x] Add transport tests (AC: 2, 3, 5)
  - [x] Verify marker payload shape from a presentation document.
  - [x] Verify default payloads remain marker-empty for existing presentation documents.

## Dev Notes

- Active frontend is Theia only.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend modules.
- Keep marker meaning upstream; Theia must not infer document references from canvas geometry.
- Do not add new `.athena` syntax.
- Verification must run sequentially on Windows.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` failed in red phase because marker payload fields and direct document-projection dependency were missing.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed after adding marker payload mapping.
- `yarn test` in `integrations/graph-glsp` passed.
- `yarn test` in `ide/theia-frontend` passed.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added LSP reference marker payload contracts and document location payloads.
- Mapped `PresentationDocument.referenceMarkers` into `AthenaPresentationDocumentPayload`.
- Added GLSP translation source types and Theia frontend marker resolver.
- Preserved backward compatibility through optional/empty marker payload defaults.

### File List

- `_bmad-output/implementation-artifacts/m26/3-1-transport-document-projection-snapshot-to-theia.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationReferenceMarkerPayloadTest.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/scripts/athena-m26-reference-marker-transport.test.mjs`

## Change Log

- 2026-07-20: Created Story 3.1 from M26 Epic 3.
- 2026-07-20: Implemented document reference marker transport through LSP and Theia source types.
- 2026-07-20: Marked Story 3.1 done after full verification.
