---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E2.S3: Reject Invalid Geometry Bindings

Status: done

## Story

As an engineer or AI agent,
I want invalid Port-to-Anchor geometry bindings to fail closed,
so that no layout or routing is based on inferred, ambiguous, or conflicting geometry.

**Requirements:** FR-2, FR-8.

## Acceptance Criteria

1. A representation that declares Port-to-Anchor bindings but omits, duplicates, or conflicts
   geometry references fails with source-spanned diagnostics before binding output is accepted.
2. Native Athena Symbols and SVG-backed Elements use the same typed compiler/LSP diagnostic
   contract for invalid geometry binding.
3. The compiler never infers Port semantics from unmarked SVG geometry, CSS, DOM order, visual
   appearance, or primitive order.
4. Missing, duplicate, ambiguous, or conflicting geometry references fail closed and prevent any
   binding result from reaching layout or routing lowering.
5. Invalid binding evidence remains traceable to source, package snapshot, representation source,
   and compiler snapshot.

## Tasks / Subtasks

- [x] Add failing tests for invalid geometry binding on native and SVG-backed representations
  (AC: 1-5)
  - [x] Cover a native Symbol with a missing Anchor ref and one with an unresolved Anchor ref.
  - [x] Cover an SVG-backed Element with duplicate `data-athena-ref` geometry references.
  - [x] Prove the compiler does not infer Port meaning from DOM order, visual position, or
        unmarked geometry.
- [x] Harden the compiler-owned invalid-binding path so it fails closed and emits one diagnostic
  contract (AC: 1-5)
  - [x] Keep Athena source as the only authority for Anchor meaning.
  - [x] Reject missing, duplicate, ambiguous, and conflicting geometry refs before any downstream
        lowering consumes the representation.
  - [x] Preserve the current `data-athena-ref`-only SVG bridge and do not add fallback inference.
- [x] Update tests, fixtures, and LSP coverage that prove the invalid-binding contract (AC: 1-5)
  - [x] Refresh compiler tests and sample fixtures that need explicit invalid-binding coverage.
  - [x] Add or update LSP coverage so the same diagnostics surface through the IDE path.
  - [x] Keep the change scoped to representation binding and diagnostics; do not expand into
        layout, routing, or Cabinet placement.
- [x] Run story evidence gate (AC: 1-5)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. This story must stay inside representation binding and diagnostics.
- Current validator behavior lives in
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt`.
  It already emits `symbol.anchor.ref.missing` and `symbol.anchor.ref.unresolved`; extend the
  invalid-binding coverage without adding an alternate geometry authority.
- Current lowering behavior lives in
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`.
  `lowerAnchor` currently resolves a geometry reference by explicit match and should fail closed on
  missing or ambiguous refs, not infer from primitive order or fallback heuristics.
- SVG geometry indexing lives in
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`.
  `data-athena-ref` is the sole SVG extension. Duplicate geometry refs already produce
  `svg.geometry-ref.duplicate`; keep SVG geometry-only.
- Use the same typed diagnostic vocabulary for native and SVG-backed failures. Do not add
  compatibility shims or a second binding path.
- Invalid geometry bindings must fail before layout or routing sees the representation. Do not move
  this story into planner, physical layout, routing, or renderer work.
- Run RED first, then sequential Gradle verification. Do not run Gradle tasks in parallel.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Keep text assets UTF-8.
- Keep scope out of renderer, routing, and physical-layout modules for this story.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 2.3, FR-2, FR-8.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-2, FR-8, NFR-1,
  NFR-2, NFR-4, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-5, AD-9, SVG geometry bridge rules, fail-closed diagnostics.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt`
  - anchor validation and unresolved ref diagnostics.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
  - anchor lowering and geometry-ref resolution.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
  - `data-athena-ref` geometry bridge and duplicate-ref diagnostics.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
  - SVG-backed geometry admission and bridge coverage.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
  - package snapshot and representation proof coverage.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
  - governed representation diagnostics through the IDE path.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Focused compiler regression first failed with SVG-backed anchor `ref "svg-0001"`, proving the fallback hole, then passed after lowerer hardening.
- 2026-07-29: Focused LSP regression passed after fixing `PresentationOccurrence.toPayload()` local reference shadowing.
- 2026-07-29: Full `:ide:lsp:test` initially exposed stale M35 fixture usage of generated SVG ids; fixture was updated to explicit `data-athena-ref` geometry.
- 2026-07-29: Sequential verification passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### Completion Notes List

- SVG-backed anchor lowering now rejects generated primitive ids and resolves only explicit `data-athena-ref` geometry refs.
- Native Athena symbols still resolve authored primitive ids, preserving the source-owned native geometry path.
- Invalid SVG-backed geometry bindings publish the same compiler diagnostic contract through LSP.
- Stale M35 vendor fixture no longer depends on generated SVG primitive ids.

### Change Log

- 2026-07-29: Hardened SVG-backed anchor lowering to fail closed on unmarked/generated geometry ids.
- 2026-07-29: Added compiler and LSP regressions for invalid SVG-backed geometry binding diagnostics.
- 2026-07-29: Updated stale M35 SVG-backed fixture and LSP formatter expectation to match current M36 authority rules.

### File List

- `_bmad-output/implementation-artifacts/m36/2-3-reject-invalid-geometry-bindings.md`
- `_bmad-output/implementation-artifacts/m36/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
- `examples/m35/physical-installation-cabinet/packages/representation/com/engineeringood/m35/vendor/abb/pfea112/vendor-elements.athena`
