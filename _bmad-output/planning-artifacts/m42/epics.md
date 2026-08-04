---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
  - step-03-create-stories
  - step-04-final-validation
status: final
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/addendum.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/m42-engineering-knowledge-system-design.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-04-m42/ARCHITECTURE-SPINE.md
---

# Athena M42 - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Athena M42, decomposing the final
PRD and architecture into dependency-ordered, implementable stories.

## Requirements Inventory

### Functional Requirements

- **FR-1:** Represent exact Quantity, Integer, Boolean, Text, Symbol, and Reference EngineeringValues.
- **FR-2:** Compile stable EngineeringEntity, Entity-owned EngineeringFunction, and EngineeringPort anatomy.
- **FR-3:** Assign functional, installation, and product/device structure independently from identity.
- **FR-4:** Compile typed EngineeringRelationships with named Participant Roles and admitted subject levels.
- **FR-5:** Keep package-defined Flow separate from Relationship, connectivity, routing, and geometry.
- **FR-6:** Replace active EngineeringComponent and generic EngineeringConnection authority directly.
- **FR-7:** Author Concepts, Parts, Capabilities, Relationships, Flows, formulas, and Constraints in package-local Athena Source.
- **FR-8:** Resolve Knowledge declarations deterministically by package plus qualified declaration name.
- **FR-9:** Separate vendor-neutral Concepts from optional Part Implementations and their facts.
- **FR-10:** Define Capability participation/provision/consumption without classification inference.
- **FR-11:** Govern Relationship participant levels, roles, cardinality, direction, Capabilities, properties, Flows, and connectivity admission.
- **FR-12:** Define bounded, deterministic, dimension-checked formulas and Constraints.
- **FR-13:** Publish one immutable EngineeringKnowledgeDocument for the resolved package set.
- **FR-14:** Resolve proof-scope project subjects against exactly one governed definition.
- **FR-15:** Derive explicit CapabilityProvision, CapabilityRequirement, and satisfaction evidence.
- **FR-16:** Evaluate typed formulas, comparisons, intervals, dimensions, cardinality, and all/any composition.
- **FR-17:** Publish one EngineeringValidationDocument with `READY`, `INCOMPLETE`, or `INVALID` state.
- **FR-18:** Publish plain Judgements and structured, non-executing Correction Options.
- **FR-19:** Preserve project/knowledge Provenance and deterministic impact evidence.
- **FR-20:** Keep provider choices, source edits, and engineering decisions human-owned.
- **FR-21:** Publish canonical JSON and JSON Schema for Knowledge and Validation Documents.
- **FR-22:** Transport identical typed Knowledge and Validation facts through runtime, LSP, and CLI.
- **FR-23:** Expose knowledge, validation, corrections, and source navigation in existing Theia surfaces.
- **FR-24:** Migrate Projection/Spatial consumers without moving knowledge, geometry, or paint authority.
- **FR-25:** Compile the cross-domain controlled conveyor as `READY`.
- **FR-26:** Prove materially different deterministic `INCOMPLETE` and `INVALID` outcomes.
- **FR-27:** Prove two contactor Part bindings preserve design intent and identity.
- **FR-28:** Propagate one motor rated-current change through all dependent engineering evidence.
- **FR-29:** Remove all retired active authority and pass closure audits without changing M0-M41 artifacts.

### NonFunctional Requirements

- **NFR-1:** Identical inputs produce byte-identical canonical documents, diagnostics, and Provenance using exact arithmetic.
- **NFR-2:** Missing, ambiguous, conflicting, corrupt, or dimension-invalid knowledge fails closed without precedence or guessing.
- **NFR-3:** Blocking diagnostics use plain engineering language with exact subject, expected/actual state, correction direction, and navigation.
- **NFR-4:** Public contracts remain implementation-neutral, closed, canonically specified, and explicitly version-rejecting.
- **NFR-5:** Formula and evaluation execution is pure, bounded, terminating, and isolated from arbitrary code, filesystem, network, and mutation.
- **NFR-6:** Kernel/shared contracts contain no electrical-specific Capability, Relationship, Flow, unit, terminal, structure, or rule constants.
- **NFR-7:** Kernel, runtime, LSP, CLI, Theia, Projection, and Spatial agree in rebuilt product E2E proof.

### Additional Requirements

- **AR-1 / AD-20:** One staged compiler owns parsing, lowering, knowledge compilation/resolution, evaluation, and inherited Reality transformations.
- **AR-2 / AD-21:** Add cohesive `knowledge-model`; keep model dependencies acyclic and obey survivor migration boundaries.
- **AR-3 / AD-22:** Engineering Reality owns project subjects/applications; Knowledge owns definitions; Validation owns outcomes.
- **AR-4 / AD-23:** Knowledge compilation may fail without a document; project evaluation always returns one Validation Document.
- **AR-5 / AD-24:** ANTLR remains sole semantic parser; Tree-sitter is editor-only and shares syntax fixtures plus rebuilt WASM.
- **AR-6 / AD-25:** Definition identity excludes package version; missing, duplicate, ambiguous, conflicting, or corrupt definitions fail closed.
- **AR-7 / AD-26:** Exact numbers use reduced `BigInteger` rationals; units/dimensions originate in packages; affine multiplication/division is restricted.
- **AR-8 / AD-27:** Formula/Constraint AST is a closed algebra with no scripts, loops, reflection, or arbitrary execution.
- **AR-9 / AD-28:** Relationship/Flow are independent; Function-to-Function cross-Entity participation and exact Port connectivity are first-class.
- **AR-10 / AD-29:** `READY`, `INCOMPLETE`, and `INVALID` have one total classification; `INVALID` publishes no satisfaction collection.
- **AR-11 / AD-30:** Correction Options are sealed data, never commands or automatic decisions.
- **AR-12 / AD-31:** Provenance uses relative source references and zero-based UTF-16 positions; LSP alone maps local URIs.
- **AR-13 / AD-32:** Committed schemas own transport shape; canonical bytes obey fixed key, Unicode, escape, array-order, and version-rejection rules.
- **AR-14 / AD-33:** Runtime atomically publishes one revision; TypeScript is schema-generated; Ajv is direct; digests are lowercase SHA-256 hex.
- **AR-15 / AD-34:** Projection receives resolved engineering facts only; Spatial alone derives route legs and geometry.
- **AR-16 / AD-35:** Delete component/part/connection/reuse/template modules and Semantic Macro meaning after same-story responsibility migration.
- **AR-17 / AD-36:** M42 runs locally in desktop/CLI with locked packages and no server, database, network authority, telemetry, or credentials.
- **AR-18 / AD-37:** Canonical codec precedes transport consumers; verification is sequential; frontend and product E2E are rebuilt; proof is M42-local.
- **AR-19 / AD-38:** Authored, Part, and derived fact sources stay distinct; typed disagreement is `INVALID`; Part assignment generates no anatomy.
- **AR-20 / AD-39:** Capability, classification, authoring availability, and Relationship participation remain separate contracts.
- **AR-21 / AD-40:** Compiler alone produces impact evidence; Constraint authority and external-standard evidence are typed and explicit.
- **AR-22:** Preserve inherited M39/M40 Reality, Projection composition, A1/B3 grid, Spatial geometry, and Presentation paint ownership.
- **AR-23:** Production `src/main` contains no Proof/Demo/Sample/milestone/V0/V1 architecture; run source-set hygiene audit.
- **AR-24:** Kotlin files group small related types by role and split mixed responsibilities near 200-300 lines.
- **AR-25:** Use CodeGraph before legacy-contract edits; run Gradle commands strictly sequentially on Windows.
- **AR-26:** Rendering, Pattern/AI, provider selection, procurement, lifecycle, registry workflow, simulation, and compatibility stay out of M42.
- **AR-27:** Closed M0-M41 artifacts remain immutable; active superseded code/tests/docs/examples are deleted in replacement stories.

### UX Design Requirements

No separate M42 UX contract. Product stories must reuse existing Theia Inspector, Problems, and
source-navigation patterns; show plain engineering facts without frontend inference; rebuild the
frontend; and capture M42-local E2E screenshots. Drawing Presentation/Rendering UX is M43.

### FR Coverage Map

- **FR-1:** Epic 1 - exact typed engineering values.
- **FR-2:** Epic 1 - Entity, Function, and Port anatomy.
- **FR-3:** Epic 1 - domain-neutral Structure Assignments.
- **FR-4:** Epic 1 - typed Relationships and Participant Roles.
- **FR-5:** Epic 1 - independent Flow meaning.
- **FR-6:** Epic 1 - direct Component/Connection authority replacement.
- **FR-7:** Epic 2 - package-local knowledge authoring.
- **FR-8:** Epic 2 - deterministic definition resolution.
- **FR-9:** Epic 2 - Concepts and Part Implementations.
- **FR-10:** Epic 2 - Capability contracts without classification.
- **FR-11:** Epic 2 - Relationship and Flow participation definitions.
- **FR-12:** Epic 2 - bounded typed formulas and Constraints.
- **FR-13:** Epic 2 - immutable Knowledge Document publication.
- **FR-14:** Epic 3 - project subject resolution.
- **FR-15:** Epic 3 - explicit Capability satisfaction.
- **FR-16:** Epic 3 - typed formula/Constraint evaluation.
- **FR-17:** Epic 3 - total Validation state classification.
- **FR-18:** Epic 3 - Judgements and Correction Options.
- **FR-19:** Epic 3 - Provenance and impact evidence.
- **FR-20:** Epic 3 - human-owned engineering decisions.
- **FR-21:** Epic 4 - canonical JSON and JSON Schema.
- **FR-22:** Epic 4 - runtime/LSP/CLI transport consistency.
- **FR-23:** Epic 4 - Theia inspection, Problems, and navigation.
- **FR-24:** Epic 4 - preserved Projection/Spatial/renderer boundaries.
- **FR-25:** Epic 5 - `READY` controlled-conveyor baseline.
- **FR-26:** Epic 5 - `INCOMPLETE` and `INVALID` proof.
- **FR-27:** Epic 5 - safe Part substitution proof.
- **FR-28:** Epic 5 - rated-current impact propagation proof.
- **FR-29:** Epic 5 - complete breaking migration and closure audit.

## Epic List

### Epic 1: Engineers Express Durable Engineering Intent

Engineers can author and compile exact values, Entities, Functions, Ports, structure context, typed
Relationships, and independent Flow meaning without legacy Component/Connection authority.

**FRs covered:** FR-1, FR-2, FR-3, FR-4, FR-5, FR-6

### Epic 2: Domain Maintainers Publish Governed Cross-Domain Knowledge

Domain maintainers can author package-local Concepts, Parts, Capabilities, participation rules,
formulas, and Constraints and publish one deterministic, inspectable Knowledge Document.

**FRs covered:** FR-7, FR-8, FR-9, FR-10, FR-11, FR-12, FR-13

### Epic 3: Engineers Validate Designs And Own Corrections

Engineers can see exactly whether a design is `READY`, `INCOMPLETE`, or `INVALID`, understand every
Judgement and affected fact, and retain control over all source and provider decisions.

**FRs covered:** FR-14, FR-15, FR-16, FR-17, FR-18, FR-19, FR-20

### Epic 4: Integrators And Engineers Consume One Canonical Result Everywhere

Open-source integrators and Athena users can consume the same immutable knowledge and validation
evidence through canonical files, runtime, LSP, CLI, Theia, Projection, and Spatial surfaces without
local inference or moved authority.

**FRs covered:** FR-21, FR-22, FR-23, FR-24

### Epic 5: Teams Prove And Close A Trustworthy Cross-Domain System

Teams can run one controlled-conveyor project through ready, incomplete, invalid, substitution, and
impact-change scenarios, then verify all active legacy authority is gone with milestone-local product
evidence.

**FRs covered:** FR-25, FR-26, FR-27, FR-28, FR-29

## Epic 1: Engineers Express Durable Engineering Intent

Engineers can author and compile exact values, Entities, Functions, Ports, structure context, typed
Relationships, and independent Flow meaning without legacy Component/Connection authority.

### Story 1.1: Author Exact Engineering Anatomy And Structure

As a multidisciplinary engineer,
I want to author exact values, Entities, Functions, Ports, and structure context,
So that my design intent has stable identity and domain-neutral meaning before any drawing or vendor implementation.

**Requirements:** FR-1, FR-2, FR-3, FR-6; NFR-1, NFR-2, NFR-6; AD-21, AD-22, AD-26, AD-35

**Acceptance Criteria:**

**Given** Athena Source containing Quantity, Integer, Boolean, Text, Symbol, and Reference values
**When** the compiler lowers the source into Engineering Reality
**Then** every value uses the typed exact contract, rational quantity conversion/comparison loses no precision, and incompatible dimensions fail with exact expected/actual dimensions and source Provenance
**And** kernel/shared contracts contain no domain-specific unit, terminal, structure, or rule constants.

**Given** one Entity with main and auxiliary Functions and exact Entity- or Function-owned Ports
**When** the source compiles
**Then** ownership, stable identity, direction, admitted Flow references, cardinality, and optional package-defined terminal/interface designation are inspectable
**And** Function ownership does not prohibit later cross-Entity Relationship participation.

**Given** functional, installation, or product/device Structure Assignments including package-defined `=`, `+`, or `-` display designations
**When** an engineer changes only the Structure Assignment
**Then** Entity identity, Concept reference, Function/Port identity, and semantic relationships remain unchanged
**And** `engineering-model` owns only assignment/reference mechanics.

**Given** active code that previously consumed `EngineeringComponent`
**When** this story installs EngineeringEntity anatomy
**Then** every touched active compiler/runtime/model consumer migrates directly, superseded Component authority and tests are deleted in the same story, and no alias, adapter, fallback, or dual model exists
**And** direct contract tests, source-set hygiene, encoding audit, and sequential Gradle verification pass.

### Story 1.2: Express Typed Relationships And Independent Flows

As a systems engineer,
I want semantic Relationships with named roles and separate Flow meaning,
So that controls, protects, supplies, and drives intent is unambiguous across engineering domains.

**Requirements:** FR-4, FR-5, FR-6; NFR-2, NFR-6, NFR-7; AD-28, AD-34, AD-35

**Acceptance Criteria:**

**Given** a Relationship definition admitting Entity, Function, or exact Port participants
**When** project source binds named Participant Roles
**Then** the compiled EngineeringRelationship preserves stable ID, package-qualified definition ID, typed properties, role names, subject levels, and Provenance
**And** Function-to-Function participation across different Entities and multi-participant Relationships are first-class.

**Given** a concrete connectivity Relationship and a dependency-style `protects` Relationship
**When** both compile
**Then** connectivity requires exact admitted Ports while `protects` requires no invented Port or connector
**And** package-defined Flow references remain separate from Relationship, routing, and geometry.

**Given** a multi-participant connectivity Relationship admitted to Projection
**When** Projection and Spatial compile it
**Then** Projection keeps one coordinate-free Relationship group and Spatial alone derives zero or more route legs and all route geometry
**And** neither layer imports knowledge/validation contracts or creates/splits engineering Relationships.

**Given** active `EngineeringConnection`, `connection-model`, routing, plugin, Projection, Spatial, runtime, or transport consumers
**When** typed Relationships replace their responsibilities
**Then** each affected active path migrates directly and superseded Connection authority is deleted without compatibility code
**And** M41 Projection/Spatial regression tests, CodeGraph blast-radius checks, source-set hygiene, and sequential Gradle verification pass.

## Epic 2: Domain Maintainers Publish Governed Cross-Domain Knowledge

Domain maintainers can author package-local Concepts, Parts, Capabilities, participation rules,
formulas, and Constraints and publish one deterministic, inspectable Knowledge Document.

### Story 2.1: Author Typed Knowledge In Package-Local Athena Source

As a domain knowledge maintainer,
I want to author typed cross-domain definitions in Athena Source,
So that engineering meaning lives in inspectable packages instead of Kotlin templates or metadata payloads.

**Requirements:** FR-7, FR-9, FR-10, FR-11, FR-12; NFR-2, NFR-5, NFR-6; AD-24, AD-26, AD-27, AD-39

**Acceptance Criteria:**

**Given** a Knowledge Package source file with one default `domain`
**When** ANTLR parses Concepts, Parts, Capabilities, Relationships, Flows, units/dimensions, formulas, and Constraints
**Then** the same Athena lexer/parser and source-span model used by project source produces sealed typed AST nodes
**And** filename forks, a second semantic parser, string formula evaluation, and engineering facts in the manifest are rejected.

**Given** formulas using `+`, `-`, `*`, `/`, `min`, `max`, `round-up`, comparisons `>`, `>=`, `<`, `<=`, `=`, `!=`, and intervals such as `[10, 20)`
**When** source validation runs
**Then** operands, dimensions, interval bounds, positive rounding quantum, and affine-unit restrictions are checked exactly
**And** scripts, loops, arbitrary functions, reflection, filesystem/network access, and hidden Kotlin calculations are impossible.

**Given** Concept, Part, Capability, and Relationship definitions
**When** their contracts are validated
**Then** Capability means participation/provision/consumption only, classification triggers no inference, Part facts do not generate authored anatomy, and Relationship definitions alone own roles, subject levels, cardinality, direction, required Capabilities, admitted Flows, logical pairing, and connectivity admission
**And** `AuthoringActionAvailability`, Capability evidence, and Relationship participation remain separate contracts.

**Given** accepted and rejected M42 syntax fixtures
**When** editor grammar work closes
**Then** Tree-sitter remains syntax/highlighting-only, regenerated parser/WASM and highlight/query tests agree with the shared fixtures, and the frontend bundle rebuilds
**And** active Kotlin/default templates, ad hoc `.properties` definition authority, and superseded grammar aliases touched by this story are deleted.

### Story 2.2: Compile One Deterministic Governed Knowledge Document

As a domain knowledge maintainer,
I want the resolved package set to compile into one deterministic Knowledge Document,
So that every project and tool consumes the same governed definitions and exact source evidence.

**Requirements:** FR-7, FR-8, FR-9, FR-10, FR-11, FR-12, FR-13; NFR-1, NFR-2, NFR-4; AD-23, AD-25, AD-35, AD-38

**Acceptance Criteria:**

**Given** a locked local package graph containing engineering-core, electrical, automation, and mechanical Knowledge Packages
**When** the knowledge compiler collects and resolves declarations
**Then** identity is package name plus qualified declaration name, package version appears only in Provenance, default-domain lookup filters imports and resolves exactly once, and package order never chooses a winner
**And** package-model/package-runtime retain only manifest, locked graph, dependency, discovery, and location mechanics.

**Given** complete valid definitions
**When** Knowledge compilation succeeds
**Then** one immutable EngineeringKnowledgeDocument contains deterministically ordered typed definitions, schemas, formulas, Constraints, typed authority, and package-relative Provenance
**And** it contains no project subjects, satisfaction, Judgements, Kotlin names, absolute paths, Theia fields, renderer contracts, or duplicate Engineering Reality graph.

**Given** a missing, duplicate, ambiguous, conflicting, corrupt, schema-invalid, or dimension-invalid declaration
**When** Knowledge compilation runs
**Then** it returns deterministic package diagnostics and no authoritative Knowledge Document
**And** no precedence, fallback definition, partial document, or compatibility behavior is available.

**Given** surviving responsibilities from `component-model`, `part-model`, `reuse-model`, `template-model`, legacy knowledge loaders, and Semantic Macro definition/catalog/template paths
**When** current knowledge contracts and package sources are installed
**Then** surviving non-macro responsibilities migrate to their architecture-owned modules and superseded active modules, settings, consumers, tests, docs, and examples are deleted in the same story
**And** plugin APIs contain no Component/Connection/Part knowledge contributor or copied replacement DTO.

## Epic 3: Engineers Validate Designs And Own Corrections

Engineers can see exactly whether a design is `READY`, `INCOMPLETE`, or `INVALID`, understand every
Judgement and affected fact, and retain control over all source and provider decisions.

### Story 3.1: Evaluate Governed Requirements And Design State

As a multidisciplinary engineer,
I want governed knowledge evaluated against my canonical project subjects,
So that I know exactly which requirements are satisfied and whether the design is ready, incomplete, or invalid.

**Requirements:** FR-14, FR-15, FR-16, FR-17; NFR-1, NFR-2, NFR-5; AD-20, AD-23, AD-27, AD-29, AD-38

**Acceptance Criteria:**

**Given** canonical Entities, Part bindings, Capabilities, Relationships, authored Requirement/Constraint applications, and one Knowledge Document
**When** compiler-owned knowledge resolution runs
**Then** every proof-scope subject resolves exactly one governed definition with stable subject/definition references
**And** missing or ambiguous resolution names exact project and Knowledge Package sources without guessing.

**Given** authored facts, Part implementation facts, and compiler derivations
**When** evaluation addresses the same typed contract
**Then** fact sources remain distinct, equal normalized facts may corroborate, disagreement becomes `INVALID` with source-specific Provenance, and no source wins by precedence
**And** Part binding never creates authored Functions or Ports.

**Given** Capability provisions/requirements and typed formula/Constraint applications
**When** the closed evaluator runs
**Then** every Requirement receives one explicit `SATISFIED`, `UNSATISFIED`, or `UNRESOLVED` result and existence, equality, ordering, interval, dimensional, cardinality, and required all/any rules evaluate exactly and deterministically
**And** the compiler verifies authored provider choices but never selects or creates providers.

**Given** ready, missing-provider/protection, failed-well-formed blocking Constraint, malformed Relationship, ambiguous definition, corrupt package, and incompatible-dimension fixtures
**When** project evaluation completes
**Then** one immutable EngineeringValidationDocument classifies each as `READY`, `INCOMPLETE`, or `INVALID` under AD-29
**And** `INVALID` contains diagnostics/Provenance but no satisfaction claim or completed Constraint Judgement collection.

### Story 3.2: Explain Judgements, Corrections, Provenance, And Impact

As an engineer responsible for design decisions,
I want plain explanations, exact source evidence, and non-executing correction options,
So that I can understand consequences and choose every engineering change myself.

**Requirements:** FR-18, FR-19, FR-20; NFR-1, NFR-3; AD-30, AD-31, AD-40

**Acceptance Criteria:**

**Given** a failed or unresolved evaluation
**When** the Validation Document is published
**Then** every Judgement names exact subjects, problem, expected and actual typed values, governing authority, correction direction, and project/knowledge source in plain engineering language
**And** internal codes and transport fields remain secondary metadata.

**Given** proof-required correction cases
**When** correction evidence is derived
**Then** sealed `ChangeValue`, `AddRelationship`, `SelectExistingProvider`, `BindPart`, and `AddRequiredFunction` options reference affected source subjects and ordered supporting evidence
**And** eligible providers include only existing project Entities in stable-ID order and no option mutates source, selects a Part, creates a subject, or connects a provider.

**Given** project and package sources contributing to a result
**When** Provenance is serialized in the Validation model
**Then** it uses normalized relative source references, rejects absolute/`..` paths, carries package/version where applicable, uses zero-based UTF-16 spans, and records ordered subject/definition/formula/Constraint derivation steps
**And** only LSP may map these portable references to local file URIs.

**Given** one governed motor rated-current input change
**When** compiler evaluation reruns from identical surrounding inputs
**Then** stable impact evidence identifies every changed/unchanged Requirement, formula result, Constraint result, explanation, and Correction Option in deterministic order
**And** runtime, semantic diff/review/SCM, LSP, CLI, and frontend may query/render but never reconstruct impact or copy `EngineeringKnowledgeState`; the superseded state contract is deleted.

## Epic 4: Integrators And Engineers Consume One Canonical Result Everywhere

Open-source integrators and Athena users can consume the same immutable knowledge and validation
evidence through canonical files, runtime, LSP, CLI, Theia, Projection, and Spatial surfaces without
local inference or moved authority.

### Story 4.1: Consume Stable Canonical Knowledge And Validation Contracts

As an open-source tool integrator,
I want stable canonical JSON, schemas, versions, and digests,
So that I can consume Athena knowledge and validation without Kotlin, filesystem, Theia, or renderer dependencies.

**Requirements:** FR-21; NFR-1, NFR-4; AD-32, AD-33, AD-37

**Acceptance Criteria:**

**Given** committed Knowledge and Validation JSON Schemas
**When** a document is validated
**Then** each closed schema declares dialect, stable `$id`, numeric `schemaVersion`, required fields, `additionalProperties: false`, exact numerator/denominator strings, and one closed `x-athena-order` for every array
**And** committed schemas remain transport-shape authority while Kotlin mappings and generated TypeScript conform without reflection-derived or parallel DTOs.

**Given** any valid Knowledge or Validation Document
**When** the canonical codec emits bytes
**Then** output follows AD-32 exactly for UTF-8/BOM/whitespace, unsigned UTF-16 key order, Unicode preservation/rejection, escaping, and every array-order category
**And** repeated identical inputs plus cross-language golden/property fixtures produce byte-identical output.

**Given** canonical document bytes
**When** a digest is published
**Then** it is lowercase hexadecimal SHA-256 over exactly those bytes
**And** no runtime, LSP, CLI, or frontend consumer hashes decoded or reserialized objects.

**Given** a supported or unsupported schema version and valid/invalid transport shape
**When** Kotlin or TypeScript consumers receive the document
**Then** supported versions validate before use while unsupported versions and unknown fields fail explicitly with no fallback, partial decode, compatibility inference, or parallel payload
**And** Ajv is a direct frontend dependency and `json-schema-to-typescript` generated files are never edited.

### Story 4.2: Publish One Atomic Result Through Runtime And Open Interfaces

As a tool integrator,
I want runtime, CLI, LSP, and downstream realities to consume one atomic compilation revision,
So that every non-UI product boundary agrees without inventing engineering meaning.

**Requirements:** FR-22, FR-24; NFR-4, NFR-7; AD-21, AD-31, AD-33, AD-34

**Acceptance Criteria:**

**Given** one successful or failed project evaluation
**When** runtime publishes a compilation revision
**Then** Engineering, Knowledge, and Validation snapshots/digests appear atomically and readers never observe mixed revisions
**And** runtime caches remain digest-keyed, replaceable, and observationally identical to uncached compilation.

**Given** CLI and LSP requests for a full document or subject-indexed slice
**When** adapters answer
**Then** CLI serializes canonical documents directly and LSP uses kernel queries tied to document digests while preserving every typed value, identity, role, state, Judgement, correction, and Provenance field
**And** neither adapter resolves definitions, evaluates rules, guesses source paths, or defines a handwritten payload.

**Given** current EngineeringEntity/Relationship inputs
**When** Projection and Spatial compile
**Then** inherited M39/M40/M41 identity, A1/B3 grid, grouping, geometry, and paint boundaries remain green with no knowledge-model dependency or renderer repair
**And** M42 adds no label/style/grid/routing/export or Presentation improvement.

### Story 4.3: Inspect Validation Evidence And Retire Semantic Macro Surfaces

As an Athena engineer,
I want existing IDE surfaces to explain the atomic knowledge and validation revision,
So that I can inspect and navigate engineering evidence without a legacy macro workflow or frontend inference.

**Requirements:** FR-23; NFR-3, NFR-4, NFR-7; AD-24, AD-31, AD-33, AD-35

**Acceptance Criteria:**

**Given** a selected Entity, Function, Port, Relationship, Requirement, or diagnostic in Theia
**When** Inspector, Problems, and source navigation render the current atomic revision
**Then** users see Concept, Functions, Capabilities, Part binding, satisfaction, Participant Roles, plain failure text, Correction Options, and exact project/Knowledge Package source navigation
**And** schema mismatch/stale revision is rejected visibly, Ajv validates transport, and frontend performs no engineering inference.

**Given** old Semantic Macro runtime/LSP/frontend catalog, validation, preview, acceptance, mutation, and UI paths
**When** current Inspector, Problems, navigation, and authoring responsibilities are installed
**Then** macro meaning and dedicated protocols/UI are deleted without renamed shells; generic preview/acceptance survives only under independent authoring contracts with real non-macro consumers
**And** authoring-model never imports knowledge-model or copied Capability/Part definitions.

**Given** the updated editor grammar, generated schemas/types, and Theia adapters
**When** this story closes
**Then** Tree-sitter/query, Ajv/schema, TypeScript, Inspector/Problems/navigation, stale-revision, and frontend integration tests pass after rebuilding parser/WASM and the frontend bundle
**And** source-set hygiene, encoding, sequential Gradle tests, active-doc/example audits, and M42-local IDE E2E screenshots pass.

**Given** M42 scope boundaries
**When** product UI is reviewed
**Then** no dashboard, ontology browser, AI chat, new interaction mode, or drawing Presentation behavior exists
**And** M43 remains owner of knowledge-aware drawing UX and rendering.

## Epic 5: Teams Prove And Close A Trustworthy Cross-Domain System

Teams can run one controlled-conveyor project through ready, incomplete, invalid, substitution, and
impact-change scenarios, then verify all active legacy authority is gone with milestone-local product
evidence.

### Story 5.1: Prove Controlled-Conveyor Engineering Intelligence

As a mechatronics engineer,
I want one realistic cross-domain conveyor to prove ready and failure behavior,
So that I can trust Athena's knowledge system before using its results for engineering decisions.

**Requirements:** FR-25, FR-26, FR-27, FR-28; NFR-1, NFR-2, NFR-3, NFR-6, NFR-7; AD-29, AD-37, AD-40

**Acceptance Criteria:**

**Given** `examples/m42/controlled-conveyor` and governed engineering-core, electrical, automation, and mechanical Knowledge Packages
**When** the baseline compiles
**Then** supply, breaker Q1, contactor KM1, overload protection, motor M1, controller PLC1, and conveyor CV1 resolve with complete project/package Provenance and zero blocking diagnostics as `READY`
**And** PLC1 controls KM1, Q1 protects M1, KM1 supplies switched power to M1, and M1 drives CV1 through distinct typed Relationships and Capabilities without kernel domain constants.

**Given** required protection removed or a well-formed undersized protection Part bound
**When** evaluation reruns
**Then** state is `INCOMPLETE`, the exact unsatisfied Requirement and subjects are named, expected/actual typed values and governing package policy are shown, and structured non-executing corrections are offered
**And** no unsupported standard-compliance claim appears.

**Given** a malformed Relationship or incompatible-dimension value
**When** evaluation reruns
**Then** state is `INVALID`, deterministic diagnostics and Provenance identify the unsafe input, and no satisfaction or validated-state claim is published
**And** the invalid result is materially distinct from incomplete outcomes on every product surface.

**Given** either of two governed contactor Part definitions
**When** the engineer swaps the Part binding
**Then** Entity identity, Concept, Functions, Capabilities, and semantic Relationships remain unchanged while implementation facts and resulting Constraint outcomes remain traced
**And** Athena performs no automatic selection, equivalence ranking, pricing, stock, or procurement behavior.

**Given** one governed M1 rated-current change
**When** compilation repeats from identical surrounding inputs
**Then** Requirement derivation, Capability matching, Constraint results, impact evidence, explanation, and Correction Options update deterministically while unchanged facts stay stable
**And** kernel, canonical JSON, runtime, LSP, CLI, Inspector, Problems, Projection, and Spatial agree on every proof subject and state.

### Story 5.2: Close M42 With Current Authority And Product Evidence

As an Athena maintainer,
I want executable closure audits and rebuilt product evidence,
So that M42 finishes with one current architecture and no hidden legacy or milestone-only production code.

**Requirements:** FR-29; NFR-1 through NFR-7; AD-35, AD-36, AD-37

**Acceptance Criteria:**

**Given** the named M42 superseded-contract inventory
**When** active source, settings, dependencies, tests, docs, and examples are audited
**Then** retired Component/Connection models, five retired modules, `EngineeringKnowledgeState`, legacy loaders/`.properties`, Semantic Macro meaning, compatibility aliases/adapters/fallbacks, and stale active examples/docs are absent
**And** independently useful semantic diff/review/SCM and generic authoring primitives remain only under current non-macro contracts with real consumers.

**Given** kernel/shared production source
**When** cross-domain and source-set hygiene audits run
**Then** no electrical-specific Capability, Relationship, Flow, unit, terminal/interface, structure aspect/designation, or rule constant exists and no Proof/Demo/Sample/milestone/V0/V1 production architecture exists
**And** Kotlin organization, CodeGraph consumer migration, and no-copied-DTO boundaries pass inspection.

**Given** the final controlled-conveyor scenarios
**When** all affected JVM, runtime, LSP, CLI, Tree-sitter, generated TypeScript, Theia frontend, Projection, and Spatial surfaces are rebuilt and tested
**Then** Gradle commands run strictly sequentially, repository/unit/integration/schema/frontend tests pass, M41 regression remains green, and product E2E visibly proves ready/incomplete/invalid, inspection, Problems, navigation, substitution, and impact behavior
**And** screenshots and evidence are written only under `_bmad-output/implementation-artifacts/m42/`.

**Given** repository text and historical milestone artifacts
**When** final encoding, diff, source-set, and artifact-scope audits run
**Then** UTF-8 rules pass, closed M0-M41 planning/implementation artifacts are unchanged, all M42 story records and sprint status are complete, and no rendering/Pattern/AI/procurement/lifecycle/registry work entered scope
**And** the milestone is eligible to move every story and epic to `done` only after passing evidence is recorded.
