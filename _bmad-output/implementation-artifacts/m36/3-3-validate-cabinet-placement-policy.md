---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E3.S3: Validate Cabinet Placement Policy

Status: done

## Story

As an engineer or AI agent,
I want invalid placement rejected with evidence,
so that Cabinet composition never hides a physical conflict behind fallback coordinates.

**Requirements:** FR-17, FR-21, FR-23, FR-24.

## Acceptance Criteria

1. A planner placement proposal is rejected when Cabinet physical-policy validation finds overlap,
   containment, orientation, clearance, bounds, or unplaced-occurrence violations.
2. Planner output cannot mutate source, own semantic identity, redefine package metadata, or persist
   layout truth.
3. Failed evaluation reports violated constraints, source declarations, planner identity, and
   snapshot identity.
4. No silent fallback placement is produced, and accepted PlacementFacts preserve complete provenance.

## Tasks / Subtasks

- [x] Add failing tests for cabinet placement policy validation (AC: 1-4)
  - [x] Cover overlap, containment, orientation, clearance, bounds, and unplaced-occurrence failure evidence.
  - [x] Cover a rejected or duplicate placement proposal path before accepted facts are emitted.
  - [x] Prove planner output never gains source mutation or semantic authority through policy validation.
- [x] Introduce the cabinet placement policy contract (AC: 1-4)
  - [x] Validate the planner-owned placement proposal against physical installation rules.
  - [x] Normalize failures into Athena diagnostics with source, planner, and snapshot evidence.
  - [x] Keep policy validation compiler-owned and disposable, never persisted as project truth.
- [x] Wire the deterministic cabinet policy gate (AC: 1-4)
  - [x] Reject overlap, containment, orientation, clearance, bounds, and unplaced occurrences.
  - [x] Preserve the optional future ELK adapter boundary without making it authoritative.
  - [x] Keep policy validation separate from route realization.
- [x] Run story evidence gate (AC: 1-4)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. Policy validation must not rewrite source or infer any missing authority.
- This story consumes placement facts from story 3-2 and validates them against physical cabinet
  rules before anything reaches route realization or rendering.
- Keep policy validation compiler-owned and snapshot-bound.
- ELK remains optional and non-authoritative.
- The M36 proof sample is milestone-local; do not reuse it as a shared artifact outside the M36
  cabinet path.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/`
- Keep text assets UTF-8.
- Keep scope out of route realization and governed edit work for this story.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 3.3, FR-17, FR-21, FR-23, FR-24.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-17, FR-21,
  FR-23, FR-24, NFR-1, NFR-3, NFR-4, NFR-8.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-4, AD-6, physical policy validation and planner adapter rules.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CabinetPlacementPolicyCompiler.kt`
  - cabinet placement policy validation seam.
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorV0.kt`
  - physical constraint diagnostics and proof generation.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/CabinetPlacementPolicyCompilerTest.kt`
  - policy validation coverage.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- Story file created from the M36-E3 backlog.
- Existing cabinet placement policy code already validates overlap, containment, orientation,
  clearance, bounds, and unplaced-occurrence evidence.

### Completion Notes List

- Cabinet placement policy validation is implemented and verified by the full regression suite.

### Change Log

- 2026-07-29: Created M36-E3.S3 story for cabinet placement policy validation.

### File List

- _bmad-output/implementation-artifacts/m36/3-3-validate-cabinet-placement-policy.md
