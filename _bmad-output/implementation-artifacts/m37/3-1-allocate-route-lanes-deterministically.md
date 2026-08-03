---
story_key: 3-1-allocate-route-lanes-deterministically
epic: m37-e3
requirements: [FR-11, FR-12, FR-13]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.1: Allocate Route Lanes Deterministically

Status: review

## Story

As an engineer reviewing a dense connection drawing,
I want related routes assigned to governed lanes,
so that wire columns and channels remain readable and capacity conflicts are explicit.

## Acceptance Criteria

1. Route Channels expose compiler-owned typed lanes with capacity, spacing, orientation, occupancy, and collision evidence.
2. Lane allocation is deterministic from Connection Intent, priority, grouping, capacity, and stable tie-breakers.
3. Accepted RouteFacts identify selected lane and channel occupancy evidence.
4. Over-capacity, incompatible orientation, spacing conflicts, and impossible allocation produce typed diagnostics without fallback geometry.
5. Planner, renderer, SVG, XML, and external layout objects cannot create or mutate lanes.
6. Focused lane, determinism, boundary, compiler regression, source-set hygiene, encoding audit, and `git diff --check` gates pass sequentially.

## Tasks / Subtasks

- [x] Add RED lane allocation tests (AC: 1, 2, 3, 4)
  - [x] Add routing-model tests proving deterministic lane assignment for power/control/protective-earth intents.
  - [x] Add capacity and spacing diagnostics tests.
  - [x] Add stable tie-breaker test where shuffled requests produce identical lane assignments.
  - [x] Add compiler/professional drawing regression that accepted RouteFacts carry lane/occupancy evidence.
- [x] Extend route lane and occupancy model in place (AC: 1, 3, 5)
  - [x] Add responsibility-named data types for lane identity, capacity, spacing, occupancy, conflict, and selected lane evidence.
  - [x] Extend existing `RouteFact` / route snapshot structures instead of creating a parallel route IR.
  - [x] Keep route lanes as compiler-owned derived facts, not source facts and not renderer facts.
- [x] Implement deterministic lane allocation (AC: 2, 4)
  - [x] Allocate lanes from authored Route Intent influence, priority, physical channel/preferred region, requested endpoints, and stable connection id tie-breakers.
  - [x] Emit diagnostics for over-capacity, incompatible orientation, spacing conflict, and no legal lane.
  - [x] Do not fall back to center lines or renderer-side repair.
- [x] Wire active professional drawing route path (AC: 3, 5)
  - [x] Ensure `AthenaProfessionalDrawingCompiler` routes receive lane evidence from `AthenaRouteEngine`.
  - [x] Preserve exact endpoint attachment and intent influence already added in earlier M37 stories.
  - [x] Keep renderer paint-only; no lane inference in SVG/presentation code.
- [x] Verify and record gates (AC: 6)
  - [x] Run focused routing tests.
  - [x] Run focused compiler professional drawing/lane tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Lane allocation is derived compiler behavior. Source owns Connection Intent; route engine owns candidate facts; renderer paints only.
- Do not add auto-layout AI, ELK adapter, external planner runtime, or generic optimizer in this story.
- Do not add compatibility shims. Athena is pre-public; directly refactor stale route fields if needed.
- No XML/SVG/planner payload may define lane meaning.
- No production class with `Proof`, `Demo`, `Sample`, milestone names, or `V0`/`V1`.

### Current Code Intelligence

- `AthenaRouteEngine` is the active deterministic route engine consumed by `AthenaProfessionalDrawingCompiler`.
- `RouteConstraint`, `RouteFactSnapshot`, and route request/fact models live under `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing`.
- `AthenaProfessionalDrawingCompiler.route()` builds `AthenaRouteRequest` with exact anchors, authored `EngineeringConnectionIntentContract`, and hard constraints.
- Physical package has `RouteChannelLaneFact` and `RouteChannelLaneAssignment` in `RouteChannelTopologyCompiler`; use ideas only if they fit the active routing path and do not make physical model own schematic route lanes.
- Existing route tests live in `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineTest.kt`.

### Expected Model Shape

Use responsibility names, not milestone names:

```kotlin
RouteLaneId
RouteLaneOrientation
RouteLaneCapacity
RouteLaneOccupancy
RouteLaneAssignment
RouteLaneConflict
```

Accepted RouteFact should expose enough evidence for later M37 stories:

```text
connectionId
routeId
intentInfluence
selectedLane
channelOccupancy
quality
segments
```

### Determinism Rules

Stable order must not depend on input list order. Suggested tie-breakers:

1. Connection Intent priority.
2. Intent class / separation group.
3. Preferred channel or region.
4. Stable connection id.
5. Stable route id.

### Diagnostics

Use route-owned diagnostic names, for example:

- `route.lane.capacity.exceeded`
- `route.lane.orientation.incompatible`
- `route.lane.spacing.conflict`
- `route.lane.unavailable`

Exact names may adapt to local routing diagnostic style, but tests must pin them.

### TDD And Verification

- RED first: routing lane tests fail before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.AthenaRouteEngineTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 3.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-11, FR-12, FR-13, FR-25, NFR-3, NFR-4, NFR-5]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Route Quality Ownership, ELK Boundary, M37 Visual Quality Checklist]
- [Source: `_bmad-output/implementation-artifacts/m37/2-3-prove-projection-neutral-semantic-truth.md` - Previous story projection-neutral comparison]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-30: Added RED routing tests for deterministic lane assignment, lane capacity, lane spacing diagnostics, and lane occupancy evidence.
- 2026-07-30: Added compiler regression proving professional drawing RouteFacts carry lane assignment and occupancy evidence.
- 2026-07-30: Extended routing model with lane id, orientation, capacity, occupancy, assignment, conflict, and diagnostic contracts.
- 2026-07-30: Implemented deterministic lane allocation in `AthenaRouteEngine` using intent priority and stable ids, with lane diagnostics in `RouteFactSnapshot`.
- 2026-07-30: Verification pass: focused `AthenaRouteEngineTest`, focused professional drawing compiler test, full `:kernel:routing-model:test`, full `:kernel:compiler:test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- RouteFacts now carry compiler-owned lane assignment and occupancy evidence.
- Route snapshots now carry route-lane diagnostics for capacity and spacing conflicts.
- Lane assignment is deterministic by authored intent priority and stable route/connection identifiers, not renderer or input-list order.

### File List

- _bmad-output/implementation-artifacts/m37/3-1-allocate-route-lanes-deterministically.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalControlDrawingCompilerTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 3.1 from finalized M37 PRD, addendum, epics, route code intelligence, and Story 2.3 learnings.
- 2026-07-30: Implemented deterministic route lane allocation, lane occupancy evidence, lane diagnostics, and professional drawing regression coverage.
