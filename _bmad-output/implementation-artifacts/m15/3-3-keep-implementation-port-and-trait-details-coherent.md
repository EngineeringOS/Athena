---
baseline_commit: c04b3eb
---

# Story 3.3: Keep Implementation, Port, And Trait Details Coherent

Status: done

## Story

As an architecture owner,  
I want inspector detail rendering to stay consistent with active component knowledge,  
so that the workbench does not become a second catalog model.

## FR Traceability

- FR-5: Athena can publish a component inspector from canonical semantic identity
- FR-10: Athena can keep source, graph, and guided authoring in sync
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-3: ports, traits, and implementation choices derive from active component knowledge rather than frontend hardcoding
- NFR-4: canonical semantic identity remains stronger than widget ids or graph ids

## Acceptance Criteria

1. Given the inspector shows implementation options, semantic ports, and minimal physical traits, when the selected component is reviewed, then those details derive from active component knowledge rather than duplicated frontend truth.
2. Given the selected implementation changes, when the inspector refreshes, then the shown ports, port connection state, and minimal physical traits stay coherent with the resolved component knowledge.

## Tasks / Subtasks

- [x] Publish authored port properties through semantic inspection so the frontend can reconcile canonical inspection and active component knowledge.
- [x] Add a shared connection-state model that derives compatible targets and connected endpoint state from canonical semantic inspection plus active component knowledge.
- [x] Update inspector rendering to show implementation-owned details and connected peer paths from canonical state.
- [x] Verify the coherence slice through focused frontend model tests and the LSP-backed guided authoring proof.

## Dev Notes

### Story Intent

- Story `3.3` closes the inspector coherence gap left by story `3.2`.
- The inspector remains downstream of canonical semantic inspection and active component knowledge.
- No frontend-local port catalog or trait catalog was introduced.

### Architecture Guardrails

- Align to AD-86: implementation and part choices remain owned by M14 component knowledge.
- Align to AD-88: inspector state uses shared models rather than widget-local duplication.
- Align to AD-92: semantic identity remains the stable join key across source, graph, and inspector.

## Story Completion Status

- Status: done
- Completion note: the inspector now derives implementation choices, semantic-port details, connection state, and physical-trait context from the active component-knowledge session plus canonical semantic inspection, and no longer needs a duplicated frontend catalog model to explain what a selected component means.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
  - `yarn build`
  - `node --test scripts/athena-inspector-model.test.mjs scripts/athena-guided-connection-model.test.mjs`

