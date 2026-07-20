---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 2.2: Produce Typed Cross-Reference Facts For Related Occurrences

Status: done

## Story

As an engineer reviewing a document projection,
I want repeated components, terminals, and route-related occurrences to carry typed cross-reference
facts,
so that I can follow related engineering meaning across views without reading verbose graph labels.

## Acceptance Criteria

1. The document projection model defines `CrossReferenceFact` instances carrying source identity,
   target identity, source occurrence, target occurrence, relation type, source document location,
   target document location, display notation, and provenance.
2. Supported relation types include repeated subject, terminal continuation, and route continuation
   for M26.
3. Compact display notation avoids fully qualified semantic ids on the canvas.
4. Detailed canonical identities remain available to inspector, hover, or selection consumers.
5. Tests cover same-subject references and terminal/route continuation references.

## Tasks / Subtasks

- [x] Define cross-reference fact contracts (AC: 1, 2, 3, 4)
  - [x] Add typed cross-reference identity and relation types.
  - [x] Include source/target canonical identities, occurrence ids, document locations, compact
        display notation, and provenance.
- [x] Derive same-subject cross-reference facts (AC: 1, 2, 3, 4)
  - [x] Derive repeated-subject references when a canonical subject appears in more than one sheet
        view.
  - [x] Keep derivation based on occurrence index and canonical identity only.
- [x] Derive continuation cross-reference facts (AC: 1, 2, 5)
  - [x] Derive route-continuation references from continuation facts.
  - [x] Derive terminal-continuation references when continuation terminal occurrences are present.
- [x] Add cross-reference tests (AC: 3, 4, 5)
  - [x] Verify same-subject references preserve compact notation and canonical identities.
  - [x] Verify terminal and route continuation references are typed.
  - [x] Verify no raw geometry fields are added to cross-reference contracts.

## Dev Notes

- Build on Story 2.1 continuation facts in `kernel/document-projection-model`.
- Cross references are semantic projection facts; do not infer them from renderer labels, canvas
  line breaks, DOM nodes, or geometry.
- Do not add route editing, physical routing, cabinet routing, or new `.athena` syntax.
- Do not touch deprecated frontend modules.
- Verification must run sequentially on Windows.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-7: governed cross-reference facts for repeated subjects and related occurrences
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-9: Cross References Are Typed Semantic Facts
  - AD-11: Theia Navigates Through The Occurrence Index
- Previous story: `_bmad-output/implementation-artifacts/m26/2-1-derive-continuation-facts-from-cross-view-route-membership.md`
  - Established `ContinuationFact`, optional route terminal identities, and index-derived
    continuation generation.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase because cross-reference facts, relation types, and container storage did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after adding typed cross-reference contracts and derivation.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `CrossReferenceFact`, `CrossReferenceFactId`, and M26 relation types for repeated subject,
  route continuation, and terminal continuation references.
- Derived repeated-subject references from occurrence index membership and continuation references
  from Story 2.1 continuation facts.
- Preserved compact display notation while retaining canonical identities for hover, selection, and
  inspector consumers.
- Added regression coverage for same-subject references, terminal/route continuation references, and
  geometry-free cross-reference contracts.

### File List

- `_bmad-output/implementation-artifacts/m26/2-2-produce-typed-cross-reference-facts-for-related-occurrences.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionSnapshotModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 2.2 from M26 Epic 2.
- 2026-07-20: Implemented typed cross-reference facts and derivation.
- 2026-07-20: Marked Story 2.2 done after full verification.
