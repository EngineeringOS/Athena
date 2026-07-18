---
story_id: 5.1
story_key: 5-1-create-the-openable-m23-sample-project
epic: 5
epic_title: Sample Proof, Usage, And Boundary Guardrails
title: Create the openable M23 sample project
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 5.1: Create The Openable M23 Sample Project

## Story

As a reviewer,
I want a real M23 sample project with layout blocks in `.athena` source,
So that I can prove the milestone through the IDE instead of reading scripts.

## Acceptance Criteria

**Given** M23 parser/compiler admission is in place
**When** I open `examples/m23/sample-project`
**Then** `src/01-layout-hints.athena` contains a real system-scoped layout block
**And** the file opens without false syntax diagnostics
**And** the Graphical View projects the active layout-hint source

## Developer Context

M22 intentionally did not use layout blocks in sample `.athena` files because the language did not
admit them. M23 must provide a real project that the Theia IDE can open with accepted layout syntax.

## Architecture Guardrails

- Sample project must contain real `.athena` source, not `.mjs` user instructions.
- Layout block must be system-scoped.
- Package/import syntax must stay governed by the existing repository contract.

## Tasks/Subtasks

- [x] Add failing sample-project test for M23 project shape and launch scripts.
- [x] Create `examples/m23/sample-project` with `athena.yaml`, lock, README, and `src/01-layout-hints.athena`.
- [x] Add IDE launch scripts for M23 sample project.
- [x] Run sample shape checks; update Dev Agent Record, File List, Change Log, and story status.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 5.1 from backlog after Epic 4 reached review; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: Red gate verified with `node --test ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs`; the test failed because `examples/m23/README.md` did not exist.
- 2026-07-18: Sample shape verification passed with `node --test ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs`.

### Completion Notes

- Created a real openable M23 sample project with governed package manifest/lock files.
- Added `src/01-layout-hints.athena` with a system-scoped M23 layout block.
- Added `start:m23` and `start:smoke:m23` script entries.

### File List

- `examples/m23/README.md`
- `examples/m23/sample-project/README.md`
- `examples/m23/sample-project/athena.lock`
- `examples/m23/sample-project/athena.yaml`
- `examples/m23/sample-project/src/01-layout-hints.athena`
- `ide/package.json`
- `ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs`
- `ide/theia-product/package.json`

### Change Log

- 2026-07-18: Added the openable M23 sample project and IDE launch script entries.

## Status

Review. Story 5.1 is marked review in `sprint-status.yaml`.
