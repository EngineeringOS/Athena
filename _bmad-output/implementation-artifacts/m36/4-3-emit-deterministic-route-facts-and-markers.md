---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E4.S3: Emit Deterministic Route Facts And Markers

Status: done

## Story

As an engineer or AI agent,
I want accepted routes to carry complete quality and trace evidence,
so that their result can be inspected, reproduced, and trusted.

**Requirements:** FR-13, FR-27, FR-28, FR-40.

## Acceptance Criteria

1. Given a valid route proposal, route selection is deterministic using crossings, bends, length,
   bundle continuity, channel changes, label clearance, priority, and stable tie-breaking.
2. Accepted routes emit orthogonal segments, labels, RouteJunctionFacts, RouteCrossingFacts, and
   route quality facts.
3. Center-to-center and renderer-generated connections are forbidden.
4. Every RouteFact includes connection, endpoint Port, Anchor, occurrence, Route Intent, selected
   channels, planner, compiler snapshot, quality, and provenance.
5. Geometric crossings never alter semantic connectivity.

## Tasks / Subtasks

- [x] Add failing tests for deterministic route facts and markers (AC: 1-5)
  - [x] Cover deterministic ordering, quality evidence, and route snapshot stability.
  - [x] Cover orthogonal segments, junction facts, crossing facts, and marker-bearing payloads.
  - [x] Prove crossings do not imply connectivity and no renderer-owned fallback route is accepted.
- [x] Emit canonical RouteFact snapshots through the compiler path (AC: 1-5)
  - [x] Preserve connection, endpoint, route-intent, selected-channel, planner, snapshot, quality,
    and provenance evidence.
  - [x] Keep route facts compiler-owned and transient.
  - [x] Keep route proof normalized before it reaches presentation transport.
- [x] Preserve marker-bearing presentation payloads for route evidence (AC: 1-5)
  - [x] Carry marker keys through presentation occurrence and connector payloads.
  - [x] Keep marker payloads traceable to Athena source and representation facts.
  - [x] Keep route markers non-authoritative and derive them from compiler facts.
- [x] Run story evidence gate (AC: 1-5)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Route facts already flow through the existing compiler path. The main seams are
  `AthenaM35CabinetProjectionCompiler`, `PresentationModelDeriver`, `AthenaProfessionalDrawingCompiler`,
  and the route model in `kernel/routing-model`.
- `RouteFactSnapshot` owns deterministic route ordering and explicit crossing/junction proof.
- Presentation payloads carry `markerKeys`; those markers stay derived from Athena facts and never
  become source authority.
- This story must not reintroduce XML metadata authority, raw SVG authority, or renderer-owned route
  truth.
- No new route-planning algorithm is required here; 4-3 is about proof emission and canonical fact
  shape.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
  - `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
  - `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
  - `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/`
  - `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
- Keep text assets UTF-8.
- Do not add legacy XML compatibility paths.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 4.3, FR-13, FR-27, FR-28, FR-40.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-13, FR-27, FR-28,
  FR-40, NFR-1, NFR-3, NFR-4, NFR-8, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-7, AD-9, AD-10.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
  - canonical route facts, bundles, junctions, crossings, and quality.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`
  - Cabinet route snapshot emission and proof assembly.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
  - marker-bearing presentation routing payloads and route-fact snapshot bridge.
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`
  - route-fact snapshot contract coverage.
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFactsTest.kt`
  - canonical route fact ordering and proof structure.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

### Completion Notes List

- Canonical route facts already exist in the compiler path and are emitted deterministically.
- Route facts carry connection, endpoint, route-intent, selected-channel, planner, snapshot,
  quality, and provenance evidence through the route snapshot path.
- Route-junction and route-crossing facts remain explicit and separate from connectivity meaning.
- Marker-bearing presentation payloads already flow from compiler facts and stay non-authoritative.
- Verified with the existing full test pass, encoding audit, and `git diff --check`.

### File List

- _bmad-output/implementation-artifacts/m36/4-3-emit-deterministic-route-facts-and-markers.md
- _bmad-output/implementation-artifacts/m36/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFactsTest.kt

## Change Log

- 2026-07-29: Created M36-E4.S3 story for deterministic route facts and markers.
