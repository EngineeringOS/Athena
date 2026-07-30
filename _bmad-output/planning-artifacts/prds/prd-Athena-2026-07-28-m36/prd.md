---
title: Athena M36 - Connection Topology And Professional Cabinet Composition
status: final
created: '2026-07-28'
updated: '2026-07-28'
---

# PRD: Athena M36 - Connection Topology And Professional Cabinet Composition

## 0. Document Purpose

This PRD defines M36 after the M35 Cabinet retrospective. It focuses on one visible product
surface: a professional Cabinet projection backed by typed engineering connections, SVG-backed
representation metadata, deterministic placement, and governed routing. It preserves the Athena
principles established by M34 and M35: `.athena` source is the engineering authority, SVG is a
geometry carrier, and the renderer only paints compiled facts.

This is a draft for review. It is not implementation authorization until the scope and authority
boundaries are approved.

## 1. Vision

Athena should express engineering logic in a typed language and compile that logic into a usable
engineering drawing. A complex vendor SVG may carry geometry that is impractical to author as
primitive DSL, but it must still participate in Athena through explicit, typed metadata: ports,
directions, signal kinds, anchor compatibility, parameters, and physical constraints.

M36 establishes Engineering Connectivity Semantics. Cabinet is the first demanding consumer and
the visible acceptance surface, not the owner of the new model. M36 closes the weak point between
an element's visual geometry and the engineering connection model by lowering Athena semantics and
representation geometry into transient Connection IR and Layout Graph forms. A layout planner may
calculate placement and route proposals, including an ELK-backed adapter, but Athena validates and
owns the resulting PlacementFacts and RouteFacts.

The target is not an SVG editor or an ECAD clone. It is a governed EngineeringOS transaction:

```text
Athena engineering source
    -> typed connectable entities, ports, and connections
    -> Engineering Lowering
    -> transient Connection IR
    -> SVG/native element geometry binding
    -> transient Layout Graph
    -> planner proposal
    -> Athena validation and derived facts
    -> Graphic Primitive IR
    -> paint-only Cabinet renderer
```

## 2. Target User

### 2.1 Jobs To Be Done

- Define engineering components and their connection contracts in a form that humans and AI can
  author and the compiler can type-check.
- Reuse complex vendor geometry without duplicating engineering meaning in SVG.
- See connected elements arranged clearly, with readable routes, labels, junctions, and crossings.
- Select a rendered occurrence and trace it back to its `.athena` declaration and representation
  source.
- Receive explicit diagnostics when a connection cannot be bound, placed, or routed safely.

### 2.2 Non-Users (M36)

- Users seeking a general-purpose SVG drawing editor.
- Users editing raw renderer output as authoritative data.
- Users requiring finished Documentation or Schematic workbench surfaces.
- Users requiring a complete vendor product catalog or EPLAN-equivalent library system.

### 2.3 Key User Journey

- **UJ-1. An engineer opens a Cabinet project and verifies a connected installation.**
  - Entry state: the M36 sample opens with Cabinet active and source-backed projection loaded.
  - Path: the engineer inspects vendor-backed and native elements, follows typed connections,
    selects a port or route, and reads the source trace and route-quality evidence.
  - Climax: the Cabinet shows readable orthogonal routes that avoid element bodies, preserve
    junction meaning, and remain inside valid physical routing areas.
  - Resolution: the engineer can identify the exact source declaration and any failed or degraded
    constraint without inspecting SVG or renderer internals.

## 3. Glossary

- **Connectable Entity:** An engineering entity that participates in Connections through typed
  Ports. It may resolve from existing Component knowledge, a project entity, or another governed
  domain concept.
- **Connectable Entity Contract:** The minimum typed view of a Connectable Entity required by M36:
  stable identity, Interfaces, Ports, parameters used by compatibility, and source provenance. It
  consumes existing component knowledge but does not redefine or expand the Component System.
- **Port:** A semantic connection endpoint owned by a Connectable Entity. It carries typed direction,
  signal/medium, multiplicity, and compatibility facts.
- **Element:** A reusable visual composition made from Symbols, primitives, and optional SVG
  geometry. It does not own project product identity or engineering port meaning.
- **Symbol:** An atomic visual representation unit.
- **Anchor:** A representation-level location that binds a semantic Port to geometry. An Anchor is
  not itself a Port.
- **Connection:** An authored engineering relationship between typed Ports.
- **Route Intent:** Authored or compiler-derived routing requirements and preferences for realizing
  a Connection, such as required channels, avoidance zones, bundle affinity, and route priority. It
  is not a geometric path.
- **Connection Network:** A semantic group of related Connections and junctions that share an
  engineering relationship. It is not a visual route.
- **Route Bundle:** A derived physical or presentation grouping used to keep related routes in
  readable corridors. It does not change Connection meaning.
- **Placement Fact:** A compiler-derived occurrence position and orientation for a resolved Element
  in a projection context.
- **Route Fact:** A compiler-derived path between resolved Anchors, including segments, bundles,
  junctions, crossings, labels, and quality evidence.
- **Engineering Lowering:** The compiler stage that transforms governed Engineering IR into
  projection-specific transient IR without changing semantic identity or source authority.
- **Connection IR:** A transient, typed compiler form containing resolved Connectable Entities,
  Ports, Connections, Connection Networks, compatibility evidence, and source provenance.
- **Layout Graph:** A transient, disposable projection IR containing node bounds, Ports, Anchors,
  classified constraints, obstacles, and relationships supplied to a layout or routing planner. It
  is never persisted engineering truth.
- **Geometry Reference:** A stable SVG/native geometry identifier used by Athena source to bind an
  Anchor or visual group to geometry.
- **Planner:** A non-authoritative algorithm that proposes PlacementFacts or RouteFacts. Athena
  remains the authority after validation and normalization.
- **Physical Installation Model:** The projection-layer model for enclosures, rails, ducts, mounts,
  terminal groups, clearances, and physical route channels.

## 4. Features

### 4.1 Connectable Entity And Port Semantics

**Description:** Athena source and existing governed knowledge define the minimum contract needed
to connect and represent an engineering entity. The Connectable Entity Contract is a compiler view,
not a second Component System, vendor catalog, or renderer model.

#### FR-1: Define typed connectable entity contracts

Athena shall compile a Connectable Entity Contract with stable identity, Interfaces or port groups,
typed Ports, source provenance, and only the parameters required for connection compatibility.

**Consequences:**

- Port identity is stable and source-spanned.
- Port direction, signal/medium kind, multiplicity, and compatibility are type-checked.
- Existing M14 Component knowledge may supply the contract, but M36 does not redefine that model.
- Product catalog expansion, lifecycle, datasheets, and full vendor knowledge remain out of scope.

#### FR-2: Separate port meaning from visual anchor location

The compiler shall reject a connection that resolves directly to an SVG node or Element without an
explicit Port-to-Anchor binding.

#### FR-3: Validate connection compatibility

The compiler shall validate that both endpoints exist and that their typed Port contracts are
compatible before layout or routing begins.

#### FR-4: Preserve general EngineeringOS vocabulary

The language shall use generic Connectable Entity, Interface, Port, Connection, and Network
concepts. It
shall not make IEC, Cabinet, DIN rail, or electrical drawing terms mandatory in the semantic kernel.

### 4.2 SVG Geometry Bridge And Representation Metadata

**Description:** Complex vendor geometry may remain in package-local SVG. Athena source supplies
the engineering and representation metadata needed to make that geometry usable without creating a
second authority.

#### FR-5: Admit SVG as geometry only

The compiler shall admit a package-local SVG as a geometry resource after safe parsing and resource
validation. SVG shall not define Connectable Entity identity, Port meaning, package identity,
binding policy,
signal type, direction, lifecycle, or engineering parameters.

#### FR-6: Support stable geometry references

SVG may expose limited `data-athena-*` geometry hints, initially a stable geometry reference such
as `data-athena-ref="anchor:power-in"`. These hints identify where geometry is located; Athena
source defines what the location means.

#### FR-7: Compile SVG-backed Anchor bindings

Athena source shall be able to bind an Anchor to a Geometry Reference and declare its Port mapping,
direction, signal kind, label role, and compatibility facts.

#### FR-8: Fail closed on invalid bridge metadata

The compiler shall reject missing, duplicate, ambiguous, or conflicting Geometry References. It
shall never infer an engineering Port from an unmarked SVG path, CSS selector, DOM order, or visual
position.

#### FR-9: Keep simple Symbols native

Simple atomic Symbols may be expressed entirely in typed Athena representation source. SVG-backed
Elements and native Athena Symbols shall use the same compiled Anchor and representation contracts.

### 4.3 Semantic Connection Topology

**Description:** M36 separates engineering topology from visual line drawing. A Connection Network
may contain multiple Connections and explicit semantic junctions. A Route Bundle is derived later
for readability and physical realization.

#### FR-10: Compile typed Connections

Each authored Connection shall resolve to exactly two typed Ports or to an explicitly declared
Connection Network operation. Unresolved or incompatible endpoints shall block composition.

#### FR-11: Represent semantic junctions explicitly

A semantic junction shall be represented by a Connection Network or junction fact. A geometric line
intersection shall never imply an engineering join.

#### FR-12: Derive Route Bundles without changing meaning

The composition compiler may group related Connections into Route Bundles using typed signal,
physical channel, or authored grouping constraints. Bundle membership shall not alter semantic
connectivity.

#### FR-13: Emit crossing and junction facts

The compiler shall emit explicit RouteJunctionFacts and RouteCrossingFacts. A crossing without a
semantic junction shall remain electrically or logically unjoined.

**Constraint ownership:**

- Semantic Constraints are owned by the engineering model and govern Port/Connection validity.
- Representation Constraints are owned by the Element/Anchor model and govern visual exits,
  attachment, bounds, and representation compatibility.
- Physical Constraints are owned by the Physical Installation Model and govern mounting, clearance,
  containment, channel capacity, and physical feasibility.
- Layout Preferences are owned by authored projection intent or planner policy and influence
  arrangement without changing engineering meaning.

### 4.4 Layout Graph And Placement Compilation

**Description:** Engineering Lowering compiles semantic, representation, and physical facts into a
transient Layout Graph. Placement is derived from classified constraints and Element bounds; the
renderer never invents coordinates and the Layout Graph is never source or persisted truth.

#### FR-14: Build a typed Layout Graph

The Layout Graph shall contain, at minimum, resolved node identity, occurrence identity, bounds,
Ports, Anchors, preferred exit/entry sides, grouping constraints, alignment constraints, obstacle
bounds, Connection relationships, constraint owner, constraint strength, and source provenance.

#### FR-15: Compute placement from intent and constraints

The compiler shall support deterministic placement proposals that respect explicit `near`, `below`,
`align`, `group`, containment, orientation, spacing, and clearance constraints used by the Cabinet
proof.

#### FR-16: Separate authored intent from derived coordinates

Authored source may express placement intent or a governed placement override. Renderer coordinates
shall be derived PlacementFacts and shall not become hidden source truth. The transient Layout Graph
shall not be serialized as a project authority or independently edited.

#### FR-17: Validate placement before routing

Placement compilation shall report overlap, out-of-container, invalid orientation, insufficient
clearance, missing bounds, and unplaced occurrence diagnostics before the route planner runs.

#### FR-18: Support deterministic refinement

The compiler shall be able to rerun placement after a source or constraint change and produce stable
results for the same source revision, package snapshot, planner configuration, and layout snapshot.

### 4.5 Planner Boundary And ELK-Inspired Layout

**Description:** Athena may use an external or internal planner for graph layout and route search.
The planner receives a derived Layout Graph and returns a proposal. The proposal is normalized and
validated by Athena.

#### FR-19: Define a planner adapter boundary

Athena shall expose a planner boundary that accepts a typed Layout Graph and returns placement and/or
route proposals tagged with planner identity and snapshot identity.

#### FR-20: Permit ELK-backed proposals

M36 may use Eclipse Layout Kernel concepts or an ELK-backed adapter for layered placement,
port-aware edges, compound nodes, orthogonal routing, labels, spacing, and crossing reduction.
ELK is an implementation option, not an Athena language or authority layer.

#### FR-21: Keep planner authority constrained

No planner may own semantic Connection meaning, Port identity, source mutation, occurrence identity,
package metadata, or persisted layout truth. Athena shall reject planner output that claims any of
these authorities.

#### FR-22: Normalize planner output

Athena shall normalize planner output into PlacementFacts, RouteFacts, junction/crossing facts, and
diagnostics. Planner-specific graph objects shall not cross the LSP or renderer protocol.

#### FR-23: Support planner failure evidence

When no valid placement or route exists, the compiler shall report the failed constraints, affected
source declarations, planner identity, and snapshot identity. It shall not silently draw a guessed
fallback route.

#### FR-24: Preserve Cabinet physical rules

For Cabinet, a planner shall respect enclosure, rail, duct, mount, clearance, terminal-group, and
route-channel rules. Generic graph layout shall not override physical installation constraints.

### 4.6 Professional Connection Routing

**Description:** The Cabinet path shall produce readable orthogonal routes between bound Anchors,
with obstacle avoidance, channel selection, route bundles, labels, and explicit quality evidence.

#### FR-25: Select route channels by default

The compiler shall select a valid physical route-channel sequence from the Physical Installation
Model by default. Source may add optional `through`, `avoid`, `bundle`, or priority constraints;
source shall not be required to enumerate every channel for ordinary connections.

#### FR-26: Route with obstacle and clearance awareness

Route planning shall avoid inflated Element and physical-object bounds, preserve anchor exit/entry
directions, remain inside valid channels, and report collisions or off-channel segments.

#### FR-27: Optimize readable routes deterministically

Within valid alternatives, route selection shall use stable costs for crossings, bends, total length,
bundle continuity, channel changes, label clearance, and route priority.

#### FR-28: Draw valid route topology

The compiler shall emit orthogonal segments, labels, junction markers, crossing markers, and route
quality facts. Direct center-to-center lines and renderer-generated connections are forbidden.

#### FR-29: Separate schematic and physical policy

The route contract shall be reusable across EngineeringOS projections, while Cabinet-specific
channel and mounting rules remain in the Physical Installation projection. M36 does not polish the
Schematic or Documentation surfaces.

### 4.7 Traceability, Diagnostics, And Governed Editing

**Description:** Every visible object and route remains explainable and source-backed.

#### FR-30: Trace every occurrence and route

Each rendered occurrence, Port, Anchor, Connection, PlacementFact, and RouteFact shall carry stable
trace evidence to its source declaration, package snapshot, representation definition, and derived
compiler snapshot.

#### FR-31: Expose structured diagnostics

LSP and IDE protocols shall expose typed diagnostics for missing Geometry References, invalid Port
bindings, incompatible Connections, placement failures, route failures, crossings, and degraded
planner constraints.

#### FR-32: Govern future graphic edits

Any future move, reconnect, or replacement operation shall follow intent -> source mutation preview
-> validation/lint -> accept/reject -> compile -> rerender. Direct SVG, DOM, Presentation IR, or
Graphic Primitive IR mutation is not an authoritative edit.

### 4.8 Dedicated Cabinet Product Proof

**Description:** M36 proves one complete component-backed Cabinet project end to end. The proof must
be real and source-backed; mocked graph payloads or screenshot-only claims are invalid.

#### FR-33: Provide a dedicated M36 sample

The sample shall contain native Athena Symbols, at least one complex SVG-backed Element, typed input
and output Ports, multiple Connections, a Connection Network/junction, grouped routes, physical
obstacles, labels, and a Cabinet installation context.

#### FR-34: Open Cabinet source-first

The IDE shall open the sample with the Cabinet projection loaded from repository source without
requiring an editor lifecycle side effect.

#### FR-35: Prove professional composition

The E2E proof shall demonstrate contained placement, readable density, stable labels, obstacle-free
routes, explicit junction/crossing treatment, nonblank rendering, and source traceability at
desktop and narrow viewports.

#### FR-36: Prove authority boundaries

The E2E proof shall report zero LSP diagnostics, zero unresolved Port-to-Anchor bindings, zero
unapproved fallback routes, zero route/body intersections, zero off-channel required segments, zero
XML runtime authority, and zero raw SVG metadata authority.

### 4.9 Engineering Lowering, Route Intent, And Semantic Proof

**Description:** M36 makes the compiler stages and their evidence explicit. Semantic truth is
lowered into transient IR, Route Intent guides realization, and every accepted Route Fact carries a
complete chain back to engineering source.

#### FR-37: Lower Engineering IR into transient Connection IR

The compiler shall lower resolved Connectable Entity Contracts, Ports, Connections, Connection
Networks, compatibility evidence, and source provenance into transient Connection IR. Lowering shall
preserve semantic identities and shall not invent engineering meaning.

#### FR-38: Preserve constraint ownership through lowering

Every constraint entering Connection IR or Layout Graph shall identify its Semantic,
Representation, Physical, or Layout Preference owner and its required/preferred/optional strength.
A planner may optimize Layout Preferences but shall not weaken required constraints from another
owner.

#### FR-39: Compile Route Intent separately from Route Facts

Each routable Connection shall produce a Route Intent before planning. Route Intent may include
required or preferred channels, avoided zones, bundle affinity, priority, and endpoint-side
preferences. It shall contain no final segments or renderer coordinates.

#### FR-40: Emit complete semantic route proof

Every accepted Route Fact shall identify its Connection, source and target Ports, source and target
Anchors, source and target occurrence identities, selected physical channels where applicable,
Route Intent, planner identity, compiler snapshot, route quality, and source provenance.

## 5. Non-Goals (Explicit)

- Documentation, Schematic, and Wiring product-quality surfaces.
- A general-purpose SVG editor or visual-only connection editor.
- Expansion of the existing Component Knowledge System into a complete vendor product catalog.
- AML/XML as Athena runtime language, metadata authority, or package model.
- SVG as an engineering metadata authority.
- General-purpose global constraint solving, optimization research, or Auto Layout AI.
- Direct persistence of planner coordinates as hidden source truth.
- Strict pixel identity with QElectroTech. QET remains a visual and domain reference; Athena's
  approval authority is structural, semantic, and renderer-independent proof.
- Public compatibility with unreleased legacy XML, stale package paths, or obsolete runtime routes.

## 6. MVP Scope

### 6.1 In Scope

- Typed Connectable Entity Contract, Interface, Port, Connection, and Connection Network contracts.
- SVG Geometry References bridged by Athena-owned Anchor declarations.
- Native Athena Symbols and complex SVG-backed Elements in one representation contract.
- Layout Graph construction with bounds, ports, anchors, obstacles, and constraints.
- Engineering Lowering through transient Connection IR and Layout Graph forms.
- Classified constraint ownership and a distinct Route Intent stage.
- Deterministic placement and orthogonal routing for the Cabinet proof.
- Planner adapter boundary with ELK-backed integration as the preferred candidate, subject to
  authority normalization.
- Physical route-channel selection, bundles, junctions, crossings, labels, and route-quality facts.
- Source traceability and compiler/LSP diagnostics.
- Dedicated M36 Cabinet sample and E2E proof.
- Purging stale XML and renderer-owned connection paths.

### 6.3 Core And Stretch Acceptance

**M36 Core must finish:**

- Connectable Entity Contract, Port compatibility, and Connection/Network semantics.
- Port-to-Anchor binding for native and SVG-backed representations.
- Engineering Lowering, transient Connection IR, and transient Layout Graph.
- Constraint ownership and Route Intent.
- Deterministic baseline placement and obstacle-aware Cabinet route realization.
- Complete semantic route proof and source traceability.

**M36 Stretch, only after Core passes:**

- A production ELK dependency rather than the planner SPI and Athena-native baseline.
- Advanced crossing minimization and global route optimization.
- Complex automatic label placement beyond the sample's professional acceptance needs.

### 6.2 Out of Scope for MVP

- Full component catalog, vendor lifecycle, datasheets, simulation, manufacturing, or AI agent
  behavior.
- Remote resource URIs and package registry distribution. Record as M37 handoff.
- Editing complex SVG geometry in the IDE.
- Automatic replacement of a component by compatibility search.
- Documentation and Schematic rendering improvements.

## 7. Cross-Cutting NFRs

- **NFR-1 SSOT:** `.athena` source is the only engineering and representation metadata authority.
- **NFR-2 Type safety:** Invalid Port, Anchor, Connection, Geometry Reference, and planner outputs
  fail compilation or normalization with source-spanned diagnostics.
- **NFR-3 Determinism:** Same source revision, package snapshot, planner version, and configuration
  produce the same derived PlacementFacts and RouteFacts.
- **NFR-4 Explainability:** Every derived fact is traceable to source and compiler snapshots.
- **NFR-5 Renderer purity:** Renderer consumes compiled payloads and never infers engineering
  meaning or layout.
- **NFR-6 Security:** SVG parsing remains fail-closed; scripts, external references, unsafe entities,
  and raw markup transport remain forbidden.
- **NFR-7 Performance:** A dedicated sample with at least 20 occurrences and 30 Connections shall
  compile and render interactively on the supported development workstation without visible UI
  deadlock. Exact budget is an implementation-plan item.
- **NFR-8 Cleanup:** Because Athena is unreleased, stale incompatible paths shall be removed rather
  than preserved through compatibility shims.
- **NFR-9 Transience:** Connection IR and Layout Graph are disposable compiler forms. They shall not
  become authored project models, runtime metadata authorities, or renderer-owned state.

## 8. Success Metrics

**Primary**

- **SM-1:** A complex SVG-backed Element exposes typed Port-to-Anchor bindings without putting
  engineering meaning in SVG. Validates FR-5 through FR-9.
- **SM-2:** A Cabinet sample with at least 20 occurrences and 30 Connections produces deterministic,
  readable PlacementFacts and RouteFacts with zero unapproved fallback routes. Validates FR-14
  through FR-29.
- **SM-3:** The sample has zero unresolved endpoints, zero route/body intersections, zero off-channel
  required segments, and explicit semantic junction/crossing evidence. Validates FR-10 through
  FR-13 and FR-25 through FR-28.
- **SM-4:** Every visible occurrence, Port, Anchor, and Route can be traced to `.athena` source and
  package-local representation material. Validates FR-30 through FR-32.
- **SM-5:** Every accepted Route Fact includes Connection, endpoint Port/Anchor/occurrence,
  Route Intent, selected channel evidence, planner identity, compiler snapshot, quality, and source
  provenance. Validates FR-37 through FR-40.

**Secondary**

- **SM-6:** The IDE opens the sample source-first with Cabinet active, nonblank canvas pixels, and
  zero LSP diagnostics. Validates FR-33 through FR-36.
- **SM-7:** The planner can be replaced or disabled without changing Athena semantic identities or
  source serialization. Validates FR-19 through FR-23.

**Counter-metrics**

- **SM-C1:** Do not measure success by the number of SVG tags, DSL keywords, or planner-specific
  settings added.
- **SM-C2:** Do not accept a screenshot that passes while structured route, authority, or trace proof
  fails.
- **SM-C3:** Do not optimize for generic graph aesthetics at the expense of Cabinet physical rules.
- **SM-C4:** Do not grow M36 into a vendor catalog, multi-view IDE, or AI auto-layout milestone.

## 9. Resolved Planning Decisions

1. The authoritative planner runs in the Kotlin compiler. LSP and Theia consume only compiled
   PlacementFacts, RouteFacts, diagnostics, and proof; frontend lifecycle cannot determine layout.
2. M36 Core uses an Athena-native deterministic planner. ELK remains an optional JVM adapter after
   the core lowering, normalization, and physical-policy proof passes.
3. M36 permits only `data-athena-ref` as SVG geometry-reference vocabulary. Additional extensions
   require a later versioned proposal and remain non-semantic.
4. The Cabinet proof uses package-defined categories `power.ac`, `power.dc`, `control.digital`,
   `control.analog`, and `protective-earth`. The language kernel remains domain-neutral.
5. Remote package resources and registry resolution remain deferred to M37.

## 10. Assumptions Index

- **[ASSUMPTION A1]:** Cabinet remains the only product-quality visible surface for M36.
- **[ASSUMPTION A2]:** Existing package-local resource and SVG safety boundaries can be reused and
  refactored without preserving stale XML compatibility.
- **[ASSUMPTION A4]:** M36 compiles only the minimum Connectable Entity Contract required for
  connectivity. It consumes existing Component knowledge without expanding it into a full vendor
  product system.
- **[ASSUMPTION A5]:** Source may express optional placement and route constraints, while the
  compiler/planner selects default placement and channel paths.
