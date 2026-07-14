---
title: 'Athena Renderer Foundation'
type: architecture-addendum
purpose: build-substrate
altitude: initiative
paradigm: 'semantic authority upstream, render vocabulary downstream, protocol and renderer decoupled'
scope: 'Athena renderer foundation beyond temporary M12 hardening'
status: draft
created: '2026-07-12'
updated: '2026-07-12'
binds:
  - 'FR-1'
  - 'FR-2'
  - 'FR-3'
  - 'FR-4'
  - 'FR-5'
  - 'FR-8'
  - 'FR-9'
  - 'FR-10'
sources:
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m12/prd.md'
  - 'draft/open/2026-07-09-Eplan-cross-compare-discuss.md'
  - 'draft/screenshort/README.md'
  - 'docs/roadmap/athena-milestone-roadmap.md'
---

# Athena Renderer Foundation

## Why This Addendum Exists

M12 proved that Athena can improve the first graphical surface, but it also exposed a harder truth:

- a generic graph stack can be polished
- a generic graph stack can be hardened
- a generic graph stack cannot, by itself, become a professional electrical operator surface

The missing problem is not color, border radius, or toolbar density.

The missing problem is **render vocabulary**.

Athena already owns:

- canonical engineering semantics
- projection families
- sheets
- notation packs
- anchors
- endpoint identity
- routing-corridor guidance

But the current frontend still collapses most of that into:

- rectangular nodes
- free text labels
- generic edge paths

That collapse is the architectural limit we must remove.

## Core Conclusion

Athena should not bet on a single diagram framework as the product architecture.

Instead Athena should separate four concerns deliberately:

1. **Workbench host**
2. **Diagram interaction protocol**
3. **Electrical render vocabulary**
4. **Actual drawing engine**

Those four concerns are not the same thing and should not be owned by the same library.

## Recommended Foundation Stack

### 1. Theia Owns The Product Shell

Theia remains the host for:

- workspace model
- editor lifecycle
- panels
- outline
- SCM integration
- console
- command system
- menus and keybindings
- future AI chat and provider management

Theia is the IDE platform, not the electrical renderer.

### 2. GLSP Owns Diagram Protocol And Interaction Topology

GLSP remains the preferred architectural pattern for:

- client/server diagram separation
- Theia integration
- server-owned edit semantics
- multi-surface transport compatibility
- future web and desktop reuse

Athena should follow **GLSP as protocol architecture**, not "GLSP default client rendering equals final product".

That means:

- server publishes diagram state and governed interaction contracts
- client sends view and gesture intents
- semantic ownership remains upstream
- renderer stays replaceable

### 3. Athena Must Introduce Presentation IR

This is the main architectural decision.

Between projection and canvas, Athena should add a dedicated downstream model with a neutral top-level name:

## Presentation IR

This layer is not semantic truth.
This layer is not engineering IR.
This layer is not projection geometry.

It is the first model that can say:

- this subject is a contactor symbol archetype
- this occurrence uses a cabinet-face presentation
- this label is a terminal legend, not a floating pill
- this wire should be drawn as conductor segments with junction behavior
- this sheet has frame, zones, title block, and electrical reading posture
- this cross-reference should render as electrical navigation furniture

Electrical is the first serious pack on this layer, not the final name of the layer itself.

Without this layer, every client will keep reconstructing domain presentation ad hoc.

That would permanently trap Athena in "generic graph with domain decorations".

## Layer Boundaries

The target layered flow should be:

```text
DSL
  ->
Engineering IR
  ->
Projection Model
  ->
Presentation IR
  ->
Canvas Protocol Model
  ->
Renderer Backend
```

### Engineering IR

Owns:

- canonical engineering identity
- component, port, connection, property semantics
- domain-level correctness

Does not own:

- symbol appearance
- sheet furniture
- label leaders
- terminal mark style
- border weight
- stroke color

### Projection Model

Owns:

- view family
- view-scoped placements
- sheet identity
- notation-pack selection
- repeated-reference occurrence identity
- electrical anchors
- endpoint mapping
- routing corridor guidance

Does not own:

- final draw commands
- renderer-specific primitives
- skin tokens
- GPU/SVG scene organization

### Presentation IR

Owns:

- symbol archetype id
- presentation variant id
- terminal presentation contract
- leader-line contract
- conductor segment contract
- junction glyph contract
- text slots and label placement contract
- sheet frame and zone presentation
- visual state slots such as selected, diagnostic, related, review-affected
- notation token pack and style token references

Does not own:

- canonical engineering semantics
- mutation authority
- source of truth for routing endpoints

### Canvas Protocol Model

Owns:

- normalized scene payload needed by one renderer client
- renderer-facing display tree
- hit-target mapping
- incremental update transport

This layer is allowed to differ between:

- GLSP/Sprotty client
- custom PixiJS client
- future desktop renderer
- test snapshot renderer

### Renderer Backend

Owns:

- actual draw calls
- viewport math
- batching
- zoom performance
- label culling
- GPU/SVG path generation

Does not own:

- engineering meaning
- notation selection logic
- endpoint truth

## Framework Roles

### Theia

Use for:

- whole IDE shell
- workbench orchestration
- product integration

Do not use as:

- electrical canvas architecture

### GLSP

Use for:

- diagram editor architecture
- interaction protocol shape
- Theia integration path
- command and intent boundary

Do not confuse with:

- final electrical presentation layer

### Sprotty / SVG

Use for:

- current proof surface
- debug-friendly diagram rendering
- early integration and protocol verification

Do not treat as:

- final answer for dense electrical operator canvas

Reason:

- it is too easy to drift into generic node-edge presentation
- it becomes expensive to fake ECAD posture with pure CSS and rectangles

### ELK

Use for:

- batch auto-layout
- orthogonal route derivation
- port-constrained layout experiments
- offline or command-triggered route improvement

Do not use as:

- the sole owner of interactive routing truth

### libavoid or equivalent object-avoiding orthogonal router

Use for:

- interactive rerouting
- obstacle-aware connector recomputation
- dense conductor corridor refinement

This should be evaluated as a routing engine, not a semantic layer.

### PixiJS-class renderer

Use for:

- high-density 2D canvas
- future WebGL/WebGPU acceleration
- large-scene performance
- richer electrical symbol rendering without SVG DOM cost explosion

This is the strongest long-term direction for Athena's main graph/electrical surface.

## Why Not Bet On Generic Diagram Toolkits

Generic toolkits can help with:

- nodes
- edges
- ports
- dragging
- routers
- selections

But Athena is not building a generic graph editor.

Athena is building a professional engineering surface where:

- one canonical subject may have multiple occurrences
- views are family-specific
- notation packs differ by operator context
- sheet posture matters
- cross-reference navigation is first-class
- semantic review and knowledge diagnostics must stay coherent

That means generic toolkits are references or temporary bridges, not the architecture.

## The Key New Contract: Symbol Archetypes

Notation packs today identify broad categories such as:

- `device.schematic.default`
- `port.cabinet.default`
- `connection.wiring.default`

That is useful, but still too shallow for professional render fidelity.

Athena needs a richer downstream symbol vocabulary.

Recommended structure:

```text
notation-pack
  ->
symbol archetype
  ->
presentation variant
  ->
render tokens
```

Example shape:

```text
device.contactor
device.breaker
device.motor
device.plc-card
terminal.single-level
terminal.multi-level
wire.power
wire.control
wire.pe
wire.n
crossref.coil-contact
crossref.destination-arrow
sheet.frame.a3
```

This vocabulary belongs below projection and above renderer.

## Routing Ownership

Athena must keep routing ownership explicit:

### Semantic layer owns

- connection identity
- source/target canonical endpoint intent

### Projection layer owns

- anchor occurrence
- preferred corridor
- sheet/view-local route guidance

### Render IR owns

- conductor segmentation style
- junction glyph policy
- line-class style selection
- presentation-level simplification rules

### Renderer owns

- exact displayed path
- pixel-perfect line placement
- performance optimizations

This split prevents the classic failure mode where the visible route becomes hidden truth.

## Multi-Backend Strategy

Athena should deliberately support more than one renderer backend over time.

Recommended order:

### Backend A: SVG Proof Backend

Purpose:

- deterministic tests
- debugging
- transport inspection
- easy snapshot generation

### Backend B: Main Interactive Electrical Canvas Backend

Likely target:

- PixiJS-class renderer

Purpose:

- real operator surface
- high density
- future WebGPU path

### Backend C: Export/Print Backend

Purpose:

- PDF
- SVG export
- print sheets
- image snapshots

This is exactly why Athena needs Presentation IR instead of tying itself to one runtime canvas library.

## Migration Plan

### Phase 1 - Stop Treating Graph Model As Final Presentation

- keep current GLSP and Theia boundary
- keep current runtime projection transport
- add Presentation IR derivation after projection
- continue using SVG backend first

### Phase 2 - Replace Generic Node/Edge Presentation With Symbol-Driven Presentation

- node rectangles stop being the default electrical representation
- labels stop being generic label nodes
- wires stop being generic edge paths
- symbols and conductor segments come from archetypes

### Phase 3 - Introduce Renderer Backend Abstraction

- SVG backend remains for proof and regression
- new interactive backend consumes the same Presentation IR

### Phase 4 - Add Performance And Dense-Scene Guarantees

- label culling
- layer batching
- selective redraw
- large-scene benchmarks

### Phase 5 - Add True Bidirectional Graph Editing Above Stable Render Contracts

- graph gestures operate on governed semantic intents
- render backend remains stateless about engineering truth

## Anti-Goals

Athena should explicitly avoid:

- moving engineering meaning into symbol code
- making renderer geometry the source of truth
- choosing a diagram library and letting it define Athena's operator model
- hard-coding one EPLAN-like look into the kernel
- encoding IEC appearance rules directly into frontend widgets
- treating M12 visual cleanup as final renderer architecture

## Final Decision

Athena should proceed with this position:

- **Theia is the IDE foundation**
- **GLSP is the diagram architecture foundation**
- **Presentation IR is the missing Athena-owned downstream foundation**
- **SVG/Sprotty is an intermediate proof backend**
- **a PixiJS-class renderer is the likely long-term main electrical canvas**
- **routing and rendering stay replaceable behind Athena-owned contracts**

That is the balanced path.

It keeps Athena aligned with the original semantic-first design while still giving the product a route to become a truly professional engineering surface rather than a dressed-up graph editor.
