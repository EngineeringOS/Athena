---
baseline_commit: b699dda601e216033ed0728d610042887aa82561
---

# Story 1.2: Encode representation families for schematic purpose

Status: done

## Story

As an engineer,
I want one governed subject to be able to carry a representation family,
so that the same semantic object can be presented for the right sheet purpose.

## Acceptance Criteria

1. Given a governed engineering subject, when it is projected into a schematic sheet, its representation family is explicit.
2. Representation family data is driven by engineering purpose and projection contracts, not raw pixel state.
3. Representation families can be consumed by later sheet layout and rendering stories without frontend semantic inference.
4. Existing notation, occurrence, anchor, and cross-reference identities remain stable.
5. M20 does not introduce full IEC library breadth or a new component-library ingestion program.

## Tasks / Subtasks

- [x] Define the minimal representation-family shape (AC: 1, 2)
  - [x] Model the family as a projection/sheet fact, not renderer state.
  - [x] Keep the first family focused on `schematic-sheet`.
  - [x] Preserve canonical subject and occurrence ids.
- [x] Map representation families through runtime and LSP payloads (AC: 1, 3, 4)
  - [x] Keep existing notation and cross-reference payloads compatible.
  - [x] Avoid duplicating meaning already carried by canonical subjects.
- [x] Add regression coverage (AC: 1, 3, 4)
  - [x] Assert a projected subject carries the expected schematic representation family.
  - [x] Assert family data does not break selection/reveal identity.
- [x] Enforce scope boundaries (AC: 5)
  - [x] Do not import or model full IEC libraries.
  - [x] Do not add repository or marketplace behavior.

## Dev Notes

### Current State

- M19 proves symbols, labels, terminals, conductors, and cross references can be projected from governed semantics.
- M20 needs a representation-family hook so layout and rendering can distinguish engineering presentation purpose without guessing from canvas shape.

### Architectural Guardrails

- Follow M20 AD-1, AD-2, AD-5, AD-8, and AD-9.
- Representation family is downstream of canonical semantics and upstream of rendering.
- Do not model `symbol -> meaning`; keep `meaning -> representation`.

### Project Structure Notes

- Likely update targets:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionNotation.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- Keep the naming aligned with `schematic-sheet` and existing projection family identifiers.

### Testing Requirements

- Add focused tests at the projection-model seam first.
- Add runtime/LSP round-trip coverage only for fields exposed to the IDE.
- Reuse the M19 schematic proof fixture where possible.

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- `git rev-parse HEAD`
- `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
- `yarn build`
- `yarn test`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added governed `representationFamilyId` support for `ProjectionSheetComposition` with the first family fixed to `schematic-sheet`.
- Propagated the sheet-family data through runtime and LSP payloads without changing canonical subject, occurrence, or cross-reference identity.
- Added regression coverage at the projection-model seam and through runtime/LSP round-trips.
- Fixed the `kernel/connection-model/README.zh-CN.md` encoding so it is valid UTF-8 with BOM.
- Aligned the local M19 boundary test with the completed sprint status so the Theia frontend package test suite stays green.

### File List

- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphProjectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/scripts/athena-m19-boundary.test.mjs`
- `kernel/connection-model/README.zh-CN.md`

### Change Log

- Added representation-family publication for schematic sheets across projection, runtime, LSP, and Theia-facing bridge layers.
- Added and updated regression coverage to keep schematic-sheet identity stable while preserving canonical navigation and reveal behavior.
- Fixed the Simplified Chinese connection-model README encoding and refreshed the local frontend boundary assertion for the completed M19 state.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 1, Story 1.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-2]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-2, AD-5]
- [Source: `draft/elements-lib/0001-qelectrotech-elements.md`]

## Status

review
