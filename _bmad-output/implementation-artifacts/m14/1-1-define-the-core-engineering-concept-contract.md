---
baseline_commit: 72b498f
---

# Story 1.1: Define The Core Engineering Concept Contract

Status: done

## Story

As a platform engineer,  
I want Athena to define a vendor-neutral engineering concept contract,  
so that authored component references resolve into stable engineering meaning instead of only vendor ids or loose kind strings.

## FR Traceability

- FR-1: resolve authored component references into governed engineering concepts
- FR-2: publish component knowledge as new kernel contracts
- FR-6: model vendor parts as implementations of engineering concepts
- NFR-1: canonical authored engineering meaning remains in `Engineering IR`
- NFR-4: engineering concepts remain vendor-neutral; vendor parts remain implementations
- NFR-7: M14 output remains read-only resolved knowledge consumed by later layers
- NFR-8: M14 excludes behavior model, simulation, broad standards packs, and broad catalog parity

## Acceptance Criteria

1. Given M14 introduces component knowledge, when the core contract is reviewed, then engineering concept identity is defined in a dedicated kernel module rather than inside compiler-local helper structures, and the contract is vendor-neutral and suitable for future domain or vendor packs.
2. Given one authored component reference is resolved, when Athena publishes the resolved result, then the result includes both canonical authored semantic subject identity and resolved engineering concept identity.

## Tasks / Subtasks

- [x] Define the first vendor-neutral engineering concept contract in a dedicated kernel module. (AC: 1, 2)
  - [x] Introduce `kernel/component-model` as the home for engineering concept identity and component definition contracts.
  - [x] Use domain-neutral naming such as `EngineeringConceptId`, `EngineeringConceptDefinition`, `ResolvedComponentDefinition`, or equivalent narrow first vocabulary.
  - [x] Keep all public/core Kotlin types documented with clean KDoc.
- [x] Freeze the core ownership boundary in code and docs. (AC: 1)
  - [x] Make the contract comments explicit that canonical authored engineering meaning remains in `Engineering IR`.
  - [x] Make the contract comments explicit that engineering concepts are vendor-neutral semantic knowledge, not vendor product ids.
  - [x] Make the contract comments explicit that M14 output is read-only resolved knowledge and not a new mutation path.
- [x] Keep Story `1.1` narrow and foundational. (AC: 1, 2)
  - [x] Do not widen Story `1.1` into semantic port contracts, physical traits, or deterministic pack loading yet.
  - [x] Do not widen Story `1.1` into behavior models, simulation, or catalog breadth.
  - [x] Do not introduce renderer, projection, or presentation logic in the concept-model contract.
- [x] Add focused tests and module documentation for the new contract layer. (AC: 1, 2)
  - [x] Add focused tests under the owning kernel module to prove:
    - vendor-neutral concept naming
    - separation from authored semantic subject identity
    - suitability for later vendor and domain packs
  - [x] Add module README files in English and Chinese if a new kernel module is introduced.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is the naming-and-boundary freeze for the M14 component knowledge layer.
- The success condition is not "Athena already resolves the full Siemens catalog."
- The success condition is "Athena now has a vendor-neutral engineering concept contract that later resolution, ports, physical traits, and vendor mappings can target without poisoning the architecture."
- Story `1.2` should define the vendor implementation mapping contract in `kernel/part-model`.
- Story `2.1` and `2.3` should define semantic port and minimal physical-trait contracts.

### Architecture Guardrails

- Align to AD-75: M14 introduces component knowledge resolution between `Engineering IR` and downstream consumers. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md#AD-75---M14-Introduces-Component-Knowledge-Resolution-Between-Engineering-IR-And-Downstream-Consumers]
- Align to AD-77: engineering concepts are vendor-neutral; vendor parts are implementations. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md#AD-77---Engineering-Concepts-Are-Vendor-Neutral-Vendor-Parts-Are-Implementations]
- Align to AD-80: M14 outputs feed M9 and M13 and do not create a new mutation path. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md#AD-80---M14-Outputs-Feed-M9-And-M13-M14-Does-Not-Create-A-New-Mutation-Path]
- Align to AD-82: DSL remains canonical serialization, not the default human interface. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md#AD-82---DSL-Remains-Canonical-Serialization-Not-The-Default-Human-Interface]
- Preserve inherited AD-13, AD-16, AD-34, AD-43, and AD-67: package governance remains authoritative, `athena.lock` remains the reproducibility anchor, one mutation authority remains binding, knowledge derivation starts from canonical state only, and downstream presentation remains a separate layer. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- Current state already contains:
  - canonical `Engineering IR`
  - deterministic package governance through M5
  - one mutation authority through M8
  - executable knowledge runtime through M9
  - downstream `Presentation IR` through M13
- Story `1.1` must freeze the component-concept contract before pack loading and downstream consumers become deeper.
- The contract should be shaped so later work can target it:
  - electrical concept packs
  - vendor implementation packs
  - M9 derived-context and capability logic
  - projection and presentation pack selection
- Explicitly avoid top-level names like:
  - `SiemensComponentDefinition`
  - `ElectricalOnlyComponentType`
  if they would freeze the system around one vendor or one narrow domain at the root layer.

### Architecture Compliance

- The story is only successful if later M14 work can point to one clean ladder:
  - canonical `Engineering IR`
  - resolved engineering concept
  - later semantic ports / physical traits / vendor mappings
  - downstream M9 and M13 consumers
- Prevent these failure modes:
  - treating vendor product ids as the concept type system
  - keeping concept contracts hidden inside compiler-only code
  - creating a second mutation or authoring path
  - widening Story `1.1` into ports, traits, packs, or runtime orchestration too early

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit style already present in kernel modules.
- Do not add third-party libraries just to model the new contract.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/component-model/build.gradle.kts`
  - `kernel/component-model/src/main/kotlin/...`
  - `kernel/component-model/src/test/kotlin/...`
  - `kernel/component-model/README.md`
  - `kernel/component-model/README.zh-CN.md`
  - `settings.gradle.kts`
- Explicit non-goals:
  - no `kernel/part-model` contract yet unless a tiny shared id type is unavoidable
  - no `kernel/connection-model` contract yet
  - no `kernel/physical-model` contract yet
  - no pack loading or runtime integration yet

### Testing Requirements

- Minimum verification commands should target the new module directly first.
- Required proof checks:
  - vendor-neutral concept naming
  - stable separation between authored semantic subject identity and resolved concept identity
  - no concurrent Gradle build or test execution on Windows

### Current Code State To Preserve

- `Engineering IR` remains canonical authored engineering truth.
- M5 package governance remains the reproducibility foundation.
- M8 remains the only write authority.
- M9 remains the later knowledge-runtime layer above resolved component knowledge.
- M13 remains the downstream presentation-language layer.

### Previous Milestone Intelligence

- M9 already proved executable knowledge above canonical state; Story `1.1` must not relabel itself as the first knowledge-runtime contract.
- M13 already proved downstream presentation language; Story `1.1` must stay upstream of that layer.
- M14 now changes the kernel substrate for component identity, so Story `1.1` must be deliberate, narrow, and vendor-neutral.

### Git Intelligence Summary

- Current baseline commit:
  - `72b498f feat(m13): close presentation foundation proof`
- Practical implication:
  - M14 begins after the completed M13 closeout state
  - first work should freeze concept naming and ownership before pack loading and downstream consumer integration

### References

- [Source: _bmad-output/planning-artifacts/epics-M14-2026-07-13.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m14/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m14/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md]
- [Source: docs/roadmap/athena-milestone-roadmap.md]

## Story Completion Status

- Status: done
- Completion note: implemented `:kernel:component-model`, added focused contract tests, and verified with `java25; .\gradlew.bat :kernel:component-model:test --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 draft and product-position review
- M14 PRD review and addendum alignment
- M14 architecture-spine drafting
- M14 epic breakdown drafting
- Story `1.1` implementation and sequential Java 25 verification
