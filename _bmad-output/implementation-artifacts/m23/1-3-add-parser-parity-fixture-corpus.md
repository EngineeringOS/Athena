---
story_id: 1.3
story_key: 1-3-add-parser-parity-fixture-corpus
epic: 1
epic_title: Parser Parity And Source Fixtures
title: Add parser parity fixture corpus
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 1.3: Add parser parity fixture corpus

## Story

As a reviewer,
I want paired ANTLR and Tree-sitter fixtures for layout syntax,
So that future syntax changes cannot silently drift between backend and IDE parser paths.

## Acceptance Criteria

**Given** the M23 fixture corpus
**When** parser parity tests run
**Then** valid fixtures cover the full admitted statement vocabulary
**And** invalid fixtures cover malformed `place`, invalid axis, missing target, and rejected file-global layout
**And** fixture names and expected outcomes are shared or cross-referenced between parser stacks

## Developer Context

Stories 1.1 and 1.2 admitted syntax separately. This story creates the shared corpus that both
parser stacks use as an ongoing parity guardrail. This is still syntax admission only; authored AST
and compiler semantic lowering begin in Epic 2 and Epic 3.

## Architecture Guardrails

- Follow `AD-2`: ANTLR and Tree-sitter must reference the same M23 fixture inventory.
- Follow `AD-7`: fixtures prove syntax acceptance/recovery only; compiler/LSP semantics stay later.
- Follow `AD-8`: fixture tests must not weaken existing M0-M22 compatibility coverage.

## Tasks/Subtasks

- [x] Add failing ANTLR and Tree-sitter tests that require a checked-in M23 parser-parity fixture inventory.
- [x] Add valid and invalid M23 parser-parity fixture files under `examples/m23/parser-parity-proof`.
- [x] Verify ANTLR grammar-level tests and Tree-sitter package tests against the same inventory.
- [x] Update Dev Agent Record, File List, Change Log, and story status after verification.

## Dev Agent Record

### Debug Log

- 2026-07-18: Started Story 1.3 from `backlog`; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red phase verified with `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`; failed with `NoSuchFileException` for missing `examples/m23/parser-parity-proof`.
- 2026-07-18: Added shared M23 valid/invalid parser-parity fixtures.
- 2026-07-18: `.\gradlew.bat --no-daemon --console=plain :kernel:language:test` passed.
- 2026-07-18: `yarn --cwd ide/tree-sitter-athena test` passed and reported 40 node tests passing.

### Completion Notes

- Added shared checked-in M23 parser-parity fixtures covering valid layout syntax and invalid file-global, bad axis, malformed place, and missing target cases.
- Added ANTLR grammar smoke coverage for the shared corpus inventory and valid/invalid outcomes.
- Added Tree-sitter web parser coverage for the same corpus inventory and valid/invalid outcomes.

### File List

- `_bmad-output/implementation-artifacts/m23/1-3-add-parser-parity-fixture-corpus.md`
- `_bmad-output/implementation-artifacts/m23/sprint-status.yaml`
- `examples/m23/parser-parity-proof/valid-layout-block.athena`
- `examples/m23/parser-parity-proof/invalid-file-global-layout.athena`
- `examples/m23/parser-parity-proof/invalid-layout-bad-axis.athena`
- `examples/m23/parser-parity-proof/invalid-layout-malformed-place.athena`
- `examples/m23/parser-parity-proof/invalid-layout-missing-target.athena`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`

### Change Log

- 2026-07-18: Added shared M23 parser-parity fixture corpus and ANTLR/Tree-sitter inventory tests.

## Status

Review. Story 1.3 is ready for code review in `sprint-status.yaml`.
