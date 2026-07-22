---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 1.2
epic: 1
title: Define Single-Intent Transaction And Revision Guard
---

# Story 1.2: Define Single-Intent Transaction And Revision Guard

## Status

Done

## Story

As an engineering author,
I want each mutable action represented by one revision-safe transaction,
so that preview, validation, decision, and provenance cannot drift apart.

## Required Context

- M31 PRD, architecture spine, authoring contract, epics, sprint status, and completed Story 1.1.
- Existing authoring contracts: `kernel/authoring-model`.
- Existing capability evidence: `kernel/interaction-model/.../AuthoringCapabilities.kt`.

## Acceptance Criteria

1. One eligible mutable intent creates a frontend-independent transaction carrying all M31 envelope fields.
2. Zero or multiple intents return `authoring.transaction.intent-count-unsupported` and no transaction.
3. Revision Guard carries semantic snapshot id, source URI, document version, and SHA-256 of exact UTF-8 content.
4. Equal inputs produce equal Revision Guard and deterministic transaction data.
5. Mandatory Polish/Purge Gate and AC-to-evidence mapping complete.

## Tasks/Subtasks

- [x] Add failing authoring-model tests for guard hashing, one-intent creation, and rejected cardinality. (AC: 1,2,3,4)
- [x] Add cohesive transaction, revision, validation/result, lifecycle, diagnostic, and provenance models. (AC: 1,3)
- [x] Add a single-intent transaction factory with explicit unsupported diagnostics. (AC: 1,2,4)
- [x] Run focused and full authoring-model tests sequentially. (AC: 1,2,3,4)
- [x] Complete mandatory polish/purge review and record evidence. (AC: 5)

## Dev Notes

- Transaction state is runtime/audit state, not canonical engineering truth.
- The model may reference interaction capability evidence; it may not depend on Theia, projection,
  representation, SVG, DOM, or browser types.
- Keep existing authoring call sites source-compatible where this story does not migrate them.
- Use TDD and sequential Gradle verification.

## Dev Agent Record

### Debug Log

- RED: focused `SemanticAuthoringTransactionTest` failed because transaction and Revision Guard contracts did not exist.
- GREEN: focused transaction test passed after adding the cohesive models and single-intent factory.
- REFACTOR: strengthened exact UTF-8 hashing evidence with a known SHA-256 and one-byte-content-change assertion.
- Regression: full `:kernel:authoring-model:test` passed after the final test refinement.

### Completion Notes

- Added deterministic Revision Guard construction over exact UTF-8 source content.
- Added one-intent transaction envelope fields for capability evidence, preview, validation, provenance, lifecycle, mutation result, and diagnostics.
- Rejected zero and multiple intents with `authoring.transaction.intent-count-unsupported` and no transaction.
- Preserved existing authoring preview call sites through an optional Revision Guard field.
- AC evidence: AC-1 transaction factory test; AC-2 empty/multiple rejection test; AC-3 known exact SHA-256 assertion; AC-4 equal guard and transaction inputs; AC-5 CodeGraph overlap review, focused/full tests, and encoding audit.
- Polish/Purge: reviewed adjacent authoring models and found no duplicate transaction, lifecycle, guard, validation, provenance, or result contracts; no stale compatibility path or document claim required removal.

## File List

- `_bmad-output/implementation-artifacts/m31/1-2-define-single-intent-transaction-and-revision-guard.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringTransactionModels.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/SemanticAuthoringTransactionTest.kt`

## Change Log

- 2026-07-21: Story created and started.
- 2026-07-21: Implemented and verified single-intent transaction and deterministic Revision Guard contracts; completed polish/purge review.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent transaction, authoring, test, docs, and compatibility paths.
- Remove stale artifacts or ledger them with owner, reason, target milestone, and verification.
- Re-run final verification after cleanup.
