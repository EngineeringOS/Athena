---
baseline_commit: 179a0a2
---

# Story 3.2: Make Electrical Context Panels Coherent With Canvas-First Operation

Status: done

## Story

As an engineer,  
I want support panels and electrical context surfaces to stay coherent with the active canvas,  
so that the workbench feels like one operator surface instead of a set of unrelated demo panes.

## Completion Notes

- Preserved one canonical semantic selection path across graph, source, inspection, repository, and semantic SCM surfaces.
- Added related-subject reveal actions in the graph overlay so panel refresh still flows from canonical selection changes.
- Verified no renderer-local subject identity was introduced for endpoint, repeated-reference, or related-subject reveal.

## Change Log

- 2026-07-12: Completed during M12 related-navigation and panel-coherence pass.
