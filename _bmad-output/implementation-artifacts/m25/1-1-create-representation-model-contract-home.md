---
status: ready-for-dev
baseline_commit: 265e26109ebf75352fdf9db3f814edcc77bbf546
epic: 1
story: 1.1
title: Create representation-model contract home
---

# Story 1.1: Create representation-model contract home

## Story

As an Athena architect,
I want `kernel/representation-model` to own presentation anatomy contracts,
So that schematic symbols do not become the root architecture.

## Acceptance Criteria

- `kernel/representation-model` exposes contracts for representation ids, presentation anatomy,
  bounds, hotspots, presentation primitives, terminal points, and label anchors.
- `SymbolAnatomy` is modeled as the electrical schematic subset of presentation anatomy.
- The model has no dependency on Theia, DOM, canvas state, desktop-viewer, Compose, KMP frontend, or
  QElectroTech runtime code.
- Tests prove the model can represent the mandatory M25 component families without fallback.

## Tasks/Subtasks

- [x] Add representation-model contracts.
- [x] Add schematic symbol anatomy subset.
- [x] Add contract tests for mandatory M25 families.
- [x] Verify dependency boundaries.

## Dev Notes

- Source PRD: `../../planning-artifacts/prds/prd-Athena-2026-07-19-m25/prd.md`
- Architecture: `../../planning-artifacts/architecture/architecture-Athena-2026-07-19-m25/ARCHITECTURE-SPINE.md`
- Governed by AD-1, AD-2, AD-4, AD-8, AD-9.
- Keep implementation in Theia IDE/runtime path only.

## Dev Agent Record

### Debug Log

- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`; compile failed because representation contracts did not exist.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.

### Completion Notes

- Added `:kernel:representation-model` as a Kotlin JVM kernel module.
- Added pure representation contracts for ids, geometry, presentation anatomy, primitives,
  terminal points, label anchors, and schematic `SymbolAnatomy`.
- Kept the module independent of Theia, DOM/canvas, desktop-viewer, Compose, KMP frontend, and
  QElectroTech runtime code.
- Added contract tests proving the mandatory M25 families can be represented without fallback.

### File List

- `settings.gradle.kts`
- `kernel/representation-model/build.gradle.kts`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationIds.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationGeometry.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/PresentationAnatomy.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationModelModuleMarker.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationModelContractTest.kt`

## Change Log

- 2026-07-19: Implemented representation-model contract home and tests.

## Status

review
