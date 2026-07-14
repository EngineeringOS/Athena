---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 3.3: Support Parameter Editing And Review-First Acceptance In The Workbench

Status: done

## Story

As an engineer,
I want to edit Semantic Macro parameters and review the preview in the workbench,
so that reuse feels like one governed product flow instead of a hidden generator.

## FR Traceability

- FR-8: Athena can support parameter editing and review-first acceptance in the workbench
- NFR-4: Preview and review state remain deterministic and inspectable
- NFR-6: Workbench surfaces remain consumers of platform-owned reuse services

## Acceptance Criteria

1. Given one catalog entry is selected, when the engineer edits meaningful parameters, then the workbench submits those values through shared reuse services rather than local expansion logic.
2. Given preview is displayed, when the engineer approves or cancels, then the flow remains review-first and cancel leaves canonical state unchanged.

## Tasks / Subtasks

- [x] Extend the Theia reuse transport contract for the full review flow. (AC: 1, 2)
  - [x] Add typed frontend request builders and payloads for validation, preview, and preview acceptance.
  - [x] Keep the workbench bound to runtime/LSP-owned responses rather than local macro rules.
- [x] Upgrade the reuse catalog widget into a parameter-and-preview review surface. (AC: 1, 2)
  - [x] Add parameter editors, instantiation identity input, validation diagnostics, preview consequence rendering, and review-first flow messaging.
  - [x] Add preview approval and cancellation controls without introducing direct canonical mutation in the frontend.
- [x] Add UI verification, including end-to-end review flow coverage. (AC: 1, 2)
  - [x] Extend frontend node tests for the typed request builders.
  - [x] Add Electron smoke coverage for governed catalog selection, parameter editing, validation, preview, approval response, and cancellation response.

## Implementation Notes

- Reworked `AthenaSemanticMacroCatalogWidget` into a review-first reuse workbench that edits parameters, asks runtime for validation, renders deterministic preview consequences, and surfaces approval or cancellation as explicit user decisions.
- Added typed frontend protocol helpers and bridge methods for catalog, validation, preview, and acceptance so the workbench remains a thin consumer of the shared reuse seam.
- Added dedicated Electron smoke scripts that create a governed fixture repository, materialize its canonical `athena.lock`, then drive the reuse catalog UI end to end.
- Kept acceptance non-mutating at this stage: the workbench can submit review intent and display the runtime-owned response, but canonical expansion handoff remains Story 4.1.

## Verification

- `yarn workspace @engineeringood/athena-theia-frontend test`
- `yarn build`
- `yarn start:smoke:reuse-catalog`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-catalog-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-protocol.ts]
- [Source: ide/theia-product/scripts/athena-electron-reuse-e2e-main.js]
- [Source: ide/theia-product/scripts/verify-athena-reuse-catalog.js]
- [Source: ide/theia-frontend/scripts/athena-semantic-macro-protocol.test.mjs]

## Story Completion Status

- Status: done
- Completion note: Athena now supports governed Semantic Macro parameter editing and review-first preview flow in the workbench, with Electron E2E proving that cancel preserves unchanged canonical state.
