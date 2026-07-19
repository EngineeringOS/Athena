# M25 Sample Project

This is the openable Athena workspace for the M25 symbol and presentation policy milestone.

Open this folder in the IDE to verify real `.athena` presentation scenarios:

- `src/01-professional-symbol-sheet.athena` - mandatory PLC, terminal block, power supply, and load path
- `src/02-terminal-labels-and-routes.athena` - terminal marker and label-heavy route proof
- `src/03-six-family-acceptance.athena` - six-family presentation acceptance slice

Use:

```powershell
yarn --cwd ide start:m25
```

The sample is the product-facing proof path. Review it in the IDE, Graphical View, outline, and
inspection surfaces. It uses only admitted Athena source syntax; M25 representation policy and
symbol anatomy remain compiler/projection-owned facts consumed by Theia.
