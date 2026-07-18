---
status: ready-for-dev
epic: 1
story: 1.4
title: Define route constraints route facts and quality state
---

# Story 1.4: Define route constraints, route facts, and quality state

As a route-engine maintainer, I want stable route constraint and fact contracts, so that route
generation and rendering cannot diverge.

## Acceptance Criteria

- Route constraints represent orthogonal-only, grid-snap, avoid-node, preferred sides, lane, bundle,
  terminal order, crossing, and label-clearance preferences.
- Route facts carry deterministic ordered segments, source/target anchors, optional label anchors,
  source identity, and quality.
- Route quality can represent satisfied, degraded, and fallback states.
- Tests cover serialization/equality/deterministic sorting for route facts.
- No route fact stores hidden canvas-owned truth.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-8 and AD-9. Quality must be inspectable later.
