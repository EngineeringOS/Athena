---
baseline_commit: c04b3eb
---

# Story 3.1: Publish A Canonical Inspector Snapshot

Status: done

## Story

As an engineer,  
I want Athena to show component details in an inspector,  
so that I can understand and edit the selected component through one governed surface.

## FR Traceability

- FR-5: Athena can publish a component inspector from canonical semantic identity
- FR-10: Athena can keep source, graph, and guided authoring in sync
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-3: available components, parts, ports, and traits derive from active M14 component knowledge rather than frontend hardcoding
- NFR-4: canonical semantic identity remains stronger than graph ids, widget ids, or presentation occurrence ids

## Acceptance Criteria

1. Given a component is selected in the graph or source, when the inspector is opened, then the inspector shows component name, engineering concept, vendor implementation, semantic ports, and minimal physical traits.
2. Given the selected subject is compared across graph and inspector, when identity is reviewed, then the inspector binds to canonical semantic identity rather than to frontend-local node ids.

## Tasks / Subtasks

- [x] Reuse canonical selection and knowledge payloads to derive one selected-component snapshot. (AC: 1, 2)
- [x] Keep the selected-component snapshot logic outside the widget in one testable frontend model helper. (AC: 1, 2)
- [x] Extend the inspector widget to render concept, implementation, ports, and physical traits for the selected component. (AC: 1)
- [x] Add focused verification for the selected-component snapshot mapping. (AC: 1, 2)

## Dev Notes

### Story Intent

- Story `3.1` should upgrade the current generic semantic inspection view into the first governed authoring inspector surface.
- The inspector remains downstream of canonical semantic identity and active component knowledge.
- The story does not yet edit properties; it publishes the first selected-component snapshot only.

### Architecture Guardrails

- Align to AD-86: the inspector consumes component knowledge and does not redefine it.
- Align to AD-88: inspector behavior stays a thin consumer of shared services.
- Align to AD-90: inspector refresh remains downstream of canonical tracked-source rebuild.
- Align to AD-92: canonical semantic identity remains the only stable anchor across source, graph, and inspector.

## Story Completion Status

- Status: done
- Completion note: the semantic inspection workbench now derives one selected-component inspector snapshot from canonical semantic selection plus active component knowledge, and renders concept, vendor implementation, semantic ports, and minimal physical traits without introducing a second frontend catalog model.
- Verification:
  - `yarn build`
  - `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs scripts/athena-inspector-model.test.mjs`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
