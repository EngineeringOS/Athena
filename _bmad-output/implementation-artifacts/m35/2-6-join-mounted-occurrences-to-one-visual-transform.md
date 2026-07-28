---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 2.6: Join Mounted Occurrences To One Visual Transform

Status: review

## Story

As an engineer,
I want each valid physical occurrence joined to exactly one selected representation,
so that physical truth and visual policy meet without either becoming the other's authority.

## Acceptance Criteria

1. Given physical lowering and Cabinet representation binding for one semantic subject, when composition joins inputs, then both sides use the same `InstallationOccurrenceKey` and exactly one physical plus one representation occurrence exists, and missing/duplicate sides fail before Graphic Primitive output.
2. Given intrinsic representation bounds and a physical footprint, when `CabinetVisualTransform` is built, then the exact seven-stage normalization/scale/centre/rotate/place/target/enclosure-to-drawing order is used once, and body, anchors, labels, hotspots, bounds, routes, and trace share the same transform id.
3. Given all four occurrence rotations and horizontal/vertical Rail targets, when golden body/anchor tests run, then expected coordinates match exactly and no target frame mirrors geometry.
4. Given visual geometry or physical constraints, when ownership is audited, then geometry never supplies footprint/mounting/clearance and physical rules never select an Element.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and duplicate transform math, fallback joins, stale fixtures, and compatibility paths are purged.

## Tasks / Subtasks

- [x] Add RED tests for Cabinet occurrence join and visual transform (AC: 1..4)
  - [x] Cover one-to-one join by `InstallationOccurrenceKey`.
  - [x] Cover missing physical side, missing representation side, duplicate physical side, and duplicate representation side.
  - [x] Cover golden transformed body/anchor coordinates for deg0/90/180/270 and horizontal/vertical rail frames.
- [x] Implement drawing-composition join models and compiler (AC: 1, 4)
  - [x] Reuse `InstallationOccurrenceKey` from `kernel:physical-model`.
  - [x] Representation inputs provide intrinsic bounds and anchors only; physical inputs provide footprint, placement, target frame, and orientation.
  - [x] Do not perform representation selection or physical constraint validation here.
- [x] Implement `CabinetVisualTransform` (AC: 2, 3)
  - [x] Use the seven-stage transform once.
  - [x] Emit one transform id shared by transformed body and anchors, ready for later labels/hotspots/routes/trace.
  - [x] Preserve determinant `+1` target frames.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched paths for duplicate transform math, fallback joins, geometry-derived physical facts, physical-selected Elements, XML, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 2.6 joins validated physical occurrence facts to already-selected representation occurrence facts. It does not select Elements, validate physical fit, compile symbols, parse SVG, route conductors, compose the full Cabinet document, render UI, or create trace transport.

### Architecture Requirements

- `kernel:drawing-composition` owns Cabinet join and visual transform.
- `kernel:physical-model` owns `InstallationOccurrenceKey`, physical footprints, mount targets, and frames.
- `kernel:representation-model` owns intrinsic representation material.
- Join requires exactly one physical occurrence and exactly one representation occurrence per key.
- Transform order: normalize intrinsic min to origin; uniform scale into unrotated footprint; center; rotate clockwise around footprint center; translate rotated AABB minimum to target-local placement; apply target-to-enclosure frame; apply enclosure-to-drawing frame.
- Target frames must not mirror geometry.
- No fallback matching by label, semantic id prefix, DOM id, SVG id, source order, or list index.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.6.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-5, AD-11.
- `_bmad-output/implementation-artifacts/m35/2-5-evaluate-physical-fit-collision-and-clearance.md` - previous story validation evidence.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T04:11:42+08:00 - Created Story 2.6 from M35 Epic 2 backlog and started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test --tests com.engineeringood.athena.drawing.composition.CabinetVisualTransformCompilerTest` failed at test compile with unresolved `CabinetVisualTransformCompiler`, `CabinetVisualTransformCompilation`, and `CabinetTransformId`.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test --tests com.engineeringood.athena.drawing.composition.CabinetVisualTransformCompilerTest` passed.
- Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test` passed.
- Audit evidence: `rg -n "fallback|label.*match|prefix|DOM|dom|SVG|svg|XML|xml|select.*Element|footprint.*intrinsic|mounting.*intrinsic|clearance.*intrinsic" kernel/drawing-composition/src/main/kotlin kernel/drawing-composition/src/test/kotlin kernel/drawing-composition/build.gradle.kts` returned only expected intrinsic-bounds scaling in transform math; no fallback/DOM/SVG/XML/selection/physical-truth leakage.
- Audit evidence: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Audit evidence: `git diff --check` passed with existing CRLF normalization warnings only.

### Completion Notes List
- Added Cabinet visual join/transform models in `kernel:drawing-composition`, including transform id, physical occurrence input, representation occurrence input, transformed body/anchors, diagnostics, and success/failure result.
- Added `CabinetVisualTransformCompiler` with exact one-physical/one-representation join by `InstallationOccurrenceKey`.
- Implemented seven-stage transform over intrinsic bounds, physical footprint, occurrence rotation, target-local placement, target frame, and enclosure-to-drawing frame.
- Added golden tests for deg0/90/180/270 body bounds, shared transform id, missing/duplicate join diagnostics, and vertical rail determinant `+1` anchor behavior.
- Added `kernel:drawing-composition -> kernel:physical-model` dependency for shared `InstallationOccurrenceKey`; no representation selection or physical validation was added.
- AC mapping: AC1 covered by join and failure tests; AC2 covered by shared transform id and seven-stage transform implementation; AC3 covered by rotation and vertical frame golden tests; AC4 covered by typed input boundary and audit; AC5 covered by RED/GREEN, module regression, encoding, whitespace, and leakage audit.
- Three-layer review: physical layer supplies placement/footprint/frame; representation layer supplies intrinsic geometry/anchors only; drawing-composition joins by typed key without choosing either side.

### File List

- `_bmad-output/implementation-artifacts/m35/2-6-join-mounted-occurrences-to-one-visual-transform.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/drawing-composition/build.gradle.kts`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetVisualTransformCompiler.kt`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetVisualTransformModels.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/CabinetVisualTransformCompilerTest.kt`

### Change Log

- 2026-07-28 - Created Story 2.6 implementation guide and moved story to in-progress.
- 2026-07-28 - Completed Story 2.6 Cabinet occurrence join, visual transform compiler, tests, and verification evidence.
