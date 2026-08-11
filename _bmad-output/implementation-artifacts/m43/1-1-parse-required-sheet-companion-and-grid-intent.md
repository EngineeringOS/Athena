---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 1.1: Parse Required Sheet Companion And Grid Intent

Status: done

## Story

As an engineer, I can author `project.sheet.athena` with `grid: C * R cell: N`,
so page coordinates live beside project meaning and missing companions fail clearly.

## Acceptance Criteria

1. Same-basename companion discovery is exact; missing or ambiguous companion yields explicit `UNAVAILABLE` presentation diagnostics without corrupting Engineering Reality.
2. `grid: C * R cell: N` accepts positive columns/rows and only positive multiples of four for `N`; typed source spans cover page, grid, placements, and companion source.
3. `cell: N` means `N` subdivisions per macro cell on both axes; no fixed 4x4 interpretation remains.
4. Page/profile intent and grid dimensions are typed; project grammar is unchanged.
5. Direct placement syntax such as `"Supply" at A2`, optional `micro(x,y)`, and optional `lock` parses without `place occurrence` ceremony.
6. Diagnostics name exact statement/occurrence, problem, and correction in human-first language.
7. Focused language/LSP tests, full Gradle test, encoding audit, and source-set hygiene audit pass.

## Tasks / Subtasks

- [x] Adopt and correct Sheet Companion model/parser (AC: 2, 3, 4, 5, 6)
  - [x] Make subdivision count derive from `cell: N`.
  - [x] Validate micro coordinates against `1..N` and preserve spans.
  - [x] Keep project grammar untouched and direct placement syntax canonical.
- [x] Enforce companion discovery contract (AC: 1, 6)
  - [x] Resolve exact same-basename companion only.
  - [x] Produce explicit missing/ambiguous presentation diagnostics.
- [x] Add focused tests and absence checks (AC: 1-7)
  - [x] Cover cell 4, cell 8, invalid cell 6, micro 8, out-of-range micro, direct quoted placement, spans, and diagnostics.
  - [x] Verify no fixed 4x4 parser/model wording remains.
- [x] Verify and record (AC: 7)
  - [x] Run focused tests, full Gradle, encoding, and source-set hygiene audits.
  - [x] Complete story records and set `review` only after evidence passes.

## Dev Notes

- Adopt `SheetCompanionLanguage.kt`, parser tests, LSP parsing/symbol support, and exact sibling loader where aligned.
- `cell: N` is subdivision count, not physical micro pitch. Story 1-2 owns exact anchor math; Story 1-3 owns Spatial compilation.
- Missing companion must fail Presentation Reality closed while non-presentation compilation remains usable.
- No compatibility syntax, fixed 4x4 fallback, or project-grammar placement declarations.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Initial language boundary test failed because new public locator types were not allow-listed; updated boundary contract intentionally.

### Completion Notes List

- `SheetGridIntent.microRows` and `microColumns` now derive from `cell: N`; fixed 4x4 interpretation removed.
- Micro anchor validation uses declared subdivision count; direct quoted placement syntax remains canonical.
- Added `SheetCompanionLocator` with exact same-basename found/missing/ambiguous outcomes; LSP unavailable diagnostics use it.
- Verification: language, compiler, and LSP focused tests passed; frontend 8/8; full Gradle 114 actionable tasks; encoding and hygiene audits passed.

### File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`

### Change Log

- 2026-08-06: Made Sheet Companion subdivision and micro validation dynamic; centralized exact companion discovery.
- 2026-08-06: Revalidated full product test surface and final M43 gates; status set to `done`.

### Change Log
