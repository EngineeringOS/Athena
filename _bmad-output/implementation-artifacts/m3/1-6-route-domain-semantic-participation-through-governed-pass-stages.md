---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 1.6: Route Domain Semantic Participation Through Governed Pass Stages

Status: done

## Story

As an extension author,
I want approved plugins to participate in declared compiler stages,
so that domains can contribute meaning without taking ownership of canonical semantic orchestration.

## Acceptance Criteria

1. Given the named pass pipeline and stable contribution contracts exist, when domain participation is wired into the compiler, then Athena allows approved plugins to contribute domain semantic behavior through declared stage contribution points, and those contributions execute in deterministic approved-plugin order.
2. Given `Engineering IR` remains the canonical semantic authority, when plugin-hosted semantic contributions are executed, then the kernel continues to own canonical semantic contracts, identity, and orchestration, and plugin contributions interpret domain schema and relationships without replacing kernel ownership.
3. Given the authored DSL remains generic in M3, when domain semantic contributions are reviewed in the compiler path, then plugin participation extends interpretation of generic authored forms rather than extending grammar, and removing one domain plugin removes only that plugin's meaning without destabilizing generic compilation.
4. Given governed domain semantic participation is implemented, when the standard Java `25` build and compiler/plugin tests are executed, then the workspace builds successfully and approved plugins can participate through declared pass stages, and Epic 1 closes with a usable hosted extensibility platform rather than a partial contract-only refactor.

## Tasks / Subtasks

- [x] Govern domain-stage participation through declared compiler pass metadata. (AC: 1, 2, 3, 4)
  - [x] Restrict domain lowering callbacks to plugins that declared the `LOWER` stage.
  - [x] Restrict domain validation callbacks to plugins that declared the `VALIDATE` stage.
  - [x] Surface declared semantic-enrichment contribution ids through the compiler-owned semantic-enrichment pass record.
- [x] Rewire proof plugins and fixtures onto the governed stage model. (AC: 1, 2, 3, 4)
  - [x] Update `ElectricalRuntimeDomainPlugin` to declare both its lowering and validation participation through stable stage descriptors.
  - [x] Update compiler test fixtures so lowering and validation participation is declared explicitly rather than inferred from arbitrary callbacks.
  - [x] Remove the unnecessary `:kernel:compiler` dependency from `:extensions:domain-electrical` while touching the proof plugin shape.
- [x] Prove governed participation and deterministic ordering with focused tests. (AC: 1, 2, 3, 4)
  - [x] Extend compiler coordinator tests to prove callbacks run only when the matching stage was declared.
  - [x] Update hosted plugin contract tests to reflect the governed electrical proof shape.
  - [x] Verify compiler, plugin-host, runtime, and electrical-extension tests sequentially on Java `25`.

## Dev Notes

### Story Intent

- Story `1.6` is the execution half of Stories `1.2` and `1.5`: typed stage declarations now affect real compiler behavior instead of remaining metadata-only.
- The kernel still owns orchestration. Plugins declare allowed participation and contribute domain behavior inside the core-owned pass stages, but they do not own pass ordering or canonical identity.
- This story stays on the semantic side of the boundary. Renderer extensibility and stronger domain-validation attribution remain in Epic 2.

### Implementation Direction

- `AthenaDomainSemanticsCoordinator` now filters active domain plugins by declared compiler stage before calling lowering or validation callbacks.
- `AthenaCompiler` reads semantic-enrichment declarations from the coordinator so runtime-hosted domain plugin sets and discovered plugin inventories use one governed view of stage participation.
- The electrical proof plugin now declares both lowering and validation stage participation through the stable SPI, and no longer needs a direct module dependency on `:kernel:compiler`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-16-route-domain-semantic-participation-through-governed-pass-stages]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-5---the-compiler-is-governed-as-a-named-pass-pipeline]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaContributionModel.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `codegraph node kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `codegraph node extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `Get-Content kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`

### Completion Notes List

- Governed domain-stage participation now runs through declared compiler stage metadata instead of ad hoc callback availability.
- `AthenaDomainSemanticsCoordinator` now gates lowering and validation callbacks by declared `LOWER` and `VALIDATE` stage participation while preserving deterministic approved-plugin order.
- `AthenaCompiler` now reports semantic-enrichment declarations through the same coordinator used for stage execution.
- Updated the electrical proof plugin and compiler test fixtures to declare their stage participation explicitly.
- Removed the unused `:kernel:compiler` dependency from `:extensions:domain-electrical`, tightening the extension boundary while preserving the proof plugin behavior.

### File List

- `_bmad-output/implementation-artifacts/m3/1-6-route-domain-semantic-participation-through-governed-pass-stages.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `extensions/domain-electrical/README.md`
- `extensions/domain-electrical/README.zh-CN.md`
- `extensions/domain-electrical/build.gradle.kts`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/plugins/plugin-api/README.md`
- `kernel/plugins/plugin-api/README.zh-CN.md`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`

### Change Log

- 2026-07-07: Routed domain lowering and validation callbacks through declared compiler stages, rewired proof plugins, and tightened the electrical extension boundary.


