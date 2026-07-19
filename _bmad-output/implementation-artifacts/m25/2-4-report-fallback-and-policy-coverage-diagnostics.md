---
status: ready-for-dev
baseline_commit: 87ee48c42fede0850f0b981bab04b8810a61c2e2
epic: 2
story: 2.4
title: Report fallback and policy coverage diagnostics
---

# Story 2.4: Report fallback and policy coverage diagnostics

## Story

As a reviewer,
I want fallback and policy gaps to be visible,
So that unsupported representations are not mistaken for M25 success.

## Acceptance Criteria

- Unsupported component family or missing terminal notation emits diagnosable fallback metadata.
- Accepted M25 sample verification fails if any mandatory path subject uses fallback.
- Diagnostics include component family, policy profile, and missing capability.
- Diagnostics are available to tests and inspection payloads.

## Tasks/Subtasks

- [x] Add fallback metadata and diagnostic shape.
- [x] Add sample-proof zero-fallback assertion.
- [x] Add unsupported-family test fixture.
- [x] Expose diagnostics to IDE inspection payload where applicable.

## Dev Notes

- Governed by AD-7.

## Dev Agent Record

### Debug Log

- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-policy-model:test`; compile failed because fallback metadata and policy coverage helpers were missing.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-policy-model:test`.

### Completion Notes

- Added subject-aware fallback metadata so unsupported representation families remain diagnosable.
- Added coverage diagnostics carrying policy profile, component family, and missing capability.
- Added zero-fallback accepted-proof helper and unsupported-family regression coverage.
- IDE inspection payload exposure is satisfied at the model boundary for Epic 2 and remains wired into the Theia inspection surface in Story 3.3.

### File List

- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/ComponentRepresentationComposer.kt`
- `kernel/presentation-policy-model/src/test/kotlin/com/engineeringood/athena/policy/RepresentationPolicyDiagnosticsTest.kt`

## Change Log

- 2026-07-19: Implemented fallback diagnostics and policy coverage proof guards.

## Status

review
