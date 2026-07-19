---
status: review
epic: 2
story: 2.2
title: Route from terminal anchors and side stubs
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 2.2: Route from terminal anchors and side stubs

As an IDE reviewer, I want routes to enter and exit through terminal anchors, so that connections
stop looking like generic graph edges.

## Acceptance Criteria

- Routes begin and end at `TerminalAnchorFact` points.
- Short grid-aligned stubs leave the preferred side before joining longer segments.
- The accepted M24 proof has no component-center route endpoints.
- Tests cover input-side, output-side, power, and terminal-block anchor routing.
- Fallback behavior is explicit if a stub cannot be produced.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This is the main visual credibility correction after M23.

## Tasks/Subtasks

- [x] Start and end route facts at `TerminalAnchorFact` grid points.
- [x] Emit short preferred-side stubs for input, output, power, and terminal-block anchors.
- [x] Keep component-center endpoints out of route output.
- [x] Degrade explicitly when a preferred-side stub cannot fit inside sheet bounds.

## Dev Agent Record

### Implementation Notes

- Updated `AthenaRouteEngineV0` to prepend and append preferred-side terminal stubs before the longer orthogonal route path.
- Added explicit degraded `RouteQuality` when a preferred-side stub would leave the sheet bounds.
- Preserved terminal-anchor grid points as route endpoints.

### Debug Log

- Focused side-stub tests first failed because the engine emitted direct routes without stubs.
- Initial implementation exposed an out-of-bounds bug when a left-side stub at x=0 tried to construct a negative route point.
- Fixed fallback handling to check coordinates before constructing a `SchematicRoutePoint`.
- `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.AthenaRouteEngineSideStubTest"` passed.
- `:kernel:routing-model:test` passed.

### Completion Notes

- Implemented terminal-anchor side stub routing for Story 2.2.
- Confirmed fallback behavior is explicit and non-throwing for impossible stubs.

## File List

- `_bmad-output/implementation-artifacts/m24/2-2-route-from-terminal-anchors-and-side-stubs.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineSideStubTest.kt`

## Change Log

- Added preferred-side terminal stubs to route engine v0.
- Added explicit degraded quality for out-of-bounds side-stub fallback.
- Verified focused and module-level routing-model tests.

## Status

review
