# M21 Addendum

## Reference Inputs

These references shaped the M21 direction but do not become the milestone itself:

- M20 retrospective and usage summary
- `_bmad-output/implementation-artifacts/m20/m20-ui-acceptance-deep-retrospective-2026-07-17.md`
- `_bmad-output/implementation-artifacts/m20/epic-4-retro-2026-07-17.md`
- `docs/usages/m20-proof-usage.md`
- `examples/m20/sample-project/`
- M21 brainstorming memlog:
  `_bmad-output/brainstorming/brainstorm-m21-direction-2026-07-17/.memlog.md`
- Prior layout technology discussion:
  `draft/layouts/001-disucss.md`
- Engineering symbol/library discussion:
  `draft/elements-lib/0001-qelectrotech-elements.md`

## Product Interpretation

M21 is the first layout-intelligence milestone. It should not be framed as more M20 polish.

The useful product thesis is:

> Athena should use governed engineering layout rules to improve schematic placement, routing,
> grouping, and label readability, then prove that improvement in the normal Theia IDE workflow.

That means M21 is about:

- visible IDE proof first
- openable sample project with real `.athena` files
- deterministic engineering layout rules
- explicit layout intent before numeric layout facts
- semantic grouping by engineering role
- deterministic schematic conductor routing
- label and cross-reference readability
- screenshot or E2E evidence for graph workbench behavior
- preserving accepted M20 canvas chrome and grid behavior

It is not about:

- public repository/import ecosystem work
- full IEC library breadth
- full EPLAN parity
- cabinet authoring
- uncontrolled canvas drag/save position
- moving layout authority into Theia, renderer, or ELK

## Technical Notes

### Layout authority

M21 should keep the authority chain explicit:

```text
Semantic Model
    -> Projection
    -> Presentation IR
    -> Layout Intent
    -> Layout Engine
    -> Layout Facts
    -> Renderer
```

The renderer paints. Theia presents, inspects, and synchronizes. External layout helpers may assist
only through adapters that normalize output back into Athena layout facts.

### Layout intent

M21 should introduce layout intent between presentation and solved layout facts.

Useful layout-intent examples:

- `role: controller`
- `preferredZone: control`
- `priority: high`
- `alignment: vertical`
- `near: HMI`
- `above: terminal block`

This keeps layout explainable. Later AI-assisted layout can reason over intent and constraints
instead of opaque coordinates such as `x=400, y=200`.

Potential downstream architecture names:

- `layout-model` for `LayoutIntent`, `LayoutConstraint`, `LayoutFact`, and `LayoutSnapshot`
- `layout-engine` for `RuleBasedLayoutEngine`, adapter strategies, and future AI-assisted engines
- `routing-model` for `RouteIntent`, `RouteSegment`, and schematic `RouteFact`

### ELK and layout helpers

ELK-style graph layout may be useful for arrangement, hierarchy, and routing assistance. It does not
know Athena's engineering conventions by itself.

The correct posture is:

```text
Athena Layout Contract
    -> optional adapter
    -> normalized Athena Layout Facts
```

Not:

```text
ELK output
    -> renderer truth
```

M21 can research or spike adapter boundaries if useful, but the PRD should not require a final layout
stack decision.

### Routing scope

M21 routing means schematic topology routing only:

```text
symbol endpoint -> schematic conductor path -> symbol endpoint
```

M21 does not own:

- cabinet wire path routing
- harness routing
- cable tray routing
- 3D installation routing
- physical optimization

That boundary prevents M21 from expanding into EPLAN-class physical routing.

### Engineering readability criteria

The acceptance sheet should let an engineer identify:

- where power starts
- where protection is
- where control logic is
- where terminals are
- where the primary load path goes

Power-flow readability and control-hierarchy readability matter more than generic graph neatness.

### M20 lessons to preserve

M21 must not repeat the M20 presentation-proof mistake. The milestone must include:

- a visible IDE proof path from the first story
- a real sample project, not only scripts
- negative UI assertions for forbidden stale behavior
- source, bundle, and runtime-log checks before completion
- docs that match the current IDE surface
- screenshot/E2E proof where visual acceptance matters

### Accepted canvas invariants carried from M20

- The stage grid is the coordinate surface.
- Sheet and component bodies should not hide the grid unless a governed mode requires it.
- `Cabinet Main` details live in the top information popover only.
- Bottom controls are transparent canvas overlays.
- Outline navigation should keep the same `.athena` editor tab.

## Planning Guidance

The M21 epic/story breakdown should favor:

- visible IDE proof and sample project setup
- layout intent, layout rule contracts, layout-engine boundary, and deterministic facts
- semantic grouping and placement
- schematic conductor routing and label avoidance
- IDE coherence and visual regression safety
- explicit boundaries that keep repository/import, IEC breadth, cabinet authoring, and stack
  selection out of scope

The first implementation story should not be a hidden model-only task. It should establish the
openable M21 sample project and the acceptance proof path, then later stories can deepen the layout
rules behind that visible surface.
