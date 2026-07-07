---
baseline_commit: 1339722f55143519dbd7ee0ec0a06049159302f1
---

# Story 1.3: Split Hosted Plugin Discovery Into Source And Approval Layers

Status: done

## Story

As a platform engineer,
I want hosted plugin loading divided into source and approval layers,
so that ServiceLoader can remain the first source without becoming the permanent architecture or bypassing governance.

## Acceptance Criteria

1. Given the current JVM-first path already uses ServiceLoader, when M3 hosted plugin loading is refactored, then Athena exposes a plugin source layer that enumerates candidates and a separate approval layer that validates and admits candidates into the approved inventory, and ServiceLoader is treated as one source implementation rather than as the whole plugin architecture.
2. Given hosted plugins must remain governed, when candidate approval is executed, then Athena validates manifest compatibility, declared extension points, and stable contract conformance before a plugin becomes approved, and unapproved candidates cannot silently participate in compiler or runtime flows.
3. Given future local-directory and remote-URL loading are deferred but not forbidden, when the hosting architecture is reviewed, then the source layer can accept future source types without bypassing the same approval boundary, and M3 does not need to implement those future source types.
4. Given the source-versus-approval split is implemented, when the standard Java `25` build and hosted plugin checks are executed, then the workspace builds successfully and the existing ServiceLoader proof path still works through the new governed structure, and approved plugin ordering remains deterministic.

## Tasks / Subtasks

- [x] Add the dedicated hosted plugin module and move host-only discovery concerns into it. (AC: 1, 3, 4)
  - [x] Register `:kernel:plugins:plugin-host` in `settings.gradle.kts` and keep it grouped under `kernel/`.
  - [x] Create `kernel/plugins/plugin-host/build.gradle.kts`, `README.md`, and `README.zh-CN.md`.
  - [x] Update grouped kernel documentation so hosted plugin responsibilities no longer appear compiler-owned.
- [x] Split source enumeration from approval explicitly. (AC: 1, 2, 3)
  - [x] Introduce a hosted source layer that loads plugin implementations and materializes candidate records plus source-level rejections.
  - [x] Introduce a separate approval layer that validates candidates, enforces deterministic ordering, and produces the approved inventory.
  - [x] Keep `ServiceLoader` as the first concrete source implementation only.
- [x] Rewire compiler and runtime onto the shared hosted plugin boundary. (AC: 1, 2, 4)
  - [x] Update compiler imports and dependencies to consume hosted inventory and discovery reports from `:kernel:plugins:plugin-host`.
  - [x] Update runtime imports and dependencies to consume the same hosted boundary without re-implementing approval logic.
  - [x] Preserve current hosted behavior and deterministic plugin ordering while changing ownership.
- [x] Prove the split with focused tests and Java `25` verification. (AC: 2, 4)
  - [x] Move or add hosted plugin discovery and validator tests to the new hosted module.
  - [x] Keep compiler and runtime regression tests passing against the refactored ownership boundary.
  - [x] Run sequential Windows verification and record the results here.

## Dev Notes

### Story Intent

- Story `1.3` is the first host-architecture refactor after the stable SPI and contribution contracts from Stories `1.1` and `1.2`.
- Its job is to separate plugin enumeration from plugin approval so the current `ServiceLoader` proof remains valid without hard-coding future architecture around it.
- This story is not the lifecycle-inspection story. Story `1.4` owns governed lifecycle and inventory inspection surfaces.
- This story is also not the pass-pipeline story. Story `1.5` owns the explicit compiler pass refactor.

### Architecture Guardrails

- AD-3: Stable extension-facing contracts stay in `:kernel:plugins:plugin-api`; do not move hosted internals into the SPI.
- AD-4: Hosted plugin discovery is explicitly a two-layer concern: source, then approval.
- AD-5 is not implemented here. Do not use Story `1.3` to refactor the compiler into the full named pass pipeline.
- Runtime ownership from M1 remains intact. Hosted plugin approval supports runtime and compiler, but it does not make plugins owners of lifecycle orchestration.
- Keep the kernel generic. This host split must not introduce proof-domain nouns into kernel host logic.

### Current Code Reality

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt` currently mixes:
  - plugin source enumeration
  - manifest-read failure handling
  - candidate record construction
  - approval validation
  - duplicate-id rejection
  - approved inventory assembly
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt` is host approval logic, not compiler pass logic.
- `AthenaCompiler` and `AthenaHostedPluginRuntimeServices` both depend on those compiler-owned host types even though discovery and approval are not truly compiler-private concerns.
- The architecture spine already declares `:kernel:plugins:plugin-host` as the right home for plugin sources, approval, inventory, and hosted inspection.

### Implementation Direction

- Preferred target module: `:kernel:plugins:plugin-host`.
- Preferred package root: `com.engineeringood.athena.plugin.host`.
- Expected hosted responsibilities in this story:
  - plugin source contracts and the JVM-first `ServiceLoader` implementation
  - candidate, approved, rejected, inventory, and discovery-report models
  - approval validator and approval orchestration
  - a thin discovery facade that composes source enumeration plus approval
- Compiler-specific domain coordination stays in `:kernel:compiler`.
- Runtime-owned hosted inspection surfaces stay in `:kernel:runtime`, but they should consume host models from `:kernel:plugins:plugin-host`.

### Technical Requirements

- Preserve deterministic ordering of candidates, approved plugins, and rejected candidates.
- Keep rejection causes inspectable; source failures and unreadable manifests must remain visible in the hosted report.
- Preserve the current validation rules for manifest structure, plugin type conformance, extension-point legality, core compatibility, and forbidden ownership claims.
- Do not implement local-directory loading, remote URL loading, or hot load/unload in Story `1.3`.
- Add KDoc for the new hosted classes and interfaces.
- Keep build versions sourced from `gradle/libs.versions.toml` and continue using Java `25`.

### Architecture Compliance

- The source layer must be open to future source implementations without bypassing the approval layer.
- Approval remains a core-owned governance function. Plugins are never active merely because they were found on a source.
- `Engineering IR` remains canonical; this story changes host structure only, not semantic authority.
- Compiler and runtime may share the same approved inventory, but neither should own the source-versus-approval split as an internal private detail after this story.

### File Structure Requirements

- Expected new paths:
  - `kernel/plugins/plugin-host/build.gradle.kts`
  - `kernel/plugins/plugin-host/README.md`
  - `kernel/plugins/plugin-host/README.zh-CN.md`
  - `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/**`
  - `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/**`
- Expected update paths:
  - `settings.gradle.kts`
  - `kernel/README.md`
  - `kernel/README.zh-CN.md`
  - `kernel/compiler/README.md`
  - `kernel/runtime/README.md`
  - `kernel/compiler/build.gradle.kts`
  - `kernel/runtime/build.gradle.kts`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
  - affected compiler and runtime tests
- Expected removed or relocated paths:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Run Gradle verification sequentially on Windows. Do not run concurrent Gradle commands in this repo.

### Previous Story Intelligence

- Story `1.1` created `:kernel:plugins:plugin-api` but intentionally left host discovery and approval in compiler-owned code.
- Story `1.2` expanded the stable SPI with generic schema and contribution contracts while deliberately not changing host architecture.
- The M3 architecture spine already names `:kernel:plugins:plugin-host` as the durable home for hosted plugin source, approval, inventory, and inspection concerns.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-13-split-hosted-plugin-discovery-into-source-and-approval-layers]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-3---stable-plugin-contracts-live-in-a-dedicated-kernel-api-boundary]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-4---hosted-plugin-discovery-is-a-two-layer-concern-source-then-approval]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: _bmad-output/implementation-artifacts/m3/1-1-establish-the-dedicated-kernel-plugin-api-boundary.md]
- [Source: _bmad-output/implementation-artifacts/m3/1-2-define-stable-domain-schema-and-contribution-contracts.md]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `codegraph node kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt`
- `codegraph node kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `codegraph node kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `Get-Content _bmad-output/planning-artifacts/epics-M3-2026-07-07.md`
- `Get-Content _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md`

### Completion Notes List

- Added `:kernel:plugins:plugin-host` as the dedicated host-governance module for plugin source, approval, discovery reporting, and approved inventory models.
- Split the old compiler-owned discovery blob into explicit layers: `AthenaPluginCandidateSource` for source enumeration and `AthenaPluginApprovalService` for governed approval.
- Preserved the JVM-first `ServiceLoader` proof path as one hosted source implementation rather than the whole plugin architecture.
- Rewired compiler and runtime to consume hosted inventory, discovery reports, and validator logic from `com.engineeringood.athena.plugin.host`.
- Moved host-specific discovery and validator tests into `:kernel:plugins:plugin-host` and kept compiler/runtime regressions green on Java `25`.

### File List

- `_bmad-output/implementation-artifacts/m3/1-3-split-hosted-plugin-discovery-into-source-and-approval-layers.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `settings.gradle.kts`
- `kernel/README.md`
- `kernel/README.zh-CN.md`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`
- `kernel/compiler/build.gradle.kts`
- `kernel/runtime/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt` (deleted)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginDiscoveryTest.kt` (deleted)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt` (deleted)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `kernel/plugins/plugin-host/build.gradle.kts`
- `kernel/plugins/plugin-host/README.md`
- `kernel/plugins/plugin-host/README.zh-CN.md`
- `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginSource.kt`
- `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginHostModels.kt`
- `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginCandidateSource.kt`
- `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginApproval.kt`
- `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginDiscovery.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginHostTestFixtures.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginDiscoveryTest.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`

### Change Log

- 2026-07-07: Created Story `1.3` artifact and anchored it to the M3 host-architecture split.
- 2026-07-07: Added `:kernel:plugins:plugin-host`, split hosted source enumeration from approval, rewired compiler/runtime ownership, and verified the Java `25` build.


