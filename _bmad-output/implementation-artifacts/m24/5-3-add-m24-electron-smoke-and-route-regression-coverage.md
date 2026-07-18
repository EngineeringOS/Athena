---
status: ready-for-dev
epic: 5
story: 5.3
title: Add M24 Electron smoke and route regression coverage
---

# Story 5.3: Add M24 Electron smoke and route regression coverage

As an Athena maintainer, I want product-path smoke tests for M24, so that route rendering does not
pass only in unit tests.

## Acceptance Criteria

- Product smoke opens `examples/m24/sample-project` in Theia.
- Smoke proves terminal-anchor route rendering exists.
- Smoke proves no center-to-center fallback is used in the accepted proof.
- Failures include actionable route/projection state.
- The installed LSP host is rebuilt or confirmed current before product smoke claims success.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Carry forward the M23 stale installed-LSP lesson.
