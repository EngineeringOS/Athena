---
baseline_commit: c04b3eb
---

# Story 5.2: Reuse Review-First Mutation Preview For Guided Authoring

Status: done

## Story

As a product owner,  
I want guided authoring to remain preview-first before commit,  
so that engineers can inspect what will change before Athena persists it.

## FR Traceability

- FR-12: Athena can preview guided mutations before acceptance
- FR-13: Athena can commit accepted guided mutations into canonical state
- NFR-1: guided authoring introduces no second write path outside M8
- NFR-5: preview and approval remain inspectable and deterministic

## Acceptance Criteria

1. Given a create, update, or connect action is requested, when preview is shown, then the preview can describe what semantic subjects or properties will be added or changed.
2. Given preview semantics are compared with M6 and M8 review patterns, when the product flow is reviewed, then guided authoring aligns with the existing review-first direction rather than introducing opaque direct commit.

## Tasks / Subtasks

- [x] Keep create, update, and connect actions inside the shared preview and decision protocol.
- [x] Extend the graph workbench with preview-state rendering and explicit accept or reject controls for connect flow.
- [x] Reuse the same backend review-first semantics already established for earlier guided authoring stories.

## Story Completion Status

- Status: done
- Completion note: component placement, inspector editing, and graph connection authoring all remain preview-first operations, and no direct frontend commit path was introduced for M15.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
  - `yarn build`

