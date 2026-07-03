# Athena M0 DSL

## Scope

Story `1.2` defines the first executable Athena language slice as a small standalone text DSL for:

- system declaration
- device declarations
- port declarations
- connection declarations

This is a syntax-only boundary. The parser does not assign semantic meaning beyond preserving authored structure, references, and source provenance.

## Current Example Shape

```text
system DemoCabinet {
  device PLC1 {
    type PLC
    model "S7-1200"
  }

  device M1 {
    type Motor
  }

  port PLC1.out {
    direction out
    signal Digital
  }

  port M1.in {
    direction in
    signal Digital
  }

  connect PLC1.out -> M1.in
}
```

## Rules

- The DSL is standalone text, not an embedded Kotlin builder.
- The DSL expresses engineering intent only.
- Layout, geometry, renderer settings, page coordinates, and view mechanics are excluded.
- The AST is syntax-only and is not the canonical semantic model.
- Parse diagnostics must retain file, line, and column provenance.

## Expected Story 1.2 Outcome

- A valid source file parses deterministically into the same AST every run.
- A malformed source file emits syntax diagnostics and stops before later semantic or rendering phases.
- `examples/m0/demo-cabinet.athena` is the first real conformance seed for later lowering work.
