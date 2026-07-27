# Athena M33 PRD Addendum

## Standards Reference Posture

M33 uses standards as design anchors, not as full compliance claims.

- IEC 60617: graphical symbols for electrotechnical diagrams.
- IEC 61082: preparation of documents used in electrotechnology.
- IEC 81346: reference designation principles for systems and components.
- IEC 81714: design principles for graphical symbols in technical documentation.

M33 acceptance language must say "IEC-style" or "IEC-referenced" unless full compliance is
separately audited.

## Review Integration: Why M33 Is Generic Underneath

M33 keeps an IEC-style electrical demo because it is the fastest customer-visible proof, but the
milestone must not make Athena an ECAD-specific platform. The durable layer is:

```text
Engineering Drawing Symbol Anatomy
  -> Graphic Primitive IR
  -> Renderer Adapter
  -> Workbench Surface
```

IEC symbols are one representation package. Later hydraulic, P&ID, robotics, SCADA, cabinet,
network, and mechanical packages should reuse the same drawing engine contracts.

## Rendering Engine vs Workbench UX

M33 contains two different work streams and they must not be confused:

```text
Drawing engine:
  symbol anatomy
  Graphic Primitive IR
  sheet composition
  route anchors
  bounds
  renderer adapters

Workbench UX:
  toolbar
  sheet selector
  Create Device
  cabinet/documentation/debug visibility
  inspection controls
```

The renderer must not know toolbar state. The Workbench must not invent drawing meaning.

## Graphic Primitive IR Rationale

M33 introduces Graphic Primitive IR v0 so representation output does not directly become SVG.

```text
Representation Descriptor
  -> Graphic Primitive IR
  -> SVG / Canvas / PDF / Skia / WebGPU later
```

M33 implements SVG first because it is the current product surface. The architecture must still
make SVG a renderer adapter, not the drawing authority.

## Presentation Profile Promotion

Presentation Profile remains first-class policy:

```text
IEC
ANSI
DIN
GB
ABB
Customer
Dark
Print
Maintenance
Training
```

Profiles are not renderer options, source syntax, or symbol packages. They are policy inputs to
binding and drawing selection.

## Why M33 Prioritizes Renderer And IEC Elements

M32 established package management and package-backed representation resolution, but that does not
automatically make Athena look professional. Customers judge the canvas first. If the Graph View
still shows generic boxes, unstable controls, duplicate labels, large viewBox, and visible wrapper
borders, package architecture is invisible.

M33 therefore prioritizes visible output:

```text
package-backed descriptor
  -> symbol anatomy
  -> renderer primitive scene
  -> sheet composition
  -> professional Graph View
```

## Element Management Model

Standard symbols and vendor elements are managed as packages:

```text
Engineering Package
  owns product/concept facts

Presentation Profile
  owns visual policy and standard/customer choice

Representation Package
  owns symbol descriptors, anchors, slots, resources, and visual variants
```

`.athena` does not name SVG files, QET paths, coordinates, symbols, or visual variants.

## Vendor Element Integration

Vendor engineering package example:

```text
com.vendor.product.drive.engineering
  product facts
  terminals
  ratings
  parameters
  lifecycle
  datasheet references
```

Vendor or standard representation package example:

```text
org.athena.standard.iec60617.representation
  symbol descriptors
  primitive definitions
  anchors
  label slots
  style tokens
```

Binding manifest bridges them:

```text
FrequencyDrive concept
  -> presentation profile iec-compact
  -> representation descriptor iec.drive.compact
```

## Rejected M33 Directions

### Package Catalog UI First

Rejected for M33. It improves platform tooling but does not solve toy canvas perception.

### Full QET Importer

Rejected for M33. QET `.elmt` is useful research but has its own runtime semantics. Importer work
must target Athena representation descriptors later, not `.athena`.

### Symbol Quantity Push

Rejected for M33. Large weak libraries hide defects. M33 needs ten high-quality symbols with proof.

### New `.athena` Visual Syntax

Rejected. Visual choice remains package/profile policy. Semantic source stays clean.

## M33 Proof Discipline

Every visual claim needs at least one machine-checkable proof:

- symbol anatomy proof
- descriptor validation proof
- primitive scene proof
- route anchor proof
- sheet bounds proof
- viewBox proof
- DOM no-wrapper/no-duplicate proof
- screenshot proof

Screenshot evidence supports human review but never replaces structured proof.

## M34 Direction After M33

M33 should close first-generation platform rendering. After M33, the highest-value direction should
move upward into knowledge, not continue renderer polishing unless M33 proof fails.

Likely M34 theme:

```text
Engineering Knowledge Runtime
```

or:

```text
Engineering Standards Platform
```

Target capabilities:

- richer engineering package facts
- standards constraints
- validation rules
- BOM knowledge
- datasheet/manual references
- PLC mapping rules
- maintenance knowledge
- AI context hooks

This keeps Athena moving toward EngineeringOS instead of another schematic editor.
