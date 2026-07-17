---
title: Athena M22 - Governed Auto Layout And Layout Round-Trip Foundation
status: draft
created: 2026-07-17
updated: 2026-07-17
---

# PRD: Athena M22 - Governed Auto Layout And Layout Round-Trip Foundation

## 0. Document Purpose

M22 follows M21 by turning Athena's layout-intelligence foundation into a visibly better and
round-trippable engineering sheet workflow.

M21 introduced layout intent, layout facts, schematic region facts, schematic route facts, label
facts, and Theia proof. The visible result is still far from the professional engineering layout
standard shown in the EPLAN-style references under `draft/screenshort`. M22 exists to close the
next credibility gap:

> Can Athena automatically improve schematic layout quality and persist user-approved layout
> adjustments as governed `.athena` intent instead of canvas-local state?

M22 is not full EPLAN parity, not a public repository/import milestone, not a full IEC library
milestone, and not a cabinet/physical-routing milestone. It is the first governed auto-layout and
layout round-trip milestone.

## 1. Vision

Athena should help engineers produce professional schematic sheets without making the canvas the
source of truth. A user should be able to open a real `.athena` project, see Athena produce a more
credible layout than M21, adjust placement where needed, and have those adjustments reflected back
as governed layout intent.

The target pipeline is:

```text
.athena semantic source
    -> projection
    -> presentation / sheet IR
    -> layout intent
    -> Athena layout rules
    -> optional ELK adapter
    -> normalized Athena layout facts
    -> renderer
    -> user adjustment intent
    -> governed `.athena` layout hints
```

ELK or any other helper may assist. It must not become Athena's architecture, semantic authority, or
persistence format.

## 1.1 Why Now

M19 proved Athena could publish a schematic sheet. M20 made the sheet surface acceptable in Theia.
M21 introduced governed layout-intelligence contracts. The remaining customer-facing problem is that
the sheet still does not look intelligent enough.

If M22 moves to broad component libraries, repository/import infrastructure, or AI layout before
layout quality and round-trip behavior are credible, Athena risks scaling a weak presentation
surface. M22 should make the core schematic workflow visibly stronger first.

## 2. Target User

### 2.1 Jobs To Be Done

- Aaron needs to open the M22 sample project and see obvious layout improvement over M21.
- Maya needs to adjust a schematic placement without losing source, outline, Problems, or sheet
  identity.
- Priya needs to present Athena as a product that can generate and refine a credible engineering
  sheet, not only a semantic model with a graph view.
- Winston needs ELK and future layout helpers to remain subordinate to Athena's governed layout
  contracts.

### 2.2 Non-Users

- Teams expecting full EPLAN parity
- Teams expecting complete IEC/QElectroTech library ingestion
- Teams expecting public Maven/npm-style repository or marketplace behavior
- Teams expecting cabinet authoring or physical wire routing
- Teams expecting free-form drawing edits saved as canvas state

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a visibly improved schematic sheet.**
  - **Persona + context:** Aaron is reviewing whether Athena can move beyond M21's foundation-level
    layout.
  - **Entry state:** `examples/m22/sample-project` is open in the Athena Theia IDE.
  - **Path:** Aaron opens the main schematic `.athena` file, opens Graphical View, compares the sheet
    with M21 and the references under `draft/screenshort`, and inspects grouping, spacing, routing,
    labels, and sheet readability.
  - **Climax:** The sheet is visibly more professional than M21: fewer overlaps, clearer zones,
    cleaner orthogonal routing, and better label placement.
  - **Resolution:** Aaron can show the result as a real customer-facing auto-layout proof.

- **UJ-2. Maya adjusts layout and sees governed source intent.**
  - **Persona + context:** Maya needs to refine a schematic without turning Athena into a drawing
    editor.
  - **Entry state:** A schematic sheet is open with a selected component or route.
  - **Path:** Maya drags or commands a placement adjustment, Athena evaluates it as a layout intent,
    and the IDE applies a governed source edit or mutation preview.
  - **Climax:** The `.athena` source changes through a layout hint or layout block, not hidden canvas
    state.
  - **Resolution:** Maya can close and reopen the project and get the same layout from source.

- **UJ-3. Winston validates the ELK boundary.**
  - **Persona + context:** Winston is checking that Athena has not outsourced architecture to ELK.
  - **Entry state:** The M22 auto-layout proof is implemented.
  - **Path:** Winston inspects layout intent, ELK adapter input/output, normalized Athena layout
    facts, and renderer consumption.
  - **Climax:** ELK output is normalized into Athena facts and cannot define semantic meaning or
    persistence.
  - **Resolution:** Athena can use a powerful helper without losing EngineeringOS authority.

## 3. Glossary

- **Governed Auto Layout** - Automatic layout improvement driven by Athena layout intent and rules,
  optionally assisted by helper adapters, with normalized Athena layout facts as output.
- **ELK Adapter** - A subordinate integration that converts Athena layout intent into helper input
  and normalizes helper output back into Athena-owned layout facts.
- **Layout Round-Trip** - The ability for a user-visible layout adjustment to become governed source
  intent and then reproduce the same layout later.
- **Layout Hint** - Source-level or model-level authored intent that constrains placement, grouping,
  alignment, or routing without becoming raw pixel truth.
- **Adjustment Intent** - A user action such as moving a component, aligning a group, or selecting a
  routing preference, expressed as a semantic/projection command.
- **Professional Layout Acceptance** - A review standard based on engineering readability: clear
  zones, readable labels, orthogonal routes, stable spacing, and recognizable schematic convention.

## 4. Features

### 4.1 Visible M22 Sample And Baseline Comparison

**Description:** M22 starts with a real Theia sample project and compares against M21.

#### FR-1: Provide an openable M22 sample project

Athena provides `examples/m22/sample-project` with real `.athena` files that exercise auto-layout,
layout adjustment, and layout-hint round-trip scenarios.

**Consequences:**

- The project opens through the normal Athena Theia workflow.
- Users do not need to inspect `.mjs` files to understand M22.
- The sample includes power source, protection, controller, HMI, terminal block, and load subjects.
- The sample includes a baseline scenario that demonstrates improvement over M21.

#### FR-2: Define professional layout acceptance references

Athena documents the M22 visual acceptance expectations using `draft/screenshort` references and
M21 comparison criteria.

**Consequences:**

- The PRD and usage docs state what "better than M21" means.
- Acceptance checks cover spacing, grouping, routing, label readability, and sheet scanability.
- The acceptance bar does not claim full EPLAN parity.

### 4.2 Governed Auto Layout With Optional ELK Adapter

**Description:** Athena introduces an auto-layout path that may use ELK through an adapter boundary.

#### FR-3: Add an Athena layout-solver boundary

Athena can turn layout intent, layout rules, and existing layout facts into improved layout facts
through a governed layout-solver boundary.

**Consequences:**

- The solver consumes Athena layout intent and facts.
- The solver emits normalized Athena layout facts.
- The renderer remains paint-only.
- The same governed input produces stable output.

#### FR-4: Add an ELK adapter spike without final stack selection

Athena can evaluate ELK-style layout assistance behind the layout-solver boundary.

**Consequences:**

- ELK input is derived from Athena layout intent, not raw renderer DOM.
- ELK output is normalized into Athena layout facts before rendering.
- Athena rules retain authority over engineering role, zone, representation family, and persistence.
- M22 does not choose ELK as the final architecture or sole layout engine.

#### FR-5: Improve visible schematic layout quality

Athena visibly improves placement, grouping, orthogonal routing, and label placement in the M22
sample project.

**Consequences:**

- Power, protection, controller, HMI, terminals, and load path are visually identifiable.
- Routes avoid obvious major overlaps in the acceptance sheet.
- Labels avoid obvious conflicts with their own anchors and primary routes.
- The sheet remains grid-backed and readable in the existing Theia graph workbench.

### 4.3 Layout Adjustment And `.athena` Round-Trip

**Description:** M22 turns user layout adjustments into governed source/model intent.

#### FR-6: Capture canvas adjustments as layout adjustment intents

Athena can capture a user placement or alignment adjustment as an explicit layout adjustment intent.

**Consequences:**

- Adjustment payloads carry canonical subject, occurrence, view, sheet, and source identities.
- Adjustment intent goes through the existing governed mutation authority.
- The canvas never persists hidden layout truth by itself.

#### FR-7: Persist approved adjustments as `.athena` layout hints

Athena can reflect approved layout adjustments in `.athena` as governed layout hints or layout
blocks.

**Consequences:**

- Closing and reopening the project reproduces the adjusted layout from source.
- The source edit is reviewable in the editor and SCM.
- Source, outline, Problems, and sheet identity remain coherent after the round-trip.
- Layout hints express engineering intent, not arbitrary DOM or canvas state.

#### FR-8: Provide mutation preview before applying layout source edits

Athena shows an inspectable preview before applying layout-hint edits.

**Consequences:**

- The user can accept or reject a proposed source edit.
- The preview names the affected subject and layout intent.
- Rejected edits do not modify source or canvas state.

### 4.4 IDE Coherence And Guardrails

**Description:** Auto layout and round-trip edits must preserve the accepted Theia workflow.

#### FR-9: Preserve source, outline, Problems, and graph coherence

Athena keeps canonical identities stable across source, outline, Problems, and Graphical View while
auto layout and layout round-trip are active.

**Consequences:**

- Opening `src/02-layout-intelligence-acceptance.athena` projects that active file, not the seed
  file.
- Outline navigation keeps the same `.athena` editor tab.
- Diagnostics and selection reveal use the same canonical identities as rendered sheet occurrences.

#### FR-10: Preserve accepted graph workbench canvas behavior

Athena keeps the accepted M20/M21 graph workbench behavior.

**Consequences:**

- The grid remains the coordinate surface.
- Sheet and component bodies do not hide the grid unless a governed mode explicitly changes this.
- `Cabinet Main` information remains in the top information popover.
- Floating controls remain transparent overlays.

#### FR-11: Keep M22 boundaries explicit

Athena keeps M22 scoped to governed auto layout and layout round-trip.

**Consequences:**

- No public package registry or marketplace work.
- No full IEC/QElectroTech library ingestion.
- No cabinet authoring or physical routing.
- No full EPLAN parity claim.
- No final ELK/layout-stack decision.
- No sheet-local drag-save truth.

## 5. Non-Goals

- Full EPLAN parity
- Full IEC/QElectroTech library ingestion
- Public repository/import ecosystem work
- Cabinet authoring
- Physical wire, harness, cable tray, 3D installation, or cabinet routing
- AI layout
- Final ELK or layout-stack selection
- Free-form drawing editor behavior
- Persisting raw canvas coordinates as hidden state

## 6. MVP Scope

### 6.1 In Scope

- `examples/m22/sample-project` with real `.athena` files
- Theia-visible auto-layout proof
- Layout-solver boundary
- ELK adapter spike behind Athena layout contracts
- Improved schematic placement, grouping, routing, and label readability in one governed acceptance
  sheet
- Layout adjustment intent from the graph workbench
- Approved adjustment reflected into `.athena` layout hints or layout blocks
- Source/outline/Problems/sheet coherence after layout round-trip
- Regression coverage for active source projection selection
- Boundary tests proving ELK, canvas, and deferred scopes remain subordinate

### 6.2 Out Of Scope

- Full symbol/library breadth
- Broad multi-page engineering packages
- Full authoring depth
- Physical layout or cabinet routing
- Public ecosystem infrastructure
- AI optimization
- Final solver-stack decision

## 7. Success Metrics

**Primary**

- **SM-1:** A reviewer opens the M22 sample project in Theia and sees visible layout improvement over
  M21.
- **SM-2:** The M22 acceptance sheet exposes power, protection, controller, HMI, terminals, and load
  path without reading implementation code.
- **SM-3:** ELK-assisted output, if used, is normalized into Athena layout facts and does not become
  authority.
- **SM-4:** A user-approved placement adjustment is reflected into `.athena` layout intent and
  reproduces after reopening.
- **SM-5:** Source, outline, Problems, and Graphical View remain coherent for the active `.athena`
  file.
- **SM-6:** Repeated runs on the same governed input produce stable layout facts and stable visible
  presentation.

**Counter-metrics**

- **SM-C1:** Do not optimize for full EPLAN parity in M22.
- **SM-C2:** Do not hide layout state in the canvas.
- **SM-C3:** Do not let ELK become architecture or persistence format.
- **SM-C4:** Do not widen into repository/import, full IEC breadth, cabinet authoring, or physical
  routing.

## 8. Assumptions Index

- M21 layout intent, layout engine, route facts, label facts, and graph workbench proof remain
  available.
- Theia remains the only frontend scope for M22.
- ELK can be evaluated locally as an optional helper without forcing a final layout-stack decision.
- `.athena` can accept a small layout-hint syntax or layout block without destabilizing the language.
- M8-style governed mutation authority remains the right path for source-changing layout
  adjustments.
- The references under `draft/screenshort` are visual acceptance inspiration, not full parity scope.

## 9. Open Questions

- What is the smallest `.athena` layout-hint syntax that can represent M22 adjustment intent without
  overfitting to canvas coordinates?
- Should ELK be included directly as a dependency in M22 or isolated behind an experimental adapter
  package?
- Which exact `draft/screenshort` images define the M22 acceptance comparison?
- Should M22 persist only component placement hints, or also routing and label hints?
- Should layout adjustment preview reuse guided authoring UI patterns from M15/M21 or introduce a
  dedicated layout-edit preview surface?
