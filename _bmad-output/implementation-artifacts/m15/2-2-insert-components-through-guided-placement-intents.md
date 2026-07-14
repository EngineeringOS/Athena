---
baseline_commit: c04b3eb
---

# Story 2.2: Insert Components Through Guided Placement Intents

Status: done

## Story

As an engineer,  
I want to insert one component instance through the workbench,  
so that Athena creates governed engineering intent rather than a temporary graph shape.

## FR Traceability

- FR-4: Athena can insert components through guided placement intents
- FR-10: Athena can keep source, graph, and guided authoring in sync
- NFR-1: guided authoring introduces no second write path outside M8
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-3: available components, parts, ports, and traits derive from active M14 component knowledge rather than frontend hardcoding
- NFR-6: the first proof stays narrow, Siemens-first, and electrical only

## Acceptance Criteria

1. Given a user double-clicks, commands, or drags one available component, when insertion is requested, then Athena emits a guided placement intent rather than directly creating a graph node.
2. Given the insertion preview is accepted, when canonical state is rebuilt, then the project contains one new component instance in source and graph.

## Tasks / Subtasks

- [x] Publish one workbench insertion gesture from the governed component panel. (AC: 1)
  - [x] Let one component-panel action submit a `create-component` authoring preview request.
  - [x] Keep the panel surface thin; do not create graph nodes directly from frontend code.
  - [x] Keep the first proof narrow to one active Athena editor and one current repository session.
- [x] Return one governed source-backed insertion handoff from the Athena backend. (AC: 1, 2)
  - [x] Extend the authoring decision path so accepted create-component previews can return one backend-generated source edit.
  - [x] Derive inserted authored fields from governed authoring intent and active component knowledge instead of from a frontend-local palette catalog.
  - [x] Keep rejection non-mutating.
- [x] Apply accepted insertion into the active `.athena` source buffer and rely on the existing LSP semantic boundary for rebuild. (AC: 2)
  - [x] Apply the returned source edit through the active Monaco model.
  - [x] Let existing Athena LSP did-change tracking rebuild diagnostics and projection state.
  - [x] Refresh the component panel after insertion.
- [x] Add focused verification. (AC: 1, 2)
  - [x] Add backend tests for accepted create-component source-edit handoff.
  - [x] Add frontend tests for the source-edit apply helper or request-shape helper.
  - [x] Run required sequential verification and encoding audit.

## Dev Notes

### Story Intent

- Story `2.2` is the first governed component creation proof for M15.
- The proof is source-backed and repository-backed.
- The panel remains a consumer of backend-generated authoring intent and backend-generated insertion consequences.

### Architecture Guardrails

- Align to AD-84: authoring stays above M8 and does not create a frontend mutation shortcut.
- Align to AD-86: guided authoring consumes governed component knowledge and does not redefine it.
- Align to AD-88: palette and insertion flows stay consumers of shared authoring services.
- Align to AD-90: source, graph, and authoring surfaces refresh through canonical rebuild.
- Align to AD-91: insertion stays preview-first before acceptance.

### File Structure Requirements

- Expected update files likely include:
  - `ide/lsp/src/main/kotlin/...`
  - `kernel/runtime/src/main/kotlin/...`
  - `ide/theia-frontend/src/browser/...`
  - `ide/theia-frontend/scripts/...`
- Explicit non-goals:
  - no drag-router
  - no inspector editing yet
  - no connection authoring yet
  - no broad vendor catalog flow yet

## Story Completion Status

- Status: done
- Completion note: the governed component panel can now preview one create-component intent, accept it through the shared authoring decision path, receive a backend-generated source edit, apply it into the active `.athena` editor, and rely on tracked source rebuild for graph and diagnostics refresh.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaComponentKnowledgeRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest"`
  - `yarn build`
  - `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
