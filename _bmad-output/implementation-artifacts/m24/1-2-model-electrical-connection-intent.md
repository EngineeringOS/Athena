---
status: review
epic: 1
story: 1.2
title: Model electrical connection intent
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 1.2: Model electrical connection intent

As a compiler engineer, I want semantic `connect` facts to classify electrical connection intent, so
that routing decisions can use engineering meaning instead of only topology.

## Acceptance Criteria

- Electrical connection intent can classify at least control, power, terminal transition, and load
  connection classes for the M24 sample.
- Intent carries canonical connection, source port, target port, source subject, target subject, and
  source span identity where available.
- Unknown or unsupported classes degrade explicitly instead of crashing.
- Tests cover direction, signal, and terminal-transition mapping.
- No renderer or Theia code participates in classification.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-2. Initial classification should be modest and deterministic.

## Tasks/Subtasks

- [x] Add deterministic electrical connection intent classification for the M24 routing model.
- [x] Preserve canonical connection, port, subject, and source span identities where available.
- [x] Degrade unsupported direction/signal combinations explicitly instead of throwing.
- [x] Cover direction, signal, terminal-transition, power, load, and unsupported mappings with tests.

## Dev Agent Record

### Implementation Notes

- Extended `ElectricalConnectionIntent` with canonical port semantic ids, optional source span, and classification quality.
- Added `ElectricalConnectionIntentClassifier` with modest deterministic mapping from connection-model direction/signal facts to routing intent.
- Added endpoint-kind input so terminal transitions and load connections are model facts, not renderer inference.

### Debug Log

- Focused classifier test initially failed at `compileTestKotlin` because the routing model did not yet depend on `connection-model` and classifier types were missing.
- Added `:kernel:connection-model` as a routing-model dependency so classification consumes canonical semantic port direction and signal family types.
- `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.ElectricalConnectionIntentClassifierTest"` passed.
- `:kernel:routing-model:test` passed.

### Completion Notes

- Implemented Story 1.2 classification in `kernel/routing-model`; no Theia, renderer, or canvas code participates.
- Unknown signal/direction combinations produce degraded intent with an explicit message.

## File List

- `_bmad-output/implementation-artifacts/m24/1-2-model-electrical-connection-intent.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`
- `kernel/routing-model/build.gradle.kts`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntent.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntentClassifierTest.kt`

## Change Log

- Added electrical connection intent classification from connection-model direction/signal facts.
- Added degraded classification quality for unsupported mappings.
- Added focused classifier tests and verified the routing-model suite.

## Status

review
