---
title: Athena M25 - Engineering Representation And Presentation Policy Foundation
status: draft
created: 2026-07-19
updated: 2026-07-19
---

# PRD: Athena M25 - Engineering Representation And Presentation Policy Foundation

## 0. Document Purpose

M25 follows M24 by addressing the next visible credibility gap in Athena's customer-facing schematic
workflow: routes now attach and fold more like engineering connections, but component symbols,
terminal notation, labels, and presentation conventions are still too generic.

M24 proved that Athena can derive governed schematic route facts from semantic connections and
terminal anchors. M25 proves that those routes connect to professional, semantically governed
representation and notation facts rather than anonymous boxes with labels.

M25 is not full EPLAN parity, not a full IEC/QElectroTech library ingestion milestone, not a symbol
editor, not a public repository milestone, and not a desktop-viewer milestone. It is the first
professional engineering representation and presentation policy foundation for the Theia IDE sheet
workflow.

## 1. Vision

An engineer opening Athena should see that a PLC, HMI, power supply, breaker, terminal block, and
load are represented by purposeful engineering notation. The visible result should no longer feel
like a graph with better wires. It should begin to feel like an electrical engineering sheet whose
symbols, terminals, labels, and routes come from governed component knowledge.

The target pipeline is:

```text
.athena source devices / ports / connects / layout hints
    -> compiler semantic model
    -> component knowledge
    -> presentation policy profile
    -> presentation compiler
    -> Presentation IR
    -> presentation anatomy facts
    -> terminal and label notation facts
    -> sheet occurrences
    -> M24 route facts
    -> paint-only Theia renderer
```

Athena must keep the direction that differentiates it from traditional CAD:

```text
component knowledge -> presentation primitive -> symbol composition -> sheet occurrence
```

not:

```text
symbol drawing -> inferred meaning
```

The renderer may paint representation, symbol, terminal, label, and route facts. It must not invent
component meaning, terminal identity, notation rules, label authority, or hidden symbol state from
DOM or canvas geometry.

## 1.1 Why Now

M19 proved that Athena can produce a schematic sheet. M20 made the sheet surface acceptable. M21
introduced layout intelligence. M22 defined governed layout optimization and round-trip direction.
M23 admitted layout intent into the Athena language. M24 improved connection routing so wires no
longer need to fall back to center-to-center graph edges in the accepted proof.

The next user-visible limitation is now clear: professional routes attached to generic component
boxes still do not create an EPLAN-class impression. Engineers trust the artifact when terminals,
pin notation, device tags, representation geometry, label positions, and connection anchors all look
like engineering documentation.

M25 should therefore establish the governed representation and notation policy layer before broad
library ingestion, AI symbol generation, or deeper multi-sheet documentation work.

## 2. Target User

### 2.1 Jobs To Be Done

- Aaron needs to open an M25 sample project and immediately see visible professional representation
  and terminal notation improvement beyond M24.
- Maya needs ports and labels to be recognizable as engineering terminals, not only abstract graph
  connection points.
- Priya needs a customer-facing proof that Athena can make semantic engineering data look like a
  real schematic artifact.
- Winston needs representation and notation behavior to remain governed by component knowledge,
  presentation policy, and projection facts rather than renderer-local drawing choices.

### 2.2 Non-Users

- Teams expecting full EPLAN visual parity in one milestone
- Teams expecting full IEC, QElectroTech, manufacturer, or company-standard library breadth
- Teams expecting a symbol authoring UI
- Teams expecting QElectroTech `.elmt` import as the architecture
- Teams expecting public repository, package marketplace, or library distribution work
- Teams expecting desktop-viewer, Compose, or deprecated KMP frontend changes
- Teams expecting canvas-local symbol editing or renderer-owned meaning

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a symbol-fidelity proof.**
  - **Context:** Aaron opens `examples/m25/sample-project` in the Athena Theia IDE.
  - **Path:** He opens the main `.athena` source and reveals Graphical View.
  - **Climax:** PLC, HMI, terminal block, power/protection, and load subjects use visible terminal
    notation, label anchors, and component-family presentation anatomy instead of generic boxes.
  - **Resolution:** The sheet looks like the next step after M24 routing, not a separate demo.

- **UJ-2. Maya validates terminal readability.**
  - **Context:** Maya inspects a PLC output connected through a terminal block to a load.
  - **Path:** She checks port labels, terminal numbers, connection anchors, and route labels.
  - **Climax:** She can tell which route attaches to which terminal without reading raw source.
  - **Resolution:** Terminal identity is visually inspectable and still traceable to canonical
    source identity.

- **UJ-3. Winston validates presentation authority.**
  - **Context:** Winston reviews the model and Theia rendering.
  - **Path:** He traces `.athena` semantics through component knowledge, presentation policy,
    Presentation IR, presentation anatomy, notation facts, route facts, and renderer output.
  - **Climax:** The renderer paints facts only and does not infer symbol meaning from canvas state.
  - **Resolution:** M25 improves professional presentation without turning Athena into a drawing
    program.

## 3. Glossary

- **Engineering Representation Model** - A domain-neutral model for how engineering meaning becomes
  a visible representation in a given context, such as electrical schematic, cabinet, HMI, SCADA,
  3D, documentation, or future AI inspection views.
- **Presentation Anatomy** - A governed description of a component family's visible presentation for
  one representation context: primitive graphics, terminal points, hotspots, label anchors, dynamic
  text anchors, and bounds.
- **Symbol Anatomy** - The electrical schematic subset of presentation anatomy: a governed
  description of a component family's visible schematic body:
  primitive graphics, terminal points, hotspots, label anchors, dynamic text anchors, and bounds.
- **Presentation Policy Profile** - A named rule set that determines how component knowledge should
  be presented in a representation context. The M25 proof uses
  `athena-industrial-control-v0`.
- **Presentation IR** - The renderer-facing projection layer that carries representation, symbol,
  terminal, label, route, and occurrence facts while keeping meaning above the renderer.
- **Terminal Notation Policy** - Rules for terminal marker shape, terminal number visibility, port
  label placement, and anchor naming.
- **Label Policy** - Rules for device tags, type labels, terminal labels, route labels, and dynamic
  text anchors.
- **Symbol Composition** - The process of turning component knowledge and presentation policy into
  symbol anatomy and occurrence-specific notation facts.
- **Sheet Occurrence** - A placed appearance of a semantic subject on a sheet, carrying canonical
  subject identity, occurrence identity, symbol facts, terminal facts, labels, and route anchors.
- **Semantic Port** - A model-level connection capability such as a digital output, power input, or
  communication port.
- **Physical Terminal** - A real terminal or pin assignment such as `X1:14`, derived from component
  knowledge or authored engineering data.
- **Presentation Terminal** - The visible terminal marker, number, and route anchor shown on a sheet
  for a semantic port or physical terminal.
- **QElectroTech-Inspired Anatomy** - A reference vocabulary of symbols, terminals, hotspots,
  labels, and graphics used as inspiration. M25 does not import QET as a product library.

## 4. Features

### 4.1 Openable M25 Representation And Notation Proof

**Description:** M25 starts with a real sample project that demonstrates professional
representation, terminal, and label improvement inside the Theia IDE.

#### FR-1: Provide an openable M25 sample project

Athena provides `examples/m25/sample-project` with real `.athena` files that exercise symbol
anatomy, terminal notation, label policy, and M24 routing together.

**Consequences:**

- The project opens through the normal Athena Theia workflow.
- Reviewers do not need to inspect `.mjs` files to understand the proof.
- The sample includes the six M25 component families: PLC/controller, HMI/operator device, terminal
  block, power supply, protection device, and load/actuator.
- The mandatory acceptance path focuses on PLC/controller, terminal block, power supply, and
  load/actuator.
- HMI/operator device and protection device may appear as secondary sample cases, but they must not
  expand the milestone into broad library work.
- The sample demonstrates at least one connection path where terminal notation matters:
  controller output -> terminal strip -> load.
- The sample reuses M24 route facts; M25 does not regress route quality.

#### FR-2: Define representation and notation acceptance references

Athena documents acceptance expectations using QElectroTech and IEC-style element anatomy as
directional references.

**Consequences:**

- Acceptance checks name terminal markers, terminal numbers, device tags, label anchors, symbol
  bounds, hotspots, line primitives, and dynamic text placeholders.
- The proof states how M25 differs from M24 generic visual components.
- QElectroTech references are used for anatomy vocabulary, not as a requirement to import its full
  element collection.
- The acceptance bar is "professional symbol and terminal notation foundation," not full EPLAN or
  IEC library parity.

### 4.2 Governed Representation Anatomy And Presentation Policy

**Description:** Athena introduces governed representation, symbol, and notation contracts above the
renderer.

#### FR-3: Define a presentation anatomy model with symbol anatomy as the schematic subset

Athena defines a model for component-family presentation anatomy including graphics primitives,
terminal points, hotspots, label anchors, dynamic text anchors, and representation bounds.
Electrical schematic symbol anatomy is the first M25 use of that broader model.

**Consequences:**

- Presentation anatomy can express simple professional symbols without requiring a full external
  library.
- Terminal points are first-class facts, not incidental drawing coordinates.
- Terminal points carry semantic role information such as power input, digital output,
  communication, protective earth, or terminal transition where available.
- Hotspots and bounds support deterministic placement and route attachment.
- The model can later support IEC, company, or imported library profiles without changing renderer
  authority.

#### FR-4: Define presentation policy profiles

Athena defines one active presentation policy profile for the M25 schematic proof:
`athena-industrial-control-v0`.

**Consequences:**

- The initial profile is named `athena-industrial-control-v0`.
- The name stays vendor-neutral and does not claim IEC completeness.
- The profile governs symbol selection, terminal notation, label placement, terminal marker style,
  and device tag visibility.
- The policy is small and explicit.
- The renderer consumes profile-derived facts; it does not hardcode component family meaning.

#### FR-5: Compose symbols from component knowledge

Athena maps component knowledge into symbol composition facts before rendering.

**Consequences:**

- Component family, role, ports, terminal definitions, and labels drive the visible symbol.
- The direction remains meaning-to-symbol, not symbol-to-meaning.
- Generic fallback symbols remain allowed only as diagnosable fallback for unsupported component
  families.
- The accepted M25 proof must contain zero generic fallback symbols.
- Any fallback use outside the accepted proof must be visible in tests or proof metadata.

#### FR-6: Preserve Presentation IR as the bridge to rendering

M25 symbol, terminal, label, and route facts must flow through Athena's Presentation IR rather than
bypassing the M13 presentation layer.

**Consequences:**

- Component knowledge and presentation policy feed a presentation compiler or equivalent projection
  step.
- Presentation IR carries representation facts, symbol facts, terminal facts, label facts, route
  anchors, and occurrence identity.
- Theia consumes Presentation IR facts; it does not resolve component representation rules locally.

### 4.3 Terminal And Label Notation Fidelity

**Description:** M25 makes terminal and label facts visibly useful in the sheet.

#### FR-7: Render terminal notation facts

Athena renders terminal points and terminal labels from governed facts.

**Consequences:**

- Terminal notation includes stable terminal markers and readable terminal numbers.
- Terminal labels stay near their terminal anchors without covering route lines.
- Terminal facts carry canonical subject, port, terminal, occurrence, and source identities.
- Terminal modeling separates semantic port, physical terminal, and presentation terminal.
- M24 route facts attach to M25 terminal facts without center fallback in the accepted proof.

#### FR-8: Render label policy facts

Athena renders device tags, component labels, terminal labels, and route labels from label policy
facts.

**Consequences:**

- Device tags and type labels have predictable anchors.
- Labels remain readable at accepted IDE zoom levels.
- Label placement does not become frontend-owned free text.
- Labels are semantic presentation facts, not raw `drawText` calls.
- Label facts carry subject identity, label role, value, anchor, and source identity where
  applicable.
- Label source identity remains available for reveal and inspector behavior.

### 4.4 IDE Coherence And Verification

**Description:** M25 keeps the Theia IDE workflow coherent while improving visual fidelity.

#### FR-9: Preserve source, outline, inspector, problems, graph, and route identity coherence

Representation and notation facts must round-trip through the same canonical subject and occurrence
identities used by M24 routes.

**Consequences:**

- Selecting a symbol, terminal, or route reveals the correct source subject where supported.
- Outline and Problems behavior must not regress for the M25 sample.
- Graphical View must update for the currently opened `.athena` file, not stale sample state.
- The proof must explicitly check that accepted symbol and terminal facts are present in the IDE
  rendering path.

#### FR-10: Publish M25 usage and evidence

Athena publishes usage documentation and executable proof for M25.

**Consequences:**

- `docs/usages/m25-proof-usage.md` explains how to open and test the M25 sample.
- The implementation retrospective must state what M25 proves and what remains deferred.
- Product smoke or equivalent IDE-path verification must prove the sample opens and visible symbol
  facts are rendered.
- Verification must be project-aware, not only single-file compiler tests.

## 5. Non-Goals

- Full EPLAN visual parity
- Full IEC standards library breadth
- Full QElectroTech `.elmt` ingestion or direct runtime dependency
- Public Maven/npm-style engineering repository or symbol marketplace
- Symbol authoring UI
- Free-form canvas-local symbol drawing
- Renderer-owned component or terminal meaning
- Route editing or route-hint syntax expansion
- Physical cabinet routing, harness routing, cable tray routing, or 3D routing
- AI-generated symbols or AI presentation optimization
- Desktop-viewer, Compose, or deprecated KMP frontend work
- New `.athena` syntax unless ANTLR4, Tree-sitter, compiler, LSP, tests, and sample docs are all
  updated together

## 6. MVP Scope

M25 MVP includes:

- One openable Theia sample project at `examples/m25/sample-project`
- A small governed presentation anatomy model with symbol anatomy as the electrical schematic subset
- One active presentation policy profile named `athena-industrial-control-v0`
- Visible professional notation for six sample component families:
  - PLC/controller
  - HMI/operator device
  - terminal block
  - power supply
  - protection device
  - load device
- A mandatory acceptance path using:
  - PLC/controller
  - terminal block
  - power supply
  - load/actuator
- Terminal marker and terminal label facts
- Terminal marker plus terminal number as the minimum accepted terminal notation
- Separation of semantic port, physical terminal, and presentation terminal
- Device tag and component label facts
- Label facts treated as semantic presentation objects
- M24 route-fact integration with M25 terminal anchors
- Zero generic fallback symbols in the accepted proof
- Usage docs and implementation retrospective
- Product-path verification that proves the sample renders symbol and terminal facts in Theia

M25 MVP does not include:

- Broad library coverage
- Standards-complete IEC symbol rules
- User-authored symbol libraries
- Import from QElectroTech collections
- Pixel-perfect comparison to EPLAN or QElectroTech

## 7. Success Metrics

- **SM-1:** The M25 sample opens in the Athena Theia IDE using normal project workflow.
- **SM-2:** The rendered sheet visibly differs from M24 by using presentation anatomy, terminal
  markers, and label anchors rather than only generic component boxes.
- **SM-3:** Accepted terminal and label facts carry canonical subject, occurrence, port, terminal,
  and source identities where applicable.
- **SM-4:** M24 route quality remains intact for the accepted route proof.
- **SM-5:** The accepted proof includes no renderer-owned symbol meaning or canvas-local hidden
  symbol state.
- **SM-6:** Generic fallback symbols are absent from the accepted M25 proof.
- **SM-7:** The M25 usage doc gives a reviewer a concrete IDE path to see the result.
- **SM-8:** No desktop-viewer, Compose, or deprecated KMP frontend module is touched for M25 scope.

## 8. Assumptions Index

- **A-1:** M24 route facts and terminal-anchor concepts are available as the routing baseline for
  M25.
- **A-2:** M25 can improve professional credibility with a small curated component subset rather
  than a full IEC or QElectroTech library.
- **A-3:** QElectroTech element anatomy is a useful reference vocabulary, but Athena must invert the
  authority direction: meaning produces symbols.
- **A-4:** Theia IDE remains the only frontend proof surface for M25.
- **A-5:** No new `.athena` syntax is required for the initial M25 proof unless the full parser and
  IDE language stack are upgraded together.

## 9. Open Questions

1. Which QElectroTech element should be used for the single documentation-only anatomy mapping
   example?
2. Should HMI/operator and protection device be required in the first product smoke, or remain
   secondary sample fixtures behind the four-family mandatory acceptance path?
3. Should signal labels remain route-label facts in M25, or should M25 add a separate signal-label
   presentation role for terminals?
