---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 2.2: Author Type-Safe Cabinet Installation Source

Status: review

## Story

As an engineer or AI author,
I want a compact typed Cabinet installation syntax inside my system,
so that physical intent is readable, compilable, lintable, and independent from renderer details.

## Acceptance Criteria

1. Given the normative M35 source surface, when a system declares `installation cabinet`, enclosure, surface, rail, duct, channel, terminal group, mount, and route members, then ANTLR4 produces typed source models with spans and `cabinet` is the only accepted installation kind, and IR, occurrence, descriptor, snapshot, renderer, pixel, DOM, and transport terms are not public syntax.
2. Given valid, invalid, and incomplete installation fixtures, when ANTLR4, Tree-sitter, formatter, LSP diagnostics/completion/tokens, and parser parity run, then all frontends agree on acceptance, recovery, highlighting, and canonical formatting, and the existing `examples/m23/parser-parity-proof` corpus is extended rather than duplicated.
3. Given physical intent is parsed, when source ownership is inspected, then project Athena source owns authored engineering/installation intent and source spans, and no renderer, SVG, package manifest, or generated IR becomes source authority.
4. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and touched/adjacent stale grammar branches, generated parsers, fixtures, docs, and compatibility names are purged.

## Tasks / Subtasks

- [x] Add RED tests for Cabinet installation syntax and source ownership (AC: 1, 2, 3)
  - [x] Cover one valid `installation cabinet` with enclosure, surface, rail, duct, channel, terminal group, mount, and route members.
  - [x] Cover invalid installation kind, missing required members, unknown public/internal terms, incomplete recovery, and duplicate member ids where the parser/language layer owns the check.
  - [x] Assert parsed source models carry source spans for installation and each typed member.
- [x] Implement typed ANTLR4 source surface and AST models (AC: 1, 3)
  - [x] Add compact source syntax for `installation cabinet` and typed members without exposing IR/renderer/SVG/DOM/transport vocabulary.
  - [x] Add language model types for installation declarations and member declarations with spans.
  - [x] Keep route members as references to stable same-source connection aliases from Story 2.1; do not implement route geometry or channel topology here.
- [x] Update parser adapter, diagnostics, and formatting (AC: 1, 2)
  - [x] Lower parse contexts into typed language models with source-spanned diagnostics.
  - [x] Keep canonical formatting stable and idempotent for installation blocks and typed members.
  - [x] Reject stale/compatibility grammar branches instead of adding compatibility readers.
- [x] Update IDE language surfaces (AC: 2)
  - [x] Update LSP diagnostics/completion/semantic-token classification for new installation keywords and member names.
  - [x] Update Tree-sitter grammar, generated artifacts, corpus, and highlights.
  - [x] Extend existing parser parity corpus, especially `examples/m23/parser-parity-proof`, instead of creating a separate duplicate proof corpus.
- [x] Polish/purge and evidence gate (AC: 4)
  - [x] Audit touched and adjacent grammar/model/LSP/Tree-sitter paths for XML, renderer, DOM, Presentation IR, Graphic Primitive IR, compatibility, and stale Cabinet syntax leakage.
  - [x] Run sequential verification and record RED/GREEN evidence.
  - [x] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 2.2 owns only the authored, type-safe Cabinet installation source surface. It does not build `PhysicalInstallationIR v0`, validate physical contracts, compute placement, compose Cabinet graphics, render UI, route conductors, implement selection trace, or create ECS/product catalog concepts. Later Epic 2 stories consume this parsed source.

### Normative Source Shape

Use the M35 addendum syntax as the source target:

```text
installation cabinet <id> { <installation-member>* }
enclosure <id> size (<length>, <length>, <length>)
surface <id> in <enclosure-id> at (<length>, <length>) size (<length>, <length>) accepts [<mounting-type-id> (, <mounting-type-id>)*]
rail <id> on <surface-id> at (<length>, <length>) length <length> orientation <horizontal|vertical> mounting <mounting-type-id>
duct <id> in <enclosure-id> at (<length>, <length>) size (<length>, <length>) orientation <horizontal|vertical> wall <length>
channel <id> in <duct-id> at (<length>, <length>) size (<length>, <length>) lanes <integer> margin <length>
terminal-group <id> in <enclosure-id> at (<length>, <length>) size (<length>, <length>) orientation <horizontal|vertical> accepts [<mounting-type-id> (, <mounting-type-id>)*]
mount <occurrence-id> device <device-id> on <surface-id|rail-id|terminal-group-id> at (<length>, <length>) orientation <horizontal|vertical>
route <connection-alias> through [<channel-id> (, <channel-id>)*]
```

If existing language naming forces small spelling adjustments, keep the public concepts above intact and document the exact accepted syntax in tests and examples.

### Architecture Requirements

- Athena source is the only source authority for authored installation intent and spans.
- `cabinet` is the only M35 accepted installation kind.
- Public syntax must not expose implementation words: IR, occurrence, descriptor, snapshot, renderer, pixel, DOM, transport, Presentation IR, Graphic Primitive IR, SVG markup, XML, HTML.
- Source models may store parsed intent and spans only. Derived physical facts, final coordinates, representation selection, anchor coordinates, route segments, and graphics remain out of this story.
- Enclosure is the sole physical container. Surface, rail, duct, channel, terminal group, mount, and route are typed declarations, not generic key/value nodes.
- Route syntax references Story 2.1 connection aliases. Connection group names remain organizational and must not identify routes.
- ANTLR4 remains semantic parser authority. Tree-sitter, formatter, LSP, highlighting, examples, and parser parity must move together.
- Because Athena is unreleased, delete stale grammar/fixture paths instead of preserving compatibility adapters.

### Previous Story Intelligence

- Story 2.1 made every engineering connection require a source-unit-scoped alias and migrated active examples/tests. Do not reintroduce endpoint-shaped ids, alias-free route references, or group-name route identity.
- Story 2.1 found stale source paths across runtime/LSP tests; when adding examples, use package-hierarchical paths and explicit `package` declarations.
- Story 2.1 verification showed parser, LSP, runtime, and Tree-sitter cursor positions often fail after syntax changes. Update fixture positions deliberately.

### Likely Code Areas

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaParserContinuityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM17ParserParityProofTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareSymbolsTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/test/corpus/*`
- `examples/m23/parser-parity-proof/*.athena`

### Testing Requirements

- Follow RED/GREEN. Add or update failing tests before production code.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification:
  - targeted language/parser tests;
  - parser parity/continuity tests;
  - targeted LSP diagnostics/completion/token tests;
  - `npm test` in `ide/tree-sitter-athena`;
  - audits for forbidden public syntax leakage and XML/compatibility paths in touched areas;
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.2.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-18..FR-20, FR-23, FR-41..FR-42.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - Physical Installation Source Surface and Cabinet composition boundary.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-4, AD-15, module ownership table.
- `_bmad-output/implementation-artifacts/m35/2-1-give-every-engineering-connection-stable-authored-identity.md` - previous story implementation evidence.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T02:41:16+08:00 - Started Story 2.2 implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`; moving parser/model/LSP/Tree-sitter surfaces together.
- 2026-07-28T02:41:16+08:00 - Started RED tests for Cabinet installation syntax and source ownership.
- 2026-07-28T02:43:00+08:00 - CodeGraph/source read found current grammar supports system/device/port/connect/layout and M34 representation declarations only; no installation declaration exists yet.
- RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests com.engineeringood.athena.language.AthenaLanguageParserTest` failed at test compile with unresolved `InstallationDeclaration`, `InstallationKind`, and member model types.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests com.engineeringood.athena.language.AthenaLanguageParserTest --tests com.engineeringood.athena.language.antlr.AthenaGrammarSmokeTest --tests com.engineeringood.athena.language.AstExtensibilityLandingZoneTest --tests com.engineeringood.athena.language.AthenaM18SyntaxScopeTest` passed.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaParserContinuityTest --tests com.engineeringood.athena.compiler.AuthoredLayoutIntentMapperTest --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexerTest --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticReferenceLinkerTest` passed.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringSupportTest --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareSymbolsTest --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest` passed.
- GREEN evidence: `npm test` in `ide/tree-sitter-athena` passed after `npm run build` regenerated the checked-in wasm.
- Audit evidence: forbidden public syntax search returned only internal comments/model names and explicit negative tests; `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed; `git diff --check` passed with CRLF warnings only.

### Completion Notes List

- Added typed `installation cabinet` authored source syntax with enclosure, surface, rail, duct, channel, terminal-group, mount, and route members.
- Added source AST models and ANTLR adapter lowering with source spans and duplicate installation member id rejection.
- Added parser parity fixtures under the existing M23 corpus, Tree-sitter grammar/highlight coverage, generated parser artifacts, and updated wasm.
- Added LSP outline, semantic token, and canonical project-source formatting support for installation source.
- AC mapping: AC1 covered by language parser tests and model exhaustiveness; AC2 covered by parser parity, Tree-sitter, LSP, and formatter tests; AC3 covered by source-only AST models and no renderer/package/SVG authority in installation syntax; AC4 covered by sequential verification and audit commands.
- Three-layer review: semantic layer keeps installation as authored intent only; surface layer aligns ANTLR, Tree-sitter, LSP, formatter, and examples; architecture layer preserves Athena source SSOT and does not implement physical topology, routing geometry, rendering, ECS, or compatibility readers in this story.

### File List

- `_bmad-output/implementation-artifacts/m35/2-2-author-type-safe-cabinet-installation-source.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `examples/m23/parser-parity-proof/invalid-installation-kind.athena`
- `examples/m23/parser-parity-proof/valid-installation-cabinet.athena`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/installation.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`

### Change Log

- 2026-07-28 - Completed Story 2.2 typed Cabinet installation source surface, IDE syntax support, canonical formatting, parser parity, and verification evidence.
