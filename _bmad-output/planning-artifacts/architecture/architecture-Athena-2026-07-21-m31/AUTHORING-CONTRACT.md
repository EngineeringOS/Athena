# M31 Authoring Contract

## Purpose

This contract defines the boundary between capability discovery, Graphical View actions,
Interaction IR, Semantic Authoring Transaction, revision-bound authoring preview, Mutation
Authority, semantic persistence, and downstream reprojection.

## Core Data Shapes

```text
SemanticAuthoringIntent
SemanticAuthoringIntentId
SemanticAuthoringIntentKind
SemanticAuthoringTransaction
SemanticAuthoringTransactionId
SemanticAuthoringPreview
SemanticAuthoringResult
AuthoringSourceRevision
AuthoringRevisionGuard
EngineeringConceptTemplate
CompositionTarget
RelationshipImpact
AuthoringDiagnostic
AuthoringLifecycleState
AuthoringCapability
AuthoringCapabilityEvidence
```

Names may be refined during implementation, but responsibilities and authority direction are
binding.

## Semantic Authoring Intent

```text
intentId
kind
actor
origin
reason?
revisionGuard
subjectContext?
conceptTemplateId?
propertyValues
relationshipEndpoints
compositionTarget?
provenance
```

Supported kinds:

```text
create-entity
update-entity
remove-entity
create-relationship
remove-relationship
```

## Authoring Capability

Authoring Capability is contributed to M29's existing `SemanticCapabilityRegistry`:

```text
capabilityId
subjectKindOrCreationContext
intentKind
actorPolicy
domainEligibility
conceptTemplateRequirement?
projectionRequirement?
representationRequirement?
```

The registry emits Authoring Capability Evidence consumed by transaction creation. No parallel
authoring capability registry is permitted.

## Semantic Authoring Transaction v0

```text
transactionId
intent
capabilityEvidence
revisionGuard
preview
validation
actor/provenance
decision
lifecycleState
mutationId?
result?
diagnostics
```

M31 v0 requires exactly one mutable intent. Empty or multi-intent transaction input returns
`authoring.transaction.intent-count-unsupported`. The contract remains producer-neutral; M31 only
implements Graphical View as a producer.

The transaction is runtime and audit state, not engineering truth. Accepted `.athena` semantics
remain canonical; transaction state cannot be consumed as a replacement engineering model.

## Authoring Preview

```text
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

Preview is transient. It is not semantic truth, document truth, or an accepted mutation.

## Revision Guard

```text
semanticSnapshotId
sourceUri
documentVersion
contentSha256
```

- Preview validation uses one exact Revision Guard.
- Accept carries the preview intent id and Revision Guard.
- `contentSha256` is computed from the exact UTF-8 source content used for preview.
- If active source differs, accept returns `authoring.preview.stale`.
- Stale acceptance writes nothing.
- Refresh produces a new preview and revision binding.

## Engineering Concept Template

```text
templateId
conceptKind
deviceType
defaultModel?
propertySchema
portTemplates
relationshipCapabilities
provenance
```

Port template:

```text
name
direction
signalOrMedium
terminalNumber?
required
```

An Engineering Concept Template contains no symbol geometry, SVG, style tokens, anchors, or
viewBox.

## Composition Target

```text
sheetId
zoneId?
laneId?
alignmentGroupId?
```

Forbidden fields:

```text
x
y
width
height
svgTransform
domId
```

Composition Target is a policy-resolved preview result. A requested sheet or zone is a provisional
context only. Existing document projection policy re-derives final sheet and zone membership from
accepted semantic facts after compile and reopen.

The M31 customer-demo Document Projection Policy exposes exactly two sheet roles: control and
field/device. Sheet count is independent of source file count and frontend state.

## Lifecycle

```text
requested
discovered
validated
previewing
accepted
rejected
mutation-pending
committed
recompiled
reprojected
blocked
stale
cancelled
projection-failed
```

`committed` followed by `projection-failed` means canonical source changed successfully and
downstream projection failed. The result must carry both the mutation id and projection diagnostic.

## Validation Order

```text
intent shape
  -> actor and subject eligibility
  -> source revision
  -> semantic identity and template
  -> relationship compatibility or dependency impact
  -> proposed source edit
  -> parser validation
  -> semantic validation
  -> representation and composition preview
  -> acceptance eligibility
```

M31 action discovery excludes concepts without valid semantic and active-projection representation
capabilities. After semantic acceptance, an unexpected representation or composition failure does
not roll back source; it returns `projection-failed` with structured diagnostics and must never
trigger a renderer fallback as silent success.

## Mutation Handoff

Accepted request:

```text
intentId
revisionGuard
validatedSourceEdits
affectedSemanticIds
actor/provenance
```

Returned result:

```text
mutationId?
lifecycleState
committedRevision?
semanticIds
projectionOccurrenceIds
diagnostics
```

Backend authoring protocol computes insertion spans and serializes `validatedSourceEdits`. Theia may
apply the returned edit through the editor bridge as transport. It may not construct the edit.
Mutation Authority remains the semantic acceptance gate.

## Relationship Rules

- Endpoints are canonical semantic port or terminal identities.
- Compatibility comes from semantic/domain facts.
- Route preview is downstream evidence, not relationship truth.
- Grouped source syntax lowers to the same canonical relationship as flat syntax.
- Removing a relationship does not remove endpoint entities.

## Entity Removal Rules

- Preview lists dependent relationships and Projection Occurrences.
- Non-empty dependency removal is blocked in M31.
- Automatic cascade and orphan cleanup are deferred.
- Rejection or blocked removal writes nothing.

## Diagnostic Envelope

```text
code
authority
lifecycleStage
severity
message
subjectId?
sourceRange?
relatedIds
recoveryAction?
```

Required codes:

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
authoring.transaction.intent-count-unsupported
authoring.capability.unavailable
```

## Downstream Re-Derivation Rule

Authoring operations may mutate engineering semantic truth only. They may not directly mutate:

```text
Presentation IR
Representation Occurrence
Projection Occurrence
document sheet output
layout facts
route facts
rendered geometry
```

These outputs are re-derived after accepted mutation.

## Frontend Adapter Contract

Theia may:

- request available actions;
- collect user-entered semantic values;
- request and display Authoring Preview;
- display source diff, diagnostics, temporary occurrences, and relationship impact;
- request accept, reject, cancel, refresh, and reveal;
- render returned Presentation IR and transient interaction chrome.

Theia may not:

- create canonical identity from DOM or SVG;
- author final source edits;
- decide relationship compatibility;
- select representation by frontend convention;
- persist canvas coordinates;
- collapse blocked, stale, compile-stop, or projection-failed outcomes into one generic error.
- create a transaction for an action absent from SemanticCapabilityRegistry discovery.

## Product Proof Contract

The M31 product smoke must prove:

```text
open exact M31 sample
discover graphical authoring capability and action
create one single-intent Semantic Authoring Transaction
preview entity creation
accept and verify nested-port source
recompile and verify representation occurrence
preview and accept compatible relationship
verify terminal-anchored route
switch two sheets and projection modes
reveal canonical subject across available surfaces
close and reopen
verify stable persisted semantic and document identity
exercise stale and blocked diagnostics
verify downstream projection artifacts were re-derived rather than directly mutated
```

Structured proof is authoritative; screenshot evidence is secondary.
