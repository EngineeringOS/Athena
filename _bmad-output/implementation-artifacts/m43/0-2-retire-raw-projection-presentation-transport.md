---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 0.2: Retire Raw Projection Presentation Transport

Status: done

## Story

As a maintainer, I can publish `AthenaScenePublication` through the new LSP methods,
so no raw Projection/Spatial presentation transport remains as a second authority.

## Acceptance Criteria

1. `athena/diagramScene`, `athena/diagramConnectOptions`, and `athena/applyDiagramCommand` are the only diagram presentation/authoring methods; each uses M43 generated contracts.
2. `athena/projectionSession`, duplicate Kotlin/TypeScript payloads, and frontend reconstruction path are deleted; no compatibility aliases or fallback readers remain.
3. Runtime/LSP boundary publishes one complete scene publication revision and asset bundle, or explicit `UNAVAILABLE`, never raw Spatial DTOs.
4. Frontend bridge imports generated scene/publication/command types; no `AthenaProjection*Payload`, raw geometry transport, `requestProjectionSession`, SVG paint layer, or Canvas paint layer remains.
5. Architecture absence gate and focused protocol/frontend tests pass; production source-set and encoding audits pass.

## Tasks / Subtasks

- [x] Replace raw LSP protocol (AC: 1, 2, 3)
  - [x] Add typed `athena/diagramScene`, `athena/diagramConnectOptions`, and `athena/applyDiagramCommand` methods using M43 contracts.
  - [x] Delete `AthenaProjectionPayloads.kt`, `AthenaProjectionSessionProtocol.kt`, and `athena/projectionSession`.
  - [x] Publish complete `AthenaScenePublication`; fail closed when scene compiler unavailable.
- [x] Remove duplicate frontend transport (AC: 2, 4)
  - [x] Delete raw Projection/Spatial payload types and `requestProjectionSession`.
  - [x] Update bridge/widget imports to generated M43 contracts and remove source-to-paint reconstruction.
  - [x] Remove old split SVG/Canvas selectors and paint implementation.
- [x] Add absence and contract tests (AC: 1, 2, 5)
  - [x] Test old method/types/selectors absent and new method names present.
  - [x] Validate publication/command schema before transport use.
- [x] Verify and record (AC: 5)
  - [x] Run focused LSP/frontend tests, full Gradle test, frontend test, encoding audit, hygiene audit.
  - [x] Complete story records and set status `review` only after passing evidence.

## Dev Notes

- Follow `_bmad-output/planning-artifacts/m43/epics.md`, `M43-CONTRACT-PACK.md`, `ARCHITECTURE-SPINE.md`, and `LEGACY-REPLACEMENT.md`.
- No backward compatibility. Delete retired transport and tests; do not preserve aliases.
- `AthenaDiagramScene` is sole Presentation Reality contract. LSP transports validated contract objects; frontend never parses source or derives geometry.
- Runtime Projection/Spatial models may remain internal compiler inputs until Story 2-1, but cannot cross presentation transport.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Initial LSP test caught obsolete architecture test referencing deleted protocol; replaced with new absence-gate test.

### Completion Notes List

- Deleted raw Kotlin Projection/Spatial DTO and session mapper; removed `athena/projectionSession`.
- Added typed M43 scene/connect-options/command method names. Scene and command fail closed until later canonical compiler/authoring stories activate.
- Deleted duplicate frontend Projection/Spatial types, request, split SVG/Canvas paint, geometry reconstruction, and CSS selectors.
- Absence gate passes for all listed retired contracts.
- Verification: `:ide:lsp:test` passed; frontend test passed 8/8; full Gradle test passed (114 actionable tasks); encoding and source-set hygiene audits passed.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt` (deleted)
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt` (deleted)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocolArchitectureTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`

### Change Log

- 2026-08-06: Replaced raw Projection/Spatial presentation transport with M43 method boundary and fail-closed publication.
- 2026-08-06: Removed duplicate frontend transport and split SVG/Canvas paint path; final absence
  and regression gates passed; status set to `done`.

### Change Log
