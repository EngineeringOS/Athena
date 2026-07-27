---
story_id: 3.2
story_key: 3-2-implement-svg-adapter-for-graphic-primitive-ir
epic: 3
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 3.2: Implement SVG Adapter For Graphic Primitive IR

## Status

Done

## Story

As a Workbench user, I want Graphic Primitive IR rendered as SVG so current Graph View displays
professional symbols.

## Acceptance Criteria

- SVG adapter renders all Graphic Primitive IR v0 kinds with stroke, fill, text baseline/anchor,
  transform, marker, line cap/join, dash, and stable numeric fidelity.
- Normal output contains no visible hitbox/background wrapper borders.
- SVG DOM includes traceable proof attributes without making DOM ids semantic authority.
- Production SVG rendering consumes `GraphicPrimitiveDocument` output from
  `M33IecSymbolCompilationService`; the compiled package adapter must not remain test-only.
- Invalid IR, unresolved paint/style input, and unsupported output fail closed with structured
  diagnostics and no partial SVG.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED generic adapter tests covering every Graphic Primitive IR v0 kind, nested groups,
  translation/rotation/scale, text escaping, style fidelity, and deterministic proof. (AC: 1,3)
- [x] Add RED fail-closed tests for invalid IR, unresolved paint tokens, malformed numeric output,
  and partial-output leakage. (AC: 4)
- [x] Add `representation-model` dependency to `kernel/svg-renderer` and implement one cohesive
  `GraphicPrimitiveSvgAdapter` that emits SVG fragments, not a root canvas/viewBox. (AC: 1..4)
- [x] Emit traceable `data-athena-*` proof attributes while never using DOM ids, CSS classes, text,
  or element kind as semantic authority. (AC: 2,3)
- [x] Add production integration orchestration downstream of the generic renderer that compiles
  `M33IecSymbolCatalog` through `M33IecSymbolCompilationService` and renders all ten documents;
  do not add IEC/package dependencies to the generic adapter contract. (AC: 4)
- [x] Add an integration test proving exact ten-symbol catalog order, no generic fallback element,
  no wrapper/background border, and deterministic SVG/proof output. (AC: 2..4)
- [x] Audit direct `PresentationSvgPath`, JSX symbol fragments, and old `SvgRenderer` callers;
  remove only superseded paths and ledger active compatibility for Story 3.3/6.x. (AC: 2,5)
- [x] Run focused, module, repository, frontend-if-touched, and encoding verification strictly
  sequentially on Windows. (AC: 1..5)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review. (AC: 5)

## Dev Notes

- Bind to M33 AD-1, AD-2, AD-3, AD-7, AD-8, and AD-10.
- The generic adapter consumes a validated `GraphicPrimitiveDocument` plus explicit renderer paint
  resolution. It must not import package-runtime, IEC identities, semantic models, Theia, DOM APIs,
  or source syntax.
- Emit SVG fragments/groups only. Story 3.3 owns root `<svg>`, derived viewBox, governed margins,
  off-screen/duplicate proof, and no-fallback canvas enforcement.
- Arc path calculation and marker geometry are adapter behavior. Raw SVG path data must never flow
  back into Graphic Primitive IR or become package authority.
- Text content is display payload only. Escape XML deterministically and never inspect label text
  to infer engineering meaning.
- Normal fragments contain engineering linework only. Do not emit visible component background,
  hitbox, card, selection, hover, or drag rectangles.
- Production integration may live in `ide/lsp` or another existing downstream orchestration layer
  that can depend on both package-runtime and svg-renderer. Do not reverse dependency direction by
  teaching package-runtime about SVG.
- Add a package-runtime-to-SVG integration test proving the production call path starts from the
  governed M33 catalog compilation service and does not reconstruct symbols in the renderer.
- Rebuild Theia product bundles before IDE validation when adapter behavior changes.

## Architecture Compliance

- Graphic Primitive IR remains renderer-neutral; only this concrete adapter knows SVG element/path
  syntax.
- Package resolution, symbol selection, anchors, slots, and engineering meaning remain upstream.
- SVG proof attributes are traceability only and cannot be consumed as semantic identity.
- No root canvas size or viewBox constant is allowed in this story.

## File Structure Requirements

- Generic adapter models and behavior belong in `kernel/svg-renderer/src/main/kotlin/.../` and may
  share one cohesive file until responsibilities exceed repository split guidance.
- Adapter unit tests stay in the matching svg-renderer test source set.
- M33-specific orchestration belongs downstream, never in representation-model or package-model.
- No external dependency is expected; use Kotlin/JDK math and deterministic string construction.

## Testing

- TDD is mandatory; each primitive family and fail-closed path must be observed RED first.
- Assert exact fragment strings/proof for line, polyline, arc, circle, rectangle, text, marker,
  connection dot, reference arrow, group, and transform.
- Assert style mapping for stroke width, cap, join, dash, fill, text anchor, and baseline.
- Assert XML escaping and locale-independent numeric formatting.
- Assert invalid input returns diagnostics with no fragment or partial element list.
- Integration test for `M33IecSymbolCompilationService` output consumed by the production adapter.
- Run `:kernel:svg-renderer:test`, affected integration tests, full `gradlew test`, and encoding
  audit sequentially.

## Evidence Plan

- AC1: exact primitive-family and style/transform adapter tests.
- AC2: output scan asserts no visible wrapper/hitbox/background chrome.
- AC3: proof/data-attribute assertions plus no `id=` authority.
- AC4: invalid IR/paint tests and ten-symbol service-to-adapter integration.
- AC5: Dev Agent Record, regression evidence, adversarial review, and cleanup ledger.

## Polish And Purge

Audit CSS for visible normal hitboxes, cards, borders, and one-off SVG fragments. Audit old
`PresentationSvgPath`, frontend `svg_path` JSX, and generic-box renderer paths, but do not delete an
active compatibility path until its production caller is migrated and covered.

## Previous Story Intelligence

- Story 3.1 compiles exactly ten `M33IecSymbolCatalog` definitions to validated Graphic Primitive
  IR and deterministic proof through exact `athena-native:<resource-id>` handles.
- The compiler validates Graphic Primitive IR before flattening, so cyclic/deep scenes fail closed
  instead of overflowing proof traversal.
- Package identity/coordinates, catalog inventory, resource handles, and descriptor style refs all
  fail closed before adapter input.
- `PresentationSvgPath`, M30 `PresentationPrimitive`, and old `SvgRenderer` still have active
  callers. They are compatibility debt, not safe Story 3.1 deletions.
- Independent review explicitly requires Story 3.2 to establish a real catalog-service-to-SVG call
  path so the new service cannot remain test-only.

## Git Intelligence

- Baseline remains M32 commit `0311ad6`; M33 is one uncommitted milestone change.
- Existing `kernel/svg-renderer` depends on engineering/geometry models and renders an older simple
  geometry/box SVG path. Preserve its callers while adding the Graphic Primitive IR adapter.
- Use existing Kotlin tests and no third-party SVG library. Never add `.tools` or modify references.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: svg-renderer tests failed because `GraphicPrimitiveSvgAdapter` contracts did not exist.
- GREEN: all Graphic Primitive IR kinds, nested groups, three transforms, style fidelity, escaping,
  traceability, and fail-closed tests passed.
- RED/GREEN: full-circle arcs initially collapsed to one equal-endpoint SVG arc; rendering now
  deterministically segments sweeps up to 360 degrees.
- RED/GREEN: excessive sweeps and XML 1.0-invalid control characters initially rendered; both now
  fail closed with no fragment.
- RED/GREEN: LSP integration initially lacked `M33IecDrawingSvgCatalogService`; the production
  `athena/drawingSvgCatalog` request now compiles and renders the governed ten-symbol catalog.
- REVIEW: deep review covered arc workload, XML safety, palette resolution, proof suppression,
  root viewBox leakage, wrapper chrome, partial output, package dependency direction, and active
  compatibility paths. No blocking finding remains.
- MODULE: `:kernel:svg-renderer:test` and `:ide:lsp:test` passed sequentially.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (12 executed, 135 up-to-date).

### Completion Notes List

- Added a generic paint-only SVG fragment adapter for every Graphic Primitive IR v0 kind, including
  deterministic arc geometry, markers, arrows, groups, and translation/rotation/scale transforms.
- Adapter validates IR before traversal, resolves every paint explicitly, rejects invalid XML and
  excessive arc sweep, escapes all text/attributes, and returns no partial SVG on diagnostics.
- Output contains only a traceable `<g>` fragment and primitive elements: no root `<svg>`, viewBox,
  CSS class, DOM `id`, normal hitbox/background border, or generic component fallback.
- Added `M33IecDrawingSvgCatalogService` downstream in LSP; it consumes Story 3.1 catalog compiler
  documents and publishes deterministic production payloads via `athena/drawingSvgCatalog`.
- AC-to-evidence: AC1 is covered by full primitive/style/transform tests; AC2 by fragment scans and
  `normalChromeVisible=false`; AC3 by `data-athena-*` proof and no DOM-id assertions; AC4 by invalid
  IR/paint/XML/sweep tests plus ten-symbol LSP integration; AC5 by this record, regression, audit,
  encoding check, and cleanup ledger.
- Polish/purge: no one-off SVG path data moved upstream and no frontend JSX was added. Existing
  `PresentationSvgPath`, old box `SvgRenderer`, and frontend `svg_path` compatibility remain active
  and are ledgered for migration only after Story 3.3/live graph coverage.
- Final acceptance review: PASS for AC1-AC5; no unresolved Story 3.2 finding remains.

### File List

- `_bmad-output/implementation-artifacts/m33/3-2-implement-svg-adapter-for-graphic-primitive-ir.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/M33IecDrawingSvgCatalogService.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M33IecDrawingSvgCatalogServiceTest.kt`
- `kernel/svg-renderer/build.gradle.kts`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/GraphicPrimitiveSvgAdapter.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/GraphicPrimitiveSvgAdapterTest.kt`

## Change Log

- 2026-07-23: Expanded BMAD story context and fixed generic adapter, viewBox deferral,
  service-to-SVG integration, and compatibility-cleanup boundaries.
- 2026-07-23: Implemented and verified the generic SVG fragment adapter and governed ten-symbol
  production LSP catalog path.
- 2026-07-23: Completed deep edge-case review, polish/purge evidence, and story closeout.
