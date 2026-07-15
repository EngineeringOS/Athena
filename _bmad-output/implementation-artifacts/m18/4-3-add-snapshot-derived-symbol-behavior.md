---
baseline_commit: c2a30d4c2629c84bcf307c9b140b7fc150e8a2e6
---

# Story 4.3: Add Snapshot-Derived Symbol Behavior

Status: done

## Story

As an IDE user,
I want document symbols, and explicitly promoted workspace symbols if included, to reflect package-aware authored meaning,
so that source units and imported declarations are discoverable in familiar IDE flows.

## Acceptance Criteria

1. Given a package-aware source unit with a compiler-owned project semantic snapshot, when document symbols are requested through LSP, then symbol results reflect the authored package-aware document structure and declaration provenance.
2. Given package-aware snapshot declarations for the current source unit, document-symbol children derive from snapshot declaration records when available.
3. Given a non-package or project-semantic-unavailable document, existing document-symbol behavior remains unchanged.
4. Workspace symbols remain deferred unless explicitly promoted; if deferred, no placeholder workspace-symbol API or frontend semantic search is added.
5. The implementation does not add frontend-local import resolution, Tree-sitter semantic symbols, canvas logic, desktop-viewer logic, raw filesystem import authority, or Kotlin Compose UI work.
6. Verification runs sequentially through focused LSP symbol tests, `:kernel:compiler:test`, `:ide:lsp:test`, and encoding audit.

## Tasks / Subtasks

- [x] Add failing LSP coverage for package-aware document symbols (AC: 1-4)
  - [x] Add or extend an `ide/lsp` test that opens a package-aware governed source unit.
  - [x] Assert package-aware document symbols expose a package-level outline root with current-document declarations below it.
  - [x] Assert sibling/imported declarations do not leak into the current document outline.
  - [x] Assert existing non-package document symbol behavior remains unchanged.
- [x] Derive package-aware document symbols from compiler-owned state (AC: 1-5)
  - [x] Reuse tracked project semantic navigation/snapshot records from Stories 4.1 and 4.2.
  - [x] Map current-source-unit declarations to LSP `DocumentSymbol` children using snapshot declaration provenance.
  - [x] Add a package-level `DocumentSymbol` root for package-aware documents.
  - [x] Fall back to existing AST-backed document symbols when no package-aware snapshot exists.
- [x] Preserve scope boundaries (AC: 4-5)
  - [x] Do not add workspace-symbol behavior unless explicitly required during implementation.
  - [x] Keep Tree-sitter syntax UX out of semantic symbol generation.
  - [x] Keep `apps/desktop-viewer`, Kotlin Compose, canvas, and frontend-local semantic resolution untouched.
- [x] Run scoped verification sequentially (AC: 6)
  - [x] Run focused Story 4.3 LSP symbol tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Notes

- Current document symbols are implemented in `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`.
  - `documentSymbols(params)` currently returns a single system `DocumentSymbol` built from the current AST declarations.
  - `Declaration.toDocumentSymbol()` maps devices, ports, and connections to LSP symbol kinds.
- Story 4.2 added `AthenaProjectSemanticNavigationSnapshot` with `graphId`, current source unit id, source-unit URI map, declarations, and bindings.
- For package-aware documents, use snapshot declarations for current-source-unit declaration children when available.
- Keep sibling and imported declarations out of the current document outline. Workspace symbol search is explicitly deferred by sprint notes unless promoted later.
- Package root guidance:
  - Use LSP `SymbolKind.Package` for the package root.
  - Use the authored package declaration text as the symbol name, e.g. `com.root`.
  - Keep Theia/VS Code-like outline behavior: standard `textDocument/documentSymbol`, no custom frontend command.
- Non-package documents must keep existing behavior exactly: a system root with declaration children.
- Do not touch `apps/desktop-viewer`, Kotlin Compose UI, frontend semantic resolution, Tree-sitter semantic behavior, or canvas code for this story.

### Previous Story Intelligence

- Story 4.1 established project semantic snapshot tracking and source-unit URI mapping in LSP.
- Story 4.2 preserved linked declarations and bindings in LSP state and added snapshot-first definition/references with fallback to `AthenaNavigationIndex`.
- Reuse those records rather than rescanning source units or resolving imports in the IDE.
- Recent verification pattern: focused test first, then `:kernel:compiler:test`, then `:ide:lsp:test`, then encoding audit. Gradle commands must run sequentially.

### Project Structure Notes

- Expected implementation modules:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Keep Kotlin support types cohesive in the existing LSP file unless a distinct role grows large enough to justify splitting.

### References

- [Source: `epics.md` - Epic 4, Story 4.3]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-8, SM-4]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 8-10]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-8, AD-12, AD-14]
- [Source: `4-2-add-package-aware-definition-and-references.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareSymbolsTest` failed before package-aware document symbols exposed a package root.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareSymbolsTest` passed after adding the package-aware outline root.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - Story 4.3 prepared for package-aware document-symbol projection.
- Added a package-level `DocumentSymbol` root for package-aware documents when project semantic navigation state is available.
- Preserved existing system/declaration outline children under the package root, including connection symbols that are still AST-backed.
- Added focused LSP coverage that verifies package-aware outline shape and prevents sibling/provider declarations from leaking into the current document outline.
- Kept workspace symbols deferred and did not add frontend-local semantic search.
- Kept Tree-sitter, canvas, desktop-viewer, and Kotlin Compose out of scope.

### File List

- `_bmad-output/implementation-artifacts/m18/4-3-add-snapshot-derived-symbol-behavior.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareSymbolsTest.kt`

## Change Log

- 2026-07-15: Created Story 4.3 from Epic 4, PRD FR-8/SM-4, architecture AD-8/AD-14, and Story 4.2 implementation intelligence.
- 2026-07-15: Implemented package-aware document-symbol outline root and focused LSP symbol coverage.
