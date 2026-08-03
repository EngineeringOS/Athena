---
title: Athena M39 - Human DSL And Four Reality Proof
status: draft
created: 2026-08-01
updated: 2026-08-01
---

# Athena M39 PRD

## Purpose

M39 keeps Epic 1 as finished Human DSL work, then changes direction.

Epic 1 made source readable:

```athena
power Supply.L1 to Breaker.1
control Drive.DO1 to Terminal.1
earth PE to [Drive.PE, Motor.PE]
```

The rest of M39 must not keep expanding syntax. M39 now establishes Athena's first Reality Graph:

```text
Engineering Reality
  -> Projection Reality
  -> Spatial Reality
  -> Presentation Reality
  -> Theia renderer
```

The goal is proof, not grand redesign. M39 establishes four canonical realities, implements the transformation chain between them, and proves Theia renders only Presentation Reality.

A Reality is a coherent domain that owns one set of authoritative facts. Only one compiler subsystem may create or modify those facts. Downstream realities consume immutable snapshots through typed transformations. Realities never mutate each other directly.

## Product Thesis

Engineer writes meaning. Compiler creates facts. Theia paints facts.

Athena source remains the single source of truth. The compiler may become richer, but normal source must stay human-first, natural, concrete, and small.

## What Changes After Epic 1

The prior M39 plan treated Diagram Grammar as the main architecture. That was still too close to "better drawing rules." The corrected M39 treats drawing as the result of reality transformations.

M39 now focuses on:

- Engineering Reality: what the engineered system is.
- Projection Reality: which engineering facts appear in a view-specific document, without coordinates or style.
- Spatial Reality: where projected things are placed and how connection geometry is routed.
- Presentation Reality: how final facts are painted.

M39 does not create a generic graph framework, a universal `Fact` base type, empty wrapper classes, or another user-facing DSL.

## Scope

M39 must deliver:

- completed Epic 1 Human DSL retained as the source surface;
- `to` as preferred relation spelling and `->` as the same relation alias through one compiler path;
- no `intent` blocks in normal source;
- four concrete Reality ownership boundaries;
- one thin typed transformation interface between realities;
- first Spatial compiler that owns placement, bounds, anchor positions, lanes, routes, and alignment;
- first Presentation compiler that owns visual facts only;
- Theia paint-only proof;
- M39-local example and screenshot proof.

M39 must not deliver:

- EPLAN-quality drawing;
- AI auto-layout;
- general constraint solving;
- Java2D or another renderer;
- XML runtime authority;
- SVG semantic authority;
- compatibility shims beyond the explicitly approved `to` / `->` alias;
- stale Diagram Grammar stories from the abandoned plan.

## Functional Requirements

### Epic 1: Human DSL

**FR-1:** Normal Athena source shall use one relation statement shape: `<relation> <source> to <target-or-group>`.

**FR-2:** `->` shall be accepted as an exact alias for `to` and shall lower through the same AST, semantic, trace, and diagnostic path.

**FR-3:** Relation words shall be declared by the active engineering domain package. The electrical proof package shall provide `power`, `control`, and `earth`; Athena kernel shall not hardcode them as universal language keywords.

**FR-4:** Unknown relation words shall fail with a plain diagnostic that identifies the active domain package and its available relations.

**FR-5:** Normal M39 example source shall not use user-facing `intent` blocks for connections.

**FR-6:** Normal M39 example source shall not repeat route priority, separation, channel, label policy, owner, or strength on every connection.

**FR-7:** Engineering relation contracts shall own engineering class, compatibility, medium, separation, and physical restrictions. Drawing defaults shall not change engineering truth.

**FR-8:** One-to-many statements such as `earth A to [B, C]` shall remain one traceable engineering relationship until a later reality derives branch facts.

### Epic 2: Reality Foundations

**FR-9:** M39 shall define Reality plainly: a coherent domain that owns one set of authoritative facts, is created or modified by one compiler subsystem, and is consumed downstream as an immutable snapshot.

**FR-10:** Reality Graph shall mean the ordered four-reality pipeline, not a graph database, generic framework, universal `Fact` base type, or empty wrapper model.

**FR-11:** Engineering Reality shall use the existing engineering model root where possible. Its purpose is to represent what the engineered system is. It owns systems, devices, ports, signals, connections, networks, and engineering constraints.

**FR-12:** Projection Reality purpose is to transform Engineering Reality into a view-specific engineering document without introducing spatial coordinates or presentation styling. It may own view, sheet, occurrence, projection group, and reading order. A region is allowed only as a logical document section, not a layout box.

**FR-13:** Projection Reality shall not own coordinates, anchor positions, lanes, route geometry, stroke, label styling, paint order, or renderer state.

**FR-14:** Spatial Reality purpose is to turn view-specific projection facts into geometry. It owns placement, bounds, transformed anchor positions, alignment, and measured overlaps/crossings. Routing is a subsystem of Spatial Reality and owns lanes and routes.

**FR-15:** Presentation Reality purpose is to turn spatial facts into paintable drawing facts. It owns visible shape facts, connector paint facts, stroke, label, visibility, theme result, and paint order.

**FR-16:** Presentation Reality shall not own engineering meaning, projection grouping, route authority, or semantic repair logic.

**FR-17:** Every reality shall declare purpose, owned facts, authority, identity rules, and validation failures in plain product terms.

**FR-18:** M39 shall remove or refactor stale model ownership where projection models currently own spatial facts.

**FR-19:** M39 shall avoid names that hide meaning from humans, including stale milestone names, `V0`/`V1`, `ProfessionalControlDrawing`, and vague `Evidence` naming in new M39 artifacts.

### Epic 3: Reality Transformations

**FR-20:** M39 shall introduce one thin typed transformation interface, equivalent in intent to `RealityTransformation<InputReality, OutputReality>`.

**FR-21:** M39 shall not introduce transformation metadata for preserved, derived, discarded, and validation facts. That metadata is deferred until repeated transformations prove it is needed.

**FR-22:** Engineering to Projection transformation shall produce Projection Reality from Engineering Reality through the typed interface.

**FR-23:** Projection to Spatial transformation shall produce Spatial Reality from Projection Reality through the typed interface.

**FR-24:** Spatial to Presentation transformation shall produce Presentation Reality from Spatial Reality through the typed interface.

**FR-25:** The end-to-end chain shall preserve source trace and identity across the four realities without using renderer repair.

### Epic 4: Spatial Compiler

**FR-26:** Spatial compiler shall be the only M39 owner of placement, bounds, anchor positions, lanes, routes, and alignment.

**FR-27:** Projection models shall not compute or store final route start/end points.

**FR-28:** Spatial compiler shall compute connector endpoints from placed anchor positions, not from approximate node bounds.

**FR-29:** Spatial compiler shall keep M38 endpoint trust: visible connector first and last points must equal placed anchor points.

**FR-30:** Spatial compiler shall produce honest quality measures for overlap, body intersection, crossing count, twist, and label pressure.

**FR-31:** Spatial compiler may provide simple alignment and lane derivation, but M39 shall not claim professional routing or general optimization. Routing remains a Spatial subsystem.

**FR-32:** Spatial compiler shall fail before Presentation Reality when required anchor, placement, route, or lane facts are missing.

**FR-33:** Spatial compiler shall not depend on Theia, SVG export, or frontend repair.

### Epic 5: Presentation Compiler

**FR-34:** Presentation compiler shall be the only M39 owner of visual styling, label facts, visibility facts, theme/profile result, and paint order.

**FR-35:** Line style, weight, color, marker, label, and label placement shall be dynamic presentation facts derived from relation contracts and drawing defaults.

**FR-36:** User source may carry simple relation properties when needed, such as `style`, `label`, or high-level label position, but those properties shall be optional and shall not replace compiler ownership.

**FR-37:** Presentation compiler shall not repair routes, infer endpoints, or change engineering meaning.

**FR-38:** Presentation compiler shall emit one atomic Presentation Document consumed by Theia and SVG export.

**FR-39:** Presentation compiler shall use human-readable model names in new M39 code and docs.

**FR-40:** Presentation compiler shall reject incomplete visual facts with plain diagnostics instead of silent fallback.

### Epic 6: Renderer And Proof

**FR-41:** Theia shall remain paint-only and domain-neutral.

**FR-42:** Theia shall not snap endpoints, reroute connectors, infer topology, relabel, or interpret engineering domains.

**FR-43:** SVG export shall consume the same Presentation Reality facts as Theia.

**FR-44:** M39 shall provide a dedicated M39 example and shall not share M36, M37, or M38 examples.

**FR-45:** M39 proof shall rebuild affected kernel, LSP, frontend, and product surfaces before final screenshots.

**FR-46:** M39 screenshots shall be stored under `_bmad-output/implementation-artifacts/m39/screenshots`.

**FR-47:** M39 closure shall record what improved, what remains visually poor, and which failures move to M40+.

## Non-Functional Requirements

**NFR-1:** K.I.S.S. is binding: human-first names, direct concepts, small source language, no compiler-shaped authoring.

**NFR-2:** Athena source must be LLM-friendly: regular forms, explicit names, low ambiguity, and low boilerplate.

**NFR-3:** Clean architecture wins over compatibility. Athena is pre-public.

**NFR-4:** No stale source, docs, examples, tests, or compatibility branches may remain if they violate the current M39 model.

**NFR-5:** XML is out of the active runtime path unless explicitly used as temporary import/input.

**NFR-6:** SVG may provide package-local geometry and stable geometry references. SVG must not own engineering facts.

**NFR-7:** Visual claims must be honest. M39 may claim cleaner reality ownership and stronger proof, not professional drawing parity.

## User-Facing Syntax Target

Preferred:

```athena
system RollingShutter {
  device Supply : PowerSupply
  device Q1 : Breaker
  device KM1 : Contactor
  device Motor : Motor
  device EarthBar : ProtectiveEarthBar

  power Supply.L1 to Q1.1
  power Q1.2 to KM1.1
  power KM1.2 to Motor.U

  control S1.2 to KM1.A1
  earth EarthBar.PE to [Motor.PE, Cabinet.PE]

  drawing IECControl
}
```

Allowed alias:

```athena
power Supply.L1 -> Q1.1
```

Both spellings mean the same relation and must produce the same compiled result.

Optional relation properties stay small:

```athena
power Supply.L1 to Q1.1 {
  style: "dot"
  label: "main feed"
  position: "right top"
}
```

These properties are presentation hints, not engineering truth.

## Acceptance Criteria

1. Epic 1 remains complete and M39 examples use human relation syntax without `intent`.
2. `to` and `->` both compile through one path and produce the same engineering relationship.
3. PRD, architecture, epics, sprint status, examples, and stories no longer describe stale Diagram Grammar-first Epic 2+ work.
4. Engineering, Projection, Spatial, and Presentation realities have explicit purpose, owned facts, authority, identity, and validation.
5. Projection Reality no longer owns final coordinates, anchor positions, or routes.
6. Spatial Reality owns placement, bounds, anchor positions, lanes, routes, and geometry quality measures.
7. Presentation Reality owns visual styling, labels, visibility, theme result, and paint order.
8. Reality transformations use one thin typed interface and prove the chain works without transformation metadata.
9. Theia and SVG export consume the same Presentation Document and perform no repair.
10. Final M39 E2E proof captures screenshots from the M39 example under M39 artifacts.
11. M39 retrospective honestly records remaining visual debt.

## Handoff From M38

Keep:

- exact endpoint equality;
- source trace from visible connector to source relation;
- RepresentationDefinition as intrinsic geometry authority;
- GraphicOccurrence as placed geometry authority until replaced by the clean Spatial model;
- atomic Presentation Document;
- Theia paint-only boundary.

Change:

- M39 Epic 2 onward is Reality Graph, not Diagram Grammar-first;
- projection models stop owning spatial facts;
- route/style defaults move to Spatial and Presentation ownership;
- stale names and stale compatibility paths are removed;
- `->` remains only as the explicitly approved relation alias.

## Handoff To M40+

M40 should build real composition quality on top of the Reality Graph:

- diagram constructs and engineering reading order;
- functional regions;
- symbol grouping;
- contact/coil/terminal organization;
- rail/rung/trunk/bundle composition;
- better sheet density.

M41+ should build professional routing:

- lane optimization;
- bend minimization;
- crossing minimization;
- bundle/trunk routing;
- multi-sheet continuation;
- label engine;
- visual polish toward EPLAN/QET-level readability.
