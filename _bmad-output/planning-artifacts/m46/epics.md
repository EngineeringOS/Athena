---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md
  - draft/20260803-confuse/connections-details.md
  - draft/eplan-help/index/connectionbrowsergui.md
  - AGENTS.md
---

# Athena - M46 Epic Breakdown

## Overview

This document decomposes M46 semantic connectivity, Connection IR, deterministic planning, professional
rendering, editing, product proof, and closure into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Author stable typed Engineering Connections between Engineering Ports.
FR2: Author first-class multi-endpoint Engineering Nets without pairwise flattening.
FR3: Validate source/sink/pass endpoint role independently from Port direction.
FR4: Support CONDUCTOR, WIRE, CABLE_CORE, JUMPER, BUSBAR, and SIGNAL Connection Kinds.
FR5: Resolve typed Connection Specifications through project, potential/signal, Net, and Connection precedence.
FR6: Validate Port, role, domain, flow/signal, potential, and physical compatibility before publication.
FR7: Publish deterministic normalized Connection IR with identity, facts, validation, provenance, and trace.
FR8: Publish explicit stable branch, merge, pass-through, and interruption Topology Operators.
FR9: Preserve one Engineering Connection/Net identity across multiple projections.
FR10: Plan deterministic professional orthogonal routes from admitted anchors and spatial constraints.
FR11: Plan exact junction, unconnected crossing, shared trunk, bridge/gap, and interruption geometry.
FR12: Place only explicit compact collision-aware Connection Annotations.
FR13: Inspect placed/unplaced Connection/Net facts through Connection Navigator independent from graphics.
FR14: Connect and reconnect through validated typed source transactions.
FR15: Edit route presentation through logical Sheet constraints without semantic mutation.
FR16: Publish and prove a professional M46 rolling-shutter engineering document.

### NonFunctional Requirements

NFR1: Equal accepted inputs produce equal Connection IR, Route Plan, Scene, SVG, and pinned PNG digests.
NFR2: Invalid semantic or planning state fails closed and retains previous accepted publication.
NFR3: Route, junction, bridge, marker, and label visual weight remains constant in screen space.
NFR4: Pinned 1,000-connection profile meets 50 ms pan/zoom operation, 100 ms selection, and 250 ms local replan p95 budgets on the supported Electron/Windows proof host.
NFR5: Connection IR and Route Plan contain no Konva or DOM contract.
NFR6: Production has no compatibility shim, deprecated route model, dual path, milestone/demo/proof/sample class, or stale test.
NFR7: Closure rebuilds kernel/LSP/frontend/Electron, verifies workspace root and READY state, captures screenshots/exports, and runs audits.

### Additional Requirements

- Add first-class EngineeringConnection and EngineeringNet collections to EngineeringDocument.
- Keep generic EngineeringRelationship for non-connectivity but stop projecting it as route truth.
- Add `kernel/connection-model` with `athena-connection-ir-c14n-v1` canonical digest.
- Replace ProjectionConnection with ConnectionProjection.
- Replace SpatialRoute/SpatialRouteCompiler with ConnectionRoutePlan/ConnectionRoutePlanner.
- Replace SceneRoute with SceneConnection and increment Canonical Scene schema once.
- Use explicit specification precedence: project < potential/signal < Net < Connection.
- Keep topology operators derived and unable to change Net membership.
- Use deterministic lexicographic route cost and stable tie-breaking.
- Paint shared segments once and infer no junction/crossing semantics in renderer.
- Keep hit geometry invisible and paint sizes fixed in screen space.
- Classify Engineering versus Presentation edit operations and reuse full Source Revision transactions/journal.
- Publish immutable ConnectionReadModel from accepted Connection IR.
- Invalidate only affected topology groups/local collisions for one route edit.
- Use current Kotlin/Theia/Konva stack; ELK is reference only, not runtime authority.
- Delete legacy route contracts and tests; no compatibility path.
- Create independent `examples/m46/rolling-shutter` and M46-only evidence folder.

### UX Design Requirements

UX-DR1: Canvas uses one-pixel square frame, narrow flush top/left rulers, blank white interior, no default grid, and no bottom table.
UX-DR2: Routes use thin regular black orthogonal linework, tiny junctions, compact labels, and no oversized arrows or persistent Port rings.
UX-DR3: Default canvas hides source paths, AST links, internal ids, debug labels, and unselected Port names.
UX-DR4: Route/Connection selection resolves semantic trace without changing style target or label visibility.
UX-DR5: Candidate reconnect gives visible valid/invalid feedback while accepted scene remains unchanged on rejection.
UX-DR6: Desktop and narrow editor layouts preserve canvas readability, rulers, labels, and non-overlap.

### FR Coverage Map

FR1: Epic 1 - author stable typed Engineering Connections.
FR2: Epic 1 - author first-class multi-endpoint Engineering Nets.
FR3: Epic 1 - separate endpoint role from Port direction.
FR4: Epic 1 - classify six M46 electrical Connection Kinds.
FR5: Epic 1 - resolve typed scoped Connection Specifications.
FR6: Epic 1 - validate connectivity before publication.
FR7: Epic 1 - publish canonical Connection IR.
FR8: Epic 1 - publish explicit Topology Operators.
FR9: Epic 1 - preserve identity across projections.
FR10: Epic 2 - plan deterministic professional orthogonal routes.
FR11: Epic 2 - plan exact junction/crossing/shared/interruption geometry.
FR12: Epic 2 - place explicit compact Connection Annotations.
FR13: Epic 3 - inspect placed/unplaced connectivity independently of graphics.
FR14: Epic 3 - connect/reconnect through typed transactions.
FR15: Epic 3 - edit route presentation without semantic mutation.
FR16: Epic 4 - publish professional rolling-shutter proof.

## Epic List

### Epic 1: Author Trusted Engineering Connectivity

Engineers can author, validate, compile, and inspect stable Engineering Connections and Engineering Nets
whose meaning survives placement and projection changes.

**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR7, FR8, FR9.

### Epic 2: Produce Professional Connection Documents

Engineers receive deterministic readable orthogonal connection plans with exact electrical topology and
compact annotations from accepted Connection IR.

**FRs covered:** FR10, FR11, FR12.

### Epic 3: Inspect And Edit Connections In Athena Workspace

Engineers can navigate, select, connect, reconnect, and adjust route presentation through authority-safe
Theia interactions and server transactions.

**FRs covered:** FR13, FR14, FR15.

### Epic 4: Prove The Professional Rolling-Shutter Workflow

Engineers can open, edit, reopen, benchmark, and export a package-backed M46 rolling-shutter document that
matches Athena's IEC visual grammar.

**FRs covered:** FR16 plus all cross-cutting NFR/UX proof.

### Epic 5: Close M46 On Verified Product Evidence

Maintainers can trust M46 closure because full sequential verification, hygiene, artifact reconciliation,
and retrospective are complete.

**FRs covered:** closure coverage for FR1-FR16 and NFR1-NFR7.

## Epic 1: Author Trusted Engineering Connectivity

Engineers can author, validate, compile, and inspect stable Engineering Connections and Engineering Nets
whose meaning survives placement and projection changes.

### Story 1.1: Author Typed Binary Engineering Connections

As an engineer,
I want concise source syntax for typed binary connections,
So that connectivity exists as engineering meaning before any line is drawn.

**Requirements:** FR1, FR3, FR6, NFR2, NFR6.

**Acceptance Criteria:**

**Given** two resolved Engineering Ports and a supported binary connection phrase
**When** Athena parses and lowers the source
**Then** EngineeringDocument contains one stable EngineeringConnection with two typed Endpoints, explicit SOURCE/SINK/PASS roles, Connection Kind, properties, and provenance
**And** Port direction remains separate from Endpoint role.

**Given** graphic proximity, an SVG anchor, or an arbitrary EngineeringRelationship without explicit connection meaning
**When** projection compiles
**Then** no EngineeringConnection is created
**And** retired generic relationship-to-route tests and production paths are removed.

**Given** malformed or unresolved source
**When** diagnostics publish through LSP
**Then** diagnostic names exact Port/connection and correction in human language
**And** Tree-sitter highlighting, outline, formatting, and document symbols recognize current syntax.

### Story 1.2: Author Engineering Nets And Connection Specifications

As an engineer,
I want first-class multi-endpoint Nets and scoped connection facts,
So that shared electrical equivalence and physical requirements are explicit.

**Requirements:** FR2, FR4, FR5, NFR2.

**Acceptance Criteria:**

**Given** a Net with two or more Ports
**When** source lowers
**Then** one EngineeringNet retains stable identity, Endpoint membership/roles, potential or signal, properties, and provenance
**And** compiler does not flatten it into arbitrary binary EngineeringConnections.

**Given** project, potential/signal, Net, and Connection specifications
**When** values resolve
**Then** precedence is project < potential/signal < Net < Connection
**And** same-scope contradictions identify both authored locations and block publication.

**Given** CONDUCTOR, WIRE, CABLE_CORE, JUMPER, BUSBAR, and SIGNAL source cases
**When** model contracts validate
**Then** all six kinds enforce their required/permitted typed facts
**And** incomplete or unsupported kind data fails closed.

### Story 1.3: Validate And Compile Canonical Connection IR

As an engineer,
I want accepted connectivity compiled into one canonical Connection IR,
So that every downstream view uses the same validated facts.

**Requirements:** FR6, FR7, FR8, NFR1, NFR2, NFR5.

**Acceptance Criteria:**

**Given** valid Connections/Nets and Port anatomy
**When** ConnectionIrCompiler runs
**Then** it resolves, validates, normalizes, derives topology, canonicalizes, and publishes ConnectionDocument from new `kernel/connection-model`
**And** canonical digest uses `athena-connection-ir-c14n-v1` and is equal for equal inputs.

**Given** invalid direction/role, domain, potential/signal, physical requirement, missing Endpoint, or topology
**When** compile runs
**Then** no replacement Connection IR publishes
**And** previous accepted publication remains under existing STALE/UNAVAILABLE rules.

**Given** a Net requiring branch, merge, pass-through, or interruption topology
**When** IR publishes
**Then** explicit stable Topology Operators preserve order and trace
**And** no operator can alter Net membership or own route geometry.

### Story 1.4: Publish Connection Projections And Remove Generic Route Truth

As an engineer,
I want each view to project accepted Connection IR explicitly,
So that one connection meaning can support many documents without identity drift.

**Requirements:** FR9, NFR5, NFR6.

**Acceptance Criteria:**

**Given** accepted Connection IR and one schematic view
**When** projection compiles
**Then** ConnectionProjection references one connection/net identity, selected occurrence Ports, role, logical route constraints, and source trace
**And** it contains no physical coordinates.

**Given** two projections of one connection/net
**When** both compile
**Then** both resolve to the same Engineering identity and Connection IR fact
**And** geometry/style differences cannot mutate semantic meaning.

**Given** current production source
**When** this story completes
**Then** ProjectionConnection and direct generic Relationship route lowering are deleted
**And** no adapter, deprecated alias, fallback parser, or dual projection path remains.

## Epic 2: Produce Professional Connection Documents

Engineers receive deterministic readable orthogonal connection plans with exact electrical topology and
compact annotations from accepted Connection IR.

### Story 2.1: Plan Deterministic Orthogonal Connection Routes

As an engineer,
I want clean deterministic routes around real symbols,
So that generated documents are readable without manual repair.

**Requirements:** FR10, NFR1, NFR2, NFR6.

**Acceptance Criteria:**

**Given** ConnectionProjection, admitted Port anchors, drawing bounds, frame/rulers, occurrence bounds, and keep-outs
**When** ConnectionRoutePlanner runs
**Then** ConnectionRoutePlan contains typed orthogonal segments, endpoint anchors, quality metrics, and trace
**And** every segment stays inside drawing area and outside hard obstacle interiors.

**Given** multiple valid plans
**When** planner selects one
**Then** it compares semantic validity, obstacle violations, ambiguous overlap, crossings, bends, Manhattan length, and stable geometry identity in fixed lexicographic order
**And** repeated runs produce equal plan bytes.

**Given** replacement implementation is accepted
**When** source hygiene runs
**Then** SpatialRoute and SpatialRouteCompiler are absent
**And** no first-valid or giant-detour compatibility logic remains.

### Story 2.2: Plan Junctions Crossings Shared Trunks And Interruptions

As an engineer,
I want route topology to display exact electrical meaning,
So that a reviewer never confuses crossing conductors with connected conductors.

**Requirements:** FR11, UX-DR2.

**Acceptance Criteria:**

**Given** a joined Net branch
**When** planning runs
**Then** one stable tiny-junction fact is emitted at joined topology
**And** shared trunk segments are canonicalized and painted once.

**Given** two unconnected routes cross
**When** planning runs
**Then** one stable Crossing fact is emitted without a Junction
**And** deterministic bridge/gap ownership is explicit data.

**Given** an interrupted continuation
**When** planning runs
**Then** paired stable interruption anchors retain one semantic connection/net trace
**And** renderer does not infer continuation from geometry.

### Story 2.3: Place Compact Connection Annotations And Validate Quality

As an engineer,
I want selected connection facts placed without clutter,
So that the sheet communicates engineering information clearly.

**Requirements:** FR12, UX-DR3, UX-DR6.

**Acceptance Criteria:**

**Given** explicit annotation selections
**When** annotation planning runs
**Then** compact anchors avoid symbol/route/label collisions and retain subject/style/trace
**And** regular-weight text stays inside drawing bounds.

**Given** no annotation selection
**When** Scene compiles
**Then** no source path, AST link, internal id, anchor id, debug label, or automatic Port-name flood is emitted.

**Given** route quality violations or impossible label placement
**When** spatial validation runs
**Then** exact subject/problem/correction diagnostics block new publication
**And** tests cover crossing, bend, overlap, detour, label collision, and deterministic tie boundaries.

## Epic 3: Inspect And Edit Connections In Athena Workspace

Engineers can navigate, select, connect, reconnect, and adjust route presentation through authority-safe
Theia interactions and server transactions.

### Story 3.1: Publish SceneConnection And Deterministic SVG

As an engineer,
I want canonical presentation to retain connection topology,
So that every renderer paints the same professional meaning.

**Requirements:** FR9, FR11, FR12, NFR1, NFR3, NFR5, NFR6.

**Acceptance Criteria:**

**Given** accepted ConnectionRoutePlans
**When** Scene compiles
**Then** SceneConnection contains semantic identity, endpoints, typed paths, Junction/Crossing/Interruption markers, annotations, z-order, style ids, and trace
**And** Scene schema increments once with generated JSON/TypeScript contracts.

**Given** SceneConnection input
**When** SVG renderer exports
**Then** linework, markers, and labels follow screen/print-safe professional proportions and exact topology
**And** repeated SVG bytes/digest are equal.

**Given** replacement completion
**When** hygiene scans source/tests
**Then** SceneRoute and legacy route schema fields are deleted without compatibility aliases.

### Story 3.2: Paint And Select Professional Connections In Konva

As an engineer,
I want thin selectable connections with invisible generous hit targets,
So that editing is precise without polluting printed output.

**Requirements:** FR12, NFR3, UX-DR1, UX-DR2, UX-DR3, UX-DR4, UX-DR6.

**Acceptance Criteria:**

**Given** SceneConnection publication
**When** Konva paints at any zoom
**Then** routes stay approximately one screen pixel, Junction/bridge/marker sizes remain constant, and hidden hit geometry does not print
**And** no oversized arrows, thick black routes, or persistent Port rings appear.

**Given** route selection
**When** engineer clicks visible line or hit target
**Then** selection resolves semantic connection/net identity and trace
**And** selection does not change style target, annotation visibility, or emit internal text.

**Given** desktop and narrow center editor sizes
**When** canvas fits/resizes/pans/zooms
**Then** edge rulers remain aligned and drawing remains unclipped, untwisted, and non-overlapping.

### Story 3.3: Navigate Placed And Unplaced Connectivity

As an engineer,
I want a Connection Navigator independent from the drawing,
So that I can inspect and locate all connectivity, including unplaced facts.

**Requirements:** FR13, UX-DR4.

**Acceptance Criteria:**

**Given** accepted Connection IR
**When** LSP publishes ConnectionReadModel
**Then** it lists Connections/Nets, placed/unplaced state, endpoints, roles, kind, potential/signal, resolved specifications, validation, and source traces
**And** caches are keyed by accepted Input Revision.

**Given** Navigator selection
**When** an item has projections
**Then** all matching projections can be located/selected through trace
**And** unplaced items remain inspectable without placeholder graphics.

**Given** source changes or stale revision
**When** read model refreshes
**Then** frontend discards stale cache and never fabricates connection state.

### Story 3.4: Connect Reconnect And Adjust Routes Through Transactions

As an engineer,
I want canvas connection edits to update the correct source authority,
So that visual editing and engineering truth remain synchronized.

**Requirements:** FR14, FR15, NFR2, UX-DR5.

**Acceptance Criteria:**

**Given** two compatible package-backed Port anchors
**When** engineer connects or reconnects them
**Then** frontend sends typed EngineeringEditOperation with Endpoint identities, intent, operation id, and full Source Revision
**And** server validates, stages source, compiles IR/projection/plan/Scene, journals, commits, then publishes.

**Given** incompatible or stale candidate
**When** operation runs
**Then** exact plain-language rejection appears
**And** source, journal, lock, IR, and Scene remain unchanged.

**Given** a route segment/bend adjustment
**When** engineer commits it
**Then** typed PresentationEditOperation persists logical Sheet constraint only
**And** it cannot reconnect Ports, change Net membership, kind, potential, or physical requirements.

**Given** accepted connect, reconnect, or route edit
**When** Undo/Redo runs
**Then** accepted source transaction reverses/reapplies atomically and recompiles
**And** pointer events never become history entries.

## Epic 4: Prove The Professional Rolling-Shutter Workflow

Engineers can open, edit, reopen, benchmark, and export a package-backed M46 rolling-shutter document that
matches Athena's IEC visual grammar.

### Story 4.1: Build M46 Rolling-Shutter Connection Project

As an engineer,
I want one complete realistic connection example,
So that semantic and visual behavior can be judged together.

**Requirements:** FR16, UX-DR1, UX-DR2, UX-DR3, UX-DR6.

**Acceptance Criteria:**

**Given** independent `examples/m46/rolling-shutter`
**When** repository compiles
**Then** direct local packages, Lock V3, source, bindings, Sheet, and style publish READY without reading prior milestone examples
**And** source proves all six Connection Kinds, one multi-endpoint Net, potentials/signals, physical requirements, and placed/unplaced connectivity.

**Given** compiled document
**When** Scene renders
**Then** supply, breaker, contactors, overload, motor, PLC, sensors, terminals, coils, auxiliary contacts, and lamps use package-backed geometry
**And** junction, unconnected crossing, shared trunk, interruption, and explicit compact annotation are visible.

**Given** visual comparison
**When** compared with canonical screenshot
**Then** page follows one-pixel frame, narrow rulers, blank white interior, thin linework, tiny points, compact labels, disciplined spacing, and no bottom table/default grid.

### Story 4.4: Project A Rolling-Shutter Folio Across Pages

As an engineer,
I want one engineering Folio with distinct power and CPU/control pages,
So that each page stays readable while all connectivity retains one semantic identity.

**Requirements:** FR9, FR10, FR11, FR16, NFR1, NFR2, NFR5, UX-DR1 through UX-DR6.

**Acceptance Criteria:**

**Given** one same-root Folio Companion with independent Page Companions
**When** it declares `power` and `control-cpu` pages in stable order
**Then** compiler publishes two ordered Projection Sheets, Route Plans, and Scene pages with independent
logical frame/ruler/placement/route ownership
**And** the source grammar exposes no renderer coordinates or duplicated engineering Connection/Net facts.

**Given** a Page projects occurrences bound to different admitted packages
**When** its Function Representation Bindings resolve through the project manifest and lock
**Then** the Page renders the resolved package assets without owning, copying, or re-declaring the package
**And** package identity has no effect on Folio/Page ordering or Connection identity.

**Given** one Connection or Net is projected on both pages
**When** both pages compile and render
**Then** every projection resolves the same Connection IR identity
**And** cross-page continuation uses explicit interruption/reference facts rather than inferred geometry or
Smart Connect authoring behavior.

**Given** the M46 rolling-shutter example opens in Theia
**When** an engineer switches page
**Then** power and CPU/control pages each render as a clean professional IEC sheet with the canonical frame,
rulers, white interior, thin linework, compact labels, consistent package-asset scale, and no debug text.

### Story 4.2: Prove Incremental Connection Performance

As an engineer,
I want large connection documents to remain responsive,
So that professional planning does not make the IDE unusable.

**Requirements:** NFR4, NFR5.

**Acceptance Criteria:**

**Given** pinned 1,000-connection checked profile
**When** benchmark runs in rebuilt product
**Then** p95 pan/zoom frame <=22 ms, selection <=100 ms, and one-route local replan <=250 ms
**And** identity errors, trace errors, and unhandled errors equal zero.

**Given** one route edit
**When** invalidation is measured
**Then** only affected topology group and local collisions replan/repaint
**And** evidence records paint counts, changed ids, durations, environment, and thresholds without hardcoded PASS.

### Story 4.3: Prove Edit Reopen And Deterministic Export

As an engineer,
I want rebuilt product evidence for the complete authoring loop,
So that M46 is proven in the real IDE rather than fixtures alone.

**Requirements:** FR13, FR14, FR15, FR16, NFR1, NFR2, NFR3, NFR7, UX-DR1 through UX-DR6.

**Acceptance Criteria:**

**Given** Electron/Theia launch
**When** product proof starts
**Then** M46 example root opens first, Explorer shows repository, Repository Session is READY, and LSP root equals example root before canvas assertions.

**Given** valid reconnect, rejected reconnect, route edit, Undo, Redo, and restart
**When** proof runs
**Then** accepted/rejected source and Scene states, stable identities, traces, journal, and reopen evidence match expected results.

**Given** repeated export and desktop/narrow capture
**When** proof completes
**Then** SVG/PNG digests are deterministic and screenshots live under M46 artifacts
**And** screenshots pass explicit visual checks for frame/rulers/line weight/junctions/labels/no debug text/no overlap.

## Epic 5: Close M46 On Verified Product Evidence

Maintainers can trust M46 closure because full sequential verification, hygiene, artifact reconciliation,
and retrospective are complete.

### Story 5.1: Run Full M46 Verification And Hygiene

As a maintainer,
I want one fresh sequential verification record,
So that no stale build or retired route architecture can hide failure.

**Requirements:** FR1 through FR16, NFR1 through NFR7, UX-DR1 through UX-DR6.

**Acceptance Criteria:**

**Given** all M46 implementation stories
**When** verification runs
**Then** affected Gradle module tests run sequentially, LSP tests rerun, frontend contracts/tests/build pass, and product E2E/export/performance proof passes
**And** no concurrent Gradle command is used.

**Given** source and artifacts
**When** audits run
**Then** encoding audit, source-set hygiene, deprecated/compatibility/old route/milestone production name scans pass
**And** acceptance audit maps every FR/NFR/UX requirement to fresh evidence.

### Story 5.2: Publish M46 Closure And Retrospective

As a team,
I want closure and lessons recorded from verified evidence,
So that M47 starts from current connection truth.

**Requirements:** closure trace for FR1 through FR16 and NFR1 through NFR7.

**Acceptance Criteria:**

**Given** Story 5.1 passing evidence
**When** closure publishes
**Then** closure maps every story, FR, NFR, product artifact, digest, screenshot, and residual risk
**And** sprint status marks stories/epics done only after verified evidence.

**Given** implementation history and failures
**When** retrospective publishes
**Then** it records what worked, what failed first, corrections, usage, architectural laws, and M47 carry-forward actions
**And** no unresolved critical/high finding remains.
