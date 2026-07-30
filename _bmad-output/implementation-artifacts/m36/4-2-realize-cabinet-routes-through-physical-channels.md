---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E4.S2: Realize Cabinet Routes Through Physical Channels

Status: done

## Story

As the compiler,
I want to realize valid orthogonal routes through Cabinet physical channels,
so that connections respect physical installation policy.

**Requirements:** FR-25, FR-26, FR-29.

## Acceptance Criteria

1. Given valid PlacementFacts, Route Intent, and Cabinet physical policy, the route realizer
   selects a valid physical route-channel sequence by default when no exact sequence is authored.
2. The realized path honors anchor exit and entry directions, inflated component and physical-object
   bounds, clearance, and valid channels.
3. Off-channel segments, collisions, blocked paths, and violated required intent fail with
   source-spanned diagnostics instead of fallback routes.
4. Generic route contracts remain reusable while Cabinet-specific policy stays in the Physical
   Installation projection.

## Tasks / Subtasks

- [x] Add failing tests for route realization through Cabinet channels (AC: 1-4)
  - [x] Cover default channel-sequence selection from valid physical topology.
  - [x] Cover anchor direction, inflated bounds, clearance, and blocked-path rejection.
  - [x] Prove no fallback route is accepted when a required channel or constraint fails.
- [x] Introduce the cabinet route-realization stage (AC: 1-4)
  - [x] Derive channel sequences from Route Intent, topology, and Cabinet physical policy.
  - [x] Keep the realizer compiler-owned and transient.
  - [x] Keep Cabinet-specific policy out of generic routing contracts.
- [x] Wire route realization into the Cabinet projection pipeline (AC: 1-4)
  - [x] Feed the realized channel sequence into the existing cabinet routing/composition path.
  - [x] Preserve route intent, bundle ownership, provenance, and source spans.
  - [x] Do not add route markers, final route-proof elaboration, or E2E sample work here.
- [x] Run story evidence gate (AC: 1-4)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- 4-1 already lowered Route Intent and bundles. Reuse that compiler output; do not recompute intent
  semantics here.
- Existing cabinet routing code still assumes preselected `route.channelIds`. 4-2 must move the
  channel-choice step into a compiler-owned realization stage or helper before route composition.
- Use `RouteChannelTopologyCompiler` for passable adjacency/lane topology and
  `PhysicalConstraintEvaluatorV0` for fit, collision, and clearance gating.
- Route bundles, `through`, `avoid`, `bundle`, and priority remain inputs or hints. They do not
  become renderer coordinates or final route facts in this story.
- Do not implement route markers, RouteFact proof elaboration, or the sample-project E2E proof.
  Those belong to 4-3 and 4-4.
- No XML compatibility paths. This project is unreleased, so stale legacy routing behavior may be
  removed rather than preserved.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompiler.kt`
  - `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompilerTest.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/RouteChannelTopologyCompiler.kt`
  - `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorV0.kt`
- Keep text assets UTF-8.
- Do not add legacy XML or raw SVG authority paths.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 4.2, FR-25, FR-26, FR-29.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-25, FR-26,
  FR-29, NFR-1, NFR-3, NFR-4, NFR-8, NFR-9.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/addendum.md` - route channel
  policy, planner boundary, and cleanup notes.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-7, AD-8.
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompiler.kt`
  - current cabinet route compilation seam.
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/RouteChannelTopologyCompiler.kt`
  - channel lane and adjacency topology.
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorV0.kt`
  - clearance and collision gating.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RouteIntentLowerer.kt`
  - upstream route-intent lowering from story 4-1.
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompilerTest.kt`
  - existing route compiler test shape and diagnostics style.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

### Completion Notes List

- Added compiler-owned route realization before route topology and cabinet routing.
- Realized default physical channel sequences from route intent and passable topology when no authored sequence exists.
- Tightened cabinet routing obstacle checks to respect mounted-occurrence clearance inflations.
- Added targeted tests for route realization and verified with `clean`, full `test`, encoding audit, and `git diff --check`.

### File List

- _bmad-output/implementation-artifacts/m36/4-2-realize-cabinet-routes-through-physical-channels.md
- _bmad-output/implementation-artifacts/m36/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CabinetRouteRealizationCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt
- kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompiler.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/CabinetRouteRealizationCompilerTest.kt

## Change Log

- 2026-07-29: Implemented M36-E4.S2 route realization, verified, and moved the story to review.
