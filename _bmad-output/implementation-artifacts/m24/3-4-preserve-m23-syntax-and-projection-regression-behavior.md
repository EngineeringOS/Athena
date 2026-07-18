---
status: ready-for-dev
epic: 3
story: 3.4
title: Preserve M23 syntax and projection regression behavior
---

# Story 3.4: Preserve M23 syntax and projection regression behavior

As an Athena maintainer, I want routing work to preserve M23 language admission, so that M24 does
not break layout blocks or active-source projection.

## Acceptance Criteria

- M23 layout-block sample and parser fixtures still pass.
- ANTLR4, Tree-sitter, compiler, LSP, and Theia still accept the M23 sample.
- Active-source Graphical View projection still uses the currently opened `.athena` file.
- No route feature introduces new source syntax without dual-parser parity.
- Product smoke rebuilds or uses the current installed LSP host before making IDE claims.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This story protects the M23 root-cause lesson: source tests alone do not prove the Theia product path.
