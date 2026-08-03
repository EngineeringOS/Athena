---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.1: Define Four Reality Roots

Status: review

## Story

As a compiler maintainer,
I want concrete roots for Engineering, Projection, Spatial, and Presentation realities,
so that each stage has one clear owner.

## Acceptance Criteria

1. Engineering Reality has a clear purpose and reuses or cleans the existing `EngineeringDocument` root where possible.
2. Projection Reality has a clear purpose as a view-specific engineering document without coordinates or style.
3. Spatial Reality has a clean root for placement, bounds, anchor positions, alignment, lanes, and routes.
4. Presentation Reality has a clear root for paintable facts only.
5. No generic graph framework, universal `Fact` base class, or empty wrapper model is introduced.

## Tasks / Subtasks

- [x] Establish root contract tests first (AC: 1-5)
  - [x] Add tests proving each reality exposes a concrete purpose, owner, and owned fact list.
  - [x] Add a spatial root test proving `SpatialDocument` owns placement, bounds, anchors, alignments, lanes, and routes.
  - [x] Add a guard test proving no M39 root uses a universal `Fact` base or empty wrapper.
- [x] Define Engineering Reality root using existing engineering model (AC: 1, 5)
  - [x] Add a small concrete root descriptor beside `EngineeringDocument`.
  - [x] Keep `EngineeringDocument` constructor and current callers unchanged.
  - [x] Do not rename or wrap `EngineeringDocument`.
- [x] Define Projection Reality root purpose without moving spatial facts yet (AC: 2, 5)
  - [x] Add a concrete root descriptor beside `ProjectionDocument`.
  - [x] State that Projection Reality is a view-specific engineering document without coordinates or style.
  - [x] Do not remove projection spatial fields in this story; Story 2.2 owns that cut.
- [x] Define Spatial Reality root (AC: 3, 5)
  - [x] Add `SpatialDocument` as the real spatial root in `kernel:spatial-model`.
  - [x] Model only root-level spatial result facts needed by M39: placements, bounds, anchor positions, alignments, lanes, routes.
  - [x] Keep routing documented as a subsystem of Spatial Reality.
  - [x] Do not use `intent` naming in new Spatial root classes.
- [x] Define Presentation Reality root purpose using existing presentation model (AC: 4, 5)
  - [x] Add a concrete root descriptor beside `PresentationDocument`.
  - [x] Keep `PresentationDocument` constructor and current callers unchanged.
  - [x] Do not rename stale presentation terms in this story unless directly needed for the root proof.
- [x] Verify and update tracking (AC: 1-5)
  - [x] Run affected module tests sequentially.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story proves the four roots. It does not implement the transformation chain, does not remove spatial facts from projection models, does not refactor presentation naming, and does not touch Theia.

The root design must be concrete:

- use existing roots where they already exist;
- add `SpatialDocument` because Spatial Reality lacks a clean root;
- use small root descriptors only to state purpose, owner, and owned facts;
- do not create a graph framework, generic `Fact` hierarchy, or empty wrappers.

### Current Code Intelligence

- `EngineeringDocument` already exists in `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt` and is used by compiler, runtime, routing, and tests. Keep its constructor stable.
- `ProjectionDocument` exists in `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`. It currently includes spatial-looking fields such as canvas size, electrical anchors, endpoints, and routing corridors. Do not remove them here; Story 2.2 owns that hard cut.
- `kernel:spatial-model` existed with a stale `SemanticSpatialIntentModels.kt` main model. It had no production dependents and was deleted under the pre-1.0 cleanup rule.
- `PresentationDocument` exists in `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`. It is the active renderer-facing root and is used by compiler, LSP, SVG export, and tests. Keep its constructor stable.

### Implementation Hints

Recommended files:

- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringReality.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReality.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt`

Use direct names. Avoid `ProfessionalControlDrawing`, `Evidence`, milestone names, `V0`, `V1`, `Proof`, `Demo`, and `Sample` in production names.

### Testing Requirements

Use TDD. Write failing tests before production code.

Run Gradle commands sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
```

Final audits:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-9 through FR-19]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-1 through AD-6]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E2 Story 2.1]
- [Source: `_bmad-output/implementation-artifacts/m39/1-3-remove-intent-from-normal-m39-source.md` - previous story guardrails]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created from corrected M39 sprint plan after Epic 1 completion.
- Started development from sprint status after story creation.
- Red test: `:kernel:engineering-model:test --tests com.engineeringood.athena.ir.EngineeringRealityTest` failed because `EngineeringReality` did not exist.
- Added concrete reality descriptors for Engineering, Projection, Spatial, and Presentation roots.
- Added `SpatialDocument` and removed stale `SemanticSpatialIntentModels.kt` plus its stale test.
- Verification passed sequentially for engineering, projection, spatial, and presentation model modules.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Defined four concrete M39 Reality roots without a generic graph framework, universal `Fact` base, or empty wrapper model.
- Kept `EngineeringDocument`, `ProjectionDocument`, and `PresentationDocument` constructors stable.
- Added `SpatialDocument` as the first clean Spatial Reality root with placements, bounds, anchor positions, alignments, lanes, and routes.
- Removed the unused stale `SemanticSpatialIntent` model and test because it conflicts with the current no-`intent` architecture.
- Verification passed: affected module tests, source-set hygiene audit, encoding audit, and `git diff --check`.

### File List

- _bmad-output/implementation-artifacts/m39/2-1-define-four-reality-roots.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringReality.kt
- kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringRealityTest.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReality.kt
- kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionRealityTest.kt
- kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SemanticSpatialIntentModels.kt (deleted)
- kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt
- kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SemanticSpatialIntentModelTest.kt (deleted)
- kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRealityTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 1.3 guardrails.
- 2026-08-01: Started development.
- 2026-08-01: Added four Reality root descriptors, introduced `SpatialDocument`, deleted stale spatial intent model, and completed verification.
