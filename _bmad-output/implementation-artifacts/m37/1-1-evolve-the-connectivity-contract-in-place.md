---
story_key: 1-1-evolve-the-connectivity-contract-in-place
epic: m37-e1
requirements: [FR-1, FR-3, FR-4]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.1: Evolve The Connectivity Contract In Place

Status: done

## Story

As an engineering author,
I want one stable Engineering Connectivity Contract vocabulary,
so that source, compiler, and AI tooling do not encounter two competing connectivity models.

## Acceptance Criteria

1. Existing M36 connectivity contracts and every active consumer are directly renamed and evolved into one `EngineeringConnectivityContract` family; no parallel model is added.
2. Stable semantic identities, Interfaces, Ports, compatibility, Connections, Connection Networks, optional physical/representation/evidence reference hooks, and source provenance remain typed and projection-neutral.
3. Manufacturer lifecycle, procurement, BOM ownership, datasheet lifecycle, simulation, product replacement, and other full ECS facts remain outside this contract.
4. Authored participation uses `connectivity enabled`, diagnostics use the `connectivity.*` namespace, and compiler API/fields describe Engineering Connectivity responsibility.
5. No old contract type, old source marker, adapter, alias, deprecated wrapper, compatibility facade, duplicate lowering path, or milestone-named production comment/class remains.
6. Focused connection-model, compiler, LSP, and existing regression tests pass with unchanged semantic IDs and topology; source-set hygiene, encoding, and diff checks pass.

## Tasks / Subtasks

- [x] Rename and evolve the source-owned contract family (AC: 1, 2, 3, 4, 5)
  - [x] Rename `ConnectableEntityContracts.kt` and all `Connectable*` contract types by current Engineering Connectivity responsibility.
  - [x] Rename compiler, compilation result, diagnostics, helper vocabulary, messages, and codes without aliases.
  - [x] Rename participation marker from `connectable enabled` to `connectivity enabled` and remove the old marker path.
  - [x] Keep typed physical, representation, and evidence reference hooks narrow and projection-neutral; do not add ECS product facts or new source syntax in this story.
- [x] Update active compiler and transient lowering consumers (AC: 1, 2, 4, 5)
  - [x] Rename `AthenaCompiler.compileConnectableEntities` and injected compiler fields by responsibility.
  - [x] Update compilation support, `EngineeringIrLowerer`, `ConnectionIrLowerer`, and `ConnectionIrModels` to consume the renamed contract directly.
  - [x] Preserve canonical semantic identities, connections, networks, constraint owner/strength, snapshots, and provenance.
- [x] Replace old test and fixture vocabulary test-first (AC: 4, 5, 6)
  - [x] Rename focused connection-model/compiler tests without milestone names and make them compile against the desired API before production edits.
  - [x] Update LSP diagnostics assertions and all authored source fixtures to `connectivity enabled` and `connectivity.*` codes.
  - [x] Add negative assertions proving the legacy marker does not opt a component into the contract.
- [x] Verify direct-refactor and architecture gates (AC: 5, 6)
  - [x] Prove no old contract symbols, compiler API, diagnostic namespace, source marker, or M36 production comment remains.
  - [x] Run focused tests, full regression, source-set hygiene, UTF-8 encoding audit, and `git diff --check` sequentially.

## Dev Notes

### Authority And Scope

- Athena source remains SSOT. This contract is a compiler-owned projection-neutral view of canonical Engineering IR.
- Direct refactor only. Athena is pre-public: no migration adapter, type alias, deprecated overload, dual marker, or fallback.
- Port meaning remains separate from representation Anchor and SVG geometry. SVG and renderer code cannot create connectivity.
- `ConnectionIr` remains transient derived IR. This story renames its input types; it does not create another IR.
- Keep full ECS outside scope. Do not add manufacturer/article/lifecycle/procurement/BOM/datasheet/simulation/replacement fields.
- Story 1.2 owns grouped Interface/Port source syntax. Story 2.1 owns full External Evidence Mapping syntax. Story 1.1 may retain narrow typed reference hooks only; it must not pre-implement those stories.

### Current Code And Required Changes

- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/ConnectableEntityContracts.kt` currently owns all contracts, validation, diagnostics, connection/network compilation, compatibility parsing, and the `connectable` participation marker. Rename this cohesive file by responsibility; preserve behavior except deliberate vocabulary changes.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` exposes `compileConnectableEntities` and injects `ConnectableEntityContractCompiler`. Replace both directly.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt` compiles connectivity for presentation gating and semantic diagnostics. Both paths must use the same renamed compiler instance.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` and `ConnectionIrLowerer.kt` lower only successful validated contracts. Preserve this single path.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrModels.kt` imports contract Interface IDs, compatibility enums, and owner/strength types. Rename imports and conversion functions without changing Connection IR identity or wire behavior.
- Update focused tests under `kernel/connection-model/src/test`, `kernel/compiler/src/test`, and `ide/lsp/src/test`. Rename `AthenaM36ConnectableEntityCompilationTest` and `AthenaM36ConnectionNetworkCompilationTest` by behavior; production naming rules also apply to new tests where a milestone prefix adds no useful responsibility.
- Generic UI notions such as whether a visual node can participate in a connect action are not automatically the old contract model. Rename only when they expose the removed contract vocabulary or marker; avoid unrelated frontend churn.

### Naming Contract

- Root: `EngineeringConnectivityContract`, `EngineeringConnectivityContractCompiler`, `EngineeringConnectivityCompilation`, `EngineeringConnectivityDiagnostic`.
- Related types use one consistent `EngineeringConnectivity*` family unless an existing generic `EngineeringConnection*` IR type already owns that concept.
- Compiler API: `compileEngineeringConnectivity`.
- Source marker: `connectivity enabled`.
- Diagnostic namespace: `connectivity.*`.
- Internal locals describe `connectivityParticipants` or `connectivityOwners`, not milestone history.

### TDD And Verification

- RED: update focused tests to desired names/API/marker/diagnostic codes, then run `:kernel:connection-model:test` and `:kernel:compiler:test` to observe compile/assertion failure caused by missing renamed production contract.
- GREEN: perform direct production rename and run those focused module tests sequentially.
- Verify LSP tests after compiler tests; Gradle commands must never overlap on Windows.
- Final commands include root `test`, `tools/source-set-hygiene-audit.ps1`, `tools/encoding-audit.ps1`, and `git diff --check`.

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 1.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-1, FR-3, FR-4, NFR-1, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Engineering Connectivity Contract Boundary]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md` - AD-1 through AD-4]
- [Source: `_bmad-output/implementation-artifacts/m36/retrospective.md` - Architecture Decisions Preserved]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:kernel:connection-model:test` failed against missing `EngineeringConnectivity*` API after tests were renamed first.
- GREEN: direct contract-family replacement made `:kernel:connection-model:test` pass.
- RED: focused `ConnectionIrLoweringTest` proved removed `connectable enabled` still produced orphan Connection IR through raw-document lowering.
- GREEN: validation now compiles Engineering Connectivity once and passes that exact successful artifact into `ConnectionIrLowerer`; raw `EngineeringDocument` lowering was removed.
- RED: reference-preservation test failed until authored paths, target identities, and provenance were represented in contract and Connection IR references.
- GREEN: typed physical, representation, and evidence hooks now survive lowering without adding source syntax or ECS product facts.
- Regression diagnosis: M31/M34/M35/M36 samples depended on the removed raw lowering path. Their authoritative `.athena` sources were migrated to explicit `connectivity enabled`; all affected compiler-owned locks were regenerated.
- Verification: `:kernel:compiler:test` passed 334 tests; `:ide:lsp:test` passed 121 tests; root `test` passed 152 tasks.
- Quality gates: source-set hygiene, encoding audit, legacy-vocabulary audit, production-filename audit, and `git diff --check` passed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Replaced the complete `Connectable*` family, compiler API, diagnostics, and source marker in place with one `EngineeringConnectivity*` vocabulary. No aliases, adapters, deprecated APIs, or fallback marker remain.
- Removed duplicate connectivity compilation and raw-document Connection IR lowering. Backend emission now consumes the exact validated connectivity artifact from the validation pass.
- Preserved typed semantic IDs, Interfaces, Ports, compatibility, Connections, Networks, constraint ownership/strength, snapshots, authored references, and provenance.
- Added narrow physical-installation, representation-binding, and external-evidence reference hooks while keeping manufacturer, lifecycle, procurement, BOM, datasheet, simulation, and replacement facts outside the contract.
- Migrated maintained sample sources to explicit connectivity participation and regenerated derived lock evidence through the compiler-owned materializer.
- Full regression and repository quality gates pass.
- Code review follow-up removed stale milestone/proof labels from production comments; production audit now has no `Connectable`, `connectable`, milestone, `Proof`, `Demo`, `V0`, or `V1` hits in active main paths.

### File List

- `_bmad-output/implementation-artifacts/m37/1-1-evolve-the-connectivity-contract-in-place.md`
- `_bmad-output/implementation-artifacts/m37/sprint-status.yaml`
- `examples/m31/sample-project/athena.lock`
- `examples/m31/sample-project/src/com/engineeringood/m31/sample/01-governed-authoring-customer-source.athena`
- `examples/m34/sample-project/athena.lock`
- `examples/m34/sample-project/src/com/engineeringood/m34/sample/01-native-cabinet-proof.athena`
- `examples/m35/physical-installation-cabinet/src/com/engineeringood/m35/physicalinstallationcabinet/01-physical-installation-cabinet.athena`
- `examples/m35/physical-installation-cabinet/athena.lock`
- `examples/m36/connectivity-cabinet/athena.lock`
- `examples/m36/connectivity-cabinet/src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentMapper.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredLayoutIntentSourceSerializer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaComponentKnowledgeContextBuilder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutConstraintLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriver.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaEntityUpdateSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36ConnectableEntityCompilationTest.kt` (deleted)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36ConnectionNetworkCompilationTest.kt` (deleted)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt` (added)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityNetworkCompilationTest.kt` (added)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RouteIntentLowererTest.kt`
- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/ConnectableEntityContracts.kt` (deleted)
- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt` (added)
- `kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/ConnectableEntityContractCompilerTest.kt` (deleted)
- `kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContractCompilerTest.kt` (added)

## Change Log

- 2026-07-30: Created implementation-ready Story 1.1 from finalized M37 PRD, architecture, retrospective, and current-code analysis.
- 2026-07-30: Directly replaced legacy connectivity vocabulary and lowering paths with one validated Engineering Connectivity Contract pipeline; migrated active samples and passed full regression.
- 2026-07-30: Code review follow-up removed stale milestone/proof labels from production comments and re-ran full regression plus hygiene gates.
