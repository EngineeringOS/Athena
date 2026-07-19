---
status: ready-for-dev
baseline_commit: 87ee48c42fede0850f0b981bab04b8810a61c2e2
epic: 2
story: 2.2
title: Integrate representation facts into Presentation IR
---

# Story 2.2: Integrate representation facts into Presentation IR

## Story

As a presentation pipeline maintainer,
I want representation, symbol, terminal, and label facts carried by Presentation IR,
So that M25 does not bypass the M13 presentation layer.

## Acceptance Criteria

- Presentation snapshots include representation facts, symbol facts, terminal facts, label facts,
  route anchors, and occurrence identity.
- Existing M24 route facts remain present.
- Theia can consume the payload without resolving representation policy.
- Serialization remains deterministic and reload-stable.

## Tasks/Subtasks

- [x] Locate presentation snapshot payloads.
- [x] Extend Presentation IR with M25 facts.
- [x] Add deterministic serialization tests.
- [x] Preserve M24 route fact compatibility.

## Dev Notes

- Governed by AD-2.
- Do not couple component knowledge directly to Theia.

## Dev Agent Record

### Debug Log

- 2026-07-19: Used CodeGraph to inspect `PresentationDocument`, `connectorsForRendering`, and
  `PresentationModelDeriver`.
- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`; compile failed because representation facts were not yet in Presentation IR.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`.

### Completion Notes

- Added `PresentationRepresentationFact` to `PresentationDocument`.
- Added deterministic `representationFactsForRendering()` ordering by subject and occurrence.
- Added `:kernel:representation-model` dependency to `:kernel:presentation-model`.
- Existing M24 route fact compatibility was preserved by keeping existing route snapshot fields and
  constructor defaults intact.

### File List

- `kernel/presentation-model/build.gradle.kts`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRepresentationFactContractTest.kt`

## Change Log

- 2026-07-19: Integrated representation facts into Presentation IR.

## Status

review
