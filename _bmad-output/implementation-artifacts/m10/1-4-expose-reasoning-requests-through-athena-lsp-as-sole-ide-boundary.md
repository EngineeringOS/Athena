---
baseline_commit: 61fa8d7
---

# Story 1.4: Expose Reasoning Requests Through Athena LSP As Sole IDE Boundary

Status: done

## Story

As a platform engineer,
I want Athena to expose governed reasoning requests and results through `ide/lsp`,
so that IDE surfaces can consume reasoning proposals without importing JVM kernel code directly.

## FR Traceability

- FR-1: build one deterministic AI reasoning context package from governed semantic outputs
- FR-2: preserve traceable audit data and cited evidence for each reasoning proposal
- FR-8: route AI reasoning through the sole IDE semantic boundary
- FR-9: keep provider boundary replaceable and downstream of Athena-owned reasoning contracts
- NFR-1: canonical engineering truth remains upstream of provider calls
- NFR-4: provider transport remains replaceable

## Acceptance Criteria

1. Given one reasoning request is initiated from the workbench, when Athena transports it to the backend, then the request and result flow only through Athena LSP and runtime-owned contracts.
2. Given reasoning transport payloads are reviewed, when architecture boundaries are inspected, then frontend payloads remain transport DTOs rather than the source of proposal truth or evidence assembly.

## Tasks / Subtasks

- [x] Add transport DTOs for the first Athena reasoning request and result seam. (AC: 1, 2)
  - [x] Publish request and response payloads under `ide/lsp` with clean KDoc.
  - [x] Keep transport payloads separate from runtime-owned proposal and session models.
  - [x] Avoid frontend-owned evidence or prompt-body truth.
- [x] Route one reasoning request through Athena LSP into runtime-owned reasoning sessions. (AC: 1, 2)
  - [x] Add one JSON request on `AthenaLanguageServer`.
  - [x] Convert transport params into runtime-owned reasoning-session requests.
  - [x] Return transport payloads mapped from runtime-owned session and proposal outputs only.
- [x] Allow SCM-backed review context to join the request without leaking review truth into frontend DTOs. (AC: 1, 2)
  - [x] Reuse existing baseline-driven semantic SCM seams when review summary context is needed.
  - [x] Keep review-summary resolution JVM-owned.
  - [x] Do not make frontend send raw review entries or impact facts.
- [x] Verify the LSP seam with focused tests and docs. (AC: 1, 2)
  - [x] Cover at least one transport path with provider-unavailable or provider-success result.
  - [x] Prove runtime-owned evidence reaches the provider boundary and the transport response is DTO-only.
  - [x] Update `ide/lsp` README files if public request surfaces expand.
  - [x] Run Gradle verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `1.4` is the first IDE transport proof for governed AI reasoning.
- Runtime continues to own deterministic context assembly, proposal recording, and provider-neutral session orchestration.
- Frontend payloads remain transport-only DTOs; they are not semantic truth.

### Architecture Guardrails

- Align to AD-50: runtime owns deterministic reasoning context assembly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-50---Runtime-Owns-Deterministic-Reasoning-Context-Assembly]
- Align to AD-51: AI output is a typed reasoning proposal, never canonical truth. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-51---AI-Output-Is-A-Typed-Reasoning-Proposal-Never-Canonical-Truth]
- Align to AD-52: provider transport stays downstream and Theia must consume LSP instead of kernel code directly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#AD-52---Theia-AI-And-Provider-Transports-Stay-Downstream-Of-Athena-Contracts]
- Preserve inherited AD-49: existing semantic delivery surfaces remain the product path for new downstream meaning. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### File Structure Requirements

- Expected update files:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/lsp/README.md`
  - `ide/lsp/README.zh-CN.md`
- Files whose behavior must stay stable:
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt)
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningSessionRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningSessionRuntimeService.kt)
- Explicit non-goals:
  - no Theia AI UI wiring
  - no vendor SDK integration
  - no frontend-owned prompt templates
  - no direct kernel import from frontend

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAiReasoningRequestTest"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningSessionRuntimeServiceTest"`

### References

- [Source: _bmad-output/planning-artifacts/epics-M10-2026-07-12.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m10/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m10/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m10/1-3-host-provider-neutral-reasoning-sessions-and-typed-outcome-states.md]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M10 Story `1.3` review and existing semantic SCM / projection LSP seams review
- `codegraph explore "AthenaLanguageServer AthenaLspSessionHost semantic review protocol projection protocol semantic scm protocol AI reasoning LSP"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAiReasoningRequestTest :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningSessionRuntimeServiceTest"` (first run failed on private test helpers reused across files)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAiReasoningRequestTest :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiReasoningSessionRuntimeServiceTest"` (second run passed after local test fixtures were inlined)

### Completion Notes List

- Added a dedicated `ide/lsp` reasoning protocol with transport DTOs for requests, session payloads, proposal payloads, and cited evidence payloads.
- Routed `athena/aiReasoning` through `AthenaLanguageServer` into the runtime-owned reasoning session service using an injected provider adapter that stays downstream of Athena contracts.
- Reused the existing semantic SCM baseline request seam so impact-summary requests can resolve governed review context inside the JVM path instead of importing review facts from frontend code.
- Verified one unavailable path and one SCM-backed success path, including proof that runtime-owned evidence reaches the provider boundary while the returned LSP payload remains DTO-only.
- Updated `ide/lsp` README files to document the new narrow reasoning transport boundary.

### File List

- _bmad-output/implementation-artifacts/m10/1-4-expose-reasoning-requests-through-athena-lsp-as-sole-ide-boundary.md
- _bmad-output/implementation-artifacts/m10/sprint-status.yaml
- _bmad-output/implementation-artifacts/sprint-status.yaml
- ide/lsp/README.md
- ide/lsp/README.zh-CN.md
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAiReasoningProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAiReasoningRequestTest.kt

### Change Log

- 2026-07-12: Story created and moved directly into implementation.
- 2026-07-12: Implemented Story 1.4 by adding a narrow Athena LSP reasoning request seam, SCM-backed review-context resolution, focused LSP/runtime tests, and LSP module documentation updates.
