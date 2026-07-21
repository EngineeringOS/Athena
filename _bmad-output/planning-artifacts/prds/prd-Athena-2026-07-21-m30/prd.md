---
title: Athena M30 - Native Engineering Representation System Foundation
status: draft
created: '2026-07-21'
updated: '2026-07-21'
---

# Athena M30 PRD

## Executive Summary

M30 addresses Athena's current customer-demo credibility gap: the graph can be semantically correct
and still look like a toy because Athena does not yet have a professional engineering
representation system. M27 improved sheet density and routing. M28 added governed semantic
mutation. M29 made interaction semantic. M30 must now introduce the missing layer between semantic
truth and paint: an Athena-owned representation definition system, binding compiler, and
schematic composition proof.

M30 is not a QElectroTech importer and not an EPLAN clone milestone. QElectroTech `.elmt` files are
useful evidence that professional engineering tools need a real visual definition language, but
they are presentation/vector assets with QET-specific runtime semantics. They must not become
Athena source syntax, kernel concepts, or runtime dependencies.

The M30 thesis:

```text
.athena remains semantic truth.
The semantic kernel owns representation-relevant engineering facts.
The representation layer owns visual symbol vocabulary.
The binding compiler maps semantic/projection facts to representation occurrences.
The renderer paints resolved Presentation IR only.
```

## Product Thesis

Athena should generate a customer-demo credible industrial control sheet from semantic source plus
Athena-owned representation libraries.

```text
.athena source
  -> Engineering Semantic Model
  -> Projection Facts
  -> Representation Policy IR
  -> Representation Binding Compiler
  -> Athena Representation Definition IR
  -> Representation Occurrence IR
  -> Schematic Composition Intent Compiler
  -> Spatial Intent / Route Facts
  -> Presentation IR
  -> Renderer
```

This fills the visual gap without corrupting the kernel:

- kernel knows a device has input/output ports, terminal numbers, signal/media, relationship
  capability, functional role, and occurrence context;
- representation library knows IEC-style primitives, anchors, label slots, hotspots, variants,
  style tokens, and symbol geometry;
- binding policy chooses which representation occurrence is appropriate for a semantic entity in a
  projection context;
- representation policy chooses the visual dialect, symbol family, variant, and occurrence role for
  a projection context;
- renderer does not infer engineering meaning from SVG, DOM, CSS, or geometry.

## Problem

The current renderer looks toy-like for four structural reasons:

1. **Weak representation vocabulary.** Generic boxes and labels cannot express IEC-like symbol
   families, continuation arrows, contacts, coils, terminals, and compact dynamic labels.
2. **Weak binding.** Athena does not yet have a governed layer that maps semantic facts to
   representation occurrences such as coil view, contact view, terminal-strip view, or folio
   reference view.
3. **Weak schematic composition.** Professional sheets are composed through domain patterns:
   rails, columns, terminal strips, cross-reference markers, compact labels, and route lanes. A
   generic graph layout cannot produce this reliably.
4. **Renderer overcompensation.** Without a strong upstream representation system, frontend code
   invents wrappers, large hitboxes, generic rectangles, and visible UI borders that damage visual
   density.

## Goals

- Introduce Representation Policy IR v0 so projection context chooses representation families and
  variants without hard-coding those decisions into the renderer or semantic kernel.
- Introduce Athena Representation Definition IR v0 and split it from Representation Occurrence IR.
- Define a small native professional IEC-style symbol pack sufficient for one customer-demo
  industrial control sheet.
- Define representation-relevant domain contracts in the semantic/projection layer without putting
  visual geometry in the kernel.
- Build Representation Binding Compiler v0 that maps semantic/projection facts to representation
  occurrences through policy.
- Build Schematic Composition Intent Compiler v0 for dense 2D electrical/control schematic
  planning facts rather than CAD geometry truth.
- Improve renderer behavior so normal component chrome is transparent and interaction borders only
  appear on hover, selection, or drag.
- Produce `examples/m30/sample-project` as a credible demo sheet inspired by the rolling-shutter
  QET reference, without importing or depending on QET runtime assets.
- Record QET `.elmt` converter work as a deferred offline/plugin spike targeting Athena
  Representation Definition IR, not `.athena`.
- Support semantic reference representation occurrences needed by professional electrical drawings,
  including coil/contact, device/terminal-strip, component/location, and folio continuation
  references in the demo scope.
- Define representation lifecycle/versioning fields for library assets so long-lived engineering
  projects can later manage deprecation and migration.
- Finish every story with polish/purge review and close M30 with a cleanup ledger.

## Non-Goals

- No QET `.elmt` runtime dependency.
- No `.athena` syntax extension for visual primitives, QET paths, symbol geometry, or presentation
  libraries.
- No conversion of QET `.elmt` into `.athena` semantic source.
- No full IEC, EPLAN, QET, AutoCAD Electrical, or company standards parity.
- No generalized CAD drawing kernel.
- No freehand symbol drawing editor.
- No PDF/print/revision package workflow.
- No marketplace or supplier catalog ingestion.
- No AI-generated symbol library.
- No full cabinet, harness, BIM, P&ID, hydraulic, pneumatic, robotics, or 3D representation pack.

## Functional Requirements

### Epic 1 - Representation Boundary, Policy, And Kernel Contract

**FR-1:** M30 shall introduce first-class Athena Representation Policy IR, Representation
Definition IR, and Representation Occurrence IR owned outside the semantic kernel and outside
Theia.

**FR-2:** The semantic kernel shall expose representation-relevant engineering facts only:
canonical identity, type, role, ports, port direction, signal/medium, terminal numbering,
relationship capability, occurrence context, and source provenance.

**FR-3:** The semantic kernel shall not own line, polygon, arc, hotspot, SVG path, QET link type,
IEC drawing geometry, or visual style tokens.

**FR-4:** Representation Definition IR shall model reusable symbol identity, library identity,
version, lifecycle state, deprecation metadata, migration hint, primitives, ports/terminal anchors,
label slots, hotspot, bounds, variants, style tokens, dynamic binding slots, and provenance.

**FR-5:** Representation Occurrence IR shall model a per-projection use of a representation
definition with canonical semantic identity, projection occurrence identity, occurrence role,
variant, label bindings, terminal bindings, reference bindings, composition intent membership, and
diagnostics.

**FR-6:** Representation Policy IR shall select representation family, symbol id, variant,
occurrence role, and fallback behavior from semantic facts, projection context, standard/profile
metadata, and document/view context.

**FR-7:** Representation Policy IR, Representation Definition IR, and Representation Occurrence IR
shall be serializable and testable without Theia, SVG DOM, or browser runtime.

### Epic 2 - Native Professional Symbol Pack v0

**FR-8:** M30 shall create an Athena-owned native symbol pack for the customer-demo sheet.

**FR-9:** The symbol pack shall include at least: supply/reference marker, terminal, switch/contact,
coil/actuator, lamp/indicator, motor/load, protective device, and folio continuation/reference.

**FR-10:** Each symbol definition shall expose terminal anchors and label slots required for route
attachment and compact text rendering.

**FR-11:** Symbols shall use Athena style tokens and representation primitives, not copied QET XML
or hard-coded SVG snippets in Theia.

**FR-12:** Each native symbol shall have structured proof tests for anchors, label slots, bounds,
and deterministic primitive output.

**FR-13:** Native symbol definitions shall include lifecycle/versioning metadata sufficient to
record active, deprecated, superseded-by, and migration-hint states, even if M30 only uses active
symbols in the demo.

### Epic 3 - Representation Binding Compiler v0

**FR-14:** M30 shall map semantic/projection facts to representation occurrences through
Representation Policy IR and a binding compiler.

**FR-15:** The same semantic entity may produce multiple representation occurrences in different
projection contexts, such as coil occurrence, contact occurrence, terminal occurrence,
cross-reference occurrence, report occurrence, or maintenance occurrence.

**FR-16:** Binding shall use semantic facts and projection context, not source file boundaries,
screen coordinates, DOM structure, or QET element names.

**FR-17:** Binding diagnostics shall report missing symbol, missing anchor, incompatible terminal
role, missing label slot, unsupported occurrence role, and ambiguous representation policy.

**FR-18:** Binding output shall reference canonical semantic identity and projection occurrence
identity separately.

**FR-19:** Binding shall support semantic reference occurrences needed by the M30 demo, including
coil-to-contact, device-to-terminal-strip, component-to-location, and folio continuation/reference
relationships.

### Epic 4 - Schematic Composition Intent Compiler v0

**FR-20:** M30 shall introduce schematic composition intent patterns for dense 2D
electrical/control schematics.

**FR-21:** Composition intent shall support rails/columns, terminal-strip grouping,
control-circuit lanes,
folio references, compact label placement, and route-channel discipline for the sample.

**FR-22:** Composition intent shall consume M27 spatial intent and M30 representation
bounds/anchors.

**FR-23:** Composition intent shall produce planning facts such as lane membership, column
membership, alignment group, label band, route channel, and reference zone before final layout or
presentation geometry is resolved.

**FR-24:** Composition intent shall not become a CAD geometry database or source-of-truth layout
system.

**FR-25:** Composition proof shall reject large wrapper containers and default visible component
borders unless they are part of the actual engineering symbol.

### Epic 5 - Renderer Integration And Workbench Visual Credibility

**FR-26:** Renderer shall consume resolved Presentation IR from representation and composition
outputs, not raw representation library files.

**FR-27:** Normal component backgrounds and interaction hitboxes shall be transparent. Dotted or
highlight borders may appear only on hover, selection, focus, or drag.

**FR-28:** SVG viewBox and canvas framing shall be derived from actual resolved presentation bounds
plus governed sheet margins, not hard-coded large constants.

**FR-29:** Renderer shall not duplicate off-screen representation occurrences or repeated text
nodes in accepted M30 proof.

**FR-30:** Theia Graphical View shall default to the M30 customer-demo schematic projection and
preserve sheet/projection switch controls across mode changes.

### Epic 6 - Customer Demo Sample And Acceptance Evidence

**FR-31:** M30 shall include `examples/m30/sample-project`.

**FR-32:** The sample shall demonstrate one customer-demo credible industrial control sheet
inspired by the QET rolling-shutter reference, using Athena semantic source and Athena-owned
representation libraries only.

**FR-33:** Product smoke shall open the M30 sample in Theia and verify structured proof payloads
for representation binding, symbol anchors, composition bounds, route anchors, and visual chrome
rules.

**FR-34:** Acceptance shall include screenshot evidence as a visual regression guard and structured
assertions as the source of truth.

**FR-35:** M30 shall publish usage docs, QET gap rationale, retrospective, and cleanup ledger.

### Epic 7 - Deferred QET Converter Spike Record

**FR-36:** M30 shall document a deferred offline QET `.elmt` converter path targeting Athena
Representation Definition IR.

**FR-37:** The deferred converter design shall include XML parsing, primitive normalization, style
mapping, terminal orientation mapping, dynamic text binding mapping, unsupported-feature
diagnostics, licensing/provenance handling, and deterministic output.

**FR-38:** M30 shall explicitly forbid runtime cross-reference to QET `.elmt` assets and forbid
`.athena` source references to QET paths.

## Non-Functional Requirements

**NFR-1:** `.athena` remains the single source of semantic truth for M30.

**NFR-2:** Representation libraries are view-layer assets and must not be added to the semantic
kernel.

**NFR-3:** Representation IR must be platform-owned and frontend-independent.

**NFR-4:** The renderer remains paint-only; it does not infer symbol meaning, terminal meaning, or
relationship meaning from geometry.

**NFR-5:** The M30 sample must be customer-demo credible, not merely architecturally correct.

**NFR-6:** Visual acceptance uses professional references for qualitative direction, not
pixel-perfect cloning.

**NFR-7:** Gradle verification commands must run sequentially on Windows.

**NFR-8:** Every story ends with polish/purge review for dead code, stale docs, obsolete samples,
unused compatibility paths, and misleading design claims.

**NFR-9:** M30 may refactor aggressively where the current renderer/library architecture blocks
the representation boundary, but stale paths must be removed or ledgered.

## Success Metrics

**SM-1:** A reviewer can open the M30 sample and see a dense professional control-sheet proof,
not generic graph boxes.

**SM-2:** At least eight native representation symbols compile through Athena Representation
Definition IR.

**SM-3:** Every demo symbol has terminal anchors, label slots, bounds, and deterministic primitive
proof.

**SM-4:** Representation Policy IR plus Binding Compiler maps the sample's semantic devices, ports,
relationships, and reference facts to representation occurrences without Theia inference.

**SM-5:** The sample contains no hard-coded large SVG viewBox, off-screen duplicate symbol
occurrences, repeated label text nodes, or visible non-symbol wrapper borders in normal state.

**SM-6:** Component hitboxes are transparent in normal state and only show interaction chrome on
hover/selection/focus/drag.

**SM-7:** QET `.elmt` converter work is documented as deferred/offline and does not appear in
runtime dependency or `.athena` syntax.

**SM-8:** Final cleanup ledger records removed stale visual paths and any retained deferred items.

**SM-9:** The M30 sample includes at least one semantic reference occurrence proof, such as
coil/contact, device/terminal-strip, component/location, or folio continuation reference.

**SM-10:** Representation definitions include lifecycle/versioning metadata and tests prove the
active demo symbols resolve through the current policy.

## Acceptance Criteria

- Planning artifacts define the kernel/representation/binding/composition/renderer boundaries.
- Representation Definition IR v0 exists outside semantic kernel and Theia.
- Native symbol pack v0 exists and covers the M30 demo set.
- Binding compiler v0 produces representation occurrences from semantic/projection facts.
- Schematic composition v0 produces a compact customer-demo sheet.
- Renderer uses derived bounds and transparent normal interaction chrome.
- M30 sample opens in Theia and defaults to the demo schematic projection.
- Product smoke verifies structured representation, composition, and visual chrome proof payloads.
- QET `.elmt` import remains deferred/offline and targets Representation IR only.
- Cleanup ledger and retrospective are published.

## Open Questions

1. Should Representation Definition IR v0 be authored as JSON/YAML assets first, or Kotlin data
   fixtures first?
   - **Resolved recommendation:** JSON/YAML assets plus Kotlin tests, so the layer is clearly
     data-driven
     and not hidden in renderer code.
2. Should the first symbol primitives support arcs immediately?
   - **Resolved recommendation:** Yes, minimally. IEC-like symbols need more than rectangles and
     lines, but
     keep curves as presentation primitives, not kernel geometry.
3. Should QET converter spike be implemented in M30?
   - **Resolved recommendation:** No product runtime importer. Add only a documented deferred
     design unless
     a tiny offline parser spike is needed to validate Representation IR coverage.
4. Should M30 touch `.athena` syntax?
   - **Resolved recommendation:** No. If symbol/profile selection becomes necessary, model it as
     presentation policy/config first, not semantic source syntax.
5. What is the customer-demo target?
   - **Resolved recommendation:** A rolling-shutter/control-circuit style sheet with supply,
     controls,
     terminals, actuator/motor/load, references, and compact professional linework.
