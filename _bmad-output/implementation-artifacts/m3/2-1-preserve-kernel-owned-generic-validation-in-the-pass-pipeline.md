---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 2.1: Preserve Kernel-Owned Generic Validation In The Pass Pipeline

Status: done

## Story

As a platform engineer,
I want generic validation to remain explicitly kernel-owned inside the M3 pass pipeline,
so that domain plugins add rules without weakening the generic semantic guarantees inherited from M0 to M2.

## Acceptance Criteria

1. Given the compiler now exposes a named pass pipeline, when generic validation is reviewed in the M3 architecture, then Athena keeps duplicate identifier checks, missing reference checks, and generic graph validation inside kernel-owned validation contracts, and those rules do not depend on any hosted domain plugin validation contribution to function.
2. Given plugins may contribute additional validation in M3, when the validation boundary is implemented, then the kernel validation path is executed as a separate generic concern from domain validation, and compiler results expose the distinction instead of flattening all diagnostics into one unstructured source.
3. Given M0 to M2 behavior must remain intact, when validation ownership is inspected across semantic, runtime, and projection flows, then `Engineering IR` remains the canonical subject of generic validation, and layout, geometry, and renderer layers do not become fallback semantic validators.
4. Given the generic validation boundary is implemented, when the standard Java `25` build and validation tests are executed, then the workspace builds successfully and generic validation still works when no domain validation contributor participates in the validate stage, and Epic 2 continues from a stable kernel-owned semantic baseline.

## Tasks / Subtasks

- [x] Expose the compiler-owned validation boundary in public compiler results. (AC: 1, 2, 3, 4)
  - [x] Add an inspectable validation breakdown model that separates semantic-enrichment, kernel, and domain diagnostics.
  - [x] Attach the validation breakdown to `CompilerCompilationSuccess`.
  - [x] Make the validate-pass summary report boundary counts so pass output stays inspectable.
- [x] Preserve kernel validation as an explicit compiler concern independent from domain validation callbacks. (AC: 1, 2, 3, 4)
  - [x] Keep `EngineeringIrValidator` as the sole source of generic validation diagnostics inside `AthenaCompiler`.
  - [x] Aggregate domain diagnostics only after the kernel validation result has been computed.
  - [x] Preserve the final canonical `SemanticValidationResult` contract for downstream consumers.
- [x] Prove the validation boundary with focused regression coverage. (AC: 1, 2, 3, 4)
  - [x] Add compiler tests that attribute generic diagnostics to the kernel side and electrical rules to the domain side.
  - [x] Add a compiler test that proves kernel validation still runs when a lowering-only plugin participates and no domain validation contributor is active.
  - [x] Verify compiler, plugin-host, runtime, electrical-extension, and full-build regressions sequentially on Java `25`.

## Dev Notes

### Story Intent

- Story `2.1` makes the validation boundary inspectable in the compiler-facing result model instead of keeping kernel and domain diagnostics mixed together implicitly.
- The canonical `SemanticValidationResult` remains the downstream truth for runtime and backend orchestration, but the compiler now also exposes where those diagnostics came from.
- Full zero-plugin hosted-state proof still belongs to Epic `3.2`. In the current M3 architecture, the absence of any lowering participant still yields `domain.semantics.unavailable`; this story proves that kernel validation remains separate and active when generic IR exists and no domain validation contributor participates.

### Implementation Direction

- `CompilerValidationBreakdown` now separates:
  - semantic-enrichment diagnostics
  - kernel-owned generic validation diagnostics
  - domain/plugin validation diagnostics
- `AthenaCompiler` computes the kernel validation result first, then aggregates domain diagnostics afterward, and finally rebuilds the canonical `SemanticValidationResult` from the three explicit buckets.
- Validate-pass pipeline summaries now surface `kernel=`, `domain=`, and `enrichment=` counts so host/runtime inspection can see the boundary without re-deriving it from raw diagnostics.
- A new synthetic `GenericLoweringOnlyTestPlugin` proves the validate pass can emit kernel diagnostics even when no plugin participates in the `VALIDATE` stage.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-21-preserve-kernel-owned-generic-validation-in-the-pass-pipeline]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-4---kernel-validation-stays-generic-domain-validation-stays-external]
- [Source: kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `codegraph explore "Story 2.1 preserve kernel owned generic validation AthenaCompiler validateSemantics EngineeringIrValidator SemanticValidationResult domain validation compiler models"`
- `codegraph node kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `codegraph node kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `Get-Content kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added `CompilerValidationBreakdown` so compiler results now distinguish kernel validation from domain/plugin validation and semantic-enrichment diagnostics.
- Kept `EngineeringIrValidator` as the kernel-owned generic validation engine and made the compiler aggregate domain diagnostics only after the kernel result is computed.
- Updated validate-pass summaries to report boundary counts, improving inspectability without changing downstream semantic truth.
- Added compiler regression coverage proving:
  - generic reference diagnostics are attributed to the kernel side,
  - electrical property/signal rules remain attributed to the domain side,
  - kernel validation still runs when a lowering-only plugin participates and no validate-stage plugin is active.
- Verified the story with sequential Java `25` runs for compiler, plugin-host, runtime, electrical extension, and a full workspace build.

### File List

- `_bmad-output/implementation-artifacts/m3/2-1-preserve-kernel-owned-generic-validation-in-the-pass-pipeline.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`

### Change Log

- 2026-07-07: Exposed compiler validation breakdowns, preserved kernel validation as an explicit pass responsibility, and verified the boundary with sequential Java 25 regressions.
