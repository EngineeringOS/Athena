---
baseline_commit: 4f7746983d0e3c0f8f1157ec1052b82850f94f70
---

# Story 2.1: Define Core-Owned Typed Plugin Contracts

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to publish core-owned typed contracts and manifest requirements for plugins,
so that domain, rule, and renderer extensions can attach to the compiler without redefining semantic authority.

## Acceptance Criteria

1. Given the M0 core compiler architecture, when extension points are defined, then Athena exposes typed contracts for at least domain, rule, and renderer plugins, and those contracts define what a plugin may contribute without allowing it to replace `Engineering IR` as the semantic authority.
2. Given a plugin implementation targeting Athena, when it declares itself for local use, then it includes a manifest with plugin identity, version, declared plugin type, compatible core version range, and required extension points, and the manifest format is owned by the core rather than by individual plugins.
3. Given multiple plugin implementations in the system, when their dependencies are evaluated, then each plugin depends only on published core contracts and shared core types, and no plugin requires direct linkage to concrete sibling plugins to function.

## Tasks / Subtasks

- [x] Publish the core-owned plugin contract surface under `compiler` for the minimum M0 plugin classes. (AC: 1, 3)
  - [x] Add a shared base plugin contract plus typed `domain`, `rule`, and `renderer` plugin contracts.
  - [x] Keep the contract surface shape-first and narrow; do not expose hooks that can replace `Engineering IR`, mutate semantic authority, or alter compiler pass ordering.
- [x] Publish the core-owned manifest and compatibility models for plugin declaration. (AC: 2)
  - [x] Add manifest types for plugin identity, version, plugin type, core compatibility range, and required extension points.
  - [x] Add a core-owned extension-point vocabulary that keeps plugin declarations explicit.
- [x] Add inspectable validation for plugin manifests and direct plugin objects. (AC: 1, 2)
  - [x] Return stable diagnostics for malformed manifests or plugin-type versus extension-point mismatches.
  - [x] Keep validation local and direct in this story; do not add classpath discovery, `ServiceLoader`, or activation order logic.
- [x] Implement one minimal sample domain plugin in `domain-electrical-runtime`. (AC: 1, 2, 3)
  - [x] Keep the sample plugin real and typed, but minimal: manifest plus contract implementation only.
  - [x] Do not convert `renderer-svg` or rule execution into real plugins in this story.
- [x] Add tests that prove the published contract is usable without discovery. (AC: 1, 2, 3)
  - [x] Instantiate the sample domain plugin directly in tests.
  - [x] Verify valid manifest acceptance, invalid manifest rejection, and no dependency on concrete sibling plugins.
- [x] Document the M0 plugin contract boundary and Story `2.1` non-goals. (AC: 1, 2, 3)

### Review Findings

- [x] [Review][Patch] Reject plugins that do not implement exactly one typed plugin contract [compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt:140]

## Dev Notes

### Story Intent

- Story `2.1` is the first Epic 2 slice. It does not prove discovery yet; it proves ownership of the plugin boundary.
- The success condition is not merely "some interfaces exist." The success condition is that the core owns the typed plugin contract, manifest declaration shape, compatibility vocabulary, and validation rules.
- The first proof plugin should be a minimal `domain` plugin because Story `2.3` will build on it directly.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority in M0. Plugin contracts may extend behavior around it, but they may not replace it.
- The compiler continues to own pass ordering and phase boundaries. Do not let plugins schedule, reorder, or inject pass order semantics in Story `2.1`.
- Plugins are real, typed, and non-sovereign per AD-5. This story must not create generic hook points that act as escape hatches.
- Plugin discovery is manifest-driven and local per AD-6, but discovery itself belongs to Story `2.2`. Story `2.1` stops at contract publication and direct validation.
- `AutomationML` remains a standards-boundary reference only. Do not implement standards plugins or importer logic here.

### Technical Requirements

- Add a new package under `compiler`, expected at `com.engineeringood.athena.compiler.plugin`, for plugin governance types.
- Publish at minimum:
  - a base plugin contract
  - typed plugin contracts for `domain`, `rule`, and `renderer`
  - a plugin type model
  - a manifest model
  - a core compatibility range model
  - an extension-point model
  - validation result and diagnostic models
  - a validator for manifests and direct plugin objects
- The typed contract surface should be intentionally narrow and shape-first:
  - identity and declared capability metadata are allowed
  - core-owned contribution seams are allowed
  - any method that would replace `EngineeringIrDocument`, semantic diagnostics, or compiler pass ordering is not allowed
- The sample plugin should live in `domain-electrical-runtime` and implement the domain plugin contract with a real manifest.
- Keep the sample plugin directly instantiable from tests; no classpath scanning, manifest resource loading, or activation inventory yet.

### Architecture Compliance

- Align to AD-5 by proving a real typed plugin system without private semantic authority.
- Align to AD-6 by modeling manifest metadata and compatibility in a way Story `2.2` can discover later, while explicitly not implementing discovery in this story.
- Align to AD-2 by treating Electrical/Runtime as the first domain extension rather than permanent core vocabulary.
- Align to AD-3 by ensuring all plugin contracts depend only on core-owned contracts and shared types, never on alternate semantic models.
- Align to AD-1 by keeping this story JVM-first, local, and single-process with no service or remote plugin concerns.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Keep KDoc on all new core Kotlin classes and interfaces under the plugin contract surface.
- Reuse the existing Kotlin test stack. Do not add plugin frameworks, annotation processors, or external manifest libraries for this story.
- Keep the manifest as typed Kotlin data in `2.1`; externalized classpath publication belongs to Story `2.2`.

### File Structure Requirements

- Expected primary touch points:
  - `compiler/build.gradle.kts`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `domain-electrical-runtime/build.gradle.kts`
  - `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/**`
  - `docs/compiler/**`
- Current file state to preserve:
  - [compiler/build.gradle.kts](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/build.gradle.kts:1) currently depends on `language`, `semantics-core`, `ir`, and `renderer-svg`. Add only the minimum needed to publish and test the plugin contracts.
  - [domain-electrical-runtime/build.gradle.kts](D:/Aaron/workspace/projects/2026/eos/Athena/domain-electrical-runtime/build.gradle.kts:1) currently depends on `semantics-core` and `ir`. It will likely need a dependency on `:compiler` for the contract types, but must not gain dependencies on concrete sibling plugins.
  - [ElectricalRuntimeDomainMarker.kt](D:/Aaron/workspace/projects/2026/eos/Athena/domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainMarker.kt:1) is currently only a bootstrap marker. Replace or extend this area carefully so the sample plugin becomes the real proof object for this story.
  - [CompilerModels.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt:1) currently exposes compiler entry-path results. Keep plugin governance types separate under a plugin package rather than mixing them into the compiler result surface.
- Files that should stay semantically unchanged in Story `2.1`:
  - parser and AST files under `language`
  - canonical IR model under `ir`
  - current render derivation and `SVG` emission
  - current semantic validation behavior for the M0 examples

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Required tests:
  - valid manifest validation succeeds for the sample domain plugin
  - invalid manifest fields produce stable validation diagnostics
  - illegal plugin-type versus extension-point combinations are rejected
  - the sample plugin can be instantiated directly and treated as the typed domain plugin contract
  - no contract requires linkage to a concrete sibling plugin
- Preserve the current repository rule: keep Gradle verification sequential only on Windows.

### Git Intelligence Summary

- Current recent commits are:
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical implementation guidance must come from the current working tree and the approved Story `2.1` design spec, not from historical commit patterns.

### Project Structure Notes

- No `project-context.md` file was found in the repository.
- No UX artifact exists for this phase. Keep the story compiler-first and contract-first.
- Epic 1 is complete and already proved:
  - deterministic parse/lower/validate/render pipeline
  - `Engineering IR` as the canonical semantic authority
  - conformance examples as architecture artifacts
- Story `2.1` must build on that proof without destabilizing it. Do not route existing M0 compilation behavior through plugin activation yet.
- The approved design for this story is captured in [2026-07-03-typed-plugin-contracts-design.md](D:/Aaron/workspace/projects/2026/eos/Athena/docs/superpowers/specs/2026-07-03-typed-plugin-contracts-design.md:1). Treat it as the implementation shape for this story.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 2, Story `2.1` acceptance criteria and FR mapping.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-5, constraints, and non-goals for local typed plugin contracts.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-3, AD-5, AD-6, and the typed plugin ports paradigm.
- `docs/superpowers/specs/2026-07-03-typed-plugin-contracts-design.md` - approved Story `2.1` design and implementation recommendation.
- `manifesto/docs/architecture/05-plugin.md` - plugin taxonomy, governance boundary, and plugin-first rationale.
- `manifesto/docs/rfc/RFC-0006-plugin.md` - plugin compatibility and extension-class framing.
- `manifesto/docs/rfc/RFC-0005-compiler.md` - compiler-owned pass pipeline and open question about plugin-contributed ordering.

## Story Completion Status

- Status: review
- Completion note: Core-owned typed plugin contracts, manifest validation, the sample Electrical/Runtime domain plugin, and Story `2.1` verification are complete and ready for review.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story auto-discovery selected `2-1-define-core-owned-typed-plugin-contracts` as the first `ready-for-dev` Epic 2 story.
- Baseline commit preserved from the story frontmatter: `4f7746983d0e3c0f8f1157ec1052b82850f94f70`.
- Red phase verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaPluginContractTest` failed on unresolved core plugin contract types.
  - After adding the contract tests for the sample plugin path, the same command failed on unresolved `ElectricalRuntimeDomainPlugin`.
  - `java25; .\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test --tests com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPluginTest` failed on unresolved `ElectricalRuntimeDomainPlugin`.
- Root-cause investigation during full verification:
  - An attempted parallel `build` and `test` run reproduced the repository's known Windows Gradle artifact contention in `:cli:test` (`EOFException` / missing `in-progress-results-generic.bin`).
  - Sequential reruns required by the repo rule resolved the issue without code changes, confirming the failure was caused by parallel verification against the same workspace rather than Story `2.1` code.
- Final passing verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaPluginContractTest`
  - `java25; .\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test --tests com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPluginTest`
  - `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\gradlew.bat --no-daemon --console=plain test`

### Completion Notes List

- Added the first core-owned M0 plugin contract surface under `compiler`, covering the shared plugin base plus typed `domain`, `rule`, and `renderer` contracts.
- Added the core-owned manifest, plugin type, extension-point, compatibility-range, validation result, and diagnostic models.
- Implemented direct plugin and manifest validation with stable diagnostic rule IDs for malformed declarations, illegal extension-point claims, and typed contract mismatches.
- Added a minimal real sample plugin, `ElectricalRuntimeDomainPlugin`, in `domain-electrical-runtime` with a core-owned manifest and narrow capability metadata.
- Added compiler and domain-module tests that prove the contract is usable directly without classpath discovery or activation mechanics.
- Documented the Story `2.1` plugin boundary and explicit non-goals in `docs/compiler/m0-plugin-contract-boundary.md`.
- Preserved the Story `2.1` non-goal boundary: no classpath discovery, no `ServiceLoader`, no plugin ordering, and no plugin-owned pass scheduling.

### File List

- `_bmad-output/implementation-artifacts/2-1-define-core-owned-typed-plugin-contracts.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `compiler/build.gradle.kts`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidationModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`
- `docs/compiler/m0-plugin-contract-boundary.md`
- `docs/superpowers/plans/2026-07-03-typed-plugin-contracts.md`
- `domain-electrical-runtime/build.gradle.kts`
- `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `domain-electrical-runtime/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`

## Change Log

- 2026-07-03: Added the core-owned typed plugin contract surface, manifest validation, and the first sample Electrical/Runtime domain plugin for Story `2.1`.
- 2026-07-03: Verified Story `2.1` on Java 25 and documented the Windows sequential-verification rule after reproducing and isolating the known parallel Gradle artifact contention issue.
