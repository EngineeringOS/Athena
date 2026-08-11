# Story 2.2: Derive And Validate Grid-Aligned Spatial Reality

Status: review

## Story

As a reviewer,
I want deterministic, bounded, grid-aligned Spatial Reality,
so that a live page never shows guessed or contradictory geometry.

## Acceptance Criteria

1. A compiler derives occurrence geometry from Projection Reality plus optional Sheet placement
   constraints in deterministic sheet and occurrence order.
2. Each derived origin uses the Sheet `cell: K` contract: one square macro cell spans `4K` logical
   units and each implicit micro step is `K`; no independent horizontal or vertical scale exists.
3. Authored anchors are applied to the matching Sheet occurrence without changing semantic or
   Projection identity. Unanchored occurrences receive deterministic initial placement.
4. Anchor cells, derived rectangles, and blocking occurrence overlaps outside the Sheet drawing area
   produce plain engineering diagnostics and no partial Spatial Reality.
5. Exact geometry remains Spatial-owned; routes, labels, and paint fields are not authored or emitted
   by this compiler.
6. Repeated identical inputs and reordered input lists produce equal occurrences and diagnostics.
7. Focused compiler tests and the affected Gradle module test pass sequentially.

## Tasks / Subtasks

- [x] Task 1: Extend the placement constraint contract with owning Sheet identity (AC: 1, 3).
  - [x] Preserve stable occurrence identity, cell, micro, lock, and source provenance.
  - [x] Update mapper tests without introducing a second projection document.
- [x] Task 2: Implement deterministic grid-aligned Spatial compiler (AC: 1-5).
  - [x] Reuse `ProjectionSpatialLayout` for deterministic baseline ordering.
  - [x] Apply authored anchors using `cell * 4` macro span and `micro * K` offset.
  - [x] Snap unanchored origins to the same Sheet micro lattice.
  - [x] Reject missing Sheet grid, invalid cell bounds, rectangle bounds, duplicate anchors, and
    blocking overlaps with stable diagnostics.
- [x] Task 3: Add focused tests for grid math, identity, fail-closed validation, and determinism
  (AC: 2, 4, 6).
- [x] Task 4: Run affected Gradle tests sequentially (AC: 7).

## Dev Notes

- `project.athena` remains Engineering Reality authority. `*.sheet.athena` contributes only
  presentation intent; this story must not add layout fields to project grammar.
- `SheetPlacementConstraint` belongs in `kernel/layout-model`; compiler behavior belongs in
  `kernel/compiler`; spatial facts use existing `SpatialOccurrenceGeometry`, `SpatialRect`,
  `SpatialDiagnostic`, `SpatialPlacementReason`, and `SpatialSourceTrace`.
- `ProjectionSpatialLayout` already owns deterministic region/topology ordering and baseline sizes
  (`NODE_WIDTH`, `NODE_HEIGHT`). Compose it; do not duplicate placement grouping logic.
- Cell coordinates are one-based `DrawingGridPosition`. A cell with `cell: K` has macro span `4K`
  and micro positions `1..4`; derived origin is drawing-area origin plus zero-based macro offset
  plus zero-based micro offset. Exact pixels are compiler output, never source syntax.
- Fail closed: any diagnostic returns an empty occurrence list. Do not clamp, guess, or preserve
  partial geometry. Diagnostics name the Sheet and occurrence and include source trace identities.
- No Pattern/Macro system, renderer DTO, route solver, label solver, PDF/export, compatibility
  adapter, or legacy fallback belongs here.

### Project Structure Notes

- Layout contract: `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- Sheet mapper: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SheetCompanionProjectionPlacementMapper.kt`
- Existing deterministic baseline: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt`
- New grid compiler and tests stay in `kernel/compiler`.
- Keep small related value types together; do not create one-file-per-field DTOs.

### Previous Story Intelligence

- Story 2-1 established `SheetPlacementConstraint` and deterministic mapping from parsed Sheet
  placements to Projection occurrence IDs. Its compiler test uses the current `ProjectionDocument`
  constructor (`view`, `nodes`, `connections`, optional `sheets`) and passed
  `:kernel:compiler:test`.
- Preserve `LayoutSourceSpan` conversion and stable sorting by occurrence identity.

### References

- [Source: _bmad-output/planning-artifacts/m43/epics.md#Epic 2 - Deterministic Spatial Compilation]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md#FR-6]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#AD-2---layout-cell-uses-one-four-divisible-scalar]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#AD-4---persist-constraints-derive-geometry]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayout.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `:kernel:compiler:test` passed; focused grid, identity, overlap, bounds, and determinism tests pass.

### Completion Notes List

- Added owning Sheet identity to placement constraints.
- Added deterministic grid-aligned compiler composing `ProjectionSpatialLayout`.
- Applied `cell: K` macro/micro math, lattice snapping, bounds and overlap diagnostics, fail-closed output.

### File List

- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SheetCompanionProjectionPlacementMapper.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GridAlignedSpatialLayoutCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/GridAlignedSpatialLayoutCompilerTest.kt`

### Change Log

- 2026-08-05: Implemented and verified grid-aligned Spatial Reality compilation.
