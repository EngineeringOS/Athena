# M22 Layout Optimization Proof Usage

Updated: 2026-07-18

## Review Status

M22 is the governed layout optimization and component round-trip milestone. It proves Athena can
start turning layout intent into more professional engineering sheet presentation while the Theia
IDE remains a projection-only consumer.

The local proof corpus lives under `../../examples/m22/`:

- `sample-project/` is the openable IDE workspace with real `.athena` files
- `sample-project/M22-LAYOUT-ACCEPTANCE.md` is the professional layout acceptance checklist
- `sample-project/M22-BASELINE-PROOF.md` is the IDE-visible baseline proof
- `sample-project/M22-LAYOUT-REPLAY-PROOF.md` is the deterministic fact-level replay proof
- `_bmad-output/implementation-artifacts/m22/M22-ELK-SPIKE-ENVELOPE.md` records the isolated,
  local-only ELK spike envelope
- `_bmad-output/implementation-artifacts/m22/M22-ELK-COMPARISON.md` compares ELK-assisted output
  against Athena rule output using normalized Athena facts
- `_bmad-output/implementation-artifacts/m22/M22-LAYOUT-HINT-SYNTAX.md` records the selected layout
  block syntax for M22 source round-trip

## Supported Workflow Slice

Supported:

- openable M22 sample project in the Athena Theia IDE
- accepted M21 graph workbench behavior as the visible baseline
- real `.athena` source scenarios for layout optimization and component round-trip stories
- local proof checks for sample-project shape, launch path, graph workbench DOM behavior, and
  boundary discipline

Not supported in M22:

- public repository/import ecosystem work
- full IEC or QElectroTech library ingestion
- cabinet authoring
- cabinet, harness, cable tray, 3D installation, or physical wire routing
- AI layout or final layout-stack selection
- full EPLAN parity
- sheet-local drag-save truth

## IDE Usage

Use the normal Athena Theia flow:

```powershell
Set-Location ide
yarn start:m22
```

That command opens the sample project at `../../examples/m22/sample-project/`.

In the IDE:

- open the `.athena` source files in the sample project
- verify active-source projection by opening a non-baseline source before Graphical View; the graph
  does not fall back to the baseline seed file after the graph widget receives focus
- review `src/01-baseline-sheet.athena` for the accepted M21 graph workbench baseline
- review `src/02-layout-optimization-acceptance.athena` for power, protection, controller, HMI,
  terminal, and load readability subjects
- use `M22-LAYOUT-ACCEPTANCE.md` to inspect Story 2.3 governed placement and grouping evidence:
  preferred-zone constraints, grouped-with constraints, explicit group facts, and no renderer
  inference
- inspect Story 2.4 route and label evidence through basic route-lane preference and subject-bound
  label placement; this remains schematic topology and non-standards-specific label behavior
- review `src/03-component-round-trip.athena` for placement, alignment, grouping, and source
  round-trip subject identities
- approve a graph workbench placement, alignment, or grouping change from the approved layout preview;
  the layout block is appended to the active `.athena` source through the editor bridge
- review `src/04-boundary-scope.athena` for deferred-scope guardrails
- review `M22-LAYOUT-ACCEPTANCE.md` for the named comparison set and required layout acceptance
  checks
- review `M22-BASELINE-PROOF.md` for the accepted graph workbench baseline proof
- review `M22-LAYOUT-REPLAY-PROOF.md` to confirm layout facts are compared before screenshots or
  manual visual inspection
- switch to the schematic sheet surface
- inspect source and Problems against the same canonical subject
- confirm source, outline, Problems, and sheet identity remain coherent through the same canonical subject and occurrence identity
- use outline navigation in the same editor tab for `.athena` files
- use the top information icon to open the `Cabinet Main` popover
- use the floating bottom controls for zoom and canvas navigation
- confirm the stage grid remains the coordinate surface, `Cabinet Main` stays in the top information popover, and floating controls remain transparent
- whitespace click closes the info popover
- bottom controls remain icon-only and transparent

The smoke proof opens the same sample project, uses the home `Graphical View` action, and validates
the rendered graph workbench DOM for the stage grid, transparent overlays, sheet frame, info
popover, and whitespace-close behavior.

## Verification Path

Run checks sequentially on Windows:

```powershell
node --test ide/theia-frontend/scripts/athena-m22-sample-project.test.mjs
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM22SampleProjectCompilerTest
Set-Location ide
yarn workspace @engineeringood/athena-theia-product start:smoke:m22
Set-Location ..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Development Rules After M22 Story 1.1

- Keep semantic authority upstream of projection, layout constraints, layout facts, and rendering.
- Keep the sample project openable as a normal Athena project with real `.athena` files.
- Keep `.mjs` files as supporting tests only; they are not the customer proof.
- Keep M20/M21 canvas invariants intact.
- Keep future layout improvements routed through governed layout intent, constraints, and
  renderer-consumed facts rather than canvas-local state.

## Boundary Handoff

M22 deliberately leaves deferred domains for future milestones: public repository/import ecosystem,
full IEC/QElectroTech library ingestion, cabinet authoring, physical routing, AI layout, final
solver-stack decision, and full EPLAN parity. No hidden canvas state persists layout truth; approved
adjustments must become reviewable `.athena` layout intent.
