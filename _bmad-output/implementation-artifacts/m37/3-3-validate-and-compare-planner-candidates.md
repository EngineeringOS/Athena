---
story_key: 3-3-validate-and-compare-planner-candidates
epic: m37-e3
requirements: [FR-25, FR-26, FR-27]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.3: Validate And Compare Planner Candidates

Status: review

## Story

As an engineer or AI agent,
I want Athena to compare planner proposals under its own rules,
so that route optimization remains replaceable and non-authoritative.

## Acceptance Criteria

1. Planner proposals are transient derived IR and cannot mutate Athena source, identity, representation, or policy.
2. Athena rejects candidates missing endpoints, anchors, lanes, constraints, snapshot evidence, or route proof fields before scoring.
3. Valid candidates are compared deterministically through Route Quality Policy and stable tie-breakers.
4. Rejection records planner identity, snapshot, violated facts, and source provenance where available.
5. The Athena-native planner path is proven; no ELK runtime dependency or adapter is introduced.
6. Focused planner/mismatch/determinism tests, routing regression, compiler regression if touched, source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED planner candidate tests (AC: 1, 2, 3, 4, 5)
  - [x] Add valid candidate comparison test using Route Quality Policy scoring.
  - [x] Add missing endpoint/anchor/lane/snapshot/proof rejection tests.
  - [x] Add deterministic tie-breaker test.
- [x] Implement transient planner candidate model (AC: 1, 4)
  - [x] Add responsibility-named types only; no `Proof`, `Demo`, `Sample`, milestone names, `V0`/`V1`, ELK types, or adapter shims.
  - [x] Keep proposal facts disposable and snapshot-bound.
  - [x] Carry planner identity and source provenance evidence where provided.
- [x] Implement validation and deterministic comparison (AC: 2, 3, 5)
  - [x] Reject incomplete candidates before scoring.
  - [x] Compare valid candidates through `RouteQualityPolicyCompiler.score`.
  - [x] Use stable route/candidate/planner ids as deterministic tie-breakers.
- [x] Verify and record gates (AC: 6)
  - [x] Run focused planner candidate tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Planner proposals are candidates. Athena validation decides.
- No ELK dependency, no external planner runtime, no planner-native objects in accepted RouteFacts.
- Candidate comparison consumes route metrics, lane/proof evidence, and Route Quality Policy only.
- Do not alter source, semantic IDs, representation bindings, or Projection Policy from planner data.
- No compatibility shims. Athena is pre-public.

### Current Code Intelligence

- Story 3.1 added route lane assignment and lane diagnostics to `RouteFactSnapshot`.
- Story 3.2 added `RouteQualityPolicyCompiler` and deterministic metric scoring.
- `AthenaRouteEngine` is Athena-native planner path and already produces accepted `RouteFactSnapshot`.
- Existing route models live in `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing`.

### Expected Diagnostics

- `planner.candidate.endpoint.missing`
- `planner.candidate.anchor.missing`
- `planner.candidate.lane.missing`
- `planner.candidate.snapshot.missing`
- `planner.candidate.proof.missing`

### TDD And Verification

- RED first: planner candidate test fails before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.RoutePlannerCandidateTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 3.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-25, FR-26, FR-27]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - ELK Boundary]
- [Source: `_bmad-output/implementation-artifacts/m37/3-2-author-and-validate-route-quality-policy.md` - Previous story route quality policy]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.RoutePlannerCandidateTest` failed before implementation with unresolved `RoutePlannerCandidateCompiler` and candidate model symbols.
- GREEN: focused `RoutePlannerCandidateTest` passed after adding transient planner candidate compiler.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- Hygiene: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- Encoding: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Whitespace: `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added transient planner candidate model and compiler under routing-model.
- Candidate validation rejects missing snapshot, endpoint, anchor, lane, constraint, and route evidence before quality scoring.
- Valid candidates rank through `RouteQualityPolicyCompiler.score`, then stable candidate/planner/route tie-breakers.
- Rejections carry candidate id, planner id, snapshot id, affected route ids, source provenance, and violated facts.
- No compiler module touched; `:kernel:compiler:test` not required for this story.

### File List

- _bmad-output/implementation-artifacts/m37/3-3-validate-and-compare-planner-candidates.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RoutePlannerCandidate.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RoutePlannerCandidateTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 3.3 from finalized M37 PRD, addendum, epics, and Stories 3.1/3.2 learnings.
- 2026-07-30: Implemented transient planner candidate validation, deterministic comparison, rejection evidence, and routing tests.
