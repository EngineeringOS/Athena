---
baseline_commit: 9ff8ecb
---

# Story 1.5: Add Syntax Proof Fixtures

Status: done

## Story

As a maintainer,
I want executable examples for valid and invalid M18 syntax,
so that syntax behavior is proven before semantic graph work builds on it.

## Acceptance Criteria

1. `examples/m18/syntax-proof/` contains checked-in valid package-only, package-plus-import, alias, wildcard, visibility, and missing-target source fixtures.
2. Compiler parser tests prove valid fixtures produce the expected Athena-owned package/import AST and invalid fixtures produce deterministic typed positioned diagnostics.
3. Tree-sitter tests prove valid fixtures have no error nodes, alias/wildcard/visibility fixtures retain error nodes, and missing-target recovery retains explicit incomplete-header plus system structure.
4. The fixture inventory and expectation metadata are deterministic and documented as the syntax-only first slice of the accumulated M18 proof corpus.
5. Existing M17 and M18 parser/Tree-sitter tests remain compatible.
6. This story performs no repository lookup, semantic graph, import resolution, linking, lowering, LSP semantics, frontend resolution, canvas, remote registry, marketplace, or publish work.

## Tasks / Subtasks

- [x] Add proof harness tests first (AC: 1-5)
  - [x] Freeze source and invalid-expectation inventory under `examples/m18/syntax-proof/`.
  - [x] Add compiler parser coverage for valid AST values and invalid deterministic diagnostics.
  - [x] Add Tree-sitter WASM coverage for valid, invalid, and incomplete-header fixture behavior.
- [x] Add documented syntax fixtures (AC: 1, 4, 6)
  - [x] Add valid package-only and package/import examples.
  - [x] Add invalid alias, wildcard, visibility, and missing-target examples with expectation metadata.
  - [x] Document syntax-only authority and later M18 corpus accumulation boundaries.
- [x] Run scoped verification sequentially
  - [x] Run `:kernel:language:test` without concurrent Gradle invocations.
  - [x] Run `yarn --cwd ide/tree-sitter-athena test` against the checked-in WASM.
  - [x] Run the encoding audit after text edits.

### Review Findings

- [x] [Review][Patch] Validate exact expectation key sets, syntax-failure status, duplicates, and nonblank message fragments [kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxProofTest.kt]
- [x] [Review][Patch] Assert source-bounded diagnostic spans and determinism across fresh parser instances [kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxProofTest.kt]
- [x] [Review][Patch] Assert valid Tree-sitter declaration/target structure and invalid ERROR ranges containing forbidden syntax [ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs]
- [x] [Review][Patch] Keep Tree-sitter fixture coverage aligned with the checked-in source inventory [ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs]
- [x] [Review][Patch] Document fixture inventory and expectation metadata contract [examples/m18/syntax-proof/README.md]

## Dev Notes

- Keep fixtures in `examples/m18/syntax-proof/`; later epics add semantic/linking fixtures in their own M18 subfolders rather than replacing this slice.
- Reuse `AthenaLanguageParser` and the existing web-tree-sitter harness. Do not add a parser path or duplicate syntax implementation.
- Invalid expectation metadata is compiler-facing and uses simple `key=value` fields. Tree-sitter expectations remain structural and syntax-only.
- A bare import may be represented by Tree-sitter as `incomplete_import_declaration`; that is editor recovery, not semantic acceptance.
- Add no dependency and do not touch Theia UI production code, canvas, LSP, or desktop-viewer.

### References

- [Source: `epics.md` - Epic 1, Story 1.5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-10, NFR-5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 8, 12]
- [Source: `1-4-mirror-package-and-import-syntax-in-tree-sitter.md` - syntax parity and recovery behavior]
- [Source: `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM17InvalidSourceProofTest.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: compiler proof tests failed because `examples/m18/syntax-proof/` did not exist.
- RED: six Tree-sitter fixture tests failed with `ENOENT` before fixture creation.
- GREEN: focused compiler proof tests passed after adding fixtures and calibrating exact alias/wildcard columns.
- Verification: full `:kernel:language:test` passed; Tree-sitter passed 8 corpus and 33 Node tests; encoding audit passed.
- Review: all three layers completed; five harness/documentation patches were applied. Final Tree-sitter verification passed 8 corpus and 34 Node tests.

### Completion Notes List

- Ultimate context engine analysis completed - executable syntax corpus contract prepared.
- Added two valid and four invalid package/import syntax fixtures with deterministic expectation metadata.
- Added compiler AST/diagnostic proof coverage and Tree-sitter valid/error/recovery proof coverage over the same checked-in sources.
- Documented the syntax-only corpus boundary and later M18 evidence accumulation model.
- Hardened metadata parsing, exact span checks, fresh-instance determinism, Tree-sitter node/range assertions, and cross-harness inventory alignment.

### File List

- `_bmad-output/implementation-artifacts/m18/1-5-add-syntax-proof-fixtures.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `examples/m18/README.md`
- `examples/m18/syntax-proof/README.md`
- `examples/m18/syntax-proof/valid-package-import.athena`
- `examples/m18/syntax-proof/valid-package-only.athena`
- `examples/m18/syntax-proof/invalid-alias.athena`
- `examples/m18/syntax-proof/invalid-alias.expectation.txt`
- `examples/m18/syntax-proof/invalid-missing-target.athena`
- `examples/m18/syntax-proof/invalid-missing-target.expectation.txt`
- `examples/m18/syntax-proof/invalid-visibility.athena`
- `examples/m18/syntax-proof/invalid-visibility.expectation.txt`
- `examples/m18/syntax-proof/invalid-wildcard.athena`
- `examples/m18/syntax-proof/invalid-wildcard.expectation.txt`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxProofTest.kt`

## Change Log

- 2026-07-15: Added the first executable `examples/m18` syntax proof slice with compiler and Tree-sitter coverage.
- 2026-07-15: Applied all actionable code-review hardening and completed Epic 1 syntax evidence.
