# M24 Addendum

## User Feedback Trigger

The user could not see a meaningful routing difference after M23. The specific complaint was that
input and output wire connections still do not look coordinated like EPLAN: lines are not clean,
orthogonal, or professionally attached to terminal/port sides.

M24 should treat this as a product credibility issue, not a cosmetic preference.

The clearest supplied visual reference is:

```text
../../../draft/screenshort/coffret_cordons_chauffants.png
```

The important qualities are:

- wires avoid elements instead of crossing through them
- long runs use clean horizontal and vertical folds
- related wires run in ordered parallel lanes
- terminal-strip connections preserve visible ordering
- route density still remains inspectable
- the result looks engineered rather than graph-laid-out

This reference has cabinet-like wiring qualities, but M24 must not become physical cabinet routing.
Athena should borrow the ordered route presentation style while deriving the route geometry from
semantic connections, ports, terminal anchors, layout context, and routing policy. The reference is
directional only; M24 proves one narrow terminal-anchor and terminal-strip lane/bundle step.

## Recommended Architecture Direction

M24 should introduce schematic routing as a governed model layer:

```text
Semantic connection
  -> Electrical connection intent
  -> Schematic routing policy
  -> Presentation occurrence
  -> Terminal anchor facts
  -> Schematic route intent
  -> Route constraints
  -> Deterministic route facts
  -> Renderer
```

The renderer should consume route facts only. It should not infer source connection meaning, port
roles, terminal sides, route lanes, or label identity from DOM or canvas positions.

M24 should add or seed a dedicated `kernel/routing-model` rather than hiding route semantics inside
presentation or renderer code.

Structural seed:

```text
kernel/
  routing-model/       # route intent, route constraints, terminal anchors, routing policy, route facts
  routing-engine/      # deterministic Athena route engine v0 if implementation size justifies split
  layout-model/        # existing placement/layout context consumed by routing
ide/
  theia-frontend/      # route-fact rendering and inspection only
```

## Routing Model Seed

Candidate concepts:

```kotlin
data class ElectricalConnectionIntent(
    val connectionId: CanonicalConnectionId,
    val connectionClass: ConnectionClass,
    val sourceRole: PortRole,
    val targetRole: PortRole,
)

data class RoutingPolicy(
    val policyId: RoutingPolicyId,
    val portPresentationPolicies: List<PortPresentationPolicy>,
    val laneRules: List<RouteLaneRule>,
    val bundleRules: List<RouteBundleRule>,
    val crossingPolicy: CrossingPolicy,
)

data class TerminalAnchorFact(
    val subjectId: CanonicalSubjectId,
    val portId: CanonicalPortId,
    val occurrenceId: OccurrenceId,
    val side: PortSide,
    val point: GridPoint,
)

data class SchematicRouteIntent(
    val connectionId: CanonicalConnectionId,
    val sourceAnchor: TerminalAnchorRef,
    val targetAnchor: TerminalAnchorRef,
    val constraints: List<RouteConstraint>,
)

data class SchematicRouteFact(
    val connectionId: CanonicalConnectionId,
    val routeId: RouteId,
    val segments: List<OrthogonalRouteSegment>,
    val labelAnchor: GridPoint?,
    val quality: RouteQuality,
)
```

Initial enums:

```kotlin
enum class PortSide {
    Left,
    Right,
    Top,
    Bottom,
}

enum class RouteQuality {
    Satisfied,
    Degraded,
    Fallback,
}
```

Policy-owned port-side decisions:

```kotlin
data class PortPresentationPolicy(
    val componentFamily: ComponentFamilyRef,
    val portRole: PortRole,
    val preferredSide: PortSide,
)
```

## Initial Rule Direction

Start with deterministic engineering rules, not AI, ELK, Graphviz, or a generic graph router:

- inputs may prefer left side through the active `PortPresentationPolicy`
- outputs may prefer right side through the active `PortPresentationPolicy`
- power can prefer top or left depending on component role and policy
- ground/protective conductors can prefer bottom depending on component role and policy
- terminal blocks can expose left/right paired anchors
- route exits use short stubs before joining lanes
- long routes use orthogonal lanes
- semantically related routes can join ordered bundles before splitting to terminal anchors
- terminal strip routes preserve terminal ordering where possible
- avoid component body rectangles
- labels attach to route segments with clearance

These rules are intentionally modest. They are enough to stop the worst center-to-center graph edge
look and create a measurable routing baseline.

## Route Syntax Decision

Default recommendation: do not introduce new route syntax in M24 unless implementation remains very
small and fully covered by ANTLR4, Tree-sitter, compiler, LSP, docs, and sample proof.

M24 can derive initial route intent from existing source:

```athena
port PLC1.command {
  direction out
  signal Digital
}

port HMI1.status {
  direction in
  signal Digital
}

connect PLC1.command -> HMI1.status
```

This is enough to infer:

```text
PLC1.command exits from output-preferred side
HMI1.status enters from input-preferred side
route is orthogonal and grid-aligned
```

Route hint syntax can become M25 or later if users need manual route preference persistence.

## EPLAN Boundary

M24 should learn from EPLAN, not clone it.

EPLAN-class routing includes many deeper concerns:

- multi-page cross references
- connection definition points
- wire numbering standards
- potential/signal management
- cable and terminal plans
- physical routing and manufacturing packages
- company standards

M24 should not claim those. It should prove one narrower capability:

```text
Athena schematic connections render as disciplined, governed, orthogonal routes.
```

## External Router Boundary

M24 should not introduce ELK as the route architecture. The route semantics are still too immature.

Correct M24 sequence:

```text
Athena routing policy
  -> Athena route engine v0
  -> deterministic route facts
```

Possible later sequence:

```text
Athena routing policy
  -> route optimization adapter
  -> normalized Athena route facts
```

An external helper may appear in M25/M26 only after Athena owns the route intent, policy,
constraints, quality states, and fact format.

## Proof Requirement

The sample project must include a case where M23 looks visibly weak:

```text
ControllerPLC1.command -> OperatorHMI1.status
ControllerPLC1.command -> TerminalBlockXT1.in
TerminalBlockXT1.out -> PrimaryLoadM1.in
PowerSupply24V.out -> ControllerPLC1.power
```

The accepted M24 view should show:

- output-side exit from controller
- input-side entry to HMI/load
- terminal block through-routing
- ordered parallel lane/bundle behavior near terminal strip connections
- no component-center edge rendering
- grid-aligned orthogonal segments
- readable route label or signal marker

## M23-Vs-M24 Comparison Requirement

The usage docs and proof checklist should include an explicit comparison:

```text
M23 baseline:
  component placement and layout hints are accepted,
  but connections may still read as graph-like edges.

M24 acceptance:
  the same kind of engineering source renders terminal-anchor,
  orthogonal, grid-aligned schematic routes.
```

The comparison does not need pixel-perfect screenshot matching, but it must name visible differences
that a reviewer can inspect in Theia:

- no component-center route attachment in the accepted proof
- visible terminal/port-side stubs
- orthogonal route segments
- visible ordered route bundles or parallel lanes around the terminal-strip case
- route labels or signal markers placed near route geometry
- route identity available for inspection or diagnostics

## Architecture Decisions To Carry Into M24 Spine

- **ADR-1:** Add `kernel/routing-model` as the contract home for electrical connection intent,
  routing policy, port presentation policy, terminal anchors, constraints, facts, and quality state.
- **ADR-2:** Build Athena Route Engine v0 first; defer ELK, yFiles, Graphviz, and other external
  routers until Athena owns routing semantics.
- **ADR-3:** Port sides come from `PortPresentationPolicy`, not renderer hardcoding.
- **ADR-4:** M24 derives route intent from existing `port` and `connect` semantics; new route syntax
  remains deferred unless admitted through both parsers and the compiler/LSP path.
- **ADR-5:** Renderer paints route facts only.
- **ADR-6:** The M24 acceptance proof must include Theia-visible terminal-anchor route behavior, not
  only unit tests.

## Deferred

- physical wire routing
- cabinet routing
- harness/cable tray routing
- 3D installation routing
- full IEC/EPLAN/QElectroTech standards profile
- route editing from canvas
- route-hint language unless parser parity is fully handled
- AI routing
- final external routing engine selection
- general graph visualization routing
- renderer-side center-to-center route fallback in the accepted M24 proof
