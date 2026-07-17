---
baseline_commit: 0dc5446
---

# Story 4.3: Keep the deferred boundaries explicit

Status: review

## Story

As a product reviewer,
I want M20 to stay visibly bounded,
so that cabinet preview, repository/import work, IEC breadth, and layout-stack selection remain deferred.

## Acceptance Criteria

1. Given the M20 PRD and architecture, when I inspect scope, the deferred boundaries remain explicit.
2. No story in M20 requires cabinet preview authoring.
3. No story in M20 requires repository/import ecosystem behavior.
4. No story in M20 chooses a final layout stack or layout engine.
5. No story in M20 expands to full IEC breadth or frontend-owned semantic resolution.

## Tasks / Subtasks

- [x] Keep the boundary language visible in docs (AC: 1, 2, 3, 4, 5)
  - [x] Keep scope notes consistent across PRD, architecture, epics, and stories.
  - [x] Preserve explicit deferrals in acceptance criteria.
- [x] Add boundary checks in tests where useful (AC: 1, 2, 3, 4, 5)
  - [x] Verify the deferred work stays out of the proof corpus and story targets.

## Dev Notes

### Current State

- M19 already deferred cabinet preview and ecosystem expansion.
- M20 adds layout-fidelity work but still must not reopen stack-selection or platform-expansion work.

### Architectural Guardrails

- Follow M20 AD-6, AD-8, AD-9, and AD-10.
- Keep the milestone inside the current Athena/Theia shell and outside layout-engine selection.

### Project Structure Notes

- Likely update targets:
  - `_bmad-output/implementation-artifacts/m20/epics.md`
  - `_bmad-output/implementation-artifacts/m20/sprint-status.yaml`
  - boundary assertions in M20 tests where appropriate
- Keep boundary wording exact and repeatable.

### Testing Requirements

- Add or update boundary assertions only where they help prevent scope drift.
- Keep the proof corpus free of deferred-work fixtures.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 4, Story 4.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-8, Non-Goals]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-6, AD-8, AD-9, AD-10]
- [Source: `_bmad-output/implementation-artifacts/m19/3-1-make-cabinet-preview-a-deferred-boundary.md`]
- [Source: `_bmad-output/implementation-artifacts/m19/3-2-keep-ecosystem-expansion-out-of-m19.md`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- RED: `node --test ide/theia-frontend/scripts/athena-m20-boundary.test.mjs` initially failed because the test was resolving the repo root from the wrong working directory.
- GREEN: `node --test ide/theia-frontend/scripts/athena-m20-boundary.test.mjs` passed after the boundary test resolved the repo root adaptively.
- Verification: `yarn test` from `ide/theia-frontend` passed all 68 frontend scripted checks.

### Completion Notes List

- Added an executable M20 boundary test that checks the PRD, architecture spine, epics, story text, and acceptance fixture docs for explicit deferred boundaries.
- Kept cabinet preview, repository/import, full IEC breadth, and stack-selection work explicitly deferred.
- No new semantic model or stack decision was introduced.

### File List

- `_bmad-output/implementation-artifacts/m20/4-3-keep-the-deferred-boundaries-explicit.md`
- `ide/theia-frontend/scripts/athena-m20-boundary.test.mjs`

## Change Log

- 2026-07-17: Added executable boundary checks to keep the M20 deferred work visibly out of scope.
