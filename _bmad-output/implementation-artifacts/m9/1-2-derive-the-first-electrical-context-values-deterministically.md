---
baseline_commit: e5f5ef7fb0fbf10f583f0cf9acef52eb6a0e914d
---

# Story 1.2: Derive The First Electrical Context Values Deterministically

Status: done

## Story

As an engineer,
I want Athena to compute first-pass electrical context values from canonical state,
so that engineering reasoning uses calculated meaning instead of only raw authored properties.

## FR Traceability

- FR-1: compute the first governed derived engineering context values from canonical `Engineering IR`
- FR-2: prepare a clean separation between raw authored properties and later capability facts
- NFR-1: keep derivation upstream of runtime, IDE, renderer, and vendor adapters
- NFR-2: the same canonical state yields the same derived context values deterministically
- NFR-3: each derived value remains inspectable and attributable to canonical identities and authored inputs
- NFR-4: keep the proof small enough to review honestly

## Acceptance Criteria

1. Given a valid canonical engineering state for the first M9 proof slice, when Athena evaluates derived engineering context, then it computes at least one governed set of intermediate electrical values such as full-load current, starting current, or thermal load, and the same canonical state yields the same derived context values deterministically.
2. Given derived context output is inspected, when traceability is checked, then each derived value remains attributable to canonical semantic identities and authored inputs, and the output remains Athena-owned kernel data rather than renderer-local metadata.

## Tasks / Subtasks

- [x] Add a compiler-owned derivation path for the first M9 electrical context slice. (AC: 1, 2)
  - [x] Introduce one additive derivation unit under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/` such as `DerivedEngineeringContextDeriver.kt`.
  - [x] Keep derivation rooted in canonical `EngineeringDocument` plus the M9 contracts added in Story `1.1`; do not derive from layout, geometry, projection, runtime view state, or frontend payloads.
  - [x] Keep all new public/core Kotlin types documented with KDoc.
- [x] Normalize the narrow governed electrical inputs deterministically without widening parser scope. (AC: 1)
  - [x] Reuse the current authored property surface from canonical `EngineeringPropertyValue`.
  - [x] Add a small proof-only normalization path for values needed by the first slice, such as:
    - motor power
    - voltage
    - power factor
    - efficiency
    - breaker rated current
    - cable allowed current
    - relay rated current
  - [x] Do not widen `kernel/language` grammar, tokenization, or DSL syntax in Story `1.2`.
- [x] Compute the first deterministic derived electrical values above canonical state. (AC: 1, 2)
  - [x] Produce at least one governed intermediate value family such as:
    - full-load current
    - starting current
    - thermal load
  - [x] Ensure the output order is deterministic for the same canonical state.
  - [x] Keep these outputs in `DerivedEngineeringContext`; do not promote them to capability facts yet.
- [x] Publish derived context through the compiler-owned result surface. (AC: 1, 2)
  - [x] Expose the derived context in the compiler result path where downstream M9 stories can consume it safely, most likely through `CompilerCompilationSuccess` and any closely related compiler-facing result shape that needs parity.
  - [x] Keep the output Athena-owned kernel/compiler data. Do not publish it only as README prose, test fixtures, renderer annotations, or IDE-local metadata.
  - [x] Do not route the result through `ide/lsp`, runtime review, semantic SCM, or plugin review enrichment in Story `1.2`.
- [x] Add proof corpus and regression-safe tests for deterministic derivation. (AC: 1, 2)
  - [x] Add focused compiler and/or engineering-model tests for:
    - supported governed input parsing or normalization
    - deterministic derived-context values for the same canonical state
    - traceability from derived values back to canonical identities and authored inputs
    - absence of capability-fact or diagnostic widening in this story
  - [x] Add a small M9 example under `examples/m9/` if needed to make the first derivation slice inspectable and repeatable.
  - [x] Update affected module README files in English and Chinese if the public compiler or engineering-model contract surface changes.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.2` turns the M9 contract shell from Story `1.1` into a deterministic first-pass calculation layer.
- The success condition is not broad electrical intelligence. The success condition is one narrow, repeatable derivation path from canonical semantic state to inspectable derived values.
- Story `1.3` owns capability-fact promotion through a fixed knowledge pack.
- Story `1.4` owns sufficiency rules and diagnostics.
- Story `2.x` owns impact consequence and delivery through review, SCM, and semantic product surfaces.

### Architecture Guardrails

- Align to AD-43 by deriving context only from canonical engineering state and governed input interpretation. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-43---M9-Knowledge-Derivation-Starts-From-Canonical-Engineering-State-Only]
- Align to AD-44 by making `DerivedEngineeringContext` a real kernel output rather than passive authored metadata. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-44---Derived-Engineering-Context-Is-First-Class-Kernel-Output-Above-Raw-Properties]
- Align to AD-45 by keeping derived values below capability facts. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-45---Capability-Facts-Sit-Above-Derived-Context-As-Engineering-Judgements]
- Align to AD-46 by keeping the derivation family intentionally narrow and electrical only. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-46---The-First-Knowledge-Proof-Uses-A-Narrow-Domain-Scoped-Knowledge-Pack]
- Align to AD-47 by keeping derivation typed and deterministic before later rule evaluation begins. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-47---Constraint-Evaluation-Is-Deterministic-Typed-And-Separate-From-Structural-Validation]
- Align to AD-49 by not widening into product-surface delivery yet. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-49---Existing-Semantic-Delivery-Surfaces-Remain-The-Product-Path]

### Technical Requirements

- Story `1.1` already published the contract nouns in `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt`.
- The current compiler result seam is in:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- The current canonical authored-value surface is still:
  - `EngineeringPropertyValue.Symbol`
  - `EngineeringPropertyValue.Text`
- The most likely implementation shape is:
  - one compiler-owned derivation service for the first electrical proof slice
  - one additive compiler result field that exposes `DerivedEngineeringContext`
  - focused normalization helpers for the narrow proof input vocabulary
- Prefer a compiler-owned derivation service now rather than embedding formula logic directly in `ElectricalRuntimeDomainPlugin`. The plugin already proves domain structure and view semantics, but Story `1.2` is about kernel-owned derivation above canonical IR.
- If quantity parsing is needed for values like `7.5kw`, `400V`, `18A`, or `0.86`, keep the parser narrow and proof-owned in compiler/kernel code. Do not widen the DSL grammar in this story.

### Architecture Compliance

- The story is only successful if later M9 work can depend on one stable ladder:
  - canonical `EngineeringDocument`
  - normalized governed inputs
  - `DerivedEngineeringContext`
  - later capability facts
- Prevent these failure modes:
  - storing derived values only in tests or example fixtures
  - computing values from geometry, layout, or projection state
  - reusing the word "fact" for raw derived calculations
  - widening into rule evaluation, sufficiency diagnostics, or review output too early
  - burying formula logic inside IDE, runtime widget, or renderer code

### Library / Framework Requirements

- Use the repo-approved stack already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse the current Kotlin/JUnit patterns already used in `kernel/compiler` and `kernel/engineering-model`.
- Do not add third-party unit or quantity libraries in Story `1.2` unless absolutely necessary. The first proof slice should stay small and explicit.

### File Structure Requirements

- Expected update files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
  - one new derivation file under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/` if contract-level assertions need extension
  - `kernel/compiler/README.md`
  - `kernel/compiler/README.zh-CN.md`
- Possible additive proof files:
  - `examples/m9/`
- Files whose current behavior and ownership must be preserved:
  - [`kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt`](../../../kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt)
    - remains the contract boundary published in Story `1.1`
  - [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt)
    - lowering remains canonical IR materialization, not electrical calculation ownership
  - [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt)
    - do not turn this plugin into the first hidden knowledge-runtime engine
  - [`kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`](../../../kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt)
    - parser/value grammar stays unchanged in Story `1.2`
- Explicit non-goals:
  - no capability-fact model
  - no knowledge-pack rule execution
  - no engineering sufficiency diagnostics
  - no semantic review, SCM, or LSP delivery work
  - no renderer or graph workbench changes

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Recommended focused companion verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test"`
- Optional wider regression after focused compiler checks pass:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- Required proof checks:
  - same canonical state yields the same derived context values
  - derived values remain attributable to source inputs and canonical identities
  - the compiler exposes derived context as Athena-owned kernel/compiler data
  - no capability facts, diagnostics, or review outputs are introduced by Story `1.2`
  - no parser widening is required to land the first proof slice
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt) currently exposes semantic result, layout, geometry, projection, and rendering, but no derived engineering context yet.
- [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt) currently owns parse, lower, validate, backend preparation, backend emission, and projection derivation.
- [`kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt`](../../../kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt) now defines the first M9 contract layer but no computation path yet.
- [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt) currently contributes type/direction/signal semantics, view definitions, render intent, runtime commands, and review enrichment, but not electrical quantity derivation.

### Previous Story Intelligence

- Story `1.1` already froze the narrow governed input vocabulary and the contract shapes. Story `1.2` should reuse those types directly rather than introduce a second context model.
- Story `1.1` also proved the current authored-value limitation explicitly, so Story `1.2` must normalize existing `EngineeringPropertyValue` content instead of widening parser scope.
- The current repo rules still matter directly:
  - physical structure must match architecture
  - root package remains `com.engineeringood`
  - public/core Kotlin surfaces require clean KDoc
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Current milestone baseline:
  - `e5f5ef7 feat(m8): close unified mutation milestone`
- Current M9 grounding:
  - Story `1.1` is complete and in review, so Story `1.2` can assume the contract shell exists.
- Practical implication:
  - keep Story `1.2` additive and compiler-owned
  - avoid disruptive refactors in runtime, plugin host, or product surfaces

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`

### Project Structure Notes

- `m9/` remains the active milestone-local implementation artifact folder.
- This story should make the eventual M9 stack easier to explain:
  - Story `1.1` publishes contracts
  - Story `1.2` derives deterministic context
  - Story `1.3` promotes capability facts
  - Story `1.4` evaluates sufficiency
- If a new compiler file is added, prefer a dedicated derivation file rather than embedding all M9 logic into `AthenaCompiler.kt`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M9-2026-07-11.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m9/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m9/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m9/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m9/1-1-publish-derived-engineering-context-contracts-for-the-first-electrical-proof-slice.md]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt]
- [Source: kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Story Completion Status

- Status: review
- Completion note: Compiler-owned deterministic M9 derived-context computation is implemented, exposed through compiler results, covered by focused tests, and regression-checked sequentially on Java 25.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- cleanup after an earlier timed-out full-test attempt:
  - `Remove-Item -Recurse -Force 'kernel\runtime\build\test-results\test' -ErrorAction SilentlyContinue`
  - `Remove-Item -Recurse -Force 'kernel\runtime\build\reports\tests\test' -ErrorAction SilentlyContinue`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`

### Completion Notes List

- Added `DerivedEngineeringContextDeriver` as a compiler-owned deterministic derivation boundary above canonical `EngineeringDocument`.
- Exposed `derivedContext` through `CompilerCompilationSuccess` without widening parser grammar, plugin responsibilities, or renderer/runtime metadata.
- Normalized the first narrow motor input slice from existing authored `EngineeringPropertyValue` data and derived deterministic full-load current plus thermal load.
- Added a repeatable M9 proof example and focused compiler tests for normalization, determinism, traceability, and compiler result exposure.
- Verified the implementation sequentially on Windows with Java 25. One earlier full-test attempt timed out and left `kernel:runtime` test results in a broken Gradle state; cleaning that directory and rerunning sequentially restored a green full-test run.

### File List

- _bmad-output/implementation-artifacts/m9/1-2-derive-the-first-electrical-context-values-deterministically.md
- examples/m9/motor-derived-context.athena
- kernel/compiler/README.md
- kernel/compiler/README.zh-CN.md
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/DerivedEngineeringContextDeriver.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DerivedEngineeringContextDeriverTest.kt
