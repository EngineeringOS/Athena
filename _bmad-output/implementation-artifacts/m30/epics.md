---
stepsCompleted:
  - m30-requirements-extraction
  - m30-architecture-boundary
  - m30-epic-story-generation
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
---

# Athena M30 - Epic Breakdown

## Overview

M30 fills Athena's last major credibility gap before the product can feel like a professional
engineering system: semantic truth and interaction are already governed, but the representation
system is still too weak to generate a customer-demo credible industrial schematic. M30 adds the
missing middle layer between semantic facts and paint: representation policy, representation
definition, representation occurrence, binding, schematic composition intent, and renderer
discipline.

M30 is not a QET importer and not an EPLAN clone. QET `.elmt` files are reference material only.
They help define the problem, but they must not become runtime dependency, `.athena` source syntax,
or kernel architecture.

## Requirements Inventory

### Functional Requirements

- FR-1..FR-7: representation policy, representation definition, and representation occurrence
  contracts.
- FR-8..FR-13: native symbol pack and lifecycle/versioning metadata.
- FR-14..FR-19: binding compiler, multi-occurrence support, and reference occurrences.
- FR-20..FR-25: schematic composition intent compiler and composition facts.
- FR-26..FR-30: renderer integration and workbench visual credibility.
- FR-31..FR-35: customer-demo sample and acceptance evidence.
- FR-36..FR-38: deferred QET converter record and runtime prohibitions.

### Non-Functional Requirements

- NFR-1..NFR-4: `.athena` remains source truth, representation stays outside kernel, renderer is
  paint-only.
- NFR-5..NFR-6: customer-demo credibility and qualitative visual reference discipline.
- NFR-7: sequential Gradle verification on Windows.
- NFR-8..NFR-9: mandatory polish/purge gate and room for aggressive refactor where needed.

## Epic List

### Epic 1: Representation Boundary, Policy, And Kernel Contract

Athena gains platform-owned representation policy, definition, and occurrence contracts outside
semantic kernel and Theia.

**FRs covered:** FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7.

### Epic 2: Native Professional Symbol Pack v0

Athena gains a small native IEC-style demo symbol pack with anchors, label slots, bounds,
lifecycle/versioning metadata, and deterministic primitive output.

**FRs covered:** FR-8, FR-9, FR-10, FR-11, FR-12, FR-13.

### Epic 3: Representation Binding Compiler v0

Athena maps semantic/projection facts to representation occurrences through representation policy,
including professional reference occurrences.

**FRs covered:** FR-14, FR-15, FR-16, FR-17, FR-18, FR-19.

### Epic 4: Schematic Composition Intent Compiler v0

Athena produces composition intent facts for a dense professional control schematic rather than
generic graph layout or CAD geometry truth.

**FRs covered:** FR-20, FR-21, FR-22, FR-23, FR-24, FR-25.

### Epic 5: Renderer Integration And Workbench Visual Credibility

Theia paints resolved presentation output with transparent normal chrome, derived bounds, and no
off-screen duplicates.

**FRs covered:** FR-26, FR-27, FR-28, FR-29, FR-30.

### Epic 6: Customer Demo Sample And Product Proof

M30 ships an openable sample and product-path proof for customer-demo credibility.

**FRs covered:** FR-31, FR-32, FR-33, FR-34, FR-35.

### Epic 7: QET Gap Record, Retrospective, And Cleanup

M30 records the QET converter boundary and closes with a purge ledger.

**FRs covered:** FR-36, FR-37, FR-38.

### Epic 8: Compact Authoring Syntax Refinement

Athena admits compact source-authoring syntax for repeated relationships without changing the
canonical engineering model or turning authoring groups into renderer/CAD objects.

**FRs covered:** M30 follow-up language ergonomics.

## Story Details

### Epic 1: Representation Boundary, Policy, And Kernel Contract

#### Story 1.1: Define Representation Policy, Definition, And Occurrence IR Contracts

As an Athena platform engineer, I want a representation model independent of kernel and Theia, so
professional symbols are governed upstream of rendering.

**Acceptance Criteria:**

- Representation contract types exist for policy id, symbol id, library id, primitive, anchor,
  terminal, label slot, style token, variant, symbol definition, representation occurrence,
  reference binding, lifecycle state, and diagnostics.
- Representation Definition IR and Representation Occurrence IR are separate shapes.
- Representation Policy IR selects symbol family, variant, occurrence role, fallback behavior,
  and priority from semantic facts and projection context.
- No representation type depends on Theia, SVG DOM, QET runtime classes, or semantic source parser
  internals.
- Contract tests serialize and deserialize the v0 model.
- Polish/Purge Gate complete.

#### Story 1.2: Add Representation-Relevant Semantic Contract Audit

As an architect, I want the semantic kernel boundary audited, so M30 uses engineering facts without
moving symbol geometry into kernel.

**Acceptance Criteria:**

- Audit lists the semantic facts available for binding: identity, type, role, ports, direction,
  signal/medium, terminal number, relationship capability, occurrence context, and provenance.
- Audit confirms visual primitives, hotspots, style tokens, QET link types, and SVG paths are not
  kernel concepts.
- Any missing binding-critical semantic fact is recorded as a domain-contract gap, not papered over
  in renderer code.
- Polish/Purge Gate complete.

#### Story 1.3: Add Representation Diagnostics And Serialization Tests

As a maintainer, I want stable representation diagnostics, so missing symbols and anchors fail
clearly.

**Acceptance Criteria:**

- Stable diagnostic codes from `REPRESENTATION-CONTRACT.md` are represented and tested.
- Invalid library assets produce structured diagnostics.
- Diagnostics are transport-safe for LSP/runtime proof payloads.
- Polish/Purge Gate complete.

### Epic 2: Native Professional Symbol Pack v0

#### Story 2.1: Create Native Symbol Library Asset Format And Loader

As a library maintainer, I want native Athena symbol assets, so symbols are data-driven and not
hard-coded in renderer code.

**Acceptance Criteria:**

- A v0 asset format is chosen and documented.
- Loader validates symbol id, version, bounds, primitives, anchors, label slots, and style tokens.
- Assets load in tests without browser runtime.
- QET XML is not used as runtime asset format.
- Polish/Purge Gate complete.

#### Story 2.2: Add Demo Symbol Set For Control-Sheet Proof

As a demo reviewer, I want enough native symbols to recognize a professional control sheet.

**Acceptance Criteria:**

- Symbol pack includes supply/reference marker, terminal, switch/contact, coil/actuator,
  lamp/indicator, motor/load, protective device, and folio continuation/reference.
- Symbols use compact professional linework and label slots.
- No symbol is copied as raw QET XML or hidden Theia SVG snippets.
- Polish/Purge Gate complete.

#### Story 2.3: Add Anchor, Label-Slot, Bounds, Lifecycle, And Primitive Tests

As a maintainer, I want symbol pack quality checked structurally.

**Acceptance Criteria:**

- Tests assert every route-attached symbol has named anchors.
- Tests assert required label slots and terminal slots exist.
- Primitive output is deterministic.
- Bounds and hotspot are valid.
- Symbol lifecycle/versioning metadata is valid.
- Polish/Purge Gate complete.

### Epic 3: Representation Binding Compiler v0

#### Story 3.1: Build Binding Rule Model And Occurrence Output

As a compiler engineer, I want binding rules to produce representation occurrences from semantic
facts.

**Acceptance Criteria:**

- Binding rule model maps subject kind, role, projection context, occurrence role, symbol id,
  variant, labels, terminals, priority, and diagnostics.
- Binding rules consume Representation Policy IR instead of hard-coding visual choices in renderer
  code.
- Binding output separates canonical semantic id from projection occurrence id.
- Binding compiler does not read DOM, SVG, source file names as sheet identity, or QET element
  names.
- Polish/Purge Gate complete.

#### Story 3.2: Bind Demo Devices, Ports, Relationships, And References

As a user, I want semantic entities to appear as correct visual occurrences.

**Acceptance Criteria:**

- Demo devices bind to native representation symbols.
- Ports bind to terminal anchors and label slots.
- Relationships and document/reference facts bind to route or continuation occurrences.
- Same semantic device can be represented by different occurrence roles where the sample needs it.
- At least one semantic reference occurrence is proven: coil/contact, device/terminal-strip,
  component/location, or folio continuation/reference.
- Polish/Purge Gate complete.

#### Story 3.3: Surface Missing Or Ambiguous Binding Diagnostics

As a reviewer, I want binding failures to be explicit instead of guessed by the renderer.

**Acceptance Criteria:**

- Missing symbol, missing anchor, missing label slot, unsupported role, and ambiguous binding are
  covered by tests.
- Runtime/LSP proof payload exposes binding status for the M30 sample.
- The renderer never invents fallback boxes for accepted M30 proof without a diagnostic.
- Polish/Purge Gate complete.

### Epic 4: Schematic Composition Intent Compiler v0

#### Story 4.1: Define Composition Intent Model

As a compiler engineer, I want schematic composition intent patterns, so the sheet is not generic
graph layout.

**Acceptance Criteria:**

- Composition intent model covers rails, columns, terminal groups, route lanes, reference zones,
  and compact label placement for the demo.
- Composition intent consumes representation bounds/anchors and M27 spatial facts.
- Composition intent emits lane membership, column membership, alignment group, label band, route
  channel, terminal group, and reference zone facts before final presentation geometry.
- Composition intent does not persist CAD geometry as semantic truth.
- Polish/Purge Gate complete.

#### Story 4.2: Compose The M30 Control-Sheet Proof

As a demo reviewer, I want the sample sheet to look dense and professional.

**Acceptance Criteria:**

- M30 sample composes into one credible industrial control-sheet projection from composition
  intent facts.
- Component wrappers are not visible unless part of the actual engineering symbol.
- Labels are compact and do not overlap symbol bodies or route channels in accepted proof.
- Polish/Purge Gate complete.

#### Story 4.3: Add Composition Bounds Proof And Wrapper Regression Guard

As a maintainer, I want automated guards against the M27/M29 visual mistakes.

**Acceptance Criteria:**

- Tests/proof assert derived bounds match actual resolved presentation content plus governed
  margins.
- Tests/proof reject hard-coded large viewBox, off-screen duplicate elements, repeated label text,
  and visible non-symbol wrapper borders in normal state.
- Polish/Purge Gate complete.

### Epic 5: Renderer Integration And Workbench Visual Credibility

#### Story 5.1: Render Representation Primitives From Presentation IR

As a frontend maintainer, I want Theia to paint resolved primitives only.

**Acceptance Criteria:**

- Theia renders M30 primitives from Presentation IR.
- Theia does not load raw representation assets or QET `.elmt`.
- Data attributes used for testing are downstream metadata only.
- Polish/Purge Gate complete.

#### Story 5.2: Enforce Transparent Normal Chrome

As a user, I want visual focus to stay on engineering symbols.

**Acceptance Criteria:**

- Normal hitboxes, component backgrounds, and interaction wrappers are transparent.
- Dotted/selection borders appear only for hover, selection, focus, or drag.
- Tests or smoke proof verify normal-state absence of visible wrapper borders.
- Polish/Purge Gate complete.

#### Story 5.3: Derive SVG ViewBox And Remove Duplicate Elements

As a reviewer, I want the canvas to fit the actual drawing.

**Acceptance Criteria:**

- SVG viewBox derives from resolved presentation bounds and governed margins.
- Accepted proof contains no off-screen duplicate symbol occurrence set.
- Accepted proof contains no duplicated always-visible text nodes for the same label slot.
- Polish/Purge Gate complete.

#### Story 5.4: Preserve Projection And Sheet Switch Controls

As a user, I want the demo view controls to stay usable.

**Acceptance Criteria:**

- M30 opens to the customer-demo schematic projection.
- Sheet/projection switch controls remain visible and usable after changing view modes.
- Control state uses projection/document facts, not source file count.
- Polish/Purge Gate complete.

### Epic 6: Customer Demo Sample And Product Proof

#### Story 6.1: Create M30 Sample Project

As a customer-demo owner, I want a sample project that proves Athena's visual direction.

**Acceptance Criteria:**

- `examples/m30/sample-project` exists and uses admitted `.athena` syntax.
- Sample source remains semantic and does not reference QET files or symbol geometry.
- Sample includes enough devices, ports, relationships, and references for the demo symbol set.
- Polish/Purge Gate complete.

#### Story 6.2: Add Structured Product Smoke

As a maintainer, I want product proof that does not rely on visual guessing.

**Acceptance Criteria:**

- Smoke opens the M30 sample in Theia.
- Smoke verifies representation library loaded, binding counts, missing-binding diagnostics absent
  in accepted proof, anchor usage, composition bounds, route anchors, and chrome rules.
- Gradle commands, if needed, run sequentially.
- Polish/Purge Gate complete.

#### Story 6.3: Add Screenshot Guard And Usage Documentation

As a reviewer, I want evidence I can inspect.

**Acceptance Criteria:**

- Screenshot guard captures the M30 demo sheet.
- Usage doc explains how to open and evaluate the sample.
- Doc states that visual references are qualitative and not QET/EPLAN parity claims.
- Polish/Purge Gate complete.

### Epic 7: QET Gap Record, Retrospective, And Cleanup

#### Story 7.1: Document Deferred Offline QET Converter Design

As an architect, I want QET converter scope recorded correctly.

**Acceptance Criteria:**

- Design documents QET `.elmt` -> QET AST -> Representation IR candidate.
- Design covers primitive normalization, style mapping, terminal orientation, dynamic text,
  diagnostics, licensing/provenance, and deterministic output.
- Design forbids QET runtime dependency and `.athena` QET references.
- Polish/Purge Gate complete.

#### Story 7.2: Publish M30 Retrospective And Cleanup Ledger

As a maintainer, I want M30 lessons recorded.

**Acceptance Criteria:**

- Retrospective records what blocked visual credibility before M30.
- Cleanup ledger records removed stale renderer/library/docs/design paths and retained deferred
  items.
- Lessons include "do not patch renderer around missing representation semantics."
- Polish/Purge Gate complete.

#### Story 7.3: Run Final Purge And Regression Checks

As a project owner, I want M30 to close cleanly.

**Acceptance Criteria:**

- Final audit checks stale docs, old screenshots, obsolete renderer fallbacks, dead sample paths,
  and misleading design claims.
- M27/M28/M29 core regression smoke either passes or is explicitly migrated with reason.
- Encoding audit passes after docs edits.
- Polish/Purge Gate complete.

### Epic 8: Compact Authoring Syntax Refinement

#### Story 8.1: Support Grouped Connect Authoring Syntax

As an Athena author,
I want repeated connection edges to be grouped under a readable `connect <name> { ... }` block,
so dense control-sheet source stays compact without changing Athena's flat semantic relationship
model.

**Acceptance Criteria:**

- ANTLR accepts both existing single-line `connect A.out -> B.in` declarations and new grouped
  `connect groupName { A.out -> B.in }` declarations.
- Tree-sitter accepts and highlights the grouped form for editor syntax UX without becoming a
  semantic authority.
- The authored AST preserves group name and child connection edge spans.
- Electrical/domain lowering flattens grouped child edges into the same canonical
  `EngineeringConnection` output as equivalent single-line connections.
- LSP outline/document symbols expose grouped connect blocks without hiding their child edges.
- Tests cover mixed flat/grouped declarations, empty groups, invalid endpoint arity inside a group,
  and canonical IR equivalence.
- Polish/Purge Gate complete.
