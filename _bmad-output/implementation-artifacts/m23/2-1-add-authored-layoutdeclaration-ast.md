---
story_id: 2.1
story_key: 2-1-add-authored-layoutdeclaration-ast
epic: 2
epic_title: Authored AST And Layout Intent Admission
title: Add authored `LayoutDeclaration` AST
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 2.1: Add Authored `LayoutDeclaration` AST

## Story

As a compiler engineer,
I want a first-class authored AST node for layout declarations,
So that generated parser types stay internal to the parser adapter.

## Acceptance Criteria

**Given** an admitted layout block parse tree
**When** parser adaptation builds authored syntax
**Then** it emits `LayoutDeclaration` with view family, statements, and source span
**And** statements represent place-near, place-below, align-with-axis, and group-with
**And** declaration consumers handle the new variant explicitly

## Developer Context

Stories 1.1-1.4 admitted parser syntax only. Before this story, the public `AthenaLanguageParser`
still fails on layout blocks because the internal ANTLR adapter does not map `layoutDecl` into
Athena-owned AST. This story adds syntax-only public model support; semantic lowering remains later.

## Architecture Guardrails

- Follow `AD-3`: downstream code consumes Athena-owned `LayoutDeclaration`, not generated ANTLR contexts.
- Follow `AD-4`: AST remains syntax-only. Do not bind subjects or lower constraints in this story.
- Handle every exhaustive `Declaration` consumer explicitly. Ignoring layout declarations is acceptable only where the consumer is semantic-device/port/connection specific and documents that layout is deferred.

## Tasks/Subtasks

- [x] Add failing public parser test for `LayoutDeclaration` and layout statement variants.
- [x] Add syntax-only `LayoutDeclaration` / `LayoutStatement` public model types.
- [x] Adapt ANTLR `layoutDecl` into authored AST with source spans.
- [x] Update public facade allow-list and declaration consumers for the new sealed variant.
- [x] Run language tests and relevant compile checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Started Story 2.1 from `backlog`; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`; compile failed because `LayoutDeclaration`, `LayoutStatement`, and `LayoutAxis` did not exist.
- 2026-07-18: Green verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`.
- 2026-07-18: Downstream explicit-consumer verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` and `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`.

### Completion Notes

- Added public syntax-only `LayoutDeclaration`, `LayoutStatement`, and `LayoutAxis` model types.
- Adapted ANTLR `layoutDecl` parse trees into Athena-owned authored AST with source spans.
- Updated public facade allow-list and explicit sealed `Declaration` consumers, keeping semantic lowering deferred.

### File List

- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`

### Change Log

- 2026-07-18: Added authored layout declaration AST, adapter mapping, facade allow-list updates, and explicit deferred handling in compiler/LSP declaration consumers.

## Status

Review. Story 2.1 is marked review in `sprint-status.yaml`.
