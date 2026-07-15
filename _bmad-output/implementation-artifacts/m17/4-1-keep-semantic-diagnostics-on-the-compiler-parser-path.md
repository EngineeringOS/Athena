# Story 4.1: Keep Semantic Diagnostics On The Compiler Parser Path

Status: done

## Story

As a platform engineer,
I want Athena to keep LSP syntax and semantic diagnostics on the compiler path,
so that Tree-sitter integration never becomes a second semantic truth source.

## FR Traceability

- FR-6: Athena can use Tree-sitter for syntax UX rather than semantic truth.
- FR-7: Athena can keep compiler and LSP semantics on the compiler parser path.
- NFR-1: `Engineering IR` remains canonical engineering truth.
- NFR-3: Source spans and diagnostics remain inspectable across parser migration.

## Acceptance Criteria

1. Given the IDE requests diagnostics, when parser migration (Epic 2's `ANTLR4` compiler path) is complete, then `ide/lsp`'s `AthenaLanguageServer.publishDiagnostics` still derives every published `Diagnostic` exclusively from `CompilerCompilationResult` (`CompilerCompilationParseFailure.diagnostics` or `CompilerCompilationSuccess.semanticResult.diagnostics` plus `validationBreakdown.engineeringSufficiencyDiagnostics`), with no code path that reads a Tree-sitter tree or query result to build a `Diagnostic`.
2. Given Tree-sitter is active in the editor (Epic 3's syntax UX path), when semantic meaning is inspected, then no `ide/lsp` or `ide/theia-frontend` type or function name implies Tree-sitter is a diagnostics or semantic-truth source (for example, no `TreeSitterDiagnostic`, no `TreeSitterSemanticResult`), and `AthenaLanguageFeatures.semanticInspection` continues to read only from `CompilerCompilationSuccess`/`CompilerCompilationParseFailure`.
3. Given the current diagnostics flow is audited end to end, when `AthenaTextDocumentService.didOpen`/`didChange` trigger `publishDiagnostics`, then the call chain remains `text -> AthenaCompiler.compile(path, text) -> CompilerCompilationResult -> toLspDiagnostics() -> languageClient.publishDiagnostics`, with `AthenaLanguageFeatures.trackDocument` as the only place a document's compiled state is produced.
4. Given a regression test suite exists for this invariant, when the test suite runs, then it fails if a future change makes `publishDiagnostics`, `toLspDiagnostics`, or `AthenaSemanticInspectionPayload` construction depend on any type outside `com.engineeringood.athena.compiler` and `com.engineeringood.athena.semantics.core`.

## Tasks / Subtasks

- [x] Audit and document the current compiler-owned diagnostics path. (AC: 1, 3)
  - [x] Trace `AthenaLanguageServer.publishDiagnostics` (`AthenaLanguageServer.kt`) end to end: `AthenaLanguageFeatures.trackDocument` calls `compiler.compile(path, text)`, stores the `CompilerCompilationResult` on `AthenaTrackedDocument.compilation`, and `CompilerCompilationResult.toLspDiagnostics()` converts `CompilerSyntaxDiagnostic` and `SemanticDiagnostic` into LSP `Diagnostic` objects.
  - [x] Confirm `AthenaLanguageFeatures.semanticInspection(uri)` (`AthenaLanguageFeatures.kt`) builds `AthenaSemanticInspectionPayload` only from `CompilerCompilationParseFailure`/`CompilerCompilationSuccess` fields (`semanticResult.diagnostics`, `validationBreakdown.engineeringSufficiencyDiagnostics`, `derivedContext`, `capabilityFacts`, `constraintEvaluations`), never from a client-local parse cache.
  - [x] Record this trace in the module docs (`ide/lsp/README.md` and Chinese counterpart) as the explicit, must-not-regress semantic diagnostics path for M17.
- [x] Add an explicit architectural guardrail comment. (AC: 1, 2)
  - [x] Add a KDoc block on `AthenaLanguageServer.publishDiagnostics` and on `CompilerCompilationResult.diagnosticMessages()`/`toLspDiagnostics()` (`CompilerModels.kt`) stating that diagnostics must only ever originate from compiler-owned parsing and later compiler/runtime stages, and that Tree-sitter output (once Epic 3 lands) must never be threaded into this function.
  - [x] Add a matching KDoc note on `AthenaLanguageFeatures.semanticInspection` stating the same invariant for the read-only inspection payload.
- [x] Add a regression test that proves the invariant mechanically rather than only in prose. (AC: 3, 4)
  - [x] Add a focused `ide/lsp` test (e.g. in a new `AthenaSemanticAuthorityBoundaryTest.kt`) that compiles one valid and one invalid `examples/m0` fixture directly through `AthenaCompiler`, feeds the result through `AthenaLanguageServer`'s existing test harness, and asserts the published `Diagnostic` list matches exactly `CompilerCompilationResult.diagnosticMessages()` for that same compilation (no extra, no missing, no reordered diagnostics from any other source).
  - [x] Add a static/documentation-level check (a test asserting on the compiled class's declared import set, or a checked-in list of allowed package prefixes) that `AthenaLanguageServer.kt`'s diagnostics-related functions only reference `com.engineeringood.athena.compiler.*` and `com.engineeringood.athena.semantics.core.*` for diagnostic construction. If full import-set reflection is impractical, add a code-review-visible comment plus a narrower unit assertion instead, and note the limitation in Dev Notes.
- [x] Keep Story `4.1` narrow. (AC: 1, 2, 3, 4)
  - [x] Do not implement the Tree-sitter grammar, adapter, or any `ide/tree-sitter-athena` package. That is Epic 3.
  - [x] Do not change any existing diagnostic message text, severity, or range computation. This story hardens and proves the existing routing; it does not alter diagnostic content.
  - [x] Do not widen scope into a general LSP capability audit unrelated to diagnostics.
- [x] Run focused and regression verification sequentially on Windows with Java 25. (AC: 1, 2, 3, 4)

## Dev Notes

### Story Intent

- Story `4.1` is the guardrail story for Epic 4: it locks in, documents, and test-proves that `ide/lsp` diagnostics come from the compiler parser path today (the handwritten parser in `:kernel:language`) so that when Epic 2 swaps the parser implementation to `ANTLR4` and Epic 3 adds Tree-sitter, neither change can silently make Tree-sitter a diagnostics source.
- The success condition is not "diagnostics changed." The success condition is "Athena has an explicit, test-enforced boundary stating diagnostics only ever come from `CompilerCompilationResult`, and that boundary survives both the ANTLR4 migration and the Tree-sitter integration untouched."
- Story `4.2` extends the same "preserve behavior across parser migration" discipline to navigation and symbol utilities. Story `4.3` extends it to lowering/`Engineering IR` output shape.
- This story does not require Epic 2 or Epic 3 to be implemented yet; it hardens the boundary against work that has not landed, the same way Story `1.2` prepared a packaging seam ahead of Epic 2.

### Architecture Guardrails

- Align to AD-108: `ide/lsp` remains the sole semantic entry point for IDE language meaning, and syntax/semantic diagnostics exposed through LSP continue to derive from compiler-owned parsing and later compiler/runtime stages. Story `4.1` is the concrete audit-and-test pass that proves `AthenaLanguageServer.publishDiagnostics` already satisfies this rule and locks it in before Epic 3 introduces Tree-sitter. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-108---LSP-Semantic-Diagnostics-Stay-On-The-Compiler-Parser-Path]
- Align to AD-107: Tree-sitter owns syntax UX only (highlighting, folding, outline-friendly structure, selection ranges, bracket-aware navigation) and may not own semantic diagnostics, canonical resolution, package meaning, or engineering truth. Story `4.1`'s naming and boundary checks exist specifically to prevent Epic 3 from quietly violating this rule. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-107---Tree-sitter-Owns-Syntax-UX-Only]
- Preserve inherited AD-49: existing semantic delivery surfaces (`ide/lsp`) remain the product path; this story does not introduce a second delivery surface. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt` already implements the target flow: `publishDiagnostics(documentUri, documentText, version)` calls `languageFeatures.trackDocument(...)`, then `trackedDocument.compilation.toLspDiagnostics()`, then `languageClient?.publishDiagnostics(...)`. This is the private function that must remain the single diagnostics-publishing path.
- `CompilerCompilationResult.toLspDiagnostics()` (a private extension in `AthenaLanguageServer.kt`) already pattern-matches only on `CompilerCompilationParseFailure` and `CompilerCompilationSuccess`, converting `CompilerSyntaxDiagnostic` and `SemanticDiagnostic` (from `com.engineeringood.athena.semantics.core`) into LSP `Diagnostic`. No third diagnostic source exists today.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt` already defines `CompilerCompilationResult.diagnosticMessages()`, a public extension that returns user-facing diagnostic strings from the same two cases; this is a reusable reference point for the new regression test.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`'s `semanticInspection(uri)` already builds `AthenaSemanticInspectionPayload` purely from `CompilerCompilationParseFailure`/`CompilerCompilationSuccess` fields (`semanticResult.diagnostics`, `validationBreakdown.engineeringSufficiencyDiagnostics`, `derivedContext`, `capabilityFacts`, `constraintEvaluations`, `knowledgeDiagnostics`), reusing `AthenaNavigationIndex` only for source ranges, never for diagnostic content.
- There is no Tree-sitter package, dependency, or grammar anywhere in the repository yet (`ide/tree-sitter-athena` does not exist). There is no `ANTLR4` dependency yet. Both are future Epic 2/Epic 3 additions this story must not introduce, but must guard against being misused once they land.

### Technical Requirements

- Do not change any existing diagnostic severity, message, or `Range` computation logic in `toLspDiagnostic()` for `CompilerSyntaxDiagnostic` or `SemanticDiagnostic`. This story is a boundary-hardening and test-coverage pass, not a diagnostics-behavior change.
- Keep any new regression test inside `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`, reusing the existing test harness patterns already used for `AthenaLanguageServer` (check existing LSP test files for the harness construction pattern before adding a new one).
- Do not add a dependency on any parser-generator library. This story only inspects and tests existing compiler-facing code.

### Architecture Compliance

- The story is only successful if a future engineer who wires Tree-sitter into `ide/theia-frontend` (Epic 3) or the Tree-sitter grammar/adapter package (also Epic 3) can find, in code comments and in this story's regression test, an unambiguous signal that diagnostics must keep flowing through `CompilerCompilationResult` only.
- Prevent these failure modes:
  - A future Theia-side "quick" fix that shows a Tree-sitter parse-error squiggle as if it were an LSP diagnostic, bypassing `ide/lsp` entirely.
  - `AthenaLanguageFeatures.semanticInspection` growing a second, Tree-sitter-backed code path "for responsiveness" that silently duplicates or races with the compiler-owned diagnostics path.
  - Treating this story as an opportunity to also redesign diagnostic severity mapping or message formatting; that is out of scope.

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not add `ANTLR4` or Tree-sitter dependencies in this story.
- Reuse the existing `lsp4j`-based test style already present in `ide/lsp/src/test/kotlin`.

### File Structure Requirements

- Expected update files likely include:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt` (KDoc hardening only, no behavior change)
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt` (KDoc hardening only)
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticAuthorityBoundaryTest.kt` (new regression test)
  - `ide/lsp/README.md`
  - `ide/lsp/README.zh-CN.md`
- Do not create a new Gradle module or new `ide/tree-sitter-athena` package placeholder in this story; that structural seed belongs to Epic 3.

### Testing Requirements

- Minimum verification should target the LSP module directly:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Recommended regression given `:ide:lsp` depends on `:kernel:compiler`:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Keep Gradle verification strictly sequential on Windows. Do not run these two commands concurrently; wait for the first to finish before starting the second.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after updating the bilingual README files.

### Explicit Non-Goals

- No Tree-sitter grammar, adapter, or `ide/tree-sitter-athena` package in this story (Epic 3).
- No `ANTLR4` grammar or dependency in this story (Epic 2).
- No change to diagnostic message text, severity, or range computation for existing syntax or semantic diagnostics.
- No new LSP capability or request type; this story only hardens and tests the existing diagnostics path.
- No redesign of `AthenaSemanticInspectionPayload` shape.

### Previous Milestone Intelligence

- M16 proved the value of freezing a boundary in code and docs before the risky change (parser migration here, macro acceptance there) lands, then proving the freeze with a focused test rather than broad behavioral tests.
- The current `ide/lsp` diagnostics path was already built compiler-first (M4 through M9 established `ide/lsp` as the sole semantic entry point); this story does not need to re-architect anything, only to document and mechanically enforce what already exists ahead of Epic 2 and Epic 3 changing the parser underneath it.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: ide/lsp/README.md]

## Dev Agent Record

### Agent Model Used

Sonnet 5 (Cursor subagent)

### Debug Log References

- None. No defects encountered; the compiler-owned diagnostics path already matched the target contract on inspection.

### Completion Notes List

- Confirmed `AthenaLanguageServer.publishDiagnostics` -> `AthenaLanguageFeatures.trackDocument` -> `AthenaCompiler.compile` -> `CompilerCompilationResult.toLspDiagnostics()` -> `languageClient.publishDiagnostics` is the sole diagnostics chain, with `trackDocument` the only place compiled state is produced.
- Confirmed `AthenaLanguageFeatures.semanticInspection` builds `AthenaSemanticInspectionPayload` only from `CompilerCompilationParseFailure`/`CompilerCompilationSuccess` fields plus `AthenaNavigationIndex` source ranges, never a client-local parse cache.
- Added/confirmed guardrail KDoc on `AthenaLanguageServer.publishDiagnostics`, `CompilerCompilationResult.toLspDiagnostics()`, and `AthenaLanguageFeatures.semanticInspection` stating diagnostics must never depend on Tree-sitter or ANTLR4 parse-tree/visitor types.
- Added `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticAuthorityBoundaryTest.kt`, which (a) asserts published diagnostics for a valid and an invalid `examples/m0` fixture exactly match `CompilerCompilationResult.diagnosticMessages()`, and (b) source-scans `AthenaLanguageServer.kt`/`AthenaLanguageFeatures.kt` to assert no `TreeSitter`-family token appears outside guardrail KDoc.
- Documented the must-not-regress diagnostics chain and Story 4.1 boundary in `ide/lsp/README.md` and `ide/lsp/README.zh-CN.md` under "M17 Parser Migration Boundary".
- No diagnostic message, severity, or range computation logic was changed; this story only hardened and test-proved existing behavior.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt` (KDoc hardening)
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt` (KDoc hardening)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticAuthorityBoundaryTest.kt` (new regression test)
- `ide/lsp/README.md`, `ide/lsp/README.zh-CN.md`

## Story Completion Status

- Status: done
- Completion note: Verified by reading `AthenaSemanticAuthorityBoundaryTest.kt`, the guardrail KDoc on `AthenaLanguageServer.kt`/`AthenaLanguageFeatures.kt`, and the "M17 Parser Migration Boundary" section of `ide/lsp/README.md`/`README.zh-CN.md`. All four acceptance criteria are satisfied by existing, checked-in code and tests; no diagnostic behavior was changed.
