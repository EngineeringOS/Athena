---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.1: Build The Dedicated M40 Example

Status: review

## Story

As a milestone owner,
I want a dedicated rolling-shutter control example,
so that M40 is judged on its own architecture.

## Acceptance Criteria

1. `examples/m40` declares at least one of each electrical construct and at least one functional
   region; the verifier asserts each appears in the projection snapshot.
2. The example compiles through all four realities, uses human-first `to` syntax and no `intent`
   blocks, and cites only M40 paths.
3. The example does not reuse M36-M39 examples as proof authority.

## Tasks / Subtasks

- [x] Create `examples/m40/rolling-shutter-control` (athena.yaml, lock, source).
- [x] Add compiler proof test (all seven constructs + region + four-reality chain).
- [x] Run focused tests; update story + sprint.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 5.1]
- [Source: PRD FR-19; Decisions 4]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Created the dedicated M40 rolling-shutter example + proof test.
- 2026-08-02: Four-reality chain proof green.

### Completion Notes List

- `examples/m40/rolling-shutter-control` authored with human-first `to` relations, no `intent`,
  one `view` block with 3 functional regions, grid (8x10), all seven electrical constructs, and
  reading order.
- `DedicatedM40ExampleTest` proves: repository contract/lock valid, semantic diagnostics empty,
  projection carries 3 regions + 7 construct kinds + grid, Spatial bridge derives placements +
  grid references, SpatialToPresentation emits a Presentation Document, and metrics are reported.

### File List

- examples/m40/rolling-shutter-control/athena.yaml
- examples/m40/rolling-shutter-control/athena.lock
- examples/m40/rolling-shutter-control/src/com/engineeringood/m40/rollingshutter/01-rolling-shutter-control.athena
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM40ExampleTest.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 5.1 and PRD FR-19/Decision 4.
- 2026-08-02: Built the dedicated example + four-reality proof; green; review.
