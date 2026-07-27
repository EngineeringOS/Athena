---
story_id: 4.1
story_key: 4-1-add-sheet-frame-title-block-and-zones
epic: 4
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 4.1: Add Sheet Frame, Title Block, And Zones

## Status

Done

## Story

As a controls engineer, I want a real drawing sheet frame so the canvas reads as documentation.

## Acceptance Criteria

- Composition emits frame, title block, zones/grid references, and governed margins.
- Bounds proof includes frame/title block without hard-coded oversized canvas constants.
- Title block fields are projection facts, not semantic source truth.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED compiler tests for exact content-derived sheet, frame, drawing-area, title-block,
  margin, column-zone, and row-zone bounds with non-zero/negative origins. (AC: 1,2)
- [x] Add RED projection-authority tests proving title, family, sheet number, revision, format,
  orientation, frame id/style, and named zones come only from `ProjectionSheetPublication`. (AC: 3)
- [x] Add RED fail-closed tests for missing/invalid content bounds, invalid margin/title-block policy,
  blank/duplicate zone labels, malformed projection facts, arithmetic overflow, and policy maximum
  sheet overflow. (AC: 1..3)
- [x] Add generic `:kernel:drawing-composition` module depending only on `projection-model` and
  `representation-model`; keep models, deterministic compiler, diagnostics, and proof cohesive.
  (AC: 1..3)
- [x] Emit renderer-neutral frame, title-block, named-zone, coordinate-grid-zone, governed-margin,
  drawing-area, and projected-sheet bounds facts. (AC: 1,2)
- [x] Emit deterministic proof with content/frame/title/drawing/sheet bounds, projection/policy
  authorities, zone ids, policy identity, and no source/CAD/DOM/SVG authority. (AC: 1..3)
- [x] Add transport-safe proof/fact serialization tests without teaching the compiler JSON, LSP,
  SVG, Theia, or source syntax. (AC: 1..3)
- [x] Audit `ProjectionSheetPublication`, M30 composition intent/proof, current Workbench sheet
  fallback, and Story 3.3 root canvas; remove no active path and ledger exact later integration.
  (AC: 4)
- [x] Run focused module, affected projection/representation modules, repository, and encoding
  verification strictly sequentially on Windows. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 AD-4, AD-8.
- Keep composition as facts, not CAD database.
- Add a dedicated `kernel/drawing-composition` module because M33's architecture spine names this
  compiler boundary explicitly. Do not expand the old M30 proof helper into the new authority.
- Compiler input is one typed `ProjectionSheetPublication`, validated resolved Graphic Primitive
  content bounds, and one explicit drawing composition policy. It does not read `.athena`, package
  files, DOM, viewport dimensions, file count, or SVG.
- Policy supplies exact content-to-frame margin, frame-to-sheet inset, title-block height, maximum
  sheet width/height, and ordered column/row labels. Do not guess IEC paper geometry or zone labels.
- Derivation v1:
  - frame expands content by content margin and reserves title-block height below content;
  - sheet expands frame by frame inset;
  - title block occupies the governed bottom band inside the frame;
  - drawing area is the frame minus title block;
  - column/row zone bounds divide the drawing area using policy-provided ordered labels.
- Every derived number must remain finite and every extent positive. A derived sheet exceeding the
  explicit policy maximum returns `drawing.composition.content.out-of-sheet` and no plan/proof.
- `ProjectionSheetPublication.titleBlock`, `revisionMetadata`, `pageSize`, `frame`, and
  `coordinateZones` are projection facts. The compiler may carry them into facts/proof but must not
  reinterpret them as semantic source properties.
- Named publication zones and generated coordinate-grid zones are different facts. Preserve both.
- Story 4.1 emits structured facts and proof, not Graphic Primitive/SVG painting. Epic 4 follow-up
  and Epic 6 product integration consume this boundary; do not claim live Workbench rendering yet.
- No external library or web research is needed. Use Kotlin/JDK only; M33 makes an IEC-referenced,
  not IEC-compliance, claim.

## Architecture Compliance

- Bind to AD-4: composition owns renderer-neutral document facts, never `.athena` mutation or final
  engineering truth.
- Bind to AD-8: bounds derive from validated content, explicit policy, title block, and margins; no
  `1680x1188`, A3 pixel constant, viewport size, DOM bounding box, or file-count inference.
- Preserve dependency direction: projection + representation facts -> drawing composition facts.
- Do not depend on compiler, package-runtime, presentation, svg-renderer, LSP, or Theia modules.
- Do not change `ProjectionSheetPublication` defaults in this story; consume and validate the typed
  contract already used by M26-M32.

## File Structure Requirements

- Register `:kernel:drawing-composition` in `settings.gradle.kts`.
- Use `DrawingSheetCompositionModels.kt` for closely related ids, facts, proof, diagnostics, and
  transport maps.
- Use `DrawingSheetCompositionCompiler.kt` for the one cohesive derivation/validation flow.
- Keep matching tests in `kernel/drawing-composition/src/test/kotlin/...`.
- Split only if a file exceeds the repository's 200-300 line mixed-responsibility threshold.

## Testing

- Composition tests for frame, zones, title block, and bounds.
- Negative test for out-of-sheet content.
- Assert exact formulas for positive, negative, and fractional content origins/dimensions.
- Assert repeated compilation and transport serialization are byte-for-byte/data-equal.
- Assert all diagnostics include code, authority, subject, and deterministic message.
- Assert invalid input returns no partial plan or proof.
- Run `:kernel:drawing-composition:test`, `:kernel:projection-model:test`,
  `:kernel:representation-model:test`, full `gradlew test`, and encoding audit sequentially.

## Evidence Plan

- Structured proof shows sheet frame facts and derived bounds.
- AC1: exact compiler facts and transport assertions for frame/title/zones/margins.
- AC2: exact derived bounds, overflow, maximum-sheet, and no-hard-coded-source scans.
- AC3: projection publication mapping tests and no source/semantic authority imports.
- AC4: Dev Agent Record, cleanup ledger, regressions, encoding audit, and adversarial review.

## Polish And Purge

Audit old sheet/display-title fallbacks for stale authority leaks.

Keep M30 `SchematicCompositionIntentCompiler` and `M30ControlSheetCompositionProofCompiler` while
their electrical authoring/runtime tests and callers remain. Ledger migration instead of silently
creating a false deletion claim. Story 3.3 root composition remains the root SVG authority and must
later consume resolved sheet bounds rather than be duplicated here.

## Previous Story Intelligence

- Epic 3 established validated Graphic Primitive IR, the generic SVG fragment adapter, and root SVG
  composition from exact IR bounds plus governed margin.
- Story 3.3 found that finite inputs can overflow derived geometry; Story 4.1 must validate every
  derived bound before constructing proof or transport output.
- Story 3.3 intentionally left active box `SvgRenderer`, `PresentationSvgPath`, and frontend
  `svg_path` compatibility paths ledgered. Do not mix that live migration into this fact compiler.
- `ProjectionSheetPublication` already owns page format/orientation, frame identity/style, named
  zones, title block, revision metadata, and view composition. Reuse it rather than introducing a
  second document metadata source.
- Existing M30 `SchematicCompositionIntentCompiler` emits broad memberships only and has live
  callers. It is not sufficient for frame geometry and is not safe to delete in this story.

## Git Intelligence

- Baseline remains M32 commit `0311ad6`; M33 is one uncommitted milestone change.
- Reuse `GraphicBounds` from `representation-model` and `ProjectionSheetPublication` from
  `projection-model`; do not add another generic rectangle or title metadata model upstream.
- Gradle 9.6.1/Kotlin/JDK 25 conventions and `kotlin.test` match existing modules.
- Never add `.tools`, generated Theia bundles, build outputs, or reference/QET sources.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: module/compiler references were absent, so contract tests could not compile.
- GREEN: added deterministic sheet composition over projection publication, Graphic Primitive
  bounds, and explicit policy.
- RED/GREEN: nullable bounds, arithmetic overflow, policy maximum overflow, and coordinate-zone
  underflow each failed before the corresponding fail-closed validation was added.
- REVIEW: adversarial review found that frame/title/zone facts conflated projection/policy metadata
  authority with derived-bounds authority, and proof omitted named-zone ids. Both findings were
  fixed with explicit authority fields and transport assertions.
- FOCUSED: `:kernel:drawing-composition:test`, `:kernel:projection-model:test`, and
  `:kernel:representation-model:test` passed sequentially.
- FRONTEND: `yarn test` passed all 204 tests, including the composition authority guard.
- FULL REGRESSION: `gradlew test` passed with 151 actionable tasks before final evidence refresh.

### Completion Notes List

- Added generic renderer-neutral sheet composition facts for frame, drawing area, title block,
  sheet bounds, named zones, coordinate zones, and governed margins.
- Derived every bound from validated content plus explicit policy; invalid, non-finite, undersized,
  or oversized results fail closed with deterministic authority-bearing diagnostics.
- Separated projection metadata authority, presentation-policy label/margin authority, and
  drawing-composition bounds authority in facts and transport.
- AC-to-evidence: AC1 is covered by exact positive/negative-origin fact and transport assertions;
  AC2 by exact formulas, overflow/underflow/maximum tests, and the frontend no-constant scan; AC3
  by projection-publication field and split-authority assertions; AC4 by the ledger, sequential
  regression evidence, encoding audit, and adversarial review.
- Polish/purge: removed no active compatibility path. The M30 membership-only compiler and live
  Workbench A3/A4/960x540 fallback remain explicitly ledgered for Stories 4.2, 6.2, and 7.2. The
  new compiler does not claim live Workbench consumption.
- Final acceptance review: PASS for AC1-AC4. Fresh full Gradle (151 tasks), frontend (204
  tests), encoding, and diff checks passed after the adversarial fixes.

### File List

- `_bmad-output/implementation-artifacts/m33/4-1-add-sheet-frame-title-block-and-zones.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m33-sheet-composition-authority.test.mjs`
- `kernel/drawing-composition/build.gradle.kts`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetCompositionCompiler.kt`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetCompositionModels.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetCompositionCompilerTest.kt`
- `settings.gradle.kts`

## Change Log

- 2026-07-23: Expanded BMAD story context and fixed projection-fact, content-derived bounds,
  policy, module, diagnostics, proof, and legacy-path boundaries.
- 2026-07-23: Implemented renderer-neutral sheet composition facts, deterministic transport/proof,
  fail-closed validation, authority regression guard, and legacy integration ledger.
- 2026-07-23: Fixed adversarial authority-conflation and named-zone-proof findings and moved the
  story to review.
- 2026-07-23: Passed fresh focused/full/frontend/encoding verification and closed the story.
