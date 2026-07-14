---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 5.2: Expose Origin And Membership Inspection Across Product Surfaces

Status: done

## Story

As an engineer,
I want Athena to reveal macro origin and expansion membership across product surfaces,
so that accepted reuse stays inspectable after the initial preview is gone.

## FR Traceability

- FR-8: Athena exposes accepted Semantic Macro traceability across transport and workbench surfaces
- FR-10: Athena can identify which accepted expansion a selected subject belongs to
- NFR-4: Accepted reuse remains inspectable after the preview panel is dismissed

## Acceptance Criteria

1. Given one accepted expansion exists, when the engineer inspects source, graph, or review surfaces, then Athena can reveal which Semantic Macro created the structure and which parameter values were used.
2. Given the engineer selects one expanded subject, when origin or membership inspection is requested, then Athena can identify the accepted expansion that subject belongs to.

## Tasks / Subtasks

- [x] Extend frontend transport for runtime-owned origin inspection. (AC: 1, 2)
  - [x] Added typed origin inspection request and payload contracts in frontend protocol and bridge services.
  - [x] Kept origin lookup transport-neutral so later surfaces can reuse the same runtime endpoint.
- [x] Surface accepted origin traceability in the reuse workbench. (AC: 1, 2)
  - [x] Added an origin traceability panel with status, expansion id, command id, matched role, parameter values, and memberships.
  - [x] Added subject-level and instantiation-level inspection actions.
- [x] Verify UI persistence after preview dismissal. (AC: 1, 2)
  - [x] Extended desktop Electron smoke to assert the origin panel is ready after approval.
  - [x] Verified origin traceability persists after the preview panel is cancelled.

## Implementation Notes

- The reuse catalog now auto-loads accepted origin inspection after approval and keeps that traceability visible even after the preview panel is dismissed.
- Membership rows can be re-inspected by subject, while the panel also supports instantiation-level requery.
- The current product proof surface is the reuse catalog, but the underlying LSP/runtime contract is shared and can be reused by later graph or review surfaces without re-encoding traceability logic.

## Verification

- `yarn workspace @engineeringood/athena-theia-frontend test`
- `yarn build`
- `yarn start:smoke:reuse-catalog`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-protocol.ts]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-catalog-widget.tsx]
- [Source: ide/theia-product/scripts/athena-electron-reuse-e2e-main.js]
- [Source: ide/theia-product/scripts/verify-athena-reuse-catalog.js]

## Story Completion Status

- Status: done
- Completion note: Accepted Semantic Macro origin and membership inspection is now visible in the product workbench and remains available after the preview interaction is gone.
