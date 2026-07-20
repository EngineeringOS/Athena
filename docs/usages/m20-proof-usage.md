# M20 Presentation Proof Usage

Updated: 2026-07-17

## Review Status

M20 is the presentation-fidelity milestone. It proves Athena can present governed semantics as a
professional engineering sheet without moving authority into the frontend.

The local proof corpus lives under `../../examples/m20/`:

- `schematic-sheet-proof/` reuses the M19 schematic proof and adds explicit sheet-composition facts
- `dense-sheet-proof/` adds denser placement and drawing-rule metadata
- `acceptance-sheet-proof/` aliases the schematic proof as the customer-facing baseline

## Supported Workflow Slice

Use the governed M20 fixtures as the entry point for presentation work.

Supported:

- sheet composition and representation families
- deterministic layout facts and dense-content drawing rules
- source, Problems, and sheet round-trips through canonical ids
- viewport choreography and repeated-fit behavior
- acceptance review against the customer-facing proof
- boundary checks for deferred scope

Not supported in M20:

- cabinet preview authoring
- public repository/import ecosystem work
- full IEC breadth
- frontend-owned semantic reconstruction
- a final layout-stack decision

## IDE Usage

Use the normal Athena Theia flow:

```powershell
Set-Location ide
yarn start:m20
```

That opens the sample project at `../../examples/m20/sample-project/`.

Use the sample project to inspect the finished scenarios as real `.athena` source files.

```powershell
yarn workspace @engineeringood/athena-theia-product start:smoke
yarn workspace @engineeringood/athena-theia-product start
```

In the IDE:

- open a governed Athena project
- switch to the schematic sheet surface
- inspect source and Problems against the same canonical subject
- use outline navigation in the same editor tab for `.athena` files
- use the top information icon to open the `Cabinet Main` popover for selection, sheet, and
  cross-reference details
- use the floating bottom controls for zoom and canvas navigation; the controls are chrome over the
  canvas, not sheet publication content
- use the acceptance fixture for customer-facing evaluation
- use the dense fixture to check readability at common window sizes
- use the boundary fixture to confirm deferred scope stays deferred

## Verification Path

Run checks sequentially on Windows:

```powershell
yarn --cwd ide/theia-frontend test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Development Rules After M20

- Keep semantic authority upstream of projection, layout, and rendering.
- Keep the proof corpus local and governed.
- Keep cabinet preview, repository/import work, IEC breadth, and stack selection out of M20 follow-up.
- Keep reveal and selection keyed to canonical ids.

## Retrospective Pointer

The M20 retrospective summary is recorded in
`../../_bmad-output/implementation-artifacts/m20/epic-4-retro-2026-07-17.md`.

The M20 UI acceptance debugging retrospective is recorded in
`../../_bmad-output/implementation-artifacts/m20/m20-ui-acceptance-deep-retrospective-2026-07-17.md`.
