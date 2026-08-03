# M37 PRD Addendum

## Non-Negotiable Source Principle

Athena is semantic driven. All professional drawings and layouts must lower from the same Athena
source and compiler-owned semantic facts:

```text
Athena source
  -> semantic facts
  -> Engineering Connectivity Contract / Connection Intent / physical / representation facts
  -> projection policy
  -> presentation and graphic IR
  -> renderer
```

Cabinet, professional connection drawing, schematic, documentation, and future projections may use
different Projection Policies and Presentation Profiles, but they cannot create separate engineering
truth.

Forbidden:

```text
Cabinet source truth
Schematic source truth
Renderer source truth
SVG source truth
XML source truth
```

Allowed:

```text
one Athena source truth
many typed projections
```

## External Standards Boundary

M37 should learn from industrial standards without importing their authority models into Athena.

- AutomationML is relevant as an engineering data exchange ecosystem, especially because it is based
  on IEC 62714 and CAEX-style plant/component exchange. For M37, it is mapping evidence or future
  importer input only.
- ECLASS-like classification is relevant for product classification and property references. For
  M37, it can identify external classification evidence but must not define Athena Ports or
  Interfaces.
- IEC references are useful for naming and validation evidence. They do not replace Athena's typed
  language.

Rule:

```text
external standard reference
    -> Athena External Evidence Mapping
    -> compiler diagnostics/proof
```

Not:

```text
external XML/catalog
    -> runtime product truth
```

## Engineering Connectivity Contract Boundary

M37 should introduce only the minimum Engineering Connectivity Contract required for routing and
explainability. It answers "how can this governed engineering object participate in topology?" It
does not answer "what purchasable product is this?"

This is not a new parallel contract. M37 directly evolves and renames the M36 Connectable Entity
Contract and its active consumers. Because Athena is pre-public, the old contract name and any
adapter or compatibility path are removed in the same change.

```text
Engineering Connectivity Contract
  -> Interfaces
  -> Ports
  -> compatibility predicates
  -> Connection Intent hooks
  -> physical/representation bindings
  -> External Evidence Mapping references
```

M37 should not introduce:

```text
manufacturer lifecycle
article replacement
BOM/procurement ownership
simulation model
datasheet lifecycle
AI replacement rules
```

Those belong to a later Engineering Component System milestone.

## Connection Intent Boundary

Connection topology says what is connected. Connection Intent says how the connection should be
treated by engineering validation, routing, projection, and future AI reasoning.

Allowed in M37:

```text
intent class
priority
separation requirement
preferred drawing region
preferred physical channel
route label policy
```

Not allowed in M37:

```text
final geometry
AI-generated layout authority
product replacement logic
vendor catalog lifecycle
```

Intent resolution order is fixed:

```text
Connection
  -> route group
  -> Interface default
  -> selected Drawing Standard Profile default
```

The first applicable declaration wins. Conflicting declarations at the same level fail validation.
Endpoint types, geometry, and renderer state never infer Connection Intent. In particular, the
current endpoint-derived `ElectricalConnectionIntentClassifier` path must be replaced in the active
professional drawing compiler rather than retained as fallback behavior.

## Route Quality Ownership

The compiler owns the Route Quality Policy schema, hard-reject vocabulary, deterministic scoring,
and validation. A typed Drawing Standard Profile owns concrete weights and presentation defaults.
Projection Policy selects that profile. Project source owns Connection Intent and cannot weaken hard
rejects.

```text
compiler schema + hard rejects
  -> typed profile values
  -> Projection Policy selection
  -> transient planner constraints
  -> candidate validation and scoring
  -> accepted RouteFacts
```

The active drawing compiler must not inject an untraceable fixed list of route constraints. Existing
hardcoded constraints are refactored into compiler-owned hard rules or source/profile-traceable
policy facts.

## SVG Bridge Rule

SVG can point. Athena defines.

Allowed:

```xml
<path id="terminal-l1" data-athena-ref="anchor:drive.power.l1" />
```

Athena source owns:

```text
anchor drive.power.l1
port L1
interface powerInput
signal electrical.ac
direction input
classification/reference evidence
```

Disallowed:

```xml
data-athena-port="L1"
data-athena-signal="electrical.ac"
data-athena-direction="input"
```

Those attributes would make SVG a second language and must fail validation.

## ELK Boundary

ELK concepts worth learning from:

- ports and port constraints;
- layered graph layout;
- orthogonal edge routing;
- edge labels;
- crossing reduction;
- compound nodes.

Athena boundary:

```text
Athena Engineering IR
  -> transient Layout Graph
  -> Planner SPI
  -> planner proposal
  -> Athena validation
  -> PlacementFacts / RouteFacts / RouteLabelFacts
```

Planner proposals are candidates. Athena validation decides.

M37 proves the Athena-native deterministic planner only. ELK remains a studied adapter option until
the Planner SPI conformance suite and M37 professional route gates are complete.

## M37 Visual Quality Checklist

The M37 professional connection drawing E2E should check structure, not pixel perfection:

- no center-anchor fallback;
- no route/body intersections;
- disciplined line endpoints attached to anchors, terminals, buses, junctions, or sheet references;
- route lanes, wire columns, or equivalent drawing regions visible where applicable;
- consistent line weight, style, and color by Connection Presentation Class;
- junctions and crossings rendered from semantic facts;
- labels do not overlap component bodies;
- sheet frame, coordinate grid, and title block render;
- terminal groups and reference designations remain readable at desktop viewports;
- degraded routes produce diagnostics;
- screenshot evidence exists for desktop and narrow viewports;
- every route has source-to-route proof.

The valid proof has zero loose endpoints, center/fallback anchors, route/body intersections,
ambiguous crossings, label/body overlaps, label/title-block overlaps, and unclassified visible
routes. Invalid fixtures separately prove blocking diagnostics. Screenshot comparison supports human
review but does not override these structural gates.

Reference composition:

```text
draft/screenshort/equipement_d'un_volet_roulant.png
```

This is a visual and domain reference. Athena does not copy QET persistence, XML, editor, or runtime
architecture and does not claim pixel-perfect parity.

## Direct Refactor Targets

M37 planning must account for current production behavior that conflicts with the target authority
model:

- evolve and rename M36 `ConnectableEntityContract` types in place;
- replace endpoint-derived connection intent classification in the active professional drawing
  compilation path;
- replace untraceable hardcoded drawing route constraints with compiler hard rules and selected
  typed profile facts;
- replace hardcoded schematic projection context and control-drawing view selection with typed
  Projection Policy input;
- derive clearance, fallback-absence, renderer-purity, endpoint, crossing, and label proof fields
  from compiled facts and diagnostics; production proof booleans cannot be success constants;
- extend existing professional drawing and sheet composition flows instead of creating a parallel
  renderer or presentation IR;
- keep all milestone proof helpers, samples, and screenshot assertions outside production
  `src/main`.

## Deferred To Later Milestones

- Full Engineering Component System.
- AML importer/exporter.
- ECLASS/IEC catalog resolver.
- Remote package registry and resource URI runtime.
- AI auto-layout or AI component replacement.
- Multi-view professional parity.

## External References

- AutomationML: https://www.automationml.org/about-automationml/automationml/
- AutomationML specifications: https://www.automationml.org/about-automationml/specifications/
- Eclipse Layout Kernel layered algorithm: https://eclipse.dev/elk/reference/algorithms/org-eclipse-elk-layered.html
- Eclipse Layout Kernel edge routing option: https://eclipse.dev/elk/reference/options/org-eclipse-elk-edgeRouting.html
- ECLASS standard: https://eclass.eu/en/eclass-standard
- ECLASS conceptual data model: https://eclass.eu/support/technical-specification/data-model/conceptual-data-model
