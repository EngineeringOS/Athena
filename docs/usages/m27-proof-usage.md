# Athena M27 Proof Usage

M27 proves professional sheet visual acceptance and the first Semantic Spatial Compiler foundation
without changing `.athena` source syntax.

## Open The Sample

Sample project:

```text
examples/m27/sample-project
```

The reviewer path is the Athena Theia IDE Graphical View. The M27 sample opens through the normal
project workflow and uses the active Theia frontend only.

Convenience launch helper:

```powershell
_bmad-output\implementation-artifacts\m27\ide-start-m27.cmd
```

Debug helper:

```powershell
_bmad-output\implementation-artifacts\m27\ide-debug-m27.cmd
```

## Verification Commands

Run Gradle commands sequentially on Windows.

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test
yarn --cwd ide/theia-frontend test
yarn --cwd ide build
yarn --cwd ide start:smoke:m27
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## What M27 Proves

- Professional sheet surface evidence remains fact-driven: frame, zones, title block metadata, grid,
  compact text, and sheet selector metadata are transported to Theia.
- Live SVG scene bounds are based on active filtered content, not the A3 publication frame. A3
  remains sheet chrome metadata.
- Verbose route endpoint strings are not visible by default on the canvas. They remain available
  through hover, selection, inspector, and proof payloads.
- Semantic Spatial Intent is represented by a narrow `kernel/spatial-model` contract limited to 2D
  electrical schematic projection.
- Routing backend support is a boundary, not a backend adoption. Athena v0 remains the accepted
  proof backend and normalizes output into Athena `RouteFact` objects.
- Accepted M27 routes use terminal anchors, orthogonal bends, satisfied route quality, zero center
  fallback ids, and zero visual route/body intersections in smoke proof.

## Current Proof Artifact

The product smoke writes:

```text
_bmad-output/implementation-artifacts/m27/proofs/m27-graph-workbench-smoke.png
```

Fresh proof values from the accepted run included:

- active first-sheet SVG viewBox: `0 36 624 124`
- active first-sheet route count: `2`
- visible route label count: `0`
- visible verbose route label count: `0`
- center fallback route ids: `[]`
- route/body intersection count: `0`
- route quality on accepted routes: `SATISFIED`
- sheet selector option count: `3`

## M26 Versus M27

M26 proved semantic document projection:

```text
semantic model -> document projection -> sheet views -> cross-reference facts -> Presentation IR
```

M27 improves the visible and spatial trust layer:

```text
semantic model -> semantic spatial intent -> layout/routing facts -> professional Presentation IR -> Theia
```

M27 does not make sheets, pages, SVG geometry, or canvas state authoritative. The `.athena` source
and compiler-owned semantic model remain upstream truth.

## QElectroTech Reference Boundary

QElectroTech screenshots are qualitative references for professional density, frame, linework, and
visual trust. M27 does not import QElectroTech elements, copy QET data models, require pixel-perfect
similarity, or use QET as runtime authority.

## Deferred

M27 does not include:

- PDF export or print fidelity
- production ELK/libavoid/yFiles backend adoption
- QElectroTech `.elmt` import
- standards-complete IEC/company style packs
- cabinet, harness, 3D, or physical routing
- auto-connection source mutation acceptance
- new `.athena` syntax
