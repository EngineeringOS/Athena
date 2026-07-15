---
baseline_commit: 132c2ee
---

# Story 1.4: Mirror Package And Import Syntax In Tree-sitter

Status: done

## Story

As an IDE user,
I want package/import syntax to highlight and structure correctly,
so that authoring feels native without giving Tree-sitter semantic authority.

## Acceptance Criteria

1. Tree-sitter accepts the supported optional `package`, repeated plain qualified-target `import`, and existing `system` syntax in compiler-compatible order.
2. The syntax tree exposes stable `package_declaration`, `import_declaration`, and qualified package-name structure suitable for outline/folding and incremental editor UX.
3. `package` and `import` keywords are highlighted through the existing Theia Tree-sitter semantic-token adapter; package/import name segments remain syntax tokens, not semantic resolution results.
4. Incomplete package/import declarations retain a full-range usable tree and preserve the following system declaration where recovery permits.
5. Existing M17 grammar corpus, incomplete-source behavior, and Theia fallback behavior remain compatible.
6. Tree-sitter performs no package lookup, import classification, diagnostics, linking, lowering, LSP semantics, frontend-local resolution, or canvas work.

## Tasks / Subtasks

- [x] Add Tree-sitter tests first (AC: 1-5)
  - [x] Add corpus coverage for package plus ordered package/symbol-target imports and exact named-node structure.
  - [x] Add incomplete package/import recovery fixtures to the web-tree-sitter harness.
  - [x] Extend the Theia highlighting-service test to prove `package` and `import` keyword tokens from checked-in grammar assets.
- [x] Mirror the supported compiler syntax (AC: 1-3, 6)
  - [x] Add optional package and repeated import rules before the existing system declaration.
  - [x] Preserve dotted, digit-bearing, capitalized, and internally hyphenated package/import segments.
  - [x] Add only syntax captures for `package` and `import`; do not add semantic capture types or frontend resolution.
  - [x] Update syntax-only boundary documentation.
- [x] Regenerate and verify checked-in assets (AC: 1-5)
  - [x] Regenerate `src/grammar.json`, `src/node-types.json`, and `src/parser.c` with the pinned Tree-sitter CLI.
  - [x] Rebuild `tree-sitter-athena.wasm` for the Theia syntax path.
  - [x] Run grammar tests and Theia highlighting-service tests.
  - [x] Run the encoding audit after text edits.

### Review Findings

- [x] [Review][Patch] Preserve the following system/import structure when package or import has no same-line target [ide/tree-sitter-athena/grammar.js]
- [x] [Review][Patch] Assert recovered header fields, prefix text, and following import survival [ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs]
- [x] [Review][Patch] Use and assert the standard namespace syntax-token kind for package/import paths [ide/tree-sitter-athena/queries/highlights.scm]
- [x] [Review][Patch] Cover bare-header recovery through checked-in WASM and the Theia highlighting service [ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs]

## Dev Notes

- Work only in `ide/tree-sitter-athena` and the existing `ide/theia-frontend` syntax-token test unless verification proves another syntax-asset seam must change.
- Keep Tree-sitter syntax-only per AD-107/AD-108. A clean tree or keyword token must never imply package/import semantic success.
- Mirror the ANTLR header order from Stories 1.1/1.2; do not add alias, wildcard, export, visibility, comments, numeric literals, expressions, or new declaration families.
- Use a dedicated package-name rule because existing system-body `qualified_name` does not permit hyphenated segments.
- Stable declaration nodes provide outline/folding structure; do not build a custom outline or folding UI in this story.
- Keep `tree-sitter-cli >=0.26.1` and `web-tree-sitter ^0.26.0`; add no dependency.
- The frontend authority remains Theia/VS Code-like syntax UX. The EPLAN-style canvas and Kotlin Compose desktop viewer are not involved.

### References

- [Source: `epics.md` - Epic 1, Story 1.4]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-9, NFR-4, NFR-5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 6, 8, 12]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-2, AD-4, AD-11]
- [Source: `ide/tree-sitter-athena/grammar.js`]
- [Source: `1-3-preserve-narrow-syntax-scope.md` - supported syntax boundary]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- RED: Tree-sitter corpus reported the package/import header as an `ERROR` node under the M17-only grammar.
- RED: Theia highlighting test produced no `package` keyword token from the checked-in M17 WASM/query assets.
- GREEN: `yarn --cwd ide/tree-sitter-athena test` passed 8 corpus parses and 13 web-tree-sitter tests after grammar generation and WASM rebuild.
- GREEN: `yarn --cwd ide/theia-frontend test` passed all 48 Theia frontend tests with explicit package/import keyword-token assertions.
- Review: Blind Hunter findings were applied; Acceptance Auditor found no AC violations and independently verified all suites; Edge Case Hunter timed out without findings.
- Review verification: Tree-sitter passed 8 corpus cases and 27 Node tests; Theia passed 49 frontend tests after recovery and namespace-token patches.

### Completion Notes List

- Ultimate context engine analysis completed - syntax-only Tree-sitter mirror guide prepared.
- Added optional package and repeated import declaration nodes with contiguous qualified package-name tokens.
- Added package/import keyword captures through the existing Theia semantic-token syntax adapter.
- Added exact corpus structure, contextual-keyword behavior, and incomplete-header recovery coverage.
- Regenerated parser sources/node types and rebuilt the checked-in Tree-sitter WASM without adding semantic behavior.
- Added explicit incomplete header nodes so bare package/import recovery preserves following declarations without frontend diagnostics.
- Added standard namespace token classification and valid/invalid WASM-level header parity matrices.

### File List

- `_bmad-output/implementation-artifacts/m18/1-4-mirror-package-and-import-syntax-in-tree-sitter.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs`
- `ide/theia-frontend/src/browser/athena-tree-sitter-highlighting-service.ts`
- `ide/tree-sitter-athena/README.md`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/package-import.txt`
- `ide/tree-sitter-athena/test/incomplete/incomplete-import.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/incomplete-package.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/bare-import.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/bare-package.athena.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`

## Change Log

- 2026-07-15: Mirrored package/import syntax into Tree-sitter, Theia keyword highlighting, recovery fixtures, and generated WASM/parser assets.
- 2026-07-15: Applied review hardening for bare-header recovery, namespace tokens, exact recovery assertions, and narrow parity matrices.
