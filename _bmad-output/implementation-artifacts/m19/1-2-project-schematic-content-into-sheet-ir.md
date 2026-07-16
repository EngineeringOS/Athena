---
baseline_commit: 80a790cb5e7b6733055a529e4f6f6de5b03ff857
---

# Story 1.2: Project schematic content into sheet IR

Status: done

## Story

As an engineer,
I want canonical semantics to become sheet IR before rendering,
so that the sheet remains a governed projection instead of a frontend reconstruction.

## Acceptance Criteria

1. Given governed semantic input for a schematic view, the projection emits sheet IR that carries page size, coordinate system, revision metadata, and view composition.
2. The sheet IR is derived from canonical projection state, not invented by the renderer or Theia frontend.
3. The runtime projection payload exposes the same sheet IR semantics that the compiler projection derived.
4. Repeated runs on the same governed input produce the same sheet IR content and sheet identity.
5. The sheet IR round-trips the publication semantics needed for the schematic workflow without leaking renderer-local state into the contract.
6. Theia and the renderer continue to consume projection-produced sheet facts only; they do not become alternate sources of page or publication semantics.
7. The implementation remains schematic-first and does not introduce cabinet preview, ecosystem expansion, or final protocol/layout-stack selection work.

## Tasks / Subtasks

- [x] Derive publication semantics from projection state (AC: 1, 2, 4)
  - [x] Extend the compiler projection derivation so the sheet publication contract is populated from governed view state.
  - [x] Keep the sheet IR deterministic for the same semantic input.
  - [x] Preserve canonical subject, order, and sheet identity behavior from Story 1.1.
- [x] Thread the sheet IR through runtime and IDE payloads (AC: 1, 3, 5)
  - [x] Update runtime sheet mapping so the derived sheet IR survives the runtime bridge unchanged.
  - [x] Ensure the LSP projection payload exposes the same publication semantics.
  - [x] Keep Theia and renderer consumers read-only with respect to page semantics.
- [x] Prove deterministic round-tripping (AC: 4, 5, 6)
  - [x] Add or update focused projection tests for deterministic sheet IR derivation.
  - [x] Add or update runtime or LSP tests that verify the same sheet IR arrives at the IDE boundary.
  - [x] Verify repeated runs keep the same sheet identity and publication facts.
- [x] Keep the milestone bounded (AC: 7)
  - [x] Do not add cabinet preview behavior.
  - [x] Do not bind the story to a specific diagram protocol or layout engine decision.
  - [x] Do not move semantic authority into the frontend.

## Dev Notes

### Current State

- Story 1.1 already introduced governed sheet publication contracts in `ProjectionSheet`, runtime sheet models, LSP payloads, and Theia bridge types.
- `ProjectionModelDeriver.deriveSheets()` currently emits deterministic `ProjectionSheet` values for schematic and documentation families.
- `ProjectionSheetPublication.defaultFor(...)` exists, but it still reflects a generic default publication shape rather than a projection-derived schematic publication model.
- Runtime and LSP mapping already preserve sheet publication structure end-to-end.

### Architectural Guardrails

- Semantic authority stays upstream in Athena.
- Sheet IR is a first-class governed contract, not renderer-local state.
- Layout facts and page semantics are projection outputs, not renderer decisions.
- Theia and renderer surfaces may consume derived facts, but may not infer publication meaning on their own.
- Cabinet preview remains deferred.
- Final diagram protocol/layout-stack selection remains a separate tech-selector discussion.

### Project Structure Notes

- Likely update targets:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver*Test.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- Keep publication types grouped with the existing sheet contracts; do not split tiny value objects into one file each.

### Testing Requirements

- Use the repository-pinned stack only: Java 25, Kotlin 2.4.0, Gradle 9.6.1.
- Run Gradle verification sequentially on Windows; never overlap `gradlew` invocations.
- Assert deterministic sheet IR and publication semantics, not just parse or mapping success.
- Keep the proof corpus small and governed.
- If text files change, run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 1, Story 1.2]
- [Source: `_bmad-output/implementation-artifacts/m19/1-1-establish-sheet-identity-and-page-structure.md` - current sheet contract baseline]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-1, FR-3, FR-6, FR-7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md` - Sheet IR, Layout pattern, Boundary Notes]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-3, AD-6, AD-9]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt:193`]
- [Source: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`]
- [Source: `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt:67`]
- [Source: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt:176`]
- [Source: `draft/layouts/001-disucss.md` - GLSP/Sprotty/ELK discussion]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 planning artifacts and the completed Story 1.1 sheet contract baseline.
- Made projection-state publication derivation explicit in compiler, runtime, and LSP sheet factories.
- Added deterministic publication object assertions at the projection-model, runtime, and IDE boundaries.
- Verified `:kernel:compiler:test`, `:kernel:projection-model:test`, `:kernel:runtime:test`, and `:ide:lsp:test` sequentially on Windows.

### Completion Notes List

- Derived sheet publication semantics explicitly from governed projection state.
- Kept runtime and LSP payloads aligned with the same governed sheet IR contract.
- Added deterministic object-level assertions for projection, runtime, and IDE-boundary round-tripping.
- Verified the affected compiler, projection-model, runtime, and LSP suites sequentially.

### File List

- `_bmad-output/implementation-artifacts/m19/1-2-project-schematic-content-into-sheet-ir.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`

## Change Log

- 2026-07-16: Made sheet publication derivation explicit from governed projection state and verified runtime/LSP round-tripping for M19 Story 1.2.
