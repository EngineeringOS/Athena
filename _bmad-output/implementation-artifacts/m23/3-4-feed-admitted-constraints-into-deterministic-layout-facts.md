---
story_id: 3.4
story_key: 3-4-feed-admitted-constraints-into-deterministic-layout-facts
epic: 3
epic_title: Compiler Constraint Lowering And Deterministic Facts
title: Feed admitted constraints into deterministic layout facts
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 3.4: Feed Admitted Constraints Into Deterministic Layout Facts

## Story

As a reviewer,
I want admitted layout hints to influence layout facts,
So that M23 proves source-owned layout intent reaches the renderer contract.

## Acceptance Criteria

**Given** a sample project with admitted layout hints
**When** projection/layout generation runs
**Then** the emitted layout facts reflect the admitted constraints where the current engine supports them
**And** repeated runs on the same input produce identical facts
**And** renderer code remains paint-only

## Developer Context

Story 3.2 lowered layout hints into `LayoutConstraintSnapshot`. The existing `layout-engine`
already consumes constraint snapshots and emits Athena-owned schematic layout facts. This story
connects the compiler-owned M23 constraint path to that engine without adding renderer authority.

## Architecture Guardrails

- Use the existing `kernel:layout-engine` optimizer boundary.
- Renderer/canvas code must not be touched.
- Repeated runs on the same governed semantic snapshot must produce identical facts.

## Tasks/Subtasks

- [x] Add failing compiler tests proving M23 constraints feed deterministic schematic layout facts.
- [x] Add compiler bridge from bound semantic snapshot to layout engine optimization result.
- [x] Preserve paint-only renderer boundary.
- [x] Run compiler/layout-engine checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 3.4 from backlog after Story 3.3 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`; compile failed because `ProjectSemanticSchematicLayoutFactDeriver` did not exist.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
- 2026-07-18: Layout-engine verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`.

### Completion Notes

- Added compiler bridge from M23 semantic layout constraints to the existing rule-based schematic layout optimizer.
- Verified repeated derivation produces identical Athena-owned layout facts.
- Preserved renderer/canvas code unchanged for this story.

### File List

- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriverTest.kt`

### Change Log

- 2026-07-18: Added deterministic layout fact derivation from admitted M23 constraints.

## Status

Review. Story 3.4 is marked review in `sprint-status.yaml`.
