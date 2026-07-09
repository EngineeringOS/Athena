---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.3: Materialize And Validate `athena.lock` From Resolver Output

Status: done

## Story

As a platform engineer,
I want Athena to write and validate `athena.lock` from canonical resolver output,
so that lock state is inspectable, reproducible, and clearly subordinate to manifest intent.

## FR Traceability

- FR-2: define a deterministic lock contract
- FR-4: resolve local and declared package dependencies deterministically
- FR-5: surface package-aware diagnostics through the semantic boundary
- FR-6: preserve canonical semantic authority during resolution
- FR-11: stable package meaning prepares later semantic SCM
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-2: lock results must be deterministic and reproducible from the same repository state
- NFR-3: lock state and lock diagnostics remain inspectable for debugging

## Acceptance Criteria

1. Given a successful resolved package graph, when Athena materializes lock state, then it writes a stable-order `athena.lock` that captures the reproducibility-critical derived resolution result, and the lock content is derived from resolver authority rather than handwritten dependency intent.
2. Given `athena.lock` content differs from the resolver result or contains invalid edits, when Athena validates lock state, then it reports lock inconsistencies through Athena-owned diagnostics, and manifest intent in `athena.yaml` remains the governing source of dependency meaning.

## Tasks / Subtasks

- [x] Add the narrow compiler-owned lock materialization and validation seam. (AC: 1, 2)
  - [x] Introduce a typed compiler result that packages the validated repository contract, canonical graph result, materialized `RepositoryLock`, and lock diagnostics together.
  - [x] Expose compiler-facing APIs for lock materialization and lock validation without widening into runtime session upgrades or IDE transport work.
  - [x] Keep `RepositoryLock` as the canonical repository-model noun; do not invent parallel lock models in `ide/*`.
- [x] Define deterministic `athena.lock` serialization from canonical resolver output. (AC: 1)
  - [x] Serialize the resolved root package and resolved package nodes in stable order.
  - [x] Keep the emitted lock readable and inspectable as Athena-owned derived state rather than generic package-manager boilerplate.
  - [x] Ensure the same graph state emits byte-for-byte stable lock content from the same repository state.
- [x] Validate on-disk `athena.lock` against resolver authority and explicit lock syntax rules. (AC: 2)
  - [x] Parse the current lock file through a compiler-owned seam rather than ad hoc string comparison.
  - [x] Report invalid lock structure, unsupported edits, or stale lock content explicitly through Athena-owned diagnostics.
  - [x] Keep `athena.yaml` plus resolver output authoritative; lock content must not override manifest intent or dependency meaning.
- [x] Keep existing bootstrap and repository-open behavior compatible with the now-real lock contract. (AC: 1, 2)
  - [x] Update governed repository fixtures or bootstrap expectations only where the new canonical lock contract requires it.
  - [x] Preserve existing open-path contract validation and IDE seed behavior unless a lock-contract compatibility change is unavoidable.
- [x] Cover deterministic valid and invalid lock paths with focused tests and sequential verification. (AC: 1, 2)
  - [x] Prove stable lock emission from the same repository state.
  - [x] Prove stale lock content and invalid lock edits surface explicit diagnostics.
  - [x] Run Java 25 Gradle verification sequentially, including relevant module tests and a wider regression command.

## Dev Notes

### Story Intent

- Story `2.1` normalized manifest dependency intent into deterministic resolution input.
- Story `2.2` resolved that input into the first canonical local-first package graph.
- Story `2.3` now turns canonical graph authority into the real `athena.lock` contract and validates that on-disk lock state stays subordinate to manifest plus resolver authority.
- Story `2.4` still owns downstream package-report publication for runtime, IDE, and M6 foundations.

### Architecture Guardrails

- Align to AD-15 by keeping lock production part of the explicit compiler-owned deterministic resolution sequence.
- Align to AD-16 by treating `athena.lock` strictly as derived state that records resolver output and never authored dependency intent.
- Preserve AD-13 by reusing `RepositoryLock` and related repository-model nouns rather than creating alternate lock DTOs elsewhere.
- Preserve AD-17 and AD-18 by keeping Story `2.3` out of runtime `RepositoryGraphSession` work and Theia/LSP transport changes except for compatibility fallout.

### Technical Requirements

- Reuse Story `2.2` compiler seam: `AthenaCompiler.resolveRepositoryGraph(repositoryRoot)`.
- The current graph result already exposes:
  - validated repository contract
  - deterministic resolution input
  - canonical `ResolvedPackageGraph`
  - inspectable diagnostics
- Story `2.3` should add the next compiler-owned seam for:
  - deriving `RepositoryLock` from canonical graph output
  - rendering canonical `athena.lock` text deterministically
  - validating on-disk lock content against compiler-owned authority
- Keep parser and serializer logic JVM-owned under `kernel/compiler`; do not move lock interpretation into Node, Theia, or LSP adapters.
- The lock contract should remain intentionally narrow in M5:
  - stable lock format version
  - root package identity
  - resolved package entries in stable order
  - stable direct dependency edges
  - stable source-root information as required for reproducibility
- Do not introduce:
  - remote registry coordinates
  - Git transport metadata
  - publish metadata
  - runtime session state upgrades
  - IDE-facing report publication beyond what current compatibility tests require

### Architecture Compliance

- Prevent these failure modes:
  - lock bytes depend on host traversal order or incidental map iteration order
  - manual lock edits silently override manifest/resolver authority
  - lock validation is implemented as brittle raw-text equality without typed parsing
  - Story `2.3` widens into runtime or IDE package-report publication
  - bootstrap or repository-open paths quietly keep depending on obsolete placeholder lock content

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- No new serialization dependency is justified. Extend the existing narrow compiler-owned text parsing/formatting approach.

### File Structure Requirements

- Expected update files:
  - `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolutionModel.kt`
- Likely new files:
  - one or more compiler-side lock materialization / validation model files under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/`
  - one or more compiler-side lock parser / serializer files under the same package
  - matching tests under `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/`
- Files whose current behavior must be preserved:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
    - continues to own manifest and layout validation; lock content semantics should remain separate
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt`
    - should continue to rely on compiler-owned contract validation without becoming a lock parser

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - stable lock bytes from the same canonical graph state
  - stale or manually edited lock content produces explicit diagnostics
  - current create/open repository flows remain compatible with the canonical lock contract after necessary fixture or bootstrap updates
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- `RepositoryLock` already exists in `kernel/repository-model`, but current compiler flows do not materialize or validate it yet.
- `AthenaRepositoryContractLoader` currently validates lock presence and root placement only; content semantics were intentionally deferred to Story `2.3`.
- `AthenaRepositoryGraphResolver` already produces deterministic canonical graph state without touching the lock file.
- Existing repository bootstrap and LSP fixtures still seed a placeholder `athena.lock` shape (`version: 1`, `packages: []`) that may need compatibility updates if the canonical contract becomes stricter.

### Previous Story Intelligence

- Story `2.2` proved the local-first graph is already deterministic and inspectable, so Story `2.3` should treat that graph as the only source of lock truth.
- The nested governed-subrepository allowance added in Story `2.2` is resolver-only. Do not accidentally weaken default repository validation while adding lock parsing or validation.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical structure and docs must match the architecture
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent M5 work hardened one repository/package boundary at a time: contract, input, then graph.
- Practical implication:
  - implement Story `2.3` as the next narrow compiler-owned boundary step rather than blending it with Story `2.4` publication work.

### Latest Technical Information

- No web research is required for Story `2.3`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `2.3` should leave clean room for:
  - Story `2.4` package-report and diagnostics publication
  - Epic 3 runtime session and IDE protocol exposure

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-23-materialize-and-validate-athenalock-from-resolver-output]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/2-1-build-deterministic-resolution-inputs-from-the-repository-contract.md]
- [Source: _bmad-output/implementation-artifacts/m5/2-2-resolve-local-first-dependencies-into-a-canonical-package-graph.md]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolutionModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolver.kt]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, addendum, and architecture spine review
- Story `2.1` and `2.2` completion notes and guardrails review
- current `RepositoryContracts.kt`, graph-resolution model, and graph resolver review
- current bootstrap and LSP fixture lock-shape review
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:compiler:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain build"`
- `node --test ide/theia-backend/scripts/athena-repository-bootstrapper.test.mjs`

### Completion Notes List

- Added a compiler-owned lock materialization seam that derives canonical `RepositoryLock` state from the resolved package graph, renders stable `athena.lock` text, and writes it to disk.
- Added a compiler-owned lock validation seam that parses on-disk `athena.lock`, validates lock structure, and reports stale or non-canonical lock content through Athena-owned diagnostics.
- Extended the canonical `RepositoryLock` contract with explicit lock format versioning while keeping the lock noun in `kernel/repository-model`.
- Updated governed repository bootstrap and LSP test fixtures to seed the real canonical lock shape instead of the old placeholder `packages: []` contract.
- Verified sequentially on Windows with Java 25 using focused repository-model and compiler tests, the full Gradle build, and the direct Node bootstrapper test.

### File List

- _bmad-output/implementation-artifacts/m5/2-3-materialize-and-validate-athena-lock-from-resolver-output.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServerTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspTestFixtures.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolverTest.kt
- ide/theia-backend/lib/node/athena-repository-bootstrapper.js
- ide/theia-backend/scripts/athena-repository-bootstrapper.test.mjs
- ide/theia-backend/src/node/athena-repository-bootstrapper.ts
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializationModel.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializerTest.kt
- kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt
- kernel/repository-model/src/test/kotlin/com/engineeringood/athena/repository/RepositoryContractsTest.kt

## Change Log

- 2026-07-09: Created Story `2.3` context and moved it to in-progress.
- 2026-07-09: Implemented canonical lock materialization and validation, normalized seeded lock fixtures, and passed sequential Java 25 plus backend bootstrap verification.
