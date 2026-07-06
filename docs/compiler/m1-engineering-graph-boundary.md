# Athena M1 Engineering Graph Boundary

## Purpose

Story `2.1` introduces the first runtime-owned `Engineering Graph` projection over the active project.

This graph is not a second semantic model. It is a queryable runtime view derived from canonical `Engineering IR` so later GUI, diff, command, and inspection surfaces can navigate project structure without depending on parser-private or renderer-private shapes.

## Ownership Boundary

- `Engineering IR`
  - remains the only canonical semantic authority
- `AthenaRuntime`
  - remains the owner of workspace, active project, and execution context lifecycle
- `AthenaServiceRegistry`
  - resolves the runtime-owned engineering-graph capability as a typed shared service
- `AthenaExecutionContext`
  - exposes graph projection for the active project through the shared runtime service
- `AthenaEngineeringGraphService`
  - derives engineering-graph projections from the runtime-owned active canonical state
- `AthenaEngineeringGraph`
  - is a derived runtime projection for traversal and inspection only

The graph may help consumers inspect semantics, but it may not redefine them.

## Identity Boundary

The graph reuses canonical semantic identifiers directly:

- `system:<name>`
- `component:<name>`
- `port:<owner>.<port>`
- `connection:<from>-><to>`

Story `2.1` does not invent graph-only durable identities.
If a consumer sees a graph node identity, it is seeing the canonical semantic identity already assigned by lowering.

## Query Scope

The first runtime graph slice supports:

- node lookup by canonical semantic identity
- relationship lookup by source or target identity
- direct neighbor traversal
- dependency traversal from existing relationships
- authored reference lookup and resolved-reference lookup

This is enough for runtime-facing inspection surfaces to answer:

- what object is this
- what does it reference
- what depends on it
- what is adjacent to it in the current semantic graph

## Non-Goals

Story `2.1` does not introduce:

- semantic mutation
- command runtime behavior
- undo or redo
- diff or history views
- incremental recomputation
- plugin-owned graph authority

Those remain later Epic `2` work.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
```

These checks prove the runtime can expose a queryable engineering graph over the active project while preserving the invariant that canonical semantics still live in `Engineering IR`.
