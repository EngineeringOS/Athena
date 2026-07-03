---
baseline_commit: ae76b71c58bb036f1367e96608aaee7eac213dac
---

# Story 1.3: Lower Parsed Intent Into Canonical Engineering IR

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to lower a valid syntax-only AST into canonical `Engineering IR` with stable semantic identities,
so that every later compiler pass works from one authoritative semantic model rather than from syntax trees or renderer-specific structures.

## Acceptance Criteria

1. Given a valid parsed M0 AST describing electrical or runtime declarations, ports, and connections, when the semantic lowering pass executes, then it produces `Engineering IR` objects for the declared engineering elements and their relationships, and the resulting IR is independent of any layout, geometry, or renderer-specific fields.
2. Given two compilations of semantically unchanged authored input, when the lowering pass produces `Engineering IR`, then each engineering object retains a stable semantic identity across runs, and downstream passes can reference those identities without using AST node positions as authority.
3. Given authored input that parses successfully but contains unresolved semantic references reserved for later validation, when lowering completes, then the compiler still emits a structurally well-formed `Engineering IR` with traceable source provenance, and semantic errors remain the responsibility of later validation passes rather than the lowering phase.

## Tasks / Subtasks

- [x] Define the first canonical `Engineering IR` types and provenance contracts under `ir/` for systems, engineering elements, ports, connections, and symbolic references. (AC: 1, 2, 3)
  - [x] Create minimal IR data structures that preserve stable identities, authored names, typed relationships, and source provenance without including layout or renderer fields.
  - [x] Keep the first IR vocabulary general enough for the semantic core while still carrying the M0 Electrical/Runtime wedge.
- [x] Implement deterministic lowering from the syntax-only AST into canonical `Engineering IR`. (AC: 1, 2, 3)
  - [x] Lower `system`, `device`, `port`, and `connect` syntax into IR objects and relationship/reference records.
  - [x] Preserve unresolved semantic references as explicit IR structures rather than emitting lowering-time semantic diagnostics.
  - [x] Define a stable semantic identity strategy anchored in authored meaning, not AST offsets, line numbers, or renderer concerns.
- [x] Expose the lowering result through the compiler-facing entry path without introducing another temporary authority model. (AC: 1, 2)
  - [x] Reshape the current compiler facade so downstream work can consume canonical AST/IR outputs and provenance instead of declaration-count summaries.
  - [x] Keep AST ownership in `language` and canonical semantic ownership in `ir`.
- [x] Add deterministic fixture tests for IR shape, stable identities, unresolved-reference lowering, and provenance retention. (AC: 1, 2, 3)
  - [x] Assert exact IR structure for the representative example under `examples/`.
  - [x] Assert repeated lowering of identical source yields the same semantic identities.
  - [x] Assert lowering succeeds structurally for parse-valid but semantically unresolved inputs.
- [x] Document the M0 lowering boundary, the first IR shape, and the identity/provenance rules for later validation and rendering stories. (AC: 1, 2, 3)

### Review Findings

- [x] [Review][Patch] Synthesize distinct fallback identities for duplicate declarations and repeated connections so lowering stays permissive before validation [compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt:38]
- [x] [Review][Patch] Preserve per-reference provenance in `EngineeringReference` [ir/src/main/kotlin/com/engineeringood/athena/ir/EngineeringIrModel.kt:64]
- [x] [Review][Patch] Reject over-qualified `port` declarations that the lowerer cannot resolve [language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt:186]
- [x] [Review][Patch] Return compiler diagnostics for unreadable source files instead of throwing [compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:33]
- [x] [Review][Patch] Demote `ParseSummary` to a CLI-only helper instead of keeping it in the public compiler model [compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt:22]
- [x] [Review][Patch] Publish the canonical IR expectation as an `examples/` conformance artifact, not only inline in tests [compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt:29]
- [x] [Review][Patch] Add compiler tests for mixed endpoint resolution and public failure-path diagnostics [compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt:152]
- [x] [Review][Patch] Split the parser provenance test so `connect` endpoint qualification is exercised independently [language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt:27]
- [x] [Review][Defer] Accumulate multiple syntax diagnostics instead of stopping at the first parser error [language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt:6] - deferred, pre-existing

## Dev Notes

### Story Intent

- Story `1.3` is the first semantic boundary in M0. It must prove that the syntax-only AST from Story `1.2` can be lowered into one canonical semantic model that later validation, rule execution, and rendering can trust.
- The architectural point is not to enrich the parser. The point is to move semantic authority out of AST and into `Engineering IR`.
- Keep the first IR intentionally small and inspectable. It only needs enough structure to carry the current M0 declarations, ports, references, and connections into a canonical semantic form.

### Architecture Guardrails

- `Engineering IR` is the first and only canonical semantic authority in M0. AST remains syntax-only and may not become a second semantic substrate.
- Lowering must remain distinct from semantic validation. Story `1.3` may normalize and structure authored meaning, but unresolved references, type compatibility, port legality, and connection legality belong to Story `1.4`.
- The IR must remain free of layout, geometry, page coordinates, renderer settings, and output-target mechanics.
- Keep M0 JVM-first, local, and single-process. Lowering lives inside the existing Kotlin/JVM workspace and remains callable from the compiler shell.
- Do not let the first Electrical/Runtime wedge hard-code the permanent core vocabulary. The IR types created here should stay general enough that future domain extensions attach around them.

### Technical Requirements

- Implement the canonical IR model primarily under `ir/`.
- Implement lowering orchestration primarily under `compiler/`, with `semantics-core/` used only if a truly shared semantic contract is needed now.
- Preserve provenance as first-class data in the IR. Lowered objects and references must remain traceable back to authored source locations or AST-owned provenance structures.
- Stable semantic identity must be deterministic across runs for semantically unchanged input. Do **not** derive identity from AST positions, line numbers, character offsets, or parse-order coincidence alone.
- Lowering must carry unresolved semantic references structurally so later validation can diagnose them. A parse-valid source with a missing or unresolved semantic target should still lower into a well-formed IR.
- Keep the first IR shape small. The `Kernel v0` node/edge proof in the manifesto is a useful lower bound, but the Kotlin IR may use clearer typed data classes if that improves explicitness and provenance.
- Do not introduce renderer-facing or export-facing data into the IR just to make later stories easier.
- Do not proliferate temporary summary models. The current `AthenaCompiler` parse summary is already too narrow for later compiler work and should evolve toward AST/IR authority instead of creating a third durable shape.

### Architecture Compliance

- Align to AD-1 by keeping lowering inside the single JVM compiler process.
- Align to AD-2 by keeping the semantic core general while treating Electrical/Runtime as the first domain wedge rather than the permanent core vocabulary.
- Align to AD-3 by making `Engineering IR` the only canonical semantic authority and keeping AST syntax-only.
- Align to AD-4 and `manifesto/docs/architecture/09-layout-and-geometry.md` by excluding layout and geometry from the IR.
- Align to AD-7 by treating example IR expectations as architecture-contract fixtures rather than casual test churn.

### Library / Framework Requirements

- Stay on the pinned workspace stack from Story `1.1`: Java `25`, Kotlin `2.4.0`, Gradle `9.6.1`.
- Use ordinary Kotlin data classes, sealed hierarchies, and value objects for the first IR model.
- Preserve the package root `com.engineeringood.athena`.
- Reuse the existing Kotlin test setup already established in the workspace. Do not introduce a second test framework.
- No external compiler frameworks, graph databases, LLVM/MLIR stacks, or serialization-heavy infrastructure are needed for this story.

### File Structure Requirements

- Expected primary touch points:
  - `ir/src/main/kotlin/com/engineeringood/athena/ir/**`
  - `ir/src/test/kotlin/com/engineeringood/athena/ir/**`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/**` only if a shared semantic contract is genuinely required now
  - `examples/**` for representative lowering fixtures
- `language/` should remain syntax-owned. Modify it only if direct 1.2 review blockers around provenance or AST contract must be resolved for lowering to meet its acceptance criteria.
- Keep IR-facing types out of `cli/`, `renderer-svg/`, and the domain placeholder module unless a thin orchestration seam is unavoidable.
- Do not modify generated `build/` outputs or treat them as source files.

### Testing Requirements

- Minimum verification for story completion should include:
  - `./gradlew.bat build`
  - `./gradlew.bat test`
  - targeted lowering tests in `ir` and/or `compiler`
  - one standard compiler-path verification against `examples/m0/demo-cabinet.athena`
- Add tests that prove:
  - valid parsed input lowers into a stable canonical IR fixture
  - the same authored input lowers to the same semantic identities across repeated runs
  - unresolved semantic references are preserved structurally for later validation instead of failing the lowering pass
  - the lowered IR contains no layout, geometry, or renderer-specific authority
- Keep fixtures explicit and provenance-aware. Do not assert only counts or incidental string output.

### Previous Story Intelligence

- Story `1.2` created the first real DSL parser, AST, example fixture, and parse path, but it is **not done yet**. It is currently back in `in-progress` after code review.
- Review findings from Story `1.2` that directly affect Story `1.3` include:
  - qualified names are not yet enforced as dotted references
  - `SystemDeclaration.span` is too narrow
  - the `SourceSpan` contract and emitted token ranges are misaligned
  - the compiler-facing parse path currently discards the AST/provenance and reduces success to a declaration-count summary
  - parser fixture tests do not yet independently pin provenance
- Those findings sit directly on the AST/provenance seam that lowering depends on. If any of them block correct canonical IR lowering, the dev agent should resolve them within the same implementation branch rather than building lowering on top of a known-bad contract.
- Story `1.2` also established the canonical example at `examples/m0/demo-cabinet.athena` and the docs `docs/compiler/m0-dsl.md` and `docs/compiler/parser-front-end-decision.md`. Reuse those rather than inventing a second authoring example or a second front-end thesis.

### Git Intelligence Summary

- Current committed history is still minimal:
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- There is still no prior IR implementation pattern to inherit. The first real IR model will be created in this story.

### Project Structure Notes

- No UX artifact exists for this phase. Keep the story compiler-first and text-first.
- The current module intent remains:
  - `language` owns DSL syntax, parser, and AST
  - `ir` must become the first canonical semantic model in this story
  - `compiler` owns lowering orchestration and later pass sequencing
  - `semantics-core` remains reserved for general semantic contracts and validation-oriented concepts
  - `renderer-svg` stays downstream and should not shape IR decisions here
- `examples/` should begin to accumulate stable IR expectations in addition to source examples.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 1, Story 1.3 acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - Sections 4.1, 4.2, 6, 8, and 10.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1 through AD-4 and AD-7, Stack, Structural Seed.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-2, Constraints, Success signal, Open Questions.
- `_bmad-output/specs/spec-athena/glossary.md` - `Engineering IR`, `Engineering Compiler`, `Electrical/Runtime domain extension`, `Conformance artifact`.
- `_bmad-output/implementation-artifacts/1-2-author-and-parse-the-m0-electrical-runtime-dsl.md` - current AST boundary, example fixture, and unresolved review findings that may block lowering.
- `docs/compiler/m0-dsl.md` - current M0 language slice and conformance example shape.
- `docs/compiler/parser-front-end-decision.md` - locked M0 choice for standalone text DSL, syntax-only AST, and hand-written Kotlin parser.
- `manifesto/docs/architecture/03-ir.md` - why `Engineering IR` exists, canonical semantic authority, identity, provenance, and IR-vs-layout boundary.
- `manifesto/docs/architecture/09-layout-and-geometry.md` - binding semantic/layout/geometry separation.
- `manifesto/docs/prd/01-kernel-v0.md` - minimal DSL -> IR -> rule -> export proof and the initial node/edge IR example.
- `manifesto/docs/rfc/RFC-0002-language.md` - language lowers into canonical IR rather than acting as the final execution substrate.
- `draft/0002.md` - M0 thesis that the DSL is the source of truth, the IR is the canonical model, and later stages are compiler backends around that model.

## Story Completion Status

- Status: done
- Completion note: Canonical Engineering IR, review-driven hardening patches, and Java 25 sequential verification are complete.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Sprint status auto-discovery selected `1-3-lower-parsed-intent-into-canonical-engineering-ir` as the first backlog story after the Story `1.2` review pass.
- No `project-context.md` file was present in the repository.
- No UX artifact was found under `_bmad-output/planning-artifacts/`.
- Story `1.2` was loaded as the immediate predecessor story and contains unresolved review findings that directly affect the AST/provenance seam for lowering.
- Current production code inspection confirmed `ir/` and `semantics-core/` still contain only module markers, while `compiler/AthenaCompiler.kt` currently returns a narrow parse summary rather than canonical AST/IR outputs.
- Added parser seam fixes required by lowering: full-system spans, dotted qualified-name enforcement for ports/connections, BOM-tolerant tokenization, and half-open `SourceSpan` contract documentation.
- Added targeted red/green verification with `:language:test --tests com.engineeringood.athena.language.AthenaLanguageProvenanceTest` and `:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`.
- Verified the full workspace with `java25; .\\gradlew.bat --console=plain build` and `java25; .\\gradlew.bat --console=plain test`.
- Code review triage classified 8 actionable patches and 1 deferred pre-existing parser limitation.
- Sequential no-daemon verification was required after reproducing the known Windows/Kotlin incremental-cache corruption path from parallel Gradle runs.

### Completion Notes List

- Created the Story `1.3` implementation guide for the first canonical `Engineering IR` boundary in M0.
- Carried forward the compiler-first thesis that AST is syntax-only and `Engineering IR` is the first semantic authority.
- Made the unresolved Story `1.2` review findings explicit as likely blockers on the lowering seam so the dev agent does not build new semantic code on top of a known-bad provenance contract.
- Kept the story focused on deterministic lowering, stable semantic identity, and provenance retention rather than premature validation or rendering concerns.
- Added the first canonical IR model under `ir/` with stable semantic identities, typed property values, unresolved-reference records, and provenance-rich semantic objects.
- Reworked the compiler facade so parse keeps syntax authority and lower emits canonical `Engineering IR` through a deterministic lowering pass.
- Documented the M0 lowering boundary in `docs/compiler/m0-lowering-boundary.md` and verified the workspace build and test tasks on Java 25.
- Applied all Story `1.3` review patches: duplicate fallback identities, reference provenance, stricter `owner.port` parsing, unreadable-source diagnostics, CLI-only parse summaries, external IR conformance artifact, and added coverage for mixed resolution and failure paths.

### File List

- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `cli/build.gradle.kts`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `docs/compiler/m0-lowering-boundary.md`
- `examples/README.md`
- `examples/m0/demo-cabinet.engineering-ir.txt`
- `ir/src/main/kotlin/com/engineeringood/athena/ir/EngineeringIrModel.kt`
- `language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt`
- `language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt`
- `_bmad-output/implementation-artifacts/deferred-work.md`
- `_bmad-output/implementation-artifacts/1-3-lower-parsed-intent-into-canonical-engineering-ir.md`

### Change Log

- 2026-07-02: Implemented the first canonical Engineering IR, deterministic AST-to-IR lowering, parser seam fixes required by lowering, provenance-aware compiler outputs, and the M0 lowering-boundary documentation.
- 2026-07-02: Applied all Story `1.3` review patches and re-verified the workspace on Java 25 using sequential no-daemon Gradle runs.
