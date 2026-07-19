# M25 Engineering Representation Usage

Updated: 2026-07-19

## Review Status

M25 is the engineering representation and presentation policy milestone. It follows M24 routing
fidelity by making Graphical View render governed symbol anatomy, terminal notation, and label facts
instead of only generic component boxes attached to improved routes.

The openable proof project is:

- `../../examples/m25/sample-project`

The source files to open are:

- `../../examples/m25/sample-project/src/01-professional-symbol-sheet.athena`
- `../../examples/m25/sample-project/src/02-terminal-labels-and-routes.athena`
- `../../examples/m25/sample-project/src/03-six-family-acceptance.athena`

The M24-vs-M25 comparison is recorded in
`m25-representation-acceptance-proof.md`.

## IDE Usage

Build the installed LSP host before manual IDE proof or Electron smoke:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
```

Then open the M25 sample project:

```powershell
yarn --cwd ide start:m25
```

In the IDE:

- open each M25 `.athena` source file
- confirm the editor reports no false syntax diagnostics
- open `Graphical View`
- confirm Graphical View follows the active `.athena` file
- confirm visible components no longer present only as generic boxes
- confirm terminal markers and terminal numbers are visible where the representation facts provide
  them
- confirm labels attach to governed label anchors rather than free canvas text
- select symbols, terminals, labels, and routes to inspect canonical identity where supported

## What M25 Proves

M25 proves:

- component knowledge can compile into presentation anatomy facts
- the `athena-industrial-control-v0` presentation policy can govern symbol and terminal notation
- labels are semantic presentation facts with roles and anchors
- M24 route facts can attach to M25 presentation terminals
- the accepted proof path has zero-fallback representation behavior
- Theia paints and inspects the facts without becoming symbol authority

## Explicit Boundaries

M25 has no QElectroTech import, no IEC completeness, and no EPLAN parity.

M25 does not support:

- QElectroTech `.elmt` ingestion
- full IEC, QElectroTech, manufacturer, or company-standard library breadth
- symbol authoring UI
- renderer-owned component, terminal, or label meaning
- route editing or route-hint syntax expansion
- physical cabinet routing, harness routing, cable tray routing, or 3D routing
- desktop-viewer, Compose, or deprecated KMP frontend changes

## Verification Path

Run checks sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM25SampleProjectCompilerTest"
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.PresentationModelDeriverTest"
yarn --cwd ide/theia-frontend test --test-name-pattern "Presentation IR occurrences"
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd ide build
yarn --cwd ide start:smoke:m24
yarn --cwd ide start:smoke:m25
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Latest Product Smoke Evidence

The accepted M25 product smoke opens `examples/m25/sample-project` through the Theia product path and
collects DOM evidence from Graphical View. The verified proof reported:

| Evidence | Result |
| --- | ---: |
| Governed route facts | 3 |
| Route terminal endpoints | 6 |
| Routes with terminal anchors | 3 |
| Routes with orthogonal bends | 3 |
| Center-fallback routes | 0 |
| Governed representation facts | 4 |
| Presentation terminal facts | 4 |
| Presentation label facts | 4 |
| Fallback representation symbols | 0 |

The represented M25 subjects were:

- `component:ActuatorY1`
- `component:ControllerPLC1`
- `component:PowerSupplyPS1`
- `component:TerminalBlockXT1`

The smoke also rechecks the accepted M20 canvas contract: transparent floating controls, grid-backed
sheet surface, page frame, and info popover close-on-whitespace behavior.

## Debugging Lesson

M25 initially failed product smoke even though compiler-focused tests passed. The root cause was a
Theia model-path gap: representation facts were attached to fallback graph nodes, but
`presentation.occurrences` nodes dropped `presentationRepresentation`, `presentationTerminals`, and
`presentationLabels`. Future presentation milestones must test the real Presentation IR occurrence
path because that is the path used by the customer-visible sheet.
