---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 2.1: Derive Continuation Facts From Cross-View Route Membership

Status: done

## Story

As an electrical engineer,
I want a route that crosses sheet-view membership to produce governed continuation facts,
so that cross-view conductors remain semantic route facts instead of broken rendered lines.

## Acceptance Criteria

1. The document projection model defines `ContinuationFact` instances linking route identity, source
   terminal, target terminal, source document location, and target document location.
2. Each continuation fact includes source and target route occurrence identities.
3. Continuation markers attach to route/presentation anchors later; Story 2.1 must keep the semantic
   continuation fact upstream and not infer it from rendered line breaks or canvas geometry.
4. The visible notation is compact by default, such as target view plus zone.
5. Tests verify continuation meaning is not inferred from rendered line breaks or canvas geometry.

## Tasks / Subtasks

- [x] Define continuation fact contracts (AC: 1, 2, 4)
  - [x] Add continuation identity, route identity, source/target occurrence ids, source/target
        document locations, optional terminal identities, display notation, and provenance.
- [x] Derive continuation facts from route membership (AC: 1, 2, 3)
  - [x] Derive continuations when a canonical route subject appears in more than one sheet view.
  - [x] Keep derivation based on occurrence index and canonical subject identity only.
- [x] Add continuation tests (AC: 3, 4, 5)
  - [x] Verify a cross-view route produces a continuation fact.
  - [x] Verify a single-view route produces no continuation fact.
  - [x] Verify no raw geometry fields are added to continuation contracts.

## Dev Notes

- Do not add renderer line-break inference. Theia must later paint facts only.
- Do not add route editing, physical routing, cabinet routing, or new `.athena` syntax.
- Keep this in `kernel/document-projection-model`.
- Do not touch deprecated frontend modules.
- Verification must run sequentially on Windows.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-6: governed continuation facts for cross-view routes
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-8: Continuations Are Route-Segmentation Facts
  - AD-9: Cross References Are Typed Semantic Facts

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase because continuation fact contracts, terminal route metadata, and continuation fact storage did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after adding continuation contracts and index-derived continuation generation.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added upstream `ContinuationFact` and `ContinuationFactId` contracts without raw geometry authority.
- Extended route subject summaries with optional source/target terminal identities.
- Derived continuation facts from canonical route occurrences across sheet views through the document occurrence index.
- Added regression coverage for cross-view route continuations, single-view non-continuations, and geometry-free contracts.

### File List

- `_bmad-output/implementation-artifacts/m26/2-1-derive-continuation-facts-from-cross-view-route-membership.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionSnapshotModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 2.1 from M26 Epic 2.
- 2026-07-20: Implemented cross-view route continuation facts.
- 2026-07-20: Marked Story 2.1 done after full verification.
