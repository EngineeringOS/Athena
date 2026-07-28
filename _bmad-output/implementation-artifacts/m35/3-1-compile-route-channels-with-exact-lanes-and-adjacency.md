---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 3.1: Compile Route Channels With Exact Lanes And Adjacency

Status: review

## Story

As an engineer,
I want route channels compiled into deterministic topology and lane facts,
so that every conductor path is reproducible before route geometry is drawn.

## Acceptance Criteria

1. Given an authored RouteChannel rectangle, orientation, margin `M`, lane count `N`, and cross span `S`, when topology lowering runs, then it requires `N > 0` and `U = S - 2M > 0`, and lane `i` centre is the reduced exact rational `M + ((2i + 1) * U) / (2N)` with no pre-drawing rounding.
2. Given connections whose route intents traverse one channel, when lanes are allocated, then stable connection-id order receives lane indices up to capacity, horizontal order is top-to-bottom, and vertical order is left-to-right, and overflow fails instead of searching or overlapping.
3. Given two consecutive authored channels, when `RouteChannelTopology` derives adjacency, then exactly one adjacency exists only for same-Duct, interior-disjoint rectangles sharing one axis-aligned segment that remains positive after margin trimming, and cross-Duct, corner-only, overlapping, gapped, or multiply intersecting transitions fail.
4. Given odd/non-divisible spans, full capacity, and invalid topology fixtures, when golden tests and structured proof run, then rational lane centres, ordering, passable-boundary midpoint, and diagnostics are deterministic.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and inferred channels, floating-point lane variants, alternate topology models, stale fixtures, and compatibility paths are purged.

## Tasks / Subtasks

- [x] Add RED tests for route channel topology (AC: 1..4)
  - [x] Cover exact rational lane centres for odd spans.
  - [x] Cover stable route alias lane allocation and overflow.
  - [x] Cover valid same-duct adjacency and invalid cross-duct/corner/gap/overlap transitions.
  - [x] Assert structured proof and diagnostics are deterministic.
- [x] Implement route channel topology models in `kernel:physical-model` (AC: 1..4)
  - [x] Add exact rational type, lane facts, allocation facts, adjacency facts, diagnostics, and proof.
  - [x] Reuse `PhysicalRouteChannelV0` and physical route intent; do not use schematic routing-model.
- [x] Implement `RouteChannelTopologyCompiler` (AC: 1..4)
  - [x] Validate lane count and usable span.
  - [x] Allocate lanes by stable connection alias order.
  - [x] Derive passable same-duct adjacency only; fail closed otherwise.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched paths for inferred channels, floats, schematic route leakage, alternate models, XML, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 3.1 compiles channel topology and lane facts only. It does not route conductor segments, bind endpoints to anchors, render routes, build trace transport, or search for alternate paths.

### Architecture Requirements

- Physical routing topology belongs to M35 physical/drawing path, not schematic `routing-model`.
- Lane centers are exact rational millimetres until final rendering.
- No floating-point pre-rounding in topology.
- Adjacency is same-Duct only and requires one positive shared segment after margin trimming.
- Overflow fails; no search, reroute, overlap, or solver behavior.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 3, Story 3.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-28..FR-29.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-12, route proof conventions.
- `_bmad-output/implementation-artifacts/m35/2-7-compose-one-paint-only-cabinet-document.md` - previous story composition evidence.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T04:31:30+08:00 - Created Story 3.1 from M35 Epic 3 backlog and started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- 2026-07-28T04:38:58+08:00 - RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.RouteChannelTopologyCompilerTest` failed before fixture correction with expected structured diagnostic mismatch.
- 2026-07-28T04:45:00+08:00 - GREEN evidence: targeted RouteChannelTopologyCompilerTest passed with `BUILD SUCCESSFUL`.
- 2026-07-28T04:46:00+08:00 - Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test` passed with `BUILD SUCCESSFUL`.
- 2026-07-28T04:47:00+08:00 - Audit evidence: forbidden-route audit returned only existing physical compatibility terminology, encoding audit passed, and `git diff --check` reported no whitespace errors beyond existing CRLF warnings.

### Completion Notes List

- Added exact route channel topology compilation for M35 physical routing: rational lane centers, deterministic lane assignments, same-duct adjacency facts, proof payloads, and structured diagnostics.
- Extended physical route channel intents and compiled channel facts with explicit orientation so horizontal lanes are top-to-bottom and vertical lanes are left-to-right.
- Verified invalid topology fails closed for invalid lane counts, unusable spans, capacity overflow, cross-duct transitions, corner-only contact, gaps, and overlap.
- AC evidence: AC1 covered by odd-span rational center test; AC2 by stable alias allocation and overflow test; AC3 by valid and invalid adjacency tests; AC4 by deterministic proof/diagnostic assertions; AC5 by audit and sequential verification.

### File List

- `_bmad-output/implementation-artifacts/m35/3-1-compile-route-channels-with-exact-lanes-and-adjacency.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalInstallationTopologyCompiler.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalInstallationTopologyModels.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/RouteChannelTopologyCompiler.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/RouteChannelTopologyCompilerTest.kt`

### Change Log

- 2026-07-28 - Created Story 3.1 implementation guide and moved story to in-progress.
- 2026-07-28 - Implemented exact route channel topology compilation, deterministic lane allocation, same-duct adjacency validation, and verification evidence.
