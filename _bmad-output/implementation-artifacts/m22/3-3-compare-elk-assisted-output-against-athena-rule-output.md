---
baseline_commit: 0b43cbe
---

# Story 3.3: Compare ELK-Assisted Output Against Athena Rule Output

Status: done

## Story

As a reviewer,
I want to compare ELK-assisted output with Athena rule-based output,
so that M22 can evaluate helper value without choosing a final layout stack.

## Acceptance Criteria

1. Given the M22 sample project has rule-based and ELK-assisted layout paths available, when comparison evidence is generated, then the comparison uses normalized Athena layout facts.
2. Given checklist items are reviewed, when ELK-assisted output is compared, then the comparison reports whether ELK improves spacing, grouping, and basic routing.
3. Given M22 scope is reviewed, when the comparison is read, then it states that M22 does not select ELK as final architecture or sole layout engine.

## Tasks / Subtasks

- [x] Publish ELK comparison artifact (AC: 1, 2, 3)
  - [x] Add failing static test for the comparison artifact.
  - [x] Compare normalized Athena facts, not raw adapter output.
  - [x] Report spacing, grouping, and basic routing comparison.
- [x] Link comparison from usage docs (AC: 3)
  - [x] Keep final stack decision deferred.
  - [x] Keep adapter value framed as optional helper evidence.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run the static ELK comparison test.
  - [x] Run layout-engine tests.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 3.2 added an isolated experimental ELK-style adapter normalization path.
- M22 must compare helper value only after output is normalized into Athena facts.

### Guardrails

- Do not select ELK as final architecture.
- Do not claim ELK is the sole layout engine.
- Do not compare raw adapter output as renderer truth.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-elk-comparison.test.mjs`
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 3, Story 3.3]
- [Source: `_bmad-output/implementation-artifacts/m22/M22-ELK-SPIKE-ENVELOPE.md`]
- [Source: `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-elk-comparison.test.mjs` failed first because `M22-ELK-COMPARISON.md` did not exist.
- `node --test ide/theia-frontend/scripts/athena-m22-elk-comparison.test.mjs` passed after adding the comparison artifact and usage link.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `M22-ELK-COMPARISON.md` to compare rule-based and ELK-assisted paths using normalized Athena layout facts.
- Recorded that the local adapter proves the normalization boundary but does not yet improve spacing, grouping, or basic routing.
- Reaffirmed that M22 does not select ELK as final architecture or sole layout engine.

### File List

- `_bmad-output/implementation-artifacts/m22/3-3-compare-elk-assisted-output-against-athena-rule-output.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m22/M22-ELK-COMPARISON.md`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-elk-comparison.test.mjs`

## Change Log

- 2026-07-18: Created M22 Story 3.3 with normalized ELK comparison requirements.
- 2026-07-18: Added normalized ELK comparison artifact, usage link, static validation, and test evidence.
