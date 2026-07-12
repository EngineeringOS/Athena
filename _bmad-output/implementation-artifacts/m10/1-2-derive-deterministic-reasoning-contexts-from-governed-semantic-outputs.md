---
baseline_commit: 61fa8d7
---

# Story 1.2: Derive Deterministic Reasoning Contexts From Governed Semantic Outputs

Status: done

## Story

As a platform engineer,
I want Athena to assemble deterministic reasoning context packages from M9 and M6 outputs,
so that the same engineering state always yields the same AI input package for the same request kind.

## FR Traceability

- FR-1: build one deterministic AI reasoning context package from governed semantic outputs
- FR-2: preserve traceable audit data and cited evidence for each reasoning proposal
- FR-6: keep AI outputs assistive and non-authoritative
- NFR-1: canonical engineering truth remains upstream of any provider call
- NFR-2: same semantic state and request kind yield same reasoning context package
- NFR-3: proposal audit data remains inspectable and tied to governed evidence

## Acceptance Criteria

1. Given the same semantic state and request kind, when Athena assembles a reasoning context package twice, then both packages are structurally identical and preserve deterministic ordering.
2. Given a reasoning request for diagnostic explanation, impact summary, or next-check suggestion, when Athena assembles the context, then it uses canonical subject identities, derived context, capability facts, diagnostics, impact consequences, and review facts where available instead of raw frontend-only state.

## Tasks / Subtasks

- [x] Add a dedicated runtime-owned reasoning-context assembly seam. (AC: 1, 2)
  - [x] Implement the first deterministic context assembly service under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/`.
  - [x] Keep contract nouns additive beside Story `1.1` reasoning proposal types.
  - [x] Avoid growing one giant mixed-responsibility Kotlin file; split files if the AI runtime file becomes hard to read.
- [x] Assemble deterministic context from current compiler-owned and review-owned state. (AC: 1, 2)
  - [x] Build context from `CompilerCompilationSuccess` and existing runtime review seams rather than raw source strings.
  - [x] Include when available:
    - canonical subject identities
    - derived engineering context
    - capability facts
    - constraint evaluations
    - knowledge diagnostics
    - engineering impact consequences
    - semantic review facts
  - [x] Preserve deterministic ordering and duplicate elimination across all included evidence.
- [x] Support first three governed request categories without provider coupling. (AC: 1, 2)
  - [x] Cover:
    - diagnostic explanation
    - impact summary
    - next check
  - [x] Keep request shaping Athena-owned and provider-neutral.
  - [x] Do not add provider invocation, prompt templates, or UI work in this story.
- [x] Reuse existing runtime state instead of inventing frontend-owned inputs. (AC: 2)
  - [x] Prefer active runtime compilation snapshot and semantic review services.
  - [x] Allow review facts to join context only through existing runtime/semantic-review contracts.
  - [x] Do not let editor widget state, graph widget state, or freeform prompt text become required context inputs.
- [x] Verify deterministic assembly with focused tests and docs. (AC: 1, 2)
  - [x] Add runtime tests for repeated assembly equality, evidence ordering, and review-fact inclusion.
  - [x] Keep Story `1.1` reasoning proposal tests green.
  - [x] Update runtime README files if public contract surfaces expand.
  - [x] Run Gradle verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `1.2` turns Story `1.1` nouns into real deterministic context assembly.
- This story is still JVM runtime only. No LSP request, no Theia action, no provider transport yet.
- Output of this story should be reusable by:
  - later provider session orchestration
  - later diagnostic explanation proof
  - later impact summary proof
  - later next-check proof

### Architecture Guardrails

- Align to AD-50: runtime owns deterministic reasoning context assembly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-50---Runtime-Owns-Deterministic-Reasoning-Context-Assembly]
- Preserve AD-51: assembled context supports typed reasoning proposals and must not imply semantic or mutation authority transfer. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-51---AI-Output-Is-A-Typed-Reasoning-Proposal-Never-Canonical-Truth]
- Preserve inherited AD-38 and AD-43: review facts and knowledge derivation stay anchored to existing semantic services and canonical engineering state. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Preserve AD-52: provider transport stays downstream. Do not add SDK or Theia AI coupling here. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-52---Theia-AI-And-Provider-Transports-Stay-Downstream-Of-Athena-Contracts]

### Technical Requirements

- `CompilerCompilationSuccess` already carries:
  - `document`
  - `derivedContext`
  - `capabilityFacts`
  - `constraintEvaluations`
  - `semanticResult`
  - `validationBreakdown.engineeringSufficiencyDiagnostics`
- `AthenaSemanticReviewService` and `SemanticReviewSummaryGenerator` already publish typed review facts and engineering-impact entries.
- `AthenaSemanticScmStateService` already composes baseline diagnostics, review summary, and commit intent above runtime sessions.
- Story `1.2` should assemble context from these stable typed surfaces rather than restating business logic.

### Architecture Compliance

- Prevent these failure modes:
  - assembling context from raw `.athena` text instead of compiled/runtime-owned semantic state
  - using unstable list ordering that changes across repeated runs
  - dropping review facts when they already exist in runtime-owned summaries
  - hiding evidence in prose-only blobs instead of typed evidence refs
  - coupling request categories to one provider prompt format
- Preferred shape:
  - one dedicated assembly seam
  - deterministic normalization helpers
  - additive request-category-specific evidence selection

### Library / Framework Requirements

- Use repo-approved stack only:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit runtime test style.
- Do not add provider SDKs, prompt libraries, or JSON mapping dependencies.

### File Structure Requirements

- Expected update files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt` only if a new runtime seam needs registration
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt` only if a new runtime seam needs registration
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`
  - `kernel/runtime/README.md`
  - `kernel/runtime/README.zh-CN.md`
- Strong candidate new files:
  - dedicated context-assembly file
  - dedicated test file for context assembly
- Files whose current behavior must be preserved:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt)
    - proposal recording and decision states from Story `1.1` must remain stable
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewService.kt)
    - review summary remains source of review facts
  - [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt)
    - existing compiled knowledge outputs remain source substrate
- Explicit non-goals:
  - no provider invocation
  - no LSP method
  - no Theia UI
  - no autonomous apply path

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningRuntimeServiceTest"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiProposalRuntimeServiceTest"`
- Recommended broader runtime regression:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"`
- Required proof checks:
  - repeated assembly with same inputs yields identical context objects
  - request-category-specific evidence stays deterministic and typed
  - review facts enter context only through governed review summary contracts
  - current Story `1.1` proposal lifecycle still passes
  - no concurrent Gradle runs on Windows

### Current Code State To Preserve

- [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/DerivedEngineeringContextDeriver.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/DerivedEngineeringContextDeriver.kt) already produces deterministic derived-context snapshots from canonical `EngineeringDocument`.
- [`kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringKnowledgeStateModel.kt`](../../../kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringKnowledgeStateModel.kt) defines the stable triad of derived context, capability facts, and constraint evaluations.
- [`kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringImpactConsequenceModel.kt`](../../../kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringImpactConsequenceModel.kt) already canonicalizes impact consequences deterministically.
- [`kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt`](../../../kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt) already defines stable review entries and fact references.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt) already composes runtime SCM state from baseline resolution and review generation.

### Previous Story Intelligence

- Story `1.1` already established typed reasoning nouns and dedicated reasoning proposal runtime state.
- Story `1.2` should build on those nouns instead of renaming or moving them again.
- Story `1.1` verification showed full `:kernel:runtime:test` may exceed current local timeout; prefer targeted runtime test commands first, then broader checks as time allows.
- Keep Kotlin files readable. If context-assembly helpers become large, split them into focused files instead of piling them into one class.

### Git Intelligence Summary

- Current baseline commit remains `61fa8d7`.
- M10 code is still uncommitted in working tree, so Story `1.2` must preserve current `1.1` runtime behavior while building forward.

### References

- [Source: _bmad-output/planning-artifacts/epics-M10-2026-07-12.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m10/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m10/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m10/1-1-publish-governed-ai-reasoning-contracts.md]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/DerivedEngineeringContextDeriver.kt]
- [Source: kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringKnowledgeStateModel.kt]
- [Source: kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringImpactConsequenceModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M10 epic / architecture review and Story `1.1` follow-through.
- `codegraph explore "AthenaSemanticMutationReviewService AthenaSemanticScmStateService engineering impact consequence knowledge diagnostics derived context capability facts"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningContextAssemblyTest --tests com.engineeringood.athena.runtime.AthenaAiReasoningRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaAiProposalRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"` (first run failed on missing trigger-subject carryover in impact context assembly)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningContextAssemblyTest --tests com.engineeringood.athena.runtime.AthenaAiReasoningRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaAiProposalRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"` (second run passed after governed impact subject fix)

### Completion Notes List

- Added a dedicated runtime reasoning-context assembly seam so deterministic AI inputs stay runtime-owned instead of leaking into frontend or provider code.
- Extended the AI runtime service with provider-neutral context assembly requests and outputs for diagnostic explanation, impact summary, and next-check flows.
- Mapped governed evidence from compiled derived context, capability facts, constraint evaluations, diagnostics, impact consequences, and semantic review fact references.
- Preserved deterministic ordering, duplicate elimination, and canonical subject identity focus across repeated assembly runs.
- Verified repeated assembly equality and review-fact inclusion while keeping Story `1.1` runtime proposal tests green.

### File List

- _bmad-output/implementation-artifacts/m10/1-2-derive-deterministic-reasoning-contexts-from-governed-semantic-outputs.md
- _bmad-output/implementation-artifacts/m10/sprint-status.yaml
- _bmad-output/implementation-artifacts/sprint-status.yaml
- kernel/runtime/README.md
- kernel/runtime/README.zh-CN.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningContextAssembly.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningContextAssemblyTest.kt

### Change Log

- 2026-07-12: Story created and moved directly into implementation.
- 2026-07-12: Implemented Story 1.2 by adding deterministic runtime reasoning-context assembly over compiled knowledge outputs and governed review facts, plus focused runtime verification.
