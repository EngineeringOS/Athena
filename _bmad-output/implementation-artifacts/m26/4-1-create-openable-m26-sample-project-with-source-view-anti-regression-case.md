---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 4.1: Create Openable M26 Sample Project With Source/View Anti-Regression Case

Status: done

## Story

As a reviewer,
I want an M26 sample project that opens through normal Athena workflow,
so that I can see semantic document projection without inspecting generated artifacts first.

## Acceptance Criteria

1. `examples/m26/sample-project` contains a coherent industrial-control system using only admitted
   `.athena` syntax.
2. The sample includes subjects projected into `Power Distribution`, `Control And PLC Logic`, and
   `Field Wiring And Terminal Transition` sheet views.
3. At least one `.athena` source file contributes subjects to more than one sheet view.
4. At least one sheet view contains subjects not defined by its filename.
5. The sample uses M24 route facts and M25 professional symbols, terminals, and labels.
6. The sample includes at least one cross-view route and at least one repeated or related subject
   reference.

## Tasks / Subtasks

- [x] Create sample workspace metadata (AC: 1)
  - [x] Add M26 `athena.yaml`, `athena.lock`, and README.
- [x] Add admitted Athena source files (AC: 1, 2, 3, 4)
  - [x] Define a coherent industrial-control system.
  - [x] Use file names that are not sheet-view titles.
  - [x] Include at least one file whose subjects belong to multiple projected sheet views.
- [x] Add routing/presentation proof subjects (AC: 5, 6)
  - [x] Include power, PLC/control, terminal transition, and field load paths.
  - [x] Include cross-view and repeated/related-reference candidates.
- [x] Add sample validation coverage (AC: 1, 2, 3, 4, 5, 6)
  - [x] Verify sample files exist and avoid unsupported syntax.
  - [x] Verify accepted sheet-view titles are described without treating source files as sheets.

## Dev Notes

- Do not add new `.athena` syntax.
- Use the existing M25 sample syntax pattern.
- The sample is a workspace proof. `.athena` files are source organization only, not document
  sheet-view boundaries.
- Verification must run sequentially on Windows.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story started after Epic 3 completion.
- `yarn test` in `ide/theia-frontend` passed after adding the M26 sample validation test.

### Completion Notes List

- Added `examples/m26/sample-project` with workspace metadata, README, and two admitted Athena
  source files.
- Included power, breaker, PLC, HMI, terminal, and motor/load proof subjects.
- Added an anti-regression sample shape where source file names are not sheet-view titles and a
  source file contributes subjects that project across document views.
- Added executable frontend validation for sample existence, admitted syntax, accepted sheet-view
  titles, and source/view boundary language.

### File List

- `_bmad-output/implementation-artifacts/m26/4-1-create-openable-m26-sample-project-with-source-view-anti-regression-case.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `examples/m26/sample-project/athena.yaml`
- `examples/m26/sample-project/athena.lock`
- `examples/m26/sample-project/README.md`
- `examples/m26/sample-project/src/01-workspace-semantic-source.athena`
- `examples/m26/sample-project/src/02-field-assets-not-a-sheet.athena`
- `ide/theia-frontend/scripts/athena-m26-sample-project.test.mjs`

## Change Log

- 2026-07-20: Created Story 4.1 from M26 Epic 4.
- 2026-07-20: Added openable M26 sample project and sample validation coverage.
- 2026-07-20: Marked Story 4.1 done after frontend verification.
