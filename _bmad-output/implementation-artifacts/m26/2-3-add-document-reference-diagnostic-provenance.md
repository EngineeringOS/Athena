---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 2.3: Add Document Reference Diagnostic Provenance

Status: done

## Story

As an Athena maintainer,
I want unresolved or ambiguous document references to report source or projection provenance,
so that Problems only shows authored source issues and derived projection issues stay explainable.

## Acceptance Criteria

1. Document reference diagnostics include severity, diagnostic code, relation type, affected
   canonical identity, and provenance.
2. Diagnostics caused by authored `.athena` input carry source range and can publish to Problems.
3. Diagnostics caused only by projection policy or derived view behavior carry projection/view
   provenance and remain in inspector or proof metadata.
4. Theia does not create semantic diagnostics by resolving cross references locally.
5. Tests cover source-backed and projection-only diagnostic routing.

## Tasks / Subtasks

- [x] Define document reference diagnostic contracts (AC: 1, 2, 3)
  - [x] Add diagnostic severity, code, provenance kind, affected identity, relation type, message,
        and Problems-routing flag.
  - [x] Keep source provenance separate from projection/view provenance.
- [x] Emit diagnostics during reference derivation (AC: 1, 2, 3)
  - [x] Emit source-backed diagnostics for authored route continuation terminal references that
        cannot resolve to document occurrences.
  - [x] Emit projection-only diagnostics for derived ambiguous terminal continuation targets.
- [x] Add diagnostic tests (AC: 2, 3, 4, 5)
  - [x] Verify source-backed diagnostic carries source range and can publish to Problems.
  - [x] Verify projection-only diagnostic does not publish to Problems.
  - [x] Verify diagnostics live in the projection snapshot, not Theia renderer inference.

## Dev Notes

- Keep diagnostics in `kernel/document-projection-model`; Theia will consume them later.
- Do not add Theia-side semantic diagnosis or canvas scanning.
- Do not add new `.athena` syntax.
- Do not touch deprecated frontend modules.
- Verification must run sequentially on Windows.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-8: document-reference diagnostics with source/projection provenance
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-10: Diagnostics Preserve Source And Projection Provenance
- Previous stories:
  - `_bmad-output/implementation-artifacts/m26/2-1-derive-continuation-facts-from-cross-view-route-membership.md`
  - `_bmad-output/implementation-artifacts/m26/2-2-produce-typed-cross-reference-facts-for-related-occurrences.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase because diagnostic contracts and typed snapshot diagnostics did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after adding diagnostic contracts and reference derivation diagnostics.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added typed document projection diagnostics with severity, code, relation type, affected identity,
  provenance, and Problems-routing control.
- Emitted source-backed missing terminal continuation diagnostics when authored route provenance is
  available.
- Emitted projection-only ambiguous terminal continuation diagnostics for derived repeated terminal
  occurrences.
- Kept diagnostic ownership upstream in the document projection snapshot.

### File List

- `_bmad-output/implementation-artifacts/m26/2-3-add-document-reference-diagnostic-provenance.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionSnapshotModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 2.3 from M26 Epic 2.
- 2026-07-20: Implemented document reference diagnostic provenance.
- 2026-07-20: Marked Story 2.3 done after full verification.
