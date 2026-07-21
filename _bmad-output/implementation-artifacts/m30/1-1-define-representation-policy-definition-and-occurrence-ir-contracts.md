---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 1.1
epic: 1
title: Define Representation Policy, Definition, And Occurrence IR Contracts
---

# Story 1.1: Define Representation Policy, Definition, And Occurrence IR Contracts

## Status

Done

## Story

As a Athena platform engineer,
I want platform-owned representation policy, definition, and occurrence contracts,
so that professional symbols are governed before rendering and outside the kernel.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given representation contracts are compiled, when tests inspect them, then policy id, symbol id, library id, primitive, anchor, terminal, label slot, style token, variant, definition, occurrence, reference binding, lifecycle state, and diagnostics are present.
2. Given a reusable symbol asset and a projected usage, when modeled, then Representation Definition IR and Representation Occurrence IR are separate shapes.
3. Given policy chooses a symbol, when the model is inspected, then symbol family, variant, occurrence role, fallback behavior, and priority are policy fields, not renderer code or Athena source syntax.
4. Given representation contracts compile, when dependencies are inspected, then they do not depend on Theia, SVG DOM, QET runtime classes, or source parser internals.

## Tasks/Subtasks

- [x] Add failing contract tests for policy, definition, occurrence, reference binding, lifecycle, and diagnostics. (AC: 1,2,3)
- [x] Create or update the platform-owned representation model boundary. (AC: 1,2,3,4)
- [x] Add deterministic serialization/transport tests for v0 contracts. (AC: 1,4)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Preferred module seed is kernel/representation-model unless code inspection proves a lower-risk existing module.
- Definition describes reusable assets; occurrence describes one semantic/projection use; policy selects the representation dialect.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.RepresentationModelContractTest"` failing on missing M30 representation contract symbols.
- 2026-07-21: GREEN confirmed with focused `RepresentationModelContractTest` after adding platform-owned policy, definition, occurrence, lifecycle, reference binding, diagnostic, and transport contracts.
- 2026-07-21: Added additional RED coverage for deterministic policy/definition/occurrence transport maps; RED failed on missing `toTransportMap`.
- 2026-07-21: GREEN confirmed with focused `RepresentationModelContractTest` after adding deterministic transport maps.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Review RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests com.engineeringood.athena.representation.RepresentationModelContractTest` failing because `RepresentationPolicy` did not expose the required symbol-family field.
- 2026-07-21: Review GREEN confirmed with focused `RepresentationModelContractTest` after adding `symbolFamilyId` to `RepresentationPolicy` and deterministic transport.
- 2026-07-21: Review regression passed with full `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.

### Completion Notes

- Extended the existing `kernel/representation-model` boundary instead of creating a duplicate module.
- Added M30 representation policy, definition, occurrence, lifecycle, provenance, reference binding, diagnostics, and deterministic transport contracts.
- Review fixed an AC-3 gap: representation policy now carries explicit `symbolFamilyId` instead of only concrete `symbolId`.
- Kept contracts free of Theia, SVG DOM, QET runtime, and source parser dependencies.
- Completed final polish/purge review; no temporary proof files, stale compatibility paths, or cleanup-ledger entries were required for this story.

## File List

- `_bmad-output/implementation-artifacts/m30/1-1-define-representation-policy-definition-and-occurrence-ir-contracts.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompilerTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingStatusPayloadTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationDiagnosticSerializationTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationModelContractTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Implemented M30 representation IR contract foundation and verification.
- 2026-07-21: Closed review gap by adding explicit representation policy symbol-family contract and verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.

