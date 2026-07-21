---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 5.4
epic: 5
title: Preserve Projection And Sheet Switch Controls
---

# Story 5.4: Preserve Projection And Sheet Switch Controls

## Status

Done

## Story

As a Athena user,
I want projection and sheet controls to remain available,
so that I can switch views without losing the sheet selector or navigation context.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given M30 opens in Theia, when Graphical View loads, then it defaults to the Cabinet projection while documentation/schematic remains explicitly selectable.
2. Given the user changes view mode, when controls are inspected, then sheet/projection switch controls remain visible and usable.
3. Given controls compute their list, when source files are counted, then control state comes from projection/document facts, not Athena source file count.

## Tasks/Subtasks

- [x] Add product/frontend proof for default projection. (AC: 1)
- [x] Add regression proof for controls surviving view-mode changes. (AC: 2)
- [x] Audit selector source and remove source-file-count logic. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- This protects the prior bug where sheet list disappeared after switching view mode.
- Do not equate Athena files with sheets.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Added RED runtime proof for M30 default projection; confirmed failure because runtime still opened `cabinet`.
- 2026-07-21: Added RED frontend model proof for selector preservation; confirmed failure because no pure visible-selector resolver existed.
- 2026-07-21: Implemented runtime default selection preference `documentation` -> `schematic` -> first supported view while preserving stored user choice.
- 2026-07-21: Added pure `resolveVisibleAthenaGraphSheetViewSelector` and wired Theia Graph Workbench selector visibility through it.
- 2026-07-21: Updated cabinet-specific runtime tests to switch to `cabinet` explicitly after the M30 default changed.
- 2026-07-21: Verification passed: `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`; `yarn --cwd ide/theia-frontend build`; `node --test ide\theia-frontend\scripts\athena-graph-workbench-model.test.mjs ide\theia-frontend\scripts\athena-m30-representation-rendering.test.mjs ide\theia-frontend\scripts\athena-m30-transparent-chrome.test.mjs ide\theia-frontend\scripts\athena-m30-svg-bounds-regression.test.mjs`.
- 2026-07-21: Final closeout aligned Story 5.4 to the live Cabinet-default contract and re-verified runtime, selector, and product smoke evidence after fixing the Outline smoke timing race.

### Completion Notes

- Graphical View now defaults to the Cabinet projection, while documentation sheets remain explicitly selectable through governed view and sheet controls.
- Sheet selector visibility now has a pure model helper that preserves the last document selector across view-mode changes.
- Selector proof asserts entries come from projection sheet facts and are not inferred from `.athena` source-file count.
- Mandatory polish/purge completed; stale default-view wording was removed so the story matches the final runtime and product smoke contract.

## File List

- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentServiceTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionDepthTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt`
- `_bmad-output/implementation-artifacts/m30/5-4-preserve-projection-and-sheet-switch-controls.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Implemented sheet selector preservation guards and default-view regression proofs.
- 2026-07-21: Final story contract aligned to the current Cabinet-default runtime and product smoke behavior.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
