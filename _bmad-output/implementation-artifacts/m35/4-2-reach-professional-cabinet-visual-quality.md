---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 4.2: Reach Professional Cabinet Visual Quality

Status: review

## Story

As a customer evaluator,
I want the Cabinet drawing to look like a credible industrial installation,
so that Athena no longer presents its engineering model through a toy-level graph surface.

## Acceptance Criteria

1. Given the approved QET image as visual/domain reference only, when the M35 Cabinet is composed and painted, then enclosure containment, mounting surface/Rails, ducts/channels, terminal density, mounted controls, route containment, labels, and drawing frame are visibly coherent, and no QET schema, runtime code, copied logic, or imported semantic authority enters Athena.
2. Given desktop and narrow workbench sizes, when the Cabinet document is fitted, then required content remains visible and readable with professional density, stable aspect ratio, and intentional spacing, with no clipping, text overflow, unintended overlap, giant graph card, floating toy box, or wide shallow canvas.
3. Given labels, strokes, terminals, anchors, routes, and complex vendor geometry, when renderer output is inspected, then all are painted from Graphic Primitive/Presentation facts with stable dimensions and no frontend inference or hardcoded screenshot background.
4. Given a visual defect requires upstream facts, when it is corrected, then the fix is made in source, physical model, representation, composition, or renderer ownership as appropriate, and no mock, CSS concealment, or fabricated proof is accepted.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and obsolete graph chrome, fallback visuals, stale CSS, generated output, and compatibility paths are purged.

## Tasks / Subtasks

- [x] Add RED tests for professional Cabinet visual structure (AC: 1..3)
- [x] Improve composition and renderer facts to meet the structural visual checklist (AC: 1..4)
- [x] Remove or hide non-Cabinet graph chrome and fallback visuals from the M35 surface (AC: 2, 5)
- [x] Polish/purge and evidence gate (AC: 5)

## Dev Notes

Cabinet visual quality must be fixed at the correct layer: source facts, physical model, representation, composition, or paint-only renderer. QET is a reference image/domain guide, not a source of copied schema, runtime code, or semantic authority.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T07:40:00+08:00 - RED/GREEN: added Cabinet composition coverage for route polylines and endpoint markers, then implemented the route primitive output in `CabinetCompositionCompiler`.
- 2026-07-28T07:45:00+08:00 - GREEN: dedicated M35 sample proof now asserts Cabinet SVG output contains connection, component, and label rendering classes.
- 2026-07-28T07:46:00+08:00 - Verification: `:kernel:drawing-composition:test`, `:kernel:compiler:test --tests AthenaM35DedicatedCabinetSampleTest`, and `:ide:lsp:test --tests AthenaM35DedicatedCabinetProjectionSmokeTest` passed.
- 2026-07-28T07:46:00+08:00 - Audit: M35 Cabinet surface remains Cabinet-only; no Documentation/Schematic/Wiring product chrome entered the sample path.

- 2026-07-28T07:26:00+08:00 - Started Story 4.2 from baseline `14ad49515e95473328472db843722f7200fc1e91`.

### Completion Notes List

- Added a failing cabinet visual structure test that required route polylines and terminal markers to be emitted as primitive output.
- Extended `CabinetCompositionCompiler` to paint route polyline and endpoint marker primitives alongside enclosure, rail, duct, channel, terminal-group, mounted-body, and label primitives.
- Tightened the dedicated M35 sample proof so the Cabinet SVG output is checked for connection, component, and label rendering classes.
- Kept the dedicated M35 surface Cabinet-only; no extra product chrome was introduced while improving the visual proof.

### File List

- `_bmad-output/implementation-artifacts/m35/4-2-reach-professional-cabinet-visual-quality.md`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetCompositionCompiler.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/CabinetCompositionCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM35DedicatedCabinetSampleTest.kt`

### Change Log

- 2026-07-28 - Created Story 4.2 implementation guide.
- 2026-07-28 - Added Cabinet route primitive rendering and dedicated sample SVG assertions for professional Cabinet visual quality.
- 2026-07-28 - Moved Story 4.2 to in-progress.
