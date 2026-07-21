---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 3.2
epic: 3
title: Bind Demo Devices, Ports, Relationships, And References
---

# Story 3.2: Bind Demo Devices, Ports, Relationships, And References

## Status

Done

## Story

As a Athena user,
I want demo semantic entities bound to professional representation occurrences,
so that the same engineering model can appear as correct visual roles.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given the M30 sample is compiled, when binding runs, then demo devices bind to native representation symbols.
2. Given ports are bound, when route anchors are inspected, then ports resolve to terminal anchors and compact label slots.
3. Given relationship and document/reference facts exist, when binding runs, then route or continuation representation occurrences are produced.
4. Given the sample needs reference semantics, when binding proof runs, then at least one coil/contact, device/terminal-strip, component/location, or folio continuation/reference occurrence is proven.

## Tasks/Subtasks

- [x] Add sample binding tests for devices and ports. (AC: 1,2)
- [x] Bind relationships and document/reference facts. (AC: 3)
- [x] Add one semantic reference occurrence proof. (AC: 4)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- A semantic device may have multiple occurrences; do not collapse it into one drawn box.
- Reference marks are visual occurrences of semantic/projection reference facts.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.M30DemoRepresentationBinderTest"` failing on missing demo binder/sample contracts.
- 2026-07-21: GREEN confirmed with focused `M30DemoRepresentationBinderTest` after adding demo binding sample, binder, and proof model.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Review verification passed with focused `M30DemoRepresentationBinderTest`, `RepresentationBindingStatusPayloadTest`, and full module test.

### Completion Notes

- Added deterministic demo representation binding proof over the native IEC v0 library.
- Bound demo devices to native representation symbols with terminal bindings and compact label slots.
- Added folio continuation/reference occurrence proof for `connection:ControlRelayK1.a1->MotorM1.u1`.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed device occurrences, terminal bindings, label bindings, and folio continuation reference proof stay diagnostic-clean.

## File List

- `_bmad-output/implementation-artifacts/m30/3-2-bind-demo-devices-ports-relationships-and-references.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinderTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added demo device, port, and reference binding proof.
- 2026-07-21: Closed review after demo binding proof verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
