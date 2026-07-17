---
baseline_commit: 0b43cbe
---

# Story 3.2: Implement the Local ELK Adapter Normalization Path

Status: done

## Story

As an implementer,
I want ELK output normalized into Athena layout facts,
so that ELK can assist optimization without becoming authority.

## Acceptance Criteria

1. Given Athena layout intent and constraints for the M22 sample, when the optional ELK adapter is enabled, then adapter input is derived from Athena constraints, not renderer DOM.
2. Given the adapter emits output, when the output is consumed, then adapter output is normalized into Athena layout facts before rendering or comparison.
3. Given the adapter path is run repeatedly, when normalized facts are compared, then the adapter path is deterministic after normalization.
4. Given the adapter is disabled, when optimization runs, then it falls back to Athena rules without changing the renderer contract.

## Tasks / Subtasks

- [x] Add local ELK-style adapter contracts (AC: 1, 2)
  - [x] Add failing layout-engine tests for adapter proposal and normalization.
  - [x] Implement isolated experimental adapter classes outside renderer/frontend code.
  - [x] Ensure adapter output is normalized through existing Athena helper normalizer.
- [x] Add deterministic fallback path (AC: 3, 4)
  - [x] Cover repeated normalized adapter output.
  - [x] Cover disabled adapter fallback to rule-based Athena optimizer.
- [x] Run validation (AC: 1, 2, 3, 4)
  - [x] Run layout-engine tests.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 3.1 selected an isolated experimental adapter envelope.
- Existing `SchematicLayoutHelperNormalizer` can normalize subordinate helper proposals into Athena layout facts.

### Guardrails

- Do not add an external ELK dependency.
- Do not add renderer or frontend runtime dependency.
- Do not persist adapter output.
- Do not change renderer contracts.

### Testing Requirements

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 3, Story 3.2]
- [Source: `_bmad-output/implementation-artifacts/m22/M22-ELK-SPIKE-ENVELOPE.md`]
- [Source: `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` failed first because the experimental adapter classes and helper-id result field did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` then failed on an ordering expectation; the test was corrected to compare normalized identity sets because adapter output is intentionally normalized by intent id.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed after adding the adapter and fallback path.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added isolated experimental ELK-style adapter contracts in `SchematicLayoutAdapters.kt`.
- Added adapter proposal emission and normalization through `SchematicLayoutHelperNormalizer`.
- Added `helperId` to optimization results so normalized adapter output remains inspectable.
- Covered deterministic adapter replay and disabled-adapter fallback to rule-based Athena optimization.

### File List

- `_bmad-output/implementation-artifacts/m22/3-2-implement-the-local-elk-adapter-normalization-path.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutAdapters.kt`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`

## Change Log

- 2026-07-18: Created M22 Story 3.2 with local ELK adapter normalization requirements.
- 2026-07-18: Added experimental adapter proposal, normalization, helper identity, fallback path, and tests.
