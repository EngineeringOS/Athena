---
stepsCompleted:
  - m28-fast-path-epic-breakdown
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m28/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m28/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m28/ARCHITECTURE-SPINE.md
---

# Athena M28 - Epic Breakdown

## Overview

M28 turns Athena from a professional semantic viewer into a governed authoring system. The work is
sequenced to avoid architecture drift:

1. Admit compact nested component anatomy syntax across both parsers.
2. Preserve canonical semantic identities and source provenance.
3. Introduce generic semantic relationship authoring, with electrical connection as the first
   specialization.
4. Route accepted authoring through M8 mutation authority and today's `.athena` serialization.
5. Prove the whole product path in Theia and purge stale artifacts.

## Requirements Inventory

### Functional Requirements

FR1: Admit nested `port <name> { ... }` declarations inside `device` blocks.
FR2: Lower nested ports to canonical `port:Device.port` identities with correct provenance.
FR3: Make nested ports the canonical authoring style for M28 examples, docs, and mutation output.
FR4: Keep top-level `port Device.port` temporarily accepted as legacy-compatible syntax.
FR5: Provide an openable `examples/m28/sample-project` using nested ports.
FR6: Preserve M27 visual density and sheet behavior while adding authoring overlays.
FR7: Select semantic subject candidates from Graphical View without DOM/SVG identity inference.
FR8: Create a governed `SemanticRelationshipIntent` with electrical specialization.
FR9: Validate relationship compatibility before preview or persistence.
FR10: Validate persistence eligibility before source mutation.
FR11: Show transient relationship preview using M27 spatial/routing contracts.
FR12: Explain persistence impact before acceptance.
FR13: Accept relationship through M8 mutation authority.
FR14: Serialize accepted electrical relationship to `.athena` `connect A.p -> B.q`.
FR15: Recompile and reproject after mutation.
FR16: Surface compatibility diagnostics in preview and inspector.
FR17: Preserve source, graph, inspector, document selector, Problems, and Semantic SCM coherence.
FR18: Add product-path authoring smoke.
FR19: Add structured mutation, parser, compiler, runtime, LSP, and frontend assertions.
FR20: Publish usage, retrospective, and cleanup ledger.

### Non-Functional Requirements

NFR1: Preserve Athena authority chain from source to compiler to projection to Theia and back
through mutation authority.
NFR2: Theia remains downstream; it never owns source writes or relationship truth.
NFR3: No freehand wire geometry.
NFR4: Nested ports are the only deliberate M28 source syntax admission.
NFR5: Mutation and projection must be deterministic.
NFR6: Product-path proof is required.
NFR7: Dirty/invalid source safety is mandatory.
NFR8: Aggressive internal refactor is allowed only when it improves architecture and cleanup follows.
NFR9: Gradle verification commands run sequentially on Windows.
NFR10: M27 visual density regressions must stay blocked.
NFR11: ANTLR4 and Tree-sitter parser parity is mandatory.
NFR12: Semantic Interaction Compiler boundary is reserved for M29.

## Epic List

### Epic 1: Compact Component Anatomy Language Admission

Athena authors ports where engineers expect them: inside the owning device. The milestone admits
nested ports across ANTLR4, Tree-sitter, AST, lowering, source spans, diagnostics, and compatibility
with legacy top-level ports.

**FRs covered:** FR1, FR2, FR3, FR4, FR19.

### Epic 2: M28 Sample And Semantic Relationship Contract

Athena exposes a generic relationship authoring boundary and an M28 sample that proves electrical
connection is only the first specialization.

**FRs covered:** FR5, FR7, FR8, FR9, FR10, FR16.

### Epic 3: Governed Graphical Relationship Authoring

Theia lets users select semantic subjects, preview valid or invalid relationships, explain source
impact, and accept valid electrical relationships through M8 mutation authority.

**FRs covered:** FR6, FR7, FR8, FR9, FR10, FR11, FR12, FR13, FR14, FR15, FR16, FR17, FR19.

### Epic 4: Product Proof, Documentation, Retrospective, And Cleanup

M28 is accepted only after product-path smoke, structured assertions, usage docs, retrospective, and
stale artifact purge.

**FRs covered:** FR18, FR19, FR20.

## Epic 1: Compact Component Anatomy Language Admission

### Story 1.1: Admit Nested Device-Owned Ports In ANTLR And AST

As an Athena author,
I want to write ports inside the owning device block,
So that component anatomy is compact and straightforward.

**Acceptance Criteria:**

Given a source file with `device D { port p { direction in signal Digital } }`
When the ANTLR parser and parse adapter run
Then the source parses without syntax errors
And the authored AST represents `p` as a nested first-class `PortDeclaration` owned by `D`.

Given a malformed nested port
When parsing fails
Then diagnostics point at the nested port source span instead of treating the block as a property.

### Story 1.2: Preserve Canonical Port Identity In Lowering And Semantic Index

As an Athena compiler engineer,
I want nested ports to lower to the existing `port:Device.port` identity,
So that routing, presentation, references, and diagnostics do not learn a second identity scheme.

**Acceptance Criteria:**

Given `device D { port p { ... } }`
When compiler lowering and semantic indexing run
Then the canonical identity is `port:D.p`
And source provenance points to the nested port block/name span.

Given both `device D { port p { ... } }` and `port D.p { ... }` exist
When semantic validation runs
Then Athena emits a governed duplicate-identity diagnostic.

### Story 1.3: Admit Nested Ports In Tree-Sitter And Syntax UX

As an Athena IDE user,
I want nested ports to parse and highlight in Tree-sitter,
So that source UX matches compiler syntax.

**Acceptance Criteria:**

Given nested device-owned port source
When Tree-sitter corpus tests run
Then the nested `port` block appears as syntax structure, not a property assignment.

Given the same nested source is opened in the IDE
When highlighting/navigation use syntax data
Then nested ports receive port declaration treatment and no parser parity drift exists.

### Story 1.4: Canonicalize M28 Examples And Docs Around Nested Anatomy

As an Athena maintainer,
I want M28 examples and docs to use nested ports,
So that the canonical style is clear while old fixtures remain temporarily accepted.

**Acceptance Criteria:**

Given M28 sample and usage docs
When they are reviewed
Then new authored ports use nested `device { port ... }` syntax
And docs state that top-level `port D.p` is legacy-compatible, not canonical.

## Epic 2: M28 Sample And Semantic Relationship Contract

### Story 2.1: Create Openable M28 Sample Project

As an engineer,
I want an M28 sample project with nested ports and candidate relationships,
So that authoring can be proven through the product path.

**Acceptance Criteria:**

Given `examples/m28/sample-project`
When opened in Theia Graphical View
Then it projects without errors
And includes one compatible unconnected electrical endpoint pair plus two invalid relationship pairs.

### Story 2.2: Define SemanticRelationshipIntent And Electrical Specialization

As an Athena platform engineer,
I want a generic relationship intent with an electrical specialization,
So that M28 does not hard-code ECAD vocabulary as the platform architecture.

**Acceptance Criteria:**

Given selected semantic subjects
When relationship authoring creates an intent
Then the intent is represented as `SemanticRelationshipIntent`
And M28 sets relationship type to `ElectricalConnectionRelationship`.

Given future relationship types are named in tests or docs
When architecture is reviewed
Then flow, containment, control, and communication can fit without renaming the intent boundary.

### Story 2.3: Validate Relationship Compatibility And Persistence Eligibility

As an Athena user,
I want invalid relationships blocked before persistence,
So that the source graph cannot be corrupted by the canvas.

**Acceptance Criteria:**

Given compatible output-to-input electrical subjects
When compatibility validation runs
Then the relationship may proceed to preview.

Given output-to-output, input-to-input, signal mismatch, duplicate connection, ambiguous owner, or
dirty/invalid source state
When validation runs
Then persistence is blocked with a governed diagnostic and source remains unchanged.

## Epic 3: Governed Graphical Relationship Authoring

### Story 3.1: Select Semantic Subjects In Graphical View

As an Athena user,
I want to select relationship candidates from the sheet,
So that graphical authoring starts from semantic identity rather than SVG geometry.

**Acceptance Criteria:**

Given visible ports or presentation terminals in Graphical View
When relationship mode is active
Then selecting them resolves canonical semantic subject ids from projection facts
And Theia does not use DOM text or SVG coordinates as identity.

### Story 3.2: Preview Relationship Route And Source Impact

As an Athena user,
I want to see route preview and source impact before accepting,
So that I understand what will change.

**Acceptance Criteria:**

Given a valid electrical relationship intent
When preview is requested
Then Theia shows a transient route preview, compatibility state, route quality, and proposed
`.athena` `connect A.p -> B.q` serialization target.

Given preview is canceled or source reloads
When the UI refreshes
Then preview state is removed and no hidden relationship truth remains.

### Story 3.3: Accept Relationship Through M8 Mutation Authority

As an Athena user,
I want accepted relationships to persist through governed mutation,
So that `.athena` remains today's source of truth.

**Acceptance Criteria:**

Given a valid preview
When the user accepts it
Then Theia sends the semantic relationship intent to M8 mutation authority
And does not edit source text directly.

Given mutation succeeds
When Athena persists the accepted electrical relationship
Then source contains a deterministic `connect A.p -> B.q` statement.

### Story 3.4: Recompile, Reproject, And Preserve Coherence

As an Athena user,
I want accepted relationships to reappear as normal projection facts,
So that preview state cannot masquerade as committed engineering state.

**Acceptance Criteria:**

Given an accepted mutation
When compiler validation and projection refresh complete
Then the new relationship appears as committed route facts with terminal anchors and route quality
And source, Problems, Semantic SCM, inspector, sheet selector, and graph remain coherent.

## Epic 4: Product Proof, Documentation, Retrospective, And Cleanup

### Story 4.1: Product-Path M28 Authoring Smoke

As an Athena maintainer,
I want a product smoke that proves accepted and rejected authoring flows,
So that M28 is not accepted on unit tests alone.

**Acceptance Criteria:**

Given the M28 sample opens in Theia
When the smoke runs
Then one valid electrical relationship is accepted through the UI path
And two invalid relationship attempts are rejected without source mutation.

### Story 4.2: Structured Parser, Mutation, And Projection Assertions

As an Athena maintainer,
I want structured assertions at parser, compiler, runtime, LSP, and frontend seams,
So that M28 failures are diagnosable without visual guessing.

**Acceptance Criteria:**

Given M28 tests run
When parser, lowering, relationship intent, mutation, projection refresh, and frontend preview are
exercised
Then each seam has deterministic assertions and no test uses DOM text as semantic authority.

### Story 4.3: M28 Usage, Retrospective, And Cleanup Ledger

As an Athena maintainer,
I want accurate M28 docs and a cleanup ledger,
So that future milestones do not inherit stale claims or dead design paths.

**Acceptance Criteria:**

Given M28 implementation is complete
When docs and retrospective are published
Then they explain nested anatomy, semantic relationship authoring, electrical specialization,
canonical semantic persistence, `.athena` serialization today, and M29 interaction boundary.

Given cleanup runs
When stale artifacts are found
Then they are removed or recorded with owner, reason, and target milestone.
