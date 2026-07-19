---
status: ready-for-dev
epic: 4
story: 4.3
title: Add M25 product smoke and regression coverage
---

# Story 4.3: Add M25 product smoke and regression coverage

## Story

As a developer,
I want product-path tests for M25,
So that Theia proof failures are caught before user review.

## Acceptance Criteria

- Product smoke verifies rendered representation facts, terminal facts, label facts, route
  attachments, zero fallback symbols, and active-source behavior.
- M24 route quality regression checks still pass.
- Verification commands are documented.
- Gradle verification is not run concurrently.

## Tasks/Subtasks

- [ ] Add M25 product smoke test.
- [ ] Add regression checks for M24 route quality and M25 zero fallback.
- [ ] Add script/package wiring consistent with prior milestone pattern.
- [ ] Document commands and evidence.

## Dev Notes

- Governed by AD-2, AD-7, AD-9.
- Follow repo Windows Gradle rule: sequential Gradle only.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
