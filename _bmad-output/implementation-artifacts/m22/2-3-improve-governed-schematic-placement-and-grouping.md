---
baseline_commit: 0b43cbe
---

# Story 2.3: Improve Governed Schematic Placement and Grouping

Status: done

## Story

As an engineer,
I want the M22 schematic to group engineering subjects by readable purpose,
so that power, protection, controller, HMI, terminals, and load path are easy to identify.

## Acceptance Criteria

1. Given the M22 optimized sample sheet, when layout facts are produced, then power, protection, controller, HMI, terminals, and load subjects are placed into readable zones or groups.
2. Given grouping is applied, when the renderer consumes layout facts, then grouping follows Athena constraints and rules rather than renderer inference.
3. Given the M22 acceptance checklist is used, when optimized layout is compared against the M21 baseline, then grouping and placement evidence is available for review.

## Tasks / Subtasks

- [x] Add governed grouping facts (AC: 1, 2)
  - [x] Add failing layout-engine tests for constraint-derived grouping.
  - [x] Add group fact contracts emitted by the optimizer.
  - [x] Preserve subject and occurrence identity in group facts.
- [x] Apply constraint-owned placement influence (AC: 1, 2)
  - [x] Allow preferred-zone constraints to influence optimizer placement.
  - [x] Keep placement deterministic with stable tie-breakers.
- [x] Publish acceptance evidence hooks (AC: 3)
  - [x] Link grouping/placement evidence from M22 sample docs or usage docs.
  - [x] Add validation coverage.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run layout-engine tests.
  - [x] Run M22 static proof tests affected by docs.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- Story 2.2 added the optimizer boundary.
- Existing region facts group by schematic zone, but M22 needs explicit grouping facts derived from constraints so renderer code does not infer engineering groups.

### Guardrails

- Do not add renderer-local grouping inference.
- Do not add ELK behavior in this story.
- Do not add route or label persistence in this story.
- Keep grouping facts deterministic and identity preserving.

### Testing Requirements

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- M22 static proof tests affected by documentation updates
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 2, Story 2.3]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-1, AD-2, AD-7]
- [Source: `examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` failed first because group fact contracts and optimizer grouping behavior did not exist.
- `node --test ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs` passed after adding governed grouping evidence to the checklist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed after adding preferred-zone constraint application and group fact emission.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `SchematicLayoutGroupFact` and `SchematicLayoutGroupId` to carry constraint-derived grouping facts with intent, occurrence, role, zone, and constraint identity.
- Updated `RuleBasedSchematicLayoutOptimizer` to apply preferred-zone constraints before solving placement.
- Published Story 2.3 governed placement/grouping evidence in the M22 acceptance checklist, sample README, and usage doc.

### File List

- `_bmad-output/implementation-artifacts/m22/2-3-improve-governed-schematic-placement-and-grouping.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md`
- `examples/m22/sample-project/README.md`
- `ide/theia-frontend/scripts/athena-m22-layout-acceptance-checklist.test.mjs`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`

## Change Log

- 2026-07-18: Created M22 Story 2.3 with governed placement and grouping requirements.
- 2026-07-18: Added constraint-owned placement influence, explicit group facts, evidence docs, and tests.
