---
status: review
epic: 2
story: 2.3
title: Add lane assignment and component avoidance
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 2.3: Add lane assignment and component avoidance

As an electrical engineer, I want long routes to use lanes and avoid component bodies, so that the
sheet remains ordered and readable.

## Acceptance Criteria

- Long route segments can use horizontal or vertical routing lanes.
- Routes avoid obvious component body overlap in the accepted M24 sample.
- Route lane assignment is deterministic.
- Fallback quality is emitted when avoidance cannot be satisfied.
- Tests cover at least one obstacle and one clear lane path.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This is not a general router. Implement the smallest rule-based behavior that proves the M24 sample.

## Tasks/Subtasks

- [x] Add deterministic lane assignment to route facts.
- [x] Add component-bound segment intersection and a small clear-lane avoidance rule.
- [x] Cover one obstacle path and one clear lane path with tests.
- [x] Keep the behavior rule-based and narrow, not a generic router.

## Dev Agent Record

### Implementation Notes

- Added `lane` to `RouteFact` and deterministic lane assignment in `AthenaRouteEngineV0`.
- Added `SchematicComponentBounds.intersects` and a simple lane-above-obstacle reroute for obvious component body overlap.
- The behavior remains intentionally narrow and local to the M24 accepted sample path.

### Debug Log

- Focused lane/avoidance test first failed because `RouteFact.lane` and component-bound intersection did not exist.
- Added the missing lane and intersection contracts, then updated route engine v0 to reroute through a clear lane when a direct path intersects an obstacle.
- `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.AthenaRouteEngineLaneAndAvoidanceTest"` passed.
- `:kernel:routing-model:test` passed.

### Completion Notes

- Implemented deterministic lane assignment and first obstacle avoidance for Story 2.3.
- Verified the routing-model suite stays green.

## File List

- `_bmad-output/implementation-artifacts/m24/2-3-add-lane-assignment-and-component-avoidance.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineLaneAndAvoidanceTest.kt`

## Change Log

- Added deterministic route lane assignment.
- Added component-bound intersection and simple clear-lane avoidance in route engine v0.
- Verified focused and module-level routing-model tests.

## Status

review
