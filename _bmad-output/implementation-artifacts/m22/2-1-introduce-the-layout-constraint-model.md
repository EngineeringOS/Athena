---
baseline_commit: 0b43cbe
---

# Story 2.1: Introduce the Layout Constraint Model

Status: done

## Story

As an architect,
I want layout constraints to be explicit between layout intent and layout facts,
so that optimization is guided by engineering relationships instead of raw canvas coordinates.

## Acceptance Criteria

1. Given governed layout intent for the M22 sample, when layout constraints are derived, then constraints can express near, below, aligned-with, grouped-with, preferred-zone, preserve-order, and route-lane preference relationships.
2. Given layout constraints are inspected, when canonical identities are available, then constraints carry canonical subject, occurrence, sheet/view, snapshot, and source identities.
3. Given the constraint model is used by future optimization, when authored constraints are represented, then raw `x/y` coordinates are not the primary authored constraint language.

## Tasks / Subtasks

- [x] Add layout-constraint model contracts (AC: 1, 2, 3)
  - [x] Add failing model tests for the supported M22 constraint vocabulary.
  - [x] Add model types for constraint ids, subjects, targets, kinds, route-lane preferences, and snapshots.
  - [x] Canonicalize constraint snapshots deterministically.
- [x] Preserve canonical identity in constraints (AC: 2)
  - [x] Carry subject, occurrence, sheet, view, snapshot, and source-span identity where available.
  - [x] Cover identity preservation with model tests.
- [x] Guard against raw coordinate constraints (AC: 3)
  - [x] Keep authored constraints relationship/zone/lane based.
  - [x] Add regression coverage proving no `x` or `y` coordinate properties are exposed by the constraint contract.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run layout-model tests.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- M21 introduced `LayoutIntentSnapshot`, `LayoutIntentItem`, and early relationship constraints in `kernel/layout-model`.
- M22 architecture requires a distinct Layout Constraint Model between layout intent and solved facts.
- The model must support optimization without making renderer-local geometry or raw adapter output authoritative.

### Guardrails

- Do not introduce ELK or optimizer behavior in this story.
- Do not add parser syntax or source mutation behavior in this story.
- Do not model raw `x/y` as authored constraints.
- Keep the model cohesive with existing `kernel/layout-model` contracts.

### Testing Requirements

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 2, Story 2.1]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-1, AD-2]
- [Source: `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test` failed first because the M22 layout constraint model types did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test` then passed after adding the constraint model contracts and tightening the coordinate-guard assertion.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added M22 layout constraint model contracts in `kernel/layout-model` for near, below, aligned-with, grouped-with, preferred-zone, preserve-order, and route-lane preference constraints.
- Added canonical constraint subjects carrying subject, occurrence, sheet, view, snapshot, and source-span identities where available.
- Added deterministic constraint snapshot canonicalization and regression coverage that keeps authored constraints relationship/zone/lane based rather than raw `x/y` fields.

### File List

- `_bmad-output/implementation-artifacts/m22/2-1-introduce-the-layout-constraint-model.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`

## Change Log

- 2026-07-18: Created M22 Story 2.1 with layout constraint model requirements.
- 2026-07-18: Added the M22 layout constraint model, deterministic snapshot contract, and layout-model test coverage.
