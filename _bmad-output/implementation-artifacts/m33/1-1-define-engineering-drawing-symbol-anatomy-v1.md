---
story_id: 1.1
story_key: 1-1-define-engineering-drawing-symbol-anatomy-v1
epic: 1
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 1.1: Define Engineering Drawing Symbol Anatomy v1

## Status

Done

## Story

As an Athena platform developer, I want a generic drawing symbol anatomy contract so IEC and future
domain packages share one symbol boundary.

## Acceptance Criteria

- Symbol definition captures identity, package id, domain/profile tags, lifecycle, anchors, label
  slots, reference slots, hotspots, bounds, orientation, and provenance.
- Validation rejects engineering truth, source mutation behavior, renderer-specific DOM
  assumptions, and `.athena` visual syntax.
- Contract serializes and tests without Theia/browser runtime.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED contract tests for valid generic drawing symbol anatomy and deterministic transport
  serialization. (AC: 1,3)
- [x] Add RED diagnostics tests for missing identity, package, tags, lifecycle, anchors, label slots,
  reference slots, hotspots, bounds, orientation, and provenance. (AC: 1,3)
- [x] Add RED validation tests for engineering truth, source mutation, DOM, SVG, and `.athena`
  visual-syntax authority leaks. (AC: 2)
- [x] Implement frontend-neutral drawing symbol anatomy models in the existing representation model
  boundary. (AC: 1,3)
- [x] Implement deterministic fail-closed validation and transport-safe diagnostics. (AC: 2,3)
- [x] Run focused and repository regression tests sequentially on Windows. (AC: 1..3)
- [x] Complete mandatory AC-to-evidence and polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 architecture AD-1, AD-3, AD-10.
- Prefer existing package/representation model patterns before adding a new module.
- Do not put product facts, port semantics, SVG paths, DOM ids, or source mutation behavior in
  symbol anatomy.

## Testing

- Add contract tests for valid anatomy and forbidden fields.
- Add diagnostics tests for missing required identity, anchors, slots, bounds, and lifecycle.

## Evidence Plan

- Unit tests prove contract and diagnostics.
- Story file updated with AC-to-evidence before review.
- Cleanup ledger entry only if stale model paths are retained.

## Polish And Purge

Review touched and adjacent model files, tests, package descriptors, and docs for duplicate DTOs,
dead fields, stale M30 terminology, and misleading IEC-only names.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- CodeGraph exploration was attempted first and timed out; repository lookup then used `rg`.
- RED: focused test failed during test compilation because `DrawingSymbolAnatomyValidator`, the
  anatomy contract, and transport serialization did not exist.
- GREEN: focused `DrawingSymbolAnatomyContractTest` passed after the minimal contract and validator
  were added.
- REFACTOR: split the 240-line mixed model/validation file into cohesive model and validation files;
  focused tests remained green.
- REGRESSION: `:kernel:representation-model:test` passed.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (25 executed, 122 up-to-date).
- TEXT: `tools/encoding-audit.ps1` passed.
- REVIEW RED: acceptance audit failed because version, primitives, anchor/terminal roles, complete
  transport, unique ids, and interaction-geometry guards were missing.
- REVIEW GREEN: focused contract tests, `:kernel:representation-model:test`, full `gradlew test`,
  and encoding audit passed after the review fixes.

### Completion Notes List

- Added generic symbol identity, package, domain/profile tags, lifecycle, anchors, label/reference
  slots, hotspots, bounds, orientations, provenance, and deterministic transport mapping.
- Added fail-closed diagnostics for every required anatomy section and invalid bounds.
- Added explicit rejection evidence for engineering truth, source mutation, DOM selector, SVG path,
  and `.athena` visual-syntax authority.
- Preserved existing M30 representation APIs; no Theia/browser/runtime or source syntax changed.
- AC-to-evidence: AC1 and AC3 are covered by
  `valid anatomy captures generic identity package policy and interaction geometry` and
  `validator rejects missing required anatomy sections with stable diagnostics`; AC2 is covered by
  `validator rejects semantic source and renderer authority claims`; AC4 is covered by full Gradle
  regression, encoding audit, authority scan, and this completion record.
- Polish/purge: split model and validation roles; found no duplicate DTO to remove, no stale M30 path
  touched, and no retained debt requiring a cleanup-ledger entry.
- Review follow-up: added version, renderer-neutral primitives, required/optional anchor roles,
  terminal roles, typed label/reference roles, complete structured transport payloads, unique member
  diagnostics, required-anchor checks, and bounded hotspot/anchor validation.
- Final acceptance review: PASS for AC1-AC4; the final auditor confirmed anatomy completeness and
  transport checks after review fixes.

### File List

- `_bmad-output/implementation-artifacts/m33/1-1-define-engineering-drawing-symbol-anatomy-v1.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingSymbolAnatomy.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingSymbolAnatomyTransport.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingSymbolAnatomyValidation.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/DrawingSymbolAnatomyContractTest.kt`

## Change Log

- 2026-07-23: Implemented and verified generic Engineering Drawing Symbol Anatomy v1 contract.
- 2026-07-23: Addressed Epic 1 code-review findings for complete anatomy and lossless transport.
- 2026-07-23: Final acceptance review passed; story closed.
