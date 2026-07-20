---
baseline_commit: b699dda601e216033ed0728d610042887aa82561
---

# Story 1.3: Bind the proof corpus to the sheet composition model

Status: done

## Story

As a reviewer,
I want the proof fixtures to exercise the composition model,
so that the milestone proves governed sheet composition instead of only a visual screenshot.

## Acceptance Criteria

1. Given the local M20 proof corpus, when tests run, they assert sheet composition facts as well as rendered output.
2. The fixture set stays small, local, and governed.
3. Proof coverage verifies frame, title block, zones, views, occurrences, and representation families where those are exposed.
4. The proof corpus builds on M19 fixtures without copying unrelated or unstable data.
5. No test relies on frontend-local semantic reconstruction.

## Tasks / Subtasks

- [x] Establish the M20 proof fixture location (AC: 2, 4)
  - [x] Create or reuse `examples/m20/` with a small governed fixture set.
  - [x] Keep fixture data deterministic and easy to review.
- [x] Add composition assertions (AC: 1, 3, 5)
  - [x] Assert sheet composition facts before render-only checks.
  - [x] Assert representation family and occurrence data where available.
- [x] Preserve M19 proof behavior (AC: 4, 5)
  - [x] Reuse M19 proof concepts without coupling to M19-only story names.
  - [x] Keep source/reveal identity paths intact.

## Dev Notes

### Current State

- M19 proof usage is documented in `docs/usages/m19-proof-usage.md`.
- M19 stories established local fixture-driven tests for sheet publication, rendering, selection, reveal, and boundary checks.
- M20 proof work should extend that pattern, not replace it with manual screenshot review.

### Architectural Guardrails

- Follow M20 AD-2 and AD-7.
- Proof corpus must validate governed data before visible rendering.
- Keep the proof local; do not introduce public registry/import behavior.

### Project Structure Notes

- Likely update targets:
  - `examples/m20/`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/*Test.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/*Test.kt`
  - `ide/theia-frontend/scripts/*m20*.test.mjs`
- Keep fixtures small enough that failure diffs are useful.

### Testing Requirements

- Add deterministic tests for composition facts.
- Add frontend proof tests only where the IDE consumes the new composition fields.
- Run encoding audit after creating fixture or doc files.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 1, Story 1.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-1, FR-2, FR-7]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-7]
- [Source: `docs/usages/m19-proof-usage.md`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- `git rev-parse HEAD`
- `node --test scripts/athena-m20-sheet-proof.test.mjs`
- `yarn test`
- `powershell -ExecutionPolicy Bypass -File .\\tools\\encoding-audit.ps1`

### Completion Notes List

- Added a governed M20 proof corpus under `examples/m20/` that reuses the M19 schematic fixture and layers in explicit sheet-composition facts.
- Added a focused frontend proof test that asserts composition facts before render checks and then verifies rendered sheet selection and title-block behavior.
- Kept the proof corpus local and small, with no registry, repository, or frontend-owned semantic reconstruction behavior.

### File List

- `examples/m20/README.md`
- `examples/m20/schematic-sheet-proof/README.md`
- `examples/m20/schematic-sheet-proof/ready-sheet.diagram.mjs`
- `ide/theia-frontend/scripts/athena-m20-sheet-proof.test.mjs`

### Change Log

- Introduced the M20 proof corpus and verified that sheet composition facts are exercised before the render path.
- Reused the M19 proof fixture as the semantic base so the new corpus stays governed and minimal.
- Added regression coverage for composition, representation family, and render-selection behavior.

## Status

review
