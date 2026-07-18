---
stepsCompleted:
  - extract-m24-requirements
  - design-m24-epics
  - create-m24-stories
  - validate-m24-coverage
inputDocuments:
  - ../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/review-rubric.md
  - ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md
---

# Athena - M24 Epic Breakdown

## Overview

M24 proves governed schematic routing fidelity. The milestone moves Athena beyond M23 layout hint
language admission by making rendered wires attach to terminal anchors, follow deterministic
orthogonal route facts, avoid component-center graph edges, and show a narrow ordered
terminal-strip lane/bundle proof inspired by `../../draft/screenshort/coffret_cordons_chauffants.png`.

M24 keeps Athena's rhythm. It is not full EPLAN parity, not physical/cabinet/harness routing, not a
generic graph router project, not route-editing syntax, not AI routing, and not a public
repository/library milestone.

## Requirements Inventory

### Functional Requirements

FR1: Provide an openable M24 sample project.

FR2: Define routing acceptance references, including explicit M23-vs-M24 comparison and the
directional screenshot reference.

FR3: Add port-side and terminal-anchor facts derived from semantic identity and policy.

FR4: Add electrical connection intent, schematic routing policy, route intent, and route constraints.

FR4A: Establish `kernel/routing-model` as the routing contract home.

FR5: Produce deterministic orthogonal schematic route facts.

FR6: Provide route quality diagnostics or explanations for degraded/fallback routing.

FR7: Render wires from terminal anchors with coordinated geometry.

FR8: Preserve source, outline, Problems, and Graphical View identity.

FR9: Keep routing interaction inspectable but not canvas-owned.

FR10: Preserve M23 layout hint language admission.

FR11: Keep physical routing and full parity deferred.

FR12: Publish usage and verification evidence.

### NonFunctional Requirements

NFR1: Route authority remains source semantics -> compiler/projection -> routing model -> route
facts -> renderer.

NFR2: Renderer and Theia do not infer route meaning, port side, terminal identity, or hidden route
state.

NFR3: M24 routing is schematic topology only; cabinet, physical, harness, cable tray, and 3D routing
remain deferred.

NFR4: No ELK, Graphviz, yFiles, or external generic router becomes the M24 architecture.

NFR5: Route facts are deterministic and stable after source reload and projection rebuild.

NFR6: New route syntax is deferred unless admitted through ANTLR4, Tree-sitter, compiler, LSP, docs,
and sample proof together.

NFR7: The M24 sample must prove visible routing improvement in Theia, not only unit tests.

### Additional Requirements

- Architecture AD-1: Routing model owns route semantics.
- Architecture AD-2: Existing source derives initial route intent.
- Architecture AD-3: Port sides are policy-owned.
- Architecture AD-4: Route facts attach to terminal anchors.
- Architecture AD-5: Route engine v0 is Athena-owned and rule-based.
- Architecture AD-6: M24 routing is schematic topology only.
- Architecture AD-7: Ordered lanes and bundles are semantic presentation, not physical truth.
- Architecture AD-8: Route quality must be visible when degraded.
- Architecture AD-9: Route facts are deterministic and reload-stable.
- Architecture AD-10: Theia renders and inspects facts only.

### UX Design Requirements

UX-DR1: A reviewer can open `examples/m24/sample-project` in the normal Athena Theia IDE workflow.

UX-DR2: The M24 Graphical View visibly attaches routes to terminal anchors instead of component
centers.

UX-DR3: The sample includes a small terminal-strip lane/bundle case inspired by the supplied
reference image without claiming full reference-sheet parity.

UX-DR4: Route inspection or status can name source connection identity and degraded/fallback quality.

UX-DR5: Active-source projection, same-tab outline navigation, grid-backed canvas, transparent
controls, and top-popover behavior do not regress.

### FR Coverage Map

FR1: Epic 5 - sample project and product proof.

FR2: Epic 5 - acceptance references and usage docs.

FR3: Epic 1 - routing model, port presentation policy, and terminal anchors.

FR4: Epic 1 and Epic 2 - electrical intent, policies, constraints, and engine inputs.

FR4A: Epic 1 - `kernel/routing-model` contract home.

FR5: Epic 2 - deterministic route engine v0 and route facts.

FR6: Epic 3 - quality diagnostics and fallback explanations.

FR7: Epic 4 - Theia route rendering.

FR8: Epic 4 - IDE identity coherence.

FR9: Epic 4 - route inspection without canvas truth.

FR10: Epic 3 and Epic 5 - M23 regression preservation.

FR11: Epic 5 - boundary guardrails.

FR12: Epic 5 - usage and verification evidence.

## Epic List

### Epic 1: Routing Model And Policy Foundation
Athena introduces a routing-model contract for electrical connection intent, routing policy, port
presentation policy, terminal anchors, route constraints, route facts, and route quality.
**FRs covered:** FR3, FR4, FR4A

### Epic 2: Athena Route Engine V0
Athena produces deterministic orthogonal route facts from semantic connections, terminal anchors,
layout context, routing constraints, lanes, and a narrow route-bundle proof.
**FRs covered:** FR4, FR5

### Epic 3: Compiler Projection And Route Quality Diagnostics
Compiler/projection code feeds route intent and route facts into the existing presentation path,
reports degraded/fallback quality, and preserves M23 language behavior.
**FRs covered:** FR6, FR8, FR10

### Epic 4: Theia Route Rendering And Inspection
Theia renders terminal-anchor route facts, exposes route identity/quality inspection, and keeps the
canvas paint-only with accepted workbench behavior intact.
**FRs covered:** FR7, FR8, FR9

### Epic 5: M24 Sample Proof, Usage, And Boundary Guardrails
Reviewers can open a real M24 sample project, compare M23 vs M24 routing, and verify that M24 stays
schematic-only and does not overclaim EPLAN or physical routing parity.
**FRs covered:** FR1, FR2, FR10, FR11, FR12

## Epic 1: Routing Model And Policy Foundation

Athena introduces a routing-model contract for electrical connection intent, routing policy, port
presentation policy, terminal anchors, route constraints, route facts, and route quality.

### Story 1.1: Create routing-model contract home

As an Athena architect,
I want `kernel/routing-model` to own routing contracts,
So that route semantics do not leak into presentation, renderer, or Theia code.

**Acceptance Criteria:**

**Given** M24 routing concepts are introduced
**When** routing-model compiles
**Then** it exposes contracts for electrical connection intent, routing policy, port presentation
policy, terminal anchors, route constraints, route facts, route segments, route labels, and route
quality
**And** it has no dependency on Theia, renderer DOM, or canvas state
**And** Kotlin file organization keeps related small models together without a dump file

### Story 1.2: Model electrical connection intent

As a compiler engineer,
I want semantic `connect` facts to classify electrical connection intent,
So that routing decisions can use engineering meaning instead of only topology.

**Acceptance Criteria:**

**Given** source devices, ports, signals, and connects
**When** connection intent mapping runs
**Then** it can classify control, power, terminal transition, and load connection classes for the
M24 sample
**And** intent carries canonical connection, source port, target port, and source span identity where
available
**And** unknown or unsupported classes degrade explicitly instead of crashing

### Story 1.3: Add port presentation policy and terminal anchors

As a layout/routing engineer,
I want port sides and terminal anchors derived from policy,
So that renderer code does not hardcode universal input/output side rules.

**Acceptance Criteria:**

**Given** component occurrences and port roles
**When** terminal anchor derivation runs
**Then** anchors carry subject, port, occurrence, side, grid point, and policy source
**And** simple default policies cover input, output, power, ground, bidirectional, and terminal-block
ports for the M24 sample
**And** tests prove renderer code is not the owner of side selection

### Story 1.4: Define route constraints, route facts, and quality state

As a route-engine maintainer,
I want stable route constraint and fact contracts,
So that route generation and rendering cannot diverge.

**Acceptance Criteria:**

**Given** route intent and terminal anchors
**When** constraints and facts are built
**Then** constraints can represent orthogonal-only, grid-snap, avoid-node, preferred sides, lane,
bundle, terminal order, crossing, and label clearance
**And** route facts carry deterministic ordered segments, anchors, optional label anchors, source
identity, and quality
**And** quality can represent satisfied, degraded, and fallback route states

## Epic 2: Athena Route Engine V0

Athena produces deterministic orthogonal route facts from semantic connections, terminal anchors,
layout context, routing constraints, lanes, and a narrow route-bundle proof.

### Story 2.1: Build deterministic orthogonal route engine v0

As a routing engineer,
I want a rule-based Athena route engine v0,
So that M24 improves route fidelity without adopting a generic external router.

**Acceptance Criteria:**

**Given** route intent, terminal anchors, component bounds, and layout context
**When** route engine v0 runs
**Then** it emits grid-aligned horizontal and vertical segments
**And** it avoids component-center attachment
**And** repeated runs on the same input produce identical route facts
**And** no ELK, Graphviz, yFiles, or external generic router is introduced

### Story 2.2: Route from terminal anchors and side stubs

As an IDE reviewer,
I want routes to enter and exit through terminal anchors,
So that connections stop looking like generic graph edges.

**Acceptance Criteria:**

**Given** source and target terminal anchors
**When** route facts are generated
**Then** routes begin and end at anchor points
**And** short grid-aligned stubs leave the preferred side before joining longer segments
**And** route facts never use component centers in the accepted M24 proof

### Story 2.3: Add lane assignment and component avoidance

As an electrical engineer,
I want long routes to use lanes and avoid component bodies,
So that the sheet remains ordered and readable.

**Acceptance Criteria:**

**Given** multiple routed connections in the M24 sample
**When** route engine v0 assigns paths
**Then** long segments use horizontal or vertical routing lanes
**And** routes avoid obvious component body overlap
**And** fallback quality is emitted when avoidance cannot be satisfied

### Story 2.4: Add narrow ordered terminal-strip bundle proof

As Aaron,
I want a small terminal-strip case to show ordered parallel route behavior,
So that M24 moves toward the supplied EPLAN-like reference without trying to clone it.

**Acceptance Criteria:**

**Given** a terminal-strip route scenario inspired by `../../draft/screenshort/coffret_cordons_chauffants.png`
**When** route engine v0 computes routes
**Then** semantically related routes can travel through ordered parallel lanes or bundles
**And** terminal attachment order is stable and readable
**And** the proof remains narrow and does not claim full cabinet or EPLAN parity

## Epic 3: Compiler Projection And Route Quality Diagnostics

Compiler/projection code feeds route intent and route facts into the existing presentation path,
reports degraded/fallback quality, and preserves M23 language behavior.

### Story 3.1: Feed semantic connections into route intent

As a compiler engineer,
I want projection to provide route intent from semantic connections,
So that route generation follows Athena source truth.

**Acceptance Criteria:**

**Given** compiled source with ports and connects
**When** projection prepares schematic routing
**Then** it emits route intent with connection identity, port identities, view/sheet context, and
layout context
**And** it does not derive meaning from renderer positions
**And** route intent is sorted deterministically

### Story 3.2: Integrate route facts into presentation snapshots

As a renderer developer,
I want presentation snapshots to carry route facts,
So that Theia can render routed wires without recalculating meaning.

**Acceptance Criteria:**

**Given** route engine v0 output
**When** presentation/projection snapshots are built
**Then** route facts are available to the Graphical View renderer
**And** existing node/edge identity remains compatible
**And** M23 layout constraints can provide context without being replaced

### Story 3.3: Publish route quality diagnostics and inspection payloads

As an IDE user,
I want degraded or fallback routes to be explainable,
So that routing limits are visible instead of silently pretending to be professional.

**Acceptance Criteria:**

**Given** a route cannot satisfy one or more constraints
**When** diagnostics or inspection payloads are requested
**Then** the affected connection, constraint family, and quality state are visible
**And** normal satisfied routes remain clean
**And** diagnostics do not block rendering of available route facts

### Story 3.4: Preserve M23 syntax and projection regression behavior

As an Athena maintainer,
I want routing work to preserve M23 language admission,
So that M24 does not break layout blocks or active-source projection.

**Acceptance Criteria:**

**Given** M23 layout-block sample and parser fixtures
**When** M24 verification runs
**Then** ANTLR4, Tree-sitter, compiler, LSP, and Theia still accept the M23 sample
**And** active-source Graphical View projection still uses the currently opened `.athena` file
**And** no route feature introduces new source syntax without dual-parser parity

## Epic 4: Theia Route Rendering And Inspection

Theia renders terminal-anchor route facts, exposes route identity/quality inspection, and keeps the
canvas paint-only with accepted workbench behavior intact.

### Story 4.1: Render route facts as terminal-anchor schematic wires

As a reviewer,
I want Theia to render route facts from terminal anchors,
So that wires visually attach to ports/terminals instead of component centers.

**Acceptance Criteria:**

**Given** route facts in the Graphical View model
**When** Theia renders the sheet
**Then** wires begin/end at terminal anchors
**And** route segments are orthogonal and grid-aligned
**And** the accepted M24 proof has no renderer-side center-to-center fallback

### Story 4.2: Render lanes, bundles, crossings, and labels readably

As an electrical engineer,
I want route details to remain readable,
So that the sheet looks ordered under moderate connection density.

**Acceptance Criteria:**

**Given** the M24 terminal-strip acceptance case
**When** Graphical View renders it
**Then** ordered parallel lanes or bundles are visible
**And** crossings are deliberate and distinguishable
**And** route labels or signal markers do not cover component bodies in the accepted sample

### Story 4.3: Add route inspection without canvas ownership

As an IDE user,
I want to inspect a route's source connection and quality,
So that rendered wires remain traceable to Athena semantics.

**Acceptance Criteria:**

**Given** a rendered route is selected or inspected
**When** the inspector or route status surface opens
**Then** it shows source connection identity, source/target ports, quality state, and route policy
summary where available
**And** it does not create or persist hidden route coordinates

### Story 4.4: Preserve accepted Graph Workbench behavior

As Aaron,
I want routing improvements without UI regressions,
So that M24 does not reopen the M20-M23 canvas issues.

**Acceptance Criteria:**

**Given** the M24 sample project is open in Theia
**When** Graphical View is used
**Then** the grid remains the coordinate surface
**And** floating controls remain transparent
**And** the top information popover behavior remains unchanged
**And** outline navigation keeps the same `.athena` editor tab

## Epic 5: M24 Sample Proof, Usage, And Boundary Guardrails

Reviewers can open a real M24 sample project, compare M23 vs M24 routing, and verify that M24 stays
schematic-only and does not overclaim EPLAN or physical routing parity.

### Story 5.1: Create the openable M24 routing sample project

As a product reviewer,
I want a real M24 sample project,
So that I can test routing fidelity through the IDE.

**Acceptance Criteria:**

**Given** `examples/m24/sample-project`
**When** it is opened in Athena Theia
**Then** it contains real `.athena` files with PLC-HMI, PLC-terminal-load, 24V power/protection, and
terminal-strip route cases
**And** the sample opens without false syntax diagnostics
**And** Graphical View projects the active M24 source

### Story 5.2: Add M23-vs-M24 routing acceptance proof

As Aaron,
I want explicit comparison evidence,
So that I can see what M24 improved and what remains deferred.

**Acceptance Criteria:**

**Given** the M24 sample and M23 baseline behavior
**When** acceptance docs and tests are reviewed
**Then** they name visible changes from graph-like edges to terminal-anchor route facts
**And** they include the directional reference image path
**And** they state that full EPLAN/cabinet routing parity is not claimed

### Story 5.3: Add M24 Electron smoke and route regression coverage

As an Athena maintainer,
I want product-path smoke tests for M24,
So that route rendering does not pass only in unit tests.

**Acceptance Criteria:**

**Given** the M24 sample project
**When** product smoke runs
**Then** it opens the sample in Theia
**And** it proves terminal-anchor route rendering exists
**And** it proves no center-to-center fallback is used in the accepted proof
**And** failures include actionable route/projection state

### Story 5.4: Publish M24 usage, retrospective hooks, and boundary checks

As Aaron,
I want clear usage and honest boundary documentation,
So that M24 can be presented without overclaiming.

**Acceptance Criteria:**

**Given** M24 is implemented
**When** usage docs and boundary checks are published
**Then** they identify `examples/m24/sample-project` and expected IDE checks
**And** they record schematic-only routing scope, deferred physical routing, deferred EPLAN parity,
and no generic-router architecture
**And** they preserve M23 layout syntax regression expectations
