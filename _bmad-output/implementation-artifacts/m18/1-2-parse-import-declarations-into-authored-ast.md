---
baseline_commit: 953e3ecb782da8715f80a648e606f89391cf33d4
---

# Story 1.2: Parse Import Declarations Into Authored AST

Status: done

## Story

As a package author,
I want Athena source to import governed package or symbol meaning,
so that later semantic graph resolution can bind those imports.

## Acceptance Criteria

1. ANTLR accepts zero or more `import <qualified-target>` declarations after the optional package declaration and before the existing `system` block.
2. `SourceFileAst` exposes ordered Athena-owned `ImportDeclaration` values carrying the authored target and exact half-open source spans; generated ANTLR types do not escape `:kernel:language`.
3. Import targets preserve dotted segments, digits, capitalization, and internal hyphens needed for package and symbol-target intent. Semantic package-versus-symbol classification is deferred to graph resolution.
4. Missing targets, duplicate/misplaced syntax forms, aliases, wildcard imports, export/visibility forms, and malformed target punctuation fail deterministically as typed syntax diagnostics.
5. Existing package-only and package-less source remains compatible. `import` stays contextual where the established identifier rule permits keywords.
6. This story performs no repository lookup, package availability validation, symbol linking, lowering, Tree-sitter, LSP, frontend, canvas, or proof-corpus work.

## Tasks / Subtasks

- [x] Extend ANTLR with repeated file-level imports (AC: 1, 3-5)
  - [x] Accept imports only between package metadata and `system`.
  - [x] Reuse the existing dotted/hyphenated target shape and reject skipped trivia inside names.
  - [x] Add only the `import` keyword; do not add alias, wildcard, export, visibility, or terminator syntax.
- [x] Extend authored AST and adapter contracts (AC: 2, 3, 5)
  - [x] Add cohesive `ImportDeclaration` data and an ordered default-empty `SourceFileAst.imports` property.
  - [x] Map each import target and span through the internal ANTLR adapter.
  - [x] Keep imports outside system-body `Declaration` and preserve package-less/package-only constructor compatibility.
- [x] Add parser and grammar tests first (AC: 1-5)
  - [x] Prove ordered package and symbol-target examples with exact spans.
  - [x] Prove malformed, aliased, wildcard, misplaced, and whitespace-split targets fail deterministically.
  - [x] Prove existing package/system declarations and contextual identifiers remain intact.
- [x] Run scoped verification sequentially
  - [x] Run `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:plugins:plugin-api:test`, and `:ide:lsp:test` without concurrent Gradle invocations.
  - [x] Run the encoding audit after text edits.

### Review Findings

- [x] [Review][Patch] Reject a missing or next-line import target before contextual `system` can be consumed as its name [kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt]
- [x] [Review][Patch] Reject duplicate import targets at the second `import` keyword [kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt]
- [x] [Review][Patch] Assert exact authored diagnostics for unsupported and malformed import forms, including tokenless lexer errors [kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [x] [Review][Patch] Cover package-free import mapping and the `SourceFileAst` root span [kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [x] [Review][Patch] Cover contextual `import` identifiers and valid/invalid hyphenated import targets [kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt]

## Dev Notes

- Build on Story 1.1's `packageName`, package-name trivia guard, `PackageDeclaration`, and optional file-header AST shape; do not create a second parser path or dotted-name DTO.
- Recommended syntax is `import com.engineeringood.controls` and `import com.engineeringood.controls.Switch`. The parser preserves the target; later compiler stories determine whether it resolves to a package or declaration.
- Expected updates stay in `kernel/language` grammar, models, adapter, parser tests, grammar smoke tests, and facade allow-list.
- Keep Java 25, Kotlin 2.4.0, ANTLR 4.13.2, and existing Gradle wiring. Add no dependency.
- The Theia frontend and Kotlin Compose desktop viewer are not involved.

### References

- [Source: `epics.md` - Epic 1, Story 1.2]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-1, FR-2]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-2, AD-4]
- [Source: `1-1-parse-package-declarations-into-authored-ast.md` - implementation and review learnings]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: focused parser tests failed compilation for missing `ImportDeclaration`, `SourceFileAst.imports`, and generated `importDecl()`.
- GREEN: focused grammar and parser tests passed after the minimal grammar/model/adapter implementation.
- Verification: `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:plugins:plugin-api:test`, and `:ide:lsp:test` passed sequentially.
- Encoding: `tools/encoding-audit.ps1` passed after source and artifact edits.
- Review: all three review layers completed; five patch groups were applied and verified, with no deferred or unresolved findings.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added ordered file-level imports between optional package metadata and the system block.
- Added Athena-owned import AST data with exact target/declaration spans and default-empty compatibility.
- Reused the dotted/hyphenated header-name mapper and deterministic trivia rejection without semantic resolution.
- Preserved contextual keywords and kept imports outside system-body `Declaration`.
- Rejected missing/next-line and duplicate import targets with exact authored diagnostics; tokenless lexer failures now retain valid source offsets.
- Added package-free span, contextual-keyword, hyphen-boundary, duplicate, and exact malformed-form coverage from code review.

### File List

- `_bmad-output/implementation-artifacts/m18/1-2-parse-import-declarations-into-authored-ast.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`

## Change Log

- 2026-07-15: Implemented Story 1.2 import grammar, authored AST adaptation, diagnostics, compatibility, and regression coverage; completed scoped sequential verification.
- 2026-07-15: Applied all five code-review patch groups and repeated full scoped language/compiler/plugin API/LSP verification.
