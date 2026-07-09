---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.2: Prepare Deterministic Commit Intent From Semantic Change

Status: review

## Story

As an engineer,
I want Athena to prepare structured commit intent from semantic change,
so that commit preparation reflects semantic work instead of only file staging vocabulary.

## FR Traceability

- FR-6: prepare commit intent from semantic change summaries
- FR-7: keep commit preparation deterministic and inspectable
- FR-5: preserve validation and contract consequences in downstream change outputs
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-2: user-facing commit nouns remain vendor-neutral
- NFR-3: the same semantic change set yields the same commit-intent output
- NFR-4: commit-intent output remains typed and inspectable

## Acceptance Criteria

1. Given semantic diff and consequence records are available, when Athena prepares commit intent, then it emits an inspectable structured result organized around affected packages, changed contracts, dependency movement, and validation consequences, and the same semantic change set yields the same commit-intent output.
2. Given vendor execution remains downstream, when commit preparation is reviewed, then the result is adapter-ready rather than vendor-owned, and it does not require Git-specific core nouns, staging semantics, or UI workflow assumptions in the semantic kernel.

## Tasks / Subtasks

- [x] Tighten the commit-intent contract in `:kernel:semantic-scm`. (AC: 1, 2)
  - [x] Evolve `SemanticCommitIntent` beyond the current thin mirror so it can publish explicit typed commit-intent entries or sections.
  - [x] Keep commit-intent nouns VCS-neutral and traceable back to semantic diff, review, and consequence facts.
  - [x] Keep all public/core Kotlin commit-intent types under `com.engineeringood.athena.scm` with clean KDoc.
- [x] Implement deterministic commit-intent generation in the semantic SCM kernel. (AC: 1, 2)
  - [x] Add a kernel-owned commit-intent service/factory that derives commit intent from reviewed semantic change rather than raw files.
  - [x] Organize commit intent around affected packages, repository-contract movement, dependency movement, engineering change, validation consequences, and degraded-input warnings where applicable.
  - [x] Preserve explicit authored-versus-derived separation and stable ordering for the same semantic review input.
  - [x] Ensure each commit-intent item remains inspectably linked to stable core facts such as `SemanticChangeRecord`, `SemanticDerivedConsequence`, `SemanticReviewEntry`, or `SemanticDiagnostic`.
- [x] Wire commit-intent generation through runtime-owned services. (AC: 1, 2)
  - [x] Add a runtime-owned semantic commit service or facade that consumes the existing runtime baseline/diff/review path instead of rebuilding comparison logic.
  - [x] Keep `RepositoryGraphSession` and the active execution context as the current-state authority for baseline-driven commit preparation.
  - [x] Ensure runtime can produce the same commit intent for the same baseline plus repository state across repeated calls.
- [x] Verify deterministic commit-intent behavior with focused tests. (AC: 1, 2)
  - [x] Add kernel tests proving deterministic commit-intent content and authored-versus-derived separation.
  - [x] Add runtime tests proving the commit path reuses the existing semantic diff/review layer and yields stable commit-intent output.
  - [x] Keep Gradle verification sequential on Windows with Java 25.
- [x] Update live M6 docs for commit-intent ownership. (AC: 1, 2)
  - [x] Update `:kernel:semantic-scm` docs so the module clearly owns commit-intent generation above semantic review.
  - [x] Update root/workspace/runtime docs so the current M6 state reflects JVM-owned semantic commit preparation.

## Dev Notes

### Story Intent

- Story 2.2 is the second Epic 2 layer above the completed baseline, diff, consequence, and review-summary substrate from Stories 1.1 through 2.1.
- This story should freeze commit-intent meaning in the kernel and runtime before plugin enrichment or IDE/LSP transport tries to consume it.
- Commit intent should be more useful than replaying review entries, but it must still stay traceable back to reviewed semantic facts.

### Architecture Guardrails

- Align to AD-19 by keeping semantic commit-intent logic in the dedicated VCS-neutral semantic SCM core above `:kernel:repository-model`.
- Align to AD-21 by deriving commit intent from the same governed JVM semantic comparison path used for baseline comparison, diff, consequence, and review generation.
- Align to AD-22 by keeping vendor execution and staging mechanics in downstream integrations rather than kernel or runtime contracts.
- Align to AD-23 by avoiding Theia, LSP, or frontend-owned commit-preparation models in this story.
- Align to AD-24 by keeping authored intent and derived consequences explicitly separated in the published commit-intent contract.
- Align to AD-25 by keeping core commit-intent output generic and stable; plugin-specific enrichment remains a later additive story.

### Technical Requirements

- Prefer evolving `SemanticCommitIntent` into a richer inspectable contract instead of adding an unrelated commit-only data model elsewhere.
- Reuse and reference existing canonical types where possible:
  - `SemanticDiff`
  - `SemanticReviewSummary`
  - `SemanticReviewEntry`
  - `SemanticChangeRecord`
  - `SemanticDerivedConsequence`
  - `SemanticDiagnostic`
  - `SemanticBaselineDescriptor`
  - `PackageIdentifier`
- If a richer commit-intent entry model is introduced, it must preserve traceability to stable core semantic facts without embedding provider-native SCM terms.
- Runtime should reuse the existing semantic review service and diff service rather than reconstructing commit inputs from raw files or frontend state.
- All public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - commit intent remaining a thin mirror of review or diff state with no typed commit-preparation structure
  - authored and derived changes collapsing into one undifferentiated commit list
  - runtime rebuilding semantic commit intent from raw repository files instead of reusing the existing diff/review path
  - Git, staging, or Theia nouns leaking into kernel commit-intent contracts
  - non-deterministic commit-intent ordering or unstable wording for the same reviewed semantic input

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 2.2.

### File Structure Requirements

- Expected new or updated files:
  - `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/...`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/...`
  - live doc updates in `README.md`, `docs/usages/athena-workspace-summary.md`, `kernel/semantic-scm/README*.md`, and `kernel/runtime/README.md`

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :kernel:runtime:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:repository-model:test :kernel:compiler:test"`
- Completion gate:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 2.1 already published typed semantic review summaries through `:kernel:semantic-scm` and runtime-owned services.
- Story 2.1 established stronger review entry kinds and stable fact references, so Story 2.2 should reuse those review facts instead of inventing a second parallel trace system.
- Story 1.4 already published validation and repository-contract consequences through the runtime semantic path and preserved snapshot diagnostics for degraded comparisons.
- Epic 1 and Story 2.1 now leave Story 2.2 with a stable baseline/diff/consequence/review substrate, so this story should focus on commit-intent meaning instead of reopening lower-layer contracts unnecessarily.

### Git Intelligence Summary

- Recent milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - keep grouped module structure physical and explicit
  - keep semantic authority in kernel/runtime contracts rather than frontend or adapter shells

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
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-1-generate-semantic-review-summaries-from-typed-change-records.md]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGenerator.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt]

## Story Completion Status

- Status: review
- Completion note: Typed semantic commit intent is now generated in `:kernel:semantic-scm`, published through runtime-owned services, documented in the live M6 docs, and fully verified under Java 25.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epics, PRD, addendum, and architecture spine review for Story 2.2 commit-intent requirements
- Story 2.1 completion review for typed review-summary substrate and stable fact reference behavior
- current semantic-scm and runtime contract inspection for commit-intent seam planning
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :kernel:runtime:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:repository-model:test :kernel:compiler:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Added typed commit-intent entry and fact-reference contracts so semantic commit preparation no longer collapses into a thin authored/derived mirror.
- Added `SemanticCommitIntentGenerator` to produce deterministic adapter-ready commit intent from reviewed semantic change while preserving authored-versus-derived separation and traceability.
- Added `AthenaSemanticCommitService` plus runtime registry wiring so commit preparation reuses the existing runtime baseline/diff/review path instead of rebuilding from raw repository files.
- Added kernel and runtime tests covering deterministic commit-intent generation, degraded-input warnings, registry reuse, and repeated baseline-driven commit preparation.
- Updated the root, workspace, semantic-SCM, and runtime READMEs to reflect that M6 now owns semantic commit-intent generation on the JVM path.

### File List

- _bmad-output/implementation-artifacts/m6/2-2-prepare-deterministic-commit-intent-from-semantic-change.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- README.md
- README.zh-CN.md
- docs/usages/athena-workspace-summary.md
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGenerator.kt
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGeneratorTest.kt
- kernel/runtime/README.md
- kernel/runtime/README.zh-CN.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticCommitService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticCommitServiceTest.kt

### Change Log

- 2026-07-09: Published deterministic typed semantic commit intent in `:kernel:semantic-scm`, exposed it through runtime-owned services, added focused kernel/runtime tests, and updated live M6 documentation.
