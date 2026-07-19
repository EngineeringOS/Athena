---
status: ready-for-dev
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

- [ ] Locate existing component knowledge and presentation projection path.
- [ ] Add representation composition from component semantics.
- [ ] Add supported-family mapping for M25 profile.
- [ ] Add zero-fallback coverage tests for mandatory path.

## Dev Notes

- Use CodeGraph before code exploration.
- Governed by AD-2, AD-3, AD-4, AD-7.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
