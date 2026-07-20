---
status: done
story_id: 1.4
epic: 1
title: Canonicalize M28 Examples And Docs Around Nested Anatomy
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 1.4: Canonicalize M28 Examples And Docs Around Nested Anatomy

## Story

As an Athena maintainer, I want M28 examples and docs to use nested ports, so that the canonical
style is clear while old fixtures remain temporarily accepted.

## Acceptance Criteria

- M28 docs and samples use nested `device { port ... }` syntax.
- Docs explicitly state top-level `port D.p` is legacy-compatible, not canonical.
- Existing older milestone fixtures are not rewritten unless required by M28 proof.
- Encoding audit passes after documentation edits.

## Tasks/Subtasks

- [x] Create or update M28 usage docs.
- [x] Add canonical nested anatomy examples.
- [x] Add legacy policy note.
- [x] Run encoding audit.

## Dev Notes

- Architecture: M28 AD-3 and AD-9 are binding.

## Dev Agent Record

### Debug Log

- Added `docs/usages/m28-proof-usage.md`.
- Verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes

- Documented nested device-owned ports as canonical M28 component anatomy.
- Documented top-level `port Device.port` as legacy-compatible, not canonical.
- Documented `SemanticRelationshipIntent`, electrical specialization, canonical semantic persistence, and M29 interaction boundary.

## File List

- `docs/usages/m28-proof-usage.md`

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Added M28 usage documentation and marked story ready for review.
