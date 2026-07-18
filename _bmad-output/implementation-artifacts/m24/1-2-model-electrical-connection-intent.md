---
status: ready-for-dev
epic: 1
story: 1.2
title: Model electrical connection intent
---

# Story 1.2: Model electrical connection intent

As a compiler engineer, I want semantic `connect` facts to classify electrical connection intent, so
that routing decisions can use engineering meaning instead of only topology.

## Acceptance Criteria

- Electrical connection intent can classify at least control, power, terminal transition, and load
  connection classes for the M24 sample.
- Intent carries canonical connection, source port, target port, source subject, target subject, and
  source span identity where available.
- Unknown or unsupported classes degrade explicitly instead of crashing.
- Tests cover direction, signal, and terminal-transition mapping.
- No renderer or Theia code participates in classification.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-2. Initial classification should be modest and deterministic.
