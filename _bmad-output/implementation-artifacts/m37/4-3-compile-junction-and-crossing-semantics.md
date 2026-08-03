---
story_key: 4-3-compile-junction-and-crossing-semantics
epic: m37-e4
requirements: [FR-20]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.3: Compile Junction And Crossing Semantics

Status: review

## Story

As an engineer reading a connection drawing,
I want joins and crossings rendered from explicit topology,
so that visual intersection never changes electrical or engineering meaning.

## Acceptance Criteria

1. Junction dots, terminal joins, bus taps, disconnected crossings, and wire hops are emitted only from explicit semantic or route facts.
2. A geometric crossing without a junction fact remains disconnected.
3. Join facts emit profile-selected marker classes and carry network, route, source, and policy trace.
4. Ambiguous or contradictory join/crossing evidence fails before rendering.
5. Renderer payload contains compiled marker facts only and no topology inference.
6. Focused topology, crossing, marker, negative tests, source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED junction/crossing grammar tests (AC: 1, 2, 3, 4, 5)
  - [x] Add junction marker test from explicit `RouteJunctionFact`.
  - [x] Add disconnected crossing marker test from explicit `RouteCrossingFact`.
  - [x] Add geometric crossing without junction stays disconnected test.
  - [x] Add ambiguous/contradictory join/crossing negative test.
  - [x] Add renderer payload hygiene test.
- [x] Implement junction/crossing marker compiler (AC: 1, 2, 3)
  - [x] Use responsibility-named types only; no `Proof`, `Demo`, `Sample`, milestone names, `V0`/`V1`, XML, SVG metadata, renderer authority, or compatibility shims.
  - [x] Consume route topology facts only.
  - [x] Carry route ids, semantic/network ids, policy/profile marker class, source provenance, and compiler snapshot.
- [x] Implement conflict diagnostics and normalized payload (AC: 4, 5)
  - [x] Reject joined crossings, duplicate contradictory marker facts, and missing marker policy.
  - [x] Normalize marker payload to Athena facts only.
  - [x] Keep renderer paint-only.
- [x] Verify and record gates (AC: 6)
  - [x] Run focused junction/crossing marker tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Visual intersection never creates electrical meaning. Only explicit `RouteJunctionFact` joins.
- `RouteCrossingFact(joined = false)` means disconnected crossing. Joined crossings are invalid because joined intersections must be junction facts.
- Marker selection comes from typed drawing/profile policy, not renderer heuristics.
- Renderer receives marker facts; it must not infer join, crossing, hop, or bus tap from geometry.

### Current Code Intelligence

- `AthenaRouteEngine` derives `RouteJunctionFact` and `RouteCrossingFact`.
- `RouteCrossingFact` already rejects `joined = true`.
- `DrawingProfileCompiler.kt` added drawing crossing/junction rule enums and profile policy.
- Keep this story in `routing-model` unless compiler integration becomes necessary.

### Expected Diagnostics

- `drawing.marker.policy.missing`
- `drawing.marker.crossing.joined`
- `drawing.marker.topology.contradictory`
- `drawing.marker.route.missing`
- `drawing.marker.protocol-authority.invalid`

### TDD And Verification

- RED first: focused marker compiler test fails before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.JunctionCrossingMarkerCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 4.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-20]
- [Source: `_bmad-output/implementation-artifacts/m37/4-1-compile-drawing-profiles-and-line-classes.md` - Drawing profile rules]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.JunctionCrossingMarkerCompilerTest` failed before implementation with unresolved marker compiler symbols.
- GREEN: focused `JunctionCrossingMarkerCompilerTest` passed after adding junction/crossing marker compiler.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- Hygiene: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- Encoding: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Whitespace: `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added junction/crossing marker compiler and Athena-only marker payload.
- Explicit `RouteJunctionFact` emits joined junction marker; explicit `RouteCrossingFact` emits disconnected or wire-hop crossing marker from profile rule.
- Contradictory junction/crossing evidence fails before rendering.
- Marker payload carries route ids, semantic id where applicable, selected policy, compiler snapshot, and no renderer topology inference.

### File List

- _bmad-output/implementation-artifacts/m37/4-3-compile-junction-and-crossing-semantics.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/JunctionCrossingMarkerCompiler.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/JunctionCrossingMarkerCompilerTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 4.3 from finalized M37 PRD, epics, and Story 4.1 drawing profile learnings.
- 2026-07-30: Implemented explicit junction/crossing marker compilation, contradiction diagnostics, normalized marker payload, and routing tests.
