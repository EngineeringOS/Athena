---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E2.S2: Bind Athena Anchors To Geometry

Status: done

## Story

As a representation author,
I want Athena-declared anchors to bind to concrete geometry references,
so that native Symbols and SVG-backed Elements share one typed anchor contract.

**Requirements:** FR-7, FR-9.

## Acceptance Criteria

1. Athena source declares anchor identity, geometry reference, role, accepted directions, accepted signals, and provenance for each anchor that participates in representation binding.
2. The same typed Anchor contract works for native Athena Symbols and SVG-backed Elements.
3. SVG identifies geometry only through safe package-local geometry references; Athena source assigns the anchor meaning and never defers that meaning to SVG metadata.
4. Binding output is typed, deterministic, and traceable to source, package, representation, and compiler snapshot.
5. Invalid or missing geometry references fail with source-spanned diagnostics before layout or routing ever sees the representation.

## Tasks / Subtasks

- [x] Add failing tests for explicit anchor-to-geometry binding on native and SVG-backed representations (AC: 1-5)
  - [x] Cover one native Symbol whose anchors lower to typed geometry-backed anchor contracts.
  - [x] Cover one SVG-backed Symbol and one SVG-backed Element that resolve anchors through the safe SVG geometry bridge.
  - [x] Prove the current implicit/first-primitive fallback is not accepted as the final contract.
- [x] Update the compiler-owned anchor bridge so geometry references are explicit and deterministic (AC: 1-4)
  - [x] Keep Athena source as the only semantic authority for anchor meaning.
  - [x] Preserve package-local SVG admission from Story 2.1 and bind anchors only to marked geometry.
  - [x] Make the lowered anchor contract deterministic across native and SVG-backed representations.
- [x] Update tests and fixtures that prove the shared anchor contract (AC: 1-5)
  - [x] Refresh representation compiler tests, snapshot coverage, and any sample SVG or Athena fixtures needed to prove the binding.
  - [x] Keep the change scoped to representation binding; do not expand into layout, routing, or Cabinet placement.
- [x] Run story evidence gate (AC: 1-5)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. The anchor contract must stay typed and compiler-owned; SVG may carry geometry hints only.
- Current anchor source lives in `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` and is parsed in `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`.
- Current lowering uses `AthenaSymbolSourceLowerer` and `AthenaElementSourceLowerer`. SVG-backed symbols currently bind anchors through the SVG geometry bridge, and this story should harden that bridge rather than create a second path.
- `AthenaSvgGraphicBodySupport` marks safe geometry nodes with `data-athena-ref`; that is the only SVG extension this milestone accepts.
- The story must not add layout, routing, planner, or Cabinet work. It should remain inside representation binding and diagnostics.
- If compiler behavior must change, keep the change minimal and deterministic. Do not add compatibility shims for stale geometry-binding behavior.
- Run RED first, then sequential Gradle verification. Do not run Gradle tasks in parallel.

### Project Structure Notes

- Likely touchpoints: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/`, `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/`, `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`, `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`, and `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`.
- Keep text assets UTF-8.
- Do not move work into renderer, routing, or physical-layout modules for this story.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 2.2, FR-7, FR-9.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-7, FR-9, NFR-1, NFR-4.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md` - anchor bridge rules and SVG geometry boundary.
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` - authored anchor contract.
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt` - anchor parser bridge.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt` - symbol anchor validation.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt` - native and SVG-backed anchor lowering.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt` - exported anchor lowering.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt` - safe SVG geometry bridge and `data-athena-ref`.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceCompilerTest.kt` - native anchor lowering coverage.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt` - SVG-backed bridge coverage.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt` - package snapshot and anchor proof coverage.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt` - LSP coverage for governed anchor bindings.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Story created from M36-E2 backlog with explicit geometry-binding scope.
- 2026-07-29: Anchor bridge readback confirmed current SVG geometry nodes are marked with `data-athena-ref` but anchor binding still needs deterministic proof coverage.
- 2026-07-29: Removed stale diagnostic expectations, aligned SVG-backed anchor fixtures to the direct geometry bridge, and verified the full `:kernel:compiler:test` suite sequentially.

### Completion Notes List

- Native symbol anchors now lower to typed geometry-backed contracts with the new `symbol.anchor.ref.*` diagnostics.
- SVG-backed anchor tests now use direct package-local geometry fixtures and prove the shared anchor contract without compatibility shims.
- Verified sequentially with `:kernel:compiler:test`, `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`, and `git diff --check`.

### Change Log

- 2026-07-29: Created M36-E2.S2 story for explicit anchor-to-geometry binding.
- 2026-07-29: Completed anchor bridge hardening, updated native and SVG-backed tests, and verified the compiler suite sequentially.

### File List

- _bmad-output/implementation-artifacts/m36/2-2-bind-athena-anchors-to-geometry.md
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceFormatter.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialBinder.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34DrawingPrimitiveCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceCompilerTest.kt
