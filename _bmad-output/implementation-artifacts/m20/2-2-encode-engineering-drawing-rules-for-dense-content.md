---
baseline_commit: b699dda601e216033ed0728d610042887aa82561
---

# Story 2.2: Encode engineering drawing rules for dense content

Status: review

## Story

As an engineer,
I want spacing, routing, grouping, and label-avoidance rules to be governed,
so that dense schematic content remains readable and trustworthy.

## Acceptance Criteria

1. Given a dense schematic fixture, when layout is computed, placement and routing remain understandable.
2. Obvious label overlap, route clutter, and cramped grouping are reduced through deterministic rules.
3. Drawing rules are represented as layout facts or rule metadata, not renderer-local hacks.
4. Engineering relationships such as subject grouping, terminals, anchors, routes, labels, and cross references remain visible.
5. No AI layout, auto-router, or optimization engine is introduced in M20.

## Tasks / Subtasks

- [x] Define the first engineering drawing rules (AC: 1, 2, 3)
  - [x] Cover minimum spacing, grouping, label positioning, routing lanes, and title-block avoidance.
  - [x] Keep the rules deterministic and inspectable.
- [x] Apply rules to the dense proof fixture (AC: 1, 2, 4)
  - [x] Use governed anchors and occurrences as inputs.
  - [x] Preserve cross-reference and canonical subject visibility.
- [x] Add regression tests for dense content (AC: 1, 2, 5)
  - [x] Assert no known dense-fixture overlaps.
  - [x] Assert repeated runs produce the same rule outcomes.

## Dev Notes

### Current State

- M19 schematic rendering is credible but not yet acceptable enough for dense customer-facing use.
- M20 should codify simple drawing rules before any later layout-intelligence milestone.
- ELK and constraint solving stay deferred; simple deterministic rules are enough here.

### Architectural Guardrails

- Follow M20 AD-4 and AD-10.
- Layout rules help presentation; they do not become semantic authority.
- Do not add draggable sheet-local saved positions.

### Project Structure Notes

- Likely update targets:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
  - `examples/m20/`
  - `ide/theia-frontend/scripts/*m20*.test.mjs`
- Keep rule names stable and explicit enough for tests to assert.

### Testing Requirements

- Prefer deterministic data assertions over screenshot-only checks.
- Add frontend visual-acceptance tests only after kernel/runtime facts are stable.
- Keep tests local to the governed fixture set.

### References

- [Source: `_bmad-output/implementation-artifacts/m20/epics.md` - Epic 2, Story 2.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md` - FR-4]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md` - AD-4, AD-10]
- [Source: `draft/screenshort/`]

## Dev Agent Record

### Agent Model Used

GPT-5

### Debug Log References

- Added a governed dense M20 proof fixture with explicit drawing-rule metadata.
- Added a frontend regression test that checks spacing, label separation, routing style, and repeatability.
- Verification: `yarn workspace @engineeringood/athena-theia-frontend test` and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### Completion Notes List

- Dense schematic rules are now carried as governed fixture metadata, not renderer hacks.
- The dense proof fixture stays small, deterministic, and readable under repeated model builds.
- Frontend proof coverage now checks pairwise node separation and orthogonal routing.

### File List

- `_bmad-output/implementation-artifacts/m20/2-2-encode-engineering-drawing-rules-for-dense-content.md`
- `examples/m20/dense-sheet-proof/README.md`
- `examples/m20/dense-sheet-proof/ready-sheet.diagram.mjs`
- `ide/theia-frontend/scripts/athena-m20-dense-sheet-proof.test.mjs`

## Change Log

- 2026-07-17: Added the dense sheet proof corpus and deterministic regression coverage for governed drawing rules.
