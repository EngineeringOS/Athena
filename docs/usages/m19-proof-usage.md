# M19 First Professional Engineering Sheet Workflow Usage

Updated: 2026-07-16

## Review Status

M19 is closed from a story perspective. All 8 stories in
`../../_bmad-output/implementation-artifacts/m19/sprint-status.yaml` are done.

M19 proves that Athena can present governed semantic engineering meaning as a professional
schematic-sheet workflow in Theia without moving authority into the frontend:

- canonical semantics project into a schematic sheet with stable identity
- deterministic sheet IR and layout facts drive rendering
- source, Problems, inspector, and reveal stay synchronized through canonical subject ids
- cabinet preview stays deferred from M19
- repository/import ecosystem work stays out of M19
- the frontend remains projection-only

## Supported Workflow Slice

Use the governed schematic proof set in `../../examples/m19/` as the entry point.

Supported:

- schematic sheet chrome and title block
- deterministic labels, terminals, conductors, and cross-reference markers
- source-to-sheet reveal
- Problems/diagnostic-to-sheet reveal
- canonical selection round-trips
- cabinet-preview and ecosystem boundary checks

Not supported in M19:

- cabinet preview authoring
- full EPLAN parity
- full IEC library breadth or a full element-catalog program
- public repository/import ecosystem work
- frontend-owned semantic resolution

## Proof Corpus

Inventory:

- `../../examples/m19/schematic-sheet-proof/`
  - governed schematic fixture input
  - deterministic sheet proof outputs
  - selection and reveal proof coverage

These fixtures are local and governed. They do not imply registry, marketplace, desktop-viewer,
Kotlin Compose, or frontend-local semantic-resolution behavior.

## IDE Usage

Use the normal Theia flow:

- open a governed Athena project
- switch to the schematic sheet surface
- inspect source and Problems against the same canonical subject
- use selection/reveal to move between source and rendered sheet

The frontend renders compiler/LSP results. It must not independently resolve imports, packages,
or symbols.

The current front-end launch path is:

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-product start:smoke
yarn workspace @engineeringood/athena-theia-product start
```

## Verification Path

Run checks sequentially on Windows:

```powershell
yarn --cwd ide/theia-frontend test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Development Rules After M19

- Keep semantic authority upstream of projection, layout, and rendering.
- Keep cabinet preview deferred unless a later milestone explicitly owns it.
- Keep repository/import ecosystem work out of schematic-sheet milestone work.
- Keep future scope expansions explicit in PRD, architecture, stories, proof fixtures, and usage docs.

## Retrospective Pointer

The milestone retrospective summary is recorded in
`../../_bmad-output/implementation-artifacts/m19/epic-3-retro-2026-07-16.md`.
