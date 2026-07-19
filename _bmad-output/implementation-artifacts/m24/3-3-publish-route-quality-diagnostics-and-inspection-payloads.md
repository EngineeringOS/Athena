---
status: review
epic: 3
story: 3.3
title: Publish route quality diagnostics and inspection payloads
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 3.3: Publish route quality diagnostics and inspection payloads

As an IDE user, I want degraded or fallback routes to be explainable, so that routing limits are
visible instead of silently pretending to be professional.

## Acceptance Criteria

- Degraded or fallback route facts expose affected connection identity.
- Diagnostics or inspection payloads name failed constraint families where available.
- Satisfied routes remain clean and do not spam Problems.
- Rendering continues when a route has degraded or fallback quality.
- Tests cover satisfied and fallback route quality states.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-8. Fallback visibility is required for honest product behavior.

## Tasks/Subtasks

- [x] Add route-quality diagnostics for degraded and fallback route facts.
- [x] Add route inspection payloads that preserve route, connection, anchor, failed constraint, and quality identity.
- [x] Ensure satisfied routes produce no Problems-style diagnostic noise.
- [x] Verify rendering-facing presentation remains compatible when route quality is degraded or fallback.

## Dev Agent Record

### Debug Log

- Confirmed RED with `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.RouteQualityDiagnosticsTest"`; compilation failed because route-quality diagnostic publishing did not exist.
- Added `RouteQualityDiagnosticPublisher`, route-quality diagnostic models, and inspection payload models in `kernel/routing-model`.
- Verified routing-model and presentation-model regression behavior.

### Completion Notes

- Degraded and fallback route facts now publish diagnostics tied to affected connection identity and failed constraint families.
- Satisfied route facts remain clean and available in inspection payloads without producing diagnostics.
- Validation passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests "com.engineeringood.athena.routing.RouteQualityDiagnosticsTest"`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`

## File List

- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteQualityDiagnostics.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteQualityDiagnosticsTest.kt`
- `_bmad-output/implementation-artifacts/m24/3-3-publish-route-quality-diagnostics-and-inspection-payloads.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`

## Change Log

- 2026-07-19: Completed Story 3.3 route-quality diagnostics and inspection payloads.

## Status

review
