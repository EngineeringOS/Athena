---
baseline_commit: f4510fd64041548ad8f064f6396db92f2de0178b
---

# Story 1.1: Freeze The Public Authored Syntax Contract

Status: done

## Story

As a platform engineer,
I want Athena to define one stable authored AST and parse-result contract,
so that compiler parsing can change without forcing downstream semantic consumers to depend on parser-generator internals.

## FR Traceability

- FR-1: Athena can preserve one explicit authored AST boundary.
- FR-2: Athena can publish a dual-parser responsibility model.
- NFR-1: `Engineering IR` remains canonical engineering truth.
- NFR-2: Generated parser-tree types do not become public semantic contracts.
- NFR-3: Source spans and diagnostics remain inspectable across parser migration.

## Acceptance Criteria

1. Given M17 hardens the language substrate, when the syntax boundary is reviewed, then Athena publishes `SourceFileAst`, `ParseResult` (`ParseSuccess`/`ParseFailure`), `SourceSpan`, `SourcePosition`, and `SyntaxDiagnostic` as Athena-owned types in `:kernel:language`, not as generated parser-tree types.
2. Given downstream consumers are inspected, when `:kernel:compiler` (`AthenaCompiler`), tests, and any other module that touches syntax are reviewed, then they depend only on `com.engineeringood.athena.language` contract types and never import ANTLR or Tree-sitter implementation classes (none exist yet, and none may be introduced by this story).
3. Given the public contract surface is reviewed, when KDoc and module documentation are inspected, then every public type in `AthenaLanguageModel.kt` states its ownership boundary (syntax-only, no semantic truth) and its stability expectation across future parser implementation changes.
4. Given the contract is exercised by tests, when the existing parser test suite and any new contract-focused tests run, then they prove that `ParseSuccess`, `ParseFailure`, and `SyntaxDiagnostic` are stable, structurally-equal, syntax-only carriers that do not leak parser-internal types (e.g. no `Token`, no tokenizer-private types) through their public API.

## Tasks / Subtasks

- [x] Audit the current public syntax contract surface in `:kernel:language`. (AC: 1, 2)
  - [x] Re-read `AthenaLanguageModel.kt` end to end and confirm every public type (`SourcePosition`, `SourceSpan`, `SourceFileAst`, `SystemDeclaration`, `Declaration`, `DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`, `QualifiedName`, `PropertyAssignment`, `ScalarValue`, `ParseResult`, `ParseSuccess`, `ParseFailure`, `SyntaxDiagnostic`) is exposed with `public` (default) visibility and has no leaking private/internal parser type in its signature.
  - [x] Confirm `AthenaLanguageParser` (the public class in `AthenaLanguageParser.kt`) exposes only `parse(file: String, source: String): ParseResult` and that `AthenaTokenizer`, `AthenaParser`, `Token`, `TokenKind`, `ParseException`, `TokenizationResult`, `TokenizationSuccess`, and `TokenizationFailure` remain `private` and are never referenced outside `AthenaLanguageParser.kt`.
  - [x] Confirm `:kernel:compiler`'s `AthenaCompiler.kt` only imports `AthenaLanguageParser`, `ParseFailure`, `ParseResult`, `ParseSuccess` from `com.engineeringood.athena.language`, and that `CompilerSourceDocument` (in `CompilerModels.kt`) wraps `SourceFileAst` rather than any parser-internal type.
- [x] Strengthen KDoc to state the freeze explicitly. (AC: 1, 3)
  - [x] Add or extend KDoc on `SourceFileAst`, `ParseResult`, `SourceSpan`, `SourcePosition`, and `SyntaxDiagnostic` in `AthenaLanguageModel.kt` stating each type is the Athena-owned public syntax contract and remains stable across compiler-parser implementation changes (i.e. across a future ANTLR4 migration).
  - [x] Add or extend KDoc on `AthenaLanguageParser.parse` stating that its return type (`ParseResult`) is the only supported way for callers to obtain a syntax-owned AST, and that callers must not depend on how parsing is implemented internally.
  - [x] Do not rename existing public types in this story. Naming stability itself is part of the freeze; renames (if ever needed) are an explicit, separate decision.
- [x] Update module documentation to describe the frozen contract. (AC: 1, 3)
  - [x] Update `kernel/language/README.md` to state explicitly that `AthenaLanguageModel.kt` is Athena's frozen public authored syntax contract for M17, and that any future parser implementation (including a future ANTLR4 migration in Epic 2) must keep producing these same contract types.
  - [x] Update `kernel/language/README.zh-CN.md` with the equivalent Chinese wording, keeping the file saved as UTF-8 with BOM per the workspace encoding rule.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after editing both README files.
- [x] Add contract-focused tests that prove the boundary, without duplicating existing parser-behavior tests. (AC: 2, 4)
  - [x] Add a focused test (e.g. in a new `AthenaLanguageContractTest.kt` alongside the existing `AthenaLanguageParserTest.kt`) that parses a small valid source snippet and asserts the returned `ParseResult` is exactly `ParseSuccess`/`ParseFailure` with no other sealed-interface implementers reachable from the public API.
  - [x] Add a test asserting that `SyntaxDiagnostic` carries `file`, `line`, `column`, `message`, and `span` and nothing parser-internal (e.g. no raw token references).
  - [x] Do not remove or weaken any assertions in the existing `AthenaLanguageParserTest.kt` or `AthenaLanguageProvenanceTest.kt`.
- [x] Keep Story `1.1` narrow. (AC: 1, 2, 3, 4)
  - [x] Do not introduce an ANTLR4 grammar, ANTLR dependency, or generated parser artifacts. That work is scoped to Epic 2, Story `2.1`.
  - [x] Do not introduce a Tree-sitter grammar or package. That work is scoped to Epic 3.
  - [x] Do not change `AthenaLanguageParser`'s tokenizer/parser implementation behavior. This story hardens and documents the existing contract; it does not rewrite parsing logic.
  - [x] Do not add a `:kernel:language`-internal package split for parser internals yet; that packaging move belongs to Story `1.2`.
- [x] Run focused and regression verification sequentially on Windows with Java 25. (AC: 1, 2, 3, 4)

## Dev Notes

### Story Intent

- Story `1.1` is the naming-and-boundary freeze for M17's language architecture, mirroring how M16 Story `1.1` froze the Semantic Macro contract before any runtime or catalog work began.
- The success condition is not "Athena has ANTLR4 now." The success condition is "Athena has explicitly declared, documented, and test-proven that `SourceFileAst`, `ParseResult`, `SourceSpan`, and `SyntaxDiagnostic` are the permanent public syntax contract that every future parser implementation (handwritten today, ANTLR4 tomorrow) must keep producing."
- Story `1.2` isolates the parser implementation packaging behind this now-frozen contract.
- Story `1.3` extends the AST shape itself with a deliberate extensibility landing zone, still without touching parser internals.
- Epic 2 is the only place a real ANTLR4 grammar may be introduced, and even then it must keep emitting the exact contract types this story freezes.

### Architecture Guardrails

- Align to AD-104: M17 freezes one language architecture before language breadth expands. Story `1.1` is the concrete first move of that freeze: lock the public contract types before any parser-generator work begins. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-104---M17-Freezes-One-Language-Architecture-Before-Language-Breadth-Expands]
- Align to AD-105: compiler parsing will eventually use ANTLR4, but public syntax contracts remain Athena-owned. Story `1.1` defines and documents exactly which types are "Athena-owned" so Epic 2 has an unambiguous target to keep producing. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-105---Compiler-Parsing-Uses-ANTLR4-But-Public-Syntax-Contracts-Remain-Athena-Owned]
- Align to AD-106: the authored AST remains the only lowering input before `Engineering IR`. Story `1.1` confirms `AthenaCompiler` already only lowers from `SourceFileAst`/`ParseSuccess` and documents that this must not change. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-106---Authored-AST-Remains-The-Only-Lowering-Input-Before-Engineering-IR]
- Align to AD-109: parser migration must preserve provenance and failure quality. Story `1.1`'s contract tests must prove `SyntaxDiagnostic` and `SourceSpan` provenance survives as part of the frozen surface, since Epic 2 will be measured against this baseline. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-109---Parser-Migration-Must-Preserve-Provenance-And-Failure-Quality]
- Preserve inherited AD-82: DSL remains canonical serialization, not the default human interface. Nothing in this story changes that; the story only hardens syntax contract types, not the authoring UX story. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` already defines the full syntax contract: `SourcePosition`, `SourceSpan`, `SourceFileAst`, `SystemDeclaration`, sealed `Declaration` (`DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`), `QualifiedName`, `PropertyAssignment`, sealed `ScalarValue` (`Identifier`, `StringLiteral`), sealed `ParseResult` (`ParseSuccess`, `ParseFailure`), and `SyntaxDiagnostic`.
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` already implements a handwritten recursive-descent parser: public class `AthenaLanguageParser` with one public method `parse(file: String, source: String): ParseResult`; everything else (`AthenaTokenizer`, `AthenaParser`, `Token`, `TokenKind`, `ParseException`, `TokenizationResult`/`TokenizationSuccess`/`TokenizationFailure`) is already `private` to the file.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` already imports only `AthenaLanguageParser`, `ParseFailure`, `ParseResult`, `ParseSuccess` and wraps the parsed `SourceFileAst` inside `CompilerSourceDocument` (defined in `CompilerModels.kt`). Its private `parseSource(...)` helpers already convert `SyntaxDiagnostic` into a compiler-owned `CompilerSyntaxDiagnostic`. This is the reference pattern for "downstream depends on Athena syntax contracts, not parser internals."
- `kernel/language/README.md` and `README.zh-CN.md` already describe the syntax-only boundary in prose but do not yet call out that this is an explicit, frozen M17 contract that later parser implementations must preserve.
- Tests already exist: `AthenaLanguageParserTest.kt` (parses `examples/m0/demo-cabinet.athena`, checks determinism, checks diagnostic provenance), `AthenaLanguageProvenanceTest.kt` (span coverage, over-qualified port rejection, qualified connection endpoint rejection), `LanguageModuleMarkerTest.kt` (trivial marker check). None of these currently assert on the shape of the public contract itself (i.e. that only `ParseSuccess`/`ParseFailure` exist, or that `SyntaxDiagnostic` carries no parser-internal type) — that gap is what this story's new contract test closes.
- There is no ANTLR4 dependency, grammar file, or generated parser code anywhere in the repository yet. There is no Tree-sitter package yet. This story must not introduce either.

### Technical Requirements

- Do not rename any existing public type (`SourceFileAst`, `ParseResult`, `ParseSuccess`, `ParseFailure`, `SourceSpan`, `SourcePosition`, `SyntaxDiagnostic`, `SystemDeclaration`, `Declaration`, `DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`, `QualifiedName`, `PropertyAssignment`, `ScalarValue`). The architecture spine's consistency conventions already recommend `AuthoredAst`, `ParseResult`, `SyntaxDiagnostic`, and `SourceSpan` as architecture nouns, and the current names already satisfy that; renaming is out of scope and would break Story `1.1`'s own "freeze" premise.
- Add KDoc, not new fields or new sealed variants, to the existing model types. Structural changes to the AST (e.g. new declaration kinds) are Story `1.3`'s responsibility, not this story's.
- Keep all new test code in `kernel/language/src/test/kotlin/com/engineeringood/athena/language/`, following the existing `kotlin.test` (`Test`, `assertEquals`, `assertIs`, `assertTrue`) style already used by `AthenaLanguageParserTest.kt` and `AthenaLanguageProvenanceTest.kt`. Do not introduce a different test framework.
- Do not add any third-party dependency to `kernel/language/build.gradle.kts`. It currently has zero `dependencies { }` entries; this story should keep it that way.

### Architecture Compliance

- The story is only successful if later Epic 2 work (ANTLR4 grammar and parse-tree adaptation) can point to one unambiguous, already-documented contract surface to keep producing, instead of re-deriving "what counts as public" mid-migration.
- Prevent these failure modes:
  - A future contributor assuming `AthenaLanguageParser`'s private tokenizer/parser types are safe to reference from `:kernel:compiler` or elsewhere.
  - Documentation drift where the README still describes the syntax boundary only in general prose without stating it is the frozen M17 contract target.
  - Contract tests that only re-test parsing behavior (already covered by `AthenaLanguageParserTest`) instead of testing the contract's shape and closure (only `ParseSuccess`/`ParseFailure` reachable, no leaking internal types).
  - Silently drifting `AthenaCompiler`'s syntax imports to include something other than the four already-approved names (`AthenaLanguageParser`, `ParseFailure`, `ParseResult`, `ParseSuccess`).

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not add ANTLR4, Tree-sitter, or any other parser-generator dependency in this story.
- Reuse current Kotlin and test style already present in `kernel/language`.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` (KDoc hardening only)
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` (KDoc hardening only)
  - `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageContractTest.kt` (new contract test file)
  - `kernel/language/README.md`
  - `kernel/language/README.zh-CN.md`
- Do not create a new Gradle module. This story stays entirely inside the existing `:kernel:language` module.
- Do not touch `:kernel:compiler` production code in this story; if the audit in Task 1 finds an unexpected import, note it as a follow-up rather than silently refactoring compiler internals under this story's scope.

### Testing Requirements

- Minimum verification should target the language module directly first:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
- Recommended regression after the module tests pass, since `:kernel:compiler` is the primary downstream consumer of this contract:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Keep Gradle verification strictly sequential on Windows. Do not run these two commands concurrently; wait for the first to finish before starting the second.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after updating the bilingual README files.

### Explicit Non-Goals

- No ANTLR4 grammar, dependency, or generated parser code in this story.
- No Tree-sitter grammar or package in this story.
- No change to tokenizer or parser runtime behavior; parsing outcomes for existing valid and invalid inputs must remain byte-for-byte identical.
- No renaming of existing public contract types.
- No new AST node kinds or new `ScalarValue`/`Declaration` variants (that is Story `1.3`).
- No package restructuring of parser internals into a separate internal package (that is Story `1.2`).
- No changes to `:kernel:compiler` production code beyond what the audit in Task 1 discovers and documents.

### Previous Milestone Intelligence

- M16 Story `1.1` established the pattern this story follows: freeze naming and ownership boundaries in code comments and docs before any deeper implementation work begins, and prove the freeze with focused contract tests rather than broad behavioral tests.
- M16 also proved that keeping contract modules dependency-free (no third-party libraries) keeps the freeze meaningful; `:kernel:language` already follows that pattern with zero dependencies in `build.gradle.kts`, and this story must not change that.
- The existing `:kernel:language` module was already built with future parser-generator migration in mind (syntax-only AST, explicit spans, explicit diagnostics), which is why this story is a hardening-and-documentation pass rather than a redesign.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt]
- [Source: kernel/language/README.md]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]

## Dev Agent Record

### Agent Model Used

Composer (Auto)

### Debug Log References

- `:kernel:language:test` initially failed on contract fixture using invalid `type = PLC` property syntax; fixed to `type PLC`.
- Existing `AthenaLanguageParserTest` expected demo `type PLC` but fixture `examples/m0/demo-cabinet.athena` uses `type Switch`; aligned expectation to `Switch` without weakening other assertions.
- Encoding audit passed after bilingual README updates.

### Completion Notes List

- Frozen public syntax contract in KDoc on all public model types plus `AthenaLanguageParser` facade.
- Documented M17 freeze boundary in English and Chinese module READMEs.
- Added `AthenaLanguageContractTest` proving `ParseSuccess`/`ParseFailure` and `SyntaxDiagnostic` field shape without leaking parser internals.
- No ANTLR/Tree-sitter, no parser behavior rewrite, no internal package split (deferred to 1.2).
- Verified: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:language:test"` → BUILD SUCCESSFUL.
- Encoding audit passed.

### File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageContractTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/README.md`
- `kernel/language/README.zh-CN.md`
- `_bmad-output/implementation-artifacts/m17/1-1-freeze-the-public-authored-syntax-contract.md`
- `_bmad-output/implementation-artifacts/m17/sprint-status.yaml`

### Change Log

- 2026-07-14: Implemented Story 1.1 contract freeze, docs, and contract tests; status → review.

## Story Completion Status

- Status: done
- Completion note: Public authored syntax contract is frozen in KDoc/docs/tests. `:kernel:language:test` and encoding audit pass. Ready for code review before Story 1.2.
