# M21 Layout Intelligence Proof Usage

Updated: 2026-07-17

## Review Status

M21 is the engineering layout-intelligence milestone. It proves Athena can begin improving schematic
grouping, schematic conductor routing, and label readability from governed layout contracts while the
Theia IDE remains a projection-only consumer.

The local proof corpus lives under `../../examples/m21/`:

- `sample-project/` is the openable IDE workspace with real `.athena` files

## Supported Workflow Slice

Supported:

- openable M21 sample project in the Athena Theia IDE
- accepted M20 canvas behavior as the visible baseline
- real `.athena` source scenarios for later layout-intent and schematic-routing stories
- local proof checks for sample-project shape and launch path

Not supported in M21:

- public repository/import ecosystem work
- full IEC or QElectroTech library ingestion
- cabinet authoring
- cabinet, harness, cable tray, 3D installation, or physical wire routing
- desktop-viewer scope
- AI layout or final layout-stack selection
- sheet-local drag-save truth

## IDE Usage

Use the normal Athena Theia flow:

```powershell
Set-Location ide
yarn start:m21
```

That command opens the sample project at `../../examples/m21/sample-project/`.

In the IDE:

- open the `.athena` source files in the sample project
- switch to the schematic sheet surface
- inspect source and Problems against the same canonical subject
- use outline navigation in the same editor tab for `.athena` files
- use the top information icon to open the `Cabinet Main` popover
- use the floating bottom controls for zoom and canvas navigation
- whitespace click closes the info popover
- bottom controls remain icon-only and transparent

The smoke proof opens the same sample project, uses the home `Graphical View` action, and validates
the rendered graph workbench DOM for the stage grid, transparent overlays, sheet frame, info
popover, and whitespace-close behavior.

## Verification Path

Run checks sequentially on Windows:

```powershell
node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs
node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs
node --test ide/theia-frontend/scripts/athena-m21-canvas-contract.test.mjs
Set-Location ide
yarn workspace @engineeringood/athena-theia-product start:smoke:m21
Set-Location ..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Development Rules After M21 Story 1.1

- Keep semantic authority upstream of projection, layout intent, layout facts, and rendering.
- Keep the sample project openable as a normal Athena project with real `.athena` files.
- Keep `.mjs` files as supporting tests only; they are not the customer proof.
- Keep M20 canvas invariants intact.
