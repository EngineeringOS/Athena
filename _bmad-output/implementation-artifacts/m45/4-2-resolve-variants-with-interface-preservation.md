---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 4.2: Resolve Variants with Interface Preservation

Status: review

## Story

As an engineer,
I want named representation variants,
so that visual alternatives remain safe and semantic identity stays unchanged.

## Acceptance Criteria

1. Variant resolution is deterministic from Element identity, Variant identity, and admitted payload.
2. Every Variant of an Element has the same normalized Function-slot/public-port interface fingerprint.
3. Interface changes fail closed and require a new Element item/version; semantic Function/Entity/Relationship identity remains unchanged.

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 2)
  - [x] Add normalized interface fingerprint and Variant contract.
  - [x] Add deterministic variant resolver over admitted items.
- [x] Task 2 (AC: 2, 3)
  - [x] Reject interface mismatch with exact correction diagnostic.
  - [x] Prove Element identity and semantic binding identity are preserved.
- [x] Task 3 (AC: 1, 3)
  - [x] Add tests and run focused package-model/runtime verification plus audits sequentially.

## Dev Notes

- Variant is representation alternative only. It cannot create Function, Entity, Relationship, Part, capability, Pattern, or engineering rules.
- Reuse existing `ElementComposition`, `FunctionSlot`, `ElementPort`, `PackageItemIdentity`, and `AdmittedPackageItem`.
- Interface fingerprint includes normalized Function-slot ids/roles and public port keys/slot/child-anchor compatibility; order must not affect digest.
- Interface change requires a new Element item/version. Do not add compatibility adapters or dual paths.

### References

- [Source: _bmad-output/planning-artifacts/m45/epics.md#Story 4.2]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-11]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-19]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Focused `:kernel:package-model:test :kernel:package-runtime:test` passed.
- Interface fingerprint ordering and mismatch rejection covered.

### Completion Notes List

- Added normalized Function-slot/public-port fingerprint and `RepresentationVariant` contract.
- Added deterministic resolver preserving Element identity and rejecting interface changes.

### File List

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/VariantContracts.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/VariantContractsTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/VariantResolution.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/VariantResolutionTest.kt`
- `_bmad-output/implementation-artifacts/m45/4-2-resolve-variants-with-interface-preservation.md`

### Change Log

- 2026-08-09: Created BMad story context; implementation started.
- 2026-08-09: Implemented variant interface preservation and deterministic resolution; focused tests pass.
