---
title: Athena M46 Semantic Connection Kernel and Professional Engineering Documents
status: final
created: 2026-08-10
updated: 2026-08-10
---

# PRD: Athena M46 Semantic Connection Kernel and Professional Engineering Documents

## 0. Document Purpose

This PRD defines M46 for product, architecture, UX, epic, story, and implementation work. M46 turns
M45 package-backed occurrences and basic routes into one source-owned connection system whose semantic
meaning, normalized Connection IR, layout projection, editing, and professional IEC rendering remain
separate and traceable.

M46 is a breaking pre-1.0 milestone. Retired route-only, paint-owned, milestone-specific, and compatibility
paths have no preservation rights.

## 1. Vision

Athena must understand a connection before it draws a line. An engineer or AI states that Ports belong
to one Engineering Connection or Engineering Net, including connection kind, endpoint role, potential or
signal, physical requirements, and engineering constraints. Athena validates and compiles that meaning
into Connection IR, plans a readable projection, then paints a professional engineering document.

The same Engineering Connection may appear in a schematic, wiring view, terminal view, or future 3D
routing view without changing identity. Moving a route bend changes presentation. Reconnecting a Port
changes Engineering Reality and must pass semantic validation.

M46 succeeds when `examples/m46/rolling-shutter` renders the same class of clean IEC engineering sheet as
`draft/screenshort/equipement_d'un_volet_roulant.png`: thin orthogonal conductors, precise junctions,
compact labels, disciplined spacing, package-backed symbols, and a quiet white page.

## 2. Target User

### 2.1 Jobs To Be Done

- Define why Ports are connected without authoring route geometry.
- Express point-to-point and multi-target electrical connectivity without hidden graphic inference.
- Inspect connection kind, endpoints, potential, signal, physical requirements, validation, and source.
- Move or reconnect symbols while preserving connection identity where engineering meaning is unchanged.
- Produce a readable IEC-style sheet suitable for technical review and deterministic export.

### 2.2 Non-Users

- General-purpose CAD authors requiring arbitrary freehand geometry.
- Users expecting full EPLAN file/database compatibility.
- Manufacturing teams requiring complete cable fabrication, harness, terminal, or 3D routing reports in M46.

### 2.3 Key User Journeys

- **UJ-1. Mei authors a control connection.** Mei connects a PLC output Port to a contactor coil Port in
  Athena source. Validation accepts compatible roles and electrical facts. The compiled document shows a
  thin orthogonal route with stable identity and inspectable trace.
- **UJ-2. Mei creates a shared control Net.** Mei places a coil and indicator on one shared Net. Athena
  preserves one multi-endpoint semantic identity, emits an explicit branch topology, and paints one tiny
  junction only where conductors are electrically joined.
- **UJ-3. Mei corrects an invalid reconnect.** Mei drags a connection endpoint toward an incompatible PE
  Port. Athena previews the candidate, rejects the engineering operation in plain language, and keeps the
  accepted source and scene unchanged.
- **UJ-4. Mei reviews the rolling-shutter sheet.** Mei opens the M46 example in Theia and inspects a clean
  IEC page, selects a route to inspect engineering facts, exports SVG/PNG, restarts, and sees identical
  connection identities and geometry.

## 3. Glossary

- **Engineering Connection** - Source-owned engineering relationship between exactly two Connection
  Endpoints. It may exist without any placed representation.
- **Engineering Net** - Source-owned multi-endpoint electrical equivalence group with stable identity.
  Pairwise Engineering Connections do not substitute for one Engineering Net.
- **Connection Endpoint** - Reference to one Engineering Port plus its source, sink, or pass role in the
  connection graph. Endpoint role is distinct from device-local Port direction.
- **Connection Kind** - Source-owned engineering classification such as conductor, wire, cable core,
  jumper, busbar, signal, or fluid connection. M46 proves the electrical subset.
- **Physical Connection Requirement** - Typed implementation requirement such as cross-section, color,
  conductor type, shielding, termination, or required length. It is engineering meaning, not line style.
- **Connection Specification** - Source-owned defaults or overrides applied at project, potential/signal,
  Engineering Net, or Engineering Connection scope through deterministic precedence.
- **Connection IR** - Compiler-owned canonical handoff containing normalized endpoints, roles, topology,
  connection facts, resolved specifications, validation, provenance, and projection inputs.
- **Topology Operator** - Compiler-owned explicit topology fact such as branch, merge, interruption, or
  pass-through. It carries stable identity and ordering; it is not a Symbol.
- **Route Plan** - Compiler-derived orthogonal segments, junctions, crossings, bridges, interruption
  anchors, and label anchors for one projection.
- **Connection Projection** - Sheet-specific representation of Connection IR and Route Plan. It owns
  geometry and style references, never engineering meaning.
- **Connection Annotation** - Optional visible projection of selected connection properties. Hidden
  source ids, AST links, and internal Port ids are never canvas labels.
- **Connection Navigator** - Read/intent UI for inspecting and selecting source-owned connection facts
  independently of their placed representation.

## 4. Features

### 4.1 Semantic Connection Kernel

**Description:** Athena authors and validates Engineering Connections and Engineering Nets as first-class
Engineering Reality. Graphic proximity may propose a candidate but cannot create accepted connectivity.

#### FR-1: Author typed Engineering Connections

Engineers can author stable Engineering Connections between typed Engineering Ports. Realizes UJ-1.

**Consequences:**
- Identity, endpoints, endpoint roles, Connection Kind, provenance, and optional engineering facts survive
  recompile and reopen.
- An Engineering Connection can remain valid while unplaced and can have multiple projections.
- Port geometry and SVG anchors cannot define or override connection meaning.

#### FR-2: Author first-class Engineering Nets

Engineers can author one Engineering Net containing two or more Connection Endpoints. Realizes UJ-2.

**Consequences:**
- One Net remains one semantic identity; compiler does not replace it with arbitrary pairwise edges.
- Net potential/signal and endpoint membership are directly queryable without graphic traversal.
- Branch order used by a projection cannot change semantic Net membership.

#### FR-3: Separate endpoint role from Port direction

Athena validates source, sink, and pass endpoint roles independently from device-local `in`, `out`, or
`bidirectional` Port direction.

**Consequences:**
- Diagnostics name the exact Port, conflicting role/direction, and valid correction.
- Source/target exchange changes directional connection facts atomically across projections where allowed.

#### FR-4: Classify electrical Connection Kinds

Athena supports at least conductor, wire, cable core, jumper, busbar, and signal Connection Kinds with a
closed M46 electrical contract.

**Consequences:**
- Each kind declares required and permitted facts and valid endpoint compatibility.
- Unknown or incomplete kind data fails before Connection IR publication.
- Fluid, process, and 3D-specific behavior remains outside M46, without encoding electrical assumptions as
  universal kernel truth.

#### FR-5: Resolve connection specifications

Athena resolves typed Connection Specifications from project, potential/signal, Engineering Net, and
Engineering Connection scopes.

**Consequences:**
- Precedence is deterministic and inspectable; explicit connection values win over broader defaults.
- Conflicting same-precedence values fail with exact source locations.
- Cross-section, color code, conductor type, shielding, termination, and required length remain engineering
  facts; line color, width, dash, and bend geometry remain projection facts.

#### FR-6: Validate accepted connectivity

Athena validates Port compatibility, endpoint roles, domain, flow/signal type, potential, and connection
requirements before accepting or publishing connectivity. Realizes UJ-3.

**Consequences:**
- Invalid connect/reconnect operations commit no source, journal, scene, or lock change.
- Validation is deterministic and plain-language; renderer state is never validation input.

### 4.2 Canonical Connection IR

**Description:** Compiler lowers accepted semantic connectivity into one renderer-neutral Connection IR.
Connection IR is derived, canonical, stable, inspectable, and disposable.

#### FR-7: Publish normalized Connection IR

Compiler publishes Connection IR for every accepted Engineering Connection and Engineering Net.

**Consequences:**
- IR retains stable connection/net identity, normalized endpoint roles, kind, resolved specifications,
  potential/signal, provenance, validation result, and source trace.
- Canonical ordering and serialization produce equal digest for equal inputs.
- Connection IR never contains Konva nodes, viewport pixels, DOM ids, or raw mouse state.

#### FR-8: Publish explicit topology operators

Compiler publishes stable branch, merge, pass-through, and interruption operators required by each
projection.

**Consequences:**
- T-nodes, angles, and jumpers are not modeled as engineering Symbols solely to control traversal.
- Projection ordering and preferred continuation are explicit data, never hidden renderer heuristics.
- Crossing without electrical connection remains distinct from a junction.

#### FR-9: Preserve one meaning across projections

One Engineering Connection or Engineering Net can produce multiple Connection Projections while
preserving semantic identity.

**Consequences:**
- Schematic route geometry cannot become wiring or 3D routing truth.
- Connection Navigator and source trace can resolve every projection to the same source meaning.

### 4.3 Deterministic Connection Planning

**Description:** Layout planning converts Connection IR, package-backed Port anchors, logical Sheet
coordinates, obstacles, and explicit route intent into readable Route Plans.

#### FR-10: Plan professional orthogonal routes

Athena produces deterministic orthogonal routes with minimal avoidable bends, crossings, detours, and
overlaps.

**Consequences:**
- Routes start and end at admitted package anchor geometry while preserving Engineering Port identity.
- Planner respects symbol bounds, sheet frame, rulers, declared keep-outs, and logical layout grid.
- Equal inputs produce byte-equal Route Plans regardless of viewport size or zoom.

#### FR-11: Plan junctions, crossings, and shared trunks

Athena distinguishes connected junctions, unconnected crossings, shared trunks, bridges, and interrupted
continuations.

**Consequences:**
- Junction markers appear only for electrically joined topology.
- Crossings never imply connectivity; any bridge/gap follows one deterministic projection rule.
- Shared Net segments use stable branch ordering and avoid duplicate coincident paint.

#### FR-12: Place compact connection annotations

Athena places only explicitly selected engineering annotations at stable collision-aware anchors.

**Consequences:**
- Default document never paints AST links, source paths, internal ids, debug labels, or every Port name.
- Labels remain compact, regular-weight, non-overlapping, and readable at desktop and narrow editor sizes.

### 4.4 Connection Authoring Workspace

**Description:** Theia exposes connection inspection and typed edit intent while Canonical Scene and
server transactions remain authoritative.

#### FR-13: Inspect connections independently of graphics

Connection Navigator lists placed and unplaced Engineering Connections and Engineering Nets with kind,
endpoints, roles, potential/signal, resolved specifications, validation, and source trace.

**Consequences:**
- Selecting navigator entry selects all relevant projections without changing style or label visibility.
- Selecting a route resolves semantic selection but does not expose interaction metadata as canvas text.

#### FR-14: Connect and reconnect through typed transactions

Engineers can initiate connect/reconnect from package-backed Port anchors. Realizes UJ-1 and UJ-3.

**Consequences:**
- UI sends candidate endpoint identities, intent, operation id, and full Source Revision.
- Server validates, mutates source, recompiles Connection IR and Scene, journals, then publishes.
- Failed or stale operations retain the previous accepted source and scene.

#### FR-15: Edit route presentation without semantic mutation

Engineers can move a route segment or bend as a Presentation Edit Operation.

**Consequences:**
- Persisted route intent uses logical Sheet coordinates or constraints, never raw viewport pixels.
- Route edits cannot reconnect Ports, change Net membership, or alter Physical Connection Requirements.
- Undo/Redo reverses accepted source transactions, not pointer events.

### 4.5 Golden Engineering Document

**Description:** M46 proves complete semantics, IR, planning, editing, and render quality on the rolling-
shutter example. Realizes UJ-4.

#### FR-16: Publish professional rolling-shutter connection proof

`examples/m46/rolling-shutter` must use M45-style direct local packages and render a complete connected
IEC-style Folio. The Folio contains a rolling-shutter power page and a CPU/control connection page. Both
pages project one engineering source and one Connection IR, while their symbols/elements/parts may resolve
from different project-imported packages.

**Consequences:**
- The Folio contains a power page and a CPU/control page. Each Page Companion owns its own logical frame,
  ruler, placement, route plan, and Scene page; neither duplicates engineering Connections or Nets.
- Packages are project dependencies, never Folio or Page owners. One page may render occurrences bound to
  different vendor, IEC, or community packages through the project's resolved bindings.
- Page content together covers power and control connections, at least one multi-endpoint Net, jumper or
  shared trunk, unconnected crossing, junction, potential/signal, and typed physical requirement.
- A connection projected on more than one page retains one Connection IR identity. M46 renders the stable
  interruption/reference fact; cross-page smart-connect authoring remains later scope.
- Canvas matches repository Golden Rule: one-pixel square frame, narrow flush rulers, blank white interior,
  thin screen-space linework, tiny points, compact labels, no bottom table, no default grid.
- Reopen preserves connection/net/operator identities and deterministic SVG/PNG digests.

## 5. Cross-Cutting Non-Functional Requirements

- **NFR-1 Determinism:** equal source, packages, Sheet, style, and compiler profile produce equal Connection
  IR, Route Plan, Scene, SVG, and pinned PNG digests.
- **NFR-2 Fail closed:** invalid semantic or graphic planning state publishes no partial replacement.
- **NFR-3 Screen-space visual weight:** route width, junction size, bridge size, arrow/marker size, and text
  stroke do not scale with scene zoom.
- **NFR-4 Interaction performance:** on the pinned M46 proof workstation, a 1,000-connection checked
  profile sustains p95 frame time at or below 22 ms during pan/zoom, completes selection at or below
  100 ms p95, and replans one edited route at or below 250 ms p95 without repainting unrelated routes.
- **NFR-5 Renderer independence:** Connection IR and Route Plan contain no Konva or DOM contract.
- **NFR-6 Source-set hygiene:** no M46/demo/proof/sample class enters production `src/main`; no compatibility
  shim, deprecated route model, or dual path remains.
- **NFR-7 Evidence:** rebuilt kernel, LSP, frontend, and Electron product proof open the M46 workspace root,
  verify Repository Session `READY` and exact LSP root, then capture desktop and narrow screenshots.

## 6. Non-Goals

- Full EPLAN file, database, navigator, or property-inheritance compatibility.
- Graphic-first extraction where lines create Engineering Connections.
- Complete cable/harness fabrication, terminal plans, wire numbering, BOM, reports, or 3D routing.
- Universal fluid/process connection behavior.
- Remote package registry or AI Engineering Pattern generation.
- New renderer, WebGPU rewrite, or CAD-scale 100k connection claim.
- Final automatic layout of arbitrary industrial projects without authored placement intent.

## 7. MVP Scope

### 7.1 In Scope

- Electrical Engineering Connection and Engineering Net source model.
- Endpoint roles, Connection Kinds, physical requirements, deterministic specification precedence.
- Canonical Connection IR and topology operators.
- Deterministic orthogonal route planning, junction/crossing/shared-trunk rules, compact annotations.
- Connection Navigator, typed connect/reconnect, route presentation edits, Undo/Redo integration.
- M46 rolling-shutter product proof and deterministic export.

### 7.2 Out of Scope

- Later manufacturing, cable, terminal, and 3D projections.
- Cross-page smart-connect authoring UX beyond the stable multi-page projection identity and interruption
  reference contract.
- Automatic engineering solution selection or standards certification.

## 8. Success Metrics

- **SM-1:** 100% of visible M46 routes trace to one Engineering Connection or Engineering Net through
  Connection IR; validates FR-1, FR-2, FR-7, FR-9.
- **SM-2:** Golden project and contract tests prove all six M46 electrical Connection Kinds, including
  fail-closed incomplete/unsupported cases; validates FR-4, FR-16.
- **SM-3:** Golden project proves junction, unconnected crossing, shared trunk, interruption, and compact
  annotation without hidden debug text; validates FR-8, FR-11, FR-12, FR-16.
- **SM-4:** Repeated compile/export digests match and reopen preserves identities; validates FR-7, FR-10,
  FR-16 and NFR-1.
- **SM-5:** Valid reconnect, rejected reconnect, route edit, Undo, and Redo all preserve authority
  boundaries and journal evidence; validates FR-6, FR-14, FR-15.
- **SM-6:** Checked 1,000-connection profile meets recorded interaction and incremental replan budgets;
  validates NFR-4.
- **SM-C1:** Do not reduce line crossings by inventing engineering connectivity or changing authored
  placements; counterbalances SM-3.
- **SM-C2:** Do not improve visual similarity by painting source/debug metadata or hardcoding screenshot
  geometry outside source/package/Sheet/style authority; counterbalances SM-4.

## 9. Open Questions

None block M46. Later milestones decide cross-page interruption UX, cable/harness manufacturing models,
and full multi-domain Connection Kind extensibility.

## 10. Assumptions Index

No unresolved assumptions. User supplied M46 semantic-connection requirement, Connection IR requirement,
EPLAN reference corpus, golden image, and authorization to use best-practice decisions through the BMad
workflow.
