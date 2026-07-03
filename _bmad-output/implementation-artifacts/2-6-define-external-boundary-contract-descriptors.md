# Story 2.6: Define External Boundary Contract Descriptors

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to define machine-readable boundary contract descriptors for external tools, standards, and runtime or enterprise contexts,
so that future integrations can connect to the semantic core without becoming alternate semantic authorities.

## Acceptance Criteria

1. Given a candidate external boundary such as a standards interchange, runtime connector, or enterprise bridge, when Athena defines the boundary descriptor, then the descriptor declares the boundary direction, owned semantic authority, expected exchanged forms, and compatibility assumptions, and those contracts keep `Engineering IR` as the upstream semantic authority.
2. Given a boundary descriptor for a standards concept such as `AutomationML`, when it is validated in M0, then Athena can represent it as a reference or compatibility boundary without requiring a production importer, exporter, or live connector, and validation fixtures prove that the descriptor does not relocate authority out of the semantic core.

## Tasks / Subtasks

- [x] Add a compiler-owned external boundary descriptor model under a distinct boundary package. (AC: 1, 2)
  - [x] Define explicit descriptor vocabulary for boundary category, direction, owned semantic authority, exchanged form kinds, and compatibility assumptions.
  - [x] Keep descriptor contracts distinct from plugin manifests, governed knowledge packages, and authored DSL parsing.
- [x] Validate M0 external boundary descriptors deterministically before operational use. (AC: 1, 2)
  - [x] Add stable validation diagnostics for unsupported authority claims, incomplete direction or exchanged-form declarations, and invalid compatibility assumptions.
  - [x] Produce deterministic descriptor ordering and deterministic diagnostic ordering for identical local descriptor inputs.
- [x] Prove standards and runtime or enterprise boundary descriptors remain non-sovereign around `Engineering IR`. (AC: 1, 2)
  - [x] Add at least one `AutomationML` descriptor fixture as a standards reference or compatibility boundary only.
  - [x] Add at least one non-standards descriptor fixture representing a runtime or enterprise boundary without implementing a live connector.
  - [x] Reject any descriptor that attempts to make an external boundary the canonical semantic authority.
- [x] Expose descriptor validation results without changing compiler pass ownership. (AC: 1)
  - [x] Keep `Engineering IR` as upstream semantic authority and avoid adding importer, exporter, or live connector execution to the compile path.
  - [x] Preserve the existing `PARSE`, `LOWER`, `VALIDATE`, and `DOWNSTREAM_DERIVATION` schedule; do not let boundary descriptors behave like plugins or governed knowledge activation.
- [x] Add proof tests for descriptor validity and authority preservation. (AC: 1, 2)
  - [x] Prove valid standards and runtime or enterprise descriptors declare direction, authority, exchanged forms, and compatibility assumptions.
  - [x] Prove invalid descriptors are rejected when they relocate authority or imply production connector behavior in M0.
  - [x] Prove descriptors remain boundary metadata and do not redefine compiler or `Engineering IR` semantics.
- [x] Document the M0 external boundary descriptor boundary and Story `2.6` non-goals. (AC: 1, 2)
  - [x] Clarify how descriptor contracts differ from plugin discovery and governed knowledge packages.
  - [x] Clarify that `AutomationML` remains a standards boundary reference and that live connectors, importers, and exporters remain out of scope for M0.

## Dev Notes

### Story Intent

- Story `2.6` is the final Epic 2 proof slice for external-system boundaries.
- The proof target is not a working integration. The proof target is that Athena can express external boundaries as machine-readable contracts while keeping semantic authority inside `Engineering IR`.
- This story should produce descriptor-plus-fixture proof, not a standards engine, runtime bridge implementation, or enterprise adapter.
- Story `2.5` explicitly deferred external boundary descriptors. Story `2.6` should add those contracts without collapsing them into governed knowledge activation or plugin discovery.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority for project instances in M0.
  - External boundary descriptors may describe sources, targets, or compatibility boundaries.
  - They must not make `AutomationML`, runtime data, enterprise systems, or any external schema the canonical internal model.
- Preserve the declared compiler schedule from Story `1.5`.
  - Do not add, remove, or reorder `PARSE`, `LOWER`, `VALIDATE`, or `DOWNSTREAM_DERIVATION`.
  - Descriptor validation should remain compiler-owned metadata or fixture validation, not a fifth public pass.
- Keep external boundary descriptors separate from plugin discovery.
  - No `ServiceLoader`
  - No plugin manifest reuse
  - No descriptor-driven classpath activation
- Keep external boundary descriptors separate from governed knowledge packages.
  - Standards mappings under `compiler.knowledge` remain reviewed knowledge artifacts consumed by the compiler.
  - Boundary descriptors describe interaction contracts at the perimeter and must not replace governed knowledge packages.
- `AutomationML` remains a standards boundary reference only in M0.
  - It may appear as a descriptor target or compatibility boundary.
  - Story `2.6` must not implement a production `AutomationML` importer, exporter, runtime connector, or replacement ontology.
- Runtime and enterprise boundaries remain descriptor-only in M0.
  - If a runtime or enterprise example is used, it must remain passive metadata.
  - Do not implement `OPC UA`, ERP, MES, or cloud connector logic here.

### Technical Requirements

- Preferred package for the new contract surface is `com.engineeringood.athena.compiler.boundary`.
- Preferred minimal proof shape for this story:
  - one compiler-owned descriptor model
  - one deterministic validator or loader+validator path
  - one inspectable diagnostic report
  - local machine-readable fixtures proving valid and invalid descriptors
- Preferred machine-readable proof format is a local `.properties` descriptor manifest under test resources to stay JVM-first and zero-extra-dependency, similar in operational simplicity to Story `2.4` while remaining semantically separate from governed knowledge packages.
- The descriptor contract must declare at minimum:
  - boundary identity
  - boundary category
  - boundary direction
  - owned semantic authority
  - expected exchanged form kinds
  - compatibility or assumption fields
- The validator must reject any descriptor that:
  - claims an external boundary is the canonical semantic authority
  - omits boundary direction or exchanged-form declarations
  - smuggles importer, exporter, or live connector execution into M0
  - conflicts with the rule that `Engineering IR` stays upstream
- The simplest acceptable M0 proof for AC 2 is:
  - `AutomationML` can be represented as a standards boundary descriptor
  - a runtime or enterprise boundary can be represented as passive metadata
  - invalid descriptors are rejected with stable diagnostics
  - validation fixtures prove authority does not move outside the semantic core
- Keep the compiler-facing surface minimal.
  - Only expose descriptor validation results if needed for inspection or tests.
  - Do not introduce runtime connector orchestration, transport stacks, or external protocol clients.

### Architecture Compliance

- Align to AD-1 by keeping descriptor definition local, deterministic, JVM-first, and single-process.
- Align to AD-3 by keeping `Engineering IR` as the only canonical semantic authority.
- Align to AD-5 by treating boundary descriptors as core-owned contracts rather than sovereign extension systems.
- Align to AD-6 by preserving the distinction between local manifest-driven plugin discovery and descriptor-only external boundary metadata.
- Align to FR10 and FR11 by treating external tools, standards, runtime, and enterprise systems as sources, targets, or compatibility boundaries around the core rather than substitutes for it.
- Align to NFR6 and NFR7 by keeping standards and external integrations reviewed, explicit, and non-sovereign.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Keep KDoc on all new core Kotlin classes and interfaces added for external boundary descriptors.
- Reuse the current Kotlin/JUnit stack and the deterministic validation style established in Stories `2.2` through `2.5`.
- Do not add protocol stacks, XML toolchains, archive libraries, DI containers, or third-party serialization frameworks for this story.

### File Structure Requirements

- Expected primary touch points:
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/**`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/resources/boundary-descriptors/**`
  - `docs/compiler/**`
- Current file state to preserve:
  - [AthenaCompiler.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:1) already owns compiler-facing result and pass reporting surfaces. Story `2.6` must not let boundary descriptors redefine pass ownership.
  - [CompilerModels.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt:1) already exposes public compiler result models and governed knowledge attribution metadata. Extend carefully only if descriptor validation must become inspectable externally.
  - [AthenaKnowledgeResolutionModel.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolutionModel.kt:1) and [AthenaKnowledgeResolver.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolver.kt:1) already own governed knowledge activation. Story `2.6` must not overload these types with boundary-descriptor responsibilities.
  - [AthenaPluginContracts.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt:1) and [AthenaPluginDiscovery.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt:1) already define plugin contracts and discovery. Story `2.6` should complement them, not reuse them as descriptor identity or activation surfaces.
  - [docs/compiler/m0-plugin-discovery-boundary.md](D:/Aaron/workspace/projects/2026/eos/Athena/docs/compiler/m0-plugin-discovery-boundary.md:1) and [docs/compiler/m0-knowledge-package-boundary.md](D:/Aaron/workspace/projects/2026/eos/Athena/docs/compiler/m0-knowledge-package-boundary.md:1) already document adjacent boundaries. Story `2.6` should add a parallel boundary doc rather than blur those lines.
- Files that should stay semantically unchanged in Story `2.6` unless a direct need is proven:
  - parser/tokenization behavior under `language`
  - canonical IR data model under `ir`
  - current domain plugin semantics in `domain-electrical-runtime`
  - renderer and `SVG` emission behavior
  - live plugin discovery and governed knowledge activation semantics
- Keep boundary descriptor fixtures separate from `examples/` and separate from `knowledge-packages/`.
  - `examples/` remains authored DSL conformance input
  - `knowledge-packages/` remains governed knowledge fixture input
  - external boundary descriptors should live under their own fixture root

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Required tests:
  - valid standards and runtime or enterprise boundary descriptors load or validate deterministically
  - valid descriptors expose direction, authority, exchanged-form, and compatibility assumptions
  - invalid descriptors are rejected when they relocate semantic authority away from `Engineering IR`
  - `AutomationML` remains representable as a boundary reference without importer/exporter execution
  - boundary descriptors do not redefine compiler pass ordering or plugin activation
- Preserve the repository rule: keep Gradle verification sequential only on Windows. Do not run `build` and `test` in parallel in this repo.

### Previous Story Intelligence

- Story `2.5` completed the governed knowledge resolution boundary and is now `done`.
- Story `2.5` added:
  - compiler-owned governed knowledge resolution models
  - deterministic active and rejected context ordering
  - explicit compiler-facing knowledge attribution metadata
  - restored public pass descriptor semantics after review
- Story `2.5` explicitly deferred:
  - external-system boundary descriptors
  - standards import/export execution
  - runtime or enterprise connector behavior
- Story `2.6` should add exactly those deferred descriptor contracts, but it still must not:
  - implement a production `AutomationML` importer or exporter
  - implement `OPC UA` or enterprise protocol transport
  - move semantic authority into a boundary descriptor
  - collapse descriptor metadata into plugin discovery or governed knowledge activation

### Git Intelligence Summary

- Current recent commits are:
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical implementation guidance should come from the current working tree and the completed Story `2.5` artifact rather than from historical commit patterns.

### Project Structure Notes

- No `project-context.md` file was found in the repository.
- No UX artifact exists for this phase. Keep the story compiler-first, contract-first, and boundary-first.
- Epic 2 sequence remains load-bearing:
  - Story `2.4` package shape and validation
  - Story `2.5` active resolution into compilation context
  - Story `2.6` external boundary contract descriptors
- This story should prove explicit boundary metadata around the semantic core without claiming that external systems already participate operationally in M0.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 2, Story `2.6`, FR10/FR11 mapping, and acceptance criteria.
- `_bmad-output/planning-artifacts/sprint-change-proposal-2026-07-02.md` - rationale that Story `2.6` is a descriptor-plus-fixture slice rather than a broad policy statement.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - FR10, FR11, and boundary-integration posture.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-3, AD-5, AD-6, and `AutomationML` boundary constraints.
- `_bmad-output/planning-artifacts/implementation-readiness-report-2026-07-02.md` - readiness confirmation that Story `2.6` covers FR10 and FR11.
- `_bmad-output/specs/spec-athena/SPEC.md` - M0 constraints and non-goals, including `AutomationML` and `OPC UA`.
- `_bmad-output/implementation-artifacts/2-5-resolve-governed-knowledge-artifacts-into-compilation-context.md` - prior story implementation, deferrals, and adjacency rules.
- `docs/compiler/m0-plugin-discovery-boundary.md` - plugin boundary separation that Story `2.6` must preserve.
- `docs/compiler/m0-knowledge-package-boundary.md` - governed knowledge boundary separation that Story `2.6` must preserve.
- `manifesto/docs/architecture/01-compiler.md` - compiler input/output model and downstream integration posture.
- `manifesto/docs/architecture/02-ontology.md` - ontology authority and standards mapping posture.
- `manifesto/docs/references/01-standards.md` - standards usage as ontology references, rule sources, and importer/exporter boundaries.
- `manifesto/docs/technologies/09-automationml.md` - `AutomationML` as a standards-facing boundary, not the native internal model.

## Dev Agent Record

### Story Completion Status

- Status: done
- Completion note: Added compiler-owned external boundary descriptor models, deterministic loading and validation, compiler-facing boundary validation metadata, passive `AutomationML` and `OPC UA` fixtures, boundary documentation, and proof tests while preserving the declared four-pass compiler schedule. Post-review hardening added typed compatibility assumptions, standards posture validation, duplicate identity rejection, canonical root deduplication, UTF-8 manifest loading, and regression coverage for malformed manifests.

### Debug Log

- Wrote the red-phase tests for boundary descriptor resolution and compiler metadata exposure before adding production code.
- Added a new `compiler.boundary` package rather than reusing plugin or governed knowledge surfaces.
- Fixed one Kotlin compilation issue by making the generic enum parsing calls explicit in the descriptor loader.
- Verified the final implementation with sequential Java 25 `:compiler:test`, `build`, and `test` runs.

### File List

- `docs/superpowers/specs/2026-07-03-external-boundary-descriptors-design.md`
- `docs/superpowers/plans/2026-07-03-external-boundary-descriptors.md`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorValidationModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorLoader.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorResolver.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaBoundaryDescriptorResolverTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `compiler/src/test/resources/boundary-descriptors/automationml-reference/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/opc-ua-runtime/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/external-authority/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/operational-execution/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/invalid-assumption/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/malformed-exchange-forms/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/duplicate-id-a/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/duplicate-id-b/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/automationml-outbound/athena-boundary.properties`
- `compiler/src/test/resources/boundary-descriptors/utf8-identity/athena-boundary.properties`
- `docs/compiler/m0-external-boundary-descriptors.md`

### Change Log

- Added the external boundary descriptor contract package with explicit M0 vocabulary, diagnostics, loader, and resolver.
- Extended compiler compilation results to expose inspectable boundary validation metadata without changing pass ownership.
- Added deterministic tests and descriptor fixtures proving passive standards and runtime boundaries while rejecting sovereign or operational descriptors.
- Added M0 boundary documentation and short local design and planning artifacts for Story `2.6`.
- Applied code-review hardening for typed compatibility assumptions, malformed CSV diagnostics, standards reference-or-compatibility posture enforcement, duplicate descriptor identity rejection, canonical root deduplication, and UTF-8 manifest loading.

### Review Findings

- [x] [Review][Patch] Reject descriptors that imply operational importer, exporter, connector, or non-reference standards posture even when `m0.mode=PASSIVE_METADATA` [compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorResolver.kt:76]
- [x] [Review][Patch] Replace free-form compatibility assumptions with validated vocabulary and emit invalid-assumption diagnostics [compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorModel.kt:56]
- [x] [Review][Patch] Stop silently discarding blank CSV entries so malformed exchanged-form and assumption lists produce stable diagnostics [compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorLoader.kt:202]
- [x] [Review][Patch] Reject duplicate `descriptor.id` values across different descriptor roots [compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorResolver.kt:15]
- [x] [Review][Patch] Canonicalize descriptor roots before deduplication so filesystem aliases do not load the same boundary twice [compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorResolver.kt:15]
- [x] [Review][Patch] Read boundary manifests with explicit UTF-8 decoding instead of legacy `Properties.load(InputStream)` defaults [compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/AthenaBoundaryDescriptorLoader.kt:151]
