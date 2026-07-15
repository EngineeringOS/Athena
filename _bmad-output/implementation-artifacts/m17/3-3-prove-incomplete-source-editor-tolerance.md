---
baseline_commit: f4510fd64041548ad8f064f6396db92f2de0178b
---

# Story 3.3: Prove Incomplete-Source Editor Tolerance

Status: done

## Story

As an IDE engineer,
I want Tree-sitter-backed syntax UX to stay usable on incomplete source,
so that the editor remains responsive while the compiler path still owns semantic failure.

## FR Traceability

- FR-6: Athena can use Tree-sitter for syntax UX rather than semantic truth.
- FR-8: Athena can preserve useful failure behavior on invalid source.
- NFR-4: Editor syntax parsing remains error tolerant and low latency.
- NFR-3: Source spans and diagnostics remain inspectable across parser migration.

## Acceptance Criteria

1. Given a source file is incomplete (e.g. an unclosed `system`/`device`/`port` block, a dangling `connect` missing its `->` target, or a file that ends mid-declaration while actively being typed) or malformed (e.g. an unterminated string literal, a stray character), when Story `3.1`'s grammar parses it through `web-tree-sitter`, then it still returns a non-null `Tree` whose `rootNode` covers the full input range, and the tree contains at least the well-formed prefix as real (non-`ERROR`) nodes rather than the whole document collapsing into one opaque error node.
2. Given that same tree, when Story `3.2`'s Tree-sitter-backed semantic tokens provider runs `queries/highlights.scm` over it, then the well-formed prefix still receives correct token classifications (keywords, identifiers, strings), and the malformed/incomplete suffix degrades to at most unclassified/default tokens — it never throws, never blanks out the entire previously-highlighted document, and never crashes the Monaco editor session.
3. Given the exact same incomplete/malformed source is submitted through the compiler/LSP path (`AthenaLanguageParser` -> `AthenaCompiler` -> `AthenaLanguageFeatures.semanticInspection`/diagnostics), when semantic failure is inspected, then it is still reported as a typed, provenance-rich `SyntaxDiagnostic`/`CompilerSyntaxDiagnostic` from the compiler path exactly as today — Tree-sitter's tolerant tree must never be mistaken for, or substituted as, that semantic failure signal.
4. Given both proofs are run together on the same fixture set, when the story's automated tests execute, then one shared, checked-in set of incomplete/malformed `.athena`-shaped fixtures is used by both the Tree-sitter-side test and the compiler-side test (not two independently invented fixture sets that could silently drift), and the tests explicitly assert the two paths disagree in exactly the expected way: Tree-sitter stays usable, the compiler still fails.

## Tasks / Subtasks

- [ ] Publish one small, shared incomplete/malformed fixture set reused by both proof sides. (AC: 4)
  - [ ] Add narrow fixtures under `ide/tree-sitter-athena/test/incomplete/` (e.g. `unclosed-system.athena.txt`, `unclosed-device-block.athena.txt`, `dangling-connect.athena.txt`, `unterminated-string.athena.txt`), each representing one incomplete/malformed shape distinct from the others.
  - [ ] Keep this fixture set intentionally narrow and scoped to proving editor tolerance; do not attempt to build the full milestone-wide invalid/incomplete corpus here — that broader, checked-in `examples/m17/` corpus is Epic 5 Story `5.2`'s explicit responsibility. If useful, this story's fixtures may later be folded into that corpus by Story `5.2` without needing to be duplicated now.
  - [ ] Reuse `examples/m0/invalid-direction-cabinet.athena` (syntactically valid, semantically invalid) as one control fixture proving the two-path split even on a case where Tree-sitter's tree is fully clean and only the compiler fails — this sharpens the contrast the story must prove.
- [ ] Prove Tree-sitter-side tolerance against the fixture set. (AC: 1, 2)
  - [ ] Extend `ide/tree-sitter-athena`'s Node test suite (from Story `3.1`) with a new test file (e.g. `scripts/athena-tree-sitter-incomplete-source.test.mjs`) that parses every fixture under `test/incomplete/` and asserts: `tree.rootNode` is non-null, `tree.rootNode.startIndex === 0` and `endIndex` covers the full source length, and the well-formed prefix (e.g. the already-typed `system X {` line) is reachable as a real named node rather than being absorbed into a single top-level `ERROR` node.
  - [ ] Extend `ide/theia-frontend`'s highlighting-service test (from Story `3.2`, `athena-tree-sitter-highlighting-service.test.mjs`) with cases feeding the same incomplete fixtures into the semantic tokens provider and asserting it returns token data without throwing, and that the well-formed prefix still resolves the expected keyword/identifier/string classifications.
  - [ ] Add or extend one Electron smoke assertion (reusing/extending Story `3.2`'s smoke script) that types (or loads) an incomplete document into a live editor and confirms the editor keeps rendering highlighted text rather than freezing, throwing, or reverting to fully unhighlighted plain text.
- [ ] Prove the compiler/LSP path still owns semantic failure on the identical fixtures. (AC: 3, 4)
  - [ ] Add a focused JVM test (e.g. in a new `AthenaLanguageIncompleteSourceTest.kt` alongside `AthenaLanguageParserTest.kt`/`AthenaLanguageProvenanceTest.kt`) that feeds the same fixture text (read from the shared fixtures, or an equivalent Kotlin-side copy kept byte-identical and cross-checked) into `AthenaLanguageParser().parse(...)` and asserts a `ParseFailure` with a provenance-rich `SyntaxDiagnostic` (non-empty `message`, correct `file`/`line`/`column`), exactly matching today's failure behavior — this story must not change tokenizer/parser behavior.
  - [ ] Add or extend a compiler-level test proving `AthenaCompiler` surfaces the equivalent `CompilerSyntaxDiagnostic` for the same fixture, and that `AthenaLanguageFeatures.semanticInspection(...)` in `ide/lsp` still reports `status = "parse-failure"` with the diagnostic summaries populated, exactly as it does today for `CompilerCompilationParseFailure`.
  - [ ] Explicitly assert (in a comment and in the test name) that this compiler-side failure is independent of and unaffected by Tree-sitter's tolerant parse of the same text, proving the "semantic failure still comes from compiler/LSP, not Tree-sitter" boundary from AD-108 with a real, runnable test rather than only a documentation claim.
- [ ] Cross-check that the shared fixtures actually exercise both a "different outcome, same input" case and a "both agree the input is fine syntactically, only semantics fails" case. (AC: 4)
  - [ ] For each fixture under `test/incomplete/`, document (in a short comment or a small fixture-index table in the new test file) which of the two proof sides is expected to notice a problem: Tree-sitter tree stays usable / clean vs. compiler fails with a specific diagnostic message fragment.
  - [ ] For `invalid-direction-cabinet.athena`, document that both sides parse cleanly at the syntax level, and only the compiler's semantic validation (not syntax parsing) flags the direction mismatch — reinforcing that this fixture is a semantic, not syntactic, counter-example, and keeping the boundary between "Tree-sitter tolerance" and "compiler semantic failure" sharp.
- [ ] Keep Story `3.3` narrow. (AC: 1, 2, 3, 4)
  - [ ] Do not modify `ide/tree-sitter-athena/grammar.js`, `queries/highlights.scm`, or the Story `3.2` provider-registration code beyond what is strictly needed to add the new tests described above.
  - [ ] Do not change `AthenaLanguageParser`/`AthenaLanguageModel` tokenizer or parser behavior; this story is a proof story, not a hardening story — if a genuine gap is found (e.g. a case where Tree-sitter's tree becomes unusable, or the compiler crashes instead of emitting a diagnostic), record it as an explicit follow-up rather than silently patching parser internals under this story's scope.
  - [ ] Do not build the full Epic 5 invalid/incomplete proof corpus under `examples/m17/`; keep this story's fixtures narrow and additive to that later, broader corpus.

## Dev Notes

### Story Intent

- Story `3.3` is the "prove the boundary held" story for Epic 3: Stories `3.1` and `3.2` built and wired a real Tree-sitter path; this story is where Athena stops asserting AD-107/AD-108 in prose and starts asserting them with a runnable test that watches both paths react to the exact same broken input differently, and correctly.
- The success condition is not "the editor doesn't crash on bad input" alone. It is "one shared fixture set proves, side by side, that Tree-sitter stays usable for syntax UX and the compiler/LSP path still independently owns semantic/syntax failure reporting for the same text" — a dual-path proof, not a single-path robustness check.
- This story deliberately reuses the highlighting path built in Story `3.2` rather than inventing a second Tree-sitter integration surface just for tolerance testing; if the highlighting service from `3.2` cannot survive these fixtures without changes, that is a signal `3.2`'s adapter was not built resiliently enough, not a reason to build a parallel adapter here.
- Epic 5 (`5.1`/`5.2`) owns the milestone-wide, broader parser-parity and invalid/incomplete proof corpus under `examples/m17/`; this story's fixtures are intentionally small and can be reused or folded into that later corpus, but this story does not attempt to be that corpus itself.

### Architecture Guardrails

- Align to AD-107: Tree-sitter owns syntax UX only. Story `3.3` proves the flip side of that rule concretely: even when the input is broken, Tree-sitter must keep behaving like a syntax-UX aid (usable tree, usable highlighting) and must never be asked to declare the document semantically valid or invalid. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-107---Tree-sitter-Owns-Syntax-UX-Only]
- Align to AD-108: `ide/lsp` remains the sole semantic entry point, and syntax/semantic diagnostics continue to derive from compiler-owned parsing. Story `3.3`'s core deliverable is a test that would fail if this boundary were ever violated (e.g. if some future change made the frontend trust Tree-sitter's "no error" tree as a signal that the document is semantically fine). [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-108---LSP-Semantic-Diagnostics-Stay-On-The-Compiler-Parser-Path]
- Align to AD-109: parser migration (and, by extension, parser addition) must preserve provenance and failure quality; invalid source should fail as typed compiler diagnostics, not opaque crashes or lost-position messages. Story `3.3`'s compiler-side tests assert exactly this provenance (`file`/`line`/`column`/`message`) on the shared fixtures. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-109---Parser-Migration-Must-Preserve-Provenance-And-Failure-Quality]
- Align to AD-110: the first M17 proof stays parity-first on the current supported syntax subset. Story `3.3`'s incomplete/malformed fixtures must be minimal perturbations of that same subset (an unclosed `system`/`device`/`port` block, a dangling `connect`, an unterminated string) rather than incomplete forms of speculative future syntax. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-110---The-First-M17-Proof-Stays-Parity-First-On-The-Current-Supported-Syntax-Subset]
- Align to AD-112: IDE integration remains additive to the existing product path. Story `3.3` adds tests around the existing Story `3.1`/`3.2` artifacts; it introduces no new adapter, widget, or seam of its own. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-112---IDE-Integration-Remains-Additive-To-The-Existing-Product-Path]
- Align to AD-113: repository-backed proof inputs remain stronger than inline-only parser demos. This story's checked-in `test/incomplete/*` fixtures plus the reused `examples/m0/invalid-direction-cabinet.athena` file are the concrete AD-113 artifacts for the "malformed/incomplete" half of the M17 proof story. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-113---Repository-Backed-Proof-Inputs-Remain-Stronger-Than-Inline-Only-Parser-Demos]

### Current Code State To Preserve

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt` already fails fast on malformed input (unterminated string, unexpected character, missing token) and returns a `ParseFailure` with exactly one `SyntaxDiagnostic` carrying `file`, `line`, `column`, `message`, `span` — see the existing `reports syntax diagnostics with file line and column provenance` test in `AthenaLanguageParserTest.kt` for the established shape and style to match.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` already converts `ParseFailure`'s `SyntaxDiagnostic`s into `CompilerSyntaxDiagnostic`s and returns `CompilerParseFailure` / `CompilerCompilationParseFailure` (see `parseSource(...)` around the compiler's `ParseResult` handling). This story's compiler-side test must exercise this existing path, not a new one.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`'s `semanticInspection(uri)` already branches on `CompilerCompilationParseFailure` to report `status = "parse-failure"` with `diagnosticsCount`/`diagnosticSummaries` built from those diagnostics; this is the existing LSP-side surface this story's compiler/LSP proof should target rather than inventing a new inspection payload.
- `examples/m0/invalid-direction-cabinet.athena` already exists as a real, syntactically valid but semantically invalid fixture (a `connect` between two `in`-direction ports). It is currently used to prove compiler/semantic validation behavior; this story reuses it as-is to prove the sharpest possible contrast case (Tree-sitter and the handwritten parser both see clean syntax; only compiler-level semantic validation fails).
- Story `3.1`'s grammar is required to already be tolerant "by construction" (relying on Tree-sitter's built-in error recovery rather than custom error productions) — this story is where that claim gets its first real test. If Story `3.1` was implemented as specified, no grammar changes should be necessary here.
- Story `3.2`'s highlighting service is required to already degrade gracefully on load/parse failure (per its Technical Requirements) — this story is where that resilience claim gets its first real test. If Story `3.2` was implemented as specified, no adapter changes should be necessary here beyond adding test cases.

### Technical Requirements

- Keep the shared fixture set physically small (four to six short files) and named so each file's failure mode is obvious from its filename alone (e.g. `unclosed-system.athena.txt`, `dangling-connect.athena.txt`).
- Use a `.athena.txt` (or equivalent non-`.athena`) extension for the intentionally-broken fixtures living under `ide/tree-sitter-athena/test/incomplete/` so they are clearly test fixtures and are not mistaken for real, buildable `.athena` source by any tooling that globs `**/*.athena`.
- On the Kotlin side, either read the exact same fixture text from the shared location (if the JVM test can conveniently resolve a path under `ide/tree-sitter-athena/test/incomplete/` relative to the repo root, following the existing `resolveRepoRoot()` helper pattern already used in `AthenaLanguageParserTest.kt`/`AthenaLanguageProvenanceTest.kt`) or embed the identical string as a Kotlin triple-quoted literal with a code comment cross-referencing the JS-side fixture file by name, so the two sides cannot silently drift apart.
- Do not introduce a new test framework on either side: keep `kotlin.test` (`Test`, `assertIs`, `assertEquals`, `assertTrue`) on the JVM side and `node --test` on the Node/TypeScript side, matching every existing test file already read during this story's preparation.

### Architecture Compliance

- The story is only successful if a future reviewer can read one test (or one small, clearly-named group of tests) and see, in the same run, both "Tree-sitter stayed usable" and "the compiler still failed correctly" for the same broken input — not two disconnected test suites that happen to both exist.
- Prevent these failure modes:
  - A Tree-sitter fixture that is technically "incomplete" but happens to still fully satisfy the grammar (making the test meaningless); each fixture must be checked to actually produce at least one `ERROR`/`MISSING` node region, or in the unclosed-block case, an unterminated tree, when parsed.
  - A compiler-side test that only checks "it doesn't crash" instead of asserting the specific typed `SyntaxDiagnostic`/`CompilerSyntaxDiagnostic` provenance fields.
  - Silently loosening the compiler's failure behavior (e.g. making a currently-failing case now "recover" and produce a document) to make a test pass more easily — that would be an undocumented behavior change and is explicitly out of scope.
  - Letting the frontend highlighting service's error-handling path swallow a genuinely broken `.wasm` load without any signal at all (it must degrade to Monarch-only, not silently do nothing while claiming success).

### Library / Framework Requirements

- No new dependency in this story on either the JVM or Node/TypeScript side; it exercises the exact toolchains already introduced by Stories `3.1` (`tree-sitter-cli`, `web-tree-sitter`) and `3.2` (the highlighting service, Electron smoke scripts), plus the pre-existing JVM `kotlin.test` stack.
- Reuse `AthenaLanguageParser`, `AthenaCompiler`, and `AthenaLanguageFeatures` exactly as they exist today; this story is proof-only.

### File Structure Requirements

- New files:
  - `ide/tree-sitter-athena/test/incomplete/unclosed-system.athena.txt`
  - `ide/tree-sitter-athena/test/incomplete/unclosed-device-block.athena.txt`
  - `ide/tree-sitter-athena/test/incomplete/dangling-connect.athena.txt`
  - `ide/tree-sitter-athena/test/incomplete/unterminated-string.athena.txt`
  - `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
  - `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageIncompleteSourceTest.kt`
- Updated files:
  - `ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs` (from Story `3.2`; add incomplete-source cases)
  - the Electron smoke script added/extended in Story `3.2` (add one incomplete-source assertion)
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/...` (extend the existing compiler test file that already covers parse-failure propagation, if one exists, rather than creating a redundant new file — confirm the exact existing file name during implementation by reading `kernel/compiler/src/test/kotlin/...` first)
- Files that must remain behaviorally unchanged by this story:
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `ide/tree-sitter-athena/grammar.js`
  - `ide/theia-frontend/src/browser/athena-tree-sitter-highlighting-service.ts`

### Testing Requirements

- `npx tree-sitter test` and `yarn --cwd ide/tree-sitter-athena test` (extended with the new incomplete-source test file).
- `yarn workspace @engineeringood/athena-theia-frontend test` (extended highlighting-service test cases).
- The extended/added Electron smoke script under `ide/theia-product/scripts/` via its `yarn start:smoke:...` entry.
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:language:test"` followed, only after that succeeds, by `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"` and `:ide:lsp:test` if an LSP-level assertion is added — run these strictly sequentially, never in parallel, per the workspace Gradle rule.
- Required proof checks across the whole story:
  - every fixture under `test/incomplete/` yields a usable (non-null, full-range) Tree-sitter tree with a real, non-`ERROR` well-formed prefix;
  - the Tree-sitter-backed highlighting path does not throw and does not blank the document on any of those fixtures;
  - the identical fixture text still produces a typed, provenance-rich compiler/LSP `SyntaxDiagnostic`/`CompilerSyntaxDiagnostic` failure;
  - `examples/m0/invalid-direction-cabinet.athena` parses cleanly on both the Tree-sitter side and the handwritten-parser side, and only compiler-level semantic validation flags it.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` if any documentation is touched while recording follow-ups.

### Explicit Non-Goals

- No grammar changes in `ide/tree-sitter-athena/grammar.js` (file a follow-up instead, if a genuine gap is found).
- No tokenizer/parser behavior changes in `kernel/language`.
- No new frontend UX surface beyond what Story `3.2` already built.
- No attempt to build the full Epic 5 invalid/incomplete proof corpus under `examples/m17/`.
- No change to how `ide/lsp` reports diagnostics to the frontend; this story only adds tests around the existing reporting path.
- No relaxing of any existing compiler failure behavior to make a new test easier to pass.

### Previous Milestone Intelligence

- M0's `examples/m0/invalid-*.athena` fixtures (`invalid-direction-cabinet.athena`, `invalid-semantic-cabinet.athena`, `duplicate-identity-cabinet.athena`) already established the pattern of pairing every fixture with a clear single failure reason and, where relevant, an `.expectation.txt` sidecar; this story's new `test/incomplete/*` fixtures follow the same one-fixture-one-failure-mode discipline, scaled down to syntax-level rather than semantic-level breakage.
- M17 Story `1.1` already proved that a "freeze the contract, then prove it with focused tests" story can stay narrow and evidence-driven without touching production parser code; Story `3.3` follows the same discipline on the dual-parser boundary specifically.
- The M17 addendum's explicit reminder (section `8`) that verification should include "at least one valid-source and one invalid/incomplete-source proof input" for both the compiler and IDE sides is the direct source of this story's dual-path fixture-sharing requirement.

### Latest Technical Information

- `web-tree-sitter`'s incremental/error-tolerant parsing behavior (via `Tree-sitter`'s built-in error-recovery grammar mode, not custom error rules) is a core, stable library guarantee as of the `web-tree-sitter` `^0.26` line already adopted in Stories `3.1`/`3.2`; no additional library research is required specifically for this story beyond what those stories already captured.
- No new external research is required for the JVM side; the `kotlin.test` assertion API (`assertIs`, `assertEquals`, `assertTrue`) already in use across `kernel/language`/`kernel/compiler` test suites is sufficient.

### Project Structure Notes

- This story should be readable as "the receipt" for Epic 3's central claim: Tree-sitter stays usable, the compiler still fails correctly, and neither path was quietly merged into the other.
- Keep the fixture set and its two consuming test suites close together in spirit (same failure-mode naming on both sides) even though they physically live in different runtimes (`ide/tree-sitter-athena` + `ide/theia-frontend` vs. `kernel/language`/`kernel/compiler`/`ide/lsp`), since that pairing is the entire point of the story.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m17/3-1-publish-the-tree-sitter-grammar-for-athena-syntax-ux.md]
- [Source: _bmad-output/implementation-artifacts/m17/3-2-integrate-tree-sitter-into-one-supported-theia-syntax-ux-path.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: examples/m0/invalid-direction-cabinet.athena]
- [Source: examples/README.md]

## Story Completion Status

- Status: done
- Completion note: Story `3.3` closed on 2026-07-15. The shared incomplete/malformed fixtures now pass through the Tree-sitter grammar package, the frontend highlighting-service test exercises the tolerant syntax-UX path, `yarn --cwd ide start:smoke:tree-sitter` asserts the packaged product still produces tokens for an incomplete M17 proof fixture, and the compiler/LSP-side failure ownership remains covered by the passing M17 language/compiler/LSP verification suite.
