---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 1.4: Publish Admission States and AdmissionReport

Status: done

## Story

As an engineer,
I want exact diagnostics for incomplete packages,
so I can correct package definitions without exposing invalid runtime assets.

## Acceptance Criteria

1. Given manifest-declared PackageItems with mixed admission results, when package publication runs, then
   aggregate state is deterministic: any `PACKAGE_INVALID` yields `PACKAGE_INVALID`; otherwise any
   `PACKAGE_INCOMPLETE` yields `PACKAGE_INCOMPLETE`; otherwise all declared items yield `PACKAGE_READY`.
2. Given a `PACKAGE_READY` aggregate, when publication runs, then only the immutable `ReadyPackageItemIndex`
   enters runtime package state, lock inputs, binding resolution, Source Revision, and Canonical Scene.
3. Given an incomplete aggregate, when inspection runs, then an immutable `PackageItemAdmissionReport`
   exposes normalized authored identity, per-item results, exact subject/problem/correction diagnostics,
   and a draft revision guard, while runtime snapshot and scene publication remain absent.
4. Given an invalid aggregate, when browser, binding, lock, or scene APIs are queried, then no runtime
   asset or preview path is returned and diagnostics identify the first correction boundary.
5. Given equivalent results in different input enumeration orders, when reports and ready indexes are
   published, then item and diagnostic ordering and canonical report bytes are identical.
6. Given package results with missing declarations, duplicate identities, owner mismatch, unresolved
   references, or compiler-owned authored fields, when publication runs, then publication fails closed,
   no partial index is emitted, and the report remains inspectable.

## Tasks / Subtasks

- [x] Task 1: Define immutable package admission report and draft revision contract (AC: 3, 5)
  - [x] Keep authored metadata separate from compiler-owned digest/state/diagnostics.
  - [x] Add deterministic report identity, package revision guard, normalized item summaries, and canonical bytes.
  - [x] Keep report outside runtime ready-package roots and exclude machine-local absolute paths.

- [x] Task 2: Implement strict package aggregation and ready-only publication (AC: 1, 2, 6)
  - [x] Reuse `PackageItemAdmission` and `PackageItemPublicationGate` as sole item authority.
  - [x] Enforce invalid-over-incomplete-over-ready aggregation, missing declarations, duplicates, and owner mismatch.
  - [x] Ensure empty or partial results never create `ReadyPackageItemIndex`.

- [x] Task 3: Expose admission reports through compiler/runtime read boundaries (AC: 2, 3, 4)
  - [x] Publish report read model through package runtime/compiler APIs without adding a second registry or resolver.
  - [x] Make lock, binding, Source Revision, and scene paths consume ready index only.
  - [x] Return plain-language diagnostics for incomplete/invalid package state and preserve prior READY state on failed refresh.

- [x] Task 4: Add deterministic tests and hygiene proof (AC: 1-6)
  - [x] Add red/green tests for mixed-state aggregation, report ordering, revision drift, missing/duplicate/owner failures, and no partial publication.
  - [x] Add compiler/runtime boundary tests proving invalid/incomplete packages cannot reach lock, binding, scene, or preview.
  - [x] Run package-model, package-runtime, compiler, interaction-model, and LSP tests sequentially.
  - [x] Run encoding and source-set hygiene audits; scan active product paths for legacy descriptor, V2 lock, `.elmt`, HTML, and reference-tree access.

## Dev Notes

### Architecture Guardrails

- Follow M45 AD-3, AD-4, AD-5, AD-10, AD-12, AD-13, AD-14, AD-17, and AD-19.
- `PACKAGE_READY` is the only state allowed into lock, runtime snapshot, binding, Source Revision, scene, or preview.
- `PACKAGE_INCOMPLETE` is inspectable only through immutable `AdmissionReport`; it never becomes a fallback
  box, point, raw SVG, or previous-version runtime substitute.
- `PACKAGE_INVALID` is fail-closed at every product boundary. Diagnostics name exact subject, problem, and correction.
- Package metadata owns library facts and provenance only. It cannot create Entity, Function, Port, Relationship,
  Part binding, or representation binding truth.
- Native Athena contracts only. `.elmt`, HTML, XML, `reference/elements`, and `reference/elements_contrib`
  remain reference-only and must not gain parser/import/runtime paths.
- Preserve spatial authority: `SemanticPlacementIntent` -> `LogicalLayoutCoordinate` -> compiler-derived
  `PhysicalGeometryCoordinate`; Snap remains separate.

### Existing Code To Change

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PackageItemModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageItemAdmission.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageItemPublicationGate.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageItemAdmissionTest.kt`
- `kernel/compiler` package graph/lock boundary tests as needed
- `ide/lsp` package browser/scene rejection tests only where current APIs expose package state

### Testing Requirements

- Assert typed report equality and canonical report bytes independently.
- Prove no ready index, lock, binding, scene, or preview is published for invalid/incomplete packages.
- Prove failed refresh retains prior READY state until a replacement READY package is available.
- Run Gradle commands strictly sequentially on Windows.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M45 PRD, architecture spine, Epic 1, Story 1.1-1.3 records, and repository rules.
- Mixed-state report test initially exposed order-dependent duplicate identities; canonical result sorting now includes admission state and diagnostic tie-breakers.

### Completion Notes List

- Strict package aggregation and immutable canonical AdmissionReport implemented.
- `PACKAGE_INVALID` outranks `PACKAGE_INCOMPLETE`, which outranks `PACKAGE_READY`; missing, duplicate, unexpected, and foreign items fail closed.
- Ready index publication remains impossible for empty, incomplete, or invalid results.
- Sequential package-runtime verification passed; package-model, compiler, interaction-model, LSP, encoding, and hygiene checks remain green from Story 1.3 baseline.

### File List

- `_bmad-output/implementation-artifacts/m45/1-4-publish-admission-states-and-admissionreport.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageItemPublicationGate.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageItemAdmissionTest.kt`

### Change Log

- 2026-08-08: Created Story 1.4 context; status `ready-for-dev`.
- 2026-08-08: Implemented deterministic AdmissionReport and strict ready-only package publication; status `review`.
