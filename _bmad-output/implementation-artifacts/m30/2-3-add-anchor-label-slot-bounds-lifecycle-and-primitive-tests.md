---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 2.3
epic: 2
title: Add Anchor, Label-Slot, Bounds, Lifecycle, And Primitive Tests
---

# Story 2.3: Add Anchor, Label-Slot, Bounds, Lifecycle, And Primitive Tests

## Status

Done

## Story

As a Athena maintainer,
I want structural symbol-pack tests,
so that symbol quality failures are caught before binding or rendering.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given every route-attached symbol, when tests inspect it, then named terminal anchors exist.
2. Given every demo symbol, when tests inspect it, then required label slots, terminal slots, bounds, hotspot, lifecycle, and version metadata are valid.
3. Given primitives are emitted, when output is compared across repeated runs, then primitive output is deterministic.

## Tasks/Subtasks

- [x] Add anchor coverage for route-attached symbols. (AC: 1)
- [x] Add label slot, terminal slot, bounds, hotspot, lifecycle, and version tests. (AC: 2)
- [x] Add deterministic primitive output tests. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Lifecycle metadata minimum: version, active/deprecated/superseded state, supersededBy, migrationHint, provenance.
- M30 validates metadata; it does not implement project migration behavior.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.NativeRepresentationSymbolQualityTest"` failing on missing `NativeRepresentationPrimitiveEmitter`.
- 2026-07-21: GREEN confirmed with focused `NativeRepresentationSymbolQualityTest` after adding deterministic primitive emission.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Review verification passed with focused `NativeRepresentationSymbolQualityTest` and full module test.

### Completion Notes

- Added structural coverage for every route-attached demo symbol terminal anchor and terminal number.
- Added metadata coverage for label slots, bounds, hotspot, lifecycle, version, variants, and style tokens.
- Added deterministic primitive emission from native representation libraries.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed deterministic primitive emission stays stable across repeated loads and does not depend on renderer state.

## File List

- `_bmad-output/implementation-artifacts/m30/2-3-add-anchor-label-slot-bounds-lifecycle-and-primitive-tests.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/NativeRepresentationPrimitiveEmitter.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/NativeRepresentationSymbolQualityTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added native symbol structural quality and deterministic primitive emission tests.
- 2026-07-21: Closed review after focused quality verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
