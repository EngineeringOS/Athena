---
baseline_commit: e5f5ef7fb0fbf10f583f0cf9acef52eb6a0e914d
---

# Story 1.3: Promote Capability Facts Through A Fixed Electrical Knowledge Pack

Status: done

## Story

As a platform engineer,
I want Athena to promote selected derived context into capability facts through a fixed governed knowledge pack,
so that constraint evaluation consumes explicit engineering judgements instead of raw formulas.

## FR Traceability

- FR-2: derive capability facts from derived context through explicit domain semantics
- NFR-1: keep capability promotion upstream of runtime presentation, IDE, and renderer layers
- NFR-2: the same canonical state and active knowledge pack yield the same capability facts deterministically
- NFR-3: each capability fact remains inspectable and attributable to derived context plus governed knowledge-pack provenance
- NFR-5: the first proof ships as one fixed governed knowledge pack instead of a generic rule-authoring system

## Acceptance Criteria

1. Given derived engineering context is available for the first electrical proof slice, when Athena applies the fixed M9 knowledge-pack semantics, then it produces inspectable capability facts such as required protection current, cable demand, or relay sizing demand, and those facts remain distinguishable from raw calculations and vendor-part data.
2. Given the M9 knowledge-pack boundary is reviewed, when extensibility and scope are checked, then the first proof uses one fixed governed electrical knowledge pack rather than a generic rule-authoring system, and any future knowledge growth remains deferred behind governed plugin-hosted seams.

## Tasks / Subtasks

- [x] Publish typed capability-fact contracts above derived context. (AC: 1)
  - [x] Add additive M9 capability-fact models under `kernel/engineering-model`.
  - [x] Keep the model typed, deterministic, and traceable to derived-context values plus governed knowledge-pack provenance.
  - [x] Keep all new public/core Kotlin types documented with KDoc.
- [x] Introduce one fixed governed electrical knowledge pack artifact. (AC: 1, 2)
  - [x] Add one reviewed directory-backed knowledge pack under `extensions/knowledge-electrical-basic/`.
  - [x] Keep the pack data-only and narrow to the first electrical capability-promotion slice.
  - [x] Use the existing governed compiler knowledge-package seam instead of plugin code or renderer-local metadata.
- [x] Promote first-wave capability facts through the fixed pack semantics. (AC: 1)
  - [x] Add one compiler-owned capability promoter that consumes `DerivedEngineeringContext` plus active reviewed knowledge packages.
  - [x] Promote at least `REQUIRED_PROTECTION_CURRENT`, `REQUIRED_CABLE_CURRENT`, and `REQUIRED_RELAY_SIZING_CURRENT` from `FULL_LOAD_CURRENT`.
  - [x] Keep the output distinct from raw calculations by publishing typed capability facts with comparison semantics and knowledge-pack trace.
- [x] Publish capability facts through the compiler-owned result surface. (AC: 1)
  - [x] Expose `capabilityFacts` through `CompilerCompilationSuccess`.
  - [x] Keep the output Athena-owned kernel/compiler data.
  - [x] Do not widen into rule evaluation, diagnostics, review delivery, or vendor catalogs in Story `1.3`.
- [x] Add regression-safe tests and update affected docs. (AC: 1, 2)
  - [x] Add engineering-model contract tests for capability-fact vocabulary and deterministic ordering.
  - [x] Add compiler tests proving fixed-pack promotion and empty output when the pack is absent.
  - [x] Update affected README files in English and Chinese.
  - [x] Run Gradle verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `1.3` is the first semantic lift from derived context into engineering judgements.
- The proof remains intentionally narrow: one fixed electrical pack, one capability-promotion payload, and one first subject family rooted in motor full-load current.
- Story `1.4` will consume these facts for fixed rule-slice evaluation and engineering sufficiency diagnostics.

### Architecture Guardrails

- Capability facts sit above derived context and below later rule evaluation. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-45---Capability-Facts-Sit-Above-Derived-Context-As-Engineering-Judgements]
- The first proof must use one narrow fixed electrical knowledge pack only. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-46---The-First-Knowledge-Proof-Uses-A-Narrow-Domain-Scoped-Knowledge-Pack]
- Plugins and packs may extend knowledge only through governed hosted seams. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md#AD-51---Knowledge-Runtime-Remains-Plugin-Extensible-Through-Governed-Knowledge-Packs]

### Completion Notes

- Added `EngineeringCapabilityFactModel.kt` and `EngineeringCapabilityFactContractTest.kt` in `:kernel:engineering-model`.
- Added `EngineeringCapabilityFactPromoter.kt` in `:kernel:compiler` and exposed `capabilityFacts` through `CompilerCompilationSuccess`.
- Added the fixed reviewed pack under `extensions/knowledge-electrical-basic/` with manifest, payload, and README files.
- Promoted `FULL_LOAD_CURRENT` into required protection, cable, and relay current judgements with typed comparison semantics and knowledge-pack traceability.
- Kept the proof below rule evaluation and diagnostics; no parser widening, no plugin code execution, no renderer ownership, and no vendor catalog modeling were introduced.

## Testing

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:compiler:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

## File List

- `_bmad-output/implementation-artifacts/m9/1-3-promote-capability-facts-through-a-fixed-electrical-knowledge-pack.md`
- `extensions/knowledge-electrical-basic/README.md`
- `extensions/knowledge-electrical-basic/README.zh-CN.md`
- `extensions/knowledge-electrical-basic/athena-knowledge.properties`
- `extensions/knowledge-electrical-basic/payload/capability-semantics.properties`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringCapabilityFactPromoter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaKnowledgePackageLoaderTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringCapabilityFactPromoterTest.kt`
- `kernel/engineering-model/README.md`
- `kernel/engineering-model/README.zh-CN.md`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringCapabilityFactModel.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringCapabilityFactContractTest.kt`
