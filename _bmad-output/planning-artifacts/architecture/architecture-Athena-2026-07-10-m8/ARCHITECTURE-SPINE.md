---
title: 'Athena M8'
type: architecture-spine
purpose: build-substrate
altitude: initiative
paradigm: 'runtime-owned unified semantic mutation with projection-aware command surfaces and downstream graph clients'
scope: 'Athena M8 unified semantic mutation model'
status: draft
created: '2026-07-10'
updated: '2026-07-10'
binds:
  - 'FR-1'
  - 'FR-2'
  - 'FR-3'
  - 'FR-4'
  - 'FR-5'
  - 'FR-6'
  - 'FR-7'
  - 'FR-8'
sources:
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md'
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md'
  - 'draft/m8/003-draft.md'
  - 'docs/roadmap/athena-milestone-roadmap.md'
  - 'docs/usages/athena-workspace-summary.md'
companions:
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md'
---

# Architecture Spine - Athena M8

## Design Paradigm

Athena M8 is a **runtime-owned unified semantic mutation with projection-aware command surfaces and downstream graph clients** architecture.

- **runtime-owned unified semantic mutation** means all meaningful persisted change converges into one Athena-owned command, validation, and mutation path.
- **projection-aware command surfaces** means source and graph may both initiate supported change, but each change is expressed in Athena mutation vocabulary rather than renderer-local save logic.
- **downstream graph clients** means the graph stack remains a consumer of Athena projection and mutation contracts, not an owner of engineering truth, review vocabulary, or durable edit semantics.

## Inherited Invariants

| Inherited | From parent | Binds here |
| --- | --- | --- |
| AD-13 | `architecture-Athena-2026-07-08-m5` | Repository/package contracts still live in `kernel/repository-model`. |
| AD-17 | `architecture-Athena-2026-07-08-m5` | The active repository remains one runtime-owned `RepositoryGraphSession` per product window. |
| AD-18 | `architecture-Athena-2026-07-08-m5` | IDE work stays additive and product-operability scoped through existing seams. |
| AD-19 | `architecture-Athena-2026-07-09-m6` | Semantic SCM remains a dedicated VCS-neutral core above repository/package meaning. |
| AD-23 | `architecture-Athena-2026-07-09-m6` | Theia-hosted surfaces remain downstream bridges rather than semantic cores. |
| AD-25 | `architecture-Athena-2026-07-09-m6` | Domain-specific enrichments remain additive through hosted plugin contracts. |
| AD-27 | `architecture-Athena-2026-07-09-m7` | `kernel/projection-model` remains the dedicated renderer-neutral projection boundary. |
| AD-28 | `architecture-Athena-2026-07-09-m7` | Engineering identity remains in the object graph; view definitions and renderer assets stay downstream. |
| AD-29 | `architecture-Athena-2026-07-09-m7` | Layout and geometry remain view-scoped metadata, not engineering truth. |
| AD-30 | `architecture-Athena-2026-07-09-m7` | The graphical workbench continues to consume runtime-owned projection sessions through Athena-owned transport. |
| AD-31 | `architecture-Athena-2026-07-09-m7` | Unapproved frontend interaction remains transient; meaningful graphical mutation must route through governed commands. |
| AD-33 | `architecture-Athena-2026-07-09-m7` | Domain projection contributions still enter through extensions, not kernel forks. |

## Invariants & Rules

### AD-34 - M8 Freezes One Mutation Authority Above Source And Graph

- **Binds:** `FR-1`, `FR-2`, `FR-3`, `FR-4`
- **Prevents:** source editing, graph interaction, or future tooling from creating separate persisted change paths with inconsistent validation or review semantics
- **Rule:** M8 defines one mutation authority in Athena runtime. Accepted source-originated and graph-originated changes must converge into Athena-owned command contracts, runtime orchestration, validation, and resulting state refresh. No persisted engineering or projection change may bypass this path through direct renderer save logic, direct editor-local state, or transport shortcuts.

### AD-35 - M8 Distinguishes Semantic Mutation, Projection Mutation, And Transient Interaction Explicitly

- **Binds:** `FR-2`, `FR-4`, `FR-7`
- **Prevents:** projection-only changes from leaking into engineering truth, or transient UI behavior being mistaken for governed persisted state
- **Rule:** Every supported interaction in M8 is classified as one of three categories:
  - **semantic mutation** changes canonical engineering meaning
  - **projection mutation** changes governed projection/layout metadata only
  - **transient interaction** remains frontend-local and non-persisted
  The category is part of the command and ownership contract, not an implicit UI convention.

### AD-36 - Graph Gestures Emit Athena Command Intent, Never Renderer-Native Save State

- **Binds:** `FR-1`, `FR-3`, `FR-8`
- **Prevents:** direct manipulation flows from becoming durable renderer-owned semantics or private frontend mutation protocols
- **Rule:** Supported graph actions must be translated into Athena-owned command intent before runtime evaluates them. Renderer-native concepts such as drag, drop, or direct property edit may exist as UI affordances, but they become durable only through Athena command vocabulary. The current `integrations/graph-glsp` adapter and Theia frontend may deliver gestures and transport payloads, but they may not define persistent mutation meaning.

### AD-37 - Runtime Owns Mutation Evaluation, Rejection, And Refresh

- **Binds:** `FR-1`, `FR-3`, `FR-4`
- **Prevents:** accepted and rejected mutations from leaving divergent private graph state or bypassing validation feedback
- **Rule:** Runtime decides whether a command is accepted, rejected, or returned with validation feedback. Accepted semantic mutations update canonical engineering state; accepted projection mutations update governed projection/layout metadata; rejected mutations do not create durable divergence. In all cases, projection refresh remains runtime-owned and deterministic.

### AD-38 - Unified Review Facts Remain Semantic And Shared Across Interaction Origins

- **Binds:** `FR-5`, `FR-6`, `FR-8`
- **Prevents:** graph-originated changes from inventing a parallel renderer-specific review vocabulary or forking semantic SCM meaning
- **Rule:** Accepted mutation outcomes flow into the same semantic diff, review, and history vocabulary already proven by M6. Source-originated and graph-originated changes may differ in interaction origin, but not in resulting review semantics. `kernel/semantic-scm` remains the downstream review/history authority above accepted mutation outcomes.

### AD-39 - Reveal And Anchoring Must Use Canonical Semantic Identity

- **Binds:** `FR-5`, `FR-6`
- **Prevents:** source, graph, and review surfaces from drifting into different identity schemes or revealing mismatched subjects
- **Rule:** Bidirectional reveal across source, graph, semantic inspection, and semantic SCM must anchor through canonical semantic identity and governed projection references. Graph-local ids, widget ids, and transport ids may exist, but they remain downstream aliases rather than review or mutation authority.

### AD-40 - Every Interactive Projection Publishes An Ownership Contract

- **Binds:** `FR-2`, `FR-7`
- **Prevents:** editability rules from being inferred accidentally from UI behavior, renderer capability, or incomplete documentation
- **Rule:** Each supported projection must explicitly declare:
  - what it can display
  - what it can edit
  - what command intents it may emit
  - what state is transient only
  - what projection metadata may persist
  Unsupported or undeclared mutation attempts must be rejected or snapped back on refresh rather than silently accepted.

### AD-41 - Text Editing And Graph Editing Converge Before Review And Persistence

- **Binds:** `FR-1`, `FR-5`, `FR-6`
- **Prevents:** text-originated and graph-originated workflows from producing equivalent visible outcomes but incompatible persisted semantics or review trails
- **Rule:** Source editing and graph editing may begin through different interaction surfaces, but before persistence and review they must converge into the same governed runtime mutation semantics. This does not require identical frontend affordances; it requires identical mutation authority, validation semantics, and review consequences.

### AD-42 - M8 Must Prove One Real Semantic Edit Path And One Real Projection Edit Path Before Broadening Scope

- **Binds:** `FR-2`, `FR-3`, `FR-4`, `FR-7`
- **Prevents:** M8 from dissolving into a broad editing milestone without freezing the core mutation model first
- **Rule:** M8 remains narrow. The milestone should prove:
  - one semantic mutation path end to end
  - one projection mutation path end to end
  - one rejection or validation feedback path
  - one reveal/review path shared across source and graph
  Broader graphical authoring, notation depth, and domain-rich editing breadth remain later milestones.

```mermaid
flowchart LR
  source[Source editor]
  graph[Graphical workbench]
  frontend[ide/theia-frontend\ninteraction + reveal]
  adapter[integrations/graph-glsp\ntranslation-only adapter]
  lsp[ide/lsp\nAthena-owned transport]
  runtime[kernel/runtime\nmutation authority]
  commands[command contracts]
  engineering[kernel/engineering-model]
  projection[kernel/projection-model]
  layout[kernel/layout-model]
  scm[kernel/semantic-scm]
  review[semantic review facts]

  source --> lsp
  graph --> frontend
  frontend --> adapter
  adapter --> lsp
  lsp --> runtime
  runtime --> commands
  commands --> engineering
  commands --> projection
  projection --> layout
  runtime --> scm
  scm --> review
  runtime --> lsp
```

## Consistency Conventions

| Concern | Convention |
| --- | --- |
| Naming (entities, files, interfaces, events) | Use `MutationAuthority`, `MutationCategory`, `CommandIntent`, `ProjectionOwnershipContract`, `SemanticMutation`, `ProjectionMutation`, `TransientInteraction`, `ReviewFactAnchor`, and `MutationResult` consistently. Avoid naming durable mutation contracts after renderer gestures such as `drag` or `drop`; renderer language may appear only at the adapter/UI edge. |
| Data & formats (ids, dates, error shapes, envelopes) | Every accepted mutation must preserve canonical engineering ids and governed projection refs. Rejection and validation feedback payloads must be inspectable and attributable to a command intent. Review anchors must resolve through canonical semantic identity first, projection aliases second. |
| State & cross-cutting (mutation, errors, logging, config, auth) | Runtime owns mutation evaluation, acceptance, rejection, and refresh. Frontend state remains disposable. Adapter state remains translation-only. Validation feedback, rejection reason, and resulting refresh state should be observable through Athena-owned transport rather than inferred from renderer internals. |
| Build and dependency management | `kernel/runtime`, `kernel/engineering-model`, `kernel/projection-model`, and `kernel/semantic-scm` continue to own semantics and review meaning. `integrations/graph-*` may depend on graph framework vocabulary but remains downstream. `ide/*` consumes and presents mutation outcomes only through Athena-owned protocol seams. |

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

```mermaid
flowchart TB
  repo[Engineering Repository]
  editor[Source editor]
  graph[Graph workbench]
  frontend[ide/theia-frontend]
  adapter[integrations/graph-glsp]
  lsp[ide/lsp]
  runtime[kernel/runtime]
  commands[command contracts]
  engineering[kernel/engineering-model]
  projection[kernel/projection-model]
  layout[kernel/layout-model]
  scm[kernel/semantic-scm]
  extensions[extensions/domain-*]

  repo --> editor
  editor --> lsp
  graph --> frontend
  frontend --> adapter
  adapter --> lsp
  lsp --> runtime
  runtime --> commands
  commands --> engineering
  commands --> projection
  projection --> layout
  projection --> extensions
  runtime --> scm
```

```text
Athena/
  kernel/
    engineering-model/          # canonical engineering truth
    projection-model/           # renderer-neutral projection contracts
    layout-model/               # persisted layout/projection metadata
    runtime/                    # mutation authority, command execution, refresh
    semantic-scm/               # semantic review, diff, history, reveal anchors
  ide/
    lsp/                        # sole IDE semantic/projection/mutation transport entry point
    theia-frontend/             # source + graph reveal, interaction wiring, feedback surfaces
    theia-backend/              # service lifecycle and transport wiring
  integrations/
    graph-glsp/                 # translation-only graph adapter for current proof stack
  extensions/
    domain-*/                   # domain semantics, view definitions, projection contracts
  examples/
    m8/                         # future unified mutation proof corpus
```

## Capability -> Architecture Map

| Capability / Area | Lives in | Governed by |
| --- | --- | --- |
| One mutation authority for source and graph | `kernel/runtime`, command contracts, `ide/lsp` | AD-34, AD-41 |
| Mutation-category classification | `kernel/runtime`, projection ownership contracts | AD-35, AD-40 |
| Graph gesture to Athena command intent | `ide/theia-frontend`, `integrations/graph-glsp`, `ide/lsp`, `kernel/runtime` | AD-36, AD-37 |
| Deterministic refresh after accepted mutation | `kernel/runtime`, `kernel/projection-model`, `kernel/layout-model` | AD-35, AD-37 |
| Unified semantic review model | `kernel/semantic-scm`, `kernel/runtime`, `ide/lsp` | AD-38, AD-41 |
| Reveal and anchoring across source, graph, and review | `ide/theia-frontend`, `ide/lsp`, `kernel/runtime`, `kernel/semantic-scm` | AD-39 |
| Projection ownership contracts | `kernel/projection-model`, `extensions/domain-*`, runtime mutation policy | AD-40 |

## Deferred

- Broad unrestricted graphical authoring is deferred beyond the first M8 proof.
- Final notation-library depth and richer symbol-pack editing remain later than M8.
- Multi-user approval workflow redesign remains later than M8 even if future commands gain approval-oriented states.
- Full graph-stack replacement or alternate runtime integration remains deferred; the current proof stack continues to use `integrations/graph-glsp` as a downstream client.
- Wider domain authoring breadth across multiple industrial verticals remains deferred until one unified mutation authority is proven cleanly.
