---
status: review
epic: 1
story: 1.1
title: Create routing-model contract home
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 1.1: Create routing-model contract home

As an Athena architect, I want `kernel/routing-model` to own routing contracts, so that route
semantics do not leak into presentation, renderer, or Theia code.

## Acceptance Criteria

- `kernel/routing-model` exists and is wired into the Gradle build if a new module is required.
- It exposes contracts for electrical connection intent, routing policy, port presentation policy,
  terminal anchors, route constraints, route facts, route segments, route labels, and route quality.
- It has no dependency on Theia, renderer DOM, canvas state, or frontend code.
- Kotlin files are grouped by responsibility and avoid a large mixed dump file.
- Tests prove the model can represent the M24 sample route contracts.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-1. Keep this as contract foundation only; do not implement route rendering here.

## Tasks/Subtasks

- [x] Create the routing-model contract home with M24 electrical connection, policy, anchor, constraint, fact, label, and quality types.
- [x] Keep the module free of Theia, renderer, DOM, and frontend dependencies.
- [x] Add tests proving the model can represent the M24 sample route contracts.

## Dev Agent Record

### Implementation Notes

- Added dedicated routing-model contract files split by responsibility: identities, electrical connection intent, port presentation policy, routing policy, and route constraints/facts.
- Kept the module kernel-only and reused existing layout/engineering identity types where appropriate.
- Added a focused contract test that constructs a governed terminal-anchor routing sample and verifies side policy, route quality, constraint kind, and label representation.

### Debug Log

- Initial test run failed at `compileTestKotlin` because the M24 contract types did not exist yet.
- After adding the contract types, `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.M24RoutingContractTest"` passed.
- Verified the full `:kernel:routing-model:test` task passes after the implementation.
- The repository-wide `test` task is not a valid gate for this milestone because `apps:desktop-viewer` is excluded from the current IDE scope.

### Completion Notes

- Implemented the M24 routing-model contract home and the first governed electrical routing vocabulary.
- Confirmed the routing-model module stays free of frontend/runtime dependencies.
- Confirmed the routing-model module tests pass and the story stays within the accepted Theia/kernel scope.

## File List

- `_bmad-output/implementation-artifacts/m24/1-1-create-routing-model-contract-home.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntent.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/PortPresentationPolicy.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RoutingIdentities.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RoutingPolicy.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/M24RoutingContractTest.kt`

## Change Log

- Added the M24 routing-model contract home and split the routing vocabulary into focused Kotlin files.
- Added a governed terminal-anchor sample contract test covering connection intent, policy, constraints, labels, and quality.
- Verified the routing-model module test suite passes after implementation.

## Status

review
