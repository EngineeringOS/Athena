---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.1: Build Dynamic Connection Appearance

Status: review

## Story

As an engineer,
I want each visible connection to have clear style facts,
so that line weight, style, color, marker, and label are controlled in the right layer.

## Acceptance Criteria

1. Presentation compiler derives connector style, weight, color, marker, label, and label position from Presentation-owned rules.
2. Optional source properties such as `style`, `label`, and `position` override presentation defaults only; they do not change Engineering, Projection, or Spatial truth.
3. `SpatialToPresentationTransformation` delegates connector appearance to a focused Presentation authority instead of hardcoding style inline.
4. Presentation connector facts remain concrete and human-readable; no vague `Evidence`, `ProfessionalControlDrawing`, milestone names, `V0`/`V1`, generic graph framework, universal `Fact` base class, or compatibility shim is introduced.
5. Theia, SVG export, Spatial compiler, and Projection models do not infer, repair, or own connector appearance.
6. Missing required style, label, marker, or position facts fail closed with plain diagnostics before renderer publication.
7. Existing endpoint trust remains intact: connector route first and last points still equal source and target endpoint points.

## Tasks / Subtasks

- [x] Establish failing presentation appearance tests first (AC: 1-7)
  - [x] Add tests proving connector appearance includes style, weight, color, marker, label, and label position.
  - [x] Add tests proving optional relation properties affect only Presentation connector facts.
  - [x] Add tests proving `SpatialToPresentationTransformation` delegates appearance to Presentation-owned code.
  - [x] Add tests proving missing visual facts fail before publication.
  - [x] Add naming guard coverage for new Presentation files and tests.
- [x] Implement Presentation appearance authority (AC: 1, 3-6)
  - [x] Create a short, concrete production class for connector appearance.
  - [x] Reuse `PresentationConnectorLine`, `PresentationConnectorLabel`, and `PresentationConnectionMarker` where possible.
  - [x] Keep marker, style, label, and position names plain.
  - [x] Keep calculations direct; do not add a generic style framework.
- [x] Wire Spatial to Presentation transformation (AC: 1, 3, 5, 7)
  - [x] Remove inline connector style defaults from `SpatialToPresentationTransformation`.
  - [x] Delegate line and label creation to the Presentation appearance authority.
  - [x] Preserve endpoint equality and existing Spatial route behavior.
- [x] Keep appearance out of wrong owners (AC: 2, 5)
  - [x] Ensure Engineering, Projection, and Spatial models do not gain presentation style authority.
  - [x] Ensure Theia and SVG export remain paint-only consumers of Presentation facts.
- [x] Verify and update tracking (AC: 1-7)
  - [x] Run focused compiler and presentation-model tests sequentially.
  - [x] Run spatial and projection model tests because transformation boundaries are affected.
  - [x] Run broader `gradlew test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story builds only:

```text
dynamic connector appearance facts in Presentation Reality
```

It does not improve routing, move endpoints, introduce Diagram Grammar, change Theia behavior, change SVG export behavior, or capture screenshots.

### Current Code Intelligence

Use CodeGraph before editing. Current direct targets:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
  - currently builds `PresentationConnectorLine` inline with fixed `weight = 1.5`, `style = "solid"`, `colorKey = "connection"`, `labelPolicy = "route-label"`;
  - currently builds `PresentationConnectorLabel` inline from `routeId`.
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
  - owns `PresentationConnector`, `PresentationConnectorLine`, `PresentationConnectorLabel`, and `PresentationConnectionMarker`;
  - already validates endpoint equality and orthogonal route points.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt`
  - older route-fact path already maps line and labels to presentation connectors;
  - do not copy stale `ConnectionPresentationLineEvidence` naming into new M39 code.

Expected new/changed files are likely:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectionAppearanceCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationConnectionAppearanceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`

### Architecture Requirements

- Presentation Reality owns visual styling, labels, markers, visibility, theme result, and paint order. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5]
- Renderer cannot repair truth. Theia and SVG export must not snap endpoints, infer topology, reroute, relabel, or apply domain rules. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-7]
- New M39 naming must be direct and human-readable. Avoid vague `Evidence` terms for product concepts. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-8]
- Clean architecture wins over compatibility. Delete stale incompatible paths instead of adding shims. [Source: `AGENTS.md` - Pre-1.0 Architecture Rule]

### Implementation Guidance

- Prefer a concrete API shaped like:

```kotlin
class PresentationConnectionAppearanceCompiler {
    fun lineFor(route: SpatialRoute): PresentationConnectorLine
    fun labelsFor(route: SpatialRoute, points: List<PresentationPoint>): List<PresentationConnectorLabel>
    fun markersFor(route: SpatialRoute): List<PresentationConnectionMarker>
}
```

- If source relation properties are not yet available in the Spatial input, create the Presentation authority and defaults now, and add tests that prove properties are Presentation-only through the current available model path. Do not add a broad property framework.
- Keep approved `to` / `->` alias behavior untouched.
- Do not reintroduce `intent` blocks.
- Do not add Java2D, XML runtime authority, or frontend repair.

### Previous Story Intelligence

Story 4.3 established:

- Spatial quality is measured by `SpatialQualityCompiler`.
- `ProjectionToSpatialTransformation` delegates Spatial-owned responsibilities to focused authorities.
- Quality facts are honest debt measurements, not professional routing claims.

Apply the same pattern here: `SpatialToPresentationTransformation` should delegate Presentation-owned appearance to a focused authority.

### Testing Requirements

Use TDD. Write failing tests before production code.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
```

Regression and hygiene:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-34 through FR-40]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5, AD-7, AD-8]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E5 Story 5.1]
- [Source: `_bmad-output/implementation-artifacts/m39/4-3-capture-spatial-quality-baseline.md` - delegation pattern]
- [Source: `AGENTS.md` - Source-Set Hygiene Rule, Pre-1.0 Architecture Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 4.3 entered review.
- Started development from sprint status after story creation.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `ConnectionPaintCompiler` as the focused Presentation-owned connector appearance authority.
- `SpatialToPresentationTransformation` now delegates connector line, label, and marker ids to the paint authority.
- Optional paint overrides affect Presentation facts only in tests; Spatial route equality remains unchanged.
- Verification passed: focused paint and transformation tests, compiler/presentation/spatial/projection module tests, full `gradlew test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### File List

- _bmad-output/implementation-artifacts/m39/5-1-build-dynamic-connection-appearance.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 4.3 output.
- 2026-08-01: Started development.
- 2026-08-01: Completed dynamic connection paint authority and marked ready for review.
