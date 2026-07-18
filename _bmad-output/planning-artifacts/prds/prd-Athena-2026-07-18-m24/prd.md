---
title: Athena M24 - Governed Schematic Routing Fidelity
status: draft
created: 2026-07-18
updated: 2026-07-18
---

# PRD: Athena M24 - Governed Schematic Routing Fidelity

## 0. Document Purpose

M24 follows M23 by addressing the most visible remaining credibility gap in the Athena sheet
workflow: wire and connection routing still looks like a graph edge, not like a professional
electrical schematic.

M23 admitted layout hints into real `.athena` syntax. It did not make input/output wiring, terminal
exits, route lanes, crossings, or label placement behave like an EPLAN-class schematic. M24 exists
to prove that Athena can render governed schematic connections as disciplined orthogonal engineering
routes while keeping routing authority above the renderer.

M24 is not full EPLAN parity, not cabinet or physical routing, not harness routing, not an AI layout
milestone, and not a public library/ecosystem milestone. It is the first governed schematic routing
fidelity milestone.

## 1. Vision

An engineer opening Athena should no longer see wires drawn as generic center-to-center graph lines.
Connections should leave component ports from the correct side, snap to a sheet grid, use stable
orthogonal route segments, avoid obvious component bodies, align through routing lanes and bundles,
and keep labels readable.

The visual reference is the ordered wiring style shown in
`../../../draft/screenshort/coffret_cordons_chauffants.png`: wires run in disciplined parallel
lanes, fold cleanly, avoid elements, and attach to terminal strips in an ordered way. M24 uses that
as routing-fidelity inspiration, not as permission to save physical cabinet wire geometry as canvas
truth.

M24 keeps Athena's rhythm. It should not attempt to reproduce that reference sheet in full. It should
prove the first narrow routing step that moves Athena toward that quality: terminal-anchor routes,
orthogonal grid-aligned segments, and one small ordered terminal-strip lane/bundle case.

The target pipeline is:

```text
.athena source devices / ports / connects / layout hints
    -> compiler semantic connection model
    -> electrical connection intent
    -> projection / presentation IR
    -> schematic routing policy
    -> port-side and terminal-anchor facts
    -> schematic route intent
    -> routing constraints
    -> deterministic route facts
    -> paint-only Theia renderer
```

The renderer may paint routes and interactions. It must not invent connection meaning, terminal
identity, route authority, or hidden wire state.

M24 must not become a general graph visualization router. Generic graph routers optimize topology
aesthetics. Athena routing must optimize electrical engineering readability: signal role, power
hierarchy, terminal transitions, port direction, maintenance scanability, and deterministic review.

## 1.1 Why Now

M19 proved a schematic sheet exists. M20 made the sheet surface more acceptable. M21 introduced
layout intent and facts. M22 introduced governed layout optimization direction. M23 made layout
intent real source syntax.

The current user-visible problem is now sharper: even when components are placed acceptably, the
wires do not yet communicate professional engineering intent. Input/output connection lines are not
coordinated enough. If Athena waits until M25 or later, the IDE can keep looking naive even though
the language and layout contracts are improving underneath.

M24 should therefore directly improve schematic routing fidelity before broader symbol libraries,
standards profiles, or full visual parity work.

## 2. Target User

### 2.1 Jobs To Be Done

- Aaron needs to open a sample project and immediately see that wire connections look more like an
  engineering schematic than a generic graph.
- Maya needs PLC, HMI, terminal, power, and load connections to enter and exit component sides
  predictably.
- Priya needs a customer-facing proof that Athena can generate readable electrical connection
  routes, not only valid semantic links.
- Winston needs routing to remain governed by source, projection, route intent, and route facts
  rather than canvas-local decisions.

### 2.2 Non-Users

- Teams expecting full EPLAN parity in one milestone
- Teams expecting cabinet layout or physical wire routing
- Teams expecting harness, cable tray, or 3D installation routing
- Teams expecting full IEC/QElectroTech symbol library ingestion
- Teams expecting free-form wire drawing saved as canvas state
- Teams expecting AI-generated routing

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a routed schematic proof.**
  - **Context:** Aaron opens `examples/m24/sample-project` in the Athena Theia IDE.
  - **Path:** He opens the main `.athena` file and reveals Graphical View.
  - **Climax:** PLC, HMI, terminal block, power source, protection, and load connections use
    orthogonal routes with clear terminal exits and readable labels.
  - **Resolution:** The sheet no longer looks like a generic graph viewer for the accepted M24
    scenario.

- **UJ-2. Maya checks input/output connection discipline.**
  - **Context:** Maya inspects a component with input and output ports.
  - **Path:** She checks whether inputs enter from the expected side, outputs leave from the
    expected side, and route stubs align with the sheet grid.
  - **Climax:** The connection geometry makes the engineering role visible without reading source.
  - **Resolution:** The routing facts communicate port role and connection direction.

- **UJ-3. Winston validates routing authority.**
  - **Context:** Winston reviews route model, compiler/projection derivation, and Theia rendering.
  - **Path:** He traces connection source to route intent, route constraints, route facts, and
    renderer output.
  - **Climax:** The renderer paints facts only; it does not infer wire meaning from DOM positions.
  - **Resolution:** M24 improves routing without compromising EngineeringOS authority.

## 3. Glossary

- **Schematic Routing Fidelity** - The professional readability of rendered schematic connections:
  port-side discipline, orthogonal segments, lanes, crossings, labels, and grid alignment.
- **Schematic Route Intent** - A model-level description of how a connection should be routed in a
  schematic view, derived from semantic connection facts, port role, component occurrence, and layout
  context.
- **Electrical Connection Intent** - Electrical-aware interpretation of a semantic connection, such
  as digital output, power feed, ground, control signal, terminal transition, or load connection.
- **Routing Policy** - Domain-owned route preference rules derived from engineering meaning,
  component role, port role, signal class, and presentation context.
- **Port Presentation Policy** - Domain-owned rules that choose preferred port sides and terminal
  anchors for a component family. These rules are not renderer hardcoding.
- **Route Constraint** - A governed routing relationship such as preferred exit side, avoid
  component body, lane preference, orthogonal-only, grid snap, or label clearance.
- **Route Fact** - Deterministic route output consumed by the renderer: polyline segments, anchor
  points, lane ids, crossing markers, label anchors, and source identities.
- **Terminal Anchor** - The occurrence-specific point where a route attaches to a port or terminal
  on a rendered component.
- **Routing Lane** - A reusable horizontal or vertical channel used to align multiple schematic
  routes and reduce visual noise.
- **Route Bundle** - A semantically related group of routes that may travel in parallel lanes before
  splitting to individual terminal anchors.
- **Schematic Topology Routing** - Routing for 2D schematic readability. It is not physical routing
  through a cabinet, harness, cable tray, or installation.

## 4. Features

### 4.1 Openable M24 Routing Proof

**Description:** M24 starts with a visible sample project that proves routing quality in Theia.

#### FR-1: Provide an openable M24 sample project

Athena provides `examples/m24/sample-project` with real `.athena` files that exercise port-side
routing, orthogonal connection geometry, lanes, crossings, and label placement.

**Consequences:**

- The project opens through the normal Athena Theia workflow.
- Reviewers do not need to inspect `.mjs` files to understand the proof.
- The sample includes PLC/controller, HMI/operator device, terminal block, power/protection, and
  load subjects.
- The sample includes at least one input/output connection case where center-to-center graph edges
  would be visibly wrong.

#### FR-2: Define routing acceptance references

Athena documents routing acceptance expectations using `draft/screenshort` as visual inspiration and
M23 as the baseline comparison.

**Consequences:**

- Acceptance checks name terminal exits, route orthogonality, grid alignment, lane usage, crossing
  behavior, route bundle ordering, terminal-strip attachment order, and label readability.
- The acceptance proof includes an explicit M23-vs-M24 comparison so reviewers can see what changed
  from graph-edge routing to schematic route facts.
- `../../../draft/screenshort/coffret_cordons_chauffants.png` is an explicit visual reference for
  ordered lane/bundle routing quality.
- The reference is directional only; M24 does not need to reproduce the full sheet, symbol density,
  cabinet completeness, or EPLAN-level route perfection.
- The acceptance bar is "credible governed schematic routing," not full EPLAN parity.
- The proof states where Athena still differs from EPLAN.

### 4.2 Governed Route Model And Facts

**Description:** Athena introduces routing concepts above the renderer.

#### FR-3: Add port-side and terminal-anchor facts

Athena derives occurrence-specific terminal anchors from port role, component occurrence,
presentation context, and domain-owned port presentation policy.

**Consequences:**

- Inputs, outputs, power, ground, and bidirectional ports can receive preferred schematic sides from
  policy.
- Route segments attach to terminal anchors, not component centers.
- Terminal anchors carry canonical subject, port, occurrence, and source identities.
- The renderer receives anchor facts and never computes engineering-side meaning by itself.
- Port-side defaults are not hardcoded as universal rules such as "input equals left" and "output
  equals right"; they are policy defaults that can later vary by component family and standards
  profile.

#### FR-4: Add schematic route intent and route constraints

Athena derives electrical connection intent, schematic routing policy, and route intent from
semantic connections and layout context.

**Consequences:**

- Route intent references source connection identities.
- Electrical connection intent can classify control, power, terminal transition, and load routes
  before geometry is solved.
- Routing policy can choose preferred sides, lanes, clearance, and crossing behavior based on
  engineering meaning.
- Route constraints can express orthogonal-only, grid-snap, avoid-node, preferred-exit-side,
  preferred-entry-side, route-lane, crossing, and label-clearance preferences.
- M24 derives these from existing source and model facts first; new route-hint syntax is deferred
  unless it stays mechanically small and parser/LSP parity is fully covered.
- Routing constraints remain schematic-only.

#### FR-4A: Establish `kernel/routing-model` as the routing contract home

Athena introduces a dedicated routing-model contract instead of hiding route semantics inside
presentation, layout, renderer, or Theia frontend code.

**Consequences:**

- `ElectricalConnectionIntent`, `RoutingPolicy`, `PortPresentationPolicy`, `TerminalAnchorFact`,
  `RouteConstraint`, `RouteFact`, and `RouteQuality` have a clear kernel home.
- Layout and presentation code may provide context, but routing meaning lives in the routing model.
- Renderer code consumes route facts and does not own route semantics.

#### FR-5: Produce deterministic orthogonal schematic route facts

Athena produces deterministic route facts for the M24 sample and targeted fixtures.

**Consequences:**

- The same governed input produces the same route segment output.
- Route facts are grid-aligned.
- Routes use horizontal and vertical segments.
- Routes avoid obvious component body overlap in the accepted sample.
- Routes carry identities for selection, reveal, diagnostics, and inspection.
- Route facts remain stable after source reload and projection rebuild.

#### FR-6: Provide route quality diagnostics or explanations

Athena exposes basic routing quality status when it cannot satisfy the intended route constraints.

**Consequences:**

- The system can report blocked, degraded, or fallback routing.
- A fallback route is visible as fallback, not silently presented as professional output.
- Diagnostics or inspector text name the affected connection and failed constraint family.

### 4.3 Professional Theia Routing Experience

**Description:** Theia renders governed route facts as professional schematic routes.

#### FR-7: Render wires from terminal anchors with coordinated geometry

The Graphical View renders route facts as orthogonal schematic wires attached to terminal anchors.

**Consequences:**

- Wires no longer default to center-to-center component lines.
- Port stubs are short, aligned, and grid-aware.
- Multiple routes can share lanes without unreadable overlap.
- Route crossings are deliberate and visually distinguishable.
- Route labels are placed near their routes without covering component bodies.

#### FR-8: Preserve source, outline, Problems, and Graphical View identity

Routing fidelity does not break the accepted M20-M23 IDE behavior.

**Consequences:**

- Active `.athena` source still drives Graphical View.
- Selecting a route reveals the source connection or port identity where available.
- Problems and inspector payloads use the same canonical connection and occurrence identities.
- Outline navigation keeps the same `.athena` editor tab.

#### FR-9: Keep routing interaction inspectable but not canvas-owned

The Graphical View may expose route inspection and route-quality information, but M24 does not save
hidden route state from the canvas.

**Consequences:**

- Route inspection can show source connection, route quality, lane, anchors, and labels.
- User route dragging remains deferred unless it becomes governed intent and accepted source/model
  mutation.
- The canvas does not persist route coordinates as hidden truth.

### 4.4 Guardrails And Regression Proof

**Description:** M24 must improve routing without regressing syntax, layout, or product truth.

#### FR-10: Preserve M23 layout hint language admission

M24 does not regress system-scoped layout blocks.

**Consequences:**

- ANTLR4 and Tree-sitter continue accepting M23 layout syntax.
- M23 sample project continues opening without false syntax errors.
- M24 route facts may consume M23 layout constraints but do not replace them.

#### FR-11: Keep physical routing and full parity deferred

M24 explicitly remains schematic topology routing.

**Consequences:**

- No cabinet routing, harness routing, cable tray routing, 3D routing, or installation routing.
- No full EPLAN parity claim.
- No full IEC/QElectroTech library ingestion.
- No public package/import ecosystem expansion.

#### FR-12: Publish usage and verification evidence

Athena records how to run and verify the M24 routing proof.

**Consequences:**

- Usage docs identify the sample project, files, and expected routing checks.
- Tests cover route fact determinism, terminal-anchor derivation, Theia rendering structure, and
  M23 regression.
- The Electron smoke opens the M24 sample project and proves route DOM/canvas facts are present,
  terminal-anchor routing is visible, and routes are not falling back to center-to-center component
  edges.

## 5. Non-Goals

- Full EPLAN parity
- Cabinet routing or physical wire routing
- Harness, cable tray, 3D installation, or manufacturing routing
- AI routing or AI layout optimization
- Public repository/import ecosystem work
- Full IEC/QElectroTech symbol-library ingestion
- Full standards-specific label generation
- Free-form wire drawing saved as canvas state
- Raw coordinate route persistence as the primary authored model
- Final external routing engine selection

## 6. MVP Scope

### 6.1 In Scope

- `examples/m24/sample-project` with real `.athena` source
- Minimum sample routes: PLC to HMI, PLC to terminal block to load, and 24V power/protection path
- Port-side and terminal-anchor facts
- Schematic route intent and route constraints
- Deterministic orthogonal route facts
- Basic routing lanes for the accepted sample
- Basic crossing and label-placement behavior
- Graphical View rendering from route facts, not component-center graph edges
- Route inspection/status sufficient to explain degraded fallback routes
- Regression coverage for M23 layout syntax and sample behavior
- Usage documentation and routing acceptance checklist

### 6.2 Out Of Scope

- Route editing and route-hint source syntax unless it remains very small and fully covered
- Physical/cabinet/harness/cable routing
- General graph visualization routing
- Full symbol library breadth
- Multi-page route continuity and cross-sheet routing beyond a small proof if mechanically clean
- Final ELK or routing-engine stack decision
- AI or optimization-heavy routing

## 7. Success Metrics

**Primary**

- **SM-1:** The M24 sample project opens in Theia and renders route facts from terminal anchors, not
  center-to-center component lines.
- **SM-2:** Inputs and outputs in the accepted sample enter or exit predictable sides based on port
  role and presentation context.
- **SM-3:** Routes in the accepted sample are orthogonal, grid-aligned, and deterministic across
  repeated runs.
- **SM-4:** Route labels and crossings remain readable in the accepted sample.
- **SM-5:** Route selection/inspection can identify the source connection or route quality state.
- **SM-6:** M23 layout syntax and sample behavior remain accepted by parser, LSP, and Theia.
- **SM-7:** Route facts remain stable after source reload and projection rebuild.

**Counter-metrics**

- **SM-C1:** Do not claim full EPLAN parity.
- **SM-C2:** Do not persist hidden canvas route state.
- **SM-C3:** Do not implement physical routing under a schematic-routing name.
- **SM-C4:** Do not add new route syntax unless ANTLR4, Tree-sitter, compiler, LSP, docs, and sample
  all admit it together.
- **SM-C5:** Do not make ELK, Graphviz, or any external generic router the M24 architecture.
- **SM-C6:** Do not allow renderer-side fallback to center-to-center graph edges in the accepted M24
  routing proof.

## 8. Assumptions Index

- M23 layout block syntax remains stable and can be used as layout context for routing.
- Existing semantic connections and ports provide enough information to derive initial schematic
  route intent without new source syntax.
- M24 can improve routing visibly through rule-based deterministic facts before adopting a full
  external router.
- M24 should build Athena route engine v0 first and defer ELK/external router evaluation until
  routing semantics and policy are stable.
- Theia remains the only frontend scope for M24.
- The references under `draft/screenshort` are visual acceptance inspiration, not full parity scope.
- Route facts can be introduced without breaking existing M19-M23 Graphical View behavior.

## 9. Open Questions

- What are the initial port-side rules for `in`, `out`, bidirectional, power, and ground ports?
- Should route labels be derived from signal names first, connection ids first, or existing source
  labels when available?
- Should route-lane assignment be purely rule-based in M24, or should an external helper be evaluated
  behind an adapter only after the rule baseline passes?
- Should M24 reserve route-hint syntax in the layout block, or keep route hint language fully
  deferred?
- What minimum screenshot/reference set under `draft/screenshort` defines the M24 routing acceptance
  comparison?
