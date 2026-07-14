---
baseline_commit: c04b3eb
---

# Story 2.3: Keep Placement Synchronized Across Source And Graph

Status: done

## Story

As a product owner,  
I want guided placement to refresh all relevant workbench surfaces,  
so that component insertion feels like one coherent authoring action.

## FR Traceability

- FR-10: Athena can keep source, graph, and guided authoring in sync
- NFR-1: guided authoring introduces no second write path outside M8
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-4: canonical semantic identity remains stronger than graph ids, widget ids, or presentation occurrence ids
- NFR-5: preview and approval remain inspectable and deterministic

## Acceptance Criteria

1. Given a component insertion is accepted, when the runtime completes canonical rebuild, then source, graph, diagnostics, and selection-aware panels refresh coherently.
2. Given the same inserted subject is revealed, when the user moves between workbench surfaces, then the same canonical semantic identity remains selected and traceable.

## Tasks / Subtasks

- [x] Carry inserted-subject selection data through the governed source-edit handoff. (AC: 1, 2)
  - [x] Extend the backend-generated source edit payload with one selection range for the inserted authored block.
  - [x] Keep the handoff canonical and source-backed rather than renderer-owned.
- [x] Apply accepted placement into the active editor and reuse existing semantic selection infrastructure. (AC: 1, 2)
  - [x] Reveal and select the inserted authored block in the active `.athena` editor after the governed source edit is applied.
  - [x] Reuse the shared semantic selection service so graph, inspector, and semantic SCM remain downstream consumers.
- [x] Keep refresh behavior downstream of tracked source. (AC: 1)
  - [x] Let document change propagation continue to drive diagnostics refresh.
  - [x] Let graph refresh continue to rebuild from the tracked source-backed projection session.
- [x] Add focused verification. (AC: 1, 2)
  - [x] Extend backend tests to prove accepted create-component source edits include a selection slice for the inserted device declaration.
  - [x] Run sequential LSP and frontend verification plus encoding audit.

## Dev Notes

### Story Intent

- Story `2.3` closes Epic 2 by making guided placement feel coherent across source and graph.
- The selection anchor remains canonical semantic identity, not one frontend-local node id.
- Graph, inspector, and semantic SCM continue to consume the shared selection service rather than inventing new sync paths.

### Architecture Guardrails

- Align to AD-84: authoring stays above M8 and does not create a frontend mutation shortcut.
- Align to AD-88: workbench surfaces stay consumers of shared authoring services.
- Align to AD-90: source, graph, diagnostics, and selection-aware panels rebuild from canonical tracked source.
- Align to AD-91: placement remains preview-first before acceptance.

### File Structure Requirements

- Updated files:
  - `ide/lsp/src/main/kotlin/...`
  - `ide/lsp/src/test/kotlin/...`
  - `ide/theia-frontend/src/browser/...`
- Explicit non-goals:
  - no graph-local selection truth
  - no renderer-owned insertion state
  - no new mutation path outside the existing authoring and source-backed rebuild flow

## Story Completion Status

- Status: done
- Completion note: accepted guided placement now returns both a governed source edit and an inserted-block selection range; the active editor applies and reveals that range, and the shared semantic selection service carries the resulting canonical semantic identity across source, graph, inspector, and semantic SCM.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest"`
  - `yarn build`
  - `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
