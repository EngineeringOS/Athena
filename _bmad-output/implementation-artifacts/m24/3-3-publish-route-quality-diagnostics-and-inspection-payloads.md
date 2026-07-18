---
status: ready-for-dev
epic: 3
story: 3.3
title: Publish route quality diagnostics and inspection payloads
---

# Story 3.3: Publish route quality diagnostics and inspection payloads

As an IDE user, I want degraded or fallback routes to be explainable, so that routing limits are
visible instead of silently pretending to be professional.

## Acceptance Criteria

- Degraded or fallback route facts expose affected connection identity.
- Diagnostics or inspection payloads name failed constraint families where available.
- Satisfied routes remain clean and do not spam Problems.
- Rendering continues when a route has degraded or fallback quality.
- Tests cover satisfied and fallback route quality states.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-8. Fallback visibility is required for honest product behavior.
