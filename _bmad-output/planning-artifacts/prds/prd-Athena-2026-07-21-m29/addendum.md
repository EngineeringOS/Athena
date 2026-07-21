# M29 Addendum: Semantic Interaction Boundary Rationale

## Approved Position

M29 should include:

```text
A: component insertion proof
B: relationship authoring cleanup
C: semantic reveal/navigation
```

But this must be structured as:

```text
one platform interaction layer
  plus
three narrow consumers
```

not as three independent feature builds.

## Why M29 Is Needed

Athena has already kept authority out of the renderer, router, layout engine, and document pages.
Interaction is now the next place where authority can accidentally leak into the frontend.

Without M29, Theia slowly becomes the place that decides:

- what a click means,
- what can be selected,
- what commands exist,
- what a preview represents,
- how source and graph reveal relate,
- when a command is mutation-eligible.

That would violate the same semantic-first principle that earlier milestones protected.

## Interaction Name vs Action Primitive

Keep the milestone name:

```text
Semantic Interaction Model
```

because M29 is proving human-facing interaction behavior. But the deeper internal primitive should
be:

```text
Semantic Action Intent
```

Reason:

- humans click, hover, select, focus, and confirm;
- AI agents discover, evaluate, request, explain, and execute;
- API/workflow clients request capabilities directly.

All of those should converge on a semantic action contract rather than a UI gesture contract.

```text
Human adapter      AI adapter      API/workflow adapter
      \               |                 /
       \              |                /
        v             v               v
             Semantic Action Intent
                       |
                 Interaction IR
```

## Correct Mental Model

Avoid:

```text
Theia widget click
  -> inspect DOM/SVG
  -> infer subject
  -> run frontend command
  -> maybe mutate source
```

Use:

```text
projection fact / semantic subject
  -> InteractionSubject
  -> InteractionAction discovery
  -> InteractionCommand
  -> preview / reveal / mutation request
  -> backend validation and source-backed authority
```

## Interaction IR Candidate Vocabulary

M29 architecture may refine names, but the model should cover:

```text
InteractionSubject
InteractionSurface
InteractionOccurrence
InteractionAction
InteractionCommand
InteractionPreview
InteractionDiagnostic
InteractionRevealTarget
InteractionLifecycleState
InteractionAdapterContext
InteractionResult
SemanticActionIntent
SemanticCapability
InteractionProvenance
```

Required lifecycle vocabulary:

```text
requested
discovered
validated
previewing
accepted
rejected
mutation-pending
committed
reprojected
blocked
stale
cancelled
```

M29 should not implement a full workflow engine. These names are contracts for small, testable
interaction flows.

## Interaction Lifecycle State Machine

M29 should turn interaction lifecycle into an explicit model, not a list of states hidden in UI
code.

Example semantic relationship mutation:

```text
Idle
  -> Candidate
  -> Validated
  -> Previewing
  -> Accepted
  -> MutationPending
  -> Committed
  -> Reprojected
```

Example semantic entity creation:

```text
Requested
  -> Resolved
  -> Validated
  -> Previewing
  -> Accepted
  -> Persisted
  -> Projected
```

This enables future collaboration, AI agents, approvals, undo/redo, and long-running operations
without forcing M29 to implement all of them.

M29 should include only vocabulary and structured payloads for undo/redo:

```text
undoable: Boolean
mutationId: optional id linking to M8 mutation authority
```

Actual undo/redo behavior remains outside M29.

## Subject Identity Rule

An interaction subject must carry:

```text
canonicalSubjectId
subjectKind
surface
projectionViewId?
projectionOccurrenceId?
sourceRange?
diagnosticId?
provenance
capabilities
standardMetadata?
presentationMetadata?
```

Frontend-local ids may be carried only as adapter metadata. They may not become subject identity.

## Semantic Capability Registry Rule

The subject index should be understood as a semantic capability registry, not a frontend lookup map.

Wrong:

```text
SVG node
  -> subject id
```

Correct:

```text
semantic subject
  -> capabilities
  -> projection occurrences
  -> frontend representations
```

For example:

```text
component:MotorM101
  capabilities:
    - reveal-source
    - reveal-graph
    - inspect
    - create-relationship
    - replace
  occurrences:
    - documentation/sheet/02-control.component:MotorM101
    - source:src/main.athena:44
```

This keeps M29 aligned with EngineeringOS and prepares future AI action discovery.

## Interaction Runtime Boundary

Interaction is not only a data model. It carries session and lifecycle behavior:

- pending commands,
- stale previews,
- active actor/provenance,
- command eligibility,
- diagnostics,
- mutation handoff state.

Recommended boundary:

```text
kernel/interaction-model
  interaction contracts and payload-neutral data types

runtime/interaction-runtime or kernel/runtime integration
  session state, lifecycle, pending commands, and provenance

compiler/interaction-compiler or compiler-owned derivation seam
  builds subject/capability registry from semantic and projection facts

ide/theia-frontend
  adapter only
```

The architecture spine may choose exact module placement based on current Gradle boundaries, but it
must keep this responsibility split visible.

## LSP And GLSP Boundary

M29 should clarify the relationship to existing transport/rendering seams:

```text
Interaction compiler/runtime
  -> product-safe Interaction payloads
  -> LSP transport
  -> Theia adapter
  -> graph/source/inspector UI
```

GLSP/SVG may render interaction affordances, but must not own interaction meaning.

```text
Interaction IR is upstream of frontend behavior.
GLSP/SVG/Theia are downstream adapters.
LSP transports interaction payloads and decisions.
```

Interaction indexes should refresh with projection refresh and remain bound to the current active
`.athena` source context.

## Theia Adapter Rule

Theia may:

- render available actions,
- show hover/selection chrome,
- ask runtime/LSP for interaction commands,
- show previews,
- apply backend-returned source edits,
- request reveal targets.

Theia may not:

- create canonical subject identity from DOM text,
- treat SVG geometry as relationship truth,
- persist interaction state as semantic truth,
- bypass backend validation for accepted mutation,
- invent source edits.

## Semantic Relationship Mutation Cleanup Rule

M28 semantic relationship mutation should move behind Interaction IR. Use "relationship authoring"
only for user-facing workflow language; the kernel/runtime contract is mutation of semantic reality,
not UI authoring behavior.

The command discovered by Interaction IR may be domain-labeled as:

```text
Connect
```

but the underlying command target must remain:

```text
SemanticRelationshipIntent(ElectricalConnectionRelationship)
```

Retained `connect-ports` paths must be explicitly classified:

- compatibility transport,
- old graph-command test,
- retained until M29 migration completes,
- removed.

## Semantic Entity Creation Proof Rule

Component insertion is included to prove semantic entity creation, not to build a complete component
library UX and not to implement symbol placement as the source of truth.

Minimum proof:

```text
select system or empty sheet context
  -> discover "Insert component" action
  -> choose one known component concept
  -> preview source impact
  -> accept
  -> backend source edit
  -> nested-port source
  -> recompile/reproject
  -> new component appears
```

The source serializer must preserve the M28 nested anatomy rule.

Wrong:

```text
drag symbol
  -> create motor
```

Correct:

```text
create semantic component entity
  -> compiler/projection derives symbol occurrence
```

## Reveal And Navigation Rule

Reveal is the safest first consumer because it exercises the core interaction model without mutation.

M29 should prove reveal from:

- graph subject to source,
- source subject to graph,
- diagnostic to source and graph where possible,
- graph subject to inspector,
- document reference or sheet occurrence to graph subject.

Missing targets should return structured diagnostics, not guessed navigation.

## Product Smoke Rule

The M29 product smoke should verify:

- M29 sample opens,
- Interaction proof payload exists,
- subject index count is nonzero for key kinds,
- reveal proof does not use DOM text,
- relationship authoring goes through Interaction IR,
- component insertion source edit uses nested ports,
- reprojected graph contains inserted component,
- retained legacy paths are absent or explicitly reported.

## Deferred Standards And Visual Fidelity Boundary

M29 does not introduce IEC/QElectroTech/EPLAN-style symbol library expansion or visual equivalence.

M29 may preserve and consume M25-M27 presentation, routing, symbol, standards, and sheet metadata
when present, but it must not expand the standards library, import QElectroTech `.elmt` elements, or
attempt EPLAN visual parity.

This concern is deferred to a later standards/presentation milestone. It does not have to be M30;
the milestone order should be decided after M29 based on whether Athena needs agent runtime next or
standards/library depth next.

```text
Future Governed Standards And Symbol Library Foundation
```

That milestone should address:

- IEC symbol families,
- QElectroTech reference mapping as documentation/reference, not direct authority,
- governed symbol variant policy,
- standards profile selection,
- richer component library UX,
- professional visual acceptance corpus,
- continued non-EPLAN-clone boundary.

M29 should only ensure that future IEC/standards metadata can travel through Interaction IR as
metadata, not as interaction-owned standards meaning.

## Post-M29 Direction Note

M29 creates the interaction/action pillar. After it, Athena will have the basic OS-level chain:

```text
Semantic Kernel
  -> Spatial Compiler
  -> Presentation Compiler
  -> Interaction Runtime
  -> Mutation Authority
```

The strongest post-M29 candidate is a Semantic Agent Runtime milestone, because agents need the
same subjects, capabilities, actions, lifecycle, provenance, previews, and mutation handoff that
M29 introduces. A standards/symbol-library milestone remains important, but it should not be allowed
to pull M29 back toward an EPLAN/QElectroTech clone.

## Deferred

M29 should defer:

- full undo/redo implementation,
- AI command planning,
- multi-user interaction state,
- complete palette UX,
- gesture grammar,
- Web/3D/VR/CLI adapters,
- full standards intelligence,
- IEC/QElectroTech/EPLAN visual/library expansion,
- automatic source conflict resolution.
