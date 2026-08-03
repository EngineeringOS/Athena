---
title: Athena M39 Architecture Spine
status: draft
created: 2026-08-01
updated: 2026-08-01
---

# M39 Architecture Spine

## Paradigm

Human source, concrete realities, paint-only renderer.

```text
Athena Source
  -> Engineering Reality
  -> Projection Reality
  -> Spatial Reality
  -> Presentation Reality
  -> Theia
```

Each step is a real ownership boundary. The compiler may derive richer facts, but normal source stays small.

A Reality is a coherent domain that owns one set of authoritative facts. Only one compiler subsystem may create or modify those facts. Downstream realities consume immutable snapshots through typed transformations. Realities never mutate each other directly.

## Inherited Invariants

- Athena source is the single source of truth.
- No `intent` in normal source.
- `to` is preferred; `->` is the same relation alias through one compiler path.
- SVG owns geometry references only, not engineering facts.
- XML is not runtime authority.
- Theia and SVG export paint final facts only.
- No Java2D or second renderer.
- No milestone names, `V0`/`V1`, stale proof/demo classes, or compatibility shells in production code.
- Pre-1.0 cleanup is allowed and expected.

## Decisions

### AD-1: Reality Graph Is A Pipeline, Not A Framework

**Binds:** model design, package boundaries, compiler stages.

**Rule:** Reality Graph means the four-reality pipeline and immutable snapshots. Do not create a generic graph framework, universal `Fact` base class, or empty wrapper models just to satisfy naming.

### AD-2: Engineering Reality Owns Engineering Truth

**Binds:** semantic model and relation lowering.

**Rule:** Engineering Reality represents what the engineered system is. It owns systems, devices, ports, signals, connections, networks, and engineering constraints. It does not own drawing coordinates, route geometry, or paint style.

### AD-3: Projection Reality Owns View Selection

**Binds:** projection model cleanup.

**Rule:** Projection Reality transforms Engineering Reality into a view-specific engineering document without coordinates or style. It may own drawing/view, sheet, occurrence, projection group, and semantic reading order. A region is allowed only as a logical document section, not a layout box. It must not own final coordinates, anchor positions, lanes, route start/end points, stroke, labels, or paint order.

### AD-4: Spatial Reality Owns Geometry Result

**Binds:** layout, route, anchor, and alignment code.

**Rule:** Spatial Reality owns placement, bounds, transformed anchor positions, alignment, and measured visual quality facts. Routing is a subsystem of Spatial Reality and owns lanes and routes. Spatial compiler is the only M39 owner of these facts.

### AD-5: Presentation Reality Owns Paint Facts

**Binds:** presentation model, Theia protocol, SVG export.

**Rule:** Presentation Reality owns visible shapes, connector paint facts, strokes, labels, markers, visibility, theme result, and paint order. It cannot change engineering truth, projection grouping, or spatial geometry.

### AD-6: Transformations Stay Thin

**Binds:** compiler APIs and tests.

**Rule:** M39 uses one typed interface equivalent to `RealityTransformation<InputReality, OutputReality>`. Do not add preserved/derived/discarded metadata in M39. Add it later only after repeated transformations prove the need.

### AD-7: Renderer Cannot Repair Truth

**Binds:** Theia and SVG export.

**Rule:** Theia and SVG export receive the same Presentation Document. They do not snap endpoints, infer topology, reroute, relabel, or apply domain rules.

### AD-8: Human Names Are Product Architecture

**Binds:** new M39 classes, docs, examples, stories.

**Rule:** New M39 naming must be direct and human-readable. Avoid vague terms such as `Evidence` for product concepts. Avoid long academic names when a short concrete name is clear.

## Pipeline

```text
source relation
  -> engineering connection
  -> projection occurrence/group/sheet
  -> spatial placement/anchor/route/lane
  -> presentation connector/label/style/order
  -> Theia paint
```

## Reality Ownership

### Engineering Reality

Owned facts:

- system;
- device or component occurrence;
- port;
- signal;
- connection or network;
- engineering constraint.

Authority:

- engineering compiler.

Validation examples:

- unknown relation;
- incompatible ports;
- missing domain relation contract;
- invalid grouped endpoint.

### Projection Reality

Owned facts:

- view/drawing;
- sheet;
- region;
- occurrence;
- projection group;
- semantic reading order.

Authority:

- projection compiler.

Validation examples:

- occurrence without engineering source;
- group without members;
- sheet without selected view.

### Spatial Reality

Owned facts:

- placement;
- bounds;
- anchor position;
- lane;
- route;
- alignment;
- measured overlap/crossing/twist facts.

Authority:

- spatial compiler.

Validation examples:

- route without placed anchors;
- connector endpoint not equal to anchor point;
- body intersection not recorded;
- lane references missing placement.

### Presentation Reality

Owned facts:

- shape;
- connector visual;
- stroke;
- marker;
- label;
- visibility;
- theme result;
- paint order.

Authority:

- presentation compiler.

Validation examples:

- connector without spatial route;
- label without target;
- missing stroke;
- paint item without order.

## Reality Transformations

M39 requires only the chain and one typed interface:

```kotlin
interface RealityTransformation<InputReality, OutputReality> {
    fun transform(input: InputReality): OutputReality
}
```

Required transformations:

- Engineering Reality to Projection Reality.
- Projection Reality to Spatial Reality.
- Spatial Reality to Presentation Reality.

The proof is simple: each stage accepts only the previous reality, emits only the next reality, and keeps source trace through the chain.

## Deferred

M40:

- diagram constructs;
- functional regions;
- rail/rung/trunk/bundle composition;
- sheet density;
- symbol grouping and reading-order quality.

M41+:

- professional routing;
- route optimization;
- label engine;
- controlled crossings;
- multi-sheet continuation;
- EPLAN/QET-level visual parity.
