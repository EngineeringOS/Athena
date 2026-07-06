# Athena M1 Incremental Recompute Boundary

## Purpose

Story `2.5` adds the first real incremental recompute slice above the existing runtime-owned command path.

This is still intentionally narrow. Athena does not claim a general incremental compiler yet. The point of this slice is to prove the ownership rule for M1 mutations:

- the runtime derives the affected semantic scope from changed canonical identities
- the compiler consumes that scope during recompute
- the compiler emits one inspectable incremental update report
- downstream viewers and GUI adapters refresh from runtime-owned state instead of inventing local editor truth

That keeps the existing architectural thesis intact: DSL remains the source of truth, Engineering IR remains canonical, and runtime mutation still travels through compiler-owned validation and render derivation.

## Boundary Split

- `AthenaCommandRuntimeService`
  - still owns command execution and history mutation
  - now forwards changed semantic identities into runtime recompute
- `AthenaExecutionContext`
  - derives the minimal affected scope for the current mutation slice
  - exposes runtime-owned diagnostics and incremental update reporting to app layers
- `AthenaCompiler`
  - accepts affected scope plus previous rendering during recompute
  - runs scoped validation where supported
  - attempts scoped render refresh before falling back to full derivation
- `AthenaComposeViewerWorkbenchSession`
  - does not inspect compiler internals directly
  - reads runtime-owned diagnostics and incremental recompute metadata only

## Affected Scope In This Slice

The current M1 affected scope is deterministic and intentionally conservative.

- `changedSemanticIds`
  - the exact stable semantic identities reported by the accepted runtime command
- `validationSemanticIds`
  - changed identities plus directly implied port and owning component identities
- `renderComponentSemanticIds`
  - owning components of changed or referenced ports
- `renderConnectionSemanticIds`
  - changed connection identities, including connections removed by undo or replay

This is enough for the first `CONNECT_PORTS` mutation path, including undo, redo, and replay.

## Incremental Policy

- Validation
  - uses a scoped validator pass when runtime supplies affected scope
  - still preserves full compiler ownership over semantic diagnostics and continuation policy
- Rendering
  - reuses the prior component layout when the current document shape is compatible
  - refreshes only the affected connection lines in the first scoped path
  - falls back to full render derivation when scoped reuse is not safe
- Reporting
  - each recompute emits `CompilerIncrementalUpdateReport`
  - runtime republishes that as a runtime-owned report for app layers

## Non-Goals

Story `2.5` does not introduce:

- a generic dependency graph for all future compiler passes
- partial parsing or partial lowering
- full incremental semantics for plugin-owned domain rules
- geometry-aware partial layout invalidation
- UI-specific caching rules or editor-side semantic state

Those remain later work.

## Verification Path

From the repo root:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests "com.engineeringood.athena.runtime.AthenaCommandRuntimeTest"
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test --tests "com.engineeringood.athena.apps.composeviewer.AthenaComposeViewerWorkbenchSessionTest"
java25; .\gradlew.bat --no-daemon --console=plain build
java25; .\gradlew.bat --no-daemon --console=plain test
```

Keep these checks sequential on Windows. Do not run Gradle verification tasks concurrently in this repository.
