---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 2.3: Component-Avoiding Orthogonal Routes

Status: done

## Story

As an engineer,
I want schematic routes to avoid component bodies and enter at terminal anchors,
so that linework reads like engineering wiring rather than generic graph edges.

## Acceptance Criteria

1. Given presentation bounds and terminal anchors are available, when route facts are generated,
   then accepted routes do not cross component bodies.
2. Given route facts are generated, endpoints attach to terminal anchors instead of center fallback
   points.
3. Given a route cannot fully satisfy component avoidance, when route quality is emitted, then the
   route is marked degraded with a reason.
4. The renderer does not silently present degraded routes as satisfied.

## Tasks / Subtasks

- [x] Preserve component avoidance for clear routes (AC: 1)
  - [x] Keep `AthenaRouteEngineV0` lane-around behavior for obvious component bodies.
- [x] Preserve terminal-anchor attachment (AC: 2)
  - [x] Keep route segments starting/ending at `TerminalAnchorFact.gridPoint`.
  - [x] Keep M27 smoke structured proof checking terminal anchors and center fallback absence.
- [x] Emit degraded route quality when avoidance cannot be satisfied (AC: 3, 4)
  - [x] Extend route quality calculation to inspect solved segments against component bounds.
  - [x] Attach failed avoidance constraints to degraded route quality.
  - [x] Add regression coverage for a blocked route with no available avoidance lane.

## Dev Notes

- The route engine still produces Athena `RouteFact` output. No backend or renderer owns route
  quality.
- M27 accepted sample smoke already verifies `routeBodyIntersectionCount: 0` and
  `centerFallbackRouteIds: []`.
- No `.athena` syntax is introduced.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-20: Implemented after Story 2.2 established the routing backend boundary.

### Completion Notes List

- `AthenaRouteEngineV0` now marks unavoidable intermediate component-body intersections as
  `DEGRADED` route quality instead of silently reporting success.
- Source and target component bodies are excluded from obstacle quality checks because routes must
  attach to their terminal anchors on those components.
- Added blocked-lane regression coverage for an unavoidable obstacle.
- Fresh M27 product smoke reports accepted route quality `SATISFIED`, terminal anchors present,
  `centerFallbackRouteIds: []`, and `routeBodyIntersectionCount: 0` across the accepted sheets.

### File List

- `_bmad-output/implementation-artifacts/m27/2-3-component-avoiding-orthogonal-routes.md`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineLaneAndAvoidanceTest.kt`

## Change Log

- 2026-07-20: Created and implemented component-avoidance degradation coverage.
- 2026-07-20: Closed Story 2.3 after routing tests, Theia rebuild, M27 smoke, and encoding audit passed.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` - passed.
- `yarn --cwd ide build` - passed.
- `yarn --cwd ide start:smoke:m27` - passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - passed.
