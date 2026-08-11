---
story: 1.1
epic: 1
title: Author Typed Binary Engineering Connections
status: done
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-10
---

# Story 1.1: Author Typed Binary Engineering Connections

Status: done

## Story

As an engineer,
I want concise source syntax for typed binary connections,
so that connectivity exists as engineering meaning before any line is drawn.

## Acceptance Criteria

1. **Given** two resolved Engineering Ports and a supported binary connection phrase **When** Athena
   parses and lowers the source **Then** `EngineeringDocument` contains one stable
   `EngineeringConnection` with exactly two typed `ConnectionEndpoint` values, explicit `SOURCE`/`SINK`
   or `PASS` endpoint roles, a closed M46 `ConnectionKind`, authored properties, and provenance **And**
   Port direction remains a separate device-local fact.
2. **Given** graphic proximity, an SVG anchor, or an arbitrary `EngineeringRelationship` without explicit
   connection meaning **When** projection compiles **Then** no `EngineeringConnection` is created **And**
   this story does not add a compatibility bridge; complete generic relationship-to-route deletion is
   owned by Story 1.4.
3. **Given** malformed or unresolved source **When** diagnostics publish through LSP **Then** each
   diagnostic names exact connection/Port subject and plain-language correction **And** Tree-sitter
   highlighting, outline, formatting, and document symbols recognize current syntax.

## Tasks / Subtasks

- [x] Define source-owned binary connection model (AC: 1)
  - [x] Add `EngineeringConnection`, `ConnectionEndpoint`, endpoint-role enum, closed M46 kind enum,
        typed property/provenance references in `kernel/engineering-model`.
  - [x] Enforce exactly two endpoints, stable identity inputs, nonblank roles/paths, and no geometry,
        DOM, Konva, viewport, or SVG fields.
  - [x] Add `connections` to `EngineeringDocument` without changing `nets` authority beyond the empty
        future collection needed by current model construction.
- [x] Admit concise typed connection syntax (AC: 1, 3)
  - [x] Update ANTLR grammar and syntax AST/parser adapter consistently.
  - [x] Use human-first form `connect <kind> <source-port> to <target-port>` (with `->` accepted only
        where current grammar already treats it as the same separator); no `intent {}`, coordinates,
        route points, style, IR digest, or renderer fields in source.
  - [x] Update Tree-sitter grammar, highlights, corpus, and generated/runtime grammar asset as required by
        existing build scripts.
- [x] Lower and validate binary connections (AC: 1, 2, 3)
  - [x] Resolve authored Port paths against canonical entity/function Port identities; derive stable
        source-based connection identity and `SOURCE`/`SINK` role independently from Port direction.
  - [x] Lower only explicit typed connection syntax into `EngineeringDocument.connections`.
  - [x] Keep generic `EngineeringRelationship` lowering for non-connection meaning until Story 1.4;
        never treat proximity, package anchor, or arbitrary relationship as connection truth.
  - [x] Add fail-closed unresolved endpoint, unsupported kind, duplicate/invalid endpoint, and illegal
        role/direction diagnostics with exact source provenance. Do not publish partial connection state.
- [x] Preserve IDE authoring contract (AC: 3)
  - [x] Update formatter, document symbols/outline, semantic tokens, and LSP diagnostics for new syntax.
  - [x] Keep internal ids, AST links, source paths, and Port ids out of canvas labels; they remain trace/
        interaction metadata only.
- [x] Author tests before implementation, then red-green-refactor (AC: 1-3)
  - [x] Model tests: exact-two endpoint invariant, stable identity, role/direction separation, no geometry.
  - [x] Language tests: valid phrases, malformed phrases, `to`/`->` parity, provenance spans, formatter,
        Tree-sitter corpus/highlight/outline.
  - [x] Compiler/validation tests: resolved endpoints, stable recompile identity, unsupported/unresolved
        fail-closed diagnostics, explicit connection versus generic relationship non-conversion.
  - [x] LSP tests: human diagnostic subject/correction, document symbols, semantic tokens, formatting.
  - [x] Run Gradle tasks strictly sequentially; never run two `gradlew` processes concurrently.

## Dev Notes

### Authority And Scope

M46 chain is:

```text
Athena source
  -> Engineering Connection / Engineering Net
  -> canonical Connection IR
  -> Connection Projection / Route Plan
  -> SceneConnection
  -> Theia/Konva/SVG
```

This story establishes only the first source-owned binary connection contract. Connection IR belongs Story
1.3. Nets/specification precedence belongs Story 1.2. Projection replacement belongs Story 1.4. Do not
invent placeholder IR, route, topology, or renderer contracts here.

Source owns engineering meaning: endpoints, roles, kind, authored properties, and provenance. Package assets
may constrain compatibility later; SVG is geometry only. Canvas line intersections never create connectivity.

### Existing Code To Reuse/Update

- `kernel/engineering-model/.../EngineeringModel.kt`: current `EngineeringDocument` root; add first-class
  connection collection while preserving entity/function/port/relationship fields needed by current pass.
- `kernel/engineering-model/.../EngineeringAnatomyModels.kt`: current `EngineeringPortDirection`; do not
  conflate it with `ConnectionEndpointRole`.
- `kernel/engineering-model/.../EngineeringRelationshipModels.kt`: generic relationship remains for
  non-connection semantics during this story; no new route lowering from it.
- `kernel/language/.../AthenaLanguageModel.kt`: current `RelationDeclaration` and declaration hierarchy;
  introduce the explicit typed connection AST contract consistently with ANTLR adapter.
- `kernel/language/.../antlr/Athena.g4` and `AthenaAntlrParseAdapter.kt`: parser authority and AST adapter.
- `kernel/compiler/.../EngineeringIrLowerer.kt`: current source AST lowering and stable identity patterns;
  resolve explicit connection Ports here, not from graphic or package geometry.
- `extensions/domain-electrical/.../ElectricalRuntimeValidation.kt`: current relation direction/flow
  validation; migrate binary connection validation to typed model without making generic relationships route
  truth.
- `ide/tree-sitter-athena/grammar.js`, `queries/highlights.scm`, `test/corpus/connect.txt`: editor syntax
  contract. Update corpus intentionally; remove retired `intent {}` fixture rather than preserving it.
- `ide/lsp/.../AthenaLanguageFeatures.kt`, formatter, and LSP tests: document symbols, semantic tokens,
  plain diagnostics, formatting.

### Architectural Guardrails

- No backward compatibility aliases, fallback parser, deprecated route bridge, `.elmt`/HTML/XML runtime
  path, or milestone-named production class.
- No coordinates, layout, style, SVG, DOM, Konva, viewport, mouse, or digest fields in authored connection
  syntax or engineering model.
- Stable identity derives from source unit, kind, and canonical authored endpoint paths; parser offsets and
  runtime object identity cannot be identity authority.
- Endpoint role (`SOURCE`, `SINK`, `PASS`) is explicit/derived connection meaning; Port direction
  (`INPUT`, `OUTPUT`, `BIDIRECTIONAL`) remains independent validation input.
- Invalid source fails closed. Accepted publication remains unchanged under existing STALE/UNAVAILABLE
  rules. Diagnostics name exact subject, problem, correction in human language.
- Do not delete `ProjectionConnection`/`SpatialRoute`/`SceneRoute` in this story unless compiler cannot
  compile without the deletion; Story 1.4 owns complete replacement and hygiene proof. Do not introduce
  a second path to preserve them.

### Technical Stack

Kotlin 2.4.0, ANTLR 4.13.2, LSP4J 0.23.1, Tree-sitter grammar in `ide/tree-sitter-athena`, Theia 1.73.1,
Konva 10.3.0, TypeScript 5.9.2, Node >=22, Yarn 1.22.22. No new runtime dependency for this story.

### Verification

Run sequentially, waiting for each command:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:validation:test
Set-Location ide
yarn test
```

Run encoding audit after text edits and source-set hygiene after production cleanup:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md` - Story 1.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md` - FR-1, FR-3, FR-6]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/addendum.md` - Four Authority Layers]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md` - AD-1 through AD-6]
- [Source: `draft/20260803-confuse/connections-details.md`]
- [Source: `draft/eplan-help/index/connectionbrowsergui.md`]
- [Source: `AGENTS.md` - Pre-1.0 Architecture, Human-First Language, Visual Golden Rule]
- [Source: `_bmad-output/implementation-artifacts/m45/M45-RETROSPECTIVE.md` - carry-forward laws]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- RED: `EngineeringConnectionModelsTest`, `TypedConnectionSyntaxTest`, and
  `EngineeringConnectionLoweringTest` initially failed because model/parser/lowering contracts were absent.
- RED: unsupported `connect magic ...` accepted until parser adapter added closed-kind validation.
- RED: LSP outline exposed receiver shadowing in `DocumentSymbol.kind`; fixed with explicit declaration receiver.
- REVIEW: unresolved Endpoints were initially retained as partial connection facts; source validation now
  diagnoses them and lowering admits only fully resolved binary connections.
- REVIEW: generic Engineering Relationships still projected as route truth; removed that authority path.
- REVIEW: authored property blocks, all-six-kind coverage, exact diagnostics, and Tree-sitter/LSP token
  parity were added before closure.
- Root `ide/yarn test` cannot run because `ide/package.json` has no `test` script. Applicable
  `ide/tree-sitter-athena/yarn test` passed.

### Completion Notes List

Ultimate BMad story context created from active M46 PRD, architecture, epics, implementation plan, M45
closure/retrospective, EPLAN connection lessons, repository instructions, CodeGraph blast-radius analysis,
and current parser/compiler/editor contracts.

- Added source-owned typed binary Engineering Connection model and `EngineeringDocument.connections`.
- Added human-first `connect <kind> <source> to <target>` syntax with `to`/`->` parity and six closed kinds.
- Added optional typed authored property blocks and canonical `EngineeringProperty` lowering.
- Lowering derives stable source-based identity and resolved Port endpoints without renderer fields.
- Validation rejects unresolved endpoints, duplicate connections, and illegal source/sink Port directions;
  unresolved source publishes no partial Engineering Connection.
- Generic Engineering Relationships remain separate and no longer synthesize Engineering Connections or
  projection routes.
- Formatter, outline, semantic tokens, Tree-sitter grammar/highlights/corpus, generated parser, and WASM updated.
- Passing verification: `:kernel:engineering-model:test`, `:kernel:language:test`,
  `:kernel:compiler:test`, `:kernel:validation:test`, `:ide:lsp:test`, and Tree-sitter `yarn test`.

### File List

- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/TypedConnectionLanguageFeaturesTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/connect.txt`
- `ide/tree-sitter-athena/test/corpus/installation.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringAnatomySourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectionLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformationTest.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringConnectionModels.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringConnectionModelsTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/TypedConnectionSyntaxTest.kt`
- `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`

### Change Log

- 2026-08-10: Created Story 1.1 context; status `ready-for-dev`.
- 2026-08-10: Implemented and verified typed binary Engineering Connections; status `review`.
- 2026-08-10: Addressed adversarial review findings, reran full sequential verification and audits, and
  closed Story 1.1; status `done`.

## Senior Developer Review (AI)

**Outcome:** Approve

**Review Date:** 2026-08-10

### Action Items

- [x] [HIGH] Reject repeated Endpoint Port paths in parser and canonical model.
- [x] [HIGH] Fail closed for unresolved Endpoint paths without partial `EngineeringConnection` publication.
- [x] [HIGH] Remove generic `EngineeringRelationship` to projection-route authority.
- [x] [HIGH] Publish exact Port subject, problem, and correction through LSP-visible diagnostics.
- [x] [MEDIUM] Admit and lower authored typed Connection properties.
- [x] [MEDIUM] Verify all six closed M46 Connection Kinds.
- [x] [MEDIUM] Preserve `to`/`->` Tree-sitter and semantic-token parity.
- [x] [MEDIUM] Enforce nonblank Connection identity and authored Port path segments.

### Verification Evidence

- `:kernel:engineering-model:test` - passed.
- `:kernel:language:test` - passed.
- `:kernel:compiler:test` - passed.
- `:kernel:validation:test` - passed.
- `:ide:lsp:test` - passed.
- `ide/tree-sitter-athena/yarn test` - 22/22 corpus parses passed; WASM rebuilt.
- `tools/encoding-audit.ps1` - passed.
- `tools/source-set-hygiene-audit.ps1` - passed.
