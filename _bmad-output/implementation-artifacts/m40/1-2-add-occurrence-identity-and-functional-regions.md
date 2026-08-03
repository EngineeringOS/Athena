---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.2: Add Occurrence Identity And Functional Regions

Status: review

## Story

As a compiler maintainer,
I want occurrences traceable to engineering subjects and authored functional regions,
so that projected content is provably derived from engineering truth.

## Acceptance Criteria

1. Every projected occurrence carries stable identity traceable to its engineering subject.
2. An occurrence without an engineering source, or a duplicate occurrence identity, fails with a
   named diagnostic.
3. A functional region groups exactly its declared occurrences by identity; an empty region or a
   region referencing a missing occurrence fails with a plain diagnostic.
4. A region has no placement, size, or style facts at the Projection level.
5. Authored empty-sheet enforcement completes here: a sheet whose regions carry no occurrences
   fails with a plain diagnostic (Story 1.1 deferral).

## Tasks / Subtasks

- [x] Add `region "Name" { occurrences [A, B] }` grammar inside view blocks.
- [x] Lower regions into projection regions with occurrence nodes traceable to engineering
  subjects.
- [x] Wire validation: missing source, duplicate occurrence, empty region, missing occurrence,
  empty sheet.
- [x] Add compiler + language tests.
- [x] Run focused tests, hygiene + encoding audits; update story + sprint status.

## Dev Notes

### Current Code Intelligence

- Story 1.1 delivered authored `view`/`sheet`/`grid`; `AuthoredProjectionViewCompiler` emits
  `ProjectionDocument` (no occurrences yet).
- `ProjectionNode` (ProjectionElements.kt) carries semantic id + node id; `ProjectionSheetSubject`
  already maps semantic ids to node ids.
- Committed syntax: `region "Power Distribution" { occurrences [Supply, Breaker] }` inside a view
  block. Rule (documented): a region belongs to the most recently declared sheet in the view
  block; regions must appear after their sheet.
- Engineering subjects resolve by component name (`component:<Name>` identity, M39 pattern).

### References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 1.2]
- [Source: PRD FR-3, FR-4; Decisions 2]
- [Source: ARCHITECTURE-SPINE.md AD-9, AD-12]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added region grammar/AST/IR/projection model + occurrence resolution.
- 2026-08-02: Language 76/76, compiler 403/403, LSP tests green.

### Completion Notes List

- Grammar: `region "Name" { occurrences [A, B] }` inside view blocks; empty occurrence list
  allowed so the empty-region rule is reachable; reused existing LBRACK/RBRACK tokens.
- Language model: `RegionDeclaration`; adapter wired; keywords added to `ident`.
- IR: `EngineeringProjectionRegion` bound to the most recently declared sheet (documented rule).
- Projection model: `ProjectionRegion` attached to `ProjectionSheet`.
- Compiler: `AuthoredProjectionViewCompiler` resolves occurrences to `component:<Name>`
  subjects, builds regions, and fails closed on missing source, duplicate occurrence, empty
  region, region-before-sheet, and empty sheet (Story 1.1 deferral completed).
- LSP: formatter + document symbols handle regions; facade allow-list extended.

### File List

- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/ViewDeclarationParserTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RegionOccurrenceCompilationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ViewAndSheetAuthorityCompilationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt
- extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 1.2 and PRD FR-3/FR-4 decisions.
- 2026-08-02: Implemented occurrence identity + functional regions with fail-closed validation;
  language 76/76, compiler 403/403, LSP green; marked review.
