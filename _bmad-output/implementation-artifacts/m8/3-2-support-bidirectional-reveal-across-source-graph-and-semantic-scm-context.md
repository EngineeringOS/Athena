---
baseline_commit: d8e1c6163b7edd8895e8b8fe182519f0fbf54b53
---

# Story 3.2: Support Bidirectional Reveal Across Source, Graph, And Semantic SCM Context

Status: done

## Story

As an engineer or reviewer,  
I want source, graph, and semantic SCM views to reveal the same accepted change coherently,  
so that I can move between representations without identity drift.

## FR Traceability

- FR-5: produce unified semantic review facts for accepted mutations
- FR-8: preserve renderer-neutral mutation semantics
- NFR-1: meaningful changes must route through one Athena-owned mutation path
- NFR-2: canonical engineering meaning remains upstream of any renderer or editor client
- NFR-4: command intents, mutation outcomes, rejection paths, and review facts remain inspectable
- NFR-5: graph-originated and source-originated mutations must share one semantic review and history vocabulary

## Acceptance Criteria

1. Given source, graph, and semantic SCM surfaces are open together, when the user reveals a changed subject from any one of those surfaces, then Athena resolves the corresponding graph, source, and review context through canonical semantic identity and governed projection references, and the same repository and semantic state yield the same reveal outcome.
2. Given graph-local ids and projection-local refs exist, when reveal anchoring is reviewed, then those ids remain downstream aliases only, and canonical semantic identity remains the authority for cross-surface anchoring.

## Tasks / Subtasks

- [x] Let the source editor participate directly in semantic reveal through canonical source ranges. (AC: 1)
- [x] Let SCM review and commit entries become explicit reveal origins through semantic identity. (AC: 1, 2)
- [x] Let the graph workbench resolve semantic selection into the correct governed projection view. (AC: 1, 2)
- [x] Add focused frontend tests for reveal anchoring and selection resolution. (AC: 1, 2)
- [x] Run sequential verification on Windows with Java 25. (AC: 1, 2)

## Dev Notes

### Story Intent

- Story `3.2` closes the navigation gap left after Story `3.1`: accepted mutation facts were unified semantically, but reveal still depended on whichever surface the user happened to be looking at.
- The proof target stays narrow. This story does not introduce a new navigation backend; it reuses canonical semantic ids plus governed projection views.

### Technical Notes

- The source editor now acts as a reveal origin through semantic source ranges published by the existing semantic inspection payload.
- Semantic SCM review, enrichment, and commit entries now become clickable reveal origins when they carry canonical subject identity or fact-reference subject identity.
- The graph workbench now resolves a semantic selection into the correct active governed projection view instead of assuming the current view already contains the subject.

### Testing Requirements

- Verification commands:
  - `yarn --cwd ide/theia-frontend test`
  - `yarn --cwd integrations/graph-glsp test`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:compileKotlin :kernel:runtime:compileKotlin"`

## Story Completion Status

- Status: done
- Completion note: Source editor selection, graph selection, and semantic SCM entries now converge on one canonical semantic-id reveal path, and the graph workbench can switch governed views to reveal the selected subject instead of dropping context when the current view does not contain it.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `yarn --cwd ide/theia-frontend test`
- `yarn --cwd integrations/graph-glsp test`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:compileKotlin :kernel:runtime:compileKotlin"`

### Completion Notes List

- Extended the semantic selection model with source-range resolution, SCM reveal-target selection, and graph semantic-id containment helpers.
- Updated the Theia semantic selection service so editor selection changes can become canonical semantic reveal events without creating a second source-owned selection model.
- Updated the graph adapter and graph workbench so semantic selection can reveal across governed projection views when the current graph view does not contain the selected semantic subject.
- Updated semantic SCM review, enrichment, and commit entries so they can reveal source and graph context through the existing semantic selection path.
