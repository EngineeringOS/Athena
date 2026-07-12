---
baseline_commit: 179a0a2
---

# Story 2.1: Derive Reliable Scene Bounds And Fit-To-Viewport Behavior

Status: done

## Story

As an engineer,  
I want fit-to-viewport to land on the actual electrical scene reliably,  
so that I do not have to hunt the graph manually after basic viewport actions.

## Completion Notes

- Hardened `buildAthenaGraphWorkbenchModel` so scene bounds are derived from normalized graph payloads and routed edge points.
- Added runtime-safe fallback normalization for optional diagram arrays to prevent `undefined.map` style frontend crashes.
- Added viewport-fit unit coverage plus sparse-payload coverage in `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`.

## Change Log

- 2026-07-12: Completed during M12 viewport hardening pass.
