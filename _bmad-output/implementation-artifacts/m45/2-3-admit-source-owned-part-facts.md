---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 2.3: Admit Source-Owned Part Facts

Status: review

## Story

As an engineer,
I want physical and procurement Parts,
so implementation selection is validated without owning geometry or solution intent.

## Acceptance Criteria

1. Part metadata admits manufacturer/article identity, typed technical values, function templates,
   capability compatibility, accessories, provenance, and license.
2. Part never owns Symbol/Element geometry, creates Relationships, or infers Patterns/solutions.
3. Missing identity, malformed typed value, incompatible capability, unsafe provenance, or duplicate
   accessory identity fails closed.
4. Canonical Part payload is deterministic across map/order enumeration.

## Tasks / Subtasks

- [x] Task 1: Add native Part facts and typed technical value contracts.
- [x] Task 2: Add Part admission validation and compatibility diagnostics.
- [x] Task 3: Add deterministic tests and run package model/runtime audits.

## Dev Notes

- Follow AD-3, AD-4, AD-7, AD-18, AD-19.
- Part is Engineering implementation fact, source-owned through later FunctionPartBinding.
- No geometry authority, Element mutation, Relationship creation, Pattern inference, `.elmt`, HTML, or reference-tree runtime.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M45 Epic 2 and current PackageItem/Element contracts.

### Completion Notes List

- Added native `PartFacts`, typed technical fields, accessories, provenance, license, and
  capability compatibility contracts.
- Added fail-closed Part admission for missing identity, malformed values, incompatible
  capability, unsafe provenance, and duplicate accessory identity.
- Canonical Part payload ordering is deterministic.
- Verified with `:kernel:package-model:test` and `:kernel:package-runtime:test`.

### File List

- `_bmad-output/implementation-artifacts/m45/2-3-admit-source-owned-part-facts.md`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PartContracts.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/PartContractsTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PartFactsAdmission.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PartFactsAdmissionTest.kt`

### Change Log

- 2026-08-08: Created Story 2.3 context; status `in-progress`.
- 2026-08-09: Implemented Part facts/admission, deterministic tests, and moved story to `review`.
