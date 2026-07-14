---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 4.2: Persist Accepted Expansion Into Canonical State And Downstream Refresh

Status: done

## Story

As a product owner,
I want accepted Semantic Macro expansion to refresh canonical and downstream state coherently,
so that reused assemblies behave like first-class engineered structure.

## FR Traceability

- FR-6: Athena can persist accepted expansion into canonical engineering state
- NFR-4: Accepted expansion stays deterministic and inspectable after commit
- NFR-6: Accepted reuse still flows through the sole M8 mutation authority

## Acceptance Criteria

1. Given one approved expansion has been committed through M8, when canonical rebuild completes, then source, graph, semantic inspection, and review state all refresh coherently.
2. Given accepted output is inspected, when its representation is reviewed, then the result remains machine-readable semantic structure rather than an opaque generated blob.

## Tasks / Subtasks

- [x] Extend M8 mutation authority with a Semantic Macro bundle command. (AC: 1, 2)
  - [x] Add a command kind that commits one prepared Semantic Macro bundle through `AthenaCommandRuntimeService`.
  - [x] Materialize bundle operations into canonical `EngineeringDocument` components, ports, and connections.
- [x] Route Semantic Macro acceptance through the existing M8 path. (AC: 1)
  - [x] Execute the prepared bundle through command history instead of mutating canonical state inside `reuseRuntime`.
  - [x] Refresh canonical compilation, history, diff inspection, and semantic review from the accepted command result.
- [x] Publish commit evidence across transport and workbench surfaces. (AC: 1, 2)
  - [x] Extend LSP acceptance payloads with command execution, inspection, and semantic review details.
  - [x] Surface committed command id, changed semantic ids, and review counts in the reuse workbench.
- [x] Verify runtime and UI end to end. (AC: 1, 2)
  - [x] Add runtime and LSP tests for canonical commit, history, and inspection refresh.
  - [x] Extend Electron smoke to verify committed-through-M8 acceptance details in the UI.

## Implementation Notes

- Added `AthenaApplySemanticMacroBundleCommand` and `APPLY_SEMANTIC_MACRO_BUNDLE` so accepted reuse uses the same canonical write authority, history journal, and diff refresh model as other M8 semantic commands.
- `AthenaSemanticMacroRuntimeService.accept()` now commits the approved bundle through `AthenaCommandRuntimeService` with `SEMANTIC_MACRO_ACCEPTED` origin, then returns command id, changed semantic ids, semantic diff inspection, and semantic review output.
- Bundle execution materializes machine-readable `EngineeringComponent`, `EngineeringPort`, and `EngineeringConnection` objects instead of storing opaque generated blobs.
- Reuse workbench approval status now shows the committed M8 command id, changed semantic-id count, and downstream review counts; Electron smoke was hardened to create the widget before waiting for its DOM root.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSemanticMacroRuntimeServiceTest`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaReuseRequestTest`
- `yarn workspace @engineeringood/athena-theia-frontend test`
- `yarn build`
- `yarn start:smoke:reuse-catalog`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeServiceTest.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMacroProtocol.kt]
- [Source: ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaReuseRequestTest.kt]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-catalog-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-protocol.ts]
- [Source: ide/theia-product/scripts/athena-electron-reuse-e2e-main.js]
- [Source: ide/theia-product/scripts/verify-athena-reuse-catalog.js]

## Story Completion Status

- Status: done
- Completion note: Approved Semantic Macro reuse now commits through M8, refreshes canonical and downstream state coherently, and remains inspectable in runtime, transport, and UI surfaces.
