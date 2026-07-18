---
story_id: 3.2
story_key: 3-2-lower-layout-intent-into-constraint-snapshots
epic: 3
epic_title: Compiler Constraint Lowering And Deterministic Facts
title: Lower layout intent into constraint snapshots
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 3.2: Lower Layout Intent Into Constraint Snapshots

## Story

As a layout engineer,
I want admitted hints to become governed layout constraints,
So that the existing layout engine can consume source-owned relationships.

## Acceptance Criteria

**Given** bound layout intent for a system
**When** constraint lowering runs
**Then** `near`, `below`, `aligned-with`, and `grouped-with` constraints are emitted
**And** constraints carry view family, subject, target, priority, source span, and snapshot identity where available
**And** constraints are sorted deterministically

## Developer Context

Story 3.1 binds layout hint names against compiler-owned semantic declarations. Story 3.2 turns
those admitted and bindable hints into `LayoutConstraintSnapshot` values that remain source-owned,
deterministic, and renderer-independent.

## Architecture Guardrails

- Lower only through compiler-owned semantic declarations and layout model constraints.
- Preserve authored priority on constraints; do not drop it during lowering.
- Unknown references may be skipped here because Story 3.1 already emits diagnostics.
- Do not feed renderer/canvas state.

## Tasks/Subtasks

- [x] Add failing tests for constraint snapshot lowering from all admitted layout statements.
- [x] Add authored priority to layout constraints without changing renderer ownership.
- [x] Add compiler-owned constraint lowerer with deterministic IDs and ordering.
- [x] Run layout/compiler checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 3.2 from backlog after Story 3.1 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`; compile failed because `ProjectSemanticLayoutConstraintLowerer` and constraint authored priority were missing.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
- 2026-07-18: Layout model verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`.

### Completion Notes

- Added authored priority to `LayoutConstraint` while preserving default behavior.
- Added compiler-owned `ProjectSemanticLayoutConstraintLowerer`.
- Lowered known layout hints into deterministic `LayoutConstraintSnapshot` constraints with source span, view family, subject, target, priority, and snapshot identity.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutConstraintLowerer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutConstraintLowererTest.kt`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`

### Change Log

- 2026-07-18: Added governed layout constraint snapshot lowering from admitted layout hints.

## Status

Review. Story 3.2 is marked review in `sprint-status.yaml`.
