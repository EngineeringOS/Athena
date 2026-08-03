---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.1: Transform Engineering To Projection

Status: review

## Story

As a compiler maintainer,
I want Engineering Reality to lower into Projection Reality through one typed transformation,
so that views do not corrupt engineering truth.

## Acceptance Criteria

1. A small typed transformation interface exists for M39 reality transitions and does not introduce preserved/derived/discarded metadata.
2. Engineering to Projection accepts `EngineeringDocument` and emits `ProjectionDocument` only.
3. The transformation preserves engineering source trace, engineering identity, connection identity, and hierarchy into projection-owned occurrences, sheets, and reading order.
4. Diagnostics name the missing engineering or projection fact plainly.
5. No generic graph framework, universal `Fact` base class, compatibility shim, empty wrapper model, milestone-named class, `V0`/`V1`, vague `Evidence`, or `ProfessionalControlDrawing` naming is introduced.

## Tasks / Subtasks

- [x] Establish failing transformation tests first (AC: 1-5)
  - [x] Add tests proving the typed transformation interface returns a `ProjectionDocument` from an `EngineeringDocument`.
  - [x] Add tests proving component and connection identities are preserved in projection nodes and connections.
  - [x] Add tests proving sheet identity and reading order are emitted by Projection Reality.
  - [x] Add tests proving missing engineering or projection facts produce plain diagnostics.
  - [x] Add naming guard coverage for new transformation files.
- [x] Add the thin transformation interface (AC: 1, 5)
  - [x] Keep it concrete and small: equivalent to `RealityTransformation<InputReality, OutputReality>`.
  - [x] Do not add transformation metadata for preserved, derived, discarded, or validation facts.
  - [x] Do not create graph nodes, fact base classes, wrappers, shims, or compatibility paths.
- [x] Implement Engineering to Projection transformation (AC: 2-4)
  - [x] Use the existing `EngineeringDocument` root as input.
  - [x] Emit the existing `ProjectionDocument` root as output.
  - [x] Derive projection nodes from engineering components and ports where current projection model supports them.
  - [x] Derive projection connections from engineering connections.
  - [x] Derive projection sheet facts and reading order without coordinates, anchors, lanes, routes, labels, stroke, or paint order.
- [x] Wire validation boundaries without expanding scope (AC: 3-4)
  - [x] Run Engineering Reality validation before transformation.
  - [x] Run Projection Reality validation before returning success.
  - [x] Map validation failures to plain transformation diagnostics.
  - [x] Keep renderer, Spatial Reality, Presentation Reality, Theia, SVG export, and screenshots untouched in this story.
- [x] Clean stale naming discovered during implementation (AC: 5)
  - [x] Rename/delete incompatible stale production code directly; do not add aliases.
  - [x] Update tests/docs only where needed for current M39 architecture.
  - [x] Keep `to` and `->` alias behavior untouched.
- [x] Verify and update tracking (AC: 1-5)
  - [x] Run focused module tests sequentially.
  - [x] Run broader `gradlew test` if compiler/runtime/LSP are affected.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story starts Epic 3. It proves the first typed reality transformation only:

```text
Engineering Reality -> Projection Reality
```

It does not implement Projection to Spatial, Spatial to Presentation, placement, routing, labels, Theia rendering, SVG export, or screenshots.

Do not change Athena source syntax. Do not restore `intent`. Do not add compatibility code. `to` and `->` remain the only approved alias behavior and must stay on the existing compiler path.

### Current Code Intelligence

Story 2.3 added:

- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/RealityDeclaration.kt`
- `EngineeringReality.validate(document: EngineeringDocument)`
- `ProjectionReality.validate(document: ProjectionDocument)`
- `SpatialReality.validate(document: SpatialDocument)`
- `PresentationReality.validate(document: PresentationDocument)`

Current roots:

- `EngineeringDocument` in `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- `ProjectionDocument` in `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`

Existing compiler code already derives projection models:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`

Use CodeGraph before editing. Prefer refactoring an existing projection derivation path into the new typed transformation over duplicating logic.

### Architecture Requirements

- Engineering Reality owns engineering truth: systems, devices, ports, signals, connections, networks, constraints.
- Projection Reality owns view selection only: view, sheet, occurrence, projection group, reading order.
- Projection Reality must not own coordinates, anchor positions, lanes, routes, stroke, labels, paint order, or renderer state.
- M39 transformation interface must stay thin. No metadata contract yet.
- Diagnostics must be plain and product-named.
- New names must be short and human-readable.

### Suggested Product Names

Allowed:

- `RealityTransformation`
- `RealityTransformationDiagnostic`
- `RealityTransformationResult`
- `EngineeringToProjectionTransformation`

Avoid:

- `M39EngineeringProjection...`
- `ProjectionEvidence...`
- `ProfessionalControlDrawing...`
- `FactBase`
- `RealityGraphNode`
- `V0` / `V1`
- `Compatibility...`

### Testing Requirements

Use TDD. Write failing tests before production code.

Likely focused tests:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
```

Run broader checks if shared compiler APIs affect runtime/LSP:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### Previous Story Intelligence

Story 2.3 proved that validation belongs to each reality root and should stay direct:

- reality declarations are concrete product contracts;
- validation reports `reality` plus plain `message`;
- the four realities avoid milestone wording in touched root declarations;
- no graph framework, universal fact base, or compatibility shim was added.

Keep that style for transformations.

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-20 through FR-25]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-6 and Reality Transformations]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E3 Story 3.1]
- [Source: `_bmad-output/implementation-artifacts/m39/2-3-declare-reality-authority-and-validation.md` - validation foundation]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 2.3 entered review.
- Started development from sprint status after story creation.
- Used CodeGraph before editing the current projection derivation path.
- Red phase confirmed with missing `RealityTransformation` and `EngineeringToProjectionTransformation`.
- Focused checks passed sequentially:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.EngineeringToProjectionTransformationTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test`
- Full regression and audits passed:
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added the thin `RealityTransformation<InputReality, OutputReality>` interface with typed success/failure result.
- Added `EngineeringToProjectionTransformation` from `EngineeringDocument` to `ProjectionDocument`.
- The transformation validates Engineering Reality before lowering and Projection Reality before success.
- Projection output preserves authored component, port, and connection identities into projection nodes, connections, sheet subjects, and reading order.
- Removed an accidental id sort so authored order remains the first projection reading order.

### File List

- _bmad-output/implementation-artifacts/m39/3-1-transform-engineering-to-projection.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformationTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 2.3 output.
- 2026-08-01: Started development.
- 2026-08-01: Implemented Engineering Reality to Projection Reality typed transformation and validation-backed diagnostics.
