---
baseline_commit: 561a977
---

# Story 2.3: Guard the adapter boundary without choosing a final layout stack

Status: done

## Story

As an architect,
I want external layout helpers to remain subordinate adapters,
so that ELK or any future helper cannot become Athena's layout authority.

## Acceptance Criteria

1. Given the M21 layout architecture, when adapter support or adapter documentation is inspected, then any helper output must normalize into Athena-owned layout facts.
2. Given adapter-boundary contracts, when they are inspected, then engineering grouping, ordering, and schematic purpose remain governed by Athena layout intent and rule-based strategy contracts.
3. Given M21 scope, when implementation is reviewed, then no M21 artifact chooses ELK, any final external layout stack, or a concrete adapter dependency.
4. Given Story 2.3 scope, when implementation is reviewed, then it does not implement route facts, label avoidance, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or user drag-save truth.
5. Given the M21 proof baseline, when checks run, then the existing M21 sample-project and graph-workbench smoke proofs still pass.

## Tasks / Subtasks

- [x] Add subordinate adapter-boundary contracts (AC: 1, 2, 3, 4)
  - [x] Add small contract types that describe helper proposals without importing or naming a final helper stack.
  - [x] Ensure helper proposals normalize into `SchematicLayoutStrategyResult` or Athena-owned placement facts.
  - [x] Keep layout intent and rule-based strategy as the authority over engineering role, ordering, and schematic purpose.
- [x] Add adapter-boundary documentation (AC: 1, 2, 3, 4)
  - [x] Update `kernel/layout-engine/README.md` to describe subordinate helper boundaries.
  - [x] State explicitly that Story 2.3 does not choose ELK or any final layout stack.
  - [x] Keep route, label, cabinet, physical, desktop-viewer, AI layout, and drag-save scope deferred.
- [x] Add focused tests (AC: 1, 2, 3, 4)
  - [x] Prove helper proposal output normalizes into Athena-owned placement facts.
  - [x] Prove adapter metadata does not replace snapshot, subject, occurrence, role, zone, or source-span identity.
  - [x] Prove the module has no external helper dependency or final stack selection.
  - [x] Prove deferred scope terms are absent from implementation contracts.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Story 2.1 introduced `LayoutIntentSnapshot` and related identity/intent contracts.
- Story 2.2 introduced `:kernel:layout-engine`, `SchematicLayoutStrategy`, `RuleBasedSchematicLayoutStrategy`, and Athena-owned placement facts.
- No external layout helper or final layout stack is currently selected.

### Architectural Guardrails

- Follow M21 AD-1, AD-3, AD-4, AD-5, AD-6, AD-7, AD-8, and AD-11.
- Adapter output is never authority. It must normalize into Athena layout facts before any renderer or IDE consumer sees it.
- Story 2.3 may define a boundary, but it must not add an ELK dependency, adapter implementation, final stack decision, route facts, label facts, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or drag-save truth.

### Implementation Guidance

Likely update targets:

- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`
- `kernel/layout-engine/README.md`
- this story file and `sprint-status.yaml`

Reasonable contract names include:

- `LayoutHelperAdapterId`
- `SchematicLayoutHelperProposal`
- `SchematicLayoutHelperNormalizer`

Do not import or mention a concrete helper library in implementation contracts.

### Previous Story Intelligence

- Story 2.2 review added a guard that the rule-based schematic strategy rejects non-schematic snapshots.
- Keep that guard intact.
- Keep placement facts as Athena-owned output; helper proposals are subordinate input to normalization only.

### Testing Requirements

Run checks sequentially on Windows:

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` rerun after review full-coverage guard
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added subordinate helper proposal and normalizer contracts in `:kernel:layout-engine`.
- Ensured helper proposals normalize into Athena-owned schematic placement facts instead of becoming authority.
- Added tests that helper output preserves snapshot, subject, occurrence, role, zone, source-span, and full intent coverage.
- Kept Story 2.3 free of concrete helper dependencies, final stack selection, route facts, label facts, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, and drag-save truth.

### File List

- `_bmad-output/implementation-artifacts/m21/2-3-guard-the-adapter-boundary-without-choosing-a-final-layout-stack.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `kernel/layout-engine/README.md`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`

## Change Log

- 2026-07-17: Created M21 Story 2.3 for subordinate adapter-boundary guardrails.
- 2026-07-17: Implemented and reviewed subordinate helper proposal normalization guardrails.

## Senior Developer Review (AI)

### Outcome

Approved after patch.

### Action Items

- [x] [Review][Patch] Require helper proposals to cover every layout intent item exactly once before normalization.
