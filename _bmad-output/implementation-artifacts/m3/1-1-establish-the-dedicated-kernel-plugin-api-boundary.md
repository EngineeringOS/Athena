---
baseline_commit: 87b1342e4f95f523c84712c1841aebcaaee7975b
---

# Story 1.1: Establish The Dedicated Kernel Plugin API Boundary

Status: in-progress

## Story

As a platform engineer,
I want stable plugin-facing contracts moved into a dedicated kernel API boundary,
so that extensions can target an official SPI instead of depending on compiler internals.

## Acceptance Criteria

1. Given the current workspace still hosts plugin contracts inside compiler implementation areas, when the M3 SPI boundary is introduced, then Athena adds a dedicated kernel plugin API module or equivalent stable kernel-owned boundary for plugin-facing contracts, and extensions no longer need to depend on compiler implementation packages to exist.
2. Given the new API boundary is created, when stable plugin-facing contracts are moved, then manifest metadata, extension-point declarations, and other durable hosted plugin contracts live in that boundary, and contracts that are compiler-private or runtime-private do not remain mixed into the stable SPI surface.
3. Given the SPI boundary must preserve earlier milestones, when module dependencies are reviewed, then the new boundary keeps `Engineering IR`, runtime ownership, and projection ownership inside kernel-owned modules, and M0, M1, and M2 proof modules continue to build without regressing their semantic authority rules.
4. Given the new API boundary is implemented, when the standard Java `25` build and module checks are executed, then the workspace builds successfully with the dedicated plugin API boundary in place, and shared build versions continue to be managed through `gradle/libs.versions.toml`.

## Tasks / Subtasks

- [x] Add the dedicated kernel SPI module without introducing host logic. (AC: 1, 4)
  - [x] Register `:kernel:plugin-api` in `settings.gradle.kts` and keep it grouped under `kernel/`.
  - [x] Create `kernel/plugin-api/build.gradle.kts`, `README.md`, and `README.zh-CN.md` using the existing lightweight Kotlin/JVM module pattern.
  - [x] Keep shared versions and plugins sourced through `gradle/libs.versions.toml`; do not introduce ad-hoc version declarations.
- [x] Move only the durable plugin-facing contracts into the new boundary. (AC: 1, 2, 3)
  - [x] Relocate the stable SPI types currently buried under `kernel/compiler/.../plugin/` into `:kernel:plugin-api`.
  - [x] Give the moved SPI a package root that no longer advertises compiler ownership, such as `com.engineeringood.athena.plugin`.
  - [x] Keep compiler-private discovery, approval, and orchestration types out of the SPI module.
- [x] Untangle extension-facing contracts from compiler-private dependencies. (AC: 1, 2, 3)
  - [x] Ensure the new SPI module does not depend on `:kernel:compiler`.
  - [x] If a plugin-facing contract currently references compiler-private types such as `CompilerSourceDocument`, split or reshape that contract so the extension API remains compiler-independent.
  - [x] Preserve ownership by referencing existing kernel models like `EngineeringDocument`, `StableSemanticIdentity`, layout contracts, and geometry contracts from their current modules instead of rehosting them in the SPI.
- [x] Rewire current consumers onto the dedicated API boundary. (AC: 1, 2, 3)
  - [x] Update `:extensions:domain-electrical` to depend on `:kernel:plugin-api` instead of compiler internals for stable plugin contracts.
  - [x] Update any runtime or test imports that should point to the stable SPI package, while leaving host-only logic for later M3 stories.
  - [x] Update the `ServiceLoader` registration path if the public `AthenaPlugin` type moves to a new package.
- [x] Preserve milestone continuity and document the boundary. (AC: 2, 3, 4)
  - [x] Update kernel and module READMEs so the new SPI boundary is visible in the grouped workspace map.
  - [x] Keep M0, M1, and M2 behavior intact; do not fold plugin-host, approval workflow, pass-pipeline redesign, or proof-domain refactors into Story `1.1`.
  - [x] Add focused tests that prove extensions can compile against the new SPI boundary without importing compiler implementation packages.

## Dev Notes

### Story Intent

- Story `1.1` is the entry point for M3. Its job is to create the stable extension-facing boundary before plugin hosting, approval flow splitting, pass-pipeline refactoring, or proof-domain reshaping proceed.
- The architectural risk is clear in the current workspace: `:extensions:domain-electrical` and runtime-facing code import contracts directly from `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/*`.
- This story should fix the API ownership problem, not solve every plugin concern at once.
- Story `1.2` owns expansion and clarification of stable domain schema and contribution contracts.
- Story `1.3` owns separating plugin source enumeration from host approval.
- Story `1.4` owns lifecycle and inventory inspection as an explicit hosted concern.
- Stop once the stable plugin API boundary exists and current extension code can target it cleanly.

### Architecture Guardrails

- AD-1: The kernel stays generic. Domain meaning, domain validation, and domain rendering remain extension-owned.
- AD-2: The authored DSL remains structurally generic. M3 extensions contribute interpretation, not grammar changes.
- AD-3: Stable plugin contracts belong in a dedicated kernel API boundary rather than compiler implementation packages.
- AD-4 is not implemented here. `ServiceLoader` remains the current hosted source, but source-versus-approval separation belongs to Story `1.3`.
- AD-5 is not implemented here. Do not refactor the compiler into the named pass pipeline in Story `1.1`.
- AD-6 remains in force. Generic validation stays kernel-owned; this story must not move domain validation into `:kernel:validation`.
- AD-7 remains in force. Renderer orchestration stays kernel-owned and is not part of this boundary extraction.
- AD-8 and AD-10 matter later. `:extensions:domain-electrical` is the first real proof domain, but this story only rewires its dependency surface.

### Current Code Reality

- Current stable-looking plugin contracts are mixed with compiler-private concerns under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/`.
- Extension code currently imports compiler-owned packages, for example:
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
  - `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- Runtime code currently also imports plugin types from compiler internals:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- The compiler README still describes plugin contracts and discovery as a compiler responsibility, which is the exact ownership smell this story begins to correct.

### Stable SPI Scope For This Story

- Expected stable SPI candidates:
  - `AthenaPlugin`
  - typed extension contracts such as `AthenaDomainPlugin` and `AthenaViewDefinitionContributor`
  - manifest vocabulary and compatibility models such as `AthenaPluginManifest`, `AthenaPluginType`, `AthenaExtensionPoint`, `AthenaPluginOwnershipClaim`, and `CoreVersionRange`
  - only those contribution or context models that extensions must compile against in order to implement the current plugin contract
- Expected non-SPI or still-host-owned types for later stories:
  - `AthenaPluginDiscovery`
  - `AthenaPluginDiscoveryReport`
  - `AthenaApprovedPluginInventory`
  - `ApprovedAthenaPlugin`
  - `RejectedAthenaPluginCandidate`
  - `AthenaPluginValidator`
  - host approval and runtime inspection policies
- If a type is consumed only by compiler-host approval/orchestration and not by extension implementations, keep it out of `:kernel:plugin-api`.

### Technical Requirements

- Prefer `:kernel:plugin-api` now and defer `:kernel:plugin-host` until Story `1.3` or `1.4`.
- The stable SPI package should be easy to read and module-neutral. `com.engineeringood.athena.plugin` is the preferred target unless an equally clear package is justified.
- Do not keep the public SPI under `com.engineeringood.athena.compiler.plugin` once moved; that would preserve the wrong ownership signal.
- Do not duplicate `Engineering IR`, `Layout IR`, `Geometry IR`, or runtime-owned models in the SPI. Reference existing kernel contracts from their current modules.
- If the current lowering or validation context models embed compiler-private objects, reshape them so extension implementations can compile without a dependency edge back to `:kernel:compiler`.
- Keep all core Kotlin classes introduced or moved in the stable SPI documented with KDoc.
- Add English and Chinese README files for the new module.

### Architecture Compliance

- The dedicated SPI boundary must reduce extension dependence on compiler internals, not merely rename packages.
- The new boundary must keep the kernel small by moving only durable contracts and by refusing to absorb host approval/discovery orchestration prematurely.
- `Engineering IR` remains canonical. Plugin contracts may reference canonical kernel models, but they may not become new semantic authorities.
- Runtime ownership remains in `:kernel:runtime`; plugin API extraction must not move workspace, command, or lifecycle ownership into extensions.
- Projection ownership remains in `:kernel:layout-model`, `:kernel:geometry-model`, and `:kernel:svg-renderer`; Story `1.1` must not relocate view/render orchestration.

### Library / Framework Requirements

- Use the existing repo toolchain and dependency management:
  - Java `25`
  - Kotlin `2.4.0`
  - Compose and other shared versions from `gradle/libs.versions.toml`
- Do not add new external libraries for Story `1.1`.
- Keep the module on the same Kotlin/JVM and JUnit conventions already used by other kernel modules.

### File Structure Requirements

- Expected new paths:
  - `kernel/plugin-api/build.gradle.kts`
  - `kernel/plugin-api/README.md`
  - `kernel/plugin-api/README.zh-CN.md`
  - `kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/**`
  - `kernel/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/**`
- Expected update paths:
  - `settings.gradle.kts`
  - `kernel/README.md`
  - `kernel/README.zh-CN.md`
  - `kernel/compiler/build.gradle.kts`
  - `kernel/runtime/build.gradle.kts`
  - `extensions/domain-electrical/build.gradle.kts`
  - `extensions/domain-electrical/src/main/resources/META-INF/services/**`
  - current plugin contract files under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/**`
- Files whose current role should be preserved in this story:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
    - Keep compiler orchestration behavior intact. Story `1.1` is about ownership boundaries, not pass redesign.
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
    - Runtime host inspection logic still exists, but only stable imports should move to the new API module in this story.
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
    - Preserve its current domain behavior; only migrate it off compiler-internal SPI usage.
- Explicit non-goals:
  - no `:kernel:plugin-host` yet
  - no pass-pipeline refactor
  - no plugin lifecycle UI or remote loading
  - no `domain-dummy`
  - no proof-corpus publication

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:plugin-api:test`
  - `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Run Gradle verification sequentially on Windows. Do not run concurrent Gradle build/test commands; this repo has already hit repeated Windows locking/process issues from parallel execution.
- Required proof tests:
  - module marker test for `:kernel:plugin-api`
  - focused tests that stable SPI types resolve from the new module/package
  - regression tests proving `:extensions:domain-electrical` compiles and loads through the moved public `AthenaPlugin` contract
  - regression checks proving compiler-private discovery/approval types were not accidentally re-exported as the public SPI

### Project Structure Notes

- This repo now groups modules by responsibility: `apps`, `ui`, `kernel`, and `extensions`. Story `1.1` must preserve that grouping.
- The new plugin API boundary belongs under `kernel/` because it is core-owned and semantically authoritative as a contract surface.
- Keep module names straightforward. `plugin-api` is clearer than inventing a vague name or leaving public SPI buried under `compiler`.
- Update module READMEs when the grouped topology changes; the user explicitly wants every module and group to stay documented.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#epic-1-prove-the-hosted-extensibility-platform]
- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-11-establish-the-dedicated-kernel-plugin-api-boundary]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-3---stable-plugin-contracts-live-in-a-dedicated-kernel-api-boundary]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-4---hosted-plugin-discovery-is-a-two-layer-concern-source-then-approval]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]
- [Source: extensions/domain-electrical/src/main/resources/META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin]
- [Source: settings.gradle.kts]
- [Source: gradle/libs.versions.toml]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- CodeGraph exploration of current plugin contract and discovery ownership
- `git log -5 --oneline`
- module build file inspection for `kernel/compiler`, `kernel/runtime`, and `extensions/domain-electrical`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugin-api:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain clean :kernel:compiler:compileKotlin`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added `:kernel:plugin-api` as the dedicated extension-facing module with KDoc-backed SPI classes, marker coverage, and focused boundary tests.
- Moved stable plugin contracts, manifest models, compatibility models, validation models, and plugin-facing lowering/validation contexts into `com.engineeringood.athena.plugin`.
- Kept compiler-private host responsibilities in `kernel/compiler/plugin/*`, including discovery, approval inventory, validator orchestration, and domain coordination.
- Removed the extension-facing dependency on compiler internals by converting plugin contexts to `AthenaSourceDocument` while keeping `CompilerSourceDocument` as a compiler-owned facade model.
- Rewired `domain-electrical`, `runtime`, compiler tests, and service registration to the new SPI package and updated grouped READMEs to reflect the ownership split.
- Verified the boundary with sequential Java 25 runs for `:kernel:plugin-api:test`, `:extensions:domain-electrical:test`, `:kernel:compiler:test`, `:kernel:runtime:test`, and a full `build`.

### File List

- `_bmad-output/implementation-artifacts/m3/1-1-establish-the-dedicated-kernel-plugin-api-boundary.md`
- `extensions/domain-electrical/README.md`
- `extensions/domain-electrical/README.zh-CN.md`
- `extensions/domain-electrical/build.gradle.kts`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/main/resources/META-INF/services/com.engineeringood.athena.plugin.AthenaPlugin`
- `extensions/domain-electrical/src/main/resources/META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin` (deleted)
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `kernel/README.md`
- `kernel/README.zh-CN.md`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageLoader.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaCoreRuntime.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsModel.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidationModel.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginDiscoveryTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/plugin-api/README.md`
- `kernel/plugin-api/README.zh-CN.md`
- `kernel/plugin-api/build.gradle.kts`
- `kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaCoreRuntime.kt`
- `kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
- `kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`
- `kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginManifestModel.kt`
- `kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginValidationModel.kt`
- `kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/PluginApiModuleMarker.kt`
- `kernel/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiBoundaryTest.kt`
- `kernel/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiModuleMarkerTest.kt`
- `kernel/runtime/build.gradle.kts`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `settings.gradle.kts`

### Change Log

- 2026-07-07: Established `:kernel:plugin-api`, moved stable SPI into `com.engineeringood.athena.plugin`, rewired current consumers, updated service registration, and verified the full Java 25 build.

### Review Findings

- [ ] [Review][Patch] `domain-electrical` still depends directly on `:kernel:compiler`, so the proof extension can continue compiling against compiler internals even after the SPI extraction and the boundary is not actually enforced. Remove the unnecessary compiler module edge and keep the extension compiling against `:kernel:plugin-api`, `:kernel:runtime`, and other truly needed kernel-owned modules only. [extensions/domain-electrical/build.gradle.kts:6]
- [ ] [Review][Patch] Host-only compatibility and validation models were promoted into the public `com.engineeringood.athena.plugin` package even though they are consumed only by compiler/runtime host code (`AthenaCoreVersion`, `AthenaCoreRuntime`, `PluginValidationDiagnostic`, `PluginValidationResult`, `PluginValidationRuleId`, `PluginValidationSeverity`). This keeps compiler-private and runtime-private concerns mixed into the stable SPI surface, which violates Story 1.1 AC2 and weakens the boundary. Relocate those host-side models out of `:kernel:plugin-api` and leave only true extension implementation contracts in the stable SPI package. [kernel/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaCoreRuntime.kt:3]
