---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-02-m40/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-02-m40/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-02-m40/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-02-m40/reviews/review-adversarial.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-02-m40/reviews/review-edge-case.md
---

# Athena M40 Epic Breakdown

## Outcome

M40 establishes Projection Reality as the authoritative owner of engineering views. Projection
owns view, sheet, occurrence, region, reading order, projection rules, projection selection, and
domain-contributed projection constructs. Composition is one projection capability, not a
separate reality. Spatial consumes Projection; Spatial Quality metrics are acceptance evidence,
not an epic.

```text
Engineering Reality
  -> Projection Reality  (views, sheets, occurrences, regions, reading order, constructs)
  -> Spatial Reality     (derived placement/bounds/anchors/routes + quality metrics)
  -> Presentation Reality
  -> Theia
```

The goal is not professional drawing parity. The goal is one authoritative Projection owner and
an honest measured step toward the QElectroTech rolling-shutter composition target.

## Epic List

### M40-E1: Projection Model

Engineers author views, sheets, occurrences, functional regions, reading order, and projection
selection; Projection Reality is the sole authoritative owner of the view-specific engineering
document.

**FRs covered:** FR-1 through FR-6.

### M40-E2: Projection Constructs

The kernel owns a domain-neutral `ProjectionConstruct` contract; the electrical package
contributes `RailProjection`, `RungProjection`, `BranchProjection`, `WireBundleProjection`,
`TerminalStripProjection`, `ContactGroupProjection`, and `CoilGroupProjection`; the stale
`:kernel:drawing-composition` authority is retired.

**FRs covered:** FR-7 through FR-12.

### M40-E3: Projection Compiler

Engineering Reality lowers to Projection Reality through one thin typed transformation carrying
views, sheets, occurrences, regions, reading order, and constructs, with boundary validation.

**FRs covered:** FR-13 through FR-15.

### M40-E4: Spatial Consumes Projection

Spatial derives placement, bounds, anchors, and routes from Projection facts and measures
Spatial Quality metrics against the M40 target; no routing optimization happens in M40.

**FRs covered:** FR-16 through FR-18.

### M40-E5: Proof

A dedicated rolling-shutter `examples/m40` project, M40 verifier, screenshots, and an honest
retrospective prove the milestone on its own paths.

**FRs covered:** FR-19 through FR-22.

## M40-E1: Projection Model

### Story 1.1: Establish Projection View And Sheet Authority

As a compiler maintainer,
I want Projection Reality to own view and sheet identity, membership, and source trace,
so that every view-specific document has one authoritative owner.

**Acceptance Criteria:**

Given an Engineering Document
When the projection model represents views and sheets
Then every projection document carries exactly one view identity and one compiler authority
And every sheet belongs to exactly one view and carries a stable sheet identity
And a view with no sheets fails with a plain diagnostic
And an empty sheet (no subjects) fails with a plain diagnostic
And sheets preserve source trace and ordered membership
And no generic graph framework, compatibility shim, milestone name, or `V0`/`V1` naming is introduced.

### Story 1.2: Add Occurrence Identity And Functional Regions

As an engineer,
I want occurrences to carry stable identity and group into functional regions,
so that the drawing reflects engineering meaning without layout facts.

**Acceptance Criteria:**

Given engineering subjects and region declarations
When Projection compiles them
Then every projected occurrence traces to its engineering subject
And duplicate occurrence identities are rejected with a named diagnostic
And an occurrence without an engineering source fails before Spatial
And a region groups exactly its declared occurrences by identity
And a region has no placement, size, or style facts at the Projection level
And an incomplete region (empty or referencing a missing occurrence) fails with a plain diagnostic.

### Story 1.3: Add Deterministic Reading Order And Projection Selection

As an evaluator,
I want a deterministic reading order and a single projection selection surface,
so that the same source always projects the same view.

**Acceptance Criteria:**

Given view declarations and sheet declarations
When Projection compiles them
Then reading order is a stable, repeatable projection of source order plus region order
And reading order is a permutation of the declared sheets; duplicates or unknown entries fail with a plain diagnostic
And view declarations are the sole authoring surface for view selection in M40 source
And the same source compiles to the same selection on every run.

## M40-E2: Projection Constructs

### Story 2.1: Retire The Drawing-Composition Authority

As an architect,
I want the stale Cabinet drawing authority gone,
so that M40 leaves exactly one projection construct owner.

**Acceptance Criteria:**

Given the pre-M40 `:kernel:drawing-composition` path
When M40 cleanup runs
Then the module, `AthenaProfessionalDrawingCompiler`, `AthenaCabinetProjectionCompiler`, the
`deriveProfessionalControlDrawing` branch, the `professional-connection-drawing` policy target,
and the LSP `AthenaDrawingCompositionPayload` surface are deleted
And the Cabinet deletion-gate and M36/M37 cabinet tests are rewritten to the current model or deleted
And no doc, test, or example references the retired path
And source-set hygiene, encoding audit, and `git diff --check` pass.

### Story 2.2: Define The Domain-Neutral ProjectionConstruct Contract

As a platform maintainer,
I want one kernel contract for projection constructs,
so that any domain can contribute constructs without touching the kernel.

**Acceptance Criteria:**

Given the kernel projection model
When the `ProjectionConstruct` contract is added
Then it defines identity, source trace, membership, and validation shape only
And no `Rail`, `Rung`, `ContactGroup`, or other domain construct name appears in
`kernel/*/src/main` production source
And a new domain package can add constructs without changing kernel code
And no universal `Fact` base class, empty wrapper, or generic graph API is introduced.

### Story 2.3: Add Electrical Package Construct Implementations

As an electrical engineer,
I want rail, rung, branch, wire bundle, terminal strip, contact group, and coil group projection
constructs,
so that a schematic can express real composition intent.

**Acceptance Criteria:**

Given the `ProjectionConstruct` contract
When the electrical package contributes implementations
Then `RailProjection`, `RungProjection`, `BranchProjection`, `WireBundleProjection`,
`TerminalStripProjection`, `ContactGroupProjection`, and `CoilGroupProjection` resolve through the
electrical package, exactly like `power`/`control`/`earth` resolve through `ConnectionVerb`
And the kernel compiles without dependency on any domain construct implementation
And construct words resolve in M40 source without a second DSL.

### Story 2.4: Validate Constructs And Preserve Grouped Endpoint Integrity

As an evaluator,
I want incomplete or ambiguous constructs to fail before Spatial,
so that projection facts are trustworthy.

**Acceptance Criteria:**

Given construct declarations
When Projection validates them
Then a group referencing a missing occurrence, a coil without its device, a strip referencing a
missing terminal, a rung without an assigned occurrence, an empty construct, a duplicate
construct identity, a construct without source trace, or invalid construct nesting fails with a
named diagnostic before Spatial
And engineering connections and their source traces are identical with and without projection
declarations
And a grouped endpoint (e.g., `earth A to [B, C]`) remains one traceable relationship through
Projection.

## M40-E3: Projection Compiler

### Story 3.1: Transform Engineering To Projection With Constructs

As a compiler maintainer,
I want one thin typed transformation from Engineering to Projection,
so that views carry the full projection document without corrupting engineering truth.

**Acceptance Criteria:**

Given Engineering Reality
When Engineering to Projection runs
Then it uses `RealityTransformation<InputReality, OutputReality>`
And it emits Projection Reality only, carrying views, sheets, occurrences, regions, reading
order, and constructs
And source trace and engineering identity survive the transformation
And the same source and domain packages produce the same projection snapshot on every run
And diagnostics name the missing engineering or projection fact plainly.

### Story 3.2: Enforce Projection Boundary Validation

As a maintainer,
I want Projection snapshots validated before downstream consumption,
so that coordinates and paint facts cannot leak into Projection.

**Acceptance Criteria:**

Given a Projection snapshot
When boundary validation runs
Then a snapshot containing coordinates, anchors, lanes, routes, stroke, labels, or paint order
fails with a named diagnostic
And validation runs before Spatial consumes the snapshot.

## M40-E4: Spatial Consumes Projection

### Story 4.1: Derive Placement From Projection Constructs

As a compiler maintainer,
I want Spatial to derive placement, bounds, anchors, and routes from Projection facts,
so that Projection stays coordinate-free.

**Acceptance Criteria:**

Given Projection occurrences and constructs
When the Spatial compiler runs
Then placement, bounds, anchors, and route facts are derived from projection construct and
occurrence identities
And Projection models store no final coordinates
And no M40 code path performs lane optimization, bend minimization, crossing minimization, or
multi-sheet continuation.

### Story 4.2: Measure Spatial Quality Against The M40 Target

As a milestone owner,
I want honest quality measurements,
so that M40 proves non-regression and records exact numbers.

**Acceptance Criteria:**

Given the M40 example composed and flat
When the Spatial Quality Analyzer runs
Then density, collision, occupancy, and label pressure are computed deterministically from
Presentation Document bounds (no pixels)
And the same M40 source is measured flat vs composed at desktop 1920x1080 fit-to-screen zoom
And label collisions do not exceed the M39 baseline (28) for the composed M40 example
And route/body intersections stay at 0
And the full emitted label set is measured and label count reported (no label suppression).

## M40-E5: Proof

### Story 5.1: Build The Dedicated M40 Example

As a milestone owner,
I want a dedicated rolling-shutter example,
so that M40 is judged on its own architecture.

**Acceptance Criteria:**

Given the M40 example project under `examples/m40`
When it compiles through all four realities
Then it declares at least one of each electrical construct (rail, rung, branch, wire bundle,
terminal strip, contact group, coil group) and at least one functional region
And it uses human-first relation syntax (`to` preferred) and no `intent` blocks
And proof artifacts cite only M40 paths.

### Story 5.2: Build The M40 E2E Evidence

As a milestone owner,
I want real product evidence,
so that M40 closure is verifiable.

**Acceptance Criteria:**

Given the M40 example
When kernel, LSP, frontend, and product surfaces are rebuilt
Then the verifier asserts projection constructs, Spatial Quality metrics, and paint-only
assertions
And screenshots exist under `_bmad-output/implementation-artifacts/m40/screenshots` at the
reference viewports and are non-blank
And the renderer creates no views, regions, rails, rungs, or groups
And the LSP surface recompiles and validates M40 source without regression and with no new LSP
protocol surface.

### Story 5.3: Close M40 With An Honest Retrospective

As a milestone owner,
I want closure to separate foundation from visual parity,
so that M41+ starts from real remaining work.

**Acceptance Criteria:**

Given all M40 stories are complete
When the retrospective is written
Then it records Projection ownership, construct authority, Spatial Quality measurements, the
drawing-composition retirement, and screenshots
And it compares M40 output against the M39 baseline and the QElectroTech reference without
claiming EPLAN/QET parity
And routing, label-engine, and parity items are explicitly deferred per the roadmap.

## FR Coverage Map

FR-1: Epic 1 - Projection is authoritative owner of engineering views
FR-2: Epic 1 - View and sheet identity, membership, source trace
FR-3: Epic 1 - Occurrence identity traceable to engineering subject
FR-4: Epic 1 - Functional region as logical section
FR-5: Epic 1 - Deterministic reading order
FR-6: Epic 1 - Projection rules and selection, sole authoring surface
FR-7: Epic 2 - Domain-neutral ProjectionConstruct contract
FR-8: Epic 2 - Domain packages provide construct implementations
FR-9: Epic 2 - Construct validation
FR-10: Epic 2 - One projection construct authority (retire drawing-composition)
FR-11: Epic 2 - Grouped endpoint integrity
FR-12: Epic 2 - Concrete types, not a generic framework
FR-13: Epic 3 - Engineering to Projection transformation
FR-14: Epic 3 - Deterministic projection compilation
FR-15: Epic 3 - Projection boundary validation
FR-16: Epic 4 - Spatial derives placement from Projection
FR-17: Epic 4 - Composition is not routing
FR-18: Epic 4 - Spatial Quality metrics as acceptance evidence
FR-19: Epic 5 - Dedicated M40 example
FR-20: Epic 5 - M40 E2E evidence
FR-21: Epic 5 - Paint-only renderer boundary
FR-22: Epic 5 - Honest M40 closure
