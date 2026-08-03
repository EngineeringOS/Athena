---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.2: Add Domain Connection Verbs

Status: done

Post-story correction: M39 later approved `->` as an exact alias for `to`. `to` remains preferred in examples and formatting, but `->` is no longer forbidden when it lowers through the same relation path.

## Story

As an engineer,
I want to write domain relations such as `power`, `control`, and `earth`,
so that common engineering relations are clear and short.

## Acceptance Criteria

1. `power A.out to B.in`, `control A.out to B.in`, and `earth PE.out to [M1.pe, Cabinet.pe]` parse as first-class relation declarations without `connect` or `intent`. `->` is allowed later only as the same relation alias.
2. Relation words are resolved by the active electrical domain package. Athena kernel syntax admits a generic relation word but does not hardcode `power`, `control`, or `earth` as universal keywords.
3. Compiled engineering connections keep the existing endpoint identity, source span, target span, connection identity, validation, and downstream lowering behavior.
4. Each domain relation carries a domain-owned relation contract property that later stories can use for compatibility, medium, separation, and drawing defaults.
5. Grouped target syntax remains one authored relationship with ordered targets and one source trace; compiler may derive member connections only as children of that source relationship.
6. Unknown relation words fail with a plain diagnostic that names the active domain plugin and available relation words.
7. Formatter, LSP document symbols, Tree-sitter grammar/highlights, maintained tests, and examples use the new natural relation syntax where this story requires it.
8. No second compiler path is added for `->`, no electrical words become kernel keywords, no Theia/render/layout behavior changes, and connection `intent` removal remains Story 1.3.

## Tasks / Subtasks

- [x] Establish red language tests (AC: 1, 2, 5, 6)
  - [x] Add parser tests for single relation statements using non-keyword relation words.
  - [x] Add parser tests for grouped target relation syntax with ordered targets.
  - [x] Add a rejection/diagnostic test for an unknown relation under the electrical plugin path.
- [x] Add syntax-owned relation AST without kernel vocabulary leakage (AC: 1, 2, 5)
  - [x] Extend `Athena.g4` with a generic relation statement form that starts with `ident`, uses `TO`, and accepts one endpoint or `[endpoint, ...]`.
  - [x] Add authored AST nodes that preserve relation word, source endpoint, ordered targets, spans, and optional generated child aliases.
  - [x] Keep `connect` parsing only for still-active legacy tests until Story 1.3 removes remaining `intent` authoring; later alias work may add `->` only through the same relation path.
- [x] Add domain relation contract resolution (AC: 2, 4, 6)
  - [x] Add plugin-facing relation vocabulary/contract API in `kernel/plugins/plugin-api` or the existing domain semantics contribution path.
  - [x] Electrical runtime declares exactly `power`, `control`, and `earth` for this story.
  - [x] Unknown relation diagnostic must include plugin id `com.engineeringood.athena.domain.electrical-runtime` and available words `power, control, earth`.
- [x] Lower relation statements to canonical engineering connections (AC: 3, 4, 5)
  - [x] Lower single relation to existing `EngineeringConnection` shape with unchanged endpoint resolution.
  - [x] Lower grouped target relation into deterministic member connections tied to one relationship/network trace.
  - [x] Add a relation contract property such as `relation.kind=<word>` without moving visual policy into source.
- [x] Align IDE syntax surface (AC: 7)
  - [x] Formatter emits natural relation syntax for relation declarations.
  - [x] LSP document symbols display `<relation> <source> to <target>` and grouped target membership plainly.
  - [x] Tree-sitter parses/highlights relation words as relationship language and `to` as relationship keyword.
- [x] Verify story boundary (AC: 1-8)
  - [x] Run focused language/compiler/LSP tests for relation syntax and diagnostics.
  - [x] Run Tree-sitter corpus/highlight tests and regenerate checked-in generated outputs if grammar changes.
  - [x] Run sequential Gradle suites required by affected modules.
  - [x] Run source-set hygiene, encoding audit, and `git diff --check`.

## Dev Notes

### Governing Boundary

This story adds a human relation surface, not a renderer feature. User source says:

```athena
power Supply.L1 to Breaker.1
control Drive.DO1 to Terminal.1
earth EarthBar.PE to [Motor.PE, Cabinet.PE]
```

Athena core may know there is a `relationWord`; it must not know electrical vocabulary. Electrical meaning belongs to the active domain package.

Do not implement M40/M41 work here: no placement quality, routing optimization, line styling, label policy, Theia repair, SVG layout, or diagram grammar facts beyond the minimum relation contract needed by Story 2.x.

### Current Implementation Map

| Responsibility | Current authority | Story change |
| --- | --- | --- |
| ANTLR grammar | `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4` | Add generic relation statement and target list syntax. Do not add `POWER`, `CONTROL`, or `EARTH` lexer tokens. |
| AST | `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` | Add syntax-only relation declaration preserving word, source, ordered targets, spans. |
| AST adapter | `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt` | Adapt relation parse tree to AST; keep endpoint diagnostics plain. |
| Domain lowering | `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt` | Electrical plugin lowers known relation words to connection blueprints with relation properties. |
| Domain validation | `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt` | Emit unknown relation diagnostic with active plugin and available words. |
| Canonical IR | `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt` | Prefer adding only the minimum relation trace/contract data needed; do not invent ECS or diagram grammar here. |
| Compiler lowerer | `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` | Preserve existing `EngineeringConnection` output and grouped/network behavior. |
| LSP/formatter | `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`, `AthenaLanguageFeatures.kt` | Show and format relation declarations naturally. |
| Tree-sitter | `ide/tree-sitter-athena/grammar.js`, `queries/highlights.scm` | Mirror compiler syntax only. |

### Previous Story Intelligence

Story 1.1 made `to` the preferred spelling and verified language, compiler, runtime, LSP, Tree-sitter, source-set hygiene, encoding, syntax residue, and `git diff --check`. Later M39 planning permits `->` as the same relation alias only. It found legacy package layout issues; do not add general compatibility shims when old sources break. Current rule: direct pre-1.0 cleanup.

Any old arrow rejection fixture is superseded by the approved alias rule. Replace rejection-only coverage with proof that `to` and `->` lower identically.

### Implementation Guardrails

- Use test-first changes.
- Keep normal source K.I.S.S.; no `intent`, `owner semantic`, `strength required`, route channel, bend point, line style, or label policy in relation examples.
- `connect` may remain until Story 1.3 if required by existing tests, but new M39 examples should use relation verbs.
- Any generated alias for grouped targets must be deterministic, stable, and traceable to the one authored relationship.
- Unknown relation diagnostics belong to domain validation, not parser syntax. Parser should accept generic identifiers so future domains can define their own vocabulary.
- Do not add dependencies.
- Do not put milestone names, `Proof`, `Demo`, `Sample`, `V0`, or `V1` in production names.

### Test Requirements

Run Gradle commands sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
```

Run Tree-sitter tests if grammar/highlights/generated files changed:

```powershell
yarn --cwd ide/tree-sitter-athena test
```

Final audits:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E1 Story 1.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-1 through FR-8, FR-22]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/addendum.md` - Language Rule and Pre-Mortem Correction]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-1 through AD-4]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]
- [Source: `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4` - current `connectDecl`, `connectGroupEdge`, `TO`, `ident`]
- [Source: `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt` - electrical domain lowering]
- [Source: `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt` - electrical diagnostics]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Started implementation from sprint status; baseline commit preserved.
- Focused parser test passed: `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests "com.engineeringood.athena.language.AthenaLanguageParserTest"`.
- Focused compiler test passed: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.DomainRelationVerbCompilationTest"`.
- Tree-sitter passed: 22 corpus parses, 64 script tests.
- Full affected Gradle suites passed sequentially: `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:runtime:test`, `:ide:lsp:test`.
- Audits passed: source-set hygiene, encoding, `git diff --check`.
- Self-review found grouped unknown relations could leave an empty network shell; fixed in generic lowering and covered by `DomainRelationVerbCompilationTest`.
- Post-review verification passed sequentially: focused `DomainRelationVerbCompilationTest`, full `:kernel:compiler:test`, `:kernel:runtime:test`, `:ide:lsp:test`, source-set hygiene, encoding, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added syntax-owned `RelationDeclaration` for natural domain relation source such as `power A.out to B.in`.
- Kept electrical relation words domain-owned through plugin schema; kernel grammar accepts generic identifiers only.
- Lowered known electrical relations to canonical engineering connections with `relation.kind` and deterministic grouped member aliases/networks.
- Added IDE formatter, document symbols, navigation ranges, and Tree-sitter parse/highlight support for relation declarations.
- Hardened grouped relation network lowering so networks are emitted only when every domain-derived member connection exists.

### File List

- `_bmad-output/implementation-artifacts/m39/1-2-add-domain-connection-verbs.md`
- `_bmad-output/implementation-artifacts/m39/sprint-status.yaml`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeContracts.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/connect.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinker.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DomainRelationVerbCompilationTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSchemaModel.kt`

### Change Log

- 2026-08-01: Created story from M39 sprint plan, PRD, architecture spine, addendum, and Story 1.1 review outcome.
- 2026-08-01: Implemented domain relation verbs and moved story to review after full affected verification.
- 2026-08-01: Closed self-review finding for unknown grouped relations and marked story done after fresh verification.
