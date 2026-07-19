---
status: ready-for-dev
baseline_commit: 87ee48c42fede0850f0b981bab04b8810a61c2e2
epic: 2
story: 2.3
title: Attach M24 route facts to presentation terminals
---

# Story 2.3: Attach M24 route facts to presentation terminals

## Story

As a routing integration engineer,
I want M24 routes to attach to M25 presentation terminals,
So that improved wire routes connect to professional terminal notation.

## Acceptance Criteria

- Route endpoints reference presentation terminal anchors.
- Center fallback is absent from the accepted M25 proof.
- Route quality remains satisfied for the mandatory acceptance path.
- Terminal and route identities remain inspectable together.

## Tasks/Subtasks

- [x] Map terminal notation facts to M24 route anchors.
- [x] Preserve route quality data.
- [x] Add mandatory-path route attachment tests.
- [x] Add regression guard for center fallback absence.

## Dev Notes

- Governed by AD-5 and inherited M24 AD-4, AD-8, AD-9.

## Dev Agent Record

### Debug Log

- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`; compile failed because route attachment facts/helpers did not exist.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`.

### Completion Notes

- Added `PresentationRouteAttachmentFact` and `attachRoutesToPresentationTerminals`.
- Route attachment maps M24 route terminal anchor ids to M25 presentation terminal route anchors.
- Route quality state is preserved on attachment facts.
- Center fallback remains false for accepted presentation terminal attachment facts.

### File List

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRouteAttachmentContractTest.kt`

## Change Log

- 2026-07-19: Implemented route-to-presentation-terminal attachment helper and tests.

## Status

review
