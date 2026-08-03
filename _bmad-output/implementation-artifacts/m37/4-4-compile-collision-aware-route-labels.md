---
story_key: 4-4-compile-collision-aware-route-labels
epic: m37-e4
requirements: [FR-21]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.4: Compile Collision-Aware Route Labels

Status: review

## Story

As an engineer reading route and device labels,
I want label placement validated against the drawing structure,
so that labels remain legible and their quality evidence is real.

## Acceptance Criteria

1. Every route label fact carries content, bounds, attachment point, class, collision evidence, source provenance, and compiler snapshot.
2. Labels are checked against device bodies, route segments, other labels, frame, grid labels, and title-block regions.
3. Collision-free placement is deterministic for the same source and policy.
4. Unresolved or colliding labels in the valid evidence block acceptance rather than setting a hardcoded clearance-success field.
5. Renderer payload contains compiled label facts only and no label placement inference.
6. Focused label, bounds, collision, determinism, negative tests, source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED route-label validation tests (AC: 1, 2, 3, 4, 5)
  - [x] Add accepted label fact test for content, bounds, attachment, class, provenance, and snapshot.
  - [x] Add collision tests against component body, route segment, other labels, frame/grid/title-block regions.
  - [x] Add deterministic placement test.
  - [x] Add unresolved/colliding label negative success-gate test.
  - [x] Add renderer payload hygiene test.
- [x] Implement collision-aware route label compiler (AC: 1, 2, 3)
  - [x] Use responsibility-named types only; no `Proof`, `Demo`, `Sample`, milestone names, `V0`/`V1`, XML, SVG metadata, renderer authority, or compatibility shims.
  - [x] Derive label facts from route facts and typed policy.
  - [x] Carry bounds and collision evidence as computed facts.
- [x] Implement diagnostics and normalized payload (AC: 4, 5)
  - [x] Block success for unresolved or colliding labels.
  - [x] Normalize label payload to Athena facts only.
  - [x] Keep renderer paint-only.
- [x] Verify and record gates (AC: 6)
  - [x] Run focused route label tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Label placement is compiled evidence. Renderer does not move, hide, infer, or repair labels.
- Bounds and collisions are computed from route facts, component bounds, route segments, other labels, frame, grid labels, and title-block regions.
- This story does not implement visual polish; it prevents false success and creates reliable label evidence for E5.

### Current Code Intelligence

- `RouteFact` already carries `labels: List<RouteLabelFact>`.
- `SchematicLabelModel.kt` defines label ids and placement values.
- `DrawingProfileCompiler.kt` defines label policies.
- Keep this story in `routing-model` unless compiler integration becomes necessary.

### Expected Diagnostics

- `drawing.label.unresolved`
- `drawing.label.collision.component`
- `drawing.label.collision.route`
- `drawing.label.collision.label`
- `drawing.label.collision.frame`
- `drawing.label.collision.grid`
- `drawing.label.collision.title-block`
- `drawing.label.protocol-authority.invalid`

### TDD And Verification

- RED first: focused route label compiler test fails before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.RouteLabelPlacementCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 4.4]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-21]
- [Source: `_bmad-output/implementation-artifacts/m37/4-1-compile-drawing-profiles-and-line-classes.md` - Label policy]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.RouteLabelPlacementCompilerTest` failed before implementation with unresolved route label placement compiler symbols.
- GREEN: focused `RouteLabelPlacementCompilerTest` passed after adding collision-aware route label compiler.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- Hygiene: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- Encoding: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Whitespace: `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added route label placement evidence, bounds, collision detection, diagnostics, deterministic result ordering, and renderer-safe payload.
- Labels are checked against component bounds, route segments, other labels, frame bounds, grid labels, and title-block regions.
- Success blocks for unresolved route labels or any collision diagnostic.
- Renderer payload carries Athena label facts only and no placement inference.

### File List

- _bmad-output/implementation-artifacts/m37/4-4-compile-collision-aware-route-labels.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteLabelPlacementCompiler.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteLabelPlacementCompilerTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 4.4 from finalized M37 PRD, epics, and Story 4.1 label policy learnings.
- 2026-07-30: Implemented collision-aware route label placement evidence, diagnostics, normalized payload, and routing tests.
