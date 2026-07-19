---
status: review
epic: 3
story: 3.1
title: Feed semantic connections into route intent
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 3.1: Feed semantic connections into route intent

As a compiler engineer, I want projection to provide route intent from semantic connections, so that
route generation follows Athena source truth.

## Acceptance Criteria

- Projection emits route intent with connection identity, port identities, view/sheet context, and
  layout context.
- Route intent is derived from compiled source semantics, not renderer positions.
- Route intent is sorted deterministically.
- Existing connection identity remains compatible with selection and reveal.
- Tests cover PLC-HMI, PLC-terminal-load, and 24V power/protection route intent.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-2. Existing `.athena` source should be enough for M24 route intent.

## Tasks/Subtasks

- [x] Add route-intent snapshot contracts that carry connection identity, port identities, view/sheet context, and layout context.
- [x] Add deterministic projection from electrical connection intent plus terminal anchors into route intent.
- [x] Cover PLC-HMI, PLC-terminal, and 24V power/protection route-intent scenarios.
- [x] Verify route intent is derived from semantic connection/anchor facts, not renderer state.

## Dev Agent Record

### Debug Log

- Confirmed RED with `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.SchematicRouteIntentProjectorTest"`; compilation failed because route-intent projector contracts did not exist.
- Added `SchematicRouteIntent`, `SchematicRouteIntentSnapshot`, and `SchematicRouteIntentProjector` in `kernel/routing-model`.
- Verified focused 3.1 behavior and full routing-model regression behavior.

### Completion Notes

- Semantic electrical connection intent now lowers into deterministic schematic route intent with preserved connection, source port, target port, sheet, view, and layout context.
- The bridge remains renderer-free and does not introduce route source syntax.
- Validation passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests "com.engineeringood.athena.routing.SchematicRouteIntentProjectorTest"`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`

## File List

- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicRouteIntent.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicRouteIntentProjectorTest.kt`
- `_bmad-output/implementation-artifacts/m24/3-1-feed-semantic-connections-into-route-intent.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`

## Change Log

- 2026-07-19: Completed Story 3.1 semantic connection to schematic route-intent projection.

## Status

review
