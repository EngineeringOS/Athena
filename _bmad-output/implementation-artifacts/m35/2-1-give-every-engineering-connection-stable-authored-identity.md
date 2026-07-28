---
baseline_commit: c007cd140cd0fcaffdc5ccb66bf2f917d69294cf
---

# Story 2.1: Give Every Engineering Connection Stable Authored Identity

Status: review

## Story

As an engineer or AI author,
I want every connection to have a stable source alias,
so that physical installation intent and future editing can reference one connection without endpoint or group guesses.

## Acceptance Criteria

1. Given grouped and ungrouped connection source, when M35 grammar and AST compile it, then every connection requires a source-unit-unique alias and `EngineeringConnectionId` is `(SourceUnitId, connectionAlias)`, and group names remain organizational and never identify or merge connections.
2. Given alias-free current grammar, AST, endpoint-plus-ordinal identity, examples, and fixtures, when the migration story completes, then all repository sources/tests are migrated to required aliases and the old forms/identity are deleted, and there is no compatibility parser or adapter.
3. Given an alias reference in governed source, when semantic linking runs, then it resolves exactly one connection alias in the same `SourceUnitId`, and missing, duplicate, group-name, or cross-source references fail with stable source-spanned diagnostics.
4. Given aliased syntax in normal, invalid, and incomplete files, when ANTLR4, semantic lowering, formatter, LSP, Tree-sitter, highlighting, and parser parity run, then every frontend and generated artifact reflects the same required alias contract.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and alias-free source, parser branches, generated grammar artifacts, fixtures, ids, and compatibility names are purged.

## Tasks / Subtasks

- [x] Add RED tests for required connection aliases and stable identity (AC: 1, 2, 3, 4)
  - [x] Cover grouped and ungrouped valid connections with explicit aliases.
  - [x] Cover missing alias syntax rejection, duplicate aliases, group-name misuse, missing references, and cross-source references.
  - [x] Cover `EngineeringConnectionId` construction from `SourceUnitId + alias`, not endpoints or ordinals.
  - [x] Cover ANTLR4, formatter, LSP, Tree-sitter/highlighting, and parser parity surfaces.
- [x] Implement required alias syntax and source model updates (AC: 1, 2, 4)
  - [x] Update ANTLR4 grammar and language models so every connection carries a required alias token and source span.
  - [x] Remove parser branches and compatibility constructors for connections without authored aliases.
  - [x] Update formatter and language feature surfaces to keep aliased connection syntax canonical.
- [x] Update semantic lowering and diagnostics (AC: 1, 3)
  - [x] Lower each connection to a source-unit-scoped `EngineeringConnectionId`.
  - [x] Make group declarations organizational only; never use group names as connection ids.
  - [x] Emit stable source-spanned diagnostics for duplicate/missing/cross-source aliases.
- [x] Migrate repository sources, examples, fixtures, and generated artifacts (AC: 2, 4)
  - [x] Update all active `.athena` source and tests to required aliases.
  - [x] Update Tree-sitter grammar/corpus/highlighting and parser parity corpus together.
  - [x] Delete or rewrite no-alias fixtures instead of preserving compatibility.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched and adjacent paths for no-alias grammar, endpoint-plus-ordinal ids, stale examples, generated artifacts, and compatibility names.
  - [x] Run sequential verification and record RED/GREEN evidence.
  - [x] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 2.1 owns stable authored identity for engineering connections. It does not introduce Cabinet installation syntax, physical routing geometry, route channels, selection trace, or graphical editing. Later Epic 2/3 stories may reference aliases, but this story only makes connection identity stable, source-scoped, and universally parsed/lowered.

### Architecture Requirements

- Athena source remains the only authority for connection identity.
- Every connection must have a source-unit-unique alias. Alias-free connection syntax is removed, not preserved.
- `EngineeringConnectionId` must be derived from `SourceUnitId` and the connection alias, never from endpoints, source order, group names, or generated ordinals.
- Connection group names are organizational only. They may not merge, identify, or substitute for individual connection aliases.
- Alias references resolve only inside the same `SourceUnitId` unless a later story explicitly adds a typed cross-source import/reference model.
- ANTLR4 remains parser authority. Tree-sitter, formatter, LSP, highlighting, examples, and parser parity must move together.
- Because Athena is unreleased, stale grammar branches, examples, and compatibility adapters should be deleted instead of wrapped.

### Previous Story Intelligence

- Epic 1 established package/resource/lock/SVG authority. Do not disturb package-local resource semantics while migrating `.athena` sources.
- Story 1.5 created `examples/m35/package-platform-proof` as a package proof only; it has no connections and should not be forced into physical routing work.
- Existing examples were already migrated toward package-path hierarchy in earlier M35 work; preserve that discipline when editing source fixtures.

### Likely Code Areas

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- `kernel/connection-model/src/main/kotlin/...` if connection value objects are separated there.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/test/corpus/*`
- active `examples/**/src/**/*.athena` connection fixtures and parser parity corpus.

### Testing Requirements

- Follow RED/GREEN. Write failing tests before production code.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification for this story:
  - targeted language/parser tests;
  - targeted compiler semantic lowering tests;
  - targeted LSP diagnostics/completion/token tests;
  - targeted Tree-sitter/parser parity tests if JS tests are present;
  - `encoding-audit.ps1` after text edits;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-28, FR-35, FR-41..FR-42.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-12, AD-13, AD-14, AD-17.
- `_bmad-output/implementation-artifacts/m35/1-5-prove-portable-standard-and-vendor-packages-end-to-end.md` - previous story.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T02:33:29+08:00 - Completed migration to required authored connection aliases across active examples, parser fixtures, semantic lowering expectations, LSP/runtime projections, and model-contract tests.
- 2026-07-28T02:33:29+08:00 - Removed production relationship preview compatibility API and endpoint-owner inference from projection derivation.
- 2026-07-28T02:33:29+08:00 - Fixed stale M32/M31/M27 package-path test references exposed by strict repository hierarchy validation.
- RED evidence observed during implementation: compiler/runtime/LSP/package-runtime tests failed on stale endpoint IDs, stale source paths, and cursor positions after required aliases shifted syntax columns; these failures drove the fixture and expectation updates.
- GREEN evidence:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests com.engineeringood.athena.language.AthenaLanguageParserTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest --tests com.engineeringood.athena.compiler.AthenaM17ParserParityProofTest --tests com.engineeringood.athena.compiler.AthenaParserContinuityTest --tests com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlannerTest --tests com.engineeringood.athena.compiler.PresentationModelDeriverTest --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticCapabilityProvenanceProjectorTest --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticReferenceLinkerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaEngineeringGraphProjectionTest --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest --tests com.engineeringood.athena.runtime.AthenaAuthoringSessionRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaSourceMutationRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaM31SampleAuthoringProofTest --tests com.engineeringood.athena.runtime.GovernedRelationshipPreviewServiceTest --tests com.engineeringood.athena.runtime.AthenaAiProposalRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaCommandHistoryTest --tests com.engineeringood.athena.runtime.AthenaCommandRuntimeTest --tests com.engineeringood.athena.runtime.AthenaPluginRuntimeServicesTest --tests com.engineeringood.athena.runtime.AthenaSemanticCommitServiceTest --tests com.engineeringood.athena.runtime.AthenaSemanticDiffInspectionTest --tests com.engineeringood.athena.runtime.AthenaSemanticDiffServiceTest --tests com.engineeringood.athena.runtime.AthenaSemanticHistoryStateServiceTest --tests com.engineeringood.athena.runtime.AthenaSemanticReviewServiceTest --tests com.engineeringood.athena.runtime.AthenaSemanticScmStateServiceTest`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaM28ProductAuthoringSmokeTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaSemanticInspectionTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceNavigationParityTest --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareSymbolsTest --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringSupportTest --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareNavigationTest --tests com.engineeringood.athena.ide.lsp.AthenaPresentationReferenceMarkerPayloadTest --tests com.engineeringood.athena.ide.lsp.AthenaRepeatedEditingStabilityTest --tests com.engineeringood.athena.ide.lsp.AthenaSemanticHistoryStateRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaSemanticScmStateRequestTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests com.engineeringood.athena.representation.M30DemoRepresentationBinderTest --tests com.engineeringood.athena.representation.M30ControlSheetCompositionProofTest --tests com.engineeringood.athena.representation.RepresentationBindingStatusPayloadTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:validation:test :kernel:svg-renderer:test :kernel:spatial-model:test :kernel:authoring-model:test :kernel:routing-model:test :kernel:presentation-model:test :kernel:projection-model:test :kernel:document-projection-model:test :kernel:semantic-scm:test --tests com.engineeringood.athena.semantics.core.EngineeringIrValidatorTest --tests com.engineeringood.athena.renderer.svg.SvgRendererModuleMarkerTest --tests com.engineeringood.athena.spatial.SemanticSpatialIntentModelTest --tests com.engineeringood.athena.authoring.SemanticRelationshipCompatibilityValidatorTest --tests com.engineeringood.athena.authoring.SemanticEntityAuthoringContractTest --tests com.engineeringood.athena.routing.TerminalStripBundleProofTest --tests com.engineeringood.athena.routing.SchematicRouteIntentProjectorTest --tests com.engineeringood.athena.routing.RouteQualityDiagnosticsTest --tests com.engineeringood.athena.routing.ElectricalConnectionIntentClassifierTest --tests com.engineeringood.athena.routing.AthenaRouteEngineSideStubTest --tests com.engineeringood.athena.presentation.PresentationModelContractTest --tests com.engineeringood.athena.projection.ProjectionModelContractTest --tests com.engineeringood.athena.document.DocumentProjectionModelContractTest --tests com.engineeringood.athena.scm.SemanticReviewSummaryGeneratorTest --tests com.engineeringood.athena.scm.SemanticDiffCalculatorTest --tests com.engineeringood.athena.scm.SemanticCommitIntentGeneratorTest`
  - `npm test` from `ide/tree-sitter-athena`
  - `rg -n "connection:.*->|route:connection:.*->|connection:\$.*->" kernel ide extensions examples -g "*.kt" -g "*.athena" -g "*.txt"` returned no matches.
  - `rg -n "legacy-connect-ports|compatibilityContract\(\)|legacy-preview-readonly|endpoint-plus-ordinal|alias-free" kernel ide extensions examples -g "*.kt" -g "*.athena" -g "*.txt" -g "*.md"` returned no matches.
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### Completion Notes List

- Required connection aliases are now the only accepted source form across ANTLR-facing parser tests, Tree-sitter corpus, active examples, runtime fixtures, and LSP tests.
- Connection identity now follows `connection:<SourceUnitId>:<alias>` in compiler/runtime surfaces; route, projection, document, presentation, SCM, and renderer tests no longer use endpoint-shaped connection IDs.
- Relationship authoring preview rebuilds canonical connection state from the authoritative backend source document and no longer exposes the deleted runtime compatibility contract.
- Repository hierarchy fixture fallout was corrected by moving stale test expectations to package-path source roots and explicit package declarations.
- AC evidence mapping: AC1 covered by language/compiler/model tests; AC2 covered by source migration plus stale ID/compat audits; AC3 covered by compiler semantic linker tests and LSP/runtime relationship preview tests; AC4 covered by LSP and Tree-sitter verification; AC5 covered by final audits and sequential verification commands above.
- Three-layer review:
  - Contract layer: source identity is authored alias + source unit, not endpoints/order/group.
  - Surface layer: ANTLR, Tree-sitter, LSP, formatter/navigation, examples, and generated artifacts agree on required aliases.
  - Regression layer: runtime, SCM, projection, route, document, presentation, package, and renderer tests were re-run after stale fixture cleanup.

### File List

- `examples/m0/demo-cabinet.engineering-ir.txt`
- `examples/m0/dual-drive-cabinet.athena`
- `examples/m0/duplicate-identity-cabinet.athena`
- `examples/m0/invalid-direction-cabinet.athena`
- `examples/m0/invalid-semantic-cabinet.athena`
- `examples/m11/dense-electrical-proof/src/com/engineeringood/assemblyline/assemblyline.athena`
- `examples/m12/renderer-benchmark-proof/src/expansion-line.athena`
- `examples/m14/siemens-proof-corpus/src/siemens-proof-corpus.athena`
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
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/01-schematic-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/02-dense-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/03-acceptance-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/04-boundary-scope.athena`
- `examples/m21/sample-project/src/01-baseline-sheet.athena`
- `examples/m21/sample-project/src/02-layout-intelligence-acceptance.athena`
- `examples/m21/sample-project/src/03-routing-and-label-readability.athena`
- `examples/m21/sample-project/src/04-boundary-scope.athena`
- `examples/m22/sample-project/src/01-baseline-sheet.athena`
- `examples/m22/sample-project/src/02-layout-optimization-acceptance.athena`
- `examples/m22/sample-project/src/03-component-round-trip.athena`
- `examples/m22/sample-project/src/04-boundary-scope.athena`
- `examples/m23/sample-project/src/com/engineeringood/m23/sample/01-layout-hints.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/01-control-route.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/02-terminal-strip-routes.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/03-power-protection-load.athena`
- `examples/m25/sample-project/src/01-professional-symbol-sheet.athena`
- `examples/m25/sample-project/src/02-terminal-labels-and-routes.athena`
- `examples/m25/sample-project/src/03-six-family-acceptance.athena`
- `examples/m26/sample-project/src/com/engineeringood/m26/sample/01-workspace-semantic-source.athena`
- `examples/m26/sample-project/src/com/engineeringood/m26/sample/02-field-assets-not-a-sheet.athena`
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
- `examples/m32/sample-project/README.md`
- `examples/m32/sample-project/athena.lock`
- `examples/m32/sample-project/src/com/engineeringood/m32/sample/01-package-platform-demo.athena`
- `examples/m34/professional-control-drawing/athena.lock`
- `examples/m34/professional-control-drawing/src/com/engineeringood/m34/professional/01-control-drawing.athena`
- `examples/m34/sample-project/athena.lock`
- `examples/m34/sample-project/src/com/engineeringood/m34/sample/01-native-cabinet-proof.athena`
- `examples/m4/open-repository-proof/src/com/engineeringood/factoryline/factoryline.athena`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeWorkbench.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM28ProductAuthoringSmokeTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareNavigationTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareSymbolsTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationReferenceMarkerPayloadTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepeatedEditingStabilityTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticHistoryStateRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceNavigationParityTest.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/connect.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/SemanticEntityAuthoringContractTest.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/SemanticRelationshipCompatibilityValidatorTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM17ParserParityProofTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaParserContinuityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlannerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticCapabilityProvenanceProjectorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinkerTest.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProof.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/DescriptorAnchorRouteEvidenceTest.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinderTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineSideStubTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntentClassifierTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteQualityDiagnosticsTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicRouteIntentProjectorTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/TerminalStripBundleProofTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningSessionRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphProjectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaM31SampleAuthoringProofTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticCommitServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticHistoryStateServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/GovernedRelationshipPreviewServiceTest.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGeneratorTest.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculatorTest.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGeneratorTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SemanticSpatialIntentModelTest.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererModuleMarkerTest.kt`
- `kernel/validation/src/test/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidatorTest.kt`

### Change Log

- 2026-07-28: Implemented stable authored connection aliases, migrated sources/fixtures/tests, removed compatibility paths, and verified Story 2.1 evidence gates.
