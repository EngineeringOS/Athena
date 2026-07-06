# Athena M1 Compose Interaction Boundary

## Purpose

Story `1.5` adds the first interactive inspection behavior to the Compose semantic viewer without changing the authority model established in Story `1.4`.

The viewer now supports:

- viewport tracking
- disposable camera state
- pan
- zoom
- hit-testing
- selection highlighting

This work improves navigation and inspection only. It does not create a second engineering model.

## Semantic Boundary

- `Engineering IR`
  - remains the canonical semantic source of truth
- `AthenaRuntime`
  - remains the owner of project activation and semantic compilation
- `AthenaRuntimeViewerProjection`
  - remains the runtime-facing projection consumed by the desktop app
- `:ui:compose-workbench`
  - owns session-local interaction infrastructure layered over the derived scene

Selection, viewport, and camera state are disposable viewer state. They may change how the current scene is inspected, but they do not redefine engineering meaning.

## Layout And Geometry Alignment

This story follows `manifesto/docs/architecture/09-layout-and-geometry.md`:

- semantic structure is not layout state
- layout and camera state are not semantic authority
- rendered geometry is derived and disposable

Hit-testing works against derived viewer geometry only. A hit result is an inspection signal, not a semantic mutation.

## Interaction Scope

Story `1.5` owns:

- drag-based panning
- zoom controls over the current viewport
- hit-testing against rendered component boxes and connection lines
- selection display and highlighting
- viewer-local camera reset

Story `1.5` does not own:

- command-backed semantic mutation
- engineering graph edits
- undo/redo
- diff/history inspection
- plugin-private rules
- electrical ownership inside generic viewer APIs

Those remain Epic `2` scope.

## Implementation Shape

The interaction slice is intentionally split in two layers:

- pure viewer-state types
  - `AthenaSemanticViewerViewport`
  - `AthenaSemanticViewerCamera`
  - `AthenaSemanticViewerSelection`
  - `AthenaSemanticViewerInteractionState`
- Compose binding
  - `AthenaSemanticViewerStage`

The pure types own camera math and hit-testing. The Compose stage only binds pointer input and control widgets to that disposable state.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :ui:compose-workbench:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
```

These checks prove that viewer interaction now exists while semantic authority still remains in the runtime and canonical IR path.
The Java `25` runtime-selection details behind these checks are documented in `docs/compiler/java-25-build-and-launch-notes.md`.
