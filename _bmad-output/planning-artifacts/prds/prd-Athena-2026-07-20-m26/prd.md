---
title: Athena M26 - Semantic Document Projection Foundation
status: draft
created: 2026-07-20
updated: 2026-07-20
---

# PRD: Athena M26 - Semantic Document Projection Foundation

## 0. Document Purpose

M26 follows M25 by moving Athena from a professional single-sheet schematic proof toward governed
semantic document projection. M24 made connection routes look more like engineering conductors. M25
made symbols, terminals, and labels come from governed presentation policy. M26 addresses the next
product credibility gap: one semantic engineering model must be able to project into multiple
document-oriented views with stable occurrences, continuation markers, and cross references.

M26 is not a publishing milestone, not full EPLAN page management, not standards-complete cross
reference formatting, and not broad IEC/QElectroTech library work. It is the first foundation for
document projection from the Athena semantic model.

## 1. Vision

Athena does not create drawings as source truth. Athena projects semantic engineering knowledge into
human and machine consumable views.

An engineer opening Athena should be able to move from one professional-looking schematic sheet to a
small document projection: power distribution, control, and field wiring can appear as separate sheet
views while remaining projections of one governed workspace project. Cross-view routes and repeated
component references should show compact engineering references, not raw graph labels or
renderer-local state.

The target pipeline is:

```text
.athena source devices / ports / connects / layout hints
    -> compiler semantic model
    -> projection snapshot
    -> Document Projection Policy
    -> Document Projection IR
    -> document occurrences and sheet views
    -> continuation and cross-reference facts
    -> Presentation Policy/Profile
    -> Presentation IR
    -> paint-only Theia renderer
```

Athena must keep the same authority direction established from M19 through M25:

```text
engineering meaning -> projection -> Document Projection IR -> Presentation IR -> renderer
```

not:

```text
canvas page state -> inferred engineering relationship
```

Theia may show sheet views, navigate between references, reveal related subjects, and inspect
document projection facts. It must not decide what a sheet means, invent cross references from screen
geometry, or persist hidden page state outside governed projection contracts.

The `.athena` project remains the single source of truth. M26 must not introduce a separate document
file, page database, renderer cache, or UI state that can become authoritative over engineering
meaning, document projection identity, occurrence membership, or cross-reference relationships.

A `.athena` source file is not a document page. Source files may contain shared components,
packages, or systems; sheet views are materialized occurrences over the projected semantic graph.

## 1.1 Why Now

M19 proved that Athena can generate a schematic sheet. M20 made the sheet surface acceptable. M21
introduced layout intent and layout facts. M22 introduced governed layout optimization direction.
M23 admitted layout intent into the Athena language. M24 improved route fidelity. M25 added governed
presentation anatomy, terminal notation, and presentation policy.

The next visible limitation is projection scale. A professional symbol with a professional route is
still not enough if the workspace project cannot be projected into multiple document views, if
cross-view continuations are unclear, or if the IDE cannot reveal where a referenced occurrence
lives.

M26 should therefore introduce Document Projection IR, deterministic sheet-view materialization,
compact cross-reference facts, and IDE navigation across document occurrences before deeper
publishing, standards packs, or library ingestion.

## 2. Target User

### 2.1 Jobs To Be Done

- Aaron needs to open an M26 sample project and see a small semantic document projection, not another
  single-canvas demo.
- Maya needs to follow a power or control connection when it leaves one sheet view and continues in
  another.
- Priya needs a customer-facing proof that Athena can present professional document views while
  keeping semantic authority upstream.
- Winston needs document occurrences and cross-reference markers to stay derived from canonical
  projection identities rather than Theia-local page state.

### 2.2 Non-Users

- Teams expecting full EPLAN project/documentation parity
- Teams expecting final PDF export, print layout, plot settings, or revision workflow
- Teams expecting complete IEC cross-reference formatting or company-standard document packs
- Teams expecting automatic large-project pagination
- Teams expecting a free-form drawing page editor
- Teams expecting canvas-local wires, page breaks, or hidden reference state
- Teams expecting desktop-viewer, Compose, or deprecated KMP frontend changes

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a document projection proof.**
  - **Context:** Aaron opens `examples/m26/sample-project` in the Athena Theia IDE.
  - **Path:** He opens the sample project and reveals Graphical View.
  - **Climax:** The workbench exposes governed sheet views with at least power, control, and field
    wiring roles.
  - **Resolution:** The project feels like the next step after M25, not a separate drawing demo.

- **UJ-2. Maya follows a cross-view connection.**
  - **Context:** Maya inspects a route that leaves the control sheet view and continues through a
    field terminal sheet view.
  - **Path:** She selects a continuation marker or cross-reference marker.
  - **Climax:** The IDE reveals the related document occurrence through canonical subject and
    occurrence identity.
  - **Resolution:** She can answer where the connection continues without reading raw source.

- **UJ-3. Winston validates document projection authority.**
  - **Context:** Winston reviews the data flow and Theia rendering.
  - **Path:** He traces `.athena` semantics through projection, Document Projection IR, document
    occurrences, cross-reference facts, Presentation IR, and renderer output.
  - **Climax:** The renderer paints compact references only; it does not infer page, connection, or
    reference meaning.
  - **Resolution:** M26 improves document-projection usability without turning Athena into a CAD
    drawing file.

## 3. Glossary

- **Document Projection** - A governed projection family that turns Athena semantic meaning into
  document-oriented views. It is one projection family beside schematic, manufacturing, report,
  maintenance, and future AI-facing projections.
- **Document Projection Identity** - A stable projection identity for one document-view organization
  derived from the workspace semantic graph, document projection policy id, and policy version or
  hash. It is not a separately authored document id.
- **Document Projection Policy** - A governed policy that decides document-view organization, view
  roles, occurrence membership, reference topology, and continuation behavior. The first M26 policy
  is `athena-document-projection-v0`.
- **Document Projection IR** - The governed intermediate representation introduced by M26. It
  carries document projection identity, view roles, occurrence membership, logical references, and
  continuation facts between projection and Presentation IR.
- **Sheet View** - A rendered document occurrence container generated from semantic projection
  rules. It may have a page number or title for display, but it is not a source unit, not an
  authority, and not a CAD page model.
- **Document Occurrence** - A projected instance of a canonical subject, route, terminal, label, or
  view container inside a document projection.
- **Document Occurrence Identity Recipe** - The deterministic identity basis for M26 document
  occurrences: `documentProjectionId + sheetViewId + canonicalSubjectId + occurrenceRole +
  representation/terminal/route role`.
- **Document Occurrence Index** - A deterministic index of all document and sheet-view occurrences
  for a canonical subject across a document projection.
- **Continuation Marker** - A visible compact marker showing that a route continues in another sheet
  view or document location.
- **Continuation Fact** - A governed route-segmentation fact produced when a canonical connection
  spans sheet-view membership. It links source route occurrence, target route occurrence,
  continuation markers, and M24 anchors or corridors.
- **Cross-Reference Fact** - A governed fact linking one occurrence, terminal, route, or subject to
  another occurrence location using canonical identity, source occurrence identity, target occurrence
  identity, relation type, and display notation.
- **Document Location** - A document-level reference such as view occurrence plus zone, used for
  engineering navigation. It is not raw canvas pixel state.
- **Sheet Navigator** - The IDE surface that lists sheet views in the current document projection and
  lets the user switch views without creating frontend-owned document meaning.
- **Document Projection Snapshot** - The projection output carrying sheet views, view metadata,
  occurrence indexes, continuation facts, and cross-reference facts.
- **Document Projection Artifact Kind** - A future category of document projection content such as
  schematic sheet view, project index, terminal report, partial part list, complete part list, or
  maintenance view. M26 MVP only proves schematic sheet views.

## 4. Features

### 4.1 Openable M26 Document Projection Proof

**Description:** M26 starts with a real sample project that demonstrates governed document
projection inside the Theia IDE.

#### FR-1: Provide an openable M26 sample project

Athena provides `examples/m26/sample-project` with real `.athena` files that exercise document
projection, continuation markers, cross references, M24 route facts, and M25 presentation facts
together.

**Consequences:**

- The project opens through the normal Athena Theia workflow.
- Reviewers do not need to inspect generated `.mjs` files to understand the proof.
- The accepted proof includes at least three sheet-view roles:
  - power distribution
  - control and PLC logic
  - field wiring or terminal transition
- The accepted proof must include an anti-regression case proving that source organization is not
  document organization:
  - at least one `.athena` source file contributes subjects to more than one sheet view
  - at least one sheet view contains subjects that are not defined by its filename
- The sample includes professional symbols and terminal notation from the M25 path.
- The sample includes at least one connection or subject relationship that crosses sheet-view
  boundaries.
- The sample demonstrates compact references by default and detailed reference data through
  selection or inspection.

#### FR-2: Define document projection acceptance references

Athena documents acceptance expectations for a small semantic document projection.

**Consequences:**

- Acceptance checks name sheet view role, view occurrence identity, view display metadata, logical
  zones, continuation markers, and cross-reference markers.
- The proof states how M26 differs from the M25 single-sheet representation proof.
- The acceptance bar is "governed document projection foundation," not full EPLAN page management.

### 4.2 Governed Document Projection IR And Sheet View Model

**Description:** Athena introduces document-projection contracts above individual sheet rendering.

#### FR-3: Define Document Projection IR

Athena defines Document Projection IR for document projection identity, sheet views, view metadata,
view roles, logical zones, occurrence membership, reference topology, and deterministic view
identity.

**Consequences:**

- M26 introduces one small built-in document projection policy named
  `athena-document-projection-v0`, owned by the compiler/runtime projection path.
- Document Projection Policy decides view organization, view roles, occurrence membership,
  continuation facts, cross-reference facts, and navigation topology.
- Presentation Policy/Profile decides visual notation, symbols, labels, terminals, markers,
  rendering primitives, and paint-ready coordinates.
- Document Projection IR is derived from `.athena` semantic state and projection policy.
- It is not separately authored, persisted, or mutated as an independent source of truth.
- M26 introduces a workspace-level document projection entry point from the project semantic graph
  snapshot or linked/lowered project units, not only active-file compilation.
- Sheet views are projection facts, not renderer state.
- Sheet views do not own rendered geometry, coordinates, or page packing authority.
- Document Projection IR may contain logical zones and document locations, but never raw
  `x`, `y`, `width`, or `height`.
- Document Projection IR owns projection identity, view identity, view role and order, logical
  location, occurrence membership, occurrence index, continuation facts, cross-reference facts, and
  navigation topology.
- Presentation IR owns visual placement, symbols, labels, terminals, markers, rendering primitives,
  and paint-ready coordinates.
- M26 refines the M19 Sheet IR boundary: document projection owns sheet-view identity and topology;
  Presentation IR or future publication presentation contracts own page frame, page size, title-block
  rendering, coordinates, and paint-ready sheet chrome.
- View identity remains stable for the same governed input.
- View metadata can be inspected and tested without opening the renderer.
- A `.athena` source file is not a sheet view.
- The model can represent future artifact kinds such as project index, terminal report, and part
  list, but M26 only accepts schematic sheet views.
- The model can later support PDF export, document packages, and revision workflows without making
  M26 a publishing milestone.

#### FR-4: Materialize deterministic sheet views from projection facts

Athena deterministically materializes the accepted sample into multiple sheet views.

**Consequences:**

- The same input produces the same document projection identity, view order, view titles, occurrence
  ids, zones, and cross-reference values.
- The initial document materialization policy may be small and rule-based.
- The initial proof may use deterministic projection rules based on engineering roles, system
  structure, or explicit projection policy.
- Layout facts may influence visual placement inside a sheet view, but they do not define document
  projection identity or occurrence membership.
- The M26 sample should use one coherent system projected into multiple sheet views.
- Source-file boundaries must not be treated as sheet-view boundaries.
- M26 must not depend on hand-authored canvas page state.
- New `.athena` syntax is not required for the MVP unless the story explicitly updates ANTLR4,
  Tree-sitter, compiler, LSP behavior, tests, and sample documentation together.
- The same semantic graph and same document projection policy version must produce stable projection
  identity, view ids, occurrence ids, document locations, and canonical cross-reference facts.
- Display labels such as `2-C4` may change only when view ordering or zoning policy changes.
- The accepted proof must include stability checks after source file rename or source file order
  changes where canonical semantic identities remain unchanged.

#### FR-5: Build a document occurrence index

Athena indexes all document occurrences for each canonical subject across the document projection.

**Consequences:**

- A subject can have one or more occurrences across document views.
- Occurrences carry canonical subject identity, occurrence identity, document projection identity,
  view occurrence identity, zone, representation identity, terminal identity, and source range where
  available.
- Occurrence ids follow the M26 identity recipe and remain stable when source files are renamed or
  reordered without changing semantic meaning.
- The occurrence index supports inspector, reveal, and cross-reference behavior.
- The renderer does not search the canvas to discover related occurrences.

### 4.3 Cross-Reference And Continuation Facts

**Description:** M26 makes cross-view relationships visible without turning labels into noisy graph
strings.

#### FR-6: Produce governed continuation facts for cross-view routes

Athena produces continuation facts when a canonical connection crosses sheet-view membership.

**Consequences:**

- Continuation facts link route identity, source terminal, target terminal, source document location,
  and target document location.
- A cross-view continuation produces source and target route occurrences plus continuation markers
  attached to M24 anchors or corridors.
- The visible marker uses compact engineering notation, such as target view plus zone.
- Verbose subject paths remain available through hover, selection, inspector, or equivalent
  product-safe disclosure.
- Continuation markers attach to M24 route facts and M25 presentation terminal anchors.
- Continuation meaning is derived upstream, not inferred from broken line segments in the renderer.

#### FR-7: Produce governed cross-reference facts for repeated subjects and related occurrences

Athena produces cross-reference facts for repeated component, terminal, and route-related
occurrences where the sample requires them.

**Consequences:**

- Cross-reference facts carry source identity, target identity, source occurrence, target
  occurrence, relation type, and display notation.
- Cross-reference facts can point from a component occurrence to related occurrences in other views.
- Cross-reference facts can point from terminal or route markers to their continuation targets.
- Cross-reference values are stable and compact.
- The accepted proof must avoid canvas crowding from raw fully qualified semantic ids.
- Detailed semantic ids remain inspectable when the user selects or hovers the referenced item.

#### FR-8: Report document-reference diagnostics with provenance

Athena reports diagnostics or proof metadata when required document references cannot be resolved.

**Consequences:**

- Missing target occurrence, ambiguous target occurrence, duplicate view identity, and invalid
  document location cases are detectable.
- Diagnostics carry source provenance when caused by authored `.athena` input.
- Diagnostics carry projection-policy or view provenance when caused by derived document projection
  behavior.
- Problems publishes only source-backed diagnostics; inspector or proof metadata may show
  projection-only diagnostics.
- Theia does not emit semantic diagnostics by resolving cross references locally.

### 4.4 IDE Document Projection Navigation And Coherence

**Description:** M26 makes document projection usable in the accepted Theia workflow.

#### FR-9: Add governed sheet-view navigation in the Graphical View

Theia exposes sheet views from the document projection snapshot and lets users switch between them.

**Consequences:**

- The sheet navigator lists view title, view role, and compact display order.
- Switching sheet views updates the canvas using the selected view facts.
- The Graphical View follows the currently active `.athena` source and does not show stale sample
  state.
- The navigator does not create or persist document meaning.
- Sheet navigation and cross-reference clicks consume the compiler/runtime document occurrence index
  directly. Canvas scans must not resolve document meaning.
- The visible sheet-view navigation UI stays lightweight and does not crowd the engineering canvas.

#### FR-10: Preserve source, outline, inspector, problems, graph, and cross-reference coherence

Document projection facts must round-trip through the same canonical subject and occurrence
identities used by M24 routes and M25 representation facts.

**Consequences:**

- Selecting a symbol, terminal, route, continuation marker, or cross-reference marker reveals the
  correct source subject where supported.
- Clicking a cross-reference navigates to or selects the related document occurrence without opening
  duplicate editor tabs for the same `.athena` file.
- Outline and Problems behavior must not regress for the M26 sample.
- The inspector shows document location and related occurrences without requiring verbose labels on
  the canvas.

### 4.5 Evidence And Handoff

**Description:** M26 publishes product-facing usage docs and executable proof.

#### FR-11: Publish M26 usage and evidence

Athena publishes usage documentation, acceptance proof, and regression coverage for M26.

**Consequences:**

- `docs/usages/m26-proof-usage.md` explains how to open and test the M26 sample.
- A comparison proof explains the M25 single-sheet boundary and the M26 document projection
  improvement.
- Product smoke or equivalent IDE-path verification proves the sample opens, sheet views are
  selectable, and cross-reference facts render through Theia.
- The implementation retrospective must state what M26 proves, what remains deferred, and how to
  avoid repeating M20-M25 product-proof mistakes.

## 5. Non-Goals

- Full EPLAN document management parity
- Final PDF export, print dialog, plot settings, revision table workflow, or drawing release package
- Standards-complete IEC, ANSI, or company-specific cross-reference formatting
- Automatic large-project pagination or optimal page packing
- Full terminal strip documentation or wire list generation
- Cross-project, package-repository, or supplier-library documentation references
- New broad `.athena` document syntax without simultaneous ANTLR4, Tree-sitter, parser, compiler,
  LSP, fixtures, tests, and sample documentation updates
- Free-form page editor, canvas-local page breaks, or renderer-owned document state
- Route editing, physical cabinet routing, harness routing, cable tray routing, or 3D routing
- Broad symbol library expansion beyond what the sample needs
- AI-generated page composition or AI document authoring
- Desktop-viewer, Compose, or deprecated KMP frontend work

## 6. MVP Scope

M26 MVP includes:

- One openable Theia sample project at `examples/m26/sample-project`
- Governed Document Projection IR
- A compiler/runtime-owned `athena-document-projection-v0` policy
- A workspace-level document projection entry point over the project semantic graph snapshot or
  linked/lowered project units
- Deterministic document projection identity, view order, view roles, logical zones, and display
  metadata
- At least three accepted sheet-view roles:
  - power distribution
  - control and PLC logic
  - field wiring or terminal transition
- A document occurrence index across sheet views
- Explicit `DocumentOccurrence`, `DocumentLocation`, `ContinuationFact`, and `CrossReferenceFact`
  contracts
- Continuation facts for at least one cross-view route
- Cross-reference facts for at least one repeated or related subject occurrence
- Compact visible cross-reference markers with detailed data available through selection or
  inspection
- Integration with M24 route facts and M25 presentation terminal anchors
- Theia Graphical View sheet-view navigation
- Source, outline, Problems, inspector, graph, and cross-reference coherence checks
- Product-path smoke or equivalent IDE verification
- Usage docs and implementation retrospective

M26 MVP does not include:

- PDF export or print-quality artifact generation
- Full document revision management
- Complete IEC/EPLAN cross-reference style parity
- General auto-pagination
- New language syntax unless explicitly implemented across the full parser and IDE syntax stack.
  The M26 sample must use only admitted `.athena` syntax unless a story upgrades ANTLR4,
  Tree-sitter, parser, compiler, LSP, fixtures, tests, and docs together.

## 7. Success Metrics

- **SM-1:** The M26 sample opens in the Athena Theia IDE using normal project workflow.
- **SM-2:** The Graphical View exposes a governed document projection with at least three selectable
  sheet views.
- **SM-3:** The same input produces stable document projection identity, view order, occurrence ids,
  zones, and cross-reference values.
- **SM-4:** At least one cross-view route uses governed continuation facts attached to M24 route and
  M25 terminal facts.
- **SM-5:** Cross-reference markers are compact by default and do not crowd the canvas with raw
  fully qualified semantic ids.
- **SM-6:** Selecting a continuation or cross-reference marker reveals or selects the related
  occurrence through canonical identity.
- **SM-7:** The renderer does not own view identity, reference resolution, or hidden document state.
- **SM-8:** No desktop-viewer, Compose, or deprecated KMP frontend module is touched for M26 scope.
- **SM-9:** The M26 usage doc gives a reviewer a concrete IDE path to see views, references, and
  navigation.
- **SM-10:** The same workspace semantic graph can produce a document projection without changing
  canonical subject identities or treating source-file boundaries as sheet-view boundaries.
- **SM-11:** The accepted proof includes an anti-regression case proving that source-file
  organization does not define sheet-view organization.

## 8. Assumptions Index

- **A-1:** M24 route facts and M25 presentation facts are available and stable enough to serve as the
  visual baseline for M26.
- **A-2:** M26 focuses on document projection coherence, not publishing or library breadth.
- **A-3:** The first document materialization policy can be small, deterministic, and sample-oriented
  without becoming a general auto-pagination engine.
- **A-4:** Theia IDE remains the only frontend proof surface for M26.
- **A-5:** Compact cross-reference labels plus inspector detail are preferable to visible verbose
  semantic route labels.
- **A-6:** New `.athena` syntax is avoided unless the full ANTLR4 and Tree-sitter language paths are
  upgraded together.
- **A-7:** Athena workspace/project remains the semantic source of truth; Document Projection IR is a
  projection artifact.
- **A-8:** Multiple document projection organizations from the same semantic graph are a future
  capability; M26 proves the identity and policy boundary needed for that future capability, not a
  second complete organization.

## 9. Resolved Decisions And Open Questions

### 9.1 Resolved Decisions

1. The M26 sample should use one coherent system projected into multiple sheet views. Source-file
   boundaries are not sheet-view boundaries, and the sample must include the anti-regression case
   described in FR-1.
2. Compact cross-reference notation should start with view-location notation, such as `2-C4`, while
   keeping canonical target identity in the inspector.
3. The first document occurrence index should include components, routes, and terminal labels. It
   should not index title-block fields.
4. Sheet switching should stay inside the existing Graphical View toolbar for M26 MVP. A dedicated
   document explorer remains deferred.
5. The first document projection policy is the built-in compiler/runtime policy
   `athena-document-projection-v0`.
6. Accepted proof sheet-view titles are:
   - Power Distribution
   - Control And PLC Logic
   - Field Wiring And Terminal Transition
7. The next reserved document projection artifact kind is terminal report.

### 9.2 Open Questions

1. Which exact internal package/module should host the first document projection contracts?
2. Should `athena-document-projection-v0` policy identity be serialized as a plain policy id plus
   version, or as a deterministic hash of policy inputs?
