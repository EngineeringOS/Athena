# M24 Achievement, Usage, And Retrospective

Date: 2026-07-19

## Achievement Summary

M24 moved Athena from graph-like rendered connections toward governed schematic routing fidelity.

Implemented proof layers:

- `kernel/routing-model` owns routing contracts for connection intent, routing policy, port
  presentation policy, terminal anchors, route constraints, route facts, labels, and route quality.
- The compiler and presentation path derive route facts from semantic connections and terminal
  anchors.
- The Theia Graphical View renders route paths, terminal markers, crossing markers, labels, and
  route inspection data from governed route facts.
- `examples/m24/sample-project` is a real IDE-openable project with `.athena` sources.
- Product Electron smoke opens the M24 sample project and verifies route DOM proof in the actual
  Theia product path.

## Usage

Open the sample:

```powershell
yarn --cwd ide start:m24
```

Run the product smoke:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd ide build
yarn --cwd ide start:smoke:m24
```

Primary usage doc:

- `../../../docs/usages/m24-proof-usage.md`

M23-vs-M24 comparison:

- `../../../docs/usages/m24-routing-acceptance-proof.md`

## Product Smoke Evidence

Observed on 2026-07-19:

```text
routeCount=1
terminalCount=2
labelCount=1
routesWithTerminalAnchors=1
routesWithOrthogonalBends=1
centerFallbackRouteIds=[]
quality=SATISFIED
```

This proves the accepted sample path is not using renderer-owned center-to-center fallback for the
visible route proof.

## What M24 Actually Proves

- Athena can derive schematic route facts from existing source semantics without new route syntax.
- Route facts can carry terminal anchors, port identities, labels, quality, and route geometry.
- Theia can consume and inspect route facts without becoming the route authority.
- The sample can be opened as a project, not explained through `.mjs` files.
- Product smoke can catch missing route facts or fallback route rendering.

## What Remains Deferred

- full EPLAN parity
- cabinet routing
- physical wire routing
- harness, cable tray, and 3D routing
- route editing
- route-hint syntax
- AI routing
- ELK, Graphviz, yFiles, or another generic router as the Athena architecture
- standards-specific IEC/EPLAN/QElectroTech presentation policy breadth

## Lessons Learned

- Product-facing sample projects must be verified through project-aware LSP semantics, not only
  single-file compiler tests. Story 5.1 found duplicate device names across sample files that were
  valid individually but ambiguous in the package namespace.
- Smoke tests must rebuild or confirm the installed LSP host before claiming IDE success. M23's
  stale-host lesson still applies.
- The route proof needs inspectable facts, not screenshots only. DOM markers on route facts,
  terminals, and labels made product smoke actionable.
- M24 improved routing fidelity, but it is still a narrow first step. The line quality is better
  than M23 graph-like edges, but it is not yet the full ordered EPLAN/cabinet-like routing shown in
  the reference image.

