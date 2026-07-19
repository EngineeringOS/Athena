# M24 Routing Acceptance Proof

Updated: 2026-07-19

## Purpose

M24 proves a narrow routing-fidelity step after M23.

M23 made layout intent real `.athena` language. M24 makes rendered schematic connections consume
terminal-anchor route facts instead of looking like generic graph edges. The proof is visible in
Theia through `../../examples/m24/sample-project`.

## Directional Reference

The visual direction is:

`../../draft/screenshort/coffret_cordons_chauffants.png`

That image is used only as inspiration for ordered, coordinated schematic routes: wires should fold
cleanly, stay out of component bodies, and attach to terminal areas in readable order. M24 does not
claim full EPLAN parity, cabinet routing parity, physical wire routing, harness routing, cable tray
routing, or 3D installation routing.

## M23 Baseline

M23 accepted this kind of source intent:

```athena
layout schematic-sheet {
  place OperatorHMI1 near ControllerPLC1
  place TerminalBlockXT1 below ControllerPLC1
  align OperatorHMI1 aligned-with ControllerPLC1 axis vertical
  group OperatorHMI1 grouped-with ControllerPLC1
}
```

That proved parser, compiler, LSP, Tree-sitter, Graph Workbench, and source mutation admission for
layout hints. It did not prove professional wire routing.

## M24 Visible Change

In `../../examples/m24/sample-project`, the visible acceptance change is:

| Area | M23 baseline | M24 proof |
| --- | --- | --- |
| Connection geometry | graph-like connection edges could imply component-center attachment | route facts attach to terminal anchors and port-side stubs |
| Directionality | layout hints positioned subjects but did not communicate input/output route discipline | route facts carry source/target port identity and policy-derived anchor sides |
| Terminal strip case | no ordered route bundle proof | terminal-strip routes use ordered route facts for PLC-to-terminal-to-load scenarios |
| Readability | route quality was not inspectable as routing output | route facts can carry labels, crossings, lane ids, and quality information |
| Authority | layout intent was source-owned, but route rendering could still look graph-like | renderer consumes governed route facts and does not invent connection meaning |

## Theia Acceptance Path

Open the sample:

```powershell
yarn --cwd ide start:m24
```

Then check:

- `src/01-control-route.athena` shows PLC-to-HMI control routing.
- `src/02-terminal-strip-routes.athena` shows PLC-to-terminal-strip-to-load route ordering.
- `src/03-power-protection-load.athena` shows a 24V power/protection/load route.
- Graphical View follows the active `.athena` file.
- Routes visually attach through route facts rather than renderer-owned component-center fallback.
- Route inspection identifies governed source connection or port identity where available.

## Explicit Deferrals

M24 does not claim:

- full EPLAN parity
- cabinet routing parity
- physical wire routing
- harness, cable tray, or 3D routing
- route editing or route-hint syntax
- ELK, Graphviz, yFiles, or any generic router as the Athena architecture
- full IEC/QElectroTech symbol-library breadth

