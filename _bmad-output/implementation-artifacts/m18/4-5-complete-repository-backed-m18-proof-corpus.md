---
baseline_commit: 32edf62d9de6f88e3b9a3936fc8e07ecde093c3f
---

# Story 4.5: Complete Repository-Backed M18 Proof Corpus

Status: done

## Story

As a maintainer,
I want one executable M18 proof corpus accumulated across all epics,
so that M18 closeout depends on evidence rather than prose.

## Acceptance Criteria

1. `examples/m18/` includes repository-backed proof fixtures for single-package success, cross-package success, invalid import, unresolved symbol, graph-invalid or cycle behavior, and vendor/governed package availability.
2. Compiler tests execute the repository-backed fixtures through governed repository/package graph state and package-aware semantic graph behavior.
3. LSP tests cover the repository-backed package-aware behavior needed for diagnostics/navigation closeout.
4. Tree-sitter tests continue to cover package/import syntax fixtures as syntax UX only and do not imply semantic success.
5. Vendor/governed package availability remains local and admitted through the governed repository graph; no remote registry, marketplace, publish, multi-root, frontend-owned resolution, desktop-viewer, or Kotlin Compose behavior is introduced.
6. Verification runs sequentially through focused M18 proof tests, `:kernel:compiler:test`, `:ide:lsp:test`, relevant Tree-sitter test if changed, and encoding audit.

## Tasks / Subtasks

- [x] Add failing compiler coverage for checked-in repository-backed M18 fixtures (AC: 1-2, 5)
  - [x] Assert the corpus inventory contains all required fixture categories.
  - [x] Assert single-package and cross-package fixtures build/link through governed repository state.
  - [x] Assert invalid import and unresolved symbol fixtures produce stable package-aware diagnostics.
  - [x] Assert graph-invalid or cycle fixture behavior is represented by a stable diagnostic.
  - [x] Assert vendor/governed package availability is a local admitted package, not registry/marketplace metadata.
- [x] Add or update `examples/m18/` corpus files (AC: 1, 5)
  - [x] Add a repository-backed fixture folder with `athena.yaml`, `athena.lock`, root sources, and local vendor package sources.
  - [x] Document the fixture inventory and non-goals with relative paths only.
  - [x] Preserve existing syntax-proof and linking-lowering-proof fixtures.
- [x] Add LSP closeout coverage where needed (AC: 3, 5)
  - [x] Reuse existing LSP package-aware test helpers or checked-in corpus files.
  - [x] Cover repository-backed diagnostics/navigation behavior without frontend-local resolution.
- [x] Preserve Tree-sitter boundary (AC: 4-5)
  - [x] Keep Tree-sitter assertions syntax-only.
  - [x] Do not add semantic package resolution to Tree-sitter or frontend code.
- [x] Run scoped verification sequentially (AC: 6)
  - [x] Run focused compiler M18 proof corpus test.
  - [x] Run focused LSP M18 proof test if added/changed.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`.
  - [x] Run relevant Tree-sitter test if Tree-sitter files or corpus syntax fixtures changed.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Notes

- Existing M18 corpus:
  - `examples/m18/syntax-proof/` covers package/import syntax fixtures for compiler parser and Tree-sitter syntax UX.
  - `examples/m18/linking-lowering-proof/` covers manual semantic linking/lowering fixtures from Epic 3.
- Existing tests:
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/AthenaM18LinkingLoweringProofTest.kt`
  - `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareNavigationTest.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- Story 4.5 should close the evidence gap by checking in a governed repository-shaped corpus, not by replacing earlier unit fixtures.
- Keep all artifact references relative. The user explicitly corrected absolute path references in `epics.md`; do not add absolute paths to story docs.
- Do not touch `apps/desktop-viewer`, Kotlin Compose, renderer/canvas code, or frontend semantic resolution.

### Previous Story Intelligence

- Story 4.1 proved package-aware LSP diagnostics.
- Story 4.2 proved package-aware LSP definition/references.
- Story 4.3 proved package-aware LSP document symbols.
- Story 4.4 proved Theia frontend selection/reveal handoff through existing graph workbench surfaces.
- Story 4.5 should focus on executable closeout evidence, not new feature behavior.

### Project Structure Notes

- Expected implementation/test modules:
  - `examples/m18/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/tree-sitter-athena/scripts/` only if syntax-proof test wiring changes

### References

- [Source: `epics.md` - Epic 4, Story 4.5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-10, SM-6]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 9-12]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-10, AD-11, AD-15]
- [Source: `4-4-use-existing-canvas-reveal-from-package-aware-navigation.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.AthenaM18RepositoryProofCorpusTest` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM18RepositoryProofCorpusLspTest` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- Tree-sitter verification was not run because no Tree-sitter files or syntax-proof fixtures changed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - Story 4.5 prepared for repository-backed M18 proof corpus closeout.
- Added a checked-in repository-backed M18 corpus with root, local vendor/governed package, invalid import, unresolved symbol, and graph-invalid fixtures.
- Added compiler closeout coverage that exercises governed repository publication, package graph state, import resolution, declaration indexing, reference linking, and stable diagnostics from the corpus.
- Added LSP closeout coverage proving repository-backed package-aware diagnostics are published through the IDE server boundary without frontend-owned semantic resolution.
- Preserved the Tree-sitter boundary as syntax-only and avoided desktop-viewer, Kotlin Compose, registry, marketplace, publish, multi-root, and frontend semantic-resolution behavior.

### File List

- `_bmad-output/implementation-artifacts/m18/4-5-complete-repository-backed-m18-proof-corpus.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `examples/m18/README.md`
- `examples/m18/repository-proof/README.md`
- `examples/m18/repository-proof/graph-invalid/athena.lock`
- `examples/m18/repository-proof/graph-invalid/athena.yaml`
- `examples/m18/repository-proof/graph-invalid/src/root.athena`
- `examples/m18/repository-proof/valid-workspace/athena.lock`
- `examples/m18/repository-proof/valid-workspace/athena.yaml`
- `examples/m18/repository-proof/valid-workspace/src/cross-package-consumer.athena`
- `examples/m18/repository-proof/valid-workspace/src/invalid-import.athena`
- `examples/m18/repository-proof/valid-workspace/src/single-package-success.athena`
- `examples/m18/repository-proof/valid-workspace/src/unresolved-symbol.athena`
- `examples/m18/repository-proof/valid-workspace/vendor/controls/athena.lock`
- `examples/m18/repository-proof/valid-workspace/vendor/controls/athena.yaml`
- `examples/m18/repository-proof/valid-workspace/vendor/controls/src/vendor-controls.athena`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM18RepositoryProofCorpusLspTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/AthenaM18RepositoryProofCorpusTest.kt`

## Change Log

- 2026-07-15: Created Story 4.5 from Epic 4, PRD FR-10/SM-6, architecture AD-10/AD-11/AD-15, and M18 proof-corpus inventory.
- 2026-07-15: Added repository-backed M18 proof corpus, compiler/LSP closeout coverage, and local-only scope documentation.
