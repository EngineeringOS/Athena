---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md
  - docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md
  - _bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md
---

# Athena - Epic Breakdown

## Overview

M41 is a brownfield recovery milestone. Five value epics move from coherent Spatial composition to
exact connections, trustworthy contracts, truthful measurement, and product proof. All stories are
recreated from the replacement PRD and architecture in numeric order.

## Requirements Inventory

### Functional Requirements

- **FR-1:** Compile one positive-size, in-area Occurrence rectangle per projected Occurrence on one
  owning Sheet; deterministic and explainable.
- **FR-2:** Compose authored Regions as three ordered two-dimensional columns with spacing,
  separation, and explicit unassigned-Occurrence handling.
- **FR-3:** Derive padded union Region bounds, Construct envelopes, membership, containment, and
  alignment from actual rectangles.
- **FR-4:** Derive typed center-based Grid References from each owning Sheet grid for Occurrences
  and Constructs.
- **FR-5:** Resolve each referenced occurrence-port exactly once to a stable boundary Anchor.
- **FR-6:** Emit one exact-endpoint, positive-segment, orthogonal, obstacle-safe Route per visible
  Connection, or fail completely.
- **FR-7:** Publish complete Connection, occurrence-port-Anchor, Lane, Sheet, and Source Trace for
  every Route; no fallback or silent loss.
- **FR-8:** Publish deterministic basic Lane facts without professional optimization claims.
- **FR-9:** Publish typed per-Sheet facts with stable identity, ownership, Source Trace, and exact
  Golden Fixture coverage.
- **FR-10:** Validate complete Spatial documents and return all actionable diagnostics before
  Presentation; no partial output.
- **FR-11:** Preserve Spatial coordinates and ordered Route points through Presentation and runtime
  proof without renderer repair.
- **FR-12:** Compute exact per-Sheet geometry metrics from validated facts, including union Occupancy,
  and publish no M41 label metrics.
- **FR-13:** Generate and verify a reproducible structured Golden Fixture baseline and comparable M40
  measurements only where units and methods match.

### NonFunctional Requirements

- **NFR-1:** Deterministic canonical facts across repeated compilation and unordered input.
- **NFR-2:** Human-first diagnostics with exact subject, problem, correction, and Source Trace.
- **NFR-3:** Pre-1.0 clean architecture: no compatibility shims, stale alternatives, milestone
  production types, or proof/demo/sample classes in `src/main`.
- **NFR-4:** Small typed, deterministic, inspectable, implementation-neutral Spatial contract.
- **NFR-5:** Athena source remains geometry-free and authoritative; XML is not runtime authority.

### Additional Requirements

- Single `ProjectionSpatialCompiler` orchestration entry; stale bridges, row compiler, and reporter
  are deleted after caller migration.
- Reuse current layout and `AthenaRouteEngine` geometry core behind domain-neutral internal
  adapters; solver/electrical/label fields do not enter Spatial models.
- Public Spatial geometry uses integer drawing units and typed facts grouped by role in Kotlin files.
- Sheet policy: extent `1200 x 800`, Drawing Area `(40,60,1120,640)`, title block at `y=740`, Region
  gutter 32, Occurrence separation 48, grouping padding 24.
- Complete validation runs before Presentation and returns `RealityTransformationResult.Failure`
  with no partial document.
- Product proof rebuilds kernel, LSP, frontend, and Electron surfaces; runtime payload and Drawing
  Area pixels prove geometry at desktop and narrow viewports.
- Gradle commands run sequentially on Windows. Encoding and source-set hygiene audits run after
  relevant changes.

### UX Design Requirements

None. M41 is an internal compiler capability. M42 owns Presentation styling, labels, and visibility.

### FR Coverage Map

- FR-1: Epic 1 - deterministic in-sheet Occurrence placement.
- FR-2: Epic 1 - Region-driven two-dimensional composition.
- FR-3: Epic 1 - Region/Construct geometry and alignment.
- FR-4: Epic 1 - owning-Sheet Grid References.
- FR-5: Epic 2 - exact port Anchors.
- FR-6: Epic 2 - obstacle-safe orthogonal Routes.
- FR-7: Epic 2 - complete Route trace and fail-closed endpoint handling.
- FR-8: Epic 2 - deterministic basic Lanes and M45 boundary.
- FR-9: Epic 3 - typed per-Sheet Spatial facts.
- FR-10: Epic 3 - complete validation and diagnostics.
- FR-11: Epic 3 - immutable Presentation/runtime coordinate authority.
- FR-12: Epic 4 - exact per-Sheet quality metrics.
- FR-13: Epic 4 - structured Golden Fixture baseline.
- GATE-1..GATE-7: Epic 5 - fixture, runtime proof, screenshots, and evidence-backed closure.

## Epic List

### Epic 1: Coherent 2D Spatial Composition

Engineer sees every projected subject placed inside its Sheet, grouped by ordered Regions and
Constructs, with meaningful horizontal and vertical distribution and owning-Sheet grid facts.
**FRs covered:** FR-1, FR-2, FR-3, FR-4

### Epic 2: Exact Basic Connections

Engineer sees every visible Connection start/end at its real port Anchors and route around bodies
with deterministic orthogonal segments and stable Lanes; incomplete endpoints fail clearly.
**FRs covered:** FR-5, FR-6, FR-7, FR-8

### Epic 3: Trustworthy Spatial Contract

Compiler publishes inspectable per-Sheet typed facts and blocks Presentation when identity,
ownership, geometry, trace, coverage, or authority invariants fail.
**FRs covered:** FR-9, FR-10, FR-11

### Epic 4: Truthful Spatial Measurement

Engineer and downstream milestones receive exact per-Sheet geometry metrics and a generated baseline
that cannot pass through copied constants or label proxies.
**FRs covered:** FR-12, FR-13

### Epic 5: M41 Product Proof And Closure

The dedicated M41 example compiles through the real product pipeline; rebuilt desktop/narrow proof
shows the geometry; closure records only verified evidence and deferred roadmap work.
**Gates covered:** GATE-1 through GATE-7

## Epic 1: Coherent 2D Spatial Composition

The engineer can inspect a useful Spatial composition before routing or quality analysis exists.

### Story 1.1: Derive Deterministic Explainable Placement

As an engineer,
I want every projected Occurrence placed deterministically in a meaningful two-dimensional Sheet,
so that Region grouping is visible and repeatable without authored coordinates.

**Acceptance Criteria:**

**Given** a canonical M41 Projection Snapshot with three Regions
**When** Spatial placement compiles it twice and with unordered inputs permuted
**Then** all Occurrence facts are equal across runs and permutations
**And** each fact has one owning Sheet, positive in-area rectangle, stable identity, and reason trace (FR-1.1, FR-1.3, FR-1.4, FR-1.5)

**Given** the rolling-shutter Golden Fixture
**When** placement completes
**Then** Sheet extent is `1200 x 800`, Drawing Area is `(40,60,1120,640)`, and no rectangle enters `y >= 740`
**And** the three Region X ranges are ordered and separated by at least 32 units
**And** occupied Occurrence range is at least 55% wide and 45% high (FR-1.1, FR-2.1, FR-2.4)

**Given** an Occurrence without Region membership
**When** placement compiles
**Then** it is placed in one explicit final Region using stable order
**And** no Occurrence is silently discarded (FR-2.2, FR-2.5)

**Given** a regression fixture that would place all Occurrences in one row
**When** the behavioral test runs
**Then** the test fails before implementation and passes only when distinct X columns and meaningful Y range are produced (FR-2.1, FR-2.3)

### Story 1.2: Derive Bounds And Alignment

As an engineer,
I want grouping geometry derived from actual Occurrence rectangles,
so that Regions and Constructs truthfully contain what they represent.

**Acceptance Criteria:**

**Given** a Region with multiple placed rectangles of different positions and sizes
**When** Region geometry compiles
**Then** exactly one typed Region fact exists with exact membership and a 24-unit padded union bound
**And** the bound is positive and inside the Drawing Area (FR-3.1, FR-3.5, FR-3.6)

**Given** a Construct with offset members of different sizes
**When** Construct geometry compiles
**Then** exactly one typed Construct fact exists with ordered membership and a 24-unit padded union envelope
**And** every member rectangle is fully contained
**And** maximum-member width/height alone cannot satisfy the test (FR-3.2, FR-3.3)

**Given** alignment constraints from Region and Construct membership
**When** alignment facts compile
**Then** every alignment reference resolves to an existing same-Sheet Occurrence and records the constraint source (FR-3.4)

**Given** duplicate, unknown, empty, or cross-Sheet membership
**When** validation runs
**Then** compilation fails with subject, problem, correction, and Source Trace (FR-3.5, FR-10.3)

### Story 1.3: Derive Grid Reference Facts

As an engineer,
I want grid cells derived from each fact's own Sheet geometry,
so that later export can use stable positions without recomputing layout.

**Acceptance Criteria:**

**Given** two Sheets with different valid grids and Drawing Areas
**When** Occurrence centers and Construct envelope centers compile
**Then** each maps to exactly one cell using only its owning Sheet grid
**And** Sheet A facts cannot change when Sheet B grid changes (FR-4.1, FR-4.4)

**Given** a center on a Drawing Area boundary or interior
**When** grid mapping compiles
**Then** the cell is deterministic and uses the documented row/column bounds (FR-4.1)

**Given** missing/invalid grid dimensions or an out-of-area center
**When** grid mapping runs
**Then** complete compilation fails with an actionable Spatial Diagnostic (FR-4.3)

**Given** the Golden Fixture
**When** grid facts publish
**Then** coverage is one typed Grid Reference per Occurrence and Construct with no raw string map (FR-4.2, FR-9.2)

## Epic 2: Exact Basic Connections

The engineer can trust every visible Connection to attach to real ports and avoid non-endpoint
bodies without M45 optimization claims.

### Story 2.1: Build Anchor-Accurate Routes

As an engineer,
I want every referenced port represented by one stable boundary Anchor,
so that Route endpoints retain engineering identity exactly.

**Acceptance Criteria:**

**Given** an Occurrence with one or more referenced ports
**When** Anchor compilation runs repeatedly and with ports shuffled
**Then** each port resolves once to the same Sheet/Occurrence/port Anchor identity and point
**And** multiple ports on one edge receive distinct stable positions (FR-5.1, FR-5.2)

**Given** Connection direction and source/target port roles
**When** edge selection compiles
**Then** preferred boundary sides are selected without changing Connection meaning (FR-5.3)

**Given** a missing or duplicate referenced port
**When** Anchor compilation runs
**Then** it fails with exact subject, problem, correction, and Source Trace
**And** no approximate Anchor or partial Anchor list is published (FR-5.4, FR-10.4)

**Given** a valid Connection
**When** its Route is built from Anchors
**Then** Route first and final points equal Anchor points exactly without rounding (FR-6.2)

### Story 2.2: Build Orthogonal Route Facts And Lanes

As an engineer,
I want basic Routes to avoid non-endpoint Occurrence bodies,
so that the Control Drawing does not show impossible connections.

**Acceptance Criteria:**

**Given** a valid Drawing Area, endpoint Anchors, and a body between them
**When** obstacle-aware routing runs
**Then** every visible Connection produces exactly one Route with at least one positive-length segment
**And** every segment is horizontal or vertical and no segment enters a non-endpoint body interior (FR-6.1, FR-6.3, FR-6.4)

**Given** repeated compilation and permuted request/obstacle inputs
**When** routes and lanes publish
**Then** canonical Route points and stable Lane assignment are equal (FR-6.5, FR-8.1)

**Given** a third body that makes basic routing impossible
**When** route compilation runs
**Then** it returns a complete failure rather than a crossing, dropped Route, or degraded endpoint (FR-6.6)

**Given** a Connection whose endpoints are on different Sheets
**When** routing runs
**Then** it fails with a correction that multi-Sheet continuation is deferred to M45 (FR-6.7, FR-8.2)

### Story 2.3: Trace Routes And Hold The No-Optimization Boundary

As an engineer,
I want incomplete route trace to block compilation,
so that every visible Connection remains auditable to source and ports.

**Acceptance Criteria:**

**Given** one missing endpoint Anchor while other Anchors exist
**When** route compilation runs
**Then** the result contains one or more deterministic diagnostics naming Connection, missing endpoint, correction, and Source Trace
**And** the result contains no partial Routes or fallback occurrence endpoint (FR-7.1, FR-7.2, FR-7.3)

**Given** a valid Route
**When** its fact is published
**Then** Connection, source/target Occurrence, source/target port, source/target Anchor, Sheet, Lane, and Source Trace all resolve exactly once (FR-7.1)

**Given** a code review of the active route path
**When** route implementation is inspected
**Then** no first/last fallback or `mapNotNull` silent loss remains and no professional optimization claim is present (FR-7.2, FR-8.2)

## Epic 3: Trustworthy Spatial Contract

The compiler can prove complete, typed Spatial reality before Presentation consumes it.

### Story 3.1: Publish Explicit Geometry Facts

As an engineer,
I want inspectable typed facts with stable ownership and trace,
so that downstream tools cannot confuse a Construct, Occurrence, or route endpoint.

**Acceptance Criteria:**

**Given** a valid compiled Snapshot
**When** the Spatial document is assembled
**Then** it has typed Sheet, Occurrence, Region, Construct, alignment, Anchor, Route, Lane, Grid Reference, and quality facts
**And** each fact has stable identity, owning Sheet, and required Source Trace (FR-9.1)

**Given** a Construct identity or Grid Reference
**When** the model is inspected
**Then** Construct identity is not stored in an Occurrence ID and Grid References are not raw maps (FR-9.2)

**Given** any unordered Projection collections
**When** the complete document is compiled twice
**Then** canonical ordering and equality remain stable (FR-9.3, NFR-1)

**Given** the Golden Fixture
**When** coverage is checked
**Then** counts are exactly 8 Occurrences, 3 Regions, 7 Constructs, all referenced ports/Connections, all Grid References, and only used Lanes (FR-9.4)

### Story 3.2: Enforce Geometry Validation

As an engineer,
I want all invalid geometry reported before paint facts exist,
so that Theia cannot hide compiler defects.

**Acceptance Criteria:**

**Given** a Spatial document with any missing, duplicate, foreign, nonpositive, out-of-area,
membership, containment, endpoint, orthogonality, obstacle, trace, or metric issue
**When** complete validation runs
**Then** all deterministic issues are returned sorted by subject then problem
**And** each diagnostic has exact subject, problem, correction, and Source Trace (FR-10.1, FR-10.2, FR-10.3)

**Given** any validation issue
**When** compiler result is returned
**Then** it is `RealityTransformationResult.Failure` with no partial Spatial document reaching Presentation (FR-10.4)

**Given** a valid Spatial document
**When** Presentation transformation runs
**Then** rectangle coordinates and ordered Route points equal Spatial facts exactly (FR-11.1)

## Epic 4: Truthful Spatial Measurement

The engineer receives measurements that describe actual geometry rather than route or label proxies.

### Story 4.1: Measure Quality Milestone Facts

As an engineer,
I want quality metrics computed from Spatial geometry,
so that zero-defect claims are hand-verifiable and per-Sheet.

**Acceptance Criteria:**

**Given** rectangles that only edge-touch and rectangles with positive-area overlap
**When** quality compiles
**Then** only positive-area pairs count as Occurrence overlap (FR-12.1)

**Given** a Construct envelope and member rectangles
**When** quality compiles
**Then** containment failures count members not fully contained, not maximum-size guesses (FR-12.2)

**Given** a Route segment crossing a body without a segment vertex inside
**When** body intersection quality compiles
**Then** the segment/body interior intersection is counted and endpoint-owner/boundary contact is excluded (FR-12.3)

**Given** two Routes with a shared junction and two Routes crossing elsewhere
**When** crossing quality compiles
**Then** shared engineering junctions are excluded and distinct Route-pair/intersection-point tuples count (FR-12.4)

**Given** an angled segment
**When** twist quality compiles
**Then** it increments twist count; horizontal and vertical nonzero segments do not (FR-12.5)

**Given** multiple Sheets and overlapping Occurrence rectangles
**When** Lane use, Density, and Occupancy compile
**Then** values use independent per-Sheet denominators and Occurrence union area; Region/Construct overlays do not inflate Occupancy (FR-12.6, FR-12.7, FR-12.8)

**Given** M41 quality output
**When** metric names are inspected
**Then** no label count, label pressure, or label collision metric exists (FR-12.9)

### Story 4.2: Measure Density And Occupancy Against The Baseline

As an engineer,
I want a generated baseline tied to compiler facts,
so that M41 closure and M44 work start from reproducible evidence.

**Acceptance Criteria:**

**Given** the Golden Fixture compiler output
**When** baseline generation runs
**Then** the M41 artifact records source digest, Sheet/Drawing Area bounds, subject counts, metric definitions, exact per-Sheet values, command, and timestamp (FR-13.1)

**Given** a baseline verification test
**When** it parses the artifact and recompiles the fixture
**Then** it compares structured facts and exact values rather than prose constants or self-comparisons (FR-13.2)

**Given** an M40 comparison row
**When** baseline review runs
**Then** it includes only identical fixture, viewport, units, and method; M41 label metrics are omitted (FR-13.3)

## Epic 5: M41 Product Proof And Closure

The actual product surface proves the recovered Spatial contract and closure records remain honest.

### Story 5.1: Build The Dedicated M41 Example

As an engineer,
I want the M41 rolling-shutter source compiled through the real pipeline,
so that unit facts and product proof share one authority.

**Acceptance Criteria:**

**Given** `examples/m41/rolling-shutter`
**When** the actual compiler pipeline runs
**Then** it uses `ProjectionSpatialCompiler` and produces exactly 8 Occurrences, 3 Regions, 7 Constructs, all referenced Anchors, one Route per visible Projection Connection, Grid References, Lanes, and quality facts
**And** no M40 example or stale bridge remains on the active product path (GATE-1, GATE-2)

**Given** production source after caller migration
**When** source-set hygiene runs
**Then** no proof/demo/sample/milestone class or stale alternative Spatial authority exists in `src/main` (NFR-3, GATE-6)

### Story 5.2: Build The M41 E2E Evidence

As an engineer,
I want runtime proof to reject collapsed drawings,
so that screenshots demonstrate Spatial Reality rather than file existence.

**Acceptance Criteria:**

**Given** changed kernel, LSP, frontend, or Theia code
**When** final product proof runs
**Then** all affected surfaces are rebuilt before Electron starts and verification commands run sequentially (GATE-4)

**Given** the active M41 project in desktop and narrow viewports
**When** verifier reads runtime payload and Drawing Area pixels
**Then** counts/Route coverage/zero Blocking Metrics, occupied width >= 0.55, occupied height >= 0.45, coordinate preservation, and pixels across three horizontal and three vertical buckets all pass (SM-1 through SM-4, GATE-5)

**Given** an old top-strip image, blank canvas, hardcoded renderer boolean, or PNG-only proof
**When** verifier runs
**Then** it fails with an actionable message (FR-11.2, GATE-6)

### Story 5.3: Close M41 With An Honest Retrospective

As an engineer,
I want closure records generated only after all implementation evidence passes,
so that future milestones inherit an honest baseline.

**Acceptance Criteria:**

**Given** any Story 1.1 through 5.2 or Epic 1 through 4 not marked `done`
**When** closure workflow starts
**Then** it halts and creates no retrospective (GATE-7)

**Given** all prior stories and epics are `done`
**When** final sequential verification and BMad review complete
**Then** retrospective records exact command results, baseline values, screenshot paths, and remaining M42/M43/M44/M45 work
**And** it makes no professional parity claim and validates referenced files, statuses, dimensions, and metric values (GATE-3, GATE-7)

**Given** final M41 artifact set
**When** status is updated
**Then** all five Epics and 13 Stories are `done`, retrospective is complete only after review, and all gates pass (GATE-1 through GATE-7)
