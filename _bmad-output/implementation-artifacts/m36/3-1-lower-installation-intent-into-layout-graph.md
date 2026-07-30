---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E3.S1: Lower Installation Intent Into Layout Graph

Status: done

## Story

As the compiler,
I want to lower semantic, representation, and physical facts into a transient Layout Graph,
so that placement can be evaluated without turning coordinates into project truth.

**Requirements:** FR-14, FR-16.

## Acceptance Criteria

1. The transient Layout Graph contains occurrences, bounds, Ports, Anchors, constraints,
   obstacles, relationships, provenance, and explicit constraint owner and strength.
2. Authored placement intent and overrides remain separate from derived renderer coordinates.
3. The graph is disposable compiler IR, never persisted project authority or renderer-owned state.
4. The Layout Graph exposes enclosure, rail, duct, mount, clearance, terminal-group, and
   route-channel facts required by Cabinet policy.
5. Lowering is deterministic for the same source revision, package snapshot, physical snapshot,
   planner configuration, and compiler snapshot.

## Tasks / Subtasks

- [x] Add failing tests for transient Layout Graph lowering and constraint ownership (AC: 1-5)
  - [x] Cover one valid Layout Graph proof with occurrences, bounds, Ports, Anchors, constraints,
        obstacles, relationships, provenance, and owner/strength.
  - [x] Cover a rejection path for missing bounds, invalid containment, or unplaced occurrence
        evidence before downstream composition consumes the graph.
  - [x] Prove the graph stays transient and does not leak as persisted project truth or renderer
        transport.
- [x] Implement the compiler-owned Layout Graph lowering boundary (AC: 1-5)
  - [x] Lower semantic connectivity, representation bindings, and physical installation facts into
        a transient Layout Graph.
  - [x] Preserve constraint ownership and strength through lowering.
  - [x] Keep authored placement intent separate from derived coordinates and do not add planner
        authority in this story.
- [x] Integrate validation and diagnostic coverage for the lowerer (AC: 1-5)
  - [x] Report overlap, containment, orientation, clearance, bounds, and unplaced-occurrence
        diagnostics before routing or composition sees the graph.
  - [x] Keep the change scoped to layout lowering and diagnostics; do not expand into route
        realization, Cabinet routing, or renderer work.
- [x] Run story evidence gate (AC: 1-5)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. This story must stay inside transient layout lowering and diagnostics.
- The existing layout vocabulary lives in `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`.
  `LayoutDocument`, `LayoutNode`, `LayoutRelationship`, `LayoutConstraint`, and `LayoutIntentSnapshot`
  already express pre-solver layout intent, but M36 needs a transient Layout Graph boundary for
  Cabinet lowering.
- The current Cabinet projection entry point lives in
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`.
  It currently lowers physical installation IR, applies constraint evaluation, and then moves into
  composition/routing. This story should insert transient Layout Graph lowering before any route
  realization.
- Physical installation facts live in `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/`.
  `PhysicalInstallationTopologyCompiler` already lowers installation intent into `PhysicalInstallationIRV0`.
  Use that compiler-owned physical IR as the source for layout lowering rather than inventing a new
  semantic scanner.
- Keep the Layout Graph disposable and snapshot-bound. Do not persist it as project truth, expose it
  through LSP raw transport, or let the renderer own it.
- Preserve the architecture rule that constraint ownership remains explicit: Semantic,
  Representation, Physical, and Layout Preference constraints must stay distinguishable after
  lowering.
- Run RED first, then sequential Gradle verification. Do not run Gradle tasks in parallel.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/`
- Keep text assets UTF-8.
- Keep scope out of routing realization, renderer, and governed edit work for this story.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 3.1, FR-14, FR-16.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-14, FR-15, FR-16,
  FR-17, FR-18, NFR-1, NFR-3, NFR-4, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-3, AD-4, AD-8, transient IR and constraint-ownership rules.
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
  - existing layout intent and graph vocabulary.
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalInstallationTopologyCompiler.kt`
  - physical installation IR lowering boundary.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`
  - current Cabinet compilation flow and insertion point.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References
- Lowerer boundary compiled with transient `LayoutGraphV0` output and deterministic ordering.
- Added focused lowerer tests for happy path, missing bounds, invalid containment, and missing material evidence.
- Verified compiler sample path still renders and stays separate from the transient layout graph result.

### Completion Notes List
- Implemented compiler-owned transient Layout Graph lowering for M36-3-1.
- Preserved explicit constraint ownership/strength across semantic, representation, physical, and layout-preference facts.
- Added deterministic lowerer coverage plus rejection tests for missing bounds, invalid containment, and unplaced occurrence evidence.
- Verified with `./gradlew.bat --no-daemon --console=plain :kernel:compiler:test`, `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`, and `git diff --check`.

### Change Log
- 2026-07-29: Added transient Layout Graph lowering boundary and regression tests for M36 cabinet layout proofs.

### File List
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaLayoutGraphLowerer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaLayoutGraphLowererTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM35DedicatedCabinetSampleTest.kt
- kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutGraphModels.kt
