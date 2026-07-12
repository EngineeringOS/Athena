---
baseline_commit: 179a0a2
---

# Story 1.3: Preserve Stable Endpoint Anchoring And Selection Coherence

Status: done

## Story

As an engineer,  
I want rendered electrical connections to terminate at the intended endpoints,  
so that selection, reveal, and inspection remain trustworthy under denser views.

## Acceptance Criteria

1. Given a rendered connection originates from governed projection anchors, when Athena draws and later selects that connection, then the rendered endpoints remain traceable to the intended subjects, ports, or terminals, and endpoint selection does not degrade into approximate node-center behavior only.
2. Given a rendered endpoint is inspected through selection or reveal, when Athena resolves the subject, then the resolved identity remains canonical and coherent with runtime, review, and knowledge paths, and the renderer does not introduce a parallel endpoint-identity model.

## Current Dev Notes

- Build on Story `1.1` anchor contracts and Story `1.2` conductor-first rendering without creating a frontend-owned endpoint identity model.
- Keep the renderer, graph adapter, and workbench selection flow explicit about anchor aliases versus canonical semantic subject identity.
- Keep touched TS code modularized by role instead of growing single-file frontend blobs.

## Completion Notes

- Added explicit endpoint and anchor alias resolution in `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`.
- Extended workbench edge presentation with terminal metadata so selection can land on canonical ports instead of approximate line centers only.
- Updated the graph workbench overlay to surface endpoint-alias resolution for the active canonical selection.
- Verified through `yarn test` in `ide/theia-frontend` and `yarn test` in `integrations/graph-glsp`.

## Change Log

- 2026-07-12: Story created from `epics-M12-2026-07-12.md` after Story 1.2 desktop verification passed.
- 2026-07-12: Completed with governed endpoint alias resolution, port-terminal selection, and refreshed workbench tests.
