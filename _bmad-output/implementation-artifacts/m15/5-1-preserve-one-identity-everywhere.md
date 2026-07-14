---
baseline_commit: c04b3eb
---

# Story 5.1: Preserve One Identity Everywhere

Status: done

## Story

As an engineer,  
I want to reveal the same subject across source, graph, inspector, and semantic SCM,  
so that Athena feels like one governed product rather than disconnected tools.

## FR Traceability

- FR-10: Athena can keep source, graph, and guided authoring in sync
- FR-11: Athena can preserve one identity everywhere
- NFR-4: canonical semantic identity remains stronger than graph ids or widget ids

## Acceptance Criteria

1. Given a component or connection is selected from any M15 surface, when reveal is requested elsewhere, then source, graph, inspector, and semantic SCM resolve to the same canonical semantic identity.
2. Given presentation ids differ from semantic ids, when reveal behavior is reviewed, then canonical semantic identity remains the anchor and downstream ids remain secondary.

## Tasks / Subtasks

- [x] Keep guided placement and connect acceptance payloads anchored on canonical `suggestedSemanticId` values.
- [x] Reuse semantic ids as the stable join key for graph selection, inspector snapshots, and source-backed apply flows.
- [x] Preserve canonical identities through rename, placement, and connect operations in the proof flow.

## Story Completion Status

- Status: done
- Completion note: M15 authoring surfaces now stay joined through canonical semantic ids for components, ports, and connections, including after accepted placement, rename, and connect operations.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`

