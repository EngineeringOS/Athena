---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.2: Resolve Local-First Dependencies Into A Canonical Package Graph

Status: done

## Story

As a package author,
I want Athena to resolve declared dependencies through a local-first deterministic pipeline,
so that the repository produces one canonical semantic package graph from the same state every time.

## FR Traceability

- FR-4: resolve local and declared package dependencies deterministically
- FR-5: surface package-aware diagnostics through the semantic boundary
- FR-6: preserve canonical semantic authority during resolution
- FR-11: stable package meaning prepares later semantic SCM
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-2: resolution and graph results must be deterministic
- NFR-3: graph state and diagnostics remain inspectable

## Acceptance Criteria

1. Given a valid resolution input model, when the M5 resolver executes, then Athena resolves the primary package and its supported dependency sources through an explicit local-first deterministic sequence, and the same repository state yields the same resolved package graph.
2. Given unsupported remote or publish-oriented dependency sources are encountered, when resolution runs in M5, then Athena rejects them as out of scope for the milestone, and the resolver does not silently widen into registry or Git transport behavior.

## Tasks / Subtasks

- [x] Add the compiler-owned canonical graph-resolution result and resolver seams. (AC: 1, 2)
  - [x] Introduce a typed compiler result that packages the validated repository contract, resolution input, resolved package graph, and diagnostics together.
  - [x] Keep the graph resolver under `kernel/compiler` and the graph nouns under `kernel/repository-model`.
  - [x] Expose one narrow compiler-facing API for package-graph resolution without widening into lock writing, runtime session upgrades, or IDE transport changes.
- [x] Resolve `local-path` dependencies through a deterministic local-first sequence. (AC: 1)
  - [x] Resolve each `local-path` locator relative to the owning repository root unless already absolute.
  - [x] Load each target through the existing governed repository-contract and resolution-input seams rather than ad hoc directory guessing.
  - [x] Reject path targets whose repository contract is invalid or whose declared package identity does not match the dependency declaration.
- [x] Resolve `local-package` dependencies only through already known local graph context. (AC: 1, 2)
  - [x] Bind `local-package` references by package identity against already discovered packages in the current local graph context.
  - [x] Diagnose unresolved or ambiguous `local-package` references explicitly.
  - [x] Do not invent registry, Git, or package-search fallback behavior for `local-package`.
- [x] Materialize one deterministic canonical `ResolvedPackageGraph` from the local-first walk. (AC: 1)
  - [x] Include the root package plus resolved dependencies in stable order.
  - [x] Keep each package node inspectable with stable source-root information and direct dependency edges.
  - [x] Ensure duplicate or conflicting package identities are diagnosed instead of silently overwritten.
- [x] Cover valid transitive resolution and invalid local-first scenarios with focused tests and sequential verification. (AC: 1, 2)
  - [x] Prove deterministic graph output from the same repository state.
  - [x] Prove invalid dependency roots, mismatched package identities, and unresolved `local-package` references surface explicit diagnostics.
  - [x] Run Java 25 Gradle verification sequentially, including relevant module tests and a wider regression command.

## Dev Notes

### Story Intent

- Story `2.1` stopped at deterministic manifest dependency parsing and resolution-input construction.
- Story `2.2` now consumes that typed input and produces the first canonical local package graph.
- Story `2.3` still owns lock materialization and validation, so `2.2` must not write or validate `athena.lock`.
- Story `2.4` still owns downstream report publication for runtime, IDE, and M6 foundations.

### Architecture Guardrails

- Align to AD-14 by resolving package dependencies only through the manifest-declared, lock-independent package graph inputs.
- Align to AD-15 by keeping the resolver local-first, deterministic, and explicit about its sequence.
- Align to AD-16 by treating `athena.lock` as later derived state, not current resolver input authority.
- Preserve AD-17 and AD-18 by keeping Story `2.2` out of runtime session upgrades and Theia/LSP product work.

### Technical Requirements

- Reuse the Story `2.1` compiler seam: `AthenaCompiler.buildRepositoryResolutionInput(repositoryRoot)`.
- The current normalized dependency sources are:
  - `LOCAL_PATH`
  - `LOCAL_PACKAGE`
- Recommended first-cut M5 semantics:
  - `LOCAL_PATH` resolves another governed repository root from a filesystem path, relative to the owning repository root unless already absolute.
  - `LOCAL_PACKAGE` resolves only against package identities already discovered in the current local graph context.
- Do not introduce:
  - remote registries
  - Git transport
  - publish-oriented coordinates
  - fallback package search heuristics
  - IDE/runtime integration work
- The canonical graph output should reuse `ResolvedPackage` and `ResolvedPackageGraph` from `kernel/repository-model`.
- Prefer deterministic sorting at output boundaries even if traversal order is already constrained.

### Architecture Compliance

- Prevent these failure modes:
  - path resolution depends on ambient working directory rather than owning repository root
  - local-package references widen into hidden filesystem scanning or registry-like lookup
  - conflicting package identities overwrite one another silently
  - Story `2.2` writes lockfiles or upgrades runtime/session state
  - graph output ordering depends on host filesystem traversal order

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- No new dependencies are justified.

### File Structure Requirements

- Expected update files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryResolutionInputBuilder.kt`
  - `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
- Likely new files:
  - compiler-side graph resolution model/result file(s) under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/`
  - compiler-side graph resolver file(s) under the same package
  - matching tests under `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/`
- Files whose current behavior must be preserved:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
    - continues to own governed manifest parsing and contract validation
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryResolutionInputBuilder.kt`
    - continues to own pre-resolution input normalization only

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - deterministic graph output from the same repository state
  - transitive local-path resolution works
  - unresolved local-package references and mismatched package identities diagnose explicitly
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- Story `2.1` already added:
  - `RepositoryResolutionDependency`
  - `RepositoryResolutionInput`
  - `AthenaRepositoryResolutionInputResult`
  - `AthenaCompiler.buildRepositoryResolutionInput(repositoryRoot)`
- The current graph types already exist in repository-model:
  - `ResolvedPackage`
  - `ResolvedPackageGraph`
  - `RepositoryGraphReport`

### Previous Story Intelligence

- Story `2.1` proved dependency intent can be normalized and diagnosed without touching runtime or IDE seams. Story `2.2` should preserve that boundary discipline.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical structure and docs must match the architecture
  - Gradle verification must stay sequential on Windows

### Latest Technical Information

- No web research is required for Story `2.2`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `2.2` should leave clean room for:
  - Story `2.3` lock materialization and validation
  - Story `2.4` graph report and diagnostics publication

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-22-resolve-local-first-dependencies-into-a-canonical-package-graph]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/2-1-build-deterministic-resolution-inputs-from-the-repository-contract.md]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryResolutionInputBuilder.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryResolutionInputModel.kt]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, addendum, and architecture spine review
- Story `2.1` completion notes and guardrails review
- repository-model and compiler seam review after Story `2.1`
- dependency-source contract search across kernel and milestone artifacts

### Completion Notes List

- Added a compiler-owned graph resolution result and resolver seam, exposed through `AthenaCompiler.resolveRepositoryGraph(repositoryRoot)`.
- Implemented deterministic `LOCAL_PATH` and `LOCAL_PACKAGE` resolution into a stable `ResolvedPackageGraph`, with explicit diagnostics for invalid targets, identity mismatch, unresolved references, ambiguity, and identity conflicts.
- Added a resolver-only repository-contract load option so nested governed subrepositories can participate in local graph resolution without weakening the default strict repository validator.
- Proved deterministic transitive local graph resolution and diagnostic behavior with focused compiler tests.
- Verified sequentially on Windows with Java 25 using `:kernel:compiler:test` and full `build`.

### File List

- _bmad-output/implementation-artifacts/m5/2-2-resolve-local-first-dependencies-into-a-canonical-package-graph.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoadOptions.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolutionModel.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolver.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolverTest.kt

## Change Log

- 2026-07-09: Created Story `2.2` context and moved it to in-progress.
- 2026-07-09: Implemented deterministic local-first package graph resolution, added nested governed-repository handling for graph resolution, and passed Java 25 sequential verification.
