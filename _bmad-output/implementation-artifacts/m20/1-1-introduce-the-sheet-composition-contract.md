---
baseline_commit: b699dda601e216033ed0728d610042887aa82561
---

# Story 1.1: Introduce the sheet composition contract

Status: done

## Story

As an engineer,
I want the sheet to be represented as explicit composition data,
so that the frame, title block, zones, views, and occurrences feel governed rather than ad hoc.

## Acceptance Criteria

1. Given the M19 schematic proof, when the sheet is projected in M20, the sheet composition keeps frame, title block, zones, views, and occurrences distinct.
2. The composition contract is produced upstream of rendering and remains separate from renderer paint state.
3. Runtime and IDE-facing payloads preserve the same composition facts without frontend reconstruction.
4. Existing M19 sheet identity, publication metadata, and canonical subject behavior remain compatible.
5. No cabinet preview, repository/import ecosystem, desktop viewer, or layout-stack selection work is introduced.

## Tasks / Subtasks

- [x] Define the M20 sheet composition contract (AC: 1, 2)
  - [x] Identify the smallest extension from the M19 `ProjectionSheet` and publication model.
  - [x] Keep frame, title block, zones, views, and occurrences explicit.
  - [x] Keep the contract immutable and projection-owned.
- [x] Thread composition facts through runtime and IDE payloads (AC: 2, 3, 4)
  - [x] Preserve the M19 sheet publication fields.
  - [x] Add only the payload fields needed by M20 stories.
  - [x] Avoid frontend-derived sheet/page semantics.
- [x] Add focused contract coverage (AC: 1, 3, 4)
  - [x] Assert composition facts survive projection, runtime, and IDE seams.
  - [x] Assert repeated projection of the same fixture keeps stable composition facts.
- [x] Keep scope bounded (AC: 5)
  - [x] Do not add cabinet preview behavior.
  - [x] Do not choose a layout engine or protocol stack.

## Dev Notes

### Current State

- M19 added governed sheet publication data around `ProjectionSheet` and runtime sheet payloads.
- Existing sheet payloads already carry page size, frame, coordinate zones, title block, revision metadata, and view composition.
- M20 should refine the composition contract rather than replace the M19 sheet model.

### Architectural Guardrails

- Follow M20 AD-1, AD-2, AD-6, AD-8, and AD-9.
- Theia and the renderer consume composition facts; they do not construct sheet meaning.
- Keep the renderer paint-only and keep semantic authority upstream.

### Project Structure Notes

- Likely update targets:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- Keep small related Kotlin value types grouped with the existing projection/runtime model files.

### Testing Requirements

- Add or update projection, runtime, and LSP tests for composition round-tripping.
- Run Gradle verification sequentially on Windows; never overlap `gradlew` tasks.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text changes.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 1, Story 1.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-1, FR-2]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-1, AD-2]
- [Source: `_bmad-output/implementation-artifacts/m19/1-1-establish-sheet-identity-and-page-structure.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Added first-class sheet composition contracts across projection, runtime, and IDE payloads while preserving existing publication fields.
- Added projection-model, runtime, and LSP coverage for composition round-tripping.
- Verified `:kernel:projection-model:test`, `:kernel:runtime:test`, and `:ide:lsp:test`.
- Encoding audit passed after the text changes.

### Completion Notes List

- Composition facts now travel as an explicit contract across projection, runtime, and IDE payloads.
- Existing M19 publication fields remain intact for compatibility.
- The new contract stays projection-owned and renderer-neutral.

### File List

- `_bmad-output/implementation-artifacts/m20/1-1-introduce-the-sheet-composition-contract.md`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`

## Change Log

- 2026-07-16: Introduced first-class sheet composition contracts across projection, runtime, and LSP payloads.
