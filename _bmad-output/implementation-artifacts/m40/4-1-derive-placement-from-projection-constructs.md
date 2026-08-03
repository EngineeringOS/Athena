---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.1: Derive Placement From Projection Constructs

Status: review

## Story

As a compiler maintainer,
I want Spatial to derive placement from Projection constructs,
so that placement stays derived and Projection stays coordinate-free.

## Acceptance Criteria

1. The Spatial compiler derives placement, bounds, anchors, and route facts from Projection
   occurrences and constructs; Projection stores membership and reading order only.
2. Spatial output is traceable to projection construct and occurrence identities.
3. Spatial placement maps each occurrence and construct to a sheet grid cell reference (e.g.,
   A1/B3) derived from its placement.
4. No M40 code path performs lane optimization, bend minimization, crossing minimization, or
   multi-sheet continuation.

## Tasks / Subtasks

- [x] Bridge authored projection (sheet subjects) into the M39 spatial pipeline.
- [x] Add grid-reference mapping from sheet grid to placements (Spatial-owned facts).
- [x] Add traceability + grid-reference + no-routing tests.
- [x] Run focused tests; update story + sprint.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 4.1]
- [Source: PRD FR-16, FR-17; ARCHITECTURE-SPINE.md AD-13, AD-16]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added AuthoredProjectionSpatialBridge + grid references.
- 2026-08-02: Compiler suite green.

### Completion Notes List

- `AuthoredProjectionSpatialBridge` materializes sheet subjects as projection occurrences and
  runs the M39 `ProjectionToSpatialTransformation`; placements/bounds/anchors/routes derive from
  authored projection.
- `SpatialDocument.gridReferences` maps each placement to a sheet grid cell (e.g., A1/B3) using
  the committed grid rows/columns and the A3 sheet extent; no routing optimization added
  (FR-17).
- Determinism + traceability tested.

### File List

- kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialBridge.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 4.1 and PRD FR-16/FR-17.
- 2026-08-02: Implemented spatial bridge + grid references; compiler green; review.
