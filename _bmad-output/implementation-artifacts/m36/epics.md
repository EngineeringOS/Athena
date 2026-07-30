---
stepsCompleted: [1, 2, 3]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md
---

# Athena - M36 Epic Breakdown

## Overview

This document decomposes Athena M36 Engineering Connectivity Semantics into implementation epics
and stories. Cabinet is the only visible M36 product surface. The semantic connection compiler is
the milestone core; Cabinet is its proof consumer.

## Requirements Inventory

### Functional Requirements

FR-1: Compile a typed Connectable Entity Contract with stable identity, Interfaces, typed Ports,
compatibility parameters, and source provenance.

FR-2: Reject connections that resolve directly to SVG/Element geometry without an explicit
Port-to-Anchor binding.

FR-3: Validate both connection endpoints and typed Port compatibility before layout or routing.

FR-4: Keep Connectable Entity, Interface, Port, Connection, and Network vocabulary generic and out
of ECAD-specific semantic-kernel ownership.

FR-5: Admit package-local SVG only as safe geometry, never as authority for semantic or
representation metadata.

FR-6: Support only stable non-semantic SVG geometry references, initially
`data-athena-ref="anchor:<id>"`.

FR-7: Bind Athena-declared Anchors to Geometry References and declare Port mapping, direction,
signal kind, label role, and compatibility in Athena source.

FR-8: Fail compilation on missing, duplicate, ambiguous, or conflicting Geometry References; never
infer a Port from unmarked SVG geometry, CSS, DOM order, or appearance.

FR-9: Use one Anchor and representation contract for native Athena Symbols and SVG-backed Elements.

FR-10: Resolve every authored Connection to exactly two typed Ports or an explicit Connection
Network operation.

FR-11: Represent semantic junctions as Connection Network or junction facts; geometric crossings do
not imply connectivity.

FR-12: Derive Route Bundles from typed constraints without changing semantic connectivity.

FR-13: Emit explicit RouteJunctionFacts and RouteCrossingFacts.

FR-14: Build a typed transient Layout Graph containing identity, occurrences, bounds, Ports, Anchors,
constraints with owner/strength, obstacles, relationships, and provenance.

FR-15: Produce deterministic placement proposals respecting placement intent, alignment, grouping,
containment, orientation, spacing, and clearance.

FR-16: Keep authored placement intent/overrides separate from derived renderer coordinates; never
persist the Layout Graph as project authority.

FR-17: Report placement overlap, containment, orientation, clearance, bounds, and unplaced
occurrence diagnostics before routing.

FR-18: Produce stable refined placement for the same source revision, package snapshot, planner
configuration, and layout snapshot.

FR-19: Define a Planner SPI accepting a typed Layout Graph and returning snapshot-tagged placement
and/or route proposals.

FR-20: Permit, but do not require, a future ELK-backed adapter for layout/routing proposals.

FR-21: Reject any planner claim to semantic authority, source mutation, occurrence identity, package
metadata, or persisted layout truth.

FR-22: Normalize planner output into Athena PlacementFacts, RouteFacts, junction/crossing facts, and
diagnostics; no planner object crosses LSP or renderer protocols.

FR-23: Emit failed constraints, source declarations, planner identity, and snapshot identity when
placement or routing cannot be valid; do not silently guess a fallback route.

FR-24: Require Cabinet planner output to respect enclosure, rail, duct, mount, clearance,
terminal-group, and route-channel rules.

FR-25: Select a valid physical route-channel sequence by default; authored `through`, `avoid`,
`bundle`, and priority constraints are optional.

FR-26: Route around inflated Element/physical-object bounds, honor Anchor exit/entry directions,
remain in valid channels, and report collisions/off-channel segments.

FR-27: Deterministically optimize valid routes by crossings, bends, length, bundle continuity,
channel changes, label clearance, priority, and stable tie breaking.

FR-28: Emit orthogonal segments, labels, junction/crossing markers, and quality facts; forbid
center-to-center and renderer-generated connections.

FR-29: Keep generic route contracts reusable while Cabinet physical policy remains in the Physical
Installation projection; do not polish Schematic/Documentation surfaces.

FR-30: Carry stable source/package/representation/compiler trace from every occurrence, Port,
Anchor, Connection, PlacementFact, and RouteFact.

FR-31: Expose typed LSP/IDE diagnostics for bridge, binding, connection, placement, route, crossing,
and degraded-planner failures.

FR-32: Route future graphic edits through governed intent, source mutation preview, validation,
accept/reject, compile, and rerender; forbid direct SVG/DOM/IR authority mutation.

FR-33: Provide a dedicated M36 Cabinet sample with native Symbols, a complex SVG-backed Element,
typed Ports, Connections, Network/junction, bundles, obstacles, labels, and Cabinet installation.

FR-34: Open the M36 Cabinet projection source-first without an editor lifecycle side effect.

FR-35: Prove professional Cabinet composition at desktop and narrow viewports with contained
placement, readable density, routes, labels, junction/crossing treatment, nonblank output, and
source traceability.

FR-36: Prove zero LSP diagnostics, unresolved Port/Anchor bindings, unapproved fallback routes,
route/body intersections, required off-channel segments, XML authority, and raw SVG metadata
authority.

FR-37: Lower Connectable Entity Contracts, Ports, Connections, Networks, compatibility evidence,
and provenance into transient Connection IR without inventing engineering meaning.

FR-38: Preserve Semantic, Representation, Physical, or Layout Preference ownership and constraint
strength through lowering; planner optimization cannot weaken required constraints.

FR-39: Compile Route Intent before planning, with requirements/preferences but no final segments or
renderer coordinates.

FR-40: Include connection, endpoint Port/Anchor/occurrence, Route Intent, selected channels,
planner, compiler snapshot, quality, and provenance in every accepted Route Fact.

### NonFunctional Requirements

NFR-1: `.athena` source is the only engineering and representation metadata authority.

NFR-2: Invalid Ports, Anchors, Connections, Geometry References, and planner output fail with
source-spanned diagnostics.

NFR-3: Same source revision, package snapshot, planner version/configuration produce the same
PlacementFacts and RouteFacts.

NFR-4: Every derived fact is traceable to source and compiler snapshots.

NFR-5: Renderer only paints compiled payloads and never infers engineering meaning or layout.

NFR-6: SVG admission fails closed for scripts, external references, unsafe entities, and raw-markup
transport.

NFR-7: The M36 sample with at least 20 occurrences and 30 Connections compiles and renders
interactively without a visible UI deadlock on the supported development workstation.

NFR-8: Athena is unreleased; stale incompatible paths are removed instead of protected by
compatibility shims.

NFR-9: Connection IR and Layout Graph are disposable compiler forms, never authored project models,
runtime authority, or renderer-owned state.

### Additional Requirements

- Existing M14 component knowledge remains upstream. M36 adds a narrow Connectable Entity Contract
  rather than a second Component System.
- `component-model`/`connection-model` own connectivity contracts; `representation-model` owns
  Anchors and geometry binding; `physical-model` owns Cabinet feasibility policy.
- `compiler` owns Engineering Lowering, Planner SPI invocation, proposal normalization, and policy
  validation.
- `routing-model` owns Route Intent, Route Bundle, Route Fact, junction/crossing, quality, and
  planner evidence.
- Planner SPI is compiler-only. M36 Core uses an Athena-native deterministic planner; an ELK JVM
  adapter is deferred until the conformance suite exists.
- `data-athena-ref` is the only M36 SVG extension. It identifies geometry only.
- LSP/Theia and renderer consume normalized typed facts only.
- The Cabinet sample must be source-first and prove facts structurally plus through desktop/narrow
  E2E screenshots.
- M35/M36 source authority overrides stale M34 annotated-SVG contract metadata; no old SVG metadata
  profile or XML runtime path may remain active.

### UX Design Requirements

None. No M36 UX design contract was provided. Stories must preserve the existing Theia workbench
and use Cabinet only as the focused proof surface.

### FR Coverage Map

FR-1: M36-E1 - Connectable Entity Contract.
FR-2: M36-E2 - explicit Port-to-Anchor binding.
FR-3: M36-E1 - typed Port compatibility.
FR-4: M36-E1 - generic connectivity vocabulary.
FR-5: M36-E2 - safe SVG geometry admission.
FR-6: M36-E2 - `data-athena-ref` geometry bridge.
FR-7: M36-E2 - Athena-owned Anchor metadata.
FR-8: M36-E2 - fail-closed geometry-reference diagnostics.
FR-9: M36-E2 - common native/SVG representation contract.
FR-10: M36-E1 - typed Connection resolution.
FR-11: M36-E1 - semantic Network and junction meaning.
FR-12: M36-E4 - derived Route Bundles.
FR-13: M36-E4 - route junction/crossing facts.
FR-14: M36-E3 - transient Layout Graph.
FR-15: M36-E3 - deterministic placement proposals.
FR-16: M36-E3 - source intent separate from PlacementFacts.
FR-17: M36-E3 - pre-routing placement diagnostics.
FR-18: M36-E3 - stable placement refinement.
FR-19: M36-E3 - compiler-owned Planner SPI.
FR-20: M36-E3 - optional ELK adapter seam.
FR-21: M36-E3 - planner authority rejection.
FR-22: M36-E3 - normalized planner output.
FR-23: M36-E3 - planner failure evidence.
FR-24: M36-E3 - Cabinet physical-policy gate.
FR-25: M36-E4 - default route-channel selection.
FR-26: M36-E4 - obstacle/clearance-aware routing.
FR-27: M36-E4 - deterministic route quality costs.
FR-28: M36-E4 - orthogonal route realization.
FR-29: M36-E4 - generic route contract plus Cabinet policy.
FR-30: M36-E5 - source/package/compiler trace.
FR-31: M36-E5 - LSP/IDE diagnostics.
FR-32: M36-E5 - governed graphic edit contract.
FR-33: M36-E5 - dedicated M36 Cabinet sample.
FR-34: M36-E5 - source-first Cabinet loading.
FR-35: M36-E5 - visual and structural E2E proof.
FR-36: M36-E5 - no-authority-leak product smoke.
FR-37: M36-E1 - Engineering Lowering into Connection IR.
FR-38: M36-E1 - constraint ownership through lowering.
FR-39: M36-E4 - Route Intent compilation.
FR-40: M36-E4 - complete Route Fact proof.

## Epic List

### M36-E1: Compile Engineering Connectivity Semantics

Engineers and AI can author Connectable Entities, typed Ports, Connections, and Connection Networks
that compile into transient Connection IR with explicit constraint ownership. This delivers governed
engineering connectivity before any visual projection exists.

**FRs covered:** FR-1, FR-3, FR-4, FR-10, FR-11, FR-37, FR-38.

### M36-E2: Compile Port-To-Geometry Bindings

Engineers can bind typed Ports to native Symbols and complex SVG-backed Elements without allowing
SVG to become engineering authority. Invalid geometry references fail before layout.

**FRs covered:** FR-2, FR-5, FR-6, FR-7, FR-8, FR-9.

### M36-E3: Compile Valid Cabinet Placement

Engineers can compile a transient Layout Graph into deterministic PlacementFacts that respect
representation bounds and Cabinet physical policy. A compiler-owned Planner SPI proposes placement;
it cannot own source or semantic truth.

**FRs covered:** FR-14, FR-15, FR-16, FR-17, FR-18, FR-19, FR-20, FR-21, FR-22, FR-23, FR-24.

### M36-E4: Compile Physical Route Realization

Engineers can compile Connection and Route Intent into channel-aware, obstacle-free, orthogonal
RouteFacts with bundles, labels, junctions, crossings, quality, and complete route evidence.

**FRs covered:** FR-12, FR-13, FR-25, FR-26, FR-27, FR-28, FR-29, FR-39, FR-40.

### M36-E5: Inspect And Prove The Cabinet

Engineers can inspect source trace and diagnostics in IDE, then open a source-first M36 Cabinet
sample whose structured proof and screenshots establish a professional, governed product path.

**FRs covered:** FR-30, FR-31, FR-32, FR-33, FR-34, FR-35, FR-36.

## M36-E1: Compile Engineering Connectivity Semantics

Engineers and AI can author Connectable Entities, typed Ports, Connections, and Connection Networks
that compile into transient Connection IR with explicit constraint ownership. This delivers governed
engineering connectivity before any visual projection exists.

### Story 1.1: Declare Connectable Entities And Ports

As an engineer or AI agent,
I want to declare generic connectable entities, interfaces, and typed ports in Athena source,
So that connectivity has an explicit, type-safe semantic owner.

**Requirements:** FR-1, FR-4.

**Acceptance Criteria:**

**Given** valid Athena source declares a connectable entity and ports
**When** the source compiles
**Then** it produces stable identities, types, compatibility parameters, source spans, and provenance
**And** `ConnectableEntity`, `Interface`, `Port`, `Connection`, and `Network` remain generic
engineering vocabulary
**And** existing M14 component knowledge remains upstream rather than creating a second Component
System
**And** invalid declarations produce source-spanned compiler and LSP diagnostics.

### Story 1.2: Validate Typed Connections

As an engineer or AI agent,
I want to connect declared ports through typed connections,
So that invalid engineering relationships fail before projection.

**Requirements:** FR-3, FR-10.

**Acceptance Criteria:**

**Given** declared typed ports and an authored connection
**When** the source compiles
**Then** the connection resolves to exactly two declared typed ports unless it uses an explicit
network operation
**And** endpoint existence, direction, signal kind, role, and compatibility are validated before
layout or routing
**And** invalid, self-conflicting, or ambiguous endpoints fail deterministically with both source
locations
**And** no connection resolves directly to an element, symbol, SVG node, or geometry reference.

### Story 1.3: Model Networks And Semantic Junctions

As an engineer or AI agent,
I want to declare connection networks and junctions explicitly,
So that topology is never inferred from drawing crossings.

**Requirements:** FR-11.

**Acceptance Criteria:**

**Given** authored connection networks or junction operations
**When** the source compiles
**Then** each network has stable identity, member ports or connections, compatibility evidence, and
provenance
**And** a junction exists only through an authored network operation or compiled junction fact
**And** a visual crossing alone never creates engineering connectivity
**And** invalid network membership and incompatible junctions fail before lowering.

### Story 1.4: Lower Connectivity Into Transient Connection IR

As the compiler,
I want to lower validated connectivity into disposable Connection IR,
So that later placement and routing use typed facts without gaining semantic authority.

**Requirements:** FR-37, FR-38.

**Acceptance Criteria:**

**Given** validated connectivity source
**When** engineering lowering runs
**Then** Connection IR includes entities, ports, connections, networks, compatibility evidence,
provenance, and constraint ownership and strength
**And** Semantic, Representation, Physical, and Layout Preference constraints remain distinguishable
after lowering
**And** required constraints cannot be weakened by later planners
**And** Connection IR is compiler-owned, non-authored, non-persisted project truth and is excluded
from renderer and LSP raw transport.

## M36-E2: Compile Port-To-Geometry Bindings

Engineers can bind typed Ports to native Symbols and complex SVG-backed Elements without allowing
SVG to become engineering authority. Invalid geometry references fail before layout.

### Story 2.1: Admit Safe Package-Local SVG Geometry

As a representation-package author,
I want to reference complex package-local SVG as geometry,
So that Athena can use vendor artwork without giving SVG semantic authority.

**Requirements:** FR-5, FR-6.

**Acceptance Criteria:**

**Given** a package-local SVG resource is referenced by an Athena representation
**When** the resource is admitted
**Then** only stable `data-athena-ref` geometry references are recognized in M36
**And** scripts, event handlers, external references, unsafe entities, and raw-markup transport fail
closed
**And** SVG carries no Port, Anchor role, direction, signal, compatibility, or connection meaning.

### Story 2.2: Bind Athena Anchors To Geometry

As a representation author,
I want to map Athena-declared anchors and ports to native or SVG geometry references,
So that engineering meaning is explicitly connected to a visible location.

**Requirements:** FR-7, FR-9.

**Acceptance Criteria:**

**Given** an Athena representation declares anchors and Port mappings
**When** it compiles against native or SVG geometry
**Then** Athena source declares the Anchor identity, geometry reference, Port mapping, direction,
signal kind, label role, and compatibility
**And** the same Anchor contract works for native Athena Symbols and SVG-backed Elements
**And** SVG identifies only geometry while Athena alone assigns meaning
**And** binding output is typed and traceable to source, package, representation, and compiler
snapshot.

### Story 2.3: Reject Invalid Geometry Bindings

As an engineer or AI agent,
I want invalid Port-to-Anchor bindings to fail clearly,
So that no layout or route is based on inferred or ambiguous geometry.

**Requirements:** FR-2, FR-8.

**Acceptance Criteria:**

**Given** a representation has Port-to-Anchor bindings
**When** a Geometry Reference is missing, duplicate, ambiguous, or conflicting
**Then** compilation fails with source-spanned diagnostics
**And** the compiler never infers Ports from unmarked SVG geometry, CSS, DOM order, or visual
appearance
**And** a connection lacking an explicit valid Port-to-Anchor bridge fails before placement and
routing
**And** native and SVG-backed failures use one typed compiler and LSP diagnostic contract.

## M36-E3: Compile Valid Cabinet Placement

Engineers can compile a transient Layout Graph into deterministic PlacementFacts that respect
representation bounds and Cabinet physical policy. A compiler-owned Planner SPI proposes placement;
it cannot own source or semantic truth.

### Story 3.1: Lower Installation Intent Into Layout Graph

As the compiler,
I want to derive a transient Layout Graph from semantic, representation, and physical facts,
So that placement can be evaluated without making coordinates project truth.

**Requirements:** FR-14, FR-16.

**Acceptance Criteria:**

**Given** compiled connectivity, representation bindings, and physical installation facts
**When** placement lowering runs
**Then** the Layout Graph contains occurrences, bounds, Ports, Anchors, constraints, obstacles,
relationships, provenance, and ownership and strength
**And** authored placement intent and overrides remain separate from derived renderer coordinates
**And** the graph is disposable compiler IR, never persisted project authority or renderer-owned state
**And** it exposes enclosure, rail, duct, mount, clearance, terminal-group, and route-channel facts
required by Cabinet policy.

### Story 3.2: Produce Deterministic Placement Proposals

As an engineer or AI agent,
I want deterministic placement proposals from a compiler-owned planner,
So that valid Cabinet arrangement can be derived reproducibly.

**Requirements:** FR-15, FR-18, FR-19, FR-20, FR-22.

**Acceptance Criteria:**

**Given** a valid transient Layout Graph
**When** the Athena-native planner creates a placement proposal
**Then** it respects placement intent, alignment, grouping, containment, orientation, spacing, and
clearance
**And** identical source, package snapshot, planner version and configuration, and Layout Graph yield
identical PlacementFacts
**And** a compiler-only Planner SPI accepts a typed Layout Graph and returns snapshot-tagged
proposals
**And** output is normalized into Athena facts and diagnostics with no planner object crossing LSP or
renderer protocols
**And** ELK remains an optional future adapter, never a semantic authority.

### Story 3.3: Validate Cabinet Placement Policy

As an engineer or AI agent,
I want invalid placement rejected with evidence,
So that Cabinet composition never hides a physical conflict behind fallback coordinates.

**Requirements:** FR-17, FR-21, FR-23, FR-24.

**Acceptance Criteria:**

**Given** a planner placement proposal
**When** Cabinet physical-policy validation runs
**Then** it rejects overlap, containment, orientation, clearance, bounds, and unplaced-occurrence
violations before routing
**And** planner output cannot mutate source, own semantic identity, redefine package metadata, or
persist layout truth
**And** failed evaluation reports violated constraints, source declarations, planner identity, and
snapshot identity
**And** no silent fallback placement is produced and accepted PlacementFacts preserve complete
provenance.

## M36-E4: Compile Physical Route Realization

Engineers can compile Connection and Route Intent into channel-aware, obstacle-free, orthogonal
RouteFacts with bundles, labels, junctions, crossings, quality, and complete route evidence.

### Story 4.1: Compile Route Intent And Bundles

As an engineer or AI agent,
I want to express routing requirements without drawing final segments,
So that the compiler can derive routes while preserving engineering intent.

**Requirements:** FR-12, FR-39.

**Acceptance Criteria:**

**Given** typed connections and authored routing requirements
**When** Route Intent lowering runs
**Then** Route Intent supports required or preferred `through`, `avoid`, `bundle`, and priority
constraints
**And** it contains no final segments, renderer coordinates, or implicit topology
**And** typed connections lower into Route Bundles without changing semantic connectivity
**And** Route Intent and bundles preserve source spans, constraint ownership, strength, and
provenance.

### Story 4.2: Realize Cabinet Routes Through Physical Channels

As the compiler,
I want to realize valid orthogonal routes through Cabinet channels,
So that connections respect physical installation policy.

**Requirements:** FR-25, FR-26, FR-29.

**Acceptance Criteria:**

**Given** valid PlacementFacts, Route Intent, and Cabinet physical policy
**When** route planning runs
**Then** it selects valid physical route-channel sequences by default
**And** routes honor Anchor exit and entry directions, inflated component and physical-object bounds,
clearance, and valid channels
**And** off-channel segments, collisions, blocked paths, and violated required intent fail with
diagnostics rather than fallback routes
**And** generic route contracts remain reusable while Cabinet-specific policy stays in the Physical
Installation projection.

### Story 4.3: Emit Deterministic Route Facts And Markers

As an engineer or AI agent,
I want accepted routes to carry complete quality and trace evidence,
So that their result can be inspected, reproduced, and trusted.

**Requirements:** FR-13, FR-27, FR-28, FR-40.

**Acceptance Criteria:**

**Given** a valid route proposal
**When** the compiler normalizes and validates it
**Then** route selection is deterministic using crossings, bends, length, bundle continuity, channel
changes, label clearance, priority, and stable tie-breaking
**And** accepted routes emit orthogonal segments, labels, quality facts, RouteJunctionFacts, and
RouteCrossingFacts
**And** center-to-center and renderer-generated connections are forbidden
**And** every RouteFact includes connection, endpoint Port, Anchor, occurrence, Route Intent,
selected channels, planner, compiler snapshot, quality, and provenance
**And** geometric crossings never alter semantic connectivity.

## M36-E5: Inspect And Prove The Cabinet

Engineers can inspect source trace and diagnostics in IDE, then open a source-first M36 Cabinet
sample whose structured proof and screenshots establish a professional, governed product path.

### Story 5.1: Expose Typed Trace And Diagnostics

As an engineer or AI agent,
I want to inspect every rendered Cabinet fact back to its source and compiler evidence,
So that I can understand and correct the governing engineering intent.

**Requirements:** FR-30, FR-31.

**Acceptance Criteria:**

**Given** compiled Cabinet occurrences and facts
**When** an engineer inspects them through the IDE or LSP
**Then** occurrences, Ports, Anchors, Connections, PlacementFacts, and RouteFacts expose stable
source, package, representation, and compiler trace
**And** typed bridge, binding, connection, placement, route, crossing, and degraded-planner
diagnostics are available
**And** diagnostics identify relevant source spans and evidence rather than raw planner or SVG objects
**And** renderer and workbench retain no independent engineering or layout authority.

### Story 5.2: Define Governed Graphic Edit Intent

As an engineer,
I want graphic edits to become reviewable source mutations,
So that visual interaction cannot corrupt Athena SSOT.

**Requirements:** FR-32.

**Acceptance Criteria:**

**Given** a graphic edit request
**When** it enters the governed mutation path
**Then** it produces intent, source-mutation preview, validation result, accept or reject result,
recompilation, and rerendering
**And** direct mutation of SVG, DOM, Graphic Primitive IR, PlacementFacts, or RouteFacts is forbidden
**And** this story establishes the typed contract only and does not introduce a full Cabinet editor
**And** rejected edits preserve the compiled result and return actionable diagnostics.

### Story 5.3: Create The Source-First M36 Cabinet Sample

As an evaluator,
I want a dedicated M36 Cabinet example,
So that the connectivity compiler is proven through a realistic single product surface.

**Requirements:** FR-33, FR-34.

**Acceptance Criteria:**

**Given** the dedicated M36 sample project
**When** it compiles and opens its Cabinet projection
**Then** it includes at least 20 occurrences and 30 typed Connections
**And** it includes native Symbols, one complex SVG-backed Element, typed Ports, explicit bindings, a
Network or junction, bundles, obstacles, labels, and Cabinet installation facts
**And** the Cabinet source opens source-first without editor lifecycle side effects
**And** the sample contains no XML authority, raw SVG metadata authority, unresolved Port or Anchor
bridge, or generic fallback component.

### Story 5.4: Prove Cabinet Compilation End To End

As an evaluator,
I want structural and visual E2E evidence for the M36 Cabinet sample,
So that professional rendering is verified rather than asserted.

**Requirements:** FR-35, FR-36.

**Acceptance Criteria:**

**Given** the M36 Cabinet sample on supported desktop and narrow viewports
**When** end-to-end verification runs
**Then** it shows nonblank output, contained placement, readable density, orthogonal routes, labels,
and correct junction and crossing treatment
**And** structural proof confirms traceability, resolved bindings, valid placement, valid channel
routing, and complete RouteFact evidence
**And** product smoke proves zero LSP diagnostics, unapproved fallback routes, route or body
intersections, required off-channel segments, XML authority, and raw SVG metadata authority
**And** same source and pinned snapshots reproduce equivalent PlacementFacts and RouteFacts
**And** tests fail when a required bridge, placement constraint, channel rule, or route proof field is
removed.
