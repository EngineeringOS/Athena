---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.2: Transform Projection To Spatial

Status: review

## Story

As a compiler maintainer,
I want Projection Reality to lower into Spatial Reality through one typed transformation,
so that coordinates and routes do not leak backward.

## Acceptance Criteria

1. Projection to Spatial uses the existing thin `RealityTransformation<InputReality, OutputReality>` interface.
2. The transformation accepts `ProjectionDocument` and emits `SpatialDocument` only.
3. Placement, bounds, anchor positions, alignment, lanes, routes, and basic quality measurements are derived in Spatial ownership.
4. Missing projection facts, placement facts, or anchor facts stop before Presentation Reality with plain diagnostics.
5. No projection model stores final coordinates, anchor positions, lanes, routes, labels, stroke, paint order, or renderer state.
6. No generic graph framework, universal `Fact` base class, compatibility shim, empty wrapper model, milestone-named class, `V0`/`V1`, vague `Evidence`, or `ProfessionalControlDrawing` naming is introduced.

## Tasks / Subtasks

- [x] Establish failing transformation tests first (AC: 1-6)
  - [x] Add tests proving Projection to Spatial returns `SpatialDocument` from `ProjectionDocument`.
  - [x] Add tests proving placements and bounds are derived from projection occurrences without mutating Projection Reality.
  - [x] Add tests proving route facts reference projection connection identity and lane identity.
  - [x] Add tests proving missing projection facts or route prerequisites produce plain diagnostics.
  - [x] Add naming guard coverage for new transformation files.
- [x] Implement Projection to Spatial transformation (AC: 1-4)
  - [x] Use `ProjectionDocument` as input and `SpatialDocument` as output.
  - [x] Reuse `RealityTransformation` from Story 3.1.
  - [x] Run Projection Reality validation before transformation.
  - [x] Run Spatial Reality validation before success.
  - [x] Map validation failures to plain transformation diagnostics.
- [x] Derive first spatial facts conservatively (AC: 3-5)
  - [x] Derive deterministic placements for projection nodes.
  - [x] Derive deterministic bounds for each placed occurrence.
  - [x] Derive anchor positions from placement and bounds using simple stable edge points.
  - [x] Derive a lane and route for each projection connection using source identity, not renderer repair.
  - [x] Keep routing simple; do not claim professional routing or optimization.
- [x] Keep architecture clean (AC: 5-6)
  - [x] Do not add coordinates, anchors, lanes, route points, style, label, visibility, or paint order to Projection models.
  - [x] Delete or rename stale incompatible code directly if discovered.
  - [x] Keep Theia, SVG export, Presentation Reality, and screenshots untouched in this story.
- [x] Verify and update tracking (AC: 1-6)
  - [x] Run focused module tests sequentially.
  - [x] Run broader `gradlew test` if compiler/runtime/LSP are affected.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story proves only:

```text
Projection Reality -> Spatial Reality
```

It does not implement Spatial to Presentation, Presentation compiler, Theia rendering, SVG export, screenshots, professional routing, or auto-layout.

### Current Code Intelligence

Story 3.1 added:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`

Story 2.3 added:

- `ProjectionReality.validate(document: ProjectionDocument)`
- `SpatialReality.validate(document: SpatialDocument)`

Current Spatial root:

- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`

Use CodeGraph before editing. Keep output small and deterministic.

### Architecture Requirements

- Projection Reality owns view membership and reading order only.
- Spatial Reality owns all geometry result.
- Routing is a subsystem of Spatial Reality.
- This story may create simple deterministic geometry as a proof, but must not claim professional layout or routing.
- Projection remains coordinate-free after this story.

### Testing Requirements

Use TDD. Write failing tests before production code.

Likely focused tests:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
```

Run broader checks if compiler APIs affect runtime/LSP:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### Previous Story Intelligence

Story 3.1 proved:

- `RealityTransformation` stays thin;
- success/failure result is enough for M39;
- Engineering validation failures map directly to transformation diagnostics;
- authored order should be preserved before derived ordering rules exist.

Preserve those choices.

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-20 through FR-25, FR-26 through FR-33]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-4 and AD-6]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E3 Story 3.2]
- [Source: `_bmad-output/implementation-artifacts/m39/3-1-transform-engineering-to-projection.md` - first transformation pattern]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 3.1 entered review.
- Started development from sprint status after story creation.
- Used CodeGraph before editing projection and spatial roots.
- Red phase confirmed missing compiler dependency on `spatial-model` and missing `ProjectionToSpatialTransformation`.
- Focused checks passed sequentially:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.ProjectionToSpatialTransformationTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test`
- Full regression and audits passed:
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `ProjectionToSpatialTransformation` using the existing thin `RealityTransformation` interface.
- Added compiler dependency on `spatial-model` because the compiler now owns the typed Projection to Spatial transition.
- Spatial output now derives placements, bounds, anchor positions, alignment, lanes, routes, and basic quality measurements.
- Added `SpatialQualityMeasurement` as a Spatial-owned fact.
- Projection models remain coordinate-free; no Projection fields were added for geometry, routes, labels, stroke, visibility, or paint order.

### File List

- _bmad-output/implementation-artifacts/m39/3-2-transform-projection-to-spatial.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/compiler/build.gradle.kts
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformationTest.kt
- kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt
- kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 3.1 output.
- 2026-08-01: Started development.
- 2026-08-01: Implemented Projection Reality to Spatial Reality typed transformation and Spatial-owned quality facts.
