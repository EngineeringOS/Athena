# M26 Sample Project

This is the openable Athena workspace for the M26 semantic document projection milestone.

Open this folder in the IDE to verify the real Theia path:

- `src/01-workspace-semantic-source.athena` - shared control, power, and HMI source that projects
  into more than one sheet view
- `src/02-field-assets-not-a-sheet.athena` - field terminal and load source that still contributes
  to the generated document projection

Accepted M26 sheet-view titles:

- `Power Distribution`
- `Control And PLC Logic`
- `Field Wiring And Terminal Transition`

Use:

```powershell
yarn --cwd ide start:m26
```

The sample uses only admitted Athena source syntax. Source files are semantic workspace units, not
sheet-view boundaries. The document projection policy, reference facts, compact markers, and sheet
view navigation remain compiler/runtime-derived facts consumed by Theia.
