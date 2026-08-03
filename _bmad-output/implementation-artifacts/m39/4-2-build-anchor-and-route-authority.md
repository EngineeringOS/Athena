---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.2: Build Anchor And Route Authority

Status: review

## Story

As an evaluator,
I want connector endpoints to be computed from placed anchors,
so that visible lines attach to real terminals.

## Acceptance Criteria

1. Spatial anchor and route authority derives anchor positions, lanes, and routes from projection connections plus placed occurrences.
2. `ProjectionToSpatialTransformation` delegates anchor, lane, and route derivation to Spatial authority instead of owning that logic inline.
3. Connector first and last route points equal placed anchor points.
4. Route facts reference source connection identity, occurrence identity, lane identity, and anchor identity.
5. Missing placements, bounds, anchors, lanes, or route prerequisites fail before Presentation Reality with plain diagnostics.
6. Projection Reality does not own route start/end geometry.
7. No generic graph framework, universal `Fact` base class, compatibility shim, empty wrapper model, milestone-named class, `V0`/`V1`, vague `Evidence`, or `ProfessionalControlDrawing` naming is introduced.

## Tasks / Subtasks

- [x] Establish failing anchor and route authority tests first (AC: 1-7)
  - [x] Add tests proving a dedicated Spatial anchor and route authority derives anchors, lanes, and routes.
  - [x] Add tests proving route first and last points equal derived anchor points.
  - [x] Add tests proving route facts carry connection, occurrence, lane, and anchor identities.
  - [x] Add tests proving missing placement or bounds prerequisites produce plain diagnostics.
  - [x] Add tests proving Projection models do not own route endpoint geometry.
  - [x] Add naming guard coverage for new Spatial authority files.
- [x] Extend Spatial route facts only as needed (AC: 3-5)
  - [x] Add source and target anchor ids to `SpatialRoute` if needed for traceability.
  - [x] Keep names short and concrete.
  - [x] Update existing tests and callers directly; no compatibility overloads.
- [x] Implement Spatial anchor and route authority (AC: 1-5)
  - [x] Create a short, concrete production class for anchors, lanes, and routes.
  - [x] Accept `ProjectionDocument`, Spatial placements, and Spatial bounds.
  - [x] Emit Spatial-owned anchors, lanes, and routes only.
  - [x] Return plain diagnostics instead of silent fallback.
- [x] Refactor Projection to Spatial transformation (AC: 2-6)
  - [x] Remove inline anchor, lane, and route derivation from `ProjectionToSpatialTransformation`.
  - [x] Delegate to Spatial anchor and route authority.
  - [x] Preserve Story 3.2, 3.3, and 4.1 behavior.
- [x] Verify and update tracking (AC: 1-7)
  - [x] Run focused compiler, projection, spatial, and presentation tests sequentially.
  - [x] Run broader `gradlew test` because compiler and model APIs are affected.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story builds only:

```text
Spatial anchor, lane, and route authority
```

It does not implement professional routing, crossing minimization, dynamic connection appearance, Theia rendering, SVG export, or screenshots.

### Current Code Intelligence

Story 4.1 added:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt`

Current inline anchor and route logic lives in:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt`

Spatial route facts live in:

- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`

Use CodeGraph before editing. Keep authority concrete. Do not create a framework.

### Architecture Requirements

- Spatial compiler owns anchor positions, lanes, and routes.
- Visible connector endpoints must come from placed anchors.
- Projection cannot own final route points.
- M39 routing stays simple and honest.

### Testing Requirements

Use TDD. Write failing tests before production code.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
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

Story 4.1 proved:

- Placement and bounds are a dedicated Spatial authority.
- `ProjectionToSpatialTransformation` should stay orchestration only.
- Direct cleanup beats compatibility shims.

Preserve those choices.

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-26 through FR-33]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-4]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E4 Story 4.2]
- [Source: `_bmad-output/implementation-artifacts/m39/4-1-build-spatial-placement-authority.md` - placement authority pattern]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 4.1 entered review.
- Started development from sprint status after story creation.
- Red phase confirmed missing `SpatialRouteCompiler` and missing explicit route endpoint identity fields.
- Focused route test passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.SpatialRouteCompilerTest`
- Focused module checks passed sequentially:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
- Full regression and audits passed:
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `SpatialRouteCompiler` as the Spatial-owned anchor, lane, and route authority.
- `SpatialRoute` now carries source/target occurrence and anchor ids.
- Route endpoints now attach from source right anchor to target left anchor for the simple M39 proof.
- `ProjectionToSpatialTransformation` now delegates anchor, lane, and route derivation.

### File List

- _bmad-output/implementation-artifacts/m39/4-2-build-anchor-and-route-authority.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt
- kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt
- kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 4.1 output.
- 2026-08-01: Started development.
- 2026-08-01: Implemented Spatial anchor and route authority with explicit endpoint identity.
