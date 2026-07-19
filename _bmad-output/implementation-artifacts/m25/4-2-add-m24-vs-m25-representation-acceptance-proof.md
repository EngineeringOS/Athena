---
baseline_commit: 616271a8721cf9fd6538bf8823eaf57a0392074a
status: ready-for-dev
epic: 4
story: 4.2
title: Add M24-vs-M25 representation acceptance proof
---

# Story 4.2: Add M24-vs-M25 representation acceptance proof

## Story

As a reviewer,
I want a documented comparison from M24 to M25,
So that symbol and terminal improvements are concrete.

## Acceptance Criteria

- Acceptance docs compare generic-box M24 representation against M25 presentation anatomy.
- Docs identify terminal markers, terminal numbers, label anchors, and zero-fallback proof.
- Docs include one documentation-only QElectroTech-inspired anatomy mapping example.
- Docs do not claim QElectroTech import, IEC completeness, or EPLAN parity.

## Tasks/Subtasks

- [x] Create M24-vs-M25 acceptance proof doc.
- [x] Add QElectroTech-inspired mapping example from local reference mirror.
- [x] State deferred library/import boundaries.
- [x] Link proof docs from M25 usage.

## Dev Notes

- Governed by AD-8.

## Dev Agent Record

### Debug Log

- Red: `yarn --cwd ide/theia-frontend test --test-name-pattern "M25 acceptance proof"` failed because `docs/usages/m25-representation-acceptance-proof.md` did not exist.
- Green: the same command passed after adding the M25 representation acceptance proof and usage doc.

### Completion Notes

- Added an M24-vs-M25 proof that describes the move from generic-box route presentation to governed presentation anatomy.
- Included a documentation-only QElectroTech-inspired mapping using the local `Terminale_Spring_Box_3.elmt` reference element.
- Explicitly recorded no QElectroTech import, no IEC completeness, and no EPLAN parity boundaries.
- Linked the comparison from M25 usage.

### File List

- `_bmad-output/implementation-artifacts/m25/4-2-add-m24-vs-m25-representation-acceptance-proof.md`
- `_bmad-output/implementation-artifacts/m25/sprint-status.yaml`
- `docs/usages/m25-proof-usage.md`
- `docs/usages/m25-representation-acceptance-proof.md`
- `ide/theia-frontend/scripts/athena-m25-acceptance-proof.test.mjs`

## Change Log

- 2026-07-19: Added M25 representation acceptance proof, M25 usage link, and doc boundary test.

## Status

done
