---
story_id: 3.3
story_key: 3-3-diagnose-duplicate-and-contradictory-hints
epic: 3
epic_title: Compiler Constraint Lowering And Deterministic Facts
title: Diagnose duplicate and contradictory hints
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 3.3: Diagnose Duplicate And Contradictory Hints

## Story

As an engineer,
I want layout hint conflicts reported predictably,
So that source intent stays reviewable and does not produce mysterious layouts.

## Acceptance Criteria

**Given** duplicate or contradictory hints in a layout block
**When** compiler diagnostics run
**Then** duplicates are either de-duplicated with evidence or reported consistently
**And** contradictory hints include relation, subject, target, and priority in the diagnostic
**And** diagnostics do not crash layout fact generation

## Developer Context

Story 3.1 introduced unknown-reference diagnostics. Story 3.3 extends compiler-owned layout
diagnostics to catch duplicate and contradictory hints before later layout fact generation.

## Architecture Guardrails

- Diagnostics remain compiler-owned.
- Do not ask the renderer or frontend to resolve conflicts.
- Constraint lowering should tolerate diagnosed conflicts and remain deterministic.

## Tasks/Subtasks

- [x] Add failing compiler tests for duplicate and contradictory layout hints.
- [x] Emit deterministic duplicate-hint diagnostics.
- [x] Emit deterministic contradictory-hint diagnostics including relation, subject, target, and priority.
- [x] Verify constraint lowering still runs with diagnosed conflicts.
- [x] Run compiler checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 3.3 from backlog after Story 3.2 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`; the new duplicate/contradictory diagnostic test failed because only unknown-reference diagnostics existed.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.

### Completion Notes

- Added duplicate layout hint diagnostics.
- Added contradictory layout hint diagnostics for direct placement conflicts and alignment-axis conflicts.
- Preserved valid combinations of placement, alignment, and grouping hints.
- Verified constraint lowering remains deterministic with diagnosed conflicts.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinder.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinderTest.kt`

### Change Log

- 2026-07-18: Added compiler-owned duplicate and contradictory layout hint diagnostics.

## Status

Review. Story 3.3 is marked review in `sprint-status.yaml`.
