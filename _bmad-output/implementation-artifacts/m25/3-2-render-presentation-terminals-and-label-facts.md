---
status: ready-for-dev
baseline_commit: b195399ea8ba56f120948427e5f63d55cc8fec5f
epic: 3
story: 3.2
title: Render presentation terminals and label facts
---

# Story 3.2: Render presentation terminals and label facts

## Story

As an electrical reviewer,
I want terminal markers, terminal numbers, and labels visible at anchors,
So that route endpoints and component identity are readable.

## Acceptance Criteria

- Terminal marker shape plus terminal number is visible for accepted terminals.
- Device tags and component labels render at deterministic anchors.
- Labels do not cover accepted route lines.
- Renderer does not invent label or terminal meaning.

## Tasks/Subtasks

- [x] Render presentation terminal marker and number facts.
- [x] Render device, component, terminal, and route label facts.
- [x] Add tests or smoke assertions for visible terminal/label DOM markers.
- [x] Preserve M24 route label behavior.

## Dev Notes

- Governed by AD-5, AD-6, AD-9.

## Dev Agent Record

### Debug Log

- 2026-07-19: Red phase confirmed with `yarn --cwd ide/theia-frontend test --test-name-pattern "M25 representation|route inspection"`; M25 representation terminal and label model data were missing.
- 2026-07-19: Green phase passed with `yarn --cwd ide/theia-frontend test --test-name-pattern "M25 representation|route inspection"`.
- 2026-07-19: Added DOM marker smoke test; first run failed because the assertion expected a static JSX attribute instead of guarded JSX.
- 2026-07-19: Final frontend verification passed with `yarn --cwd ide/theia-frontend test --test-name-pattern "M25|representation|Presentation IR|route inspection|ready graphical"`; 109 tests passed.

### Completion Notes

- M25 representation terminal facts now resolve to scaled workbench terminal model data with marker, number, side, port, physical terminal, and anchor identity.
- M25 label facts now resolve to scaled workbench label model data with label role, value, subject, occurrence, and anchor identity.
- Theia renders terminal markers, terminal numbers, and representation labels as SVG elements with explicit DOM markers.
- Existing M24 route label inspection behavior remains covered by the route-inspection regression.

### File List

- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m25-representation-rendering.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`

## Change Log

- 2026-07-19: Implemented terminal marker/number and label fact rendering for M25 representation facts.

## Status

done
