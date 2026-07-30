---
title: Athena M35 PRD Addendum
status: final
created: '2026-07-27'
updated: '2026-07-27'
---

# M35 Addendum

## Why M35 Is Not More Rendering Polish

The M34 screenshot gap is not mainly a CSS problem. It is a missing physical layout compiler problem.
The renderer can only look professional when upstream facts say:

```text
this is an enclosure
this rail contains these mounted occurrences
this terminal strip owns these terminals
this duct/channel carries these conductors
this element occurrence binds to this package source and resource
```

M35 therefore fixes the physical installation and package substrate first. The renderer remains
paint-only. Cabinet is the first projection, not a new semantic world.

```text
Engineering Semantic Model + governed installation constraints
  -> Physical Installation Compiler
  -> PhysicalInstallationIR v0

Resolved representation package graph
  -> Representation Binding

PhysicalInstallationIR v0 + bound representation occurrences
  -> Cabinet Composition Compiler
  -> Graphic Primitive IR
  -> Theia Renderer
```

## Package Hierarchy Rule

M35 adopts Java-style package discipline for governed `.athena` files:

```text
src/com/engineeringood/m35/cabinet/01-control-cabinet.athena
```

```athena
package com.engineeringood.m35.cabinet
```

The same rule applies inside representation package roots:

```text
packages/representation/com/engineeringood/standard/iec/control/coil.athena
packages/representation/com/vendor/acme/drive/acs380/drive.athena
packages/representation/com/vendor/acme/drive/acs380/drive-front.svg
```

This makes source discovery predictable for humans, AI authors, package tooling, and future dependency
resolution. It does not make namespace equal to product identity. Manufacturer, article number,
revision, lifecycle, datasheet, and engineering parameters belong to the future Engineering
Component System, not to filesystem paths.

For each governed source root, the normalized source-root-relative parent directory must equal the
lowercase Athena namespace segments exactly. Default-package source, case-only alternatives, Unicode
normalization collisions, and mismatched directory segments fail admission.

M35 uses the existing `athena.yaml` repository/package descriptor for package coordinate,
source-root, and dependency intent. Governed `.athena` source owns resource declarations and
exported definitions. Compiler-generated `athena.lock` and proof own resolved package/resource
evidence. M35 does not introduce `package.athena`, duplicated resource lists, another manifest
format, or another package resolver.

`RepositoryManifest` is extended as the one typed owner of authored package identity/coordinate,
package/source roots, and dependency intent. Derived `ResolvedPackageCoordinate` belongs to the
resolved graph/snapshot and `RepositoryLockV2`, never the manifest. Compiler, package admission, and
LSP consume one loader result; duplicate YAML line scanners
are removed. `RepositoryLockV2` replaces the unreleased v1 schema without a compatibility reader.
Validate mode is read-only and rejects missing, stale, or incompatible lock state. Explicit update
mode writes canonical lock bytes through a same-directory temporary file and atomic replacement,
then revalidates before compilation output is accepted. Lock bytes never feed resource or package
snapshot identity.

## Package-Local Resource Resolution

Resource references should behave like resources beside a Java source unit inside one admitted
dependency. A declared path resolves only from the directory of the declaring Athena source unit
inside its admitted package snapshot:

```athena
package com.vendor.acme.drive.acs380

element acs380_panel_element {
  identity "com.vendor.acme.drive.acs380.panel"
  version "1.0.0"

  resource acs380PanelSvg {
    kind svg
    path "./acs380-panel.svg"
  }

  graphic svg resource acs380PanelSvg
}
```

The compiler resolves `acs380PanelSvg` from the owning package context, stages the resource into an
immutable package snapshot, hashes it, records lock evidence, and never lets renderer/runtime scan the
original source path.

Resource ids are lexical to one `.athena` source unit. `graphic svg resource acs380PanelSvg` can
resolve only a declaration in that same file. Duplicate ids in one file fail; equal ids in different
files are independent. Dependencies export compiled Symbol/Element definitions, not raw resources,
and no source unit may reach into another source unit or package by resource id.

Direct physical paths never cross package boundaries. A dependency resource is consumed only through
an exported logical definition resolved inside the dependency's own admitted snapshot.
Workspace root, repository root, process working directory, and renderer-relative resolution are
forbidden.

Bad pattern to eliminate:

```kotlin
VALID_SOURCE.replaceFirst("./vendor-drive.svg", "../vendor-drive.svg")
```

That kind of path mutation proves the resource model is too weak.

## Complex SVG With Athena-Owned Metadata

M35 keeps the mixed model from M34, but proves it on a realistic vendor element:

```athena
package com.vendor.acme.drive.acs380

element acs380_cabinet_front {
  identity "com.vendor.acme.drive.acs380.cabinet-front"
  version "1.0.0"

  resource frontSvg {
    kind svg
    path "./acs380-cabinet-front.svg"
  }

  graphic svg resource frontSvg

  anchor line {
    geometryRef svg.L1
    point (42, 0)
    role terminal
    direction in
    signal Power
  }

  anchor load {
    geometryRef svg.T1
    point (42, 180)
    role terminal
    direction out
    signal Power
  }

  export label deviceTag from svg.deviceTag
}
```

SVG may expose selected geometry nodes only as referenceable geometry:

```svg
<g id="L1" data-athena-geometry-ref="L1">
  <circle cx="42" cy="8" r="3"/>
</g>
```

The important boundary:

- Athena owns element identity/version/export/binding/anchors/roles/directions/signals.
- SVG owns geometry and local geometry-reference hints.
- Project `.athena` owns actual device ports and connections.
- Renderer owns none of this.

SVG geometry hints are optional and non-semantic. Removing them cannot change the semantic model. If
an Athena representation declaration references a geometry node that is no longer available, the
representation package fails admission; the compiler does not infer a replacement meaning.

M35 removes prior `data-athena-anchor`, `data-athena-role`, `data-athena-directions`, and
`data-athena-signals` support from active package admission. No runtime translation or compatibility
authority remains.

## Cabinet Physical Composition Model

M35 should add the minimum typed composition facts needed for a professional Cabinet:

```text
PhysicalInstallationIR v0
  InstallationSpace
    Enclosure
      MountingSurface
        Rail
      TerminalGroup
      Duct / RouteChannel
      MountedOccurrence -> mount target + canonical semantic subject
      PhysicalRouteIntent
```

Enclosure is the only v0 physical container. MountingSurface, Duct, and TerminalGroup belong to an
Enclosure. MountingSurface and TerminalGroup declare bounds and accepted mounting types;
TerminalGroup also declares an ordering orientation. Rail belongs to a MountingSurface. Every
RouteChannel belongs to exactly one Duct, has an authored duct-interior-local rectangle, and must fit
the wall-inset interior. Mounted occurrences target only MountingSurface, Rail, or TerminalGroup.
Terminals are ordinary MountedOccurrences; their TerminalGroup owns an ordered key list sorted by
along-axis placement, cross-axis placement, then occurrence key. Duct and RouteChannel are never
mount targets. M35 uses occurrence-owned four-sided clearance and defers shared authored keep-out
zones.

This is not a CAD constraint solver. It is a deterministic compiler from authored installation intent and
semantic facts into physical composition facts. The required separation is:

```text
physical intent
  -> placement constraints
  -> compiled physical facts
  -> renderer coordinates
```

Renderer coordinates are output, not source truth.

Minimum output facts:

- enclosure bounds;
- rail id, bounds, orientation, mounting lane;
- duct/channel id, bounds, routing lane;
- mounted occurrence id, canonical semantic subject, installation occurrence key, container id,
  mount-target id, position, size, and orientation;
- placement constraints: width, height, mounting type, clearance, container compatibility;
- terminal group id and terminal ordering;
- physical route intent id, engineering connection id, and ordered route-channel ids;
- source provenance for every occurrence.

Cabinet composition separately joins each installation occurrence to one resolved representation,
derives labels and drawing bounds, and emits final `CabinetRouteFact` endpoint bindings and segments.
Final terminal-anchor coordinates and route segments are not owned by PhysicalInstallationIR.

MountingSurface, Duct, and TerminalGroup `at` coordinates are enclosure-interior-local; Rail is
MountingSurface-local; RouteChannel is local to the Duct wall-inset interior; and MountedOccurrence
is mount-target-local. MountingSurface and TerminalGroup use upper-left local origins. Horizontal
rail local +X is enclosure +X and local +Y is enclosure +Y. Vertical rail
local +X is enclosure +Y and local +Y is enclosure -X. Every basis has determinant `+1`, so target
frames cannot mirror representation geometry. The compiler maps these physical millimetre
frames into drawing coordinates; source never stores pixels.

These constraints are enough for the M35 Cabinet proof. Full product dimensions, manufacturer
catalog identity, lifecycle, and validation rules stay deferred to the Engineering Component System.

## Physical Constraint Evaluation v0

M35 performs deterministic evaluation only:

```text
fit validation
collision detection
clearance checking
container compatibility
```

The selected orientation must belong to the resolved allowed set. In-plane orientation does not
rotate depth. V0 enclosure usable depth equals its authored depth, with no Z offset or depth zones,
and occurrence depth must fit that value. The clearance-inflated footprint
must always fit the enclosure. On MountingSurface or TerminalGroup it must also fit target bounds and
its mounting type must be in the target's accepted set. On Rail, normal placement is exactly zero;
the oriented along-axis span plus leading/trailing clearance must fit rail length; and mounting type
must equal the rail mounting type. Rail normal extent is validated against the enclosure rather than
the zero-width rail line. MountingSurface, Duct, and TerminalGroup bounds must fit the enclosure;
each Rail's complete oriented interval must fit its MountingSurface.

M35 does not implement:

```text
general constraint solving
placement optimization
automatic placement
Auto Layout AI
```

## Deterministic Physical Routing v0

Route intent names an ordered channel sequence; the compiler does not discover another path.
For channel cross-axis span `S`, margin `M`, and lane count `N`, require `N > 0` and
`U = S - 2M > 0`. Zero-based lane `i` has centre offset
`M + ((2i + 1) * U) / (2N)` from the minimum cross-axis edge. Derived coordinates remain reduced
exact rational millimetres until drawing transformation. Connections using a channel sort by stable
connection id and take lane indices in order up to capacity `N`; horizontal lanes allocate
top-to-bottom and vertical lanes left-to-right.

Two consecutive channels are adjacent only when they belong to the same Duct, their interiors are
disjoint, and their boundaries share exactly one axis-aligned segment that remains positive after
trimming `max(marginA, marginB)` from both ends. The exact midpoint of that segment is the sole
passable boundary. Cross-duct, corner-only, overlapping, gapped, or multiply intersecting transitions
fail. Endpoint stubs connect the transformed anchor to the nearest point on the first or last lane.
Orthogonal bends are horizontal then vertical; exact ties choose lower X then lower Y. Invalid
adjacency, capacity, duct containment, anchor compatibility, or body intersection fails composition.

## Normative M35 Source Surface

The public v0 surface is intentionally narrow. `installation` is a `system` member, its only M35 kind
is the literal `cabinet`, and `route` is an installation member. Connections remain system members.
These are the normative forms; IR and renderer names are not language keywords:

```text
installation cabinet <id> { <installation-member>* }
enclosure <id> size (<length>, <length>, <length>)
surface <id> in <enclosure-id> at (<length>, <length>) size (<length>, <length>)
  accepts [<mounting-type-id> (, <mounting-type-id>)*]
rail <id> on <surface-id> at (<length>, <length>) length <length>
  orientation <horizontal|vertical> mounting <mounting-type-id>
duct <id> in <enclosure-id> at (<length>, <length>) size (<length>, <length>)
  orientation <horizontal|vertical> wall <length>
channel <id> in <duct-id> at (<length>, <length>) size (<length>, <length>)
  orientation <horizontal|vertical> lanes <positive-int> margin <length>
terminal-group <id> in <enclosure-id> at (<length>, <length>) size (<length>, <length>)
  orientation <horizontal|vertical> accepts [<mounting-type-id> (, <mounting-type-id>)*]
mount <semantic-subject-id> as <occurrence-id> on <mount-target-id> at (<length>, <length>) {
  footprint (<length>, <length>, <length>)
  mounting <mounting-type-id>
  orientation <deg0|deg90|deg180|deg270>
  allowed-orientations [<orientation> (, <orientation>)*]
  clearance (<length>, <length>, <length>, <length>)
  compatible-containers [<container-kind-id> (, <container-kind-id>)*]
}
connect <connection-alias> <from-port> -> <to-port>
connect <group-id> { <connection-alias> : <from-port> -> <to-port> ... }
route <same-source-unit-connection-alias> through [<channel-id> (, <channel-id>)*]
```

Accepted proof:

```athena
package com.engineeringood.m35.cabinet

system M35PhysicalCabinet {
  device MainBreakerQF35 {
    type Breaker
    port load {}
  }

  device FieldTerminalXT35 {
    type Terminal
    port line {}
  }

  connect MainPowerConnections {
    MainSupplyConnection: MainBreakerQF35.load -> FieldTerminalXT35.line
  }

  installation cabinet MainCabinet {
    enclosure ENC1 size (800mm, 600mm, 250mm)
    surface Backplate in ENC1 at (20mm, 20mm) size (760mm, 560mm) accepts [din35, screw]
    rail DIN1 on Backplate at (60mm, 120mm) length 680mm orientation horizontal mounting din35
    duct D1 in ENC1 at (30mm, 60mm) size (40mm, 480mm) orientation vertical wall 2mm
    channel CH1 in D1 at (0mm, 0mm) size (36mm, 476mm) orientation vertical lanes 4 margin 4mm
    terminal-group XT1 in ENC1 at (520mm, 420mm) size (180mm, 50mm)
      orientation horizontal accepts [terminal-snap]

    mount MainBreakerQF35 as QF35Mount on DIN1 at (100mm, 0mm) {
      footprint (45mm, 90mm, 70mm)
      mounting din35
      orientation deg0
      allowed-orientations [deg0, deg180]
      clearance (10mm, 5mm, 10mm, 5mm)
      compatible-containers [cabinet]
    }

    mount FieldTerminalXT35 as XT35Mount on XT1 at (10mm, 5mm) {
      footprint (20mm, 40mm, 35mm)
      mounting terminal-snap
      orientation deg0
      allowed-orientations [deg0]
      clearance (2mm, 2mm, 2mm, 2mm)
      compatible-containers [cabinet]
    }

    route MainSupplyConnection through [CH1]
  }
}
```

Engineering connections remain authoritative. Every grouped and ungrouped `connect` requires a
source-unit-unique individual alias. `EngineeringConnectionId` is derived from
`(SourceUnitId, connectionAlias)`; endpoints validate the alias but do not replace it. Each `route`
resolves only an alias in the same SourceUnitId; cross-source references fail. Group names remain
organizational and never merge or identify connections. Existing alias-free grammar/AST forms and
all unreleased fixtures are migrated and deleted in the same language story; there is no
compatibility parser.

## Physical Installation Contract And Future ECS Boundary

M35 defines a narrow consumer contract, not a component package or product language:

```text
PhysicalInstallationContractV0
  PhysicalSize(width, height, depth)       required, positive
  mountingTypeId                          required
  allowedOrientations                     required, non-empty
  clearance(top, right, bottom, left)      required, non-negative
  compatibleContainerKinds                required, non-empty
```

Project values override existing resolved physical-trait values. Width, height, depth, and each
clearance side override independently; orientation and container sets replace atomically. Duplicate
same-precedence values, missing values, and empty required sets fail. Existing unchecked
`PhysicalSize` is accepted only as resolver input, validated field by field, and converted into M35
validated measurements before resolved contracts or physical IR are built.

M35 only needs enough installation input to prevent bad ownership:

- namespace is package/dependency identity, not product identity;
- visual Element is not a product semantic owner;
- physical constraints come from explicit project `.athena` facts or admitted exports from the
  existing resolved package graph, not renderer geometry;
- existing `athena.yaml` records package coordinate, source-root, and dependency intent only;
- governed Athena declarations own exported definitions and resource declarations;
- compiler-generated `athena.lock` records resolved versions, admitted snapshot digest, and
  resource hashes;
- M35 introduces no `Physical Installation Reference Package` and no new product/component package
  type.

A future Engineering Component System may own reusable product identity and provide the same
Physical Installation Contract. M35 does not implement that system.

## Editable Trace

M35 does not need a full graphic editor, but it must define and prove the trace contract:

```text
graphic occurrence clicked
  -> interaction subject id
  -> semantic/representation source provenance
  -> source declaration location
  -> authoring capability
  -> future governed mutation preview
```

This makes future editing possible without corrupting SSOT.

Allowed future edit path:

```text
UI action
  -> SemanticActionIntent
  -> governed capability/command translation
  -> AuthoringIntent
  -> SemanticAuthoringTransaction
  -> AuthoringPreview + AuthoringSourceEditEvidence
  -> compile/lint/proof
  -> accept/reject
  -> rerender
```

Rejected path:

```text
drag SVG node
  -> mutate SVG DOM / Presentation IR / GraphicPrimitive directly
```

The editor contract is valuable because AI agents need the same trace. A future agent should be able
to ask why a mounted occurrence exists, which source declared it, which package provided its visual
resource, and which governed mutation can change it.

## Visual Proof

M35 should use both structured proof and screenshots.

Structured proof catches authority and geometry facts:

- package/path clean;
- resource snapshot clean;
- zero XML runtime authority;
- zero fallback elements;
- zero unbound anchors;
- zero route/body intersections;
- all visible occurrences have source provenance.

Screenshots catch product quality through a structural visual checklist, not pixel-perfect matching:

- enclosure visible;
- mounting surface and rail layout readable;
- terminal group dense and aligned;
- duct and route-channel routing visible and contained;
- mounted controls recognizable and correctly aligned;
- labels readable;
- no clipping, text overflow, unintended overlap, or off-canvas required content;
- every selectable occurrence has complete trace;
- no giant graph cards;
- no wide shallow canvas;
- no floating toy boxes.

Pixel comparison is not the approval authority because industrial drawings can be semantically
correct with different renderer metrics. The hard gate is structural: all required Cabinet concepts
must be present, readable, contained, aligned, and traceable.

## Deferred Engineering Component System

M35 intentionally does not introduce:

```text
component ABB_ACS380 {
  manufacturer ABB
  article ...
  electrical parameters ...
  mounting rules ...
  representations ...
}
```

That belongs after M35. The likely sequence is:

```text
M36 - Engineering Component System
M37 - Industrial Library / Vendor Package Model
M38 - Constraint Solver + Auto Layout
M39 - Engineering AI Agent
```

M35 must avoid putting this product knowledge into `Element`. `Element` remains a reusable visual
representation.

## M35 Story Shape

Recommended story sequence:

1. Package hierarchy lint, descriptor/source/lock authority separation, and sample migration.
2. Package-local resource resolver, explicit resources, `athena.lock` evidence, and immutable resource snapshot.
3. Complex SVG-backed vendor element proof with SVG geometry hints only.
4. Minimal Physical Installation Contract and Physical Constraint Evaluation v0.
5. `PhysicalInstallationIR v0` with Cabinet composition projection facts.
6. Physical connection route channels and terminal routing.
7. Graphic occurrence source trace and editable contract.
8. Product E2E proof, structural screenshot gates, polish, and purge.
