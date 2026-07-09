---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.2: Validate Repository Root Contract And Primary Package Layout

Status: done

## Story

As a package author,
I want Athena to validate the repository-root contract and primary package layout early,
so that invalid repository structure fails fast before deeper package-graph work begins.

## FR Traceability

- FR-1: stable repository manifest contract
- FR-2: deterministic lock contract presence at repository root
- FR-3: package identity and governed source layout rules
- FR-5: package-aware diagnostics for contract failures
- FR-8: governed repository open/create flows need a validated contract base
- NFR-1: repository/package meaning stays owned by compiler and runtime layers
- NFR-2: repository validation must be deterministic
- NFR-3: contract diagnostics must remain inspectable

## Acceptance Criteria

1. Given a repository with `athena.yaml`, `athena.lock`, and `src/`, when Athena loads the repository contract, then it validates repository-root presence, primary package identity, and governed authored-source placement under `src/`, and invalid root structure or package identity is rejected through Athena-owned diagnostics.
2. Given a repository violates M5 layout rules, when validation runs, then Athena reports explicit contract errors for missing manifest fields, invalid layout, or unsupported package-local manifest behavior, and those errors are inspectable through the JVM-owned semantic path.

## Tasks / Subtasks

- [x] Add a compiler-owned repository contract loading and validation seam that consumes `:kernel:repository-model` as the canonical noun set. (AC: 1, 2)
  - [x] Create a focused repository-validation package under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/`.
  - [x] Keep `RepositoryManifest`, `RepositoryLock`, `PrimaryPackage`, `EngineeringRepository`, and `RepositoryDiagnostic` sourced from `:kernel:repository-model` rather than redefining them in compiler or LSP.
  - [x] Add KDoc to all new public/core Kotlin classes.
- [x] Load the governed root contract from the repository root with deterministic rules. (AC: 1, 2)
  - [x] Require the canonical root files `athena.yaml` and `athena.lock` to be addressed at repository root only.
  - [x] Parse the narrow M5 manifest fields needed for this story: primary package name, optional version, and governed source root.
  - [x] Treat missing root manifest content, blank package identity, or unsupported source-root values as contract diagnostics rather than frontend heuristics.
  - [x] Keep parsing/loading local and dependency-free; do not add YAML libraries or network behavior for this story.
- [x] Validate the primary package layout and reject unsupported repository structure. (AC: 1, 2)
  - [x] Validate that the primary package source root is governed under `src/` for M5.
  - [x] Reject unsupported package-local manifests or nested `athena.yaml` files under the repository tree.
  - [x] Validate canonical root shape explicitly: repository root exists, is a directory, and surfaces missing manifest/lock or invalid layout through deterministic diagnostics.
  - [x] Keep multi-package repository authoring out of scope; this story validates and rejects unsupported layouts instead of widening the contract.
- [x] Publish an inspectable JVM-owned validation result for downstream runtime/LSP use. (AC: 1, 2)
  - [x] Return a typed validation/load result that includes the normalized repository root, any loaded canonical repository contract, and ordered repository diagnostics.
  - [x] Keep this as a compiler-owned boundary for now so Story `1.4` can consume it during governed repository open without re-inventing validation.
  - [x] Do not rewrite `AthenaLspSessionHost`, `AthenaRepositoryResolver`, or runtime session ownership into the full open-flow upgrade yet; Story `1.4` owns that adaptation.
- [x] Cover the validation rules with focused tests and preserve current regressions. (AC: 1, 2)
  - [x] Add compiler tests for valid root contract loading, missing manifest fields, invalid primary package identity, missing/invalid source-root layout, and nested manifest rejection.
  - [x] Add any minimal LSP-side or integration-facing tests only if needed to prove the result remains inspectable without moving semantic authority into the frontend.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not overlap build or test commands.

## Dev Notes

### Story Intent

- Story `1.1` froze the canonical repository/package nouns in `:kernel:repository-model`.
- Story `1.2` is the first behavior story for those nouns, but it is still validation-first, not session-first.
- The goal is to prove that Athena can load and reject repository-root contract state deterministically before open/create-flow adaptation and before package-graph resolution.
- Story `1.3` owns repository creation under the new contract.
- Story `1.4` owns governed repository open into contract-aware seed state.
- Story `2.x` owns dependency resolution and lock materialization semantics.

### Architecture Guardrails

- Align to AD-11 by recognizing exactly one repository-root `athena.yaml` and one repository-root `athena.lock` in M5.
- Align to AD-12 by validating one primary package per repository and keeping authored sources under governed `src/`.
- Align to AD-13 by consuming `:kernel:repository-model` for canonical contract nouns and diagnostics.
- Align to AD-15 by keeping validation deterministic, local-first, and independent of remote/package-transport behavior.
- Align to AD-16 by treating `athena.lock` as required derived contract state at the root, but not yet as the story that computes final resolver output.
- Preserve inherited AD-3 and AD-5: compiler/runtime remain the semantic owners and `ide/lsp` remains only the transport boundary when later stories consume this seam.

### Technical Requirements

- Prefer a small compiler-owned loading/validation surface such as:
  - `AthenaRepositoryContractLoader`
  - `AthenaRepositoryContractValidationResult`
  - helper parser/diagnostic types only if strictly needed
- Keep all canonical data shapes rooted in `com.engineeringood.athena.repository`.
- The loader must normalize repository roots deterministically.
- The result should preserve diagnostic order deterministically so repeated validation of the same tree yields the same output.
- Primary package identity should reject obviously invalid forms:
  - missing or blank package name
  - blank version if the field is present
  - unsupported source-root values outside `src`
- Treat nested `athena.yaml` under subdirectories as unsupported package-local manifest behavior in M5.
- It is acceptable for `athena.lock` content validation to stay narrow in this story. Presence and root placement matter now; resolver-derived content semantics belong to Story `2.3`.
- Do not add SCM, Git, registry, publish, or multi-package concepts here.

### Architecture Compliance

- Prevent these failure modes:
  - validation logic appears in Theia frontend or Node code
  - `ide/lsp` invents its own repository contract model
  - repository root validation depends on arbitrary `.athena` file discovery instead of governed root contracts
  - the loader silently accepts package-local manifests or non-`src` authored roots
  - M5 validation widens into full repository open/session upgrade too early

### Library / Framework Requirements

- Use the repo-approved stack already frozen in planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Keep this story dependency-free beyond the current standard library and test stack.
- Do not add a YAML parser library for this story; a narrow deterministic manifest reader is sufficient and keeps the contract understandable.

### File Structure Requirements

- Expected new main files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/...`
- Expected new test files:
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/...`
  - optional test resources under `kernel/compiler/src/test/resources/repository-contracts/...`
- Likely update files:
  - `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt` only if a small missing canonical helper/report type is truly needed
  - `kernel/compiler/README.md` if the new repository-validation seam should be documented
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt` only if adapter comments need tightening; not for full behavior takeover
- Files whose current behavior must be preserved:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt`
    - still the M4 single-source resolver until Story `1.4` upgrades the open path
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
    - still owns the current runtime activation seam; do not collapse 1.2 into 1.4
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
    - runtime workspace lifecycle stays intact for this story
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt`
    - active project selection remains separate from repository-contract validation

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Preferred wider regression once focused tests are green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - valid root contract loads deterministically
  - invalid repository root shape yields explicit ordered diagnostics
  - nested/package-local manifest behavior is rejected explicitly
  - no frontend-owned repository validation logic is introduced
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- `AthenaRepositoryResolver` currently resolves one authored `.athena` file under `src/` and rejects multiple sources.
- `AthenaLspSessionHost` currently opens a runtime workspace directly from that resolver result.
- `AthenaRuntime` and `AthenaWorkspace` currently own one active workspace and one active project without package-graph session state.
- `RepositoryContracts.kt` currently publishes the canonical repository nouns but does not yet provide a compiler-owned load/validation workflow.

### Previous Story Intelligence

- Story `1.1` already established the physical `:kernel:repository-model` module and wired compiler/runtime/LSP to it.
- Story `1.1` also recorded the explicit rule that repository-model must stay VCS-neutral.
- The user’s standing constraints still apply here:
  - physical structure must match architecture
  - package root is `com.engineeringood`
  - core Kotlin classes need readable KDoc
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Recent relevant commits:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - keep the implementation narrow, typed, and physically grouped under kernel/compiler rather than scattering repository logic across the workspace

### Latest Technical Information

- No web research is required for Story `1.2`.
- The controlling local stack remains:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `1.2` should establish the reusable validation seam that Stories `1.3` and `1.4` consume next.
- Keep the current M4 LSP open-path adapter intact enough that later stories can replace it deliberately rather than by accidental scope creep here.

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-12-validate-repository-root-contract-and-primary-package-layout]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/implementation-readiness-report-2026-07-08-m5.md]
- [Source: _bmad-output/implementation-artifacts/m5/1-1-publish-canonical-repository-contracts-in-kernel-repository-model.md]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolver.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt]

## Story Completion Status

- Status: done
- Completion note: Added a compiler-owned repository contract loader plus validation result model, exposed the seam through `AthenaCompiler`, covered the M5 root-contract rules with focused tests, recovered from one mistaken parallel Gradle invocation by terminating the stray Java process, and finished with sequential Java 25 verification including full `build`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 PRD, addendum, architecture spine, epics file, and implementation-readiness report review
- prior Story `1.1` review and completion notes
- CodeGraph exploration of current LSP resolver/session seams and runtime workspace state
- current compiler knowledge/boundary resolver patterns review
- current repository-model contracts and repository resolver tests review
- recent commit history review
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- mistaken parallel verification attempt that launched `:kernel:runtime:test` and `:ide:lsp:test` together, timed out the LSP-side shell command, and left one stray Java Gradle process
- `Stop-Process` cleanup of the stray Java process from the mistaken parallel verification attempt
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test"` rerun sequentially
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain build"`

### Completion Notes List

- Added `AthenaRepositoryContractLoader` and `AthenaRepositoryContractValidationResult` under `kernel/compiler/repository`.
- Implemented a dependency-free narrow manifest reader for `primaryPackage.name`, optional `version`, and governed `sourceRoot`.
- Added deterministic diagnostics for missing root files, invalid primary package identity, unsupported source-root values, nested manifests, and `.athena` sources outside `src/`.
- Exposed repository-contract validation through `AthenaCompiler.validateRepositoryContract(...)` so later runtime and LSP stories can consume the JVM-owned semantic path directly.
- Updated compiler module documentation in English and Chinese to include the new repository-validation area.
- Added focused compiler tests for valid contract loading, invalid identity, invalid layout, nested manifest rejection, and compiler-facade access.
- Accidentally invoked runtime and LSP Gradle verification in parallel once, then terminated the stray Java process and reran verification sequentially per workspace policy.

### File List

- _bmad-output/implementation-artifacts/m5/1-2-validate-repository-root-contract-and-primary-package-layout.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- kernel/compiler/README.md
- kernel/compiler/README.zh-CN.md
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractValidationModel.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt

## Change Log

- 2026-07-09: Created Story `1.2` context and moved it to ready-for-dev.
- 2026-07-09: Implemented compiler-owned repository contract validation, exposed the seam through `AthenaCompiler`, added focused tests, and completed sequential Java 25 verification.
