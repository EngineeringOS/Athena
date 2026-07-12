---
baseline_commit: c278a71
---

# Story 2.1: Support Repeated References And Cross-Reference Resolution

Status: done

## Completion Summary

- Added `ProjectionCrossReference` and `ProjectionCrossReferenceKind` to the kernel projection boundary.
- Derived documentation-view repeated references from canonical semantic ids, stable sheet ids, and occurrence ids in the compiler.
- Transported cross-reference state through runtime, LSP, the GLSP adapter, and the Theia selection model without inventing alias identities.

## Acceptance Outcome

1. Repeated references resolve back to one canonical semantic identity across sheets and views.
2. Cross-reference display remains compatible with inspection and review paths.
3. Ambiguity is not hidden behind frontend-local fallback identity tricks.

## Verification

- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerM11DepthTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionDepthTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionM11DepthRequestTest"`
- `yarn test` in `integrations/graph-glsp`
- `yarn test` in `ide/theia-frontend`

## Key Files

- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReferences.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
