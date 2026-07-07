---
title: Athena M2
status: draft
created: 2026-07-06
updated: 2026-07-06
---

# PRD: Athena M2

*Codename: Semantic Layout and Multi-View Projection.*

## 0. Document Purpose

This PRD defines the M2 product requirements for Athena after the M0 semantic compiler proof and the M1 runtime-centered workspace proof have both been completed. It is written for founders, product owners, architecture owners, developers, and downstream BMad workflows. The document is capability-first: Glossary terms are authoritative, Features are grouped with globally numbered Functional Requirements, system-wide Non-Functional Requirements are explicit, and inferred points are tagged inline as `[ASSUMPTION]`. This M2 PRD builds on the current workspace state summarized in `docs/usages/athena-workspace-summary.md`, on the final M1 PRD in `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/`, and on the older milestone draft in `draft/M2/001-draft.md`, while superseding that draft's milestone numbering where the repository has already moved beyond it.

## 1. Vision

M0 proved that Athena can compile authored engineering intent into canonical `Engineering IR` and deterministic downstream artifacts. M1 proved that the same semantic core can be hosted by `Athena Runtime`, inspected as graph, changed through commands, tracked through history and diff, projected into a desktop viewer, and extended through non-sovereign plugins.

M2 must prove the next manifesto boundary: engineering meaning, layout intent, and geometry must become explicit, separate, and synchronized layers instead of collapsing back into one drawing-centric model. Athena therefore needs a first `Layout IR`, a first `Geometry IR`, and a first multi-view projection workflow that can derive more than one human-facing view from one canonical semantic model without changing engineering identity.

If M2 succeeds, Athena stops being only a semantic runtime with one viewer proof and becomes the first real EngineeringOS view system. The platform will be able to say that the same semantic project can support multiple synchronized projections, runtime-driven view refresh, and renderer-ready geometry while preserving the manifesto rule that layout and geometry are downstream consequences, not semantic truth.

## 1.1 Why Now

The next architectural risk is not whether Athena can host semantics. That has already been proven. The risk is whether UI-facing projection work will remain faithful to the manifesto once richer views arrive. If layout and geometry are allowed to emerge implicitly inside Compose state, SVG rendering, or ad hoc view models, Athena will drift back toward the same drawing-centric structure the manifesto rejects.

M2 exists to install the missing downstream layers before Studio grows further. The platform needs explicit view intent and explicit geometry boundaries now, while the current surface area is still small enough to govern cleanly. That sequencing is load-bearing.

## 2. Target User

### 2.1 Jobs To Be Done

- Platform engineers need Athena to represent layout and geometry as explicit downstream layers rather than burying them in viewer-local state.
- Application builders need more than one synchronized view over the same semantic project without forking semantic authority.
- Operators and reviewers need to inspect the same semantic objects across different views and trust that identity, diagnostics, and history still refer to the same engineering truth.
- Founders need Athena to demonstrate the manifesto's three-layer consequence chain: semantic meaning, layout intent, and geometry output.
- Future Studio work needs a governed projection substrate before richer editing, panel systems, or multi-surface delivery are attempted.

### 2.2 Non-Users (M2)

- Teams expecting M2 to deliver a full ECAD editor
- Teams expecting freeform geometry editing to become the source of engineering truth
- Teams primarily seeking cloud collaboration, browser-first deployment, or external target adapters in this phase

### 2.3 Key User Journeys

- **UJ-1. Aaron derives two synchronized views from one semantic project.**
  - **Persona + context:** Aaron is validating that Athena can grow beyond a single-view proof without collapsing semantics into presentation.
  - **Entry state:** A `Project` is open through `Athena Runtime` and compiles into canonical `Engineering IR`.
  - **Path:** Aaron requests two view definitions over the same `Project`; Athena derives `Layout IR` for each view; Athena derives `Geometry IR` from each layout; the viewer renders both views without changing canonical semantic identity.
  - **Climax:** Aaron inspects one semantic object in both views and confirms that the same canonical identity, diagnostics, and references survive across both projections.
  - **Resolution:** Aaron can explain Athena as `Engineering IR -> Layout IR -> Geometry IR -> viewer/backend`, not as `viewer state = design truth`.

- **UJ-2. Maya switches views to inspect the same system in different human contexts.**
  - **Persona + context:** Maya is reviewing a runtime-managed engineering workspace and needs different presentation structures for different tasks.
  - **Entry state:** A desktop viewer session is open with at least two supported views over the active `Project`.
  - **Path:** Maya selects one view, inspects semantic objects, switches to another view, and follows the same object through the alternate presentation.
  - **Climax:** The object remains recognizably the same semantic object across both views while layout and geometry change around it.
  - **Resolution:** Maya trusts that the viewer is switching projections, not switching truths.

- **UJ-3. Priya changes semantic state and watches only affected view scope refresh.**
  - **Persona + context:** Priya is extending Athena and wants proof that richer projections can still stay incremental and inspectable.
  - **Entry state:** An active `Project` has supported semantic commands and at least one loaded view.
  - **Path:** Priya executes a semantic change through the existing command path; Athena identifies affected semantic scope; only affected `Layout IR`, `Geometry IR`, and rendered output recompute where possible.
  - **Climax:** The refreshed view remains synchronized with canonical semantics without forcing all views and geometry to be rebuilt blindly. `[ASSUMPTION: M2 proves dependency-scoped projection refresh over at least one supported change path rather than general arbitrary-layout editing.]`
  - **Resolution:** Priya can treat view projection as a governed runtime capability instead of disposable UI cache.

## 3. Glossary

- **Engineering IR** - The canonical semantic engineering model. It remains the only source of semantic truth in M2.
- **Layout IR** - The first explicit representation of view intent derived from `Engineering IR`. It captures how humans want to see semantic truth in a particular view without redefining engineering meaning.
- **Geometry IR** - The explicit renderable structure derived from `Layout IR`. It captures coordinates, paths, placements, and other precise geometry needed by a backend or viewer.
- **View Definition** - A runtime-owned declaration of one supported projection context such as cabinet, wiring, or functional view.
- **View Projection** - The runtime-owned consequence chain from `Engineering IR` through `Layout IR` and `Geometry IR` into a rendered or inspectable view.
- **Canonical Identity** - The stable semantic identity already owned by `Engineering IR` and reused across graph, commands, history, diagnostics, layout, and geometry layers.
- **Projection Refresh** - Runtime-owned recomputation of affected `Layout IR`, `Geometry IR`, and downstream output after a semantic change.
- **Compose Workbench** - The shared desktop-oriented interaction and viewing infrastructure that remains downstream of runtime, semantics, layout, and geometry contracts.
- **Renderer Backend** - A downstream consumer of `Geometry IR` or related derived output such as SVG or Compose display surfaces.

## 4. Features

### 4.1 Explicit Layout and Geometry Layers
**Description:** M2 must add the first explicit downstream layers after `Engineering IR`: `Layout IR` and `Geometry IR`. These layers are compiler- and runtime-visible contracts, not ad hoc view-model shapes hidden in render code. Realizes UJ-1, UJ-3.

**Functional Requirements:**

#### FR-1: Derive Layout IR From Canonical Semantics

Athena can derive `Layout IR` from canonical `Engineering IR` for a supported `View Definition`. Realizes UJ-1.

**Consequences (testable):**
- `Layout IR` is produced as a distinct stage after canonical semantics and before geometry.
- `Layout IR` uses `Canonical Identity` from `Engineering IR` rather than inventing view-local semantic identity.
- `Layout IR` can represent grouping, ordering, relative arrangement, and view-specific emphasis without redefining engineering meaning.

#### FR-2: Derive Geometry IR From Layout IR

Athena can derive `Geometry IR` from `Layout IR` for at least the initial supported views. Realizes UJ-1.

**Consequences (testable):**
- `Geometry IR` is produced as a distinct stage after layout derivation.
- `Geometry IR` contains the precise renderable structure needed by a viewer or backend.
- The geometry stage does not need to reinterpret authored DSL or mutate canonical semantics to complete.

#### FR-3: Preserve Semantic Identity Through Projection Layers

Athena preserves `Canonical Identity` across `Engineering IR`, `Layout IR`, `Geometry IR`, and rendered projection results. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The same semantic object can be referenced across semantic, layout, and geometry layers through stable identity.
- Diagnostics, graph queries, command history, and diff inspection can still point to the same semantic object after projection.
- Layout or geometry changes alone do not create a new semantic object.

### 4.2 Multi-View Projection
**Description:** Athena must support more than one human-facing view over the same semantic model. The first proof is not "many view types forever"; it is "one semantic truth, multiple synchronized projections." Realizes UJ-1, UJ-2.

**Functional Requirements:**

#### FR-4: Support At Least Two View Definitions

Athena can derive and expose at least two supported `View Definition` types over one active `Project`. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The active `Project` can produce at least two distinct layout projections from the same semantic source.
- The views differ in arrangement or emphasis while continuing to reference the same canonical semantic objects.
- View selection remains runtime-owned rather than being hard-coded private viewer state.

#### FR-5: Allow Operators To Switch Between Views

An operator can switch between supported views in the desktop surface without changing canonical semantics. Realizes UJ-2.

**Consequences (testable):**
- The viewer can load a different `View Projection` for the same active `Project`.
- Switching views does not trigger semantic mutation.
- The viewer can show that one selected semantic object corresponds to the same `Canonical Identity` across views.

#### FR-6: Keep Views Synchronized To One Semantic Source

Athena keeps all supported views derived from the same `Engineering IR` rather than allowing view-local semantic divergence. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- A supported semantic change becomes visible through refreshed projections over the same active `Project`.
- Different views may present different layout logic without holding separate engineering truth.
- Runtime-inspectable projection metadata can explain which semantic state a view was derived from.

### 4.3 Runtime-Coordinated Projection Refresh
**Description:** M2 must prove that richer projection layers can still participate in the existing runtime-centered change path. Semantic commands stay canonical; layout and geometry refresh stay downstream and explainable. Realizes UJ-3.

**Functional Requirements:**

#### FR-7: Recompute Affected Layout and Geometry Scope

After a supported semantic mutation, Athena can recompute only affected `Layout IR` and `Geometry IR` scope where dependency information allows it. Realizes UJ-3.

**Consequences (testable):**
- Projection refresh can identify affected projection scope from changed semantic identities and relationships.
- Unaffected views or unaffected view regions are not required to rebuild blindly in every case.
- Refresh reasoning remains runtime-inspectable instead of hidden in viewer-local caches.

#### FR-8: Refresh Rendered Output From Updated Geometry

Athena can refresh rendered output from updated `Geometry IR` after a supported semantic mutation. Realizes UJ-3.

**Consequences (testable):**
- A viewer or SVG-oriented backend can consume refreshed geometry without rederiving semantics privately.
- Refresh remains consistent with the post-command semantic state.
- The runtime can explain which semantic change caused the refreshed projection.

#### FR-9: Preserve History and Diff Meaning Across Projection Refresh

Athena keeps command history and semantic diff inspection anchored in canonical semantics even when layout and geometry refresh. Realizes UJ-3.

**Consequences (testable):**
- A projection refresh does not replace semantic diff with geometry-only diff as the primary explanation of change.
- Command-linked inspection can still explain what changed semantically and what projection consequences followed.
- View refresh remains a downstream consequence of canonical change, not a second history system.

### 4.4 Studio-Facing Inspection Surface
**Description:** M2 should extend the existing desktop proof just enough to make multi-view projection inspectable. It does not attempt a full editor shell. Realizes UJ-2.

**Functional Requirements:**

#### FR-10: Expose View Projection State To The Desktop Surface

The desktop surface can consume runtime-owned `View Projection` state over the active `Project`. Realizes UJ-2.

**Consequences (testable):**
- The desktop application can request supported view projections through runtime-owned contracts.
- Compose workbench code does not privately own layout or geometry truth.
- The desktop surface can show which view is active and which semantic object is selected.

#### FR-11: Allow View-Oriented Inspection Without Geometry Becoming Sovereign

An operator can inspect layout and geometry consequences in the desktop surface without those layers becoming semantic authority. Realizes UJ-2.

**Consequences (testable):**
- View-facing UI state can inspect projection layers separately from semantic truth.
- The operator can distinguish semantic identity from view placement.
- Geometry presentation does not become the source of command semantics, validation semantics, or graph truth.

### 4.5 Geometry-Aware Backends
**Description:** M2 must keep backend consequences explicit. The current proof can remain narrow, but at least one downstream backend must consume `Geometry IR` as geometry, not as rebuilt semantics. Realizes UJ-1.

**Functional Requirements:**

#### FR-12: Feed At Least One Backend From Geometry IR

Athena can feed at least one current downstream backend from `Geometry IR`. Realizes UJ-1.

**Consequences (testable):**
- The backend receives geometry-ready structure from the geometry stage rather than reconstructing presentation internally from semantic data alone.
- The proof remains deterministic for the same semantic, layout, and geometry inputs.
- The backend remains downstream of runtime and compiler ownership rules.

## 5. Non-Goals (Explicit)

- M2 does not deliver a full CAD or ECAD editor.
- M2 does not allow freeform pixel-level editing to become the source of engineering truth.
- M2 does not collapse `Layout IR` or `Geometry IR` back into `Engineering IR`.
- M2 does not deliver browser-first or WASM Studio parity.
- M2 does not deliver cloud collaboration, multi-user conflict handling, or enterprise deployment control.
- M2 does not add broad external target adapters such as `QElectroTech`, `EPLAN`, or `FreeCAD` beyond what is strictly needed for the first projection proof.
- M2 does not attempt a full standalone knowledge compiler.

## 6. MVP Scope

### 6.1 In Scope

- First `Layout IR`
- First `Geometry IR`
- At least two supported `View Definition` types over the same semantic model
- Runtime-owned multi-view projection and view switching
- Identity-preserving projection layers
- Projection refresh after at least one supported semantic mutation path
- At least one backend or viewer path consuming `Geometry IR`
- Desktop-first projection proof

### 6.2 Out of Scope for MVP

- Arbitrary manual layout authoring workflows
- Production-grade web Studio delivery
- Full panel, docking, welcome, or IDE-like shell parity
- Broad target-export ecosystem
- General-purpose layout optimization engine `[NON-GOAL for MVP]`
- Geometry editing as a first-class mutation system `[NON-GOAL for MVP]`

## 7. Success Metrics

**Primary**
- **SM-1**: One active `Project` can derive at least two synchronized views from one `Engineering IR` source with preserved `Canonical Identity`. Validates FR-1, FR-3, FR-4, FR-6.
- **SM-2**: After at least one supported semantic command, Athena refreshes affected projection state and rendered output without reintroducing view-local semantic authority. Validates FR-7, FR-8, FR-9.

**Secondary**
- **SM-3**: A desktop operator can switch between supported views and follow one semantic object across those views through stable identity. Validates FR-5, FR-10, FR-11.
- **SM-4**: At least one backend or viewer path consumes `Geometry IR` directly as downstream renderable structure. Validates FR-2, FR-12.

**Counter-metrics (do not optimize)**
- **SM-C1**: Do not optimize for arbitrary manual geometry editing volume; that would encourage geometry to become the hidden source of truth. Counterbalances SM-2.
- **SM-C2**: Do not optimize for view-specific hacks that bypass runtime or semantic identity just to make one projection look correct. Counterbalances SM-1 and SM-3.

## 8. Cross-Cutting NFRs

- **NFR-1 Determinism:** Given the same `Engineering IR`, `View Definition`, and projection inputs, `Layout IR`, `Geometry IR`, and downstream outputs remain deterministic.
- **NFR-2 Canonical Ownership:** `Engineering IR` remains the only canonical semantic authority; layout and geometry are always downstream consequences.
- **NFR-3 Inspectability:** Runtime inspection can explain how a view was derived, which semantic identities it references, and why a projection refreshed.
- **NFR-4 Incrementality:** Projection refresh should be dependency-scoped where the runtime can justify it rather than defaulting to blind full rebuild.
- **NFR-5 Desktop-First Delivery:** The first proof is optimized for JVM-first local execution and desktop inspection. `[ASSUMPTION: Web/WASM delivery remains future work unless later planning explicitly promotes it.]`
- **NFR-6 Performance Posture:** The first supported multi-view workflows must remain interactive enough for a local operator proof rather than behaving like a batch-only export pipeline. `[ASSUMPTION: Exact budgets are deferred to architecture and benchmark design.]`

## 9. Constraints and Guardrails

### 9.1 Semantic Guardrails

- Semantic commands continue to mutate only canonical semantic state.
- Layout and geometry layers may react to semantic change but may not redefine engineering meaning.
- Graph, diff, history, and validation remain anchored in canonical identity even when projection layers grow.

### 9.2 Studio Guardrails

- Compose workbench remains a downstream consumer of runtime-owned projection state.
- M2 should not let view code become the hidden owner of layout rules, geometry rules, or semantic recovery behavior.
- Desktop remains the primary inspection surface in this milestone.

### 9.3 Architecture Guardrails

- M2 builds on the current grouped module topology rather than undoing it.
- New projection layers should be explicit enough that later web, renderer, and target work can consume them without semantic reinterpretation.
- The system should evolve toward the manifesto chain `Engineering IR -> Layout IR -> Geometry IR -> target/backend`.

## 10. Platform and Delivery

- **Primary platform:** JVM desktop
- **Primary application shell:** `:apps:desktop-viewer`
- **Shared UI substrate:** `:ui:compose-workbench`
- **Primary runtime owner:** `:kernel:runtime`
- **Current backend proof:** SVG and Compose-based projection
- **Deferred platform:** web/WASM enrichment after M2 proof

## 11. Open Questions

1. Which two `View Definition` types should be the official M2 proof pair: cabinet plus wiring, cabinet plus functional, or another combination?
2. Should `View Definition` be authored through config, runtime-owned commands, or a first dedicated layout authoring surface?
3. How much manual layout intent, if any, is allowed in M2 before it starts behaving like a full editor?
4. Should `Geometry IR` be serialized as a published artifact in M2 or remain an internal runtime/compiler contract first?
5. Does M2 require one dedicated layout module and one dedicated geometry module, or is one projection-focused module acceptable for the first proof as long as the boundaries remain explicit?

## 12. Assumptions Index

- Inline assumption from 2.3 UJ-3 - M2 proves dependency-scoped projection refresh over at least one supported change path rather than a general arbitrary-layout editing system.
- Inline assumption from 8 NFR-5 - Web/WASM delivery remains future work unless later planning explicitly promotes it into M2 scope.
- Inline assumption from 8 NFR-6 - Exact performance budgets are deferred to architecture and benchmark design rather than frozen in this PRD.
