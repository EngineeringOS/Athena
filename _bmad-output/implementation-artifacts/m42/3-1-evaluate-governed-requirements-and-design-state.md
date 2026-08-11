---
baseline_commit: c00b416c463d3e18876dced3be6b750d2f0652ff
---

---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 3.1: Evaluate Governed Requirements And Design State

Status: done

## Story

As a multidisciplinary engineer,
I want governed knowledge evaluated against my canonical project subjects,
so that I know exactly which requirements are satisfied and whether the design is ready, incomplete, or invalid.

## Acceptance Criteria

1. Canonical Entities, Part bindings, Capabilities, Relationships, authored Requirement/Constraint
   applications, and one Knowledge Document resolve every proof-scope subject exactly once with stable
   subject/definition references. Missing/ambiguous resolution names exact project and package sources;
   no guessing.
2. Authored facts, Part implementation facts, and compiler derivations remain distinct. Equal normalized
   facts may corroborate; disagreement is `INVALID` with source-specific Provenance; no precedence. Part
   binding never creates authored Functions or Ports.
3. Closed evaluator gives every Requirement exactly one `SATISFIED`, `UNSATISFIED`, or `UNRESOLVED`
   result. Existence, equality, ordering, interval, dimension, cardinality, and all/any rules evaluate
   exactly/deterministically. Compiler verifies provider choices but never selects/creates providers.
4. Ready, missing-provider/protection, failed well-formed blocking Constraint, malformed Relationship,
   ambiguous definition, corrupt package, and incompatible-dimension fixtures each produce immutable
   `EngineeringValidationDocument` classified `READY`, `INCOMPLETE`, or `INVALID` per AD-29. `INVALID`
   contains diagnostics/Provenance but no satisfaction or completed Constraint Judgement collection.

## Tasks / Subtasks

- [x] Task 1: Install validation document and state contracts (AC: 1-4)
  - [x] Add immutable `EngineeringValidationDocument`, total `ValidationState`, Requirement,
        Satisfaction, diagnostic, and source-specific fact provenance contracts.
  - [x] Enforce INVALID cannot carry satisfaction collection or completed judgement collection.
- [x] Task 2: Resolve project subjects and typed facts (AC: 1-2)
  - [x] Add compiler-owned resolution over Engineering Reality + Knowledge Document; fail closed for
    missing/ambiguous definitions and preserve project/package source evidence.
  - [x] Keep authored, Part, and derived facts distinct; detect disagreement without precedence.
  - [x] Add red tests for exact subject identity, Part no-anatomy mutation, and conflict INVALID; green,
        then refactor.
- [x] Task 3: Evaluate closed predicates and classify state (AC: 3-4)
  - [x] Implement bounded exact evaluation for capability match, comparisons, intervals, dimensions,
    cardinality, and all/any composition with deterministic evidence.
  - [x] Publish one result per Requirement and `READY`/`INCOMPLETE`/`INVALID` classification; provider
    selection/creation remains absent.
  - [x] Add materially distinct state fixtures and no-satisfaction INVALID assertions.
- [x] Task 4: Verify and complete records (AC: 1-4)
  - [x] Run affected Gradle tests sequentially, full `test`, audits, encoding, and `git diff --check`.
  - [x] Complete BMad records; mark `review` only after all ACs pass.

## Dev Notes

- Follow AD-20, AD-22, AD-23, AD-27, AD-29, AD-30, AD-31, AD-38, AD-40. Validation is separate
  document; never mutate Engineering Reality or create a third Reality.
- Reuse `EngineeringKnowledgeDocument`, typed `EngineeringValue`, Relationship/Flow, and exact formula
  contracts from Stories 1.2/2.1/2.2. No string evaluator, arbitrary execution, compatibility shim,
  automatic provider, Pattern, AI, or renderer behavior.
- `INVALID` has diagnostics and Provenance only. `INCOMPLETE` may carry unsatisfied/unresolved evidence.
  `READY` requires all required knowledge and blocking constraints satisfied.
- Human-first diagnostics name exact subject, expected/actual state, correction direction, and source.

### Testing Requirements

- Assert literal state, result, stable evidence ordering, expected/actual typed values, source kinds,
  and no mutation/provider creation.
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

- Created through BMad create-story workflow from complete M42 artifacts and Story 2.2 intelligence.

### Completion Notes List

- Added immutable validation state, requirement status, evidence, diagnostic, and provenance contracts.
- Added fail-closed evaluator with deterministic requirement ordering and authored-only provider verification.
- Verified READY, INCOMPLETE, and INVALID fixtures; INVALID publishes diagnostics only.

### File List

- `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringValidationDocument.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/KnowledgeRequirementEvaluator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/knowledge/KnowledgeRequirementEvaluatorTest.kt`

### Change Log

- 2026-08-05: Created Story 3.1 from M42 Epic 3 after closing Epic 2 contract stories.
- 2026-08-05: Implemented governed requirement evaluation and completed sequential verification.
