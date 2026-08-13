# M003 Professional Workbench Design

## Status

Approved direction: Graphite-inspired editor shell and architecture, filled
with Athena's electrical-schematic kernel behavior.

## Goal

Turn the operable M002 shells into a professional electrical-schematic editor
whose shell, panel model, tool routing, viewport behavior, and presentation
separation follow the new-age Graphite/Zed model, while its domain vocabulary
and electrical authoring outcomes come from QElectroTech.

## Reference Separation

- QElectroTech source and user documentation remain the old-world inventory of
  electrical features, terminology, and expected authoring outcomes.
- Zed and Graphite are the new-world references for shell composition,
  command/tool routing, viewport ownership, contextual focus, and presentation
  separation.
- `gpui-component` supplies native component patterns where compatible with
  the desktop shell. No reference implementation or UI is copied directly.

## Reuse Principle

The efficient path is evidence-led reproduction: inspect Graphite/Zed code,
trace the relevant message, panel, viewport, and overlay boundaries, and
implement the same proven shape in Athena's own Rust contracts. We copy the
design and architecture patterns, not source files, dependencies, or
non-electrical product assumptions.

## Product Decisions

The default shell is Graphite-inspired and engineering-dense where it matters:

- persistent symbol library on the left;
- persistent inspector on the right;
- compact command and tool rows above the canvas;
- maximum practical canvas area between them;
- contextual panels that can collapse, dock, and restore without changing the
  document;
- dense engineering typography and restrained borders, with clear selected,
  active, disabled, and status states.

Desktop and browser shells share the same interaction model and panel roles.
They may use different widget implementations, but they must expose equivalent
commands, selection feedback, inspector fields, canvas affordances, and
presentation-only panel state. The browser is a WASM host, not a second editor
implementation.

## Layout Contract

The shell has Graphite-style stable regions:

1. Application/menu bar: product identity, project/document identity, file and
   history commands.
2. Contextual tool shelf: explicit active tool state, transform and viewport
   commands, and room for future tool groups.
3. Document viewport: the primary framed schematic canvas with viewport state,
   overlays, rulers/readouts, and pointer routing.
4. Dockable panels: electrical symbol catalog, layers/document structure, and
   typed properties. M003 implements catalog and properties first while
   preserving the panel boundary for layers.
5. Status and message region: command feedback, selection state, zoom/grid,
   and validation indicators.

The canvas remains the only mutation surface for schematic geometry. Shell
controls route through the existing shared Rust session; JavaScript and GPUI
only adapt events and render state.

## Interaction and Accessibility

- Every command has a stable `data-command`/element identity for automation.
- Tool buttons expose active state and accessible names.
- Library search filters visible symbols without changing the core catalog.
- Collapsing a rail changes presentation only and never changes document state.
- Empty selection shows sheet properties; selection shows typed symbol/wire
  properties.
- Status feedback is concise and updates after commands.
- Existing keyboard shortcuts and pointer semantics remain unchanged.

## Verification

The slice is complete only when:

- browser layout tests verify all five regions, active tool state, library
  filtering, rail collapse, and command routing;
- existing browser authoring tests still pass;
- native desktop authoring tests and clippy still pass;
- WASM rebuild and native launch both succeed;
- a verification note records commands and any unavailable OS-level checks.
