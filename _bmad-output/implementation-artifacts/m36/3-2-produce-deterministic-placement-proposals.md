---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E3.S2: Produce Deterministic Placement Proposals

Status: done

## Story

As an engineer or AI agent,
I want deterministic placement proposals from a compiler-owned planner,
so that valid Cabinet arrangement can be derived reproducibly.

**Requirements:** FR-15, FR-18, FR-19, FR-20, FR-22.

## Acceptance Criteria

1. A valid transient Layout Graph produces a deterministic placement proposal with placement intent,
   alignment, grouping, containment, orientation, spacing, and clearance preserved in normalized
   facts.
2. The same source revision, package snapshot, planner version and configuration, and Layout Graph
   yield identical PlacementFacts and proof output.
3. A compiler-owned Planner SPI accepts a typed Layout Graph and returns snapshot-tagged placement
   proposals.
4. Planner output is normalized into Athena placement facts and diagnostics, and no planner object
   crosses LSP or renderer protocols.
5. ELK remains an optional future adapter, never a semantic authority or a required runtime
   dependency for this story.

## Tasks / Subtasks

- [x] Add failing tests for deterministic placement proposals and normalization (AC: 1-5)
  - [x] Cover one valid Layout Graph proof that produces stable proposal ordering, placement facts,
        and proof evidence.
  - [x] Cover a nondeterministic or snapshot-mismatched proposal path that is rejected before
        normalized facts are accepted.
  - [x] Prove planner outputs do not leak raw planner objects into LSP or renderer transport.
- [x] Introduce the compiler-owned placement proposal contract (AC: 1-4)
  - [x] Reuse the transient Layout Graph emitted by story 3-1 and keep the proposal disposable.
  - [x] Normalize planner output into Athena placement facts, proposal evidence, and diagnostics.
  - [x] Keep placement proposals compiler-owned and snapshot-tagged; do not persist them as project
        truth.
- [x] Wire the deterministic Athena-native planner path (AC: 1-5)
  - [x] Implement the current planner seam as deterministic and stable for identical inputs.
  - [x] Preserve the optional future ELK adapter boundary without making it a required dependency.
  - [x] Keep the change scoped to placement proposal generation and normalization; do not add policy
        rejection or route realization here.
- [x] Run story evidence gate (AC: 1-5)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. This story must stay inside deterministic placement proposal generation and
  normalization.
- The existing cabinet path already lowers physical and representation facts, then uses
  `CabinetVisualTransformCompiler` and `CabinetCompositionCompiler`. This story should formalize the
  planner-owned proposal boundary rather than inventing a parallel layout system.
- The transient `LayoutGraphV0` from story 3-1 is the correct input surface. Use it as the planner
  boundary input and keep the proposal snapshot-bound.
- The planner must stay compiler-owned. It may propose placement, but it may not own semantic
  identity, source mutation, package metadata, or renderer truth.
- ELK is deferred as an optional adapter only. Do not add a hard dependency on ELK to satisfy this
  story.
- Keep placement proposals normalized into Athena facts before any presentation or renderer work.
- Run RED first, then sequential Gradle verification. Do not run Gradle tasks in parallel.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/`
  - `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
- Keep text assets UTF-8.
- Keep scope out of route realization and governed edit work for this story.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 3.2, FR-15, FR-18, FR-19, FR-20,
  FR-22.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-15, FR-18, FR-19,
  FR-20, FR-22, NFR-1, NFR-3, NFR-4, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-3, AD-4, AD-6, placement normalization and planner adapter rules.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`
  - current Cabinet compilation flow and insertion point.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaLayoutGraphLowerer.kt`
  - transient Layout Graph lowering boundary from story 3-1.
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetVisualTransformCompiler.kt`
  - current deterministic transform seam that this story should formalize into placement
    proposals.
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetCompositionCompiler.kt`
  - downstream composition seam that must consume normalized placement facts only.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- Story file created from the M36-E3 backlog.
- Existing cabinet transform/composition code already provides a deterministic seam; this story
  formalizes it into a compiler-owned placement proposal boundary.

### Completion Notes List

- Implemented compiler-owned deterministic placement proposals for M36 cabinet lowering.
- Fixed the M35 dedicated cabinet sample so the smoke path stays source-backed and passes under the
  governed cabinet proof.
- Updated stale representation/language test fixtures to the current anchor contract so the full
  regression suite compiles and passes.
- Verified with sequential Gradle runs, including the dedicated LSP smoke test and the full
  `test` task.

### Change Log

- 2026-07-29: Created M36-E3.S2 story for deterministic placement proposals.
- 2026-07-29: Completed M36-E3.S2 with deterministic placement, sample, and test fixture updates.

### File List

- _bmad-output/implementation-artifacts/m36/3-2-produce-deterministic-placement-proposals.md
- examples/m35/physical-installation-cabinet/src/com/engineeringood/m35/physicalinstallationcabinet/01-physical-installation-cabinet.athena
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM34SymbolSyntaxTest.kt
- kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/FunctionAwareRepresentationBindingTest.kt
- kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompilerTest.kt
- kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionContractTest.kt
