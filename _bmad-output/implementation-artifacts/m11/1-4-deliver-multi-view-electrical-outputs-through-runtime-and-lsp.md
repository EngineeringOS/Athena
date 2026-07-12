---
baseline_commit: c278a71
---

# Story 1.4: Deliver Multi-View Electrical Outputs Through Runtime And LSP

Status: done

## Completion Summary

- Extended runtime-owned projection snapshots with electrical `familyId`, governed sheet metadata, notation-pack payloads, and later M11 cross-reference fields.
- Extended `ide/lsp` projection payloads so the workbench consumes the same JVM-owned multi-view output without reconstructing engineering meaning in frontend code.
- Kept delivery on the existing runtime session and `athena/projectionSession` / `athena/projectionCommand` seam.

## Acceptance Outcome

1. Richer electrical views, sheets, and notation now flow through runtime projection sessions and `ide/lsp`.
2. Delivered payloads identify canonical subjects plus downstream electrical family context without creating a second semantic authority.

## Verification

- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`

## Key Files

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
