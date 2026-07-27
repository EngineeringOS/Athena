---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 1.1: Enforce Governed Package Hierarchy

Status: review

## Story

As a package author,
I want Athena package declarations and source paths validated by one repository contract,
so that packages remain predictable without turning filesystem namespaces into product identity.

## Acceptance Criteria

1. Given an `athena.yaml` manifest with governed source roots and package intent, when repository validation runs, then one extended `RepositoryManifest` and one loader provide typed roots to compiler, package runtime, and LSP, and duplicate YAML/line scanners are removed from the active path.
2. Given governed Athena source under a declared root, when its package namespace and normalized parent path differ by segment, case, default package, or NFC form, then admission fails with a stable machine-readable source/path diagnostic.
3. Given the same package/path mismatch, when LSP diagnostics are published, then LSP publishes the same stable diagnostic and excludes generated snapshots and caches from validation.
4. Given a valid namespace and path, when semantic/package identity is inspected, then namespace remains transport identity only and cannot supply manufacturer, article, revision, rating, or lifecycle facts.
5. Given all previous ACs are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, three-layer review, and polish/purge are recorded.

## Tasks / Subtasks

- [x] Add RED tests for repository package/path validation (AC: 1, 2, 4)
  - [x] Cover valid source-root-relative package/path parity.
  - [x] Cover default package rejection.
  - [x] Cover segment mismatch.
  - [x] Cover uppercase package segment rejection.
  - [x] Cover case-only path mismatch and generated/cache exclusion.
  - [x] Cover package namespace not exposing product identity fields.
- [x] Implement one governed package hierarchy validator through the repository contract loader (AC: 1, 2, 4)
  - [x] Extend existing repository manifest/root models only if needed.
  - [x] Reuse parsed `.athena` package facts; do not add regex package scanners.
  - [x] Emit stable diagnostics with code, message, severity, source path, and source span where available.
  - [x] Exclude generated snapshots, caches, build output, and lock/proof artifacts from validation.
- [x] Publish the same diagnostics through LSP (AC: 3)
  - [x] Reuse repository loader/validator output; do not create an LSP-only scanner.
  - [x] Add or update LSP diagnostic tests for package/path mismatches.
- [x] Migrate the M35 sample hierarchy seed if sample files exist in this story scope (AC: 2, 3)
  - [x] Keep source paths Java-style under governed roots.
  - [x] Do not create resource/SVG/physical model syntax in this story.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched and adjacent paths for duplicate YAML scanners, XML/package compatibility names, generated evidence, and stale tests.
  - [x] Run sequential verification commands and record RED/GREEN evidence.
  - [x] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 1.1 is only the package hierarchy and repository-loader authority story. It implements FR-1..FR-4, FR-11..FR-12, FR-41..FR-42. It must not implement package-local resources, lock v2, SVG geometry hints, physical installation syntax, cabinet composition, routing, or trace. Those belong to later M35 stories.

### Architecture Requirements

- `RepositoryManifest` stays the one authored descriptor owner. Do not introduce `package.athena`, another manifest format, or runtime semantic interpretation of descriptor metadata.
- One repository loader/parser must feed compiler, package runtime, and LSP. Independent YAML line scanners are an anti-pattern for this story.
- Governed `.athena` files under source roots must declare a non-empty package. The lowercase namespace segments must exactly equal the normalized source-root-relative parent directory.
- Namespace is transport identity only. It must not become manufacturer, article number, revision, lifecycle, rating, datasheet, or engineering product identity.
- Generated snapshots and caches must not participate in package/path validation.
- ANTLR4 remains semantic parser authority. Tree-sitter/editor grammar work is not owned by this story unless an existing test proves diagnostics depend on it.

### Likely Code Areas

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractValidationModel.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt` only if existing package parsing cannot expose source spans.

Use CodeGraph before reading or editing source files because the repo is indexed.

### Testing Requirements

- Follow RED/GREEN. Write failing tests before production code.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification for this story:
  - targeted compiler repository tests;
  - targeted LSP diagnostics tests;
  - encoding audit after doc/source text edits;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 1, Story 1.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-1..FR-4, FR-11..FR-12, FR-41..FR-42.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - Package Hierarchy Rule.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-7, AD-8, AD-15, AD-17.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoaderTest" --tests "com.engineeringood.athena.compiler.repository.AthenaRepositoryLockMaterializerTest"` initially exposed missing governed package/path validation and lock materialization gaps.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.repository.AthenaRepositoryContractLoaderTest" --tests "com.engineeringood.athena.compiler.repository.AthenaRepositoryLockMaterializerTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM32SampleProjectCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM34ProfessionalCabinetCompositionTest" --tests "com.engineeringood.athena.compiler.PresentationModelDeriverTest"` passed after purging the stale M25 representation fallback and fixing M34 Java-style repo-root discovery.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- GREEN: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- GREEN: `git diff --check` passed; Git emitted line-ending warnings only.

### Completion Notes List

- Added governed package hierarchy enforcement through the repository contract loader: source-root-relative parent path must match the declared `.athena` package, default packages are rejected, uppercase segments are rejected, generated/cache paths are excluded, and diagnostics use stable codes.
- Kept package namespace as transport identity only. Repository/package models do not expose manufacturer, article, revision, rating, lifecycle, or datasheet facts.
- Reused repository validation output in LSP diagnostics instead of adding an LSP-only scanner.
- Migrated governed examples/tests to Java-style source paths, including the stale M17/M18 flat fixtures found by full regression.
- Purged the active `athena-industrial-control-v0` fallback from `PresentationModelDeriver`; M34 professional drawing now proves package/material authority through `graphicOccurrences`.
- AC-to-evidence: AC1 = repository loader/model tests and no duplicate active loader path; AC2 = package/path mismatch tests and migrated examples; AC3 = LSP diagnostics tests; AC4 = package identity model assertions; AC5 = full compiler, full LSP, encoding audit, diff check, and this record.
- Three-layer review: Contract layer checks one repository loader owns admission; semantic layer checks package namespace is not product identity; product layer checks stale fallback rendering authority is removed from active M34 path.

### File List

- `_bmad-output/implementation-artifacts/m35/1-1-enforce-governed-package-hierarchy.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractValidationModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM17ParserParityProofTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalCabinetCompositionTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspTestFixtures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM18RepositoryProofCorpusLspTest.kt`
- `ide/theia-backend/src/node/athena-repository-bootstrapper.ts`
- `ide/theia-backend/scripts/athena-repository-bootstrapper.test.mjs`
- `examples/m4/open-repository-proof/src/com/engineeringood/factoryline/factoryline.athena`
- `examples/m10/reasoning-proof/baseline/src/com/engineeringood/m10/proof/factoryline.athena`
- `examples/m10/reasoning-proof/current/src/com/engineeringood/m10/proof/factoryline.athena`
- `examples/m11/dense-electrical-proof/src/com/engineeringood/assemblyline/assemblyline.athena`
- `examples/m17/repository-parity-proof/src/com/engineeringood/m17/parity/parity-repo.athena`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/*.athena`
- `examples/m18/repository-proof/valid-workspace/vendor/controls/src/com/engineeringood/m18/vendor/controls/vendor-controls.athena`
- `examples/m18/repository-proof/graph-invalid/src/com/engineeringood/m18/graphinvalid/root.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/*.athena`
- `examples/m23/sample-project/src/com/engineeringood/m23/sample/01-layout-hints.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/*.athena`
- `examples/m26/sample-project/src/com/engineeringood/m26/sample/*.athena`
- `examples/m27/sample-project/src/com/engineeringood/m27/sample/*.athena`
- `examples/m28/sample-project/src/com/engineeringood/m28/sample/*.athena`
- `examples/m29/sample-project/src/com/engineeringood/m29/sample/*.athena`
- `examples/m30/sample-project/src/com/engineeringood/m30/sample/01-rolling-shutter-control-source.athena`
- `examples/m31/sample-project/src/com/engineeringood/m31/sample/01-governed-authoring-customer-source.athena`
- `examples/m32/sample-project/src/com/engineeringood/m32/sample/01-package-platform-demo.athena`
- `examples/m34/professional-control-drawing/src/com/engineeringood/m34/professional/01-control-drawing.athena`
- `docs/usages/m17-proof-usage.md`
- `examples/README.md`
- `examples/m17/README.md`
- `_bmad-output/implementation-artifacts/m17/5-1-publish-a-checked-in-parser-parity-corpus.md`
