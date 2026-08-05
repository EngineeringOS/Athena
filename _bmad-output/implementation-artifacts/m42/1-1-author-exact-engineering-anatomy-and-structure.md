---
baseline_commit: c00b416c463d3e18876dced3be6b750d2f0652ff
---

# Story 1.1: Author Exact Engineering Anatomy And Structure

Status: done

## Story

As a multidisciplinary engineer,
I want to author exact values, Entities, Functions, Ports, and structure context,
so that my design intent has stable identity and domain-neutral meaning before any drawing or vendor implementation.

## Acceptance Criteria

1. Given Athena Source containing Quantity, Integer, Boolean, Text, Symbol, and Reference values,
   when the compiler lowers the source into Engineering Reality, then every value uses the typed
   exact contract, rational quantity conversion/comparison loses no precision, and incompatible
   dimensions fail with exact expected/actual dimensions and source Provenance. Kernel/shared
   contracts contain no domain-specific unit, terminal, structure, or rule constants.
2. Given one Entity with main and auxiliary Functions and exact Entity- or Function-owned Ports,
   when the source compiles, then ownership, stable identity, direction, admitted Flow references,
   cardinality, and optional package-defined terminal/interface designation are inspectable.
   Function ownership does not prohibit later cross-Entity Relationship participation.
3. Given functional, installation, or product/entity Structure Assignments including package-defined
   `=`, `+`, or `-` display designations, when an engineer changes only the Structure Assignment,
   then Entity identity, Concept reference, Function/Port identity, and semantic relationships remain
   unchanged. `engineering-model` owns only assignment/reference mechanics.
4. Given active code that previously consumed `EngineeringComponent`, when this story installs
   EngineeringEntity anatomy, then every affected active compiler/runtime/model consumer migrates
   directly, superseded Component authority and tests are deleted in the same story, and no alias,
   adapter, fallback, or dual model exists. Direct contract tests, source-set hygiene, encoding audit,
   and sequential Gradle verification pass.

## Tasks / Subtasks

- [x] Task 1: Install exact domain-neutral Engineering Values (AC: 1)
  - [x] Add failing `engineering-model` tests for normalized reduced `BigInteger` rationals, positive
        denominators, exact equality/order/arithmetic, compatible-unit conversion, and dimension mismatch.
  - [x] Implement cohesive value contracts: `ExactNumber`, package-qualified unit/reference IDs,
        sparse package-qualified dimension signatures, exact unit scale/offset input, and sealed
        `EngineeringValue` variants. Reject zero denominators, malformed dimensions, incompatible
        comparisons, and illegal affine multiplication/division with typed failure evidence.
  - [x] Keep all unit and dimension names package-supplied. Do not add `V`, `A`, `kW`, electrical,
        mechanical, automation, terminal, or rule constants to kernel/shared production code.
- [x] Task 2: Install Entity, Function, Port, and Structure anatomy (AC: 2, 3)
  - [x] Add failing model/compiler tests for one Entity with main and auxiliary Functions, Entity-owned
        and Function-owned Ports, explicit owner references, stable IDs, direction, admitted Flow
        definition references, cardinality, optional interface designation, and Concept reference.
  - [x] Add failing change tests proving Structure Assignment display address never forms Entity,
        Function, or Port identity.
  - [x] Replace `EngineeringComponent` with `EngineeringEntity`; make Function/Port ownership reference
        exact Engineering subjects. Keep Functions Entity-owned for identity while exposing references
        usable by Story 1.2 cross-Entity Relationship participants.
  - [x] Add domain-neutral Structure Aspect references and Structure Assignments. Package-defined
        designations are data; `engineering-model` contains no built-in `=`, `+`, or `-` semantics.
- [x] Task 3: Replace project source and lowering authority directly (AC: 1-4)
  - [x] Add failing accepted/rejected source fixtures for exact values and Entity/Function/Port/Structure
        authoring, including precise source spans and plain dimension/ownership diagnostics.
  - [x] Add a compiler permutation test proving declaration/collection order never forms Entity,
        Function, or Port identity.
  - [x] Add failing compiler/language tests proving Quantity, Integer, Boolean, Text, Symbol, and
        Reference source values lower without `Double` or evaluator-boundary string parsing.
  - [x] Replace `entity`/`DeviceDeclaration` as active project authority with Entity syntax/model and
        update ANTLR adapter, AST, formatter, document symbols, completion, semantic tokens, source edits,
        domain lowering blueprints, coordinator, and `EngineeringIrLowerer` directly.
  - [x] Preserve one ANTLR semantic parser and Athena-owned AST/span boundary. Do not add a parser fork,
        compatibility grammar alias, old-to-new adapter, or Tree-sitter semantic acceptance.
  - [x] Keep generic `EngineeringConnection` replacement scoped to Story 1.2, but compile existing
        connectivity against current Entity/Function/Port identities without restoring Component authority.
- [x] Task 4: Migrate all active Component consumers and delete superseded authority (AC: 4)
  - [x] Use CodeGraph plus compile failures to migrate every active production consumer of
        `EngineeringComponent`, `document.components`, Component node/reference kinds, and component
        blueprints to current Entity contracts across compiler, validation, runtime, semantic SCM,
        plugin API/hosts, domain extensions, LSP, Projection inputs, and surviving package/routing paths.
  - [x] Rename or delete active tests, fixtures, docs, payload wording, graph kinds, helper methods,
        identities, and diagnostics whose authority still says Component. Do not edit closed M0-M41
        BMad artifacts; do not preserve stale examples/tests to prove retired behavior.
  - [x] Add an executable absence test covering active production paths for `EngineeringComponent`,
        `DeviceDeclaration` as canonical project authority, `component:` identities, and compatibility
        aliases/adapters/fallbacks. Domain UI may later display Entity, but shared contracts say Entity.
- [x] Task 5: Verify Story 1.1 and complete records (AC: 1-4)
  - [x] Run focused red/green tests after each task, then all affected module tests and repository tests
        strictly sequentially. Record actual failing and passing commands/results in Debug Log.
  - [x] Run source-set hygiene audit, encoding audit, active-source legacy scan, and `git diff --check`.
  - [x] Complete every checkbox, Debug Log, Completion Notes, File List, and Change Log before status
        changes to `review`; do not claim Story 1.2 Relationship/Flow or Epic 2 knowledge behavior.

## Dev Notes

### Architecture Guardrails

- Authority chain remains source meaning -> compiled Engineering facts -> Projection -> Spatial ->
  Presentation/Theia paint. This story changes upstream Engineering anatomy only.
- Apply AD-20, AD-21, AD-22, AD-24, AD-26, AD-34, AD-35, AD-37, and AD-38.
- `engineering-model` owns project identity, exact values, Entities, Functions, Ports, Structure
  Assignment mechanics, and resolved definition references. It owns no reusable Concept/unit/structure
  definitions, Judgements, renderer fields, or domain constants.
- `knowledge-model` does not exist yet. Do not create it in Story 1.1. Package-authored definitions,
  formulas, Constraints, and canonical Knowledge Document begin in Epic 2.
- No backward compatibility. Delete old type/name authority; never use type aliases, deprecated shells,
  conversion adapters, dual fields, fallback readers, or legacy feature flags.
- Story 1.2 owns typed `EngineeringRelationship`, named Participant Roles, independent Flow facts,
  `EngineeringConnection` deletion, and Projection/Spatial Relationship migration. Do not pre-implement it.
- M43 owns rendering. No label, style, grid, routing, export, canvas, or paint improvement.

### Exact Value Contract

- `ExactNumber` is a normalized reduced rational with `BigInteger` numerator and positive denominator.
  Zero is canonical `0/1`; sign belongs on numerator. Never cross exact boundaries as `Double`.
- Quantity carries exact value plus package-qualified Unit reference. Unit conversion input carries exact
  scale, optional exact offset, and sparse dimension signature keyed by package-qualified base-dimension
  ID with nonzero integer exponents.
- Dimension mismatch is a typed failure naming subject, expected signature, actual signature, correction
  direction, and source Provenance. No unit registry, package priority, or guessed conversion lives here.
- `EngineeringValue` is sealed: Quantity, Integer, Boolean, Text, Symbol, Reference. Existing
  `EngineeringPropertyValue.Symbol/Text` is superseded rather than wrapped; all exhaustive `when`
  consumers must migrate.

### Anatomy And Identity Contract

- `EngineeringEntity`: stable ID, authored name, resolved Concept reference, typed properties,
  Structure Assignments, Provenance.
- `EngineeringFunction`: stable ID, Entity owner reference, authored name/role, owned/reference Ports,
  typed properties, Provenance. Main/auxiliary meaning must be package/authored data, not kernel enum.
- `EngineeringPort`: stable ID, exact Entity or Function owner reference, authored name, direction,
  admitted package-qualified Flow references, cardinality, optional package-defined interface/terminal
  designation, typed properties, Provenance.
- Stable identity derives from semantic authored containment, never parser offset, list index, Structure
  Assignment, display designation, Part binding, Projection occurrence, or runtime object identity.
- Prefer a sealed owner-subject reference or other typed subject-level contract over an unvalidated
  generic path. Preserve unresolved authored path plus Provenance where fail-closed diagnostics need it.

### Current Code And Required Migration

- `EngineeringModel.kt` currently exposes `components`, `EngineeringComponent`, global Ports with a
  generic owner reference, Function role plus Port references, and only Symbol/Text property values.
- `EngineeringIrLowerer.kt` builds `component:` IDs from plugin `components`, resolves every Port and
  Function owner through the component map, and publishes `EngineeringDocument.components`.
- `AthenaDomainSemanticsModel.kt` exposes `AthenaDomainComponentBlueprint`, `component()`, and a
  contribution `components` list. Coordinator concatenates those lists. Replace this authority directly.
- `AthenaLanguageModel.kt` and `Athena.g4` expose `DeviceDeclaration`/`entity`. Adapter, formatter,
  LSP symbols/completion/tokens/source edits, electrical/dummy domain plugins, and compiler tests depend
  on it. Migrate the active project syntax contract and regenerate ANTLR output through Gradle.
- `AthenaEngineeringGraphProjection.kt` exposes COMPONENT/CONNECTION node vocabulary and stringifies
  only Symbol/Text. Entity/value migration must be complete; Relationship node changes remain 1.2.
- `EngineeringIrValidator`, `SemanticDiffCalculator`, projection derivation, representation material
  selection, runtime commands/mutation, domain validation, connection-model, routing evidence, LSP
  inspection, and active tests consume `document.components` either directly or transitively. Compile
  every affected module; do not stop after the kernel model compiles.
- Dedicated Component Knowledge and Semantic Macro paths are architecturally retired, but their full
  knowledge/macro responsibility deletion is owned by Stories 2.2 and 4.3. This story must remove their
  dependency on Component authority without inventing replacement knowledge DTOs.

### Project Structure

- Split `EngineeringModel.kt` by cohesive role if needed: small IDs/references together, exact values in
  `EngineeringValueModels.kt`, anatomy in `EngineeringAnatomyModels.kt`, existing Projection carriers in
  their current cohesive file. Avoid one tiny file per DTO and mixed 400-line dumps.
- Keep package namespace consistent with current `com.engineeringood.athena.ir` unless a repo-wide
  current-name move is part of the direct migration. Do not create milestone-named production types.
- Expected primary update areas:
  - `kernel/engineering-model/src/main|test`
  - `kernel/language/src/main|test`
  - `kernel/plugins/plugin-api/src/main|test`
  - `kernel/compiler/src/main|test`
  - `kernel/validation`, `kernel/semantic-scm`, `kernel/runtime`
  - `extensions/domain-electrical`, `extensions/domain-dummy`
  - `ide/lsp` and active current docs/examples required by compilation
- Use CodeGraph before each legacy-contract edit wave. Generated ANTLR files stay build output; never
  hand-edit generated parser classes.

### Testing Requirements

- Kotlin/JUnit tests must assert literal expected values and failures. No self-comparison, count-only,
  reflection-only, or source-text-only acceptance proof.
- Required focused coverage: rational normalization/arithmetic; exact compatible conversion; dimension
  mismatch; all six EngineeringValue variants; Entity/Function/Port ownership and identity; Port
  direction/Flow/cardinality/designation; Structure Assignment identity independence; parser spans;
  deterministic permutation; active Component authority absence.
- First focused model/compiler runs must fail for missing M42 behavior before production edits. Record
  exact red evidence. Every later task stays green before next task begins.
- Gradle verification is strictly sequential on Windows. Minimum close sequence:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:validation:test
.\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### Git Intelligence

- M41 commit `29d2a60` installed current Projection/Spatial contracts across compiler/runtime/LSP and
  must remain green. M42 Story 1.1 migrates their upstream identity inputs, not their ownership.
- M42 planning commits `765e530`, `20a8075`, `3f152e6`, and `c00b416` freeze PRD, architecture,
  readiness, epics, and sprint. Do not rewrite those artifacts during development.
- Worktree before story creation contains only expected M42 sprint-status transition.

### References

- [Source: `_bmad-output/planning-artifacts/m42/epics.md` Epic 1, Story 1.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/prd.md` Sections 3-6, 11-16]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/m42-engineering-knowledge-system-design.md` Canonical Engineering Vocabulary, Typed Engineering Values]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-04-m42/ARCHITECTURE-SPINE.md` AD-20..AD-26, AD-34..AD-38]
- [Source: `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`]
- [Source: `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`]
- [Source: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`]
- [Source: `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:kernel:engineering-model:test --tests "*EngineeringValueModelsTest*"` failed at
  `compileTestKotlin` because M42 exact value, unit, dimension, conversion, and operation types did not exist.
- GREEN: focused `EngineeringValueModelsTest` and full `:kernel:engineering-model:test` passed.
- RED: focused `EngineeringAnatomyModelsTest` failed at `compileTestKotlin` on absent Entity,
  typed owner, Port anatomy, and Structure Assignment contracts.
- GREEN: focused anatomy test and full `:kernel:engineering-model:test` passed after direct model replacement.
- GREEN: `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:validation:test`,
  `:kernel:semantic-scm:test`, `:kernel:runtime:test`, and `:ide:lsp:test` passed sequentially.
- CLEANUP RED/GREEN: compile/test failures exposed retired engineering-impact, knowledge-inspection,
  electrical render-contribution, M32 proof, and plugin-host expectations; obsolete tests/contracts were
  deleted or migrated, then each focused module passed.
- REGRESSION GREEN: `gradlew test` passed all active modules after the final cleanup wave.
- FRONTEND GREEN: `ide/theia-frontend yarn test` built TypeScript and passed 6/6 Node tests.
- AUDIT GREEN: source-set hygiene, UTF-8 encoding audit, active production legacy scan, and
  `git diff --check` passed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Installed exact reduced rational arithmetic, package-qualified definition references, sparse
  dimension signatures, all six Engineering Value variants, exact unit conversion/comparison, and
  affine multiplication/division rejection without domain constants.
- Replaced Component model authority with Entity, Entity-owned Functions, exact Entity/Function Port
  ownership, cross-domain Port contract, and structure assignments independent from stable identity.
- Replaced active source grammar, AST, formatter, compiler lowering, domain plugin, runtime, SCM, and
  LSP consumers with direct Entity contracts. No Component alias, adapter, fallback, or dual model remains.
- Removed retired AI, Semantic Macro, Component Knowledge, presentation/SVG, routing/template/reuse,
  command-history, authoring-mutation, and milestone proof paths that no longer belong to current product authority.
- Preserved IDE editor/LSP, repository graph, semantic SCM, Entity inspection, Tree-sitter syntax UX,
  Projection, and Spatial contracts needed by M42/M43 without adding render behavior.
- Acceptance review completed after sequential Gradle regression, frontend tests, source-set hygiene,
  encoding audit, active-source legacy scan, and `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m42/1-1-author-exact-engineering-anatomy-and-structure.md`
- `_bmad-output/implementation-artifacts/m42/sprint-status.yaml`
- `apps/cli/build.gradle.kts`
- `apps/cli/src/main/kotlin/com/engineeringood/athena/cli/AthenaCliSessionStore.kt`
- `apps/cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/AthenaAiProposalCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/CommandHistoryCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/ConnectCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/ParseCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/PluginRuntimeCliTest.kt`
- `docs/compiler/m0-domain-plugin-boundary.md`
- `docs/compiler/m0-dsl.md`
- `docs/compiler/m0-validation-boundary.md`
- `docs/compiler/m1-incremental-recompute-boundary.md`
- `docs/superpowers/specs/2026-07-16-m19-schematic-projection-layout-design.md`
- `docs/usages/m13-proof-usage.md`
- `docs/usages/m14-proof-usage.md`
- `docs/usages/m15-proof-usage.md`
- `docs/usages/m18-proof-usage.md`
- `docs/usages/m25-representation-acceptance-proof.md`
- `docs/usages/m28-proof-usage.md`
- `docs/usages/m8-proof-usage.md`
- `examples/m0/demo-cabinet.athena`
- `examples/m0/demo-cabinet.engineering-ir.txt`
- `examples/m0/demo-cabinet.expectation.txt`
- `examples/m0/demo-cabinet.svg`
- `examples/m0/dual-drive-cabinet.athena`
- `examples/m0/dual-drive-cabinet.expectation.txt`
- `examples/m0/duplicate-identity-cabinet.athena`
- `examples/m0/duplicate-identity-cabinet.expectation.txt`
- `examples/m0/invalid-direction-cabinet.athena`
- `examples/m0/invalid-direction-cabinet.expectation.txt`
- `examples/m0/invalid-semantic-cabinet.athena`
- `examples/m0/invalid-semantic-cabinet.expectation.txt`
- `examples/m0/quoted-properties-cabinet.athena`
- `examples/m0/quoted-properties-cabinet.expectation.txt`
- `examples/m10/reasoning-proof/baseline/athena.lock`
- `examples/m10/reasoning-proof/baseline/src/com/engineeringood/m10/proof/factoryline.athena`
- `examples/m10/reasoning-proof/current/athena.lock`
- `examples/m10/reasoning-proof/current/src/com/engineeringood/m10/proof/factoryline.athena`
- `examples/m11/dense-electrical-proof/athena.lock`
- `examples/m11/dense-electrical-proof/src/com/engineeringood/assemblyline/assemblyline.athena`
- `examples/m12/renderer-benchmark-proof/athena.lock`
- `examples/m12/renderer-benchmark-proof/src/com/engineeringood/rendererbenchmark/expansion-line.athena`
- `examples/m14/siemens-proof-corpus/athena.lock`
- `examples/m14/siemens-proof-corpus/src/com/engineeringood/examples/m14/siemens/proof/corpus/siemens-proof-corpus.athena`
- `examples/m15/guided-authoring-proof/src/guided-authoring-proof.athena`
- `examples/m16/semantic-reuse-proof/athena.lock`
- `examples/m16/semantic-reuse-proof/src/com/engineeringood/examples/m16/semantic/reuse/proof/semantic-reuse-proof.athena`
- `examples/m17/invalid-and-incomplete-proof/incomplete-brace.athena`
- `examples/m17/invalid-and-incomplete-proof/missing-to.athena`
- `examples/m17/invalid-and-incomplete-proof/over-qualified-port.athena`
- `examples/m17/invalid-and-incomplete-proof/over-qualified-port.expectation.txt`
- `examples/m17/invalid-and-incomplete-proof/unterminated-string.athena`
- `examples/m17/parser-parity-proof/dense-qualified-names.athena`
- `examples/m17/parser-parity-proof/dense-qualified-names.expectation.txt`
- `examples/m17/parser-parity-proof/parity-cabinet.athena`
- `examples/m17/parser-parity-proof/parity-cabinet.expectation.txt`
- `examples/m17/repository-parity-proof/athena.lock`
- `examples/m17/repository-parity-proof/src/com/engineeringood/m17/parity/parity-repo.athena`
- `examples/m18/repository-proof/valid-workspace/athena.lock`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/cross-package-consumer.athena`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/invalid-import.athena`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/single-package-success.athena`
- `examples/m18/repository-proof/valid-workspace/src/com/engineeringood/m18/root/unresolved-symbol.athena`
- `examples/m18/repository-proof/valid-workspace/vendor/controls/athena.lock`
- `examples/m18/repository-proof/valid-workspace/vendor/controls/src/com/engineeringood/m18/vendor/controls/vendor-controls.athena`
- `examples/m18/syntax-proof/valid-package-import.athena`
- `examples/m18/syntax-proof/valid-package-only.athena`
- `examples/m19/schematic-sheet-proof/ready-sheet.diagram.mjs`
- `examples/m2/demo-cabinet.athena`
- `examples/m2/demo-cabinet.cabinet.svg`
- `examples/m2/demo-cabinet.wiring.svg`
- `examples/m2/operator-proof.athena`
- `examples/m20/dense-sheet-proof/ready-sheet.diagram.mjs`
- `examples/m20/sample-project/athena.lock`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/01-schematic-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/02-dense-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/03-acceptance-sheet.athena`
- `examples/m20/sample-project/src/com/engineeringood/m20/sample/04-boundary-scope.athena`
- `examples/m21/sample-project/athena.lock`
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/01-baseline-sheet.athena`
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/02-layout-intelligence-acceptance.athena`
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/03-routing-and-label-readability.athena`
- `examples/m21/sample-project/src/com/engineeringood/m21/sample/04-boundary-scope.athena`
- `examples/m22/sample-project/athena.lock`
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/01-baseline-sheet.athena`
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/02-layout-optimization-acceptance.athena`
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/03-component-round-trip.athena`
- `examples/m22/sample-project/src/com/engineeringood/m22/sample/04-boundary-scope.athena`
- `examples/m23/parser-parity-proof/valid-installation-cabinet.athena`
- `examples/m23/parser-parity-proof/valid-layout-block.athena`
- `examples/m23/sample-project/athena.lock`
- `examples/m23/sample-project/src/com/engineeringood/m23/sample/01-layout-hints.athena`
- `examples/m24/sample-project/athena.lock`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/01-control-route.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/02-terminal-strip-routes.athena`
- `examples/m24/sample-project/src/com/engineeringood/m24/sample/03-power-protection-load.athena`
- `examples/m25/sample-project/athena.lock`
- `examples/m25/sample-project/src/com/engineeringood/m25/sample/01-professional-symbol-sheet.athena`
- `examples/m25/sample-project/src/com/engineeringood/m25/sample/02-terminal-labels-and-routes.athena`
- `examples/m25/sample-project/src/com/engineeringood/m25/sample/03-six-family-acceptance.athena`
- `examples/m26/sample-project/athena.lock`
- `examples/m26/sample-project/src/com/engineeringood/m26/sample/01-workspace-semantic-source.athena`
- `examples/m26/sample-project/src/com/engineeringood/m26/sample/02-field-assets-not-a-sheet.athena`
- `examples/m27/sample-project/athena.lock`
- `examples/m27/sample-project/src/com/engineeringood/m27/sample/01-workspace-semantic-source.athena`
- `examples/m27/sample-project/src/com/engineeringood/m27/sample/02-field-assets-not-a-sheet.athena`
- `examples/m28/sample-project/athena.lock`
- `examples/m28/sample-project/README.md`
- `examples/m28/sample-project/src/com/engineeringood/m28/sample/01-relationship-authoring-source.athena`
- `examples/m28/sample-project/src/com/engineeringood/m28/sample/02-relationship-candidates.athena`
- `examples/m29/sample-project/athena.lock`
- `examples/m29/sample-project/src/com/engineeringood/m29/sample/01-interaction-authoring-source.athena`
- `examples/m29/sample-project/src/com/engineeringood/m29/sample/02-interaction-candidates.athena`
- `examples/m29/sample-project/src/com/engineeringood/m29/sample/03-entity-creation-context.athena`
- `examples/m3/dual-domain-proof.athena`
- `examples/m3/dual-domain-proof.expectation.txt`
- `examples/m3/dummy-proof.athena`
- `examples/m3/dummy-proof.expectation.txt`
- `examples/m3/electrical-proof.athena`
- `examples/m3/electrical-proof.expectation.txt`
- `examples/m30/sample-project/athena.lock`
- `examples/m30/sample-project/src/com/engineeringood/m30/sample/01-rolling-shutter-control-source.athena`
- `examples/m31/sample-project/athena.lock`
- `examples/m31/sample-project/src/com/engineeringood/m31/sample/01-governed-authoring-customer-source.athena`
- `examples/m32/sample-project/athena.lock`
- `examples/m32/sample-project/src/com/engineeringood/m32/sample/01-package-platform-demo.athena`
- `examples/m34/sample-project/athena.lock`
- `examples/m34/sample-project/packages/representation/athena/iec/cabinet-bindings.athena`
- `examples/m34/sample-project/src/com/engineeringood/m34/sample/01-native-cabinet-proof.athena`
- `examples/m39/reality-product-proof/athena.lock`
- `examples/m39/reality-product-proof/packages/representation/com/engineeringood/m39/realityproductproof/bindings.athena`
- `examples/m39/reality-product-proof/src/com/engineeringood/m39/realityproductproof/01-reality-product-proof.athena`
- `examples/m4/open-repository-proof/athena.lock`
- `examples/m4/open-repository-proof/src/com/engineeringood/factoryline/factoryline.athena`
- `examples/m40/rolling-shutter-control/athena.lock`
- `examples/m40/rolling-shutter-control/src/com/engineeringood/m40/rollingshutter/01-rolling-shutter-control.athena`
- `examples/m41/rolling-shutter/athena.lock`
- `examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena`
- `examples/m9/motor-derived-context.athena`
- `examples/m9/motor-impact-after.athena`
- `examples/m9/motor-impact-before.athena`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/build.gradle.kts`
- `extensions/domain-electrical/README.md`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalAuthoringCapabilityRegistryFactory.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEngineeringConceptTemplates.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeComponentKnowledge.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeConnectionAndPhysicalKnowledge.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeContracts.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimePresentationPacks.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeReview.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeWorkbench.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalAuthoringCapabilityRegistryFactoryTest.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEngineeringConceptTemplatesTest.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeComponentKnowledgeTest.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeConnectionAndPhysicalKnowledgeTest.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/README.md`
- `ide/lsp/README.zh-CN.md`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAiReasoningProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaComponentKnowledgeSessionProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingCompositionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingEvidencePayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGraphCommandIntentProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaKnowledgeProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationTracePayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionCommandProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMacroProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMutationReviewProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAiReasoningRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaComponentKnowledgeRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingProofPayloadMapperTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM18RepositoryProofCorpusLspTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM41SpatialPayloadTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareNavigationTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareSymbolsTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationConnectorPayloadTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationReferenceMarkerPayloadTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProductAuthoringSmokeTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepeatedEditingStabilityTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaReuseRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticAuthorityBoundaryTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticHistoryStateRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceNavigationParityTest.kt`
- `ide/theia-frontend/package.json`
- `ide/theia-frontend/README.md`
- `ide/theia-frontend/README.zh-CN.md`
- `ide/theia-frontend/scripts/athena-authoring-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-component-panel-model.test.mjs`
- `ide/theia-frontend/scripts/athena-governed-graphic-edit-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-graph-command-intent-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-guided-connection-model.test.mjs`
- `ide/theia-frontend/scripts/athena-ide-density-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-inspector-model.test.mjs`
- `ide/theia-frontend/scripts/athena-language-highlighting-definition.test.mjs`
- `ide/theia-frontend/scripts/athena-lsp-editor-bridge-service.test.mjs`
- `ide/theia-frontend/scripts/athena-m19-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m19-sheet-proof.test.mjs`
- `ide/theia-frontend/scripts/athena-m20-acceptance-fixture.test.mjs`
- `ide/theia-frontend/scripts/athena-m20-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m20-dense-sheet-proof.test.mjs`
- `ide/theia-frontend/scripts/athena-m20-regression-suite.test.mjs`
- `ide/theia-frontend/scripts/athena-m20-sheet-proof.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-acceptance-coverage.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-canvas-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-active-source-projection.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-baseline-proof.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-elk-comparison.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-elk-envelope.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-identity-coherence.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-adjustment-intent.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-hint-syntax.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-replay-proof.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m23-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m23-ide-behavior-preservation.test.mjs`
- `ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs`
- `ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-graph-workbench-preservation.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-routing-acceptance.test.mjs`
- `ide/theia-frontend/scripts/athena-m24-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m25-acceptance-proof.test.mjs`
- `ide/theia-frontend/scripts/athena-m25-frontend-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m25-product-smoke-wiring.test.mjs`
- `ide/theia-frontend/scripts/athena-m25-representation-rendering.test.mjs`
- `ide/theia-frontend/scripts/athena-m26-reference-marker-transport.test.mjs`
- `ide/theia-frontend/scripts/athena-m26-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m27-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m28-product-smoke-wiring.test.mjs`
- `ide/theia-frontend/scripts/athena-m29-interaction-adapter-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m29-product-smoke-wiring.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-final-purge-regression.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-product-smoke-wiring.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-qet-converter-design.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-representation-rendering.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-retrospective-cleanup-ledger.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-svg-bounds-regression.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-transparent-chrome.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-backend-source-authority.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-cont…3236 tokens truncated…kotlin/com/engineeringood/athena/compiler/AthenaM21SampleProjectCompilerTest.kt`
- `ide/tree-sitter-athena/test/corpus/entity.txt`
- `ide/tree-sitter-athena/test/fixtures/engineering-anatomy.athena`
- `ide/tree-sitter-athena/test/incomplete/unclosed-entity-block.athena.txt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringAnatomySourceValidator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM22SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM23SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM24SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM25SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM26SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM27SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM28NestedPortCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM28SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM29SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM32SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34DrawingPrimitiveCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34FunctionCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaParserContinuityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalGraphicOccurrenceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaProfileBindingSourceValidatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaProjectionPresentationComponentKnowledgeIntegrationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM40ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41SpatialGoldenAssertions.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DerivedEngineeringContextDeriverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DomainRelationVerbCompilationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringAnatomyAuthorityAuditTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringAnatomyLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringCapabilityFactPromoterTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityNetworkCompilationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConstraintEvaluatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringImpactConsequenceCalculatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M0ConformanceExamplesTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M2GeometryBackendExamplesTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M38DrawingAuthorityAuditTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M3ExternalDomainProofExamplesTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41GeometryQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineGenerator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineModels.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineProjector.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineVerifier.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationPublicationValidatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionConstructCompilationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayoutTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ReadingOrderProjectionSelectionTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RegionOccurrenceCompilationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RouteIntentLowererTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/AthenaM18LinkingLoweringProofTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/AthenaM18RepositoryProofCorpusTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/AthenaM34FunctionPlacementCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/CanonicalSemanticIdentityBuilderTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutConstraintLowererTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinderTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinkerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ViewAndSheetAuthorityCompilationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/representation/M34CabinetRenderPathProof.kt`
- `kernel/compiler/src/test/resources/knowledge-packages/valid-standards-mapping/payload/automationml-map.txt`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionPolicyModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`
- `kernel/engineering-model/README.md`
- `kernel/engineering-model/README.zh-CN.md`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextModel.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringAnatomyModels.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringCapabilityFactModel.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringConstraintEvaluationModel.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringImpactConsequenceModel.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringKnowledgeStateModel.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringReality.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringValueModels.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/DerivedEngineeringContextContractTest.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringAnatomyModelsTest.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringCapabilityFactContractTest.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringConstraintEvaluationContractTest.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringImpactConsequenceContractTest.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringKnowledgeStateContractTest.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringRealityTest.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringValueModelsTest.kt`
- `kernel/language/docs/future-syntax-landing-zone.md`
- `kernel/language/README.md`
- `kernel/language/README.zh-CN.md`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaEngineeringAnatomySyntaxTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageContractTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageProvenanceTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM34FunctionPlacementSyntaxTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM34SymbolSyntaxTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/ViewDeclarationParserTest.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`
- `kernel/package-runtime/build.gradle.kts`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactory.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceModels.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingResolverSelectionTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProof.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProofTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSetTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactoryTest.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorTest.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/PhysicalInstallationContractResolverTest.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/PhysicalInstallationTopologyCompilerTest.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/PhysicalTraitModelContractTest.kt`
- `kernel/plugins/plugin-api/build.gradle.kts`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaComponentKnowledgePluginModel.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSchemaModel.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPresentationPackContracts.kt`
- `kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiContributionContractTest.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistryTest.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionRealityTest.kt`
- `kernel/runtime/build.gradle.kts`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningContextAssembly.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningSessionRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaComponentKnowledgeRuntimeModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaComponentKnowledgeRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphProjection.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSceneMapper.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspection.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroAcceptanceModels.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroCatalogResolver.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroDefinitionLoader.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMutationReviewService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningContextAssemblyTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningSessionRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaComponentKnowledgeRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphProjectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaM14ProofCorpusTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaM16ProofSliceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeM41SpatialProofTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionAuthoritySelectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticCommitServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticHistoryStateServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeServiceTest.kt`
- `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGenerator.kt`
- `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculator.kt`
- `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGenerator.kt`
- `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGeneratorTest.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculatorTest.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticHistorySummaryGeneratorTest.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGeneratorTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`
- `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`
- `kernel/validation/src/test/kotlin/com/engineeringood/athena/semantics/core/EngineeringFunctionValidationTest.kt`
- `kernel/validation/src/test/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidatorTest.kt`
- `settings.gradle.kts`
- `ide/theia-frontend/scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-governed-entity-preview.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-governed-relationship-preview.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-product-smoke-wiring.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-revision-guard-apply.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-sample-project.test.mjs`
- `ide/theia-frontend/scripts/athena-m31-semantic-relationship-migration.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-cabinet-first-authoring-ux.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-create-entity-panel.test.mjs`
- `ide/theia-frontend/scripts/athena-m32-graph-view-taxonomy.test.mjs`
- `ide/theia-frontend/scripts/athena-m39-product-proof-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-m40-product-proof-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-m41-product-proof-contract.test.mjs`
- `ide/theia-frontend/scripts/athena-semantic-macro-model.test.mjs`
- `ide/theia-frontend/scripts/athena-semantic-macro-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`
- `ide/theia-frontend/scripts/athena-theia-editor-navigation.test.mjs`
- `ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-authoring-revision-guard.ts`
- `ide/theia-frontend/src/browser/athena-component-knowledge-protocol.ts`
- `ide/theia-frontend/src/browser/athena-component-panel-model.ts`
- `ide/theia-frontend/src/browser/athena-component-panel-widget.tsx`
- `ide/theia-frontend/src/browser/athena-frontend-module.ts`
- `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`
- `ide/theia-frontend/src/browser/athena-graph-command-intent-protocol.ts`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`
- `ide/theia-frontend/src/browser/athena-graph-workbench-types.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/athena-guided-connection-model.ts`
- `ide/theia-frontend/src/browser/athena-inspector-model.ts`
- `ide/theia-frontend/src/browser/athena-interaction-adapter-model.ts`
- `ide/theia-frontend/src/browser/athena-language-definition.ts`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-product-contribution.ts`
- `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-macro-catalog-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-macro-model.ts`
- `ide/theia-frontend/src/browser/athena-semantic-macro-protocol.ts`
- `ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
- `ide/theia-frontend/src/browser/athena-source-mutation-protocol.ts`
- `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/athena-m41-product-proof-contract.test.mjs`
- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`
- `ide/theia-product/scripts/verify-athena-m29-sample-project.js`
- `ide/theia-product/scripts/verify-athena-m31-sample-project.js`
- `ide/theia-product/scripts/verify-athena-m32-sample-project.js`
- `ide/theia-product/scripts/verify-athena-reuse-catalog.js`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-grammar-corpus.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-incomplete-source.test.mjs`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/connect.txt`
- `ide/tree-sitter-athena/test/corpus/device.txt`
- `ide/tree-sitter-athena/test/corpus/installation.txt`
- `ide/tree-sitter-athena/test/corpus/layout.txt`
- `ide/tree-sitter-athena/test/corpus/package-import.txt`
- `ide/tree-sitter-athena/test/corpus/port.txt`
- `ide/tree-sitter-athena/test/corpus/system.txt`
- `ide/tree-sitter-athena/test/fixtures/m34-function-placement.athena`
- `ide/tree-sitter-athena/test/fixtures/m34-representation-vocabulary.athena`
- `ide/tree-sitter-athena/test/fixtures/m37-grouped-interface.athena`
- `ide/tree-sitter-athena/test/incomplete/bare-import.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/bare-package.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/dangling-connect.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/incomplete-import.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/incomplete-package.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/unclosed-device-block.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/unclosed-system.athena.txt`
- `ide/tree-sitter-athena/test/incomplete/unterminated-string.athena.txt`
- `ide/tree-sitter-athena/tree-sitter-athena.wasm`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapter.kt`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementBasicValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementCycleValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementReferenceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementValidationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaGeneratedRepresentationBoundaryVerifier.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalGraphicOccurrenceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfileBindingSourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialBinder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialSubjects.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSourceResourceSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolGraphicStyles.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceFormatter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerPipelineModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerRenderingModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/DerivedEngineeringContextDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ElectricalKnowledgePackConstants.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringCapabilityFactPromoter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringConstraintEvaluator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringImpactConsequenceCalculator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GeometryIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaComponentKnowledgeContextBuilder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaComponentKnowledgeResolutionModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaComponentKnowledgeResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolutionModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPublicationValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProfessionalDrawingRouteHardRules.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSemanticComparisonCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RouteIntentLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutConstraintLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticLayoutHintBinder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerComponentKnowledgeIntegrationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerSpatialPipelineTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaComponentKnowledgeResolverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaGroupedConnectLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM17ParserParityProofTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM20SampleProjectCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM21SampleProjectCompilerTest.kt`

### Change Log

- 2026-08-04: Created through BMad create-story from final M42 sprint, epics, PRD, architecture,
  CodeGraph blast radius, current code, and git context; status set to ready-for-dev.
- 2026-08-05: Installed exact Engineering Values and Entity/Function/Port/Structure anatomy, migrated
  active authority end to end, deleted incompatible legacy product paths and stale proofs, and passed
  the full sequential verification matrix.
