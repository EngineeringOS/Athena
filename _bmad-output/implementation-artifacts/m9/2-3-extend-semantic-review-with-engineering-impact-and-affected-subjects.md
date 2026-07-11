---
baseline_commit: e5f5ef7fb0fbf10f583f0cf9acef52eb6a0e914d
---

# Story 2.3: Extend Semantic Review With Engineering Impact And Affected Subjects

Status: done

## Story

As a reviewer,
I want Athena review output to distinguish direct edits from affected engineering subjects,
so that semantic review expresses engineering consequence rather than only raw delta.

## FR Traceability

- FR-5: compute affected engineering subjects for governed changes
- FR-6: route engineering impact into the existing semantic review path
- FR-7: surface the first knowledge-runtime proof through existing semantic delivery surfaces
- NFR-1: keep knowledge delivery upstream of renderer, IDE widgets, and vendor adapters
- NFR-3: impact consequences remain inspectable in review, commit, and SCM transport contracts

## Acceptance Criteria

1. Given an accepted change in the first M9 proof slice, when Athena computes semantic review output, then existing M6 and M8 review vocabulary is extended with engineering impact facts without creating a second review subsystem, and review output can distinguish directly edited subjects from downstream affected subjects.
2. Given engineering consequence is inspected from review or SCM context, when the output is compared with runtime and diagnostics surfaces, then the same canonical change yields compatible consequence language across those paths, and `kernel/semantic-scm` remains the downstream review and history authority.

## Tasks / Subtasks

- [x] Extend semantic SCM contracts with neutral engineering-knowledge and engineering-impact state. (AC: 1, 2)
  - [x] Add `EngineeringKnowledgeState` to `:kernel:engineering-model`.
  - [x] Extend `SemanticBaselineSnapshot`, `SemanticDiff`, `SemanticReviewSummary`, and `SemanticCommitIntent` with additive knowledge and impact fields.
- [x] Carry engineering knowledge through baseline and runtime comparison seams. (AC: 1, 2)
  - [x] Populate baseline snapshots with knowledge state and knowledge diagnostics in `integrations/scm-git` and runtime-owned current snapshots.
  - [x] Compute typed engineering impact consequences in both repository baseline comparison and accepted-mutation review flows.
- [x] Extend review and commit vocabulary instead of creating a second review model. (AC: 1)
  - [x] Add typed `ENGINEERING_IMPACT` review and commit entry kinds plus traceable fact-reference support.
  - [x] Keep direct authored engineering change entries distinct from downstream impact entries.
- [x] Project the enriched review state through existing LSP seams. (AC: 1, 2)
  - [x] Extend semantic SCM and accepted-mutation payloads with typed engineering-impact lists and counts.
  - [x] Keep semantic inspection and Problems unchanged as the upstream authority from Story 2.2.
- [x] Add regression tests and update affected docs. (AC: 1, 2)
  - [x] Add engineering-model, semantic-scm, runtime, and LSP tests for M9 review impact behavior.
  - [x] Update `engineering-model`, `semantic-scm`, `runtime`, and `ide/lsp` README files in English and Chinese.

## Dev Notes

### Story Intent

- Story `2.3` keeps semantic review downstream of canonical knowledge runtime: no frontend-owned impact reconstruction, no renderer-owned authority, and no third review subsystem.
- Direct edits remain `ENGINEERING_CHANGE`.
- Downstream affected subjects now surface as explicit `ENGINEERING_IMPACT` entries plus typed engineering-impact consequence lists.

### Completion Notes

- Added neutral `EngineeringKnowledgeState` so baseline snapshots can carry derived context, capability facts, and constraint evaluations without importing compiler result types into semantic-SCM contracts.
- Extended `AthenaSemanticDiffService` and `AthenaSemanticMutationReviewService` so both repository-baseline review and accepted-mutation review publish the same typed engineering-impact consequences.
- Extended semantic SCM review and commit generators with explicit engineering-impact entry kinds while preserving authored-change versus downstream-consequence separation.
- Extended LSP semantic SCM and accepted-mutation payloads with engineering-impact consequence lists and counts.
- Fixed `kernel/engineering-model/README.zh-CN.md` encoding while updating M9 documentation.

## Testing

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSemanticReviewServiceTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSemanticScmStateRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:semantic-scm:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"` attempted, but the local tool invocation timed out before completion, so only the targeted suites above are confirmed in this story.

## File List

- `_bmad-output/implementation-artifacts/m9/2-3-extend-semantic-review-with-engineering-impact-and-affected-subjects.md`
- `ide/lsp/README.md`
- `ide/lsp/README.zh-CN.md`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMutationReviewProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt`
- `integrations/scm-git/src/main/kotlin/com/engineeringood/athena/integrations/scm/git/GitSemanticBaselineAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringImpactConsequenceCalculator.kt`
- `kernel/engineering-model/README.md`
- `kernel/engineering-model/README.zh-CN.md`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringKnowledgeStateModel.kt`
- `kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringKnowledgeStateContractTest.kt`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMutationReviewService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewServiceTest.kt`
- `kernel/semantic-scm/README.md`
- `kernel/semantic-scm/README.zh-CN.md`
- `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGenerator.kt`
- `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGenerator.kt`
- `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticCommitIntentGeneratorTest.kt`
- `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticReviewSummaryGeneratorTest.kt`
