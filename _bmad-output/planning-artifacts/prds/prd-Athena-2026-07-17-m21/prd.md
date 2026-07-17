---
title: Athena M21 - Engineering Layout Intelligence Foundation
status: draft
created: 2026-07-17
updated: 2026-07-17
---

# PRD: Athena M21 - Engineering Layout Intelligence Foundation

## 0. Document Purpose
M21 follows M20 by proving that Athena can make better engineering layout decisions from governed
semantic intent. M20 made the sheet acceptable as a professional surface. M21 must make the sheet
more intelligent: clearer placement, more readable routing, stronger grouping, and visible layout
improvement inside the Theia IDE.

This is not a repository/import milestone, not a full IEC library milestone, and not a generic canvas
polish milestone. It is the first governed layout-intelligence milestone.

## 1. Vision
Athena should produce schematic layouts that engineers recognize as intentional engineering
presentation, not generic graph arrangement. The system should understand that a power supply, PLC,
HMI, motor starter, terminal block, conductor, and cross-reference have engineering roles that affect
where they belong and how they should be read.

M21 proves this through a visible IDE workflow:

```text
Semantic Engineering Model
    -> Projection
    -> Presentation IR
    -> Layout Intent
    -> Layout Engine
    -> Layout Facts
    -> Renderer
    -> Theia IDE Proof
```

The layout layer may use adapters later, including ELK-style engines, but no adapter becomes the
architecture or the authority. Athena owns the layout contract.

## 1.1 Why Now
M19 proved Athena can publish a governed schematic sheet. M20 proved that sheet can become visually
acceptable in the IDE. The next product risk is whether Athena can improve the schematic through
engineering layout intelligence instead of leaving the result as a manually adjusted or generic graph
view.

If M21 skips this and moves directly to repository/import or broad component libraries, Athena risks
scaling a surface that still does not feel like an engineering tool. M21 should strengthen the core
customer-facing workflow before widening the ecosystem.

## 2. Target User

### 2.1 Jobs To Be Done
- Aaron needs to open the M21 sample project in Theia and see a clear improvement over the M20 sheet.
- Maya needs source, outline, Problems, and sheet navigation to remain coherent while layout improves.
- Priya needs a customer-facing proof that Athena's governed model can produce professional schematic
  structure, not only a corrected canvas.
- Winston needs the architecture to keep layout intelligence downstream of semantic authority and
  upstream of rendering.

### 2.2 Non-Users (v1)
- Teams expecting public Maven/npm-style package repository behavior
- Teams expecting complete IEC library ingestion
- Teams expecting cabinet authoring or physical-layout optimization
- Teams expecting full EPLAN parity
- Teams expecting direct canvas drag-save authoring

### 2.3 Key User Journeys
- **UJ-1. Aaron opens the M21 sample project and sees intelligent layout.**
  - **Persona + context:** Aaron is reviewing whether Athena is moving from acceptable presentation
    toward credible engineering layout.
  - **Entry state:** `examples/m21/sample-project` is open in the Athena Theia IDE.
  - **Path:** Aaron opens the `.athena` source, switches to the schematic sheet, scans grouping,
    routing, labels, and cross-references, then compares the result to the M20 baseline.
  - **Climax:** The sheet shows obvious engineering intent: related subjects are grouped, labels are
    readable, routes are clean, and the grid remains useful.
  - **Resolution:** Aaron can show the IDE workflow as a customer-facing proof without explaining
    internal fixture scripts.

- **UJ-2. Maya follows identity across source, outline, Problems, and sheet.**
  - **Persona + context:** Maya is editing and reviewing a governed Athena project.
  - **Entry state:** The source editor, outline, Problems, and schematic sheet are available.
  - **Path:** Maya selects a source subject, uses outline navigation, follows a diagnostic or reveal,
    and checks the corresponding sheet occurrence.
  - **Climax:** Improved layout does not break canonical identity or open duplicate editor tabs.
  - **Resolution:** Maya trusts the IDE workflow while layout intelligence changes the presentation.

- **UJ-3. Priya validates layout quality through repeatable proof.**
  - **Persona + context:** Priya needs a product acceptance check, not a developer-only test result.
  - **Entry state:** The M21 sample project and visual proof tests are available.
  - **Path:** Priya runs the documented proof path, opens the IDE, and reviews screenshot or E2E
    evidence for grid, grouping, routes, labels, and popover behavior.
  - **Climax:** The evidence shows the same governed input produces the same readable layout.
  - **Resolution:** Priya can approve the milestone without manually rediscovering how the proof
    works.

## 3. Glossary
- **Engineering Layout Intelligence** - Governed rules that arrange engineering representations
  according to domain purpose, not generic graph aesthetics.
- **Sheet Layout Rules** - Deterministic placement, grouping, spacing, routing-lane, and label rules
  owned by Athena's projection/layout layer.
- **Layout Intent** - Explainable intermediate layout meaning such as engineering role, preferred
  zone, grouping priority, alignment preference, and relationship constraints before numeric
  placement is solved.
- **Layout Engine** - A governed engine or strategy boundary that turns layout intent and rules into
  layout facts.
- **Layout Facts** - Immutable projection output consumed by the renderer and IDE to paint the
  sheet.
- **Layout Adapter** - Optional helper integration, such as ELK, that can propose graph arrangement
  but cannot define engineering meaning or final authority.
- **Visual Proof Path** - The documented, repeatable IDE workflow and automated evidence used to
  prove a user-visible presentation milestone.
- **Semantic Grouping** - Arrangement of related subjects by engineering role, such as power,
  control, terminals, PLC, HMI, or motor starter.
- **Orthogonal Routing** - Horizontal and vertical conductor routing that follows engineering sheet
  conventions.
- **Schematic Conductor Routing** - Sheet-level symbolic wire routing between schematic endpoints;
  not cabinet, harness, cable tray, or physical installation routing.
- **Label Avoidance** - Rules that keep labels, terminal names, device names, and cross-references
  from overlapping important sheet content.

## 4. Features

### 4.1 Visible IDE Proof Baseline
**Description:** M21 must begin with a visible Theia workflow. The sample project is part of the
product proof, not a late demo artifact.

#### FR-1: Provide an openable M21 sample project
Athena provides `examples/m21/sample-project` with real `.athena` source files that exercise the M21
layout-intelligence scenarios.
**Consequences (testable):**
- The sample project opens through the normal Athena Theia workflow.
- The project does not require users or customers to inspect `.mjs` files to understand the feature.
- The project includes the scenarios needed to review grouping, routing, labels, identity, and
  acceptance.

#### FR-2: Prove M21 through IDE-visible evidence
Athena provides automated or semi-automated proof that the M21 layout appears correctly in the IDE.
**Consequences (testable):**
- The proof checks the graph workbench surface, not only model fixtures.
- Screenshot, Playwright-style, or equivalent evidence verifies grid visibility, grouping, routes,
  labels, and popover behavior.
- Runtime logs and diagnostics are checked before the milestone is treated as complete.

### 4.2 Governed Layout Rule Model
**Description:** Athena introduces a layout rule model that expresses engineering arrangement
constraints without moving authority into Theia, the renderer, or an external layout library.

#### FR-3: Define layout intent, rules, and facts as governed layout contracts
Athena can model layout intent, placement rules, spacing, grouping, routing lanes, and label rules as
explicit governed layout contracts.
**Consequences (testable):**
- Layout intent captures engineering role, preferred zone, priority, alignment, and relationship
  preferences before numeric placement is solved.
- Layout intent and layout rules are separate from renderer-local CSS, DOM state, or canvas
  interaction state.
- The same governed input produces stable layout facts.
- The renderer consumes layout facts and does not invent engineering meaning.

#### FR-4: Keep adapters subordinate to Athena layout contracts
Athena can use layout helpers only through an adapter boundary that preserves Athena's authority over
engineering layout facts.
**Consequences (testable):**
- No story makes ELK or any other helper the architecture.
- Adapter output is normalized into Athena layout facts before rendering.
- Engineering ordering, grouping, and purpose remain governed by Athena rules.

### 4.3 Semantic Grouping And Placement
**Description:** M21 improves layout by arranging subjects according to engineering role and
presentation purpose.

#### FR-5: Group related engineering subjects by role
Athena can place related subjects so the schematic reads by engineering intent rather than raw graph
topology.
**Consequences (testable):**
- Power supply, PLC, HMI, motor starter, terminal block, and signal subjects can appear in coherent
  regions.
- Power-flow subjects can be arranged so source, protection, switching, and load are readable in a
  consistent schematic direction.
- Control subjects can be arranged so controller, HMI, sensors, actuators, and terminals expose a
  clear control hierarchy.
- Related terminals, connectors, and labels stay near their governed subjects.
- Grouping remains deterministic and explainable from layout facts.

#### FR-6: Preserve representation purpose during placement
Athena can distinguish schematic representation purpose from future cabinet, terminal-plan, or other
representation families.
**Consequences (testable):**
- M21 layout rules target the schematic sheet family.
- Future families are not accidentally introduced as M21 scope.
- One semantic subject may have placement facts tied to the active representation family.

### 4.4 Routing And Label Readability
**Description:** M21 improves conductor routing and label placement enough for engineers to read the
sheet without fighting overlaps or ambiguous paths.

#### FR-7: Produce deterministic schematic conductor routes
Athena can route schematic conductors through stable orthogonal path facts that respect endpoints and
routing lanes.
**Consequences (testable):**
- Routes connect governed endpoints without renderer guessing.
- Routes avoid obvious collisions with major component bodies where feasible in M21 scope.
- Routes represent schematic topology only; they do not imply cabinet, harness, cable tray, or
  physical wire path routing.
- Repeated runs produce the same route facts for the same input.

#### FR-8: Keep labels and cross-references readable
Athena can place labels, terminal names, device names, and cross-reference markers so they do not
mask important schematic content.
**Consequences (testable):**
- Labels do not overlap their own subject in the acceptance fixture.
- Cross-reference markers remain tied to canonical subjects and occurrences.
- Label avoidance is rule-based and deterministic, not manual pixel editing.
- An engineer can quickly identify the power source, protection, controller, terminals, and primary
  load path in the acceptance sheet.

### 4.5 IDE Coherence During Layout Intelligence
**Description:** M21 must preserve the M20 user-visible behavior while layout intelligence changes
the sheet.

#### FR-9: Preserve source, outline, Problems, and sheet identity
Athena keeps canonical identity round-trips stable across source, outline, Problems, and sheet
selection.
**Consequences (testable):**
- Outline selection does not open duplicate editor tabs for the same `.athena` file.
- Source and Problems reveal the same canonical subject and occurrence shown on the sheet.
- Layout rule changes do not break selection or inspector behavior.

#### FR-10: Preserve accepted M20 canvas chrome behavior
Athena keeps the accepted M20 graph workbench behavior while improving layout.
**Consequences (testable):**
- The stage grid remains the main canvas coordinate surface.
- Sheet and component bodies do not hide the coordinate grid unless a governed drawing mode
  explicitly requires it.
- `Cabinet Main` information remains in the top information popover, not in the sheet canvas.
- Top and bottom controls remain transparent canvas overlays.

### 4.6 Scope Boundaries
**Description:** M21 stays focused on layout intelligence and visible IDE proof.

#### FR-11: Keep M21 bounded
Athena keeps public repository/import ecosystem work, full IEC breadth, cabinet authoring, full EPLAN
parity, and uncontrolled drag-save canvas position out of M21.
**Consequences (testable):**
- No M21 story requires a public package registry or marketplace.
- No M21 story imports a complete IEC/QElectroTech library.
- No M21 story implements cabinet authoring or physical-layout optimization.
- No M21 story persists arbitrary canvas edits as semantic truth.

## 5. Non-Goals
- Public Maven/npm-style package repository or marketplace
- Broad package/import ecosystem expansion
- Full IEC symbol library ingestion
- Full EPLAN parity
- Cabinet authoring or physical-layout optimization
- Desktop viewer scope
- User drag-save as sheet-local truth
- Frontend-owned semantic or layout authority
- Final layout-engine stack selection

## 6. MVP Scope

### 6.1 In Scope
- Openable `examples/m21/sample-project` with real `.athena` files
- IDE-visible proof path for M21 layout intelligence
- Governed layout rule model for schematic sheets
- Layout intent, layout rules, layout-engine boundary, and deterministic layout facts
- Deterministic grouping, placement, schematic routing-lane, and label-avoidance facts
- Schematic topology routing and readability improvements over the M20 baseline
- Source, outline, Problems, and sheet coherence under improved layout
- Visual regression or screenshot-style checks for the Theia graph workbench

### 6.2 Out of Scope for MVP
- Repository/import ecosystem
- Full IEC component breadth
- Cabinet authoring or full cabinet preview expansion
- Desktop viewer implementation
- Final ELK/layout-stack decision
- AI-driven layout optimization
- Manual drawing editor parity

## 7. Success Metrics

**Primary**
- **SM-1:** A reviewer can open the M21 sample project in Theia and see layout improvement over M20
  without reading implementation scripts.
- **SM-2:** The acceptance sheet shows coherent engineering grouping and readable conductor/label
  placement with no obvious major overlaps.
- **SM-2a:** A reviewer can quickly identify power source, protection, controller, terminals, and
  primary load path from the acceptance sheet without reading implementation code.
- **SM-3:** Repeated runs produce stable layout facts and stable IDE-visible presentation.
- **SM-4:** Source, outline, Problems, and sheet selection remain coherent while layout intelligence
  is active.
- **SM-5:** Visual proof checks catch regressions in grid visibility, overlays, labels, routing, and
  information popover behavior.

**Counter-metrics**
- **SM-C1:** Do not optimize for broad library or repository scope in M21.
- **SM-C2:** Do not make an external layout helper the authority.
- **SM-C3:** Do not hide customer proof behind `.mjs` fixtures.
- **SM-C4:** Do not accept sheet-local drag/save behavior as semantic truth.

## 8. Assumptions Index
- The M20 sheet composition, layout model, and accepted graph workbench behavior remain available.
- Theia remains the primary user-facing proof surface for M21.
- The M21 proof can reuse and extend the M20 sample-project pattern.
- Layout intelligence can start with deterministic rules before optimization or AI-assisted layout.
- Layout intent is useful now for explainable placement and later as substrate for AI-assisted layout.
- External layout helpers may be researched, but choosing a final stack is not required for M21.

## 9. Open Questions
- Which exact M20 fixture becomes the baseline comparison for the M21 acceptance sheet?
- Should M21 include a minimal adapter spike behind the Athena layout contract, or defer all adapter
  work to M22?
- What screenshot/E2E framework should be standardized for Theia graph workbench verification?
- Which engineering roles must be present in the first M21 sample: PLC, HMI, motor starter, terminal
  block, power supply, or all of them?
