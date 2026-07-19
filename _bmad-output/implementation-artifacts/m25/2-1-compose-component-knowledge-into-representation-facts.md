---
status: ready-for-dev
baseline_commit: 87ee48c42fede0850f0b981bab04b8810a61c2e2
epic: 2
story: 2.1
title: Compose component knowledge into representation facts
---

# Story 2.1: Compose component knowledge into representation facts

## Story

As a runtime/projection engineer,
I want component family, role, ports, and terminal definitions to compose into representation facts,
So that visible schematic subjects are generated from meaning.

## Acceptance Criteria

- PLC/controller, terminal block, power supply, and load/actuator produce supported presentation
  anatomy facts.
- HMI/operator and protection device produce supported facts when present.
- The accepted proof uses zero generic fallback symbols.
- Composition stays upstream of Theia and renderer code.

## Tasks/Subtasks

- [x] Locate existing component knowledge and presentation projection path.
- [x] Add representation composition from component semantics.
- [x] Add supported-family mapping for M25 profile.
- [x] Add zero-fallback coverage tests for mandatory path.

## Dev Notes

- Use CodeGraph before code exploration.
- Governed by AD-2, AD-3, AD-4, AD-7.

## Dev Agent Record

### Debug Log

- 2026-07-19: Used CodeGraph to inspect `PresentationModelDeriver`, `PresentationDocument`,
  component family handling, M24 route facts, and terminal anchors.
- 2026-07-19: Red phase confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-policy-model:test`; compile failed because the component representation composer did not exist.
- 2026-07-19: Green phase passed with `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-policy-model:test`.

### Completion Notes

- Added `ComponentRepresentationComposer` to compose supported component-family requests into
  presentation anatomy, symbol anatomy, presentation terminals, and label facts.
- Mandatory M25 path families now compose with zero fallback symbols.
- HMI/operator and protection-device optional families compose when present.
- Composition stays in kernel policy/model code and does not touch Theia.

### File List

- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/ComponentRepresentationComposer.kt`
- `kernel/presentation-policy-model/src/test/kotlin/com/engineeringood/athena/policy/ComponentRepresentationComposerTest.kt`

## Change Log

- 2026-07-19: Implemented component representation composition and zero-fallback tests.

## Status

done
