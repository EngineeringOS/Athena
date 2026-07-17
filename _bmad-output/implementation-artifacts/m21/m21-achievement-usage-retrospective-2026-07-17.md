# Athena M21 Achievement, Usage, And Retrospective

Date: 2026-07-17
Milestone: M21 - Engineering Layout Intelligence Foundation
Status: completed

## Executive Summary

M21 moved Athena from an accepted schematic sheet surface to the first governed layout-intelligence
foundation. The milestone did not try to clone EPLAN, select ELK, build public package
infrastructure, or widen the IEC library. It focused on a narrower product risk:

> Can Athena turn governed engineering meaning into a more readable schematic layout inside the
> normal Theia IDE, while keeping semantic and layout authority upstream of the renderer?

The answer from M21 is yes at foundation level. Athena now has an openable M21 sample project, a
visible Theia proof path, layout intent contracts, a rule-based schematic layout strategy boundary,
schematic region facts, deterministic schematic route facts, label and cross-reference facts,
same-tab source navigation proof, and executable M21 boundary checks.

M21 is still not full professional EPLAN-level layout. It is the contract and proof foundation that
lets future milestones improve layout quality without turning the canvas into the source of truth.

## What M21 Delivered

### Epic 1 - Visible M21 IDE Proof Baseline

Delivered:

- Added `examples/m21/sample-project` with real `.athena` sources.
- Added a truthful `yarn start:m21` launch path that opens the sample project directly.
- Added graph workbench smoke evidence from the rendered Theia DOM.
- Preserved the accepted M20 canvas behavior: stage grid as coordinate surface, transparent sheet
  and component bodies, transparent floating controls, popover-only `Cabinet Main`, and whitespace
  close behavior.

Key lesson:

- Customer-facing proof must start with an openable IDE project. `.mjs` files are supporting
  automation, not the proof the user or customer is expected to understand.

### Epic 2 - Governed Layout Intent And Strategy Contract

Delivered:

- Added first-class layout intent snapshot contracts in `:kernel:layout-model`.
- Added `:kernel:layout-engine` as the layout strategy boundary.
- Added deterministic rule-based schematic placement facts.
- Added subordinate helper proposal normalization without choosing ELK or any final external layout
  stack.

Key lesson:

- Layout intent is the critical layer between Presentation IR and coordinates. It keeps engineering
  placement explainable and gives future rule, adapter, or AI engines a governed input contract.

### Epic 3 - Engineering Schematic Readability Intelligence

Delivered:

- Added explicit schematic region facts for power, control, terminal, load, and annotation zones.
- Added `:kernel:routing-model` for deterministic schematic route facts.
- Added schematic label and cross-reference facts with deterministic placement.
- Preserved canonical subject, occurrence, snapshot, route, endpoint, and label identities.

Key lesson:

- Engineering readability is not generic graph neatness. M21's useful unit is not "prettier nodes";
  it is inspectable facts that explain why a power source, protection, controller, terminals, and
  load path are readable.

### Epic 4 - IDE Coherence And Scope Guardrails

Delivered:

- Added same-tab `.athena` outline navigation proof.
- Extended sample-project checks for source identity vocabulary.
- Added M21 acceptance coverage guard.
- Added M21 boundary test covering PRD, architecture, epics, usage, sprint status, and contracts.

Key lesson:

- Layout intelligence must not break the normal IDE loop. Source, outline, Problems, and sheet
  surfaces must continue to share canonical identities.

## Usage Summary

### Customer-Facing IDE Path

Use the normal Athena Theia flow:

```powershell
Set-Location ide
yarn start:m21
```

This opens:

```text
examples/m21/sample-project
```

In the IDE, review these files:

- `src/01-baseline-sheet.athena` - accepted M20 canvas behavior baseline.
- `src/02-layout-intelligence-acceptance.athena` - power, control, terminal, and load readability
  scenario.
- `src/03-routing-and-label-readability.athena` - schematic conductor routing and label scenario.
- `src/04-boundary-scope.athena` - deferred-scope guardrail scenario.

Then open the Graphical View and inspect:

- the grid-backed stage as the coordinate surface
- transparent sheet and component bodies
- transparent top and bottom controls
- the top information icon and `Cabinet Main` popover
- whitespace click closing the popover
- source/outline behavior staying in the same `.athena` editor tab

### Verification Path

Run checks sequentially:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test
.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test
node --test ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs
node --test ide/theia-frontend/scripts/athena-m21-boundary.test.mjs
node --test ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs
node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs
node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs
Set-Location ide
yarn workspace @engineeringood/athena-theia-product start:smoke:m21
Set-Location ..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

The smoke proof emits `ATHENA_GRAPH_WORKBENCH_PROOF={...}` after the rendered graph workbench DOM
contract passes.

## What M21 Proves

M21 proves:

- Athena can start visible milestone proof from a real Theia sample project.
- Layout intent can exist before solved placement facts.
- A rule-based schematic layout strategy can produce deterministic Athena-owned facts.
- External layout helpers can be kept subordinate to Athena contracts.
- Schematic grouping can be represented as inspectable region facts.
- Schematic routes can be represented as deterministic topology facts, not physical route claims.
- Labels and cross-references can carry governed identities and deterministic placements.
- IDE source, outline, Problems, and sheet identity can stay coherent while layout intelligence is
  active.
- M21 scope can be guarded by executable boundary tests.

M21 does not prove:

- full EPLAN parity
- full IEC or QElectroTech library coverage
- public repository/import ecosystem behavior
- cabinet authoring
- physical wire, harness, cable tray, or installation routing
- AI layout
- final ELK or external layout-stack selection
- sheet-local drag-save behavior as semantic truth

## Review Findings And Corrections

The milestone review process caught real issues and improved the system:

- The first IDE proof pattern risked becoming self-fulfilling. It was replaced with runtime DOM
  proof from the rendered Theia graph workbench.
- Workspace proof could have passed from stale IDE state. Smoke execution now isolates Electron user
  data and verifies the target M21 sample project.
- CSS guards could have matched across unrelated rules. They now check exact selector contracts.
- Layout source spans could accept inverted same-line ranges. Validation now rejects that case.
- The schematic strategy could accept non-schematic snapshots. It now rejects them.
- Helper proposal normalization could miss exact intent coverage or accept invalid geometry. It now
  enforces exact coverage and geometry validation.
- Route derivation could create zero-length segments for close diagonal or same-point endpoints. It
  now handles close diagonal routing and rejects same-point routes.
- Label snapshots could accept duplicate label ids. They now reject duplicates.
- Boundary tests initially used stricter phrasing than the PRD. They were aligned to documented
  M21 language without weakening the deferred-scope checks.

## Retrospective

### What Went Well

- M21 applied the strongest M20 lesson immediately: prove through the real IDE, not through scripts
  alone.
- The architecture stayed aligned with EngineeringOS: semantic authority upstream, layout intent and
  facts in the kernel, renderer as paint-only consumer.
- The sample project gave the milestone a concrete user-facing anchor from Epic 1.
- The implementation kept layout, routing, and label models small and deterministic.
- Review patches strengthened identity, geometry, determinism, and scope boundaries instead of only
  cleaning prose.

### What Was Difficult

- The visible proof path needed discipline. It was easy for a smoke script to prove that code ran
  rather than that the actual customer-facing surface rendered correctly.
- Boundary testing needed careful wording. Tests should enforce the PRD and architecture, not invent
  stricter private requirements.
- Layout terms are overloaded. "Routing" had to be repeatedly constrained to schematic topology so
  M21 did not drift into cabinet or physical wiring scope.
- UI proof and kernel proof had to stay connected. Either one alone would have been misleading.

### What We Learned

- A milestone that affects user trust needs an openable sample project before deep kernel work
  expands.
- Layout intent is not optional documentation. It is the contract that prevents coordinates from
  becoming unexplained truth.
- Runtime DOM proof is more valuable than a marker string when the acceptance claim is about the IDE
  surface.
- Deterministic output requires explicit tie-breakers and identity rules, not reliance on collection
  iteration.
- Deferred boundaries should be executable. Scope decisions decay if they live only in prose.
- `.mjs` automation is valid engineering support, but it must never be positioned as what a user or
  customer authors to use Athena.

## Carry-Forward Rules

- Keep Theia as the IDE surface and ignore desktop-viewer scope unless a future milestone explicitly
  reopens it.
- Keep the renderer paint-only. It consumes layout, route, label, and identity facts; it does not
  infer engineering meaning.
- Keep the M21 sample project openable and syntax-valid whenever future milestones extend it.
- Keep `Cabinet Main` information out of the main canvas and in an information popover unless a
  governed publication/export mode explicitly places it.
- Keep public repository/import, full IEC breadth, cabinet authoring, physical routing, AI layout,
  and final layout-stack selection out of M21 follow-up fixes.
- Treat visual proof and kernel proof as a pair for future presentation milestones.

## Handoff To The Next Milestone

Recommended next focus:

- Turn the M21 layout, routing, and label facts into a stronger visible sheet rendering path.
- Add richer engineering drawing rules only through governed layout intent and facts.
- Keep the sample project as the first acceptance surface for any customer-facing layout milestone.
- Consider external helpers only through the M21 adapter boundary; do not let an external engine
  define Athena's layout authority.
- Preserve same-tab navigation and graph workbench canvas contracts while improving presentation.

## Action Items

| Owner | Action | Status |
| --- | --- | --- |
| Product | Keep M21 usage framed around the openable Theia sample project, not `.mjs` files. | open |
| Architecture | Use M21 layout intent as the required contract for future layout intelligence. | open |
| Development | Preserve `:kernel:layout-engine` and `:kernel:routing-model` determinism tests when extending layout behavior. | open |
| QA | Keep M21 acceptance coverage and boundary checks in the verification path for future presentation work. | open |
| UX/Product | Evaluate future sheet rendering improvements against engineering readability, not generic visual polish. | open |
