---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-02-m40/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-02-m40/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-02-m40/ARCHITECTURE-SPINE.md
---

# Athena M40 Epic Breakdown

## Outcome

M40 establishes **Projection Reality** as the authoritative owner of engineering views.
Composition is one projection capability, not the milestone itself:

```text
Engineering Reality
  -> Projection Reality (views, sheets, occurrences, regions, reading order,
                         projection rules/selection, projection constructs)
  -> Spatial Reality
  -> Presentation Reality
  -> Theia
```

Projection stays domain-neutral: the kernel owns the `ProjectionConstruct` contract only;
the electrical package contributes construct implementations (rail, rung, branch, wire bundle,
terminal strip, contact group, coil group), exactly like M39 domain relation verbs
(`power`/`control`/`earth`). Spatial consumes Projection and derives placement; density metrics
are acceptance evidence, not an epic. The stale `:kernel:drawing-composition` authority is
retired. The visual target is the QElectroTech rolling-shutter reference as a composition
reference, not pixel parity.

## Epic List

### M40-E1: Projection Model

Establish Projection Reality as the authoritative owner of engineering views.

**FRs covered:** FR-1 through FR-6.

### M40-E2: Projection Constructs

Domain-neutral `ProjectionConstruct` contract with electrical package implementations, one
construct authority, and construct validation.

**FRs covered:** FR-7 through FR-12.

### M40-E3: Projection Compiler

One thin typed Engineering -> Projection transformation with boundary validation.

**FRs covered:** FR-13 through FR-15.

### M40-E4: Spatial Consumes Projection

Spatial derives placement from Projection constructs; Spatial Quality metrics measured against
the M39 baseline.

**FRs covered:** FR-16 through FR-18.

### M40-E5: Proof

Dedicated M40 example, E2E evidence, paint-only renderer boundary, and honest retrospective.

**FRs covered:** FR-19 through FR-22.

## M40-E1: Projection Model

### Story 1.1: Establish Projection View And Sheet Authority

As a compiler maintainer,
I want Projection to own views and sheets,
so that engineering views have one authority before Spatial touches them.

**Acceptance Criteria:**

- A view declaration creates exactly one view identity with one projection compiler authority.
- A sheet belongs to exactly one view, carries a stable identity, and groups its declared
  occurrences.
- A sheet exposes a grid reference system (rows, columns, cell references such as A1/B3) as
  Projection structure; grid facts carry no coordinates.
- A view with no sheets and a sheet with no occurrences (directly or through a region or
  construct) each fail with a plain diagnostic naming the subject and problem.
- No projection fact is created or modified by Spatial, Presentation, or the renderer.

### Story 1.2: Add Occurrence Identity And Functional Regions

As a compiler maintainer,
I want occurrences traceable to engineering subjects and functional regions,
so that projected content is provably derived from engineering truth.

**Acceptance Criteria:**

- Every occurrence carries stable identity traceable to its engineering subject.
- An occurrence without an engineering source, or a duplicate occurrence identity, fails with a
  named diagnostic.
- A functional region groups exactly its declared occurrences by identity; an empty region or a
  missing occurrence fails with a plain diagnostic.
- A region has no placement, size, or style facts at the Projection level.

### Story 1.3: Add Deterministic Reading Order And Projection Selection

As an engineer,
I want deterministic reading order and projection selection,
so that the same source always produces the same view.

**Acceptance Criteria:**

- Reading order is a permutation of the declared sheets; within a sheet, occurrences follow
  declaration order inside regions, then region declaration order.
- A duplicate or unknown sheet entry fails with a plain diagnostic.
- The same source compiles to the same reading order and selection on every run.
- View declarations are the sole M40 authoring surface for view selection; the existing
  projection-policy selection surface is retired or rewritten, not shimmed.

## M40-E2: Projection Constructs

### Story 2.1: Retire The Drawing-Composition Authority

As a milestone owner,
I want the stale composition authority removed,
so that M40 has exactly one projection construct authority.

**Acceptance Criteria:**

- `:kernel:drawing-composition` is removed from `settings.gradle.kts` and
  `kernel/compiler/build.gradle.kts`; `AthenaProfessionalDrawingCompiler`,
  `AthenaCabinetProjectionCompiler`, and the LSP `AthenaDrawingCompositionPayload` surface are
  deleted.
- The `deriveProfessionalControlDrawing` branch and the `professional-connection-drawing` policy
  target surface are removed, not left as silent no-ops.
- Sheet-frame and title-block facts (`DrawingSheetFrameFact`/`DrawingSheetTitleBlockFact`) are
  replaced by M40-owned facts before retirement so metric inputs and sheet chrome survive.
- Dependent tests and M34-M38 examples using the retired path are rewritten or deleted; no doc,
  test, or example references the retired path; hygiene and encoding audits pass.

### Story 2.2: Define The Domain-Neutral ProjectionConstruct Contract

As a compiler maintainer,
I want a kernel-owned `ProjectionConstruct` contract,
so that the kernel stays domain-neutral.

**Acceptance Criteria:**

- The contract defines identity, source trace, membership, and validation shape; it names no
  electrical, mechanical, or process vocabulary.
- No `Rail`, `Rung`, `ContactGroup`, or other domain construct name appears in kernel production
  source (`kernel/*/src/main`) after the Story 2.1 retirement.
- A new domain package can add constructs without changing kernel code.
- No generic graph API, universal `Fact` base class, or empty wrapper model is introduced.

### Story 2.3: Add Electrical Package Construct Implementations

As an engineer,
I want rail, rung, branch, wire bundle, terminal strip, contact group, and coil group constructs,
so that I can author projection structure with electrical vocabulary.

**Acceptance Criteria:**

- The electrical package contributes `RailProjection`, `RungProjection`, `BranchProjection`,
  `WireBundleProjection`, `TerminalStripProjection`, `ContactGroupProjection`, and
  `CoilGroupProjection` under the `ProjectionConstruct` contract.
- Construct words resolve through the electrical package exactly like M39 domain relation verbs
  (`power`/`control`/`earth`); the kernel compiles without dependency on them.
- The M40 example authors all seven construct forms from the committed Syntax Target.
- Grouped endpoints remain one traceable engineering relationship through Projection.

### Story 2.4: Validate Constructs And Preserve Grouped Endpoint Integrity

As an engineer,
I want broken constructs to fail clearly,
so that invalid projection never reaches Spatial.

**Acceptance Criteria:**

- Empty constructs, duplicate construct identities, constructs without source trace, and invalid
  nesting (e.g., a rung containing a rail) fail with named diagnostics before Spatial.
- A group referencing a missing occurrence, a coil without its device, a strip referencing a
  missing terminal, or a rung without an assigned occurrence fails with a plain diagnostic.
- Engineering connections and their source traces are identical with and without projection
  declarations.

## M40-E3: Projection Compiler

### Story 3.1: Transform Engineering To Projection With Constructs

As a compiler maintainer,
I want Engineering Reality to lower to Projection Reality through one typed transformation,
so that views never corrupt engineering truth.

**Acceptance Criteria:**

- The transformation uses the common typed interface
  (`RealityTransformation<InputReality, OutputReality>`), accepts Engineering Reality, and emits
  Projection Reality only.
- Views, sheets, occurrences, regions, reading order, and constructs survive with source trace
  and engineering identity.
- The same source and domain packages produce the same projection snapshot on every run.

### Story 3.2: Enforce Projection Boundary Validation

As a maintainer,
I want projection snapshots validated before downstream consumption,
so that spatial or presentation facts never leak into Projection.

**Acceptance Criteria:**

- A projection snapshot containing coordinate, anchor, lane, route, stroke, label, or paint-order
  facts fails validation with a named diagnostic.
- Diagnostics name the exact subject, problem, and correction in plain engineering language.

## M40-E4: Spatial Consumes Projection

### Story 4.1: Derive Placement From Projection Constructs

As a compiler maintainer,
I want Spatial to derive placement from Projection constructs,
so that placement stays derived and Projection stays coordinate-free.

**Acceptance Criteria:**

- The Spatial compiler derives placement, bounds, anchors, and route facts from Projection
  occurrences and constructs; Projection stores membership and reading order only.
- Spatial output is traceable to projection construct and occurrence identities.
- Spatial placement maps each occurrence and construct to a sheet grid cell reference (e.g.,
  A1/B3) derived from its placement; grid references are Spatial-owned facts available to later
  export surfaces.
- No M40 code path performs lane optimization, bend minimization, crossing minimization, or
  multi-sheet continuation.

### Story 4.2: Measure Spatial Quality Against The M40 Target

As a milestone owner,
I want density and quality measured, not claimed,
so that M40 has an honest non-regression baseline.

**Acceptance Criteria:**

- The Spatial Quality Analyzer measures density, occupancy, label pressure, and route/body
  intersections from Presentation Document bounds (no pixels).
- The M40 proof reports the same M40 source flat vs composed at desktop 1920x1080 fit-to-screen,
  with the M39 baseline (28 label collisions) as cross-reference only.
- Target (decided): label collisions <= 28, route/body intersections = 0; label suppression is
  forbidden and the full emitted label count is reported.
- Sheet-area facts come from the M40 replacement for the retired
  `DrawingSheetFrameFact`/`DrawingSheetTitleBlockFact` inputs.

## M40-E5: Proof

### Story 5.1: Build The Dedicated M40 Example

As a milestone owner,
I want a dedicated rolling-shutter control example,
so that M40 is judged on its own architecture.

**Acceptance Criteria:**

- `examples/m40` declares at least one of each electrical construct and at least one functional
  region; the verifier asserts each appears in the projection snapshot.
- The example compiles through all four realities, uses human-first `to` syntax and no `intent`
  blocks, and cites only M40 paths.
- The example does not reuse M36-M39 examples as proof authority.

### Story 5.2: Build The M40 E2E Evidence

As a milestone owner,
I want rebuilt surfaces and screenshots,
so that M40 proof is backed by real product verification.

**Acceptance Criteria:**

- Kernel, LSP, frontend, and product surfaces are rebuilt before screenshots; screenshots exist
  under `_bmad-output/implementation-artifacts/m40/screenshots` and are non-blank.
- The verifier reports projection constructs, Spatial Quality metrics, and paint-only assertions.
- Theia and SVG export perform no projection, composition, placement, grouping, or routing
  inference.
- The LSP surface recompiles and validates M40 source without regression and without a new LSP
  protocol surface.

### Story 5.3: Close M40 With An Honest Retrospective

As a milestone owner,
I want honest closure,
so that M41 starts from real remaining work.

**Acceptance Criteria:**

- The retrospective records what improved, what remains visually poor, and which failures move to
  later milestones.
- It compares M40 output against the M39 baseline and the QElectroTech composition reference
  without claiming EPLAN/QET parity.
- It records the `:kernel:drawing-composition` retirement and the `layout`/`place` migration.
- Counter-metric evidence is reported: authored declaration lines per connection relation and
  projection-compile time (within 2x M39 example compile time, median of three runs).
