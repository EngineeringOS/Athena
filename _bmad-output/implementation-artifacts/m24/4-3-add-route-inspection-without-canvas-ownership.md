---
status: review
epic: 4
story: 4.3
title: Add route inspection without canvas ownership
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 4.3: Add route inspection without canvas ownership

As an IDE user, I want to inspect a route's source connection and quality, so that rendered wires
remain traceable to Athena semantics.

## Acceptance Criteria

- Selecting or inspecting a rendered route exposes source connection identity.
- Route inspection shows source/target ports, route quality, and policy summary where available.
- Inspection payloads come from route facts, not DOM geometry.
- No hidden route coordinates are persisted from the canvas.
- Rejected or unavailable route inspection does not break normal selection behavior.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-10. This is inspectable projection, not route editing.

## Tasks/Subtasks

- [x] Extend route inspection payloads with source/target ports and deterministic policy summary.
- [x] Add graph workbench route inspection from governed presentation connector facts.
- [x] Show route quality, source/target ports, and policy summary in the existing info popover when a route is selected.
- [x] Keep route inspection read-only with no persisted canvas coordinates or route editing path.

## Dev Agent Record

### Debug Log

- Added RED routing-model assertions for source/target route ports and policy summary in `RouteQualityInspectionPayload`.
- Added RED frontend assertions for `buildAthenaGraphRouteInspection`, including absence of `routePoints` and `canvasCoordinates` fields.
- Extended `RouteInspectionPayload` with port ids, port semantic ids, and policy summary from route facts.
- Added `buildAthenaGraphRouteInspection` to the graph workbench model; it only accepts presentation-backed route facts.
- Added route quality, route ports, and route policy rows to the existing graph workbench info popover for selected routes.

### Completion Notes

- Story 4.3 keeps route inspection projection-owned and read-only.
- No hidden route coordinates are persisted from the canvas.
- Unavailable route inspection returns an unavailable result and leaves normal selection behavior intact.
- No route syntax, route editing, desktop-viewer, or physical routing work was introduced.

### Verification

- RED confirmed: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests "com.engineeringood.athena.routing.RouteQualityDiagnosticsTest.satisfied routes stay clean while fallback routes publish diagnostics and inspection payloads"` failed before inspection fields existed.
- RED confirmed: `yarn --cwd ide/theia-frontend test` failed before `buildAthenaGraphRouteInspection` existed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests "com.engineeringood.athena.routing.RouteQualityDiagnosticsTest.satisfied routes stay clean while fallback routes publish diagnostics and inspection payloads"`
- `yarn --cwd ide/theia-frontend test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`

## File List

- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteQualityDiagnostics.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteQualityDiagnosticsTest.kt`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

## Change Log

- 2026-07-19: Added read-only route inspection payloads and Theia route inspection display without canvas-owned route state.

## Status

review
