---
baseline_commit: 8dd23ddb8f1618ee3cef45d5c556766567434f52
---

# Story 3.1: Arrange schematic subjects by engineering role

Status: done

## Story

As an engineer,
I want related schematic subjects grouped by engineering role,
so that the sheet reads by power, control, terminals, and load intent rather than generic graph topology.

## Acceptance Criteria

1. Given an M21 schematic sample containing power supply, protection, controller, terminals, and a primary load path, when layout intent and layout facts are produced, then related subjects are grouped into coherent schematic regions.
2. Given the layout facts, when they are inspected, then power source, protection, controller, terminals, and primary load path are identifiable from the layout output without reading renderer code.
3. Given repeated runs on the same governed input, when grouping facts are produced, then grouping remains deterministic and explainable through layout intent and facts.
4. Given Story 3.1 scope, when implementation is reviewed, then it does not implement route facts, schematic conductor routing, label avoidance, external adapter selection, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or user drag-save truth.
5. Given the M21 proof baseline, when checks run, then the existing M21 sample-project and graph-workbench smoke proofs still pass.

## Tasks / Subtasks

- [x] Add explicit schematic grouping facts (AC: 1, 2, 3, 4)
  - [x] Extend layout-engine output with Athena-owned region/grouping facts derived from layout intent zones and roles.
  - [x] Preserve canonical intent ids, roles, zones, and deterministic ordering in grouping facts.
  - [x] Keep grouping facts separate from route facts, label facts, renderer state, and adapter output.
- [x] Update the rule-based schematic strategy (AC: 1, 2, 3)
  - [x] Emit deterministic region/grouping facts for power, control, terminal, load, and annotation zones when present.
  - [x] Ensure placement facts remain stable after adding grouping facts.
  - [x] Keep the strategy schematic-only.
- [x] Add focused tests (AC: 1, 2, 3, 4)
  - [x] Prove a sample with power source, protection, controller, terminal, and load emits identifiable grouped regions.
  - [x] Prove repeated runs produce identical grouping facts.
  - [x] Prove grouping facts are explainable from layout intent roles/zones and do not require renderer code.
  - [x] Prove route, label, cabinet, physical routing, desktop-viewer, AI layout, and final stack terms stay out of implementation contracts.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Story 2.1 added `LayoutIntentSnapshot`, roles, zones, priority, alignment, relationship constraints, occurrence identity, and source spans.
- Story 2.2 added `:kernel:layout-engine`, `SchematicLayoutStrategy`, `SchematicPlacementFact`, and `RuleBasedSchematicLayoutStrategy`.
- Story 2.3 added subordinate helper proposal normalization without choosing any concrete helper stack.
- Current placement facts group implicitly by zone through stable coordinates, but they do not yet expose explicit region/grouping facts.

### Architectural Guardrails

- Follow M21 AD-1, AD-2, AD-4, AD-6, AD-7, AD-8, AD-9, AD-10, and AD-11.
- Engineering readability beats generic graph neatness.
- Grouping facts must be explainable from layout intent and must remain upstream of renderer/Theia.
- This story does not implement route facts, conductor routing, label placement, adapter selection, cabinet authoring, physical routing, desktop-viewer behavior, AI layout, or final layout-stack selection.

### Implementation Guidance

Likely update targets:

- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`
- `kernel/layout-engine/README.md`
- this story file and `sprint-status.yaml`

Reasonable contract names include:

- `SchematicRegionFact`
- `SchematicRegionBounds`
- `SchematicRegionPurpose`

Names may vary if the implementation stays clear and satisfies the ACs.

### Previous Story Intelligence

- Story 2.3 requires helper proposals to cover every layout intent item exactly once before normalization.
- Keep helper normalization preserving grouping authority; helpers may not replace role or zone meaning.
- Keep the M21 graph-workbench smoke proof intact.

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

- Red phase: `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` failed before implementation with unresolved `regionFacts` and `SchematicRegionId`.
- Green phase: `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed after adding region facts and aligning fixture priority values.
- Review follow-up: addressed helper duplicate coverage, helper geometry validation, deterministic tie-breakers, and snapshot-scoped region id documentation.
- Final verification after review fixes:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
  - `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
  - `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- M21 proof checks passed:
  - `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
  - `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
  - `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added `SchematicRegionId`, `SchematicRegionBounds`, and `SchematicRegionFact` to make schematic grouping inspectable from layout output.
- Extended `SchematicLayoutStrategyResult` with deterministic `regionFacts` derived from Athena-owned placement facts.
- Updated the rule-based strategy and helper normalizer to emit region facts for present schematic zones without changing placement authority or introducing deferred scope.
- Expanded layout-engine tests to prove region identification, deterministic repeats, explainability from intent roles/zones, and scope boundaries.
- Resolved review findings by enforcing exact helper coverage, validating helper geometry, adding deterministic tie-breakers, and documenting snapshot-scoped region identity.

### File List

- `_bmad-output/implementation-artifacts/m21/3-1-arrange-schematic-subjects-by-engineering-role.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `kernel/layout-engine/README.md`
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt`
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`

## Change Log

- 2026-07-17: Created M21 Story 3.1 for schematic grouping by engineering role.
- 2026-07-17: Implemented Athena-owned schematic region facts and verification coverage.
- 2026-07-17: Addressed code review findings and marked Story 3.1 done after final verification.

## Senior Developer Review (AI)

### Review Outcome

Approved after fixes.

### Findings Addressed

- [x] Helper proposals could include all required intent ids plus a duplicate valid fact; fixed by requiring exact one-to-one intent coverage.
- [x] Helper-normalized region facts could be derived from raw proposal facts; fixed by deriving from normalized placement facts.
- [x] Region ordering needed deterministic tie-breakers beyond position and intent id; fixed with occurrence id and role tie-breakers.
- [x] Helper proposal geometry could contain invalid dimensions or overflowing bounds; fixed with nonnegative coordinate, positive size, and checked coordinate bounds validation.
- [x] Region ids needed explicit snapshot scope guidance; documented composite use of snapshot id plus region id.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` passed from `ide/`.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
