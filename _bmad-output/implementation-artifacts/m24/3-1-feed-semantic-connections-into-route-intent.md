---
status: ready-for-dev
epic: 3
story: 3.1
title: Feed semantic connections into route intent
---

# Story 3.1: Feed semantic connections into route intent

As a compiler engineer, I want projection to provide route intent from semantic connections, so that
route generation follows Athena source truth.

## Acceptance Criteria

- Projection emits route intent with connection identity, port identities, view/sheet context, and
  layout context.
- Route intent is derived from compiled source semantics, not renderer positions.
- Route intent is sorted deterministically.
- Existing connection identity remains compatible with selection and reveal.
- Tests cover PLC-HMI, PLC-terminal-load, and 24V power/protection route intent.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-2. Existing `.athena` source should be enough for M24 route intent.
