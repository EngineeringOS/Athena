---
story_id: 2.2
story_key: 2-2-map-layout-ast-into-layout-intent
epic: 2
epic_title: Authored AST And Layout Intent Admission
title: Map layout AST into layout intent
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 2.2: Map Layout AST Into Layout Intent

## Story

As an architect,
I want layout syntax to lower into domain-neutral intent before constraints,
So that syntax does not leak directly into solver behavior.

## Acceptance Criteria

**Given** a `LayoutDeclaration`
**When** compiler admission runs
**Then** it produces layout intent objects with subject, relation, target, optional axis, source span, and priority
**And** authored statements default to preference priority
**And** AST remains syntax-only with no semantic subject resolution

## Developer Context

Story 2.1 added syntax-only public AST support for `LayoutDeclaration`. Story 2.2 introduces the
next boundary: an Athena-owned layout intent model derived from authored AST but still not bound to
canonical semantic identities. Compiler constraint lowering and diagnostics remain later Epic 3 work.

## Architecture Guardrails

- Follow `AD-4`: `LayoutDeclaration` lowers through layout intent before constraints.
- Follow `AD-5`: authored statements default to preference priority.
- Do not resolve subjects or targets in this story.
- Do not feed layout facts or renderer behavior in this story.
- Keep generated parser types internal; this story consumes only public AST.

## Tasks/Subtasks

- [x] Add failing tests for layout AST to domain-neutral layout intent mapping.
- [x] Add model-owned layout intent relation, priority, and statement types.
- [x] Add a mapper from `LayoutDeclaration` to layout intent without semantic resolution.
- [x] Cover all admitted statements and default preference priority.
- [x] Run language/layout/compiler checks as relevant; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 2.2 from backlog after Story 2.1 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`; compile failed because authored layout intent model and mapper did not exist.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
- 2026-07-18: Model verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`.

### Completion Notes

- Added authored layout intent relation, axis, priority, block, and statement model types in `layout-model`.
- Added `AuthoredLayoutIntentMapper` to convert `LayoutDeclaration` into source-owned intent without semantic subject resolution.
- Covered all admitted M23 statements and default preference priority.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentMapper.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentMapperTest.kt`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`

### Change Log

- 2026-07-18: Added source-owned authored layout intent model and compiler mapper from layout AST.

## Status

Review. Story 2.2 is marked review in `sprint-status.yaml`.
