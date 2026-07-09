---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.4: Publish Validation And Contract Consequences Through The Runtime Semantic Path

Status: review

## Story

As a reviewer,
I want semantic diff results to include contract and validation consequences through the runtime-owned semantic path,
so that later review and history flows stay compiler-derived instead of frontend-guessed.

## FR Traceability

- FR-5: surface validation and contract consequences of semantic change
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-3: the same baseline plus repository state yields the same semantic diff and consequence output
- NFR-4: consequence output remains typed and inspectable

## Acceptance Criteria

1. Given semantic diff results are available for a repository comparison, when Athena derives consequences, then package dependency movement, repository contract impact, and validation deltas are published through JVM-owned semantic services, and those outputs remain downstream of compiler and runtime authority.
2. Given comparison inputs are incomplete, invalid, or partially resolvable, when consequence analysis runs, then Athena emits deterministic failure or consequence records rather than opaque frontend summaries, and the output remains inspectable for development and architecture debugging.

## Tasks / Subtasks

- [x] Extend the semantic SCM consequence model in `:kernel:semantic-scm`. (AC: 1, 2)
  - [x] Publish any additional typed consequence vocabulary needed for repository contract impact, validation deltas, and deterministic degraded-input reporting.
  - [x] Keep authored semantic change records separate from derived consequence records; do not collapse validation fallout into authored intent.
  - [x] Keep public/core Kotlin consequence types under `com.engineeringood.athena.scm` with clean KDoc.
- [x] Implement deterministic consequence derivation above canonical repository and validation contracts. (AC: 1, 2)
  - [x] Extend the semantic diff calculation path so repository contract impact and validation deltas are derived from canonical `RepositoryGraphReport` and `SemanticValidationResult` inputs instead of UI-owned summaries.
  - [x] Emit deterministic consequence records when one side is incomplete or partially resolvable, including inspectable diagnostic evidence where available.
  - [x] Preserve stable ordering for authored changes, derived consequences, and any attached diagnostics for the same baseline plus current state.
- [x] Wire the enriched consequence path through runtime-owned services. (AC: 1, 2)
  - [x] Keep `RepositoryGraphSession` and `AthenaExecutionContext` as the current-state authority for consequence analysis.
  - [x] Reuse the active compilation snapshot and baseline snapshot data instead of inventing a second semantic interpreter or frontend consequence model.
  - [x] Ensure runtime-facing semantic diff access now returns validation and contract consequences from the JVM-owned path.
- [x] Verify failure and degraded-input behavior with focused tests. (AC: 1, 2)
  - [x] Add kernel tests proving deterministic repository-contract and validation consequence output.
  - [x] Add runtime tests proving incomplete or invalid comparison inputs still yield deterministic inspectable consequence records.
  - [x] Keep Gradle verification sequential on Windows with Java 25.
- [x] Update live M6 docs for consequence publication. (AC: 1, 2)
  - [x] Update `:kernel:semantic-scm` docs so consequence ownership now explicitly covers validation and contract fallout.
  - [x] Update the root/workspace summary and runtime docs so the current M6 story reflects compiler-derived consequence publication.

## Dev Notes

### Story Intent

- Story 1.4 deepens Story 1.3's diff layer by adding the consequence model that later review, commit, and history stories will consume.
- The authored change taxonomy from Story 1.3 stays intact; Story 1.4 adds richer downstream fallout rather than redefining authored intent.
- Consequence publication must stay on the governed JVM path and must not depend on Theia, LSP-local reconstruction, or ad hoc frontend summary logic.

### Architecture Guardrails

- Align to AD-19 by keeping consequence contracts inside the dedicated semantic SCM core above `:kernel:repository-model`.
- Align to AD-20 by treating baseline and current repository snapshots as explicit semantic inputs rather than command journals or frontend-local state.
- Align to AD-21 by deriving repository/package and validation consequences through the same JVM semantic path used for canonical repository/package and engineering meaning.
- Align to AD-22 by keeping vendor substrate behavior in `integrations/`; consequence classification must remain kernel-owned.
- Align to AD-24 by keeping authored change, repository/package contract fallout, lock churn, validation fallout, and degraded comparison state explicitly distinguishable.

### Technical Requirements

- Public/core Kotlin types added for this story must remain under `com.engineeringood.athena.scm`.
- Reuse existing canonical types where possible:
  - `RepositoryGraphReport`
  - `RepositoryDiagnostic`
  - `SemanticValidationResult`
  - `SemanticDiagnostic`
  - `RepositoryGraphSession`
  - `SemanticBaselineSnapshot`
- Prefer extending the existing semantic diff and derived-consequence path over creating a parallel runtime-only report type.
- Incomplete comparison input should remain inspectable through typed consequence records and/or attached diagnostics; do not hide it behind nulls or thrown UI-facing exceptions.
- All public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - validation fallout represented as frontend-only text instead of typed semantic consequence records
  - repository contract impact derived from raw file names or UI state instead of canonical report/validation contracts
  - runtime building a separate consequence interpreter that diverges from compiler/runtime-owned meaning
  - incomplete baseline/current data causing opaque failure instead of deterministic inspectable consequence output
  - non-deterministic ordering of consequence records or diagnostics for the same inputs

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 1.4.

### File Structure Requirements

- Expected new or updated files:
  - `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/...`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/...`
  - `README.md`, `docs/usages/athena-workspace-summary.md`, `kernel/runtime/README.md`, and `kernel/semantic-scm/README*.md` if consequence ownership changes are surfaced there

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :kernel:runtime:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:repository-model:test :kernel:compiler:test"`
- Completion gate:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 1.3 already froze deterministic authored semantic change categories and the first derived lock/package fallout.
- Story 1.3 already enriched baseline snapshots with compile-derived engineering documents and validation output, so Story 1.4 should consume that seam rather than reopening baseline loading.
- Story 1.3 already added the runtime semantic diff service, which is the correct runtime-owned entry point for consequence publication.

### Git Intelligence Summary

- Recent milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - keep grouped module structure physical and explicit
  - keep semantic authority in kernel/runtime contracts, not in adapters or frontend shells

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
- [Source: _bmad-output/implementation-artifacts/m6/1-3-produce-deterministic-semantic-diff-categories-from-baseline-comparison.md]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculator.kt]
- [Source: kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/SemanticValidationModel.kt]

## Story Completion Status

- Status: review
- Completion note: Validation and repository-contract consequences now flow through the runtime-owned semantic comparison path, partial baseline/current inputs remain inspectable through typed snapshot diagnostics and consequence records, and Java 25 verification is green through focused plus full test sweeps.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epics, PRD, addendum, and architecture spine review for Story 1.4 consequence requirements
- Story 1.3 implementation review for baseline-loading and deterministic diff context
- current semantic-scm, repository-model, runtime, and validation contract inspection
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test"` (red: missing consequence types and snapshot diagnostics)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test"` (green)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:repository-model:test :kernel:compiler:test"` (green)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"` (green)

### Completion Notes List

- Extended `SemanticBaselineSnapshot` so baseline/current comparison inputs can carry typed snapshot diagnostics alongside repository reports and validation results.
- Expanded the semantic consequence vocabulary with repository-contract review impact, validation delta, and incomplete-comparison consequence types while preserving authored-versus-derived separation.
- Updated `SemanticDiffCalculator` to derive repository-contract impact, validation deltas, and deterministic degraded-input consequence records from canonical repository, validation, and snapshot-diagnostic inputs.
- Updated the Git baseline adapter and runtime semantic diff service so baseline and current snapshots preserve compile/report diagnostics on the JVM side instead of dropping them before consequence analysis.
- Added kernel, integration, and runtime tests proving deterministic consequence ordering, validation delta publication, and incomplete-input diagnostics through the runtime-owned semantic path.
- Updated root, workspace, semantic-scm, and runtime docs so the live M6 description matches the implemented consequence behavior.

### File List

- _bmad-output/implementation-artifacts/m6/1-4-publish-validation-and-contract-consequences-through-the-runtime-semantic-path.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- README.md
- docs/usages/athena-workspace-summary.md
- integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapter.kt
- integrations/scm-git/src/test/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapterTest.kt
- kernel/runtime/README.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffService.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffServiceTest.kt
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculator.kt
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticDiffCalculatorTest.kt

### Change Log

- 2026-07-09: Published validation and repository-contract consequences through the runtime semantic path, preserved snapshot diagnostics for degraded comparisons, added focused regression coverage, and updated live M6 docs.
