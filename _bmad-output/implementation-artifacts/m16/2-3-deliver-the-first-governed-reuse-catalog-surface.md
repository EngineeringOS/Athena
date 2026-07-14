---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 2.3: Deliver The First Governed Reuse Catalog Surface

Status: done

## Story

As an engineer,
I want Athena to show available Semantic Macros in a dedicated reuse catalog,
so that I can start assembly reuse without authoring raw DSL or navigating package internals.

## FR Traceability

- FR-7: Athena can expose available Semantic Macros through a governed reuse catalog
- FR-8: Athena can support guided Semantic Macro parameter editing and review-first acceptance in the workbench
- NFR-6: Workbench surfaces remain consumers of platform-owned reuse services

## Acceptance Criteria

1. Given the first M16 workbench proof is implemented, when the reuse catalog is displayed, then it lists available Semantic Macros from the runtime-owned catalog rather than from hardcoded frontend lists.
2. Given the first proof slice is electrical only, when catalog entries are reviewed, then the surface can show at least `DOL Starter`, `PLC Rack`, and `24V Distribution Unit`.

## Tasks / Subtasks

- [x] Add a frontend-owned transport-safe reuse catalog contract. (AC: 1, 2)
  - [x] Add typed Theia protocol payloads and a stable request builder for `athena/semanticMacroCatalog`.
  - [x] Add deterministic catalog grouping for the first electrical proof slice.
- [x] Deliver the dedicated reuse catalog widget. (AC: 1, 2)
  - [x] Add a dedicated Theia widget backed by the runtime-owned catalog request.
  - [x] Register the widget in the workbench module, command palette, and right-side layout extensions.
  - [x] Keep the surface read-only and runtime-owned until parameter editing lands in Story 3.
- [x] Add frontend verification. (AC: 1, 2)
  - [x] Add node-based frontend tests covering the request contract.
  - [x] Add grouping tests that prove the electrical proof-slice entries render through governed catalog categories rather than hardcoded menus.

## Implementation Notes

- Added `AthenaSemanticMacroCatalogWidget` as a dedicated right-panel reuse catalog surface under `ide/theia-frontend`.
- Added `athena-semantic-macro-protocol.ts` and `athena-semantic-macro-model.ts` so the frontend consumes one typed runtime-owned catalog and groups it deterministically for the first electrical proof slice.
- Registered the widget through `athena-frontend-module.ts` and `ATHENA_WORKBENCH_EXTENSIONS`, keeping the surface inside the existing Athena workbench composition instead of inventing a separate UI path.
- Kept the surface catalog-only for now: it explains scope and the Story 3 handoff rather than faking local parameter editing or local preview logic.

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-catalog-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-protocol.ts]
- [Source: ide/theia-frontend/src/browser/athena-semantic-macro-model.ts]
- [Source: ide/theia-frontend/src/browser/athena-workbench-extensions.ts]
- [Source: ide/theia-frontend/scripts/athena-semantic-macro-protocol.test.mjs]
- [Source: ide/theia-frontend/scripts/athena-semantic-macro-model.test.mjs]

## Story Completion Status

- Status: done
- Completion note: Athena now exposes a dedicated governed reuse catalog surface in the Theia workbench, and the frontend tests prove it consumes runtime-owned catalog entries and can present the first electrical proof-slice names without reverting to hardcoded lists.
