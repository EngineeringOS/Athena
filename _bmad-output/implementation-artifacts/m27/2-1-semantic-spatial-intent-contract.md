---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 2.1: Semantic Spatial Intent Contract

Status: done

## Story

As an Athena engineer,
I want a Semantic Spatial Intent contract for 2D electrical schematic projection,
so that layout and routing behavior is driven by engineering meaning instead of renderer heuristics.

## Acceptance Criteria

1. Given the M27 projection pipeline consumes the semantic model, when spatial intent is derived,
   then the contract represents direction, terminal side, lane preference, grouping, separation,
   component avoidance, route ordering, priority, confidence, and constraint source.
2. Given spatial intent is serialized into proof or diagnostic output, when it is inspected, then it
   contains no persisted raw canvas coordinates and preserves the authority chain from `.athena`
   source through compiler-owned facts.
3. The M27 contract is explicitly limited to 2D electrical schematic projection and does not create
   a general spatial, CAD, 3D, cabinet, factory layout, or physical-routing kernel.

## Tasks / Subtasks

- [x] Establish the contract location and module boundary (AC: 1, 3)
  - [x] Add a focused `kernel/spatial-model` module upstream of layout and routing.
  - [x] Keep the module data-only; no solver, renderer, backend, or source mutation authority.
- [x] Define semantic spatial intent vocabulary (AC: 1, 2, 3)
  - [x] Model projection scope, subject refs, direction, side, lane preference, ordering, grouping,
        separation, avoidance, priority, confidence, and constraint source.
  - [x] Require canonical semantic identity references and reject blank projection aliases.
  - [x] Avoid raw x/y coordinates, route points, DOM ids, SVG ids, or backend ids.
- [x] Add deterministic snapshot behavior (AC: 2)
  - [x] Canonicalize snapshot ordering by priority, source, scope, subject, relation, and intent id.
  - [x] Reject mixed projection scopes inside one snapshot.
- [x] Add contract regression coverage (AC: 1, 2, 3)
  - [x] Verify the M27 vocabulary exists and remains coordinate-free.
  - [x] Verify confidence bounds and deterministic canonical ordering.

## Dev Notes

- This story intentionally adds `kernel/spatial-model` rather than placing spatial intent inside
  `routing-model`, because M27 architecture states spatial intent is upstream of both layout and
  routing.
- The contract is narrow by construction: `SemanticSpatialProjectionScope` only admits
  `ELECTRICAL_SCHEMATIC_2D` in M27.
- No `.athena` syntax is introduced.
- No Theia DOM/SVG/canvas concepts are allowed in this model.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-20: Implemented after Story 1.3 graph-view sizing failure note clarified that visual
  fixes must keep semantic projection authority upstream of DOM/SVG evidence.

### Completion Notes List

- Added `kernel/spatial-model` as a focused contract module upstream of layout and routing.
- `SemanticSpatialIntent` models M27-required direction, terminal side, lane preference, grouping,
  separation, component avoidance, route ordering, priority, confidence, and constraint source.
- The contract is explicitly limited to `ELECTRICAL_SCHEMATIC_2D` for M27 and carries no raw
  canvas coordinates, route points, DOM ids, SVG ids, or backend-specific ids.
- Canonical snapshot ordering is deterministic and covered by tests.

### File List

- `_bmad-output/implementation-artifacts/m27/2-1-semantic-spatial-intent-contract.md`
- `settings.gradle.kts`
- `kernel/spatial-model/build.gradle.kts`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SemanticSpatialIntentModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SemanticSpatialIntentModelTest.kt`

## Change Log

- 2026-07-20: Created and implemented the M27 Semantic Spatial Intent contract story.
- 2026-07-20: Closed Story 2.1 after spatial-model contract tests and encoding audit passed.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test` - passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - passed.
