# Story 2.1: Compile Sheet Intent Into Projection Occurrences

Status: review

## Story

As a compiler,
I want to associate Sheet Companion placement constraints with Projection occurrences,
so that source-authored anchors influence layout without becoming engineering identities.

## Acceptance Criteria

1. A typed compiler mapper accepts `SheetCompanionSource` and a Projection sheet occurrence index.
2. Known placement references resolve to canonical occurrence identities without changing Entity,
   Function, Port, Relationship, or Projection occurrence identity.
3. Authored cell, optional micro anchor, and lock state become projection-owned placement constraints
   with source provenance; exact geometry is not produced by the mapper.
4. Unknown occurrence references produce deterministic diagnostics naming the Sheet Companion subject.
5. Reordering source files or placement statements does not change occurrence identity or canonical
   output ordering.
6. Focused compiler/layout tests pass, followed by the affected Gradle module test.

## Tasks / Subtasks

- [x] Task 1: Add a typed Projection placement-constraint contract for cell/micro/lock intent (AC: 2-3)
  - [x] Keep it in the existing projection/layout model boundary.
  - [x] Carry source provenance and stable occurrence identity.
- [x] Task 2: Implement deterministic Sheet-to-Projection mapper (AC: 1, 4-5)
  - [x] Resolve by existing occurrence identity/label index.
  - [x] Reject unknown references without fallback or renderer inference.
- [x] Task 3: Add unit tests for identity stability, provenance, and diagnostics (AC: 2-6).
- [x] Task 4: Run affected Gradle tests sequentially.

## Dev Notes

- Reuse `ProjectionSheet`, `ProjectionSheetSubject`, existing occurrence identity recipes, and
  `LayoutSourceSpan`; do not create a second Projection document or a renderer-facing DTO.
- `cell: K` semantics remain Sheet Companion-owned. Mapping may carry K and micro indices, but exact
  page pixels and routes remain Spatial-owned.
- Existing `ProjectionPlacementPlanner` is the source of deterministic placement groups. Extend or
  compose it only where the new contract belongs; do not bypass Projection Reality.
- No Pattern/Macro instantiation belongs here.

### Project Structure Notes

- Projection contracts: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/`
- Layout contracts: `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/`
- Compiler mapping: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
- Tests beside owning module.

### References

- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md#FR-5-apply-authored-anchors-and-locks]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#AD-4---persist-constraints-derive-geometry]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionPlacementPlanner.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `:kernel:compiler:test` passed after correcting the fixture to current `ProjectionDocument`.

### Completion Notes List

- Added `SheetPlacementConstraint`, `MicroGridPosition`, and deterministic Sheet-to-Projection mapping.
- Preserved Projection occurrence identity and source spans; unknown references fail closed.
- Mapper emits placement intent only; no geometry or renderer state introduced.

### File List

- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SheetCompanionProjectionPlacementMapper.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SheetCompanionProjectionPlacementMapperTest.kt`

### Change Log

- 2026-08-05: Implemented Sheet Companion placement mapping and verified compiler tests.
