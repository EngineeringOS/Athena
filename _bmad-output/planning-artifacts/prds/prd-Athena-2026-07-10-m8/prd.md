---
title: Athena M8
status: draft
created: 2026-07-10
updated: 2026-07-10
---

# PRD: Athena M8

*Codename: Athena Unified Semantic Mutation Model.*

## 0. Document Purpose

This PRD defines the M8 product requirements for Athena after the completed M7 milestone.

M8 exists to close the next platform gap intentionally left open by M7:

> Athena can now project engineering meaning into a real graphical workbench without moving semantic authority into the frontend. M8 must now prove that engineering changes can originate from either text or graph interaction while still flowing through one mutation authority, one validation path, and one source of truth.

This PRD is capability-first. It builds on the completed M7 PRD and M7 architecture spine, the roadmap under `docs/roadmap/`, the current workspace summary, and the merged M8 draft under `draft/m8/003-draft.md`. Implementation-shaped detail that is useful but too low-level for the PRD body is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved authored DSL to canonical semantic model. M1 proved runtime-owned workspace state, commands, history, and semantic diff. M2 proved explicit `Layout IR` and `Geometry IR` as downstream projections. M3 proved stable plugin-hosted extensibility. M4 proved the first serious Athena IDE shell on Theia. M5 proved governed repository/package meaning. M6 proved semantic SCM, semantic review, semantic commit intent, and package-aware semantic history. M7 proved graphical projection and a graph-first professional workbench without surrendering semantic authority.

M8 must now prove the next layer that makes Athena credible as a real engineering editing environment instead of only a semantic platform with text plus graph inspection:

- one governed mutation authority for both source and graph
- explicit mutation categories across semantic, projection, and transient interaction concerns
- graph-originated actions expressed as Athena commands instead of local canvas state
- unified review and reveal across source, graph, and semantic SCM
- projection ownership contracts that say clearly what a graphical surface may display, edit, emit, and own

In other words, M7 proved that Athena can show engineering meaning graphically. M8 must prove that Athena can let engineers act through either representation without splitting truth, review, or validation into separate worlds.

## 1.1 Why Now

The next technical risk is no longer whether Athena can host a graph-first workbench.

Today the workspace already has the needed upstream proof:

- M1 already proved runtime-owned command execution and history
- M2 already proved explicit projection layers and runtime-owned view sessions
- M6 already proved semantic SCM, semantic review, and semantic history
- M7 already proved projection authority, graph-first workbench delivery, and translation-only graph adaptation

That is exactly why M8 can become the mutation milestone:

- semantic authority is already upstream
- projection authority is already upstream
- graphical interaction no longer has to invent its own model to become useful
- review and reveal no longer have to be text-only

Starting M8 earlier would have risked mixing mutation, review, and graphical delivery before the projection boundary was stable.

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need Athena to accept engineering changes through either source editing or graphical interaction without creating two different truths.
- Reviewers need semantic review facts to stay coherent across source, graph, and semantic SCM views.
- Platform engineers need one governed mutation model that classifies semantic change, projection change, and transient interaction explicitly.
- Product and architecture owners need proof that Athena can evolve from viewer-plus-editor into a real engineering workbench without collapsing into canvas-owned state.

### 2.2 Non-Users (M8)

- Teams expecting M8 to revisit repository/package contracts already frozen by M5
- Teams expecting M8 to revisit semantic SCM foundations already frozen by M6
- Teams expecting M8 to redo the M7 renderer and graphical delivery proof
- Teams expecting M8 to become a freeform CAD or whiteboard editor
- Teams expecting M8 to finalize symbol packs, notation libraries, or broad EPLAN/QElectroTech-class authoring depth

### 2.3 Key User Journeys

- **UJ-1. Aaron changes engineering intent from the graph without creating local canvas truth.**
  - **Persona + context:** Aaron is validating that the graph is a real engineering work surface rather than only a viewer.
  - **Entry state:** An active Athena repository session and graphical projection are already open.
  - **Path:** Aaron performs a supported graphical action. Athena translates that action into a governed command intent, validates it, mutates the canonical model or projection metadata through runtime, and refreshes the graph deterministically.
  - **Climax:** Aaron sees the change take effect without the frontend owning semantics or private saved state.
  - **Resolution:** Athena proves that graph interaction can be real while remaining downstream of one mutation authority.

- **UJ-2. Maya edits source and sees graph and review context stay coherent.**
  - **Persona + context:** Maya moves between authored `.athena` source, graph view, and semantic review surfaces.
  - **Entry state:** Source, graph, and semantic SCM surfaces are open together in the current workbench.
  - **Path:** Maya edits source or accepts a graph-originated change. Athena routes both through the same semantic command and validation path, then refreshes graph and review context from the resulting state.
  - **Climax:** Maya can move between source and graph without semantic drift or duplicate review meaning.
  - **Resolution:** Athena proves that code and graph are two clients of one mutation/review model.

- **UJ-3. Priya verifies which parts of a projection are editable and why.**
  - **Persona + context:** Priya is reviewing whether the first interactive graphical workflows preserve the architecture invariants needed for later growth.
  - **Entry state:** M8 mutation rules and graphical interaction paths are available in the IDE.
  - **Path:** Priya inspects supported actions, rejection paths, persisted projection changes, and transient interaction behavior for one or more projections.
  - **Climax:** Priya can explain exactly which actions are semantic, which are projection-only, which are transient, and which are disallowed.
  - **Resolution:** Athena gains a credible and governable editing model rather than a pile of ad hoc UI behaviors.

## 3. Glossary

- **Mutation Authority** - the single governed path through which Athena accepts and persists meaningful engineering or projection change.
- **Semantic Mutation** - a change that alters engineering meaning in canonical state.
- **Projection Mutation** - a change that alters representation or persisted layout/projection metadata without changing engineering meaning.
- **Transient Interaction** - non-persisted UI behavior such as pan, zoom, hover, selection, or temporary highlight.
- **Command Intent** - the Athena-owned expression of a requested change before runtime decides whether it is valid and allowed.
- **Projection Ownership Contract** - the explicit rule set that states what a projection can display, edit, emit, and own locally.
- **Unified Review Model** - the rule that accepted mutations produce semantic review facts visible coherently across source, graph, and semantic SCM surfaces.

## 4. Features

### 4.1 One Mutation Authority For Code And Graph

**Description:** Athena must define one mutation authority that governs both source-originated and graph-originated changes. Realizes UJ-1, UJ-2, UJ-3.

#### FR-1: Route All Meaningful Changes Through Athena Commands

Athena can keep one mutation authority for both text and graph interaction. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- Accepted source-originated and graph-originated changes route through Athena command contracts and runtime orchestration.
- No graphical action may persist engineering truth directly through canvas-local state.
- The same mutation path remains subject to canonical validation before persisted outcome is accepted.

#### FR-2: Distinguish Semantic Mutation From Projection Mutation

Athena can classify meaningful changes explicitly. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- M8 defines explicit categories for semantic mutation, projection mutation, and transient interaction.
- Semantic mutation changes canonical engineering meaning.
- Projection mutation changes persisted representation only.
- Transient interaction remains non-persisted UI behavior.

### 4.2 Graph-Originated Command Integration

**Description:** Athena must turn supported graph gestures into Athena-owned command intents instead of private frontend state transitions. Realizes UJ-1, UJ-3.

#### FR-3: Express Graph Gestures As Athena Command Intents

Athena can translate supported graph operations into Athena-owned command intents. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Supported graph gestures are represented as Athena commands or command intents rather than renderer-local save logic.
- Runtime can accept, reject, or return validation feedback for graph-originated command requests.
- Command identity and payload remain inspectable for development and architecture review.

#### FR-4: Refresh Projection Deterministically After Accepted Mutation

Athena can refresh graphical state deterministically after an accepted change. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Accepted semantic mutation refreshes graph state from canonical upstream meaning.
- Accepted projection mutation refreshes graph state from governed projection/layout metadata.
- Rejected graph-originated mutations do not leave durable local divergence behind.

### 4.3 Unified Review And Reveal

**Description:** M8 must unify change understanding across source, graph, and semantic SCM instead of letting each surface describe mutation differently. Realizes UJ-2, UJ-3.

#### FR-5: Produce Unified Semantic Review Facts For Accepted Mutations

Athena can route accepted mutation outcomes into one review model. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Accepted mutations produce semantic diff and review facts through the same review pipeline.
- Graph-originated and source-originated changes do not create separate review vocabularies.
- Semantic SCM remains the downstream review/history authority rather than the graph or editor.

#### FR-6: Support Bidirectional Reveal Across Source, Graph, And Review Context

Athena can support coherent reveal and navigation across representations. Realizes UJ-2.

**Consequences (testable):**
- Source can reveal the corresponding graph context.
- Graph can reveal the corresponding source and semantic context.
- Review facts can anchor to both source and graph through canonical semantic identity.
- The same repository and semantic state produce the same reveal outcome.

### 4.4 Projection Ownership Contracts

**Description:** M8 must make projection editability explicit so future growth does not reverse-engineer behavior from UI accidents. Realizes UJ-1, UJ-3.

#### FR-7: Publish Projection Ownership Contracts

Athena can define what each projection may display, edit, emit, and own. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Each supported projection explicitly declares what it can display.
- Each supported projection explicitly declares what it can edit.
- Each supported projection explicitly declares what command intents it may emit.
- Each supported projection explicitly declares which local state is transient only and which projection metadata may persist.

#### FR-8: Preserve Renderer-Neutral Mutation Semantics

Athena can keep mutation semantics independent from the current renderer stack. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- Graph frameworks remain downstream clients of Athena mutation contracts.
- Renderer-local edit models do not become semantic authority or durable review authority by convention.
- M8 keeps the current architectural rule that command meaning, semantic identity, and review semantics remain Athena-owned.

## 5. Non-Goals (Explicit)

- M8 does not revisit repository/package graph authority that belongs to M5.
- M8 does not revisit semantic SCM foundations that belong to M6.
- M8 does not redo the M7 renderer-proof and workbench-delivery milestone.
- M8 does not become unrestricted graphical authoring.
- M8 does not become a symbol-library or final notation-system milestone.
- M8 does not become generic IDE polish unrelated to the mutation model.

## 6. MVP Scope

### 6.1 In Scope

- one governed mutation authority for code and graph
- explicit mutation categories
- one real graph-originated command path
- one real source-originated mutation path synchronized through graph refresh
- unified semantic review facts for accepted mutations
- bidirectional reveal between source, graph, and review context
- explicit projection ownership contract for the first supported projections

### 6.2 Out Of Scope For MVP

- unrestricted graphical authoring
- broad multi-user collaboration or approval workflow redesign
- final notation library depth
- full UX skin/token finalization
- broad domain authoring parity with mature ECAD products

## 7. Success Metrics

**Primary**

- **SM-1:** Athena can accept supported graph-originated changes without creating canvas-owned truth.
- **SM-2:** Athena can route source-originated and graph-originated changes through one runtime-owned mutation authority.
- **SM-3:** Athena can distinguish semantic mutation, projection mutation, and transient interaction explicitly.
- **SM-4:** Athena can refresh graph state deterministically after accepted mutation and discard rejected local divergence.
- **SM-5:** Athena can produce one semantic review model for accepted mutations regardless of their interaction origin.
- **SM-6:** Athena can define explicit projection ownership contracts for the first supported interactive projections.

**Secondary**

- **SM-7:** M8 prepares later richer domain editing without reopening mutation authority or review semantics.
- **SM-8:** M8 proves that the current graph stack can remain a downstream client of Athena mutation semantics.

**Counter-metrics**

- **SM-C1:** Do not optimize for rich canvas behavior over mutation governance.
- **SM-C2:** Do not optimize for fast direct manipulation if it bypasses validation or review.
- **SM-C3:** Do not optimize for renderer convenience if it weakens Athena-owned command meaning.

## 8. Cross-Cutting NFRs

- **NFR-1 Mutation Authority Preservation:** Meaningful changes must route through one Athena-owned mutation path.
- **NFR-2 Semantic Authority Preservation:** Canonical engineering meaning remains upstream of any renderer or editor client.
- **NFR-3 Determinism:** The same accepted mutation over the same state yields the same resulting canonical and projection state.
- **NFR-4 Inspectability:** Command intents, mutation outcomes, rejection paths, and review facts must remain inspectable for development and architecture debugging.
- **NFR-5 Review Coherence:** Graph-originated and source-originated mutations must share one semantic review and history vocabulary.
- **NFR-6 Renderer Neutrality:** The current graph stack may implement interaction delivery, but it must not own command meaning or durable mutation semantics.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- The current Athena workbench remains the product host for M8.
- Supporting IDE work is allowed only where it directly improves the governed mutation model.
- M8 should prove operational editing seriousness, not a flashy canvas demo.

### 9.2 Architectural Guardrails

- Compiler and runtime remain semantic authorities.
- `ide/lsp` remains the sole IDE semantic and projection entry point.
- Source edits and graph edits must converge into the same runtime-owned mutation path.
- Projection ownership contracts must stay explicit and inspectable.
- Semantic SCM remains the downstream review/history authority above accepted change outcomes.

### 9.3 Roadmap Guardrails

- M8 owns governed bidirectional mutation and review across code and graph.
- M8 does not reopen M5, M6, or M7 milestone centers.
- Final notation depth and broad domain authoring remain later than M8.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4 to M7
- **Primary delivery target:** local developer-run product shell plus deterministic JVM, IDE, and mutation verification
- **Primary runtime authority:** runtime-backed semantic and projection state
- **Primary text/language-service foundation:** Athena LSP
- **Primary graphical delivery concern:** graph-originated interaction routed through Athena-owned command meaning

## 11. Open Questions

1. What is the narrowest useful first semantic mutation to prove through graph-originated interaction?
2. What is the narrowest useful first projection mutation to prove through graph-originated interaction?
3. Should the first graph-originated mutation path target electrical `cabinet`, `wiring`, or another projection first?
4. How should Athena surface rejection and validation feedback inside the graph-first workbench without creating renderer-owned error semantics?
5. Which mutation outcomes must immediately appear in semantic SCM versus remaining local runtime feedback only until later workflow steps?

## 12. Assumptions Index

- M8 should build on the completed M7 projection boundary instead of reopening it.
- M8 should preserve the same mutation and semantic authority model already proven by M0 through M7.
- M8 should prove one real semantic edit path and one real projection edit path before widening scope.
- M8 should keep graph frameworks downstream of Athena command and review meaning.
- M8 should prepare richer engineering workbench behavior later without turning M8 into unrestricted graphical authoring.
