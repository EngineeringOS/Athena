---
status: ready-for-dev
baseline_commit: 265e26109ebf75352fdf9db3f814edcc77bbf546
epic: 1
story: 1.4
title: Define label facts and label policy
---

# Story 1.4: Define label facts and label policy

## Story

As a projection engineer,
I want labels represented as semantic presentation facts,
So that labels are inspectable and not raw text drawing calls.

## Acceptance Criteria

- Label facts carry subject id, occurrence id, role, value, anchor, and source identity where
  available.
- Label policy can produce device tags, component labels, terminal labels, and route labels.
- Label anchors are deterministic.
- Renderer code consumes label facts without owning label semantics.

## Tasks/Subtasks

- [x] Add label fact and label policy contracts.
- [x] Add deterministic label anchor rules for M25 profile.
- [x] Add tests for label identity and role coverage.
- [x] Verify renderer has no label-authority logic.

## Dev Notes

- Governed by AD-6.
- Labels are semantic presentation facts, not `drawText` authority.

## Dev Agent Record

### Debug Log

- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`; compile failed because label contracts did not exist.
- 2026-07-19: Fixed a test assertion that incorrectly compared policy order to lexical anchor-id order.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.

### Completion Notes

- Added `LabelFact`, `LabelPolicy`, `PresentationSourceIdentity`, and `SourceSpanRef` contracts.
- Label facts carry subject, occurrence, role, value, anchor, and optional source identity.
- Added deterministic default label anchors for industrial-control roles.
- Kept label authority out of renderer code for this contract story.

### File List

- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationIds.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/LabelFacts.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/LabelFactContractTest.kt`

## Change Log

- 2026-07-19: Implemented label fact and label policy contracts.

## Status

review
