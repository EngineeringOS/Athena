---
title: Athena M11
status: draft
created: 2026-07-11
updated: 2026-07-11
---

# PRD: Athena M11

*Codename: Athena Electrical Workbench Depth.*

## 0. Document Purpose

This PRD defines the M11 product requirements for Athena as the first serious ECAD-depth milestone for the electrical domain.

M11 exists to close the next product gap intentionally left open by M7 through M10:

> Athena already proves semantic electrical modeling, graphical projection, governed mutation, executable engineering knowledge, and planned AI-assisted reasoning, but it still does not provide the dense, sheet-aware, notation-rich operator workbench depth expected from a serious electrical tool.

This PRD is product-depth-first, not architecture-reset-first. It builds on the completed M0 through M9 foundations, the planned M10 boundary, the workspace roadmap, and the M11 brief under [`draft/m11/001-draft.md`](../../../../draft/m11/001-draft.md). Implementation-shaped technical detail that is useful but too low-level for the PRD body is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved authored DSL to canonical `Engineering IR`. M1 proved runtime-owned workspace and mutation orchestration. M2 proved explicit projection layers. M3 proved hosted extensibility. M4 proved the first serious IDE shell. M5 proved governed repository meaning. M6 proved semantic SCM. M7 proved graphical projection and the first renderer path. M8 proved one mutation authority across source and graph. M9 proved executable engineering knowledge above canonical structure. M10 is positioned to prove AI-assisted reasoning above governed knowledge outputs.

M11 must now prove the next layer that makes Athena a credible first ECAD domain product instead of a graph-oriented semantic platform:

- richer electrical projection families above canonical engineering entities
- explicit sheet or page structure for electrical documentation and navigation
- governed symbol, notation, and repeated-reference depth
- denser operator workbench behavior over more realistic electrical cases
- preserved coherence with existing mutation, review, and knowledge authority

In other words, earlier milestones proved that Athena can own engineering meaning and project it into views. M11 must prove that the first electrical workbench can feel like a serious tool without moving semantic truth into symbols, pages, or renderer state.

## 1.1 Why Now

The next technical risk is no longer whether Athena can represent or reason about electrical meaning.

Today the workspace already has the needed upstream proof:

- M3 already established the first real electrical domain extension
- M7 already established projection sessions, graph delivery, and the first renderer path
- M8 already established governed source and graph mutation
- M9 already established electrical knowledge diagnostics and impact above canonical semantic state
- M10 is planned to add AI assistance on top of those governed outputs rather than replacing them

That is exactly why M11 can now become the first serious ECAD-depth milestone:

- the semantic and runtime base already exists
- the electrical domain already exists
- the graphical delivery path already exists
- the missing proof is now workbench depth, notation depth, and operator density

Starting this milestone earlier would have risked building electrical workbench behavior on top of unstable semantic, mutation, or review foundations.

## 2. Target User

### 2.1 Jobs To Be Done

- Electrical engineers need Athena to display denser electrical projects through sheet-aware, notation-aware, and reference-aware views rather than narrow demo graphs.
- Reviewers need source, graph, diagnostics, knowledge, and cross-reference context to remain coherent when an electrical project becomes visually denser.
- Platform engineers need to deepen the first ECAD domain without moving authority into symbol placement, page structure, or renderer-local heuristics.
- Product and architecture owners need proof that Athena can become a serious electrical workbench while still staying true to the semantic-layer strategy.

### 2.2 Non-Users (M11)

- Teams expecting M11 to become full EPLAN feature parity
- Teams expecting M11 to become a standards-compliance platform, procurement suite, or ERP-integrated article-master system
- Teams expecting M11 to become unrestricted freeform CAD drawing
- Teams expecting M11 to move engineering meaning into page XML, symbol metadata, or frontend state
- Teams expecting M11 to reopen the kernel authority, mutation authority, or knowledge-runtime boundaries already proven

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a larger electrical project.**
  - **Persona + context:** Aaron is no longer validating a toy graph; he wants to know whether Athena can hold a more realistic electrical project shape.
  - **Entry state:** The repository already compiles, and the electrical extension can already derive projections and knowledge outputs.
  - **Path:** Aaron opens a denser project with repeated devices, more connections, and more labels. Athena renders sheet-aware electrical views, preserves selection and reveal coherence, and keeps navigation usable under higher visual density.
  - **Climax:** Aaron can inspect and navigate a denser electrical project without losing semantic context or tool responsiveness.
  - **Resolution:** Athena proves that the first ECAD domain is not limited to narrow graph demos.

- **UJ-2. Maya reviews the same engineering subject across multiple electrical representations.**
  - **Persona + context:** Maya needs one engineering subject to remain coherent whether she sees it in schematic, wiring, cabinet, or cross-reference-oriented views.
  - **Entry state:** Canonical engineering meaning, mutation authority, and knowledge diagnostics are already available.
  - **Path:** Maya selects or reviews an electrical subject and moves between views or sheets. Athena preserves semantic identity, reveal coherence, diagnostic context, and review context across those representations.
  - **Climax:** Maya sees different downstream representations of the same engineering entity without semantic drift.
  - **Resolution:** Athena proves that an electrical workbench can deepen without creating parallel truths.

- **UJ-3. Priya audits whether ECAD depth stayed downstream of semantics.**
  - **Persona + context:** Priya needs confidence that M11 deepened the electrical workbench without quietly turning the renderer into the architecture center.
  - **Entry state:** Sheet, notation, symbol, and dense view behavior are implemented.
  - **Path:** Priya inspects the boundary between engineering entities, projection rules, sheet structure, symbol mapping, and workbench behavior.
  - **Climax:** Priya can explain what belongs to canonical semantic state, what belongs to projection contracts, and what belongs to downstream electrical presentation.
  - **Resolution:** Athena proves the first serious ECAD depth while preserving the manifesto’s semantic-first rule.

## 3. Glossary

- **Electrical Projection Family** - the set of downstream representations used to express one canonical electrical subject across schematic, cabinet, wiring, documentation, or related views.
- **Sheet Model** - the governed model for page or sheet identity, ordering, layout grouping, and navigation in electrical documentation views.
- **Notation Pack** - the governed package of symbol, label, marker, or repeated-reference presentation rules used by one electrical projection family.
- **Cross Reference** - the downstream reference that links repeated or related electrical representations of the same canonical engineering subject.
- **Repeated Reference** - a second or later representation of a canonical electrical entity or its parts in another sheet, view, or documentation context.
- **Workbench Density** - the level of information, navigation, and interaction richness required for a serious electrical operator workflow.
- **Electrical Proof Repository** - the milestone-published example repository used to verify realistic electrical workbench depth rather than toy projections only.

## 4. Features

### 4.1 Electrical Projection Family Depth

**Description:** Athena must deepen electrical view definitions so one engineering entity can be represented through multiple serious electrical representations without semantic drift. Realizes UJ-1, UJ-2, UJ-3.

#### FR-1: Support Richer Electrical Projection Families

Athena can represent the first ECAD domain through richer electrical projection families. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Athena supports clearer distinction among at least schematic, cabinet, wiring, and documentation-oriented electrical views.
- One canonical engineering entity can participate in more than one electrical projection family without redefining engineering meaning.
- The first M11 proof remains electrical-domain-focused rather than widening into a general multi-industry drafting platform.

#### FR-2: Preserve Canonical Identity Across Electrical Views

Athena can preserve one semantic identity across richer electrical workbench views. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- The same engineering subject remains revealable and inspectable across source, graph, sheet, and review contexts.
- Repeated references or alternate electrical representations do not create parallel semantic identities.
- M11 does not allow view-local symbols or page positions to become the source of engineering truth.

### 4.2 Sheet, Symbol, And Notation Depth

**Description:** Athena must add the first serious electrical-document workbench depth above canonical meaning. Realizes UJ-1, UJ-2, UJ-3.

#### FR-3: Introduce A First Explicit Electrical Sheet Model

Athena can support the first governed sheet or page model for electrical workbench operation. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Athena can model at least one first sheet or page structure with stable identity, ordering, and navigation semantics.
- Sheet structure remains downstream of canonical engineering meaning and projection rules.
- M11 does not require sheet identity to redefine engineering identity.

#### FR-4: Introduce A Governed Electrical Symbol And Notation Boundary

Athena can support the first governed electrical symbol and notation pack boundary. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Athena can map canonical electrical subjects into governed symbol or notation selections without making the symbol the source of truth.
- Symbol, label, and notation behavior remain inspectable and extension-compatible.
- The first notation depth remains explicitly governed instead of becoming ad hoc renderer-local data.

#### FR-5: Support Repeated References And Cross-Reference Display

Athena can display repeated references and cross-reference relationships for the first ECAD domain. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Athena can show at least one first repeated-reference or cross-reference pattern for electrical entities.
- Cross-reference behavior remains linked to canonical subject identity and not to duplicated symbol objects only.
- The same repeated or cross-referenced entity remains coherent in diagnostics, inspection, and review context.

### 4.3 Operator Workbench Depth And Scale

**Description:** M11 must prove that Athena can handle denser electrical operator workflows rather than just narrow proof graphs. Realizes UJ-1, UJ-2.

#### FR-6: Support Denser Electrical Workbench Interaction

Athena can support a denser electrical workbench under realistic information load. Realizes UJ-1.

**Consequences (testable):**
- The first M11 proof includes electrical cases larger than the current narrow graph proofs, such as more than 10 components and more than 20 connections.
- Core operations such as fit-to-view, navigation, selection, reveal, and inspection remain usable under denser cases.
- The workbench can present higher property, label, and reference density without collapsing into unreadable demo-only layouts.

#### FR-7: Publish Large-Case Electrical Proof Fixtures

Athena can validate the first serious electrical workbench through published larger proof fixtures. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- M11 publishes at least one realistic electrical proof repository or equivalent denser fixture.
- The milestone includes repeatable verification steps for rendering, navigation, selection, and coherence on that larger case.
- Known limits are documented explicitly instead of hidden inside the proof.

### 4.4 Coherence With Existing Semantic, Review, And Knowledge Paths

**Description:** Electrical workbench depth must remain coherent with the existing Athena platform seams. Realizes UJ-2, UJ-3.

#### FR-8: Preserve Mutation, Review, And Knowledge Coherence Across Electrical Depth

Athena can keep earlier milestone guarantees intact while deepening the electrical workbench. Realizes UJ-2.

**Consequences (testable):**
- The richer electrical workbench stays coherent with the M8 mutation path.
- The richer electrical workbench stays coherent with M9 knowledge diagnostics and impact.
- The richer electrical workbench remains compatible with the M10 AI-assisted reasoning path where AI assistance is present.

#### FR-9: Keep Electrical Workbench Depth Downstream Of Canonical Semantic Authority

Athena can deepen electrical product behavior without shifting the architecture center into the renderer. Realizes UJ-3.

**Consequences (testable):**
- Page, symbol, notation, and view behavior remain downstream contracts rather than canonical engineering truth.
- Renderer and workbench layers remain consumers of governed semantic and projection outputs.
- M11 does not require a renderer-local semantics model to achieve the first serious ECAD depth.

## 5. Non-Goals (Explicit)

- M11 does not become full EPLAN feature parity.
- M11 does not become a procurement, article-master, or ERP integration milestone.
- M11 does not become a broad standards and compliance platform.
- M11 does not become unrestricted freeform CAD drawing.
- M11 does not become a full 3D or mechanical workbench.
- M11 does not move engineering truth into symbols, pages, or renderer state.

## 6. MVP Scope

### 6.1 In Scope

- richer electrical projection families
- one first explicit sheet or page model
- one governed electrical symbol and notation boundary
- first repeated-reference and cross-reference display behavior
- denser electrical proof cases
- stronger workbench navigation and reveal coherence under scale
- published proof corpus and explicit limit recording

### 6.2 Out Of Scope For MVP

- full article-master and purchasing workflows
- full report automation suite
- full EPLAN macro parity
- broad multi-domain drafting depth outside the first electrical ECAD target
- unrestricted user-authored freeform symbol systems
- full browser-first or collaborative workbench redesign

## 7. Success Metrics

**Primary**
- **SM-1:** Athena can support richer electrical projection families without losing semantic identity coherence. Validates FR-1, FR-2.
- **SM-2:** Athena can support a first explicit electrical sheet model and governed notation boundary. Validates FR-3, FR-4.
- **SM-3:** Athena can display repeated references and cross-reference relationships coherently. Validates FR-5.
- **SM-4:** Athena can operate over denser electrical proof cases such as more than 10 components and more than 20 connections. Validates FR-6.
- **SM-5:** Athena can publish a realistic larger electrical proof repository with repeatable validation steps. Validates FR-7.
- **SM-6:** Athena can preserve M8 mutation, M9 knowledge, and M10 reasoning coherence while deepening electrical workbench behavior. Validates FR-8, FR-9.

**Secondary**
- **SM-7:** M11 proves that the first ECAD domain can become a serious operator workbench without abandoning the semantic-layer strategy.
- **SM-8:** M11 prepares later deeper electrical product breadth without pretending to solve the whole EPLAN-class surface in one step.

**Counter-metrics**
- **SM-C1:** Do not optimize for renderer spectacle if it weakens semantic and projection coherence.
- **SM-C2:** Do not optimize for feature-count parity with existing electrical tools if it weakens architectural discipline.
- **SM-C3:** Do not optimize for sheet or symbol richness if those structures start redefining engineering truth.

## 8. Cross-Cutting NFRs

- **NFR-1 Semantic Authority Preservation:** Canonical engineering meaning remains upstream of sheet, symbol, notation, and renderer behavior.
- **NFR-2 Identity Coherence:** The same engineering subject must remain stable across repeated references, sheets, views, diagnostics, and review contexts.
- **NFR-3 Scale Usability:** The first serious electrical proof must remain usable at denser graph and panel loads than earlier proof cases.
- **NFR-4 Inspectability:** The boundary between engineering entity, projection rule, sheet structure, and notation pack remains inspectable for architecture and debugging.
- **NFR-5 Narrowness:** The first ECAD-depth milestone must stay focused on serious electrical workbench depth rather than turning into a full downstream product clone.
- **NFR-6 Delivery Reuse:** M11 must preserve the existing runtime, LSP, review, and workbench seams instead of creating a second frontend-owned authority.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- The current Athena workbench remains the product host for M11.
- Supporting UI work is allowed where it directly improves serious electrical workbench operation and density.
- M11 should deepen ECAD electrical behavior, not broaden into every downstream industrial surface at once.
- QElectroTech and EPLAN remain important product references, but they do not define Athena’s architecture center.

### 9.2 Architectural Guardrails

- `Engineering IR` remains the canonical semantic authority.
- Projection families, sheet structure, symbol packs, and notation packs remain downstream of canonical engineering meaning.
- Runtime remains the owner of projection-session and mutation coherence.
- `ide/lsp` remains the sole IDE semantic and projection entry point.
- The renderer and workbench must not infer new engineering meaning from symbols or coordinates alone.

### 9.3 Roadmap Guardrails

- M11 owns the first serious electrical workbench depth proof.
- M11 does not reopen M8 mutation authority, M9 knowledge-runtime authority, or M10 AI reasoning authority.
- Broader standards, company policy, and ecosystem packaging remain later than M11.
- Full EPLAN parity remains explicitly later than M11, if ever pursued at all.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4 through M10
- **Primary delivery target:** local developer-run professional electrical workbench for the first ECAD domain
- **Primary semantic foundation:** canonical `Engineering IR` plus M9 knowledge outputs
- **Primary graphical foundation:** M7 projection model, runtime-owned projection sessions, and current graph adapter path
- **Primary product-delivery surfaces:** electrical graphical views, source reveal, semantic inspection, diagnostics, semantic review, and related workbench panels

## 11. Open Questions

1. What is the narrowest first explicit sheet model that still feels like real ECAD depth rather than a toy page list?
2. Which electrical projection families should be first-class in M11 beyond the current cabinet and wiring emphasis?
3. What is the right boundary between governed notation packs and later broader symbol-library ecosystems?
4. Which repeated-reference and cross-reference patterns are the most important first proof for the electrical domain?
5. How much denser should the first realistic electrical proof repository be to validate workbench depth honestly?
6. Should documentation or report-oriented views enter M11 directly, or stay behind the first sheet and notation proof?

## 12. Assumptions Index

- Athena already has the semantic and projection foundation needed for the first serious ECAD domain, but not yet the product-depth proof.
- The first honest ECAD milestone is workbench density, sheet depth, notation depth, and repeated-reference behavior, not full feature parity with incumbent electrical tools.
- QElectroTech and EPLAN should be used as product references and compatibility guides, not as architecture centers.
- The first serious electrical workbench proof must still remain small enough to validate honestly.
- Later electrical breadth can grow only if M11 first proves that downstream ECAD depth can stay coherent with canonical semantic authority.
