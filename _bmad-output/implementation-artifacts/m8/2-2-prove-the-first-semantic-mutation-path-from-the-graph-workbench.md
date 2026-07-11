---
baseline_commit: 4b09cacc3435a1c902dc5be72ca30a3c596f784e
---

# Story 2.2: Prove The First Semantic Mutation Path From The Graph Workbench

Status: done

## Story

As an engineer,  
I want one supported graph-originated semantic edit to execute through Athena runtime,  
so that the graph becomes a real engineering editing surface without bypassing validation or semantic authority.

## FR Traceability

- FR-1: route all meaningful changes through Athena commands
- FR-2: classify meaningful changes explicitly
- FR-3: keep graph-originated editing downstream of Athena-owned meaning
- FR-4: accepted semantic mutation refreshes graph state from canonical upstream meaning
- FR-7: projection ownership contracts define what a view may emit
- NFR-1: meaningful changes route through one Athena-owned mutation path
- NFR-2: canonical engineering meaning remains upstream of any renderer or editor client
- NFR-4: command intents, mutation outcomes, and rejection paths remain inspectable
- NFR-6: graph stack must not own command meaning or durable mutation semantics

## Acceptance Criteria

1. Given a supported graph-originated semantic edit path, when the user requests that edit from the graph workbench, then Athena routes the request through command intent, runtime evaluation, validation, canonical engineering-state update, and deterministic projection refresh, and the resulting graph state reflects the accepted semantic outcome rather than local canvas truth.
2. Given the same semantic edit is invalid or disallowed, when runtime rejects it or returns validation feedback, then the workbench surfaces that result through Athena-owned feedback, and the graph does not retain durable divergent local state.

## Tasks / Subtasks

- [x] Extend the graph-intent contract with one real semantic intent that reuses the existing runtime command vocabulary. (AC: 1, 2)
  - [x] Keep the proof target narrow by reusing `CONNECT_PORTS` rather than inventing a second semantic mutation path.
  - [x] Publish typed intent arguments for a graph-originated port-to-port connection request without leaking renderer-native gesture meaning.
  - [x] Keep inspectable mutation identity, category, and target information visible across runtime and LSP transport.
- [x] Govern the semantic graph intent through projection ownership contracts and runtime evaluation. (AC: 1, 2)
  - [x] Use `semanticCommandIds` on the interactive projection contract instead of assuming graph editability from frontend behavior.
  - [x] Validate view ownership, subject kinds, and required target arguments before semantic mutation executes.
  - [x] Delegate accepted semantic intent to the existing `AthenaCommandRuntimeService.execute(...)` path.
- [x] Extend the Athena LSP boundary so semantic graph intent results remain inspectable and frontend-safe. (AC: 1, 2)
  - [x] Carry enough result detail for the workbench to distinguish accepted execution, rejection, validation feedback, and unavailable states.
  - [x] Preserve existing placement-intent behavior from Story `2.1` without regressing the later projection proof path in Story `2.3`.
- [x] Add one graph workbench interaction that proves the semantic mutation path without introducing graph-local truth. (AC: 1, 2)
  - [x] Keep the interaction inside the governed `cabinet` view and leave `wiring` inspect-only.
  - [x] Refresh the rendered graph from runtime-owned projection state only after accepted semantic execution.
  - [x] Surface rejection or validation feedback in the workbench without retaining divergent local mutation state.
- [x] Verify the semantic proof with focused regression-safe tests and sequential Windows verification. (AC: 1, 2)
  - [x] Add runtime, LSP, and Theia coverage for accepted semantic execution and rejected/disallowed execution.
  - [x] Run Gradle and Node verification sequentially on Windows with Java 25.

## Dev Notes

### Story Intent

- Story `2.1` stopped at Athena-owned intent publication and inspectable pre-execution routing.
- Story `2.2` is the first place where a graph-originated semantic action is allowed to reach canonical mutation execution.
- The narrow proof target is the existing `AthenaConnectPortsCommand` path because it already has canonical runtime behavior, history capture, diff inspection, and projection refresh.

### Architecture Guardrails

- Align to AD-34, AD-36, and AD-37 by forcing graph-originated semantic edits through Athena command intent and runtime-owned mutation evaluation instead of renderer-local state.
- Align to AD-40 by governing semantic graph edits through the projection ownership contract, not by assuming every visible graph element is editable.
- Preserve Story `2.1`'s boundary decision: `integrations/graph-glsp` remains translation-only, `ide/lsp` remains the IDE boundary, and runtime remains the mutation authority.

### Technical Notes

- The likely graph interaction target is the cabinet view's port label nodes, because the current projection already exposes those as selectable semantic graph elements.
- The semantic proof should add to the existing graph-intent contract instead of creating a second graph-mutation protocol.
- Public/core Kotlin additions must keep clean KDoc.

### Testing Requirements

- Minimum verification commands:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
  - `yarn --cwd integrations/graph-glsp test`
  - `yarn --cwd ide/theia-frontend test`
- Keep all Gradle and Node verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

## References

- [Source: _bmad-output/planning-artifacts/epics-M8-2026-07-10.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m8/2-1-translate-supported-graph-gestures-into-athena-command-intents.md]
- [Source: _bmad-output/implementation-artifacts/m1/2-4-deliver-one-gui-command-backed-port-connection-mutation-path.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt]
- [Source: apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt]

## Story Completion Status

- Status: done
- Completion note: The cabinet graph workbench can now emit a governed `connect-ports` Athena intent that executes through the existing runtime semantic command path, returns inspectable execution results through LSP, and refreshes the graph from canonical runtime state only after accepted execution.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- `yarn --cwd integrations/graph-glsp test`
- `yarn --cwd ide/theia-frontend test`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`

### Completion Notes List

- Extended the runtime-owned graph intent contract with a semantic `connect-ports` intent and inspectable execution detail payloads.
- Governed graph-originated semantic edits through `semanticCommandIds` on the cabinet ownership contract while keeping wiring inspect-only.
- Routed accepted graph semantic intent to the existing `AthenaConnectPortsCommand` execution path instead of creating a graph-local semantic mutation model.
- Added a Theia graph workbench connect-ports interaction that selects port labels, submits the Athena intent, and refreshes the diagram from canonical runtime state after accepted execution.
- Verified sequentially on Windows with Java 25 across runtime/LSP Gradle tests plus `integrations/graph-glsp` and `ide/theia-frontend` Node suites.
