# M31 Addendum: Graphical-First Engineering Model Authoring Rationale

## Selected Direction

M31 completes one graphical-first semantic authoring vertical slice. It does not expand symbol
library breadth and does not introduce a Semantic Agent Runtime.

Alternatives considered:

| Direction | Decision | Reason |
| --- | --- | --- |
| Graphical semantic authoring vertical slice | Selected | Integrates M8 and M27-M30 into a customer-usable workflow. |
| Standards and symbol-library expansion | Deferred | More symbols do not prove authoring. |
| Semantic Agent Runtime | Deferred | Agents should consume a proven authoring contract. |

## Graphical-First Is Not Geometry-First

```text
Graphical View action
  -> Semantic Capability Registry
  -> Interaction IR / Semantic Action Intent
  -> Semantic Authoring Transaction
  -> Authoring Preview
  -> Mutation Authority
  -> .athena persistence
  -> compile and project
```

The frontend may collect intent and display a temporary occurrence. It may not infer a semantic
entity from a dragged symbol, construct authoritative source text, or persist final coordinates.

## Authoring Intent Candidate Shape

```text
SemanticAuthoringIntent
  intentId
  kind
  actor
  origin
  sourceRevision
  subjectContext?
  conceptTemplateId?
  propertyValues
  endpointIds
  compositionTarget?
```

Intent kinds for M31:

```text
CreateEntity
UpdateEntity
RemoveEntity
CreateRelationship
RemoveRelationship
```

## Semantic Authoring Transaction v0

```text
SemanticAuthoringTransaction
  transactionId
  intent
  capabilityEvidence
  revisionGuard
  preview
  validation
  actor/provenance
  decision
  lifecycle
  mutationId?
  result?
  diagnostics
```

M31 v0 admits exactly one mutable intent per transaction. The envelope is producer-neutral, but
M31 implements only Graphical View production. Multi-intent AI plans, batch edits, optimization,
collaboration, and atomic multi-edit commit remain deferred.

## Authoring Capability Model

Authoring capability extends M29's existing `SemanticCapabilityRegistry`:

```text
semantic subject or creation context
  -> authoring capabilities
  -> action discovery
  -> Semantic Action Intent
  -> Semantic Authoring Transaction
```

No separate Authoring Capability Registry is introduced. Domain providers contribute typed
capabilities to the existing registry; Interaction IR consumes the resulting action eligibility.

## Revision-Bound Preview

```text
SemanticAuthoringPreview
  intentId
  revisionGuard
  affectedSemanticIds
  proposedSourceEdits
  semanticDiagnostics
  relationshipImpact
  representationPreview
  compositionTarget
  acceptanceEligibility
```

An accepted preview must match the active Revision Guard:

```text
semanticSnapshotId
sourceUri
documentVersion
contentSha256
```

A mismatch returns a stale diagnostic and does not mutate source. The semantic snapshot protects
project-wide eligibility decisions; the document version and digest protect the exact edit target.

## Semantic Concept vs Visual Definition

An Engineering Concept Template supplies semantic anatomy:

```text
device type
default model
governed properties
nested port names
port directions
signals or media
relationship capabilities
```

A Representation Definition supplies view vocabulary:

```text
primitives
terminal anchors
label slots
variants
style tokens
bounds
```

Representation Policy binds the two downstream. The frontend does not equate a palette symbol with
a semantic entity type.

## Composition Target

Authoring Preview may request a provisional document context and receives a policy-resolved target:

```text
sheetId
zoneId?
laneId?
alignmentGroupId?
```

The requested context is not an independent persisted assignment. Existing document projection
policy re-derives sheet and zone membership from semantic role and projection context after every
compile and reopen. M31 source must not persist arbitrary visual coordinates. Layout, routing, and
final bounds remain derived projection facts.

## Relationship Serialization

Both forms remain valid authoring syntax:

```text
connect A.out -> B.in
```

```text
connect control_group {
  A.out -> B.in
}
```

Both lower to the same canonical engineering relationship collection. The group name is provenance
and organization only.

## Failure Taxonomy

Required authoring diagnostics include:

```text
authoring.intent.unsupported
authoring.identity.duplicate
authoring.template.missing
authoring.port.invalid
authoring.relationship.incompatible
authoring.preview.stale
authoring.source.conflict
authoring.removal.dependencies
authoring.validation.stop-downstream
authoring.representation.unresolved
authoring.composition.unsatisfied
authoring.projection.failed-after-commit
```

`Committed` and `ProjectionFailed` may both be true for one operation. The product must explain
that source mutation succeeded while downstream projection failed.

## Brownfield Authoring Cleanup

M31 replaces two known authority leaks:

- legacy `ConnectPortsIntent` and component-specific authoring names migrate to generic semantic
  entity and `SemanticRelationshipIntent` contracts;
- frontend graph layout source serialization migrates to backend authoring source-edit planning.

Theia may apply a backend-returned editor edit as transport. It may not calculate insertion spans or
serialize `.athena` mutation text.

## Core Product Slice

Customer-facing acceptance is deliberately limited to one entity creation, one relationship
creation, two policy-owned sheets, one cross-sheet reference, and deterministic close/reopen.
Update and removal paths receive typed contracts, capability rules, validation, and tests only.

## No Hidden Projection Editing

Authoring may mutate engineering model truth only. Presentation IR, Representation Occurrence,
Projection Occurrence, sheet output, layout facts, route facts, and rendered geometry are rebuilt
downstream and are never direct mutation targets.

## Two-Sheet Product Proof

The sample contains:

```text
Sheet 1: control logic, switching, relay/coil, status indication
Sheet 2: field terminals, motor/load, continuation/reference occurrence
```

These sheets come from one deterministic customer-demo Document Projection Policy profile. They do
not correspond one-to-one with source files, and the frontend does not infer sheet count.

The user can switch sheets and projection modes without losing the sheet selector. Reopening the
project reconstructs stable sheet and occurrence identities from canonical inputs.

## M32 Direction

M32 is not specified during M31 planning. After M31 evidence, Athena will choose between:

- Semantic Agent Runtime consuming M29/M31 action contracts; or
- governed standards and symbol-library expansion using M30 representation contracts.

No M32 epic or story is created before that decision.
