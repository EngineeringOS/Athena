---
title: Athena M15
status: draft
created: 2026-07-13
updated: 2026-07-13
---

# PRD: Athena M15

*Codename: Athena Guided Semantic Authoring Foundation.*

## 0. Document Purpose

This PRD defines the next milestone after M14.

M14 proved that Athena can resolve governed component knowledge above canonical `Engineering IR` and below later reasoning, projection, and presentation consumers.

That success exposes the next real product gap:

> Athena can now understand what an authored component is, but it still has not proven that a mainstream engineer can create and modify engineering intent without directly authoring canonical DSL.

M15 therefore exists to prove **guided semantic authoring** above the completed M8 unified mutation authority and above the completed M14 component-knowledge foundation.

M15 freezes one product rule:

> DSL is Athena's canonical serialization.  
> DSL is not Athena's required default authoring surface.

## 1. Vision

M0 proved DSL to `Engineering IR`.
M1 proved runtime-owned workspace and mutation orchestration.
M2 proved explicit projection layers.
M3 proved hosted extensibility.
M4 proved the first serious IDE shell.
M5 proved governed repository meaning and package graph resolution.
M6 proved semantic SCM.
M7 proved graphical projection and the first renderer path.
M8 proved one mutation authority across source and graph.
M9 proved executable engineering knowledge.
M10 proved AI-assisted reasoning above governed knowledge outputs.
M11 proved serious electrical multi-view workbench depth.
M12 proved renderer trust and operator-surface hardening.
M13 proved a real presentation language foundation.
M14 proved a governed component-knowledge foundation.

M15 must prove the next strategic layer:

- engineers can create engineering intent without writing raw DSL directly
- guided authoring surfaces still converge through the single M8 mutation authority
- component placement is driven by governed component knowledge rather than frontend hardcoding
- property editing is governed semantic mutation rather than local widget state
- connection creation is port-aware and semantically filtered rather than generic line drawing
- source, graph, inspector, diagnostics, and semantic SCM remain one identity everywhere

In plain terms:

- M14 proved Athena understands components
- M15 proves engineers can use those components without understanding Athena internals

## 1.1 Why Now

The current gap is no longer primarily:

- repository governance
- semantic SCM
- first graph rendering
- one mutation authority
- component identity and vendor mapping
- downstream presentation-language foundations

The current gap is:

- the product still asks too much semantic awareness from mainstream users
- the strongest non-DSL authoring path is still too thin
- component knowledge exists, but guided authoring does not yet operationalize it
- graph interaction still risks collapsing into frontend-driven behavior if authoring is not formalized
- later AI, template, form, and catalog experiences need one authoring contract rather than separate UI-specific paths

Without solving that gap, Athena risks becoming:

- a strong kernel with a weak product surface
- a DSL-first system that mainstream engineers will avoid
- a frontend-by-frontend collection of authoring shortcuts
- an architecture where palette, graph, or inspector code can start bypassing canonical mutation semantics

M15 is the correct milestone to solve this because:

- M8 already provides the unified mutation authority
- M14 already provides the component-knowledge substrate
- M12 and M13 already provide a trustworthy downstream workbench and presentation path
- future AI, template, and form-based surfaces need a stable guided-authoring contract first

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need to create and edit components through guided workbench surfaces without authoring raw DSL directly.
- Product teams need Athena to prove that guided authoring can stay semantically governed instead of becoming canvas-local behavior.
- Platform engineers need one authoring-intent boundary that later palette, inspector, forms, templates, AI, and API surfaces can all share.
- Domain-extension authors need their M14 component knowledge to appear in usable authoring surfaces rather than only in compiler or runtime diagnostics.

### 2.2 Non-Users (M15)

- Teams expecting M15 to become full EPLAN replacement
- Teams expecting M15 to become broad multi-vendor catalog parity
- Teams expecting M15 to become final routing or wire-layout automation
- Teams expecting M15 to become macro authoring or template authoring depth
- Teams expecting M15 to become AI-generation as the primary entry path
- Teams expecting M15 to introduce a second mutation authority outside M8

## 3. Strategic Decision

M15 is a **guided authoring milestone**, not a palette-only milestone.

Why:

- palette is one UI implementation
- inspector is another UI implementation
- graph-originated connect flow is another implementation
- future AI, forms, templates, search, and agent flows must all converge through the same authoring contract

The architectural rule is:

```text
Palette / Inspector / Connect Flow / Graph / Forms / Templates / AI
        ->
Authoring Intent
        ->
Authoring Service
        ->
M8 Semantic Mutation Authority
        ->
Engineering IR
        ->
M14 Component Knowledge
        ->
M9 Knowledge / M13 Presentation / Workbench
```

M15 must therefore avoid:

```text
palette
    ->
graph node
```

or:

```text
inspector
    ->
frontend state update
```

Those paths would break the architecture.

## 4. Product Position

Athena should let mainstream engineers work through guided product surfaces while preserving machine-readable, inspectable semantic authority.

The correct long-term model is:

```text
Palette / Inspector / Connect Flow / Graph / Forms / Templates / AI / DSL / API
        ->
Guided Authoring Intent
        ->
Semantic Command And Mutation Layer
        ->
Engineering IR
        ->
Component Knowledge / Knowledge Runtime / Projection / Presentation / Renderer
```

Consequences:

- DSL remains canonical serialization
- direct DSL authoring remains available for power users, automation, vendors, and AI agents
- guided authoring becomes the first serious mainstream product path
- the current Theia workbench becomes a serious creation surface, not only an inspection surface
- M15 does not claim final authoring breadth, but it must prove the product direction is viable

## 5. Features

### 5.1 Guided Authoring Foundation

**Description:** Athena introduces a guided authoring layer that converts user-facing creation or edit operations into governed authoring intents above M8 mutation authority.

#### FR-1: Publish Authoring Intent As A First-Class Platform Contract

Athena can represent guided authoring requests as stable intent contracts rather than as frontend-specific behavior.

**Consequences (testable):**
- Athena defines authoring intents for component creation, component property update, connection creation, reveal, and review.
- One authoring intent may expand into one or more governed mutation operations.
- Authoring intents remain platform contracts rather than extension-local widget behavior.

#### FR-2: Preserve M8 As The Only Mutation Authority

Athena can route all guided authoring through the completed unified mutation authority.

**Consequences (testable):**
- Palette, inspector, and connect flows do not bypass M8.
- No guided authoring surface writes canonical semantic state directly.
- Every accepted guided action remains reviewable through the same mutation path used elsewhere.

### 5.2 Guided Component Placement

**Description:** Athena introduces a component palette and search-driven insertion flow backed by M14 component knowledge.

#### FR-3: Publish A Governed Component Palette From Active Component-Knowledge Packs

Athena can list available authorable components from governed active packs.

**Consequences (testable):**
- The Theia workbench exposes a left-side Athena component panel.
- Available components derive from active component-knowledge packs, not from hardcoded frontend lists.
- The first proof can group components by narrow electrical categories such as PLC, power supply, motor, and contactor.

#### FR-4: Insert Components Through Guided Placement Intents

Athena can insert one component instance into the current engineering workspace through a governed intent path.

**Consequences (testable):**
- Double-click, command, or drag/drop insertion emits a guided placement intent rather than directly creating a graph node.
- Placement resolves to canonical semantic mutation through M8.
- Accepted insertion updates source, graph, diagnostics, and review state coherently.

### 5.3 Inspector Editing

**Description:** Athena introduces a property inspector for governed component editing.

#### FR-5: Publish A Component Inspector From Canonical Semantic Identity

Athena can display selected-component properties through one shared semantic identity.

**Consequences (testable):**
- The Theia workbench exposes a right-side Athena inspector panel.
- The inspector can show component name, engineering concept, vendor implementation, semantic ports, and minimal physical traits.
- The inspector always binds to canonical semantic identity instead of ephemeral frontend instance identity.

#### FR-6: Edit Component Properties Through Governed Update Intents

Athena can update selected component properties through guided semantic intents.

**Consequences (testable):**
- The first proof can edit at least tag or name, vendor implementation choice, description, and custom labels.
- Inspector edits emit governed update intents rather than local frontend state writes.
- Accepted edits refresh source, graph, diagnostics, and inspector state coherently.

### 5.4 Port-Aware Connection Authoring

**Description:** Athena introduces a connection flow that is aware of semantic ports and compatible target filtering.

#### FR-7: Start Connection Authoring From Semantic Ports

Athena can begin one connection operation from a known semantic port.

**Consequences (testable):**
- A user can start connection authoring from a selected source port on the graph surface.
- Connection authoring remains driven by semantic port identity rather than generic edge drawing.
- The first proof may use direct straight-line connections rather than a full routing engine.

#### FR-8: Filter Allowed Targets By Port Meaning Rather Than Graph Shape

Athena can constrain suggested or selectable connection targets by M14 semantic-port meaning.

**Consequences (testable):**
- Compatible targets are filtered by stable direction and signal-family constraints.
- Incompatible targets do not appear as valid completion targets in the guided flow.
- Richer engineering sufficiency remains later M9 knowledge logic, not an M15 rule-engine explosion.

#### FR-9: Create Connections Through Governed Connect Intents

Athena can create one connection through a guided intent that resolves into canonical mutation.

**Consequences (testable):**
- A successful connect flow emits a governed connect intent rather than direct graph-edge creation.
- Accepted connection creation updates source, graph, inspector connection state, and diagnostics coherently.
- The first proof can demonstrate at least one compatible communication or power connection slice.

### 5.5 Unified Reveal And Three-Way Synchronization

**Description:** Athena proves that source, graph, and guided-authoring surfaces remain synchronized around one semantic identity.

#### FR-10: Keep Source, Graph, And Guided Authoring In Sync

Athena can keep component and connection state coherent across the main authoring surfaces.

**Consequences (testable):**
- Panel action -> M8 mutation -> `Engineering IR` -> graph refresh
- Graph action -> M8 mutation -> `Engineering IR` -> inspector refresh
- DSL edit -> compiler -> `Engineering IR` -> panel and graph refresh
- The same semantic subject can be revealed from source, graph, inspector, and semantic SCM surfaces

#### FR-11: Preserve One Identity Everywhere

Athena can reveal one selected subject consistently across the major workbench surfaces.

**Consequences (testable):**
- A selected component can reveal source, graph, inspector, and semantic SCM state.
- Guided authoring does not invent a second identity or surface-local ownership model.
- Review and diagnostics remain tied to canonical semantic subjects.

### 5.6 Mutation Preview And Commit

**Description:** Athena reuses the review-first product direction so guided actions remain inspectable before acceptance.

#### FR-12: Preview Guided Mutations Before Acceptance

Athena can preview the semantic consequences of one guided authoring action.

**Consequences (testable):**
- Insert, update, and connect flows surface pending mutation previews before commitment.
- A preview can show what will be added or changed, not just that a UI action occurred.
- Guided authoring preview reuses the M6 and M8 review-first product direction rather than inventing an opaque frontend confirmation.

#### FR-13: Commit Accepted Guided Mutations Into Canonical State

Athena can persist accepted guided authoring changes through the canonical mutation path.

**Consequences (testable):**
- After approval, changes persist into canonical source and derived runtime state.
- Guided authoring actions appear in mutation or review history rather than disappearing into frontend-only state.
- Rejected previews do not partially mutate canonical engineering state.

## 6. Demo Scenario

The narrow M15 proof should demonstrate:

1. Open a governed repository.
2. Search for `CPU313C` in the Athena component panel.
3. Insert one PLC CPU instance through guided placement.
4. Edit the instance tag to `PLC1` in the inspector.
5. Insert one 24V power supply.
6. Create one compatible connection through a port-aware connect flow.
7. Preview the resulting mutation set.
8. Approve the mutation.
9. Verify graph refresh, source refresh, diagnostics refresh, and inspector refresh all remain coherent.

If that demo works, Athena has proven:

> engineers can create governed engineering intent without directly authoring canonical DSL.

## 7. Success Metrics

### 7.1 Functional Metrics

- A user can insert at least five governed components into one narrow electrical proof repository without writing DSL manually.
- A user can edit at least three supported component properties entirely through guided authoring surfaces.
- A user can create at least one valid port-aware connection and be blocked from at least one incompatible target.
- Every accepted guided action is visible in source, graph, and review state.

### 7.2 Product Metrics

- The proof does not require the user to understand `Engineering IR`, `Presentation IR`, or internal mutation service design.
- The workbench demonstrates guided creation rather than only inspection.
- The milestone proves product viability for mainstream-authoring direction, not just kernel extensibility.

## 8. Scope Boundaries

### 8.1 In Scope

- guided authoring intent contracts
- component palette backed by active knowledge packs
- narrow inspector editing for selected component properties
- port-aware connect flow with basic semantic filtering
- three-way synchronization across source, graph, and guided authoring surfaces
- mutation preview and approval before commit
- one narrow Siemens-first electrical proof slice

### 8.2 Explicitly Out Of Scope

- broad multi-vendor ingestion
- full EPLAN-class routing
- macro authoring and template libraries
- AI generation as the primary workflow
- behavior and simulation modeling
- final cabinet editor depth
- unrestricted electrical compatibility reasoning
- second mutation path outside M8

## 9. Implementation Shape (Non-Binding Product Guidance)

M15 is expected to require:

- platform-side authoring contracts and runtime services above M8
- workbench surfaces for palette and inspector in the current Theia product shell
- graph-side connect tooling that still routes through semantic intent rather than direct graph mutation
- LSP requests that expose available components and guided authoring operations through existing product seams

This section is intentionally product-level, not the final architecture spine.

## 10. Open Questions For Architecture

1. Should authoring contracts live as `kernel/authoring-model` and `kernel/authoring-runtime`, or should runtime orchestration stay partially inside existing runtime modules?
2. How should authoring intent expand into one or more mutation operations without leaking frontend assumptions into M8?
3. Which current M6 or M8 review models can be reused directly for guided mutation preview, and which need a thin authoring-specific wrapper?
4. How much connection compatibility belongs in M15 authoring filtering versus later M9 sufficiency logic?
5. Which workbench surfaces should own palette and inspector state in the Theia product shell while still preserving one semantic identity everywhere?

## 11. Final Statement

M14 proved:

> Athena understands components.

M15 must prove:

> engineers can use those components without directly understanding Athena internals.

That is the first serious market-validation milestone for Athena as a product rather than only as a technical architecture proof.
