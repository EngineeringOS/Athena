---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 2.2
epic: 2
title: Add Demo Symbol Set For Control-Sheet Proof
---

# Story 2.2: Add Demo Symbol Set For Control-Sheet Proof

## Status

Done

## Story

As a demo reviewer,
I want a focused native symbol set,
so that the M30 customer demo is recognizable as an industrial control sheet.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given the native symbol pack is loaded, when symbols are enumerated, then it includes supply/reference marker, terminal, switch/contact, coil/actuator, lamp/indicator, motor/load, protective device, and folio continuation/reference.
2. Given symbols are inspected, when primitives and labels are reviewed, then they use compact professional linework, anchors, and label slots.
3. Given symbol assets are searched, when QET XML or hidden Theia SVG snippets are looked for, then none are used as the source of symbol truth.

## Tasks/Subtasks

- [x] Create the minimum demo symbol definitions. (AC: 1)
- [x] Add anchors and label slots required by routing/binding. (AC: 2)
- [x] Add tests or audits proving assets are native Athena definitions. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Do not expand beyond the demo set; 20 excellent symbols beat hundreds of weak symbols.
- Arcs are allowed as presentation primitives, not kernel geometry.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.NativeRepresentationDemoSymbolPackTest"` failing because the native IEC v0 pack was missing.
- 2026-07-21: Initial run after adding the pack failed because the test used a repo-relative path under Gradle's module working directory; fixed test to load the runtime resource through the classloader.
- 2026-07-21: GREEN confirmed with focused `NativeRepresentationDemoSymbolPackTest`.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Review verification passed with focused `NativeRepresentationDemoSymbolPackTest`.

### Completion Notes

- Added a focused eight-symbol native IEC v0 representation pack: supply/reference marker, terminal, switch/contact, coil/actuator, lamp/indicator, motor/load, protective device, and folio continuation/reference.
- Added terminals, compact primitives, label slots, label anchors, variants, and style tokens for each demo symbol.
- Added tests proving the pack loads as a native Athena runtime resource and contains no QET XML, `.elmt`, Theia, or SVG snippet source truth.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed the shipped pack matches the focused industrial control demo set without expanding beyond the demo scope.

## File List

- `_bmad-output/implementation-artifacts/m30/2-2-add-demo-symbol-set-for-control-sheet-proof.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/resources/representation-libraries/athena-native-iec-v0.properties`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/NativeRepresentationDemoSymbolPackTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added native IEC v0 demo symbol pack and tests.
- 2026-07-21: Closed review after focused pack verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
