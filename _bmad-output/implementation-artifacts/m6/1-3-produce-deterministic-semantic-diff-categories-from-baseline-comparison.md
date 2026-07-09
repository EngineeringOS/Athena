---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.3: Produce Deterministic Semantic Diff Categories From Baseline Comparison

Status: review

## Story

As an engineer,
I want Athena to compare current and baseline repository meaning and publish typed semantic change categories,
so that change is understood in repository, package, and engineering terms instead of raw text delta.

## FR Traceability

- FR-3: compare current repository state against a semantic baseline
- FR-4: publish stable semantic change categories
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-2: user-facing semantic diff nouns remain vendor-neutral
- NFR-3: the same baseline plus current repository state yields the same semantic diff output
- NFR-4: semantic diff output remains typed and inspectable

## Acceptance Criteria

1. Given the same repository baseline and current repository state, when Athena runs semantic comparison, then it produces the same typed semantic diff result with stable change categories over repository, package, and engineering meaning, and raw file or line diff is treated as supporting evidence rather than the primary truth.
2. Given the repository change includes manifest, lock, declaration, connection, or dependency movement, when the semantic diff is inspected, then authored changes are distinguished from derived consequences, and the change taxonomy is explicit enough for downstream review, commit, and history workflows.

## Tasks / Subtasks

- [x] Publish the deterministic semantic diff calculator in `:kernel:semantic-scm`. (AC: 1, 2)
  - [x] Add a kernel-owned semantic diff service/calculator that compares a baseline snapshot and a current snapshot without depending on runtime or compiler.
  - [x] Compare repository/package meaning through canonical `RepositoryGraphReport` contracts instead of text delta.
  - [x] Compare engineering meaning through canonical `EngineeringDocument` state when both snapshots carry it.
  - [x] Keep authored semantic changes separate from derived consequences such as canonical lock churn.
- [x] Enrich the baseline snapshot path so engineering-level comparison is available. (AC: 1)
  - [x] Update the first `:integrations:scm-git` adapter seed to materialize engineering semantic state and validation state for the resolved baseline when a governed source file can be compiled.
  - [x] Keep this enrichment inside the adapter/integration boundary; do not move compiler dependency into `:kernel:semantic-scm`.
- [x] Wire deterministic semantic comparison through the runtime-owned repository session. (AC: 1, 2)
  - [x] Add a runtime-owned semantic diff service that builds the current semantic snapshot from the active `RepositoryGraphSession` and active execution context.
  - [x] Reuse the active compilation snapshot when available instead of inventing a parallel semantic interpreter.
  - [x] Add focused runtime tests proving the same baseline plus current state yields the same typed semantic diff result.
- [x] Update live docs for the current M6 workspace story. (AC: 1, 2)
  - [x] Update module/group docs where needed so `:kernel:semantic-scm` now clearly owns baseline comparison and semantic diff categorization.
  - [x] Update the root/workspace summary so early M6 now includes deterministic semantic diff categorization above the baseline seam.
- [x] Verify with focused and regression-safe tests. (AC: 1, 2)
  - [x] Add focused kernel tests for repository/package/engineering semantic diff categories and deterministic ordering.
  - [x] Add or update integration tests proving the baseline adapter now returns compile-derived engineering semantic state when available.
  - [x] Run Java 25 Gradle verification sequentially on Windows.
  - [x] Run adjacent regression tests for touched compiler/runtime/kernel boundaries.

## Dev Notes

### Story Intent

- Story 1.3 proves deterministic typed semantic diff categories.
- Story 1.2 already proved baseline loading; Story 1.3 must build on that seam instead of inventing another comparison path.
- Story 1.4 will deepen consequences and validation fallout, so Story 1.3 should stay focused on stable authored change categories plus the minimal derived lock/package fallout needed to keep authored vs derived explicit.

### Architecture Guardrails

- Align to AD-19 by keeping semantic diff logic in the dedicated VCS-neutral core above `:kernel:repository-model`.
- Align to AD-20 by consuming explicit baseline snapshots plus active repository session state, not command history or frontend state.
- Align to AD-21 by comparing baseline and current state through the same governed JVM repository/package and engineering path used elsewhere.
- Align to AD-22 by keeping vendor mechanics in `integrations/`; diff category logic stays in the kernel semantic layer.
- Align to AD-24 by keeping authored changes distinct from derived consequences such as canonical lock updates.
- Preserve Story 1.2's boundary split: runtime owns the active session, `integrations/scm-git` loads baseline substrate state, and `:kernel:semantic-scm` owns semantic comparison meaning.

### Technical Requirements

- Public/core Kotlin types for the diff layer should remain under `com.engineeringood.athena.scm`.
- Do not add Git nouns, Theia nouns, or frontend state types to the semantic diff contracts.
- Prefer deterministic ordering in diff output. If multiple changes are emitted, their order must be stable across runs for the same inputs.
- Reuse existing canonical types:
  - `RepositoryGraphReport`
  - `PackageIdentifier`
  - `EngineeringDocument`
  - `StableSemanticIdentity`
  - `SemanticValidationResult`
  - `SemanticDiagnostic`
- Raw file diff may be deferred entirely in Story 1.3; it is not the primary truth and does not need a first-class implementation here.
- All public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - diff logic implemented in the Git adapter instead of the semantic kernel
  - current state rebuilt from raw files in runtime without reusing compiler/runtime authority
  - baseline/current comparison depending on command history as the primary baseline
  - lock churn presented as authored repository intent instead of derived consequence
  - non-deterministic change ordering that makes the same inputs produce different diff output

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 1.3.

### File Structure Requirements

- Expected new or updated files:
  - semantic diff implementation/test files under `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/` and `src/test/kotlin/...`
  - updates to `integrations/scm-git/src/main/kotlin/...` and tests so the baseline snapshot can include engineering semantic state
  - runtime semantic diff service/test files under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/` and `src/test/kotlin/...`
  - live doc updates in `README.md`, `docs/usages/athena-workspace-summary.md`, and module READMEs if needed

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:compiler:test"`
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 1.2 already established the baseline-loading seam and the `integrations/` group physically.
- Story 1.2's runtime service proves the active `RepositoryGraphSession` is the correct current-state authority for the comparison path.
- Story 1.2 intentionally left diff generation out so Story 1.3 could build a clean deterministic comparison layer on top.

### Git Intelligence Summary

- Recent milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - keep grouped module structure physical and explicit
  - keep semantic authority in kernel, not in vendor adapters or UI shells

### Latest Technical Information

- No extra web research is required for this story.
- The relevant stack is already frozen locally:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`

### Project Structure Notes

- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- Root package remains `com.engineeringood`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/1-2-resolve-repository-baselines-through-a-vendor-neutral-adapter-boundary.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticBaselineService.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticBaselineResolution.kt]

## Story Completion Status

- Status: review
- Completion note: Deterministic semantic diff categorization now runs through the semantic SCM kernel, baseline snapshots carry compile-derived engineering state, and Java 25 verification is green through focused and full Gradle test sweeps.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epics, PRD, and architecture spine review for deterministic diff requirements
- CodeGraph exploration over current semantic-scm, runtime session, and runtime semantic diff inspection patterns
- current compiler, execution-context, and baseline-resolution boundary review
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test"` (green)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:compiler:test"` (green)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"` (green)

### Completion Notes List

- Added `SemanticDiffCalculator` in `:kernel:semantic-scm` so repository/package meaning and engineering meaning are compared through canonical semantic contracts with deterministic ordering.
- Extended the Git baseline adapter to compile governed Athena sources when present and attach engineering documents plus validation output to the resolved baseline snapshot without moving compiler logic into the semantic kernel.
- Added `AthenaSemanticDiffService` in runtime so the active `RepositoryGraphSession` and compiler-owned execution context produce the current semantic snapshot for baseline comparison.
- Added kernel, integration, and runtime tests proving deterministic diff output, authored-versus-derived categorization, and compile-derived engineering state in baseline resolution.
- Updated root and module docs so the current M6 workspace summary and semantic SCM module description reflect the new deterministic diff layer.

### File List

- _bmad-output/implementation-artifacts/m6/1-3-produce-deterministic-semantic-diff-categories-from-baseline-comparison.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- README.md
- docs/usages/athena-workspace-summary.md
- integrations/scm-git/build.gradle.kts
- integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapter.kt
- integrations/scm-git/src/test/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapterTest.kt
- kernel/runtime/build.gradle.kts
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffService.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffServiceTest.kt
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculator.kt
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculatorTest.kt

### Change Log

- 2026-07-09: Implemented deterministic semantic diff categorization over baseline/current snapshots, enriched baseline resolution with compile-derived engineering state, added runtime diff service and tests, and updated M6 live docs.
