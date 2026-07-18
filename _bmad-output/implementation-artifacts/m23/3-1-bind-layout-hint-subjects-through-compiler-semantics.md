---
story_id: 3.1
story_key: 3-1-bind-layout-hint-subjects-through-compiler-semantics
epic: 3
epic_title: Compiler Constraint Lowering And Deterministic Facts
title: Bind layout hint subjects through compiler semantics
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 3.1: Bind Layout Hint Subjects Through Compiler Semantics

## Story

As a compiler user,
I want layout hint subjects and targets resolved by the compiler,
So that unknown layout references are reported instead of ignored.

## Acceptance Criteria

**Given** layout intent references authored subject names
**When** semantic binding runs
**Then** known subjects bind to canonical identities where available
**And** unknown subjects and targets produce semantic diagnostics with source spans
**And** Tree-sitter or Theia code does not perform semantic binding

## Developer Context

Epic 2 admitted layout syntax and source-owned authored intent. This story starts Epic 3 by binding
layout hint names against compiler-owned semantic declarations. The binding result should use
existing project semantic declaration/binding concepts where possible and must not move semantic
resolution into frontend code.

## Architecture Guardrails

- Bind against `ProjectSemanticDeclaration` records produced by `ProjectSemanticDeclarationIndexer`.
- Do not use Tree-sitter, Theia, canvas, or renderer state.
- Unknown references must become `ProjectSemanticDiagnostic` entries with the authored source span.
- Constraint lowering remains Story 3.2.

## Tasks/Subtasks

- [x] Add failing compiler semantic tests for known and unknown layout hint references.
- [x] Add compiler-owned layout reference binder that emits `ProjectSemanticBinding` entries.
- [x] Emit deterministic diagnostics for unknown subjects and targets.
- [x] Preserve existing declaration indexing behavior.
- [x] Run compiler checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 3.1 from backlog after Epic 2 stories reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`; compile failed because `ProjectSemanticLayoutHintBinder` did not exist.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.

### Completion Notes

- Added compiler-owned `ProjectSemanticLayoutHintBinder`.
- Bound known single-name layout references against same-namespace device declarations.
- Emitted deterministic `semantic.layout.reference.unknown` diagnostics for unknown layout subject/target names.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinder.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinderTest.kt`

### Change Log

- 2026-07-18: Added compiler-owned layout hint reference binding and unknown-reference diagnostics.

## Status

Review. Story 3.1 is marked review in `sprint-status.yaml`.
