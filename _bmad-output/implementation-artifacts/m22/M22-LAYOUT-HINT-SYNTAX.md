# M22 Layout Hint Syntax Decision

Decision: layout block.

M22 selects a small project-level `layout` block shape for component placement, alignment, and
grouping round-trip intent. This is a syntax decision and frontend preview/source-edit direction,
not completed language admission.

Current M22 limitation: the ANTLR grammar, authored AST, compiler, and LSP do not yet accept this
block as real `.athena` syntax. The real M22 sample sources therefore do not include it. Parser,
compiler, LSP, and sample-project admission are deferred to M23.

The block expresses engineering presentation intent as relationships. Raw pixel coordinates are not
the primary authored language.

Rule: raw pixel coordinates are not the primary authored language.

## Minimal Shape

```athena
layout schematic-sheet {
  place HMI1 near PLC1
  place XT1 below PLC1
  align HMI1 aligned-with PLC1 axis vertical
  group HMI1 grouped-with PLC1
}
```

## Supported M22 Intent Shape

- place a component near another component
- place a component below another component
- align components with `aligned-with`
- group components with `grouped-with`

These map to the M22 layout constraint vocabulary: `near`, `below`, `aligned-with`, and
`grouped-with`.

## Deferred

Parser/compiler/LSP support for the layout block is deferred to M23. Route and label persistence remains deferred.
M22 may use route and label facts for readability, but source-level layout block admission was not
completed in this milestone.
