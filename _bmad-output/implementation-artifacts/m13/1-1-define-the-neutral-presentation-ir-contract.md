---
baseline_commit: f81d61d
---

# Story 1.1: Define The Neutral Presentation IR Contract

Status: done

## Story

As a platform engineer,  
I want Athena to define a domain-neutral `Presentation IR`,  
so that electrical can be the first serious pack without freezing the architecture around one domain name.

## FR Traceability

- FR-1: introduce a dedicated `Presentation IR` boundary
- FR-2: keep `Presentation IR` downstream of semantic and projection authority
- FR-9: support multiple renderer backends over one `Presentation IR`
- NFR-1: canonical engineering meaning remains in `Engineering IR`
- NFR-2: `Projection Model` remains the renderer-neutral view contract
- NFR-3: `Presentation IR` remains rebuildable from upstream artifacts and may not become a second semantic core
- NFR-4: electrical is the first serious presentation pack, but the top-level architecture name remains domain-neutral
- NFR-6: M13 must not smuggle semantic macro or engineering assembly into presentation ownership

## Acceptance Criteria

1. Given M13 introduces the new downstream layer, when the contract is reviewed, then the top-level model is named `Presentation IR` rather than an electrical-only name, and the contract remains suitable for future domain packs such as electrical, SCADA, or documentation.
2. Given the layer boundary is inspected, when architecture owners trace ownership, then `Engineering IR` remains semantic truth, `Projection Model` remains view contract, and `Presentation IR` remains downstream language only.

## Tasks / Subtasks

- [ ] Define the first typed `Presentation IR` contract surface in a kernel-owned downstream module. (AC: 1, 2)
  - [ ] Choose the contract home intentionally:
    - prefer extending `kernel/projection-model` only if the new layer still belongs there cleanly
    - otherwise introduce a dedicated presentation-facing kernel module or package boundary with straightforward naming
  - [ ] Use a domain-neutral name such as `PresentationDocument`, `PresentationSubject`, `PresentationOccurrence`, or equivalent narrow first vocabulary.
  - [ ] Keep public/core Kotlin types documented with clean KDoc.
- [ ] Freeze the ownership ladder explicitly in code and docs. (AC: 2)
  - [ ] Make the contract comments explicit that `Engineering IR` remains canonical truth.
  - [ ] Make the contract comments explicit that `Projection Model` remains the renderer-neutral view contract.
  - [ ] Make the contract comments explicit that `Presentation IR` is downstream language only and rebuildable from upstream artifacts.
- [ ] Keep the first contract narrow and foundational. (AC: 1, 2)
  - [ ] Do not widen Story `1.1` into full primitive-pack implementation, composite-pack implementation, or backend-specific rendering.
  - [ ] Do not encode semantic macro or engineering assembly into the `Presentation IR` layer.
  - [ ] Do not hardcode electrical-only naming at the top architecture layer.
- [ ] Add focused tests and module documentation for the new contract layer. (AC: 1, 2)
  - [ ] Add or extend focused tests under the owning kernel module to prove:
    - domain-neutral naming
    - downstream-only ownership
    - compatibility with electrical-first pack planning
  - [ ] Update affected README files in English and Chinese if public/core contract surfaces change.
  - [ ] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is the naming-and-boundary freeze for M13.
- The success condition is not "Athena already has primitive symbols rendered."
- The success condition is "Athena now has a neutral downstream presentation contract that later electrical packs can target without poisoning the architecture name."
- Story `1.2` should derive `Presentation IR` from existing projection-family, sheet, notation, anchor, and routing contracts.
- Story `1.3` should publish the new layer through runtime and existing transport seams.

### Architecture Guardrails

- Align to AD-67: `Presentation IR` is a new dedicated downstream layer. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md#AD-67---M13-Introduces-Presentation-IR-As-A-New-Dedicated-Downstream-Layer]
- Align to AD-68: primitive presentation atoms are downstream assets, not engineering entities. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md#AD-68---Primitive-Presentation-Atoms-Are-Downstream-Assets-Not-Engineering-Entities]
- Align to AD-70: semantic macro or engineering assembly is not part of `Presentation IR`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md#AD-70---Semantic-Macro-Or-Engineering-Assembly-Is-Not-Part-Of-Presentation-IR]
- Preserve inherited AD-27, AD-28, AD-53, AD-56, and AD-62: projection remains renderer-neutral, engineering identity remains canonical, electrical remains downstream, symbol/notation packs remain governed, and readability remains a downstream consequence of canonical state. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- Current state already contains:
  - canonical `Engineering IR`
  - `Projection Model`
  - electrical projection-family, sheet, notation, repeated-reference, anchor, endpoint, and routing-corridor contracts
  - renderer-hardening direction recorded in M12
- Story `1.1` must freeze the next layer name and ownership, not skip ahead into rendering features.
- The contract should be shaped so future packs can target it:
  - electrical presentation pack
  - future SCADA presentation pack
  - future documentation presentation pack
- Explicitly avoid top-level names like:
  - `ElectricalRenderIr`
  - `ElectricalPresentationDocument` as the root architecture type
  if they would freeze the system around one domain instead of one presentation layer.

### Architecture Compliance

- The story is only successful if later M13 work can point to one clean ladder:
  - canonical `Engineering IR`
  - `Projection Model`
  - neutral `Presentation IR`
  - domain presentation packs
  - renderer backend
- Prevent these failure modes:
  - keeping electrical-only naming at the root layer
  - placing semantic macro or engineering assembly into presentation ownership
  - letting frontend widgets define the first real contract instead of a kernel-owned or kernel-adjacent typed boundary
  - widening Story `1.1` into pack implementation or backend rendering work

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit style already present in kernel modules.
- Do not add third-party libraries just to name or model the new contract.

### File Structure Requirements

- Expected update files likely include:
  - one kernel-owned contract location for the first `Presentation IR` types
  - focused tests under the same owner module
  - module README files in English and Chinese if public/core surface changes
- Possible owning locations to assess carefully:
  - `kernel/projection-model`
  - a new kernel module if `Presentation IR` deserves a clean physical boundary
- Explicit non-goals:
  - no primitive electrical pack implementation yet
  - no composite pack implementation yet
  - no proof backend rendering yet
  - no semantic macro system

### Testing Requirements

- Minimum verification commands for story completion should be chosen according to the final owning module.
- If a new kernel module is introduced, verify that module directly first.
- If the contract stays in an existing kernel module, run that module's tests sequentially with Java 25.
- Required proof checks:
  - the new layer uses domain-neutral top-level naming
  - ownership comments and tests make the downstream-only posture explicit
  - no concurrent Gradle build or test execution on Windows

### Current Code State To Preserve

- `Engineering IR` remains canonical semantic truth.
- `Projection Model` remains the renderer-neutral view contract.
- M11 electrical projection-family and notation contract surfaces remain valid upstream inputs.
- M12 renderer-foundation planning already states that a neutral presentation layer is needed; Story `1.1` should freeze that in implementation-facing form.

### Previous Milestone Intelligence

- M11 already proved the electrical downstream projection vocabulary.
- M12 already proved the need for a new downstream presentation layer and recorded the renderer-foundation direction.
- M13 now changes the architecture center for presentation vocabulary, so Story `1.1` must be deliberate, narrow, and terminology-correct.

### Git Intelligence Summary

- Current baseline commit:
  - `f81d61d docs: add m12 planning artifacts`
- Practical implication:
  - M13 begins after the M12 planning closeout state
  - first work should freeze naming and ownership before touching deeper pack or backend code

### References

- [Source: _bmad-output/planning-artifacts/epics-M13-2026-07-12.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m13/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m13/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/RENDERER-FOUNDATION.md]
- [Source: docs/roadmap/athena-milestone-roadmap.md]

## Story Completion Status

- Status: done
- Completion note: `kernel/presentation-model` now hosts the neutral `Presentation IR` contract with KDoc-backed ownership rules, contract tests, and downstream-only naming that stays valid for future electrical, SCADA, or documentation packs.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M13 PRD refactor review
- M13 architecture-spine review
- M13 epic breakdown review
- prior M11 and M12 implementation-artifact pattern review
