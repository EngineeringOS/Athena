---
status: ready-for-dev
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

- [ ] Locate presentation snapshot payloads.
- [ ] Extend Presentation IR with M25 facts.
- [ ] Add deterministic serialization tests.
- [ ] Preserve M24 route fact compatibility.

## Dev Notes

- Governed by AD-2.
- Do not couple component knowledge directly to Theia.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
