---
story_id: 1.4
story_key: 1-4-preserve-existing-source-compatibility
epic: 1
epic_title: Parser Parity And Source Fixtures
title: Preserve existing source compatibility
status: review
created: 2026-07-18
updated: 2026-07-18
baseline_commit: bdbe0295684bd70da89aa436cc302a59157c111f
source_epics: epics.md
source_prd: ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
source_architecture: ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Story 1.4: Preserve existing source compatibility

## Story

As an Athena engineer,
I want M23 grammar changes to preserve prior language behavior,
So that layout admission does not regress established source contracts.

## Acceptance Criteria

**Given** existing package/import/system/device/port/connect examples and tests
**When** parser and compiler verification runs
**Then** existing M0-M22 syntax still parses
**And** unsupported import forms still report the same deterministic diagnostics
**And** no prior example is rewritten only to satisfy M23

## Developer Context

This story is a compatibility gate for Epic 1. It should not expand the M23 language. It verifies
that the ANTLR and Tree-sitter changes from Stories 1.1-1.3 did not break existing source behavior.

## Architecture Guardrails

- Follow `AD-8`: M0-M22 source compatibility remains binding.
- Follow `AD-10`: this story is not new layout depth or sample proof.
- Do not rewrite prior milestone examples just to satisfy M23.

## Tasks/Subtasks

- [x] Run sequential language parser verification for existing M0-M22 syntax.
- [x] Run Tree-sitter syntax UX verification for existing M0-M22 syntax.
- [x] Confirm no prior examples were rewritten for M23 compatibility.
- [x] Update Dev Agent Record, File List, Change Log, and story status after verification.

## Dev Agent Record

### Debug Log

- 2026-07-18: Started Story 1.4 from `backlog`; baseline commit `bdbe0295684bd70da89aa436cc302a59157c111f`.
- 2026-07-18: `.\gradlew.bat --no-daemon --console=plain :kernel:language:test` passed.
- 2026-07-18: `yarn --cwd ide/tree-sitter-athena test` passed and reported 40 node tests passing.
- 2026-07-18: Checked `git status --short examples`; M23 added `examples/m23`, while older dirty example paths were pre-existing and not rewritten for this story.

### Completion Notes

- Verified language parser compatibility for existing package/import/system/device/port/connect coverage after M23 grammar changes.
- Verified Tree-sitter compatibility for existing M0/M4/M17/M18 syntax UX corpus after M23 layout-node additions.
- Confirmed this story did not rewrite prior examples to satisfy M23.

### File List

- `_bmad-output/implementation-artifacts/m23/1-4-preserve-existing-source-compatibility.md`
- `_bmad-output/implementation-artifacts/m23/sprint-status.yaml`

### Change Log

- 2026-07-18: Completed M23 Epic 1 compatibility verification and story status update.

## Status

Review. Story 1.4 is ready for code review in `sprint-status.yaml`.
