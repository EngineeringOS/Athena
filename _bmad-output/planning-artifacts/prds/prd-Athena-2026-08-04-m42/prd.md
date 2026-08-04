---
title: M42 Engineering Knowledge System
status: final
created: 2026-08-04
updated: 2026-08-04
---

# PRD: M42 Engineering Knowledge System

## 0. Document Purpose

This PRD defines M42 product outcomes, boundaries, and acceptance evidence for product, architecture,
UX, epic, and story workflows. Its Glossary is normative. Features contain globally numbered
Functional Requirements (FRs), while success metrics and closure gates define milestone proof.
Implementation mechanisms, migration evidence, and rejected alternatives live in `addendum.md` and
`m42-engineering-knowledge-system-design.md`.

## 1. Vision

M42 establishes Athena as an Engineering Knowledge System capable of validating Engineering
Reality, not merely representing engineering objects. Engineers author durable engineering meaning:
Entities, Functions, Ports, typed Relationships, properties, constraints, and optional Part
bindings. Governed Knowledge Packages define Concepts, Capabilities, valid relationship
participation, typed calculations, and constraints. Athena then explains why a design is `READY`,
`INCOMPLETE`, or `INVALID`.

Knowledge is a cross-cutting authority, not another Reality domain, graph, database, or project
model. Engineering Reality remains canonical owner of project subjects. The Knowledge System
supplies governed definitions during resolution and evaluates those canonical subjects without
copying them or creating another mutation path.

M42 proves this direction with one controlled-conveyor system spanning electrical, automation, and
mechanical meaning. Electrical rules provide depth. Automation and mechanical participation prove
that kernel contracts are not electrical-only. Presentation improvement remains M43 work.

### 1.1 Why Now

M39 established Reality ownership. M40 established composition inside Projection Reality. M41
established Spatial Reality and downstream representation evidence. Continuing directly into
rendering polish would improve appearance while leaving engineering knowledge fragmented across
Kotlin, string values, package metadata, generic Connections, and Semantic Macro templates. M42
corrects that upstream authority before M43 turns validated meaning into professional documents.

## 2. Target Users

### 2.1 Primary User

The primary user is a multidisciplinary machine or systems engineer who must understand and change
electrical, automation, and mechanical design intent without manually reconciling disconnected
drawings, properties, and vendor data.

### 2.2 Supporting Users

- Domain knowledge maintainers who publish governed Concepts, Parts, Capabilities, Relationships,
  formulas, and Constraints in Athena source.
- Tool integrators who consume stable, implementation-neutral Knowledge and Validation documents.
- Reviewers who need plain engineering explanations, exact source provenance, and deterministic
  correction options.

### 2.3 Jobs To Be Done

- Express what an engineering subject is intended to do independently from how it is drawn or which
  vendor Part implements it.
- State and inspect semantic Relationships such as controls, protects, supplies power, and drives.
- Know whether required Capabilities are satisfied and why.
- Change an engineering rating or Part binding and see every affected requirement, judgement,
  explanation, and correction without manual recalculation.
- Navigate every resolved fact or failure back to project source and governed knowledge source.
- Reuse the same canonical facts across runtime, CLI, IDE, Projection Reality, and Spatial Reality.

### 2.4 Non-Users In M42

- Engineers seeking automatic solution generation, provider selection, or AI-authored fixes.
- Procurement and operations users seeking pricing, stock, ordering, commissioning, maintenance, or
  PhysicalAsset lifecycle.
- Drawing specialists seeking improved labels, styling, grid chrome, routing, or export.
- Ontology administrators seeking a universal taxonomy or graph database.

### 2.5 Key User Journey

**UJ-1. Maya validates and corrects a controlled conveyor after a motor change.**

Maya is a mechatronics engineer working in Athena's Theia desktop product. She opens
`examples/m42/controlled-conveyor`, whose electrical supply, breaker, contactor, overload, motor,
automation controller, and mechanical conveyor compile as `READY`. Inspector shows each selected
Entity's Concept, Functions, provided and required Capabilities, Part binding, and satisfaction.
Relationship inspection shows that PLC1 controls KM1, Q1 protects M1, KM1 supplies switched power to
M1, and M1 drives CV1.

Maya changes M1's rated current in Athena source. Athena recompiles deterministically, marks the
undersized protection result, names Q1 and M1, shows expected and actual typed values, explains the
governed rule, and links both project and knowledge provenance. Problems offers non-executing
correction options. Maya chooses the engineering decision herself, edits the source or Part binding,
and recompiles. The design returns to `READY` without Entity identity or semantic Relationships
changing.

If Maya removes the protector, the model remains valid but `INCOMPLETE`. If she authors a malformed
Relationship or dimensionally incompatible value, validation becomes `INVALID` and publishes no
claim that the Engineering Reality is validated.

**UJ-2. Nikhil publishes governed domain knowledge.**

Nikhil maintains an Athena Knowledge Package. He authors a Concept, Capability, Relationship, Part,
and Constraint in package-local Athena Source, then compiles the resolved package set. Athena either
publishes a deterministic Knowledge Document with definition Provenance or blocks publication with
exact ambiguity, formula, schema, or dimension diagnostics. Registry publication, organizational
approval, and deprecation workflow remain outside M42.

**UJ-3. Elena integrates validation evidence without Athena internals.**

Elena builds an open-source tool against Athena's canonical JSON Schemas. She consumes Knowledge and
Validation documents, follows stable subject and definition references, and presents state and
Judgements without Kotlin, filesystem, Theia, or renderer dependencies. A schema-version mismatch or
unknown-field-policy violation fails explicitly instead of producing partial inferred behavior.

## 3. Product Thesis And Authority

### 3.1 Authority Chain

```text
governed Knowledge Packages -> Engineering Knowledge System
                                      |
Athena source -> Semantic Graph -> Engineering Reality
                                      |
                                      +-> Knowledge Evaluation
                                                |
                                                v
                                  Validated Engineering Reality
                                                |
                                                v
                                     Projection Reality
                                                |
                                                v
                                      Spatial Reality
                                                |
                                                v
                                      existing renderer
```

Knowledge definitions may resolve knowledge-backed meaning during Engineering Reality construction.
Validation evidence governs whether that Reality is accepted, incomplete, or invalid. Neither step
owns or duplicates project subjects.

### 3.2 Separate Knowledge And Validation Contracts

- `EngineeringKnowledgeDocument` is the immutable compiled contract for governed definitions from
  the resolved Knowledge Package set.
- `EngineeringValidationDocument` is the immutable project-specific evaluation contract over
  canonical Engineering Reality.
- `Validated Engineering Reality` is an architectural state: canonical Engineering Reality paired
  with a `READY` or `INCOMPLETE` Validation Document. It is not a third model or database.

### 3.3 Product Principles

- Human-first source expresses engineering meaning, never compiler IR, transport fields, paint
  mechanics, or diagnostic codes.
- Domain packages own domain meaning. Kernel contracts remain cross-domain.
- Ambiguity and conflict fail closed. Package order and fallback never guess engineering truth.
- Corrections inform engineers but never edit source automatically.
- Standards claims require exact evidence; demonstration policy never masquerades as compliance.
- Closed milestone artifacts remain history. Active product code and documentation use only current
  M42 vocabulary.

## 4. Glossary

The human term before parentheses is normative PRD prose. The parenthetical identifier is its public
contract name, not a second concept.

- **Athena Source** - Human-authored, canonical project or Knowledge Package source. The single source
  of truth for engineering metadata.
- **Engineering Reality** - Canonical compiled project subjects and authored/resolved project facts.
  Owns Entities, Functions, Ports, Relationships, and their identity.
- **Engineering Knowledge System** - Cross-cutting system that compiles governed definitions and
  evaluates Engineering Reality. It owns neither project subjects nor rendering.
- **Knowledge Package** - Package containing package-local Athena Source definitions and a manifest
  for identity and dependencies. A Knowledge Package is governed when the deterministic resolved
  package graph selects it and its manifest, dependencies, and definitions compile without blocking
  diagnostics; M42 governance does not include registry, approval, or deprecation workflow.
- **Knowledge Document (`EngineeringKnowledgeDocument`)** - Immutable, implementation-neutral
  compiled definitions from a resolved Knowledge Package set.
- **Validation Document (`EngineeringValidationDocument`)** - Immutable, implementation-neutral
  project evaluation containing state, subject references, satisfaction, judgements, corrections,
  and provenance.
- **Validated Engineering Reality** - Engineering Reality paired with a `READY` or `INCOMPLETE`
  Validation Document; a validated state, not a duplicate model.
- **Concept (`EngineeringConcept`)** - Vendor-neutral definition of expected functional anatomy,
  property schema, Capabilities, and permitted Relationship participation.
- **Entity (`EngineeringEntity`)** - Authored project instance with stable semantic identity and
  exactly one resolved Concept in the M42 proof.
- **Function (`EngineeringFunction`)** - Entity-owned system role or partition. Ownership establishes
  identity and containment; Relationships may bind Functions across Entities.
- **Port (`EngineeringPort`)** - Exact Entity or Function interface with direction, admitted Flow
  definitions, cardinality, and optional terminal or interface designation.
- **Structure Assignment** - Domain-neutral assignment that provides functional, installation, or
  product context and display address without replacing semantic identity.
- **Part (`PartImplementation`)** - Optional vendor realization assigned to an Entity. It supplies
  implementation facts but does not own Entity identity or design intent.
- **CapabilityDefinition** - Governed definition of an engineering ability to participate, provide, or
  consume. It is not classification.
- **CapabilityProvision** - Evidence that an Entity or Function provides a Capability.
- **CapabilityRequirement** - Evidence that an Entity or Function requires a Capability.
- **CapabilitySatisfaction** - Explicit result linking a Requirement to qualifying Provision evidence,
  or recording why it is unsatisfied or unresolved.
- **Relationship (`EngineeringRelationship`)** - Typed semantic relation with package-qualified
  definition identity, named participant roles, typed properties, and provenance.
- **Participant Role** - Governed role played by an Entity, Function, or Port in a Relationship.
- **Flow** - Typed meaning that passes through or is carried by a Relationship. Flow is separate from
  Relationship and does not imply rendering.
- **Engineering Value (`EngineeringValue`)** - Typed value: exact Quantity, Integer, Boolean, Text,
  Symbol, or Reference.
- **Constraint** - Governed, deterministic engineering condition over typed operands.
- **Judgement** - Evaluation result containing status, exact subjects, expected and actual values,
  explanation, and provenance.
- **Correction Option** - Structured, non-executing possible correction linked to affected source
  subjects and evidence.
- **Provenance** - Stable trace to project source, Knowledge Package source, package version, and
  derivation or evaluation evidence.
- **Projection Occurrence (`ProjectionOccurrence`)** - Downstream appearance of an Entity or Function.
  It owns no engineering facts.

## 5. Brownfield Delta

M42 deepens useful existing foundations: stable semantic identity, EngineeringFunction,
EngineeringKnowledgeState consumers, package resolution, Concept and Part prototypes, M9 capability
and constraint evidence, provenance, governed mutation review, Projection Reality, and Spatial
Reality.

M42 replaces fragmented authority rather than wrapping it:

- `EngineeringComponent` becomes `EngineeringEntity` with no alias or adapter.
- generic `EngineeringConnection` authority becomes typed EngineeringRelationship participation.
- string-parsed engineering values become typed EngineeringValue inputs.
- electrical rule enums and compiler constants move to governed Knowledge Package source.
- duplicated thin component, connection, part, package, and knowledge contracts consolidate around
  current responsibilities.
- Semantic Macro Component/Connection templates, `.properties` definitions, and dedicated transport
  surfaces retire. Independently useful provenance, preview, acceptance, and review primitives may
  survive under independent contracts.

Every replacement removes its superseded active code, tests, examples, and documentation in the
same delivery story. No compatibility phase follows M42.

## 6. Feature 1: Kernel Authority

### FR-1: Represent Typed Engineering Values

Athena can represent exact Quantities with units and dimensions plus Integer, Boolean, Text, Symbol,
and Reference values as domain-neutral EngineeringValue variants. Realizes UJ-1.

**Consequences (testable):**

- Exact numeric serialization and comparison are deterministic across repeated compilation.
- Compatible units convert and compare without precision loss accepted by the contract.
- Incompatible dimensions fail with exact subject, expected dimension, actual dimension, and source
  provenance.
- No electrical unit is hardcoded as kernel rule authority.

### FR-2: Model Entity, Function, And Port Anatomy

Athena can compile EngineeringEntity, Entity-owned EngineeringFunction, and EngineeringPort subjects
with stable identities and explicit ownership. Realizes UJ-1.

**Consequences (testable):**

- One Entity may own main and auxiliary Functions without conflating Function with Capability.
- One Function may own exact Ports; each Port exposes direction, admitted Flow definitions, and
  cardinality.
- Function ownership does not prevent a Relationship from binding Functions across Entities.

### FR-3: Assign Domain-Neutral Structure Context

Athena can assign functional, installation, and product/device structure context independently from
semantic identity.

**Consequences (testable):**

- Electrical packages may expose familiar `=`, `+`, and `-` display conventions.
- Changing Structure Assignment does not change Entity identity, Concept binding, or semantic
  Relationships.
- M42 does not require automatic numbering, an IEC 81346 platform, or location-tree UI.

### FR-4: Model Typed Relationships And Participant Roles

Athena can compile EngineeringRelationship subjects with package-qualified definition identity,
named Participant Roles, typed properties, and participants bound at the Entity, Function, or Port
level admitted by the definition. Realizes UJ-1.

**Consequences (testable):**

- Function-to-Function Relationships across different Entities are first-class.
- Concrete connectivity can require exact Port participants.
- Dependency-style Relationships can bind Entities or Functions without invented Ports.
- Generic endpoint order never substitutes for semantic Participant Roles.

### FR-5: Keep Flow Separate From Relationship

Athena can associate package-defined Flow meaning with a Relationship without treating Relationship,
Flow, connectivity, routing, or painted geometry as synonyms.

**Consequences (testable):**

- Electrical power, control information, and mechanical power can coexist without kernel-specific
  enum constants.
- A `protects` Relationship does not become drawable merely because it links two subjects.
- A connectivity Relationship can declare whether Projection and routing are admitted.

### FR-6: Replace Legacy Component And Connection Authority

Athena can expose only EngineeringEntity and typed EngineeringRelationship authority to active
compiler, runtime, Projection, Spatial, and product consumers.

**Consequences (testable):**

- Active production code contains no EngineeringComponent or generic EngineeringConnection authority,
  compatibility alias, adapter, dual model, or fallback.
- Projection and Spatial inputs bind directly to current Entity, Function, Port, and Relationship
  contracts.
- Closed M0-M41 artifacts remain unchanged as historical evidence.

## 7. Feature 2: Governed Knowledge Contract

### FR-7: Author Package-Local Knowledge In Athena Source

Domain maintainers can author Concepts, Parts, Capabilities, Relationships, Flows, formulas, and
Constraints in package-local Athena Source. Realizes UJ-2.

**Consequences (testable):**

- Manifests govern package identity and dependencies but contain no engineering facts.
- Kotlin templates, compiler constants, and ad hoc `.properties` payloads contain no canonical
  engineering truth.
- One source file declares one default domain, including `electrical`, `automation`, or `mechanical`.

### FR-8: Resolve Knowledge Identities Deterministically

Athena can resolve each Knowledge Package declaration by package identity plus qualified declaration
name. Realizes UJ-2.

**Consequences (testable):**

- Package version appears in Provenance but does not alter semantic definition identity.
- Unqualified lookup filters by the file's default domain and must produce exactly one imported
  declaration.
- Ambiguity, conflict, or missing declarations fail closed; package order never chooses a winner.

### FR-9: Define Vendor-Neutral Concepts And Part Implementations

Domain maintainers can define EngineeringConcept and PartImplementation separately, and engineers
can bind an optional PartImplementation to an EngineeringEntity. Realizes UJ-1.

**Consequences (testable):**

- Concept defines expected anatomy, property schema, Capabilities, and Relationship participation.
- Part declares supported anatomy and vendor implementation facts.
- Part assignment never silently generates authored Functions or Ports.
- Conflicting authored, Part, or derived facts are invalid rather than silently resolved by
  precedence.

### FR-10: Define Capability Contracts Without Classification

Domain maintainers can define Capabilities that subjects provide or require, including typed
parameters and admitted conditions. Realizes UJ-2.

**Consequences (testable):**

- One Entity or Function can provide multiple Capabilities and require multiple Capabilities.
- Capability definitions express participation, provision, or consumption, never subject kind.
- Classification keys may support grouping but cannot trigger hidden engineering inference.
- Authoring availability and Relationship participation use distinct contracts, not Capability.

### FR-11: Govern Relationship And Flow Participation

Domain maintainers can define valid Relationship participant levels, roles, cardinality, required
Capabilities, direction, properties, and admitted Flow meanings. Realizes UJ-2.

**Consequences (testable):**

- `controls`, `protects`, `supplies switched power`, and `drives` resolve to distinct definitions.
- Each participant is validated against its exact role and admitted Entity, Function, or Port level.
- Cross-domain meanings resolve through an owning Knowledge Package or explicit integration package.
- Imported verb ambiguity requires qualification and never resolves by package order.

### FR-12: Define Bounded Typed Formulas And Constraints

Domain maintainers can define deterministic, dimension-checked formulas and Constraints over typed
references and values. Realizes UJ-2.

**Consequences (testable):**

- Formulas admit only `+`, `-`, `*`, `/`, `min`, `max`, `round-up`, parentheses, and typed
  references.
- Comparisons admit `>`, `>=`, `<`, `<=`, `=`, and `!=`.
- Interval membership admits inclusive and exclusive bounds such as `[10, 20)`, `[10, 20]`, and
  `(10, 20)` with dimension-compatible bounds.
- Loops, scripts, arbitrary functions, network calls, and hidden Kotlin engineering calculations are
  rejected.

### FR-13: Publish Compiled Knowledge Definitions

Athena can publish one immutable EngineeringKnowledgeDocument for the resolved Knowledge Package set.
Realizes UJ-2.

**Consequences (testable):**

- Document contains package-qualified definitions, typed schemas, formulas, Constraints, and
  Knowledge Package Provenance.
- Document contains no project satisfaction state, duplicate Engineering Reality graph, Kotlin class
  names, filesystem objects, Theia fields, or renderer contracts.
- Invalid package definitions produce deterministic package diagnostics and no authoritative
  Knowledge Document.

## 8. Feature 3: Knowledge Evaluation And Validation

### FR-14: Resolve Project Subjects Against Governed Knowledge

Athena can resolve each proof-scope EngineeringEntity, Part binding, Capability, and Relationship
against exactly one governed definition.

**Consequences (testable):**

- Every baseline Entity resolves exactly one Concept.
- Every baseline Relationship resolves exactly one definition and valid Participant Roles.
- Missing or ambiguous definitions name exact project subject and relevant Knowledge Package source.

### FR-15: Evaluate Capability Satisfaction Explicitly

Athena can derive CapabilityProvision and CapabilityRequirement evidence and publish one explicit
satisfaction result for every Requirement. Realizes UJ-1.

**Consequences (testable):**

- Each Requirement reports `SATISFIED`, `UNSATISFIED`, or `UNRESOLVED` with exact evidence.
- Satisfaction cannot be inferred from classification, rendering, endpoint order, or frontend state.
- The compiler verifies authored provider choices but never chooses, creates, or connects providers.

### FR-16: Evaluate Typed Constraints And Formulas

Athena can evaluate existence, capability match, equality, ordered comparison, interval membership,
dimension compatibility, cardinality, and proof-required all/any composition.

**Consequences (testable):**

- Evaluation is exact, deterministic, dimension-safe, side-effect-free, and fully traced.
- A motor's rated current comes from authored or selected Part facts, never power-only inference.
- Governed policy can derive protection requirements from rated current through an explicit formula.

### FR-17: Publish Validation State Without Mixing Authority

Athena can publish one EngineeringValidationDocument with `READY`, `INCOMPLETE`, or `INVALID` state
for a project evaluation.

**Consequences (testable):**

- `READY` requires all required knowledge resolved, every required Capability satisfied, and no
  blocking Constraint result; non-blocking warnings may remain.
- `INCOMPLETE` means authored Engineering Reality is valid but an accepted Requirement, Relationship,
  provider, implementation choice, or well-formed blocking Constraint remains unresolved,
  unsatisfied, or failed.
- `INVALID` means definitions conflict, resolution is ambiguous, values are dimensionally invalid,
  Relationships are malformed, or required Knowledge Packages are corrupt.
- `INVALID` publishes deterministic diagnostics and Provenance but no Validated Engineering Reality
  or satisfaction claim.

### FR-18: Explain Judgements And Offer Corrections

Athena can publish plain engineering Judgements and structured Correction Options for failed or
unresolved evaluation. Realizes UJ-1.

**Consequences (testable):**

- Every Judgement names exact subjects, problem, expected and actual typed values, and correction
  direction in human-first language.
- Correction Options cover proof-required `ChangeValue`, `AddRelationship`,
  `SelectExistingProvider`, `BindPart`, and `AddRequiredFunction` cases.
- Internal diagnostic codes and transport fields do not appear as primary user-facing text.

### FR-19: Preserve Complete Provenance And Impact Evidence

Athena can trace every resolved fact, Requirement, Satisfaction, Judgement, and Correction Option to
project and Knowledge Package evidence.

**Consequences (testable):**

- Provenance includes stable source subject, source span or anchor, package identity, package version,
  and derivation/evaluation path where applicable.
- Changing one input identifies every affected Requirement, formula result, Constraint result,
  explanation, and Correction Option.
- Tool consumers can navigate to both project and knowledge source without parsing prose.

### FR-20: Keep Engineering Decisions Human-Owned

Athena can expose deterministic eligible existing providers and possible corrections without
executing them.

**Consequences (testable):**

- Evaluation never edits Athena Source, selects a Part, creates a subject, or connects a provider.
- No fallback guesses through missing or ambiguous knowledge.
- Future Pattern and AI systems can consume evidence without becoming M42 dependencies.

## 9. Feature 4: Open Contracts And Product Surfaces

### FR-21: Publish Canonical JSON And JSON Schema

Tool integrators can consume canonical JSON and JSON Schema for EngineeringKnowledgeDocument and
EngineeringValidationDocument. Realizes UJ-3.

**Consequences (testable):**

- Repeated compilation of identical inputs produces byte-identical canonical documents.
- Each schema declares its dialect, stable schema identity, required fields, and deliberate
  unknown-field policy.
- A published canonicalization specification defines byte ordering and normalization independently
  from JSON Schema validation.
- Schemas are implementation-neutral and contain no Kotlin, filesystem, Theia, renderer, or vendor
  runtime types.
- Schema versions detect mismatch without adding a pre-1.0 compatibility obligation.

### FR-22: Transport Typed Knowledge And Validation Consistently

Runtime, LSP, and CLI can publish the same typed Knowledge and Validation facts without inference.
Realizes UJ-3.

**Consequences (testable):**

- Runtime, LSP, CLI, canonical JSON, and kernel facts agree for every controlled-conveyor subject.
- Transport preserves typed values, identities, Participant Roles, state, Provenance, Judgements, and
  Correction Options.
- Frontend or CLI code does not re-evaluate engineering rules.

### FR-23: Expose Knowledge And Validation In Theia

Engineers can inspect resolved knowledge and validation evidence through existing Theia product
surfaces. Realizes UJ-1.

**Consequences (testable):**

- Inspector shows Concept, Functions, provided and required Capabilities, Part binding, satisfaction,
  and Relationship Participant Roles.
- Problems shows plain engineering failures and Correction Options.
- Source navigation reveals exact project or Knowledge Package source for every displayed fact.
- M42 adds no dashboard, ontology browser, graph-database UI, or AI chat.

### FR-24: Preserve Downstream Reality Boundaries

Projection Reality, Spatial Reality, and the existing renderer can consume migrated Entity,
Function, Port, and Relationship inputs without gaining knowledge authority.

**Consequences (testable):**

- Existing M41 projection and spatial regression proof remains green after direct migration.
- Incomplete designs may project valid authored subjects but never display incomplete knowledge as
  satisfied.
- M42 adds no label, style, grid, route optimization, canvas, export, or renderer improvement.

## 10. Feature 5: Controlled-Conveyor Proof And Closure

### FR-25: Prove A Ready Cross-Domain Baseline

Athena can compile `examples/m42/controlled-conveyor` as one `READY` electrical, automation, and
mechanical design. Realizes UJ-1.

**Consequences (testable):**

- Baseline includes supply, breaker Q1, contactor KM1, overload protection, motor M1, controller PLC1,
  and conveyor CV1.
- PLC1 controls KM1, Q1 protects M1, KM1 supplies switched power to M1, and M1 drives CV1 through
  distinct governed Relationships.
- Baseline has zero blocking validation diagnostics and complete project/knowledge Provenance.

### FR-26: Prove Incomplete And Invalid Failure Modes

Athena can demonstrate deterministic, materially different `INCOMPLETE` and `INVALID` outcomes.

**Consequences (testable):**

- Removing required protection yields `INCOMPLETE`, names the unsatisfied Requirement, and offers a
  structured non-executing correction.
- An undersized but well-formed Part yields `INCOMPLETE`, exact typed expected/actual values, and the
  governing Constraint source.
- A malformed Relationship or incompatible dimension yields `INVALID` and no satisfaction claim.

### FR-27: Prove Part Substitution Safety

Athena can bind either of two vendor Part definitions to the controlled-conveyor contactor while
preserving design intent. Realizes UJ-1.

**Consequences (testable):**

- Part swap preserves Entity identity, Concept, Functions, Capabilities, and semantic Relationships.
- Implementation facts and resulting Constraint outcomes may change and remain fully traced.
- M42 performs no automatic Part selection, equivalence ranking, pricing, stock, or procurement.

### FR-28: Prove Engineering Intelligence Density

Athena can propagate one governed motor rated-current change through all dependent engineering
evidence without human recalculation. Realizes UJ-1.

**Consequences (testable):**

- The change updates derived Requirements, Capability matching, Constraint results, impact evidence,
  explanation, and Correction Options.
- Repeating the same change from identical inputs produces identical outputs and ordering.
- All product surfaces agree on changed and unchanged facts.

### FR-29: Complete Breaking Migration And Legacy Audit

Athena can close M42 with only current Knowledge System authority in active product paths.

**Consequences (testable):**

- Retired Component/Connection, Semantic Macro, legacy knowledge loader, `.properties`, tests, active
  docs, and examples are absent according to a named superseded-contract inventory.
- Kernel contains no electrical Capability, Relationship, Flow, unit, or rule constants.
- Source-set hygiene, encoding, repository tests, frontend tests, product E2E, and milestone-local
  evidence gates pass.
- Closed M0-M41 planning and implementation artifacts remain unchanged.

## 11. MVP Scope

### 11.1 In Scope

- Domain-neutral EngineeringValue, EngineeringEntity, EngineeringFunction, EngineeringPort,
  EngineeringRelationship, Participant Role, Flow reference, and Structure Assignment authority.
- Package-local Athena Source for governed electrical, automation, and mechanical definitions.
- EngineeringConcept, PartImplementation, Capability, Relationship, Flow, formula, and Constraint
  definition contracts.
- Capability satisfaction, typed validation, Judgement, Correction Option, Provenance, and validation
  states.
- Canonical Knowledge and Validation JSON documents and JSON Schemas.
- Runtime, LSP, CLI, Inspector, Problems, and source-navigation integration.
- Direct Projection and Spatial migration with M41 regression preservation.
- Controlled-conveyor proof and complete active legacy deletion.

### 11.2 Out Of Scope

- Pattern language, variants, automatic solution generation, provider selection, or wiring.
- AI chat, AI engineering decisions, or AI-authored mutation.
- Ontology engine, universal taxonomy, graph database, Concept inheritance, or broad capability tree.
- Rendering or presentation improvement; M43 owns Presentation and Rendering Reality.
- Simulation, load flow, PLC execution, timing analysis, hydraulic calculation, or physical solver.
- Procurement, pricing, stock, catalog synchronization, Part ranking, or equivalence search.
- PhysicalAsset lifecycle, serial numbers, commissioning, maintenance, or digital twins.
- General standards platform, unproven compliance claim, or automatic device numbering.
- Knowledge registry publication, approval workflow, deprecation policy, or compatibility migration.
- Arbitrary scripting or general-purpose rule language.
- Backward compatibility, migration adapters, dual models, or legacy feature flags.

## 12. Cross-Cutting Non-Functional Requirements

### NFR-1: Determinism And Exactness

- Identical source, Knowledge Package graph, and compiler version must produce byte-identical
  canonical Knowledge and Validation documents, stable diagnostic ordering, and stable Provenance.
- Numeric comparison and unit conversion must use exact deterministic representation admitted by the
  public contract.

### NFR-2: Fail-Closed Authority

- Missing, ambiguous, conflicting, corrupt, or dimensionally invalid knowledge must never be guessed,
  silently overridden, or reported as satisfied.
- Package order, frontend state, rendering, and geometry must never decide engineering truth.

### NFR-3: Human-First Diagnostics

- Every blocking diagnostic must name exact subject, problem, expected state, actual state, correction
  direction, and navigable source evidence.
- Internal codes may support automation but cannot replace plain engineering primary text.

### NFR-4: Open Contract Quality

- Public documents and schemas must be implementation-neutral, inspectable, and documented.
- Each schema must declare required fields and a deliberate unknown-field policy.
- A published canonicalization specification must define byte ordering and normalization separately
  from JSON Schema shape validation.
- Pre-1.0 schema versioning detects mismatch but creates no backward-compatibility obligation.

### NFR-5: Bounded Execution

- Knowledge formulas and evaluation must be pure, side-effect-free, terminating, and isolated from
  arbitrary code, filesystem mutation, network access, and source mutation.

### NFR-6: Cross-Domain Integrity

- Kernel and shared Knowledge System contracts must contain no electrical-specific Capability,
  Relationship, Flow, unit, terminal, or rule constants.
- Domain-specific terminology and rules must originate in governed Knowledge Packages.

### NFR-7: Product Consistency

- Rebuild and E2E proof must show kernel, runtime, LSP, CLI, Theia Inspector, Problems, Projection, and
  Spatial consumers agree on the same canonical subject identities and validation results.

## 13. API And Versioning Policy

- EngineeringKnowledgeDocument and EngineeringValidationDocument are M42 public open-source
  contracts with canonical JSON Schema.
- Package-qualified declaration identity excludes package version; Provenance includes package
  version.
- Schema version mismatch fails explicitly.
- Athena remains pre-1.0. M42 deletes obsolete contracts instead of providing aliases, adapters,
  fallback readers, or parallel payloads.

## 14. Standards Evidence Policy

- Each Constraint identifies its authority as project policy, Knowledge Package policy, vendor data,
  or exact external standard reference.
- A standards claim requires standard name, edition, clause, applicability, and source Provenance.
- Controlled-conveyor demonstration rules are governed example policies unless exact standards
  evidence is supplied.
- M42 makes no broad IEC, EPLAN, or professional-engineering compliance claim.

## 15. Risks And Mitigations

- **Ontology expansion:** Cross-domain terms could grow into a universal taxonomy. Mitigation: admit
  only abstractions earned by controlled-conveyor proof; Capability never means classification.
- **Knowledge/validation mixing:** Definitions and project outcomes could collapse into one model.
  Mitigation: separate Knowledge and Validation documents with no duplicated Engineering Reality.
- **Engineering Reality overload:** Satisfaction and correction could become mutable project facts.
  Mitigation: Engineering Reality owns subjects; immutable Validation evidence references them.
- **Migration incompleteness:** Legacy models could survive behind adapters or stale tests.
  Mitigation: deletion occurs in each replacement story; FR-29 performs final absence audit.
- **Electrical kernel leakage:** Deep electrical proof could hardcode domain meaning. Mitigation:
  automation and mechanical participation plus NFR-6 absence checks.
- **Unsafe engineering claims:** Demonstration policy could be mistaken for standards compliance.
  Mitigation: FR provenance and Section 14 evidence requirements.
- **Scope dilution:** Rendering, Pattern, AI, procurement, or lifecycle work could enter M42.
  Mitigation: explicit scope and counter-metrics; M43 and later milestones own those outcomes.
- **Stale product proof:** Backend changes could appear missing or inconsistent in Theia. Mitigation:
  rebuild every affected runtime and frontend surface before final product E2E.

## 16. Success Metrics And Closure Gates

### Primary Metrics

- **SM-1: Ready baseline.** Controlled conveyor compiles `READY` with zero blocking validation
  diagnostics and complete Provenance. Validates FR-2, FR-3, FR-4, FR-5, FR-7, FR-8, FR-9, FR-10,
  FR-11, FR-12, FR-13, FR-14, FR-15, FR-16, FR-17, FR-18, FR-19, and FR-25.
- **SM-2: Engineering Intelligence Density.** One motor rated-current change deterministically
  updates Requirement derivation, Capability matching, Constraint judgement, impact explanation,
  and Correction Options without human recalculation. Validates FR-12, FR-15, FR-16, FR-18, FR-19,
  and FR-28.
- **SM-3: Failure-state truth.** Missing protection yields `INCOMPLETE`; malformed Relationship or
  incompatible dimension yields `INVALID`; neither path claims satisfaction. Validates FR-17,
  FR-18, and FR-26.
- **SM-4: Substitution safety.** Both contactor Parts preserve Entity identity and semantic design
  intent while implementation facts may change validation. Validates FR-9 and FR-27.
- **SM-5: Product agreement.** Kernel, canonical JSON, runtime, LSP, CLI, Inspector, Problems,
  Projection, and Spatial evidence agree for every proof subject. Validates FR-13, FR-21, FR-22,
  FR-23, FR-24, and FR-25.

### Secondary Metrics

- **SM-6: Cross-domain proof.** Electrical, automation, and mechanical Knowledge Packages contribute
  real Concepts, Capabilities, and Relationships while kernel domain-constant audit remains empty.
  Validates FR-7, FR-10, FR-11, FR-25, and NFR-6.
- **SM-7: Deterministic replay.** Repeated identical controlled-conveyor compilations produce
  byte-identical canonical documents, state, diagnostic order, and Provenance. Validates FR-1,
  FR-21, FR-28, and NFR-1.
- **SM-8: Legacy absence.** Source-set and repository audits find no retired active authority,
  compatibility path, or Semantic Macro meaning. Validates FR-6 and FR-29.

### Counter-Metrics

- **SM-C1: Taxonomy size is not success.** Number of Concepts, Capabilities, or generic abstractions
  must not grow beyond proof needs. Counterbalances SM-6.
- **SM-C2: Automatic decisions are not success.** Number of auto-applied fixes, selected providers,
  or generated solutions must remain zero. Validates FR-20 and counterbalances SM-2.
- **SM-C3: Drawing change is not success.** New rendering, label, style, grid, routing, or export
  behavior must remain zero except migration regressions fixed to preserve M41. Counterbalances SM-5.
- **SM-C4: Compatibility is not success.** Legacy tests passing through shims, aliases, or fallback
  loaders count as closure failure. Counterbalances SM-8.

### Delivery Gates

- Work follows four dependency phases: Kernel Authority, Knowledge Contract, Evaluation, Product
  Proof and Closure.
- Exact epics and approximately 8-10 stories are created only after PRD, architecture, and
  implementation-readiness approval.
- Every story is created through BMad create-story, implemented through BMad dev-story, tested,
  adversarially reviewed, and completed in numeric order.
- All planning, stories, status, reviews, screenshots, and closure evidence remain in M42-specific
  artifact directories.

## 17. Open Questions

No product-scope question currently blocks architecture. Architecture must choose internal module,
numeric-library, parser, and transport mechanisms while preserving this PRD's public behavior and
authority boundaries.

## 18. Assumptions Index

No unresolved product assumptions. The user approved the discussion design, review corrections,
launch-grade rigor, and Vision + Features coaching spine with controlled-conveyor proof plus
supporting maintainer and integrator journeys required by PRD review.
