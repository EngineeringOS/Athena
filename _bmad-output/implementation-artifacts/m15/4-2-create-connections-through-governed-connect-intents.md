---
baseline_commit: c04b3eb
---

# Story 4.2: Create Connections Through Governed Connect Intents

Status: done

## Story

As an engineer,  
I want Athena to create one connection through a guided flow,  
so that graph interaction results in canonical engineering intent rather than a temporary line.

## FR Traceability

- FR-9: Athena can create connections through governed connect intents
- FR-12: Athena can preview guided mutations before acceptance
- FR-13: Athena can commit accepted guided mutations into canonical state
- NFR-1: guided authoring introduces no second write path outside M8
- NFR-5: preview and approval remain inspectable and deterministic

## Acceptance Criteria

1. Given a compatible target is chosen, when the connection action is previewed and accepted, then Athena creates one canonical connection through governed mutation instead of direct graph-edge creation.
2. Given a connection was accepted, when canonical state rebuild completes, then the connection appears in source, graph, and relevant inspector state.

## Tasks / Subtasks

- [x] Extend the frontend authoring protocol with a governed `connect-ports` preview request.
- [x] Route graph connect completion through preview-first accept or reject behavior instead of direct graph mutation.
- [x] Emit the accepted connect consequence as a source-backed `.athena` edit from the LSP layer.
- [x] Add runtime compatibility guards so incompatible port pairs are rejected before canonical mutation.
- [x] Cover the connect path with backend and proof-style tests.

## Story Completion Status

- Status: done
- Completion note: graph-originated connection authoring now flows through `connect-ports` preview, acceptance, source-backed apply, and canonical rebuild, with runtime semantic-port compatibility checks stopping invalid connections before they become canonical state.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
  - `node --test scripts/athena-guided-connection-model.test.mjs`

