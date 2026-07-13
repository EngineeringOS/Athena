# Story 1.3: Publish Presentation IR Through Existing Runtime And Transport Seams

Status: done

## Implementation Summary

- Published `Presentation IR` through the existing runtime projection snapshot, LSP payloads, GLSP adapter, and Theia workbench graph path.
- Frontend graph workbench now prefers Athena-owned `diagram.presentation` when present instead of reconstructing the full presentation from generic graph nodes alone.

## Evidence

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
- `yarn test` in `integrations/graph-glsp`
- `yarn test` in `ide/theia-frontend`
