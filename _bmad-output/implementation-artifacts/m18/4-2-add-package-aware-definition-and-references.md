---
baseline_commit: cd7c455e9f6062486d088033176d5111db2f8982
---

# Story 4.2: Add Package-Aware Definition And References

Status: done

## Story

As an IDE user,
I want go-to-definition and references to cross source-unit and package boundaries,
so that I can navigate package-aware authored meaning from the IDE.

## Acceptance Criteria

1. Given a linked reference in a compiler-owned project semantic graph snapshot, when `textDocument/definition` is requested through LSP, then the returned `Location` targets the snapshot declaration provenance, including cross-source-unit locations.
2. Given a declaration or linked reference in a compiler-owned project semantic graph snapshot, when `textDocument/references` is requested through LSP, then returned locations are derived from snapshot declaration/binding provenance and respect `ReferenceContext.includeDeclaration`.
3. Definition and references preserve stable source-unit URI and source-span provenance across source-unit/package boundaries.
4. Existing single-document definition and references behavior continues to work for non-package or project-semantic-unavailable documents.
5. The implementation does not add frontend-local import resolution, Tree-sitter semantic navigation, canvas logic, desktop-viewer logic, raw filesystem import authority, or Kotlin Compose UI work.
6. Verification runs sequentially through focused LSP navigation tests, `:kernel:compiler:test`, `:ide:lsp:test`, and encoding audit.

## Tasks / Subtasks

- [x] Add failing LSP coverage for package-aware definition and references (AC: 1-4)
  - [x] Add or extend an `ide/lsp` test that opens a governed repository document containing a package-aware linked reference to a sibling source unit.
  - [x] Assert `textDocument/definition` on the reference returns the provider source unit URI and declaration span.
  - [x] Assert `textDocument/references` on the reference returns binding locations from snapshot records and includes the declaration only when requested.
  - [x] Assert existing document-local navigation tests still pass for non-package sources.
- [x] Preserve linked project semantic snapshot navigation records in LSP state (AC: 1-3)
  - [x] Extend the Story 4.1 project semantic snapshot path to retain the linked snapshot data needed for navigation: source-unit URI map, declarations, bindings, and `graphId`.
  - [x] Keep navigation records compiler-owned; do not rebuild imports or symbols in frontend code or from Tree-sitter.
  - [x] Ensure dirty-buffer overlay remains active for the current document while sibling source units are read from governed package roots.
- [x] Project snapshot definition and references through existing LSP methods (AC: 1-5)
  - [x] In `AthenaLanguageFeatures.definition(...)`, first resolve package-aware binding/declaration hits from the tracked project semantic snapshot.
  - [x] In `AthenaLanguageFeatures.references(...)`, first resolve package-aware binding/declaration hits from the tracked project semantic snapshot.
  - [x] Fall back to the existing `AthenaNavigationIndex` document-local behavior when no package-aware navigation hit exists.
  - [x] Convert declaration and binding `SourceSpan` values to LSP `Location` values using the source-unit URI map.
- [x] Preserve authority boundaries and IDE conventions (AC: 3-5)
  - [x] Keep `AthenaTextDocumentService.definition` and `references` as normal LSP request handlers.
  - [x] Keep Theia/VS Code-like behavior: return standard LSP `Location` results, not custom commands or frontend-only payloads.
  - [x] Keep `apps/desktop-viewer`, Kotlin Compose, canvas, and Tree-sitter semantic behavior untouched.
- [x] Run scoped verification sequentially (AC: 6)
  - [x] Run focused Story 4.2 LSP navigation tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Notes

- Current LSP navigation entry points are in `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`.
  - `definition(uri, position)` currently uses `AthenaNavigationIndex.definition(offset)`.
  - `references(params)` currently uses `AthenaNavigationIndex.references(offset, includeDeclaration)`.
  - `AthenaNavigationIndex` is document-local and AST-backed; it should remain the fallback for non-package documents and local-only behavior.
- Story 4.1 added project semantic tracking to `AthenaTrackedDocument`:
  - `projectSemanticGraphId`
  - `projectSemanticSourceUnitId`
  - `projectSemanticSourceUnitUris`
  - `projectSemanticDiagnostics`
- Story 4.2 likely needs an internal LSP-side navigation projection record derived from the linked compiler snapshot. A minimal shape is enough:
  - graph id
  - current source unit id
  - source-unit URI map
  - declarations with `declarationId`, `sourceUnitId`, `authoredSpan`
  - bindings with `sourceUnitId`, `referenceSpan`, `resolvedDeclarationId`
- Compiler-owned records already exist in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`:
  - `ProjectSemanticDeclaration`
  - `ProjectSemanticBinding`
  - `ProjectSemanticSourceUnit`
- Existing compiler tests prove the linking semantics Story 4.2 should project rather than recompute:
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinkerTest.kt`
  - Especially tests for cross-source-unit and cross-package binding.
- LSP tests should follow existing request style in:
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceNavigationParityTest.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- Definition behavior should be snapshot-first:
  - If cursor offset is inside a binding `referenceSpan` for the current source unit, return the resolved declaration location.
  - If no package-aware hit exists, use existing `AthenaNavigationIndex`.
- References behavior should be snapshot-first:
  - If cursor offset is inside a binding `referenceSpan`, use that binding's `resolvedDeclarationId`.
  - If cursor offset is inside a declaration span, use that declaration's `declarationId`.
  - Return all binding reference spans whose `resolvedDeclarationId` matches.
  - Include the declaration location only when `ReferenceContext.includeDeclaration` is true.
  - If no package-aware hit exists, use existing `AthenaNavigationIndex`.
- URI behavior should reuse the Story 4.1 source-unit URI map. If a source-unit URI cannot be resolved, skip that cross-source result instead of fabricating a raw path.
- Keep all file references in artifacts relative to this implementation folder or repo root.
- Do not touch `apps/desktop-viewer`, Kotlin Compose UI, frontend semantic resolution, Tree-sitter semantic behavior, or canvas code for this story.

### Previous Story Intelligence

- Story 4.1 built package-aware project semantic snapshots in LSP from governed source units and overlaid the dirty current buffer.
- Story 4.1 fixed the current-source-unit identity bug for multi-source snapshots; Story 4.2 must not reintroduce `singleOrNull()` or content-based matching when identifying the current source unit.
- Story 4.1 added source-unit URI mapping for cross-source diagnostics. Reuse the same authority for definition/reference locations.
- Story 4.1 skips project semantic behavior unless the main compiler result is a compilation success; keep that safety so parse failures do not produce stale or duplicate project navigation.
- Story 3.2 and Story 3.3 established `ProjectSemanticBinding` records for cross-source-unit and cross-package references. LSP should project these records, not link references again.
- Recent verification pattern: focused test first, then `:kernel:compiler:test`, then `:ide:lsp:test`, then encoding audit. Gradle commands must run sequentially.

### Project Structure Notes

- Expected implementation modules:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Compiler contracts should remain unchanged unless an existing public model is insufficient for LSP projection.
- Prefer small cohesive internal data classes near the LSP projection code if needed; do not split every tiny DTO into its own Kotlin file.

### References

- [Source: `epics.md` - Epic 4, Story 4.2]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-5, FR-8, SM-4, NFR-4]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 8-10]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-6, AD-8, AD-12, AD-14]
- [Source: `4-1-project-package-aware-diagnostics-through-lsp.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareNavigationTest` failed before package-aware definition projected linked snapshot declarations.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareNavigationTest` passed after preserving linked snapshot navigation records in LSP state.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - Story 4.2 prepared for compiler-owned package-aware LSP definition/reference projection.
- Added package-aware LSP navigation projection from compiler-owned project semantic declarations and bindings.
- Preserved linked snapshot navigation records alongside Story 4.1 diagnostics state, including graph id, current source unit, source-unit URI map, declarations, and bindings.
- Implemented snapshot-first `definition` and `references` behavior with fallback to the existing document-local `AthenaNavigationIndex`.
- Added focused LSP coverage for cross-source-unit definition and references through a governed repository fixture.
- Kept Tree-sitter, frontend-local semantic resolution, canvas, desktop-viewer, and Kotlin Compose out of scope.

### File List

- `_bmad-output/implementation-artifacts/m18/4-2-add-package-aware-definition-and-references.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareNavigationTest.kt`

## Change Log

- 2026-07-15: Created Story 4.2 from Epic 4, PRD FR-8/SM-4, architecture AD-6/AD-8/AD-14, and Story 4.1 implementation intelligence.
- 2026-07-15: Implemented package-aware definition and references through compiler-owned linked snapshot projection.
