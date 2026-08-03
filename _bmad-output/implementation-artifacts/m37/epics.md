---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/review-rubric.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md
  - _bmad-output/implementation-artifacts/m36/retrospective.md
---

# Athena - M37 Epic Breakdown

## Overview

This document decomposes Athena M37 Engineering Connectivity and Professional Routing Grammar into
implementation epics and stories. One Athena semantic source remains authoritative. The professional
connection drawing is the only M37 visible quality surface; a non-visible Cabinet lowering test
proves that the same source facts remain projection-neutral.

## Requirements Inventory

### Functional Requirements

FR-1: Directly evolve and rename the M36 Connectable Entity Contract into the minimum typed
Engineering Connectivity Contract with stable IDs, Interfaces, Ports, compatibility, optional
physical references, representation bindings, external evidence, and source provenance. Do not
create a parallel model or compatibility adapter.

FR-2: Group related Ports into typed Interfaces with explicit group defaults and source-spanned
Port overrides.

FR-3: Keep manufacturer lifecycle, procurement, BOM ownership, datasheet management, simulation,
and product-replacement knowledge outside the M37 Engineering Connectivity Contract.

FR-4: Keep Engineering Connectivity Contracts, Interfaces, Ports, Connections, Connection Intents,
External Evidence Mappings, and provenance projection-neutral and source-owned.

FR-5: Author typed Connection Intent with class, priority, separation, preferred drawing region or
physical channel, and label policy; resolve deterministic precedence and forbid endpoint- or
geometry-derived intent inference.

FR-6: Lower Connection Intent into transient Route Intent and planner constraints while preserving
owner, strength, source span, and diagnostic identity.

FR-7: Record the Connection Intent influence in every accepted RouteFact.

FR-8: Attach typed, source-spanned External Evidence Mappings to connectivity and policy facts
without allowing evidence to create engineering facts.

FR-9: Keep AML/XML as import or external evidence only; no active XML runtime authority is allowed.

FR-10: Document the future importer boundary and prove one IEC citation plus one neutral external
classification reference without implementing AML/ECLASS resolvers.

FR-11: Model compiler-owned Route Lanes with capacity, spacing, orientation, occupancy, and
collision evidence inside Route Channels.

FR-12: Allocate lanes deterministically from Route Intent, priority, capacity, grouping, and stable
tie-breakers.

FR-13: Report channel occupancy, over-capacity conditions, lane assignments, conflicts, and degraded
route reasons.

FR-14: Define compiler-owned Route Quality Policy schema and hard rejects with typed
profile-authored values for crossings, bends, lane changes, labels, length, and density.

FR-15: Emit quality grade, score components, degraded reasons, lane, channel sequence, planner, and
compiler snapshot for every accepted RouteFact.

FR-16: Surface route-quality diagnostics through compiler, LSP, structured proof, and invalid
fixtures; do not silently accept weak routes.

FR-17: Define typed Drawing Standard Profiles covering sheet frame, coordinate grid, title block,
text scale, stroke classes, junction and crossing rules, and designation labels.

FR-18: Resolve every visible connection to a traceable Connection Presentation Class defining
stroke weight, style, color, endpoints, labels, and crossing behavior.

FR-19: Reject every engineering connection line with an unresolved start or end; loose endpoints
are allowed only for explicitly authored graphical annotation.

FR-20: Render junction dots, disconnected crossings, wire hops, terminal joins, and bus taps only
from explicit semantic or route facts.

FR-21: Compute label bounds and collision evidence and diagnose label overlap with bodies, routes,
title blocks, or other labels.

FR-22: Model typed Projection Policy input selecting target surface, layout strategy, presentation
profile, route policy, and proof obligations without owning engineering truth.

FR-23: Lower the same M37 source through professional connection drawing and Cabinet Projection
Policies while preserving identical semantic identities, topology, intent, evidence, and provenance.

FR-24: Reject any projection, renderer payload, SVG, or planner graph that redefines Ports,
Connections, Connection Intent, compatibility, or evidence.

FR-25: Keep planner graphs and proposals transient, snapshot-bound derived IR.

FR-26: Compare candidate routes or placements through deterministic Route Quality Policy.

FR-27: Reject incomplete or mismatched planner proposals; prove the Athena-native planner and defer
ELK integration.

FR-28: Validate the SVG geometry-reference schema, duplicate references, unsupported
`data-athena-*` keys, missing geometry, and external resource attempts.

FR-29: Keep simple Symbols native in Athena and admit complex vendor geometry through package-local
SVG only.

FR-30: Keep package-local resource resolution as the default and defer remote resource runtime.

FR-31: Create a dedicated `examples/m37/...` source-first professional connection drawing sample
with frame, grid, title block, supply rails, terminals, PE, contacts, coils, indicators, and
orthogonal wiring.

FR-32: Enforce zero loose endpoints, fallback anchors, route/body intersections, ambiguous
crossings, label/body overlaps, label/title-block overlaps, and unclassified visible routes in the
accepted valid proof.

FR-33: Trace every occurrence, Interface, Port, Anchor, lane, RouteFact, RouteLabelFact, Connection
Intent, presentation class, and External Evidence Mapping to Athena source and package resources.

FR-34: Capture desktop and narrow Electron screenshots plus computed structural evidence; hardcoded
proof-success booleans are forbidden.

FR-35: Enforce source-set hygiene against proof/demo/sample/milestone/version names and fixtures in
production `src/main`.

FR-36: Directly remove or refactor stale XML, fallback, renderer-authority, compatibility,
endpoint-derived intent, hardcoded route policy, hardcoded projection/view selection, and asserted
proof-success paths.

### NonFunctional Requirements

NFR-1: Athena source is the single engineering metadata authority.

NFR-2: Invalid contracts, Ports, Anchors, intent, evidence, lanes, Projection Policy, and quality
references fail during compiler or LSP validation with source spans.

NFR-3: The same source, package snapshot, planner version, and policy produce deterministic
PlacementFacts, RouteFacts, RouteLabelFacts, and stable screenshots.

NFR-4: Every accepted or rejected route candidate has source-spanned explainability evidence.

NFR-5: The renderer paints Graphic Primitive IR and does not infer or repair engineering routes.

NFR-6: SVG admission remains fail-closed for scripts, external references, unsafe entities, and
unsupported Athena keys.

NFR-7: After the IDE is ready, the M37 compile-to-presentation refresh completes within 10 seconds
on the supported development workstation while the UI remains responsive.

NFR-8: Athena is pre-public; incompatible legacy paths are removed rather than protected by
compatibility shims.

### Additional Requirements

- Evolve existing M36 `connection-model` contracts in place; do not add a new module or dual
  contract hierarchy.
- Keep source Connection Intent in `connection-model`; lower derived Route Intent and route facts
  into `routing-model` through `compiler`.
- Extend the parser, AST, tree-sitter grammar, formatter/highlighting, LSP diagnostics, compiler, and
  negative fixtures together for each new Athena syntax surface.
- Keep constraint owner and strength explicit across semantic, representation, physical, and layout
  preference layers.
- Keep Projection Policy selection compiler-owned and source/profile-traceable; remove hardcoded
  schematic context and control-drawing view selection.
- Extend existing professional drawing, sheet composition, presentation, and Graphic Primitive IR
  paths; do not create another renderer or representation IR.
- Keep Drawing Standard Profile and Connection Presentation Class typed and source-spanned through
  presentation transport.
- Replace `ElectricalConnectionIntentClassifier` inference in the active professional drawing path
  with authored and validated Connection Intent.
- Replace fixed route-constraint injection with compiler hard rules plus selected typed profile
  values.
- Compute all proof evidence from actual compiled facts and diagnostics; do not return success
  constants.
- Use the Athena-native deterministic planner for M37 and add Planner SPI conformance tests before
  any ELK adapter work.
- Use `draft/screenshort/equipement_d'un_volet_roulant.png` only as a visual/domain reference; do not
  copy QET XML, persistence, editor, or runtime architecture.
- Keep the dedicated sample under `examples/m37`; do not reuse M36 as the active proof project.
- Rebuild kernel/LSP and the Theia frontend before final Electron E2E so stale bundles cannot pass or
  fail the milestone incorrectly.
- Run source-set hygiene and encoding audits throughout development and full root, runtime,
  tree-sitter, frontend, LSP, IDE, and Electron verification before completion.

### UX Design Requirements

No separate M37 UX design contract exists. Stories preserve the existing Theia workbench and add no
new graphic editor. The professional connection drawing is a compiled inspection surface with
desktop and narrow responsive evidence.

### FR Coverage Map

FR-1: M37-E1 - evolve the existing connectivity contract in place.
FR-2: M37-E1 - group Ports into typed Interfaces.
FR-3: M37-E1 - preserve the full ECS boundary.
FR-4: M37-E1 - keep connectivity facts projection-neutral.
FR-5: M37-E1 - author deterministic Connection Intent.
FR-6: M37-E1 - lower intent with owner, strength, and provenance.
FR-7: M37-E1 - trace intent influence into RouteFacts.
FR-8: M37-E2 - attach typed external evidence.
FR-9: M37-E2 - keep AML/XML outside runtime authority.
FR-10: M37-E2 - document and prove the external mapping boundary.
FR-11: M37-E3 - model lanes and occupancy.
FR-12: M37-E3 - allocate lanes deterministically.
FR-13: M37-E3 - report channel occupancy and conflicts.
FR-14: M37-E3 - define route-quality policy ownership and rules.
FR-15: M37-E3 - emit complete route-quality evidence.
FR-16: M37-E3 - expose weak-route diagnostics.
FR-17: M37-E4 - define Drawing Standard Profiles.
FR-18: M37-E4 - classify and trace line presentation.
FR-19: M37-E4 - reject loose connection endpoints.
FR-20: M37-E4 - render explicit junction and crossing semantics.
FR-21: M37-E4 - compute label bounds and collisions.
FR-22: M37-E2 - define typed Projection Policy input.
FR-23: M37-E2 - prove same-source multi-projection lowering.
FR-24: M37-E2 - reject projection-specific engineering truth.
FR-25: M37-E3 - keep planner data transient.
FR-26: M37-E3 - compare planner candidates deterministically.
FR-27: M37-E3 - reject planner mismatch and defer ELK.
FR-28: M37-E6 - harden SVG geometry references.
FR-29: M37-E6 - preserve native Symbols and package-local SVG.
FR-30: M37-E6 - preserve package-local resource defaults.
FR-31: M37-E5 - create the dedicated M37 sample.
FR-32: M37-E5 - enforce professional structural route gates.
FR-33: M37-E5 - prove complete source-to-graphic trace.
FR-34: M37-E5 - capture screenshot and computed E2E evidence.
FR-35: M37-E6 - enforce source-set hygiene.
FR-36: M37-E6 - remove stale authority and hardcoded proof paths.

## Epic List

### M37-E1: Author Governed Connectivity And Intent

Engineers and AI agents can author grouped Interfaces, typed Ports, and explicit Connection Intent
that compile through the evolved M36 connectivity contract without inference or duplicate models.

**FRs covered:** FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7.

### M37-E2: Project One Source With External Evidence

Engineers can attach standards and classification evidence, select a typed Projection Policy, and
prove that professional drawing and Cabinet projections consume the same semantic truth.

**FRs covered:** FR-8, FR-9, FR-10, FR-22, FR-23, FR-24.

### M37-E3: Produce Deterministic Professional Routes

Engineers receive lane-aware route candidates that Athena validates, scores, explains, and accepts
deterministically without giving planner technology authority.

**FRs covered:** FR-11, FR-12, FR-13, FR-14, FR-15, FR-16, FR-25, FR-26, FR-27.

### M37-E4: Compile Professional Drawing Grammar

Engineering connections compile into standard-profiled lines with exact endpoint attachment,
explicit junction/crossing semantics, traceable line presentation, and collision-aware labels.

**FRs covered:** FR-17, FR-18, FR-19, FR-20, FR-21.

### M37-E5: Inspect And Prove The Professional Drawing

An evaluator can open a dedicated source-first M37 example and inspect computed structural evidence,
source trace, diagnostics, and responsive screenshots for one professional connection drawing.

**FRs covered:** FR-31, FR-32, FR-33, FR-34.

### M37-E6: Preserve Safe Geometry And Athena Authority

Package authors can use native Symbols and complex package-local SVG geometry while the platform
fails closed on unsafe references and removes every stale or fabricated production authority path.

**FRs covered:** FR-28, FR-29, FR-30, FR-35, FR-36.

## M37-E1: Author Governed Connectivity And Intent

Engineers and AI agents can author grouped Interfaces, typed Ports, and explicit Connection Intent
that compile through the evolved M36 connectivity contract without inference or duplicate models.

### Story 1.1: Evolve The Connectivity Contract In Place

As an engineering author,
I want one stable Engineering Connectivity Contract vocabulary,
So that source, compiler, and AI tooling do not encounter two competing connectivity models.

**Requirements:** FR-1, FR-3, FR-4.

**Acceptance Criteria:**

**Given** the M36 Connectable Entity Contract and its production consumers
**When** M37 evolves the contract
**Then** existing contract types and consumers are directly renamed and extended as Engineering
Connectivity Contract behavior
**And** contract identity, Interfaces, Ports, compatibility, physical references, representation
bindings, evidence references, and provenance remain typed
**And** manufacturer lifecycle, procurement, BOM, datasheet, simulation, and replacement facts stay
outside the contract
**And** no old contract type, adapter, compatibility facade, duplicate lowering path, or
milestone-named production class remains
**And** focused model/compiler tests plus the source-set hygiene and encoding audits pass.

### Story 1.2: Author Grouped Interfaces And Ports

As an engineering author or AI agent,
I want to declare related Ports inside typed Interfaces,
So that connection surfaces are readable, reusable, and validated as coherent groups.

**Requirements:** FR-2, FR-4.

**Acceptance Criteria:**

**Given** Athena source declares power, signal, protective-earth, fieldbus, or service Interfaces
**When** parser, tree-sitter, semantic validation, and LSP processing run
**Then** each Interface and Port receives stable identity, source span, direction, signal role,
compatibility, and provenance
**And** Interface defaults apply only to members without explicit Port overrides
**And** duplicate members, invalid overrides, missing references, and incompatible group defaults
produce matching compiler and LSP diagnostics
**And** formatter/highlighting, corpus tests, and valid and invalid source fixtures cover the syntax
**And** focused tests plus the source-set hygiene and encoding audits pass.

### Story 1.3: Author Deterministic Connection Intent

As an engineering author or AI agent,
I want to declare how a Connection should be treated without drawing geometry,
So that routing and presentation follow explicit engineering intent rather than guesses.

**Requirements:** FR-5.

**Acceptance Criteria:**

**Given** a Connection, route group, Interface, or selected profile supplies Connection Intent
**When** intent resolution runs
**Then** the model supports intent class, priority, separation, preferred drawing region or physical
channel, and route-label policy with source spans
**And** precedence is Connection, route group, Interface default, then selected profile default
**And** same-level conflicts, missing required intent, and incompatible intent fail with typed
compiler and LSP diagnostics
**And** endpoint types, SVG geometry, visual appearance, and renderer state never infer intent
**And** parser, tree-sitter, formatter/highlighting, compiler, and negative fixtures evolve together
**And** focused tests plus the source-set hygiene and encoding audits pass.

### Story 1.4: Lower Intent Into Traceable Route Facts

As an engineer inspecting a compiled connection,
I want its authored intent preserved through route planning,
So that I can explain why Athena selected its region, channel, label policy, and route class.

**Requirements:** FR-6, FR-7.

**Acceptance Criteria:**

**Given** validated Connection Intent and typed connection endpoints
**When** Engineering Lowering and route planning run
**Then** transient Route Intent preserves owner, strength, source span, diagnostic identity, and
provenance without final geometry
**And** every accepted RouteFact identifies the Connection Intent and each influenced channel,
region, separation rule, label policy, and presentation class decision
**And** the active professional drawing path consumes authored intent and no longer invokes
endpoint-derived intent classification as fallback
**And** invalid intent prevents route acceptance before rendering
**And** focused lowering, route, and regression tests plus the source-set hygiene and encoding audits
pass.

## M37-E2: Project One Source With External Evidence

Engineers can attach standards and classification evidence, select a typed Projection Policy, and
prove that professional drawing and Cabinet projections consume the same semantic truth.

### Story 2.1: Attach Typed External Evidence

As an engineering package author,
I want to attach external references to Athena-owned connectivity facts,
So that standards and classifications remain inspectable evidence without becoming runtime truth.

**Requirements:** FR-8, FR-9, FR-10.

**Acceptance Criteria:**

**Given** source declares External Evidence Mappings on a contract, Interface, Port, Connection
Intent, or policy
**When** parser, semantic validation, and LSP processing run
**Then** each mapping carries typed namespace, reference, subject, source span, and provenance
**And** an unknown namespace, invalid reference shape, duplicate mapping, or invalid subject fails
with typed diagnostics
**And** evidence cannot create Ports, Interfaces, Anchors, intent, compatibility, or RouteFacts
**And** the valid fixture includes one IEC citation and one neutral classification reference without
an XML, AML, or ECLASS runtime parser
**And** importer-boundary documentation explains future lowering into Athena facts rather than
copying external schemas into the kernel
**And** focused parser, compiler, LSP, and negative tests plus the source-set hygiene and encoding
audits pass.

### Story 2.2: Select A Typed Projection Policy

As an engineering author,
I want to select a projection through typed Athena policy,
So that target view, layout, presentation, route policy, and proof obligations are explicit and
traceable.

**Requirements:** FR-22, FR-24.

**Acceptance Criteria:**

**Given** Athena source selects a Projection Policy
**When** the source compiles
**Then** the policy resolves target surface, layout strategy, Drawing Standard Profile, Route
Quality Policy, and proof obligations with stable identity and source provenance
**And** the active professional drawing compiler receives the selected policy instead of hardcoding
schematic context or control-drawing view selection
**And** Projection Policy cannot create or redefine Ports, Connections, intent, compatibility, or
external evidence
**And** invalid targets, missing profiles, incompatible policies, and duplicate engineering facts
fail through compiler and LSP diagnostics
**And** parser, tree-sitter, formatter/highlighting, compiler, and negative fixtures evolve together
**And** focused tests plus the source-set hygiene and encoding audits pass.

### Story 2.3: Prove Projection-Neutral Semantic Truth

As an engineering platform evaluator,
I want the same source compiled through different Projection Policies,
So that Athena demonstrates one semantic truth rather than separate Cabinet and drawing worlds.

**Requirements:** FR-4, FR-23, FR-24.

**Acceptance Criteria:**

**Given** the dedicated M37 source and its professional drawing and Cabinet Projection Policies
**When** structural compilation runs for both policies
**Then** both outputs preserve identical contract, Interface, Port, Connection, Connection Intent,
evidence, and provenance identities
**And** only placement, routing, and presentation facts permitted by each policy differ
**And** the professional connection drawing remains the only M37 visible product-quality surface
**And** a fixture that attempts to redefine engineering truth inside a projection fails before
planning or rendering
**And** a structured comparison records the shared semantic snapshot and policy-specific derived
snapshots
**And** focused projection and regression tests plus the source-set hygiene and encoding audits pass.

## M37-E3: Produce Deterministic Professional Routes

Engineers receive lane-aware route candidates that Athena validates, scores, explains, and accepts
deterministically without giving planner technology authority.

### Story 3.1: Allocate Route Lanes Deterministically

As an engineer reviewing a dense connection drawing,
I want related routes assigned to governed lanes,
So that wire columns and channels remain readable and capacity conflicts are explicit.

**Requirements:** FR-11, FR-12, FR-13.

**Acceptance Criteria:**

**Given** valid Route Intent, channel geometry, route groups, and priority
**When** lane allocation runs
**Then** each Route Channel exposes typed lanes with capacity, spacing, orientation, occupancy, and
collision evidence
**And** allocation uses intent, priority, grouping, capacity, and stable tie-breakers to produce the
same result for the same snapshot
**And** accepted RouteFacts identify selected lane and channel occupancy
**And** over-capacity, incompatible orientation, spacing conflicts, and impossible allocation produce
typed diagnostics without fallback geometry
**And** focused lane, determinism, and boundary tests plus the source-set hygiene and encoding audits
pass.

### Story 3.2: Author And Validate Route Quality Policy

As a drawing-profile author,
I want explicit route-quality rules and weights,
So that professional readability is governed and testable rather than subjective renderer behavior.

**Requirements:** FR-14.

**Acceptance Criteria:**

**Given** a typed Drawing Standard Profile declares Route Quality Policy values
**When** policy compilation runs
**Then** the compiler-owned schema distinguishes non-negotiable hard rejects from scored preferences
**And** hard rejects cover missing anchors, semantic incompatibility, route/body intersection,
invalid channel use, and violated clearance
**And** soft scoring covers crossings, bends, lane changes, label collisions, length, density, and
stable tie-breaking
**And** project intent may influence preferences but cannot redefine the schema or weaken hard
rejects
**And** invalid weights, unknown criteria, and attempts to disable hard rejects fail with source-
spanned diagnostics
**And** focused policy and negative tests plus the source-set hygiene and encoding audits pass.

### Story 3.3: Validate And Compare Planner Candidates

As an engineer or AI agent,
I want Athena to compare planner proposals under its own rules,
So that route optimization remains replaceable and non-authoritative.

**Requirements:** FR-25, FR-26, FR-27.

**Acceptance Criteria:**

**Given** a snapshot-bound transient Layout Graph and one or more Athena-native planner proposals
**When** proposal normalization and validation run
**Then** proposals remain disposable derived IR and cannot mutate source, identity, representation,
or policy
**And** Athena rejects missing endpoints, Anchors, lanes, constraints, snapshot evidence, or proof
fields before candidate scoring
**And** valid candidates are compared deterministically through Route Quality Policy and stable
tie-breakers
**And** rejection records planner identity, snapshot, violated facts, and source provenance
**And** the Planner SPI conformance suite proves the Athena-native planner while no ELK runtime
dependency or adapter is introduced
**And** focused planner, mismatch, and determinism tests plus the source-set hygiene and encoding
audits pass.

### Story 3.4: Emit Explainable Route Quality Evidence

As an engineer inspecting a route,
I want its quality and degradation evidence exposed through compiler and IDE protocols,
So that weak routing can be corrected from source or policy.

**Requirements:** FR-15, FR-16.

**Acceptance Criteria:**

**Given** Athena accepts or rejects a route candidate
**When** route-quality evidence is emitted
**Then** accepted RouteFacts include grade, score components, lane, channel sequence, Connection
Intent influence, planner identity, compiler snapshot, and provenance
**And** rejected candidates and degraded invalid fixtures produce machine-readable compiler and LSP
diagnostics with affected source spans
**And** a valid accepted route cannot be marked successful when a hard reject or unresolved proof
field exists
**And** normalized LSP/presentation payloads contain Athena facts only and no planner-native object
**And** focused route-fact, diagnostic, protocol, and invalid-fixture tests plus the source-set hygiene
and encoding audits pass.

## M37-E4: Compile Professional Drawing Grammar

Engineering connections compile into standard-profiled lines with exact endpoint attachment,
explicit junction/crossing semantics, traceable line presentation, and collision-aware labels.

### Story 4.1: Compile Drawing Profiles And Line Classes

As a professional drawing author,
I want typed drawing profiles and connection presentation classes,
So that line appearance and sheet grammar are governed by source instead of renderer defaults.

**Requirements:** FR-17, FR-18.

**Acceptance Criteria:**

**Given** a Drawing Standard Profile and Connection Presentation Classes are declared in Athena
source
**When** profile and projection compilation run
**Then** the profile resolves frame, grid, title-block regions, text scale, stroke classes, junction
rules, crossing rules, and designation-label policy
**And** each line class resolves stroke weight, stroke style, color, endpoint behavior, label policy,
and crossing behavior
**And** every visible engineering Connection resolves exactly one class from Connection Intent and
the selected profile
**And** profile and class identity, source span, and selection evidence survive through Graphic
Primitive IR and presentation transport
**And** missing, duplicate, incompatible, or unclassified presentation produces typed diagnostics
**And** parser, tree-sitter, compiler, renderer-adapter, and protocol tests plus the source-set hygiene
and encoding audits pass.

### Story 4.2: Enforce Exact Endpoint Attachment

As an engineer reviewing a compiled drawing,
I want every engineering line attached to a valid endpoint fact,
So that no random line start or end can be mistaken for a real connection.

**Requirements:** FR-19.

**Acceptance Criteria:**

**Given** a visible engineering Connection is lowered for presentation
**When** endpoint attachment validation runs
**Then** its start and end resolve exactly to an Anchor, terminal, bus, junction, or sheet reference
**And** the first and last rendered segment coordinates equal the validated endpoint facts after
projection transforms
**And** unresolved, ambiguous, center/fallback, detached, or body-interior endpoints produce blocking
diagnostics and no accepted route
**And** a loose line is permitted only when authored and typed as graphical annotation rather than
engineering connectivity
**And** the renderer cannot synthesize, snap, or repair endpoints
**And** focused endpoint, transform, negative, and rendering-adapter tests plus the source-set hygiene
and encoding audits pass.

### Story 4.3: Compile Junction And Crossing Semantics

As an engineer reading a connection drawing,
I want joins and crossings rendered from explicit topology,
So that visual intersection never changes electrical or engineering meaning.

**Requirements:** FR-20.

**Acceptance Criteria:**

**Given** connection networks and route intersection facts
**When** drawing grammar compilation runs
**Then** junction dots, terminal joins, bus taps, disconnected crossings, and wire hops are emitted
only from explicit semantic or route facts
**And** a geometric crossing without a join fact remains disconnected
**And** a join fact emits the profile-selected marker and carries network, route, source, and policy
trace
**And** ambiguous or contradictory join/crossing evidence fails before rendering
**And** the renderer paints the compiled marker primitives without topology inference
**And** focused topology, crossing, marker, and negative tests plus the source-set hygiene and
encoding audits pass.

### Story 4.4: Compile Collision-Aware Route Labels

As an engineer reading route and device labels,
I want label placement validated against the drawing structure,
So that labels remain legible and their quality evidence is real.

**Requirements:** FR-21.

**Acceptance Criteria:**

**Given** accepted routes, device occurrences, sheet regions, and a selected label policy
**When** label placement and validation run
**Then** every RouteLabelFact carries content, bounds, attachment point, class, collision evidence,
source provenance, and compiler snapshot
**And** labels are checked against device bodies, route segments, other labels, frame, grid labels,
and title-block regions
**And** collision-free placement is deterministic for the same source and policy
**And** unresolved or colliding labels in the valid proof block acceptance rather than setting a
hardcoded clearance-success flag
**And** focused label, bounds, collision, determinism, and negative tests plus the source-set hygiene
and encoding audits pass.

## M37-E5: Inspect And Prove The Professional Drawing

An evaluator can open a dedicated source-first M37 example and inspect computed structural evidence,
source trace, diagnostics, and responsive screenshots for one professional connection drawing.

### Story 5.1: Author The Dedicated M37 Example

As an evaluator,
I want a dedicated professional control-drawing example authored in Athena,
So that M37 is proven from realistic semantic source rather than reused fixtures or mocked payloads.

**Requirements:** FR-31.

**Acceptance Criteria:**

**Given** the new `examples/m37` project
**When** its package and source hierarchy are inspected and compiled
**Then** package names follow the filesystem hierarchy and all resources resolve package-locally
**And** the source declares grouped Interfaces, Ports, Connections, Connection Intent, evidence,
representation bindings, Projection Policies, and professional drawing profiles
**And** the source models supply rails, terminals, protective earth, contacts, coils, indicators,
and connection topology suitable for the rolling-shutter reference composition
**And** the example contains no XML authority, copied QET element format, reused M36 active proof,
mock presentation payload, or generic fallback component
**And** the valid project compiles with zero blocking or degraded diagnostics
**And** sample compilation plus the source-set hygiene and encoding audits pass.

### Story 5.2: Compile The Professional Drawing Surface

As an evaluator,
I want the M37 source compiled into one complete professional drawing sheet,
So that semantic connectivity and drawing grammar can be judged together.

**Requirements:** FR-31, FR-32.

**Acceptance Criteria:**

**Given** the dedicated M37 source and professional drawing Projection Policy
**When** the compilation pipeline produces presentation output
**Then** the sheet includes frame, coordinate grid, title block, supply rails, terminal groups,
protective earth, device contacts, coils, indicators, labels, and orthogonal wiring
**And** every visible route has validated endpoints, Connection Intent, lane or wire-column evidence,
quality evidence, and Connection Presentation Class
**And** the valid output has zero loose endpoints, fallback anchors, route/body intersections,
ambiguous crossings, label/body overlaps, label/title-block overlaps, and unclassified routes
**And** the renderer consumes only compiled Graphic Primitive IR and performs no engineering or route
inference
**And** structural composition and renderer tests plus the source-set hygiene and encoding audits
pass.

### Story 5.3: Expose Computed Source And Route Evidence

As an engineer inspecting the M37 drawing,
I want every visual fact traced to its governing source and compiler decision,
So that humans and AI can explain and correct the engineering result.

**Requirements:** FR-33, FR-34.

**Acceptance Criteria:**

**Given** a compiled M37 drawing occurrence, route, label, marker, or sheet structure
**When** trace and proof data cross compiler, LSP, runtime, and Theia protocols
**Then** the data identifies semantic subject, contract, Interface, Port, Anchor, Connection Intent,
lane, RouteFact, RouteLabelFact, presentation class, evidence mapping, policy, package resource,
source span, and compiler snapshot where applicable
**And** proof fields for endpoint attachment, clearance, fallback absence, crossing semantics,
renderer purity, and source authority are computed from compiled facts and diagnostics
**And** removing or corrupting a required fact causes the corresponding proof to fail
**And** no success field is a constant and no raw planner, SVG markup, XML, or DOM object crosses the
protocol
**And** focused trace, protocol, mutation, and negative tests plus the source-set hygiene and
encoding audits pass.

### Story 5.4: Prove M37 End To End With Screenshots

As a milestone evaluator,
I want repeatable structural and visual E2E evidence,
So that professional rendering is demonstrated rather than claimed.

**Requirements:** FR-32, FR-34, NFR-3, NFR-7.

**Acceptance Criteria:**

**Given** freshly rebuilt kernel, LSP, Theia frontend, and dedicated M37 project
**When** Electron E2E runs at desktop and narrow viewports
**Then** the professional connection drawing surface is active, nonblank, responsive, and
source-backed
**And** structural assertions prove every zero-defect gate from FR-32 and complete trace from FR-33
**And** a post-ready compile-to-presentation refresh completes within 10 seconds on the supported
development workstation
**And** deterministic reruns preserve semantic and route snapshots and stable screenshots within the
declared tolerance
**And** screenshots are stored under `_bmad-output/implementation-artifacts/m37/screenshots`
**And** E2E fails when a required endpoint, line class, intent, lane, label bound, marker, trace, or
proof field is removed
**And** Electron E2E plus the source-set hygiene and encoding audits pass.

## M37-E6: Preserve Safe Geometry And Athena Authority

Package authors can use native Symbols and complex package-local SVG geometry while the platform
fails closed on unsafe references and removes every stale or fabricated production authority path.

### Story 6.1: Harden Package-Local Geometry References

As a representation package author,
I want native Symbols and complex SVG geometry admitted through one safe contract,
So that visual complexity does not create another engineering language or resource authority.

**Requirements:** FR-28, FR-29, FR-30.

**Acceptance Criteria:**

**Given** a native Athena Symbol or package-local SVG-backed Element
**When** representation resource compilation runs
**Then** native primitives and SVG geometry resolve through the same Athena-owned Anchor contract
**And** SVG recognizes only the allowed geometry-reference schema, including
`data-athena-ref="anchor:<id>"`, without Port, signal, direction, compatibility, or intent meaning
**And** duplicate references, unsupported `data-athena-*` keys, missing geometry, path escape,
external resources, scripts, handlers, unsafe entities, and raw-markup transport fail closed
**And** package-local resolution is the only active resource scheme and remote URI/registry runtime
remains deferred
**And** a complex vendor-like SVG fixture and simple native Symbol fixture both compile with complete
source and package trace
**And** focused security, resource, geometry, and negative tests plus the source-set hygiene and
encoding audits pass.

### Story 6.2: Purge Stale And Fabricated Authority Paths

As a platform maintainer,
I want conflicting production paths removed directly,
So that M37 has one understandable compiler architecture and proof cannot report invented success.

**Requirements:** FR-35, FR-36, NFR-8.

**Acceptance Criteria:**

**Given** the active professional drawing and M37 compilation paths
**When** architecture cleanup is completed
**Then** endpoint-derived intent classification, fixed drawing route-policy injection, hardcoded
projection/view selection, asserted proof-success constants, stale XML authority, renderer repair,
fallback routing, and compatibility shims are deleted or directly refactored
**And** no `Proof`, `Demo`, `Sample`, milestone name, `V0`/`V1`, or test fixture exists in production
`src/main`
**And** valid reusable logic is named by responsibility and test-only evidence remains under
`src/test`, E2E, or milestone artifact directories
**And** no production class retains a deprecated parallel path solely to keep pre-M37 behavior
working
**And** code comments and module documentation explain only current authority boundaries and flows
**And** source-set hygiene, encoding, focused regression, and dead-path audits pass.

### Story 6.3: Close The M37 Architecture And Regression Gate

As a milestone owner,
I want a complete clean verification pass and implementation handoff,
So that M37 can be developed and reviewed without hidden legacy or unverified claims.

**Requirements:** FR-35, FR-36, NFR-1 through NFR-8.

**Acceptance Criteria:**

**Given** all M37 feature stories are implemented
**When** the final architecture and verification gate runs sequentially
**Then** root Gradle tests, runtime tests, tree-sitter corpus and Node tests, Theia frontend tests,
LSP `installDist`, IDE build, source-set hygiene, encoding audit, and `git diff --check` pass
**And** Electron M37 E2E passes after both LSP/kernel and Theia frontend rebuilds
**And** architecture audit finds one Athena source authority, transient planner IR, computed proof,
paint-only rendering, package-local SVG geometry, and no XML runtime authority
**And** every FR and NFR has fresh test, diagnostic, structured proof, documentation, or E2E evidence
**And** the M37 retrospective, usage handoff, screenshot index, and final sprint status record only
verified outcomes
**And** no completion claim is made while a required command, story, or epic remains incomplete.
