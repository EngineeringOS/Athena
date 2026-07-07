---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 1.4: Derive Geometry IR And Preserve Canonical Identity Across Projection Layers

Status: done

## Story

As a platform engineer,
I want Athena to derive `Geometry IR` from `Layout IR` while preserving canonical identity,
so that renderable output can be produced from explicit projection contracts without losing the link back to semantic truth.

## Acceptance Criteria

1. Given a derived `Layout IR` for one supported view, when geometry derivation is requested, then Athena derives explicit `Geometry IR` from that layout artifact, and the geometry derivation stage is distinct from both semantic lowering and layout derivation.
2. Given `Geometry IR` is renderer-facing, when geometry artifacts are inspected, then they contain precise renderable structure such as placements, paths, and other output-ready primitives, and the geometry stage does not reinterpret authored DSL text or mutate canonical semantic state.
3. Given one semantic object appears in semantic, layout, and geometry layers, when those layers are inspected together, then the same canonical semantic identity can be followed across `Engineering IR`, `Layout IR`, and `Geometry IR`, and projection-local identifiers never replace the stable semantic identity reference.
4. Given geometry derivation and identity preservation are implemented, when the standard Java `25` build and regression checks are executed, then the workspace builds successfully and the first supported views produce deterministic `Geometry IR`, and identity-preserving projection behavior is verifiable in tests or proof fixtures.

## Tasks / Subtasks

- [x] Enrich the durable `Geometry IR` contract so it can express renderer-facing geometry rather than only loose bounds. (AC: 2, 3)
  - [x] Add explicit geometry types for canvas/viewport, placements, paths, and other minimal output-ready primitives needed by the first proof pair.
  - [x] Keep canonical semantic identity first-class on geometry artifacts.
  - [x] Keep the model renderer-facing and downstream-only: no semantic reinterpretation and no view-session ownership.
- [x] Add a compiler-owned geometry derivation seam that consumes `Layout IR`. (AC: 1, 4)
  - [x] Add deterministic compiler logic that derives `Geometry IR` from one `Layout IR` document.
  - [x] Keep geometry derivation distinct from both semantic lowering and layout derivation.
  - [x] Expose derived geometry artifacts through compiler-owned APIs or compiler success results for later stories to consume.
- [x] Derive deterministic `cabinet` and `wiring` geometry while preserving canonical identity. (AC: 1, 2, 3)
  - [x] Make `cabinet` geometry encode structural placements derived from the structural layout.
  - [x] Make `wiring` geometry encode connectivity-oriented placements and paths derived from the connectivity layout.
  - [x] Preserve the same canonical semantic identities across semantic, layout, and geometry artifacts.
- [x] Cover the geometry derivation path with deterministic regression tests. (AC: 1, 3, 4)
  - [x] Add or update geometry-model tests for the richer geometry contract shapes.
  - [x] Add compiler tests that prove `cabinet` and `wiring` geometry derives deterministically from the same semantic example through the layout stage.
  - [x] Preserve the current M1 runtime viewer and SVG path in this story; do not switch the backend to `Geometry IR` yet.
- [x] Update module documentation for the new explicit geometry stage. (AC: 4)
  - [x] Update `:kernel:geometry-model` docs to describe the richer geometry contract.
  - [x] Update compiler docs if needed to explain that explicit `Geometry IR` now exists after `Layout IR`.
  - [x] Do not claim backend migration, runtime projection sessions, or UI view switching in this story.

## Dev Notes

### Story Intent

- Story `1.4` introduces the first explicit `Geometry IR` stage after the newly explicit `Layout IR` stage from Story `1.3`.
- The success condition is not “the backend now consumes geometry.” It is “the compiler can deterministically derive renderer-facing geometry from layout while preserving canonical identity.”
- Story `1.5` will migrate the first backend proof to consume `Geometry IR` directly. Story `1.4` must stop before that backend switch.

### Architecture Guardrails

- Align to the manifesto and `manifesto/docs/architecture/09-layout-and-geometry.md`: geometry is the precise renderable consequence of layout intent and must stay downstream of semantic truth.
- Align to AD-2: deterministic projection derivation remains compiler-owned.
- Align to AD-5: canonical semantic identity must survive across semantic, layout, and geometry layers.
- Align to AD-6: `Geometry IR` is the only renderer-facing projection contract for M2, even if existing backend consumption remains temporarily unchanged in this story.
- Align to AD-8: durable geometry contracts live in `:kernel:geometry-model`; runtime and UI are consumers, not derivation owners.

### Technical Requirements

- Keep the current `Engineering IR -> SvgRenderModel` path working in Story `1.4` for regression safety; backend migration belongs to Story `1.5`.
- Add an explicit compiler-owned geometry derivation stage that consumes the `Layout IR` artifacts introduced in Story `1.3`.
- Derive geometry for both supported views from the same canonical semantic document and its derived layouts:
  - `cabinet`
  - `wiring`
- Geometry artifacts must include precise renderer-facing structure:
  - placements / bounds
  - paths or path points for connectivity
  - other minimal output-ready primitives needed by the first proof pair
- Geometry artifacts must stay deterministic for the same layout input.
- Keep all newly introduced core Kotlin classes documented with KDoc.

### Architecture Compliance

- `Geometry IR` must remain downstream of `Layout IR` and may not bypass it.
- The compiler may interpret layout artifacts into precise geometry, but it may not reinterpret authored DSL text or mutate canonical semantics.
- Runtime and UI may inspect geometry later, but they must not own the derivation rules here.
- Do not migrate SVG/backend rendering to `Geometry IR` in this story.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Keep dependency changes minimal and local to the affected modules.
- Reuse the current Kotlin/JUnit test approach.

### File Structure Requirements

- Likely update files:
  - `kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt`
  - `kernel/geometry-model/src/test/kotlin/com/engineeringood/athena/geometry/GeometryModelTest.kt`
  - `kernel/compiler/build.gradle.kts`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
  - `kernel/geometry-model/README.md`
  - `kernel/geometry-model/README.zh-CN.md`
- Likely add files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GeometryIrDeriver.kt`
- Preserve current behavior in:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
  - `kernel/svg-renderer/**`

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:geometry-model:test :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof tests:
  - repeated derivation for the same layout input and view yields identical `Geometry IR`
  - the compiler exposes both supported geometry projections for the same canonical semantic document
  - `cabinet` and `wiring` geometry preserve canonical semantic identity while differing by geometry consequence
  - the existing runtime viewer and SVG path remain green until Story `1.5`
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `1.3` already introduced explicit `Layout IR` and compiler-owned layout derivation; geometry must consume those layout artifacts rather than reconstruct intent directly from semantic state.
- The repo already enforces:
  - grouped module layout
  - Java `25`
  - KDoc on core Kotlin classes
  - English and Chinese README coverage for core modules
- Preserve the current backend and runtime viewer path until Story `1.5` explicitly migrates backend consumption to `Geometry IR`.

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` remains the current baseline.
- The M2 sequence so far has established durable modules, added typed view definitions, then explicit layout derivation. Story `1.4` should extend that pattern by adding explicit geometry derivation without pulling backend migration forward.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 1.4: Derive Geometry IR And Preserve Canonical Identity Across Projection Layers`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-2`
  - `FR-3`
  - `NFR-1`
  - `NFR-3`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-2`
  - `AD-5`
  - `AD-6`
  - `AD-8`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `_bmad-output/implementation-artifacts/m2/1-3-derive-layout-ir-for-cabinet-and-wiring-views.md`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`

## Story Completion Status

- Status: done
- Completion note: Added explicit compiler-owned `Geometry IR` derivation for `cabinet` and `wiring`, enriched the durable geometry contract, and verified identity-preserving deterministic geometry plus existing runtime/build compatibility under Java `25`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context creation baseline:
  - `git rev-parse HEAD`
- Targeted implementation verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:geometry-model:test :kernel:compiler:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- Full regression verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Expanded `:kernel:geometry-model` so `Geometry IR` can represent renderer-facing canvas dimensions, exact element bounds, and path points while preserving canonical semantic identity.
- Added `GeometryIrDeriver` and compiler APIs that derive supported geometry documents strictly from explicit `Layout IR`.
- `CompilerCompilationSuccess` now exposes derived geometry artifacts alongside layouts and the existing rendering result without breaking the current runtime viewer path.
- Added deterministic compiler tests for the `cabinet` and `wiring` geometry proof pair and verified that semantic-invalid inputs do not derive layout or geometry.
- Updated English and Chinese module documentation for the geometry-model and compiler modules to describe the new explicit geometry stage.

## File List

- `_bmad-output/implementation-artifacts/m2/1-4-derive-geometry-ir-and-preserve-canonical-identity-across-projection-layers.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt`
- `kernel/geometry-model/src/test/kotlin/com/engineeringood/athena/geometry/GeometryModelTest.kt`
- `kernel/geometry-model/README.md`
- `kernel/geometry-model/README.zh-CN.md`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GeometryIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`

## Change Log

- 2026-07-06: Created Story 1.4 with geometry-model, compiler, and manifesto-aligned implementation guardrails.
- 2026-07-06: Implemented deterministic compiler-owned `Geometry IR` derivation for `cabinet` and `wiring`, added regression coverage, and updated module documentation.
