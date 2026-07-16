---
baseline_commit: 6761b4ac8978b39b17d767e86a18ebd952e45444
---

# Story 3.1: Make cabinet preview a deferred boundary

Status: review

## Story

As a product reviewer,
I want cabinet preview explicitly out of M19,
so that the milestone does not split focus away from the schematic sheet.

## Acceptance Criteria

1. Given the M19 PRD and addendum, when I inspect the scope, cabinet preview is explicitly deferred from M19.
2. Given the M19 architecture spine, when I inspect architectural decisions, cabinet preview is not part of the M19 MVP and any future cabinet work must reuse the same semantic/projection/sheet/identity contracts.
3. Given the M19 epics and sprint status, when I inspect the story set, no M19 story requires cabinet preview implementation.
4. Given the executable guardrail tests, when the frontend test suite runs, the cabinet-preview boundary is checked from local planning artifacts.
5. The implementation does not add cabinet preview behavior, desktop viewer behavior, Kotlin Compose work, protocol/layout-stack selection, or frontend-owned semantic resolution.

## Tasks / Subtasks

- [x] Create explicit cabinet-preview boundary checks (AC: 1, 2, 3, 4)
  - [x] Add a deterministic local test that reads M19 PRD, addendum, architecture spine, epics, and sprint status.
  - [x] Assert the PRD/addendum/architecture defer cabinet preview from M19.
  - [x] Assert the implementation story set does not require a cabinet preview deliverable.
- [x] Keep Epic 3 as scope discipline, not feature work (AC: 5)
  - [x] Do not add renderer, Theia surface, Kotlin, or desktop-viewer cabinet preview code.
  - [x] Do not choose a final GLSP/Sprotty/ELK or alternative layout stack.
  - [x] Do not weaken the schematic-first wording in the planning artifacts.
- [x] Verify and record completion (AC: 4, 5)
  - [x] Run focused boundary tests.
  - [x] Run the full Theia frontend test suite.
  - [x] Run the repository encoding audit after text changes.

## Dev Notes

### Current State

- The M19 PRD already lists cabinet preview as out of scope for the MVP.
- The M19 addendum says cabinet preview is deferred from M19 and should not become a design-system mandate.
- The M19 architecture spine AD-5 states cabinet preview is deferred.
- The M19 epics include Story 3.1 specifically to make this scope boundary visible.
- The user explicitly requested front-end focus in the IDE/Theia path and to ignore desktop viewer and Kotlin Compose work.

### Architectural Guardrails

- M19 is schematic-first.
- Cabinet preview is not part of the M19 MVP.
- Any future cabinet preview must reuse semantic, projection, sheet, and identity contracts instead of introducing a second semantic model.
- This story should add guardrails and verification only; it should not implement cabinet preview.

### Project Structure Notes

- Likely update targets:
  - `_bmad-output/implementation-artifacts/m19/3-1-make-cabinet-preview-a-deferred-boundary.md`
  - `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
  - `ide/theia-frontend/scripts/athena-m19-boundary.test.mjs`
- Prefer a small Node test under the existing Theia frontend test harness so the boundary stays executable with `yarn test`.

### Testing Requirements

- Use `node --test scripts/athena-m19-boundary.test.mjs` for focused boundary verification after `yarn build` if needed by the suite.
- Run `yarn test` in `ide/theia-frontend` for regression.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text changes.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 3, Story 3.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-4, MVP out of scope]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md` - Boundary Notes]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-5]
- [Source: `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 Epic 3, PRD FR-4/FR-7, architecture AD-5/AD-7, and the user's Theia-only frontend boundary.
- Red phase: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-m19-boundary.test.mjs }` initially failed because the guardrail treated the boundary story itself as forbidden cabinet work.
- Focused verification: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-m19-boundary.test.mjs }` passed after the test allowed the explicit deferred-boundary story while still blocking implementation stories.
- Regression verification: `yarn test` in `ide/theia-frontend` passed 58 tests.

### Completion Notes List

- Added an executable M19 boundary test that reads local PRD, addendum, architecture spine, epics, and sprint status artifacts.
- The test asserts cabinet preview remains deferred from M19 and blocks active cabinet-preview implementation stories while allowing this boundary-control story.
- No cabinet preview code, desktop viewer work, Kotlin Compose work, or protocol/layout-stack decision was added.

### File List

- `_bmad-output/implementation-artifacts/m19/3-1-make-cabinet-preview-a-deferred-boundary.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m19/epics.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md`
- `ide/theia-frontend/scripts/athena-m19-boundary.test.mjs`

### Change Log

- 2026-07-16: Created story context for the cabinet-preview deferred boundary.
- 2026-07-16: Added executable cabinet-preview scope guardrail for M19 planning artifacts.
