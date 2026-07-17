# M22 IDE-Visible Baseline Proof

This proof fixes the visible starting point for M22 before layout optimization and round-trip stories
change the sheet behavior.

## Proof Path

1. Open the M22 sample project in Athena Theia:

   ```powershell
   Set-Location ide
   yarn start:m22
   ```

2. Open `src/01-baseline-sheet.athena`.
3. Launch `Graphical View`.
4. Confirm the graph workbench renders the active source file from `examples/m22/sample-project`.

The automated proof uses:

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-product start:smoke:m22
```

## Required Baseline Behavior

- The stage grid remains visible as the coordinate surface.
- Sheet and component bodies do not hide the grid.
- `Cabinet Main` information remains in the top information popover only.
- Top and bottom controls remain transparent canvas overlays.
- Whitespace click closes the information popover.
- The sample opens as the active source workspace, not stale workspace history.

## Smoke Assertions

The M22 smoke proof checks the same accepted M20/M21 graph workbench markers:

- graph workbench root
- stage
- viewport
- sheet
- canvas
- transparent floating bar
- transparent bottom dock
- transparent zoom dock
- transparent sheet body
- sheet frame
- stage grid
- `Cabinet Main` popover open
- popover close on whitespace

This story does not add layout optimization, ELK, or source round-trip behavior. It only proves the
accepted baseline surface that later M22 stories must preserve.
