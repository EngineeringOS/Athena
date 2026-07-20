---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 1.4: Materialize Deterministic Sheet Views And Occurrence Membership

Status: done

## Story

As an Athena reviewer,
I want deterministic sheet views and occurrence membership from semantic projection facts,
so that the same engineering model always produces the same document projection without canvas
state.

## Acceptance Criteria

1. `athena-document-projection-v0` produces stable sheet-view ids, display order, display titles,
   view roles, logical zones, and occurrence membership for the accepted M26 roles.
2. Layout facts may influence placement inside a selected sheet view but do not define document
   projection identity or occurrence membership.
3. No hand-authored canvas page state is required.
4. No `.athena` source file boundary is treated as a sheet-view boundary.
5. Tests verify stable output for repeated materialization of the same semantic graph.

## Tasks / Subtasks

- [x] Add semantic subject membership input contracts (AC: 1, 4)
  - [x] Represent canonical subject identity, occurrence/detail role, preferred sheet-view roles,
        and optional source provenance.
  - [x] Keep source unit/file data as provenance only.
- [x] Materialize occurrence membership deterministically (AC: 1, 2, 3, 4)
  - [x] Produce sheet views with stable ids/titles/roles/order and logical zones.
  - [x] Produce document occurrences and occurrence index entries for each subject membership.
  - [x] Publish occurrence ids grouped by sheet view without using source filenames.
- [x] Add stability and boundary tests (AC: 1, 2, 3, 4, 5)
  - [x] Verify repeated materialization of the same semantic graph is stable.
  - [x] Verify source unit order and filenames do not change sheet-view ids or membership.
  - [x] Verify one source unit can contribute to more than one sheet view.
  - [x] Verify one sheet view can contain subjects from multiple source units.

## Dev Notes

- Story 1.4 extends the Story 1.3 entry point; it does not add continuation or cross-reference
  derivation. Epic 2 owns those facts.
- Document Projection IR owns topology and occurrence membership, not geometry.
- Keep `.athena` source as the single source of truth. This story adds derived projection contracts
  only.
- No new `.athena` syntax, grammar, Tree-sitter, compiler syntax, LSP syntax, or sample syntax docs.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend scope.
- Verification must run sequentially on Windows.

### Project Structure Notes

- Expected update files:
  - `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionSnapshotModel.kt`
  - `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-4: deterministic sheet-view materialization
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-3: Document Projection IR Owns Topology, Not Geometry
  - AD-6: Source Files Are Not Sheet Views
  - AD-7: Occurrence Identity Is Deterministic And Policy-Versioned
- Epics: `_bmad-output/implementation-artifacts/m26/epics.md`
  - Epic 1, Story 1.4

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase because subject membership and sheet-view occurrence grouping contracts did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after implementing deterministic occurrence membership.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `DocumentProjectionSubjectSummary` with canonical subject identity, occurrence/detail role,
  target sheet-view roles, and optional source provenance.
- Extended `DocumentProjectionEntryPoint.projectWorkspace()` to materialize sheet views with logical
  zones, document occurrences, occurrence index entries, and occurrence ids grouped by sheet view.
- Verified source unit order and filenames do not change sheet-view ids or occurrence membership.
- Kept continuation and cross-reference fact derivation deferred to Epic 2.

### File List

- `_bmad-output/implementation-artifacts/m26/1-4-materialize-deterministic-sheet-views-and-occurrence-membership.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionSnapshotModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 1.4 from M26 Epic 1.
- 2026-07-20: Implemented deterministic sheet-view occurrence membership.
- 2026-07-20: Marked Story 1.4 done after full verification.
