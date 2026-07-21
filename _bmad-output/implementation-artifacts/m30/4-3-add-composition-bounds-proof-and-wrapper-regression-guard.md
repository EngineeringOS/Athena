---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 4.3
epic: 4
title: Add Composition Bounds Proof And Wrapper Regression Guard
---

# Story 4.3: Add Composition Bounds Proof And Wrapper Regression Guard

## Status

Done

## Story

As a maintainer,
I want automated guards against known visual failures,
so that large viewBox, duplicate elements, repeated labels, and wrapper borders do not return.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given resolved presentation content, when bounds proof runs, then derived bounds match actual content plus governed margins.
2. Given accepted proof output, when inspected, then hard-coded large viewBox, off-screen duplicate elements, repeated label text, and visible non-symbol wrapper borders are rejected.
3. Given the proof fails, when diagnostics are shown, then the failing visual invariant is explicit.

## Tasks/Subtasks

- [x] Add structured bounds proof. (AC: 1)
- [x] Add regression guards for hard-coded viewBox, off-screen duplicates, repeated text, and wrapper borders. (AC: 2)
- [x] Expose clear proof diagnostics for failures. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- This story directly protects against the M27/M29 SVG failures called out by the user.
- Do not rely only on screenshot diff for structural invariants.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.CompositionBoundsProofGuardTest"` failing on missing bounds proof guard contracts.
- 2026-07-21: GREEN confirmed with focused `CompositionBoundsProofGuardTest` after adding content bounds, viewBox, proof diagnostics, and guard logic.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Review verification passed with focused `CompositionBoundsProofGuardTest` and full module test.

### Completion Notes

- Added structured bounds proof deriving viewBox from actual content plus governed margin.
- Added explicit diagnostics for hard-coded viewBox, off-screen duplicate content, repeated label text, and visible normal-state wrapper borders.
- Guard is structural and does not rely on screenshots.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed the known M27/M29 regressions remain covered by explicit structural diagnostics instead of visual guesswork.

## File List

- `_bmad-output/implementation-artifacts/m30/4-3-add-composition-bounds-proof-and-wrapper-regression-guard.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/CompositionBoundsProofGuard.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/CompositionBoundsProofGuardTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added composition bounds proof and wrapper regression guard.
- 2026-07-21: Closed review after bounds-proof verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
