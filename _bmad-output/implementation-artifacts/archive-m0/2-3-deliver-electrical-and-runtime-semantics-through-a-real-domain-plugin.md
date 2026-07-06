---
baseline_commit: 4f7746983d0e3c0f8f1157ec1052b82850f94f70
---

# Story 2.3: Deliver Electrical And Runtime Semantics Through A Real Domain Plugin

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want the first Electrical/Runtime domain semantics to be delivered through a real Athena domain plugin,
so that M0 proves the semantic core stays general while the first domain extension remains replaceable and governed by core contracts.

## Acceptance Criteria

1. Given the core Athena compiler and plugin contracts, when the M0 Electrical/Runtime domain is implemented, then its domain vocabulary, lowering contributions, and semantic validation contributions are provided through a domain plugin, and the core compiler remains free of hard-coded Electrical/Runtime-specific semantic meaning beyond shared contracts.
2. Given the M0 proof slice for declarations, ports, references, types, and connections, when the Electrical/Runtime domain plugin is active, then the compiler can parse, lower, and validate those domain concepts through the declared extension points, and removing the plugin disables those domain semantics without breaking the core compiler architecture.
3. Given future domain growth beyond the first M0 scope, when new domain concepts are considered, then the architecture permits them to arrive through the same contract-governed extension mechanism, and the Electrical/Runtime plugin does not become the permanent core vocabulary of Athena.

## Tasks / Subtasks

- [x] Extend the core-owned domain plugin contract surface to carry real semantic contributions. (AC: 1, 3)
  - [x] Add narrow typed contribution seams for domain lowering and domain validation under the core-owned plugin package.
  - [x] Keep the contribution shape data-in/data-out and compiler-governed; do not let plugins own pass ordering, renderer state, or alternate semantic models.
- [x] Extract the current Electrical/Runtime lowering behavior out of hard-coded core implementation. (AC: 1, 2)
  - [x] Refactor the current M0 declaration-to-IR behavior so the active domain plugin contributes the Electrical/Runtime-specific lowering decisions.
  - [x] Preserve core-owned identity, provenance, and generic IR assembly helpers where they are truly domain-neutral.
- [x] Extract the current Electrical/Runtime validation behavior out of hard-coded core implementation. (AC: 1, 2)
  - [x] Move device-type, direction, and signal-compatibility semantics into the Electrical/Runtime domain plugin.
  - [x] Keep generic reference, uniqueness, and continuation-policy orchestration in the core unless a rule is truly domain-owned.
- [x] Wire approved domain plugins into the existing compiler passes without changing pass order. (AC: 1, 2, 3)
  - [x] Execute active domain semantics inside the existing `LOWER` and `VALIDATE` pass boundaries.
  - [x] Reuse Story `2.2` approved plugin inventory rather than manual plugin wiring.
  - [x] Ensure absence of the Electrical/Runtime plugin disables those semantics through stable inspectable behavior rather than a crash or silent success.
- [x] Upgrade the sample `ElectricalRuntimeDomainPlugin` from manifest-only proof to real semantic contributor. (AC: 1, 2, 3)
  - [x] Keep the plugin in `domain-electrical-runtime` and keep its manifest/core contract ownership in the compiler-owned plugin package.
  - [x] Do not introduce sibling-plugin coupling, remote plugin behavior, or standards/import-export responsibilities.
- [x] Add proof tests for plugin-owned domain semantics. (AC: 1, 2, 3)
  - [x] Prove the default compiler path still satisfies current M0 conformance expectations when the Electrical/Runtime plugin is active.
  - [x] Prove a compiler instance without the Electrical/Runtime plugin no longer succeeds on Electrical/Runtime semantics while the core architecture and pass pipeline still behave deterministically.
  - [x] Prove core-owned diagnostic IDs and published example outcomes remain stable for the active-plugin path unless an architectural change is explicitly justified.
- [x] Document the M0 real-domain-plugin boundary and Story `2.3` non-goals. (AC: 1, 2, 3)

### Review Findings

- [x] [Review][Patch] Make `lower()` fail inspectably when no domain plugin is active instead of emitting empty IR [compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:55]
- [x] [Review][Patch] Diagnose duplicate or text-valued domain `signal` properties instead of silently skipping compatibility checks [domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt:186]

## Dev Notes

### Story Intent

- Story `2.3` is the first slice where the plugin system stops being metadata and becomes real semantic execution.
- The proof target is not "more plugin features." The proof target is that the current Electrical/Runtime lowering and validation behavior no longer lives as hard-coded M0 domain logic in the core.
- The active default path must still behave like the current M0 proof when the real Electrical/Runtime plugin is attached.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority in M0. The plugin may contribute lowering and validation logic, but it may not replace the IR or create a second durable semantic substrate.
- The compiler still owns pass ordering and phase boundaries. Story `2.3` must not add plugin-defined passes or allow plugins to reorder `PARSE`, `LOWER`, `VALIDATE`, or `DOWNSTREAM_DERIVATION`.
- AST remains syntax-only per AD-3. Do not move semantic validation authority into `language/` or parser code while extracting domain behavior into the plugin.
- Rendering remains downstream per AD-4. Do not move `SVG` or render-model derivation into the domain plugin.
- Plugin discovery, compatibility checks, and approved inventory already exist from Story `2.2`. Story `2.3` must reuse that governed activation path instead of bypassing it with ad hoc construction.
- `AutomationML` remains a boundary-only concept. Do not turn this story into standards import/export work.

### Technical Requirements

- Extend `com.engineeringood.athena.compiler.plugin` with the minimum core-owned execution seam needed for real domain behavior.
- Preferred contribution shape for this story:
  - one narrow domain-lowering contribution surface
  - one narrow domain-validation contribution surface
  - explicit deterministic aggregation by the compiler over approved plugins
- Avoid generic plugin escape hatches:
  - no reflection-driven callback registries
  - no plugin-owned mutable global state
  - no plugin-owned pass descriptors
  - no plugin-specific compiler entrypoints
- Current hard-coded Electrical/Runtime semantics that should move behind the plugin seam include:
  - M0 declaration-to-IR lowering for the current `device`, `port`, and `connect` wedge where that behavior is not truly domain-neutral
  - valid device types `PLC` and `Motor`
  - valid directions `in` and `out`
  - connection direction rule `out -> in`
  - signal compatibility rule requiring equal symbolic `signal`
  - invalid symbolic-property handling currently enforced for `type` and `direction`
- Core behavior that should remain core-owned unless there is a strong reason otherwise:
  - stable identity generation
  - provenance propagation
  - authored-reference preservation
  - duplicate-key detection
  - unresolved and ambiguous reference classification
  - semantic continuation policy
- The compiler-without-plugin path must remain architecturally intact.
  - Parsing must still work.
  - Pass reporting must still work.
  - Domain semantics must no longer succeed silently.
  - Prefer a stable inspectable disable/failure outcome over a crash or hidden permissiveness.
- Preserve the current published conformance behavior for the default active-plugin path.
  - Treat changed diagnostics, changed `Engineering IR` artifacts, or changed `SVG` artifacts as suspect until justified.

### Architecture Compliance

- Align to AD-2 by keeping Electrical/Runtime as the first domain extension rather than the permanent semantic center.
- Align to AD-3 by keeping all semantic truth in canonical `Engineering IR`, even when plugin-contributed logic participates in lowering and validation.
- Align to AD-5 by making the plugin real and executable while keeping it non-sovereign and core-governed.
- Align to AD-6 by executing only through the approved local plugin inventory already established in Story `2.2`.
- Align to AD-1 by keeping the whole proof local, JVM-first, deterministic, and single-process.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Keep KDoc on all new core Kotlin classes and interfaces added to the plugin execution surface.
- Reuse the current Kotlin/JUnit stack and Story `2.2` discovery test patterns.
- Do not add plugin frameworks, DI containers, annotation processors, parser generators, or external rule engines for this story.

### File Structure Requirements

- Expected primary touch points:
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/**`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/**`
  - `semantics-core/src/test/kotlin/com/engineeringood/athena/semantics/core/**`
  - `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/**`
  - `domain-electrical-runtime/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/**`
  - `docs/compiler/**`
- Current file state to preserve:
  - [AthenaPluginContracts.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt:1) currently publishes metadata-oriented typed plugin contracts. Extend around this surface; do not replace the core ownership model.
  - [AthenaPluginDiscovery.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt:1) already discovers, validates, and approves plugins. Reuse its approved inventory instead of creating a second activation path.
  - [AthenaCompiler.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:1) already exposes the pass pipeline and plugin inventory. Story `2.3` should wire domain execution inside existing pass boundaries, not redesign the compiler facade.
  - [EngineeringIrLowerer.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt:1) currently hard-codes M0 declaration lowering. This is one of the primary extraction targets.
  - [EngineeringIrValidator.kt](../../../kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt:1) currently hard-codes M0 Electrical/Runtime rule tables and compatibility checks. This is the other primary extraction target.
  - [SvgRenderModelDeriver.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt:1) remains downstream of semantic truth and should stay renderer-focused.
  - [ElectricalRuntimeDomainPlugin.kt](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt:1) is currently manifest-only. Story `2.3` upgrades it into the first real semantic contributor.
  - [M0ConformanceExamplesTest.kt](../../../kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M0ConformanceExamplesTest.kt:1) locks the end-to-end proof behavior. Preserve its default-path expectations.
- Files that should stay semantically unchanged in Story `2.3` unless a direct story need proves otherwise:
  - parser/tokenization behavior under `language`
  - canonical IR data model under `ir`
  - render-model derivation and `SVG` emission behavior
  - compiler pass descriptor ordering
  - example source files and published expectations for the active-plugin default path

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :semantics-core:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Required tests:
  - active default plugin path preserves current M0 example outcomes and diagnostic rule IDs
  - compiler execution without the Electrical/Runtime plugin disables domain semantics but preserves deterministic pass reporting
  - domain-lowering and domain-validation contribution ordering is deterministic for identical approved plugin inventories
  - no core path requires direct linkage to a concrete sibling plugin outside the published contracts
  - renderer output remains a downstream consequence of semantically valid IR, not plugin-owned rendering logic
- Preserve the repository rule: keep Gradle verification sequential only on Windows. Do not run `build` and `test` in parallel in this repo.

### Previous Story Intelligence

- Story `2.1` established the core-owned plugin contract surface, manifest model, and validation vocabulary.
- Story `2.2` added:
  - JVM `ServiceLoader` discovery
  - activation-time compatibility validation
  - deterministic candidate / rejected / approved plugin inventory
  - compiler attachment of approved plugins without changing pass order
- Story `2.3` must build on those two stories directly.
  - Do not invent a separate plugin registry.
  - Do not manually instantiate the domain plugin in production code paths.
  - Do not push semantic behavior back into the core because the discovery path already exists.
- Story `2.1` and `2.2` both reinforced the same non-goals:
  - no remote plugin mechanics
  - no plugin-owned pass scheduling
  - no standards/import/export activation
  - no sibling-plugin coupling
- The repository already has a real Windows parallel-verification footgun in Gradle artifact output. Keep all verification commands sequential.

### Git Intelligence Summary

- Current recent commits are:
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical implementation guidance must come from the current working tree and the completed Stories `2.1` and `2.2`, not from historical commit patterns.

### Project Structure Notes

- No `project-context.md` file was found in the repository.
- No UX artifact exists for this phase. Keep the story compiler-first and boundary-first.
- Epic 1 already proved the end-to-end parse/lower/validate/render pipeline and the conformance suite. Story `2.3` is a semantic ownership refactor plus plugin execution proof, not a new feature wedge.
- The existing M0 parser and IR are already sufficient for this story. The architectural target is to relocate Electrical/Runtime ownership, not to redesign the syntax or canonical data model.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 2, Story `2.3` acceptance criteria and FR mapping.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-5 success signal and the typed local plugin constraint.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-3, AD-5, AD-6, and the typed plugin ports paradigm.
- `_bmad-output/implementation-artifacts/archive-m0/2-1-define-core-owned-typed-plugin-contracts.md` - prior contract surface, validation vocabulary, and Story `2.1` non-goals.
- `_bmad-output/implementation-artifacts/archive-m0/2-2-discover-local-plugins-and-validate-compatibility-before-use.md` - approved inventory model, discovery path, and Story `2.2` execution notes.
- `docs/compiler/m0-plugin-contract-boundary.md` - Story `2.1` contract boundary.
- `docs/compiler/m0-plugin-discovery-boundary.md` - Story `2.2` discovery and activation boundary.
- `docs/compiler/m0-lowering-boundary.md` - current lowering ownership and invariants.
- `docs/compiler/m0-validation-boundary.md` - current validator-owned rules and diagnostic ordering.
- `docs/compiler/m0-pass-pipeline.md` - declared pass schedule that Story `2.3` must preserve.
- `manifesto/docs/architecture/01-compiler.md` - compiler pass and rule execution responsibilities.
- `manifesto/docs/architecture/05-plugin.md` - plugin-first architecture and governance boundary.
- `manifesto/docs/rfc/RFC-0005-compiler.md` - explicit pass pipeline framing and ordering caution.
- `manifesto/docs/rfc/RFC-0006-plugin.md` - plugin compatibility and extension-class framing.

## Dev Agent Record

### Implementation Plan

- Add a compiler-owned domain semantics seam with narrow lowering and validation contribution models under `compiler.plugin`.
- Refactor the compiler to aggregate approved domain plugins in deterministic order, feed their lowering contributions into IR assembly, and merge their diagnostics into the existing `VALIDATE` pass without changing pass order.
- Move the current Electrical/Runtime property and connection semantics into the real domain plugin while trimming `semantics-core` back to generic duplicate/reference orchestration.
- Prove the active-plugin and no-plugin paths through focused compiler tests, then run the required sequential Java `25` verification set.

### Debug Log

- Red: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaDomainSemanticsCoordinatorTest --tests com.engineeringood.athena.compiler.AthenaCompilerTest` failed on the missing domain semantics coordinator, missing domain contribution models, and the absent `DOMAIN` diagnostic category.
- Green: added compiler-owned domain lowering blueprints, a plugin validation context, and `AthenaDomainSemanticsCoordinator`; extended `AthenaDomainPlugin` with narrow `lower` and `validate` contribution seams.
- Green: refactored `EngineeringIrLowerer` to assemble canonical IR from plugin-owned blueprints while keeping identity generation, provenance propagation, and authored-reference resolution in the core compiler.
- Green: moved Electrical/Runtime property and connection semantics into `ElectricalRuntimeDomainPlugin`; kept generic duplicate/reference checks and continuation policy in `semantics-core`.
- Refactor: updated validator ownership docs and added `m0-domain-plugin-boundary.md`; adapted `semantics-core` tests to the new generic-only boundary.
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaDomainSemanticsCoordinatorTest --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test :semantics-core:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :semantics-core:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :domain-electrical-runtime:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain build`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain test`

### Completion Notes

- Added a real domain execution seam under `compiler.plugin`, including lowering blueprints, validation context, and deterministic compiler-owned aggregation of approved domain plugins.
- Refactored the compiler so approved domain plugins participate inside the existing `LOWER` and `VALIDATE` pass boundaries without changing pass descriptors or order.
- Upgraded `ElectricalRuntimeDomainPlugin` from a manifest-only proof object into the first real semantic contributor for M0 declaration lowering and Electrical/Runtime property/connection validation.
- Removed hard-coded Electrical/Runtime rule tables from `semantics-core/EngineeringIrValidator`, leaving generic duplicate/reference validation and continuation policy in the shared core.
- Added proof tests for deterministic domain contribution ordering and for the plugin-absent path that blocks Electrical/Runtime semantics without breaking the compiler architecture.
- Updated compiler boundary docs so the current ownership model matches the implemented code rather than the pre-plugin M0 state.

## File List

- `_bmad-output/implementation-artifacts/archive-m0/2-3-deliver-electrical-and-runtime-semantics-through-a-real-domain-plugin.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDomainSemanticsCoordinatorTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `docs/compiler/m0-domain-plugin-boundary.md`
- `docs/compiler/m0-lowering-boundary.md`
- `docs/compiler/m0-validation-boundary.md`
- `domain-electrical-runtime/build.gradle.kts`
- `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`
- `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/SemanticValidationModel.kt`
- `semantics-core/src/test/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidatorTest.kt`

## Change Log

- 2026-07-03: Implemented Story `2.3` by moving Electrical/Runtime lowering and validation semantics behind a real domain plugin, wiring approved domain plugins into the existing compiler passes, and documenting the new boundary.

## Story Completion Status

- Status: review
- Completion note: Story `2.3` now executes the first Electrical/Runtime lowering and validation semantics through a real approved domain plugin while preserving the core compiler pass order, canonical `Engineering IR`, and default M0 conformance outcomes.

