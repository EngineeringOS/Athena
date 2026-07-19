---
status: ready-for-dev
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

- [ ] Add fallback metadata and diagnostic shape.
- [ ] Add sample-proof zero-fallback assertion.
- [ ] Add unsupported-family test fixture.
- [ ] Expose diagnostics to IDE inspection payload where applicable.

## Dev Notes

- Governed by AD-7.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
