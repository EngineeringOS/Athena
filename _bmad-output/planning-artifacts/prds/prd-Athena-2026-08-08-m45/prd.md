---
title: Athena M45 Symbol, Element, Part and Engineering Package Authoring
status: final
created: 2026-08-08
updated: 2026-08-08
---

# PRD: Athena M45 Symbol, Element, Part and Engineering Package Authoring

## 0. Purpose

M45 extends the M44 editable canvas into a complete local engineering-library authoring loop. It
proves that a project can consume package-local IEC, vendor, and community definitions; resolve
Symbol, Element, Part, Macro, Variant, and Placeholder metadata; bind those definitions to typed
Engineering Functions; and render a complete rolling-shutter engineering page.

M45 uses project-local packages as a registry simulation. No remote package registry is required.
The package and lock contracts remain implementation-neutral and are designed for a future Maven/npm/
OCI-like registry without changing project source semantics.

M45 is breaking and milestone-local. No retired compatibility path, raw legacy macro runtime, or direct
runtime dependency on `reference/elements` or `reference/elements_contrib` is allowed.

## 1. Vision

An engineer can select a reusable engineering Element, inspect its ports and composition, bind it to a
Function and Part, place it on a Sheet, and obtain a readable page with real symbol geometry and
traceable connections. A library item is not a copied picture. It is a versioned, inspectable package
item with geometry, interfaces, provenance, and compatibility metadata.

The M45 golden result is visually similar to:

`draft/screenshort/equipement_d'un_volet_roulant.png`

The proof need not claim that the illustrative circuit is production-certified. Definitions, interfaces,
relationships, package provenance, and source traces must be complete and deterministic. Standards
correctness is recorded separately from rendering completeness.

## 2. Product Boundaries

### 2.1 Authority

```text
Athena source
  Entity / Function / Relationship / Port / Part binding
        |
Package metadata
  Symbol / Element / Part / Macro / Variant / Placeholder facts
        |
Canonical Scene
        |
Theia / SVG / PNG
```

- Athena source owns engineering meaning and authored relationships.
- Package metadata owns reusable library facts and representation compatibility.
- SVG bytes own graphic geometry only. SVG bytes may be copied into an admitted package.
- External catalog formats are reference material only. Athena does not parse, convert, package, or
  execute them. Package metadata is authored directly in Athena-native contracts.
- Sheet source owns occurrence placement, frame, and presentation intent.
- Canonical Scene owns derived render data.

### 2.3 Three-Layer Spatial Location

Athena does not expose one universal X/Y coordinate system. Spatial location has three authorities:

```text
SemanticPlacementIntent
  region / role / relationship to engineering area
        |
LogicalLayoutCoordinate
  Sheet / Region / Cell / SubGrid position
        |
PhysicalGeometryCoordinate
  compiler-derived x / y / rotation / bounds for scene and renderer
```

- Engineering source may own durable semantic placement intent when location has engineering meaning.
- Sheet source owns authored logical layout coordinates. Logical coordinates remain stable when page
  physical size, viewport, zoom, or render backend changes.
- Compiler owns physical geometry derivation. Physical x/y is not normal authoring syntax or engineering
  truth.
- Snap is an independent interaction/layout policy. Changing snap spacing cannot reinterpret persisted
  logical coordinates.
- Engineering Port owns connection meaning. Symbol/Element package metadata maps stable port keys to
  projection anchors. SVG points never become semantic ports.

### 2.2 Local Package Layout

Every dependency is a direct package directory under the project `packages` directory. No extra
`registry/representation/engineering` nesting is used.

```text
examples/m45/rolling-shutter/
  packages/
    com.athena.iec/
      package.yaml
      symbols/
      elements/
      parts/
      macros/
      docs/
      resources/
    com.vendor.siemens/
      package.yaml
      symbols/
      elements/
      parts/
      macros/
      resources/
    com.community.reference-elements/
      package.yaml
      symbols/
      elements/
      parts/
      resources/
  athena.yaml
  athena.lock
```

`package.yaml` declares package identity, version, dependencies, provenance, license, and item index.
`athena.lock` fixes package and item digests. Package name is the directory name and must be stable.

Packages declare both exported items and semantic dependencies:

```yaml
exports:
  symbols: []
  elements: []
  parts: []
  macros: []
  interfaces: []
requires:
  packages: []
  capabilities: []
  interfaces: []
```

A package may depend on a stable capability or interface contract without depending on one concrete IEC
or vendor package. File/package dependencies remain explicit where exact bytes are required.

## 3. Domain Model

### 3.1 Package Item

Every exported library definition starts from one common authored Package Item metadata contract and is
published as one admitted Package Item:

```text
PackageItemMetadata
  packageId
  itemId
  itemVersion
  kind
  provenance
  license

AdmittedPackageItem
  metadata
  normalizedPayload
  digest
  packageProvenance
  admissionState
  diagnostics
```

`kind` is one closed value: `SYMBOL`, `ELEMENT`, `PART`, `MACRO`, `VARIANT`, or `PLACEHOLDER`.
Kind-specific payloads extend the common metadata. Digest, resolved references, admission state, and
diagnostics are compiler-owned and cannot be authored. Symbol, Element, Part, and Macro subsystems must
not duplicate common metadata or admission output contracts.

### 3.2 Symbol

A Symbol is one pure graphical representation. It owns no Engineering Function. Through Element and
binding metadata it may be compatible with no Function slot, one Function slot, multiple Function slots,
or part of one Function representation.

Symbol metadata must include:

- SVG resource path and digest;
- viewBox, bounds, center/origin, and transform policy;
- stable port-anchor mapping;
- package-side port compatibility declarations for key, direction, domain, signal/flow kind, and hit
  geometry; these declarations constrain binding compatibility and never replace source-owned
  Engineering Port identity or semantics;
- label slots and property anchors;
- symbol group, variants, provenance, license, and package trace.

### 3.3 Element

An Element is an insertable representation unit. It can expose one or more Symbols, map to one or more
Functions, and expose a stable interface for composition.

Required Element facts:

- identity and package item version;
- Symbol references and composition order;
- Function-slot mappings;
- exposed port contracts;
- internal representation links;
- compatible Part references;
- variants and placeholders;
- provenance and digest.

Element is representation composition, never an Engineering Entity or Device. The required chain is:

```text
Engineering Entity
  -> Function
  -> Function Representation Binding
  -> Element
  -> Symbol
```

No compiler or UI path may shortcut `Element = Entity`.

### 3.4 Part

A Part is a physical or procurement implementation. It is not a Symbol and does not own graphic
geometry.

Required Part facts:

- manufacturer and article/order number;
- technical properties and units;
- function templates and capability compatibility;
- compatible Elements/Symbols;
- accessories or related part references;
- provenance, license, and digest.

A Part may state implementation facts and compatibility. It cannot state that an engineering solution
should exist, create a relationship, or recommend a Pattern. Those decisions remain Knowledge/Pattern
authority.

### 3.5 Function Representation Binding

Function representation choice is an explicit object, not an implicit direct reference:

```text
FunctionRepresentationBinding
  functionId
  elementItemId
  variantItemId?
  placeholderValues
  compatibilityConstraints
  packageTrace
  provenance
```

One Function may own multiple bindings for IEC, regional, simulation, 3D, or other projections. A binding
change preserves Function and Engineering Entity identity.

### 3.6 Macro, Variant, Placeholder

M45 adopts the useful EPLAN concepts without treating a Macro as a raw image template.

- **Symbol Macro:** reusable composition of Symbol references and exposed ports.
- **Element Macro:** reusable representation set containing Elements and placement geometry.
- **Page Macro:** reusable Sheet representation and frame content; no hidden engineering mutation.
- **Variant:** named, versioned representation alternative with explicit compatibility conditions. It
  cannot change semantic identity or replace one engineering concept with another solution.
- **Placeholder:** typed unresolved parameter slot resolved during insertion or package configuration.
  It performs no selection, reasoning, recommendation, or design decision.

Macro insertion may create representation occurrences and bindings only when the source operation lists
those mutations explicitly. Macro expansion must not silently invent engineering relationships. Future
Engineering Pattern authoring may use these packages but is outside the M45 MVP.

`Macro != Pattern`. A representation Macro may reuse geometry, occurrences, bindings, placement,
variants, and placeholders. Only a future Engineering Pattern may generate an engineering solution.

### 3.7 Package Admission State

Every package and item publishes one explicit admission state:

- `PACKAGE_READY`: all required runtime facts are valid; optional vendor data may produce warnings.
- `PACKAGE_INCOMPLETE`: authored package content is inspectable but required runtime facts are missing;
  no binding or scene asset may be published.
- `PACKAGE_INVALID`: malformed, contradictory, unsafe, or digest-invalid content is rejected.

State and diagnostics are deterministic. No incomplete or invalid item silently falls back to a point,
placeholder box, or raw external asset.

## 4. Import and Admission

### FR-1: Admit Local Packages

The compiler must discover direct child packages under `packages/<package-name>`, validate `package.yaml`,
resolve package dependencies, and materialize deterministic `athena.lock` output.

All exported definitions must use the common Package Item envelope and closed item-kind type system.

### FR-2: Admit Package-Local SVG Resources

Package authors may place copied SVG bytes into governed package resources. Admission validates their
digest, provenance, license, safe profile, and package-relative resource path. SVG remains geometry
authority only. Compiler and runtime do not read the external reference directories.

### FR-3: Author Native Package Definitions

Symbol, Element, Part, Macro, Variant, and Placeholder metadata is authored directly through Athena-native
package contracts. Each definition references package-local resources and declares center/origin, port
compatibility, composition, variants, and provenance as appropriate. No external catalog format is an
Athena input or runtime contract.

### FR-4: Pair Metadata and Geometry

No Symbol or Element is admitted when required geometry, center/origin, exposed ports, digest, or
provenance is missing. Diagnostics name exact package, item, field, and correction.

Admission publishes `PACKAGE_READY`, `PACKAGE_INCOMPLETE`, or `PACKAGE_INVALID`. Missing optional vendor
facts may keep an item READY with warning; missing required runtime facts never does.

### FR-5: Resolve Part Compatibility

A Part binding must resolve to a package Part whose function templates and interfaces are compatible with
the authored Function. Incompatibility fails closed without source or scene mutation.

## 5. Authoring and Rendering

### FR-6: Bind Functions to Elements

An Engineering Function may bind through a `FunctionRepresentationBinding` to an Element while
preserving Entity, Function, Relationship, Port, Occurrence, and Source Trace identities. Representation
changes do not become engineering truth. Different projections may bind the same Function to different
Elements without changing Function identity.

### FR-7: Resolve Composite Elements

Composite Elements expose stable public ports and retain child Symbol identity. Internal composition is
inspectable package metadata, not an implicit engineering relationship generator.

### FR-8: Place and Edit Real Occurrences

The Theia library surface must support package browsing, metadata inspection, preview, and insertion of
an Element into the active Sheet. Placement submits typed source operations and recompiles the Canonical
Scene. No frontend source assembly is permitted.

Insertion may author `SemanticPlacementIntent` and/or `LogicalLayoutCoordinate` according to operation
authority. Compiler derives `PhysicalGeometryCoordinate`; frontend never persists raw canvas X/Y.

### FR-9: Publish Full Rolling-Shutter Page

The active M45 example must render a complete page containing, at minimum:

- supply, breaker, contactors, overload, motor, PLC, sensors, terminals, coils, auxiliary contacts,
  and indicator lamps;
- real package-backed Symbol/Element geometry;
- visible and connectable ports;
- relationship routes and labels;
- A-H row ruler and 1-17 column ruler;
- square one-pixel page frame, narrow flush top/left rulers, and clean print-oriented canvas with no
  default cell lines, micro-grid, or bottom title/table block.

The page must preserve stable identity and source traces through compile, edit, reopen, and export.

### FR-10: Macro Variant and Placeholder Resolution

The compiler must resolve a selected Macro Variant and typed Placeholder values deterministically. A
missing, ambiguous, or incompatible value set fails with a plain-language diagnostic.

Variant resolution must preserve semantic identity. Placeholder resolution performs typed substitution
only and cannot select Parts, create relationships, or apply engineering rules.

### FR-11: Publish Package Lineage

Every authored item that uses copied geometry must publish an inspectable provenance view connecting:

```text
governed source locator and digest
  -> admitted Package Item
  -> Function Representation Binding
  -> Canonical Scene occurrence
```

Package provenance records a normalized logical source locator, source digest, license, and admitted item
digest. Usage trace records binding and scene occurrence identities. A composed read-only provenance view
must answer where any visible Symbol came from without relying on filenames alone. Machine-local absolute
paths never enter deterministic digests or exports.

### FR-12: Resolve Semantic Package Dependencies

Package resolution must validate declared exported and required capabilities/interfaces. Missing or
incompatible semantic contracts block admission before Function binding or scene publication.

## 6. User Journeys

### UJ-1: Browse Package Items

Engineer opens the project package tree, searches IEC/vendor/community packages, previews a Symbol or
Element, and inspects center, ports, functions, compatible Parts, variants, admission state, provenance,
lineage, and license.

### UJ-2: Insert Element

Engineer inserts a selected Element into the Sheet. Athena creates a typed occurrence and binding,
validates interfaces, compiles, and publishes the updated Canonical Scene.

### UJ-3: Configure Variant

Engineer selects a Macro Variant and supplies Placeholder values. Athena resolves the package contract,
reports incompatibility clearly, and leaves the previous accepted scene unchanged on failure.

### UJ-4: Review Full Page

Engineer opens the rolling-shutter example and sees a coherent page close to the supplied reference,
with real geometry, routes, ports, labels, and traceable source identities.

## 7. Non-Functional Requirements

- Deterministic package resolution, lock materialization, descriptor digest, scene digest, and repeated
  SVG output.
- No direct runtime access to `reference/elements` or `reference/elements_contrib`.
- Package imports must preserve provenance and license metadata.
- Package Item metadata is represented once through the common item envelope.
- Provenance is an inspectable lineage graph, not only one digest or source string.
- Invalid package metadata, unsafe SVG, digest mismatch, and incompatible ports fail closed.
- Variants preserve semantic identity; Placeholders remain typed substitution only.
- Macros remain representation reuse; Parts remain implementation facts; Elements remain representation
  composition.
- Theia library UI remains a client of server/package authority.
- Canonical Scene remains renderer-neutral; Konva remains disposable.
- All M45 proof artifacts remain under `_bmad-output/implementation-artifacts/m45/`.
- Production source contains no demo-only package converters or screenshot-only classes.

## 8. Out Of Scope

- Remote package registry, marketplace, authentication, or publishing service.
- Full EPLAN project/file/database compatibility.
- AI-generated engineering Patterns.
- Automatic creation of engineering relationships from a graphic Macro.
- PDF/BOM/report production.
- 3D Macro authoring.
- Universal CAD-scale performance claims.

## 9. Golden Acceptance

M45 is complete only when one local-package example proves:

```text
reference/elements + reference/elements_contrib used only during fixture authoring
        -> curated SVG copy + directly authored Athena package metadata
        -> direct project-local packages
        -> athena.lock
        -> Symbol / Element / Part resolution
        -> Function bindings
        -> Macro Variant / Placeholder resolution
        -> Canonical Scene
        -> editable Theia page
        -> deterministic SVG/PNG evidence
```

The golden screenshots must show a complete rolling-shutter page visually similar to the supplied
reference. Evidence must also show package metadata, port anchors, part bindings, source traces, and
reopen stability.

## 10. Success Metrics

- One active M45 project resolves at least three direct local packages: IEC, vendor, and community.
- At least ten real Symbol/Element definitions are admitted with metadata and SVG resources.
- At least five Parts resolve to compatible Elements.
- At least one composite Element, three Macro Variants, and one Placeholder value set compile.
- Every admitted item uses the common Package Item envelope and explicit admission state.
- One Function proves two projection bindings without changing Function identity.
- One visible occurrence traces through binding and Package Item to copied SVG source provenance.
- Full rolling-shutter page contains at least ten package-backed occurrences and visible routes.
- Invalid package item, unsafe SVG, invalid port, invalid Part, and missing Placeholder tests fail closed.
- Repeated compile/export produces equal scene and SVG digests.
