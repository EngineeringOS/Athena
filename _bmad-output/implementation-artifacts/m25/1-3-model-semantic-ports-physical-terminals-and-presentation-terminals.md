---
status: ready-for-dev
baseline_commit: 265e26109ebf75352fdf9db3f814edcc77bbf546
epic: 1
story: 1.3
title: Model semantic ports, physical terminals, and presentation terminals
---

# Story 1.3: Model semantic ports, physical terminals, and presentation terminals

## Story

As an electrical modeling engineer,
I want terminal meaning split across semantic, physical, and presentation layers,
So that terminal notation remains engineering-owned instead of coordinate-owned.

## Acceptance Criteria

- Semantic ports, physical terminals, and presentation terminals are distinct model concepts.
- Presentation terminals carry marker, number, side, route anchor, subject id, occurrence id, port
  id, and terminal id.
- Terminal marker plus terminal number is enough for the M25 minimum accepted notation.
- Tests prove terminal numbers are not derived from renderer text.

## Tasks/Subtasks

- [x] Add terminal-layer contracts.
- [x] Add terminal notation data for mandatory sample families.
- [x] Add route-anchor compatibility with M24 routing facts.
- [x] Add tests for terminal identity and notation.

## Dev Notes

- Governed by AD-5 and inherited M24 AD-4.
- Keep terminal side policy-owned, not renderer-owned.

## Dev Agent Record

### Debug Log

- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`; compile failed because terminal-layer contracts did not exist.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.

### Completion Notes

- Added distinct `SemanticPortRef`, `PhysicalTerminalRef`, and `PresentationTerminalFact` contracts.
- Added `PresentationRouteAnchor` to provide route-anchor-compatible terminal facts without
  frontend state.
- Added identity value classes for representation subject, occurrence, semantic port, physical
  terminal, and presentation route anchor ids.
- Added tests for terminal identity separation and marker-plus-number minimum notation.

### File List

- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationIds.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/TerminalLayers.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/TerminalLayerContractTest.kt`

## Change Log

- 2026-07-19: Implemented semantic/physical/presentation terminal layer contracts.

## Status

review
