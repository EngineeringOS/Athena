---
baseline_commit: 0b43cbe
---

# Story 4.1: Select the Minimal `.athena` Layout-Hint Syntax

Status: done

## Story

As an engineer,
I want a minimal source syntax for component layout hints,
so that layout round-trip persists engineering intent instead of arbitrary canvas coordinates.

## Acceptance Criteria

1. Given the M22 PRD, addendum, and architecture spine, when source-mutating round-trip implementation begins, then the team has selected layout block, projection hint, or subject-local hint syntax.
2. Given the selected syntax is reviewed, when component layout hints are authored, then the syntax supports component placement, alignment, and grouping.
3. Given the syntax is inspected, when layout intent is expressed, then it avoids raw pixel coordinates as the primary authored language.
4. Given route and label persistence is reviewed, when M22 scope is enforced, then route and label persistence remains deferred unless mechanically trivial.

## Tasks / Subtasks

- [x] Publish syntax decision artifact (AC: 1, 2, 3, 4)
  - [x] Add failing static test for the syntax decision.
  - [x] Select layout block, projection hint, or subject-local hint syntax.
  - [x] Include placement, alignment, and grouping examples.
  - [x] Explicitly avoid raw pixel coordinates as primary language.
- [x] Link syntax decision from usage docs (AC: 1, 4)
  - [x] Add reviewer-facing usage link.
  - [x] Keep route and label persistence deferred.
- [x] Run validation (AC: 1, 2, 3, 4)
  - [x] Run static syntax decision test.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- M22 architecture blocks source-mutating round-trip until the syntax direction is selected.
- The syntax must persist engineering layout intent, not renderer-local coordinates.

### Guardrails

- Do not implement parser/compiler mutation in this story.
- Do not persist route or label hints in this story.
- Do not select raw `x/y` coordinates as the primary authored layout language.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-layout-hint-syntax.test.mjs`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 4, Story 4.1]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-4, AD-5]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-layout-hint-syntax.test.mjs` failed first because `M22-LAYOUT-HINT-SYNTAX.md` did not exist.
- `node --test ide/theia-frontend/scripts/athena-m22-layout-hint-syntax.test.mjs` passed after adding the syntax decision artifact and usage link.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Selected a project-level `layout` block syntax for M22 placement, alignment, and grouping round-trip.
- Documented relationship-oriented examples using `near`, `below`, `aligned-with`, and `grouped-with`.
- Explicitly deferred route and label persistence.

### File List

- `_bmad-output/implementation-artifacts/m22/4-1-select-the-minimal-athena-layout-hint-syntax.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m22/M22-LAYOUT-HINT-SYNTAX.md`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-layout-hint-syntax.test.mjs`

## Change Log

- 2026-07-18: Created M22 Story 4.1 with layout-hint syntax decision requirements.
- 2026-07-18: Added layout block syntax decision, usage link, and static validation.
