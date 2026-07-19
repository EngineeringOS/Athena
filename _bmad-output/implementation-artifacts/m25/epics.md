---
stepsCompleted:
  - extract-m25-requirements
  - design-m25-epics
  - create-m25-stories
  - validate-m25-coverage
inputDocuments:
  - ../../planning-artifacts/prds/prd-Athena-2026-07-19-m25/prd.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-19-m25/addendum.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-19-m25/review-rubric.md
  - ../../planning-artifacts/architecture/architecture-Athena-2026-07-19-m25/ARCHITECTURE-SPINE.md
---

# Athena - M25 Epic Breakdown

## Overview

M25 proves governed engineering representation and presentation policy. The milestone follows M24
routing fidelity by making routed schematic subjects look like professional engineering
representations instead of generic boxes. It introduces a representation model above schematic
symbols, keeps Presentation IR as the renderer bridge, separates semantic ports, physical terminals,
and presentation terminals, treats labels as semantic presentation facts, and proves the result in
the Theia IDE.

M25 is not full EPLAN parity, not full IEC/QElectroTech library ingestion, not a symbol editor, not
a public repository milestone, and not desktop-viewer/KMP/Compose frontend work.

## Requirements Inventory

### Functional Requirements

FR1: Provide an openable M25 sample project.

FR2: Define representation and notation acceptance references using QElectroTech/IEC-style anatomy
as directional vocabulary only.

FR3: Define a presentation anatomy model with symbol anatomy as the electrical schematic subset.

FR4: Define one active presentation policy profile named `athena-industrial-control-v0`.

FR5: Compose symbols from component knowledge; accepted proof has zero generic fallback symbols.

FR6: Preserve Presentation IR as the bridge to rendering.

FR7: Render terminal notation facts with semantic port, physical terminal, and presentation terminal
kept distinct.

FR8: Render label policy facts as semantic presentation facts.

FR9: Preserve source, outline, inspector, Problems, graph, symbol, terminal, label, and route
identity coherence.

FR10: Publish M25 usage and executable evidence.

### NonFunctional Requirements

NFR1: Authority remains source -> compiler semantic model -> component knowledge -> presentation
policy -> Presentation IR -> Theia renderer.

NFR2: Theia and renderer paint/inspect facts only; they do not infer component, terminal, label, or
route meaning.

NFR3: Presentation anatomy is the general model; symbol anatomy is only the electrical schematic
subset.

NFR4: `athena-industrial-control-v0` is vendor-neutral and does not claim IEC completeness.

NFR5: Accepted M25 proof must contain zero generic fallback symbols.

NFR6: QElectroTech is reference vocabulary only; no `.elmt` ingestion or runtime dependency.

NFR7: No new `.athena` syntax unless ANTLR4, Tree-sitter, compiler, LSP, tests, sample, and docs
are updated together.

NFR8: Desktop-viewer, Compose, and deprecated KMP frontend modules are out of scope.

### Additional Requirements

- Architecture AD-1: Representation model is above symbol model.
- Architecture AD-2: Presentation IR remains the rendering bridge.
- Architecture AD-3: Presentation policy profile owns representation choice.
- Architecture AD-4: Component knowledge compiles to presentation facts.
- Architecture AD-5: Terminal meaning is layered.
- Architecture AD-6: Labels are semantic presentation facts.
- Architecture AD-7: Accepted proof has zero generic fallback symbols.
- Architecture AD-8: QElectroTech is reference vocabulary only.
- Architecture AD-9: Theia IDE is the only frontend scope.
- Architecture AD-10: No new source syntax by default.

### UX Design Requirements

UX-DR1: A reviewer can open `examples/m25/sample-project` in the normal Athena Theia IDE workflow.

UX-DR2: The M25 Graphical View visibly differs from M24 by showing presentation anatomy, terminal
markers, terminal numbers, and label anchors.

UX-DR3: The mandatory path proves PLC/controller, terminal block, power supply, and load/actuator
with zero generic fallback symbols.

UX-DR4: Terminal and label inspection can name canonical identities and roles where supported.

UX-DR5: M24 route quality remains intact and attaches to M25 presentation terminals.

UX-DR6: Active-source projection, same-tab outline navigation, grid-backed canvas, transparent
controls, and top-popover behavior do not regress.

### FR Coverage Map

FR1: Epic 4 - sample project and IDE proof.

FR2: Epic 4 - acceptance references and QElectroTech mapping documentation.

FR3: Epic 1 - presentation anatomy and symbol anatomy contracts.

FR4: Epic 1 - active presentation policy profile.

FR5: Epic 2 and Epic 4 - component knowledge composition and zero-fallback proof.

FR6: Epic 2 - Presentation IR integration.

FR7: Epic 1, Epic 2, and Epic 3 - terminal model, projection, and rendering.

FR8: Epic 1, Epic 2, and Epic 3 - label facts, projection, and rendering.

FR9: Epic 3 - IDE coherence and inspection.

FR10: Epic 4 - usage, smoke, retrospective hooks, and boundary guardrails.

## Epic List

### Epic 1: Representation Model And Policy Contracts
Athena introduces representation-model and presentation-policy contracts for presentation anatomy,
schematic symbol anatomy, terminal notation, label facts, and the
`athena-industrial-control-v0` profile.
**FRs covered:** FR3, FR4, FR7, FR8

### Epic 2: Presentation IR Composition And Route Integration
Compiler/runtime projection composes component knowledge into Presentation IR facts and connects
those facts to M24 route anchors without bypassing semantic authority.
**FRs covered:** FR5, FR6, FR7, FR8

### Epic 3: Theia Representation Rendering And Inspection
Theia renders presentation anatomy, terminal notation, labels, and route attachments as paint-only
facts while preserving active-source, selection, outline, Problems, and inspector coherence.
**FRs covered:** FR7, FR8, FR9

### Epic 4: M25 Sample Proof, Usage, And Boundary Guardrails
Reviewers can open a real M25 sample project, compare M24 vs M25 visual behavior, and verify zero
fallback symbols, usage docs, smoke evidence, and out-of-scope boundaries.
**FRs covered:** FR1, FR2, FR5, FR10

## Epic 1: Representation Model And Policy Contracts

Athena introduces representation-model and presentation-policy contracts for presentation anatomy,
schematic symbol anatomy, terminal notation, label facts, and the
`athena-industrial-control-v0` profile.

### Story 1.1: Create representation-model contract home

As an Athena architect,
I want `kernel/representation-model` to own presentation anatomy contracts,
So that schematic symbols do not become the root architecture.

**Acceptance Criteria:**

**Given** M25 representation concepts are introduced
**When** representation-model compiles
**Then** it exposes contracts for representation ids, presentation anatomy, bounds, hotspots,
presentation primitives, terminal points, and label anchors
**And** `SymbolAnatomy` is represented as the electrical schematic subset
**And** the model has no dependency on Theia, DOM, canvas state, or QElectroTech runtime code

### Story 1.2: Define presentation policy profile contracts

As a presentation-policy maintainer,
I want `athena-industrial-control-v0` represented as a governed profile,
So that component appearance is selected by policy rather than renderer hardcoding.

**Acceptance Criteria:**

**Given** component family and representation context
**When** profile selection runs
**Then** `athena-industrial-control-v0` can select supported representations for PLC/controller,
HMI/operator, terminal block, power supply, protection device, and load/actuator
**And** the profile does not claim IEC completeness
**And** unsupported families produce diagnosable fallback metadata

### Story 1.3: Model semantic ports, physical terminals, and presentation terminals

As an electrical modeling engineer,
I want terminal meaning split across semantic, physical, and presentation layers,
So that terminal notation remains engineering-owned instead of coordinate-owned.

**Acceptance Criteria:**

**Given** component ports and optional terminal assignments
**When** terminal facts are built
**Then** semantic ports, physical terminals, and presentation terminals are distinct
**And** presentation terminals carry marker, number, side, route anchor, subject id, occurrence id,
port id, and terminal id
**And** tests prove terminal numbers are not derived from renderer text

### Story 1.4: Define label facts and label policy

As a projection engineer,
I want labels represented as semantic presentation facts,
So that labels are inspectable and not raw text drawing calls.

**Acceptance Criteria:**

**Given** device tags, component labels, terminal labels, and route labels
**When** label policy applies
**Then** label facts carry subject id, occurrence id, role, value, anchor, and source identity where
available
**And** label anchors are deterministic
**And** renderer code consumes label facts without owning label semantics

## Epic 2: Presentation IR Composition And Route Integration

Compiler/runtime projection composes component knowledge into Presentation IR facts and connects
those facts to M24 route anchors without bypassing semantic authority.

### Story 2.1: Compose component knowledge into representation facts

As a runtime/projection engineer,
I want component family, role, ports, and terminal definitions to compose into representation facts,
So that visible schematic subjects are generated from meaning.

**Acceptance Criteria:**

**Given** the M25 sample semantic model
**When** representation composition runs
**Then** PLC/controller, terminal block, power supply, and load/actuator produce supported
presentation anatomy facts
**And** HMI/operator and protection device produce supported facts when present
**And** the accepted proof uses zero generic fallback symbols

### Story 2.2: Integrate representation facts into Presentation IR

As a presentation pipeline maintainer,
I want representation, symbol, terminal, and label facts carried by Presentation IR,
So that M25 does not bypass the M13 presentation layer.

**Acceptance Criteria:**

**Given** representation composition output
**When** a presentation snapshot is built
**Then** the snapshot includes representation facts, symbol facts, terminal facts, label facts,
route anchors, and occurrence identity
**And** existing M24 route facts remain present
**And** tests prove Theia can consume the payload without resolving representation policy

### Story 2.3: Attach M24 route facts to presentation terminals

As a routing integration engineer,
I want M24 routes to attach to M25 presentation terminals,
So that improved wire routes connect to professional terminal notation.

**Acceptance Criteria:**

**Given** terminal facts and M24 route facts
**When** route integration runs
**Then** route endpoints reference presentation terminal anchors
**And** center fallback is absent from the accepted M25 proof
**And** route quality remains satisfied for the mandatory acceptance path

### Story 2.4: Report fallback and policy coverage diagnostics

As a reviewer,
I want fallback and policy gaps to be visible,
So that unsupported representations are not mistaken for M25 success.

**Acceptance Criteria:**

**Given** an unsupported component family or missing terminal notation
**When** representation composition runs
**Then** it emits visible diagnosable fallback metadata
**And** accepted M25 sample verification fails if any mandatory path subject uses fallback
**And** diagnostics include component family, policy profile, and missing capability

## Epic 3: Theia Representation Rendering And Inspection

Theia renders presentation anatomy, terminal notation, labels, and route attachments as paint-only
facts while preserving active-source, selection, outline, Problems, and inspector coherence.

### Story 3.1: Render presentation primitives and schematic symbol anatomy

As an IDE user,
I want components to render as governed engineering representations,
So that the sheet no longer looks like generic graph boxes.

**Acceptance Criteria:**

**Given** Presentation IR representation facts
**When** Graphical View renders the M25 sample
**Then** supported symbols render from primitives, bounds, and hotspots
**And** generic fallback styling is absent from the accepted proof
**And** renderer code remains paint-only

### Story 3.2: Render presentation terminals and label facts

As an electrical reviewer,
I want terminal markers, terminal numbers, and labels visible at anchors,
So that route endpoints and component identity are readable.

**Acceptance Criteria:**

**Given** terminal and label facts
**When** Theia renders the sheet
**Then** terminal marker shape plus terminal number is visible for accepted terminals
**And** device tags and component labels render at deterministic anchors
**And** labels do not cover accepted route lines

### Story 3.3: Add representation, terminal, and label inspection

As an IDE user,
I want selected symbols, terminals, labels, and routes to reveal their identities,
So that graphical inspection remains tied to source semantics.

**Acceptance Criteria:**

**Given** a rendered M25 sheet
**When** a user selects a symbol, terminal, label, or route
**Then** inspection payloads expose canonical subject, occurrence, terminal, port, label role, and
route identity where applicable
**And** source reveal uses existing identity paths
**And** no duplicate editor panel opens for the same source file

### Story 3.4: Preserve accepted Graph Workbench behavior and frontend boundary

As a product owner,
I want M25 to keep accepted M20-M24 IDE behavior,
So that symbol work does not regress the actual product shell.

**Acceptance Criteria:**

**Given** the M25 sample runs in Theia
**When** users pan, zoom, inspect, reveal, and switch source files
**Then** active-source projection, same-tab outline navigation, grid-backed canvas, transparent
controls, and top-popover behavior remain intact
**And** no desktop-viewer, Compose, or deprecated KMP frontend module is changed

## Epic 4: M25 Sample Proof, Usage, And Boundary Guardrails

Reviewers can open a real M25 sample project, compare M24 vs M25 visual behavior, and verify zero
fallback symbols, usage docs, smoke evidence, and out-of-scope boundaries.

### Story 4.1: Create the openable M25 sample project

As Aaron,
I want a real M25 sample project with `.athena` sources,
So that I can present M25 in the IDE without explaining scripts.

**Acceptance Criteria:**

**Given** `examples/m25/sample-project`
**When** it is opened in Athena Theia IDE
**Then** it contains real `.athena` files for the six sample families
**And** the mandatory path includes PLC/controller, terminal block, power supply, and load/actuator
**And** source syntax is accepted by the existing language stack

### Story 4.2: Add M24-vs-M25 representation acceptance proof

As a reviewer,
I want a documented comparison from M24 to M25,
So that symbol and terminal improvements are concrete.

**Acceptance Criteria:**

**Given** M24 and M25 proof data
**When** acceptance docs are generated
**Then** they compare generic-box M24 representation against M25 presentation anatomy
**And** they identify terminal markers, terminal numbers, label anchors, and zero-fallback proof
**And** they include one documentation-only QElectroTech-inspired anatomy mapping example

### Story 4.3: Add M25 product smoke and regression coverage

As a developer,
I want product-path tests for M25,
So that Theia proof failures are caught before user review.

**Acceptance Criteria:**

**Given** the M25 sample project
**When** product smoke runs
**Then** it verifies rendered representation facts, terminal facts, label facts, route attachments,
zero fallback symbols, and active-source behavior
**And** M24 route quality regression checks still pass
**And** verification commands are documented

### Story 4.4: Publish M25 usage, retrospective hooks, and boundary checks

As a product owner,
I want M25 usage and boundary docs,
So that the milestone can be demonstrated honestly.

**Acceptance Criteria:**

**Given** M25 implementation is complete
**When** documentation is reviewed
**Then** `docs/usages/m25-proof-usage.md` explains how to open and test the sample
**And** the implementation retrospective records achievements, usage, deferred work, and lessons
**And** stale docs do not claim QElectroTech import, full IEC/EPLAN parity, new syntax, or desktop
frontend scope
