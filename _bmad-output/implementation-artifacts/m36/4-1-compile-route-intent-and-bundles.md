---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E4.S1: Compile Route Intent And Bundles

Status: done

## Story

As an engineer or AI agent,
I want to express routing requirements without drawing final segments,
so that the compiler can derive routes while preserving engineering intent.

**Requirements:** FR-12, FR-39.

## Acceptance Criteria

1. Route Intent supports required or preferred `through`, `avoid`, `bundle`, and priority
   constraints.
2. Route Intent contains no final segments, renderer coordinates, or implicit topology.
3. Typed Connections lower into Route Bundles without changing semantic connectivity.
4. Route Intent and bundles preserve source spans, constraint ownership, strength, and provenance.

## Tasks / Subtasks

- [x] Add failing tests for route intent and route bundle lowering (AC: 1-4)
  - [x] Cover authored `through`, `avoid`, `bundle`, and priority constraints.
  - [x] Prove route intent contains no final segments or renderer coordinates.
  - [x] Prove typed connections lower into bundles without mutating semantic connectivity.
- [x] Introduce the route intent contract (AC: 1-4)
  - [x] Model required/preferred routing constraints with owner, strength, and provenance.
  - [x] Keep route intent transient and compiler-owned.
  - [x] Keep Cabinet-specific channel policy out of generic route intent.
- [x] Lower connection facts into route bundles (AC: 3-4)
  - [x] Preserve connection identity, endpoint ports, compatibility evidence, and provenance.
  - [x] Emit deterministic bundle ordering and stable tie handling.
  - [x] Reject missing connection aliases before route realization.
- [x] Run story evidence gate (AC: 1-4)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. Route Intent is engineering intent, not route geometry.
- This story must not implement channel route realization, obstacle routing, marker drawing, or
  advanced optimization; those belong to stories 4-2 and 4-3.
- Keep Cabinet as the current proof consumer, but the route intent contract must remain reusable
  across EngineeringOS projections.
- The M36 proof sample must live under `examples/m36`; do not reuse M35 sample assets as the M36
  acceptance fixture.
- ELK remains optional and non-authoritative.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/`
  - `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
- Keep text assets UTF-8.
- Do not add XML compatibility paths.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 4.1, FR-12, FR-39.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-12, FR-39,
  NFR-1, NFR-3, NFR-4, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-7, route intent before route geometry.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrModels.kt`
  - transient connection IR from M36-E1.
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/`
  - reusable route intent, bundle, and route fact model target.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- Story file created from the M36-E4 backlog.
- Implemented route intent contract and compiler lowerer for M36-4-1.
- Verified with targeted route intent tests, full `test`, encoding audit, and `git diff --check`.

### Completion Notes List

- Added typed route intent constraints for `through`, `avoid`, `bundle`, and `priority`.
- Added compiler-owned lowering from `ConnectionIr` into deterministic route intents and bundles.
- Added tests proving no final route geometry appears in route intent and unresolved aliases fail early.
- Verified with `:kernel:routing-model:test --tests com.engineeringood.athena.routing.RouteIntentContractTest`, `:kernel:compiler:test --tests com.engineeringood.athena.compiler.RouteIntentLowererTest`, `test`, encoding audit, and `git diff --check`.

### Change Log

- 2026-07-29: Created M36-E4.S1 story for route intent and bundle lowering.
- 2026-07-29: Implemented M36-E4.S1 route intent contract and lowering; story moved to review.

### File List

- _bmad-output/implementation-artifacts/m36/4-1-compile-route-intent-and-bundles.md
- _bmad-output/implementation-artifacts/m36/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteIntentModels.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteIntentContractTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RouteIntentLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RouteIntentLowererTest.kt
