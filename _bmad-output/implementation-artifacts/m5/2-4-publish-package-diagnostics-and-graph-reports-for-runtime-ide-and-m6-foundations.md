---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.4: Publish Package Diagnostics And Graph Reports For Runtime, IDE, And M6 Foundations

Status: done

## Story

As a language-tooling engineer,
I want Athena to publish package-aware diagnostics and resolved graph reports from the compiler/runtime path,
so that the IDE can consume package semantics now and M6 can build later on stable package meaning.

## FR Traceability

- FR-4: resolve local and declared package dependencies deterministically
- FR-5: surface package-aware diagnostics through the semantic boundary
- FR-6: preserve canonical semantic authority during resolution
- FR-7: upgrade the active runtime-backed repository session into a package graph session
- FR-9: surface package state in the existing Athena IDE path
- FR-11: stable package meaning prepares later semantic SCM
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-2: report results must be deterministic and reproducible from the same repository state
- NFR-3: manifest, lock, graph, and diagnostics remain inspectable for debugging

## Acceptance Criteria

1. Given repository validation, resolution, and lock materialization have run, when Athena exposes the result to downstream consumers, then it produces package-aware diagnostics plus a resolved package-graph report through JVM-owned semantic services, and those outputs are inspectable enough for runtime, IDE, and architecture debugging.
2. Given later milestones such as semantic SCM need stable package meaning, when the M5 outputs are reviewed, then package identity, dependency meaning, and resolved graph state are available as stable semantic foundations, and M5 does not implement semantic diff, review, or publish workflows itself.

## Tasks / Subtasks

- [x] Add the narrow compiler-owned repository report publication seam. (AC: 1, 2)
  - [x] Introduce a typed compiler publication result that packages canonical repository report state plus package diagnostics and relevant validation/materialization metadata.
  - [x] Expose one narrow compiler-facing API for publishing the current repository graph report without widening into LSP transport or runtime session ownership.
  - [x] Keep `RepositoryGraphReport` in `kernel/repository-model` as the canonical report noun; do not invent frontend or Node-owned mirror report shapes.
- [x] Publish canonical report state from manifest, lock, graph, and diagnostic authority. (AC: 1)
  - [x] Ensure published report state carries the canonical manifest, canonical lock, resolved graph, and aggregated package diagnostics from the compiler-owned sequence.
  - [x] Preserve deterministic output from the same repository state, including when lock validation reports stale or malformed lock content.
  - [x] Keep canonical report authority downstream of manifest plus resolver ownership rather than ad hoc workspace inspection.
- [x] Add a runtime-owned repository report service that consumes the compiler publication seam without redefining package meaning. (AC: 1, 2)
  - [x] Expose repository/package report access through a runtime service or workspace-facing seam consistent with existing runtime service patterns.
  - [x] Keep the runtime service a consumer of compiler-owned publication output rather than a second package-resolution implementation.
  - [x] Avoid turning Story `2.4` into the later `RepositoryGraphSession` or IDE protocol story.
- [x] Keep the publication surface inspectable enough for downstream IDE and M6 work while staying milestone-scoped. (AC: 1, 2)
  - [x] Make the published result easy to inspect in tests and future adapters.
  - [x] Do not implement semantic diff, review, publish workflow, or IDE transport payloads here.
- [x] Cover deterministic valid and invalid publication paths with focused tests and sequential verification. (AC: 1, 2)
  - [x] Prove report publication includes canonical lock and resolved graph state from the same repository authority chain.
  - [x] Prove stale or malformed lock states still publish explicit diagnostics instead of hiding package problems.
  - [x] Run Java 25 Gradle verification sequentially, including relevant module tests and a wider regression command.

## Dev Notes

### Story Intent

- Story `2.1` created deterministic resolution inputs.
- Story `2.2` created the canonical local-first package graph.
- Story `2.3` created and validated the real `athena.lock` contract.
- Story `2.4` now publishes the canonical repository/package report through compiler and runtime JVM seams so later runtime-session and IDE stories can consume one stable authority output.

### Architecture Guardrails

- Align to AD-13 by keeping `RepositoryGraphReport` and repository/package nouns in `kernel/repository-model`.
- Align to AD-15 and AD-16 by publishing report state from the explicit compiler-owned manifest → graph → lock sequence.
- Preserve AD-17 by keeping this story short of a full `RepositoryGraphSession`; that belongs to Epic 3.
- Preserve AD-18 by keeping IDE transport or frontend projection work out of this story.

### Technical Requirements

- Reuse the existing compiler seams:
  - `AthenaCompiler.resolveRepositoryGraph(repositoryRoot)`
  - `AthenaCompiler.materializeRepositoryLock(repositoryRoot)`
  - `AthenaCompiler.validateRepositoryLock(repositoryRoot)`
- Existing compiler result types already expose report-friendly state:
  - validated repository contract
  - deterministic resolution input
  - canonical `ResolvedPackageGraph`
  - canonical `RepositoryLock`
  - aggregated diagnostics
- Story `2.4` should add:
  - one compiler publication result for canonical repository/package reporting
  - one runtime-owned repository report service that consumes that compiler publication result
- Prefer exposing report access by repository root or workspace root rather than smuggling repository semantics through source-file-only project state.
- Do not introduce:
  - Node-side report model duplication
  - LSP protocol payload publication
  - runtime session upgrade semantics
  - semantic SCM flows
  - publish/review workflow concepts

### Architecture Compliance

- Prevent these failure modes:
  - runtime re-derives repository/package meaning independently from the compiler
  - report publication hides stale or malformed lock diagnostics
  - a second repository/package report shape appears in runtime or IDE code
  - Story `2.4` silently becomes the `RepositoryGraphSession` or IDE protocol milestone
  - published outputs stop being deterministic or inspectable

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- No new dependencies are justified.

### File Structure Requirements

- Expected update files:
  - `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- Likely new files:
  - one or more compiler-side repository report publication model/service files under `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/`
  - one or more runtime repository-report service files under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/`
  - matching tests under `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/` and `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`
- Files whose current behavior must be preserved:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphService.kt`
    - remains runtime-owned consumption of canonical compiler state; new report service should follow this pattern
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
    - should stay untouched unless unavoidable, because actual IDE protocol exposure belongs to Epic 3

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :kernel:runtime:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - published report includes canonical lock, graph, and diagnostics
  - runtime consumes compiler publication output without redefining package meaning
  - stale or malformed lock state still yields explicit report diagnostics
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- `RepositoryGraphReport` already exists in `kernel/repository-model`.
- `AthenaRepositoryGraphResolutionResult` and lock materialization/validation results already expose `report` properties, but no single canonical publication seam exists yet.
- `AthenaServiceRegistry` already hosts runtime services such as engineering graph, command runtime, and AI proposal runtime.
- `AthenaExecutionContext` already exposes runtime-owned services for the active project and currently exposes engineering-graph projection only.

### Previous Story Intelligence

- Story `2.3` proved canonical lock state can now be materialized and validated, so Story `2.4` should aggregate and publish that state rather than re-solve anything in runtime.
- Story `2.2` and `2.3` already established inspectable result models with `report` properties; the publication seam should build on those patterns instead of replacing them.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical structure and docs must match the architecture
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent M5 work hardened repository/package capabilities in a clear sequence: contract, input, graph, then lock.
- Practical implication:
  - Story `2.4` should be another narrow publication boundary step, not an excuse to start Epic 3 early.

### Latest Technical Information

- No web research is required for Story `2.4`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `2.4` should leave clean room for:
  - Epic 3 `RepositoryGraphSession`
  - Epic 3 LSP package-state exposure
  - later M6 semantic SCM flows

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-24-publish-package-diagnostics-and-graph-reports-for-runtime-ide-and-m6-foundations]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/2-2-resolve-local-first-dependencies-into-a-canonical-package-graph.md]
- [Source: _bmad-output/implementation-artifacts/m5/2-3-materialize-and-validate-athena-lock-from-resolver-output.md]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryGraphResolutionModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializationModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, addendum, and architecture spine review
- Story `2.2` and `2.3` completion notes and guardrails review
- CodeGraph exploration of runtime engineering-graph and compiler/runtime repository-report seams
- runtime service registry, execution context, and workspace seam review

### Completion Notes List

- Added `AthenaRepositoryReportPublicationResult` and `AthenaRepositoryReportLockState` as the compiler-owned publication surface for canonical repository report output.
- Added `AthenaRepositoryReportPublisher` and `AthenaCompiler.publishRepositoryGraphReport(repositoryRoot)` so downstream consumers read one compiler-owned authority seam.
- Added `AthenaRepositoryReportService` plus runtime registry/workspace accessors to consume the compiler publication seam without introducing `RepositoryGraphSession` early.
- Covered deterministic current and stale lock publication paths in compiler/runtime tests.
- Verified sequentially with `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :kernel:runtime:test"` and `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

### File List

- _bmad-output/implementation-artifacts/m5/2-4-publish-package-diagnostics-and-graph-reports-for-runtime-ide-and-m6-foundations.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublicationModel.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublisher.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublisherTest.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRepositoryReportService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRepositoryReportServiceTest.kt

## Change Log

- 2026-07-09: Created Story `2.4` context and moved it to in-progress.
- 2026-07-09: Implemented compiler-owned repository report publication, added runtime repository report service/workspace accessors, and moved the story to review after sequential Java 25 verification.
