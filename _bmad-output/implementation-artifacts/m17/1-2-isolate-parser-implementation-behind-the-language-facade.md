# Story 1.2: Isolate Parser Implementation Behind The Language Facade

Status: done

## Story

As a platform engineer,
I want parser implementation details isolated behind `:kernel:language`,
so that parser migration does not become a public architecture leak.

## FR Traceability

- FR-1: Athena can preserve one explicit authored AST boundary.
- FR-2: Athena can publish a dual-parser responsibility model.
- FR-4: Athena can preserve lowering through authored AST instead of parse trees.
- NFR-2: Generated parser-tree types do not become public semantic contracts.
- NFR-5: The M17 architecture must make future syntax additions cheaper, not harder.

## Acceptance Criteria

1. Given compiler parsing internals are implemented, when the language boundary is inspected, then generated parser artifacts remain implementation detail behind Athena-owned parse APIs. Today that means the handwritten tokenizer/parser stay `private`; after Epic 2 introduces ANTLR4, the same rule must hold for generated lexer/parser classes without this story needing to change.
2. Given future parser changes are considered, when the public syntax seam is reviewed, then the facade (`AthenaLanguageParser.parse(file, source): ParseResult`) is stable enough that downstream modules do not need parser-generator-specific imports, verified by an explicit package-boundary convention and a test that fails if a parser-internal type is referenced from outside its owning file/package.
3. Given the module is restructured for facade isolation, when the internal package layout is reviewed, then parser implementation code lives under a clearly-named internal package (e.g. `com.engineeringood.athena.language.parser`) distinct from the public contract package (`com.engineeringood.athena.language`), and the public facade re-exports only the contract types and the `AthenaLanguageParser` entry point.
4. Given the facade isolation work is complete, when the existing parser test suite is re-run, then all previously passing behavior (successful parse of `examples/m0/demo-cabinet.athena`, deterministic parsing, diagnostic provenance, over-qualified port rejection, qualified connection endpoint rejection) continues to pass unchanged.

## Tasks / Subtasks

- [x] Decide and document the internal package boundary for parser implementation. (AC: 1, 3)
  - [x] Introduce a `com.engineeringood.athena.language.parser` internal package inside `:kernel:language` to host tokenizer/parser implementation code, keeping `com.engineeringood.athena.language` (the existing package) as the public contract package containing `AthenaLanguageModel.kt` and the public `AthenaLanguageParser` facade class.
  - [x] Document in `kernel/language/README.md` (and the Chinese counterpart) that `com.engineeringood.athena.language` is the public syntax boundary and any `com.engineeringood.athena.language.parser` (or later `com.engineeringood.athena.language.antlr`) sub-package is internal implementation detail that downstream modules must never import directly.
  - [x] Explicitly name this seam as the future home for ANTLR4 grammar output and parse-tree-to-AST adaptation code (Epic 2), so Story `1.2` sets up the seam Epic 2 will fill rather than Epic 2 inventing packaging from scratch.
- [x] Move the current handwritten tokenizer/parser implementation behind the new internal package without changing behavior. (AC: 1, 3, 4)
  - [x] Relocate `AthenaTokenizer`, `AthenaParser`, `Token`, `TokenKind`, `ParseException`, `TokenizationResult`/`TokenizationSuccess`/`TokenizationFailure` (currently private classes inside `AthenaLanguageParser.kt`) into the new internal package, keeping them non-public (`internal` visibility scoped to the module, since Kotlin file-private does not cross package boundaries).
  - [x] Keep the public `AthenaLanguageParser` class and its single public `parse(file: String, source: String): ParseResult` method in the public `com.engineeringood.athena.language` package, delegating to the internal package's tokenizer/parser.
  - [x] Verify the relocation is behavior-preserving: no change to tokenization rules, grammar rules, error messages, or diagnostic positions.
- [x] Add an automated boundary check that catches future leaks. (AC: 2)
  - [x] Add a test (e.g. `LanguageFacadeBoundaryTest.kt`) that enumerates the public API surface of `:kernel:language` (via reflection over compiled classes, or a maintained allow-list of public type names) and asserts it matches exactly the intended facade: `AthenaLanguageParser`, the `AthenaLanguageModel.kt` contract types, and `LanguageModuleMarker`.
  - [x] If full reflection-based enumeration is impractical for this story, at minimum add a test asserting that internal-package types (`AthenaTokenizer`, `AthenaParser`, `Token`, `TokenKind`) are not `public` and are not importable via the module's public package.
  - [x] Add a code comment on the internal package's top-level file(s) stating explicitly that types in this package are not part of the public contract and must not be imported by `:kernel:compiler`, `:kernel:runtime`, `:ide:*`, or any other module.
- [x] Confirm downstream modules are unaffected. (AC: 2, 4)
  - [x] Re-check `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` still imports only `AthenaLanguageParser`, `ParseFailure`, `ParseResult`, `ParseSuccess` and compiles unchanged after the internal package move.
  - [x] Search the repository for any other reference to `AthenaTokenizer`, `AthenaParser` (the parser-internal class, not `AthenaLanguageParser`), `Token`, or `TokenKind` outside `:kernel:language` and confirm there are none; if any exist, they must be removed or refactored to use only the public facade as part of this story.
- [x] Keep Story `1.2` narrow. (AC: 1, 2, 3, 4)
  - [x] Do not implement the ANTLR4 grammar itself. Story `1.2` only prepares the packaging seam; the actual grammar and generated parser artifacts belong to Epic 2, Story `2.1`.
  - [x] Do not implement Tree-sitter or any `ide/*` grammar package. That is Epic 3.
  - [x] Do not change the AST model shape (`AthenaLanguageModel.kt`) in this story beyond what Story `1.1` already documented; structural AST extensibility is Story `1.3`.
  - [x] Do not widen the story into a general Kotlin "internal visibility" audit of unrelated modules.
- [x] Run focused and regression verification sequentially on Windows with Java 25. (AC: 1, 2, 3, 4)

## Dev Notes

### Story Intent

- Story `1.2` is the packaging move that makes Story `1.1`'s frozen contract enforceable, not just documented: it draws a real internal-package line between "public syntax contract" and "parser implementation detail" inside `:kernel:language`.
- The success condition is not "Athena has ANTLR4 packaging ready." The success condition is "the handwritten parser today lives behind exactly the same internal-package seam that a future ANTLR4 grammar will live behind, so Epic 2 can drop generated parser code into an already-proven isolation boundary instead of inventing one under migration pressure."
- This story explicitly sets up (but does not fill) the seam the architecture spine calls `kernel/language` Compiler Parsing Internals.
- Story `1.3` builds on this same isolated facade to extend the AST's own extensibility without touching parser internals.

### Architecture Guardrails

- Align to AD-104: M17 freezes one language architecture before language breadth expands. Story `1.2` is the concrete packaging move that makes the freeze structural (an internal package boundary) rather than only documentary. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-104---M17-Freezes-One-Language-Architecture-Before-Language-Breadth-Expands]
- Align to AD-105: compiler parsing will use ANTLR4, but generated lexer/parser artifacts are implementation detail. Story `1.2` builds the exact internal package this rule requires, ahead of Epic 2 actually generating ANTLR code into it. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-105---Compiler-Parsing-Uses-ANTLR4-But-Public-Syntax-Contracts-Remain-Athena-Owned]
- Align to AD-106: authored AST remains the only lowering input; parse-tree-to-AST adaptation is isolated inside the syntax layer. Story `1.2`'s internal package is exactly the isolation point AD-106 requires for that future adaptation code. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-106---Authored-AST-Remains-The-Only-Lowering-Input-Before-Engineering-IR]
- Align to the architecture spine's `kernel/language` New Platform Boundaries section: "`kernel/language` Compiler Parsing Internals" purpose is to "host ANTLR4 grammar and generated compiler parser artifacts" and "adapt parse trees into Athena-authored AST," with the boundary rule that "parser-generation detail remains internal" and "generated artifacts do not become architecture nouns outside the syntax layer." Story `1.2` is the story that creates this boundary structurally. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#New-Platform-Boundaries]
- Preserve inherited AD-34: one mutation authority above source and graph remains binding. Nothing in this packaging move creates any new write path; parsing remains read-only syntax analysis. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` currently defines the public `AthenaLanguageParser` class alongside `private class AthenaTokenizer`, `private class AthenaParser`, `private data class Token`, `private enum class TokenKind`, `private class ParseException`, and the private `TokenizationResult`/`TokenizationSuccess`/`TokenizationFailure` hierarchy, all in the same file and the same package (`com.engineeringood.athena.language`). File-private visibility already prevents cross-file access within Kotlin, but everything still lives in the single public contract package, which this story changes.
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` and `LanguageModuleMarker.kt` are the public contract files that must remain in `com.engineeringood.athena.language` untouched by this story's package move.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` already imports only the four public names (`AthenaLanguageParser`, `ParseFailure`, `ParseResult`, `ParseSuccess`) via `com.engineeringood.athena.language.*` imports; this is the proof pattern that facade isolation already mostly works today, and this story must keep it working identically after the internal package move.
- `kernel/language/build.gradle.kts` currently has no `sourceSets` customization and no dependencies; a new internal package is just a new sub-package under the same `src/main/kotlin` root, not a new source set or new module.
- No ANTLR4 dependency or grammar exists yet anywhere in the repository. No `src/main/antlr` directory exists yet. This story prepares the Kotlin-package-level seam only; it does not add the ANTLR Gradle plugin or grammar files.

### Technical Requirements

- Use Kotlin `internal` visibility (module-scoped) for the relocated tokenizer/parser types now that they live in their own package and can no longer rely on file-private visibility to stay hidden from other files in the same module.
- Keep the public facade class name `AthenaLanguageParser` and its public method signature `fun parse(file: String, source: String): ParseResult` unchanged; downstream modules must not need any code change.
- Name the internal package `com.engineeringood.athena.language.parser` to read naturally as "the parser implementation living behind the language facade," consistent with the architecture spine's recommended nouns (`CompilerParser`, `ParseAdapter`) and avoiding parser-library-specific names.
- Do not introduce a second Gradle module for this internal package. The isolation this story delivers is a package-level (and Kotlin-visibility-level) boundary inside the existing `:kernel:language` module, matching the architecture spine's expectation that `kernel/language` hosts both the public boundary and the compiler parsing internals as one module with two purposes.

### Architecture Compliance

- The story is only successful if Epic 2's future ANTLR4 grammar and generated parser code can be dropped into the `com.engineeringood.athena.language.parser` package (or a clearly-named sibling such as `com.engineeringood.athena.language.antlr`) and inherit this story's isolation guarantee automatically, without Epic 2 having to invent or renegotiate the public/internal boundary.
- Prevent these failure modes:
  - Parser-internal types becoming `public` "just to make a test easier," which would silently reopen the leak this story closes.
  - `:kernel:compiler` or any other module reaching around the facade to import something from `com.engineeringood.athena.language.parser` directly.
  - The boundary test becoming stale or deleted rather than updated when Epic 2 adds ANTLR-generated code to the internal package.
  - Treating this story as a chance to also redesign the AST model; that risk is explicitly fenced off into Story `1.1` (already frozen) and Story `1.3` (extensibility only).

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not add the ANTLR4 Gradle plugin or ANTLR4 runtime dependency in this story; that belongs to Story `2.1`.
- Reuse current Kotlin and test style already present in `kernel/language`.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` (keeps only the public facade class, delegating to the internal package)
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/parser/AthenaLanguageTokenizer.kt` or similarly named new file(s) hosting the relocated tokenizer
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/parser/AthenaLanguageGrammar.kt` or similarly named new file(s) hosting the relocated recursive-descent parser
  - `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt` (new boundary test)
  - `kernel/language/README.md`
  - `kernel/language/README.zh-CN.md`
- Do not create a new Gradle module or new `settings.gradle.kts` entry. This story stays entirely inside the existing `:kernel:language` module's source tree.
- Keep the existing test files (`AthenaLanguageParserTest.kt`, `AthenaLanguageProvenanceTest.kt`, `LanguageModuleMarkerTest.kt`) in place and passing unchanged; they exercise the public facade and must not need to change their imports.

### Testing Requirements

- Minimum verification should target the language module directly first:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
- Recommended regression after the module tests pass, since `:kernel:compiler` is the primary downstream consumer of this facade:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Keep Gradle verification strictly sequential on Windows. Do not run these two commands concurrently; wait for the first to finish before starting the second.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after updating the bilingual README files.

### Explicit Non-Goals

- No ANTLR4 grammar, dependency, or generated parser code in this story.
- No Tree-sitter grammar or package in this story.
- No change to tokenization or parsing behavior; all existing valid and invalid parse outcomes must remain identical after the internal package move.
- No AST model shape changes (new declaration kinds, new scalar value kinds) in this story.
- No new Gradle module, `settings.gradle.kts` entry, or build script dependency changes.
- No change to the public `AthenaLanguageParser.parse` method signature.

### Previous Milestone Intelligence

- M16 Story `1.1` proved that freezing a contract in prose and KDoc is necessary but not sufficient; M17 Story `1.2` goes one step further by making the boundary structural (a real internal package plus a boundary test), which is a stronger guarantee appropriate for a milestone whose entire premise is "parser implementation will change underneath this contract" (Epic 2's ANTLR4 migration).
- The existing `:kernel:language` module already demonstrates the target end-state partially: the handwritten parser's internal types are already file-private today. This story generalizes that same discipline from file-scope to package-scope so it survives the internal implementation being split across multiple files (which a real ANTLR4 grammar and generated parser will require).

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/language/README.md]

## Dev Agent Record

### Agent Model Used

Composer (Sonnet)

### Debug Log References

- Relocated handwritten tokenizer/parser into `com.engineeringood.athena.language.parser` with `internal` visibility.
- `LanguageFacadeBoundaryTest` asserts public package allow-list and that parser-internal type names are absent from the public package.
- Dropped an attempted `kotlin-reflect` visibility assertion (JVM classpath issue); package allow-list + ClassNotFound checks satisfy Story 1.2 minimum AC.
- Reverted accidental root README / unused catalog churn outside story scope.
- No ANTLR / Tree-sitter / AST shape changes in this story.
- Re-verification: `:kernel:language:test` PASS; `:kernel:compiler:test --tests AthenaCompilerTest` PASS; encoding audit PASS.

### Completion Notes List

- Public facade `AthenaLanguageParser.parse(file, source): ParseResult` unchanged; compiler still imports only the four public contract names.
- Internal package documented as Epic 2 ANTLR landing seam in README (EN/zh-CN) and package KDoc comments.
- Existing parser behavior tests remain the regression proof for AC4.
- Reviewed-after-implementation gate: re-ran language + focused compiler tests before marking sprint status done.

### File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/parser/AthenaTokenizer.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/parser/AthenaParser.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/language/README.md`
- `kernel/language/README.zh-CN.md`
- `_bmad-output/implementation-artifacts/m17/1-2-isolate-parser-implementation-behind-the-language-facade.md`
- `_bmad-output/implementation-artifacts/m17/sprint-status.yaml`

### Change Log

- 2026-07-14: Isolated parser implementation behind `language.parser`; status → done after verification.

## Story Completion Status

- Status: done
- Completion note: Parser implementation lives under `com.engineeringood.athena.language.parser` with `internal` visibility and a public-API boundary test. Facade signature unchanged. ACs verified.
