---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 2.2: Routing Backend Boundary And Normalized Route Facts

Status: done

## Story

As an Athena engineer,
I want routing backends hidden behind an Athena-owned adapter boundary,
so that external routing tools can assist later without owning semantic meaning or persisted layout truth.

## Acceptance Criteria

1. Given route generation is invoked for the M27 sample, when a routing backend or internal router
   produces candidate geometry, then Athena normalizes the result into owned route geometry facts.
2. Given a backend result crosses the adapter boundary, semantic meaning, terminal identity,
   document occurrence identity, source mutation, and persisted layout truth remain outside the
   backend.
3. Given an external backend is absent, when M27 routing tests and sample proof run, then the
   built-in route path still produces deterministic normalized route facts.
4. The adapter boundary is covered by a minimal contract test or fixture.

## Tasks / Subtasks

- [x] Define the routing backend adapter seam (AC: 1, 2, 4)
  - [x] Add backend id, authority claims, backend result, adapter interface, and boundary normalizer.
  - [x] Make backend authority claims explicit and rejected by the boundary.
- [x] Keep Athena v0 as the accepted proof backend (AC: 1, 3)
  - [x] Add `AthenaV0RoutingBackendAdapter` that delegates to `AthenaRouteEngineV0`.
  - [x] Normalize adapter output into `RouteFactSnapshot.canonical`.
- [x] Add boundary regression coverage (AC: 2, 3, 4)
  - [x] Prove route facts are deterministic and canvas-truth-free.
  - [x] Prove unsafe backend authority claims are rejected.
  - [x] Prove invented/unrequested route facts are rejected.

## Dev Notes

- This story does not introduce ELK, libavoid, yFiles, or any external routing dependency.
- The backend seam is a boundary, not a dependency decision.
- The renderer remains downstream of normalized Athena `RouteFact` output.
- No `.athena` syntax is introduced.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-20: Implemented after Story 2.1 established the upstream Semantic Spatial Intent contract.

### Completion Notes List

- Added `RoutingBackendAdapter` and `RoutingBackendBoundary` as the M27 adapter seam.
- Added `AthenaV0RoutingBackendAdapter` so the accepted proof path still uses Athena-owned
  deterministic routing when no external backend exists.
- Backend results are normalized into `RouteFactSnapshot.canonical`.
- The boundary rejects backend claims over semantic connection meaning, source mutation, terminal
  identity, document occurrence identity, and persisted layout truth.
- The boundary rejects canvas-owned route truth, route facts for another snapshot, and invented
  route facts not requested by Athena.

### File List

- `_bmad-output/implementation-artifacts/m27/2-2-routing-backend-boundary-and-normalized-route-facts.md`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RoutingBackendBoundary.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RoutingBackendBoundaryTest.kt`

## Change Log

- 2026-07-20: Created and implemented the M27 routing backend boundary story.
- 2026-07-20: Closed Story 2.2 after routing-model tests and encoding audit passed.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` - passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - passed.
