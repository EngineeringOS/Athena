---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 2.5: Add The Synthetic `domain-dummy` Proof Plugin

Status: done

## Story

As an extension author,
I want a second hosted proof plugin with synthetic entities and rules,
so that Athena can prove the SPI is generic rather than secretly shaped around the electrical extension.

## Acceptance Criteria

1. Given the stable plugin API boundary and contribution contracts already exist, when the synthetic proof plugin is added, then Athena provides a new `:extensions:domain-dummy` module that consumes the same stable SPI as `domain-electrical`, and the dummy plugin introduces no kernel-owned domain edits for its synthetic vocabulary.
2. Given the dummy plugin exists only to prove generality, when its scope is reviewed, then it contributes synthetic entities, validation behavior, and presentation behavior with no engineering meaning, and it remains intentionally small and architecture-focused.
3. Given the dummy plugin must prove that the SPI is not electrical-specific, when its hosted contributions are compared with the electrical proof domain, then both domains participate through the same approved contracts and governed pass structure, and the dummy plugin does not require special-case handling in kernel code.
4. Given the synthetic proof plugin is implemented, when the standard Java `25` build and plugin tests are executed, then the workspace builds successfully and the dummy domain can be discovered, approved, and exercised through the same hosted path as the electrical proof domain, and Epic 2 closes with two distinct proof domains using one stable SPI.

## Tasks / Subtasks

- [x] Create the `:extensions:domain-dummy` hosted proof module on the existing extension boundary. (AC: 1, 2, 3, 4)
  - [x] Add the physical `extensions/domain-dummy` module with Gradle wiring, ServiceLoader registration, English and Chinese READMEs, and a simple module marker.
  - [x] Publish a synthetic domain manifest, schema, validation contributions, compiler-stage contributions, and renderer-facing contribution metadata through the stable plugin API.
  - [x] Keep the plugin intentionally synthetic and architecture-focused, with no kernel-owned dummy vocabulary or electrical-specific coupling.
- [x] Make the dummy proof domain safe to coexist with the electrical proof domain. (AC: 1, 2, 3, 4)
  - [x] Scope the dummy plugin so it only claims explicitly dummy-authored declarations and does not perturb electrical examples.
  - [x] Refine the electrical proof plugin only as needed so it does not overreach dummy-owned declarations once both plugins are hosted together.
  - [x] Avoid introducing globally active dummy view definitions that would widen default compile outputs before the platform has a view-ownership seam.
- [x] Prove the second hosted domain through tests and inspection surfaces. (AC: 2, 3, 4)
  - [x] Add direct module tests for the dummy plugin's schema and hosted contribution metadata.
  - [x] Update discovery, runtime, compiler, CLI, and other affected regressions so the default hosted inventory becomes deterministic for two proof domains.
  - [x] Exercise the dummy plugin through the hosted path without adding kernel special cases.
- [x] Verify the workspace sequentially on Java `25` and record the result. (AC: 1, 2, 3, 4)
  - [x] Run targeted module and hosted-path tests affected by the second plugin.
  - [x] Run the full workspace build sequentially on Java `25`.
  - [x] Record commands, changed files, and completion notes in this story record.

## Dev Notes

### Story Intent

- The real proof is not just "another module compiles." It is that a second domain can be added without the kernel learning dummy nouns.
- The dummy domain should be synthetic by design and obviously non-product. Its job is to expose whether the current seams are genuinely generic.
- Keep the change narrow. This story is not the place to redesign the entire authored DSL around explicit domain routing unless the existing hosted seams truly force it.

### Implementation Direction

- A second hosted domain introduces two immediate collision risks:
  - lowering and validation over the same generic `device` / `port` / `connect` syntax
  - globally active view definitions, because the compiler currently derives supported layouts for every approved plugin view definition
- The safe M3 move is:
  - scope the dummy plugin with an explicit synthetic authored marker
  - ensure electrical semantics ignore foreign dummy-owned declarations
  - keep dummy presentation proof to runtime-view inspection plus declared render metadata rather than adding default global view definitions in this story
- Prefer deterministic plugin ids, contribution ids, and inventory order so default hosted inspections are stable with two approved domains.

### Previous Story Intelligence

- Story `2.4` tightened `domain-electrical` to the stable M3 proof vocabulary but it still assumes it is the only real default proof domain.
- Story `2.3` established generic render contribution attribution, which this story can reuse for declared dummy render metadata even if the dummy view ids remain non-default.
- Epic `3` will later automate zero/one/multi-plugin matrix verification, so this story should make the default two-plugin hosted state stable enough for that next step.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-25-add-the-synthetic-domain-dummy-proof-plugin]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-9---a-synthetic-dummy-domain-is-required-to-prove-generality]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiContributionContractTest.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `Get-Content -Raw _bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `Get-Content _bmad-output/planning-artifacts/epics-M3-2026-07-07.md`
- `codegraph explore "AthenaDomainPlugin AthenaViewDefinitionContributor AthenaRuntimePluginCommandContributor AthenaRuntimePluginViewContributor AthenaRenderContribution domain-dummy synthetic proof plugin electrical plugin pattern service loader hosted SPI"`
- `Get-Content -Raw settings.gradle.kts`
- `Get-Content -Raw extensions/domain-electrical/build.gradle.kts`
- `Get-Content -Raw extensions/README.md`
- `Get-Content -Raw kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiContributionContractTest.kt`
- `Get-Content -Raw kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginDiscoveryTest.kt`
- `Get-Content -Raw kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistryTest.kt`
- `Get-Content -Raw apps/cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :apps:cli:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-dummy:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added the real `:extensions:domain-dummy` hosted proof module with ServiceLoader registration, bilingual READMEs, a module marker, synthetic schema and validation metadata, and runtime-view plus render-intent contributions published through the stable plugin SPI.
- Wired `domain-dummy` onto the same hosted runtime and test classpaths as `domain-electrical` for CLI, desktop viewer, compiler tests, runtime tests, and plugin-host tests so the default approved inventory now proves two distinct proof domains.
- Scoped coexistence without kernel special cases by keeping dummy ownership explicit, preventing default dummy view definitions, and refining the electrical plugin so it ignores explicitly foreign-domain declarations while still lowering unresolved generic declarations for later kernel validation.
- Updated default hosted regressions to assert the deterministic two-plugin inventory, expanded compiler render-metadata inventory, dummy-only runtime-view activation, and CLI plugin ordering.
- Kept the authored dummy routing marker explicit by using `domain "dummy-runtime"` in hosted-path proof coverage because the current DSL identifier grammar does not allow hyphenated bare identifiers; plugin-side ownership checks now accept the quoted marker without widening kernel grammar.
- Sequential Java `25` verification passed for targeted suites, the desktop bootstrap smoke, and the full workspace `build`.

### File List

- `_bmad-output/implementation-artifacts/m3/2-5-add-the-synthetic-domain-dummy-proof-plugin.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `settings.gradle.kts`
- `extensions/README.md`
- `extensions/README.zh-CN.md`
- `extensions/domain-dummy/build.gradle.kts`
- `extensions/domain-dummy/README.md`
- `extensions/domain-dummy/README.zh-CN.md`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainMarker.kt`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `extensions/domain-dummy/src/main/resources/META-INF/services/com.engineeringood.athena.plugin.AthenaPlugin`
- `extensions/domain-dummy/src/test/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainMarkerTest.kt`
- `extensions/domain-dummy/src/test/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPluginTest.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `apps/cli/build.gradle.kts`
- `apps/cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/PluginRuntimeCliTest.kt`
- `apps/desktop-viewer/build.gradle.kts`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/runtime/build.gradle.kts`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/plugins/plugin-host/build.gradle.kts`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginDiscoveryTest.kt`

### Change Log

- 2026-07-07: Created Story 2.5 and started implementation for the synthetic hosted proof plugin.
- 2026-07-07: Completed Story 2.5 with the synthetic `domain-dummy` proof plugin, deterministic two-plugin hosted inventory, scoped coexistence updates, and sequential Java `25` verification.
