---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 4.2: Publish M26 Acceptance Proof And M25 Comparison Documentation

Status: done

## Story

As a product reviewer,
I want documentation that explains the M26 proof path and how it differs from M25,
so that I can validate the milestone against the intended architecture.

## Acceptance Criteria

1. `docs/usages/m26-proof-usage.md` explains how to open the sample in Theia, select sheet views,
   inspect compact markers, and follow references.
2. Documentation states the accepted sheet-view titles, document projection policy id, occurrence
   identity recipe, and anti-regression source/view proof.
3. Documentation compares M25 single-sheet presentation with M26 semantic document projection.
4. Documentation states that pages, documents, source-file names, and Theia canvas state are not
   source truth.
5. Documentation avoids unsupported `.athena` syntax.

## Tasks / Subtasks

- [x] Publish usage guide (AC: 1, 2, 4, 5)
  - [x] Include sample path, start command, sheet-view selector path, compact marker inspection, and
        reference navigation.
  - [x] State policy id and occurrence identity recipe.
- [x] Publish M25-vs-M26 comparison (AC: 3, 4)
  - [x] Explain the move from professional single-sheet presentation to semantic document
        projection.
  - [x] Reaffirm `.athena` source and compiler/runtime snapshots as source truth.
- [x] Add documentation coverage (AC: 1, 2, 3, 4, 5)
  - [x] Verify required terms and unsupported syntax exclusions.

## Dev Notes

- Do not document syntax Athena does not implement.
- Avoid the term `folio`; M26 uses semantic document projection.
- Verification must run sequentially on Windows.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story started after Story 4.1 sample validation.
- `yarn test` in `ide/theia-frontend` passed after adding M26 usage documentation coverage.

### Completion Notes List

- Published `docs/usages/m26-proof-usage.md` with the sample path, Theia command, accepted
  sheet-view titles, compact marker navigation path, policy id, and occurrence identity recipe.
- Documented the M25-to-M26 progression from professional single-sheet presentation to semantic
  document projection.
- Reaffirmed `.athena` source plus compiler/runtime semantic snapshots as engineering truth and
  avoided unsupported source syntax.

### File List

- `_bmad-output/implementation-artifacts/m26/4-2-publish-m26-acceptance-proof-and-m25-comparison-documentation.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `docs/usages/m26-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m26-sample-project.test.mjs`

## Change Log

- 2026-07-20: Created Story 4.2 from M26 Epic 4.
- 2026-07-20: Published M26 proof usage and M25 comparison documentation.
- 2026-07-20: Marked Story 4.2 done after frontend documentation verification.
