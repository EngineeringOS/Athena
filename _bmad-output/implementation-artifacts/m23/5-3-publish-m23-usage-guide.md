---
story_id: 5.3
story_key: 5-3-publish-m23-usage-guide
epic: 5
epic_title: Sample Proof, Usage, And Boundary Guardrails
title: Publish M23 usage guide
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 5.3: Publish M23 Usage Guide

## Story

As Aaron,
I want usage documentation for M23,
So that I know which project to open, what syntax is supported, and what behavior is expected.

## Acceptance Criteria

**Given** M23 implementation is complete
**When** usage docs are published
**Then** they name `examples/m23/sample-project` and `src/01-layout-hints.athena`
**And** they show the admitted syntax and expected IDE behavior
**And** they clearly state M22 was preview-only and M23 is real language admission

## Developer Context

Story 5.1 created the sample project. Story 5.2 proved compiler, LSP, and Theia smoke acceptance.
Story 5.3 documents the human-facing usage path and the boundary between M22 preview snippets and
M23 admitted language syntax.

## Architecture Guardrails

- Documentation must point reviewers to the openable IDE sample, not `.mjs` helper scripts.
- Documentation must state that ANTLR/LSP and Tree-sitter both need syntax upgrades for future syntax.
- Documentation must avoid claiming new visual layout depth beyond M23 language admission.

## Tasks/Subtasks

- [x] Add M23 usage documentation under `docs/usages`.
- [x] Name the openable sample project and source file.
- [x] Show admitted layout syntax and expected IDE behavior.
- [x] Record the M22 preview-only to M23 admitted-language correction.
- [x] Record verification commands and the installed-LSP-host prerequisite for IDE smoke.

## Dev Agent Record

### Debug Log

- 2026-07-18: Created Story 5.3 from Epic 5 backlog after Story 5.2 reached review.
- 2026-07-18: Added the M23 usage guide and linked the normal IDE workflow to `examples/m23/sample-project/src/01-layout-hints.athena`.

### Completion Notes

- Published `docs/usages/m23-proof-usage.md` with the supported layout block syntax, IDE usage path,
  verification commands, and honest boundary notes.
- Recorded that M22 layout-block text was preview-only and M23 is the first real ANTLR,
  Tree-sitter, compiler, LSP, and IDE admission.

### File List

- `docs/usages/m23-proof-usage.md`
- `_bmad-output/implementation-artifacts/m23/5-3-publish-m23-usage-guide.md`
- `_bmad-output/implementation-artifacts/m23/sprint-status.yaml`

### Change Log

- 2026-07-18: Added M23 usage guide and story record.

## Status

Review.
