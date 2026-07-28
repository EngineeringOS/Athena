---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 2.7: Compose One Paint-Only Cabinet Document

Status: review

## Story

As an engineer,
I want the Cabinet view composed from validated physical and representation facts,
so that the renderer paints one bounded physical projection instead of arranging a toy graph.

## Acceptance Criteria

1. Given valid Physical IR and joined representation occurrences, when `CabinetCompositionCompiler` runs, then it emits deterministic Graphic Primitives for enclosure, mounting surfaces, rails, ducts, terminal groups, mounted bodies, labels, and frame, and Cabinet does not consume schematic LayoutIntent/placement, legacy GeometryDocument, PresentationPrimitive, or direct SVG/box producers.
2. Given composed Cabinet facts, when document bounds and viewport are calculated, then bounds derive from required physical/composed content plus governed padding and labels, and no renderer hardcoded width, shallow canvas, semantic source-order placement, clipping, or off-canvas required content remains.
3. Given the presentation payload reaches Theia, when the Cabinet surface paints, then the frontend performs no physical inference, representation selection, filesystem access, or engineering classification.
4. Given the retained M34 Control Drawing path, when Cabinet migration tests run, then Control Drawing regression remains green but gains no physical-Cabinet authority, and old Cabinet-specific producers are deleted after zero-caller proof.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and duplicate Cabinet paths, stale render helpers, generated output, XML logic, and compatibility names are purged.

## Tasks / Subtasks

- [x] Add RED tests for Cabinet composition (AC: 1, 2, 3)
  - [x] Cover deterministic primitives for enclosure, surface, rail, duct, terminal group, mounted body, label, and frame.
  - [x] Cover derived document bounds/viewport from physical/composed content plus padding.
  - [x] Assert compiler input has no schematic layout, renderer, filesystem, SVG, or frontend inference path.
- [x] Implement Cabinet composition models/compiler in `kernel:drawing-composition` (AC: 1, 2, 3)
  - [x] Consume `PhysicalInstallationIRV0` and `CabinetOccurrenceVisualJoin`.
  - [x] Emit paint-only Graphic Primitive-style Cabinet payload with deterministic ids/order.
  - [x] Derive bounds from content and policy padding.
- [x] Preserve M34 regression boundary (AC: 4)
  - [x] Run existing drawing-composition regression tests.
  - [x] Do not add Cabinet authority to M34 Control Drawing path.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched paths for duplicate Cabinet producers, schematic layout authority, renderer inference, SVG/XML/direct box producers, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 2.7 composes a renderer-neutral Cabinet document payload. It does not build Theia UI, route conductor segments, selection trace, source editing, package resolution, or product E2E screenshots.

### Architecture Requirements

- `kernel:drawing-composition` owns Cabinet composition.
- Renderer is paint-only and receives deterministic primitives/payload only.
- Inputs are `PhysicalInstallationIRV0`, joined representation occurrences, and a composition policy.
- No schematic `LayoutIntent`, legacy `GeometryDocument`, `PresentationPrimitive`, direct SVG/box producer, source-order placement, renderer hardcoded viewport, filesystem lookup, or frontend classification.
- Bounds derive from content and governed padding.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.7.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-19, FR-21..FR-22.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-11, AD-16.
- `_bmad-output/implementation-artifacts/m35/2-6-join-mounted-occurrences-to-one-visual-transform.md` - previous story join/transform evidence.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T04:22:00+08:00 - Created Story 2.7 from M35 Epic 2 backlog and started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test --tests com.engineeringood.athena.drawing.composition.CabinetCompositionCompilerTest` failed at test compile with unresolved `CabinetCompositionCompiler`, `CabinetCompositionRequest`, and `CabinetCompositionPolicy`.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test --tests com.engineeringood.athena.drawing.composition.CabinetCompositionCompilerTest` passed.
- Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:drawing-composition:test` passed.
- Audit evidence: `rg -n "LayoutIntent|GeometryDocument|PresentationPrimitive|direct SVG|SVG|svg|XML|xml|filesystem|File\\(|Paths\\.|renderer hardcoded|source-order|semantic source-order|frontend|infer|fallback" kernel/drawing-composition/src/main/kotlin kernel/drawing-composition/src/test/kotlin kernel/drawing-composition/build.gradle.kts` returned only an existing drawing-sheet test name containing "without inference".
- Audit evidence: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Audit evidence: `git diff --check` passed with existing CRLF normalization warnings only.

### Completion Notes List
- Added `CabinetCompositionCompiler` with request, policy, proof, diagnostic, and success/failure result models.
- The compiler consumes `PhysicalInstallationIRV0` and `CabinetOccurrenceVisualJoin`, validates join-to-physical presence, and emits deterministic `GraphicPrimitiveDocument` output.
- Emitted paint-only primitives for frame, enclosure, mounting surface, rail, duct, channel, terminal group, mounted body, and mounted label.
- Document bounds derive from content union plus governed padding; provenance sources identify physical IR, joined representation, and composition compiler.
- No schematic layout, GeometryDocument, PresentationPrimitive, renderer, filesystem, SVG, source-order placement, or frontend inference path was added.
- AC mapping: AC1 covered by primitive id/kind/order test; AC2 covered by document bounds/padding test; AC3 covered by input model boundary and audit; AC4 covered by full drawing-composition regression; AC5 covered by RED/GREEN, module regression, encoding, whitespace, and leakage audit.
- Three-layer review: physical topology supplies Cabinet facts; representation join supplies mounted visual bounds; renderer receives only Graphic Primitive document payload.

### File List

- `_bmad-output/implementation-artifacts/m35/2-7-compose-one-paint-only-cabinet-document.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetCompositionCompiler.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/CabinetCompositionCompilerTest.kt`

### Change Log

- 2026-07-28 - Created Story 2.7 implementation guide and moved story to in-progress.
- 2026-07-28 - Completed Story 2.7 paint-only Cabinet composition compiler, tests, and verification evidence.
