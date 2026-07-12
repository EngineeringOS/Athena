---
baseline_commit: c278a71
---

# Story 2.5: Keep The Renderer Downstream Under Serious Electrical Depth

Status: done

## Completion Summary

- Kept repeated references, notation, family ids, and sheet state as kernel/runtime/LSP-owned contracts before they reach graph adapters or workbench widgets.
- Extended the GLSP and Theia adapters as consumers of typed payloads instead of letting them infer or own semantic meaning.
- Documented the dense proof path and public module boundaries so renderer layers remain downstream implementation detail.

## Acceptance Outcome

1. Renderer and workbench layers remain consumers of governed projection/runtime outputs.
2. M11 deepens the electrical workbench without moving semantic authority into frontend state.

## Verification

- `yarn test` in `integrations/graph-glsp`
- `yarn test` in `ide/theia-frontend`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionM11DepthRequestTest"`

## Key Files

- `integrations/graph-glsp/src/athena-glsp-diagram-model.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
