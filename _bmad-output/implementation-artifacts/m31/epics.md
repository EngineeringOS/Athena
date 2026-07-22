---
stepsCompleted:
  - m31-requirements-extraction
  - m31-epic-design
  - m31-story-generation
  - m31-final-validation
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/reconcile-cross-review.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md
---

# Athena M31 - Epic Breakdown

## Overview

M31 delivers Athena's first complete graphical-first engineering model authoring transaction. The
engineer creates one semantic entity and one compatible relationship, accepts revision-safe backend
mutations, receives exactly two policy-owned document sheets with one cross-sheet reference, and
reopens the same semantic and projection identities. Documents, representations, layout, routing,
and geometry remain re-derived outputs rather than mutation targets.

## Requirements Inventory

### Functional Requirements

- **FR-1:** Discover graphical authoring actions from Interaction IR and semantic capabilities.
- **FR-2:** Model create/update/remove entity and create/remove relationship intent families.
- **FR-3:** Carry actor, origin, canonical context, and stable intent identity.
- **FR-4:** Produce Authoring Preview before every mutable action is accepted.
- **FR-5:** Preview Revision Guard, affected ids, source edits, diagnostics, impact, representation,
  composition, and eligibility.
- **FR-6:** Reject/cancel without source, semantic, projection, or document mutation.
- **FR-7:** Create one entity from a discoverable Engineering Concept Template with valid active
  representation capability.
- **FR-8:** Create canonical tag, type, model, properties, and nested ports.
- **FR-9:** Serialize nested ports and never generate legacy top-level device ports.
- **FR-10:** Provide typed update-property intent, capability, preview, revision, and diagnostics.
- **FR-11:** Provide typed entity-removal intent and dependency impact.
- **FR-12:** Prove dependency-blocked removal eligibility without implementing accepted removal.
- **FR-13:** Discover relationship actions from compatible canonical terminals.
- **FR-14:** Preview endpoint ids, compatibility, route facts, source impact, and diagnostics.
- **FR-15:** Accept relationship creation through `SemanticRelationshipIntent` and Mutation Authority.
- **FR-16:** Lower flat and grouped `connect` source to equivalent canonical relationships.
- **FR-17:** Keep connect group names as provenance, never relationship truth.
- **FR-18:** Provide typed relationship-removal intent, preview, and validation readiness.
- **FR-19:** Provide exactly two policy-owned sheet roles: control and field/device.
- **FR-20:** Keep sheet and projection occurrence identity stable across compile and reopen.
- **FR-21:** Resolve Composition Target from document/composition policy without independent persistence.
- **FR-22:** Re-derive sheet membership and placement after accepted semantic mutation.
- **FR-23:** Produce at least one semantic cross-sheet reference occurrence.
- **FR-24:** Preserve Cabinet default, sheet selector, modes, focus, and reveal through switches.
- **FR-25:** Resolve accepted entities through M30 Representation Policy and Binding Compiler.
- **FR-26:** Keep Engineering Concept Templates separate from Representation Definitions.
- **FR-27:** Route relationships through governed terminal anchors without center fallback.
- **FR-28:** Diagnose missing representation and composition facts without generic fallback boxes.
- **FR-29:** Keep normal component/hitbox chrome transparent and interaction chrome transient.
- **FR-30:** Derive viewBox and framing from presentation bounds and governed margins.
- **FR-31:** Distinguish all requested-through-projection-failed lifecycle states.
- **FR-32:** Bind preview to one exact Revision Guard and block stale acceptance.
- **FR-33:** Parse and semantically validate proposed source before persistence.
- **FR-34:** Preserve `STOP_DOWNSTREAM` as a named structured failure.
- **FR-35:** Distinguish committed mutation from later reprojection failure.
- **FR-36:** Reveal canonical subjects across source, Outline, Inspector, Problems, graph, and sheets.
- **FR-37:** Add `examples/m31/sample-project` with Athena-owned source and representation assets.
- **FR-38:** Smoke create, preview, accept, relationship, switching, reveal, close, and reopen.
- **FR-39:** Prove semantic, source, relationship, representation, routing, composition, sheet,
  lifecycle, and diagnostic facts structurally.
- **FR-40:** Keep structured proof authoritative and screenshots secondary.
- **FR-41:** End every story with deep polish and purge of touched and adjacent artifacts.
- **FR-42:** Remove stale artifacts or ledger owner, reason, target milestone, and verification.
- **FR-43:** Make backend protocol the only source-edit planning and serialization authority.
- **FR-44:** Remove legacy component-specific and `ConnectPortsIntent` authoring paths after migration.
- **FR-45:** Introduce first-class Semantic Authoring Transaction v0 outside frontend/projection models.
- **FR-46:** Permit exactly one mutable intent per transaction and diagnose other cardinalities.
- **FR-47:** Carry transaction identity, intent, capability evidence, Revision Guard, preview,
  validation, actor/provenance, decision, lifecycle, mutation/result, and diagnostics.
- **FR-48:** Extend M29 `SemanticCapabilityRegistry`; do not create a second registry.
- **FR-49:** Derive authoring eligibility from subject/context, actor policy, domain capability,
  concept template, projection context, and representation capability.
- **FR-50:** Share intent/transaction contracts across human, API, workflow, and future agent producers;
  implement Graphical View production only.
- **FR-51:** Never mutate presentation, representation, projection, sheet, layout, route, or geometry
  outputs directly; re-derive all downstream artifacts.

### Non-Functional Requirements

- **NFR-1:** `.athena` remains M31 canonical semantic persistence.
- **NFR-2:** Theia remains an adapter and owns no source, identity, relationship, representation, or
  composition authority.
- **NFR-3:** Identical inputs produce deterministic authoring results.
- **NFR-4:** Stale previews never apply silently.
- **NFR-5:** Payloads remain transport-safe and frontend-independent.
- **NFR-6:** M27-M30 layout, mutation, interaction, and representation invariants do not regress.
- **NFR-7:** Visual density does not regress through wrappers, duplicates, labels, viewBox, or route fallback.
- **NFR-8:** Diagnostics identify failed authority and lifecycle stage.
- **NFR-9:** Gradle verification runs strictly sequentially on Windows.
- **NFR-10:** Repository text remains UTF-8; Chinese markdown retains UTF-8 BOM.
- **NFR-11:** Aggressive refactor may restore one authority path, but replaced paths are removed.
- **NFR-12:** Story completion requires AC-to-evidence mapping and polish/purge.
- **NFR-13:** Contracts use engineering model authoring terminology; documents remain projections.

### Additional Architecture Requirements

- Extend current `kernel/authoring-model`, `kernel/interaction-model`, `kernel/runtime`, compiler,
  LSP, and Theia boundaries; do not create a parallel platform.
- Authoring Capability entries belong to M29 `SemanticCapabilityRegistry`; electrical template
  instances belong to the electrical domain extension or its platform registry.
- Revision Guard is semantic snapshot id plus source URI, LSP document version, and SHA-256 of the
  exact UTF-8 document content.
- Semantic Authoring Transaction is runtime/audit state, not canonical engineering truth.
- Backend authoring protocol computes insertion spans and serializes source edits; Theia only applies
  returned editor edits as transport.
- Existing document projection policy owns sheet count, role, order, and stable identity. The M31
  profile exposes exactly control and field/device roles.
- Capability discovery precedes transaction creation.
- M31 v0 rejects empty or multi-intent transactions explicitly.
- Unexpected projection failure after commit does not roll back accepted semantic source.
- No new external framework, service, deployment topology, or `.athena` syntax is introduced.
- Existing frontend graph layout serialization and legacy authoring contracts are mandatory cleanup targets.

### UX Design Requirements

No separate M31 UX contract exists. The PRD user journeys and existing Athena workbench conventions
are authoritative: graphical-first action discovery, inspectable source/semantic preview, explicit
accept/reject, distinct lifecycle diagnostics, preserved sheet controls, semantic reveal coherence,
compact industrial density, transparent normal chrome, and structured proof over screenshots.

### FR Coverage Map

| FR | Epic | Coverage |
| --- | --- | --- |
| FR-1 | Epic 1 | Capability-based graphical action discovery |
| FR-2 | Epic 1 | Generic authoring intent families |
| FR-3 | Epic 1 | Actor, origin, context, and intent identity |
| FR-4 | Epic 1 | Mandatory preview |
| FR-5 | Epic 1 | Complete preview evidence |
| FR-6 | Epic 1 | No-mutation reject/cancel |
| FR-7 | Epic 2 | Concept-template entity creation |
| FR-8 | Epic 2 | Entity anatomy and nested ports |
| FR-9 | Epic 2 | Nested-port serialization |
| FR-10 | Epic 2 | Update contract readiness |
| FR-11 | Epic 2 | Removal dependency impact |
| FR-12 | Epic 2 | Dependency-blocked removal readiness |
| FR-13 | Epic 2 | Compatible terminal action discovery |
| FR-14 | Epic 2 | Relationship preview |
| FR-15 | Epic 2 | Semantic relationship mutation |
| FR-16 | Epic 2 | Flat/grouped canonical equivalence |
| FR-17 | Epic 2 | Group provenance boundary |
| FR-18 | Epic 2 | Relationship removal readiness |
| FR-19 | Epic 3 | Exactly two policy-owned sheets |
| FR-20 | Epic 3 | Stable sheet/occurrence identity |
| FR-21 | Epic 3 | Policy-resolved composition target |
| FR-22 | Epic 3 | Re-derived membership and placement |
| FR-23 | Epic 3 | Semantic cross-sheet reference |
| FR-24 | Epic 3 | Cabinet default and stable controls |
| FR-25 | Epic 3 | Representation policy resolution |
| FR-26 | Epic 3 | Concept/representation separation |
| FR-27 | Epic 3 | Terminal-anchor routing |
| FR-28 | Epic 3 | Explicit projection diagnostics |
| FR-29 | Epic 3 | Transparent normal chrome |
| FR-30 | Epic 3 | Derived framing |
| FR-31 | Epic 1 | Full lifecycle vocabulary |
| FR-32 | Epic 1 | Revision Guard stale blocking |
| FR-33 | Epic 1 | Pre-persistence validation |
| FR-34 | Epic 1 | Named STOP_DOWNSTREAM failure |
| FR-35 | Epic 1 | Commit/projection failure distinction |
| FR-36 | Epic 4 | Cross-surface reveal coherence |
| FR-37 | Epic 5 | Exact M31 sample project |
| FR-38 | Epic 4 | Complete graphical product workflow |
| FR-39 | Epic 5 | Structured proof inventory |
| FR-40 | Epic 5 | Screenshot as secondary evidence |
| FR-41 | Epic 5 | Per-story polish/purge |
| FR-42 | Epic 5 | Cleanup ledger ownership |
| FR-43 | Epic 2 | Backend-only source-edit planning |
| FR-44 | Epic 2 | Legacy authoring path removal |
| FR-45 | Epic 1 | First-class transaction v0 |
| FR-46 | Epic 1 | Single-intent cardinality |
| FR-47 | Epic 1 | Complete transaction envelope |
| FR-48 | Epic 1 | Existing capability registry extension |
| FR-49 | Epic 1 | Governed eligibility derivation |
| FR-50 | Epic 1 | Producer-neutral authoring contracts |
| FR-51 | Epic 3 | No direct downstream artifact mutation |

## Epic List

### Epic 1: Trustworthy Engineering Authoring Transactions

An engineer can discover eligible model-authoring actions, inspect one revision-safe semantic
transaction, accept or reject it, and receive explicit lifecycle outcomes without stale or hidden
mutation.

**FRs covered:** FR-1..FR-6, FR-31..FR-35, FR-45..FR-50.

### Epic 2: Create And Connect The Engineering Model

An engineer can create one governed semantic entity with nested ports and one compatible semantic
relationship through backend-owned source planning, while obsolete component/connect-port and
frontend serialization authorities are removed.

**FRs covered:** FR-7..FR-18, FR-43, FR-44.

### Epic 3: Stable Professional Two-Sheet Projection

An accepted engineering model deterministically projects into exactly two professional sheets with
stable identities, semantic cross-reference, governed representation, anchored routing, compact
chrome, and no hidden downstream editing.

**FRs covered:** FR-19..FR-30, FR-51.

### Epic 4: Complete Graphical Authoring Workflow

An engineer completes the create-connect-inspect-switch-reveal-close-reopen workflow from Graphical
View while Theia remains an adapter and all semantic decisions come from platform contracts.

**FRs covered:** FR-36, FR-38.

### Epic 5: Verified Customer Proof And Clean Closeout

A customer reviewer can open the exact M31 sample and inspect authoritative structured evidence and
secondary screenshot proof, while every story and final milestone close without stale or unowned
artifacts.

**FRs covered:** FR-37, FR-39..FR-42.

## Epic 1: Trustworthy Engineering Authoring Transactions

An engineer can discover eligible model-authoring actions, inspect one revision-safe semantic
transaction, accept or reject it, and receive explicit lifecycle outcomes without stale or hidden
mutation.

### Story 1.1: Extend Semantic Capability Discovery For Authoring

As an engineering author,
I want available authoring actions derived from the existing semantic capability registry,
So that Graphical View never invents what can be authored.

**Requirements:** FR-1, FR-2, FR-3, FR-48, FR-49, FR-50; NFR-2, NFR-3, NFR-5, NFR-13.

**Acceptance Criteria:**

**Given** the M29 `SemanticCapabilityRegistry` and a semantic subject or creation context
**When** domain authoring capability providers contribute create, update, remove, or relationship capabilities
**Then** the registry exposes typed Authoring Capability Evidence with stable capability id, intent kind, actor policy, domain eligibility, template requirement, projection context, and representation requirement
**And** no parallel authoring capability registry or frontend action-eligibility table is introduced.

**Given** a concept without valid semantic template or active-projection representation capability
**When** authoring actions are discovered
**Then** creation is absent or ineligible with `authoring.capability.unavailable`
**And** the diagnostic identifies the failed capability requirement.

**Given** the story implementation is functionally complete
**When** touched and adjacent capability, interaction, test, and documentation paths are reviewed
**Then** stale artifacts are removed or ledgered with owner, reason, target milestone, and verification
**And** the story records an AC-to-evidence mapping.
**And** the mandatory Polish/Purge Gate is complete.

### Story 1.2: Define Single-Intent Transaction And Revision Guard

As an engineering author,
I want each mutable action represented by one revision-safe transaction,
So that preview, validation, decision, and provenance cannot drift apart.

**Requirements:** FR-4, FR-5, FR-32, FR-45, FR-46, FR-47; NFR-3, NFR-4, NFR-5.

**Acceptance Criteria:**

**Given** one eligible mutable Semantic Action Intent
**When** Authoring Runtime creates a transaction
**Then** the transaction carries transaction id, one intent, capability evidence, Revision Guard, preview, validation, actor/provenance, decision, lifecycle, mutation/result fields, and diagnostics
**And** the transaction is serializable without Theia, representation, projection, or browser dependencies.

**Given** zero or more than one mutable intent
**When** transaction creation is requested
**Then** creation is blocked with `authoring.transaction.intent-count-unsupported`
**And** no preview or source edit is produced.

**Given** an Authoring Preview
**When** its Revision Guard is inspected
**Then** it contains semantic snapshot id, source URI, LSP document version, and SHA-256 of the exact UTF-8 content
**And** equivalent inputs produce deterministic transaction and guard output.

**Given** the story implementation is complete
**When** transaction and revision code, tests, docs, and compatibility paths are deeply reviewed
**Then** stale artifacts are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 1.3: Enforce Transaction Validation And Lifecycle Outcomes

As an engineering author,
I want explicit validation and lifecycle outcomes before and after commit,
So that stale, blocked, cancelled, compile-stopped, and projection-failed changes are never confused.

**Requirements:** FR-6, FR-31, FR-33, FR-34, FR-35; NFR-3, NFR-4, NFR-8.

**Acceptance Criteria:**

**Given** a mutable transaction
**When** runtime validates it
**Then** validation follows intent shape, capability evidence, actor/subject eligibility, Revision Guard, semantic rules, source planning, parser validation, semantic validation, and preview eligibility in order
**And** accepted mutation cannot bypass any stage.

**Given** source content or semantic snapshot differs from the Revision Guard
**When** accept is requested
**Then** lifecycle becomes stale with `authoring.preview.stale`
**And** source and all downstream projections remain unchanged.

**Given** proposed source requests `STOP_DOWNSTREAM`
**When** validation runs
**Then** the transaction is blocked with `authoring.validation.stop-downstream`
**And** the frontend can distinguish this from generic projection failure.

**Given** semantic mutation commits and reprojection later fails
**When** the result is published
**Then** mutation id and committed revision remain present with lifecycle `projection-failed`
**And** no renderer fallback is reported as success.

**Given** the story implementation is complete
**When** lifecycle, diagnostic, runtime, transport, and documentation paths are deeply reviewed
**Then** stale artifacts are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 2: Create And Connect The Engineering Model

An engineer can create one governed semantic entity with nested ports and one compatible semantic
relationship through backend-owned source planning, while obsolete component/connect-port and
frontend serialization authorities are removed.

### Story 2.1: Generalize Semantic Entity Authoring Contracts

As an EngineeringOS platform author,
I want generic entity authoring contracts and governed concept templates,
So that electrical components are the first specialization rather than the platform abstraction.

**Requirements:** FR-7, FR-10, FR-11, FR-12, FR-26, FR-44; NFR-1, NFR-5, NFR-11, NFR-13.

**Acceptance Criteria:**

**Given** current component-specific create/update authoring contracts
**When** M31 generic contracts are introduced
**Then** create, update, and remove entity intents use canonical subject/context, concept template, properties, Revision Guard, and provenance
**And** electrical concept template instances live in the electrical domain extension or platform registry.

**Given** an electrical Engineering Concept Template
**When** it is validated
**Then** it supplies semantic type, default model, governed properties, nested port names, directions, signals/media, and relationship capabilities
**And** it contains no representation primitive, SVG, style, anchor geometry, or viewBox.

**Given** update and entity-removal intent
**When** contract tests run
**Then** typed preview, capability, dependency-impact, blocked eligibility, and diagnostics are proven
**And** no customer-facing accepted update/removal workflow is claimed.

**Given** all consumers are migrated
**When** cleanup runs
**Then** replaced component-specific intent and transport names, stale tests, and misleading docs are removed
**And** the story records purge and AC evidence.
**And** the mandatory Polish/Purge Gate is complete.

### Story 2.2: Move Source Edit Planning Behind Backend Authority

As an engineering author,
I want all Athena source edits planned and serialized by the backend,
So that frontend code cannot become a competing language authority.

**Requirements:** FR-9, FR-33, FR-43; NFR-1, NFR-2, NFR-11.

**Acceptance Criteria:**

**Given** an eligible entity, relationship, or authored-layout transaction
**When** preview and acceptance request source impact
**Then** backend authoring protocol computes AST-aware insertion/replacement spans and serializes admitted `.athena` text
**And** returned edits carry the exact Revision Guard they require.

**Given** Theia receives a backend-generated source edit
**When** acceptance succeeds
**Then** Theia applies the edit through the editor bridge as transport only
**And** it performs no source parsing, insertion-position calculation, or `.athena` serialization.

**Given** existing graph layout source construction
**When** M31 migration completes
**Then** frontend serializer/build-source-edit functions and their stale tests are removed or replaced by backend protocol coverage
**And** no other frontend source serializer remains in the authoring path.

**Given** the story implementation is complete
**When** backend/frontend ownership, code, tests, and docs are deeply reviewed
**Then** stale authority paths are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 2.3: Create A Nested-Port Entity Through Governed Mutation

As a controls engineer,
I want to create one semantic device from a governed concept,
So that Athena writes valid compact source and projects the new engineering entity.

**Requirements:** FR-7, FR-8, FR-9, FR-25; NFR-1, NFR-3, NFR-6.

**Acceptance Criteria:**

**Given** an eligible electrical concept and creation context
**When** the creation transaction is previewed
**Then** preview shows canonical tag, type, model, nested ports, affected identities, exact backend source edit, composition target, representation preview, and eligibility
**And** rejection writes nothing.

**Given** the preview remains revision-current and valid
**When** acceptance is requested
**Then** Mutation Authority persists nested-port source, parser and semantic validation pass, and the new canonical entity appears after recompile
**And** legacy top-level device port declarations are not emitted.

**Given** duplicate identity, missing template, or invalid nested port anatomy
**When** preview is requested
**Then** acceptance is blocked with the stable authoring diagnostic
**And** no fallback entity or generic box becomes semantic success.

**Given** the story implementation is complete
**When** source generation, compiler, domain, test, and documentation paths are deeply reviewed
**Then** stale fixtures and paths are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 2.4: Create Semantic Relationships And Retire Legacy Connect Ports

As a controls engineer,
I want to create one compatible semantic relationship from canonical terminals,
So that relationship truth is governed independently of route geometry and source formatting.

**Requirements:** FR-13, FR-14, FR-15, FR-16, FR-17, FR-18, FR-44; NFR-1, NFR-2, NFR-11.

**Acceptance Criteria:**

**Given** two compatible canonical terminal subjects
**When** relationship capability is discovered and previewed
**Then** preview contains endpoint ids, relationship type, compatibility, source impact, route preview facts, Revision Guard, and diagnostics
**And** route geometry is evidence rather than relationship authority.

**Given** the preview is accepted
**When** backend mutation runs
**Then** `SemanticRelationshipIntent` persists the relationship and recompile yields the expected canonical edge
**And** equivalent flat and grouped `connect` source lower to the same canonical relationship.

**Given** incompatible endpoints
**When** preview or accept runs
**Then** mutation is blocked with `authoring.relationship.incompatible`
**And** source remains unchanged.

**Given** all consumers are migrated
**When** cleanup runs
**Then** `ConnectPortsIntent`, legacy transport names, duplicate tests, and stale docs are removed
**And** typed relationship-removal preview/validation readiness remains without claiming accepted removal UX.

**Given** the story implementation is complete
**When** relationship, routing, protocol, frontend, test, and documentation paths are deeply reviewed
**Then** stale artifacts are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 3: Stable Professional Two-Sheet Projection

An accepted engineering model deterministically projects into exactly two professional sheets with
stable identities, semantic cross-reference, governed representation, anchored routing, compact
chrome, and no hidden downstream editing.

### Story 3.1: Add The Two-Sheet Customer Projection Policy

As a controls engineer,
I want a control sheet and a field/device sheet derived from engineering policy,
So that document structure does not depend on source file count or frontend tabs.

**Requirements:** FR-19, FR-20, FR-24; NFR-3, NFR-6, NFR-13.

**Acceptance Criteria:**

**Given** the M31 customer projection profile
**When** Document Projection Policy is loaded
**Then** it exposes exactly control and field/device roles with deterministic order and identity recipe
**And** source file count does not affect sheet count.

**Given** unchanged semantic and policy input
**When** the model recompiles or reopens
**Then** sheet and Projection Occurrence identities remain stable
**And** no widget, DOM, or renderer identity enters the recipe.

**Given** Graphical View opens the M31 sample
**When** no explicit mode is selected
**Then** the accepted Cabinet default is preserved
**And** both governed sheet choices remain available after projection switches.

**Given** the story implementation is complete
**When** policy, document, transport, frontend, test, and docs are deeply reviewed
**Then** stale sheet assumptions are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 3.2: Re-Derive Representation Composition And Routing

As an engineering author,
I want accepted semantic changes reprojected through governed representation and spatial compilers,
So that the canvas cannot directly edit downstream engineering documents.

**Requirements:** FR-21, FR-22, FR-25, FR-26, FR-27, FR-28, FR-29, FR-30, FR-51; NFR-2, NFR-6, NFR-7.

**Acceptance Criteria:**

**Given** an accepted entity or relationship mutation
**When** reprojection runs
**Then** document membership, Representation Occurrence, composition, layout, route facts, Presentation IR, and geometry are freshly derived
**And** no authoring operation directly mutates any downstream artifact.

**Given** a created M31 entity
**When** representation binding runs
**Then** M30 policy selects the active symbol and labels from semantic/template facts
**And** Theia does not infer the symbol from the chosen action or DOM.

**Given** an accepted relationship
**When** routing runs
**Then** route endpoints use governed terminal anchors with zero center fallback
**And** missing anchor/representation/composition facts produce structured diagnostics rather than generic boxes.

**Given** normal rendering
**When** visual proof is inspected
**Then** wrappers and hitboxes are transparent, viewBox derives from actual bounds, and no duplicate occurrences or repeated labels exist
**And** interaction chrome appears only in transient states.

**Given** the story implementation is complete
**When** projection, representation, spatial, renderer, test, and docs are deeply reviewed
**Then** stale direct-mutation or fallback paths are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 3.3: Add Cross-Sheet Reference And Reopen Stability

As a controls engineer,
I want to follow one semantic relationship across the two document sheets,
So that the projected document remains coherent after save and reopen.

**Requirements:** FR-20, FR-23, FR-24, FR-36; NFR-3, NFR-6.

**Acceptance Criteria:**

**Given** one relationship spans control and field/device sheet occurrences
**When** document projection runs
**Then** one first-class semantic continuation or cross-reference occurrence links stable source and target occurrence ids
**And** display notation derives from governed document locations.

**Given** the cross-reference is activated
**When** the user reveals its target
**Then** Graphical View switches to the target sheet and canonical subject without guessing from label text
**And** source, Inspector, and Problems reveal targets remain coherent where available.

**Given** the project is closed and reopened with unchanged source
**When** compile and projection complete
**Then** semantic, relationship, sheet, occurrence, reference, and route identities match the pre-close proof
**And** the sheet selector remains usable.

**Given** the story implementation is complete
**When** reference, persistence, reveal, test, and docs are deeply reviewed
**Then** stale identities and assumptions are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 4: Complete Graphical Authoring Workflow

An engineer completes the create-connect-inspect-switch-reveal-close-reopen workflow from Graphical
View while Theia remains an adapter and all semantic decisions come from platform contracts.

### Story 4.1: Add Graphical Entity Creation Transaction UX

As a controls engineer,
I want to create a device from Graphical View and inspect its governed transaction,
So that graphical authoring remains transparent and semantic.

**Requirements:** FR-1, FR-3, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-31, FR-38, FR-45, FR-47, FR-48, FR-49, FR-50; NFR-2, NFR-4, NFR-7.

**Acceptance Criteria:**

**Given** an empty sheet or valid creation context
**When** the user requests available actions
**Then** Theia renders registry-discovered create actions for eligible concepts only
**And** no symbol palette item creates semantic identity by itself.

**Given** the user enters tag/model values
**When** preview is requested
**Then** the UI shows transaction identity, semantic changes, nested ports, source diff, diagnostics, composition/representation preview, and accept/reject controls
**And** normal engineering canvas density is not reduced by persistent cards or wrappers.

**Given** the user accepts or rejects
**When** the backend result returns
**Then** accepted source is applied as transport and rejected source remains unchanged
**And** the UI follows returned lifecycle state rather than inventing success.

**Given** the story implementation is complete
**When** UI, CSS, protocol, accessibility, test, and docs are deeply reviewed
**Then** stale creation widgets and visible wrapper regressions are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 4.2: Add Graphical Relationship And Reveal Workflow

As a controls engineer,
I want to connect compatible terminals and navigate the result across surfaces,
So that one canonical relationship remains understandable everywhere.

**Requirements:** FR-13, FR-14, FR-15, FR-16, FR-17, FR-27, FR-32, FR-36, FR-38; NFR-2, NFR-4, NFR-6.

**Acceptance Criteria:**

**Given** a selected source terminal
**When** the user requests relationship candidates
**Then** Theia displays capability-derived compatible targets and explicit rejected reasons
**And** endpoint identity comes from projection facts, never SVG coordinates or labels.

**Given** a compatible target is chosen
**When** preview and accept complete
**Then** the relationship transaction shows source impact and route preview, applies only the backend edit, and refreshes the anchored route after recompile
**And** cancel or stale state clears transient preview without mutation.

**Given** the new entity or relationship is selected
**When** reveal is requested
**Then** source, Outline nested port, Inspector, Problems, graph occurrence, and sheet target resolve through canonical identity where available
**And** missing targets return structured diagnostics.

**Given** the story implementation is complete
**When** relationship UX, selection, reveal, tests, and docs are deeply reviewed
**Then** stale frontend compatibility paths are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 4.3: Preserve Controls And Surface Lifecycle Diagnostics

As a controls engineer,
I want authoring controls and failures to remain understandable through view changes,
So that I never lose the document or mistake failure for success.

**Requirements:** FR-24, FR-31, FR-32, FR-33, FR-34, FR-35, FR-36, FR-38; NFR-6, NFR-7, NFR-8.

**Acceptance Criteria:**

**Given** the M31 project is open
**When** the user switches Cabinet, Wire, or other available projection modes and sheets
**Then** the sheet selector remains visible, exactly two policy sheets remain selectable, and focus/reveal state is preserved where meaningful
**And** no third sheet is inferred from source files.

**Given** a transaction becomes blocked, stale, cancelled, compile-stopped, committed, reprojected, or projection-failed
**When** Theia renders the result
**Then** the distinct lifecycle and recovery action are shown without generic `Projection unavailable` collapse
**And** controls remain usable after recovery.

**Given** the accepted workflow is completed and the IDE is reopened
**When** the project reloads
**Then** the same semantic and projection proof is available and no transient transaction is treated as canonical truth
**And** Outline contains nested ports for the created entity.

**Given** the story implementation is complete
**When** controls, state, lifecycle UI, smoke hooks, tests, and docs are deeply reviewed
**Then** stale control assumptions and races are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 5: Verified Customer Proof And Clean Closeout

A customer reviewer can open the exact M31 sample and inspect authoritative structured evidence and
secondary screenshot proof, while every story and final milestone close without stale or unowned
artifacts.

### Story 5.1: Create The M31 Customer Sample Project

As a customer-demo owner,
I want an exact rolling-shutter engineering model authoring sample,
So that M31 can be evaluated through a realistic two-sheet workflow.

**Requirements:** FR-37; NFR-1, NFR-6, NFR-7, NFR-13.

**Acceptance Criteria:**

**Given** `examples/m31/sample-project`
**When** its source and manifest are inspected
**Then** it uses admitted nested-port/grouped-connect syntax, Athena-owned concept/representation assets, and no QET runtime reference
**And** its initial model supports one eligible entity creation, one compatible relationship, two sheets, and one cross-reference.

**Given** the sample opens in Athena IDE
**When** compile and projection complete
**Then** the accepted Cabinet default and two governed sheet choices are present
**And** no fallback representation or center route exists.

**Given** the story implementation is complete
**When** sample source, manifest, README, fixture names, and adjacent milestone references are deeply reviewed
**Then** stale samples are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 5.2: Add Structured Product Smoke And Screenshot Guard

As a maintainer,
I want authoritative end-to-end proof of the customer workflow,
So that M31 completion does not depend on visual guessing or mocked integration.

**Requirements:** FR-38, FR-39, FR-40; NFR-3, NFR-4, NFR-6, NFR-7, NFR-8, NFR-9.

**Acceptance Criteria:**

**Given** the exact M31 sample and Electron product
**When** M31 smoke runs
**Then** it discovers capability, creates one single-intent entity transaction, previews/accepts nested source, creates one relationship transaction, verifies anchored route, switches sheets/modes, reveals subjects, closes/reopens, and verifies stable identities
**And** it exercises stale and blocked diagnostics.

**Given** structured proof output
**When** assertions run
**Then** they cover transaction, Revision Guard, source edit, semantic ids, nested ports, relationship endpoints, representation, composition, route anchors, sheet/reference ids, lifecycle, no direct downstream mutation, and no visual fallback regressions
**And** no assertion depends on DOM text as semantic authority.

**Given** visual evidence is captured
**When** the screenshot guard runs
**Then** the professional two-sheet surface is human-inspectable without wrappers, oversized empty canvas, or overlapping controls
**And** the screenshot remains secondary to structured proof.

**Given** the story implementation is complete
**When** smoke scripts, hooks, screenshots, test fixtures, and docs are deeply reviewed
**Then** stale proof paths and races are removed or ledgered and the AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 5.3: Run Final Purge Regression And Retrospective

As the Athena project owner,
I want M31 to close with verified regressions and no stale authoring architecture,
So that M32 starts from an accurate workspace.

**Requirements:** FR-41, FR-42, FR-43, FR-44; NFR-6, NFR-9, NFR-10, NFR-11, NFR-12.

**Acceptance Criteria:**

**Given** all M31 stories have implementation and fresh evidence
**When** final purge runs
**Then** it audits dead authoring contracts, frontend serializers, compatibility transports, stale tests, misleading docs, samples, screenshots, and design claims
**And** every retained/deferred item has owner, reason, target milestone, and verification.

**Given** final M31 code
**When** regression verification runs sequentially
**Then** relevant Gradle tests and M27, M28, M29, M30, and M31 product smokes pass or are explicitly migrated with evidence
**And** encoding audit and duplicate sprint-key checks pass.

**Given** the milestone closes
**When** retrospectives are published
**Then** each epic retrospective and the M31 retrospective record blockers, root causes, effective practices, cleanup outcomes, and concrete prevention actions
**And** sprint status contains no open item that was actually completed or unowned deferred gap.

**Given** final documentation is reviewed
**When** status is marked done
**Then** every story includes AC-to-evidence and polish/purge results, all stories and retrospectives are done, and M31 artifacts accurately match live code
**And** no `.tools` path is staged or committed.
**And** the mandatory Polish/Purge Gate is complete.
