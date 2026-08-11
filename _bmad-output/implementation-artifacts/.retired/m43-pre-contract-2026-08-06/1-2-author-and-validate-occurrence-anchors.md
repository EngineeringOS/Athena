# Story 1.2: Author And Validate Occurrence Anchors

Status: review

## Story

As an engineer,
I want to author a known occurrence at `C3 micro(2,1)` and lock it,
so that important items retain human-authored presentation intent across recompilation.

## Acceptance Criteria

1. Sheet Companion accepts zero or more occurrence placement statements after page/grid/title intent.
2. Each statement names an occurrence, a cell reference with column number and row label, optional
   `micro(x,y)` with both coordinates in `1..4`, and optional `lock`.
3. Unknown occurrence, malformed cell, out-of-range row/column, and invalid micro coordinate produce
   deterministic source-linked diagnostics.
4. Parsed placement stores only occurrence reference, cell reference, micro anchor, lock state, and
   source span; no exact x/y, route, SVG, Canvas, or engineering relationship field is admitted.
5. Duplicate placement for one occurrence is rejected unless the source explicitly targets different
   sheets in a later story; this story has one Sheet Companion document.
6. Focused parser/model tests pass.

## Tasks / Subtasks

- [x] Task 1: Extend Sheet Companion model with placement constraint value types (AC: 1-4)
  - [x] Add validated cell and micro-anchor types.
  - [x] Add optional lock state and source provenance.
- [x] Task 2: Extend isolated Sheet Companion parser (AC: 1-5)
  - [x] Parse occurrence statements without changing project DSL grammar.
  - [x] Validate references, duplicate placements, and coordinate bounds.
- [x] Task 3: Add focused tests (AC: 2-6)
  - [x] Cover valid placement, optional micro, lock, duplicate, unknown, and out-of-range cases.
  - [x] Assert deterministic diagnostic order and source spans.
- [x] Task 4: Run `:kernel:language:test` sequentially.

## Dev Notes

- Build directly on `AthenaSheetCompanionParser` and `SheetCompanionSource` from Story 1.1. Do not
  introduce another parser or duplicate source-span type.
- Placement is presentation intent. Resolution to Projection occurrences belongs to Story 2.1.
- Keep `cell` scalar semantics from M43 architecture: `cell: K`, K positive multiple of 4, implicit
  4 x 4 micro lattice. Do not expose a configurable micro-grid declaration.
- Preserve public package allow-list coverage in `LanguageFacadeBoundaryTest`.

### Project Structure Notes

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`

### References

- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md#FR-3-author-occurrence-placement-constraints]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#AD-3---coordinates-identify-presentation-occurrences-only]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `:kernel:language:test` passed after correcting public facade allow-list coverage for new placement
  types.

### Completion Notes List

- Added validated `SheetCellReference`, `SheetMicroAnchor`, and `SheetPlacementIntent` contracts.
- Added optional micro and lock parsing with duplicate and grid-bound validation.
- Verification: `:kernel:language:test` passed.

### File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
