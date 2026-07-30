# M36 PRD Addendum

## Technical Boundary Notes

This addendum records technical direction that should inform architecture and implementation
planning without becoming a second authority model.

### SVG bridge

Recommended pattern:

```xml
<path id="power-in" data-athena-ref="anchor:power-in" />
```

```athena
element ABB.PFEA112.Cabinet {
  resource "abb_pfea112_ip20.svg"

  anchor powerIn ref "anchor:power-in" {
    port power.L1
    direction input
    signal electrical.ac
  }
}
```

The SVG reference identifies a geometry location. The Athena declaration supplies the engineering
meaning. SVG-derived bounds, transforms, groups, and geometry references may enter the geometry
index. Manufacturer identity, article number, voltage, signal meaning, compatibility, lifecycle,
and parameters may not enter the authority model through SVG.

### ELK boundary

ELK is useful as a source of layout and routing algorithms, especially for layered graphs, ports,
compound nodes, edge labels, orthogonal edges, and crossing reduction. It is not a replacement for
Athena's semantic model.

The adapter should construct a temporary planner graph from Athena-owned facts:

```text
Athena Connectable Entity/Port/Connection facts
Athena Element/Anchor/Geometry facts
Physical Installation constraints
        -> temporary planner graph
        -> planner proposal
        -> Athena normalization
        -> validation and quality diagnostics
        -> PlacementFacts + RouteFacts
```

Planner objects must not cross the LSP or renderer protocol. The planner must not write `.athena`,
assign Port identity, classify SVG geometry, or persist coordinates as hidden source truth.

For Cabinet, generic graph layout is not enough. Rails, ducts, mounting targets, clearances, and
physical route channels require an Athena-owned physical policy around the planner. A planner may
propose a placement or path only when it satisfies the physical policy.

### Engineering lowering

M36 should formalize this compiler progression:

```text
.athena source
    -> Engineering AST
    -> Engineering IR
    -> transient Connection IR
    -> transient Layout Graph
    -> planner proposal
    -> Athena PlacementFacts and RouteFacts
    -> Graphic Primitive IR
```

Connection IR and Layout Graph are disposable, snapshot-bound compiler forms. They must not be
persisted as project truth, edited independently, or interpreted by the renderer.

### Constraint ownership

```text
Semantic Constraint       -> engineering model
Representation Constraint -> Element and Anchor model
Physical Constraint       -> Physical Installation Model
Layout Preference         -> authored projection intent or planner policy
```

Every lowered constraint should retain owner, source provenance, strength, and diagnostic identity.
Planner optimization may trade preferred Layout Preferences but cannot weaken required Semantic,
Representation, or Physical Constraints.

### Connection model

Keep these concepts separate:

```text
Connection Network  = semantic topology
Route Intent         = realization requirements and preferences
Route Bundle         = derived readability/physical grouping
Route Fact           = derived geometric realization
```

A crossing is never a join. A join requires an explicit semantic Network/junction fact.

Every Route Fact should prove this chain:

```text
Connection
  -> source/target Port
  -> source/target Anchor
  -> source/target occurrence
  -> Route Intent
  -> selected channels
  -> planner and compiler snapshot
  -> route quality and source provenance
```

### Recommended routing costs

The first deterministic planner may score valid candidates by:

1. hard constraint violations: reject;
2. route/body intersection: reject;
3. invalid channel or clearance: reject;
4. semantic/physical priority;
5. crossings;
6. bends;
7. channel changes;
8. label collisions;
9. total route length;
10. stable lexical tie-breaker.

Exact weights belong in architecture and tests, not in the public language surface.

### Migration and cleanup

M36 is unreleased work. Legacy XML runtime paths, renderer-owned connections, raw SVG metadata
authority, and mandatory authored channel sequences may be removed or redesigned directly. No public
compatibility layer is required.

## Handoff To M37

- remote resource URI and package registry resolution;
- expansion of existing Component knowledge into a full vendor product system;
- domain standards/import mappings such as AML, IEC catalogs, and ECLASS;
- multi-view reuse of the stabilized connection/layout contracts;
- AI proposals over validated component, placement, and route facts.
