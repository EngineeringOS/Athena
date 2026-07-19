---
status: ready-for-dev
epic: 3
story: 3.3
title: Add representation, terminal, and label inspection
---

# Story 3.3: Add representation, terminal, and label inspection

## Story

As an IDE user,
I want selected symbols, terminals, labels, and routes to reveal their identities,
So that graphical inspection remains tied to source semantics.

## Acceptance Criteria

- Inspection payloads expose canonical subject, occurrence, terminal, port, label role, and route
  identity where applicable.
- Source reveal uses existing identity paths.
- No duplicate editor panel opens for the same source file.
- Active source projection updates correctly when switching `.athena` files.

## Tasks/Subtasks

- [ ] Extend inspection payloads for representation, terminals, and labels.
- [ ] Wire selection to existing source reveal path.
- [ ] Add regression for same-tab reveal behavior.
- [ ] Add active-source projection regression for M25 sample.

## Dev Notes

- Governed by AD-2, AD-5, AD-6, AD-9.
- Preserve the M20-M24 accepted Graph Workbench UX.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
