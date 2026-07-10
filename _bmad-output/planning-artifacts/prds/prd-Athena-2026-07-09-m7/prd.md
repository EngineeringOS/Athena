---
title: Athena M7
status: draft
created: 2026-07-09
updated: 2026-07-10
---

# PRD: Athena M7

*Codename: Athena Graphical Projection And Visual Workbench Proof.*

## 0. Document Purpose

This PRD defines the M7 product requirements for Athena after the completed M6 milestone.

M7 exists to close the next platform gap intentionally left open by M6:

> Athena can now understand repository change semantically, prepare review and commit intent through the JVM semantic path, and surface package-aware history in the current IDE shell. M7 must now prove that real graphical projection can live in the product without turning canvas state into engineering truth.

This PRD is capability-first. It builds on the completed M6 PRD and M6 architecture spine, the roadmap under `docs/roadmap/`, the current workspace summary, the M7 draft under `draft/m7/001-draft.md`, and the already-proven M2 layout/geometry and M4-M6 IDE seams. Implementation-shaped detail that is useful but too low-level for the PRD body is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved authored DSL to canonical semantic model. M1 proved runtime-owned workspace state, commands, history, and semantic diff. M2 proved explicit `Layout IR` and `Geometry IR` as downstream projections. M3 proved stable plugin-hosted extensibility. M4 proved the first serious Athena IDE shell on Theia. M5 proved governed repository/package meaning. M6 proved semantic SCM, semantic review, semantic commit intent, and package-aware semantic history.

M7 must now prove the next layer that makes Athena credible as a professional engineering workbench instead of only a text-first semantic platform:

- a governed graphical projection boundary
- real visual workbench surfaces under the existing product shell
- read-only and later limited interactive projection behavior without moving semantic truth into frontend or canvas state
- projection-oriented inspection and navigation that remain downstream of canonical semantic, layout, geometry, repository, and SCM meaning

In other words, M6 proved how Athena understands change semantically. M7 must prove how Athena presents and navigates engineering meaning graphically while keeping the same semantic authority model intact.

The renderer reference discussion under `draft/open/2026-07-09-Eplan-cross-compare-discuss.md` sharpens one more constraint for M7: Athena must treat the semantic core as an engineering object graph with multiple possible projections, not as a drawing-first device model. M7 therefore is not about making the canvas smarter than the kernel. It is about proving that one semantic reality can drive renderer targets safely inside the workbench.

## 1.1 Why Now

The next technical risk is no longer repository/package meaning or semantic change meaning.

Today the workspace already has the needed upstream proof:

- M2 already proved explicit `Layout IR`, `Geometry IR`, and synchronized multi-view runtime projections
- M4 already proved a real Theia desktop shell and additive professional workbench
- M5 already froze repository/package authority
- M6 already froze semantic review/history authority

That is exactly why M7 can become graphical:

- semantic authority is already upstream
- graphical projection no longer has to invent package or SCM meaning
- visual workbench capability can now consume semantic, layout, geometry, repository, and review state instead of defining them

Starting M7 earlier would have risked solving presentation before meaning was stable.

The cross-compare renderer note also changes the quality bar for "graphical" proof. A successful M7 is not merely a canvas that draws symbols. It is a workbench that projects engineering objects, relationships, and later view definitions without confusing renderer assets with semantic entities.

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need Athena to project canonical engineering meaning into real diagrammatic surfaces without losing semantic authority.
- Reviewers need visual inspection and navigation over the same semantic truth already proven in text and structured review flows.
- Platform engineers need one governed projection boundary that keeps protocol, view model, and interaction rules explicit.
- Product and architecture owners need proof that Athena can become a real visual engineering environment without collapsing into canvas-owned state.

### 2.2 Non-Users (M7)

- Teams expecting M7 to revisit repository/package contracts already frozen by M5
- Teams expecting M7 to revisit semantic review/history authority already frozen by M6
- Teams expecting M7 to replace Athena LSP or replace the current IDE shell
- Teams expecting M7 to become final freeform graphical editing or full ECAD parity
- Teams expecting M7 to lock one graphics technology before Athena evaluates current constraints

### 2.3 Key User Journeys

- **UJ-1. Aaron opens an Athena repository and sees a real graphical semantic projection in the workbench.**
  - **Persona + context:** Aaron is validating that Athena can present engineering meaning visually without losing the semantic compiler/runtime authority model.
  - **Entry state:** An active Athena repository session already exists in the IDE.
  - **Path:** Aaron opens a graphical Athena view. Athena resolves runtime-owned projection state from canonical semantics and presents a view that can be inspected and navigated visually.
  - **Climax:** Aaron can understand system structure and relationships visually while still trusting that semantics remain upstream.
  - **Resolution:** Athena becomes credible as a visual engineering workbench, not only a semantic text environment.

- **UJ-2. Maya inspects graphical and textual projections side by side without semantic drift.**
  - **Persona + context:** Maya needs to compare authored DSL, semantic inspection, and graphical projection in one professional workspace.
  - **Entry state:** Maya has source open and a supported graphical view available.
  - **Path:** Maya opens source, semantic inspection, and graphical projection together in a split workbench where the graph stays primary and the source remains visible as secondary context. Selections and navigation stay coherent across views.
  - **Climax:** Maya can move between textual and graphical understanding without the frontend creating a second semantic model or forcing a text-heavy dashboard posture that wastes graph space.
  - **Resolution:** Athena proves that graphical workbench capability can stay downstream of the same runtime-owned truth.

- **UJ-3. Priya evaluates whether a graphical projection surface is safe to evolve toward later interaction.**
  - **Persona + context:** Priya is reviewing whether the first visual path preserves the architectural invariants needed for future editing and richer interaction.
  - **Entry state:** M7 graphical projection is available in the IDE.
  - **Path:** Priya inspects projection boundary behavior, view ownership, and what changes are or are not allowed directly in the visual surface.
  - **Climax:** Priya can verify that M7 graphical capability is real but still constrained by canonical semantic authority.
  - **Resolution:** Athena gains a credible path toward later interactive visual workflows without architectural compromise.

## 3. Glossary

- **Graphical Projection** - a view of canonical engineering meaning rendered through downstream layout and geometry interpretation.
- **Engineering Object Graph** - the canonical semantic graph of engineering entities, relationships, capabilities, interfaces, and constraints that exists independently of any one drawing or renderer.
- **Projection Boundary** - the explicit contract where runtime-owned semantic/projection state is exposed to a graphical surface.
- **Visual Workbench** - the product-shell area where graphical projections, navigation, and projection-oriented inspection are hosted.
- **Projection Model** - the inspectable view model consumed by graphical surfaces; it is downstream of canonical semantic and projection layers.
- **View Definition** - a renderer-facing definition of how engineering meaning is projected in one style or discipline, such as an IEC-style electrical view, not the semantic meaning itself.
- **Renderer Asset** - downstream symbol, template, or view-pack content used by a renderer; it must not become a substitute for engineering identity.
- **Editable Projection Rule** - the policy describing what, if anything, a graphical surface can change directly and how such changes route back into governed commands.

## 4. Features

### 4.1 Governed Projection Boundary

**Description:** Athena must define a stable graphical projection boundary above runtime-owned projection authority and below product-shell presentation. Realizes UJ-1, UJ-2, UJ-3.

#### FR-1: Define A Stable Graphical Projection Boundary

Athena can define a stable projection boundary for graphical workbench delivery. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- Athena introduces an explicit projection protocol or equivalent boundary for graphical workbench consumption.
- The boundary depends on canonical semantic state plus downstream layout/geometry/projection state rather than raw frontend-local reconstruction.
- The boundary is inspectable and stable enough to support later richer graphical milestones.

#### FR-2: Keep Graphical Projection Downstream Of Semantic Authority

Athena can keep graphical projection downstream of semantic authority. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- Graphical surfaces consume canonical semantic, layout, and geometry meaning instead of redefining it.
- M7 does not let canvas or frontend state become engineering truth.
- Engineering identity, package meaning, repository meaning, and semantic SCM meaning remain unchanged by the introduction of graphical projection.
- M7 treats renderer definitions, symbol packs, and view templates as downstream projection assets rather than semantic entities.
- One engineering object can support multiple graphical or non-graphical projections without duplicating semantic identity.

### 4.2 Real Visual Workbench Capability

**Description:** Athena must host real graphical workbench capability inside the current product shell instead of treating graphics as a separate demo. Realizes UJ-1, UJ-2.

#### FR-3: Surface Graphical Views In The Existing Athena Workbench

Athena can host graphical views in the existing product shell. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Theia workbench users can open one or more Athena graphical views through additive product boundaries.
- Graphical views coexist with source, repository, semantic inspection, and semantic SCM surfaces.
- Athena can keep the active `.athena` source and the active graphical projection visible together in a docked split workbench arrangement without shell replacement.
- M7 does not require replacing the current workbench architecture.

#### FR-4: Support Graphical Navigation And Projection-Oriented Inspection

Athena can support graphical navigation and projection-oriented inspection. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Users can inspect graphical elements, relationships, or grouped structures through runtime-owned projection data.
- Navigation between textual and graphical surfaces remains coherent.
- The same repository and semantic state produce the same graphical inspection outcome.
- M7 can surface graph-shaped engineering structure and relationship meaning rather than only symbol placement.
- The first graphical surfaces prove renderer-target delivery over canonical objects, not drawing-file ownership.
- The first renderer proof favors a graph-first, high-density professional work posture rather than a spacious dashboard-style panel dominated by descriptive text.

### 4.3 Explicit Read-Only And Interaction Rules

**Description:** M7 must clarify what graphical interaction means before broad editing is attempted. Realizes UJ-2, UJ-3.

#### FR-5: Publish Explicit Read-Only Versus Editable Rules

Athena can define explicit read-only versus editable projection rules. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- M7 clearly states which graphical surfaces are read-only and which interactions are allowed.
- If interaction is supported, it routes through governed runtime or command boundaries instead of mutating visual state privately.
- Later editing milestones inherit explicit rules instead of reverse-engineering them from ad hoc behavior.

#### FR-6: Preserve Deterministic Projection Refresh

Athena can preserve deterministic graphical refresh from the same underlying semantic state. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The same semantic state and chosen view definition yield the same graphical projection state.
- Graphical refresh remains downstream of runtime/compiler authority rather than local UI guesses.
- Projection refresh remains inspectable for debugging and architecture review.

### 4.4 Growth Path Toward Later Graphical Interaction

**Description:** M7 must prove the architectural path for later richer graphical behavior without widening into it prematurely. Realizes UJ-3.

#### FR-7: Prepare For Later Interactive Graphical Work Without Locking Final Editing Scope

Athena can prepare for later graphical interaction without turning M7 into a full editing milestone. Realizes UJ-3.

**Consequences (testable):**
- M7 establishes a view/interaction model that later milestones can extend safely.
- M7 may include narrow interaction proofs if they preserve semantic authority, but it does not become unrestricted graphical editing.
- Later milestones can build on M7 without rethinking the projection boundary from scratch.

#### FR-8: Re-Evaluate Graphical Technology Against Current Athena Constraints

Athena can choose or validate the graphical technology path at M7 time. Realizes UJ-3.

**Consequences (testable):**
- M7 evaluates the current best-fit graphical architecture against Athena constraints instead of inheriting an old decision uncritically.
- The chosen path supports the governed projection boundary and current product-shell constraints.
- M7 documents the decision well enough for downstream architecture and epic planning.

## 5. Non-Goals (Explicit)

- M7 does not revisit repository/package graph authority that belongs to M5.
- M7 does not revisit semantic SCM, semantic review, semantic commit, or semantic history authority that belongs to M6.
- M7 does not replace Athena LSP as the text/language-service boundary.
- M7 does not move engineering truth into canvas or frontend state.
- M7 does not attempt full freeform graphical editing or final ECAD-grade interaction parity.
- M7 does not become generic IDE polish, semantic tokens, or unrelated language-tooling backlog work.

## 6. MVP Scope

### 6.1 In Scope

- stable graphical projection boundary
- real graphical view delivery inside the current Athena workbench
- graph-first split workbench posture where the graphical canvas is primary and source remains visible as secondary context
- projection-oriented inspection and navigation
- explicit read-only versus editable surface rules
- deterministic projection refresh from canonical upstream state
- technology-path decision or validation for the first graphical delivery architecture

### 6.2 Out Of Scope For MVP

- replacing LSP or the current Theia shell
- repository/package or semantic SCM contract redesign
- broad freeform editing workflows
- final graphical UX, skin system, or broad design-system completion
- cloud collaboration or remote rendering infrastructure

## 7. Success Metrics

**Primary**

- **SM-1:** Athena can surface a real graphical projection in the product shell from the same runtime-owned semantic/projection authority model proven in earlier milestones.
- **SM-2:** Athena can keep graphical projection downstream of canonical semantic, layout, geometry, repository, and SCM meaning.
- **SM-3:** Athena can define an inspectable projection boundary and explicit read-only/editable rules suitable for later richer graphical work.
- **SM-4:** Athena can support graphical navigation and inspection without creating a second semantic model in the frontend.
- **SM-5:** Athena can prove that view definitions and renderer assets remain downstream projection concerns over the engineering object graph.
- **SM-6:** Athena can present the first renderer proof as a graph-first split workbench with professional information density instead of a demo-style panel layout.

**Secondary**

- **SM-7:** M7 produces a credible architecture decision for the first governed graphical technology path.
- **SM-8:** M7 prepares later interactive graphical editing without widening into full editing scope prematurely.

**Counter-metrics**

- **SM-C1:** Do not optimize for flashy graphics over semantic authority preservation.
- **SM-C2:** Do not optimize for unrestricted editing over explicit interaction rules.
- **SM-C3:** Do not optimize for adopting one graphics technology simply because it was researched earlier.

## 8. Cross-Cutting NFRs

- **NFR-1 Semantic Authority Preservation:** Graphical surfaces must remain downstream of canonical semantic, repository, package, and SCM meaning.
- **NFR-2 Determinism:** The same upstream semantic state and chosen view must yield the same projection state.
- **NFR-3 Inspectability:** Projection boundary output must remain inspectable for development and architecture debugging.
- **NFR-4 Workbench Continuity:** M7 must extend the current Athena shell rather than replacing it.
- **NFR-5 Growth Safety:** M7 must prepare later richer graphical interaction without collapsing into unrestricted editing.
- **NFR-6 Technology Discipline:** M7 must choose or validate its graphical architecture from current constraints, not from draft-era momentum alone.
- **NFR-7 Renderer Discipline:** View definitions, symbol packs, and renderer assets must remain downstream of the engineering object graph and must not become semantic authority by convention.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- The current Athena workbench remains the product host for graphical capability.
- Supporting IDE work is allowed only where it directly improves graphical-projection operability.
- Graphical surfaces remain Athena-owned additions, not parallel products.

### 9.2 Architectural Guardrails

- Compiler and runtime remain semantic authorities.
- `ide/lsp` remains the sole language-service and textual semantic entry point.
- Runtime-owned projection state remains the upstream source for graphical delivery.
- Any graphical protocol or server boundary must stay downstream of runtime-owned semantic/projection state.
- Graphical interaction must route through governed runtime or command paths if it changes meaningful state.

### 9.3 Roadmap Guardrails

- M7 owns graphical projection and visual workbench concerns.
- M7 does not reopen M5 or M6 milestone centers.
- Broader parser evolution remains a kernel-language watchpoint, not M7 core.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4 to M6
- **Primary delivery target:** local developer-run product shell plus deterministic JVM, IDE, and graphical verification
- **Primary runtime authority:** runtime-backed semantic/projection state
- **Primary text/language-service foundation:** Athena LSP
- **Primary graphical delivery concern:** governed visual projection over canonical upstream state

## 11. Open Questions

1. What is the narrowest useful projection protocol or server boundary for M7?
2. Should M7 remain purely read-only visually, or is there one narrow governed interaction slice worth proving now?
3. Which current Athena views should become the first graphical workbench proof surfaces?
4. Does the current GLSP direction still fit the real Athena constraints, or does M7 need a different protocol/runtime arrangement?
5. What selection, focus, or inspection synchronization is mandatory between text, semantic inspection, semantic SCM, and graphical views?
6. Which first renderer target best proves the model: relationship graph, IEC-style engineering projection, or another view that stays faithful to the same canonical objects?

## 12. Assumptions Index

- M7 should consume the completed M2 projection layers and M4-M6 product seams rather than redefining them.
- M7 should preserve the same semantic authority model already proven by M0 through M6.
- M7 should become the first real graphical milestone, but it should not become unrestricted editing yet.
- M7 should evaluate its graphical architecture against current constraints before freezing detailed implementation direction.
- M7 should treat symbols, IEC-style notations, and comparable renderer assets as view-layer concerns over the engineering object graph, not as semantic roots.
