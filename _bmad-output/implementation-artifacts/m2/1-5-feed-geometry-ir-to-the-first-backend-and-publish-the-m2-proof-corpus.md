---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 1.5: Feed Geometry IR To The First Backend And Publish The M2 Proof Corpus

Status: done

## Story

As a platform engineer,
I want the first downstream backend and a minimal proof corpus to consume `Geometry IR` directly,
so that M2 demonstrates the full `Engineering IR -> Layout IR -> Geometry IR -> backend` consequence chain.

## Acceptance Criteria

1. Given supported `Geometry IR` artifacts exist for the first proof pair, when the first downstream backend is executed, then Athena feeds that backend directly from `Geometry IR`, and the backend does not privately reconstruct layout or geometry from `Engineering IR`.
2. Given SVG is the current proven downstream backend in the workspace, when backend proof coverage is defined for M2, then SVG or another explicitly approved current backend consumes geometry-backed projection artifacts, and the output remains deterministic for the same semantic, layout, and geometry inputs.
3. Given M2 requires published proof fixtures, when example coverage is added, then Athena adds an `examples/m2/` corpus for synchronized `cabinet` and `wiring` projections from the same semantic seed, and the proof corpus is limited to the minimum expected projection artifacts or expectation files needed to verify the first M2 backend chain.
4. Given the first backend proof and example corpus are implemented, when the standard Java `25` build and regression checks are executed, then the workspace builds successfully and the M2 examples prove geometry-backed downstream output, and Epic 1 demonstrates the explicit projection pipeline end to end without turning fixture publication into a separate broad documentation effort.

## Tasks / Subtasks

- [x] Migrate the first backend seam so it consumes explicit `Geometry IR` rather than canonical semantics directly. (AC: 1, 2)
  - [x] Update the SVG-facing derivation path so renderer-facing output is built from `Geometry IR`.
  - [x] Keep the backend downstream-only: no private `Engineering IR -> SVG` geometry reconstruction.
  - [x] Preserve deterministic output for the same geometry input and preserve canonical semantic identity through render artifacts.
- [x] Thread geometry-backed backend output through compiler-owned success models and existing consumers. (AC: 1, 2, 4)
  - [x] Make compiler success/results and existing runtime-facing seams consume the geometry-backed backend path without bypassing the new geometry stage.
  - [x] Preserve the current JVM-first runtime and viewer behavior while changing the backend source-of-truth to `Geometry IR`.
  - [x] Keep all newly introduced or changed core Kotlin classes documented with KDoc.
- [x] Publish the minimal `examples/m2/` proof corpus for the synchronized `cabinet` and `wiring` projections. (AC: 3, 4)
  - [x] Add the shared semantic seed used by the first proof pair.
  - [x] Add only the minimum expectation artifacts needed to prove deterministic geometry-backed backend output for both supported views.
  - [x] Add concise README coverage for the new M2 examples corpus if the folder would otherwise be unclear.
- [x] Add deterministic regression coverage for the geometry-backed backend chain. (AC: 1, 2, 4)
  - [x] Add or update compiler and/or backend tests that prove SVG rendering now depends on `Geometry IR`.
  - [x] Add proof checks that the same semantic seed yields stable `cabinet` and `wiring` backend artifacts through the full chain.
  - [x] Preserve green regression coverage for existing runtime and renderer modules under Java `25`.
- [x] Update module documentation for the geometry-backed backend boundary. (AC: 4)
  - [x] Update `:kernel:svg-renderer` and compiler docs to state that SVG is fed from `Geometry IR`.
  - [x] Document the role of `examples/m2/` as architecture proof fixtures rather than disposable demo files.

## Dev Notes

### Story Intent

- Story `1.5` closes Epic `1` by proving the full M2 consequence chain end to end: `Engineering IR -> Layout IR -> Geometry IR -> backend`.
- Story `1.4` already introduced deterministic explicit `Geometry IR`; this story must consume that geometry directly instead of keeping the older semantic-to-SVG shortcut.
- The scope is intentionally narrow: one approved backend path plus a minimal proof corpus, not a broad export system or published interchange format.

### Architecture Guardrails

- Align to `manifesto/docs/architecture/09-layout-and-geometry.md`: geometry remains the precise renderable consequence of layout intent and may not become a second semantic authority.
- Align to `ARCHITECTURE-SPINE.md`:
  - `AD-1`: `Layout IR` and `Geometry IR` remain explicit kernel-owned projection contracts.
  - `AD-2`: deterministic projection derivation stays compiler-owned.
  - `AD-5`: canonical semantic identity survives across semantic, layout, geometry, and backend artifacts.
  - `AD-6`: `Geometry IR` is the only renderer-facing projection contract for M2.
  - `AD-8`: backend consumption belongs in `:kernel:svg-renderer`; proof fixtures belong in `examples/m2/`.

### Technical Requirements

- Replace the existing direct semantic-to-SVG derivation seam with a geometry-backed seam.
- Treat SVG as the current approved first backend unless a different already-existing backend is clearly simpler; do not add a new backend technology in this story.
- Preserve deterministic outputs for both supported views from the same semantic seed:
  - `cabinet`
  - `wiring`
- Keep the runtime/viewer path working after the backend source changes.
- Keep the proof corpus minimal:
  - one shared semantic seed
  - only the expectation artifacts needed to prove geometry-backed downstream output
  - no extra UX, export workflow, or broad documentation expansion

### Architecture Compliance

- `Geometry IR` remains downstream of `Layout IR` and upstream of the backend.
- `:kernel:svg-renderer` may consume `Geometry IR` and produce SVG artifacts, but it may not reconstruct layout intent or semantic meaning from `Engineering IR`.
- Runtime and UI remain consumers of compiler/runtime output; they do not gain private projection derivation here.
- `examples/m2/` is an architecture-proof corpus and should stay small, deterministic, and reviewable.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Keep dependency changes minimal and continue using the version catalog.
- Reuse the current Kotlin/JUnit test approach.

### File Structure Requirements

- Likely update files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
  - `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`
  - `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderer.kt`
  - `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererTest.kt`
  - `kernel/compiler/README.md`
  - `kernel/compiler/README.zh-CN.md`
  - `kernel/svg-renderer/README.md`
  - `kernel/svg-renderer/README.zh-CN.md`
- Likely add files:
  - `examples/m2/README.md`
  - `examples/m2/README.zh-CN.md`
  - minimal proof corpus artifacts under `examples/m2/`
- Preserve current behavior in:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
  - existing M1 examples and modules outside the geometry-backed backend path

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :kernel:svg-renderer:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof checks:
  - the same `Geometry IR` input yields identical backend output
  - both supported views produce deterministic geometry-backed backend artifacts from one semantic seed
  - the backend path no longer depends on private semantic-to-geometry reconstruction
  - the current runtime and existing renderer tests remain green
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `1.3` established explicit `Layout IR` for `cabinet` and `wiring`.
- Story `1.4` established explicit `Geometry IR` with deterministic derived placements and paths, and intentionally kept the older backend path unchanged for regression safety.
- The next correct move is to consume `Geometry IR` directly, not to widen scope into runtime projection sessions or UI view switching yet.
- The repo already enforces:
  - grouped module layout
  - Java `25`
  - KDoc on core Kotlin classes
  - English and Chinese README coverage for core modules

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` remains the current baseline commit.
- The M2 sequence so far has established durable projection modules, typed view definitions, explicit layout derivation, and explicit geometry derivation.
- Story `1.5` should finish Epic `1` by making backend consumption honor the explicit geometry stage and by publishing the smallest useful proof corpus.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 1.5: Feed Geometry IR To The First Backend And Publish The M2 Proof Corpus`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-2`
  - `FR-3`
  - `FR-12`
  - `NFR-1`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-1`
  - `AD-2`
  - `AD-5`
  - `AD-6`
  - `AD-8`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `_bmad-output/implementation-artifacts/m2/1-4-derive-geometry-ir-and-preserve-canonical-identity-across-projection-layers.md`
- `kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/GeometryIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderer.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`

## Story Completion Status

- Status: done
- Completion note: SVG now consumes explicit `Geometry IR`, the compiler and runtime viewer path derive their downstream model from geometry, and `examples/m2/` publishes the first synchronized cabinet and wiring backend proof corpus.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context creation inputs:
  - `git rev-parse HEAD`
  - `git log -5 --pretty=format:"%h %s"`
- Geometry-backed backend verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain --rerun-tasks :kernel:svg-renderer:test :kernel:compiler:test :kernel:runtime:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Migrated the SVG backend seam so `SvgRenderer` consumes explicit `Geometry IR` directly while the compiler rebuilds the runtime viewer model from the same geometry document.
- Updated `AthenaCompiler` so the downstream pass selects a supported geometry document, emits geometry-backed SVG, and preserves scoped runtime refresh for the current command path.
- Added `examples/m2/` with one shared semantic seed plus published `cabinet` and `wiring` SVG expectations for the first backend proof chain.
- Added regression coverage in `M2GeometryBackendExamplesTest` and `SvgRendererModuleMarkerTest`, and updated existing compiler expectations to the geometry-backed cabinet output.
- Updated English and Chinese README coverage for the compiler, SVG renderer, and M2 examples corpus to document the new backend boundary honestly.

## File List

- `_bmad-output/implementation-artifacts/m2/1-5-feed-geometry-ir-to-the-first-backend-and-publish-the-m2-proof-corpus.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `examples/README.md`
- `examples/m0/demo-cabinet.svg`
- `examples/m2/README.md`
- `examples/m2/README.zh-CN.md`
- `examples/m2/demo-cabinet.athena`
- `examples/m2/demo-cabinet.expectation.txt`
- `examples/m2/demo-cabinet.cabinet.svg`
- `examples/m2/demo-cabinet.wiring.svg`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M2GeometryBackendExamplesTest.kt`
- `kernel/svg-renderer/README.md`
- `kernel/svg-renderer/README.zh-CN.md`
- `kernel/svg-renderer/build.gradle.kts`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderer.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererModuleMarkerTest.kt`

## Change Log

- 2026-07-06: Created Story `1.5` with geometry-backed backend and proof-corpus implementation guidance.
- 2026-07-06: Implemented the first geometry-backed SVG backend path, updated compiler/runtime-facing downstream models, published the minimal `examples/m2/` proof corpus, and verified the build under Java `25`.
