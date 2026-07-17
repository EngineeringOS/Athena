---
baseline_commit: 0b43cbe
---

# Story 2.2: Add the Deterministic Layout Optimization Boundary

Status: done

## Story

As an implementer,
I want a deterministic optimization boundary that emits normalized Athena layout facts,
so that renderer and adapter code cannot become layout authority.

## Acceptance Criteria

1. Given layout intent, constraints, rules, and existing facts, when the M22 optimization boundary runs, then it emits normalized Athena layout facts consumed by the renderer.
2. Given optimizer inputs are unordered, when the boundary canonicalizes them, then subject, occurrence, and sheet ordering is stable.
3. Given equal-cost layout choices exist, when the boundary solves them, then deterministic tie-breakers are used.
4. Given the same governed input is optimized repeatedly, when facts are compared, then repeated runs produce the same layout facts.

## Tasks / Subtasks

- [x] Add optimization boundary contracts (AC: 1, 2)
  - [x] Add failing layout-engine tests for canonical optimizer input and normalized fact output.
  - [x] Add optimizer input/result contracts that consume layout intent and constraints.
  - [x] Ensure output stays in Athena layout facts.
- [x] Add deterministic ordering and tie-breakers (AC: 2, 3, 4)
  - [x] Canonicalize intent items and constraints before solving.
  - [x] Use stable subject/occurrence/sheet ordering for equal-cost choices.
  - [x] Cover repeated-run replay with tests.
- [x] Run validation (AC: 1, 2, 3, 4)
  - [x] Run layout-engine tests.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 2.1 added `LayoutConstraintSnapshot` and the constraint vocabulary in `kernel/layout-model`.
- M21 layout-engine already emits placement, region, route, and label facts for rule-based schematic layout.
- M22 needs an explicit optimization boundary before optional adapter work begins.

### Guardrails

- Do not introduce ELK behavior in this story.
- Do not change Theia or renderer behavior in this story.
- Do not persist source layout hints in this story.
- Optimization output must remain normalized Athena facts.

### Testing Requirements

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 2, Story 2.2]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-1, AD-2]
- [Source: `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`]
- [Source: `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` failed first because `SchematicLayoutOptimizationInput` and `RuleBasedSchematicLayoutOptimizer` did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed after adding the optimizer boundary and deterministic replay test.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added a M22 schematic layout optimization boundary that consumes intent snapshots, constraint snapshots, and existing placement facts.
- Added deterministic input canonicalization and stable applied-constraint ordering.
- Kept optimizer output normalized to existing Athena placement and region facts.

### File List

- `_bmad-output/implementation-artifacts/m22/2-2-add-the-deterministic-layout-optimization-boundary.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`

## Change Log

- 2026-07-18: Created M22 Story 2.2 with deterministic optimization boundary requirements.
- 2026-07-18: Added the deterministic schematic layout optimizer boundary and layout-engine test coverage.
