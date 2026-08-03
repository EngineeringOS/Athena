---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.2: Measure Spatial Quality Against The M40 Target

Status: review

## Story

As a milestone owner,
I want density and quality measured, not claimed,
so that M40 has an honest non-regression baseline.

## Acceptance Criteria

1. The Spatial Quality Analyzer measures density, occupancy, label pressure, and route/body
   intersections from Presentation Document bounds (no pixels).
2. The M40 proof reports the same M40 source flat vs composed at desktop 1920x1080 fit-to-screen,
   with the M39 baseline (28 label collisions) as cross-reference only.
3. Target (decided): label collisions <= 28, route/body intersections = 0; label suppression is
   forbidden and the full emitted label count is reported.
4. Sheet-area facts come from the M40 replacement for the retired sheet-frame/title-block inputs.

## Tasks / Subtasks

- [x] Add `SpatialQualityMetricsReporter` (density, occupancy, label pressure, intersections).
- [x] Add flat-vs-composed + label-count + metric tests.
- [x] Run focused tests; update story + sprint.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 4.2]
- [Source: PRD FR-18; ARCHITECTURE-SPINE.md AD-14]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added SpatialQualityMetricsReporter.
- 2026-08-02: Compiler suite green.

### Completion Notes List

- `SpatialQualityMetricsReporter` derives density, occupancy, label pressure, label count, and
  route/body intersections from Spatial facts only (no pixels); label count always reported
  (label suppression forbidden by design).
- Metrics deterministic; sheet-area uses the A3 extent (1200x800) consistent with the renderer.
- Target (label collisions <= 28, intersections = 0) enforced in the E2E proof (Story 5.2) and
  the retrospective (Story 5.3).

### File List

- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityMetricsReporter.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 4.2 and PRD FR-18.
- 2026-08-02: Implemented metrics reporter; compiler green; review.
