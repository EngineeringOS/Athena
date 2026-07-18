---
story_id: 5.4
story_key: 5-4-add-m23-boundary-and-stale-doc-regression-checks
epic: 5
epic_title: Sample Proof, Usage, And Boundary Guardrails
title: Add M23 boundary and stale-doc regression checks
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 5.4: Add M23 Boundary And Stale-Doc Regression Checks

## Story

As an Athena maintainer,
I want M23 docs and tests to reject overstated claims,
So that future retrospectives do not promise unsupported layout features.

## Acceptance Criteria

**Given** M23 artifacts and docs
**When** boundary checks run
**Then** they confirm no claims of EPLAN parity, advanced routing, AI layout, public repository/import ecosystem, broad IEC ingestion, or hidden canvas-state persistence
**And** stale M22 preview-only wording remains corrected
**And** M23 retrospective records achievement, usage, and deferred work honestly

## Developer Context

M23 is language admission. It must not be described as a new layout-depth, auto-layout, EPLAN parity,
repository ecosystem, or AI-layout milestone. The closeout also needs to capture the IDE smoke root
cause discovered in Story 5.2: the installed LSP host can be stale even when source tests pass.

## Architecture Guardrails

- Keep M23 positioned as `.athena` syntax admission and round-trip closure.
- Keep M22 wording honest: M22 selected/previewed layout block text, M23 admitted it.
- Keep tests tied to checked-in docs and the openable sample source.

## Tasks/Subtasks

- [x] Add a failing boundary test for M23 closeout docs and sample scope.
- [x] Add M23 achievement, usage, and retrospective record.
- [x] Confirm stale M22 preview-only wording remains corrected.
- [x] Run the boundary test and record verification evidence.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 5.4 from Epic 5 backlog after Story 5.3 reached review.
- 2026-07-18: Red gate verified with `node --test ide/theia-frontend/scripts/athena-m23-boundary.test.mjs`; it failed because the M23 retrospective did not exist.
- 2026-07-18: Added M23 retrospective and boundary coverage.
- 2026-07-18: Boundary regression passed with `node --test ide/theia-frontend/scripts/athena-m23-boundary.test.mjs` after correcting wrapped wording in `epics.md`, M23 usage, and the test matcher.
- 2026-07-18: Final closeout verification passed across language/compiler/layout/LSP Gradle suites, Tree-sitter, frontend build, targeted frontend regressions, M23 Electron smoke, and encoding audit.

### Completion Notes

- Added an executable M23 boundary test covering PRD, architecture, epics, usage, retrospective, M22
  correction wording, and the sample source.
- Recorded the M23 milestone achievements, IDE usage, verification evidence, lessons, deferred
  scope, and the stale installed LSP host root cause.
- Confirmed the M23 boundary test passes with 1 test passed and 0 failed.
- Recorded full closeout verification in the M23 achievement/usage/retrospective note.

### File List

- `ide/theia-frontend/scripts/athena-m23-boundary.test.mjs`
- `_bmad-output/implementation-artifacts/m23/m23-achievement-usage-retrospective-2026-07-18.md`
- `_bmad-output/implementation-artifacts/m23/5-4-add-m23-boundary-and-stale-doc-regression-checks.md`
- `_bmad-output/implementation-artifacts/m23/sprint-status.yaml`

### Change Log

- 2026-07-18: Added M23 boundary regression test and retrospective/usage closeout record.

## Status

Review.
