---
title: Athena M12
status: draft
created: 2026-07-12
updated: 2026-07-12
---

# PRD: Athena M12

*Codename: Athena Electrical Renderer Correctness And Workbench Hardening.*

## 0. Document Purpose

This PRD defines the M12 product requirements for Athena after the completed M11 milestone.

M12 exists to close the next product gap intentionally left open by M11:

> Athena can now publish richer electrical projection families, sheets, notation packs, repeated references, and denser proof repositories, but the electrical graph can still read too much like a generic node-edge canvas instead of a serious electrical operator surface.

> M12 proves that electrical readability can remain downstream of semantic authority.

This PRD is renderer-correctness-first and operator-density-first, not architecture-reset-first. It builds on the completed M0 through M11 foundations, the current roadmap under `docs/roadmap/`, the electrical product references under `draft/screenshort/` and `draft/open/`, and the M12 brief under [`draft/m12/001-draft.md`](../../../../draft/m12/001-draft.md). Implementation-shaped detail that is useful but too low-level for the PRD body is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved authored DSL to canonical `Engineering IR`. M1 proved runtime-owned workspace and mutation orchestration. M2 proved explicit projection layers. M3 proved hosted extensibility. M4 proved the first serious IDE shell. M5 proved governed repository meaning. M6 proved semantic SCM. M7 proved graphical projection and the first renderer path. M8 proved one mutation authority across source and graph. M9 proved executable engineering knowledge above canonical structure. M10 proved AI-assisted reasoning above governed knowledge outputs. M11 proved the first serious electrical ECAD workbench depth through richer view families, sheet structure, notation packs, repeated references, and denser proof repositories.

M12 must now prove the next layer that makes the first electrical workbench credible under real operator scrutiny:

- electrical connections render as electrical relationships rather than generic graph edges
- endpoint anchoring, routing, labels, and markers remain legible under denser cases
- viewport, fit, focus, and canvas behavior remain reliable on larger scenes
- Athena-owned support panels become denser, more professional, and less demo-shaped
- electrical navigation and cross-reference movement feel like operator workflows rather than graph-demo interactions
- all of that still stays downstream of canonical semantic and projection authority

In other words, M11 proved that Athena can carry richer electrical downstream contracts. M12 must prove that the current renderer and workbench can consume those contracts with enough correctness and density to stop feeling like an early proof shell.

## 1.1 Why Now

The next risk is no longer whether Athena can represent electrical meaning, mutation, review, knowledge, or AI reasoning coherently.

Today the workspace already has the needed upstream proof:

- M7 already established projection delivery into the workbench
- M8 already established governed source and graph mutation
- M9 already established electrical knowledge diagnostics and impact
- M10 already established AI-assisted reasoning above governed outputs
- M11 already established richer electrical projection families, sheet structure, notation packs, repeated references, and denser electrical repositories

That is exactly why M12 can now become the renderer-correctness milestone:

- the semantic and runtime base already exists
- the electrical workbench vocabulary already exists
- the graph-first workbench already exists
- the missing proof is now operator trust in the rendered electrical surface

Starting this milestone earlier would have risked hardening visual behavior before the electrical projection vocabulary was stable enough to justify it.

## 2. Target User

### 2.1 Jobs To Be Done

- Electrical engineers need Athena connections, routing, and labels to read like electrical intent instead of generic graph links.
- Electrical engineers need to navigate from one electrical representation to related references, destinations, and diagnostics without losing semantic identity.
- Reviewers need dense electrical cases to remain navigable and inspectable without losing semantic coherence.
- Platform engineers need to improve rendering fidelity and operator density without moving authority into renderer-local heuristics or frontend-owned state.
- Product and architecture owners need proof that Athena can move from "serious foundation" to "credible operator surface" for the first ECAD target.

### 2.2 Non-Users (M12)

- Teams expecting M12 to become a new kernel-semantics milestone
- Teams expecting M12 to become a standards-compliance or rule-pack expansion milestone
- Teams expecting M12 to become a broad AI milestone
- Teams expecting M12 to become unrestricted drawing or CAD freedom
- Teams expecting M12 to become full EPLAN or QElectroTech parity
- Teams expecting M12 to reassign engineering authority to renderer or canvas state

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a denser electrical project and immediately checks whether the connections read correctly.**
  - **Persona + context:** Aaron is validating whether Athena has moved beyond a graph demo into something closer to electrical operator expectations.
  - **Entry state:** A repository already compiles and already publishes M11-rich electrical projection state.
  - **Path:** Aaron opens a larger electrical case, switches views, zooms, fits, and inspects the graph. Athena renders conductors, endpoints, labels, and related markers clearly enough that the scene reads as electrical structure.
  - **Climax:** Aaron can tell what is connected to what without mentally reverse-engineering generic graph edges.
  - **Resolution:** Athena proves that the first electrical renderer is operationally credible.

- **UJ-2. Maya navigates a dense electrical case without losing focus.**
  - **Persona + context:** Maya needs the workbench to stay usable under density, not just semantically correct.
  - **Entry state:** Richer views, sheets, repeated references, and diagnostics already exist.
  - **Path:** Maya fits the graph, pans, zooms, selects, switches support panels, and inspects related electrical information.
  - **Climax:** The canvas remains dominant, fit-to-view lands on the real scene, and support panels stay compact instead of visually fighting the graph.
  - **Resolution:** Athena proves that denser electrical operator behavior is possible without redesigning the architecture center.

- **UJ-3. Priya audits whether renderer hardening stayed downstream of semantics.**
  - **Persona + context:** Priya needs confidence that M12 improved operator trust without allowing renderer heuristics to become the truth source.
  - **Entry state:** Connection rendering, viewport behavior, and panel density have all been improved.
  - **Path:** Priya inspects the current contracts, the renderer responsibilities, the visual constraints, and the explicit non-goals.
  - **Climax:** Priya can explain that semantic identity and projection contracts still drive the renderer rather than being reconstructed inside it.
  - **Resolution:** Athena gains renderer depth without violating the semantic-layer strategy.

## 3. Glossary

- **Electrical Connection Rendering** - the downstream visual expression of electrical relationships such as conductors, terminals, wiring links, or cabinet links over canonical semantic identities.
- **Endpoint Anchoring** - the governed mapping from a rendered line or segment to the intended subject ports, terminals, or connection anchors from projection state.
- **Routing Correctness** - the quality of the rendered connection path in making electrical relationships readable without inventing new engineering meaning.
- **Viewport Fidelity** - the reliability of fit, pan, zoom, focus, and canvas extent behavior when operating on real electrical scenes.
- **Operator Density** - the level of information and control compactness expected from a serious IDE and ECAD workbench rather than a demo UI.
- **Renderer Hardening** - the milestone effort that strengthens visual correctness, reliability, and readability without moving authority into renderer-local models.
- **Electrical Reference Navigation** - the downstream operator flow that reveals related contacts, destinations, cross references, or diagnostics for one canonical electrical subject.
- **Preferred Routing Corridor** - projection-owned guidance for how a connection should generally traverse the downstream visual space without making the exact rendered route canonical truth.

## 4. Features

### 4.1 Electrical Connection Rendering Correctness

**Description:** Athena must make electrical relationships render and read like electrical relationships instead of generic graph edges. Realizes UJ-1, UJ-3.

#### FR-1: Render Electrical Connections As Distinct Electrical Relationships

Athena can render the first electrical-domain connections in a visually electrical way. Realizes UJ-1.

**Consequences (testable):**
- Connection output visually distinguishes electrical conductor intent from generic graph relationship lines.
- The same semantic relationship remains recognizably connected across supported electrical views such as wiring or cabinet-oriented projections.
- M12 does not require new semantic modeling just to express the first renderer-correct connection path.

#### FR-2: Preserve Stable Endpoint Anchoring And Port Readability

Athena can anchor rendered connections to the intended downstream electrical endpoints reliably. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Rendered lines terminate at governed subject anchors or ports rather than approximate generic node centers only.
- Port, terminal, or endpoint readability remains adequate under the denser M11 and later M12 proof cases.
- Endpoint anchoring stays traceable to projection-owned payloads rather than renderer-private semantic guesses.

#### FR-3: Improve Routing, Overlap Handling, And Marker Visibility

Athena can make the first renderer path materially more readable under dense electrical cases. Realizes UJ-1.

**Consequences (testable):**
- Routing, overlap handling, or comparable visibility controls reduce the "hairball graph" effect on denser cases.
- Wires, cabinet links, labels, and markers remain visible enough to support inspection.
- M12 may use deterministic presentation heuristics, but those heuristics do not create new engineering truth.

### 4.2 Canvas And Viewport Hardening

**Description:** Athena must make the graph-first workbench behave reliably under real operator navigation. Realizes UJ-1, UJ-2.

#### FR-4: Make Fit, Pan, Zoom, And Focus Reliable On Larger Scenes

Athena can keep the electrical canvas navigable on denser cases. Realizes UJ-2.

**Consequences (testable):**
- Fit-to-viewport centers and scales against the real rendered scene reliably.
- Pan and zoom remain usable under larger proof fixtures without losing orientation immediately.
- Focus behavior does not leave the operator hunting the active scene after basic viewport actions.

#### FR-5: Keep The Graph Surface Dominant In The Main Workbench

Athena can preserve a canvas-first electrical operator posture. Realizes UJ-2.

**Consequences (testable):**
- The graph area remains the dominant main-panel surface when the electrical workbench is in use.
- Utility controls, tips, and floating actions do not consume disproportionate visual attention.
- Closing or collapsing support panels expands the usable canvas area predictably.

### 4.3 Professional Electrical Workbench Density

**Description:** Athena must harden the surrounding Athena-owned panels so the whole operator surface feels professional instead of demo-like. Realizes UJ-2, UJ-3.

#### FR-6: Increase Athena Panel Density In A Professional IDE Style

Athena can present support information in a denser and more professional form. Realizes UJ-2.

**Consequences (testable):**
- Athena-owned hierarchy, diagnostics, outline, and inspection surfaces prefer compact IDE-style lists, trees, or tables over noisy demo-card styling.
- Repeated borders, oversized empty states, and decorative panel framing are reduced where they do not help operator work.
- Information density improves without weakening readability of the active engineering context.

#### FR-7: Use Compact And Clear Workbench Controls Around The Canvas

Athena can keep electrical workbench controls compact and legible. Realizes UJ-2.

**Consequences (testable):**
- Repeated status or mode controls around the graph use concise icon-led presentation where appropriate.
- Floating actions and compact controls expose clear hover meaning instead of unexplained visual clutter.
- Control polish stays subordinate to operator throughput and main-canvas focus.

### 4.4 Larger-Case Renderer Validation

**Description:** M12 must prove its value on denser real cases rather than on a small pretty demo. Realizes UJ-1, UJ-3.

#### FR-8: Publish Renderer-Correct Electrical Proof Fixtures

Athena can validate M12 through explicit proof repositories or fixtures designed for renderer correctness. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- M12 publishes at least one electrical case where connection rendering correctness is materially visible and testable.
- The proof case is dense enough to expose routing, anchoring, viewport, and panel-density weaknesses.
- M12 defines at least one larger readability benchmark tier beyond the current proof baseline, such as a materially larger device and wire count, for visual and interaction validation.
- Known visual limits are documented explicitly instead of hidden inside the proof.

#### FR-9: Preserve Semantic, Mutation, Knowledge, And Reasoning Coherence

Athena can improve renderer correctness without reopening earlier semantic foundations. Realizes UJ-3.

**Consequences (testable):**
- M12 remains coherent with M8 mutation authority.
- M12 remains coherent with M9 knowledge diagnostics and M10 reasoning outputs.
- M12 does not require renderer-local semantic reconstruction to achieve better operator results.

### 4.5 Electrical Navigation And Reference Surface

**Description:** Athena must let engineers move across related electrical representations and consequences the way a serious ECAD operator surface does. Realizes UJ-1, UJ-2, UJ-3.

#### FR-10: Support Electrical Cross-Reference Navigation And Related Reveal

Athena can navigate from one electrical subject to its related references, destinations, and consequences without losing semantic identity. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Athena can reveal at least one governed cross-reference navigation flow such as coil to contact, source to destination, or subject to repeated reference.
- Navigation can reveal related diagnostics, affected knowledge outputs, or related semantic objects from the same canonical subject path.
- Navigation remains anchored on canonical semantic identity and existing projection contracts rather than renderer-local object ids.

## 5. Non-Goals (Explicit)

- M12 does not become a new semantic-kernel milestone.
- M12 does not become a broader knowledge-pack, standards, or company-policy milestone.
- M12 does not become a broad AI product milestone.
- M12 does not become unrestricted freeform drafting.
- M12 does not become full EPLAN or QElectroTech parity.
- M12 does not move engineering truth into symbols, coordinates, or renderer state.

## 6. MVP Scope

### 6.1 In Scope

- electrical connection rendering that reads as electrical intent
- stronger endpoint anchoring and port readability
- deterministic routing or visibility improvements for dense cases
- reliable fit, pan, zoom, and focus on larger scenes
- graph-surface-dominant workbench behavior
- denser Athena-owned support panels
- larger renderer-validation proof fixtures
- first governed electrical cross-reference and related-reveal navigation flow

### 6.2 Out Of Scope For MVP

- new domain semantics beyond what current electrical projections already require
- broad new standards or rule-pack ecosystems
- large-scale browser, collaboration, or web renderer redesign
- full symbol-library parity with incumbent tools
- unrestricted user-authored freeform notation systems
- procurement, article-master, or ERP workflows

## 7. Success Metrics

**Primary**
- **SM-1:** Athena renders electrical connections in a way that is visibly more electrical and less generic-graph-like. Validates FR-1, FR-2.
- **SM-2:** Athena preserves readable endpoints, markers, and labels on denser electrical cases. Validates FR-2, FR-3.
- **SM-3:** Athena fit, pan, zoom, and focus behavior remain reliable on larger scenes. Validates FR-4, FR-5.
- **SM-4:** Athena-owned workbench panels become denser and more professional without reducing operator throughput. Validates FR-6, FR-7.
- **SM-5:** Athena publishes at least one larger renderer-validation proof case with explicit known limits. Validates FR-8.
- **SM-6:** Athena preserves M8 mutation, M9 knowledge, and M10 reasoning coherence while hardening the renderer surface. Validates FR-9.
- **SM-7:** Athena can navigate from one electrical subject to related references or consequences without losing semantic identity. Validates FR-10.

**Secondary**
- **SM-8:** M12 proves that the first electrical workbench can feel materially less toy-like without changing architecture center.
- **SM-9:** Athena maintains readable labels, usable zoom/fit behavior, and acceptable interaction quality on at least one materially larger benchmark tier beyond the baseline dense proof case.
- **SM-10:** M12 creates a credible base for later deeper electrical workflow and compatibility milestones.

**Counter-metrics**
- **SM-C1:** Do not optimize for visual spectacle if electrical readability and operator density remain poor.
- **SM-C2:** Do not optimize for feature-count parity if renderer correctness is still weak on the main proof cases.
- **SM-C3:** Do not optimize for renderer-local cleverness if it weakens inspectability or semantic authority boundaries.

## 8. Cross-Cutting NFRs

- **NFR-1 Semantic Authority Preservation:** Canonical engineering meaning remains upstream of renderer and workbench behavior.
- **NFR-2 Projection Traceability:** Endpoint and routing behavior must stay explainable against governed projection payloads.
- **NFR-3 Routing Ownership Separation:** Semantic layer owns endpoint identity, projection owns preferred routing corridor guidance, and renderer owns only the actual visual path.
- **NFR-4 Dense-Case Readability:** Denser proof scenes must remain materially more legible than the current generic graph baseline.
- **NFR-5 Operator Throughput:** Main-canvas focus and panel density must improve operator work rather than add presentation clutter.
- **NFR-6 Narrowness:** M12 must stay focused on renderer correctness and workbench hardening rather than broad new platform scope.
- **NFR-7 Delivery Reuse:** M12 must preserve the existing runtime, LSP, review, and workbench seams instead of creating a second frontend-owned authority.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- The current Athena workbench remains the product host for M12.
- Supporting UI work is allowed where it directly improves electrical renderer correctness and operator density.
- M12 should improve the first electrical ECAD product surface, not broaden into every industrial authoring surface at once.
- QElectroTech and EPLAN remain important product references, but they do not define Athena's architecture center.

### 9.2 Architectural Guardrails

- `Engineering IR` remains the canonical semantic authority.
- M11 electrical projection families, sheet structure, notation packs, and repeated references remain downstream contracts.
- Runtime remains the owner of projection-session, mutation, and review coherence.
- `ide/lsp` remains the sole IDE semantic and projection entry point.
- The renderer and workbench must not infer new engineering meaning from symbols, coordinates, or overlap heuristics alone.

### 9.3 Roadmap Guardrails

- M12 owns electrical renderer correctness and operator-density hardening.
- M12 does not reopen M8 mutation authority, M9 knowledge-runtime authority, M10 reasoning authority, or M11 projection-family authority.
- Broader electrical workflow breadth, deeper compatibility, and larger ecosystem coverage remain later than M12.
- Full EPLAN parity remains explicitly later than M12, if ever pursued at all.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4 through M11
- **Primary delivery target:** local developer-run professional electrical renderer and workbench surface for the first ECAD domain
- **Primary semantic foundation:** canonical `Engineering IR` plus M9 knowledge outputs and M10 reasoning surfaces where present
- **Primary graphical foundation:** M7 projection model, M11 electrical projection-family contracts, and the current graph adapter path
- **Primary product-delivery surfaces:** electrical graphical views, source reveal, semantic inspection, diagnostics, semantic review, hierarchy, outline, and related workbench panels

## 11. Open Questions

1. What is the narrowest routing and endpoint model that makes the rendered scene read as electrical rather than generic graph without forcing a renderer architecture reset?
2. Which electrical views should receive the highest rendering-fidelity priority first: wiring, cabinet, schematic, or a narrower governed subset?
3. How much deterministic routing or overlap handling is enough for M12 before later deeper editor or notation milestones?
4. Which Athena-owned panels most need density hardening first: hierarchy, diagnostics, outline, inspector, or graph-side controls?
5. What is the right M12 proof size for honestly validating renderer correctness under density without expanding into a giant corpus milestone?
6. Which current visual defects should be treated as milestone-closing blockers versus known remaining limits?

## 12. Assumptions Index

- Athena already has enough semantic, mutation, knowledge, and electrical projection foundation to justify renderer hardening now.
- The next honest product gap is electrical readability and operator density, not another broad semantic or AI thesis.
- QElectroTech and EPLAN should be used as product references and compatibility guides, not as architecture centers.
- The renderer can improve materially through governed downstream hardening before any later deeper framework or parity decision is forced.
- Later electrical workflow breadth should build on a renderer surface that operators can already trust visually.
