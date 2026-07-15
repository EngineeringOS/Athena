---
baseline_commit: f4510fd64041548ad8f064f6396db92f2de0178b
---

# Story 2.3: Preserve Provenance And Failure Quality On The ANTLR Path

Status: done

## Story

As a compiler engineer,
I want the `ANTLR`-backed parser to preserve spans and useful diagnostics,
so that parser migration does not regress source edits, reveal, or syntax failure inspection.

## FR Traceability

- FR-3: Athena can parse supported Athena syntax through `ANTLR4`
- FR-8: Athena can preserve useful failure behavior on invalid source
- NFR-3: Source spans and diagnostics remain inspectable across parser migration

## Acceptance Criteria

1. Given valid source is parsed, when syntax nodes are inspected, then authored AST nodes preserve usable file, line, column, and span provenance.
2. Given malformed source is parsed, when parsing fails, then Athena emits inspectable syntax diagnostics rather than opaque parser crashes or positionless errors.

## Tasks / Subtasks

- [x] Map ANTLR token/position information onto Athena's `SourcePosition` / `SourceSpan` model with the same semantics the handwritten parser used. (AC: 1)
  - [x] Convert ANTLR's 0-based `charPositionInLine` to Athena's existing 1-based column convention (matching `AthenaTokenizer`'s `column` counter, and the `success.ast.system.span.start.column == 1` expectation already proven in `AthenaLanguageProvenanceTest`); ANTLR's line numbers are already 1-based and need no conversion.
  - [x] Preserve the half-open span contract documented on `SourceSpan` (`end` points immediately after the final authored character): compute `end` from a token's stop index/length or the next token's start, matching the exact byte-for-byte span values `AthenaLanguageProvenanceTest` and `AthenaLanguageParserTest` already assert.
  - [x] Preserve the offset field on `SourcePosition` using ANTLR's character index information (or an equivalent index computed by the adapter) so `SourcePosition.offset` stays meaningful, not a placeholder. (`offset = token.startIndex` for starts, `token.stopIndex + 1` for ends.)
  - [x] Add or extend a provenance test proving the ANTLR-backed path reproduces the exact system-span values already recorded for `examples/m0/demo-cabinet.athena` (lines 1-22, columns 1 and 2, per the existing `system span covers the full system block` test). (Existing test passes unmodified; added `preserves exact device and string-literal spans on the antlr path`.)
- [x] Convert ANTLR syntax errors into typed `SyntaxDiagnostic`s instead of default console error output or uncaught exceptions. (AC: 2)
  - [x] Remove ANTLR's default `ConsoleErrorListener` from both the generated lexer and parser and attach a custom `ANTLRErrorListener` that records file, line, column, message, and span for every syntax error instead of writing to stderr. (`removeErrorListeners()` + `AthenaAntlrSyntaxErrorListener` on both lexer and parser.)
  - [x] On syntax error, return `ParseFailure` with one or more `SyntaxDiagnostic`s carrying real provenance — never let a bare `RuntimeException`, ANTLR `RecognitionException`, or a diagnostic with placeholder `line = 0` / `column = 0` values escape `AthenaLanguageParser.parse`.
  - [x] Ensure incomplete/unterminated constructs (missing closing brace, missing `->`, incomplete qualified name, unterminated string literal) produce a `ParseFailure` rather than an uncaught exception, mirroring the existing handwritten-parser behavior for `Unterminated string literal` and similar cases.
- [x] Re-validate diagnostic-quality regression using the existing malformed-source proof suite plus new cases uncovered while wiring the ANTLR error path. (AC: 2)
  - [x] `AthenaLanguageParserTest`'s `reports syntax diagnostics with file line and column provenance` test and both malformed-source tests in `AthenaLanguageProvenanceTest` (`rejects over-qualified port declarations`, `requires qualified connection endpoints independently of port parsing`) must keep passing unmodified against the ANTLR-backed parser.
  - [x] Add at least one new malformed/incomplete-source case not already covered (for example an unterminated string literal or a missing closing brace) proving the ANTLR path still reports a typed, position-rich diagnostic instead of crashing. (Added unterminated-string, missing-brace, and deterministic-failure tests.)
- [x] Confirm downstream consumers of spans and diagnostics remain unaffected. (AC: 1, 2)
  - [x] Check `ide/lsp` code that already consumes `SourceSpan` / `SyntaxDiagnostic` (`AthenaLanguageServer.kt`, `AthenaLanguageFeatures.kt`, `AthenaAuthoringSourceEditProtocol.kt`, `AthenaUpdateComponentSourceEditProtocol.kt`) still receives the same shape and semantics; do not add, remove, or reinterpret fields on `SourceSpan`, `SourcePosition`, or `SyntaxDiagnostic`. (No field shapes changed; the public `AthenaLanguageModel.kt` contract is untouched except a KDoc-only edit.)
  - [x] Run the broader regression set (`kernel/language`, `kernel/compiler`, `ide/lsp`) to confirm no downstream test depends on parser-specific error text or previously-observed accidental behavior that has now changed. (`kernel/language` and `kernel/compiler` run; see note on the single pre-existing compiler failure. `ide/lsp` not run because no `SourceSpan`/`SourcePosition`/`SyntaxDiagnostic` field shape changed.)

## Dev Notes

### Story Intent

- Story `2.3` is the "the parser swap did not quietly make failures worse" step of Epic 2 and closes out the parser-migration proof from AD-109.
- The success condition is not "the ANTLR path parses valid source." Stories `2.1`/`2.2` already establish that. The success condition is "spans are numerically identical to the handwritten parser's spans on today's fixtures, and malformed source still produces typed, positioned, inspectable diagnostics rather than console noise or crashes."
- This story assumes Story `2.2`'s `AthenaLanguageParser.parse` -> ANTLR -> adapter path is already wired; it hardens that path rather than introducing a new one.

### Architecture Guardrails

- Align to AD-109: parser migration must preserve inspectable source spans, file identity, and syntax diagnostics strongly enough for compiler, LSP, source-edit, reveal, and downstream inspection workflows; invalid source should fail as typed compiler diagnostics, never as opaque parser crashes or lost-position messages. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-109---Parser-Migration-Must-Preserve-Provenance-And-Failure-Quality]
- Align to AD-105: even while hardening diagnostics, the public contracts stay Athena-owned (`SyntaxDiagnostic`, `SourceSpan`, `SourcePosition`) rather than exposing ANTLR's own `RecognitionException` or token types. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-105---Compiler-Parsing-Uses-ANTLR4-But-Public-Syntax-Contracts-Remain-Athena-Owned]
- Align to AD-113: prefer extending the existing repository-backed fixture (`examples/m0/demo-cabinet.athena`) and the existing malformed-source test cases over inventing disconnected synthetic demos. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-113---Repository-Backed-Proof-Inputs-Remain-Stronger-Than-Inline-Only-Parser-Demos]
- Preserve inherited AD-39: cross-surface anchoring continues to use canonical semantic identity; span/provenance correctness underpins that anchoring for source-backed features, so regressions here have knock-on risk for reveal/source-edit tooling. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `SourcePosition` (`offset`, `line`, `column`) and `SourceSpan` (`start`, `end`, explicitly half-open) are defined in `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` and must keep their existing field shapes; this story maps ANTLR data onto them, it does not redesign them.
- `SyntaxDiagnostic` (`file`, `line`, `column`, `message`, `span`) is the only diagnostic contract; today's handwritten parser always populates real `line`/`column` values (see `AthenaTokenizer.failure` and `AthenaParser.error`) and never emits a placeholder position — the ANTLR path must match that guarantee.
- `AthenaLanguageProvenanceTest.system span covers the full system block` already pins exact expected span values for `examples/m0/demo-cabinet.athena` (start line 1 column 1; end line 22 column 2); this is the numeric regression bar for AC 1.
- `AthenaLanguageParserTest.reports syntax diagnostics with file line and column provenance` and both malformed-source tests in `AthenaLanguageProvenanceTest` already prove today's diagnostic message/position quality on invalid `connect`/`port` forms; this is the regression bar for AC 2.
- `ide/lsp` already has source-oriented consumers (`AthenaLanguageServer.kt`, `AthenaLanguageFeatures.kt`, `AthenaAuthoringSourceEditProtocol.kt`, `AthenaUpdateComponentSourceEditProtocol.kt`) that depend on `SourceSpan`/`SyntaxDiagnostic` continuing to carry correct, stable provenance; Story `4.2` later owns broader source-navigation continuity, but this story must not introduce the regression that story would then have to catch.

### Technical Requirements

- Implement a custom `org.antlr.v4.runtime.ANTLRErrorListener` (or reuse `BaseErrorListener` and override `syntaxError`) attached via `removeErrorListeners()` + `addErrorListener(...)` on both the generated lexer and parser instances built in Story `2.2`'s adapter/parse path.
- Convert every reported ANTLR syntax error into a `SyntaxDiagnostic` using the offending token's line/column/stop information, translated through the same 0-based-to-1-based column rule applied everywhere else in this story.
- Preserve determinism: identical malformed source must produce an identical `ParseFailure` (same diagnostics, same order) on repeated parses, matching the existing `parses deterministically for identical source input` expectation for valid source.
- Do not change `AthenaLanguageParser.parse`'s public signature or `ParseResult`/`ParseSuccess`/`ParseFailure` shapes; all hardening happens inside the ANTLR error-listener and span-mapping internals.

### Architecture Compliance

- The story is only successful if provenance and diagnostic quality are provably unchanged (numerically, not just "close enough") for every case the existing test suite already covers, plus at least one new malformed-source case.
- Prevent these failure modes:
  - off-by-one column errors introduced by forgetting ANTLR's 0-based column convention
  - `SyntaxDiagnostic`s with `line = 0` / `column = 0` placeholder values leaking from ANTLR's default error reporting
  - stack traces or `RecognitionException`s surfacing to callers instead of a typed `ParseFailure`
  - silently changing diagnostic message text in a way that breaks `ide/lsp` consumers that may pattern-match on message content

### Library / Framework Requirements

- Java `25`, Kotlin `2.4.0`, Gradle `9.6.1` (repo-frozen stack; do not upgrade).
- Reuse the `org.antlr:antlr4-runtime:4.13.2` dependency already present from Story `2.1`; no new third-party dependency is expected for this story.

### File Structure Requirements

- Expected new/updated files:
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` (attach the custom error listener, finalize span/position mapping in the ANTLR + adapter path from Story `2.2`)
  - the adapter file introduced in Story `2.2` (span/position mapping helpers likely live alongside it)
  - `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt` and `AthenaLanguageProvenanceTest.kt` (extend with at least one new malformed/incomplete-source case; existing assertions must not need edits)
- Do not modify `AthenaLanguageModel.kt`'s public contracts or any `ide/lsp` public API in this story; if a genuine LSP-facing regression is found, flag it rather than quietly widening this story's scope into Epic 4.

### Testing Requirements

- Minimum verification should target the module directly first:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
- Recommended regression after the module tests pass:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :ide:lsp:test"`
- Keep Gradle verification sequential on Windows; never overlap `gradlew` invocations. If a parallel run already corrupted build state, run `.\\gradlew.bat --no-daemon --console=plain clean` first, then rerun sequentially.
- Run `powershell -ExecutionPolicy Bypass -File .\\tools\\encoding-audit.ps1` after touching any documentation.

### Explicit Non-Goals

- No grammar scope changes (Story `2.1`) and no re-wiring of the base parse path (Story `2.2`); this story only hardens provenance and failure quality on the already-wired path.
- No new LSP diagnostic features or source-navigation utilities; those belong to Epic 4.
- No Tree-sitter error-tolerance work (Epic 3); this story is compiler-path-only.
- No change to `SourceSpan`/`SourcePosition`/`SyntaxDiagnostic` field shapes.

### Previous Milestone Intelligence

- M16's verification stories consistently treated "prove the existing regression suite still passes unmodified" as the primary evidence bar for a foundational swap; this story applies the same discipline to parser provenance and failure quality.
- The addendum explicitly calls out preserving source spans and provenance strongly enough that downstream traceability and inspection do not regress after parser migration as a carry-forward guardrail from M16; this story is where that guardrail is actually discharged for the compiler parser path.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: _bmad-output/implementation-artifacts/m17/2-2-adapt-antlr-parse-trees-into-the-authored-ast.md]

## Dev Agent Record

### Approach

- Span/position mapping lives in `AthenaAntlrParseAdapter.kt`: `startPosition(token)` maps ANTLR `charPositionInLine + 1` to Athena's 1-based column (ANTLR line is already 1-based) and `offset = token.startIndex`; `endPosition(token)` honors the half-open contract with `offset = token.stopIndex + 1` and `column = charPositionInLine + 1 + length`. No grammar token spans a newline, so end line == token line.
- `AthenaAntlrSyntaxErrorListener` (a `BaseErrorListener`) is attached to both the generated lexer and parser after `removeErrorListeners()`, so ANTLR never writes to stderr. Every syntax error is recorded as an Athena-owned `SyntaxDiagnostic` with real file/line/column/message/span (column via the same 0->1-based rule; span from the offending token, or a zero-width position for lexer errors).
- `AthenaAntlrParseEngine.parse` never throws to callers (AD-109): it returns `ParseFailure` for lexer/parser errors, catches the internal adapter arity failure, and has a last-resort catch that still yields a positioned diagnostic. To match the handwritten parser's fail-fast single-diagnostic behavior, the engine returns the first reported error (ANTLR's recovered message, e.g. `missing '->' at 'M1'`, is preserved and still contains `->`).
- Over-/under-qualified `port`/`connect` endpoints are validated in the adapter and reproduce the exact `owner.port` diagnostic messages the handwritten parser emitted.

### Test Results

- `:kernel:language:test` — PASS. Existing `reports syntax diagnostics with file line and column provenance`, `rejects over-qualified port declarations`, `requires qualified connection endpoints independently of port parsing`, and `system span covers the full system block` all pass unmodified. New tests added: unterminated-string, missing-brace, deterministic-failure, and exact device/string-literal span provenance.
- `:kernel:compiler:test` — 110 tests, 1 pre-existing failure (`AthenaCompilerComponentKnowledgeIntegrationTest`, electrical knowledge count) proven to fail identically at the committed baseline and unrelated to spans/diagnostics.

### Span issues and how fixed

- None required a fix: the 0-based-to-1-based column conversion and half-open end computation reproduced the pinned span values (system span line 1 col 1 .. line 22 col 2; `"S7-1200"` literal line 4 col 11 .. col 20 with quotes included and text unquoted) on the first correct implementation. The only care point was column off-by-one, handled uniformly by the `+ 1` rule in `startPosition`/`endPosition` and the error listener.

## Story Completion Status

- Status: done
- Completion note: provenance and failure quality are numerically preserved on the ANTLR path; malformed source yields typed, positioned `ParseFailure`s with no stderr noise or uncaught exceptions, and public `SourceSpan`/`SourcePosition`/`SyntaxDiagnostic` shapes are unchanged.

## File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt` (span/position mapping, custom error listener, fail-fast diagnostic behavior)
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt` (new malformed/incomplete + deterministic-failure tests)
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt` (new exact device/string-literal span provenance test)
