---
status: review
epic: 1
story: 1.4
title: Define route constraints route facts and quality state
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 1.4: Define route constraints, route facts, and quality state

As a route-engine maintainer, I want stable route constraint and fact contracts, so that route
generation and rendering cannot diverge.

## Acceptance Criteria

- Route constraints represent orthogonal-only, grid-snap, avoid-node, preferred sides, lane, bundle,
  terminal order, crossing, and label-clearance preferences.
- Route facts carry deterministic ordered segments, source/target anchors, optional label anchors,
  source identity, and quality.
- Route quality can represent satisfied, degraded, and fallback states.
- Tests cover serialization/equality/deterministic sorting for route facts.
- No route fact stores hidden canvas-owned truth.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-8 and AD-9. Quality must be inspectable later.

## Tasks/Subtasks

- [x] Complete route constraint vocabulary for schematic routing preferences.
- [x] Add deterministic route fact snapshot sorting.
- [x] Ensure route facts carry anchors, ordered segments, labels, source identity, and quality.
- [x] Prove equality, sorting, and no canvas-owned truth in tests.

## Dev Agent Record

### Implementation Notes

- Added missing route constraint kinds for avoid-node, route bundle, and terminal order.
- Added `RouteFactSnapshot.canonical` to keep route fact ordering deterministic for downstream route generation and rendering.
- Added route quality convenience state and an explicit route-fact contract that exposes no canvas-owned truth.

### Debug Log

- Focused Story 1.4 test initially failed for missing constraint kinds, snapshot sorting, and quality/canvas-truth helpers.
- Added the missing contract fields and deterministic snapshot wrapper.
- `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.RouteConstraintsAndFactsTest"` passed.
- `:kernel:routing-model:test` passed.

### Completion Notes

- Completed the M24 route constraint/fact/quality contract layer in `kernel/routing-model`.
- Verified the routing-model suite remains green.

## File List

- `_bmad-output/implementation-artifacts/m24/1-4-define-route-constraints-route-facts-and-quality-state.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFactsTest.kt`

## Change Log

- Added missing M24 route constraint kinds and deterministic route-fact snapshot sorting.
- Added quality helpers and a no-canvas-truth route fact check.
- Verified focused and module-level routing-model tests.

## Status

review
