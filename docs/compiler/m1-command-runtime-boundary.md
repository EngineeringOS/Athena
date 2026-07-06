# Athena M1 Command Runtime Boundary

## Purpose

Story `2.2` introduces the first runtime-owned semantic mutation boundary.

This slice does not build a full editor. It proves the narrower architectural point: canonical project semantics now live behind an explicit runtime-owned command path, and later GUI, CLI, plugin, or AI mutation surfaces must reuse that same path instead of editing canonical objects directly.

## Ownership Boundary

- `Engineering IR`
  - remains the only canonical semantic authority
- `AthenaRuntime`
  - remains the owner of workspace, active project, and execution context lifecycle
- `AthenaExecutionContext`
  - now caches the active project's current canonical compilation state
  - resolves the shared command runtime through the typed service registry
- `AthenaServiceRegistry`
  - resolves the runtime-owned command service as a shared capability
- `AthenaCommandRuntimeService`
  - accepts explicit runtime-owned commands
  - applies semantic mutation over canonical runtime state
  - returns inspectable execution results for later history and diff work

The command runtime may change canonical semantics, but it may not relocate ownership away from runtime-managed `Engineering IR`.

## Mutation Rule

Story `2.2` makes one rule concrete:

- semantic mutation must enter Athena as an explicit runtime-owned command
- graph and viewer projections must consume runtime-owned active state after commands
- callers do not get a second mutation path through UI-local models, parser-private objects, or renderer-private structures

The first concrete command is `CONNECT_PORTS`.
It creates one canonical connection between two existing canonical port identities without editing authored DSL text.

The first surface-level adapter in this slice is the CLI `connect` command:

- `connect <source-file> <source-port-path> <target-port-path>`

That adapter resolves authored port paths into canonical port identities, executes the runtime-owned command, and reports the resulting canonical and viewer-facing state without bypassing the command boundary.

Story `2.3` extends this boundary with runtime-owned command history, undo, redo, replay, and serialization. See `docs/compiler/m1-command-history-boundary.md`.

## Inspectability Rule

The first command runtime result exposes:

- command kind
- runtime project name
- canonical document before the mutation
- canonical document after the mutation
- changed semantic identities

That is enough for later stories to add history, diff, undo, redo, replay, and serialization without redesigning the mutation boundary.

## Non-Goals

Story `2.2` does not yet introduce:

- undo or redo
- replay
- serialized command records
- diff presentation
- incremental affected-scope recomputation
- plugin-hosted command contributions
- GUI wiring for the first command

Those remain later Epic `2` work.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
```

These checks prove the runtime can execute the first semantic mutation through an explicit command path while keeping canonical ownership in `Engineering IR` and runtime-managed active state.
