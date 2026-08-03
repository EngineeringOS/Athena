---
story_key: 2-1-attach-typed-external-evidence
epic: m37-e2
requirements: [FR-8, FR-9, FR-10]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.1: Attach Typed External Evidence

Status: review

## Story

As an engineering package author,
I want to attach external references to Athena-owned connectivity facts,
so that standards and classifications remain inspectable evidence without becoming runtime truth.

## Acceptance Criteria

1. Athena source admits typed External Evidence Mapping declarations for an Engineering Connectivity Contract, Interface, Port, Connection Intent, and route policy subject.
2. Each mapping carries namespace, reference, subject, source span, and provenance through parser, semantic lowering, connectivity compilation, diagnostics, and proof-ready payloads.
3. Supported M37 evidence namespaces include one IEC citation namespace and one neutral classification namespace; AML/XML/ECLASS parsers or runtime resolvers are not implemented.
4. Unknown namespace, invalid reference shape, duplicate mapping, and invalid subject fail with typed diagnostics and correct source spans.
5. Evidence cannot create Ports, Interfaces, Anchors, Connection Intent, compatibility, RouteFacts, or projection truth by itself.
6. Future importer-boundary documentation explains lowering external standards into Athena source/facts, not copying external schemas into kernel authority.
7. Focused parser/compiler/LSP tests, source-set hygiene audit, encoding audit, and diff-check gates pass sequentially.

## Tasks / Subtasks

- [x] Add red tests for external evidence syntax and validation (AC: 1, 2, 3, 4, 5)
  - [x] Add parser/AST tests for evidence declarations on contract, Interface, Port, Connection Intent, and route policy subject shapes.
  - [x] Add compiler/connectivity tests proving evidence references carry namespace, reference, subject, source span, and provenance.
  - [x] Add negative tests for unknown namespace, invalid reference, duplicate mapping, invalid subject, and evidence-only fact creation.
- [x] Extend language and semantic lowering in place (AC: 1, 2, 5)
  - [x] Add typed syntax without XML, raw external schema, or a second authority model.
  - [x] Lower mappings into canonical Engineering IR properties or a typed model already consumed by connectivity compilation.
  - [x] Keep evidence declarative and non-creative: no Port, Interface, Anchor, intent, compatibility, RouteFact, or projection fact is generated from evidence alone.
- [x] Extend Engineering Connectivity Contract evidence model (AC: 2, 3, 4)
  - [x] Add typed External Evidence Mapping contracts with subject, namespace, reference, owner/provenance where needed.
  - [x] Validate supported namespaces and reference shapes.
  - [x] Surface diagnostics through compiler and LSP under `connectivity.evidence.*`.
- [x] Document external importer boundary (AC: 3, 6)
  - [x] Add M37 implementation note explaining AML/XML/ECLASS remain import/evidence only.
  - [x] Record that future importers lower into Athena source/facts before product use.
- [x] Verify and record gates (AC: 7)
  - [x] Run focused language, connection-model/compiler, and LSP tests sequentially.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source stays SSOT. External standards are evidence, not runtime truth.
- XML is out of product path. AutomationML and ECLASS parser/resolver work is deferred.
- Do not add full ECS, catalog, manufacturer lifecycle, BOM/procurement, datasheets, or replacement facts.
- Direct refactor only. No compatibility shims, no stale external authority, no milestone-named production classes.

### Current Code Intelligence

- `EngineeringConnectivityContracts.kt` already has `EngineeringConnectivityEvidenceReference` on contracts and `EngineeringConnectivityNetworkCompatibilityEvidenceContract`; Story 2.1 should evolve this into typed evidence mappings rather than adding a parallel model.
- `EngineeringModel.kt` carries `EngineeringProperty` for lowered source facts. If evidence syntax is property-backed first, keep it typed at the connectivity boundary.
- `ElectricalRuntimeLowering.kt` and `EngineeringIrLowerer.kt` are the likely source-to-IR paths for new authored declarations.
- `Athena.g4`, `AthenaLanguageModel.kt`, `AthenaAntlrParseAdapter.kt`, Tree-sitter, and LSP diagnostics must evolve together if a new syntax surface is added.
- Story 1.4 added route intent influence; do not let evidence alter route facts or intent behavior in this story.

### Suggested Source Shape

Exact syntax may adapt to parser fit, but it must stay typed and declarative:

```athena
evidence Drive.powerInput.L1 {
  namespace iec
  reference "IEC:60204-1:protective-conductors"
  subject port Drive.L1
}

evidence Drive {
  namespace classification
  reference "neutral:drive"
  subject contract Drive
}
```

### TDD And Verification

- RED first: parser/compiler/LSP tests fail before production edits.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 2.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-8, FR-9, FR-10, NFR-1, NFR-2, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - External Standards Boundary, Deferred To Later Milestones]
- [Source: `_bmad-output/implementation-artifacts/m37/1-4-lower-intent-into-traceable-route-facts.md` - Previous route intent trace boundary]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-30: Ran focused and full language/connectivity/compiler/LSP gates sequentially.
- 2026-07-30: Ran tree-sitter `yarn test`.
- 2026-07-30: Ran source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added typed `evidence` declarations to Athena language, parser model, tree-sitter grammar, semantic lowering, connectivity contracts, compiler diagnostics, and LSP diagnostics.
- Evidence mappings carry namespace, reference, subject, source span, and provenance into proof-ready connectivity payloads.
- Supported M37 evidence namespaces are `iec` with `IEC:` references and `classification` with `neutral:` references.
- Added diagnostics under `connectivity.evidence.*` for unknown namespace, invalid reference shape, duplicate mapping, and invalid subject.
- Preserved Athena source SSOT: evidence is non-authoritative and cannot create ports, interfaces, anchors, intents, compatibility, route facts, or projection truth.
- Added M37 external importer boundary note: AML/XML/ECLASS remain import/evidence inputs only and must lower into Athena-owned source/facts before product use.
- Verification passed sequentially:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.EngineeringConnectivityCompilationTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `yarn test` in `ide/tree-sitter-athena`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### File List

- _bmad-output/implementation-artifacts/m37/2-1-attach-typed-external-evidence.md
- _bmad-output/implementation-artifacts/m37/external-importer-boundary.md
- extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- ide/tree-sitter-athena/grammar.js
- ide/tree-sitter-athena/queries/highlights.scm
- ide/tree-sitter-athena/src/grammar.json
- ide/tree-sitter-athena/src/node-types.json
- ide/tree-sitter-athena/src/parser.c
- ide/tree-sitter-athena/test/corpus/connect.txt
- ide/tree-sitter-athena/tree-sitter-athena.wasm
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrLowerer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrLoweringTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt
- kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt
- kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContractCompilerTest.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt
- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 2.1 from finalized M37 PRD, addendum, epics, and active M37 story learnings.
- 2026-07-30: Implemented typed external evidence declarations, lowering, connectivity contracts, diagnostics, LSP/tree-sitter support, importer-boundary note, and sequential verification gates.
