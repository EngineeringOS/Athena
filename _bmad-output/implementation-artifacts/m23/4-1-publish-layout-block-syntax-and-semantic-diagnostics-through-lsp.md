---
story_id: 4.1
story_key: 4-1-publish-layout-block-syntax-and-semantic-diagnostics-through-lsp
epic: 4
epic_title: LSP And Graph Workbench Round-Trip Closure
title: Publish layout-block syntax and semantic diagnostics through LSP
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 4.1: Publish Layout-Block Syntax And Semantic Diagnostics Through LSP

## Story

As an IDE user,
I want valid layout blocks accepted and invalid blocks diagnosed in Problems,
So that source truth and editor feedback agree.

## Acceptance Criteria

**Given** valid and invalid M23 layout sources
**When** the Athena LSP analyzes them
**Then** valid layout blocks produce no false syntax errors
**And** invalid relation, missing target, invalid axis, unknown subject, and unknown target cases produce useful diagnostics
**And** diagnostics carry source ranges for reveal

## Developer Context

Epic 1 made ANTLR and Tree-sitter accept the same M23 layout syntax. Epic 3 added compiler-owned
layout hint binding and diagnostics. This story closes the IDE Problems path by threading those
compiler diagnostics through the existing LSP project semantic pipeline.

## Architecture Guardrails

- Follow AD-7: compiler and LSP own meaning and diagnostics.
- Tree-sitter remains syntax UX only and must not bind layout subjects.
- Keep existing package semantic diagnostics and active-source behavior intact.

## Tasks/Subtasks

- [x] Add failing LSP tests proving valid layout blocks are accepted and invalid layout sources publish syntax/semantic diagnostics.
- [x] Expose compiler-owned layout hint binding through the compiler facade.
- [x] Run layout hint binding in the LSP project semantic pipeline.
- [x] Run LSP/compiler checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 4.1 from backlog after Story 3.4 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`; the new unknown-layout-reference LSP test failed because no `semantic.layout.reference.unknown` diagnostics were published.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`.
- 2026-07-18: Compiler facade verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.

### Completion Notes

- Added LSP coverage for valid layout blocks, invalid layout syntax, and unknown layout references.
- Exposed compiler-owned layout hint binding through `AthenaCompiler`.
- Wired layout hint binding into the LSP project semantic diagnostics pipeline after declaration indexing.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`

### Change Log

- 2026-07-18: Published M23 layout syntax and semantic diagnostics through the LSP Problems path.

## Status

Review. Story 4.1 is marked review in `sprint-status.yaml`.
