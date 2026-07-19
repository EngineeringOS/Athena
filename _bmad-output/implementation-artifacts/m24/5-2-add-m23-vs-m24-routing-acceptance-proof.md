---
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
status: review
epic: 5
story: 5.2
title: Add M23 vs M24 routing acceptance proof
---

# Story 5.2: Add M23-vs-M24 routing acceptance proof

As Aaron, I want explicit comparison evidence, so that I can see what M24 improved and what remains
deferred.

## Acceptance Criteria

- Acceptance docs name visible changes from graph-like edges to terminal-anchor route facts.
- The directional reference image path is included:
  `../../draft/screenshort/coffret_cordons_chauffants.png`.
- The comparison states that full EPLAN/cabinet routing parity is not claimed.
- The comparison is testable in Theia using `examples/m24/sample-project`.
- Boundary checks reject overclaiming language.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/review-rubric.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This story protects the "keep our rhythm" decision.

## Tasks/Subtasks

- [x] Add an M23-vs-M24 routing acceptance comparison document.
- [x] Link the acceptance proof from the M24 example entry point.
- [x] Add boundary regression coverage that rejects M24 overclaiming.

## Dev Agent Record

### Debug Log

- 2026-07-19: Started from Story 5.2 acceptance criteria; scope is documentation and executable boundary proof only.
- 2026-07-19: Verified `yarn --cwd ide/theia-frontend test` and repository encoding audit after adding the acceptance proof.

### Completion Notes

- Added a dedicated routing acceptance proof comparing the M23 layout baseline with M24 terminal-anchor route facts.
- Included the directional reference image path `../../draft/screenshort/coffret_cordons_chauffants.png`.
- Added executable boundary coverage that checks the proof remains sample-testable and does not claim EPLAN, cabinet, physical routing, route-hint syntax, or generic-router authority.

### File List

- docs/usages/m24-routing-acceptance-proof.md
- examples/m24/README.md
- ide/theia-frontend/scripts/athena-m24-routing-acceptance.test.mjs
- _bmad-output/implementation-artifacts/m24/5-2-add-m23-vs-m24-routing-acceptance-proof.md
- _bmad-output/implementation-artifacts/m24/sprint-status.yaml

### Change Log

- 2026-07-19: Started Story 5.2 and added the M24 routing acceptance proof artifact.
- 2026-07-19: Completed Story 5.2 docs, example link, and frontend boundary regression test.
