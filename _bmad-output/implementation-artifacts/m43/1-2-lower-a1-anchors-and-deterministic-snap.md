---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 1.2: Lower A1 Anchors And Deterministic Snap

Status: done

## Story

As an engineer, I can place an occurrence at A1-style coordinates and N x N micro anchors,
so drag preview and committed placement agree exactly.

## Acceptance Criteria

1. Row letters, numeric columns, macro origin, default micro, boundary ownership, and placement-anchor formulas match shared `cell: 4` and `cell: 8` vectors.
2. Micro outside `1..N`, unknown occurrence, out-of-grid address, and locked drag fail closed.
3. Kotlin lowering and TypeScript snap helpers use the same deterministic vectors and tie-break rules.
4. Authored lock and placement provenance remain stable across source reorder.
5. Focused compiler/language/frontend tests, full Gradle, encoding, and hygiene audits pass.

## Tasks / Subtasks

- [x] Correct typed lowering formulas (AC: 1, 2, 4)
  - [x] Treat `cell: N` as N subdivisions per macro axis.
  - [x] Lower omitted micro to deterministic center anchor; explicit micro uses one-based boundary anchors.
  - [x] Validate micro against cell count and preserve lock/source span.
- [x] Align deterministic snap vectors (AC: 1, 3)
  - [x] Add shared cell-4/cell-8 lowering tests.
  - [x] Add TypeScript helper/vector parity tests without renderer ownership.
- [x] Verify failure and provenance behavior (AC: 2, 4)
  - [x] Cover unknown occurrence, bounds, locked intent, source reorder, and stable identity.
- [x] Verify and record (AC: 5)
  - [x] Run focused tests, frontend tests, full Gradle, encoding, hygiene.
  - [x] Complete story records and set `review` only after evidence passes.

## Dev Notes

- Use `contracts/presentation/v1/grid/cell-4-vectors.json` and `cell-8-vectors.json` as sole vector authority.
- Formula: macro origin `(column-1)*N,(row-1)*N`; omitted micro `(N/2,N/2)`; explicit micro `(x-1,y-1)`; all logical coordinates remain integer.
- Do not add renderer pixels, Canvas, CSS, or compatibility paths to kernel lowering.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- TypeScript vector test caught A1 address interpretation reversed; corrected numeric column/letter row lowering.

### Completion Notes List

- `MicroGridPosition` now validates positive coordinates; `SheetPlacementConstraint` validates against declared `cellPitch` subdivision count.
- Spatial anchor lowering uses N subdivisions, explicit `(x,y)` offsets, and deterministic omitted center `(N/2,N/2)`.
- Added TypeScript `lowerSheetAnchor` parity helper and 10-test frontend suite.
- Verification: compiler tests passed; frontend tests 10/10; full Gradle 114 actionable tasks; encoding and source-set hygiene audits passed.

### File List

- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GridAlignedSpatialLayoutCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/GridAlignedSpatialLayoutCompilerTest.kt`
- `ide/theia-frontend/src/browser/diagram/grid-snap.ts`
- `ide/theia-frontend/scripts/athena-grid-snap.test.mjs`

### Change Log

- 2026-08-06: Replaced fixed-4 lowering with cell-count-driven anchor formulas and shared TS parity vectors.
- 2026-08-06: Revalidated full test surface and final M43 gates; status set to `done`.

### Change Log
