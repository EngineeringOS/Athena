# M28 Structured Assertion Inventory

Date: 2026-07-21

## Purpose

M28 must be diagnosable through structured tests instead of visual guessing. This inventory maps the
required seams to executable assertions and records stale assertion decisions.

## Coverage By Seam

| Seam | Assertion Location | What It Proves |
|---|---|---|
| ANTLR4 parser | `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt` | Nested device-owned ports parse as first-class anatomy with owner-qualified canonical name. |
| Tree-sitter | `ide/tree-sitter-athena/test/corpus/port.txt` | Nested port syntax is accepted by editor grammar/highlighting. |
| Compiler lowering | `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM28NestedPortCompilerTest.kt` | Nested ports lower to `port:Device.port` identity and preserve source provenance. |
| Semantic index | `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexerTest.kt` | Nested and legacy top-level ports collide on the same canonical identity instead of creating duplicate truth. |
| M28 sample repository | `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM28SampleProjectCompilerTest.kt` | Checked-in M28 sample compiles and links without ambiguous authored references. |
| Authoring contract | `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringIntentContractTest.kt` | `SemanticRelationshipIntent` is the root authoring contract and legacy `ConnectPortsIntent` lifts into it. |
| Relationship validation | `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/SemanticRelationshipCompatibilityValidatorTest.kt` | Electrical specialization validates direction, signal, duplicate, ownership, dirty, and invalid source gates without source mutation. |
| LSP mutation authority | `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt` | Generic semantic relationship accept returns a governed `.athena` source edit and refreshes projection state. |
| Product-path authoring smoke | `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM28ProductAuthoringSmokeTest.kt` | M28 sample accepts one valid relationship, rejects two invalid attempts without source mutation, and blocks invalid accept at the backend edit gate. |
| Frontend relationship mode | `ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs` | Selection uses projection facts, rejects DOM-only subjects, and keeps authoring preview transient. |
| Product wiring | `ide/theia-frontend/scripts/athena-m28-product-smoke-wiring.test.mjs` | M28 Theia scripts open the sample and frontend emits generic semantic relationship payloads from projection facts. |

## Stale Assertion Decisions

| Item | Decision | Reason |
|---|---|---|
| Legacy `connect-ports` protocol tests | Retain | `ConnectPortsIntent` remains a compatibility shape; M28 tests must prove it is not the architectural root. |
| Guided connection model tests | Retain | They cover pre-M28 command compatibility and should be retired only after M29 interaction IR absorbs the old path. |
| M28 docs and PRD language | Updated/kept | M28 planning explicitly names `SemanticRelationshipIntent` and treats `.athena` serialization as today's canonical persistence implementation. |

## Missing Assertion Closed During Epic 4

The backend source-edit path now validates electrical semantic relationship persistence before
serializing a `connect` statement. This prevents a malicious or stale client from accepting an
invalid relationship and receiving a source edit.
