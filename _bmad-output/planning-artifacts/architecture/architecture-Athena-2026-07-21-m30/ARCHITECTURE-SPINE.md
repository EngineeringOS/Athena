---
name: Athena M30 Native Engineering Representation System
type: architecture-spine
purpose: build-substrate
altitude: milestone-to-epics
paradigm: semantic-to-representation-binding
scope: Athena M30 representation policy, representation definition, representation occurrence, binding compiler, schematic composition intent, renderer integration, and customer demo proof
status: draft
created: '2026-07-21'
updated: '2026-07-21'
binds:
  - M30 PRD FR-1..FR-33
  - M30 PRD NFR-1..NFR-9
sources:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
companions:
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
---

# Architecture Spine - Athena M30 Native Engineering Representation System

## Design Paradigm

M30 uses semantic-to-representation binding:

```text
.athena source
  -> Semantic Kernel
  -> Projection Facts
  -> Representation Policy IR
  -> Representation Binding Compiler
  -> Representation Definition IR
  -> Representation Occurrence IR
  -> Schematic Composition Intent Compiler
  -> Spatial Intent / Route Facts
  -> Presentation IR
  -> Renderer
```

Representation is downstream from semantic truth but upstream from paint.

## Invariants And Rules

### AD-1 - `.athena` Remains Semantic Truth

- **Binds:** FR-1, FR-2, FR-3, NFR-1
- **Prevents:** presentation assets becoming source truth.
- **Rule:** M30 must not add visual primitives, symbol geometry, QET paths, or presentation library
  references to `.athena` source syntax.

### AD-2 - Kernel Owns Representation-Relevant Semantics Only

- **Binds:** FR-2, FR-3, NFR-2
- **Prevents:** IEC/QET visual concepts entering the semantic kernel.
- **Rule:** Kernel may expose device role, port anatomy, signal/medium, direction, terminal number,
  relationship capability, and projection occurrence context. Kernel must not own primitives,
  hotspots, anchors as geometry, style tokens, SVG paths, or QET link types.

### AD-3 - Representation Policy Is Explicit

- **Binds:** FR-6, FR-14, FR-15, FR-16, FR-17, FR-18, FR-19
- **Prevents:** binding logic and renderer code hard-coding visual dialect choices.
- **Rule:** Representation Policy IR chooses representation family, symbol id, variant,
  occurrence role, fallback behavior, and priority from semantic facts, projection context,
  standard metadata, and document/view context. M30 must not encode these choices in `.athena`
  source syntax.

### AD-4 - Representation Definition And Occurrence Are Separate

- **Binds:** FR-1, FR-4, FR-5, FR-7
- **Prevents:** reusable symbol assets being confused with placed/projected uses.
- **Rule:** Representation Definition IR describes reusable symbol assets. Representation
  Occurrence IR describes one semantic/projection use with bound labels, terminals, references,
  occurrence role, and composition intent membership.

### AD-5 - Representation IR Is Platform-Owned And Frontend-Independent

- **Binds:** FR-1, FR-4, FR-5, FR-6, FR-7, NFR-3
- **Prevents:** Theia or SVG becoming the symbol definition language.
- **Rule:** Representation IR must be loadable/testable without browser runtime. Theia consumes
  Presentation IR, not raw representation assets.

### AD-6 - Binding Compiler Is The Only Semantic-To-Symbol Mapping Authority

- **Binds:** FR-14, FR-15, FR-16, FR-17, FR-18, FR-19
- **Prevents:** renderer, DOM, source file, or QET element name deciding what symbol a subject is.
- **Rule:** Semantic/projection facts plus Representation Policy IR choose representation occurrence
  roles and symbol variants. Missing or ambiguous mapping produces structured diagnostics.

### AD-7 - Same Semantic Entity May Have Multiple Occurrences

- **Binds:** FR-15
- **Prevents:** one device equals one drawn box.
- **Rule:** A semantic device can bind to different occurrence roles: coil, contact, terminal strip,
  cross-reference, load, report, maintenance, or future non-electrical projection.

### AD-8 - Semantic Reference Occurrences Are First-Class Projection Outputs

- **Binds:** FR-19
- **Prevents:** professional references being drawn as disconnected labels or QET-like visual hacks.
- **Rule:** Coil/contact, device/terminal-strip, component/location, and folio continuation
  references must be represented as semantic reference occurrences bound to visual definitions.

### AD-9 - Composition Intent Is Patterned Projection, Not CAD Truth

- **Binds:** FR-20, FR-21, FR-22, FR-23, FR-24, FR-25
- **Prevents:** generic graph layout and CAD geometry persistence.
- **Rule:** Schematic Composition Intent Compiler v0 produces planning facts such as lane
  membership, column membership, alignment group, label band, route channel, and reference zone.
  It does not write source truth or create a drawing database.

### AD-10 - Renderer Is Paint-Only

- **Binds:** FR-26, FR-27, FR-28, FR-29, FR-30, NFR-4
- **Prevents:** frontend patches from becoming architecture.
- **Rule:** Renderer paints resolved Presentation IR. Normal hitboxes and component backgrounds are
  transparent; visible interaction chrome is stateful and transient only.

### AD-11 - Representation Definitions Have Lifecycle Metadata

- **Binds:** FR-4, FR-13
- **Prevents:** long-lived engineering projects losing track of symbol evolution.
- **Rule:** Representation definitions carry version, status, superseded-by, migration hint, and
  provenance metadata. M30 validates metadata but does not implement full project migration.

### AD-12 - QET Is Reference And Optional Offline Input Only

- **Binds:** FR-36, FR-37, FR-38
- **Prevents:** importing QET's mental model into Athena runtime.
- **Rule:** A future QET converter may target Athena Representation IR offline. Runtime must not
  load QET `.elmt`, and `.athena` must not reference QET paths.

### AD-13 - Product Proof Must Be Visually Credible

- **Binds:** FR-31, FR-32, FR-33, FR-34, FR-35, NFR-5, NFR-6
- **Prevents:** accepting another architecture-only renderer milestone.
- **Rule:** M30 requires both structured proof and screenshot proof for one customer-demo sheet.

### AD-14 - Every Story Ends With Polish/Purge

- **Binds:** NFR-8, NFR-9
- **Prevents:** stale visual experiments, dead renderer code, and false docs surviving.
- **Rule:** Each story must remove or ledger stale code, docs, examples, screenshots, tests, and
  design claims it introduces or exposes.

## Proposed Structural Seed

```text
kernel/
  representation-model/       # Representation Policy, Definition, Occurrence IR contracts
  representation-library/     # native symbol pack loading/validation if module split is warranted
  compiler/                   # binding compiler and schematic composition intent integration
  presentation-model/         # resolved Presentation IR remains renderer input

ide/
  lsp/                        # product-safe representation/composition proof payloads
  theia-frontend/             # adapter and paint surface only

examples/
  m30/sample-project/         # customer-demo sheet proof

_bmad-output/
  implementation-artifacts/m30
```

Exact Gradle module split may change after code inspection, but the dependency direction may not:

```text
semantic kernel -> projection facts -> representation binding -> presentation -> renderer
```

No reverse dependency from kernel to representation assets or Theia is allowed.

## Deferred

| Deferred | Reason |
| --- | --- |
| Full IEC/company standards pack | M30 proves the representation system with a demo set only. |
| QET `.elmt` product importer | Converter is non-trivial and must target Representation IR offline. |
| Symbol editor UI | M30 needs authored native assets and tests, not a drawing tool. |
| PDF/print/revision workflow | Downstream of credible representation and composition. |
| P&ID/hydraulic/pneumatic/robot/BIM packs | Future domains can reuse the representation boundary after v0 proves out. |
