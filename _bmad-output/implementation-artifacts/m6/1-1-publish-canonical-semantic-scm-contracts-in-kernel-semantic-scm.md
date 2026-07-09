---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.1: Publish Canonical Semantic SCM Contracts In `kernel/semantic-scm`

Status: review

## Story

As a platform engineer,
I want Athena to define typed semantic SCM contracts in a dedicated kernel boundary,
so that baseline, diff, consequence, and history semantics have one VCS-neutral source of truth above `repository-model`.

## FR Traceability

- FR-1: stable semantic SCM boundary above repository/package meaning
- FR-2: keep vendor storage mechanics downstream of semantic meaning
- FR-3: repository baseline comparison needs canonical typed baseline nouns
- FR-4: semantic change categories need a stable inspectable contract surface
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-2: user-facing SCM nouns stay VCS-neutral
- NFR-3: diff/review/commit outputs remain deterministic
- NFR-4: outputs remain typed and inspectable

## Acceptance Criteria

1. Given the completed M5 repository and package graph foundation, when M6 semantic SCM begins, then Athena adds a dedicated `kernel/semantic-scm` boundary for semantic baseline, semantic diff, semantic change taxonomy, consequence records, commit intent, review summary, and semantic history contracts, and those contracts remain typed, inspectable, and VCS-neutral rather than using Git or Theia SCM types.
2. Given the semantic SCM boundary is reviewed against existing kernel responsibilities, when ownership is checked, then `kernel/repository-model` stays free of source-control vocabulary, and plugin enrichment plus vendor adapters remain separate downstream seams rather than alternative semantic authorities.

## Tasks / Subtasks

- [x] Create the physical `:kernel:semantic-scm` module and register it consistently. (AC: 1, 2)
  - [x] Add `kernel/semantic-scm/` with `build.gradle.kts`, `README.md`, and `README.zh-CN.md`.
  - [x] Add `:kernel:semantic-scm` to [`settings.gradle.kts`](../../../settings.gradle.kts) as a normal sibling kernel module.
  - [x] Add a simple module marker plus focused test, following the existing kernel module convention used by `:kernel:repository-model` and `:kernel:plugins:plugin-api`.
- [x] Publish the canonical semantic SCM contract surface in the new module. (AC: 1, 2)
  - [x] Define typed Kotlin models for the first M6 nouns:
    - `SemanticBaselineDescriptor` or equivalent baseline identity model
    - `SemanticChangeCategory`
    - `SemanticChangeRecord`
    - `SemanticDerivedConsequence`
    - `SemanticDiff`
    - `SemanticReviewSummary`
    - `SemanticCommitIntent`
    - `SemanticHistorySummary`
    - one VCS-neutral adapter-facing seam such as `SemanticScmAdapter`
  - [x] Keep the package root under `com.engineeringood.athena.scm`, matching the simple kernel noun style instead of `semanticscm`, `git`, or Theia-shaped packages.
  - [x] Add clean KDoc to all public/core Kotlin classes in this new module.
  - [x] Keep the contract surface intentionally narrow to M6 core nouns only; do not add executable Git behavior, Theia provider types, registry transport, or graphical review shapes yet.
- [x] Keep the module dependency-light and architecture-safe. (AC: 1, 2)
  - [x] Prefer only the minimal project dependencies required by the typed contract surface:
    - `:kernel:repository-model`
    - `:kernel:engineering-model`
    - `:kernel:validation`
    - `:kernel:plugins:plugin-api` only if a public contract truly needs stable plugin-facing types
  - [x] Do not make `:kernel:semantic-scm` depend on `:kernel:runtime` or `:kernel:compiler` in Story `1.1`; later stories can adapt runtime/compiler outputs into this boundary without creating cycles.
  - [x] Do not implement baseline loading, semantic comparison logic, vendor adapters, commit execution, LSP methods, Theia bridges, or history UI in this story.
- [x] Update workspace maps and module documentation so the repo tells the same story everywhere. (AC: 1, 2)
  - [x] Update [`kernel/README.md`](../../../kernel/README.md) and [`kernel/README.zh-CN.md`](../../../kernel/README.zh-CN.md) to include `:kernel:semantic-scm`.
  - [x] Update [`README.md`](../../../README.md) and [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md) so the module graph reflects the new semantic SCM boundary.
  - [x] Keep documentation explicit that the module is VCS-neutral, sits above `:kernel:repository-model`, and is not the home of Git or Theia SCM types.
- [x] Verify the boundary with focused and regression-safe tests. (AC: 1, 2)
  - [x] Add focused tests in `kernel/semantic-scm/src/test/kotlin/...` for module marker coverage and any contract helper logic introduced.
  - [x] Verify that the new module builds cleanly under Java 25 and the repo still passes at least focused semantic-kernel verification.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- This is the contract-publication story for M6, not the behavior story.
- The point is to freeze the canonical semantic SCM nouns and ownership before baseline loading, diff execution, review generation, commit preparation, or history surfaces begin.
- The safest implementation is a small, typed, JVM-first kernel module that expresses semantic SCM meaning without pulling in vendor or workbench mechanics.
- Story `1.2` owns baseline resolution through a vendor-neutral adapter boundary.
- Story `1.3` owns deterministic semantic diff categories from baseline comparison.
- Story `1.4` owns validation and contract consequence publication.
- Epic `2.x` owns review and commit semantics.
- Epic `3.x` owns semantic history and release relevance surfaces.

### Architecture Guardrails

- Align to AD-19 by making `:kernel:semantic-scm` the dedicated home for semantic baseline, diff, review, commit-intent, and history contracts above M5 repository meaning.
- Align to AD-20 by modeling baselines as explicit repository-scoped semantic inputs, not runtime command history or frontend state.
- Align to AD-21 by keeping semantic diff modeled around canonical repository/package and engineering meaning rather than raw line diff.
- Align to AD-22 by keeping vendor-specific substrate execution outside this story and outside this module.
- Align to AD-23 by keeping Theia SCM entirely out of this module's public types.
- Align to AD-24 by making authored intent versus derived consequences explicit in the contract vocabulary.
- Align to AD-25 by leaving room for additive domain review enrichment without making plugins the semantic authority.
- Align to AD-26 by keeping publish-oriented history transport-light and package-identity-aware.
- Preserve the inherited M5 rule that `:kernel:repository-model` remains the only canonical home of repository/package contracts.
- Preserve the inherited M4/M5 rule that `RepositoryGraphSession` remains runtime-owned state, not a type relocated into semantic-scm.

### Technical Requirements

- `:kernel:semantic-scm` should be a typed contract module, not a hidden service module.
- Use Athena-native, vendor-neutral language only. Do not introduce types or fields such as `GitRepository`, `CommitHash`, `Branch`, `Head`, `Remote`, `ScmProvider`, or Theia provider/resource-group concepts in this story.
- If a contract needs repository/package identity, reuse existing `:kernel:repository-model` types such as:
  - `EngineeringRepository`
  - `RepositoryGraphReport`
  - `PackageIdentifier`
- If a contract needs engineering semantic identity, reuse existing `:kernel:engineering-model` types such as:
  - `StableSemanticIdentity`
  - `SourceProvenance`
- If a contract needs semantic diagnostics, reuse existing `:kernel:validation` types such as:
  - `SemanticDiagnostic`
  - `SemanticRuleId`
  - `SemanticDiagnosticCategory`
  - `SemanticDiagnosticSeverity`
- If a contract truly needs plugin-facing extension nouns, consume only the stable API from `:kernel:plugins:plugin-api`; do not reach into compiler-private plugin coordination packages.
- Prefer immutable `data class`, `enum class`, and `sealed interface` contracts consistent with the existing kernel model modules.
- Avoid helper logic beyond tiny invariants/defaults needed to keep the public contract coherent and testable.
- Do not put filesystem IO, repository scanning, diff algorithms, ServiceLoader usage, process execution, network access, or IDE transport code inside `:kernel:semantic-scm`.
- All public/core Kotlin classes added here need clean KDoc because the user explicitly wants the core Kotlin surface to stay easy to read.

### Architecture Compliance

- The story is only successful if semantic SCM meaning becomes easier to point to:
  - one physical module
  - one canonical package namespace
  - one documented ownership story
- Prevent these failure modes:
  - semantic SCM nouns are added under `com.engineeringood.athena.compiler.*`
  - runtime session types are moved into semantic-scm
  - semantic-scm takes a dependency on runtime or compiler and creates a future cycle
  - Git or Theia vocabulary leaks into the public semantic contract surface
  - plugin enrichment contracts are invented inside runtime or compiler-private code instead of relying on stable plugin-api seams
  - Story `1.1` widens into baseline loading, diff algorithms, adapter implementations, or IDE exposure

### Library / Framework Requirements

- Use the repo-approved stack already frozen in local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Follow the existing sibling-module Gradle pattern:
  - `plugins { alias(libs.plugins.kotlinJvm) }`
- Reuse existing Kotlin/JUnit test conventions from the root [`build.gradle.kts`](../../../build.gradle.kts).
- No third-party dependency should be added just to publish these typed contracts.

### File Structure Requirements

- Expected new files:
  - `kernel/semantic-scm/build.gradle.kts`
  - `kernel/semantic-scm/README.md`
  - `kernel/semantic-scm/README.zh-CN.md`
  - `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmModuleMarker.kt`
  - one or more contract files under `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/`
  - `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticScmModuleMarkerTest.kt`
  - focused contract tests under `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/`
- Expected update files:
  - [`settings.gradle.kts`](../../../settings.gradle.kts)
  - [`kernel/README.md`](../../../kernel/README.md)
  - [`kernel/README.zh-CN.md`](../../../kernel/README.zh-CN.md)
  - [`README.md`](../../../README.md)
  - [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md)
- Files whose current behavior must be preserved:
  - [`kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`](../../../kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt)
    - This remains the canonical repository/package contract boundary and must not absorb semantic SCM nouns.
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt)
    - This remains the runtime-owned active repository session; Story `1.1` must not move it or make semantic-scm depend directly on runtime.
  - [`kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`](../../../kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt)
    - This remains the stable plugin-facing semantic contribution surface; Story `1.1` must not duplicate plugin semantic contracts under semantic-scm.
  - [`kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt`](../../../kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt)
    - This remains the stable SPI root for hosted plugins; semantic-scm should consume it only if public contract types genuinely require stable plugin nouns.
- Explicit non-goals:
  - no baseline loader implementation
  - no Git adapter
  - no `integrations/scm-git` module yet
  - no semantic diff algorithm
  - no semantic review generation logic
  - no commit execution
  - no Theia/LSP transport additions
  - no graphical review or history UI

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test"`
- Recommended focused regression after the module exists:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Optional wider regression once focused tests are green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - `:kernel:semantic-scm` is a real physical module, not only a logical plan artifact
  - the module package root is `com.engineeringood.athena.scm`
  - the public contract surface stays VCS-neutral and typed
  - `:kernel:repository-model` remains free of SCM vocabulary
  - docs consistently describe semantic-scm as an M6 semantic boundary above repository-model, not a Git or Theia host
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`settings.gradle.kts`](../../../settings.gradle.kts) currently includes `:kernel:repository-model`, `:kernel:plugins:plugin-api`, `:kernel:plugins:plugin-host`, `:kernel:compiler`, and `:kernel:runtime`, but does not yet include `:kernel:semantic-scm` or `:integrations:scm-git`.
- [`kernel/repository-model/build.gradle.kts`](../../../kernel/repository-model/build.gradle.kts) is currently dependency-light and provides the model-module pattern Story `1.1` should emulate.
- [`kernel/compiler/build.gradle.kts`](../../../kernel/compiler/build.gradle.kts) currently depends on `:kernel:repository-model`, `:kernel:plugins:plugin-api`, `:kernel:plugins:plugin-host`, `:kernel:engineering-model`, `:kernel:language`, `:kernel:layout-model`, `:kernel:geometry-model`, `:kernel:svg-renderer`, and `:kernel:validation`.
- [`kernel/runtime/build.gradle.kts`](../../../kernel/runtime/build.gradle.kts) currently depends on `:kernel:compiler`, `:kernel:plugins:plugin-api`, `:kernel:plugins:plugin-host`, `:kernel:repository-model`, `:kernel:engineering-model`, `:kernel:geometry-model`, `:kernel:layout-model`, and `:kernel:svg-renderer`.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt) currently wraps `AthenaRepositoryReportPublicationResult` and exposes `RepositoryGraphReport?` as downstream runtime state.
- [`kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt`](../../../kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt) already publishes lowering, enrichment, and validation context types that semantic-scm must not duplicate.
- [`README.md`](../../../README.md) and [`kernel/README.md`](../../../kernel/README.md) currently document the kernel without a semantic-scm module entry.

### Previous Milestone Intelligence

- M5 Story `1.1` established the exact pattern for a physical contract-publication module:
  - real directory
  - bilingual README coverage
  - module marker
  - KDoc on public/core contract types
  - light dependency surface
- M3 locked in the stable split between `:kernel:plugins:plugin-api` and `:kernel:plugins:plugin-host`; Story `1.1` must preserve that separation and not invent hosted-lifecycle semantics inside semantic-scm.
- M5 locked in `:kernel:repository-model` as the repository/package contract boundary; Story `1.1` must consume that fact, not reopen it.
- The user has repeatedly enforced three workspace rules that matter directly here:
  - physical module structure must match the intended architecture
  - root package is `com.engineeringood`
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Recent milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - follow the same grouped-module discipline used in M1, M3, and M5
  - keep new kernel boundaries small, explicit, and documented
  - do not smuggle M6 semantics into runtime, compiler, or IDE packages before the dedicated boundary exists

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by the local M6 architecture and root build configuration:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Story `1.1` is a local contract-publication change, so external version chasing would add noise rather than implementation value.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- The new module should sit alongside the other kernel semantic/model boundaries:
  - `kernel/repository-model`
  - `kernel/engineering-model`
  - `kernel/validation`
- The naming should stay easy to understand:
  - module: `semantic-scm`
  - Gradle path: `:kernel:semantic-scm`
  - Kotlin package root: `com.engineeringood.athena.scm`
- This story should reduce future renaming churn by freezing the contract nouns now, but it should not over-spec future adapter or UI mechanics.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/implementation-readiness-report-2026-07-09.md]
- [Source: settings.gradle.kts]
- [Source: build.gradle.kts]
- [Source: README.md]
- [Source: kernel/README.md]
- [Source: kernel/runtime/README.md]
- [Source: kernel/plugins/plugin-api/README.md]
- [Source: kernel/plugins/plugin-host/README.md]
- [Source: kernel/repository-model/README.md]
- [Source: kernel/repository-model/build.gradle.kts]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryModelModuleMarker.kt]
- [Source: kernel/compiler/build.gradle.kts]
- [Source: kernel/runtime/build.gradle.kts]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt]
- [Source: _bmad-output/implementation-artifacts/m5/1-1-publish-canonical-repository-contracts-in-kernel-repository-model.md]

## Story Completion Status

- Status: review
- Completion note: Physical `:kernel:semantic-scm` module, typed semantic SCM contracts, module docs, and focused Java 25 verification are complete and ready for review.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 PRD, addendum, architecture spine, epics file, and implementation-readiness report review
- current module topology review through `settings.gradle.kts`, root docs, and kernel docs
- CodeGraph exploration of current repository-model, runtime session, compiler, and plugin semantic boundaries
- current `kernel/repository-model`, `kernel/compiler`, and `kernel/runtime` build dependency review
- current `RepositoryGraphSession`, repository contracts, and plugin semantic contract review
- recent commit history review
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test"` red phase confirming missing semantic-scm contract types
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test"` green phase after publishing the contract module
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:plugins:plugin-api:test :kernel:runtime:test"` focused regression verification

### Completion Notes List

- Identified Story `1.1` as the first backlog story in M6 and the Epic 1 entry point.
- Locked the new boundary to a physical `:kernel:semantic-scm` module with VCS-neutral typed contracts only.
- Flagged runtime/compiler cycle risk and explicitly constrained Story `1.1` to stay dependency-light and behavior-free.
- Captured the existing repository-model, runtime session, and plugin-api files whose semantics must be preserved.
- Published `SemanticBaselineDescriptor`, `SemanticBaselineSnapshot`, `SemanticChangeCategory`, `SemanticChangeLayer`, `SemanticChangeRecord`, `SemanticDerivedConsequence`, `SemanticDiff`, `SemanticReviewSummary`, `SemanticCommitIntent`, `SemanticHistorySummary`, `SemanticHistoryEntry`, and `SemanticScmAdapter` under `com.engineeringood.athena.scm`.
- Added KDoc to the full public contract surface and a `SemanticScmModuleMarker` consistent with sibling kernel modules.
- Updated the root, kernel, and workspace-summary docs so the M6 semantic SCM boundary appears in the live module map.
- Verified the new module and adjacent kernel boundaries sequentially under Java 25 on Windows.

### File List

- _bmad-output/implementation-artifacts/m6/1-1-publish-canonical-semantic-scm-contracts-in-kernel-semantic-scm.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md
- _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md
- _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md
- _bmad-output/planning-artifacts/epics-M6-2026-07-09.md
- _bmad-output/planning-artifacts/implementation-readiness-report-2026-07-09.md
- settings.gradle.kts
- build.gradle.kts
- README.md
- kernel/README.md
- kernel/runtime/README.md
- kernel/repository-model/README.md
- kernel/repository-model/build.gradle.kts
- kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt
- kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryModelModuleMarker.kt
- kernel/compiler/build.gradle.kts
- kernel/runtime/build.gradle.kts
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt
- kernel/plugins/plugin-api/README.md
- kernel/plugins/plugin-host/README.md
- kernel/plugins/plugin-api/build.gradle.kts
- kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaDomainSemanticsModel.kt
- kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt
- kernel/semantic-scm/build.gradle.kts
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmModuleMarker.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticScmContractsTest.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticScmModuleMarkerTest.kt
- docs/usages/athena-workspace-summary.md
- kernel/README.zh-CN.md
