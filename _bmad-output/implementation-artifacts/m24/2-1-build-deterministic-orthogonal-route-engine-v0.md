---
status: review
epic: 2
story: 2.1
title: Build deterministic orthogonal route engine v0
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 2.1: Build deterministic orthogonal route engine v0

As a routing engineer, I want a rule-based Athena route engine v0, so that M24 improves route
fidelity without adopting a generic external router.

## Acceptance Criteria

- Route engine v0 consumes route intent, terminal anchors, component bounds, constraints, and layout
  context.
- It emits grid-aligned horizontal and vertical route segments.
- It avoids component-center attachment.
- Repeated runs on the same input produce identical route facts.
- No ELK, Graphviz, yFiles, or external generic router is introduced.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-5. Keep the algorithm simple enough to verify.

## Tasks/Subtasks

- [x] Add route engine v0 input that consumes route intent, terminal anchors, component bounds, constraints, and layout context.
- [x] Emit deterministic grid-aligned horizontal/vertical route facts.
- [x] Preserve terminal anchors as route endpoints instead of component centers.
- [x] Prove repeated runs are deterministic and no external router is introduced.

## Dev Agent Record

### Implementation Notes

- Added `AthenaRouteEngineV0` in `kernel/routing-model` as a small rule-based route engine.
- Added routing layout context, component bounds, and route request contracts consumed by the engine.
- The engine sorts requests deterministically and emits `RouteFactSnapshot` output attached to terminal anchors.

### Debug Log

- Focused engine test initially failed at `compileTestKotlin` because engine input/result types did not exist.
- Added the minimal engine and supporting contracts.
- `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.AthenaRouteEngineV0Test"` passed.
- `:kernel:routing-model:test` passed.

### Completion Notes

- Implemented deterministic Athena route engine v0 without ELK, Graphviz, yFiles, or external routers.
- Kept the implementation inside the kernel routing model and out of Theia/rendering code.

## File List

- `_bmad-output/implementation-artifacts/m24/2-1-build-deterministic-orthogonal-route-engine-v0.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0Test.kt`

## Change Log

- Added Athena-owned deterministic orthogonal route engine v0.
- Added engine input contracts for layout context, component bounds, and terminal-anchor route requests.
- Verified focused and module-level routing-model tests.

## Status

review
