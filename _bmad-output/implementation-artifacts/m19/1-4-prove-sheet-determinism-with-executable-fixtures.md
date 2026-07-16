---
baseline_commit: 5f65bab3ff7c3f1d5e3452b9fb2bcf30cfad0006
---

# Story 1.4: Prove sheet determinism with executable fixtures

Status: done

## Story

As a reviewer,
I want a small executable proof corpus for the schematic sheet,
so that I can verify the milestone without relying on a manual demo.

## Acceptance Criteria

1. Given local M19 proof fixtures, executable tests verify schematic rendering, deterministic sheet IR, and stable output from the same input state.
2. The proof corpus lives under `examples/m19` and stays small, local, and governed.
3. Repeated runs on the same fixture state produce the same sheet chrome, element placement, and canonical identity mapping.
4. The proof corpus covers the visible schematic sheet workflow only; it does not require cabinet preview, ecosystem expansion, or protocol/layout-stack selection.
5. Fixture-driven checks preserve canonical subject and occurrence ids across the sheet model, rendered subject mapping, and cross-reference presentation.
6. The implementation ships with deterministic executable tests that can run from the repository fixtures without manual setup beyond the repo checkout.

## Tasks / Subtasks

- [x] Establish the M19 proof corpus on disk (AC: 1, 2, 4)
  - [x] Create a small governed `examples/m19` proof corpus with local fixture data and a short README.
  - [x] Keep the corpus schematic-first and explicitly local.
  - [x] Reuse the existing M18 proof-corpus pattern for structure and scope boundaries.
- [x] Add executable determinism checks for the schematic sheet (AC: 1, 3, 5, 6)
  - [x] Add or update tests that load the local fixtures and build the same governed sheet output twice.
  - [x] Assert `sheetChrome`, rendered node/edge mapping, and canonical ids remain stable across repeated runs.
  - [x] Assert fixture-driven output does not invent cabinet preview, registry, or frontend-owned semantic truth.
- [x] Prove the corpus stays bounded and maintainable (AC: 2, 4)
  - [x] Keep the fixture set small enough to stay maintainable.
  - [x] Keep the proof corpus local and executable from repository fixtures only.
  - [x] Keep the milestone focused on schematic credibility, not protocol or ecosystem selection.

## Dev Notes

### Current State

- Story 1.3 already added governed `sheetChrome` data to the Theia workbench model.
- Story 1.3 already renders the workbench SVG inside a transformed schematic sheet surface with a frame, grid, title block, and cross-reference markers.
- The frontend model test already proves repeated builds of the same governed diagram are deterministic.
- M18 already established the local-proof-corpus pattern under `examples/m18` with small executable fixture sets.

### Architectural Guardrails

- Semantic authority stays upstream in Athena.
- The proof corpus must exercise governed fixture output, not frontend-local reconstruction.
- The corpus should prove deterministic sheet IR and sheet chrome using local fixtures only.
- Cabinet preview stays deferred from M19.
- Final protocol/layout-stack selection stays out of M19.
- Keep the existing Athena/Theia boundary intact.

### Project Structure Notes

- Likely update targets:
  - `examples/m19/README.md`
  - `examples/m19/schematic-sheet-proof/`
  - `ide/theia-frontend/scripts/athena-m19-sheet-proof.test.mjs`
  - `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/`
- Reuse the existing M18 proof-corpus layout style instead of inventing a new fixture shape.

### Testing Requirements

- Use the repository-pinned stack only: Java 25, Kotlin 2.4.0, Gradle 9.6.1, and the existing Theia frontend toolchain.
- Run verification sequentially on Windows; never overlap `gradlew` invocations.
- Prefer executable fixture checks over manual inspection.
- Keep the proof corpus small and governed.
- If text files change, run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 1, Story 1.4]
- [Source: `_bmad-output/implementation-artifacts/m19/1-3-render-credible-schematic-elements.md` - current sheet chrome and deterministic model baseline]
- [Source: `_bmad-output/implementation-artifacts/m19/1-2-project-schematic-content-into-sheet-ir.md` - projection-state publication contract baseline]
- [Source: `_bmad-output/implementation-artifacts/m19/1-1-establish-sheet-identity-and-page-structure.md` - current sheet contract baseline]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-3, FR-6, FR-7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md` - Sheet IR, proof corpus boundaries]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-3, AD-6, AD-9]
- [Source: `examples/m18/README.md` - prior local proof-corpus pattern]
- [Source: `examples/m18/repository-proof/README.md` - repository-backed proof corpus pattern]
- [Source: `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 epics, PRD, addendum, architecture spine, and the M18 proof-corpus pattern.
- Scoped to a small local proof corpus and executable determinism checks.
- The M19 sheet surface and `sheetChrome` model already exist from Story 1.3 and should be reused.
- Red phase: `node --test scripts/athena-m19-sheet-proof.test.mjs` failed because `examples/m19/schematic-sheet-proof/ready-sheet.diagram.mjs` did not exist.
- Focused verification: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-m19-sheet-proof.test.mjs }` passed.
- Regression verification: `yarn test` in `ide/theia-frontend` passed 53 tests.
- Encoding verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added a small local M19 proof corpus under `examples/m19` with a governed schematic sheet diagram fixture.
- Added an executable frontend proof test that loads the local fixture and verifies stable sheet chrome, rendered node/edge identity mapping, cross-reference presentation, and scope boundaries across repeated runs.
- Kept the corpus schematic-first and explicitly out of cabinet preview, registry/import ecosystem, and protocol/layout-stack selection work.

### File List

- `_bmad-output/implementation-artifacts/m19/1-4-prove-sheet-determinism-with-executable-fixtures.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `examples/m19/README.md`
- `examples/m19/schematic-sheet-proof/README.md`
- `examples/m19/schematic-sheet-proof/ready-sheet.diagram.mjs`
- `ide/theia-frontend/scripts/athena-m19-sheet-proof.test.mjs`

### Change Log

- 2026-07-16: Added local M19 schematic proof corpus and deterministic executable frontend proof test.
