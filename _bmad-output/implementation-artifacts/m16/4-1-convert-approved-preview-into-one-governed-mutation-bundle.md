---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 4.1: Convert Approved Preview Into One Governed Mutation Bundle

Status: done

## Story

As an engineer,
I want approved Semantic Macro preview to become one governed mutation bundle,
so that accepted reuse still enters canonical state through the sole M8 mutation path.

## FR Traceability

- FR-9: Athena can convert approved preview into one governed mutation bundle
- NFR-4: Acceptance state remains deterministic and inspectable
- NFR-6: Runtime owns accepted expansion handoff instead of frontend-local mutation logic

## Acceptance Criteria

1. Given one preview was built from governed parameters, when the engineer approves it, then runtime returns one deterministic mutation bundle instead of mutating canonical state directly.
2. Given one bundle is prepared, when downstream M8 handoff has not executed yet, then accepted expansion identity, memberships, and affected semantic ids remain inspectable without losing review-first behavior.

## Tasks / Subtasks

- [x] Persist preview-owned acceptance inputs inside runtime session state. (AC: 1)
  - [x] Record preview, package binding, and normalized parameter values by deterministic preview id.
  - [x] Reuse stored preview state during approval instead of trusting frontend resubmission.
- [x] Convert approval into one deterministic acceptance bundle. (AC: 1, 2)
  - [x] Build accepted expansion origin and membership facts from the approved preview.
  - [x] Emit one stable mutation bundle containing component, port, connection, and traceability operations.
- [x] Publish acceptance bundle details across transport and UI surfaces. (AC: 2)
  - [x] Extend LSP acceptance payloads with bundle, accepted expansion, operations, and affected semantic ids.
  - [x] Surface prepared bundle details in the Theia reuse review panel.
- [x] Add verification, including UI E2E. (AC: 1, 2)
  - [x] Add runtime and LSP tests for acceptance-ready bundle preparation.
  - [x] Extend Electron smoke verification to assert bundle preparation in the workbench.

## Implementation Notes

- Added runtime-owned preview session records so approval can resolve the exact governed preview, package binding, and normalized parameter set that was reviewed.
- Implemented `AthenaSemanticMacroAcceptanceReady` plus stable bundle operations for create-component, create-port, create-connection, and expansion-traceability registration.
- Enriched preview origin anchors with deterministic derived semantic ids so preview traceability aligns with later accepted bundle identities.
- Extended LSP and frontend acceptance payloads so the reuse workbench can display bundle id, accepted expansion id, operation count, and affected semantic ids without inventing local mutation truth.
- Kept canonical state unchanged in Story 4.1. Executing the prepared bundle through M8 remains Story 4.2.

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
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroAcceptanceModels.kt]
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
- Completion note: Approved Semantic Macro preview now becomes one deterministic governed mutation bundle with accepted expansion traceability, and the full UI flow is verified end to end without mutating canonical state early.
