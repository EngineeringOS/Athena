---
status: ready-for-dev
baseline_commit: b195399ea8ba56f120948427e5f63d55cc8fec5f
epic: 3
story: 3.3
title: Add representation, terminal, and label inspection
---

# Story 3.3: Add representation, terminal, and label inspection

## Story

As an IDE user,
I want selected symbols, terminals, labels, and routes to reveal their identities,
So that graphical inspection remains tied to source semantics.

## Acceptance Criteria

- Inspection payloads expose canonical subject, occurrence, terminal, port, label role, and route
  identity where applicable.
- Source reveal uses existing identity paths.
- No duplicate editor panel opens for the same source file.
- Active source projection updates correctly when switching `.athena` files.

## Tasks/Subtasks

- [x] Extend inspection payloads for representation, terminals, and labels.
- [x] Wire selection to existing source reveal path.
- [x] Add regression for same-tab reveal behavior.
- [x] Add active-source projection regression for M25 sample.

## Dev Notes

- Governed by AD-2, AD-5, AD-6, AD-9.
- Preserve the M20-M24 accepted Graph Workbench UX.

## Dev Agent Record

### Debug Log

- 2026-07-19: Used CodeGraph to inspect Graph Workbench selection, route inspection, semantic selection, reveal, and active-source projection paths.
- 2026-07-19: Red phase confirmed with `yarn --cwd ide/theia-frontend test --test-name-pattern "M25 representation"`; `buildAthenaGraphRepresentationInspection` was missing.
- 2026-07-19: Green phase passed with `yarn --cwd ide/theia-frontend test --test-name-pattern "M25 representation|same source|active source|same-tab|Graphical View projection"`; 109 tests passed.

### Completion Notes

- Added `buildAthenaGraphRepresentationInspection` to expose subject, occurrence, representation, symbol family, terminal, port, physical terminal, label role, and route-adjacent identity details from model facts.
- Added representation inspection rows to the existing top info popover.
- Presentation terminal and label SVG elements now select their upstream terminal/label ids without bubbling to parent component selection.
- Same-tab reveal and active-source projection behavior remain covered by existing Graph Workbench regression tests in the verified suite.

### File List

- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`

## Change Log

- 2026-07-19: Added M25 representation, terminal, and label inspection payloads and selection wiring.

## Status

review
