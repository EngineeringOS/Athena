---
baseline_commit: 8a634d4e3362446f2bcb196f6d8216212f047a09
---

# Story 3.2: Keep ecosystem expansion out of M19

Status: done

## Story

As a product reviewer,
I want public repository/import ecosystem work and full IEC breadth excluded from M19,
so that the milestone stays centered on a credible sheet workflow.

## Acceptance Criteria

1. Given the M19 PRD, when I inspect the non-goals and success framing, public repository/import ecosystem work is deferred out of M19.
2. Given the M19 addendum and architecture spine, when I inspect the milestone boundaries, full IEC breadth, public repository/import ecosystem work, and frontend-owned semantic resolution are excluded.
3. Given the M19 epics and sprint status, when I inspect the story set, no M19 story requires repository/import ecosystem work or a full IEC library program.
4. Given the executable guardrail tests, when the frontend test suite runs, the ecosystem-expansion boundary is checked from local planning artifacts.
5. The implementation does not add repository/import ecosystem behavior, full IEC library ingestion, frontend-owned semantic resolution, or any new protocol/layout-stack decision.

## Tasks / Subtasks

- [x] Create explicit ecosystem-expansion boundary checks (AC: 1, 2, 3, 4)
  - [x] Extend the existing deterministic local guardrail test to read the M19 PRD, addendum, architecture spine, epics, and sprint status.
  - [x] Assert the planning artifacts exclude repository/import ecosystem work, full IEC breadth, and frontend-owned semantic resolution from M19.
  - [x] Assert the implementation story set does not require repository/import ecosystem work or a full IEC library program.
- [x] Keep Epic 3 as scope discipline, not feature work (AC: 5)
  - [x] Do not add repository/import, registry, or marketplace code.
  - [x] Do not add IEC library ingestion or a full element catalog program.
  - [x] Do not change the final protocol/layout stack decision in M19.
- [x] Verify and record completion (AC: 4, 5)
  - [x] Run focused boundary tests.
  - [x] Run the full Theia frontend test suite.
  - [x] Run the repository encoding audit after text changes.

## Dev Notes

### Current State

- The M19 PRD explicitly frames public repository/import ecosystem work as out of scope.
- The M19 addendum says public repository/import ecosystem work is deferred.
- The M19 architecture spine AD-7 excludes ecosystem expansion from M19.
- The M19 epics include Story 3.2 specifically to keep ecosystem expansion out of the milestone.

### Architectural Guardrails

- M19 remains schematic-first.
- Full IEC breadth, public repository/import ecosystem work, and frontend-owned semantic resolution are out of M19.
- This story should add guardrails and verification only; it should not add repository, registry, marketplace, or symbol-library features.
- Theia remains the only frontend target for this story. Ignore desktop viewer and Kotlin Compose surfaces.

### Project Structure Notes

- Likely update targets:
  - `_bmad-output/implementation-artifacts/m19/3-2-keep-ecosystem-expansion-out-of-m19.md`
  - `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
  - `ide/theia-frontend/scripts/athena-m19-boundary.test.mjs`
- Prefer extending the existing boundary test rather than creating a second M19 boundary file.

### Testing Requirements

- Use `node --test scripts/athena-m19-boundary.test.mjs` for focused boundary verification after `yarn build` if needed by the suite.
- Run `yarn test` in `ide/theia-frontend` for regression.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text changes.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 3, Story 3.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-7, Non-Goals]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md` - Boundary Notes]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-7]
- [Source: `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 Epic 3, PRD FR-7, architecture AD-7, and the user's request to keep repository/import work out of M19.
- Red phase: the initial boundary check failed until the test matched the exact PRD/addendum wording for public package repository and full IEC element catalog.
- Focused verification: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-m19-boundary.test.mjs }` passed with both cabinet and ecosystem boundary clauses.
- Regression verification: `yarn test` in `ide/theia-frontend` passed 59 tests.
- Encoding verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Extended the executable M19 boundary guardrail so it now checks both deferred cabinet preview and excluded ecosystem expansion from local planning artifacts.
- The guardrail confirms the PRD, addendum, architecture spine, epics, and sprint status all keep public repository/import work, full IEC breadth, and frontend-owned semantic resolution out of M19.
- No repository/import/registry implementation, IEC catalog ingestion, or protocol/layout-stack decision was added.

### File List

- `_bmad-output/implementation-artifacts/m19/3-2-keep-ecosystem-expansion-out-of-m19.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m19/epics.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md`
- `ide/theia-frontend/scripts/athena-m19-boundary.test.mjs`

### Change Log

- 2026-07-16: Created story context for the ecosystem-expansion boundary.
- 2026-07-16: Added executable ecosystem-expansion scope guardrail for M19 planning artifacts.
