---
baseline_commit: 4b09cacc3435a1c902dc5be72ca30a3c596f784e
---

# Story 2.1: Translate Supported Graph Gestures Into Athena Command Intents

Status: done

## Story

As a platform engineer,  
I want the graph workbench to emit Athena command intents instead of renderer-native save behavior,  
so that graph interaction remains downstream of Athena mutation semantics.

## FR Traceability

- FR-1: route all meaningful changes through Athena commands
- FR-2: classify meaningful changes explicitly
- FR-3: keep graph-originated editing downstream of Athena-owned meaning
- FR-7: projection ownership contracts define what a view may emit
- FR-8: keep mutation semantics independent from the current renderer stack
- NFR-1: meaningful changes route through one Athena-owned mutation path
- NFR-2: canonical engineering meaning remains upstream of any renderer or editor client
- NFR-4: command intents, mutation outcomes, and rejection paths remain inspectable
- NFR-6: graph stack must not own command meaning or durable mutation semantics

## Acceptance Criteria

1. Given the current Theia graph workbench and `integrations/graph-glsp` adapter, when users perform a supported graph-originated action, then the frontend and adapter translate that action into Athena-owned command intent over existing transport seams, and no renderer-local save model becomes persistent authority.
2. Given command-intent payloads are reviewed, when inspectability is checked, then intent identity, mutation category, and target subject remain visible enough for debugging and architecture review, and the graph stack remains a downstream client of Athena command meaning.

## Tasks / Subtasks

- [x] Publish one runtime-owned graph command-intent contract that stays renderer-neutral and inspectable. (AC: 1, 2)
  - [x] Add a typed command-intent/request model under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/` or a closely related runtime-owned file so graph-originated actions can be described without inventing frontend-only maps.
  - [x] Reuse `AthenaMutationCategory` and the ownership-contract vocabulary already published in Story `1.2`; do not encode raw GLSP or DOM gesture names such as `drag`, `drop`, or `save` as durable Athena meaning.
  - [x] Keep this story pre-execution only: do not broaden `AthenaCommandRuntimeService.execute(...)` into the first real semantic or projection mutation path yet.
- [x] Extend the Athena LSP boundary to carry graph command intents over the existing workbench transport seam. (AC: 1, 2)
  - [x] Add one typed request/payload surface in `ide/lsp` for graph-originated command intent submission and inspectable echo/decision data.
  - [x] Ensure the payload remains debuggable and architecture-reviewable by surfacing at least:
    - stable intent identity
    - mutation category
    - Athena command or intent id
    - target semantic subject and/or projection subject
    - active view context
    - typed arguments needed by later Stories `2.2` and `2.3`
  - [x] Keep the existing `switch-active-view` governed projection command working; do not regress current view-switch behavior while adding the new intent path.
- [x] Translate the first supported graph workbench actions into Athena-owned command intent without creating renderer-local persistence. (AC: 1, 2)
  - [x] Extend `ide/theia-frontend` graph adapter and workbench code so supported graph actions flow through Athena-owned intent translation instead of private renderer save behavior.
  - [x] Use the view ownership contracts already exposed through runtime/LSP to govern which actions may emit command intent.
  - [x] Preserve current transient-only interactions such as pan, zoom, fit, hover, and selection as frontend-local behavior.
  - [x] Keep `wiring` inspect-only. Interactive command-intent emission must remain governed by the `cabinet` ownership contract and not leak into non-interactive views.
- [x] Keep the GLSP side translation-only and disposable. (AC: 1, 2)
  - [x] If `integrations/graph-glsp` needs new types or helpers, keep them limited to adapter translation or transport shaping.
  - [x] Do not introduce a durable graph document, renderer-owned mutation queue, or adapter-local persistence model.
- [x] Verify the translation path with focused regression-safe tests. (AC: 1, 2)
  - [x] Add focused coverage for the new runtime/LSP intent contract, the Theia bridge request helper, and the graph-adapter translation path.
  - [x] Cover at least:
    - supported interactive action emits Athena-owned intent
    - inspect-only projection does not emit unsupported mutation intent
    - payload contains inspectable identity, mutation category, and target information
    - existing view-switch request path remains green
  - [x] Run Gradle and Node verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `2.1` is the transport-and-translation story for Epic `2`, not the first live graph mutation execution story.
- The success condition is that the graph workbench can describe supported actions in Athena-owned command intent vocabulary before runtime starts accepting real semantic or projection edits from those actions.
- Story `2.2` owns the first real semantic mutation path from the graph workbench.
- Story `2.3` owns the first real projection mutation path from the graph workbench.
- Story `2.1` should therefore stop at typed intent publication, inspectability, and transport routing.

### Architecture Guardrails

- Align to AD-34 by freezing one mutation authority above source and graph. Graph interaction may begin in Theia/GLSP, but durable change meaning must converge into Athena-owned command intent before review or persistence. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-34]
- Align to AD-35 by classifying emitted intents explicitly as semantic mutation, projection mutation, or transient interaction. The category belongs to Athena-owned intent meaning, not to UI conventions. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-35]
- Align to AD-36 by translating supported graph actions into Athena-owned command intent rather than renderer-native save state or private graph protocol meaning. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-36]
- Align to AD-37 by keeping runtime as the owner of later evaluation, rejection, validation, and refresh. Story `2.1` may route intent to Athena boundaries, but it must not become the first execution story. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-37]
- Align to AD-40 by treating projection ownership contracts as the authority on which views may emit which intent families. `cabinet` may participate; `wiring` remains inspect-only. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-40]
- Align to AD-41 and AD-42 by preparing the same mutation authority model that source edits already use, while keeping scope narrow until Stories `2.2` and `2.3` prove one real semantic path and one real projection path. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-41] [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-42]

### Technical Requirements

- The current graph workbench path today is:
  - `AthenaGraphWorkbenchWidget`
  - `AthenaGraphAdapterService`
  - `AthenaLspEditorBridgeService`
  - `AthenaProjectionProtocol`
  - `AthenaLanguageServer`
  - runtime-owned projection session
- The current graph workbench already supports:
  - active-view switching through `switch-active-view`
  - transient selection
  - transient pan/zoom/fit
  - inspect-first overlay behavior
- The current ownership-contract path already exposes:
  - `interactivity`
  - `semanticCommandIds`
  - `projectionCommandIds`
  - transient interaction kinds
  - persisted projection metadata keys
- Story `2.1` should build on those typed seams instead of inventing a second graph-editing authority model.
- Use the addendum's M8 intent framing as the design anchor:
  - renderer gestures stay UI affordances only
  - Athena-owned command intent is the durable meaning
  - candidate projection example: `MoveProjectionNode(id="KM1", projection="cabinet", position=(x, y))`
- If new runtime-owned shared types are required, place them beside the existing mutation contracts in `kernel/runtime` rather than under Theia or `integrations/graph-glsp`.
- Add clean KDoc for all new public/core Kotlin classes introduced by this story.

### Architecture Compliance

- Preserve these boundaries:
  - `ide/lsp` remains the only IDE semantic/projection boundary
  - `integrations/graph-glsp` remains translation-only
  - Theia workbench remains a downstream client, not a mutation authority
  - runtime-owned mutation semantics stay above transport/framework-specific event names
- Prevent these failure modes:
  - a frontend-only graph mutation protocol that never becomes Athena-owned
  - direct GLSP or widget save behavior becoming durable authority
  - treating transient canvas movement as accepted projection mutation before runtime says so
  - letting `wiring` emit mutation intents despite its inspect-only ownership contract
  - Story `2.1` widening into the actual command execution logic that belongs in Stories `2.2` and `2.3`

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the current M8 architecture:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Eclipse Theia `1.73.1`
- Reuse the existing Kotlin/JUnit and TypeScript test conventions already present in runtime, LSP, `integrations/graph-glsp`, and `ide/theia-frontend`.
- Do not add third-party gesture, state-machine, or persistence libraries for this story.

### File Structure Requirements

- Expected update files:
  - [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)
  - [`ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`](../../../ide/theia-frontend/src/browser/athena-graph-adapter-service.ts)
  - [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) or one dedicated graph-intent protocol file beside it
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt)
  - [`integrations/graph-glsp/src/athena-glsp-diagram-model.ts`](../../../integrations/graph-glsp/src/athena-glsp-diagram-model.ts) if transport types need additive intent metadata
  - [`integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`](../../../integrations/graph-glsp/src/athena-glsp-projection-adapter.ts) if adapter-side translation needs to stay type-complete
- Likely new files if a shared Athena-owned intent contract is needed:
  - one runtime-owned Kotlin intent contract file under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/`
  - one focused LSP protocol file under `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
- Expected focused test updates:
  - [`ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`](../../../ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt) or one dedicated graph-intent request test
  - [`ide/theia-frontend/scripts/athena-lsp-editor-bridge-service.test.mjs`](../../../ide/theia-frontend/scripts/athena-lsp-editor-bridge-service.test.mjs)
  - [`integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`](../../../integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs)
- Files whose current behavior and ownership must be preserved:
  - [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)
    - current pan/zoom/fit/selection and inspect-first behavior remain transient unless explicitly translated into Athena intent
  - [`ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`](../../../ide/theia-frontend/src/browser/athena-graph-adapter-service.ts)
    - currently owns only diagram request and governed active-view switching; new work should extend this seam rather than bypass it
  - [`integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`](../../../integrations/graph-glsp/src/athena-glsp-projection-adapter.ts)
    - remains a disposable translation boundary, not a persisted graph state owner
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt)
    - currently exposes supported views, ownership contracts, and governed projection commands; new intent transport should extend this boundary additively

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
  - `yarn --cwd integrations/graph-glsp test`
  - `yarn --cwd ide/theia-frontend test`
- Required proof checks:
  - supported graph-originated action emits Athena-owned command intent payload
  - inspect-only views do not emit unsupported mutation intent
  - intent payload exposes inspectable identity, mutation category, and target subject
  - no renderer-local save model or durable local mutation queue is introduced
  - current `switch-active-view` behavior remains green
- Keep all Gradle and Node verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`](../../../ide/theia-frontend/src/browser/athena-graph-adapter-service.ts) currently exposes only:
  - `requestDiagram()`
  - `switchActiveView(viewId)`
  It does not yet expose graph-originated command-intent requests.
- [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts) currently exposes typed request helpers for projection sessions, governed projection commands, and source-mutation evaluation, but no graph command-intent request helper.
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) currently supports:
  - projection session payloads
  - ownership-contract payloads
  - one governed projection command request: `switch-active-view`
  It does not yet expose a graph-originated Athena command-intent request.
- [`kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`](../../../kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt) already publishes ownership contracts with `semanticCommandIds`, `projectionCommandIds`, and transient interaction kinds.
- [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt) currently marks:
  - `cabinet` as interactive with projection command families `adjust-layout-placement` and `adjust-layout-grouping`
  - `wiring` as inspect-only
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt) currently owns executable semantic command results and only supports `CONNECT_PORTS` as the live semantic command kind. Story `2.1` must not silently turn this into the first graph-execution implementation.

### Previous Story Intelligence

- Story `1.1` already proved:
  - one shared runtime-owned mutation result vocabulary
  - mutation categories must stay explicit and inspectable
  - public wrappers should not invent parallel semantics
- Story `1.2` already proved:
  - projection ownership contracts are the correct place to declare what a view may emit
  - `cabinet` is interactive while `wiring` remains inspect-only
  - renderer-neutral nouns matter; public contracts must not leak graph-framework vocabulary
- Story `1.3` already proved:
  - source-originated changes can converge into the shared mutation-result path without mutating canonical state or history prematurely
  - additive transport extension is safer than replacing existing request flows
- Practical carry-forward for Story `2.1`:
  - extend typed seams additively
  - keep the graph stack downstream
  - stop at intent transport and inspectability
  - leave real semantic/projection execution to Stories `2.2` and `2.3`

### Git Intelligence Summary

- Recent milestone baseline:
  - `4b09cac feat(m8): complete epic 1 mutation foundations`
  - `d8e1c61 feat: complete m7 graphical workbench proof`
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
- Practical implication:
  - build on the existing M7 workbench/LSP/graph adapter seams
  - reuse Epic `1` mutation vocabulary and ownership contracts instead of inventing new graph-local terms
  - keep the first real mutation execution proof for the next two stories

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Eclipse Theia `1.73.1`

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- `m8/` is the active milestone folder and Story `2.1` becomes the first Epic `2` implementation artifact under it.
- The intended M8 progression now stays explicit:
  - Epic `1`: freeze mutation categories, ownership contracts, and source-side convergence
  - Story `2.1`: translate graph actions into Athena command intent
  - Story `2.2`: execute one real semantic mutation path
  - Story `2.3`: execute one real projection mutation path
  - Epic `3`: unify review and reveal
- Keep naming easy to understand and Athena-owned:
  - command intent
  - mutation category
  - semantic subject
  - projection subject
  - active view context

### References

- [Source: _bmad-output/planning-artifacts/epics-M8-2026-07-10.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m8/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m8/1-1-publish-unified-mutation-contracts-and-categories-in-runtime-owned-command-surfaces.md]
- [Source: _bmad-output/implementation-artifacts/m8/1-2-define-projection-ownership-contracts-for-the-first-interactive-projections.md]
- [Source: _bmad-output/implementation-artifacts/m8/1-3-normalize-source-originated-changes-into-the-same-runtime-mutation-result-path.md]
- [Source: draft/m8/003-draft.md]
- [Source: kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaMutationContract.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-graph-adapter-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: integrations/graph-glsp/src/athena-glsp-diagram-model.ts]
- [Source: integrations/graph-glsp/src/athena-glsp-projection-adapter.ts]

## Story Completion Status

- Status: done
- Completion note: Cabinet node dragging now emits Athena-owned `adjust-layout-placement` intent through runtime and LSP validation/echo, while inspect-only views remain rejected and existing transient interactions stay frontend-local.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M8 sprint status, epic breakdown, PRD, addendum, and architecture spine review
- Story `1.2` and Story `1.3` completion record review for ownership and mutation-result carry-forward
- CodeGraph exploration of graph workbench, graph adapter, LSP projection protocol, language server, and runtime mutation surfaces
- current Theia graph workbench, adapter, GLSP model, GLSP adapter, LSP protocol, and runtime command service review
- recent commit history review
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
- `yarn --cwd integrations/graph-glsp test`
- `yarn --cwd ide/theia-frontend test`

### Completion Notes List

- Added a runtime-owned graph command-intent contract and validator for `adjust-layout-placement` without widening into canonical projection mutation execution.
- Added an Athena LSP `athena/graphCommandIntent` boundary with typed request/payload mapping and explicit unavailable or rejected results.
- Added Theia-side graph command-intent request builders plus adapter wiring so the graph workbench stays downstream of the Athena LSP bridge.
- Extended the graphical workbench with transient cabinet-node drag handling that emits Athena-owned intent on release, then snaps back because Story `2.1` stops before mutation execution.
- Surfaced the last graph command-intent receipt inside the workbench for inspectability and kept `switch-active-view`, pan, zoom, fit, hover, and selection behavior intact.
- Verified sequentially on Windows with Java 25 through runtime/LSP Gradle tests plus `integrations/graph-glsp` and `ide/theia-frontend` Node suites.

### File List

- _bmad-output/implementation-artifacts/m8/2-1-translate-supported-graph-gestures-into-athena-command-intents.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentRuntimeService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentServiceTest.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGraphCommandIntentProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt
- ide/theia-frontend/src/browser/athena-graph-adapter-service.ts
- ide/theia-frontend/src/browser/athena-graph-command-intent-protocol.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/scripts/athena-graph-command-intent-protocol.test.mjs
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.d.ts
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.js
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.js.map
- ide/theia-frontend/lib/browser/athena-graph-command-intent-protocol.d.ts
- ide/theia-frontend/lib/browser/athena-graph-command-intent-protocol.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-command-intent-protocol.js
- ide/theia-frontend/lib/browser/athena-graph-command-intent-protocol.js.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js
- ide/theia-frontend/lib/browser/athena-graph-workbench-widget.js.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map
- ide/theia-frontend/tsconfig.tsbuildinfo
- _bmad-output/implementation-artifacts/m8/sprint-status.yaml
