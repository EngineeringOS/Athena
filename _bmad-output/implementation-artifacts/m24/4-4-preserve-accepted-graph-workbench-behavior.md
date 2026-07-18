---
status: ready-for-dev
epic: 4
story: 4.4
title: Preserve accepted Graph Workbench behavior
---

# Story 4.4: Preserve accepted Graph Workbench behavior

As Aaron, I want routing improvements without UI regressions, so that M24 does not reopen the
M20-M23 canvas issues.

## Acceptance Criteria

- Grid remains the coordinate surface.
- Floating controls remain transparent overlays.
- Top information popover behavior remains unchanged.
- Whitespace click closes the information popover.
- Outline navigation keeps the same `.athena` editor tab.
- Active-source Graphical View projection remains correct.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Run the accepted M20-M23 UI regression checks after route rendering changes.
