---
stepsCompleted:
  - m29-fast-path-requirements-extraction
  - m29-fast-path-epic-breakdown
  - m29-fast-path-story-generation
  - m29-fast-path-validation
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m29/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m29/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m29/ARCHITECTURE-SPINE.md
---

# Athena M29 - Epic Breakdown

## Overview

M29 makes interaction a governed semantic contract. It prevents Theia, GLSP, SVG, DOM, or widget
state from becoming interaction authority by introducing a platform-owned Interaction IR,
Semantic Action Intent primitive, Semantic Capability Registry, Interaction Compiler v0, and
Interaction Runtime boundary. The milestone proves the layer through reveal/navigation, M28
semantic relationship mutation cleanup, and one semantic entity creation proof.

## Requirements Inventory

### Functional Requirements

FR1: Introduce a platform-owned Interaction IR model independent of Theia.
FR2: Model canonical subjects, roles, surfaces, affordances, semantic action intents, commands,
previews, reveal targets, command lifecycle, diagnostics, and provenance.
FR3: Interaction subjects carry canonical semantic identity plus projection occurrence context when
available.
FR4: Interaction IR distinguishes durable semantic identity from transient frontend state.
FR5: Interaction IR models select, hover, focus, reveal, preview, accept, reject, and mutate action
families.
FR6: Interaction IR includes diagnostics for unresolved subjects, unsupported actions, invalid
command state, and mutation-ineligible commands.
FR7: Interaction IR includes lifecycle states: requested, discovered, validated, previewing,
accepted, rejected, mutation-pending, committed, reprojected, blocked, stale, and cancelled.
FR8: Interaction IR includes provenance fields for actor, origin surface, reason, timestamp when
available, and confidence when available.
FR9: Build a Semantic Capability Registry / Interaction Subject Index from compiled semantic model
and projection facts.
FR10: Subject index includes components, ports, connections, routes, document sheet occurrences,
reference markers, diagnostics, and source ranges where available.
FR11: Interaction Compiler v0 discovers available actions from facts and capability policy, not
frontend widget structure.
FR12: Compiler preserves source/projection/inspector/problem reveal coherence for the same
canonical subject.
FR13: Compiler exposes product-safe payloads through LSP/runtime transport.
FR14: Compiler does not mutate source or projection state directly.
FR15: Interaction indexes refresh on projection refresh and bind to the current active `.athena`
source context.
FR16: Interaction IR preserves standard, symbol, and presentation metadata when present, but does
not create or own standards meaning.
FR17: Selecting component, port, connection, route, reference marker, or diagnostic resolves to an
Interaction subject.
FR18: User can reveal the subject in source, graph, inspector, and Problems where that target
exists.
FR19: Reveal uses governed source ranges and projection occurrence facts.
FR20: Missing reveal targets degrade with explicit diagnostics rather than guessing.
FR21: Reveal state remains stable when switching document sheets or projection modes.
FR22: M28 semantic relationship mutation becomes an Interaction command, not a Theia-only mode.
FR23: Relationship subject selection continues to use projection facts and canonical port identity.
FR24: `SemanticRelationshipIntent` remains the mutation contract for accepted relationship
mutation.
FR25: Legacy `connect-ports` frontend paths are removed, migrated, or recorded in a retention
ledger.
FR26: Invalid relationship accepts remain blocked at backend mutation/source-edit gate.
FR27: Relationship preview remains transient and clears on cancel, source reload, projection
refresh, or accepted mutation.
FR28: Provide one governed semantic entity creation proof through Interaction IR, with component
insertion as first example.
FR29: Insertion flow starts from Interaction action discovery.
FR30: Insertion preview shows source impact and affected semantic identities.
FR31: Acceptance flows through existing runtime/mutation authority and returns governed source edit.
FR32: Generated component anatomy uses nested ports.
FR33: Recompile/reproject shows inserted component in source, semantic inspection, projection
facts, and graph view.
FR34: Rejected component insertion leaves source and projection state unchanged.
FR35: Component insertion is modeled as semantic entity creation first; symbol placement is a
projection consequence.
FR36: Include `examples/m29/sample-project`.
FR37: Sample proves reveal/navigation, relationship mutation through Interaction IR, and semantic
entity creation proof.
FR38: Theia product smoke opens M29 sample and verifies structured interaction proof payloads.
FR39: Include structured assertions at model, compiler/runtime, LSP, frontend, and product-smoke
seams.
FR40: Finish with usage docs, retrospective, and cleanup ledger.

### Non-Functional Requirements

NFR1: Interaction semantics are deterministic for identical source and projection inputs.
NFR2: Theia remains an adapter and may not persist semantic interaction truth.
NFR3: No DOM text, SVG geometry, CSS class, or widget id may become semantic authority.
NFR4: Interaction payloads are transport-safe and stable enough for future non-Theia adapters.
NFR5: M27 visual density guarantees must not regress.
NFR6: M28 source-backed mutation guarantees must not regress.
NFR7: Gradle verification commands run sequentially on Windows.
NFR8: M29 produces a cleanup ledger for removed or retained stale interaction paths.
NFR9: M29 does not introduce IEC/QElectroTech/EPLAN symbol-library expansion or visual equivalence.
NFR10: M29 preserves the option for future AI, workflow, and API producers to use the same
Semantic Action Intent contract as human frontend adapters.

### Additional Requirements

- Interaction contracts should live outside Theia, initially in `kernel/interaction-model` unless
  implementation proves a lower-risk placement.
- The Semantic Capability Registry is keyed by canonical subject identity plus projection
  occurrence context, not frontend object ids.
- Interaction Compiler v0 derives actions and reveal payloads only; it never mutates source or
  projection state.
- Accepted commands cross existing authoring/runtime/source-edit gates.
- Interaction Runtime owns session lifecycle, preview state, diagnostics, and provenance, not
  durable semantic truth.
- LSP/runtime transport carries product-safe Interaction payloads; GLSP/SVG/Theia remain adapters.
- Entity creation creates semantic entities first; graph symbols are projection consequences.
- Standards and visual fidelity metadata may pass through Interaction IR but standards/library
  expansion is deferred.
- Product proof uses structured payload assertions before UI click assertions.
- `INTERACTION-CONTRACT.md` is binding for M29 story work: it defines core shapes, conversion rules,
  lifecycle state machine, preview ownership, LSP envelope, diagnostic codes, legacy path inventory,
  and structured proof payloads.
- Every M29 story has a mandatory final polish/purge gate: before marking the story done, review the
  workspace for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design
  notes, and unused compatibility paths introduced or exposed by that story. Remove them, or record
  retained items with owner, reason, and target milestone.

### UX Design Requirements

No standalone UX design contract is an input for M29. UX requirements are limited to adapter
behavior required by the PRD: stable reveal/navigation, non-authoritative UI affordances, transient
preview state, and no regression of M27 graph density.

### FR Coverage Map

FR1-FR8: Epic 1 - Governed Interaction Contract.
FR9-FR16: Epic 2 - Semantic Capability Registry And Interaction Compiler.
FR17-FR21: Epic 3 - Semantic Reveal And Navigation.
FR22-FR27: Epic 4 - Semantic Relationship Mutation Cleanup.
FR28-FR35: Epic 5 - Semantic Entity Creation Proof.
FR36-FR40: Epic 6 - Product Proof, Documentation, Retrospective, And Cleanup.

## Epic List

### Epic 1: Governed Interaction Contract

Athena gains a platform-owned Interaction IR and Semantic Action Intent contract that can be
consumed by human UI, future AI agents, workflow clients, and APIs without making Theia the owner of
meaning.

**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR7, FR8.

### Epic 2: Semantic Capability Registry And Interaction Compiler

Athena can discover available actions and reveal targets from semantic/projection facts instead of
frontend widget structure.

**FRs covered:** FR9, FR10, FR11, FR12, FR13, FR14, FR15, FR16.

### Epic 3: Semantic Reveal And Navigation

Users can select engineering subjects and reveal the same canonical identity across source, graph,
inspector, and Problems without DOM/SVG guessing.

**FRs covered:** FR17, FR18, FR19, FR20, FR21.

### Epic 4: Semantic Relationship Mutation Cleanup

M28 relationship mutation becomes an Interaction command while preserving `SemanticRelationshipIntent`
and backend mutation/source-edit authority.

**FRs covered:** FR22, FR23, FR24, FR25, FR26, FR27.

### Epic 5: Semantic Entity Creation Proof

Athena proves that interaction can create semantic entities through governed mutation, with component
insertion as the first example and nested ports as generated anatomy.

**FRs covered:** FR28, FR29, FR30, FR31, FR32, FR33, FR34, FR35.

### Epic 6: Product Proof, Documentation, Retrospective, And Cleanup

M29 is accepted only after the sample, structured proof payloads, regression checks, usage docs,
retrospective, and stale path cleanup ledger are complete.

**FRs covered:** FR36, FR37, FR38, FR39, FR40.

## Epic 1: Governed Interaction Contract

### Story 1.1: Define Platform-Owned Interaction IR Models

As an Athena platform engineer,
I want Interaction IR types outside Theia,
So that interaction meaning is governed by Athena instead of frontend widgets.

**Acceptance Criteria:**

**Given** M29 implementation starts
**When** the interaction model boundary is added
**Then** it defines subjects, surfaces, occurrences, affordances, reveal targets, commands, previews,
diagnostics, lifecycle state, and provenance
**And** no type depends on Theia, DOM, SVG, GLSP widget ids, or frontend CSS classes.
**And** the model covers the required fields in `INTERACTION-CONTRACT.md`.

**Given** a subject has projection context
**When** it is represented in Interaction IR
**Then** canonical semantic identity and projection occurrence context are distinct fields
**And** transient frontend metadata cannot become the subject identity.

### Story 1.2: Introduce Semantic Action Intent Primitive

As an Athena runtime maintainer,
I want a Semantic Action Intent primitive below UI gestures,
So that future human, AI, workflow, and API producers can share one command contract.

**Acceptance Criteria:**

**Given** a reveal, preview, or mutate-capable action is discovered
**When** it becomes executable
**Then** the payload carries or derives a Semantic Action Intent with action family, subject,
lifecycle state, and provenance
**And** human gesture names remain adapter-level metadata.
**And** accepted mutation-capable commands map through `InteractionCommand` to the existing
`AuthoringIntent` contracts named in `INTERACTION-CONTRACT.md`.

**Given** an AI or API producer is represented in tests
**When** it requests an action
**Then** the contract does not require hover, click, DOM node, SVG node, or Theia widget concepts.

### Story 1.3: Add Lifecycle, Provenance, And Diagnostic Contract Tests

As an Athena maintainer,
I want lifecycle/provenance/diagnostic contracts tested,
So that interaction flows are deterministic and inspectable.

**Acceptance Criteria:**

**Given** Interaction IR contract tests run
**When** lifecycle states are serialized or compared
**Then** requested, discovered, validated, previewing, accepted, rejected, mutation-pending,
committed, reprojected, blocked, stale, and cancelled are covered.
**And** illegal transitions produce `interaction.command.invalid-state`.

**Given** unresolved subjects, unsupported actions, invalid command state, and mutation-ineligible
commands
**When** diagnostics are produced
**Then** each has a stable code and transport-safe payload.

## Epic 2: Semantic Capability Registry And Interaction Compiler

### Story 2.1: Build Semantic Capability Registry From Facts

As an Athena compiler engineer,
I want a registry of semantic subjects and capabilities from model/projection facts,
So that action discovery starts from engineering reality.

**Acceptance Criteria:**

**Given** a compiled workspace with components, ports, connections, routes, sheet occurrences,
reference markers, diagnostics, and source ranges
**When** the registry is built
**Then** those subjects are indexed by canonical semantic identity plus projection occurrence context
where available
**And** frontend ids are stored only as adapter metadata if present.
**And** keys follow the `InteractionSubjectKey` and `InteractionOccurrenceKey` contract.

**Given** standards, symbol, or presentation metadata exists
**When** subjects are indexed
**Then** metadata is preserved as metadata
**And** the registry does not infer or own standards meaning.

### Story 2.2: Discover Interaction Actions From Registry And Policy

As an Athena user,
I want available actions discovered from facts and policy,
So that the UI offers valid commands without inventing meaning.

**Acceptance Criteria:**

**Given** a registered component, port, connection, route, sheet occurrence, reference marker, or
diagnostic
**When** Interaction Compiler v0 evaluates it
**Then** available actions include only supported reveal, inspect, preview, or mutation command
families for that subject
**And** unsupported actions return structured diagnostics.

**Given** action discovery runs
**When** source or projection state changes
**Then** the compiler does not mutate source or projection facts directly.

### Story 2.3: Expose Product-Safe Interaction Payloads Through Runtime Transport

As a frontend adapter implementer,
I want product-safe Interaction payloads over the existing runtime/LSP boundary,
So that Theia can consume interaction semantics without local inference.

**Acceptance Criteria:**

**Given** Interaction Compiler v0 has produced subjects and actions
**When** LSP/runtime transport exposes them
**Then** payloads are deterministic, JSON-safe, and contain no direct frontend object authority.
**And** payloads use `InteractionEnvelope(schemaVersion="m29.interaction.v1")`.

**Given** projection refresh occurs
**When** the active `.athena` source context changes
**Then** interaction indexes refresh or mark stale state explicitly.

## Epic 3: Semantic Reveal And Navigation

### Story 3.1: Resolve Selected Subjects To Interaction Subjects

As an Athena user,
I want graph/source/problem/inspector selections to resolve to semantic subjects,
So that I can navigate engineering identity rather than screen objects.

**Acceptance Criteria:**

**Given** a user selects a component, port, connection, route, reference marker, or diagnostic
**When** the adapter requests subject resolution
**Then** it receives an Interaction subject from canonical identity and projection/source facts
**And** no DOM text, SVG geometry, or CSS class is used as authority.

**Given** a selected frontend element lacks governed semantic payload
**When** resolution is attempted
**Then** the result is an unresolved-subject diagnostic.

### Story 3.2: Reveal Subjects Across Source, Graph, Inspector, And Problems

As an Athena user,
I want one selected subject to reveal across workbench surfaces,
So that source, projection, diagnostics, and inspection stay coherent.

**Acceptance Criteria:**

**Given** a subject has source range, graph occurrence, inspector payload, or problem diagnostic
**When** reveal is requested
**Then** the correct reveal target is returned for every available surface
**And** missing targets are reported without guessed navigation.

**Given** a document sheet or projection mode is switched
**When** the same canonical subject still exists
**Then** reveal remains stable through refreshed projection occurrence context.

### Story 3.3: Adapt Theia Reveal UI To Interaction IR

As an Athena maintainer,
I want Theia reveal behavior to consume Interaction IR,
So that frontend code no longer owns reveal meaning.

**Acceptance Criteria:**

**Given** Theia graph, source, inspector, or Problems UI requests reveal
**When** M29 code handles the request
**Then** it uses Interaction payloads from runtime/LSP
**And** previous local reveal inference is removed, migrated, or recorded in the cleanup ledger.

## Epic 4: Semantic Relationship Mutation Cleanup

### Story 4.1: Route Relationship Mode Through Interaction Command Discovery

As an Athena user,
I want relationship mutation to start from interaction actions,
So that connection authoring is no longer a Theia-only mode.

**Acceptance Criteria:**

**Given** relationship mode starts from a port subject
**When** M29 discovers commands
**Then** the available relationship command comes from Interaction IR
**And** the underlying target remains `SemanticRelationshipIntent(ElectricalConnectionRelationship)`.

**Given** a non-port subject is selected for the M28 electrical relationship flow
**When** action discovery runs
**Then** the command is unavailable with a structured diagnostic.

### Story 4.2: Preserve Backend Mutation And Source-Edit Gates

As an Athena maintainer,
I want accepted relationships to keep using existing mutation authority,
So that interaction does not create a second source write path.

**Acceptance Criteria:**

**Given** a valid relationship command is accepted
**When** it crosses the runtime boundary
**Then** it serializes through `SemanticRelationshipIntent` and existing source-edit/mutation gates
**And** Theia does not edit `.athena` text directly.
**And** new M29 code does not call `ConnectPortsIntent` directly.

**Given** an invalid relationship accept is attempted
**When** backend validation runs
**Then** mutation remains blocked and source is unchanged.

### Story 4.3: Clear Previews And Ledger Legacy Connect-Ports Paths

As an Athena maintainer,
I want stale relationship preview and legacy paths cleaned up,
So that M29 does not inherit hidden frontend authority.

**Acceptance Criteria:**

**Given** relationship preview is active
**When** cancel, source reload, projection refresh, or accepted mutation occurs
**Then** preview state clears and cannot masquerade as committed semantic truth.

**Given** legacy `connect-ports` paths remain
**When** cleanup is complete
**Then** each path is removed, migrated, or listed with owner, reason, and target milestone.

## Epic 5: Semantic Entity Creation Proof

### Story 5.1: Discover Insert Semantic Entity Action

As an Athena user,
I want component insertion to begin as a semantic action,
So that I create engineering entities rather than dragging symbols.

**Acceptance Criteria:**

**Given** the user is in a valid graph, system, or empty-sheet context
**When** action discovery runs
**Then** an insert semantic entity action is available for one known component concept
**And** the action describes the semantic parent/context rather than a screen coordinate as truth.

### Story 5.2: Preview Semantic Entity Creation Source Impact

As an Athena user,
I want to inspect insertion source impact before accepting,
So that I know what semantic entity will be created.

**Acceptance Criteria:**

**Given** insert semantic entity action is requested
**When** preview is built
**Then** it lists proposed source edit target, component identity, nested ports, and affected
semantic identities
**And** rejection leaves source and projection state unchanged.
**And** source impact is generated by backend runtime/source-edit logic rather than Theia string
concatenation.

### Story 5.3: Accept Nested-Port Component Creation Through Mutation Authority

As an Athena user,
I want accepted component insertion to persist through governed mutation,
So that `.athena` remains today's single source of truth.

**Acceptance Criteria:**

**Given** a valid insert preview is accepted
**When** runtime/mutation authority applies it
**Then** source receives deterministic nested-port component anatomy
**And** no direct Theia source edit bypass exists.
**And** component insertion maps to `CreateComponentIntent` unless a reviewed replacement is added.

**Given** recompile/reproject completes
**When** source, semantic inspection, projection facts, and graph view refresh
**Then** the inserted component appears as a semantic entity and rendered occurrence.

## Epic 6: Product Proof, Documentation, Retrospective, And Cleanup

### Story 6.1: Create Openable M29 Sample Project

As an Athena maintainer,
I want an M29 sample project,
So that reveal, relationship mutation, and semantic entity creation can be tested through one
product path.

**Acceptance Criteria:**

**Given** `examples/m29/sample-project`
**When** it is opened in Theia
**Then** it compiles and projects without semantic validation blocking downstream projection
**And** it contains subjects suitable for reveal, relationship mutation, and insertion proof.

### Story 6.2: Add Structured Interaction Product Smoke

As an Athena maintainer,
I want product smoke based on structured proof payloads,
So that M29 verification does not rely on visual guessing.

**Acceptance Criteria:**

**Given** M29 smoke runs
**When** it opens the sample
**Then** it verifies nonzero subject index counts, reveal payloads, relationship command discovery,
entity creation preview/accept/reject proof, and no DOM text authority.
**And** it covers the structured proof payload inventory from `INTERACTION-CONTRACT.md`.

**Given** UI click smoke is present
**When** it runs
**Then** it is secondary to structured payload assertions and does not replace them.

### Story 6.3: Publish M29 Usage, Retrospective, Cleanup Ledger, And Regression Checks

As an Athena maintainer,
I want the milestone closed with accurate docs and cleanup,
So that future milestones do not inherit stale design or dead code.

**Acceptance Criteria:**

**Given** M29 implementation is complete
**When** docs are published
**Then** usage explains Interaction IR, Semantic Action Intent, reveal/navigation, relationship
mutation cleanup, semantic entity creation proof, and deferred standards visual fidelity.

**Given** cleanup runs
**When** stale code/docs/design paths are found
**Then** they are removed or recorded in the M29 cleanup ledger.

**Given** verification runs on Windows
**When** Gradle tasks are needed
**Then** they run sequentially and M27/M28 regression contracts pass or are explicitly migrated.
