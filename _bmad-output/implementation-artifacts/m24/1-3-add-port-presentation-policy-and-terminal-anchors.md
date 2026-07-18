---
status: ready-for-dev
epic: 1
story: 1.3
title: Add port presentation policy and terminal anchors
---

# Story 1.3: Add port presentation policy and terminal anchors

As a layout/routing engineer, I want port sides and terminal anchors derived from policy, so that
renderer code does not hardcode universal input/output side rules.

## Acceptance Criteria

- `PortPresentationPolicy` selects preferred sides for input, output, power, ground, bidirectional,
  and terminal-block ports in the M24 sample.
- `TerminalAnchorFact` carries subject, port, occurrence, side, grid point, and policy source.
- Component centers are not used as route endpoints in the accepted M24 proof.
- Tests prove side selection is policy-owned and deterministic.
- Renderer code receives terminal anchors as facts and does not infer side meaning.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-3 and AD-4. Defaults may be simple, but must not be renderer-owned.
