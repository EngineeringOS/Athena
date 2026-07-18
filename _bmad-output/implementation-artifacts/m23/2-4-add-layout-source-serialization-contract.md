---
story_id: 2.4
story_key: 2-4-add-layout-source-serialization-contract
epic: 2
epic_title: Authored AST And Layout Intent Admission
title: Add layout source serialization contract
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 2.4: Add Layout Source Serialization Contract

## Story

As a Graph Workbench developer,
I want a serializer for layout intent source text,
So that frontend code does not hand-build final `.athena` syntax.

## Acceptance Criteria

**Given** a layout intent object for placement, alignment, or grouping
**When** the serializer runs
**Then** it emits accepted M23 system-scoped layout block syntax
**And** formatting is stable across repeated runs
**And** serializer tests cover all admitted statements

## Developer Context

Stories 2.2 and 2.3 introduced authored layout intent and priority. Story 2.4 adds the source text
serializer contract that future LSP/Graph Workbench stories consume. The serializer must not become
a semantic authority; it only renders already-approved authored intent into admitted `.athena`
layout block syntax.

## Architecture Guardrails

- Serializer output must parse through the real `AthenaLanguageParser`.
- Do not add frontend-owned syntax building.
- Do not emit unsupported priority syntax; M23 admitted syntax defaults to preference.
- Keep formatting deterministic and reviewable.

## Tasks/Subtasks

- [x] Add failing serializer tests covering place-near, place-below, align-with-axis, and group-with.
- [x] Add `AuthoredLayoutIntentSourceSerializer` with stable formatting.
- [x] Verify serializer output reparses through the real language parser.
- [x] Run compiler/language checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 2.4 from backlog after Story 2.3 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`; compile failed because `AuthoredLayoutIntentSourceSerializer` did not exist.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.

### Completion Notes

- Added backend-owned serializer for authored layout intent source text.
- Serializer covers place-near, place-below, align-with-axis, and group-with statements.
- Serializer output is stable across repeated runs and accepted by the real `AthenaLanguageParser`.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentSourceSerializer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentSourceSerializerTest.kt`

### Change Log

- 2026-07-18: Added stable authored layout intent source serializer and parser-backed tests.

## Status

Review. Story 2.4 is marked review in `sprint-status.yaml`.
