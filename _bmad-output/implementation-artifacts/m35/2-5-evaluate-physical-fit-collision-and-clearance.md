---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 2.5: Evaluate Physical Fit Collision And Clearance

Status: review

## Story

As an engineer,
I want invalid Cabinet placement rejected deterministically,
so that the drawing represents physically coherent installation intent rather than attractive guesses.

## Acceptance Criteria

1. Given surfaces, ducts, terminal groups, rails, channels, and occurrences, when Physical Constraint Evaluation v0 runs, then every parent object fits its typed parent, every Rail interval fits its surface, every channel rectangle fits its duct interior, and every occurrence fits its enclosure and target-specific rule.
2. Given a mounted occurrence, when orientation, depth, mounting, and compatibility are checked, then selected orientation belongs to the allowed set, depth fits authored enclosure depth, mounting type matches the target, and enclosure kind is compatible.
3. Given two occurrence footprints and four-sided clearances, when collision/clearance checks run, then positive-area footprint intersections fail, edge contact alone is allowed, either inflated rectangle intersecting the other footprint fails, and required inflated bounds fit their target/enclosure.
4. Given Rail placement, when fit is checked, then target-local normal coordinate is zero, along-axis footprint plus leading/trailing clearance fits `[0, length]`, and normal extent is checked against enclosure bounds.
5. Given invalid physical input, when evaluation completes, then stable source-spanned diagnostics include subject, measured value, and expected constraint, and no optimization, automatic placement, general solver, or AI layout executes.
6. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and renderer-side fit checks, duplicated evaluators, stale tests, and compatibility code are purged.

## Tasks / Subtasks

- [x] Add RED tests for Physical Constraint Evaluation v0 (AC: 1..5)
  - [x] Cover valid fit with surface, rail, duct/channel, terminal group, occurrences, and edge-contact allowed.
  - [x] Cover parent containment failures, channel outside duct interior, rail interval outside surface, orientation/depth/mounting/container mismatch, positive-area collision, clearance collision, inflated bounds outside target/enclosure, and rail normal offset.
  - [x] Assert diagnostics include stable code, subject, measured, expected, and source span where available.
- [x] Implement evaluator models/results in `kernel:physical-model` (AC: 1..5)
  - [x] Add physical rectangles and evaluation proof/result types.
  - [x] Keep evaluation pure and deterministic; no placement search, optimization, solver, renderer, or AI logic.
- [x] Implement `PhysicalConstraintEvaluatorV0` (AC: 1..5)
  - [x] Validate typed containment for infrastructure and mounted occurrences.
  - [x] Validate occurrence orientation, depth, mounting type, compatible container kind, collision, clearance, and rail-specific rules.
  - [x] Emit stable diagnostics and proof counts.
- [x] Polish/purge and evidence gate (AC: 6)
  - [x] Audit touched paths for renderer-side validation, duplicate evaluators, solver/autolayout leakage, XML, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 2.5 validates already-compiled PhysicalInstallationIR. It does not change source syntax, build visual transforms, route wires, compose graphics, render UI, perform automatic placement, run a constraint solver, or introduce AI layout.

### Architecture Requirements

- `kernel:physical-model` owns pure constraint evaluation.
- Evaluation consumes `PhysicalInstallationIRV0` and validated contracts only.
- Enclosure usable depth equals authored depth in M35 v0.
- Clearance-inflated occurrence footprint must fit enclosure and bounded surface/terminal targets.
- Rail mounts require target-local normal coordinate exactly zero.
- Rail along-axis span plus leading/trailing clearance must fit rail length.
- Rail normal extent is checked against enclosure bounds, not against the zero-width rail line.
- Edge contact is allowed; positive-area footprint intersection fails.
- Clearance fails if either inflated occurrence rectangle intersects the other footprint.
- Diagnostics must be stable and include subject, span where available, measured value, and expected constraint.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.5.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-23..FR-27.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - Physical Constraint Evaluation v0.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-2..AD-6, conventions.
- `_bmad-output/implementation-artifacts/m35/2-4-compile-typed-physical-cabinet-topology.md` - previous story topology evidence.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T04:01:52+08:00 - Created Story 2.5 from M35 Epic 2 backlog and started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalConstraintEvaluatorV0Test` failed at test compile with unresolved `PhysicalConstraintEvaluatorV0`, `PhysicalConstraintEvaluationV0`, and `PhysicalConstraintEvaluationMode`.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalConstraintEvaluatorV0Test` passed after evaluator implementation and fixture correction.
- Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test` passed.
- Audit evidence: `rg -n "XML|xml|SVG|svg|renderer|pixel|DOM|Presentation|GraphicPrimitive|solver|optimization|AI layout|Auto Layout|manufacturer|article|product catalog|package kind|manifest" kernel/physical-model/src/main/kotlin kernel/physical-model/src/test/kotlin kernel/physical-model/README.md` returned only explicit negative README boundary text, resolver names, and older physical-trait comments saying renderer is not owner.
- Audit evidence: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Audit evidence: `git diff --check` passed with existing CRLF normalization warnings only.

### Completion Notes List
- Added `PhysicalConstraintEvaluatorV0` with validation-only proof/result types and stable diagnostics.
- Implemented enclosure containment for surfaces, ducts, terminal groups, rail interval fit, channel fit inside duct wall-inset interior, and mounted occurrence enclosure/target fit.
- Implemented occurrence orientation, depth, mounting compatibility, container compatibility, body collision, clearance collision, and rail normal/along-axis checks.
- Kept evaluator pure and deterministic; no placement search, optimization, solver, renderer, or AI layout behavior was added.
- Updated `kernel:physical-model` README with evaluator ownership and explicit boundaries.
- AC mapping: AC1 covered by valid/invalid infrastructure tests; AC2 covered by orientation/depth/mounting/container diagnostics; AC3 covered by edge-contact allowed, collision, and clearance tests; AC4 covered by rail normal and along-axis tests; AC5 covered by diagnostics/proof assertions; AC6 covered by RED/GREEN, module regression, encoding, whitespace, and leakage audit.
- Three-layer review: physical layer owns validation; topology layer supplies facts; renderer/composition/AI layers remain outside this story.

### File List

- `_bmad-output/implementation-artifacts/m35/2-5-evaluate-physical-fit-collision-and-clearance.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/physical-model/README.md`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorV0.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorV0Test.kt`

### Change Log

- 2026-07-28 - Created Story 2.5 implementation guide and moved story to in-progress.
- 2026-07-28 - Completed Story 2.5 physical constraint evaluator, tests, documentation, and verification evidence.
