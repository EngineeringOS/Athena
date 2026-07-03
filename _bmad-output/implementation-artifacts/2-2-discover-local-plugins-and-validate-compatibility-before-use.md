---
baseline_commit: 4f7746983d0e3c0f8f1157ec1052b82850f94f70
---

# Story 2.2: Discover Local Plugins And Validate Compatibility Before Use

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to discover local plugins from manifests and validate compatibility before activation,
so that the M0 compiler can load real extensions safely and deterministically without hidden runtime coupling.

## Acceptance Criteria

1. Given a local Athena installation with one or more plugin artifacts on the JVM classpath, when the compiler initializes plugin discovery, then it locates plugin manifests through the core-defined discovery mechanism, and it builds a deterministic inventory of candidate plugins before any compiler pass uses them.
2. Given a discovered plugin manifest, when Athena evaluates it for activation, then the core validates plugin identity, declared type, compatible core version range, and required extension points, and incompatible or malformed plugins are rejected with inspectable diagnostics before activation.
3. Given a valid set of compatible plugins, when the compiler starts a compilation run, then only the approved plugins are attached at declared extension points owned by the core, and plugin activation order does not override the compiler's pass ordering or semantic authority.

## Tasks / Subtasks

- [x] Add a core-owned classpath discovery mechanism for local plugins. (AC: 1)
  - [x] Discover plugins from the JVM classpath through one core-defined mechanism rather than ad hoc manual wiring.
  - [x] Build a deterministic candidate inventory independent of raw classpath load order.
- [x] Extend plugin validation from Story `2.1` to support activation decisions. (AC: 2)
  - [x] Validate compatible core version range against a core-owned runtime version surface.
  - [x] Reject malformed, incompatible, or otherwise invalid plugins with inspectable diagnostics before activation.
- [x] Add an approved plugin inventory / attachment model owned by the compiler. (AC: 1, 3)
  - [x] Represent candidate, rejected, and approved plugin states explicitly.
  - [x] Group approved plugins by declared extension points without letting plugin order redefine compiler pass order.
- [x] Make the sample `ElectricalRuntimeDomainPlugin` discoverable on the classpath. (AC: 1, 3)
  - [x] Publish the required JVM service registration or equivalent classpath discovery resource in the plugin module.
  - [x] Keep the plugin's core contract and manifest ownership in the core; do not move manifest authority into plugin-local custom formats.
- [x] Add tests that prove discovery, rejection, and deterministic approval behavior. (AC: 1, 2, 3)
  - [x] Prove the real sample plugin is discovered from the classpath.
  - [x] Prove incompatible or malformed plugins are rejected before activation.
  - [x] Prove approved inventory order is deterministic and does not alter compiler pass ordering.
- [x] Document the M0 plugin discovery boundary and Story `2.2` non-goals. (AC: 1, 2, 3)

### Review Findings

- [x] [Review][Patch] Turn plugin provider load failures into rejected candidates instead of compiler-construction crashes [compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt:89]
- [x] [Review][Patch] Reject duplicate plugin identities before multiple candidates attach to the same extension point [compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt:90]

## Dev Notes

### Story Intent

- Story `2.2` turns the typed plugin contracts from Story `2.1` into a real local discovery and approval path.
- The proof target is not "dynamic plugin behavior" yet. The proof target is deterministic discovery, compatibility validation, and approved attachment inventory.
- Story `2.2` should make the sample plugin discoverable without yet routing semantic contributions through it. Live semantic contributions belong to Story `2.3`.

### Architecture Guardrails

- Plugin discovery remains local and JVM-first per AD-6. Do not introduce remote distribution, marketplace concepts, hot loading, sandboxing, or cloud-oriented plugin mechanics.
- `Engineering IR` remains the only semantic authority in M0. Discovery and approval may not create alternate semantic ownership or plugin-owned pass scheduling.
- The compiler continues to own pass ordering. Approved plugin inventory may be sorted deterministically, but plugin discovery order must never change `PARSE`, `LOWER`, `VALIDATE`, or `DOWNSTREAM_DERIVATION`.
- Keep plugin discovery manifest-driven. The core should discover plugin implementations, then evaluate the core-owned manifest carried by each plugin.
- `AutomationML` remains a standards boundary concept only. Do not add standards import/export activation in this story.

### Technical Requirements

- Extend the plugin area under `compiler`, expected under `com.engineeringood.athena.compiler.plugin`, to cover:
  - classpath discovery
  - candidate inventory
  - activation / approval result models
  - core-version compatibility evaluation
  - deterministic ordering rules
- Preferred discovery shape for this story:
  - use the JVM classpath and a core-defined discovery mechanism compatible with current M0 constraints
  - `ServiceLoader` over the core-owned `AthenaPlugin` base contract is the most natural fit unless a better fully local, zero-extra-dependency mechanism is clearly justified
- Add a core-owned runtime version surface for compatibility evaluation.
  - Story `2.1` only validated manifest shape
  - Story `2.2` must compare declared compatibility against a core-owned current version
- Add explicit models for:
  - discovered plugin candidate
  - rejected plugin candidate with diagnostics
  - approved plugin inventory / activation view
- Deterministic ordering should be explicit and testable.
  - Do not trust raw `ServiceLoader` order
  - Sort candidates / approved plugins using a stable core-owned rule such as manifest identity then version, or a similarly explicit deterministic strategy
- Approved inventory may be grouped by extension point, but no plugin behavior should yet influence the M0 semantic pipeline.

### Architecture Compliance

- Align to AD-5 by keeping plugins non-sovereign and core-governed even after discovery exists.
- Align to AD-6 by proving local manifest-driven classpath discovery with compatibility validation before use.
- Align to AD-3 by ensuring discovered plugins attach around the semantic core instead of replacing it.
- Align to AD-4 by keeping renderer plugin discovery downstream-only; do not let discovery imply renderer authority.
- Align to AD-1 by keeping the whole mechanism local, deterministic, and single-process.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Keep KDoc on all new core Kotlin classes in the discovery and activation surface.
- Do not add third-party plugin frameworks or manifest parsers for Story `2.2`.
- Reuse the existing Kotlin/JUnit test stack and the existing plugin contract types from Story `2.1`.

### File Structure Requirements

- Expected primary touch points:
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/resources/**`
  - `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/**`
  - `domain-electrical-runtime/src/main/resources/**`
  - `docs/compiler/**`
- Current file state to preserve:
  - [AthenaPluginContracts.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt:1) already publishes the shared base plugin contract plus typed `domain`, `rule`, and `renderer` contracts. Extend around this surface; do not replace it.
  - [AthenaPluginManifestModel.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt:1) already defines the core-owned plugin manifest, type, extension-point, and compatibility-range models. Story `2.2` should add compatibility evaluation, not invent new manifest ownership.
  - [AthenaPluginValidationModel.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidationModel.kt:1) and [AthenaPluginValidator.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt:1) already handle manifest-shape and typed-contract validation. Extend them for activation-time compatibility and rejection diagnostics.
  - [ElectricalRuntimeDomainPlugin.kt](D:/Aaron/workspace/projects/2026/eos/Athena/domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt:1) already provides the real sample plugin. Story `2.2` should make it discoverable on the classpath, not redesign its manifest authority.
- Files that should stay semantically unchanged in Story `2.2`:
  - parser and AST files under `language`
  - canonical IR model under `ir`
  - current semantic validation behavior
  - current render derivation and `SVG` emission
  - current pass descriptor ordering in `AthenaCompiler`

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Required tests:
  - real sample plugin is discovered from the classpath by the core-defined mechanism
  - discovery inventory is deterministic across repeated runs
  - malformed or incompatible plugin candidates are rejected with inspectable diagnostics
  - approved plugins are grouped / attached only at declared extension points
  - plugin discovery order does not affect compiler pass ordering or semantic authority
- Use test fixtures or test-only plugins where necessary to prove malformed / incompatible scenarios without corrupting the real sample plugin.
- Preserve the current repository rule: keep Gradle verification sequential only on Windows. Do not run `build` and `test` in parallel in this repo.

### Previous Story Intelligence

- Story `2.1` completed the core contract surface and is currently in `review`.
- Story `2.1` added:
  - the core-owned plugin contract package under `compiler`
  - manifest, compatibility-range, and extension-point models
  - direct plugin validation with stable diagnostic rule IDs
  - a real sample plugin in `domain-electrical-runtime`
  - targeted tests proving direct instantiation without discovery
- Story `2.1` explicitly deferred:
  - classpath discovery
  - manifest resource loading
  - `ServiceLoader`
  - activation inventory building
  - plugin ordering
- Carry those previous non-goals forward as the exact scope line for this story: Story `2.2` adds them, but it still must not activate semantic contributions into the live compiler behavior yet.

### Git Intelligence Summary

- Current recent commits are:
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Current working tree contains uncommitted Story `2.1` implementation. Read that code carefully and work with it; do not overwrite or revert it while implementing Story `2.2`.

### Project Structure Notes

- No `project-context.md` file was found in the repository.
- No UX artifact exists for this phase. Keep the work compiler-first and boundary-first.
- Epic 1 is complete and Story `2.1` established the plugin contract substrate. Story `2.2` should turn that substrate into discoverable runtime inventory without changing the core M0 semantics.
- The sequential Windows Gradle verification rule is real in this repo. Story `2.1` reproduced a parallel artifact-contention failure in `:cli:test`; avoid parallel verification runs entirely.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 2, Story `2.2` acceptance criteria and FR mapping.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-5, constraints, and non-goals for typed local plugin contracts.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1, AD-3, AD-5, AD-6, and typed plugin ports.
- `_bmad-output/implementation-artifacts/2-1-define-core-owned-typed-plugin-contracts.md` - previous story completion notes, verification evidence, and local implementation guardrails.
- `docs/compiler/m0-plugin-contract-boundary.md` - Story `2.1` plugin boundary and deferred discovery items.
- `manifesto/docs/architecture/05-plugin.md` - plugin taxonomy, governance boundary, and plugin-first rationale.
- `manifesto/docs/rfc/RFC-0006-plugin.md` - plugin compatibility and extension-class framing.
- `manifesto/docs/rfc/RFC-0005-compiler.md` - compiler-owned pass pipeline and plugin-contributed ordering constraints.

## Dev Agent Record

### Implementation Plan

- Add a core-owned runtime version surface and extend plugin validation with activation-time compatibility checks.
- Discover plugins through `ServiceLoader`, sort candidates deterministically, and model approved/rejected inventory in the compiler-owned plugin package.
- Attach approved inventory to `AthenaCompiler` without changing the declared M0 pass sequence or semantic/render behavior.
- Prove classpath discovery, deterministic approval, and rejection behavior with targeted compiler tests, then run the required sequential Java `25` verification commands.

### Debug Log

- Red: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaPluginDiscoveryTest` failed because the runtime/discovery API and compiler attachment surface did not exist yet.
- Green: added `AthenaCoreVersion`, `AthenaCoreRuntime`, `AthenaPluginSource`, `ServiceLoaderAthenaPluginSource`, deterministic candidate sorting, approved/rejected inventory models, and activation-time compatibility validation.
- Refactor: attached the approved inventory to `AthenaCompiler` as compiler-owned state while preserving the existing pass descriptors and execution order.
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaPluginDiscoveryTest --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain build`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain test`

### Completion Notes

- Implemented JVM-first plugin discovery through `ServiceLoader` and converted raw providers into deterministic compiler-owned candidate, rejected, and approved inventory models.
- Extended `AthenaPluginValidator` to evaluate activation-time core version compatibility using a core-owned runtime version surface and stable diagnostics.
- Made `ElectricalRuntimeDomainPlugin` discoverable through JVM service registration while preserving manifest authority in the compiler-owned contract.
- Added compiler tests that prove sample plugin discovery, malformed/incompatible rejection, deterministic extension-point attachment, and no change to compiler pass ordering.
- Documented the Story `2.2` discovery boundary and explicit non-goals for Story `2.3`.

## File List

- `_bmad-output/implementation-artifacts/2-2-discover-local-plugins-and-validate-compatibility-before-use.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaCoreRuntime.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginDiscoveryTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `compiler/src/test/resources/META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin`
- `docs/compiler/m0-plugin-discovery-boundary.md`
- `domain-electrical-runtime/src/main/resources/META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin`

## Change Log

- 2026-07-03: Implemented Story `2.2` with local `ServiceLoader` discovery, activation-time compatibility validation, deterministic approved inventory attachment, JVM service registration for the sample plugin, and compiler documentation/tests.

## Story Completion Status

- Status: review
- Completion note: Story `2.2` now discovers local plugins through `ServiceLoader`, rejects incompatible candidates before activation, attaches only approved inventory at core-owned extension points, and preserves the existing M0 compiler pass ordering.
