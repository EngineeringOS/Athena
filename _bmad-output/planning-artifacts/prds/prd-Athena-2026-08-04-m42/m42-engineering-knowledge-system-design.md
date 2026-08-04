# M42 Engineering Knowledge System - Discussion Design

Date: 2026-08-04
Status: Draft for written review
Milestone: M42

## Purpose

This document preserves the shared design reached after reviewing:

- `draft/20260803-confuse/FULL-DISCUSS.md`
- `draft/20260803-confuse/dir-where-we-go-and-learn-from-eplan-study.md`
- current Athena kernel, compiler, knowledge-pack, Semantic Macro, runtime, LSP, GLSP, Theia,
  Projection Reality, and Spatial Reality code

It is the milestone-local input for the M42 BMad PRD and architecture. It is not the PRD, story
plan, or implementation plan.

## Strategic Decision

M42 changes direction from Presentation Reality to the **Engineering Knowledge System**.

Athena will not spend M42 polishing drawing output. M42 will prove that Athena understands why
engineering objects exist, which abilities they provide or require, how their relationships satisfy
system intent, and why a design is valid, incomplete, or invalid.

The Engineering Knowledge System is a cross-cutting authority, not another Reality domain, graph,
database, or source of project subjects. It supplies governed definitions during Engineering Reality
construction, then evaluates the canonical Engineering Reality without taking ownership from it.

```text
governed Athena knowledge packages
            |
            v
Engineering Knowledge System
  definitions + evaluation
            |
            +---- resolves knowledge-backed meaning ----+
            |                                            |
Athena source -> Semantic Graph -> Engineering Reality <-+
                                      |
                                      +---- knowledge evaluation
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

Knowledge-backed Concept, Part, capability, and relationship definitions participate in resolving
Engineering Reality. Project Entities, Functions, Ports, Relationships, and authored facts remain
owned only by Engineering Reality. Evaluation adds satisfaction, constraint, correction, and
explanation evidence; it does not mutate or duplicate those subjects.

`Validated Engineering Reality` names the canonical Engineering Reality paired with its immutable
validation evidence. It is not a second entity graph, database, or authority.

Knowledge definition and project validation remain separate contracts:

```text
EngineeringKnowledgeDocument
    compiled governed definitions from the resolved package set

EngineeringValidationDocument
    project-specific evaluation over canonical Engineering Reality
```

Neither contract becomes a source-mutation path.

## Approaches Considered

| Approach | Decision | Reason |
|---|---|---|
| Continue original M42 Presentation styling, labels, visibility, and grid chrome | Rejected | Improves downstream appearance before Athena has a coherent engineering knowledge model. |
| Build a universal ontology, graph database, reasoning engine, and AI agent | Rejected | Too broad, speculative, and unable to earn its abstractions through one falsifiable product slice. |
| Build one cross-domain Engineering Knowledge System vertical slice | Adopted | Replaces fragmented prototypes with a coherent model while keeping scope grounded in executable engineering decisions. |

## Existing Reality To Replace Or Deepen

Athena already contains useful but fragmented knowledge foundations:

- vendor-neutral `EngineeringConceptDefinition`
- vendor-specific `PartImplementationDefinition`
- `EngineeringKnowledgeState`
- M9 derived electrical capability facts and constraint evaluations
- package-backed knowledge artifacts
- `EngineeringFunction`
- relationship admission contracts
- Semantic Macro definitions, previews, acceptance, and origin trace

M42 does not create knowledge from nothing. It replaces the narrow prototypes with one coherent
contract. Current defects include:

- `capability` means authoring availability, relationship permission, and derived sizing facts;
- kernel enums hardcode one electrical rule slice;
- engineering values frequently remain strings parsed by evaluators;
- `EngineeringConnection` carries generic binary endpoints but no governed relationship meaning;
- concept, component, part, connection, package, and knowledge contracts are duplicated across thin
  modules;
- knowledge truth is split across Kotlin, `.properties`, manifests, and compiler constants;
- Semantic Macro remains a parameterized Component/Connection template system rather than an
  intent-to-solution Pattern system.

## M42 Product Thesis

> M42 establishes Athena as an engineering knowledge system capable of validating Engineering
> Reality, not merely representing engineering objects.

Athena compiles authored engineering entities, functions, relationships, and governed domain
knowledge into explicit capability requirements, provisions, satisfaction, constraints,
implementation facts, corrections, and provenance.

M42 succeeds through one dedicated mechatronic fixture:

```text
examples/m42/controlled-conveyor
```

The baseline includes an electrical supply, breaker, contactor, overload protection, motor, an
automation controller, and a mechanical conveyor. It proves, at minimum:

```text
PLC1 controls KM1
Q1 protects M1
KM1 supplies switched power to M1
M1 drives CV1
```

Electrical knowledge receives the deep rule proof. Automation and mechanical knowledge remain
small but real, preventing electrical assumptions from entering kernel contracts.

## Canonical Engineering Vocabulary

### Identity Stack

```text
EngineeringConcept
    vendor-neutral knowledge definition

EngineeringEntity
    authored project instance

PartImplementation
    optional vendor realization selected for an Entity

PhysicalAsset
    installed real-world instance; deferred beyond M42

ProjectionOccurrence
    downstream appearance of an Entity or Function
```

Example:

```text
Concept: electrical contactor
Entity: KM1
PartImplementation: Siemens 3RT2026
ProjectionOccurrence: KM1 main Function shown on Sheet S1
```

M42 replaces `EngineeringComponent` with `EngineeringEntity`. It adds no alias or compatibility
adapter.

### EPLAN-Aligned Functional Anatomy

Athena keeps the user model familiar to EPLAN engineers while keeping the kernel cross-domain:

```text
Project
  Structure
    Device (electrical domain term for EngineeringEntity)
      Main Function
      Auxiliary Functions
        Ports / Connection Points
      Part assignment
  Relationships / Connections
    Participants bind exact Ports, Functions, or Entities as defined by relationship knowledge
```

`EngineeringFunction` remains first-class. A Function is a system role or partition inside an
Entity, such as a coil, main contact, auxiliary contact, or overload sensing Function. Capability
is the reusable semantic contract behind an Entity or Function; it is not a replacement for
Function.

Function ownership establishes identity and containment only. It does not force relationships into
an Entity-to-Entity hierarchy. Project-level Relationships may bind Function-to-Function across
different Entities as first-class participants. For example, KM1's switching Function supplies
power to M1's electrical-input Function. Relationships may bind an Entity or exact Port only when
their governed definition admits that participant level.

Electrical UI may say Device and Connection Point. Other domains may use their own display terms.
Kernel contracts remain `EngineeringEntity`, `EngineeringFunction`, and `EngineeringPort`.

### Structure Assignments

M42 adds minimal domain-neutral structure aspects and assignments. The electrical knowledge package
defines familiar functional assignment, installation location, and product/device designation
aspects, including EPLAN-style `=`, `+`, and `-` display conventions.

Structure assignment supplies context and display address. It does not replace canonical semantic
identity. M42 does not add automatic numbering, an IEC 81346 platform, or location-tree UI.

## Capability Contract

Capability means only an engineering ability an Entity or Function provides or requires: what the
subject can participate in, provide, or consume. Capability never states what the subject is.

```text
CapabilityDefinition
CapabilityProvision
CapabilityRequirement
CapabilitySatisfaction
```

Capability is a typed contract, not a classification tag or inheritance node. It may carry typed
parameters, conditions admitted by the M42 formula boundary, subject provenance, knowledge
provenance, and exact satisfaction status.

For example, `rotating machine` is classification and does not qualify as a Capability. Providing
mechanical torque and requiring electrical energy are Capabilities. Concept identity and explicit
classification keys carry kind information.

Concepts use composition, not inheritance. A Concept declares:

- functional anatomy;
- provided capabilities;
- required capabilities;
- permitted relationship participation;
- typed property schema;
- structure expectations.

Classification keys may support search and grouping but never drive hidden engineering inference.

M42 removes overloaded legacy terminology:

- authoring capability becomes `AuthoringActionAvailability`;
- relationship capability becomes `RelationshipParticipationRule`;
- derived capability facts migrate into Capability requirements and derivation evidence.

## Relationship And Flow Contract

M42 replaces generic `EngineeringConnection` authority with typed `EngineeringRelationship`:

```text
EngineeringRelationship
- package-qualified relationship definition identity
- participants with named roles
- typed properties
- provenance
```

Examples use explicit roles such as provider/consumer, controller/controlled,
protector/protected, and driver/driven. Domain knowledge packages define valid participant levels,
roles, cardinality, capability requirements, direction, and whether a relationship admits
connectivity projection and routing.

Not every relationship is a wire. `protects` must never render merely because it links two subjects.

Flow remains distinct from Relationship:

- electrical power;
- control information;
- mechanical power;
- material;
- signal or communication;
- other package-defined flow meanings.

A Flow has a package-qualified definition, source and sink roles, optional medium or signal
identity, typed engineering values, relationship reference, and provenance. M42 adds no simulation,
load-flow solver, PLC execution, hydraulic calculation, or timing analysis.

Relationships that represent concrete connectivity bind exact Ports. Dependency-style relations may
bind Entities or Functions without inventing Ports. Port definitions declare owner, direction,
provided or accepted Flow types, cardinality, and optional terminal/interface designation.

Cross-domain relationship definitions belong to the package owning their engineering meaning.
Integration packages exist only for genuinely cross-domain meaning. Ambiguous imported verbs require
qualification; package order never selects a winner.

## Typed Engineering Values

M42 replaces string parsing at evaluation boundaries with one domain-neutral value surface:

```text
EngineeringValue
- Quantity(exact value, unit, dimension)
- Integer
- Boolean
- Text
- Symbol
- Reference
```

Requirements:

- deterministic exact numeric representation;
- dimension-safe comparison and conversion;
- implementation-neutral serialized form;
- natural source such as `power 7.5 kW` and `rated current 20 A`;
- no electrical units hardcoded into the knowledge kernel.

M42 supports familiar ASCII comparison operators:

```text
>  >=  <  <=  =  !=
```

It also supports typed interval membership:

```athena
voltage in [380 V, 420 V]
temperature in [-20 C, 60 C)
speed in (0 rpm, 1500 rpm]
```

Square bounds are inclusive; round bounds are exclusive. Bounds require compatible dimensions.
Open-ended limits use comparison operators rather than infinity literals.

## Knowledge Formula Boundary

Knowledge packages may derive typed values through a small pure formula subset:

```text
+  -  *  /
min
max
round-up
parentheses
typed references
```

Formulas are deterministic, dimension-checked, side-effect-free, and fully traced. M42 adds no
loops, scripts, network calls, arbitrary functions, or hidden Kotlin calculations.

The controlled-conveyor proof does not infer motor current from power alone. Motor or selected Part
data supplies rated current because power, voltage, phase, efficiency, power factor, and vendor data
all influence real current. Governed knowledge may derive a protection requirement from that rated
current through an explicit policy formula.

## Constraint And Correction Contract

M42 provides a small generic evaluator, not a rule programming platform. Supported judgement shapes
include:

- existence;
- capability match;
- equality and ordered comparison;
- interval membership;
- dimension/unit compatibility;
- cardinality;
- all/any composition required by the accepted vertical slice.

Domain packages supply constraint meaning and operands. Kernel evaluates them deterministically.

Every evaluation records:

- satisfied, warning, or error status;
- exact subjects;
- expected and actual values;
- plain engineering explanation;
- project-source and knowledge-source provenance.

M42 emits structured, non-executing correction options such as:

- `ChangeValue`;
- `AddRelationship`;
- `SelectExistingProvider`;
- `BindPart`;
- `AddRequiredFunction`.

Correction options identify affected source subjects and evidence. They never mutate source
automatically.

## Concept, Part, And Fact Authority

Part assignment proves the EPLAN distinction between design intent and procurement realization.
M42 includes a vendor-neutral contactor Concept and two interchangeable vendor Part definitions.
Swapping Parts preserves Entity identity, Concept, intent, Functions, and Relationships. It may
change implementation facts and constraint outcomes.

M42 adds no automatic Part selection, pricing, stock, procurement, equivalence ranking, online
catalog, or PhysicalAsset lifecycle.

Part assignment does not silently generate Functions or Ports. Concept defines expected functional
anatomy; Part declares supported anatomy and ratings; authored source owns accepted Functions and
Ports. Compiler validates the combination and emits structured corrections for missing anatomy.

There is no silent precedence when Concept defaults, project facts, Part facts, and derived values
interact:

- Concept defines schema and optional suggestions;
- project source owns authored instance facts;
- Part owns vendor implementation facts;
- derived facts remain separate;
- duplicate facts must agree;
- disagreement is invalid rather than resolved by precedence.

Project-specific derating or operating conditions are modeled as distinct facts or constraints, not
as silent overrides of vendor data.

## Knowledge Source Authority

Canonical domain knowledge is authored in package-local Athena source. M42 removes engineering
truth from Kotlin templates, fixed kernel enums, and ad hoc `.properties` payloads.

Expected package shape:

```text
extensions/knowledge-electrical-basic/
  athena.yaml
  src/
    capabilities.athena
    concepts.athena
    relationships.athena
    constraints.athena
```

Manifests govern package identity and dependencies. They do not own engineering facts. Kotlin owns
compiler mechanics only.

Knowledge identities derive from package plus qualified declaration name. Package version appears
in provenance, not semantic identity. Existing namespace, import, and package-resolution rules bind
references. Unqualified resolution filters by the file's default domain and must still produce
exactly one imported declaration. Package order never breaks a tie. Conflicts and ambiguity fail
closed.

## Human-First Source Direction

One file declares one default domain so engineers do not repeatedly type long domain qualifiers:

```athena
package com.example.conveyor
domain electrical

motor M1 {
  power 7.5 kW
  voltage 400 V
}

breaker Q1 {
  rated current 20 A
}
```

Automation and mechanical definitions live in files with their own domain context. Cross-domain
system source imports their subjects and writes natural engineering verbs:

```athena
PLC1 controls KM1
Q1 protects M1
KM1 supplies switched power to M1
M1 drives CV1
```

Concept names resolve within the file's default domain. Qualification is required when that lookup
does not produce exactly one imported declaration. Exact grammar is derived after the semantic
model is fixed; parser convenience never changes kernel authority.

Engineers author entities, Functions, properties, semantic relationships, optional Part bindings,
and real project constraints. They do not author `CapabilitySatisfaction`, evaluator operators as
protocol fields, knowledge entry IDs, transport payloads, or compiler trace objects.

## Knowledge Compilation, Validation State, And Failure Policy

Knowledge compilation and project validation are separate. A resolved knowledge package set either
compiles into an `EngineeringKnowledgeDocument` or fails with package diagnostics. It does not carry
project satisfaction state.

Project evaluation publishes an `EngineeringValidationDocument` with one of three states:

```text
READY
INCOMPLETE
INVALID
```

- `READY`: required knowledge resolves, every required capability is satisfied, and no accepted
  constraint has a blocking result. Non-blocking warnings may remain.
- `INCOMPLETE`: authored model is valid but a capability, relationship, provider, implementation
  choice, or accepted engineering requirement remains unsatisfied or unresolved.
- `INVALID`: definitions conflict, concepts are ambiguous, units are invalid, relationships are
  malformed, or a knowledge package is corrupt.

`INCOMPLETE` preserves authored Engineering Reality and publishes exact validation diagnostics.
`INVALID` still publishes a deterministic validation document containing failure diagnostics and
provenance, but publishes no Validated Engineering Reality and makes no satisfaction claim.
Projection may continue showing valid authored subjects during incomplete design, but must never
present incomplete knowledge as satisfied. No fallback selects or guesses knowledge.

The compiler verifies authored provider choices. It does not choose, create, or connect providers.
It may list deterministic eligible Entities already present in the project as correction evidence.
Future Pattern and AI systems own provider selection and solution generation.

## Open Contracts And Product Surface

M42 publishes two deterministic, implementation-neutral contracts with canonical JSON and JSON
Schema:

- `EngineeringKnowledgeDocument` contains compiled package-qualified definitions, typed property
  schemas, formulas, constraint definitions, and knowledge provenance;
- `EngineeringValidationDocument` contains project state, canonical Engineering Reality subject
  references, requirements, satisfactions, judgements, corrections, and evaluation provenance.

Neither document copies or owns the Engineering Reality entity graph. Neither contains Kotlin class
names, filesystem objects, Theia fields, or renderer contracts.

A schema version detects mismatch. It does not create a compatibility obligation while Athena is
pre-1.0.

Existing product surfaces expose M42 knowledge definitions and validation evidence:

- Inspector: Concept, provided/required capabilities, implementation, satisfaction;
- relationship inspection: semantic definition and participant roles;
- Problems: plain engineering failures and corrections;
- source navigation: reveal project or knowledge source for every fact;
- CLI/structured output: canonical Knowledge and Validation documents for tools.

Runtime and LSP transport typed facts. Theia displays them without inference. M42 adds no dashboard,
ontology browser, graph-database UI, or new rendering engine.

## AI Boundary

M42 adds no AI chat or AI engineering decisions. It publishes AI-ready explanation evidence:

- why a requirement exists;
- which capability is missing;
- expected and actual values;
- involved entities and relationships;
- source and knowledge provenance;
- deterministic correction options;
- exact source subjects affected.

Future AI may explain, rank, and propose reviewable source edits from this evidence. It must never
infer engineering truth from pixels, prose parsing, or frontend state.

## Standards Evidence Policy

M42 makes no broad IEC, EPLAN, or professional-engineering compliance claim. Every constraint names
its authority: project policy, domain package, vendor data, or an exact externally sourced standard
reference. A standard claim requires clause, edition, applicability, and provenance.

The M42 example rules are governed demonstration policies unless their exact standards basis is
proven.

## Breaking Migration And Cleanup

M42 is intentionally breaking. No backward compatibility, type aliases, adapters, feature flags,
dual models, fallback loaders, or legacy examples remain.

Target module shape:

```text
kernel/engineering-model
  Entities, Functions, Ports, Relationships, Values, authored/resolved project facts

kernel/knowledge-model
  definition contracts: Concepts, Parts, Capabilities, Constraints, Knowledge Document
  evaluation contracts: Requirements, Satisfaction, Judgements, Corrections, Validation Document

kernel/compiler
  package compilation, resolution, derivation, evaluation

extensions/knowledge-*
  domain knowledge source
```

M42 consolidates and removes thin or duplicate component, connection, part, compiler-knowledge, and
package contracts when their responsibilities migrate.

The Semantic Macro and template pipeline is retired where tied to old architecture, including
`.properties` definitions, Component/Connection templates, runtime/LSP/frontend macro protocols,
and stale tests or documentation. Provenance, governed mutation preview, explicit acceptance, and
review mechanisms survive as independently named OS-level primitives only where they have consumers
outside the retired macro semantics.

Each replacement story deletes its superseded code, tests, active docs, and examples immediately.
Final cleanup is an audit, not a delayed migration phase.

Closed M0-M41 BMad PRDs, stories, reviews, and retrospectives remain unchanged as historical
evidence. Active architecture, roadmap, user docs, examples, and tests move to M42 vocabulary.

Projection and Spatial compilers migrate directly to EngineeringEntity and typed Relationship
inputs. The M41 Golden example remains regression evidence, not an active M42 product fixture. No
compatibility bridge preserves `EngineeringComponent` or generic `EngineeringConnection`.

## Rendering Boundary And M43 Handoff

M42 adds no label layout, styling, visibility, terminal-label painting, grid chrome, route
optimization, canvas logic, export, or renderer evolution. Existing diagrams continue to render only
as regression proof.

Visible M41 debt remains explicit during M42: small or overlapping labels, basic styling, absent
grid chrome, and M41-quality routing.

M43 becomes **Presentation and Rendering Reality** and owns:

- labels and terminal labels;
- styling and visibility;
- grid chrome;
- knowledge-aware visual semantics;
- Theia/SVG painting;
- readability proof.

PDF, Canvas, Excel, and other export work remains outside M43 until presentation authority is
correct. Pattern and AI milestones follow the Knowledge and Presentation foundations.

## Explicit M42 Non-Goals

- rendering or presentation improvement;
- Pattern language or automatic solution generation;
- AI chat, AI provider choice, or AI-authored mutation;
- ontology engine, universal taxonomy, graph database, or Concept inheritance;
- automatic provider selection or wiring;
- general standards platform or unproven compliance claim;
- procurement, stock, pricing, catalog synchronization, or Part ranking;
- PhysicalAsset, serial-number, commissioning, maintenance, or digital-twin lifecycle;
- simulation, load flow, PLC execution, signal timing, or physical solver;
- arbitrary scripting or general-purpose rule language;
- automatic device numbering or full structure-management UI;
- backward compatibility.

## Closure Gates

The controlled-conveyor baseline must prove:

- every Entity resolves exactly one Concept;
- every Relationship resolves exactly one definition and valid participant roles;
- every capability requirement has `SATISFIED`, `UNSATISFIED`, or `UNRESOLVED` evidence;
- every derived judgement carries project and knowledge provenance;
- baseline has zero blocking validation diagnostics;
- missing protector yields `INCOMPLETE` with structured correction;
- undersized Part yields exact typed constraint failure;
- invalid relationship or incompatible dimension yields `INVALID`;
- Part swap preserves Entity identity and semantic relationships;
- motor rated-current change deterministically updates requirements, constraint results, impact,
  explanation, and correction options;
- runtime, LSP, Inspector, Problems, both canonical JSON documents, and kernel facts agree;
- existing M41 projection and spatial regression remains green;
- kernel contains no electrical capability, relationship, flow, unit, or rule constants;
- retired legacy knowledge, Component/Connection, Semantic Macro, loaders, tests, active docs, and
  examples are absent;
- source-set hygiene, encoding, repository tests, frontend tests, product E2E, and milestone-local
  evidence gates pass.

M42 Engineering Intelligence Density is demonstrated when one governed motor-rating change causes
requirement derivation, capability matching, constraint evaluation, impact explanation, and
correction generation without human recalculation.

## Delivery Order

M42 is intentionally larger than a normal milestone. Delivery is divided into four dependency
phases so knowledge grammar and evaluation cannot distort the core engineering model.

### Phase 1 - Kernel Authority

- `EngineeringValue`, exact quantities, units, and dimensions;
- `EngineeringEntity`, `EngineeringFunction`, `EngineeringPort`, Structure, typed
  `EngineeringRelationship`, participant roles, and Flow references;
- direct replacement of generic Component/Connection authority and direct Projection/Spatial
  migration;
- no knowledge package grammar or evaluator yet.

### Phase 2 - Knowledge Contract

- package-qualified knowledge identities and package-local Athena source grammar;
- Concept, Part, Capability, Relationship, Flow, formula, and Constraint definitions;
- immutable `EngineeringKnowledgeDocument` with canonical JSON and JSON Schema.

### Phase 3 - Evaluation

- provisions, requirements, satisfaction, formula and constraint evaluation;
- corrections, provenance, validation states, and `EngineeringValidationDocument`;
- runtime, LSP, Inspector, Problems, and CLI integration.

### Phase 4 - Product Proof And Closure

- dedicated controlled-conveyor cross-domain product proof;
- electrical depth plus real automation and mechanical participation;
- legacy deletion audit, active-document migration, product E2E, and closure evidence.

Exact epics and stories are created later through BMad after PRD, architecture, and implementation
readiness review. Every story is created through BMad create-story, implemented through BMad
dev-story, tested, adversarially reviewed, and completed in numeric order.

## Milestone-Local Artifact Rule

M42 artifacts remain milestone-local:

```text
_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/
_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-04-m42/
_bmad-output/implementation-artifacts/m42/
```

No global sprint-change proposal or unrelated milestone artifact owns M42 decisions, status, proof,
or closure.
