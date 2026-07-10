---
baseline_commit: d8e1c6163b7edd8895e8b8fe182519f0fbf54b53
---

# Story 1.1: Publish Unified Mutation Contracts And Categories In Runtime-Owned Command Surfaces

Status: done

## Story

As a platform engineer,  
I want Athena to define explicit mutation contracts and categories,  
so that source and graph change requests converge into one inspectable runtime-owned mutation model.

## FR Traceability

- FR-1: keep one mutation authority for both text and graph interaction
- FR-2: classify meaningful changes explicitly
- FR-3: translate supported graph operations into Athena-owned command intents
- FR-4: refresh graphical state deterministically after an accepted change
- FR-7: define what each projection may display, edit, emit, and own
- FR-8: keep mutation semantics independent from the current renderer stack
- NFR-1: meaningful changes route through one Athena-owned mutation path
- NFR-3: accepted mutation over the same state yields the same resulting canonical and projection state
- NFR-4: command intents, mutation outcomes, and rejection paths remain inspectable
- NFR-6: graph stack must not own command meaning or durable mutation semantics

## Acceptance Criteria

1. Given the completed M1 command runtime and the M8 architecture spine, when M8 begins implementation, then Athena publishes explicit mutation contracts for accepted, rejected, and validation-feedback outcomes, and those contracts remain Athena-owned rather than renderer- or editor-owned.
2. Given interaction types are reviewed, when mutation categories are checked, then Athena classifies supported behavior as semantic mutation, projection mutation, or transient interaction, and the category is carried explicitly through the mutation model instead of being inferred from frontend behavior.

## Tasks / Subtasks

- [x] Publish explicit mutation-category vocabulary in runtime-owned command surfaces. (AC: 1, 2)
  - [x] Add one explicit mutation-category model in [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt) or a closely related runtime-owned file, with at least:
    - semantic mutation
    - projection mutation
    - transient interaction
  - [x] Keep the public vocabulary Athena-owned and renderer-neutral. Do not introduce public categories named after GLSP, Theia, drag, drop, or canvas-library mechanics.
  - [x] Add clean KDoc for every new public/core Kotlin type introduced by this story.
- [x] Extend the runtime-owned mutation result model so accepted, rejected, and validation-feedback outcomes are first-class and inspectable. (AC: 1)
  - [x] Reuse the current result pattern in [`AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt) and [`AthenaCommandHistory.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt) rather than inventing a second result channel.
  - [x] Add any new result types or fields needed so later graph-originated requests can surface validation feedback without becoming renderer-owned semantics.
  - [x] Preserve the current accepted / rejected / unavailable behavior for the existing `CONNECT_PORTS` command path unless this story intentionally generalizes it in a backward-compatible way.
- [x] Freeze the first unified mutation contract shape without widening into graph implementation yet. (AC: 1, 2)
  - [x] Keep this story focused on contract publication and runtime-owned vocabulary only.
  - [x] Do not yet implement graph gesture handling, Theia widget event translation, LSP mutation requests, or renderer callbacks in this story.
  - [x] Do not yet implement projection ownership contracts themselves; that belongs to Story `1.2`.
  - [x] Do not yet normalize source-originated editor flows into the new result path end to end; that belongs to Story `1.3`.
- [x] Update the current protocol and documentation surfaces so the repo tells one M8 mutation story everywhere. (AC: 1, 2)
  - [x] Update [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) only if small additive contract placeholders are required to keep later mutation feedback naming aligned; do not add live mutation execution in Story `1.1`.
  - [x] Update milestone-facing docs if needed so M8 clearly distinguishes semantic mutation, projection mutation, and transient interaction.
  - [x] Keep English wording consistent with the M8 PRD and architecture spine: "mutation authority", "command intent", "semantic mutation", "projection mutation", and "transient interaction".
- [x] Verify the new mutation-contract layer with focused tests and regression-safe checks. (AC: 1, 2)
  - [x] Add focused runtime tests for mutation categories and mutation-result contract behavior under `kernel/runtime/src/test/kotlin/...`.
  - [x] Verify that the existing command runtime and history tests still pass under Java 25 after the contract changes.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

### Review Findings

- [x] [Review][Patch] Public runtime mutation contracts changed non-additively and break existing callers [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt:22]
- [x] [Review][Patch] Hosted plugin command execution still exposes a parallel wrapper result hierarchy instead of the unified mutation contract [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt:219]
- [x] [Review][Patch] AI proposal acceptance still re-wraps command execution into a separate public result hierarchy [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt:45]

## Dev Notes

### Story Intent

- This is the contract-publication entry story for M8, not the graph-editing story.
- The point is to freeze one Athena-owned mutation vocabulary before graph gestures, ownership contracts, source normalization, unified review, or reveal flows begin.
- The safest implementation is to extend the existing runtime-owned command/result model rather than adding a second mutation model beside it.
- Story `1.2` owns explicit projection ownership contracts.
- Story `1.3` owns normalization of source-originated change into the same governed mutation-result path.
- Epic `2.x` owns graph gesture translation plus the first semantic and projection edit paths from the graph workbench.
- Epic `3.x` owns unified review facts, bidirectional reveal, and the M8 proof corpus.

### Architecture Guardrails

- Align to AD-34 by freezing one mutation authority above source and graph in runtime-owned command surfaces. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-34]
- Align to AD-35 by carrying semantic mutation, projection mutation, and transient interaction explicitly in the model instead of leaving category implied by UI behavior. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-35]
- Align to AD-36 by keeping renderer gestures and graph-framework language out of durable mutation contracts. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-36]
- Align to AD-37 by keeping acceptance, rejection, validation feedback, and refresh runtime-owned. Story `1.1` may publish contract shapes for these outcomes, but it must not widen into graph execution plumbing yet. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-37]
- Align to AD-38 and AD-41 by preserving the future requirement that source- and graph-originated changes converge before review and persistence. Story `1.1` only defines the shared mutation-result language that later stories will use. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-38] [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-41]
- Align to AD-42 by keeping the scope narrow and contract-first. Do not widen into broad authoring behavior. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-42]
- Preserve inherited M7 rules:
  - `kernel/projection-model` remains the projection boundary
  - layout/geometry remain view-scoped metadata, not engineering truth
  - `ide/lsp` remains the sole IDE semantic/projection entry point
  - graph adapters remain downstream and translation-only

### Technical Requirements

- The current runtime command layer already exists in [`AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt) with:
  - `AthenaCommandKind`
  - `AthenaCommand`
  - `AthenaCommandExecutionResult`
  - `AthenaCommandExecutionSuccess`
  - `AthenaCommandExecutionRejected`
  - `AthenaCommandExecutionUnavailable`
- The current history layer already exists in [`AthenaCommandHistory.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt) with inspectable mutation-history result shapes.
- Story `1.1` should extend these runtime-owned contracts instead of creating a parallel `mutation-model` module unless there is a very strong boundary reason.
- The first M8 contract layer likely needs:
  - one mutation-category enum or sealed model
  - one mutation-result vocabulary that is future-safe for graph-originated validation feedback
  - one stable place to attach category and feedback semantics to command execution and later reveal/review flows
- Keep the Kotlin package root under `com.engineeringood.athena.runtime` unless a deliberate, documented split is justified.
- Add KDoc for all public/core Kotlin classes touched by this story because the user explicitly requires clean KDoc on core Kotlin surfaces.

### Architecture Compliance

- The story is only successful if future M8 work can point to one clear mutation language:
  - one category model
  - one result model
  - one runtime-owned command authority
- Prevent these failure modes:
  - graph-specific mutation enums under `ide/theia-frontend` or `integrations/graph-glsp`
  - direct references to GLSP/Theia gestures in public runtime-owned mutation contracts
  - source edits and graph edits each getting their own durable result model
  - "validation feedback" being treated as a frontend-only toast concept rather than a runtime-owned result shape
  - Story `1.1` widening into actual graph mutation execution or projection ownership policies

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the current M8 architecture:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse the existing Kotlin/JUnit test conventions already used by the runtime module.
- Do not add third-party dependencies just to express mutation categories or mutation-result contracts.
- Theia and GLSP stay later consumers of these contracts, not dependencies of the runtime mutation vocabulary.

### File Structure Requirements

- Expected update files:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt) if history/result alignment needs additive changes
  - [`kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime)
- Possible additive update files if naming alignment is needed, but only narrowly:
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt)
- Files whose current behavior and ownership must be preserved:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
    - runtime remains the owner of command runtime and projection session authority
  - [`integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`](../../../integrations/graph-glsp/src/athena-glsp-projection-adapter.ts)
    - adapter is translation-only and must not be given durable mutation meaning in this story
  - [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)
    - current pan/zoom/selection/info interactions remain transient UI behavior and must not be reclassified as persisted mutation here
  - [`ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx`](../../../ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx)
    - semantic SCM remains a downstream review/history surface, not a mutation authority

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Recommended focused regression after contract updates:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Required proof checks:
  - runtime command/result vocabulary now carries explicit mutation-category semantics
  - accepted, rejected, and validation-feedback outcomes are inspectable and Athena-owned
  - no frontend, Theia, or GLSP-specific terms leak into the public runtime mutation contracts
  - existing command runtime tests still pass
  - current inspect-first graph workbench behavior remains unchanged because Story `1.1` is contract-only
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt) currently supports only one semantic command kind, `CONNECT_PORTS`, and only semantic execution result types; it has no explicit mutation-category vocabulary yet.
- [`AthenaCommandHistory.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt) currently provides runtime-owned history result types but no explicit category split between semantic and projection mutation.
- [`AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) currently exposes typed projection queries and one governed projection command (`switchActiveView`); it does not yet expose mutation feedback contracts for graph-originated change.
- [`athena-graph-workbench-widget.tsx`](../../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx) currently implements pan, zoom, fit, selection, overlay toggles, and active-view switching. Those interactions are still transient or already governed and must stay that way in Story `1.1`.
- [`athena-glsp-projection-adapter.ts`](../../../integrations/graph-glsp/src/athena-glsp-projection-adapter.ts) currently translates projection payloads into GLSP-shaped graph data only; it has no mutation path and must stay translation-only in this story.

### Previous Milestone Intelligence

- M1 already proved runtime-owned command execution and history. M8 should extend that path, not replace it.
- M6 already proved semantic review/history authority in `kernel/semantic-scm`; Story `1.1` must keep future mutation review consequences aligned to that authority.
- M7 already proved:
  - projection authority through runtime plus `ide/lsp`
  - translation-only graph adaptation in `integrations/graph-glsp`
  - inspect-first graph workbench behavior
- The user has repeatedly enforced these workspace rules that matter directly here:
  - Java `25` is non-negotiable
  - Windows build/test tasks must run sequentially
  - root package is `com.engineeringood`
  - module and artifact structure must stay physically aligned to the architecture

### Git Intelligence Summary

- Recent milestone baseline:
  - `d8e1c61 feat: complete m7 graphical workbench proof`
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
- Practical implication:
  - follow the same contract-first discipline used in M3, M6, and M7
  - keep new M8 nouns in the runtime/kernel authority path
  - do not let the graph stack become the first place that defines mutation semantics

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+` and Eclipse Theia `1.73.1` remain relevant to later graph/workbench stories, but Story `1.1` should not depend on frontend framework behavior.

### Project Structure Notes

- `m8/` is a new active milestone folder and this story is the first ready-for-dev artifact under it.
- The story should reduce future naming churn by freezing mutation nouns now:
  - mutation authority
  - command intent
  - semantic mutation
  - projection mutation
  - transient interaction
  - mutation result
- The story should not yet decide the first concrete graph-originated semantic or projection edit path; those belong to Stories `2.2` and `2.3`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M8-2026-07-10.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m8/sprint-status.yaml]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt]
- [Source: ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx]
- [Source: integrations/graph-glsp/src/athena-glsp-projection-adapter.ts]

## Story Completion Status

- Status: review
- Completion note: Runtime-owned mutation contracts, explicit categories, and validation-feedback outcomes are implemented and verified for M8 Story 1.1.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M8 PRD, addendum, architecture spine, and epic breakdown review
- current runtime command/history service review
- current projection protocol review
- current graph workbench and graph adapter boundary review
- current semantic SCM widget review
- recent commit history review
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Added `AthenaMutationContract.kt` to publish runtime-owned mutation categories, outcomes, and validation-feedback items with clean KDoc.
- Extended command execution and history mutation results to carry explicit mutation categories, stable outcomes, and validation feedback while preserving the existing `CONNECT_PORTS` behavior.
- Updated AI proposal, hosted plugin, CLI, and desktop-viewer wrappers so the new validation-feedback outcome remains inspectable through downstream runtime-owned surfaces.
- Added focused runtime contract assertions and verified `:kernel:runtime:test`, `:ide:lsp:test`, and the full workspace `test` task sequentially under Java 25.
- Kept Story `1.1` contract-only: no graph gesture handling, LSP mutation execution, or projection ownership logic was introduced.

### File List

- _bmad-output/implementation-artifacts/m8/sprint-status.yaml
- _bmad-output/implementation-artifacts/m8/1-1-publish-unified-mutation-contracts-and-categories-in-runtime-owned-command-surfaces.md
- apps/cli/src/main/kotlin/com/engineeringood/athena/cli/AthenaCliSessionStore.kt
- apps/cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt
- apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt

### Change Log

- 2026-07-10: Implemented M8 Story 1.1 by publishing shared runtime mutation contracts, extending command/history outcomes with validation feedback, and propagating the new sealed result branches through CLI and desktop-viewer consumers.
