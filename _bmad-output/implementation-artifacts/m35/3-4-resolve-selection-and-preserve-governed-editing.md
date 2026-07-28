---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 3.4: Resolve Selection And Preserve Governed Editing

Status: review

## Story

As an engineer,
I want Cabinet selection to resolve through typed capabilities and governed source mutation contracts,
so that graphic interaction cannot corrupt Athena's source-of-truth model.

## Acceptance Criteria

1. Given a selected `GraphicOccurrenceId`, when the frontend asks LSP for its subject, then typed trace maps through existing interaction subject concepts backed by a capability registry contract, and semantic-id prefix inference, DOM ids, labels, and SVG nodes are not fallback authority.
2. Given future move, representation change, or engineering replacement intent, when the governed flow is evaluated, then it follows `SemanticActionIntent -> capability/command translation -> AuthoringIntent -> SemanticAuthoringTransaction -> AuthoringPreview + AuthoringSourceEditEvidence -> compile/lint -> accept/reject -> rerender`, and each action targets the authoritative installation, binding/profile, or engineering declaration.
3. Given a UI, renderer, or agent attempts direct mutation, when boundary tests run, then SVG DOM, Graphic Primitive IR, Presentation IR, representation occurrences, and renderer state cannot be authoritative mutation targets.
4. Given the Cabinet view is selected and source reveal is invoked, when interaction proof runs, then the exact governed source span opens and the resolved capability/subject evidence matches the trace table.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and prefix heuristics, direct graphic mutation hooks, stale selection DTOs, and compatibility paths are purged.

## Tasks / Subtasks

- [x] Add RED tests for trace-backed selection resolution and edit boundary (AC: 1..4)
  - [x] Cover `GraphicOccurrenceId` resolving to typed subject and source reveal from the trace table.
  - [x] Cover governed action preview path evidence for future move/rebind/replace intents.
  - [x] Cover direct mutation target rejection for DOM/SVG/Graphic/PIR/renderer state.
  - [x] Cover prefix/label/DOM fallback rejection.
- [x] Implement Cabinet selection resolver contract (AC: 1, 4)
  - [x] Resolve only from `GraphicOccurrenceTraceTable`.
  - [x] Emit typed subject evidence and exact source span.
  - [x] Do not infer subject kind from string prefixes, labels, primitive ids, or DOM ids.
- [x] Implement governed edit boundary model (AC: 2..3)
  - [x] Model allowed governed action path evidence.
  - [x] Model forbidden direct mutation targets and fail-closed diagnostics.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched paths for prefix heuristics, direct graphic mutation hooks, stale DTOs, XML, fallback, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 3.4 defines backend/kernel contracts for Cabinet selection resolution and editing boundaries. It does not implement full UI commands, actual source mutations, or graphic drag authoring.

### Architecture Requirements

- Selection starts with `GraphicOccurrenceId`, then resolves through the normalized `GraphicOccurrenceTraceTable`.
- Source reveal uses the trace table source span; labels, primitive ids, DOM ids, SVG node ids, and semantic-id prefixes are not authority.
- Future editing must remain a governed transaction path; direct mutation of renderer state, Presentation IR, Graphic Primitive IR, SVG DOM/files, or representation occurrence data is forbidden.
- Existing frontend prefix heuristics are stale for M35 Cabinet and must not be the Cabinet selection contract.

### Previous Story Intelligence

- Story 3.3 added `GraphicOccurrenceTraceTable`, selectable primitive refs, trace entries, diagnostics, proof, and deterministic transport.
- Story 3.2 added `CabinetRouteFact`; route selection can be modeled as a trace subject but not as DOM geometry guessing.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 3, Story 3.4.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-33, FR-35..FR-36.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-13, AD-14 selection/edit boundary.
- `_bmad-output/implementation-artifacts/m35/3-3-trace-every-graphic-occurrence-to-governed-source.md` - trace table handoff.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T05:46:00+08:00 - Created Story 3.4 from M35 Epic 3 backlog using PRD, architecture spine, epics, and Story 3.3 trace evidence.
- 2026-07-28T05:46:00+08:00 - Started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- 2026-07-28T05:55:00+08:00 - RED: `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test --tests com.engineeringood.athena.presentation.CabinetSelectionAndEditBoundaryTest` failed on missing selection/edit contracts and missing direct module dependencies.
- 2026-07-28T06:00:00+08:00 - GREEN: targeted Cabinet selection/edit boundary test passed.
- 2026-07-28T06:01:00+08:00 - Regression: full `:kernel:presentation-model:test` passed.
- 2026-07-28T06:02:00+08:00 - Audit: searched presentation/frontend touched boundary terms for prefix, DOM/SVG/XML, fallback, compatibility, and direct mutation; hits were existing presentation/frontend terminology or explicit negative tests/contracts.

### Completion Notes List

- Added a Cabinet graphic selection resolver that starts with `GraphicOccurrenceId`, resolves through `GraphicOccurrenceTraceTable`, requires the typed subject from `SemanticCapabilityRegistry`, and emits source reveal from the trace span.
- Added fail-closed diagnostics for forbidden selection fallback and direct graphic mutation targets.
- Added governed edit evidence proof for the required future path: semantic intent, authoring intent, transaction, preview, source edit evidence, compile/lint, and rerender.

### File List

- `_bmad-output/implementation-artifacts/m35/3-4-resolve-selection-and-preserve-governed-editing.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/presentation-model/build.gradle.kts`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/CabinetSelectionAndEditBoundary.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/CabinetSelectionAndEditBoundaryTest.kt`

### Change Log

- 2026-07-28 - Created Story 3.4 implementation guide.
- 2026-07-28 - Moved Story 3.4 to in-progress.
- 2026-07-28 - Implemented trace-backed Cabinet selection resolution and governed edit boundary contracts.
