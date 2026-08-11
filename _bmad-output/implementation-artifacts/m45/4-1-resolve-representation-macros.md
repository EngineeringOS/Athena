---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 4.1: Resolve Representation Macros

Status: review

## Story

As an engineer,
I want reusable Symbol, Element, and Page representation Macros,
so that repeated visual composition is fast without hidden engineering mutation.

## Acceptance Criteria

1. Macro uses relative geometry and declared child PackageItem/binding references only.
2. Insertion requires target Sheet, explicit existing Function ids, transform, and operation id.
3. Resolution allocates deterministic binding/occurrence ids from stable operation inputs and preserves them on recompile.
4. Invalid child/item, duplicate ids, missing Function target, or unsafe geometry fails closed with plain diagnostics.
5. Macro resolution never creates/changes Relationship, Part, Function, Entity, capability, or Pattern facts.

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 5)
  - [x] Add native Macro contract with relative child placements and declared binding references.
  - [x] Reject engineering mutation fields and non-relative geometry.
- [x] Task 2 (AC: 2, 3, 4)
  - [x] Add deterministic Macro admission/resolution with explicit insertion request.
  - [x] Add fail-closed diagnostics for unresolved children/functions and duplicate identities.
- [x] Task 3 (AC: 3, 4)
  - [x] Add tests and run focused package/compiler verification plus audits sequentially.

## Dev Notes

- Use current `kernel/package-model` and `kernel/package-runtime` M45 contracts. Do not revive source-less legacy `kernel/reuse-model` classes.
- Macro means representation reuse only. It cannot infer engineering solutions, select Parts, create Relationships, or act as Pattern/AI logic.
- Geometry is relative macro-local placement; persisted Sheet source remains the placement authority and compiler derives physical geometry.
- PackageItem identity/digest and FunctionRepresentationBinding identity remain stable. Renderer/Canonical Scene remains disposable output.
- No `.elmt`, HTML, XML, reference-tree, filename matching, compatibility adapters, or external package runtime paths.

### References

- [Source: _bmad-output/planning-artifacts/m45/epics.md#Story 4.1]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-11]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-12]
- [Source: AGENTS.md#Pre-1.0 Architecture Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Focused `:kernel:package-model:test :kernel:package-runtime:test` passed.
- Encoding and source-set hygiene audits passed.

### Completion Notes List

- Added native `RepresentationMacro`, relative child placement, explicit binding references, insertion request, and deterministic occurrence resolution.
- Added fail-closed admission for non-ready children and missing Function slots; no engineering facts are inferred or mutated.

### File List

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/MacroContracts.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/MacroContractsTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/RepresentationMacroAdmission.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/RepresentationMacroAdmissionTest.kt`
- `_bmad-output/implementation-artifacts/m45/4-1-resolve-representation-macros.md`

### Change Log

- 2026-08-09: Created BMad story context; implementation started.
- 2026-08-09: Implemented native representation Macro admission/resolution; focused tests and audits pass.
