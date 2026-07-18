---
status: ready-for-dev
epic: 3
story: 3.2
title: Integrate route facts into presentation snapshots
---

# Story 3.2: Integrate route facts into presentation snapshots

As a renderer developer, I want presentation snapshots to carry route facts, so that Theia can
render routed wires without recalculating meaning.

## Acceptance Criteria

- Presentation/projection snapshots expose route facts to Graphical View.
- Existing node/edge identity remains compatible.
- M23 layout constraints can provide context without being replaced.
- Renderer-facing payloads include route segments, anchors, labels, and quality state.
- Tests prove old edge rendering does not remain the accepted route path for M24 proof cases.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Keep route facts as downstream projection data. Do not move route solving into Theia.
