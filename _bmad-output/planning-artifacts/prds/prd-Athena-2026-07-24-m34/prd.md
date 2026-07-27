---
title: Athena M34 - Typed Symbol And Element System
status: draft
created: '2026-07-24'
updated: '2026-07-24'
---

# Athena M34 PRD - Typed Symbol And Element System

## Executive Summary

M34 corrects the package and rendering direction exposed by M33. M33 proved that Athena can route
a Cabinet surface through packages, representation facts, Graphic Primitive IR, and Electron. It
also introduced a fragmented XML runtime path that duplicated semantic selectors and lacked Athena
language, compiler, and IDE governance.

M34 replaces that runtime authority with one compiled Symbol and Element system. The definition
entry point is always Athena source. Simple visuals use a native Athena `graphic` body. Complex
visuals use an Athena `graphic svg "..."` reference to one governed annotated SVG geometry resource.
Both graphic body forms lower into the same canonical contracts while preserving the valid M32
boundaries for Presentation Profile, binding policy, package identity, and deterministic resolution.

The core hierarchy is:

```text
Symbol          -> atomic visual glyph
Element         -> reusable visual composition with compatible anchors
Binding Policy  -> selects an Element from semantic facts and Presentation Profile
Cabinet         -> project view composed from governed Element occurrences
```

Each reusable definition has exactly one Athena metadata authority. The `.athena` declaration owns
kind, identity, version, lifecycle, composition, binding visibility, and the choice of graphic body.
A complex SVG owns only its geometry plus node-local `data-athena-*` anchor, slot, hotspot, and
compatibility annotations. The compiler never merges duplicate fields between Athena and SVG.
Project `.athena` files remain the SSOT for actual devices, instance ports, connections,
Profile/Binding policy, and layout intent.

M34 focuses on Cabinet only. Documentation, Schematic, and Wiring remain hidden compatibility
paths.

## Change Control From M33

M33 handed off two goals: harden the XML package path and finish a professional Cabinet surface.
M34 supersedes the first goal because the party review found XML runtime authority itself to be the
wrong product boundary. XML parsing exists only inside the hardened compile-time SVG frontend;
legacy XML product/runtime compatibility is removed. M34 keeps the professional Cabinet outcome as
a mandatory completion gate.

## Authority Model

| Concern | Authority | Must Not Own |
| --- | --- | --- |
| Project engineering instances | `.athena` project source | Reusable drawing geometry |
| Atomic visual anatomy | One Athena Symbol declaration; native graphic body or governed SVG body | Project ports or device classification |
| Reusable visual composition | One Athena Element declaration; Symbol children or one governed SVG body | Actual device instances or connections |
| Representation selection | Presentation Profile and typed binding policy | Semantic mutation |
| Package identity and discovery | Package model plus repository `athena.yaml` declarations | Project device facts |
| Representation vocabulary | Versioned Athena compiler schema | Package-specific reinterpretation |
| Occurrence placement | Spatial/composition compiler | Intrinsic Element child layout |
| Generated IR, caches, and evidence | Reproducible derived output | Source authority |
| Visual output | Graphic Primitive IR and renderer | Engineering inference |

Direction, signal, role, and terminal information in a representation source are compatibility
predicates for binding. Native Athena anchors declare them directly. For complex SVG graphics,
selected SVG nodes may declare only these node-local reusable representation contracts through the
closed `data-athena-*` profile. SVG cannot declare definition identity/version/kind, actual project
ports, or engineering facts, and unmarked geometry has no Athena meaning.

An `Element` is a reusable visual representation, not a reusable engineering component definition.
An ABB drive, PLC, or motor family may eventually own parameters, functions, validation, behavior,
and several view-specific Elements through a separate Engineering Component System. M34 preserves
that boundary but does not introduce the component language.

## Product Problem

The M33 Cabinet sample splits one reusable visual path across engineering catalog XML, presentation
profile XML, binding manifest XML, representation package XML, and native Kotlin resources. Fields
such as `semanticType` duplicate or appear to duplicate project truth. The result is hard to author,
hard to validate in the IDE, and easy to evolve inconsistently.

Athena needs reusable visual engineering definitions that are:

- compiled and type checked;
- understandable as one logical unit;
- authored from one Athena definition with either a native graphic body or one governed annotated SVG
  geometry resource, with deterministic compiler and lint diagnostics;
- selected by a separate governed policy;
- lowered into paint-only Graphic Primitive IR;
- safe to load from vendor or user content.

## Goals

- Define Symbol as the atomic reusable visual concept.
- Define Element as reusable visual composition with governed anchor compatibility.
- Support Symbol and Element declarations in ordinary `*.athena` source.
- Support native typed Athena Graphic Primitive bodies when concise textual geometry is the clearer
  and more maintainable source.
- Support governed annotated SVG as a first-class graphic body when the visual is shape-heavy,
  design-tool-authored, or materially clearer as SVG.
- Enforce exactly one metadata authority per definition with no duplicate-field merge or override behavior.
- Parse each source through its governed frontend, then run the same semantic type checking, lint,
  canonical admission, and lowering contracts.
- Preserve Presentation Profile and Binding Resolver as typed policy boundaries.
- Validate project ports against Element anchors without guessing or reclassification.
- Compile untrusted annotated SVG through a closed safe geometry and metadata profile into canonical
  representation contracts and Graphic Primitive IR.
- Migrate the active Cabinet path away from XML manifest authority.
- Deliver one visually credible Cabinet example with structured and screenshot evidence.
- Extend ANTLR4, tree-sitter, LSP diagnostics, completion, highlighting, and outline.

## Non-Goals

- No full vendor package marketplace.
- No symbol editor UI.
- No full QElectroTech importer implementation.
- No runtime QET schema or QET dependency.
- No IEC compliance or EPLAN/QET equivalence claim.
- No broad Documentation, Schematic, or Wiring polish.
- No raw SVG DOM authority in the frontend.
- No project instance truth inside Symbol or Element definitions.
- No unrestricted SVG metadata, duplicate identity/version/kind in SVG, or source-precedence merge for one definition.
- No geometry, CSS class, DOM position, or SVG id guessing.
- No fourth parallel representation IR.
- No Engineering Component System or `component` declaration; M34 does not mix reusable engineering
  product knowledge into visual Elements.

## Functional Requirements

### Feature 1 - Symbol And Element Contracts

**FR-1:** M34 shall define `Symbol` as the smallest reusable visual glyph with identity, lifecycle,
provenance, visual body, intrinsic bounds, anchors, label slots, and hotspots.

**FR-2:** M34 shall define `Element` as a reusable visual component composed from Symbols or one
governed visual body referenced by its Athena declaration.

**FR-3:** Every connectable Symbol or Element anchor shall declare role, accepted direction, and
accepted signal predicates, but shall not declare actual project device ports or connections.
Non-connectable layout/label anchors shall not pretend to be terminal anchors.

**FR-4:** Symbol and Element definitions shall not own actual device instances, authored layout
intent, or semantic device classification.

### Feature 2 - Governed SVG Geometry

**FR-5:** M34 shall accept complex reusable graphics through Athena `graphic svg "..."` references
to governed SVG resources using an exhaustive lowercase `data-athena-*` node profile. XML shall not
be a package manifest, general product language, transport payload, or runtime authority; it is only
the syntax parsed by the isolated SVG compiler frontend.

**FR-6:** An annotated SVG root shall declare only `data-athena-schema="representation/v1"` plus
ordinary governed geometry root facts such as namespace and viewBox. Representation kind, identity,
version, and lifecycle are owned by the referencing Athena declaration. Selected SVG nodes may
declare only reusable representation anchors, explicit anchor points, label slots, hotspots, role,
direction, signal, and terminal compatibility through governed `data-athena-*` attributes. SVG
`<metadata>`, arbitrary namespaces, CSS, and ungoverned attributes are not authority.

**FR-7:** The compiler shall reject missing/duplicate definition or SVG ids, unknown/duplicate
`data-athena-*` fields, invalid typed values, unresolved marked references, incompatible annotation
targets, and unsupported geometry/reference targets.

**FR-8:** Unmarked SVG geometry, ids, CSS, element kinds, and DOM order shall never be interpreted as
engineering or representation meaning. Only explicit governed attributes may bind a selected node to
a representation contract, and no SVG attribute may declare project devices, actual ports,
connections, classification, Profile, or Binding policy.

### Feature 3 - Athena Representation Source

**FR-9:** M34 shall introduce `symbol`, `element`, `profile`, and `binding` declarations in the same
Athena language and ordinary `*.athena` file extension used by the platform.

**FR-10:** Symbol and Element definitions shall support native typed `graphic` bodies parsed by
ANTLR4 and tree-sitter and checked by the Athena semantic type checker and linter. Native Athena is
preferred when it is the smaller, clearer source; it shall not be required for shape-heavy visuals
that are more maintainable as one governed annotated SVG.

**FR-11:** Complex definitions shall be authorable as one Athena declaration that references one
annotated SVG geometry resource. A `(library, identity, version)` may be authored by exactly one
Athena declaration, and a declaration may contain either native geometry or one SVG graphic
reference, never both.

**FR-12:** ANTLR4, tree-sitter, SVG parsing, LSP diagnostics, completion, highlighting, and outline
shall support the governed source constructs. Parser, semantic type checker, linter, and formatter or
canonicalizer diagnostics shall be deterministic, machine-readable, and source-spanned for human and
AI authoring.

### Feature 4 - SSOT And Binding Validation

**FR-13:** `.athena` project files shall remain SSOT for device instances, instance ports,
connections, and authored layout intent.

**FR-14:** Symbol and Element definitions shall be SSOT only for reusable representation contracts.

**FR-15:** A Symbol, Element, Presentation Profile, or binding policy shall not reclassify an
authored device or alter its ports.

**FR-16:** Binding shall fail closed when a project port cannot bind to a compatible Element anchor.

**FR-17:** Direction, signal, role, and terminal compatibility shall be validated from the authored
project port to the Element anchor predicates.

### Feature 5 - Representation Selection And Source Authority

**FR-18:** Presentation Profile, BindingManifest, RepresentationBindingRule, BindingResolver,
RepresentationPolicy, and RepresentationBindingCompiler shall form one typed responsibility chain.

**FR-19:** Presentation Profile shall constrain projection context, standards/style, compatibility,
and fallback; BindingManifest shall join engineering concept/package to allowed representation
packages; Athena `binding` declarations shall compile to typed `RepresentationBindingRule`; and
BindingResolver shall be the sole descriptor/variant selection authority.

**FR-20:** RepresentationPolicy shall be derived from a successful BindingResolver result, and
RepresentationBindingCompiler shall be the sole authority that constructs RepresentationOccurrence.
Neither may perform a second independent selection.
Legacy `BindingManifest.policyTags` shall have no active M34 selection authority.

**FR-21:** One Athena declaration shall be the sole metadata authority for every reusable definition.
XML manifests, SVG root identity/version/kind, duplicate sidecars, Kotlin fixtures, and renderer code
shall not compete with it.

**FR-22:** Athena representation declarations shall lower into the existing `RepresentationDefinition`,
extended with Symbol/Element kind, composition, anchor compatibility, Graphic Primitive body, and
source provenance. `RepresentationDescriptor` shall be generated and not independently authored.

**FR-23:** M34 shall publish and enforce a `Reuse / Extend / Replace / Delete` migration map for
`DrawingSymbolAnatomy`, `RepresentationDefinition`, `RepresentationDescriptor`, selection/binding
contracts, visual primitive vocabularies, XML loaders, Cabinet lowerers, and renderers.

### Feature 6 - Secure SVG Compilation

**FR-24:** All SVG input shall pass through a namespace-aware, fail-closed, DTD/entity/XInclude-disabled
compiler frontend with a no-I/O resolver and exhaustive geometry plus `data-athena-*`
element/attribute/value grammar before reaching canonical representation contracts or Graphic
Primitive IR.

**FR-25:** The SVG compiler shall reject scripts, event attributes, `foreignObject`, external URLs,
unsafe CSS resources, DTD/entities, and ungoverned resource references.

**FR-26:** The SVG compiler shall enforce per-file and aggregate package bounds for source bytes,
file count, DOM depth, element count, path segments, transform depth, `use` expansion, emitted
primitives, and deterministic compiler work units.

**FR-27:** External asset resolution shall use a repository-root-confined immutable staging snapshot,
no-follow acquisition, pre/post stable file-identity verification, and rejection of absolute paths,
traversal, symlinks, Windows junctions/reparse points, or unsupported secure-open guarantees.

**FR-28:** Anchor coordinates shall normalize deterministically into the visual root coordinate
system across supported groups, transforms, viewBox, `defs`, and `use` constructs.

**FR-29:** LSP/Electron transport contracts shall be structurally incapable of carrying raw SVG/HTML
markup, and instrumented E2E shall prove no raw-markup parsing sink handles Cabinet content.

The safe frontend shall extend the existing `GraphicPrimitive` vocabulary only where required for
professional assets, including governed ellipse and normalized path segments. It shall not create a
parallel SVG-specific primitive model.

### Feature 7 - Cabinet Product Path

**FR-30:** The active M34 Cabinet path shall render from compiled `RepresentationDefinition` through the
existing representation, binding, Graphic Primitive IR, and renderer boundaries.

**FR-31:** Active Cabinet rendering shall not require M32/M33 XML manifests as product runtime
authority.

**FR-32:** Every visible reusable Cabinet component shall identify its compiled Element definition,
selected binding policy, anchors, labels, and source provenance in structured proof.

**FR-33:** The Cabinet smoke shall fail on generic fallback boxes, unresolved visual bodies,
missing anchor metadata, missing required labels, duplicated/off-screen content, raw SVG bypass,
and hard-coded document viewBox.

**FR-34:** Intrinsic Symbol/Element viewBox values may be fixed asset coordinates; the Cabinet
document viewBox shall derive from compiled composition bounds.

**FR-35:** The M34 example shall visibly include a cabinet enclosure, DIN rails, professional
element proportions, terminal placement, readable labels, route channels, and a restrained drawing
frame without normal-state component borders. Structured proof shall report zero fallback elements,
zero clipped elements, zero text overflow, zero unintended element overlap, zero normal-state
hitbox/border visibility, and zero route endpoints outside their bound anchors.

**FR-36:** Electron evidence shall use the M34 sample at 1920x1080 and 1280x900 with a fresh temporary
user-data directory, deterministic active Cabinet view, zero LSP diagnostics, structured proof,
canvas pixel nonblank checks, and human visual review against the approved Cabinet reference criteria.

### Feature 8 - Legacy And Importer Boundary

**FR-37:** M32/M33 XML product/runtime paths shall be removed. The cleanup ledger records deletion
evidence and verification, not retained compatibility.

**FR-38:** QET `.elmt` may be used only as input to an offline converter design after the Athena
Element contract is stable.

**FR-39:** Any importer or AI author shall output canonical Athena representation source. For complex
geometry it may also output one referenced governed annotated SVG resource, but definition metadata
stays in Athena source. Foreign schemas and raw source metadata shall not enter product runtime.

**FR-40:** Imported connectable points shall be rejected until explicitly governed by native Athena
or valid `data-athena-*` representation metadata and validated against the same canonical compiler
contract as native assets.

### Feature 9 - Evidence And Cleanup

**FR-41:** Every M34 story shall include RED/GREEN evidence, AC-to-evidence mapping, three-layer
review, and a final polish/purge task that examines source, tests, fixtures, docs, generated assets,
and workspace state for stale or duplicate artifacts.

All representation material produced by a human, importer, or AI shall pass its governed parser,
semantic type checking, stable machine-readable lint diagnostics with source spans, deterministic
formatting/canonicalization, and compilation before package admission or Cabinet projection.

**FR-42:** Every epic shall produce a Cabinet-visible outcome paired with structured end-to-end
evidence; product proof shall not be deferred to a standalone closure-only epic.

**FR-43:** M34 shall include fresh Cabinet-only Electron E2E evidence and a retrospective that
records whether SSOT recovery, SVG safety, XML retirement, and visual credibility succeeded.

## Non-Functional Requirements

**NFR-1:** Athena compilers own interpretation and validation of reusable representation metadata.

**NFR-2:** Annotated SVG is an untrusted compile-time representation source, never project semantic
truth, runtime metadata, or renderer input.

**NFR-3:** A reusable Symbol or Element shall be understandable as one logical authoritative artifact:
one Athena declaration, optionally paired with one governed SVG geometry resource whose metadata
scope is limited to node-local contracts.

**NFR-4:** Binding and rendering shall be deterministic and fail closed.

**NFR-5:** No downstream layer may infer engineering meaning from geometry, CSS, DOM order, or an
ungoverned identifier.

**NFR-6:** Existing Presentation Profile, Binding Resolver, Representation Descriptor, and Graphic
Primitive IR boundaries shall be reused or explicitly migrated rather than duplicated.

**NFR-7:** Cabinet is the only customer-facing M34 surface.

**NFR-8:** Visual density shall remain suitable for industrial engineering work: transparent normal
component backgrounds, no visible normal-state hitboxes, and no decorative card chrome.

**NFR-9:** Repository text shall remain UTF-8 and generated evidence shall not become an
unreviewed source authority.

**NFR-10:** Representation sources shall be discovered only from package roots declared by the
repository contract. Runtime shall consume an immutable compiled package snapshot; renderers shall
not scan or reread package files.

**NFR-11:** Compiled caches shall be content-addressed by source bytes, compiler/schema version,
and dependency lock digest, and shall be safe to purge and reproduce.

**NFR-12:** The public Athena representation grammar shall remain small and domain-facing. M34 adds
only `symbol`, `element`, `profile`, `binding`, and the minimum nested visual/anchor constructs needed
to author them. Compiler/runtime terms such as descriptor, occurrence, renderer, transport, DOM, and
IR shall not become source-language declarations.

## Core Acceptance Scope

M34 is complete when:

1. Symbol and Element ownership is compiler-enforced without duplicating project port truth.
2. Every definition has exactly one Athena metadata authority; native graphic bodies and referenced
   governed SVG bodies both compile to the same canonical contract and never duplicate fields.
3. SVG security, resource limits, coordinate normalization, and repository-root confinement pass.
4. ANTLR4, tree-sitter, LSP diagnostics, completion, highlighting, and outline support the source.
5. The ordered profile/manifest/resolver/policy/binding chain selects and constructs occurrences
   without duplicate selection or semantic mutation.
6. The active Cabinet path has no XML manifest or raw SVG runtime authority.
7. The Cabinet example passes structured proof and desktop/narrow visual review.
8. The M30-M33 migration map and cleanup ledger identify every retained and retired path.
9. Package discovery, immutable snapshots, identity collisions, and reproducible caches pass.

## Success Metrics

**SM-1:** Every reusable visual definition has exactly one Athena metadata authority and zero
XML/Kotlin/SVG-root metadata peers; duplicate identity across Athena declarations rejects the entire
package.

**SM-2:** Project port truth appears only in project semantic source and compiled semantic model.

**SM-3:** Malicious, ambiguous, oversized, or ungoverned SVG fails before Graphic Primitive IR.

**SM-4:** Cabinet proof identifies the selected Element and binding policy for every visible device.

**SM-5:** No XML manifest or raw SVG DOM is required by the active M34 Cabinet path.

**SM-6:** The approved Cabinet screenshots contain no generic fallback components and satisfy the
professional visual checklist.

## Counter-Metrics

**SM-C1:** Do not replace XML fragmentation with multiple authorities for one definition. Source-kind
choice is exclusive and canonical output is shared.

**SM-C2:** Do not collapse Presentation Profile or binding policy into Element definitions.

**SM-C3:** Do not create a new IR where an existing M30-M33 contract can be extended.

**SM-C4:** Do not polish Documentation, Schematic, and Cabinet simultaneously.

**SM-C5:** Do not claim standards compliance or customer-demo quality from architecture proof alone.

**SM-C6:** Do not turn Element into an engineering product/component definition or expose compiler
implementation vocabulary as public Athena language concepts.

## Resolved Decisions

1. Project, Profile, Binding, Symbol, and Element declarations use ordinary `*.athena` files.
2. Source form follows maintainability, not format preference: concise textual geometry uses native
   Athena `graphic { ... }`; shape-heavy or design-tool-authored geometry uses `graphic svg "..."`
   with `data-athena-*` only on the SVG nodes that carry node-local representation contracts. Both
   compile to the same canonical contracts; SVG never duplicates definition identity/version/kind.
3. QET conversion remains a documented offline boundary in M34, not a product runtime feature.
4. `RepresentationDefinition` is the canonical compiled reusable representation contract.
   `RepresentationDescriptor` is its generated package/index projection.
5. `BindingResolver` alone selects descriptor and variant. `RepresentationBindingCompiler` alone
   constructs the occurrence from the resolved selection.
6. `GraphicPrimitive` is the sole active Cabinet drawing vocabulary. `PresentationPrimitive` and
   direct SVG/box paths are compatibility-only until their recorded deletion gates pass.
7. `Element` remains visual representation. A future Engineering Component System may own reusable
   product parameters, ports, constraints, behaviors, and representation sets, but is outside M34.
8. `GraphicPrimitive` remains the internal M34 name to avoid migration churn; public Athena source
   exposes only domain-facing visual constructs, not the IR type name.

## Corrective Product Acceptance - Professional Control Drawing

This section was approved on 2026-07-26 and supersedes every earlier statement that Cabinet is the
only M34 product surface or final completion target. Earlier Cabinet work remains implementation
history and reusable infrastructure. Final M34 acceptance is one focused professional control
drawing based on `draft/screenshort/equipement_d'un_volet_roulant.png`.

**FR-44:** The M34 sample shall compile and render one professional rolling-shutter electrical
control drawing with a 17-column by 8-row sheet, border coordinates, title block, power circuit,
control circuit, terminal identities, device references, conductors, and cross-references.

**FR-45:** Athena shall model a project-owned `EngineeringFunction` as a typed functional unit of
one physical `EngineeringComponent`. A function references existing component-owned semantic ports;
it does not duplicate device identity, ports, or representation geometry. The minimum proof shall
cover a contactor coil, main contact group, NO auxiliary contact, and NC auxiliary contact.

**FR-46:** Authored layout shall support deterministic projection-occurrence placement in typed
drawing-grid coordinates and orientation. These facts belong to layout/projection intent and shall
not enter Engineering IR as device properties or renderer-owned guesses.

**FR-47:** Native Athena graphic bodies shall expose the minimum concise drawing vocabulary already
represented by Graphic Primitive IR: line, polyline, arc, circle, rectangle, and governed dynamic
label slots. A general path language is not added; shape-heavy visuals remain package-local governed
SVG resources.

**FR-48:** Representation resolution shall create multiple function-aware occurrences for one
physical semantic device while preserving canonical physical identity, terminal bindings, source
provenance, interaction subject discovery, and cross-reference facts.

**FR-49:** The active sample and product path shall not model sheet frames, title blocks, zones,
DIN rails, route channels, or labels as engineering devices and shall not infer engineering roles
from identifiers such as `QF`, `KM`, `XT`, or `M`.

**FR-50:** Fresh Electron E2E shall open the dedicated M34 sample, select the focused professional
drawing surface, prove zero LSP diagnostics and zero fallback/XML/raw-markup authority, assert a
nonblank correctly framed canvas, and compare actual screenshots at 1920x1080 and 1280x900 against
the approved visual contract. A screenshot background, mocked sheet, hardcoded rendered image, or
weakened assertion is prohibited.

**NFR-13:** QElectroTech sources are offline design evidence only. Athena may reinterpret terminal
position/orientation, reusable local geometry, repeated physical-device representations, orthogonal
routing, and separate sheet metadata, but shall not copy QET XML runtime, page object ownership,
Master/Slave classes, or text-label linking.

**NFR-14:** The full reusable Engineering Component System remains deferred. M34 adds only the
minimum generic project-function contract required to represent one physical subject faithfully.

M34 is not complete until FR-44 through FR-50 pass. The earlier Cabinet-only SM-4 through SM-6 and
NFR-7 are historical criteria and no longer authorize milestone completion.
