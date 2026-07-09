---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.1: Generate Semantic Review Summaries From Typed Change Records

Status: review

## Story

As a reviewer,
I want Athena to summarize semantic change in repository, package, and engineering language,
so that I can understand affected packages, contracts, dependencies, and validation outcomes without reverse-engineering raw file edits.

## FR Traceability

- FR-8: produce semantic review summaries over current repository change
- FR-5: surface validation and contract consequences of semantic change
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-2: user-facing review nouns remain vendor-neutral
- NFR-3: the same baseline plus repository state yields the same review summary output
- NFR-4: review output remains typed and inspectable

## Acceptance Criteria

1. Given typed semantic diff and consequence records exist for the current repository state, when Athena generates a semantic review summary, then the summary explains affected packages, changed contracts, dependency movement, and validation impact using VCS-neutral semantic language, and the same baseline and repository state yield the same summary content.
2. Given authored and derived changes both appear in one comparison, when the review summary is inspected, then authored intent is distinguished from derived consequences such as `athena.lock` churn or validation fallout, and each summary item can be traced back to stable core semantic facts.

## Tasks / Subtasks

- [x] Tighten the review-summary contract in `:kernel:semantic-scm`. (AC: 1, 2)
  - [x] Publish any additional typed review-summary entry/section vocabulary needed so review output is more explicit than a raw diff mirror.
  - [x] Keep review-summary nouns VCS-neutral and traceable back to authored semantic changes or derived consequences.
  - [x] Keep all public/core Kotlin review-summary types under `com.engineeringood.athena.scm` with clean KDoc.
- [x] Implement deterministic review-summary generation in the semantic SCM kernel. (AC: 1, 2)
  - [x] Add a kernel-owned review-summary service/factory that derives review output from one `SemanticDiff`.
  - [x] Summarize affected packages, changed repository contracts, dependency movement, engineering changes, validation impact, and degraded-input warnings using stable semantic language.
  - [x] Preserve explicit authored-versus-derived separation and stable item ordering for the same diff input.
  - [x] Ensure each review item remains inspectably linked to stable core facts such as `SemanticChangeRecord`, `SemanticDerivedConsequence`, or `SemanticDiagnostic`.
- [x] Wire review-summary generation through runtime-owned services. (AC: 1, 2)
  - [x] Add a runtime-owned semantic review service or facade that consumes the existing runtime semantic diff path instead of rebuilding comparison logic.
  - [x] Keep `RepositoryGraphSession` and the active execution context as the current-state authority for baseline-driven review generation.
  - [x] Ensure runtime can produce the same review summary for the same baseline plus repository state across repeated calls.
- [x] Verify deterministic review behavior with focused tests. (AC: 1, 2)
  - [x] Add kernel tests proving deterministic review-summary content and authored-versus-derived separation.
  - [x] Add runtime tests proving the review path reuses the existing semantic diff/consequence layer and yields stable summary output.
  - [x] Keep Gradle verification sequential on Windows with Java 25.
- [x] Update live M6 docs for review-summary ownership. (AC: 1, 2)
  - [x] Update `:kernel:semantic-scm` docs so the module clearly owns review-summary generation above semantic diff.
  - [x] Update root/workspace/runtime docs so the current M6 state reflects JVM-owned semantic review generation.

## Dev Notes

### Story Intent

- Story 2.1 is the first Epic 2 layer above the completed semantic diff and consequence foundation from Stories 1.1 through 1.4.
- This story should not jump ahead into commit intent, plugin enrichment, or LSP/Theia transport; it should freeze the kernel and runtime review-summary meaning first.
- Review output should be more useful than replaying raw diff records, but it must still stay traceable back to those typed facts.

### Architecture Guardrails

- Align to AD-19 by keeping semantic review summary logic in the dedicated VCS-neutral semantic SCM core above `:kernel:repository-model`.
- Align to AD-21 by deriving review output from the same governed JVM semantic diff/consequence path used for baseline comparison.
- Align to AD-23 by avoiding Theia, LSP, or frontend-owned review models in this story.
- Align to AD-24 by keeping authored intent and derived consequences explicitly separated in the published review contract.
- Align to AD-25 by keeping core review output generic and stable; plugin-specific enrichment remains a later additive story.

### Technical Requirements

- Prefer evolving `SemanticReviewSummary` into a richer inspectable contract instead of adding an unrelated review-only data model elsewhere.
- Reuse and reference existing canonical types where possible:
  - `SemanticDiff`
  - `SemanticChangeRecord`
  - `SemanticDerivedConsequence`
  - `SemanticDiagnostic`
  - `SemanticBaselineDescriptor`
  - `PackageIdentifier`
- If a richer review entry model is introduced, it must preserve traceability to stable core semantic facts without embedding provider-native SCM terms.
- Runtime should reuse the existing semantic diff service rather than reconstructing review inputs from raw files or frontend state.
- All public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - review summary remaining a thin mirror of raw diff state with no typed summary structure
  - authored and derived changes collapsing into one undifferentiated review list
  - runtime rebuilding semantic review from raw repository files instead of reusing the semantic diff/consequence path
  - Theia/LSP/provider nouns leaking into kernel review contracts
  - non-deterministic review item ordering or unstable summary wording for the same semantic diff input

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 2.1.

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

- Story 1.4 already published validation and repository-contract consequences through the runtime semantic path and preserved snapshot diagnostics for degraded comparisons.
- Story 1.3 already established deterministic authored change categorization and the runtime semantic diff facade.
- Epic 1 now leaves Epic 2 with a stable baseline/diff/consequence substrate, so Story 2.1 should focus on summary meaning instead of reopening lower-layer contracts unnecessarily.

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
- [Source: _bmad-output/implementation-artifacts/m6/1-4-publish-validation-and-contract-consequences-through-the-runtime-semantic-path.md]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt]

## Story Completion Status

- Status: review
- Completion note: Typed semantic review summaries are now generated in `:kernel:semantic-scm`, published through runtime-owned services, documented in the live M6 docs, and fully verified under Java 25.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epics, PRD, addendum, and architecture spine review for Story 2.1 review-summary requirements
- Story 1.4 completion review for diff/consequence substrate and degraded-input behavior
- current semantic-scm and runtime contract inspection for review-summary seam planning
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :kernel:runtime:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:repository-model:test :kernel:compiler:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Tightened `SemanticReviewEntryKind` so authored review output now distinguishes repository-contract, package-dependency, and engineering-change entries instead of flattening everything into one generic authored bucket.
- Added `SemanticReviewSummaryGenerator` to sort authored and derived facts deterministically, generate typed review entries, and attach stable fact references back to `SemanticChangeRecord`, `SemanticDerivedConsequence`, and `SemanticDiagnostic`.
- Added `AthenaSemanticReviewService` plus registry wiring so runtime publishes review summaries by reusing the existing baseline/diff path rather than reconstructing review state from raw repository files.
- Added kernel and runtime tests covering deterministic summaries, authored-versus-derived separation, degraded-input warnings, registry reuse, and repeated baseline-driven review generation.
- Updated the root, workspace, semantic-SCM, and runtime READMEs to reflect that M6 now owns semantic review-summary generation on the JVM path.

### File List

- _bmad-output/implementation-artifacts/m6/2-1-generate-semantic-review-summaries-from-typed-change-records.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- README.md
- README.zh-CN.md
- docs/usages/athena-workspace-summary.md
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGenerator.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGeneratorTest.kt
- kernel/runtime/README.md
- kernel/runtime/README.zh-CN.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewService.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewServiceTest.kt

### Change Log

- 2026-07-09: Published deterministic typed semantic review summaries in `:kernel:semantic-scm`, exposed them through runtime-owned services, added focused kernel/runtime tests, and updated live M6 documentation.
