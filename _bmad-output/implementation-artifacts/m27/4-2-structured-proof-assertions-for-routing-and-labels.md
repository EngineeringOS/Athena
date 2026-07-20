---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 4.2: Structured Proof Assertions For Routing And Labels

Status: done

## Story

As an Athena maintainer,
I want structured DOM or proof-payload assertions for M27 routing and label quality,
so that visual acceptance does not rely only on screenshots.

## Acceptance Criteria

1. Structured assertions verify sheet frame, title block, zones, compact labels, ordered routes,
   route quality, component-crossing absence, center-fallback absence, and verbose-label absence.
2. Assertions are deterministic across rebuilds.
3. Degraded route state and reason are visible from route facts and diagnostics, not inferred from
   SVG geometry as engineering meaning.

## Completion Notes List

- Frontend tests cover model density, compact route labels, sheet selector, reference navigation,
  route inspection, and graph workbench behavior.
- Routing-model tests cover route quality diagnostics, backend boundary normalization, component
  avoidance, blocked-route degradation, side stubs, and deterministic lanes.
- Product smoke proof reports visible label counts, route states, terminal anchors, orthogonal bend
  status, center fallback ids, route/body intersections, and all-sheet visual proof.

## File List

- `_bmad-output/implementation-artifacts/m27/4-2-structured-proof-assertions-for-routing-and-labels.md`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`
- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RoutingBackendBoundaryTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineLaneAndAvoidanceTest.kt`

## Verification

- `yarn --cwd ide/theia-frontend test` - passed, 133/133 tests.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` - passed during Story 2.3 closeout.
- `yarn --cwd ide start:smoke:m27` - passed during Story 2.3 closeout.
