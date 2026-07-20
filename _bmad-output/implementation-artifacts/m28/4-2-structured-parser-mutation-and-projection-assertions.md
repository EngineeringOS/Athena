---
status: done
story_id: 4.2
epic: 4
title: Structured Parser, Mutation, And Projection Assertions
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 4.2: Structured Parser, Mutation, And Projection Assertions

## Story

As an Athena maintainer, I want structured assertions at parser, compiler, runtime, LSP, and
frontend seams, so that M28 failures are diagnosable without visual guessing.

## Acceptance Criteria

- Parser assertions cover nested ports in ANTLR4 and Tree-sitter.
- Compiler assertions cover canonical identity and provenance.
- Runtime/LSP assertions cover `SemanticRelationshipIntent`, electrical specialization, mutation
  result, and diagnostics.
- Frontend assertions cover relationship mode, preview cleanup, and no DOM-text semantic authority.

## Tasks/Subtasks

- [x] Inventory M28 coverage by seam.
- [x] Add missing structured assertions.
- [x] Remove or update stale assertions that encode old connection-only naming.
- [x] Run focused and integration tests sequentially.

## Dev Notes

- Architecture: M28 AD-1, AD-4, AD-7 are binding.

## Dev Agent Record

### Debug Log

- Created `m28-structured-assertion-inventory.md`.
- Confirmed retained `connect-ports` assertions are legacy compatibility coverage, not M28 root architecture.
- Added missing backend source-edit gate assertion through Story 4.1 product smoke.
- Ran parser, compiler/indexer, authoring-model, LSP, tree-sitter, and frontend M28 checks sequentially.

### Completion Notes

- M28 has structured coverage across ANTLR4, Tree-sitter, compiler lowering/indexing, authoring contracts, relationship validation, LSP mutation/projection, frontend relationship mode, and product smoke wiring.
- No M28 assertion depends on visible DOM text as semantic authority.
- Stale connection-only test language is either outside M28 compatibility coverage or explicitly retained with rationale.

## File List

- _bmad-output/implementation-artifacts/m28/m28-structured-assertion-inventory.md
- _bmad-output/implementation-artifacts/m28/4-2-structured-parser-mutation-and-projection-assertions.md

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Added structured assertion inventory and verified M28 parser, mutation, projection, and frontend seams.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests "com.engineeringood.athena.language.AthenaLanguageParserTest.parses nested device owned ports as first class component anatomy"`: passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM28NestedPortCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM28SampleProjectCompilerTest" --tests "com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexerTest"`: passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test`: passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest" --tests "com.engineeringood.athena.ide.lsp.AthenaM28ProductAuthoringSmokeTest"`: passed.
- `npm test` in `ide/tree-sitter-athena`: passed.
- `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-authoring-protocol.test.mjs scripts/athena-m28-relationship-authoring-model.test.mjs scripts/athena-m28-product-smoke-wiring.test.mjs } else { exit $LASTEXITCODE }` in `ide/theia-frontend`: passed.
