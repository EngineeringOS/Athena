---
story_key: 1-4-lower-intent-into-traceable-route-facts
epic: m37-e1
requirements: [FR-6, FR-7]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.4: Lower Intent Into Traceable Route Facts

Status: review

## Story

As an engineer inspecting a compiled connection,
I want its authored intent preserved through route planning,
so that I can explain why Athena selected its region, channel, label policy, and route class.

## Acceptance Criteria

1. Validated `EngineeringConnectionIntentContract` lowers into transient Route Intent with owner, strength, source span, diagnostic identity, provenance, and no final geometry.
2. `ConnectionIr` and `RouteIntent` preserve authored intent influence for class, priority, separation, selected region or channel, label policy, owner, strength, and source provenance.
3. Every accepted RouteFact or professional drawing route request identifies the Connection Intent that influenced route class, channel or drawing region, separation behavior, label policy, and presentation class decision.
4. The active professional drawing path consumes authored intent evidence and must not invoke endpoint-derived `ElectricalConnectionIntentClassifier` when authored intent is present or required.
5. Invalid or missing required intent blocks route acceptance before rendering and emits typed diagnostics with source spans.
6. No SVG, renderer, endpoint type, visual geometry, planner proposal, or compatibility shim creates or repairs Connection Intent.
7. Focused lowering, route, compiler regression, source-set hygiene, encoding, and diff-check gates pass sequentially.

## Tasks / Subtasks

- [x] Add red tests for intent-to-route trace lowering (AC: 1, 2, 3)
  - [x] Extend `ConnectionIrLoweringTest` or M37 compiler tests so authored intent fields survive from connectivity compilation into `ConnectionIr`.
  - [x] Extend `RouteIntentLowererTest` so route intents include source-owned intent influence and deterministic constraint/evidence IDs.
  - [x] Add negative test proving missing or invalid required intent prevents accepted route facts before rendering.
- [x] Extend disposable IR models, not source authority (AC: 1, 2, 6)
  - [x] Add typed intent influence/evidence fields to existing `ConnectionIrConnection` and `RouteIntent` families.
  - [x] Preserve owner, strength, source span, diagnostic identity, provenance, class, priority, separation, region/channel, and label policy.
  - [x] Do not add final coordinates, anchors, SVG data, renderer state, ECS product fields, parallel models, or compatibility adapters.
- [x] Lower authored intent into route planning facts (AC: 1, 2, 3)
  - [x] Map `EngineeringConnectionIntentContract` into `ConnectionIr` and then `RouteIntent` through existing lowerers.
  - [x] Emit route constraints or compatibility evidence only when traceable to source-owned intent or compiler-owned hard rule.
  - [x] Keep ordering deterministic by stable IDs and source spans.
- [x] Clean active professional drawing route intent authority (AC: 3, 4, 5, 6)
  - [x] Replace endpoint-derived fallback in `AthenaProfessionalDrawingCompiler.route(...)` with authored-intent consumption.
  - [x] Convert authored intent to `ElectricalConnectionIntent` only as routing-engine input, carrying source trace.
  - [x] Emit blocking diagnostics when a visible engineering connection has no resolved authored intent.
- [x] Verify and record gates (AC: 7)
  - [x] Run focused compiler and routing tests sequentially.
  - [x] Run root `test`, source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source is SSOT. Connection Intent comes from `.athena` source/profile defaults resolved in Story 1.3.
- Story 1.4 owns lowering and trace evidence only. Route lanes, route-quality scoring, line grammar, labels, junctions, and E2E screenshots belong later M37 stories.
- Direct refactor only. Athena is pre-public: remove stale inferred active authority instead of preserving compatibility.
- No full ECS fields. No manufacturer, article number, lifecycle, procurement, BOM, datasheet, simulation, replacement, or AI layout facts.

### Current Code Intelligence

- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/EngineeringConnectivityContracts.kt` contains `EngineeringConnectionIntentContract` from Story 1.3.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrLowerer.kt` currently lowers `EngineeringConnectivityCompilation.Success` into `ConnectionIrConnection` with only id/from/to/provenance. Story 1.4 must carry intent there.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RouteIntentLowerer.kt` currently builds `RouteIntent` constraints from route declarations and network bundle membership only. Story 1.4 must include authored intent influence.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteIntentModels.kt` owns transient Route Intent facts consumed downstream.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt` owns route facts. If direct RouteFact fields are too broad for this story, preserve trace through route request/intent snapshot now and add RouteFact fields only where existing route engine can carry them cleanly.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt` still constructs `ElectricalConnectionIntentClassifier()` and falls back to endpoint-derived classification in `route(...)`. Story 1.4 must remove that active fallback for authored-intent routes.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntent.kt` is routing-engine input. It may remain as engine vocabulary but must not own source intent authority.

### Previous Story Learnings

- Story 1.3 added explicit `intent` syntax for flat Connections, route groups, Interface defaults, and profile defaults.
- Intent precedence is fixed: Connection, route group, Interface default, selected profile default.
- Intent fields are `class`, `priority`, `separation`, exactly one of `region` or `channel`, `label policy`, `owner`, and `strength`.
- Authored intent branch bypasses `ElectricalConnectionIntentClassifier`; do not reintroduce classifier authority.
- Old samples without M37 intent stay valid only when no authored intent vocabulary/profile default exists. The active professional drawing path for M37 must require authored intent.

### TDD And Verification

- RED first: add failing lowering/route tests before production edits.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 1.4]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-6, FR-7, NFR-1, NFR-2, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Connection Intent Boundary, Route Quality Ownership, Direct Refactor Targets]
- [Source: `_bmad-output/implementation-artifacts/m37/1-3-author-deterministic-connection-intent.md` - Previous Story Learnings]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-30: RED focused compiler test failed on missing `ConnectionIrConnection.intent` and `RouteIntent.intentInfluence`.
- 2026-07-30: GREEN focused compiler and routing gates passed after adding typed intent influence through `ConnectionIr`, `RouteIntent`, `AthenaRouteRequest`, and `RouteFact`.
- 2026-07-30: Full compiler test initially failed because old professional sample had no connectivity participants; sample was directly aligned to authored `connectivity enabled` and explicit source intent instead of restoring classifier fallback.
- 2026-07-30: Final gates passed sequentially: `:kernel:compiler:test`, `:kernel:routing-model:test`, root `test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added typed `ConnectionIrConnectionIntent` and `RouteIntentInfluence` so authored Connection Intent survives lowering without geometry or renderer authority.
- Added route constraints and compatibility evidence derived from source-owned intent class, priority, separation, region/channel, label policy, owner, strength, and provenance.
- Added `RouteFact.intentInfluence` and `AthenaRouteRequest.intentInfluence` so accepted route facts preserve intent influence evidence.
- Removed active professional drawing fallback to `ElectricalConnectionIntentClassifier`; visible routes now require authored intent and emit `drawing.route.intent.missing` before rendering when absent.
- Aligned the professional control drawing sample with current source authority by declaring `connectivity enabled` and authored intent in source.

### File List

- _bmad-output/implementation-artifacts/m37/1-4-lower-intent-into-traceable-route-facts.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- examples/m34/professional-control-drawing/src/com/engineeringood/m34/professional/01-control-drawing.athena
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrLowerer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrModels.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RouteIntentLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrLoweringTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RouteIntentLowererTest.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteIntentModels.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 1.4 from finalized M37 PRD, addendum, epics, CodeGraph code intelligence, and Story 1.3 learnings.
- 2026-07-30: Implemented intent-to-route trace lowering, route-fact intent evidence, professional drawing authored-intent enforcement, sample source alignment, and verification gates.
