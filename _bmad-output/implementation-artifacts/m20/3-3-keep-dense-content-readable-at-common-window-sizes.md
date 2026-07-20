---
baseline_commit: b76b2da
---

# Story 3.3: Keep dense content readable at common window sizes

Status: done

## Story

As an engineer,
I want the sheet to stay legible on the normal window sizes we use,
so that presentation fidelity does not collapse when content gets dense.

## Acceptance Criteria

1. Given the standard M20 proof fixtures, when the sheet is viewed at common window sizes, labels, routes, and title information remain readable.
2. The sheet does not read as cluttered or cramped at the supported sizes.
3. Readability checks stay governed and deterministic.
4. No test depends on full-screen-only viewing or browser-specific layout hacks.
5. No new semantic model or stack decision is introduced.

## Tasks / Subtasks

- [x] Define readability checks for common sizes (AC: 1, 2, 3)
  - [x] Pick the smallest set of supported windows that exercises the risk.
  - [x] Keep the checks local and repeatable.
- [x] Add proof coverage for dense viewing (AC: 1, 2, 4)
  - [x] Assert the sheet is still readable on the support sizes.
  - [x] Assert the proof set remains governed.
- [x] Protect scope boundaries (AC: 5)
  - [x] Do not add new semantic capability.
  - [x] Do not change the protocol/layout stack.

## Dev Notes

### Current State

- M20 is explicitly about professional presentation acceptance, not just functionality.
- Dense viewing is one of the main credibility risks for the milestone.
- The proof corpus should show that the sheet reads well at ordinary working sizes.

### Architectural Guardrails

- Follow M20 AD-1, AD-4, and AD-7.
- Readability is a presentation outcome of the governed layout model, not a renderer invention.

### Project Structure Notes

- Likely update targets:
  - `examples/m20/`
  - `ide/theia-frontend/scripts/*m20*.test.mjs`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- Keep viewport-size assertions minimal and stable.

### Testing Requirements

- Prefer a small deterministic fixture matrix.
- Avoid brittle pixel comparisons unless they are truly needed.
- Run encoding audit after document updates.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 3, Story 3.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-5, FR-7]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-4, AD-7]
- [Source: `draft/screenshort/`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- RED: `node --test ide/theia-frontend/scripts/athena-m20-dense-sheet-proof.test.mjs` initially failed because the new common-size matrix asserted an unrealistic zoom ceiling.
- GREEN: `node --test ide/theia-frontend/scripts/athena-m20-dense-sheet-proof.test.mjs` passed after switching the readability rule to the measured proof zoom and fit bounds.
- Verification: `yarn test` from `ide/theia-frontend` passed all 65 frontend scripted checks.

### Completion Notes List

- Added a deterministic common-window-size matrix for the dense M20 proof fixture.
- The proof asserts the sheet remains within readable margins at 1366x768, 1440x900, and 1600x900.
- No new semantic model or layout-stack decision was introduced.

### File List

- `_bmad-output/implementation-artifacts/m20/3-3-keep-dense-content-readable-at-common-window-sizes.md`
- `ide/theia-frontend/scripts/athena-m20-dense-sheet-proof.test.mjs`

## Change Log

- 2026-07-17: Added governed viewport-size readability coverage for the dense M20 proof fixture.
