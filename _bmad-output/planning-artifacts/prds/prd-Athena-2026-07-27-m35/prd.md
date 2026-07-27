---
title: Athena M35 - Physical Installation Model And Cabinet Composition Foundation
status: final
created: '2026-07-27'
updated: '2026-07-27'
---

# Athena M35 PRD - Physical Installation Model And Cabinet Composition Foundation

## Executive Summary

M35 turns the M34 representation authority proof into the first customer-demo-quality physical
installation drawing, with Cabinet as the first focused projection.
M34 corrected the architecture by making Athena source the single metadata authority for symbols,
elements, profiles, bindings, anchors, compatibility, and package-local resources. It also proved
that the IDE can render a governed drawing path, but the result still does not match the target
QElectroTech-style Cabinet layout.

M35 fixes the remaining gap at the correct layer:

```text
Athena project source
  -> package/path verified source units
  -> semantic model and resolved package graph
  -> governed physical installation constraints
  -> PhysicalInstallationIR v0
  -> Cabinet composition projection
  -> representation binding and package-local symbol/element resources
  -> editable graphic occurrence trace
  -> Graphic Primitive IR
  -> paint-only Cabinet renderer
```

M35 does not add another broad view. Cabinet is the only visible product surface, but it is treated
as the first consumer of a more general Physical Installation Model. Documentation, schematic, and
wiring remain hidden or compatibility-only until this focused path reaches a professional baseline.

The core boundary is:

```text
Engineering truth
  -> physical projection intent
  -> physical constraints and mounting topology
  -> compiled Cabinet composition
  -> rendering coordinates
```

Cabinet is a compiled physical projection of engineering truth. It is not the place where
engineering truth, vendor product identity, or package metadata is created.

The intended hierarchy is:

```text
Engineering Semantic Model
  -> Physical Installation Model
      -> Cabinet Layout Projection
      -> Panel Projection
      -> Machine Mounting Projection
      -> Field Installation Projection
```

M35 implements only the Cabinet projection slice.

## Product Problem

Athena is architecturally close to the target, but the current product output still looks weak
because it lacks a true Physical Installation Model, a Cabinet composition projection, and a scalable
package resource model.
Specific gaps:

- project source package names do not consistently follow filesystem hierarchy;
- complex vendor SVG-backed elements are not covered strongly enough;
- representation resources are local sample files, not dependency-style package resources;
- Cabinet layout still behaves too much like flat drawing order, not physical mounting;
- component mounting constraints are not yet explicit enough to answer whether a product fits;
- rendered graphic occurrences are not yet a first-class editable trace back to governed `.athena`
  source.

This creates two risks. First, the visual output cannot convincingly demo to a customer. Second,
standard/vendor/user libraries will become hard to maintain if package structure and resource
authority remain loose.

## Goals

- Deliver one professional Cabinet physical installation drawing that can be demoed as Athena's
  focused M35 product proof.
- Make `.athena` package declarations match source filesystem hierarchy.
- Treat standard/vendor/user representation libraries as package hierarchies with package-local
  resources.
- Cover complex SVG-backed vendor elements where SVG exposes geometry references and Athena owns
  metadata.
- Model Cabinet enclosure, rails, ducts, mounted components, terminal strips, labels, and route
  channels as typed composition facts, not renderer guesses.
- Define a minimal Physical Installation Contract so installation constraints do not leak into
  visual Elements or prematurely create an Engineering Component System.
- Route conductors through governed route channels and element anchors.
- Preserve Athena source as the single metadata authority.
- Add source-to-graphic and graphic-to-source traceability for future governed editing.
- Add hard screenshot and structured proof gates against the approved Cabinet reference.
- Separate physical intent, physical constraints, and rendering coordinates.
- Keep filesystem namespace separate from engineering product identity.
- Introduce minimal reproducible dependency-lock evidence for representation package resources.

## Non-Goals

- No Documentation/Schematic/Wiring polish.
- No full EPLAN/QET equivalent engine.
- No QET runtime dependency or QET schema authority.
- No XML package manifests.
- No SVG metadata authority for identity, version, profile, binding, project ports, or engineering
  facts.
- No symbol editor UI.
- No full package marketplace.
- No Engineering Component System, component catalog language, or new physical-installation package
  kind.
- No Auto Layout AI.
- No product parameters, catalog identity, lifecycle, datasheets, or validation rules inside visual
  Elements.
- No direct renderer-side mutation of Presentation IR, geometry, or raw SVG.
- No compatibility requirement for unreleased M33/M34 XML paths.

## Authority Model

| Concern | Authority |
| --- | --- |
| Project device instances, ports, connections, layout intent | Project `.athena` source |
| Reusable symbol/element metadata | Representation `.athena` package source |
| Heavy geometry | Package-local SVG resource referenced by Athena source |
| Package identity and dependency intent | Existing `athena.yaml` repository/package descriptor contract |
| Resource declarations and exported definitions | Governed representation `.athena` source |
| Resolved resource and export evidence | Compiler-generated `athena.lock` and proof payloads |
| Engineering product identity | Existing semantic/package facts; M35 introduces no new product identity model |
| Physical installation constraints | Explicit project `.athena` facts or admitted exports from the existing resolved package graph |
| Physical installation facts | Compiler-derived `PhysicalInstallationIR v0` |
| Cabinet physical arrangement | Cabinet composition projection compiler |
| Rendered visual output | Derived Graphic Primitive / Presentation payload |
| Editing | Governed source mutation followed by compile/lint/proof |

Filesystem package names are transport and dependency identity. They are not manufacturer, article
number, revision, lifecycle, rating, or product semantic identity.

SVG may expose geometry ids and strictly limited node-local reference hints. Athena source owns the
meaning of those nodes: anchor identity, label role, compatibility, binding, package identity, and
version. SVG does not own project, package, product, or engineering truth.

## Glossary

**Mounted Occurrence:** A compiled physical occurrence of one canonical semantic subject attached to
an explicit Cabinet container and mount target. Its visual Element is selected independently and
joined only during Cabinet composition.

**Physical Composition Model:** A constraint-aware model describing mounting relationships,
containers, physical dimensions, clearances, and routing spaces before renderer coordinates are
emitted.

**PhysicalInstallationIR v0:** The compiler-derived physical installation vocabulary containing
installation spaces, enclosures, mounting surfaces, rails, ducts, mounted occurrences, terminal
groups, and route channels. It is derived output, not authored source or renderer
state.

**Package Resource:** A package-owned file such as SVG geometry. It is resolved through package
identity and resource declarations, staged into an immutable snapshot, and never treated as source
metadata authority.

**Engineering Component System:** A future layer for reusable product knowledge: manufacturer,
article number, parameters, ports, dimensions, mounting rules, datasheets, validations, behavior,
and representation sets. M35 explicitly defers this layer.

**Physical Installation Contract:** The minimum typed installation constraints consumed by M35:
footprint, mounting type, orientation constraints, clearance, and container compatibility. M35 may
consume explicit project facts or existing governed package exports, but it does not define product
identity. A future Engineering Component System may provide this contract.

## Functional Requirements

### Feature 1 - Package Source Hierarchy

**FR-1:** M35 shall enforce package/path consistency for governed `.athena` source files. A file
declaring `package com.engineeringood.m35.cabinet` shall live under a matching
`com/engineeringood/m35/cabinet` package-relative path. Namespace segments shall be lowercase and
shall match the normalized source-root-relative parent directory exactly; default-package source is
forbidden.

**FR-2:** Package/path diagnostics shall be machine-readable, source-spanned where possible, and
published through LSP.

**FR-3:** The M35 sample shall migrate from flat source layout to package-hierarchical source layout.

**FR-4:** Generated snapshots and caches shall not participate in package/path validation.

### Feature 2 - Package-Local Resource Resolution

**FR-5:** A representation resource path shall resolve only from the directory of the governed
Athena source unit that declares it, inside that unit's admitted package snapshot. It shall not
resolve from workspace root, repository root, process working directory, or renderer state.

**FR-6:** Logical package resource identity shall include resolved package coordinate, declaring
source-unit id, and declared resource id. Admitted resource evidence shall include normalized
source-relative path, content hash, and compiler schema version. Lock state shall affect only the
higher compilation/cache fingerprint and shall not participate in logical resource or admitted
package snapshot identity.

**FR-7:** M35 shall support at least one local standard library package and one vendor/user package
fixture using Java-style directory hierarchy.

**FR-8:** Resource resolution shall reject absolute paths, traversal, symlinks, Windows junctions,
and every direct physical-path read across a package boundary. Dependency resources shall be reached
only through an exported logical Symbol/Element definition resolved inside the dependency's own
admitted snapshot; raw resource keys shall not cross source-unit or package boundaries.

**FR-9:** M35 shall introduce an explicit typed resource declaration for package-local assets. SVG
shall be one resource kind, not a special package language.

**FR-10:** M35 shall extend the existing compiler-generated `athena.lock` evidence with resolved
package coordinates, admitted package snapshot digests, resource hashes, dependency ids, and
compiler/schema version. Generated lock bytes shall not participate in the package snapshot digest.
Validate mode shall fail closed on missing, stale, or incompatible lock state. Explicit update mode
shall use canonical serialization and atomic replacement. Full package marketplace behavior remains
out of scope.

**FR-11:** M35 shall extend the existing `athena.yaml` repository/package descriptor contract with
the minimum authored package id/version intent, source-root, and dependency intent needed by the
proof. Derived resolved package coordinates belong to the resolved graph/snapshot and lock. Governed
`.athena` source owns typed resource declarations and exported definitions; compiler-generated lock
and proof payloads own resolved resource/export evidence. M35 shall not introduce `package.athena`,
a second manifest format, duplicated resource declarations, or runtime semantic interpretation of
descriptor metadata.

**FR-12:** Package identity shall remain independent from engineering product identity. Namespaces
such as `com.vendor.drive` shall not substitute for manufacturer, article number, revision, rating,
or lifecycle facts.

### Feature 3 - Complex SVG-Backed Vendor Elements

**FR-13:** M35 shall include a complex SVG-backed vendor element proof. The Athena declaration owns
identity, version, lifecycle, anchors, labels, compatibility, policy exposure, and binding. The SVG
owns only geometry ids and optional node-local reference hints.

**FR-14:** The SVG-backed element shall compile through the same representation contracts as native
Athena graphic bodies.

**FR-15:** Tests shall prove that unmarked SVG geometry is visual only and cannot become a connectable
anchor, label slot, engineering role, or binding predicate.

**FR-16:** Tests shall reject duplicate or conflicting metadata between Athena source and SVG.

**FR-17:** SVG `data-athena-*` support shall remain limited to geometry-reference hints. It shall not
declare ports, roles, directions, signals, element identity, package identity, lifecycle, profile, or
binding policy. Those declarations belong in Athena source. Removing SVG hints shall not change the
semantic model; when an Athena representation declaration references missing geometry, package
admission shall fail closed without reclassifying geometry as semantics.

### Feature 4 - Physical Installation And Cabinet Composition

**FR-18:** M35 shall define `PhysicalInstallationIR v0` with Cabinet as the first projection. Its
typed physical vocabulary shall include installation space, enclosure, mounting surface, rail, duct,
mounted occurrence, terminal group, route channel, physical extents, and source
provenance. Cabinet composition shall derive labels and drawing bounds from this IR plus resolved
representation occurrences.

**FR-19:** Authored Cabinet layout intent shall be compiled into physical placement facts instead of
being interpreted by the renderer.

**FR-20:** Enclosure shall be the sole physical container. Every mounted occurrence shall identify
one enclosure and exactly one mount target: mounting surface, rail, or terminal group. Duct and route
channel shall never be containers or mount targets. Authored placement coordinates shall be local to
the selected mount target. Terminals shall use the same mounted-occurrence contract; TerminalGroup
shall own their deterministic occurrence-key ordering.

**FR-21:** Cabinet document bounds and viewport shall derive from compiled physical composition
bounds, not hardcoded renderer dimensions.

**FR-22:** The renderer shall paint only compiled Cabinet composition and graphic primitive payloads.
It shall not infer placement from semantic ids, source order, DOM nodes, SVG ids, or CSS.

**FR-23:** Cabinet layout shall separate physical intent, placement constraints, and renderer
coordinates. Constraint facts include at minimum width, height, depth, selected/allowed orientation,
mounting type, clearance, target accepted mounting types, and container compatibility for mounted
occurrences.

**FR-24:** M35 shall implement deterministic Physical Constraint Evaluation v0 for fit validation,
collision detection, clearance checking, and container compatibility. It shall not implement general
constraint solving, placement optimization, automatic placement, or Auto Layout AI. Evaluation shall
require selected orientation membership, depth within enclosure usable depth, clearance-inflated fit
within enclosure and bounded surface/terminal targets, rail along-axis fit with zero normal offset,
exact typed mounting compatibility, containment of surfaces/ducts/terminal groups by the enclosure,
and containment of each complete oriented rail interval by its mounting surface.

**FR-25:** Visual Elements shall not become engineering product definitions. Physical dimensions and
mounting predicates used by M35 shall come from explicit project installation facts or admitted
exports from the existing package graph, never from visual geometry. A future Engineering Component
System may own reusable product values.

**FR-26:** M35 shall define a minimal Physical Installation Contract that validates whether a mounted
occurrence fits its physical context. `PhysicalInstallationContractV0` shall require positive width,
height, and depth; mounting type; a non-empty set of allowed orientations; non-negative top, right,
bottom, and left clearance; and a non-empty set of compatible container kinds.

**FR-27:** The Physical Installation Contract shall be sourced from explicit governed project facts
or admitted exports from the existing resolved package graph, never from renderer geometry, SVG
hints, or visual Element guesses. M35 shall not introduce a new product/component package type to
carry this contract.

### Feature 5 - Physical Connection Routing

**FR-28:** Conductors shall route through authored ordered typed route-channel sequences and terminal
anchors. Every engineering connection shall have a source-unit-unique authored alias distinct from
any group name; unreleased alias-free grouped and ungrouped syntax shall be migrated and removed.
`route` shall resolve aliases only in its own source unit. Every route channel shall belong to exactly one duct and declare a
duct-interior-local rectangle, axis-aligned orientation, lane count, and margin. M35 routing shall be
deterministic and shall not search for alternate routes.

**FR-29:** Route endpoints shall bind to explicit element anchors and fail closed when an anchor is
missing or incompatible. Lane centres shall use one exact rational formula. Consecutive route
channels shall transition only through one deterministically derived same-duct passable adjacency;
cross-duct, corner-only, overlapping, or gapped transitions shall fail.

**FR-30:** Structured proof shall report route count, channel usage, endpoint bindings,
body intersections, off-channel segments, and unbound endpoints.

**FR-31:** The target demo shall prove zero route/body intersections and zero route endpoints outside
their bound anchors.

### Feature 6 - Editable Traceability

**FR-32:** Every rendered Cabinet graphic occurrence shall carry stable trace evidence back to its
authoritative `.athena` declaration and compiled package/source provenance.

**FR-33:** Selection in the Cabinet view shall resolve to a semantic or representation subject through
the existing interaction/subject model, not through DOM/SVG guessing.

**FR-34:** A minimal graphic-to-source proof shall show that a selected mounted occurrence can identify
the source declaration that owns its device, binding, element definition, and SVG/native graphic body.

**FR-35:** M35 shall define the governed editing contract for future graphic-side movement or element
replacement: UI action -> semantic/representation action intent -> source mutation preview -> compile
and lint -> accept/reject -> rerender.

**FR-36:** M35 shall not directly mutate Presentation IR, Graphic Primitive IR, SVG files, or renderer
state as an authoritative edit.

### Feature 7 - Product Demo Proof

**FR-37:** M35 shall provide one dedicated sample project under `examples/m35/...` that opens with the
Cabinet surface active by default.

**FR-38:** The sample shall include an enclosure, rail-mounted controls, terminal strip, route
channels, labels, complex SVG-backed vendor element, and package-hierarchical sources.

**FR-39:** E2E shall capture desktop and narrow screenshots and compare them against a structural
Cabinet visual checklist derived from the approved QET reference. M35 shall not use strict
pixel-perfect comparison as the approval authority. The checklist shall require visible enclosure,
mounting surface/rail, duct and route channels, terminal group, mounted controls, readable labels,
contained routes, professional density, no clipping/text overflow/unintended overlap, and complete
graphic occurrence trace.

**FR-40:** The product smoke shall prove zero LSP diagnostics, zero fallback components, zero XML
runtime authority, zero raw SVG/HTML transport authority, nonblank canvas pixels, and visible
professional Cabinet layout.

### Feature 8 - Cleanup Gate

**FR-41:** Every M35 story shall end with a polish/purge step covering dead code, stale docs, old
fixtures, generated snapshots, duplicate paths, unused XML logic, and obsolete compatibility names.

**FR-42:** M35 shall remove unreleased legacy XML/package/render paths instead of preserving
compatibility unless a test explicitly proves they are still needed as migration evidence.

## Non-Functional Requirements

**NFR-1:** Athena source remains the single metadata authority.

**NFR-2:** Package/resource resolution shall be deterministic, reproducible, and safe to cache.

**NFR-3:** Cabinet composition shall be compiler-owned and renderer-neutral.

**NFR-4:** The public language surface shall stay small and type-safe for human and AI authors.

**NFR-5:** Tests and E2E proof shall be falsifiable; screenshots cannot be mocked or replaced with
hardcoded background images.

**NFR-6:** The implementation shall prefer deleting unreleased legacy paths over compatibility
adapters.

**NFR-7:** M35 shall preserve Athena's general EngineeringOS boundary. Cabinet-specific work must be
implemented as one physical projection/compiler path and shall not pollute semantic kernel ownership
with ECAD-only drawing concepts.

**NFR-8:** M35 shall not introduce AI auto-layout. AI can consume future traceability and diagnostics,
but physical constraints and deterministic compilers must exist before agent-driven layout.

## Core Acceptance Scope

M35 is complete when:

1. Package/path lint rejects mismatched `.athena` source hierarchy.
2. Package-local resource declarations, existing `athena.yaml` descriptor evidence, and extended
   `athena.lock` evidence work for standard and vendor/user representation packages.
3. A complex SVG-backed vendor element compiles with Athena-owned metadata and SVG-owned geometry
   hints only.
4. Physical Installation Model has Cabinet as the first projection and separates intent,
   constraints, and coordinates while modeling enclosure, rails, ducts, terminal strips, mounted
   components, route channels, labels, and bounds.
5. Minimal Physical Installation Contract validates footprint, mounting type, clearance, and container
   compatibility for the sample.
6. The Cabinet renderer paints only compiled composition/primitive payloads.
7. Every rendered occurrence traces back to authoritative `.athena` source.
8. The M35 sample opens in IDE with Cabinet active and zero LSP diagnostics.
9. Desktop and narrow screenshots satisfy the professional Cabinet visual contract.
10. Every story has RED/GREEN evidence and final polish/purge notes.

## Success Metrics

**SM-1:** Package/path mismatch is caught by compiler/LSP before runtime.

**SM-2:** Representation package resources resolve without workspace-relative hacks.

**SM-3:** At least one complex SVG-backed vendor element renders and exposes governed anchors/labels.

**SM-4:** Cabinet proof reports zero fallback components, zero unbound anchors, zero route/body
intersections, and zero off-canvas required content.

**SM-5:** Every visible mounted occurrence has source trace evidence.

**SM-6:** Product screenshots are visually Cabinet-like, not a flat toy graph.

**SM-7:** No visual Element owns manufacturer article identity, product lifecycle, datasheet,
engineering parameters, or validation rules.

**SM-8:** Cabinet-specific concepts remain outside the semantic kernel except as typed physical
projection facts.

## Counter-Metrics

**CM-1:** Do not spread effort across Cabinet, Documentation, and Schematic.

**CM-2:** Do not reintroduce XML or SVG as metadata authority.

**CM-3:** Do not hide layout failure behind passing structural payload tests.

**CM-4:** Do not create a second package model for resources outside the existing repository/package
contracts.

**CM-5:** Do not mutate renderer output directly and call it editing.

**CM-6:** Do not let Java-style namespace become engineering product identity.

**CM-7:** Do not introduce Auto Layout AI before the physical model, constraints, and rules are
compiler-owned.

**CM-8:** Do not create a Physical Installation Reference Package or any other package model parallel
to Athena's existing repository/package contracts.

## Resolved Planning Decisions

- M35 uses the typed `resource <id> { kind svg path "./asset.svg" }` declaration already shown in
  the addendum; `graphic svg resource <id>` consumes it.
- The mandatory structural visual checklist is fixed by FR-39 and supplemented by structured
  authority, routing, bounds, and trace proof.
