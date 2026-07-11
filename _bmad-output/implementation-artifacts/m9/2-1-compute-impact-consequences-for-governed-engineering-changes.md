---
baseline_commit: e5f5ef7fb0fbf10f583f0cf9acef52eb6a0e914d
---

# Story 2.1: Compute Impact Consequences For Governed Engineering Changes

Status: done

## Story

As a reviewer,
I want Athena to compute affected engineering subjects when a governed value changes,
so that I can see downstream consequence instead of only the directly edited field.

## FR Traceability

- FR-5: compute a first narrow impact consequence set when a relevant engineering value changes
- NFR-1: keep impact computation upstream of renderer, IDE, and vendor adapters
- NFR-2: the same before/after canonical state yields the same impact consequences deterministically
- NFR-3: affected subjects and impact reasons remain inspectable and anchored on canonical semantic identities

## Acceptance Criteria

1. Given a before and after canonical engineering state for the first M9 proof slice, when a governed engineering value changes such as motor power or protection sizing, then Athena identifies at least one affected downstream semantic subject or rule evaluation set, and impact consequence computation works over semantic dependency, derived-context, and capability relationships rather than textual diff alone.
2. Given repeated governed changes happen in the same repository session, when Athena recomputes impact, then the consequence set remains deterministic for the same before and after state, and affected subjects remain anchored on canonical semantic identities.

## Tasks / Subtasks

- [x] Publish typed impact-consequence contracts above constraint evaluation. (AC: 1, 2)
  - [x] Add additive M9 impact models under `kernel/engineering-model`.
  - [x] Keep the model typed, deterministic, and anchored on affected plus trigger semantic identities.
  - [x] Publish short categorized reason labels for governed input, derived-context, capability-fact, and constraint-evaluation change.
- [x] Add a deterministic before/after impact calculator in the compiler-facing kernel path. (AC: 1, 2)
  - [x] Compare governed inputs through derived-context semantics instead of raw source diff.
  - [x] Compare derived values, capability facts, and constraint evaluations per stable semantic subject.
  - [x] Preserve affected-subject propagation through typed constraint relationships.
- [x] Expose the typed impact calculation through the public compiler facade. (AC: 1)
  - [x] Add `AthenaCompiler.calculateImpactConsequences(before, after)`.
  - [x] Keep the result independent from review wording, LSP envelopes, or renderer state.
- [x] Add regression-safe tests and update affected docs. (AC: 1, 2)
  - [x] Add engineering-model contract tests for impact vocabulary and canonical ordering.
  - [x] Add compiler tests for deterministic motor-power and breaker-sizing impact computation.
  - [x] Update affected README files in English and Chinese.
  - [x] Run Gradle verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `2.1` proves that Athena can compare two governed canonical states and publish engineering impact as typed kernel data before runtime, review, and LSP delivery pick it up in later stories.
- The first impact contract stays intentionally narrow: it names affected semantic subjects, trigger subjects, and changed semantic layers instead of trying to become a review UI model.
- Short categorized reason labels close the PRD open question without widening M9 into authoring or expert-system scope.

### Completion Notes

- Added `EngineeringImpactConsequenceModel.kt` and `EngineeringImpactConsequenceContractTest.kt` in `:kernel:engineering-model`.
- Added `EngineeringImpactConsequenceCalculator.kt` in `:kernel:compiler`.
- Exposed `AthenaCompiler.calculateImpactConsequences(before, after)` as the typed comparison seam for later runtime and review stories.
- Kept impact computation semantic-first by comparing governed inputs, derived values, capability facts, and constraint evaluations instead of raw textual deltas.
- Preserved affected-subject anchoring through `StableSemanticIdentity` and constraint `affectedSubjectIdentities`.

## Testing

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:compiler:test"`

## File List

- `_bmad-output/implementation-artifacts/m9/2-1-compute-impact-consequences-for-governed-engineering-changes.md`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringImpactConsequenceCalculator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringImpactConsequenceCalculatorTest.kt`
- `kernel/engineering-model/README.md`
- `kernel/engineering-model/README.zh-CN.md`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringImpactConsequenceModel.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringImpactConsequenceContractTest.kt`
