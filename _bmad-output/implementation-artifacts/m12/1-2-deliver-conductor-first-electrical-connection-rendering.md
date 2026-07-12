---
baseline_commit: 179a0a2
---

# Story 1.2: Deliver Conductor-First Electrical Connection Rendering

Status: done

## Story

As an engineer,  
I want electrical connections to read like wiring or cabinet linkage rather than generic graph edges,  
so that the main canvas looks and feels like electrical work instead of a node-edge demo.

## Acceptance Criteria

1. Given one dense electrical view is rendered, when Athena draws connection relationships, then the connection output is visually distinguishable as electrical conductor intent rather than a generic graph line, and labels, markers, or similar connection cues remain visible enough to support inspection.
2. Given routing or overlap quality is inspected on a denser scene, when the rendered result is compared with the current generic baseline, then the scene is materially more readable, and readability improvements do not require renderer-local semantic inference.

## Current Dev Notes

- Consume the typed M12 anchor and routing-corridor contracts published in Story `1.1`.
- Keep semantic endpoint truth in kernel/runtime/LSP and render only downstream conductor appearance in the JS workbench path.
- TS implementation should stay modularized by role instead of growing existing large files.

## Completion Notes

- Split `integrations/graph-glsp` types into projection-source and graph-model files instead of growing one mixed TS file.
- Split Theia graph workbench edge rendering into a dedicated `athena-graph-workbench-edge-layer.tsx` and widget-local state types into `athena-graph-workbench-types.ts`.
- Upgraded adapter-owned graph edges to carry corridor-driven route points, endpoint metadata, and conductor-style rendering hints.
- Replaced generic single-line edge rendering with conductor-style SVG paths, casing, bend markers, and endpoint terminals in the workbench canvas.
- Focused JS/TS verification passed.
- Real desktop verification passed through `ide/yarn build` followed by `ide/yarn start:smoke`, confirming the Theia product still boots with the conductor-rendering path active on Java 25.

## Change Log

- 2026-07-12: Story created and implementation started on top of the M12 anchor/corridor contract layer.
- 2026-07-12: Modularized the touched TS code paths and implemented conductor-style edge rendering over governed routing corridors.
- 2026-07-12: Verified the real desktop launch path with `yarn build` and `yarn start:smoke`; story marked done.
