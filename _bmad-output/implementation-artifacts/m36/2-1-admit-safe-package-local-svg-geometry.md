---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E2.S1: Admit Safe Package-Local SVG Geometry

Status: done

## Story

As a representation-package author,
I want to reference package-local SVG as geometry,
so that Athena can use vendor artwork without giving SVG semantic authority.

**Requirements:** FR-5, FR-6.

## Acceptance Criteria

1. A package-local SVG resource is admitted only when the path stays inside the source-unit directory and resolves to a real local SVG file; absolute paths, parent traversal, symlinks, missing files, and remote-style paths fail closed.
2. M36 recognizes only `data-athena-ref` as the SVG geometry-reference extension; `data-athena-geometry-ref` and all other `data-athena-*` metadata remain rejected or forbidden.
3. Unsafe SVG content fails closed with source-spanned diagnostics: scripts, event handlers, unsupported namespaces, external/data/file/CSS URL references, DOCTYPE/entity transport, and raw markup transport.
4. Safe package-local SVG still compiles through the existing representation pipeline with deterministic geometry-only output, and SVG carries no Port, Anchor role, direction, signal, compatibility, or connection meaning.

## Tasks / Subtasks

- [x] Add failing tests for safe package-local SVG admission and legacy bridge rejection (AC: 1-4)
  - [x] Cover a valid package-local SVG resource using `data-athena-ref`.
  - [x] Cover rejected legacy `data-athena-geometry-ref`, script/event/URL/entity cases, and unsafe path admission.
  - [x] Prove the tests fail before any compiler changes.
- [x] Update SVG admission and safe parsing to use `data-athena-ref` only (AC: 1-4)
  - [x] Extend the existing compiler-owned SVG bridge helper instead of adding a second SVG pipeline.
  - [x] Keep package-local path sealing, fail-closed XML parsing, and geometry-only validation boundaries intact.
  - [x] Remove the legacy `data-athena-geometry-ref` spelling rather than aliasing it.
- [x] Update package fixtures and LSP/snapshot coverage to the new bridge spelling (AC: 1-4)
  - [x] Refresh the direct compiler tests, LSP coverage, and sample SVG fixtures that still use the old attribute.
  - [x] Keep native Symbols and SVG-backed Elements on the same representation contract.
- [x] Run story evidence gate (AC: 1-4)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. SVG is geometry only. Do not add a second authority, compatibility shim, or XML runtime model.
- The current safe SVG boundary lives in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSourceResourceSupport.kt`, `AthenaSvgGraphicBodySupport.kt`, and `AthenaSvgGraphicBodyCompiler.kt`.
- `resolvePackageLocalSvgPath` already seals package-local paths. Keep that boundary; do not widen to remote URI, registry, or absolute-path loading in this story.
- The legacy bridge spelling `data-athena-geometry-ref` is stale for M36. Replace it with `data-athena-ref` in code, tests, and sample SVG fixtures instead of keeping both.
- Keep SVG metadata rejection strict: scripts, event handlers, unsupported namespaces, external references, unsafe XML transport, and unknown `data-athena-*` attributes stay forbidden.
- Keep scope out of Port-to-Anchor binding, Layout Graph, routing, Cabinet placement, and renderer work. Those belong to later M36 stories.
- Run RED first, then sequential Gradle verification. Do not run Gradle tasks in parallel.

### Project Structure Notes

- Likely touchpoints: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`, `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`, `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`, and package SVG fixtures under `examples/m34/` and `examples/m35/`.
- Do not move work into renderer, routing, or physical-layout modules for this story.
- Keep text assets UTF-8; run the encoding audit after touching SVG or other text fixtures.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 2.1, FR-5, FR-6.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-5, FR-6, NFR-6.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md` - AD-5, SVG bridge convention, deferred remote resources.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/addendum.md` - `data-athena-ref` bridge example.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSourceResourceSupport.kt` - package-local SVG path sealing.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt` - SVG safe parsing and metadata rejection.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt` - package-local SVG compilation boundary.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt` - SVG geometry admission coverage.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt` - package snapshot proof coverage.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt` - LSP coverage for governed SVG resources.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Red check confirmed `AthenaReferencedSvgGraphicCompilerTest` fails when the old SVG bridge spelling is active.
- 2026-07-29: Restored `data-athena-ref` as the only allowed SVG bridge spelling and kept package-local geometry admission fail-closed.
- 2026-07-29: Added deterministic compiler coverage for traversal, absolute-path, remote-style, and missing-file admission failures.
- 2026-07-29: Verified `:kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest`, `:kernel:compiler:test`, and `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaRepresentationSourceLspSupportTest`.
- 2026-07-29: Verified `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` and `git diff --check`.

### Completion Notes List

- Package-local SVG admission now accepts `data-athena-ref`, rejects the stale `data-athena-geometry-ref` spelling, and keeps SVG geometry-only.
- The compiler path guard now fails closed for traversal, absolute, remote-style, and missing package-local SVG paths.
- Updated compiler, LSP, sample SVG fixtures, and package snapshot coverage to the M36 bridge spelling.
- Safe SVG parsing still fails closed on scripts, event handlers, unsupported namespaces, unsafe URLs, and raw XML transport.

### Change Log

- 2026-07-29: Implemented safe package-local SVG admission with `data-athena-ref`, removed the legacy geometry-ref spelling, and verified the compiler/LSP regression gates.

### File List

- _bmad-output/implementation-artifacts/m36/2-1-admit-safe-package-local-svg-geometry.md
- _bmad-output/implementation-artifacts/m36/sprint-status.yaml
- examples/m34/sample-project/packages/representation/athena/generated/generated-drive.svg
- examples/m34/sample-project/packages/representation/athena/vendor/vendor-drive.svg
- examples/m35/package-platform-proof/packages/representation/com/engineeringood/m35/vendor/abb/pfea112/pfea112.svg
- examples/m35/physical-installation-cabinet/packages/representation/com/engineeringood/m35/vendor/abb/pfea112/pfea112.svg
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ImporterAiBoundaryTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt
