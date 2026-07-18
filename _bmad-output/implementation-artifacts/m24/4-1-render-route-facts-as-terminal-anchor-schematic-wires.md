---
status: ready-for-dev
epic: 4
story: 4.1
title: Render route facts as terminal anchor schematic wires
---

# Story 4.1: Render route facts as terminal-anchor schematic wires

As a reviewer, I want Theia to render route facts from terminal anchors, so that wires visually
attach to ports/terminals instead of component centers.

## Acceptance Criteria

- Graphical View renders wires from route facts when route facts are present.
- Wires begin and end at terminal anchors.
- Route segments are orthogonal and grid-aligned.
- The accepted M24 proof has no renderer-side center-to-center fallback.
- DOM/canvas smoke checks can distinguish terminal-anchor routes from old graph-edge routes.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This is the user-visible heart of M24. Do not finish it with only model tests.
