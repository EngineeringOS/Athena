---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 8.1
epic: 8
title: Support Grouped Connect Authoring Syntax
---

# Story 8.1: Support Grouped Connect Authoring Syntax

## Status

done

## Story

As an Athena author,
I want repeated connection edges to be grouped under a readable `connect <name> { ... }` block,
so dense control-sheet source stays compact without changing Athena's flat semantic relationship
model.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given existing source uses `connect A.out -> B.in`, when it is parsed, then behavior remains
   unchanged.
2. Given source uses `connect groupName { A.out -> B.in }`, when it is parsed by ANTLR, then the
   authored AST preserves `groupName`, child connection declarations, and child edge spans.
3. Given the same grouped source is opened in the editor, when Tree-sitter parses it, then syntax
   highlighting/structure accepts the grouped form without semantic inference.
4. Given grouped and flat forms describe the same edges, when the electrical domain lowers them,
   then canonical `EngineeringConnection` output is equivalent and remains flat.
5. Given LSP document symbols are requested, when a grouped connect block exists, then the Outline
   can show the group and the child edge symbols.
6. Given malformed endpoint arity appears inside a group, when parsing runs, then the diagnostic
   remains the same authored `owner.port` style diagnostic used by flat connect declarations.
7. Given final review runs, when stale parser docs, generated artifacts, obsolete tests, or
   temporary syntax assumptions are found, then they are removed or ledgered.

## Tasks/Subtasks

- [x] Add RED parser/AST tests for grouped connect syntax. (AC: 1, 2, 6)
- [x] Add RED lowering tests proving grouped and flat connections produce equivalent canonical IR.
  (AC: 4)
- [x] Add RED Tree-sitter corpus/highlight tests for grouped connect declarations. (AC: 3)
- [x] Add RED LSP outline test for grouped connect block and child edge symbols. (AC: 5)
- [x] Implement ANTLR grammar, AST model, and parse adapter support. (AC: 1, 2, 6)
- [x] Implement electrical lowering flattening and LSP outline support. (AC: 4, 5)
- [x] Implement Tree-sitter grammar/query/corpus support and regenerate checked-in parser assets if
  required by the project workflow. (AC: 3)
- [x] Run targeted parser/lowering/LSP/Tree-sitter verification sequentially. (AC: 1-6)
- [x] Complete mandatory polish/purge gate, update cleanup ledger if needed, and run encoding
  audit. (AC: 7)

## Dev Notes

- `connect <name> { ... }` is v1 authoring structure only. The group name is readable/foldable
  source provenance, not a canonical semantic relationship bundle.
- Do not add `ConnectionGroup` to `EngineeringModel`; downstream canonical relationships remain
  flat `EngineeringConnection` facts.
- Add a new Athena-owned syntax node under `Declaration`, then update exhaustive consumers
  deliberately. Do not expose ANTLR or Tree-sitter parse tree types downstream.
- Tree-sitter remains syntax UX only. It must not provide compiler diagnostics or semantic truth.
- Empty connect groups are syntactically accepted and lower to zero connections. A future lint can
  warn if needed.
- Duplicate group names are allowed in v1 because the name has no semantic identity.
- Nested connect groups are out of scope.
- Use TDD where production code changes are required. On Windows, run Gradle verification
  sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Story created from user-approved grouped connect syntax design. Implementation started immediately.
- 2026-07-21: Added `ConnectionGroupDeclaration` as an authored syntax node only; canonical
  engineering connections remain flat.
- 2026-07-21: Added ANTLR grouped-connect grammar and parse adapter support, including empty group
  acceptance and child edge spans.
- 2026-07-21: Added explicit grouped-connect flattening in electrical runtime, dummy runtime, and
  compiler test plugin fixture consumers.
- 2026-07-21: Added LSP document symbols for `connect <name>` groups and child edge symbols.
- 2026-07-21: Product smoke exposed a missing semantic inspection source range for grouped child
  edges; fixed by indexing authored connection declarations from both flat declarations and group
  children.
- 2026-07-21: Tree-sitter grouped grammar initially conflicted when the group closing brace was
  optional; resolved by requiring the closing brace for the accepted grouped syntax.
- 2026-07-21: M30 sample group names were renamed from `*path` wording to `supply_feed` and
  `motor_drive` because M30 visual/source token guards prohibit misleading visual geometry terms.
- 2026-07-21: Reproduced the previous M30 Electron screenshot-readiness timeout from evidence:
  the semantic source-range failure no longer appears, and the current fresh smoke passed with a
  routed Cabinet sheet, 9 route facts, grouped Outline nodes, and screenshot capture.
- 2026-07-21: Removed temporary smoke debug log during the mandatory polish/purge gate; no retained
  temporary artifact needs cleanup-ledger ownership.
- 2026-07-21: Review pass found stale `control_path` grouped-connect fixture names; renamed them to
  `control_feed` in language, LSP, and Tree-sitter corpus fixtures so grouped-connect tests do not
  preserve misleading geometry/path vocabulary.

### Completion Notes

- Implemented grouped connect authoring syntax across ANTLR, authored AST, parse adapter,
  Tree-sitter, LSP Outline, semantic inspection source indexing, and runtime lowering.
- Preserved Athena's architecture boundary: group names are source ergonomics/provenance only, and
  downstream `EngineeringConnection` output stays flat.
- Updated the M30 sample to use grouped connect blocks while preserving the product smoke proof and
  Cabinet default graph view.
- Final verification passed sequentially:
  `:kernel:language:test` targeted parser/provenance/exhaustiveness tests,
  `:kernel:compiler:test` grouped lowering and M30 sample tests,
  `:ide:lsp:test` authoring support and semantic inspection tests,
  `yarn --cwd ide/tree-sitter-athena test`,
  `yarn --cwd ide/tree-sitter-athena build`,
  `node ide/theia-frontend/scripts/athena-m30-sample-project.test.mjs`,
  `node ide/theia-frontend/scripts/athena-m30-final-purge-regression.test.mjs`,
  `yarn --cwd ide start:smoke:m30`, and
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- Post-review cleanup verification also passed:
  `:kernel:language:test --tests com.engineeringood.athena.language.AthenaLanguageProvenanceTest --tests com.engineeringood.athena.language.AstExtensibilityLandingZoneTest`,
  `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringSupportTest`,
  `yarn --cwd ide/tree-sitter-athena test`,
  `yarn --cwd ide/tree-sitter-athena build`,
  and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- `yarn --cwd ide/tree-sitter-athena build` emitted the existing `wasm-ld` shared-library warning
  while exiting successfully.
- Code review gate completed locally after the external review agents timed out without findings.
  The local review found and fixed stale grouped-connect `*_path` fixture vocabulary, verified no
  grouped relationship type leaked into canonical engineering model/runtime output, and found no
  remaining Story 8.1 acceptance issue.

## File List

- `_bmad-output/implementation-artifacts/m30/8-1-support-grouped-connect-authoring-syntax.md`
- `_bmad-output/implementation-artifacts/m30/epics.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `examples/m30/sample-project/src/01-rolling-shutter-control-source.athena`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt`
- `ide/theia-frontend/scripts/athena-m30-sample-project.test.mjs`
- `ide/theia-product/scripts/verify-athena-m30-sample-project.js`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/connect.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaGroupedConnectLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`

## Change Log

- 2026-07-21: Story created for grouped connect authoring syntax.
- 2026-07-21: Implemented grouped connect syntax, lowering, editor structure support, M30 sample
  usage, verification, and polish/purge record.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof
  artifacts, misleading design notes, unused compatibility paths, and accidental generated
  artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story
  notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what
  passed.
