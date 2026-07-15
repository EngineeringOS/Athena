---
baseline_commit: 953e3ecb782da8715f80a648e606f89391cf33d4
---

# Story 1.1: Parse Package Declarations Into Authored AST

Status: done

## Story

As a package author,
I want Athena source to declare its governed package namespace,
so that a source unit can participate in package-aware compilation.

## Acceptance Criteria

1. Given authored source with one supported package declaration before the existing `system` block, when `AthenaLanguageParser` parses it through ANTLR, then parsing succeeds and the Athena-owned `SourceFileAst` contains a typed package declaration with the authored package name.
2. The package declaration and package name expose half-open `SourceSpan` values using the existing 1-based line/column and 0-based offset conventions; no generated ANTLR type crosses the public `com.engineeringood.athena.language` boundary.
3. Existing package-less Athena source remains valid and produces the same system/declaration AST behavior, with package data absent rather than synthesized.
4. Package names can represent the existing governed identity shape: lowercase dot-separated segments with digits and internal hyphens, such as `com.engineeringood.factory-line`. Parsing preserves each dot-separated segment exactly for later semantic validation and binding.
5. Missing names, duplicate package declarations, package declarations after the system block, and otherwise malformed package syntax return deterministic `ParseFailure` results with one Athena-owned syntax diagnostic carrying file, line, column, message, and span provenance.
6. This story adds syntax and authored-AST intent only. It does not add imports, aliases, package resolution, repository traversal, semantic validation, symbol linking, lowering behavior, Tree-sitter changes, LSP behavior, or M18 proof-corpus fixtures.

## Tasks / Subtasks

- [x] Extend the authoritative ANTLR grammar with the narrow package-declaration form (AC: 1, 3, 4, 5)
  - [x] Change `sourceFile` to accept at most one optional package declaration before `systemDecl`; keep package-less M0-M17 source valid.
  - [x] Add a package-name parser rule that preserves dot-separated segments and supports internal hyphens without introducing a lexer token that consumes existing lowercase dotted port/reference names.
  - [x] Preserve `->` tokenization when adding any hyphen token/rule, and keep `package` usable where the current contextual-keyword `ident` rule permits keywords as identifiers.
  - [x] Do not add import, alias, export, visibility, semicolon, or unrelated declaration syntax.
- [x] Extend the Athena-owned authored AST contract (AC: 1, 2, 3, 4)
  - [x] Add a small `PackageDeclaration` syntax model alongside the existing source-file/system models in `AthenaLanguageModel.kt`; use Athena-owned `QualifiedName`/`SourceSpan` contracts and no repository-model or generated-parser type.
  - [x] Add optional package data to `SourceFileAst` with a compatibility-preserving default so existing named constructor calls remain valid.
  - [x] Keep package metadata outside the sealed `Declaration` hierarchy because current `Declaration` values are system-body declarations consumed by lowering and LSP classifiers.
- [x] Adapt the ANTLR parse tree through the existing internal adapter (AC: 1, 2, 3, 4, 5)
  - [x] Map the optional package context to `PackageDeclaration`, preserving authored dot segments and hyphens.
  - [x] Keep the existing system span unchanged; make the source-file span cover the package declaration through the system closing brace when a package is present.
  - [x] Continue returning only `ParseSuccess` or `ParseFailure`; malformed input must not escape as an ANTLR exception or expose parser internals.
- [x] Add focused grammar and public-parser tests (AC: 1-5)
  - [x] Prove valid single-segment, dotted, and hyphenated governed package names and exact AST/span mapping.
  - [x] Prove package-less `examples/m0/demo-cabinet.athena` behavior remains unchanged.
  - [x] Prove missing, duplicate, misplaced, and malformed package declarations fail with deterministic positioned diagnostics.
  - [x] Add a regression proving the new keyword/hyphen handling does not break existing `device`, `port`, `connect`, property, BOM, or arrow parsing.
- [x] Refresh code-adjacent comments that still describe the grammar/root AST as M17-only (AC: 6)
  - [x] Keep historical M17 proof documentation accurate while making current source comments describe the M18 package-only increment.
- [x] Verify affected modules sequentially on Windows (AC: 1-6)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` to catch public `SourceFileAst` compatibility regressions.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test` and `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` sequentially; never overlap Gradle invocations.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

### Review Findings

- [x] [Review][Patch] Reject skipped whitespace around package-name dots and hyphens [`kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`:29]
- [x] [Review][Patch] Assert exact diagnostic columns and spans for malformed package declarations [`kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`:232]
- [x] [Review][Patch] Cover leading, trailing, and repeated package-name hyphens [`kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`:232]
- [x] [Review][Patch] Prove package headers preserve non-empty system declarations and references [`kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`:12]
- [x] [Review][Defer] Strengthen the pre-existing component-knowledge count test with implementation identity assertions [`kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerComponentKnowledgeIntegrationTest.kt`:65] - deferred, pre-existing M15 test completeness

## Dev Notes

### Current Implementation

- `Athena.g4` currently defines `sourceFile : systemDecl EOF`, and `AthenaAntlrAstAdapter.adapt` assumes `tree.systemDecl()` is the complete source root.
- `SourceFileAst` currently contains `system`, system-body `declarations`, and `span`. Existing constructor calls use named arguments, but the new package property should still default to absent for source compatibility and package-less parsing.
- `AthenaAntlrSyntaxErrorListener` already converts lexer/parser failures into one provenance-rich `SyntaxDiagnostic`; reuse it instead of creating package-specific error transport.
- `QualifiedName` already preserves authored name parts and spans. Reuse it for package syntax rather than introducing a second dotted-name DTO.
- Governed manifest names are validated by `PACKAGE_NAME_PATTERN = ^[a-z][a-z0-9-]*(\.[a-z][a-z0-9-]*)*$`. The syntax must be able to preserve that identity shape, but semantic equality with `PackageIdentifier` belongs to later project-semantic-graph stories.

### Architecture Compliance

- ANTLR remains the only compiler/LSP syntax authority. Generated `AthenaLexer`/`AthenaParser` types remain internal to `com.engineeringood.athena.language.antlr`.
- `:kernel:language` remains syntax-only and must not gain a dependency on `:kernel:repository-model`, inspect `athena.yaml`, resolve package paths, or validate package availability.
- Keep the package declaration as file-level authored intent. Do not force existing domain lowerers or `Declaration.toDocumentSymbol` to classify it as a system-body engineering declaration.
- Preserve deterministic parsing and half-open spans. Use the existing `startPosition`, `endPosition`, `spanOfToken`, and `spanOfContext` helpers.
- Do not reintroduce the removed handwritten parser or add a fallback parser.

### File Structure Requirements

Expected updates:

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`

Keep the small package/root syntax models together in `AthenaLanguageModel.kt`; a one-type file is unnecessary. Additional test files are acceptable only when they make package diagnostic cases materially easier to scan.

### Testing Requirements

- Assert model values and exact spans, not only parse success.
- Assert malformed package inputs produce `ParseFailure`, exactly one diagnostic, nonblank message, expected file/line/column, and deterministic repeated results.
- Retain M17 invalid-source corpus behavior and existing grammar smoke coverage.
- Do not create `examples/m18/` fixtures here; Story 1.5 owns syntax proof fixtures and Story 4.5 owns closeout corpus integration.
- Gradle commands must run strictly sequentially in this Windows repository.

### Scope Boundaries And Dependencies

- Story 1.2 builds import syntax on this file-level package/name foundation.
- Story 1.3 proves the narrow syntax boundary; avoid adding nearby language features preemptively.
- Story 1.4 owns Tree-sitter syntax mirroring. Compiler syntax may land first, but Tree-sitter must never gain semantic authority.
- Epic 2 owns canonical package identity, governed repository binding, and semantic graph construction. This story only preserves authored package intent for that later work.
- No frontend or canvas work is required; Theia/VS Code and EPLAN interaction standards are unaffected by this syntax-only story.

### Library And Framework Requirements

- Use the repository-pinned Java 25, Kotlin 2.4.0, Gradle 9.6.1, and ANTLR 4.13.2 stack.
- Reuse the existing Gradle ANTLR plugin configuration (`-visitor`, `-no-listener`) and generated-source wiring; add no dependency or plugin.
- No external research or version upgrade is required because this story uses existing pinned ANTLR APIs and repository-local patterns.

### References

- [Source: `epics.md` - Epic 1, Story 1.1 and Stories 1.2-1.5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-1, FR-2, NFR-4, NFR-5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 4, 5, 6, 8, 12]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-2, AD-4, AD-11 and Structural Seed]
- [Source: `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`]
- [Source: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt:32`]
- [Source: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt:45`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt:551`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: package grammar smoke test failed because `package` was rejected before `system`.
- RED: public parser tests failed compilation because `PackageDeclaration` and `SourceFileAst.packageDeclaration` did not exist.
- GREEN: focused grammar and parser tests passed after the minimal grammar/model/adapter implementation.
- Regression: corrected the pre-existing compiler integration expectation from five to six active electrical implementations, matching commit `9424a861`.
- Verification: `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:plugins:plugin-api:test`, and `:ide:lsp:test` passed sequentially.
- Scope: the repository-wide test task was intentionally excluded after confirming it enters the out-of-scope Kotlin Compose desktop viewer; no desktop-viewer file remains changed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added optional file-level package syntax with dotted, digit-bearing, and internally hyphenated name segments.
- Added Athena-owned package AST data and exact package/source spans without exposing ANTLR or repository-model types.
- Preserved package-less parsing, contextual keyword behavior, dotted references, arrows, and deterministic single-diagnostic failures.
- Added grammar, parser, facade-boundary, compatibility, and malformed-input coverage.
- Resolved all four actionable code-review findings: package-name trivia rejection, exact diagnostics, hyphen boundaries, and non-empty packaged-system preservation.

### File List

- `_bmad-output/implementation-artifacts/m18/1-1-parse-package-declarations-into-authored-ast.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerComponentKnowledgeIntegrationTest.kt`

## Change Log

- 2026-07-15: Implemented Story 1.1 package declaration grammar, authored AST adaptation, diagnostics and regression tests; aligned one stale compiler test expectation discovered during required verification.
- 2026-07-15: Applied all actionable code-review patches and completed scoped language/compiler/plugin API/LSP verification.
