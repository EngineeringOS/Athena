# M44 Layout Coordinate Correction

**Status:** approved  
**Date:** 2026-08-07  
**Scope:** M44 Sheet companion, Canonical Scene, presentation operations, and Theia canvas

## Decision

M44 separates three presentation concerns that were incorrectly coupled:

```text
Plot Frame        page border and human navigation
Snap Grid         editing constraint
Sheet Point       persisted occurrence placement truth
```

The prior `grid: <columns> * <rows> cell: <subdivisions>` declaration made `cell` simultaneously:

- Plot Frame subdivision;
- Snap Grid density;
- placement coordinate codec.

That model is retired. Changing a frame or `cell` setting could reinterpret an existing placement.

## EPLAN Alignment

EPLAN Plot Frames define page size/alignment, title block, grid, and the path/position areas of columns
and rows. EPLAN Grid Snap places insertion and element points on grid points; Grid Snap is independent
from whether grid lines are displayed. Athena adopts that separation without importing EPLAN storage or
compatibility behavior.

## M44 Sheet Contract

The replacement Sheet surface is:

```athena
sheet "rolling-shutter" {
  page format A3 landscape
  frame: 17 * 16
  snap: 1
  title "Rolling Shutter Control"
  "Supply" at (8, 12)
  "Q1" at (24, 16) lock
}
```

- `frame: 17 * 16` means 17 horizontal columns and 16 vertical rows.
- Frame columns display `A..Q` across the top. Frame rows display `1..16` down the left.
- `snap: 1` is the document snap step in canonical integer Sheet units.
- `(x, y)` is the occurrence's canonical, persisted Sheet Point. It is not a cell-relative coordinate.
- `lock` remains presentation placement protection only.
- The canvas may accept an `A2` target as a human input aid, but the LSP resolves it to a canonical Sheet
  Point before it writes source. `A2` is never persisted placement truth.

`snap` changes only affect subsequent Move, Align, Distribute, and Snap quantization. Existing points
remain unchanged. `frame` changes only change rulers, page decoration, and point-to-frame reference
mapping. Existing points remain unchanged.

## Authority

```text
*.sheet.athena
  owns frame, snap step, title, canonical occurrence Sheet Points, lock

AthenaDiagramScene
  carries ScenePlotFrame, SceneSnapGrid, and canonical occurrence placement anchors

Theia
  paints frame labels and optional interaction grid
  never persists coordinates or frame mappings
```

No Engineering Reality, Symbol metadata, SVG geometry, Representation Binding, or route paint owns
Sheet Point truth.

## Required Breaking Changes

1. Remove `SheetGridIntent.cell`, `SheetCellReference`, and `SheetMicroAnchor`.
2. Replace them with `SheetPlotFrameIntent`, `SheetSnapIntent`, and `SheetPoint`.
3. Replace `SheetAnchor(address, microX, microY)` with `SheetPoint(x, y)` in typed operations.
4. Add `ScenePlotFrame` next to `SceneSnapGrid`; do not put ruler rows/columns in `SceneSnapGrid`.
5. Reverse current label mapping: letters are horizontal columns and numbers are vertical rows.
6. Delete old `micro(...)` grammar, tests, examples, and compatibility code. Athena is pre-1.0.
7. Use a transaction-based `Change Plot Frame` operation later if Athena exposes interactive frame changes.
   M44 only preserves point truth when source is changed and recompiles it.

## Acceptance Proof

- Same persisted occurrence point renders at same scene point after `frame` count changes.
- Same persisted occurrence point renders at same scene point after `snap` changes.
- A point maps to `A2` only through active frame geometry; `A2` is horizontal column A, vertical row 2.
- Snap on/off and optional grid-line display do not alter persisted points.
- Move, Align, Distribute, and Snap write only `(x, y)` Sheet Points through the existing transaction
  engine and survive reopen.
