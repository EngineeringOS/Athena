---
status: ready-for-dev
epic: 2
story: 2.1
title: Build deterministic orthogonal route engine v0
---

# Story 2.1: Build deterministic orthogonal route engine v0

As a routing engineer, I want a rule-based Athena route engine v0, so that M24 improves route
fidelity without adopting a generic external router.

## Acceptance Criteria

- Route engine v0 consumes route intent, terminal anchors, component bounds, constraints, and layout
  context.
- It emits grid-aligned horizontal and vertical route segments.
- It avoids component-center attachment.
- Repeated runs on the same input produce identical route facts.
- No ELK, Graphviz, yFiles, or external generic router is introduced.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-5. Keep the algorithm simple enough to verify.
