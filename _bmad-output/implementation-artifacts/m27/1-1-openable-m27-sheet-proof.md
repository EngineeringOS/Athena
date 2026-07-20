---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 1.1: Openable M27 Sheet Proof

Status: done

## Story

As an engineer,
I want to open the M27 sample project through the Athena Theia IDE,
so that I can verify the milestone has a real, admitted, openable proof target before the visual
sheet-surface work lands in later stories.

## Acceptance Criteria

1. `examples/m27/sample-project` exists and opens through the normal Athena Theia graphical-view
   workflow.
2. The sample uses admitted `.athena` syntax only.
3. The sample contains semantic content sufficient to exercise later M27 sheet-surface stories:
   at least one power path, one control path, one terminal transition, and one repeated or
   cross-view reference candidate.
4. The proof path uses the active Theia frontend only and does not depend on deprecated
   desktop-viewer, Compose, or KMP frontend modules.
5. Reopening or rebuilding the sample produces deterministic project identity and stable proof
   launch behavior.
6. The sample does not persist hidden canvas truth as engineering source.

## Tasks / Subtasks

- [x] Create the M27 sample project workspace skeleton (AC: 1, 2, 4, 5, 6)
  - [x] Add `examples/m27/sample-project` workspace metadata and README.
  - [x] Add the minimal admitted `.athena` source files needed for the openable proof.
  - [x] Keep source-file names semantic and not sheet/page titles.
- [x] Add proof subjects that later stories can project into professional sheet visuals (AC: 3)
  - [x] Include power, control, terminal transition, and repeated-reference candidates.
  - [x] Keep the sample semantically coherent and deterministic.
- [x] Add openability validation coverage in the Theia proof path (AC: 1, 2, 4, 5, 6)
  - [x] Verify the sample opens through the Graphical View path.
  - [x] Verify no deprecated frontend module is required.
  - [x] Verify admitted syntax and stable proof launch behavior.

## Dev Notes

- Story 1.1 is a proof-surface story, not the final professional sheet-fidelity story.
- Do not implement the full M27 sheet frame, title block, dense grid, or compact label polish here;
  those belong to Stories 1.2 and 1.3.
- Use the existing admitted Athena syntax patterns from M25/M26. Do not add new `.athena` syntax.
- Keep the sample semantically coherent so later stories can project richer visuals without
  rewriting the proof project.
- The active frontend is Theia only. Do not touch `apps:desktop-viewer`, Compose, or deprecated KMP
  frontend scope.
- The sample must not become a new source-of-truth mechanism for canvas state.
- Verification must run sequentially on Windows.

### Project Structure Notes

- Expected new sample root: `examples/m27/sample-project`
- Expected source folder: `examples/m27/sample-project/src`
- Expected proof or validation hook: `ide/theia-frontend`

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m27/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m27/epics.md`
- Previous pattern: `_bmad-output/implementation-artifacts/m26/4-1-create-openable-m26-sample-project-with-source-view-anti-regression-case.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `yarn --cwd ide/theia-frontend test` passed after adding the M27 sample regression test.
- `yarn --cwd ide start:smoke:m27` passed and reported an openable M27 workspace with graph
  workbench proof.

### Completion Notes List

- Added `examples/m27/sample-project` with workspace metadata, README, and two admitted Athena
  source files.
- Included power, control, terminal transition, and cross-file reference candidate subjects without
  duplicating authored semantic identities across files.
- Added `ide/theia-product/scripts/verify-athena-m27-sample-project.js` and wired `start:m27` and
  `start:smoke:m27` into the IDE product scripts.
- Added `ide/theia-frontend/scripts/athena-m27-sample-project.test.mjs` to keep the M27 proof
  workspace and smoke wiring from drifting.
- Verified the Theia product smoke for M27 opens the sample workspace and reports graph workbench
  proof, route proof, representation proof, and document projection proof.

### File List

- `_bmad-output/implementation-artifacts/m27/1-1-openable-m27-sheet-proof.md`
- `_bmad-output/implementation-artifacts/m27/epics.md`
- `_bmad-output/implementation-artifacts/m27/sprint-status.yaml`
- `examples/m27/sample-project/athena.yaml`
- `examples/m27/sample-project/athena.lock`
- `examples/m27/sample-project/README.md`
- `examples/m27/sample-project/src/01-workspace-semantic-source.athena`
- `examples/m27/sample-project/src/02-field-assets-not-a-sheet.athena`
- `ide/package.json`
- `ide/theia-frontend/scripts/athena-m27-sample-project.test.mjs`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m27-sample-project.js`

## Change Log

- 2026-07-20: Created Story 1.1 as the first M27 proof-surface story.
- 2026-07-20: Implemented the M27 openable sample project, smoke hook, and frontend validation.
