# `:kernel:runtime`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:runtime` module owns Athena's long-lived execution boundary. It manages workspace lifecycle, active project context, runtime service resolution, projection sessions, command execution, history, graph projection, hosted plugin lifecycle inspection and execution, and optional AI proposal review without becoming a second semantic authority.

## Responsibilities

- Open and close workspaces through `AthenaRuntime`.
- Activate projects into a shared `AthenaExecutionContext`.
- Resolve runtime-owned services such as graph, command, plugin, and renderer coordination.
- Consume the governed approved plugin inventory from `:kernel:plugins:plugin-host`.
- Expose runtime-visible plugin lifecycle inspection without handing orchestration ownership to plugins.
- Host runtime-owned projection sessions with supported-view discovery and active-view switching.
- Keep canonical runtime state aligned with `Engineering IR`.
- Host command history, undo, redo, replay, diff inspection, and accepted AI proposal flow.
- Publish runtime-visible incremental refresh metadata after supported semantic mutations.

## Main Types

- `AthenaRuntime`
- `AthenaWorkspace`
- `AthenaExecutionContext`
- `AthenaServiceRegistry`
- `AthenaRuntimeProjectionSession`
- `AthenaCommandRuntimeService`
- `AthenaEngineeringGraphService`

## Dependencies

- `:kernel:compiler`
- `:kernel:plugins:plugin-host`
- `:kernel:engineering-model`
- `:kernel:svg-renderer`

## Boundaries

This module does not parse DSL source text directly, define the canonical IR schema, or own domain semantics. It owns runtime lifecycle and orchestration above those lower layers. Projection sessions remain runtime-owned state over compiler-derived projection artifacts; switching views does not mutate canonical engineering semantics.

## Incremental Refresh Boundary

Story `2.3` keeps the runtime contract intentionally narrow:

- The first scoped refresh proof is limited to the existing `connect ports` command path.
- `AthenaExecutionContext.incrementalUpdateReport()` exposes semantic scope plus layout, geometry, and rendering refresh modes.
- Runtime owns refresh coordination and active projection replacement, but compiler still owns every derivation rule.
- Desktop and other consumers read refreshed projection state through runtime-owned projections rather than maintaining private view caches.
- If compiler reuse is not safe, runtime surfaces the fallback mode instead of masking it.

## Review Contract

Story `2.4` keeps semantic review primary while making projection refresh inspectable:

- `AthenaSemanticDiffInspection` remains anchored in canonical semantic ids and command-linked history consequences.
- Projection refresh evidence is attached as downstream consequence metadata, not as a second history or diff system.
- Runtime inspection may explain affected views and downstream layers, but it does not replace semantic change review with geometry-only review.

## Verification

```bash
./gradlew :kernel:runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:runtime:test
```

