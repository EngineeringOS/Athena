---
baseline_commit: 561a977
---

# Story 2.2: Add the rule-based schematic layout strategy boundary

Status: done

## Story

As an implementer,
I want a layout strategy boundary that turns intent and rules into facts,
so that M21 can start with deterministic rules without locking the future layout engine.

## Acceptance Criteria

1. Given a `LayoutIntentSnapshot`, when the rule-based schematic layout strategy runs, then it emits deterministic placement/layout facts for the same governed input.
2. Given the strategy output, when it is inspected, then it remains an Athena-owned layout contract and does not require Theia, renderer CSS, DOM state, canvas state, or an external adapter to solve layout.
3. Given M21 architecture, when implementation is reviewed, then the strategy boundary can later host rule-based logic, adapters, or AI-assisted engines, but Story 2.2 does not choose ELK or any final external layout stack.
4. Given Story 2.2 scope, when implementation is reviewed, then it does not implement schematic conductor routing, route facts, label avoidance, cabinet authoring, physical routing, desktop-viewer behavior, or user drag-save truth.
5. Given the M21 proof baseline, when checks run, then the existing M21 sample-project and graph-workbench smoke proofs still pass.

## Tasks / Subtasks

- [x] Introduce the layout strategy boundary (AC: 1, 2, 3, 4)
  - [x] Add a small `:kernel:layout-engine` module or an equivalent clearly scoped kernel package if a module is not needed.
  - [x] Define a strategy interface that consumes `LayoutIntentSnapshot`.
  - [x] Define deterministic layout fact output types for placement/layout facts only.
  - [x] Keep route facts, label facts, adapter implementations, and renderer state out of this story.
- [x] Add a rule-based schematic strategy implementation (AC: 1, 2, 3)
  - [x] Implement a minimal deterministic rule-based strategy that maps intent items to stable placement/layout facts.
  - [x] Ensure repeated runs on the same snapshot return identical facts.
  - [x] Preserve canonical subject id, occurrence id, intent id, snapshot id, and source span in the output where applicable.
- [x] Preserve architecture boundaries (AC: 2, 3, 4)
  - [x] Keep Theia, renderer CSS, DOM, and canvas state out of the strategy API.
  - [x] Do not add ELK or any other layout dependency.
  - [x] Do not introduce route/routing, label avoidance, cabinet authoring, physical routing, desktop-viewer, AI layout, or final stack-selection terms as implementation contracts.
- [x] Add focused tests (AC: 1, 2, 3, 4)
  - [x] Add tests proving deterministic output for repeated runs.
  - [x] Add tests proving output preserves canonical identity from `LayoutIntentSnapshot`.
  - [x] Add tests proving no renderer/frontend/adapter dependency is required.
  - [x] Add boundary tests proving route facts, label facts, external adapters, cabinet, physical routing, and AI layout are absent from Story 2.2 contracts.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5)
  - [x] Run the new module or package tests.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Story 2.1 added M21 layout intent snapshot contracts in `:kernel:layout-model`.
- `settings.gradle.kts` currently includes `:kernel:layout-model` but no `:kernel:layout-engine`.
- `:kernel:layout-model` already depends on `:kernel:engineering-model`.
- Existing layout contracts intentionally do not solve coordinates, route conductors, or avoid labels.

### Architectural Guardrails

- Follow M21 AD-1, AD-2, AD-3, AD-4, AD-5, AD-8, AD-9, AD-10, and AD-11.
- The strategy boundary belongs upstream of renderer and Theia.
- A rule-based strategy is allowed. Adapter selection is not.
- Output facts are layout facts only, not route facts or label facts.

### Implementation Guidance

Likely update targets:

- `settings.gradle.kts` if a new module is added
- `kernel/layout-engine/build.gradle.kts`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/...`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/...`
- `kernel/layout-engine/README.md`
- this story file and `sprint-status.yaml`

Keep type names conservative and architecture-readable. Reasonable names include:

- `SchematicLayoutStrategy`
- `RuleBasedSchematicLayoutStrategy`
- `SchematicLayoutFact`
- `SchematicPlacementFact`
- `SchematicLayoutPoint`
- `SchematicLayoutSize`
- `SchematicLayoutStrategyResult`

Do not use route/routing terminology in this story; Story 3.2 owns schematic conductor route facts.

### Previous Story Intelligence

- Story 2.1 kept the existing view-level `LayoutIntent` enum intact and added distinct M21 `LayoutIntentSnapshot` contracts.
- Reuse those contracts directly instead of creating another intent vocabulary.
- Story 2.1 review tightened `LayoutSourceSpan`; preserve that validation and identity behavior.

### Testing Requirements

Run checks sequentially on Windows:

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` if a new module is added
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` rerun after review schematic-family guard
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added `:kernel:layout-engine` as the M21 strategy-boundary module.
- Added `SchematicLayoutStrategy`, deterministic placement fact contracts, and `RuleBasedSchematicLayoutStrategy`.
- Preserved canonical subject, occurrence, intent, snapshot, role, zone, and source-span identity in placement facts.
- Kept Story 2.2 bounded away from route facts, label facts, external layout adapters, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, and final stack selection.
- Addressed review feedback by rejecting non-schematic snapshots in the schematic strategy.

### File List

- `_bmad-output/implementation-artifacts/m21/2-2-add-the-rule-based-schematic-layout-strategy-boundary.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `settings.gradle.kts`
- `kernel/layout-engine/README.md`
- `kernel/layout-engine/build.gradle.kts`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`

## Change Log

- 2026-07-17: Created M21 Story 2.2 for the rule-based schematic layout strategy boundary.
- 2026-07-17: Implemented and reviewed the rule-based schematic layout strategy boundary.

## Senior Developer Review (AI)

### Outcome

Approved after patch.

### Action Items

- [x] [Review][Patch] Reject non-schematic layout intent snapshots in the rule-based schematic strategy.
