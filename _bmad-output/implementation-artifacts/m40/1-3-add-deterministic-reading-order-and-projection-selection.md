---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.3: Add Deterministic Reading Order And Projection Selection

Status: review

## Story

As an engineer,
I want deterministic reading order and projection selection,
so that the same source always produces the same view.

## Acceptance Criteria

1. Reading order is a permutation of the declared sheets; within a sheet, occurrences follow
   declaration order inside regions, then region declaration order.
2. A duplicate or unknown sheet entry in `reading-order` fails with a plain diagnostic.
3. The same source compiles to the same reading order and selection on every run.
4. View declarations are the sole M40 authoring surface for view selection; no competing
   selection syntax coexists (existing projection-policy surface for M40 source is retired or
   rewritten, not shimmed).

## Tasks / Subtasks

- [x] Add `reading-order [S1, S2]` grammar inside view blocks.
- [x] Lower reading order into projection sheets; validate permutation (duplicate/unknown fail).
- [x] Enforce sole selection surface: `view` + `projection` policy in one M40 source fails with a
  plain diagnostic.
- [x] Add determinism + permutation + surface tests.
- [x] Run focused tests, hygiene + encoding audits; update story + sprint status.

## Dev Notes

### Current Code Intelligence

- Stories 1.1/1.2 delivered authored views with sheets, grid, regions, occurrences, and
  fail-closed validation in `AuthoredProjectionViewCompiler`.
- `ProjectionSheet.order` currently equals declaration index; reading order will reassign it.
- M39 projection policy surface: `AthenaProjectionPolicyCompiler` + `projection` blocks; Decision
  8 retires it for M40 source (not shimmed). M39/M34 examples keep their own syntax.

### References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 1.3]
- [Source: PRD FR-5, FR-6; Decisions 2, 8]
- [Source: ARCHITECTURE-SPINE.md AD-9, AD-19]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added reading-order grammar/AST/IR + permutation and sole-surface validation.
- 2026-08-02: Language 76/76, compiler 408/408, LSP tests green.

### Completion Notes List

- Grammar: `reading-order [S1, S2]` view member; token added to `ident`.
- Language model: `ViewDeclaration.readingOrder`; adapter wired.
- IR: `EngineeringProjectionView.readingOrder`; lowered from AST.
- Compiler: `AuthoredProjectionViewCompiler` validates reading order is a permutation of declared
  sheets (unknown/duplicate/non-permutation fail closed), reassigns sheet order, and rejects
  M40 source that mixes `view` declarations with retired `projection` policy declarations
  (Decision 8, sole selection surface). Determinism covered by double-compile test.
- LSP: formatter emits reading-order for round-trip.

### File List

- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/ViewDeclarationParserTest.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ReadingOrderProjectionSelectionTest.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 1.3 and PRD FR-5/FR-6 decisions.
- 2026-08-02: Implemented deterministic reading order + sole projection selection surface;
  language 76/76, compiler 408/408, LSP green; marked review.
