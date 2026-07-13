---
title: 'Athena M14'
type: architecture-spine
purpose: build-substrate
altitude: initiative
paradigm: 'deterministic component knowledge resolution with semantic-first multi-surface authoring and package-governed extension packs'
scope: 'Athena M14 component knowledge foundation'
status: draft
created: '2026-07-13'
updated: '2026-07-13'
binds:
  - 'FR-1'
  - 'FR-2'
  - 'FR-3'
  - 'FR-4'
  - 'FR-5'
  - 'FR-6'
  - 'FR-7'
  - 'FR-8'
  - 'FR-9'
  - 'FR-10'
sources:
  - 'draft/m14/001-chatgpt.md'
  - 'draft/m14/003-chatgpt.md'
  - 'docs/roadmap/athena-milestone-roadmap.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md'
companions:
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m9/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md'
---

# Architecture Spine - Athena M14

## Design Paradigm

Athena M14 is a **deterministic component knowledge resolution with semantic-first multi-surface authoring and package-governed extension packs** architecture.

- **deterministic component knowledge resolution** means Athena resolves authored component references into governed engineering concepts, semantic ports, minimal physical traits, and vendor implementations through JVM-owned compile-time flows.
- **semantic-first multi-surface authoring** means mainstream users are not required to write raw DSL directly, but every authoring surface still converges through the same semantic mutation authority and canonical serialization.
- **package-governed extension packs** means component knowledge lives in extension-owned packs whose versions and availability are constrained by the existing repository and package graph rather than by ad hoc runtime discovery.

## Inherited Invariants

| Inherited | From parent | Binds here |
| --- | --- | --- |
| AD-13 | `architecture-Athena-2026-07-08-m5` | Repository and package contracts remain in `kernel/repository-model`. |
| AD-16 | `architecture-Athena-2026-07-08-m5` | `athena.lock` remains derived reproducibility state, not authored intent. |
| AD-17 | `architecture-Athena-2026-07-08-m5` | Runtime continues to own one active repository graph session per product window. |
| AD-18 | `architecture-Athena-2026-07-08-m5` | IDE work stays additive and product-operability scoped through existing seams. |
| AD-34 | `architecture-Athena-2026-07-10-m8` | One mutation authority above source and graph remains binding. |
| AD-39 | `architecture-Athena-2026-07-10-m8` | Cross-surface anchoring continues to use canonical semantic identity. |
| AD-41 | `architecture-Athena-2026-07-10-m8` | Source and graph editing still converge before review and persistence. |
| AD-43 | `architecture-Athena-2026-07-11-m9` | Knowledge derivation starts from canonical engineering state only. |
| AD-44 | `architecture-Athena-2026-07-11-m9` | Derived context remains a first-class layer above canonical `Engineering IR`. |
| AD-45 | `architecture-Athena-2026-07-11-m9` | Capability facts remain engineering judgements above derived context. |
| AD-46 | `architecture-Athena-2026-07-11-m9` | The first knowledge proof stays narrow and governed. |
| AD-47 | `architecture-Athena-2026-07-11-m9` | Engineering sufficiency remains typed and separate from structural validation. |
| AD-49 | `architecture-Athena-2026-07-11-m9` | Existing semantic delivery surfaces remain the product path. |
| AD-50 | `architecture-Athena-2026-07-12-m10` | Runtime owns deterministic reasoning-context assembly for AI-assisted surfaces. |
| AD-51 | `architecture-Athena-2026-07-12-m10` | AI output remains typed proposal and never becomes canonical truth. |
| AD-67 | `architecture-Athena-2026-07-12-m13` | `Presentation IR` remains a dedicated downstream layer. |
| AD-72 | `architecture-Athena-2026-07-12-m13` | Canonical semantic identity stays stronger than presentation occurrences. |
| AD-74 | `architecture-Athena-2026-07-12-m13` | Downstream packs remain extension-compatible assets. |

## Invariants & Rules

### AD-75 - M14 Introduces Component Knowledge Resolution Between Engineering IR And Downstream Consumers

- **Binds:** `FR-1`, `FR-2`, `FR-5`, `FR-8`
- **Prevents:** M14 from rebranding M9 knowledge runtime or from pushing component meaning into renderer, AI, or frontend heuristics
- **Rule:** M14 introduces a dedicated component-knowledge resolution layer above canonical `Engineering IR` and below later derived-context, projection, presentation, validation, and AI consumers. This layer answers what a component is, what semantic ports it owns, what minimal physical traits it carries, and which vendor implementations may realize it.

### AD-76 - Component Knowledge Packs Resolve Deterministically At Compile Time

- **Binds:** `FR-1`, `FR-2`, `FR-6`, `FR-9`
- **Prevents:** runtime drift, hidden remote lookups, or frontend-specific package discovery from becoming practical knowledge authority
- **Rule:** The first M14 proof resolves component knowledge through compiler-owned deterministic package loading over the existing M5 repository graph and approved hosted plugin seams. Knowledge-pack availability, versions, and identities are constrained by the package graph already locked by `athena.lock`. M14 does not add ad hoc runtime network lookups, dynamic remote catalog fetches, or a second lockfile as a source of truth.

### AD-77 - Engineering Concepts Are Vendor-Neutral; Vendor Parts Are Implementations

- **Binds:** `FR-2`, `FR-3`, `FR-6`
- **Prevents:** vendor product identifiers from becoming the semantic type system
- **Rule:** M14 models an engineering concept such as `electrical.plc.cpu` or `electrical.contactor.3p` separately from vendor implementations such as `Siemens.S7300.CPU313C`. Engineering concepts own semantic identity, semantic ports, and minimal physical meaning. Vendor parts contribute implementation mappings and part-specific metadata only as realizations of an engineering concept.

### AD-78 - Semantic Ports Are First-Class Knowledge Contracts

- **Binds:** `FR-2`, `FR-4`, `FR-7`
- **Prevents:** ports from remaining label strings, symbol affordances, or rule-specific ad hoc metadata
- **Rule:** M14 introduces semantic port contracts as typed connection concepts with stable roles, direction, signal family, and optional protocol or compatibility metadata. The first M14 slice may define connection type and role only. Richer compatibility evaluation remains downstream M9 rule logic rather than being embedded directly inside the connection-model contract.

### AD-79 - Minimal Physical Traits Stay Separate From Layout And Geometry Truth

- **Binds:** `FR-2`, `FR-5`, `FR-7`
- **Prevents:** physical dimensions from collapsing into view coordinates or from exploding M14 into a CAD geometry engine
- **Rule:** M14 physical traits are limited to minimal reusable component facts such as width, height, depth, mounting type, and basic installation environment markers. These traits are downstream inputs for later layout, projection, and validation work. They do not replace `Layout IR`, `Geometry IR`, or renderer-specific scene calculations.

### AD-80 - M14 Outputs Feed M9 And M13; M14 Does Not Create A New Mutation Path

- **Binds:** `FR-1`, `FR-4`, `FR-5`, `FR-10`
- **Prevents:** component resolution from becoming a second command system, a hidden authoring path, or an alternate review authority
- **Rule:** M14 output is read-only resolved component knowledge. M9 consumes it as input to later derived-context and capability-fact computation. M13 and projection layers consume it as input to family and presentation-pack selection. Changes to authored components still converge through the existing M8 mutation authority before resolution runs again.

### AD-81 - Conflicting Knowledge Definitions Fail Explicitly

- **Binds:** `FR-2`, `FR-6`, `FR-9`
- **Prevents:** hidden package precedence, classpath luck, or vendor pack ordering from choosing semantic truth silently
- **Rule:** If two active knowledge packs claim the same engineering concept id, vendor part id, or canonical component-definition slot without an explicit override contract, M14 reports a compiler-owned conflict diagnostic. The first proof does not allow implicit precedence by load order, filesystem order, or frontend preference.

### AD-82 - DSL Remains Canonical Serialization, Not The Default Human Interface

- **Binds:** `FR-3`, `FR-8`, `FR-10`
- **Prevents:** the semantic core from being mistaken for a DSL-first product experience
- **Rule:** M14 freezes the product position that DSL is Athena's canonical, inspectable serialization and direct expert surface, but not the required primary interface for mainstream engineers. Graph, forms, templates, API, and AI-assisted flows remain valid future producers, provided they all converge through the same semantic command and mutation path before canonical serialization.

### AD-83 - Behavior, Simulation, And Catalog Breadth Stay Deferred

- **Binds:** `FR-6`, `FR-9`
- **Prevents:** M14 from widening into digital twin, behavior engines, or broad vendor database work before the component-knowledge substrate is proven
- **Rule:** M14 excludes behavior models, simulation state, digital-twin execution, broad standards packs, broad multi-vendor catalogs, and procurement optimization. The proof stays narrow: electrical only, minimal component classes, minimal physical traits, and one narrow vendor proof set.

## Layer Responsibilities

### Authoring Surfaces

Own:

- human or agent interaction style
- graph, form, template, API, and DSL entrypoints
- future usability layers

Do not own:

- semantic truth
- component knowledge resolution
- package selection truth

### Semantic Command / Mutation Layer

Owns:

- the only authoritative write path
- review, undo, redo, and audit coherence
- accepted change categories and mutation outcomes

Does not own:

- component knowledge pack contents
- vendor catalog semantics

### Engineering IR

Owns:

- canonical authored engineering structure
- stable identities
- authored properties and references

Does not own:

- resolved engineering concept libraries
- vendor implementation catalogs
- presentation packs

### Component Knowledge Resolution

Owns:

- component concept resolution
- semantic port resolution
- minimal physical trait resolution
- vendor implementation mapping
- deterministic conflict detection

Does not own:

- mutation authority
- derived-context formulas
- capability facts
- renderer behavior

### M9 Knowledge Runtime

Consumes:

- canonical `Engineering IR`
- resolved component knowledge

Owns:

- derived engineering context
- capability facts
- constraint evaluation
- impact consequences

### Projection / Presentation / Renderer

Consume:

- canonical `Engineering IR`
- resolved component knowledge
- later M9 outputs where needed

Own:

- downstream view, presentation, and visual realization only

## New Kernel Boundaries

### `kernel/component-model`

Purpose:

- define engineering concept identity
- define component classes
- define stable component knowledge contracts

### `kernel/connection-model`

Purpose:

- define semantic ports
- define connection role and signal family vocabulary
- define direction and protocol-bearing port metadata where needed

Boundary:

- M14 defines typed connection concepts only
- richer compatibility judgement remains downstream M9 knowledge logic

### `kernel/physical-model`

Purpose:

- define minimal physical traits
- dimensions
- mounting type
- basic installation markers

Boundary:

- no CAD geometry engine
- no layout ownership

### `kernel/part-model`

Purpose:

- map engineering concepts to vendor implementations
- keep vendor ids, manufacturer metadata, and implementation-specific traits separate from concept identity

## Knowledge Pack Loading Model

The M14 first proof uses this loading strategy:

1. Repository package graph resolves first through existing M5 rules.
2. Approved extension packs become available through the locked package graph and hosted plugin seams.
3. Compiler builds one deterministic active knowledge-pack set for the session.
4. Component references resolve against that active set.
5. Unresolved or conflicting references surface as typed compiler diagnostics.

This means:

- package version reproducibility remains governed by `athena.lock`
- runtime may cache resolved knowledge in-memory per session
- no separate knowledge lockfile becomes canonical truth in M14

## Recommended Proof Slice

### P0 Components

- PLC CPU
- Digital I/O
- Analog I/O

### P1 Components

- Contactor
- Relay
- Motor

### P2 Components

- 24V power supply
- Breaker
- Cable

### Deferred

- HMI
- Drive / VFD
- Sensor families
- broad catalog substitutes

### First Vendor Proof

- Siemens only

This is enough to prove:

- concept resolution
- semantic port publication
- minimal physical trait publication
- vendor implementation mapping
- downstream projection and presentation consumption

## Structural Flow

```mermaid
flowchart LR
  surfaces[Graph / AI / Forms / DSL / API]
  mutation[Semantic command and mutation layer]
  ir[Engineering IR]
  graph[M5 package graph + athena.lock]
  packs[Approved knowledge packs]
  resolve[Component knowledge resolution]
  context[M9 derived context and capability facts]
  projection[Projection model]
  presentation[Presentation IR]
  renderer[Renderer]

  surfaces --> mutation
  mutation --> ir
  graph --> packs
  ir --> resolve
  packs --> resolve
  resolve --> context
  resolve --> projection
  resolve --> presentation
  projection --> presentation
  presentation --> renderer
```

## Consistency Conventions

| Concern | Convention |
| --- | --- |
| Naming (entities, files, interfaces, events) | Prefer `EngineeringConcept`, `ResolvedComponentDefinition`, `SemanticPortDefinition`, `PhysicalTraitSet`, `VendorPartImplementation`, `KnowledgePackRegistry`, and `ComponentResolutionDiagnostic`. Avoid UI-shaped or vendor-shaped core names such as `SymbolComponent`, `SiemensPortTruth`, or `PaletteDeviceType`. |
| Data & formats (ids, dates, error shapes, envelopes) | Engineering concept ids remain vendor-neutral. Vendor part ids remain separate implementation ids. Resolution outputs always preserve canonical semantic subject identity plus the resolved concept id that informed them. |
| State & cross-cutting (mutation, errors, logging, config, auth) | Resolution is deterministic and replayable for the same canonical repository and active package graph. Frontend caches are disposable. Runtime session caches are derivative only. Conflict and unresolved-reference states surface as typed compiler diagnostics. |
| Build and dependency management | `kernel/component-model`, `kernel/connection-model`, `kernel/physical-model`, and `kernel/part-model` become JVM-first typed contract modules. `kernel/compiler` resolves against them. `kernel/runtime`, M9, `ide/lsp`, and downstream presentation consumers transport the results; they do not redefine the contracts. |

## Stack

| Name | Version |
| --- | --- |
| Java | 25 |
| Kotlin | 2.4.0 |
| Gradle | 9.6.1 |
| Node.js | 22+ |
| Yarn | 1.22.22 |
| Eclipse Theia | 1.73.1 |

## Structural Seed

```text
Athena/
  kernel/
    component-model/           # engineering concept identity and component definition contracts
    connection-model/          # semantic port and connection vocabulary
    physical-model/            # minimal physical traits
    part-model/                # vendor implementation mapping
    compiler/                  # deterministic resolution pipeline over package-governed packs
    runtime/                   # session-scoped resolved knowledge access
    engineering-model/         # canonical authored engineering structure
    semantic-scm/              # downstream review and history
    projection-model/          # downstream view contracts
    presentation-model/        # downstream presentation contracts
    plugins/                   # hosted extension seams
  extensions/
    domain-electrical/         # electrical concept and semantic-port packs
    vendors/
      siemens/                 # first narrow vendor proof pack
  examples/
    m14/                       # proof repositories for component resolution
```

## Capability -> Architecture Map

| Capability / Area | Lives in | Governed by |
| --- | --- | --- |
| Engineering concept identity and component class contracts | `kernel/component-model` | AD-75, AD-77 |
| Semantic port contracts and connection vocabulary | `kernel/connection-model` | AD-78 |
| Minimal physical trait contracts | `kernel/physical-model` | AD-79 |
| Vendor implementation mapping | `kernel/part-model` | AD-77 |
| Deterministic knowledge-pack loading and conflict detection | `kernel/compiler`, package graph, hosted plugin seams | AD-76, AD-81 |
| Resolved component knowledge access in runtime and IDE | `kernel/runtime`, `ide/lsp` | AD-80 |
| Downstream derived-context, validation, and AI consumption | M9 and M10 layers | AD-80 |
| Downstream projection and presentation consumption | `kernel/projection-model`, `kernel/presentation-model`, downstream packs | AD-80 |

## Deferred

- behavior model and simulation state
- broad standards-pack ecosystem
- broad multi-vendor catalog coverage
- procurement and selection optimization
- direct product-surface palette and form milestone work
- remote knowledge marketplace or dynamic runtime pack downloads

## Final Statement

M14 should prove:

> Athena can resolve what an engineering component actually is through governed, vendor-neutral, package-locked component knowledge.

And product-wise it should freeze one permanent rule:

> DSL is Athena's canonical serialization.  
> DSL is not Athena's default human interface.  
> All future authoring surfaces converge through one semantic mutation authority before component knowledge resolves downstream.
