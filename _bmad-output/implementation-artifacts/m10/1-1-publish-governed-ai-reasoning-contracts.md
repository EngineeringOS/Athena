---
baseline_commit: 61fa8d7
---

# Story 1.1: Publish Governed AI Reasoning Contracts

Status: done

## Story

As a platform engineer,
I want Athena to publish typed reasoning request, evidence, proposal, and decision-state contracts,
so that AI assistance can sit above governed semantic outputs without hiding inside provider-specific payloads.

## FR Traceability

- FR-1: build one deterministic AI reasoning context package from governed semantic outputs
- FR-2: preserve traceable audit data and cited evidence for each reasoning proposal
- FR-6: keep AI outputs assistive and non-authoritative
- FR-7: preserve explicit accepted, dismissed, unresolved, and unavailable proposal states
- FR-9: keep provider boundary replaceable and downstream of Athena-owned reasoning contracts
- NFR-1: canonical engineering truth remains upstream of provider calls
- NFR-2: reasoning inputs stay deterministic for the same semantic state
- NFR-3: proposal audit data remains inspectable
- NFR-4: provider transport remains replaceable

## Acceptance Criteria

1. Given M9 knowledge-runtime outputs already exist, when M10 defines its first AI boundary, then Athena publishes typed reasoning nouns for request category, reasoning context, cited evidence, proposal category, provider result status, and proposal decision state, and those contracts remain Athena-owned rather than vendor-owned.
2. Given one reasoning proposal is inspected later, when Athena reads the recorded contract state, then it can identify what governed evidence was used, what response category was produced, and whether the proposal is accepted, dismissed, unresolved, or unavailable.

## Tasks / Subtasks

- [x] Publish the first Athena-owned reasoning contract vocabulary in runtime-owned Kotlin types. (AC: 1, 2)
  - [x] Add additive typed contracts under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/` for at least:
    - reasoning request category
    - reasoning context package
    - reasoning evidence reference
    - reasoning proposal category
    - provider result status
    - proposal decision state
  - [x] Keep public/core Kotlin classes documented with clean KDoc.
  - [x] Do not encode first M10 contracts as provider-specific request bodies or frontend-only TypeScript types.
- [x] Separate governed reasoning proposals from existing command-only AI proposal groundwork. (AC: 1, 2)
  - [x] Assess current `AthenaAiProposalRuntimeService` and keep its command-acceptance semantics intact unless an additive refactor is required.
  - [x] Introduce a broader proposal model that can represent explanation, impact-summary, and next-check outputs without implying executable mutation.
  - [x] Preserve explicit distinction between:
    - command proposals
    - reasoning proposals
    - provider session outcomes
- [x] Freeze the first explicit proposal and provider outcome states. (AC: 2)
  - [x] Publish proposal decision states for:
    - accepted
    - dismissed
    - unresolved
    - unavailable
  - [x] Publish provider or session outcome states that at minimum distinguish:
    - success
    - unavailable
    - failed
  - [x] Keep outcome states additive and transport-neutral.
- [x] Preserve evidence anchoring back to governed semantic truth. (AC: 1, 2)
  - [x] Ensure cited evidence references can point back to canonical semantic ids, diagnostics, impact facts, or review facts.
  - [x] Do not let proposal contracts store opaque prompt text as the only audit record.
  - [x] Make it impossible for frontend-only widget state to be treated as authoritative evidence.
- [x] Verify the contract layer with focused tests and module documentation. (AC: 1, 2)
  - [x] Add or extend focused tests under `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`.
  - [x] Cover:
    - typed contract creation
    - stable proposal state vocabulary
    - evidence anchoring
    - additive compatibility with current AI command proposal groundwork
  - [x] Update affected README files if public runtime contract surfaces change.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is contract-first. No provider call, Theia UI, or real explanation generation is required yet.
- M10 succeeds only if later stories can stand on one clean chain:
  - M9 knowledge outputs
  - typed reasoning context contract
  - typed reasoning proposal contract
  - provider-neutral session boundary
  - LSP transport
  - additive workbench surface
- This story should narrow and harden nouns. Do not widen into prompt engineering experiments or UI chrome.

### Architecture Guardrails

- Align to AD-50: runtime owns deterministic reasoning context assembly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-50---Runtime-Owns-Deterministic-Reasoning-Context-Assembly]
- Align to AD-51: AI output is a typed reasoning proposal, never canonical truth. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-51---AI-Output-Is-A-Typed-Reasoning-Proposal-Never-Canonical-Truth]
- Preserve inherited AD-34, AD-38, and AD-43: one mutation authority, one review fact path, and one knowledge-runtime authority remain binding. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Preserve AD-52: provider transports and Theia AI are downstream, not the owner of these contracts. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-52---Theia-AI-And-Provider-Transports-Stay-Downstream-Of-Athena-Contracts]

### Technical Requirements

- Existing M9 substrate already exists for later context assembly:
  - derived engineering context
  - capability facts
  - constraint evaluations
  - knowledge diagnostics
  - engineering impact consequences
  - semantic review facts
- Existing optional AI groundwork already exists in `kernel/runtime`:
  - `AthenaAiCommandProposalDraft`
  - `AthenaAiCommandProposal`
  - `AthenaAiProposalQueueSnapshot`
  - `AthenaAiProposalRuntimeService`
- Story `1.1` should use that groundwork as input context, not as the final M10 vocabulary.
- Keep the contract layer JVM-side first. LSP and Theia transport belong to later stories unless a tiny compatibility shim is strictly necessary for tests.

### Architecture Compliance

- Prevent these failure modes:
  - defining M10 nouns only in Theia TypeScript payloads
  - storing only opaque prompt strings with no typed evidence references
  - collapsing reasoning proposals into executable command proposals
  - binding proposal states to one provider SDK enum
  - letting provider failure text become the only machine-readable status
- Preferred shape:
  - additive runtime-owned contracts
  - clean mapping points for later deterministic context assembly
  - clean separation between reasoning proposal and accepted mutation

### Library / Framework Requirements

- Use repo-approved stack only:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit test style already present in `kernel/runtime`.
- Do not add AI SDKs, JSON libraries, or provider dependencies in Story `1.1`.

### File Structure Requirements

- Expected update files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt`
  - `kernel/runtime/README.md`
  - `kernel/runtime/README.zh-CN.md`
- Strong candidate new files:
  - one or more focused runtime contract files for reasoning nouns instead of growing `AthenaAiProposalRuntimeService.kt` into a mixed-responsibility dump
- Files whose current behavior must be preserved:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt)
    - current command-proposal queue must keep explicit accept or reject semantics
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
    - runtime-owned state stays attached to active project context
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt)
    - service registry remains runtime-owned composition root
- Explicit non-goals:
  - no provider invocation yet
  - no LSP request yet
  - no Theia AI package integration yet
  - no prompt template library
  - no autonomous source or graph mutation

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiProposalRuntimeServiceTest"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"`
- Recommended broader regression after contract refactor:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Required proof checks:
  - reasoning contract vocabulary exists as typed Kotlin surfaces
  - proposal decision states are explicit and stable
  - cited evidence can anchor back to governed semantic ids or facts
  - current command-proposal queue behavior still works
  - no concurrent Gradle build/test execution on Windows

### Current Code State To Preserve

- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt) currently models only pending AI command proposals that become canonical mutation history after explicit acceptance.
- [`kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt) already proves:
  - queued AI command proposals do not mutate canonical state before acceptance
  - accepted proposals route through normal command history with `AI_ACCEPTED`
  - rejected or invalid proposals leave canonical state unchanged
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt) already owns per-project AI proposal state. Story `1.1` should extend this carefully, not leak state ownership to frontend.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt) already exposes the AI proposal runtime service through the shared runtime registry.
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt) currently has no AI reasoning request surface; keep it that way in this story unless tests prove a tiny bridge is unavoidable.

### Previous Milestone Intelligence

- M9 already proved executable engineering knowledge. M10 must stand above that proof, not reopen it.
- M11 is now complete in workspace history, but M10 scope should still stay in its original narrow position:
  - governed AI reasoning
  - no renderer redesign
  - no standards-pack expansion
- Repo conventions still matter directly:
  - milestone artifacts live under `m10/`
  - public/core Kotlin surfaces require KDoc
  - Java `25` and sequential Windows Gradle execution are non-negotiable
  - avoid giant Kotlin files; split contracts into readable focused files if the current AI runtime file would become too large

### Git Intelligence Summary

- Baseline commit:
  - `61fa8d7 feat(m11): complete dense electrical proof`
- Practical implication:
  - M10 starts from stable post-M11 workspace, but should not absorb M11 scope
  - first code touches should stay in runtime contracts
  - product and provider work stays later

### Latest Technical Information

- Local reference confirms the current Theia AI package family available to the workspace baseline is `1.73.1`, including:
  - `@theia/ai-core`
  - `@theia/ai-chat`
  - `@theia/ai-chat-ui`
  - `@theia/ai-ide`
  - `@theia/ai-openai`
  - `@theia/ai-ollama`
- Story `1.1` should not consume those packages yet; this note only prevents later contract naming drift.

### References

- [Source: _bmad-output/planning-artifacts/epics-M10-2026-07-12.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m10/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m10/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/theia-product/package.json]
- [Source: reference/theia-ide/applications/electron/package.json]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Root sprint-status and current runtime AI groundwork review
- `codegraph explore "AthenaAiProposalRuntimeService AthenaSemanticInspectionWidget AthenaLspEditorBridgeService reasoning proposal runtime lsp"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"` (red phase compile failure before implementation)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"` (green targeted verification)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"` (attempted twice; exceeded local timeout window)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiProposalRuntimeServiceTest"` (command-proposal regression verification)

### Completion Notes List

- Published additive M10 reasoning contracts in `:kernel:runtime` for request category, proposal category, evidence kind, provider result status, and proposal decision state.
- Added runtime-owned reasoning proposal lifecycle service that records inspectable proposals separately from executable AI command proposals.
- Preserved current AI command proposal queue and verified it still passes after the M10 contract split.
- Extended `AthenaExecutionContext` and `AthenaServiceRegistry` with a dedicated reasoning runtime seam instead of overloading existing command-proposal state.
- Updated runtime README files in English and Chinese to reflect typed reasoning audit ownership.
- Verified focused targeted runtime tests under Java 25 sequentially on Windows; full `:kernel:runtime:test` was attempted twice but exceeded the local tool timeout window.

### File List

- _bmad-output/implementation-artifacts/m10/1-1-publish-governed-ai-reasoning-contracts.md
- _bmad-output/implementation-artifacts/m10/sprint-status.yaml
- _bmad-output/implementation-artifacts/sprint-status.yaml
- kernel/runtime/README.md
- kernel/runtime/README.zh-CN.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeServiceTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt

### Change Log

- 2026-07-12: Implemented Story 1.1 by publishing additive governed AI reasoning contracts, adding a dedicated runtime reasoning proposal service, updating runtime docs, and verifying targeted Java 25 runtime regressions.
