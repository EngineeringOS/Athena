---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 2.4: Refactor `domain-electrical` Into The Stable M3 Proof Shape

Status: done

## Story

As a platform engineer,
I want the existing electrical extension aligned to the stable M3 SPI,
so that Athena proves a real hosted domain without creating a parallel electrical implementation.

## Acceptance Criteria

1. Given the existing `:extensions:domain-electrical` module already exists from earlier milestones, when it is refactored for M3, then the extension adopts the dedicated plugin API boundary and the stable M3 contribution contracts, and it no longer depends on compiler internals as its primary extensibility surface.
2. Given M3 is an extensibility proof rather than a broad electrical product milestone, when the refactored electrical proof scope is defined, then the supported proof vocabulary remains intentionally narrow to `Motor`, `Lamp`, `Switch`, and `Wire`, and broader industry-grade electrical coverage remains deferred.
3. Given the electrical extension is the first real proof domain, when its hosted contributions are reviewed, then it proves domain schema, domain validation, and domain rendering through the stable M3 contracts, and it does so without reintroducing kernel-owned electrical logic.
4. Given the electrical proof refactor is implemented, when the standard Java `25` build and extension tests are executed, then the workspace builds successfully and `domain-electrical` functions as the first real hosted proof domain through the stable SPI, and Epic 2 demonstrates that a real domain can live outside the kernel.

## Tasks / Subtasks

- [x] Tighten the electrical proof plugin to the intentional M3 vocabulary. (AC: 1, 2, 3)
  - [x] Replace the legacy `PLC`-based proof schema with the narrow `Motor`, `Lamp`, `Switch`, and `Wire` type set inside `ElectricalRuntimeDomainPlugin`.
  - [x] Keep `domain-electrical` on the dedicated `:kernel:plugins:plugin-api` surface and avoid introducing compiler-internal dependencies or kernel-owned electrical rules.
  - [x] Preserve generic port, connection, validation, and render contribution seams while narrowing only the domain-owned proof vocabulary.
- [x] Refactor the electrical proof fixtures and module documentation to match the M3 scope. (AC: 2, 3, 4)
  - [x] Update electrical extension tests and stable contract assertions to reflect the narrowed proof vocabulary and contribution surface.
  - [x] Update the minimum authored examples and proof artifacts that must remain compatible with the hosted electrical plugin.
  - [x] Refresh module README guidance so it describes the M3 proof shape rather than the earlier M0 sample framing.
- [x] Prove the refactor through sequential Java `25` regression coverage. (AC: 1, 2, 3, 4)
  - [x] Run targeted extension, plugin-host, compiler, and runtime tests affected by the proof-vocabulary change.
  - [x] Run the full workspace build sequentially on Java `25`.
  - [x] Record the exact verification commands and changed files in the story record.

## Dev Notes

### Story Intent

- This story is a proof-scope refactor, not a new architecture spike.
- `:extensions:domain-electrical` already participates through the stable hosted SPI; the remaining work is to make its published proof shape match the M3 architecture contract.
- The electrical extension remains the reference real domain, but M3 intentionally proves only a narrow vocabulary and stable hosted contracts.

### Implementation Direction

- Keep the kernel small:
  - no kernel-owned electrical types
  - no compiler-internal extension hooks
  - no plugin-private semantic truth outside canonical engineering, layout, and geometry ownership
- Prefer narrow updates with broad verification:
  - domain schema entities and allowed type values
  - domain-electrical tests
  - proof fixtures that must compile through the hosted path
  - module README text where it still describes the old M0 framing
- If sample instances still use historical names, judge them by whether they violate the proof contract, not by cosmetic churn alone.

### Previous Story Intelligence

- Story `1.2` defined the stable domain schema and contribution contracts.
- Story `1.6` routed domain participation through governed compiler stages.
- Story `2.2` and Story `2.3` proved validation and render contributions can remain inspectable without moving authority out of the kernel.
- Story `2.6` will publish the broader M3 proof corpus, so this story should update only the fixtures necessary to keep the electrical reference domain coherent and passing.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-24-refactor-domain-electrical-into-the-stable-m3-proof-shape]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-8---the-existing-electrical-extension-becomes-the-reference-real-proof-domain]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]
- [Source: extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt]
- [Source: kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `python _bmad/scripts/resolve_customization.py --skill .agents/skills/bmad-dev-story --key workflow`
- `Get-Content -Raw _bmad/bmm/config.yaml`
- `Get-Content -Raw _bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `Get-Content _bmad-output/planning-artifacts/epics-M3-2026-07-07.md`
- `Get-Content _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md`
- `codegraph node com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPlugin`
- `Get-Content -Raw extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `Get-Content -Raw extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `Get-Content -Raw kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`
- `Get-Content -Raw examples/m0/demo-cabinet.athena`
- `rg -n "\bPLC\b|PLC1|type PLC|electrical-runtime" extensions/domain-electrical kernel examples`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`
- `git diff --check -- <story-2.4 file set>`

### Completion Notes List

- Narrowed `ElectricalRuntimeDomainPlugin` to the stable M3 proof vocabulary by replacing the legacy `PLC` schema with component types `Lamp`, `Motor`, and `Switch`, and by renaming the published connection type to `Wire`.
- Kept the electrical extension on the dedicated hosted SPI surface and updated the module READMEs to describe the M3 proof role, dependencies, and vocabulary without reintroducing compiler-internal coupling.
- Updated the minimum electrical proof fixtures and regression sources from `type PLC` to `type Switch`, while intentionally keeping the historical authored instance names to avoid broad semantic-id churn in Story 2.4.
- Extended electrical contract tests to assert the narrowed schema and updated compiler, runtime, CLI, and desktop-viewer regressions that compile electrical proof fixtures through the hosted path.
- Verified the refactor sequentially on Java `25` with targeted module tests plus a full workspace `build`.

### File List

- `_bmad-output/implementation-artifacts/m3/2-4-refactor-domain-electrical-into-the-stable-m3-proof-shape.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/AthenaAiProposalCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/CommandHistoryCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/ConnectCliTest.kt`
- `apps/cli/src/test/kotlin/com/engineeringood/athena/cli/PluginRuntimeCliTest.kt`
- `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `examples/m0/demo-cabinet.athena`
- `examples/m0/demo-cabinet.engineering-ir.txt`
- `examples/m0/dual-drive-cabinet.athena`
- `examples/m0/duplicate-identity-cabinet.athena`
- `examples/m0/quoted-properties-cabinet.athena`
- `examples/m2/demo-cabinet.athena`
- `examples/m2/operator-proof.athena`
- `extensions/domain-electrical/README.md`
- `extensions/domain-electrical/README.zh-CN.md`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphProjectionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspectionTest.kt`

### Change Log

- 2026-07-07: Created Story 2.4, narrowed `domain-electrical` to the stable M3 proof vocabulary, updated the dependent proof fixtures, and verified the workspace on Java `25`.
