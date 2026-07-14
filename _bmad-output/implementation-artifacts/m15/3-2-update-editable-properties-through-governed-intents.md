---
baseline_commit: c04b3eb
---

# Story 3.2: Update Editable Properties Through Governed Intents

Status: done

## Story

As an engineer,  
I want Athena's inspector to update governed component properties through previewed intents,  
so that I can change canonical engineering state without editing frontend-local state directly.

## FR Traceability

- FR-6: Athena can update governed component properties from the inspector
- FR-10: Athena can keep source, graph, inspector, and diagnostics synchronized after accepted changes
- NFR-2: workbench editing remains downstream of platform-owned authoring and mutation contracts
- NFR-3: implementation choices remain sourced from active component knowledge rather than frontend hardcoding
- NFR-4: canonical semantic identity remains stronger than widget ids or graph node ids

## Acceptance Criteria

1. Given one governed component is selected, when the engineer edits supported inspector fields and requests a preview, then Athena emits one inspectable `update-component-properties` preview through the shared authoring contract.
2. Given the preview is accepted, when Athena applies the returned source edit, then the tracked source, semantic inspection, and downstream graph rebuild stay coherent, including component rename references.
3. Given implementation choices are shown in the inspector, when the engineer selects an alternative implementation, then the request uses active component knowledge rather than a frontend-local vendor catalog.

## Tasks / Subtasks

- [x] Publish structured authored component properties through semantic inspection so inspector editing can start from canonical authored values. (AC: 1)
- [x] Add the governed inspector draft and request helpers outside the widget so editable state and changed-property detection stay testable. (AC: 1, 3)
- [x] Extend the inspector widget to preview, accept, reject, and reset governed property updates through the shared authoring flow. (AC: 1, 2)
- [x] Make accepted rename edits rewrite dependent `port` and `connect` owner references so the tracked document stays semantically coherent after apply. (AC: 2)
- [x] Expand the Siemens proof component catalog with a second PLC CPU implementation so implementation selection is a real governed choice. (AC: 3)
- [x] Add focused backend and frontend verification for the update-intent path. (AC: 1, 2, 3)

## Dev Notes

### Story Intent

- Story `3.2` is the first governed inspector editing slice in M15.
- The inspector remains a consumer of M8/M15 authoring contracts rather than a direct mutation surface.
- Accepted edits remain source-backed for this proof slice.

### Architecture Guardrails

- Align to AD-86: component and implementation identity remain owned by M14 component knowledge.
- Align to AD-88: inspector operations stay thin and reuse shared authoring services.
- Align to AD-90: accepted edits rebuild tracked source first, then all downstream semantic consumers refresh.
- Align to AD-92: canonical semantic identity remains the common anchor after rename and reselection.

## Story Completion Status

- Status: done
- Completion note: the semantic inspection workbench now exposes an editable inspector draft for governed component properties, emits `update-component-properties` previews through the shared authoring protocol, applies accepted source-backed edits back into the tracked `.athena` document, and rewrites dependent authored references when a component name changes so downstream semantic rebuilds remain coherent.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
  - `yarn build`
  - `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs scripts/athena-inspector-model.test.mjs`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
