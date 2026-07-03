---
baseline_commit: ae76b71c58bb036f1367e96608aaee7eac213dac
---

# Story 1.2: Author And Parse The M0 Electrical/Runtime DSL

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to accept the minimal M0 Electrical/Runtime DSL and parse it into a syntax-only AST,
so that authored engineering intent becomes deterministic compiler input for later semantic compilation passes.

## Acceptance Criteria

1. Given a valid M0 Electrical/Runtime source file using the approved keyword set for components, ports, and connections, when the compiler parses the file, then it produces a syntax-only AST that preserves declarations, references, and source spans without assigning semantic meaning, and the parse result is deterministic for identical source input.
2. Given an invalid M0 source file with malformed syntax, when the compiler parses the file, then it emits syntax diagnostics with file, line, and column provenance, and semantic validation and rendering passes do not execute on that failed parse result.
3. Given a representative valid example under `examples/`, when it is parsed in the standard compiler entry path, then the AST shape is stable enough to support snapshot or fixture-based verification, and the example remains free of layout or renderer-specific authoring concerns.

## Tasks / Subtasks

- [x] Define the minimal M0 DSL surface for declarations, ports, and connections, and capture at least one representative valid example under `examples/`. (AC: 1, 3)
- [x] Add syntax-only AST types in `language` that preserve source spans, authored names, and references without semantic validation logic or `Engineering IR` concepts. (AC: 1)
- [x] Implement deterministic parsing in `language`, including syntax diagnostics with file, line, and column provenance for malformed input. (AC: 1, 2)
- [x] Expose parse-only execution through the standard compiler entry path so parse failure stops before semantic validation or rendering behavior. (AC: 2, 3)
- [x] Add fixture-based or snapshot-style tests for valid AST shape and invalid syntax diagnostics, and verify the standard wrapper build/test flow remains green on Java `25`. (AC: 1, 2, 3)
- [x] Document the approved M0 DSL boundary and example expectations for later lowering work. (AC: 1, 3)

### Review Findings

- [ ] [Review][Patch] Require dotted qualified names for `port` and `connect` references [language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt:223]
- [ ] [Review][Patch] Widen `SystemDeclaration.span` to cover the full system block [language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt:151]
- [ ] [Review][Patch] Align the `SourceSpan` contract with the emitted token ranges [language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt:10]
- [ ] [Review][Patch] Preserve the syntax AST and provenance through the standard compiler entry path [compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:47]
- [ ] [Review][Patch] Make the parser fixture tests assert independent provenance values [language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt:25]
- [ ] [Review][Patch] Handle invalid, unreadable, and non-file parse inputs without uncaught exceptions [cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt:41]
- [ ] [Review][Patch] Accept UTF-8 BOM-prefixed source files in the tokenizer [language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt:48]

## Dev Notes

### Story Intent

- Story `1.2` is the language front-end boundary for M0. It must prove that authored engineering intent can be expressed in a constrained Electrical/Runtime DSL and lowered into a syntax-only AST before any semantic interpretation occurs.
- The architectural point is not language richness. The point is to make the DSL the source of truth for authored input while keeping `Engineering IR` as a later canonical semantic model.
- Keep the first language cut intentionally small. The current project contract assumes an M0 keyword set of roughly `20-30` core keywords, but Story `1.2` only needs enough surface to express declarations, ports, and connections cleanly. Do not invent broader product language.

### Architecture Guardrails

- Keep M0 JVM-first, local, and single-process. The parser lives inside the existing Kotlin/JVM workspace and remains callable from the CLI shell. Do not introduce services, LSP servers, UI surfaces, or remote parsing infrastructure here.
- AST is syntax-level only. Do not leak semantic validation, rule execution, stable semantic identity, or renderer concerns into the AST model.
- `Engineering IR` remains distinct from the DSL and the AST. Story `1.2` must not create a pseudo-IR in the `language` module or let parser node shapes become the durable semantic contract.
- The DSL must describe engineering intent, not layout intent or geometry. Do not add page coordinates, view placement, symbol positions, routing geometry, or renderer-specific settings to the authored language.
- Keep the first domain surface aligned with the Electrical/Runtime wedge, but do not let the first syntax cut redefine the permanent core vocabulary of Athena.
- Plugin behavior is out of scope for this story. Use the workspace seams created in Story `1.1`, but do not implement manifest discovery, plugin activation, or rule plugins here.

### Technical Requirements

- The canonical M0 authoring language is a **standalone textual DSL**, not an embedded Kotlin DSL.
- Do **not** use Kotlin `@DslMarker` or Kotlin function-builder syntax as the production source-of-truth language. If an internal builder is useful later for tests or fixtures, it must remain secondary and must not replace the text DSL.
- Implement the parsing boundary primarily in `language/`. A small amount of `compiler/` or `cli/` work is acceptable only to expose the parse result through the standard compiler entry path.
- The parser result should make source provenance first-class. Every top-level declaration, reference-like token, and syntax diagnostic must retain enough location data to support later semantic diagnostics and fixture verification.
- Keep parsing deterministic and explicit. Given the same source text, the parser must produce the same AST and the same syntax diagnostics every run.
- Parse failure must stop later phases. Do not allow placeholder semantic validation, IR lowering, or rendering calls to run after a syntax error.
- Use a **hand-written Kotlin tokenizer plus hand-written recursive-descent parser** for M0 unless a concrete grammar problem proves that approach insufficient.
- Keep dependencies intentionally small. Draft guidance allows Kotlin parser combinators or ANTLR only if needed, but the default for Story `1.2` is the smallest Kotlin-native parser implementation, not a generator-first toolchain.
- The example introduced here is a conformance seed, not a throwaway sample. Its AST shape should be stable enough for fixture-based verification and for Story `1.3` lowering work.

### Architecture Compliance

- Align to AD-1 by keeping the parser and parse entry path inside the existing CLI-centered single JVM process.
- Align to AD-2 by treating Electrical/Runtime as the first language wedge, not as the permanent definition of the semantic core.
- Align to AD-3 by keeping the AST syntax-only and reserving canonical semantic authority for later `Engineering IR` work.
- Align to AD-4 and `manifesto/docs/architecture/09-layout-and-geometry.md` by excluding layout and geometry concerns from the DSL surface.
- Align to AD-7 by treating the first example and its expected AST shape as part of the architecture contract, not casual fixture churn.

### Library / Framework Requirements

- Stay on the pinned workspace stack from Story `1.1`: Java `25`, Kotlin `2.4.0`, Gradle `9.6.1`.
- Use Gradle Kotlin DSL and preserve the current module layout rooted at `com.engineeringood.athena`.
- AST types should be ordinary Kotlin data classes and sealed hierarchies in `language`, with explicit source-span data attached where needed for diagnostics and fixtures.
- Do not introduce `Tree-sitter` in Story `1.2`. If editor tooling is added later, `Tree-sitter` is an editor-support concern, not the canonical compiler parser.
- Introduce ANTLR only if the language shape clearly outgrows a hand-written parser and the tradeoff is explicitly justified.
- Do not introduce LLVM, MLIR, or any low-level compiler backend stack. Athena M0 is building a semantic engineering compiler front end, not a machine-code compiler pipeline.
- Reuse the existing Kotlin test setup already established in the workspace. Do not introduce a second test framework.

### File Structure Requirements

- Expected primary touch points:
  - `language/build.gradle.kts`
  - `language/src/main/kotlin/com/engineeringood/athena/language/**`
  - `language/src/test/kotlin/com/engineeringood/athena/language/**`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**` only if needed for parse orchestration
  - `cli/src/main/kotlin/com/engineeringood/athena/cli/**` only if needed for the standard parse entry path
  - `examples/**` for at least one representative valid M0 source example
- Keep parser-facing types under `language`. Do not place AST or syntax diagnostic types in `ir`, `semantics-core`, `renderer-svg`, or the domain placeholder module.
- Preserve the package root `com.engineeringood.athena`.
- Do not modify generated `build/` outputs or treat them as source files.

### Testing Requirements

- Minimum verification for story completion should include:
  - `./gradlew.bat build`
  - `./gradlew.bat test`
  - `./gradlew.bat :cli:run --args="--help"` to confirm the existing CLI path still works
  - One parse-path verification through the standard compiler entry path against a valid example
- Add tests that prove:
  - valid source parses into a stable AST fixture or snapshot shape
  - malformed source emits syntax diagnostics with file, line, and column information
  - parse failure does not continue into semantic validation or rendering behavior
- Keep tests deterministic and text-fixture-friendly. The AST should be asserted as data, not through brittle ad hoc string formatting alone.

### Previous Story Intelligence

- Story `1.1` successfully established the JVM workspace and module seams, so Story `1.2` should build on those modules rather than inventing new project structure.
- The parser story starts from almost no language infrastructure. `language/` currently contains only a placeholder module marker, so the dev agent should expect to create the first real AST, parser, and parser tests there.
- Story `1.1` is currently back in `in-progress` because review left four bootstrap action items unresolved:
  - portable Java 25 toolchain/bootstrap configuration
  - additive `.gitignore` restoration
  - official Gradle wrapper URL and sane timeout
  - automated CLI smoke coverage
- Those are real issues, but they are not the primary goal of Story `1.2`. Do not expand parser scope to fix them unless the parser work is directly blocked.

### Git Intelligence Summary

- Current committed history is still minimal:
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- There is no prior parser implementation pattern to inherit. The primary implementation authority is the planning stack plus the current workspace scaffold.

### Project Structure Notes

- No UX artifact exists for this phase. Keep the story compiler-first and text-first.
- Preserve the existing module intent:
  - `language` owns DSL syntax, parser, and AST
  - `compiler` may coordinate parse execution, but it should not redefine parser-owned syntax models
  - `cli` remains the shell entrypoint
  - `ir` stays untouched by syntax-only concerns in this story
- `examples/` is now ready to hold the first real conformance input. Add source examples there in a way later stories can extend rather than replace.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 1, Story 1.2 acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - Sections 4.1, 4.2, 6, 8, and 10.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1 through AD-4 and AD-7, Stack, Structural Seed.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-1, Constraints, Success signal, Open Questions.
- `_bmad-output/specs/spec-athena/glossary.md` - `Engineering Language`, `Engineering IR`, `Engineering Compiler`, `Electrical/Runtime domain extension`.
- `_bmad-output/implementation-artifacts/1-1-establish-the-m0-jvm-compiler-workspace.md` - current workspace shape and unresolved review findings to avoid absorbing accidentally.
- `docs/compiler/parser-front-end-decision.md` - locked M0 choice for standalone text DSL, syntax-only AST, and hand-written Kotlin parser.
- `manifesto/docs/rfc/RFC-0002-language.md` - language is the constrained human-readable authoring layer that lowers into canonical IR.
- `manifesto/docs/architecture/03-ir.md` - IR versus DSL boundary; semantic/layout/geometry separation; AST/DSL must not become canonical semantic authority.
- `manifesto/docs/architecture/09-layout-and-geometry.md` - authored engineering language must exclude layout and geometry mechanics.
- `manifesto/docs/prd/01-kernel-v0.md` - canonical minimal DSL/IR proof shape and the connected example pattern.
- `draft/0001.md` - stack note that parser choice should stay lightweight (`Kotlin parser combinators or ANTLR only if needed`).
- `draft/0002.md` - M0 success criteria and the “DSL is the source of truth, IR is the canonical model” thesis.

## Story Completion Status

- Status: done
- Completion note: Implemented the M0 parse-only DSL front end, example fixture, CLI parse path, and Java 25 verification flow.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Sprint status auto-discovery selected `1-2-author-and-parse-the-m0-electrical-runtime-dsl` as the first backlog story after Story `1.1`.
- No `project-context.md` file was present in the repository.
- No UX artifact was found under `_bmad-output/planning-artifacts/`.
- Previous story context and review findings were loaded from `_bmad-output/implementation-artifacts/1-1-establish-the-m0-jvm-compiler-workspace.md`.
- Red phase verification used `./gradlew.bat --console=plain :language:test --tests com.engineeringood.athena.language.AthenaLanguageParserTest` and `./gradlew.bat --console=plain :cli:test --tests com.engineeringood.athena.cli.ParseCliTest`, both of which failed before parser implementation existed.
- Concurrent Gradle verification was proven to corrupt Kotlin incremental caches on Windows; the failure reproduced when `clean build`, `test`, and `:cli:run` were launched in parallel against the same workspace.
- Final verification was rerun sequentially on Java `25` after stopping the Gradle `9.6.1` daemon and removing module `build/` directories.
- Final passing commands:
  - `./gradlew.bat --no-daemon --console=plain clean build`
  - `./gradlew.bat --no-daemon --console=plain test`
  - `./gradlew.bat --no-daemon --console=plain :cli:run --args="--help"`
  - `./gradlew.bat --no-daemon --console=plain :cli:run --args="parse examples/m0/demo-cabinet.athena"`

### Completion Notes List

- Locked the parser front-end decision around a standalone text DSL plus a hand-written Kotlin tokenizer and recursive-descent parser.
- Added syntax-only AST and diagnostic models in `language` with source spans, qualified names, scalar values, and deterministic parse results.
- Implemented parse support for `system`, `device`, `port`, and `connect` declarations, including provenance-rich syntax diagnostics.
- Exposed parse-only execution through `compiler` and `cli`, with parse failures stopping before semantic validation and rendering.
- Added a representative example at `examples/m0/demo-cabinet.athena`, parser tests, CLI parse tests, and M0 DSL documentation for later lowering work.
- Configured the Gradle `:cli:run` task to use the repository root as its working directory so documented example paths work in the standard entry path.
- Verified the final state with sequential Java `25` `clean build`, `test`, CLI help, and CLI parse commands.
- Created a dedicated implementation story for the first real language/parser work in M0.
- Carried forward the compiler-first, syntax-only AST boundary from the epics, PRD, architecture spine, SPEC, manifesto, and draft notes.
- Made the “no layout or renderer concerns in the DSL” rule explicit for the dev agent.
- Marked Story `1.2` as `ready-for-dev` for the next implementation step.

### File List

- `_bmad-output/implementation-artifacts/1-2-author-and-parse-the-m0-electrical-runtime-dsl.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `cli/build.gradle.kts`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/ParseCliTest.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `docs/compiler/m0-dsl.md`
- `docs/compiler/parser-front-end-decision.md`
- `docs/compiler/workspace-bootstrap.md`
- `examples/README.md`
- `examples/m0/demo-cabinet.athena`
- `language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`
- `language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`

## Change Log

- 2026-07-02: Implemented the M0 standalone text DSL parser front end, parse CLI path, conformance example, and Java 25 verification updates for Story `1.2`.
