# M39 Addendum

## Why M39 Changed After Epic 1

Epic 1 did the right work: it removed compiler-shaped source and made relation statements readable.

After Epic 1, the old M39 plan was no longer correct. It still tried to solve the next problem through Diagram Grammar. That was healthier than raw routing, but still too narrow. The root issue is ownership:

```text
Engineering meaning mixed with projection choices.
Projection choices mixed with spatial coordinates.
Spatial coordinates mixed with presentation style.
Presentation facts mixed with renderer repair.
```

M39 now fixes those boundaries first.

## Reality Graph Meaning

Reality Graph is architecture vocabulary, not a new framework.

It means:

- one immutable snapshot per reality;
- one clear authority per snapshot;
- one transformation between snapshots;
- plain validation at each boundary.

It does not mean:

- graph database;
- universal `Fact` base class;
- empty `EngineeringReality` wrapper;
- new user-facing DSL;
- optimizer framework;
- another package runtime.

Concrete model roots should stay concrete. If `EngineeringDocument` already holds Engineering Reality well, keep it and clean it. Add `SpatialDocument` only if no current model owns spatial facts cleanly.

## Second Review Correction

The second M39 review found the remaining risk: M39 still sounded like it wanted to build a large architecture instead of proving one.

Correction:

- M39 proves four realities only.
- Epic 2 is Reality Foundations.
- Epic 3 is Reality Transformations.
- Transformations need one typed interface only.
- Metadata such as preserved, derived, discarded, and validation lists is deferred.
- Projection Reality is purpose-first: view-specific engineering document, no coordinates, no style.
- Routing is named as a subsystem of Spatial Reality.

## Reality Boundaries

Engineering Reality owns what is true in the engineering system:

- system;
- device/component occurrence;
- port;
- signal;
- connection/network;
- engineering constraint.

Projection Reality owns how a reality is selected for a view:

- drawing/view;
- sheet;
- region;
- occurrence;
- projection group;
- semantic reading order.

Projection Reality must not own final coordinates or routes.

Spatial Reality owns geometry results:

- placement;
- bounds;
- transformed anchor position;
- lane;
- route;
- alignment;
- measured overlap/crossing/twist facts.

Presentation Reality owns paintable result:

- shape;
- connector visual;
- stroke;
- label;
- marker;
- visibility;
- theme result;
- paint order.

Presentation Reality must not change engineering, projection, or spatial truth.

## `to` And `->`

`to` is the preferred human spelling.

`->` is allowed because the user explicitly approved it as the same relation expression. This is not a general compatibility policy. Both spellings must lower through one parser/AST/semantic path and produce identical diagnostics and trace.

## `intent` Remains Out

Normal source must not use `intent`.

Connection properties may exist only as simple optional properties:

```athena
power Supply.L1 to Q1.1 {
  style: "dot"
  label: "main feed"
  position: "right top"
}
```

These are presentation hints. They do not own engineering truth, route authority, or layout algorithms.

## Diagram Grammar Handoff

Diagram Grammar is not deleted as an idea. It moves behind the Reality Graph.

M40 can introduce diagram constructs after M39 makes reality ownership clean:

- rail;
- rung;
- branch;
- terminal strip;
- bus;
- functional region;
- contact group;
- coil group;
- wire bundle.

This ordering matters. If M39 adds those constructs before ownership is clean, they will become another grammar monster.

## Visual Honesty

M39 does not promise professional drawings.

M39 promises:

- facts have one owner;
- endpoints stay exact;
- route and style are derived in the right reality;
- Theia paints only final facts;
- screenshots are honest enough to show remaining debt.

Professional composition belongs to M40. Professional routing belongs to M41+.
