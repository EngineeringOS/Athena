---
baseline_commit: b699dda601e216033ed0728d610042887aa82561
---

# Story 2.1: Insert the sheet layout model between Presentation IR and rendering

Status: done

## Story

As an architect,
I want a governed sheet layout model to sit between Presentation IR and rendering,
so that layout facts stay explicit and renderer-local guessing stays out of the system.

## Acceptance Criteria

1. Given a governed projection snapshot, when the sheet is laid out, layout facts are produced before rendering.
2. The renderer consumes layout facts and does not invent semantic meaning or authoritative placement.
3. The sheet layout model is deterministic for the same governed input.
4. The model is a contract, not a final layout-engine or protocol decision.
5. Existing M19 sheet publication and selection/reveal paths remain compatible.

## Tasks / Subtasks

- [x] Define the first sheet layout model contract (AC: 1, 2, 4)
  - [x] Represent placement, bounds, routing guidance, and label layout as governed facts.
  - [x] Keep the contract downstream of Presentation IR and sheet composition.
  - [x] Avoid binding to ELK or any final layout engine.
- [x] Thread layout facts to renderer-facing payloads (AC: 1, 2, 5)
  - [x] Keep the renderer as a consumer of facts.
  - [x] Preserve canonical subject and occurrence ids.
- [x] Add deterministic model tests (AC: 3, 5)
  - [x] Assert the same fixture emits the same layout facts.
  - [x] Assert M19 publication behavior still works.

## Dev Notes

### Current State

- M19 already emits deterministic sheet IR, anchors, endpoints, and routing corridors.
- M20 needs a clearer layout contract so renderer behavior is not the hidden authority.
- `draft/layouts/001-disucss.md` is a tech-selector discussion input, not a mandate to choose ELK in this story.

### Architectural Guardrails

- Follow M20 AD-3, AD-4, AD-9, and AD-10.
- `Sheet Layout Model` lives between Presentation IR and renderer.
- Layout intelligence, constraint solving, auto-routing, and final stack choice are deferred.

### Project Structure Notes

- Likely update targets:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- If a new source package is needed, prefer a cohesive `sheet-layout-model` cluster instead of scattering types across renderer files.

### Testing Requirements

- Projection-model contract tests should prove the layout model exists before renderer tests consume it.
- Runtime/LSP tests should prove the fields survive transport.
- Keep Gradle verification sequential.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 2, Story 2.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-3]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-3, AD-9, AD-10]
- [Source: `draft/layouts/001-disucss.md`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- Added `ProjectionSheetLayout` and mirrored runtime/LSP layout payloads.
- Wired `sheetLayout` through `AthenaRuntimeProjectionReadySnapshot` and the LSP ready payload.
- Added contract, runtime, and LSP tests for deterministic layout and transport preservation.
- Verification: `:kernel:projection-model:test`, `:kernel:runtime:test`, `:ide:lsp:test`, `yarn workspace @engineeringood/athena-theia-frontend test`, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### Completion Notes List

- Governed sheet layout facts now exist as a contract between projection and rendering.
- Runtime ready snapshots now carry `sheetLayout` downstream without renderer-owned inference.
- LSP and frontend payload mirrors preserve the layout contract for the IDE surface.
- Deterministic model and transport tests pass.

### File List

- `_bmad-output/implementation-artifacts/m20/2-1-insert-the-sheet-layout-model-between-presentation-ir-and-rendering.md`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheetLayout.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`

## Change Log

- 2026-07-17: Added the governed sheet layout contract and threaded it through runtime, LSP, frontend payloads, and verification.
