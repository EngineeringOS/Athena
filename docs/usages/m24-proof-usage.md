# M24 Governed Schematic Routing Fidelity Usage

Updated: 2026-07-19

## Review Status

M24 is the governed schematic routing fidelity milestone. It follows M23 layout-hint language
admission by making Graphical View render schematic connections from terminal-anchor route facts
instead of generic graph-like edges.

The openable proof project is:

- `../../examples/m24/sample-project`

The source files to open are:

- `../../examples/m24/sample-project/src/01-control-route.athena`
- `../../examples/m24/sample-project/src/02-terminal-strip-routes.athena`
- `../../examples/m24/sample-project/src/03-power-protection-load.athena`

The M23-vs-M24 comparison is recorded in
`m24-routing-acceptance-proof.md`.

## IDE Usage

Build the installed LSP host before manual IDE proof or Electron smoke:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
```

Then open the M24 sample project:

```powershell
yarn --cwd ide start:m24
```

In the IDE:

- open each M24 `.athena` source file
- confirm the editor reports no false syntax diagnostics
- open `Graphical View`
- confirm Graphical View follows the active `.athena` file
- confirm routes attach through visible terminal/port anchors rather than component-center fallback
- use the top information icon to inspect sheet and route information
- confirm the grid-backed canvas and transparent floating controls remain unchanged from M20-M23

## What M24 Proves

M24 proves:

- semantic `connect` facts can become electrical connection intent
- routing policy and port presentation policy can derive terminal anchors
- route facts can carry route points, labels, quality, endpoints, and anchor identities
- Theia can render route facts without inventing connection meaning
- Electron smoke can prove terminal-anchor route rendering in the actual product path

The product smoke emits `ATHENA_GRAPH_WORKBENCH_PROOF={...}`. For the M24 sample it must include a
`routeProof` payload with terminal anchors, orthogonal bend evidence, route labels, and no
`centerFallbackRouteIds`.

## Verification Path

Run checks sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM24SampleProjectCompilerTest"
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest.m24 sample project routing sources open without diagnostics"
yarn --cwd ide/theia-frontend test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd ide build
yarn --cwd ide start:smoke:m24
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

Observed product smoke evidence on 2026-07-19:

```text
routeCount=1
terminalCount=2
labelCount=1
routesWithTerminalAnchors=1
routesWithOrthogonalBends=1
centerFallbackRouteIds=[]
quality=SATISFIED
```

## Explicit Boundaries

M24 remains schematic topology routing only.

M24 does not support:

- physical wire routing
- cabinet routing
- harness, cable tray, or 3D installation routing
- full EPLAN parity
- route editing or route-hint syntax
- AI routing
- ELK, Graphviz, yFiles, or another generic router as Athena architecture
- full IEC/QElectroTech library breadth

## M23 Regression Expectations

M24 must preserve M23 system-scoped layout blocks:

```athena
layout schematic-sheet {
  place OperatorHMI1 near ControllerPLC1
  place TerminalBlockXT1 below ControllerPLC1
  align OperatorHMI1 aligned-with ControllerPLC1 axis vertical
  group OperatorHMI1 grouped-with ControllerPLC1
}
```

New `.athena` syntax still requires ANTLR4 and Tree-sitter updates together, plus compiler and LSP
proof. M24 adds no new route syntax.

