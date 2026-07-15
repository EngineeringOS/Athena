---
baseline_commit: f4510fd64041548ad8f064f6396db92f2de0178b
---

# Story 3.1: Publish The Tree-sitter Grammar For Athena Syntax UX

Status: done

## Story

As an IDE engineer,
I want Athena to publish a Tree-sitter grammar for the current supported source subset,
so that editor syntax behavior has an incremental and error-tolerant parser foundation before any Theia wiring exists.

## FR Traceability

- FR-5: Athena can publish a Tree-sitter grammar for Athena source.
- FR-2: Athena can publish a dual-parser responsibility model (this story is the first concrete artifact of the editor-parser half of that split).
- NFR-2: Generated parser-tree types do not become public semantic contracts.
- NFR-4: Editor syntax parsing remains error tolerant and low latency.
- NFR-5: The M17 architecture must make future syntax additions cheaper, not harder.

## Acceptance Criteria

1. Given the current Athena source subset is reviewed, when the Tree-sitter grammar is implemented in `ide/tree-sitter-athena/grammar.js`, then it recognizes exactly the forms `AthenaLanguageParser`/`AthenaLanguageModel` already accept: one top-level `system <name> { ... }` block, `device`, `port`, and `connect` declarations, dotted qualified names (`owner.port`), string literals, bare identifiers, and `name value` property assignments — no more, no less.
2. Given real checked-in proof inputs are parsed, when the grammar is exercised against `examples/m0/demo-cabinet.athena`, `examples/m0/dual-drive-cabinet.athena`, `examples/m0/quoted-properties-cabinet.athena`, and at least one governed multi-file fixture such as `examples/m4/open-repository-proof/src/factory-line.athena`, then every file parses to a syntax tree with zero `ERROR`/`MISSING` nodes (`tree.rootNode.hasError` is `false`), proving grammar parity with the handwritten JVM parser on real source rather than synthetic inline snippets only.
3. Given the editor parser boundary is reviewed, when grammar ownership is inspected, then `ide/tree-sitter-athena/` contains only syntax-structure artifacts (grammar rules, generated parser, compiled `.wasm`, `queries/highlights.scm`, and tests) and contains no semantic validation, no reference resolution, no `Engineering IR` derivation, and no dependency on `kernel/language`, `kernel/compiler`, or any other JVM module.
4. Given the package is meant to be consumed later, when the build and test scripts are inspected, then `yarn --cwd ide/tree-sitter-athena build` deterministically regenerates the parser and `.wasm` binary from `grammar.js`, `yarn --cwd ide/tree-sitter-athena test` proves grammar correctness through both the standard Tree-sitter test corpus and a `web-tree-sitter`-driven parity script over the real fixtures from AC 2, and the package publishes bilingual `README.md`/`README.zh-CN.md` documentation stating the AD-107 syntax-only boundary explicitly.

## Tasks / Subtasks

- [ ] Scaffold the `ide/tree-sitter-athena/` package per the architecture spine's Structural Seed. (AC: 3, 4)
  - [ ] Create `ide/tree-sitter-athena/package.json` named `@engineeringood/athena-tree-sitter-grammar`, following the existing sibling-package pattern used by `integrations/graph-glsp/package.json` (private, MIT, `clean`/`build`/`test` scripts, its own `yarn.lock`) rather than joining the `ide/package.json` Theia-only workspaces array.
  - [ ] Add `tree-sitter-cli` and `web-tree-sitter` as `devDependencies`. Pin versions current at authoring time (`tree-sitter-cli` >= `0.26.1`, `web-tree-sitter` `^0.26`) so `tree-sitter build --wasm` uses the auto-downloaded `wasi-sdk` path and needs no Docker/Emscripten on this Windows repo.
  - [ ] Add a narrow module marker/README stating this package is NOT part of the `ide/theia-*` yarn workspaces and is linked into `ide/theia-frontend` only in Story `3.2`.
- [ ] Author `grammar.js` for exactly the current supported syntax subset. (AC: 1, 3)
  - [ ] Define `grammar({ name: 'athena', ... })` producing rules mirroring `AthenaLanguageModel.kt` 1:1: `source_file` -> one `system_declaration`; `system_declaration` -> `'system' identifier block`; `declaration` -> `device_declaration | port_declaration | connect_declaration`; `device_declaration`/`port_declaration` -> keyword, name/qualified_name, `{` `property_assignment*` `}`; `connect_declaration` -> `qualified_name '->' qualified_name`; `qualified_name` -> `identifier ('.' identifier)*`; `property_assignment` -> `identifier (identifier | string)`.
  - [ ] Do not add grammar rules for constructs the current JVM parser does not accept (no comments, no numeric literals, no expressions, no `import`). Add an explicit top-of-file comment in `grammar.js` stating the subset is frozen to AD-110's list and that widening the grammar is an explicit future-story decision, not an incidental addition.
  - [ ] Keep the grammar tolerant by construction: rely on Tree-sitter's built-in error-recovery (do not hand-roll custom error productions) so partial/incomplete input still yields a best-effort tree, which Story `3.3` will prove concretely.
- [ ] Generate and build the parser. (AC: 4)
  - [ ] Run `npx tree-sitter generate` to produce `src/parser.c`, `src/grammar.json`, `src/node-types.json`; check these generated files into the repository (standard Tree-sitter grammar convention) so consumers do not need `tree-sitter-cli` installed just to load the grammar.
  - [ ] Run `npx tree-sitter build --wasm` to produce `tree-sitter-athena.wasm` at the package root; check the compiled binary into the repository so Story `3.2`'s frontend code has a stable artifact to load without a build-time WASM toolchain dependency in Theia's own build.
  - [ ] Wire both commands into `package.json` `scripts.generate` / `scripts.build` so regeneration stays a documented, repeatable command rather than tribal knowledge.
- [ ] Publish a `queries/highlights.scm` syntax-highlighting query. (AC: 3)
  - [ ] Map `system`/`device`/`port`/`connect` keyword tokens to `@keyword`, qualified-name identifiers to `@property`/`@variable` as appropriate, string literals to `@string`, and braces/arrow/dot to `@punctuation.bracket`/`@operator`/`@punctuation.delimiter`.
  - [ ] Keep this file syntax-classification only; it must not encode any semantic rule (e.g. it must not try to distinguish a valid port reference from an invalid one — that stays compiler/LSP work).
  - [ ] Do not wire this query into Theia/Monaco yet; that integration is Story `3.2`'s responsibility.
- [ ] Prove grammar correctness with both native Tree-sitter tests and real-fixture parity tests. (AC: 1, 2, 4)
  - [ ] Add standard Tree-sitter corpus tests under `test/corpus/*.txt` (e.g. `system.txt`, `device.txt`, `port.txt`, `connect.txt`) covering each declaration form and the exact S-expression shape expected, runnable via `npx tree-sitter test`.
  - [ ] Add a Node test script (e.g. `scripts/athena-tree-sitter-grammar-corpus.test.mjs`, following the `node --test scripts/*.test.mjs` convention already used in `ide/theia-frontend` and `integrations/graph-glsp`) that loads `tree-sitter-athena.wasm` through `web-tree-sitter`, parses every fixture from AC 2 read from disk, and asserts `hasError === false` for each — this is the AD-113 repository-backed proof, not just inline grammar demos.
  - [ ] Do not remove, weaken, or duplicate the existing JVM parser tests in `kernel/language/src/test/kotlin/...`; this story adds a second, independent proof path over the same real fixtures, it does not replace the first.
- [ ] Add bilingual module documentation and keep encoding compliant. (AC: 4)
  - [ ] Write `ide/tree-sitter-athena/README.md` and `README.zh-CN.md` (UTF-8 with BOM for the Chinese file per the workspace encoding rule) describing scope, the AD-107 syntax-only boundary, how to regenerate/build, and how Story `3.2` will consume the compiled `.wasm`.
  - [ ] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after adding both README files.
- [ ] Keep Story `3.1` narrow. (AC: 1, 2, 3, 4)
  - [ ] Do not modify `ide/theia-frontend`, `ide/lsp`, `kernel/language`, or `kernel/compiler` in this story.
  - [ ] Do not introduce ANTLR4 or touch Epic 2 work; the ANTLR4 compiler-parser migration is a fully separate parser and separate module.
  - [ ] Do not add folding or outline queries beyond `highlights.scm` yet; only add them later if Story `3.2` specifically needs them for its chosen UX path.
  - [ ] Do not add the `import` keyword or any other speculative future syntax to the grammar.

## Dev Notes

### Story Intent

- Story `3.1` is the grammar-publish foundation for Epic 3, mirroring how M17 Story `1.1` froze the authored-syntax contract before any parser-generator work began: this story produces a real, independently testable Tree-sitter grammar package with zero IDE wiring.
- The success condition is not "Tree-sitter is visible in the editor." It is "Athena now has one real, versioned, buildable Tree-sitter grammar package that parses the exact current supported syntax subset — proven against real checked-in fixtures, not toy demos — with zero coupling to the Kotlin/JVM stack."
- Story `3.2` consumes the compiled `.wasm` and `highlights.scm` from this story to wire one concrete Theia syntax UX path.
- Story `3.3` reuses this same grammar to prove incomplete/malformed-source tolerance; the error-tolerant-by-construction requirement in this story's tasks exists specifically so Story `3.3` has something real to prove against.
- Epic 5 (`5.1`/`5.2`) later publishes the full milestone-wide parser-parity and invalid/incomplete corpus under `examples/m17/`; this story's own fixture reuse (AC 2) is deliberately narrow and must not be read as satisfying that broader Epic 5 corpus requirement.

### Architecture Guardrails

- Align to AD-107: Tree-sitter is introduced only for syntax-oriented editor behavior. Story `3.1` must produce a grammar and query set that stays scoped to tokenization/structure and never encodes resolution, diagnostics, or engineering meaning. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-107---Tree-sitter-Owns-Syntax-UX-Only]
- Align to AD-108: `ide/lsp` remains the sole semantic entry point. This story does not touch `ide/lsp` at all — that boundary is preserved by construction, not by a runtime check, precisely because the grammar package has no dependency on any compiler or LSP module. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-108---LSP-Semantic-Diagnostics-Stay-On-The-Compiler-Parser-Path]
- Align to AD-110: the first M17 editor-parser proof stays parity-first on the current supported subset (`system`, `device`, `port`, `connect`, qualified names, strings, property assignments). Story `3.1`'s grammar must match that list exactly, not a broader guess at future syntax. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-110---The-First-M17-Proof-Stays-Parity-First-On-The-Current-Supported-Syntax-Subset]
- Align to AD-112: IDE integration remains additive to the existing product path, and any Athena-owned grammar package remains subordinate to the current product structure. Story `3.1` places the grammar exactly where the Structural Seed says (`ide/tree-sitter-athena/`) and does not import into `kernel/*` in either direction. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-112---IDE-Integration-Remains-Additive-To-The-Existing-Product-Path]
- Align to AD-113: repository-backed proof inputs remain stronger than inline-only parser demos. Story `3.1`'s corpus proof (AC 2) must run against real `examples/m0/*` and `examples/m4/*` fixtures already used by the JVM parser tests, not fabricated one-off strings only. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-113---Repository-Backed-Proof-Inputs-Remain-Stronger-Than-Inline-Only-Parser-Demos]
- Align to AD-104: M17 freezes one language architecture before language breadth expands. Story `3.1` must resist the temptation to "improve" the grammar beyond today's syntax; any widening (e.g. comments, `import`) is a distinct, later, explicit decision. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-104---M17-Freezes-One-Language-Architecture-Before-Language-Breadth-Expands]
- Preserve inherited AD-82: DSL remains canonical serialization, not the default human interface. Publishing a better editor parser does not change that positioning; this story only improves editing ergonomics for the existing DSL surface. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` and `AthenaLanguageParser.kt` already define and implement the exact supported grammar this story must mirror in Tree-sitter form: one `system` block; `device`/`port`/`connect` declarations; dotted `QualifiedName`; `PropertyAssignment` with `Identifier`/`StringLiteral` scalar values; `->` connection arrow. Read both files end to end before writing `grammar.js` — do not infer the grammar from the PRD prose alone.
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt` and `AthenaLanguageProvenanceTest.kt` already parse `examples/m0/demo-cabinet.athena` and inline snippets and assert exact AST shape, determinism, and diagnostic provenance. This story does not touch these files; it adds an independent, cross-runtime proof of the same real fixtures.
- `examples/m0/demo-cabinet.athena`, `examples/m0/dual-drive-cabinet.athena`, and `examples/m0/quoted-properties-cabinet.athena` are real, already-used valid fixtures. `examples/m4/open-repository-proof/src/factory-line.athena` is a real governed multi-declaration fixture. Reuse these files by reading them from disk in the Node parity test; do not copy their contents into a second inline literal.
- There is no `ide/tree-sitter-athena/` directory, no ANTLR4 dependency, and no Tree-sitter dependency anywhere in the repository yet. `ide/package.json` workspaces currently list only `theia-product`, `theia-frontend`, `theia-backend`; `integrations/graph-glsp` is the existing precedent for a sibling package that stays outside that workspaces array and is instead consumed via a `link:` dependency by a workspace member — Story `3.2` will use the same pattern for this package.
- `ide/theia-frontend/src/browser/athena-language-definition.ts` already defines `ATHENA_LANGUAGE_ID = 'athena'` and a Monaco Monarch tokenizer (`athenaMonarchLanguage`) for the same keyword set this story's grammar targets. This story does not touch that file; Story `3.2` will decide how (or whether) Tree-sitter output complements it.

### Technical Requirements

- Use Node `>=22` and Yarn `1.22.22`, matching the rest of `ide/*` and the architecture spine's frozen stack table.
- Pin `tree-sitter-cli` to a version `>= 0.26.1` so `tree-sitter build --wasm` uses the `wasi-sdk` path and auto-downloads its own toolchain — this avoids requiring Docker, Podman, or a manually installed Emscripten toolchain on this Windows repository (older tree-sitter-cli releases required Emscripten/Docker for `--wasm`; that constraint no longer applies at the pinned version).
- Use `web-tree-sitter`'s current API surface for the Node parity test: `await Parser.init()`, `const language = await Language.load(wasmPath)`, `parser.setLanguage(language)`, `parser.parse(sourceText)`, `tree.rootNode.hasError`.
- Keep `grammar.js` dependency-free beyond `tree-sitter-cli`'s own toolchain; do not add a parser-combinator or template-expansion library.
- Do not add any Kotlin, Gradle, or JVM dependency to this package. It is a pure Node/TypeScript-adjacent package (grammar authoring is plain JS by Tree-sitter convention) with zero cross-runtime coupling.

### Architecture Compliance

- The story is only successful if Story `3.2` can point to one already-tested artifact pair (`tree-sitter-athena.wasm` + `queries/highlights.scm`) and start Theia wiring immediately, without first having to fix grammar gaps discovered mid-integration.
- Prevent these failure modes:
  - Grammar rules that silently accept syntax the JVM parser rejects (or vice versa), which would make "parity-first" a slogan rather than a tested fact.
  - Checking in only inline/synthetic corpus tests and skipping the real-fixture parity script, which would violate AD-113.
  - Letting `highlights.scm` encode a semantic judgment (e.g. "this qualified name looks like a valid port") instead of pure syntax classification.
  - Adding any import path from `ide/tree-sitter-athena` into `kernel/*` Kotlin sources, or from any `kernel/*` module into this package.
  - Treating this story as done once `grammar.js` exists, without also generating, building, and testing the compiled `.wasm` artifact Story `3.2` actually needs.

### Library / Framework Requirements

- `tree-sitter-cli` (devDependency, `>= 0.26.1`) — grammar authoring, `generate`, and `build --wasm`.
- `web-tree-sitter` (devDependency for the Node parity test in this story; Story `3.2` will add it as a runtime dependency of `ide/theia-frontend`).
- No Theia, Monaco, or Electron dependency belongs in this package; those are Story `3.2`'s concern.
- Follow the existing sibling-package pattern from `integrations/graph-glsp/package.json` for `scripts.clean`/`scripts.build`/`scripts.test` naming, even though this package's `build` step is grammar generation rather than `tsc`.

### File Structure Requirements

- New files:
  - `ide/tree-sitter-athena/package.json`
  - `ide/tree-sitter-athena/grammar.js`
  - `ide/tree-sitter-athena/src/parser.c` (generated)
  - `ide/tree-sitter-athena/src/grammar.json` (generated)
  - `ide/tree-sitter-athena/src/node-types.json` (generated)
  - `ide/tree-sitter-athena/tree-sitter-athena.wasm` (built)
  - `ide/tree-sitter-athena/queries/highlights.scm`
  - `ide/tree-sitter-athena/test/corpus/system.txt`
  - `ide/tree-sitter-athena/test/corpus/device.txt`
  - `ide/tree-sitter-athena/test/corpus/port.txt`
  - `ide/tree-sitter-athena/test/corpus/connect.txt`
  - `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
  - `ide/tree-sitter-athena/README.md`
  - `ide/tree-sitter-athena/README.zh-CN.md`
  - `ide/tree-sitter-athena/.gitignore` (mirroring `integrations/graph-glsp/.gitignore` for `node_modules`, but not for the checked-in generated parser or `.wasm`)
- Files that must remain untouched by this story:
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`
  - `ide/theia-frontend/src/browser/athena-language-definition.ts`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - `ide/package.json`

### Testing Requirements

- `npx tree-sitter test` (run from `ide/tree-sitter-athena/`) to verify the standard corpus in `test/corpus/*.txt`.
- `yarn --cwd ide/tree-sitter-athena test` to run the Node parity script (`node --test scripts/*.test.mjs`) proving `hasError === false` on every real fixture from AC 2.
- Keep the existing JVM suite green as an unrelated regression check: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:language:test"` (sequential, do not overlap with any other Gradle invocation; this story should not change JVM behavior at all, so this is a smoke check, not a target).
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after adding the bilingual READMEs.

### Explicit Non-Goals

- No Theia, Monaco, or `ide/theia-frontend` wiring in this story (Story `3.2`).
- No incomplete/malformed-source proof beyond "the grammar is tolerant by construction" (Story `3.3` owns the concrete proof).
- No ANTLR4 grammar or compiler-parser work (Epic 2).
- No folding-range or outline query beyond `highlights.scm`.
- No `import` keyword or other speculative future syntax.
- No changes to `kernel/language`, `kernel/compiler`, or `ide/lsp`.

### Previous Milestone Intelligence

- M17 Story `1.1` already established the pattern this story follows: freeze/publish one clean artifact with strong contract tests before any consumer wiring begins, and lean on real repository fixtures rather than narrative claims.
- M16 and earlier milestones consistently prove that keeping a new foundational package free of cross-module dependencies (no third-party libraries beyond what the task strictly needs, no reach into unrelated modules) keeps later integration stories fast; this story should follow that same discipline for `ide/tree-sitter-athena`.
- `ide/theia-frontend` and `integrations/graph-glsp` already prove the `node --test scripts/*.test.mjs` / `test/*.test.mjs` convention works well for this repository's Node-side verification; reuse it rather than introducing Jest, Mocha, or another test runner.

### Latest Technical Information

- As of `tree-sitter-cli` `0.26.1`+, `tree-sitter build --wasm` compiles parsers to WebAssembly using the `wasi-sdk` toolchain, which the CLI downloads automatically on first use. Docker, Podman, and a manually installed Emscripten toolchain are **no longer required** for this step — only pin a current `tree-sitter-cli` version and the build works directly on this Windows repository's shell.
- `web-tree-sitter` (npm, `^0.26`) exposes `Parser.init()`, `Language.load(pathOrBuffer)`, `parser.setLanguage(language)`, and `parser.parse(sourceText)`; `tree.rootNode.hasError` is the correct incomplete/malformed-source signal and is the same API Story `3.2`/`3.3` will reuse in the browser.
- Emscripten is still relevant only for regenerating the `web-tree-sitter` runtime binding itself (`tree-sitter.wasm`), not for compiling this grammar's parser (`tree-sitter-athena.wasm`); this story only needs the latter.

### Project Structure Notes

- This story sits entirely inside a new, independent `ide/tree-sitter-athena/` package. It has no Gradle module, no `settings.gradle.kts` entry, and no `ide/package.json` workspaces entry.
- Naming should stay aligned with the architecture spine's consistency conventions: grammar name `athena` (so generated symbols are `tree_sitter_athena`), directory `ide/tree-sitter-athena/`, package `@engineeringood/athena-tree-sitter-grammar`.
- The story should make Story `3.2`'s job mechanical: "depend on this package, load the checked-in `.wasm`, run the checked-in query."

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m17/1-1-freeze-the-public-authored-syntax-contract.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt]
- [Source: kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt]
- [Source: examples/m0/demo-cabinet.athena]
- [Source: examples/m0/dual-drive-cabinet.athena]
- [Source: examples/m0/quoted-properties-cabinet.athena]
- [Source: examples/m4/open-repository-proof/src/factory-line.athena]
- [Source: integrations/graph-glsp/package.json]
- [Source: ide/package.json]
- [Source: ide/theia-frontend/src/browser/athena-language-definition.ts]

## Story Completion Status

- Status: done
- Completion note: Story `3.1` closed on 2026-07-15. The checked-in `tree-sitter-athena.wasm` artifact now exists, `tree-sitter.json` publishes the grammar metadata, `yarn --cwd ide/tree-sitter-athena build` deterministically regenerates the parser + wasm through `scripts/build-tree-sitter-wasm.mjs`, and `yarn --cwd ide/tree-sitter-athena test` passes end to end over both the corpus tests and the repository-backed `web-tree-sitter` proofs.
