---
stepsCompleted:
  - extract-m21-requirements
  - design-m21-epics
  - create-m21-stories
  - validate-m21-coverage
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m21/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m21/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m21/ARCHITECTURE-SPINE.md
  - _bmad-output/implementation-artifacts/m20/m20-ui-acceptance-deep-retrospective-2026-07-17.md
  - _bmad-output/implementation-artifacts/m20/epic-4-retro-2026-07-17.md
---

# Athena - M21 Epic Breakdown

## Overview

M21 turns the accepted M20 schematic sheet into the first governed layout-intelligence milestone.
The goal is to make Athena improve schematic grouping, schematic conductor routing, label
readability, and engineering layout explanation while keeping authority upstream and proving the
result inside the Theia IDE.

M21 must not repeat the M20 proof mistake. The visible IDE proof path and openable sample project are
part of the milestone from the first epic, not a late demo.

## Requirements Inventory

### Functional Requirements

FR1: Provide `examples/m21/sample-project` with real `.athena` files that exercise M21 layout
intelligence scenarios.

FR2: Prove M21 through IDE-visible graph workbench evidence, including grid visibility, grouping,
routes, labels, popover behavior, runtime logs, and diagnostics.

FR3: Define layout intent, layout rules, and layout facts as governed layout contracts separate from
renderer-local CSS, DOM state, canvas interaction state, or frontend inference.

FR4: Keep adapters subordinate to Athena layout contracts so ELK or any other helper cannot become
the architecture or authority.

FR5: Group related engineering subjects by role so the schematic reads by engineering intent,
including power flow and control hierarchy.

FR6: Preserve representation purpose during placement so M21 targets the schematic sheet family and
does not accidentally introduce cabinet or terminal-plan scope.

FR7: Produce deterministic schematic conductor routes through stable orthogonal path facts that
respect governed endpoints and schematic routing lanes.

FR8: Keep labels, terminal names, device names, and cross-reference markers readable and tied to
canonical subjects and occurrences.

FR9: Preserve source, outline, Problems, and sheet identity while layout intelligence changes the
sheet.

FR10: Preserve accepted M20 canvas behavior: stage grid as coordinate surface, transparent overlays,
popover-only `Cabinet Main` information, and same-tab `.athena` outline navigation.

FR11: Keep M21 bounded away from public repository/import ecosystem work, full IEC breadth, cabinet
authoring, full EPLAN parity, uncontrolled drag-save canvas position, AI layout, and final
layout-stack selection.

### NonFunctional Requirements

NFR1: The same governed input must produce stable layout intent, layout facts, route facts, and
IDE-visible presentation.

NFR2: Layout snapshots must be immutable, ordered, replayable, and derived from governed
projection/presentation inputs.

NFR3: Theia and the renderer must remain consumers of layout snapshots, not owners of engineering
meaning or layout authority.

NFR4: M21 proof must include user-visible IDE evidence, not only model or `.mjs` fixture checks.

NFR5: M21 must stay inside the existing Athena/Theia product shell and exclude desktop-viewer scope.

NFR6: Schematic route terminology must stay separate from cabinet, harness, cable tray, 3D
installation, and physical routing terminology.

### Additional Requirements

- Architecture AD-1: Semantic authority remains upstream; layout intelligence may derive intent and
  facts but may not redefine canonical semantics.
- Architecture AD-2: Layout intent is first-class and carries engineering role, preferred zone,
  priority, alignment, and relationship constraints before solved coordinates.
- Architecture AD-3: Layout engine is a strategy boundary shared by rule-based logic, adapters, and
  future engines.
- Architecture AD-4: Layout facts are the renderer contract; renderer paints only.
- Architecture AD-5: Adapter output must normalize into Athena layout facts and remain subordinate.
- Architecture AD-6: Route facts describe schematic topology only.
- Architecture AD-7: Engineering readability beats generic graph neatness.
- Architecture AD-8: Layout intent, facts, route facts, and label facts carry canonical identities
  needed for IDE coherence.
- Architecture AD-9: The openable M21 sample project and graph workbench evidence are acceptance
  gates.
- Architecture AD-10: Accepted M20 canvas behavior carries forward.
- Architecture AD-11: Ecosystem expansion, cabinet authoring, physical routing, AI layout, full
  EPLAN parity, and sheet-local drag-save truth stay deferred.

### UX Design Requirements

No separate M21 UX design contract exists. The accepted M20 canvas invariants are treated as
architecture and acceptance requirements:

UX-DR1: The stage grid remains the canvas coordinate surface.

UX-DR2: Sheet and component bodies do not hide the coordinate grid unless a future governed mode
explicitly requires it.

UX-DR3: `Cabinet Main` details remain in the top information popover only.

UX-DR4: Top and bottom controls remain transparent canvas overlays.

UX-DR5: Outline navigation keeps the same `.athena` editor tab.

### FR Coverage Map

FR1: Epic 1 - visible IDE proof baseline and openable sample project.

FR2: Epic 1 and Epic 4 - IDE-visible evidence and visual regression proof.

FR3: Epic 2 - governed layout intent, rules, facts, and snapshots.

FR4: Epic 2 - layout strategy and adapter boundary.

FR5: Epic 3 - semantic grouping and engineering placement.

FR6: Epic 3 - representation-purpose preservation during schematic placement.

FR7: Epic 3 - deterministic schematic conductor routing.

FR8: Epic 3 - labels, terminals, device names, and cross-reference readability.

FR9: Epic 4 - source, outline, Problems, and sheet coherence.

FR10: Epic 1 and Epic 4 - accepted M20 canvas behavior preservation.

FR11: Epic 4 - boundary tests and deferred-scope documentation.

## Epic List

### Epic 1: Visible M21 IDE Proof Baseline
Engineers and reviewers can open a real M21 Athena project in Theia and see the accepted M20 canvas
behavior preserved before deeper layout intelligence is added.
**FRs covered:** FR1, FR2, FR10

### Epic 2: Governed Layout Intent And Strategy Contract
Architects and implementers can rely on explicit layout intent, constraints, strategy boundaries,
and layout facts instead of renderer-local placement logic.
**FRs covered:** FR3, FR4

### Epic 3: Engineering Schematic Readability Intelligence
Engineers can see Athena arrange schematic subjects by engineering role, route schematic conductors
deterministically, and keep labels readable.
**FRs covered:** FR5, FR6, FR7, FR8

### Epic 4: IDE Coherence And Scope Guardrails
Reviewers can verify that layout intelligence preserves source/outline/Problems/sheet identity,
keeps accepted graph workbench behavior, and stays inside M21 boundaries.
**FRs covered:** FR2, FR9, FR10, FR11

## Epic 1: Visible M21 IDE Proof Baseline

Engineers and reviewers can open a real M21 Athena project in Theia and see the accepted M20 canvas
behavior preserved before deeper layout intelligence is added.

### Story 1.1: Create the openable M21 sample project

As a reviewer,
I want an M21 sample project with real `.athena` files,
So that I can inspect M21 layout work through the normal Athena Theia workflow.

**Acceptance Criteria:**

**Given** the M21 milestone workspace
**When** I inspect `examples/m21/sample-project`
**Then** it contains real `.athena` source files covering the M21 acceptance scenarios
**And** the documented launch path opens the project in the Athena Theia IDE without requiring users
to inspect `.mjs` files
**And** the sample starts from the accepted M20 sheet behavior as the visible baseline

### Story 1.2: Add graph workbench visual proof for the sample project

As a reviewer,
I want executable IDE-visible proof for the M21 graph workbench,
So that the milestone is judged through the actual customer-facing surface.

**Acceptance Criteria:**

**Given** the M21 sample project
**When** the visual proof path runs
**Then** it verifies the graph workbench surface rather than only model fixtures
**And** it checks grid visibility, sheet transparency, canvas overlays, information popover behavior,
and absence of stale M20-forbidden canvas elements
**And** runtime diagnostics or logs needed for the proof are recorded or checked

### Story 1.3: Preserve the accepted M20 canvas contract in M21

As an engineer,
I want the M21 sheet to preserve the accepted M20 canvas behavior,
So that layout intelligence does not regress the IDE surface that was already accepted.

**Acceptance Criteria:**

**Given** the M21 graph workbench surface
**When** the sheet is opened and inspected
**Then** the stage grid remains the coordinate surface
**And** sheet and component bodies do not hide the grid
**And** `Cabinet Main` information remains in the top information popover only
**And** top and bottom controls remain transparent canvas overlays

## Epic 2: Governed Layout Intent And Strategy Contract

Architects and implementers can rely on explicit layout intent, constraints, strategy boundaries,
and layout facts instead of renderer-local placement logic.

### Story 2.1: Introduce layout intent and layout snapshot contracts

As an architect,
I want layout intent to be explicit before solved layout facts,
So that engineering layout decisions stay explainable and do not collapse into opaque coordinates.

**Acceptance Criteria:**

**Given** governed projection and Presentation IR input
**When** M21 derives layout input for a schematic sheet
**Then** it emits layout intent carrying role, preferred zone, priority, alignment, and relationship
constraints where applicable
**And** the layout snapshot preserves canonical subject, occurrence, snapshot, and source-span
identity
**And** the contract is separate from renderer CSS, DOM state, and canvas interaction state

### Story 2.2: Add the rule-based schematic layout strategy boundary

As an implementer,
I want a layout strategy boundary that turns intent and rules into facts,
So that M21 can start with deterministic rules without locking the future layout engine.

**Acceptance Criteria:**

**Given** a layout intent snapshot
**When** the rule-based schematic strategy runs
**Then** it emits deterministic placement and layout facts for the same governed input
**And** the renderer consumes those facts without solving layout locally
**And** the strategy boundary can be tested independently of Theia rendering

### Story 2.3: Guard the adapter boundary without choosing a final layout stack

As an architect,
I want external layout helpers to remain subordinate adapters,
So that ELK or any future helper cannot become Athena's layout authority.

**Acceptance Criteria:**

**Given** the M21 layout architecture
**When** adapter support or adapter documentation is inspected
**Then** any helper output must normalize into Athena layout facts
**And** engineering grouping, ordering, and schematic purpose remain governed by Athena layout intent
and rules
**And** no M21 artifact chooses a final external layout stack

## Epic 3: Engineering Schematic Readability Intelligence

Engineers can see Athena arrange schematic subjects by engineering role, route schematic conductors
deterministically, and keep labels readable.

### Story 3.1: Arrange schematic subjects by engineering role

As an engineer,
I want related schematic subjects grouped by engineering role,
So that the sheet reads by power, control, terminals, and load intent rather than generic graph
topology.

**Acceptance Criteria:**

**Given** an M21 schematic sample containing power supply, protection, controller, terminals, and a
primary load path
**When** layout intent and layout facts are produced
**Then** related subjects are grouped into coherent schematic regions
**And** power source, protection, controller, terminals, and primary load path are identifiable from
the layout
**And** grouping remains deterministic and explainable through layout intent and facts

### Story 3.2: Produce deterministic schematic conductor route facts

As an engineer,
I want schematic conductor routes to be deterministic and endpoint-aware,
So that wires read as schematic topology without implying physical routing.

**Acceptance Criteria:**

**Given** governed schematic endpoints in the M21 sample
**When** route facts are derived
**Then** route facts describe sheet-level schematic conductor topology between governed endpoints
**And** routes use stable orthogonal segments and routing lanes where applicable
**And** routes do not claim cabinet, harness, cable tray, 3D installation, or physical wire path
meaning

### Story 3.3: Keep labels and cross-references readable

As an engineer,
I want labels, terminal names, device names, and cross-references to stay readable,
So that layout intelligence improves engineering communication instead of only moving shapes.

**Acceptance Criteria:**

**Given** the M21 acceptance sheet
**When** labels and cross-references are placed
**Then** they remain tied to canonical subjects and occurrences
**And** they avoid obvious overlap with their own subject and primary routes
**And** a reviewer can quickly identify power source, protection, controller, terminals, and primary
load path without reading implementation code

## Epic 4: IDE Coherence And Scope Guardrails

Reviewers can verify that layout intelligence preserves source/outline/Problems/sheet identity,
keeps accepted graph workbench behavior, and stays inside M21 boundaries.

### Story 4.1: Preserve source, outline, Problems, and sheet identity

As an engineer,
I want layout intelligence to preserve canonical IDE navigation,
So that source, outline, Problems, and sheet selection remain one coherent workflow.

**Acceptance Criteria:**

**Given** a source subject, outline entry, diagnostic, or sheet occurrence in the M21 sample project
**When** I trigger reveal or selection
**Then** the same canonical subject and occurrence identity is used across source, outline,
Problems, and sheet
**And** outline navigation keeps the same `.athena` editor tab
**And** layout facts do not introduce frontend-owned semantic resolution

### Story 4.2: Add M21 visual regression and acceptance coverage

As a reviewer,
I want automated coverage for the customer-facing M21 layout proof,
So that visual and interaction regressions are caught before manual review.

**Acceptance Criteria:**

**Given** the M21 sample project and graph workbench
**When** the M21 regression suite runs
**Then** it covers layout intent/fact stability, schematic route facts, label readability, M20 canvas
invariants, and source/outline/sheet coherence
**And** screenshot, Playwright-style, or equivalent graph workbench evidence is produced or checked
**And** repeated runs on the same governed input remain stable

### Story 4.3: Keep M21 deferred boundaries explicit

As a product reviewer,
I want M21 scope boundaries to stay executable and visible,
So that layout intelligence does not drift into repository/import, IEC breadth, cabinet authoring, or
physical routing.

**Acceptance Criteria:**

**Given** the M21 PRD, architecture, epics, stories, and proof corpus
**When** boundary checks run
**Then** they confirm public repository/import ecosystem work, full IEC breadth, cabinet authoring,
full EPLAN parity, AI layout, final layout-stack selection, desktop viewer scope, and physical
routing are deferred
**And** no M21 story persists arbitrary canvas edits as semantic truth
**And** deferred boundaries are documented in the usage and retrospective handoff material
