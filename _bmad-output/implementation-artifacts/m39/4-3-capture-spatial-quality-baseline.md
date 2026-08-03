---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.3: Capture Spatial Quality Baseline

Status: review

## Story

As a milestone owner,
I want honest geometry measurements,
so that M40 and M41 start from truth.

## Acceptance Criteria

1. Spatial compiler records baseline measurements for overlap, body intersection, crossing count, twist, lane use, and label pressure.
2. `ProjectionToSpatialTransformation` delegates quality measurement to Spatial quality authority instead of owning it inline.
3. Spatial quality facts are measurements, not professional routing claims.
4. Trust invariant failures such as missing anchors or detached endpoints still fail before Presentation Reality.
5. Non-blocking visual debt is recorded as Spatial quality measurements rather than hidden or repaired.
6. Routing remains documented and tested as a subsystem of Spatial Reality.
7. No generic graph framework, universal `Fact` base class, compatibility shim, empty wrapper model, milestone-named class, `V0`/`V1`, vague `Evidence`, or `ProfessionalControlDrawing` naming is introduced.

## Tasks / Subtasks

- [x] Establish failing quality baseline tests first (AC: 1-7)
  - [x] Add tests proving Spatial quality authority emits required measurement kinds.
  - [x] Add tests proving overlap/body/crossing/twist/lane/label measurements are non-negative.
  - [x] Add tests proving `ProjectionToSpatialTransformation` carries quality facts from the authority.
  - [x] Add tests proving quality facts do not claim professional routing.
  - [x] Add naming guard coverage for new Spatial quality files.
- [x] Implement Spatial quality authority (AC: 1-5)
  - [x] Create a short, concrete production class for quality measurements.
  - [x] Accept placements, bounds, lanes, and routes.
  - [x] Emit Spatial-owned quality measurements only.
  - [x] Keep calculations simple and honest.
- [x] Refactor Projection to Spatial transformation (AC: 2-5)
  - [x] Remove inline quality measurement logic from `ProjectionToSpatialTransformation`.
  - [x] Delegate to Spatial quality authority.
  - [x] Preserve Story 3.2 through 4.2 behavior.
- [x] Verify routing subsystem documentation and tests (AC: 6-7)
  - [x] Keep Spatial Reality declaration saying routing is Spatial-owned.
  - [x] Ensure tests prove route ownership stays in Spatial.
- [x] Verify and update tracking (AC: 1-7)
  - [x] Run focused compiler, spatial, projection, and presentation tests sequentially.
  - [x] Run broader `gradlew test` because compiler and model path is affected.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story builds only:

```text
Spatial quality baseline
```

It does not optimize routes, claim professional drawing quality, change Theia, change SVG export, or capture screenshots.

### Current Code Intelligence

Story 4.1 added:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt`

Story 4.2 added:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- explicit source/target occurrence and anchor ids on `SpatialRoute`.

Current quality logic is inline in:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt`

Use CodeGraph before editing. Keep measurements direct.

### Architecture Requirements

- Spatial Reality owns geometry quality measurements.
- Quality facts expose debt; they do not repair geometry.
- M39 may say quality is measured. M39 must not say routing is professional.

### Testing Requirements

Use TDD. Write failing tests before production code.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
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

Story 4.2 proved:

- Routes attach from placed anchors.
- Route facts carry connection, occurrence, lane, and anchor identities.
- Projection to Spatial should delegate real work to focused Spatial authorities.

Preserve those choices.

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-26 through FR-33]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-4]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E4 Story 4.3]
- [Source: `_bmad-output/implementation-artifacts/m39/4-2-build-anchor-and-route-authority.md` - route authority pattern]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 4.2 entered review.
- Started development from sprint status after story creation.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added Spatial quality authority that records baseline counts for overlap, body intersection, crossing, twist, lane use, label pressure, routes, and lanes.
- `ProjectionToSpatialTransformation` now delegates quality measurement to Spatial-owned code instead of measuring inline.
- Quality facts are debt measurements only; tests guard against professional-routing claims and banned naming.
- Verification passed: focused quality test, compiler/spatial/projection/presentation module tests, full `gradlew test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### File List

- _bmad-output/implementation-artifacts/m39/4-3-capture-spatial-quality-baseline.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 4.2 output.
- 2026-08-01: Started development.
- 2026-08-01: Completed Spatial quality baseline authority and marked ready for review.
