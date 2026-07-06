# Athena M1 Runtime Host Boundary

## Purpose

Story `1.1` adds the first runtime-owned layer above the proven M0 compiler path. The goal is architectural, not feature-rich: Athena now has a runtime host that owns workspace lifecycle, active project state, execution context creation, and typed service lookup without rewriting the M0 compiler pipeline.

Story `1.2` extends that boundary so the existing DSL path is now invoked through runtime-owned orchestration. The compiler remains real and still owns parse, lower, validate, and downstream derivation, but the frontend path is no longer supposed to bootstrap compiler internals directly.

Story `1.3` then adds the first Compose bootstrap layer above this runtime boundary. See `docs/compiler/m1-compose-bootstrap-boundary.md` for the module split and bootstrap-specific limits.
Story `2.1` extends the same runtime ownership with a queryable engineering-graph projection over the active project. See `docs/compiler/m1-engineering-graph-boundary.md`.
Story `2.2` extends it again with the first runtime-owned semantic mutation path. See `docs/compiler/m1-command-runtime-boundary.md`.
Story `2.3` then adds runtime-owned command history over that same mutation path. See `docs/compiler/m1-command-history-boundary.md`.
Story `2.4` then adds the first GUI-backed adapter over the same runtime-owned command path. See `docs/compiler/m1-gui-command-boundary.md`.
Story `2.5` then adds runtime-derived affected scope and incremental recompute reporting over that same mutation path. See `docs/compiler/m1-incremental-recompute-boundary.md`.
Story `2.6` then adds runtime-owned semantic diff and history consequence inspection over that same mutation path. See `docs/compiler/m1-diff-history-inspection-boundary.md`.
Story `2.7` then turns plugins into a runtime-hosted capability while keeping compiler and runtime on one approved inventory. See `docs/compiler/m1-plugin-runtime-hosting-boundary.md`.

## Runtime-Owned Surface

- `AthenaRuntime`
  - Owns opening and closing a workspace.
  - Tracks the active workspace and active execution context.
- `AthenaWorkspace`
  - Keeps path-backed workspace state in memory.
  - Allows a project to become active without triggering compilation.
- `AthenaProjectRef`
  - Acts as the minimal path-backed project handle for M1.
- `AthenaExecutionContext`
  - Binds one active project to one runtime-owned service registry.
  - Resolves compiler, graph, command, and renderer capabilities through runtime-owned accessors.
- `AthenaServiceRegistry`
  - Exposes typed capability lookup for compiler, graph, command, renderer, and a minimal forward-looking plugin runtime contract.

## Module Boundary

- New module: `:kernel:runtime`
- Minimal dependency role:
  - depends on `:kernel:compiler`
  - depends on `:kernel:svg-renderer`
- CLI relationship:
  - `:apps:cli` may depend on `:kernel:runtime`
  - CLI help now reports the runtime host above the compiler
  - the existing `parse <source-file>` command now opens runtime-owned workspace and project state before invoking the parse path through `AthenaExecutionContext`

## DSL Frontend Routing

- DSL is now treated as a frontend adapter to the runtime boundary.
- Runtime-owned execution context exposes explicit DSL-oriented compiler entrypoints for the active project:
  - `parseActiveProject()`
  - `lowerActiveProject()`
  - `compileActiveProject()`
- These runtime methods call the existing `AthenaCompiler` service using the active project's path-backed source handle.
- The runtime owns lifecycle and orchestration; `AthenaCompiler` still owns compiler-pass behavior.

## Non-Goals In Stories 1.1 And 1.2

- No Compose UI, viewer shell, docking, panels, or welcome screen behavior yet.
- No command runtime, graph model, persistence layer, manifest format, or history system.
- No replacement or reuse of `compiler.plugin.AthenaCoreRuntime`; that remains the compiler plugin compatibility surface.
- No change to `Engineering IR` ownership; IR remains the canonical semantic authority.

Stories `2.1` and `2.2` intentionally extend this runtime boundary afterward with graph inspection and command-backed mutation, without changing the ownership rules above.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :apps:cli:test
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
.\gradlew.bat --no-daemon --console=plain :apps:cli:run --args="--help"
.\gradlew.bat --no-daemon --console=plain :apps:cli:run --args="parse examples/m0/demo-cabinet.athena"
```

These checks prove that the runtime host sits above M0 and that the DSL frontend now travels through runtime-owned workspace and execution-context orchestration without rewriting the existing compiler path.
See `docs/compiler/java-25-build-and-launch-notes.md` for the final Java `25` launcher and daemon behavior behind this verification path.
