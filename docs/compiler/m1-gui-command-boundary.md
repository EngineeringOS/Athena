# Athena M1 GUI Command Boundary

## Purpose

Story `2.4` proves the first GUI-originated semantic mutation over the existing runtime-owned command path.

This slice is intentionally narrow. It does not claim Athena already has a full graphical editor. It proves the more important architectural point: a desktop Compose surface can choose two runtime-derived compatible ports, dispatch an explicit `CONNECT_PORTS` command, and then refresh viewer, graph, inspector, and history-facing state from canonical runtime ownership without editing DSL text or mutating semantic objects locally.

## Ownership Boundary

- `AthenaComposeViewerWorkbenchSession`
  - owns app-layer GUI selection and command-panel state only
  - keeps one active `Athena Runtime` project session alive for the desktop workbench
  - rebuilds shell state from runtime-owned projections after each GUI intent
- `AthenaComposeShell`
  - remains a typed presentational surface
  - emits typed shell intents for source selection, target selection, and connect execution
  - does not mutate canonical semantics directly
- `AthenaCommandRuntimeService`
  - remains the only semantic mutation path for the GUI action
- `Engineering IR`
  - remains the only canonical semantic authority after the GUI command completes

The GUI session may hold disposable operator intent such as selected source and selected target. That state is not semantic truth. The semantic change only exists after the runtime accepts and applies the explicit command.

## First GUI Mutation Path

The first GUI-backed mutation path is:

1. The workbench session opens one runtime-managed project.
2. The session derives source and target port choices from runtime-owned graph data.
3. The operator selects a source port and a compatible target port in the command panel.
4. The session dispatches `AthenaConnectPortsCommand`.
5. The command runtime mutates canonical `Engineering IR`.
6. The session refreshes:
   - viewer scene
   - graph-backed port choices
   - inspector fields
   - console output
   - command history counts

That is the M1 proof path `GUI -> command -> canonical semantics -> refreshed projections`.

## Compatibility Rule In This Slice

The GUI panel only offers target ports that are runtime-derived and compatible for the first proof:

- source port direction must be `out`
- target port direction must be `in`
- both ports must carry the same `signal` property
- the exact connection must not already exist in the current runtime-managed graph state

This keeps the first GUI path honest without claiming a broader editor or rule-authoring surface.

## Non-Goals

Story `2.4` does not introduce:

- graphical port handles on the canvas
- drag-to-wire editing
- authored DSL text editing or regeneration
- arbitrary create, rename, or delete GUI mutations
- richer incremental recomputation policy
- a complete review UI for diffs and history

Those remain later work.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
```

Keep these checks sequential on Windows. Do not run Gradle verification tasks concurrently in this repository.
