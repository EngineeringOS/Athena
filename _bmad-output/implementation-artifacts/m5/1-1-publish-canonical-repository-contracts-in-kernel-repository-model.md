---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.1: Publish Canonical Repository Contracts In `kernel/repository-model`

Status: done

## Story

As a platform engineer,
I want Athena to define typed repository manifest, lock, package identity, and dependency contracts in one kernel boundary,
so that compiler, runtime, and LSP share one governed repository model.

## FR Traceability

- FR-1: stable repository manifest contract
- FR-2: deterministic lock contract
- FR-3: stable package identity and layout rules
- FR-8: governed repository open/create flows need one canonical contract base
- NFR-1: repository/package meaning stays owned by compiler and runtime layers
- NFR-2: contract shapes must support deterministic resolution and lock output
- NFR-3: manifest, lock, graph, and diagnostics remain inspectable

## Acceptance Criteria

1. Given the current M4 kernel structure, when M5 repository contracts are introduced, then Athena adds a dedicated `kernel/repository-model` boundary for repository manifest, lock, package identity, dependency declarations, and resolved package-graph report types, and compiler, runtime, and LSP consume that boundary instead of defining parallel repository models.
2. Given the new repository-model boundary exists, when its responsibility is reviewed, then `athena.yaml` is treated as authored intent and `athena.lock` as derived deterministic resolution state, and package-local manifests and multi-package repository authoring remain out of scope for M5.

## Tasks / Subtasks

- [x] Create the physical `:kernel:repository-model` module and register it consistently. (AC: 1, 2)
  - [x] Add `kernel/repository-model/` with `build.gradle.kts`, `README.md`, and `README.zh-CN.md`.
  - [x] Add `:kernel:repository-model` to [`settings.gradle.kts`](../../../settings.gradle.kts) as a normal sibling kernel module.
  - [x] Add a simple module marker plus focused test, following the existing kernel module convention.
- [x] Publish the canonical repository/package contract surface in the new module. (AC: 1, 2)
  - [x] Define typed Kotlin models for the repository-root manifest contract, lock contract, primary package identity, dependency declarations, and resolved package-graph/report shapes.
  - [x] Keep the package root under `com.engineeringood.athena.repository`, matching the existing `*-model` module pattern instead of introducing `repositorymodel` or compiler-owned packages.
  - [x] Add clean KDoc to all public/core Kotlin classes in this new module.
  - [x] Keep the type surface intentionally narrow to the nouns already required by M5; do not add remote registry, Git transport, publish, or multi-package authoring shapes yet.
- [x] Make compiler, runtime, and LSP consume the boundary without widening scope. (AC: 1)
  - [x] Add project dependencies from `:kernel:compiler`, `:kernel:runtime`, and `:ide:lsp` to `:kernel:repository-model`.
  - [x] Replace or demote any new repository/package contract duplication outside the new module; if a local type must temporarily remain for M4 transport, it must be an adapter-only shape and not the canonical repository/package model.
  - [x] Do not implement repository validation, manifest loading, dependency resolution, lock materialization, create/open-flow behavior changes, or `RepositoryGraphSession` in this story; later M5 stories own that execution logic.
- [x] Update workspace maps and module documentation so the repo tells the same story everywhere. (AC: 1, 2)
  - [x] Update [`kernel/README.md`](../../../kernel/README.md) and [`kernel/README.zh-CN.md`](../../../kernel/README.zh-CN.md) to include `:kernel:repository-model`.
  - [x] Update [`README.md`](../../../README.md) and [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md) so the module graph reflects the new repository contract boundary.
  - [x] Keep documentation explicit that `athena.yaml` is authored intent, `athena.lock` is derived state, and package-local manifests are deferred.
- [x] Verify the boundary with focused and regression-safe tests. (AC: 1, 2)
  - [x] Add focused tests in `kernel/repository-model/src/test/kotlin/...` for module marker and any contract helper logic introduced.
  - [x] Keep `:kernel:compiler:test`, `:kernel:runtime:test`, and `:ide:lsp:test` passing after the new dependency wiring.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- This is the contract-publication story for M5, not the behavior story.
- The point is to freeze the canonical nouns and module ownership before repository validation, resolver execution, lock writing, or IDE package-state exposure begins.
- The safest implementation is a small, typed, JVM-first contract module that other layers depend on immediately.
- This story must stay explicitly VCS-neutral. Athena users and downstream product surfaces should reason about Engineering Repositories, manifests, locks, packages, and resolved graphs, not Git or any other version-control vendor vocabulary.
- Story `1.2` owns repository-root contract validation and primary package layout enforcement.
- Story `1.3` owns governed repository creation.
- Story `1.4` owns governed repository open into contract-aware seed state.
- Story `2.x` owns actual package-resolution and lock materialization behavior.
- Story `3.1` owns the full `RepositoryGraphSession` upgrade.

### Architecture Guardrails

- Align to AD-11 by treating one repository-root `athena.yaml` and one repository-root `athena.lock` as the only M5 repository contract pair.
- Align to AD-12 by modeling one primary package per repository and keeping package-local manifests and multi-package repository authoring out of scope.
- Align to AD-13 by making `:kernel:repository-model` the canonical home for repository manifest, lock, package identity, dependency declarations, resolved graph, and package-report types.
- Align to AD-16 by making `athena.lock` a derived-state concept in both naming and docs; do not let lock state look like authored intent.
- Preserve inherited M4 AD-3 and AD-5: `ide/lsp` remains the IDE semantic entry point, and runtime/compiler remain the semantic owners underneath it.
- Keep M6 SCM preparation indirect: future semantic SCM must consume the repository/package contracts published here, but Story `1.1` must not introduce Git-shaped entities, version-control adapters, or SCM-facing domain nouns into `:kernel:repository-model`.

### Technical Requirements

- `:kernel:repository-model` should be a typed contract module, not a hidden service module.
- Keep the public type names aligned with the M5 architecture consistency conventions:
  - `EngineeringRepository`
  - `RepositoryManifest`
  - `RepositoryLock`
  - `PrimaryPackage`
  - `PackageDependency`
  - `ResolvedPackageGraph`
- It is acceptable to introduce a small number of supporting types such as package identifiers, graph nodes, diagnostics, or reports if they keep the contract coherent and help later stories avoid renaming churn.
- Use Athena-native, vendor-neutral language only. Do not introduce types or fields such as `GitRepository`, `Commit`, `Branch`, `Revision`, `Remote`, or any other source-control-vendor concept in this module.
- If future SCM work needs abstraction, it should depend on this repository/package model from a later dedicated SCM boundary rather than forcing repository-model to carry version-control semantics early.
- Prefer immutable `data class` / `sealed interface` style contract types consistent with the existing kernel model modules.
- The new module should stay dependency-light; avoid adding project-module dependencies unless a tiny shared type is strictly required.
- Do not put YAML parsing, filesystem walking, repository validation, resolver execution, or lockfile IO inside `:kernel:repository-model`. Those behaviors belong to later compiler/runtime stories.
- All public/core Kotlin classes added here need clear KDoc because the user explicitly wants the core Kotlin model surface to stay easy to read.

### Architecture Compliance

- The story is only successful if repository/package meaning becomes easier to point to:
  - one physical module
  - one canonical package namespace
  - one documented ownership story
- Prevent these failure modes:
  - compiler invents repository contract types under `com.engineeringood.athena.compiler.*`
  - runtime invents session-adjacent package contract types under `com.engineeringood.athena.runtime.*`
  - `ide/lsp` keeps a private repository/package model that later becomes the practical source of truth
  - lock state is described as authored configuration instead of derived resolution state
  - repository/package contracts become polluted with Git or other source-control concepts before the SCM milestone exists
  - M5 widens into resolver behavior, frontend state, or multi-package semantics too early

### Library / Framework Requirements

- Use the repo-approved stack already frozen in local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- No additional third-party library should be required just to publish these typed contracts.
- Reuse existing Kotlin/JUnit test conventions from the root [`build.gradle.kts`](../../../build.gradle.kts).

### File Structure Requirements

- Expected new files:
  - `kernel/repository-model/build.gradle.kts`
  - `kernel/repository-model/README.md`
  - `kernel/repository-model/README.zh-CN.md`
  - `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryModelModuleMarker.kt`
  - one or more contract files under `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/`
  - `kernel/repository-model/src/test/kotlin/com/engineeringood/athena/repository/RepositoryModelModuleMarkerTest.kt`
- Expected update files:
  - [`settings.gradle.kts`](../../../settings.gradle.kts)
  - [`kernel/README.md`](../../../kernel/README.md)
  - [`kernel/README.zh-CN.md`](../../../kernel/README.zh-CN.md)
  - [`README.md`](../../../README.md)
  - [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md)
  - [`kernel/compiler/build.gradle.kts`](../../../kernel/compiler/build.gradle.kts)
  - [`kernel/runtime/build.gradle.kts`](../../../kernel/runtime/build.gradle.kts)
  - [`ide/lsp/build.gradle.kts`](../../../ide/lsp/build.gradle.kts)
- Files whose current behavior must be preserved:
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt)
    - This is still the light M4 open-path resolver and should not be turned into the full M5 contract/validation engine in Story `1.1`.
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryDescriptor.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryDescriptor.kt)
    - If kept for the moment, it must be treated as a transport/open-flow adapter, not the canonical repository/package model.
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt)
    - Still owns the active workspace lifecycle; Story `1.1` must not rewrite runtime session behavior.
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt)
    - Still activates one path-backed project; do not replace this with `RepositoryGraphSession` yet.
- Explicit non-goals:
  - no manifest parser
  - no contract validator implementation
  - no resolver pipeline
  - no lockfile writer
  - no repository create/open flow rewrite
  - no SCM model
  - no Git abstraction or version-control vendor abstraction inside repository-model
  - no frontend or Theia package-state surface

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
- Optional wider regression once focused tests are green:
  - `java25; .\gradlew.bat --no-daemon --console=plain build`
- Required proof checks:
  - `:kernel:repository-model` is a real physical module, not only a settings alias
  - `compiler`, `runtime`, and `ide/lsp` build against the new boundary
  - no new parallel repository/package model appears outside `:kernel:repository-model`
  - docs consistently describe `athena.yaml` as authored intent and `athena.lock` as derived state
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`settings.gradle.kts`](../../../settings.gradle.kts) currently does not include `:kernel:repository-model`.
- [`kernel/README.md`](../../../kernel/README.md) currently lists no repository-model boundary.
- [`kernel/compiler/build.gradle.kts`](../../../kernel/compiler/build.gradle.kts) currently depends on plugin, language, validation, engineering, layout, geometry, and renderer modules, but not repository-model.
- [`kernel/runtime/build.gradle.kts`](../../../kernel/runtime/build.gradle.kts) currently depends on compiler, plugin-host, engineering, geometry, layout, and renderer modules, but not repository-model.
- [`ide/lsp/build.gradle.kts`](../../../ide/lsp/build.gradle.kts) currently depends on compiler, engineering-model, language, runtime, validation, the proof extensions, and `lsp4j`, but not repository-model.
- M4 repository opening is still intentionally light:
  - one repository root
  - at most one active authored `.athena` source
  - no final `athena.yaml`
  - no final `athena.lock`
  - no governed package graph yet

### Previous Milestone Intelligence

- M3 locked in the grouped physical workspace rule: new kernel boundaries should exist as real directories with bilingual README coverage, not only as logical aliases in Gradle.
- M4 proved the Theia/LSP shell but deliberately kept repository semantics light; Story `1.1` must preserve that discipline by publishing contracts first instead of collapsing multiple M5 stories together.
- The user has repeatedly enforced three workspace rules that matter directly here:
  - physical module structure must match the intended architecture
  - root package is `com.engineeringood`
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Recent milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - follow the same grouped-module discipline used in M1 and M3
  - do not sneak M5 repository semantics into plugin, compiler, runtime, or IDE packages without first freezing the dedicated model boundary

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by the local M5 architecture and root build configuration:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Story `1.1` is a local contract-publication change, so external version chasing would add noise rather than implementation value.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- The new module should sit alongside the other kernel model boundaries:
  - `kernel/engineering-model`
  - `kernel/layout-model`
  - `kernel/geometry-model`
- The naming should stay easy to understand:
  - module: `repository-model`
  - Gradle path: `:kernel:repository-model`
  - Kotlin package root: `com.engineeringood.athena.repository`
- This story should reduce future renaming churn by freezing the contract nouns now, but it should not over-spec future M6 or M7 concepts.

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-11-publish-canonical-repository-contracts-in-kernelrepository-model]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/implementation-readiness-report-2026-07-08-m5.md]
- [Source: settings.gradle.kts]
- [Source: build.gradle.kts]
- [Source: README.md]
- [Source: kernel/README.md]
- [Source: kernel/compiler/README.md]
- [Source: kernel/runtime/README.md]
- [Source: docs/usages/athena-workspace-summary.md]
- [Source: ide/lsp/build.gradle.kts]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryDescriptor.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt]

## Story Completion Status

- Status: done
- Completion note: Added the real `:kernel:repository-model` module, published VCS-neutral repository/package contracts with KDoc, wired compiler/runtime/LSP to the new boundary, updated docs, passed sequential Java 25 verification including full `build`, and completed local code review with no actionable findings.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 PRD, addendum, architecture spine, epics file, and implementation-readiness report review
- current module topology review through `settings.gradle.kts`, root docs, and kernel docs
- CodeGraph exploration of current `ide/lsp` repository-session and repository-resolver seams
- current `kernel/compiler`, `kernel/runtime`, and `ide/lsp` build dependency review
- recent commit history review
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test"` red-phase failure before contract sources existed
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test"` green-phase pass after adding repository-model contracts
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain build"`

### Completion Notes List

- Added `:kernel:repository-model` as a real physical kernel module with no project-module dependencies.
- Published the first VCS-neutral repository/package contract surface under `com.engineeringood.athena.repository`.
- Kept `RepositoryManifest` explicit as authored intent and `RepositoryLock` explicit as derived state.
- Added focused module tests for marker coverage and contract-role / graph-report behavior.
- Wired `:kernel:compiler`, `:kernel:runtime`, and `:ide:lsp` to depend on the canonical repository-model boundary.
- Demoted the existing M4 LSP repository descriptor/resolver to documented adapter-only status rather than treating them as canonical repository/package models.
- Recorded the future SCM-boundary decision in the M6 draft and roadmap while keeping M5 repository-model vendor-neutral.
- Passed sequential Java 25 verification for `:kernel:repository-model:test`, `:kernel:compiler:test`, `:kernel:runtime:test`, `:ide:lsp:test`, and full `build`.
- Completed local code review for Story `1.1`; no actionable findings remained, although the parallel review-agent layer returned unusable stale outputs in this session.

### File List

- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- _bmad-output/implementation-artifacts/m5/1-1-publish-canonical-repository-contracts-in-kernel-repository-model.md
- docs/roadmap/athena-milestone-roadmap.md
- docs/usages/athena-workspace-summary.md
- draft/m6/001-draft.md
- ide/lsp/build.gradle.kts
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryDescriptor.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt
- kernel/README.md
- kernel/README.zh-CN.md
- kernel/compiler/build.gradle.kts
- kernel/repository-model/README.md
- kernel/repository-model/README.zh-CN.md
- kernel/repository-model/build.gradle.kts
- kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt
- kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryModelModuleMarker.kt
- kernel/repository-model/src/test/kotlin/com/engineeringood/athena/repository/RepositoryContractsTest.kt
- kernel/repository-model/src/test/kotlin/com/engineeringood/athena/repository/RepositoryModelModuleMarkerTest.kt
- kernel/runtime/build.gradle.kts
- README.md
- settings.gradle.kts

## Change Log

- 2026-07-09: Added the canonical `:kernel:repository-model` boundary, documented the VCS-neutral contract rule, wired compiler/runtime/LSP to the module, and passed sequential Java 25 regression verification.
- 2026-07-09: Completed Story `1.1` code review and closed the story with no actionable follow-up patches.
