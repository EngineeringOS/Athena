---
name: Athena M33 Professional Engineering Drawing Engine Foundation
type: architecture-spine
purpose: build-substrate
altitude: milestone-to-epics
paradigm: package-backed-generic-drawing-engine
scope: drawing symbol anatomy, Graphic Primitive IR, SVG renderer adapter, sheet composition, package-backed graph integration, Workbench UX cleanup, demo proof
status: draft
created: '2026-07-23'
updated: '2026-07-23'
binds:
  - M33 PRD FR-1..FR-46
  - M33 PRD NFR-1..NFR-11
sources:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-23-m33/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-23-m33/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md
---

# Architecture Spine - Athena M33 Professional Engineering Drawing Engine Foundation

## Design Paradigm

M33 uses a package-backed generic drawing engine:

```text
.athena semantic source
  -> Engineering Semantic Model
  -> Engineering Package / Presentation Profile / Binding Manifest
  -> Representation Package / Drawing Symbol Anatomy
  -> Graphic Primitive IR
  -> Sheet Composition Facts
  -> Presentation IR
  -> SVG Renderer Adapter
  -> Workbench Surface
```

IEC electrical symbols are the first proof package. The drawing engine contracts must remain
generic enough for later P&ID, hydraulic, pneumatic, robotics, SCADA, cabinet, network, and
mechanical drawing packages.

## Inherited Invariants

| Inherited | Source | Binds M33 |
| --- | --- | --- |
| `.athena` remains semantic truth | M32 AD-1 | No visual syntax, symbols, coordinates, or resources enter source. |
| Package authorities are separated by kind | M32 AD-2 | Engineering Packages, Presentation Profiles, and Representation Packages stay separate. |
| Representation Descriptor is the resource contract | M32 AD-3 | Graphic resources never carry hidden semantic authority. |
| Presentation Profile is independent policy | M32 AD-4 | Profile remains policy input, not renderer option or source syntax. |
| Binding Resolver owns selection and mapping | M32 AD-6 | Workbench and renderer do not choose symbols or profiles. |
| Renderer consumes resolved handles only | M32 AD-10 | SVG adapter paints resolved Graphic Primitive IR only. |
| Structured proof is package acceptance authority | M32 AD-16 | Screenshot evidence supports but does not replace proof. |
| Polish and purge is a story gate | M32 AD-17 | Every M33 story ends with AC-to-evidence and cleanup. |

## Invariants And Rules

### AD-1 - Drawing Engine Is Generic, IEC Is First Package [ADOPTED]

- **Binds:** FR-1..FR-11, NFR-2, NFR-8
- **Prevents:** M33 becoming a new ECAD-specific core layer.
- **Rule:** Engineering Drawing Symbol Anatomy and Graphic Primitive IR are domain-neutral
  contracts. IEC-style electrical/control symbols are the first M33 representation package only.

### AD-2 - Graphic Primitive IR Is Renderer-Neutral [ADOPTED]

- **Binds:** FR-12..FR-20, NFR-3, NFR-4
- **Prevents:** SVG paths, DOM ids, CSS classes, or JSX fragments becoming drawing authority.
- **Rule:** Representation descriptors compile to Graphic Primitive IR. Concrete adapters such as
  SVG consume Graphic Primitive IR and paint only.

### AD-3 - Symbol Anatomy Owns Anchors And Slots [ADOPTED]

- **Binds:** FR-1..FR-11, FR-20, FR-40
- **Prevents:** center-fallback routes, duplicated text, label guessing, and hidden DOM semantics.
- **Rule:** Symbol definitions declare bounds, anchors, label slots, reference slots, hotspots,
  orientation support, lifecycle, and provenance. Missing required anchors or slots fail closed.

### AD-4 - Sheet Composition Produces Facts, Not CAD Truth [ADOPTED]

- **Binds:** FR-21..FR-26
- **Prevents:** composition becoming a page-centric CAD geometry database.
- **Rule:** Composition owns frame, zones, lane membership, label bands, terminal-strip grouping,
  route channels, references, and bounds facts. It does not mutate `.athena` or own final semantic
  truth.

### AD-5 - Workbench UX Is Separate From Drawing Engine [ADOPTED]

- **Binds:** FR-32..FR-37, NFR-5
- **Prevents:** toolbar state, Create Device panels, and debug controls changing rendering meaning.
- **Rule:** Workbench owns customer-facing controls and interaction chrome. Drawing engine output is
  independent of toolbar labels, panel state, and debug affordances.

### AD-6 - Presentation Profile Remains First-Class Policy [ADOPTED]

- **Binds:** FR-27..FR-31, NFR-2
- **Prevents:** IEC/ANSI/customer/print/maintenance choices becoming renderer options or source
  syntax.
- **Rule:** Presentation Profile is policy input consumed by binding and drawing selection. It is
  not a symbol package, source feature, or Theia preference.

### AD-7 - Normal Chrome Must Be Transparent [ADOPTED]

- **Binds:** FR-18, FR-19, FR-43
- **Prevents:** toy UI wrappers, visible hitboxes, duplicate borders, and low-density graph cards.
- **Rule:** Normal drawing state shows engineering symbols only. Hover, selection, focus, and drag
  chrome are transient Workbench overlays.

### AD-8 - ViewBox And Framing Are Derived From Content [ADOPTED]

- **Binds:** FR-17, FR-23, FR-40, SM-4
- **Prevents:** hard-coded oversized canvas and off-screen ghost elements.
- **Rule:** Bounds derive from Graphic Primitive IR, sheet composition, routed anchors, labels, and
  governed margins. Hard-coded sample viewBox values fail proof.

### AD-9 - Product Surface Is Cabinet Only [ADOPTED]

- **Binds:** FR-32..FR-37
- **Prevents:** cabinet/documentation/schematic/debug competition that leaves every surface
  incomplete and visually weak.
- **Rule:** Cabinet is the only customer-facing M33 surface. Documentation and Schematic/Wiring
  remain hidden compatibility or inspection projections and receive no M33 product-polish scope.

### AD-10 - Visual Claims Require Structured Evidence [ADOPTED]

- **Binds:** FR-38..FR-46
- **Prevents:** "looks better" claims without deterministic proof.
- **Rule:** M33 proof includes package resolution, symbol anatomy, Graphic Primitive IR, sheet
  composition, route anchors, text placement, bounds, viewBox, DOM no-fallback/no-duplicate checks,
  screenshots, and cleanup ledger.

## Dependency Direction

```text
Package model / profile / manifest
  -> Drawing Symbol Anatomy
  -> Graphic Primitive IR
  -> Sheet Composition Facts
  -> Presentation IR
  -> SVG Renderer Adapter
  -> Workbench UI
```

No downstream layer may infer upstream truth. No renderer adapter may select a symbol, profile, or
package.

## Structural Seed

```text
kernel/
  drawing-model/              # symbol anatomy, Graphic Primitive IR, drawing diagnostics
  package-model/              # existing package/profile/descriptor contracts
  presentation-model/         # existing Presentation IR integration

runtime/
  drawing-runtime/            # descriptor -> primitive compilation, proof payloads
  package-runtime/            # existing package and binding resolution

compiler/
  drawing-composition/        # sheet frame, zones, lanes, terminal strips, bounds facts

ide/
  lsp/                        # drawing proof payload transport
  theia-frontend/             # Workbench adapter, toolbar cleanup, SVG render adapter integration

examples/
  m33/sample-project/
  m33/sample-project/packages/

_bmad-output/
  implementation-artifacts/m33/
```

Exact module splits follow existing repository patterns during implementation. Dependency direction
above is binding.

## Capability To Architecture Map

| Capability | Lives in | Governed by |
| --- | --- | --- |
| Engineering Drawing Symbol Anatomy | `kernel/drawing-model` seed or representation model extension | AD-1, AD-3 |
| Graphic Primitive IR | `kernel/drawing-model` seed | AD-2 |
| IEC-style symbol package | M33 example/packages + package runtime validation | AD-1, AD-3 |
| Descriptor-to-primitive compiler | runtime/compiler drawing layer | AD-2, AD-6 |
| Sheet composition facts | drawing composition compiler | AD-4, AD-8 |
| SVG renderer adapter | Theia/PIR renderer adapter | AD-2, AD-7 |
| Cabinet product surface | Theia Workbench adapter | AD-5, AD-9 |
| Demo proof | tests, smoke, screenshots, cleanup ledger | AD-10 |

## Deferred

| Deferred | Reason |
| --- | --- |
| Full IEC compliance | Needs standards audit and larger symbol library. |
| QET `.elmt` importer | Importer should target stable descriptor/anatomy contracts later. |
| Symbol editor | Data contracts and renderer proof must stabilize first. |
| Package catalog UI | M33 fixes drawing credibility; catalog UX is not the visible blocker. |
| Canvas/PDF/Skia/WebGPU adapters | Graphic Primitive IR prepares for them; SVG remains first adapter. |
| M34 Engineering Knowledge Runtime | Next platform layer after drawing credibility closes. |
