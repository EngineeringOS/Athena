---
status: done
story_id: 3.1
epic: 3
title: Select Semantic Subjects In Graphical View
---

# Story 3.1: Select Semantic Subjects In Graphical View

## Story

As an Athena user, I want to select relationship candidates from the sheet, so that graphical
authoring starts from semantic identity rather than SVG geometry.

## Acceptance Criteria

- Relationship mode can be activated in Graphical View.
- Visible ports/presentation terminals resolve to canonical semantic subject ids from projection
  facts.
- Theia does not use DOM text or SVG coordinates as identity.
- Hover/selection affordance remains transient and preserves M27 density.

## Tasks/Subtasks

- [x] Add failing frontend model tests for relationship subject selection.
- [x] Add relationship mode state.
- [x] Resolve subject identity from projection facts.
- [x] Keep normal-state UI chrome transparent and compact.
- [x] Run focused frontend tests sequentially.

## Dev Notes

- Architecture: M28 AD-1, AD-6, AD-8 are binding.

## Dev Agent Record

### Debug Log

- RED: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-m28-relationship-authoring-model.test.mjs } else { exit $LASTEXITCODE }` failed with `ERR_MODULE_NOT_FOUND` for `athena-relationship-authoring-model.js`.
- GREEN: the same focused frontend command passed after adding the relationship authoring model.

### Completion Notes

- Added a transient frontend relationship mode state that starts empty, captures source/target port subjects, and rejects DOM-only or non-port subjects.
- Relationship subject selection reuses governed projection facts via `resolveRenderedSelectionTarget`; it does not parse SVG text or coordinates.
- Added an affordance class helper that keeps normal chrome unmarked and only emits hover/selected classes for transient relationship states.

## File List

- ide/theia-frontend/src/browser/athena-relationship-authoring-model.ts
- ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented frontend relationship mode state and projection-fact subject selection.
