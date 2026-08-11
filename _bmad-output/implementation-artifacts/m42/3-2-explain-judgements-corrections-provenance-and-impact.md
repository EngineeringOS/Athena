---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 3.2: Explain Judgements, Corrections, Provenance, And Impact

Status: done

## Story

As an engineer responsible for design decisions,
I want plain explanations, exact source evidence, and non-executing correction options,
so that I understand consequences and choose every engineering change myself.

## Acceptance Criteria

1. Failed or unresolved evaluation publishes immutable Judgements naming exact subjects, problem,
   expected/actual typed values, governing authority, correction direction, and project/knowledge
   sources in plain engineering language; internal codes remain secondary metadata.
2. Correction evidence is sealed and non-executing: `ChangeValue`, `AddRelationship`,
   `SelectExistingProvider`, `BindPart`, and `AddRequiredFunction` options reference affected source
   subjects and ordered evidence. Eligible providers are existing project Entities in stable-ID order;
   no option mutates source, selects a Part, creates a subject, or connects a provider.
3. Portable Provenance uses normalized relative paths, rejects absolute/`..` paths, carries package and
   version where applicable, uses zero-based UTF-16 spans, and records ordered derivation steps. Only
   LSP maps portable references to file URIs.
4. Re-evaluation of one governed input change emits deterministic impact evidence for every changed or
   unchanged Requirement, formula, Constraint, explanation, and Correction Option. Runtime, SCM, LSP,
   CLI, and frontend query this result and never reconstruct evaluator state.

## Tasks / Subtasks

- [x] Task 1: Install immutable judgement and correction contracts (AC: 1-2)
  - [x] Add typed judgement status, expected/actual values, authority, explanation, and correction
        direction contracts with deterministic ordering.
  - [x] Add sealed non-executing correction option types and source-subject/evidence references.
  - [x] Add red tests proving options cannot mutate source or create/select subjects; green then refactor.
- [x] Task 2: Add portable provenance and derivation trace (AC: 3)
  - [x] Normalize and validate relative paths and UTF-16 source spans; preserve package/version metadata.
  - [x] Record ordered subject, definition, formula, and Constraint derivation steps in Validation.
  - [x] Add malformed path/span tests and deterministic serialization-order tests.
- [x] Task 3: Publish deterministic impact evidence (AC: 4)
  - [x] Compare two evaluator inputs/results and emit changed/unchanged Requirement, formula,
        Constraint, explanation, and Correction Option impact entries in stable order.
  - [x] Keep impact authority in compiler; add tests for one rated-current change and unchanged evidence.
- [x] Task 4: Verify and complete records (AC: 1-4)
  - [x] Run affected tests, full `test`, audits, encoding audit, and `git diff --check` sequentially.
  - [x] Complete BMad records and mark `review` then `done` only after all ACs pass.

## Dev Notes

- Follow AD-20, AD-22, AD-23, AD-29, AD-30, AD-31, AD-40. Validation remains separate from
  Engineering Reality and Knowledge Document; no third model, evaluator in adapters, AI, renderer,
  automatic provider selection, or compatibility shim.
- Reuse `EngineeringValidationDocument`, typed `EngineeringValue`, `SourceProvenance`, and stable
  definition/subject identities from Stories 1.2, 2.1, 2.2, and 3.1.
- Diagnostics are human-first. Codes and transport fields are metadata only. Corrections describe
  options; they never apply edits.
- Production source remains free of milestone/demo/proof names and retired semantic macro/component/
  connection authority. Keep all M42 evidence under this directory.

### Testing Requirements

- Assert exact plain-language subjects, expected/actual values, authorities, correction direction,
  provenance paths/spans, stable ordering, immutability, and no provider/Part/Entity mutation.
- Required sequential commands:
  ` .\gradlew.bat --no-daemon --console=plain :kernel:validation:test`
  ` .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  ` .\gradlew.bat --no-daemon --console=plain test`
  ` powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  ` powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  ` git diff --check`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Created through BMad create-story workflow using complete M42 PRD, architecture, epics, sprint
  status, and Story 3.1 implementation intelligence.

### Completion Notes List

- Added immutable `EngineeringJudgement`, sealed correction options, portable provenance, derivation,
  and impact contracts to Validation.
- Extended compiler evaluator with plain-language failure explanations and stable existing-provider
  correction options; added compiler-owned impact comparison.
- Verified correction non-execution, path/span rejection, stable ordering, and changed/unchanged impact.

### File List

- `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringValidationDocument.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/KnowledgeRequirementEvaluator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/EngineeringImpactCalculator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/knowledge/KnowledgeRequirementEvaluatorTest.kt`

### Change Log

- 2026-08-05: Created Story 3.2 from M42 Epic 3 after Story 3.1 completion.
- 2026-08-05: Implemented judgement, correction, provenance, and deterministic impact contracts; all sequential verification passed.
