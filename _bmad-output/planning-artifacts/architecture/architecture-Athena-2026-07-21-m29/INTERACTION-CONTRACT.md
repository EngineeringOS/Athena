# M29 Interaction Contract

This companion pins the shared shapes that downstream M29 stories must use. Names may be adjusted
to fit Kotlin/TypeScript conventions, but field meaning, ownership, and conversion rules are
binding.

## Ownership

| Contract | Normative Owner | Transport Rule |
| --- | --- | --- |
| Interaction model shapes | `kernel/interaction-model` | LSP DTOs mirror these shapes and may not add canonical fields. |
| Semantic Capability Registry build | compiler-owned derivation seam | Runtime may cache per active source/projection revision. |
| Command lifecycle and preview invalidation | Interaction Runtime | Frontend renders returned state only. |
| Accepted mutation | existing authoring/runtime/source-edit gates | Interaction Runtime hands off, then observes result. |
| Adapter metadata | frontend adapter | Never accepted as canonical input on command accept. |

## Core Shapes

```text
InteractionSubjectKey {
  canonicalSubjectId: StableSemanticIdentity
  subjectKind: component | port | connection | route | sheetOccurrence | referenceMarker | diagnostic | sourceRange | workspace
  sourceContextId?: string
}

InteractionOccurrenceKey {
  subjectKey: InteractionSubjectKey
  projectionViewId?: string
  sheetId?: string
  documentProjectionId?: string
  occurrenceId?: string
  sourceRevision?: string
}

InteractionSubject {
  key: InteractionSubjectKey
  occurrences: InteractionOccurrenceKey[]
  sourceRange?: SourceRangeRef
  diagnosticId?: string
  capabilities: SemanticCapability[]
  presentationMetadata?: object
  standardMetadata?: object
  adapterMetadata?: object
  provenance: InteractionProvenance
}

SemanticCapability {
  capabilityId: string
  actionFamily: select | hover | focus | reveal | preview | accept | reject | mutate
  enabled: Boolean
  disabledReason?: InteractionDiagnostic
}

SemanticActionIntent {
  actionIntentId: string
  actionFamily: string
  subject: InteractionSubjectKey
  targetSubjects: InteractionSubjectKey[]
  requestedBy: InteractionProvenance
  parameters: object
}

InteractionCommand {
  commandId: string
  actionIntent: SemanticActionIntent
  lifecycleState: InteractionLifecycleState
  preview?: InteractionPreview
  diagnostics: InteractionDiagnostic[]
  undoable: Boolean
  mutationId?: string
  createdAt?: string
  updatedAt?: string
}

InteractionPreview {
  previewId: string
  commandId: string
  status: ready | blocked | stale
  sourceImpact?: SourceImpact
  affectedSubjects: InteractionSubjectKey[]
  projectionPreview?: object
  diagnostics: InteractionDiagnostic[]
  transient: true
  persisted: false
}

InteractionRevealRequest {
  subject: InteractionSubjectKey
  preferredTargets: source | graph | inspector | problems
  occurrence?: InteractionOccurrenceKey
}

InteractionRevealResult {
  subject: InteractionSubjectKey
  targets: InteractionRevealTarget[]
  diagnostics: InteractionDiagnostic[]
  partial: Boolean
}

InteractionDiagnostic {
  code: string
  severity: info | warning | error
  message: string
  subject?: InteractionSubjectKey
  commandId?: string
  sourceRange?: SourceRangeRef
  retryable: Boolean
}

InteractionProvenance {
  actor?: string
  originSurface: graph | source | inspector | problems | palette | commandPalette | ai | api | runtime
  reason?: string
  timestamp?: string
  confidence?: number
}
```

## Conversion Rules

```text
Human / AI / API producer
  -> SemanticActionIntent
  -> InteractionCommand
  -> optional preview
  -> accept
  -> AuthoringIntent where mutation is required
  -> existing mutation/source-edit authority
```

Relationship mutation:

```text
SemanticActionIntent(actionFamily=mutate, relationshipType=ElectricalConnectionRelationship)
  -> InteractionCommand
  -> SemanticRelationshipIntent
  -> source edit / mutation authority
```

Component insertion proof:

```text
SemanticActionIntent(actionFamily=mutate, entityConcept=component)
  -> InteractionCommand
  -> CreateComponentIntent
  -> source edit / mutation authority
  -> nested-port source anatomy
```

`ConnectPortsIntent` may only appear inside a compatibility adapter that immediately converts to
`SemanticRelationshipIntent`. New M29 code may not call it directly.

## Lifecycle State Machine

Interaction Runtime owns transitions.

```text
requested
  -> discovered
  -> validated
  -> previewing
  -> accepted
  -> mutation-pending
  -> committed
  -> reprojected
```

Any state may transition to:

```text
blocked
cancelled
stale
rejected
```

Rules:

- Compiler may propose `discovered` actions but does not advance command state.
- Runtime validates transitions and emits `interaction.command.invalid-state` for illegal moves.
- Frontend may request transitions but does not set authoritative state.
- Preview state becomes `stale` on source reload, projection refresh, active source change, or
  accepted mutation.
- `accepted` means user or producer confirmed intent; `committed` means mutation authority
  succeeded; `reprojected` means refreshed projection contains committed semantic state.

## Preview Ownership

- Compiler derives preview candidates and action eligibility from facts.
- Runtime owns preview lifecycle, preview id, command correlation, stale clearing, and diagnostics.
- Mutation/source-edit authority validates accepted mutation and creates source edits.
- Frontend renders preview payloads and may hold display-only hover/selection chrome.
- `sourceImpact` and `affectedSubjects` must be generated by backend runtime/source-edit logic, not
  by Theia string concatenation.

## LSP Envelope

```text
InteractionEnvelope {
  schemaVersion: "m29.interaction.v1"
  requestId: string
  activeSourceUri?: string
  activeSourceRevision?: string
  payloadKind: subjects | actions | command | preview | reveal | diagnostic | proof
  payload: object
  adapterMetadata?: object
}
```

Rules:

- LSP DTOs mirror kernel contracts; they are not the normative schema.
- Adapter metadata may be round-tripped for UI correlation but is ignored for canonical identity,
  eligibility, preview generation, and mutation accept.
- Payloads must remain JSON-safe and deterministic.
- Unknown schema versions are rejected with `interaction.transport.unsupported-version`.

## Minimum Diagnostic Codes

```text
interaction.subject.unresolved
interaction.action.unsupported
interaction.command.invalid-state
interaction.command.stale
interaction.mutation.ineligible
interaction.reveal.missing-target
interaction.registry.stale
interaction.transport.unsupported-version
interaction.legacy-connect-ports.rejected
```

## Connect-Ports Inventory Rule

M29 cleanup starts with an inventory listing every current `connect-ports` or `ConnectPortsIntent`
path as one of:

```text
removed
migrated-to-interaction-ir
compatibility-adapter
retained-with-owner-target-milestone
```

Acceptance requires tests proving M29 relationship mutation reaches
`SemanticRelationshipIntent(ElectricalConnectionRelationship)` through Interaction IR. Retained
compatibility paths may not be used by new M29 story code.

## Structured Proof Payload Inventory

M29 product proof must emit or assert payloads for:

```text
subject-registry
action-discovery
reveal-source-graph-inspector-problems
relationship-preview
relationship-accept
entity-creation-preview
entity-creation-accept
preview-stale-clearing
legacy-connect-ports-inventory
```

Each proof payload uses `InteractionEnvelope(schemaVersion="m29.interaction.v1")` and includes the
active source context.
