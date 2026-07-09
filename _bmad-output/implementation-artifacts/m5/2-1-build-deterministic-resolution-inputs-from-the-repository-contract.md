---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.1: Build Deterministic Resolution Inputs From The Repository Contract

Status: done

## Story

As a package author,
I want Athena to turn manifest dependency declarations into a governed resolution input model,
so that package resolution starts from explicit semantic intent instead of ad hoc path discovery.

## FR Traceability

- FR-4: resolve local and declared package dependencies deterministically
- FR-5: surface package-aware diagnostics through the semantic boundary
- FR-6: preserve canonical semantic authority during resolution
- FR-11: stable package meaning prepares later semantic SCM
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-2: resolution inputs must be deterministic from the same repository state
- NFR-3: manifest and resolution inputs remain inspectable for debugging

## Acceptance Criteria

1. Given a governed Athena repository with dependency declarations in `athena.yaml`, when the compiler loads the repository contract for resolution, then Athena derives a typed resolution input from manifest-owned package identity and dependency declarations, and the input ordering and normalization rules are deterministic.
2. Given a dependency declaration is malformed or unsupported, when resolution input construction runs, then Athena rejects that declaration through Athena-owned package diagnostics, and the error is reported without falling back to filesystem heuristics or classpath coincidence.

## Tasks / Subtasks

- [x] Extend the canonical repository-model boundary with the narrow typed nouns required for resolution-input construction. (AC: 1, 2)
  - [x] Add dedicated repository-model types for resolution-input state rather than overloading later lock or graph result types.
  - [x] Keep the new types authored-intent-derived and JVM-first; do not introduce Node or frontend-owned mirror models.
  - [x] Preserve the existing `PackageDependency` contract if it still fits, or tighten it only where Story `2.1` requires explicit normalization and validation.
- [x] Parse manifest dependency declarations through the existing compiler-owned repository-contract loader. (AC: 1, 2)
  - [x] Extend `AthenaRepositoryContractLoader` so `athena.yaml` can declare dependency entries in a deterministic format under the root manifest.
  - [x] Normalize dependency ordering and stable field handling during manifest load instead of leaving order to later resolver heuristics.
  - [x] Keep package-local manifests, remote registry coordinates, Git transport, and publish metadata out of scope.
- [x] Build a compiler-owned resolution-input result from the validated repository contract. (AC: 1)
  - [x] Add a compiler-facing API that turns a governed repository contract into a typed resolution input result.
  - [x] Make the result inspectable enough for later resolver, runtime, and IDE consumers without implementing actual dependency resolution yet.
  - [x] Keep semantic authority in `kernel/compiler` and `kernel/repository-model`; do not move any resolution-input logic into `ide/*`.
- [x] Reject malformed or unsupported dependency intent through repository/package diagnostics. (AC: 2)
  - [x] Report invalid dependency names, missing required fields, blank locators where required, and unsupported dependency sources explicitly.
  - [x] Publish those failures as Athena-owned diagnostics through the same JVM seam used by repository contract validation.
  - [x] Do not silently coerce malformed declarations into guessed filesystem traversal, classpath lookup, or fallback package meaning.
- [x] Cover deterministic valid and invalid paths with focused tests and sequential verification. (AC: 1, 2)
  - [x] Add repository-model tests for the new typed resolution-input contracts if new contracts are introduced there.
  - [x] Add compiler tests proving deterministic dependency-order normalization and invalid dependency diagnostics.
  - [x] Run Java 25 Gradle verification sequentially, including the relevant module tests and a wider regression command.

## Dev Notes

### Story Intent

- Story `1.1` created the repository-model boundary and seeded dependency-related nouns.
- Story `1.2` proved governed repository-root validation and manifest parsing for the primary package only.
- Story `1.3` and `1.4` adapted create and open flows to the governed M5 repository contract.
- Story `2.1` is the first real package-graph step: it must stop at deterministic resolution-input construction and diagnostics, leaving actual dependency resolution and lock materialization to Stories `2.2` and `2.3`.

### Architecture Guardrails

- Align to AD-13 by keeping all repository/package resolution-input nouns in `kernel/repository-model`.
- Align to AD-14 by ensuring dependency meaning comes only from manifest-owned declarations, not filesystem or classpath coincidence.
- Align to AD-15 by making the input-construction path deterministic and local-first.
- Align to AD-16 by keeping `athena.lock` out of authored dependency intent; Story `2.1` should read intent from `athena.yaml`, not from the lock.
- Preserve AD-17 and AD-18 by keeping this story out of `RepositoryGraphSession` and Theia IDE semantics work.

### Technical Requirements

- The existing repository-model seam is `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`.
- The existing compiler seam is `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`.
- The current compiler facade only exposes `validateRepositoryContract(repositoryRoot)`. Story `2.1` should add the next narrow facade for resolution-input construction rather than overloading repository-open or IDE-specific seams.
- The current manifest parser understands `primaryPackage:` only. Story `2.1` will need to extend parsing to a deterministic dependency block format.
- Existing repository-model contracts already contain:
  - `PackageDependencySource` with `LOCAL_PATH` and `LOCAL_PACKAGE`
  - `PackageDependency`
  - `ResolvedPackageGraph` and `RepositoryGraphReport`
- Be careful not to skip directly to resolved package graph construction here. Story `2.1` owns inputs, not final graph state.
- Keep the implementation JVM-only for now; `ide/lsp`, Theia frontend, and Theia backend should not be touched unless a truly unavoidable compiler-facade exposure is missing.

### Architecture Compliance

- Prevent these failure modes:
  - dependency declarations are parsed in a loose or order-dependent way
  - malformed dependencies are ignored instead of diagnosed
  - Story `2.1` silently implements full package resolution, lock writing, or runtime session graph state
  - repository-model and compiler define duplicate resolution-input shapes
  - any fallback logic reaches into raw filesystem discovery or classpath coincidence to "help" malformed dependency declarations

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- No new third-party parser dependency is justified here. Extend the existing narrow manifest parser unless the current seam makes that impossible.

### File Structure Requirements

- Expected update files:
  - `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
  - `kernel/repository-model/src/test/kotlin/com/engineeringood/athena/repository/RepositoryContractsTest.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractValidationModel.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt`
- Likely new files:
  - one or more compiler-side repository resolution-input model/result files under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/`
  - matching tests under `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/`
- Files whose current behavior must be preserved:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt`
    - Story `1.4` already switched open semantics to contract-aware seed state; do not couple Story `2.1` to IDE transport
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
    - existing primary-package and layout validation must remain intact while dependency parsing is added

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - deterministic ordering from the same dependency declarations
  - invalid dependency declarations produce explicit diagnostics
  - no IDE or frontend regression is introduced by compiler-facing API additions
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- `RepositoryManifest` already contains `dependencies: List<PackageDependency> = emptyList()`, but the loader does not populate it yet.
- `PackageDependencySource` currently includes only `LOCAL_PATH` and `LOCAL_PACKAGE`; unsupported future sources should stay rejected.
- `AthenaRepositoryContractValidationResult` currently packages the validated manifest and diagnostics only.
- `AthenaCompiler.validateRepositoryContract()` already exposes the current repository validation seam used by Story `1.4`.

### Previous Story Intelligence

- Story `1.4` proved the compiler-owned contract seam can already drive downstream open behavior without moving authority into Theia. Story `2.1` should follow that pattern and keep package-input authority in the same JVM boundary.
- Story `1.4` also showed that current runtime and IDE paths can consume richer contract metadata later without needing to own parsing logic now.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical structure and docs must match the architecture
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent milestone commits progressed by freezing one boundary at a time: runtime workspace in M1, projection in M2, plugin contracts in M3, and Theia/LSP product seams in M4/M5.
- Practical implication:
  - implement Story `2.1` as another narrow boundary-hardening step instead of collapsing multiple Epic 2 stories together.

### Latest Technical Information

- No web research is required for Story `2.1`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `2.1` should leave clean room for:
  - Story `2.2` actual local-first dependency resolution into a canonical graph
  - Story `2.3` lock materialization and validation
  - Story `2.4` runtime/IDE-facing graph reports and diagnostics publication

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-21-build-deterministic-resolution-inputs-from-the-repository-contract]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/1-4-open-a-governed-repository-into-a-contract-aware-session-seed.md]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractValidationModel.kt]

## Story Completion Status

- Status: done
- Completion note: Implemented deterministic repository resolution-input construction, added manifest dependency parsing and diagnostics, and passed sequential Java 25 verification through the full build.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, addendum, and architecture spine review
- Story `1.4` completion notes and guardrails review
- CodeGraph exploration of repository-model and compiler repository seams
- current `RepositoryContracts.kt` review
- current `AthenaRepositoryContractLoader.kt` and validation model review
- recent milestone commit log review
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain build"`

### Completion Notes List

- Added dedicated repository-model resolution-input types so authored dependency intent can be carried into later resolver stories without overloading resolved-graph or lock contracts.
- Extended the compiler-owned manifest loader to parse deterministic `dependencies:` entries, normalize supported dependency sources, normalize local-path locators, and reject malformed dependency declarations explicitly.
- Added a compiler-facing `buildRepositoryResolutionInput(repositoryRoot)` seam that packages the validated repository contract, deterministic resolution input, and diagnostics together.
- Added repository-model and compiler tests covering deterministic dependency ordering, normalized locator paths, malformed dependency diagnostics, and unsupported source rejection.
- Kept the work JVM-only and stopped before actual dependency resolution, lock materialization, runtime session graph state, or IDE transport changes.

### File List

- _bmad-output/implementation-artifacts/m5/2-1-build-deterministic-resolution-inputs-from-the-repository-contract.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryResolutionInputBuilder.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryResolutionInputModel.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryResolutionInputBuilderTest.kt
- kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt
- kernel/repository-model/src/test/kotlin/com/engineeringood/athena/repository/RepositoryContractsTest.kt

## Change Log

- 2026-07-09: Created Story `2.1` context and moved it to ready-for-dev.
- 2026-07-09: Implemented deterministic manifest dependency parsing and compiler-owned resolution-input construction, added focused repository-model and compiler coverage, and passed sequential Java 25 verification through the full build.
