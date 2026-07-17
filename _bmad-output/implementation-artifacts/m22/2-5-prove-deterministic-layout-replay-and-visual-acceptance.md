---
baseline_commit: 0b43cbe
---

# Story 2.5: Prove Deterministic Layout Replay and Visual Acceptance

Status: done

## Story

As a reviewer,
I want executable proof that M22 layout optimization is stable and visible,
so that the milestone is not judged only by screenshots or manual inspection.

## Acceptance Criteria

1. Given the M22 sample project, when the layout replay tests run, then layout facts are compared across repeated runs before screenshot checks.
2. Given adapter-normalized facts are present, when they are compared, then they are compared after Athena normalization.
3. Given visual acceptance evidence is checked, when the proof runs, then it checks the named M22 checklist items.

## Tasks / Subtasks

- [x] Publish deterministic replay proof artifact (AC: 1, 2, 3)
  - [x] Add failing static test for the replay proof document.
  - [x] Document fact-level replay before visual review.
  - [x] Document adapter-normalized fact comparison if adapter facts exist.
- [x] Link visual acceptance evidence (AC: 3)
  - [x] Link the proof from sample README and usage docs.
  - [x] Name checklist items covered by the proof.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run the static replay proof test.
  - [x] Run layout-engine tests.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Stories 2.2 and 2.3 added optimizer replay and group-fact tests.
- Story 2.4 added route/label readability evidence.
- M22 needs an explicit reviewer-facing proof that facts are compared before visual acceptance.

### Guardrails

- Do not claim screenshots alone prove layout correctness.
- Do not claim ELK is selected or required.
- Do not add new renderer behavior in this story.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-layout-replay-proof.test.mjs`
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 2, Story 2.5]
- [Source: `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`]
- [Source: `examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-layout-replay-proof.test.mjs` failed first because `M22-LAYOUT-REPLAY-PROOF.md` did not exist.
- `node --test ide/theia-frontend/scripts/athena-m22-layout-replay-proof.test.mjs` passed after adding the proof artifact and links.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `M22-LAYOUT-REPLAY-PROOF.md` to document fact-level replay before screenshots or manual visual review.
- Linked replay proof from the M22 sample README and usage doc.
- Added static validation that the proof names adapter-normalized fact comparison and the M22 visual checklist items.

### File List

- `_bmad-output/implementation-artifacts/m22/2-5-prove-deterministic-layout-replay-and-visual-acceptance.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `examples/m22/sample-project/M22-LAYOUT-REPLAY-PROOF.md`
- `examples/m22/sample-project/README.md`
- `ide/theia-frontend/scripts/athena-m22-layout-replay-proof.test.mjs`

## Change Log

- 2026-07-18: Created M22 Story 2.5 with deterministic replay and visual acceptance proof requirements.
- 2026-07-18: Added deterministic replay proof artifact, links, static validation, and test evidence.
