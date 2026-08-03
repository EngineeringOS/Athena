---
story_key: 3-2-author-and-validate-route-quality-policy
epic: m37-e3
requirements: [FR-14]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.2: Author And Validate Route Quality Policy

Status: review

## Story

As a drawing-profile author,
I want explicit route-quality rules and weights,
so that professional readability is governed and testable rather than subjective renderer behavior.

## Acceptance Criteria

1. Route Quality Policy has a typed compiler-owned schema separating hard rejects from scored preferences.
2. Hard rejects cover missing anchors, semantic incompatibility, route/body intersection, invalid channel use, and violated clearance.
3. Soft scoring covers crossings, bends, lane changes, label collisions, length, density, and stable tie-breaking.
4. Project intent may influence preferences but cannot redefine schema or weaken hard rejects.
5. Invalid weights, unknown criteria, and attempts to disable hard rejects fail with typed diagnostics.
6. Focused policy/negative tests, routing regression, compiler regression if touched, source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED Route Quality Policy tests (AC: 1, 2, 3, 5)
  - [x] Add valid policy test for hard rejects and scored preferences.
  - [x] Add invalid tests for unknown criterion, invalid weight, and disabled hard reject.
  - [x] Add route quality scoring test that uses metrics from accepted RouteFacts.
- [x] Implement typed route-quality policy schema (AC: 1, 2, 3)
  - [x] Add responsibility-named data types in routing-model; no milestone names, no `V0`/`V1`.
  - [x] Keep schema compiler-owned; do not let project source redefine hard reject vocabulary.
  - [x] Keep policy independent from renderer, SVG, XML, planner-native objects, and external standards.
- [x] Implement validation and scoring (AC: 4, 5)
  - [x] Validate hard reject coverage and reject attempts to disable hard rejects.
  - [x] Validate soft scoring criteria and weights.
  - [x] Score existing `RouteQualityMetrics` deterministically.
- [x] Wire route engine evidence minimally (AC: 3, 4)
  - [x] Keep accepted RouteFacts compatible with existing `RouteQuality`.
  - [x] Add policy score evidence only from compiled route metrics and diagnostics.
  - [x] Do not introduce profile DSL here unless required by existing language patterns.
- [x] Verify and record gates (AC: 6)
  - [x] Run focused route-quality tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Compiler owns Route Quality Policy schema and hard rejects.
- Drawing/profile authors may provide weights and thresholds, but cannot remove hard rejects.
- Do not implement full Drawing Standard Profile vocabulary; E4 handles drawing grammar and line classes.
- Do not add ELK adapter, AI layout, SVG/XML authority, or renderer repair.
- No production class with `Proof`, `Demo`, `Sample`, milestone names, or `V0`/`V1`.

### Current Code Intelligence

- `RouteQuality`, `RouteQualityMetrics`, `RouteFact`, and `RouteFactSnapshot` live in `RouteConstraintsAndFacts.kt`.
- `AthenaRouteEngine` computes `RouteQualityMetrics` from route segments and currently emits `RouteQuality.satisfied()` or degraded/fallback state.
- Story 3.1 added route lane assignment and `laneDiagnostics`; reuse those facts for lane-change and density criteria.
- Existing route-quality diagnostic support exists in `RouteQualityDiagnostics.kt`; reuse or evolve by responsibility, not by milestone naming.

### Required Hard Rejects

- `missing-anchor`
- `semantic-incompatibility`
- `route-body-intersection`
- `invalid-channel`
- `clearance-violation`

### Required Soft Criteria

- `crossings`
- `bends`
- `lane-changes`
- `label-collisions`
- `length`
- `density`
- `stable-tie-breaker`

### TDD And Verification

- RED first: route-quality policy tests fail before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.RouteQualityPolicyTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 3.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-14, NFR-2, NFR-4, NFR-5]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Route Quality Ownership]
- [Source: `_bmad-output/implementation-artifacts/m37/3-1-allocate-route-lanes-deterministically.md` - Previous story lane evidence]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-30: Added RED route quality policy tests for required hard rejects, scored preferences, invalid criteria, invalid weights, disabled hard rejects, and deterministic scoring.
- 2026-07-30: Implemented compiler-owned `RouteQualityPolicy`, hard reject validation, scoring rule validation, diagnostics, and metric scoring.
- 2026-07-30: Verification pass: focused `RouteQualityPolicyTest`, full `:kernel:routing-model:test`, full `:kernel:compiler:test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Route Quality Policy now has compiler-owned hard rejects and scored preferences.
- Attempts to disable hard rejects, use unknown criteria, or use invalid weights fail through typed diagnostics.
- Route quality scoring now derives deterministic penalty components from `RouteQualityMetrics`.

### File List

- _bmad-output/implementation-artifacts/m37/3-2-author-and-validate-route-quality-policy.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteQualityPolicy.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteQualityPolicyTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 3.2 from finalized M37 PRD, addendum, epics, route-quality code intelligence, and Story 3.1 learnings.
- 2026-07-30: Implemented route-quality policy schema, validation diagnostics, and deterministic metric scoring.
