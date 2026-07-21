---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 3.3
epic: 3
title: Surface Missing Or Ambiguous Binding Diagnostics
---

# Story 3.3: Surface Missing Or Ambiguous Binding Diagnostics

## Status

Done

## Story

As a reviewer,
I want binding failures surfaced explicitly,
so that renderer fallback boxes cannot hide incomplete representation policy.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given missing symbol, missing anchor, missing label slot, unsupported role, or ambiguous binding cases, when tests run, then each produces a stable diagnostic.
2. Given the M30 sample proof payload is emitted, when binding status is inspected, then accepted proof has no missing-binding diagnostics.
3. Given a symbol cannot be resolved, when renderer output is produced, then silent generic fallback boxes are not accepted without diagnostics.

## Tasks/Subtasks

- [x] Add negative binding tests for missing and ambiguous cases. (AC: 1)
- [x] Expose binding status in runtime/LSP proof payload. (AC: 2)
- [x] Guard renderer fallback paths with diagnostics. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- This story prevents the old toy renderer path from surviving as silent fallback.
- Any retained fallback must be ledgered with owner, reason, and target milestone.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.RepresentationBindingStatusPayloadTest"` failing on missing binding status payload and fallback guard.
- 2026-07-21: Initial GREEN attempt surfaced an extra `representation.policy.missing` because the negative fixture had no valid LOAD policy; corrected fixture to isolate the required diagnostics.
- 2026-07-21: GREEN confirmed with focused `RepresentationBindingStatusPayloadTest`.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Review verification passed with focused `RepresentationBindingStatusPayloadTest` and full module test.

### Completion Notes

- Added negative coverage for missing symbol, missing anchor, missing label slot, unsupported role, and ambiguous binding diagnostics.
- Exposed deterministic string-only binding status payload for the M30 demo proof.
- Added a fallback guard rejecting renderer fallback when no representation diagnostic exists.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed the accepted demo proof contains zero missing-binding diagnostics and does not permit silent generic fallback.

## File List

- `_bmad-output/implementation-artifacts/m30/3-3-surface-missing-or-ambiguous-binding-diagnostics.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingStatusPayloadTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added binding status payload, negative diagnostics tests, and silent fallback guard.
- 2026-07-21: Closed review after diagnostic payload verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
