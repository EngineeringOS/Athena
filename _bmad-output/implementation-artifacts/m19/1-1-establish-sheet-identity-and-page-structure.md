---
baseline_commit: 80a790cb5e7b6733055a529e4f6f6de5b03ff857
---

# Story 1.1: Establish sheet identity and page structure

Status: done

## Story

As an engineer,
I want the schematic sheet to carry stable page identity, frame, zones, and title block,
so that I can trust the sheet as a real engineering publication surface.

## Acceptance Criteria

1. Given governed semantic input for a projected view, the projection emits a stable `ProjectionSheetId`, sheet order, and display name that do not depend on frontend-local state.
2. Given the same governed input, repeated projection runs produce the same sheet identity and page structure.
3. The sheet contract carries publication semantics for page size, frame, coordinate zones, title block, revision metadata, and view composition before rendering.
4. The runtime projection payload and IDE-facing sheet payload preserve the same sheet identity and page structure without the frontend reconstructing them.
5. The Theia workbench can display the sheet as a schematic-sheet workflow surface, but it may only consume projection-produced sheet facts and may not invent sheet/page semantics locally.
6. A small governed proof fixture set demonstrates deterministic sheet identity and page structure from local repository state.
7. Cabinet preview, full EPLAN parity, public repository/import ecosystem work, and final protocol/layout-stack selection remain out of scope for this story.

## Tasks / Subtasks

- [x] Define the sheet IR and page-structure contract in the projection model (AC: 1, 2, 3)
  - [x] Extend the sheet contract so page identity and publication metadata live upstream of rendering.
  - [x] Keep sheet identity deterministic and projection-owned.
  - [x] Preserve existing subject identity and sheet ordering semantics.
- [x] Thread page-structure facts through the runtime projection payloads (AC: 1, 3, 4)
  - [x] Extend the runtime sheet model to carry the new sheet IR fields.
  - [x] Update the runtime support mapper so the IDE sees the same sheet identity and page structure.
  - [x] Keep the payload read-only from the frontend perspective.
- [x] Keep the Theia workbench projection-only (AC: 4, 5)
  - [x] Surface the new sheet facts in the workbench model without letting the frontend infer publication semantics.
  - [x] Preserve the existing IDE coherence path for selection, reveal, Problems, and inspection.
- [x] Add deterministic proof coverage (AC: 2, 6)
  - [x] Add focused tests for stable sheet identity and page-structure round-tripping.
  - [x] Add a governed fixture in the M19 proof corpus if needed.
  - [x] Verify repeated runs produce the same observable sheet result.
- [x] Keep scope bounded (AC: 7)
  - [x] Do not introduce cabinet preview work.
  - [x] Do not bind the milestone to the final protocol/layout stack decision.
  - [x] Do not shift semantic authority into the renderer or Theia.

## Dev Notes

### Current State

- `ProjectionSheet` already exists in `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`, but it currently carries only `sheetId`, `displayName`, `order`, `previousSheetId`, `nextSheetId`, and canonical `subjects`.
- `ProjectionModelDeriver.deriveSheets()` currently emits a single deterministic main sheet id like `${view.id}/sheet/01-main` for non-documentation families.
- `AthenaRuntimeProjectionSupport.toRuntimeProjectionSheet()` currently bridges only sheet id, display name, order, and subject ids into `AthenaRuntimeProjectionSheet`.
- `AthenaGraphWorkbenchModel` already treats sheet state as downstream presentation data, not authoritative semantics.

### Architectural Guardrails

- Semantic authority stays upstream in Athena.
- Sheet IR owns publication semantics such as page size, frame, coordinate zones, title block, revision metadata, and view composition.
- Layout facts are deterministic and projection-owned.
- Theia and renderer surfaces may inspect and paint the result, but they may not reconstruct engineering meaning from the canvas.
- Cabinet preview is deferred from M19.
- Final protocol/layout-stack selection is a separate tech-selector discussion, not hidden scope in this story.

### Project Structure Notes

- Likely update targets:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionIdentifiers.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
  - `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/*Test.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/*Test.kt`
- Keep sheet and page-structure types grouped with the existing projection contracts; do not split tiny value objects into a file-per-type layout.

### Testing Requirements

- Use the repository-pinned stack only: Java 25, Kotlin 2.4.0, Gradle 9.6.1.
- Run Gradle verification sequentially on Windows; never overlap `gradlew` invocations.
- Verify deterministic sheet identity and page-structure round-tripping at the projection, runtime, and IDE-facing seams.
- Add or update fixture coverage for the smallest governed M19 proof set that demonstrates the new sheet contract.
- If text files change, run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 1, Story 1.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-1, FR-3, FR-6, FR-7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md` - Sheet IR, Layout pattern, Visual cues to preserve, Boundary Notes]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-3, AD-4, AD-5, AD-6, AD-7, AD-8, AD-9]
- [Source: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt:193`]
- [Source: `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt:57`]
- [Source: `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt:67`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts:40`]
- [Source: `draft/layouts/001-disucss.md` - stack and layout discussion]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 planning artifacts and current projection/runtime/workbench contracts.
- Implemented the sheet publication contract in projection, runtime, and LSP payloads.
- Extended runtime and LSP tests to cover deterministic sheet publication round-tripping.
- Updated stale runtime parse-failure assertions to match the current diagnostic text.
- Verified `:kernel:projection-model:test`, `:kernel:runtime:test`, `:ide:lsp:test`, and `yarn workspace @engineeringood/athena-theia-frontend build`.

### Completion Notes List

- Added governed sheet publication data upstream in `ProjectionSheet`.
- Mirrored the publication contract through runtime projection models and LSP payloads.
- Added deterministic publication assertions to projection-model, runtime, and LSP tests.
- Kept the Theia bridge types aligned with the new payload shape.

### File List

- `_bmad-output/implementation-artifacts/m19/1-1-establish-sheet-identity-and-page-structure.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
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

## Change Log

- 2026-07-16: Implemented governed sheet publication contracts, runtime/LSP round-tripping, and verification coverage for M19 Story 1.1.
