---
baseline_commit: 5c22bae
---

# Story 4.1: Define the customer-facing acceptance fixture

Status: review

## Story

As a reviewer,
I want a small proof fixture that represents a real engineering sheet,
so that I can judge whether Athena looks professional without needing a manual demo.

## Acceptance Criteria

1. Given the acceptance fixture set, when I inspect the rendered result, it looks like a serious engineering artifact.
2. The fixture stays grounded in governed source data.
3. The fixture set is small and focused enough to be used as a repeatable acceptance baseline.
4. The fixture supports M20 sheet composition, layout rules, and viewport behavior coverage where needed.
5. No cabinet preview or repository/import scope is required.

## Tasks / Subtasks

- [x] Define the acceptance fixture shape (AC: 1, 2, 3)
  - [x] Pick the smallest governed example that still looks like a real sheet.
  - [x] Keep the fixture local and reviewable.
- [x] Tie the fixture to M20 coverage (AC: 3, 4)
  - [x] Reuse the composition and layout facts already introduced in Epic 1 and 2.
  - [x] Keep the acceptance fixture aligned with the view surface.
- [x] Preserve scope boundaries (AC: 5)
  - [x] Do not make the fixture depend on cabinet preview.
  - [x] Do not turn it into a repository/import proof.

## Dev Notes

### Current State

- M19 already has a governed proof corpus for schematic-sheet behavior.
- M20 needs a customer-facing acceptance fixture that is visibly professional, not just technically correct.

### Architectural Guardrails

- Follow M20 AD-2 and AD-7.
- The fixture should prove the sheet composition contract and the layout model together.

### Project Structure Notes

- Likely update targets:
  - `examples/m20/`
  - `ide/theia-frontend/scripts/*m20*.test.mjs`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/*Test.kt`
- Keep the fixture minimal but representative.

### Testing Requirements

- Treat the fixture as the baseline for acceptance and regression checks.
- Keep the proof corpus small enough to maintain manually.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 4, Story 4.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-7]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-7]
- [Source: `docs/usages/m19-proof-usage.md`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- RED: `node --test ide/theia-frontend/scripts/athena-m20-acceptance-fixture.test.mjs` failed until a dedicated acceptance fixture alias was added.
- GREEN: `node --test ide/theia-frontend/scripts/athena-m20-acceptance-fixture.test.mjs` passed once the alias reused the governed schematic proof corpus.
- Verification: `yarn test` from `ide/theia-frontend` passed all 66 frontend scripted checks.

### Completion Notes List

- Added a customer-facing acceptance fixture alias at `examples/m20/acceptance-sheet-proof/` that reuses the governed schematic-sheet proof.
- Kept the acceptance fixture local, small, and explicitly tied to the existing M20 composition/layout proof.
- No cabinet preview or repository/import scope was introduced.

### File List

- `_bmad-output/implementation-artifacts/m20/4-1-define-the-customer-facing-acceptance-fixture.md`
- `examples/m20/README.md`
- `examples/m20/acceptance-sheet-proof/README.md`
- `examples/m20/acceptance-sheet-proof/ready-sheet.diagram.mjs`
- `ide/theia-frontend/scripts/athena-m20-acceptance-fixture.test.mjs`

## Change Log

- 2026-07-17: Defined the M20 customer-facing acceptance fixture as a governed alias of the schematic proof corpus.
