---
baseline_commit: ae76b71c58bb036f1367e96608aaee7eac213dac
---

# Story 1.5: Execute The M0 Compiler As Declared Deterministic Passes

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to run M0 compilation through an explicit ordered pass pipeline,
so that parsing, lowering, validation, and downstream derivation happen predictably through declared responsibilities and phase boundaries.

## Acceptance Criteria

1. Given the standard M0 compiler entry path, when a compilation request is executed, then the compiler runs parsing, lowering, validation, and downstream derivation through an explicitly declared pass sequence, and each pass has a documented responsibility, declared input state, and declared output state.
2. Given identical semantic inputs and governed knowledge versions, when the same pass pipeline is executed multiple times, then the compiler produces the same pass outcomes, diagnostics, and success or failure state, and no pass may rely on hidden mutable state or implicit ordering outside the declared pipeline.
3. Given a pass failure or a pipeline gate condition, when the compiler evaluates whether to continue, then it applies deterministic continuation or stop rules for subsequent passes, and the pipeline behavior is inspectable enough for reviewers to understand why later passes did or did not run.

## Tasks / Subtasks

- [x] Define the minimal compiler pass contract surface under `compiler/` for the current M0 pipeline. (AC: 1, 2, 3)
  - [x] Model stable pass identity, declared responsibility, declared input state, declared output state, and per-pass execution status without introducing a second semantic authority.
  - [x] Keep the pass contract local, explicit, and JVM-first; do not introduce plugin ordering, async scheduling, or a workflow engine.
- [x] Refactor the current compiler entry path to execute parse, lower, validate, and downstream-derivation stages through one declared ordered pipeline. (AC: 1, 2, 3)
  - [x] Reuse the existing parser, lowerer, and validator implementations rather than re-implementing their logic.
  - [x] Make continuation or stop decisions explicit at the pipeline level and preserve the current semantic-invalid stop rule from Story `1.4`.
  - [x] Represent downstream derivation as a declared compiler-owned stage without pulling actual render-model or `SVG` logic forward from Story `1.6`.
- [x] Expose inspectable pipeline results through the compiler-facing surface and any minimal reviewer-facing output needed by current tests. (AC: 1, 3)
  - [x] Let callers inspect which passes ran, which pass stopped the pipeline, and why later passes were skipped.
  - [x] Preserve the existing `parse`, `lower`, and `compile` convenience entry points unless a breaking change is architecturally required.
- [x] Add deterministic tests for pass ordering, repeated execution stability, and gate behavior. (AC: 2, 3)
  - [x] Cover valid execution where all declared passes run.
  - [x] Cover syntax failure where semantic and downstream passes do not run.
  - [x] Cover semantic-invalid execution where downstream derivation is skipped according to policy.
- [x] Document the M0 compiler pass schedule, pass contracts, and continuation rules. (AC: 1, 2, 3)

## Dev Notes

### Story Intent

- Story `1.5` is the point where Athena stops merely having compiler phases in code and starts declaring them as an executable pipeline contract.
- The proof target is inspectable compiler structure, not more semantic rules. Parsing, lowering, validation, and downstream derivation must execute through one declared schedule that a reviewer can explain.
- Story `1.6` owns the real render-facing model and `SVG` emission. Story `1.5` must only create the compiler-owned pass boundary that makes that later derivation stage explicit.

### Architecture Guardrails

- The compiler owns pass ordering and continuation policy. No pass may depend on hidden mutable state, implicit call order, or renderer internals.
- `Engineering IR` remains the only canonical semantic authority. Pass metadata may describe execution state, but it must not become a second durable model of engineering meaning.
- Preserve the separation bound by `manifesto/docs/architecture/09-layout-and-geometry.md`: semantic truth stays upstream, downstream derivation stays downstream, and no layout or geometry fields enter `Engineering IR`.
- Keep the pipeline single-process, local, and JVM-first. No services, event buses, coroutines, or background execution surfaces belong in this story.
- Do not pull plugin discovery, plugin-contributed pass ordering, or rule-pack compatibility into Story `1.5`. The contract should be clean enough for later extension, but the implementation remains built-in and compiler-owned in M0.

### Technical Requirements

- Implement the pass contract surface under `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`.
- Reuse `AthenaLanguageParser`, `EngineeringIrLowerer`, and `EngineeringIrValidator` as the current concrete pass bodies.
- Introduce explicit pass descriptors for the current M0 schedule at minimum covering:
  - parse authored source into syntax-owned AST
  - lower syntax-owned source into canonical `Engineering IR`
  - validate `Engineering IR` and compute continuation policy
  - perform a compiler-owned downstream-derivation stage that is declared and inspectable even if its current output is only a placeholder for later render-model work
- Keep pass inputs and outputs typed and explicit. A reviewer should be able to see, from code and test output, which artifact each pass consumes and produces.
- Preserve current syntax failure behavior: parse diagnostics stop later passes deterministically.
- Preserve current semantic failure behavior from Story `1.4`: semantic-invalid state keeps parse and lowering artifacts inspectable but stops downstream derivation when continuation is `STOP_DOWNSTREAM`.
- If you need a pipeline report model, keep it compiler-facing and lightweight: ordered pass records, execution status, skip reason, and any pass-local diagnostics summary are sufficient for M0.
- Do not move semantic validation contracts out of `semantics-core/`, and do not move canonical IR contracts out of `ir/`.

### Architecture Compliance

- Align to AD-1 by keeping the pipeline inside one Kotlin/JVM process with deterministic local execution.
- Align to AD-3 by ensuring pass execution after lowering operates over `Engineering IR`, not AST reinterpretation.
- Align to AD-4 by declaring downstream derivation without collapsing renderer or layout concerns back into semantics.
- Align to AD-6 by keeping pass ordering compiler-owned even though later plugin extension points will attach inside that schedule.
- Align to AD-7 by treating deterministic pass outcomes as part of the conformance proof, not incidental implementation detail.

### Library / Framework Requirements

- Stay on the pinned workspace stack: Java `25`, Kotlin `2.4.0`, Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Use standard Kotlin data classes, enums, sealed interfaces, and value classes for pipeline contracts.
- Reuse the existing Kotlin test stack already present in the workspace.
- Do not introduce ANTLR changes, coroutines, workflow libraries, DI containers, or external state-machine frameworks for this story.

### File Structure Requirements

- Expected primary touch points:
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `cli/src/main/kotlin/com/engineeringood/athena/cli/**` only if a minimal current-phase inspection surface truly needs it
  - `docs/compiler/**` for the declared pass schedule and gate policy
  - `examples/**` only if a conformance fixture or expected output needs a pipeline-oriented assertion
- `language/`, `ir/`, and `semantics-core/` should only change if the pass contract exposes a genuine missing typed boundary that belongs in those modules.
- Do not drag Story `1.6` render-model types into this story. If downstream derivation needs a current placeholder contract, keep it local to `compiler/` and obviously transitional.
- Do not modify generated `build/` outputs or treat them as source files.

### Testing Requirements

- Minimum verification for story completion should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Add tests that prove:
  - the declared pass order is stable and inspectable
  - repeated execution over identical input yields identical pass records and end state
  - syntax failure stops lowering, validation, and downstream derivation
  - semantic invalidity still exposes IR and diagnostics but skips downstream derivation according to policy
  - the convenience `compile()` surface remains coherent with the new pass pipeline
- **Windows verification rule:** do not run Gradle verification commands in parallel in this repository. If Kotlin cache corruption appears again, stop the daemon, remove module `build/` directories, and rerun sequentially.

### Previous Story Intelligence

- Story `1.4` is complete and already established:
  - `SemanticValidationResult` with explicit `SemanticContinuationDecision`
  - deterministic semantic diagnostics over canonical `Engineering IR`
  - `AthenaCompiler.compile()` as a unified parse/lower/validate entry path
- Story `1.4` intentionally stopped short of an explicit pass schedule. Reuse its continuation policy rather than inventing a second gate model.
- Current code inspection shows:
  - `AthenaCompiler` currently calls parse, lower, and validate directly in sequence
  - `CompilerCompilationSuccess` carries source, canonical IR, and semantic validation result, but no pass-by-pass execution record yet
  - `BootstrapCli` currently exposes only `parse`; pipeline inspection can remain test-first unless a minimal CLI surface is clearly justified
- Keep the Story `1.4` review hardening intact:
  - duplicate connection authored keys are diagnosed
  - quoted `type` or `direction` values are invalid, not missing

### Git Intelligence Summary

- Current repository history is still minimal:
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical implementation guidance must come from the current working tree and completed story artifacts, not from commit history.

### Project Structure Notes

- No UX artifact exists for this phase. Keep the story compiler-first and inspection-first.
- The current module intent remains:
  - `language` owns DSL syntax, parser, and AST
  - `ir` owns canonical semantic objects and provenance
  - `semantics-core` owns semantic validation and continuation policy
  - `compiler` owns orchestration and the declared pass schedule
  - `renderer-svg` remains downstream and should not become a dependency for Story `1.5` implementation
- `manifesto/docs/architecture/09-layout-and-geometry.md` remains binding for this story even though a durable `Layout IR` is deferred. Story `1.5` must preserve that separation in the executable schedule.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 1, Story `1.5` acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - FR-4, determinism, and downstream-output requirements.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1, AD-3, AD-4, AD-6, AD-7, stack, and module map.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-2, CAP-3, constraints, and success signal.
- `_bmad-output/implementation-artifacts/m0/1-4-validate-engineering-ir-and-emit-provenance-rich-diagnostics.md` - continuation policy, compiler compile-path shape, and review-hardened semantic boundary.
- `docs/compiler/m0-validation-boundary.md` - current validation contracts and downstream stop rule inherited by this story.
- `manifesto/docs/architecture/01-compiler.md` - explicit pass responsibilities and compiler role.
- `manifesto/docs/architecture/03-ir.md` - canonical IR authority and stable pass semantics.
- `manifesto/docs/architecture/09-layout-and-geometry.md` - semantic/layout/geometry separation boundary.
- `manifesto/docs/prd/02-compiler-v1.md` - explicit pass pipeline as product-facing compiler contract.
- `manifesto/docs/rfc/RFC-0005-compiler.md` - explicit compiler pipeline framing and ordering responsibility.
- `draft/0002.md` - M0 proof thesis that the DSL is the source of truth and everything downstream is a compiler backend.

## Story Completion Status

- Status: done
- Completion note: Declared deterministic pass execution, pipeline reporting, compiler gating, and documentation are implemented and verified.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Sprint status identified `1-5-execute-the-m0-compiler-as-declared-deterministic-passes` as the first backlog story after Story `1.4`.
- No `project-context.md` file was present in the repository.
- No UX artifact was found under `_bmad-output/planning-artifacts/`.
- Architecture and manifesto review consistently bound compiler execution to explicit ordered passes, compiler-owned continuation policy, and strict separation from layout or renderer authority.
- Current code inspection confirmed that `AthenaCompiler` already exposes parse/lower/compile entry points but still executes passes through direct calls without a declared pass report.
- Story `1.4` completion notes and review hardening patches were loaded and carried forward as implementation constraints.
- Red-phase compiler tests were added first for valid pass reporting, deterministic repeated execution, semantic-invalid downstream skipping, and syntax-failure pipeline stopping.
- Fresh verification evidence:
  - `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest` -> `BUILD SUCCESSFUL`
  - `java25; .\gradlew.bat --no-daemon --console=plain build` -> `BUILD SUCCESSFUL`
  - `java25; .\gradlew.bat --no-daemon --console=plain test` -> `BUILD SUCCESSFUL`

### Completion Notes List

- Created the concrete Story `1.5` developer guide for explicit compiler pass execution, deterministic continuation policy, and inspectable pipeline outcomes.
- Scoped Story `1.5` to executable pass declaration and pipeline inspection only, leaving actual render-model and `SVG` derivation for Story `1.6`.
- Preserved the JVM-first, compiler-first, and semantic-authority boundaries across story guardrails, file structure, and testing expectations.
- Added `CompilerPassId`, `CompilerPassDescriptor`, `CompilerPassExecutionStatus`, `CompilerPassRecord`, and `CompilerPipelineReport` as the minimal inspectable compiler pass contract.
- Refactored `AthenaCompiler.compile()` to emit deterministic pass records for parse, lower, validate, and downstream-derivation stages while preserving the existing `parse()` and `lower()` convenience surfaces.
- Added compiler-path regression coverage for valid execution, deterministic repeated pass reports, semantic-invalid downstream skipping, and syntax-failure pipeline stopping.
- Documented the executable M0 pass schedule, pass descriptors, and continuation rules in `docs/compiler/m0-pass-pipeline.md`.

### File List

- `_bmad-output/implementation-artifacts/m0/1-5-execute-the-m0-compiler-as-declared-deterministic-passes.md`
- `_bmad-output/implementation-artifacts/m0/sprint-status.yaml`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerPipelineModel.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `docs/compiler/m0-pass-pipeline.md`
- `docs/superpowers/plans/2026-07-02-m0-compiler-pass-pipeline.md`

### Change Log

- 2026-07-02: Implemented Story `1.5` explicit compiler pass execution, deterministic pipeline reporting, semantic gate handling, regression coverage, and M0 pass-pipeline documentation.
