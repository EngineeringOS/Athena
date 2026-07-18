---
story_id: 2.3
story_key: 2-3-add-priority-model-compatibility
epic: 2
epic_title: Authored AST And Layout Intent Admission
title: Add priority model compatibility
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 2.3: Add Priority Model Compatibility

## Story

As a layout-model maintainer,
I want M23 authored priority to coexist with existing layout priorities,
So that future conflict handling has a stable model without breaking M21/M22 layout contracts.

## Acceptance Criteria

**Given** existing layout priority types
**When** M23 adds authored layout priority support
**Then** preference/default priority is represented without silently changing existing semantics
**And** hard/soft/preference mapping or a separate authored constraint priority type is documented
**And** tests cover deterministic priority ordering

## Developer Context

Story 2.2 introduced `AuthoredLayoutIntentPriority.PREFERENCE` as a source-owned default. Story 2.3
must make the authored priority model explicit enough for later conflict diagnostics while avoiding
changes to existing M21/M22 `LayoutPriority` behavior.

## Architecture Guardrails

- Keep authored priority separate from solved layout priority unless an explicit mapper is introduced.
- Existing `LayoutPriority` semantics must remain unchanged.
- Authored statements still default to preference priority.
- No parser syntax for `hard` or `soft` is required in M23 unless a later story explicitly admits it.

## Tasks/Subtasks

- [x] Add failing tests for authored hard/soft/preference priority ordering and default preference.
- [x] Extend/document authored priority compatibility without changing existing `LayoutPriority` semantics.
- [x] Keep Story 2.2 mapper defaulting admitted syntax to preference priority.
- [x] Run layout/compiler checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 2.3 from backlog after Story 2.2 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`; compile failed because authored `HARD`/`SOFT` priorities and sort rank did not exist.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`.
- 2026-07-18: Compiler integration check passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.

### Completion Notes

- Added separate authored priority values `HARD`, `SOFT`, and `PREFERENCE` with deterministic sort ranks.
- Preserved existing `LayoutPriority` values and mapper default behavior.
- Documented authored priority compatibility in `kernel/layout-model/README.md`.

### File List

- `kernel/layout-model/README.md`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`

### Change Log

- 2026-07-18: Added authored layout priority compatibility model and documentation.

## Status

Review. Story 2.3 is marked review in `sprint-status.yaml`.
