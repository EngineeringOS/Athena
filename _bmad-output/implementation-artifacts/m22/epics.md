---
stepsCompleted:
  - extract-m22-requirements
  - design-m22-epics
  - create-m22-stories
  - validate-m22-coverage
inputDocuments:
  - ../../planning-artifacts/prds/prd-Athena-2026-07-17-m22/prd.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-17-m22/addendum.md
  - ../../planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md
---

# Athena - M22 Epic Breakdown

## Overview

M22 turns M21's governed layout-intelligence foundation into governed layout optimization and
component layout round-trip. The milestone must make the schematic sheet visibly more professional
inside the Athena Theia IDE while preserving upstream semantic authority, deterministic layout facts,
and reviewable `.athena` source intent.

M22 is not an EPLAN clone, not an ecosystem/import milestone, not a public repository milestone, and
not a physical routing milestone. ELK may be evaluated only as an optional local adapter behind
Athena contracts.

## Requirements Inventory

### Functional Requirements

FR1: Provide `examples/m22/sample-project` with real `.athena` files that exercise governed layout
optimization, layout adjustment, and layout-hint round-trip scenarios.

FR2: Define professional layout acceptance references using `draft/screenshort` inspiration and M21
comparison criteria, including spacing, grouping, basic routing, label readability, and sheet
scanability without claiming full EPLAN parity.

FR3: Add a Layout Constraint Model that derives and persists declarative relationships between
layout intent and solved layout facts.

FR4: Add an Athena layout-solver / optimization boundary that consumes layout intent, constraints,
rules, and existing facts and emits improved normalized Athena layout facts.

FR5: Add an optional experimental ELK adapter spike behind Athena's layout-solver boundary without
choosing ELK as final architecture, sole layout engine, semantic authority, or persistence format.

FR6: Improve visible schematic layout quality in the M22 sample through better placement, grouping,
basic diagram edge routing, and basic label overlap avoidance.

FR7: Capture user component placement, alignment, or grouping adjustments as explicit layout
adjustment intents with canonical subject, occurrence, view, sheet, and source identities.

FR8: Persist approved component placement, alignment, and grouping adjustments into `.athena` as
governed layout hints or layout blocks, with route and label hint persistence deferred unless
mechanically trivial.

FR9: Provide an inspectable mutation preview before applying layout-hint source edits.

FR10: Preserve source, outline, Problems, and Graphical View coherence while layout optimization and
layout round-trip are active, including projecting the active `.athena` file instead of a seed file.

FR11: Preserve accepted M20/M21 graph workbench behavior: grid as coordinate surface, sheet/component
transparency over the grid, `Cabinet Main` information in the top popover, and transparent floating
controls.

FR12: Keep M22 boundaries explicit: no public package registry, full IEC/QElectroTech ingestion,
cabinet authoring, physical routing, full EPLAN parity, final ELK/layout-stack decision, or
sheet-local drag-save truth.

### NonFunctional Requirements

NFR1: The same governed input must produce stable replayable layout facts and stable visible
presentation.

NFR2: Layout input must be canonicalized; subject, occurrence, and sheet ordering must be stable;
equal-cost choices must use deterministic tie-breakers.

NFR3: Adapter output must be normalized into Athena layout facts before comparison or rendering.

NFR4: Replay tests must compare layout facts before relying on screenshots.

NFR5: Theia and the renderer must remain projection consumers and must not infer or persist
engineering meaning.

NFR6: User-approved layout adjustment persistence must flow through governed mutation authority and
produce reviewable `.athena` source changes.

NFR7: M22 proof must include an openable Theia sample project and a named visual acceptance checklist;
script/model tests are supporting evidence, not a replacement.

NFR8: The ELK spike, if implemented, must be local-only, isolated, deterministic after normalization,
removable without renderer or fact-contract changes, and must not introduce a remote service tier.

NFR9: Source-mutating round-trip stories must not start until the minimal `.athena` layout-hint syntax
direction is selected.

### Additional Requirements

- Architecture AD-1: Layout constraints are the optimization contract and must prevent layout work
  from collapsing into opaque `x/y` coordinates or renderer-local placement decisions.
- Architecture AD-2: Optimization emits normalized Athena facts only, with canonicalized inputs,
  stable ordering, deterministic tie-breakers, adapter normalization, and fact-level replay tests.
- Architecture AD-3: ELK is optional and experimental, local-only, isolated in an adapter
  package/module, deterministic after normalization, and removable without changing renderer
  contracts.
- Architecture AD-4: Round-trip scope is component placement, alignment, and grouping only.
- Architecture AD-5: Layout adjustments use governed mutation authority and are blocked from
  source mutation until the minimal `.athena` layout-hint syntax direction is selected.
- Architecture AD-6: Theia remains a projection consumer and preserves active-source projection,
  same-tab outline navigation, grid-backed canvas behavior, transparent controls, and top-popover
  information patterns.
- Architecture AD-7: Professional readability beats generic graph neatness and requires a reviewable
  acceptance artifact/checklist naming the comparison set.
- Architecture AD-8: Deferred domains stay explicit: ecosystem work, broad IEC/QElectroTech library
  ingestion, cabinet authoring, physical routing, advanced routing intelligence, standards-specific
  labels, AI layout, and full EPLAN parity stay out of M22.
- The exact `.athena` layout-hint syntax is a blocker for source-mutating round-trip implementation
  stories.
- The ELK dependency/package choice must be made before any ELK adapter story starts, while still
  avoiding a final stack decision.
- The `draft/screenshort` acceptance comparison set must be selected before acceptance fixture and
  visual proof stories close.

### UX Design Requirements

No separate M22 UX design contract exists. M22 uses accepted M20/M21 Theia graph workbench behavior
and M22 architecture proof constraints as the UX input:

UX-DR1: The M22 sample project must open through the normal Athena Theia IDE workflow, not through
`.mjs` fixture files.

UX-DR2: The stage grid remains the coordinate surface behind sheet and component bodies.

UX-DR3: Sheet and component bodies must not hide the grid unless a future governed mode explicitly
requires it.

UX-DR4: `Cabinet Main` information remains in the top information popover only.

UX-DR5: Top and bottom controls remain transparent canvas overlays.

UX-DR6: Outline navigation keeps the same `.athena` editor tab.

UX-DR7: The layout adjustment preview must name the affected subject and layout intent before source
edits are applied.

UX-DR8: The M22 visual acceptance checklist must cover zones, spacing, grouping, basic orthogonal
edge routing, label overlap avoidance, and M21 baseline comparison.

### FR Coverage Map

FR1: Epic 1 - openable M22 sample project with real `.athena` files.

FR2: Epic 1 and Epic 4 - professional layout acceptance references, checklist, and visual
comparison evidence.

FR3: Epic 2 - Layout Constraint Model for governed placement and relationship constraints.

FR4: Epic 2 - deterministic layout optimization boundary and normalized Athena layout facts.

FR5: Epic 3 - optional local ELK adapter spike behind Athena contracts.

FR6: Epic 2 - visible schematic layout quality improvement in the M22 sample project.

FR7: Epic 4 - component placement, alignment, and grouping adjustment intents.

FR8: Epic 4 - approved component adjustments persisted as governed `.athena` layout hints or blocks.

FR9: Epic 4 - inspectable mutation preview before source edits.

FR10: Epic 5 - source, outline, Problems, and Graphical View coherence during optimization and
round-trip.

FR11: Epic 1 and Epic 5 - accepted M20/M21 graph workbench behavior preserved.

FR12: Epic 5 - explicit boundary checks for deferred domains and no canvas-local layout truth.

## Epic List

### Epic 1: Openable M22 Layout Proof Baseline
Reviewers can open a real M22 project in the Athena Theia IDE, compare it against the M21 baseline
and selected EPLAN-style references, and use a named acceptance checklist instead of subjective
"looks better" judgment.
**FRs covered:** FR1, FR2, FR11

### Epic 2: Governed Professional Layout Optimization
Engineers can see the M22 sample become more readable through governed constraints, deterministic
optimization, normalized Athena layout facts, and a reviewable professional layout acceptance
checklist.
**FRs covered:** FR2, FR3, FR4, FR6

### Epic 3: Optional Local ELK-Assisted Optimization Spike
The team can evaluate whether ELK improves layout quality through a local, removable, deterministic
adapter without making ELK the architecture, authority, persistence format, or renderer truth.
**FRs covered:** FR5

### Epic 4: Reviewable Component Layout Round-Trip
Engineers can adjust component placement, alignment, or grouping and see an inspectable source
mutation preview that persists approved layout intent into `.athena`.
**FRs covered:** FR7, FR8, FR9

### Epic 5: IDE Coherence And Scope Guardrails
Reviewers can verify active-source projection, source/outline/Problems/Graphical View coherence,
accepted graph workbench behavior, and M22 deferred boundaries while optimization and round-trip are
active.
**FRs covered:** FR10, FR11, FR12

## Epic 1: Openable M22 Layout Proof Baseline

Reviewers can open a real M22 project in the Athena Theia IDE, compare it against the M21 baseline
and selected EPLAN-style references, and use a named acceptance checklist instead of subjective
"looks better" judgment.

### Story 1.1: Create the openable M22 sample project

As a reviewer,
I want an M22 sample project with real `.athena` files,
So that I can inspect M22 layout optimization through the normal Athena Theia workflow.

**Acceptance Criteria:**

**Given** the M22 milestone workspace
**When** I inspect `examples/m22/sample-project`
**Then** it contains real `.athena` source files for baseline, optimized layout, and round-trip
scenarios
**And** the sample includes power source, protection, controller, HMI, terminal block, and load
subjects
**And** the documented launch path opens the project in the Athena Theia IDE without requiring users
to inspect `.mjs` files

### Story 1.2: Define the M22 professional layout acceptance checklist

As a reviewer,
I want a named visual acceptance checklist for M22,
So that layout quality is judged against explicit engineering criteria rather than subjective taste.

**Acceptance Criteria:**

**Given** the M22 sample project and `draft/screenshort` references
**When** the acceptance checklist is published
**Then** it names the comparison set used for M22
**And** it covers zones, spacing, grouping, basic orthogonal edge routing, label overlap avoidance,
and M21 baseline comparison
**And** it states that full EPLAN parity is not part of the M22 acceptance bar

### Story 1.3: Add the M22 IDE-visible baseline proof

As a reviewer,
I want the M22 sample to prove baseline behavior in the graph workbench,
So that later optimization work starts from a visible, accepted IDE surface.

**Acceptance Criteria:**

**Given** the M22 sample project is opened in Theia
**When** the Graphical View is launched for the baseline `.athena` file
**Then** the graph workbench renders the active source file
**And** the stage grid remains visible behind sheet and component bodies
**And** `Cabinet Main` information remains in the top information popover only
**And** top and bottom controls remain transparent canvas overlays

## Epic 2: Governed Professional Layout Optimization

Engineers can see the M22 sample become more readable through governed constraints, deterministic
optimization, normalized Athena layout facts, and a reviewable professional layout acceptance
checklist.

### Story 2.1: Introduce the Layout Constraint Model

As an architect,
I want layout constraints to be explicit between layout intent and layout facts,
So that optimization is guided by engineering relationships instead of raw canvas coordinates.

**Acceptance Criteria:**

**Given** governed layout intent for the M22 sample
**When** layout constraints are derived
**Then** constraints can express near, below, aligned-with, grouped-with, preferred-zone,
preserve-order, and route-lane preference relationships
**And** constraints carry canonical subject, occurrence, sheet/view, snapshot, and source identities
where available
**And** raw `x/y` coordinates are not the primary authored constraint language

### Story 2.2: Add the deterministic layout optimization boundary

As an implementer,
I want a deterministic optimization boundary that emits normalized Athena layout facts,
So that renderer and adapter code cannot become layout authority.

**Acceptance Criteria:**

**Given** layout intent, constraints, rules, and existing facts
**When** the M22 optimization boundary runs
**Then** it emits normalized Athena layout facts consumed by the renderer
**And** optimizer inputs are canonicalized with stable subject, occurrence, and sheet ordering
**And** equal-cost choices use deterministic tie-breakers
**And** repeated runs on the same governed input produce the same layout facts

### Story 2.3: Improve governed schematic placement and grouping

As an engineer,
I want the M22 schematic to group engineering subjects by readable purpose,
So that power, protection, controller, HMI, terminals, and load path are easy to identify.

**Acceptance Criteria:**

**Given** the M22 optimized sample sheet
**When** layout facts are produced
**Then** power, protection, controller, HMI, terminals, and load subjects are placed into readable
zones or groups
**And** grouping follows Athena constraints and rules rather than renderer inference
**And** the M22 acceptance checklist can compare the optimized layout against the M21 baseline

### Story 2.4: Add basic route and label readability improvement

As an engineer,
I want basic routes and labels to avoid obvious conflicts,
So that M22 improves schematic communication without entering advanced routing or standards-specific
labeling scope.

**Acceptance Criteria:**

**Given** the M22 optimized sample sheet
**When** basic route and label facts are produced
**Then** routes use basic schematic edge routing only
**And** labels avoid obvious overlap with their own subject and primary routes
**And** no implementation claims physical routing, advanced electrical routing intelligence, or
standards-specific label generation

### Story 2.5: Prove deterministic layout replay and visual acceptance

As a reviewer,
I want executable proof that M22 layout optimization is stable and visible,
So that the milestone is not judged only by screenshots or manual inspection.

**Acceptance Criteria:**

**Given** the M22 sample project
**When** the layout replay tests run
**Then** layout facts are compared across repeated runs before screenshot checks
**And** adapter-normalized facts, if present, are compared after Athena normalization
**And** visual acceptance evidence checks the named M22 checklist items

## Epic 3: Optional Local ELK-Assisted Optimization Spike

The team can evaluate whether ELK improves layout quality through a local, removable, deterministic
adapter without making ELK the architecture, authority, persistence format, or renderer truth.

### Story 3.1: Decide the M22 ELK spike envelope

As an architect,
I want the ELK spike dependency and packaging envelope decided before implementation,
So that the adapter work cannot leak into the renderer, frontend runtime, or persistence format.

**Acceptance Criteria:**

**Given** the M22 architecture spine and PRD
**When** the ELK spike story begins
**Then** the team has selected whether ELK is included directly or isolated behind an experimental
adapter package
**And** the decision preserves a local-only execution envelope with no remote service tier
**And** the decision records how the spike can be removed without changing layout facts or renderer
contracts

### Story 3.2: Implement the local ELK adapter normalization path

As an implementer,
I want ELK output normalized into Athena layout facts,
So that ELK can assist optimization without becoming authority.

**Acceptance Criteria:**

**Given** Athena layout intent and constraints for the M22 sample
**When** the optional ELK adapter is enabled
**Then** adapter input is derived from Athena constraints, not renderer DOM
**And** adapter output is normalized into Athena layout facts before rendering or comparison
**And** the adapter path is deterministic after normalization
**And** disabling the adapter falls back to Athena rules without changing the renderer contract

### Story 3.3: Compare ELK-assisted output against Athena rule output

As a reviewer,
I want to compare ELK-assisted output with Athena rule-based output,
So that M22 can evaluate helper value without choosing a final layout stack.

**Acceptance Criteria:**

**Given** the M22 sample project has rule-based and ELK-assisted layout paths available
**When** comparison evidence is generated
**Then** the comparison uses normalized Athena layout facts
**And** it reports whether ELK improves checklist items such as spacing, grouping, and basic routing
**And** it states that M22 does not select ELK as final architecture or sole layout engine

## Epic 4: Reviewable Component Layout Round-Trip

Engineers can adjust component placement, alignment, or grouping and see an inspectable source
mutation preview that persists approved layout intent into `.athena`.

### Story 4.1: Select the minimal `.athena` layout-hint syntax

As an engineer,
I want a minimal source syntax for component layout hints,
So that layout round-trip persists engineering intent instead of arbitrary canvas coordinates.

**Acceptance Criteria:**

**Given** the M22 PRD, addendum, and architecture spine
**When** source-mutating round-trip implementation begins
**Then** the team has selected layout block, projection hint, or subject-local hint syntax
**And** the selected syntax supports component placement, alignment, and grouping
**And** the selected syntax avoids raw pixel coordinates as the primary authored language
**And** route and label persistence remains deferred unless mechanically trivial

### Story 4.2: Capture component adjustment intent from the graph workbench

As an engineer,
I want component placement, alignment, and grouping adjustments captured as governed intents,
So that user adjustment starts as semantic layout input rather than hidden canvas state.

**Acceptance Criteria:**

**Given** an M22 sheet occurrence is selected in the Graphical View
**When** the user adjusts placement, alignment, or grouping
**Then** Athena creates a layout adjustment intent with canonical subject, occurrence, view, sheet,
snapshot, and source identities
**And** the canvas does not persist hidden layout truth
**And** unsupported route or label adjustments are rejected or ignored with a clear non-M22 boundary

### Story 4.3: Show mutation preview before applying layout source edits

As an engineer,
I want to inspect a proposed layout source mutation before applying it,
So that round-trip edits remain intentional and reviewable.

**Acceptance Criteria:**

**Given** a layout adjustment intent has been created
**When** Athena prepares a source mutation
**Then** the preview names the affected subject and layout intent
**And** the preview shows the proposed `.athena` layout hint or block change
**And** rejecting the preview does not modify source or canvas state

### Story 4.4: Persist approved component layout hints and reproduce them

As an engineer,
I want approved component layout adjustments persisted into `.athena`,
So that closing and reopening the project reproduces the same governed layout.

**Acceptance Criteria:**

**Given** a user approves a placement, alignment, or grouping mutation preview
**When** Athena applies the change through governed mutation authority
**Then** the `.athena` source receives a reviewable layout hint or layout block
**And** source, outline, Problems, and sheet identity remain coherent after the edit
**And** reopening or reprojecting the project reproduces the adjusted layout from source

## Epic 5: IDE Coherence And Scope Guardrails

Reviewers can verify active-source projection, source/outline/Problems/Graphical View coherence,
accepted graph workbench behavior, and M22 deferred boundaries while optimization and round-trip are
active.

### Story 5.1: Preserve active-source projection in Graphical View

As an engineer,
I want Graphical View to project the active `.athena` file,
So that opening an optimized or round-trip scenario does not show a stale seed file.

**Acceptance Criteria:**

**Given** the M22 sample project contains multiple `.athena` files
**When** I open a non-baseline M22 source file and launch Graphical View
**Then** the projection corresponds to the active source file
**And** no stale baseline or seed projection is shown
**And** regression coverage proves the active-source selection path

### Story 5.2: Preserve source, outline, Problems, and sheet identity after optimization

As an engineer,
I want IDE navigation to remain coherent while layout optimization is active,
So that source, outline, diagnostics, and rendered sheet occurrences refer to the same identities.

**Acceptance Criteria:**

**Given** a source subject, outline node, diagnostic, or rendered occurrence in the M22 sample
**When** I reveal or select the subject across IDE surfaces
**Then** the same canonical subject and occurrence identity is used
**And** outline navigation keeps the same `.athena` editor tab
**And** layout optimization does not introduce frontend-owned semantic resolution

### Story 5.3: Preserve accepted graph workbench canvas behavior

As a reviewer,
I want M22 optimization and round-trip work to preserve the accepted graph workbench surface,
So that layout intelligence does not regress M20/M21 UI behavior.

**Acceptance Criteria:**

**Given** the M22 graph workbench is open
**When** optimization, ELK-assisted layout, or round-trip preview behavior is active
**Then** the stage grid remains the coordinate surface
**And** sheet and component bodies do not hide the grid
**And** `Cabinet Main` information remains in the top information popover only
**And** floating controls remain transparent overlays

### Story 5.4: Add M22 boundary and deferred-scope regression checks

As a product reviewer,
I want executable checks and documentation for M22 boundaries,
So that the milestone does not drift into deferred domains.

**Acceptance Criteria:**

**Given** the M22 PRD, architecture, sample project, and implementation artifacts
**When** boundary checks run
**Then** they confirm no public repository/import ecosystem, broad IEC/QElectroTech ingestion,
cabinet authoring, physical routing, AI layout, final solver-stack decision, or full EPLAN parity is
claimed by M22
**And** they confirm no hidden canvas state persists layout truth
**And** the usage or retrospective handoff records the deferred domains for future milestones
