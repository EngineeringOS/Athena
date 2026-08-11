---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 4.1: Prove Rolling-Shutter Live Document

Status: in-progress

## Story

As a reviewer,

I want to open the M43 rolling-shutter project and inspect its live page,
so that milestone value is demonstrated on the real product surface.

## Acceptance Criteria

1. M43-local example contains colocated `rolling-shutter.athena` and `rolling-shutter.sheet.athena`.
2. Sheet source uses `cell: 4` or `cell: 8`, authored A1-style occurrences, and no macro syntax.
3. Product proof records ready Projection/Spatial/Presentation payload, visible title/grid/
   occurrences/routes, and source trace interaction.
4. Desktop and mobile screenshots live under `_bmad-output/implementation-artifacts/m43`.
5. Proof uses only M43 example; no earlier milestone fixture is referenced.

## Tasks / Subtasks

- [ ] Task 1: Create M43 rolling-shutter example with colocated source and Sheet Companion.
- [ ] Task 2: Run product E2E against Theia and capture desktop/mobile evidence.
- [ ] Task 3: Record deterministic proof manifest and source trace interaction.

## Dev Notes

- Keep project source engineering meaning and Sheet Companion presentation intent separate.
- Use existing Theia launch/build and LSP projection session. Do not copy earlier milestone proof
  artifacts or reintroduce retired examples.

### References

- [Source: _bmad-output/planning-artifacts/m43/epics.md#Epic 4 - Product Proof And Closure]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md#Structural Seed]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

### Completion Notes List

### File List
