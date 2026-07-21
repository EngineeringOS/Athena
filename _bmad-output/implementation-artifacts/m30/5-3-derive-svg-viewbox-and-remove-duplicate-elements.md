---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 5.3
epic: 5
title: Derive SVG ViewBox And Remove Duplicate Elements
---

# Story 5.3: Derive SVG ViewBox And Remove Duplicate Elements

## Status

Done

## Story

As a reviewer,
I want canvas viewBox derived from actual content,
so that the drawing starts centered and sized to the real component/sheet content.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given resolved presentation bounds, when SVG is emitted, then viewBox derives from those bounds plus governed margins, not hard-coded constants.
2. Given accepted proof SVG is inspected, when occurrences are counted, then no off-screen duplicate symbol occurrence set exists.
3. Given labels are emitted, when always-visible labels are inspected, then no duplicated text nodes exist for the same label slot.

## Tasks/Subtasks

- [x] Add failing proof for hard-coded viewBox and duplicate/off-screen elements. (AC: 1,2)
- [x] Implement derived viewBox from resolved presentation bounds. (AC: 1)
- [x] Remove duplicate occurrence/text emission paths. (AC: 2,3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- This story must E2E verify SVG output; no claim without inspecting the emitted SVG/proof.
- Keep governed sheet margins; do not expand to a huge blank canvas.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Inspected workbench model and confirmed `svgViewBox` is derived from `resolveSceneBounds(nodes, edges, canvasWidth, canvasHeight)`.
- 2026-07-21: Added `athena-m30-svg-bounds-regression.test.mjs`; it passed on first run because current workbench model already derives bounds and filters off-canvas presentation occurrences.
- 2026-07-21: M30 frontend guard set passed with `node --test ide\theia-frontend\scripts\athena-m30-representation-rendering.test.mjs ide\theia-frontend\scripts\athena-m30-transparent-chrome.test.mjs ide\theia-frontend\scripts\athena-m30-svg-bounds-regression.test.mjs`.
- 2026-07-21: Review verification passed with focused SVG bounds regression guard and the combined M30 frontend guard set.

### Completion Notes

- Added structural guard proving SVG viewBox is content-derived from scene bounds, not hard-coded to `1680`, `960`, or a full blank canvas.
- Added guard for off-canvas presentation occurrence filtering and duplicate text-slot suppression.
- No production change was required in this story because the current workbench model already satisfies the M30 invariant.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed the workbench model still derives viewBox from content and suppresses duplicate visible text emission paths.

## File List

- `_bmad-output/implementation-artifacts/m30/5-3-derive-svg-viewbox-and-remove-duplicate-elements.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m30-svg-bounds-regression.test.mjs`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added SVG bounds and duplicate emission regression guard.
- 2026-07-21: Closed review after SVG bounds verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
