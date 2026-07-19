---
status: ready-for-dev
epic: 4
story: 4.1
title: Create the openable M25 sample project
---

# Story 4.1: Create the openable M25 sample project

## Story

As Aaron,
I want a real M25 sample project with `.athena` sources,
So that I can present M25 in the IDE without explaining scripts.

## Acceptance Criteria

- `examples/m25/sample-project` contains real `.athena` files for the six sample families.
- Mandatory path includes PLC/controller, terminal block, power supply, and load/actuator.
- Source syntax is accepted by the existing language stack.
- The sample can be opened through normal Athena Theia IDE workflow.

## Tasks/Subtasks

- [ ] Create sample project structure and `.athena` sources.
- [ ] Keep syntax limited to already-supported language constructs.
- [ ] Add project launch script or package entry if the repo pattern requires it.
- [ ] Verify the sample opens in the product path.

## Dev Notes

- Governed by AD-7, AD-9, AD-10.
- No `.mjs`-only proof.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
