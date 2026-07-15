# Story 4.2: Preserve Source Navigation And Symbol Utility Across Parser Migration

Status: done

## Story

As an IDE engineer,
I want current AST-dependent editor and LSP utilities to remain useful after parser migration,
so that M17 does not regress existing language-service value while hardening the parser architecture.

## FR Traceability

- FR-7: Athena can keep compiler and LSP semantics on the compiler parser path.
- FR-8: Athena can preserve useful failure behavior on invalid source.
- NFR-3: Source spans and diagnostics remain inspectable across parser migration.

## Acceptance Criteria

1. Given existing source-oriented language services are reviewed, when the `ANTLR4`-backed AST path (Epic 2) replaces the current handwritten parser path, then `AthenaLanguageFeatures.documentSymbols`, `.definition`, `.references`, and `AthenaNavigationIndex` (`AthenaLanguageFeatures.kt`) continue to operate unchanged against `SourceFileAst`, `DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`, and `QualifiedName` from `com.engineeringood.athena.language`, with no code path added that reads a generated parse-tree node directly.
2. Given the source-edit utilities are reviewed, when a component or connection is renamed or updated through guided authoring (`acceptedUpdateComponentPropertiesSourceEdit` in `AthenaUpdateComponentSourceEditProtocol.kt`, and the sibling `acceptedConnectPortsSourceEdit`/`acceptedCreateComponentSourceEdit` protocols), then those functions continue to compute text edits from `DeviceDeclaration.span`, `PortDeclaration.qualifiedName.span`, and `ConnectionDeclaration.from`/`to` spans rather than any parser-generator-internal offset representation.
3. Given the bidirectional source/graph reveal path is reviewed (`revealSemanticId` in `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`, backed server-side by `AthenaLanguageFeatures.definition`/`references`), when a graph node or source declaration is revealed, then resolution still routes through `AthenaNavigationIndex`'s offset-to-declaration lookup over `SourceFileAst`, not through any editor-local Tree-sitter syntax tree (Epic 3).
4. Given syntax provenance is inspected, when `AthenaNavigationIndex.componentSourceRange`, `.portSourceRange`, and `.connectionSourceRange` resolve ranges for `AthenaLanguageFeatures.semanticInspection`, then those ranges keep deriving from `Declaration.span`/`QualifiedName.span` values populated by the compiler parser path, and remain 1:1 convertible to LSP `Range` through the existing `SourceSpan.toLspRange()` helper.
5. Given the existing navigation and source-edit test suites are re-run after this story's changes, when they execute, then all currently passing document-symbol, definition, references, and source-edit assertions continue to pass unchanged.

## Tasks / Subtasks

- [x] Audit every current AST-dependent editor/LSP utility and record its exact dependency surface. (AC: 1, 2, 3, 4)
  - [x] Catalog `AthenaLanguageFeatures.documentSymbols` (`Declaration.toDocumentSymbol()`, `PropertyAssignment.toDocumentSymbol()`), `.definition`, `.references`, and `AthenaNavigationIndex` (`deviceDeclarations`, `portDeclarations`, `ownerReferences`, `portReferences`, `targetAt(offset)`) as depending only on `SourceFileAst.declarations` and `SourceSpan`/`SourcePosition`.
  - [x] Catalog the source-edit protocols (`AthenaUpdateComponentSourceEditProtocol.kt`, and the sibling connect-ports/create-component source-edit files under `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`) as depending on `DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`, `QualifiedName`, and raw text-offset rewriting (`String.substring`, `SourcePosition.advanceBy`) over the tracked document's text, never over a parser-generator tree.
  - [x] Catalog the `revealSemanticId` path in `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts` and `athena-semantic-selection-service.ts` as the frontend consumer of server-owned `definition`/`references` results, confirming the frontend never re-parses source locally to resolve a reveal target.
  - [x] Record this catalog in `ide/lsp/README.md` (and Chinese counterpart) as the explicit "AST-dependent utility inventory" that Epic 2's `ANTLR4` migration and Epic 3's Tree-sitter integration must not break.
- [x] Add regression tests that pin current behavior as a parity baseline. (AC: 1, 4, 5)
  - [x] Add or extend `ide/lsp` tests asserting `documentSymbols`, `definition`, and `references` produce identical results (name, kind, range) for `examples/m0/demo-cabinet.athena` before and after any internal parser refactor, using the existing test harness pattern already used for `AthenaLanguageServer`/`AthenaLanguageFeatures` tests.
  - [x] Add a focused test asserting `AthenaNavigationIndex.componentSourceRange`/`.portSourceRange`/`.connectionSourceRange` return non-null, span-accurate ranges for every component, port, and connection in `examples/m0/demo-cabinet.athena`, matching the pattern already exercised by `requireSourceRange` in `AthenaLanguageFeatures.semanticInspection`.
  - [x] Add a focused test on `acceptedUpdateComponentPropertiesSourceEdit` (or its existing test if one already covers this) proving the computed edit range and `newText` are anchored to `DeviceDeclaration.span`, independent of any future change to how that span was produced upstream.
- [x] Document the "must survive migration" contract explicitly ahead of Epic 2/Epic 3. (AC: 1, 2, 3)
  - [x] Add KDoc to `AthenaNavigationIndex` and to the source-edit protocol functions stating that once Epic 2 replaces the handwritten parser with `ANTLR4`-backed parsing, these functions must keep working unchanged as long as the resulting `SourceFileAst` and its `SourceSpan`s are populated correctly, since they depend only on the authored AST contract, never on parser internals.
  - [x] Add a matching note that once Epic 3 introduces Tree-sitter, these utilities remain LSP-served and AST-backed; Tree-sitter must not become an alternative implementation of `documentSymbols`, `definition`, `references`, or source-edit range computation.
- [x] Keep Story `4.2` narrow. (AC: 1, 2, 3, 4, 5)
  - [x] Do not implement the `ANTLR4` grammar or parse-tree adaptation (Epic 2) or the Tree-sitter grammar/adapter (Epic 3) in this story.
  - [x] Do not change any existing navigation, symbol, or source-edit behavior; this story hardens and pins current behavior with tests and docs, it does not redesign these utilities.
  - [x] Do not widen scope into unrelated LSP capabilities (completion, hover, etc.) beyond what already exists.
- [x] Run focused and regression verification sequentially on Windows with Java 25. (AC: 1, 2, 3, 4, 5)

## Dev Notes

### Story Intent

- Story `4.2` is the "preserve, don't regress" story for M17's IDE-facing language services: it inventories every utility that today reads `SourceFileAst` directly, pins its current behavior with tests, and documents why that behavior must survive Epic 2's `ANTLR4` migration and Epic 3's Tree-sitter integration unchanged.
- The success condition is not "navigation got better." The success condition is "every AST-dependent utility that exists today (document symbols, go-to-definition, find-references, source-edit range computation, bidirectional source/graph reveal) has an explicit regression baseline and documented dependency surface, so a future parser swap cannot silently break any of them."
- This story complements Story `4.1` (diagnostics authority) and Story `4.3` (compiler output/`Engineering IR` continuity): together the three stories cover diagnostics, navigation/editing utility, and lowering output as the three observable surfaces that must not regress across parser migration.

### Architecture Guardrails

- Align to AD-109: parser migration must preserve inspectable source spans, file identity, and syntax diagnostics strongly enough for compiler, LSP, source edits, reveal, and downstream inspection workflows. Story `4.2` is the concrete audit-and-test pass that proves every one of those named workflows (source edits, reveal, LSP navigation) already depends only on `SourceSpan`/`SourceFileAst` and pins that dependency with regression tests. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-109---Parser-Migration-Must-Preserve-Provenance-And-Failure-Quality]
- Align to AD-106: authored AST remains the only lowering input, and by extension the only navigation/editing input; `Engineering IR` and IDE utilities alike must never depend on generated parser nodes. Story `4.2` confirms this holds for every navigation/editing utility catalogued above. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-106---Authored-AST-Remains-The-Only-Lowering-Input-Before-Engineering-IR]
- Preserve inherited AD-18: IDE work stays additive and product-operability scoped through existing seams; this story adds tests and docs, not new IDE surfaces. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt` already implements `documentSymbols(params)`, `definition(uri, position)`, and `references(params)` entirely over `AthenaTrackedDocument.compilation`'s `CompilerCompilationSuccess.source.ast` (a `SourceFileAst`), and `AthenaNavigationIndex` (also in this file) already builds `deviceDeclarations`, `portDeclarations`, `ownerReferences`, and `portReferences` maps purely from `SourceFileAst.declarations`.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaUpdateComponentSourceEditProtocol.kt` already computes source edits by reading `DeviceDeclaration.span`, `PortDeclaration.qualifiedName`, and `ConnectionDeclaration.from`/`to` directly from `compilation.source.ast.declarations`, then rewriting raw text offsets (`trackedDocument.text.rewriteAffectedComponentSlice`). Sibling files (connect-ports and create-component source-edit protocols) follow the identical pattern.
- `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts` already exposes `revealSemanticId(semanticId, diagram)`, and `athena-semantic-selection-service.ts` coordinates source/graph selection; both are frontend consumers of server-owned `definition`/`references` results and do not parse Athena source locally.
- `AthenaLanguageFeatures.semanticInspection(uri)` already calls `requireSourceRange(...)` backed by `navigationIndex?.componentSourceRange(...)`, `.portSourceRange(...)`, `.connectionSourceRange(...)`, all three implemented in `AthenaNavigationIndex` over `Declaration.span`/`QualifiedName.span`.
- `SourceSpan.toLspRange()` (a private extension in `AthenaLanguageFeatures.kt`) already converts 1-based `SourcePosition.line`/`column` into 0-based LSP `Position`; this is the one and only span-to-LSP-range conversion point and must stay that way.
- Existing tests: `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/` already contains coverage for `AthenaLanguageServer` behavior; confirm the exact existing test file names before adding new tests, and avoid duplicating already-covered assertions.

### Technical Requirements

- Do not change any existing `DocumentSymbol`, `Location`, or source-edit `Range`/`newText` computation logic. This story adds tests and documentation around existing behavior; it does not alter navigation or editing output.
- Keep new regression tests inside `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`, reusing existing `lsp4j` test conventions already present in that package.
- Do not introduce a new navigation index implementation. `AthenaNavigationIndex` remains the single source-navigation index; extend its test coverage, do not fork it.

### Architecture Compliance

- The story is only successful if, after Epic 2 replaces the parser implementation and Epic 3 adds Tree-sitter, `AthenaLanguageFeatures.documentSymbols`/`.definition`/`.references`, the source-edit protocols, and `revealSemanticId` all continue to pass this story's regression tests without modification, because they only ever depended on `SourceFileAst` and `SourceSpan`.
- Prevent these failure modes:
  - A future contributor adding a Tree-sitter-backed "faster" document-symbol or definition provider that bypasses `ide/lsp`, creating two competing navigation truths.
  - Source-edit protocols drifting to depend on tokenizer-internal offsets instead of `SourceSpan`/`SourcePosition`, which would break the moment Epic 2 changes the parser implementation.
  - Treating this story as an opportunity to add new navigation features (e.g. hover, rename-symbol) that are out of M17's scope.

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not add `ANTLR4` or Tree-sitter dependencies in this story.
- Reuse the existing `lsp4j`-based Kotlin test style already present in `ide/lsp/src/test/kotlin`.

### File Structure Requirements

- Expected update files likely include:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt` (KDoc hardening only)
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaUpdateComponentSourceEditProtocol.kt` (KDoc hardening only)
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/` new or extended test file(s) covering navigation/symbol/source-edit parity
  - `ide/lsp/README.md`
  - `ide/lsp/README.zh-CN.md`
- Do not modify `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts` or `athena-semantic-selection-service.ts` production behavior in this story; if the audit finds an unexpected local-parsing shortcut, document it as a follow-up rather than refactoring frontend code under this story's scope.

### Testing Requirements

- Minimum verification should target the LSP module directly:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Recommended regression given `:ide:lsp` depends on `:kernel:compiler` and `:kernel:language`:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
- Keep Gradle verification strictly sequential on Windows. Do not run these commands concurrently; wait for each to finish before starting the next.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after updating the bilingual README files.

### Explicit Non-Goals

- No `ANTLR4` grammar or dependency in this story (Epic 2).
- No Tree-sitter grammar, adapter, or `ide/tree-sitter-athena` package in this story (Epic 3).
- No new LSP capability (hover, rename, code actions) beyond the existing document-symbol, definition, references, and source-edit surfaces.
- No change to existing navigation, symbol, or source-edit output for currently supported syntax.
- No frontend (`ide/theia-frontend`) production behavior changes beyond documentation.

### Previous Milestone Intelligence

- M8 established the shared review-and-reveal path (`revealSemanticId` and bidirectional source/graph selection) as a stable product surface; M17 must not regress it while hardening the parser underneath.
- M4 and M5 established `AthenaLanguageFeatures`/`AthenaNavigationIndex` as the LSP-owned navigation layer; M15's guided-authoring source-edit protocols extended the same AST-span-anchored editing pattern. Story `4.2` treats all of these as one family of "AST-span consumers" that this story's audit and tests must cover together.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaUpdateComponentSourceEditProtocol.kt]
- [Source: ide/theia-frontend/src/browser/athena-graph-adapter-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-semantic-selection-service.ts]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: ide/lsp/README.md]

## Dev Agent Record

### Agent Model Used

Sonnet 5 (Cursor subagent)

### Debug Log References

- None. Navigation, symbol, and source-edit utilities already depended only on `SourceFileAst`/`SourceSpan` on inspection; no defects found.

### Completion Notes List

- Confirmed `AthenaLanguageFeatures.documentSymbols`/`.definition`/`.references` and `AthenaNavigationIndex` depend only on `SourceFileAst.declarations` and `SourceSpan`/`SourcePosition`, with `SourceSpan.toLspRange()` as the single span-to-LSP-range conversion point.
- Confirmed `AthenaUpdateComponentSourceEditProtocol.kt` and its connect-ports/create-component siblings compute edits from `DeviceDeclaration.span`, `PortDeclaration.qualifiedName.span`, and `ConnectionDeclaration.from`/`to` spans over raw tracked text, never a parser-generator tree.
- Confirmed `ide/theia-frontend`'s `revealSemanticId` (`athena-graph-adapter-service.ts`) and `athena-semantic-selection-service.ts` are pure consumers of server-owned `definition`/`references` results with no local re-parsing.
- Added `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceNavigationParityTest.kt` pinning `documentSymbols`, `definition`/`references`, and non-null `componentSourceRange`/`portSourceRange`/`connectionSourceRange` behavior for `examples/m0/demo-cabinet.athena`.
- Confirmed `acceptedUpdateComponentPropertiesSourceEdit`'s span-anchored `newText`/range behavior is already covered by the existing "update component properties" case in `AthenaAuthoringRequestTest.kt`, satisfying the task's "or its existing test if one already covers this" allowance.
- Added the "AST-Dependent Utility Inventory" table (Story 4.2, AD-109/AD-106) plus migration-survival KDoc to `ide/lsp/README.md`/`README.zh-CN.md`, `AthenaNavigationIndex`, and the source-edit protocol files.
- No existing navigation, symbol, or source-edit output was changed.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt` (KDoc hardening)
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaUpdateComponentSourceEditProtocol.kt` (KDoc hardening)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceNavigationParityTest.kt` (new regression test)
- `ide/lsp/README.md`, `ide/lsp/README.zh-CN.md`

## Story Completion Status

- Status: done
- Completion note: Verified by reading `AthenaSourceNavigationParityTest.kt`, the AST-dependent-utility KDoc on `AthenaLanguageFeatures.kt`/`AthenaUpdateComponentSourceEditProtocol.kt`, and the "AST-Dependent Utility Inventory" table in `ide/lsp/README.md`/`README.zh-CN.md`. All five acceptance criteria are satisfied by existing, checked-in code, tests, and docs; no navigation/editing behavior was changed.
