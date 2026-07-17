---
baseline_commit: 561a977
---

# Story 2.1: Introduce layout intent and layout snapshot contracts

Status: done

## Story

As an architect,
I want layout intent to be explicit before solved layout facts,
so that engineering layout decisions stay explainable and do not collapse into opaque coordinates.

## Acceptance Criteria

1. Given governed projection and Presentation IR input, when M21 derives layout input for a schematic sheet, then `:kernel:layout-model` exposes explicit layout intent contracts carrying engineering role, preferred zone, priority, alignment, and relationship constraints where applicable.
2. Given a layout intent snapshot, when it is inspected or serialized by tests, then it preserves canonical subject identity, occurrence identity, snapshot identity, and source-span identity without relying on renderer CSS, DOM state, or canvas interaction state.
3. Given existing layout-model contracts, when Story 2.1 is implemented, then existing `LayoutDocument`, `ViewDefinition`, `LayoutGroup`, `LayoutNode`, and `LayoutRelationship` behavior remains source-compatible or is migrated with focused tests.
4. Given M21 architecture, when implementation is reviewed, then it does not introduce a layout engine, route facts, label avoidance, adapter selection, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection.
5. Given the M21 sample project and visible proof baseline, when checks run, then the existing M21 sample-project and graph-workbench smoke proofs still pass.

## Tasks / Subtasks

- [x] Add first-class M21 layout intent contracts in `:kernel:layout-model` (AC: 1, 2, 3, 4)
  - [x] Introduce explicit value types for layout snapshot id, layout intent id, canonical occurrence id, and source-span identity where the current model has no suitable type.
  - [x] Introduce role, preferred-zone, priority, alignment, and relationship-constraint vocabulary for schematic layout intent.
  - [x] Avoid confusing the existing view-level `LayoutIntent` enum with the new M21 layout-intent snapshot contract; rename or scope types if needed to keep call sites readable.
  - [x] Keep the contracts immutable, ordered, and renderer-independent.
- [x] Add layout intent snapshot contracts (AC: 2, 3, 4)
  - [x] Add a `LayoutIntentSnapshot` or equivalent aggregate that carries snapshot identity, view family, intent items, constraints, and source-span/canonical identity fields.
  - [x] Ensure snapshot construction is deterministic for the same ordered input.
  - [x] Keep route facts, label facts, solved placement facts, and layout-engine strategy out of this story.
- [x] Preserve existing layout-model behavior (AC: 3)
  - [x] Update existing layout-model tests only where type names or constructor shape require migration.
  - [x] Keep `LayoutDocument` group/node/relationship identity behavior passing.
  - [x] Update `kernel/layout-model/README.md` to describe M21 layout intent snapshots without claiming layout solving.
- [x] Add focused Story 2.1 tests (AC: 1, 2, 3, 4)
  - [x] Add tests proving layout intent items carry role, zone, priority, alignment, relationship constraints, canonical subject id, occurrence id, snapshot id, and source span.
  - [x] Add tests proving renderer/CSS/DOM/canvas state is not part of the model contract.
  - [x] Add boundary assertions that no route facts, layout engine, adapter, cabinet authoring, physical routing, or AI layout types were introduced.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt` already defines:
  - `ViewDefinition`
  - `LayoutIntent` as a small view-level enum with `STRUCTURAL` and `CONNECTIVITY`
  - `LayoutDocument`, `LayoutGroup`, `LayoutNode`, `LayoutRelationship`
  - projection family contracts and canonical semantic identity anchors
- Existing tests:
  - `LayoutModelTest` verifies electrical projection-family contracts and canonical semantic identity across groups, nodes, and relationships.
  - `LayoutModelMarkerTest` verifies the module marker.
- Story 2.1 must not discard that M2-era foundation. It should extend or carefully rename around it.

### Architectural Guardrails

- Follow M21 AD-1, AD-2, AD-4, AD-8, AD-9, AD-10, and AD-11.
- Layout intent is explainable intermediate meaning before solved coordinates.
- Layout intent snapshots carry identity and constraints; they are not renderer state.
- Theia and renderer remain consumers; they do not own layout meaning.
- This story creates contracts only. It does not implement rule-based layout strategy, route facts, label avoidance, adapter selection, or visible layout improvement.

### Implementation Guidance

Likely update targets:

- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`
- `kernel/layout-model/README.md`
- this story file and `sprint-status.yaml`

Potential type directions:

- Keep or rename the existing `LayoutIntent` enum if it conflicts with new M21 terminology.
- Add M21-specific contracts such as:
  - `LayoutSnapshotId`
  - `LayoutIntentId`
  - `LayoutOccurrenceId`
  - `LayoutSourceSpan`
  - `SchematicLayoutRole`
  - `SchematicLayoutZone`
  - `LayoutAlignment`
  - `LayoutPriority`
  - `LayoutIntentConstraint`
  - `LayoutIntentItem`
  - `LayoutIntentSnapshot`

Names may vary if the final implementation is clearer, but the acceptance criteria must remain covered.

### Previous Story Intelligence

- Epic 1 proved the sample project and graph workbench through actual Theia DOM evidence, not a self-fulfilling marker.
- Keep the M21 smoke proof alive while adding kernel contracts; do not regress the IDE proof path.
- The review for Epic 1 caught overclaiming. Story 2.1 documentation must not claim layout solving or visible grouping improvement before Epic 3 implements it.

### Testing Requirements

Run checks sequentially on Windows:

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test` rerun after review source-span validation fix
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added first-class M21 layout intent snapshot contracts in `:kernel:layout-model`.
- Kept the existing view-level `LayoutIntent` enum intact to avoid unnecessary compiler/runtime migration.
- Added deterministic canonical snapshot construction and tests for identity, source span, role, zone, priority, alignment, and relationship constraints.
- Addressed review feedback by rejecting inverted same-line `LayoutSourceSpan` ranges and adding regression coverage.
- Kept Story 2.1 contract-only: no layout engine, route facts, label facts, adapter selection, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection.
- Updated the layout-model README to describe M21 layout intent snapshots without claiming solved layout behavior.

### File List

- `_bmad-output/implementation-artifacts/m21/2-1-introduce-layout-intent-and-layout-snapshot-contracts.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `kernel/layout-model/README.md`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`

## Change Log

- 2026-07-17: Created M21 Story 2.1 for first-class layout intent and layout snapshot contracts.
- 2026-07-17: Implemented M21 layout intent snapshot contracts and focused layout-model tests.
- 2026-07-17: Review passed after source-span validation was tightened.

## Senior Developer Review (AI)

### Outcome

Approved after patch.

### Action Items

- [x] [Review][Patch] Reject inverted same-line `LayoutSourceSpan` ranges and cover the validation with a focused test.
