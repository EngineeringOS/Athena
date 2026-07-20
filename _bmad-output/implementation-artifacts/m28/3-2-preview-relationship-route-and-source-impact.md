---
status: done
story_id: 3.2
epic: 3
title: Preview Relationship Route And Source Impact
---

# Story 3.2: Preview Relationship Route And Source Impact

## Story

As an Athena user, I want to see route preview and source impact before accepting, so that I
understand what will change.

## Acceptance Criteria

- Valid electrical relationship intent produces transient route preview.
- Preview shows compatibility state, route quality, and proposed `.athena` serialization target.
- Preview disappears on cancel, source reload, projection refresh, or accepted mutation.
- Preview is not persisted as route facts or hidden Theia truth.

## Tasks/Subtasks

- [x] Add failing frontend/runtime preview tests.
- [x] Implement preview state from relationship intent and routing facts.
- [x] Add source-impact summary for `connect A.p -> B.q`.
- [x] Clear preview on cancel/reload/refresh.
- [x] Run focused tests sequentially.

## Dev Notes

- Architecture: M28 AD-5, AD-6, AD-7 are binding.

## Dev Agent Record

### Debug Log

- RED: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-m28-relationship-authoring-model.test.mjs } else { exit $LASTEXITCODE }` failed with `TypeError: buildRelationshipAuthoringPreview is not a function`.
- GREEN: the same focused frontend command passed after adding transient relationship preview and clear helpers.

### Completion Notes

- Added a transient relationship authoring preview model with electrical relationship type, source/target semantic ids, route quality, and source-impact summary.
- Preview source impact emits the proposed `.athena` statement target as `connect A.p -> B.q`.
- Preview state is explicitly `transient: true` and `persisted: false`, and clears on cancel, source reload, projection refresh, or accepted mutation.

## File List

- ide/theia-frontend/src/browser/athena-relationship-authoring-model.ts
- ide/theia-frontend/scripts/athena-m28-relationship-authoring-model.test.mjs

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented transient relationship route/source-impact preview model.
