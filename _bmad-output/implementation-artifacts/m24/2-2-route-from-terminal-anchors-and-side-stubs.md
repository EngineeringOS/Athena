---
status: ready-for-dev
epic: 2
story: 2.2
title: Route from terminal anchors and side stubs
---

# Story 2.2: Route from terminal anchors and side stubs

As an IDE reviewer, I want routes to enter and exit through terminal anchors, so that connections
stop looking like generic graph edges.

## Acceptance Criteria

- Routes begin and end at `TerminalAnchorFact` points.
- Short grid-aligned stubs leave the preferred side before joining longer segments.
- The accepted M24 proof has no component-center route endpoints.
- Tests cover input-side, output-side, power, and terminal-block anchor routing.
- Fallback behavior is explicit if a stub cannot be produced.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This is the main visual credibility correction after M23.
