---
title: Athena M37 - Engineering Connectivity And Professional Routing Grammar
status: final
created: 2026-07-30
updated: 2026-07-30
---

# PRD: Athena M37 - Engineering Connectivity And Professional Routing Grammar

## 0. Document Purpose

This PRD defines M37 after M36 completed Engineering Connectivity Semantics and exposed the next
product gap: the route and drawing result is structurally compiled but not yet professional.
Connections can still look visually random, line endpoints are not disciplined enough, and the
rendered surface does not yet match the controlled grammar seen in professional IEC/QET-style
engineering drawings.

M37 keeps Athena source as the single source of truth for every projection. Cabinet, schematic,
control drawing, documentation, and future views must all compile from the same semantic source and
derived compiler pipeline. No projection may invent its own source world. M37 does not turn SVG,
XML, ELK, renderer state, or external standards into runtime authority. It introduces a minimal typed
Engineering Connectivity Contract layer, External Evidence Mapping, Connection Intent,
route-quality policy, professional connection presentation profiles, and a repeatable drawing proof
that downstream architecture, epics, and stories can implement without drifting into a full vendor
catalog or another graphics editor.

The Engineering Connectivity Contract is a direct evolution and rename of M36's Connectable Entity
Contract. M37 shall refactor the existing contract and consumers in place. It shall not add a second
contract model, adapter, compatibility facade, or parallel lowering path.

## 1. Vision

M34 made representation authoring typed. M35 established the Physical Installation Model. M36
established typed connectivity and route facts. M37 should now make those facts useful enough for
real engineering work: components must expose stable interfaces, routes must be planned and judged
by explicit engineering policy, and external standards such as AML, IEC references, and product
classification systems must attach as evidence without becoming Athena's language.

The milestone is not "draw nicer lines" as a renderer task. M37 establishes professional routing
grammar on top of M36 connectivity. It gives Athena the vocabulary to say what
a connectable engineering object exposes, which ports participate in which interfaces, what
Connection Intent applies, what external evidence is attached, which route policies apply, and why
the compiler accepted or rejected the final route.

The visible product proof remains one focused surface, not three, but the proof must still be a
projection of the same Athena semantic source. If the acceptance target is the QET-style
rolling-shutter drawing reference, M37 should use a dedicated professional connection drawing sheet
as the proof surface rather than pretending Cabinet layout alone can match a schematic sheet. The
same source must remain capable of lowering toward Cabinet or other future projections through typed
projection policies. A professional M37 drawing should show disciplined endpoint attachment, wire
classes, junction/crossing rules, line style profiles, readable labels, sheet frame/grid/title block,
and strong source-to-graphic explanation. If the visual result is weak, the fix belongs upstream in
Engineering Connectivity Contracts, route intent, drawing grammar, route scoring, layout facts, or
presentation profiles, not in renderer guesswork.

## 2. Target User

### 2.1 Jobs To Be Done

- As an engineering author, define a component's connectable interfaces in typed Athena source so AI
  and humans can reason about ports, signals, and compatibility.
- As a package author, attach external classification and standards evidence to Athena-owned
  connectivity facts without importing foreign XML or catalog models as authority.
- As a drawing author, compile readable professional connection lines with explicit endpoint,
  junction, crossing, style, and label evidence.
- As a platform engineer, integrate optional planner technology such as ELK behind an Athena-owned
  Planner SPI and validator.
- As an AI engineering agent consumer, trace from any rendered route or component occurrence back to
  the Engineering Connectivity Contract, Connection Intent, route facts, External Evidence Mapping,
  and source file.

### 2.2 Non-Users (M37)

- Users needing a complete EPLAN/Data Portal replacement.
- Users needing a full AutomationML importer/exporter.
- Users needing multi-view Schematic, Documentation, and Cabinet parity.
- Users needing AI auto-layout or AI component replacement.

### 2.3 Key User Journey

- **UJ-1. Aaron reviews a professional connection drawing proof.**
  Aaron opens the dedicated M37 example. The IDE opens one professional connection drawing surface.
  He inspects a route from a supply line through terminals, devices, contacts, coils, and indicators.
  The UI can report the source Engineering Connectivity Contract, ports, anchors, route intent, selected drawing
  policy, line class, route quality, external evidence, and compiler diagnostics. The drawing is
  readable enough to compare structurally against a QET-style IEC control drawing without treating
  it as a toy graph.

- **UJ-2. A package author defines a vendor-like Engineering Connectivity Contract.**
  The author writes Athena source for a drive-like Engineering Connectivity Contract with power, signal, and PE
  ports. The source references package-local SVG geometry only for shapes and anchor locations. The
  Engineering Connectivity Contract owns signal role, direction, compatibility, Connection Intent,
  and external evidence. Invalid or duplicated SVG metadata fails compilation.

- **UJ-3. A planner proposes routes but Athena decides.**
  A route planner receives a transient layout graph and proposes path candidates. Athena validates
  the candidates against semantic, representation, physical, and route-quality rules. Accepted
  RouteFacts include quality evidence; rejected candidates produce diagnostics. Planner output never
  becomes source truth.

## 3. Glossary

- **Engineering Connectivity Contract:** The minimum typed Athena contract describing how a governed
  engineering object participates in topology for M37: interfaces, ports, signal roles,
  compatibility predicates, Connection Intent hooks, physical installation references,
  representation bindings, and external evidence references. It does not own manufacturer identity,
  article number, lifecycle, procurement, datasheets, or other full Engineering Component System
  facts. It replaces and evolves the M36 Connectable Entity Contract; both names do not coexist in
  production contracts.
- **Interface:** A named group of Ports that work together, such as power input, motor output,
  discrete input, fieldbus, protective earth, or service. Interfaces are source-owned.
- **Port:** A typed engineering connection point owned by an Engineering Connectivity Contract. A Port is
  not a visual Anchor.
- **Anchor:** A representation connection location on an Element or SVG-backed geometry reference.
  An Anchor binds to a Port through Athena source.
- **Connection Intent:** Source-owned engineering intent attached to a Connection or Interface route,
  such as power, control, protective earth, safety, service, priority, separation requirement,
  preferred drawing region, or preferred physical channel. It guides planning but is not geometry.
- **External Evidence Mapping:** Athena-owned evidence that links an Engineering Connectivity
  Contract, Interface, Port, Connection Intent, or classification to external standards or catalogs.
  It is reference evidence, not external runtime authority or an ontology owned by the kernel.
- **Route Quality Policy:** Athena-owned rules and weights used to judge route candidates after hard
  constraints pass.
- **Route Lane:** A compiler-owned lane inside a Route Channel used to separate routes and improve
  readability.
- **Route Label Fact:** A derived fact describing label content, attachment point, collision state,
  and source provenance for a route.
- **Planner Proposal:** A non-authoritative placement or routing candidate returned by Athena-native
  or external planner adapters.
- **Routing Diagnostics:** Machine-readable compiler diagnostics for rejected route candidates,
  degraded route quality, collisions, unreadable labels, fallback anchors, or standards mismatches.
- **Drawing Standard Profile:** A typed presentation profile that defines sheet frame, grid,
  title-block, stroke classes, text scale, endpoint markers, junction markers, crossing policy,
  label placement, and symbol spacing for a professional drawing family.
- **Connection Presentation Class:** The style and behavior assigned to a Connection or RouteFact,
  such as power conductor, control signal, protective earth, reference line, device link, or
  annotation leader. It controls stroke weight/style/color, endpoint markers, label policy, and
  crossing behavior.
- **Endpoint Attachment Rule:** A compile-time rule proving that a visible line starts and ends on a
  resolved Anchor, bus, terminal, junction, or sheet reference. Loose line starts and ends are
  invalid unless explicitly authored as graphical annotation.
- **Projection Policy:** A typed compiler policy that lowers the same Athena semantic source into a
  target projection such as Cabinet, professional connection drawing, schematic, documentation, or
  future engineering views. It selects presentation and layout strategy but does not own engineering
  truth.

## 4. Features

### 4.1 Engineering Connectivity Contract

**Description:** M37 introduces the smallest useful engineering-connectivity layer required for routing,
External Evidence Mapping, and AI-readable engineering explanation. It consumes existing semantic/package
facts but does not become the full Engineering Component System.

#### FR-1: Define Engineering Connectivity Contracts

Athena source shall support Engineering Connectivity Contracts with stable contract IDs,
Interfaces, Ports, compatibility predicates, optional physical installation references,
representation bindings, and External Evidence Mapping references.

**Consequences:**
- A Port cannot exist only in SVG or renderer payload.
- A visual Element cannot define product identity or manufacturer lifecycle.
- Invalid interface/port references fail during compile or LSP validation.
- Existing M36 Connectable Entity Contract code and consumers are renamed and evolved directly;
  there is no dual model, migration adapter, or compatibility path.

#### FR-2: Group ports into interfaces

Athena shall support grouped connection definitions so related Ports can be authored and reviewed
together.

**Consequences:**
- Power, signal, PE, fieldbus, and service groups can have group-level compatibility defaults.
- Port-level overrides are explicit and source-spanned.
- Generated route intent can preserve interface grouping.

#### FR-3: Preserve boundary from full Engineering Component System

M37 shall not introduce manufacturer article lifecycle, procurement data, BOM ownership, datasheet
management, simulation models, or replacement rules as first-class Component System features.

#### FR-4: Preserve one semantic source across projections

The Engineering Connectivity Contract, Interfaces, Ports, Connections, Connection Intents, External Evidence Mappings, and source
provenance shall be projection-neutral. Cabinet, professional connection drawing, schematic, and
future views shall consume the same semantic facts through Projection Policies. A projection-specific
file or renderer payload shall not duplicate or redefine engineering truth.

### 4.2 Connection Intent Model

**Description:** M37 separates topology from engineering intent. A Connection says what is connected.
Connection Intent says how that connection should be treated by validation, routing, presentation,
and future AI reasoning.

#### FR-5: Define Connection Intent

Athena source shall support Connection Intent on Connections, Interfaces, or route groups. At
minimum, M37 shall support intent class, priority, separation requirements, preferred drawing region
or physical channel, and route-label policy.

**Consequences:**
- Intent is source-owned and source-spanned.
- Intent can guide route planning and presentation without becoming geometry.
- Missing or incompatible intent produces diagnostics before rendering.
- Resolution is deterministic: Connection intent overrides route-group intent, which overrides
  Interface defaults, which override the selected profile default.
- Compiler code shall not infer intent class from endpoint types, visual geometry, or renderer state.

#### FR-6: Lower Connection Intent into route planning

The compiler shall lower Connection Intent into transient Route Intent and planner constraints while
preserving provenance, owner, strength, and diagnostic identity.

#### FR-7: Prove intent-to-route traceability

Every accepted RouteFact shall identify the Connection Intent that influenced its route class,
channel or drawing region, separation behavior, label policy, and route quality score.

### 4.3 External Evidence Mapping

**Description:** M37 acknowledges real industrial ecosystems without importing their authority
model. AutomationML, IEC references, ECLASS-like classifications, and vendor catalog IDs may be
mapped as evidence. Athena remains the compiled source of truth.

#### FR-8: Attach external evidence mappings

Athena source shall support typed external evidence references on Engineering Connectivity
Contracts, Interfaces, Ports, Connection Intent, and route policies.

**Consequences:**
- References are structured, source-spanned, and included in proof payloads.
- Unknown evidence namespaces or invalid reference syntax produce diagnostics.
- Evidence references cannot create Ports, Interfaces, Anchors, Connection Intent, or route facts by
  themselves.

#### FR-9: Keep AML/XML as import or evidence only

M37 shall not add active XML runtime authority. If AutomationML or other XML-based formats are used,
they are external interchange/import inputs or standards evidence and must lower into Athena source
or compiler-owned facts before product use.

#### FR-10: Define import boundary notes

M37 shall document how future AML/ECLASS/IEC importers would map into Athena Engineering
Connectivity Contracts without copying external schemas into the kernel language.

The M37 valid sample shall use one IEC citation and one neutral external classification reference.
AutomationML and ECLASS parser/resolver implementation remains deferred.

### 4.4 Route Lane And Channel Policy

**Description:** M36 proved route facts. M37 makes route realization readable by adding route lanes,
channel occupancy, route ordering, spacing policy, and deterministic quality scoring.

#### FR-11: Model route lanes in channels

Route Channels shall expose compiler-owned Route Lanes with capacity, spacing, orientation,
occupancy, and collision evidence.

#### FR-12: Allocate lanes deterministically

The route engine shall assign lanes deterministically from Route Intent, Interface priority, channel
capacity, route grouping, and stable tie-breakers.

#### FR-13: Report channel occupancy

Structured proof shall report channel occupancy, over-capacity conditions, lane assignments,
conflicts, and degraded-route reasons.

### 4.5 Route Quality Policy And Diagnostics

**Description:** M37 turns "route looks bad" into measurable compiler evidence. The renderer remains
paint-only; quality belongs to route policy and diagnostics.

#### FR-14: Define route quality policy

Athena shall define a Route Quality Policy that separates hard rejects from soft scoring. Hard
rejects include route/body intersection, missing anchors, invalid channel use, violated clearance,
and invalid semantic connection. Soft scoring includes crossings, bends, lane changes, label
collisions, length, and visual density.

The policy vocabulary and validation contract are compiler-owned. Policy values are authored in a
typed Drawing Standard Profile selected by Projection Policy. Project source may express Connection
Intent but shall not redefine the scoring vocabulary or bypass hard rejects.

#### FR-15: Emit route quality facts

Every accepted RouteFact shall include quality grade, score components, degraded reasons, selected
lane, selected channel sequence, planner identity, and compiler snapshot.

#### FR-16: Diagnose weak routes

LSP and E2E proof shall surface route-quality diagnostics for unreadable or degraded routes without
silently accepting them as success.

### 4.6 Professional Drawing Grammar And Line Presentation

**Description:** M37 turns professional drawing appearance into typed presentation grammar. Line
weight, style, color, endpoint symbols, junction dots, crossing behavior, label placement, sheet
frame, and title block are selected by Drawing Standard Profile and Connection Presentation Class.
The renderer paints the result but does not decide it.

#### FR-17: Define drawing standard profiles

Athena shall support a Drawing Standard Profile for the M37 proof drawing with, at minimum, sheet
frame, coordinate grid, title block regions, default text scale, stroke classes, junction marker
rules, crossing rules, and reference designation label policy.

#### FR-18: Classify connection presentation

Every visible Connection or RouteFact shall resolve to a Connection Presentation Class. The class
shall define stroke weight, stroke style, color, endpoint behavior, label policy, and crossing
behavior. The selected class and profile declaration shall remain traceable through Graphic
Primitive IR and the presentation payload.

#### FR-19: Reject loose connection endpoints

Any connection line whose start or end does not resolve to an Anchor, terminal, bus, junction, or
sheet reference shall fail compile or emit a blocking diagnostic. The renderer shall not draw random
unowned line endpoints as if they were valid engineering connections.

#### FR-20: Render junctions and crossings by semantic fact

Junction dots, no-connection crossings, wire hops, terminal joins, and bus taps shall render only
from explicit semantic or route facts. A visual crossing cannot imply electrical connection.

#### FR-21: Prove label placement

Route and device labels shall carry bounds and collision evidence. Labels that overlap device bodies,
route lines, title blocks, or other labels shall produce diagnostics unless explicitly accepted as
degraded proof.

### 4.7 Projection Policy

**Description:** Projection Policy is the core mechanism that lets one Athena semantic source lower
into multiple professional views. It is equivalent to a compiler backend or materialized view policy:
it selects drawing/layout behavior while semantic truth remains upstream.

#### FR-22: Define Projection Policy as compiler input

Athena shall model Projection Policy as typed compiler input that selects target surface, layout
strategy, presentation profile, route policy, and proof obligations.

#### FR-23: Prove projection-neutral semantic facts

The M37 proof shall show that Engineering Connectivity Contracts, Interfaces, Ports, Connections,
Connection Intents, and External Evidence Mappings are projection-neutral facts consumed by the
selected Projection Policy. A structural compiler test shall lower the same M37 example source
through the professional connection drawing policy and the existing Cabinet policy, preserving the
same semantic identities, topology, intent, evidence, and provenance. Only the professional
connection drawing is a product-quality visible acceptance surface in M37.

#### FR-24: Reject projection-specific engineering truth

A projection file, renderer payload, SVG resource, or planner graph shall not redefine Ports,
Connections, Connection Intent, compatibility, or external evidence. Such duplication shall fail
validation.

### 4.8 Planner Boundary And ELK Learning

**Description:** M37 may learn from ELK concepts such as ports, layered layout, orthogonal edges,
edge labels, and crossing reduction. ELK remains an adapter behind Athena's planner SPI.

#### FR-25: Keep planner graph transient

Planner input and output shall remain transient derived IR. No planner object, ELK object, or
external graph coordinate may become persisted Athena source.

#### FR-26: Support planner candidate comparison

Athena shall be able to compare multiple route or placement candidates using Route Quality Policy
and choose the accepted candidate deterministically.

#### FR-27: Fail closed on planner mismatch

If a planner proposal omits required ports, anchors, lanes, constraints, or route proof fields,
Athena shall reject it and either use a validated deterministic native candidate or report a
diagnostic. It shall not guess.

M37 shall ship and prove the Athena-native deterministic planner only. An ELK adapter remains
deferred until the Planner SPI conformance suite and M37 route-quality gates pass.

### 4.9 SVG Geometry Bridge Hardening

**Description:** M37 keeps the M34-M36 SVG boundary and makes it harder to regress. SVG may provide
geometry IDs and `data-athena-ref` hints. Athena source owns all engineering meaning.

#### FR-28: Validate geometry reference schema

The compiler shall validate allowed geometry-reference syntax, duplicate references, unsupported
`data-athena-*` keys, missing referenced geometry, and external resource attempts.

#### FR-29: Keep simple symbols native

Simple primitive Symbols shall remain authorable in Athena source without SVG. Complex vendor-like
geometry may use package-local SVG resources.

#### FR-30: Preserve package-local resource default

M37 shall keep package-local resource resolution as the default. Remote resource URI and registry
support may be designed but not implemented unless all core routing and engineering-connectivity gates
pass.

### 4.10 Professional Drawing Proof

**Description:** M37 must prove the new engineering-connectivity, routing-quality, and drawing-grammar
model on one dedicated professional drawing example. This is the product acceptance surface.

#### FR-31: Provide a dedicated M37 sample

M37 shall create `examples/m37/...` as the only M37 product proof. It shall not reuse M36 as the
active proof project. The sample shall use one Athena semantic source and one selected Projection
Policy for the proof surface. The sample shall target a QET-style professional control drawing
structure: sheet frame, grid coordinates, title block, supply rails, terminals, protective earth,
device contacts, coils, indicators, and disciplined orthogonal wiring.

#### FR-32: Enforce professional route readability

The M37 sample shall show disciplined endpoints, route lanes or wire columns where applicable,
readable labels, consistent line classes, explicit junction/crossing rendering, and diagnostics for
any invalid fixture. The accepted valid proof shall have zero loose endpoints, fallback anchors,
route/body intersections, ambiguous crossings, label/body overlaps, label/title-block overlaps, and
unclassified visible routes.

#### FR-33: Prove source-to-route traceability

Every visible component occurrence, interface, port, anchor, route lane, RouteFact, RouteLabelFact,
Connection Intent, and External Evidence Mapping shall trace back to Athena source and package-local
resources.

#### FR-34: Capture screenshot-backed E2E evidence

Electron E2E shall capture desktop and narrow screenshots under M37 implementation artifacts and
prove the professional drawing surface is active, nonblank, source-backed, and free of fallback route
geometry. Structural assertions, not screenshot appearance alone, are the acceptance authority.
Every proof field shall be computed from compiled facts and diagnostics; hardcoded success booleans
are forbidden.

### 4.11 Cleanup And Architecture Gate

**Description:** M37 must not repeat the M36 cleanup incident.

#### FR-35: Enforce source-set hygiene

Every M37 story shall run the source-set hygiene audit and reject `Proof`, `Demo`, `Sample`,
milestone-named production classes, `V0`/`V1`, or test fixtures under `src/main`.

#### FR-36: Remove stale incompatible paths

Because Athena is pre-public, M37 shall delete or directly refactor stale XML, fallback, renderer
authority, or compatibility paths that conflict with the current architecture.

This includes removing endpoint-derived intent classification and hardcoded professional-drawing
route-policy injection from the active compilation path once authored Connection Intent and selected
profiles replace them. It also includes replacing hardcoded projection/view selection and asserted
proof-success constants with selected Projection Policy and computed evidence.

## 5. Non-Goals

- No full Engineering Component System.
- No complete vendor product catalog, procurement model, BOM ownership, lifecycle model, or
  datasheet platform.
- No AutomationML runtime authority and no XML-driven product path.
- No strict EPLAN/QET clone logic.
- No multi-view polish across Cabinet, Documentation, and Schematic.
- No AI auto-layout or AI component replacement.
- No renderer-side inference of engineering facts, route meaning, or component compatibility.
- No public package registry implementation unless core M37 gates are already complete.

## 6. MVP Scope

### 6.1 In Scope

- Minimal Engineering Connectivity Contract.
- Grouped Port/Interface syntax and diagnostics.
- Connection Intent syntax and diagnostics.
- External Evidence Mapping for Engineering Connectivity Contracts, Ports, Connection Intent, and
  route policies.
- Route lanes, channel occupancy, and deterministic lane allocation.
- Route Quality Policy, route-quality facts, and diagnostics.
- Projection Policy as typed compiler input.
- Planner candidate comparison behind Athena-owned Planner SPI and validator.
- SVG geometry bridge hardening.
- Dedicated M37 professional connection drawing sample and screenshot-backed E2E proof.
- Source-set cleanup gate.

### 6.2 Out of Scope for MVP

- Full ECS and vendor catalog.
- AML/ECLASS/IEC import implementation beyond mapping design and evidence fields.
- Registry/remote package resource runtime.
- Multi-view product parity.
- General constraint solving or optimization research.
- Human graphic editor for route editing.

## 7. Cross-Cutting NFRs

- **NFR-1 SSOT:** Athena source remains the single engineering metadata authority.
- **NFR-2 Type safety:** Invalid Engineering Connectivity Contract, Port, Anchor, Connection Intent,
  External Evidence Mapping, route lane, Projection Policy, or route-quality reference fails during
  compile or LSP validation.
- **NFR-3 Determinism:** Same source, package snapshot, planner version, and route policy produce the
  same PlacementFacts, RouteFacts, RouteLabelFacts, and screenshots within stable tolerances.
- **NFR-4 Explainability:** Every accepted or rejected route candidate has source-spanned evidence.
- **NFR-5 Renderer purity:** Renderer paints Graphic Primitive IR only and does not fix routes.
- **NFR-6 Security:** SVG remains fail-closed; external references, scripts, unsafe entities, and
  unsupported Athena data keys are rejected.
- **NFR-7 Performance:** After the IDE is ready, an M37 compile-to-presentation refresh shall finish
  within 10 seconds on the supported development workstation, and the UI shall remain responsive
  while route-quality evidence is produced.
- **NFR-8 Cleanup:** Pre-public legacy compatibility paths are removed, not wrapped.

## 8. Success Metrics

**Primary**

- **SM-1:** The M37 sample defines at least one vendor-like Engineering Connectivity Contract with grouped
  power, signal, and PE interfaces and zero LSP diagnostics. The proof shows the same semantic
  source feeding the selected Projection Policy. Validates FR-1 through FR-4.
- **SM-2:** Every visible route has lane assignment or wire-column evidence, channel/drawing-region
  occupancy, Connection Intent, quality evidence, line presentation class, and source-to-route
  traceability. Validates FR-5 through FR-21 and FR-33.
- **SM-3:** Screenshot-backed E2E proves the professional drawing surface is active and nonblank,
  while structural assertions prove zero loose endpoints, fallback anchors, route/body
  intersections, ambiguous crossings, label/body overlaps, label/title-block overlaps, and
  unclassified visible routes at desktop and narrow viewports. Validates FR-31 through FR-34.
- **SM-4:** External Evidence Mappings appear in structured proof without creating runtime authority
  from AML/XML/ECLASS/IEC inputs. Validates FR-8 through FR-10.
- **SM-5:** The same M37 example source lowers through professional connection drawing and Cabinet
  Projection Policies with identical semantic identities, topology, intent, evidence, and
  provenance; projection-specific engineering truth is rejected. Only the professional connection
  drawing is held to the M37 visible-quality gate. Validates FR-22 through FR-24.

**Secondary**

- **SM-6:** Invalid SVG metadata and invalid geometry references fail closed with source-spanned
  diagnostics. Validates FR-28 through FR-30.
- **SM-7:** Planner candidate comparison accepts only Athena-validated proposals and records rejected
  candidates. Validates FR-25 through FR-27.
- **SM-8:** Source-set hygiene audit, encoding audit, full tests, IDE build, LSP installDist, and M37
  Electron E2E pass before completion. Validates FR-35 and FR-36.

**Counter-metrics**

- **SM-C1:** Do not maximize route optimization complexity at the expense of the typed connectivity,
  intent, projection, and route evidence model.
- **SM-C2:** Do not make external standards, XML, SVG, or ELK own Athena engineering facts.
- **SM-C3:** Do not spread M37 across multiple visible product surfaces.
- **SM-C4:** Do not introduce milestone-named production classes or compatibility shims.

## 9. Resolved Planning Decisions

1. M37 is about Engineering Connectivity Contracts, Connection Intent, Projection Policy, and route
   intelligence, not a full Engineering Component System.
2. M37 keeps one visible product-quality surface: a professional connection drawing sheet if the
   QET-style reference remains the acceptance target.
3. External standards are mapped as External Evidence Mapping; Athena source remains authority.
4. ELK concepts may inform Planner SPI, but ELK remains optional and non-authoritative.
5. Package-local SVG remains the default geometry resource model.
6. Remote resource URI and registry support are design notes unless core M37 acceptance is already
   complete.
7. M37 evolves the current M36 connectivity declarations and compiler contracts directly; it does
   not prototype a second syntax or model behind existing declarations.
8. Connection Intent precedence is Connection, route group, Interface default, then selected profile
   default. Missing required intent and conflicting declarations fail validation.
9. Route Quality Policy values are profile-authored in typed Athena source; the compiler owns the
   policy schema and hard-reject semantics; Projection Policy selects the profile.
10. The valid sample carries one IEC citation and one neutral external classification reference.
    AML and ECLASS parser/resolver work is deferred.
11. M37 proves the Athena-native deterministic planner and its conformance boundary. It does not ship
    an ELK adapter.
12. Professional acceptance uses the fixed structural gates in FR-32 and SM-3. QET remains a visual
    and domain reference, not a pixel-perfect clone target.

## 10. Open Items

No phase-blocking product or architecture questions remain for epic and story creation. Exact
surface syntax is an implementation design decision constrained by the direct-evolution rule and
must be covered by parser, tree-sitter, LSP, compiler, and negative-fixture tests in one story.
