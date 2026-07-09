---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.2: Produce Publish-Oriented History Summaries From Baseline Sequences

Status: review

## Story

As a release owner,
I want Athena to summarize package evolution and release relevance across semantic baselines,
so that I can judge publish significance from semantic change instead of reconstructing it from raw source-control history.

## FR Traceability

- FR-10: relate semantic change and history to package identity and version meaning
- FR-11: keep publish-oriented semantic history narrow and semantic-first in M6
- FR-12: preserve a later graphical projection path without widening M6 into it
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-3: the same baseline sequence yields the same history summary
- NFR-6: M6 prepares later publish and graphical work without widening into either milestone

## Acceptance Criteria

1. Given one or more semantic baselines and the current repository state are available, when Athena generates a publish-oriented history summary, then it describes release-relevant package evolution, contract break risk, dependency shifts, and validation movement in deterministic, inspectable form, and the same baseline sequence yields the same history summary.
2. Given authored and derived effects both appear across the history window, when the summary is inspected, then package-affecting authored changes are distinguishable from derived state churn, and the result remains useful without becoming a registry, transport, or broad changelog platform.

## Tasks / Subtasks

- [x] Add one deterministic kernel history summarizer over baseline-sequence comparisons. (AC: 1, 2)
  - [x] Reuse the existing semantic diff and package-history contracts rather than introducing a separate history vocabulary.
  - [x] Keep the summarizer package-centered and baseline-sequence-oriented.
  - [x] Keep all new core Kotlin classes under `com.engineeringood.athena.scm` with clean KDoc.
- [x] Publish typed history output for release relevance, contract break risk, dependency shifts, and validation movement. (AC: 1)
  - [x] Keep authored changes separate from derived churn inside the resulting history entries.
  - [x] Keep the output transport-light and free of registry or provider log nouns.
- [x] Add focused tests that prove deterministic history summaries from the same baseline sequence. (AC: 1, 2)
  - [x] Verify release relevance, contract break risk, dependency movement, and validation movement appear in typed inspectable form.
  - [x] Verify authored package-affecting change remains distinguishable from derived churn such as lock refresh or package-graph recomputation.
- [x] Update live semantic-SCM docs for the publish-oriented summary generator. (AC: 2)
  - [x] Refresh the module README type and responsibility lists.
  - [x] Note that M6 history stays useful for publish review without widening into registry or transport workflows.

## Dev Notes

### Story Intent

- Story 3.2 is the first implementation step for Epic 3 history generation.
- This story should summarize baseline-sequence comparisons in the kernel, not expose product surfaces yet.
- Runtime/LSP/IDE exposure remains Story 3.3.

### Architecture Guardrails

- Align to AD-21 by keeping history summary meaning downstream of canonical semantic diff facts.
- Align to AD-24 by preserving authored-versus-derived distinctions in publish-oriented history output.
- Align to AD-26 by keeping package evolution semantic-first, transport-light, and anchored to stable package identity.

### Technical Requirements

- Build on the Story 3.1 history contracts already present in `kernel/semantic-scm`.
- Prefer one deterministic history summarizer helper rather than embedding history logic into runtime or adapters early.
- Do not introduce Git, registry, remote publish, or changelog-platform nouns into public kernel APIs.
- Public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - history meaning being rebuilt from free-form strings instead of typed semantic facts
  - derived churn being presented as primary authored package evolution
  - release relevance and contract break risk being implied only by ad hoc UI text
  - M6 history widening into registry, transport, or broad release automation concerns

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 3.2.

### File Structure Requirements

- Expected new or updated files:
  - `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/semantic-scm/README.md`

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:semantic-scm:test"`
- Keep Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 3.1 froze typed package-aware history contracts and a baseline-sequence request model.
- Stories 1.3 through 2.4 already provide deterministic semantic diff, review, commit, runtime, and IDE seams that history should build on rather than bypass.
- M5 already froze package identity, dependency meaning, and repository contract authority upstream of history.

### Project Structure Notes

- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- Root package remains `com.engineeringood`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/3-1-model-package-aware-semantic-history-over-stable-identities.md]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculator.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGenerator.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGenerator.kt]

## Story Completion Status

- Status: review
- Completion note: `:kernel:semantic-scm` now produces deterministic publish-oriented history summaries from baseline-sequence comparisons, with typed release relevance, contract-break risk, dependency movement, validation movement, and explicit authored-versus-derived separation.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epic, PRD, addendum, and architecture review for publish-oriented semantic history constraints
- CodeGraph plus source inspection over `SemanticDiffCalculator`, `SemanticReviewSummaryGenerator`, `SemanticCommitIntentGenerator`, `SemanticHistoryRequest`, and the current semantic-SCM contract surface
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:semantic-scm:test"`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added `SemanticHistorySummaryGenerator` so Epic 3 now has a deterministic kernel summarizer over typed baseline-sequence comparisons.
- Expanded the history contracts with `SemanticHistoryComparison`, `SemanticValidationMovement`, `SemanticContractBreakRisk`, `SemanticHistoryEntryKind`, and summary-level aggregation fields.
- Kept authored evolution separate from validation movement and derived churn inside the generated history entries.
- Added focused kernel tests proving deterministic history summaries from the same baseline sequence and explicit separation of authored package evolution from lock/package-graph churn.
- Refreshed the semantic-SCM English and Chinese READMEs so the live module docs match the new publish-oriented history generator surface.

### File List

- _bmad-output/implementation-artifacts/m6/3-2-produce-publish-oriented-history-summaries-from-baseline-sequences.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticHistorySummaryGenerator.kt
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticHistorySummaryGeneratorTest.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticScmContractsTest.kt

### Change Log

- 2026-07-09: Added deterministic publish-oriented history summarization over baseline-sequence comparisons, expanded typed history contracts for release relevance and contract-break risk, refreshed module docs, and verified the focused Java 25 kernel gate.
