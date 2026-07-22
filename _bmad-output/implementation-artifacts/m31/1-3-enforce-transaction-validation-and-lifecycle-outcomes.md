---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 1.3
epic: 1
title: Enforce Transaction Validation And Lifecycle Outcomes
---

# Story 1.3: Enforce Transaction Validation And Lifecycle Outcomes

## Status

Done

## Story

As an engineering author,
I want explicit validation and lifecycle outcomes before and after commit,
so that stale, blocked, cancelled, compile-stopped, and projection-failed changes are never confused.

## Acceptance Criteria

1. Runtime validates one mutable transaction in this fixed order: intent shape, capability evidence, actor/subject eligibility, Revision Guard, semantic rules, source planning, parser validation, semantic validation, preview eligibility; no accepted mutation can bypass a stage.
2. A source-content or semantic-snapshot mismatch produces lifecycle `STALE` and diagnostic `authoring.preview.stale`, invokes no mutation, and leaves canonical/downstream state unchanged.
3. A proposed-source `STOP_DOWNSTREAM` result produces lifecycle `BLOCKED` and diagnostic `authoring.validation.stop-downstream`, distinct from projection failure.
4. A committed mutation followed by reprojection failure preserves mutation id and committed Revision Guard, reports lifecycle `PROJECTION_FAILED`, and carries `authoring.projection.failed-after-commit`; no fallback is success.
5. Reject and cancel outcomes invoke no mutation and remain distinct lifecycle results.
6. Existing guided authoring session preview/decision behavior remains source-compatible and its regression tests pass.
7. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete.

## Tasks/Subtasks

- [x] Add failing `authoring-model` tests for the complete diagnostic envelope and lifecycle result invariants. (AC: 2,3,4,5)
- [x] Add failing `runtime` tests proving fixed validation order, stale short-circuit, STOP_DOWNSTREAM blocking, reject/cancel no-op, successful commit/reproject, and failed-after-commit projection. (AC: 1,2,3,4,5)
- [x] Complete cohesive model contracts for validation stages, diagnostic authority/stage/severity/recovery metadata, and immutable transaction outcome transitions. (AC: 1,2,3,4,5)
- [x] Implement a frontend-independent transaction validation runtime using injected stage, revision, mutation, and reprojection authorities; do not implement Story 2.2 source serialization here. (AC: 1,2,3,4)
- [x] Preserve and verify existing `AthenaAuthoringSessionRuntimeService` behavior and all authoring-model/runtime callers. (AC: 6)
- [x] Run focused tests, full `:kernel:authoring-model:test`, and full `:kernel:runtime:test` sequentially. (AC: 1,2,3,4,5,6)
- [x] Complete mandatory polish/purge review and record AC-to-evidence mapping. (AC: 7)

## Dev Notes

### Architecture Guardrails

- Extend `kernel/authoring-model` and `kernel/runtime`; do not create a second authoring runtime module, capability registry, mutation authority, or frontend lifecycle table.
- The transaction is runtime/audit state, never canonical engineering truth. Accepted `.athena` semantics remain truth.
- Validation order is a platform contract. Model stages explicitly and stop at the first blocking result.
- Compare the active Revision Guard to the transaction and preview guard before source planning or mutation.
- Rejection, cancellation, stale state, and blocked validation must not call mutation or reprojection authorities.
- `COMMITTED` followed by `PROJECTION_FAILED` is valid. Preserve mutation identity and committed revision; do not roll back or report a renderer fallback as success.
- Keep contracts transport-safe: no Theia, DOM, SVG, representation, projection implementation, browser, or editor types.
- Story 2.2 will own AST-aware source planning and serialization. This story defines and orchestrates the stage boundary only.
- Preserve the current guided-preview session API; migrate it only when a later story explicitly replaces its authority path.

### Existing Code To Extend

- `kernel/authoring-model/.../AuthoringTransactionModels.kt`: Story 1.2 transaction, Revision Guard, lifecycle, validation, result, provenance, and diagnostics. Extend these cohesive contracts rather than duplicating them.
- `kernel/authoring-model/.../AuthoringPreviewModels.kt`: preview carries an optional Revision Guard for existing caller compatibility.
- `kernel/runtime/.../AthenaAuthoringSessionRuntimeService.kt`: existing preview session has no canonical mutation behavior. Preserve its submit/state/snapshot/restore/applyDecision behavior.
- `kernel/runtime/build.gradle.kts`: already depends on authoring-model and compiler; add no dependency unless a tested implementation requires it.

### Intended Runtime Boundary

```text
SemanticAuthoringTransaction
  -> ordered validation stage authority
  -> active Revision Guard authority
  -> mutation authority handoff
  -> compile/reprojection authority
  -> SemanticAuthoringTransaction outcome
```

Use small interfaces/fakes in tests to prove call order and no-call guarantees. Do not add test-only methods to production code.

### Diagnostic Requirements

Diagnostics must support stable code plus authority, lifecycle stage, severity, message, optional subject/source range/related ids, and optional recovery action. Story 1.3 must at least admit:

- `authoring.preview.stale`
- `authoring.validation.stop-downstream`
- `authoring.projection.failed-after-commit`

Do not collapse these into free-form strings or generic projection unavailable errors.

### Testing Requirements

- Follow RED-GREEN-REFACTOR and record the expected RED failures.
- Assert exact validation stage order and exact stage at which processing stops.
- Assert mutation/reprojection authority invocation counts, not only final state.
- Assert committed failure keeps mutation id and committed revision.
- Run Gradle commands sequentially on Windows.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text changes.

### Previous Story Intelligence

- Story 1.2 established one-intent transaction creation and exact UTF-8 SHA-256 Revision Guard hashing.
- Reuse `SemanticAuthoringTransactionFactory`; do not weaken its cardinality or preview-guard checks.
- Story 1.2 found no duplicate adjacent transaction/lifecycle model, so this story should evolve that file rather than create overlapping contracts.
- Story 1.2 verification commands were `:kernel:authoring-model:test` focused and full, always sequential.

### Scope Boundaries

- No accepted entity/relationship product workflow yet.
- No frontend UI or transport payload migration.
- No AST insertion calculation or `.athena` serialization.
- No undo/redo, multi-intent transaction, collaboration, AI planning, or rollback engine.
- No direct mutation of Presentation IR, representation/projection occurrences, sheets, layout, routes, or geometry.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 1, Story 1.3.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-3, AD-5, AD-10, AD-16, AD-18.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Lifecycle, Validation Order, Diagnostic Envelope, Mutation Handoff.
- `_bmad-output/implementation-artifacts/m31/1-2-define-single-intent-transaction-and-revision-guard.md` - prior story implementation evidence.

## Dev Agent Record

### Debug Log

- RED model: focused test failed on missing diagnostic authority, severity, recovery, lifecycle, and stable diagnostic codes.
- RED runtime: focused test failed on missing validation-stage and transaction-runtime APIs.
- GREEN model: focused `AuthoringLifecycleContractTest` passed after contract completion.
- GREEN runtime: focused `SemanticAuthoringTransactionRuntimeTest` passed after ordered orchestration implementation.
- Regression: full `:kernel:authoring-model:test` and `:kernel:runtime:test` passed sequentially.

### Completion Notes

- Added explicit nine-stage validation order and structured diagnostic envelope.
- Added distinct stale, blocked STOP_DOWNSTREAM, rejected, cancelled, reprojected, and projection-failed outcomes.
- Kept committed mutation id and Revision Guard when reprojection fails.
- Kept source planning behind an injected stage boundary for Story 2.2; no frontend or second serializer was added.
- AC evidence: AC-1 success stage-order test; AC-2 stale no-call test; AC-3 STOP_DOWNSTREAM test; AC-4 committed projection-failure test and result invariant; AC-5 reject/cancel no-call test; AC-6 full runtime regression; AC-7 CodeGraph overlap review, cleanup ledger, diff check, and encoding audit.
- Polish/Purge: no duplicate lifecycle or transaction model was found. Three intentionally retained integration/compatibility paths are recorded as M31-CL-001..003 with owner, reason, target story, and verification.

## File List

- `_bmad-output/implementation-artifacts/m31/1-3-enforce-transaction-validation-and-lifecycle-outcomes.md`
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringTransactionModels.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringLifecycleContractTest.kt`
- `kernel/runtime/build.gradle.kts`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/SemanticAuthoringTransactionRuntime.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/SemanticAuthoringTransactionRuntimeTest.kt`

## Change Log

- 2026-07-21: Ultimate context engine analysis completed; comprehensive developer guide created.
- 2026-07-21: Implemented and verified ordered transaction validation and distinct lifecycle outcomes; completed polish/purge ledger review.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent lifecycle, transaction, runtime, transport, tests, docs, and compatibility paths.
- Remove stale artifacts or ledger them with owner, reason, target milestone, and verification.
- Confirm no duplicate lifecycle state machine, free-form diagnostic authority, or unowned compatibility path remains.
- Re-run final verification after cleanup.
