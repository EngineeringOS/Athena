# M22 Professional Layout Acceptance Checklist

This checklist defines how reviewers judge the M22 sample project. It is a product acceptance aid,
not a pixel-perfect target.

## Comparison Set

Use these references together:

- M21 baseline: `examples/m21/sample-project`
- M22 baseline source: `src/01-baseline-sheet.athena`
- M22 optimized source: `src/02-layout-optimization-acceptance.athena`
- M22 round-trip source: `src/03-component-round-trip.athena`
- visual inspiration from `draft/screenshort`, especially schematic and engineering-sheet examples
  such as `qelectrotech15.jpg`, `qelectrotech5.jpg`, `machine_no_000_-_op,_cpu.png`, and
  `nuri_bustopologie_profinet,_ethernet,_drivecliq.png`

## Required Acceptance Checks

### Zones

- Power source, protection, controller, HMI, terminal, and load subjects are visually identifiable.
- Related subjects read as engineering groups instead of a generic graph cluster.
- The sheet remains readable without opening implementation code.

### Spacing

- Components have enough separation to scan labels, ports, and routes.
- Dense areas remain compact without obvious collisions.
- The grid remains visible as the coordinate surface.

### Grouping

- Power and protection subjects read together.
- Controller and HMI subjects read as the control area.
- Terminals and load subjects remain distinguishable from controller subjects.
- Governed `grouped-with` constraints emit explicit group facts with subject and occurrence
  identities; the renderer must not infer these groups from pixels or DOM order.

### Governed Placement And Grouping Evidence

- `LayoutConstraintSnapshot` is the input contract for preferred-zone and grouped-with constraints.
- `RuleBasedSchematicLayoutOptimizer` applies preferred-zone constraints before solving placement.
- `SchematicLayoutGroupFact` records grouping evidence for review with constraint, intent,
  occurrence, role, and zone identity.
- M22 Story 2.3 verifies HMI/control grouping and placement through `:kernel:layout-engine:test`.

### Basic Orthogonal Edge Routing

- Primary connections use basic orthogonal edge routing or routing facts that can be rendered as
  horizontal and vertical segments.
- Routes avoid obvious major overlaps in the acceptance sheet.
- Route-lane preference can select horizontal-first or vertical-first schematic segments while
  remaining basic schematic topology.
- Route behavior stays schematic only and does not claim physical wiring, cabinet routing, harness
  routing, cable tray routing, or 3D installation meaning.

### Label Overlap Avoidance

- Device names, terminal names, and signal labels avoid obvious overlap with their own subject.
- Labels avoid obvious conflict with the primary route they describe.
- Subject labels can use declared subject bounds to place text outside the component body.
- M22 does not claim standards-specific label generation.

### M21 Baseline Comparison

- The M22 optimized view must be visibly more readable than the M21 baseline for the same reviewer
  task: find power source, protection, controller, HMI, terminal, and load path.
- Improvements must come from governed layout constraints and layout facts, not hidden canvas state.
- The same input should reproduce the same layout facts on repeated runs.

## Non-Acceptance Items

M22 is not full EPLAN parity and is not pixel-perfect against `draft/screenshort`.

M22 does not accept or require:

- full EPLAN toolbar, panel, or authoring depth
- full IEC or QElectroTech symbol library breadth
- public repository/import ecosystem behavior
- cabinet authoring
- physical routing
- advanced electrical routing intelligence
- standards-specific label generation
- AI layout optimization
- final ELK or layout-stack selection
- sheet-local drag-save truth
