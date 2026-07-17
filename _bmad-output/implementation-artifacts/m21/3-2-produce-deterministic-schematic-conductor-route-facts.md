---
baseline_commit: 8dd23ddb8f1618ee3cef45d5c556766567434f52
---

# Story 3.2: Produce deterministic schematic conductor route facts

Status: done

## Story

As an engineer,
I want schematic conductor routes to be deterministic and endpoint-aware,
so that wires read as schematic topology without implying physical routing.

## Acceptance Criteria

1. Given governed schematic endpoints in the M21 sample, when route facts are derived, then route facts describe sheet-level schematic conductor topology between governed endpoints.
2. Given route facts are inspected, when endpoints, route identity, route segments, and routing lanes are read, then they preserve canonical subject, occurrence, snapshot, and endpoint identity without requiring renderer code.
3. Given repeated runs on the same governed input, when route facts are produced, then route identity, endpoint ordering, segment ordering, and lane assignment remain deterministic.
4. Given M21 routing scope, when implementation is reviewed, then route facts do not claim cabinet, harness, cable tray, 3D installation, or physical wire path meaning.
5. Given the M21 proof baseline, when checks run, then existing layout-engine, sample-project, graph-workbench, and Theia smoke proofs still pass.

## Tasks / Subtasks

- [x] Introduce schematic route fact contracts (AC: 1, 2, 3, 4)
  - [x] Add a narrow Athena-owned routing contract for schematic route ids, endpoint refs, orthogonal segments, route lanes, and route facts.
  - [x] Preserve snapshot id, canonical subject identity, occurrence identity, and endpoint identity in route facts.
  - [x] Keep route facts separate from placement facts, region facts, label facts, renderer state, adapter output, and physical routing vocabulary.
- [x] Add deterministic route derivation (AC: 1, 2, 3)
  - [x] Derive route facts from governed schematic endpoint input plus existing layout placement facts.
  - [x] Emit stable orthogonal segments between endpoint positions using deterministic lane assignment.
  - [x] Keep derivation schematic-only and reject non-schematic snapshots or mismatched placement snapshots.
- [x] Add focused tests (AC: 1, 2, 3, 4)
  - [x] Prove sensor-to-controller and controller-to-terminal-to-load paths emit endpoint-aware route facts.
  - [x] Prove repeated runs produce identical route facts, segment order, and lane assignment.
  - [x] Prove route facts are inspectable from routing contracts and do not require renderer code.
  - [x] Prove cabinet, harness, cable tray, 3D installation, physical wire path, desktop-viewer, AI layout, and final stack terms stay out of implementation contracts.
- [x] Validate and update story status (AC: 1, 2, 3, 4, 5)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`.
  - [x] Run any new routing module tests if a module is introduced.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`.
  - [x] Run `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`.
  - [x] Run `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` from `ide/`.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Update this story's Dev Agent Record and File List.

## Dev Notes

### Current State

- Story 2.1 introduced `LayoutIntentSnapshot`, schematic roles/zones, relationship constraints, occurrence identity, and source spans.
- Story 2.2 introduced `:kernel:layout-engine`, `SchematicLayoutStrategy`, and deterministic placement facts.
- Story 2.3 introduced helper proposal normalization while keeping helpers subordinate.
- Story 3.1 added Athena-owned `SchematicRegionFact` output, helper duplicate coverage validation, helper geometry validation, deterministic region tie-breakers, and snapshot-scoped region identity documentation.
- There is no current `kernel/routing-model` module in `settings.gradle.kts`. M21 architecture names `kernel/routing-model` as the structural seed, so Story 3.2 may introduce it if that stays cleaner than placing route facts in `layout-engine`.
- Earlier M12 work published projection anchor and routing-corridor guidance in `kernel/projection-model`; Story 3.2 must not confuse that downstream projection guidance with M21 solved schematic route facts.

### Architectural Guardrails

- Follow M21 AD-1, AD-3, AD-4, AD-6, AD-7, AD-8, AD-9, AD-10, and AD-11.
- Route facts are schematic topology only. They are not cabinet routing, harness routing, cable tray routing, installation routing, or physical wire paths.
- Route facts are layout facts consumed by renderer/Theia. Renderer must not invent endpoint meaning or solve engineering route semantics locally.
- Route facts must carry canonical identity needed by future source, outline, Problems, and sheet coherence.
- Do not select ELK, Dagre, Graphviz, AI layout, or any final layout stack in this story.

### Implementation Guidance

Likely update targets:

- `settings.gradle.kts` if a new `:kernel:routing-model` module is introduced.
- `kernel/routing-model/build.gradle.kts` if a new module is introduced.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/` for route contracts and deterministic schematic route derivation.
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/` for focused route tests.
- `kernel/routing-model/README.md` if a new module is introduced.
- `kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt` only if derivation belongs beside placement and region output.
- `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt` for integration with placement/region facts if needed.
- this story file and `sprint-status.yaml`.

Reasonable contract names include:

- `SchematicRouteId`
- `SchematicEndpointRef`
- `SchematicRouteLane`
- `SchematicRouteSegment`
- `SchematicRouteFact`
- `SchematicRouteStrategy` or `RuleBasedSchematicRouteStrategy`

Names may vary if the implementation stays clear, deterministic, and schematic-only.

### Previous Story Intelligence

- Helper proposal validation in Story 3.1 now requires exact one-to-one intent coverage and valid geometry; preserve that rigor for route endpoint coverage.
- Region ids are scoped by snapshot id; route ids should either include snapshot context or be documented/tested as snapshot-scoped.
- Keep ordering deterministic with explicit tie-breakers, not collection iteration assumptions.
- Keep the M21 graph-workbench smoke proof intact; Story 3.2 must not change accepted M20/M21 canvas behavior.

### Testing Requirements

Run checks sequentially on Windows:

- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- any new routing module test task, for example `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs`
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Red phase: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` failed before implementation with unresolved route contract and strategy symbols.
- Green phase:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test`
- Review follow-up: added close-diagonal route coverage and same-point endpoint rejection to prevent zero-length segments.
- Final verification after review fixes:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
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

- Added `:kernel:routing-model` as the narrow M21 schematic route fact module.
- Added endpoint, route, lane, point, orthogonal segment, request, snapshot, route fact, result, and strategy contracts.
- Implemented a deterministic rule-based schematic route strategy with stable request sorting and lane assignment.
- Added tests for endpoint-aware route facts, deterministic repeats, identity preservation, non-schematic rejection, and deferred-scope boundaries.
- Resolved review finding by preventing zero-length segments for close diagonal endpoints and rejecting same-point endpoint routes.

### File List

- `_bmad-output/implementation-artifacts/m21/3-2-produce-deterministic-schematic-conductor-route-facts.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `settings.gradle.kts`
- `kernel/routing-model/build.gradle.kts`
- `kernel/routing-model/README.md`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicRoutingModel.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/SchematicRoutingModelTest.kt`

## Change Log

- 2026-07-17: Created M21 Story 3.2 for deterministic schematic conductor route facts.
- 2026-07-17: Implemented schematic route fact contracts and deterministic route strategy.
- 2026-07-17: Addressed review edge case and marked Story 3.2 done after final verification.

## Senior Developer Review (AI)

### Review Outcome

Approved after fix.

### Findings Addressed

- [x] Close diagonal endpoints could make the midpoint equal one endpoint and create a zero-length segment; fixed by emitting a two-segment L path for that case.
- [x] Same-point endpoints could produce invalid route geometry; fixed by rejecting requests whose anchors share the same sheet point.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed.
- `node --test ide/theia-frontend/scripts/athena-m21-graph-workbench-visual-proof.test.mjs` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` passed from `ide/`.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
