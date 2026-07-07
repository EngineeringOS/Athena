---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 1.5: Refactor The Compiler Into An Explicit Named Pass Pipeline

Status: done

## Story

As a platform engineer,
I want the compiler expressed as a named pass pipeline,
so that domain participation happens through visible stages instead of opaque branching.

## Acceptance Criteria

1. Given the current compiler flow already performs parse, lower, validate, and downstream derivation work, when the M3 compiler structure is refactored, then Athena exposes compilation as an explicit named pass pipeline with stable stage responsibilities, and the minimum M3 path includes `parse`, `lower`, `semantic enrichment`, `validate`, and downstream backend preparation or emission stages.
2. Given the kernel must stay generic, when the pass pipeline is reviewed, then stage names and responsibilities remain domain-agnostic, and domain-specific logic does not reappear as hidden branches inside generic stage orchestration.
3. Given determinism is a non-functional requirement, when the same authored input and approved plugin set are compiled, then pass execution ordering and pass results remain deterministic, and the compiler can report pass participation clearly enough for verification and debugging.
4. Given the named pass pipeline is implemented, when the standard Java `25` build and compiler regression checks are executed, then the workspace builds successfully and the compiler reports explicit pass structure instead of one opaque flow, and existing M0 to M2 behavior remains compatible with the refactored orchestration.

## Tasks / Subtasks

- [x] Replace the old downstream-derivation pass model with an explicit six-stage pipeline. (AC: 1, 2, 3, 4)
  - [x] Expand `CompilerPassId` to `PARSE`, `LOWER`, `SEMANTIC_ENRICHMENT`, `VALIDATE`, `BACKEND_PREPARATION`, and `BACKEND_EMISSION`.
  - [x] Keep stable stage descriptors with generic responsibilities, input states, and output states.
  - [x] Preserve deterministic pass ordering for both full compile and runtime recompute flows.
- [x] Rework compiler pipeline reporting around the named stages. (AC: 1, 2, 3, 4)
  - [x] Split validation, backend preparation, and backend emission into explicit pass records.
  - [x] Ensure parse failures skip all downstream stages explicitly.
  - [x] Ensure semantic stop conditions skip backend preparation and backend emission explicitly.
- [x] Align regression tests and docs to the new pass structure. (AC: 3, 4)
  - [x] Update compiler tests to assert the six-stage pipeline descriptors, statuses, and summaries.
  - [x] Update runtime incremental assertions to target named pass ids rather than stale positional assumptions.
  - [x] Update compiler module documentation to publish the new pass order.
- [x] Verify the refactor with sequential Java `25` checks on Windows. (AC: 4)
  - [x] Run `:kernel:compiler:test`.
  - [x] Run `:kernel:runtime:test`.
  - [x] Record the explicit pipeline verification here.

## Dev Notes

### Story Intent

- Story `1.5` makes the compiler pipeline explicit before Story `1.6` governs which plugin callbacks are allowed to participate in those stages.
- The pass structure is intentionally generic: it must describe compiler authority without smuggling domain nouns into the orchestration boundary.
- M0 to M2 behavior must stay intact: canonical `Engineering IR`, deterministic layout and geometry derivation, and SVG emission still happen in the same architecture, but they are now reported as explicit pass stages.

### Implementation Direction

- `AthenaCompiler` now records one ordered pass report for parse, lower, semantic enrichment, validate, backend preparation, and backend emission.
- Validation can succeed while still returning semantic diagnostics that stop backend stages; the block is represented by explicit skipped backend pass records rather than one opaque downstream-derivation failure.
- Runtime incremental recompilation keeps using the same pass model, with scoped or fallback summaries recorded in the stage outputs.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-15-refactor-the-compiler-into-an-explicit-named-pass-pipeline]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-5---the-compiler-is-governed-as-a-named-pass-pipeline]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerPipelineModel.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `codegraph explore "AthenaCompiler semantic enrichment backend preparation backend emission AthenaDomainSemanticsCoordinator AthenaPluginRuntimeServices"`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerPipelineModel.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `Get-Content kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`

### Completion Notes List

- Refactored the compiler into an explicit six-stage pipeline with stable descriptors for parse, lower, semantic enrichment, validate, backend preparation, and backend emission.
- Replaced the old opaque downstream derivation reporting with explicit backend preparation and backend emission pass records.
- Made parse failures and semantic stop conditions visible through explicit skipped downstream pass records instead of implicit branching.
- Updated compiler and runtime regression tests to assert the new named pipeline shape and stage summaries.
- Updated compiler module documentation to publish the M3 pass order.

### File List

- `_bmad-output/implementation-artifacts/m3/1-5-refactor-the-compiler-into-an-explicit-named-pass-pipeline.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerPipelineModel.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`

### Change Log

- 2026-07-07: Refactored compiler reporting into an explicit six-stage pass pipeline and aligned compiler/runtime regression coverage.
