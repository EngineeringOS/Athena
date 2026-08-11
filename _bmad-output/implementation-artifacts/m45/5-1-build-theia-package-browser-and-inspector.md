---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 5.1: Build Theia Package Browser and Inspector

Status: done

## Story

As an engineer,
I want to browse and inspect admitted package items,
so that I choose assets by engineering metadata rather than filenames.

## Acceptance Criteria

1. Typed read model exposes only READY package snapshot items plus AdmissionReport diagnostics.
2. Item inspection includes kind, identity/version, center/ports/compatibility, compatible Parts, variants, provenance, license, and digest.
3. Preview is read-only and cannot mutate source, bindings, or scene.
4. Incomplete/invalid items remain inspectable as diagnostics but never appear as runtime insertable assets.

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 2, 4)
  - [x] Add typed package browser/inspector read contracts over existing admitted index/report.
  - [x] Ensure READY-only item list and diagnostic projection.
- [x] Task 2 (AC: 2, 3)
  - [x] Add read-only preview payload with provenance and compatibility metadata.
  - [x] Reject mutation through browser read model.
- [x] Task 3 (AC: 1, 4)
  - [x] Add tests and run focused package/runtime verification plus audits sequentially.

## Dev Notes

- Browser is Theia typed client/read projection. It must not parse package files, assemble metadata, or mutate source/scene.
- Reuse `ReadyPackageItemIndex`, `PackageItemAdmissionReport`, `AdmittedPackageItem`, `PackageProvenanceView`, and existing typed LSP payload conventions.
- No `.elmt`, HTML, XML, reference-tree, filename matching, second registry, or renderer-owned package state.

### References

- [Source: _bmad-output/planning-artifacts/m45/epics.md#Story 5.1]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-14]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Focused `:kernel:package-runtime:test` passed.
- Encoding and source-set hygiene audits passed.

### Completion Notes List

- Added typed `PackageBrowserSnapshot`/`PackageBrowserItem` read model over READY index and immutable AdmissionReport.
- Inspector requires stable digest/provenance identity and fails closed when READY details are missing.

### File List

- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBrowserReadModel.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageBrowserReadModelTest.kt`
- `_bmad-output/implementation-artifacts/m45/5-1-build-theia-package-browser-and-inspector.md`

### Change Log

- 2026-08-09: Created BMad story context; implementation started.
- 2026-08-09: Implemented package browser/inspector read model; focused tests pass.
