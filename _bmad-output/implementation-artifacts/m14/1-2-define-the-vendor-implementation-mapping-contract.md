---
baseline_commit: 72b498f
---

# Story 1.2: Define The Vendor Implementation Mapping Contract

Status: done

## Story

As a platform engineer,  
I want Athena to model vendor parts as implementations of engineering concepts,  
so that vendor ids stop acting as the semantic type system.

## FR Traceability

- FR-2: publish component knowledge as new kernel contracts
- FR-6: model vendor parts as implementations of engineering concepts
- NFR-1: canonical authored engineering meaning remains in `Engineering IR`
- NFR-4: engineering concepts remain vendor-neutral; vendor parts remain implementations
- NFR-7: M14 output remains read-only resolved knowledge consumed by later layers
- NFR-8: M14 excludes behavior model, simulation, broad standards packs, and broad catalog parity

## Acceptance Criteria

1. Given the first M14 part-model contract is reviewed, when architecture owners inspect its responsibilities, then the contract separates engineering concept identity from vendor implementation identity.
2. Given one concept has multiple possible realizations, when vendor implementations are published, then the model can represent more than one implementation without redefining the concept itself.

## Tasks / Subtasks

- [x] Define the first vendor implementation mapping contract in a dedicated kernel module. (AC: 1, 2)
  - [x] Introduce `kernel/part-model` as the home for vendor and implementation mapping contracts.
  - [x] Use narrow names such as `VendorId`, `VendorPartNumber`, `PartImplementationId`, and `PartImplementationDefinition`.
  - [x] Keep all public/core Kotlin types documented with clean KDoc.
- [x] Freeze the ownership boundary between concepts and vendor implementations. (AC: 1)
  - [x] Make the contract comments explicit that `EngineeringConceptId` remains the vendor-neutral semantic target.
  - [x] Make the contract comments explicit that vendor ids and vendor part numbers are implementation metadata, not the type system.
  - [x] Make the contract comments explicit that resolved part mappings remain read-only and not a new mutation path.
- [x] Keep Story `1.2` narrow and foundational. (AC: 1, 2)
  - [x] Do not widen Story `1.2` into semantic ports, physical traits, deterministic pack loading, or runtime transport.
  - [x] Do not widen Story `1.2` into behavior models, simulation, or catalog breadth.
  - [x] Do not introduce renderer, projection, or presentation logic in the part-model contract.
- [x] Add focused tests and module documentation for the new contract layer. (AC: 1, 2)
  - [x] Add focused tests under the owning kernel module to prove:
    - concept identity stays separate from vendor implementation identity
    - one concept can point to more than one implementation
    - vendor part number does not replace the concept id
  - [x] Add module README files in English and Chinese.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.2` freezes the vendor implementation mapping contract above `component-model`.
- The success condition is not broad vendor coverage.
- The success condition is that one vendor-neutral concept can own many vendor implementations without collapsing concept identity into vendor ids.
- Story `1.3` should publish the first electrical concept and Siemens implementation slice over the new contracts.

### Architecture Guardrails

- Align to AD-75: M14 introduces component knowledge resolution between `Engineering IR` and downstream consumers.
- Align to AD-77: engineering concepts are vendor-neutral; vendor parts are implementations.
- Align to AD-80: M14 outputs feed M9 and M13 and do not create a new mutation path.
- Align to AD-82: DSL remains canonical serialization, not the default human interface.
- Preserve inherited AD-13, AD-16, AD-34, AD-43, and AD-67.

### Implementation Notes

- `kernel/part-model` depends on `kernel/component-model` so concept identity remains the semantic target.
- `PartImplementationId` is stable Athena-owned implementation identity.
- `VendorPartNumber` is vendor-facing catalog identity and must not be treated as the semantic type.
- `ResolvedPartImplementation` stays read-only and keeps canonical authored subject identity separate from implementation metadata.

## Story Completion Status

- Status: done
- Completion note: implemented `:kernel:part-model`, added focused contract tests, and verified with `java25; .\gradlew.bat :kernel:part-model:test --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 Story `1.2` module and contract implementation
- Sequential Java 25 verification
