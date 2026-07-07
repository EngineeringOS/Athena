---
baseline_commit: 1339722f55143519dbd7ee0ec0a06049159302f1
---

# Story 1.2: Define Stable Domain Schema And Contribution Contracts

Status: done

## Story

As an extension author,
I want typed SPI contracts for domain schema and plugin contributions,
so that I can declare domain meaning, validation, compiler participation, and rendering through one governed contract set.

## Acceptance Criteria

1. Given the dedicated plugin API boundary exists, when M3 contribution contracts are defined, then Athena provides typed contracts for plugin-declared domain schema such as domain entities, properties, ports, and related semantic descriptors, and those contracts remain generic rather than embedding proof-domain nouns like `Motor` or `Lamp`.
2. Given plugin-hosted domains must contribute more than schema, when the SPI is completed for M3, then Athena provides stable contracts for validation contributions, compiler pass contributions, and renderer contributions, and those contributions are inspectable by the kernel host without ad hoc reflection rules.
3. Given the authored DSL must remain generic in M3, when the contribution contracts are reviewed, then the contracts support plugin-defined interpretation rather than plugin-defined grammar, and the kernel parser remains responsible for generic language structure.
4. Given the stable contribution contracts are implemented, when the standard Java `25` build and API tests are executed, then the workspace builds successfully and the typed SPI is available to extensions through the dedicated plugin API boundary, and the SPI is documented clearly enough for later proof domains to consume.

## Tasks / Subtasks

- [x] Add generic domain schema contracts to `:kernel:plugins:plugin-api`. (AC: 1, 3, 4)
  - [x] Introduce typed schema descriptors for domain entities, properties, ports, and related semantic capabilities under `com.engineeringood.athena.plugin`.
  - [x] Prefer straightforward, generic names such as `AthenaDomainSchema`, `AthenaDomainEntitySchema`, `AthenaDomainPropertySchema`, and `AthenaDomainPortSchema` unless current code inspection reveals a clearer collision-free naming fit.
  - [x] Keep the schema surface generic and extension-facing; do not encode electrical nouns, dummy-proof nouns, or plugin-defined grammar forms into the stable SPI.
- [x] Add typed stable contribution contracts for validation, compiler-stage participation, and rendering. (AC: 2, 3, 4)
  - [x] Introduce inspectable contribution models or interfaces for domain validation, compiler-stage participation, and renderer-facing contribution intent inside `:kernel:plugins:plugin-api`.
  - [x] Ensure the contribution surface is typed and host-inspectable without depending on ad hoc reflection over arbitrary plugin methods.
  - [x] Define contribution declarations in a way that later stories can execute them through governed stages, without implementing Story `1.3`, `1.4`, or `1.5` early.
- [x] Rewire the first proof extension and boundary tests onto the new contracts. (AC: 1, 2, 4)
  - [x] Update `:extensions:domain-electrical` to publish its schema and declared contributions through the new SPI contracts while preserving current proof behavior.
  - [x] Add focused `plugin-api`, compiler, and extension tests that prove the typed schema and contribution contracts resolve through `:kernel:plugins:plugin-api`.
  - [x] Update SPI-facing documentation so later proof domains can follow the new contract shape without inspecting compiler internals.
- [x] Preserve the milestone boundary while expanding the SPI. (AC: 1, 2, 3, 4)
  - [x] Keep parser ownership, canonical semantic ownership, runtime ownership, and renderer orchestration in their current kernel-owned modules.
  - [x] Do not fold source-versus-approval splitting, lifecycle inspection, or the explicit named pass-pipeline refactor into Story `1.2`.
  - [x] Do not use Story `1.2` to absorb the deferred Story `1.1` boundary-cleanup refactor unless a narrow compatibility adjustment is strictly required for the new contracts.

## Dev Notes

### Story Intent

- Story `1.2` is the first expansion step after the dedicated SPI boundary from Story `1.1`.
- Its job is to make `:kernel:plugins:plugin-api` expressive enough for real hosted domain contracts, while still stopping short of host restructuring or pipeline execution changes.
- This story is about contract definition and proof-extension adoption, not yet about full hosted source/approval splitting, lifecycle services, or the final pass execution model.
- Story `1.3` owns hosted plugin source-versus-approval architecture.
- Story `1.4` owns hosted lifecycle and inventory inspection surfaces.
- Story `1.5` owns the explicit compiler pass-pipeline refactor.

### Architecture Guardrails

- AD-1: The kernel owns only generic semantics and orchestration. Domain schema, domain validation, and domain rendering remain extension-owned.
- AD-2: The authored DSL stays generic. Plugins extend interpretation through schema and contribution contracts; they do not extend grammar.
- AD-3: Stable plugin contracts live in `:kernel:plugins:plugin-api`, and the durable SPI includes domain schema contracts plus validation, compiler-pass, and renderer contribution contracts.
- AD-4 is not implemented here. Do not split hosted plugin loading into source and approval layers in Story `1.2`.
- AD-5 is not implemented here. Story `1.2` may define declared compiler-stage contribution contracts, but it must not refactor the compiler into the final named pass pipeline.
- AD-6 remains in force. Kernel validation stays separate from domain validation; do not move domain rules into `:kernel:validation`.
- AD-7 remains in force. Renderer orchestration stays kernel-owned; renderer contributions are extension intent, not backend ownership.
- AD-8 remains in force. `:extensions:domain-electrical` is the first real proof domain and should be the reference adopter of the new contracts.

### Current Code Reality

- `:kernel:plugins:plugin-api` currently exposes moved manifest models, extension-point vocabulary, compatibility models, typed plugin interfaces, and plugin-facing lowering or validation contexts.
- The current stable SPI files are:
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginManifestModel.kt`
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginValidationModel.kt`
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaCoreRuntime.kt`
- `AthenaDomainPlugin` currently exposes lowering and validation behavior directly, but there is no generic typed schema model that declares domain entities, properties, or ports.
- `ElectricalRuntimeDomainPlugin` currently encodes its domain meaning directly inside implementation code in:
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- Runtime-owned command and view contributor contracts still live in:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
  They are not the primary target of Story `1.2`.
- Host inspection of plugin capabilities is still driven largely by manifest extension points and interface checks. Story `1.2` should make schema and contribution declarations typed and inspectable, not add more implicit host inference.

### Stable SPI Scope For This Story

- Expected stable schema surface:
  - a generic domain schema root such as `AthenaDomainSchema`
  - typed entity, property, port, and related capability descriptors with generic naming
  - extension-facing contracts that let a proof domain publish schema without leaking domain nouns into the kernel
- Expected stable contribution surface:
  - validation contribution contracts
  - compiler-stage or pass contribution declarations
  - renderer-facing contribution contracts
  - inspectable descriptors that a future host can enumerate deterministically
- Expected non-goals for this story:
  - hosted source-versus-approval architecture
  - plugin lifecycle state or shutdown services
  - final compiler pass-pipeline execution refactor
  - remote or dynamic plugin loading
  - runtime command or runtime view contract redesign
- If a type is only needed by host approval, lifecycle, or runtime inspection, it should not be invented as new public SPI for Story `1.2`.

### Technical Requirements

- The schema contracts must describe domain meaning over generic authored structures. They must not introduce plugin-defined grammar or parser hooks.
- The contribution contracts must be inspectable through typed models, properties, or declared interfaces. The host must not need ad hoc reflection conventions to discover what a plugin contributes.
- Compiler-stage declarations should express contribution intent in a stable, generic way that Story `1.5` can later execute through explicit pass stages.
- Renderer contribution contracts must stay downstream of canonical semantics, layout, and geometry boundaries; they must not become new semantic authorities.
- New or changed core Kotlin classes in `:kernel:plugins:plugin-api` must have KDoc.
- Keep shared toolchain and version ownership in `gradle/libs.versions.toml`; do not introduce ad hoc versions or new external libraries.
- Because Story `1.1` review follow-up refactors were intentionally deferred, Story `1.2` must not widen the current host-only leakage in the SPI while adding the new stable contracts.

### Architecture Compliance

- `Engineering IR` remains the only canonical semantic authority. Schema and contribution contracts may describe interpretation over canonical models, but they may not replace them.
- The parser remains in `:kernel:language` and continues to own generic authored structure.
- Runtime remains the owner of lifecycle, command execution, and hosted service orchestration from M1.
- Layout, geometry, and renderer orchestration remain downstream consequences from M2 and must not become plugin-owned semantic truth.
- The stable SPI should become easier for a second proof domain such as `domain-dummy` to consume, not more electrical-shaped.

### Library / Framework Requirements

- Use the existing repo toolchain and dependency management:
  - Java `25`
  - Kotlin `2.4.0`
  - shared versions from `gradle/libs.versions.toml`
- Do not add new external libraries for Story `1.2`.
- Keep the module on the same Kotlin/JVM and JUnit conventions already used by the kernel modules.

### File Structure Requirements

- Expected update paths:
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
  - new stable SPI files under `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/**`
  - `kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/**`
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
  - `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/**`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `kernel/plugins/plugin-api/README.md`
  - `kernel/plugins/plugin-api/README.zh-CN.md`
  - `extensions/domain-electrical/README.md`
  - `extensions/domain-electrical/README.zh-CN.md`
- Files whose current role should be preserved in this story:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
    - keep compiler orchestration behavior intact; do not force the Story `1.5` pipeline refactor here
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
    - keep runtime-owned hosted inspection and execution logic intact unless a narrow typed-contract adapter is required
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
    - preserve current proof behavior while expressing schema and contribution declarations through the stable SPI

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test`
  - `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Run Gradle verification sequentially on Windows. Do not run concurrent Gradle build or test commands in this repo.
- Required proof tests:
  - `plugin-api` tests for the new generic schema descriptors
  - `plugin-api` tests for validation, compiler-stage, and renderer contribution contracts
  - regression tests proving `domain-electrical` can publish typed schema and contribution declarations through the stable SPI
  - regression checks proving the contribution declarations are inspectable through typed contracts rather than host reflection conventions

### Previous Story Intelligence

- Story `1.1` established the dedicated `:kernel:plugins:plugin-api` boundary and proved the moved SPI builds on Java `25`.
- Story `1.1` also proved that plugin-facing source models needed to split away from compiler-private models such as `CompilerSourceDocument`.
- Review of Story `1.1` left two broader cleanup items intentionally deferred:
  - `domain-electrical` still carries a direct `:kernel:compiler` dependency
  - host-only compatibility and validation models still remain inside the public SPI package
- Those deferred items should not be quietly expanded inside Story `1.2`; keep this story focused on stable domain schema and contribution contracts.
- Group and module READMEs are expected to stay updated in both English and Chinese when the stable surface changes.

### Git Intelligence Summary

- `1339722 feat(m3): establish kernel plugin api boundary`
  - the current workspace already contains the new SPI boundary and the first M3 story artifacts
- `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - preserve explicit layout and geometry boundaries from M2 while adding renderer contribution contracts
- `ad382d8 Complete M1 runtime workspace and regroup modules`
  - preserve runtime ownership of lifecycle, orchestration, and command-backed semantic mutation from M1

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-12-define-stable-domain-schema-and-contribution-contracts]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-07/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-07/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-2---the-authored-dsl-stays-generic-domain-meaning-enters-through-plugin-schema]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-3---stable-plugin-contracts-live-in-a-dedicated-kernel-api-boundary]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-5---the-compiler-is-governed-as-a-named-pass-pipeline]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-6---kernel-validation-and-domain-validation-stay-separate]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-7---kernel-renderer-contracts-stay-generic-domains-contribute-only-domain-presentation]
- [Source: docs/usages/athena-workspace-summary.md]
- [Source: manifesto/docs/architecture/05-plugin.md]
- [Source: manifesto/docs/architecture/03-ir.md]
- [Source: kernel/plugins/plugin-api/README.md]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]
- [Source: _bmad-output/implementation-artifacts/m3/1-1-establish-the-dedicated-kernel-plugin-api-boundary.md]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `Get-Content .agents/skills/bmad-create-story/SKILL.md`
- `Get-Content .agents/skills/bmad-create-story/discover-inputs.md`
- `Get-Content .agents/skills/bmad-create-story/checklist.md`
- `Get-Content _bmad-output/planning-artifacts/epics-M3-2026-07-07.md`
- `Get-Content _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-07/prd.md`
- `Get-Content _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-07/addendum.md`
- `Get-Content _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md`
- `Get-Content docs/usages/athena-workspace-summary.md`
- `Get-Content manifesto/docs/architecture/05-plugin.md`
- `Get-Content manifesto/docs/architecture/03-ir.md`
- `Get-Content _bmad-output/implementation-artifacts/m3/1-1-establish-the-dedicated-kernel-plugin-api-boundary.md`
- `git log -5 --oneline`
- `codegraph explore "kernel plugin api domain schema validation contribution renderer contribution AthenaPlugin AthenaDomainPlugin AthenaRuntimePluginCommandContributor AthenaRuntimePluginViewContributor"`
- `Get-Content kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added generic domain schema models and typed contribution descriptors to `:kernel:plugins:plugin-api` with KDoc-backed extension-facing contracts.
- Extended `AthenaDomainPlugin` to publish stable schema, validation, compiler-stage, and renderer-facing contribution metadata without changing plugin type rules.
- Updated `ElectricalRuntimeDomainPlugin` to publish its current electrical proof schema and inspectable contribution declarations through the stable SPI while preserving existing lowering, validation, view, and runtime behavior.
- Added focused `plugin-api` and compiler contract tests that prove synthetic and electrical plugins can expose typed schema and contribution metadata through `:kernel:plugins:plugin-api`.
- Updated the English and Chinese README files for `:kernel:plugins:plugin-api` and `:extensions:domain-electrical` to document the expanded SPI surface.

### File List

- `_bmad-output/implementation-artifacts/m3/1-2-define-stable-domain-schema-and-contribution-contracts.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `kernel/plugins/plugin-api/README.md`
- `kernel/plugins/plugin-api/README.zh-CN.md`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSchemaModel.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaContributionModel.kt`
- `kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiContributionContractTest.kt`
- `extensions/domain-electrical/README.md`
- `extensions/domain-electrical/README.zh-CN.md`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`

### Change Log

- 2026-07-07: Created Story `1.2` with M3-specific contract, architecture, and implementation guardrails.
- 2026-07-07: Implemented stable generic domain schema and contribution contracts in `:kernel:plugins:plugin-api`, rewired the electrical proof plugin, and verified the full Java 25 build.


