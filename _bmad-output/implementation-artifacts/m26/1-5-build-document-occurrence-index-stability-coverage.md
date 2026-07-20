---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 1.5: Build Document Occurrence Index Stability Coverage

Status: done

## Story

As an Athena IDE integrator,
I want a deterministic document occurrence index for canonical subjects,
so that inspectors, reveal actions, and cross-reference navigation can use governed identity.

## Acceptance Criteria

1. Each indexed occurrence includes document projection identity, sheet-view identity, canonical
   subject identity, occurrence role, representation/terminal/route role where applicable, logical
   zone, display location, and source range where available.
2. Occurrence ids follow the M26 identity recipe using policy-versioned projection identity.
3. The index supports one canonical subject appearing in multiple sheet views.
4. Renaming or reordering source files without changing canonical semantic identities does not
   change occurrence ids.
5. Tests cover component, terminal, route, label, repeated-subject, and source-rename stability
   cases.

## Tasks / Subtasks

- [x] Add occurrence index lookup coverage (AC: 1, 3)
  - [x] Verify subject lookup returns multiple sheet-view occurrences deterministically.
  - [x] Add a sheet-view lookup helper if needed by future Theia navigation.
- [x] Add full role coverage (AC: 1, 2, 5)
  - [x] Cover component, terminal, route, and label occurrence roles in a projected snapshot.
  - [x] Verify source provenance and source ranges survive into occurrence entries where supplied.
- [x] Add rename/reorder stability coverage (AC: 2, 4, 5)
  - [x] Verify source file rename and source unit reorder do not change occurrence ids when
        semantic graph id and canonical subject identities are unchanged.

## Dev Notes

- Story 1.5 should mostly harden and prove the Story 1.4 occurrence index; avoid adding cross
  reference or continuation derivation.
- Keep source paths as provenance/proof metadata only. They must not enter occurrence identity.
- Do not introduce new `.athena` syntax or touch parser/frontend syntax paths.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend scope.
- Verification must run sequentially on Windows.

### Project Structure Notes

- Expected update files:
  - `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
  - `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-5: document occurrence index
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-7: Occurrence Identity Is Deterministic And Policy-Versioned
  - AD-11: Theia Navigates Through The Occurrence Index
- Epics: `_bmad-output/implementation-artifacts/m26/epics.md`
  - Epic 1, Story 1.5

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase because `DocumentOccurrenceIndex.forSheetView` did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after adding sheet-view lookup coverage.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `DocumentOccurrenceIndex.forSheetView()` for future Theia navigation through governed
  occurrence identity.
- Added tests covering component, terminal, route, and label occurrences in a projected snapshot.
- Added tests proving repeated subject lookup, source range preservation, and occurrence id stability
  under source file rename/reorder.

### File List

- `_bmad-output/implementation-artifacts/m26/1-5-build-document-occurrence-index-stability-coverage.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 1.5 from M26 Epic 1.
- 2026-07-20: Implemented occurrence index stability and lookup coverage.
- 2026-07-20: Marked Story 1.5 done after full verification.
