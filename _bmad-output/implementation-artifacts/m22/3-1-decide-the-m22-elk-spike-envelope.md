---
baseline_commit: 0b43cbe
---

# Story 3.1: Decide the M22 ELK Spike Envelope

Status: done

## Story

As an architect,
I want the ELK spike dependency and packaging envelope decided before implementation,
so that the adapter work cannot leak into the renderer, frontend runtime, or persistence format.

## Acceptance Criteria

1. Given the M22 architecture spine and PRD, when the ELK spike story begins, then the team has selected whether ELK is included directly or isolated behind an experimental adapter package.
2. Given the envelope is selected, when it is reviewed, then it preserves a local-only execution envelope with no remote service tier.
3. Given the spike is evaluated, when removal is considered, then the decision records how the spike can be removed without changing layout facts or renderer contracts.

## Tasks / Subtasks

- [x] Add ELK envelope decision artifact (AC: 1, 2, 3)
  - [x] Add failing static test for the envelope decision.
  - [x] Document direct dependency vs isolated adapter decision.
  - [x] Document local-only execution and removal path.
- [x] Link decision from M22 usage docs (AC: 1, 2, 3)
  - [x] Add reviewer-facing usage link.
  - [x] Keep final stack decision deferred.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run the static ELK envelope test.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- M22 architecture allows ELK only as an optional experimental helper behind Athena contracts.
- Story 3.1 must decide the envelope before any adapter code starts.

### Guardrails

- Do not add an ELK dependency in this story.
- Do not select ELK as the final layout stack.
- Do not add renderer or frontend runtime coupling.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-elk-envelope.test.mjs`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 3, Story 3.1]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-3]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-elk-envelope.test.mjs` failed first because `M22-ELK-SPIKE-ENVELOPE.md` did not exist.
- `node --test ide/theia-frontend/scripts/athena-m22-elk-envelope.test.mjs` passed after adding the decision artifact and usage link.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Recorded the M22 ELK decision as an isolated experimental adapter envelope.
- Documented local-only execution, no remote service tier, no renderer/frontend dependency, removability, and no final ELK stack selection.
- Added a static guard test for the decision artifact.

### File List

- `_bmad-output/implementation-artifacts/m22/3-1-decide-the-m22-elk-spike-envelope.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m22/M22-ELK-SPIKE-ENVELOPE.md`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-elk-envelope.test.mjs`

## Change Log

- 2026-07-18: Created M22 Story 3.1 with ELK spike envelope decision requirements.
- 2026-07-18: Added ELK spike envelope decision artifact, usage link, and static validation.
