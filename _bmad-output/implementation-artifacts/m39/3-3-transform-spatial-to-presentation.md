---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.3: Transform Spatial To Presentation

Status: review

## Story

As a compiler maintainer,
I want Spatial Reality to lower into Presentation Reality through one typed transformation,
so that the renderer cannot become a compiler.

## Acceptance Criteria

1. Spatial to Presentation uses the existing thin `RealityTransformation<InputReality, OutputReality>` interface.
2. The transformation accepts `SpatialDocument` and emits `PresentationDocument` only.
3. Presentation output derives shape paint facts, connector paint facts, labels, style, visibility, and paint order from Spatial facts.
4. Incomplete Spatial or Presentation visual facts fail before publication with plain diagnostics.
5. Presentation does not repair Spatial routes, infer endpoints, change engineering meaning, or introduce Theia/SVG behavior.
6. No generic graph framework, universal `Fact` base class, compatibility shim, empty wrapper model, milestone-named class, `V0`/`V1`, vague `Evidence`, or `ProfessionalControlDrawing` naming is introduced.

## Tasks / Subtasks

- [x] Establish failing transformation tests first (AC: 1-6)
  - [x] Add tests proving Spatial to Presentation returns `PresentationDocument` from `SpatialDocument`.
  - [x] Add tests proving placed Spatial bounds become Presentation occurrences and shape paint facts.
  - [x] Add tests proving Spatial routes become Presentation connectors with the same first and last points.
  - [x] Add tests proving labels, connector line style, visibility, and paint order are explicit.
  - [x] Add tests proving missing Spatial or Presentation facts produce plain diagnostics.
  - [x] Add naming guard coverage for new transformation files.
- [x] Implement Spatial to Presentation transformation (AC: 1-4)
  - [x] Use `SpatialDocument` as input and `PresentationDocument` as output.
  - [x] Reuse `RealityTransformation` from Story 3.1.
  - [x] Run Spatial Reality validation before transformation.
  - [x] Run Presentation Reality validation before success.
  - [x] Map validation failures to plain transformation diagnostics.
- [x] Derive first presentation facts conservatively (AC: 3-5)
  - [x] Derive one primitive shape pack for Spatial placements.
  - [x] Derive `PresentationOccurrence` facts from Spatial placements and bounds.
  - [x] Derive `PresentationConnector` facts from Spatial routes without route repair.
  - [x] Derive basic connector labels from route identity.
  - [x] Derive `PresentationDrawingComposition` as visibility and paint order authority.
- [x] Keep architecture clean (AC: 5-6)
  - [x] Do not add engineering meaning, projection grouping, route ownership, or renderer state to Presentation.
  - [x] Do not touch Theia, SVG export, screenshots, professional routing, or label engine polish in this story.
  - [x] Delete or rename stale incompatible code directly if this story touches it.
- [x] Verify and update tracking (AC: 1-6)
  - [x] Run focused compiler, spatial, and presentation tests sequentially.
  - [x] Run broader `gradlew test` because compiler and model APIs are affected.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story proves only:

```text
Spatial Reality -> Presentation Reality
```

It does not implement Theia rendering, SVG export, screenshots, professional routing, dynamic connection appearance policy, or final label engine. Those belong to later M39 stories.

### Current Code Intelligence

Story 3.1 added:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`

Story 3.2 added:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- Spatial-owned quality measurements.

Current Presentation root:

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDrawingComposition.kt`

Use CodeGraph before editing. Keep output small and deterministic.

### Architecture Requirements

- Spatial Reality owns geometry result.
- Presentation Reality owns paint facts only.
- Presentation compiler cannot repair route geometry. Connector first and last points must come from Spatial route points.
- Theia and SVG export remain paint-only consumers of later Presentation output.
- New names must stay human-first, short, and concrete.

### Testing Requirements

Use TDD. Write failing tests before production code.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
```

Regression and hygiene:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### Previous Story Intelligence

Story 3.2 proved:

- Projection to Spatial stays a thin typed transformation.
- Projection remains coordinate-free.
- Spatial owns placement, bounds, anchor positions, lanes, routes, and quality measurements.
- Missing prerequisites fail before entering the next reality.
- Simple geometry is allowed as a proof, but no professional routing claim is allowed.

Preserve those choices.

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-20 through FR-25, FR-34 through FR-40]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5 and AD-6]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E3 Story 3.3]
- [Source: `_bmad-output/implementation-artifacts/m39/3-2-transform-projection-to-spatial.md` - previous transformation pattern]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 3.2 entered review.
- Started development from sprint status after story creation.
- Red phase confirmed missing `SpatialToPresentationTransformation`.
- Focused story test passed after implementation:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.SpatialToPresentationTransformationTest`
- Focused module checks passed sequentially:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
- Full regression and audits passed:
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `SpatialToPresentationTransformation` through the existing thin `RealityTransformation` interface.
- Spatial output now lowers into a `PresentationDocument` with shape pack, occurrences, connectors, labels, line style, and drawing composition.
- Connector route points are copied from Spatial routes; Presentation does not snap or repair endpoints.
- Cut stale M34 compatibility ledger assertion that blocked valid current `PresentationPrimitive` model usage.

### File List

- _bmad-output/implementation-artifacts/m39/3-3-transform-spatial-to-presentation.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/CabinetRenderPathDeletionGateTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34CabinetRenderPathDeletionGateTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 3.2 output.
- 2026-08-01: Started development.
- 2026-08-01: Implemented Spatial Reality to Presentation Reality typed transformation and removed stale M34 compatibility ledger gate.
