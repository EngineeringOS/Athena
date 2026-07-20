---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 2.4: Deterministic Lanes And Route Quality Facts

Status: done

## Story

As an engineer,
I want routes aligned into readable deterministic lanes with visible quality status,
so that schematic linework remains ordered and reviewable across rebuilds.

## Acceptance Criteria

1. Given multiple related routes exist in the M27 sample, when route facts are generated, then
   routes use deterministic orthogonal lanes with stable ordering.
2. Given terminal presentation policy defines side preference, terminal-side entry is preserved.
3. Given a route is selected or inspected through proof output, route quality metadata reports
   satisfied, degraded, crossing, or fallback status.
4. Route quality is derived from facts, not DOM scanning or rendered SVG guessing.

## Tasks / Subtasks

- [x] Verify deterministic lanes and ordering (AC: 1)
  - [x] Existing routing tests assert deterministic lane assignment for clear routes.
  - [x] Existing route snapshots canonicalize route facts deterministically.
- [x] Verify terminal-side entry (AC: 2)
  - [x] Existing side-stub tests assert output/input and power/terminal preferred side stubs.
- [x] Verify route quality facts and inspection payloads (AC: 3, 4)
  - [x] Existing route-quality diagnostics tests assert satisfied/fallback inspection payloads.
  - [x] Story 2.3 added degraded quality for unavoidable component-body crossings.
  - [x] M27 smoke exposes `quality` for each route in structured proof output.

## Dev Notes

- This story did not need additional product code after Stories 2.2 and 2.3. The required behavior
  is already covered by the routing model tests and live M27 smoke proof.
- No `.athena` syntax is introduced.
- Theia remains a fact consumer; proof output reads route facts transported from the runtime path.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-20: Closed after Story 2.3 smoke proved accepted M27 routes are satisfied,
  terminal-anchored, orthogonal, center-fallback-free, and body-intersection-free.

### Completion Notes List

- Fresh routing tests cover deterministic lane assignment, side stubs, route quality diagnostics,
  routing backend normalization, and component-avoidance degradation.
- Fresh M27 smoke reports structured route `quality` values, terminal anchor use,
  orthogonal bends, `centerFallbackRouteIds: []`, and `routeBodyIntersectionCount: 0`.

### File List

- `_bmad-output/implementation-artifacts/m27/2-4-deterministic-lanes-and-route-quality-facts.md`

## Change Log

- 2026-07-20: Created and closed Story 2.4 from existing implementation and fresh proof evidence.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` - passed during Story 2.3 closeout.
- `yarn --cwd ide build` - passed during Story 2.3 closeout.
- `yarn --cwd ide start:smoke:m27` - passed during Story 2.3 closeout.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - passed during Story 2.3 closeout.
