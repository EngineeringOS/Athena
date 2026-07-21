# M30 Addendum: Representation Boundary And QET Gap Rationale

## Core Decision

M30 must not treat QElectroTech `.elmt` files as `.athena` source. A `.elmt` file is a
presentation/vector element definition with QET runtime semantics:

```text
definition metadata
uuid
translations
primitive geometry
style strings
dynamic text bindings
terminal locations
hotspot and element bounds
QET link_type behavior
```

That is not semantic engineering truth. It is a visual dialect.

## Correct Boundary

```text
.athena
  semantic engineering source

semantic kernel
  engineering facts needed by every projection

representation library
  Athena-owned visual symbol definitions

representation policy
  chooses visual dialect, symbol family, variant, and occurrence role for a projection context

binding compiler
  maps semantic/projection facts to representation occurrences

renderer
  paints Presentation IR
```

Kernel owns representation-relevant semantics:

- canonical ids,
- device type and functional role,
- ports and terminal numbers,
- port direction,
- signal or medium,
- relationship capability,
- source provenance,
- projection occurrence context.

Kernel must not own:

- polygon,
- line,
- arc,
- hotspot,
- SVG path,
- QET `link_type`,
- IEC drawing geometry,
- frontend hitbox behavior,
- visual style tokens.

## Why The Current UI Looks Like A Toy

The renderer has been forced to infer visuals from weak facts. It receives "device", "port", and
"connection", then compensates with generic boxes, wrappers, hitboxes, labels, and graph-like
spacing. Professional schematics require a real representation vocabulary and a composition
discipline. Without those, every visual fix becomes a frontend patch.

## What QET Teaches Us

QET `.elmt` proves that professional symbols need:

- primitive geometry,
- anchor points,
- terminal orientation,
- dynamic label slots,
- styles,
- variants,
- bounds and hotspots,
- metadata and translation.

Athena should build its own equivalent representation layer, but the Athena version must bind to
semantic identities and projection facts rather than become an independent drawing database.

## Representation Definition vs Occurrence

M30 must keep reusable definitions separate from per-projection usage.

```text
Representation Definition
  reusable asset such as IEC motor, contact, terminal, or folio reference symbol

Representation Occurrence
  one semantic/projection use of that definition with bound labels, terminals, references, and
  composition intent membership
```

This prevents the common CAD trap:

```text
one semantic device = one drawn box
```

A single semantic device may have multiple representation occurrences:

- coil occurrence,
- contact occurrence,
- terminal-strip occurrence,
- folio reference occurrence,
- report or maintenance occurrence later.

## Representation Policy

Binding should not hard-code visual choices. Representation Policy IR decides which family,
variant, and occurrence role applies.

Example policy shape:

```text
when:
  projection = electrical-schematic
  semantic role = power-load
  standard profile = athena-industrial-control-v0

choose:
  symbol = iec.motor.compact
  occurrence role = load-symbol
  variant = compact
```

The Binding Compiler consumes policy and semantic facts, then emits Representation Occurrence IR.
Policy is not `.athena` source syntax in M30.

## Semantic Reference Occurrences

Professional electrical drawings depend heavily on reference semantics. M30 must explicitly support
representation occurrences for references such as:

- coil to contact,
- device to terminal strip,
- component to location,
- folio continuation and previous/next sheet reference.

These references are semantic/projection facts first. The arrow, label, or cross-reference mark is
only the visual occurrence.

## Composition Intent, Not CAD Geometry

M30 should use "Schematic Composition Intent Compiler" wording. The compiler produces planning
facts:

```text
lane membership
column membership
alignment group
label band
route channel
reference zone
```

It should not directly become a CAD geometry database. Final coordinates are downstream layout and
presentation facts.

## Representation Lifecycle

Representation definitions need version and lifecycle metadata because engineering projects live
for years.

Minimum M30 lifecycle fields:

```text
version
status = active | deprecated | superseded
supersededBy?
migrationHint?
provenance
```

M30 does not need migration behavior beyond validating and transporting the metadata.

## Deferred Offline Converter

A future converter may exist:

```text
QET .elmt
  -> QET Element AST
  -> normalization diagnostics
  -> Athena Representation Definition IR candidate
  -> Athena-owned symbol pack asset
```

It must not become:

```text
QET .elmt -> .athena source
```

It must not become:

```text
.athena source references QET file path
```

It must not become:

```text
runtime renderer loads QET assets directly
```

## M30 Demo Bar

The demo bar is not "can render symbols." It is:

```text
Can Athena open one industrial control-sheet sample that a customer recognizes as an engineering
schematic rather than a graph demo?
```

The proof needs:

- native symbols,
- compact label slots,
- transparent normal chrome,
- no large wrappers,
- no hard-coded viewBox,
- no off-screen duplicate elements,
- no repeated text nodes,
- composition patterns,
- structured proof payloads,
- screenshot guard.
