---
title: 'Athena M12'
type: architecture-spine
purpose: build-substrate
altitude: initiative
paradigm: 'downstream electrical readability with layered routing ownership and additive operator-dense workbench hardening'
scope: 'Athena M12 electrical renderer correctness and workbench hardening'
status: draft
created: '2026-07-12'
updated: '2026-07-12'
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
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m12/prd.md'
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m12/addendum.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md'
  - 'draft/m12/001-draft.md'
  - 'draft/open/2026-07-09-Eplan-cross-compare-discuss.md'
  - 'draft/screenshort/README.md'
  - 'docs/roadmap/athena-milestone-roadmap.md'
  - 'docs/usages/m11-proof-usage.md'
companions:
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m12/prd.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md'
---

# Architecture Spine - Athena M12

## Design Paradigm

Athena M12 is a **downstream electrical readability with layered routing ownership and additive operator-dense workbench hardening** architecture.

- **downstream electrical readability** means M12 proves that electrical readability can improve materially without moving engineering truth into renderer state, symbols, or coordinates.
- **layered routing ownership** means endpoint ownership, preferred routing guidance, and actual visual path are deliberately split across semantic, projection, and renderer layers instead of collapsing into one hidden graph model.
- **additive operator-dense workbench hardening** means canvas behavior, panel density, and navigation trust improve through existing runtime, `ide/lsp`, and Theia workbench seams rather than by inventing a second frontend authority.

## Inherited Invariants

| Inherited | From parent | Binds here |
| --- | --- | --- |
| AD-18 | `architecture-Athena-2026-07-08-m5` | IDE work stays additive and product-operability scoped through existing seams. |
| AD-23 | `architecture-Athena-2026-07-09-m6` | Theia-hosted surfaces remain downstream bridges rather than semantic cores. |
| AD-25 | `architecture-Athena-2026-07-09-m6` | Domain-specific enrichments remain additive through hosted plugin contracts. |
| AD-27 | `architecture-Athena-2026-07-09-m7` | `kernel/projection-model` remains the dedicated renderer-neutral projection boundary. |
| AD-28 | `architecture-Athena-2026-07-09-m7` | Engineering identity remains in the object graph; view definitions and renderer assets stay downstream. |
| AD-29 | `architecture-Athena-2026-07-09-m7` | Layout and geometry remain view-scoped metadata, not engineering truth. |
| AD-30 | `architecture-Athena-2026-07-09-m7` | The graphical workbench continues to consume runtime-owned state through Athena-owned transport. |
| AD-34 | `architecture-Athena-2026-07-10-m8` | One mutation authority above source and graph remains binding. |
| AD-38 | `architecture-Athena-2026-07-10-m8` | Unified semantic review facts remain shared across interaction origins. |
| AD-39 | `architecture-Athena-2026-07-10-m8` | Cross-surface anchoring continues to use canonical semantic identity. |
| AD-43 | `architecture-Athena-2026-07-11-m9` | Knowledge derivation starts from canonical engineering state only. |
| AD-47 | `architecture-Athena-2026-07-11-m9` | Engineering sufficiency remains typed and separate from structural validation. |
| AD-49 | `architecture-Athena-2026-07-11-m9` | Existing semantic delivery surfaces remain the product path for new downstream meaning. |
| AD-50 | `architecture-Athena-2026-07-12-m10` | Runtime owns deterministic reasoning-context assembly for AI-assisted surfaces. |
| AD-51 | `architecture-Athena-2026-07-12-m10` | AI output remains typed proposal and never becomes canonical truth. |
| AD-53 | `architecture-Athena-2026-07-11-m11` | Electrical workbench depth starts from canonical engineering entities, not symbols. |
| AD-54 | `architecture-Athena-2026-07-11-m11` | Explicit electrical projection families remain above one canonical subject. |
| AD-55 | `architecture-Athena-2026-07-11-m11` | Sheet identity remains projection-owned and separate from engineering identity. |
| AD-56 | `architecture-Athena-2026-07-11-m11` | Symbol and notation packs remain governed downstream contracts. |
| AD-57 | `architecture-Athena-2026-07-11-m11` | Repeated references and cross references anchor to canonical semantic identity. |
| AD-59 | `architecture-Athena-2026-07-11-m11` | Workbench density remains additive through existing runtime and LSP seams. |
| AD-60 | `architecture-Athena-2026-07-11-m11` | Electrical workbench depth must preserve mutation, review, and knowledge coherence. |
| AD-61 | `architecture-Athena-2026-07-11-m11` | Product references constrain UX and workflow, not semantic ownership. |

## Invariants & Rules

### AD-62 - Electrical Readability Remains A Downstream Consequence Of Canonical State

- **Binds:** `FR-1`, `FR-3`, `FR-8`, `FR-9`
- **Prevents:** renderer-correctness work from becoming a hidden semantics rewrite
- **Rule:** M12 may improve conductor appearance, connection visibility, overlap handling, and scene readability only as a downstream consequence of canonical engineering entities and governed projection contracts. No renderer-correctness feature may require semantic truth to be stored in graph-local edges, visual grouping, symbol positions, or raw coordinates.

### AD-63 - Endpoint Anchoring Resolves Through Governed Projection Anchors

- **Binds:** `FR-1`, `FR-2`, `FR-10`
- **Prevents:** connections from snapping to approximate node centers or renderer-only anchor guesses that cannot be traced back to electrical meaning
- **Rule:** Every rendered electrical connection must resolve from canonical subject identity through governed projection-owned anchor or port information before it reaches the renderer. The renderer may style, offset, or visually refine anchor placement, but it may not invent the endpoint ownership model.

### AD-64 - Routing Ownership Stays Explicitly Split Across Semantic, Projection, And Renderer Layers

- **Binds:** `FR-1`, `FR-3`, `FR-8`, `FR-9`
- **Prevents:** route geometry from silently becoming truth or one renderer framework from becoming the architecture center
- **Rule:** M12 freezes the routing split:
  - **semantic layer** owns endpoint identity and connection intent
  - **projection layer** may own preferred routing corridor or equivalent guidance
  - **renderer layer** owns the actual visual path only
  Renderer heuristics may improve readability, but rendered route geometry is never canonical engineering truth and never the first owner of connection semantics.

### AD-65 - Canvas And Viewport Hardening Remain Product-Surface Improvements, Not Graph-Model Authority

- **Binds:** `FR-4`, `FR-5`, `FR-7`
- **Prevents:** fit, pan, zoom, and focus fixes from turning into a second frontend-owned scene model
- **Rule:** Viewport behavior remains derived from runtime-owned projection payloads and downstream scene bounds, not from a private workbench truth model. M12 may add scene-bound calculations, focus targets, and utility overlays, but those stay disposable product-surface behavior. Closing or collapsing support panels must expand usable canvas area predictably without changing semantic or projection authority.

### AD-66 - Electrical Navigation And Reference Reveal Resolve Through Canonical Identity First

- **Binds:** `FR-2`, `FR-9`, `FR-10`
- **Prevents:** cross-reference navigation from depending on renderer-local object ids, widget aliases, or repeated-instance drift
- **Rule:** Cross-reference navigation, go-to-source, go-to-destination, related-diagnostic reveal, and related-semantic-subject reveal all start from canonical semantic identity and existing repeated-reference or sheet contracts. Renderer-local ids, occurrence ids, and workbench widget ids remain aliases only. Navigation trust is achieved by stronger identity resolution, not by inventing a new electrical-navigation authority.

### AD-67 - Operator Density Improvements Stay Additive And Canvas-Subordinate

- **Binds:** `FR-5`, `FR-6`, `FR-7`
- **Prevents:** panel hardening from devolving into decorative UI churn or a shell redesign that steals attention from the graph
- **Rule:** Athena-owned hierarchy, diagnostics, outline, inspector, and graph-side controls may become denser and more compact in M12, but they remain additive surfaces around a canvas-first electrical workspace. Lists, trees, tables, and compact icon-led controls are preferred where they improve operator throughput. Card-heavy demo framing, decorative empty states, and visually dominant support chrome are not valid architecture outcomes.

### AD-68 - Readability Benchmark Tiers Are A Required Architecture Contract

- **Binds:** `FR-3`, `FR-4`, `FR-8`
- **Prevents:** M12 from claiming renderer trust based only on a small pretty proof scene
- **Rule:** M12 must define at least one readability benchmark tier beyond the current M11 dense baseline, with materially higher device and wire counts and explicit acceptance checks for label readability, usable zoom or fit behavior, and acceptable interaction quality. This benchmark is part of the architecture proof, not optional polish.

### AD-69 - Product References Constrain Operator Expectations, Not Runtime Ownership

- **Binds:** `FR-6`, `FR-7`, `FR-10`
- **Prevents:** QElectroTech or EPLAN familiarity from pulling Athena into legacy ownership assumptions or framework-specific lock-in
- **Rule:** QElectroTech and EPLAN may shape connection readability, operator density, sheet ergonomics, and cross-reference expectations. They do not own Athena's semantic contracts, routing truth, mutation authority, or runtime delivery seams. M12 may borrow workflow expectations aggressively while keeping ownership inside Athena's existing semantic and projection spine.

```mermaid
flowchart LR
  source[Authored Athena source]
  compiler[kernel/compiler]
  ir[kernel/engineering-model]
  projection[kernel/projection-model]
  families[Electrical families]
  sheets[Sheet contracts]
  notation[Notation packs]
  refs[Repeated refs / cross refs]
  anchors[Projection anchors]
  corridor[Preferred routing corridor]
  runtime[kernel/runtime]
  lsp[ide/lsp]
  frontend[ide/theia-frontend]
  graph[integrations/graph-glsp]
  renderer[Rendered path / viewport / panel surfaces]
  review[Diagnostics / review / AI reasoning]

  source --> compiler
  compiler --> ir
  ir --> projection
  projection --> families
  families --> sheets
  families --> notation
  families --> refs
  families --> anchors
  families --> corridor
  projection --> runtime
  runtime --> lsp
  lsp --> frontend
  frontend --> graph
  graph --> renderer
  runtime --> review
  review --> lsp
```

## Consistency Conventions

| Concern | Convention |
| --- | --- |
| Naming (entities, files, interfaces, events) | Prefer `ElectricalAnchor`, `RoutingCorridor`, `ReferenceNavigation`, `ViewportBounds`, `RendererBenchmarkTier`, and `WorkbenchDensity` or equivalent explicit names. Avoid naming renderer-owned outputs as if they were canonical semantics, such as `ConnectionTruth`, `RouteSemantic`, or `PageEntity`. |
| Data & formats (ids, dates, error shapes, envelopes) | Canonical semantic ids remain primary. Port ids, anchor ids, sheet ids, occurrence ids, and cross-reference ids remain downstream aliases or containers. Benchmark tiers should record scene counts and acceptance checks explicitly rather than relying on subjective screenshots alone. |
| State & cross-cutting (mutation, errors, logging, config, auth) | Runtime continues to own projection refresh, reveal coherence, mutation consequences, and review or reasoning context delivery. Frontend scene state, fit state, overlay state, and panel collapse state remain disposable. Renderer routing failures or visibility degradation should surface as inspectable product defects, never as hidden semantic fallbacks. |
| Build and dependency management | `kernel/engineering-model`, `kernel/projection-model`, `kernel/runtime`, and `ide/lsp` remain the semantic and delivery spine. `ide/theia-frontend` and `integrations/graph-glsp` may harden renderer behavior and workbench density only as downstream consumers. No M12 change may require moving projection truth into renderer packages. |

## Stack

| Name | Version |
| --- | --- |
| Java | 25 |
| Kotlin | 2.4.0 |
| Gradle | 9.6.1 |
| Node.js | 22+ |
| Yarn | 1.22.22 |
| Eclipse Theia | 1.73.1 |
| Theia AI packages | 1.73.1 |

## Structural Seed

```mermaid
flowchart TB
  repo[Engineering Repository]
  source[Source editor]
  runtime[kernel/runtime]
  engineering[kernel/engineering-model]
  projection[kernel/projection-model]
  electrical[extensions/domain-electrical]
  refs[Cross-reference contracts]
  anchors[Anchor + corridor contracts]
  lsp[ide/lsp]
  frontend[ide/theia-frontend]
  graph[integrations/graph-glsp]
  renderer[Electrical renderer surface]
  panels[Athena-owned dense panels]
  review[kernel/semantic-scm + M9/M10 outputs]

  repo --> source
  source --> lsp
  lsp --> runtime
  runtime --> engineering
  runtime --> projection
  projection --> electrical
  electrical --> refs
  electrical --> anchors
  runtime --> review
  lsp --> frontend
  frontend --> graph
  graph --> renderer
  frontend --> panels
  review --> lsp
```

```text
Athena/
  kernel/
    engineering-model/          # canonical engineering truth
    projection-model/           # electrical families, sheets, notation, refs, anchors, routing guidance
    runtime/                    # projection session, reveal, mutation, diagnostics, reasoning coherence
    semantic-scm/               # review, history, and consequence delivery reused by M12
  extensions/
    domain-electrical/          # electrical family, notation, reference, and anchor contribution seams
  ide/
    lsp/                        # sole semantic and projection transport boundary
    theia-frontend/             # viewport hardening, dense panels, additive navigation surfaces
  integrations/
    graph-glsp/                 # downstream graph adapter and renderer-facing scene translation
  examples/
    m11/                        # baseline dense proof
    m12/                        # larger readability benchmark tiers and renderer-correct proof cases
```

## Capability -> Architecture Map

| Capability / Area | Lives in | Governed by |
| --- | --- | --- |
| Electrical connection rendering that reads as conductor intent | projection anchor and corridor contracts plus downstream renderer consumption | AD-62, AD-63, AD-64 |
| Stable endpoint anchoring and port readability | canonical identity, projection anchors, graph adapter, workbench selection | AD-63, AD-66 |
| Deterministic routing and overlap readability improvements | projection guidance plus renderer-only visual path logic | AD-62, AD-64, AD-68 |
| Fit, pan, zoom, focus, and graph-surface dominance | `ide/theia-frontend`, graph adapter, scene-bound calculations from delivered projection payloads | AD-65, AD-67 |
| Dense Athena-owned hierarchy, diagnostics, outline, and inspector surfaces | `ide/theia-frontend`, existing runtime and LSP seams | AD-67, AD-69 |
| Cross-reference navigation and related reveal | repeated-reference contracts, runtime reveal, `ide/lsp`, workbench navigation surfaces | AD-66, AD-69 |
| Readability benchmark tiers and explicit visual limits | proof fixtures, tests, usage docs, and product validation path | AD-68 |
| Mutation, review, knowledge, and reasoning coherence under renderer hardening | `kernel/runtime`, `kernel/semantic-scm`, `ide/lsp`, existing M9 and M10 outputs | AD-34, AD-38, AD-50, AD-51, AD-66 |

## Deferred

- Full EPLAN or QElectroTech parity remains later than M12 and is not a success condition of this milestone.
- New kernel semantics, broader standards packs, and broader knowledge-pack ecosystems remain later than M12.
- Broad AI product redesign remains later than M12; M10 reasoning surfaces are reused but not reopened here.
- Unrestricted freeform drafting remains later than M12.
- Full browser-first, WASM, or alternate renderer architecture decisions remain later than the M12 hardening proof.
- Wider compatibility adapters, import flows, and production-scale electrical corpora remain later than the first renderer-trust milestone.
