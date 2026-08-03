---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.2: Define The Domain-Neutral ProjectionConstruct Contract

Status: review

## Story

As a compiler maintainer,
I want a kernel-owned `ProjectionConstruct` contract,
so that the kernel stays domain-neutral.

## Acceptance Criteria

1. The contract defines identity, source trace, membership, and validation shape; it names no
   electrical, mechanical, or process vocabulary.
2. No `Rail`, `Rung`, `ContactGroup`, or other domain construct name appears in kernel production
   source (`kernel/*/src/main`) after the Story 2.1 retirement.
3. A new domain package can add constructs without changing kernel code.
4. No generic graph API, universal `Fact` base class, or empty wrapper model is introduced.

## Tasks / Subtasks

- [x] Add `ProjectionConstruct` contract + id to projection-model (identity, source trace,
  membership, validation shape).
- [x] Add `EngineeringProjectionConstruct` IR carrier on projection views.
- [x] Kernel-neutrality test: no domain construct names in kernel production sources.
- [x] Contract tests; run focused tests, hygiene + encoding audits; update story + sprint.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 2.2]
- [Source: PRD FR-7, FR-12; ARCHITECTURE-SPINE.md AD-11, AD-15]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added ProjectionConstruct contract, IR carrier, neutrality + contract tests.
- 2026-08-02: Compiler suite green (364 tests).

### Completion Notes List

- `ProjectionConstruct` interface + `ProjectionConstructId` in projection-model: identity,
  source trace, membership, validation shape; no domain vocabulary.
- `EngineeringProjectionConstruct` IR carrier on projection views (name, kind, sheet, members).
- `KernelDomainNeutralityTest` scans kernel/src/main for the seven domain construct
  implementation names; passes.
- `ProjectionConstructContractTest` verifies the contract shape and plain-language validation.

### File List

- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionConstructContract.kt
- kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionConstructContractTest.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/KernelDomainNeutralityTest.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 2.2 and PRD FR-7/FR-12.
- 2026-08-02: Implemented domain-neutral ProjectionConstruct contract + neutrality guard;
  compiler green; marked review.
