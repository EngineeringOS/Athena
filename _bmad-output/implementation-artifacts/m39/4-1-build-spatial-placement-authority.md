---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.1: Build Spatial Placement Authority

Status: review

## Story

As a compiler maintainer,
I want placement and bounds to be derived in Spatial Reality,
so that projection remains coordinate-free.

## Acceptance Criteria

1. Spatial placement authority derives placements and bounds from `ProjectionDocument` occurrences.
2. `ProjectionToSpatialTransformation` delegates placement and bounds derivation to Spatial authority instead of owning that logic inline.
3. Projection models do not store final coordinates, anchor positions, lanes, routes, labels, stroke, paint order, or renderer state.
4. Placement and bounds identities trace to projection occurrence ids.
5. Missing projection occurrences or missing derived placement facts fail before route or Presentation work with plain diagnostics.
6. No generic graph framework, universal `Fact` base class, compatibility shim, empty wrapper model, milestone-named class, `V0`/`V1`, vague `Evidence`, or `ProfessionalControlDrawing` naming is introduced.

## Tasks / Subtasks

- [x] Establish failing placement authority tests first (AC: 1-6)
  - [x] Add tests proving a dedicated Spatial placement authority derives placements and bounds.
  - [x] Add tests proving identity traces to projection occurrence ids.
  - [x] Add tests proving missing projection nodes produce plain diagnostics.
  - [x] Add tests proving Projection models stay coordinate-free.
  - [x] Add naming guard coverage for new Spatial authority files.
- [x] Implement Spatial placement authority (AC: 1-5)
  - [x] Create a short, concrete production class for placement and bounds derivation.
  - [x] Accept `ProjectionDocument` input and emit Spatial-owned placement result only.
  - [x] Keep deterministic placement simple and honest.
  - [x] Return plain diagnostics instead of silent fallback.
- [x] Refactor Projection to Spatial transformation (AC: 2-5)
  - [x] Remove inline placement and bounds derivation from `ProjectionToSpatialTransformation`.
  - [x] Delegate to Spatial placement authority.
  - [x] Keep anchor, lane, route, and quality work unchanged unless placement result contract requires small cleanup.
  - [x] Preserve Story 3.2 and 3.3 behavior.
- [x] Keep architecture clean (AC: 3, 6)
  - [x] Do not add geometry fields to Projection models.
  - [x] Delete or rename stale incompatible code directly if this story touches it.
  - [x] Do not touch Theia, SVG export, screenshots, professional routing, or label polish.
- [x] Verify and update tracking (AC: 1-6)
  - [x] Run focused compiler, projection, spatial, and presentation tests sequentially.
  - [x] Run broader `gradlew test` because compiler path is affected.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story builds only:

```text
Projection Reality -> Spatial placement and bounds authority
```

It does not implement routing improvement, anchor truth, route endpoint correction, dynamic connection appearance, renderer changes, or screenshots.

### Current Code Intelligence

Story 3.2 put deterministic placement and bounds directly inside:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt`

Story 3.3 added downstream Presentation proof:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`

Spatial root:

- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`

Use CodeGraph before editing. Keep the authority concrete. Do not create a framework.

### Architecture Requirements

- Spatial compiler is the only M39 owner of placement and bounds.
- Projection Reality owns view membership only and remains coordinate-free.
- Placement authority can be simple, but it must be separate from transformation plumbing.
- This is foundation, not professional layout.

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

Story 3.3 proved:

- Presentation transforms from Spatial only.
- Presentation does not repair route geometry.
- Existing stale compatibility tests may need direct deletion or rename when they block current architecture.

Preserve those choices.

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-26 through FR-33]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-4]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E4 Story 4.1]
- [Source: `_bmad-output/implementation-artifacts/m39/3-2-transform-projection-to-spatial.md` - current inline placement behavior]
- [Source: `_bmad-output/implementation-artifacts/m39/3-3-transform-spatial-to-presentation.md` - downstream Presentation proof]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 3.3 entered review.
- Started development from sprint status after story creation.
- Red phase confirmed missing `SpatialPlacementCompiler`.
- Focused placement test passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.SpatialPlacementCompilerTest`
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
- Added `SpatialPlacementCompiler` as the Spatial-owned placement and bounds authority.
- `ProjectionToSpatialTransformation` now delegates placement and bounds derivation instead of keeping that logic inline.
- Projection models remain coordinate-free.

### File List

- _bmad-output/implementation-artifacts/m39/4-1-build-spatial-placement-authority.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionToSpatialTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialPlacementCompilerTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 3.3 output.
- 2026-08-01: Started development.
- 2026-08-01: Implemented Spatial placement authority and delegated Projection to Spatial placement derivation.
