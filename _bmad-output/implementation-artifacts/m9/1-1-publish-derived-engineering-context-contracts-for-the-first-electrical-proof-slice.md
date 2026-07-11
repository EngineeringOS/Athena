---
baseline_commit: e5f5ef7fb0fbf10f583f0cf9acef52eb6a0e914d
---

# Story 1.1: Publish Derived Engineering Context Contracts For The First Electrical Proof Slice

Status: done

## Story

As a platform engineer,
I want Athena to define typed derived-engineering-context contracts and governed electrical inputs,
so that the first knowledge-runtime proof starts from explicit, inspectable semantics above canonical `Engineering IR`.

## FR Traceability

- FR-1: compute a first narrow set of derived engineering context from canonical `Engineering IR`
- FR-2: make later capability facts depend on explicit derived context rather than raw authored properties
- NFR-2: the same canonical state yields the same typed derived-context contract shape
- NFR-3: derived context and governed inputs remain inspectable and traceable
- NFR-4: keep the first proof intentionally narrow
- NFR-5: prepare for one fixed governed electrical knowledge pack instead of a generic rule platform

## Acceptance Criteria

1. Given the completed M8 runtime and the M9 architecture spine, when M9 begins implementation, then Athena publishes explicit typed contracts for `DerivedEngineeringContext` and the governed electrical inputs used by the first proof slice, and those contracts remain traceable to canonical semantic subjects and authored input values.
2. Given the first electrical proof slice is reviewed, when the input boundary is checked, then the supported inputs remain intentionally narrow such as motor power, voltage, power factor, efficiency, breaker current, cable current, or relay current, and the proof does not widen into multi-domain or vendor-catalog modeling.

## Tasks / Subtasks

- [x] Publish the core typed derived-context contract layer in the kernel-owned engineering model. (AC: 1)
  - [x] Add one additive contract file under `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/` for M9 nouns such as `DerivedEngineeringContext`, `DerivedEngineeringInput`, `DerivedEngineeringValue`, and traceability records rooted in canonical `StableSemanticIdentity`.
  - [x] Keep the contract layer additive beside the existing canonical `EngineeringDocument` model. Do not replace or rename M0-M8 canonical objects.
  - [x] Add clean KDoc for every new public/core Kotlin type introduced by this story.
- [x] Freeze the first governed electrical input vocabulary without implementing formulas yet. (AC: 1, 2)
  - [x] Define a narrow, explicit input kind set for the first electrical proof slice only:
    - motor power
    - voltage
    - power factor
    - efficiency
    - breaker rated current
    - cable allowed current
    - relay rated current
  - [x] Keep the vocabulary typed and inspectable. Do not collapse it into free-form stringly typed metadata if a stable enum, value class, or sealed contract is sufficient.
  - [x] Do not widen this story into vendor catalogs, standards databases, procurement attributes, or multi-domain subject sets.
- [x] Preserve traceability from canonical engineering subjects and authored inputs into the new contracts. (AC: 1)
  - [x] Ensure every governed input and future derived-context output can point back to:
    - canonical semantic subject identity
    - authored property name
    - authored provenance or equivalent stable source trace
  - [x] Reuse existing provenance and identity models where possible instead of inventing a second tracing system.
- [x] Keep the new contract layer aligned with future knowledge-pack and plugin seams without implementing runtime evaluation yet. (AC: 1, 2)
  - [x] If minimal plugin-facing or runtime-facing contract hooks are required, add them narrowly under `kernel/plugins/plugin-api` or another already-governed kernel seam.
  - [x] Do not implement derived formulas, capability facts, rule evaluation, diagnostics, impact consequence, review enrichment, or LSP publishing in Story `1.1`.
  - [x] Do not create a general rule-authoring DSL, standards-pack runtime, or remote knowledge marketplace surface in this story.
- [x] Verify the contract layer with focused tests and module documentation. (AC: 1, 2)
  - [x] Add focused tests under `kernel/engineering-model/src/test/kotlin/...` for:
    - typed contract shape
    - canonical identity traceability
    - narrow supported electrical input coverage
    - deterministic ordering or equality behavior if the contract exposes collections
  - [x] If plugin-api seams are touched, add or extend focused tests under `kernel/plugins/plugin-api/src/test/kotlin/...`.
  - [x] Update affected module README files in English and Chinese if this story changes the public core contract surface.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is the contract-publication entry story for M9.
- The success condition is not "Athena already computes electrical values." The success condition is "Athena has one clear, typed, kernel-owned contract for governed inputs and future derived context."
- Story `1.2` owns deterministic derivation of first electrical context values.
- Story `1.3` owns promotion into capability facts through a fixed knowledge pack.
- Story `1.4` owns the first sufficiency rule slice and typed diagnostics.
- Story `2.x` owns impact consequences, review vocabulary, and proof delivery through existing semantic surfaces.

### Architecture Guardrails

- Align to AD-43 by deriving M9 knowledge only from canonical engineering state and governed runtime context. Story `1.1` must not define contracts around graph state, frontend metadata, or review text. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-43---M9-Knowledge-Derivation-Starts-From-Canonical-Engineering-State-Only]
- Align to AD-44 by making `Derived Engineering Context` a first-class kernel output above raw authored properties. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-44---Derived-Engineering-Context-Is-First-Class-Kernel-Output-Above-Raw-Properties]
- Align to AD-45 by keeping capability facts separate from raw calculations. Story `1.1` prepares for that boundary and must not collapse the two concepts into one contract. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-45---Capability-Facts-Sit-Above-Derived-Context-As-Engineering-Judgements]
- Align to AD-46 by freezing a narrow electrical proof slice only. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-46---The-First-Knowledge-Proof-Uses-A-Narrow-Domain-Scoped-Knowledge-Pack]
- Align to AD-51 by keeping future growth plugin-extensible only through governed knowledge-pack seams rather than ad hoc kernel edits. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-51---Knowledge-Runtime-Remains-Plugin-Extensible-Through-Governed-Knowledge-Packs]
- Align to AD-52 by explicitly deferring vendor catalogs and standards richness. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-52---Vendor-Catalog-And-Standards-Richness-Stay-Deferred-Beyond-The-First-Proof]
- Preserve inherited AD-39 by anchoring all new contracts on canonical semantic identity. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- The current canonical engineering model already preserves the identity and provenance needed for M9:
  - `EngineeringDocument`
  - `EngineeringComponent`
  - `EngineeringPort`
  - `EngineeringConnection`
  - `EngineeringReference`
  - `StableSemanticIdentity`
  - `SourceProvenance`
- The current authored scalar surface is intentionally small:
  - `ScalarValue.Identifier`
  - `ScalarValue.StringLiteral`
  - lowered into `EngineeringPropertyValue.Symbol` and `EngineeringPropertyValue.Text`
- Story `1.1` should not widen parser grammar, token rules, or DSL syntax just to publish derived-context contracts. Prefer an additive contract layer above current canonical IR and current authored property representation.
- Reuse existing terminology and governance where possible:
  - `AthenaKnowledgeArtifactPackage` already exists for governed knowledge artifacts in `kernel/compiler`
  - `AthenaDomainLoweringContext` already maps authored properties into canonical `EngineeringProperty` values
  - `AthenaDomainPlugin` and hosted plugin seams already define the current domain extension path
- The first contract slice must support explicit input identity and traceability for future values like:
  - full-load current
  - starting current
  - thermal load
- Those values belong to derived context, not capability facts. Do not name them as facts in Story `1.1`.

### Architecture Compliance

- The story is only successful if later M9 work can point to one clean semantic ladder:
  - canonical `Engineering IR`
  - `DerivedEngineeringContext`
  - `EngineeringCapabilityFact`
  - constraint result
  - impact consequence
  - diagnostic
- Prevent these failure modes:
  - adding formulas directly into `ElectricalRuntimeDomainPlugin` with no shared typed contract
  - naming every intermediate value a "fact"
  - widening the DSL or parser before the contract boundary is frozen
  - creating renderer-, IDE-, or LSP-owned context models
  - embedding vendor-specific part data into the first M9 contract set

### Library / Framework Requirements

- Use the repo-approved stack already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse the current Kotlin/JUnit test style already present in `kernel/engineering-model`, `kernel/plugins/plugin-api`, and sibling kernel modules.
- Do not add third-party libraries just to model typed contracts, units, or traceability in Story `1.1`.

### File Structure Requirements

- Expected update files:
  - `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt` or one new sibling contract file under the same package
  - `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/`
  - `kernel/engineering-model/README.md`
  - `kernel/engineering-model/README.zh-CN.md`
- Possible narrow additive update files if a governed seam must be prepared now:
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/`
  - `kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/`
  - `kernel/plugins/plugin-api/README.md`
  - `kernel/plugins/plugin-api/README.zh-CN.md`
- Files whose current behavior and ownership must be preserved:
  - [`kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`](../../../kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt)
    - syntax remains the narrow M0/M8 authored-value surface in Story `1.1`
  - [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt)
    - lowering remains the canonical IR materialization path and should not be replaced by a second context-authority path
  - [`kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`](../../../kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt)
    - plugin contracts stay additive and governed
  - [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt)
    - currently owns the narrow electrical runtime proof and must not absorb full M9 semantics ad hoc
- Explicit non-goals:
  - no formula execution
  - no capability-fact promotion
  - no sufficiency rule evaluation
  - no semantic review or SCM changes
  - no `ide/lsp` or frontend delivery work
  - no new `extensions/knowledge-*` runtime module unless a minimal empty contract shell is strictly required

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test"`
- Recommended focused regression if plugin seams are touched:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
- Required proof checks:
  - the first M9 contract types are kernel-owned, typed, and inspectable
  - governed electrical input kinds are narrow and explicit
  - canonical semantic identity and authored provenance remain attached or reachable
  - no formula, rule, or diagnostic behavior is required to instantiate the contracts
  - no parser or renderer dependency is introduced just to define the contract surface
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`](../../../kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt) currently defines the canonical object graph and typed property values but no derived-context layer yet.
- [`kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`](../../../kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt) currently supports only identifier and string-literal scalar values.
- [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt) currently resolves canonical identities and authored provenance deterministically; Story `1.1` should build on that instead of replacing it.
- [`kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`](../../../kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt) currently owns plugin-facing lowering and validation context helpers; reuse or extend this seam only if Story `1.1` truly needs plugin-facing contract publication.
- [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt) currently proves type/direction/signal semantics only; it does not yet model motor power, voltage, current, or electrical sufficiency.
- [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt) already establishes governed knowledge-package terminology that M9 should align with rather than duplicate under a conflicting name.

### Previous Milestone Intelligence

- M0 proved the DSL-to-canonical-IR pipeline. M9 must stay above that source-of-truth layer instead of inventing a parallel semantic model.
- M3 proved hosted plugin seams. M9 should reuse governed extension boundaries instead of introducing kernel-owned domain forks.
- M6 and M8 proved semantic review and mutation authority downstream of canonical state. Story `1.1` must stay upstream of those surfaces.
- The user has repeatedly enforced these workspace rules that matter directly here:
  - physical module structure must match the architecture
  - root package stays `com.engineeringood`
  - public/core Kotlin surfaces require clean KDoc
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Current baseline commit:
  - `e5f5ef7 feat(m8): close unified mutation milestone`
- Practical implication:
  - M9 starts from a stable post-M8 kernel/runtime baseline
  - Story `1.1` should prefer additive contract publication over disruptive refactor
  - the first M9 file touches should stay in kernel-owned modules, not IDE or renderer modules

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`

### Project Structure Notes

- `m9/` is now the active milestone-local implementation artifact folder and should follow the same convention as `m6/`, `m7/`, and `m8/`.
- Keep naming explicit and easy to read:
  - `DerivedEngineeringContext`
  - `DerivedEngineeringInput`
  - `DerivedEngineeringValue`
  - `EngineeringCapabilityFact`
- Avoid burying M9 nouns inside unrelated runtime or frontend package names.
- If a new file is added, prefer a dedicated contract file over overloading `EngineeringModel.kt` into a very large mixed-responsibility file.

### References

- [Source: _bmad-output/planning-artifacts/epics-M9-2026-07-11.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m9/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m9/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m9/sprint-status.yaml]
- [Source: kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Story Completion Status

- Status: review
- Completion note: M9 derived-engineering-context contracts are implemented, documented, and verified with focused and full regression tests.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M9 epic breakdown, PRD, addendum, architecture spine, readiness report, and sprint-status review
- live review of canonical engineering model, language model, compiler lowering path, plugin contracts, runtime plugin services, and electrical extension seams
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test"` (red-phase expected compile failure before contract implementation)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:compileTestKotlin --stacktrace"` (captured unresolved M9 contract symbols)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test"` (green-phase focused verification)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"` (regression verification after stale cabinet-layout expectation fix)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"` (full sequential workspace regression)

### Completion Notes List

- Added `DerivedEngineeringContextModel.kt` to publish M9 kernel-owned subject context, governed electrical input kinds, typed derived values, and traceability records above canonical `Engineering IR`.
- Kept the first M9 contract slice additive to the existing engineering model and explicitly separate from capability-fact, rule, diagnostic, and IDE concerns.
- Added focused engineering-model contract tests for narrow input coverage, canonical identity/provenance traceability, and deterministic context ordering.
- Updated `:kernel:engineering-model` English and Chinese READMEs to describe the new M9 public contract surface.
- Fixed a stale compiler test expectation so the cabinet-view contract matches the current `connect-ports` semantic command surface.
- Verified the story with sequential Java 25 Gradle runs, including the full workspace `test` task.

### File List

- _bmad-output/implementation-artifacts/m9/1-1-publish-derived-engineering-context-contracts-for-the-first-electrical-proof-slice.md
- _bmad-output/implementation-artifacts/m9/sprint-status.yaml
- _bmad-output/implementation-artifacts/sprint-status.yaml
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt
- kernel/engineering-model/README.md
- kernel/engineering-model/README.zh-CN.md
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt
- kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextContractTest.kt

### Change Log

- 2026-07-11: Implemented M9 Story 1.1 by publishing kernel-owned derived engineering context contracts, adding focused contract tests, refreshing engineering-model module docs, and repairing one stale compiler expectation uncovered during full regression.
