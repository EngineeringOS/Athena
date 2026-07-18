---
status: ready-for-dev
epic: 5
story: 5.1
title: Create the openable M24 routing sample project
---

# Story 5.1: Create the openable M24 routing sample project

As a product reviewer, I want a real M24 sample project, so that I can test routing fidelity through
the IDE.

## Acceptance Criteria

- `examples/m24/sample-project` exists with real `.athena` files.
- The sample includes PLC-HMI, PLC-terminal-load, 24V power/protection, and terminal-strip route
  cases.
- The sample opens without false syntax diagnostics.
- Graphical View projects the active M24 source.
- The sample is documented for customer-facing proof and does not require reading `.mjs` files.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`

## Notes

The sample is the product proof. Do not create docs that claim behavior the sample cannot show.
