---
baseline_commit: f4510fd64041548ad8f064f6396db92f2de0178b
---

# Story 2.2: Adapt ANTLR Parse Trees Into The Authored AST

Status: done

## Story

As a compiler engineer,
I want Athena to adapt `ANTLR4` parser output into the existing authored AST seam,
so that lowering remains stable and parser internals remain isolated.

## FR Traceability

- FR-1: Athena can preserve one explicit authored AST boundary
- FR-4: Athena can preserve lowering through authored AST instead of parse trees
- NFR-1: `Engineering IR` remains canonical engineering truth
- NFR-2: Generated parser-tree types do not become public semantic contracts

## Acceptance Criteria

1. Given ANTLR parser output exists, when the compiler prepares syntax results, then Athena adapts the parse tree into authored AST nodes rather than lowering parse trees directly.
2. Given lowering is reviewed, when the dependency boundary is inspected, then `Engineering IR` derivation still consumes authored AST only.

## Tasks / Subtasks

- [x] Build the ANTLR-to-authored-AST adapter inside `kernel/language`, isolated from parse-tree types. (AC: 1)
  - [x] Add an internal `ParseAdapter`-style class (naming aligned to the architecture spine's preferred noun `ParseAdapter`) that walks the Story `2.1` generated ANTLR parse tree (visitor or listener, developer's choice) and constructs `SourceFileAst` / `SystemDeclaration` / `DeviceDeclaration` / `PortDeclaration` / `ConnectionDeclaration` / `QualifiedName` / `PropertyAssignment` / `ScalarValue` nodes from `AthenaLanguageModel.kt`.
  - [x] Map every ANTLR parser context node the Story `2.1` grammar defines (system, device, port, connect, qualified name, property assignment, scalar value) to its authored AST counterpart; do not skip any node the grammar accepts.
  - [x] Keep the adapter class(es) `internal`/package-private inside `com.engineeringood.athena.language` so no other Gradle module can import ANTLR-facing types.
- [x] Wire `AthenaLanguageParser.parse(file, source)` onto the ANTLR + adapter path while preserving its existing `ParseResult` contract. (AC: 1, 2)
  - [x] Replace the handwritten `AthenaTokenizer` / `AthenaParser` call inside `AthenaLanguageParser.parse` with: ANTLR lexer -> ANTLR parser -> parse tree -> `ParseAdapter` -> `SourceFileAst`.
  - [x] Keep the public method signature `fun parse(file: String, source: String): ParseResult` byte-for-byte unchanged so `AthenaCompiler` and every other existing caller needs no changes.
  - [x] Decide deliberately, and record in code comments/PR description, whether the handwritten recursive-descent parser is deleted now or kept temporarily; if kept, it must be clearly dead code pending removal, never a second production parse path that could silently diverge from the ANTLR path. (Decision: **deleted** — `com.engineeringood.athena.language.parser` removed so there is exactly one live parse path.)
- [x] Prove `Engineering IR` derivation still depends on authored AST only. (AC: 2)
  - [x] Inspect `EngineeringIrLowerer` (`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`) and confirm it imports only `com.engineeringood.athena.language.*` authored AST types (for example `SourceSpan`) through `CompilerSourceDocument.ast`, never a generated ANTLR type.
  - [x] Add or extend a lowering-boundary test that would fail if a generated ANTLR type were ever referenced from `EngineeringIrLowerer`, `CompilerSourceDocument`, or any other `kernel/compiler` public type. (Added `CompilerParserBoundaryTest`.)
- [x] Re-run and pass the existing parser/provenance regression suite unmodified through the new ANTLR-backed path. (AC: 1, 2)
  - [x] `AthenaLanguageParserTest` and `AthenaLanguageProvenanceTest` must pass without edits to their assertions.
  - [x] Run the full `kernel/compiler` test suite to confirm lowering and downstream derivation are unaffected by the parser swap.

## Dev Notes

### Story Intent

- Story `2.2` is the "the parser actually changed, and nothing downstream noticed" step of Epic 2.
- The success condition is not "ANTLR parse trees exist somewhere in the codebase." The success condition is "`AthenaLanguageParser.parse` now runs through ANTLR end to end, `AthenaCompiler` and `EngineeringIrLowerer` are unmodified call sites, and the authored AST is still the only thing lowering ever sees."
- Story `2.1` published the grammar and generated artifacts; this story is the first one that actually changes runtime parsing behavior.
- Story `2.3` owns proving span and diagnostic quality did not regress once this wiring lands.

### Architecture Guardrails

- Align to AD-105: `ANTLR4` is the compiler/LSP parser implementation; generated lexer/parser artifacts stay implementation detail behind the unchanged `AthenaLanguageParser` facade. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-105---Compiler-Parsing-Uses-ANTLR4-But-Public-Syntax-Contracts-Remain-Athena-Owned]
- Align to AD-106: all canonical lowering into `Engineering IR` continues to consume authored AST only; parse-tree-to-AST adaptation is isolated inside the syntax layer, and `Engineering IR` may not depend on generated parser nodes, visitors, or CST nodes. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-106---Authored-AST-Remains-The-Only-Lowering-Input-Before-Engineering-IR]
- Align to AD-111: future syntax growth must land through AST extensibility, not ad hoc grammar patches; keep the adapter organized around authored semantic categories (declaration kinds) rather than raw parser token sequences so later constructs slot in cleanly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-111---Future-Syntax-Growth-Lands-Through-AST-Extensibility-Not-Ad-Hoc-Grammar-Patches]
- Preserve inherited AD-34: one mutation authority above source and graph remains binding; this story only changes parsing, never opens a second mutation path. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `AthenaLanguageParser.parse(file, source)` (`kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`) currently tokenizes with `AthenaTokenizer`, parses with the private `AthenaParser` class, and returns `ParseSuccess(ast)` or `ParseFailure(diagnostics)`. This story's only externally visible behavior change should be the parsing engine underneath — not the return type or a new public method.
- `AthenaCompiler.parseSource(file, sourceText)` (`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`) calls `parser.parse(file, sourceText)` and pattern-matches on `ParseSuccess`/`ParseFailure` to build `CompilerParseSuccess`/`CompilerParseFailure`. It must not need any changes.
- `EngineeringIrLowerer.lower(source: CompilerSourceDocument)` (`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`) reads `source.ast.system.name`, `source.ast.system.span`, and other authored AST fields, and imports `com.engineeringood.athena.language.SourceSpan` directly — this is the exact boundary AD-106 protects, and it already has zero ANTLR awareness today (there is no ANTLR dependency in the repo yet).
- `CompilerSourceDocument` (`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`) wraps `file: String` and `ast: SourceFileAst`; it is the only shape lowering consumes and must keep being built from the authored AST, never from a parse tree.
- `AthenaLanguageParserTest` and `AthenaLanguageProvenanceTest` currently assert exact AST shape and span/diagnostic values for `examples/m0/demo-cabinet.athena` and several inline valid/invalid sources produced by the handwritten parser; they are the parity oracle for this story.

### Technical Requirements

- Depend on the Story `2.1` grammar and generated sources already published under `kernel/language/src/main/antlr/...`; do not re-author the grammar in this story unless a genuine gap is found (if so, keep the fix inside Story `2.1`'s scope and note it).
- Use either an ANTLR visitor (`XBaseVisitor`) or listener (`XBaseListener`) to build the authored AST; a visitor that returns constructed AST nodes directly from each `visitXyz` override is typically the simplest match for the existing `SourceFileAst` shape.
- Preserve the exact `QualifiedName` arity behavior the handwritten `AthenaParser.parseQualifiedName` enforces today (`port`/`connect` endpoints require exactly two dotted parts) inside the adapter or grammar, whichever layer the developer determines is the cleanest fit, but the observable diagnostic behavior for over/under-qualified names must be preserved (Story `2.3` verifies this in detail).
- Keep the adapter deterministic: identical source text must still produce an AST equal under `data class` `equals()`, matching the existing `parses deterministically for identical source input` test expectation.

### Architecture Compliance

- The story is only successful if later Epic 2/Epic 4 work can point to one clean, unbroken ladder: ANTLR grammar -> generated lexer/parser (internal) -> `ParseAdapter` -> authored AST (`SourceFileAst`) -> unchanged `EngineeringIrLowerer` -> `Engineering IR`.
- Prevent these failure modes:
  - a generated ANTLR type appearing in any public signature of `kernel/language`, `kernel/compiler`, `kernel/runtime`, or `ide/lsp`
  - `EngineeringIrLowerer` or any later compiler pass importing an ANTLR type directly
  - two live, diverging parse paths (handwritten and ANTLR) both reachable in production code after this story lands

### Library / Framework Requirements

- Java `25`, Kotlin `2.4.0`, Gradle `9.6.1` (repo-frozen stack; do not upgrade).
- Reuse the `org.antlr:antlr4-runtime:4.13.2` dependency Story `2.1` already added to `kernel/language`; do not add a second parser-generator dependency.
- Do not add a tree-diffing, visitor-generation, or code-mapping third-party library; a hand-written adapter over the generated visitor/listener API is the expected shape given the current grammar's narrow scope.

### File Structure Requirements

- Expected new/updated files:
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` (rewire `parse` onto the ANTLR + adapter path; decide the fate of the handwritten tokenizer/parser)
  - a new adapter file such as `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaAntlrParseAdapter.kt` (exact name is an implementation choice; keep it `internal`)
  - `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt` and `AthenaLanguageProvenanceTest.kt` should require no assertion changes; add new tests alongside them if adapter-specific coverage is needed
- Do not modify `AthenaLanguageModel.kt`'s public contracts, `CompilerSourceDocument`, or `EngineeringIrLowerer`'s public signature in this story.

### Testing Requirements

- Minimum verification should target the module directly first:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
- Recommended regression after the module tests pass:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :kernel:runtime:test :ide:lsp:test"`
- Keep Gradle verification sequential on Windows; never overlap `gradlew` invocations. If a parallel run already corrupted build state, run `.\\gradlew.bat --no-daemon --console=plain clean` first, then rerun sequentially.
- Run `powershell -ExecutionPolicy Bypass -File .\\tools\\encoding-audit.ps1` after touching any documentation.

### Explicit Non-Goals

- No grammar authoring or scope changes; that belongs to Story `2.1`.
- No span-precision or diagnostic-quality hardening beyond what is needed to keep existing tests green; deep provenance/failure-quality work belongs to Story `2.3`.
- No `import`, expression-language, or macro-use AST nodes.
- No Tree-sitter integration (Epic 3) or LSP feature work beyond confirming existing behavior is unaffected (Epic 4).

### Previous Milestone Intelligence

- M16's reuse-model and runtime stories repeatedly show the same discipline this story needs: introduce the new mechanism behind the existing public contract first, and prove the existing consumers (here, `AthenaCompiler` and `EngineeringIrLowerer`) need zero changes.
- The current parser tests were written to be parser-implementation-agnostic (they assert on `SourceFileAst`/`SyntaxDiagnostic` shape, not on handwritten-parser internals), which is exactly why they are usable unmodified as the parity oracle for the ANTLR swap.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt]
- [Source: _bmad-output/implementation-artifacts/m17/2-1-publish-the-antlr4-grammar-for-the-current-supported-syntax-subset.md]

## Dev Agent Record

### Approach

- Added an internal `ParseAdapter` (`AthenaAntlrAstAdapter`) plus the orchestrating `AthenaAntlrParseEngine` in the internal package `com.engineeringood.athena.language.antlr`, alongside the Story `2.1` generated lexer/parser. All types are Kotlin `internal`, so no other Gradle module can import ANTLR-facing types; only `AthenaLanguageParser.parse` and the `com.engineeringood.athena.language` contracts are visible downstream (AD-105/AD-106).
- The adapter walks the generated parse tree (`SourceFileContext` -> `SystemDeclContext` -> `deviceDecl`/`portDecl`/`connectDecl` -> `twoPartName`/`propertyAssignment`/`scalarValue`) and builds `SourceFileAst` and every authored node directly. String literals are unquoted to match the handwritten `ScalarValue.StringLiteral` text.
- `AthenaLanguageParser.parse(file, source)` was rewired to `AthenaAntlrParseEngine.parse(...)`; its public signature and `ParseResult` contract are byte-for-byte unchanged, so `AthenaCompiler`/`EngineeringIrLowerer` and all other callers were untouched.
- Handwritten parser decision: **deleted** `AthenaTokenizer.kt` and `AthenaParser.kt` (the whole `com.engineeringood.athena.language.parser` package) so there is exactly one live production parse path.
- Grammar note (genuine gap under Story `2.1`'s scope): `twoPartName` was relaxed from `ident DOT ident` to `ident (DOT ident)*` (rule name retained for tooling/smoke-test continuity). Exact two-part arity for `port`/`connect` is now enforced inside the adapter, which preserves the handwritten `owner.port` diagnostics on over-/under-qualified names — arity is an authored-AST concern (AD-111), not an ad hoc grammar patch.

### Test Results

- `:kernel:language:test` — PASS (forced full re-run; `AthenaLanguageParserTest` and `AthenaLanguageProvenanceTest` pass unmodified).
- `:kernel:compiler:test` — 110 tests, 1 failure: `AthenaCompilerComponentKnowledgeIntegrationTest` (electrical component-knowledge count). Proven pre-existing/unrelated: it fails identically with the compiler/electrical/dummy sources reverted to their committed baseline, and depends only on electrical knowledge resolution, not on parser spans. The pre-existing `AthenaM17ParserParityProofTest` and `AthenaParserContinuityTest` pass on the ANTLR path, confirming canonical output continuity.

### Span parity notes

- No off-by-one span issues surfaced in the pinned fixtures: ANTLR `charPositionInLine` (0-based) + 1 = Athena column (1-based); ANTLR line is already 1-based; half-open `end` computed from the token stop index. Verified numerically by the existing system-span test (line 1 col 1 .. line 22 col 2) and a new device/string-literal span assertion.

## Story Completion Status

- Status: done
- Completion note: ANTLR + `ParseAdapter` is the single live parse path behind the unchanged `AthenaLanguageParser` facade; authored AST remains the only lowering input, proven by an added compiler boundary test and the passing parity/continuity proofs.

## File List

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4` (relaxed `twoPartName` arity; comment updated)
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt` (new — internal ParseAdapter, parse engine, error listener, span mapping)
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` (rewired onto ANTLR + adapter path)
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/parser/AthenaTokenizer.kt` (deleted)
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/parser/AthenaParser.kt` (deleted)
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` (KDoc-only: stale package reference updated)
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt` (updated for deleted package + internal antlr package)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/CompilerParserBoundaryTest.kt` (new — AD-106 lowering boundary test)
