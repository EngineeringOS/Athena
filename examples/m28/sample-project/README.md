# M28 Sample Project

This is the openable Athena workspace for the M28 governed component anatomy and semantic
relationship authoring milestone.

Open this folder in the IDE to verify the normal Theia path:

- `src/01-relationship-authoring-source.athena` - primary nested-port source with existing
  committed connections plus one compatible unconnected electrical relationship candidate
- `src/02-relationship-candidates.athena` - additional nested-port candidates for invalid
  relationship rejection proof

Source files are semantic workspace units, not sheet or page boundaries.

M28 canonical source style uses nested device-owned ports:

```athena
device ControllerPLC1 {
  type Switch
  model "PLC"

  port do1 {
    direction out
    signal Digital
  }
}
```

The canonical port identity remains `port:ControllerPLC1.do1`.
