---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 3.3: Trace Every Graphic Occurrence To Governed Source

Status: review

## Story

As an engineer,
I want every selectable Cabinet occurrence to reveal its authoritative source chain,
so that I and future agents can explain what is shown without reading DOM or labels.

## Acceptance Criteria

1. Given composed Cabinet bodies, labels, hotspots, routes, and decorative primitives, when `GraphicOccurrenceTraceTable` is built, then each selectable primitive carries one `GraphicOccurrenceId`, decorative primitives are explicitly nonselectable, and table completeness is proven, and `GraphicOccurrenceId` remains distinct from `GraphicPrimitiveId`.
2. Given one mounted occurrence, when its trace entry is inspected, then ordered typed ids, source spans, and digests identify semantic subject, installation declaration, mounted occurrence, binding rule, representation definition, resource snapshot, and owning declarations, and no source bytes, filesystem handles, snapshot objects, DOM ids, or label guesses are transported.
3. Given duplicate, missing, synthetic, or mismatched trace subjects, when presentation validation runs, then composition fails with stable diagnostics and no selectable primitive is emitted without complete trace.
4. Given LSP transport, when trace payloads are serialized twice, then table version, ordering, ids, spans, and digests are byte-stable.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and duplicate per-primitive traces, DOM/source guesses, stale transport DTOs, and compatibility names are purged.

## Tasks / Subtasks

- [x] Add RED tests for normalized graphic occurrence trace (AC: 1..4)
  - [x] Cover selectable primitives carrying occurrence ids while decorative primitives are nonselectable.
  - [x] Cover complete typed source chain for one mounted occurrence.
  - [x] Cover duplicate, missing, synthetic, and mismatched trace diagnostics.
  - [x] Cover deterministic byte-stable transport serialization.
- [x] Implement trace table models in `kernel:presentation-model` (AC: 1..4)
  - [x] Add `GraphicOccurrenceId`, selectable primitive refs, normalized trace entries, proof, diagnostics, and transport DTO.
  - [x] Keep occurrence id distinct from primitive id and transport no source bytes, DOM ids, filesystem handles, or snapshot objects.
- [x] Implement trace table compiler/validator (AC: 1..4)
  - [x] Validate table completeness for selectable primitives.
  - [x] Validate duplicate and mismatched subjects fail closed.
  - [x] Emit deterministic sorted proof and stable transport bytes.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched paths for duplicate per-primitive trace payloads, DOM/source guesses, stale DTOs, XML, fallback, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 3.3 builds the normalized trace model and deterministic transport proof. It does not implement frontend selection, source reveal commands, editing, or new renderer behavior; those belong to Story 3.4.

### Architecture Requirements

- `presentation-model` owns the normalized `GraphicOccurrenceTraceTable`; primitives carry ids only.
- Trace entries use ordered typed ids, source spans, and digests. They never carry source bytes, filesystem handles, package snapshots, SVG DOM ids, or label guesses.
- Cabinet composition/drawing may supply trace inputs later, but this story establishes the table validator and byte-stable payload contract first.
- Fail closed on selectable primitive without trace, trace without selectable primitive, duplicate occurrence id, duplicate primitive id ownership, synthetic subject, or mismatch.

### Previous Story Intelligence

- Story 3.1 added deterministic route topology and exact lane facts.
- Story 3.2 added deterministic `CabinetRouteFact` and proof in `drawing-composition`.
- `CabinetOccurrenceVisualJoin` already carries `InstallationOccurrenceKey`, physical occurrence id, representation occurrence id, transformed body, and anchors.
- `CabinetCompositionCompiler` currently emits paint-only primitives without trace; Story 3.3 must not turn labels/DOM/primitive ids into authority.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 3, Story 3.3.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-32, FR-34.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-13, AD-14 trace ownership.
- `_bmad-output/implementation-artifacts/m35/2-7-compose-one-paint-only-cabinet-document.md` - Cabinet primitive producer handoff.
- `_bmad-output/implementation-artifacts/m35/3-2-compile-deterministic-physical-routes-and-proof.md` - route fact handoff.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T05:29:37+08:00 - Created Story 3.3 from M35 Epic 3 backlog using PRD, architecture spine, epics, and Story 3.2 routing evidence.
- 2026-07-28T05:32:00+08:00 - Started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- 2026-07-28T05:35:00+08:00 - RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test --tests com.engineeringood.athena.presentation.GraphicOccurrenceTraceTableTest` failed with missing trace table contract symbols.
- 2026-07-28T05:42:00+08:00 - GREEN evidence: targeted `GraphicOccurrenceTraceTableTest` passed with `BUILD SUCCESSFUL`.
- 2026-07-28T05:43:00+08:00 - Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test` passed with `BUILD SUCCESSFUL`.
- 2026-07-28T05:45:00+08:00 - Audit evidence: trace forbidden-term audit returned only negative assertions and an existing SVG shape-model comment; encoding audit passed; `git diff --check` reported no whitespace errors beyond existing CRLF warnings.

### Completion Notes List

- Added normalized `GraphicOccurrenceTraceTable` models and compiler in `kernel:presentation-model`.
- Trace is a side table: selectable primitive refs map `GraphicPrimitiveId` to `GraphicOccurrenceId`; decorative primitive ids are explicit; primitive payloads are not mutated.
- Trace entries carry typed subject ids, source spans, digests, owning declarations, binding rule, representation definition, and resource snapshot digest without source bytes, filesystem handles, DOM ids, SVG node ids, or labels as authority.
- AC evidence: AC1 covered by selectable/decorative table proof; AC2 by complete source-chain assertions and transport exclusions; AC3 by duplicate/missing/synthetic/mismatch diagnostics; AC4 by deterministic transport serialization; AC5 by audit and sequential verification.

### File List

- `_bmad-output/implementation-artifacts/m35/3-3-trace-every-graphic-occurrence-to-governed-source.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/GraphicOccurrenceTraceTable.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/GraphicOccurrenceTraceTableTest.kt`

### Change Log

- 2026-07-28 - Created Story 3.3 implementation guide.
- 2026-07-28 - Moved Story 3.3 to in-progress.
- 2026-07-28 - Implemented normalized graphic occurrence trace table, validation, stable transport, and verification evidence.
