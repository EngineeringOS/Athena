---
baseline_commit: 9fcd97a
---

# Story 4.2: Add executable regression coverage for layout and interaction

Status: done

## Story

As a reviewer,
I want the layout and interaction behavior to be testable,
so that regressions are caught without subjective review alone.

## Acceptance Criteria

1. Given the M20 proof corpus, when automated tests run, layout acceptability and selection coherence are covered.
2. Repeated runs remain stable.
3. The tests are executable and local.
4. The coverage includes the new sheet composition, layout rules, and viewport behavior where relevant.
5. No test depends on manual demo steps.

## Tasks / Subtasks

- [x] Expand the executable regression suite (AC: 1, 2, 3)
  - [x] Add tests that run from local fixtures.
  - [x] Cover the new acceptance fixture and the dense fixture.
- [x] Make the coverage meaningful (AC: 1, 4, 5)
  - [x] Assert visible layout facts and interaction coherence.
  - [x] Avoid manual demo-only assertions.
- [x] Keep the regression suite stable (AC: 2, 3)
  - [x] Normalize deterministic outputs as needed.
  - [x] Keep failure diffs readable.

## Dev Notes

### Current State

- M19 already proved deterministic behavior across projection, runtime, and Theia seams.
- M20 should extend that discipline to presentation acceptability and viewport behavior.

### Architectural Guardrails

- Follow M20 AD-7 and AD-1.
- Tests must verify governed data, not frontend guesses.

### Project Structure Notes

- Likely update targets:
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/*Test.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/*Test.kt`
  - `ide/theia-frontend/scripts/*m20*.test.mjs`
- Reuse M19 testing patterns where possible.

### Testing Requirements

- Keep tests deterministic and local.
- Run verification sequentially on Windows.
- Run encoding audit after any text fixture changes.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 4, Story 4.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-7]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-7]
- [Source: `_bmad-output/implementation-artifacts/m19/1-4-prove-sheet-determinism-with-executable-fixtures.md`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- RED: `node --test ide/theia-frontend/scripts/athena-m20-acceptance-fixture.test.mjs` failed until the dedicated acceptance fixture alias existed.
- GREEN: `node --test ide/theia-frontend/scripts/athena-m20-regression-suite.test.mjs` passed after adding the local executable M20 regression suite.
- Verification: `yarn test` from `ide/theia-frontend` passed all 67 frontend scripted checks.

### Completion Notes List

- Added an explicit local regression suite that covers both the acceptance fixture and the dense fixture.
- The suite exercises layout facts, canonical selection targets, and repeatable fit behavior without manual demo steps.
- The coverage stays grounded in governed local fixtures.

### File List

- `_bmad-output/implementation-artifacts/m20/4-2-add-executable-regression-coverage-for-layout-and-interaction.md`
- `ide/theia-frontend/scripts/athena-m20-acceptance-fixture.test.mjs`
- `ide/theia-frontend/scripts/athena-m20-regression-suite.test.mjs`
- `ide/theia-frontend/scripts/athena-m20-dense-sheet-proof.test.mjs`

## Change Log

- 2026-07-17: Added local executable regression coverage for the M20 acceptance and dense fixtures.
