---
status: review
epic: 3
story: 3.2
title: Integrate route facts into presentation snapshots
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 3.2: Integrate route facts into presentation snapshots

As a renderer developer, I want presentation snapshots to carry route facts, so that Theia can
render routed wires without recalculating meaning.

## Acceptance Criteria

- Presentation/projection snapshots expose route facts to Graphical View.
- Existing node/edge identity remains compatible.
- M23 layout constraints can provide context without being replaced.
- Renderer-facing payloads include route segments, anchors, labels, and quality state.
- Tests prove old edge rendering does not remain the accepted route path for M24 proof cases.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Keep route facts as downstream projection data. Do not move route solving into Theia.

## Tasks/Subtasks

- [x] Add route-fact snapshot exposure to presentation documents.
- [x] Add renderer-facing connector derivation from route facts before legacy edge route points.
- [x] Preserve existing connector fallback behavior when no route facts are present.
- [x] Verify route segments, anchors, route lane, and quality state reach the renderer-facing payload.

## Dev Agent Record

### Debug Log

- Confirmed RED with `:kernel:presentation-model:test --tests "com.engineeringood.athena.presentation.PresentationModelContractTest.presentation snapshots expose route facts instead of accepting old edge route points"`; compilation failed because presentation snapshots did not expose route facts.
- Added a presentation-model dependency on routing-model, optional `routeFactSnapshot`, and `connectorsForRendering()`.
- Added optional `portSemanticId` to `TerminalAnchorFact` so renderer-facing connectors can preserve canonical port identities when available.

### Completion Notes

- Presentation snapshots can now carry M24 route facts and produce renderable connectors from route segments, anchors, labels, lane, and quality state.
- Legacy connector route points remain fallback only when route facts are absent; M24 proof paths do not accept old straight-edge rendering as the routed wire.
- Validation passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test --tests "com.engineeringood.athena.presentation.PresentationModelContractTest.presentation snapshots expose route facts instead of accepting old edge route points"`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`

## File List

- `kernel/presentation-model/build.gradle.kts`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/PortPresentationPolicy.kt`
- `_bmad-output/implementation-artifacts/m24/3-2-integrate-route-facts-into-presentation-snapshots.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`

## Change Log

- 2026-07-19: Completed Story 3.2 route facts in presentation snapshots.

## Status

review
