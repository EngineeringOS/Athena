---
baseline_commit: f4510fd64041548ad8f064f6396db92f2de0178b
---

# Story 2.1: Publish The ANTLR4 Grammar For The Current Supported Syntax Subset

Status: done

## Story

As a compiler engineer,
I want Athena to define an `ANTLR4` grammar for the current authored syntax subset,
so that compiler parsing moves onto a durable parser technology without widening syntax scope prematurely.

## FR Traceability

- FR-3: Athena can parse supported Athena syntax through `ANTLR4`
- NFR-2: Generated parser-tree types do not become public semantic contracts
- NFR-5: The M17 architecture must make future syntax additions cheaper, not harder

## Acceptance Criteria

1. Given the current Athena source subset is reviewed, when the compiler grammar is implemented, then it supports `system`, `device`, `port`, `connect`, qualified names, string literals, and property assignments.
2. Given M17 remains parity-first, when grammar scope is inspected, then the first proof targets today's supported syntax rather than full future-language breadth.

## Tasks / Subtasks

- [x] Add ANTLR4 build support to `:kernel:language` without touching the active compiler parse path. (AC: 1, 2)
  - [x] Add an `antlr` version plus `antlr` (tool) and `antlr-runtime` library aliases pinned to `4.13.2` in `gradle/libs.versions.toml`; there is no ANTLR entry there today.
  - [x] Apply Gradle's built-in `antlr` plugin (`id("antlr")`) to `kernel/language/build.gradle.kts` alongside the existing `kotlinJvm` plugin.
  - [x] Add the `antlr` tool dependency and the `antlr4-runtime` implementation dependency; do not introduce any other new third-party dependency.
- [x] Author the ANTLR4 grammar for the current supported syntax subset. (AC: 1, 2)
  - [x] Add grammar file(s) under `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/`, the Gradle `antlr` plugin's default source set, scoped to today's parity subset only.
  - [x] Define lexer rules for `system`, `device`, `port`, `connect`, `{`, `}`, `.`, `->`, identifiers, string literals, and whitespace skipping consistent with the existing `AthenaTokenizer` behavior (including tolerating a leading UTF-8 BOM).
  - [x] Define parser rules for one system block, `device`/`port`/`connect` declarations, qualified names (respecting the same arity rules `AthenaParser.parseQualifiedName` enforces today: `port` and `connect` endpoints require exactly two dotted parts), property assignments, and scalar values (identifier or string literal).
  - [x] Do not add grammar rules for constructs the handwritten parser does not already accept; no premature widening toward `import`, expressions, or macro-use forms.
- [x] Prove the grammar recognizes today's supported forms without wiring it into the compiler path yet. (AC: 1, 2)
  - [x] Add a narrow smoke test that drives the generated ANTLR lexer/parser directly (not through `AthenaLanguageParser`) against the checked-in `examples/m0/demo-cabinet.athena` fixture.
  - [x] Assert the generated parser reaches EOF without a syntax error on that fixture, and add at least one inline case covering a single-part qualified name if the handwritten grammar in `AthenaParser.parseQualifiedName` allows it for `device` scope.
  - [x] Leave `AthenaLanguageParser`, `AthenaCompiler`, and all existing lowering untouched. Story `2.2` adapts parser output into the authored AST and Story `2.3` covers provenance/diagnostic parity.
- [x] Keep generated ANTLR artifacts implementation detail. (AC: 1, 2)
  - [x] Confirm generated lexer/parser/listener/visitor classes stay under the internal `...language.antlr` package and are not imported from any other Gradle module.
  - [x] Confirm no public type in `AthenaLanguageModel.kt` or `AthenaLanguageParser.kt` references a generated ANTLR type.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run Gradle tasks concurrently.

## Dev Notes

### Story Intent

- Story `2.1` is the "durable parser technology lands, scope stays frozen" step of Epic 2.
- The success condition is not "the compiler now parses through ANTLR." The success condition is "Athena has a generated ANTLR4 grammar that recognizably covers today's supported syntax, built through the existing `kernel/language` module, with generated artifacts kept as implementation detail."
- Story `2.2` owns adapting ANTLR parse trees into the authored AST and wiring the language facade onto them.
- Story `2.3` owns span/diagnostic parity once the ANTLR path is live end to end.

### Architecture Guardrails

- Align to AD-104: M17 freezes the parser architecture, not the final grammar; this story's grammar scope must match the current supported subset only. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-104---M17-Freezes-One-Language-Architecture-Before-Language-Breadth-Expands]
- Align to AD-105: `ANTLR4` is the compiler/LSP parser implementation; generated lexer/parser artifacts are implementation detail, and public syntax contracts stay Athena-owned in `AthenaLanguageModel.kt`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-105---Compiler-Parsing-Uses-ANTLR4-But-Public-Syntax-Contracts-Remain-Athena-Owned]
- Align to AD-110: the first M17 proof stays parity-first on `system`, `device`, `port`, `connect`, qualified names, string literals, and property assignments. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-110---The-First-M17-Proof-Stays-Parity-First-On-The-Current-Supported-Syntax-Subset]
- Align to AD-113: repository-backed proof inputs such as `examples/m0/demo-cabinet.athena` are stronger evidence than inline-only grammar demos. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-113---Repository-Backed-Proof-Inputs-Remain-Stronger-Than-Inline-Only-Parser-Demos]
- Preserve inherited AD-82 and AD-88: direct DSL remains canonical serialization rather than the default human interface, and this module change stays additive underneath existing platform seams. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` is the current handwritten tokenizer (`AthenaTokenizer`) and recursive-descent parser (`AthenaParser`); it remains the parser `AthenaCompiler` actually uses until Story `2.2` rewires the facade.
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` defines the only public syntax contracts (`SourceFileAst`, `SystemDeclaration`, `Declaration` and its `DeviceDeclaration`/`PortDeclaration`/`ConnectionDeclaration` implementations, `QualifiedName`, `PropertyAssignment`, `ScalarValue`, `ParseResult`, `ParseSuccess`, `ParseFailure`, `SyntaxDiagnostic`, `SourceSpan`, `SourcePosition`). Story `2.1` must not add a second competing contract.
- `AthenaCompiler` (`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`) constructs `AthenaLanguageParser()` by default and calls `parser.parse(file, sourceText)` inside `parseSource`; Story `2.1` does not change this call site.
- `kernel/language/build.gradle.kts` currently declares only the `kotlinJvm` plugin with an empty `dependencies {}` block; there is no ANTLR dependency yet.
- `AthenaLanguageParserTest` and `AthenaLanguageProvenanceTest` already prove today's supported subset and provenance for the handwritten parser against `examples/m0/demo-cabinet.athena` and inline malformed-source cases; they are the regression bar Stories `2.2`/`2.3` must not break, and this story must not modify them.

### Technical Requirements

- Use Gradle's built-in `antlr` plugin (`id("antlr")`), not a third-party ANTLR Gradle plugin, matching the repo's minimal-plugin convention already used by other `kernel/*` modules.
- Pin `org.antlr:antlr4` (tool) and `org.antlr:antlr4-runtime` (runtime) to `4.13.2`, the latest stable ANTLR4 release, via new `[versions]`/`[libraries]` entries in `gradle/libs.versions.toml`.
- Place the grammar under `src/main/antlr` (the Gradle `antlr` plugin's default source set) so the `generateGrammarSource` task produces generated sources scoped to `:kernel:language` only.
- Scope the grammar strictly to AD-110's parity list; do not add rules for `import`, expressions, or macro-use syntax.
- Keep the smoke test under `kernel/language/src/test/kotlin/...`, importing only the generated grammar types, never modifying the existing handwritten-parser tests.

### Architecture Compliance

- The story is only successful if later Epic 2 work can point to one clean ladder: ANTLR grammar -> generated lexer/parser (internal) -> Story `2.2` parse-tree adapter -> existing authored AST -> unchanged lowering.
- Prevent these failure modes:
  - generated ANTLR types leaking into `AthenaLanguageModel.kt` or any other module's public API
  - grammar rules that accept syntax the handwritten parser does not (silent scope creep beyond AD-110)
  - wiring the compiler onto the ANTLR path before Story `2.2` exists, which would break story boundaries and risk an unreviewed regression

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Add `org.antlr:antlr4:4.13.2` (tool, `antlr` configuration) and `org.antlr:antlr4-runtime:4.13.2` (implementation dependency) — the only new third-party dependency this story introduces.
- Do not add a Kotlin ANTLR wrapper library; consume the generated Java API directly from Kotlin, as `kernel/language` already does for other JVM interop.

### File Structure Requirements

- Expected new/updated files:
  - `gradle/libs.versions.toml` (add `antlr` version plus `antlr` and `antlr-runtime` library aliases)
  - `kernel/language/build.gradle.kts` (apply the `antlr` plugin, add the tool and runtime dependencies)
  - `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/*.g4` (grammar file(s) scoped to the parity subset; combined or split lexer/parser grammar is an implementation choice)
  - `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt` (or a similarly scoped new test file)
- Do not touch `AthenaLanguageParser.kt`, `AthenaLanguageModel.kt`, or `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` in this story.

### Testing Requirements

- Minimum verification should target the module directly first:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
- Recommended regression after the module tests pass:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:language:test :kernel:compiler:test"`
- Keep Gradle verification sequential on Windows; never overlap `gradlew` invocations. If a parallel run already corrupted build state, run `.\\gradlew.bat --no-daemon --console=plain clean` first, then rerun sequentially.
- Run `powershell -ExecutionPolicy Bypass -File .\\tools\\encoding-audit.ps1` after touching any documentation.

### Explicit Non-Goals

- No adaptation of ANTLR parse trees into the authored AST yet (Story `2.2`).
- No change to `AthenaLanguageParser`, `AthenaCompiler`, or the active compiler parse path yet.
- No span/diagnostic parity work on the ANTLR path yet (Story `2.3`).
- No `import`, expression-language, or macro-use grammar rules.
- No Tree-sitter grammar work (Epic 3).

### Previous Milestone Intelligence

- M16 established the pattern of landing one narrow, focused module change per story rather than a combined "big bang" migration; Epic 2 should mirror that discipline for the parser swap.
- The existing `kernel/language` tests already encode the exact parity surface (`examples/m0/demo-cabinet.athena`, qualified-name arity checks, string/identifier scalar values); reuse that fixture rather than inventing a new one.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/language/build.gradle.kts]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: examples/m0/demo-cabinet.athena]

## Dev Agent Record

### Agent Model Used

Composer (Auto)

### Debug Log References

- Added `antlr`/`antlr-runtime` `4.13.2` catalog entries and Gradle `antlr` plugin to `:kernel:language`.
- Used AntlrTask `packageName` property (Gradle 9 preferred over raw `-package` argument).
- Smoke test initially called nonexistent `sourceFile.declaration()`; fixed to `systemDecl().declaration()`.
- Confirmed no ANTLR imports in `AthenaLanguageModel.kt` / `AthenaLanguageParser.kt` facade; only smoke test and generated sources use `language.antlr`.

### Completion Notes List

- Published combined `Athena.g4` covering AD-110 parity subset with BOM skip and keyword-as-ident parity with handwritten tokenizer.
- Port/connect names enforced as exactly two dotted parts via `twoPartName`.
- Active compiler path still uses handwritten `AthenaLanguageParser`; no Story `2.2` adapter yet.
- Verified: `:kernel:language:test` PASS; `:kernel:compiler:test --tests AthenaCompilerTest` PASS.

### File List

- `gradle/libs.versions.toml`
- `kernel/language/build.gradle.kts`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`
- `_bmad-output/implementation-artifacts/m17/2-1-publish-the-antlr4-grammar-for-the-current-supported-syntax-subset.md`
- `_bmad-output/implementation-artifacts/m17/sprint-status.yaml`

### Change Log

- 2026-07-14: Published ANTLR4 grammar + smoke tests; compiler path untouched; status → done.

## Story Completion Status

- Status: done
- Completion note: ANTLR4 `4.13.2` grammar recognizes today's supported Athena syntax via generated internal types only. Facade/compiler path unchanged pending Story `2.2`.
