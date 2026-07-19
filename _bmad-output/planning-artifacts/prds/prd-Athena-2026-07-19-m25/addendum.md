# M25 Addendum

## User Feedback Trigger

After M24, the user confirmed that the line string between components looks better. That means the
highest-priority visual complaint moved from route geometry to the next credibility layer: the
components and terminals that routes attach to must look like professional engineering notation.

M25 should therefore focus on engineering representation anatomy, terminal notation, label policy,
and presentation policy. Symbol anatomy is the electrical schematic subset of that broader
representation model. M25 should not restart the router discussion and should not jump to full EPLAN
parity.

## Strategic Position

M25 sits after these milestones:

```text
M14 Component Knowledge
M15 Guided Semantic Authoring
M19 First Professional Schematic Sheet Proof
M20 Engineering Presentation Quality Acceptance
M21 Layout Intelligence Foundation
M22 Governed Layout Optimization And Round Trip Direction
M23 Layout Intent Language Admission
M24 Governed Schematic Routing Fidelity
M25 Engineering Representation And Presentation Policy Foundation
```

M24 made connections more credible. M25 should make the connected subjects themselves more credible.

The product risk is no longer only:

```text
Can Athena understand electrical semantics?
```

or:

```text
Can Athena route a connection without generic graph edges?
```

The M25 risk is:

```text
Can the visible component, terminal, and label notation convince an engineer this is an engineering
sheet?
```

## QElectroTech-Inspired Anatomy

QElectroTech element files provide a useful anatomy vocabulary:

```text
symbol
  terminals
  hotspots
  labels
  dynamic text
  graphics primitives
  metadata
```

Examples in `.elmt` files show:

- `<terminal>` elements with local coordinates, orientation, names, and ids
- `dynamic_text` placeholders bound to element information such as label fields
- line, rectangle, polygon, and text primitives
- element bounds and hotspots used for placement
- metadata and localized names

Athena should not copy QElectroTech's authority direction. QET starts from element drawings that
carry terminals and labels. Athena should start from governed component knowledge and compile that
meaning into presentation facts.

Athena direction:

```text
Component Knowledge
  -> Presentation Policy
  -> Presentation Compiler
  -> Presentation IR
  -> Presentation Anatomy
  -> Symbol Anatomy
  -> Terminal Notation Facts
  -> Label Facts
  -> Sheet Occurrence
  -> Renderer
```

Not:

```text
Drawn Symbol
  -> Inferred Engineering Meaning
```

## Recommended Architecture Direction

M25 should introduce or seed representation and presentation policy contracts above the renderer:

```text
Semantic subject
  -> Component knowledge
  -> Presentation policy profile
  -> Representation resolver
  -> Presentation IR
  -> Symbol family selection
  -> Presentation anatomy facts
  -> Symbol anatomy facts
  -> Terminal notation facts
  -> Label policy facts
  -> Sheet occurrence
  -> Route facts
  -> Renderer
```

The renderer paints only. It should receive all symbol primitives, terminal anchors, terminal labels,
device labels, and route attachments as facts.

## Structural Seed

Possible structure:

```text
kernel/
  representation-model/          # presentation anatomy and representation resolver contracts
  symbol-model/                  # symbol anatomy, primitive graphics, hotspots, terminal points
  presentation-policy-model/     # profile, terminal notation, label policy, fallback policy
  component-model/               # existing component knowledge consumed by symbol composition
  routing-model/                 # M24 route facts that attach to M25 terminal facts
ide/
  theia-frontend/                # paint-only representation and notation rendering
examples/
  m25/
    sample-project/              # openable IDE proof
docs/
  usages/
    m25-proof-usage.md
```

If implementation size stays small, `symbol-model` and `presentation-policy-model` can begin as
cohesive model files rather than over-splitting tiny types.

## Model Seed

Candidate model concepts:

```kotlin
data class SymbolAnatomy(
    val familyId: SymbolFamilyId,
    val representationId: RepresentationId,
    val bounds: SymbolBounds,
    val hotspot: SymbolHotspot,
    val primitives: List<SymbolPrimitive>,
    val terminals: List<SymbolTerminalPoint>,
    val labelAnchors: List<SymbolLabelAnchor>,
)

data class SymbolTerminalPoint(
    val terminalId: TerminalId,
    val portRole: PortRole,
    val semanticRole: TerminalSemanticRole,
    val localPoint: GridPoint,
    val side: PortSide,
    val notation: TerminalNotation,
)

data class PresentationPolicyProfile(
    val profileId: PresentationPolicyProfileId,
    val symbolRules: List<SymbolSelectionRule>,
    val terminalNotationPolicy: TerminalNotationPolicy,
    val labelPolicy: LabelPolicy,
    val fallbackPolicy: SymbolFallbackPolicy,
)

data class SymbolOccurrenceFact(
    val subjectId: CanonicalSubjectId,
    val occurrenceId: OccurrenceId,
    val familyId: SymbolFamilyId,
    val anatomy: SymbolAnatomy,
    val terminalFacts: List<TerminalNotationFact>,
    val labelFacts: List<LabelFact>,
)

data class LabelFact(
    val subjectId: CanonicalSubjectId,
    val occurrenceId: OccurrenceId,
    val role: LabelRole,
    val value: String,
    val anchor: LabelAnchor,
    val sourceIdentity: SourceIdentity?,
)
```

Initial primitive vocabulary can be small:

```text
line
rectangle
polyline
circle
text-anchor
terminal-marker
```

Do not add a large vector grammar unless the first sample needs it.

Terminal modeling should remain separated:

```text
Semantic Port          # engineering connection capability
Physical Terminal      # device or terminal assignment such as X1:14
Presentation Terminal  # visible marker, number, and route anchor
```

This prevents M25 from collapsing terminal meaning into a drawing coordinate.

## Initial Component Subset

M25 should prove a small industrial-control subset using the profile
`athena-industrial-control-v0`.

Accepted sample families:

- PLC/controller
- HMI/operator device
- terminal block
- power supply
- breaker/protection device
- load/actuator

Mandatory acceptance path:

```text
power supply -> PLC/controller -> terminal block -> motor/load
```

HMI/operator and protection device may appear as secondary sample subjects, but they must not expand
M25 into broad standards or library work.

The subset should be enough to show:

- device tags
- terminal numbers
- input/output notation
- route attachment to terminal anchors
- terminal strip readability
- labels bound to source identity
- zero generic fallback symbols in the accepted proof

## QElectroTech Reference Boundary

M25 may include one documentation-only QElectroTech-inspired anatomy mapping example from the local
reference mirror:

```text
reference/qelectrotech-source-mirror/qelectrotech-elements
```

The mapping should show how Athena reads QET-style concepts as vocabulary:

```text
QET terminal -> Athena Presentation Terminal
QET hotspot -> Athena Presentation Hotspot
QET dynamic_text -> Athena LabelFact
QET primitive graphics -> Athena Presentation Primitive
```

It must not claim QElectroTech ingestion support, runtime dependency, or library parity.

## Syntax Decision

Default recommendation: M25 should not introduce new `.athena` syntax.

Reason:

- M23 already introduced layout syntax and required ANTLR4 plus Tree-sitter synchronization.
- M25 can derive the first presentation policy from component family and port semantics.
- Adding symbol syntax now risks turning M25 into a language milestone instead of a presentation
  policy milestone.

If new syntax becomes unavoidable, both parser surfaces must be upgraded together:

```text
ANTLR4 grammar
Tree-sitter grammar
compiler / AST
LSP diagnostics
Theia syntax behavior
tests
sample project
usage docs
```

No PRD, retrospective, or sample should claim support for syntax that the IDE parser stack does not
accept.

## Deferred Library Direction

M25 should prepare for a future library milestone but not implement it.

Future milestones may include:

- IEC standards profile breadth
- QElectroTech `.elmt` ingestion
- company presentation policy profiles
- curated component/symbol repository
- symbol authoring workflow
- reviewable standards compliance reports

M25 only needs a small governed subset that proves the architecture.

## Guardrails

- Theia IDE is the only frontend target.
- Deprecated desktop-viewer, Compose, and KMP frontend modules are out of scope.
- Renderer-owned symbol meaning is forbidden.
- Canvas-local symbol edits are forbidden.
- Generic fallback symbols are forbidden in the accepted proof. Outside the accepted proof, fallback
  is acceptable only if visible and diagnosable.
- M24 route quality must not regress.
- M25 should stay aligned with Athena's principle: engineering meaning compiles into professional
  engineering artifacts.

## Resolved PRD Review Decisions

- First presentation policy profile: `athena-industrial-control-v0`
- Accepted component families: PLC/controller, HMI/operator device, terminal block, power supply,
  protection device, load/actuator
- Mandatory acceptance path: PLC/controller, terminal block, power supply, load/actuator
- QElectroTech mapping: documentation-only reference example
- Minimum terminal notation: terminal marker shape plus terminal number
- Generic fallback symbols: zero fallback in the accepted proof
- Main architectural term: Presentation Anatomy / Engineering Representation Model, with Symbol
  Anatomy as the electrical schematic subset
