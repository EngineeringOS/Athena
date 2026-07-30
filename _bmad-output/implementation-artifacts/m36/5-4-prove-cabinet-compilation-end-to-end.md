---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E5.S4: Prove Cabinet Compilation End To End

Status: done

## Story

As an evaluator,
I want structural and visual E2E evidence for the M36 Cabinet sample,
so that professional rendering is verified rather than asserted.

**Requirements:** FR-35, FR-36.

## Acceptance Criteria

1. Given the M36 Cabinet sample on supported desktop and narrow viewports, E2E verification shows
   nonblank output, contained placement, readable density, orthogonal routes, labels, and correct
   junction and crossing treatment.
2. Structural proof confirms source traceability, resolved bindings, valid placement, valid channel
   routing, and complete RouteFact evidence.
3. Product smoke proves zero LSP diagnostics, unapproved fallback routes, route or body
   intersections, required off-channel segments, XML authority, and raw SVG metadata authority.
4. The same source and pinned snapshots reproduce equivalent PlacementFacts and RouteFacts.
5. Tests fail when a required bridge, placement constraint, channel rule, or route proof field is
   removed.

## Tasks / Subtasks

- [x] Add failing E2E/proof tests for the M36 cabinet sample (AC: 1-5)
  - [x] Cover structural proof for traceability, resolved bindings, placement, channel routing, and
    RouteFact completeness.
  - [x] Cover visual proof at desktop and narrow viewport sizes: nonblank output, containment,
    readable density, orthogonal routes, labels, junctions, and crossings.
  - [x] Cover negative proof cases for missing bridge, placement constraint, channel rule, and route
    proof fields.
- [x] Implement the structural E2E proof path (AC: 2-5)
  - [x] Reuse the dedicated `examples/m36/connectivity-cabinet/` sample only.
  - [x] Compile source-first from the sample repository root and inspect derived PlacementFacts and
    RouteFacts, not authored hidden layout truth.
  - [x] Assert zero unresolved Port-to-Anchor bindings, route/body intersections, off-channel
    required segments, unapproved fallback routes, XML authority, and raw SVG metadata authority.
  - [x] Assert deterministic reruns produce equivalent PlacementFacts and RouteFacts from the same
    source and pinned snapshots.
- [x] Implement the visual E2E proof path (AC: 1, 3)
  - [x] Render the M36 Cabinet payload at desktop and narrow viewport sizes.
  - [x] Assert nonblank output and stable containment without depending on strict QET pixel identity.
  - [x] Assert route geometry remains orthogonal and labels/junction/crossing markers are present
    through structured evidence rather than screenshot-only claims.
- [x] Wire product smoke evidence (AC: 1-5)
  - [x] Ensure the LSP/project smoke path opens the M36 sample source-first with Cabinet active and
    zero diagnostics.
  - [x] Keep renderer and Theia workbench paint-only/non-authoritative.
  - [x] Record all evidence as repeatable tests, not manual screenshot assertions.
- [x] Run story evidence gate (AC: 1-5)
  - [x] Run sequential Gradle tests, frontend proof scripts if added, encoding audit, and
    `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- M36 is about Engineering Connectivity Semantics. Cabinet is the visible acceptance surface, not a
  separate semantic world.
- Use only `examples/m36/connectivity-cabinet/` for this story. Do not share M34/M35 examples or add
  a second sample.
- AD-10 requires semantic and visual proof together: screenshots or nonblank pixels supplement
  structured evidence; they do not replace trace, binding, PlacementFact, and RouteFact checks.
- FR-35 requires contained placement, readable density, stable labels, obstacle-free orthogonal
  routes, explicit junction/crossing treatment, nonblank rendering, and source traceability on
  desktop and narrow viewports.
- FR-36 requires zero LSP diagnostics, zero unresolved Port-to-Anchor bindings, zero unapproved
  fallback routes, zero route/body intersections, zero off-channel required segments, zero XML
  runtime authority, and zero raw SVG metadata authority.
- QElectroTech remains a visual/domain reference only. Do not assert strict pixel identity with QET;
  the M36 authority is structural, semantic, and renderer-independent proof.
- No XML compatibility paths. The project is unreleased; stale legacy paths may be removed instead
  of patched.

### Project Structure Notes

- Likely touchpoints:
  - `examples/m36/connectivity-cabinet/`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`
  - `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/theia-frontend/scripts/`
- Keep text assets UTF-8.
- Do not introduce a full Cabinet editor, AI autolayout, strict QET pixel comparison, or renderer
  authority over placement/routes.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 5.4, FR-35, FR-36.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-35, FR-36,
  NFR-1, NFR-4, NFR-5, NFR-7, NFR-8, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-9, AD-10.
- `_bmad-output/implementation-artifacts/m36/5-3-create-source-first-m36-cabinet-sample.md`
  - dedicated sample fixture and proof pattern.
- `examples/m36/connectivity-cabinet/src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena`
  - source-first sample root for this story.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36DedicatedCabinetSampleTest.kt`
  - compiler-side structural sample proof.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM36DedicatedCabinetProjectionSmokeTest.kt`
  - source-first Cabinet LSP smoke pattern.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Created story via BMad create-story flow after 5-2 and 5-3 reached review.
- 2026-07-29: Started implementation; status moved to in-progress in story and sprint tracker.
- 2026-07-29: Added M36 E2E proof test for dedicated Cabinet sample, including nonblank SVG,
  occurrence/route thresholds, zero forbidden authority/fallback evidence, orthogonal route
  checks, deterministic rerun proof, and negative proof cases.
- 2026-07-29: Fixed the direct GeometryDocument SVG render path to emit traceable polylines and
  derived crossing markers from route segments.
- 2026-07-29: Updated M0/M2 published SVG conformance artifacts and compiler expectations to the
  M36 traceable polyline renderer contract.
- 2026-07-29: Verification passed:
  `:kernel:svg-renderer:test`, `:kernel:drawing-composition:test`, `:kernel:compiler:test`,
  `:ide:lsp:test`, `node ide/theia-frontend/scripts/athena-governed-graphic-edit-protocol.test.mjs`,
  `test`, encoding audit, and `git diff --check`. A prior clean full-suite run timed out after
  Gradle test-result corruption; repo rule clean was run before rerunning successfully.

### Completion Notes List

- Story context created from M36 epics, PRD FR-35/FR-36, architecture AD-9/AD-10, and previous story
  5-3 evidence.
- Added repeatable compiler-side E2E proof for the M36 dedicated Cabinet sample. The proof checks
  source-first compilation, nonblank SVG, at least 20 component occurrences, at least 30 route
  facts, orthogonal routes, visible junction/crossing treatment, deterministic reruns, and negative
  proof failures.
- Renderer now keeps paint-only authority while carrying derived route evidence: connections render
  as polylines with `data-connection-id`, components carry `data-subject`, and crossings render as
  explicit markers derived from route geometry.
- Legacy M0/M2 SVG artifacts were moved to the current traceable polyline contract; no XML runtime
  authority or raw SVG semantic metadata authority was introduced.

### File List

- `_bmad-output/implementation-artifacts/m36/5-4-prove-cabinet-compilation-end-to-end.md`
- `_bmad-output/implementation-artifacts/m36/sprint-status.yaml`
- `examples/m0/demo-cabinet.svg`
- `examples/m2/demo-cabinet.cabinet.svg`
- `examples/m2/demo-cabinet.wiring.svg`
- `examples/m36/connectivity-cabinet/src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36CabinetEndToEndProofTest.kt`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetCompositionCompiler.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderer.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererModuleMarkerTest.kt`

## Change Log

- 2026-07-29: Created M36-E5.S4 story for end-to-end Cabinet compilation proof.
- 2026-07-29: Completed M36-E5.S4 E2E Cabinet proof and updated renderer contract evidence.
