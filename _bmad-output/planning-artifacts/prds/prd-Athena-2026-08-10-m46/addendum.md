# M46 Addendum

## EPLAN Lessons Adopted

EPLAN correctly treats a connection as more than line paint: it owns endpoints, source/target,
connection type, potential/signal inheritance, physical properties, placed/unplaced state, target order,
and reportable identity. EPLAN also proves useful workflows through its Connections Navigator, connection
definition points, Smart Connect, and delayed connection generation.

Athena keeps these product lessons but reverses authority:

```text
EPLAN: graphic/autoconnect line -> generated connection data
Athena: source connection meaning -> Connection IR -> planned graphic projection
```

## Four Authority Layers

```text
Athena source
  Engineering Connection / Engineering Net / endpoint role / kind / physical facts
        |
Compiler-owned Connection IR
  normalized graph / resolved specifications / topology / validation / provenance
        |
Connection projection and Route Plan
  orthogonal segments / junctions / crossings / interruption and label anchors
        |
Theia / Konva / SVG
  disposable paint and user intent capture
```

## Connection Property Precedence

M46 adopts deterministic scoped specifications instead of EPLAN's graphic definition-point authority.

```text
project defaults
  < potential or signal
  < Engineering Net
  < Engineering Connection
```

Package metadata may constrain compatible values but cannot author project connection facts. A visible
Connection Annotation may display resolved data and provide an edit entry point; it does not own the data.

## Net And Topology

An Engineering Net is first-class. It is not serialized as an arbitrary chain of binary connections.
Topology Operators are derived projection facts with explicit priority and stable identity. A Branch
operator controls visual continuation order only. It cannot add/remove Net members.

Connection junction grammar:

- joined topology: one tiny filled junction marker;
- unconnected crossing: no junction marker, with one deterministic bridge/gap rule when needed;
- branch/shared trunk: one segment painted once, branches ordered deterministically;
- interruption: paired stable anchors and cross-reference data, not a severed semantic identity;
- angle: Route Plan bend, not a Symbol or engineering object.

## Route Quality

Planner optimizes under hard semantic and spatial constraints in this order:

1. preserve exact endpoint and Net topology;
2. avoid symbol bounds, frame, rulers, and declared keep-outs;
3. avoid ambiguous overlaps and false junctions;
4. minimize crossings;
5. minimize bends and total route length;
6. align shared trunks and parallel conductors;
7. keep labels collision-free and close to their subject.

These priorities are explicit compiler data/configuration, not renderer heuristics. M46 should deepen the
existing spatial route compiler before introducing a new runtime dependency. ELK remains a reference for
layout ideas; no ELK model becomes Athena authority.

## Visual Grammar

`draft/screenshort/equipement_d'un_volet_roulant.png` remains canonical class reference. Repository Golden
Rule overrides incidental details: Athena keeps no bottom title table and hides construction grid by
default.

- route stroke: approximately one screen pixel, regular black by default;
- junction: tiny professional dot, never a large Port ring;
- labels: compact regular-weight black text, only explicitly selected engineering data;
- routes: orthogonal, disciplined, no giant detour rectangles or oversized arrows;
- white space: protected as part of readability;
- hit targets: generous but invisible.

## Pre-1.0 Replacement Rule

M46 replaces route-only contracts that cannot carry semantic Connection IR and deletes retired duplicate
logic/tests/examples. No adapter, deprecated alias, V1/V2 bridge, or old milestone fixture remains.
