---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.1: Establish Projection View And Sheet Authority

Status: review

## Story

As a compiler maintainer,
I want Projection to own views and sheets through authored `view` blocks,
so that engineering views have one authority before Spatial touches them.

## Acceptance Criteria

1. A `view` declaration creates exactly one view identity with one projection compiler authority.
2. A sheet belongs to exactly one view, carries a stable identity, and groups its declared
   occurrences (occurrences land in Story 1.2; the model rule exists now).
3. A sheet exposes a grid reference system (rows, columns, cell references such as A1/B3) as
   Projection structure; grid facts carry no coordinates.
4. A view with no sheets and a sheet with no occurrences (directly or through a region or
   construct) each fail with a plain diagnostic naming the subject and problem.
5. No projection fact is created or modified by Spatial, Presentation, or the renderer.

## Tasks / Subtasks

- [x] Add authored `view` block grammar (VIEW ident { SHEET ident ... }) and sheet grid
  declaration, per committed M40 syntax (PRD Decision 2).
- [x] Lower authored views/sheets into ProjectionDocument (existing model, extend sheet with grid
  reference system).
- [x] Wire validation: view with no sheets, duplicate sheet identity, sheet view membership;
  empty-sheet model rule verified (ProjectionReality.validate), authored enforcement lands with
  occurrences in Story 1.2.
- [x] Add compiler tests: authored view compiles to one view + sheets + grid; failure diagnostics.
- [x] Run focused tests, hygiene audit, encoding audit; update story + sprint status.

## Dev Notes

### Current Code Intelligence

- Projection model already owns view/sheet identity and validation:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`
    (view: ViewDefinition, sheets: List<ProjectionSheet>)
  - `ProjectionSheets.kt` (ProjectionSheet with viewId membership, subjects)
  - `ProjectionReality.kt` (validate: missing view identity, empty sheets, duplicate sheet
    identity, empty sheet, sheet view membership)
- M39 authored projection via `projectionPolicyDecl` (`PROJECTION ident { ... }`) and
  `layoutDecl` (`LAYOUT ... { PLACE ... }`) in `kernel/language/src/main/antlr/.../Athena.g4`.
  M40 commits `view` blocks as the sole authoring surface (PRD Decision 8); policy surface
  retirement lands with Story 1.3/FR-6.
- Existing compiler path: `EngineeringToProjectionTransformation.kt`, `ProjectionModelDeriver.kt`,
  `AthenaProjectionPolicyCompiler.kt` (policy surface to retire later).

### Implementation Guidance

- Grammar: add `viewDecl : VIEW ident LBRACE viewMember* RBRACE` with
  `viewMember : sheetDecl | gridDecl`. Sheet declaration: `SHEET ident`. Grid declaration:
  rows/columns/cell-reference vocabulary (e.g., `GRID ident LBRACE ROWS INT COLUMNS INT RBRACE`).
- Model: add `ProjectionSheetGrid(rows, columns, cellReferences)` to sheet (or `ProjectionGrid`)
  carrying no coordinates.
- Lowering: authored `view` produces `ViewDefinition` + `ProjectionSheet` entries; sheet order
  follows declaration order.
- Diagnostics: name subject + problem + correction in plain engineering language (NFR-9).
- Tests: `kernel/compiler/src/test/kotlin/.../ViewAndSheetAuthorityCompilationTest.kt`.

### References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 1.1]
- [Source: PRD FR-1, FR-2, FR-6; Decisions 2, 8]
- [Source: ARCHITECTURE-SPINE.md AD-9, AD-19]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Implemented authored `view` grammar, AST, lowering, grid model, compiler wiring.
- 2026-08-02: Language 75/75, compiler 397/397, LSP tests green.

### Completion Notes List

- Grammar: `viewDecl`, `sheetDecl`, `gridDecl` with VIEW/SHEET/GRID/ROWS/COLUMNS keywords added to
  `ident` so existing hyphenated identifiers (e.g., `schematic-sheet`) keep parsing.
- Language model: `ViewDeclaration`, `SheetDeclaration`, `GridDeclaration`; parse adapter wired.
- IR: `EngineeringProjectionView/Sheet/Grid`; lowered from AST in `EngineeringIrLowerer`.
- Projection model: `ProjectionSheetGrid` with deterministic cell references (A1..D3) attached to
  `ProjectionSheet`; no coordinates.
- Compiler: `AuthoredProjectionViewCompiler` emits `ProjectionDocument` per authored view with
  plain diagnostics for no-sheets and duplicate sheet; wired into `CompilerCompilationSuccess`
  (`authoredProjectionViews`, `authoredProjectionDiagnostics`).
- LSP: formatter and document symbols handle view/sheet/grid; facade allow-list extended.
- Empty-sheet model rule verified via `ProjectionReality.validate`; authored-path enforcement
  completes in Story 1.2 when occurrences exist.

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
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ViewAndSheetAuthorityCompilationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt
- extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 1.1 and PRD FR-1/FR-2/FR-6 decisions.
- 2026-08-02: Implemented authored view/sheet/grid authority with tests; language 75/75,
  compiler 397/397, LSP green; marked review.
