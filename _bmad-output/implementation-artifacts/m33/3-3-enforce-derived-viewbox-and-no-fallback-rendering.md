---
story_id: 3.3
story_key: 3-3-enforce-derived-viewbox-and-no-fallback-rendering
epic: 3
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 3.3: Enforce Derived ViewBox And No-Fallback Rendering

## Status

Done

## Story

As a reviewer, I want renderer proof to fail on toy regressions.

## Acceptance Criteria

- `viewBox` derives from resolved primitive/sheet bounds plus governed margins.
- Proof rejects duplicate labels, off-screen duplicate elements, center fallback routes, and
  generic fallback boxes in the demo.
- Failures name the owning authority and affected subject.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED root SVG composer tests proving viewBox derives exactly from IR bounds and governed
  margin for non-zero/negative origins and multiple dimensions. (AC: 1)
- [x] Add RED fail-closed tests for invalid margin/bounds and explicit duplicate-label,
  off-screen-occurrence, center-fallback-route, and generic-fallback facts. (AC: 2,3)
- [x] Implement generic root SVG composition in `kernel/svg-renderer` over Story 3.2 fragments;
  never inspect DOM/text/classes to recover safety facts. (AC: 1..3)
- [x] Emit deterministic proof containing content bounds, bounds authority, margin policy, derived
  viewBox, rejected fact categories, and affected subjects. (AC: 1..3)
- [x] Extend the production M33 LSP catalog path to publish derived root SVG documents and proof for
  all ten symbols without a constant canvas size. (AC: 1,3)
- [x] Add source/DOM scans that fail on M33 hard-coded sample viewBox, generic component class,
  duplicate/off-screen fragments, or accepted center fallback. (AC: 1..3)
- [x] Audit old `SvgRenderer`, Workbench canvas viewBox calculation, generic node fallback, and route
  center fallback; remove only migrated paths and ledger remaining live integration debt. (AC: 4)
- [x] Run focused, affected modules, repository, and encoding verification sequentially. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 AD-2, AD-3, AD-7, AD-8, and AD-10.
- Root composer consumes a validated Graphic Primitive document/Story 3.2 fragment and explicit
  safety facts from owning upstream authorities. It does not parse generated SVG.
- Governed margin is a finite non-negative scalar supplied by policy. ViewBox is exactly
  `(bounds.x-margin, bounds.y-margin, bounds.width+2*margin, bounds.height+2*margin)`.
- Safety facts are explicit subject lists: duplicate label occurrence, off-screen occurrence,
  center-fallback route, and generic-fallback occurrence. Any non-empty category blocks output.
- Diagnostics identify authority (`presentation`, `spatial-routing`, or `representation-binding`)
  and exact subject. Renderer does not infer those facts from coordinates, labels, classes, or ids.
- Do not add sheet frame/title block composition; Epic 4 expands bounds ownership after this
  primitive/document proof.
- Existing old `SvgRenderer` hard-codes its own legacy viewBox from geometry canvas dimensions.
  Do not delete while compiler callers remain; ledger migration after new live graph consumption.

## Architecture Compliance

- Bounds and margin are upstream facts; SVG only serializes them.
- No fixed `1680x1188`, sample-specific dimensions, file count, viewport size, or DOM bounding box.
- Fallback rejection is structured evidence, not a CSS/DOM string heuristic in production code.
- Story 3.2 generic fragment adapter remains the only Graphic Primitive IR-to-element painter.

## File Structure Requirements

- Root composer/proof contracts belong in `kernel/svg-renderer` beside the fragment adapter.
- LSP mapping extends the cohesive M33 catalog service; no package logic enters the renderer.
- Tests stay in matching svg-renderer and LSP source sets; no external dependencies.

## Testing

- TDD mandatory with observed RED for composer API and each rejection category.
- Assert exact locale-independent viewBox values for positive, negative, and fractional bounds.
- Assert invalid/non-finite/negative margin returns diagnostics and no SVG.
- Assert each safety fact category returns deterministic authority/subject diagnostics and no SVG.
- Assert all ten LSP catalog symbols publish root `<svg>` documents with viewBoxes derived from
  each compiled document bounds, and repeated runs are equal.
- Run `:kernel:svg-renderer:test`, `:ide:lsp:test`, full `gradlew test`, and encoding audit.

## Evidence Plan

- AC1: exact composer viewBox/bounds/margin tests and ten-symbol LSP proof.
- AC2: four explicit safety-category negative tests and no-partial-output assertions.
- AC3: deterministic diagnostic authority/subject assertions.
- AC4: regression logs, source audit, cleanup ledger, and adversarial review.

## Polish And Purge

Remove or ledger hard-coded viewBox and fallback-box paths. Do not claim the old live graph path is
migrated until Workbench consumes the new LSP payload; carry that exact debt to Story 6.2/7.2.

## Previous Story Intelligence

- Story 3.2 emits validated SVG fragments only, deliberately without root `<svg>` or viewBox.
- Fragment adapter covers all primitive kinds, segments full-circle arcs, limits sweep workload,
  rejects invalid XML, resolves paints explicitly, and emits no normal chrome.
- `athena/drawingSvgCatalog` is a production LSP request that compiles and renders ten governed
  symbols. Story 3.3 should extend this payload rather than create another catalog path.
- Old `PresentationSvgPath`, frontend `svg_path`, and box `SvgRenderer` remain active compatibility
  paths and are ledgered; deletion requires live graph migration proof.

## Git Intelligence

- Baseline remains M32 `0311ad6`; M33 remains uncommitted as one coherent milestone.
- Reuse existing `GraphicPrimitiveSvgAdapter` and `M33IecDrawingSvgCatalogService` contracts.
- No new dependency is expected. Never add `.tools` or modify reference sources.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: LSP test compilation failed because root SVG, bounds, viewBox, margin, and authority proof
  fields did not exist.
- GREEN: the production ten-symbol catalog composes Story 3.2 fragments through the generic root
  composer with an explicit eight-unit governed margin.
- RED/GREEN: finite extreme bounds and margin initially overflowed viewBox derivation and crashed
  numeric serialization; the composer now rejects non-finite derived viewBoxes with no SVG/proof.
- REVIEW: adversarial review found nullable transport authority and overflow exposure. Both were
  corrected; no unresolved Story 3.3 finding remains.
- MODULE: `:kernel:svg-renderer:test` and `:ide:lsp:test` passed sequentially.
- FRONTEND: `yarn test` passed all 203 tests, including the M33 source-authority guard.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (12 executed, 135 up-to-date).

### Completion Notes List

- Added a generic root SVG composer whose viewBox derives only from validated Graphic Primitive IR
  bounds and an explicit governed margin; positive, negative, fractional, and overflow cases are
  covered.
- Added explicit fail-closed safety facts for duplicate labels, off-screen occurrences,
  center-fallback routes, and generic fallback occurrences. Diagnostics carry non-null authority
  and exact subject, and failures return no SVG or proof.
- Extended `athena/drawingSvgCatalog` so all ten governed symbols publish deterministic root SVG,
  content bounds, viewBox, margin, margin policy, and bounds authority.
- AC-to-evidence: AC1 is covered by exact composer formulas, fractional/negative-origin tests, and
  ten-symbol LSP proof; AC2 by four safety-category and no-partial-output assertions; AC3 by exact
  authority/subject assertions and non-null LSP authority; AC4 by source guard, cleanup ledger,
  sequential regressions, encoding audit, and adversarial review.
- Polish/purge: the new path contains no hard-coded sample canvas, DOM/text/class inference,
  generic wrapper, or center route fallback. The active legacy box `SvgRenderer`,
  `PresentationSvgPath`, and frontend `svg_path` callers remain ledgered for Stories 6.2/7.2; they
  were not falsely removed or claimed migrated.
- Final acceptance review: PASS for AC1-AC4.

### File List

- `_bmad-output/implementation-artifacts/m33/3-3-enforce-derived-viewbox-and-no-fallback-rendering.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/M33IecDrawingSvgCatalogService.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M33IecDrawingSvgCatalogServiceTest.kt`
- `ide/theia-frontend/scripts/athena-m33-svg-authority-regression.test.mjs`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/GraphicPrimitiveSvgCanvasComposer.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/GraphicPrimitiveSvgCanvasComposerTest.kt`

## Change Log

- 2026-07-23: Expanded BMAD story context and fixed viewBox formula, safety-fact authority,
  production payload, and legacy-path cleanup boundaries.
- 2026-07-23: Implemented IR-derived root SVG composition, explicit no-fallback safety facts, and
  ten-symbol LSP bounds proof.
- 2026-07-23: Fixed adversarial overflow/authority findings, completed polish/purge evidence, and
  closed the story.
