---
status: ready-for-dev
epic: 2
story: 2.3
title: Add lane assignment and component avoidance
---

# Story 2.3: Add lane assignment and component avoidance

As an electrical engineer, I want long routes to use lanes and avoid component bodies, so that the
sheet remains ordered and readable.

## Acceptance Criteria

- Long route segments can use horizontal or vertical routing lanes.
- Routes avoid obvious component body overlap in the accepted M24 sample.
- Route lane assignment is deterministic.
- Fallback quality is emitted when avoidance cannot be satisfied.
- Tests cover at least one obstacle and one clear lane path.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This is not a general router. Implement the smallest rule-based behavior that proves the M24 sample.
