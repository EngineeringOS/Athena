---
title: Athena M31 - Governed Engineering Model Authoring
status: final
created: '2026-07-21'
updated: '2026-07-21'
---

# Athena M31 PRD - Governed Engineering Model Authoring

## Executive Summary

M31 turns Athena's separate semantic mutation, spatial, interaction, and representation proofs into
one customer-usable engineering model authoring workflow. A controls engineer starts from Graphical
View, creates semantic engineering reality, reviews the governed source impact, accepts or rejects
the transaction, and sees professional documents recompile as downstream projections.

M31 is graphical-first but not geometry-first. The canvas initiates Semantic Action Intent and
displays previews; it does not own canonical identity, source edits, engineering meaning, permanent
coordinates, or representation selection.

The M31 thesis:

```text
Graphical View starts semantic authoring.
Interaction IR governs action discovery and lifecycle.
Mutation Authority validates and persists semantic truth.
M30 representation and composition derive professional occurrences.
Theia remains a replaceable adapter.
```

## Product Thesis

M30 proved that Athena can generate a credible engineering representation. M31 must prove that an
engineer can author the engineering model through the product and receive professional document
projections without bypassing Athena's semantic authorities.

```text
Graphical action
  -> authoring capability discovery
  -> Semantic Action Intent
  -> Semantic Authoring Transaction
  -> revision-bound Authoring Preview
  -> accept or reject
  -> governed .athena mutation
  -> compile semantic model
  -> representation binding
  -> composition and routing
  -> Presentation IR
  -> updated Graphical View
```

The product outcome is one complete rolling-shutter/control-document workflow:

```text
Create -> Preview -> Accept -> Connect -> Inspect -> Switch sheet -> Persist -> Reopen
```

## Users And Journeys

### Target User

The primary user is a controls engineer who needs to author a compact electrical/control document
while preserving inspectable semantic truth. A customer reviewer is a secondary user who needs to
recognize the result as an engineering document rather than a graph demo.

### Jobs To Be Done

- Create an engineering entity without manually writing all source anatomy.
- Understand the semantic and source consequences before accepting a change.
- Create valid relationships from compatible terminals.
- Navigate the same subject across source, Outline, Inspector, Problems, sheets, and projections.
- Reopen a project and recover the same semantic document and professional projection.

### User Journeys

**UJ-1 - Maya adds a field device from Graphical View.** Maya opens the M31 rolling-shutter sample,
uses the sheet context action to create a known device concept, reviews its generated tag, model,
nested ports, affected identities, source edit, and temporary occurrence, then accepts. Athena
validates and persists the mutation, recompiles, binds the M30 representation, composes it into the
governed sheet zone, and reveals the new subject in Graphical View and Outline.

**UJ-2 - Maya connects a control relationship across sheets.** Maya selects compatible terminals,
reviews semantic compatibility, route and source impact, accepts the relationship, switches between
the control and field sheets, and follows the governed folio reference without losing the sheet
selector, projection mode, focus, or reveal state.

**UJ-3 - Maya recovers from a stale or invalid change.** Maya leaves a preview open while source
changes elsewhere. Acceptance is blocked as stale, the exact source revision and diagnostic are
shown, and no mutation occurs. She refreshes the preview and can then accept a newly validated
change.

## Glossary

- **Authoring Intent:** A semantic request to create, update, remove, or relate engineering truth.
- **Authoring Capability:** A typed capability attached to a semantic subject or creation context
  through the existing M29 Semantic Capability Registry. It governs which Authoring Intents may be
  discovered.
- **Authoring Preview:** A revision-bound, non-persistent result containing proposed source edits,
  affected semantic identities, diagnostics, relationship impact, and downstream visual preview.
- **Composition Target:** A policy-resolved sheet, zone, lane, and alignment result used by
  Authoring Preview. It never contains permanent canvas coordinates or independent persistence.
- **Engineering Concept Template:** A platform-owned semantic creation template that supplies
  engineering anatomy and nested ports. It is not a visual symbol definition.
- **Mutation Authority:** The existing backend authority that validates and applies source-backed
  semantic changes.
- **Document Projection Policy:** Platform-owned configuration that selects sheet roles, ordering,
  identity rules, and supported document artifacts independently of source file count.
- **Projection Occurrence:** One downstream visual/document occurrence of a canonical semantic
  subject.
- **Semantic Action Intent:** The M29 frontend-independent primitive used by human, API, workflow,
  or future agent producers to request governed actions.
- **Source Revision:** The exact source state against which an Authoring Preview was validated.
- **Revision Guard:** The semantic snapshot identity plus target source URI, document version, and
  UTF-8 content digest required to accept an Authoring Preview.
- **Semantic Authoring Transaction:** A producer-neutral, revision-guarded envelope containing
  Authoring Intent, capability evidence, preview, validation, decision, mutation handoff, lifecycle,
  result, and provenance. M31 v0 permits exactly one mutable intent per transaction.

## Goals

- Deliver one complete graphical-first engineering model authoring workflow.
- Create one governed semantic entity with nested ports through the product.
- Create one governed semantic relationship from compatible terminals through the product.
- Establish contract and validation readiness for future update and removal workflows.
- Preview source, semantic, relationship, representation, and composition impact before mutation.
- Prove two stable document sheets with one cross-sheet semantic reference occurrence.
- Reuse M29 Interaction IR and M8 Mutation Authority instead of creating frontend authority.
- Reuse M30 representation policy, symbol pack, binding, and composition contracts.
- Preserve deterministic save, close, reopen, compile, and reproject behavior.
- End every story with deep polish and purge, then close with a cleanup ledger and regression proof.

## Non-Goals

- No QET `.elmt` runtime import or `.athena` QET references.
- No full IEC, EPLAN, QET, AutoCAD Electrical, or company-library parity.
- No arbitrary CAD drawing, freehand symbol editor, or geometry database.
- No permanent source-level `x/y` coordinate persistence.
- No new `.athena` syntax; M31 consumes existing nested-port and grouped-relationship syntax.
- No complete component-library browser or marketplace.
- No copy/paste, bulk editing, or full undo/redo system.
- No complete Graphical View workflow for update, entity removal, or relationship removal; M31
  provides contract and validation readiness only.
- No AI planning or Semantic Agent Runtime.
- No PDF, printing, revision package, or release package workflow.
- No 3D, BIM, P&ID, hydraulic, pneumatic, robotics, or harness authoring pack.

## Functional Requirements

### Feature 1 - Graphical Semantic Authoring Session

**FR-1:** Graphical View shall discover authoring actions from M29 Interaction IR and semantic
capabilities, not DOM, SVG geometry, CSS classes, or widget identity.

**FR-2:** M31 shall support Authoring Intent families for create entity, update entity, remove
entity, create relationship, and remove relationship.

**FR-3:** Every Authoring Intent shall carry actor and origin provenance, canonical subject context
where available, and a stable intent id.

**FR-4:** Every mutable action shall produce an Authoring Preview before acceptance.

**FR-5:** An Authoring Preview shall expose Revision Guard, affected semantic identities, proposed
source edits, semantic diagnostics, relationship impact, representation preview, Composition
Target, and acceptance eligibility.

**FR-6:** Rejecting or cancelling an Authoring Preview shall leave source, semantic model,
projection, and document state unchanged.

### Feature 2 - Governed Entity Lifecycle

**FR-7:** A user shall be able to create a semantic entity from an Engineering Concept Template
through Graphical View. The M31 action catalog shall expose only concepts with a valid semantic
template and representation capability for the active projection.

**FR-8:** Entity creation shall support at least canonical tag, type, model, governed properties,
and nested ports required by the selected concept.

**FR-9:** Generated entity source shall use the nested-port `.athena` syntax established before
M31 and shall not generate legacy top-level device port declarations.

**FR-10:** Authoring contracts shall model semantic property update intent, capability, preview,
Revision Guard validation, and diagnostics. M31 does not require a complete Graphical View update
flow.

**FR-11:** Authoring contracts shall model entity removal intent and dependency impact containing
dependent relationships and affected Projection Occurrences. M31 does not require accepted entity
removal through the product.

**FR-12:** Entity removal validation shall prove that dependencies block eligibility when they
would be silently discarded; accepted removal and governed cascade are outside M31 core delivery.

### Feature 3 - Governed Relationship Lifecycle

**FR-13:** A user shall be able to select compatible semantic terminals and discover supported
relationship actions through Interaction IR.

**FR-14:** Relationship preview shall include endpoint identities, compatibility outcome, proposed
route facts, source impact, and structured diagnostics.

**FR-15:** Accepted relationship creation shall pass through `SemanticRelationshipIntent` and the
existing Mutation Authority.

**FR-16:** Flat and grouped `connect` authoring forms shall lower to equivalent flat canonical
engineering relationships.

**FR-17:** Group names shall remain source provenance and authoring organization; they shall not
become a new relationship type or engineering truth.

**FR-18:** Authoring contracts shall model relationship removal intent, preview, and validation
without requiring a complete accepted-removal product flow in M31.

### Feature 4 - Multi-Sheet Document Composition

**FR-19:** M31 shall provide a governed customer-demo Document Projection Policy containing exactly
two sheet roles: control and field/device. Sheet count shall not derive from `.athena` file count.

**FR-20:** Every sheet and Projection Occurrence shall have stable platform-owned identity across
recompile and reopen for unchanged semantic input.

**FR-21:** Authoring Preview shall expose the Composition Target resolved by existing document and
composition policy from semantic role and projection context; it shall not persist independent
sheet membership or permanent coordinates.

**FR-22:** After accepted semantic mutation, final sheet membership and placement shall be
re-derived through document projection policy, composition intent, spatial intent, and layout
rather than retained from preview or renderer-local state.

**FR-23:** The two-sheet proof shall include at least one first-class semantic folio or cross-sheet
reference occurrence.

**FR-24:** M31 shall preserve the accepted Cabinet default projection. Sheet or projection
switching shall preserve the sheet selector, available projection modes, current subject focus
where meaningful, and reveal coherence.

### Feature 5 - Representation, Routing, And Projection Integration

**FR-25:** Accepted entities shall resolve through M30 Representation Policy IR and the
Representation Binding Compiler without Theia symbol inference.

**FR-26:** Engineering Concept Templates and Representation Definitions shall remain separate;
creating a semantic concept shall not create or select a symbol by frontend convention.

**FR-27:** Relationship routes shall attach to governed terminal anchors and shall not silently
fall back to component centers.

**FR-28:** Missing representation policy, symbol, anchor, label slot, or composition target shall
produce structured diagnostics rather than a generic renderer box.

**FR-29:** Normal component background and hitbox chrome shall remain transparent; interaction
chrome may appear only for hover, selection, focus, preview, or drag state.

**FR-30:** ViewBox and framing shall remain derived from resolved presentation bounds and governed
sheet margins, with no hard-coded oversized canvas.

### Feature 6 - Lifecycle, Diagnostics, And Workbench Coherence

**FR-31:** Authoring lifecycle shall distinguish requested, discovered, validated, previewing,
accepted, rejected, mutation-pending, committed, recompiled, reprojected, blocked, stale,
cancelled, and projection-failed states.

**FR-32:** An Authoring Preview shall bind to one Source Revision; acceptance against a different
revision shall be blocked as stale.

**FR-33:** Proposed source shall pass parser and semantic validation before Mutation Authority may
persist it.

**FR-34:** Compile validation requesting `STOP_DOWNSTREAM` shall remain a distinct structured
failure and shall not be reduced to an unexplained `Projection unavailable` message.

**FR-35:** A committed mutation followed by reprojection failure shall report both facts distinctly
and preserve the mutation id and diagnostics.

**FR-36:** The same canonical subject shall remain revealable in source, Outline, Inspector,
Problems, Graphical View, and document sheet occurrences where those targets exist.

### Feature 7 - Customer Proof, Regression, And Cleanup

**FR-37:** M31 shall include `examples/m31/sample-project` using Athena semantic source and
Athena-owned representation assets only.

**FR-38:** Product smoke shall exercise create, preview, accept, relationship creation, sheet and
projection switching, reveal, close, reopen, and persisted-result verification.

**FR-39:** Structured proof shall verify semantic identities, nested ports, source edits,
relationship endpoints, representation occurrences, terminal-anchored routes, composition facts,
sheet identities, lifecycle state, and diagnostics.

**FR-40:** Screenshot evidence shall support human visual review, while structured proof remains
the acceptance authority.

**FR-41:** Every story shall end with a deep polish-and-purge review of touched and adjacent code,
tests, samples, documentation, compatibility paths, and design claims.

**FR-42:** Stale artifacts exposed by a story shall be removed or recorded in the M31 cleanup
ledger with owner, reason, target milestone, and verification.

**FR-43:** Backend authoring protocol shall be the only source-edit planning and serialization
authority. Existing frontend source construction, including graph layout source-edit construction,
shall be migrated to the backend or removed.

**FR-44:** Legacy component-specific and `ConnectPortsIntent` authoring paths shall be migrated to
the generic semantic entity and `SemanticRelationshipIntent` contracts, then removed with their
stale tests, transport names, and documentation.

### Feature 8 - Semantic Authoring Transaction And Capability Model

**FR-45:** M31 shall introduce first-class Semantic Authoring Transaction v0 outside Theia and
outside representation or projection models.

**FR-46:** Transaction v0 shall contain exactly one mutable Authoring Intent. Multi-intent, batch,
optimization, and agent-planned transactions shall return an explicit unsupported diagnostic and
remain deferred.

**FR-47:** A transaction shall carry transaction id, Authoring Intent, capability evidence,
Revision Guard, Authoring Preview, validation result, actor and provenance, decision, lifecycle,
mutation id where committed, result, and diagnostics.

**FR-48:** Authoring Capability shall extend the existing M29 Semantic Capability Registry and
action-discovery pipeline; M31 shall not create a second competing capability registry.

**FR-49:** Authoring action discovery shall derive eligibility from semantic subject or creation
context, actor policy, Engineering Concept Template availability, relationship capability,
projection context, and active representation capability.

**FR-50:** Human Graphical View, API, workflow, and future agent producers shall share the same
Semantic Action Intent and Semantic Authoring Transaction contracts. M31 implements only the human
Graphical View producer.

**FR-51:** No authoring operation shall directly mutate Presentation IR, Representation
Occurrence, Projection Occurrence, document sheet output, layout fact, route fact, or rendered
geometry. All downstream artifacts shall be re-derived after accepted semantic mutation.

## Non-Functional Requirements

**NFR-1:** `.athena` remains the canonical semantic persistence format for M31.

**NFR-2:** Theia remains an adapter and shall not construct authoritative source edits, canonical
identity, relationship meaning, representation selection, or permanent composition state.

**NFR-3:** Authoring results shall be deterministic for identical source, policy, template,
document, and action inputs.

**NFR-4:** Accepted mutation shall be revision-safe and shall never apply a stale preview silently.

**NFR-5:** Authoring payloads shall remain transport-safe and frontend-independent for future API,
workflow, or agent consumers.

**NFR-6:** M27 layout/routing, M28 relationship mutation, M29 interaction, and M30 representation
invariants shall not regress.

**NFR-7:** Normal-state visual density shall not regress through visible wrappers, duplicated
occurrences, repeated labels, oversized viewBox, or center-fallback routes.

**NFR-8:** Structured diagnostics shall name the failed authority and lifecycle stage.

**NFR-9:** Gradle verification commands shall run strictly sequentially on Windows.

**NFR-10:** Repository text shall remain UTF-8, and `*.zh-CN.md` shall retain UTF-8 with BOM.

**NFR-11:** M31 may refactor aggressively where required to restore one authority path, but replaced
paths shall be removed rather than retained as undocumented compatibility code.

**NFR-12:** Story completion requires acceptance-criterion-to-evidence mapping and the mandatory
polish-and-purge gate.

**NFR-13:** Engineering model authoring terminology shall remain authoritative in contracts and
architecture; document authoring may be used only for the downstream user-facing projection
workflow.

## M31 Core Acceptance Scope

The customer-facing milestone is complete only when all five outcomes work together:

1. Create one semantic device with nested ports through Graphical View.
2. Create one compatible semantic relationship through governed preview and acceptance.
3. Project the accepted model into exactly two policy-owned sheets.
4. Resolve and navigate one semantic cross-sheet reference occurrence.
5. Close and reopen the project with identical semantic, sheet, occurrence, and relationship
   identity.

Update entity, remove entity, and remove relationship must have typed contracts, capability rules,
validation, and tests, but are not customer-facing M31 acceptance workflows.

## Success Metrics

- **SM-1:** One Electron product smoke completes the full M31 workflow from graphical entity
  creation through persisted reopen. Validates FR-1..FR-40.
- **SM-2:** Accepted generated source uses nested ports, parses, validates, and recompiles with zero
  hidden source edits. Validates FR-7..FR-9 and FR-32..FR-35.
- **SM-3:** All accepted relationship mutations use canonical endpoint identities and
  `SemanticRelationshipIntent`; grouped and flat forms produce equivalent semantic relationships.
  Validates FR-13..FR-18.
- **SM-4:** The sample policy exposes exactly two governed sheets, stable switching controls, and at least
  one cross-sheet reference occurrence after reopen. Validates FR-19..FR-24.
- **SM-5:** Accepted proof contains zero renderer fallback boxes, center-fallback routes, duplicate
  off-screen occurrences, repeated labels, visible normal wrappers, or hard-coded oversized
  viewBox. Validates FR-25..FR-30.
- **SM-6:** Stale preview, blocked removal, invalid relationship, `STOP_DOWNSTREAM`, and
  projection-failed cases each produce distinct structured diagnostics. Validates FR-31..FR-36.
- **SM-7:** Every completed story contains fresh evidence mapping plus a purge result, final
  closeout leaves no frontend source serializer or legacy `ConnectPortsIntent` path, and no stale
  M31 artifact remains unowned. Validates FR-41..FR-44.
- **SM-8:** Every mutable M31 action is represented by a single-intent Semantic Authoring
  Transaction discovered through the existing Semantic Capability Registry, and accepted mutation
  directly changes no downstream projection artifact. Validates FR-45..FR-51.
- **SM-9:** Update entity, remove entity, and remove relationship have typed intent, capability,
  preview, validation, dependency-impact, and diagnostic contract tests without a customer-facing
  completion claim. Validates FR-10..FR-12 and FR-18.

### Counter-Metrics

- **SM-C1:** Do not optimize symbol count; M31 succeeds through one complete authoring workflow,
  not library breadth.
- **SM-C2:** Do not reduce interaction steps by bypassing preview, validation, provenance, or
  Mutation Authority.
- **SM-C3:** Do not improve visual placement by persisting arbitrary coordinates as semantic truth.

## Acceptance Criteria

- Dedicated M31 PRD, addendum, architecture, epics, sprint, story, retrospective, and cleanup
  artifacts follow the established BMAD milestone structure.
- `examples/m31/sample-project` opens in Athena IDE and defaults to the governed customer proof.
- Graphical View supports the approved create-to-reopen workflow through Interaction IR.
- Every accepted mutation flows through a single-intent Semantic Authoring Transaction discovered
  from the existing M29 Semantic Capability Registry.
- Accepted source uses nested ports and governed relationship serialization.
- Two sheets and one semantic cross-sheet reference are proven.
- M30 representation policy and native assets render accepted entities without Theia inference.
- Product smoke and structured proof cover success and failure lifecycle paths.
- Backend-generated source edits are revision-guarded; frontend source serializers and legacy
  component/connect-port intent paths are absent.
- No authoring path directly mutates document, representation, projection, layout, route, or
  rendered geometry outputs.
- M27 through M31 regression smokes run sequentially before closeout.
- Encoding audit passes after repository text changes.
- Cleanup ledger and retrospective are complete and accurate.

## Open Questions

No phase-blocking questions remain. M32 direction is intentionally deferred until M31 customer
proof exposes whether agent runtime or standards/library depth is the higher-value next step.

## Assumptions Index

No unconfirmed assumptions remain in this PRD. The graphical-first workflow, semantic authority
boundary, two-sheet proof, scope exclusions, and mandatory purge requirement were explicitly
approved during M31 discovery.
