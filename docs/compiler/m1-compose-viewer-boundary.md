# Athena M1 Compose Viewer Boundary

## Purpose

Story `1.4` turns the Compose bootstrap from Story `1.3` into the first real semantic display proof. The desktop viewer now loads an active runtime-managed project, compiles it through the existing runtime-owned path, derives a viewer scene from downstream rendering output, and displays that scene in the shared Compose shell.

This story proves `DSL -> Engineering IR -> Compose Viewer`.

## Ownership Boundary

- `AthenaRuntime`
  - Owns workspace opening, project activation, and execution-context creation.
- `AthenaExecutionContext`
  - Owns the runtime-facing `compileActiveProject()` entrypoint used by the viewer bootstrap.
- `:apps:desktop-viewer`
  - Owns desktop bootstrap and fixture-oriented runtime session startup for the proof application.
- `:ui:compose-workbench`
  - Owns the domain-neutral shared shell and derived viewer scene presentation.

The Compose UI does not construct parser, lowerer, validator, or semantic state privately. It receives a derived scene from runtime-coordinated compilation results.

## Derived Viewer Scene

Story `1.4` introduces a read-only semantic viewer scene:

- `AthenaSemanticViewerScene`
- `AthenaSemanticViewerComponentBox`
- `AthenaSemanticViewerConnectionLine`

These types are derived from `CompilerRenderingSuccess` and remain viewer-facing presentation state only. They do not become canonical semantic ownership.

## Display Scope

Story `1.4` owns:

- displaying the active project identity
- displaying derived component boxes
- displaying derived connection lines
- showing a calm runtime-facing semantic viewer inside the shared Compose shell

Story `1.4` does not own:

- viewport camera behavior
- pan or zoom
- selection state
- hit-testing
- semantic mutation
- diff/history panels

Those remain later work, primarily Story `1.5` for viewing interaction and Epic `2` for mutation/history behavior.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :ui:compose-workbench:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
```

These checks prove that the active project can be rendered through runtime-owned orchestration without leaking semantic authority into the Compose UI.
The Java `25` build and desktop-launch experiment that established this verification path is documented in `docs/compiler/java-25-build-and-launch-notes.md`.
