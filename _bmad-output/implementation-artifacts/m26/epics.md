---
stepsCompleted:
  - extract-m26-requirements
  - design-m26-epics
  - create-m26-stories
  - final-validation
inputDocuments:
  - ../../planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-20-m26/addendum.md
  - ../../planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md
---

# Athena - M26 Epic Breakdown

## Overview

This document captures the extracted M26 requirements before epic/story design. M26 proves semantic
document projection: Athena projects one governed workspace semantic graph into multiple
document-oriented sheet views with stable occurrences, cross-view continuations, and cross-reference
facts without making pages, documents, source-file names, or Theia canvas state the source of truth.

## Requirements Inventory

### Functional Requirements

FR1: Provide an openable M26 sample project at `examples/m26/sample-project` that uses admitted
`.athena` syntax, proves document projection through normal Theia workflow, includes at least three
sheet-view roles, and includes an anti-regression case proving source-file organization is not
sheet-view organization.

FR2: Define document projection acceptance references covering sheet-view role, view occurrence
identity, view display metadata, logical zones, continuation markers, and cross-reference markers,
with M25 single-sheet comparison evidence.

FR3: Define Document Projection IR, `athena-document-projection-v0`, document projection identity,
view identity, view metadata, view roles, logical zones, occurrence membership, reference topology,
and the M19 Sheet IR boundary split.

FR4: Materialize deterministic sheet views from projection facts using a compiler/runtime-owned
document materialization policy, stable policy identity/version or hash, workspace-level projection
entry point, and no source-file or canvas-page authority.

FR5: Build a document occurrence index for canonical subjects across document views with
deterministic occurrence identity, source range where available, representation/terminal/route role,
and stability under source-file rename or reorder.

FR6: Produce governed continuation facts when a canonical connection crosses sheet-view membership,
including source and target route occurrences, continuation markers, and attachment to M24 anchors or
corridors.

FR7: Produce governed cross-reference facts for repeated subjects and related occurrences, carrying
source identity, target identity, source occurrence, target occurrence, relation type, document
locations, display notation, and provenance.

FR8: Report document-reference diagnostics with source provenance for authored `.athena` causes and
projection-policy/view provenance for derived document projection causes.

FR9: Add governed sheet-view navigation in Graphical View using compiler/runtime document occurrence
index data, not canvas scans or graph-node heuristics.

FR10: Preserve source, outline, inspector, Problems, graph, and cross-reference coherence through
canonical subject and occurrence identities without opening duplicate editor tabs.

FR11: Publish M26 usage, M25-vs-M26 acceptance proof, product-path smoke evidence, and implementation
retrospective.

### NonFunctional Requirements

NFR1: `.athena` source plus compiler/runtime semantic snapshots remain the single engineering source
of truth; Document Projection IR is derived projection output only.

NFR2: Document Projection Policy owns view organization, roles, occurrence membership,
continuations, cross references, and navigation topology.

NFR3: Presentation Policy/Profile and Presentation IR own visual notation, symbols, labels,
terminals, markers, rendering primitives, and paint-ready coordinates.

NFR4: Document Projection IR may contain logical zones and document locations, but never raw `x`,
`y`, `width`, or `height`.

NFR5: M26 document projection must consume a workspace/project semantic graph snapshot or
linked/lowered project units, not only active-file compilation.

NFR6: A `.athena` source file is never a sheet-view boundary.

NFR7: Document occurrence identity is deterministic and policy-versioned using
`documentProjectionId + sheetViewId + canonicalSubjectId + occurrenceRole +
representation/terminal/route role`.

NFR8: Cross-view continuations and cross references are semantic projection facts, not labels
inferred from broken rendered lines.

NFR9: Source-backed document-reference diagnostics may publish to Problems; projection-only
diagnostics stay in inspector/proof metadata.

NFR10: Theia navigation and cross-reference clicks consume the document occurrence index directly;
canvas scans must not resolve document meaning.

NFR11: M26 introduces no new `.athena` syntax unless ANTLR4, Tree-sitter, parser, compiler, LSP,
fixtures, tests, and docs are updated in the same story.

NFR12: Theia IDE is the only frontend scope; desktop-viewer, Compose, and deprecated KMP frontend
modules are out of scope.

NFR13: M26 does not include PDF/print export, revision workflow, standards-complete cross-reference
formatting, auto-pagination, document release packages, terminal reports, wire lists, part lists, or
AI document authoring.

### Additional Requirements

- Architecture AD-1: Source truth remains upstream.
- Architecture AD-2: `athena-document-projection-v0` owns view organization.
- Architecture AD-3: Document Projection IR owns topology, not geometry.
- Architecture AD-4: Presentation IR owns paint-ready sheet presentation.
- Architecture AD-5: Workspace-level projection entry point is required.
- Architecture AD-6: Source files are not sheet views.
- Architecture AD-7: Occurrence identity is deterministic and policy-versioned.
- Architecture AD-8: Continuations are route-segmentation facts.
- Architecture AD-9: Cross references are typed semantic facts.
- Architecture AD-10: Diagnostics preserve source and projection provenance.
- Architecture AD-11: Theia navigates through the occurrence index.
- Architecture AD-12: No new source syntax by default.
- Create or extend kernel contracts for `DocumentOccurrence`, `DocumentLocation`,
  `ContinuationFact`, and `CrossReferenceFact`.
- Integrate M26 document facts with M24 routing facts and M25 presentation/terminal facts.
- Product smoke must prove real Theia path, not only compiler/unit tests.
- Verification commands must run sequentially on Windows.

### UX Design Requirements

UX-DR1: Graphical View exposes a lightweight sheet-view selector in the existing toolbar rather than
creating a new document explorer for M26.

UX-DR2: Cross-reference and continuation markers remain compact on canvas, with detailed canonical
identity available through selection, hover, or inspector.

UX-DR3: Selecting or clicking a continuation/cross-reference marker reveals or selects the related
document occurrence without opening duplicate editor tabs for the same `.athena` file.

UX-DR4: The sheet-view navigation UI must not crowd the engineering canvas or regress the accepted
M20-M25 grid-backed canvas, transparent controls, and info/inspector behavior.

UX-DR5: The accepted sample should use the display titles `Power Distribution`, `Control And PLC
Logic`, and `Field Wiring And Terminal Transition`.

### FR Coverage Map

FR1: Epic 4 - openable sample project.

FR2: Epic 4 - acceptance references.

FR3: Epic 1 - Document Projection IR.

FR4: Epic 1 - deterministic sheet-view materialization.

FR5: Epic 1 - document occurrence index.

FR6: Epic 2 - continuation facts.

FR7: Epic 2 - cross-reference facts.

FR8: Epic 2 - diagnostics with provenance.

FR9: Epic 3 - sheet-view navigation.

FR10: Epic 3 - IDE/source/inspector coherence.

FR11: Epic 4 - usage, smoke, and retrospective evidence.

## Epic List

### Epic 1: Governed Semantic Document Projection

Users can project the workspace semantic graph into stable document-oriented sheet views without
creating a second source of truth.

**FRs covered:** FR3, FR4, FR5

### Epic 2: Cross-View Engineering References

Engineers can follow connections and related occurrences across sheet views through governed
continuation and cross-reference facts.

**FRs covered:** FR6, FR7, FR8

### Epic 3: Theia Sheet-View Navigation And Coherence

Engineers can switch sheet views, select references, reveal related occurrences, and keep source,
outline, Problems, inspector, and graph coherence.

**FRs covered:** FR9, FR10

### Epic 4: Openable M26 Product Proof And Evidence

Reviewers can open the M26 sample project, see the semantic document projection proof, and verify it
through usage docs and product-path smoke.

**FRs covered:** FR1, FR2, FR11

## Epic 1: Governed Semantic Document Projection

Users can project the workspace semantic graph into stable document-oriented sheet views without
creating a second source of truth.

**FRs covered:** FR3, FR4, FR5

### Story 1.1: Define Document Projection Model Contracts

As an Athena platform engineer,
I want explicit Document Projection IR contracts,
So that sheet views, document occurrences, locations, and occurrence indexing have a governed model
before renderer integration.

**Acceptance Criteria:**

**Given** the existing M24 routing and M25 presentation model contracts
**When** the M26 document projection model package is introduced
**Then** it defines contracts for document projection identity, sheet view identity, sheet view role,
logical zone, document occurrence, document location, and occurrence index entry
**And** the contracts contain canonical subject identity and projection provenance fields where
needed
**And** the contracts contain no raw `x`, `y`, `width`, or `height` geometry fields
**And** the contract names use document projection, sheet view, document occurrence, and document
location terminology instead of document-authority terminology
**And** model-level tests verify deterministic identity serialization for representative
component, terminal, route, and label occurrences.

### Story 1.2: Define The Built-In Document Projection Policy Contract

As an Athena platform engineer,
I want a compiler/runtime-owned `athena-document-projection-v0` policy contract,
So that document-view organization is governed upstream and can be versioned deterministically.

**Acceptance Criteria:**

**Given** the Document Projection IR contracts from Story 1.1
**When** the first document projection policy contract is added
**Then** it exposes policy id, policy version or deterministic policy hash, supported sheet-view
roles, supported artifact kinds, and occurrence identity recipe metadata
**And** it assigns the initial schematic sheet-view roles for `Power Distribution`,
`Control And PLC Logic`, and `Field Wiring And Terminal Transition`
**And** it documents that M26 supports schematic sheet views only while reserving terminal report as
a future artifact kind
**And** it does not introduce authored `.athena` syntax
**And** tests verify the same policy input produces the same policy identity and view-role ordering.

### Story 1.3: Add Workspace-Level Document Projection Entry Point

As an Athena runtime integrator,
I want document projection to start from the workspace semantic graph or linked project snapshot,
So that M26 does not pretend an active file is the whole engineering project.

**Acceptance Criteria:**

**Given** a compiled Athena project with multiple source files or lowered project units
**When** the M26 document projection entry point is invoked
**Then** it consumes the project/workspace semantic snapshot rather than only the active editor file
**And** it returns a document projection snapshot containing projection identity, policy identity,
sheet views, occurrence index, reference-fact containers, and diagnostics/proof metadata
**And** the reference-fact containers can be empty until the Epic 2 derivation stories populate
continuation and cross-reference facts
**And** source-file names are not accepted as sheet-view ids or sheet-view roles
**And** the entry point can still project a single-file project for regression compatibility
**And** tests cover both single-file and multi-file sample snapshots.

### Story 1.4: Materialize Deterministic Sheet Views And Occurrence Membership

As an Athena reviewer,
I want deterministic sheet views and occurrence membership from semantic projection facts,
So that the same engineering model always produces the same document projection without canvas
state.

**Acceptance Criteria:**

**Given** semantic subjects with component roles, route roles, terminal roles, and presentation facts
from M24 and M25
**When** `athena-document-projection-v0` materializes sheet views
**Then** it produces stable sheet-view ids, display order, display titles, view roles, logical zones,
and occurrence membership for the accepted M26 roles
**And** layout facts may influence placement inside a selected sheet view but do not define document
projection identity or occurrence membership
**And** no hand-authored canvas page state is required
**And** no `.athena` source file boundary is treated as a sheet-view boundary
**And** tests verify stable output for repeated materialization of the same semantic graph.

### Story 1.5: Build Document Occurrence Index Stability Coverage

As an Athena IDE integrator,
I want a deterministic document occurrence index for canonical subjects,
So that inspectors, reveal actions, and cross-reference navigation can use governed identity.

**Acceptance Criteria:**

**Given** a document projection snapshot with components, terminals, labels, and routes
**When** the occurrence index is built
**Then** each indexed occurrence includes document projection identity, sheet-view identity,
canonical subject identity, occurrence role, representation/terminal/route role where applicable,
logical zone, display location, and source range where available
**And** occurrence ids follow the M26 identity recipe using policy-versioned projection identity
**And** the index supports one canonical subject appearing in multiple sheet views
**And** renaming or reordering source files without changing canonical semantic identities does not
change occurrence ids
**And** tests cover component, terminal, route, label, repeated-subject, and source-rename stability
cases.

## Epic 2: Cross-View Engineering References

Engineers can follow connections and related occurrences across sheet views through governed
continuation and cross-reference facts.

**FRs covered:** FR6, FR7, FR8

### Story 2.1: Derive Continuation Facts From Cross-View Route Membership

As an electrical engineer,
I want a route that crosses sheet-view membership to produce governed continuation facts,
So that cross-view conductors remain semantic route facts instead of broken rendered lines.

**Acceptance Criteria:**

**Given** M26 core document projection contracts, M24 route facts with canonical connection identity,
and M25 terminal anchors
**When** a canonical connection has route occurrences in more than one M26 sheet view
**Then** the document projection model defines and the document projection engine emits
`ContinuationFact` instances linking route identity, source terminal, target terminal, source
document location, and target document location
**And** each continuation fact includes source and target route occurrence identities
**And** continuation markers attach to M24 route anchors or corridors and M25 terminal anchors where
available
**And** the visible notation is compact by default, such as target view plus zone
**And** tests verify continuation meaning is not inferred from rendered line breaks or canvas
geometry.

### Story 2.2: Produce Typed Cross-Reference Facts For Related Occurrences

As an engineer reviewing a document projection,
I want repeated components, terminals, and route-related occurrences to carry typed cross-reference
facts,
So that I can follow related engineering meaning across views without reading verbose graph labels.

**Acceptance Criteria:**

**Given** a document occurrence index with repeated or related canonical subjects and continuation
facts where routes cross sheet views
**When** cross-reference facts are produced
**Then** the document projection model defines and the document projection engine emits
`CrossReferenceFact` instances carrying source identity, target identity, source occurrence, target
occurrence, relation type, source document location, target document location, display notation, and
provenance
**And** supported relation types include repeated subject, terminal continuation, and route
continuation for M26
**And** compact display notation avoids fully qualified semantic ids on the canvas
**And** detailed canonical identities remain available to inspector, hover, or selection consumers
**And** tests cover same-subject references and terminal/route continuation references.

### Story 2.3: Add Document Reference Diagnostic Provenance

As an Athena maintainer,
I want unresolved or ambiguous document references to report source or projection provenance,
So that Problems only shows authored source issues and derived projection issues stay explainable.

**Acceptance Criteria:**

**Given** document continuation and cross-reference derivation
**When** a missing target occurrence, ambiguous target occurrence, duplicate view identity, or invalid
document location is detected
**Then** the diagnostic includes severity, diagnostic code, relation type, affected canonical
identity, and provenance
**And** diagnostics caused by authored `.athena` input carry source range and can publish to
Problems
**And** diagnostics caused only by projection policy or derived view behavior carry projection/view
provenance and remain in inspector or proof metadata
**And** Theia does not create semantic diagnostics by resolving cross references locally
**And** tests cover source-backed and projection-only diagnostic routing.

### Story 2.4: Integrate Compact Reference Markers Into Presentation Facts

As an engineer reading the sheet view,
I want continuation and cross-reference markers to render compactly while preserving inspectable
meaning,
So that the canvas remains readable and does not repeat long semantic ids on every line.

**Acceptance Criteria:**

**Given** continuation facts and cross-reference facts from the document projection snapshot
**When** Presentation IR is created for a selected sheet view
**Then** compact marker facts are added for visible continuation and cross-reference notation
**And** marker facts include canonical reference payload for hover, selection, and inspector use
**And** raw labels such as `ControllerPLC3.hmi -> OperatorHMI3.status` are not used as default
visible route titles in the M26 path
**And** marker rendering stays inside Presentation IR and paint-only Theia rendering boundaries
**And** visual/projection tests verify marker compactness and metadata availability.

## Epic 3: Theia Sheet-View Navigation And Coherence

Engineers can switch sheet views, select references, reveal related occurrences, and keep source,
outline, Problems, inspector, and graph coherence.

**FRs covered:** FR9, FR10

### Story 3.1: Transport Document Projection Snapshot To Theia

As an Athena IDE integrator,
I want Theia to receive document projection snapshots and occurrence indexes from the compiler
runtime,
So that sheet-view navigation uses governed projection data rather than canvas scans.

**Acceptance Criteria:**

**Given** a document projection snapshot from the runtime projection entry point
**When** the LSP or existing IDE transport publishes graph/presentation data to Theia
**Then** it includes sheet-view metadata, selected view id, occurrence index entries, continuation
facts, cross-reference facts, compact display notation, and diagnostics/proof metadata required by
M26
**And** the payload preserves canonical subject identity and occurrence identity
**And** the transport remains compatible with existing M24/M25 single-sheet consumers
**And** no desktop-viewer, Compose, or deprecated KMP frontend module is modified
**And** tests cover payload shape and backward compatibility.

### Story 3.2: Add Lightweight Sheet-View Selector In Graphical View

As an Athena user,
I want a lightweight sheet-view selector in the existing Graphical View toolbar,
So that I can switch between document views without a new document explorer or crowded canvas.

**Acceptance Criteria:**

**Given** Theia receives sheet-view metadata for an M26 project
**When** the Graphical View opens
**Then** the toolbar lists available sheet views by compact display order, view title, and view role
**And** selecting `Power Distribution`, `Control And PLC Logic`, or `Field Wiring And Terminal
Transition` updates the canvas to the selected view facts
**And** the selector does not create or persist document meaning
**And** the UI does not obscure the engineering canvas or regress the accepted M20-M25 canvas
controls
**And** frontend or integration tests verify the selector switches between supplied sheet-view
facts without changing document projection data.

### Story 3.3: Navigate Continuation And Cross-Reference Clicks Through Occurrence Index

As an engineer following a reference,
I want clicking a continuation or cross-reference marker to reveal the related occurrence,
So that I can navigate document views through semantic identity rather than screen geometry.

**Acceptance Criteria:**

**Given** a visible continuation or cross-reference marker with a target occurrence identity
**When** the user clicks or selects the marker
**Then** Theia resolves the target through the document occurrence index
**And** Theia switches to the target sheet view when needed and selects or reveals the target
occurrence
**And** canonical subject details remain available in hover, selection, or inspector without
crowding the default canvas
**And** navigation does not scan rendered line segments, DOM nodes, or graph-node labels to infer
document meaning
**And** tests cover same-view references, cross-view references, and missing-target behavior.

### Story 3.4: Preserve Source, Outline, Problems, Inspector, And Editor Coherence

As an Athena user,
I want document projection navigation to stay coherent with source and IDE side panels,
So that selecting projected occurrences does not create stale views or duplicate editor tabs.

**Acceptance Criteria:**

**Given** an M26 sample project opened in Theia
**When** the user selects a symbol, terminal, route, continuation marker, or cross-reference marker
**Then** source reveal uses the canonical subject and source range where available
**And** outline and Problems behavior remains consistent with the active project and selected
subject
**And** inspector shows document location, related occurrences, reference relation type, compact
notation, and canonical identity
**And** navigating to another occurrence in the same `.athena` file reuses the existing editor tab
instead of opening a duplicate
**And** regression tests cover stale-state prevention for switching active files and sheet views.

## Epic 4: Openable M26 Product Proof And Evidence

Reviewers can open the M26 sample project, see the semantic document projection proof, and verify it
through usage docs and product-path smoke.

**FRs covered:** FR1, FR2, FR11

### Story 4.1: Create Openable M26 Sample Project With Source/View Anti-Regression Case

As a reviewer,
I want an M26 sample project that opens through normal Athena workflow,
So that I can see semantic document projection without inspecting generated artifacts first.

**Acceptance Criteria:**

**Given** the existing examples structure and admitted `.athena` syntax
**When** `examples/m26/sample-project` is added
**Then** it contains a coherent industrial-control system using only supported source syntax
**And** it includes subjects projected into `Power Distribution`, `Control And PLC Logic`, and
`Field Wiring And Terminal Transition` sheet views
**And** at least one `.athena` source file contributes subjects to more than one sheet view
**And** at least one sheet view contains subjects not defined by its filename
**And** the sample uses M24 route facts and M25 professional symbols, terminals, and labels
**And** the sample includes at least one cross-view route and at least one repeated or related
subject reference.

### Story 4.2: Publish M26 Acceptance Proof And M25 Comparison Documentation

As a product reviewer,
I want documentation that explains the M26 proof path and how it differs from M25,
So that I can validate the milestone against the intended architecture.

**Acceptance Criteria:**

**Given** the M26 sample project and document projection behavior
**When** M26 usage documentation is published
**Then** `docs/usages/m26-proof-usage.md` explains how to open the sample in Theia, select sheet
views, inspect compact markers, and follow references
**And** the documentation states the accepted sheet-view titles, document projection policy id,
occurrence identity recipe, and anti-regression source/view proof
**And** it compares M25 single-sheet presentation with M26 semantic document projection
**And** it states that pages, documents, source-file names, and Theia canvas state are not source
truth
**And** it avoids documenting unsupported `.athena` syntax.

### Story 4.3: Add Product Smoke And Regression Coverage

As an Athena maintainer,
I want executable proof that M26 works through the real IDE path,
So that the milestone is not only model-level code.

**Acceptance Criteria:**

**Given** the M26 sample project and Theia product path
**When** product smoke or equivalent IDE verification runs
**Then** it verifies the sample opens, sheet views are available, view switching works, compact
reference markers render, and a cross-reference or continuation reveal path resolves through the
occurrence index
**And** regression coverage verifies document projection identity stability and source-file
rename/reorder stability
**And** regression coverage verifies no default raw fully qualified semantic route labels crowd the
canvas
**And** verification commands run sequentially on Windows
**And** the smoke evidence is referenced from the M26 usage or retrospective artifact.

### Story 4.4: Publish M26 Retrospective Hooks And Boundary Checks

As an Athena project maintainer,
I want M26 retrospective notes and boundary checks captured before closeout,
So that future milestones do not accidentally turn document projection into EPLAN-style page
authority.

**Acceptance Criteria:**

**Given** the M26 implementation and verification evidence
**When** the retrospective artifact is created
**Then** it records what M26 proves, what remains deferred, and how it preserves `.athena` source as
the single source of truth
**And** it records usage evidence, product smoke commands, key files changed, and known limitations
**And** it explicitly states that Document Projection IR owns topology and reference identity while
Presentation IR owns paint-ready sheet presentation
**And** it confirms no deprecated desktop-viewer, Compose, or KMP frontend scope was used
**And** it lists deferred work such as PDF/print export, terminal reports, wire lists, standards
packs, revision workflow, auto-pagination, and any future document syntax admission.
