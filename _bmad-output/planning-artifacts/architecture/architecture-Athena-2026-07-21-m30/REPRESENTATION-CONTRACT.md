# M30 Representation Contract

## Purpose

This contract defines the M30 boundary between semantic engineering truth, representation assets,
schematic composition, and rendering.

## Core Data Shapes

Names are candidate contract names. Implementation may refine them, but stories must preserve the
responsibilities.

```text
RepresentationSymbolId
RepresentationLibraryId
RepresentationPolicyId
RepresentationPrimitive
RepresentationAnchor
RepresentationTerminal
RepresentationLabelSlot
RepresentationStyleToken
RepresentationVariant
RepresentationSymbolDefinition
RepresentationOccurrence
RepresentationReferenceBinding
RepresentationBindingRule
RepresentationBindingDiagnostic
SchematicCompositionIntent
SchematicCompositionFact
RepresentationLifecycleState
```

## Representation Symbol Definition

A symbol definition describes reusable view-layer vocabulary:

```text
symbolId
libraryId
version
lifecycle
supersededBy?
migrationHint?
kind
bounds
hotspot
primitives
terminals
anchors
labelSlots
variants
styleTokens
provenance
```

It must not contain semantic source truth. It may declare what semantic roles it can represent, but
binding policy decides whether it is used.

## Representation Policy IR

Representation policy chooses what visual vocabulary applies in a projection context:

```text
policyId
projectionKind
standardProfile?
semanticSubjectKind
semanticRole?
occurrenceRole
symbolId
variant?
fallback
priority
```

Policy is not `.athena` syntax in M30. It is platform-owned presentation configuration.

## Representation Occurrence IR

An occurrence describes one resolved use of a definition:

```text
occurrenceId
canonicalSemanticId
projectionOccurrenceId
occurrenceRole
symbolId
variant
labelBindings
terminalBindings
referenceBindings
compositionIntentMembership
diagnostics
```

The occurrence is not a reusable asset and is not source truth.

## Primitive Vocabulary v0

M30 v0 supports only what the demo needs:

```text
line
polyline
rectangle
circle
arc
polygon
text-slot
```

All primitives are presentation primitives. They are not kernel geometry.

## Anchor And Terminal Rules

- Every route-attached symbol occurrence must expose named terminal anchors.
- Anchors have local symbol coordinates and orientation.
- Binding output connects anchors to canonical port/terminal semantic ids.
- Missing anchor is a binding diagnostic, not renderer guesswork.

## Label Slot Rules

- Labels are slots, not hard-coded repeated text.
- Slots bind to canonical values such as device tag, terminal number, reference label, model, or
  relationship id.
- Verbose semantic ids are inspector/debug details by default, not always-visible sheet labels.

## Binding Rule Shape

```text
semanticSubjectKind
semanticRole?
projectionContext
occurrenceRole
symbolId
variant?
labelBindings
terminalBindings
priority
diagnostics
```

Binding rule output is a representation occurrence, not a source mutation.

## Reference Binding Rules

Reference occurrences are first-class binding outputs for professional drawings:

```text
coil-contact
device-terminal-strip
component-location
folio-continuation
previous-next-reference
```

The reference relationship is semantic/projection data. The mark, arrow, or label is visual
representation.

## Composition Intent Rules

Schematic composition intent produces planning facts before final presentation geometry:

```text
lane membership
column membership
alignment group
label band
route channel
reference zone
terminal group
```

Forbidden:

```text
composition as CAD database
composition as source mutation
composition as renderer-owned layout truth
```

## Diagnostics

Stable diagnostic categories:

```text
representation.symbol.missing
representation.symbol.unsupported-role
representation.anchor.missing
representation.terminal.incompatible
representation.label-slot.missing
representation.binding.ambiguous
representation.library.invalid
representation.composition.unsatisfied
representation.policy.ambiguous
representation.policy.missing
representation.lifecycle.unsupported
```

## Renderer Contract

Renderer receives Presentation IR and may not:

- load QET `.elmt`;
- load raw representation assets as semantic authority;
- duplicate off-screen symbol occurrences;
- create visible wrapper borders in normal state;
- hard-code large viewBox constants;
- infer terminal semantics from SVG geometry.

Renderer may:

- paint resolved primitives;
- attach transparent hitboxes;
- show dotted hover/selection/focus/drag affordances;
- expose data attributes for structured testing when they are downstream metadata only.

## QET Converter Boundary

Future converter target:

```text
QET .elmt -> QET AST -> Athena RepresentationSymbolDefinition candidate
```

Forbidden target:

```text
QET .elmt -> .athena source
```

Runtime references to QET files are forbidden for M30.
