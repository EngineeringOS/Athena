---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 3.3: Validate The First Graphical Technology Path And Publish The M7 Proof Corpus

Status: review

## Story

As an architecture owner,  
I want Athena to validate the first graphical technology path and publish proof artifacts,  
so that M7 ends with a credible implementation direction and a repeatable proof corpus instead of an unresolved framework debate.

## Acceptance Criteria

1. Given the first graph adapter and renderer proof are implemented, when Athena evaluates the chosen graphical technology path, then it compares the implementation against M7 constraints such as JVM-first authority, Theia product-shell fit, translation-only adapters, inspectability, and deterministic refresh, and the decision is recorded clearly enough for later milestone planning.
2. Given M7 is reviewed as a milestone proof, when users inspect the proof corpus, then Athena provides examples, runnable verification, or equivalent milestone artifacts that demonstrate the relationship-forward renderer and product integration path, and the milestone remains focused on graphical projection rather than broad UX-polish or unrestricted editing scope.

## Tasks / Subtasks

- [x] Record the M7 graphical technology decision against the implemented seams. (AC: 1)
  - [x] Add an M7 decision record under the milestone architecture folder.
  - [x] Evaluate the current path against JVM-first authority, Theia fit, translation-only adapters, inspectability, and deterministic refresh.
- [x] Publish a dedicated M7 proof-usage guide. (AC: 1, 2)
  - [x] Document the graph adapter proof.
  - [x] Document the extension-owned renderer mapping proof.
  - [x] Document the graph-first workbench and interactive operator path.
- [x] Publish the M7 proof corpus entry under `examples/`. (AC: 2)
  - [x] Add `examples/m7/` documentation that points at the real governed repository fixture used by the current graphical proof.
  - [x] Keep the corpus focused on the current milestone instead of inventing a fake graphical-only repository shape.
- [x] Add one repeatable M7 frontend verification entrypoint. (AC: 2)
  - [x] Add `yarn --cwd ide verify:m7`.
  - [x] Validate the new entrypoint on the current workstation.
- [x] Update the milestone indexes that should point at M7. (AC: 2)
  - [x] Update the examples index.
  - [x] Update the workspace summary reading path.
  - [x] Refresh the graph adapter README and its Chinese counterpart to reflect the final M7 position.

## Dev Notes

### Story Intent

- Story `3.3` closes M7 as an architecture-proof milestone.
- The main work here is not new semantic authority or renderer behavior.
- The missing value was packaging the implemented path into a repeatable proof set and freezing the technology decision against the actual on-disk architecture.

### Decision Summary

Athena keeps the current graphical path as:

- runtime-owned projection authority
- `ide/lsp` transport
- translation-only `integrations/graph-glsp`
- Athena-owned Theia rendering and workbench composition

M7 does **not** commit to full GLSP runtime adoption as its live editor core.

### Verification

- `cmd /c "call java25 && echo shutdown | ide\\lsp\\build\\install\\athena-lsp-host\\bin\\athena-lsp-host.bat --repository-root examples\\m4\\open-repository-proof"`
- `yarn --cwd ide verify:m7`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Completion Notes List

- Added a dedicated M7 graphical technology decision record under the M7 architecture artifact folder.
- Published `docs/usages/m7-proof-usage.md` covering the graph adapter, extension-owned mapping path, graph-first workbench, and recorded technology decision.
- Added `examples/m7/` as the published proof-corpus entry and explicitly tied it to the governed `examples/m4/open-repository-proof` repository fixture.
- Added `yarn --cwd ide verify:m7` and validated it successfully.
- Updated M7-facing indexes and refreshed the `graph-glsp` package docs, including replacing the corrupted Chinese README with a clean UTF-8 version.

### File List

- `_bmad-output/implementation-artifacts/m7/3-3-validate-the-first-graphical-technology-path-and-publish-the-m7-proof-corpus.md`
- `_bmad-output/implementation-artifacts/m7/sprint-status.yaml`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/GRAPHICAL-TECHNOLOGY-DECISION.md`
- `docs/usages/m7-proof-usage.md`
- `docs/roadmap/athena-milestone-roadmap.md`
- `docs/usages/athena-workspace-summary.md`
- `examples/README.md`
- `examples/m7/README.md`
- `examples/m7/README.zh-CN.md`
- `integrations/graph-glsp/README.md`
- `integrations/graph-glsp/README.zh-CN.md`
- `ide/package.json`

### Change Log

- 2026-07-10: Implemented M7 Story `3.3` by freezing the current graphical technology decision and publishing the M7 proof corpus plus verification entrypoints.
