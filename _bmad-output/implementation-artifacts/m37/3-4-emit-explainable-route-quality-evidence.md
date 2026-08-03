---
story_key: 3-4-emit-explainable-route-quality-evidence
epic: m37-e3
requirements: [FR-15, FR-16]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.4: Emit Explainable Route Quality Evidence

Status: review

## Story

As an engineer inspecting a route,
I want its quality and degradation evidence exposed through compiler and IDE protocols,
so that weak routing can be corrected from source or policy.

## Acceptance Criteria

1. Accepted `RouteFact`s expose route quality grade, score components, degraded reasons, selected lane, selected channel sequence, Connection Intent influence, planner identity, compiler snapshot, and source provenance.
2. Rejected planner candidates and degraded invalid fixtures emit machine-readable diagnostics with affected route ids, violated facts, planner identity, snapshot id, and source span where available.
3. A route cannot be reported successful when any hard reject, missing route proof, unresolved snapshot evidence, or blocking lane diagnostic exists.
4. Normalized compiler, LSP, and presentation-facing payloads carry Athena-owned facts only; planner-native objects, SVG/DOM/XML markup, and mutable route proposal objects do not cross protocol boundaries.
5. Focused route evidence, diagnostic, normalization, and invalid-fixture tests, routing regression, compiler regression if touched, source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED route-quality evidence tests (AC: 1, 2, 3, 4)
  - [x] Add accepted-route evidence test proving grade, score components, lane, channel, intent influence, planner identity, compiler snapshot, and provenance.
  - [x] Add rejected-candidate diagnostic normalization test using Story 3.3 candidate rejections.
  - [x] Add negative success-gate test for hard reject or unresolved proof evidence.
  - [x] Add protocol hygiene test proving normalized payload contains Athena facts only.
- [x] Implement route-quality evidence model (AC: 1, 4)
  - [x] Add responsibility-named types only; no `Proof`, `Demo`, `Sample`, milestone names, `V0`/`V1`, ELK types, or adapter shims.
  - [x] Derive evidence from `RouteFact`, `RouteQualityPolicyCompiler`, `RoutePlannerCandidateComparison`, and existing lane diagnostics.
  - [x] Preserve source provenance and compiler snapshot identity without making planner candidates authoritative.
- [x] Implement diagnostics and success gating (AC: 2, 3)
  - [x] Normalize candidate rejections into compiler/LSP-safe diagnostic records.
  - [x] Block success for hard rejects, missing evidence, unresolved snapshot evidence, and blocking lane diagnostics.
  - [x] Keep accepted evidence immutable and detached from transient planner proposal objects.
- [x] Verify and record gates (AC: 5)
  - [x] Run focused route-quality evidence tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Route quality is compiler-owned evidence, not renderer taste and not planner authority.
- Accepted evidence is derived from Athena `RouteFact`s, route quality policy scoring, lane/channel facts, Connection Intent influence, planner identity, compiler snapshot, and source provenance.
- Rejected planner candidates remain disposable derived IR. Diagnostics may quote planner identity and violated facts, but must not expose planner-native objects or proposal mutation surfaces.
- LSP/presentation-facing payloads must contain normalized Athena data only.
- No compatibility shims. Athena is pre-public.

### Current Code Intelligence

- Story 3.1 added deterministic route lane assignments and `RouteFactSnapshot.laneDiagnostics`.
- Story 3.2 added `RouteQualityPolicyCompiler`, required hard rejects, scoring criteria, and route quality metrics.
- Story 3.3 added `RoutePlannerCandidateCompiler`, transient candidate models, deterministic candidate comparison, and rejection records.
- Existing route models live in `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing`.
- Protocol or compiler changes are only needed if evidence must cross module boundaries now; otherwise keep Story 3.4 focused in `routing-model`.

### Expected Diagnostics

- `route.quality.evidence.hard-reject`
- `route.quality.evidence.snapshot.missing`
- `route.quality.evidence.proof.missing`
- `route.quality.evidence.lane.blocking`
- `route.quality.evidence.candidate.rejected`
- `route.quality.evidence.protocol-authority.invalid`

### TDD And Verification

- RED first: route evidence test fails before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.RouteQualityEvidenceTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 3.4]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-15, FR-16]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Route Quality Ownership, ELK Boundary]
- [Source: `_bmad-output/implementation-artifacts/m37/3-1-allocate-route-lanes-deterministically.md` - Lane evidence]
- [Source: `_bmad-output/implementation-artifacts/m37/3-2-author-and-validate-route-quality-policy.md` - Route Quality Policy]
- [Source: `_bmad-output/implementation-artifacts/m37/3-3-validate-and-compare-planner-candidates.md` - Planner candidate validation]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.RouteQualityEvidenceTest` failed before implementation with unresolved `RouteQualityEvidenceCompiler` and evidence model symbols.
- GREEN: focused `RouteQualityEvidenceTest` passed after adding route quality evidence compiler.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- Hygiene: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- Encoding: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Whitespace: `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added immutable route quality evidence and normalized payloads derived from accepted `RouteFact`s.
- Accepted evidence includes grade, score components, degraded reasons, lane assignment, selected channels, Connection Intent influence, planner id, compiler snapshot, and provenance.
- Candidate rejections normalize to blocking route-quality diagnostics without exposing transient planner objects.
- Success gate blocks hard rejects, snapshot mismatches, lane evidence mismatches, and lane diagnostics.
- No compiler module touched; `:kernel:compiler:test` not required for this story.

### File List

- _bmad-output/implementation-artifacts/m37/3-4-emit-explainable-route-quality-evidence.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteQualityEvidence.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteQualityEvidenceTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 3.4 from finalized M37 PRD, addendum, epics, and Stories 3.1-3.3 learnings.
- 2026-07-30: Implemented route quality evidence emission, diagnostic normalization, payload hygiene, and routing tests.
