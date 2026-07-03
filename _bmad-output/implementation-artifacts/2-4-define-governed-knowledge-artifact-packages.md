---
baseline_commit: 4f7746983d0e3c0f8f1157ec1052b82850f94f70
---

# Story 2.4: Define Governed Knowledge Artifact Packages

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to define versioned package and manifest formats for governed ontology, mapping, and rule artifacts,
so that reviewed knowledge can be published as reusable compiler inputs with explicit provenance and compatibility.

## Acceptance Criteria

1. Given reviewed ontology, mapping, or rule content approved for operational use, when that content is packaged for Athena, then the result includes a typed artifact package and manifest declaring artifact kind, version, provenance, and compatible core or contract range, and the package format remains distinct from project-authored engineering input.
2. Given a malformed or incomplete governed knowledge package, when Athena validates the package, then it rejects the package before operational use, and it emits diagnostics describing the packaging or manifest defect.

## Tasks / Subtasks

- [x] Define a core-owned governed knowledge package vocabulary and manifest model. (AC: 1, 2)
  - [x] Add explicit artifact-kind vocabulary for ontology additions, standards mappings, and rule artifacts.
  - [x] Define the minimum manifest surface for package identity, package format version, artifact version, provenance, and compatible core or knowledge-contract range.
  - [x] Keep this model distinct from the classpath plugin manifest and distinct from authored DSL input.
- [x] Add a minimal local M0 package shape for governed knowledge artifacts. (AC: 1, 2)
  - [x] Prefer a deterministic directory-backed package proof shape with a core-owned manifest file and typed payload entries.
  - [x] Keep the package format JVM-first and zero-extra-dependency; do not introduce archive tooling, remote registries, or third-party serialization frameworks just to prove the boundary.
- [x] Implement compiler-owned package loading and validation for governed knowledge artifacts. (AC: 1, 2)
  - [x] Load package metadata and required payload references from the local filesystem without attaching the package to project compilation yet.
  - [x] Reject malformed manifests, unsupported artifact kinds, invalid compatibility ranges, and missing declared payload files with stable inspectable diagnostics.
- [x] Add sample governed knowledge package fixtures for the first M0 proof. (AC: 1, 2)
  - [x] Provide at least one valid fixture for each artifact kind: ontology, mapping, and rule.
  - [x] Provide malformed or incomplete fixtures that prove rejection behavior deterministically.
- [x] Document the governed knowledge package boundary and Story `2.4` non-goals. (AC: 1, 2)
  - [x] Explain how the package path differs from plugin discovery and from project-authored DSL compilation.
  - [x] State clearly that operational package resolution into compilation context belongs to Story `2.5`.

### Review Findings

- [x] [Review][Patch] Prevent governed knowledge payload paths from escaping the package root [compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageLoader.kt:270]
- [x] [Review][Patch] Keep governed knowledge compatibility models independent from plugin contract types [compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt:3]

## Dev Notes

### Story Intent

- Story `2.4` defines the first real package contract for governed knowledge, not the full `Knowledge Compiler` and not live knowledge-driven project compilation.
- The proof target is that reviewed knowledge can exist as a distinct reusable package with explicit provenance and compatibility, and that Athena can reject malformed packages before they affect operational behavior.
- Story `2.5` owns package resolution into compilation context. Story `2.4` must stop at package format, package loading, and package validation.

### Architecture Guardrails

- Keep the `Knowledge Compiler` path distinct from the `Engineering Compiler` path. Project-authored DSL and governed knowledge packages are different authorities with different lifecycles.
- `Engineering IR` remains the only canonical semantic authority for project instances in M0. Knowledge packages may influence future compilation, but Story `2.4` must not create a second instance-level semantic model or bypass `Engineering IR`.
- Do not treat governed knowledge packages as classpath plugins. Reuse validation patterns where useful, but keep package identity, manifests, and filesystem loading separate from `ServiceLoader` plugin discovery.
- Keep the whole proof local, JVM-first, deterministic, and single-process per AD-1.
- `AutomationML` remains a standards and ontology boundary reference only. Story `2.4` may reference it as an example standards-mapping target, but it must not become an importer, exporter, runtime connector, or replacement internal model.
- AI review workflow, evidence extraction pipelines, and human approval tooling remain out of scope here. This story only defines the accepted package substrate those workflows would eventually produce.

### Technical Requirements

- Add a compiler-owned knowledge package contract surface under `compiler`, preferably a new package such as `com.engineeringood.athena.compiler.knowledge`, instead of overloading runtime plugin contracts.
- The manifest model for this story should distinguish at minimum:
  - stable artifact identity
  - artifact kind
  - package format version
  - artifact semantic version
  - provenance or evidence references
  - compatibility range against Athena core or a core-owned knowledge contract version
  - declared payload entries needed for package validity
- Preserve the proven validator style from Stories `2.1` and `2.2`:
  - stable diagnostic rule ids
  - inspectable subjects
  - deterministic ordering
  - compiler-owned validation logic
- Preferred M0 package proof shape:
  - a local directory package
  - one core-owned manifest file
  - one or more payload files referenced by the manifest
  - no zip/archive distribution yet
  - no repository or marketplace mechanics
- Prefer a zero-extra-dependency manifest format. A simple text format such as `.properties` is acceptable for M0; do not add JSON/YAML parsing frameworks unless the need is unusually strong and well justified.
- Package validation must prove more than field presence. It should also verify:
  - artifact kind is supported
  - compatibility range is parseable
  - required provenance fields are present
  - declared payload files exist
  - package structure is distinct from project DSL inputs
- Keep payload semantics intentionally shallow in Story `2.4`.
  - Ontology packages may use minimal placeholder payload content.
  - Standards mapping packages may point at reference concepts such as `AutomationML` without implementing standards import/export.
  - Rule packages may define only the accepted artifact envelope, not full new rule execution wiring.

### Architecture Compliance

- Align to AD-1 by keeping package loading local, deterministic, JVM-first, and single-process.
- Align to AD-2 by treating ontology additions and standards mappings as extension artifacts rather than permanent core rewrites.
- Align to AD-3 by ensuring knowledge packages influence project compilation only through explicit later contracts, never by replacing canonical `Engineering IR`.
- Align to AD-5 by keeping governed knowledge extensible but non-sovereign through core-owned contracts and validation.
- Align to AD-6 by not confusing filesystem-governed knowledge packages with local classpath plugin discovery.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Keep KDoc on all new core Kotlin classes and interfaces for the governed knowledge package surface.
- Reuse the current Kotlin/JUnit stack and the existing validator/discovery test style where it fits.
- Do not add database layers, DI containers, plugin frameworks, archive libraries, or general-purpose serialization stacks for this story unless absolutely necessary.

### File Structure Requirements

- Expected primary touch points:
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/resources/**`
  - `docs/compiler/**`
- Likely new files or packages:
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/**`
  - `compiler/src/test/resources/knowledge-packages/**`
  - `docs/compiler/m0-knowledge-package-boundary.md`
- Current file state to preserve:
  - [AthenaPluginManifestModel.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt:1) already owns the classpath plugin manifest vocabulary. Story `2.4` may borrow compatibility and manifest-shape ideas from it, but governed knowledge packages must remain a distinct contract surface.
  - [AthenaPluginValidator.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt:1) already demonstrates stable validator diagnostics. Reuse that rigor for knowledge package validation instead of inventing ad hoc error handling.
  - [AthenaPluginDiscovery.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt:1) already owns classpath plugin discovery. Do not repurpose it for governed knowledge package scanning.
  - [AthenaCompiler.kt](D:/Aaron/workspace/projects/2026/eos/Athena/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:1) already owns the project compilation pass pipeline. Story `2.4` must not yet thread active governed knowledge into `compile`; that belongs to Story `2.5`.
  - [docs/compiler/m0-plugin-contract-boundary.md](D:/Aaron/workspace/projects/2026/eos/Athena/docs/compiler/m0-plugin-contract-boundary.md:1) and [docs/compiler/m0-plugin-discovery-boundary.md](D:/Aaron/workspace/projects/2026/eos/Athena/docs/compiler/m0-plugin-discovery-boundary.md:1) already define the plugin path. Story `2.4` should add a sibling knowledge-package boundary document, not blur those existing boundaries.
- Files that should stay semantically unchanged in Story `2.4` unless a direct need is proven:
  - parser and AST code under `language`
  - canonical IR model under `ir`
  - current domain plugin semantics in `domain-electrical-runtime`
  - current render derivation and `SVG` emission
  - current compiler pass descriptors and order
  - `examples/` DSL conformance inputs
- Keep governed knowledge fixtures out of `examples/`.
  - `examples/` remains the authored-project conformance suite.
  - Package fixtures belong in test resources or another clearly separate governed-knowledge location.

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Required tests:
  - valid ontology, mapping, and rule package fixtures load and validate deterministically
  - malformed manifests emit stable diagnostics with inspectable subjects
  - missing declared payload files are rejected deterministically
  - unsupported artifact kinds or invalid compatibility ranges are rejected before operational use
  - governed knowledge package fixtures are not mistaken for project-authored DSL inputs or classpath plugins
- Preserve the repository rule: keep Gradle verification sequential only on Windows. Do not run `build` and `test` in parallel in this repo.

### Previous Story Intelligence

- Story `2.1` established the core-owned contract-and-validator pattern for typed extension surfaces.
- Story `2.2` established deterministic discovery and activation inventory for local classpath plugins.
- Story `2.3` proved that real domain semantics can execute through approved plugin contracts while the compiler core remains general.
- Story `2.4` should follow the same architectural discipline:
  - core-owned contracts
  - deterministic validation
  - stable diagnostics
  - explicit separation between extension boundary and semantic authority
- The main new boundary to protect is different from Story `2.3`:
  - governed knowledge is not runtime plugin discovery
  - governed knowledge is not authored DSL input
  - governed knowledge is not yet active compilation context
- The Windows sequential-verification rule remains real. Keep all Gradle verification commands sequential.

### Git Intelligence Summary

- Current recent commits are:
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical implementation guidance should come from the current working tree and completed Epic 2 story artifacts rather than from historical commit patterns.

### Project Structure Notes

- No `project-context.md` file was found in the repository.
- No UX artifact exists for this phase. Keep the story compiler-first, contract-first, and boundary-first.
- Epic 2 sequence matters:
  - Story `2.1` contracts
  - Story `2.2` discovery
  - Story `2.3` real domain plugin
  - Story `2.4` governed knowledge package format
  - Story `2.5` governed knowledge resolution
  - Story `2.6` external boundary descriptors
- This story should define the reusable package substrate that later `Knowledge Compiler` work will consume; it should not jump ahead into runtime knowledge resolution or external-system integration.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 2, Story `2.4` acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - FR `7`, FR `8`, governance, provenance, and reusable artifact expectations.
- `_bmad-output/specs/spec-athena/SPEC.md` - M0 constraints, typed local extension posture, and non-goals.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1 through AD-6 and the JVM-first typed-extension substrate.
- `_bmad-output/planning-artifacts/implementation-readiness-report-2026-07-02.md` - Epic 2 sequencing and FR/NFR traceability for governed knowledge.
- `_bmad-output/planning-artifacts/briefs/brief-Athena-2026-07-02/addendum.md` - separate `Knowledge Compiler` path and plugin-first/governed-center posture.
- `_bmad-output/implementation-artifacts/2-1-define-core-owned-typed-plugin-contracts.md` - prior contract, validator, and diagnostic patterns.
- `_bmad-output/implementation-artifacts/2-2-discover-local-plugins-and-validate-compatibility-before-use.md` - deterministic discovery and approved inventory boundary to avoid reusing incorrectly.
- `_bmad-output/implementation-artifacts/2-3-deliver-electrical-and-runtime-semantics-through-a-real-domain-plugin.md` - latest Epic 2 implementation learnings and boundary discipline.
- `manifesto/docs/architecture/01-compiler.md` - `Engineering Compiler` vs `Knowledge Compiler` separation and rule/provenance expectations.
- `manifesto/docs/architecture/02-ontology.md` - standards mapping, ontology governance, provenance, and review boundary.
- `manifesto/docs/architecture/05-plugin.md` - plugin taxonomy and governance boundary.
- `manifesto/docs/rfc/RFC-0008-knowledge.md` - knowledge path scope and evidence/review open questions.
- `manifesto/docs/references/04-glossary.md` - glossary definitions for `Knowledge Compiler`, `Plugin`, and `Standards Mapping`.
- `manifesto/docs/technologies/09-automationml.md` - `AutomationML` as a standards boundary reference, not the native internal model.

## Dev Agent Record

### Implementation Plan

- Add a compiler-owned `compiler.knowledge` surface with distinct manifest, payload, provenance, and load-result models for governed knowledge artifacts.
- Implement a deterministic local directory loader over `athena-knowledge.properties`, including stable diagnostics for malformed manifests, invalid compatibility ranges, missing payloads, and non-package inputs.
- Prove the boundary with valid ontology, standards-mapping, and rule fixtures plus invalid fixture cases, then document the package boundary without wiring active knowledge into `AthenaCompiler`.

### Debug Log

- Red: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaKnowledgePackageLoaderTest` failed because the `compiler.knowledge` package and loader contract did not exist yet.
- Green: added compiler-owned governed knowledge models, diagnostics, and a deterministic local directory loader based on `athena-knowledge.properties`.
- Green: added test fixtures for valid ontology, standards-mapping, and rule packages plus malformed, missing-payload, invalid-kind, invalid-compatibility, and plugin-like rejection cases.
- Green: added `docs/compiler/m0-knowledge-package-boundary.md` to separate governed knowledge packages from authored DSL input and classpath plugin discovery.
- Debugging: an accidental parallel `build` and `test` run reproduced the known Windows Gradle artifact-contention footgun and corrupted `:cli:test` results.
- Recovery: re-established a clean sequential verification path with `java25; .\\gradlew.bat --no-daemon --console=plain :cli:cleanTest :cli:test`, then reran top-level verification one command at a time.
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaKnowledgePackageLoaderTest`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain :cli:cleanTest :cli:test`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain build`
- Verification: `java25; .\\gradlew.bat --no-daemon --console=plain test`

### Completion Notes

- Added a distinct compiler-owned governed knowledge package surface under `com.engineeringood.athena.compiler.knowledge` rather than reusing classpath plugin contracts.
- Defined artifact kind, provenance, payload entry, manifest, and load-result models for local M0 governed knowledge packages.
- Implemented deterministic `.properties`-based package loading and validation with stable inspectable diagnostics for malformed manifests, invalid compatibility, missing payloads, and non-package inputs.
- Added valid ontology, standards-mapping, and rule fixtures plus malformed and negative fixtures to prove the boundary through focused compiler tests.
- Documented the M0 governed knowledge package boundary and kept active package resolution into `AthenaCompiler` deferred to Story `2.5`.

## File List

- `_bmad-output/implementation-artifacts/2-4-define-governed-knowledge-artifact-packages.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageLoader.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageValidationModel.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaKnowledgePackageLoaderTest.kt`
- `compiler/src/test/resources/knowledge-packages/invalid-compatibility/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/invalid-compatibility/payload/rule.txt`
- `compiler/src/test/resources/knowledge-packages/invalid-kind/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/invalid-kind/payload/mapping.txt`
- `compiler/src/test/resources/knowledge-packages/malformed-manifest/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/missing-payload/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/plugin-like-layout/META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin`
- `compiler/src/test/resources/knowledge-packages/valid-ontology/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/valid-ontology/payload/base-entities.txt`
- `compiler/src/test/resources/knowledge-packages/valid-rule/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/valid-rule/payload/connection-safety-rule.txt`
- `compiler/src/test/resources/knowledge-packages/valid-standards-mapping/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/valid-standards-mapping/payload/automationml-map.txt`
- `docs/compiler/m0-knowledge-package-boundary.md`

## Change Log

- 2026-07-03: Implemented Story `2.4` by adding a compiler-owned governed knowledge package contract surface, deterministic local package loading and validation, representative package fixtures, and boundary documentation for the M0 knowledge path.

### Story Completion Status

- Status: review
- Completion note: Story `2.4` now defines local governed knowledge artifact packages with core-owned manifests, typed payload entries, explicit provenance, compatibility validation, and stable rejection diagnostics while keeping active compilation-context resolution deferred to Story `2.5`.
