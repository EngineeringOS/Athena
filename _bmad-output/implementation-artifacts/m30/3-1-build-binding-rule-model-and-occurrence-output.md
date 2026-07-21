---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 3.1
epic: 3
title: Build Binding Rule Model And Occurrence Output
---

# Story 3.1: Build Binding Rule Model And Occurrence Output

## Status

Done

## Story

As a compiler engineer,
I want binding rules that produce representation occurrences from semantic facts,
so that visual choices are governed by policy instead of renderer inference.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given binding rules are evaluated, when subject kind, role, projection context, occurrence role, symbol id, variant, labels, terminals, priority, and diagnostics are required, then the model supports them.
2. Given a binding is produced, when identity fields are inspected, then canonical semantic id and projection occurrence id are separate.
3. Given binding code is reviewed, when authority sources are checked, then it does not use DOM, SVG, source file names as sheet identity, or QET element names.
4. Given visual choices are needed, when binding runs, then it consumes Representation Policy IR rather than hard-coding choices in renderer code.

## Tasks/Subtasks

- [x] Add failing tests for binding rule model and occurrence output. (AC: 1,2)
- [x] Implement binding rule model and occurrence output. (AC: 1,2,4)
- [x] Add anti-authority tests/audit for DOM/SVG/QET/source-file-name usage. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Binding emits occurrence IR only; it must not mutate source or produce final renderer geometry.
- Policy is the selection layer; binding is resolution.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.RepresentationBindingCompilerTest"` failing on missing binding compiler contracts.
- 2026-07-21: GREEN confirmed with focused `RepresentationBindingCompilerTest` after adding binding request/result/compiler.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Anti-authority audit passed: `rg -n "DOM|document\.|svg|SVG|qet|QET|\.elmt|sourceFile|source file|fileName|filename|sheet.*file" kernel\representation-model\src\main\kotlin\com\engineeringood\athena\representation\RepresentationBindingCompiler.kt` returned no matches.
- 2026-07-21: Review verification passed with focused `RepresentationBindingCompilerTest`, full module test, and fresh anti-authority scan.

### Completion Notes

- Added policy-driven representation binding request/result/compiler contracts.
- Binding emits `RepresentationOccurrence` IR with separate canonical semantic id and projection occurrence id.
- Binding delegates disagreement to representation diagnostics and does not accept explicit fallback as renderer guessing.
- Binding consumes symbol family through `RepresentationPolicy`; validation against definition family is deliberately deferred until Representation Definition IR exposes an explicit family field.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.

## File List

- `_bmad-output/implementation-artifacts/m30/3-1-build-binding-rule-model-and-occurrence-output.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompiler.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompilerTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added representation binding compiler and occurrence output tests.
- 2026-07-21: Closed review after binding verification and anti-authority scan.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
