---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 2.2: Add Plugin-Owned Domain Validation Contributions

Status: done

## Story

As an extension author,
I want approved plugins to contribute domain-specific validation through declared contracts,
so that engineering rules can live in plugins instead of flowing back into kernel validation code.

## Acceptance Criteria

1. Given stable contribution contracts and a named pass pipeline exist, when domain validation support is added, then Athena allows approved plugins to contribute domain diagnostics through typed validation contribution contracts, and those diagnostics execute through declared validation stages rather than through ad hoc plugin callbacks.
2. Given the kernel must remain domain-agnostic, when plugin-owned domain validation is reviewed, then domain rules such as electrical compatibility checks or dummy proof rules remain outside kernel validation modules, and kernel validation code does not gain ownership of proof-domain concepts like `Motor`, `Lamp`, `Switch`, `Wire`, or dummy equivalents.
3. Given inspectability is a non-functional requirement, when plugin-owned validation contributions are surfaced, then the system can attribute diagnostics to the contributing plugin and rule identity, and the contribution path remains visible enough for debugging and verification.
4. Given plugin-owned validation contributions are implemented, when the standard Java `25` build and plugin-validation tests are executed, then the workspace builds successfully and approved plugins can emit domain diagnostics without weakening generic validation, and removing a plugin removes only its domain validation behavior.

## Tasks / Subtasks

- [x] Replace raw plugin validation callbacks with a typed validation-emission contract. (AC: 1, 2, 3, 4)
  - [x] Extend the stable plugin API with typed validation-emission models that preserve declared contribution identity.
  - [x] Change `AthenaDomainPlugin.validate(...)` to return the typed validation-emission result instead of a raw diagnostic list.
  - [x] Keep domain rule construction in extensions and test fixtures rather than moving proof-domain rules into kernel validation modules.
- [x] Attribute plugin-owned validation inside the compiler-owned validate stage. (AC: 1, 3, 4)
  - [x] Make `AthenaDomainSemanticsCoordinator` aggregate plugin validation emissions in deterministic approved-plugin order.
  - [x] Preserve flattened `domainDiagnostics` for downstream compatibility while exposing attributed plugin-owned validation metadata for inspection.
  - [x] Ensure unattributed kernel-owned diagnostics such as `domain.semantics.unavailable` remain separate from plugin-owned validation attributions.
- [x] Refactor the electrical proof plugin and test fixtures onto the typed contract. (AC: 1, 2, 3, 4)
  - [x] Update `:extensions:domain-electrical` to emit typed validation contributions through its declared validation contribution id.
  - [x] Update compiler test plugins to declare validation contribution ids where they participate in the validate stage.
  - [x] Keep the proof-domain rule vocabulary outside `:kernel:validation`.
- [x] Prove the attribution path with regression coverage and sequential Java `25` verification. (AC: 1, 3, 4)
  - [x] Add coordinator and compiler tests that assert plugin id, contribution id, and rule-id attribution for plugin-owned diagnostics.
  - [x] Add regression coverage proving removing a plugin removes only that plugin-owned domain validation attribution.
  - [x] Verify compiler, plugin-host, runtime, electrical-extension, and full-build regressions sequentially on Java `25`.

## Dev Notes

### Story Intent

- Story `2.2` strengthens the `VALIDATE` stage from a flat plugin callback into an inspectable typed contribution path.
- Story `2.1` already separated kernel validation from domain validation. Story `2.2` now makes the domain side attributable by plugin and declared validation contribution.
- Kernel-owned generic validation remains in `:kernel:validation`; proof-domain rules stay in extensions or test fixtures.

### Implementation Direction

- Prefer a small SPI addition:
  - plugins emit typed validation contributions keyed by declared `AthenaValidationContribution.contributionId`
  - the compiler keeps a flattened domain diagnostic list for compatibility
  - compiler-facing validation breakdown also exposes attributed plugin-owned validation metadata
- `domain.semantics.unavailable` remains compiler-owned and unattributed because it is a kernel-hosting failure, not a plugin-emitted validation rule.
- Deterministic ordering must remain:
  - approved plugin order first
  - plugin-declared validation contribution order inside each plugin
  - plugin-emitted diagnostic order inside each contribution

### Previous Story Intelligence

- Story `2.1` introduced `CompilerValidationBreakdown` and kept `EngineeringIrValidator` as the sole kernel-owned generic validator.
- Story `2.1` also proved that kernel validation still runs when generic lowering exists without any validate-stage plugin contribution.
- Do not collapse the new attribution data back into one opaque diagnostics list; inspectability is the main architectural gain of this story.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-22-add-plugin-owned-domain-validation-contributions]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-4---kernel-validation-stays-generic-domain-validation-stays-external]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `codegraph explore "Story 2.2 plugin owned domain validation contributions AthenaPluginValidationContext AthenaDomainValidationContribution AthenaValidationContribution AthenaDomainSemanticsCoordinator ElectricalRuntimeDomainPlugin"`
- `codegraph explore "AthenaDomainValidationContribution AthenaPluginValidationContext AthenaPluginContracts AthenaDomainSemanticsModel AthenaCompiler CompilerValidationBreakdown AthenaCompilerTestFixtures ElectricalRuntimeDomainPlugin validate contribution identity"`
- `Get-Content kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
- `Get-Content kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added typed plugin-validation SPI models so plugins now emit validation contributions by declared contribution id instead of returning raw flattened diagnostics.
- Updated `AthenaDomainPlugin.validate(...)`, the validation context, and the compiler-domain coordinator so plugin-owned domain validation is attributed by plugin id, contribution id, and emitted rule ids.
- Preserved flattened `domainDiagnostics` for downstream compatibility while extending `CompilerValidationBreakdown` with inspectable `domainValidationAttributions`.
- Refit `:extensions:domain-electrical` and compiler test plugins to the typed validation-emission contract without moving proof-domain rules into kernel validation modules.
- Added attribution-focused coordinator and compiler regression coverage, including proof that removing the electrical plugin removes only plugin-owned domain validation while kernel validation remains intact.
- Verified the story with sequential Java `25` runs for compiler, plugin-api, plugin-host, runtime, electrical extension, and a full workspace build.

### File List

- `_bmad-output/implementation-artifacts/m3/2-2-add-plugin-owned-domain-validation-contributions.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`

### Change Log

- 2026-07-07: Created Story 2.2 and implemented typed plugin-owned domain validation attribution across the SPI, compiler, electrical proof plugin, and regression suite.
