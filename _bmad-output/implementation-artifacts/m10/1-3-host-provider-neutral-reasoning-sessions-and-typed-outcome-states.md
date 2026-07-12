---
baseline_commit: 61fa8d7
---

# Story 1.3: Host Provider-Neutral Reasoning Sessions And Typed Outcome States

Status: done

## Story

As a platform engineer,
I want Athena runtime to own reasoning session orchestration and typed provider outcomes,
so that hosted, local, or mock providers can plug in without redefining proposal lifecycle rules.

## FR Traceability

- FR-2: preserve traceable audit data and cited evidence for each reasoning proposal
- FR-6: keep AI outputs assistive and non-authoritative
- FR-7: preserve explicit accepted, dismissed, unresolved, and unavailable proposal states
- FR-9: keep provider boundary replaceable and downstream of Athena-owned reasoning contracts
- NFR-1: canonical engineering truth remains upstream of provider calls
- NFR-3: proposal audit data remains inspectable
- NFR-4: provider transport remains replaceable

## Acceptance Criteria

1. Given a reasoning request is submitted, when Athena routes it through the provider boundary, then runtime records a provider-neutral session result such as success, unavailable, or failed without changing canonical engineering truth.
2. Given provider integration changes later, when M10 contracts are inspected, then provider transport remains downstream of Athena-owned reasoning session and proposal models.

## Tasks / Subtasks

- [x] Add provider-neutral reasoning session contracts under `:kernel:runtime`. (AC: 1, 2)
  - [x] Publish typed request, provider request, provider outcome, session record, and snapshot contracts with KDoc.
  - [x] Keep provider transport details additive and downstream of Athena-owned request and proposal nouns.
  - [x] Avoid growing one giant Kotlin file; split session contracts into focused files if needed.
- [x] Orchestrate runtime-owned reasoning sessions above deterministic context assembly. (AC: 1, 2)
  - [x] Submit one reasoning request through runtime-owned context assembly and one replaceable provider boundary.
  - [x] Record typed session outcome plus linked reasoning proposal without mutating canonical engineering state.
  - [x] Preserve deterministic session ids and proposal linkage for later inspection.
- [x] Keep provider coupling replaceable and mock-friendly. (AC: 2)
  - [x] Use a provider-neutral interface or equivalent seam that can host local, remote, or mock adapters later.
  - [x] Do not add vendor SDKs, Theia AI packages, or transport payload ownership in this story.
  - [x] Keep LSP and UI work out of scope.
- [x] Verify runtime session behavior with focused Java 25 tests. (AC: 1, 2)
  - [x] Cover success, unavailable, and failed provider outcomes.
  - [x] Prove command history and canonical compilation state stay unchanged.
  - [x] Update runtime README files if the public session surface expands.
  - [x] Run Gradle verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `1.3` proves runtime-owned provider orchestration without bringing in real provider SDKs yet.
- Story `1.3` must stand on Story `1.1` proposal contracts and Story `1.2` deterministic context assembly.
- This story is still JVM runtime only. LSP and Theia transport stay in later stories.

### Architecture Guardrails

- Align to AD-50: runtime owns deterministic reasoning context assembly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-50---Runtime-Owns-Deterministic-Reasoning-Context-Assembly]
- Align to AD-51: AI output is a typed reasoning proposal, never canonical truth. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-51---AI-Output-Is-A-Typed-Reasoning-Proposal-Never-Canonical-Truth]
- Align to AD-52: provider transports stay downstream of Athena-owned contracts. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-52---Theia-AI-And-Provider-Transports-Stay-Downstream-Of-Athena-Contracts]
- Preserve inherited AD-38 and AD-43: review facts and knowledge derivation remain governed by existing semantic services. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- Reuse `AthenaAiReasoningContextRequest`, `AthenaAiReasoningContext`, and Story `1.1` proposal contracts.
- Provider inputs must be Athena-owned typed requests, not raw prompt strings as the only boundary.
- Session outcome recording must remain inspectable and must not mutate command history or canonical engineering state.
- Mock providers should be easy to inject in runtime tests.

### File Structure Requirements

- Expected update files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`
  - `kernel/runtime/README.md`
  - `kernel/runtime/README.zh-CN.md`
- Strong candidate new files:
  - one focused session contract/runtime file
  - one focused provider boundary file
- Files whose current behavior must be preserved:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningContextAssembly.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningContextAssembly.kt)
  - [`kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeServiceTest.kt`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeServiceTest.kt)
- Explicit non-goals:
  - no vendor SDK
  - no Theia AI package wiring
  - no LSP transport
  - no autonomous source or graph mutation

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaAiReasoningContextAssemblyTest --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"`
- Required proof checks:
  - success, unavailable, and failed provider outcomes are recorded as provider-neutral runtime sessions
  - linked reasoning proposals keep typed provider status and evidence context
  - command history remains unchanged
  - canonical compilation state remains unchanged
  - no concurrent Gradle runs on Windows

### References

- [Source: _bmad-output/planning-artifacts/epics-M10-2026-07-12.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m10/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m10/1-1-publish-governed-ai-reasoning-contracts.md]
- [Source: _bmad-output/implementation-artifacts/m10/1-2-derive-deterministic-reasoning-contexts-from-governed-semantic-outputs.md]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M10 story sequencing review over Stories `1.1` and `1.2`
- `codegraph explore "AthenaAiReasoningRuntimeService AthenaAiReasoningProposalRecord AthenaExecutionContext AthenaServiceRegistry runtime session reasoning provider neutral outcome state"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningSessionRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaAiReasoningRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaAiReasoningContextAssemblyTest --tests com.engineeringood.athena.runtime.AthenaRuntimeTest"`

### Completion Notes List

- Added a dedicated runtime-owned provider-neutral reasoning session seam instead of pushing provider orchestration into LSP, Theia, or vendor SDK code.
- Published typed request, provider request, provider outcome, session record, and snapshot contracts with KDoc under `:kernel:runtime`.
- Linked session submission to existing deterministic context assembly and reasoning proposal recording so every provider attempt preserves governed evidence and typed proposal state.
- Preserved canonical engineering truth by keeping command history empty and reusing the cached canonical compilation snapshot across session submission tests.
- Verified success, unavailable, and failed outcomes with focused Java 25 runtime tests and updated runtime module documentation.

### File List

- _bmad-output/implementation-artifacts/m10/1-3-host-provider-neutral-reasoning-sessions-and-typed-outcome-states.md
- _bmad-output/implementation-artifacts/m10/sprint-status.yaml
- _bmad-output/implementation-artifacts/sprint-status.yaml
- kernel/runtime/README.md
- kernel/runtime/README.zh-CN.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningSessionRuntimeService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningSessionRuntimeServiceTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt

### Change Log

- 2026-07-12: Story created and moved directly into implementation.
- 2026-07-12: Implemented Story 1.3 by adding provider-neutral reasoning session orchestration, typed outcome contracts, focused runtime tests, and runtime module documentation updates.
