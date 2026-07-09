---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.2: Resolve Repository Baselines Through A Vendor-Neutral Adapter Boundary

Status: review

## Story

As a platform engineer,
I want Athena to load comparison baselines through a vendor-neutral adapter seam,
so that repository change can be compared against stable baseline state without leaking vendor terms into the semantic core.

## FR Traceability

- FR-1: stable semantic SCM boundary above repository/package meaning
- FR-2: keep vendor storage mechanics downstream of Athena semantic meaning
- FR-3: compare current repository state against a semantic baseline
- NFR-1: semantic SCM stays downstream of compiler/runtime semantic authority
- NFR-2: user-facing semantic SCM nouns remain vendor-neutral
- NFR-3: the same baseline request yields deterministic semantic baseline resolution
- NFR-4: baseline failures remain typed and inspectable

## Acceptance Criteria

1. Given an active `RepositoryGraphSession` and a request for semantic comparison, when Athena resolves the comparison baseline, then it uses a vendor-neutral baseline descriptor and adapter handoff rather than frontend-local state or reconstructed command history, and baseline resolution failures are surfaced as deterministic, inspectable semantic diagnostics.
2. Given Git is the first practical storage substrate, when the adapter boundary is reviewed, then vendor-specific reference resolution and execution live in a separate `integrations/` layer rather than the semantic kernel or IDE contracts, and the semantic core remains usable without Git-shaped public nouns.

## Tasks / Subtasks

- [x] Create the Story 1.2 baseline-resolution contract slice in `:kernel:semantic-scm`. (AC: 1, 2)
  - [x] Add typed baseline-resolution request/result contracts that remain VCS-neutral and live under `com.engineeringood.athena.scm`.
  - [x] Add a runtime-independent baseline adapter seam plus a small resolver/orchestrator in `:kernel:semantic-scm`.
  - [x] Reuse `SemanticDiagnostic`, `SemanticRuleId`, and related validation types for deterministic baseline-resolution failures instead of inventing ad-hoc error payloads.
- [x] Seed the first vendor adapter in a separate `integrations/` layer. (AC: 1, 2)
  - [x] Add a physical `:integrations:scm-git` module under `integrations/scm-git/` with its own README set and module marker.
  - [x] Implement the first baseline adapter there, keeping vendor-specific repository loading outside `kernel/`.
  - [x] Keep any vendor specifics inside the integration module; no Git nouns should leak into `:kernel:semantic-scm` public contracts.
- [x] Wire the active runtime repository session into the new baseline-resolution seam. (AC: 1)
  - [x] Add a runtime-owned semantic baseline service that resolves a baseline from an active `RepositoryGraphSession`.
  - [x] Keep `RepositoryGraphSession` runtime-owned and pass only the required state into the semantic resolver.
  - [x] Add focused runtime tests proving Athena does not fall back to frontend state or command history for repository baseline loading.
- [x] Update the live workspace maps and module docs. (AC: 2)
  - [x] Add `integrations/` group documentation plus `:integrations:scm-git` module documentation.
  - [x] Update root and workspace-summary docs so M6 baseline resolution appears in the current topology and milestone narrative.
  - [x] Keep docs explicit that `:kernel:semantic-scm` stays VCS-neutral while `:integrations:scm-git` owns the first substrate adapter.
- [x] Verify the baseline-resolution seam with focused and regression-safe tests. (AC: 1, 2)
  - [x] Add focused kernel, integration, and runtime tests for successful baseline resolution and deterministic failure diagnostics.
  - [x] Run Java 25 Gradle verification sequentially on Windows.
  - [x] Run adjacent regression tests for the touched kernel/runtime boundaries.

## Dev Notes

### Story Intent

- Story 1.2 proves baseline loading, not semantic diff or semantic review generation.
- The baseline authority must come from an explicit adapter-backed repository baseline, not from M1 command history, editor buffers, or frontend state.
- The first practical substrate may be Git, but the kernel semantic surface must still speak Athena semantic nouns only.
- This story should leave Story 1.3 free to compare current and baseline semantic states without reopening ownership.

### Architecture Guardrails

- Align to AD-19 by keeping semantic baseline resolution in the dedicated VCS-neutral core above `:kernel:repository-model`.
- Align to AD-20 by treating baselines as repository-scoped semantic inputs, not reconstructed command journals.
- Align to AD-21 by loading baseline semantic state through the same JVM authority used for repository/package validation.
- Align to AD-22 by putting vendor-specific baseline loading in `integrations/`, not `kernel/`, `runtime/`, or `ide/`.
- Align to AD-23 by keeping Theia SCM and IDE transport completely out of the baseline-resolution contract surface.
- Preserve the inherited M5 rule that `RepositoryGraphSession` remains runtime-owned active state.
- Preserve Story 1.1's rule that `:kernel:semantic-scm` stays dependency-light and free of compiler/runtime cycles.

### Technical Requirements

- Public/core Kotlin types for Story 1.2 should stay under `com.engineeringood.athena.scm`.
- Reuse M5 and M6 contract boundaries rather than duplicating them:
  - `RepositoryGraphSession` stays in runtime.
  - `RepositoryGraphReport` and package identity stay in `:kernel:repository-model`.
  - `SemanticDiagnostic`, `SemanticDiagnosticSeverity`, `SemanticDiagnosticCategory`, and `SemanticRuleId` stay in `:kernel:validation`.
- The neutral baseline-resolution surface may carry opaque adapter-owned locators or references, but it must not expose `GitRepository`, `CommitHash`, `Branch`, `Head`, `Remote`, or Theia types in kernel public contracts.
- The integration adapter may use compiler-owned repository publication authority internally to materialize one baseline snapshot, but runtime/compiler must not depend back on `integrations/`.
- Story 1.2 should not add semantic diff algorithms, review summaries, commit execution, or IDE UI work.
- All new public/core Kotlin classes require clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - baseline loading implemented directly inside `AthenaRuntime` or `AthenaWorkspace`
  - `:kernel:semantic-scm` depending on `:kernel:runtime` or `:kernel:compiler`
  - Git nouns added to the semantic kernel contract surface
  - baseline failures returned as strings or exceptions only, without typed inspectable diagnostics
  - integration code becoming the semantic authority instead of loading substrate state for the semantic core

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 1.2.

### File Structure Requirements

- Expected new files:
  - `integrations/README.md`
  - `integrations/README.zh-CN.md`
  - `integrations/scm-git/build.gradle.kts`
  - `integrations/scm-git/README.md`
  - `integrations/scm-git/README.zh-CN.md`
  - `integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitScmIntegrationModuleMarker.kt`
  - baseline adapter implementation files under `integrations/scm-git/src/main/kotlin/...`
  - focused tests under `integrations/scm-git/src/test/kotlin/...`
  - baseline-resolution contract/service files under `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/`
  - focused tests under `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/`
  - runtime semantic baseline service files/tests under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/` and `src/test/kotlin/...`
- Expected update files:
  - `settings.gradle.kts`
  - `README.md`
  - `docs/usages/athena-workspace-summary.md`
  - `kernel/runtime/build.gradle.kts`
  - `kernel/semantic-scm/build.gradle.kts` only if strictly necessary

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:compiler:test"`
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 1.1 established `:kernel:semantic-scm` as the dedicated M6 core boundary and already proved the workspace rule that this semantic layer stays VCS-neutral.
- Story 1.1 also established the KDoc, module marker, bilingual README, and focused-test pattern that Story 1.2 should continue.
- Story 1.1 explicitly avoided runtime/compiler dependencies from `:kernel:semantic-scm`; Story 1.2 must preserve that.

### Git Intelligence Summary

- Recent milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - add the new `integrations/` group physically
  - keep module boundaries explicit and documented
  - avoid logical-only module declarations without real directories

### Latest Technical Information

- No extra web research is required for this story.
- The relevant stack is already frozen locally:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`

### Project Structure Notes

- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- Root package remains `com.engineeringood`.
- The new integration module should fit the existing grouped workspace shape rather than introducing orphaned modules.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/1-1-publish-canonical-semantic-scm-contracts-in-kernel-semantic-scm.md]
- [Source: settings.gradle.kts]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRepositoryReportService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublicationModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryReportPublisher.kt]

## Story Completion Status

- Status: review
- Completion note: Semantic baseline resolution now routes through a VCS-neutral kernel seam, a runtime-owned baseline service, and the first `integrations/scm-git` adapter seed with focused Java 25 verification complete.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epics, PRD, and architecture spine review for baseline and adapter requirements
- CodeGraph exploration over compiler/runtime/repository publication paths
- current runtime session, repository report, and semantic-scm boundary review
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test"` red phase confirming missing baseline-resolution seam types
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test"` green phase after implementing semantic baseline contracts, integration adapter, and runtime service
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:compiler:test"` adjacent regression verification

### Completion Notes List

- Story 1.2 is the next M6 backlog story after Story 1.1.
- The implementation slice is intentionally narrow: baseline loading only, no semantic diff or review logic yet.
- Vendor substrate logic belongs in `integrations/`, while runtime only passes active session state into the semantic seam.
- Added `SemanticBaselineLocator`, `SemanticBaselineResolutionRequest`, `SemanticBaselineResolutionResult`, `SemanticBaselineAdapter`, and `SemanticBaselineResolver` to `:kernel:semantic-scm`.
- Added `AthenaSemanticBaselineService` so runtime resolves baselines from the active `RepositoryGraphSession` without moving baseline authority into frontend state or command history.
- Seeded `:integrations:scm-git` with a first baseline adapter that reuses compiler-owned repository graph publication while keeping Git-shaped mechanics out of kernel contracts.
- Added focused tests for unsupported-adapter diagnostics, relative baseline-path loading, missing-baseline failures, and runtime session-based resolution.

### File List

- _bmad-output/implementation-artifacts/m6/1-2-resolve-repository-baselines-through-a-vendor-neutral-adapter-boundary.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- README.md
- docs/usages/athena-workspace-summary.md
- settings.gradle.kts
- integrations/README.md
- integrations/README.zh-CN.md
- integrations/scm-git/build.gradle.kts
- integrations/scm-git/README.md
- integrations/scm-git/README.zh-CN.md
- integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitScmIntegrationModuleMarker.kt
- integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapter.kt
- integrations/scm-git/src/test/kotlin/com/engineeringood/athena/integrations/scm/git/GitScmIntegrationModuleMarkerTest.kt
- integrations/scm-git/src/test/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapterTest.kt
- kernel/runtime/build.gradle.kts
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticBaselineService.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticBaselineServiceTest.kt
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticBaselineResolution.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticBaselineResolverTest.kt

### Change Log

- 2026-07-09: created the Story 1.2 context artifact, implemented the VCS-neutral semantic baseline seam, seeded `:integrations:scm-git`, wired runtime baseline resolution, updated module docs, and completed focused plus adjacent regression verification under Java 25.
