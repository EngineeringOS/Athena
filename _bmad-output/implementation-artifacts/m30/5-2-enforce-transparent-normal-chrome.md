---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 5.2
epic: 5
title: Enforce Transparent Normal Chrome
---

# Story 5.2: Enforce Transparent Normal Chrome

## Status

Done

## Story

As a Athena user,
I want normal graph chrome to be visually transparent,
so that the engineering symbols keep focus and the canvas stays dense.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given normal state graph rendering, when hitboxes, component backgrounds, and interaction wrappers are inspected, then they are transparent.
2. Given hover, selection, focus, or drag state, when state is active, then dotted or highlighted borders may appear.
3. Given smoke or DOM proof runs, when normal state is checked, then visible wrapper borders are absent.

## Tasks/Subtasks

- [x] Add failing normal-state chrome proof. (AC: 1,3)
- [x] Adjust renderer/CSS to keep normal chrome transparent. (AC: 1)
- [x] Preserve state-only hover/selection/focus/drag affordances. (AC: 2)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- This fixes the border-a/border-b issue: wrappers are interaction affordances, not permanent drawing elements.
- Do not remove actual engineering symbol strokes.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Located graph workbench chrome styles in `ide/theia-frontend/src/browser/style/index.css`.
- 2026-07-21: Added `athena-m30-transparent-chrome.test.mjs`; the proof passed on first run because current CSS already keeps hitboxes/backgrounds transparent and hover/focus affordances conditional.
- 2026-07-21: Combined frontend verification passed with `node --test ide\theia-frontend\scripts\athena-m30-representation-rendering.test.mjs ide\theia-frontend\scripts\athena-m30-transparent-chrome.test.mjs`.
- 2026-07-21: Review verification passed with focused transparent-chrome and representation-rendering frontend guards.

### Completion Notes

- Added a structural normal-state chrome regression guard for transparent hitboxes, transparent electrical-device fills, conditional hover/focus hitbox stroke, and no wrapper border classes.
- No CSS production change was required; existing chrome already satisfied this story after prior fixes.
- Preserved actual engineering symbol strokes and state-only affordances.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed the current renderer keeps normal chrome transparent without sacrificing state-only affordances.

## File List

- `_bmad-output/implementation-artifacts/m30/5-2-enforce-transparent-normal-chrome.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m30-transparent-chrome.test.mjs`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added transparent normal chrome regression guard.
- 2026-07-21: Closed review after transparent-chrome verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
