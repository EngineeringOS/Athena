# Story 1.1: Parse Sheet Companion Grid And Page Intent

Status: review

## Story

As an engineer,
I want to author a colocated `*.sheet.athena` file with page, grid, and title intent,
so that presentation choices live beside but outside Engineering Reality.

## Acceptance Criteria

1. Sheet Companion source is recognized as a distinct source unit and does not alter project-source
   grammar or Engineering Reality declarations.
2. Parser accepts page format/orientation, row labels, column count, title intent, and one scalar
   `cell: K` setting.
3. `K` accepts only positive integer multiples of 4; malformed, zero, negative, or non-divisible
   values produce deterministic typed diagnostics with source spans.
4. Parsed page/grid/title values are exposed through Athena-owned language model types; generated
   ANTLR types do not leak to compiler consumers.
5. No exact x/y, SVG, Canvas, route, or engineering relationship field is admitted by this story.
6. Focused language and parser tests pass sequentially.

## Tasks / Subtasks

- [x] Task 1: Extend public language model for Sheet Companion page/grid/title intent (AC: 1-4)
  - [x] Add small related value types in the existing language model organization.
  - [x] Preserve source spans and human-first diagnostics.
- [x] Task 2: Add isolated `*.sheet.athena` parser surface (AC: 1-3, 5)
  - [x] Admit page, grid, title, and `cell: K` without changing project grammar.
  - [x] Map parser output to Athena-owned contracts.
- [x] Task 3: Author focused tests (AC: 2-4, 6)
  - [x] Add valid page/grid/title and `cell: 4`/`cell: 8` fixtures.
  - [x] Add invalid `cell: 6` and missing/unknown statement cases.
  - [x] Assert source spans and stable diagnostics.
- [x] Task 4: Run affected language tests and record results.

## Dev Notes

- M43 authority: `*.sheet.athena` is presentation intent only. Do not add compatibility readers or
  move the old system-scoped layout authority into a second model.
- Existing parser boundary: `AthenaLanguageParser` and `AthenaAntlrParseAdapter` own conversion from
  generated parser types to `com.engineeringood.athena.language` contracts.
- Existing layout grammar and models are M41/M40 inputs; inspect them before editing and reuse stable
  source-span and diagnostic conventions. Do not create duplicate parser facades.
- Keep small strongly-related Kotlin value types together; avoid one-file-per-tiny-type.
- This story does not compile placement into Projection or Spatial; later stories consume this model.

### Project Structure Notes

- Grammar: `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- Public language model: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/`
- Parser adapter: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/`
- Tests: `kernel/language/src/test/kotlin/com/engineeringood/athena/language/`
- No production proof/demo/sample classes.

### References

- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md#4.1-sheet-companion-authoring]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#AD-1---companion-file-has-presentation-authority]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#AD-2---layout-cell-uses-one-four-divisible-scalar]
- [Source: kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `:kernel:language:test` initially exposed the expected public facade allow-list drift; updated the
  boundary test for the new Athena-owned Sheet types, then reran successfully.

### Completion Notes List

- Added `AthenaSheetCompanionParser` with typed page/grid/title contracts.
- Fixed grid contract to one scalar `cell: K`, positive multiple of 4, implicit 4 x 4 micro lattice.
- Kept project DSL and existing ANTLR parser path unchanged.
- Verification: `:kernel:language:test` passed.

### File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
