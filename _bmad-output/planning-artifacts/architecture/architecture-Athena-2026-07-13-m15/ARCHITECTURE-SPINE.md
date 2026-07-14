---
title: 'Athena M15'
type: architecture-spine
purpose: build-substrate
altitude: initiative
paradigm: 'guided semantic authoring above one mutation authority with component-knowledge-backed workbench surfaces'
scope: 'Athena M15 guided semantic authoring foundation'
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
  - 'FR-11'
  - 'FR-12'
  - 'FR-13'
sources:
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/prd.md'
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/addendum.md'
  - 'docs/roadmap/athena-milestone-roadmap.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m13/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md'
companions:
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/prd.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md'
---

# Architecture Spine - Athena M15

## Design Paradigm

Athena M15 is a **guided semantic authoring above one mutation authority with component-knowledge-backed workbench surfaces** architecture.

- **guided semantic authoring** means mainstream users create and modify engineering intent through workbench surfaces such as palette, inspector, connect flow, forms, and later AI or templates rather than by authoring raw DSL directly.
- **one mutation authority** means all guided authoring still converges through the completed M8 semantic mutation path before canonical state changes.
- **component-knowledge-backed workbench surfaces** means available authoring actions derive from governed M14 component knowledge rather than from frontend hardcoded lists or graph-local behavior.

## Inherited Invariants

| Inherited | From parent | Binds here |
| --- | --- | --- |
| AD-17 | `architecture-Athena-2026-07-08-m5` | Runtime continues to own one active repository graph session per product window. |
| AD-18 | `architecture-Athena-2026-07-08-m5` | IDE work stays additive and product-operability scoped through existing seams. |
| AD-34 | `architecture-Athena-2026-07-10-m8` | One mutation authority above source and graph remains binding. |
| AD-38 | `architecture-Athena-2026-07-10-m8` | Unified semantic review facts remain shared across interaction origins. |
| AD-39 | `architecture-Athena-2026-07-10-m8` | Cross-surface anchoring continues to use canonical semantic identity. |
| AD-41 | `architecture-Athena-2026-07-10-m8` | Source and graph editing still converge before review and persistence. |
| AD-49 | `architecture-Athena-2026-07-11-m9` | Existing semantic delivery surfaces remain the product path. |
| AD-67 | `architecture-Athena-2026-07-12-m13` | `Presentation IR` remains a dedicated downstream layer. |
| AD-72 | `architecture-Athena-2026-07-12-m13` | Canonical semantic identity stays stronger than presentation occurrences. |
| AD-74 | `architecture-Athena-2026-07-12-m13` | Downstream packs remain extension-compatible assets. |
| AD-75 | `architecture-Athena-2026-07-13-m14` | Component knowledge resolution remains a dedicated layer above `Engineering IR` and below later consumers. |
| AD-77 | `architecture-Athena-2026-07-13-m14` | Engineering concepts remain vendor-neutral while vendor parts remain implementations. |
| AD-78 | `architecture-Athena-2026-07-13-m14` | Semantic ports remain first-class knowledge contracts. |
| AD-80 | `architecture-Athena-2026-07-13-m14` | Resolved component knowledge remains read-only and does not create a new mutation path. |
| AD-82 | `architecture-Athena-2026-07-13-m14` | DSL remains canonical serialization, not the default human interface. |

## Invariants & Rules

### AD-84 - M15 Introduces Authoring Intent Above M8, Not A Frontend Mutation Shortcut

- **Binds:** `FR-1`, `FR-2`, `FR-12`, `FR-13`
- **Prevents:** palette clicks, inspector edits, or graph gestures from mutating canonical state directly
- **Rule:** M15 introduces a dedicated authoring-intent layer above the existing M8 semantic mutation authority. Authoring surfaces may request creation, update, connection, reveal, and preview actions only by emitting typed authoring intents. No M15 surface may write canonical engineering state, projection state, or persisted source directly.

### AD-85 - Authoring Intent Is Platform Capability, Not Extension-Local UI Logic

- **Binds:** `FR-1`, `FR-3`, `FR-5`
- **Prevents:** every new frontend surface from inventing its own authoring contract or transport semantics
- **Rule:** Core authoring intent models and authoring orchestration belong to Athena platform modules. Domain extensions may contribute authorable component knowledge and optional domain-specific authoring metadata, but the intent contract, preview contract, acceptance semantics, and mutation handoff remain platform-owned.

### AD-86 - Guided Authoring Surfaces Consume Component Knowledge; They Do Not Redefine It

- **Binds:** `FR-3`, `FR-4`, `FR-5`, `FR-7`
- **Prevents:** component palettes, inspectors, or graph tools from becoming a second semantic catalog
- **Rule:** Available components, part options, semantic ports, and minimal physical traits shown in M15 workbench surfaces derive from the active M14 component-knowledge layer. Theia widgets, GLSP tools, and frontend stores may cache display-ready views, but they may not own component identity, compatibility truth, or vendor mapping truth.

### AD-87 - One Authoring Intent May Expand Into Multiple Governed Mutations

- **Binds:** `FR-1`, `FR-4`, `FR-6`, `FR-9`, `FR-12`
- **Prevents:** forcing UI-level actions to map one-to-one to low-level mutation primitives
- **Rule:** M15 authoring intent represents user intent, not renderer gestures or direct source edits. One accepted authoring intent may expand into several governed mutation operations, such as creating a component plus default tags, ports, placement metadata, or review facts. That expansion remains runtime-owned and inspectable before commit.

### AD-88 - Palette, Inspector, And Connect Flow Stay Consumers Of Shared Authoring Services

- **Binds:** `FR-3`, `FR-4`, `FR-5`, `FR-6`, `FR-9`
- **Prevents:** duplicate orchestration logic across the Theia sidebar, graph adapter, or later forms
- **Rule:** The first M15 workbench proof may use a Theia left component panel, a right inspector, and graph-side connect flow, but those surfaces must all consume shared authoring services through Athena-owned transport and runtime seams. UI widgets remain thin consumers of authoring state and responses.

### AD-89 - Port-Aware Connect Filtering Uses M14 Semantic Port Meaning, Not Full Rule Explosion

- **Binds:** `FR-7`, `FR-8`, `FR-9`
- **Prevents:** M15 from turning into a broad compatibility engine or frontend-only heuristics layer
- **Rule:** The first M15 connect flow may filter allowed targets by stable semantic-port direction, signal-family, and narrow protocol-bearing metadata from M14. Broader engineering sufficiency, overload, compliance, or contextual compatibility remains later M9 knowledge-runtime logic.

### AD-90 - Guided Authoring Must Preserve Three-Way Synchronization Through Canonical Rebuild

- **Binds:** `FR-4`, `FR-6`, `FR-9`, `FR-10`, `FR-11`
- **Prevents:** source, graph, and panel views from drifting into separate local truths
- **Rule:** M15 synchronization remains canonical-state-first. Panel action, graph action, or DSL edit all converge through runtime-owned canonical rebuild. Graph refresh, inspector refresh, diagnostics refresh, and source refresh are downstream consequences of canonical state and may not rely on ad hoc frontend patching as the source of truth.

### AD-91 - Mutation Preview Reuses Review-First Product Semantics

- **Binds:** `FR-2`, `FR-12`, `FR-13`
- **Prevents:** guided authoring from becoming an opaque "click equals commit" path
- **Rule:** M15 preview and approval semantics build on the completed M6 and M8 review-first direction. Guided authoring actions must surface pending semantic consequences before acceptance. Approval hands off to canonical mutation commit. Rejection discards the pending intent outcome without partially mutating canonical state.

### AD-92 - Reveal Remains Canonical-Identity-First Across All Authoring Surfaces

- **Binds:** `FR-10`, `FR-11`
- **Prevents:** palette entries, inspector rows, graph shapes, and source ranges from fragmenting identity
- **Rule:** Source reveal, graph reveal, inspector selection, and semantic SCM reveal must all anchor to canonical semantic identity first. Presentation occurrence ids, graph element ids, and widget-local selection state remain downstream references only.

### AD-93 - First Proof Stays Narrow, Productive, And Siemens-First

- **Binds:** `FR-3`, `FR-4`, `FR-6`, `FR-9`, `FR-13`
- **Prevents:** M15 from widening into broad catalog ingestion, routing engines, or full ECAD parity
- **Rule:** The first M15 proof stays narrow: one governed repository, one Siemens-first electrical slice, a small authorable component set such as PLC CPU, 24V supply, motor, and one communication or power connection scenario. M15 does not claim broad vendor breadth, macro libraries, advanced routing, or unrestricted authoring depth.

## Layer Responsibilities

### Authoring Surfaces

Own:

- user interaction style
- palette browsing and search behavior
- inspector layout
- graph gestures and affordances
- future form, template, or AI entrypoints

Do not own:

- canonical semantic mutation
- component knowledge truth
- preview acceptance semantics

### Authoring Intent Layer

Owns:

- typed authoring requests
- intent payload validation
- user-intent-level operation categories
- previewable authoring outcomes before mutation acceptance

Does not own:

- canonical persistence
- component catalog truth
- renderer-specific gestures

### Authoring Runtime / Service Layer

Owns:

- intent expansion into governed mutation requests
- authoring previews
- allowed-target queries for narrow connect flow
- orchestration across runtime, review, and transport seams

Does not own:

- domain concept truth
- repository graph truth
- final presentation rendering

### M8 Semantic Mutation Authority

Owns:

- the only authoritative write path
- acceptance and persistence of semantic change
- review, undo, redo, and audit coherence

Does not own:

- UI widget flow
- authoring catalog presentation

### Engineering IR

Owns:

- canonical authored engineering structure
- stable identities
- authored component, port, and connection references

Does not own:

- palette grouping
- inspector view models
- authoring previews

### M14 Component Knowledge

Owns:

- concept identity
- vendor implementation mapping
- semantic-port meaning
- minimal physical traits

Does not own:

- authoring flow orchestration
- mutation authority
- frontend-local filtering truth beyond exported contracts

### Projection / Presentation / Workbench

Consume:

- canonical engineering state
- accepted mutations
- component knowledge outputs where needed

Own:

- downstream visual realization
- workbench-specific layout and interaction polish

Do not own:

- semantic authoring truth
- acceptance semantics

## New Platform Boundaries

### `kernel/authoring-model`

Purpose:

- define typed authoring intents
- define preview models
- define accepted authoring result contracts
- define reveal and selection request contracts where needed

Examples:

- `CreateComponentIntent`
- `UpdateComponentPropertiesIntent`
- `ConnectPortsIntent`
- `RevealSubjectIntent`
- `AuthoringPreview`

### `kernel/authoring-runtime`

Purpose:

- validate and normalize authoring intents
- derive authoring options from active component knowledge
- expand authoring intents into governed mutation requests
- assemble preview payloads and acceptance flows

Boundary:

- no direct persistence outside M8
- no domain concept ownership outside M14

## IDE And Transport Responsibilities

### `ide/lsp`

Purpose:

- expose authoring-friendly read and command seams through Athena-owned transport
- carry component list, inspector snapshot, allowed connect targets, preview, and commit requests

Boundary:

- transport remains a boundary only
- LSP request handlers delegate to runtime-owned authoring services
- no frontend-originated semantic truth is accepted without runtime validation

### `ide/theia-*`

Purpose:

- host the first component panel and inspector
- present authoring previews
- bind graph and panel selection to canonical identity

Boundary:

- keep workbench additions under the current `ide` grouping
- do not create a second product-local mutation model

### `integrations/graph-glsp`

Purpose:

- host the first port-aware connect gesture and graph-originated authoring actions

Boundary:

- graph gestures emit authoring intent or delegated authoring requests
- graph adapter does not create semantic connections directly

## Transport Shape

The first M15 transport set may include Athena-owned requests such as:

- list available components
- inspect selected authorable subject
- preview create component
- preview update component properties
- preview connect ports
- accept or reject guided authoring preview

Exact names remain transport detail and should follow the current Athena LSP naming style when implemented.

## Authoring Flow Model

The first M15 proof should follow this canonical flow:

1. User chooses a guided authoring action in panel, inspector, or graph.
2. Surface emits typed authoring intent.
3. Runtime authoring service resolves active component knowledge and current canonical state.
4. Runtime produces previewable semantic consequences.
5. User approves or rejects.
6. Approved preview hands off to M8 mutation authority.
7. Canonical state updates.
8. Source, graph, inspector, diagnostics, and semantic SCM all refresh from canonical rebuild.

## Proof Shape

The first M15 proof should stay narrow:

- one governed repository fixture
- one Theia component panel
- one Theia inspector
- one graph-side port-aware connect flow
- one shared preview-and-approve experience
- one Siemens-first electrical slice

That is enough to prove product viability without widening into final ECAD parity.

## Final Statement

M15 should prove:

> engineers can create governed engineering intent through guided workbench surfaces while Athena preserves one semantic mutation authority and one canonical serialization.

If this milestone succeeds, Athena crosses from architecture proof toward real product usability proof.
