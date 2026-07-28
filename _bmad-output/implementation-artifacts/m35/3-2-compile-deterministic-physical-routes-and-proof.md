---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 3.2: Compile Deterministic Physical Routes And Proof

Status: review

## Story

As an engineer,
I want aliased connections routed through their authored channel sequences and bound anchors,
so that Cabinet conductors are physically contained, inspectable, and fail closed.

## Acceptance Criteria

1. Given engineering endpoints, ordered channel ids, channel topology, bound representation anchors, and Cabinet transforms, when `CabinetRoutingCompiler` runs, then it emits one `CabinetRouteFact` per stable connection with endpoint bindings, ordered channels, orthogonal segments, source provenance, and intersection proof, and existing schematic `routing-model` owns none of these facts.
2. Given channel transitions and endpoint stubs, when segment geometry is built, then transitions use the exact passable midpoint projected to assigned lanes, bends are horizontal then vertical, ties use lower X then lower Y, and stubs use the nearest point on first/last lane.
3. Given missing/incompatible anchors, invalid adjacency/capacity, off-channel segments, or non-endpoint body intersections, when routing completes, then composition fails with stable diagnostics and no alternate-route search or renderer heuristic runs.
4. Given valid Cabinet routes, when structured route proof is emitted, then it reports route count, channel use, endpoint bindings, ordered segments, intersections, off-channel segments, and unbound endpoints, and the acceptance fixture proves zero route/body intersections and zero endpoints outside their anchors.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and generic midpoint routes, schematic physical-route leakage, stale route helpers, fixtures, and compatibility paths are purged.

## Tasks / Subtasks

- [x] Add RED tests for deterministic Cabinet routing (AC: 1..4)
  - [x] Cover one valid aliased route through authored channel sequence with endpoints bound to anchors.
  - [x] Cover exact lane projection, passable midpoint transition, horizontal-then-vertical bends, and tie ordering.
  - [x] Cover missing anchor, missing lane assignment, missing adjacency, off-channel segment, and body-intersection diagnostics.
  - [x] Assert structured route proof is deterministic and reports zero acceptance failures for the valid fixture.
- [x] Implement route models in `kernel:drawing-composition` (AC: 1..4)
  - [x] Add endpoint bindings, exact route points, segments, route facts, route proof, diagnostics, and compile result.
  - [x] Consume `RouteChannelTopology`, `PhysicalRouteIntentV0`, and `CabinetVisualTransform`; do not use schematic `routing-model`.
- [x] Implement `CabinetRoutingCompiler` (AC: 1..4)
  - [x] Bind endpoints to explicit transformed anchors.
  - [x] Build deterministic orthogonal segments from endpoint stubs, assigned lanes, and adjacency points.
  - [x] Validate route containment, endpoint containment, and non-endpoint body intersections.
  - [x] Fail closed without search, alternate routing, inferred channels, or renderer heuristics.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched paths for generic midpoint routes, schematic route leakage, stale helpers, XML, fallback, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 3.2 compiles physical Cabinet route facts only. It does not render route strokes, implement selection/trace transport, change public syntax, or add route search/optimization.

### Architecture Requirements

- Physical route facts belong to `kernel:drawing-composition`, consuming `kernel:physical-model` topology.
- Schematic `routing-model` remains M34 schematic-only and must not own Cabinet physical route facts.
- The compiler must be deterministic: sort by stable connection alias, use exact rational lane points until final integer drawing points, and produce stable diagnostics/proof.
- Invalid topology or missing anchors fail closed. No fallback lane, midpoint, solver, search, reroute, or renderer inference is allowed.
- Endpoint and body geometry comes from Cabinet visual transforms and composed physical bounds, not SVG DOM or labels.

### Previous Story Intelligence

- Story 3.1 added `RouteChannelTopologyCompiler`, `ExactMillimeters`, lane assignments, adjacency facts, and deterministic proof/diagnostics in `kernel:physical-model`.
- `PhysicalRouteChannelV0` now carries explicit orientation, and lane centers use exact rational millimetres.
- Existing physical model tests verify invalid lane geometry, overflow, cross-duct/corner/gap/overlap transitions, and same-duct adjacency.
- Continue using sequential Gradle verification on Windows.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 3, Story 3.2.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-28..FR-31.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-11, AD-12, AD-18 route ownership and proof conventions.
- `_bmad-output/implementation-artifacts/m35/3-1-compile-route-channels-with-exact-lanes-and-adjacency.md` - route topology handoff.
- `_bmad-output/implementation-artifacts/m35/2-6-join-mounted-occurrences-to-one-visual-transform.md` - Cabinet visual transform handoff.
- `_bmad-output/implementation-artifacts/m35/2-7-compose-one-paint-only-cabinet-document.md` - Cabinet composition handoff.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T04:47:45+08:00 - Created Story 3.2 from M35 Epic 3 backlog using PRD, architecture spine, epics, and Story 3.1 route topology evidence.
- 2026-07-28T04:49:00+08:00 - Started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- 2026-07-28T05:10:00+08:00 - RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test --tests com.engineeringood.athena.drawing.composition.CabinetRoutingCompilerTest` failed with missing `CabinetRoutingCompiler` and route contract symbols.
- 2026-07-28T05:28:00+08:00 - GREEN evidence: targeted `CabinetRoutingCompilerTest` passed with `BUILD SUCCESSFUL`.
- 2026-07-28T05:29:00+08:00 - Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test` passed with `BUILD SUCCESSFUL`.
- 2026-07-28T05:31:00+08:00 - Audit evidence: route/composition forbidden-term audit returned only expected physical compatibility terminology, encoding audit passed, and `git diff --check` reported no whitespace errors beyond existing CRLF warnings.

### Completion Notes List

- Added deterministic `CabinetRoutingCompiler` and route facts in `kernel:drawing-composition`, consuming physical IR, route topology, transformed anchors, and explicit endpoint bindings.
- Route geometry now keeps lane centers channel-local, adjacency midpoints duct-local, and emitted route points in drawing/enclosure coordinates.
- Added fail-closed diagnostics for unbound anchors, missing lane assignments, missing adjacency, body intersections, and non-admitted channels.
- AC evidence: AC1 covered by route fact/proof test; AC2 by exact lane/midpoint segment assertions; AC3 by invalid-path diagnostics; AC4 by deterministic proof fields and zero acceptance failures; AC5 by audit and sequential verification.

### File List

- `_bmad-output/implementation-artifacts/m35/3-2-compile-deterministic-physical-routes-and-proof.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompiler.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompilerTest.kt`

### Change Log

- 2026-07-28 - Created Story 3.2 implementation guide.
- 2026-07-28 - Moved Story 3.2 to in-progress.
- 2026-07-28 - Implemented deterministic physical Cabinet routing, route proof, fail-closed diagnostics, and verification evidence.
