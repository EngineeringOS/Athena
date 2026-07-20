# M27 Sample Project

This is the openable Athena workspace for the M27 professional sheet visual acceptance milestone.

Open this folder in the IDE to verify the real Theia path:

- `src/01-workspace-semantic-source.athena` - shared control, power, and HMI source that
  contributes to the professional proof
- `src/02-field-assets-not-a-sheet.athena` - field terminal and load source that still contributes
  to the generated project proof

Use:

```powershell
yarn --cwd ide start:m27
```

The sample uses only admitted Athena source syntax. Source files are semantic workspace units, not
sheet-view boundaries, and each file remains independently projectable through the current
active-editor projection path. Cross-file route projection is deferred until Athena has a governed
workspace projection compiler path for active Theia views.

Later M27 stories will refine the sheet frame, linework fidelity, compact labels, and connection
preview behavior without changing the proof workspace boundary.
