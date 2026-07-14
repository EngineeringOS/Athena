---
baseline_commit: c04b3eb
---

# Story 4.3: Reflect Connection State Across Graph And Inspector

Status: done

## Story

As a product owner,  
I want the workbench to show connection state coherently after guided connect,  
so that the connection flow feels like one semantic operation.

## FR Traceability

- FR-9: Athena can create connections through governed connect intents
- FR-10: Athena can keep source, graph, and guided authoring in sync
- FR-11: Athena can preserve one identity everywhere

## Acceptance Criteria

1. Given one compatible connection exists, when the connected ports are inspected, then the inspector can show connection state for both ends.
2. Given the connection is selected or revealed, when graph and inspector are compared, then both surfaces remain anchored to canonical connection identity and endpoints.

## Tasks / Subtasks

- [x] Project canonical connection state onto selected inspector ports, including connected peer semantic ids and authored peer paths.
- [x] Keep graph connect mode aligned with compatible and incompatible endpoint state.
- [x] Reselect the canonical connection semantic identity after accepted connect so downstream reveal stays coherent.
- [x] Verify graph and inspector connection-state projection with focused model tests and the guided proof flow.

## Story Completion Status

- Status: done
- Completion note: accepted connections now refresh both graph and inspector from canonical connection identity, and the inspector can show connected peer paths for selected ports instead of leaving graph state and detail state disconnected.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
  - `yarn build`
  - `node --test scripts/athena-inspector-model.test.mjs scripts/athena-guided-connection-model.test.mjs`

