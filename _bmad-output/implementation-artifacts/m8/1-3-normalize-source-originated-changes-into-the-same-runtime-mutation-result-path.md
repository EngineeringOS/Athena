---
baseline_commit: d8e1c6163b7edd8895e8b8fe182519f0fbf54b53
---

# Story 1.3: Normalize Source-Originated Changes Into The Same Runtime Mutation Result Path

Status: done

## Story

As an IDE and runtime engineer,  
I want source-originated edits to produce the same governed mutation-result shape used by graph-originated edits,  
so that text and graph remain two clients of one mutation authority before review and persistence.

## FR Traceability

- FR-1: route all meaningful changes through Athena commands
- FR-2: classify meaningful changes explicitly
- FR-4: refresh graphical state deterministically after an accepted change
- FR-5: produce unified semantic review facts for accepted mutations
- FR-7: publish projection ownership contracts
- FR-8: preserve renderer-neutral mutation semantics
- NFR-1: meaningful changes must route through one Athena-owned mutation path
- NFR-2: canonical engineering meaning remains upstream of any renderer or editor client
- NFR-4: command intents, mutation outcomes, rejection paths, and review facts remain inspectable
- NFR-5: graph-originated and source-originated mutations must share one semantic review and history vocabulary

## Acceptance Criteria

1. Given an accepted source-originated change in an active Athena repository session, when Athena evaluates the change, then runtime produces the same governed mutation-result structure used by graph-originated change requests, and validation, refresh, and later review consequences are represented through the same runtime-owned path.
2. Given a source-originated change fails validation or is rejected by ownership policy, when the result is surfaced, then Athena exposes inspectable rejection or validation-feedback output, and the failure does not create a second editor-only mutation semantics path.

## Tasks / Subtasks

- [x] Publish a runtime-owned source-mutation evaluation contract that reuses the governed mutation-result vocabulary. (AC: 1, 2)
  - [x] Add typed runtime-owned result shapes for source-originated mutation evaluation beside the existing mutation contracts instead of inventing editor-only payloads.
  - [x] Reuse `AthenaMutationCategory`, `AthenaMutationOutcome`, and `AthenaMutationValidationFeedback` as the shared mutation-result language.
  - [x] Add clean KDoc for every new public/core Kotlin type introduced by this story.
- [x] Evaluate dirty source buffers against canonical runtime state without mutating canonical cache or history yet. (AC: 1, 2)
  - [x] Extend the runtime path so an in-memory tracked source compilation can be compared against the active canonical project state and produce one governed source-mutation evaluation result.
  - [x] Keep accepted source evaluation additive and preview-oriented: do not write files, mutate command history, or replace canonical runtime state in Story `1.3`.
  - [x] Represent refresh and later review consequences through Athena-owned inspection data rather than editor-private heuristics.
- [x] Surface source-mutation evaluation through the Athena LSP boundary. (AC: 1, 2)
  - [x] Add one typed Athena LSP request/payload for source-mutation evaluation so IDE clients can inspect the same runtime-owned result shape used downstream by future graph editing.
  - [x] Keep `textDocument/didOpen` and `textDocument/didChange` as document-tracking notifications only; the new evaluation request must remain explicit and typed.
  - [x] Preserve the current projection-session preview path and diagnostics publishing path while normalizing source-originated mutation meaning beside them.
- [x] Expose downstream TypeScript transport typing without widening into UI workflow redesign yet. (AC: 1)
  - [x] Extend the Theia LSP bridge type surface so downstream clients can request and inspect source-mutation evaluation payloads through the same semantic boundary.
  - [x] Do not add new editor UX, approval flow, save workflow, or persistence behavior in Story `1.3`.
- [x] Verify that invalid or unauthorized source-originated changes stay governed and inspectable. (AC: 2)
  - [x] Cover accepted, validation-feedback, and rejection/unavailable cases with focused runtime and LSP tests.
  - [x] Prove that stale document versions or dirty-buffer edits do not overwrite the canonical runtime cache or command history.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

### Review Findings

- [x] [Review][Patch] Reject equivalent authoritative paths by same-file identity instead of `normalize()` string equality. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt:98]
- [x] [Review][Patch] Treat semantically invalid canonical baselines as unavailable before accepting source-mutation diffs. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt:105]
- [x] [Review][Patch] Derive source-mutation projection consequences from the existing preview/runtime path instead of fabricating all-view layout and geometry refreshes. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt:156]
- [x] [Review][Patch] Preserve semantic diagnostic identity in validation feedback so LSP clients receive anchored `relatedSemanticIds`. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt:123]
- [x] [Review][Patch] Return one structured unavailable payload contract when the LSP session is inactive, rather than `null`. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt:222]
- [x] [Review][Patch] Correlate unavailable source-mutation payloads to the actual editor state instead of hard-coding `version = 0`. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationProtocol.kt:157]
- [x] [Review][Patch] Re-synchronize the model when requesting source mutation evaluation so in-flight editor changes cannot return stale results. [ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts:772]
- [x] [Review][Patch] Add the missing LSP rejection/unavailable tests and the required Theia bridge Node-side coverage for the new request/type surface. [ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt:16]

## Dev Notes

### Story Intent

- Story `1.3` is the source-originated counterpart to Story `1.1`, not a file-save or persistence milestone.
- The current Theia/LSP flow already tracks dirty `.athena` buffers and publishes diagnostics, but it stops before the governed mutation-result language.
- The goal here is to normalize source-originated evaluation into Athena-owned mutation-result semantics before Epic `2.x` introduces the first live graph-originated edit paths.
- Story `3.1` will later feed accepted mutation outcomes into the unified semantic review model. Story `1.3` should represent those later consequences through the same runtime-owned path without implementing the full review UI/workflow yet.

### Architecture Guardrails

- Align to AD-34 by freezing one mutation authority above source and graph. Dirty source edits may begin in the editor, but their governed evaluation result must become Athena-owned before review or persistence. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-34]
- Align to AD-35 by classifying source-originated evaluation as semantic mutation and by keeping transient editor behavior out of the durable mutation contract. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-35]
- Align to AD-37 by keeping acceptance, rejection, validation feedback, and refresh consequences runtime-owned. The editor may request evaluation, but runtime decides the mutation result. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-37]
- Align to AD-38 and AD-41 by ensuring source-originated changes converge into the same review and mutation language that graph-originated edits will later use. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-38] [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-41]
- Align to AD-42 by keeping scope narrow: this story normalizes the source path only. Do not widen into graph gesture translation, persistence workflow, or review-surface redesign. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-42]

### Technical Requirements

- The existing source edit path today is:
  - Theia bridge `textDocument/didOpen` / `textDocument/didChange`
  - Athena LSP tracked document state in `AthenaLanguageFeatures`
  - compiler compilation for dirty buffer state
  - diagnostics publishing
  - projection preview through `previewProjectionSession(...)`
- The existing governed mutation-result vocabulary already exists in:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt)
- The current projection preview path already proves that dirty tracked documents can remain visually aligned without mutating canonical runtime cache:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt)
- Story `1.3` should build beside those seams rather than bypassing them with editor-only comparison logic.
- The cleanest target is one runtime-owned source-mutation evaluation result that:
  - compares dirty tracked compilation against active canonical compilation
  - reports accepted / rejected / validation-feedback / unavailable through `AthenaMutationResult`
  - exposes changed semantic ids and downstream inspection data needed for later review/refresh reasoning
  - does not mutate canonical runtime state or command history in this story

### Architecture Compliance

- Preserve these boundaries:
  - `AthenaLanguageServer.didOpen` / `didChange` keep tracking documents and publishing diagnostics
  - `AthenaExecutionContext.compileActiveProject()` remains the canonical cached state source
  - dirty source evaluation must not call `replaceActiveProjectDocument(...)` in Story `1.3`
  - source-originated evaluation must not append command-history records in Story `1.3`
- Prevent these failure modes:
  - using editor diagnostics as the only mutation-result contract
  - adding a second source-only outcome vocabulary that bypasses `AthenaMutationResult`
  - mutating canonical runtime cache from dirty-buffer evaluation
  - introducing frontend-owned diff or refresh semantics
  - widening into save/apply/persist workflow before Epic `2.x` and Epic `3.x`

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the current M8 architecture:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Eclipse Theia `1.73.1`
- Reuse the existing Kotlin/JUnit and TypeScript test conventions already present in runtime, LSP, and Theia bridge modules.
- Do not add third-party diff, mutation, or editor-state libraries for this story.

### File Structure Requirements

- Expected update files:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt)
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt) if tracked-document access or path normalization needs tightening
  - [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts) if downstream transport typing is exposed
- Likely new files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationProtocol.kt`
- Expected focused test updates:
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeServiceTest.kt`
  - [`ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepeatedEditingStabilityTest.kt`](../../../ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepeatedEditingStabilityTest.kt)
  - new or updated LSP request tests under `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
  - Theia bridge Node test only if a new request helper/type surface is added

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
- Required proof checks:
  - accepted source evaluation returns a governed mutation-result payload instead of diagnostics-only output
  - invalid source evaluation returns inspectable validation-feedback or rejection output
  - dirty source evaluation does not replace canonical runtime state or append command history
  - projection preview remains aligned with dirty tracked compilation
  - the new LSP request exposes the runtime-owned result shape without inventing editor-private semantics
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt) currently:
  - tracks dirty source through `didOpen` / `didChange`
  - compiles dirty text through `AthenaLanguageFeatures.trackDocument(...)`
  - publishes diagnostics
  - exposes projection session, semantic inspection, semantic SCM state, and history requests
  It does not yet expose a typed source-mutation evaluation request.
- [`AthenaLanguageFeatures.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt) currently owns tracked dirty document state and compiler results, including version filtering and `trackedDocumentByPath(...)`.
- [`AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt) currently distinguishes:
  - canonical cached compilation through `compileActiveProject()`
  - preview-only projection alignment through `previewProjectionSession(...)`
  It does not yet normalize dirty source compilation into a governed mutation-result contract.
- [`AthenaMutationContract.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt) already defines the shared mutation category, outcome, and validation feedback vocabulary that source evaluation should reuse.
- [`athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts) currently forwards dirty text and requests diagnostics/projection/semantic SCM state, but it has no typed helper for source-mutation evaluation.

### Previous Story Intelligence

- Story `1.1` already proved:
  - one shared `AthenaMutationResult` vocabulary
  - additive extension discipline matters
  - wrappers should delegate to the shared result contract instead of inventing parallel public semantics
- Story `1.2` already proved:
  - dirty tracked compilation can preview projection state without mutating canonical runtime cache
  - projection ownership stays explicit and transport-safe
  - adapter/frontend transport can remain downstream and typed
- Practical carry-forward for Story `1.3`:
  - extend public shapes additively
  - prefer runtime-owned typed results over editor-only strings or booleans
  - keep source evaluation narrow and preview-oriented; no persistence or command-history mutation yet

### Git Intelligence Summary

- Recent milestone baseline:
  - `d8e1c61 feat: complete m7 graphical workbench proof`
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
- Practical implication:
  - build on the existing M4-M7 LSP/document-tracking seams
  - do not reopen repository/session activation
  - reuse the existing runtime mutation and semantic-diff contracts instead of inventing a source-only path

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Eclipse Theia `1.73.1`

### Project Structure Notes

- `m8/` is the active milestone folder and Story `1.3` should become the third implementation artifact under it.
- This story should normalize source-originated semantics before:
  - Epic `2.x` proves graph-originated semantic and projection mutation paths
  - Epic `3.x` routes accepted mutation outcomes into unified review/reveal surfaces
- Keep the path explicit:
  - dirty source buffer
  - tracked compilation
  - runtime-owned source mutation evaluation
  - preview / consequence inspection
  - later review and persistence layers

### References

- [Source: _bmad-output/planning-artifacts/epics-M8-2026-07-10.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m8/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m8/1-1-publish-unified-mutation-contracts-and-categories-in-runtime-owned-command-surfaces.md]
- [Source: _bmad-output/implementation-artifacts/m8/1-2-define-projection-ownership-contracts-for-the-first-interactive-projections.md]
- [Source: draft/m8/003-draft.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]

## Story Completion Status

- Status: review
- Completion note: Runtime-owned source mutation evaluation now reuses the shared mutation-result vocabulary, stays preview-only against canonical cache/history, and is exposed through one typed Athena LSP request plus downstream Theia transport typing.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M8 epic, PRD, addendum, and architecture spine review
- Story 1.2 completion record review
- current LSP tracked-document, diagnostics, and projection-preview path review
- current runtime mutation contract and command-history contract review
- semantic diff / review service review
- recent commit history review
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSourceMutationRuntimeServiceTest :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"`
- `yarn --cwd ide/theia-frontend test`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Added `AthenaSourceMutationRuntimeService` plus typed accepted/rejected/validation-feedback/unavailable source mutation result contracts that reuse the shared runtime mutation vocabulary.
- Kept source-originated evaluation preview-only by comparing dirty source compilations against canonical runtime state without replacing canonical cache, mutating command history, or overwriting the latest canonical semantic diff inspection.
- Added the explicit `athena/sourceMutationEvaluation` LSP request, transport-safe mutation/inspection payloads, and Theia bridge typing for downstream clients.
- Added focused runtime and LSP tests for accepted, validation-feedback, rejection/unavailable, and stale-version cases, then reran Theia package tests and the full JVM regression suite with Java 25.

### File List

- _bmad-output/implementation-artifacts/m8/1-3-normalize-source-originated-changes-into-the-same-runtime-mutation-result-path.md
- _bmad-output/implementation-artifacts/m8/sprint-status.yaml
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspection.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeService.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSourceMutationRuntimeServiceTest.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map
- ide/theia-frontend/tsconfig.tsbuildinfo

### Change Log

- 2026-07-10: Created M8 Story 1.3 with comprehensive context for source-originated mutation normalization, shared mutation-result contracts, and dirty-buffer guardrails.
- 2026-07-10: Implemented preview-only source mutation evaluation, typed LSP transport payloads, Theia bridge typing, and focused/runtime regression coverage for the unified mutation path.
