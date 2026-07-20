# M27 Retrospective - 2026-07-20

## Outcome

M27 established the professional sheet visual acceptance path and the first Semantic Spatial
Compiler foundation:

- compact, centered Theia Graphical View proof for the M27 sample
- fact-driven sheet surface evidence
- `kernel/spatial-model` semantic spatial intent contract
- routing backend boundary with Athena v0 as accepted proof backend
- component-avoidance quality degradation when avoidance cannot be satisfied
- structured visual, DOM, and route proof assertions

## What Went Wrong

The largest failure was the graph-view sizing regression. A3 publication frame size was incorrectly
treated as live SVG scene size. That produced a huge `viewBox="0 0 1680 1188"` while real content was
small and near the top-left.

The correction was to separate:

```text
publication sheet frame != live SVG scene bounds != component representation bounds != DOM viewport bounds
```

This is recorded in:

```text
_bmad-output/implementation-artifacts/m27/M27-GRAPH-VIEW-FAILURE-NOTE.md
```

## Lessons

1. Visual stories require live DOM/SVG and screenshot proof, not only data-model tests.
2. Component size must come from Presentation IR and route facts, not guessed publication frame size.
3. A3/A4 metadata belongs to sheet chrome, not compact component SVG viewBox sizing.
4. Projection scope must be semantic. Source files, support catalogs, and sheets are not the same
   concept.
5. Route quality must be a fact. A visually accepted route should be `SATISFIED`; unavoidable
   intersections should be explicitly `DEGRADED`.
6. Backend routing must stay below Athena-owned normalized route facts.

## Final Graph-View Recap

The late M27 graph-view failures were not one bug. They were a stack of boundary mistakes:

- `.athena` files were mentally confused with generated document sheet views.
- A3 publication sheet size leaked into the live SVG scene `viewBox`.
- Off-sheet/reference render elements could affect visible bounds.
- Sheet and frame helper chrome was visible as gray wrapper borders.
- The sheet selector was tied too tightly to the active projection mode and disappeared after
  switching to cabinet/wiring.

The corrected boundary is now:

```text
.athena source files -> semantic graph -> generated projection sheets -> Presentation IR -> SVG scene
```

The sheet selector lists generated projection sheets, not source files. For the M27 sample this is
expected to be:

```text
2 .athena files -> 3 documentation sheet views
```

Fresh M27 smoke evidence after the fix:

- live first-sheet SVG viewBox: `0 12 678 148`, not `0 0 1680 1188`
- live first-sheet centering: `sheetCenterDeltaX: 1`, `sheetCenterDeltaY: 0`
- sheet and sheet-frame border widths: all `0px`
- sheet and sheet-frame box shadows: `none`
- sheet and sheet-frame backgrounds: transparent
- sheet selector option count: `3`
- selector remains visible after switching to `cabinet`
- selector restores `documentation/sheet/01-power-distribution`
- visible verbose route labels: `0`
- route body intersections: `0`
- center fallback routes: none

This proves the coded acceptance checks and current screenshot state. It does not replace human
visual acceptance in the IDE for final taste, density, and industrial drawing quality.

## Verification Evidence

- `.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test` - passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` - passed.
- `yarn --cwd ide/theia-frontend test` - passed, 133/133 tests.
- `yarn --cwd ide build` - passed.
- `yarn --cwd ide start:smoke:m27` - passed.
- `yarn --cwd ide start:smoke:m27` - passed again after the graph-view boundary fixes above.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - passed during story closeouts.

## Known Limitations

- M27 does not implement production ELK/libavoid/yFiles backend adoption.
- M27 does not implement auto-connection source mutation acceptance.
- M27 does not introduce new `.athena` syntax.
- M27 does not provide standards-complete IEC/company presentation packs.
- M27 does not provide PDF export, print, cabinet routing, harness routing, or 3D routing.
- The visual style is professional acceptance foundation, not QElectroTech pixel parity.

## Cleanup Decisions

See:

```text
_bmad-output/implementation-artifacts/m27/m27-stale-artifact-retention-ledger.md
```
