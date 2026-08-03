---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.1: Prefer `to` For Connections

Status: done

Post-story correction: M39 later approved `->` as an exact alias for `to`. This story remains the completed migration that made `to` the preferred human spelling. Any old rejection-only guidance in the historical notes is superseded by the M39 Reality Graph PRD: both spellings must lower through one path.

## Story

As an engineer,
I want every authored connection to use `to`,
so that Athena reads like engineering language instead of programmer notation.

## Acceptance Criteria

1. Single connections parse with `connect name Source.port to Target.port` and produce the existing `ConnectionDeclaration` shape, spans, alias, source endpoint, and target endpoint.
2. Grouped connection edges parse with `name Source.port to Target.port` and preserve the existing `ConnectionGroupDeclaration` behavior.
3. `to` becomes the preferred emitted spelling in grammar, source generators, formatter, LSP labels, maintained Athena sources, tests, and user-facing syntax documentation. Later M39 planning permits `->` as the same relation alias through one compiler path.
4. Formatter output, governed source edits, and LSP document symbols emit `to` consistently.
5. Tree-sitter highlights `to` as relationship language, regenerates all checked-in parser/WASM outputs, and preserves incomplete-source behavior.
6. Compiler and domain lowering receive the same authored connection model and produce the same semantic identities, provenance, validation, and engineering relationships as before this syntax replacement.
7. The story changes no domain verbs, connection `intent` behavior, grouped relationship semantics, layout, routing, presentation facts, SVG export, or Theia rendering.

## Tasks / Subtasks

- [x] Establish red language tests (AC: 1, 2, 3)
  - [x] Change focused ANTLR and parser fixtures to `to`; assert the existing AST values and spans remain correct.
  - [x] Add an explicit rejection test for `connect name A.p -> B.p`.
  - [x] Rename the M17 `missing-arrow` invalid fixture and expectation to describe missing `to`.
- [x] Replace the compiler-owned syntax token (AC: 1, 2, 3, 6)
  - [x] In `Athena.g4`, use the existing `TO` token in `connectDecl`, `connectGroupEdge`, and the connection-shaped projection-policy guard.
  - [x] Delete the `ARROW` lexer token. Do not introduce a second token or transition rule.
  - [x] Change parser diagnostics from "after '->'" to "after 'to'".
  - [x] Keep `ConnectionDeclaration`, `ConnectionGroupDeclaration`, `EngineeringIrLowerer`, and domain-plugin contracts structurally unchanged.
- [x] Align every Athena source producer and IDE label (AC: 3, 4, 6)
  - [x] Make `BackendAuthoringSourceEditPlanner` emit `connect ... to ...`.
  - [x] Make `AthenaProjectSourceFormatter` emit `to` for single and grouped connections.
  - [x] Make LSP document-symbol names emit `to` for single connections and grouped child edges.
  - [x] Update focused compiler/LSP tests for generated edits, formatting, symbols, navigation, source mutation, and repeated-edit stability.
- [x] Align Tree-sitter syntax UX (AC: 3, 5)
  - [x] Replace arrow literals in `connect_declaration`, `connect_group_edge`, and the projection-policy forbidden connection shape.
  - [x] Highlight `to` as `@athenaRelationshipKeyword`; remove arrow operator queries.
  - [x] Update connection corpus and incomplete-source fixtures/tests.
  - [x] Regenerate `src/grammar.json`, `src/node-types.json`, `src/parser.c`, and `tree-sitter-athena.wasm` through repository scripts. Do not edit generated files by hand.
- [x] Migrate the active repository without compatibility residue (AC: 3, 6)
  - [x] Replace connection separators in every maintained `.athena` source. The current audit finds 61 files under `examples`, `kernel`, `extensions`, and `ide`.
  - [x] Update embedded Athena source and expected generated-source strings in Kotlin and JavaScript tests.
  - [x] Update user-facing documentation that demonstrates `connect ... -> ...`; do not rewrite unrelated architecture-flow arrows, Kotlin lambdas, SVG markers, or prose notation.
  - [x] Preserve existing uncommitted work in every touched file. Re-read current contents before editing and never revert unrelated changes.
- [x] Verify the complete syntax replacement (AC: 1-7)
  - [x] Run language, compiler, runtime, and LSP Gradle verification strictly sequentially.
  - [x] Run the Tree-sitter generation, corpus, highlighting, and WASM tests.
  - [x] Prove no active `.athena` source or source producer still emits the arrow connection form.
  - [x] Run source-set hygiene, encoding audit, and `git diff --check`.

## Dev Notes

### Governing Boundary

This was a direct pre-1.0 language cleanup that made `to` the preferred spelling. The later approved M39 rule is narrower: `to` and `->` are two spellings of the same relation and must not create two compiler paths, serializers, diagnostics, or legacy helpers.

Story 1.1 changes spelling, not meaning:

```text
connect alias Source.port to Target.port
    -> existing ConnectionDeclaration
    -> existing compiler/domain lowering
    -> unchanged engineering relationship
```

The arrows above describe compiler flow; they are not Athena syntax.

### Current Implementation Map

| Responsibility | Current authority | Required change |
| --- | --- | --- |
| Compiler grammar | `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4` | Replace `ARROW` with the already-defined `TO`; delete `ARROW` |
| AST adaptation | `AthenaAntlrParseAdapter.kt` | Update target diagnostics only; retain model construction |
| Authored model | `AthenaLanguageModel.kt` | No data-shape change; update stale syntax comments only if touched |
| Governed source creation | `BackendAuthoringSourceEditPlanner.kt` | Emit `to` |
| Formatting | `AthenaProjectSourceFormatter.kt` | Emit `to` for single/grouped forms |
| LSP outline | `AthenaLanguageFeatures.kt` | Display `to` in single/grouped connection symbols |
| Syntax UX | `ide/tree-sitter-athena/grammar.js` and `queries/highlights.scm` | Parse/highlight `to`; remove arrow operator behavior |
| Generated syntax UX | `ide/tree-sitter-athena/src/*` and `tree-sitter-athena.wasm` | Regenerate from `grammar.js` |

`TO : 'to' ;` already exists in the ANTLR grammar for graphic primitives, so this story reuses the established token. It must not add another `to` token.

`ConnectionDeclaration` does not store the separator token. `EngineeringIrLowerer`, semantic indexing/linking, connection validation, domain plugins, layout, routing, Presentation Document creation, and Theia should therefore require no behavioral production changes. If one of those layers must change to make `to` work, stop and identify the leaked syntax dependency before proceeding.

### Scope Guardrails

- Story 1.2 introduces domain-provided relation words such as `power`, `control`, and `earth`; do not add them here.
- Story 1.3 removes connection `intent`; keep current intent parsing behavior in this story.
- Do not redesign `ConnectionGroupDeclaration` or its current lowering here. Grouped one-to-many relationship preservation belongs to the following language/domain work.
- Do not modify layout, routing, SVG, Presentation Document, or Theia behavior.
- Do not add dependencies or a new parser/formatter abstraction.
- Do not put milestone, proof, demo, sample, `V0`, or `V1` names in production classes.

### Test Requirements

Use test-first implementation. Minimum focused coverage:

- ANTLR grammar accepts `to` and exposes `TO()` in the connection rule.
- ANTLR and `AthenaLanguageParser` accept `to`; later M39 alias work must make `->` follow the same parse and semantic path.
- Single and grouped declarations retain aliases, endpoint names, intent values, and source spans.
- Formatter round-trip emits only `to`.
- Governed relationship creation emits source accepted by the compiler.
- LSP document symbols and grouped child symbols show `to`; navigation/ranges remain valid.
- Tree-sitter corpus parses single/grouped/intent-bearing connections with `to`, highlights it as relationship language, and retains incomplete-source tolerance.
- Compiler lowering snapshots remain semantically unchanged after fixture migration.

Run Gradle commands one at a time on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
```

Then run:

```powershell
yarn --cwd ide/tree-sitter-athena test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Final syntax audits must target Athena connection syntax, not every use of `->` in the repository:

```powershell
rg -n --glob '*.athena' --fixed-strings -- '->' examples kernel extensions ide
rg -n --glob '*.kt' --glob '*.mjs' 'connect [^\r\n]* -> ' kernel ide extensions examples
```

Both commands must return no active connection-syntax matches. Historical discussion may retain quoted old syntax only when it is explicitly labeled obsolete; active usage docs must use `to`.

No screenshot is required for Story 1.1 because renderer behavior is intentionally unchanged.

### Project Structure Notes

- ANTLR remains compiler syntax authority; generated ANTLR sources remain under Gradle build output.
- Tree-sitter remains editor syntax UX only and must mirror, never redefine, compiler syntax.
- The repository is currently heavily modified by active milestone work. Work with those changes in place and keep this story's edits scoped to connection syntax.
- External web research is unnecessary: this story changes no library or protocol version and uses the repository's existing ANTLR and Tree-sitter toolchains.

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - Functional Requirements FR-1 through FR-7 and FR-22]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-1 and Source Boundary]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E1, Story 1.1]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Build Verification Rule, Source-Set Hygiene Rule]
- [Source: `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4` - `connectDecl`, `connectGroupEdge`, `TO`, `ARROW`]
- [Source: `ide/tree-sitter-athena/README.md` - Syntax UX boundary and regeneration commands]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Focused language tests established `to` parsing and deterministic arrow rejection before grammar replacement.
- Full compiler verification initially exposed stale governed lock hashes. Compiler-owned lock materialization then exposed four invalid legacy repositories instead of hiding them behind compatibility behavior.
- M12 was corrected to `com.engineeringood.rendererbenchmark`; M21, M22, and M25 sources were moved beneath package-matching source directories. All 23 affected governed locks were regenerated through `AthenaCompiler.materializeRepositoryLock`.
- First full runtime run found one stale arrow-form preview assertion in `AthenaM31SampleAuthoringProofTest`; the canonical producer already emitted `to`. The assertion was corrected and focused plus full runtime tests passed.
- Final residue audit found and removed stale M21/M22/M24/M26/M27 frontend syntax expectations. Only the explicit parser rejection fixture retains authored arrow syntax.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Replaced arrow connection syntax with sole canonical separator `to` across ANTLR, diagnostics, source generation, formatting, LSP symbols, Tree-sitter, maintained sources, tests, and current usage docs.
- Preserved authored AST, semantic lowering, connection identity, provenance, routing, presentation, SVG export, and Theia rendering behavior.
- Removed all compatibility residue: no dual grammar, alias, fallback, rewrite, deprecation path, or legacy token remains.
- Cleaned four legacy governed repository layouts to the current package contract and regenerated all affected locks from compiler authority.
- Verified sequential Gradle suites for language, compiler, runtime, and LSP; Tree-sitter generation/corpus/highlight/WASM; 15 affected Theia source-contract tests; source-set hygiene; encoding; syntax residue; and `git diff --check`.

### File List

See complete story-owned inventory below. Deleted paths are retained in this list to document direct pre-1.0 removal and relocation.

- `_bmad-output/implementation-artifacts/m39/1-1-replace-arrow-connections-with-to.md`
- `_bmad-output/implementation-artifacts/m39/sprint-status.yaml`
- `docs/compiler/m0-dsl.md`
- `docs/usages/m12-proof-usage.md`
- `docs/usages/m15-proof-usage.md`
- `docs/usages/m17-proof-usage.md`
- `docs/usages/m18-proof-usage.md`
- `docs/usages/m21-proof-usage.md`
- `docs/usages/m22-proof-usage.md`
- `docs/usages/m25-proof-usage.md`
- `docs/usages/m25-representation-acceptance-proof.md`
- `docs/usages/m28-proof-usage.md`
- `examples/m0/demo-cabinet.athena`
- `examples/m0/dual-drive-cabinet.athena`
- `examples/m0/duplicate-identity-cabinet.athena`
- `examples/m0/invalid-direction-cabinet.athena`
- `examples/m0/invalid-semantic-cabinet.athena`
- `examples/m11/dense-electrical-proof/athena.lock`
- `examples/m11/dense-electrical-proof/src/com/engineeringood/assemblyline/assemblyline.athena`
- `examples/m12/README.md`
- `examples/m12/README.zh-CN.md`
- `examples/m12/renderer-benchmark-proof/athena.lock`
- `examples/m12/renderer-benchmark-proof/athena.yaml`
- `examples/m12/renderer-benchmark-proof/src/expansion-line.athena` (deleted)
- `examples/m12/renderer-benchmark-proof/src/com/engineeringood/rendererbenchmark/expansion-line.athena` (added)
- `examples/m14/siemens-proof-corpus/athena.lock`
- `examples/m14/siemens-proof-corpus/src/com/engineeringood/examples/m14/siemens/proof/corpus/siemens-proof-corpus.athena`
- `examples/m17/README.md`
- `examples/m17/invalid-and-incomplete-proof/missing-arrow.athena` (deleted)
- `examples/m17/invalid-and-incomplete-proof/missing-arrow.expectation.txt` (deleted)
- `examples/m17/invalid-and-incomplete-proof/missing-to.athena` (added)
- `examples/m17/invalid-and-incomplete-proof/missing-to.expectation.txt` (added)
- `examples/m17/parser-parity-proof/dense-qualified-names.athena`
- `examples/m17/parser-parity-proof/parity-cabinet.athena`
- `examples/m17/repository-parity-proof/athena.lock`
- `examples/m17/repository-parity-proof/src/com/engineeringood/m17/parity/parity-repo.athena`
- `examples/m18/linking-lowering-proof/cross-package-consumer.athena`
- `examples/m18/linking-lowering-proof/cross-source-consumer.athena`
- `examples/m18/linking-lowering-proof/invalid-availability-consumer.athena`
- `examples/m18/linking-lowering-proof/single-package-success.athena`
- `examples/m18/linking-lowering-proof/unresolved-symbol.athena`
- `examples/m18/repository-proof/valid-workspace/athena.lock`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/cross-package-consumer.athena`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/single-package-success.athena`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/unresolved-symbol.athena`
- `examples/m2/demo-cabinet.athena`
- `examples/m20/sample-project/athena.lock`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/01-schematic-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/02-dense-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/03-acceptance-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/04-boundary-scope.athena`
- `examples/m21/sample-project/README.md`
- `examples/m21/sample-project/athena.lock`
- `examples/m21/sample-project/src/01-baseline-sheet.athena` (deleted)
- `examples/m21/sample-project/src/02-layout-intelligence-acceptance.athena` (deleted)
- `examples/m21/sample-project/src/03-routing-and-label-readability.athena` (deleted)
- `examples/m21/sample-project/src/04-boundary-scope.athena` (deleted)
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/01-baseline-sheet.athena` (added)
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/02-layout-intelligence-acceptance.athena` (added)
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/03-routing-and-label-readability.athena` (added)
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/04-boundary-scope.athena` (added)
- `examples/m22/sample-project/M22-BASELINE-PROOF.md`
- `examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md`
- `examples/m22/sample-project/README.md`
- `examples/m22/sample-project/athena.lock`
- `examples/m22/sample-project/src/01-baseline-sheet.athena` (deleted)
- `examples/m22/sample-project/src/02-layout-optimization-acceptance.athena` (deleted)
- `examples/m22/sample-project/src/03-component-round-trip.athena` (deleted)
- `examples/m22/sample-project/src/04-boundary-scope.athena` (deleted)
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/01-baseline-sheet.athena` (added)
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/02-layout-optimization-acceptance.athena` (added)
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/03-component-round-trip.athena` (added)
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/04-boundary-scope.athena` (added)
- `examples/m23/parser-parity-proof/valid-installation-cabinet.athena`
- `examples/m23/sample-project/athena.lock`
- `examples/m23/sample-project/src/com/engineeringood/m23/sample/01-layout-hints.athena`
- `examples/m24/sample-project/athena.lock`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/01-control-route.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/02-terminal-strip-routes.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/03-power-protection-load.athena`
- `examples/m25/sample-project/README.md`
- `examples/m25/sample-project/athena.lock`
- `examples/m25/sample-project/src/01-professional-symbol-sheet.athena` (deleted)
- `examples/m25/sample-project/src/02-terminal-labels-and-routes.athena` (deleted)
- `examples/m25/sample-project/src/03-six-family-acceptance.athena` (deleted)
- `examples/m25/sample-project/src/com/engineeringood/m25/sample/01-professional-symbol-sheet.athena` (added)
- `examples/m25/sample-project/src/com/engineeringood/m25/sample/02-terminal-labels-and-routes.athena` (added)
- `examples/m25/sample-project/src/com/engineeringood/m25/sample/03-six-family-acceptance.athena` (added)
- `examples/m26/sample-project/athena.lock`
- `examples/m26/sample-project/src/com/engineeringood/m26/sample/01-workspace-semantic-source.athena`
- `examples/m26/sample-project/src/com/engineeringood/m26/sample/02-field-assets-not-a-sheet.athena`
- `examples/m27/sample-project/athena.lock`
- `examples/m27/sample-project/src/com/engineeringood/m27/sample/01-workspace-semantic-source.athena`
- `examples/m27/sample-project/src/com/engineeringood/m27/sample/02-field-assets-not-a-sheet.athena`
- `examples/m28/sample-project/athena.lock`
- `examples/m28/sample-project/src/com/engineeringood/m28/sample/01-relationship-authoring-source.athena`
- `examples/m28/sample-project/src/com/engineeringood/m28/sample/02-relationship-candidates.athena`
- `examples/m29/sample-project/athena.lock`
- `examples/m29/sample-project/src/com/engineeringood/m29/sample/01-interaction-authoring-source.athena`
- `examples/m29/sample-project/src/com/engineeringood/m29/sample/02-interaction-candidates.athena`
- `examples/m3/dual-domain-proof.athena`
- `examples/m3/dummy-proof.athena`
- `examples/m3/electrical-proof.athena`
- `examples/m30/sample-project/athena.lock`
- `examples/m30/sample-project/src/com/engineeringood/m30/sample/01-rolling-shutter-control-source.athena`
- `examples/m31/sample-project/athena.lock`
- `examples/m31/sample-project/src/com/engineeringood/m31/sample/01-governed-authoring-customer-source.athena`
- `examples/m32/sample-project/athena.lock`
- `examples/m32/sample-project/src/com/engineeringood/m32/sample/01-package-platform-demo.athena`
- `examples/m34/professional-control-drawing/athena.lock`
- `examples/m34/professional-control-drawing/src/com/engineeringood/m34/professional/01-control-drawing.athena`
- `examples/m34/sample-project/athena.lock`
- `examples/m34/sample-project/src/com/engineeringood/m34/sample/01-native-cabinet-proof.athena`
- `examples/m35/physical-installation-cabinet/athena.lock`
- `examples/m35/physical-installation-cabinet/src/com/engineeringood/m35/physicalinstallationcabinet/01-physical-installation-cabinet.athena`
- `examples/m36/connectivity-cabinet/athena.lock`
- `examples/m36/connectivity-cabinet/src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena`
- `examples/m4/open-repository-proof/athena.lock`
- `examples/m4/open-repository-proof/src/com/engineeringood/factoryline/factoryline.athena`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM28ProductAuthoringSmokeTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareNavigationTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareSymbolsTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepeatedEditingStabilityTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticHistoryStateRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceNavigationParityTest.kt`
- `ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-routing-acceptance.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m26-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m27-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/connect.txt`
- `ide/tree-sitter-athena/test/corpus/installation.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM12RendererBenchmarkTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaGroupedConnectLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM21SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM22SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM25SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM35DedicatedCabinetSampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlannerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticCapabilityProvenanceProjectorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLinkedLowererTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinkerTest.kt`
- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM17InvalidSourceProofTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaM31SampleAuthoringProofTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticCommitServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticHistoryStateServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewServiceTest.kt`

### Change Log

- 2026-08-01: Replaced authored arrow connections with canonical `to`, migrated maintained sources and source producers, corrected invalid governed sample layouts, regenerated locks and Tree-sitter outputs, and completed full verification.

### Senior Developer Review (AI)

Outcome: Approve

Date: 2026-08-01

Findings: None.

Review notes:

- Active `.athena` source audit found no `->` connection syntax under `examples`, `kernel`, `extensions`, or `ide`.
- Source-producer audit found no active `connect ... -> ...` emission in Kotlin or JavaScript production/test producers.
- Remaining authored arrow occurrence is the intentional parser rejection fixture in `AthenaLanguageParserTest`.
