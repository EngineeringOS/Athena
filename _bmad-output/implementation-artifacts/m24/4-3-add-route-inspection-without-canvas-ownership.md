---
status: ready-for-dev
epic: 4
story: 4.3
title: Add route inspection without canvas ownership
---

# Story 4.3: Add route inspection without canvas ownership

As an IDE user, I want to inspect a route's source connection and quality, so that rendered wires
remain traceable to Athena semantics.

## Acceptance Criteria

- Selecting or inspecting a rendered route exposes source connection identity.
- Route inspection shows source/target ports, route quality, and policy summary where available.
- Inspection payloads come from route facts, not DOM geometry.
- No hidden route coordinates are persisted from the canvas.
- Rejected or unavailable route inspection does not break normal selection behavior.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-10. This is inspectable projection, not route editing.
