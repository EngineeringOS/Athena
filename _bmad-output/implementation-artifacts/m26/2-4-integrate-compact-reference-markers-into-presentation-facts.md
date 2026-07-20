---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 2.4: Integrate Compact Reference Markers Into Presentation Facts

Status: done

## Story

As an engineer reading the sheet view,
I want continuation and cross-reference markers to render compactly while preserving inspectable
meaning,
so that the canvas remains readable and does not repeat long semantic ids on every line.

## Acceptance Criteria

1. Presentation IR can carry compact marker facts for visible continuation and cross-reference
   notation for a selected sheet view.
2. Marker facts include canonical reference payload for hover, selection, and inspector use.
3. Raw labels such as `ControllerPLC3.hmi -> OperatorHMI3.status` are not used as default visible
   route titles in the M26 path.
4. Marker rendering stays inside Presentation IR and paint-only Theia rendering boundaries.
5. Visual/projection tests verify marker compactness and metadata availability.

## Tasks / Subtasks

- [x] Define presentation reference marker facts (AC: 1, 2, 4)
  - [x] Add marker identity, marker kind, relation type, selected sheet view, source/target
        occurrence ids, canonical identities, compact notation, and provenance payload.
  - [x] Attach markers to Presentation IR without making Theia infer reference meaning.
- [x] Project document references into selected sheet-view marker facts (AC: 1, 2, 3)
  - [x] Produce marker facts for cross references whose source occurrence is in the selected sheet
        view.
  - [x] Keep compact notation separate from canonical identity payload.
- [x] Add presentation marker tests (AC: 2, 3, 4, 5)
  - [x] Verify compact marker notation does not expose fully qualified semantic ids.
  - [x] Verify marker facts preserve relation type and canonical source/target payload.
  - [x] Verify a `PresentationDocument` carries marker facts as IR, not renderer-owned state.

## Dev Notes

- Keep this in `kernel/presentation-model`; Theia consumes marker facts later.
- It is acceptable for `presentation-model` to depend on `document-projection-model` because
  document projection is upstream of Presentation IR and does not depend on presentation.
- Do not touch Theia renderer or deprecated desktop modules in this story.
- Do not add new `.athena` syntax.
- Verification must run sequentially on Windows.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-6 and FR-7 markers
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-3: Document Projection IR owns topology, not geometry
  - AD-4: Presentation IR owns paint-ready sheet presentation
- Previous stories:
  - `_bmad-output/implementation-artifacts/m26/2-1-derive-continuation-facts-from-cross-view-route-membership.md`
  - `_bmad-output/implementation-artifacts/m26/2-2-produce-typed-cross-reference-facts-for-related-occurrences.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test` failed in red phase because document projection marker APIs and module dependency did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test` exposed a missing test assertion import; fixed and reran successfully.
- `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `PresentationReferenceMarkerFact` and marker kinds for continuation and cross-reference
  notation in Presentation IR.
- Added `documentReferenceMarkersForSheetView()` to project selected-view document cross references
  into compact marker facts.
- Added marker payload fields for relation type, source/target occurrence ids, canonical identities,
  document locations, compact notation, and source projection ids.
- Extended `PresentationDocument` with marker facts so Theia can paint/inspect facts without
  inferring semantic reference meaning.

### File List

- `_bmad-output/implementation-artifacts/m26/2-4-integrate-compact-reference-markers-into-presentation-facts.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/presentation-model/build.gradle.kts`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 2.4 from M26 Epic 2.
- 2026-07-20: Implemented compact document reference marker facts in Presentation IR.
- 2026-07-20: Marked Story 2.4 done after full verification.
