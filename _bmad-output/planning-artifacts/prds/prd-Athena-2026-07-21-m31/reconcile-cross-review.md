# M31 Cross-Review Reconciliation

## Outcome

The cross-review was accepted selectively and reconciled against existing M29/M30 contracts and
current code.

## Accepted

- Rename the milestone to **Governed Engineering Model Authoring** because documents are downstream
  projections of authored engineering reality.
- Add Semantic Authoring Transaction as a producer-neutral envelope around intent, capability
  evidence, revision, preview, validation, decision, mutation, lifecycle, result, and provenance.
- Define a narrow core product slice: one entity creation, one relationship creation, exactly two
  policy-owned sheets, one cross-sheet reference, and deterministic reopen.
- Make update entity, remove entity, and remove relationship contract-readiness work rather than
  customer-facing M31 product flows.
- Add an explicit rule prohibiting direct mutation of presentation, representation, projection,
  sheet, layout, route, and geometry outputs.
- Preserve human, API, workflow, and future agent compatibility through shared platform contracts.

## Adapted

- Authoring Capability extends M29's existing `SemanticCapabilityRegistry`. A separate Authoring
  Capability Registry would duplicate authority and was not adopted.
- Semantic Authoring Transaction v0 contains exactly one mutable intent. Multi-intent AI, batch,
  optimization, and collaboration transactions remain deferred to avoid expanding M31 into a
  workflow engine.
- Capability discovery precedes transaction creation. The review diagram placed capability after
  transaction, which would let transaction runtime invent action eligibility.

## Already Covered

- Backend-only source-edit planning and serialization remain FR-43 and AD-4.
- Engineering Concept Template and Representation Definition separation remains FR-26 and AD-6.
- Document Projection Policy remains the sheet-count and sheet-identity authority.
- Frontend-independent API, workflow, and future agent payloads were already required by NFR-5 and
  are now also explicit in FR-50.
