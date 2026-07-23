# M32 Addendum: Engineering Package Platform Rationale

## Selected Direction

M32 selects **Engineering Package Platform Foundation**.

Alternatives considered:

| Direction | Decision | Reason |
| --- | --- | --- |
| Semantic Agent Runtime | Deferred | Agents should consume stable package, representation, and authoring contracts rather than compensate for missing ecosystem structure. |
| Draw more native symbols | Rejected as milestone thesis | More drawings do not fix the boundary between engineering knowledge, presentation policy, representation descriptors, and renderer resources. |
| QET `.elmt` importer | Deferred | QET is useful as reference/import research, but runtime import would couple Athena to a foreign symbol model before its own descriptor language is stable. |
| Engineering Package Platform | Selected | Establishes the extension mechanism needed for catalogs, profiles, representation packages, standards, customers, demos, and future agents. |

## Core Separation

M32 formalizes three language families and four package/policy boundaries:

```text
Engineering Language
  "What the engineering is"

Presentation Policy Language
  "How engineering should appear under a context or standard"

Representation Language
  "Which descriptors and resource contracts can express that appearance"

Rendering Language
  "How pixels, vectors, pages, or 3D media are drawn"
```

The platform boundary is:

```text
Engineering Package != Presentation Profile != Representation Package != Graphic Resource
```

An Engineering Package may define product parameters, defaults, semantic capabilities, validation
rules, lifecycle metadata, and documentation. It must not define drawing resources, coordinates,
anchors, style, or viewBox.

A Presentation Profile may define IEC, ANSI, customer, print, maintenance, training, compact, or
theme policy. It owns neither engineering truth nor Graphic Resources.

A Representation Package may define descriptors, anchors, label slots, hotspots, variants, style
tokens, and Graphic Resource references. It must not define engineering truth or source mutation
rules.

A Graphic Resource is a renderer resource like a font file. Athena may inspect it for rendering
compatibility, but it does not become a semantic model.

## Engineering Catalog

Engineering Packages should scale toward catalogs rather than single symbol-like assets:

```text
groupId: com.athena.example.drives
artifactId: compact-vfd-catalog
version: 1.0.0

contains:
  product definitions
  concept definitions
  templates
  defaults
  rules
  parameter schema
  lifecycle metadata
  docs and datasheet references
```

Representation is the first package family M32 exercises, but the platform should later host
validation, simulation, documentation, report, manufacturing, standards, and AI knowledge packages
without changing the core package model.

## Presentation Profile

Presentation Profile is the accepted missing abstraction between engineering knowledge and graphic
resources:

```text
Engineering Package
  -> Presentation Profile
  -> Binding Resolver
  -> Representation Package
```

Examples:

```text
IEC
ANSI
Customer A
Customer B
Compact
Print
Maintenance
Training
Dark theme
```

This makes it possible for the same engineering product to appear differently under standards,
customer preferences, operating context, or output medium without changing `.athena` source.

## Descriptor Over Raw Resource

The most important representation-side abstraction is the Representation Descriptor:

```yaml
representation:
  id: iec.motor.standard
  version: 1.0.0
  resources:
    default:
      kind: graphic-resource
      path: motor.svg
  bounds:
    width: 80
    height: 48
  anchors:
    power_u:
      resourceAnchor: a
      side: top
    power_v:
      resourceAnchor: b
      side: top
  labelSlots:
    tag:
      role: device_tag
    model:
      role: model
  variants:
    compact: {}
    detailed: {}
```

The resource may be complex. The descriptor is the controlled contract the platform validates and
binds.

## Binding Manifest And Binding Resolver

Engineering, profile, and representation packages are separate but must be resolvable together.
The Binding Manifest is the compatibility bridge:

```yaml
package:
  id: com.example.engineering.drive.acs380
  concept: FrequencyDrive

profiles:
  default: iec60617
  alternatives:
    - ansi
    - customer-demo-compact

representations:
  default: com.example.representation.drive.iec
  alternatives:
    - com.example.representation.drive.compact
    - com.customer.representation.drive.maintenance
```

The Binding Resolver is a compiler stage:

```text
Semantic Compiler
  -> Engineering Resolver
  -> Binding Resolver
  -> Representation Resolver
  -> Presentation Compiler
  -> Renderer
```

This is intentionally three resolvers, not one:

| Resolver | Owns | Does not own |
| --- | --- | --- |
| Engineering Resolver | engineering package/catalog selection and validation | presentation policy or graphic resources |
| Binding Resolver | profile, manifest, policy, descriptor choice, anchor/label mapping | semantic truth or rendering |
| Representation Resolver | representation package and Graphic Resource descriptor validation | product facts |

The separation matters for incremental compilation: a product parameter change, profile change, and
resource file change should invalidate different parts of the pipeline.

## Why M32 Is Not New `.athena` Syntax

M32 package and profile selection belongs to project configuration, resolver policy, or active
presentation profile. It should not appear as visual declarations inside semantic source:

```athena
device DriveA {
  type FrequencyDrive
  model "ACS380"
}
```

The source describes the engineering object. Engineering Resolver, Presentation Profile, Binding
Resolver, and Representation Resolver decide which package and descriptor apply.

## QET Position

QElectroTech `.elmt` files are valuable research input because they show real symbol complexity,
localization, anchors, text slots, and drawing primitives. They are not simple SVG and should not
be converted directly into `.athena` semantic files.

Deferred path:

```text
QET .elmt
  -> offline importer research
  -> Athena Representation Descriptor candidate
  -> human/package validation
  -> Athena-owned Representation Package
```

Forbidden path:

```text
QET .elmt
  -> runtime dependency
  -> .athena source
  -> semantic authority
```

## Sample Asset Rule

M32 should demonstrate vendor-like product depth without shipping proprietary vendor assets unless
licenses are explicit. Sample package names may be synthetic while carrying realistic product
roles:

```text
com.athena.example.engineering.drive.compact-vfd
com.athena.example.engineering.controller.compact-plc
com.athena.example.engineering.relay.interface-relay
```

The demo can still prove the intended customer outcome: professional representation, distinct
engineering packages, presentation profiles, representation packages, policy-driven variants, and
source purity.

## Deferred Beyond M32

| Deferred | Reason |
| --- | --- |
| Full Engineering Concept Library | M32 proves package mechanics with limited sample concepts; M33 can expand semantic catalog depth. |
| Standards/domain knowledge engine | M32 supports policy tags but does not implement IEC/ANSI compliance engines. |
| Semantic Agent Runtime | Package and authoring contracts should exist before AI plans consume them. |
| Internet registry and package publishing | Requires security, trust, version lifecycle, and update policy. |
| Full QET importer | Requires descriptor coverage and legal/product decisions after descriptor v0 stabilizes. |
| Symbol editor | Data-driven package validation should exist before editing UX. |
