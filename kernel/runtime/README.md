# `:kernel:runtime`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:runtime` module owns Athena's long-lived execution boundary. It manages workspace lifecycle, active project context, runtime service resolution, command execution, history, graph projection, plugin hosting, and optional AI proposal review without becoming a second semantic authority.

## Responsibilities

- Open and close workspaces through `AthenaRuntime`.
- Activate projects into a shared `AthenaExecutionContext`.
- Resolve runtime-owned services such as graph, command, plugin, and renderer coordination.
- Keep canonical runtime state aligned with `Engineering IR`.
- Host command history, undo, redo, replay, diff inspection, and accepted AI proposal flow.

## Main Types

- `AthenaRuntime`
- `AthenaWorkspace`
- `AthenaExecutionContext`
- `AthenaServiceRegistry`
- `AthenaCommandRuntimeService`
- `AthenaEngineeringGraphService`

## Dependencies

- `:kernel:compiler`
- `:kernel:engineering-model`
- `:kernel:svg-renderer`

## Boundaries

This module does not parse DSL source text directly, define the canonical IR schema, or own domain semantics. It owns runtime lifecycle and orchestration above those lower layers.

## Verification

```bash
./gradlew :kernel:runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:runtime:test
```
