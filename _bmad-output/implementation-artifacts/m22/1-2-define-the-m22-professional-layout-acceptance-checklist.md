---
baseline_commit: 0b43cbe
---

# Story 1.2: Define the M22 professional layout acceptance checklist

Status: done

## Story

As a reviewer,
I want a named visual acceptance checklist for M22,
so that layout quality is judged against explicit engineering criteria rather than subjective taste.

## Acceptance Criteria

1. Given the M22 sample project and `draft/screenshort` references, when the acceptance checklist is published, then it names the comparison set used for M22.
2. Given the checklist, when reviewers evaluate M22 layout quality, then it covers zones, spacing, grouping, basic orthogonal edge routing, label overlap avoidance, and M21 baseline comparison.
3. Given M22 scope boundaries, when the checklist is reviewed, then it states that full EPLAN parity is not part of the M22 acceptance bar.
4. Given the M22 usage path, when users read the sample docs, then they can find the acceptance checklist from the M22 sample and usage documentation.

## Tasks / Subtasks

- [x] Publish the M22 acceptance checklist (AC: 1, 2, 3)
  - [x] Add a named checklist document under `examples/m22/sample-project/`.
  - [x] Name the M21 baseline and `draft/screenshort` reference set used for comparison.
  - [x] Cover zones, spacing, grouping, basic orthogonal edge routing, label overlap avoidance, and M21 baseline comparison.
  - [x] State that full EPLAN parity is out of scope for M22.
- [x] Link the checklist from user-facing docs (AC: 4)
  - [x] Update `examples/m22/sample-project/README.md`.
  - [x] Update `docs/usages/m22-proof-usage.md`.
  - [x] Keep `.mjs` files framed as supporting tests only.
- [x] Add focused validation (AC: 1, 2, 3, 4)
  - [x] Add a Node static check that verifies the checklist exists, is linked, and covers required criteria.
  - [x] Ensure the static check does not depend on screenshots being present at runtime.
  - [x] Run encoding audit after docs changes.

## Dev Notes

### Current State

- Story 1.1 added the M22 openable sample project and usage path.
- The M22 architecture AD-7 requires a reviewable acceptance artifact/checklist naming the comparison set and covering zones, spacing, grouping, basic orthogonal edge routing, label overlap avoidance, and M21 baseline comparison.
- `draft/screenshort` is a visual inspiration source, not a pixel-perfect target.

### Guardrails

- Do not claim full EPLAN parity.
- Do not require public ecosystem, full IEC/QElectroTech library, cabinet authoring, physical routing, AI layout, or final solver-stack decisions.
- Keep checklist language useful for manual review and simple enough for static validation.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 1, Story 1.2]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m22/prd.md` - FR-2]
- [Source: `docs/usages/m22-proof-usage.md`]
- [Source: `examples/m22/sample-project/README.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs` failed first because `M22-LAYOUT-ACCEPTANCE.md` did not exist, then passed after the checklist and links were added.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md` as the named professional layout acceptance checklist.
- Linked the checklist from the M22 sample README and M22 usage doc.
- Captured the comparison set, zones, spacing, grouping, basic orthogonal edge routing, label overlap avoidance, M21 baseline comparison, and explicit non-parity boundaries.

### File List

- `_bmad-output/implementation-artifacts/m22/1-2-define-the-m22-professional-layout-acceptance-checklist.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `examples/m22/sample-project/README.md`
- `examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md`
- `ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs`
## Change Log

- 2026-07-18: Created M22 Story 1.2 with professional layout acceptance checklist requirements.
- 2026-07-18: Added the M22 professional layout acceptance checklist, links, and validation coverage.
