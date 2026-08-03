---
story_key: 2-2-select-a-typed-projection-policy
epic: m37-e2
requirements: [FR-22, FR-24]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.2: Select A Typed Projection Policy

Status: review

## Story

As an engineering author,
I want to select a projection through typed Athena policy,
so that target view, layout, presentation, route policy, and proof obligations are explicit and traceable.

## Acceptance Criteria

1. Athena source admits typed Projection Policy declarations that resolve target surface, layout strategy, Drawing Standard Profile, Route Quality Policy, and proof obligations with stable identity and source provenance.
2. Parser, AST, semantic lowering, tree-sitter, formatter/highlighting, compiler, and LSP diagnostics evolve together for the selected syntax.
3. The active professional drawing compiler receives the selected policy from source/compiler input instead of hardcoding schematic projection context or control-drawing view selection.
4. Projection Policy cannot create or redefine Ports, Connections, Connection Intent, compatibility, Anchors, External Evidence Mapping, or other engineering truth.
5. Invalid target surface, missing profile, missing route-quality policy, duplicate policy identity, incompatible policy fields, and projection-specific engineering facts fail with typed compiler/LSP diagnostics and source spans.
6. Projection policy identity and selection evidence appear in proof-ready compiler payloads and presentation transport.
7. Focused parser/compiler/LSP/tree-sitter tests, source-set hygiene audit, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add red tests for projection policy syntax and validation (AC: 1, 2, 4, 5)
  - [x] Add parser/AST tests for valid policy target/layout/profile/route-policy/proof-obligation declarations.
  - [x] Add negative parser/compiler tests for invalid target, missing profile, missing route-quality policy, duplicate policy identity, incompatible fields, and projection-owned engineering truth.
  - [x] Add LSP diagnostic tests under `projection.policy.*` with source spans.
- [x] Extend Athena language and semantic lowering in place (AC: 1, 2, 4)
  - [x] Add typed syntax without XML, renderer payload truth, or projection-specific source worlds.
  - [x] Lower policy declarations into canonical Engineering IR properties or a typed projection model consumed by the compiler.
  - [x] Preserve non-creative rule: policy selection may choose projection behavior but cannot create ports, connections, intent, compatibility, anchors, or evidence.
- [x] Add typed Projection Policy compiler model (AC: 1, 3, 5, 6)
  - [x] Model stable policy identity, target surface, layout strategy, drawing profile, route-quality policy, proof obligations, source span, and provenance.
  - [x] Validate supported M37 target surfaces and strategy/profile compatibility.
  - [x] Emit deterministic selection evidence and diagnostics under `projection.policy.*`.
- [x] Wire selected policy into active professional drawing path (AC: 3, 6)
  - [x] Replace hardcoded `ProjectionContextId("schematic")` in professional material resolution with selected policy context.
  - [x] Replace hardcoded `controlDrawingView()` selection with selected policy view/surface evidence.
  - [x] Keep Cabinet/professional drawing as policy consumers of same semantic source, not separate truth stores.
- [x] Update tree-sitter, formatter/highlighting, and protocol payloads (AC: 2, 6)
  - [x] Add grammar/corpus/highlighting coverage for policy declarations.
  - [x] Include policy selection evidence in compiler/presentation payloads without raw planner, XML, SVG markup, or renderer-owned facts.
- [x] Verify and record gates (AC: 7)
  - [x] Run focused language, compiler, LSP, tree-sitter tests sequentially.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source remains SSOT. Projection Policy selects target projection behavior only.
- Projection Policy is compiler input, similar to backend/materialized-view selection. It never owns engineering truth.
- No compatibility shim. Athena is pre-public: remove or directly refactor stale hardcoded projection paths.
- No XML/AML/ECLASS/runtime catalog authority. No renderer inference. No second representation IR.
- Do not implement full Drawing Standard Profile or Route Quality Policy vocabulary here; Story 2.2 must validate references and carry selection evidence. Full grammar lands in E3/E4.

### Current Code Intelligence

- CodeGraph found `AthenaProfessionalDrawingCompiler.compile` currently calls `materialResolver.resolve(... projectionContext = ProjectionContextId("schematic"))` and builds `PresentationDocument(view = controlDrawingView(), ...)`. These are direct refactor targets for this story.
- `AthenaProfessionalDrawingRequest`, `AthenaProfessionalDrawingPolicy`, `AthenaProfessionalDrawingEvidence`, and `AthenaProfessionalDrawingResult` live in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingModels.kt`; evolve these by responsibility, not milestone naming.
- Existing `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionPolicyModel.kt` has `DocumentProjectionPolicy`; reuse ideas only if they fit the current compiler path. Do not create another parallel authority model.
- Story 2.1 added typed `evidence` syntax and diagnostics. Preserve evidence as non-authoritative and include it in proof payloads only through Athena-owned source facts.
- Story 1.4 added authored route intent influence. Projection Policy must not infer intent from endpoints, geometry, or renderer state.

### Suggested Source Shape

Exact syntax may adapt to parser fit, but it must stay typed and declarative:

```athena
projection ControlDrawingProjection {
  target professional-connection-drawing
  layout orthogonal-grid
  drawingProfile ControlDrawingIEC
  routeQuality ControlDrawingRouteQuality
  proof exact-endpoints
  proof source-trace
}
```

Forbidden:

```athena
projection ControlDrawingProjection {
  port Drive.L1 input
  connection Drive.L1 -> Motor.U
  intent power
  evidence iec "IEC:..."
}
```

Those facts belong to Engineering Connectivity Contracts, Connections, Connection Intent, and External Evidence Mapping.

### Expected Diagnostics

- `projection.policy.target.unknown`
- `projection.policy.layout.unknown`
- `projection.policy.profile.missing`
- `projection.policy.route-quality.missing`
- `projection.policy.duplicate`
- `projection.policy.engineering-truth.forbidden`

### TDD And Verification

- RED first: parser/compiler/LSP/tree-sitter tests fail before production edits.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `yarn test` from `ide/tree-sitter-athena`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 2.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-22, FR-24, NFR-1, NFR-2, NFR-5, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Non-Negotiable Source Principle, Direct Refactor Targets]
- [Source: `_bmad-output/implementation-artifacts/m37/2-1-attach-typed-external-evidence.md` - Previous story evidence boundary]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-30: Added RED parser/compiler/LSP/tree-sitter coverage for typed Projection Policy declarations and forbidden engineering truth.
- 2026-07-30: Implemented ANTLR/AST/lowering/Engineering IR support for `projection` declarations.
- 2026-07-30: Added `AthenaProjectionPolicyCompiler` diagnostics under `projection.policy.*`.
- 2026-07-30: Wired selected policy into professional drawing material context and presentation view evidence.
- 2026-07-30: Fixed regression where target surface was incorrectly used as presentation view id; `professional-connection-drawing` now selects compiler behavior while emitted view remains `schematic` / `Control Drawing`.
- 2026-07-30: Verification pass: `:kernel:language:test`, `:kernel:compiler:test`, `:ide:lsp:test`, tree-sitter `yarn test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Projection Policy syntax now parses, lowers, highlights, formats, validates, and reaches professional drawing compilation as typed compiler input.
- Projection Policy cannot author Ports, Connections, Connection Intent, Anchors, External Evidence, or other engineering truth.
- Professional drawing compilation consumes selected projection policy for material context and proof/view evidence without turning target surface into renderer-owned view identity.

### File List

- _bmad-output/implementation-artifacts/m37/2-2-select-a-typed-projection-policy.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- examples/m34/professional-control-drawing/src/com/engineeringood/m34/professional/01-control-drawing.athena
- extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- ide/tree-sitter-athena/grammar.js
- ide/tree-sitter-athena/queries/highlights.scm
- ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs
- ide/tree-sitter-athena/src/grammar.json
- ide/tree-sitter-athena/src/node-types.json
- ide/tree-sitter-athena/src/parser.c
- ide/tree-sitter-athena/test/corpus/connect.txt
- ide/tree-sitter-athena/test/fixtures/m37-projection-policy.athena
- ide/tree-sitter-athena/tree-sitter-athena.wasm
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingModels.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProjectionPolicyCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt
- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt
- kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/SemanticValidationModel.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 2.2 from finalized M37 PRD, addendum, epics, and Story 2.1 learnings.
- 2026-07-30: Implemented typed Projection Policy syntax, lowering, compiler diagnostics, LSP/tree-sitter support, selected professional drawing policy wiring, and regression fix for stable emitted view identity.
