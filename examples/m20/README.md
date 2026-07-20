# M20 Proof Corpus

This directory contains the local executable proof corpus for the M20 sheet-composition milestone.

- `schematic-sheet-proof/` extends the M19 schematic proof with governed sheet composition facts.
- `acceptance-sheet-proof/` aliases the schematic proof as the customer-facing acceptance baseline.
- `sample-project/` is the openable IDE workspace that presents the finished M20 scenarios as real
  `.athena` source files.

The corpus is local and governed. It does not imply registry, marketplace, public repository, import
ecosystem, or frontend-owned semantic reconstruction behavior.

To inspect the customer-facing proof in the IDE:

```powershell
Set-Location ../../ide
yarn start:m20
```

The schematic canvas uses the main stage grid as the coordinate surface. Sheet content and component
bodies stay transparent over that grid. `Cabinet Main` details are available from the top information
icon popover only; they are not rendered inside the sheet canvas.
